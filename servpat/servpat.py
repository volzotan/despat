from flask import Flask, json, g, render_template, redirect, url_for, request, Response, flash, abort, send_from_directory
from werkzeug.utils import secure_filename
from functools import wraps
import sqlite3
import sys, os
from datetime import datetime, timedelta

DATEFORMAT_INPUT    = "%Y-%m-%d %H:%M:%S.%f"
DATEFORMAT_STORE    = "%Y-%m-%d %H:%M:%S.%f"
DATEFORMAT_OUTPUT   = "%Y.%m.%d - %H:%M:%S.%f"

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

    filter_device = get_filter("device")

    if filter_device is None:
        filter_device = "%"

    data_status = query_db("SELECT * FROM status WHERE deviceId LIKE (?) ORDER BY datetime(timestamp) DESC", (filter_device,))
    data_session = query_db("SELECT * FROM session WHERE deviceId LIKE (?) ORDER BY datetime(start) DESC", (filter_device,))
    data_capture = query_db("SELECT * FROM capture WHERE deviceId LIKE (?) ORDER BY datetime(recordingTime) DESC", (filter_device,))
    data_event 	= query_db("SELECT * FROM event WHERE deviceId LIKE (?) ORDER BY datetime(timestamp) DESC", (filter_device,))
    data_upload = query_db("SELECT * FROM upload WHERE deviceId LIKE (?) ORDER BY datetime(timestamp) DESC", (filter_device,))

    graph_status = None

    if filter_device != "%":
        graph_status = rows_to_list(data_status)

    return render_template("overview.html", data_status=data_status, 
                                            data_session=data_session, 
                                            data_capture=data_capture, 
                                            data_event=data_event, 
                                            data_command=(), 
                                            data_upload=data_upload,
                                            graph_status=graph_status)


@app.route("/device/<device_id>")
def device(device_id):
    db = get_db()

    if device_id is None:
        return render_template("device.html")

    device_status = query_db("SELECT * FROM status WHERE deviceid LIKE (?) ORDER BY datetime(timestamp) DESC", (device_id,))

    # TODO: add some serious filtering
    now = datetime.now()
    comp = now - timedelta(days=3)
    graph_status = query_db("SELECT * FROM status WHERE deviceId LIKE (?) AND timestamp > (?) ORDER BY datetime(timestamp) DESC", (device_id, comp))
    graph_status = rows_to_list(graph_status)

    cur = db.execute("SELECT * FROM upload WHERE deviceid LIKE (?) ORDER BY datetime(timestamp) DESC LIMIT 4", (device_id,))
    device_upload = cur.fetchall()

    return render_template("device.html", graph_status=graph_status, data_status=device_status, data_upload=device_upload, page_title=device_id)


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


@app.route("/knock")
def knock():

    # TODO

    return ("", 204)


@app.route("/status", methods=["POST"])
def status():
    content = request.get_json()

    if content is None:
        return ("empty request", 400)

    if (len(content) == 0):
    	print ("no status to import")
    	return ("", 204)

    # insert into db
    for s in content:
	    values = [  s["deviceId"], 
	    			datetime.now().strftime(DATEFORMAT_STORE),

	    			s["statusId"],
        			time_to_store(s["timestamp"]), 
	                s["deviceName"],
	        
	                s["imagesTaken"],
	                s["imagesInMemory"],

	                s["freeSpaceInternal"], 
	                s["freeSpaceExternal"], 

	                s["batteryInternal"],
	                s["batteryExternal"],

	                s["stateCharging"]]

	    db = get_db()
	    db.execute("""
	    	insert into status (deviceId, 
	    						serverTimestamp, 
	    						statusId,
	    						timestamp, 
	    						deviceName, 
	    						imagesTaken, 
	    						imagesInMemory, 
	    						freeSpaceInternal, 
	    						freeSpaceExternal, 
	    						batteryInternal, 
	    						batteryExternal, 
	    						stateCharging
	    	) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", values)
	    db.commit()

    return ("", 204)


@app.route("/session", methods=["POST"])
def session():
    content = request.get_json()

    if content is None:
        return ("empty request", 400)

    if (len(content) == 0):
        print ("no session to import")
        return ("", 204)

    for s in content:

        end = s["end"]
        if end is not None:
            end = (datetime.strptime(end, DATEFORMAT_INPUT)).strftime(DATEFORMAT_STORE)
        else:
            end = None

        # insert into db
        values = [  s["deviceId"], 
                    datetime.now().strftime(DATEFORMAT_STORE),

                    s["sessionId"],
                    time_to_store(s["start"]), 
                    end,
            
                    s["latitude"],
                    s["longitude"],

                    s["resumed"]]

        db = get_db()
        db.execute("""
            insert into session (   deviceId, 
                                    serverTimestamp, 
                                    sessionId,
                                    start, 
                                    end, 
                                    latitude, 
                                    longitude, 
                                    resumed
            ) values (?, ?, ?, ?, ?, ?, ?, ?)""", values)
        db.commit()

    return ("", 204)



@app.route("/capture", methods=["POST"])
def capture():
    content = request.get_json()

    if content is None:
        return ("empty request", 400)

    if (len(content) == 0):
        print ("no capture to import")
        return ("", 204)

    for c in content:

        # insert into db
        values = [  c["deviceId"], 
                    datetime.now().strftime(DATEFORMAT_STORE),

                    c["captureId"],
                    c["sessionId"],
                    time_to_store(c["recordingTime"])]

        db = get_db()
        db.execute("""
            insert into capture (   deviceId, 
                                    serverTimestamp, 
                                    captureId,
                                    sessionId, 
                                    recordingTime
            ) values (?, ?, ?, ?, ?)""", values)
        db.commit()

    return ("", 204)



@app.route("/event", methods=["POST"])
def event():
    content = request.json

    if content is None:
        return ("empty request", 400)

    if (len(content) == 0):
        print ("no event to import")
        return ("", 204)

    for e in content:
        # if content["eventtype"] is not in EVENTTYPESDICTIONARY... # TODO

        # insert into db
        values = [  e["deviceId"], 
                    datetime.now().strftime(DATEFORMAT_STORE),

                    e["eventId"],
                    time_to_store(e["timestamp"]), 

                    e["type"],
                    e["payload"]]

        db = get_db()
        db.execute("""
            insert into event ( deviceId, 
                                serverTimestamp, 
                                eventId,
                                timestamp, 
                                type, 
                                payload
            ) values (?, ?, ?, ?, ?, ?)""", values)
        db.commit()

    return ("", 204)


@app.route("/upload", methods=["POST"])
def upload():

    # check free space on server
    statvfs = os.statvfs(app.config["UPLOAD_FOLDER"])
    free_space = statvfs.f_frsize * statvfs.f_bavail
    if (free_space < app.config["MIN_FREE_SPACE"]):
        abort(500, "no free space left") # 507 Insufficient storage

    # TODO: get device id from session
    # content = {}
    # content["deviceId"] = "123"
    # content["timestamp"] = datetime.now()

    content = request.form
    timestamp = datetime.strptime(content["timestamp"], DATEFORMAT_INPUT)

    if "file" not in request.files:
        app.logger.warn("image file missing in request")
        #flash('No file part')
        abort(400, "image file missing in request")
    imagefile = request.files["file"]
    if imagefile.filename == "":
        app.logger.warn("empty image filename in request")
        abort(400, "empty image filename in request")
        return redirect(request.url)
    filename = "{}_{}.jpg".format(content["deviceId"], timestamp.strftime(DATEFORMAT_IMG)[:-3])
    # filename = device_id + "_" + timestamp.strftime(DATEFORMAT_IMG) + "_" + secure_filename(imagefile.filename)
    unique_filename = get_unique_filename(app.config["UPLOAD_FOLDER"], filename)
    full_filename = os.path.join(app.config["UPLOAD_FOLDER"], unique_filename)
    if full_filename is None:
        app.logger.error("no new filenames available anymore")
        abort(500, "no filename available")
    imagefile.save(full_filename)
    app.logger.info("uploaded: {}".format(unique_filename))

    # insert into db

    timestamp = datetime.strptime(content["timestamp"], DATEFORMAT_INPUT)
    timestamp = timestamp.strftime(DATEFORMAT_STORE)
    serverTimestamp = datetime.now().strftime(DATEFORMAT_STORE)

    values = [  content["deviceId"], 
                serverTimestamp,
                timestamp,
                unique_filename
            ]
    db = get_db()
    db.execute("insert into upload (deviceId, serverTimestamp, timestamp, filename) values (?, ?, ?, ?)", values)
    db.commit()

    return ("", 204)


@app.route('/image/<path:path>')
def image(path):
    return send_from_directory(app.config["UPLOAD_FOLDER"], path)


# @app.route("/sync")
# def sync():
#     return "sync"


@app.route("/sync/<table>", methods=["POST"])
def sync(table):
    content = request.get_json()

    if content is None:
        return ("empty request", 400)

    ids = []

    if (table == "status"): 
        for o in content:
            duplicate = query_db("SELECT * FROM status WHERE deviceId LIKE (?) AND statusId LIKE (?) AND timestamp LIKE (?)", (o["deviceId"], o["id"], time_to_store(o["timestamp"])))
            if duplicate is None or len(duplicate) == 0:
                ids.append(o["id"])
    elif (table == "session"): 
        for o in content:
            duplicate = query_db("SELECT * FROM session WHERE deviceId LIKE (?) AND sessionId LIKE (?) AND start LIKE (?)", (o["deviceId"], o["id"], time_to_store(o["timestamp"])))
            if duplicate is None or len(duplicate) == 0:
                ids.append(o["id"])
    elif (table == "capture"): 
        for o in content:
            duplicate = query_db("SELECT * FROM capture WHERE deviceId LIKE (?) AND captureId LIKE (?) AND recordingTime LIKE (?)", (o["deviceId"], o["id"], time_to_store(o["timestamp"])))
            if duplicate is None or len(duplicate) == 0:
                ids.append(o["id"])
    elif (table == "event"):
        for o in content:
            duplicate = query_db("SELECT * FROM event WHERE deviceId LIKE (?) AND eventId LIKE (?) AND timestamp LIKE (?)", (o["deviceId"], o["id"], time_to_store(o["timestamp"])))
            if duplicate is None or len(duplicate) == 0:
                ids.append(o["id"])
    else:
        abort(404)

    response = app.response_class(
        response=json.dumps(ids),
        status=200,
        mimetype="application/json"
    )

    return response



# --------------------------------------------------------------------------- #

@app.template_filter("suppressnegative")
def suppressnegative_filter(e):
    if e < 0:
        return ""
    return e


@app.template_filter("suppresszero")
def suppresszero_filter(e):
    if e < 1:
        return ""
    return e
    

@app.template_filter("suppressnull")
def suppressnull_filter(e):
    if e is None:
        return ""
    return e


@app.template_filter("bool")
def bool_filter(e):
    if e == 0:
        return "☐"
    if e == 1:
        return "☒"
    return e


@app.template_filter("eventtype")
def eventtype_filter(e):
    types = {
        10: "INIT",
        20: "BOOT",
        30: "SHUTDOWN",

        40: "START",
        41: "STOP",
        42: "RESTART",

        45: "INFO",

        50: "ERROR",
        51: "SCHEDULE GLITCH",

        60: "SLEEP MODE CHANGE",
        61: "DISPLAY ON",
        62: "DISPLAY OFF",
    }

    try:
        return types[e]
    except KeyError as ke:
        return e


@app.template_filter("location")
def location_filter(loc):
    if loc is None or len(loc) != 2:
        return ""
    return "http://maps.google.com/?q={},{}".format(loc[0], loc[1])


@app.template_filter("dateformat")
def dateformat_filter(inp):
    if inp is None or inp == "":
        return ""
    return datetime.strptime(inp, DATEFORMAT_STORE).strftime(DATEFORMAT_OUTPUT)[:-3]


@app.template_filter("timediff")
def timediff_filter(i):

    if i is None or len(i) != 2:
        return ""

    if i[0] is None or i[1] is None:
        return ""

    start = datetime.strptime(i[0], DATEFORMAT_STORE)
    end   = datetime.strptime(i[1], DATEFORMAT_STORE)
    diff  = end-start
    
    ret = []
    if (diff.days > 0):
        ret.append(str(diff.days))
        ret.append(" day")
        if diff.days > 1:
            ret.append("s")
        ret.append(", ")
    hours = diff.seconds / (60 * 60)
    if (hours > 0):
        ret.append(str(hours))
        ret.append(" hour")
        if hours != 1:
            ret.append("s")
        ret.append(", ")
    minutes = diff.seconds / 60
    if (minutes > 0):
        ret.append(str(minutes))
        ret.append(" minute")
        if minutes != 1:
            ret.append("s")
        ret.append(", ")
    ret.append(str(diff.seconds))
    ret.append(" second")
    if diff.seconds != 1:
        ret.append("s")

    return "".join(ret)



def get_filter(param):
    val = request.args.get(param)
    if val is None:
        return "%"
    else:
        return val


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


def time_to_store(inp):
    ret = datetime.strptime(inp, DATEFORMAT_INPUT)
    return ret.strftime(DATEFORMAT_STORE)


# as taken from http://flask.pocoo.org/docs/0.12/patterns/sqlite3/
def query_db(query, args=(), one=False):
    cur = get_db().execute(query, args)
    rv = cur.fetchall()
    cur.close()
    return (rv[0] if rv else None) if one else rv


def rows_to_list(cursor):
    obj = []
    for item in cursor:
        d = []
        for key in item.keys():
            d.append(item[key])
        obj.append(d)

    return obj #json.dumps(obj)


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

