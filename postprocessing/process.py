import cv2
import numpy as np
import csv
import os
from datetime import *

import sys
sys.path.append('..')
from util import converter

CONFIDENCE_THRESHOLD = 0.1
CLASS_WHITELIST = ["car", "truck", "bus", "bicycle", "motorcycle", "person"]

def calculate_homography(points_src, points_dst):

    pts_src = np.array(points_src, dtype=np.float)
    pts_dst = np.array(points_dst, dtype=np.float)

    h, status = cv2.findHomography(pts_src, pts_dst)

    return h, status


def classname_to_id(name):
    classnames =  [
        "person", 
        "bicycle", 
        "car", 
        "motorcycle", 
        "airplane", 
        "bus", 
        "train", 
        "truck", 
        "boat", 
        "traffic light", 
        "fire hydrant", 
        "stop sign", 
        "parking meter", 
        "bench", 
        "bird", 
        "cat", 
        "dog", 
        "horse", 
        "sheep", 
        "cow", 
        "elephant", 
        "bear", 
        "zebra", 
        "giraffe", 
        "backpack", 
        "umbrella", 
        "handbag", 
        "tie", 
        "suitcase", 
        "frisbee", 
        "skis", 
        "snowboard", 
        "sports ball", 
        "kite", 
        "baseball bat", 
        "baseball glove", 
        "skateboard", 
        "surfboard", 
        "tennis racket", 
        "bottle", 
        "wine glass", 
        "cup", 
        "fork", 
        "knife", 
        "spoon", 
        "bowl", 
        "banana", 
        "apple", 
        "sandwich", 
        "orange", 
        "broccoli", 
        "carrot", 
        "hot dog", 
        "pizza", 
        "donut", 
        "cake", 
        "chair", 
        "couch", 
        "potted plant", 
        "bed", 
        "dining table", 
        "toilet", 
        "tv", 
        "laptop", 
        "mouse", 
        "remote", 
        "keyboard", 
        "cell phone", 
        "microwave", 
        "oven", 
        "toaster", 
        "sink", 
        "refrigerator", 
        "book", 
        "clock", 
        "vase", 
        "scissors", 
        "teddy bear", 
        "hair drier", 
        "toothbrush"
    ]

    return classnames.index(name)

# input

# OUTPUT_FILENAME = "boxes_darmstadt.txt"
# DATA = [
#     {
#         "name": "darmstadt",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation/ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03_800px",
#         "src": [
#             [200, 2930],
#             [2890, 2350],
#             [3373, 1678],
#             [2895, 1596],
#             [2532, 1589],
#             [2397, 1446]
#         ],
#         "dst": [
#             [49.876611, 8.650386],
#             [49.876455, 8.650274],
#             [49.876099, 8.650315],
#             [49.876088, 8.650444],
#             [49.876149, 8.650517],
#             [49.875917, 8.650722]
#         ]
#     }
# ]

OUTPUT_FILENAME = "boxes_campusoffice.txt"
DATA = [
    {
        "name": "campusoffice",
        "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/16-07-14_campusoffice_annotation/ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03_JITTER_1100px",
        "src": [
            #[3090, 3722],   # 1
            [4010, 2623],   # 2
            [3000, 2780],   # 3
            #[2753, 2360],  # 4
            [1070, 2782],   # 5
            [4824, 3925]    # 6
        ],
        "dst": [
            #[50.974806, 11.328820], # 1
            [50.974629, 11.328794], # 2
            [50.974719, 11.328906], # 3
            #[...],                 # 4
            [50.974796, 11.329128], # [50.974791, 11.329041], # 5
            [50.974816, 11.328647]  # 6
        ]
    }
]
startdate = datetime(2016, 7, 14, 11)

# OUTPUT_FILENAME = "boxes_bahnhof3.txt"
# DATA = [
#     {
#         "name": "default camera",
#         # "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03_JITTER_800px",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_fusion",
#         "src": [
#             [1124, 1416],
#             [1773, 2470],
#             [3785, 1267],
#             [3416, 928],
#             [2856, 1303],
#             [2452, 916]
#         ],
#         "dst": [
#             [50.971296, 11.037630],
#             [50.971173, 11.037914],
#             [50.971456, 11.037915],
#             [50.971705, 11.037711],
#             [50.971402, 11.037796],
#             [50.971636, 11.037486]
#         ]
#     }
# ]

# DATA = [
#     {
#         "name": "low-fi",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/ssd_mobilenet_v1_coco_2018_01_28_600px",
#         "src": [
#             [1124, 1416],
#             [1773, 2470],
#             [3785, 1267],
#             [3416, 928],
#             [2856, 1303],
#             [2452, 916]
#         ],
#         "dst": [
#             [50.971296, 11.037630],
#             [50.971173, 11.037914],
#             [50.971456, 11.037915],
#             [50.971705, 11.037711],
#             [50.971402, 11.037796],
#             [50.971636, 11.037486]
#         ]
#     },
#     {
#         "name": "mid-fi",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/faster_rcnn_inception_v2_coco_2018_01_28_1600px",
#         "src": [
#             [1124, 1416],
#             [1773, 2470],
#             [3785, 1267],
#             [3416, 928],
#             [2856, 1303],
#             [2452, 916]
#         ],
#         "dst": [
#             [50.971296, 11.037630],
#             [50.971173, 11.037914],
#             [50.971456, 11.037915],
#             [50.971705, 11.037711],
#             [50.971402, 11.037796],
#             [50.971636, 11.037486]
#         ]
#     },
# ]

# DATA = [
#     {
#         "name": "nexus1_hauptgebaeude1",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-07-13_summaery/hauptgebaeude1_annotation/faster_rcnn_nas_coco_2018_01_28",
#         "src": [
#             [989, 1044],
#             [1513, 919],
#             [1631, 1239],
#             [628, 2375]
#         ],
#         "dst": [
#             [50.974466, 11.329602],
#             [50.974402, 11.329671],
#             [50.974418, 11.329485],
#             [50.974488, 11.329267]
#         ]
#     },
#     {
#         "name": "nexus2_hauptgebaeude2",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-07-13_summaery/hauptgebaeude2_annotation/faster_rcnn_nas_coco_2018_01_28",
#         "src": [
#             [789, 1069],
#             [2583, 1191],
#             [2905, 1357],
#             [1750, 2236]
#         ],
#         "dst": [
#             [50.974273, 11.329306],
#             [50.974184, 11.329073],
#             [50.974214, 11.329001],
#             [50.974385, 11.329020]
#         ]
#     },
#     {
#         "name": "zte_infar",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-07-13_summaery/infar_annotation/faster_rcnn_nas_coco_2018_01_28",
#         "src": [
#             [2030, 1177],
#             [2749, 1415],
#             [4028, 2445],
#             [1456, 3243]
#         ],
#         "dst": [
#             [50.974456, 11.329067],
#             [50.974498, 11.329196],
#             [50.974530, 11.329404],
#             [50.974418, 11.329482]
#         ]
#     },
#     {
#         "name": "nexus_werkstatt1",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-07-13_summaery/werkstatt1_annotation/faster_rcnn_nas_coco_2018_01_28",
#         "src": [
#             [640, 1590],
#             [1270, 1480],
#             [1850, 211],
#             [460, 680]
#         ],
#         "dst": [
#             [50.974252, 11.328980],
#             [50.974264, 11.329024],
#             [50.974428, 11.329017],
#             [50.974295, 11.328926]
#         ]
#     },
#     {
#         "name": "nexus_werkstatt2",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-07-13_summaery/werkstatt2_annotation/faster_rcnn_nas_coco_2018_01_28",
#         "src": [
#             [64, 1222],
#             [907, 2070],
#             [1620, 550],
#             [2960, 523]
#         ],
#         "dst": [
#             [50.974291, 11.328905],
#             [50.974269, 11.329029],
#             [50.974448, 11.329066],
#             [50.974541, 11.329309]
#         ]
#     },
#     {
#         "name": "nexus_dblwerkstatt2",
#         "input_dir" : "/Users/volzotan/Documents/DESPATDATASETS/18-07-13_summaery/dbl_annotation/faster_rcnn_nas_coco_2018_01_28",
#         "src": [
#             [1500, 2200],
#             [2700, 1370],
#             [1597, 197],
#             [130, 340]
#         ],
#         "dst": [
#             [50.973270, 11.329641],
#             [50.973305, 11.329692],
#             [50.973389, 11.329516],
#             [50.973338, 11.329390]
#         ]
#     }
# ]

with open(OUTPUT_FILENAME, "w") as outputfile:
    writer = csv.writer(outputfile, delimiter="|", quotechar="'", quoting=csv.QUOTE_MINIMAL)
    writer.writerow([
        "timestamp",
        "device",
        "class",
        "confidence",
        "lat",
        "lon",
        "minx",
        "miny",
        "maxx",
        "maxy",
        "action"
    ])

for dataset in DATA:

    json_files = []
    image_list = []

    for root, dirs, files in os.walk(dataset["input_dir"]):
        for f in files:
            if not f.endswith(".json"):
                continue

            full_path = os.path.join(root, f)
            json_files.append(full_path)
            print(full_path)

    for f in json_files:
        data = converter.parse_json(f)
        image_list.append(data)

    h, status = calculate_homography(dataset["src"], dataset["dst"])

    output = []

    for imagedata in image_list:

        # dirty fix for the missing timestamps in the campusoffice dataset
        startdate = startdate + timedelta(seconds=60)

        for i in range(0, len(imagedata["boxes"])):

            if imagedata["scores"] is not None and imagedata["scores"][i] < CONFIDENCE_THRESHOLD:
                continue

            if imagedata["classes"] is not None and imagedata["classes"][i] not in CLASS_WHITELIST:
                continue

            out = []

            # data:
            # timestamp device class confidence lat lon minx miny maxx maxy

            # dirty fix for the missing timestamps in the campusoffice dataset
            out.append(startdate)

            # out.append(imagedata["timestamp"])
            out.append(DATA.index(dataset))
            out.append(classname_to_id(imagedata["classes"][i]))
            out.append(imagedata["scores"][i]) # TODO: use here own confidence metric based on camera angle, etc

            # float32 should be enough (6 decimal places)
            # 6th decimal place in lat/lon has a precision of 0.11m

            # beware: only a single point (the anchor) of the bounding box should be transformed (or the bbox will be grossly warped)
            b = imagedata["boxes"][i]
            x = b[0]+(b[2]-b[0])/2.0
            y = b[3]

            values = np.array([[[x, y]]], dtype=np.float64)
            pointsOut = cv2.perspectiveTransform(values, h)
            out.append(pointsOut[0][0][0].item())
            out.append(pointsOut[0][0][1].item())

            out = out + imagedata["boxes"][i]

            if "actions" in imagedata.keys():
                out.append(imagedata["actions"][i])
            else:
                out.append(-1)

            output.append(out)

    # output

    with open(OUTPUT_FILENAME, "a") as outputfile:
        writer = csv.writer(outputfile, delimiter="|", quotechar="'", quoting=csv.QUOTE_MINIMAL)
        for line in output:
            writer.writerow(line)