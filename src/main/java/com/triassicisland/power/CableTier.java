package com.triassicisland.power;

/**
 * Every cable carries the same current; higher voltage cables move more power through it
 * (1 kW at 50 V, 4 kW at 200 V, 16 kW at 800 V). Too much current melts a cable, and too much
 * voltage breaks through its insulation.
 */
public enum CableTier {
    LOW(Voltage.LOW), MEDIUM(Voltage.MEDIUM), HIGH(Voltage.HIGH);

    /** Ohms per block of cable. */
    public static final double RESISTANCE = 0.05;
    public static final double RATED_AMPS = 20;

    public final double ratedVoltage;

    CableTier(double ratedVoltage) {
        this.ratedVoltage = ratedVoltage;
    }
}
