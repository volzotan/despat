import requests
from requests.auth import HTTPBasicAuth
from datetime import datetime
import json
import sys

URL = "http://localhost:5000/" + "upload"
#URL = "http://zoltep.de/" + "upload"
DATEFORMAT_INPUT = "%Y-%m-%d %H:%M:%S.%f"

files = {"file": open("test.jpg", "rb")}

#r = requests.post(URL, auth=HTTPBasicAuth('pythonTestClient', 'GRfX58yed'), files=files, data={"deviceId": "1020", "timestamp": datetime.now().strftime(DATEFORMAT_INPUT)})
#print(r)

# sys.exit()

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

