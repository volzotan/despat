package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import de.volzo.despat.services.RecognitionService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.FixedAspectRatioFrameLayout;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    public static final String TAG = MainActivity.class.getName();
    MainActivity activity = this;

    ImageRollover imgroll;
    PowerbrainConnector powerbrain;
    CameraController cameraController;
    CameraController2 cameraController2;
    Recognizer recognizer;

    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "application init");

        checkPermissions();
        Config.init();

        File dir = Config.IMAGE_FOLDER;
        imgroll = new ImageRollover(dir);
        //Log.e(TAG, imgroll.getUnusedFilename(".jpg"));

        powerbrain = new PowerbrainConnector(this);
        powerbrain.connect();

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        Button startCapturing = (Button) findViewById(R.id.bt_startCapturing);
        startCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shutterIntent = new Intent(activity, Orchestrator.class);
                shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
                sendBroadcast(shutterIntent);
            }
        });

        Button startPreview = (Button) findViewById(R.id.bt_startPreview);
        startPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startPreview();
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

        Button btSleep = (Button) findViewById(R.id.bt_sleep);
        btSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        // receiver old
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String path = intent.getStringExtra("path");
                Log.d("image taken", "path: " + path);

            }
        }, new IntentFilter(Broadcast.PICTURE_TAKEN));

        // receiver new // TODO debug
        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.PICTURE_TAKEN);
        registerReceiver(broadcastReceiver, filter);

//        startCapturing.callOnClick();
//
//        cameraController = new CameraController(this, null);
//        cameraController.generateFilename(Config.IMAGE_FOLDER);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Broadcast Receiver
        unregisterReceiver(broadcastReceiver);
        if (powerbrain != null) {powerbrain.disconnect();}

        if (cameraController != null) cameraController.cleanup();

        Log.i(TAG, "application exit");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        // FIXME
        try {
            cameraController2 = new CameraController2(this, textureView);
        } catch (CameraAccessException e) {
            Log.e(TAG, "fail", e);
            e.printStackTrace();
        }
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

    public void startPreview() {

        if (cameraController == null) {
            cameraController = new CameraController(this, textureView.getSurfaceTexture());
        }
        cameraController.startPreview();
    }

    public void takePhoto() {

//        cameraController2 = new CameraController2(this, textureView);
//        cameraController2.openCamera();
//        cameraController2.takePicture();

        if (cameraController == null) {
            cameraController = new CameraController(this, textureView.getSurfaceTexture());
        }
        cameraController.takeImage();
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "photo taken");
        }
    };

    public void checkPermissions() {

        Activity activity = this;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1337);
        }
    }

}
