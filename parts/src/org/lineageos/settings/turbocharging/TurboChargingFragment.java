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

package org.lineageos.settings.turbocharging;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.MainSwitchPreference;

import org.lineageos.settings.Constants;
import org.lineageos.settings.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TurboChargingFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "TurboChargingFragment";
    private static final String DEFAULT_OFF_VALUE = "0";

    private MainSwitchPreference mTurboEnabled;
    private SwitchPreferenceCompat mSportsMode;
    private ListPreference mTurboCurrent;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.turbocharging, rootKey);

        mTurboEnabled = (MainSwitchPreference) findPreference(Constants.KEY_TURBO_ENABLED);
        mTurboEnabled.setOnPreferenceChangeListener(this);

        mSportsMode = (SwitchPreferenceCompat) findPreference(Constants.KEY_SPORTS_MODE);
        mSportsMode.setOnPreferenceChangeListener(this);
        mSportsMode.setEnabled(mTurboEnabled.isChecked());

        mTurboCurrent = (ListPreference) findPreference(Constants.KEY_TURBO_CURRENT);
        mTurboCurrent.setOnPreferenceChangeListener(this);
        mTurboCurrent.setEnabled(mTurboEnabled.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTurboEnabled) {
            boolean turboEnabled = (boolean) newValue;
            mTurboCurrent.setEnabled(turboEnabled);
            if (!turboEnabled) {
                mSportsMode.setChecked(false);
                updateSportsMode(false);
            }
            mSportsMode.setEnabled(turboEnabled);
            updateChargeCurrent();
            Toast.makeText(getActivity(),
                    turboEnabled ? getString(R.string.toast_turbo_on) : getString(R.string.toast_turbo_off),
                    Toast.LENGTH_SHORT).show();
            return true;

        } else if (preference == mSportsMode) {
            boolean sportsEnabled = (boolean) newValue;
            updateSportsMode(sportsEnabled);
            Toast.makeText(getActivity(),
                    sportsEnabled ? getString(R.string.toast_sports_on) : getString(R.string.toast_sports_off),
                    Toast.LENGTH_SHORT).show();
            return true;

        } else if (preference == mTurboCurrent) {
            String value = (String) newValue;
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putString(Constants.KEY_TURBO_CURRENT, value)
                    .apply();

            if ("1800000".equals(value)) {
                mSportsMode.setChecked(false);
                updateSportsMode(false);
            }

            updateChargeCurrent();

            CharSequence entry = mTurboCurrent.getEntries()[mTurboCurrent.findIndexOfValue(value)];
            String entryStr = entry.toString();
            if (entryStr.endsWith("W")) {
                entryStr = entryStr.substring(0, entryStr.length() - 1) + " watt";
            }
            Toast.makeText(getActivity(),
                    String.format(getString(R.string.toast_wattage_set), entryStr),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void updateChargeCurrent() {
        boolean turboEnabled = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(Constants.KEY_TURBO_ENABLED, false);
        String valueToSet = turboEnabled
                ? PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(Constants.KEY_TURBO_CURRENT, Constants.DEFAULT_TURBO_ON)
                : DEFAULT_OFF_VALUE;
        Log.i(TAG, "Setting System property " + Constants.PROP_TURBO_CURRENT + " to: " + valueToSet);
        setChargingProperty(valueToSet);
    }

    private void updateSportsMode(boolean enabled) {
        String value = enabled ? "1" : "0";
        Log.i(TAG, "Setting Sports Mode to: " + value);
        setSystemProperty("persist.vendor.turbo.sports", value);
    }

    private void setChargingProperty(String value) {
        setSystemProperty(Constants.PROP_TURBO_CURRENT, value);
    }

    private void setSystemProperty(String key, String value) {
        try {
            Process process = Runtime.getRuntime().exec("setprop " + key + " " + value);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Failed to set property " + key, e);
        }
    }
}
