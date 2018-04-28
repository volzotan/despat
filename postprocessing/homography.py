import cv2
import numpy as np

import argparse
import sys

image = None
counter = 0

def draw_text(image, x, y, text):

    font = cv2.FONT_HERSHEY_SIMPLEX
    scale = 0.5
    thickness = 0

    padding = 3

    text_size, baseline = cv2.getTextSize(text, font, scale, thickness);
    cv2.rectangle(image, (x, y), (x+text_size[0]+padding*2, y-text_size[1]-baseline-padding), (0, 0, 0), thickness=-1);
    cv2.putText(image, text, (x+padding, y-padding), font, scale, (255, 255, 255));


def click(event, x, y, flags, param):

    global image, counter

    if event == cv2.EVENT_LBUTTONDOWN:
        print("down")

        text = "{}: {}, {}".format(counter, x, y)
        draw_text(image, x, y, text)
        print(text)

        cv2.circle(image, (x, y), 8, (0, 0, 0))
        cv2.circle(image, (x, y), 6, (255, 255, 255))

        counter += 1
 
    elif event == cv2.EVENT_LBUTTONUP:
        print("up")


if __name__ == "__main__":

    global image

    ap = argparse.ArgumentParser()
    ap.add_argument("-i", "--image", required=True, help="Path to the image")
    args = vars(ap.parse_args())

    cv2.namedWindow("image")
    cv2.setMouseCallback("image", click)

    image = cv2.imread(args["image"])

    while True:
        # display the image and wait for a keypress
        cv2.imshow("image", image)
        key = cv2.waitKey(1) & 0xFF
     
        # if the 'r' key is pressed, reset the cropping region
        if key == ord("r"):
            image = clone.copy()
     
        # if the 'c' key is pressed, break from the loop
        elif key == ord("c"):
            break
          
    cv2.destroyAllWindows()

    sys.exit(0)


im_src = cv2.imread('book2.jpg')
# Four corners of the book in source image
pts_src = np.array([[141, 131], [480, 159], [493, 630],[64, 601]])

# Read destination image.
im_dst = cv2.imread('book1.jpg')
# Four corners of the book in destination image.
pts_dst = np.array([[318, 256],[534, 372],[316, 670],[73, 473]])

pts_src = pts_src.astype(float)
pts_dst = pts_dst.astype(float)

# Calculate Homography
h, status = cv2.findHomography(pts_src, pts_dst)
 
# Warp source image to destination based on homography
im_out = cv2.warpPerspective(im_src, h, (im_dst.shape[1],im_dst.shape[0]))
 
# Display images
cv2.imshow("Source Image", im_src)
cv2.imshow("Destination Image", im_dst)
cv2.imshow("Warped Source Image", im_out)

cv2.waitKey(0)