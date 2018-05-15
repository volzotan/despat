import json
import os
import datetime

DATEFORMAT_STORE = "%Y-%m-%d %H:%M:%S.%f"

inp_dir = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/old"
out_dir = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/"

json_files = []
data_list = []

for root, dirs, files in os.walk(inp_dir):
    for f in files:
        if not f.endswith(".json"):
            continue

        full_path = os.path.join(root, f)
        json_files.append(full_path)
        print(full_path)

for f in json_files:

    data = json.load(open(f, "r"))

    _, json_filename_only = os.path.split(f)
    timestamp = json_filename_only.split("_")[0]
    timestamp = int(timestamp) / 1000.0

    data["timestamp"] = datetime.datetime.fromtimestamp(timestamp).strftime(DATEFORMAT_STORE)

    json.dump(data, open(os.path.join(out_dir, json_filename_only), "w"), indent=4)

