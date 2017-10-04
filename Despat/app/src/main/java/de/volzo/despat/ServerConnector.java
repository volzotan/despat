package de.volzo.despat;

import android.content.Context;
import android.util.Base64;
import android.util.EventLog;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ExecutorDelivery;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.volzo.despat.support.Config;

/**
 * Created by volzotan on 10.08.17.
 */

public class ServerConnector {

    public static final String TAG = ServerConnector.class.getSimpleName();

    private Context context;
    private String serverAddress;
    SimpleDateFormat dateFormat;


    public ServerConnector(Context context) {
        this.context = context;
        this.serverAddress = Config.SERVER_ADDRESS;

        this.dateFormat =  new SimpleDateFormat(Config.dateFormat, new Locale("de", "DE"));
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

    public void sendStatus(StatusMessage msg) {
        try {
            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();

            jsonWriter.name("deviceId").value(Config.getUniqueDeviceId(context));
            jsonWriter.name("deviceName").value(Config.getDeviceName(context));
            //jsonWriter.name("originalDeviceId").value("");
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));

            jsonWriter.name("numberImages").value(msg.numberImages);
            jsonWriter.name("freeSpaceInternal").value(msg.freeSpaceInternal);
            jsonWriter.name("freeSpaceExternal").value(msg.freeSpaceExternal);
            jsonWriter.name("batteryInternal").value(msg.batteryInternal);
            jsonWriter.name("batteryExternal").value(msg.batteryExternal);
            jsonWriter.name("stateCharging").value(msg.stateCharging);
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

            if (payload == null) payload = new String();

            EventMessage msg = new EventMessage();

            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();

            jsonWriter.name("deviceId").value(Config.getUniqueDeviceId(context));
            jsonWriter.name("deviceName").value(Config.getDeviceName(context));
            //jsonWriter.name("originalDeviceId").value("");
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));

            jsonWriter.name("eventtype").value(msg.eventtype);
            jsonWriter.name("payload").value(msg.payload);
            jsonWriter.endObject();
            jsonWriter.close();

            send("/event", new JSONObject(writer.toString()));
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    public void sendUpload(UploadMessage msg) {
        try {
            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();

            jsonWriter.name("deviceId").value(Config.getUniqueDeviceId(context));
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));

            jsonWriter.endObject();
            jsonWriter.close();

            // Log.d(TAG, writer.toString());

            send("/image", new JSONObject(writer.toString()));
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }


    public void send(String endpoint, JSONObject statusMessage) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = this.serverAddress + endpoint;

        class CustomRequest extends Request<JSONObject> {

            private Response.Listener<JSONObject> listener;
            private Map<String, String> params;
            private JSONObject payload;

            public CustomRequest(String url, Map<String, String> params,
                                 Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
                super(Method.POST, url, errorListener);
                this.listener = responseListener;
                this.params = params;
            }

            public CustomRequest(int method, String url, Map<String, String> params,
                                 Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
                super(method, url, errorListener);
                this.listener = responseListener;
                this.params = params;
            }

            public CustomRequest(int method, String url, JSONObject payload, Map<String, String> params,
                                 Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
                super(method, url, errorListener);
                this.payload = payload;
                this.listener = responseListener;
                this.params = params;
            }

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {

                String username = context.getResources().getString(R.string.server_username);
                String password = context.getResources().getString(R.string.server_password);
                String code = username + ":" + password;
                code = Base64.encodeToString(code.getBytes(), Base64.DEFAULT);

                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Basic " + code);

                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
//                try {
//                    httpPostBody=httpPostBody+"&randomFieldFilledWithAwkwardCharacters="+URLEncoder.encode("{{%stuffToBe Escaped/","UTF-8");
//                } catch (UnsupportedEncodingException exception) {
//                    Log.e("ERROR", "exception", exception);
//                    // return null and don't pass any POST string if you encounter encoding error
//                    return null;
//                }
                return payload.toString().getBytes();
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                listener.onResponse(response);
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                Log.d(TAG, String.format("HTTP response: %d", response.statusCode));

                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    if (jsonString.length() != 0) {
                        return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                    } else {
                        return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
                    }
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    Log.e(TAG, new String(response.data));
                    return Response.error(new ParseError(je));
                }
            }
        }

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, String.format("sending data to server failed. %s", error)); //%d", error.networkResponse.statusCode));

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
                Log.d(TAG, String.format("Success Response: %s", response.toString()));
            }
        };

        Map<String, String> params = new HashMap<String, String>();
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, statusMessage, params, successListener, errorListener);

//        try {
//            Log.wtf(TAG, new String(jsObjRequest.getBody(), "utf-8"));
//        } catch (Exception e) {}

        queue.add(jsObjRequest);
    }

    public static class StatusMessage {

//        public String deviceId;
//        public String deviceName;
//        public String originalDeviceId;
//        public Date timestamp;

        public int numberImages;
        public float freeSpaceInternal;
        public float freeSpaceExternal;

        public int batteryInternal;
        public int batteryExternal;
        public boolean stateCharging;
    }

    public static class EventMessage {

        public String deviceId;
        public String deviceName;
        public String originalDeviceId;
        public Date timestamp;

        public int eventtype;
        public String payload;
    }

    public static class UploadMessage {

        public String deviceId;
        public Date timestamp;

        public File image;
    }

    public static class EventType {

        public static final int INIT        = 0x0;
        public static final int BOOT        = 0x1;
        public static final int SHUTDOWN    = 0x2;

        public static final int START       = 0x10;
        public static final int STOP        = 0x11;

        public static final int ERROR       = 0x30;
    }

}
