package com.vqsv.rebuild.resource;

import com.vqsv.rebuild.cutscene.TextCutsceneSmokeCheck;
import com.vqsv.rebuild.render.BitmapFontSmokeCheck;
import com.vqsv.rebuild.render.MapSmokeCheck;
import com.vqsv.rebuild.render.SpriteSmokeCheck;
import com.vqsv.rebuild.render.WorldSmokeCheck;
import com.vqsv.rebuild.state.BootFlowSmokeCheck;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ResourceSmokeCheck {
    private final AssetPaths paths;
    private final ResourceLocator locator;

    public ResourceSmokeCheck(AssetPaths paths) {
        this.paths = paths;
        this.locator = new ResourceLocator(paths);
    }

    public List<String> run() {
        List<String> lines = new ArrayList<>();
        lines.add(BinaryReaderSelfTest.run());
        lines.add("modulesRoot=" + paths.modulesRoot());
        lines.add("modulesRootExists=" + paths.modulesRootExists());
        checkImages(lines);
        lines.addAll(BitmapFontSmokeCheck.run(paths));
        lines.addAll(SpriteSmokeCheck.run(paths));
        lines.addAll(MapSmokeCheck.run(paths));
        lines.addAll(WorldSmokeCheck.run(paths));
        lines.addAll(TextCutsceneSmokeCheck.run(paths));
        lines.addAll(BootFlowSmokeCheck.run(paths));
        checkFile(lines, "font.bin", paths.fontBin(), true);
        checkFile(lines, "custom boot logo", paths.logoCustomPng("vqsvlogo"), false);
        checkFile(lines, "img_0 decoded png", paths.imgDecodedPng(0), false);
        checkFile(lines, "spr_0", paths.sprOriginal(0), true);
        checkFile(lines, "map_0", paths.mapOriginal(0), true);
        checkFile(lines, "mod_0", paths.modOriginal(0), true);
        checkFile(lines, "world.ui", paths.uiOriginal("world.ui"), true);
        checkFile(lines, "scene_0", paths.eventOriginalMid(0), true);
        checkFile(lines, "scene_13.mib", paths.eventOriginalMib(13), true);
        return lines;
    }

    private void checkImages(List<String> lines) {
        ImageAssetReport report = new ImageAssetInventory(paths).scan();
        lines.add(report.summary());
        lines.add(report.missingSummary(12));

        ImageLoader loader = new ImageLoader(paths);
        if (loader.hasDecodedImage(0)) {
            java.awt.image.BufferedImage first = loader.loadDecodedImage(0);
            java.awt.image.BufferedImage second = loader.loadDecodedImage(0);
            lines.add("img_0 dimensions=" + first.getWidth() + "x" + first.getHeight()
                    + " cacheSameInstance=" + (first == second)
                    + " pathCache=" + loader.cachedPathCount()
                    + " idCache=" + loader.cachedDecodedIdCount());
        }
        if (loader.hasOriginalImage(0)) {
            java.awt.image.BufferedImage original = loader.loadOriginalImage(0);
            lines.add("img_0 originalDimensions=" + original.getWidth() + "x" + original.getHeight()
                    + " pathCache=" + loader.cachedPathCount());
        }
    }

    private void checkFile(List<String> lines, String label, Path path, boolean binaryProbe) {
        if (!locator.exists(path)) {
            lines.add(label + "=missing " + path);
            return;
        }
        BinaryReader reader = locator.binary(path);
        StringBuilder builder = new StringBuilder();
        builder.append(label)
                .append("=found bytes=")
                .append(reader.length())
                .append(" path=")
                .append(path);
        if (binaryProbe && reader.length() > 0) {
            builder.append(" firstU8=").append(reader.readUnsignedByte());
        }
        lines.add(builder.toString());
    }
}
