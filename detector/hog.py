import cv2
import numpy as np
import datetime
import sys
import os

sys.path.append('..')
from util import converter

INPUT_FOLDER = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE"
OUTPUT_FOLDER = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE_annotation/hog"

images = []

def resize(image, width=None, height=None, inter=cv2.INTER_AREA):
    # initialize the dimensions of the image to be resized and
    # grab the image size
    dim = None
    (h, w) = image.shape[:2]

    # if both the width and height are None, then return the
    # original image
    if width is None and height is None:
        return image

    # check to see if the width is None
    if width is None:
        # calculate the ratio of the height and construct the
        # dimensions
        r = height / float(h)
        dim = (int(w * r), height)

    # otherwise, the height is None
    else:
        # calculate the ratio of the width and construct the
        # dimensions
        r = width / float(w)
        dim = (width, int(h * r))

    # resize the image
    resized = cv2.resize(image, dim, interpolation=inter)

    # return the resized image
    return resized


def toMinMaxBoundingBox(mat):

    # input  [[minx, miny, widthx, widthy], ...]
    # output [[minx, miny, maxx, maxy]]

    bboxes = []

    for row in mat:
        bboxes.append([int(row[0]), int(row[1]), int(row[0]+row[2]), int(row[1]+row[3])])

    return bboxes


def run(image):

    image_foldername = os.path.split(image)[0]
    image_filename = os.path.split(image)[1]
    image_timestamp = os.path.splitext(image_filename)[0]

    hog = cv2.HOGDescriptor()
    hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())

    img = cv2.imread(image)
    # img = imutils.resize(image, width=min(400, image.shape[1]))

    start = datetime.datetime.now()
    # rects, weights = hog.detectMultiScale(img, winStride=winStride, padding=padding, scale=args["scale"], useMeanshiftGrouping=meanShift)
    rects, weights = hog.detectMultiScale(img)
    print("[{}] detection took: {}".format(image_filename, (datetime.datetime.now() - start).total_seconds()))

    classes = ["person" for x in range(0, len(rects))]

    output_filename = os.path.join(OUTPUT_FOLDER, image_timestamp + ".json")
    imagesize = (np.size(img, 1), np.size(img, 0))
    converter.convert_to_json(image_foldername, image_filename, image, imagesize, toMinMaxBoundingBox(rects), classes, weights.tolist(), output_filename)

    # def convert_to_json(folder_filename, image_filename, image_path, imagesize, bboxes, classes, scores, output_filename):

    # for (x, y, w, h) in rects:
    #     cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

    # image = resize(image, width=min(1200, image.shape[1]))
    #
    # cv2.imshow("Detections", image)
    # cv2.waitKey(0)


if __name__ == "__main__":

    if not os.path.exists(OUTPUT_FOLDER):
        try:
            os.makedirs(OUTPUT_FOLDER)
            print("created output dir: {}".format(OUTPUT_FOLDER))
        except Exception as e:
            print("could not create output dir: {}".format(OUTPUT_FOLDER))
            print(e)
            print("exit")
            sys.exit(-1)

    for root, dirs, files in os.walk(INPUT_FOLDER):
       for f in files:
           if (not f.endswith(".jpg")):
               continue
           images.append(os.path.join(root, f))

    images = sorted(images, key=lambda filename: os.path.splitext(os.path.basename(filename))[0])

    for image in images:
        run(image)