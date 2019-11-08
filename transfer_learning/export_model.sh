export PYTHONPATH=$PYTHONPATH:`pwd`:`pwd`/slim
python export_inference_graph.py --input_type image_tensor                                              \
--pipeline_config_path ssd_mobilenet_v1_coco_2018_01_28/pipeline.config                                 \
--trained_checkpoint_prefix output/model.ckpt-20000                                                     \
--output_directory trained_inference_graph