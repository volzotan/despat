package de.volzo.despat.userinterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.ErrorEvent;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.SessionExporter;

public class SessionActivity extends AppCompatActivity implements SessionFragment.OnSessionActionSelectionListener, HomographyPointListFragment.OnHomographyPointListSelectionListener, ErrorEventListFragment.OnErrorEventListSelectionListener {

    public static final String TAG = SessionActivity.class.getSimpleName();

    public static final String ARG_SESSION_ID = "ARG_SESSION_ID";

    Activity activity;
    ActionBar bar;

    private String[] tabtitles = {"Info", "Points", "Errors"};
    private Fragment[] tabfragments = {
            new SessionFragment(),
            new HomographyPointListFragment(),
            new ErrorEventListFragment()
    };

    ViewPager viewPager;
    ScreenSlidePagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        this.activity = this;

        Long sessionId = getIntent().getLongExtra(ARG_SESSION_ID, -1l);

        if (sessionId < 0) {
            Log.e(TAG, "invalid session id");
            return;
        }

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();
        Session session = sessionDao.getById(sessionId);

        if (session == null) {
            Log.e(TAG, "no session found for id: " + sessionId);
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((AppCompatActivity) this).setSupportActionBar(toolbar);
        bar = ((AppCompatActivity) this).getSupportActionBar();
        bar.setTitle("Dataset");
        bar.setSubtitle(session.getSessionName());
        bar.setDisplayHomeAsUpEnabled(true);

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), session);
        viewPager.setAdapter(viewPagerAdapter);

//        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
//        viewPager.setAdapter(new CustomPagerAdapter(this));
    }


    @Override
    public void onSessionActionSelection(long sessionId, String action) {
        SessionExporter exporter = new SessionExporter(this, sessionId);

        try {
            exporter.export();
        } catch (Exception e) {
            String msg = e.getMessage();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
//            snackbar.setAction("UNDO", new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    for (Map.Entry<Session, Integer> entry : sessionsDeleteList.entrySet()) {
//                        Session s = entry.getKey();
//                        Integer i = entry.getValue();
//                        ((SessionRecyclerViewAdapter) adapter).restoreItem(s, i);
//                    }
//                }
//            });
//            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onHomographyPointListSelectionListener(HomographyPoint homographyPoint) {

    }

    @Override
    public void onErrorEventListSelection(ErrorEvent errorEvent) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        Session session;

        public ScreenSlidePagerAdapter(FragmentManager fm, Session session) {
            super(fm);

            this.session = session;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = tabfragments[position];
            Bundle args = new Bundle();
            args.putLong(SessionActivity.ARG_SESSION_ID, session.getId());
            frag.setArguments(args);
            return frag;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabtitles[position];
        }

        @Override
        public int getCount() {
            return tabfragments.length;
        }
    }
}

