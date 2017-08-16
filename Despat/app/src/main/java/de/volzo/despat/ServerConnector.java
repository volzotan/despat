package de.volzo.despat;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
            jsonWriter.name("timestamp").value(dateFormat.format(Calendar.getInstance().getTime()));
            jsonWriter.name("batteryInternal").value(100);
            jsonWriter.name("batteryExternal").value(99);
            jsonWriter.name("stateCharging").value(true);
            jsonWriter.endObject();
            jsonWriter.close();

            Log.d(TAG, jsonWriter.toString());
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    public class ServerMessage {

        int numberImages;
        float freeSpaceInternal;
        float freeSpaceExternal;

        int batteryInternal;
        int batteryExternal;
        boolean stateCharging;
    }

}
