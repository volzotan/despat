import cv2
import numpy as np
import csv
import os

import sys
sys.path.append('..')
from util import converter

CONFIDENCE_THRESHOLD = 0.1
CLASS_NAME = "person"

def calculate_homography(points_src, points_dst):

    pts_src = np.array(points_src, dtype=np.float)
    pts_dst = np.array(points_dst, dtype=np.float)

    h, status = cv2.findHomography(pts_src, pts_dst)

    return h, status

# input

inp_dir = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation"

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
    data = converter.parse_json(f)
    data_list.append(data)

# process

src = [
    [1124, 1416],
    [1773, 2470],
    [3785, 1267],
    [3416, 928],
    [2856, 1303],
    [2452, 916]
]

dst = [
    [50.971296, 11.037630],
    [50.971173, 11.037914],
    [50.971456, 11.037915],
    [50.971705, 11.037711],
    [50.971402, 11.037796],
    [50.971636, 11.037486]
]

h, status = calculate_homography(src, dst)

output = []

for data in data_list:
    for i in range(0, len(data["boxes"])):

        if data["scores"] is not None and data["scores"][i] < CONFIDENCE_THRESHOLD:
            continue

        if data["classes"] is not None and data["classes"][i] != CLASS_NAME:
            continue

        out = []

        # data:
        # timestamp device class confidence lat lon minx miny maxx maxy

        out.append(data["timestamp"])
        out.append(0) # TODO: device
        out.append(1) # TODO: class
        out.append(data["scores"][i]) # TODO: use here own confidence metric based on camera angle, etc

        # float32 should be enough (6 decimal places)
        # 6th decimal place in lat/lon has a precision of 0.11m

        # beware: only a single point (the anchor) of the bounding box should be transformed (or the bbox will be grossly warped)
        b = data["boxes"][i]
        x = b[0]+(b[2]-b[0])/2.0
        y = b[3]

        values = np.array([[[x, y]]], dtype=np.float64)
        pointsOut = cv2.perspectiveTransform(values, h)
        out.append(pointsOut[0][0][0].item())
        out.append(pointsOut[0][0][1].item())

        out = out + data["boxes"][i]

        output.append(out)

# output

with open("boxes.txt", "w") as outputfile:
    writer = csv.writer(outputfile, delimiter="|", quotechar="'", quoting=csv.QUOTE_MINIMAL)
    for line in output:
        writer.writerow(line)