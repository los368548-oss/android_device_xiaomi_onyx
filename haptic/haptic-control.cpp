/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <fstream>
#include <string>

// Vibrator sysfs paths for Xiaomi Onyx
#define VIBRATOR_BRIGHTNESS_PATH "/sys/class/leds/vibrator/brightness"
#define VIBRATOR_DURATION_PATH "/sys/class/leds/vibrator/duration"
#define VIBRATOR_ACTIVATE_PATH "/sys/class/leds/vibrator/activate"
#define VIBRATOR_STATE_PATH "/sys/class/leds/vibrator/state"
#define VIBRATOR_GAIN_PATH "/sys/class/leds/vibrator/gain"

// QTI Vibrator HAL paths
#define VIBRATOR_EFFECT_PATH "/sys/class/leds/vibrator/device/rt_gain"
#define VIBRATOR_LRA_PATH "/sys/class/leds/vibrator/device/lra_resonant_frequency"

// Default values
#define DEFAULT_VIBRATION_DURATION 50
#define MIN_VIBRATION_DURATION 10
#define MAX_VIBRATION_DURATION 10000
#define DEFAULT_GAIN 128
#define MIN_GAIN 0
#define MAX_GAIN 255

namespace haptic {
namespace control {

using ::android::base::WriteStringToFile;

// Haptic intensity levels
enum class HapticIntensity {
    OFF,        // No vibration
    LIGHT,      // Light vibration (25%)
    MEDIUM,     // Medium vibration (50%)
    STRONG,     // Strong vibration (75%)
    MAX         // Maximum vibration (100%)
};

// Predefined vibration patterns
enum class HapticPattern {
    CLICK,          // Single click
    DOUBLE_CLICK,   // Double click
    TICK,           // Light tick
    HEAVY_CLICK,    // Heavy click
    SUCCESS,        // Success pattern
    FAILURE,        // Failure pattern
    NOTIFICATION,   // Notification pattern
    RINGTONE        // Ringtone pattern
};

// Helper function to write to sysfs
static bool writeSysfs(const std::string& path, const std::string& value) {
    if (!WriteStringToFile(value, path)) {
        LOG(WARNING) << "Failed to write " << value << " to " << path;
        return false;
    }
    return true;
}

// Get gain value from intensity enum
static int getGainFromIntensity(HapticIntensity intensity) {
    switch (intensity) {
        case HapticIntensity::OFF:
            return 0;
        case HapticIntensity::LIGHT:
            return 64;   // ~25%
        case HapticIntensity::MEDIUM:
            return 128;  // ~50%
        case HapticIntensity::STRONG:
            return 192;  // ~75%
        case HapticIntensity::MAX:
            return 255;  // 100%
        default:
            return DEFAULT_GAIN;
    }
}

// Set vibration intensity/gain
bool setHapticIntensity(HapticIntensity intensity) {
    int gain = getGainFromIntensity(intensity);
    
    // Try different gain paths
    if (!writeSysfs(VIBRATOR_GAIN_PATH, std::to_string(gain))) {
        // Fallback to brightness for some devices
        writeSysfs(VIBRATOR_BRIGHTNESS_PATH, std::to_string(gain));
    }
    
    return true;
}

// Set custom gain (0-255)
bool setHapticGain(int gain) {
    if (gain < MIN_GAIN || gain > MAX_GAIN) {
        LOG(WARNING) << "Invalid gain value: " << gain;
        return false;
    }
    
    return writeSysfs(VIBRATOR_GAIN_PATH, std::to_string(gain));
}

// Perform a single vibration
bool vibrate(int durationMs) {
    if (durationMs < MIN_VIBRATION_DURATION || durationMs > MAX_VIBRATION_DURATION) {
        LOG(WARNING) << "Invalid duration: " << durationMs;
        return false;
    }
    
    // Write duration first
    writeSysfs(VIBRATOR_DURATION_PATH, std::to_string(durationMs));
    // Then activate
    writeSysfs(VIBRATOR_ACTIVATE_PATH, "1");
    
    return true;
}

// Perform vibration with intensity
bool vibrateWithIntensity(int durationMs, HapticIntensity intensity) {
    setHapticIntensity(intensity);
    return vibrate(durationMs);
}

// Cancel ongoing vibration
bool cancelVibration() {
    writeSysfs(VIBRATOR_ACTIVATE_PATH, "0");
    writeSysfs(VIBRATOR_STATE_PATH, "0");
    return true;
}

// Play predefined pattern
bool playPattern(HapticPattern pattern) {
    switch (pattern) {
        case HapticPattern::CLICK:
            setHapticIntensity(HapticIntensity::MEDIUM);
            vibrate(20);
            break;
        case HapticPattern::DOUBLE_CLICK:
            setHapticIntensity(HapticIntensity::MEDIUM);
            vibrate(20);
            usleep(50000);  // 50ms gap
            vibrate(20);
            break;
        case HapticPattern::TICK:
            setHapticIntensity(HapticIntensity::LIGHT);
            vibrate(10);
            break;
        case HapticPattern::HEAVY_CLICK:
            setHapticIntensity(HapticIntensity::STRONG);
            vibrate(30);
            break;
        case HapticPattern::SUCCESS:
            // Short-long pattern for success
            setHapticIntensity(HapticIntensity::MEDIUM);
            vibrate(20);
            usleep(30000);
            vibrate(40);
            break;
        case HapticPattern::FAILURE:
            // Long-short pattern for failure
            setHapticIntensity(HapticIntensity::STRONG);
            vibrate(50);
            usleep(30000);
            vibrate(20);
            break;
        case HapticPattern::NOTIFICATION:
            // Double pulse for notification
            setHapticIntensity(HapticIntensity::MEDIUM);
            vibrate(30);
            usleep(50000);
            vibrate(30);
            usleep(50000);
            vibrate(30);
            break;
        case HapticPattern::RINGTONE:
            // Repeating pattern for ringtone
            setHapticIntensity(HapticIntensity::STRONG);
            for (int i = 0; i < 3; i++) {
                vibrate(100);
                usleep(100000);
                vibrate(100);
                usleep(300000);
            }
            break;
        default:
            return false;
    }
    return true;
}

// Convenience functions for intensity presets
bool setIntensityOff() { return setHapticIntensity(HapticIntensity::OFF); }
bool setIntensityLight() { return setHapticIntensity(HapticIntensity::LIGHT); }
bool setIntensityMedium() { return setHapticIntensity(HapticIntensity::MEDIUM); }
bool setIntensityStrong() { return setHapticIntensity(HapticIntensity::STRONG); }
bool setIntensityMax() { return setHapticIntensity(HapticIntensity::MAX); }

// Convenience functions for patterns
bool playClick() { return playPattern(HapticPattern::CLICK); }
bool playDoubleClick() { return playPattern(HapticPattern::DOUBLE_CLICK); }
bool playTick() { return playPattern(HapticPattern::TICK); }
bool playHeavyClick() { return playPattern(HapticPattern::HEAVY_CLICK); }
bool playSuccess() { return playPattern(HapticPattern::SUCCESS); }
bool playFailure() { return playPattern(HapticPattern::FAILURE); }
bool playNotification() { return playPattern(HapticPattern::NOTIFICATION); }

}  // namespace control
}  // namespace haptic
