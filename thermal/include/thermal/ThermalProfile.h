/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

namespace thermal {
namespace profile {

// Thermal profile types
enum class ThermalProfile {
    DEFAULT,      // Default system behavior
    POWERSAVE,    // Maximum power saving
    BALANCED,     // Balance between performance and power
    PERFORMANCE,  // Maximum performance
    GAME,         // Optimized for gaming
    TURBO         // Maximum performance with minimal throttling
};

// Set thermal profile
bool setThermalProfile(ThermalProfile profile);

// Convenience functions for specific profiles
bool setDefaultProfile();
bool setPowersaveProfile();
bool setBalancedProfile();
bool setPerformanceProfile();
bool setGameProfile();
bool setTurboProfile();

}  // namespace profile
}  // namespace thermal
