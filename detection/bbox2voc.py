#import xml.etree.cElementTree as et
import lxml.etree as et

MIN_CONFIDENCE = 0.5

def class_indices_to_class_names(index, classes):
    result = []

    for item in classes:
        result.append(index[item]["name"])

    return result


def convert(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename):

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

        if scores[i] < MIN_CONFIDENCE:
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

    import pickle
    bboxes = pickle.load(open("detection_boxes.pickle", "rb"))
    classes = pickle.load(open("detection_classes.pickle", "rb"))
    scores = pickle.load(open("detection_scores.pickle", "rb"))
    category_index = pickle.load(open("category_index.pickle", "rb"))

    # print(scores)

    convert("detection", "pedestriancrossing.jpg", "/Users/volzotan/GIT/despat/detection/pedestriancrossing.jpg", [4000, 3000], bboxes, class_indices_to_class_names(category_index, classes), scores, "pedestriancrossing.xml")

