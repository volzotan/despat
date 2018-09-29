package de.volzo.despat.userinterface;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import de.volzo.despat.CameraController;
import de.volzo.despat.CameraController2;
import de.volzo.despat.R;
import de.volzo.despat.SessionManager;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.preferences.DetectorConfig;

public class ConfigureActivity extends AppCompatActivity {

    private static final String TAG = ConfigureActivity.class.getSimpleName();

    public static final String TRACKING_SESSION = "TRACKING_SESSION";
    public static final String COUNTING_SESSION = "COUNTING_SESSION";

    public static final String DATA_CAMERA_CONFIG = "DATA_CAMERA_CONFIG";
    public static final String DATA_DETECTOR_CONFIG = "DATA_DETECTOR_CONFIG";

    ConfigureActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        this.activity = this;

//        Button btSetTime = (Button) findViewById(R.id.bt_setTime);
//        btSetTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Launch Time Picker Dialog
//                TimePickerDialog timePickerDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
//
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        Log.i(TAG, Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
//                    }
//                }, 10, 10, false);
//                timePickerDialog.show();
//            }
//        });

        Button btStart = (Button) findViewById(R.id.bt_start);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();

                Context context = activity;

                CameraConfig cameraConfig = new CameraConfig(activity);
                DetectorConfig detectorConfig = new DetectorConfig(Config.getNetworkFidelity(context), 600); // TODO

//                // TODO:
//                try {
//                    Size imageSize = CameraController2.getImageSize(activity);
//                    cameraConfig.setZoomRegion(new Rect((imageSize.getWidth() / 2) - (imageSize.getWidth() / 4) / 2, (imageSize.getHeight() / 2) - (imageSize.getHeight() / 4) / 2, (imageSize.getWidth() / 2) + (imageSize.getWidth() / 4) / 2, (imageSize.getHeight() / 2) + (imageSize.getHeight() / 4) / 2));
//                } catch (Exception e) {}
//
//                cameraConfig.setShutterInterval(1000);

//                resultIntent.putExtra(DATA_CAMERA_CONFIG, cameraConfig);
//                resultIntent.putExtra(DATA_DETECTOR_CONFIG, detectorConfig);

                SessionManager session = SessionManager.getInstance(activity);
                session.startRecordingSession(null, cameraConfig, detectorConfig);

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
