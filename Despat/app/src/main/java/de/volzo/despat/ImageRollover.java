package de.volzo.despat;

import android.os.StatFs;
import android.util.Log;

import java.io.File;

import de.volzo.despat.support.Util;

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
        float freeSpace = Util.getFreeSpaceOnDevice(dir);

        Log.i(TAG, "free space in " + dir.getAbsolutePath() + " : " + freeSpace + " MB");
    }

    public String getUnusedFilename(String fileextension) {
        int max = 0;

        if (fileextension.charAt(0) != '.') {
            fileextension = "." + fileextension;
        }

        File[] listOfFiles = dir.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String name = file.getName();

                if (!name.endsWith(fileextension)) {
                    continue;
                }

                int index = -1;

                try {
                    index = Integer.parseInt(name.substring(0, name.length()-fileextension.length()));
                } catch (NumberFormatException nfe) {
                    // do nothing
                }

                max = Math.max(max, index);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(max);
        sb.append(fileextension);

        return sb.toString();
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
