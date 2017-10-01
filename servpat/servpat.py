from flask import Flask, json, g, render_template, redirect, url_for, request, Response, flash, abort
from werkzeug.utils import secure_filename
from functools import wraps
import sqlite3
import sys, os
from datetime import datetime

DATEFORMAT_INPUT    = "%Y-%m-%d %H:%M:%S.%f"
DATEFORMAT_STORE    = "%Y-%m-%d %H:%M:%S.%f"
DATEFORMAT_OUTPUT   = "%Y.%m.%d | %H:%M:%S.%f"

DATEFORMAT_IMG      = "%Y-%m-%d--%H-%M-%S-%f"

app = Flask(__name__)

app.config.from_pyfile("default.config")
app.config.from_pyfile("grinzold.config", silent=True)
app.config.from_pyfile("corodiak.config", silent=True)

# --------------------------------------------------------------------------- #

@app.route("/")
def root():
    return redirect(url_for("overview", option="all"))

@app.route("/overview/")
def overview_redirect():
    return redirect(url_for("overview", option="all"))

@app.route("/overview/<option>")
def overview(option):
    db = get_db()

    if option is not None:
        pass
        # TODO last hour ...

    cur = db.execute("SELECT * FROM status ORDER BY datetime(timestamp) DESC")
    data_status = cur.fetchall()
    cur = db.execute("SELECT * FROM events ORDER BY datetime(timestamp) DESC")
    data_events = cur.fetchall()
    cur = db.execute("SELECT * FROM uploads ORDER BY datetime(timestamp) DESC")
    data_uploads = cur.fetchall()

    return render_template("overview.html", data_status=data_status, data_events=data_events, data_commands=(), data_uploads=data_uploads)


@app.route("/device/<device_id>")
def device(device_id):
    db = get_db()

    if device_id is None:
        return render_template("device.html")

    print(type(device_id))

    cur = db.execute("SELECT * FROM status WHERE deviceid LIKE (?) ORDER BY datetime(timestamp) DESC", (device_id,))
    device_status = cur.fetchall()

    return render_template("device.html", data_status=device_status, page_title=device_id)


@app.route("/command")
def command():

    # TODO

    data = {
        "command": "SLEEP",
        "params": {
            "duration": "1000"
        }
    }
    response = app.response_class(
        response=json.dumps(data),
        status=200,
        mimetype="application/json"
    )
    return response


@app.route("/status", methods=["POST"])
def status():
    content = request.get_json()

    print(content)

    if content is None:
        return ("empty request", 400)

    timestamp = datetime.strptime(content["timestamp"], DATEFORMAT_INPUT)
    timestamp = timestamp.strftime(DATEFORMAT_STORE)

    # insert into db
    values = [  content["deviceId"], 
                content["deviceName"],
                timestamp, 
                content["numberImages"], 
                content["freeSpaceInternal"], 
                content["freeSpaceExternal"], 
                content["batteryInternal"],
                content["batteryExternal"],
                content["stateCharging"]]

    db = get_db()
    db.execute("insert into status (deviceId, deviceName, timestamp, numberImages, freeSpaceInternal, freeSpaceExternal, batteryInternal, batteryExternal, stateCharging) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", values)
    db.commit()

    return ("", 204)


@app.route("/event", methods=["POST"])
def event():
    content = request.json

    timestamp = datetime.strptime(content["timestamp"], DATEFORMAT_INPUT)
    timestamp = timestamp.strftime(DATEFORMAT_STORE)

    # insert into db
    values = [  content["deviceId"], 
                timestamp, 
                content["eventtype"],
                content["payload"]]

    db = get_db()
    db.execute("insert into events (deviceId, timestamp, eventtype, payload) values (?, ?, ?, ?)", values)
    db.commit()

    return ("", 204)


@app.route("/image", methods=["POST"])
def image():

    # check free space on server
    statvfs = os.statvfs(app.config["UPLOAD_FOLDER"])
    free_space = statvfs.f_frsize * statvfs.f_bavail
    if (free_space < app.config["MIN_FREE_SPACE"]):
        abort(500, "no free space left") # 507 Insufficient storage

    # TODO: get device id from session
    content = {}
    content["deviceId"] = "123"
    content["timestamp"] = datetime.now()

    if "file" not in request.files:
        app.logger.warn("image file missing in request")
        #flash('No file part')
        abort(400, "image file missing in request")
    imagefile = request.files["file"]
    if imagefile.filename == "":
        app.logger.warn("empty image filename in request")
        abort(400, "empty image filename in request")
        return redirect(request.url)
    filename = "{}_{}.jpg".format(content["deviceId"], content["timestamp"].strftime(DATEFORMAT_IMG)[:-3])
    # filename = device_id + "_" + timestamp.strftime(DATEFORMAT_IMG) + "_" + secure_filename(imagefile.filename)
    unique_filename = get_unique_filename(app.config["UPLOAD_FOLDER"], filename)
    full_filename = os.path.join(app.config["UPLOAD_FOLDER"], unique_filename)
    if full_filename is None:
        app.logger.error("no new filenames available anymore")
        abort(500, "no filename available")
    imagefile.save(full_filename)
    app.logger.info("uploaded: {}".format(unique_filename))

    # insert into db
    values = [  content["deviceId"], 
                content["timestamp"].strftime(DATEFORMAT_STORE),
                full_filename
            ]
    db = get_db()
    db.execute("insert into uploads (deviceId, timestamp, filename) values (?, ?, ?)", values)
    db.commit()

    return ("", 204)


@app.route("/sync")
def sync():
    return "sync"

# --------------------------------------------------------------------------- #

@app.template_filter("eventtype")
def eventtype_filter(e):

    types = {
        0x0: "APPSTART",
        0x1: "BOOT",
        0x2: "SHUTDOWN"
    }

    try:
        return types[e]
    except KeyError as ke:
        return e


@app.template_filter("dateformat")
def dateformat_filter(inp):
    return datetime.strptime(inp, DATEFORMAT_STORE).strftime(DATEFORMAT_OUTPUT)[:-3]


def get_unique_filename(path, filename):
    full_filename = os.path.join(path, filename)
    if (os.path.exists(full_filename)):
        index = filename.rfind(".")
        extension = ""
        if (index > -1):
            extension = filename[index:]
            filename = filename[:index]
        for i in range(2, 9999):
            new_filename = filename + "_" + str(i) + extension
            new_full_filename = os.path.join(path, new_filename)
            if (not os.path.exists(new_full_filename)):
                return new_filename
        return None
    return filename


def connect_db():
    rv = sqlite3.connect(app.config["DATABASE"])
    rv.row_factory = sqlite3.Row
    return rv


def get_db():
    if not hasattr(g, 'sqlite_db'):
        g.sqlite_db = connect_db()
    return g.sqlite_db


# as taken from http://flask.pocoo.org/docs/0.12/patterns/sqlite3/
def query_db(query, args=(), one=False):
    cur = get_db().execute(query, args)
    rv = cur.fetchall()
    cur.close()
    return (rv[0] if rv else None) if one else rv


@app.teardown_appcontext
def close_db(error):
    if hasattr(g, 'sqlite_db'):
        g.sqlite_db.close()


def init_db():
    db = get_db()
    with app.open_resource("schema.sql", mode='r') as f:
        db.cursor().executescript(f.read())
    db.commit()


# @app.cli.command('initdb')
# def initdb_command():
#     """Initializes the database."""
#     init_db()
#     print('Initialized the database.')


def install_secret_key(app, filename="secret_key"):
    """Configure the SECRET_KEY from a file
    in the instance directory.

    If the file does not exist, print instructions
    to create it from a shell with a random key,
    then exit.

    """
    filename = os.path.join(app.instance_path, filename)
    try:
        app.config["SECRET_KEY"] = open(filename, "rb").read()
    except IOError:
        print("Error: No secret key. Create it with:")
        if not os.path.isdir(os.path.dirname(filename)):
            print("mkdir -p", os.path.dirname(filename))
        print("head -c 24 /dev/urandom >", filename)
        sys.exit(1)


# initialization tasks

install_secret_key(app)

image_dir = app.config["UPLOAD_FOLDER"]
if not os.path.exists(image_dir):
    os.makedirs(image_dir)

if __name__ == "__main__":
    app.run()

