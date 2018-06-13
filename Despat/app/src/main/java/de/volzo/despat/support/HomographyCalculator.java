package de.volzo.despat.support;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.Position;

public class HomographyCalculator {

    private static final String TAG = HomographyCalculator.class.getSimpleName();

    Mat h;

    public HomographyCalculator() {
        System.loadLibrary("opencv_java3");
    }

    public void test() {
        try {
            List<HomographyPoint> points = new ArrayList<>();

            points.add(new HomographyPoint(1124.0, 1416.0, 50.971296, 11.037630));
            points.add(new HomographyPoint(1773.0, 2470.0, 50.971173, 11.037914));
            points.add(new HomographyPoint(3785.0, 1267.0, 50.971456, 11.037915));
            points.add(new HomographyPoint(3416.0, 928.0, 50.971705, 11.037711));
            points.add(new HomographyPoint(2856.0, 1303.0, 50.971402, 11.037796));
            points.add(new HomographyPoint(2452.0, 916.0, 50.971636, 11.037486));

            loadPoints(points);

            ArrayList<Position> positions = new ArrayList<>();
            Position pos = new Position();
            pos.setX(1000f);
            pos.setY(1000f);
            positions.add(pos);

            convertPoints(positions);

        } catch (Exception e) {
            Log.e(TAG, "fail", e);
        }
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

        Log.d(TAG, h.dump());
    }

    public void convertPoints(List<Position> positions) throws Exception {
        if (h == null) {
            throw new Exception("transformation matrix H is missing");
        }

        List<Point> imagePoints = new ArrayList<>();

        for (Position p : positions) {
            imagePoints.add(new Point(p.getX(), p.getY()));
        }

        MatOfPoint2f src = new MatOfPoint2f();
        MatOfPoint2f dst = new MatOfPoint2f();

        src.fromList(imagePoints);

        Core.perspectiveTransform(src, dst, this.h);
    }
}
