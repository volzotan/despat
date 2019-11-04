package de.volzo.despat.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;

import java.util.HashMap;

import de.volzo.despat.R;
import de.volzo.despat.detector.DetectorTensorFlowMobile;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    Context context;

    HashMap<Preference, Integer> preferenceSummaryMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getContext();

        // Load the preferences from an XML resource
        // addPreferencesFromResource(R.xml.preferences);

//        final PreferenceScreen extendedPref = (PreferenceScreen) findPreference("childPrefId");
//        extendedPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent = new Intent(PreferenceActivity.this, YourSettings.class);
//                intent.setAction("ShowChildPref");
//                startActivity(intent);
//                return true;
//            }
//        });

        Intent intent = getActivity().getIntent();
        if (intent.getAction() != null && intent.getAction().equals("extended")) {
//            setPreferenceScreen(childPref);
        } else {
            setPreferenceScreen(prepareMainScreen());
        }
    }

    private PreferenceScreen prepareMainScreen() {

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        PreferenceCategory category;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Preference textPref = new Preference(context);
        textPref.setTitle("");
        textPref.setSummary(R.string.pref_summary_intro);
        textPref.setKey("PREFERENCE_TEXT_INTRO");
        textPref.setSelectable(false);
        textPref.setPersistent(false);
        screen.addPreference(textPref);
//        preferenceSummaryMap.put(textPref, R.string.pref_summary_intro);

        // GENERAL ---------------------------------------------------------------------------------

        category = new PreferenceCategory(context);
        category.setTitle("General");
        screen.addPreference(category);

        EditTextPreference prefDeviceName = new EditTextPreference(context);
        prefDeviceName.setTitle(R.string.pref_title_deviceName);
        prefDeviceName.setSummary(R.string.pref_summary_deviceName);
        prefDeviceName.setDefaultValue(Config.DEFAULT_DEVICE_NAME);
        prefDeviceName.setKey(Config.KEY_DEVICE_NAME);
        category.addPreference(prefDeviceName);
        preferenceSummaryMap.put(prefDeviceName, R.string.pref_summary_deviceName);

        SwitchPreference prefResumeAfterReboot = new SwitchPreference(context);
        prefResumeAfterReboot.setTitle(context.getString(R.string.pref_title_resumeAfterReboot));
        prefResumeAfterReboot.setSummary(context.getString(R.string.pref_summary_resumeAfterReboot));
        prefResumeAfterReboot.setDefaultValue(Config.DEFAULT_RESUME_AFTER_REBOOT);
        prefResumeAfterReboot.setKey(Config.KEY_RESUME_AFTER_REBOOT);
        category.addPreference(prefResumeAfterReboot);

        SwitchPreference prefShowTooltips = new SwitchPreference(context);
        prefShowTooltips.setTitle(context.getString(R.string.pref_title_showTooltips));
        prefShowTooltips.setSummary(context.getString(R.string.pref_summary_showTooltips));
        prefShowTooltips.setDefaultValue(Config.DEFAULT_SHOW_TOOLTIPS);
        prefShowTooltips.setKey(Config.KEY_SHOW_TOOLTIPS);
        category.addPreference(prefShowTooltips);

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
        prefShutterInterval.setTitle(R.string.pref_title_shutterInterval);
        prefShutterInterval.setSummary(R.string.pref_summary_shutterInterval);
        prefShutterInterval.setMinValue(Config.MIN_SHUTTER_INTERVAL);
        prefShutterInterval.setMaxValue(Config.MAX_SHUTTER_INTERVAL);
        prefShutterInterval.setFactor(1000);
        prefShutterInterval.setDefaultValue(Config.DEFAULT_SHUTTER_INTERVAL);
        prefShutterInterval.setKey(Config.KEY_SHUTTER_INTERVAL);
        category.addPreference(prefShutterInterval);
        preferenceSummaryMap.put(prefShutterInterval, R.string.pref_summary_shutterInterval);

        // NumberPicker cant deal with negative values right now
//        NumberPickerPreference prefExposureCompensation = new NumberPickerPreference(context);
//        prefExposureCompensation.setTitle(R.string.pref_title_exposureCompensation);
//        prefExposureCompensation.setSummary(R.string.pref_summary_exposureCompensation);
//        prefExposureCompensation.setMinValue(-10);
//        prefExposureCompensation.setMaxValue(+10);
//        prefExposureCompensation.setDefaultValue(Config.DEFAULT_EXPOSURE_COMPENSATION);
//        prefExposureCompensation.setKey(Config.KEY_EXPOSURE_COMPENSATION);
//        category.addPreference(prefExposureCompensation);
//        preferenceSummaryMap.put(prefExposureCompensation, R.string.pref_summary_exposureCompensation);

        // NETWORK ---------------------------------------------------------------------------------

        category = new PreferenceCategory(context);
        category.setTitle("Detection Settings");
        screen.addPreference(category);

        ListPreference prefNetworkFidelity = new ListPreference(context);
        prefNetworkFidelity.setTitle(R.string.pref_title_networkFidelity);
        prefNetworkFidelity.setSummary(R.string.pref_summary_networkFidelity);
        prefNetworkFidelity.setEntryValues(DetectorTensorFlowMobile.FIDELITY_MODE);
        prefNetworkFidelity.setEntries(DetectorTensorFlowMobile.FIDELITY_MODE);
        prefNetworkFidelity.setDefaultValue(Config.DEFAULT_NETWORK_FIDELITY);
        prefNetworkFidelity.setKey(Config.KEY_NETWORK_FIDELITY);
        category.addPreference(prefNetworkFidelity);
        preferenceSummaryMap.put(prefNetworkFidelity, R.string.pref_summary_networkFidelity);

        // SERVER ----------------------------------------------------------------------------------

//        category = new PreferenceCategory(context);
//        category.setTitle("Server");
//        screen.addPreference(category);
//
//        SwitchPreference prefPhoneHome = new SwitchPreference(context);
//        prefPhoneHome.setTitle(context.getString(R.string.pref_title_phoneHome));
//        prefPhoneHome.setSummary(context.getString(R.string.pref_summary_phoneHome));
//        prefPhoneHome.setDefaultValue(Config.DEFAULT_PHONE_HOME);
//        prefPhoneHome.setKey(Config.KEY_PHONE_HOME);
//        category.addPreference(prefPhoneHome);
//
//        EditTextPreference prefServerAddress = new EditTextPreference(context);
//        prefServerAddress.setTitle(context.getString(R.string.pref_title_serverAddress));
//        prefServerAddress.setSummary(context.getString(R.string.pref_summary_serverAddress));
//        prefServerAddress.setDefaultValue(Config.DEFAULT_SERVER_ADDRESS);
//        prefServerAddress.setKey(Config.KEY_SERVER_ADDRESS);
//        category.addPreference(prefServerAddress);
//        preferenceSummaryMap.put(prefServerAddress, R.string.pref_summary_serverAddress);

        // EXTENDED --------------------------------------------------------------------------------

        category = new PreferenceCategory(context);
        category.setTitle("Extended");
        screen.addPreference(category);

        Preference extendedPreference = new Preference(context);
        extendedPreference.setTitle(context.getString(R.string.pref_title_extended));
        extendedPreference.setSummary(context.getString(R.string.pref_summary_extended));
        extendedPreference.setFragment(SettingsExtendedFragment.class.getName());
        category.addPreference(extendedPreference);

//        extendedPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                return false;
//            }
//        });

        return screen;
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
        Integer summaryKey = preferenceSummaryMap.get(preference);

        if (preference instanceof ListPreference) {
//            ListPreference listPreference = (ListPreference) preference;
//            listPreference.setSummary(listPreference.getEntry());

            if (summaryKey != null) {
                String newValue = sharedPrefs.getString(key, "undefined");
                preference.setSummary(String.format(Config.LOCALE, context.getString(summaryKey), newValue));
            }
            return;
        }

        if (preference instanceof NumberPickerPreference) {
            if (summaryKey != null) {
                int newValue = sharedPrefs.getInt(key, 0);
                preference.setSummary(String.format(Config.LOCALE, context.getString(summaryKey), newValue));
            }
            return;
        }

        if (preference instanceof TwoStatePreference) {
            return;
        }

        if (summaryKey != null) {
            String newValue = sharedPrefs.getString(key, "Default");
            String newSummary = String.format(Config.LOCALE, context.getString(summaryKey), newValue);
            preference.setSummary(newSummary);
            return;
        }

        String oldSummary = (String) preference.getSummary();
        if (oldSummary == null) {
            preference.setSummary(sharedPrefs.getString(key, "Default"));
        }
    }
}