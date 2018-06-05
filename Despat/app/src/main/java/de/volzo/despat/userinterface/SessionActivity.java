package de.volzo.despat.userinterface;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;

public class SessionActivity extends AppCompatActivity implements SessionListFragment.OnSessionListSelectionListener, SessionFragment.OnSessionActionSelectionListener, HomographyPointListFragment.OnHomographyPointListSelectionListener {

    public static final String TAG = SessionActivity.class.getSimpleName();

    Activity activity;
    ActionBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        this.activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((AppCompatActivity) this).setSupportActionBar(toolbar);
        bar = ((AppCompatActivity) this).getSupportActionBar();
        bar.setTitle("Sessions");
        bar.setDisplayHomeAsUpEnabled(true);
        //        bar.setDisplayShowTitleEnabled(false);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SessionListFragment fragment = new SessionListFragment();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onSessionListSelection(Session session) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        SessionFragment newFragment = new SessionFragment();

        Bundle args = new Bundle();
        args.putLong(SessionFragment.ARG_SESSION_ID, session.getId());
        newFragment.setArguments(args);

        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);  // TODO ?

        transaction.commit();

//        bar.setTitle("Session");
//        bar.setSubtitle(session.getSessionName());

//        bar.set
    }

    @Override
    public void onSessionActionSelection(long sessionId, String action) {
        if (action == null) {
            Log.e(TAG, "empty action");
        }

        switch (action) {
            case SessionFragment.ACTION_HOMOGRAPHY: {
//
//                ArrayList<String> mpos = new ArrayList<String>();
//                ArrayList<String> mdesc = new ArrayList<String>();
//                ArrayList<Integer> mtype = new ArrayList<Integer>();
//
//                mpos.add("50.971296, 11.037630"); mdesc.add("1"); mtype.add(MapsActivity.DespatMarker.TYPE_CORRESPONDING_POINT);
//                mpos.add("50.971173, 11.037914"); mdesc.add("2"); mtype.add(MapsActivity.DespatMarker.TYPE_CORRESPONDING_POINT);
//                mpos.add("50.971456, 11.037915"); mdesc.add("3"); mtype.add(MapsActivity.DespatMarker.TYPE_CORRESPONDING_POINT);
//                mpos.add("50.971705, 11.037711"); mdesc.add("4"); mtype.add(MapsActivity.DespatMarker.TYPE_CORRESPONDING_POINT);
//                mpos.add("50.971402, 11.037796"); mdesc.add("5"); mtype.add(MapsActivity.DespatMarker.TYPE_CORRESPONDING_POINT);
//                mpos.add("50.971636, 11.037486"); mdesc.add("6"); mtype.add(MapsActivity.DespatMarker.TYPE_CORRESPONDING_POINT);
//                mpos.add("50.971040, 11.038093"); mdesc.add("Camera"); mtype.add(MapsActivity.DespatMarker.TYPE_CAMERA);
//
//                Intent intent = new Intent(activity, MapsActivity.class);
//                intent.putExtra("MAP_POSITION", "50.971402, 11.037796");
//                intent.putStringArrayListExtra("MAP_MARKER_POSITION", mpos);
//                intent.putStringArrayListExtra("MAP_MARKER_DESCRIPTION", mdesc);
//                intent.putIntegerArrayListExtra("MAP_MARKER_TYPE", mtype);
//                startActivity(intent);

                break;
            }

            case SessionFragment.ACTION_ERRORS: {

                AppDatabase db = AppDatabase.getAppDatabase(activity);
                SessionDao sessionDao = db.sessionDao();
                Session session = sessionDao.getById(sessionId);

                if (session == null) {
                    Log.e(TAG, "session missing");
                    return;
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                ErrorEventListFragment newFragment = new ErrorEventListFragment();

                Bundle args = new Bundle();
                args.putLong(SessionFragment.ARG_SESSION_ID, session.getId());
                newFragment.setArguments(args);

                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();

                bar.setTitle("Session");
                bar.setSubtitle(session.getSessionName());

                break;
            }

            case SessionFragment.ACTION_DELETE: {
                break;
            }

            default: {
                Log.e(TAG, "unknown action: " + action);
                break;
            }
        }
    }

    @Override
    public void onHomographyPointListSelectionListener(HomographyPoint homographyPoint) {

    }
}

