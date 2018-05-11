import matplotlib.pyplot as plt
import numpy as np

import math
from PIL import Image, ImageDraw

import requests
from io import BytesIO

import sys
import os

import cv2
import pickle

ZOOM_LEVEL = 19 # max zoom level is 19

# OSM
TILE_URL = "http://a.tile.openstreetmap.org/{0}/{1}/{2}.png"
TILE_DIR = "tiles/osm_mapnik/{}/{}/"

# GMAPS satellite
# TILE_URL = "http://mt0.google.com/vt/lyrs=" + "s" + "@132&hl=de&x={1}&y={2}&z={0}"
# TILE_DIR = "tiles/gmaps_satellite/{}/{}/"

# GMAPS standard roadmap
# TILE_URL = "http://mt0.google.com/vt/lyrs=" + "m" + "@132&hl=de&x={1}&y={2}&z={0}"
# TILE_DIR = "tiles/gmaps_roadmap/{}/{}/"

# GMAPS standard hybrid
# TILE_URL = "http://mt0.google.com/vt/lyrs=" + "y" + "@132&hl=de&x={1}&y={2}&z={0}"
# TILE_DIR = "tiles/gmaps_hybrid/{}/{}/"

TILE_SAVE_PATH = os.path.join(TILE_DIR, "{}.png")


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


    def get_image(self, tiles_x, tiles_y, z):
        smurl = TILE_URL
        imgfname = TILE_SAVE_PATH

        for x in range(0, len(tiles_x)):
            for y in range(0, len(tiles_y)):
                
                fname = imgfname.format(z, tiles_x[x], tiles_y[y])
                tile = None

                if not os.path.exists(fname):
                    imgurl = smurl.format(z, tiles_x[x], tiles_y[y])
                    print("loading: " + imgurl)
        
                    response = requests.get(imgurl)
                    tile = Image.open(BytesIO(response.content))

                    try:
                        os.makedirs(TILE_DIR.format(z, tiles_x[x]))
                    except FileExistsError:
                        pass

                    tile.save(fname)


    def load(self, z):

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

        a = self.get_image(tiles_x, tiles_y, z)


if __name__ == '__main__':

    lat = 50.971403
    lon = 11.037798

    for z in range(14, 19+1):
        map = OSM(lat, lon, 2000, 2000, z)
        map.load(z)