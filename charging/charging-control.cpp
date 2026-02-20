#define LOG_TAG "xiaomi-charging-control"

#include <fcntl.h>
#include <chrono>
#include <fstream>
#include <thread>
#include <dirent.h>
#include <log/log.h>
#include "charging-control.h"

#define MAX_PATH 256
#define THERMAL_SYSFS "/sys/class/thermal/"
#define TZ_DIR_NAME "thermal_zone"

#define USB_ONLINE "/sys/class/power_supply/usb/online"
#define USB_TYPE "/sys/class/power_supply/usb/real_type"
#define BATTERY_TEMP "/sys/class/power_supply/battery/temp"
#define BATTERY_CAPACITY "/sys/class/power_supply/battery/capacity"
#define BATTERY_STATUS "/sys/class/power_supply/battery/status"

#define SPORT_MODE "/sys/class/qcom-battery/sport_mode"
#define SCENARIO_FCC "/sys/class/qcom-battery/scenario_fcc"
#define CHARGE_DONE "/sys/class/qcom-battery/charge_done"

#define MCA_QUICK_CHARGE_ENABLE "/sys/devices/platform/soc/soc:mca_strategy_quick_charge/enable"
#define MCA_PD_AUTH_ENABLE "/sys/devices/platform/soc/soc:mca_strategy_pd_auth/enable"
#define MCA_CHARGE_TYPE "/sys/devices/platform/soc/soc:mca_charge_interface/charge_type"
#define MCA_CHARGE_CURRENT "/sys/devices/platform/soc/soc:mca_charge_interface/charge_current"

#define CHARGE_PUMP_ENABLE "/sys/devices/virtual/xm_power/charge_pump/charge_pump_enable"
#define CHARGE_PUMP_STATUS "/sys/devices/virtual/xm_power/charge_pump/charge_pump_status"

#define XM_CHARGER_TYPE "/sys/devices/virtual/xm_power/charger/charge_type"
#define XM_INPUT_CURRENT "/sys/devices/virtual/xm_power/charger/input_current_max"
#define XM_CONSTANT_CURRENT "/sys/devices/virtual/xm_power/charger/constant_charge_current_max"

#define MONITOR_INTERVAL 2000

static int temp_thresholds[] = { 150, 200, 250, 300, 350, 400, 450, 500, 550 };
static int qc_current_limits[] = { 9000000, 8500000, 8000000, 7500000, 7000000, 6500000, 6000000, 5500000, 5000000 };

string ChargeStatusListener::read_line(const char *file) {
    ifstream input_file(file);
    string output;

    if (!input_file.is_open()) {
        ALOGE("Failed to open %s", file);
        return "";
    }

    getline(input_file, output);
    return output;
}

int ChargeStatusListener::write_value(const char *file, const char *value) {
    int fd = TEMP_FAILURE_RETRY(open(file, O_WRONLY));
    if (fd < 0) {
        ALOGE("Failed to open %s, errno=%d", file, errno);
        return -errno;
    }

    int ret = TEMP_FAILURE_RETRY(write(fd, value, strlen(value)));
    if (ret < 0) {
        ALOGE("Failed to write %s to %s, errno=%d", value, file, errno);
        ret = -errno;
    } else {
        ret = 0;
        ALOGD("Wrote %s to %s", value, file);
    }

    close(fd);
    return ret;
}

int ChargeStatusListener::write_value(const char *file, int value) {
    return write_value(file, to_string(value).c_str());
}

int ChargeStatusListener::get_battery_temp() {
    string temp_str = read_line(BATTERY_TEMP);
    if (temp_str.empty()) {
        return 250;
    }
    return stoi(temp_str) / 10;
}

int ChargeStatusListener::configure_fast_charge() {
    ALOGI("Configuring 90W fast charge...");

    write_value(MCA_QUICK_CHARGE_ENABLE, 1);
    write_value(MCA_PD_AUTH_ENABLE, 1);

    write_value(SPORT_MODE, 1);

    write_value(CHARGE_PUMP_ENABLE, 1);

    write_value(MCA_CHARGE_TYPE, 4);

    ALOGI("90W fast charge configured");
    return 0;
}

int ChargeStatusListener::configure_charge_pump() {
    ALOGI("Configuring charge pump for high-power charging...");

    write_value(CHARGE_PUMP_ENABLE, 1);

    string pump_status = read_line(CHARGE_PUMP_STATUS);
    ALOGI("Charge pump status: %s", pump_status.c_str());

    return 0;
}

void ChargeStatusListener::apply_thermal_throttling(int temp, bool is_fast_charge) {
    int current_limit = 5000000;

    if (temp < 350) {
        current_limit = is_fast_charge ? 9000000 : 5000000;
    } else if (temp < 400) {
        current_limit = is_fast_charge ? 7500000 : 4000000;
    } else if (temp < 450) {
        current_limit = is_fast_charge ? 6000000 : 3500000;
    } else if (temp < 500) {
        current_limit = is_fast_charge ? 4500000 : 3000000;
    } else {
        current_limit = is_fast_charge ? 3500000 : 2500000;
        if (temp >= 550) {
            write_value(SPORT_MODE, 0);
        }
    }

    write_value(XM_INPUT_CURRENT, current_limit);
    ALOGD("Thermal throttling: temp=%d, current_limit=%d", temp, current_limit);
}

int ChargeStatusListener::adjust_charge_current(int temp) {
    string usb_online = read_line(USB_ONLINE);
    if (usb_online != "1") {
        ALOGD("USB not online, skipping current adjustment");
        return 0;
    }

    string charger_type = read_line(USB_TYPE);
    bool is_fast_charge = (charger_type == "HVDCP" || charger_type == "HVDCP3" ||
                          charger_type == "PD" || charger_type == "PPS");

    if (is_fast_charge) {
        configure_fast_charge();
    }

    apply_thermal_throttling(temp, is_fast_charge);

    return 0;
}

int ChargeStatusListener::monitor_charging() {
    ALOGI("Starting charging monitor loop");

    while (true) {
        int temp = get_battery_temp();

        adjust_charge_current(temp);

        string capacity = read_line(BATTERY_CAPACITY);
        string status = read_line(BATTERY_STATUS);

        ALOGD("Battery: capacity=%s%%, status=%s, temp=%dC",
              capacity.c_str(), status.c_str(), temp);

        if (status == "Full" || status == "Not charging") {
            write_value(SPORT_MODE, 0);
        }

        this_thread::sleep_for(chrono::milliseconds(MONITOR_INTERVAL));
    }

    return 0;
}

int ChargeStatusListener::start() {
    ALOGI("Xiaomi Charging Control Service starting...");

    configure_charge_pump();
    configure_fast_charge();

    return monitor_charging();
}
