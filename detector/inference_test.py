import numpy as np
import os
import six.moves.urllib as urllib
import sys
import tarfile
import tensorflow as tf
import zipfile
from collections import defaultdict
from io import StringIO
from PIL import Image
# import opencv2 as cv2

from tilemanager import TileManager
import argparse

sys.path.append('..')
from util import converter

parser = argparse.ArgumentParser()

parser.add_argument("--images", default=".")
parser.add_argument("--output", default=".")

parser.add_argument("--model_path", default="models")
parser.add_argument("--model_name", default="default_model")
parser.add_argument("--tilesize", nargs="+", type=int)
parser.add_argument("--tensorflow-object-detection-path", default="/Users/volzotan/Downloads/tensorflow/models/research")
parser.add_argument("--export", default="json", choices=["xml", "json"])
parser.add_argument("--output_directory_appendix", default="")
parser.add_argument("--export-images", action='store_true')

# parser.add_argument("-i", "--input", default=".", nargs='+')
# parser.add_argument("-o", "--output", default="tiles", nargs='?')
# parser.add_argument("-e", "--extension", default=".jpg", nargs='?')
# parser.add_argument("-t", "--tilesize", default=300, type=int, nargs='?')
# parser.add_argument("-c", "--centered", default=True, type=bool, nargs='?')
# parser.add_argument("-r", "--outputsize", type=int, nargs='?')
# parser.add_argument("-m", "--mode", default=MODE_TILE, choices=[MODE_TILE, MODE_UNTILE])


args = parser.parse_args()

sys.path.append(args.tensorflow_object_detection_path)
from object_detection.utils import ops as utils_ops

from object_detection.utils import label_map_util
from object_detection.utils import visualization_utils as vis_util

OD_FRAMEWORK_PATH = os.path.join(args.tensorflow_object_detection_path, "object_detection")

PATH_TO_CKPT = os.path.join(args.model_path, args.model_name, "frozen_inference_graph.pb")
PATH_TO_LABELS = os.path.join(OD_FRAMEWORK_PATH, 'data', 'mscoco_label_map.pbtxt')
NUM_CLASSES = 90

VISUALIZATION_THRESHOLD = 0.3
SAVE_THRESHOLD = 0.1

LIMIT = 40

TILESIZE = None
OUTPUTSIZE = None

if args.tilesize is not None:
    if len(args.tilesize) == 1:
        TILESIZE = [args.tilesize[0], args.tilesize[0]]
    elif len(args.tilesize) == 2:
        TILESIZE = [args.tilesize[0], args.tilesize[1]]
    else:
        print("illegal length of argument TILESIZE: {}".format(len(args.tilesize)))
        sys.exit(1)

    OUTPUTSIZE = [int(TILESIZE[0]*1), int(TILESIZE[1]*1)] # whats fed into the network

def run_inference_for_single_image(sess, image):
    
    # Get handles to input and output tensors
    ops = tf.get_default_graph().get_operations()
    all_tensor_names = {output.name for op in ops for output in op.outputs}
    tensor_dict = {}

    for key in [
            'num_detections', 'detection_boxes', 'detection_scores',
            'detection_classes', 'detection_masks']:

        tensor_name = key + ':0'
        if tensor_name in all_tensor_names:
            tensor_dict[key] = tf.get_default_graph().get_tensor_by_name(tensor_name)

    if 'detection_masks' in tensor_dict:
        # The following processing is only for single image
        detection_boxes = tf.squeeze(tensor_dict['detection_boxes'], [0])
        detection_masks = tf.squeeze(tensor_dict['detection_masks'], [0])
        # Reframe is required to translate mask from box coordinates to image coordinates and fit the image size.
        real_num_detection = tf.cast(tensor_dict['num_detections'][0], tf.int32)
        detection_boxes = tf.slice(detection_boxes, [0, 0], [real_num_detection, -1])
        detection_masks = tf.slice(detection_masks, [0, 0, 0], [real_num_detection, -1, -1])
        detection_masks_reframed = utils_ops.reframe_box_masks_to_image_masks(detection_masks, detection_boxes, image.shape[0], image.shape[1])
        detection_masks_reframed = tf.cast(tf.greater(detection_masks_reframed, 0.5), tf.uint8)
        # Follow the convention by adding back the batch dimension
        tensor_dict['detection_masks'] = tf.expand_dims(detection_masks_reframed, 0)

    image_tensor = tf.get_default_graph().get_tensor_by_name('image_tensor:0')

    # Run inference
    output_dict = sess.run(tensor_dict, feed_dict={image_tensor: np.expand_dims(image, 0)})

    # all outputs are float32 numpy arrays, so convert types as appropriate
    output_dict['num_detections'] = int(output_dict['num_detections'][0])
    output_dict['detection_classes'] = output_dict['detection_classes'][0].astype(np.uint8)
    output_dict['detection_boxes'] = output_dict['detection_boxes'][0]
    output_dict['detection_scores'] = output_dict['detection_scores'][0]
    if 'detection_masks' in output_dict:
        output_dict['detection_masks'] = output_dict['detection_masks'][0]

    return output_dict


detection_graph = tf.Graph()
with detection_graph.as_default():
    od_graph_def = tf.GraphDef()
    with tf.gfile.GFile(PATH_TO_CKPT, 'rb') as fid:
        serialized_graph = fid.read()
        od_graph_def.ParseFromString(serialized_graph)
        tf.import_graph_def(od_graph_def, name='')

label_map = label_map_util.load_labelmap(PATH_TO_LABELS)
categories = label_map_util.convert_label_map_to_categories(label_map, max_num_classes=NUM_CLASSES, use_display_name=True)
category_index = label_map_util.create_category_index(categories)

SOURCE = args.images
OUTPUT_FOLDER = args.output #"output"

if (args.output_directory_appendix is not None and len(args.output_directory_appendix) > 0):
    if OUTPUT_FOLDER.endswith("/"):
        OUTPUT_FOLDER = OUTPUT_FOLDER[:-1]
    OUTPUT_FOLDER = OUTPUT_FOLDER + args.output_directory_appendix

print("OUTPUT_FOLDER: " + OUTPUT_FOLDER)

if not os.path.exists(OUTPUT_FOLDER):
    try:
        os.makedirs(OUTPUT_FOLDER)
        print("created output dir: {}".format(OUTPUT_FOLDER))
    except Exception as e:
        print("could not create output dir: {}".format(OUTPUT_FOLDER))
        print(e)
        print("exit")
        sys.exit(-1)

images = []

if (SOURCE.endswith(".jpg")):
    images.append(SOURCE)
else: 
    for root, dirs, files in os.walk(SOURCE):
       for f in files:
           if (not f.endswith(".jpg")):
               continue
           images.append(os.path.join(root, f))                    


images = sorted(images, key=lambda filename: os.path.splitext(os.path.basename(filename))[0])

if LIMIT is not None:
    if len(images) > LIMIT:
        images = images[0:LIMIT]
        print("LIMIT ENFORCED")

def run(sess, filename, tilesize, outputsize):

    print("{}: {}_{}".format(filename, tilesize, outputsize))

    file_full_path = filename

    file_folder_only, file_name_only = os.path.split(file_full_path) # /foo/bar, baz.py
    _, file_folder_only = os.path.split(file_folder_only)            # /foo, bar  

    file_output_full_path = os.path.join(OUTPUT_FOLDER, file_name_only[:-4])
    if args.export == "xml":
        file_output_full_path += ".xml"
    elif args.export == "json":
        file_output_full_path += ".json"
    else:
        raise Exception("unknown file extension")

    if os.path.exists(file_output_full_path):
        print("{} already existing".format(file_output_full_path))
        return

    tm = TileManager(filename, tilesize, outputsize, jitter=True)

    for tile_id in tm.get_all_tile_ids():
        time0 = time.time()

        image = tm.get_tile_image(tile_id)

        time1 = time.time()

        # TODO: let this be done by the tilemanager instead
        # (im_width, im_height) = image.size
        # image_np = np.array(image.getdata()).reshape((im_height, im_width, 3)).astype(np.uint8)
        image_np = image

        time2 = time.time()

        output_dict = run_inference_for_single_image(sess, image_np)

        time3 = time.time()
        
        tm.pass_result(tile_id, output_dict)

        time4 = time.time()    
        #filename=image_path[image_path.rfind("/")+1:]
        # Image.fromarray(image_np).save(os.path.join(OUTPUT_IMG_FOLDER, filename))
        time5 = time.time()

        time_stop = time.time()
        timings = [
            time1 - time0,
            time2 - time1,
            time3 - time2,
            time4 - time3,
            time5 - time4,
            time_stop - time0
        ]
        #timings_avg.append(timings)
            
        print("{0} | open: {1:.2f} prepr: {2:.2f} inference: {3:.2f} viz: {4:.2f} save: {5:.2f} total: {6:.2f}".format(
            tile_id,
            timings[0],
            timings[1],
            timings[2],
            timings[3],
            timings[4],
            timings[5]
        )) 

    time6 = time.time()

    #image = tm.get_full_image()
    #image_np = load_image_into_numpy_array(image)
    output_dict = tm.get_full_results()

    time7 = time.time()

    # vis_util.visualize_boxes_and_labels_on_image_array(
    #     image_np,
    #     output_dict['detection_boxes'],
    #     output_dict['detection_classes'],
    #     output_dict['detection_scores'],
    #     category_index,
    #     instance_masks=output_dict.get('detection_masks'),
    #     instance_boundaries=None,
    #     keypoints=None,
    #     use_normalized_coordinates=False,
    #     max_boxes_to_draw=None,
    #     min_score_thresh=.5,
    #     agnostic_mode=False,
    #     line_thickness=8,
    #     groundtruth_box_visualization_color='black',
    #     skip_scores=False,
    #     skip_labels=False
    # )
    # Image.fromarray(image_np).save(os.path.join(OUTPUT_IMG_FOLDER, "result.jpg"))

    # tm._draw_bounding_boxes("output_{}_{}-{}.jpg".format(MODEL_NAME, tilesize, outputsize), output_dict["detection_boxes"], output_dict["detection_scores"])

    # bbox2voc.convert(".", filename, tm.get_image_size(), output_dict["detection_boxes"])

    # print(output_dict["detection_classes"])

    # print(len(output_dict["detection_boxes"]))
    # print(len(output_dict["detection_classes"]))

    # print(bbox2voc.class_indices_to_class_names(category_index, output_dict["detection_classes"]))
    # print(category_index)

    # import pickle
    # pickle.dump(output_dict["detection_boxes"], open( "detection_boxes.pickle", "wb" ))
    # pickle.dump(output_dict["detection_classes"], open( "detection_classes.pickle", "wb" ))
    # pickle.dump(output_dict["detection_scores"], open( "detection_scores.pickle", "wb" ))
    # pickle.dump(category_index, open( "category_index.pickle", "wb" ))

    if args.export_images:
        export_filename = None
        if TILESIZE is None:
            export_filename = os.path.join(OUTPUT_FOLDER, "{}_{}-full.jpg".format(file_name_only, args.model_name))
        else:
            export_filename = os.path.join(OUTPUT_FOLDER, "{}_{}_{}x{}-{}x{}.jpg".format(file_name_only, args.model_name, tilesize[0], tilesize[1], outputsize[0], outputsize[1]))
        tm._draw_bounding_boxes(export_filename, output_dict["detection_boxes"], output_dict["detection_scores"], VISUALIZATION_THRESHOLD)

    exporter = None

    if args.export == "xml":
        exporter = converter.convert_to_voc
    elif args.export == "json":
        exporter = converter.convert_to_json
    else:
        raise Exception("unknown file extension")

    output_boxes = converter.sanitize_coordinate_order(output_dict["detection_boxes"])
    output_classes = converter.class_indices_to_class_names(category_index, output_dict["detection_classes"])
    if output_dict["detection_scores"] is not None:
        output_scores = output_dict["detection_scores"].tolist()
    else: 
        output_scores = []

    # filter empty SSD detection boxes
    filtered_boxes = []
    filtered_classes = []
    filtered_scores = []
    for i in range(0, len(output_boxes)):
        if output_scores[i] < SAVE_THRESHOLD:
            continue

        filtered_boxes.append(output_boxes[i])
        filtered_classes.append(output_classes[i])
        filtered_scores.append(output_scores[i])


    exporter(
        file_folder_only, 
        file_name_only, 
        file_full_path, 
        tm.get_image_size(),
        filtered_boxes,
        filtered_classes,
        filtered_scores,
        file_output_full_path
    )

    print("get results: {0:.2f} viz/exp: {1:.2f}".format(time7-time6, time.time()-time7))


import time

counter = 0
total_time = time.time()
with detection_graph.as_default():                                
    with tf.Session() as sess:

        for item in images:
            counter += 1
            total_time = time.time()
            run(sess, item, TILESIZE, OUTPUTSIZE)
    
            print(">> {0}/{1} | time: {2:.2f}s".format(counter, len(images), time.time()-total_time))


         
