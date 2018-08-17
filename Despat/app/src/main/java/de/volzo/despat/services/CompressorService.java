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
import de.volzo.despat.support.Util;

public class CompressorService extends IntentService {

    private static final String TAG = CompressorService.class.getSimpleName();

    public static final String SESSION_ID = "SESSION_ID";

    public CompressorService() {
        super("CompressorService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "CompressorService invoked");

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();

        List<Session> sessions = sessionDao.getAll();

        for (Session session : sessions) {
            Compressor compressor = new Compressor();
            try {
                compressor.runForSession(this, session);
            } catch (Exception e) {
                Log.w(TAG, "compressor failed for session: " + session);
                Util.saveErrorEvent(this, session.getId(), "compressing image failed", e);
            }
        }
    }
}
