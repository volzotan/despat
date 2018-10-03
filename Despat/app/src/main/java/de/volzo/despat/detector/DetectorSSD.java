package de.volzo.despat.detector;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Debug;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Benchmark;
import de.volzo.despat.persistence.BenchmarkDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.userinterface.DrawSurface;
import de.volzo.despat.support.Stopwatch;

public class DetectorSSD extends Detector {

    private static final String TAG = DetectorSSD.class.getSimpleName();

    private Context context;

    private DetectorConfig detectorConfig;

    private TensorFlowInterface tfInterface;
    private Stopwatch stopwatch;

    private int TILESIZE_INPUT = 800;
    private int TILESIZE_OUTPUT = 300;
    private String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1.pb";

//    private static final int TILESIZE_INPUT = 1000;
//    private static final int TILESIZE_OUTPUT = 1000;
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frcnn_inception_v2.pb";
//
//    private static final int TILESIZE_INPUT = 1280;
//    private static final int TILESIZE_OUTPUT = 640;
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_fpn.pb";

//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_ppn.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v2.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssdlite_mobilenet_v2.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_resnet50_fpn.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_inception_v2.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frcnn_resnet50.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frcnn_resnet101.pb";
//    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frcnn_nas.pb";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

    private TileManager tileManager = null;

    public DetectorSSD(Context context) {
        this.context = context;
    }

    @Override
    public void init(DetectorConfig detectorConfig) throws Exception {

        this.detectorConfig = detectorConfig;
        TILESIZE_INPUT = detectorConfig.getTilesize();

        switch (this.detectorConfig.getDetector()) {
            case "low": {
                TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1.pb";
                TILESIZE_OUTPUT = 300;
                break;
            }
            case "mid": {
                TF_OD_API_MODEL_FILE = "file:///android_asset/frcnn_inception_v2.pb";
                TILESIZE_OUTPUT = TILESIZE_INPUT;
                break;
            }
            case "high": {
                TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_fpn.pb";
                TILESIZE_OUTPUT = 640;
                break;
            }
            default: {
                throw new Exception("undefined detector selected");
            }
        }

//        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo info : processes) {
//            System.out.println(info.pid + " " + info.processName + " " + info.describeContents());
//        }
//        Debug.MemoryInfo[] meminfo = activityManager.getProcessMemoryInfo(new int[]{processes.get(0).pid});
//        for (Debug.MemoryInfo info : meminfo) {
//            Map<String, String> map = info.getMemoryStats();
//            for (String key : map.keySet()) {
//                Log.d(TAG, key + " : " + map.get(key));
//            }
//        }

        try {
            System.loadLibrary("tensorflow_demo");
            Log.d(TAG, "libtensorflow_demo.so found");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "libtensorflow_demo.so not found");
            throw new Exception("libtensorflow_demo.so missing");
        }

        stopwatch = new Stopwatch();

        try {
            tfInterface = TensorFlowInterface.create(
                    context.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TILESIZE_OUTPUT
            );
        } catch (final IOException e) {
            Log.e(TAG, "Exception initializing classifier!", e);
            throw e;
        }

        Log.d(TAG, String.format("Detector init. Model: %s @ %d", detectorConfig.getDetector(), detectorConfig.getTilesize()));

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
        stopwatch.reset();
        stopwatch.start("tileManager init");
        tileManager = new TileManager(fullFilename, TILESIZE_INPUT, TILESIZE_OUTPUT);
        stopwatch.stop("tileManager init");
    }

    @Override
    public List<Detector.Recognition> run() {
//        Log.i(TAG, "Running detection on image " + currTimestamp);

        List<Detector.Recognition> results;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        BenchmarkDao benchmarkDao = db.benchmarkDao();
        long fullImageTimer = System.currentTimeMillis();

        for (TileManager.Tile tile : tileManager.getAllTiles()){
            stopwatch.start("totalInference");
            Bitmap crop = tileManager.getTileImage(tile);
            results = tfInterface.recognizeImage(crop);
            crop.recycle();
            double totalInferenceTime = stopwatch.stop("totalInference");

            Benchmark benchmark = new Benchmark();
//            benchmark.setSessionId(session.getId());
            benchmark.setDetector(this.detectorConfig.getDetector());
            benchmark.setTimestamp(Calendar.getInstance().getTime());
            benchmark.setType(Benchmark.TYPE_TILE);
            benchmark.setInferenceTime(totalInferenceTime);
            benchmarkDao.insert(benchmark);

            // tile.setResults(results); EVIL
            tileManager.passResult(tile, results);
            System.out.println("tile done: " + tile);
        }

        Benchmark benchmark = new Benchmark();
        benchmark.setDetector(this.detectorConfig.getDetector());
        benchmark.setTimestamp(Calendar.getInstance().getTime());
        benchmark.setType(Benchmark.TYPE_IMAGE);
        benchmark.setInferenceTime(System.currentTimeMillis() - fullImageTimer);
        benchmarkDao.insert(benchmark);

//        ImageView imageView = (ImageView) ((Activity) context).getWindow().getDecorView().findViewById(R.id.imageView);
//        imageView.setImageBitmap(tileManager.getTileImage(tileManager.getAllTiles().get(12)));

        stopwatch.print();

        List<Detector.Recognition> completeResults = tileManager.getFullResults();
        tileManager.close();
        return completeResults;

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
    public void save() throws Exception {

    }

    @Override
    public void display(DrawSurface surface, final Size imageSize, List<RectF> rectangles, final DetectorConfig detectorConfig) {
        final List<RectF> rects = rectangles;
        surface.setCallback(new DrawSurface.DrawSurfaceCallback() {
            @Override
            public void onSurfaceReady(DrawSurface surface) {
                try {
                    surface.clearCanvas();

                    if (detectorConfig != null) {
                        if (tileManager == null) tileManager = new TileManager(imageSize, detectorConfig.getTilesize());
                        surface.addBoxes(imageSize, tileManager.getTileBoxes(), surface.paintBlack);
                    }
                    surface.addBoxes(imageSize, rects, surface.paintGreen);
                } catch (Exception e) {
                    Log.e(TAG, "displaying results failed. unable to draw on canvas", e);
                }
            }
        });
    }
}
