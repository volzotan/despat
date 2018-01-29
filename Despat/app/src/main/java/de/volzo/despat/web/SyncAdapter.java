package de.volzo.despat.web;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.EventDao;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;

/**
 * Created by volzotan on 25.01.18.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = SyncAdapter.class.getSimpleName();

    ContentResolver contentResolver;
    Context context;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        this.contentResolver = context.getContentResolver();
        this.context = context;
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        contentResolver = context.getContentResolver();

    }

    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        Log.d(TAG, "sync started");

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
                        serverConnector.sendSession(sessionDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for EVENT: " + missingIds.size());
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
                        serverConnector.sendCapture(captureDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for EVENT: " + missingIds.size());
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
                        serverConnector.sendEvent(eventDao.getAllById(missingIds), genericSuccessCallback, genericFailureCallback);
                        Log.i(TAG, "sync elements for EVENT: " + missingIds.size());
                    } catch (JSONException e) {
                        Log.w(TAG, "parsing missing IDs response failed", e);
                    }
                }
            };

            serverConnector.syncCheckStatus(statusDao.getAll(), statusCallback, genericFailureCallback);
            serverConnector.syncCheckSession(sessionDao.getAll(), sessionCallback, genericFailureCallback);
            // serverConnector.syncCheckCapture(captureDao.getAll(), captureCallback, genericFailureCallback);
            serverConnector.syncCheckEvent(eventDao.getAll(), eventCallback, genericFailureCallback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}