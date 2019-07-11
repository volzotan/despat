package de.volzo.despat.web;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.volzo.despat.BuildConfig;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 10.08.17.
 */

public class ServerConnector {

    private static final String TAG = ServerConnector.class.getSimpleName();

    private Context context;
    private String serverAddress;
    SimpleDateFormat dateFormat;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "foo-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;

    public ServerConnector(Context context) {
        this.context = context;
        this.serverAddress = Config.getServerAddress(context);

        this.dateFormat =  new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));
    }

    /*

    deviceId                String
    deviceName              String
    timestamp               human readable

    numberImages            123
    freeSpaceInternal       123.0
    freeSpaceExternal       -1.0

    batteryInternal         0 - 100
    batteryExternal         0 - 100
    stateCharging           true

    */

    public void syncCheckStatus(List<Status> ids, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) throws Exception {

        if (ids.size() == 0) return;

        JSONArray arr = new JSONArray();
        for (Status status : ids) {
            JSONObject o = new JSONObject();

            o.put("deviceId", Config.getUniqueDeviceId(context));
            o.put("id", status.getId());
            o.put("timestamp", dateFormat.format(status.getTimestamp()));

            arr.put(o);
        }

        send("/sync/" + "status", arr, successCallback, failureCallback);
    }

    public void syncCheckSession(List<Session> ids, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) throws Exception {

        if (ids.size() == 0) return;

        JSONArray arr = new JSONArray();
        for (Session session : ids) {
            JSONObject o = new JSONObject();

            o.put("deviceId", Config.getUniqueDeviceId(context));
            o.put("id", session.getId());
            o.put("timestamp", dateFormat.format(session.getStart()));

            arr.put(o);
        }

        send("/sync/" + "session", arr, successCallback, failureCallback);
    }

    public void syncCheckCapture(List<Capture> ids, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) throws Exception {

        if (ids.size() == 0) return;

        JSONArray arr = new JSONArray();
        for (Capture capture : ids) {
            JSONObject o = new JSONObject();

            o.put("deviceId", Config.getUniqueDeviceId(context));
            o.put("id", capture.getId());
            o.put("timestamp", dateFormat.format(capture.getRecordingTime()));

            arr.put(o);
        }

        send("/sync/" + "capture", arr, successCallback, failureCallback);
    }

    public void syncCheckEvent(List<Event> ids, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) throws Exception {

        if (ids.size() == 0) return;

        JSONArray arr = new JSONArray();
        for (Event event : ids) {
            JSONObject o = new JSONObject();

            o.put("deviceId", Config.getUniqueDeviceId(context));
            o.put("id", event.getId());
            o.put("timestamp", dateFormat.format(event.getTimestamp()));

            arr.put(o);
        }

        send("/sync/" + "event", arr, successCallback, failureCallback);
    }

    public void sendKnock(HashMap<String, String> dict, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) {
        try {
            JSONArray arr = new JSONArray();
            JSONObject o = new JSONObject();

            o.put("deviceId", Config.getUniqueDeviceId(context));
            o.put("deviceName", Config.getDeviceName(context));
            o.put("oemDeviceName", android.os.Build.MODEL);
            o.put("androidVersion", Build.VERSION.RELEASE);
            o.put("timestamp", dateFormat.format(Calendar.getInstance().getTime()));
            o.put("apkBuildTime", dateFormat.format(BuildConfig.buildTime));

            JSONObject p = new JSONObject();
            for (Map.Entry<String, String> entry  : dict.entrySet()) {
                p.put(entry.getKey(), entry.getValue());
            }
            o.put("cameraParameters", p);

            arr.put(o);

            send("/knock", arr, successCallback, failureCallback);
        } catch (Exception e) {
            Log.e(TAG, "sending knock failed", e);
        }
    }

    public void sendStatus(List<Status> statusList, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) {
        try {
            JSONArray arr = new JSONArray();
            for (Status status : statusList) {
                JSONObject o = new JSONObject();

                o.put("deviceId", Config.getUniqueDeviceId(context));

                o.put("statusId", status.getId());
                o.put("timestamp", dateFormat.format(status.getTimestamp()));
                o.put("deviceName", Config.getDeviceName(context)); // ?

                o.put("imagesTaken", status.getNumberImagesTaken());
                o.put("imagesInMemory", status.getNumberImagesInMemory());

                o.put("freeSpaceInternal", status.getFreeSpaceInternal());
                o.put("freeSpaceExternal", status.getFreeSpaceExternal());

                o.put("batteryInternal", status.getBatteryInternal());
                o.put("batteryExternal", status.getBatteryExternal());

                o.put("stateCharging", status.isStateCharging());

                o.put("temperatureDevice", status.getTemperatureDevice());
                o.put("temperatureBattery", status.getTemperatureBattery());

                o.put("freeMemoryHeap", status.getFreeMemoryHeap());
                o.put("freeMemoryHeapNative", status.getFreeMemoryHeapNative());

                arr.put(o);
            }

//            if (Util.isServiceRunning(context, ShutterService.class)) {
//                o.put("status", StatusType.CAPTURING);
//            } else {
//                SystemController systemController = new SystemController(context);
//                if (systemController.isDisplayOn()) {
//                    o.put("status", StatusType.DISPLAY_ON);
//                } else {
//                    o.put("status", StatusType.IDLE);
//                }
//            }

            send("/status", arr, successCallback, failureCallback);
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    public void sendSession(List<Session> sessionList, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) {
        try {

            JSONArray arr = new JSONArray();
            for (Session session : sessionList) {
                JSONObject o = new JSONObject();

                o.put("deviceId", Config.getUniqueDeviceId(context));
                o.put("sessionId", session.getId());

                o.put("start", dateFormat.format(session.getStart()));
                String end = session.getEnd() == null ? null : dateFormat.format(session.getEnd());
                o.put("end", allowNull(end));

                o.put("latitude", allowNull(session.getLatitude()));
                o.put("longitude", allowNull(session.getLongitude()));

                o.put("imageWidth", allowNull(session.getImageWidth()));
                o.put("imageHeight", allowNull(session.getImageHeight()));

                o.put("homographyMatrix", allowNull(session.getHomographyMatrix()));

                o.put("resumed", allowNull(session.isResumed()));

                o.put("shutterInterval", allowNull(session.getShutterInterval()));
                o.put("exposureThreshold", allowNull(session.getExposureThreshold()));
                o.put("exposureCompensation", allowNull(session.getExposureCompensation()));

                arr.put(o);
            }

            send("/session", arr, successCallback, failureCallback);
        } catch (Exception e) {
            Log.e(TAG, "sending session failed", e);
        }
    }

    public void sendCapture(List<Capture> captureList, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) {
        try {

            JSONArray arr = new JSONArray();
            for (Capture capture : captureList) {
                JSONObject o = new JSONObject();

                o.put("deviceId", Config.getUniqueDeviceId(context));

                o.put("captureId", capture.getId());
                o.put("sessionId", capture.getSessionId());
                o.put("recordingTime", dateFormat.format(capture.getRecordingTime()));
                o.put("exposureTime", capture.getExposureTime());
                if (!Double.isNaN(capture.getAperture())) {
                    o.put("aperture", capture.getAperture());
                } else {
                    o.put("aperture", -1);
                }
                o.put("iso", capture.getIso());

                double exposureValue = Util.computeExposureValue(capture.getExposureTime(), capture.getAperture(), capture.getIso());
                if (exposureValue < 0 || Double.isNaN(exposureValue)) {
                    exposureValue = -1.0;
                }
                o.put("exposureValue", exposureValue);

                arr.put(o);
            }

            send("/capture", arr, successCallback, failureCallback);
        } catch (Exception e) {
            Log.e(TAG, "sending capture failed", e);
        }
    }

    public void sendEvent(List<Event> eventList, RequestSuccessCallback successCallback, RequestFailureCallback failureCallback) {
        try {

            JSONArray arr = new JSONArray();
            for (Event event : eventList) {
                JSONObject o = new JSONObject();

                o.put("deviceId", Config.getUniqueDeviceId(context));

                o.put("eventId", event.getId());
                o.put("timestamp", dateFormat.format(event.getTimestamp()));

                o.put("type", event.getType());
                o.put("payload", allowNull(event.getPayload()));

                arr.put(o);
            }

            send("/event", arr, successCallback, failureCallback);
        } catch (Exception e) {
            Log.e(TAG, "sending event failed", e);
        }
    }


    private void send(String endpoint, JSONArray messages) {
        send(endpoint, messages, null, null);
    }

    private void send(String endpoint, JSONArray jsonMessages, final RequestSuccessCallback successCallback, final RequestFailureCallback failureCallback) {

        String url = this.serverAddress + endpoint;

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                String statusCode = "-";

                if (error.networkResponse != null) {
                    statusCode = Integer.toString(error.networkResponse.statusCode);
                }

                Log.e(TAG, String.format(Config.LOCALE, "sending data to server failed: [%s] %s", statusCode, error));

                // TODO: fire toast

                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    JSONArray obj = null;
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));

                        try {
                            obj = new JSONArray(res);

                            // error response is a JSON Array ...
                        } catch (JSONException e2) {
                            // error response is probably HTML

                            System.out.println(res);
                        }
                    } catch (UnsupportedEncodingException uee) {
                        Log.e(TAG, "parsing error response failed");
                    } finally {
                        if (failureCallback != null) failureCallback.failure(obj);
                    }
                } else {
                    if (failureCallback != null) failureCallback.failure(null);
                }
            }
        };

        Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response == null) {
                    Log.d(TAG, String.format(Config.LOCALE, "Success. Response null"));
                } else if (response.toString().equals("{}") || response.toString().equals("[]") ) {
                    Log.d(TAG, String.format(Config.LOCALE, "Success Response empty"));
                } else {
                    Log.d(TAG, String.format(Config.LOCALE, "Success Response: %s", response.toString()));
                }

                if (successCallback != null) successCallback.success(response);
            }
        };

        Map<String, String> params = new HashMap<String, String>();
        JSONArrayRequest request = new JSONArrayRequest(Request.Method.POST, url, jsonMessages, params, successListener, errorListener, context);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public void sendUpload(UploadMessage msg) {
        try {

            msg.deviceId = Config.getUniqueDeviceId(context);
            msg.timestamp = Calendar.getInstance().getTime();

            sendImage("/upload", msg);
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    // partially taken from: https://stackoverflow.com/questions/32240177/working-post-multipart-request-with-volley-and-without-httpentity
    private void sendImage(String endpoint, UploadMessage msg) {

        String url = this.serverAddress + endpoint;
        File image = msg.image;

        byte[] multipartBody = {};

        try {
            byte[] fileData1 = Util.readFileToByteArray(image);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try {
                multipartText(dos, "deviceId", msg.deviceId);
                multipartText(dos, "timestamp", dateFormat.format(msg.timestamp));
                multipartFile(dos, fileData1, "image.jpg");

                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                multipartBody = bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MultipartRequest multipartRequest = new MultipartRequest(url, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    Log.d(TAG, "image uploaded");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        Log.e(TAG, "image upload failed"); // TODO
                        Log.e(TAG, error.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, context);

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(multipartRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void multipartText(DataOutputStream dataOutputStream, String key, String value) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        dataOutputStream.writeBytes(value);

        dataOutputStream.writeBytes(lineEnd);
    }

    private void multipartFile(DataOutputStream dataOutputStream, byte[] fileData, String fileName) throws IOException {

        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    public List<Integer> parseJsonResponse(JSONArray response) throws JSONException {

        List<Integer> ids = new ArrayList<Integer>(response.length());

        for (int i=0; i < response.length(); i++) {
            ids.add(response.getInt(i));
        }

        return ids;
    }

    public Object allowNull(Object o) {
        if (o == null) return JSONObject.NULL;
        return o;
    }

    // --------------------------------------------------------------------------------------------------------------

    public static abstract class RequestSuccessCallback {
        public void success(JSONArray response) {}
    }

    public static abstract class RequestFailureCallback {
        public void failure(JSONArray response) {}
    }

    public static class UploadMessage {

        public String deviceId;
        public Date timestamp;

        public File image;
    }
}
