/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <string>

// Thermal sysfs paths
#define THERMAL_MODE_PATH "/sys/class/thermal/thermal_message/sconfig"
#define THERMAL_BALANCE_MODE_PATH "/sys/class/thermal/thermal_message/balance_mode"
#define POWER_SAVE_MODE_PATH "/sys/powersave/limit_buf"
#define POWER_SAVE_LEVEL_PATH "/sys/powersave/power_level"

// Thermal mode IDs (device-specific for Xiaomi Onyx)
#define THERMAL_MODE_DEFAULT 0
#define THERMAL_MODE_POWERSAVE 1
#define THERMAL_MODE_BALANCED 5
#define THERMAL_MODE_PERFORMANCE 18
#define THERMAL_MODE_GAME 20
#define THERMAL_MODE_TURBO 22

namespace thermal {
namespace profile {

using ::android::base::WriteStringToFile;

// Thermal profile types
enum class ThermalProfile {
    DEFAULT,
    POWERSAVE,
    BALANCED,
    PERFORMANCE,
    GAME,
    TURBO
};

// Helper function to write to sysfs
static bool writeSysfs(const std::string& path, const std::string& value) {
    if (!WriteStringToFile(value, path)) {
        LOG(WARNING) << "Failed to write " << value << " to " << path;
        return false;
    }
    return true;
}

// Get thermal mode ID from profile enum
static int getThermalModeId(ThermalProfile profile) {
    switch (profile) {
        case ThermalProfile::DEFAULT:
            return THERMAL_MODE_DEFAULT;
        case ThermalProfile::POWERSAVE:
            return THERMAL_MODE_POWERSAVE;
        case ThermalProfile::BALANCED:
            return THERMAL_MODE_BALANCED;
        case ThermalProfile::PERFORMANCE:
            return THERMAL_MODE_PERFORMANCE;
        case ThermalProfile::GAME:
            return THERMAL_MODE_GAME;
        case ThermalProfile::TURBO:
            return THERMAL_MODE_TURBO;
        default:
            return THERMAL_MODE_DEFAULT;
    }
}

// Set thermal profile
bool setThermalProfile(ThermalProfile profile) {
    int modeId = getThermalModeId(profile);
    
    // Set main thermal mode
    if (!writeSysfs(THERMAL_MODE_PATH, std::to_string(modeId))) {
        return false;
    }
    
    // Configure power save mode based on profile
    switch (profile) {
        case ThermalProfile::POWERSAVE:
            writeSysfs(POWER_SAVE_MODE_PATH, "1");
            writeSysfs(POWER_SAVE_LEVEL_PATH, "3");  // Max power saving
            break;
        case ThermalProfile::BALANCED:
            writeSysfs(POWER_SAVE_MODE_PATH, "0");
            writeSysfs(POWER_SAVE_LEVEL_PATH, "2");
            break;
        case ThermalProfile::PERFORMANCE:
        case ThermalProfile::GAME:
        case ThermalProfile::TURBO:
            writeSysfs(POWER_SAVE_MODE_PATH, "0");
            writeSysfs(POWER_SAVE_LEVEL_PATH, "0");  // No power saving
            break;
        default:
            writeSysfs(POWER_SAVE_MODE_PATH, "0");
            writeSysfs(POWER_SAVE_LEVEL_PATH, "2");
            break;
    }
    
    LOG(INFO) << "Thermal profile set to mode " << modeId;
    return true;
}

// Convenience functions for specific profiles
bool setDefaultProfile() {
    return setThermalProfile(ThermalProfile::DEFAULT);
}

bool setPowersaveProfile() {
    return setThermalProfile(ThermalProfile::POWERSAVE);
}

bool setBalancedProfile() {
    return setThermalProfile(ThermalProfile::BALANCED);
}

bool setPerformanceProfile() {
    return setThermalProfile(ThermalProfile::PERFORMANCE);
}

bool setGameProfile() {
    return setThermalProfile(ThermalProfile::GAME);
}

bool setTurboProfile() {
    return setThermalProfile(ThermalProfile::TURBO);
}

}  // namespace profile
}  // namespace thermal
