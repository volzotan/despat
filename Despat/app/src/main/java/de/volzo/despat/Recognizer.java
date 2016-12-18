package de.volzo.despat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;

/**
 * Created by volzotan on 17.12.16.
 */

public class Recognizer {

    public Recognizer() {
        System.loadLibrary("opencv_java3");
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

        // showim(cvimage2);
        // Imgcodecs.imwrite("result.jpg", cvimage2);

        double[][] coordinates = new double[rects.length][2];
        for (int i=0; i<rects.length; i++) {
            Rect rect = rects[i];

            coordinates[i][0] = rect.x + (rect.width / 2);
            coordinates[i][1] = rect.y + (rect.height / 10);
        }

        RecognizerResultset recognizerResultset = new RecognizerResultset();
        recognizerResultset.coordinates = coordinates;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap resultBitmap = Bitmap.createBitmap(cvimage2.width(), cvimage2.height(), conf);
        Utils.matToBitmap(cvimage2, resultBitmap);
        recognizerResultset.bitmap = resultBitmap;

        return recognizerResultset;
    }

    class RecognizerResultset {

        public double[][] coordinates;
        public Bitmap bitmap;

        public RecognizerResultset() {}
    }
}