package de.volzo.despat;

import android.util.Log;

import java.io.File;

/**
 * Created by volzotan on 20.12.16.
 */

public class ImageRollover {

    public static String TAG = ImageRollover.class.getName();

    private int threshold = 100;
    private boolean enabled = false;
    private File dir;

    public ImageRollover(File dir) {
        this.dir = dir;
    }

    public void run() {
        Log.d(TAG, "imageRollover running");

        // TODO

        // list files in dir
        // filter for file ending
        // derive date from filename / exif
        // delete oldest images till image count < threshold

        // alternative: delete images older than $Date
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) run();
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;

        run();
    }

}
