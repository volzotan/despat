import tensorflow as tf

for example in tf.compat.v1.python_io.tf_record_iterator("test.record"):
    print(tf.train.Example.FromString(example))