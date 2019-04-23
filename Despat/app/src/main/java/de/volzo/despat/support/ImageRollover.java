package de.volzo.despat.support;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.volzo.despat.persistence.Event;
import de.volzo.despat.preferences.Config;

/**
 * Created by volzotan on 20.12.16.
 */

public class ImageRollover {

    public static String TAG = ImageRollover.class.getSimpleName();

    private Context context;

    private File dir;
    private String fileextension;

    public ImageRollover(Context context, String suffix) {
        this.context = context;

        List<File> imageFolders = Config.getImageFolders(context);

        this.dir = imageFolders.get(0);
        File dir_alt = null;

        try {
            dir_alt = imageFolders.get(1);
        } catch (Exception e) {
            Log.wtf(TAG, e);
        }

        if (dir_alt != null && dir_alt != this.dir) {
            long freeSpaceinBytes = Util.getFreeSpaceOnDevice(dir);
            if (freeSpaceinBytes < 0) {
                Log.w(TAG, "could not determine free space in imgroll directoy");
            } else if (freeSpaceinBytes < Config.IMGROLL_FREE_SPACE_THRESHOLD_SWITCH) {
                freeSpaceinBytes = Util.getFreeSpaceOnDevice(dir_alt);
                if (freeSpaceinBytes < 0) {
                    Log.w(TAG, "could not determine free space in alternative imgroll directoy");
                } else if (freeSpaceinBytes > Config.IMGROLL_FREE_SPACE_THRESHOLD_SWITCH) {
                    this.dir = dir_alt;
                    Log.i(TAG, "switched to alt directory");
                }
            }
        }

        Log.wtf(TAG, this.dir.getAbsolutePath());

        if (suffix == null) suffix = ".jpg";

        if (suffix.charAt(0) != '.') {
            this.fileextension = "." + suffix;
        } else {
            this.fileextension = suffix;
        }

        // create directory
        this.dir.mkdirs();

//        float freeSpace = Util.getFreeSpaceOnDevice(dir);
//        Log.d(TAG, "free space in " + dir.getAbsolutePath() + " : " + freeSpace + " MB");
    }

    /*
     *  Check for the highest index in the naming scheme 'n.jpg' and return 'n+1.jpg'
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

    public File getTimestampAsFullFilename(String filenameSuffix) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        if (filenameSuffix != null) {
            sb.append(filenameSuffix);
        }
        sb.append(fileextension);
        return new File(this.dir, sb.toString());
    }

    public void run() {

        // Caveat: file.delete() doesn't free up the space immediately
        // so the ImageRollover may delete more than necessary across multiple runs

        if (!Config.IMGROLL_DELETE_IF_FULL) {
            Log.d(TAG, "imageRollover is disabled.");
            return;
        }

        Log.d(TAG, "imageRollover running");

        long freeSpace = Util.getFreeSpaceOnDevice(dir);
        long diff = ((long) Config.IMGROLL_FREE_SPACE_THRESHOLD_DELETE) * 1024 * 1024 - freeSpace;

        float freeSpaceMB = freeSpace / (1024.f * 1024.f);
        float diffMB = diff / (1024.f * 1024.f);

        if (diff < 0) {
            Log.d(TAG, String.format(Config.LOCALE, "rollover: no deletions necessary. free space: %.2f MB | difference: %.2f MB", freeSpaceMB, diffMB));
            return;
        } else {
            Log.d(TAG, String.format(Config.LOCALE, "deletion needed. free space: %.2f | difference: %.2f MB", freeSpaceMB, diffMB));
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

        if (android.os.Build.VERSION.SDK_INT >= 24) {
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
        } else {
            Log.w(TAG, "deleting in unsorted order");
        }

        int deleteCounter = 0;
        long deletedBytes = 0;
        for (File f : imageFiles) {
            if (deletedBytes >= diff) {
            //if (Util.getFreeSpaceOnDevice(dir) > Config.IMGROLL_FREE_SPACE_THRESHOLD) {
                Log.d(TAG, "rollover: deletions finished");
                break;
            }

            Log.d(TAG, "delete: " + f.getName());

            deleteCounter++;
            deletedBytes += f.length();

            try {
                boolean success = f.getCanonicalFile().delete();
                if (!success) {
                    Log.d(TAG, "unknown problem deleting file");
                }
            } catch (IOException e) {
                Log.d(TAG, "problem deleting file", e);
            }
        }

        Log.i(TAG, String.format(Config.LOCALE, "deleted %d images (%d bytes)", deleteCounter, deletedBytes));

        if (deleteCounter > 3) {
            Util.saveEvent(context, Event.EventType.INFO, "deleted images: " + deleteCounter);
        }
    }

    public int getNumberOfSavedImages() {
        File fl = dir;
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(fileextension);
            }
        });

        if (files == null) {
            return 0;
        } else {
            return files.length;
        }
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
