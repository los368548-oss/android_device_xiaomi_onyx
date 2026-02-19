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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.Constants;
import org.lineageos.settings.utils.FileUtils;

import java.io.File;

public class TurboChargingUtil {

    private static final String TAG = "TurboChargingUtil";

    // MCA Charging sysfs nodes for 90W charging
    private static final String MCA_CHARGE_CURRENT = "/sys/devices/platform/soc/soc:mca_charge_interface/charge_current";
    private static final String MCA_CHARGE_TYPE = "/sys/devices/platform/soc/soc:mca_charge_interface/charge_type";
    private static final String MCA_QUICK_CHARGE_ENABLE = "/sys/devices/platform/soc/soc:mca_strategy_quick_charge/enable";
    private static final String MCA_PD_AUTH_ENABLE = "/sys/devices/platform/soc/soc:mca_strategy_pd_auth/enable";
    private static final String MCA_BUSINESS_CHARGER = "/sys/devices/platform/soc/soc:mca_business_charger/charge_enable";
    
    // xm_power sysfs nodes
    private static final String XM_CHARGER_CURRENT_MAX = "/sys/devices/virtual/xm_power/charger/constant_charge_current_max";
    private static final String XM_CHARGER_INPUT_MAX = "/sys/devices/virtual/xm_power/charger/input_current_max";
    private static final String XM_CHARGE_PUMP_ENABLE = "/sys/devices/virtual/xm_power/charge_pump/charge_pump_enable";
    
    // Alternative qcom-battery nodes
    private static final String QCOM_SPORT_MODE = "/sys/class/qcom-battery/sport_mode";
    private static final String QCOM_CHARGE_CURRENT = "/sys/class/qcom-battery/constant_charge_current_max";

    public static void applyTurboSetting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean turboEnabled = prefs.getBoolean(Constants.KEY_TURBO_ENABLED, false);
        String turboCurrent = prefs.getString(Constants.KEY_TURBO_CURRENT, Constants.DEFAULT_TURBO_ON);
        boolean sportsMode = prefs.getBoolean(Constants.KEY_SPORTS_MODE, false);

        Log.d(TAG, "Applying turbo settings: enabled=" + turboEnabled + ", current=" + turboCurrent + ", sports=" + sportsMode);

        String currentToSet = turboEnabled ? turboCurrent : Constants.DEFAULT_TURBO_OFF;
        
        try {
            SystemProperties.set(Constants.PROP_TURBO_CURRENT, currentToSet);
            SystemProperties.set("persist.vendor.turbo.sports", sportsMode ? "1" : "0");
            SystemProperties.set("persist.vendor.turbo.enabled", turboEnabled ? "1" : "0");
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to set system properties", e);
        }

        if (turboEnabled) {
            enable90WCharging(currentToSet, sportsMode);
        } else {
            disable90WCharging();
        }
    }

    private static void enable90WCharging(String currentUA, boolean sportsMode) {
        Log.d(TAG, "Enabling 90W charging with current: " + currentUA + " uA");
        
        int currentMA = Integer.parseInt(currentUA) / 1000;
        
        FileUtils.writeLine(MCA_QUICK_CHARGE_ENABLE, "1");
        FileUtils.writeLine(MCA_PD_AUTH_ENABLE, "1");
        
        FileUtils.writeLine(MCA_CHARGE_CURRENT, currentUA);
        FileUtils.writeLine(MCA_CHARGE_TYPE, "quick");
        
        FileUtils.writeLine(MCA_BUSINESS_CHARGER, "1");
        
        FileUtils.writeLine(XM_CHARGER_CURRENT_MAX, currentUA);
        FileUtils.writeLine(XM_CHARGER_INPUT_MAX, currentUA);
        
        if (sportsMode) {
            FileUtils.writeLine(XM_CHARGE_PUMP_ENABLE, "1");
            FileUtils.writeLine(QCOM_SPORT_MODE, "1");
            try {
                SystemProperties.set("vendor.charge.sports.mode", "1");
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to set vendor.charge.sports.mode", e);
            }
        } else {
            FileUtils.writeLine(XM_CHARGE_PUMP_ENABLE, "0");
            FileUtils.writeLine(QCOM_SPORT_MODE, "0");
            try {
                SystemProperties.set("vendor.charge.sports.mode", "0");
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to set vendor.charge.sports.mode", e);
            }
        }
        
        FileUtils.writeLine(QCOM_CHARGE_CURRENT, currentUA);
        
        Log.d(TAG, "90W charging enabled - MCA quick_charge=1, PD_auth=1, current=" + currentMA + "mA");
    }

    private static void disable90WCharging() {
        Log.d(TAG, "Disabling 90W fast charging");
        
        FileUtils.writeLine(MCA_QUICK_CHARGE_ENABLE, "0");
        FileUtils.writeLine(MCA_PD_AUTH_ENABLE, "0");
        
        FileUtils.writeLine(MCA_CHARGE_CURRENT, "0");
        FileUtils.writeLine(MCA_CHARGE_TYPE, "standard");
        
        FileUtils.writeLine(MCA_BUSINESS_CHARGER, "0");
        
        FileUtils.writeLine(XM_CHARGER_CURRENT_MAX, "0");
        FileUtils.writeLine(XM_CHARGER_INPUT_MAX, "0");
        FileUtils.writeLine(XM_CHARGE_PUMP_ENABLE, "0");
        
        FileUtils.writeLine(QCOM_SPORT_MODE, "0");
        FileUtils.writeLine(QCOM_CHARGE_CURRENT, "0");
        
        Log.d(TAG, "90W charging disabled");
    }

    public static boolean isTurboChargingSupported() {
        return new File(MCA_QUICK_CHARGE_ENABLE).exists() ||
               new File(XM_CHARGER_CURRENT_MAX).exists() ||
               new File(QCOM_CHARGE_CURRENT).exists();
    }

    public static String getCurrentChargingWattage() {
        String current = FileUtils.readLine(QCOM_CHARGE_CURRENT);
        if (current != null && !current.isEmpty()) {
            try {
                int currentUA = Integer.parseInt(current.trim());
                int currentMA = currentUA / 1000;
                int wattage = (currentMA * 11) / 10000; // Approximate wattage at ~11V
                return wattage + "W";
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse current: " + current, e);
            }
        }
        return "Unknown";
    }
}