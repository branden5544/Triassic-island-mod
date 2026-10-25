package com.triassicisland.light;

import java.util.Locale;

/**
 * Electrical Age's lamp sockets. Shapes are described pointing up (mounted on the floor) in pixels and rotated
 * for the other mountings. Suspended sockets hang from ceilings only; swinging ones are drawn by a renderer.
 */
public enum SocketType {
    LAMP_SOCKET_A(false, false, false, new double[]{5, 0, 5, 11, 8, 11}),
    LAMP_SOCKET_B_PROJECTOR(false, false, true, new double[]{4, 0, 4, 12, 9, 12}),
    ROBUST_LAMP_SOCKET(false, false, false, new double[]{3, 0, 3, 13, 10, 13}),
    FLAT_LAMP_SOCKET(false, false, false, new double[]{2, 0, 2, 14, 3, 14}),
    SIMPLE_LAMP_SOCKET(false, false, false, new double[]{6, 0, 6, 10, 8, 10}),
    SCONCE_LAMP_SOCKET(false, false, false, new double[]{5, 0, 4, 11, 11, 12}),
    FLUORESCENT_LAMP_SOCKET(false, false, false, new double[]{1, 0, 5, 15, 4, 11}),
    SUSPENDED_LAMP_SOCKET(true, true, false, new double[]{4, 0, 4, 12, 14, 12}),
    SUSPENDED_LAMP_SOCKET_NO_SWING(true, false, false, new double[]{4, 0, 4, 12, 14, 12}),
    LONG_SUSPENDED_LAMP_SOCKET(true, true, false, new double[]{6, 0, 6, 10, 16, 10}),
    LONG_SUSPENDED_LAMP_SOCKET_NO_SWING(true, false, false, new double[]{6, 0, 6, 10, 16, 10});

    public final boolean ceilingOnly;
    public final boolean swings;
    public final boolean projector;
    /** Collision box pointing up: x1, y1, z1, x2, y2, z2 in pixels. */
    public final double[] box;

    SocketType(boolean ceilingOnly, boolean swings, boolean projector, double[] box) {
        this.ceilingOnly = ceilingOnly;
        this.swings = swings;
        this.projector = projector;
        this.box = box;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
