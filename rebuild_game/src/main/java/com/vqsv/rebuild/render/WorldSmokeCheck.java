package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public final class WorldSmokeCheck {
    private WorldSmokeCheck() {
    }

    public static List<String> run(AssetPaths paths) {
        List<String> lines = new ArrayList<>();
        MapModInfo modInfo = MapModInfo.load(paths);
        GameMap map = GameMap.load(paths, 0);
        WorldRenderer renderer = new WorldRenderer(new MapRenderer(map, TileSet.load(paths, modInfo, map.modId())));
        renderer.add(new RecordingActor("ground", 0, 100));
        renderer.add(new RecordingActor("far", 1, 80));
        renderer.add(new RecordingActor("near", 1, 40));
        renderer.add(new RecordingActor("high", 2, 10));
        renderer.tick();

        String actual = String.join("|", renderer.renderOrderLabels());
        String expected = "map:1|map:2|group2:high|group1:near|group1:far|map:3|group0:ground";
        lines.add("worldRenderOrder=" + (expected.equals(actual) ? "verified" : "FAILED")
                + " order:" + actual);
        lines.add("worldRenderOrderSpecialP2=partial:notPortedYet");
        return lines;
    }

    private static final class RecordingActor implements WorldRenderable {
        private final String label;
        private final int group;
        private final int worldY;

        private RecordingActor(String label, int group, int worldY) {
            this.label = label;
            this.group = group;
            this.worldY = worldY;
        }

        @Override
        public int group() {
            return group;
        }

        @Override
        public int worldY() {
            return worldY;
        }

        @Override
        public boolean visible() {
            return true;
        }

        @Override
        public void tick() {
        }

        @Override
        public void render(Graphics2D graphics, int cameraX, int cameraY) {
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
