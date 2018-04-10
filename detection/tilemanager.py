from PIL import Image
import PIL
import numpy as np

class TileManager(object):

    posx    = 0
    posy    = 0

    tiles = None

    # outputsize should be the inputsize of the network
    # if the network does further scaling as preprocessing
    # _convert_coordinate() will yield wrong results

    def __init__(self, filename, tilesize=1000, outputsize=300, centered=False):

        self.filename = filename

        self.tilesize = tilesize
        self.outputsize = outputsize

        self.image = Image.open(self.filename)

        self.imagewidth = self.image.size[0]
        self.imageheight = self.image.size[1]

        self.xoffset = 0
        self.yoffset = 0

        if centered:
            self.xoffset = int( (self.imagewidth % self.tilesize) / 2 )
            self.yoffset = int( (self.imageheight % self.tilesize) / 2 )

        x_tiles = int (self.imagewidth / self.tilesize)
        y_tiles = int (self.imageheight / self.tilesize)

        # self.tiles = [[{"x": x, "y": y} for x in range(0, x_tiles)] for y in range(0, y_tiles)]

        self.tiles = {}
        self.requested_tiles = []

        for y in range(0, y_tiles):
            for x in range(0, x_tiles):
                tile_id = y * x_tiles + x
                self.tiles[tile_id] = {"tileid": tile_id, "x": x, "y": y}

        for tile_id in self.tiles:
            tile = self.tiles[tile_id]
            tile["result"] = None


    def _get_dim_for_tile(self, x, y):
        dim = (
            x * self.tilesize + self.xoffset, 
            y * self.tilesize + self.yoffset, 
            x * self.tilesize + self.tilesize + self.xoffset, 
            y * self.tilesize + self.tilesize + self.yoffset
        )
        return dim


    def _convert_coordinates(self, data, x, y, normalized=True):

        if normalized:
            data = np.multiply(data, self.outputsize)

        if self.tilesize != self.outputsize:
            scale = float(self.tilesize) / float(self.outputsize)
            data = np.multiply(data, scale)

        # add to the first and third row the x offset
        # and vice versa for the y offset

        data[:, 0] = np.add(data[:, 0], self.xoffset + x * self.tilesize)
        data[:, 2] = np.add(data[:, 2], self.xoffset + x * self.tilesize)
        data[:, 1] = np.add(data[:, 1], self.yoffset + y * self.tilesize)
        data[:, 3] = np.add(data[:, 3], self.yoffset + y * self.tilesize)
            
        return data


    def get_all_tile_ids(self):
        return [item[0] for item in self.tiles.items()]


    def get_all_tiles(self):
        return [item[1] for item in self.tiles.items()]


    def get_full_image(self):
        return self.image


    def get_tile_image(self, tile_id):
        tile = self.tiles[tile_id]
        crop_dim = self._get_dim_for_tile(tile["x"], tile["y"])

        cropped = self.image.crop(crop_dim)
        resized = cropped.resize((self.outputsize, self.outputsize), PIL.Image.BICUBIC)

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

        full_results["detection_boxes"] = np.concatenate(boxes, axis=0)
        full_results["detection_scores"] = np.hstack(scores)
        full_results["detection_classes"] = np.hstack(classes)

        return full_results


if __name__ == "__main__":
    tm = TileManager("pedestriancrossing.jpg")
    # for i in range(0, 10):
    #     print(tm.get_next_tile())

    # print(tm.get_all_tiles())

    tile = tm.get_all_tiles()[2]
    image = tm.get_tile_image(tile["tileid"])
    print("{} {}".format(tile["x"], tile["y"]))
    image.show()