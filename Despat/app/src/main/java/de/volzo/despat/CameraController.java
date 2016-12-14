package de.volzo.despat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by volzotan on 02.12.16.
 */

public class CameraController implements Camera.PreviewCallback, Camera.PictureCallback, Camera.ShutterCallback {

    private static final String TAG = CameraController.class.getName();

    Context context;

    private Camera camera;


    public CameraController(Context context, TextureView textureView) {
        this.context = context;

        try {
            checkPermissions();
            camera = Camera.open();
            setupCamera(textureView);
        } catch (Exception e) {
            Log.e(TAG, "::onCreate: ", e );
        }
    }

    // initialize camera
    private void setupCamera( TextureView tv ) throws java.io.IOException {

        // Log.v( TAG, "::setupCamera: " + tv.toString() );

        Camera.Parameters param = camera.getParameters();
        List<Camera.Size> pvsizes = param.getSupportedPreviewSizes();
        int len = pvsizes.size();
        for (int i = 0; i < len; i++)
            Log.v( TAG, "camera preview format: "+pvsizes.get(i).width+"x"+pvsizes.get(i).height );
        param.setPreviewFormat( ImageFormat.NV21 );
        //param.setPreviewSize( pvsizes.get(len-1).width, pvsizes.get(len-1).height );
        param.setPreviewSize( 1280, 960 );
        camera.setParameters( param );

        SurfaceTexture surfaceTexture = tv.getSurfaceTexture();
        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    public void takeImage() {
        // camera.setOneShotPreviewCallback(this);
        camera.takePicture(this, this, this);
    }

    public void cleanup() {
        camera.stopPreview();
        camera.release();
    }

    private void checkPermissions() {

        Activity activity = (Activity) context;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA},
                        1337);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        Log.wtf(TAG, "image taken");
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        int width = 1280;
        int height = 960;

        Log.v( TAG, "::imageCallback: picture retrieved ("+bytes.length+" bytes), storing.." );
        //String myname = outdir.concat("img").concat(String.valueOf(counter++)).concat(".yuv");
        String myname = "foobar.jpg";

        // store YUV data
        try {

            FileOutputStream fos = new FileOutputStream(myname);
            YuvImage yuvImage = new YuvImage( bytes, ImageFormat.NV21, width, height, null);
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, fos);
            fos.close();

            Log.v( TAG, "::imageCallback: picture stored successfully as " + myname );

        } catch (Exception e) {
            Log.e( TAG, "::imageCallback: ", e );
        }
    }

    @Override
    public void onShutter() {
        Log.wtf(TAG, "shutter released");
    }
}

