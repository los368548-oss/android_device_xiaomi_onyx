/*
 * Copyright (C) 2024 The LineageOS Project
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
#define TOUCH_GLOVE_MODE 15
#define TOUCH_MAGIC 0x54
#define TOUCH_DEV_PATH "/dev/xiaomi-touch"
#define TOUCH_ID 0

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

// Glove mode support - increases touch sensitivity for use with gloves
bool isGloveModeSupported() {
    return true;
}

bool setGloveMode(bool enabled) {
    int fd = open(TOUCH_DEV_PATH, O_RDWR);
    if (fd < 0) {
        LOG(WARNING) << "Failed to open touch device for glove mode";
        return false;
    }
    
    ioctl(fd, TOUCH_IOC_SELECT_TOUCH_ID, TOUCH_ID);
    touch_data data = {};
    data.touch_id = TOUCH_ID;
    data.cmd = SET_CUR_VALUE;
    data.mode = TOUCH_GLOVE_MODE;
    data.data_len = 1;
    data.data_buf[0] = enabled ? 1 : 0;
    
    int ret = ioctl(fd, TOUCH_IOC_COMMON_DATA, &data);
    close(fd);
    
    if (ret < 0) {
        LOG(WARNING) << "Failed to set glove mode";
        return false;
    }
    
    LOG(INFO) << "Glove mode " << (enabled ? "enabled" : "disabled");
    return true;
}

}  // namespace pixel
}  // namespace impl
}  // namespace power
}  // namespace hardware
}  // namespace google
}  // namespace aidl
