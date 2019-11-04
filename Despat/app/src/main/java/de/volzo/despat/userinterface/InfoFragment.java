package de.volzo.despat.userinterface;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.fragment.app.Fragment;
import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
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


        List<DataPoint> datapointsList = new ArrayList<>();
        for (int i=0; i<captures.size(); i++) {
            Capture c = captures.get(i);

            Date recTime = c.getRecordingTime();
            double exposureValue = Util.computeExposureValue(c.getExposureTime(), c.getAperture(), c.getIso());

            if (recTime == null) {
                Log.w(TAG, "invalid recordingTime for datapoint " + i);
                continue;
            }

            if (exposureValue <= 0.0) {
                Log.w(TAG, "invalid exposureValue for datapoint " + i);
                continue;
            }

            datapointsList.add(new DataPoint(recTime, exposureValue));
        }
        datapoints = datapointsList.toArray(new DataPoint[0]);
        series = new LineGraphSeries<DataPoint>(datapoints);
//        // set date label formatter
//        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext()));
//        graph.getGridLabelRenderer().setNumHorizontalLabels(captures.size());
//        // set manual x bounds to have nice steps
//        graph.getViewport().setMinX(captures.get(0).getRecordingTime().getTime());
//        graph.getViewport().setMaxX(captures.get(captures.size()-1).getRecordingTime().getTime());
//        graph.getViewport().setXAxisBoundsManual(true);
//        // as we use dates as labels, the human rounding to nice readable numbers is not necessary
//        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

//        graph.getViewport().setYAxisBoundsManual(true);
//        graph.getViewport().setMinY(0);

        graph.addSeries(series);

        // Exposure Time

        graph = (GraphView) view.findViewById(R.id.graph_exposureTime);

        datapointsList = new ArrayList<>();
        for (int i=0; i<captures.size(); i++) {
            Capture c = captures.get(i);

            Date recTime = c.getRecordingTime();
            double value = c.getExposureTime();

            if (recTime == null) {
                Log.w(TAG, "invalid recordingTime for datapoint " + i);
                continue;
            }

            if (value <= 0.0) {
                Log.w(TAG, "invalid exposureTime for datapoint " + i);
                continue;
            }

            datapointsList.add(new DataPoint(recTime, value));
        }
        datapoints = datapointsList.toArray(new DataPoint[0]);
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.addSeries(series);

        // Aperture

        graph = (GraphView) view.findViewById(R.id.graph_aperture);

        datapointsList = new ArrayList<>();
        for (int i=0; i<captures.size(); i++) {
            Capture c = captures.get(i);

            Date recTime = c.getRecordingTime();
            double value = c.getAperture();

            if (recTime == null) {
                Log.w(TAG, "invalid recordingTime for datapoint " + i);
                continue;
            }

            if (value <= 0.0) {
                Log.w(TAG, "invalid exposureTime for datapoint " + i);
                continue;
            }

            datapointsList.add(new DataPoint(recTime, value));
        }
        datapoints = datapointsList.toArray(new DataPoint[0]);
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.addSeries(series);


        // Aperture

        graph = (GraphView) view.findViewById(R.id.graph_iso);

        datapointsList = new ArrayList<>();
        for (int i=0; i<captures.size(); i++) {
            Capture c = captures.get(i);

            Date recTime = c.getRecordingTime();
            double value = c.getIso();

            if (recTime == null) {
                Log.w(TAG, "invalid recordingTime for datapoint " + i);
                continue;
            }

            if (value <= 0.0) {
                Log.w(TAG, "invalid exposureTime for datapoint " + i);
                continue;
            }

            datapointsList.add(new DataPoint(recTime, value));
        }
        datapoints = datapointsList.toArray(new DataPoint[0]);
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
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

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(50);

        // Battery

        graph = (GraphView) view.findViewById(R.id.graph_battery);
        datapoints = new DataPoint[status.size()];
        for (int i=0; i<status.size(); i++) {
            datapoints[i] = new DataPoint(i, status.get(i).getBatteryInternal());
        }
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.addSeries(series);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

        // Free Space

        graph = (GraphView) view.findViewById(R.id.graph_freeSpace);
        datapoints = new DataPoint[status.size()];
        for (int i=0; i<status.size(); i++) {
            datapoints[i] = new DataPoint(i, status.get(i).getFreeSpaceInternal());
        }
        series = new LineGraphSeries<DataPoint>(datapoints);
        graph.addSeries(series);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);

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
//        // TODO: draw static overlay for performance (PNG with alpha channel) instead of using a canvas
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
//                    final Detector detector = new DetectorTensorFlowMobile(context, session.getDetectorConfig());
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
