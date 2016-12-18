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
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        long timestart = System.currentTimeMillis();

        File image = new File("pedestriancrossing3.jpg");

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

        // showim(cvimage2);
        Imgcodecs.imwrite("result.jpg", cvimage2);
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



}
