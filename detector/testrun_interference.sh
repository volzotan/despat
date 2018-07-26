#!/bin/bash

TENSORFLOW_OBJECT_DETECTION_DIR=/Users/volzotan/Downloads/tensorflow/models/research

MODEL_PATH=/Users/volzotan/GIT/despat/detector/models/

#MODEL_NAME=ssd_mobilenet_v1_coco_2018_01_28
#MODEL_NAME=ssd_mobilenet_v2_coco_2018_03_29
#MODEL_NAME=ssd_mobilenet_v1_quantized_300x300_coco14_sync_2018_07_03
#MODEL_NAME=ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03
#MODEL_NAME=ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03
#MODEL_NAME=ssd_inception_v2_coco_2018_01_28
#MODEL_NAME=ssdlite_mobilenet_v2_coco_2018_05_09
#MODEL_NAME=faster_rcnn_inception_v2_coco_2018_01_28
#MODEL_NAME=faster_rcnn_resnet101_coco_2018_01_28
MODEL_NAME=faster_rcnn_nas_coco_2018_01_28

INPUT=/Users/volzotan/Downloads/tmtest
OUTPUT=$INPUT"_annotation/"$MODEL_NAME

echo $INPUT
echo $OUTPUT
echo $MODELS

mkdir $OUTPUT

python3 inference_test.py                                                       \
    --images $INPUT                                                             \
    --output $OUTPUT                                                            \
    --tensorflow-object-detection-path $TENSORFLOW_OBJECT_DETECTION_DIR         \
    --model_path $MODEL_PATH                                                    \
    --model_name $MODEL_NAME                                                    \
    --export-images