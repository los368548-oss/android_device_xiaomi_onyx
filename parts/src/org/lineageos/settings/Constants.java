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

package org.lineageos.settings;

public class Constants {

    // Dolby Vision
    public static final String KEY_DOLBY_VISION_ENABLED = "dolby_vision_enabled";
    public static final String KEY_DV_PROFILE = "dv_profile";
    public static final String KEY_DV_CONTENT_DETECTION = "dv_content_detection";
    public static final String KEY_DV_TONE_MAPPING = "dv_tone_mapping";
    public static final String KEY_DV_DYNAMIC_METADATA = "dv_dynamic_metadata";
    public static final String KEY_DV_AMBIENT_ADAPTATION = "dv_ambient_adaptation";
    public static final String KEY_DV_HIGHLIGHT_RECOVERY = "dv_highlight_recovery";
    public static final String KEY_DV_SHADOW_ENHANCEMENT = "dv_shadow_enhancement";
    public static final String KEY_DV_COLOR_ENHANCEMENT = "dv_color_enhancement";
    public static final String KEY_DV_COLOR_SATURATION = "dv_color_saturation";
    public static final String KEY_DV_COLOR_GAMUT = "dv_color_gamut";
    public static final String KEY_DV_SKIN_TONE = "dv_skin_tone";
    public static final String KEY_DV_MEMORY_COLOR = "dv_memory_color";
    public static final String KEY_DV_MOTION_INTERPOLATION = "dv_motion_interpolation";
    public static final String KEY_DV_MOTION_BLUR_REDUCTION = "dv_motion_blur_reduction";
    public static final String KEY_DV_JUDDER_REDUCTION = "dv_judder_reduction";
    public static final String KEY_DV_BLACK_FRAME_INSERTION = "dv_black_frame_insertion";
    public static final String KEY_DV_LOW_LATENCY = "dv_low_latency";
    public static final String KEY_DV_VRR = "dv_vrr";
    public static final String KEY_DV_HDR10_PLUS = "dv_hdr10_plus";
    public static final String KEY_DV_HLG = "dv_hlg";
    public static final String KEY_DV_PEAK_BRIGHTNESS = "dv_peak_brightness";

    // Turbo Charging
    public static final String KEY_TURBO_ENABLED = "turbo_enable";
    public static final String KEY_SPORTS_MODE = "sports_mode";
    public static final String KEY_TURBO_CURRENT = "turbo_current";
    public static final String PROP_TURBO_CURRENT = "persist.vendor.turbo.current";

    public static final String KEY_TC_ADAPTIVE_CHARGING = "tc_adaptive_charging";
    public static final String KEY_TC_START_TIME = "tc_start_time";
    public static final String KEY_TC_END_TIME = "tc_end_time";
    public static final String KEY_TC_BYPASS_CHARGING = "tc_bypass_charging";
    public static final String KEY_TC_TEMP_PROTECTION = "tc_temp_protection";
    public static final String KEY_TC_NIGHT_CHARGE = "tc_night_charge";
    public static final String KEY_TC_BATTERY_PROTECTION = "tc_battery_protection";
    public static final String KEY_TC_CHARGE_LIMIT = "tc_charge_limit";
    public static final String KEY_TC_USB_PD = "tc_usb_pd";
    public static final String KEY_TC_QUICK_CHARGE = "tc_quick_charge";
    public static final String KEY_TC_CHARGE_MONITOR = "tc_charge_monitor";
    public static final String KEY_TC_REAL_TIME_CURRENT = "tc_real_time_current";

    public static final String DEFAULT_TURBO_OFF = "0";
    public static final String DEFAULT_TURBO_ON = "9000000";

    // Sysfs nodes
    public static final String NODE_FASTCHARGE_CURRENT = "/sys/class/qcom-battery/constant_charge_current_max";
    public static final String NODE_BYPASS_CHARGING = "/sys/class/qcom-battery/bypass_charging";
    public static final String NODE_CHARGE_LIMIT = "/sys/class/qcom-battery/charge_limit";
    public static final String NODE_CHARGE_CYCLES = "/sys/class/qcom-battery/cycle_count";
    public static final String NODE_CHARGE_CURRENT_NOW = "/sys/class/qcom-battery/current_now";
    
    // MCA Charging nodes for 90W
    public static final String NODE_MCA_QUICK_CHARGE = "/sys/devices/platform/soc/soc:mca_strategy_quick_charge/enable";
    public static final String NODE_MCA_PD_AUTH = "/sys/devices/platform/soc/soc:mca_strategy_pd_auth/enable";
    public static final String NODE_MCA_CHARGE_CURRENT = "/sys/devices/platform/soc/soc:mca_charge_interface/charge_current";
    public static final String NODE_XM_CHARGER_CURRENT = "/sys/devices/virtual/xm_power/charger/constant_charge_current_max";
    public static final String NODE_SPORT_MODE = "/sys/class/qcom-battery/sport_mode";
}
