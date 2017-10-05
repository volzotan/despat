import requests
from datetime import datetime
import json

URL = "http://localhost:5000/" + "image"
DATEFORMAT_INPUT = "%Y-%m-%d %H:%M:%S.%f"

files = {"file": open("test.jpg", "rb")}

#r = requests.post(URL, files=files)
#print(r)

r = requests.Request('POST', URL, files=files, data={"deviceId": "1020", "timestamp": datetime.now().strftime(DATEFORMAT_INPUT)})
req = r.prepare()

print('{}\n{}\n{}\n\n{}'.format(
    '-----------START-----------',
    req.method + ' ' + req.url,
    '\n'.join('{}: {}'.format(k, v) for k, v in req.headers.items()),
    req.body,
))

s = requests.Session()
s.send(req)