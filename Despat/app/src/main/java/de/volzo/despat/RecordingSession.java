package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.services.CompressorService;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.services.RecognitionService;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 12.01.18.
 */

public class RecordingSession {

    private static final String TAG = RecordingSession.class.getSimpleName();

    private static volatile RecordingSession instance;
    private Context context;
    private Session session;

    private static final int RESUME_MAX_AGE_LAST_CAPTURE = 5 * 60 * 1000;

    //private constructor.
    private RecordingSession(Context context){

        //Prevent form the reflection api.
        if (instance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        this.context = context;

        // reopen session?

        if (!Util.isServiceRunning(context, ShutterService.class)) return;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        session = sessionDao.getLast();

        if (session == null) return;

        // if the newest session was closed, don't reopen it.
        if (session.getEnd() != null) {
            session = null;
        } else {
            Log.i(TAG, "reopened session: " + session.getSessionName());
        }
    }

    public static RecordingSession getInstance(Context context) {
        if (instance == null) { //if there is no instance available... create new one
            synchronized (RecordingSession.class) {
                if (instance == null) {
                    instance = new RecordingSession(context);
                }
            }
        }

        // TODO:
        // the RecordingSession Singleton may be killed at any time during normal
        // operation of the ShutterService, but still the session variable should
        // be "repopulated" once the instance will be required again
        // just use resume session?

        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------

    public void startRecordingSession(String sessionName, CameraConfig cameraConfig) {
        if (session != null) {
            try {
                stopRecordingSession();
            } catch (NotRecordingException e) {
                Log.w(TAG, "stopping old session failed", e);
            }
        }

        if (sessionName == null || sessionName.isEmpty()) {
            // TODO: Do the animal name thingy here
            sessionName = Util.getMostlyUniqueRandomString(context);
        }

        Log.d(TAG, "init new RecordingSession [" + sessionName + "]");

        Intent heartbeatIntent = new Intent(context, Orchestrator.class);
        heartbeatIntent.putExtra(Orchestrator.SERVICE, Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
        context.sendBroadcast(heartbeatIntent);

        Despat despat = Util.getDespat(context);

        session = new Session();
        session.setSessionName(sessionName);
        session.setStart(Calendar.getInstance().getTime());
        session.setLocation(null);

        // TODO: add logic here for exclusion parts of the image and the resulting imageSize

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        final long[] ids = sessionDao.insert(session);
        session.setId(ids[0]);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
        Bundle args = new Bundle();
        args.putSerializable(Orchestrator.DATA_CAMERA_CONFIG, cameraConfig);
        shutterIntent.putExtras(args);
        context.sendBroadcast(shutterIntent);

        SystemController systemController = despat.getSystemController();
        systemController.getLocation(new SystemController.LocationCallback() {
            @Override
            public void locationAcquired(Location location) {

                try {
                    RecordingSession.getInstance(context).setLocation(location);
                } catch (NotRecordingException e) {
                    Log.w(TAG, "session already stopped when adding location");
                }

            }
        });

        Util.saveEvent(context, Event.EventType.SESSION_START, session.getSessionName());
    }

    public void resumeRecordingSession() throws Exception {
        if (session != null) {
            throw new Exception("Session still recording");
        }

        // Conditions for a resume on boot
        // - config option "ResumeAfterReboot" must be set
        // - newest RecordingSession has no end date / has end date in future
        // - newest Capture is younger than 5min

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        Session lastSession = sessionDao.getLast();

        if (lastSession == null) {
            Log.w(TAG, "no sessions found for resuming");
            throw new Exception("no sessions found");
        }

        if (lastSession.getEnd() != null) {
//            Log.d(TAG, "session resume, end date reset");
//            session.setEnd(null);
            Log.w(TAG, "newest session has ended (end date set)");
            throw new Exception("last session has already ended");
        }

        Capture lastCapture = captureDao.getLastFromSession(lastSession.getId());
        if (lastCapture == null || lastCapture.getRecordingTime() == null) {
            Log.w(TAG, "newest session has no captures");
            throw new Exception("newest session has no captures");
        }

        long diff = Calendar.getInstance().getTime().getTime() - lastCapture.getRecordingTime().getTime();
        if (diff > RESUME_MAX_AGE_LAST_CAPTURE) {
            Log.w(TAG, "last capture too old");
            throw new Exception("last capture too old");
        }

        session = lastSession;
        session.setResumed(true);
        sessionDao.update(session);

        Log.i(TAG, "resume RecordingSession [" + session.getSessionName() + "]");

        Intent heartbeatIntent = new Intent(context, Orchestrator.class);
        heartbeatIntent.putExtra(Orchestrator.SERVICE, Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
        context.sendBroadcast(heartbeatIntent);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
        context.sendBroadcast(shutterIntent);

        Util.saveEvent(context, Event.EventType.SESSION_RESTART, session.getSessionName());
    }


    public void stopRecordingSession() throws NotRecordingException {
        stopRecordingSession(null);
    }

    public void stopRecordingSession(String reason) throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();

        Log.d(TAG, "stop RecordingSession [" + session.getSessionName() + "]");

        Intent heartbeatIntent = new Intent(context, Orchestrator.class);
        heartbeatIntent.putExtra(Orchestrator.SERVICE, Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
        context.sendBroadcast(heartbeatIntent);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
        if (reason != null) {
            shutterIntent.putExtra(Orchestrator.REASON, reason);
        }
        context.sendBroadcast(shutterIntent);

        Orchestrator.runCompressorService(context);

        session.setEnd(Calendar.getInstance().getTime());

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        sessionDao.update(session);

        Util.saveEvent(context, Event.EventType.SESSION_STOP, session.getSessionName());

        session = null;

        runMaintenance();
    }

    public static void deleteSessionFromDatabase(Context context, Session session) {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        List<Capture> captures = captureDao.getAllBySession(session.getId());

        for (Capture c : captures) {
            // TODO: check if image exists in memory and delete it
        }

        sessionDao.delete(session);
    }

    public void runMaintenance() {

//        if (session != null) {
//            Intent detectorIntent = new Intent(context, RecognitionService.class);
//            detectorIntent.putExtra(RecognitionService.SESSION_ID, session.getId());
//            context.startService(detectorIntent);
//        }
//
//        Intent compressorIntent = new Intent(context, Compressor.class);
//        context.startService(compressorIntent);
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
        SessionDao sessionDao = db.sessionDao();

        // check if last capture from the same session is not more than
        // X seconds in the past, otherwise create error event

        Capture lastCap = captureDao.getLastFromSession(session.getId());
        if (lastCap != null) {
            Date now = Calendar.getInstance().getTime();
            long diff = now.getTime() - lastCap.getRecordingTime().getTime();
            long maxDiff = Config.getShutterInterval(context) + 3 * 1000;
            if (diff > maxDiff) {
                Util.saveEvent(context, Event.EventType.SCHEDULE_GLITCH, null);
            }
        }

        // TODO: not the perfect location to actually run this code:
        // check if we find a captured image to load and extract the image dimensions
        if (session.getImageSize() == null) {
            Bitmap loadedImage = null;
            if (image.exists()) {
                loadedImage = BitmapFactory.decodeFile(image.getAbsolutePath());
            } else {
                Capture lastCapture = captureDao.getLastFromSession(session.getId());
                if (lastCapture != null) {
                    if (lastCapture.getImage() != null && lastCapture.getImage().exists()) {
                        loadedImage = BitmapFactory.decodeFile(lastCapture.getImage().getAbsolutePath());
                    }
                }
            }
            if (loadedImage != null) {
                session.setImageSize(new Size(loadedImage.getWidth(), loadedImage.getHeight()));
                sessionDao.update(session);
            }
        }

        Capture capture = new Capture();
        capture.setSessionId(session.getId());
        capture.setRecordingTime(Calendar.getInstance().getTime());
        capture.setProcessed(false);
        capture.setImage(image);

        captureDao.insert(capture);
    }

    public void addError(String desc, Throwable e) throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();

        Util.saveErrorEvent(context, session.getId(), desc, e);
    }

    public int getImagesTaken() {
        if (!isActive()) return -1;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        int numberImagesTaken = sessionDao.getNumberOfCaptures(session.getId());

        return numberImagesTaken;
    }

    public int getErrors() {
        if (!isActive()) return -1;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        int numberErrors = sessionDao.getNumberOfErrors(session.getId());

        return numberErrors;
    }

    // ---------------------------------------------------------------------------------------------

    public static Status getMaxTemperatureDuringSession(Context context, Session session) throws Exception {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        CaptureDao captureDao = db.captureDao();
        StatusDao statusDao = db.statusDao();

        Date start = session.getStart();
        Date end = session.getEnd();

        if (end == null) {
            Log.w(TAG, "session end date missing. substituting by last capture date");

            Capture lastCapture = captureDao.getLastFromSession(session.getId());
            if (lastCapture == null) {
                throw new Exception("session has no end date and no captures");
            }

            end = lastCapture.getRecordingTime();
        }

        List<Status> statuses = statusDao.getAllBetween(start, end);

        Status maxTempStatus = null;
        for (Status s : statuses) {
            if (maxTempStatus == null) maxTempStatus = s;

            if (maxTempStatus.getTemperatureBattery() < s.getTemperatureBattery()) {
                maxTempStatus =s;
            }
        }

        return maxTempStatus;
    }

    public static void checkAllForIntegrity(Context context) {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        List<Session> sessions = sessionDao.getAll();

        for (Session session : sessions) {
            String res = RecordingSession.checkForIntegrity(context, session);
            boolean noGlitch = true;

            if (res != null && res.length() > 0) noGlitch = false;

            if (noGlitch) {
                Log.i(TAG, "session " + session.toString() + " has no glitch");
            } else {
                Log.i(TAG, res);
                Log.i(TAG, "session " + session.toString() + " has glitches");
            }
        }
    }

    public static String checkForIntegrity(Context context, Session session) {

        // check the DB if shutter events have occurred at the timed interval
        // or if android suppressed the alarm manager
        // (assumes that the RecordingSession is still running and checks no stop date)

        AppDatabase db = AppDatabase.getAppDatabase(context);
        CaptureDao captureDao = db.captureDao();

        List<Capture> captures = captureDao.getAllBySession(session.getId());

        boolean result = true;

        long maxTimeDiff = 1000;
        Date comp = session.getStart();

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        StringBuilder sb = new StringBuilder("\n");

        for (int i=0; i<captures.size(); i++) {
            long diff = captures.get(i).getRecordingTime().getTime() - comp.getTime() - Config.getShutterInterval(context);

            if (diff > maxTimeDiff) {
                result = false;
                sb.append(String.format(Config.LOCALE, "%s | schedule glitch at capture %d (diff: %d)\n", df.format(captures.get(i).getRecordingTime().getTime()), i, diff));
            }

            comp = captures.get(i).getRecordingTime();
        }

//        System.out.println(sb.toString());

        return sb.toString();
    }

    // ---------------------------------------------------------------------------------------------

    public long getSessionId() throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();
        return session.getId();
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

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        sessionDao.update(session);
    }

    // ---------------------------------------------------------------------------------------------

    public class NotRecordingException extends Exception {

    }
}