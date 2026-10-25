package com.triassicisland.light;

import com.triassicisland.power.Voltage;

import java.util.Locale;

/**
 * Electrical Age's light bulbs. Each has a voltage, a power draw, a brightness and a lifetime (minutes of being lit).
 * Economic bulbs take a few seconds to warm up; farming bulbs make nearby crops grow faster.
 */
public enum BulbType {
    CARBON_INCANDESCENT_50V(Voltage.LOW, 60, 12, 60, 0, false),
    INCANDESCENT_200V(Voltage.MEDIUM, 100, 14, 120, 0, false),
    ECONOMIC_50V(Voltage.LOW, 20, 13, 360, 100, false),
    ECONOMIC_200V(Voltage.MEDIUM, 30, 14, 360, 100, false),
    SMALL_ECONOMIC_50V(Voltage.LOW, 8, 10, 360, 100, false),
    LED_50V(Voltage.LOW, 12, 14, 1200, 0, false),
    LED_200V(Voltage.MEDIUM, 20, 15, 1200, 0, false),
    FARMING_50V(Voltage.LOW, 40, 11, 240, 0, true),
    FARMING_200V(Voltage.MEDIUM, 80, 12, 240, 0, true);

    public final double voltage;
    public final double watts;
    public final int light;
    /** Lifetime in ticks of being lit. */
    public final int lifeTicks;
    /** Ticks to reach full brightness (0 = instant). */
    public final int warmup;
    public final boolean farming;

    BulbType(double voltage, double watts, int light, int lifeMinutes, int warmup, boolean farming) {
        this.voltage = voltage;
        this.watts = watts;
        this.light = light;
        this.lifeTicks = lifeMinutes * 1200;
        this.warmup = warmup;
        this.farming = farming;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT) + "_bulb";
    }
}
