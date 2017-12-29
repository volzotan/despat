package de.volzo.despat.support;

/**
 * Created by volzotan on 19.08.17.
 */

public interface CameraAdapter {

    int STATE_DEAD          = 0x11;
    int STATE_EMPTY_PREVIEW = 0x12;
    int STATE_PREVIEW       = 0x13;

    // public void openCamera() throws Exception;

    public void captureImages(final int number);

    public void closeCamera();

    public int getState();
}
