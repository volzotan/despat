#!/bin/bash

INPUT_ROOT=/media/internal/DESPATDATASETS/
#INPUT_ROOT=/Users/volzotan/Documents/DESPATDATASETS/

TENSORFLOW_OBJECT_DETECTION_DIR=/home/volzotan/tensorflow_directory/models/research
#TENSORFLOW_OBJECT_DETECTION_DIR=/Users/volzotan/Downloads/tensorflow/models/research

MODEL_PATH=/home/volzotan/tensorflow_directory/zoo/
#MODEL_PATH=/Users/volzotan/GIT/despat/detector/models/

MODEL_NAMES=(
 #   "ssd_mobilenet_v1_coco_2018_01_28"
 #   "ssd_mobilenet_v2_coco_2018_03_29"
# "ssd_mobilenet_v1_quantized_300x300_coco14_sync_2018_07_03"
 #   "ssd_resnet50_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03"
 #   "ssd_mobilenet_v1_fpn_shared_box_predictor_640x640_coco14_sync_2018_07_03"
 #   "ssd_inception_v2_coco_2018_01_28"
 #   "ssdlite_mobilenet_v2_coco_2018_05_09"
 #   "faster_rcnn_inception_v2_coco_2018_01_28"
    "faster_rcnn_resnet101_coco_2018_01_28"
 #   "faster_rcnn_nas_coco_2018_01_28"
)

TILESIZES=( 
    "300" "350" "400" "450" "500" "550" "600" "650" 
#    "640" 
    "700" "750" "800" "850" "900" "950" "1000" "1050" 
    "1100" "1150" "1200" "1250" "1300" "1350"
    "1400" "1450" "1500" "1550" "1600" "1650"
    "1700" "1750" "1800" "1850" "1900" "1950" "2000"
    "2050" "2100" "2150" "2200" "2250" "2300" "2350"
    "2400" "2450" "2500" "2550" "2600" "2650" "2700"
    "2750" "2800" "2850" "2900" "2950" "3000"
)

INPUT=$INPUT_ROOT"inference_speed_testset"

for TILESIZE in "${TILESIZES[@]}"
do
    for MODEL_NAME in "${MODEL_NAMES[@]}"
    do

        OUTPUT=$INPUT"_annotation/"$MODEL_NAME
        APPENDIX="_"$TILESIZE"px"

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
            --tilesize $TILESIZE                                                        \
            --output_directory_appendix $APPENDIX > "timing_"$MODEL_NAME"_"$TILESIZE".log"

    done 
done