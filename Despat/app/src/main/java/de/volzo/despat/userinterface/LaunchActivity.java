package de.volzo.despat.userinterface;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import de.volzo.despat.MainActivity;
import de.volzo.despat.R;
import de.volzo.despat.preferences.Config;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;

        // Intro Tour?
        if (Config.getFirstTimeLaunch(this)) {
            intent = new Intent(this, TourActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }

}
