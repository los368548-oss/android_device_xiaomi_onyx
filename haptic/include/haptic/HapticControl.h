/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

namespace haptic {
namespace control {

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

// Set vibration intensity/gain
bool setHapticIntensity(HapticIntensity intensity);

// Set custom gain (0-255)
bool setHapticGain(int gain);

// Perform a single vibration (duration in milliseconds)
bool vibrate(int durationMs);

// Perform vibration with intensity
bool vibrateWithIntensity(int durationMs, HapticIntensity intensity);

// Cancel ongoing vibration
bool cancelVibration();

// Play predefined pattern
bool playPattern(HapticPattern pattern);

// Convenience functions for intensity presets
bool setIntensityOff();
bool setIntensityLight();
bool setIntensityMedium();
bool setIntensityStrong();
bool setIntensityMax();

// Convenience functions for patterns
bool playClick();
bool playDoubleClick();
bool playTick();
bool playHeavyClick();
bool playSuccess();
bool playFailure();
bool playNotification();

}  // namespace control
}  // namespace haptic
