/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

namespace torch {
namespace control {

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

// Set torch brightness (0-255)
bool setTorchBrightness(int brightness);

// Set torch level using predefined levels
bool setTorchLevel(TorchLevel level);

// Set torch mode (single, dual, or flashlight)
bool setTorchMode(TorchMode mode, TorchLevel level);

// Turn torch on with specified level
bool torchOn(TorchLevel level);

// Turn torch off
bool torchOff();

// Strobe effect - flash at specified interval (milliseconds)
bool torchStrobe(TorchLevel level, int onMs, int offMs, int count);

// SOS pattern (3 short, 3 long, 3 short)
bool torchSOS();

// Convenience functions
bool torchOnLow();
bool torchOnMedium();
bool torchOnHigh();
bool torchOnMax();

}  // namespace control
}  // namespace torch
