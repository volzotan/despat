from flask import Flask
from flask import json
import sqlite3
import os

app = Flask(__name__)
app.config.from_object(__name__)

# load default config
# app.config.update(dict(
#     DATABASE=os.path.join(app.root_path, 'servpat.db'),
#     SECRET_KEY='development key',
#     USERNAME='admin',
#     PASSWORD='default'
# ))

app.config.from_object("settings.CorodiakConfig")

# TODO: logger
#       database connection
#       json

@app.route("/")
def root():
    return "root"

@app.route("/overview")
def overview():
    return "overview"

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

@app.route("/status")
def status():
    return "status"

@app.route("/image")
def image():
    return "image"

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