package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Compressor;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class CompressorService extends IntentService {

    private static final String TAG = CompressorService.class.getSimpleName();

    public static final String ARG_SESSION_ID = "SESSION_ID";

    public CompressorService() {
        super("CompressorService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "CompressorService invoked");

        if (!Config.getEnableCompressorService(this)) {
            Log.w(TAG, "Compression disabled. aborting Compressor Service");
            return;
        }

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();

        List<Session> sessions;

        Bundle args = intent.getExtras();
        if (args == null) {
            sessions = sessionDao.getAll();
        } else {
            Long sessionId = args.getLong(ARG_SESSION_ID);
            sessions = new LinkedList<Session>();
            sessions.add(sessionDao.getById(sessionId));
        }

        for (Session session : sessions) {
            Compressor compressor = new Compressor();
            try {
                compressor.runForSession(this, session);
            } catch (Exception e) {
                Log.w(TAG, "compressor failed for session: " + session);
                Util.saveErrorEvent(this, session.getId(), "compressing image failed", e);
            }
        }

        stopSelf();
    }
}
