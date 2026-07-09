package com.vqsv.rebuild.core;

import java.net.URI;
import java.nio.file.Path;

public final class GameConfig {
    public static final int LOGICAL_WIDTH = 240;
    public static final int LOGICAL_HEIGHT = 320;
    public static final int ORIGINAL_TICK_MILLIS = 66;

    private final String title;
    private final int scale;
    private final int tickMillis;
    private final Path projectRoot;
    private final Path modulesRoot;

    private GameConfig(String title, int scale, int tickMillis, Path projectRoot, Path modulesRoot) {
        this.title = title;
        this.scale = scale;
        this.tickMillis = tickMillis;
        this.projectRoot = projectRoot;
        this.modulesRoot = modulesRoot;
    }

    public static GameConfig defaultConfig() {
        Path projectRoot = detectProjectRoot();
        String override = System.getProperty("vqsv.modules");
        Path modulesRoot = override == null || override.isBlank()
                ? projectRoot.resolve("..").resolve("modules").normalize()
                : Path.of(override).toAbsolutePath().normalize();
        return new GameConfig("VQSV Liet Hoa Rebuild", 2, ORIGINAL_TICK_MILLIS, projectRoot, modulesRoot);
    }

    private static Path detectProjectRoot() {
        try {
            URI location = GameConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Path.of(location).toAbsolutePath().normalize();
            if (path.getFileName() != null && path.getFileName().toString().endsWith(".jar")) {
                Path libsDir = path.getParent();
                if (libsDir != null && libsDir.getFileName() != null && "libs".equals(libsDir.getFileName().toString())) {
                    Path buildDir = libsDir.getParent();
                    if (buildDir != null && buildDir.getFileName() != null && "build".equals(buildDir.getFileName().toString())) {
                        return buildDir.getParent().toAbsolutePath().normalize();
                    }
                }
            }
            if (path.getFileName() != null && "classes".equals(path.getFileName().toString())) {
                Path buildDir = path.getParent();
                if (buildDir != null && buildDir.getFileName() != null && "build".equals(buildDir.getFileName().toString())) {
                    return buildDir.getParent().toAbsolutePath().normalize();
                }
            }
        } catch (Exception ignored) {
            // Fall through to the current working directory for unusual launchers.
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    public String title() {
        return title;
    }

    public int scale() {
        return scale;
    }

    public int tickMillis() {
        return tickMillis;
    }

    public Path projectRoot() {
        return projectRoot;
    }

    public Path modulesRoot() {
        return modulesRoot;
    }
}
