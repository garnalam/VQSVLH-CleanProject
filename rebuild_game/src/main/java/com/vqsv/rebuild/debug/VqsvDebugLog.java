package com.vqsv.rebuild.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public final class VqsvDebugLog {
    private static final boolean ENABLED = Boolean.parseBoolean(
            System.getProperty("vqsv.debug.live", "true"));
    private static final Path LOG_PATH = Paths.get("build", "debug", "vqsv_live_debug.log");
    private static boolean initialized;

    private VqsvDebugLog() {
    }

    public static void log(String message) {
        if (!ENABLED) {
            return;
        }
        try {
            initialize();
            Files.writeString(LOG_PATH,
                    LocalDateTime.now() + " " + message + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Debug logging must never break gameplay.
        }
    }

    public static String pathForUser() {
        return LOG_PATH.toAbsolutePath().toString();
    }

    private static void initialize() throws IOException {
        if (initialized) {
            return;
        }
        Files.createDirectories(LOG_PATH.getParent());
        Files.writeString(LOG_PATH,
                LocalDateTime.now() + " live debug log start" + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        initialized = true;
    }
}
