package de.volzo.despat.support;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Size;

import java.util.HashMap;
import java.util.List;

import de.volzo.despat.CameraController2;
import de.volzo.despat.SystemController;
import de.volzo.despat.preferences.Config;

public class DeviceInfo {

    String vendor;
    String identifier;
    String id;
    String name;

    List<CameraInfo> cameras;

    float freeSpace;
    float batteryTemperature;
    boolean gyro;

    public DeviceInfo(Context context) {
        vendor = android.os.Build.MANUFACTURER;
        identifier = android.os.Build.MODEL;
        id = Config.getUniqueDeviceId(context);
        name = Config.getDeviceName(context);

        try {
            cameras = CameraController2.getCameraInfo(context);
        } catch (Exception e) {}

        freeSpace = Util.getFreeSpaceOnDeviceInMb(Config.getImageFolder(context));
        SystemController systemController = new SystemController(context);
        batteryTemperature = systemController.getBatteryTemperature();
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = true;
        } else {
            gyro = false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SYSTEM INIT");
        sb.append("\n");

        sb.append(String.format("%-20s", "Device vendor:"));
        sb.append(String.format("%20s",  vendor));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device identifier:"));
        sb.append(String.format("%20s",  identifier));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device id:"));
        sb.append(String.format("%20s",  id));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device name:"));
        sb.append(String.format("%20s",  name));
        sb.append("\n");

        sb.append("----------------------------------------\n");
        for (CameraInfo c : cameras) {
            sb.append(c.toString());
            sb.append("\n----------------------------------------\n");
        }

        sb.append(String.format("%-20s", "free space [mb]:"));
        sb.append(String.format(Config.LOCALE, "%20.2f",  freeSpace));
        sb.append("\n");

        sb.append(String.format("%-20s", "batt temp [°C]:"));
        sb.append(String.format(Config.LOCALE, "%20.1f",  batteryTemperature));
        sb.append("\n");

        sb.append(String.format("%-20s", "gyro:"));
        sb.append(String.format("%20s",  gyro));

        sb.append("\n");

        return sb.toString();
    }

    public static class CameraInfo {
        String id;
        String direction;
        int width;
        int height;
        HashMap<String, String> parameters;

        public CameraInfo(String id, String direction, Size resolution, HashMap<String, String> parameters) {
            this.id = id;
            this.direction = direction;
            this.width = resolution.getWidth();
            this.height = resolution.getHeight();
            this.parameters = parameters;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("%-20s", "camera id:"));
            sb.append(String.format("%20s", id));
            sb.append("\n");

            sb.append(String.format("%-20s", "camera direction:"));
            sb.append(String.format("%20s", direction));
            sb.append("\n");

            sb.append(String.format("%-20s", "camera img size:"));
            sb.append(String.format("%20s", width + "x" + height));
            sb.append("\n");

            sb.append(String.format("%-20s", "camera resolution:"));
            sb.append(String.format(Config.LOCALE, "%20.2f", (width*height)/1000000.0));
            sb.append("\n");

            sb.append(String.format("%-20s", "parameters:"));
            sb.append(String.format("%20s", parameters.size()));

            return sb.toString();
        }
    }

}
