package de.volzo.despat;

import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 20.12.16.
 */

public class ImageRollover {

    public static String TAG = ImageRollover.class.getSimpleName();

    private File dir;
    private String fileextension;

    public ImageRollover(File dir, String fileextension) {
        this.dir = dir;

        if (fileextension.charAt(0) != '.') {
            this.fileextension = "." + fileextension;
        } else {
            this.fileextension = fileextension;
        }

        float freeSpace = Util.getFreeSpaceOnDevice(dir);
        Log.d(TAG, "free space in " + dir.getAbsolutePath() + " : " + freeSpace + " MB");
    }

    /*
    Check for the highest index in the naming scheme 'n.jpg' and return 'n+1.jpg'
     */
    public String getUnusedFilename() {
        int max = 0;

        File[] listOfFiles = dir.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String name = file.getName();

                if (!name.endsWith(fileextension)) {
                    continue;
                }

                int filename = -1;

                try {
                    filename = Integer.parseInt(name.substring(0, name.length()-fileextension.length()));
                } catch (NumberFormatException nfe) {
                    // do nothing
                }

                max = Math.max(max, filename);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(max+1);
        sb.append(fileextension);

        return sb.toString();
    }

    public boolean fileAlreadyExisting(File f2) {

        File[] listOfFiles = dir.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getAbsolutePath().equals(f2.getAbsolutePath())) {
                    return true;
                }
            }
        }

        return false;
    }

    public File filenamify(String name) {
        File f = new File(this.dir, name + fileextension);

        if (fileAlreadyExisting(f)) {
            for (int i=2; i<9999; i++) {
                f = new File(this.dir,name + "_" + i + fileextension);
                if (!fileAlreadyExisting(f)) {
                    return f;
                }
            }

            return null;
        }

        return f;
    }

    public File getUnusedFullFilename() {
        return new File(this.dir, getUnusedFilename());
    }

    public File getTimestampAsFullFilename() {
        String str = Long.toString(System.currentTimeMillis()) + fileextension;
        return new File(this.dir, str);
    }

    public void run() {
        Log.d(TAG, "imageRollover running");

        if (Util.getFreeSpaceOnDevice(dir) > Config.IMGROLL_FREE_SPACE_THRESHOLD) {
            Log.d(TAG, "rollover: no deletions necessary");
            return;
        }

        File[] filesInDir = dir.listFiles();
        ArrayList<File> imageFiles = new ArrayList<File>();

        for (File file : filesInDir) {
            if (file.isFile()) {
                String name = file.getName();

                if (!name.endsWith(fileextension)) {
                    continue;
                }

                imageFiles.add(file);
            }
        }

        imageFiles.sort(new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                Date dateFile = new Date(file.lastModified());
                Date dateCompare = new Date(t1.lastModified());

                if (dateFile.after(dateCompare)) {
                    return +1;
                } else if (dateCompare.after(dateFile)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        int deleteCounter = 0;
        for (File f : imageFiles) {
            if (Util.getFreeSpaceOnDevice(dir) > Config.IMGROLL_FREE_SPACE_THRESHOLD) {
                Log.d(TAG, "rollover: deletions finished");
                break;
            }

            Log.d(TAG, "delete: " + f.getName());

            deleteCounter++;

            f.delete();
        }

        Log.i(TAG, "deleted " + deleteCounter + " images");
    }

    public int getNumberOfSavedImages() {
        File fl = dir;
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(fileextension);
            }
        });

        return files.length;
    }

    public File getNewestImage() {

        // get last modified file
        // https://stackoverflow.com/questions/285955/java-get-the-newest-file-in-a-directory
        File fl = dir;
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(fileextension);
            }
        });

        if (files == null) return null;

        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }

    public void deleteAll() {
        for(File file: dir.listFiles())
            if (!file.isDirectory())
                if (file.getName().endsWith(fileextension))
                    file.delete();

        Log.i(TAG, "Deleted all image files in " + dir.getAbsolutePath());
    }
}
