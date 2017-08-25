package de.volzo.despat;

import android.content.Context;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.volzo.despat.support.Config;

/**
 * Created by volzotan on 10.08.17.
 */

public class ServerConnector {

    public static final String TAG = ServerConnector.class.getName();

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
    public void sendStatus(ServerMessage msg) {
        try {
            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();

            jsonWriter.name("deviceId").value(Config.getUniqueDeviceId(context));
            jsonWriter.name("deviceName").value(Config.getDeviceName(context));
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));

            jsonWriter.name("numberImages").value(msg.numberImages);
            jsonWriter.name("freeSpaceInternal").value(msg.freeSpaceInternal);
            jsonWriter.name("freeSpaceExternal").value(msg.freeSpaceExternal);
            jsonWriter.name("batteryInternal").value(msg.batteryInternal);
            jsonWriter.name("batteryExternal").value(msg.batteryExternal);
            jsonWriter.name("stateCharging").value(msg.stateCharging);
            jsonWriter.endObject();
            jsonWriter.close();

            Log.d(TAG, jsonWriter.toString());
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }


    public void send(JSONObject statusMessage) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = this.serverAddress + "/state";

        class CustomRequest extends Request<JSONObject> {

            private Response.Listener<JSONObject> listener;
            private Map<String, String> params;
            private JSONObject payload;

            public CustomRequest(String url, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
                super(Method.POST, url, errorListener);
                this.listener = reponseListener;
                this.params = params;
            }

            public CustomRequest(int method, String url, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
                super(method, url, errorListener);
                this.listener = reponseListener;
                this.params = params;
            }

            public CustomRequest(int method, String url, JSONObject payload, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
                super(method, url, errorListener);
                this.payload = payload;
                this.listener = reponseListener;
                this.params = params;
            }

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(
                        "Authorization",
                        String.format("Basic %s", Base64.encodeToString(
                                String.format("%s:%s",
                                        context.getResources().getString(R.string.server_username),
                                        context.getResources().getString(R.string.server_password)
                                ).getBytes(), Base64.DEFAULT)
                        )
                );
                return params;
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
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    Log.e(TAG, new String(response.data));
                    return Response.error(new ParseError(je));
                }
            }
        }

        Map<String, String> params = new HashMap<String, String>();
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, statusMessage, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Success Response: "+ response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError response) {
                Log.d("Error Response: ", response.toString());
            }
        });
        queue.add(jsObjRequest);
    }

    public static class ServerMessage {

        public int numberImages;
        public float freeSpaceInternal;
        public float freeSpaceExternal;

        public int batteryInternal;
        public int batteryExternal;
        public boolean stateCharging;
    }

}
