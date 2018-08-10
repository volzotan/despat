"""
QA merges detections (image coordinates and geocoordinates) with downscaled background subtraction masks
and computes the postprocessing confidence score.

Additional data: 
  *  camera location (is it possible to compute a camera angle?)
  *  camera resolution (?)

confidence score:

if too big --> penalty
if static object and no matching BB in prev captures --> penalty
if too many objects in close vicinity --> penalty

"""

import os
import json
import cv2
import numpy as np

THRESHOLD_OBJECT_IS_ACTIVE  = 0.2
THRESHOLD_IOU               = 0.75

ACTION_UNDEFINED    = 0
ACTION_IDLE         = 1
ACTION_MOVING       = 2

class Qa(object):

    images          = []
    annotations     = []

    bgmodel         = None
    fgmasks         = []
    newest_fgmask   = None

    def __init__(self):
        self.bgmodel = cv2.bgsegm.createBackgroundSubtractorMOG()


    def add_image(self, full_filename):
        self.images.append(full_filename)

        img = cv2.imread(full_filename)
        fgmask = self.bgmodel.apply(img)
        self.fgmasks.append(self.fgmasks)
        self.newest_fgmask = fgmask


    def add_json(self, full_filename):
        data = json.load(open(full_filename, "r"))
        self.annotations.append(data)


    def _estimate_action(self, box):
        minx = int(box[0])
        miny = int(box[1])
        maxx = int(box[2])
        maxy = int(box[3])

        thresh, mask = cv2.threshold(self.newest_fgmask, 127, 255, cv2.THRESH_BINARY)
        mask = np.clip(mask, 0, 1)
        crop = mask[miny:maxy, minx:maxx]
        
        foreground_ratio = np.sum(crop) / np.size(crop)

        if foreground_ratio >= THRESHOLD_OBJECT_IS_ACTIVE:
            return ACTION_MOVING
        else:
            return ACTION_IDLE


    def _intersection(self, a, b):
        xmin = max(a[0], b[0])
        ymin = max(a[1], b[1])
        xmax = min(a[2], b[2])
        ymax = min(a[3], b[3])

        if xmax < xmin or ymax < ymin:
            return 0

        return (xmax-xmin) * (ymax-ymin) 


    def _iou(self, b1, b2):

        box1 = b1
        box2 = b2

        intersection = self._intersection(box1, box2)
        union = (box1[2]-box1[0]) * (box1[3]-box1[1]) + (box2[2]-box2[0]) * (box2[3]-box2[1]) - intersection

        return float(intersection) / float(union)


    def run(self):
        actions = []

        # foreground detection
        boxes = self.annotations[-1]["boxes"]
        for i in range(0, len(boxes)):
            actions.append(self._estimate_action(boxes[i]))

        self.annotations[-1]["actions"] = actions

        # previous detection
        box_has_predecessor = [0] * len(boxes)
        if (len(self.annotations) > 1):
            boxes_prev = self.annotations[-2]["boxes"]
            boxes_matched = []

            for i in range(0, len(boxes)):
                box = boxes[i]
                for prevbox in [x for x in boxes_prev if x not in boxes_matched]:
                    if (self._iou(box, prevbox)) >= THRESHOLD_IOU:
                        boxes_matched.append(prevbox)
                        box_has_predecessor[i] = 1
                        break

        self.annotations[-1]["predecessor"] = box_has_predecessor     

        # penalties
        scores = self.annotations[-1]["scores"]
        if actions[i] == ACTION_IDLE and box_has_predecessor[i] == 0:  
            scores[i] -= 0.2
            if scores[i] < 0:
                scores[i] = 0


    def viz(self, output_filename):
        
        img = cv2.imread(self.images[-1])
        output = self.newest_fgmask

        grayscale_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        grayscale_img = cv2.cvtColor(grayscale_img, cv2.COLOR_GRAY2BGR)

        red_img = np.zeros(img.shape, img.dtype)
        red_img[:,:] = (0, 0, 255)
        red_mask = cv2.bitwise_and(red_img, red_img, mask = output)
        vis = cv2.addWeighted(grayscale_img, 1, red_mask, 0.3, 0)

        boxes = self.annotations[-1]["boxes"]
        actions = self.annotations[-1]["actions"]
        predecessor = self.annotations[-1]["predecessor"]
        for i in range(0, len(boxes)):
            box = boxes[i]

            cv2.rectangle(vis, (int(box[0]), int(box[1])), (int(box[2]), int(box[3])), (0, 0, 0))

            # if actions[i] == ACTION_UNDEFINED:
            #     cv2.rectangle(vis, (int(box[0]), int(box[1])), (int(box[2]), int(box[3])), (0, 0, 0))

            if actions[i] == ACTION_IDLE and predecessor[i] == 0:
                cv2.rectangle(vis, (int(box[0]), int(box[1])), (int(box[2]), int(box[3])), (0, 255, 0))

            # if actions[i] == ACTION_MOVING:
                # cv2.rectangle(vis, (int(box[0]), int(box[1])), (int(box[2]), int(box[3])), (0, 255, 0))

            # if predecessor[i] == 0:
            #     cv2.rectangle(vis, (int(box[0]), int(box[1])), (int(box[2]), int(box[3])), (0, 0, 0))
            # else:
            #     cv2.rectangle(vis, (int(box[0]), int(box[1])), (int(box[2]), int(box[3])), (255, 0, 0))

        cv2.imwrite(output_filename, vis)
        print("written output file to: {}".format(output_filename))


    def add_corresponding_points(self, points):
        pass


    def add_background_subtraction_mask(self):
        pass


if __name__ == "__main__":
    # check for CLI arguments
    
    IMAGE_DIR       = "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ"
    ANNOTATION_DIR  = "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation/ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03_1000px"
    OUTPUT_DIR      = "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_fusion"

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

            full_filename = os.path.join(root, f)
            filelist.append((full_filename, f))

    filelist = sorted(filelist, key=lambda filename: os.path.splitext(os.path.basename(filename[1]))[0])

    filelist = filelist[0:20]

    qa = Qa()

    for i in range(len(filelist)):

        data = json.load(open(filelist[i][0], "r"))
        qa.add_image(os.path.join(IMAGE_DIR, data["image_filename"]))

        qa.add_json(filelist[i][0])

        qa.run()

        qa.viz(os.path.join(OUTPUT_DIR, filelist[i][1]+".jpg"))

