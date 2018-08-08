import numpy as np
import cv2
import os

SCALE = "_1000"
# BASE_DIR    = "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ"
BASE_DIR    = "/Users/volzotan/GIT/despat/postprocessing/background_subtraction"
INPUT_DIR   = os.path.join(BASE_DIR, "scaled"+SCALE)
EXTENSION   = ".jpg"
OUTPUT_DIR  = os.path.join(BASE_DIR, "output"+SCALE)

fgbg = cv2.bgsegm.createBackgroundSubtractorMOG()
images = []

kernel = np.ones((3, 3), np.uint8)

red_img = None

for root, dirs, files in os.walk(INPUT_DIR):
    for f in files:
        if f.lower().endswith(EXTENSION):
            images.append((root, f))

if not os.path.exists(OUTPUT_DIR):
    os.makedirs(OUTPUT_DIR)
    print("created output dir: {}".format(OUTPUT_DIR))

images = sorted(images, key=lambda item: item[1])

for img_file in images:
    img = cv2.imread(os.path.join(img_file[0], img_file[1]))

    fgmask = fgbg.apply(img)

    # morphological operators
    # erosion = cv2.erode(fgmask, kernel, iterations = 1)
    #opening = cv2.morphologyEx(fgmask, cv2.MORPH_OPEN, kernel)
    #output = opening

    output = fgmask

    grayscale_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    grayscale_img = cv2.cvtColor(grayscale_img, cv2.COLOR_GRAY2BGR)

    if red_img is None:
        red_img = np.zeros(img.shape, img.dtype)
        red_img[:,:] = (0, 0, 255)
    red_mask = cv2.bitwise_and(red_img, red_img, mask = output)
    vis = cv2.addWeighted(grayscale_img, 1, red_mask, 0.3, 0)

    cv2.imwrite(os.path.join(OUTPUT_DIR, img_file[1]), vis)
    print(img_file[1])