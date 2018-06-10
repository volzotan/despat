package de.volzo.despat.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.volzo.despat.R;

/**
 * Created by christophergetschmann on 05.10.17.
 */

public class MultipartRequest extends Request<NetworkResponse> {

    private static final String TAG = MultipartRequest.class.getSimpleName();

    private final Context context;
    private final String url;

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final String mMimeType;
    private final byte[] mMultipartBody;

    public MultipartRequest(String url, String mimeType, byte[] multipartBody, Response.Listener<NetworkResponse> listener, Response.ErrorListener
            errorListener, Context context) {

        super(Method.POST, url, errorListener);

        this.url = url;
        this.context = context;

        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mMimeType = mimeType;
        this.mMultipartBody = multipartBody;

    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
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
        return mMimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mMultipartBody;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}