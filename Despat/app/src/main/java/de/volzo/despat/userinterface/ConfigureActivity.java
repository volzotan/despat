package de.volzo.despat.userinterface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.CameraController2;
import de.volzo.despat.R;
import de.volzo.despat.SessionManager;
import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.support.DeviceInfo;
import de.volzo.despat.support.Util;

public class ConfigureActivity extends AppCompatActivity {

    private static final String TAG = ConfigureActivity.class.getSimpleName();

    public static final String TRACKING_SESSION = "TRACKING_SESSION";
    public static final String COUNTING_SESSION = "COUNTING_SESSION";

    public static final String DATA_CAMERA_CONFIG = "DATA_CAMERA_CONFIG";
    public static final String DATA_DETECTOR_CONFIG = "DATA_DETECTOR_CONFIG";

    private final int sbIntervalMin = Config.MIN_SHUTTER_INTERVAL;

    ConfigureActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        this.activity = this;

        EditText etSessionName = (EditText) findViewById(R.id.et_sessionName);
        etSessionName.setText(Util.getMostlyUniqueRandomString(activity));

        final TextView tvInterval = (TextView) findViewById(R.id.tv_shutterInterval_value);
        final SeekBar sbInterval = (SeekBar) findViewById(R.id.sb_interval);

//        final HashMap<String, TextView> marker = new HashMap<>();
//        final HashMap<String, Long> computationTime = new HashMap<>();
//        RelativeLayout rlIntervalSeekbar = findViewById(R.id.layout_intervalSeekbar);
//        for (String fidelity : new String[] {"low", "mid", "high"}) {
//            TextView tv = new TextView(this);
//            tv.setText(fidelity);
//            tv.setTag(fidelity);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.BELOW, sbInterval.getId());
//            rlIntervalSeekbar.addView(tv, params);
//            marker.put(fidelity, tv);
//
//            try {
//                List<DeviceInfo.CameraInfo> cameras = CameraController2.getCameraInfo(this);
//                Size imageSize = new Size(cameras.get(0).getHeight(), cameras.get(0).getWidth());
//                Detector detector = new DetectorTensorFlowMobile(this, new DetectorConfig(fidelity, 900));
//                Long time = ((DetectorTensorFlowMobile) detector).estimateComputationTime(imageSize);
//                computationTime.put(fidelity, time);
//            } catch (Exception e) {
//                Log.e(TAG, "setting indicators failed", e);
//            }
//        }

        sbInterval.setMax(Config.MAX_SHUTTER_INTERVAL-sbIntervalMin);
        sbInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvInterval.setText(String.format("%ds", progress+sbIntervalMin));

//                for (Map.Entry<String, TextView> m : marker.entrySet()) {
//                    Long time = computationTime.get(m.getKey()) / 1000;
//
//                    int paddingLeft = sbInterval.getPaddingLeft();
//                    int paddingRight = sbInterval.getPaddingRight();
//                    int width = sbInterval.getWidth();
//
//                    float pixelProSec = (width - paddingLeft - paddingRight) / sbInterval.getMax();
//                    TextView tv = m.getValue();
//                    float xpos = paddingLeft + pixelProSec * (time-sbIntervalMin);
//
//                    if (xpos < width) {
//                        tv.setX(xpos);
//                        tv.setVisibility(View.VISIBLE);
//                    } else {
//                        tv.setVisibility(View.INVISIBLE);
//                        Log.d(TAG, "Seekbar marker " + tv.getText() + " invisible");
//                    }
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbInterval.setProgress((int) (Config.getShutterInterval(activity)/1000 - sbIntervalMin));

//        final SeekBar sbNetworkFidelity = (SeekBar) findViewById(R.id.sb_networkFidelity);
//        sbNetworkFidelity.setMax(2);
//        switch (Config.getNetworkFidelity(activity)) {
//            case "low": {
//                sbNetworkFidelity.setProgress(0);
//                break;
//            }
//            case "mid": {
//                sbNetworkFidelity.setProgress(1);
//                break;
//            }
//            case "high": {
//                sbNetworkFidelity.setProgress(2);
//                break;
//            }
//            default:
//                Log.e(TAG, "undefined network fidelity state: " + Config.getNetworkFidelity(activity));
//        }

        LinearLayout toggleLayout = findViewById(R.id.toggleContainerNetworkFidelity);
        final List<ToggleButton> detector_buttons = new ArrayList<>();

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = (String) v.getTag();
                setDetectorButtonStates(state, DetectorTensorFlowMobile.FIDELITY_MODE, detector_buttons);
            }
        };

        for (String value : DetectorTensorFlowMobile.FIDELITY_MODE) {
            ToggleButton button = new ToggleButton(this);
            button.setTag(value);
            button.setText(value);
            button.setTextOff(value);
            button.setTextOn(value);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1.0f / DetectorTensorFlowMobile.FIDELITY_MODE.length;
            button.setLayoutParams(layoutParams);
            toggleLayout.addView(button);
            detector_buttons.add(button);
            button.setOnClickListener(buttonClickListener);
        }

        setDetectorButtonStates(Config.getNetworkFidelity(activity), DetectorTensorFlowMobile.FIDELITY_MODE, detector_buttons);

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
                cameraConfig.setShutterInterval((sbInterval.getProgress()+sbIntervalMin) * 1000);

                String fidelity = Config.getNetworkFidelity(context);
                for (int i=0; i<detector_buttons.size(); i++) {
                    if (detector_buttons.get(i).isChecked()) {
                        fidelity = DetectorTensorFlowMobile.FIDELITY_MODE[i];
                    }
                }
                DetectorConfig detectorConfig = new DetectorConfig(fidelity, 700); // TODO

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

    private void setDetectorButtonStates(String state, String[] values, List<ToggleButton> buttons) {

        try {
            List<DeviceInfo.CameraInfo> cameras = CameraController2.getCameraInfo(this);
            Size imageSize = new Size(cameras.get(0).getHeight(), cameras.get(0).getWidth());
            Detector detector = new DetectorTensorFlowMobile(this, new DetectorConfig(state, 900));
            Long time = ((DetectorTensorFlowMobile) detector).estimateComputationTime(imageSize);

            TextView tv = findViewById(R.id.tv_estimatedComputationTime);
            if (time != null) {
                tv.setText(String.format("%d seconds", time / 1000));
            } else {
                Log.w(TAG, "Benchmark times for fidelity computation time estimation are missing");
                tv.setText("(could not be computed)");
            }
        } catch (Exception e) {
            Log.e(TAG, "estimating computation time failed", e);
        }

        for (int i=0; i<buttons.size(); i++) {
            buttons.get(i).setTextColor(ContextCompat.getColor(activity, R.color.white));
            buttons.get(i).setChecked(false);
        }

        for (int i=0; i<buttons.size(); i++) {
            if (state.equals(values[i])) {
                buttons.get(i).setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                buttons.get(i).setChecked(true);

                return;
            }
        }

        Log.e(TAG, "undefined network fidelity state: " + Config.getNetworkFidelity(activity));
    }

}
