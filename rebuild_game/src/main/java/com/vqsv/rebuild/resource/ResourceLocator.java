package com.vqsv.rebuild.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceLocator {
    private final AssetPaths paths;

    public ResourceLocator(AssetPaths paths) {
        this.paths = paths;
    }

    public AssetPaths paths() {
        return paths;
    }

    public boolean exists(Path path) {
        return Files.isRegularFile(path);
    }

    public Path requireFile(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalized)) {
            throw new ResourceException("Missing resource file: " + normalized);
        }
        return normalized;
    }

    public InputStream open(Path path) {
        try {
            return Files.newInputStream(requireFile(path));
        } catch (IOException exception) {
            throw new ResourceException("Cannot open resource: " + path, exception);
        }
    }

    public byte[] readBytes(Path path) {
        Path file = requireFile(path);
        try {
            return Files.readAllBytes(file);
        } catch (IOException exception) {
            throw new ResourceException("Cannot read resource: " + file, exception);
        }
    }

    public String readUtf8(Path path) {
        return new String(readBytes(path), StandardCharsets.UTF_8);
    }

    public BinaryReader binary(Path path) {
        Path file = requireFile(path);
        return BinaryReader.of(readBytes(file), file);
    }
}
