# Calculate the pixel dimensions of bounding boxes
# and print details about image resolution, etc.
#
#
#
#
#
#

import sys
sys.path.append('..')
from util.converter import vocToBbox, jsonToBbox
import os
import numpy as np
import matplotlib.pyplot as plt

def calculate_size(box):
    x = box[2] - box[0]
    y = box[3] - box[1]

    return x, y, x*y

if __name__ == "__main__":

    INPUT_DIRS = [
        "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE_annotation/",
        "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/",
        "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation"
    ]

    images = []
    for input_dir in INPUT_DIRS:
        for root, dirs, files in os.walk(input_dir):
            for f in files:
                if (not f.endswith(".xml")):
                   continue
                images.append(os.path.join(root, f))

    gts = []
    for image in images:
        gts.append(vocToBbox(image))

    classes = ["person"]

    xs = []
    ys = []
    area = []

    skipcounter_class = 0

    for gt in gts:
        for i in range(0, len(gt["box"])):
            if gt["class"][i] not in classes:
                skipcounter_class += 1
                continue

            x, y, a = calculate_size(gt["box"][i])

            xs.append(x)
            ys.append(y)
            area.append(a)

    print("{} bounding boxes. skipped {}/{} due class filtering".format(len(area), skipcounter_class, len(gt["box"])))
    print("x | min: {} | max: {} | mean: {}".format(np.min(xs), np.max(xs), np.mean(xs)))
    print("y | min: {} | max: {} | mean: {}".format(np.min(ys), np.max(ys), np.mean(ys)))
    print("a | min: {} | max: {} | mean: {}".format(np.min(area), np.max(area), np.mean(area)))

    # Build the plot
    plt.style.use('grayscale')
    fig, ax = plt.subplots()
    ax.scatter(xs, ys, s=1)
    ax.set_ylabel('height in pixel')
    # ax.set_xticks(x_pos)
    # ax.set_xticklabels(xlabels)
    ax.set_xlabel('width in pixel')
    # ax.xaxis.grid(True)
    # ax.yaxis.grid(True)

    # Save the figure and show
    plt.tight_layout()
    plt.savefig('plot_pixel_size.png')
    plt.show()

    # plt.scatter(xs, ys)
    # plt.show()


