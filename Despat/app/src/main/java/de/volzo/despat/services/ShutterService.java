package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends IntentService {

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String dataString = workIntent.getDataString();

        // ...

        Intent localIntent = new Intent(Broadcast.PICTURE_TAKEN)
                .putExtra(Broadcast.DATA_PICTURE_PATH, "narf");
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
