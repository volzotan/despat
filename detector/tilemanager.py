from PIL import Image
import numpy as np
import imageio
from skimage.transform import resize # requires scikit-image
import sys
import random
sys.path.append('..')
from util.drawhelper import Drawhelper
import util.converter

# for the pass_ground_truth() calculations
import shapely 
from shapely.geometry import Polygon

class TileManager(object):

    posx    = 0
    posy    = 0

    tiles   = None

    # outputsize should be the inputsize of the network
    # if the network does further scaling as preprocessing
    # _convert_coordinate() will yield wrong results

    def __init__(self, filename, tilesize, outputsize, centered=True, jitter=False):

        self.filename = filename

        self.image = imageio.imread(self.filename) #Image.open(self.filename)

        self.imagewidth = self.image.shape[1]
        self.imageheight = self.image.shape[0]

        if tilesize is None:
            tilesize = [self.imagewidth, self.imageheight]
            outputsize = [self.imagewidth, self.imageheight]

        if type(tilesize) is int:
            tilesize = [tilesize, tilesize]

        if type(outputsize) is int:
            outputsize = [outputsize, outputsize] 

        self.tilesize = tilesize
        self.outputsize = outputsize

        self.xoffset = 0
        self.yoffset = 0

        if centered:
            self.xoffset = int( (self.imagewidth % self.tilesize[0]) / 2 )
            self.yoffset = int( (self.imageheight % self.tilesize[1]) / 2 )

            if jitter:
                self.xoffset = int(self.xoffset * random.uniform(0, 2))
                self.yoffset = int(self.yoffset * random.uniform(0, 2))

        x_tiles = int( self.imagewidth / self.tilesize[0] )
        y_tiles = int( self.imageheight / self.tilesize[1] )

        # self.tiles = [[{"x": x, "y": y} for x in range(0, x_tiles)] for y in range(0, y_tiles)]

        self.tiles = {}
        self.requested_tiles = []

        for y in range(0, y_tiles):
            for x in range(0, x_tiles):
                tile_id = y * x_tiles + x
                self.tiles[tile_id] = {"tileid": tile_id, "x": x, "y": y}

        for tile_id in self.tiles:
            tile = self.tiles[tile_id]
            tile["result"] = []


    def _crop_and_resize(self, crop_dim, outputsize):
        minx = crop_dim[0]
        miny = crop_dim[1]
        maxx = crop_dim[2]
        maxy = crop_dim[3]
        
        cropped = self.image[miny:maxy, minx:maxx]

        if maxx-minx == outputsize[0] and maxy-miny == outputsize[1]:
            return cropped
        else:
            resized = resize(cropped, (outputsize[1], outputsize[0]), anti_aliasing=False, preserve_range=True)
            return resized.astype(np.uint8)


    def _get_dim_for_tile(self, x, y):
        dim = (
            x * self.tilesize[0] + self.xoffset, 
            y * self.tilesize[1] + self.yoffset, 
            x * self.tilesize[0] + self.xoffset + self.tilesize[0],
            y * self.tilesize[1] + self.yoffset + self.tilesize[1]
        )
        return dim


    def _convert_coordinates(self, data, x, y, normalized=True):

        if normalized:
            data = np.multiply(data, [self.outputsize[1], self.outputsize[0], self.outputsize[1], self.outputsize[0]])

        if self.tilesize[0] != self.outputsize[0] or self.tilesize[1] != self.outputsize[1]:
            scale = float(self.tilesize[0]) / float(self.outputsize[0])
            data = np.multiply(data, scale)

        # bboxes are encoded as [y_min, x_min, y_max, x_max]

        # add to the second and fourth row the x offset
        # and vice versa for the y offset

        data[:, 0] = np.add(data[:, 0], self.yoffset + y * self.tilesize[1])
        data[:, 1] = np.add(data[:, 1], self.xoffset + x * self.tilesize[0])

        data[:, 2] = np.add(data[:, 2], self.yoffset + y * self.tilesize[1])
        data[:, 3] = np.add(data[:, 3], self.xoffset + x * self.tilesize[0])
            
        return data


    def get_image_size(self):
        return (self.image.shape[1], self.image.shape[0])


    def get_all_tile_ids(self):
        return [item[0] for item in self.tiles.items()]


    def get_all_tiles(self):
        return [item[1] for item in self.tiles.items()]


    def get_full_image(self):
        return self.image


    def get_tile_image(self, tile_id):
        tile = self.tiles[tile_id]
        crop_dim = self._get_dim_for_tile(tile["x"], tile["y"])

        resized = self._crop_and_resize(crop_dim, self.outputsize)

        # cropped = self.image.crop(crop_dim)
        # resized = cropped.resize((self.outputsize[0], self.outputsize[1]), PIL.Image.BICUBIC)

        return resized


    def get_next_tile(self):
        for key, value in self.tiles.items():
            if value not in self.requested_tiles:
                self.requested_tiles.append(value)
                return value

        return None


    def pass_result(self, tile_id, result):
        self.tiles[tile_id]["result"] = result

        self.tiles[tile_id]["result"]["detection_boxes"] = self._convert_coordinates(
            self.tiles[tile_id]["result"]["detection_boxes"], 
            self.tiles[tile_id]["x"], 
            self.tiles[tile_id]["y"]
        )

        #print(result)

    """
        pass XML-VOC ground truth data so detection bounding boxes can be matched to tiles
        format: 
                      x_min y_min, x_max, y_max
            [{'box': [2555, 885, 2585, 999], 'class': 'person'}, ...]
    """
    def pass_ground_truth(self, data):

        shapely_tiles = {}

        for tile_id in self.tiles:
            tile = self.tiles[tile_id]
            dims = self._get_dim_for_tile(tile["x"], tile["y"])
            shapely_tiles[tile_id] = Polygon([
                [dims[0], dims[1]],
                [dims[2], dims[1]], 
                [dims[2], dims[3]], 
                [dims[0], dims[3]]
            ])

        # for tile_id in shapely_tiles:
        #     print(shapely_tiles[tile_id])

        for datapoint in data:
            box = datapoint["box"]
            classname = datapoint["class"]

            xmin = box[0]
            ymin = box[1]
            xmax = box[2]
            ymax = box[3]

            box_poly = Polygon([
                [xmin, ymin],
                [xmax, ymin],
                [xmax, ymax],
                [xmin, ymax]
            ])

            for tile_id in shapely_tiles:
                intersection = box_poly.intersection(shapely_tiles[tile_id])

                if not intersection.is_empty:
                    # print(intersection)

                    tile = self.tiles[tile_id]
                    dims = self._get_dim_for_tile(tile["x"], tile["y"])

                    intersection_trans = shapely.affinity.translate(intersection, -dims[0], -dims[1])

                    # TODO: what about resized tiles?

                    tile = self.tiles[tile_id]
                    tile["result"].append([intersection_trans, classname])


    def get_tile_ground_truth(self, tile_id):
        return self.tiles[tile_id]["result"]


    def _get_tile_borders(self):
        boxes_tiles = []
        for _, tile in self.tiles.items():
            boxes_tiles.append(self._get_dim_for_tile(tile["x"], tile["y"]))

        return boxes_tiles


    def _draw_bounding_boxes(self, filename, bboxes, scores, threshold):

        boxes_above_threshold = []

        if bboxes is not None and len(bboxes) > 0:
            for i in range(0, len(bboxes)):
                if scores is not None and scores[i] < threshold:
                    continue
                boxes_above_threshold.append(bboxes[i]) #[bboxes[i][1], bboxes[i][0], bboxes[i][3], bboxes[i][2]])

        boxes_tiles = self._get_tile_borders()

        drawhelper = Drawhelper(self.filename, filename)
        drawhelper.add_boxes(boxes_above_threshold, color=(0, 255, 0), strokewidth=4, inverse_coordinates=True)
        drawhelper.add_boxes(boxes_tiles, color=(0, 0, 0), strokewidth=1)
        drawhelper.draw()


    def get_full_results(self):
        full_results = {
            "num_detections": 0
        }

        boxes = []
        scores = []
        classes = []

        for _, tile in self.tiles.items():
            if tile["result"] is None:
                print("missing results: {} x: {} y: {}".format(
                    tile["tileid"], tile["x"], tile["y"]
                ))

            full_results["num_detections"] += tile["result"]["num_detections"]

            boxes.append(tile["result"]["detection_boxes"])
            scores.append(tile["result"]["detection_scores"])
            classes.append(tile["result"]["detection_classes"])

        if full_results["num_detections"] == 0:
            # TODO: useful behaviour for zero detections
            full_results["detection_boxes"] = None
            full_results["detection_scores"] = None
            full_results["detection_classes"] = None

            print("EMPTY RESULTS")

            return full_results

        full_results["detection_boxes"] = np.concatenate(boxes, axis=0)
        full_results["detection_scores"] = np.hstack(scores)
        full_results["detection_classes"] = np.hstack(classes)

        return full_results


if __name__ == "__main__":
    tm = TileManager("pedestriancrossing.jpg", [2000, 1000], [500, 250])
    # for i in range(0, 10):
    #     print(tm.get_next_tile())

    # print(tm.get_all_tiles())

    tile = tm.get_all_tiles()[1]
    imagemat = tm.get_tile_image(tile["tileid"])
    print("{} {}".format(tile["x"], tile["y"]))
    #scipy.misc.imshow(imagemat)
    image = Image.fromarray(imagemat, 'RGB')
    image.show()