import requests
from datetime import datetime
import json

URL = "http://localhost:5000/" + "status"
DATEFORMAT_INPUT = "%Y-%m-%d %H:%M:%S.%f"

payload = {}
payload["deviceId"] = "123"
payload["deviceName"] = "pythonTestClient"
payload["timestamp"] = datetime.now().strftime(DATEFORMAT_INPUT) #[:-3]
payload["numberImages"] = 100
payload["freeSpaceInternal"] = 1000.0
payload["freeSpaceExternal"] = 1000.0
payload["batteryInternal"] = 50.0
payload["batteryExternal"] = -1
payload["stateCharging"] = 1

r = requests.post(URL, json=payload)
print(r)