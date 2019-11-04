package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.location.Location;
import android.util.Log;
import android.util.Size;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.DeviceLocation;
import de.volzo.despat.persistence.DeviceLocationDao;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;

public class SessionManager {

    private static final String TAG = SessionManager.class.getSimpleName();

    private static volatile SessionManager instance;
    private Context context;
    private Session session;

    private static final int RESUME_MAX_AGE_LAST_CAPTURE = 5 * 60 * 1000;

    //private constructor.
    private SessionManager(Context context){

        //Prevent from the reflection api.
        if (instance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        this.context = context;

        // reopen/reload session?

        if (!Util.isServiceRunning(context, ShutterService.class)) return;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        session = sessionDao.getLast();

        if (session == null) return;

        // if the newest session was closed, don't reopen it.
        if (session.getEnd() != null) {
            session = null;
        } else {
            Log.i(TAG, "reopened/reloaded session: " + session.getSessionName());
        }
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) { //if there is no instance available... create new one
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }

        // TODO:
        // the SessionManager Singleton may be killed at any time during normal
        // operation of the ShutterService, but still the session variable should
        // be "repopulated" once the instance will be required again
        // just use resume session?

        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------

    public void startRecordingSession(String sessionName, CameraConfig cameraConfig, DetectorConfig detectorConfig) {
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

        Intent recognitionIntent = new Intent(context, Orchestrator.class);
        recognitionIntent.putExtra(Orchestrator.SERVICE, Broadcast.RECOGNITION_SERVICE);
        recognitionIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
        context.sendBroadcast(recognitionIntent);

        if (Config.getPhoneHome(context)) {
            Intent uploadIntent = new Intent(context, Orchestrator.class);
            uploadIntent.putExtra(Orchestrator.SERVICE, Broadcast.UPLOAD_SERVICE);
            uploadIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
            context.sendBroadcast(uploadIntent);
        }

        Despat despat = Util.getDespat(context);

        session = new Session();
        session.setSessionName(sessionName);
        session.setStart(Calendar.getInstance().getTime());
        session.setLocation(null);

        session.setCameraConfig(cameraConfig);
        // TODO: tilesize?
        session.setDetectorConfig(detectorConfig);

        // TODO: add logic here for zoom, exclusion parts and the resulting imageSize
        try {
            Rect zoomRegion = cameraConfig.getZoomRegion();
            if (zoomRegion != null) {
                session.setImageSize(new Size(zoomRegion.width(), zoomRegion.height()));
            } else {
                session.setImageSize(CameraController2.getImageSize(context, cameraConfig.getCameraDevice()));
            }
        } catch (Exception e) {
            Log.w(TAG, "unable to set image size in session");
        }

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        final long[] ids = sessionDao.insert(session);
        session.setId(ids[0]);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
//        Bundle args = new Bundle();
//        args.putSerializable(Orchestrator.DATA_CAMERA_CONFIG, cameraConfig);
//        shutterIntent.putExtras(args);
        context.sendBroadcast(shutterIntent);

        SystemController systemController = despat.getSystemController();
        systemController.getLocation(new SystemController.LocationCallback() {
            @Override
            public void locationAcquired(Location location) {

                try {
                    AppDatabase db = AppDatabase.getAppDatabase(context);
                    DeviceLocationDao deviceLocationDao = db.deviceLocationDao();
                    DeviceLocation loc = new DeviceLocation();

                    loc.setTimestamp(Calendar.getInstance().getTime());
                    loc.setLocation(location);

                    deviceLocationDao.insert(loc);

                    SessionManager.getInstance(context).setLocation(location);
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

        Log.i(TAG, "stop RecordingSession [" + session.getSessionName() + "]");

        Intent heartbeatIntent = new Intent(context, Orchestrator.class);
        heartbeatIntent.putExtra(Orchestrator.SERVICE, Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
        context.sendBroadcast(heartbeatIntent);

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
        shutterIntent.putExtra(Orchestrator.ARG_SESSION_ID, session.getId());
        if (reason != null) {
            shutterIntent.putExtra(Orchestrator.REASON, reason);
        }
        context.sendBroadcast(shutterIntent);

        session.setEnd(Calendar.getInstance().getTime());

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        sessionDao.update(session);

        Orchestrator.runCompressorService(context);

        Util.saveEvent(context, Event.EventType.SESSION_STOP, session.getSessionName());

        session = null;

        runMaintenance();
    }

    public static void deleteSessionFromDatabase(Context context, Session session) {
        Log.d(TAG, "deleting session: " + session.toString());

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        List<Capture> captures = captureDao.getAllBySession(session.getId());

        for (Capture c : captures) {
            // TODO: check if image exists in memory and delete it
        }

        // TODO: delete compressed image (store and jpg)
        // Delete ZIP (is it in ./temp ?)

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

    public void addCapture(CaptureInfo info) throws NotRecordingException {
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

            StringBuilder sb = new StringBuilder();
            sb.append("diff: ");
            sb.append(diff);

            if (diff > maxDiff) {
                Util.saveEvent(context, Event.EventType.SCHEDULE_GLITCH, sb.toString());
            }
        }

        // TODO: not the perfect location to actually run this code:
        // check if we find a captured image to load and extract the image dimensions
//        if (session.getImageSize() == null) {
//            Bitmap loadedImage = null;
//            if (image.exists()) {
//                loadedImage = BitmapFactory.decodeFile(image.getAbsolutePath());
//            } else {
//                Capture lastCapture = captureDao.getLastFromSession(session.getId());
//                if (lastCapture != null) {
//                    if (lastCapture.getImage() != null && lastCapture.getImage().exists()) {
//                        loadedImage = BitmapFactory.decodeFile(lastCapture.getImage().getAbsolutePath());
//                    }
//                }
//            }
//            if (loadedImage != null) {
//                session.setImageSize(new Size(loadedImage.getWidth(), loadedImage.getHeight()));
//                sessionDao.update(session);
//            }
//        }

        Capture capture = new Capture();
        capture.setSessionId(session.getId());
        capture.setRecordingTime(Calendar.getInstance().getTime());
        capture.setExposureTime(info.getExposureTime());
        capture.setAperture(info.getAperture());
        capture.setIso(info.getIso());
        capture.setAutofocusState(info.getAutofocusState());
        capture.setProcessed_detector(false);
        capture.setProcessed_compressor(false);
        capture.setImage(new File(info.getFilename()));

        captureDao.insert(capture);

        Log.d(TAG, "capture added");
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

    public Session createDummyData() {
        Session dummy = new Session();

        dummy.setId(ThreadLocalRandom.current().nextInt(1000, 10000 + 1));
        dummy.setSessionName(Util.getMostlyUniqueRandomString(context));

        // TODO: set start, end, etc.
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.MINUTE, -ThreadLocalRandom.current().nextInt(10, 100 + 1));
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.MINUTE, -ThreadLocalRandom.current().nextInt(0, 9 + 1));

        dummy.setStart(startCal.getTime());
        dummy.setEnd(endCal.getTime());

        dummy.setCompressedImage(null);

        return dummy;
    }

    public void createExampleSession(Context context) {
        AppDatabase database = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = database.sessionDao();
        HomographyPointDao pointDao = database.homographyPointDao();
        CaptureDao captureDao = database.captureDao();
        PositionDao positionDao = database.positionDao();

        List<Session> existingSessions = sessionDao.getAll();
        for (Session session : existingSessions) {
            if (session.getSessionName() != null && session.getSessionName().equals("Example Dataset")) {
                Log.w(TAG, "Example Dataset already existing. Not creating new one.");
                return;
            }
        }

        Session example = new Session();

        example.setSessionName("Example Dataset");
        example.setStart(new Date((long) 0));
        example.setEnd(new Date((long) 60*60*1000));

        example.setLatitude(50.971040);
        example.setLongitude(11.038093);

        try {
            File imageFolder = Config.getImageFolders(context).get(0);
            File compressedImage = new File(imageFolder, example.getSessionName() + ".jpg");
            Util.copyAssets(context, "exampledataset_compressedimage.jpg", compressedImage);
            example.setCompressedImage(compressedImage);
        } catch (Exception e) {
            Log.w(TAG, "copying compressed image for example dataset failed", e);
        }

        example.setImageWidth(5952);
        example.setImageHeight(3348);

        CameraConfig cameraConfig = new CameraConfig();
        cameraConfig.setShutterInterval(10000);
        example.setCameraConfig(cameraConfig);

        DetectorConfig detectorConfig = new DetectorConfig(DetectorTensorFlowMobile.FIDELITY_MODE[0], 1000);
        example.setDetectorConfig(detectorConfig);

        Long sessionId = sessionDao.insert(example)[0];

        List<HomographyPoint> points = new ArrayList<>();
        points.add(new HomographyPoint(1124, 1416, 50.971296, 11.037630));
        points.add(new HomographyPoint(1773, 2470, 50.971173, 11.037914));
        points.add(new HomographyPoint(3785, 1267, 50.971456, 11.037915));
        points.add(new HomographyPoint(3416, 928, 50.971705, 11.037711));
        points.add(new HomographyPoint(2856, 1303, 50.971402, 11.037796));
        points.add(new HomographyPoint(2452, 916, 50.971636, 11.037486));

        for (HomographyPoint p : points) {
            p.setSessionId(sessionId);
            pointDao.insert(p);
        }

        Capture dummyCapture = new Capture();
        dummyCapture.setSessionId(sessionId);
        dummyCapture.setProcessed_compressor(true);
        dummyCapture.setProcessed_detector(true);
        dummyCapture.setRecordingTime(new Date((long) 0 + cameraConfig.getShutterInterval()));
        Long captureId = captureDao.insert(dummyCapture)[0];

        AssetManager assetManager = context.getAssets();

//        String datFilename = "exampledataset.dat";
//        try {
//            FileInputStream fis = new FileInputStream(assetManager.open(datFilename));
//
//            short[] data = new short[1];
//            byte[] binary = new byte[2];
//            int ret = 0;
//            while (true) {
//                ret = fis.read(binary);
//
//                if (ret <= 0) {
//                    break;
//                }
//
//                data[0] = (short) ((binary[1] << 8) + binary[0]);
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "loading failed", e);
//        }

        String csvFilename = "exampledataset.csv";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(assetManager.open(csvFilename), "UTF-8"));

            int count = 0;
            String line;
            Position pos;
            String[] data;
            while ((line = reader.readLine()) != null) {
                if (count == 0) {
                    count++;
                    continue;
                }

                pos = new Position();
                pos.setCaptureId(captureId);

                data = line.split("\\|");

                try {
                    pos.setTypeId(Integer.parseInt(data[2]));
                    pos.setRecognitionConfidence(Float.parseFloat(data[3]));
                    pos.setLatitude(Double.parseDouble(data[4]));
                    pos.setLongitude(Double.parseDouble(data[5]));
                    pos.setMinx(Float.parseFloat(data[6]));
                    pos.setMiny(Float.parseFloat(data[7]));
                    pos.setMaxx(Float.parseFloat(data[8]));
                    pos.setMaxy(Float.parseFloat(data[9]));

                    positionDao.insert(pos);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "skipped line: " + count);
                }

                count++;
            }
        } catch (IOException e) {
            Log.w(TAG, "loading example dataset CSVs failed", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "closing CSV reader failed", e);
                }
            }
        }

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
            String res = SessionManager.checkForIntegrity(context, session);
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

    public Session getSession() throws NotRecordingException {
        if (!isActive()) throw new NotRecordingException();
        return session;
    }

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

    public static class NotRecordingException extends Exception {

    }
}