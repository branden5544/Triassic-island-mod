package com.triassicisland.computer;

import com.triassicisland.dino.DinosaurEntity;
import com.triassicisland.network.DataNetwork;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/** The Python modules a computer offers. Each computer gets its own set, bound to its {@link ScriptRunner}. */
final class Api {
    static Map<String, PyObject> modules(ScriptRunner r) {
        Map<String, PyObject> modules = new LinkedHashMap<>();
        modules.put("term", term(r));
        modules.put("fs", fs(r));
        modules.put("net", net(r));
        modules.put("computer", computer(r));
        modules.put("math", math());
        modules.put("random", random());
        return modules;
    }

    static PyObject rawInput(ScriptRunner r) {
        return new ApiFunction("raw_input", 0, 1, "raw_input([prompt]) -> line typed by the user", a -> {
            if (a.length > 0) r.terminal.write(a[0].toString());
            String line = r.readLine();
            r.terminal.write(line + "\n");
            return new PyString(line);
        });
    }

    // --- term ---

    private static PyObject term(ScriptRunner r) {
        PyModule m = module("term", "The computer's screen (" + Terminal.WIDTH + "x" + Terminal.HEIGHT + " characters).");
        fn(m, "write", 1, 1, "write(text) - write text at the cursor", a -> {
            r.terminal.write(a[0].toString());
            return null;
        });
        fn(m, "clear", 0, 0, "clear() - blank the screen and move the cursor home", a -> {
            r.terminal.clear();
            return null;
        });
        fn(m, "clear_line", 0, 0, "clear_line() - blank the cursor's line", a -> {
            r.terminal.clearLine();
            return null;
        });
        fn(m, "set_cursor", 2, 2, "set_cursor(x, y) - 0-based column and row", a -> {
            r.terminal.setCursor(a[0].asInt(), a[1].asInt());
            return null;
        });
        fn(m, "get_cursor", 0, 0, "get_cursor() -> (x, y)", a -> {
            int[] c = r.terminal.cursor();
            return new PyTuple(Py.newInteger(c[0]), Py.newInteger(c[1]));
        });
        fn(m, "size", 0, 0, "size() -> (width, height)", a -> new PyTuple(Py.newInteger(Terminal.WIDTH), Py.newInteger(Terminal.HEIGHT)));
        fn(m, "read", 0, 0, "read() -> a line typed by the user", a -> new PyString(r.readLine()));
        fn(m, "edit", 1, 1, "edit(name) - open a file in the editor for the person at the screen", a -> {
            String name = a[0].toString();
            if (!name.startsWith("rom/") && !ComputerFiles.isValidName(name)) throw new IllegalArgumentException("bad file name: " + name);
            String text = r.files.read(name);
            r.onServer(() -> {
                r.computer.openEditor(name, text == null ? "" : text);
                return null;
            });
            return null;
        });
        fn(m, "view_camera", 1, 1, "view_camera(id) - look through a security camera (arrow keys switch cameras)", a -> {
            String id = a[0].toString();
            return r.onServer(() -> {
                if (!r.computer.viewCameras(id)) throw new IllegalArgumentException("no camera '" + id + "' on this network");
                return null;
            });
        });
        return m;
    }

    // --- fs ---

    private static PyObject fs(ScriptRunner r) {
        PyModule m = module("fs", "This computer's files (1 MB). Names like 'notes.txt'; 'rom/...' reads built-in files.");
        fn(m, "list", 0, 1, "list() -> names of your files; list('rom') -> built-in programs", a -> {
            if (a.length > 0 && a[0].toString().equals("rom")) return toPy(Rom.programs());
            return toPy(r.files.list());
        });
        fn(m, "read", 1, 1, "read(name) -> text, or None if missing", a -> {
            String text = r.files.read(a[0].toString());
            return text == null ? Py.None : Py.newStringOrUnicode(text);
        });
        fn(m, "write", 2, 2, "write(name, text) - replace the file", a -> {
            r.files.write(a[0].toString(), a[1].toString(), false);
            return null;
        });
        fn(m, "append", 2, 2, "append(name, text) - add to the end of the file", a -> {
            r.files.write(a[0].toString(), a[1].toString(), true);
            return null;
        });
        fn(m, "delete", 1, 1, "delete(name) -> True if it existed", a -> Py.newBoolean(r.files.delete(a[0].toString())));
        fn(m, "exists", 1, 1, "exists(name) -> bool", a -> Py.newBoolean(r.files.exists(a[0].toString())));
        return m;
    }

    // --- net ---

    private static PyObject net(ScriptRunner r) {
        PyModule m = module("net", "Devices and computers connected by data cables.");
        fn(m, "devices", 0, 0, "devices() -> list of {'id', 'type', 'label'}", a -> r.onServer(() -> {
            List<Object> list = new ArrayList<>();
            for (ElectricalBlockEntity device : DataNetwork.devices(r.computer.getLevel(), r.computer.getBlockPos())) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("id", device.deviceId());
                info.put("type", device.deviceType());
                info.put("label", device.label());
                list.add(info);
            }
            return toPy(list);
        }));
        fn(m, "methods", 1, 1, "methods(id) -> names you can call on a device", a -> {
            String id = a[0].toString();
            return r.onServer(() -> toPy(new ArrayList<>(device(r, id).deviceMethods().keySet())));
        });
        fn(m, "call", 2, -1, "call(id, method, *args) -> result", a -> {
            String id = a[0].toString(), method = a[1].toString();
            List<Object> args = new ArrayList<>();
            for (PyObject arg : Arrays.copyOfRange(a, 2, a.length)) args.add(toJava(arg));
            return r.onServer(() -> {
                DeviceMethod target = device(r, id).deviceMethods().get(method);
                if (target == null) throw new IllegalArgumentException(id + " has no method '" + method + "'");
                return toPy(target.call(args));
            });
        });
        fn(m, "send", 2, 2, "send(computer_id, message) -> True if delivered", a -> {
            String id = a[0].toString(), message = a[1].toString();
            return r.onServer(() -> Py.newBoolean(r.computer.sendTo(id, message)));
        });
        fn(m, "broadcast", 1, 1, "broadcast(message) -> number of computers reached", a -> {
            String message = a[0].toString();
            return r.onServer(() -> Py.newInteger(r.computer.broadcast(message)));
        });
        fn(m, "receive", 0, 1, "receive([timeout_seconds]) -> (sender_id, message), or None on timeout", a -> {
            String[] message = r.receive(a.length > 0 ? a[0].asDouble() : -1);
            return message == null ? Py.None : new PyTuple(new PyString(message[0]), Py.newStringOrUnicode(message[1]));
        });
        return m;
    }

    private static ElectricalBlockEntity device(ScriptRunner r, String id) {
        ElectricalBlockEntity device = DataNetwork.find(r.computer.getLevel(), r.computer.getBlockPos(), id);
        if (device == null) throw new IllegalArgumentException("no device '" + id + "' on this network");
        return device;
    }

    // --- computer ---

    private static PyObject computer(ScriptRunner r) {
        PyModule m = module("computer", "This computer.");
        fn(m, "id", 0, 0, "id() -> this computer's id", a -> new PyString(r.computer.deviceId()));
        fn(m, "label", 0, 0, "label() -> this computer's name", a -> Py.newStringOrUnicode(r.computer.label()));
        fn(m, "set_label", 1, 1, "set_label(name)", a -> {
            String label = a[0].toString();
            return r.onServer(() -> {
                r.computer.setLabel(label);
                return null;
            });
        });
        fn(m, "sleep", 1, 1, "sleep(seconds) - wait (in game time)", a -> {
            r.waitTicks(Math.max(0, Math.round(a[0].asDouble() * 20)));
            return null;
        });
        fn(m, "time", 0, 0, "time() -> seconds since this computer booted", a -> new PyFloat(r.ticks() / 20.0));
        fn(m, "day_time", 0, 0, "day_time() -> time of day, 0-24000 (6:00 = 0)", a -> r.onServer(() ->
                Py.newInteger((int) (r.computer.getLevel().getDayTime() % 24000))));
        fn(m, "voltage", 0, 0, "voltage() -> supply voltage", a -> new PyFloat(r.computer.voltage()));
        fn(m, "reboot", 0, 0, "reboot()", a -> {
            r.onServer(() -> {
                r.computer.reboot();
                return null;
            });
            r.checkpoint();
            return null;
        });
        fn(m, "shutdown", 0, 0, "shutdown()", a -> {
            r.onServer(() -> {
                r.computer.shutdown();
                return null;
            });
            r.checkpoint();
            return null;
        });
        fn(m, "dinosaurs", 0, 1, "dinosaurs([radius]) -> dinosaurs within radius blocks (default 128, max 256)", a -> {
            double radius = Math.max(1, Math.min(256, a.length > 0 ? a[0].asDouble() : 128));
            return r.onServer(() -> {
                var center = r.computer.getBlockPos().getCenter();
                List<Object> list = new ArrayList<>();
                for (DinosaurEntity dino : r.computer.getLevel().getEntitiesOfClass(DinosaurEntity.class,
                        new net.minecraft.world.phys.AABB(center, center).inflate(radius))) {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("id", dino.getId());
                    info.put("species", dino.spec().species().id());
                    info.put("name", dino.hasCustomName() ? dino.getCustomName().getString() : dino.spec().species().displayName().getString());
                    info.put("health", (double) dino.getHealth());
                    info.put("max_health", (double) dino.getMaxHealth());
                    info.put("food", dino.food());
                    info.put("growth", (double) dino.growth());
                    info.put("x", dino.getBlockX());
                    info.put("y", dino.getBlockY());
                    info.put("z", dino.getBlockZ());
                    info.put("distance", Math.sqrt(dino.distanceToSqr(center)));
                    list.add(info);
                }
                return toPy(list);
            });
        });
        fn(m, "position", 0, 0, "position() -> (x, y, z) of this computer", a -> {
            var pos = r.computer.getBlockPos();
            return new PyTuple(Py.newInteger(pos.getX()), Py.newInteger(pos.getY()), Py.newInteger(pos.getZ()));
        });
        fn(m, "traceback", 0, 0, "traceback() -> text of the exception being handled", a -> {
            var exception = Py.getThreadState().exception;
            return new PyString(exception == null ? "" : exception.toString());
        });
        return m;
    }

    // --- math and random (Jython's own modules leak Java, so these are rebuilt) ---

    private static PyObject math() {
        PyModule m = module("math", "Math functions.");
        m.__setattr__("pi", new PyFloat(Math.PI));
        m.__setattr__("e", new PyFloat(Math.E));
        Object[][] unary = {{"sqrt", (DoubleUnaryOperator) Math::sqrt}, {"sin", (DoubleUnaryOperator) Math::sin},
                {"cos", (DoubleUnaryOperator) Math::cos}, {"tan", (DoubleUnaryOperator) Math::tan},
                {"asin", (DoubleUnaryOperator) Math::asin}, {"acos", (DoubleUnaryOperator) Math::acos},
                {"atan", (DoubleUnaryOperator) Math::atan}, {"exp", (DoubleUnaryOperator) Math::exp},
                {"log10", (DoubleUnaryOperator) Math::log10}, {"fabs", (DoubleUnaryOperator) Math::abs},
                {"degrees", (DoubleUnaryOperator) Math::toDegrees}, {"radians", (DoubleUnaryOperator) Math::toRadians}};
        for (Object[] f : unary) {
            DoubleUnaryOperator op = (DoubleUnaryOperator) f[1];
            fn(m, (String) f[0], 1, 1, f[0] + "(x)", a -> new PyFloat(op.applyAsDouble(a[0].asDouble())));
        }
        fn(m, "floor", 1, 1, "floor(x)", a -> new PyFloat(Math.floor(a[0].asDouble())));
        fn(m, "ceil", 1, 1, "ceil(x)", a -> new PyFloat(Math.ceil(a[0].asDouble())));
        fn(m, "pow", 2, 2, "pow(x, y)", a -> new PyFloat(Math.pow(a[0].asDouble(), a[1].asDouble())));
        fn(m, "atan2", 2, 2, "atan2(y, x)", a -> new PyFloat(Math.atan2(a[0].asDouble(), a[1].asDouble())));
        fn(m, "hypot", 2, 2, "hypot(x, y)", a -> new PyFloat(Math.hypot(a[0].asDouble(), a[1].asDouble())));
        fn(m, "log", 1, 2, "log(x[, base])", a -> new PyFloat(a.length == 1 ? Math.log(a[0].asDouble())
                : Math.log(a[0].asDouble()) / Math.log(a[1].asDouble())));
        return m;
    }

    private static PyObject random() {
        PyModule m = module("random", "Random numbers.");
        java.util.Random rng = new java.util.Random();
        fn(m, "random", 0, 0, "random() -> float in [0, 1)", a -> new PyFloat(rng.nextDouble()));
        fn(m, "randint", 2, 2, "randint(a, b) -> integer a <= n <= b", a -> {
            int lo = a[0].asInt(), hi = a[1].asInt();
            if (hi < lo) throw Py.ValueError("empty range for randint()");
            return Py.newInteger(lo + rng.nextInt(hi - lo + 1));
        });
        fn(m, "uniform", 2, 2, "uniform(a, b) -> float between a and b", a -> {
            double lo = a[0].asDouble(), hi = a[1].asDouble();
            return new PyFloat(lo + (hi - lo) * rng.nextDouble());
        });
        fn(m, "choice", 1, 1, "choice(sequence) -> random element", a -> {
            int n = a[0].__len__();
            if (n == 0) throw Py.IndexError("cannot choose from an empty sequence");
            return a[0].__getitem__(rng.nextInt(n));
        });
        return m;
    }

    // --- helpers ---

    private static PyModule module(String name, String doc) {
        PyModule m = new PyModule(name, new PyStringMap());
        m.__setattr__("__doc__", new PyString(doc));
        return m;
    }

    private static void fn(PyModule m, String name, int min, int max, String doc, ApiFunction.Body body) {
        m.__setattr__(name, new ApiFunction(name, min, max, doc, body));
    }

    static PyObject toPy(Object value) {
        if (value == null) return Py.None;
        if (value instanceof PyObject py) return py;
        if (value instanceof Boolean b) return Py.newBoolean(b);
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            long n = ((Number) value).longValue();
            return n == (int) n ? Py.newInteger((int) n) : Py.newLong(n);
        }
        if (value instanceof Number n) return new PyFloat(n.doubleValue());
        if (value instanceof String s) return Py.newStringOrUnicode(s);
        if (value instanceof List<?> list) {
            PyList out = new PyList();
            for (Object item : list) out.append(toPy(item));
            return out;
        }
        if (value instanceof Map<?, ?> map) {
            PyDictionary out = new PyDictionary();
            map.forEach((k, v) -> out.__setitem__(toPy(k), toPy(v)));
            return out;
        }
        return Py.newStringOrUnicode(value.toString());
    }

    static Object toJava(PyObject value) {
        if (value == null || value == Py.None) return null;
        if (value instanceof PyBoolean b) return b.getBooleanValue();
        if (value instanceof PyInteger || value instanceof PyLong) return value.asLong();
        if (value instanceof PyFloat f) return f.getValue();
        if (value instanceof PyString s) return s.getString();
        if (value instanceof PySequence seq && !(value instanceof PyString)) {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < seq.__len__(); i++) list.add(toJava(seq.__getitem__(i)));
            return list;
        }
        if (value instanceof PyDictionary dict) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (PyObject key : dict.keys().asIterable()) map.put(key.toString(), toJava(dict.__getitem__(key)));
            return map;
        }
        return value.toString();
    }

    private Api() {}
}
