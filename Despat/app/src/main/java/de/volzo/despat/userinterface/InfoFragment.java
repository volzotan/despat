package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.fragment.app.Fragment;
import de.volzo.despat.R;
import de.volzo.despat.SessionManager;
import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorSSD;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.AspectRatioImageView;
import de.volzo.despat.support.Util;


public class InfoFragment extends Fragment {

    private static final String TAG = InfoFragment.class.getSimpleName();

    Context context;

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info, container, false);

        Bundle args = getArguments();
        final long sessionId = args.getLong(SessionActivity.ARG_SESSION_ID);

        if (sessionId <= 0) {
            Log.e(TAG, "invalid session ID for detail view: " + sessionId);
            return null;
        }

        AppDatabase db = AppDatabase.getAppDatabase(context);
        StatusDao statusDao = db.statusDao();
        SessionDao sessionDao = db.sessionDao();
        PositionDao positionDao = db.positionDao();
        HomographyPointDao homographyPointDao = db.homographyPointDao();
        CaptureDao captureDao = db.captureDao();

        final Session session = sessionDao.getById(sessionId);

        GraphView graph;
        DataPoint datapoints[];
        LineGraphSeries<DataPoint> series;

        // Exposure

        List<Capture> captures = captureDao.getAllBySession(sessionId);
        graph = (GraphView) view.findViewById(R.id.graph_exposure);
        datapoints = new DataPoint[captures.size()];
        for (int i=0; i<captures.size(); i++) {
            Capture c = captures.get(i);
            datapoints[i] = new DataPoint(i, Util.computeExposureValue(c.getExposureTime(), c.getAperture(), c.getIso()));
        }
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.addSeries(series);

        // Temperature

        List<Status> status;
        if (session.getEnd() != null) {
            status = statusDao.getAllBetween(session.getStart(), session.getEnd());
        } else {
            Log.w(TAG, "session " + session + "is missing end date");
            status = statusDao.getAllBetween(session.getStart(), new Date());
        }
        graph = (GraphView) view.findViewById(R.id.graph_temperature);
        datapoints = new DataPoint[status.size()];
        for (int i=0; i<status.size(); i++) {
            datapoints[i] = new DataPoint(i, status.get(i).getTemperatureBattery());
        }
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.addSeries(series);

        // Battery

        graph = (GraphView) view.findViewById(R.id.graph_battery);
        datapoints = new DataPoint[status.size()];
        for (int i=0; i<status.size(); i++) {
            datapoints[i] = new DataPoint(i, status.get(i).getBatteryInternal());
        }
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.addSeries(series);

        // Free Space

        graph = (GraphView) view.findViewById(R.id.graph_freeSpace);
        datapoints = new DataPoint[status.size()];
        for (int i=0; i<status.size(); i++) {
            datapoints[i] = new DataPoint(i, status.get(i).getFreeSpaceInternal());
        }
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.addSeries(series);

//
//        List<Position> unconvertedPositions = positionDao.getAllWithoutLatLonBySession(sessionId);
//        List<HomographyPoint> points = homographyPointDao.getAllBySession(sessionId);
//
//        AspectRatioImageView ivCompressedPreview = (AspectRatioImageView) view.findViewById(R.id.compressedpreview);
//        TextView tvSessionSummary = (TextView) view.findViewById(R.id.tvSessionSummary);
//        TextView tvName = (TextView) view.findViewById(R.id.name);
//        TextView tvShutterInterval = (TextView) view.findViewById(R.id.shutterInterval);
//        TextView tvDetector = (TextView) view.findViewById(R.id.detector);
//        TextView tvStart = (TextView) view.findViewById(R.id.start);
//        TextView tvEnd = (TextView) view.findViewById(R.id.end);
//        TextView tvDuration = (TextView) view.findViewById(R.id.duration);
//        TextView tvNumberOfCaptures = (TextView) view.findViewById(R.id.numberOfCaptures);
//        TextView tvMaxTemperature = (TextView) view.findViewById(R.id.maxTemperature);
//        TextView tvNumberOfDetections = (TextView) view.findViewById(R.id.numberOfDetections);
//        TextView tvGlitches = (TextView) view.findViewById(R.id.tv_glitches);
//
//        try {
//            File f = session.getCompressedImage();
//            ivCompressedPreview.setAspectRatio(session.getImageWidth(), session.getImageHeight());
//            if (f == null) throw new Exception("compressed image missing");
//            Glide.with(context).load(f.getAbsoluteFile()).into(ivCompressedPreview);
//        } catch (Exception e) {
//            Log.w(TAG, "compressed preview for session " + session.toString() + " could not be loaded");
//            Glide.with(context).load(R.drawable.missing_img).into(ivCompressedPreview);
//        }
//
//        // TODO: draw static overlay for perfomance (PNG with alpha channel) instead of using a canvas
//
//        // draw detection boxes (do calculations not on UI thread)
//        final DrawSurface drawSurface = view.findViewById(R.id.drawSurface_session);
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                AppDatabase database = AppDatabase.getAppDatabase(context);
//                PositionDao positionDao = database.positionDao();
//                List<Position> positions = positionDao.getAllBySession(session.getId());
//                try {
//                    final Detector detector = new DetectorSSD(context, session.getDetectorConfig());
//                    final Size imageSize = session.getImageSize();
//                    final List<RectF> rectangles = detector.positionsToRectangles(positions);
//
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (detector != null && drawSurface != null) {
//                                detector.display(drawSurface, imageSize, rectangles, session.getDetectorConfig());
//                            } else {
//                                Log.w(TAG, "drawing boxes failed. surface not available.");
//                            }
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(TAG, "drawing results failed", e);
//                }
//            }
//        });
//
//        tvName.setText(session.getSessionName());
//        if (session.getCameraConfig() != null) {
//            tvShutterInterval.setText(String.format("%ds", session.getCameraConfig().getShutterInterval()/1000));
//        }
//        tvDetector.setText(session.getDetectorConfig().getDetector());
//        tvStart.setText(Util.getDateFormat().format(session.getStart()));
//
//        if (session.getEnd() != null) {
//            tvEnd.setText(Util.getDateFormat().format(session.getEnd()));
//            tvDuration.setText(Util.getHumanReadableTimediff(session.getStart(), session.getEnd(), true));
//        } else {
//            tvEnd.setText("no end date");
//            tvDuration.setText("no end date");
//        }
//
//        tvNumberOfCaptures.setText(Integer.toString(sessionDao.getNumberOfCaptures(session.getId())));
//        try {
//            Status maxTempStatus = SessionManager.getMaxTemperatureDuringSession(context, session);
//            if (maxTempStatus != null) {
//                DateFormat df = new SimpleDateFormat(Config.DATEFORMAT);
//                tvMaxTemperature.setText(
//                        String.format(Config.LOCALE, "%2.2f°C (at %s)",
//                            maxTempStatus.getTemperatureBattery(),
//                            df.format(maxTempStatus.getTimestamp())
//                        )
//                );
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "getting maximum temperature failed", e);
//        }
//

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
