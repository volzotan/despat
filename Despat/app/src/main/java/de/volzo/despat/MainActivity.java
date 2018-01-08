package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
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

import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.FixedAspectRatioFrameLayout;
import de.volzo.despat.support.Util;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    public static final String TAG = MainActivity.class.getSimpleName();

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

        checkPermissions();
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

        final ToggleButton startStopCapturing = (ToggleButton) findViewById(R.id.bt_startStopCapturing);
        startStopCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Util.isServiceRunning(activity, ShutterService.class)) {
                    Log.d(TAG, "startCapturing");
                    Intent shutterIntent = new Intent(activity, Orchestrator.class);
                    shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
                    shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
                    sendBroadcast(shutterIntent);

                    startStopCapturing.setChecked(true);
                } else {
                    Log.d(TAG, "stopCapturing");
                    Intent shutterIntent = new Intent(activity, Orchestrator.class);
                    shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
                    shutterIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
                    sendBroadcast(shutterIntent);

                    startStopCapturing.setChecked(false);
                }
            }
        });
        if (!Util.isServiceRunning(activity, ShutterService.class)) {
            startStopCapturing.setText("Start Capturing");
        } else {
            startStopCapturing.setText("Stop Capturing");
        }

        Button toggleCamera = (Button) findViewById(R.id.bt_toggleCamera);
        toggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CameraController camera = despat.getCamera();

                if (camera == null || camera.getState() == CameraController.STATE_DEAD){
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

            }
        });

        Button btConfig = (Button) findViewById(R.id.bt_config);
        btConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ConfigActivity.class);
                startActivity(intent);
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
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
            Log.d("image taken", "path: " + path);

            Toast.makeText(activity, "image taken", Toast.LENGTH_SHORT).show();

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

//        if (!ShutterService.isRunning(this)) {
//            // TODO
//        }

        Log.i(TAG, "application exit");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        //startCamera();
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
            despat.setCamera(new CameraController(this, textureView));
        } catch (Exception cae) {
            Log.e(TAG, "starting Camera failed", cae);
            Toast.makeText(this, "starting Camera failed", Toast.LENGTH_SHORT).show();
        }

    }

    public void takePhoto() {

        CameraController camera = despat.getCamera();

        if (camera == null || camera.getState() == CameraController.STATE_DEAD) {
            try {
                camera = new CameraController(this, null); //, CameraController.OPEN_PREVIEW_AND_TAKE_PHOTO);
                despat.setCamera(camera);
            } catch (Exception e) {
                Log.e(TAG, "starting camera failed", e);
            }
        } else {
            camera.captureImages();
        }

//        //camera.takePicture();
//        if (true) return;
//
//        // TODO: wait till camera has started
//        if (camera != null && camera.getState() != CameraController.STATE_DEAD) {
//            camera.captureImages(2);
//        }

//        final Context ctx = this;
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                ImageRollover imgroll = new ImageRollover(Config.getImageFolder(ctx), Config.IMAGE_FILEEXTENSION);
//                File newestImage = imgroll.getNewestImage();
//
//                if (newestImage == null) return;
//
//                Bitmap imgBitmap = BitmapFactory.decodeFile(newestImage.getAbsolutePath());
//
//                ImageView imageView = (ImageView) findViewById(R.id.imageView);
//
//                imageView.setImageBitmap(imgBitmap);
//
//                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
//                photoViewAttacher.update();
//            }
//        }, 1500); // TODO: buffer queue gets abandoned if this is not long enough. Something is wrong...

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

        ImageRollover imgroll = new ImageRollover(Config.getImageFolder(this), Config.IMAGE_FILEEXTENSION);
        File newestImage = imgroll.getNewestImage();

        Recognizer.RecognizerResultset res = recognizer.run(newestImage.getAbsoluteFile());

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(res.bitmap);

        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);

        TextView tvStatus = (TextView) findViewById(R.id.tv_status);
        tvStatus.setText("n: " + res.coordinates.length);
    }

    public void checkPermissions() {

        Activity activity = this;

        if (    ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {

                ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        1337);
        }
    }

}
