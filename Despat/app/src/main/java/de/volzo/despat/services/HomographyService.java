package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.HomographyCalculator;

public class HomographyService extends IntentService {

    private static final String TAG = HomographyService.class.getSimpleName();

    public static final String ARG_SESSION_ID = "ARG_SESSION_ID";

    public HomographyService() {
        super("HomographyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "HomographyService invoked");

//        String action = intent.getAction();
//
//        if (action == null || action.length() == 0) {
//            Log.e(TAG, "action missing");
//            return;
//        }

        Bundle args = intent.getExtras();
        Long sessionId = args.getLong(ARG_SESSION_ID);

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();
        PositionDao positionDao = db.positionDao();
        HomographyPointDao homographyPointDao = db.homographyPointDao();

        if (sessionId == null) {
            Log.e(TAG, "session id missing");
            return;
        }

        Session session = sessionDao.getById(sessionId);

        if (session == null) {
            Log.e(TAG, "no session found for id: " + sessionId);
            return;
        }

        List<HomographyPoint> points = homographyPointDao.getAllBySession(sessionId);

        if (points == null) {
            Log.e(TAG, "no corresponding points found");
            return;
        }

        if (points.size() < 4) {
            Log.e(TAG, "at least 4 corresponding points are required. available: " + points.size());
            return;
        }

        List<Position> positions = positionDao.getAllBySession(sessionId);

        try {
            HomographyCalculator hcalc = new HomographyCalculator();
            hcalc.loadPoints(points);

            session.setHomographyMatrix(hcalc.getHomographyMatrix());
            sessionDao.update(session);

            hcalc.convertPoints(positions);
        } catch (Exception e) {
            Log.e(TAG, "homography operation failed", e);
            return;
        }

        for (Position p : positions) {
            positionDao.update(p);
        }

        Log.d(TAG, "homography operation finished. positions updated: " + positions.size());
    }
}
