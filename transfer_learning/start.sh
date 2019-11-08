export PATH="/usr/local/cuda-10.0/bin:$PATH"
export LD_LIBRARY_PATH="/usr/local/cuda-10.0/lib64:$LD_LIBRARY_PATH"
export PYTHONPATH=$PYTHONPATH:`pwd`:`pwd`/slim
python3 model_main.py                                                           \
--logtostderr                                                                   \
--train_dir=/home/volzotan/despat/transfer_learning                             \
--model_dir=/home/volzotan/despat/transfer_learning/output                      \
--pipeline_config_path=ssd_mobilenet_v1_coco_2018_01_28/pipeline.config