#define LOG_TAG "xiaomi-charging-control"

#include <log/log.h>
#include "charging-control.h"

int main() {
    ALOGI("Xiaomi 90W Charging Control Service");

    ChargeStatusListener listener;

    return listener.start();
}
