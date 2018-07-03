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
from matplotlib.ticker import MultipleLocator

def calculate_size(box):
    x = box[2] - box[0]
    y = box[3] - box[1]

    return x, y, x*y

if __name__ == "__main__":

    INPUT_DIRS = [
        "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE_annotation/",
        "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation/",
        "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation",
        "/Users/volzotan/Documents/DESPATDATASETS/18-05-28_bonn_ZTE_annotation"
    ]

    LIMIT = 20

    images = []
    counter = 0
    for input_dir in INPUT_DIRS:
        counter = 0
        for root, dirs, files in os.walk(input_dir):
            for f in files:
                if (not f.endswith(".xml")):
                   continue

                filename = os.path.join(root, f)

                if LIMIT is not None and LIMIT != 0 and counter >= LIMIT:
                    print("skipped due to limit: {}".format(filename))
                    continue

                images.append(filename)
                counter += 1

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

    print("{} bounding boxes. skipped {}/{} due class filtering".format(len(area), skipcounter_class, len(area)+skipcounter_class))
    print("x | min: {} | max: {} | mean: {}".format(np.min(xs), np.max(xs), np.mean(xs)))
    print("y | min: {} | max: {} | mean: {}".format(np.min(ys), np.max(ys), np.mean(ys)))
    print("a | min: {} | max: {} | mean: {}".format(np.min(area), np.max(area), np.mean(area)))
    print("avg aspect ratio: 1:{}".format(np.mean(ys)/np.mean(xs)))

    # Build the plot
    plt.style.use('grayscale')
    fig, ax = plt.subplots()
    ax.scatter(xs, ys, s=1)
    ax.set_ylabel('height in pixel')
    # ax.set_xticks(x_pos)
    # ax.set_xticklabels(xlabels)
    ax.set_xlabel('width in pixel')

    ax.set_xlim(0, 500)
    ax.set_ylim(0, 500)
    ax.set_aspect('equal')

    alpha = 0.5
    linestyle = "dotted"
    linewidth = 0.5

    ax.xaxis.grid(True, alpha=alpha, linestyle=linestyle, linewidth=linewidth)
    ax.yaxis.grid(True, alpha=alpha, linestyle=linestyle, linewidth=linewidth)

    # Save the figure and show
    plt.tight_layout()
    plt.savefig('plot_pixel_size.png')
    plt.show()

    # plt.scatter(xs, ys)
    # plt.show()


