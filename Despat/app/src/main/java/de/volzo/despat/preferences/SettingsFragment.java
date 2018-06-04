package de.volzo.despat.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getContext();

        // Load the preferences from an XML resource
        // addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        PreferenceCategory category;

        // GENERAL ---------------------------------------------------------------------------------

        category = new PreferenceCategory(context);
        category.setTitle("General");
        screen.addPreference(category);

        EditTextPreference prefDeviceName = new EditTextPreference(context);
        prefDeviceName.setTitle(context.getString(R.string.pref_title_deviceName));
        prefDeviceName.setSummary(context.getString(R.string.pref_summary_deviceName));
        prefDeviceName.setDefaultValue(Config.DEFAULT_DEVICE_NAME);
        prefDeviceName.setKey(Config.KEY_DEVICE_NAME);
        category.addPreference(prefDeviceName);

        SwitchPreference prefResumeAfterReboot = new SwitchPreference(context);
        prefResumeAfterReboot.setTitle(context.getString(R.string.pref_title_resumeAfterReboot));
        prefResumeAfterReboot.setSummary(context.getString(R.string.pref_summary_resumeAfterReboot));
        prefResumeAfterReboot.setDefaultValue(Config.DEFAULT_PERSISTENT_CAMERA);
        prefResumeAfterReboot.setKey(Config.KEY_PERSISTENT_CAMERA);
        category.addPreference(prefResumeAfterReboot);

//        TODO: FilePicker needed
//        EditTextPreference prefWorkingDirectory = new EditTextPreference(context);
//        prefWorkingDirectory.setTitle(context.getString(R.string.pref_title_workingDirectory));
//        prefWorkingDirectory.setSummary(context.getString(R.string.pref_summary_workingDirectory));
//        prefWorkingDirectory.setDefaultValue(Config.DEFAULT_WORKING_DIRECTORY);
//        prefWorkingDirectory.setKey(Config.KEY_WORKING_DIRECTORY);
//        category.addPreference(prefWorkingDirectory);

        // CAMERA ----------------------------------------------------------------------------------

        category = new PreferenceCategory(context);
        category.setTitle("Camera");
        screen.addPreference(category);

        SwitchPreference prefPersistentCamera = new SwitchPreference(context);
        prefPersistentCamera.setTitle(context.getString(R.string.pref_title_persistentCamera));
        prefPersistentCamera.setSummary(context.getString(R.string.pref_summary_persistentCamera));
        prefPersistentCamera.setDefaultValue(Config.DEFAULT_PERSISTENT_CAMERA);
        prefPersistentCamera.setKey(Config.KEY_PERSISTENT_CAMERA);
        category.addPreference(prefPersistentCamera);

        SwitchPreference prefLegacyCameraController = new SwitchPreference(context);
        prefLegacyCameraController.setTitle(context.getString(R.string.pref_title_legacyCameraController));
        prefLegacyCameraController.setSummary(context.getString(R.string.pref_summary_legacyCameraController));
        prefLegacyCameraController.setDefaultValue(Config.DEFAULT_LEGACY_CAMERA_CONTROLLER);
        prefLegacyCameraController.setKey(Config.KEY_LEGACY_CAMERA_CONTROLLER);
        category.addPreference(prefLegacyCameraController);

        // NumberPickerPreference requires an initialized value
        if (Config.getShutterInterval(context) == Config.DEFAULT_SHUTTER_INTERVAL) {
            Config.setShutterInterval(context, Config.DEFAULT_SHUTTER_INTERVAL);
        }

        NumberPickerPreference prefShutterInterval = new NumberPickerPreference(context);
        prefShutterInterval.setTitle(context.getString(R.string.pref_title_shutterInterval));
        prefShutterInterval.setSummary(context.getString(R.string.pref_summary_shutterInterval));
        prefShutterInterval.setMinValue(2);
        prefShutterInterval.setMaxValue(120);
        prefShutterInterval.setFactor(1000);
        prefShutterInterval.setDefaultValue(Config.DEFAULT_SHUTTER_INTERVAL);
        prefShutterInterval.setKey(Config.KEY_SHUTTER_INTERVAL);
        category.addPreference(prefShutterInterval);

        // SERVER ----------------------------------------------------------------------------------

        category = new PreferenceCategory(context);
        category.setTitle("Server");
        screen.addPreference(category);

        SwitchPreference prefPhoneHome = new SwitchPreference(context);
        prefPhoneHome.setTitle(context.getString(R.string.pref_title_phoneHome));
        prefPhoneHome.setSummary(context.getString(R.string.pref_summary_phoneHome));
        prefPhoneHome.setDefaultValue(Config.DEFAULT_PHONE_HOME);
        prefPhoneHome.setKey(Config.KEY_PHONE_HOME);
        category.addPreference(prefPhoneHome);

        EditTextPreference prefServerAddress = new EditTextPreference(context);
        prefServerAddress.setTitle(context.getString(R.string.pref_title_serverAddress));
        prefServerAddress.setSummary(context.getString(R.string.pref_summary_serverAddress));
        prefServerAddress.setDefaultValue(Config.DEFAULT_SERVER_ADDRESS);
        prefServerAddress.setKey(Config.KEY_SERVER_ADDRESS);
        category.addPreference(prefServerAddress);

        setPreferenceScreen(screen);
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    Preference singlePref = preferenceGroup.getPreference(j);
                    updatePreference(singlePref, singlePref.getKey());
                }
            } else {
                updatePreference(preference, preference.getKey());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key), key);
    }

    private void updatePreference(Preference preference, String key) {
        if (preference == null) return;

        SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
        String s = (String) preference.getSummary();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
            return;
        }

        if (preference instanceof NumberPickerPreference) {
            s = String.format(s, sharedPrefs.getInt(key, 0));
            preference.setSummary(s);
            return;
        }

        if (preference instanceof TwoStatePreference) {
            return;
        }

        s = String.format(s, sharedPrefs.getString(key, "Default"));
        preference.setSummary(s);
    }
}