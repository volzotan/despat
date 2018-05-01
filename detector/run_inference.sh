#INPUT=/home/volzotan/DESPATDATASETS/18-04-18_schillerstrasse_ZTE
INPUT=/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ

OUTPUT=$INPUT"_annotation"

#MODELS=/home/volzotan/tensorflow/zoo
MODELS=/Users/volzotan/GIT/despat/detector/models

#TENSORFLOW_OBJECT_DETECTION_DIR=/home/volzotan/tensorflow/models/research
TENSORFLOW_OBJECT_DETECTION_DIR=/Users/volzotan/Downloads/tensorflow/models/research

echo $INPUT
echo $OUTPUT
echo $MODELS

mkdir $OUTPUT

python3 inference_test.py                                                       \
    --images $INPUT                                                             \
    --output $OUTPUT                                                            \
    --tensorflow-object-detection-path $TENSORFLOW_OBJECT_DETECTION_DIR         \
    --models $MODELS                                                            \
    --export-images