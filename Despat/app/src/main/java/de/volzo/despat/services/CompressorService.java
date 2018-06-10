package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import de.volzo.despat.support.Compressor;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;

public class CompressorService extends IntentService {

    private static final String TAG = CompressorService.class.getSimpleName();

    public static final String SESSION_ID = "SESSION_ID";

    public CompressorService() {
        super("CompressorService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "CompressorService invoked");

//        String action = intent.getAction();
//
//        if (action == null || action.length() == 0) {
//            Log.e(TAG, "action missing");
//            return;
//        }

//        Bundle args = intent.getExtras();
//        Long sessionId = args.getLong(SESSION_ID);
//
//        if (sessionId == null) {
//            Log.e(TAG, "session id missing");
//            return;
//        }
//
        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();
//
//        Session session = sessionDao.getById(sessionId);
//
//        if (session == null) {
//            Log.e(TAG, "no session found for id: " + sessionId);
//            return;
//        }

        List<Session> sessions = sessionDao.getAll();

        for (Session session : sessions) {
            Compressor compressor = new Compressor();
            compressor.runForSession(this, session);
        }

    }
}
