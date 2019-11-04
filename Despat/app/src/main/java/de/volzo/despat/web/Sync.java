package de.volzo.despat.web;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.EventDao;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 09.02.18.
 */

public class Sync {

    private static final String TAG = Sync.class.getSimpleName();

    public static synchronized void run(Context context, Class trigger, boolean ignoreMinSyncTime) {

        if (!Config.getPhoneHome(context)) {
            Log.d(TAG, "sync stopped. phoneHome set to false");
            return;
        }

        String credentials_username = context.getResources().getString(R.string.server_username);

        if (credentials_username == null || credentials_username.length() == 0) {
            Log.e(TAG, "sync stopped. logindata missing");
            return;
        }

        Date lastSync = Config.getLastSync(context);
        if (ignoreMinSyncTime == false && lastSync != null) {
            long diff = Calendar.getInstance().getTime().getTime() - lastSync.getTime();

            if (diff < Config.getMinSyncInterval(context)) {
                Log.d(TAG, "sync triggered by [" +  trigger.getSimpleName() + "] aborted (min sync interval)");
                return;
            }
        }

        Log.i(TAG, "sync started. triggered by: " + trigger.getSimpleName());
        Util.saveEvent(context, Event.EventType.SYNC, trigger.getSimpleName());

        Config.setLastSync(context, Calendar.getInstance().getTime());

        final ServerConnector serverConnector = new ServerConnector(context);
        AppDatabase db = AppDatabase.getAppDatabase(context);

        final StatusDao statusDao = db.statusDao();
        final SessionDao sessionDao = db.sessionDao();
        final CaptureDao captureDao = db.captureDao();
        final EventDao eventDao = db.eventDao();

        try {

            final ServerConnector.RequestSuccessCallback genericSuccessCallback = new ServerConnector.RequestSuccessCallback() {
                @Override
                public void success(JSONArray response) {
                    Log.d(TAG, "sync successful");
                }
            };

            final ServerConnector.RequestFailureCallback genericFailureCallback = new ServerConnector.RequestFailureCallback() {
                @Override
                public void failure(JSONArray response) {
                    if (response != null) {
                        Log.e(TAG, "sync failed: " + response.toString());
                    } else {
                        Log.w(TAG, "sync failed, response null");
                    }
                }
            };

            // ---

            final ServerConnector.RequestSuccessCallback statusCallback = new ServerConnector.RequestSuccessCallback() {
                @Override
                public void success(JSONArray response) {
                    try {
                        List<Integer> missingIds = serverConnector.parseJsonResponse(response);
                        Util.cutList(missingIds, 900);
                        serverConnector.sendStatus(statusDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for STATUS: " + missingIds.size());
                    } catch (JSONException e) {
                        Log.w(TAG, "parsing missing IDs response failed", e);
                    }
                }
            };

            final ServerConnector.RequestSuccessCallback sessionCallback = new ServerConnector.RequestSuccessCallback() {
                @Override
                public void success(JSONArray response) {
                    try {
                        List<Integer> missingIds = serverConnector.parseJsonResponse(response);
                        Util.cutList(missingIds, 900);
                        serverConnector.sendSession(sessionDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for SESSION: " + missingIds.size());
                    } catch (JSONException e) {
                        Log.w(TAG, "parsing missing IDs response failed", e);
                    }
                }
            };

            final ServerConnector.RequestSuccessCallback captureCallback = new ServerConnector.RequestSuccessCallback() {
                @Override
                public void success(JSONArray response) {
                    try {
                        List<Integer> missingIds = serverConnector.parseJsonResponse(response);
                        Util.cutList(missingIds, 900);
                        serverConnector.sendCapture(captureDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for CAPTURE: " + missingIds.size());
                    } catch (JSONException e) {
                        Log.w(TAG, "parsing missing IDs response failed", e);
                    }
                }
            };

            final ServerConnector.RequestSuccessCallback eventCallback = new ServerConnector.RequestSuccessCallback() {
                @Override
                public void success(JSONArray response) {
                    try {
                        List<Integer> missingIds = serverConnector.parseJsonResponse(response);
                        Util.cutList(missingIds, 900);
                        serverConnector.sendEvent(eventDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for EVENT: " + missingIds.size());
                    } catch (JSONException e) {
                        Log.w(TAG, "parsing missing IDs response failed", e);
                    }
                }
            };

            serverConnector.syncCheckStatus(statusDao.getAll(), statusCallback, genericFailureCallback);
            serverConnector.syncCheckSession(sessionDao.getAll(), sessionCallback, genericFailureCallback);
            serverConnector.syncCheckCapture(captureDao.getAll(), captureCallback, genericFailureCallback);
            serverConnector.syncCheckEvent(eventDao.getAll(), eventCallback, genericFailureCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void knock(Context context) throws Exception {

        final ServerConnector serverConnector = new ServerConnector(context);

        final ServerConnector.RequestSuccessCallback genericSuccessCallback = new ServerConnector.RequestSuccessCallback() {
            @Override
            public void success(JSONArray response) {
                Log.d(TAG, "knock successful");
            }
        };

        final ServerConnector.RequestFailureCallback genericFailureCallback = new ServerConnector.RequestFailureCallback() {
            @Override
            public void failure(JSONArray response) {
                if (response != null) {
                    Log.e(TAG, "knock failed: " + response.toString());
                } else {
                    Log.w(TAG, "knock failed, response null");
                }
            }
        };


        Despat despat = Util.getDespat(context);
        CameraController camera = despat.getCamera();

        if (camera != null) throw new Exception("camera in use");

        despat.initCamera(context);
        HashMap<String, String> parameters = camera.getCameraParameters();

        serverConnector.sendKnock(parameters, genericSuccessCallback, genericFailureCallback);
    }
}
