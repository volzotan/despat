package de.volzo.despat.preferences;

import android.content.Context;
import android.graphics.Rect;

import java.io.IOException;
import java.io.Serializable;

public class CameraConfig { // implements Serializable {

    private String cameraDevice;
    private boolean persistentCamera;
    private int shutterInterval;
    private boolean legacyCameraController;

    private boolean formatJpg;
    private boolean formatRaw;

//    private SerializableRect zoomRegion;
    private Rect zoomRegion;

    private int exposureCompensation;
    private int secondImageExposureCompensation;

    private boolean endCaptureWithoutUnlockingFocus;
    private int numberOfBurstImages;
    private byte jpegQuality;

    private int meteringMaxTime;
    private boolean runMediascannerAfterCapture;

    public CameraConfig() {}

    public CameraConfig(Context context) {
        this.cameraDevice = Config.getCameraDevice(context);
        this.persistentCamera = Config.getPersistentCamera(context);
        this.shutterInterval = Config.getShutterInterval(context);
        this.legacyCameraController = Config.getLegacyCameraController(context);

        this.formatJpg = Config.getFormatJpg(context);
        this.formatRaw = Config.getFormatRaw(context);

        this.exposureCompensation = Config.getExposureCompensation(context);
        this.secondImageExposureCompensation = Config.getSecondImageExposureCompensation(context);

        this.endCaptureWithoutUnlockingFocus = Config.END_CAPTURE_WITHOUT_UNLOCKING_FOCUS;
        this.numberOfBurstImages = Config.NUMBER_OF_BURST_IMAGES;
        this.jpegQuality = Config.JPEG_QUALITY;

        this.meteringMaxTime = Config.METERING_MAX_TIME;
        this.runMediascannerAfterCapture = Config.RUN_MEDIASCANNER_AFTER_CAPTURE;
    }

    public String getCameraDevice() {
        return cameraDevice;
    }

    public void setCameraDevice(String cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public boolean getPersistentCamera() {
        return persistentCamera;
    }

    public void setPersistentCamera(boolean persistentCamera) {
        this.persistentCamera = persistentCamera;
    }

    public int getShutterInterval() {
        return shutterInterval;
    }

    public void setShutterInterval(int shutterInterval) {
        this.shutterInterval = shutterInterval;
    }

    public boolean isLegacyCameraController() {
        return legacyCameraController;
    }

    public void setLegacyCameraController(boolean legacyCameraController) {
        this.legacyCameraController = legacyCameraController;
    }

    public boolean isFormatJpg() {
        return formatJpg;
    }

    public void setFormatJpg(boolean formatJpg) {
        this.formatJpg = formatJpg;
    }

    public boolean isFormatRaw() {
        return formatRaw;
    }

    public void setFormatRaw(boolean formatRaw) {
        this.formatRaw = formatRaw;
    }

//    public Rect getZoomRegion() {
//        if (zoomRegion != null) {
//            return zoomRegion.getRect();
//        } else {
//            return null;
//        }
//    }
//
//    public void setZoomRegion(SerializableRect zoomRegion) {
//        this.zoomRegion = zoomRegion;
//    }
//
//    public void setZoomRegion(Rect zoomRegion) {
//        this.zoomRegion = new SerializableRect(zoomRegion);
//    }

    public Rect getZoomRegion() {
        return zoomRegion;
    }

    public void setZoomRegion(Rect zoomRegion) {
        this.zoomRegion = zoomRegion;
    }

    public int getExposureCompensation() {
        return exposureCompensation;
    }

    public void setExposureCompensation(int exposureCompensation) {
        this.exposureCompensation = exposureCompensation;
    }

    public int getSecondImageExposureCompensation() {
        return secondImageExposureCompensation;
    }

    public void setSecondImageExposureCompensation(int secondImageExposureCompensation) {
        this.secondImageExposureCompensation = secondImageExposureCompensation;
    }

    public boolean isEndCaptureWithoutUnlockingFocus() {
        return endCaptureWithoutUnlockingFocus;
    }

    public void setEndCaptureWithoutUnlockingFocus(boolean endCaptureWithoutUnlockingFocus) {
        this.endCaptureWithoutUnlockingFocus = endCaptureWithoutUnlockingFocus;
    }

    public int getNumberOfBurstImages() {
        return numberOfBurstImages;
    }

    public void setNumberOfBurstImages(int numberOfBurstImages) {
        this.numberOfBurstImages = numberOfBurstImages;
    }

    public byte getJpegQuality() {
        return jpegQuality;
    }

    public void setJpegQuality(byte jpegQuality) {
        this.jpegQuality = jpegQuality;
    }

    public int getMeteringMaxTime() {
        return meteringMaxTime;
    }

    public void setMeteringMaxTime(int meteringMaxTime) {
        this.meteringMaxTime = meteringMaxTime;
    }

    public boolean isRunMediascannerAfterCapture() {
        return runMediascannerAfterCapture;
    }

    public void setRunMediascannerAfterCapture(boolean runMediascannerAfterCapture) {
        this.runMediascannerAfterCapture = runMediascannerAfterCapture;
    }

//    static class SerializableRect implements Serializable {
//
//        private static final long serialVersionUID = 1L;
//
//        private Rect mRect;
//
//        public SerializableRect(Rect rect) {
//            mRect = rect;
//        }
//
//        public Rect getRect() {
//            return mRect;
//        }
//
//        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//            int left = mRect.left;
//            int top = mRect.top;
//            int right = mRect.right;
//            int bottom = mRect.bottom;
//
//            out.writeInt(left);
//            out.writeInt(top);
//            out.writeInt(right);
//            out.writeInt(bottom);
//        }
//
//        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//            int left = in.readInt();
//            int top = in.readInt();
//            int right = in.readInt();
//            int bottom = in.readInt();
//
//            mRect = new Rect(left, top, right, bottom);
//        }
//    }
}