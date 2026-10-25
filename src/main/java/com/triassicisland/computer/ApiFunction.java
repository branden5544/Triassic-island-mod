package com.triassicisland.computer;

import org.python.core.Py;
import org.python.core.PyBuiltinFunctionNarrow;
import org.python.core.PyException;
import org.python.core.PyObject;

/**
 * A Python built-in function backed by Java. To scripts it is an ordinary builtin_function_or_method:
 * no Java methods are reachable through it, and Java exceptions never leak out (they become RuntimeError).
 */
final class ApiFunction extends PyBuiltinFunctionNarrow {
    @FunctionalInterface
    interface Body {
        PyObject call(PyObject[] args) throws Exception;
    }

    private final String fname;
    private final int min, max;
    private final Body body;

    /** {@code max} of -1 means any number of arguments. */
    ApiFunction(String name, int min, int max, String doc, Body body) {
        super(name, min, max < 0 ? -1 : max, doc);
        this.fname = name;
        this.min = min;
        this.max = max;
        this.body = body;
    }

    private PyObject invoke(PyObject[] args) {
        if (args.length < min || (max >= 0 && args.length > max)) {
            String expected = min == max ? String.valueOf(min) : max < 0 ? "at least " + min : min + " to " + max;
            throw Py.TypeError(fname + "() takes " + expected + " arguments (" + args.length + " given)");
        }
        try {
            PyObject result = body.call(args);
            return result == null ? Py.None : result;
        } catch (PyException | ComputerKilled e) {
            throw e;
        } catch (Throwable t) {
            throw Py.RuntimeError(t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
        }
    }

    @Override
    public PyObject __call__(PyObject[] args, String[] keywords) {
        if (keywords.length > 0) throw Py.TypeError(fname + "() takes no keyword arguments");
        return invoke(args);
    }

    @Override
    public PyObject fancyCall(PyObject[] args) {
        return invoke(args);
    }

    @Override
    public PyObject __call__(PyObject[] args) {
        return invoke(args);
    }

    @Override
    public PyObject __call__() {
        return invoke(new PyObject[0]);
    }

    @Override
    public PyObject __call__(PyObject a) {
        return invoke(new PyObject[]{a});
    }

    @Override
    public PyObject __call__(PyObject a, PyObject b) {
        return invoke(new PyObject[]{a, b});
    }

    @Override
    public PyObject __call__(PyObject a, PyObject b, PyObject c) {
        return invoke(new PyObject[]{a, b, c});
    }

    @Override
    public PyObject __call__(PyObject a, PyObject b, PyObject c, PyObject d) {
        return invoke(new PyObject[]{a, b, c, d});
    }
}
