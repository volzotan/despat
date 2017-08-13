from flask import Flask, json, g, render_template, redirect, request, flash, abort
from werkzeug.utils import secure_filename
import sqlite3
import sys, os

app = Flask(__name__)

app.config.from_pyfile('default.config')
app.config.from_pyfile('corodiak.config')

# --------------------------------------------------------------------------- #

@app.route("/")
def root():
    db = get_db()
    cur = db.execute('select title, text from entries order by id desc')
    entries = cur.fetchall()
    return render_template('show_entries.html', entries=entries)


@app.route("/overview")
def overview():
    db = get_db()
    cur = db.execute('select * from entries order by id desc')
    events = cur.fetchall()
    return render_template('overview.html', events=events)


@app.route("/command")
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
def status():
    content = request.json
    print(content)
    return "status"


@app.route("/image", methods=['POST'])
def image():

    # check free space on server
    statvfs = os.statvfs(app.config["UPLOAD_FOLDER"])
    free_space = statvfs.f_frsize * statvfs.f_bavail
    if (free_space < app.config["MIN_FREE_SPACE"]):
        abort(500, "no free space left") # 507 Insufficient storage

    if 'file' not in request.files:
        app.logger.warn("image file missing in requeest")
        #flash('No file part')
        abort(400, "image file missing in request")
    file = request.files["file"]
    if file.filename == "":
        app.logger.warn("empty image filename in request")
        abort(400, "empty image filename in request")
        return redirect(request.url)
    filename = secure_filename(file.filename)
    file.save(os.path.join(app.config["UPLOAD_FOLDER"], filename))
    app.logger.debug("uploaded: {}".format(filename))
    return ("", 204)


@app.route("/sync")
def sync():
    return "sync"

# --------------------------------------------------------------------------- #

def connect_db():
    rv = sqlite3.connect(app.config["DATABASE"])
    rv.row_factory = sqlite3.Row
    return rv

def get_db():
    if not hasattr(g, 'sqlite_db'):
        g.sqlite_db = connect_db()
    return g.sqlite_db

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

def install_secret_key(app, filename='secret_key'):
    """Configure the SECRET_KEY from a file
    in the instance directory.

    If the file does not exist, print instructions
    to create it from a shell with a random key,
    then exit.

    """
    filename = os.path.join(app.instance_path, filename)
    try:
        app.config['SECRET_KEY'] = open(filename, 'rb').read()
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
