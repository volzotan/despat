package de.volzo.despat.userinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.ErrorEvent;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.SessionExporter;

public class SessionActivity extends AppCompatActivity implements
        SessionFragment.OnSessionActionSelectionListener,
        ErrorEventListFragment.OnErrorEventListSelectionListener,
        EventListFragment.OnEventListSelectionListener {

    private static final String TAG = SessionActivity.class.getSimpleName();

    public static final String ARG_SESSION_ID = "ARG_SESSION_ID";

    Activity activity;
    ActionBar bar;

    Session session;

    private String[] tabtitles = {"Dataset", "Points", "Errors", "Events", "Info"};
    private Fragment[] tabfragments = {
            new SessionFragment(),
            new HomographyPointListFragment(),
            new ErrorEventListFragment(),
            new EventListFragment(),
            new InfoFragment()
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
        session = sessionDao.getById(sessionId);

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
    public void onSessionActionSelection(final long sessionId, String action) {

        // check the dataset for errors or inconsistencies
        AppDatabase database = AppDatabase.getAppDatabase(activity);
        HomographyPointDao homographyPointDao = database.homographyPointDao();
        List<HomographyPoint> homographyPoints = homographyPointDao.getAllBySession(sessionId);
        PositionDao positionDao = database.positionDao();
        List<Position> positions = positionDao.getAllBySession(sessionId); // TODO: slow. getting ALL positions

        String errorMessage = null;

        if (positions == null || positions.size() == 0) {
            errorMessage = activity.getString(R.string.error_export_emptyDataset);
        } else if (homographyPoints == null || homographyPoints.size() < 4) {
            int diff = 4 - homographyPoints.size();
            errorMessage = String.format(activity.getString(R.string.error_export_tooFewHomographyPoints), diff);
        } else if (positions.get(positions.size()-1).getLatitude() == null) {
            errorMessage = activity.getString(R.string.error_export_noTransformedCoordinates);
        }

        if (errorMessage != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(errorMessage)
                    .setPositiveButton(R.string.exportAnyway, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            export(sessionId);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            export(sessionId);
        }
    }

    private void export(long sessionId) {
        SessionExporter exporter = new SessionExporter(this, sessionId);

        try {
            exporter.export();
        } catch (Exception e) {
            String msg = e.getMessage();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public void onErrorEventListSelection(ErrorEvent errorEvent) {

    }

    @Override
    public void onEventListSelection(Event event) {

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

