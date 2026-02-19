/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <string>

// Torch sysfs paths for Xiaomi Onyx
#define TORCH_0_BRIGHTNESS_PATH "/sys/class/leds/led:torch_0/brightness"
#define TORCH_1_BRIGHTNESS_PATH "/sys/class/leds/led:torch_1/brightness"
#define FLASH_0_BRIGHTNESS_PATH "/sys/class/leds/led:flash_0/brightness"
#define FLASH_1_BRIGHTNESS_PATH "/sys/class/leds/led:flash_1/brightness"
#define FLASHLIGHT_BRIGHTNESS_PATH "/sys/class/leds/flashlight/brightness"

// Torch brightness levels (device-specific)
#define TORCH_LEVEL_OFF 0
#define TORCH_LEVEL_LOW 1
#define TORCH_LEVEL_MEDIUM 127
#define TORCH_LEVEL_HIGH 200
#define TORCH_LEVEL_MAX 255

namespace torch {
namespace control {

using ::android::base::WriteStringToFile;

// Torch brightness levels
enum class TorchLevel {
    OFF,        // Torch off
    LOW,        // Low brightness (useful for close-up)
    MEDIUM,     // Medium brightness
    HIGH,       // High brightness
    MAX         // Maximum brightness
};

// Torch modes
enum class TorchMode {
    SINGLE,     // Single torch (torch_0 only)
    DUAL,       // Dual torch (both torches)
    FLASHLIGHT  // Main flashlight
};

// Helper function to write to sysfs
static bool writeSysfs(const std::string& path, const std::string& value) {
    if (!WriteStringToFile(value, path)) {
        LOG(WARNING) << "Failed to write " << value << " to " << path;
        return false;
    }
    return true;
}

// Get brightness value from level enum
static int getBrightnessValue(TorchLevel level) {
    switch (level) {
        case TorchLevel::OFF:
            return TORCH_LEVEL_OFF;
        case TorchLevel::LOW:
            return TORCH_LEVEL_LOW;
        case TorchLevel::MEDIUM:
            return TORCH_LEVEL_MEDIUM;
        case TorchLevel::HIGH:
            return TORCH_LEVEL_HIGH;
        case TorchLevel::MAX:
            return TORCH_LEVEL_MAX;
        default:
            return TORCH_LEVEL_OFF;
    }
}

// Set torch brightness (0-255)
bool setTorchBrightness(int brightness) {
    if (brightness < 0 || brightness > 255) {
        LOG(WARNING) << "Invalid torch brightness value: " << brightness;
        return false;
    }
    
    // Write to both torch LEDs
    bool success = true;
    success &= writeSysfs(TORCH_0_BRIGHTNESS_PATH, std::to_string(brightness));
    success &= writeSysfs(TORCH_1_BRIGHTNESS_PATH, std::to_string(brightness));
    
    return success;
}

// Set torch level using predefined levels
bool setTorchLevel(TorchLevel level) {
    int brightness = getBrightnessValue(level);
    return setTorchBrightness(brightness);
}

// Set torch mode (single, dual, or flashlight)
bool setTorchMode(TorchMode mode, TorchLevel level) {
    int brightness = getBrightnessValue(level);
    
    switch (mode) {
        case TorchMode::SINGLE:
            // Use only torch_0
            writeSysfs(TORCH_0_BRIGHTNESS_PATH, std::to_string(brightness));
            writeSysfs(TORCH_1_BRIGHTNESS_PATH, "0");
            break;
        case TorchMode::DUAL:
            // Use both torches
            writeSysfs(TORCH_0_BRIGHTNESS_PATH, std::to_string(brightness));
            writeSysfs(TORCH_1_BRIGHTNESS_PATH, std::to_string(brightness));
            break;
        case TorchMode::FLASHLIGHT:
            // Use main flashlight
            writeSysfs(FLASHLIGHT_BRIGHTNESS_PATH, std::to_string(brightness));
            break;
        default:
            return false;
    }
    
    return true;
}

// Turn torch on with specified level
bool torchOn(TorchLevel level) {
    return setTorchLevel(level);
}

// Turn torch off
bool torchOff() {
    return setTorchLevel(TorchLevel::OFF);
}

// Strobe effect - flash at specified interval (milliseconds)
// Note: This is a blocking function for demonstration
// In production, use a separate thread or timer
bool torchStrobe(TorchLevel level, int onMs, int offMs, int count) {
    if (onMs < 10 || offMs < 10 || count < 1) {
        LOG(WARNING) << "Invalid strobe parameters";
        return false;
    }
    
    int brightness = getBrightnessValue(level);
    
    for (int i = 0; i < count; i++) {
        setTorchBrightness(brightness);
        usleep(onMs * 1000);
        setTorchBrightness(0);
        usleep(offMs * 1000);
    }
    
    return true;
}

// SOS pattern (3 short, 3 long, 3 short)
bool torchSOS() {
    int shortMs = 200;
    int longMs = 600;
    int gapMs = 200;
    
    // 3 short
    for (int i = 0; i < 3; i++) {
        setTorchLevel(TorchLevel::HIGH);
        usleep(shortMs * 1000);
        setTorchLevel(TorchLevel::OFF);
        usleep(gapMs * 1000);
    }
    
    // 3 long
    for (int i = 0; i < 3; i++) {
        setTorchLevel(TorchLevel::HIGH);
        usleep(longMs * 1000);
        setTorchLevel(TorchLevel::OFF);
        usleep(gapMs * 1000);
    }
    
    // 3 short
    for (int i = 0; i < 3; i++) {
        setTorchLevel(TorchLevel::HIGH);
        usleep(shortMs * 1000);
        setTorchLevel(TorchLevel::OFF);
        usleep(gapMs * 1000);
    }
    
    return true;
}

// Convenience functions
bool torchOnLow() { return torchOn(TorchLevel::LOW); }
bool torchOnMedium() { return torchOn(TorchLevel::MEDIUM); }
bool torchOnHigh() { return torchOn(TorchLevel::HIGH); }
bool torchOnMax() { return torchOn(TorchLevel::MAX); }

}  // namespace control
}  // namespace torch
