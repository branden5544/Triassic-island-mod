package com.triassicisland.power;

/** Voltage tiers, as in Electrical Age. Devices are built for one tier and are damaged well above it. */
public final class Voltage {
    public static final double LOW = 50;
    public static final double MEDIUM = 200;
    public static final double HIGH = 800;
    /** Above this multiple of its rating, a device overheats and is destroyed. */
    public static final double OVERVOLTAGE_LIMIT = 1.3;

    private Voltage() {}
}
