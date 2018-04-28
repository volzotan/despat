INPUT=/home/volzotan/DESPATDATASETS/18-04-18_schillerstrasse_ZTE
OUTPUT=$INPUT"_annotation"
MODELS=/home/volzotan/tensorflow/zoo

TENSORFLOW_OBJECT_DETECTION_DIR=/home/volzotan/tensorflow/models/research

echo $INPUT
echo $OUTPUT
echo $MODELS

mkdir $OUTPUT

python inference_test.py                                                        \
    --images $INPUT                                                             \
    --output $OUTPUT                                                            \
    --tensorflow-object-detection-path $TENSORFLOW_OBJECT_DETECTION_DIR         \
    --models $MODELS                                                            \
    --export-images