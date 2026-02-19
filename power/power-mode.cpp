/*
 * Copyright (C) 2021 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <aidl/android/hardware/power/BnPower.h>
#include <android-base/file.h>
#include <android-base/logging.h>
#include <sys/ioctl.h>

// Touch device ioctl definitions
#define CMD_DATA_BUF_SIZE 256
#define COMMON_DATA_CMD 0
#define SELECT_TOUCH_ID 3
#define SET_CUR_VALUE 0
#define TOUCH_DOUBLETAP_MODE 14
#define TOUCH_GLOVE_MODE 15
#define TOUCH_MAGIC 0x54
#define TOUCH_DEV_PATH "/dev/xiaomi-touch"
#define TOUCH_ID 0

// Power mode sysfs paths
#define CPU_FREQ_MIN_PATH "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq"
#define CPU_FREQ_MAX_PATH "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq"
#define CPU_FREQ_MIN_PRIME_PATH "/sys/devices/system/cpu/cpufreq/policy6/scaling_min_freq"
#define CPU_FREQ_MAX_PRIME_PATH "/sys/devices/system/cpu/cpufreq/policy6/scaling_max_freq"
#define GPU_FREQ_MAX_PATH "/sys/class/kgsl/kgsl-0/devfreq/max_freq"
#define THERMAL_MODE_PATH "/sys/class/thermal/thermal_message/sconfig"
#define POWER_SAVE_MODE_PATH "/sys/powersave/limit_buf"

// Frequency limits for power modes (in KHz)
#define CPU_LOW_POWER_MIN 300000
#define CPU_LOW_POWER_MAX 1036800
#define CPU_NORMAL_MIN 518400
#define CPU_NORMAL_MAX 1804800
#define CPU_PRIME_LOW_POWER_MAX 1555200
#define CPU_PRIME_NORMAL_MAX 3357000

// Thermal mode IDs
#define THERMAL_MODE_DEFAULT 0
#define THERMAL_MODE_PERFORMANCE 18
#define THERMAL_MODE_POWERSAVE 1

typedef struct {
    int8_t   touch_id;
    uint8_t  cmd;
    uint16_t mode;
    uint16_t data_len;
    int32_t  data_buf[CMD_DATA_BUF_SIZE];
} touch_data;

#define TOUCH_IOC_COMMON_DATA _IOW(TOUCH_MAGIC, COMMON_DATA_CMD, touch_data)
#define TOUCH_IOC_SELECT_TOUCH_ID _IOW(TOUCH_MAGIC, SELECT_TOUCH_ID, int)

namespace aidl {
namespace google {
namespace hardware {
namespace power {
namespace impl {
namespace pixel {

using ::aidl::android::hardware::power::Mode;
using ::android::base::WriteStringToFile;

// Helper function to write to sysfs
static bool writeSysfs(const std::string& path, const std::string& value) {
    if (!WriteStringToFile(value, path)) {
        LOG(WARNING) << "Failed to write " << value << " to " << path;
        return false;
    }
    return true;
}

bool isDeviceSpecificModeSupported(Mode type, bool* _aidl_return) {
    switch (type) {
        case Mode::DOUBLE_TAP_TO_WAKE:
        case Mode::LOW_POWER:
        case Mode::SUSTAINED_PERFORMANCE:
        case Mode::FIXED_PERFORMANCE:
        case Mode::VR:
        case Mode::LAUNCH:
        case Mode::EXPENSIVE_RENDERING:
            *_aidl_return = true;
            return true;
        default:
            return false;
    }
}

bool setDeviceSpecificMode(Mode type, bool enabled) {
    switch (type) {
        case Mode::DOUBLE_TAP_TO_WAKE: {
            int fd = open(TOUCH_DEV_PATH, O_RDWR);
            if (fd < 0) {
                LOG(WARNING) << "Failed to open touch device for double-tap-to-wake";
                return false;
            }
            ioctl(fd, TOUCH_IOC_SELECT_TOUCH_ID, TOUCH_ID);
            touch_data data = {};
            data.touch_id = TOUCH_ID;
            data.cmd = SET_CUR_VALUE;
            data.mode = TOUCH_DOUBLETAP_MODE;
            data.data_len = 1;
            data.data_buf[0] = enabled ? 1 : 0;
            ioctl(fd, TOUCH_IOC_COMMON_DATA, &data);
            close(fd);
            return true;
        }
        case Mode::LOW_POWER: {
            // Enable/disable low power mode
            // Restrict CPU frequencies and enable aggressive power saving
            if (enabled) {
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_LOW_POWER_MIN));
                writeSysfs(CPU_FREQ_MAX_PATH, std::to_string(CPU_LOW_POWER_MAX));
                writeSysfs(CPU_FREQ_MAX_PRIME_PATH, std::to_string(CPU_PRIME_LOW_POWER_MAX));
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_POWERSAVE));
                writeSysfs(POWER_SAVE_MODE_PATH, "1");
            } else {
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MIN));
                writeSysfs(CPU_FREQ_MAX_PATH, std::to_string(CPU_NORMAL_MAX));
                writeSysfs(CPU_FREQ_MAX_PRIME_PATH, std::to_string(CPU_PRIME_NORMAL_MAX));
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_DEFAULT));
                writeSysfs(POWER_SAVE_MODE_PATH, "0");
            }
            return true;
        }
        case Mode::SUSTAINED_PERFORMANCE: {
            // Sustained performance mode for long-running workloads
            // Sets thermal limits to prevent throttling during gaming
            if (enabled) {
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_PERFORMANCE));
            } else {
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_DEFAULT));
            }
            return true;
        }
        case Mode::FIXED_PERFORMANCE: {
            // Fixed performance for benchmarking
            // Locks CPU to maximum frequencies
            if (enabled) {
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MAX));
                writeSysfs(CPU_FREQ_MAX_PATH, std::to_string(CPU_NORMAL_MAX));
                writeSysfs(CPU_FREQ_MIN_PRIME_PATH, std::to_string(CPU_PRIME_NORMAL_MAX));
                writeSysfs(CPU_FREQ_MAX_PRIME_PATH, std::to_string(CPU_PRIME_NORMAL_MAX));
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_PERFORMANCE));
            } else {
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MIN));
                writeSysfs(CPU_FREQ_MAX_PATH, std::to_string(CPU_NORMAL_MAX));
                writeSysfs(CPU_FREQ_MAX_PRIME_PATH, std::to_string(CPU_PRIME_NORMAL_MAX));
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_DEFAULT));
            }
            return true;
        }
        case Mode::VR: {
            // VR mode - consistent performance for VR applications
            // Similar to sustained but with stricter requirements
            if (enabled) {
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_PERFORMANCE));
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MIN));
            } else {
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_DEFAULT));
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MIN));
            }
            return true;
        }
        case Mode::LAUNCH: {
            // Launch boost mode for app startup
            // Temporarily boost CPU frequencies
            if (enabled) {
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MAX));
                writeSysfs(CPU_FREQ_MIN_PRIME_PATH, std::to_string(CPU_PRIME_NORMAL_MAX));
            } else {
                writeSysfs(CPU_FREQ_MIN_PATH, std::to_string(CPU_NORMAL_MIN));
                writeSysfs(CPU_FREQ_MIN_PRIME_PATH, std::to_string(300000));
            }
            return true;
        }
        case Mode::EXPENSIVE_RENDERING: {
            // Expensive rendering mode for GPU-intensive tasks
            // Boost GPU frequency
            if (enabled) {
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_PERFORMANCE));
            } else {
                writeSysfs(THERMAL_MODE_PATH, std::to_string(THERMAL_MODE_DEFAULT));
            }
            return true;
        }
        default:
            return false;
    }
}

}  // namespace pixel
}  // namespace impl
}  // namespace power
}  // namespace hardware
}  // namespace google
}  // namespace aidl
