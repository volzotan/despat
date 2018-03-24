from PIL import Image
import PIL
import os
import sys
import argparse

MODE_TILE   = "tile"
MODE_UNTILE = "untile"

parser = argparse.ArgumentParser()
parser.add_argument("-i", "--input", default=".", nargs='+')
parser.add_argument("-o", "--output", default="tiles", nargs='?')
parser.add_argument("-e", "--extension", default=".jpg", nargs='?')
parser.add_argument("-t", "--tilesize", default=300, type=int, nargs='?')
parser.add_argument("-r", "--outputsize", type=int, nargs='?')
parser.add_argument("-m", "--mode", default=MODE_TILE, choices=[MODE_TILE, MODE_UNTILE])
args = parser.parse_args()

INPUT = args.input
OUTPUT_FOLDER = args.output
EXTENSION = args.extension

images = []

# print(args.input)
# sys.exit(-1)


for inp in INPUT:
    if inp.endswith(EXTENSION):
        images.append(("", inp))
        INPUT.remove(inp)
        
for inp in INPUT:
    for root, dirs, files in os.walk(inp):
        for f in files:
            if f.lower().endswith(EXTENSION):
                images.append((root, f))
                

print("found {} input images".format(len(images)))

if not os.path.exists(OUTPUT_FOLDER):
    try:
        os.makedirs(OUTPUT_FOLDER)
        print("created output dir: {}".format(OUTPUT_FOLDER))
    except Exception as e:
        print("could not create output dir: {}".format(OUTPUT_FOLDER))
        print(e)
        print("exit")
        sys.exit(-1)


if args.mode == MODE_UNTILE:

    #tile_size = (1000, 1000)
    fname = images[0][1]
    border_length = int(fname[ fname.rfind("_")+1 : fname.rfind(".") ])
    tile_size = (border_length, border_length)

    tiles = [None] * len(images)

    max_tile_x = 0
    max_tile_y = 0

    for image in images:

        # pedestriancrossing2-0072-002x005_300_1000.jpg

        input_path = os.path.join(image[0], image[1])
        fname = image[1]

        tile_x = int(fname[ fname.rfind("-")+1 : fname.find("x") ])
        tile_y = int(fname[ fname.rfind("x")+1 : fname.find("_") ])

        if tile_x > max_tile_x:
            max_tile_x = tile_x        
        if tile_y > max_tile_y:
            max_tile_y = tile_y

        tilenumber = int(fname[ fname.find("-")+1 : fname.rfind("-") ])

        tiles[tilenumber] = (tile_x, tile_y, input_path)

    img = Image.new("RGB", (max_tile_x * border_length, max_tile_y * border_length))

    for tile in tiles:
        img_tile = Image.open(tile[2])
        img.paste(img_tile, (tile[0]*border_length, tile[1]*border_length, tile[0]*border_length+border_length, tile[1]*border_length+border_length))

    fname = images[0][1]
    extension = fname[fname.rfind("."):]
    fname = fname[:fname.find("-")]
    new_filename = fname + "_untiled" + extension
    new_full_path = os.path.join(OUTPUT_FOLDER, new_filename)

    img.save(new_full_path)
    

elif args.mode == MODE_TILE:

    for image in images:

        input_path = os.path.join(image[0], image[1])
        img = Image.open(input_path)

        relative_path = ""
        extension = image[1][image[1].rfind("."):]

        border_length = args.tilesize
        tiles = (int(img.size[0] / border_length), int(img.size[1] / border_length))

        for x in range(0, tiles[0]):
            for y in range(0, tiles[1]):
                img_crop = img.crop((x*border_length, y*border_length, x*border_length+border_length, y*border_length+border_length))
                crop_number = y*tiles[0]+x

                border_length_resize = args.tilesize
                if not args.outputsize is None: 
                    border_length_resize = args.outputsize
                    img_crop = img_crop.resize((border_length_resize, border_length_resize))

                filename = image[1]
                if (filename.find("/") >= 0):
                    filename = filename[filename.find("/")+1:]
                new_filename = filename[:-len(extension)] + "-" + str(crop_number).zfill(4) + extension
                new_filename = "{0}-{1:04d}-{2:03d}x{3:03d}_{4}_{5}{6}".format(filename[:-len(extension)], crop_number, x, y, border_length, border_length_resize, extension)
                new_full_path = os.path.join(OUTPUT_FOLDER, relative_path, new_filename)

                img_crop.save(new_full_path)

                print("saved: {}".format(new_full_path))

        # if args.mode == "resize":
        #     for border_length in args.sizes:
        #         scaled_size = (0, 0)

        #         if img.size[0] > img.size[1]:
        #             scaled_size = ( border_length, img.size[1] / (img.size[0] / border_length) )
        #         else: 
        #             scaled_size = ( img.size[0] / (img.size[1] / border_length), border_length )
        #         scaled_size = (int(scaled_size[0]), int(scaled_size[1]))

        #         try:
        #             img = img.resize(scaled_size, PIL.Image.BICUBIC)
        #         except Exception as e:
        #             print("converting {} failed: {}".format(input_path, e))
        #             break;

        #         new_filename = image[1][:-len(extension)] + "_" + str(border_length) + extension
        #         new_full_path = os.path.join(OUTPUT_FOLDER, relative_path, new_filename)
        #         img.save(new_full_path)

        #         print("saved: {}".format(new_full_path))

else:
    print("unknown mode. exit.")
    sys.exit(-1)