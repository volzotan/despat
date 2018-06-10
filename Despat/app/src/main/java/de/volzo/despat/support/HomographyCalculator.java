package de.volzo.despat.support;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.persistence.HomographyPoint;

public class HomographyCalculator {

    private static final String TAG = HomographyCalculator.class.getSimpleName();

    Mat h;

    public HomographyCalculator() {
        System.loadLibrary("opencv_java3");
    }

    public void loadPoints(List<HomographyPoint> points) {

        List<Point> imagePoints = new ArrayList<>();
        List<Point> mapPoints = new ArrayList<>();

        for (HomographyPoint p : points) {
            imagePoints.add(new Point(p.getX(), p.getY()));
            mapPoints.add(new Point(p.getLatitude(), p.getLongitude()));
        }

        MatOfPoint2f src = new MatOfPoint2f();
        MatOfPoint2f dst = new MatOfPoint2f();

        src.fromList(imagePoints);
        dst.fromList(mapPoints);

        h = Calib3d.findHomography(src, dst);

        Log.wtf(TAG, h.dump());
    }
}
