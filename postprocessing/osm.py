import matplotlib.pyplot as plt
import numpy as np

import math
import urllib2
import StringIO
from PIL import Image

import sys

def deg2num(lat_deg, lon_deg, zoom):
    lat_rad = math.radians(lat_deg)
    n = 2.0 ** zoom
    xtile = int((lon_deg + 180.0) / 360.0 * n)
    ytile = int((1.0 - math.log(math.tan(lat_rad) + (1 / math.cos(lat_rad))) / math.pi) / 2.0 * n)
    return (xtile, ytile)

def num2deg(xtile, ytile, zoom):
    n = 2.0 ** zoom
    lon_deg = xtile / n * 360.0 - 180.0
    lat_rad = math.atan(math.sinh(math.pi * (1 - 2 * ytile / n)))
    lat_deg = math.degrees(lat_rad)
    return (lat_deg, lon_deg)


def coord_diff(lat, lon, lat_dist, lon_dist):

    # taken from: https://en.wikipedia.org/wiki/Lat-lon#Expressing_latitude_and_longitude_as_linear_units

    len_lat = 111132.92 - 559.82 * math.cos(2*lat) + 1.175 * math.cos(4*lat) + 0.0023 * math.cos(6*lat)
    len_lon = 111412.84 * math.cos(lat) - 93.5 * math.cos(3*lat) + 0.118 * math.cos(5*lat)

    return (lat_dist / len_lat, lon_dist / len_lon)



def get_image(lat_deg, lon_deg, delta_lat, delta_lon, zoom):
    smurl = r"http://a.tile.openstreetmap.org/{0}/{1}/{2}.png"

    xmin, ymax = deg2num(lat_deg - delta_lat/2, lon_deg - delta_lon/2, zoom)
    xmax, ymin = deg2num(lat_deg + delta_lat/2, lon_deg + delta_lon/2, zoom)

    cluster = Image.new('RGB',((xmax-xmin+1)*256-1,(ymax-ymin+1)*256-1) ) 
    for xtile in range(xmin, xmax+1):
        for ytile in range(ymin, ymax+1):
            try:
                imgurl=smurl.format(zoom, xtile, ytile)
                print("Opening: " + imgurl)
                imgstr = urllib2.urlopen(imgurl).read()
                tile = Image.open(StringIO.StringIO(imgstr))
                cluster.paste(tile, box=((xtile-xmin)*256 , (ytile-ymin)*255))
            except: 
                print("Couldn't download image")
                tile = None
                raise Exception("Couldn't download image")

    return cluster


if __name__ == '__main__':

    lat = 50.971403
    lon = 11.037798

    delta_lat, delta_lon = coord_diff(lat, lon, 100, 200)

    a = get_image(lat, lon, delta_lat, delta_lon, 19)
    fig = plt.figure()
    fig.patch.set_facecolor('white')
    plt.imshow(np.asarray(a)) #, extent=[lon-delta_lon/2, lon+delta_lon/2, lat-delta_lat/2, lat+delta_lat/2])
    plt.show()