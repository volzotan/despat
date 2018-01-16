package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 12.01.18.
 */

public class RecordingSession {

    public static final String TAG = RecordingSession.class.getSimpleName();

    public static void startRecordingSession(Context context) {
        String sessionName = UUID.randomUUID().toString();

        Log.d(TAG, "init new RecordingSession [" + sessionName + "]");

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
        context.sendBroadcast(shutterIntent);
    }

    public static String getRecordingSessionId() {
        return null; // TODO
    }

    public static void checkForIntegrity() {

        // TODO:

        // check the DB if shutter events have occurred at the timed interval
        // or if android suppressed the alarm manager

    }
}
