package com.vqsv.rebuild.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ImageAssetInventory {
    private static final Pattern ORIGINAL_PATTERN = Pattern.compile("img_(\\d+)\\.mid");
    private static final Pattern DECODED_PATTERN = Pattern.compile("data__img__img_(\\d+)\\.mid\\.png");

    private final AssetPaths paths;

    public ImageAssetInventory(AssetPaths paths) {
        this.paths = paths;
    }

    public ImageAssetReport scan() {
        List<Integer> originalIds = scanIds(paths.imgOriginalDir(), ORIGINAL_PATTERN);
        List<Integer> decodedIds = scanIds(paths.imgDecodedDir(), DECODED_PATTERN);
        return new ImageAssetReport(originalIds, decodedIds);
    }

    private static List<Integer> scanIds(Path directory, Pattern pattern) {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .forEach(name -> {
                        Matcher matcher = pattern.matcher(name);
                        if (matcher.matches()) {
                            ids.add(Integer.parseInt(matcher.group(1)));
                        }
                    });
        } catch (IOException exception) {
            throw new ResourceException("Cannot scan image directory: " + directory, exception);
        }
        return ids;
    }
}
