package de.volzo.despat.support;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.volzo.despat.BuildConfig;
import de.volzo.despat.CameraController2;
import de.volzo.despat.SystemController;
import de.volzo.despat.preferences.Config;

public class DeviceInfo {

    private static final String TAG = DeviceInfo.class.getSimpleName();

    String vendor;
    String identifier;
    String id;
    String version;
    String buildType;
    String name;
    String despatVersionName;
    int despatVersionCode;
    String despatBuildTime;

    List<CameraInfo> cameras;

    float freeSpaceInternal;
    float freeSpaceExternal;
    float batteryTemperature;
    boolean gyro;

    public DeviceInfo(Context context) {
        vendor = android.os.Build.MANUFACTURER;
        identifier = android.os.Build.MODEL;
        id = Config.getUniqueDeviceId(context);
        version = Build.VERSION.RELEASE;
        buildType = BuildConfig.BUILD_TYPE;
        name = Config.getDeviceName(context);
        despatVersionName = BuildConfig.VERSION_NAME;
        despatVersionCode = BuildConfig.VERSION_CODE;
        despatBuildTime = (new SimpleDateFormat(Config.DATEFORMAT_SHORT, Config.LOCALE)).format(BuildConfig.buildTime);

        try {
            cameras = CameraController2.getCameraInfo(context);
        } catch (Exception e) {}

        freeSpaceInternal = 0;
        freeSpaceExternal = -1;
        List<File> imageFolders = Config.getImageFolders(context);
        freeSpaceInternal = Util.getFreeSpaceOnDeviceInMb(imageFolders.get(0));
        if (imageFolders.size() > 1) {
            freeSpaceExternal = Util.getFreeSpaceOnDeviceInMb(imageFolders.get(1));
        }

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

        sb.append(String.format("%-30s", "Device vendor:"));
        sb.append(String.format("%30s",  vendor));
        sb.append("\n");

        sb.append(String.format("%-30s", "Device identifier:"));
        sb.append(String.format("%30s",  identifier));
        sb.append("\n");

        sb.append(String.format("%-30s", "Device id:"));
        sb.append(String.format("%30s",  id));
        sb.append("\n");

        sb.append(String.format("%-30s", "Device version:"));
        sb.append(String.format("%30s",  version));
        sb.append("\n");

        sb.append(String.format("%-30s", "Build  type:"));
        sb.append(String.format("%30s",  buildType));
        sb.append("\n");

        sb.append(String.format("%-30s", "Device name:"));
        sb.append(String.format("%30s",  name));
        sb.append("\n");

        sb.append(String.format("%-30s", "Despat version name:"));
        sb.append(String.format("%30s",  despatVersionName));
        sb.append("\n");

        sb.append(String.format("%-30s", "Despat version:"));
        sb.append(String.format("%20d",  despatVersionCode));
        sb.append("\n");

        sb.append(String.format("%-30s", "Despat buildTime:"));
        sb.append(String.format("%30s",  despatBuildTime));
        sb.append("\n");

        sb.append("----------------------------------------\n");
        for (CameraInfo c : cameras) {
            sb.append(c.toString());
            sb.append("----------------------------------------\n");
        }

        sb.append(String.format("%-30s", "free space internal [mb]:"));
        sb.append(String.format(Config.LOCALE, "%20.2f",  freeSpaceInternal));
        sb.append("\n");

        sb.append(String.format("%-30s", "free space external [mb]:"));
        sb.append(String.format(Config.LOCALE, "%20.2f",  freeSpaceExternal));
        sb.append("\n");

        sb.append(String.format("%-30s", "batt temp [°C]:"));
        sb.append(String.format(Config.LOCALE, "%20.1f",  batteryTemperature));
        sb.append("\n");

        sb.append(String.format("%-30s", "gyro:"));
        sb.append(String.format("%30s",  gyro));
        sb.append("\n");

        sb.append("----------------------------------------\n");

        return sb.toString();
    }

    public static class CameraInfo {
        String id;
        String direction;
        int width;
        int height;
        boolean rawSupport;
        HashMap<String, String> parameters;

        public CameraInfo(String id, String direction, Size resolution, boolean rawSupport, HashMap<String, String> parameters) {
            this.id = id;
            this.direction = direction;
            this.width = resolution.getWidth();
            this.height = resolution.getHeight();
            this.rawSupport = rawSupport;
            this.parameters = parameters;
        }

        private String checkForNullString(String info) {
            if (info == null || info.equals("null")) {
                return "---";
            }
            return info;
        }

        public String getId() {
            return id;
        }

        public String getDirection() {
            return direction;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean getRawSupport() {
            return rawSupport;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            String info;

            sb.append(String.format("%-30s", "camera id:"));
            sb.append(String.format("%30s", id));
            sb.append("\n");

            sb.append(String.format("%-30s", "camera direction:"));
            sb.append(String.format("%30s", direction));
            sb.append("\n");

            sb.append(String.format("%-30s", "camera img size:"));
            sb.append(String.format("%30s", width + "x" + height));
            sb.append("\n");

            sb.append(String.format("%-30s", "camera resolution:"));
            sb.append(String.format(Config.LOCALE, "%20.2f", (width*height)/1000000.0));
            sb.append("\n");

            sb.append(String.format("%-30s", "RAW support:"));
            sb.append(String.format(Config.LOCALE, "%20b", rawSupport));
            sb.append("\n");

            sb.append(String.format("%-30s", "parameters:"));
            sb.append(String.format("%30s", parameters.size()));
            sb.append("\n");

            sb.append(String.format("%-30s", "  info:"));
            sb.append(String.format("%30s", checkForNullString(parameters.get("INFO_VERSION"))));
            sb.append("\n");

            sb.append(String.format("%-30s", "  max zoom:"));
            sb.append(String.format("%30s", parameters.get("SCALER_AVAILABLE_MAX_DIGITAL_ZOOM")));
            sb.append("\n");

            sb.append(String.format("%-30s", "  sensor size:"));
            info = "---";
            try {
                info = parameters.get("SENSOR_INFO_PHYSICAL_SIZE");
                info = String.format(Config.LOCALE, "%2.1fx%2.1f", Float.parseFloat(info.split("x")[0]), Float.parseFloat(info.split("x")[1]));
            } catch (Exception e) {}
            sb.append(String.format("%30s", info));
            sb.append("\n");

            sb.append(String.format("%-30s", "  pixel array:"));
            info = "---";
            try {
                info = parameters.get("SENSOR_INFO_PIXEL_ARRAY_SIZE");
            } catch (Exception e) {}
            sb.append(String.format("%30s", info));
            sb.append("\n");

            sb.append(String.format("%-30s", "  max iso:"));
            sb.append(String.format("%30s", checkForNullString(parameters.get("SENSOR_MAX_ANALOG_SENSITIVITY"))));
            sb.append("\n");

            sb.append(String.format("%-30s", "  exposure range:"));
            info = "---";
            try {
                info = parameters.get("CONTROL_AE_COMPENSATION_RANGE");
            } catch (Exception e) {}
            sb.append(String.format("%30s", info));
            sb.append("\n");

            sb.append(String.format("%-30s", "  exposure steps:"));
            info = "---";
            try {
                info = parameters.get("CONTROL_AE_COMPENSATION_STEP");
            } catch (Exception e) {}
            sb.append(String.format("%30s", info));
            sb.append("\n");

            sb.append(String.format("%-30s", "  max exposure [ms]:"));
            info = "---";
            try {
                info = Long.toString(TimeUnit.MILLISECONDS.convert(Long.parseLong(parameters.get("SENSOR_INFO_MAX_FRAME_DURATION")), TimeUnit.NANOSECONDS));
            } catch (Exception e) {}
            sb.append(String.format("%30s", info));
            sb.append("\n");

//            sb.append(String.format("%-30s", "  real pixel grid:"));
//            sb.append(String.format("%30s", parameters.get("SENSOR_INFO_ACTIVE_ARRAY_SIZE")));
//            sb.append("\n");


            return sb.toString();
        }
    }

}
