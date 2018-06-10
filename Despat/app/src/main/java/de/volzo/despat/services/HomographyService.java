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

public class HomographyService extends IntentService {

    private static final String TAG = HomographyService.class.getSimpleName();

    public static final String SESSION_ID = "SESSION_ID";

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
        Long sessionId = args.getLong(SESSION_ID);

        if (sessionId == null) {
            Log.e(TAG, "session id missing");
            return;
        }

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();
        PositionDao positionDao = db.positionDao();
        HomographyPointDao homographyPointDao = db.homographyPointDao();

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

        Session session = sessionDao.getById(sessionId);

        if (session == null) {
            Log.e(TAG, "no session found for id: " + sessionId);
            return;
        }



        List<Session> sessions = sessionDao.getAll();



    }
}
