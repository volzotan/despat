import cv2
import numpy as np

import argparse
import sys

import pickle

counter         = 0

def draw_text(image, x, y, text):

    font = cv2.FONT_HERSHEY_SIMPLEX
    scale = 0.5
    thickness = 0

    padding = 3

    text_size, baseline = cv2.getTextSize(text, font, scale, thickness);
    cv2.rectangle(image, (x, y), (x+text_size[0]+padding*2, y-text_size[1]-baseline-padding), (0, 0, 0), thickness=-1);
    cv2.putText(image, text, (x+padding, y-padding), font, scale, (255, 255, 255));


def click(event, x, y, flags, param):

    global image, scaling_factor, counter

    if event == cv2.EVENT_LBUTTONDOWN:
        # print("down")

        factor = 1 / float(scaling_factor)

        unscaled_x = int(x * factor)
        unscaled_y = int(y * factor)

        text = "{}: {}, {}".format(counter, unscaled_x, unscaled_y)
        draw_text(image, x, y, text)
        print(text)

        cv2.circle(image, (x, y), 8, (0, 0, 0))
        cv2.circle(image, (x, y), 6, (255, 255, 255))

        counter += 1
 
    elif event == cv2.EVENT_LBUTTONUP:
        # print("up")
        pass


def calculate_homography(points_src, points_dst):

    pts_src = np.array(points_src, dtype=np.float)
    pts_dst = np.array(points_dst, dtype=np.float)

    h, status = cv2.findHomography(pts_src, pts_dst)

    return h, status


if __name__ == "__main__":

    # global image, scaling_factor

    # ap = argparse.ArgumentParser()
    # ap.add_argument("-i", "--image", required=True, help="Path to the image")
    # args = vars(ap.parse_args())

    # image = cv2.imread(args["image"])
    # height, width = image.shape[:2]
    # scaling_factor = 1000 / float(width)
    # image = cv2.resize(image, None, fx=scaling_factor, fy=scaling_factor, interpolation=cv2.INTER_CUBIC)

    # cv2.namedWindow("image", cv2.WINDOW_NORMAL)
    # cv2.setMouseCallback("image", click)

    # while True:
    #     # display the image and wait for a keypress
    #     cv2.imshow("image", image)

    #     key = cv2.waitKey(1) & 0xFF
     
    #     if key == ord("s"):
    #         cv2.imwrite(args["image"] + "_output.jpg", image);
    #         break
     
    #     elif key == ord("c"):
    #         break
          
    # cv2.destroyAllWindows()
    # sys.exit(0)

    src = [
        [1124, 1416],
        [1773, 2470],
        [3785, 1267],
        [3416, 928],
        [2856, 1303],
        [2452, 916]
    ]

    dst = [
        [50.971296, 11.037630],
        [50.971173, 11.037914],
        [50.971456, 11.037915],
        [50.971705, 11.037711],
        [50.971402, 11.037796],
        [50.971636, 11.037486]
    ]

    # Calculate Homography
    h, status = calculate_homography(src, dst)

    print(h, status)

    boxes = pickle.load(open("boxes.pickle", "rb"))

    boxes_warped = []

    for b in boxes:
        # TODO:
        # float32 should be enough (6 decimal places)
        # 6th decimal place in lat/lon has a precision of 0.11m

        # beware: only a single point (the anchor) of the bounding box should be transformed (or the bbox will be grossly warped)
        x = b[0] #+(b[2]-b[0])/2.0
        y = b[3]

        values = np.array([[[x, y]]], dtype=np.float64)
        pointsOut = cv2.perspectiveTransform(values, h)
        boxes_warped.append([pointsOut[0][0][0], pointsOut[0][0][1]])

    pickle.dump(boxes_warped, open("box_anchors_warped.pickle", "wb"))

    with open("boxes_warped.txt", "a") as f:
        for box in boxes_warped:
            f.write("{} {}\n".format(box[0], box[1]))

    # np.save("h_matrix.npy", h) # evil python2 pickle
    # np.savetxt("h_matrix.txt", h)

