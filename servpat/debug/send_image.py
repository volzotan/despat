import requests
from datetime import datetime
import json

URL = "http://localhost:5000/" + "image"

files = {"file": open("test.jpg", "rb")}

r = requests.post(URL, files=files)
print(r)