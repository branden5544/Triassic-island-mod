package com.triassicisland.computer;

import java.util.Arrays;

/** The computer's screen: a grid of characters with a cursor. Written by the script thread, read by the server thread. */
public class Terminal {
    /** Size of a computer's own screen. */
    public static final int WIDTH = 51, HEIGHT = 19;

    private final int width, height;
    private final char[][] grid;
    private int cursorX, cursorY;
    private boolean dirty = true;

    public Terminal() {
        this(WIDTH, HEIGHT);
    }

    public Terminal(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        clear();
    }

    public synchronized void clear() {
        for (char[] row : grid) Arrays.fill(row, ' ');
        cursorX = cursorY = 0;
        dirty = true;
    }

    /** Writes text at the cursor, wrapping and scrolling like a console. */
    public synchronized void write(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                newLine();
            } else if (c == '\r') {
                cursorX = 0;
            } else if (c == '\t') {
                do put(' '); while (cursorX % 4 != 0);
            } else if (c >= ' ') {
                put(c);
            }
        }
        dirty = true;
    }

    private void put(char c) {
        if (cursorX >= width) newLine();
        grid[cursorY][cursorX++] = c;
    }

    private void newLine() {
        cursorX = 0;
        if (++cursorY >= height) {
            System.arraycopy(grid, 1, grid, 0, height - 1);
            grid[height - 1] = new char[width];
            Arrays.fill(grid[height - 1], ' ');
            cursorY = height - 1;
        }
    }

    public synchronized void setCursor(int x, int y) {
        cursorX = Math.max(0, Math.min(width - 1, x));
        cursorY = Math.max(0, Math.min(height - 1, y));
        dirty = true;
    }

    public synchronized int[] cursor() {
        return new int[]{cursorX, cursorY};
    }

    public synchronized void clearLine() {
        Arrays.fill(grid[cursorY], ' ');
        cursorX = 0;
        dirty = true;
    }

    /** Returns the lines if anything changed since the last call (and clears the flag), else null. */
    public synchronized String[] takeIfDirty() {
        if (!dirty) return null;
        dirty = false;
        return lines();
    }

    public synchronized String[] lines() {
        String[] lines = new String[height];
        for (int y = 0; y < height; y++) lines[y] = new String(grid[y]);
        return lines;
    }

    /** Lines, without clearing the dirty flag. */
    public synchronized String[] peekLines() {
        return lines();
    }

    public synchronized boolean isDirty() {
        return dirty;
    }

    public synchronized void markDirty() {
        dirty = true;
    }
}
