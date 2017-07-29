package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;

/**
 * Created by volzotan on 02.12.16.
 */

public class CameraController implements Camera.PreviewCallback, Camera.PictureCallback, Camera.ShutterCallback {

    private static final String TAG = CameraController.class.getName();

    Context context;

    private Camera camera;
    private Camera.Parameters param;
    private int[] pictureSize;

    boolean shutdownAfterPictureTaken = true;


    public CameraController(Context context, TextureView textureView) {
        this.context = context;
        try {
            camera = Camera.open();
            setupCamera(textureView);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e );
        }
    }

    // initialize camera
    private void setupCamera( TextureView tv ) throws java.io.IOException {

        param = camera.getParameters();
        List<Camera.Size> pvsizes = param.getSupportedPreviewSizes();

        pictureSize = new int[] {param.getPictureSize().width, param.getPictureSize().height};
        Log.d(TAG, "supported picture size: " + pictureSize[0] + " x " + pictureSize[1]);

        int len = pvsizes.size();
        for (int i = 0; i < len; i++)
            Log.v( TAG, "camera preview format: "+pvsizes.get(i).width+"x"+pvsizes.get(i).height );

        param.setPreviewFormat( ImageFormat.NV21 );
        //param.setPreviewSize( pvsizes.get(len-1).width, pvsizes.get(len-1).height );
        param.setPreviewSize(1280, 960);

        camera.setParameters(param);

        SurfaceTexture surfaceTexture = tv.getSurfaceTexture();
        camera.setPreviewTexture(surfaceTexture);
        // camera.setDisplayOrientation(90);
    }

    public void takeImage() {
        // camera.setOneShotPreviewCallback(this);

        camera.startPreview();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.takePicture(this, this, this);
    }

    public void startPreview() {
        camera.startPreview();
    }

    public void stopPreview() {
        camera.stopPreview();
    }

    public void cleanup() {
        camera.stopPreview();
        // camera.release();
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {

        if (bytes == null) {
            // this callback is called once with raw image data and once with processed jpeg data
            // if the current phone doesn't support raw data, bytes will be empty
            Log.d(TAG, "image data empty");
            return;
        }

        Log.d( TAG, "imageCallback: picture retrieved ("+bytes.length+" bytes), storing.." );
        //String myname = outdir.concat("img").concat(String.valueOf(counter++)).concat(".yuv");

        File dir = Config.IMAGE_FOLDER;
        File imageFullPath = new File(dir, "foobar" + ".jpg");

        if (param.getPictureFormat() == ImageFormat.JPEG) {
            // it's already JPEG
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                fos.write(bytes);
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, "saving JPEG failed ", e);
                return;
            }
        } else {
            // try to store YUV data
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                Log.d(TAG, "image format: " + param.getPictureFormat());
                YuvImage image = new YuvImage(bytes, param.getPictureFormat(), pictureSize[0], pictureSize[1], null);
                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);

                // fos.close();

            } catch (Exception e) {
                Log.e(TAG, "saving YUV failed ", e);
                return;
            }
        }

        try {
            Log.d(TAG, "imageCallback: picture stored successfully as " + imageFullPath.getCanonicalPath());

            Intent intent = new Intent(Broadcast.PICTURE_TAKEN);
            intent.putExtra("path", imageFullPath.getAbsolutePath());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            if (shutdownAfterPictureTaken) {
                this.cleanup();
            }
        } catch (IOException e) {
            Log.e(TAG, "accessing image path failed ", e);
            return;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.d(TAG, "preview frame");
    }

    @Override
    public void onShutter() {
        Log.d(TAG, "shutter released");
    }
}

