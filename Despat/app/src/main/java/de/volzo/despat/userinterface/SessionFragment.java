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
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.volzo.despat.R;
import de.volzo.despat.SessionManager;
import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorSSD;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;


public class SessionFragment extends Fragment {

    private static final String TAG = SessionFragment.class.getSimpleName();

    public static final String ACTION_EXPORT = "ACTION_EXPORT";

    Context context;

    OnSessionActionSelectionListener listener;

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
        PositionDao positionDao = db.positionDao();

        final Session session = sessionDao.getById(sessionId);

        ImageView ivCompressedPreview = (ImageView) view.findViewById(R.id.compressedpreview);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvStart = (TextView) view.findViewById(R.id.start);
        TextView tvEnd = (TextView) view.findViewById(R.id.end);
        TextView tvDuration = (TextView) view.findViewById(R.id.duration);
        TextView tvNumberOfCaptures = (TextView) view.findViewById(R.id.numberOfCaptures);
        TextView tvMaxTemperature = (TextView) view.findViewById(R.id.maxTemperature);
        TextView tvNumberOfDetections = (TextView) view.findViewById(R.id.numberOfDetections);
        TextView tvGlitches = (TextView) view.findViewById(R.id.tv_glitches);

        try {
            File f = session.getCompressedImage();
            if (f == null) throw new Exception("compressed image missing");
            Glide.with(context).load(f.getAbsoluteFile()).into(ivCompressedPreview);
        } catch (Exception e) {
            Log.w(TAG, "compressed preview for session " + session.toString() + "could not be loaded");
            Glide.with(context).load(R.drawable.missing_img).into(ivCompressedPreview);
        }

        DrawSurface drawSurface = view.findViewById(R.id.drawSurface_session);
        List<Position> positions = positionDao.getAllBySession(session.getId());
        try {
            Detector detector = new DetectorSSD(context);
            Size imageSize = session.getImageSize();
            detector.display(drawSurface, imageSize, detector.positionsToRectangles(positions), session);
        } catch (Exception e) {
            Log.e(TAG, "drawing results failed", e);
        }

        tvName.setText(session.getSessionName());
        tvStart.setText(Util.getDateFormat().format(session.getStart()));

        if (session.getEnd() != null) {
            tvEnd.setText(Util.getDateFormat().format(session.getEnd()));
            tvDuration.setText(Util.getHumanReadableTimediff(session.getStart(), session.getEnd(), true));
        } else {
            tvEnd.setText("no end date");
            tvDuration.setText("no end date");
        }

        tvNumberOfCaptures.setText(Integer.toString(sessionDao.getNumberOfCaptures(session.getId())));
        try {
            Status maxTempStatus = SessionManager.getMaxTemperatureDuringSession(context, session);

            if (maxTempStatus != null) {
                DateFormat df = new SimpleDateFormat(Config.DATEFORMAT);
                tvMaxTemperature.setText(
                        String.format(Config.LOCALE, "%2.2f°C (at %s)",
                            maxTempStatus.getTemperatureBattery(),
                            df.format(maxTempStatus.getTimestamp())
                        )
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "getting maximum temperature failed", e);
        }
        tvNumberOfDetections.setText(Integer.toString(positionDao.getCountBySession(session.getId())));

        tvGlitches.setText(SessionManager.checkForIntegrity(getContext(), session));

        view.findViewById(R.id.bt_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSessionActionSelection(sessionId, ACTION_EXPORT);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSessionActionSelectionListener) {
            listener = (OnSessionActionSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnSessionActionSelectionListener {
        void onSessionActionSelection(long sessionId, String action);
    }
}
