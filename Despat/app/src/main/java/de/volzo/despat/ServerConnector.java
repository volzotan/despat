package de.volzo.despat;

import android.util.JsonWriter;
import android.util.Log;

import java.io.StringWriter;
import java.io.Writer;

import de.volzo.despat.support.Config;

/**
 * Created by volzotan on 10.08.17.
 */

public class ServerConnector {

    public static final String TAG = ServerConnector.class.getName();

    private String serverAddress;

    public ServerConnector() {
        serverAddress = Config.SERVER_ADDRESS;
    }

    /*

    device_id               String
    timestamp               human readable

    number_images           123
    free_space_internal     123.0
    free_space_external     -1.0

    battery_internal        0 - 100
    battery_external        0 - 100
    state_charging          true

    */
    public void sendStatus(ServerMessage msg) {
        try {
            Writer writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();
            jsonWriter.name("battery_internal").value(100);
            jsonWriter.name("battery_external").value(99);
            jsonWriter.name("state_charging").value(true);
            jsonWriter.endObject();
            jsonWriter.close();

            Log.d(TAG, jsonWriter.toString());
        } catch (Exception e) {
            Log.e(TAG, "sending status failed", e);
        }
    }

    public class ServerMessage {

        int number_images;
        float free_space_internal;
        float free_space_external;

        int battery_internal;
        int battery_external;
        boolean state_charging;
    }

}
