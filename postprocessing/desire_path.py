import os
from PIL import Image
import matplotlib.pyplot as plt
import numpy as np

import sys
sys.path.append('..')
from util import converter

CONFIDENCE_THRESHOLD = 0.3
CLASS_NAME = "person"

def run(data_list):

    imagesize = data_list[0]["image_size"]

    boxes = []
    for data in data_list:

        for i in range(0, len(data["boxes"])):

            if data["scores"] is not None and data["scores"][i] < CONFIDENCE_THRESHOLD:
                continue

            if data["classes"] is not None and data["classes"][i] != CLASS_NAME:
                continue

            boxes.append(data["boxes"][i])

    print(len(boxes))

    plt.clf()
    plt.rcParams["figure.figsize"] = (8, 6)

    plt.axis([0, imagesize[0], 0, imagesize[1]])

    axes = plt.gca()

    axes.set_xticklabels([])
    axes.set_xticks([])
    axes.set_yticklabels([])
    axes.set_yticks([])

    plt.gca().invert_yaxis()

    x = [x[0]+(x[2]-x[0])/2 for x in boxes]
    y = [x[1]+(x[3]-x[1])/2 for x in boxes]

    BACKGROUND_IMAGE_PATH = data["path"]
    BACKGROUND_IMAGE_PATH = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_stack/output.jpg"

    # .convert("L")
    plt.imshow(np.asarray(Image.open(BACKGROUND_IMAGE_PATH)), cmap="gray")

    # plt.scatter(x, y, alpha=0.1, marker="+", color=(1.0, 0.0, 0.0))

    # heatmap, xedges, yedges = np.histogram2d(x, y, bins=100)
    # extent = [xedges[0], xedges[-1], yedges[0], yedges[-1]]
    # masked_data = np.ma.masked_where(heatmap.T > 1, heatmap.T)
    # plt.imshow(masked_data, extent=extent, origin='lower', interpolation='none')

    x += [0, imagesize[0], 0, imagesize[0]]
    y += [0, 0, imagesize[1], imagesize[1]]
    gridsize = 70
    plt.hexbin(x, y, gridsize=gridsize, mincnt=2, bins=10, alpha=0.8, cmap="magma")

    plt.show()
    print("done")


if __name__ == "__main__":
    inp_dir = "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE_annotation"

    json_files = []
    data_list = []

    for root, dirs, files in os.walk(inp_dir):
        for f in files:
            if not f.endswith(".json"):
                continue

            full_path = os.path.join(root, f)
            json_files.append(full_path)
            print(full_path)


    for f in json_files:
        data = converter.parse_json(f)
        data_list.append(data)

    run(data_list)


