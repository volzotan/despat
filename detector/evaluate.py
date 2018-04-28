import xml.etree.ElementTree as et
import json
from drawhelper import Drawhelper

def vocToBbox(filename):
    res = {}
    res["box"] = []
    res["class"] = []
    res["score"] = None

    e = et.parse(filename).getroot()
    for box in e.findall("object"):
        bndbox = box.find("bndbox")

        res["box"].append([
            int(bndbox.find("xmin").text),
            int(bndbox.find("ymin").text),
            int(bndbox.find("xmax").text),
            int(bndbox.find("ymax").text)
        ])

        res["class"].append(box.find("name").text)

    return res


def jsonToBbox(filename):
    inp = json.load(open(filename, "r"))

    res = {}

    non_inv_boxes = []
    for box in inp["detection_boxes"]:
        non_inv_boxes.append([box[1], box[0], box[3], box[2]])

    res["box"] = non_inv_boxes
    res["class"] = inp["detection_classes"]
    res["score"] = inp["detection_scores"]

    return res


def _split_by_class_and_filter(boxes, classes, score, classname, threshold):
    res = []

    for i in range(0, len(boxes)):
        if classes[i] != classname:
            continue

        if score is not None and score[i] < threshold:
            continue

        res.append(boxes[i])

    return res


def _intersection(a, b):
    xmin = max(a[0], b[0])
    ymin = max(a[1], b[1])
    xmax = min(a[2], b[2])
    ymax = min(a[3], b[3])

    if xmax < xmin or ymax < ymin:
        return 0

    return (xmax-xmin) * (ymax-ymin) 


def _iou(box1, box2):

    intersection = _intersection(box1, box2)
    union = (box1[2]-box1[0]) * (box1[3]-box1[1]) + (box2[2]-box2[0]) * (box2[3]-box2[1]) - intersection

    return float(intersection) / float(union)


def evaluate(gt, dt, filter_classes=[]):

    if len(filter_classes) > 0: 
        pass

    min_confidence = 0.5
    iou_threshold = 0.5

    gt_boxes = _split_by_class_and_filter(gt["box"], gt["class"], None, "person", 0)
    dt_boxes = _split_by_class_and_filter(dt["box"], dt["class"], dt["score"], "person", min_confidence)

    print(len(gt_boxes))
    print(len(dt_boxes))

    true_positives = []
    false_positives = []
    false_negatives = []

    detected_gt = []

    for dbox in dt_boxes:
        found = False

        for gbox in gt_boxes:

            if gbox in detected_gt:
                continue

            iou = _iou(dbox, gbox)
            if iou >= iou_threshold:
                true_positives.append(dbox)
                detected_gt.append(gbox)
                found = True
                break

        if not found:
            false_positives.append(dbox)

    for gbox in gt_boxes:
        if gbox not in detected_gt:
            false_negatives.append(gbox)

    print("true_positives: {}".format(len(true_positives)))
    print("false_positives: {}".format(len(false_positives)))
    print("false_negatives: {}".format(len(false_negatives)))

    visualize(true_positives, false_positives, false_negatives)


def visualize(tp, fp, fn):
    drawhelper = Drawhelper("pedestriancrossing.jpg", "pedestriancrossing_evaluation.jpg")
    drawhelper.add_boxes(tp, color=(0, 255, 0), strokewidth=2)
    drawhelper.add_boxes(fp, color=(255, 0, 0), strokewidth=2)
    drawhelper.add_boxes(fn, color=(0, 0, 0), strokewidth=2)
    drawhelper.draw()


if __name__ == "__main__":
    # print(vocToBbox("output/1523266900504_0.xml"))

    evaluate(vocToBbox("pedestriancrossing_gt.xml"), jsonToBbox("output/pedestriancrossing.json"))