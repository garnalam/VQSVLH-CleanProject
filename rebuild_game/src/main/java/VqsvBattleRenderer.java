import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

final class VqsvBattleRenderer {
    private static final int W = 240;
    private static final int H = 320;

    private VqsvBattleRenderer() {
    }

    static void render(VqsvIntroDemo.Scene s, Graphics2D g) {
        if (s.battleOverlayTicks <= 0) {
            return;
        }
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(36, 72, 104));
        g.fillRect(0, 36, W, 118);
        g.setColor(new Color(8, 18, 28));
        g.fillRect(0, 154, W, 166);
        g.setColor(Color.WHITE);
        g.drawRect(8, 46, 224, 88);
        g.drawRect(8, 188, 224, 76);
        String branch = "auto result " + s.battleResultIndex + " -> branch " + s.battleBranchTarget;
        s.font.drawTagged(g, "#FFFFFF" + branch, 16, 206, 208, branch.length());
        s.font.drawTagged(g, "#FFFFFFScripted stub", 16, 228, 208, 13);
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
        drawBattleSprite(g, s.battleEnemyVisualId, 132, 70, 96, 118, 7, 0);
        drawBattleSprite(g, s.battlePlayerVisualId, 18, 140, 96, 95, 7, 0);

        drawBattleUiCellTopLeft(g, 101, 97, 14);
        drawBattleCommandBar(g, s.font);

        s.font.drawTagged(g, "#FFFFFF" + s.battleEnemyName, 3, 2, 58, s.battleEnemyName.length());
        s.font.drawTagged(g, "#FFFFFFlv" + s.battleEnemyLevel, 64, 2, 36, 4);
        drawBattleProgressWidget(g, 5, 16, 82, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp), 0x9B9B9B);
        drawBattleProgressWidget(g, 5, 16, 82, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp), 0x59F148);
        drawBattleProgressWidget(g, 5, 16, 82, hpPercent(s.battleEnemyHp, s.battleEnemyMaxHp), 0xFFFFFF);
        String enemyHp = s.battleEnemyHp + "/" + s.battleEnemyMaxHp;
        s.font.drawTagged(g, "#fff9b1" + enemyHp, 16, 13, 72, enemyHp.length());
        drawBattleUiCellTopLeft(g, 94 + Math.max(0, s.battleEnemyElement), 92, 2);
        drawBattlePercent(g, s.font, 124, 2, s.battleEnemyPowerPercent);
        drawStatusSlots(g, 2, 25, 10, 30, false);

        s.font.drawTagged(g, "#FFFFFF" + s.battlePlayerName, 153, 238, 58, s.battlePlayerName.length());
        s.font.drawTagged(g, "#FFFFFFlv" + s.battlePlayerLevel, 214, 238, 26, 4);
        drawBattleProgressWidget(g, 153, 252, 82, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp), 0x9B9B9B);
        drawBattleProgressWidget(g, 153, 252, 82, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp), 0x59F148);
        drawBattleProgressWidget(g, 153, 252, 82, hpPercent(s.battlePlayerHp, s.battlePlayerMaxHp), 0xFFFFFF);
        String playerHp = s.battlePlayerHp + "/" + s.battlePlayerMaxHp;
        s.font.drawTagged(g, "#fff9b1" + playerHp, 167, 249, 66, playerHp.length());
        String playerEnergy = s.battlePlayerEnergy + "/" + s.battlePlayerMaxEnergy;
        s.font.drawTagged(g, "#fff9b1" + playerEnergy, 81, 258, 72, playerEnergy.length());
        drawBattleUiCellTopLeft(g, 94 + Math.max(0, s.battlePlayerElement), 139, 249);
        drawBattlePercent(g, s.font, 104, 248, s.battlePlayerPowerPercent);
        drawStatusSlots(g, 226, 221, 234, 226, true);

        s.font.drawTagged(g, "#FFFFFF" + s.battleLog, 29, 261, 202, s.battleLog.length());
        if (s.battleCaptureTutorial) {
            s.font.drawTagged(g, "#fff9b1" + VqsvText.Battle.CAPTURE_LABEL, 48, 299, 28,
                    VqsvText.Battle.CAPTURE_LABEL.length());
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
        if (spriteIndex < 0) {
            return;
        }
        SpriteAnim.load(spriteIndex).drawAligned(g, x, y, w, h, align, orientation);
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

    private static void drawBattleCommandBar(Graphics2D g, FontBitmap font) {
        String[][] labels = VqsvText.Battle.COMMAND_LABELS;
        int[] textXs = {7, 48, 88, 128, 168, 208};
        int[] iconXs = {20, 56, 98, 137, 176, 218};
        for (int i = 0; i < iconXs.length; i++) {
            drawBattleUiCellTopLeft(g, 31, iconXs[i], 293);
        }
        for (int i = 0; i < labels.length; i++) {
            drawTinyBattleText(g, font, labels[i][0], textXs[i], 299, 34, Color.WHITE);
            drawTinyBattleText(g, font, labels[i][1], textXs[i], 309, 34, Color.WHITE);
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

    private static void drawBattleProgressWidget(Graphics2D g, int x, int y, int w, int percent, int color) {
        int fill = Math.max(0, Math.min(w, percent * w / 100));
        if (fill <= 1) {
            return;
        }
        g.setColor(new Color(color));
        g.fillRect(x + 1, y + 1, fill - 1, 7);
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
