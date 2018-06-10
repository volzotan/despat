package de.volzo.despat.support;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.Config;

public class SessionExporter {

    public static String TAG = SessionExporter.class.getSimpleName();

    private static final String del = "|";
    private static final DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));

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
        // compressedimage.jpg
        // positions.csv
        // status.csv
        // correspondingpoints.csv
        // info.json // session name, camera name, ...

        if (session == null) {
            Log.e(TAG, "session null");
            throw new Exception("session missing or invalid");
        }

        List<Capture> captures = captureDao.getAllBySession(session.getId());

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
        File tmpdir = Config.TMP_DIR;

        try {
            Util.deleteDirectory(tmpdir);
            tmpdir.mkdirs();
        } catch (Exception e) {
            Log.w(TAG, "clearing dir failed", e);
        }

        List<File> files = new ArrayList<File>();

        // compressed image
        if (session.getCompressedImage() == null || !session.getCompressedImage().exists()) {
            Log.w(TAG, "compressed image missing");
        } else {
            File exportFile = new File(tmpdir, "compressedimage.jpg");
            Util.copyFile(session.getCompressedImage(), exportFile);
            files.add(exportFile);

            Log.d(TAG, "copied compressed image file: " + session.getCompressedImage().getName());
        }

        // status
        List<Status> statuses = statusDao.getAllBetween(session.getStart(), session.getEnd());
        try {
            File exportFile = new File(tmpdir, "status.csv");
            writeStatusToFile(exportFile, statuses);
            files.add(exportFile);
        } catch (Exception e) {
            Log.e(TAG, "writing status.csv failed", e);
            throw new Exception("writing status.csv failed");
        }

        // corresponding points
//        List<HomographyPoint> homographyPoints = homographyPointDao.getAllBySession(session.getId());
//        try {
//            File exportFile = new File(tmpdir, "correspondingpoints.csv");
//            writeHomographyPointToFile(exportFile, homographyPoints);
//            files.add(exportFile);
//        } catch (Exception e) {
//            Log.e(TAG, "writing correspondingpoints.csv failed", e);
//            throw new Exception("writing correspondingpoints.csv failed");
//        }

        // info
        try {
            File exportFile = new File(tmpdir, "info.json");
            writeInfoToFile(exportFile, session);
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

    private void writeInfoToFile(File f, Session session) throws Exception {
        JSONObject o = new JSONObject();

        o.put("sessionId", session.getId());
        o.put("sessionName", session.getSessionName());
        o.put("start", session.getStart()); // TODO: dateformat?
        o.put("end", session.getEnd());
        o.put("lat", session.getLatitude());
        o.put("lon", session.getLongitude());

        try {
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(o.toString());
        } catch (IOException e) {
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
        osw.write(str);
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Integer i) throws IOException {
        osw.write(Integer.toString(i));
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Float f) throws IOException {
        osw.write(Float.toString(f));
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Double d) throws IOException {
        osw.write(Double.toString(d));
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Boolean b) throws IOException {
        osw.write(b ? "1" : "0");
        osw.write(del);
    }

    public void write(OutputStreamWriter osw, Date d) throws IOException {
        osw.write(dateFormat.format(d));
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