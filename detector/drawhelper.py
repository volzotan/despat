from PIL import Image, ImageDraw

class Drawhelper(object):

    box_list = []

    def __init__(self, input_filename, output_filename):
        self.input_filename = input_filename
        self.output_filename = output_filename

    def add_boxes(self, boxes, color, strokewidth=1, inverse_coordinates=False):
        boxset = {}

        if inverse_coordinates:
            inv_boxes = []
            for box in boxes:
                inv_boxes.append([box[1], box[0], box[3], box[2]])
            boxset["boxes"] = inv_boxes
        else:
            boxset["boxes"] = boxes

        boxset["color"] = color
        boxset["strokewidth"] = strokewidth

        self.box_list.append(boxset)

    def draw(self):
        image = Image.open(self.input_filename)
        draw = ImageDraw.Draw(image)

        for boxset in self.box_list:
            b = boxset["boxes"]

            for i in range(0, len(b)):
                for s in range(0, boxset["strokewidth"]):
                    draw.rectangle([b[i][0] + s, b[i][1] + s, b[i][2] - s, b[i][3] - s], outline=boxset["color"])

        del draw
        image.save(self.output_filename)