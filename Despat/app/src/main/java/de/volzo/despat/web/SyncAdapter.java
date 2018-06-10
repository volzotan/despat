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

    private static final String TAG = SyncAdapter.class.getSimpleName();

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

        Sync.run(context, SyncAdapter.class, false);

    }
}