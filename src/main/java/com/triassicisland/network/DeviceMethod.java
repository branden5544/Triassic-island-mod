package com.triassicisland.network;

import java.util.List;

/**
 * Something a computer can call on a device. Runs on the server thread. Arguments arrive as
 * String / Double / Long / Boolean / List / Map; return any of those (or null).
 */
@FunctionalInterface
public interface DeviceMethod {
    Object call(List<Object> args);

    static String string(List<Object> args, int index) {
        if (index >= args.size()) throw new IllegalArgumentException("missing argument " + (index + 1));
        return String.valueOf(args.get(index));
    }

    static double number(List<Object> args, int index) {
        if (index >= args.size() || !(args.get(index) instanceof Number n)) {
            throw new IllegalArgumentException("argument " + (index + 1) + " must be a number");
        }
        return n.doubleValue();
    }

    static boolean bool(List<Object> args, int index) {
        if (index >= args.size()) throw new IllegalArgumentException("missing argument " + (index + 1));
        Object value = args.get(index);
        return value instanceof Boolean b ? b : value instanceof Number n && n.doubleValue() != 0;
    }
}
