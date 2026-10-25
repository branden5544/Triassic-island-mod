package com.triassicisland.park;

import com.triassicisland.power.Voltage;

/** Each fence block draws a little power all the time (leakage through the insulators). */
public enum FenceTier {
    STANDARD(Voltage.MEDIUM, 4), HIGH_VOLTAGE(Voltage.HIGH, 8);

    public final double ratedVoltage;
    public final double idleWatts;

    FenceTier(double ratedVoltage, double idleWatts) {
        this.ratedVoltage = ratedVoltage;
        this.idleWatts = idleWatts;
    }
}
