package com.triassicisland.computer;

import com.triassicisland.ModConfig;
import com.triassicisland.TriassicIsland;
import net.minecraft.server.MinecraftServer;
import org.python.core.Py;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * Runs one computer's Python on its own thread. The script gets a budget of lines per game tick and then
 * waits for the next tick, so a busy loop can't lag the server. Anything touching the world is handed to the
 * server thread with {@link #onServer}. Stopping is cooperative: the line hook throws once {@link #kill} is called.
 */
public final class ScriptRunner implements Runnable {
    private static final Set<ScriptRunner> RUNNING = ConcurrentHashMap.newKeySet();

    final ComputerBlockEntity computer;
    final MinecraftServer server;
    final ComputerFiles files;
    final Terminal terminal;
    final BlockingQueue<String> input = new LinkedBlockingQueue<>();
    final ConcurrentLinkedQueue<String[]> inbox = new ConcurrentLinkedQueue<>();

    private Thread thread;
    private volatile boolean killed, terminate, finished;
    private volatile long ticks;
    private int linesLeft;

    ScriptRunner(ComputerBlockEntity computer, MinecraftServer server, ComputerFiles files) {
        this.computer = computer;
        this.server = server;
        this.files = files;
        this.terminal = computer.terminal;
    }

    void start() {
        RUNNING.add(this);
        thread = new Thread(this, "Triassic Island computer " + computer.deviceId());
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            PythonSandbox.run(this);
        } catch (ComputerKilled ignored) {
        } catch (Throwable t) {
            if (!killed) {
                TriassicIsland.LOGGER.warn("Computer {} crashed", computer.deviceId(), t);
                terminal.write("\nThe computer crashed: " + t.getClass().getSimpleName() + "\n");
            }
        } finally {
            finished = true;
            RUNNING.remove(this);
        }
    }

    /** Server thread, once per tick. */
    void serverTick() {
        ticks++;
    }

    long ticks() {
        return ticks;
    }

    void kill() {
        killed = true;
        if (thread != null) thread.interrupt();
    }

    boolean killed() {
        return killed;
    }

    void terminate() {
        terminate = true;
    }

    boolean finished() {
        return finished;
    }

    public static void killAll() {
        RUNNING.forEach(ScriptRunner::kill);
    }

    // --- Called on the script thread ---

    /** Stops the script if it was killed, or raises KeyboardInterrupt if the user pressed Terminate. */
    void checkpoint() {
        if (killed) throw new ComputerKilled();
        if (terminate) {
            terminate = false;
            input.clear();
            throw Py.KeyboardInterrupt("Terminated");
        }
    }

    /** Called for every line of Python. */
    void onLine() {
        checkpoint();
        if (--linesLeft <= 0) {
            waitTicks(1);
            linesLeft = ModConfig.LINES_PER_TICK.get();
        }
    }

    void waitTicks(long count) {
        long target = ticks + count;
        while (ticks < target) {
            checkpoint();
            LockSupport.parkNanos(1_000_000);
        }
    }

    String readLine() {
        while (true) {
            checkpoint();
            try {
                String line = input.poll(50, TimeUnit.MILLISECONDS);
                if (line != null) return line;
            } catch (InterruptedException ignored) {
            }
        }
    }

    /** Waits for a message; a negative timeout waits forever. Returns {sender, message} or null. */
    String[] receive(double timeoutSeconds) {
        long deadline = timeoutSeconds < 0 ? Long.MAX_VALUE : ticks + Math.round(timeoutSeconds * 20);
        while (true) {
            checkpoint();
            String[] message = inbox.poll();
            if (message != null) return message;
            if (ticks >= deadline) return null;
            LockSupport.parkNanos(5_000_000);
        }
    }

    /** Runs a task on the server thread and waits for its result. */
    <T> T onServer(Supplier<T> task) {
        CompletableFuture<T> future = server.submit(task);
        while (true) {
            checkpoint();
            try {
                return future.get(50, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException ignored) {
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                throw cause instanceof RuntimeException runtime ? runtime : new RuntimeException(cause.getMessage(), cause);
            }
        }
    }
}
