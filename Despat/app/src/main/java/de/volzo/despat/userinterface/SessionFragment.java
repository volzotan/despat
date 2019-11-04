package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.volzo.despat.R;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import de.volzo.despat.SessionManager;
import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.AspectRatioImageView;
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
        PositionDao positionDao = db.positionDao();
        HomographyPointDao homographyPointDao = db.homographyPointDao();

        final Session session = sessionDao.getById(sessionId);

        List<Position> unconvertedPositions = positionDao.getAllWithoutLatLonBySession(sessionId);
        List<HomographyPoint> points = homographyPointDao.getAllBySession(sessionId);
        if (unconvertedPositions != null && unconvertedPositions.size() > 0 && points.size() >= 4) {
            Log.d(TAG, "Session contains unconverted positions. Starting HomographyService");
            Orchestrator.runHomographyService(context, sessionId);
        }

        AspectRatioImageView ivCompressedPreview = (AspectRatioImageView) view.findViewById(R.id.compressedpreview);
        TextView tvSessionSummary = (TextView) view.findViewById(R.id.tvSessionSummary);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvShutterInterval = (TextView) view.findViewById(R.id.shutterInterval);
        TextView tvDetector = (TextView) view.findViewById(R.id.detector);
        TextView tvStart = (TextView) view.findViewById(R.id.start);
        TextView tvEnd = (TextView) view.findViewById(R.id.end);
        TextView tvDuration = (TextView) view.findViewById(R.id.duration);
        TextView tvNumberOfCaptures = (TextView) view.findViewById(R.id.numberOfCaptures);
        TextView tvMaxTemperature = (TextView) view.findViewById(R.id.maxTemperature);
        TextView tvNumberOfDetections = (TextView) view.findViewById(R.id.numberOfDetections);
        TextView tvGlitches = (TextView) view.findViewById(R.id.tv_glitches);

        try {
            File f = session.getCompressedImage();
            ivCompressedPreview.setAspectRatio(session.getImageWidth(), session.getImageHeight());
            if (f == null) throw new Exception("compressed image missing");
            Glide.with(context).load(f.getAbsoluteFile()).into(ivCompressedPreview);
        } catch (Exception e) {
            Log.w(TAG, "compressed preview for session " + session.toString() + " could not be loaded");
            Glide.with(context).load(R.drawable.missing_img).into(ivCompressedPreview);
        }

        // TODO: draw static overlay for perfomance (PNG with alpha channel) instead of using a canvas

        // draw detection boxes (do calculations not on UI thread)
        final DrawSurface drawSurface = view.findViewById(R.id.drawSurface_session);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase database = AppDatabase.getAppDatabase(context);
                PositionDao positionDao = database.positionDao();
                List<Position> positions = positionDao.getAllBySession(session.getId());
                try {
                    final Detector detector = new DetectorTensorFlowMobile(context, session.getDetectorConfig());
                    final Size imageSize = session.getImageSize();
                    final List<RectF> rectangles = detector.positionsToRectangles(positions);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (detector != null && drawSurface != null) {
                                detector.display(drawSurface, imageSize, rectangles, session.getDetectorConfig());
                            } else {
                                Log.w(TAG, "drawing boxes failed. surface not available.");
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "drawing results failed", e);
                }
            }
        });

        StringBuilder sb = new StringBuilder();
//        sb.append("TODO\n"); // TODO
        tvSessionSummary.setText(sb.toString());

        tvName.setText(session.getSessionName());
        if (session.getCameraConfig() != null) {
            tvShutterInterval.setText(String.format("%ds", session.getCameraConfig().getShutterInterval()/1000));
        }
        tvDetector.setText(session.getDetectorConfig().getDetector());
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
