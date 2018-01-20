package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.EventDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
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
            try {
                stopRecordingSession();
            } catch (NotRecordingException e) {
                Log.w(TAG, "stopping old session failed", e);
            }
        }

        if (sessionName == null || sessionName.isEmpty()) {
            // TODO: Do the animal name thingy here
            sessionName = UUID.randomUUID().toString();
        }

        Log.d(TAG, "init new RecordingSession [" + sessionName + "]");

        Despat despat = Util.getDespat(context);

        session = new Session();
        session.setSessionName(sessionName);
        session.setStart(Calendar.getInstance().getTime());
        session.setLocation(null);

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        long[] ids = sessionDao.insert(session);

        session = sessionDao.getById(ids[0]);
        // TODO: only id is autogenerated, no need to fetch whole object

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

    public void stopRecordingSession() throws NotRecordingException {
        Log.d(TAG, "stop RecordingSession [" + session.getSessionName() + "]");

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_STOP);
        context.sendBroadcast(shutterIntent);

        if (!isActive()) throw new NotRecordingException();

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

        AppDatabase db = AppDatabase.getAppDatabase(context);
        CaptureDao captureDao = db.captureDao();

        // check if last capture from the same session is not more than
        // X seconds in the past, otherwise create error event

        Capture lastCap = captureDao.getLastFromSession(session.getSid());
        if (lastCap != null) {
            Date now = Calendar.getInstance().getTime();
            long diff = now.getTime() - lastCap.getRecordingTime().getTime();

            long maxDiff = Config.getShutterInterval(context) * 1000 + 3 * 1000;
            if (diff > maxDiff) {
                Event event = new Event();
                event.setType(Event.ERROR);
                event.setPayload("irregular capture pattern");
                EventDao eventDao = db.eventDao();
                eventDao.insert(event);
            }
        }

        Capture capture = new Capture();
        capture.setSessionId(session.getSid());
        capture.setRecordingTime(Calendar.getInstance().getTime());
        capture.setImage(image);

        captureDao.insert(capture);
    }

    public int getImagesTaken() {
        if (!isActive()) return -1;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        int numberImagesTaken = sessionDao.getNumberOfCaptures(session.getSid());

        return numberImagesTaken;
    }

    public boolean checkForIntegrity() {

        // check the DB if shutter events have occurred at the timed interval
        // or if android suppressed the alarm manager
        // (assumes that the RecordingSession is still running and checks no stop date)

        AppDatabase db = AppDatabase.getAppDatabase(context);
        CaptureDao captureDao = db.captureDao();

        List<Capture> captures = captureDao.getAllBySession(session.getSid());

        long maxTimeDiff = Config.getShutterInterval(context) * 1000 + 3 * 1000;
        Date comp = session.getStart();
        for (Capture cap : captures) {
            long diff = cap.getRecordingTime().getTime() - comp.getTime();

            if (diff > maxTimeDiff) {
                return false;
            }

            comp = cap.getRecordingTime();
        }

        return true;
    }

    public String getSessionName() throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();
        return session.getSessionName();
    }

    public Date getStart() throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();
        return session.getStart();
    }

    public Date getEnd() throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();
        return session.getEnd();
    }

    public void setLocation(Location location) throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();

        session.setLocation(location);


        // update location
    }

    // ---------------------------------------------------------------------------------------------

    public class NotRecordingException extends Exception {

    }
}