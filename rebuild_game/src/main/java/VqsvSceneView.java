import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

final class VqsvSceneView {
    private VqsvSceneView() {
    }

    static void render(VqsvIntroDemo.Scene s, Graphics2D g) {
        renderWorld(s, g, true, true, true, false);
        VqsvBattleRenderer.render(s, g);
        if (s.battleOverlayTicks <= 0 && !s.panelRuntime.visible && !s.sourceReleaseConfirmVisible) {
            s.worldUi.render(g, s.useMap);
        }
        if (s.battleOverlayTicks <= 0 && s.worldPetstateVisible) {
            VqsvBattleRenderer.renderPetStateOverlay(g, s.font, s, false);
        }
        if (s.battleOverlayTicks <= 0 && s.sourcePetSettingVisible) {
            renderSourcePetSetting(s, g);
        }
        if (s.battleOverlayTicks <= 0 && s.sourceSkillVisible) {
            renderSourceSkillUi(s, g);
        }
        if (s.battleOverlayTicks <= 0 && s.sourceItemChoiceVisible) {
            renderSourceItemChoiceUi(s, g);
        }
        if (s.battleOverlayTicks <= 0 && s.sourceEquipmentChoiceVisible) {
            renderSourceChoiceUi(s, g, s.sourceEquipmentChoiceView());
        }
        if (s.battleOverlayTicks <= 0 && s.sourceReleaseConfirmVisible) {
            renderSourceReleaseConfirm(s, g);
        }
        if (s.battleOverlayTicks <= 0 && s.sourceEvolveVisible) {
            VqsvBattleRenderer.renderEvolutionOverlay(g, s.font, s);
        }
        if (s.battleOverlayTicks <= 0 && s.panelRuntime.visible) {
            s.panelRuntime.render(g, s.font, s);
        }
        if (s.text != null) {
            s.text.render(g, s.font);
        }
        if (s.choice != null) {
            s.choice.render(g, s.font);
        }
        if (s.savePromptVisible) {
            renderSavePrompt(s, g);
        }
    }

    static BufferedImage captureBattleBackground(VqsvIntroDemo.Scene s) {
        BufferedImage image = new BufferedImage(VqsvIntroDemo.W, VqsvIntroDemo.H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        renderWorld(s, g, false, false, false, true);
        g.dispose();
        return image;
    }

    private static void renderWorld(VqsvIntroDemo.Scene s, Graphics2D g,
                                    boolean renderEffects, boolean renderTempSprites,
                                    boolean renderActors, boolean renderSceneryActors) {
        g.setColor(s.useMap ? Color.BLACK : new Color(8, 16, 80));
        g.fillRect(0, 0, VqsvIntroDemo.W, VqsvIntroDemo.H);
        if (s.useMap) {
            renderMapLayer(s, g, 1);
            renderMapLayer(s, g, 2);
        }

        if (renderActors || renderSceneryActors) {
            renderActorLayer(s, g, 2, false, renderSceneryActors && !renderActors);
            renderActorLayer(s, g, 1, true, renderSceneryActors && !renderActors);
        }
        if (renderActors) {
            renderPlayer(s, g);
        }
        if (renderTempSprites) {
            for (TempSprite sprite : s.tempSprites) {
                sprite.render(g, s);
            }
        }
        if (s.useMap) {
            renderMapLayer(s, g, 3);
        }
        if (renderActors || renderSceneryActors) {
            renderActorLayer(s, g, 0, false, renderSceneryActors && !renderActors);
        }

        if (renderEffects) {
            s.effect.renderParticles(g);
            s.effect.renderOverlay(g);
        }
    }

    static void setCameraCenter(VqsvIntroDemo.Scene s, int cx, int cy) {
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.centerCameraOn(cx, cy);
            s.cameraX = s.mapRenderer.cameraX();
            s.cameraY = s.mapRenderer.cameraY();
        } else {
            s.cameraX = clamp(cx - VqsvIntroDemo.W / 2, 0, 640 - VqsvIntroDemo.W);
            s.cameraY = clamp(cy - VqsvIntroDemo.H / 2, 0, 480 - VqsvIntroDemo.H);
        }
    }

    static void moveCameraToward(VqsvIntroDemo.Scene s, int cx, int cy, int speed) {
        int targetX;
        int targetY;
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.centerCameraOn(cx, cy);
            targetX = s.mapRenderer.cameraX();
            targetY = s.mapRenderer.cameraY();
        } else {
            targetX = clamp(cx - VqsvIntroDemo.W / 2, 0, 640 - VqsvIntroDemo.W);
            targetY = clamp(cy - VqsvIntroDemo.H / 2, 0, 480 - VqsvIntroDemo.H);
        }
        if (speed <= 0) {
            s.cameraX = targetX;
            s.cameraY = targetY;
        } else {
            int dx = targetX - s.cameraX;
            int dy = targetY - s.cameraY;
            int distance = (int) Math.sqrt(dx * dx + dy * dy);
            if (distance <= speed) {
                s.cameraX = targetX;
                s.cameraY = targetY;
            } else {
                s.cameraX += dx * speed / distance;
                s.cameraY += dy * speed / distance;
            }
        }
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.setCamera(s.cameraX, s.cameraY);
            s.cameraX = s.mapRenderer.cameraX();
            s.cameraY = s.mapRenderer.cameraY();
        }
    }

    static boolean cameraCenteredOn(VqsvIntroDemo.Scene s, int cx, int cy) {
        int oldX = s.cameraX;
        int oldY = s.cameraY;
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.centerCameraOn(cx, cy);
            boolean same = oldX == s.mapRenderer.cameraX() && oldY == s.mapRenderer.cameraY();
            s.mapRenderer.setCamera(oldX, oldY);
            return same;
        }
        return oldX == clamp(cx - VqsvIntroDemo.W / 2, 0, 640 - VqsvIntroDemo.W)
                && oldY == clamp(cy - VqsvIntroDemo.H / 2, 0, 480 - VqsvIntroDemo.H);
    }

    static void followActor(VqsvIntroDemo.Scene s, int actorId) {
        s.followActorId = actorId;
        updateCameraFollow(s);
    }

    static void stopCameraFollow(VqsvIntroDemo.Scene s) {
        s.followActorId = -1;
    }

    static void updateCameraFollow(VqsvIntroDemo.Scene s) {
        if (s.followActorId < 0 || s.followActorId >= s.actors.length || s.actors[s.followActorId] == null) {
            return;
        }
        Actor actor = s.actors[s.followActorId];
        setCameraCenter(s, actor.x, actor.y);
    }

    private static void renderMapLayer(VqsvIntroDemo.Scene s, Graphics2D g, int layerIndex) {
        if (s.useMap && s.mapRenderer != null && s.mapRenderer.hasLayer(layerIndex)) {
            s.mapRenderer.renderLayer(g, layerIndex);
        }
    }

    private static void renderActorLayer(VqsvIntroDemo.Scene s, Graphics2D g,
                                         int layer, boolean sortByY, boolean sceneryOnly) {
        ArrayList<Actor> draw = new ArrayList<>();
        for (Actor a : s.actors) {
            if (a != null && a.visible && a.layer == layer && (!sceneryOnly || a.variant == 0)) {
                draw.add(a);
            }
        }
        if (sortByY) {
            draw.sort(Comparator.comparingInt(a -> a.y));
        }
        for (Actor a : draw) {
            a.render(g, s.cameraX, s.cameraY);
        }
    }

    private static void renderPlayer(VqsvIntroDemo.Scene s, Graphics2D g) {
        if (s.useMap && s.player.visible) {
            s.player.render(g, s.cameraX, s.cameraY);
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static void renderSavePrompt(VqsvIntroDemo.Scene s, Graphics2D g) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgtip.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        VqsvUiLayout.UiWidget frame = layout.widget(1);
        int frameCell = frame != null && frame.altId >= 0 ? frame.altId : 124;
        drawCellTopLeft(ui, g, frameCell, layout.x(1, 51), layout.y(1, 134));
        String message = s.savePromptStatus == null || s.savePromptStatus.isEmpty()
                ? s.savePromptMessage : s.savePromptStatus;
        drawSavePromptText(s, g, message, layout);
        if (s.savePromptStatus == null || s.savePromptStatus.isEmpty()) {
            drawSavePromptWidgetCell(ui, g, layout, 4, 75, 1, 298);
            drawSavePromptWidgetCell(ui, g, layout, 3, 133, 218, 298);
        }
    }

    private static void renderSourcePetSetting(VqsvIntroDemo.Scene s, Graphics2D g) {
        VqsvUiLayout layout = VqsvUiLayout.load("petsetting.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawPetSettingBand(g, layout, 1, 8, 0xc6f3ff);
        drawPetSettingBand(g, layout, 2, 104, 0xbde4ef);
        drawPetSettingBand(g, layout, 3, 12, 0x6ccb7b);
        drawPetSettingWidgetCell(ui, g, layout, 4, false);

        int[] rows = {5, 6, 7, 8, 10, 9};
        for (int i = 0; i < rows.length && i < s.sourcePetSettingCount; i++) {
            int widgetId = rows[i];
            boolean selected = i == s.sourcePetSettingIndex;
            drawPetSettingWidgetCell(ui, g, layout, widgetId, selected);
            drawPetSettingText(s, g, layout, widgetId,
                    s.sourcePetSettingActionLabel(i), selected);
        }
    }

    private static void drawPetSettingBand(Graphics2D g, VqsvUiLayout layout,
                                           int widgetId, int fallbackHeight, int fallbackColor) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int color = widget.jColor == 0 || widget.jColor == -1 ? fallbackColor : widget.jColor & 0xffffff;
        g.setColor(new Color(color));
        g.fillRect(widget.x, widget.y, Math.max(1, widget.w),
                Math.max(1, layout.bandHeight(widgetId, fallbackHeight)));
    }

    private static void drawPetSettingWidgetCell(SpriteAnim ui, Graphics2D g, VqsvUiLayout layout,
                                                 int widgetId, boolean selected) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int cell = selected && widget.altId >= 0 ? widget.altId : widget.imageId;
        if (cell < 0) {
            cell = widget.altId;
        }
        if (cell < 0) {
            return;
        }
        drawCellTopLeft(ui, g, cell, widget.x, widget.y);
    }

    private static void drawPetSettingText(VqsvIntroDemo.Scene s, Graphics2D g, VqsvUiLayout layout,
                                           int widgetId, String text, boolean selected) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        int color = selected && widget.jColor != 0 && widget.jColor != -1
                ? widget.jColor & 0xffffff
                : widget.lColor != 0 && widget.lColor != -1 ? widget.lColor & 0xffffff : 0x1c6c91;
        java.awt.Shape oldClip = g.getClip();
        int width = Math.max(widget.w, 76);
        g.clipRect(widget.x - 18, widget.y - 1, width, Math.max(12, layout.h(widgetId, 12)));
        int drawX = widget.x;
        if (widget.b == 4) {
            drawX = widget.x - 18 + Math.max(0, (width - s.font.taggedWidth(text)) / 2);
        }
        s.font.drawTaggedLine(g, text, drawX, widget.y,
                TextBox.visibleLength(TextBox.decodeMojibake(text)), color);
        g.setClip(oldClip);
    }

    private static void renderSourceSkillUi(VqsvIntroDemo.Scene s, Graphics2D g) {
        VqsvUiLayout layout = VqsvUiLayout.load("skill.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawSkillBand(g, layout, 4, 202, 0x89d8ef);
        drawSkillBand(g, layout, 3, 9, 0xc6f3ff);
        drawSkillBand(g, layout, 1, 159, 0xf7ffff);
        drawSkillBand(g, layout, 2, 11, 0x82d0fb);
        drawSkillCell(ui, g, layout, 4, false);
        drawSkillCell(ui, g, layout, 8, false);
        drawSkillCell(ui, g, layout, 15, false);
        drawSkillCell(ui, g, layout, 10, false);
        drawSkillCell(ui, g, layout, 11, false);

        drawSkillWidgetText(s, g, layout, 5, layout.text(5, "K\u1ef9 n\u0103ng"), 100,
                true, 0xffffff);
        drawSkillWidgetText(s, g, layout, 6, layout.text(6, "Quay l\u1ea1i"), 43,
                true, 0xffffff);
        drawSkillWidgetText(s, g, layout, 7, layout.text(7, "X\u00e1c \u0111\u1ecbnh"), 43,
                true, 0xffffff);
        drawSkillWidgetText(s, g, layout, 12, s.sourceSkillPetName(), 72,
                false, sourceWidgetColor(layout, 12, 0x1c6c91));
        drawSkillWidgetText(s, g, layout, 13, layout.text(13, "lv"), 12,
                false, sourceWidgetColor(layout, 13, 0x1c6c91));
        drawSkillWidgetText(s, g, layout, 14, String.valueOf(s.sourceSkillPetLevel()), 24,
                false, sourceWidgetColor(layout, 14, 0xf22549));
        drawSkillPetSprite(g, layout, s.sourceSkillPetVisualId());

        int[] skillWidgets = {18, 19, 20, 21, 22};
        for (int i = 0; i < skillWidgets.length; i++) {
            boolean selected = i == s.sourceSkillIndex;
            drawSkillCell(ui, g, layout, skillWidgets[i], selected);
            int color = selected ? sourceWidgetJColor(layout, skillWidgets[i], 0xfff16a)
                    : sourceWidgetColor(layout, skillWidgets[i], 0x9bffb2);
            drawSkillWidgetText(s, g, layout, skillWidgets[i], s.sourceSkillNameAt(i), 100,
                    true, color);
        }
        drawSkillWidgetText(s, g, layout, 9, s.sourceSkillDescription(), 177,
                false, sourceWidgetColor(layout, 9, 0xd0007e));
    }

    private static void drawSkillBand(Graphics2D g, VqsvUiLayout layout,
                                      int widgetId, int fallbackHeight, int fallbackColor) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int color = widget.jColor == 0 || widget.jColor == -1 ? fallbackColor : widget.jColor & 0xffffff;
        g.setColor(new Color(color));
        g.fillRect(widget.x, widget.y, Math.max(1, widget.w),
                Math.max(1, layout.bandHeight(widgetId, fallbackHeight)));
    }

    private static void drawSkillCell(SpriteAnim ui, Graphics2D g, VqsvUiLayout layout,
                                      int widgetId, boolean selected) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int cell = selected && widget.altId >= 0 ? widget.altId : widget.imageId;
        if (cell < 0) {
            cell = widget.altId;
        }
        drawCellTopLeft(ui, g, cell, widget.x, widget.y);
    }

    private static void drawSkillPetSprite(Graphics2D g, VqsvUiLayout layout, int visualId) {
        VqsvUiLayout.UiWidget widget = layout.widget(16);
        if (widget == null || visualId < 0) {
            return;
        }
        SpriteAnim sprite = SpriteAnim.load(visualId);
        sprite.setState(0);
        sprite.cursor = 0;
        java.awt.Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y, Math.max(1, widget.w), Math.max(1, layout.h(16, 88)));
        sprite.drawAligned(g, widget.x, widget.y, Math.max(1, widget.w), Math.max(1, layout.h(16, 88)), 7, 0);
        g.setClip(oldClip);
    }

    private static void drawSkillWidgetText(VqsvIntroDemo.Scene s, Graphics2D g, VqsvUiLayout layout,
                                            int widgetId, String text, int fallbackWidth,
                                            boolean center, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        String decoded = TextBox.decodeMojibake(text);
        int width = layout.w(widgetId, fallbackWidth);
        java.awt.Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y - 1, Math.max(1, width), Math.max(12, layout.h(widgetId, 13)));
        int drawX = widget.x;
        if (center || widget.b == 4) {
            drawX = widget.x + Math.max(0, (width - s.font.taggedWidth(decoded)) / 2);
        }
        s.font.drawTaggedLine(g, decoded, drawX, widget.y,
                TextBox.visibleLength(decoded), color);
        g.setClip(oldClip);
    }

    private static int sourceWidgetColor(VqsvUiLayout layout, int widgetId, int fallback) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        return widget == null || widget.lColor == 0 || widget.lColor == -1
                ? fallback : widget.lColor & 0xffffff;
    }

    private static int sourceWidgetJColor(VqsvUiLayout layout, int widgetId, int fallback) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        return widget == null || widget.jColor == 0 || widget.jColor == -1
                ? fallback : widget.jColor & 0xffffff;
    }

    private static void renderSourceItemChoiceUi(VqsvIntroDemo.Scene s, Graphics2D g) {
        renderSourceChoiceUi(s, g, s.sourceItemChoiceView());
    }

    private static void renderSourceReleaseConfirm(VqsvIntroDemo.Scene s, Graphics2D g) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgconfirm.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        VqsvUiLayout.UiWidget frame = layout.widget(1);
        int frameCell = frame != null && frame.altId >= 0 ? frame.altId : 124;
        drawCellTopLeft(ui, g, frameCell, layout.x(1, 50), layout.y(1, 137));
        drawPromptMessageText(s, g, layout, s.sourceReleaseConfirmMessage,
                sourceWidgetColor(layout, 4, 0x1c6c91));
        drawPromptSoftkeyBackground(ui, g, layout, 2, 15, 1, 296);
        drawPromptSoftkeyText(s, g, layout, 2, s.sourceReleaseConfirmAction,
                true, sourceWidgetColor(layout, 2, 0xffffff));
        drawPromptSoftkeyBackground(ui, g, layout, 3, 15, 196, 296);
        drawPromptSoftkeyText(s, g, layout, 3, "Quay l\u1ea1i",
                false, sourceWidgetColor(layout, 3, 0xffffff));
    }

    private static void renderSourceChoiceUi(VqsvIntroDemo.Scene s, Graphics2D g,
                                             VqsvChoiceUiView choice) {
        VqsvUiLayout layout = VqsvUiLayout.load("choice.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawSkillBand(g, layout, 4, 8, 0xc6f1ff);
        drawSkillBand(g, layout, 2, 160, 0xbde4ef);
        drawSkillBand(g, layout, 3, 14, 0x82cafb);
        drawSkillBand(g, layout, 7, 82, 0xbde4ef);
        drawSkillCell(ui, g, layout, 1, false);

        drawChoiceWidgetText(s, g, layout, 8, choice.widgetText(8, "\u0110\u1ea1o c\u1ee5"),
                46, true, sourceWidgetColor(layout, 8, 0x1c6c91));
        drawChoiceWidgetText(s, g, layout, 9, choice.widgetText(9, "S\u1ed1 l\u01b0\u1ee3ng"),
                36, true, sourceWidgetColor(layout, 9, 0x1c6c91));

        int start = choice.visibleStart();
        int visibleRows = choice.visibleCount();
        SpriteAnim itemIcons = SpriteAnim.load(VqsvChoiceUiView.ROW_ICON_SPRITE_ID);
        for (int row = 0; row < 5; row++) {
            int absolute = start + row;
            boolean selected = absolute == choice.selectedIndex;
            int frameId = 11 + row * 5;
            int iconId = 54 + row;
            int nameId = 13 + row * 5;
            int valueId = 14 + row * 5;
            drawSkillCell(ui, g, layout, frameId, selected);
            if (row >= visibleRows) {
                continue;
            }
            if (choice.rowIconVisible(row)) {
                drawCellTopLeft(itemIcons, g, choice.rowIconCell(row),
                        layout.x(iconId, 54), layout.y(iconId, 95 + row * 15));
            }
            int nameColor = selected ? sourceWidgetJColor(layout, nameId, 0xfff16a)
                    : sourceWidgetColor(layout, nameId, 0x1c6c91);
            drawChoiceWidgetText(s, g, layout, nameId, choice.widgetText(nameId, ""),
                    72, false, nameColor);
            int valueColor = selected ? sourceWidgetJColor(layout, valueId, nameColor)
                    : sourceWidgetColor(layout, valueId, 0x1c6c91);
            drawChoiceWidgetText(s, g, layout, valueId, choice.widgetText(valueId, ""),
                    36, true, valueColor);
        }
        if (choice.size() > 5) {
            drawSkillBand(g, layout, 50, 72, 0x51d8e9);
            int knobY = choice.scrollbarThumbY(layout.y(50, 98), 72);
            VqsvUiLayout.UiWidget knob = layout.widget(51);
            g.setColor(new Color(sourceWidgetJColor(layout, 51, 0xc6f1ff)));
            g.fillRect(layout.x(51, 183), knobY, layout.w(51, 4),
                    Math.max(1, knob == null ? 8 : layout.h(51, 8)));
        }
        if (choice.size() == 0) {
            drawChoiceWidgetText(s, g, layout, 13, "...", 72, true,
                    sourceWidgetColor(layout, 13, 0x1c6c91));
        }
        String description = choice.selectedDescription();
        if (!description.isEmpty()) {
            drawSkillCell(ui, g, layout, 52, false);
            drawChoiceWidgetText(s, g, layout, 53, description, 125, false, 0xffffff);
        }
        drawSkillCell(ui, g, layout, 59, false);
        drawChoiceWidgetText(s, g, layout, 59, choice.widgetText(59, "S\u1eed d\u1ee5ng"),
                43, true, sourceWidgetColor(layout, 59, 0xffffff));
        drawSkillCell(ui, g, layout, 60, false);
        drawChoiceWidgetText(s, g, layout, 60, choice.widgetText(60, "Quay l\u1ea1i"),
                43, true, sourceWidgetColor(layout, 60, 0xffffff));
    }

    private static void drawChoiceWidgetText(VqsvIntroDemo.Scene s, Graphics2D g, VqsvUiLayout layout,
                                             int widgetId, String text, int fallbackWidth,
                                             boolean center, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        String decoded = TextBox.decodeMojibake(text);
        int width = layout.w(widgetId, fallbackWidth);
        java.awt.Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y - 1, Math.max(1, width), Math.max(12, layout.h(widgetId, 13)));
        int drawX = widget.x;
        if (center || widget.b == 4) {
            drawX = widget.x + Math.max(0, (width - s.font.taggedWidth(decoded)) / 2);
        }
        s.font.drawTaggedLine(g, decoded, drawX, widget.y,
                TextBox.visibleLength(decoded), color);
        g.setClip(oldClip);
    }

    private static void drawSavePromptText(VqsvIntroDemo.Scene s, Graphics2D g, String message,
                                           VqsvUiLayout layout) {
        VqsvUiLayout.UiWidget text = layout.widget(2);
        int x = layout.x(2, 56);
        int y = layout.y(2, 137);
        int w = layout.w(2, 138);
        int h = layout.h(2, 16);
        int color = text == null || text.lColor == 0 || text.lColor == -1
                ? 0x1c6c91 : text.lColor & 0xffffff;
        java.awt.Shape oldClip = g.getClip();
        g.clipRect(x, y, w, Math.max(1, h));
        String decoded = TextBox.decodeMojibake(message);
        int textWidth = s.font.taggedWidth(decoded);
        int drawX = x + Math.max(0, (w - textWidth) / 2);
        s.font.drawTaggedLine(g, decoded, drawX, y,
                TextBox.visibleLength(decoded), color);
        g.setClip(oldClip);
    }

    private static void drawSavePromptWidgetCell(SpriteAnim ui, Graphics2D g, VqsvUiLayout layout,
                                                 int widgetId, int fallbackCell, int fallbackX, int fallbackY) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int cell = widget != null && widget.altId >= 0 ? widget.altId : fallbackCell;
        drawCellTopLeft(ui, g, cell, layout.x(widgetId, fallbackX), layout.y(widgetId, fallbackY));
    }

    private static void drawPromptMessageText(VqsvIntroDemo.Scene s, Graphics2D g,
                                              VqsvUiLayout layout, String message, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(4);
        if (widget == null || message == null || message.isEmpty()) {
            return;
        }
        String decoded = TextBox.decodeMojibake(message);
        int x = layout.x(4, 50);
        int y = layout.y(4, 137);
        int w = layout.w(4, 150);
        int h = 28;
        int drawX = x + Math.max(0, (w - s.font.taggedWidth(decoded)) / 2);
        int drawY = y + Math.max(0, (h - 10) / 2);
        java.awt.Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        s.font.drawTaggedLine(g, decoded, drawX, drawY,
                TextBox.visibleLength(decoded), color);
        g.setClip(oldClip);
    }

    private static void drawPromptSoftkeyText(VqsvIntroDemo.Scene s, Graphics2D g, VqsvUiLayout layout,
                                              int widgetId, String text, boolean left, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        String decoded = TextBox.decodeMojibake(text);
        int textWidth = s.font.taggedWidth(decoded);
        int sourceWidth = Math.max(1, layout.w(widgetId, 44));
        int sourceHeight = Math.max(14, layout.h(widgetId, 13));
        BufferedImage textImage = new BufferedImage(Math.max(1, textWidth + 4), sourceHeight + 4,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = textImage.createGraphics();
        tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        s.font.drawTaggedLine(tg, decoded, 2, 2, TextBox.visibleLength(decoded), color);
        tg.dispose();
        int[] textBounds = opaqueBounds(textImage);
        if (textBounds == null) {
            return;
        }
        BufferedImage trimmed = textImage.getSubimage(textBounds[0], textBounds[1],
                textBounds[2], textBounds[3]);
        int drawWidth = trimmed.getWidth();
        int drawHeight = trimmed.getHeight();
        int maxWidth = Math.max(1, sourceWidth - 2);
        int maxHeight = Math.max(1, sourceHeight - 2);
        if (drawWidth > maxWidth) {
            drawHeight = Math.max(1, drawHeight * maxWidth / drawWidth);
            drawWidth = maxWidth;
        }
        if (drawHeight > maxHeight) {
            drawWidth = Math.max(1, drawWidth * maxHeight / drawHeight);
            drawHeight = maxHeight;
        }
        int drawX = widget.x + Math.max(0, (sourceWidth - drawWidth) / 2);
        int drawY = widget.y + Math.max(0, (sourceHeight - drawHeight) / 2);
        java.awt.Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y - 1, sourceWidth, sourceHeight);
        g.drawImage(trimmed, drawX, drawY, drawWidth, drawHeight, null);
        g.setClip(oldClip);
    }

    private static int[] opaqueBounds(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xff) == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new int[]{minX, minY, maxX - minX + 1, maxY - minY + 1};
    }

    private static void drawPromptSoftkeyBackground(SpriteAnim ui, Graphics2D g, VqsvUiLayout layout,
                                                    int widgetId, int fallbackCell,
                                                    int fallbackX, int fallbackY) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int x = layout.x(widgetId, fallbackX);
        int y = layout.y(widgetId, fallbackY);
        int w = Math.max(1, layout.w(widgetId, 44));
        int h = Math.max(14, layout.h(widgetId, 16));
        if (widget != null && widget.altId >= 0 && widget.altMode == 3) {
            ui.setState(widget.altId);
            ui.drawAligned(g, x, y, w, h, widget.c, 0);
            return;
        }
        if (widget != null && widget.altId >= 0 && widget.altMode == 2) {
            drawSavePromptWidgetCell(ui, g, layout, widgetId, fallbackCell, fallbackX, fallbackY);
            return;
        }
        g.setColor(new Color(0x081050));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0x1ba8e8));
        g.drawRect(x, y, w - 1, h - 1);
    }

    private static void drawCellTopLeft(SpriteAnim ui, Graphics2D g, int cellId, int x, int y) {
        int[] bounds = ui.cellBounds(cellId);
        if (bounds == null) {
            return;
        }
        ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }

    private static void drawConfirmSoftkey(Graphics2D g, int x, int y, boolean confirm) {
        Stroke oldStroke = g.getStroke();
        g.setColor(new Color(0x1BA8E8));
        g.fillOval(x, y, 24, 24);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (confirm) {
            g.drawLine(x + 5, y + 13, x + 10, y + 18);
            g.drawLine(x + 10, y + 18, x + 20, y + 6);
        } else {
            g.drawLine(x + 7, y + 7, x + 17, y + 17);
            g.drawLine(x + 17, y + 7, x + 7, y + 17);
        }
        g.setStroke(oldStroke);
    }
}
