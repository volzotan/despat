import os
import json
import sys
sys.path.append('..')
from util.drawhelper import Drawhelper

"""
bb_visualizer reads a json file and draws all BB from selected classes on this image

"""

def filter_by_class(data, class_name, confidence_threshold=0.5):

    boxes = []

    for i in range(0, len(data["classes"])):
        if data["classes"][i] == class_name:
            if data["scores"][i] >= confidence_threshold:

                coordinateset = list(data["boxes"][i])
                for pos in range(len(coordinateset)):
                    coordinateset[pos] *= SCALING_FACTOR

                boxes.append(coordinateset)

    return boxes


if __name__ == "__main__":

    ANNOTATION_DIR  = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/offline"
    IMAGE_DIR       = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_scaled"
    OUTPUT_DIR      = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_viz"

    SCALING_FACTOR  = 1920.0 / 5952.0

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

            if f.endswith("_1.json"):
                continue

            full_filename = os.path.join(root, f)
            filelist.append((full_filename, f))
            
    filelist = sorted(filelist, key=lambda filename: os.path.splitext(os.path.basename(filename[1]))[0])

    for i in range(len(filelist)):
        data = json.load(open(filelist[i][0], "r"))
        # print(data)

        image_name = os.path.splitext(filelist[i][1])[0]
        image_name = image_name[:-2] # remove "_0"
 
        image_input_name = os.path.join(IMAGE_DIR, image_name + ".jpg")

        # {0:0Nd}{1} where N is used to pad to the length of the highest number of images (eg. 001 for 256 images)
        pattern = str("{0:0") + str(len(str(len(filelist)))) + str("d}{1}")
        filename = pattern.format(i+1, ".jpg")
        image_output_name = os.path.join(OUTPUT_DIR, filename)

        if not os.path.exists(image_input_name):
            print("missing:  {}".format(filelist[i][1]))
            continue

        drawhelper = Drawhelper(image_input_name, image_output_name)

        drawhelper.add_boxes(filter_by_class(data, "person",    confidence_threshold=0.5), "#fde725", strokewidth=2)
        drawhelper.add_boxes(filter_by_class(data, "bicycle",   confidence_threshold=0.5), "#1f968b", strokewidth=2)
        drawhelper.add_boxes(filter_by_class(data, "car",       confidence_threshold=0.5), "#450457", strokewidth=2)

        drawhelper.draw()

        print("done:     {} | {}".format(filelist[i][1], image_output_name))