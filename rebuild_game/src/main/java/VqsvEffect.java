import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

final class Effect {
    private static final int W = 240;
    private static final int H = 320;
    private static final int[][] EFFECT_SPEED = {{1, 3}, {1, 4}, {2, 5}, {2, 6}};

    private int overlayType = -1;
    private int solidColor;
    private int tick;
    private int flashLimit;
    private int flashMode;
    private int battleTransitionMode;
    private int battleTransitionProgress;
    private int barsMode;
    private int barsProgress;
    private int barsTotal;
    private int barsStep;
    private int barsWidth;
    private int barsTop;
    private int barsBottom;
    private boolean barsDone = true;
    private boolean overlayDone = true;
    private int circleMode;
    private int circleState;
    private int circleX, circleY, circleR;
    private int fadeColor;
    private int fadeAlpha;
    private int fadeType;
    private BufferedImage iconImage;
    private int iconX, iconY, iconAlpha, iconStep;
    private final int[] circleColors = {0xFFFFFF, 9115396};
    private BufferedImage[] particleImages;
    private Particle[] particles = new Particle[0];
    private final Random random = new Random(7);

    void clearOverlay() {
        overlayType = -1;
        overlayDone = true;
    }

    void startSolid(int color) {
        overlayType = 9;
        solidColor = color;
        overlayDone = false;
    }

    void startFlash(int limit, int mode) {
        overlayType = 10;
        tick = 0;
        flashLimit = limit;
        flashMode = mode;
        overlayDone = false;
    }

    void startBattleEntryTransition(int mode) {
        overlayType = 106;
        tick = 0;
        battleTransitionMode = Math.max(0, Math.min(2, mode));
        battleTransitionProgress = 0;
        overlayDone = false;
    }

    void startFade(int type, int color) {
        overlayType = type;
        fadeType = type;
        fadeColor = color & 0xFFFFFF;
        fadeAlpha = type == 1 ? 255 : 0;
        tick = 0;
        overlayDone = false;
    }

    void startBars(int type, int total, int step, int width, int top, int bottom) {
        barsMode = type;
        barsTotal = Math.max(1, total);
        barsStep = step;
        barsWidth = width;
        barsTop = top;
        barsBottom = bottom;
        barsProgress = 0;
        barsDone = false;
    }

    void startCircle(int colorIndex, int state, int x, int y, int radius) {
        overlayType = 17;
        circleMode = colorIndex;
        circleState = state;
        circleX = x;
        circleY = y;
        circleR = radius;
        tick = 0;
        overlayDone = false;
    }

    void startIcon(String name, int x, int y, int step) {
        overlayType = 15;
        iconX = x;
        iconY = y;
        iconAlpha = 0;
        iconStep = Math.max(1, step);
        try {
            java.nio.file.Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig()).texDecodedPng(name);
            iconImage = Files.isRegularFile(path)
                    ? ImageIO.read(path.toFile())
                    : ImageIO.read(Effect.class.getResource("/tex/" + name + ".png"));
        } catch (IOException ex) {
            iconImage = null;
        }
        overlayDone = false;
    }

    void startParticles(int count) {
        clearOverlay();
        try {
            particleImages = new BufferedImage[]{
                    ImageIO.read(Effect.class.getResource("/tex/star0.png")),
                    ImageIO.read(Effect.class.getResource("/tex/star1.png")),
                    ImageIO.read(Effect.class.getResource("/tex/star2.png")),
                    ImageIO.read(Effect.class.getResource("/tex/star3.png"))
            };
            particles = new Particle[count];
            for (int i = 0; i < count; i++) {
                particles[i] = new Particle();
                resetParticle(particles[i]);
            }
        } catch (IOException ex) {
            particles = new Particle[0];
        }
    }

    void startFireParticles(int count) {
        clearOverlay();
        try {
            particleImages = new BufferedImage[]{
                    ImageIO.read(Effect.class.getResource("/tex/fire0.png")),
                    ImageIO.read(Effect.class.getResource("/tex/fire1.png")),
                    ImageIO.read(Effect.class.getResource("/tex/fire2.png"))
            };
            particles = new Particle[count];
            for (int i = 0; i < count; i++) {
                particles[i] = new Particle();
                resetParticle(particles[i]);
            }
        } catch (IOException ex) {
            particles = new Particle[0];
        }
    }

    void stopParticles() {
        particleImages = null;
        particles = new Particle[0];
    }

    boolean doneBars(Object ignored) {
        return barsDone;
    }

    boolean doneOverlay(Object ignored) {
        return overlayDone;
    }

    void tick() {
        if (!barsDone) {
            barsProgress += barsStep;
            if (barsProgress > barsTotal) {
                barsProgress = barsTotal;
                barsDone = true;
                if (barsMode == 12) {
                    barsMode = -1;
                }
            }
        }
        if (overlayType == 10) {
            tick++;
            if (tick > flashLimit) {
                clearOverlay();
            }
        } else if (overlayType == 106) {
            tick++;
            if (tick >= 10) {
                battleTransitionProgress += 15;
                if (battleTransitionProgress >= W) {
                    overlayDone = true;
                }
            }
        } else if (overlayType == 1 || overlayType == 2) {
            tick++;
            if (fadeType == 1) {
                fadeAlpha -= 17;
                if (fadeAlpha <= 0) {
                    clearOverlay();
                }
            } else {
                fadeAlpha += 17;
                if (fadeAlpha >= 255) {
                    fadeAlpha = 255;
                    overlayDone = true;
                }
            }
        } else if (overlayType == 17) {
            tick++;
            if (circleState == 0) {
                int dx = W - circleX;
                int dy = H - circleY;
                if (dx * dx + dy * dy < circleR * circleR) {
                    overlayDone = true;
                }
                circleR += 10;
            } else if (circleState == 1) {
                circleR -= 10;
                if (circleR <= 0) {
                    clearOverlay();
                }
            } else {
                if (tick <= 10) {
                    circleR += 10;
                } else if (tick <= 20) {
                    circleR -= 10;
                } else {
                    clearOverlay();
                }
            }
        } else if (overlayType == 15) {
            iconAlpha += iconStep;
            if (iconAlpha >= 255) {
                iconAlpha = 255;
                overlayDone = true;
            }
        }
    }

    void renderParticles(Graphics2D g) {
        if (particleImages == null) {
            return;
        }
        for (Particle p : particles) {
            BufferedImage img = particleImages[p.img];
            if (img == null) {
                continue;
            }
            g.drawImage(img, p.x, p.y, null);
            p.x -= p.speed;
            p.y -= p.speed;
            if (p.x < -img.getWidth() || p.y < -img.getHeight()) {
                resetParticle(p);
                p.x = W + random.nextInt(80);
                p.y = random.nextInt(H);
            }
        }
    }

    void renderOverlay(Graphics2D g) {
        if (overlayType == 9) {
            g.setColor(new Color(solidColor));
            g.fillRect(0, 0, W, H);
        } else if (overlayType == 1 || overlayType == 2) {
            int a = Math.max(0, Math.min(255, fadeAlpha));
            g.setColor(new Color((a << 24) | fadeColor, true));
            g.fillRect(0, 0, W, H);
        } else if (overlayType == 10) {
            if (tick <= flashLimit) {
                if (tick % 3 / (flashMode + 1) == 0) {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, W, H);
                } else if (tick % 3 / (flashMode + 1) == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, W, H);
                }
            }
        } else if (overlayType == 106) {
            renderBattleEntryTransition(g);
        } else if (overlayType == 17) {
            g.setColor(new Color(circleColors[Math.max(0, Math.min(circleMode, 1))]));
            g.fillOval(circleX - circleR, circleY - circleR, circleR * 2, circleR * 2);
        } else if (overlayType == 15 && iconImage != null) {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconAlpha / 255.0f));
            g.drawImage(iconImage, iconX - iconImage.getWidth() / 2, iconY - iconImage.getHeight() / 2, null);
            g.setComposite(old);
        }
        if (barsMode == 12 || barsMode == 13) {
            g.setColor(Color.BLACK);
            if (barsMode == 13) {
                int top = barsProgress * barsTop / barsTotal;
                int bottom = barsProgress * barsBottom / barsTotal;
                g.fillRect(0, 0, barsWidth, top);
                g.fillRect(0, H - bottom, barsWidth, bottom);
            } else {
                int top = barsTop - barsProgress * barsTop / barsTotal;
                int bottom = barsBottom - barsProgress * barsBottom / barsTotal;
                g.fillRect(0, 0, barsWidth, top);
                g.fillRect(0, H - bottom, barsWidth, bottom);
            }
        }
    }

    private void renderBattleEntryTransition(Graphics2D g) {
        if (tick < 10) {
            if (tick % 3 == 1) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, W, H);
            }
            return;
        }
        int progress = Math.max(0, Math.min(W, battleTransitionProgress));
        g.setColor(Color.BLACK);
        if (battleTransitionMode == 0) {
            int halfH = H / 2;
            g.fillRect(0, 0, progress, halfH);
            for (int i = 1; i < 6; i++) {
                int w = Math.max(0, 15 - i * 3);
                g.fillRect(progress + i * 15, 0, w, halfH);
            }
            g.fillRect(W - progress, halfH, progress, H - halfH);
            for (int i = 1; i < 6; i++) {
                int w = Math.max(0, 15 - i * 3);
                g.fillRect(W - progress - i * 15, halfH, w, H - halfH);
            }
        } else if (battleTransitionMode == 1) {
            boolean left = false;
            for (int y = 0; y < H; y += 10) {
                if (left) {
                    g.fillRect(0, y, progress, 10);
                } else {
                    g.fillRect(W - progress, y, progress, 10);
                }
                left = !left;
            }
        } else {
            boolean top = false;
            for (int x = 0; x < W; x += 10) {
                if (top) {
                    g.fillRect(x, 0, 10, progress);
                } else {
                    g.fillRect(x, H - progress, 10, progress);
                }
                top = !top;
            }
        }
        if (overlayDone) {
            g.fillRect(0, 0, W, H);
        }
    }

    private void resetParticle(Particle p) {
        int roll = random.nextInt(100);
        if (particleImages != null && particleImages.length == 3) {
            p.img = roll < 20 ? 2 : roll < 55 ? 1 : 0;
        } else {
            p.img = roll < 3 ? 3 : roll < 15 ? 2 : roll < 50 ? 1 : 0;
        }
        p.x = random.nextInt(W);
        p.y = random.nextInt(H);
        p.speed = random.nextInt(EFFECT_SPEED[p.img][1] - EFFECT_SPEED[p.img][0]) + EFFECT_SPEED[p.img][0];
    }
}

final class Particle {
    int img, x, y, speed;
}
