package de.volzo.despat.web;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.volzo.despat.SystemController;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 10.08.17.
 */

public class ServerConnector {

    public static final String TAG = ServerConnector.class.getSimpleName();

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

    public void sendStatus(Status status) {
        try {

            // TODO: json list of status, not single status

            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();

            jsonWriter.name("deviceId").value(Config.getUniqueDeviceId(context));
            jsonWriter.name("deviceName").value(Config.getDeviceName(context));
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));

            if (Util.isServiceRunning(context, ShutterService.class)) {
                jsonWriter.name("status").value(StatusType.CAPTURING);
            } else {
                SystemController systemController = new SystemController(context);
                if (systemController.isDisplayOn()) {
                    jsonWriter.name("status").value(StatusType.DISPLAY_ON);
                } else {
                    jsonWriter.name("status").value(StatusType.IDLE);
                }
            }

            jsonWriter.name("numberImagesTaken").value(status.getNumberImagesTaken());
            jsonWriter.name("numberImagesSaved").value(status.getNumberImagesInMemory());
            jsonWriter.name("freeSpaceInternal").value(status.getFreeSpaceInternal());
            jsonWriter.name("freeSpaceExternal").value(status.getFreeSpaceExternal());
            jsonWriter.name("batteryInternal").value(status.getBatteryInternal());
            jsonWriter.name("batteryExternal").value(status.getBatteryExternal());
            jsonWriter.name("stateCharging").value(status.isStateCharging());

            jsonWriter.endObject();
            jsonWriter.close();

            // Log.d(TAG, writer.toString());

            send("/status", new JSONObject(writer.toString()));
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    public void sendEvent(int type, String payload) {
        try {

            if (payload == null) {
                payload = new String();
            }

//            EventMessage msg = new EventMessage();

            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();

            jsonWriter.name("deviceId").value(Config.getUniqueDeviceId(context));
            jsonWriter.name("deviceName").value(Config.getDeviceName(context));
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));

            jsonWriter.name("eventtype").value(type);
            jsonWriter.name("payload").value(payload);

            jsonWriter.endObject();
            jsonWriter.close();

            send("/event", new JSONObject(writer.toString()));
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    private void send(String endpoint, JSONObject jsonMessage) {

        String url = this.serverAddress + endpoint;

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, String.format("sending data to server failed: %s", error)); //%d", error.networkResponse.statusCode));

                // TODO: fire toast

                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));

                        try {
                            JSONObject obj = new JSONObject(res);

                            // error response is a JSON Object ...
                        } catch (JSONException e2) {
                            // error response is probably HTML

                            System.out.println(res);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        Log.e(TAG, "parsing error response failed");
                    }
                }
            }
        };

        Response.Listener successListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                if (response == null) {
                    Log.d(TAG, String.format("Success. Response null"));
                } else if (response.toString().equals("{}")) {
                    Log.d(TAG, String.format("Success Response empty"));
                } else {
                    Log.d(TAG, String.format("Success Response: %s", response.toString()));
                }
            }
        };

        Map<String, String> params = new HashMap<String, String>();
        JSONRequest jsObjRequest = new JSONRequest(Request.Method.POST, url, jsonMessage, params, successListener, errorListener, context);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsObjRequest);
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

    // --------------------------------------------------------------------------------------------------------------

//    public static class EventMessage {
//
//        public String deviceId;
//        public String deviceName;
//        public String originalDeviceId;
//        public Date timestamp;
//
//        public int eventtype;
//        public String payload;
//    }

    public static class UploadMessage {

        public String deviceId;
        public Date timestamp;

        public File image;
    }

//    public static class EventType {
//
//        public static final int INIT        = 0x10;
//        public static final int BOOT        = 0x11;
//        public static final int SHUTDOWN    = 0x12;
//
//        public static final int START       = 0x20;
//        public static final int STOP        = 0x21;
//
//        public static final int ERROR       = 0x30;
//    }

    public static class StatusType {

        public static final int IDLE        = 0x10;
        public static final int DISPLAY_ON  = 0x11;
        public static final int CAPTURING   = 0x12;

        public static final int ERROR       = 0x30;
    }

}
