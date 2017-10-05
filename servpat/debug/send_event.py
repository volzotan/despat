import requests
from requests.auth import HTTPBasicAuth
from datetime import datetime
import json

URL = "http://localhost:5000/" + "event"
URL = "http://zoltep.de/" + "event"
DATEFORMAT_INPUT = "%Y-%m-%d %H:%M:%S.%f"

payload = {}
payload["deviceId"] = "123"
# payload["deviceName"] = "pythonTestClient"
payload["timestamp"] = datetime.now().strftime(DATEFORMAT_INPUT) #[:-3]
payload["eventtype"] = 0x0
payload["payload"] = ""

r = requests.post(URL, json=payload)
print(r)