import numpy as np
import sys
import os
import json

import xml.etree.cElementTree as et
from PIL import Image, ImageDraw

sys.path.append('..')

from detector.tilemanager import TileManager

INPUT_DIRS          = [
    "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE"
]

OUTPUT_DIR          = "data"
TILESIZE            = 600
EXPORT_IMAGE_SIZE   = 600

# ignore personS and bicycleS classes for now

CLASSES = {
    "person": 1,
    "bicycle": 2
}

images = []


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


for inputdir in INPUT_DIRS:

    annotation_dir = inputdir + "_annotation"
    annotation_xmls = [] # [ (path, filename), ...]

    for root, dirs, files in os.walk(annotation_dir):
       for f in files:
           if (not f.lower().endswith(".xml")):
               continue
           annotation_xmls.append((root, f))         

    for xml in annotation_xmls:
        image_name = xml[1][:-4] + ".jpg"
        image_file = os.path.join(inputdir, image_name)

        if not os.path.exists(image_file):
            print("image file missing: {}").format(image_file)
        else:
            images.append((image_name, image_file, os.path.join(xml[0], xml[1])))

# print(*images, sep="\n")


for image in images:

    print(image[2])

    tm = TileManager(image[1], TILESIZE, EXPORT_IMAGE_SIZE, jitter=False)

    tm.pass_ground_truth(vocToBbox(image[2]))

    for tile_id in tm.get_all_tile_ids():
        tile_image_np = tm.get_tile_image(tile_id)
        result = tm.get_tile_ground_truth(tile_id)

        print(result)

        tile_image = Image.fromarray(tile_image_np)

        draw = ImageDraw.Draw(tile_image, "RGBA")
        for res in result:
            draw.line(list(res[0].exterior.coords))
            print(list(res[0].exterior.coords))

        del draw
        tile_image.save(os.path.join(OUTPUT_DIR, image[0][:-4]+"-{}.jpg".format(tile_id)))

    exit()