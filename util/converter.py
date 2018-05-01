#import xml.etree.cElementTree as et
import lxml.etree as et
import json
import os
from datetime import datetime
from PIL import Image, ImageDraw
import numpy as np

DATEFORMAT_STORE            = "%Y-%m-%d %H:%M:%S.%f"
MIN_CONFIDENCE_VOC_EXPORT   = 0.3
CLASS_WHITELIST             = ["car", "truck", "bus", "bicycle", "motorcycle", "person"]

"""
data exchange dict:

timestamp
path
folder_filename
image_filename
image_size
boxes
classes
scores

"""

def class_indices_to_class_names(index, classes):
    result = []

    for item in classes:
        result.append(index[item]["name"])

    return result


def parse_json(filename_json):

    data = {}
    output = json.load(open(filename_json, "r"))
    data = output

    # data["path"]             = output["path"]
    # #ret["path"] = image_path.replace("/home/volzotan/DESPATDATASETS", "/Users/volzotan/Documents/DESPATDATASETS")
    #
    # file_folder_only, file_name_only = os.path.split(data["path"])
    # data["folder_filename"]  = file_folder_only
    # data["image_filename"]   = file_name_only
    # data["image_size"]       = None
    # data["boxes"]            = output["boxes"]
    # data["classes"]          = output["classes"]
    # data["scores"]           = output["scores"]
    #
    # try:
    #     data["image_size"] = output["imagesize"]
    # except KeyError as ke:
    #     image = Image.open(ret["path"])
    #     data["image_size"] = image.size
    #     print("imagesize derived from image file")

    return data


def json_to_voc(filename_json, filename_voc):

    data = parse_json(filename_json)

    convert_to_voc(
        data["folder_filename"],
        data["image_filename"],
        data["image_path"],
        data["image_size"],
        data["boxes"],
        data["classes"],
        data["scores"],
        filename_voc
    )


def convert_to_json(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename):

    data = {}

    data["timestamp"]       = datetime.now().strftime(DATEFORMAT_STORE)
    data["path"]            = image_path
    data["folder_filename"] = folder_filename
    data["image_filename"]  = image_filename
    data["image_size"]      = imagesize
    data["boxes"]           = bboxes
    data["classes"]         = classes
    data["scores"]          = scores

    json.dump(data, open(output_filename, "w"), indent=4)


def convert_to_voc(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename):

    root = et.Element("annotation")

    node_folder     = et.SubElement(root, "folder")
    node_filename   = et.SubElement(root, "filename")
    node_path       = et.SubElement(root, "path")
    node_source     = et.SubElement(root, "source")
    node_size       = et.SubElement(root, "size")
    node_segmented  = et.SubElement(root, "segmented")

    node_folder.text    = folder_filename
    node_filename.text  = image_filename
    node_path.text      = image_path
    et.SubElement(node_source, "database").text = "Unknown"

    et.SubElement(node_size, "width").text = str(imagesize[0])
    et.SubElement(node_size, "height").text = str(imagesize[1])
    et.SubElement(node_size, "depth").text = "3"

    node_segmented.text = "0"

    for i in range(len(bboxes)):

        if scores[i] < MIN_CONFIDENCE_VOC_EXPORT:
            continue

        if not classes[i] in CLASS_WHITELIST:
            continue

        node_object             = et.SubElement(root, "object")
        node_object_name        = et.SubElement(node_object, "name")
        node_object_pose        = et.SubElement(node_object, "pose")
        node_object_truncated   = et.SubElement(node_object, "truncated")
        node_object_difficult   = et.SubElement(node_object, "difficult")
        node_object_bndbox      = et.SubElement(node_object, "bndbox")

        node_object_name.text = classes[i]
        node_object_pose.text = "Unspecified"
        node_object_truncated.text = "0"
        node_object_difficult.text = "0"

        et.SubElement(node_object_bndbox, "xmin").text = str(int(bboxes[i][0]))
        et.SubElement(node_object_bndbox, "ymin").text = str(int(bboxes[i][1]))
        et.SubElement(node_object_bndbox, "xmax").text = str(int(bboxes[i][2]))
        et.SubElement(node_object_bndbox, "ymax").text = str(int(bboxes[i][3]))

    tree = et.ElementTree(root)
    tree.write(output_filename, pretty_print=True)


def sanitize_coordinate_order(boxes_as_nparray):
    boxes = np.zeros_like(boxes_as_nparray)

    # TODO: do some magic with numpy views?

    boxes[:, 0] = boxes_as_nparray[:, 1]
    boxes[:, 1] = boxes_as_nparray[:, 0]
    boxes[:, 2] = boxes_as_nparray[:, 3]
    boxes[:, 3] = boxes_as_nparray[:, 2]

    return boxes.tolist()


if __name__ == "__main__":

    # import pickle
    # bboxes = pickle.load(open("detection_boxes.pickle", "rb"))
    # classes = pickle.load(open("detection_classes.pickle", "rb"))
    # scores = pickle.load(open("detection_scores.pickle", "rb"))
    # category_index = pickle.load(open("category_index.pickle", "rb"))

    # # print(scores)

    # convert("detection", "pedestriancrossing.jpg", "/Users/volzotan/GIT/despat/detection/pedestriancrossing.jpg", [4000, 3000], bboxes, class_indices_to_class_names(category_index, classes), scores, "pedestriancrossing.xml")

    for root, dirs, files in os.walk("/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation"):
        for f in files:
            if not f.endswith(".json"):
                continue

            image = os.path.join(root, f)
            print(image)
            json_to_voc(image, str(image[:-5] + ".xml"))
  

    # jsonToVoc("output/1523267077487_0.json", "output/1523267077487_0.xml")

