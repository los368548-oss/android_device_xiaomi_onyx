/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <string>

// Display sysfs paths for Xiaomi Onyx
#define DISPLAY_PARAM_PATH "/sys/class/mi_display/disp-DSI-0/disp_param"
#define DISPLAY_MIPI_RW_PATH "/sys/class/mi_display/disp-DSI-0/mipi_rw"
#define DISPLAY_PANEL_INFO_PATH "/sys/class/mi_display/disp-DSI-0/panel_info"
#define DISPLAY_DOZE_BRIGHTNESS_PATH "/sys/class/mi_display/disp-DSI-0/doze_brightness"

// Color mode values (device-specific)
#define COLOR_MODE_DEFAULT 0
#define COLOR_MODE_SRGB 1
#define COLOR_MODE_P3 2
#define COLOR_MODE_VIVID 3
#define COLOR_MODE_CINEMA 4
#define COLOR_MODE_NATURAL 5

// Color temperature values
#define COLOR_TEMP_COOL 0
#define COLOR_TEMP_DEFAULT 1
#define COLOR_TEMP_WARM 2

namespace display {
namespace profile {

using ::android::base::WriteStringToFile;

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

// Helper function to write to sysfs
static bool writeSysfs(const std::string& path, const std::string& value) {
    if (!WriteStringToFile(value, path)) {
        LOG(WARNING) << "Failed to write " << value << " to " << path;
        return false;
    }
    return true;
}

// Get color mode value from profile enum
static int getColorModeValue(ColorProfile profile) {
    switch (profile) {
        case ColorProfile::DEFAULT:
            return COLOR_MODE_DEFAULT;
        case ColorProfile::SRGB:
            return COLOR_MODE_SRGB;
        case ColorProfile::P3:
            return COLOR_MODE_P3;
        case ColorProfile::VIVID:
            return COLOR_MODE_VIVID;
        case ColorProfile::CINEMA:
            return COLOR_MODE_CINEMA;
        case ColorProfile::NATURAL:
            return COLOR_MODE_NATURAL;
        default:
            return COLOR_MODE_DEFAULT;
    }
}

// Set color profile
bool setColorProfile(ColorProfile profile) {
    int modeValue = getColorModeValue(profile);
    
    // Write color mode to display parameter
    // Format: "color_mode <value>"
    std::string cmd = "color_mode " + std::to_string(modeValue);
    if (!writeSysfs(DISPLAY_PARAM_PATH, cmd)) {
        return false;
    }
    
    LOG(INFO) << "Color profile set to mode " << modeValue;
    return true;
}

// Set color temperature
bool setColorTemperature(ColorTemperature temp) {
    int tempValue;
    switch (temp) {
        case ColorTemperature::COOL:
            tempValue = COLOR_TEMP_COOL;
            break;
        case ColorTemperature::DEFAULT:
            tempValue = COLOR_TEMP_DEFAULT;
            break;
        case ColorTemperature::WARM:
            tempValue = COLOR_TEMP_WARM;
            break;
        default:
            tempValue = COLOR_TEMP_DEFAULT;
    }
    
    // Write color temperature to display parameter
    std::string cmd = "color_temp " + std::to_string(tempValue);
    if (!writeSysfs(DISPLAY_PARAM_PATH, cmd)) {
        return false;
    }
    
    LOG(INFO) << "Color temperature set to " << tempValue;
    return true;
}

// Set doze brightness for AOD
bool setDozeBrightness(int brightness) {
    // Brightness range: 0-255
    if (brightness < 0 || brightness > 255) {
        LOG(WARNING) << "Invalid doze brightness value: " << brightness;
        return false;
    }
    
    return writeSysfs(DISPLAY_DOZE_BRIGHTNESS_PATH, std::to_string(brightness));
}

// Convenience functions for specific profiles
bool setDefaultProfile() {
    return setColorProfile(ColorProfile::DEFAULT);
}

bool setSRGBProfile() {
    return setColorProfile(ColorProfile::SRGB);
}

bool setP3Profile() {
    return setColorProfile(ColorProfile::P3);
}

bool setVividProfile() {
    return setColorProfile(ColorProfile::VIVID);
}

bool setCinemaProfile() {
    return setColorProfile(ColorProfile::CINEMA);
}

bool setNaturalProfile() {
    return setColorProfile(ColorProfile::NATURAL);
}

}  // namespace profile
}  // namespace display
