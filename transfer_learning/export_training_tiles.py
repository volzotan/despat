import numpy as np
import sys
import os
import json
import random

import xml.etree.cElementTree as et
from PIL import Image, ImageDraw
import io

import tensorflow as tf
from object_detection.utils import dataset_util

sys.path.append('..')

from detector.tilemanager import TileManager

INPUT_DIRS          = [
    "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_bahnhof_ZTE",
    "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ",
    "/Users/volzotan/Documents/DESPATDATASETS/18-04-21_zitadelle_ZTE",
    "/Users/volzotan/Documents/DESPATDATASETS/18-05-28_bonn_ZTE"
]

OUTPUT_DIR          = "data"
TILESIZE            = 900
EXPORT_IMAGE_SIZE   = 900
TFRECORD_FILE       = "foo.record"

# ignore personS and bicycleS classes for now

CLASSES = {
    "person": 1,
    "bicycle": 2,
    "car": 3
}

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


def create_tf_example(filename, image, data):

    height = EXPORT_IMAGE_SIZE
    width = EXPORT_IMAGE_SIZE
    image_format = b'jpg' # b'jpeg'

    xmins = []
    xmaxs = []
    ymins = []
    ymaxs = []
    classes_text = []
    classes = []

    for i in range(0, len(data)):

        bbox = list(data[i][0].exterior.coords)
        classname = data[i][1]

        if classname not in CLASSES:
            classname_new = classname[:-1]
            if classname_new not in CLASSES:
                print("ignored classname: {}".format(classname))
                continue
            else:
                classname = classname_new

        # print(bbox)

        xmin = min(bbox[0][0], bbox[1][0], bbox[2][0], bbox[3][0], bbox[4][0])
        xmax = max(bbox[0][0], bbox[1][0], bbox[2][0], bbox[3][0], bbox[4][0])
        ymin = min(bbox[0][1], bbox[1][1], bbox[2][1], bbox[3][1], bbox[4][1])
        ymax = max(bbox[0][1], bbox[1][1], bbox[2][1], bbox[3][1], bbox[4][1])

        # print(xmin)
        # print(xmax)
        # print(ymin)
        # print(ymax)

        xmins.append(xmin / width)
        xmaxs.append(xmax / width)
        ymins.append(ymin / height)
        ymaxs.append(ymax / height)
        classes_text.append(classname.encode("utf-8"))
        classes.append(CLASSES[classname])

    # print("saved {} classes".format(len(classes)))

    tf_example = tf.train.Example(features=tf.train.Features(feature={
        'image/height': dataset_util.int64_feature(height),
        'image/width': dataset_util.int64_feature(width),
        'image/filename': dataset_util.bytes_feature(filename.encode("utf-8")),
        'image/source_id': dataset_util.bytes_feature(filename.encode("utf-8")),
        'image/encoded': dataset_util.bytes_feature(image),
        'image/format': dataset_util.bytes_feature(image_format),
        'image/object/bbox/xmin': dataset_util.float_list_feature(xmins),
        'image/object/bbox/xmax': dataset_util.float_list_feature(xmaxs),
        'image/object/bbox/ymin': dataset_util.float_list_feature(ymins),
        'image/object/bbox/ymax': dataset_util.float_list_feature(ymaxs),
        'image/object/class/text': dataset_util.bytes_list_feature(classes_text),
        'image/object/class/label': dataset_util.int64_list_feature(classes),
    }))
        
    return tf_example


def write_record(recordname, images):

    writer = tf.compat.v1.python_io.TFRecordWriter(recordname)

    num_tiles_written = 0

    for image in images:

        print(image[2])

        tm = TileManager(image[1], TILESIZE, EXPORT_IMAGE_SIZE, jitter=False)

        tm.pass_ground_truth(vocToBbox(image[2]))

        for tile_id in tm.get_all_tile_ids():
            tile_image_np = tm.get_tile_image(tile_id)
            result = tm.get_tile_ground_truth(tile_id)

            # print(result)

            tile_image = Image.fromarray(tile_image_np)
            filename = image[0][:-4]+"-{}.jpg".format(tile_id)

            # draw = ImageDraw.Draw(tile_image, "RGBA")
            # for res in result:
            #     draw.line(list(res[0].exterior.coords))
            #     # print(list(res[0].exterior.coords))

            # del draw
            # tile_image.save(os.path.join(OUTPUT_DIR, image[0][:-4]+"-{}.jpg".format(tile_id)))

            imgByteArr = io.BytesIO()
            tile_image.save(imgByteArr, format='JPEG')
            imgByteArr = imgByteArr.getvalue()

            # if len(result) > 0:

            #     print(list(zip(*result))[0])
            #     print(list(zip(*result))[1])
            #     exit()

            tf_example = create_tf_example(filename, imgByteArr, result) #[], [], []) # TODO
            writer.write(tf_example.SerializeToString())

            num_tiles_written += 1

            # print(filename)

    writer.close()

    return num_tiles_written


def main(_):

    examples = []

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
                print("image file missing: {}".format(image_file))
            else:
                examples.append((image_name, image_file, os.path.join(xml[0], xml[1])))

    # print(*images, sep="\n")

    print("found images: {}".format(len(examples)))

    # examples = examples[0:20]

    random.seed(42)
    random.shuffle(examples)
    num_images = len(examples)
    num_train = int(0.9 * num_images)

    train_images = examples[:num_train]
    val_images = examples[num_train:]

    print("train images: {}".format(len(train_images)))
    print("val images: {}".format(len(val_images)))

    train_tiles_written = write_record("train.record", train_images)
    # write_record("test.record")
    eval_tiles_written = write_record("val.record", val_images)

    print("train images: {}".format(train_tiles_written))
    print("val images: {}".format(eval_tiles_written))


if __name__ == '__main__':
    tf.compat.v1.app.run()

