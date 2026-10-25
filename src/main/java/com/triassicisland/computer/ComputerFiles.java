package com.triassicisland.computer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A computer's own files, stored in the world save. Flat (no folders), plain names only, 1 MB total.
 * Paths starting with "rom/" read the mod's built-in files and are read-only.
 */
public class ComputerFiles {
    public static final long MAX_TOTAL_BYTES = 1 << 20;
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_.-]{1,40}");

    private final Path dir;

    public ComputerFiles(Path dir) {
        this.dir = dir;
    }

    public static boolean isValidName(String name) {
        return NAME.matcher(name).matches() && !name.startsWith(".");
    }

    private Path file(String name) {
        if (!isValidName(name)) throw new IllegalArgumentException("bad file name: " + name);
        return dir.resolve(name);
    }

    public List<String> list() throws IOException {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(dir)) return names;
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(Files::isRegularFile).forEach(f -> names.add(f.getFileName().toString()));
        }
        names.sort(null);
        return names;
    }

    /** Returns the file's text, or null if it doesn't exist. */
    public String read(String name) throws IOException {
        if (name.startsWith("rom/")) return Rom.read(name.substring(4));
        Path file = file(name);
        return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : null;
    }

    public boolean exists(String name) {
        if (name.startsWith("rom/")) return Rom.read(name.substring(4)) != null;
        return isValidName(name) && Files.isRegularFile(dir.resolve(name));
    }

    public void write(String name, String text, boolean append) throws IOException {
        if (name.startsWith("rom/")) throw new IOException("rom files are read-only");
        Path file = file(name);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        long existing = Files.isRegularFile(file) ? Files.size(file) : 0;
        long newSize = append ? existing + bytes.length : bytes.length;
        if (totalSize() - existing + newSize > MAX_TOTAL_BYTES) throw new IOException("disk full (1 MB limit)");
        Files.createDirectories(dir);
        if (append) {
            Files.write(file, bytes, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } else {
            Files.write(file, bytes);
        }
    }

    public boolean delete(String name) throws IOException {
        if (name.startsWith("rom/")) throw new IOException("rom files are read-only");
        return Files.deleteIfExists(file(name));
    }

    private long totalSize() throws IOException {
        long total = 0;
        for (String name : list()) total += Files.size(dir.resolve(name));
        return total;
    }
}
