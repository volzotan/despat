import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class Recognizer {

    public Recognizer() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public RecognizerResultset run(File image) {

        long timestart = System.currentTimeMillis();

        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        Mat cvimage = Imgcodecs.imread(image.getAbsolutePath());
        Mat cvimage2 = cvimage.clone();
        MatOfRect foundLocations = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();
//        hog.detectMultiScale(cvimage, foundLocations, foundWeights,
//                1,
//                new Size(4, 4),
//                new Size(8, 8),
//                1.03, 1, false);

        hog.detectMultiScale(cvimage, foundLocations, foundWeights);

        Rect[] rects = foundLocations.toArray();

        for (Rect rect : rects) {
            Imgproc.rectangle(cvimage2, new Point(rect.x, rect.y),
                    new Point(rect.x+rect.width, rect.y+rect.height),
                    new Scalar(0, 0, 255), 2);
        }

        System.out.println("hits: " + rects.length);

        float timestop = (System.currentTimeMillis() - timestart) / 1000f;
        System.out.println("runtime: " + Math.round(timestop) + "s");

        double[][] coordinates = new double[rects.length][2];
        for (int i=0; i<rects.length; i++) {
            Rect rect = rects[i];

            coordinates[i][0] = rect.x + (rect.width / 2);
            coordinates[i][1] = rect.y + (rect.height / 10);
        }

        RecognizerResultset recognizerResultset = new RecognizerResultset();
        recognizerResultset.setCoordinates(coordinates);
        recognizerResultset.setBitmap(cvimage2);

        return recognizerResultset;
    }

    public static void showim(Mat img) {
        Imgproc.resize(img, img, new Size(640, 480));
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class RecognizerResultset {

        public double[][] coordinates;
        public Mat matrix;

        void setCoordinates(double[][] coordinates) {
            this.coordinates = coordinates;
        }

        void setBitmap(Mat image) {
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

            this.matrix = image;
        }

        public RecognizerResultset() {}
    }
}
