package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import de.volzo.despat.services.RecognitionService;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.CameraAdapter;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.FixedAspectRatioFrameLayout;
import de.volzo.despat.support.Util;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    public static final String TAG = MainActivity.class.getName();

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
        Config.init();

        // kill all services remaining from prior app starts
        Intent killIntent = new Intent(activity, Orchestrator.class);
        killIntent.putExtra("service", Broadcast.ALL_SERVICES);
        killIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
        sendBroadcast(killIntent);

        powerbrain = new PowerbrainConnector(this);
        powerbrain.connect();

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        Button startCapturing = (Button) findViewById(R.id.bt_startCapturing);
        startCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "startCapturing");
                Intent shutterIntent = new Intent(activity, Orchestrator.class);
                shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
                shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
                sendBroadcast(shutterIntent);
            }
        });

        Button stopCapturing = (Button) findViewById(R.id.bt_stopCapturing);
        stopCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "stopCapturing");
                Intent shutterIntent = new Intent(activity, Orchestrator.class);
                shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
                shutterIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
                sendBroadcast(shutterIntent);
            }
        });

        Button toggleCamera = (Button) findViewById(R.id.bt_toggleCamera);
        toggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CameraAdapter camera = despat.getCamera();

                if (camera == null){
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

        Button btStartRec = (Button) findViewById(R.id.bt_startRecognizer);
        btStartRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startRecognizer();
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

        // receiver old
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
                Log.d("image taken", "path: " + path);

            }
        }, new IntentFilter(Broadcast.PICTURE_TAKEN));

        // receiver new // TODO debug
        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.PICTURE_TAKEN);
        registerReceiver(broadcastReceiver, filter);

        Intent heartbeatIntent = new Intent(activity, Orchestrator.class);
        heartbeatIntent.putExtra("service", Broadcast.UPLOAD_SERVICE);
        heartbeatIntent.putExtra("operation", Orchestrator.OPERATION_START);
        sendBroadcast(heartbeatIntent);


        startCapturing.callOnClick();

//        ServerConnector serverConnector = new ServerConnector(this);
//        ServerConnector.ServerMessage msg = new ServerConnector.ServerMessage();
//        msg.numberImages = 500;
//        serverConnector.sendStatus(msg);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "photo taken");

            despat.setImagesTaken(despat.getImagesTaken() + 1);
            Util.updateNotification(activity, despat.getImagesTaken());
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

        startCamera();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
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
            despat.setCamera(new CameraController2(this, textureView, CameraController2.OPEN_PREVIEW));
        } catch (CameraAccessException cae) {
            Log.e(TAG, "starting Camera failed", cae);
            Toast.makeText(this, "starting Camera failed", Toast.LENGTH_SHORT).show();
        }

    }

    public void takePhoto() {

        CameraAdapter camera = despat.getCamera();

        if (camera == null || camera.getState() == CameraAdapter.STATE_DEAD) {
            try {
                camera = new CameraController2(this, textureView, CameraController2.OPEN_PREVIEW_AND_TAKE_PHOTO);
                despat.setCamera(camera);
            } catch (CameraAccessException e) {
                Log.e(TAG, "taking photo failed", e);
            }
        } else {
            camera.takePhoto();
        }
    }

    public void startRecognizer() {
        // remove the textureView from the preview
        FixedAspectRatioFrameLayout aspectRatioLayout = (FixedAspectRatioFrameLayout) findViewById(R.id.aspectratio_layout);
        aspectRatioLayout.removeView(findViewById(R.id.textureView));
        textureView = new TextureView(this);
        textureView.setId(R.id.textureView);
        aspectRatioLayout.addView(textureView);

        Intent mServiceIntent = new Intent(this, RecognitionService.class);
        mServiceIntent.setData(Uri.parse("foobar.jpg"));
        this.startService(mServiceIntent);

        recognizer = new Recognizer();

        File dir = Config.IMAGE_FOLDER;
        File imageFullPath = new File(dir, "foobar" + ".jpg");

        Recognizer.RecognizerResultset res = recognizer.run(imageFullPath);

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
