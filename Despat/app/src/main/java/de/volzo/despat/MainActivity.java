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

    final String SYNC_AUTHORITY = "de.volzo.despat.web.provider";
    final String SYNC_ACCOUNT   = "despatSyncAccount";

    Account syncAccount;

    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "application init");

        despat = ((Despat) getApplicationContext());

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

        // kill all services remaining from prior app starts
//        Intent killIntent = new Intent(activity, Orchestrator.class);
//        killIntent.putExtra("service", Broadcast.ALL_SERVICES);
//        killIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
//        sendBroadcast(killIntent);

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
        final ToggleButton startStopCapturing = (ToggleButton) findViewById(R.id.bt_startStopCapturing);
        startStopCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!RecordingSession.getInstance(activity).isActive()) { // if (!Util.isServiceRunning(activity, ShutterService.class)) {
                    Log.d(TAG, "startCapturing");
                    RecordingSession session = RecordingSession.getInstance(activity);
                    session.startRecordingSession(null);
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

                // FIXME
                Despat despat = Util.getDespat(activity);
                SystemController systemController = despat.getSystemController();
                systemController.reboot();

                Intent killIntent = new Intent(activity, Orchestrator.class);
                killIntent.putExtra("service", Broadcast.ALL_SERVICES);
                killIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
                sendBroadcast(killIntent);

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

        FloatingActionButton fabSync = (FloatingActionButton) findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(syncAccount, SYNC_AUTHORITY, settingsBundle);
                Log.d(TAG, "sync requested");
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

//        startCapturing.callOnClick();
//        btConfig.callOnClick();

        syncAccount = createSyncAccount(this);
        ContentResolver.addPeriodicSync(syncAccount, SYNC_AUTHORITY, Bundle.EMPTY, 10*60);

    }

    public Account createSyncAccount(Context context) {

        final String ACCOUNT_TYPE = "de.volzo.despat.servpat";

        Account newAccount = new Account(SYNC_ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.w(TAG, "account creation failed");
        }

        return newAccount;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
            Log.d("image taken", "path: " + path);

            Toast.makeText(activity, "image taken", Toast.LENGTH_SHORT).show();

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

        despat.closeCamera();
        try {
            despat.setCamera(new CameraController(this, null, textureView));
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

        // Caveat: Camera keeps running after taking a photo this way

        CameraController camera = despat.getCamera();
        final Context context = this;

        CameraController.ControllerCallback callback = new CameraController.ControllerCallback() {

            @Override
            public void finalImageTaken() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        ImageRollover imgroll = new ImageRollover(context);
                        File newestImage = imgroll.getNewestImage();

                        if (newestImage == null) return;

                        Bitmap imgBitmap = BitmapFactory.decodeFile(newestImage.getAbsolutePath());
                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(imgBitmap);

                        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
                        photoViewAttacher.update();}
                });
            }

        };

        if (camera == null || camera.isDead()) {
            try {
                despat.setCamera(new CameraController(this, callback, null));
            } catch (Exception e) {
                Log.e(TAG, "starting camera failed", e);
            }
        } else {
            camera.captureImages();
        }

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

    public boolean checkPermissionsAreGiven() {

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

    public void requestPermissions() {
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
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    init();
                } else {

                    Log.w(TAG, "permissions denied by user");
                }
            }
        }
    }
}
