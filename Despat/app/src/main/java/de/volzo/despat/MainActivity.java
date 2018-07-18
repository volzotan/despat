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
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorSSD;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.services.CompressorService;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.HomographyCalculator;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.support.Util;
import de.volzo.despat.userinterface.ConfigureActivity;
import de.volzo.despat.userinterface.DrawSurface;
import de.volzo.despat.userinterface.SessionListActivity;
import de.volzo.despat.userinterface.SettingsActivity2;
import de.volzo.despat.web.Sync;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = MainActivity.class.getSimpleName();
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.activity_main);

        Log.i(TAG, "despat MainActivity init");

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

//        powerbrain = new PowerbrainConnector(this);
//        powerbrain.connect();

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        final Button btSessions = (Button) findViewById(R.id.bt_sessions);
        btSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, SessionListActivity.class));
            }
        });

        final Button btSettings = (Button) findViewById(R.id.bt_settings);
        btSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SettingsActivity2.class);
                startActivity(intent);
            }
        });

        Button btRunRec = (Button) findViewById(R.id.bt_runRecognizer);
        btRunRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.runRecognizer();
            }
        });

        Button btToggleCamera = (Button) findViewById(R.id.bt_toggleCamera);
        btToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageView iv = (ImageView) findViewById(R.id.imageView);
                iv.setImageDrawable(null);

                CameraController camera = despat.getCamera();

                if (camera == null || camera.isDead()) {
                    activity.startCamera(new CameraConfig(activity));
                } else {
                    despat.closeCamera();
                }
            }
        });

        Button btSync = (Button) findViewById(R.id.bt_sync);
        btSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Sync.run(activity, MainActivity.class, true);
            }
        });

        Button btCompressor = (Button) findViewById(R.id.bt_compressorServiceStart);
        btCompressor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Orchestrator.runCompressorService(activity);
            }
        });

        Button btRecognition = (Button) findViewById(R.id.bt_recognitionServiceStart);
        btRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDatabase db = AppDatabase.getAppDatabase(activity);
                SessionDao sessionDao = db.sessionDao();
                Session session = sessionDao.getLast();
                if (session != null) {
                    Orchestrator.runRecognitionService(activity, session.getId());
                } else {
                    Log.w(TAG, "no session found");
                }
            }
        });

        Button btHomography = (Button) findViewById(R.id.bt_homographyServiceStart);
        btHomography.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDatabase db = AppDatabase.getAppDatabase(activity);
                SessionDao sessionDao = db.sessionDao();
                Session session = sessionDao.getLast();
                if (session != null) {
                    Orchestrator.runHomographyService(activity, session.getId());
                } else {
                    Log.w(TAG, "no session found");
                }
            }
        });

        Button btKill = (Button) findViewById(R.id.bt_reset);
        btKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent killIntent = new Intent(activity, Orchestrator.class);
                killIntent.putExtra(Orchestrator.SERVICE, Broadcast.ALL_SERVICES);
                killIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
                sendBroadcast(killIntent);

                AppDatabase.purgeDatabase(activity);
                Config.reset(activity);

                // TODO: delete despat folder

                Log.i(TAG, "KILL: db purged. now attempting reboot!");

                Despat despat = Util.getDespat(activity);
                SystemController systemController = despat.getSystemController();
                systemController.reboot();
            }
        });

        setButtonStates();

        final FloatingActionButton fabRec = findViewById(R.id.fabRec);
        final TextView tvFapRec = findViewById(R.id.tvFapRec);
        fabRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!RecordingSession.getInstance(activity).isActive()) {
                    Log.d(TAG, "startCapturing");

                    Despat despat = Util.getDespat(activity);
                    despat.closeCamera();

                    Intent configureIntent = new Intent(activity, ConfigureActivity.class);
                    configureIntent.setAction(ConfigureActivity.TRACKING_SESSION);
                    startActivityForResult(configureIntent, 0x123);

//                    final TextureView textureView = findViewById(R.id.textureView);
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Util.clearTextureView(textureView);
//                            Util.drawTextOnTextureView(textureView, "foo");
//                            RecordingSession session = RecordingSession.getInstance(activity);
//                            session.startRecordingSession(null);
//
//                            updatePreviewImage();
//                        }
//                    }, 1000);
//                    startProgressBarUpdate();
////                    btStartStopCapturing.setChecked(true);

                    tvFapRec.setText("STOP");
                } else {
                    Log.d(TAG, "stopCapturing");
                    Util.darkenTextureView(textureView);
                    RecordingSession session = RecordingSession.getInstance(activity);
                    try {
                        session.stopRecordingSession();
                    } catch (RecordingSession.NotRecordingException e) {
                        Log.e(TAG, "stopping Recording Session failed", e);
                    }
                    stopProgressBarUpdate();
//                    btStartStopCapturing.setChecked(false);
                }

                setButtonStates();
            }
        });

        LinearLayout llBlockGeneral = (LinearLayout) findViewById(R.id.block_general);
        llBlockGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        llBlockGeneral.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout tvSysinfo = (LinearLayout) findViewById(R.id.sysinfo);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        tvSysinfo.setVisibility(View.VISIBLE);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        tvSysinfo.setVisibility(View.GONE);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        break;
                    }

                    default: {
                        break;
                    }
                }

                return true;
            }
        });

        if (Config.getPhoneHome(this)) {
            Intent uploadIntent = new Intent(activity, Orchestrator.class);
            uploadIntent.putExtra(Orchestrator.SERVICE, Broadcast.UPLOAD_SERVICE);
            uploadIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_ONCE);
            sendBroadcast(uploadIntent);
        }

        ContentResolver.addPeriodicSync(Util.createSyncAccount(this), Config.SYNC_AUTHORITY, Bundle.EMPTY, 1 * 60);

        registerAllReceivers();
        startProgressBarUpdate();
        updatePreviewImage();

        runTestCode();
    }

    private void runTestCode() {

//        Util.printCameraParameters(this);

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        Session lastSession = sessionDao.getLast();
        if (lastSession != null) {
//            Orchestrator.runRecognitionService(this, lastSession.getId());
        };
        Intent compressorIntent = new Intent(this, CompressorService.class);
        startService(compressorIntent);

//        if (lastSession != null) {
//            Intent pointIntent = new Intent(activity, PointActivity.class);
//            pointIntent.putExtra(PointActivity.ARG_SESSION_ID, lastSession.getId());
//            startActivityForResult(pointIntent, 0x1234);
//        }

//        btSessions.callOnClick();

//        HomographyPointDao homographyPointDao = db.homographyPointDao();
//
//        HomographyPoint point = new HomographyPoint();
//        Session s = sessionDao.getLast();
//        if (s != null){
//            point.setSessionId(s.getId());
//            homographyPointDao.insert(point);
//            Util.saveErrorEvent(this, s.getId(), "test", new Exception("testexception"));
//        } else {
//            Log.wtf(TAG, "session missing");
//        }
//
        HomographyCalculator hcalc = new HomographyCalculator();
        hcalc.test(this);

        //        btSettings.callOnClick();

//        startCapturing.callOnClick();
//        btConfig.callOnClick();

//        Util.printCameraParameters(this);

//        if (lastSession != null) {
//            PositionDao positionDao = db.positionDao();
//            CaptureDao captureDao = db.captureDao();
//            List<Position> positions = positionDao.getAllBySession(lastSession.getId());
//            try {
//                Detector detector = new DetectorSSD(this);
//                detector.init();
//                detector.load(captureDao.getLastFromSession(lastSession.getId()).getImage().getAbsoluteFile());
//                detector.display((DrawSurface) findViewById(R.id.drawSurface), detector.positionsToRectangles(positions));
//            } catch (Exception e) {
//                Log.e(TAG, "drawing results failed", e);
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        if (resultCode != RESULT_OK) {
            Log.e(TAG, "activity result not OK");
            return;
        }

        CameraConfig cameraConfig = (CameraConfig) data.getSerializableExtra(ConfigureActivity.DATA_CAMERA_CONFIG);

        final TextureView textureView = findViewById(R.id.textureView);
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                Util.clearTextureView(textureView);
                Util.drawTextOnTextureView(textureView, "foo");
                RecordingSession session = RecordingSession.getInstance(activity);
                session.startRecordingSession(null, cameraConfig);

                updatePreviewImage();
//            }
//        }, 1000);
        startProgressBarUpdate();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action) {
                case Broadcast.PICTURE_TAKEN:
                    String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
                    Log.d("image taken", "path: " + path);
                    updatePreviewImage();
                    break;

                case Broadcast.SHOW_MESSAGE:

                    String message = intent.getStringExtra(Broadcast.DATA_MESSAGE);
                    String reason = intent.getStringExtra(Broadcast.DATA_REASON);

                    StringBuilder sb = new StringBuilder();
                    sb.append(message);
                    if (reason != null) {
                        sb.append(" | ");
                        sb.append(reason);
                    }

                    Snackbar.make(
                            activity.findViewById(R.id.snackbarLayout),
                            sb.toString(),
                            Snackbar.LENGTH_LONG
                    ).show();

                    break;
            }
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

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        if (textureView.isAvailable()) {
            startCamera(new CameraConfig(activity));
        }

        registerAllReceivers();
        startProgressBarUpdate();
        updatePreviewImage();

        setButtonStates();
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
                try {
                    Size imageSize = CameraController2.getImageSize(activity);

                    // TODO:
                    CameraConfig cameraConfig = new CameraConfig(activity);
                    // cameraConfig.setZoomRegion(new Rect((imageSize.getWidth() / 2) - (imageSize.getWidth() / 4) / 2, (imageSize.getHeight() / 2) - (imageSize.getHeight() / 4) / 2, (imageSize.getWidth() / 2) + (imageSize.getWidth() / 4) / 2, (imageSize.getHeight() / 2) + (imageSize.getHeight() / 4) / 2));

                    startCamera(cameraConfig);
                } catch (Exception e) {
                    Log.e(TAG, "could not get image size", e);
                }
            } else {
                Toast.makeText(this, "camera inactive : permissions are missing", Toast.LENGTH_LONG).show();
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
        filter.addAction(Broadcast.SHOW_MESSAGE);
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
//            photoViewAttacher.cleanup();
        }

        if (powerbrain != null) {
            powerbrain.disconnect();
            powerbrain = null;
        }

        despat.closeCamera();

        unregisterAllReceivers();
    }

    private void setButtonStates() {
        final TextView tvFapRec = findViewById(R.id.tvFapRec);

        if (RecordingSession.getInstance(activity).isActive()) {
            findViewById(R.id.layout_buttons).setVisibility(View.GONE);
            tvFapRec.setText("STOP");

            findViewById(R.id.block_general).setVisibility(View.GONE);
            findViewById(R.id.block_session).setVisibility(View.VISIBLE);
            findViewById(R.id.block_duration).setVisibility(View.VISIBLE);
            findViewById(R.id.block_numberofimages).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_buttons).setVisibility(View.VISIBLE);
            tvFapRec.setText("REC");

            findViewById(R.id.block_general).setVisibility(View.VISIBLE);
            findViewById(R.id.block_session).setVisibility(View.GONE);
            findViewById(R.id.block_duration).setVisibility(View.GONE);
            findViewById(R.id.block_numberofimages).setVisibility(View.GONE);
            findViewById(R.id.block_errors).setVisibility(View.GONE);
        }
    }

    public void startCamera(CameraConfig cameraConfig) {

        if (RecordingSession.getInstance(activity).isActive()) {
            Log.i(TAG, "no preview while recordingSession is active");
            return;
        }

        despat.closeCamera();
        try {
            CameraController camera = despat.initCamera(this, null, textureView, cameraConfig);
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
                        camera = despat.initCamera(context, callback, null, new CameraConfig(activity));
                        camera.openCamera();
                    } catch (Exception e) {
                        Log.e(TAG, "starting camera failed", e);
                        Toast.makeText(activity, "starting camera failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        RecordingSession session = RecordingSession.getInstance(context);

        setButtonStates();

        TextView tvGeneral = findViewById(R.id.tv_block_general);
        TextView tvSession = findViewById(R.id.tv_block_session);
        TextView tvDuration = findViewById(R.id.tv_block_duration);
        TextView tvNumberOfImages = findViewById(R.id.tv_block_numberofimages);

        View blockErrors = findViewById(R.id.block_errors);
        TextView tvErrors = findViewById(R.id.tv_block_errors);

        boolean activeSession = false;
        try {
            if (session.isActive()) {
                activeSession = true;

                tvSession.setText(session.getSessionName());
                tvDuration.setText(Util.getHumanReadableTimediff(session.getStart(), Calendar.getInstance().getTime(), true));
                tvNumberOfImages.setText(Integer.toString(session.getImagesTaken()));

                int errors = session.getErrors();
                if (errors > 0) {
                    blockErrors.setVisibility(View.VISIBLE);
                    tvErrors.setText(Integer.toString(errors));
                } else {
                    blockErrors.setVisibility(View.GONE);
                }
            } else {
                activeSession = false;
            }
        } catch (RecordingSession.NotRecordingException e) {
            activeSession = false;
        }

        if (!activeSession) {
            StringBuilder sb = new StringBuilder();
            sb.append("no active recording session");
            sb.append("\n\n");
            sb.append("free space on device: ");
            sb.append(String.format(Config.LOCALE, "%.0fmb", Util.getFreeSpaceOnDeviceInMb(Config.getImageFolder(activity))));
            tvGeneral.setText(sb.toString());
        }

        if (activeSession) {

            File newestImage = null;

            try {
                AppDatabase db = AppDatabase.getAppDatabase(context);
                CaptureDao captureDao = db.captureDao();
                List<Capture> lastCaptures = captureDao.getLast3FromSession(session.getSessionId());

                for (int i=lastCaptures.size()-1; i>=0; i--) {
                    File img = lastCaptures.get(i).getImage();
                    if (img != null && img.exists()) {
                        newestImage = img;
                    }
                }
            } catch (RecordingSession.NotRecordingException e) {
                Log.w(TAG, "no session active, displaying last capture failed");
            }

//            ImageRollover imgroll = new ImageRollover(context, ".jpg");
//            newestImage = imgroll.getNewestImage();

            if (newestImage == null) return;

            ImageView imageView = findViewById(R.id.imageView);
            Glide.with(activity).load(newestImage).into(imageView);

//            imageView.setImageBitmap(BitmapFactory.decodeFile(newestImage.getAbsolutePath()));
//
//            if (photoViewAttacher != null) {
//                photoViewAttacher.cleanup();
//            }
//            photoViewAttacher = new PhotoViewAttacher(imageView);
//            photoViewAttacher.update();

        }
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

                periodicUpdateHandler.postDelayed(this, 250);

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
            detector = new DetectorSSD(activity);
//            detector = new DetectorHOG(activity);
            detector.init();
            detector.load(new File(Config.getImageFolder(activity), "test.jpg"));
            List<Detector.Recognition> detections = detector.run();
            detector.display((DrawSurface) findViewById(R.id.drawSurface), null, detector.recognitionsToRectangles(detections));
        } catch (Exception e) {
            Log.wtf(TAG, "detector failed", e);
        }


        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(new File(Config.getImageFolder(activity), "test.jpg").getAbsolutePath())); // TODO: glide

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
        if (!powerManger.isIgnoringBatteryOptimizations(activity.getPackageName())) {
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
                        Manifest.permission.RECORD_AUDIO,
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
                    Log.w(TAG, "permissions are granted by user");
                    init();
                } else {
                    Log.w(TAG, "permissions denied by user");
                }
            }
        }
    }
}
