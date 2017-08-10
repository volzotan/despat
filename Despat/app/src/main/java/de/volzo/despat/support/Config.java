package de.volzo.despat.support;


import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by volzotan on 20.12.16.
 */

public class Config {

    public static final String TAG = Config.class.getName();

    public static final File IMAGE_FOLDER = new File(Environment.getExternalStorageDirectory(), ("despat"));
    public static final String SERVER_ADDRESS = "http://grinzold.de:9000";

    /*
    image folder
    store images?
    image rollover number

    powerbrain
        usb device id
        baud rate

    intervals
        image taking interval
        server reporting interval

    */

    public static void init() {
        // check if all folders are existing
        if (!IMAGE_FOLDER.isDirectory()) {
            // not existing. create
            Log.i(TAG, "Directory IMAGE_FOLDER ( " + IMAGE_FOLDER.getAbsolutePath() + " ) missing. creating...");
            IMAGE_FOLDER.mkdirs();
        }

        // ...
    }
}
