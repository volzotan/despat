import sys
from os import walk, path
from shutil import copy, copyfile

# if (len(sys.argv) != 3):
#     print("too many or few arguments")
#     sys.exit(1)

image_dir = sys.argv[1]
try:
    annotation_dir = sys.argv[2]
except Exception as e:
    annotation_dir = image_dir + "_annotation"

# image_dir = "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ"
# annotation_dir = "/Users/volzotan/Documents/DESPATDATASETS/18-04-09_darmstadt_motoZ_annotation"

if not path.isdir(image_dir):    
    print("image dir {} is not a directory".format(image_dir))
    sys.exit(1)

if not path.isdir(annotation_dir):    
    print("annotation dir {} is not a directory".format(annotation_dir))
    sys.exit(2)

print("image directory: {} \nannotation directory: {}".format(image_dir, annotation_dir))

images = []
annotations = []

for (dirpath, dirnames, filenames) in walk(image_dir):
    for filename in filenames:
        if filename.endswith(".jpg"):
            images.append(path.splitext(filename)[0])

for (dirpath, dirnames, filenames) in walk(annotation_dir):
    for filename in filenames:
        if filename.endswith(".xml"):
            annotations.append(path.splitext(filename)[0])

# TODO: sort?

images.sort()
annotations.sort()

if len(annotations) == 0:
    print("no annotations found")
    sys.exit(3)

# for annotation in annotations:
#     print(annotation)

# print("-------")

# for image in images:
#     print(image)


for i in range(0, len(images)):
    if images[i] in annotations:
        continue

    src = path.join(annotation_dir, annotations[i-1] + ".xml")
    dst = path.join(annotation_dir, images[i] + ".xml")
    copy(src, dst)
    print("# copied annotations from {} to {}".format(annotations[i-1], dst))
    break
