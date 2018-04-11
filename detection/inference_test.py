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

sys.path.append("/Users/volzotan/Downloads/tensorflow/models/research")
from object_detection.utils import ops as utils_ops

from object_detection.utils import label_map_util
from object_detection.utils import visualization_utils as vis_util

OD_FRAMEWORK_PATH = "/Users/volzotan/Downloads/tensorflow/models/research/object_detection"


MODEL_NAME = "ssd_mobilenet_v1_coco_2017_11_17"
#MODEL_NAME = "ssd_mobilenet_v2_coco_2018_03_29" 
#MODEL_NAME = "faster_rcnn_inception_v2_coco_2018_01_28" 
#MODEL_NAME = "faster_rcnn_resnet101_coco_2018_01_28" 
#MODEL_NAME = "faster_rcnn_nas_coco_2018_01_28" 

PATH_TO_CKPT = os.path.join("models", MODEL_NAME, "frozen_inference_graph.pb")
PATH_TO_LABELS = os.path.join(OD_FRAMEWORK_PATH, 'data', 'mscoco_label_map.pbtxt')
NUM_CLASSES = 90

TILESIZE = 1500
OUTPUTSIZE = 1000 #300 # whats fed into the network


def load_image_into_numpy_array(image):
    (im_width, im_height) = image.size
    return np.array(image.getdata()).reshape((im_height, im_width, 3)).astype(np.uint8)


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
            tensor_dict[key] = tf.get_default_graph().get_tensor_by_name(
                    tensor_name)

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

tm = TileManager("pedestriancrossing.jpg", tilesize=TILESIZE, outputsize=OUTPUTSIZE)

#INPUT_IMG_PATH = "/Users/volzotan/GIT/despat/detection"
#INPUT_IMG_FOLDER = "tiles_500"
#OUTPUT_IMG_FOLDER = "output_" + INPUT_IMG_FOLDER 
OUTPUT_IMG_FOLDER = "output"

if not os.path.exists(OUTPUT_IMG_FOLDER):
    try:
        os.makedirs(OUTPUT_IMG_FOLDER)
        print("created output dir: {}".format(OUTPUT_IMG_FOLDER))
    except Exception as e:
        print("could not create output dir: {}".format(OUTPUT_IMG_FOLDER))
        print(e)
        print("exit")
        sys.exit(-1)

#TEST_IMAGE_PATHS = []
#for root, dirs, files in os.walk(os.path.join(INPUT_IMG_PATH, INPUT_IMG_FOLDER)):
#    for f in files:
#        if (not f.endswith(".jpg")):
#            continue
#        TEST_IMAGE_PATHS.append(os.path.join(root, f))                    


import time
timings_avg = []

with detection_graph.as_default():                                
    with tf.Session() as sess:

        for tile_id in tm.get_all_tile_ids():
            time0 = time.time()
            image = tm.get_tile_image(tile_id)
            # image = cv2.imread(image_path)
            # image = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)    
            time1 = time.time()
            # the array based representation of the image will be used later in order to prepare the
            # result image with boxes and labels on it.
            image_np = load_image_into_numpy_array(image)
            # Expand dimensions since the model expects images to have shape: [1, None, None, 3]
            image_np_expanded = np.expand_dims(image_np, axis=0)
            # Actual detection.
            time2 = time.time()
            output_dict = run_inference_for_single_image(sess, image_np)
            # Visualization of the results of a detection.
            time3 = time.time()
            # vis_util.visualize_boxes_and_labels_on_image_array(
            #     image_np,
            #     output_dict['detection_boxes'],
            #     output_dict['detection_classes'],
            #     output_dict['detection_scores'],
            #     category_index,
            #     instance_masks=output_dict.get('detection_masks'),
            #     use_normalized_coordinates=True,
            #     line_thickness=8)
            #plt.figure(figsize=IMAGE_SIZE)
            #plt.imshow(image_np)
            
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
            timings_avg.append(timings)
                
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

image = tm.get_full_image()
image_np = load_image_into_numpy_array(image)
output_dict = tm.get_full_results()

time7 = time.time()

vis_util.visualize_boxes_and_labels_on_image_array(
    image_np,
    output_dict['detection_boxes'],
    output_dict['detection_classes'],
    output_dict['detection_scores'],
    category_index,
    instance_masks=output_dict.get('detection_masks'),
    instance_boundaries=None,
    keypoints=None,
    use_normalized_coordinates=False,
    max_boxes_to_draw=None,
    min_score_thresh=.5,
    agnostic_mode=False,
    line_thickness=8,
    groundtruth_box_visualization_color='black',
    skip_scores=False,
    skip_labels=False
)

Image.fromarray(image_np).save(os.path.join(OUTPUT_IMG_FOLDER, "result.jpg"))

print("get results: {0:.2f} viz: {1:.2f}".format(time7-time6, time.time()-time7))
     