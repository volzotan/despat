package de.volzo.despat.detector;


import android.graphics.RectF;

import java.io.File;
import java.util.List;

import de.volzo.despat.DrawSurface;

public abstract class Detector {

    public abstract void init() throws Exception;
    public abstract void load(File fullFilename);
    public abstract List<Recognition> run() throws Exception;
    public abstract void save() throws Exception;
    public abstract void display(DrawSurface surface, List<Recognition> results);

    public static class Recognition {

        private final String id;
        private final String title;
        private final Float confidence;
        private RectF location;

        public Recognition(final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() { return title; }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }
}