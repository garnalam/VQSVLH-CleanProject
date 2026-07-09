package com.vqsv.rebuild.state;

import com.vqsv.rebuild.audio.VqsvMusicPlayer;
import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.input.InputSnapshot;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.ImageLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public final class BootFlowState implements GameState {
    private static final int LOGO_TICKS = 20;
    private static final int MENU_PARTICLE_COUNT = 10;
    private static final int[] MENU_OUTLINE_COLORS = {3958719, 3958719, 3958719, 7248110, 7248110, 9943031};
    private static final int[] PARTICLE_RECTS = {28, 3, 21, 22, 50, 5, 17, 17};
    private static final String[] MENU_WITHOUT_SAVE = {
            "Ch\u01a1i m\u1edbi", "T\u00f9y ch\u1ecdn", "Tr\u1ee3 gi\u00fap", "Gi\u1edbi thi\u1ec7u", "Tho\u00e1t"
    };
    private static final String[] MENU_WITH_SAVE = {
            "Ch\u01a1i ti\u1ebfp", "Ch\u01a1i m\u1edbi", "T\u00f9y ch\u1ecdn", "Tr\u1ee3 gi\u00fap", "Gi\u1edbi thi\u1ec7u", "Tho\u00e1t"
    };

    private final AssetPaths assets;
    private final BufferedImage logo0;
    private final BufferedImage cwaLogo;
    private final BufferedImage menuBackground;
    private final BufferedImage particleImage;
    private final Random random = new Random(System.currentTimeMillis());
    private final int[][] menuParticles = new int[MENU_PARTICLE_COUNT][5];
    private final int[] particleFinished = new int[MENU_PARTICLE_COUNT];
    private Phase phase = Phase.LOGO_0;
    private int phaseTicks;
    private int selectedMenu;
    private int colorTick;
    private boolean menuParticlesPaused;
    private int menuParticlePauseTicks;
    private boolean saveAvailable;
    private boolean musicEnabled;

    public BootFlowState(AssetPaths assets) {
        this.assets = assets;
        ImageLoader loader = new ImageLoader(assets);
        Path customLogo = assets.logoCustomPng("vqsvlogo");
        this.logo0 = Files.isRegularFile(customLogo) ? loader.load(customLogo) : loader.load(assets.logoDecodedPng("0"));
        this.cwaLogo = Files.isRegularFile(customLogo) ? loader.load(customLogo) : loader.load(assets.logoDecodedPng("cwalogo"));
        this.menuBackground = loader.load(assets.texDecodedPng("menu.mid"));
        this.particleImage = loader.loadDecodedImage(833);
        this.saveAvailable = hasRebuildSave();
        resetMenuParticles();
    }

    @Override
    public void tick(InputSnapshot input, GameStateMachine states) {
        phaseTicks++;
        switch (phase) {
            case LOGO_0:
                if (phaseTicks >= LOGO_TICKS) {
                    switchPhase(Phase.CWA_LOGO);
                }
                break;
            case CWA_LOGO:
                if (phaseTicks >= LOGO_TICKS) {
                    switchPhase(Phase.MUSIC_PROMPT);
                }
                break;
            case MUSIC_PROMPT:
                if (input.softLeftPressed() || input.confirmPressed()) {
                    chooseMusic(true);
                } else if (input.softRightPressed()) {
                    chooseMusic(false);
                }
                break;
            case TITLE_MENU:
                updateTitleMenu(input, states);
                break;
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        switch (phase) {
            case LOGO_0:
                renderLogo0(graphics);
                break;
            case CWA_LOGO:
                renderCenteredLogo(graphics, cwaLogo, Color.BLACK);
                break;
            case MUSIC_PROMPT:
                renderMusicPrompt(graphics);
                break;
            case TITLE_MENU:
                renderTitleMenu(graphics);
                break;
        }
    }

    public String phaseName() {
        return phase.name();
    }

    public String selectedMenuLabelForSmoke() {
        String[] labels = menuLabels();
        return labels[Math.max(0, Math.min(selectedMenu, labels.length - 1))];
    }

    public boolean saveAvailableForSmoke() {
        return menuLabels() == MENU_WITH_SAVE;
    }

    public boolean musicEnabledForSmoke() {
        return musicEnabled;
    }

    private void updateTitleMenu(InputSnapshot input, GameStateMachine states) {
        if (input.wasPressed(KeyEvent.VK_UP) || input.wasPressed(KeyEvent.VK_NUMPAD8)
                || input.wasPressed(KeyEvent.VK_8)
                || input.wasPressed(KeyEvent.VK_LEFT) || input.wasPressed(KeyEvent.VK_NUMPAD4)
                || input.wasPressed(KeyEvent.VK_4)) {
            previousMenu();
        } else if (input.wasPressed(KeyEvent.VK_DOWN) || input.wasPressed(KeyEvent.VK_NUMPAD2)
                || input.wasPressed(KeyEvent.VK_2)
                || input.wasPressed(KeyEvent.VK_RIGHT) || input.wasPressed(KeyEvent.VK_NUMPAD6)
                || input.wasPressed(KeyEvent.VK_6)) {
            nextMenu();
        } else if (clickedPreviousMenu(input)) {
            previousMenu();
        } else if (clickedNextMenu(input)) {
            nextMenu();
        } else if (input.confirmPressed() || clickedSelectedMenu(input)) {
            if (saveAvailable && selectedMenu == 0) {
                states.replace(new LegacyIntroDemoState(true));
            } else if ((!saveAvailable && selectedMenu == 0) || (saveAvailable && selectedMenu == 1)) {
                states.replace(new LegacyIntroDemoState());
            }
        }
        if (selectedMenu >= menuLabels().length) {
            selectedMenu = 0;
        }
        if (menuParticlesPaused) {
            menuParticlePauseTicks++;
            if (menuParticlePauseTicks >= 100) {
                resetMenuParticles();
            }
        }
    }

    private String[] menuLabels() {
        boolean nowAvailable = hasRebuildSave();
        if (nowAvailable != saveAvailable) {
            saveAvailable = nowAvailable;
            if (selectedMenu >= menuLabelsLength()) {
                selectedMenu = 0;
            }
        }
        return saveAvailable ? MENU_WITH_SAVE : MENU_WITHOUT_SAVE;
    }

    private void chooseMusic(boolean enabled) {
        musicEnabled = enabled;
        if (enabled) {
            VqsvMusicPlayer.startLoop(assets, "0");
        } else {
            VqsvMusicPlayer.stop();
        }
        switchPhase(Phase.TITLE_MENU);
        resetMenuParticles();
    }

    private void previousMenu() {
        selectedMenu--;
        if (selectedMenu < 0) {
            selectedMenu = menuLabels().length - 1;
        }
    }

    private void nextMenu() {
        selectedMenu++;
        if (selectedMenu >= menuLabels().length) {
            selectedMenu = 0;
        }
    }

    private int menuLabelsLength() {
        return saveAvailable ? MENU_WITH_SAVE.length : MENU_WITHOUT_SAVE.length;
    }

    private boolean hasRebuildSave() {
        try {
            Class<?> saveClass = Class.forName("VqsvSaveRuntime");
            java.lang.reflect.Method hasSave = saveClass.getDeclaredMethod("hasSave");
            hasSave.setAccessible(true);
            return ((Boolean) hasSave.invoke(null)).booleanValue();
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private void renderLogo0(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        drawImageCentered(graphics, logo0);
    }

    private void renderCenteredLogo(Graphics2D graphics, BufferedImage logo, Color background) {
        graphics.setColor(background);
        graphics.fillRect(0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        drawImageCentered(graphics, logo);
    }

    private void renderMusicPrompt(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        drawCenteredString(graphics, "B\u1ea1n c\u00f3 mu\u1ed1n b\u1eadt nh\u1ea1c kh\u00f4ng?", GameConfig.LOGICAL_HEIGHT / 2 - 12, Color.WHITE);
        drawCenteredString(graphics, "T\u1eaft nh\u1ea1c c\u00f3 th\u1ec3 t\u0103ng t\u1ed1c \u0111\u1ed9 ch\u01a1i",
                GameConfig.LOGICAL_HEIGHT / 2 + 12, new Color(0xFF6600));
        graphics.setColor(Color.WHITE);
        graphics.drawString("C\u00f3", 2, GameConfig.LOGICAL_HEIGHT - 2);
        drawRightString(graphics, "Kh\u00f4ng", GameConfig.LOGICAL_WIDTH - 2, GameConfig.LOGICAL_HEIGHT - 2);
    }

    private void renderTitleMenu(Graphics2D graphics) {
        graphics.drawImage(menuBackground, 0, 0, null);
        renderMenuParticles(graphics);
        String[] labels = menuLabels();
        String text = labels[Math.max(0, Math.min(selectedMenu, labels.length - 1))];
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics metrics = graphics.getFontMetrics();
        int x = (GameConfig.LOGICAL_WIDTH - metrics.stringWidth(text)) / 2;
        int y = GameConfig.LOGICAL_HEIGHT - 20;
        Color outline = new Color(MENU_OUTLINE_COLORS[colorTick]);
        graphics.setColor(outline);
        graphics.drawString(text, x, y - 1);
        graphics.drawString(text, x, y + 1);
        graphics.drawString(text, x - 1, y);
        graphics.drawString(text, x + 1, y);
        graphics.setColor(Color.WHITE);
        graphics.drawString(text, x, y);
        colorTick++;
        if (colorTick >= MENU_OUTLINE_COLORS.length) {
            colorTick = 0;
        }
    }

    private void renderMenuParticles(Graphics2D graphics) {
        if (particleImage == null || menuParticlesPaused) {
            return;
        }
        for (int index = 0; index < MENU_PARTICLE_COUNT; index++) {
            int frame = menuParticles[index][2];
            int rectBase = frame << 2;
            int sx = PARTICLE_RECTS[rectBase];
            int sy = PARTICLE_RECTS[rectBase + 1];
            int sw = PARTICLE_RECTS[rectBase + 2];
            int sh = PARTICLE_RECTS[rectBase + 3];
            int x = menuParticles[index][0];
            int y = menuParticles[index][1];
            graphics.drawImage(particleImage, x, y, x + sw, y + sh, sx, sy, sx + sw, sy + sh, null);
            menuParticles[index][0] += menuParticles[index][3];
            menuParticles[index][1] -= menuParticles[index][4];
            if (menuParticles[index][0] > GameConfig.LOGICAL_WIDTH || menuParticles[index][1] < 0) {
                particleFinished[index]++;
            }
        }
        for (int index = 0; index < MENU_PARTICLE_COUNT; index++) {
            if (particleFinished[index] <= 0) {
                return;
            }
        }
        menuParticlesPaused = true;
    }

    private void resetMenuParticles() {
        for (int index = 0; index < MENU_PARTICLE_COUNT; index++) {
            menuParticles[index][0] = -randomLessThan(30);
            menuParticles[index][1] = GameConfig.LOGICAL_HEIGHT + randomLessThan(30);
            menuParticles[index][2] = randomLessThan(2);
            menuParticles[index][3] = randomBetweenInclusive(1, 5);
            menuParticles[index][4] = randomBetweenInclusive(3, 5);
            particleFinished[index] = 0;
        }
        menuParticlePauseTicks = 0;
        menuParticlesPaused = false;
    }

    private int randomLessThan(int maxExclusive) {
        return (random.nextInt() >>> 1) % maxExclusive;
    }

    private int randomBetweenInclusive(int min, int max) {
        return (random.nextInt() >>> 1) % (max - min + 1) + min;
    }

    private boolean clickedSelectedMenu(InputSnapshot input) {
        return input.pointerPressed()
                && input.pointerX() > 80
                && input.pointerX() < 160
                && input.pointerY() >= GameConfig.LOGICAL_HEIGHT - 42
                && input.pointerY() <= GameConfig.LOGICAL_HEIGHT;
    }

    private boolean clickedPreviousMenu(InputSnapshot input) {
        return input.pointerPressed()
                && input.pointerX() >= 0
                && input.pointerX() <= 80
                && input.pointerY() >= GameConfig.LOGICAL_HEIGHT - 42
                && input.pointerY() <= GameConfig.LOGICAL_HEIGHT;
    }

    private boolean clickedNextMenu(InputSnapshot input) {
        return input.pointerPressed()
                && input.pointerX() >= 160
                && input.pointerX() < GameConfig.LOGICAL_WIDTH
                && input.pointerY() >= GameConfig.LOGICAL_HEIGHT - 42
                && input.pointerY() <= GameConfig.LOGICAL_HEIGHT;
    }

    private void drawImageCentered(Graphics2D graphics, BufferedImage image) {
        graphics.drawImage(image,
                (GameConfig.LOGICAL_WIDTH - image.getWidth()) / 2,
                (GameConfig.LOGICAL_HEIGHT - image.getHeight()) / 2,
                null);
    }

    private void drawCenteredString(Graphics2D graphics, String text, int y, Color color) {
        graphics.setColor(color);
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, (GameConfig.LOGICAL_WIDTH - metrics.stringWidth(text)) / 2, y);
    }

    private void drawRightString(Graphics2D graphics, String text, int x, int y) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, x - metrics.stringWidth(text), y);
    }

    private void switchPhase(Phase next) {
        this.phase = next;
        this.phaseTicks = 0;
    }

    private enum Phase {
        LOGO_0,
        CWA_LOGO,
        MUSIC_PROMPT,
        TITLE_MENU
    }
}
