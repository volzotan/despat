package de.volzo.despat.userinterface;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import de.volzo.despat.R;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.preferences.SettingsExtendedFragment;
import de.volzo.despat.preferences.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: lock access to settings if a RecordingSession is active
//        Config.reset(this);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {

        final Bundle args = preference.getExtras();
//        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
//                getClassLoader(),
//                preference.getFragment(),
//                args);
//        fragment.setArguments(args);

        // getSupportFragmentManager
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsExtendedFragment())
                .addToBackStack(null)
                .commit();

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.commit();
    }
}
