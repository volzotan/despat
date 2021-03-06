import sys
import matplotlib.pyplot as plt
import numpy as np

sys.path.append('..')
from util.drawhelper import Drawhelper
from util.converter import vocToBbox, jsonToBbox

IOU_THRESHOLD = 0.5


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


def evaluate(gt, dt, name_of_class, min_confidence=0.5):

    gt_boxes = _split_by_class_and_filter(gt["box"], gt["class"], None, name_of_class, 0)
    dt_boxes = _split_by_class_and_filter(dt["box"], dt["class"], dt["score"], name_of_class, min_confidence)

    true_positives = []
    false_positives = []
    false_negatives = []

    detected_gt = []

    for dbox in dt_boxes:
        found = False

        for gbox in gt_boxes:

            if gbox in detected_gt:
                continue

            if _iou(dbox, gbox) >= IOU_THRESHOLD:
                true_positives.append(dbox)
                detected_gt.append(gbox)
                found = True
                break

        if not found:
            false_positives.append(dbox)

    for gbox in gt_boxes:
        if gbox not in detected_gt:
            false_negatives.append(gbox)

    return (true_positives, false_positives, false_negatives)

    # print("true_positives: {}".format(len(true_positives)))
    # print("false_positives: {}".format(len(false_positives)))
    # print("false_negatives: {}".format(len(false_negatives)))
    #
    # print("recall: {}".format(len(true_positives) / (len(true_positives) + len(false_positives))))
    # print("precision: {}".format(len(true_positives) / (len(true_positives) + len(false_negatives))))
    #
    # visualize(true_positives, false_positives, false_negatives)


def visualize(tp, fp, fn):
    drawhelper = Drawhelper("pedestriancrossing.jpg", "pedestriancrossing_evaluation.jpg")
    drawhelper.add_boxes(tp, color=(0, 255, 0), strokewidth=2)
    drawhelper.add_boxes(fp, color=(255, 0, 0), strokewidth=2)
    drawhelper.add_boxes(fn, color=(0, 0, 0), strokewidth=2)
    drawhelper.draw()


def precision_recall_curve(gt, dt, name_of_class):
    p = []
    r = []

    for threshold in [x / 10.0 for x in range(0, 11)]:
        tp, fp, fn = evaluate(gt, dt, name_of_class, min_confidence=threshold)

        precision = 1

        if len(tp) > 0 or len(fp) > 0:
            precision = len(tp) / (len(tp) + len(fp))

        recall = len(tp) / (len(tp) + len(fn))
        r.append(recall)
        p.append(precision)

    return p, r


def missrate_fppi_curve(gt, dt, name_of_class):
    pass

# def map_for_single_image(gt_xml, dt_json, classes):
#     gt = vocToBbox(gt_xml)
#     dt = jsonToBbox(dt_json)

#     plt.xlabel('recall')
#     plt.ylabel('precision')

#     axes = plt.gca()
#     axes.set_xlim([0, 1.05])
#     axes.set_ylim([0, 1.05])

#     handles = []

#     average_precisions = []

#     for c in classes:
#         p, r = precision_recall_curve(gt, dt, c)

#         # print(p)
#         # print(r)

#         average_precisions.append(p)

#         print("{} AP: {}".format(c, np.average(p)))

#         handle = plt.plot(r, p, label=c)
#         handles.append(handle)


if __name__ == "__main__":
    # print(vocToBbox("output/1523266900504_0.xml"))

    gt = vocToBbox("pedestriancrossing_gt.xml")
    dt = jsonToBbox("pedestriancrossing.json")

    classes = ["person"]

    plt.xlabel('recall')
    plt.ylabel('precision')

    axes = plt.gca()
    axes.set_xlim([0, 1.05])
    axes.set_ylim([0, 1.05])

    handles = []

    average_precisions = []

    for c in classes:
        p, r = precision_recall_curve(gt, dt, c)

        print(p)
        print(r)

        average_precisions.append(p)

        print("{} AP: {}".format(c, np.average(p)))

        handle = plt.plot(r, p, label=c)
        handles.append(handle)

    print(average_precisions)

    plt.legend(classes)
    plt.show()

