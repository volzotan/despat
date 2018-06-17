package de.volzo.despat.preferences;

import android.content.Context;

import java.io.Serializable;

public class CameraConfig implements Serializable {

    private boolean persistentCamera;
    private int shutterInterval;
    private boolean legacyCameraController;

    private boolean formatJpg;
    private boolean formatRaw;

    private boolean endCaptureWithoutUnlockingFocus;
    private int numberOfBurstImages;
    private byte jpegQuality;

    private int meteringMaxTime;
    private boolean runMediascannerAfterCapture;

    public CameraConfig(Context context) {
        this.persistentCamera = Config.getPersistentCamera(context);
        this.shutterInterval = Config.getShutterInterval(context);
        this.legacyCameraController = Config.getLegacyCameraController(context);

        this.formatJpg = Config.FORMAT_JPG;
        this.formatRaw = Config.FORMAT_RAW;

        this.endCaptureWithoutUnlockingFocus = Config.END_CAPTURE_WITHOUT_UNLOCKING_FOCUS;
        this.numberOfBurstImages = Config.NUMBER_OF_BURST_IMAGES;
        this.jpegQuality = Config.JPEG_QUALITY;

        this.meteringMaxTime = Config.METERING_MAX_TIME;
        this.runMediascannerAfterCapture = Config.RUN_MEDIASCANNER_AFTER_CAPTURE;
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
}