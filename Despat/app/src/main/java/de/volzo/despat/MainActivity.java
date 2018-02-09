package de.volzo.despat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.media.Image;
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
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.Calendar;

import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.FixedAspectRatioFrameLayout;
import de.volzo.despat.support.Util;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 0x123;

    Despat despat;
    MainActivity activity = this;

    PowerbrainConnector powerbrain;
    Recognizer recognizer;

    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "application init");

        despat = ((Despat) getApplicationContext());

//        Util.disableDoze();
//        whitelistAppForDoze();

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

                    startStopCapturing.setChecked(true);
                } else {
                    Log.d(TAG, "stopCapturing");
                    RecordingSession session = RecordingSession.getInstance(activity);
                    try {
                        session.stopRecordingSession();
                    } catch (RecordingSession.NotRecordingException e) {
                        Log.e(TAG, "stopping Recording Session failed", e);
                    }
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

                if (camera == null || camera.isDead()){
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

                Util.purgeDatabase(activity);

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
                Intent intent = new Intent(activity, SettingsActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fabSync = findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.startSyncManually(Util.createSyncAccount(activity));
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.PICTURE_TAKEN);
        registerReceiver(broadcastReceiver, filter);

        Intent heartbeatIntent = new Intent(activity, Orchestrator.class);
        heartbeatIntent.putExtra("service", Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra("operation", Orchestrator.OPERATION_START);
        sendBroadcast(heartbeatIntent);

        Intent uploadIntent = new Intent(activity, Orchestrator.class);
        uploadIntent.putExtra("service", Broadcast.UPLOAD_SERVICE);
        uploadIntent.putExtra("operation", Orchestrator.OPERATION_ONCE);
        sendBroadcast(uploadIntent);

        ContentResolver.addPeriodicSync(Util.createSyncAccount(this), Config.SYNC_AUTHORITY, Bundle.EMPTY, 1*60);


//        startCapturing.callOnClick();
//        btConfig.callOnClick();

//        Util.printCameraParameters(this);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
            Log.d("image taken", "path: " + path);

//            Toast.makeText(activity, "image taken", Toast.LENGTH_SHORT).show();

            StringBuilder sb = new StringBuilder();
            RecordingSession session = RecordingSession.getInstance(context);

            try {
                if (session.isActive()) {
                    sb.append("Session: ");
                    sb.append(session.getSessionName());
                    sb.append("\n"); //sb.append(" | ");
                    sb.append("running for: ");
                    sb.append(Util.getHumanReadableTimediff(session.getStart(), Calendar.getInstance().getTime()));
                    sb.append(" | ");
                    sb.append("images: ");
                    sb.append(session.getImagesTaken());
                }
            } catch (RecordingSession.NotRecordingException e) {}

            TextView tvStatus = (TextView) findViewById(R.id.tv_status);
            tvStatus.setText(sb.toString());
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        cleanup();
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

        if (Util.isServiceRunning(activity, ShutterService.class)) {
            Toast.makeText(activity, "running in background", Toast.LENGTH_SHORT).show();
        }

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

    public void cleanup() {

        Log.d(TAG, "cleanup. unregistering all receivers");

        if (powerbrain != null) {
            powerbrain.disconnect();
            powerbrain = null;
        }

        despat.closeCamera();

        // Broadcast Receiver
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            // ignore. cleanup is called multiple times, unregisterReceiver
            // succeeds only on first call
        }
    }

    public void startCamera() {

        if (RecordingSession.getInstance(activity).isActive()) {
            Log.i(TAG, "no preview while recordingSession is active");
            return;
        }

        despat.closeCamera();
        try {
            despat.initCamera(this, null, textureView);
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
                ImageRollover imgroll = new ImageRollover(context);
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
            public void finalImageTaken() {
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
                        despat.initCamera(context, callback, null);
                    } catch (Exception e) {
                        Log.e(TAG, "starting camera failed", e);
                    }
                } else {
                    camera.captureImages();
                }
            }
        });
    }

    public void runRecognizer() {
        // remove the textureView from the preview
        FixedAspectRatioFrameLayout aspectRatioLayout = (FixedAspectRatioFrameLayout) findViewById(R.id.aspectratio_layout);
        aspectRatioLayout.removeView(findViewById(R.id.textureView));
        textureView = new TextureView(this);
        textureView.setId(R.id.textureView);
        aspectRatioLayout.addView(textureView);

//        Intent mServiceIntent = new Intent(this, RecognitionService.class);
//
//        ImageRollover imgroll = new ImageRollover(Config.getImageFolder(this), Config.IMAGE_FILEEXTENSION);
//        File newestImage = imgroll.getNewestImage();
//
//        mServiceIntent.setData(Uri.parse(newestImage.getAbsolutePath()));
//        this.startService(mServiceIntent);

        recognizer = new Recognizer();

        ImageRollover imgroll = new ImageRollover(this);
        File newestImage = imgroll.getNewestImage();

        Recognizer.RecognizerResultset res = recognizer.run(newestImage.getAbsoluteFile());

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(res.bitmap);

        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);

        TextView tvStatus = (TextView) findViewById(R.id.tv_status);
        tvStatus.setText("n: " + res.coordinates.length);
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

        if (    ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
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
