package de.volzo.despat.support;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Position;
import de.volzo.despat.persistence.PositionDao;
import de.volzo.despat.persistence.RoomConverter;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.Config;

public class SessionExporter {

    public static String TAG = SessionExporter.class.getSimpleName();

    private static final String del = "|";
    private static final DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));

    private static final String HEADER_POSITION         = "timestamp"+del+"device"+del+"class"+del+"confidence"+del+"lat"+del+"lon"+del+"minx"+del+"miny"+del+"maxx"+del+"maxy"+del+"action";
    private static final String HEADER_STATUS           = "timestamp"+del+"free_space_internal"+del+"free_space_external"+del+"battery_internal"+del+"battery_external"+del+"state_charging"+del+"temperature_device"+del+"temperature_battery";
    private static final String HEADER_HOMOGRAPHYPOINT  = "modification_time"+del+"x"+del+"y"+del+"latitude"+del+"longitude";

    Context context;
    Session session;

    public SessionExporter(Context context, long sessionId) throws IllegalArgumentException {
        this.context = context;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();

        session = sessionDao.getById(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("invalid session id:" + sessionId);
        }
    }

    public SessionExporter(Context context, Session session) {
        this.context = context;
        this.session = session;
    }

    public void export() throws Exception {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();
        PositionDao positionDao = db.positionDao();
        HomographyPointDao homographyPointDao = db.homographyPointDao();
        StatusDao statusDao = db.statusDao();

        // files:
        // 1) compressedimage.jpg
        // 2) positions.csv
        // 3) status.csv
        // 4) correspondingpoints.csv
        // 5) info.json // session name, camera name, ...

        if (session == null) {
            Log.e(TAG, "session null");
            throw new Exception("session missing or invalid");
        }

        if (session.getEnd() == null) {
            Log.w(TAG, "session is missing end date. substituting with last capture");
            Capture lastCapture = captureDao.getLastFromSession(session.getId());

            if (lastCapture == null) {
                Log.w(TAG, "no last capture found. using start date");
                session.setEnd(session.getStart());
            } else {
                session.setEnd(lastCapture.getRecordingTime());
            }
        }

        // create tmp dir
        File tmpdir = Config.getTempDirectory(context);

        try {
            Util.deleteDirectory(tmpdir);
            tmpdir.mkdirs();
        } catch (Exception e) {
            Log.w(TAG, "clearing dir failed", e);
        }

        List<File> files = new ArrayList<File>();

        // 1) compressedimage.jpg

        if (session.getCompressedImage() == null || !session.getCompressedImage().exists()) {
            Log.w(TAG, "compressed image missing");
        } else {
            File exportFile = new File(tmpdir, "compressedimage.jpg");
            Util.copyFile(session.getCompressedImage(), exportFile);
            files.add(exportFile);

            Log.d(TAG, "copied compressed image file: " + session.getCompressedImage().getName());
        }

        // 2) positions.csv

        List<Position> positions = positionDao.getAllBySession(session.getId());
        List<Capture> captures = captureDao.getAllBySession(session.getId());
        HashMap<Long, Date> captureTimestamps = new HashMap<>();
        for (Capture c : captures) {
            captureTimestamps.put(c.getId(), c.getRecordingTime());
        }
        try {
            File exportFile = new File(tmpdir, "detections.csv");
            writePositionsToFile(exportFile, positions, captureTimestamps);
            files.add(exportFile);
        } catch (Exception e) {
            Log.e(TAG, "writing detections.csv failed", e);
            throw new Exception("writing detections.csv failed");
        }

        // 3) status.csv

        List<Status> statuses = statusDao.getAllBetween(session.getStart(), session.getEnd());
        try {
            File exportFile = new File(tmpdir, "status.csv");
            writeStatusToFile(exportFile, statuses);
            files.add(exportFile);
        } catch (Exception e) {
            Log.e(TAG, "writing status.csv failed", e);
            throw new Exception("writing status.csv failed");
        }

        // 4) correspondingpoints.csv

        List<HomographyPoint> homographyPoints = homographyPointDao.getAllBySession(session.getId());
        try {
            File exportFile = new File(tmpdir, "correspondingpoints.csv");
            writeHomographyPointToFile(exportFile, homographyPoints);
            files.add(exportFile);
        } catch (Exception e) {
            Log.e(TAG, "writing correspondingpoints.csv failed", e);
            throw new Exception("writing correspondingpoints.csv failed");
        }

        // 5) info.json

        try {
            File exportFile = new File(tmpdir, "info.json");
            writeInfoToFile(exportFile, session, homographyPoints);
            files.add(exportFile);
        } catch (Exception e) {
            Log.e(TAG, "writing info.json failed", e);
            throw new Exception("writing info.json failed");
        }

        File exportZip = new File(tmpdir, session.getSessionName() + ".zip");
        saveZip(files, exportZip.getAbsolutePath());
        Log.d(TAG, "export finished");

        Util.shareFile(context, exportZip);
    }

    private void writeInfoToFile(File f, Session session, List<HomographyPoint> points) throws Exception {
        JSONObject o = new JSONObject();
        JSONArray sessionArray = new JSONArray();
        JSONObject sessionObject = new JSONObject();
        JSONArray correspondingPointsArray = new JSONArray();

        sessionObject.put("id", session.getId());
        sessionObject.put("sessionname", session.getSessionName());
        sessionObject.put("devicename", Config.getDeviceName(context));

        JSONArray positionArray = new JSONArray();
        positionArray.put(session.getLatitude());
        positionArray.put(session.getLongitude());
        sessionObject.put("position", positionArray);

        JSONObject point = null;
        JSONArray pointLocationArray = null;
        for (int i=0; i<points.size(); i++) {
            point = new JSONObject();
            pointLocationArray = new JSONArray();
            point.put("id", i);
            pointLocationArray.put(points.get(i).getLatitude());
            pointLocationArray.put(points.get(i).getLongitude());
            point.put("position", pointLocationArray);
            correspondingPointsArray.put(point);
        }
        sessionObject.put("corresponding_points", correspondingPointsArray);

        sessionObject.put("start", dateFormat.format(session.getStart()));
        sessionObject.put("end", dateFormat.format(session.getEnd()));
        sessionObject.put("homographyMatrix", RoomConverter.doubleArrayToString(session.getHomographyMatrix()));
        sessionObject.put("resumed", session.isResumed());

        sessionArray.put(sessionObject);
        o.put("sessions", sessionArray);

        o.put("data", "detections.csv");

        JSONArray classmapArray = new JSONArray();
        InputStream labelsInput = context.getAssets().open("coco_labels_list.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            classmapArray.put(line);
        }
        br.close();
        o.put("classmap", classmapArray);

        try {
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(o.toString(4));
            osw.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            throw e;
        }
    }

    private void writePositionsToFile(File f, List<Position> data, HashMap<Long, Date> captureTimestamps) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(HEADER_POSITION);
            osw.write("\n");

            for (Position p : data) {
                write(osw, captureTimestamps.get(p.getCaptureId()));
                write(osw,0);
                write(osw, p.getTypeId());
                write(osw, p.getRecognitionConfidence());
                write(osw, p.getLatitude());
                write(osw, p.getLongitude());
                write(osw, p.getMinx());
                write(osw, p.getMiny());
                write(osw, p.getMaxx());
                write(osw, p.getMaxy());
                write(osw, -1);

//                write(osw, p.getX());
//                write(osw, p.getY());
//                write(osw, p.getPositionConfidence());

                osw.write("\n");
            }

            osw.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            throw e;
        }
    }

    private void writeStatusToFile(File f, List<Status> data) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(HEADER_STATUS);
            osw.write("\n");

            for (Status s : data) {
                write(osw, s.getTimestamp());
                write(osw, s.getFreeSpaceInternal());
                write(osw, s.getFreeSpaceExternal());
                write(osw, s.getBatteryInternal());
                write(osw, s.getBatteryExternal());
                write(osw, s.isStateCharging());
                write(osw, s.getTemperatureDevice());
                write(osw, s.getTemperatureBattery());
                osw.write("\n");
            }

            osw.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            throw e;
        }
    }


    private void writeHomographyPointToFile(File f, List<HomographyPoint> data) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(HEADER_HOMOGRAPHYPOINT);
            osw.write("\n");

            for (HomographyPoint p : data) {
                write(osw, p.getModificationTime());
                write(osw, p.getX());
                write(osw, p.getY());
                write(osw, p.getLatitude());
                write(osw, p.getLongitude());
                osw.write("\n");
            }

            osw.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            throw e;
        }
    }

    public void write(OutputStreamWriter osw, String str) throws IOException {
        if (str == null) {
            osw.write("null");
        } else {
            osw.write(str);
        }
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Integer i) throws IOException {
        if (i == null) {
            osw.write("null");
        } else {
            osw.write(Integer.toString(i));
        }
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Float f) throws IOException {
        if (f == null) {
            osw.write("null");
        } else {
            osw.write(Float.toString(f));
        }
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Double d) throws IOException {
        if (d == null) {
            osw.write("null");
        } else {
            osw.write(Double.toString(d));
        }
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Boolean b) throws IOException {
        if (b == null) {
            osw.write("null");
        } else {
            osw.write(b ? "1" : "0");
        }
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Date d) throws IOException {
        if (d == null) {
            osw.write("null");
        } else {
            osw.write(dateFormat.format(d));
        }
        osw.write(del);
    }

    public static void saveZip(List<File> files, String filename) {
        int BUFFER = 1024;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(filename);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (File f : files) {
                Log.v(TAG, "Adding: " + f.getAbsolutePath());
                FileInputStream fis = new FileInputStream(f);
                origin = new BufferedInputStream(fis, BUFFER);

                ZipEntry entry = new ZipEntry(f.getName());
                out.putNextEntry(entry);

                int length;
                while ((length = fis.read(data)) > 0) {
                    out.write(data, 0, length);
                }

                origin.close();
            }

            out.close();
        } catch (Exception e) {
            Log.e(TAG, "export as zip failed", e);
            e.printStackTrace();
        }
    }
}
