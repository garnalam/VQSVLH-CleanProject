import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class VqsvBattleRenderer {
    private static final int W = 240;
    private static final int H = 320;
    private static final int ENEMY_RECT_X = 132;
    private static final int ENEMY_RECT_Y = 70;
    private static final int ENEMY_RECT_W = 96;
    private static final int ENEMY_RECT_H = 118;
    private static final int PLAYER_RECT_X = 18;
    private static final int PLAYER_RECT_Y = 140;
    private static final int PLAYER_RECT_W = 96;
    private static final int PLAYER_RECT_H = 95;
    private static final int JAVA_ME_EFFECT_TRANSPARENT_KEY = 0x00ffffff;
    private static final Color SOURCE_UI_TEXT = new Color(0x1c6c91);
    private static final Map<Integer, BufferedImage> TEX_CACHE = new HashMap<>();

    private VqsvBattleRenderer() {
    }

    static void render(VqsvIntroDemo.Scene s, Graphics2D g) {
        if (s.battleOverlayTicks <= 0) {
            return;
        }
        renderSourceLikeBattleUi(s, g);
    }

    static void renderSourceLikeBattleUi(VqsvIntroDemo.Scene s, Graphics2D g) {
        if ("npcEnemy".equals(s.battleUiMode) || s.battleNpcEnemyEntryVisible) {
            drawNpcEnemyEntryOverlay(g, s.font, s);
            return;
        }
        boolean petstateMode = "petstate".equals(s.battleUiMode);
        boolean levelUpMode = "levelup".equals(s.battleUiMode)
                && (s.battleLevelUpView == null || s.battleLevelUpView.leveled);
        boolean fullOverlayMode = petstateMode || levelUpMode;
        drawBattleBackground(g, s);
        if (!fullOverlayMode) {
            drawBattleUiCellTopLeft(g, 92, 0, 0);
            drawBattleUiCellTopLeft(g, 93, 0, 235);
            drawBattleUiCellTopLeft(g, 158, 101, 1);
        }
        drawBattleGroundMarkers(g, s);
        if (s.battleLVisible && !s.battleLDrawAfter) {
            drawState1LEffect(g, s);
        }
        if (!s.battleEnemyHiddenByCatch && !s.battleP7BaseHiddenEnemySide) {
            drawBattleSpriteAtSource(g, s.battleEnemyVisualId,
                    sourceBattleActorX(s, false) + enemyOffsetX(s),
                    sourceBattleActorY(s, false) + enemyOffsetY(s), sourceBattleOrientation(false),
                    s.battleP7BaseStateEnemySide,
                    baseCursor(s.battleEnemyVisualId, s.battleP7BaseStateEnemySide,
                            s.battleP7BaseCursorEnemySide, s.battleAnimationTick));
        }
        if (!s.battleP7BaseHiddenPlayerSide) {
            drawBattleSpriteAtSource(g, s.battlePlayerVisualId,
                    sourceBattleActorX(s, true) + playerOffsetX(s),
                    sourceBattleActorY(s, true) + playerOffsetY(s), sourceBattleOrientation(true),
                    s.battleP7BaseStatePlayerSide,
                    baseCursor(s.battlePlayerVisualId, s.battleP7BaseStatePlayerSide,
                            s.battleP7BaseCursorPlayerSide, s.battleAnimationTick));
        }
        if (s.battleLVisible && s.battleLDrawAfter) {
            drawState1LEffect(g, s);
        }
        drawP7ActorEffect(g, s);
        drawP7DeathEffect(g, s);
        drawP7SpecialEffect(g, s);

        if (!fullOverlayMode) {
            drawBattleUiCellTopLeft(g, 101, 97, 14);
        }
        if ("command".equals(s.battleUiMode)) {
            drawBattleCommandBar(g, s.font, s.battleCommandIndex);
        }

        if (!fullOverlayMode) {
            drawBattleHudWidgets(g, s.font, s);
        }
        if ("shopbuy".equals(s.battleUiMode) || "shopconfirm".equals(s.battleUiMode)) {
            drawShopBuyOverlay(g, s.font, s);
            if ("shopconfirm".equals(s.battleUiMode)) {
                drawShopConfirmOverlay(g, s.font, s);
            }
        } else if ("choice".equals(s.battleUiMode)) {
            drawChoiceOverlay(g, s.font, s);
        } else if ("choiceskill".equals(s.battleUiMode)) {
            drawChoiceSkillOverlay(g, s.font, s);
        } else if ("petstate".equals(s.battleUiMode)) {
            renderPetStateOverlay(g, s.font, s, true);
        } else if ("target".equals(s.battleUiMode)) {
            drawTargetCursor(g, s.font, s);
        } else if ("smsinfo".equals(s.battleUiMode)) {
            drawSmsInfoOverlay(g, s.font, s);
        } else if ("warning".equals(s.battleUiMode) && !hasSourceTextBox(s, TextBox.SOURCE_MSGWARM)) {
            drawWarningOverlay(g, s.font, s);
        } else if ("levelup".equals(s.battleUiMode)) {
            drawLevelUpOverlay(g, s.font, s);
        }
        if (s.battleCatchVisible && s.battleCatchSpriteId >= 0) {
            drawCatchAnimation(g, s);
        }
        if (s.battleP7DamageVisible) {
            drawP7Damage(g, s.font, s);
        }
        if (s.battleP7PostEffectVisible) {
            drawP7PostEffect(g, s.font, s);
        }
    }

    private static boolean hasSourceTextBox(VqsvIntroDemo.Scene s, int sourceUiKind) {
        return s.text != null && s.text.sourceUiKind == sourceUiKind;
    }

    private static void drawBattleBackground(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (s.battleBackgroundSnapshot != null) {
            g.drawImage(s.battleBackgroundSnapshot, 0, 0, null);
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRect(0, 0, W, H);
            return;
        }
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);
    }

    private static void clearBattleBackgroundRegion(Graphics2D g, VqsvIntroDemo.Scene s,
                                                    int x, int y, int w, int h) {
        Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        drawBattleBackground(g, s);
        g.setClip(oldClip);
    }

    private static void drawBattleUiCellTopLeft(Graphics2D g, int cellId, int x, int y) {
        SpriteAnim ui = SpriteAnim.load(257);
        int[] bounds = ui.cellBounds(cellId);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return;
        }
        ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }

    private static void drawSpriteCellTopLeft(Graphics2D g, int spriteIndex, int cellId, int x, int y) {
        SpriteAnim sprite = SpriteAnim.load(spriteIndex);
        int[] bounds = sprite.cellBounds(cellId);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return;
        }
        sprite.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }

    private static void drawBattleUiStateTopLeft(Graphics2D g, int state, int x, int y) {
        SpriteAnim ui = SpriteAnim.load(257);
        ui.setState(state);
        int[] bounds = ui.animationBounds(state);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return;
        }
        ui.draw(g, x - bounds[0], y - bounds[1], 0);
    }

    private static void drawBattleGroundMarkers(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (!s.battleGroundMarkersVisible) {
            return;
        }
        SpriteAnim marker = SpriteAnim.load(294);
        marker.setState(0);
        marker.cursor = 0;
        drawSourceMarker(g, marker, s, false, false);
        drawSourceMarker(g, marker, s, true, false);
        if (!s.battleActiveMarkerVisible) {
            return;
        }
        marker.setState(1);
        marker.cursor = Math.max(0, (s.battleAnimationTick / 2) % 2);
        drawSourceMarker(g, marker, s, s.battleActiveMarkerPlayerSide, true);
    }

    private static void drawSourceMarker(Graphics2D g, SpriteAnim marker, VqsvIntroDemo.Scene s,
                                         boolean playerSide, boolean activeMarker) {
        int x = sourceBattleMarkerX(s, playerSide) + sideOffsetX(s, playerSide);
        int y = sourceBattleMarkerY(s, playerSide) + sideOffsetY(s, playerSide);
        if (activeMarker) {
            x = (playerSide ? s.battlePlayerMarkerX : s.battleEnemyMarkerX)
                    + sideOffsetX(s, playerSide);
            y = (playerSide ? s.battlePlayerMarkerY : s.battleEnemyMarkerY)
                    + sideOffsetY(s, playerSide);
        }
        marker.draw(g, x, y, 0);
    }

    private static int sourceBattleActorX(VqsvIntroDemo.Scene s, boolean playerSide) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(s));
        int at = sourcePosQuadOffset(s, playerSide);
        if (row.length >= at + 4) {
            return row[at];
        }
        return playerSide ? 70 : 177;
    }

    private static int sourceBattleActorY(VqsvIntroDemo.Scene s, boolean playerSide) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(s));
        int at = sourcePosQuadOffset(s, playerSide);
        if (row.length >= at + 4) {
            return row[at + 1];
        }
        return playerSide ? 223 : 103;
    }

    private static int sourceBattleMarkerX(VqsvIntroDemo.Scene s, boolean playerSide) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(s));
        int at = sourcePosQuadOffset(s, playerSide);
        if (row.length >= at + 4) {
            return row[at + 2];
        }
        return playerSide ? 36 : 144;
    }

    private static int sourceBattleMarkerY(VqsvIntroDemo.Scene s, boolean playerSide) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(s));
        int at = sourcePosQuadOffset(s, playerSide);
        if (row.length >= at + 4) {
            return row[at + 3];
        }
        return playerSide ? 206 : 85;
    }

    private static int sourceP8ExpAnchorX(VqsvIntroDemo.Scene s) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(s));
        int at = sourcePosQuadOffset(s, true);
        if (row.length >= at + 2) {
            return row[at];
        }
        return 70;
    }

    private static int sourceP8ExpAnchorY(VqsvIntroDemo.Scene s) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup(s));
        int at = sourcePosQuadOffset(s, true);
        if (row.length >= at + 2) {
            return row[at + 1];
        }
        return 223;
    }

    private static int sourcePosQuadOffset(VqsvIntroDemo.Scene s, boolean playerSide) {
        if (sourceCposGroup(s) == 1) {
            return playerSide ? 8 : 0;
        }
        return playerSide ? 4 : 0;
    }

    private static int sourceCposGroup(VqsvIntroDemo.Scene s) {
        return s.battleMode == 0 ? (s.battleBackgroundMode == 1 ? 2 : 0) : 1;
    }

    private static int sourceBattleOrientation(boolean playerSide) {
        return playerSide ? 0 : 1;
    }

    private static int battleSpriteAnchorX(VqsvIntroDemo.Scene s, boolean playerSide) {
        return sourceBattleActorX(s, playerSide) + sideOffsetX(s, playerSide);
    }

    private static int battleSpriteAnchorY(VqsvIntroDemo.Scene s, boolean playerSide) {
        return sourceBattleActorY(s, playerSide) + sideOffsetY(s, playerSide);
    }

    private static int battleSlotAnchorX(VqsvIntroDemo.Scene s, boolean playerSide) {
        return sourceBattleActorX(s, playerSide);
    }

    private static int battleSlotAnchorY(VqsvIntroDemo.Scene s, boolean playerSide) {
        return sourceBattleActorY(s, playerSide);
    }

    private static int battleSpriteAnchor(int spriteIndex, int state, int rectPos, int rectSize, boolean xAxis) {
        if (spriteIndex < 0) {
            return rectPos;
        }
        SpriteAnim anim = SpriteAnim.load(spriteIndex);
        int[] bounds = anim.animationBounds(Math.max(0, state));
        if (bounds == null) {
            return rectPos;
        }
        int offset = xAxis ? bounds[0] : bounds[1];
        int size = xAxis ? bounds[2] : bounds[3];
        if (xAxis) {
            return rectPos + (rectSize - size) / 2 - offset;
        }
        return rectPos + (rectSize - size) - offset;
    }

    private static void drawBattleSprite(Graphics2D g, int spriteIndex, int x, int y, int w, int h, int align, int orientation) {
        drawBattleSprite(g, spriteIndex, x, y, w, h, align, orientation, 0, 0);
    }

    private static void drawBattleSprite(Graphics2D g, int spriteIndex, int x, int y, int w, int h,
                                         int align, int orientation, int state, int cursor) {
        if (spriteIndex < 0) {
            return;
        }
        SpriteAnim anim = SpriteAnim.load(spriteIndex);
        anim.setState(Math.max(0, state));
        int frames = anim.data.anim == null || anim.data.anim.length == 0
                ? 0 : anim.data.anim[anim.state].length / 2;
        anim.cursor = Math.max(0, Math.min(cursor, Math.max(0, frames - 1)));
        anim.drawAligned(g, x, y, w, h, align, orientation);
    }

    private static void drawBattleSpriteAtSource(Graphics2D g, int spriteIndex, int x, int y,
                                                 int orientation, int state, int cursor) {
        if (spriteIndex < 0) {
            return;
        }
        SpriteAnim anim = SpriteAnim.load(spriteIndex);
        anim.setState(Math.max(0, state));
        int frames = anim.data.anim == null || anim.data.anim.length == 0
                ? 0 : anim.data.anim[anim.state].length / 2;
        anim.cursor = Math.max(0, Math.min(cursor, Math.max(0, frames - 1)));
        anim.draw(g, x, y, orientation);
    }

    private static void drawBattlePanel(Graphics2D g, int x, int y, int w, int h, boolean fill) {
        if (fill) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(x, y, w, h);
        }
        g.setColor(new Color(232, 244, 255));
        g.drawRect(x, y, w - 1, h - 1);
        g.setColor(new Color(52, 88, 105));
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
    }

    private static void drawBattleCommandBar(Graphics2D g, FontBitmap font, int selected) {
        String[][] labels = VqsvText.Battle.COMMAND_LABELS;
        int[] textXs = {7, 48, 88, 128, 168, 208};
        int[] iconXs = {20, 56, 98, 137, 176, 218};
        if (selected >= 0 && selected < iconXs.length) {
            drawBattleUiCellTopLeft(g, 31, iconXs[selected], 293);
        }
        for (int i = 0; i < labels.length; i++) {
            Color color = i == selected ? new Color(0xfff16a) : Color.WHITE;
            drawTinyBattleText(g, font, labels[i][0], textXs[i], 299, 34, color);
            drawTinyBattleText(g, font, labels[i][1], textXs[i], 309, 34, color);
        }
    }

    private static void drawBattleHudWidgets(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("battle.ui");

        drawBattleUiText(g, font, layout, 15, s.battleEnemyName, Color.WHITE, s.battleAnimationTick);
        drawBattleUiText(g, font, layout, 16, "lv" + s.battleEnemyLevel, Color.WHITE, s.battleAnimationTick);
        drawSourcePercentLayer(g, layout, 42, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp), 8);
        drawSourcePercentLayer(g, layout, 56, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp), 8);
        drawSourcePercentLayer(g, layout, 14, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp), 8);
        drawBattleUiText(g, font, layout, 39, s.battleEnemyHp + "/" + s.battleEnemyMaxHp,
                new Color(0xfff9b1), s.battleAnimationTick);
        drawBattleUiCellTopLeft(g, 94 + Math.max(0, s.battleEnemyElement),
                layout.x(18, 92), layout.y(18, 2));
        if (s.battleEnemyOwnedSpecies) {
            drawBattleUiCellTopLeft(g, 101, layout.x(19, 97), layout.y(19, 14));
        } else {
            clearBattleBackgroundRegion(g, s, layout.x(19, 97) - 1, layout.y(19, 14) - 1, 15, 15);
        }
        drawBattleUiText(g, font, layout, 58, s.battleEnemyPowerPercent + "%",
                battlePercentColor(s.battleEnemyPowerPercent), s.battleAnimationTick);
        drawStatusSlots(g, layout.x(32, 2), layout.y(32, 25),
                layout.x(49, 10), layout.y(49, 30), false,
                s.battleEnemyStatusIconCells, s.battleEnemyStatusDurationCells);

        VqsvBattleLevelUpView p8ExpView = normalP8ExpView(s);
        String playerName = p8ExpView == null ? s.battlePlayerName : p8ExpView.name;
        int playerLevel = p8ExpView == null ? s.battlePlayerLevel : p8ExpView.level;
        int playerSecondaryValue = p8ExpView == null ? s.battlePlayerEnergy : p8ExpView.expValue;
        int playerSecondaryMax = p8ExpView == null ? s.battlePlayerMaxEnergy : p8ExpView.expMax;
        int playerSecondaryPercent = p8ExpView == null
                ? hpPercent(s.battlePlayerEnergy, s.battlePlayerMaxEnergy) : p8ExpView.expPercent;
        int playerElement = p8ExpView == null ? s.battlePlayerElement : p8ExpView.elementId;
        drawBattleUiText(g, font, layout, 12, playerName, Color.WHITE, s.battleAnimationTick);
        drawBattleUiText(g, font, layout, 13, "lv" + playerLevel, Color.WHITE, s.battleAnimationTick);
        drawSourcePercentLayer(g, layout, 41, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp), 8);
        drawSourcePercentLayer(g, layout, 55, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp), 8);
        drawSourcePercentLayer(g, layout, 11, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp), 8);
        drawBattleUiText(g, font, layout, 38, s.battlePlayerHp + "/" + s.battlePlayerMaxHp,
                new Color(0xfff9b1), s.battleAnimationTick);
        drawSourcePercentLayer(g, layout, 9, playerSecondaryPercent, 8);
        drawBattleUiText(g, font, layout, 40, playerSecondaryValue + "/" + playerSecondaryMax,
                new Color(0xfff9b1), s.battleAnimationTick);
        drawBattleUiCellTopLeft(g, 94 + Math.max(0, playerElement),
                layout.x(17, 139), layout.y(17, 249));
        drawBattleUiText(g, font, layout, 59, s.battlePlayerPowerPercent + "%",
                battlePercentColor(s.battlePlayerPowerPercent), s.battleAnimationTick);
        drawStatusSlots(g, layout.x(26, 226), layout.y(26, 221),
                layout.x(43, 234), layout.y(43, 226), true,
                s.battlePlayerStatusIconCells, s.battlePlayerStatusDurationCells);

        if (Boolean.getBoolean("vqsv.battle.debugLog.visible")) {
            drawBattleUiText(g, font, layout, 10, s.battleLog, Color.WHITE, s.battleAnimationTick);
        }
        if (s.battleCaptureTutorial) {
            drawBattleUiText(g, font, layout, 4, VqsvText.Battle.CAPTURE_LABEL,
                    new Color(0xfff9b1), s.battleAnimationTick);
        }
    }

    private static VqsvBattleLevelUpView normalP8ExpView(VqsvIntroDemo.Scene s) {
        VqsvBattleLevelUpView view = s.battleLevelUpView == null
                ? VqsvBattleLevelUpView.EMPTY : s.battleLevelUpView;
        if ("levelup".equals(s.battleUiMode) && view.visible && !view.leveled) {
            return view;
        }
        return null;
    }

    private static void drawBattleUiText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                         int widgetId, String text, Color fallback, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        drawSourceWidgetText(g, font, text, widget.x, widget.y,
                Math.max(1, widget.w), sourceWidgetHeight(widget),
                widgetTextColor(widget, false, fallback), tick, widget.b);
    }

    private static void drawSourcePercentLayer(Graphics2D g, VqsvUiLayout layout,
                                               int widgetId, int percent, int fallbackHeight) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int height = sourceWidgetHeight(widget);
        if (height <= 0) {
            height = fallbackHeight;
        }
        int width = Math.max(1, widget.w);
        fillSourceColor(g, widget.jColor, widget.x, widget.y, width, height);
        drawSourceRect(g, widget.kColor, widget.x, widget.y, width, height);
        int fill = Math.max(0, Math.min(width, percent * width / 100));
        if (fill > 1 && height > 1) {
            fillSourceColor(g, widget.lColor, widget.x + 1, widget.y + 1, fill - 1, height - 1);
        }
    }

    private static void fillSourceColor(Graphics2D g, int argb, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || (argb >> 24) == 0) {
            return;
        }
        g.setColor(new Color(argb, true));
        g.fillRect(x, y, w, h);
    }

    private static void drawSourceRect(Graphics2D g, int argb, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || (argb >> 24) == 0) {
            return;
        }
        g.setColor(new Color(argb, true));
        g.drawRect(x, y, w, h);
    }

    private static Color battlePercentColor(int percent) {
        return percent > 100 ? new Color(0xfff1a0) : percent < 100 ? new Color(0xb8d8ff) : Color.WHITE;
    }

    private static void drawShopBuyOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("shopbuy.ui");
        drawSourceWidgetFill(g, layout, 4, 202, 0x89d8ef);
        drawSourceWidgetFill(g, layout, 3, 8, 0xc6f1ff);
        drawSourceWidgetFill(g, layout, 1, 160, 0xbde4ef);
        drawSourceWidgetFill(g, layout, 2, 11, 0x82d0fb);
        drawSourceWidgetFill(g, layout, 8, 90, 0xbde4ef);
        drawSourceWidgetCell(g, layout, 4, false, false);
        drawShopWidgetText(g, font, layout, 5, "Mua", 100, SOURCE_UI_TEXT, s.battleAnimationTick);
        drawShopWidgetText(g, font, layout, 9, "V\u1eadt ph\u1ea9m", 62, SOURCE_UI_TEXT, s.battleAnimationTick);
        drawShopWidgetText(g, font, layout, 10, "Gi\u00e1 b\u00e1n", 46, SOURCE_UI_TEXT, s.battleAnimationTick);

        int start = Math.max(0, Math.min(s.battleMenuScroll, Math.max(0, s.battleMenuNames.length - 5)));
        int visible = Math.min(5, Math.max(0, s.battleMenuNames.length - start));
        for (int rowIndex = 0; rowIndex < visible; rowIndex++) {
            int index = start + rowIndex;
            int itemId = s.battleMenuIds[index];
            BattleItemRow row = VqsvBattleTables.instance().item(itemId);
            boolean selected = index == s.battleMenuIndex;
            int frameWidget = 12 + rowIndex * 5;
            int nameWidget = 14 + rowIndex * 5;
            int valueWidget = 15 + rowIndex * 5;
            int currencyWidget = 45 + rowIndex;
            int iconWidget = 51 + rowIndex;
            drawSourceWidgetCell(g, layout, frameWidget, selected, true);
            if (row != null) {
                drawSpriteCellTopLeft(g, VqsvChoiceUiView.ROW_ICON_SPRITE_ID, row.iconId,
                        layout.x(iconWidget, 56), layout.y(iconWidget, 100 + rowIndex * 18));
            }
            Color rowColor = selected ? new Color(0xfff16a) : SOURCE_UI_TEXT;
            drawChoiceText(g, font, layout, nameWidget, index < s.battleMenuNames.length
                    ? s.battleMenuNames[index] : "", rowColor, s.battleAnimationTick);
            drawChoiceText(g, font, layout, valueWidget, index < s.battleMenuValues.length
                    ? s.battleMenuValues[index] : "", rowColor, s.battleAnimationTick);
            drawBattleUiCellTopLeft(g, shopCurrencyCell(row == null ? 0 : row.currencyOrType),
                    layout.x(currencyWidget, 170), layout.y(currencyWidget, 101 + rowIndex * 18));
        }

        if (s.battleMenuNames.length > 5) {
            drawSourceWidgetFill(g, layout, 38, 84, 0x51d8e9);
            int trackY = layout.y(38, 102);
            int trackH = 84;
            int knobH = 8;
            int maxScroll = Math.max(1, s.battleMenuNames.length - 5);
            int scroll = Math.max(0, Math.min(s.battleMenuScroll, maxScroll));
            int knobY = trackY + (trackH - knobH) * scroll / maxScroll;
            drawSourceUiFill(g, layout.x(38, 185), knobY, layout.w(38, 4), knobH, 0xc6f3ff);
        }

        int selectedItemId = s.battleMenuIds.length == 0 ? -1
                : s.battleMenuIds[Math.max(0, Math.min(s.battleMenuIndex, s.battleMenuIds.length - 1))];
        BattleItemRow selectedRow = VqsvBattleTables.instance().item(selectedItemId);
        String description = selectedRow == null ? "" : selectedRow.description("");
        drawSourceWidgetCell(g, layout, 36, false, false);
        drawMarqueeTinyBattleText(g, font, description,
                layout.x(56, 60), layout.y(56, 195), layout.w(56, 125),
                SOURCE_UI_TEXT, s.battleAnimationTick);

        drawSourceWidgetCell(g, layout, 41, false, false);
        drawSourceWidgetCell(g, layout, 42, false, false);
        drawChoiceText(g, font, layout, 43, String.valueOf(s.sourceBadges),
                SOURCE_UI_TEXT, s.battleAnimationTick);
        drawChoiceText(g, font, layout, 44, String.valueOf(s.sourceMoney),
                SOURCE_UI_TEXT, s.battleAnimationTick);
        drawSourceWidgetCell(g, layout, 39, false, false);
        drawSourceWidgetCell(g, layout, 40, false, false);
        drawShopSoftKeyText(g, font, layout, 39, "Mua", s.battleAnimationTick);
        drawShopSoftKeyText(g, font, layout, 40, "Ph\u1ea3n", s.battleAnimationTick);
    }

    private static void drawShopConfirmOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgyn.ui");
        drawSourceWidgetFill(g, layout, 1, 96, 0xc6f3ff);
        drawSourceWidgetFill(g, layout, 2, 7, 0xc6f3ff);
        drawSourceWidgetFill(g, layout, 3, 79, 0xbef0f2);
        drawSourceWidgetFill(g, layout, 4, 10, 0x82d0fb);
        drawSourceWidgetFill(g, layout, 5, 72, 0x51d8e9);
        drawSourceWidgetCell(g, layout, 1, false, false);
        drawShopWidgetText(g, font, layout, 8, "S\u1ed1 l\u01b0\u1ee3ng", 48, SOURCE_UI_TEXT, s.battleAnimationTick);
        drawChoiceText(g, font, layout, 9, String.valueOf(s.battleShopConfirmQuantity),
                SOURCE_UI_TEXT, s.battleAnimationTick);
        drawShopWidgetText(g, font, layout, 10, "Ti\u00eau hao", 44, SOURCE_UI_TEXT, s.battleAnimationTick);
        drawChoiceText(g, font, layout, 11, String.valueOf(s.battleShopConfirmTotal),
                SOURCE_UI_TEXT, s.battleAnimationTick);
        drawBattleUiCellTopLeft(g, shopCurrencyCell(s.battleShopConfirmCurrency),
                layout.x(12, 142), layout.y(12, 142));
        drawSourceWidgetCell(g, layout, 13, false, false);
        drawSourceWidgetCell(g, layout, 14, false, false);
        drawSourceWidgetCell(g, layout, 15, false, false);
        drawMsgynOptionText(g, font, layout, 6, "X\u00e1c nh\u1eadn", s.battleAnimationTick);
        drawMsgynOptionText(g, font, layout, 7, "Kh\u00f4ng", s.battleAnimationTick);
    }

    private static int shopCurrencyCell(int currency) {
        if (currency == 1) {
            return 83;
        }
        if (currency == 2) {
            return 74;
        }
        return 84;
    }

    private static void drawShopWidgetText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                           int widgetId, String text, int width,
                                           Color fallback, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        drawSourceWidgetText(g, font, text, widget.x, widget.y,
                Math.max(width, Math.max(1, widget.w)), sourceWidgetHeight(widget),
                widgetTextColor(widget, false, fallback), tick, widget.b);
    }

    private static void drawShopSoftKeyText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                            int widgetId, String text, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int width = widgetId == 39
                ? Math.max(1, layout.x(41, 81) - widget.x - 2)
                : Math.max(1, W - widget.x - 43);
        drawSourceWidgetText(g, font, text, widget.x, widget.y,
                width, sourceWidgetHeight(widget),
                widgetTextColor(widget, false, SOURCE_UI_TEXT), tick, widget.b);
    }

    private static void drawMsgynOptionText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                            int widgetId, String text, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int x = layout.x(5, 77);
        int width = Math.max(1, layout.x(15, 128) - x - 8);
        drawSourceWidgetText(g, font, text, x, widget.y,
                width, sourceWidgetHeight(widget),
                widgetTextColor(widget, false, SOURCE_UI_TEXT), tick, 4);
    }

    private static void drawChoiceOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("choice.ui");
        VqsvChoiceUiView choice = s.battleChoiceUi == null || s.battleChoiceUi == VqsvChoiceUiView.EMPTY
                ? VqsvChoiceUiView.fromScene(s)
                : s.battleChoiceUi;
        if (choice.selectedIndex != s.battleMenuIndex || choice.scroll != s.battleMenuScroll) {
            choice = choice.withViewportScroll(s.battleMenuIndex, s.battleMenuScroll);
        }
        drawChoiceStaticWidgets(g, font, layout, choice, s.battleAnimationTick);
        int start = choice.visibleStart();
        int visibleRows = choice.visibleCount();
        for (int row = 0; row < visibleRows; row++) {
            int i = start + row;
            boolean selected = i == choice.selectedIndex;
            int frameId = 11 + row * 5;
            int iconId = 54 + row;
            int nameId = 13 + row * 5;
            int valueId = 14 + row * 5;
            if (choice.widgetVisible(frameId)) {
                drawSourceWidgetCell(g, layout, frameId, selected, true);
            }
            Color color = selected
                    ? widgetTextColor(layout.widget(nameId), true, new Color(0xfff16a))
                    : widgetTextColor(layout.widget(nameId), false, SOURCE_UI_TEXT);
            if (choice.widgetVisible(iconId) && choice.rowIconVisible(row)) {
                drawSpriteCellTopLeft(g, VqsvChoiceUiView.ROW_ICON_SPRITE_ID, choice.rowIconCell(row),
                        layout.x(iconId, 54), layout.y(iconId, 95 + row * 15));
            }
            if (choice.widgetVisible(nameId)) {
                drawSourceWidgetText(g, font, choice.widgetText(nameId, ""),
                        layout.x(nameId, 77), layout.y(nameId, 97 + row * 15),
                        layout.w(nameId, 72), sourceWidgetHeight(layout.widget(nameId)),
                        color, s.battleAnimationTick, layout.widget(nameId) == null ? 3 : layout.widget(nameId).b);
            }
            if (choice.widgetVisible(valueId)) {
                VqsvUiLayout.UiWidget valueWidget = layout.widget(valueId);
                drawSourceWidgetText(g, font, choice.widgetText(valueId, ""),
                        layout.x(valueId, 141), layout.y(valueId, 97 + row * 15),
                        layout.w(valueId, 36), sourceWidgetHeight(valueWidget),
                        widgetTextColor(valueWidget, selected, color), s.battleAnimationTick,
                        valueWidget == null ? 4 : valueWidget.b);
            }
        }
        if (choice.size() > 5) {
            VqsvUiLayout.UiWidget track = layout.widget(50);
            drawSourceWidgetFill(g, layout, 50, 72, 0x51d8e9);
            int trackY = track == null ? 98 : track.y;
            int trackH = 72;
            int knobY = choice.scrollbarThumbY(trackY, trackH);
            VqsvUiLayout.UiWidget knob = layout.widget(51);
            drawSourceUiFill(g, layout.x(51, 183), knobY, layout.w(51, 4), 8,
                    widgetFillColor(knob, false, new Color(0xc6f3ff)).getRGB() & 0xffffff);
        }
        if (choice.size() == 0) {
            drawTinyBattleText(g, font, "...", 105, 136, 40, SOURCE_UI_TEXT);
        }
        if (choice.widgetVisible(52) && choice.isCatchMenu()) {
            drawSourceWidgetCell(g, layout, 52, false, false);
            drawChoiceDescription(g, font, layout, selectedCatchCountText(s, choice), s.battleAnimationTick);
        } else {
            String description = choice.selectedDescription();
            if (choice.widgetVisible(52) && !description.isEmpty()) {
                drawSourceWidgetCell(g, layout, 52, false, false);
                drawChoiceDescription(g, font, layout, description, s.battleAnimationTick);
            }
        }
        if (choice.widgetVisible(5)) {
            drawChoiceText(g, font, layout, 5, choice.widgetText(5, layout.text(5, "")),
                    SOURCE_UI_TEXT, s.battleAnimationTick);
        }
        if (choice.widgetVisible(6)) {
            drawChoiceText(g, font, layout, 6, choice.widgetText(6, layout.text(6, "Quay l\u1ea1i")),
                    SOURCE_UI_TEXT, s.battleAnimationTick);
        }
        if (choice.widgetVisible(59)) {
            drawSourceWidgetCell(g, layout, 59, false, false);
            drawChoiceText(g, font, layout, 59, choice.widgetText(59, layout.text(59, "")),
                    SOURCE_UI_TEXT, s.battleAnimationTick);
        }
        if (choice.widgetVisible(60)) {
            drawSourceWidgetCell(g, layout, 60, false, false);
            drawChoiceText(g, font, layout, 60, choice.widgetText(60, layout.text(60, "Quay l\u1ea1i")),
                    SOURCE_UI_TEXT, s.battleAnimationTick);
        }
    }

    private static void drawChoiceStaticWidgets(Graphics2D g, FontBitmap font,
                                                VqsvUiLayout layout, VqsvChoiceUiView choice, int tick) {
        drawSourceWidgetFill(g, layout, 4, 8, 0xc6f1ff);
        drawSourceWidgetFill(g, layout, 2, 160, 0xbde4ef);
        drawSourceWidgetFill(g, layout, 3, 14, 0x82cafb);
        drawSourceWidgetCell(g, layout, 1, false, false);
        drawSourceWidgetFill(g, layout, 7, 82, 0xbde4ef);
        if (choice.widgetVisible(8)) {
            drawChoiceText(g, font, layout, 8, choice.widgetText(8, ""), SOURCE_UI_TEXT, tick);
        }
        if (choice.widgetVisible(9)) {
            drawChoiceText(g, font, layout, 9, choice.widgetText(9, ""), SOURCE_UI_TEXT, tick);
        }
    }

    private static void drawChoiceText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                       int widgetId, String text, Color fallback, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        drawSourceWidgetText(g, font, text,
                layout.x(widgetId, 0), layout.y(widgetId, 0), layout.w(widgetId, 1),
                sourceWidgetHeight(widget), widgetTextColor(widget, false, fallback),
                tick, widget == null ? 0 : widget.b);
    }

    private static void drawChoiceDescription(Graphics2D g, FontBitmap font,
                                              VqsvUiLayout layout, String text, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(53);
        drawSourceWidgetText(g, font, text,
                layout.x(53, 57), layout.y(53, 180), layout.w(53, 125),
                sourceWidgetHeight(widget), Color.WHITE, tick, widget == null ? 0 : widget.b);
    }

    private static String selectedCatchCountText(VqsvIntroDemo.Scene s, VqsvChoiceUiView choice) {
        if (choice.size() == 0) {
            return "S\u1ed1 l\u01b0\u1ee3ng: 0 c\u00e1i ";
        }
        BagItem item = s.sourceBagItems.get(choice.idAt(choice.selectedIndex));
        int count = item == null ? 0 : item.count;
        return "S\u1ed1 l\u01b0\u1ee3ng: " + count + " c\u00e1i ";
    }

    private static void drawSourceWidgetFill(Graphics2D g, VqsvUiLayout layout,
                                             int widgetId, int fallbackHeight, int rgb) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget != null) {
            drawSourceUiFill(g, widget.x, widget.y, Math.max(1, widget.w),
                    layout.bandHeight(widgetId, fallbackHeight),
                    widgetFillColor(widget, false, new Color(rgb)).getRGB() & 0xffffff);
        }
    }

    private static void drawSourceWidgetCell(Graphics2D g, VqsvUiLayout layout, int widgetId,
                                             boolean selected, boolean selectedUsesAlt) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int imageId;
        int imageMode;
        if (selected) {
            imageId = selectedUsesAlt ? widget.altId : widget.imageId;
            imageMode = selectedUsesAlt ? widget.altMode : widget.imageMode;
        } else {
            imageId = selectedUsesAlt ? widget.imageId : widget.altId;
            imageMode = selectedUsesAlt ? widget.imageMode : widget.altMode;
        }
        if (imageId < 0) {
            imageId = widget.imageId >= 0 ? widget.imageId : widget.altId;
            imageMode = widget.imageId >= 0 ? widget.imageMode : widget.altMode;
        }
        if (imageId < 0) {
            return;
        }
        if (imageMode == 3) {
            drawBattleUiStateTopLeft(g, imageId, widget.x, widget.y);
        } else {
            drawBattleUiCellTopLeft(g, imageId, widget.x, widget.y);
        }
    }

    static void renderPetStateOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s, boolean battleMode) {
        VqsvUiLayout layout = VqsvUiLayout.load("petstate.ui");
        drawPetStateStaticWidgets(g, font, layout, s);
        drawPetStateArrows(g, layout, s);
        for (int i = 0; i < 6; i++) {
            drawPetStateRow(g, font, layout, s, i);
        }
        if (s.battleMenuNames.length == 0) {
            drawTinyBattleText(g, font, "...", layout.x(48, 105), 136, 40, SOURCE_UI_TEXT);
        }
        drawPetStateDetails(g, font, layout, s);
        if (battleMode) {
            int tick = petstateUiTick(s);
            String action = s.battleMenuAction == null || s.battleMenuAction.isEmpty()
                    ? layout.text(75, VqsvText.Battle.PETSTATE_DEPLOY)
                    : s.battleMenuAction;
            drawPetStateText(g, font, layout, 75, action, 54, SOURCE_UI_TEXT, tick);
            drawPetStateText(g, font, layout, 76, layout.text(76, VqsvText.Battle.PETSTATE_BACK),
                    42, SOURCE_UI_TEXT, tick);
        }
    }

    static void renderEvolutionOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        SourceEvolutionNotice notice = s.sourceEvolveNotice;
        drawSourceUiFill(g, 43, 55, 158, 202, 0x89d8ef);
        drawSourceUiFill(g, 46, 78, 150, 9, 0xc6f3ff);
        drawSourceUiFill(g, 46, 87, 150, 159, 0xf7ffff);
        drawSourceUiFill(g, 46, 246, 150, 11, 0x82d0fb);
        drawBattleUiCellTopLeft(g, 1, 43, 55);
        drawBattleUiCellTopLeft(g, 27, 0, 264);
        drawBattleUiCellTopLeft(g, 8, 78, 94);
        drawBattleUiCellTopLeft(g, 15, 59, 176);
        drawBattleUiCellTopLeft(g, 16, 59, 189);
        drawBattleUiCellTopLeft(g, 16, 59, 202);
        drawBattleUiCellTopLeft(g, 16, 59, 215);
        drawBattleUiCellTopLeft(g, 15, 139, 176);
        drawBattleUiCellTopLeft(g, 16, 139, 189);
        drawBattleUiCellTopLeft(g, 16, 139, 202);
        drawBattleUiCellTopLeft(g, 16, 139, 215);
        drawBattleUiCellTopLeft(g, 23, 52, 223);

        String title = notice != null && notice.targetKind == 3
                ? VqsvText.Evolution.MUTATE : VqsvText.Evolution.EVOLVE;
        drawCenteredTinyText(g, font, title, 70, 60, 100, Color.WHITE);
        drawTinyBattleText(g, font, "X\u00e1c", 5, 296, 34, Color.WHITE);
        drawTinyBattleText(g, font, "\u0111\u1ecbnh", 5, 306, 34, Color.WHITE);
        drawTinyBattleText(g, font, "Ph\u1ea3n", 201, 296, 34, Color.WHITE);
        drawTinyBattleText(g, font, "h\u1ed3i", 201, 306, 34, Color.WHITE);

        int sprite = s.sourceEvolveOldVisualId;
        if (sprite >= 0) {
            Shape oldClip = g.getClip();
            g.clipRect(78, 90, 90, 88);
            drawBattleSprite(g, sprite, 78, 90, 90, 88, 7, 0);
            g.setClip(oldClip);
        }
        if (s.sourceEvolvePhase == 1) {
            drawAhType10SourceGuard(g, 78, 90, 90, 88, s.sourceEvolveEffectTicks);
        }

        String name = notice == null ? currentPetName(s) : currentName(notice);
        drawMarqueeTinyBattleText(g, font, name, 53, 84, 72, SOURCE_UI_TEXT, s.battleAnimationTick);
        int level = s.sourceEvolvePetIndex >= 0 && s.sourceEvolvePetIndex < s.sourcePets.size()
                ? s.sourcePets.get(s.sourceEvolvePetIndex).level : 0;
        drawTinyBattleText(g, font, "lv", 150, 82, 12, SOURCE_UI_TEXT);
        drawTinyBattleText(g, font, String.valueOf(level), 165, 82, 24, SOURCE_UI_TEXT);

        String[] labels = {"M\u1ec7nh", "C\u00f4ng", "Ph\u00f2ng", "M\u1eabn"};
        int[] y = {170, 184, 197, 210};
        for (int i = 0; i < 4; i++) {
            drawMarqueeTinyBattleText(g, font, labels[i], 65, y[i], 12, SOURCE_UI_TEXT, s.battleAnimationTick);
            drawTinyBattleText(g, font, String.valueOf(statAt(s.sourceEvolveOldStats, i)), 80, y[i], 24, SOURCE_UI_TEXT);
            if (notice != null) {
                drawMarqueeTinyBattleText(g, font, labels[i], 145, y[i], 12, SOURCE_UI_TEXT, s.battleAnimationTick);
                drawTinyBattleText(g, font, String.valueOf(statAt(s.sourceEvolveNewStats, i)), 160, y[i], 24, SOURCE_UI_TEXT);
            }
        }

        if (notice != null) {
            drawMarqueeTinyBattleText(g, font, "T\u00e0i li\u1ec7u c\u1ea7n thi\u1ebft", 56, 226, 48,
                    SOURCE_UI_TEXT, s.battleAnimationTick);
            String materialName = VqsvSourceEvolutionRuntime.materialName(notice);
            drawMarqueeTinyBattleText(g, font, materialName, 114, 241, 48, SOURCE_UI_TEXT, s.battleAnimationTick);
            String count = VqsvSourceEvolutionRuntime.materialCount(s, notice.materialId)
                    + "/" + notice.materialNeed;
            drawTinyBattleText(g, font, count, 164, 241, 24, SOURCE_UI_TEXT);
        }
    }

    private static void drawAhType10SourceGuard(Graphics2D g, int x, int y, int w, int h, int tick) {
        // Source h.bh creates ah row [0,0,10,0,0,oldVisual,0,0,newVisual,0,0].
        // In ah type 10, the overlay copy is l.b(..., sArray[6]); sArray[6] is 0 here,
        // so Java ME drawRGB receives an alpha-zero overlay. Keep the old widget visible
        // during the wait and avoid drawing a fabricated effect.
        // No extra drawing here until the exact alpha transform is reproduced.
    }

    private static int statAt(int[] stats, int index) {
        return stats == null || index < 0 || index >= stats.length ? 0 : stats[index];
    }

    private static String currentName(SourceEvolutionNotice notice) {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(notice.currentSpeciesId);
        return row == null ? "Pet " + notice.currentSpeciesId : row.name("Pet " + notice.currentSpeciesId);
    }

    private static String targetName(SourceEvolutionNotice notice) {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(notice.targetSpeciesId);
        return row == null ? "Pet " + notice.targetSpeciesId : row.name("Pet " + notice.targetSpeciesId);
    }

    private static String currentPetName(VqsvIntroDemo.Scene s) {
        if (s.sourceEvolvePetIndex < 0 || s.sourceEvolvePetIndex >= s.sourcePets.size()) {
            return "";
        }
        int speciesId = s.sourcePets.get(s.sourceEvolvePetIndex).speciesId;
        BattleSpeciesRow row = VqsvBattleTables.instance().species(speciesId);
        return row == null ? "Pet " + speciesId : row.name("Pet " + speciesId);
    }

    private static void drawPetStateStaticWidgets(Graphics2D g, FontBitmap font,
                                                  VqsvUiLayout layout, VqsvIntroDemo.Scene s) {
        drawSourceUiFill(g, layout.x(1, 43), layout.y(1, 55), layout.w(1, 158), 197, 0xbde4ef);
        drawPetStateWidgetCell(g, layout, 1, false);
        drawPetStateColorBand(g, layout, 3, 8, 0xc6f1ff);
        drawPetStateColorBand(g, layout, 4, 160, 0xbde4ef);
        drawPetStateColorBand(g, layout, 5, 13, 0x82cafb);
        drawPetStateWidgetCell(g, layout, 7, false);
        drawPetStateWidgetCell(g, layout, 8, false);
        drawPetStateWidgetCell(g, layout, 9, false);
        drawPetStateWidgetCell(g, layout, 10, false);
        drawPetStateWidgetCell(g, layout, 11, false);
        drawPetStateWidgetCell(g, layout, 12, false);
        drawTinyBattleText(g, font, layout.text(2, VqsvText.Battle.PETSTATE_TITLE),
                layout.x(2, 70), layout.y(2, 58), layout.w(2, 100),
                widgetTextColor(layout.widget(2), false, Color.WHITE));
    }

    private static void drawPetStateColorBand(Graphics2D g, VqsvUiLayout layout,
                                              int widgetId, int fallbackHeight, int rgb) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget != null) {
            drawSourceUiFill(g, widget.x, widget.y, widget.w, layout.bandHeight(widgetId, fallbackHeight),
                    widgetColor(widget, false, rgb, false));
        }
    }

    private static void drawPetStateWidgetCell(Graphics2D g, VqsvUiLayout layout, int widgetId) {
        drawPetStateWidgetCell(g, layout, widgetId, false);
    }

    private static void drawPetStateWidgetCell(Graphics2D g, VqsvUiLayout layout, int widgetId, boolean selected) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int imageId = selected && widget.imageId >= 0 ? widget.imageId : widget.altId;
        int imageMode = selected && widget.imageId >= 0 ? widget.imageMode : widget.altMode;
        if (imageId < 0 && widget.imageId >= 0) {
            imageId = widget.imageId;
            imageMode = widget.imageMode;
        }
        if (imageId < 0) {
            return;
        }
        if (imageMode == 3) {
            drawBattleUiStateTopLeft(g, imageId, widget.x, widget.y);
        } else {
            drawBattleUiCellTopLeft(g, imageId, widget.x, widget.y);
        }
    }

    private static void drawPetStateArrows(Graphics2D g, VqsvUiLayout layout, VqsvIntroDemo.Scene s) {
        if (s.battleMenuIndex > 0) {
            drawPetStateWidgetCell(g, layout, 49);
        }
        if (s.battleMenuIndex < s.battleMenuNames.length - 1) {
            drawPetStateWidgetCell(g, layout, 50);
        }
    }

    private static void drawPetStateRow(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                        VqsvIntroDemo.Scene s, int row) {
        int frameId = 6 + row * 6;
        int iconId = 15 + row * 6;
        int numberId = 14 + row * 6;
        int hpId = 16 + row * 6;
        int expId = 17 + row * 6;
        VqsvUiLayout.UiWidget frame = layout.widget(frameId);
        int y = frame == null ? 86 + row * 15 : frame.y;
        int menuIndex = petStateMenuIndexForRow(s, row);
        boolean selected = menuIndex == s.battleMenuIndex;
        if (frame != null) {
            drawPetStateWidgetCell(g, layout, frameId, selected);
        }
        VqsvBattlePetStateView view = petStateViewAt(s, row);
        if (view == null || !view.visible) {
            drawPetStateGauge(g, layout, hpId, selected, 0, new Color(0xff7f5f), new Color(0xd3efd2));
            drawPetStateGauge(g, layout, expId, selected, 0, new Color(0x6ba8ff), new Color(0xc6d7f8));
            return;
        }
        drawPetStateWidgetCell(g, layout, iconId, selected);
        Color number = view.alive
                ? widgetTextColor(layout.widget(numberId), selected, selected ? new Color(0xfff16a) : new Color(0xaf5d2e))
                : new Color(0x777777);
        drawTinyBattleText(g, font, String.valueOf(Math.max(0, menuIndex) + 1),
                layout.x(numberId, 47), layout.y(numberId, y + 2), layout.w(numberId, 10), number);
        drawPetStateGauge(g, layout, hpId, selected, view.hpPercent,
                new Color(0xff7f5f), new Color(0xd3efd2));
        drawPetStateGauge(g, layout, expId, selected, view.expPercent,
                new Color(0x6ba8ff), new Color(0xc6d7f8));
    }

    private static void drawPetStateDetails(Graphics2D g, FontBitmap font, VqsvUiLayout layout, VqsvIntroDemo.Scene s) {
        VqsvBattlePetStateView view = selectedPetStateView(s);
        if (view == null || !view.visible) {
            return;
        }
        drawPetQualityStars(g, layout, view);
        if (view.visualId >= 0) {
            Shape oldClip = g.getClip();
            int x = layout.x(48, 105);
            int y = layout.y(48, 85);
            int w = layout.w(48, 90);
            g.clipRect(x, y, w, 88);
            drawBattleSprite(g, view.visualId, x, y, w, 88, 7, 0);
            g.setClip(oldClip);
        }
        int tick = petstateUiTick(s);
        drawPetStateText(g, font, layout, 51, view.name, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 52, view.elementName, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 53, "T\u01b0\u01a1ng kh\u1eafc", 44, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 54, VqsvText.Battle.PETSTATE_CARRYING, 38, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 62, view.evolutionText, 48, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 61, view.relationText, 46, SOURCE_UI_TEXT, tick);
        // Source has widget 59 for the held-item icon, but the rebuild detail panel keeps this row text-only.
        drawPetStateText(g, font, layout, 60, view.heldItemName, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 55, "lv", SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 56, VqsvText.Battle.PETSTATE_ATTACK, 20, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 57, VqsvText.Battle.PETSTATE_DEFENSE, 20, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 58, VqsvText.Battle.PETSTATE_SPEED, 20, SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 65, String.valueOf(view.level), SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 66, String.valueOf(view.attack), SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 67, String.valueOf(view.defense), SOURCE_UI_TEXT, tick);
        drawPetStateText(g, font, layout, 68, String.valueOf(view.speed), SOURCE_UI_TEXT, tick);
    }

    private static VqsvBattlePetStateView selectedPetStateView(VqsvIntroDemo.Scene s) {
        if (s.battleMenuIndex >= 0 && s.battleMenuIndex < s.battleMenuIds.length) {
            int petIndex = s.battleMenuIds[s.battleMenuIndex];
            if (petIndex >= 0 && petIndex < s.sourcePets.size()) {
                int rowIndex = Math.max(0, Math.min(5, s.battleMenuIndex - s.battleMenuScroll));
                return VqsvBattlePetStateView.fromPet(rowIndex, petIndex,
                        s.sourcePets.get(petIndex), s.sourcePets.get(petIndex).sourceK());
            }
        }
        return petStateViewAt(s, Math.max(0, s.battleMenuIndex - s.battleMenuScroll));
    }

    private static VqsvBattlePetStateView petStateViewAt(VqsvIntroDemo.Scene s, int row) {
        int menuIndex = petStateMenuIndexForRow(s, row);
        if (menuIndex >= 0 && menuIndex < s.battleMenuIds.length) {
            int petIndex = s.battleMenuIds[menuIndex];
            if (petIndex >= 0 && petIndex < s.sourcePets.size()) {
                return VqsvBattlePetStateView.fromPet(row, petIndex,
                        s.sourcePets.get(petIndex), s.sourcePets.get(petIndex).sourceK());
            }
        }
        if (s.battlePetStateRows == null || row < 0 || row >= s.battlePetStateRows.length) {
            return null;
        }
        return s.battlePetStateRows[row];
    }

    private static int petStateMenuIndexForRow(VqsvIntroDemo.Scene s, int row) {
        if (row < 0 || row >= 6) {
            return -1;
        }
        int maxScroll = Math.max(0, s.battleMenuIds.length - 6);
        int start = Math.max(0, Math.min(s.battleMenuScroll, maxScroll));
        return start + row;
    }

    private static void drawPetStateGauge(Graphics2D g, int x, int y, int w, int h, int percent,
                                          Color fill, Color empty) {
        g.setColor(empty);
        g.fillRect(x, y, w, h);
        g.setColor(fill);
        int sourceFill = Math.max(0, Math.min(w, w * Math.max(0, Math.min(100, percent)) / 100));
        if (sourceFill > 1 && h > 1) {
            g.fillRect(x + 1, y + 1, sourceFill - 1, h - 1);
        }
        g.setColor(new Color(0x507f9a));
        g.drawRect(x, y, w, h);
    }

    private static void drawPetStateGauge(Graphics2D g, VqsvUiLayout layout, int widgetId, boolean selected,
                                          int percent, Color fallbackFill, Color fallbackEmpty) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            drawPetStateGauge(g, layout.x(widgetId, 73), layout.y(widgetId, 88),
                    layout.w(widgetId, 26), 4, percent, fallbackFill, fallbackEmpty);
            return;
        }
        Color fill = widgetTextColor(widget, selected, fallbackFill);
        Color empty = widgetFillColor(widget, selected, fallbackEmpty);
        int height = widget.h > 0 ? widget.h : 4;
        drawPetStateGauge(g, widget.x, widget.y, Math.max(1, widget.w), height, percent, fill, empty);
    }

    private static void drawPetQualityStars(Graphics2D g, VqsvUiLayout layout, VqsvBattlePetStateView view) {
        drawPetStateWidgetCell(g, layout, 69, false);
        int count = Math.max(0, Math.min(5, view.filledStars));
        int visible = Math.max(0, Math.min(5, view.visibleStars));
        for (int i = 0; i < visible; i++) {
            VqsvUiLayout.UiWidget widget = layout.widget(70 + i);
            if (widget != null) {
                drawBattleUiCellTopLeft(g, i < count ? 14 : Math.max(0, widget.altId), widget.x, widget.y);
            }
        }
    }

    private static void drawPetStateText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                         int widgetId, String text, Color color) {
        drawPetStateText(g, font, layout, widgetId, text, color, 0);
    }

    private static void drawPetStateText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                         int widgetId, String text, Color color, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget != null) {
            drawSourceWidgetText(g, font, text, widget.x, widget.y,
                    Math.max(1, widget.w), sourceWidgetHeight(widget),
                    widgetTextColor(widget, false, color), tick, widget.b);
        }
    }

    private static void drawPetStateText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                         int widgetId, String text, int width, Color color) {
        drawPetStateText(g, font, layout, widgetId, text, width, color, 0);
    }

    private static void drawPetStateText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                         int widgetId, String text, int width, Color color, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget != null) {
            drawSourceWidgetText(g, font, text, widget.x, widget.y,
                    Math.max(widget.w, width), sourceWidgetHeight(widget),
                    widgetTextColor(widget, false, color), tick, widget.b);
        }
    }

    private static int petstateUiTick(VqsvIntroDemo.Scene s) {
        return Math.max(0, s.battleAnimationTick - s.battleUiModeStartTick);
    }

    private static int sourceWidgetHeight(VqsvUiLayout.UiWidget widget) {
        return widget != null && widget.h > 0 ? widget.h : 10;
    }

    private static void drawSourceWidgetText(Graphics2D g, FontBitmap font, String text,
                                             int x, int y, int width, int height,
                                             Color color, int elapsedTicks, int align) {
        String decoded = TextBox.decodeMojibake(text);
        int textWidth = font.taggedWidth(decoded);
        int drawX;
        if (textWidth > width) {
            drawX = x - sourceHorizontalScrollOffset(textWidth, width, elapsedTicks);
        } else {
            switch (align) {
                case 1:
                case 4:
                case 7:
                    drawX = x + Math.max(0, (width - textWidth) / 2);
                    break;
                case 2:
                case 5:
                case 8:
                    drawX = x + Math.max(0, width - textWidth);
                    break;
                default:
                    drawX = x;
                    break;
            }
        }
        int drawY;
        if (align == 3 || align == 4 || align == 5) {
            drawY = y + Math.max(0, (height - 10) / 2);
        } else if (align == 6 || align == 7 || align == 8) {
            drawY = y + Math.max(0, height - 10);
        } else {
            drawY = y;
        }
        Shape oldClip = g.getClip();
        g.clipRect(x, y, width, Math.max(1, height));
        font.drawTaggedLine(g, decoded, drawX, drawY,
                TextBox.visibleLength(decoded), color.getRGB() & 0xFFFFFF);
        g.setClip(oldClip);
    }

    private static int sourceHorizontalScrollOffset(int textWidth, int width, int elapsedTicks) {
        int offset = -width / 2;
        int frames = Math.max(0, elapsedTicks) + 1;
        for (int i = 0; i < frames; i++) {
            if (textWidth > offset) {
                offset += 2;
            } else {
                offset = -width;
            }
        }
        return offset;
    }

    private static Color widgetTextColor(VqsvUiLayout.UiWidget widget, boolean selected, Color fallback) {
        return new Color(widgetColor(widget, selected, fallback.getRGB() & 0xffffff, true));
    }

    private static Color widgetFillColor(VqsvUiLayout.UiWidget widget, boolean selected, Color fallback) {
        return new Color(widgetColor(widget, selected, fallback.getRGB() & 0xffffff, false));
    }

    private static int widgetColor(VqsvUiLayout.UiWidget widget, boolean selected, int fallbackRgb, boolean text) {
        if (widget == null) {
            return fallbackRgb;
        }
        int source = selected
                ? (text ? widget.gColor : widget.eColor)
                : (text ? widget.lColor : widget.jColor);
        if ((source >>> 24) == 0 && source < 0) {
            return source & 0xffffff;
        }
        if (source == 0 || source == -16777216 || source == -1) {
            return fallbackRgb;
        }
        return source & 0xffffff;
    }

    private static void drawCenteredTinyText(Graphics2D g, FontBitmap font, String text,
                                            int x, int y, int width, Color color) {
        String decoded = TextBox.decodeMojibake(text);
        int textWidth = Math.min(width, font.width(decoded));
        int drawX = x + Math.max(0, (width - textWidth) / 2);
        drawTinyBattleText(g, font, text, drawX, y, width, color);
    }

    private static void drawChoiceSkillOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        drawSourceUiFill(g, 44, 70, 151, 8, 0xc6f1ff);
        drawSourceUiFill(g, 44, 78, 151, 160, 0xbde4ef);
        drawSourceUiFill(g, 44, 238, 151, 14, 0x82cafb);
        drawBattleUiCellTopLeft(g, 91, 41, 68);
        drawSourceUiFill(g, 48, 90, 143, 82, 0xbde4ef);
        drawTinyBattleText(g, font, VqsvText.Battle.SKILL_TITLE, 60, 75, 46, SOURCE_UI_TEXT);
        drawTinyBattleText(g, font, VqsvText.Battle.SKILL_PP_TITLE, 149, 75, 36, SOURCE_UI_TEXT);
        for (int i = 0; i < 5; i++) {
            int skillIndex = s.battleSkillScroll + i;
            int y = 95 + i * 15;
            boolean selected = skillIndex == s.battleSkillIndex;
            drawBattleUiCellTopLeft(g, selected ? 103 : 104, 54, y);
            if (skillIndex >= s.battleSkillNames.length) {
                continue;
            }
            Color color = selected ? new Color(0xfff16a) : SOURCE_UI_TEXT;
            drawTinyBattleText(g, font, s.battleSkillNames[skillIndex], 60, y + 1, 72, color);
            String pp = skillIndex < s.battleSkillPpLabels.length ? s.battleSkillPpLabels[skillIndex] : "";
            drawTinyBattleText(g, font, pp, 141, y + 1, 36, color);
        }
        drawChoiceSkillScroll(g, s);
        drawBattleUiCellTopLeft(g, 24, 52, 174);
        drawMarqueeTinyBattleText(g, font, s.battleSkillDescription, 57, 180, 125,
                Color.WHITE, s.battleAnimationTick);
        drawTinyBattleText(g, font, VqsvText.Battle.SKILL_USE, 50, 235, 58, SOURCE_UI_TEXT);
        drawTinyBattleText(g, font, "Quay", 164, 235, 28, SOURCE_UI_TEXT);
        drawTinyBattleText(g, font, "l\u1ea1i", 164, 245, 28, SOURCE_UI_TEXT);
    }

    private static void drawChoiceSkillScroll(Graphics2D g, VqsvIntroDemo.Scene s) {
        drawSourceUiFill(g, 183, 98, 3, 70, 0x00a5e3);
        int total = Math.max(1, s.battleSkillIds.length);
        int thumbH = total > 5 ? Math.max(8, 70 * 5 / total) : 70;
        int maxScroll = Math.max(1, total - 5);
        int thumbY = 98 + (70 - thumbH) * Math.max(0, s.battleSkillScroll) / maxScroll;
        drawSourceUiFill(g, 183, thumbY, 4, thumbH, 0xc6f1ff);
    }

    private static void drawTargetCursor(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        int x = s.battleTargetPlayerSide ? 55 : 171;
        int y = s.battleTargetPlayerSide ? 130 : 60;
        drawBattleUiCellTopLeft(g, 31, x, y);
        String name = s.battleTargetIndex >= 0 && s.battleTargetIndex < s.battleTargetNames.length
                ? s.battleTargetNames[s.battleTargetIndex] : "";
        if (!name.isEmpty()) {
            drawBattlePanel(g, 72, 268, 96, 18, true);
            drawTinyBattleText(g, font, name, 78, 273, 84, new Color(0xfff16a));
        }
    }

    private static void drawP7Damage(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        int baseX = sourceBattleActorX(s, s.battleP7TargetPlayerSide);
        int baseY = sourceBattleActorY(s, s.battleP7TargetPlayerSide);
        short[] damageBlood = VqsvBattleAnimationTables.instance().bloodRow(0);
        int damageFrame = bloodFrame(s.battleP7Ticks, damageBlood);
        int dx = bloodValue(damageBlood, damageFrame, 0);
        int dy = bloodValue(damageBlood, damageFrame, 1);
        int x = s.battleP7TargetPlayerSide ? baseX + dx + 30 : baseX - dx - 30;
        int y = baseY + dy - 30;
        if (!s.battleP7DamageText.isEmpty() && s.battleP7Ticks < frameCount(damageBlood)) {
            Color damageColor = s.battleP7DamageCritical ? new Color(0xff5d3b) : new Color(0xfff16a);
            drawOutlinedTinyBattleText(g, font, s.battleP7DamageText, x - 14, y,
                    44, damageColor, new Color(0x3f0707));
        }

        short[] textBlood = VqsvBattleAnimationTables.instance().bloodRow(1);
        String secondaryText = !s.battleP7MissText.isEmpty() ? s.battleP7MissText : s.battleP7DebuffText;
        if (!secondaryText.isEmpty() && s.battleP7Ticks < frameCount(textBlood)) {
            int textFrame = bloodFrame(s.battleP7Ticks, textBlood);
            int textDy = bloodValue(textBlood, textFrame, 1);
            int textX = s.battleP7TargetPlayerSide ? baseX - 10 : baseX + 10;
            int textY = baseY + textDy - 30;
            drawOutlinedTinyBattleText(g, font, secondaryText, textX - 22, textY,
                    62, new Color(0xffffff), new Color(0x14344a));
        }
    }

    private static int bloodFrame(int tick, short[] row) {
        int frames = row == null ? 0 : row.length / 2;
        if (frames <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(frames - 1, tick));
    }

    private static int frameCount(short[] row) {
        return row == null ? 0 : row.length / 2;
    }

    private static int bloodValue(short[] row, int frame, int axis) {
        int index = frame * 2 + axis;
        if (row == null || index < 0 || index >= row.length) {
            return 0;
        }
        return row[index];
    }

    private static void drawP7PostEffect(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        int baseX = (s.battleP7PostEffectPlayerSide ? 64 : 176) + sideOffsetX(s, s.battleP7PostEffectPlayerSide);
        int baseY = (s.battleP7PostEffectPlayerSide ? 128 : 58) + sideOffsetY(s, s.battleP7PostEffectPlayerSide);
        drawOutlinedTinyBattleText(g, font, s.battleP7PostEffectText, baseX - 20, baseY - 42,
                64, new Color(0xffffff), new Color(0x14344a));
    }

    private static void drawP7SpecialEffect(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (!s.battleP7SpecialVisible || !isSupportedP7SpecialType(s.battleP7SpecialType)) {
            return;
        }
        if (s.battleP7SpecialType == 9
                && (s.battleP7Ticks / Math.max(1, s.battleP7SpecialInterval)) % 2 != 0) {
            return;
        }
        int sprite = s.battleP7SpecialOnPlayerSide ? s.battlePlayerVisualId : s.battleEnemyVisualId;
        if (sprite < 0) {
            return;
        }
        if (s.battleP7SpecialType == 7) {
            drawP7SpecialType7(g, s, sprite);
            return;
        }
        BufferedImage overlay = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D og = overlay.createGraphics();
        drawP7SpecialBaseSpriteAtSource(og, s, sprite);
        og.dispose();
        if (s.battleP7SpecialType == 9) {
            applyAhType9Transform(overlay, s.battleP7SpecialAlpha,
                    s.battleP7SpecialRed, s.battleP7SpecialGreen, s.battleP7SpecialBlue);
            g.drawImage(overlay, 0, 0, null);
        } else if (s.battleP7SpecialType == 1) {
            applyAhType1Texture(overlay, s);
            g.drawImage(overlay, 0, 0, null);
        } else if (s.battleP7SpecialType == 8) {
            drawP7SpecialType8(g, overlay, s);
        } else if (s.battleP7SpecialType == 12) {
            drawP7SpecialType12(g, overlay, s);
        }
    }

    private static void drawP7SpecialBaseSpriteAtSource(Graphics2D g, VqsvIntroDemo.Scene s, int sprite) {
        boolean playerSide = s.battleP7SpecialOnPlayerSide;
        int state = playerSide ? s.battleP7BaseStatePlayerSide : s.battleP7BaseStateEnemySide;
        int cursor = playerSide ? s.battleP7BaseCursorPlayerSide : s.battleP7BaseCursorEnemySide;
        if (cursor < 0) {
            cursor = idleCursor(sprite, state, s.battleAnimationTick);
        }
        drawBattleSpriteAtSource(g, sprite,
                sourceBattleActorX(s, playerSide) + sideOffsetX(s, playerSide),
                sourceBattleActorY(s, playerSide) + sideOffsetY(s, playerSide),
                sourceBattleOrientation(playerSide),
                state,
                cursor);
    }

    private static boolean isSupportedP7SpecialType(int type) {
        return type == 1 || type == 7 || type == 8 || type == 9 || type == 12;
    }

    private static void drawP7SpecialType7(Graphics2D g, VqsvIntroDemo.Scene s, int sprite) {
        short[] row = s.battleP7SpecialRow;
        if (row.length < 8) {
            return;
        }
        int state = s.battleP7SpecialOnPlayerSide ? s.battleP7BaseStatePlayerSide : s.battleP7BaseStateEnemySide;
        int cursor = s.battleP7SpecialOnPlayerSide ? s.battleP7BaseCursorPlayerSide : s.battleP7BaseCursorEnemySide;
        if (cursor < 0) {
            cursor = idleCursor(sprite, state, s.battleAnimationTick);
        }
        int cellId = currentCellId(sprite, state, cursor);
        if (cellId < 0) {
            return;
        }
        SpriteAnim anim = SpriteAnim.load(sprite);
        int[] bounds = anim.cellBounds(cellId);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return;
        }
        BufferedImage base = renderSpriteCellImage(sprite, cellId, sourceBattleOrientation(s.battleP7SpecialOnPlayerSide));
        if (base == null) {
            return;
        }
        int originX = sourceBattleActorX(s, s.battleP7SpecialOnPlayerSide)
                + sideOffsetX(s, s.battleP7SpecialOnPlayerSide);
        int originY = sourceBattleActorY(s, s.battleP7SpecialOnPlayerSide)
                + sideOffsetY(s, s.battleP7SpecialOnPlayerSide);
        int x = originX + bounds[0];
        int y = originY + bounds[1];
        int interval = Math.max(1, row[3]);
        boolean drawScaled = (Math.max(0, s.battleP7Ticks) / interval) % 2 == 0;
        if (!drawScaled) {
            g.drawImage(base, x, y, null);
            return;
        }
        int scaleXNum = row.length > 4 ? row[4] : 1;
        int scaleXDen = Math.max(1, row.length > 5 ? row[5] : 1);
        int scaleYNum = row.length > 6 ? row[6] : scaleXNum;
        int scaleYDen = Math.max(1, row.length > 7 ? row[7] : scaleXDen);
        int scaledW = Math.max(1, bounds[2] * scaleXNum / scaleXDen);
        int scaledH = Math.max(1, bounds[3] * scaleYNum / scaleYDen);
        int dx = (bounds[2] - scaledW) / 2;
        int dy = bounds[3] - scaledH;
        Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(base, x + dx, y + dy, scaledW, scaledH, null);
        if (old == null) {
            g.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
        }
    }

    private static void drawP7SpecialType8(Graphics2D g, BufferedImage overlay, VqsvIntroDemo.Scene s) {
        short[] row = s.battleP7SpecialRow;
        if (row.length < 9) {
            g.drawImage(overlay, 0, 0, null);
            return;
        }
        int count = Math.max(1, (row.length - 6) / 3);
        int total = Math.max(count, row[2]);
        int ticksPerStep = Math.max(1, total / count);
        int step = Math.max(0, Math.min(count - 1, Math.max(0, s.battleP7Ticks) / ticksPerStep));
        int tripleAt = 6 + step * 3;
        int scale10 = 10;
        int dx = 0;
        int dy = 0;
        if (tripleAt + 2 < row.length) {
            scale10 = Math.max(1, row[tripleAt]);
            dx = row[tripleAt + 1];
            dy = row[tripleAt + 2];
        }
        BufferedImage bright = brightenCopy(overlay, 50);
        int scaledW = Math.max(1, bright.getWidth() * scale10 / 10);
        int scaledH = Math.max(1, bright.getHeight() * scale10 / 10);
        int x = dx + (bright.getWidth() - scaledW) / 2;
        int y = dy + (bright.getHeight() - scaledH) / 2;
        Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(bright, x, y, scaledW, scaledH, null);
        if (old == null) {
            g.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
        }
    }

    private static BufferedImage brightenCopy(BufferedImage src, int add) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int alpha = argb >>> 24;
                if (alpha == 0) {
                    continue;
                }
                int r = Math.min(255, ((argb >> 16) & 0xff) + add);
                int g = Math.min(255, ((argb >> 8) & 0xff) + add);
                int b = Math.min(255, (argb & 0xff) + add);
                out.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    private static void drawP7SpecialType12(Graphics2D g, BufferedImage overlay, VqsvIntroDemo.Scene s) {
        short[] row = s.battleP7SpecialRow;
        if (row.length < 10) {
            g.drawImage(overlay, 0, 0, null);
            return;
        }
        int total = Math.max(1, row[5]);
        int frame = Math.max(0, Math.min(total - 1, s.battleP7Ticks));
        int offsetStart = 8;
        int firstAt = offsetStart + frame * 2;
        int secondAt = offsetStart + total * 2 + frame * 2;
        if (secondAt + 1 >= row.length) {
            g.drawImage(overlay, 0, 0, null);
            return;
        }
        int dx0 = row[firstAt];
        int dy0 = row[firstAt + 1];
        int dx1 = row[secondAt];
        int dy1 = row[secondAt + 1];
        if (s.battleP7SpecialOnPlayerSide) {
            dx0 = -dx0;
            dx1 = -dx1;
        }
        BufferedImage b0 = alphaCopy(overlay, Math.max(0, Math.min(255, row[2])));
        BufferedImage b1 = alphaCopy(overlay, Math.max(0, Math.min(255, row[3])));
        g.drawImage(b1, dx0 + dx1, dy0 - dy1, null);
        g.drawImage(b0, dx0, dy0, null);
    }

    private static int currentCellId(int spriteIndex, int state, int cursor) {
        SpriteAnim anim = SpriteAnim.load(spriteIndex);
        anim.setState(Math.max(0, state));
        if (anim.data.anim == null || anim.data.anim.length == 0 || anim.data.anim[anim.state].length == 0) {
            return -1;
        }
        short[] frames = anim.data.anim[anim.state];
        int safeCursor = Math.max(0, Math.min(cursor, Math.max(0, frames.length / 2 - 1)));
        return frames[safeCursor * 2 + 1];
    }

    private static void drawP7ActorEffect(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (!s.battleP7ActorEffectVisible || s.battleP7ActorEffectSpriteId < 0) {
            return;
        }
        if (s.battleP7ActorEffectOnPlayerSide) {
            drawBattleSprite(g, s.battleP7ActorEffectSpriteId,
                    18 + playerOffsetX(s), 140 + playerOffsetY(s), 96, 95, 7, 0,
                    s.battleP7ActorEffectState, s.battleP7ActorEffectCursor);
        } else {
            drawBattleSprite(g, s.battleP7ActorEffectSpriteId,
                    132 + enemyOffsetX(s), 70 + enemyOffsetY(s), 96, 118, 7, 0,
                    s.battleP7ActorEffectState, s.battleP7ActorEffectCursor);
        }
    }

    private static void drawP7DeathEffect(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (!s.battleP7DeathEffectVisible || s.battleP7DeathEffectSpriteId < 0) {
            return;
        }
        SpriteAnim sprite = SpriteAnim.load(s.battleP7DeathEffectSpriteId);
        int[] bounds = sprite.cellBounds(0);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return;
        }
        BufferedImage image = renderSpriteCellImage(s.battleP7DeathEffectSpriteId, 0,
                s.battleP7DeathEffectPlayerSide ? 0 : 1);
        if (image == null) {
            return;
        }
        int duration = Math.max(1, s.battleP7DeathEffectDuration);
        int tick = Math.max(0, Math.min(duration, s.battleP7DeathEffectTick));
        int strips = 4;
        int stripHeight = Math.max(1, image.getHeight() / strips);
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            int strip = Math.min(strips - 1, y / stripHeight);
            int stripStart = strip * duration / strips;
            boolean faded = tick >= stripStart;
            int alpha = faded ? Math.max(0, 255 - (tick - stripStart) * 255 / duration) : 255;
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) {
                    continue;
                }
                out.setRGB(x, y, (Math.min(a, alpha) << 24) | (argb & 0xFFFFFF));
            }
        }
        int x = sourceBattleActorX(s, s.battleP7DeathEffectPlayerSide)
                + sideOffsetX(s, s.battleP7DeathEffectPlayerSide) + bounds[0];
        int y = sourceBattleActorY(s, s.battleP7DeathEffectPlayerSide)
                + sideOffsetY(s, s.battleP7DeathEffectPlayerSide) + bounds[1];
        g.drawImage(out, x, y, null);
    }

    private static void drawState1LEffect(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (!s.battleLVisible || s.battleLRow.length == 0 || s.battleLSpriteId < 0) {
            return;
        }
        if (s.battleLType == 12) {
            drawState1LEffectType12(g, s);
            return;
        }
        if (s.battleLType == 13) {
            drawState1LEffectType13(g, s);
            return;
        }
        if (s.battleLType == 14) {
            drawState1LEffectType14(g, s);
            return;
        }
        if (s.battleLType == 15) {
            drawState1LEffectType15(g, s);
            return;
        }
        if (s.battleLType != 11) {
            return;
        }
        int count = Math.max(1, s.battleLRow[1]);
        int transformCount = count - 1;
        int tStart = 2 + (transformCount << 2);
        if (tStart + 4 >= s.battleLRow.length) {
            return;
        }
        BufferedImage base = renderSpriteCellImage(s.battleLSpriteId, 0, s.battleLDirection);
        if (base == null) {
            return;
        }
        int x = (s.battleLPlayerSide ? 18 : 132) + sideOffsetX(s, s.battleLPlayerSide);
        int y = (s.battleLPlayerSide ? 140 : 70) + sideOffsetY(s, s.battleLPlayerSide);
        int frame = Math.max(0, Math.min(s.battleLFrame, Math.max(0, s.battleLRow[tStart + 1] - 1)));
        for (int clone = 1; clone < count; clone++) {
            int colorAt = 2 + ((clone - 1) << 2);
            int offsetAt = tStart + 4 + ((frame * transformCount + clone - 1) << 1);
            if (colorAt + 3 >= s.battleLRow.length || offsetAt + 1 >= s.battleLRow.length) {
                continue;
            }
            BufferedImage tinted = tintOpaque(base, s.battleLRow[colorAt],
                    s.battleLRow[colorAt + 1], s.battleLRow[colorAt + 2], s.battleLRow[colorAt + 3]);
            int dx = s.battleLRow[offsetAt];
            int dy = s.battleLRow[offsetAt + 1];
            if (s.battleLDirection == 1) {
                dx = -dx;
            }
            g.drawImage(tinted, x + dx, y + dy, null);
        }
    }

    private static void drawState1LEffectType14(Graphics2D g, VqsvIntroDemo.Scene s) {
        int count = Math.max(1, s.battleLRow[1]);
        int transformCount = count - 1;
        int tStart = 2 + (transformCount << 2);
        if (transformCount <= 0 || tStart + 4 >= s.battleLRow.length) {
            return;
        }
        BufferedImage base = renderSpriteCellImage(s.battleLSpriteId, 0, s.battleLDirection);
        if (base == null) {
            return;
        }
        int x = (s.battleLPlayerSide ? 18 : 132) + sideOffsetX(s, s.battleLPlayerSide);
        int y = (s.battleLPlayerSide ? 140 : 70) + sideOffsetY(s, s.battleLPlayerSide);
        int frame = Math.max(0, Math.min(s.battleLFrame, Math.max(0, s.battleLRow[tStart + 1] - 1)));
        for (int clone = 1; clone < count; clone++) {
            int transformAt = 2 + ((clone - 1) << 2);
            int offsetAt = tStart + 4 + ((frame * transformCount + clone - 1) << 1);
            if (transformAt + 1 >= s.battleLRow.length || offsetAt + 1 >= s.battleLRow.length) {
                continue;
            }
            BufferedImage adjusted = adjustRgb(base, s.battleLRow[transformAt], s.battleLRow[transformAt + 1]);
            int dx = s.battleLRow[offsetAt];
            int dy = s.battleLRow[offsetAt + 1];
            if (s.battleLDirection == 1) {
                dx = -dx;
            }
            g.drawImage(adjusted, x + dx, y + dy, null);
        }
    }

    private static void drawState1LEffectType13(Graphics2D g, VqsvIntroDemo.Scene s) {
        int count = Math.max(1, s.battleLRow[1]);
        int tStart = 2 + count;
        if (count < 1 || tStart + 4 >= s.battleLRow.length) {
            return;
        }
        BufferedImage base = renderSpriteCellImage(s.battleLSpriteId, 0, s.battleLDirection);
        if (base == null) {
            return;
        }
        int totalFrames = Math.max(1, s.battleLRow[tStart + 1]);
        int frame = Math.max(0, Math.min(totalFrames - 1, s.battleLFrame));
        int x = (s.battleLPlayerSide ? 18 : 132) + sideOffsetX(s, s.battleLPlayerSide);
        int y = (s.battleLPlayerSide ? 140 : 70) + sideOffsetY(s, s.battleLPlayerSide);
        for (int layer = 0; layer < count; layer++) {
            int alphaAt = 2 + layer;
            int offsetAt = tStart + 4 + ((frame * count + layer) << 1);
            if (alphaAt >= s.battleLRow.length || offsetAt + 1 >= s.battleLRow.length) {
                continue;
            }
            BufferedImage layerImage = alphaCopy(base, s.battleLRow[alphaAt]);
            int dx = s.battleLRow[offsetAt];
            int dy = s.battleLRow[offsetAt + 1];
            if (s.battleLDirection == 1) {
                dx = -dx;
            }
            g.drawImage(layerImage, x + dx, y + dy, null);
        }
    }

    private static void drawState1LEffectType15(Graphics2D g, VqsvIntroDemo.Scene s) {
        int count = Math.max(1, s.battleLRow[1]);
        int tStart = 2 + ((count - 1) << 2);
        if (count < 1 || tStart + 4 >= s.battleLRow.length) {
            return;
        }
        int totalFrames = Math.max(1, s.battleLRow[tStart + 1]);
        int frame = Math.max(0, Math.min(totalFrames - 1, s.battleLFrame));
        int frameAt = tStart + 4 + frame * 3;
        if (frameAt + 2 >= s.battleLRow.length) {
            return;
        }
        BufferedImage base = renderSpriteCellImage(s.battleLSpriteId, 0, s.battleLDirection);
        if (base == null) {
            return;
        }
        int imageIndex = Math.max(0, Math.min(count - 1, s.battleLRow[frameAt]));
        BufferedImage frameImage = base;
        if (imageIndex > 0) {
            int colorAt = 2 + ((imageIndex - 1) << 2);
            if (colorAt + 3 >= s.battleLRow.length) {
                return;
            }
            frameImage = tintOpaque(base, s.battleLRow[colorAt],
                    s.battleLRow[colorAt + 1], s.battleLRow[colorAt + 2], s.battleLRow[colorAt + 3]);
        }
        int x = (s.battleLPlayerSide ? 18 : 132) + sideOffsetX(s, s.battleLPlayerSide);
        int y = (s.battleLPlayerSide ? 140 : 70) + sideOffsetY(s, s.battleLPlayerSide);
        int dx = s.battleLRow[frameAt + 1];
        int dy = s.battleLRow[frameAt + 2];
        if (s.battleLDirection == 1) {
            dx = -dx;
        }
        g.drawImage(frameImage, x + dx, y + dy, null);
    }

    private static void drawState1LEffectType12(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (s.battleLRow.length < 10) {
            return;
        }
        int count = Math.max(1, s.battleLRow[1]);
        if (count < 2) {
            return;
        }
        int tStart = 4;
        int totalFrames = Math.max(1, s.battleLRow[tStart + 1]);
        int firstOffsetAt = tStart + 4 + Math.max(0, Math.min(totalFrames - 1, s.battleLFrame)) * 2;
        int secondOffsetAt = tStart + 4 + (totalFrames << 1)
                + Math.max(0, Math.min(totalFrames - 1, s.battleLFrame)) * 2;
        if (secondOffsetAt + 1 >= s.battleLRow.length) {
            return;
        }
        BufferedImage base = renderSpriteCellImage(s.battleLSpriteId, 0, s.battleLDirection);
        if (base == null) {
            return;
        }
        BufferedImage b0 = alphaCopy(base, s.battleLRow[2]);
        BufferedImage b1 = alphaCopy(base, s.battleLRow[3]);
        int x = (s.battleLPlayerSide ? 18 : 132) + sideOffsetX(s, s.battleLPlayerSide);
        int y = (s.battleLPlayerSide ? 140 : 70) + sideOffsetY(s, s.battleLPlayerSide);
        int dx0 = s.battleLRow[firstOffsetAt];
        int dy0 = s.battleLRow[firstOffsetAt + 1];
        int dx1 = s.battleLRow[secondOffsetAt];
        int dy1 = s.battleLRow[secondOffsetAt + 1];
        if (s.battleLDirection == 1) {
            g.drawImage(b1, x - (dx1 + dx0), y - dy1 + dy0, null);
            g.drawImage(b0, x - dx0, y + dy0, null);
        } else {
            g.drawImage(b1, x + dx1 + dx0, y - dy1 + dy0, null);
            g.drawImage(b0, x + dx0, y + dy0, null);
        }
    }

    private static BufferedImage renderSpriteCellImage(int spriteIndex, int cellId, int orientation) {
        SpriteAnim sprite = SpriteAnim.load(spriteIndex);
        int[] bounds = sprite.cellBounds(cellId);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return null;
        }
        BufferedImage img = new BufferedImage(bounds[2], bounds[3], BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = img.createGraphics();
        sprite.drawCell(ig, cellId, -bounds[0], -bounds[1], orientation);
        ig.dispose();
        normalizeJavaMeEffectPixels(img);
        return img;
    }

    private static void normalizeJavaMeEffectPixels(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0 || argb == 0xffffffff || argb == 0xff000000) {
                    image.setRGB(x, y, JAVA_ME_EFFECT_TRANSPARENT_KEY);
                }
            }
        }
    }

    private static BufferedImage tintOpaque(BufferedImage source, int alpha, int red, int green, int blue) {
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int transformed = alpha < 0 || alpha > 255
                ? (red << 16) | (green << 8) | blue
                : (alpha << 24) | (red << 16) | (green << 8) | blue;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                if (argb == JAVA_ME_EFFECT_TRANSPARENT_KEY) {
                    out.setRGB(x, y, JAVA_ME_EFFECT_TRANSPARENT_KEY);
                    continue;
                }
                out.setRGB(x, y, transformed);
            }
        }
        return out;
    }

    private static BufferedImage alphaCopy(BufferedImage source, int alpha) {
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        if (alpha < 0 || alpha > 255) {
            Graphics2D g = out.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return out;
        }
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                if (argb == JAVA_ME_EFFECT_TRANSPARENT_KEY || argb == 0) {
                    out.setRGB(x, y, argb);
                    continue;
                }
                out.setRGB(x, y, argb == 0xff000000 ? 0 : (alpha << 24) | (argb & 0x00ffffff));
            }
        }
        return out;
    }

    private static BufferedImage adjustRgb(BufferedImage source, int multiplier, int add) {
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int r = clamp(((argb >> 16) & 0xff) * multiplier + add);
                int gr = clamp(((argb >> 8) & 0xff) * multiplier + add);
                int b = clamp((argb & 0xff) * multiplier + add);
                out.setRGB(x, y, (argb & 0xff000000) | (r << 16) | (gr << 8) | b);
            }
        }
        return out;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int sideOffsetX(VqsvIntroDemo.Scene s, boolean playerSide) {
        return playerSide ? playerOffsetX(s) : enemyOffsetX(s);
    }

    private static int sideOffsetY(VqsvIntroDemo.Scene s, boolean playerSide) {
        return playerSide ? playerOffsetY(s) : enemyOffsetY(s);
    }

    private static int playerOffsetX(VqsvIntroDemo.Scene s) {
        return s.battleP7PlayerOffsetX;
    }

    private static int playerOffsetY(VqsvIntroDemo.Scene s) {
        return s.battleP7PlayerOffsetY;
    }

    private static int enemyOffsetX(VqsvIntroDemo.Scene s) {
        return s.battleP7EnemyOffsetX;
    }

    private static int enemyOffsetY(VqsvIntroDemo.Scene s) {
        return s.battleP7EnemyOffsetY;
    }

    private static void applyAhType9Transform(BufferedImage image, int alpha, int red, int green, int blue) {
        int a = Math.max(0, Math.min(255, alpha));
        int r = Math.max(0, Math.min(255, red));
        int gr = Math.max(0, Math.min(255, green));
        int b = Math.max(0, Math.min(255, blue));
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int sourceAlpha = argb >>> 24;
                if (sourceAlpha == 0) {
                    continue;
                }
                int outA = Math.min(255, a + 50);
                int outR = Math.min(255, r + 50);
                int outG = Math.min(255, gr + 50);
                int outB = Math.min(255, b + 50);
                image.setRGB(x, y, (outA << 24) | (outR << 16) | (outG << 8) | outB);
            }
        }
    }

    private static void applyAhType1Texture(BufferedImage image, VqsvIntroDemo.Scene s) {
        BufferedImage texture = loadTexImage(s.battleP7SpecialTextureId);
        if (texture == null || texture.getWidth() <= 0 || texture.getHeight() <= 0) {
            return;
        }
        int scroll = s.battleP7Ticks * 4;
        int mode = s.battleP7SpecialScrollMode;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                int tx = x;
                int ty = y;
                if (mode == 0) {
                    ty = y + scroll;
                } else if (mode == 1) {
                    ty = y - scroll;
                } else if (mode == 2) {
                    tx = x + scroll;
                } else if (mode == 3) {
                    tx = x - scroll;
                }
                int tex = texture.getRGB(Math.floorMod(tx, texture.getWidth()), Math.floorMod(ty, texture.getHeight()));
                int bright = brightenForAhType1(argb);
                int out;
                if (s.battleP7SpecialBlendMode == 1) {
                    out = bright | tex;
                } else if (s.battleP7SpecialBlendMode == 2) {
                    out = tex;
                } else {
                    out = bright & tex;
                }
                image.setRGB(x, y, out);
            }
        }
    }

    private static int brightenForAhType1(int argb) {
        int a = argb >>> 24;
        int r = Math.min(255, ((argb >>> 16) & 0xff) * 5 + 5);
        int g = Math.min(255, ((argb >>> 8) & 0xff) * 5 + 5);
        int b = Math.min(255, (argb & 0xff) * 5 + 5);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static BufferedImage loadTexImage(int texId) {
        if (texId < 0) {
            return null;
        }
        if (TEX_CACHE.containsKey(texId)) {
            return TEX_CACHE.get(texId);
        }
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            Path path = paths.texDecodedPng("tex_" + texId);
            BufferedImage image = ImageIO.read(path.toFile());
            TEX_CACHE.put(texId, image);
            return image;
        } catch (IOException | RuntimeException ex) {
            TEX_CACHE.put(texId, null);
            return null;
        }
    }

    private static void drawSourceUiFill(Graphics2D g, int x, int y, int w, int h, int rgb) {
        Color old = g.getColor();
        g.setColor(new Color(rgb));
        g.fillRect(x, y, w, h);
        g.setColor(old);
    }

    private static void drawWarningOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvMsgWarmView view = s.battleMsgWarm == null || !s.battleMsgWarm.visible()
                ? VqsvMsgWarmView.of(s.battleWarningTitle, s.battleWarningPrompt)
                : s.battleMsgWarm;
        VqsvUiLayout layout = VqsvUiLayout.load("msgwarm.ui");
        drawSourceWidgetFill(g, layout, 1, 7, 0xc6f3ff);
        drawSourceWidgetFill(g, layout, 2, 59, 0xbee6f2);
        drawSourceWidgetFill(g, layout, 3, 10, 0x6cc2fb);
        drawSourceWidgetFill(g, layout, 5, 54, 0x51d8e9);
        drawBattleUiCellTopLeft(g, VqsvMsgWarmView.FRAME_SPRITE_CELL,
                layout.x(VqsvMsgWarmView.FRAME_WIDGET_ID, 76),
                layout.y(VqsvMsgWarmView.FRAME_WIDGET_ID, 106));
        drawMarqueeTinyBattleText(g, font, view.widgetText(VqsvMsgWarmView.MESSAGE_WIDGET_ID),
                layout.x(VqsvMsgWarmView.MESSAGE_WIDGET_ID, 85),
                layout.y(VqsvMsgWarmView.MESSAGE_WIDGET_ID, 119),
                layout.w(VqsvMsgWarmView.MESSAGE_WIDGET_ID, 70),
                SOURCE_UI_TEXT, s.battleAnimationTick);
        drawMarqueeTinyBattleText(g, font, view.widgetText(VqsvMsgWarmView.PROMPT_WIDGET_ID),
                layout.x(VqsvMsgWarmView.PROMPT_WIDGET_ID, 89),
                layout.y(VqsvMsgWarmView.PROMPT_WIDGET_ID, 170),
                layout.w(VqsvMsgWarmView.PROMPT_WIDGET_ID, 60),
                SOURCE_UI_TEXT, s.battleAnimationTick);
    }

    private static void drawSmsInfoOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("smsInfo.ui");
        drawSourceWidgetFill(g, layout, 3, 9, 0xc6f1ff);
        drawSourceWidgetFill(g, layout, 1, 159, 0xbde4ef);
        drawSourceWidgetFill(g, layout, 2, 11, 0x82d0fb);
        drawSourceWidgetCell(g, layout, 4, false, false);
        drawSmsInfoWrappedText(g, font, layout, 8, s.battleWarningTitle, SOURCE_UI_TEXT);
        drawSmsInfoCenteredText(g, font, layout, 5, s.battleWarningPrompt, SOURCE_UI_TEXT);
        drawCenteredTinyText(g, font, "X\u00e1c nh\u1eadn", layout.x(10, 52), layout.y(10, 240),
                Math.max(54, layout.w(10, 24)), Color.WHITE);
        drawCenteredTinyText(g, font, "Ph\u1ea3n h\u1ed3i", Math.max(130, layout.x(11, 167) - 36),
                layout.y(11, 240), Math.max(58, layout.w(11, 24)), Color.WHITE);
    }

    private static void drawSmsInfoWrappedText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                               int widgetId, String text, Color color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int x = widget == null ? 62 : widget.x;
        int y = widget == null ? 101 : widget.y;
        int w = widget == null ? 118 : Math.max(1, widget.w);
        int h = widget == null ? 68 : Math.max(1, widget.h);
        Shape oldClip = g.getClip();
        g.clipRect(x, y - 1, w, h + 2);
        String decoded = TextBox.decodeMojibake(text);
        font.drawTagged(g, decoded, x, y, w, TextBox.visibleLength(decoded));
        g.setClip(oldClip);
    }

    private static void drawSmsInfoCenteredText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                                int widgetId, String text, Color color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int x = widget == null ? 66 : widget.x;
        int y = widget == null ? 188 : widget.y;
        int w = widget == null ? 100 : Math.max(1, widget.w);
        drawCenteredTinyText(g, font, text, x, y, w, color);
    }

    private static void drawLevelUpOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvBattleLevelUpView view = s.battleLevelUpView == null
                ? VqsvBattleLevelUpView.EMPTY : s.battleLevelUpView;
        if (!view.visible) {
            return;
        }
        if (!view.leveled) {
            return;
        }
        drawSourceUiFill(g, 43, 55, 158, 202, 0x89d8ef);
        drawSourceUiFill(g, 46, 78, 150, 9, 0xc6f3ff);
        drawSourceUiFill(g, 46, 87, 150, 159, 0xf7ffff);
        drawSourceUiFill(g, 46, 246, 150, 11, 0x82d0fb);
        drawBattleUiCellTopLeft(g, 1, 43, 55);
        drawBattleUiCellTopLeft(g, 8, 78, 94);
        drawBattleUiCellTopLeft(g, 17, 50, 90);
        drawBattleUiCellTopLeft(g, 15, 144, 90);
        drawCenteredTinyText(g, font, VqsvText.Battle.LEVEL_UP_TITLE, 70, 60, 100, Color.WHITE);
        if (view.visualId >= 0) {
            Shape oldClip = g.getClip();
            g.clipRect(78, 90, 90, 88);
            drawBattleSprite(g, view.visualId, 78, 90, 90, 88, 7, 0,
                    0, idleCursor(view.visualId, 0, s.battleAnimationTick));
            g.setClip(oldClip);
        }
        drawMarqueeTinyBattleText(g, font, view.name, 53, 84, 72, SOURCE_UI_TEXT, s.battleLevelUpTicks);
        drawTinyBattleText(g, font, "lv", 150, 82, 12, SOURCE_UI_TEXT);
        drawTinyBattleText(g, font, String.valueOf(view.level), 165, 82, 24, SOURCE_UI_TEXT);
        drawLevelUpStats(g, font, view, s.battleLevelUpTicks);
        if (!view.message.isEmpty()) {
            drawMarqueeTinyBattleText(g, font, view.message, 76, 240, 96,
                    SOURCE_UI_TEXT, s.battleLevelUpTicks);
        }
    }

    private static void drawLevelUpStats(Graphics2D g, FontBitmap font, VqsvBattleLevelUpView view, int tick) {
        String[] labels = {"M\u1ec7nh", "C\u00f4ng", "Ph\u00f2ng", "Min"};
        int[] textY = {182, 196, 209, 222};
        int[] rowY = {188, 201, 214, 227};
        for (int i = 0; i < 4; i++) {
            int rowCell = i == 0 ? 15 : 16;
            drawBattleUiCellTopLeft(g, rowCell, 59, rowY[i]);
            drawBattleUiCellTopLeft(g, rowCell, 139, rowY[i]);
            drawBattleUiCellTopLeft(g, 22, 114, rowY[i] - 1);
            drawMarqueeTinyBattleText(g, font, labels[i], 65, textY[i], 12,
                    SOURCE_UI_TEXT, tick);
            drawTinyBattleText(g, font, String.valueOf(view.oldStats[i]), 80, textY[i], 24, SOURCE_UI_TEXT);
            drawMarqueeTinyBattleText(g, font, labels[i], 145, textY[i], 12,
                    SOURCE_UI_TEXT, tick);
            drawTinyBattleText(g, font, String.valueOf(view.newStats[i]), 160, textY[i], 24, SOURCE_UI_TEXT);
        }
    }

    private static void drawCatchAnimation(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (s.battleCatchEffectVisible) {
            drawCatchEffectType8(g, s);
        }
        SpriteAnim ball = SpriteAnim.load(s.battleCatchSpriteId);
        ball.setState(Math.max(0, s.battleCatchPhase));
        ball.cursor = Math.max(0, s.battleCatchAnimCursor);
        if (s.battleCatchPhase == 3) {
            drawCatchSuccessBallOnEnemyGround(g, ball, s);
        } else {
            int[] target = enemyVisibleSpriteRect(s);
            ball.drawAligned(g, target[0], target[1], target[2], target[3], 4, 0);
        }
    }

    private static void drawNpcEnemyEntryOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        NpcEnemyEntryView view = NpcEnemyEntryView.from(s);
        if (s.battleNpcEnemyEntryStep >= 5) {
            drawBattleBackground(g, s);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, VqsvIntroDemo.W, VqsvIntroDemo.H);
        }
        VqsvUiLayout layout = VqsvUiLayout.load("npcEnemy.ui");
        drawSpriteCellTopLeft(g, 296, view.mainCell, layout.x(1, 0), layout.y(1, 71));
        drawNpcEntryPortraits(g, layout, view, s);
        drawNpcEntryTeamSlots(g, layout, view);
        drawNpcEntryLabels(g, font, layout, view, s);
        if (view.overlay36) {
            VqsvUiLayout.UiWidget overlay = layout.widget(36);
            if (overlay != null) {
                Color old = g.getColor();
                g.setColor(new Color(255, 255, 255, 92));
                g.fillRect(overlay.x, overlay.y, Math.max(1, overlay.w), sourceWidgetHeight(overlay));
                g.setColor(old);
            }
        }
    }

    private static void drawNpcEntryPortraits(Graphics2D g, VqsvUiLayout layout,
                                              NpcEnemyEntryView view, VqsvIntroDemo.Scene s) {
        drawNpcEntryPortrait(g, layout, view, 2, s.battleNpcEnemyEnemyVisualId, false);
        drawNpcEntryPortrait(g, layout, view, 3, s.battleNpcEnemyPlayerVisualId, true);
        drawNpcEntryPortrait(g, layout, view, 34, s.battleNpcEnemyEnemyVisualId, false);
        drawNpcEntryPortrait(g, layout, view, 35, s.battleNpcEnemyPlayerVisualId, true);
        drawNpcEntryPortrait(g, layout, view, 4, s.battleNpcEnemyEnemyVisualId, false);
        drawNpcEntryPortrait(g, layout, view, 5, s.battleNpcEnemyPlayerVisualId, true);
    }

    private static void drawNpcEntryPortrait(Graphics2D g, VqsvUiLayout layout, NpcEnemyEntryView view,
                                             int widgetId, int visualId, boolean playerSide) {
        if (!view.visible[widgetId]) {
            return;
        }
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int h = Math.max(48, sourceWidgetHeight(widget));
        drawNpcEntryPortrait(g, visualId, widget.x, widget.y, Math.max(1, widget.w), h, playerSide);
    }

    private static void drawNpcEntryPortrait(Graphics2D g, int visualId, int x, int y, int w, int h,
                                             boolean playerSide) {
        if (visualId < 0) {
            return;
        }
        drawBattleSprite(g, visualId, x, y, Math.max(1, w), Math.max(1, h),
                playerSide ? 7 : 1, sourceBattleOrientation(playerSide));
    }

    private static void drawNpcEntryTeamSlots(Graphics2D g, VqsvUiLayout layout, NpcEnemyEntryView view) {
        for (int id = 6; id <= 29; id++) {
            if (view.cells[id] < 0) {
                continue;
            }
            VqsvUiLayout.UiWidget widget = layout.widget(id);
            if (widget == null) {
                continue;
            }
            int x = widget.x;
            if (view.exitSlotRows && id >= 7 && id < 19 && id % 2 == 1) {
                x = 172 + 17 * (id - 7) / 2;
            } else if (view.exitSlotRows && id >= 19 && id < 31 && id % 2 == 1) {
                x = -30 + 17 * (id - 19) / 2;
            }
            drawSpriteCellTopLeft(g, 296, view.cells[id], x, widget.y);
        }
    }

    private static void drawNpcEntryLabels(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                           NpcEnemyEntryView view, VqsvIntroDemo.Scene s) {
        drawNpcEntryCell(g, layout, view, 30);
        drawNpcEntryCell(g, layout, view, 31);
        drawNpcEntryCell(g, layout, view, 32);
        drawNpcEntryCell(g, layout, view, 33);
        if (view.cells[30] >= 0) {
            drawNpcEntryText(g, font, layout, 30, s.battleEnemyName, new Color(0xffffff), s.battleAnimationTick);
        }
        if (view.cells[31] >= 0) {
            drawNpcEntryText(g, font, layout, 31, s.battlePlayerName, new Color(0xffffff), s.battleAnimationTick);
        }
        if (view.cells[32] >= 0) {
            drawNpcEntryText(g, font, layout, 32, "VS", new Color(0xfff16a), s.battleAnimationTick);
        }
    }

    private static void drawNpcEntryCell(Graphics2D g, VqsvUiLayout layout, NpcEnemyEntryView view, int widgetId) {
        if (view.cells[widgetId] < 0) {
            return;
        }
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget != null) {
            drawSpriteCellTopLeft(g, 296, view.cells[widgetId], widget.x, widget.y);
        }
    }

    private static void drawNpcEntryText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                         int widgetId, String text, Color color, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        drawSourceWidgetText(g, font, text, widget.x, widget.y,
                Math.max(1, widget.w), sourceWidgetHeight(widget), color, tick, widget.b);
    }

    private static final class NpcEnemyEntryView {
        private static final short[][] TIMELINE = new short[][]{
                {0}, {1}, {2}, {3}, {4, 5, 6, 7, 8}, {9}, {10},
                {11, 12}, {13}, {14, 15, 16, 17, 18, 19, 20}, {21}, {22}
        };

        final int[] cells = new int[37];
        final boolean[] visible = new boolean[37];
        int mainCell = 0;
        boolean overlay36;
        boolean exitSlotRows;
        int enemyCount;
        int playerCount;

        private NpcEnemyEntryView() {
            Arrays.fill(cells, -1);
        }

        static NpcEnemyEntryView from(VqsvIntroDemo.Scene s) {
            NpcEnemyEntryView view = new NpcEnemyEntryView();
            view.enemyCount = Math.max(0, Math.min(6, s.battleNpcEnemyEnemyCount));
            view.playerCount = Math.max(0, Math.min(6, s.battleNpcEnemyPlayerCount));
            int currentStep = s.battleNpcEnemyEntryStep < 0 ? 0 : s.battleNpcEnemyEntryStep;
            int currentFrame = s.battleNpcEnemyEntryFrame < 0 ? 0 : s.battleNpcEnemyEntryFrame;
            for (int step = 0; step < TIMELINE.length && step <= currentStep; step++) {
                short[] frames = TIMELINE[step];
                for (short frame : frames) {
                    apply(view, step, frame);
                    if (step == currentStep && frame == currentFrame) {
                        return view;
                    }
                }
            }
            return view;
        }

        private static void apply(NpcEnemyEntryView view, int step, int frame) {
            switch (step) {
                case 0:
                    view.mainCell = frame;
                    return;
                case 1:
                    view.visible[2] = true;
                    view.visible[3] = true;
                    view.mainCell = frame;
                    return;
                case 2:
                    view.visible[2] = false;
                    view.visible[3] = false;
                    view.visible[34] = true;
                    view.visible[35] = true;
                    view.mainCell = frame;
                    return;
                case 3:
                    view.visible[34] = false;
                    view.visible[35] = false;
                    view.visible[4] = true;
                    view.visible[5] = true;
                    view.mainCell = frame;
                    if (frame - 3 < view.enemyCount) {
                        setCell(view, 6, 6);
                    }
                    if (frame - 3 < view.playerCount) {
                        setCell(view, 18, 6);
                    }
                    return;
                case 4:
                    setCell(view, 6 + ((frame - 3) << 1), frame - 3 < view.enemyCount ? 6 : 5);
                    setCell(view, 7 + ((frame - 4) << 1), frame - 4 < view.enemyCount ? 6 : 5);
                    setCell(view, 19 + ((frame - 4) << 1), frame - 4 < view.playerCount ? 6 : 5);
                    setCell(view, 18 + ((frame - 3) << 1), frame - 3 < view.playerCount ? 6 : 5);
                    return;
                case 5:
                    setCell(view, 7 + ((frame - 4) << 1), frame - 4 < view.enemyCount ? 6 : 5);
                    setCell(view, 19 + ((frame - 4) << 1), frame - 4 < view.playerCount ? 6 : 5);
                    return;
                case 6:
                    setCell(view, 30, 8);
                    setCell(view, 31, 7);
                    return;
                case 7:
                    setCell(view, 32, 8);
                    setCell(view, 33, 7);
                    view.cells[30] = -1;
                    view.cells[31] = -1;
                    return;
                case 8:
                    view.overlay36 = true;
                    return;
                case 9:
                    view.overlay36 = false;
                    return;
                case 10:
                    view.mainCell = 4;
                    view.cells[32] = -1;
                    view.cells[33] = -1;
                    view.visible[4] = false;
                    view.visible[5] = false;
                    view.exitSlotRows = true;
                    return;
                case 11:
                    view.visible[4] = false;
                    view.visible[5] = false;
                    for (int id = 7; id < 19; id += 2) {
                        view.cells[id] = -1;
                        view.cells[id + 12] = -1;
                    }
                    view.mainCell = 0;
                    view.exitSlotRows = false;
                    return;
                default:
            }
        }

        private static void setCell(NpcEnemyEntryView view, int widgetId, int cell) {
            if (widgetId >= 0 && widgetId < view.cells.length) {
                view.cells[widgetId] = cell;
            }
        }
    }

    private static void drawCatchSuccessBallOnEnemyGround(Graphics2D g, SpriteAnim ball, VqsvIntroDemo.Scene s) {
        int[] marker = enemyGroundMarkerRect(s);
        int[] bounds = ball.currentCellBounds();
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            bounds = ball.animationBounds(Math.max(0, s.battleCatchPhase));
        }
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return;
        }
        // Sprite 269 q3 keeps the red/white ball core slightly above/right of its cell bounds center.
        // Bias the source-backed cell so the visible ball core sits in the enemy platform center.
        int centerX = marker[0] + marker[2] / 2 - 1;
        int centerY = marker[1] + marker[3] / 2 + 3;
        int drawX = centerX - bounds[0] - bounds[2] / 2;
        int drawY = centerY - bounds[1] - bounds[3] / 2;
        ball.draw(g, drawX, drawY, 0);
    }

    private static void drawCatchEffectType8(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (s.battleEnemyVisualId < 0) {
            return;
        }
        SpriteAnim enemy = SpriteAnim.load(s.battleEnemyVisualId);
        int[] bounds = enemy.cellBounds(0);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            drawBattleSprite(g, s.battleEnemyVisualId,
                    sourceBattleActorX(s, false) + enemyOffsetX(s) + s.battleCatchEffectDx,
                    sourceBattleActorY(s, false) + enemyOffsetY(s) + s.battleCatchEffectDy,
                    96, 118, 7, 0);
            return;
        }

        BufferedImage source = new BufferedImage(bounds[2], bounds[3], BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = source.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        enemy.drawCell(sg, 0, -bounds[0], -bounds[1], 0);
        sg.dispose();
        brightenOpaquePixels(source, 50);

        int scale10 = Math.max(1, s.battleCatchEffectScale10);
        int scaledW = Math.max(1, source.getWidth() * scale10 / 10);
        int scaledH = Math.max(1, source.getHeight() * scale10 / 10);
        int originX = sourceBattleActorX(s, false) + enemyOffsetX(s);
        int originY = sourceBattleActorY(s, false) + enemyOffsetY(s);
        int scaledSourceOriginX = bounds[0] * scale10 / 10;
        int scaledSourceOriginY = bounds[1] * scale10 / 10;
        int x = originX + scaledSourceOriginX + s.battleCatchEffectDx;
        int y = originY + scaledSourceOriginY + s.battleCatchEffectDy;
        Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, x, y, scaledW, scaledH, null);
        if (old == null) {
            g.getRenderingHints().remove(RenderingHints.KEY_INTERPOLATION);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
        }
    }

    private static int[] enemyVisibleSpriteRect(VqsvIntroDemo.Scene s) {
        int x = sourceBattleActorX(s, false) + enemyOffsetX(s);
        int y = sourceBattleActorY(s, false) + enemyOffsetY(s);
        if (s.battleEnemyVisualId < 0) {
            return new int[]{x, y, ENEMY_RECT_W, ENEMY_RECT_H};
        }
        SpriteAnim enemy = SpriteAnim.load(s.battleEnemyVisualId);
        int[] bounds = enemy.animationBounds(Math.max(0, s.battleP7BaseStateEnemySide));
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return new int[]{x, y, ENEMY_RECT_W, ENEMY_RECT_H};
        }
        return new int[]{x + bounds[0], y + bounds[1], bounds[2], bounds[3]};
    }

    private static int[] enemyGroundMarkerRect(VqsvIntroDemo.Scene s) {
        SpriteAnim marker = SpriteAnim.load(294);
        marker.setState(0);
        int x = sourceBattleMarkerX(s, false) + sideOffsetX(s, false);
        int y = sourceBattleMarkerY(s, false) + sideOffsetY(s, false);
        int[] bounds = marker.animationBounds(0);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            return new int[]{x, y, 64, 24};
        }
        return new int[]{x + bounds[0], y + bounds[1], bounds[2], bounds[3]};
    }

    private static void brightenOpaquePixels(BufferedImage image, int add) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = argb >>> 24;
                if (alpha == 0) {
                    continue;
                }
                int r = Math.min(255, ((argb >>> 16) & 0xff) + add);
                int gr = Math.min(255, ((argb >>> 8) & 0xff) + add);
                int b = Math.min(255, (argb & 0xff) + add);
                image.setRGB(x, y, (alpha << 24) | (r << 16) | (gr << 8) | b);
            }
        }
    }

    private static void drawStatusSlots(Graphics2D g, int iconStartX, int iconY,
                                        int overlayStartX, int overlayY, boolean rightToLeft,
                                        int[] iconCells, int[] durationCells) {
        for (int i = 0; i < 6; i++) {
            int dx = rightToLeft ? -i * 15 : i * 15;
            drawSpriteCellTopLeft(g, 325, statusCell(iconCells, i, 0), iconStartX + dx, iconY);
            drawBattleUiCellTopLeft(g, statusCell(durationCells, i, 145), overlayStartX + dx, overlayY);
        }
    }

    private static int statusCell(int[] cells, int index, int fallback) {
        if (cells == null || index < 0 || index >= cells.length) {
            return fallback;
        }
        return cells[index];
    }

    private static void drawBattlePercent(Graphics2D g, FontBitmap font, int x, int y, int percent) {
        Color color = percent > 100 ? new Color(0xfff1a0) : percent < 100 ? new Color(0xb8d8ff) : Color.WHITE;
        drawTinyBattleText(g, font, percent + "%", x, y + 1, 28, color);
    }

    private static void drawTinyBattleText(Graphics2D g, FontBitmap font, String text, int x, int y, int width, Color color) {
        Shape oldClip = g.getClip();
        g.clipRect(x, y - 1, width, 18);
        font.drawTaggedLine(g, text, x, y,
                TextBox.visibleLength(TextBox.decodeMojibake(text)),
                color.getRGB() & 0xFFFFFF);
        g.setClip(oldClip);
    }

    private static void drawMarqueeTinyBattleText(Graphics2D g, FontBitmap font, String text,
                                                  int x, int y, int width, Color color, int tick) {
        String decoded = TextBox.decodeMojibake(text);
        int textWidth = font.taggedWidth(decoded);
        int offset = 0;
        if (textWidth > width) {
            int cycle = textWidth + width;
            offset = (Math.max(0, tick) + width / 2) % Math.max(1, cycle) - width;
        }
        Shape oldClip = g.getClip();
        g.clipRect(x, y - 1, width, 18);
        font.drawTaggedLine(g, decoded, x - offset, y,
                TextBox.visibleLength(decoded), color.getRGB() & 0xFFFFFF);
        g.setClip(oldClip);
    }

    private static void drawOutlinedTinyBattleText(Graphics2D g, FontBitmap font, String text,
                                                   int x, int y, int width, Color color, Color outline) {
        drawTinyBattleText(g, font, text, x - 1, y, width, outline);
        drawTinyBattleText(g, font, text, x + 1, y, width, outline);
        drawTinyBattleText(g, font, text, x, y - 1, width, outline);
        drawTinyBattleText(g, font, text, x, y + 1, width, outline);
        drawTinyBattleText(g, font, text, x, y, width, color);
    }

    private static int idleCursor(int spriteIndex, int state, int tick) {
        if (spriteIndex < 0) {
            return 0;
        }
        SpriteAnim anim = SpriteAnim.load(spriteIndex);
        anim.setState(Math.max(0, state));
        if (anim.data.anim == null || anim.data.anim.length == 0 || anim.data.anim[anim.state].length == 0) {
            return 0;
        }
        int elapsed = Math.max(0, tick);
        short[] frames = anim.data.anim[anim.state];
        int total = 0;
        for (int i = 0; i < frames.length; i += 2) {
            total += Math.max(1, frames[i]);
        }
        int wrapped = total <= 0 ? 0 : elapsed % total;
        int sum = 0;
        for (int i = 0; i < frames.length; i += 2) {
            sum += Math.max(1, frames[i]);
            if (wrapped < sum) {
                return i / 2;
            }
        }
        return 0;
    }

    private static int baseCursor(int spriteIndex, int state, int forcedCursor, int tick) {
        if (forcedCursor >= 0) {
            return forcedCursor;
        }
        return idleCursor(spriteIndex, state, tick);
    }

    private static void drawSourceHpBar(Graphics2D g, int x, int y, int w, int percent) {
        int fill = Math.max(0, Math.min(w - 2, percent * (w - 2) / 100));
        g.setColor(new Color(0x575757));
        g.fillRect(x, y, w, 8);
        g.setColor(new Color(0x0e2130));
        g.drawRect(x, y, w - 1, 7);
        g.setColor(new Color(0x59f148));
        if (fill > 0) {
            g.fillRect(x + 1, y + 1, fill, 6);
        }
        g.setColor(new Color(0xffffff));
        g.drawLine(x + 1, y + 1, x + w - 2, y + 1);
    }

    private static int hpPercent(int hp, int maxHp) {
        return Math.max(0, Math.min(100, hp * 100 / Math.max(1, maxHp)));
    }

    private static void drawBattleHpTrack(Graphics2D g, int x, int y, int w, int hp, int maxHp) {
        int safeMax = Math.max(1, maxHp);
        int fill = Math.max(0, Math.min(w - 2, hp * (w - 2) / safeMax));
        g.setColor(new Color(0x646464));
        g.fillRect(x, y, w, 6);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, 6);
        g.setColor(new Color(0x58c548));
        g.fillRect(x + 1, y + 1, fill, 5);
    }

    private static void drawBattleHpBar(Graphics2D g, int x, int y, int hp, int maxHp, int color) {
        int safeMax = Math.max(1, maxHp);
        int fill = Math.max(0, Math.min(96, hp * 96 / safeMax));
        g.setColor(Color.WHITE);
        g.drawRect(x, y, 100, 8);
        g.setColor(new Color(color));
        g.fillRect(x + 2, y + 2, fill, 5);
    }
}
