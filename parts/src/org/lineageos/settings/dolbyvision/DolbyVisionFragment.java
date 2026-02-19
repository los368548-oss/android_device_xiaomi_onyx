/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.dolbyvision;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.MainSwitchPreference;

import org.lineageos.settings.Constants;
import org.lineageos.settings.CustomSeekBarPreference;
import org.lineageos.settings.R;

public class DolbyVisionFragment extends PreferenceFragmentCompat
        implements CompoundButton.OnCheckedChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "DolbyVisionFragment";

    private MainSwitchPreference mDolbyVisionEnabled;
    private SharedPreferences mPrefs;
    private Handler mHandler;
    private Context mContext;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dolby_vision, rootKey);
        mContext = getContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mHandler = new Handler(Looper.getMainLooper());

        mDolbyVisionEnabled = findPreference(Constants.KEY_DOLBY_VISION_ENABLED);
        mDolbyVisionEnabled.setChecked(mPrefs.getBoolean(Constants.KEY_DOLBY_VISION_ENABLED, true));
        mDolbyVisionEnabled.addOnSwitchChangeListener(this);
        mDolbyVisionEnabled.setSummary(R.string.made_by);

        setupProfilePreferences();
        setupToneMappingPreferences();
        setupColorPreferences();
        setupMotionPreferences();
        setupAdvancedPreferences();
        setupResetPreference();

        updatePreferencesState(mDolbyVisionEnabled.isChecked());
        applyCurrentSettings();
    }

    private void setupProfilePreferences() {
        ListPreference profilePref = findPreference(Constants.KEY_DV_PROFILE);
        if (profilePref != null) {
            profilePref.setOnPreferenceChangeListener(this);
            updateListSummary(profilePref);
        }
    }

    private void setupToneMappingPreferences() {
        setupSwitch(Constants.KEY_DV_CONTENT_DETECTION, true);
        setupSwitch(Constants.KEY_DV_TONE_MAPPING, true);
        setupSwitch(Constants.KEY_DV_DYNAMIC_METADATA, true);
        setupSwitch(Constants.KEY_DV_AMBIENT_ADAPTATION, true);
        setupSeekBar(Constants.KEY_DV_HIGHLIGHT_RECOVERY, 50);
        setupSeekBar(Constants.KEY_DV_SHADOW_ENHANCEMENT, 50);
    }

    private void setupColorPreferences() {
        setupSwitch(Constants.KEY_DV_COLOR_ENHANCEMENT, true);
        setupSeekBar(Constants.KEY_DV_COLOR_SATURATION, 50);
        setupListPreference(Constants.KEY_DV_COLOR_GAMUT);
        setupSwitch(Constants.KEY_DV_SKIN_TONE, true);
        setupSwitch(Constants.KEY_DV_MEMORY_COLOR, true);
    }

    private void setupMotionPreferences() {
        setupSwitch(Constants.KEY_DV_MOTION_INTERPOLATION, false);
        setupSeekBar(Constants.KEY_DV_MOTION_BLUR_REDUCTION, 30);
        setupSeekBar(Constants.KEY_DV_JUDDER_REDUCTION, 30);
        setupSwitch(Constants.KEY_DV_BLACK_FRAME_INSERTION, false);
    }

    private void setupAdvancedPreferences() {
        setupSwitch(Constants.KEY_DV_LOW_LATENCY, false);
        setupSwitch(Constants.KEY_DV_VRR, true);
        setupSwitch(Constants.KEY_DV_HDR10_PLUS, true);
        setupSwitch(Constants.KEY_DV_HLG, true);
        setupListPreference(Constants.KEY_DV_PEAK_BRIGHTNESS);
    }

    private void setupResetPreference() {
        Preference resetPref = findPreference("dv_reset");
        if (resetPref != null) {
            resetPref.setOnPreferenceClickListener(preference -> {
                resetToDefaults();
                return true;
            });
        }

        Preference aboutPref = findPreference("dv_about");
        if (aboutPref != null) {
            aboutPref.setSummary(getString(R.string.made_by) + " | " + getString(R.string.version_info));
        }
    }

    private void setupSwitch(String key, boolean defaultValue) {
        SwitchPreferenceCompat pref = findPreference(key);
        if (pref != null) {
            pref.setChecked(mPrefs.getBoolean(key, defaultValue));
            pref.setOnPreferenceChangeListener(this);
        }
    }

    private void setupSeekBar(String key, int defaultValue) {
        CustomSeekBarPreference pref = findPreference(key);
        if (pref != null) {
            pref.setValue(mPrefs.getInt(key, defaultValue));
            pref.setOnPreferenceChangeListener(this);
        }
    }

    private void setupListPreference(String key) {
        ListPreference pref = findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
            updateListSummary(pref);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPrefs.edit().putBoolean(Constants.KEY_DOLBY_VISION_ENABLED, isChecked).apply();
        updatePreferencesState(isChecked);
        DolbyVisionUtil.applyDolbyVisionSettings(mContext);
        
        int messageId = isChecked ? R.string.toast_dv_on : R.string.toast_dv_off;
        showToast(messageId);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (preference instanceof SwitchPreferenceCompat) {
            mPrefs.edit().putBoolean(key, (Boolean) newValue).apply();
        } else if (preference instanceof ListPreference) {
            mPrefs.edit().putString(key, (String) newValue).apply();
            updateListSummary((ListPreference) preference);
        } else if (preference instanceof CustomSeekBarPreference) {
            mPrefs.edit().putInt(key, Integer.parseInt(newValue.toString())).apply();
        }

        DolbyVisionUtil.applyDolbyVisionSettings(mContext);
        
        mHandler.postDelayed(() -> {
            DolbyVisionUtil.applyDolbyVisionSettings(mContext);
        }, 100);
        
        return true;
    }

    private void updatePreferencesState(boolean enabled) {
        String[] categoryKeys = {
            "dv_profile_category",
            "dv_tone_mapping_category",
            "dv_color_category",
            "dv_motion_category",
            "dv_advanced_category"
        };

        for (String key : categoryKeys) {
            PreferenceCategory category = findPreference(key);
            if (category != null) {
                category.setEnabled(enabled);
            }
        }
    }

    private void updateListSummary(ListPreference pref) {
        String key = pref.getKey();
        String value = mPrefs.getString(key, (String) pref.getEntryValues()[0]);
        int index = pref.findIndexOfValue(value);
        if (index >= 0) {
            pref.setSummary(pref.getEntries()[index]);
        }
    }

    private void resetToDefaults() {
        SharedPreferences.Editor editor = mPrefs.edit();
        
        editor.putBoolean(Constants.KEY_DOLBY_VISION_ENABLED, true);
        editor.putString(Constants.KEY_DV_PROFILE, "cinema");
        editor.putBoolean(Constants.KEY_DV_CONTENT_DETECTION, true);
        editor.putBoolean(Constants.KEY_DV_TONE_MAPPING, true);
        editor.putBoolean(Constants.KEY_DV_DYNAMIC_METADATA, true);
        editor.putBoolean(Constants.KEY_DV_AMBIENT_ADAPTATION, true);
        editor.putInt(Constants.KEY_DV_HIGHLIGHT_RECOVERY, 50);
        editor.putInt(Constants.KEY_DV_SHADOW_ENHANCEMENT, 50);
        editor.putBoolean(Constants.KEY_DV_COLOR_ENHANCEMENT, true);
        editor.putInt(Constants.KEY_DV_COLOR_SATURATION, 50);
        editor.putString(Constants.KEY_DV_COLOR_GAMUT, "auto");
        editor.putBoolean(Constants.KEY_DV_SKIN_TONE, true);
        editor.putBoolean(Constants.KEY_DV_MEMORY_COLOR, true);
        editor.putBoolean(Constants.KEY_DV_MOTION_INTERPOLATION, false);
        editor.putInt(Constants.KEY_DV_MOTION_BLUR_REDUCTION, 30);
        editor.putInt(Constants.KEY_DV_JUDDER_REDUCTION, 30);
        editor.putBoolean(Constants.KEY_DV_BLACK_FRAME_INSERTION, false);
        editor.putBoolean(Constants.KEY_DV_LOW_LATENCY, false);
        editor.putBoolean(Constants.KEY_DV_VRR, true);
        editor.putBoolean(Constants.KEY_DV_HDR10_PLUS, true);
        editor.putBoolean(Constants.KEY_DV_HLG, true);
        editor.putString(Constants.KEY_DV_PEAK_BRIGHTNESS, "2000");
        editor.apply();

        DolbyVisionUtil.applyDolbyVisionSettings(mContext);
        showToast(R.string.dv_reset_done);
        
        mHandler.postDelayed(this::recreate, 200);
    }

    private void applyCurrentSettings() {
        DolbyVisionUtil.applyDolbyVisionSettings(mContext);
    }

    private void showToast(int messageId) {
        Toast.makeText(mContext, messageId, Toast.LENGTH_SHORT).show();
    }

    private void recreate() {
        setPreferencesFromResource(R.xml.dolby_vision, null);
        onCreatePreferences(null, null);
    }
}