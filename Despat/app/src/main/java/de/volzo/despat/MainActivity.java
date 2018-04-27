package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorHOG;
import de.volzo.despat.detector.DetectorSSD;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.Sync;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 0x123;

    Despat despat;
    MainActivity activity = this;

    PowerbrainConnector powerbrain;
    Detector detector;

    TextureView textureView;
    PhotoViewAttacher photoViewAttacher;

    Handler periodicUpdateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "application init");

        despat = ((Despat) getApplicationContext());

//        Util.disableDoze();
        whitelistAppForDoze();

        if (!checkPermissionsAreGiven()) {
            requestPermissions();
        } else {
            init();
        }
    }

    public void init() {

        Config.init(activity);

        if (despat.getSystemController().hasTemperatureSensor()) {
            despat.getSystemController().startTemperatureMeasurement();
        }

        powerbrain = new PowerbrainConnector(this);
        powerbrain.connect();

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        Button btConfigure = (Button) findViewById(R.id.bt_configure);
        btConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ConfigureActivity.class);
                startActivity(intent);
            }
        });
        final ToggleButton startStopCapturing = findViewById(R.id.bt_startStopCapturing);
        startStopCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!RecordingSession.getInstance(activity).isActive()) { // if (!Util.isServiceRunning(activity, ShutterService.class)) {
                    Log.d(TAG, "startCapturing");

                    Despat despat = Util.getDespat(activity);
                    despat.closeCamera();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RecordingSession session = RecordingSession.getInstance(activity);
                            session.startRecordingSession(null);
                        }
                    }, 1000);
                    startProgressBarUpdate();
                    startStopCapturing.setChecked(true);
                } else {
                    Log.d(TAG, "stopCapturing");
                    RecordingSession session = RecordingSession.getInstance(activity);
                    try {
                        session.stopRecordingSession();
                    } catch (RecordingSession.NotRecordingException e) {
                        Log.e(TAG, "stopping Recording Session failed", e);
                    }
                    stopProgressBarUpdate();
                    startStopCapturing.setChecked(false);
                }
            }
        });
        if (!RecordingSession.getInstance(activity).isActive()) {
            startStopCapturing.setText("Start Capturing");
            startStopCapturing.setChecked(false);
        } else {
            startStopCapturing.setText("Stop Capturing");
            startStopCapturing.setChecked(true);
        }

        Button toggleCamera = (Button) findViewById(R.id.bt_toggleCamera);
        toggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageView iv = (ImageView) findViewById(R.id.imageView);
                iv.setImageDrawable(null);

                CameraController camera = despat.getCamera();

                if (camera == null || camera.isDead()) {
                    activity.startCamera();
                } else {
                    despat.closeCamera();
                }
            }
        });

        Button takePhoto = (Button) findViewById(R.id.bt_takePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.takePhoto();
            }
        });

        Button btRunRec = (Button) findViewById(R.id.bt_runRecognizer);
        btRunRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.runRecognizer();
            }
        });

        Button btKill = (Button) findViewById(R.id.bt_kill);
        btKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent killIntent = new Intent(activity, Orchestrator.class);
                killIntent.putExtra("service", Broadcast.ALL_SERVICES);
                killIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
                sendBroadcast(killIntent);

                AppDatabase.purgeDatabase(activity);
                Config.reset(activity);

                Log.i(TAG, "KILL: db purged. now attempting reboot!");

                Despat despat = Util.getDespat(activity);
                SystemController systemController = despat.getSystemController();
                systemController.reboot();
            }
        });

        Button btSettings = (Button) findViewById(R.id.bt_settings);
        btSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SettingsActivity2.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fabSync = findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sync.run(activity, MainActivity.class, true);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.PICTURE_TAKEN);
        registerReceiver(broadcastReceiver, filter);

        Intent heartbeatIntent = new Intent(activity, Orchestrator.class);
        heartbeatIntent.putExtra("service", Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra("operation", Orchestrator.OPERATION_START);
        sendBroadcast(heartbeatIntent);

        if (Config.getPhoneHome(this)) {
            Intent uploadIntent = new Intent(activity, Orchestrator.class);
            uploadIntent.putExtra("service", Broadcast.UPLOAD_SERVICE);
            uploadIntent.putExtra("operation", Orchestrator.OPERATION_ONCE);
            sendBroadcast(uploadIntent);
        }

        ContentResolver.addPeriodicSync(Util.createSyncAccount(this), Config.SYNC_AUTHORITY, Bundle.EMPTY, 1 * 60);

        registerAllReceivers();
        startProgressBarUpdate();
        updatePreviewImage();


//        btSettings.callOnClick();


//        startCapturing.callOnClick();
//        btConfig.callOnClick();

//        Util.printCameraParameters(this);

//        ImageRollover imgroll = new ImageRollover(activity, ".jpg");
//        Compressor compressor = new Compressor();
//        File img = imgroll.getNewestImage();
//        Bitmap bitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
//        compressor.init(bitmap.getWidth(), bitmap.getHeight());
//        compressor.add(imgroll.getNewestImage());
//        compressor.toJpeg(new File(Environment.getExternalStorageDirectory(), ("despat/foo.jpg")));

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        List<Session> sessions = sessionDao.getAll();

        for (Session session : sessions) {
            boolean noGlitch = RecordingSession.checkForIntegrity(this, session);

            if (noGlitch) {
                Log.i(TAG, "session [" + session.getSessionName() + "] has no glitch");
            } else {
                Log.i(TAG, "session [" + session.getSessionName() + "] has glitches");
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
            Log.d("image taken", "path: " + path);

            updatePreviewImage();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        cleanup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "MainActivity Resume");

        registerAllReceivers();
        startProgressBarUpdate();
        updatePreviewImage();
    }

    @Override
    protected void onStop() {
        super.onStop();

        cleanup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
        Log.i(TAG, "application exit");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (Config.START_CAMERA_ON_ACTIVITY_START) {
            if (checkPermissionsAreGiven()) {
                startCamera();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        despat.closeCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public void registerAllReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.PICTURE_TAKEN);
        registerReceiver(broadcastReceiver, filter);
    }

    public void unregisterAllReceivers() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            // ignore. cleanup is called multiple times, unregisterReceiver
            // succeeds only on first call
        }
    }

    public void cleanup() {

        Log.d(TAG, "cleanup. unregistering all receivers");

        stopProgressBarUpdate();

        if (photoViewAttacher != null) {
            photoViewAttacher.cleanup();
        }

        if (powerbrain != null) {
            powerbrain.disconnect();
            powerbrain = null;
        }

        despat.closeCamera();

        unregisterAllReceivers();
    }

    public void startCamera() {

        if (RecordingSession.getInstance(activity).isActive()) {
            Log.i(TAG, "no preview while recordingSession is active");
            return;
        }

        despat.closeCamera();
        try {
            CameraController camera = despat.initCamera(this, null, textureView);
            camera.openCamera();
        } catch (Exception cae) {
            Log.e(TAG, "starting Camera failed", cae);
            Toast.makeText(this, "starting Camera failed", Toast.LENGTH_SHORT).show();
        }

    }

    public void stopCamera() {
        // do not close Camera if shutterService is running

        // TODO
    }

    public void takePhoto() {

        final Context context = this;

        final Runnable displayPhotoRunnable = new Runnable() {
            @Override
            public void run() {
                ImageRollover imgroll = new ImageRollover(context, ".jpg");
                File newestImage = imgroll.getNewestImage();

                if (newestImage == null) return;

                Bitmap imgBitmap = BitmapFactory.decodeFile(newestImage.getAbsolutePath());
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(imgBitmap);

                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
                photoViewAttacher.update();

                Despat despat = Util.getDespat(context);
                despat.closeCamera();
            }
        };

        final CameraController.ControllerCallback callback = new CameraController.ControllerCallback() {
            @Override
            public void captureComplete() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Handler handler = new Handler();
                        handler.postDelayed(displayPhotoRunnable, 500);
                    }
                });
            }

        };

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                CameraController camera = despat.getCamera();
                if (camera == null || camera.isDead()) {
                    try {
                        camera = despat.initCamera(context, callback, null);
                        camera.openCamera();
                    } catch (Exception e) {
                        Log.e(TAG, "starting camera failed", e);
                        Toast.makeText(activity, "starting camera failed: " + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                } else {
                    try {
                        camera.captureImages();
                    } catch (Exception e) {
                        Log.e(TAG, "capturing image failed");
                    }
                }
            }
        });
    }

    private void updatePreviewImage() {
        Context context = activity;

        StringBuilder sb = new StringBuilder();
        RecordingSession session = RecordingSession.getInstance(context);

        boolean activeSession = false;
        try {
            if (session.isActive()) {
                activeSession = true;

                sb.append("Session: ");
                sb.append(session.getSessionName());
                sb.append("\n"); //sb.append(" | ");
                sb.append("running for: ");
                sb.append(Util.getHumanReadableTimediff(session.getStart(), Calendar.getInstance().getTime(), true));
                sb.append(" | ");
                sb.append("images: ");
                sb.append(session.getImagesTaken());

                int errors = session.getErrors();
                if (errors > 0) {
                    sb.append(" | ");
                    sb.append("errors: ");
                    sb.append(errors);
                }
            } else {
                activeSession = false;
            }
        } catch (RecordingSession.NotRecordingException e) {
            activeSession = false;
        }

        if (!activeSession) {
            sb.append("free: ");
            sb.append(String.format(Locale.ENGLISH, "%.0fmb", Util.getFreeSpaceOnDeviceInMb(Config.getImageFolder(activity))));
            sb.append(" | ");
            sb.append("no active session");
        }

        TextView tvStatus = (TextView) findViewById(R.id.tv_status);
        tvStatus.setText(sb.toString());

//        if (activeSession) {
//            ImageRollover imgroll = new ImageRollover(context, ".jpg");
//            File newestImage = imgroll.getNewestImage();
//
//            if (newestImage == null) return;
//
//            ImageView imageView = findViewById(R.id.imageView);
//            imageView.setImageBitmap(BitmapFactory.decodeFile(newestImage.getAbsolutePath()));
//
//            if (photoViewAttacher != null) {
//                photoViewAttacher.cleanup();
//            }
//            photoViewAttacher = new PhotoViewAttacher(imageView);
//            photoViewAttacher.update();
//
//        }
    }

    private void startProgressBarUpdate() {
        if (periodicUpdateHandler == null)
            periodicUpdateHandler = new Handler(Looper.getMainLooper());

        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {

                ProgressBar captureProgressBar = findViewById(R.id.captureProgressBar);
                long nextInvocation = Config.getNextShutterServiceInvocation(activity);
                long diff = nextInvocation - System.currentTimeMillis();

                periodicUpdateHandler.postDelayed(this, 500);

                if (diff < 0) {
                    captureProgressBar.setProgress(0);
                    captureProgressBar.setEnabled(false);
                    return;
                }

                long shutterInterval = Config.getShutterInterval(activity);
                int progress = (int) (((float) diff / (float) shutterInterval) * 100f);

                captureProgressBar.setEnabled(true);
                captureProgressBar.setProgress(progress);
            }
        };
        periodicUpdateHandler.post(updateRunnable);

        Log.d(TAG, "periodicUpdateHandler start");
    }

    private void stopProgressBarUpdate() {
        if (periodicUpdateHandler != null) {
            periodicUpdateHandler.removeCallbacksAndMessages(null);
        }

        ProgressBar captureProgressBar = findViewById(R.id.captureProgressBar);
        captureProgressBar.setProgress(0);
        captureProgressBar.setEnabled(false);

        Log.d(TAG, "periodicUpdateHandler start");
    }

    public void runRecognizer() {
        try {
//            detector = new DetectorSSD(activity);
            detector = new DetectorHOG(activity);
            detector.init();
            detector.load(new File(Config.getImageFolder(activity), "test.jpg"));
            List<Detector.Recognition> detections = detector.run();
            detector.display((DrawSurface) findViewById(R.id.drawSurface), detections);
        } catch (Exception e) {
            Log.wtf(TAG, "detector failed", e);
        }


        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(new File(Config.getImageFolder(activity), "test.jpg").getAbsolutePath()));

//        // remove the textureView from the preview
//        FixedAspectRatioFrameLayout aspectRatioLayout = (FixedAspectRatioFrameLayout) findViewById(R.id.aspectratio_layout);
//        aspectRatioLayout.removeView(findViewById(R.id.textureView));
//        textureView = new TextureView(this);
//        textureView.setId(R.id.textureView);
//        aspectRatioLayout.addView(textureView);
//
////        Intent mServiceIntent = new Intent(this, RecognitionService.class);
////
////        ImageRollover imgroll = new ImageRollover(Config.getImageFolder(this), Config.IMAGE_FILEEXTENSION);
////        File newestImage = imgroll.getNewestImage();
////
////        mServiceIntent.setData(Uri.parse(newestImage.getAbsolutePath()));
////        this.startService(mServiceIntent);
//
//        detector = new DetectorHOG();
//
//        ImageRollover imgroll = new ImageRollover(this);
//        File newestImage = imgroll.getNewestImage();
//
//        Detector.Resultset res = detector.run(newestImage.getAbsoluteFile());
//
//        ImageView imageView = (ImageView) findViewById(R.id.imageView);
//        imageView.setImageBitmap(res.bitmap);
//
//        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
//
//        TextView tvStatus = (TextView) findViewById(R.id.tv_status);
//        tvStatus.setText("n: " + res.coordinates.length);
    }

    /* Request a confirmation from the user that the app should be put on the Doze whitelist.
     * That allows the alarm manager to fire more often than once in 9 minutes.
     */
    private void whitelistAppForDoze() {
        PowerManager powerManger = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (!powerManger.isIgnoringBatteryOptimizations("de.volzo.despat")) { // getPackageName()
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private boolean checkPermissionsAreGiven() {
        Activity activity = this;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Log.w(TAG, "permissions denied by user");
                }
            }
        }
    }
}
