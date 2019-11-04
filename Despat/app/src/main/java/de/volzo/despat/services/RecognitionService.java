package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.NotificationUtil;
import de.volzo.despat.support.Util;


public class RecognitionService extends IntentService {

    private static final String TAG = RecognitionService.class.getSimpleName();

    public static final String ARG_SESSION_ID               = "ARG_SESSION_ID";
    private static final String NOTIFICATION_CHANNEL_ID     = "de.volzo.despat.notificationchannel.RecognitionService";
    private static final String NOTIFICATION_CHANNEL_NAME   = "Background Work Services";
    private static final int NOTIFICATION_ID                = 0x500;

    private static float MIN_CONFIDENCE = 0.1f;

    public RecognitionService() {
        super("RecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "RecognitionService invoked");

        if (!Config.getEnableRecognitionService(this)) {
            Log.w(TAG, "Recognition disabled. aborting Recognition Service");
            return;
        }

//        String action = intent.getAction();
//
//        if (action == null || action.length() == 0) {
//            Log.e(TAG, "action missing");
//            return;
//        }

        Bundle args = intent.getExtras();
        Long sessionId = args.getLong(ARG_SESSION_ID);

        if (sessionId == null) {
            Log.e(TAG, "session id missing");
            return;
        }

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        Session session = sessionDao.getById(sessionId);

        if (session == null) {
            Log.e(TAG, "no session found for id: " + sessionId);
            return;
        }

        List<Capture> captures = captureDao.getAllBySession(session.getId());
        List<Capture> queue = new ArrayList<Capture>();

        int skipcounter = 0;
        int errorcounter = 0;
        for (Capture c : captures) {
            if (c.isProcessed_detector()) {
                skipcounter += 1;
                continue;
            }
            if (c.getImage() == null || !c.getImage().exists()) {
                errorcounter += 1;
                continue;
            }
            queue.add(c);
        }

        Log.d(TAG, "skipped " + skipcounter + " captures, " + errorcounter + " errors, queued: " + queue.size());

        NotificationUtil.showProgressNotification(this, 0, 0, "RecognitionService", "started", NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME);

        // TODO

        Detector detector;
        try {
            detector = new DetectorTensorFlowMobile(this, session.getDetectorConfig());
            detector.init();
        } catch (Exception e) {
            Log.e(TAG, "detector init failed", e);
            return;
        }

        for (int i=0; i<queue.size(); i++) {
            try {
                Capture c = queue.get(i);
                NotificationUtil.showProgressNotification(this, i, queue.size(), "RecognitionService", "processing (" + (i+1) + "/" + queue.size() + ")", NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME);
                Log.d(TAG, String.format("finished image: %d/%d", i, queue.size()));
                detector.load(c.getImage());
                List<Detector.Recognition> detections = detector.run();
                saveDetections(c, detections);
                c.setProcessed_detector(true);
                captureDao.update(c);
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, "detector run failed. Saving failed. Probably the session has been deleted. Abort.", e);
                return;
            } catch (Exception e) {
                Log.e(TAG, "detector run failed", e);
            }
        }

        if (Config.getDeleteAfterRecognition(this)) {
            int deletecounter = 0;
            for (int i = 0; i < queue.size(); i++) {
                Capture c = queue.get(i);

                if (!c.isProcessed_compressor() || !c.isProcessed_detector()) {
                    Log.d(TAG, "deletion failed: capture not processed completely: " + c);
                    continue;
                }

                try {
                    Util.deleteImage(c.getImage());
                    deletecounter++;
                } catch (Exception e) {
                    Log.w(TAG, "deleting image failed: " + c.getImage().getName(), e);
                }
            }
            Log.d(TAG, "deleted " + deletecounter + " images after recognition");
        }

        NotificationUtil.showProgressNotification(this, 100, 100, "RecognitionService", "finished", NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, 3000);

//        detector.display((DrawSurface) findViewById(R.id.drawSurface), detections);
    }

    private void saveDetections(Capture capture, List<Detector.Recognition> d) throws Exception {
        AppDatabase db = AppDatabase.getAppDatabase(this);
        PositionDao positionDao = db.positionDao();

        int skipcounter = 0;
        for (Detector.Recognition rec : d) {

            if (rec.getConfidence() < MIN_CONFIDENCE) {
                skipcounter += 1;
                continue;
            }

            Position pos = new Position();
            pos.setCaptureId(capture.getId());

            RectF loc = rec.getLocation();
            pos.setMinx(loc.left);
            pos.setMiny(loc.top);
            pos.setMaxx(loc.right);
            pos.setMaxy(loc.bottom);

            // The detected position is not the center of the bounding box
            // but the approximate point where the object touches the ground
            // TODO: this works well for class person, apply different strategy for cars, etc.
            pos.setX((loc.right - loc.left)/2f);
            pos.setY((loc.bottom));

            pos.setType(rec.getTitle());
            pos.setTypeId(rec.getClassId());
            pos.setRecognitionConfidence(rec.getConfidence());

            positionDao.insert(pos);
        }

        Log.d(TAG, String.format("saved %d/%d detections (skipped %d below threshold)", d.size()-skipcounter, d.size(), skipcounter));
    }
}
