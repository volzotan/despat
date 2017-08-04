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

    private long[] computationTime = new long[3];

    public Recognizer() {
        System.loadLibrary("opencv_java3");
    }

    public RecognizerResultset run(File image) {

        long timestart = System.currentTimeMillis();

        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        computationTime[0] = System.currentTimeMillis() - timestart; // init descriptor

        Mat cvimage = Imgcodecs.imread(image.getAbsolutePath());
        Mat cvimage2 = cvimage.clone();
        MatOfRect foundLocations = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();

        computationTime[1] = System.currentTimeMillis() - timestart; // read image

//        hog.detectMultiScale(cvimage, foundLocations, foundWeights,
//                1,
//                new Size(4, 4),
//                new Size(8, 8),
//                1.03, 1, false);

        hog.detectMultiScale(cvimage, foundLocations, foundWeights);

        Rect[] rects = foundLocations.toArray();

        computationTime[2] = System.currentTimeMillis() - timestart; // computation

        for (Rect rect : rects) {
            Imgproc.rectangle(cvimage2, new Point(rect.x, rect.y),
                    new Point(rect.x+rect.width, rect.y+rect.height),
                    new Scalar(0, 0, 255), 2);
        }

        System.out.println("hits: " + rects.length);

        double[][] coordinates = new double[rects.length][2];
        for (int i=0; i<rects.length; i++) {
            Rect rect = rects[i];

            coordinates[i][0] = rect.x + (rect.width / 2);
            coordinates[i][1] = rect.y + (rect.height / 10);
        }

        computationTime[3] = System.currentTimeMillis() - timestart; // drawing boxes

        RecognizerResultset recognizerResultset = new RecognizerResultset();
        recognizerResultset.setCoordinates(coordinates);
        recognizerResultset.setBitmap(cvimage2);
        recognizerResultset.setComputationTime(computationTime);

        float timestop = (System.currentTimeMillis() - timestart) / 1000f;
        System.out.println("runtime: " + Math.round(timestop) + "s");

        return recognizerResultset;

        // Imgcodecs.imwrite("result.jpg", cvimage2);
    }

    class RecognizerResultset {

        public double[][] coordinates;
        public Bitmap bitmap;
        public long[] computationTime;

        public RecognizerResultset() {}

        void setCoordinates(double[][] coordinates) {
            this.coordinates = coordinates;
        }

        void setBitmap(Mat image) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
            Bitmap resultBitmap = Bitmap.createBitmap(image.width(), image.height(), conf);
            Utils.matToBitmap(image, resultBitmap);
            this.bitmap = resultBitmap;
        }

        void setComputationTime(long[] computationTime) {
            this.computationTime = computationTime;
        }

    }
}