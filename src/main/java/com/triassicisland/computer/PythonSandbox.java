package com.triassicisland.computer;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFrame;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.ThreadState;
import org.python.core.TraceFunction;
import org.python.util.PythonInterpreter;

import java.io.Writer;
import java.util.Map;
import java.util.Properties;

/**
 * Sets up a locked-down Jython interpreter for one computer.
 *
 * <ul>
 *   <li>Each computer gets its own copy of the builtins, without open/file/execfile/reload/help.</li>
 *   <li>{@code import} only returns the computer's own modules (term, net, fs, computer, math, random). No Python
 *       standard library and no Java: Jython's own modules expose Java's class loader.</li>
 *   <li>A line hook enforces the per-tick budget and lets the computer stop the script at any line.</li>
 *   <li>If a Java object ever surfaces in an exception, the script is stopped.</li>
 * </ul>
 */
final class PythonSandbox {
    private static final String[] REMOVED_BUILTINS = {
            "open", "file", "execfile", "reload", "help", "copyright", "credits", "license", "exit", "quit", "__import__"};
    private static boolean initialized;

    private static synchronized void init() {
        if (initialized) return;
        Properties props = new Properties();
        props.setProperty("python.import.site", "false");
        props.setProperty("python.cachedir.skip", "true");
        props.setProperty("python.console.encoding", "UTF-8");
        PySystemState.initialize(System.getProperties(), props, new String[0], PythonSandbox.class.getClassLoader());
        initialized = true;
    }

    static void run(ScriptRunner runner) {
        init();
        PySystemState sys = new PySystemState();
        PyStringMap builtins = ((PyStringMap) sys.getBuiltins()).copy();
        for (String name : REMOVED_BUILTINS) {
            if (builtins.__finditem__(name) != null) builtins.__delitem__(name);
        }

        Map<String, PyObject> modules = Api.modules(runner);
        for (String name : new String[]{"term", "net", "fs", "computer"}) builtins.__setitem__(name, modules.get(name));
        PyObject readLine = Api.rawInput(runner);
        builtins.__setitem__("raw_input", readLine);
        builtins.__setitem__("input", readLine);
        builtins.__setitem__("__import__", new ApiFunction("__import__", 1, 5, "", args -> {
            String name = args[0].toString();
            PyObject module = modules.get(name);
            if (module == null) throw Py.ImportError("No module named " + name + " (computers have: " + String.join(", ", modules.keySet()) + ")");
            return module;
        }));
        sys.setBuiltins(builtins);

        PythonInterpreter py = new PythonInterpreter(null, sys);
        py.set("__builtins__", builtins);
        Writer out = new TerminalWriter(runner.terminal);
        py.setOut(out);
        py.setErr(out);

        ThreadState state = Py.getThreadState();
        state.tracefunc = new LineHook(runner);
        try {
            py.exec(Rom.read("bios.py"));
        } catch (PyException e) {
            if (!runner.killed() && !e.match(Py.KeyboardInterrupt)) runner.terminal.write("\n" + e + "\n");
        } finally {
            state.tracefunc = null;
            py.cleanup();
        }
    }

    private static final class LineHook extends TraceFunction {
        private final ScriptRunner runner;

        LineHook(ScriptRunner runner) {
            this.runner = runner;
        }

        @Override
        public TraceFunction traceCall(PyFrame frame) {
            return this;
        }

        @Override
        public TraceFunction traceReturn(PyFrame frame, PyObject value) {
            return this;
        }

        @Override
        public TraceFunction traceLine(PyFrame frame, int line) {
            runner.onLine();
            return this;
        }

        @Override
        public TraceFunction traceException(PyFrame frame, PyException exception) {
            if (exception.value != null && exception.value.getType() instanceof PyJavaType) {
                runner.terminal.write("\nProgram stopped: internal error.\n");
                runner.kill();
                throw new ComputerKilled();
            }
            return this;
        }
    }

    /** Python's print goes straight to the terminal. */
    private static final class TerminalWriter extends Writer {
        private final Terminal terminal;

        TerminalWriter(Terminal terminal) {
            this.terminal = terminal;
        }

        @Override
        public void write(char[] buf, int off, int len) {
            terminal.write(new String(buf, off, len));
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }

    private PythonSandbox() {}
}
