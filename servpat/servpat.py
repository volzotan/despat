from flask import Flask, json, g, render_template, redirect, request, Response, flash, abort
from werkzeug.utils import secure_filename
from functools import wraps
import sqlite3
import sys, os
import datetime

app = Flask(__name__)

app.config.from_pyfile("default.config")
app.config.from_pyfile("corodiak.config")

# --------------------------------------------------------------------------- #

# as taken from: http://flask.pocoo.org/snippets/8/
def check_auth(username, password):
    return username == 'admin' and password == 'secret'


def authenticate():
    return Response("access denied", 401, {'WWW-Authenticate': 'Basic realm="Login Required"'})


def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not app.config["AUTH_DISABLED"]:
            if not auth or not check_auth(auth.username, auth.password):
                return authenticate()
        return f(*args, **kwargs)
    return decorated

# --------------------------------------------------------------------------- #

@app.route("/")
def root():
    entries = query_db("select title, text from entries order by id desc") 
    return render_template('show_entries.html', entries=entries)


@app.route("/overview")
@requires_auth
def overview():
    db = get_db()
    cur = db.execute('select * from status order by id desc')
    status_messages = cur.fetchall()
    return render_template('overview.html', status_messages=status_messages)


@app.route("/command")
@requires_auth
def command():
    data = {
        "command": "SLEEP",
        "params": {
            "duration": "1000"
        }
    }
    response = app.response_class(
        response=json.dumps(data),
        status=200,
        mimetype='application/json'
    )
    return response


@app.route("/status", methods=['POST'])
@requires_auth
def status():
    content = request.json

    # insert into db
    values = [  content["deviceId"], 
                datetime.datetime.now(), #content["timestamp"], # TODO
                content["numberImages"], 
                content["freeSpaceInternal"], 
                content["freeSpaceExternal"], 
                content["batteryInternal"],
                content["batteryExternal"]]
    db = get_db()
    db.execute('insert into status (deviceId, timestamp, numberImages, freeSpaceInternal, freeSpaceExternal, batteryInternal, batteryExternal) values (?, ?, ?, ?, ?, ?, ?)', values)
    db.commit()

    return "status"


@app.route("/image", methods=['POST'])
@requires_auth
def image():

    # check free space on server
    statvfs = os.statvfs(app.config["UPLOAD_FOLDER"])
    free_space = statvfs.f_frsize * statvfs.f_bavail
    if (free_space < app.config["MIN_FREE_SPACE"]):
        abort(500, "no free space left") # 507 Insufficient storage

    # TODO: get device id from session
    device_id = "123"

    if 'file' not in request.files:
        app.logger.warn("image file missing in requeest")
        #flash('No file part')
        abort(400, "image file missing in request")
    imagefile = request.files["file"]
    if imagefile.filename == "":
        app.logger.warn("empty image filename in request")
        abort(400, "empty image filename in request")
        return redirect(request.url)
    filename = device_id + "_" + secure_filename(imagefile.filename)
    unique_filename = get_unique_filename(app.config["UPLOAD_FOLDER"], filename)
    full_filename = os.path.join(app.config["UPLOAD_FOLDER"], unique_filename)
    if full_filename is None:
        app.logger.error("no new filenames available anymore")
        abort(500, "no filename available")
    imagefile.save(full_filename)
    app.logger.debug("uploaded: {}".format(unique_filename))
    return ("", 204)


@app.route("/sync")
@requires_auth
def sync():
    return "sync"

# --------------------------------------------------------------------------- #

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
    with app.open_resource('schema.sql', mode='r') as f:
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

if __name__ == '__main__':
    app.run()

