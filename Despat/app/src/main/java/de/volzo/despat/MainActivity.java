package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    CameraController cameraController;
    Recognizer recognizer;

    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraController != null) cameraController.cleanup();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        initialize();

//        recognizer = new Recognizer();
//        File dir = Environment.getExternalStorageDirectory();
//        File imageFullPath = new File(dir, "foobar" + ".jpg");
//        Recognizer.RecognizerResultset res = recognizer.run(imageFullPath);
//
//        Canvas canvas = textureView.lockCanvas();
//        canvas.drawBitmap(res.bitmap, new Matrix(), new Paint());
//        textureView.unlockCanvasAndPost(canvas);
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

    public void initialize() {
        checkPermissions();

        cameraController = new CameraController(this, textureView);
        cameraController.startPreview();
        cameraController.takeImage();
    }

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
