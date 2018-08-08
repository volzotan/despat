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


    def run(self):
        pass


    def viz(self, output_filename):
        
        img = cv2.imread(self.images[-1])
        output = self.newest_fgmask

        grayscale_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        grayscale_img = cv2.cvtColor(grayscale_img, cv2.COLOR_GRAY2BGR)

        red_img = np.zeros(img.shape, img.dtype)
        red_img[:,:] = (0, 0, 255)
        red_mask = cv2.bitwise_and(red_img, red_img, mask = output)
        vis = cv2.addWeighted(grayscale_img, 1, red_mask, 0.3, 0)

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

    qa = Qa()

    for i in range(len(filelist)):

        data = json.load(open(filelist[i][0], "r"))
        qa.add_image(os.path.join(IMAGE_DIR, data["image_filename"]))

        qa.add_json(filelist[i][0])

        qa.viz(os.path.join(OUTPUT_DIR, filelist[i][1]+".jpg"))
