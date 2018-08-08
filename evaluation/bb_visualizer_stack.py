import os
import json
import sys
sys.path.append('..')
from util.drawhelper import Drawhelper

"""
bb_visualizer reads a json file and draws all BB from selected classes on this image

"""

THRESHOLD = 0.2
TRANSPARENCY = 10
alpha = str(hex(int(TRANSPARENCY*2.55)))[2:]

def filter_by_class(data, class_name, confidence_threshold=0.5):

    boxes = []

    for i in range(0, len(data["classes"])):
        if data["classes"][i] == class_name:
            if data["scores"][i] >= confidence_threshold:
                coordinateset = list(data["boxes"][i])
                boxes.append(coordinateset)

    return boxes


if __name__ == "__main__":

    ANNOTATION_DIR  = "/Users/volzotan/Documents/DESPATDATASETS/16-07-14_campusoffice_annotation/ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03_1000px"
    IMAGE           = "/Users/volzotan/Documents/DESPATDATASETS/16-07-14_campusoffice_stacked/stack_.jpg"
    OUTPUT_DIR      = "/Users/volzotan/Documents/DESPATDATASETS/16-07-14_campusoffice_viz"

    try:
        os.makedirs(OUTPUT_DIR)
        print("created OUTPUT_DIR: {}".format(OUTPUT_DIR))
    except Exception as e:
        pass

    filelist = []

    for root, dirs, files in os.walk(ANNOTATION_DIR):
        for f in files:
            if not f.endswith(".json"):
                continue

            # if f.endswith("_1.json"):
            #     continue

            full_filename = os.path.join(root, f)
            filelist.append((full_filename, f))
            
    filelist = sorted(filelist, key=lambda filename: os.path.splitext(os.path.basename(filename[1]))[0])

    image_output_name = os.path.join(OUTPUT_DIR, "stackviz_{}.jpg".format(THRESHOLD))

    drawhelper = Drawhelper(IMAGE, image_output_name)

    for i in range(len(filelist)):
        data = json.load(open(filelist[i][0], "r"))

        drawhelper.add_boxes(filter_by_class(data, "car",       confidence_threshold=THRESHOLD), "#450457"+alpha, strokewidth=2)
        drawhelper.add_boxes(filter_by_class(data, "bicycle",   confidence_threshold=THRESHOLD), "#1f968b"+alpha, strokewidth=2)
        drawhelper.add_boxes(filter_by_class(data, "person",    confidence_threshold=THRESHOLD), "#fde725"+alpha, strokewidth=2)

        print("done:     {} | {}".format(filelist[i][1], i))

    drawhelper.draw()

