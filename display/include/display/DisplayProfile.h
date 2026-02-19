/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

namespace display {
namespace profile {

// Color profile types
enum class ColorProfile {
    DEFAULT,    // Default/factory calibration
    SRGB,       // sRGB color gamut
    P3,         // DCI-P3 wide color gamut
    VIVID,      // Vivid/saturated colors
    CINEMA,     // Cinema mode (DCI-P3 with specific tuning)
    NATURAL     // Natural/accurate colors
};

// Color temperature types
enum class ColorTemperature {
    COOL,       // Cool/blue-ish white point
    DEFAULT,    // Default white point
    WARM        // Warm/yellow-ish white point
};

// Set color profile
bool setColorProfile(ColorProfile profile);

// Set color temperature
bool setColorTemperature(ColorTemperature temp);

// Set doze brightness for AOD (0-255)
bool setDozeBrightness(int brightness);

// Convenience functions for specific profiles
bool setDefaultProfile();
bool setSRGBProfile();
bool setP3Profile();
bool setVividProfile();
bool setCinemaProfile();
bool setNaturalProfile();

}  // namespace profile
}  // namespace display
