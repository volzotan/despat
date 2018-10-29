package de.volzo.despat.support;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Size;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.volzo.despat.BuildConfig;
import de.volzo.despat.CameraController2;
import de.volzo.despat.SystemController;
import de.volzo.despat.preferences.Config;

public class DeviceInfo {

    String vendor;
    String identifier;
    String id;
    String version;
    String name;
    String despatVersion;
    String despatBuildTime;

    List<CameraInfo> cameras;

    float freeSpace;
    float batteryTemperature;
    boolean gyro;

    public DeviceInfo(Context context) {
        vendor = android.os.Build.MANUFACTURER;
        identifier = android.os.Build.MODEL;
        id = Config.getUniqueDeviceId(context);
        version = Build.VERSION.RELEASE;
        name = Config.getDeviceName(context);
        despatVersion = "";
        despatBuildTime = (new SimpleDateFormat(Config.DATEFORMAT_SHORT, Config.LOCALE)).format(BuildConfig.buildTime);

        try {
            cameras = CameraController2.getCameraInfo(context);
        } catch (Exception e) {}

        freeSpace = Util.getFreeSpaceOnDeviceInMb(Config.getImageFolder(context));
        SystemController systemController = new SystemController(context);
        batteryTemperature = systemController.getBatteryTemperature();
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = true;
        } else {
            gyro = false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SYSTEM INIT");
        sb.append("\n");
        sb.append("----------------------------------------\n");

        sb.append(String.format("%-20s", "Device vendor:"));
        sb.append(String.format("%20s",  vendor));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device identifier:"));
        sb.append(String.format("%20s",  identifier));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device id:"));
        sb.append(String.format("%20s",  id));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device version:"));
        sb.append(String.format("%20s",  version));
        sb.append("\n");

        sb.append(String.format("%-20s", "Device name:"));
        sb.append(String.format("%20s",  name));
        sb.append("\n");

        sb.append(String.format("%-20s", "Despat version:"));
        sb.append(String.format("%20s",  despatVersion));
        sb.append("\n");

        sb.append(String.format("%-20s", "Despat buildTime:"));
        sb.append(String.format("%20s",  despatBuildTime));
        sb.append("\n");

        sb.append("----------------------------------------\n");
        for (CameraInfo c : cameras) {
            sb.append(c.toString());
            sb.append("----------------------------------------\n");
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

        sb.append("----------------------------------------\n");

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

        private String checkForNullString(String info) {
            if (info == null || info.equals("null")) {
                return "---";
            }
            return info;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            String info;

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
            sb.append("\n");

            sb.append(String.format("%-20s", "  info:"));
            sb.append(String.format("%20s", checkForNullString(parameters.get("INFO_VERSION"))));
            sb.append("\n");

            sb.append(String.format("%-20s", "  max zoom:"));
            sb.append(String.format("%20s", parameters.get("SCALER_AVAILABLE_MAX_DIGITAL_ZOOM")));
            sb.append("\n");

            sb.append(String.format("%-20s", "  sensor size:"));
            info = "---";
            try {
                info = parameters.get("SENSOR_INFO_PHYSICAL_SIZE");
                info = String.format(Config.LOCALE, "%2.1fx%2.1f", Float.parseFloat(info.split("x")[0]), Float.parseFloat(info.split("x")[1]));
            } catch (Exception e) {}
            sb.append(String.format("%20s", info));
            sb.append("\n");

            sb.append(String.format("%-20s", "  pixel array:"));
            info = "---";
            try {
                info = parameters.get("SENSOR_INFO_PIXEL_ARRAY_SIZE");
            } catch (Exception e) {}
            sb.append(String.format("%20s", info));
            sb.append("\n");

            sb.append(String.format("%-20s", "  max iso:"));
            sb.append(String.format("%20s", checkForNullString(parameters.get("SENSOR_MAX_ANALOG_SENSITIVITY"))));
            sb.append("\n");

            sb.append(String.format("%-20s", "  exposure range:"));
            info = "---";
            try {
                info = parameters.get("CONTROL_AE_COMPENSATION_RANGE");
            } catch (Exception e) {}
            sb.append(String.format("%20s", info));
            sb.append("\n");

            sb.append(String.format("%-20s", "  exposure steps:"));
            info = "---";
            try {
                info = parameters.get("CONTROL_AE_COMPENSATION_STEP");
            } catch (Exception e) {}
            sb.append(String.format("%20s", info));
            sb.append("\n");

            sb.append(String.format("%-20s", "  max exposure [ms]:"));
            info = "---";
            try {
                info = Long.toString(TimeUnit.MILLISECONDS.convert(Long.parseLong(parameters.get("SENSOR_INFO_MAX_FRAME_DURATION")), TimeUnit.NANOSECONDS));
            } catch (Exception e) {}
            sb.append(String.format("%20s", info));
            sb.append("\n");

//            sb.append(String.format("%-20s", "  real pixel grid:"));
//            sb.append(String.format("%20s", parameters.get("SENSOR_INFO_ACTIVE_ARRAY_SIZE")));
//            sb.append("\n");


            return sb.toString();
        }
    }

}
