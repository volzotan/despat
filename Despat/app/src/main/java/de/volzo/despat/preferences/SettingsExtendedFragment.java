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
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.volzo.despat.CameraController2;
import de.volzo.despat.R;
import de.volzo.despat.support.DeviceInfo;

public class SettingsExtendedFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsExtendedFragment.class.getSimpleName();

    Context context;

    HashMap<Preference, Integer> preferenceSummaryMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getContext();

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        PreferenceCategory category;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Preference textPref = new Preference(context);
        textPref.setTitle("");
        textPref.setSummary(R.string.pref_summary_introExtended);
        textPref.setKey("PREFERENCE_TEXT_EXTENDED");
        textPref.setSelectable(false);
        textPref.setPersistent(false);
        screen.addPreference(textPref);
//        preferenceSummaryMap.put(textPref, R.string.pref_summary_introExtended);

        category = new PreferenceCategory(context);
        category.setTitle("General");
        screen.addPreference(category);

        SwitchPreference prefEnableRecognitionService = new SwitchPreference(context);
        prefEnableRecognitionService.setTitle(context.getString(R.string.pref_title_enableRecognitionService));
        prefEnableRecognitionService.setSummary(context.getString(R.string.pref_summary_enableRecognitionService));
        prefEnableRecognitionService.setDefaultValue(Config.DEFAULT_ENABLE_RECOGNITION_SERVICE);
        prefEnableRecognitionService.setKey(Config.KEY_ENABLE_RECOGNITION_SERVICE);
        category.addPreference(prefEnableRecognitionService);

        SwitchPreference prefEnableCompressorService = new SwitchPreference(context);
        prefEnableCompressorService.setTitle(context.getString(R.string.pref_title_enableCompressorService));
        prefEnableCompressorService.setSummary(context.getString(R.string.pref_summary_enableCompressorService));
        prefEnableCompressorService.setDefaultValue(Config.DEFAULT_ENABLE_COMPRESSOR_SERVICE);
        prefEnableCompressorService.setKey(Config.KEY_ENABLE_COMPRESSOR_SERVICE);
        category.addPreference(prefEnableCompressorService);

        SwitchPreference prefDeleteAfterRecognition = new SwitchPreference(context);
        prefDeleteAfterRecognition.setTitle(context.getString(R.string.pref_title_deleteAfterRecognition));
        prefDeleteAfterRecognition.setSummary(context.getString(R.string.pref_summary_deleteAfterRecognition));
        prefDeleteAfterRecognition.setDefaultValue(Config.DEFAULT_DELETE_AFTER_RECOGNITION);
        prefDeleteAfterRecognition.setKey(Config.KEY_DELETE_AFTER_RECOGNITION);
        category.addPreference(prefDeleteAfterRecognition);

        category = new PreferenceCategory(context);
        category.setTitle("Camera");
        screen.addPreference(category);

        String[] cameraIds = {};
        String[] cameraIdEntries = {};
        try {
            List<DeviceInfo.CameraInfo> cameras = CameraController2.getCameraInfo(context);

            cameraIds = new String[cameras.size()];
            cameraIdEntries = new String[cameras.size()];
            for (int i=0; i<cameras.size(); i++) {
                DeviceInfo.CameraInfo info = cameras.get(i);
                cameraIds[i] = info.getId();
                cameraIdEntries[i] = String.format("[%s] %s", info.getId(), info.getDirection());
            }
        } catch (Exception e) {
            Log.e(TAG, "getting available cameras failed", e);
        }

        ListPreference prefCameraDevice = new ListPreference(context);
        prefCameraDevice.setTitle(R.string.pref_title_cameraDevice);
        prefCameraDevice.setSummary(R.string.pref_summary_cameraDevice);
        prefCameraDevice.setEntryValues(cameraIds);
        prefCameraDevice.setEntries(cameraIdEntries);
        prefCameraDevice.setDefaultValue(Config.DEFAULT_CAMERA_DEVICE);
        prefCameraDevice.setKey(Config.KEY_CAMERA_DEVICE);
        category.addPreference(prefCameraDevice);
        preferenceSummaryMap.put(prefCameraDevice, R.string.pref_summary_cameraDevice);

        SwitchPreference prefFormatJpg = new SwitchPreference(context);
        prefFormatJpg.setTitle(context.getString(R.string.pref_title_formatJpg));
        prefFormatJpg.setSummary(context.getString(R.string.pref_summary_formatJpg));
        prefFormatJpg.setDefaultValue(Config.DEFAULT_FORMAT_JPG);
        prefFormatJpg.setKey(Config.KEY_FORMAT_JPG);
        category.addPreference(prefFormatJpg);

        SwitchPreference prefFormatRaw = new SwitchPreference(context);
        prefFormatRaw.setTitle(context.getString(R.string.pref_title_formatRaw));
        prefFormatRaw.setSummary(context.getString(R.string.pref_summary_formatRaw));
        prefFormatRaw.setDefaultValue(Config.DEFAULT_FORMAT_RAW);
        prefFormatRaw.setKey(Config.KEY_FORMAT_RAW);
        category.addPreference(prefFormatRaw);

        try {
            List<DeviceInfo.CameraInfo> cameras = CameraController2.getCameraInfo(context);
            prefFormatRaw.setEnabled(cameras.get(0).getRawSupport());
        } catch (Exception e) {
            Log.e(TAG, "checking for RAW format capabilities failed", e);
        }

        final String[] EXPOSURE_VALUES = {
                "-12", "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

        ListPreference prefExposureCompensation = new ListPreference(context);
        prefExposureCompensation.setTitle(R.string.pref_title_exposureCompensation);
        prefExposureCompensation.setSummary(R.string.pref_summary_exposureCompensation);
        prefExposureCompensation.setEntryValues(EXPOSURE_VALUES);
        prefExposureCompensation.setEntries(EXPOSURE_VALUES);
        prefExposureCompensation.setDefaultValue(Integer.toString(Config.DEFAULT_EXPOSURE_COMPENSATION));
        prefExposureCompensation.setKey(Config.KEY_EXPOSURE_COMPENSATION);
        category.addPreference(prefExposureCompensation);
        preferenceSummaryMap.put(prefExposureCompensation, R.string.pref_summary_exposureCompensation);

        ListPreference prefSecondImageExposureCompensation = new ListPreference(context);
        prefSecondImageExposureCompensation.setTitle(R.string.pref_title_secondImageExposureCompensation);
        prefSecondImageExposureCompensation.setSummary(R.string.pref_summary_secondImageExposureCompensation);
        prefSecondImageExposureCompensation.setEntryValues(EXPOSURE_VALUES);
        prefSecondImageExposureCompensation.setEntries(EXPOSURE_VALUES);
        prefSecondImageExposureCompensation.setDefaultValue(Integer.toString(Config.DEFAULT_EXPOSURE_COMPENSATION));
        prefSecondImageExposureCompensation.setKey(Config.KEY_SECOND_IMAGE_EXPOSURE_COMPENSATION);
        category.addPreference(prefSecondImageExposureCompensation);
        preferenceSummaryMap.put(prefSecondImageExposureCompensation, R.string.pref_summary_secondImageExposureCompensation);

        NumberPickerPreference prefExposureThreshold = new NumberPickerPreference(context);
        prefExposureThreshold.setTitle(R.string.pref_title_exposureThreshold);
        prefExposureThreshold.setSummary(R.string.pref_summary_exposureThreshold);
        prefExposureThreshold.setMinValue(0); //2.0);
        prefExposureThreshold.setMaxValue(50); //50.0);
        prefExposureThreshold.setDefaultValue(Config.DEFAULT_EXPOSURE_THRESHOLD);
        prefExposureThreshold.setKey(Config.KEY_EXPOSURE_THRESHOLD);
        category.addPreference(prefExposureThreshold);
        preferenceSummaryMap.put(prefExposureThreshold, R.string.pref_summary_exposureThreshold);

//        category = new PreferenceCategory(context);
//        category.setTitle("Actions");
//        screen.addPreference(category);
//
//        Preference prefEnableCTmode = new Preference(context);
//        prefEnableCTmode.setTitle(context.getString(R.string.pref_title_enableCTmode));
//        prefEnableCTmode.setSummary(context.getString(R.string.pref_summary_enableCTmode));
//        prefEnableCTmode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Config.enableCTMode(context);
//                return false;
//            }
//        });
//        category.addPreference(prefEnableCTmode);

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