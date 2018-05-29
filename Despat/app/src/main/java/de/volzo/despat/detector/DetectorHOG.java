package de.volzo.despat.detector;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.userinterface.DrawSurface;
import de.volzo.despat.support.Stopwatch;

public class DetectorHOG extends Detector {

    public static final String TAG = DetectorHOG.class.getSimpleName();

    Context context;
    Stopwatch stopwatch;

    HOGDescriptor hog;
    File image;
    Size imageSize;

    public DetectorHOG(Context context) {
        this.context = context;
    }

    @Override
    public void init() throws Exception {

        stopwatch = new Stopwatch();
        stopwatch.start("HOG init");

        System.loadLibrary("opencv_java3");
        hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        stopwatch.stop("HOG init");

    }

    @Override
    public void load(File fullFilename) {
        this.image = fullFilename;
    }

    @Override
    public List<Recognition> run() throws Exception {

        stopwatch.start("totalInference");
        stopwatch.start("preparation");

        Mat cvimage = Imgcodecs.imread(image.getAbsolutePath());
        MatOfRect foundLocations = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();

        stopwatch.stop("preparation");
        stopwatch.start("detection");

        hog.detectMultiScale(cvimage, foundLocations, foundWeights);

        stopwatch.stop("detection");
        stopwatch.start("conversion");

        Rect[] rects = foundLocations.toArray();
        List<Recognition> results = new ArrayList<Recognition>();

        for (int i=0; i<rects.length; i++) {
            Rect rect = rects[i];

            Recognition rec = new Recognition(
                    "",
                    "person",
                    (float) foundWeights.get(i, 0)[0],
                    new RectF(
                            (float) rect.x,
                            (float) rect.y,
                            (float) rect.x + rect.width,
                            (float) rect.y + rect.height
                    ));

            System.out.println(rec.toString());

            results.add(rec);
        }

        stopwatch.stop("conversion");
        stopwatch.stop("totalInference");

        imageSize = new Size(cvimage.width(), cvimage.height());

        stopwatch.print();
        System.out.println("hits: " + results.size());

        return results;
    }


    @Override
    public void save() throws Exception {

    }

    @Override
    public void display(DrawSurface surface, List<Detector.Recognition> results) {
        List<RectF> rectangles = new ArrayList<RectF>();
        for (Detector.Recognition result : results) {
            rectangles.add(result.getLocation());
        }

        try {
            surface.clearCanvas();
            surface.addBoxes(imageSize, rectangles, surface.paintGreen);
        } catch (Exception e) {
            Log.e(TAG, "displaying results failed. unable to draw on canvas", e);
        }
    }

}