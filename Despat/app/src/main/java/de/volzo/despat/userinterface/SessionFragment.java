package de.volzo.despat.userinterface;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;


public class SessionFragment extends Fragment {

    public static final String TAG = SessionFragment.class.getSimpleName();

    Context context;

    public SessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_session, container, false);

        Bundle args = getArguments();
        final long sessionId = args.getLong(SessionActivity.ARG_SESSION_ID);

        if (sessionId <= 0) {
            Log.e(TAG, "invalid session ID for detail view: " + sessionId);
            return null;
        }

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        final Session session = sessionDao.getById(sessionId);

        ImageView ivCompressedPreview = (ImageView) view.findViewById(R.id.compressedpreview);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvStart = (TextView) view.findViewById(R.id.start);
        TextView tvEnd = (TextView) view.findViewById(R.id.end);
        TextView tvDuration = (TextView) view.findViewById(R.id.duration);
        TextView tvNumberOfCaptures = (TextView) view.findViewById(R.id.numberOfCaptures);

        try {
            File f = session.getCompressedImage();
            if (f == null) throw new Exception("compressed image missing");
            Glide.with(context).load(f.getAbsoluteFile()).into(ivCompressedPreview);
        } catch (Exception e) {
            Log.w(TAG, "compressed preview for session " + session.toString() + "could not be loaded");
            Glide.with(context).load(R.drawable.missing_img).into(ivCompressedPreview);
        }

        tvName.setText(session.getSessionName());
        tvStart.setText(Util.getDateFormat().format(session.getStart()));
        tvEnd.setText(Util.getDateFormat().format(session.getEnd()));
        tvDuration.setText(Util.getHumanReadableTimediff(session.getStart(), session.getEnd(), true));
        tvNumberOfCaptures.setText(Integer.toString(sessionDao.getNumberOfCaptures(session.getId())));

//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        HomographyPointListFragment newFragment = new HomographyPointListFragment();
//        Bundle newargs = new Bundle();
//        newargs.putLong(SessionActivity.ARG_SESSION_ID, session.getId());
//        newFragment.setArguments(newargs);
//        transaction.replace(R.id.fragment_container_homographypointlist, newFragment);
//        transaction.commit();

        return view;


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
