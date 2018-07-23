import xml.etree.cElementTree as et
import json
import sys
import os
from operator import attrgetter
import matplotlib.pyplot as plt
import numpy as np

sys.path.append('..')
from util.drawhelper import Drawhelper

IOU_THRESHOLD = 0.5


def vocToBbox(filename):
    res = []

    e = et.parse(filename).getroot()
    for box in e.findall("object"):
        bndbox = box.find("bndbox")

        bbox = {}

        bbox["box"] = [
            int(bndbox.find("xmin").text),
            int(bndbox.find("ymin").text),
            int(bndbox.find("xmax").text),
            int(bndbox.find("ymax").text)
        ]

        bbox["class"] = box.find("name").text

        res.append(bbox)

    return res


def jsonToBbox(filename):
    inp = json.load(open(filename, "r"))

    res = []

    for i in range(0, len(inp["boxes"])):
        score = None

        # in an older version of the hog script, scores are incorrectly saved as [[a], [b], [c]]
        if isinstance(inp["scores"][i], list):
            score = inp["scores"][i][0]
        else:
            score = inp["scores"][i]

        if score < 0.001:
            continue

        bbox = {}

        # box = inp["boxes"][i] # de-invert xmin,ymin,... order
        # bbox["box"] = [box[1], box[0], box[3], box[2]]

        bbox["box"] = inp["boxes"][i]

        bbox["class"] = inp["classes"][i]
        bbox["score"] = inp["scores"][i]

        res.append(bbox)

    return res


def _filter_by_class(boxes, classnames):
    res = []
    for box in boxes:
        if box["class"] in classnames:
            res.append(box)

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


def _iou(b1, b2):

    box1 = b1["box"]
    box2 = b2["box"]

    intersection = _intersection(box1, box2)
    union = (box1[2]-box1[0]) * (box1[3]-box1[1]) + (box2[2]-box2[0]) * (box2[3]-box2[1]) - intersection

    return float(intersection) / float(union)


def evaluate_all(gt, dt, name_of_class):

    gt_boxes = _filter_by_class(gt, name_of_class)
    dt_boxes = _filter_by_class(dt, name_of_class)

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

    # print("true_positives: {}".format(len(true_positives)))
    # print("false_positives: {}".format(len(false_positives)))
    # print("false_negatives: {}".format(len(false_negatives)))
    #
    # print("recall: {}".format(len(true_positives) / (len(true_positives) + len(false_positives))))
    # print("precision: {}".format(len(true_positives) / (len(true_positives) + len(false_negatives))))
    #
    # visualize(true_positives, false_positives, false_negatives)

    return true_positives, false_positives, false_negatives


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

    return true_positives, false_positives, false_negatives

    # print("true_positives: {}".format(len(true_positives)))
    # print("false_positives: {}".format(len(false_positives)))
    # print("false_negatives: {}".format(len(false_negatives)))
    #
    # print("recall: {}".format(len(true_positives) / (len(true_positives) + len(false_positives))))
    # print("precision: {}".format(len(true_positives) / (len(true_positives) + len(false_negatives))))
    #
    # visualize(true_positives, false_positives, false_negatives)


def visualize(tp, fp, fn):
    drawhelper = Drawhelper("1523266870453_0.jpg", "1523266870453_0_evaluation.jpg")
    drawhelper.add_boxes([x["box"] for x in tp], color=(0, 255, 0), strokewidth=2)
    drawhelper.add_boxes([x["box"] for x in fp], color=(255, 0, 0), strokewidth=2)
    drawhelper.add_boxes([x["box"] for x in fn], color=(0, 0, 0), strokewidth=2)
    drawhelper.draw()


def precision_recall_curve(gt, dt, name_of_class):
    p = []
    r = []

    evaluate_all(gt, dt, name_of_class)

    # for threshold in [x / 10.0 for x in range(0, 11)]:
    #     tp, fp, fn = evaluate(gt, dt, name_of_class, min_confidence=threshold)
    #
    #     precision = 1
    #
    #     if len(tp) > 0 or len(fp) > 0:
    #         precision = len(tp) / (len(tp) + len(fp))
    #
    #     recall = len(tp) / (len(tp) + len(fn))
    #     r.append(recall)
    #     p.append(precision)

    return p, r


def _area_under_curve(precision, recall):
    area = 0

    for i in range(0, len(precision)):
        if i == 0:
            area += precision[i] * recall[i]
        else:
            area += precision[i] * (recall[i]-recall[i-1])

    return area

if __name__ == "__main__":
    # print(vocToBbox("output/1523266900504_0.xml"))

    # (input_dir_gt, input_dir_data)

    try:
        plt.style.use("grayscale")
        plt.style.use("despat")
    except Exception as e:
        print("Setting matplotlib style failed")

    NETWORK = "/" + "hog"

    INPUT_DIRS = [
        ("/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE_annotation/", "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE_annotation" + NETWORK),
        ("/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/", "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation" + NETWORK),
        ("/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation", "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation" + NETWORK),
        ("/Users/volzotan/Documents/DESPATDATASETS/18-05-28_bonn_ZTE_annotation", "/Users/volzotan/Documents/DESPATDATASETS/18-05-28_bonn_ZTE_annotation" + NETWORK)
    ]

    LIMIT = 20

    filepairs = []
    for input_dir in INPUT_DIRS:
        counter = 0
        for root, dirs, files in os.walk(input_dir[0]):
            for f in files:
                if not f.endswith(".xml"):
                    continue

                if LIMIT is not None and LIMIT != 0 and counter >= LIMIT:
                    print("skipped due to limit: {}".format(filename))
                    continue

                full_filename_gt = os.path.join(root, f)
                filename = os.path.splitext(os.path.basename(full_filename_gt))[0] + ".json"
                full_filename_dt = os.path.join(input_dir[1], filename)

                if not os.path.isfile(full_filename_dt):
                    print("no detection data found for ground truth file: {}".format(full_filename_gt))
                    continue

                filepairs.append((full_filename_gt, full_filename_dt))
                counter += 1

    filepairs = sorted(filepairs, key=lambda filenames: os.path.splitext(os.path.basename(filenames[0]))[0])

    print("filepairs: {}".format(len(filepairs)))

    classes = ["person"]

    plt.xlabel('recall')
    plt.ylabel('precision')

    axes = plt.gca()
    axes.set_xlim([0, 1.05])
    axes.set_ylim([0, 1.05])

    plt.style.use('grayscale')
    handles = []

    combined_tp = []
    combined_fp = []
    combined_fn = []

    for c in classes:

        for file_gtdt in filepairs:

            gt = vocToBbox(file_gtdt[0])
            dt = jsonToBbox(file_gtdt[1])

            tp, fp, fn = evaluate_all(gt, dt, c)
            combined_tp.extend(tp)
            combined_fp.extend(fp)
            combined_fn.extend(fn)

        combined_positives = combined_tp + combined_fp
        combined_positives = sorted(combined_positives, key=lambda box: box["score"], reverse=True)

        # for item in combined_positives:
        #     print(item)

        count_tp = 0
        count_fp = 0
        count_fn = len(combined_fn)

        precision = []
        recall = []

        for box in combined_positives:
            if box in combined_tp:
                count_tp += 1
            elif box in combined_fp:
                count_fp += 1
            else:
                print("error")

            if count_tp > 0 or count_fp > 0:
                precision.append(count_tp / (count_tp + count_fp))
            else:
                precision.append(1)

            if count_tp > 0 or count_fn > 0:
                recall.append(count_tp / (count_tp + count_fn))
            else:
                recall.append(1)

        # precision is always the maximum of all following precisions
        for i in range(0, len(precision)):
            precision[i] = max(precision[i:])

        # for i in range(0, len(precision)):
        #     print("{0:5.3f} {1:5.3f}".format(precision[i], recall[i]))

        handle = plt.plot(recall, precision, label=c) #, linestyle='--', marker='o')
        handles.append(handle)

        print("mAP: {}".format(_area_under_curve(precision, recall)))

    plt.legend(classes)
    plt.tight_layout()
    plt.savefig('plot_mapPerson.png')
    plt.show()
