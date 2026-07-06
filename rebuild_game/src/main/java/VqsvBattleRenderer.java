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
import java.util.HashMap;
import java.util.Map;

final class VqsvBattleRenderer {
    private static final int W = 240;
    private static final int H = 320;
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
        g.setColor(new Color(9, 42, 58));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(22, 82, 94));
        g.fillRect(0, 72, W, 92);
        g.setColor(new Color(17, 54, 82));
        g.fillRect(0, 164, W, 71);
        drawBattleUiCellTopLeft(g, 92, 0, 0);
        drawBattleUiCellTopLeft(g, 93, 0, 235);
        drawBattleUiCellTopLeft(g, 158, 101, 1);
        if (!s.battleEnemyHiddenByCatch) {
            drawBattleSprite(g, s.battleEnemyVisualId, 132, 70, 96, 118, 7, 0,
                    s.battleP7Phase == 1 && !s.battleP7EffectOnPlayerSide
                            ? s.battleP7EffectAnimState : 0,
                    s.battleP7Phase == 1 && !s.battleP7EffectOnPlayerSide
                            ? s.battleP7EffectAnimCursor : 0);
        }
        drawBattleSprite(g, s.battlePlayerVisualId, 18, 140, 96, 95, 7, 0,
                s.battleP7Phase == 1 && s.battleP7EffectOnPlayerSide
                        ? s.battleP7EffectAnimState : 0,
                s.battleP7Phase == 1 && s.battleP7EffectOnPlayerSide
                        ? s.battleP7EffectAnimCursor : 0);
        drawP7SpecialEffect(g, s);

        drawBattleUiCellTopLeft(g, 101, 97, 14);
        drawBattleCommandBar(g, s.font, s.battleCommandIndex);

        s.font.drawTagged(g, "#FFFFFF" + s.battleEnemyName, 3, 2, 58, s.battleEnemyName.length());
        s.font.drawTagged(g, "#FFFFFFlv" + s.battleEnemyLevel, 64, 2, 36, 4);
        drawSourceHpBar(g, 5, 16, 82, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp));
        String enemyHp = s.battleEnemyHp + "/" + s.battleEnemyMaxHp;
        s.font.drawTagged(g, "#fff9b1" + enemyHp, 16, 13, 72, enemyHp.length());
        drawBattleUiCellTopLeft(g, 94 + Math.max(0, s.battleEnemyElement), 92, 2);
        drawBattlePercent(g, s.font, 124, 2, s.battleEnemyPowerPercent);
        drawStatusSlots(g, 2, 25, 10, 30, false);

        s.font.drawTagged(g, "#FFFFFF" + s.battlePlayerName, 153, 238, 58, s.battlePlayerName.length());
        s.font.drawTagged(g, "#FFFFFFlv" + s.battlePlayerLevel, 214, 238, 26, 4);
        drawSourceHpBar(g, 153, 252, 82, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp));
        String playerHp = s.battlePlayerHp + "/" + s.battlePlayerMaxHp;
        s.font.drawTagged(g, "#fff9b1" + playerHp, 167, 249, 66, playerHp.length());
        String playerEnergy = s.battlePlayerEnergy + "/" + s.battlePlayerMaxEnergy;
        s.font.drawTagged(g, "#fff9b1" + playerEnergy, 81, 258, 72, playerEnergy.length());
        drawBattleUiCellTopLeft(g, 94 + Math.max(0, s.battlePlayerElement), 139, 249);
        drawBattlePercent(g, s.font, 104, 248, s.battlePlayerPowerPercent);
        drawStatusSlots(g, 226, 221, 234, 226, true);

        s.font.drawTagged(g, "#FFFFFF" + s.battleLog, 158, 260, 76, s.battleLog.length());
        if (s.battleCaptureTutorial) {
            s.font.drawTagged(g, "#fff9b1" + VqsvText.Battle.CAPTURE_LABEL, 48, 299, 28,
                    VqsvText.Battle.CAPTURE_LABEL.length());
        }
        if ("choice".equals(s.battleUiMode)) {
            drawChoiceOverlay(g, s.font, s);
        } else if ("choiceskill".equals(s.battleUiMode)) {
            drawChoiceSkillOverlay(g, s.font, s);
        } else if ("target".equals(s.battleUiMode)) {
            drawTargetCursor(g, s.font, s);
        } else if ("warning".equals(s.battleUiMode)) {
            drawWarningOverlay(g, s.font, s);
        }
        if (s.battleCatchVisible && s.battleCatchSpriteId >= 0) {
            drawCatchAnimation(g, s);
        }
        if (s.battleP7DamageVisible) {
            drawP7Damage(g, s.font, s);
        }
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

    private static void drawChoiceOverlay(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        drawBattleUiCellTopLeft(g, 91, 41, 68);
        drawBattlePanel(g, 44, 70, 151, 168, true);
        drawTinyBattleText(g, font, s.battleMenuTitle, 50, 78, 82, Color.WHITE);
        drawTinyBattleText(g, font, s.battleMenuSubtitle, 128, 78, 62, new Color(0xfff16a));
        int rowY = 96;
        int rowH = 26;
        for (int i = 0; i < Math.min(5, s.battleMenuNames.length); i++) {
            int y = rowY + i * rowH;
            if (i == s.battleMenuIndex) {
                g.setColor(new Color(0x164f73));
                g.fillRect(48, y - 3, 142, 19);
                g.setColor(new Color(0xfff16a));
                g.drawRect(47, y - 4, 144, 21);
            }
            Color color = i == s.battleMenuIndex ? new Color(0xfff16a) : Color.WHITE;
            if (i < s.battleMenuIconIds.length && s.battleMenuIconIds[i] >= 0) {
                drawSpriteCellTopLeft(g, 258, s.battleMenuIconIds[i], 51, y - 2);
            }
            drawTinyBattleText(g, font, s.battleMenuNames[i], 67, y, 74, color);
            String value = i < s.battleMenuValues.length ? s.battleMenuValues[i] : "";
            drawTinyBattleText(g, font, value, 145, y, 42, color);
        }
        if (s.battleMenuNames.length == 0) {
            drawTinyBattleText(g, font, "...", 105, 136, 40, Color.WHITE);
        }
        drawTinyBattleText(g, font, s.battleMenuAction, 50, 235, 58, Color.WHITE);
        drawTinyBattleText(g, font, "Quay", 164, 235, 28, Color.WHITE);
        drawTinyBattleText(g, font, "l\u1ea1i", 164, 245, 28, Color.WHITE);
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
        drawWrappedTinyText(g, font, s.battleSkillDescription, 57, 180, 125, 5, Color.WHITE);
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
        int x = s.battleP7TargetPlayerSide ? 64 : 176;
        int y = s.battleP7TargetPlayerSide ? 128 : 58;
        int pulse = Math.min(5, Math.max(0, s.battleP7Ticks / 2));
        g.setColor(new Color(0x9b1010));
        g.drawOval(x - 12 - pulse, y - 8 - pulse, 24 + pulse * 2, 14 + pulse * 2);
        drawTinyBattleText(g, font, s.battleP7DamageText, x - 14, y - 16 - pulse,
                42, new Color(0xfff16a));
    }

    private static void drawP7SpecialEffect(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (!s.battleP7SpecialVisible || (s.battleP7SpecialType != 9 && s.battleP7SpecialType != 1)) {
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
        BufferedImage overlay = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D og = overlay.createGraphics();
        if (s.battleP7SpecialOnPlayerSide) {
            drawBattleSprite(og, sprite, 18, 140, 96, 95, 7, 0, 0, 0);
        } else {
            drawBattleSprite(og, sprite, 132, 70, 96, 118, 7, 0, 0, 0);
        }
        og.dispose();
        if (s.battleP7SpecialType == 9) {
            applyAhType9Transform(overlay, s.battleP7SpecialAlpha,
                    s.battleP7SpecialRed, s.battleP7SpecialGreen, s.battleP7SpecialBlue);
        } else {
            applyAhType1Texture(overlay, s);
        }
        g.drawImage(overlay, 0, 0, null);
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
        drawBattleUiCellTopLeft(g, 128, 76, 106);
        drawBattlePanel(g, 72, 106, 96, 82, true);
        drawWrappedTinyText(g, font, s.battleWarningTitle, 80, 120, 80, 4, Color.WHITE);
        drawWrappedTinyText(g, font, s.battleWarningPrompt, 82, 166, 76, 2, new Color(0xfff16a));
    }

    private static void drawCatchAnimation(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (s.battleCatchEffectVisible) {
            drawCatchEffectType8(g, s);
        }
        SpriteAnim ball = SpriteAnim.load(s.battleCatchSpriteId);
        ball.setState(Math.max(0, s.battleCatchPhase));
        ball.cursor = Math.max(0, s.battleCatchAnimCursor);
        ball.drawAligned(g, 132, 70, 96, 118, 7, 0);
    }

    private static void drawCatchEffectType8(Graphics2D g, VqsvIntroDemo.Scene s) {
        if (s.battleEnemyVisualId < 0) {
            return;
        }
        SpriteAnim enemy = SpriteAnim.load(s.battleEnemyVisualId);
        int[] bounds = enemy.cellBounds(0);
        if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
            drawBattleSprite(g, s.battleEnemyVisualId,
                    132 + s.battleCatchEffectDx, 70 + s.battleCatchEffectDy,
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
        int x = 132 + (96 - scaledW) / 2 + s.battleCatchEffectDx;
        int y = 70 + 118 - scaledH + s.battleCatchEffectDy;
        Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, x, y, scaledW, scaledH, null);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
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

    private static void drawWrappedTinyText(Graphics2D g, FontBitmap font, String text,
                                            int x, int y, int width, int maxLines, Color color) {
        String[] words = TextBox.decodeMojibake(text).split(" ");
        String line = "";
        int lineIndex = 0;
        for (String word : words) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font.width(next) > width) {
                drawTinyBattleText(g, font, line, x, y + lineIndex * 10, width, color);
                lineIndex++;
                line = word;
                if (lineIndex >= maxLines) {
                    return;
                }
            } else {
                line = next;
            }
        }
        if (!line.isEmpty() && lineIndex < maxLines) {
            drawTinyBattleText(g, font, line, x, y + lineIndex * 10, width, color);
        }
    }

    private static void drawStatusSlots(Graphics2D g, int iconStartX, int iconY, int overlayStartX, int overlayY, boolean rightToLeft) {
        for (int i = 0; i < 6; i++) {
            int dx = rightToLeft ? -i * 15 : i * 15;
            drawSpriteCellTopLeft(g, 325, 0, iconStartX + dx, iconY);
            drawBattleUiCellTopLeft(g, 145, overlayStartX + dx, overlayY);
        }
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
