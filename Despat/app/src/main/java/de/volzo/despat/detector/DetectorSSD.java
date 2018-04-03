package de.volzo.despat.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DetectorSSD extends Detector {

    public static final String TAG = DetectorSSD.class.getSimpleName();

    private Context context;

    private TFLiteDetector tfLiteDetector;

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "mobilenet_ssd.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;





    private long lastProcessingTimeMs;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;


    public DetectorSSD(Context context) {
        this.context = context;
    }

    @Override
    public void init() throws Exception {

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            tfLiteDetector = TFLiteObjectDetectionAPIModel.create(
                    context.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE
            );
        } catch (final IOException e) {
            Log.e(TAG, "Exception initializing classifier!", e);
            throw e;
        }

        int sourceImageWidth = 800;
        int sourceImageHeight = 600;

        rgbFrameBitmap = Bitmap.createBitmap(sourceImageWidth, sourceImageHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

//        frameToCropTransform =
//                ImageUtils.getTransformationMatrix(
//                        previewWidth, previewHeight,
//                        cropSize, cropSize,
//                        sensorOrientation, MAINTAIN_ASPECT);

    }

    public void load(File fullFilename) {
        Bitmap bitmap = BitmapFactory.decodeFile(fullFilename.getAbsolutePath());
        croppedBitmap = bitmap;
    }

    public void run() {
//        Log.i(TAG, "Running detection on image " + currTimestamp);


        final long startTime = SystemClock.uptimeMillis();
        final List<TFLiteDetector.Recognition> results = tfLiteDetector.recognizeImage(croppedBitmap);
        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        final List<TFLiteDetector.Recognition> mappedRecognitions = new LinkedList<TFLiteDetector.Recognition>();

        for (final TFLiteDetector.Recognition result : results) {

            Log.d(TAG, "detection: " + result.getLocation());

            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= minimumConfidence) {
//                canvas.drawRect(location, paint);
//
//                cropToFrameTransform.mapRect(location);
//                result.setLocation(location);
//                mappedRecognitions.add(result);
            }
        }

//        tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
//        trackingOverlay.postInvalidate();

//        requestRender();
//        computingDetection = false;
    }

}
