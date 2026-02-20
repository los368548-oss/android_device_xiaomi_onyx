#ifndef XIAOMI_CHARGING_CONTROL_H
#define XIAOMI_CHARGING_CONTROL_H

#include <string>

using namespace std;

class ChargeStatusListener {
public:
    int start();

private:
    string read_line(const char *file);
    int write_value(const char *file, const char *value);
    int write_value(const char *file, int value);
    int configure_fast_charge();
    int configure_charge_pump();
    int monitor_charging();
    int get_battery_temp();
    int adjust_charge_current(int temp);
    void apply_thermal_throttling(int temp, bool is_fast_charge);
};

#endif
