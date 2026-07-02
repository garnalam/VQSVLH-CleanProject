package com.vqsv.rebuild.state;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.input.InputSnapshot;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.ImageAssetInventory;
import com.vqsv.rebuild.resource.ImageAssetReport;
import com.vqsv.rebuild.resource.ImageLoader;
import com.vqsv.rebuild.render.BitmapFont;
import com.vqsv.rebuild.render.GameMap;
import com.vqsv.rebuild.render.MapModInfo;
import com.vqsv.rebuild.render.MapRenderer;
import com.vqsv.rebuild.render.SpriteActor;
import com.vqsv.rebuild.render.SpriteAnimator;
import com.vqsv.rebuild.render.TileSet;
import com.vqsv.rebuild.render.WorldRenderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

public final class BootState implements GameState {
    private final AssetPaths assets;
    private final ImageAssetReport imageReport;
    private final BufferedImage sampleImage;
    private final BitmapFont bitmapFont;
    private final WorldRenderer worldRenderer;
    private int ticks;

    public BootState(AssetPaths assets) {
        this.assets = assets;
        this.imageReport = new ImageAssetInventory(assets).scan();
        Optional<BufferedImage> image = new ImageLoader(assets).findDecodedImage(0);
        this.sampleImage = image.orElse(null);
        this.bitmapFont = BitmapFont.load(assets);
        SpriteAnimator sampleSprite = SpriteAnimator.load(assets, 0, false);
        MapModInfo modInfo = MapModInfo.load(assets);
        GameMap map = GameMap.load(assets, 0);
        MapRenderer mapRenderer = new MapRenderer(map, TileSet.load(assets, modInfo, map.modId()));
        mapRenderer.centerCameraOn(map.widthPixels() / 2, map.heightPixels() / 2);
        this.worldRenderer = new WorldRenderer(mapRenderer);
        this.worldRenderer.add(new SpriteActor(sampleSprite, 1, map.widthPixels() - 142, map.heightPixels() - 76, (byte) 0));
    }

    @Override
    public void tick(InputSnapshot input, GameStateMachine states) {
        ticks++;
        worldRenderer.tick();
        if (input.confirmPressed()) {
            ticks = 0;
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        worldRenderer.render(graphics);

        graphics.setColor(new Color(18, 20, 24, 210));
        graphics.fillRect(0, 0, GameConfig.LOGICAL_WIDTH, 236);

        graphics.setColor(new Color(236, 238, 240));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        graphics.drawString("VQSV Rebuild", 16, 34);

        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        graphics.setColor(new Color(180, 210, 160));
        graphics.drawString("Skeleton runtime is alive.", 16, 58);

        graphics.setColor(assets.modulesRootExists() ? new Color(120, 220, 140) : new Color(240, 110, 100));
        graphics.drawString("modules: " + assets.modulesRoot(), 16, 82);

        graphics.setColor(assets.hasUi("world.ui") ? new Color(120, 220, 140) : new Color(240, 110, 100));
        graphics.drawString("ui/world.ui: " + (assets.hasUi("world.ui") ? "found" : "missing"), 16, 106);

        graphics.setColor(assets.hasUi("dialog.ui") ? new Color(120, 220, 140) : new Color(240, 110, 100));
        graphics.drawString("font.bin: " + (assets.fontBin().toFile().isFile() ? "found" : "missing"), 16, 130);

        graphics.setColor(new Color(200, 200, 200));
        graphics.drawString("logical screen: 240x320", 16, 154);
        graphics.drawString("img decoded/orig: " + imageReport.decodedCount() + "/" + imageReport.originalCount(), 16, 178);
        graphics.drawString("press 0 / Enter / Space", 16, 202);
        graphics.drawString("ticks: " + ticks, 16, 226);

        if (sampleImage != null) {
            graphics.setColor(new Color(70, 76, 84));
            graphics.drawRect(15, 242, 50, 50);
            graphics.drawImage(sampleImage, 16, 243, null);
            graphics.setColor(new Color(200, 200, 200));
            graphics.drawString("img_0 " + sampleImage.getWidth() + "x" + sampleImage.getHeight(), 72, 270);
        }

        graphics.setColor(Color.WHITE);
        bitmapFont.drawString(graphics, "Bitmap font OK 0123", 16, 306);
    }
}
