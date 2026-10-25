package com.triassicisland.computer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Read-only files shipped with the mod: the BIOS, the shell and the pre-made programs. */
public final class Rom {
    private static final String ROOT = "/data/triassicisland/computer/rom/";

    /** Contents of a ROM file, or null if it doesn't exist. */
    public static String read(String path) {
        if (!path.matches("[A-Za-z0-9_./-]+") || path.contains("..")) return null;
        try (InputStream in = Rom.class.getResourceAsStream(ROOT + path)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /** Names of the pre-made programs, without ".py". */
    public static List<String> programs() {
        List<String> names = new ArrayList<>();
        String index = read("programs/index.txt");
        if (index != null) {
            for (String line : index.split("\n")) {
                if (!line.isBlank()) names.add(line.trim());
            }
        }
        return names;
    }

    private Rom() {}
}
