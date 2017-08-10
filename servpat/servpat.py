from flask import Flask
app = Flask("servpat")

# TODO: logger
#       database connection
#       json

@app.route("/")
def root():
    return "root"

@app.route("/command")
def command():
    return "command"

@app.route("/status")
def status():
    return "status"

@app.route("/image")
def image():
    return "image"