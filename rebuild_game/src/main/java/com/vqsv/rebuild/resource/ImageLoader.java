package com.vqsv.rebuild.resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ImageLoader {
    private final AssetPaths paths;
    private final ResourceLocator locator;
    private final Map<Path, BufferedImage> pathCache = new HashMap<>();
    private final Map<Integer, BufferedImage> decodedIdCache = new HashMap<>();

    public ImageLoader(AssetPaths paths) {
        this.paths = paths;
        this.locator = new ResourceLocator(paths);
    }

    public boolean hasDecodedImage(int id) {
        return locator.exists(paths.imgDecodedPng(id));
    }

    public boolean hasOriginalImage(int id) {
        return locator.exists(paths.imgOriginal(id));
    }

    public Optional<BufferedImage> findDecodedImage(int id) {
        if (!hasDecodedImage(id)) {
            return Optional.empty();
        }
        return Optional.of(loadDecodedImage(id));
    }

    public BufferedImage loadDecodedImage(int id) {
        BufferedImage cached = decodedIdCache.get(id);
        if (cached != null) {
            return cached;
        }
        BufferedImage image = load(paths.imgDecodedPng(id));
        decodedIdCache.put(id, image);
        return image;
    }

    public BufferedImage loadOriginalImage(int id) {
        return load(paths.imgOriginal(id));
    }

    public BufferedImage load(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        BufferedImage cached = pathCache.get(normalized);
        if (cached != null) {
            return cached;
        }
        try (InputStream stream = locator.open(normalized)) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new ResourceException("Unsupported or invalid image resource: " + normalized);
            }
            pathCache.put(normalized, image);
            return image;
        } catch (IOException exception) {
            throw new ResourceException("Cannot decode image: " + normalized, exception);
        }
    }

    public int cachedPathCount() {
        return pathCache.size();
    }

    public int cachedDecodedIdCount() {
        return decodedIdCache.size();
    }
}
