#import xml.etree.cElementTree as et
import lxml.etree as et
import json
import os
from datetime import datetime

DATEFORMAT_STORE            = "%Y-%m-%d %H:%M:%S.%f"
MIN_CONFIDENCE_VOC_EXPORT   = 0.5

def class_indices_to_class_names(index, classes):
    result = []

    for item in classes:
        result.append(index[item]["name"])

    return result


def jsonToVoc(filename_json, filename_voc):

    output = json.load(open(filename_json, "r"))

    file_folder_only, file_name_only = os.path.split(output["path"])

    folder_filename = file_folder_only
    image_filename  = file_name_only
    image_path      = output["path"]
    imagesize       = output["imagesize"]
    bboxes          = output["detection_boxes"]
    classes         = output["detection_classes"]
    scores          = output["detection_scores"]
    output_filename = filename_voc

    print(output)

    convertToVoc(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename)


def convertToJson(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename):

    output = {}

    output["filename"]  = image_filename
    output["path"]      = image_path
    output["imagesize"] = imagesize
    output["timestamp"] = datetime.now().strftime(DATEFORMAT_STORE)

    output["detection_boxes"]   = bboxes.tolist() # inv coord format!
    output["detection_classes"] = classes
    output["detection_scores"]  = scores.tolist()

    json.dump(output, open(output_filename, "w"), indent=4)


def convertToVoc(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename):

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

        et.SubElement(node_object_bndbox, "xmin").text = str(int(bboxes[i][1]))
        et.SubElement(node_object_bndbox, "ymin").text = str(int(bboxes[i][0]))
        et.SubElement(node_object_bndbox, "xmax").text = str(int(bboxes[i][3]))
        et.SubElement(node_object_bndbox, "ymax").text = str(int(bboxes[i][2]))

    tree = et.ElementTree(root)
    tree.write(output_filename, pretty_print=True)


if __name__ == "__main__":

    # import pickle
    # bboxes = pickle.load(open("detection_boxes.pickle", "rb"))
    # classes = pickle.load(open("detection_classes.pickle", "rb"))
    # scores = pickle.load(open("detection_scores.pickle", "rb"))
    # category_index = pickle.load(open("category_index.pickle", "rb"))

    # # print(scores)

    # convert("detection", "pedestriancrossing.jpg", "/Users/volzotan/GIT/despat/detection/pedestriancrossing.jpg", [4000, 3000], bboxes, class_indices_to_class_names(category_index, classes), scores, "pedestriancrossing.xml")

    for root, dirs, files in os.walk("output"):
        for f in files:
            if (not f.endswith(".json")):
                continue

            image = os.path.join(root, f)
            print(image)
            jsonToVoc(image, str(image[:-5] + ".xml"))
  

    # jsonToVoc("output/1523267077487_0.json", "output/1523267077487_0.xml")

