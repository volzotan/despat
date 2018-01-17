package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

import de.volzo.despat.persistence.Session;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 12.01.18.
 */

public class RecordingSession {

    public static final String TAG = RecordingSession.class.getSimpleName();

    public static void startRecordingSession(Context context) {
        String sessionName = UUID.randomUUID().toString();

        Log.d(TAG, "init new RecordingSession [" + sessionName + "]");

        Session session = new Session();
        session.setSessionName(sessionName);
        session.setStart(Calendar.getInstance().getTime());
        session.setPosition(null);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
        context.sendBroadcast(shutterIntent);

        Despat despat = Util.getDespat(context);
        SystemController systemController = despat.getSystemController();
        systemController.getLocation(new SystemController.LocationCallback() {
            @Override
            public void locationAcquired(Location location) {
                // TODO
            }
        });
    }

    public static void setLocation(Location location) {
        // check if a session is active
        // update location
    }

    public static void stopRecordingSession(Context context) {
        String sessionName = "TODO"; // TODO

        Log.d(TAG, "stop RecordingSession [" + sessionName + "]");

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
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
