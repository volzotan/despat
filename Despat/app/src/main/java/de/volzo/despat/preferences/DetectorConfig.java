package de.volzo.despat.preferences;

import android.content.Context;
import android.graphics.Rect;

import java.io.IOException;
import java.io.Serializable;

public class DetectorConfig implements Serializable {

    private String detector;
    private int tilesize;

    public DetectorConfig(String detector, int tilesize) {
        this.detector = detector;
        this.tilesize = tilesize;
    }

    public String getDetector() {
        return detector;
    }

    public void setDetector(String detector) {
        this.detector = detector;
    }

    public int getTilesize() {
        return tilesize;
    }

    public void setTilesize(int tilesize) {
        this.tilesize = tilesize;
    }
}