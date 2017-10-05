package de.volzo.despat.web;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import de.volzo.despat.R;

/**
 * Created by christophergetschmann on 05.10.17.
 */

public class JSONRequest extends Request<JSONObject> {

    public static final String TAG = JSONRequest.class.getSimpleName();

    private Context context;

    private Response.Listener<JSONObject> listener;
    private Map<String, String> params;
    private JSONObject payload;

    public JSONRequest(int method, String url, JSONObject payload, Map<String, String> params,
                         Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener, Context context) {
        super(method, url, errorListener);
        this.payload = payload;
        this.listener = responseListener;
        this.params = params;

        this.context = context;
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

