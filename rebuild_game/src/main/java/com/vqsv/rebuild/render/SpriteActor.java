package com.vqsv.rebuild.render;

import java.awt.Graphics2D;

public final class SpriteActor implements WorldRenderable {
    private final SpriteAnimator sprite;
    private final int group;
    private final int worldX;
    private final int worldY;
    private final byte direction;
    private boolean visible = true;

    public SpriteActor(SpriteAnimator sprite, int group, int worldX, int worldY, byte direction) {
        this.sprite = sprite;
        this.group = group;
        this.worldX = worldX;
        this.worldY = worldY;
        this.direction = direction;
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
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void tick() {
        sprite.tick();
    }

    @Override
    public void render(Graphics2D graphics, int cameraX, int cameraY) {
        sprite.draw(graphics, worldX - cameraX, worldY - cameraY, direction);
    }
}
