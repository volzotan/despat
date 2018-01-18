package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 12.01.18.
 */

public class RecordingSession {

    public static final String TAG = RecordingSession.class.getSimpleName();

    private static volatile RecordingSession instance;
    private Context context;
    private Session session;

    //private constructor.
    private RecordingSession(){

        //Prevent form the reflection api.
        if (instance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static RecordingSession getInstance(Context context) {
        if (instance == null) { //if there is no instance available... create new one
            synchronized (RecordingSession.class) {
                if (instance == null) {
                    instance = new RecordingSession();

                    // TODO:
                    // There is the potential for fuckup here:
                    // if the context is the activity and the RecordingSession-object
                    // lives long enough to be called by the ShutterService (which is its own context)
                    // there may be two different contextes at work
                    instance.context = context;
                }
            }
        }

        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------

    public void startRecordingSession(String sessionName) {
        if (session != null) {
            stopRecordingSession();
        }

        if (sessionName == null || sessionName.isEmpty()) {
            // TODO: Do the animal name thingy here
            sessionName = UUID.randomUUID().toString();
        }

        Log.d(TAG, "init new RecordingSession [" + sessionName + "]");

        Despat despat = Util.getDespat(context);

        Session session = new Session();
        session.setSessionName(sessionName);
        session.setStart(Calendar.getInstance().getTime());
        session.setLocation(null);

        AppDatabase db = AppDatabase.getAppDatabase(despat);
        SessionDao sessionDao = db.sessionDao();
        sessionDao.insertAll(session);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
        context.sendBroadcast(shutterIntent);

        SystemController systemController = despat.getSystemController();
        systemController.getLocation(new SystemController.LocationCallback() {
            @Override
            public void locationAcquired(Location location) {
                // TODO
            }
        });
    }

    public void stopRecordingSession() {
        String sessionName = "TODO"; // TODO

        Log.d(TAG, "stop RecordingSession [" + sessionName + "]");

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
        context.sendBroadcast(shutterIntent);

        session = null;
    }

    public boolean isActive() {
        if (session == null) {
            return false;
        }

        return true;
    }

    public void addCapture(File image) throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();

        Capture capture = new Capture();
        capture.setSessionId(session.getSid());
        capture.setRecordingTime(Calendar.getInstance().getTime());
        capture.setImage(image);

        Despat despat = Util.getDespat(context);
        AppDatabase db = AppDatabase.getAppDatabase(despat);
        CaptureDao captureDao = db.captureDao();
        captureDao.insertAll(capture);
    }

    public void setLocation(Location location) {
        // check if a session is active
        // update location
    }

    public int getImagesTaken() {
        if (!isActive()) return -1;

        // TODO
        return -1;
    }

    public String getRecordingSessionId() {
        return null; // TODO
    }

    public void checkForIntegrity() {

        // TODO:

        // check the DB if shutter events have occurred at the timed interval
        // or if android suppressed the alarm manager

    }



    public class NotRecordingException extends Exception {

    }
}