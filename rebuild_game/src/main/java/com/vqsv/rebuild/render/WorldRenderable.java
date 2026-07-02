package com.vqsv.rebuild.render;

import java.awt.Graphics2D;

public interface WorldRenderable {
    int group();

    int worldY();

    boolean visible();

    void tick();

    void render(Graphics2D graphics, int cameraX, int cameraY);
}
