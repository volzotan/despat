package de.volzo.despat.userinterface;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import de.volzo.despat.R;

public class ConfigureActivity extends AppCompatActivity {

    private static final String TAG = ConfigureActivity.class.getSimpleName();
    ConfigureActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        this.activity = this;

        Button btSetTime = (Button) findViewById(R.id.bt_setTime);
        btSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Log.i(TAG, Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
                    }
                }, 10, 10, false);
                timePickerDialog.show();
            }
        });

    }

}
