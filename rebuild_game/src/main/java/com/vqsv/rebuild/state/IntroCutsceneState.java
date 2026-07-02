package com.vqsv.rebuild.state;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.cutscene.TextCutsceneRenderer;
import com.vqsv.rebuild.input.InputSnapshot;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.render.BitmapFont;
import com.vqsv.rebuild.render.GameMap;
import com.vqsv.rebuild.render.MapModInfo;
import com.vqsv.rebuild.render.MapRenderer;
import com.vqsv.rebuild.render.SpriteActor;
import com.vqsv.rebuild.render.SpriteAnimator;
import com.vqsv.rebuild.render.TileSet;
import com.vqsv.rebuild.render.WorldRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public final class IntroCutsceneState implements GameState {
    private static final String OPENING_TEXT = "#FFFFFF Nghe đồn Thiên Địa chi sơ, vạn năm về trước có hai vị thần, "
            + "một người duy trì trật tự, một người cai quản thế giới hỗn loạn, kiềm chế lẫn nhau, duy trì cân bằng của thế giới.";
    private static final String WHITE_DRAGON_TEXT = "#FFFFFF Vi Bạch Long, vị thần đứng đầu Thiên Giới phụ trách cai quản trật tự. "
            + "Ba vị thủ hộ thánh thú lần lượt là Lôi Kỳ Lân, Tinh Vân Hạc cùng Minh Vương Long.";

    private final TextCutsceneRenderer textRenderer;
    private final WorldRenderer worldRenderer;
    private Phase phase = Phase.OPENING_TEXT;

    public IntroCutsceneState(AssetPaths assets) {
        BitmapFont font = BitmapFont.load(assets);
        this.textRenderer = new TextCutsceneRenderer(font);
        this.worldRenderer = createWorldPreview(assets);
        startOpeningText();
    }

    @Override
    public void tick(InputSnapshot input, GameStateMachine states) {
        long nowMillis = System.currentTimeMillis();
        textRenderer.tick(nowMillis);
        if (phase == Phase.UPPER_SCENE) {
            worldRenderer.tick();
        }
        if (phase == Phase.OPENING_TEXT && input.confirmPressed() && textRenderer.canConfirm()) {
            textRenderer.acknowledge();
            startUpperSceneText();
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        if (phase == Phase.UPPER_SCENE) {
            renderUpperScene(graphics);
        }
        textRenderer.render(graphics);
    }

    private void startOpeningText() {
        phase = Phase.OPENING_TEXT;
        textRenderer.setPosition(30, 90);
        textRenderer.setMode0(OPENING_TEXT, 0);
        textRenderer.setWaitForConfirm(true);
    }

    private void startUpperSceneText() {
        phase = Phase.UPPER_SCENE;
        textRenderer.setPosition(10, 270);
        textRenderer.setMode0(WHITE_DRAGON_TEXT, 0);
        textRenderer.setBox(220, 50);
    }

    private void renderUpperScene(Graphics2D graphics) {
        Shape oldClip = graphics.getClip();
        graphics.setClip(0, 0, GameConfig.LOGICAL_WIDTH, 270);
        try {
            worldRenderer.render(graphics);
        } finally {
            graphics.setClip(oldClip);
        }
        graphics.setColor(new Color(0x202020));
        graphics.drawLine(0, 269, GameConfig.LOGICAL_WIDTH, 269);
    }

    private static WorldRenderer createWorldPreview(AssetPaths assets) {
        MapModInfo modInfo = MapModInfo.load(assets);
        GameMap map = GameMap.load(assets, 0);
        MapRenderer mapRenderer = new MapRenderer(map, TileSet.load(assets, modInfo, map.modId()));
        mapRenderer.centerCameraOn(map.widthPixels() / 2, map.heightPixels() / 2);
        WorldRenderer renderer = new WorldRenderer(mapRenderer);
        SpriteAnimator sampleSprite = SpriteAnimator.load(assets, 0, false);
        renderer.add(new SpriteActor(sampleSprite, 1, map.widthPixels() - 142, map.heightPixels() - 76, (byte) 0));
        return renderer;
    }

    private enum Phase {
        OPENING_TEXT,
        UPPER_SCENE
    }
}
