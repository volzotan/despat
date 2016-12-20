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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import de.volzo.despat.support.Config;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    CameraController cameraController;
    CameraController2 cameraController2;
    Recognizer recognizer;

    TextureView textureView;
    MainActivity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraController != null) cameraController.cleanup();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        initialize();



//        Canvas canvas = textureView.lockCanvas();
//
//        Matrix mat = new Matrix();
//        float scaledWidth = canvas.getWidth()/(float) res.bitmap.getWidth();
//        float scaledHeight = canvas.getHeight()/(float) res.bitmap.getHeight();
//        mat.postScale(scaledWidth, scaledHeight);
//        //
//        // mat.postRotate(90f);
//        //mat.postTranslate();
//
//        canvas.drawBitmap(res.bitmap, mat, new Paint());
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

    }

    public void takePhoto() {

//        cameraController2 = new CameraController2(this, textureView);
//        cameraController2.openCamera();
//        cameraController2.takePicture();

        cameraController = new CameraController(this, textureView);
        cameraController.takeImage();
    }

    public void startRecognizer() {
        recognizer = new Recognizer();

        File dir = Config.FOLDER;
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

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1337);
        }
    }

}
