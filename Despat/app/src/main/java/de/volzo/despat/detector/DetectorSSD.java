package de.volzo.despat.detector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.volzo.despat.DrawSurface;
import de.volzo.despat.R;
import de.volzo.despat.support.Stopwatch;

public class DetectorSSD extends Detector {

    public static final String TAG = DetectorSSD.class.getSimpleName();

    private Context context;

    private TensorFlowDetector tfDetector;
    private Stopwatch stopwatch;

    private static final int TF_OD_API_INPUT_SIZE = 300;

    //private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/faster_rcnn_inception_v2.pb";
    //private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v2.pb";
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_android_demo.pb";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    private TileManager tileManager = null;

    public DetectorSSD(Context context) {
        this.context = context;
    }

    @Override
    public void init() throws Exception {

        try {
            System.loadLibrary("tensorflow_demo");
            Log.d(TAG, "libtensorflow_demo.so found");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "libtensorflow_demo.so not found");
            throw new Exception("libtensorflow_demo.so missing");
        }

        stopwatch = new Stopwatch();

        try {
            tfDetector = TensorFlowObjectDetectionAPIModel.create(
                    context.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE
            );
        } catch (final IOException e) {
            Log.e(TAG, "Exception initializing classifier!", e);
            throw e;
        }

//        rgbFrameBitmap = Bitmap.createBitmap(sourceImageWidth, sourceImageHeight, Bitmap.Config.ARGB_8888);
//        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

//        frameToCropTransform =
//                ImageUtils.getTransformationMatrix(
//                        previewWidth, previewHeight,
//                        cropSize, cropSize,
//                        sensorOrientation, MAINTAIN_ASPECT);

    }

    @Override
    public void load(File fullFilename) {
//        Bitmap bitmap = BitmapFactory.decodeFile(fullFilename.getAbsolutePath());
//        croppedBitmap = bitmap;

        stopwatch.start("tileManager init");
        tileManager = new TileManager(fullFilename);
        stopwatch.stop("tileManager init");
    }

    @Override
    public void run() {
//        Log.i(TAG, "Running detection on image " + currTimestamp);

        List<TensorFlowDetector.Recognition> results;

        for (TileManager.Tile tile : tileManager.getAllTiles()){
            stopwatch.start("totalInference");
            Bitmap crop = tileManager.getTileImage(tile);
            results = tfDetector.recognizeImage(crop);
            crop.recycle();
            stopwatch.stop("totalInference");

            // tile.setResults(results); EVIL
            tileManager.passResult(tile, results);
        }

//        ImageView imageView = (ImageView) ((Activity) context).getWindow().getDecorView().findViewById(R.id.imageView);
//        imageView.setImageBitmap(tileManager.getTileImage(tileManager.getAllTiles().get(12)));

        stopwatch.print();


//        for (final TensorFlowDetector.Recognition result : results) {
//
//            Log.d(TAG, "detection: " + result.getLocation());
//
//            final RectF location = result.getLocation();
//            if (location != null && result.getConfidence() >= minimumConfidence) {
////                canvas.drawRect(location, paint);
////
////                cropToFrameTransform.mapRect(location);
////                result.setLocation(location);
////                mappedRecognitions.add(result);
//            }
//        }

//        tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
//        trackingOverlay.postInvalidate();

//        requestRender();
//        computingDetection = false;
    }

    @Override
    public void display(DrawSurface surface) {
        List<TensorFlowDetector.Recognition> results = tileManager.getFullResults();

        List<RectF> rectangles = new ArrayList<RectF>();
        for (TensorFlowDetector.Recognition result : results) {
            if (result.getConfidence() < MINIMUM_CONFIDENCE_TF_OD_API) {
                continue;
            }

            rectangles.add(result.getLocation());
        }

        try {
            surface.drawBoxes(tileManager.getImageSize(), rectangles, false);
        } catch (Exception e) {
            Log.e(TAG, "displaying results failed. unable to draw on canvas", e);
        }
    }

}
