#!/bin/bash

INPUT_ROOT=/media/internal/despat_compressedtime/
# INPUT_ROOT=/media/internal/DESPATDATASETS/
#INPUT_ROOT=/Users/volzotan/Documents/DESPATDATASETS/

TENSORFLOW_OBJECT_DETECTION_DIR=/home/volzotan/tensorflow_directory/models/research
#TENSORFLOW_OBJECT_DETECTION_DIR=/Users/volzotan/Downloads/tensorflow/models/research

MODEL_PATH=/home/volzotan/tensorflow_directory/zoo/
#MODEL_PATH=/Users/volzotan/GIT/despat/detector/models/

DIRECTORY=hamburg

#MODEL_NAME=ssd_mobilenet_v1_coco_2018_01_28
MODEL_NAME=ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03
#MODEL_NAME=faster_rcnn_inception_v2_coco_2018_01_28

TILESIZE=900
#TILESIZE=2000

INPUT=$INPUT_ROOT$DIRECTORY
OUTPUT=$INPUT"_annotation/"$MODEL_NAME"_JITTER"

APPENDIX="_"$TILESIZE"px"

echo $INPUT
echo $OUTPUT
echo $MODELS

# mkdir $OUTPUT

python3 inference_test.py                                                       \
    --images $INPUT                                                             \
    --output $OUTPUT                                                            \
    --tensorflow-object-detection-path $TENSORFLOW_OBJECT_DETECTION_DIR         \
    --model_path $MODEL_PATH                                                    \
    --model_name $MODEL_NAME                                                    \
    --tilesize $TILESIZE                                                        \
    --output_directory_appendix $APPENDIX                                       \
    --export-images