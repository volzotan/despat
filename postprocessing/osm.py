import matplotlib.pyplot as plt
import numpy as np

import math
import urllib2
import StringIO
from PIL import Image, ImageDraw

import sys
import os

import cv2
import pickle

ZOOM_LEVEL = 19 # max zoom level is 19

"""
taken from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Lon..2Flat._to_tile_numbers_2

"""
class OSM(object):

    def __init__(self, lat, lon, viewport_length, viewport_height, zoom=19):

        self.lat                = lat
        self.lon                = lon
        self.viewport_length    = viewport_length
        self.viewport_height    = viewport_height
        self.zoom               = zoom

        self.xmin = 0
        self.xmax = 0
        self.ymin = 0
        self.ymax = 0

        self.cropoffset_x = 0
        self.cropoffset_y = 0


    def latlon2tile(self, lat_deg, lon_deg, quantization=False):
        lat_rad = math.radians(lat_deg)
        n = 2.0 ** self.zoom
        xtile = (lon_deg + 180.0) / 360.0 * n
        ytile = (1.0 - math.log(math.tan(lat_rad) + (1 / math.cos(lat_rad))) / math.pi) / 2.0 * n

        if quantization:
            xtile = int(xtile)
            ytile = int(ytile)

        return xtile, ytile


    def tile2latlon(self, xtile, ytile):
        n = 2.0 ** self.zoom
        lon_deg = xtile / n * 360.0 - 180.0
        lat_rad = math.atan(math.sinh(math.pi * (1 - 2 * ytile / n)))
        lat_deg = math.degrees(lat_rad)
        return lat_deg, lon_deg


    def latlon2pixel(self, lat, lon):
        coords = self.latlon2tile(lat, lon)

        x = (coords[0] - self.xmin) * 256
        y = (coords[1] - self.ymin) * 256

        return x, y


    def distance_in_latlon(self, lat_dist, lon_dist): # in metres

        # taken from: https://en.wikipedia.org/wiki/Lat-lon#Expressing_latitude_and_longitude_as_linear_units

        len_lat = 111132.934 - 559.822 * math.cos(2*self.lat) + 1.175 * math.cos(4*self.lat)
        len_lon = 111412.84 * math.cos(self.lat) - 93.5 * math.cos(3*self.lat) + 0.118 * math.cos(5*self.lat)

        return lat_dist / len_lat, lon_dist / len_lon


    def get_image(self, tiles_x, tiles_y):
        smurl = r"http://a.tile.openstreetmap.org/{0}/{1}/{2}.png"
        imgfname = "{}_{}_{}.png"

        cluster = Image.new("RGB", (len(tiles_x)*256, len(tiles_y)*256))
        for x in range(0, len(tiles_x)):
            for y in range(0, len(tiles_y)):
                try:
                    fname = imgfname.format(self.zoom, tiles_x[x], tiles_y[y])
                    tile = None

                    if os.path.exists(fname):
                        tile = Image.open(fname)
                    else:
                        imgurl = smurl.format(self.zoom, tiles_x[x], tiles_y[y])
                        print("loading: " + imgurl)
                        imgstr = urllib2.urlopen(imgurl).read()
                        tile = Image.open(StringIO.StringIO(imgstr))
                        tile.save(fname)

                    cluster.paste(tile, box=(x * 256, y * 256))
                except Exception as e:
                    print("Couldn't download image")
                    tile = None
                    raise Exception("Couldn't download image", e)

        return cluster


    def plot_point(self, lats, lons):
        xs = []
        ys = []

        for i in range(0, len(lats)):
            x, y = self.latlon2pixel(lats[i], lons[i])
            xs.append(self.cropoffset_x + x)
            ys.append(self.cropoffset_y + y)

        # print(x, y)
        plt.scatter(xs, ys, marker="+", color=(1, 0, 0))


    def plot_rectangle(self, image):
        draw = ImageDraw.Draw(image)

        nw = self.latlon2pixel(self.area[0], self.area[1])
        se = self.latlon2pixel(self.area[2], self.area[3])

        line_thickness = 4

        for w in range(0, line_thickness):
            draw.rectangle([nw[0]+w, nw[1]+w, se[0]-w, se[1]-w], outline=(0, 0, 0))

        del draw


    def plot_map(self, points_x, points_y):

        delta_lat, delta_lon = self.distance_in_latlon(self.viewport_height, self.viewport_length)

        # only true for northern hemisphere, east of Greenwich

        self.area = (   
            self.lat + delta_lat / 2.0, 
            self.lon - delta_lon / 2.0,
            self.lat - delta_lat / 2.0, 
            self.lon + delta_lon / 2.0
        )

        print(self.area)

        self.xmin, self.ymin = self.latlon2tile(self.area[0], self.area[1], quantization=True)
        self.xmax, self.ymax = self.latlon2tile(self.area[2], self.area[3], quantization=True)

        tiles_x = [x for x in range(self.xmin, self.xmax+1)]
        tiles_y = [y for y in range(self.ymin, self.ymax+1)]

        print(self.xmin, self.xmax)
        print(self.ymin, self.ymax)
        print("")
        print(self.lat, self.lon + delta_lon)

        a = self.get_image(tiles_x, tiles_y)
        self.plot_rectangle(a)
        fig = plt.figure()
        fig.patch.set_facecolor('white')
        plt.imshow(np.asarray(a))  # , extent=[lon-delta_lon/2, lon+delta_lon/2, lat-delta_lat/2, lat+delta_lat/2])
        self.plot_point(points_x, points_y)
        plt.show()



if __name__ == '__main__':

    lat = 50.971403
    lon = 11.037798

    # 77.992634, 16.045922

    h = np.loadtxt("h_matrix.txt")
    boxes = pickle.load(open("boxes.pickle", "rb"))

    print(len(boxes))

    map = OSM(lat, lon, 200, 200)
    map.plot_map([], [])