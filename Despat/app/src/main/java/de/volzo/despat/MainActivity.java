package de.volzo.despat;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    CameraController cameraController;

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
        cameraController = new CameraController(this, textureView);
        cameraController.takeImage();
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
}
