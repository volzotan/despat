package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.support.Util;
import de.volzo.despat.userinterface.ConfigureActivity;
import de.volzo.despat.userinterface.DrawSurface;
import de.volzo.despat.userinterface.SessionListActivity;
import de.volzo.despat.userinterface.SettingsActivity;
import de.volzo.despat.web.Sync;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CODE     = 0x123;
    public static final int DOZE_REQUEST_CODE           = 0x124;

    Despat despat;
    MainActivity activity = this;

    Detector detector;

    TextureView textureView;

    Handler periodicUpdateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // remove launcher theme
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "MainActivity init");

        despat = ((Despat) getApplicationContext());

        whitelistAppForDoze(activity);
        if (!checkPermissionsAreGiven(activity)) {
            requestPermissions(activity);
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
                Intent intent = new Intent(activity, SettingsActivity.class);
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
                if (Config.getPhoneHome(activity)) {
                    Sync.run(activity, MainActivity.class, true);
                } else {
                    Snackbar phoneHomeSnackbar = Snackbar.make(
                            activity.findViewById(R.id.snackbarLayout),
                            "Syncing not enabled.",
                            Snackbar.LENGTH_LONG);
                    phoneHomeSnackbar.setAction("enable", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Config.setPhoneHome(activity, true);
                        }
                    });
                    phoneHomeSnackbar.show();
                }
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

//        Button btKill = (Button) findViewById(R.id.bt_reset);
//        btKill.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent killIntent = new Intent(activity, Orchestrator.class);
//                killIntent.putExtra(Orchestrator.SERVICE, Broadcast.ALL_SERVICES);
//                killIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
//                sendBroadcast(killIntent);
//
//                AppDatabase.purgeDatabase(activity);
//                Config.reset(activity);
//
//                // TODO: delete despat folder
//
//                Log.i(TAG, "KILL: db purged. now attempting reboot!");
//
//                Despat despat = Util.getDespat(activity);
//                SystemController systemController = despat.getSystemController();
//                systemController.reboot();
//            }
//        });

        setButtonStates();

        final Button fabRec = findViewById(R.id.fabRec);
        fabRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SessionManager.getInstance(activity).isActive()) {
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

                    fabRec.setText("STOP");
                } else {
                    Log.d(TAG, "stopCapturing");
                    Util.darkenTextureView(textureView);
                    SessionManager session = SessionManager.getInstance(activity);
                    try {
                        session.stopRecordingSession();
                    } catch (SessionManager.NotRecordingException e) {
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

        runTestCode();

        File obbFile = new File(Util.getObbPath(this));
        if (!obbFile.exists()) {
            Log.e(TAG, "OBB file missing: " + obbFile.getAbsolutePath());

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Missing components")
                    .setMessage("During installation the required detection algorithms have not been downloaded")
                    .setPositiveButton("Download manually", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO

                            Snackbar.make(
                                    activity.findViewById(R.id.snackbarLayout),
                                    "TODO: not implemented yet.",
                                    Snackbar.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })

                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void runTestCode() {

//        Intent intent = new Intent(activity, SettingsActivity.class);
//        startActivity(intent);

//        float foo = 12.3f;
//        byte[] arr = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(foo).array();
//        float foo2 = ByteBuffer.allocate(4).getFloat();

//        Config.enableCTMode(this);

//        AppDatabase db = AppDatabase.getAppDatabase(this);
//        SessionDao sessionDao = db.sessionDao();
//        Session lastSession = sessionDao.getLast();
//
//        if (lastSession != null) {
//            SessionExporter exporter = new SessionExporter(this, lastSession.getId());
//            try {
//                exporter.export();
//            } catch (Exception e) {
//                Log.e(TAG, "export failed:", e);
//            }
//        }

//        Compressor compressor = new Compressor();
//        compressor.test(activity);

//        Util.printCameraParameters(this);

//        AppDatabase db = AppDatabase.getAppDatabase(this);
//        SessionDao sessionDao = db.sessionDao();
//        Session lastSession = sessionDao.getLast();
//        if (lastSession != null) {
////            Orchestrator.runRecognitionService(this, lastSession.getId());
//        };
//        Intent compressorIntent = new Intent(this, CompressorService.class);
//        startService(compressorIntent);

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

//        HomographyCalculator hcalc = new HomographyCalculator();
//        hcalc.test(this);

                //        btSettings.callOnClick();

//        startCapturing.callOnClick();
//        btConfig.callOnClick();

//        Util.printCameraParameters(this);

//        if (lastSession != null) {
//            PositionDao positionDao = db.positionDao();
//            CaptureDao captureDao = db.captureDao();
//            List<Position> positions = positionDao.getAllBySession(lastSession.getId());
//            try {
//                Detector detector = new DetectorTensorFlowMobile(this);
//                detector.init();
//                detector.load(captureDao.getLastFromSession(lastSession.getId()).getImage().getAbsoluteFile());
//                detector.display((DrawSurface) findViewById(R.id.drawSurface), detector.positionsToRectangles(positions));
//            } catch (Exception e) {
//                Log.e(TAG, "drawing results failed", e);
//            }
//        }
    }

    public static void runInitializationTasks(final Context context) {

        // create example session in background
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Long time = System.currentTimeMillis();
                Log.i(TAG, "creating Example Session");
                SessionManager sessionManager = SessionManager.getInstance(context);
                sessionManager.createExampleSession(context);
                Log.d(TAG, String.format("Example Session created in %d seconds", (System.currentTimeMillis()-time)/1000));
            }
        });

        // run Benchmark
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        Intent localIntent = new Intent(Broadcast.COMMAND_RUN_BENCHMARK);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        if (resultCode != RESULT_OK) {
            Log.e(TAG, "activity result not OK");
            return;
        }

        final TextureView textureView = findViewById(R.id.textureView);
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                Util.clearTextureView(textureView);
//                Util.drawTextOnTextureView(textureView, "foo"); // TODO

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
                case Broadcast.IMAGE_TAKEN: {
                    CaptureInfo info = (CaptureInfo) intent.getSerializableExtra(Broadcast.DATA_IMAGE_CAPTUREINFO);
                    Log.d("image taken", "path: " + info.getFilename());
                    updatePreviewImage();
                    break;
                }

                case Broadcast.PREVIEW_INFO: {
                    CaptureInfo info = (CaptureInfo) intent.getSerializableExtra(Broadcast.DATA_IMAGE_CAPTUREINFO);
                    double ev = Util.computeExposureValue(info.getExposureTime(), info.getAperture(), info.getIso());
                    Toast.makeText(context, String.format("Preview Info: %f", ev), Toast.LENGTH_SHORT);
                    break;
                }

                case Broadcast.SHOW_MESSAGE: {

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
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        cleanup();

        final TextureView textureView = (TextureView) findViewById(R.id.textureView);

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (textureView != null && textureView.isAvailable() && textureView.getSurfaceTexture() != null && !textureView.getSurfaceTexture().isReleased()) {
//                    textureView.getSurfaceTexture().release();
//                    Log.wtf(TAG, "textureView released by timeout");
//                }
//            }
//        }, 1200);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "MainActivity Resume");

        registerAllReceivers();
        startProgressBarUpdate();
        updatePreviewImage();
        updateSysInfobox();

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        if (textureView.isAvailable()) {
            startCamera(new CameraConfig(activity));
        }

        setButtonStates();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "MainActivity Start");

        registerAllReceivers();
        startProgressBarUpdate();
        updatePreviewImage();
        updateSysInfobox();
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
        Log.i(TAG, "MainActivity destroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.i(TAG, "MainActivity configuration changed");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (Config.START_CAMERA_ON_ACTIVITY_START) {
            if (checkPermissionsAreGiven(activity)) {
                try {
                    Size imageSize = CameraController2.getImageSize(activity, Config.getCameraDevice(this));

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
        Log.wtf(TAG, "SurfaceTexture destroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public void registerAllReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.IMAGE_TAKEN);
        filter.addAction(Broadcast.PREVIEW_INFO);
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

        despat.closeCamera();

        unregisterAllReceivers();
    }

    private void setButtonStates() {
        final Button fabRec = findViewById(R.id.fabRec);

        if (SessionManager.getInstance(activity).isActive()) {
            findViewById(R.id.layout_buttons).setVisibility(View.GONE);

            Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_stop);
            icon.setBounds( 0, 0, 62, 62 );
            fabRec.setCompoundDrawables( icon, null, null, null );
            fabRec.setText("Stop Recording");

            findViewById(R.id.block_general).setVisibility(View.GONE);
            findViewById(R.id.block_session).setVisibility(View.VISIBLE);
            findViewById(R.id.block_duration).setVisibility(View.VISIBLE);
            findViewById(R.id.block_numberofimages).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_buttons).setVisibility(View.VISIBLE);

            Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_rec);
            icon.setBounds( 0, 0, 62, 62 );
            fabRec.setCompoundDrawables( icon, null, null, null );
            fabRec.setText("Start Recording");

            findViewById(R.id.block_general).setVisibility(View.VISIBLE);
            findViewById(R.id.block_session).setVisibility(View.GONE);
            findViewById(R.id.block_duration).setVisibility(View.GONE);
            findViewById(R.id.block_numberofimages).setVisibility(View.GONE);
            findViewById(R.id.block_errors).setVisibility(View.GONE);
        }
    }

    public void startCamera(CameraConfig cameraConfig) {

        if (SessionManager.getInstance(activity).isActive()) {
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
            public void captureComplete(CaptureInfo info) {
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
                        camera.captureImages(null);
                    } catch (Exception e) {
                        Log.e(TAG, "capturing image failed");
                    }
                }
            }
        });
    }

    private void updatePreviewImage() {
        Context context = activity;
        SessionManager session = SessionManager.getInstance(context);

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
        } catch (SessionManager.NotRecordingException e) {
            activeSession = false;
        }

        if (!activeSession) {
            StringBuilder sb = new StringBuilder();
            sb.append("no active recording session");
            sb.append("\n\n");
            sb.append("free space on device: ");
            sb.append(String.format(Config.LOCALE, "%.0fmb", Util.getFreeSpaceOnDeviceInMb(Config.getImageFolders(activity).get(0))));
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
                        break;
                    }
                }
            } catch (SessionManager.NotRecordingException e) {
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

                try {

                SessionManager sessionManager = SessionManager.getInstance(activity);
                long shutterInterval = sessionManager.getSession().getCameraConfig().getShutterInterval();
                int progress = (int) (((float) diff / (float) shutterInterval) * 100f);

                captureProgressBar.setEnabled(true);
                captureProgressBar.setProgress(progress);
                } catch (SessionManager.NotRecordingException e) {
                    captureProgressBar.setBackgroundColor(getResources().getColor(R.color.error));
                    captureProgressBar.setProgress(100);
                }
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

    private void updateSysInfobox() {
        TextView time = (TextView) findViewById(R.id.tv_sysinfo_time);
        TextView imagesTaken = (TextView) findViewById(R.id.tv_sysinfo_imagestaken);
        TextView imagesInMemory = (TextView) findViewById(R.id.tv_sysinfo_imagesinmemory);
        TextView freeSpaceInternal = (TextView) findViewById(R.id.tv_sysinfo_freespaceinternal);
        TextView freeSpaceExternal = (TextView) findViewById(R.id.tv_sysinfo_freespaceexternal);
        TextView batteryInternal = (TextView) findViewById(R.id.tv_sysinfo_batteryinternal);
        TextView batteryExternal = (TextView) findViewById(R.id.tv_sysinfo_batteryexternal);
        TextView stateCharging = (TextView) findViewById(R.id.tv_sysinfo_statecharging);
        TextView temperatureDevice = (TextView) findViewById(R.id.tv_sysinfo_temperaturedevice);
        TextView temperatureBattery = (TextView) findViewById(R.id.tv_sysinfo_temperaturebattery);
        TextView dozeWhitelisted = (TextView) findViewById(R.id.tv_sysinfo_dozewhitelisted);

        Despat despat = ((Despat) getApplicationContext());
        SystemController systemController = despat.getSystemController();
        ImageRollover imgroll = new ImageRollover(this, ".jpg");
        SessionManager sessionManager = SessionManager.getInstance(this);

        time.setText((new SimpleDateFormat(Config.DATEFORMAT_SHORT, Config.LOCALE)).format(Calendar.getInstance().getTime()));
        if (sessionManager.isActive()) {
            imagesTaken.setText(Integer.toString(sessionManager.getImagesTaken()));
        }
        imagesInMemory.setText(Integer.toString(imgroll.getNumberOfSavedImages()));

        float freeSpace = 0;
        List<File> imageFolders = Config.getImageFolders(this);
        for (File f : imageFolders) {
            freeSpace += Util.getFreeSpaceOnDeviceInMb(f);
        }
        freeSpaceInternal.setText(Float.toString(freeSpace));

        freeSpaceExternal.setText("---");
        batteryInternal.setText(Integer.toString(systemController.getBatteryLevel()) + "%");
        batteryExternal.setText("---");
        stateCharging.setText(Boolean.toString(systemController.getBatteryChargingState()));
        temperatureDevice.setText("---");
        temperatureBattery.setText(Float.toString(systemController.getBatteryTemperature()) + "°C");
        dozeWhitelisted.setText(Boolean.toString(checkWhitelistingForDoze(activity)));
    }

    public void runRecognizer() {
        try {
            detector = new DetectorTensorFlowMobile(activity, new DetectorConfig(DetectorTensorFlowMobile.FIDELITY_MODE[0], 600));
//            detector = new DetectorHOG(activity);
            detector.init();
            detector.load(new File(Config.getImageFolders(activity).get(0), "test.jpg"));
            List<Detector.Recognition> detections = detector.run();
            detector.display((DrawSurface) findViewById(R.id.drawSurface), new Size(4320, 3240), detector.recognitionsToRectangles(detections), null);
        } catch (Exception e) {
            Log.wtf(TAG, "detector failed", e);
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(new File(Config.getImageFolders(activity).get(0), "test.jpg").getAbsolutePath())); // TODO: glide

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

    public static boolean checkWhitelistingForDoze(Activity activity) {
        PowerManager powerManger = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        return powerManger.isIgnoringBatteryOptimizations(activity.getPackageName());
    }

    /* Request a confirmation from the user that the app should be put on the Doze whitelist.
     * That allows the alarm manager to fire more often than once in 9 minutes.
     */
    public static void whitelistAppForDoze(Activity activity) {
        PowerManager powerManger = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (!powerManger.isIgnoringBatteryOptimizations(activity.getPackageName())) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivityForResult(intent, DOZE_REQUEST_CODE);
        }
    }

    public static boolean checkPermissionsAreGiven(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }

    public static void requestPermissions(Activity activity) {
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
                boolean success = true;

                for (int i=0; i<grantResults.length; i++) {
                    // even though only still image permissions are required, video/audio is part of the
                    // permission package. But since android never displays the audio request, it is denied.
                    if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                        continue;
                    }

                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        success = false;
                        break;
                    }
                }

                if (success) {
                    Log.w(TAG, "permissions are granted by user");
                    init();
                } else {
                    Log.w(TAG, "permissions denied by user");
                }
            }
        }
    }
}
