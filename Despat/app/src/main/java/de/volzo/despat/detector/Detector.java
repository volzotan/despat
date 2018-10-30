package de.volzo.despat.detector;


import android.content.Context;
import android.graphics.RectF;
import android.util.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.userinterface.DrawSurface;

public abstract class Detector {

    float CONFIDENCE_THRESHOLD = 0.5f;

    public Context context;
    public DetectorConfig detectorConfig;

    public Detector(Context context, DetectorConfig detectorConfig) {
        this.context = context;
        this.detectorConfig = detectorConfig;
    }

    public abstract void init() throws Exception;
    public abstract void load(File fullFilename);
    public abstract List<Recognition> run() throws Exception;
    public abstract void save() throws Exception;
    public abstract void runBenchmark();
    public abstract void display(DrawSurface surface, Size imageSize, List<RectF> rectangles, DetectorConfig detectorConfig);

    public List<RectF> positionsToRectangles(List<Position> results) {

        List<RectF> rectangles = new ArrayList<RectF>();
        for (Position result : results) {
            if (result.getRecognitionConfidence() < CONFIDENCE_THRESHOLD) {
                continue;
            }
            rectangles.add(new RectF(result.getMinx(), result.getMiny(), result.getMaxx(), result.getMaxy()));
        }
        return rectangles;
    }

    public List<RectF> recognitionsToRectangles(List<Detector.Recognition> results) {
        List<RectF> rectangles = new ArrayList<RectF>();
        for (Detector.Recognition result : results) {
            if (result.getConfidence() < CONFIDENCE_THRESHOLD) {
                continue;
            }
            rectangles.add(result.getLocation());
        }
        return rectangles;
    }

    public static class Recognition {

        private final String id;
        private final String title;
        private final int classId;
        private final Float confidence;
        private RectF location;

        public Recognition(final String id, final String title, final int classId, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.classId = classId;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() { return title; }

        public int getClassId() { return classId; }

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
                resultString += String.format(Config.LOCALE, "(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }
}