package com.vqsv.rebuild.render;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WorldRenderer {
    private final MapRenderer mapRenderer;
    private final List<WorldRenderable> groundActors = new ArrayList<>();
    private final List<WorldRenderable> sortedActors = new ArrayList<>();
    private final List<WorldRenderable> highActors = new ArrayList<>();

    public WorldRenderer(MapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
    }

    public MapRenderer mapRenderer() {
        return mapRenderer;
    }

    public void add(WorldRenderable actor) {
        switch (actor.group()) {
            case 0:
                groundActors.add(actor);
                break;
            case 1:
                sortedActors.add(actor);
                break;
            case 2:
                highActors.add(actor);
                break;
            default:
                throw new IllegalArgumentException("Unsupported world render group: " + actor.group());
        }
    }

    public void tick() {
        tickGroup(groundActors);
        sortedActors.sort(Comparator.comparingInt(WorldRenderable::worldY));
        tickGroup(sortedActors);
        tickGroup(highActors);
    }

    public void render(Graphics2D graphics) {
        renderMapLayer(graphics, 1);
        renderMapLayer(graphics, 2);
        renderGroup(graphics, highActors);
        renderGroup(graphics, sortedActors);
        renderMapLayer(graphics, 3);
        renderGroup(graphics, groundActors);
    }

    public List<String> renderOrderLabels() {
        List<String> labels = new ArrayList<>();
        appendMapLayerLabel(labels, 1);
        appendMapLayerLabel(labels, 2);
        appendActorLabels(labels, "group2", highActors);
        appendActorLabels(labels, "group1", sortedActors);
        appendMapLayerLabel(labels, 3);
        appendActorLabels(labels, "group0", groundActors);
        return labels;
    }

    private void renderMapLayer(Graphics2D graphics, int layerIndex) {
        if (mapRenderer.hasLayer(layerIndex)) {
            mapRenderer.renderLayer(graphics, layerIndex);
        }
    }

    private void appendMapLayerLabel(List<String> labels, int layerIndex) {
        if (mapRenderer.hasLayer(layerIndex)) {
            labels.add("map:" + layerIndex);
        }
    }

    private void appendActorLabels(List<String> labels, String prefix, List<WorldRenderable> actors) {
        for (WorldRenderable actor : actors) {
            if (actor.visible()) {
                labels.add(prefix + ":" + actor);
            }
        }
    }

    private void tickGroup(List<WorldRenderable> actors) {
        for (WorldRenderable actor : actors) {
            actor.tick();
        }
    }

    private void renderGroup(Graphics2D graphics, List<WorldRenderable> actors) {
        for (WorldRenderable actor : actors) {
            if (actor.visible()) {
                actor.render(graphics, mapRenderer.cameraX(), mapRenderer.cameraY());
            }
        }
    }
}
