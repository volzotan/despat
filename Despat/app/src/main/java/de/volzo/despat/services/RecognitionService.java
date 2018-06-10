package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorSSD;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;

/**
 * Created by volzotan on 04.08.17.
 */

public class RecognitionService extends IntentService {

    private static final String TAG = RecognitionService.class.getSimpleName();

    public static final String SESSION_ID = "SESSION_ID";

    private static float MIN_CONFIDENCE = 0.1f;

    public RecognitionService() {
        super("RecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "RecognitionService invoked");

//        String action = intent.getAction();
//
//        if (action == null || action.length() == 0) {
//            Log.e(TAG, "action missing");
//            return;
//        }

        Bundle args = intent.getExtras();
        Long sessionId = args.getLong(SESSION_ID);

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
            if (c.isProcessed()) {
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

        // TODO

        Detector detector;
        try {
            detector = new DetectorSSD(this);
//            detector = new DetectorHOG(activity);
            detector.init();
        } catch (Exception e) {
            Log.e(TAG, "detector init failed", e);
            return;
        }

        for (Capture c : queue) {
            try {
                detector.load(c.getImage());
                List<Detector.Recognition> detections = detector.run();
                saveDetections(c, detections);
                c.setProcessed(true);
                captureDao.update(c);
            } catch (Exception e) {
                Log.e(TAG, "detector run failed", e);
            }
        }

//        detector.display((DrawSurface) findViewById(R.id.drawSurface), detections);
    }

    private void saveDetections(Capture capture, List<Detector.Recognition> d) {
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
            pos.setX((loc.right - loc.left)/2f);
            pos.setY((loc.bottom);

            pos.setType(rec.getTitle());
            pos.setRecognitionConfidence(rec.getConfidence());

            positionDao.insert(pos);
        }

        Log.d(TAG, "saved " + d.size() + " detections (skipped " + skipcounter + " below threshold)");
    }
}
