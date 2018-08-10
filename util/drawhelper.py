
from PIL import Image, ImageDraw

class Drawhelper(object):

    def __init__(self, input_filename, output_filename):
        self.input_filename = input_filename
        self.output_filename = output_filename
        self.box_list = []


    def add_boxes(self, boxes, color, strokewidth=1, inverse_coordinates=False):
        boxset = []

        if inverse_coordinates:
            for box in boxes:
                boxset.append([box[1], box[0], box[3], box[2]])
        else:
            boxset = boxes

        for box in boxset:
            self.box_list.append((box, color, strokewidth))


    def draw(self, draw_on_empty_canvas=False):
        image = Image.open(self.input_filename)

        if draw_on_empty_canvas:
            image = Image.new("RGBA", image.size, (0,0,0,0))

        draw = ImageDraw.Draw(image, "RGBA")

        self.box_list = sorted(self.box_list, key=lambda box: box[1])

        for box in self.box_list:
            b = box[0]
            color = box[1]
            strokewidth = box[2]
            for s in range(0, strokewidth):
                draw.rectangle([b[0] + s, b[1] + s, b[2] - s, b[3] - s], outline=color)

        del draw
        image.save(self.output_filename)