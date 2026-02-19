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
import android.os.SystemProperties;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.Constants;
import org.lineageos.settings.utils.FileUtils;

import java.io.File;

public class DolbyVisionUtil {

    private static final String TAG = "DolbyVisionUtil";
    
    private static final String DV_SYSFS_PATH = "/sys/class/dolby_vision";
    private static final String HDR_SYSFS_PATH = "/sys/class/hdr";

    public static void applyDolbyVisionSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean enabled = prefs.getBoolean(Constants.KEY_DOLBY_VISION_ENABLED, true);
        String profile = prefs.getString(Constants.KEY_DV_PROFILE, "cinema");
        
        boolean contentDetection = prefs.getBoolean(Constants.KEY_DV_CONTENT_DETECTION, true);
        boolean toneMapping = prefs.getBoolean(Constants.KEY_DV_TONE_MAPPING, true);
        boolean dynamicMetadata = prefs.getBoolean(Constants.KEY_DV_DYNAMIC_METADATA, true);
        boolean ambientAdaptation = prefs.getBoolean(Constants.KEY_DV_AMBIENT_ADAPTATION, true);
        int highlightRecovery = prefs.getInt(Constants.KEY_DV_HIGHLIGHT_RECOVERY, 50);
        int shadowEnhancement = prefs.getInt(Constants.KEY_DV_SHADOW_ENHANCEMENT, 50);
        
        boolean colorEnhancement = prefs.getBoolean(Constants.KEY_DV_COLOR_ENHANCEMENT, true);
        int colorSaturation = prefs.getInt(Constants.KEY_DV_COLOR_SATURATION, 50);
        String colorGamut = prefs.getString(Constants.KEY_DV_COLOR_GAMUT, "auto");
        boolean skinTone = prefs.getBoolean(Constants.KEY_DV_SKIN_TONE, true);
        boolean memoryColor = prefs.getBoolean(Constants.KEY_DV_MEMORY_COLOR, true);
        
        boolean motionInterpolation = prefs.getBoolean(Constants.KEY_DV_MOTION_INTERPOLATION, false);
        int motionBlurReduction = prefs.getInt(Constants.KEY_DV_MOTION_BLUR_REDUCTION, 30);
        int judderReduction = prefs.getInt(Constants.KEY_DV_JUDDER_REDUCTION, 30);
        boolean blackFrameInsertion = prefs.getBoolean(Constants.KEY_DV_BLACK_FRAME_INSERTION, false);
        
        boolean lowLatency = prefs.getBoolean(Constants.KEY_DV_LOW_LATENCY, false);
        boolean vrr = prefs.getBoolean(Constants.KEY_DV_VRR, true);
        boolean hdr10Plus = prefs.getBoolean(Constants.KEY_DV_HDR10_PLUS, true);
        boolean hlg = prefs.getBoolean(Constants.KEY_DV_HLG, true);
        String peakBrightness = prefs.getString(Constants.KEY_DV_PEAK_BRIGHTNESS, "2000");

        Log.d(TAG, "Applying Dolby Vision settings: enabled=" + enabled + ", profile=" + profile);

        try {
            SystemProperties.set("persist.vendor.dolby.vision.enabled", enabled ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.profile", profile);
            SystemProperties.set("vendor.display.dolby_vision.enabled", enabled ? "1" : "0");
            
            SystemProperties.set("persist.vendor.dolby.vision.content.detection", contentDetection ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.tone.mapping", toneMapping ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.dynamic.metadata", dynamicMetadata ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.ambient.adaptation", ambientAdaptation ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.highlight.recovery", String.valueOf(highlightRecovery));
            SystemProperties.set("persist.vendor.dolby.vision.shadow.enhancement", String.valueOf(shadowEnhancement));
            
            SystemProperties.set("persist.vendor.dolby.vision.color.enhancement", colorEnhancement ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.color.saturation", String.valueOf(colorSaturation));
            SystemProperties.set("persist.vendor.dolby.vision.color.gamut", colorGamut);
            SystemProperties.set("persist.vendor.dolby.vision.skin.tone", skinTone ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.memory.color", memoryColor ? "true" : "false");
            
            SystemProperties.set("persist.vendor.dolby.vision.motion.interpolation", motionInterpolation ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.motion.blur.reduction", String.valueOf(motionBlurReduction));
            SystemProperties.set("persist.vendor.dolby.vision.judder.reduction", String.valueOf(judderReduction));
            SystemProperties.set("persist.vendor.dolby.vision.black.frame.insertion", blackFrameInsertion ? "true" : "false");
            
            SystemProperties.set("persist.vendor.dolby.vision.low.latency", lowLatency ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.vrr", vrr ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.hdr10.plus", hdr10Plus ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.hlg", hlg ? "true" : "false");
            SystemProperties.set("persist.vendor.dolby.vision.peak.brightness", peakBrightness);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to set system properties", e);
        }
        
        applyToSysfs(enabled, profile, peakBrightness);
        applyDisplayProfile(enabled, profile);
    }
    
    private static void applyToSysfs(boolean enabled, String profile, String peakBrightness) {
        try {
            File dvEnabled = new File(DV_SYSFS_PATH + "/enabled");
            if (dvEnabled.exists()) {
                FileUtils.writeLine(dvEnabled.getAbsolutePath(), enabled ? "1" : "0");
            }
            
            File dvProfile = new File(DV_SYSFS_PATH + "/profile");
            if (dvProfile.exists()) {
                FileUtils.writeLine(dvProfile.getAbsolutePath(), getProfileIndex(profile));
            }
            
            File hdrEnabled = new File(HDR_SYSFS_PATH + "/enable");
            if (hdrEnabled.exists()) {
                FileUtils.writeLine(hdrEnabled.getAbsolutePath(), enabled ? "1" : "0");
            }
            
            File peakNits = new File(HDR_SYSFS_PATH + "/peak_brightness");
            if (peakNits.exists()) {
                FileUtils.writeLine(peakNits.getAbsolutePath(), peakBrightness);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to write to sysfs nodes", e);
        }
    }
    
    private static void applyDisplayProfile(boolean enabled, String profile) {
        try {
            String mode = enabled ? getDisplayMode(profile) : "standard";
            SystemProperties.set("vendor.display.mode", mode);
            SystemProperties.set("persist.vendor.display.profile", profile);
            
            int hdrMode = enabled ? 1 : 0;
            SystemProperties.set("vendor.display.hdr.mode", String.valueOf(hdrMode));
            
            String pqMode = "pq_" + profile;
            SystemProperties.set("vendor.display.pq.mode", pqMode);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to apply display profile", e);
        }
    }
    
    private static String getProfileIndex(String profile) {
        switch (profile) {
            case "cinema": return "0";
            case "vivid": return "1";
            case "standard": return "2";
            case "game": return "3";
            case "photo": return "4";
            case "sports": return "5";
            case "reading": return "6";
            case "outdoor": return "7";
            default: return "0";
        }
    }
    
    private static String getDisplayMode(String profile) {
        switch (profile) {
            case "cinema": return "hdr_cinema";
            case "vivid": return "hdr_vivid";
            case "standard": return "hdr_standard";
            case "game": return "hdr_game";
            case "photo": return "hdr_photo";
            case "sports": return "hdr_sports";
            case "reading": return "reading";
            case "outdoor": return "outdoor";
            default: return "hdr_cinema";
        }
    }
}