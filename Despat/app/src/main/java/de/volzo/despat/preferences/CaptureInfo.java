package de.volzo.despat.preferences;

import java.io.Serializable;

public class CaptureInfo implements Serializable {
    private String filename;
    private int sequenceNumber = -1;
    private long exposureTime = -1;
    private double aperture = -1;
    private int iso = -1;
    private int autofocusState = -1;

    public CaptureInfo(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(long exposureTime) {
        this.exposureTime = exposureTime;
    }

    public double getAperture() {
        return aperture;
    }

    public void setAperture(double aperture) {
        this.aperture = aperture;
    }

    public int getIso() {
        return iso;
    }

    public void setIso(int iso) {
        this.iso = iso;
    }

    public int getAutofocusState() {
        return autofocusState;
    }

    public void setAutofocusState(int autofocusState) {
        this.autofocusState = autofocusState;
    }
}