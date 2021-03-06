package de.volzo.despat.detector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import de.volzo.despat.Despat;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Benchmark;
import de.volzo.despat.persistence.BenchmarkDao;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.support.Util;
import de.volzo.despat.userinterface.DrawSurface;
import de.volzo.despat.support.Stopwatch;

public class DetectorTensorFlowMobile extends Detector {

    private static final String TAG = DetectorTensorFlowMobile.class.getSimpleName();

    public static final String[] FIDELITY_MODE = {"low", "mid", "high"};

    private TensorFlowMobileInterface tfInterface;
    private Stopwatch stopwatch;

    private int TILESIZE_INPUT = 800;
    private int TILESIZE_OUTPUT = 300;
    private String TF_OD_API_MODEL_FILE = "ssd_mobilenet_v1.pb";

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

    public DetectorTensorFlowMobile(Context context, DetectorConfig detectorConfig) throws Exception {
        super(context, detectorConfig);

        TILESIZE_INPUT = this.detectorConfig.getTilesize();

        switch (this.detectorConfig.getDetector()) {
            case "low": {
//                TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1.pb";
                TF_OD_API_MODEL_FILE = "ssd_mobilenet_v1.pb";
                TILESIZE_OUTPUT = 300;
                break;
            }
            case "mid": {
                TF_OD_API_MODEL_FILE = "frcnn_inception_v2.pb";
                TILESIZE_OUTPUT = TILESIZE_INPUT;
                break;
            }
            case "high": {
                TF_OD_API_MODEL_FILE = "ssd_mobilenet_v1_fpn.pb";
                TILESIZE_OUTPUT = 640;
                break;
            }
            default: {
                throw new Exception("undefined detector selected");
            }
        }
    }

    @Override
    public void init() throws Exception {

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
        Despat despat = Util.getDespat(context);
        try {
            tfInterface = TensorFlowMobileInterface.create(
                    context,
                    context.getAssets(),
                    despat.getStorageManager(),
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
    public void runBenchmark() {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        BenchmarkDao benchmarkDao = db.benchmarkDao();
        long fullImageTimer = System.currentTimeMillis();
        stopwatch.reset();
        stopwatch.start("tileManager init");
        Bitmap emptyBitmap = Bitmap.createBitmap(4000, 3000, Bitmap.Config.ARGB_8888);
        tileManager = new TileManager(emptyBitmap, TILESIZE_INPUT, TILESIZE_OUTPUT);
        stopwatch.stop("tileManager init");

        List<Detector.Recognition> results;
        List<TileManager.Tile> tiles = tileManager.getAllTiles().subList(0, 6);

        for (TileManager.Tile tile : tiles){
            stopwatch.start("totalInference");
            Bitmap crop = tileManager.getTileImage(tile);
            results = tfInterface.recognizeImage(crop);
            crop.recycle();
            double totalInferenceTime = stopwatch.stop("totalInference");

            Benchmark benchmark = new Benchmark();
            benchmark.setDetector(this.detectorConfig.getDetector());
            benchmark.setTilesize(this.detectorConfig.getTilesize());
            benchmark.setTimestamp(Calendar.getInstance().getTime());
            benchmark.setType(Benchmark.TYPE_TILE);
            benchmark.setInferenceTime(totalInferenceTime);
            benchmarkDao.insert(benchmark);

            // tile.setResults(results); EVIL
            tileManager.passResult(tile, results);
        }

        stopwatch.print();
        tileManager.close();
        emptyBitmap.recycle();

        Log.d(TAG, "Total Benchmark time: " + Long.toString(System.currentTimeMillis()-fullImageTimer));
    }

    public Long estimateComputationTime(Size imageSize) {
        AppDatabase database = AppDatabase.getAppDatabase(context);
        BenchmarkDao benchmarkDao = database.benchmarkDao();

        TileManager emptyTileManager = new TileManager(imageSize, TILESIZE_INPUT);
        List<Benchmark> benchmarks = benchmarkDao.getLast3ByDetectorOfType(detectorConfig.getDetector(), Benchmark.TYPE_TILE);

        if (benchmarks == null || benchmarks.size() == 0) {
            return null;
        }

        float avg = 0;
        for (Benchmark b : benchmarks) {
            avg += b.getInferenceTime();
        }

        avg = avg/benchmarks.size();

        return (long) avg * emptyTileManager.getAllTiles().size();
    }

    @Override
    public List<Detector.Recognition> run() {

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
            benchmark.setTilesize(this.detectorConfig.getTilesize());
            benchmark.setTimestamp(Calendar.getInstance().getTime());
            benchmark.setType(Benchmark.TYPE_TILE);
            benchmark.setInferenceTime(totalInferenceTime);
            benchmarkDao.insert(benchmark);

            // tile.setResults(results); EVIL
            tileManager.passResult(tile, results);
            Log.d(TAG, "tile done: " + tile);
        }

        Benchmark benchmark = new Benchmark();
        benchmark.setDetector(this.detectorConfig.getDetector());
        benchmark.setTilesize(this.detectorConfig.getTilesize());
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
                    surface.addBoxes(imageSize, rects, surface.paintMain);
                } catch (Exception e) {
                    Log.e(TAG, "displaying results failed. unable to draw on canvas", e);
                }
            }
        });
    }
}
