import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.render.GameMap;
import com.vqsv.rebuild.render.MapModInfo;
import com.vqsv.rebuild.render.MapRenderer;
import com.vqsv.rebuild.render.TileSet;
import com.vqsv.rebuild.resource.AssetPaths;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class VqsvIntroDemo extends JPanel {
    private static final int W = 240;
    private static final int H = 320;
    private static final int SCALE = 2;
    private final BufferedImage frame = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
    private final Scene scene;

    public static void main(String[] args) {
        if (args.length > 0 && "--smoke".equals(args[0])) {
            int ticks = args.length > 2 ? Integer.parseInt(args[2]) : 360;
            runSmoke(args.length > 1 ? args[1] : "build_intro_demo/smoke.png", ticks);
            return;
        }
        JFrame f = new JFrame("VQSV Liet Hoa - Intro Scene Rebuild");
        VqsvIntroDemo panel = new VqsvIntroDemo();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setContentPane(panel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        panel.start();
    }

    private static void runSmoke(String outPath, int ticks) {
        try {
            Scene s = new Scene();
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < ticks; i++) {
                if (s.text != null && s.text.readyForKey) {
                    s.press0();
                }
                s.tick();
            }
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-ok " + outPath + " ticks=" + ticks);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private VqsvIntroDemo() {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setFocusable(true);
        scene = new Scene();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '0' || e.getKeyCode() == KeyEvent.VK_NUMPAD0) {
                    scene.press0();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                scene.press0();
            }
        });
    }

    private void start() {
        requestFocusInWindow();
        new Timer(66, e -> {
            scene.tick();
            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D fg = frame.createGraphics();
        fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        scene.render(fg);
        fg.dispose();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(frame, 0, 0, W * SCALE, H * SCALE, null);
        g2.dispose();
    }

    private static final class Scene {
        private final FontBitmap font = new FontBitmap();
        private final Effect effect = new Effect();
        private final Actor[] actors = makeActors();
        private final List<Event> events = makeEvents();
        private final List<TempSprite> tempSprites = new ArrayList<>();
        private MapRenderer mapRenderer;
        private TextBox text;
        private int eventIndex = 0;
        private int cameraX = 0;
        private int cameraY = 0;
        private boolean useMap;
        private boolean key0;
        private Blocking current;
        private int followActorId = -1;
        private int worldEventActor = -1;
        private int battleEventActor = -1;
        private int[] battleEncounter = new int[0];
        private boolean battleCanLose = false;
        private boolean battleScriptLocksInput = false;
        private int battleMode = -1;
        private int battleBackgroundMode = -1;
        private int battleResultIndex = -1;
        private int battleBranchTarget = -1;
        private int battleOverlayTicks = 0;

        private void press0() {
            key0 = true;
        }

        private void tick() {
            effect.tick();
            if (text != null) {
                text.tick();
                if (text.disposed) {
                    text = null;
                }
            }
            for (int i = tempSprites.size() - 1; i >= 0; i--) {
                if (tempSprites.get(i).tick(this)) {
                    tempSprites.remove(i);
                }
            }
            if (current != null) {
                if (!current.tick(this)) {
                    key0 = false;
                    for (Actor a : actors) {
                        if (a != null) {
                            a.tick();
                        }
                    }
                    updateCameraFollow();
                    return;
                }
                current = null;
            }
            int guard = 0;
            while (current == null && eventIndex < events.size() && guard++ < 8) {
                current = events.get(eventIndex++).start(this);
                if (current != null && !current.tick(this)) {
                    break;
                }
                current = null;
            }
            key0 = false;
            for (Actor a : actors) {
                if (a != null) {
                    a.tick();
                }
            }
            updateCameraFollow();
        }

        private void render(Graphics2D g) {
            g.setColor(useMap ? Color.BLACK : new Color(8, 16, 80));
            g.fillRect(0, 0, W, H);
            if (useMap) {
                renderMapLayer(g, 1);
                renderMapLayer(g, 2);
            }

            renderActorLayer(g, 2, false);
            renderActorLayer(g, 1, true);
            for (TempSprite sprite : tempSprites) {
                sprite.render(g, this);
            }
            if (useMap) {
                renderMapLayer(g, 3);
            }
            renderActorLayer(g, 0, false);

            effect.renderParticles(g);
            effect.renderOverlay(g);
            renderBattleOverlay(g);
            if (text != null) {
                text.render(g, font);
            }
        }

        private void renderBattleOverlay(Graphics2D g) {
            if (battleOverlayTicks <= 0) {
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
            String encounterText = battleEncounter.length >= 3
                    ? "Encounter " + battleEncounter[0] + "  Lv " + battleEncounter[1] + "  x" + battleEncounter[2]
                    : "Encounter";
            font.drawTagged(g, "#FFFFFFBattle trigger", 16, 58, 208, 14);
            font.drawTagged(g, "#FFFFFF" + encounterText, 16, 78, 208, encounterText.length());
            String meta = "actor " + battleEventActor + "  mode " + battleMode + "/" + battleBackgroundMode;
            font.drawTagged(g, "#FFFFFF" + meta, 16, 98, 208, meta.length());
            String branch = "auto result " + battleResultIndex + " -> branch " + battleBranchTarget;
            font.drawTagged(g, "#FFFFFF" + branch, 16, 206, 208, branch.length());
            font.drawTagged(g, "#FFFFFFScripted stub", 16, 228, 208, 13);
        }

        private void setCameraCenter(int cx, int cy) {
            if (useMap && mapRenderer != null) {
                mapRenderer.centerCameraOn(cx, cy);
                cameraX = mapRenderer.cameraX();
                cameraY = mapRenderer.cameraY();
            } else {
                cameraX = clamp(cx - W / 2, 0, 640 - W);
                cameraY = clamp(cy - H / 2, 0, 480 - H);
            }
        }

        private void moveCameraToward(int cx, int cy, int speed) {
            int targetX;
            int targetY;
            if (useMap && mapRenderer != null) {
                mapRenderer.centerCameraOn(cx, cy);
                targetX = mapRenderer.cameraX();
                targetY = mapRenderer.cameraY();
            } else {
                targetX = clamp(cx - W / 2, 0, 640 - W);
                targetY = clamp(cy - H / 2, 0, 480 - H);
            }
            if (speed <= 0) {
                cameraX = targetX;
                cameraY = targetY;
            } else {
                int dx = targetX - cameraX;
                int dy = targetY - cameraY;
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                if (distance <= speed) {
                    cameraX = targetX;
                    cameraY = targetY;
                } else {
                    cameraX += dx * speed / distance;
                    cameraY += dy * speed / distance;
                }
            }
            if (useMap && mapRenderer != null) {
                mapRenderer.setCamera(cameraX, cameraY);
                cameraX = mapRenderer.cameraX();
                cameraY = mapRenderer.cameraY();
            }
        }

        private boolean cameraCenteredOn(int cx, int cy) {
            int oldX = cameraX;
            int oldY = cameraY;
            if (useMap && mapRenderer != null) {
                mapRenderer.centerCameraOn(cx, cy);
                boolean same = oldX == mapRenderer.cameraX() && oldY == mapRenderer.cameraY();
                mapRenderer.setCamera(oldX, oldY);
                return same;
            }
            return oldX == clamp(cx - W / 2, 0, 640 - W)
                    && oldY == clamp(cy - H / 2, 0, 480 - H);
        }

        private void followActor(int actorId) {
            followActorId = actorId;
            updateCameraFollow();
        }

        private void stopCameraFollow() {
            followActorId = -1;
        }

        private void updateCameraFollow() {
            if (followActorId < 0 || followActorId >= actors.length || actors[followActorId] == null) {
                return;
            }
            Actor actor = actors[followActorId];
            setCameraCenter(actor.x, actor.y);
        }

        private void renderMapLayer(Graphics2D g, int layerIndex) {
            if (useMap && mapRenderer != null && mapRenderer.hasLayer(layerIndex)) {
                mapRenderer.renderLayer(g, layerIndex);
            }
        }

        private void renderActorLayer(Graphics2D g, int layer, boolean sortByY) {
            ArrayList<Actor> draw = new ArrayList<>();
            for (Actor a : actors) {
                if (a != null && a.visible && a.layer == layer) {
                    draw.add(a);
                }
            }
            if (sortByY) {
                draw.sort(Comparator.comparingInt(a -> a.y));
            }
            for (Actor a : draw) {
                a.render(g, cameraX, cameraY);
            }
        }

        private static MapRenderer loadMapRenderer(int mapId) {
            try {
                AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
                GameMap map = GameMap.load(paths, mapId);
                MapModInfo modInfo = MapModInfo.load(paths);
                return new MapRenderer(map, TileSet.load(paths, modInfo, map.modId()));
            } catch (RuntimeException exception) {
                return null;
            }
        }

        private static int clamp(int v, int lo, int hi) {
            return Math.max(lo, Math.min(hi, v));
        }

        private static Actor[] makeActors() {
            int[][] rows = {
                    {0, 84, 0, 384, 168, 0},
                    {1, 85, 0, 9, 274, 0},
                    {2, 161, 0, 53, 192, 0},
                    {3, 173, 0, 187, 419, 0},
                    {4, 185, 0, 383, 191, 0},
                    {5, 101, 0, 15, 246, 0},
                    {6, 117, 0, 354, 110, 0},
                    {7, 133, 0, 26, 91, 0},
                    {8, 149, 0, 378, 238, 0},
                    {9, 327, 2, 124, 357, 0},
                    {10, 266, 0, 618, 130, 0},
                    {11, 266, 0, 617, 144, 0},
                    {12, 266, 0, 617, 145, 0},
                    {13, 266, 0, 617, 145, 0},
                    {14, 267, 2, 255, 223, 0},
                    {15, 267, 2, 196, 239, 0},
                    {16, 267, 2, 156, 211, 0},
                    {17, 262, 0, 602, 199, 0},
                    {18, 262, 0, 132, 227, 0},
                    {19, 262, 0, 201, 222, 0},
                    {20, 262, 0, 264, 219, 0},
                    {21, 262, 0, 601, 199, 0},
                    {22, 264, 0, 148, 201, 0},
                    {23, 264, 0, 204, 221, 0},
                    {24, 264, 0, 262, 205, 0},
                    {25, 266, 0, 615, 149, 0},
                    {26, 267, 0, 153, 199, 0},
                    {27, 267, 0, 188, 249, 0},
                    {28, 267, 0, 260, 233, 0},
                    {29, 266, 0, 615, 149, 0},
                    {30, 266, 0, 616, 149, 0},
                    {31, 266, 0, 617, 149, 0},
                    {32, 266, 0, 158, 210, 0},
                    {33, 266, 0, 211, 257, 0},
                    {34, 266, 0, 617, 148, 0},
                    {35, 266, 0, 251, 203, 0},
                    {36, 262, 0, 603, 196, 0},
                    {37, 262, 0, 601, 197, 0},
                    {38, 262, 0, 602, 197, 0},
                    {39, 85, 0, 46, 166, 0}
            };
            Actor[] out = new Actor[80];
            for (int[] r : rows) {
                Actor a = new Actor(r[0], r[1], r[2], r[3], r[4]);
                a.visible = r[5] == 1;
                out[r[0]] = a;
            }
            return out;
        }

        private static List<Event> makeEvents() {
            ArrayList<Event> e = new ArrayList<>();
            e.add(s -> {
                s.effect.startSolid(0);
                s.text = TextBox.full(30, 90, "#FFFFFF Nghe đồn Thiên Địa chi sơ, vạn năm về trước có hai vị thần, một người duy trì trật tự, một người cai quản thế giới hỗn loạn, kiềm chế lẫn nhau, duy trì cân bằng của thế giới.", true);
                return sc -> {
                    if (sc.text != null && sc.text.readyForKey && sc.key0) {
                        sc.text.disposed = true;
                        sc.effect.clearOverlay();
                        return true;
                    }
                    return false;
                };
            });
            e.add(s -> { s.effect.startBars(13, 1, 1, 240, 10, 10); return s.effect::doneBars; });
            e.add(s -> { s.effect.startParticles(80); return null; });
            e.add(s -> { s.setCameraCenter(190, 0); return null; });
            e.add(s -> { setActive(s, new int[]{1, 0}, new int[]{1, 3}); return null; });
            e.add(s -> new Move(new int[]{0, 1}, new int[]{-26, 23}, new int[]{0, 0}, new int[]{5, 5}, new int[]{0, 0}));
            e.add(s -> new Move(new int[]{0, 1}, new int[]{0, 0}, new int[]{3, -3}, new int[]{0, 0}, new int[]{20, 20}));
            e.add(s -> new Move(new int[]{0, 1}, new int[]{0, 0}, new int[]{-3, 3}, new int[]{0, 0}, new int[]{10, 10}));
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{18, 19, 20, 22, 23, 24}, new int[]{1, 3, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> { hide(s, new int[]{18, 19, 20, 22, 23, 24}); return null; });
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{26, 27, 28, 14, 15, 16}, new int[]{1, 1, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { hide(s, new int[]{26, 27, 28, 14, 15, 16}); return null; });
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{32, 33, 35}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { s.effect.startCircle(0, 0, 120, 160, 25); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{32, 33, 35}); return null; });
            e.add(s -> new Move(new int[]{0}, new int[]{75}, new int[]{0}, new int[]{2}, new int[]{0}));
            e.add(s -> new Path(new int[]{1}, new int[][]{{185}}, new int[][]{{200}}));
            e.add(s -> new Move(new int[]{1}, new int[]{0}, new int[]{4}, new int[]{0}, new int[]{6}));
            e.add(s -> { s.effect.startCircle(0, 1, 120, 160, 160); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startBars(13, 1, 1, 240, 10, 50); return s.effect::doneBars; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vi Bạch Long, vị thần đứng đầu Thiên Giới phụ trách cai quản trật tự. Ba vị thủ hộ thánh thú lần lượt là Lôi Kỳ Lân, Tinh Vân Hạc cùng Minh Vương Long.", false);
                return null;
            });
            e.add(s -> new Move(new int[]{1}, new int[]{0}, new int[]{-4}, new int[]{0}, new int[]{18}));
            e.add(s -> new Delay(50));
            e.add(s -> { setActive(s, new int[]{2, 3, 4}, new int[]{1, 1, 3}); return null; });
            e.add(s -> new Move(new int[]{2}, new int[]{35}, new int[]{0}, new int[]{2}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{4}, new int[]{-30}, new int[]{0}, new int[]{4}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{3}, new int[]{0}, new int[]{-32}, new int[]{0}, new int[]{5}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{1, 2, 3, 4}); return null; });
            e.add(s -> new Move(new int[]{0}, new int[]{-100}, new int[]{-14}, new int[]{2}, new int[]{2}));
            e.add(s -> { s.effect.startParticles(90); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vi Hắc Long, vị thần đứng đầu Địa Giới phụ trách cai quản thế giới hỗn loạn. Bốn vị chiến thần thú lần lượt là Chiến Thần Đà, Tương Quân Giải, Linh Quang Lộc và Hỏa Phượng Hoàng.", false);
                return null;
            });
            e.add(s -> new Delay(60));
            e.add(s -> { setActive(s, new int[]{5, 6, 7, 8}, new int[]{1, 1, 1, 1}); return null; });
            e.add(s -> new Move(new int[]{7}, new int[]{30}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{8}, new int[]{-38}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{6}, new int[]{-33}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{5}, new int[]{38}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Delay(5));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{5, 6, 7, 8}); return null; });
            e.add(s -> { s.loadScene7Room2(296, 140); return null; });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Mấy ngàn năm trước, lực lượng hỗn độn thế lực không ngừng lớn mạnh, dần hình thành xu thế đàn áp Thiên Giới. Để cân bằng giữa Thiên Địa, Bạch Long cùng Hắc Long đã tiến hành một cuộc Thiên Địa thánh chiến.", false);
                return null;
            });
            e.add(s -> new Delay(15));
            e.add(s -> { setActive(s, new int[]{32, 33}, new int[]{1, 3}); return null; });
            e.add(s -> new Move(new int[]{32, 33}, new int[]{10, -10}, new int[]{0, 0}, new int[]{13, 12}, new int[]{0, 0}));
            e.add(s -> new Move(new int[]{32, 33}, new int[]{0, 0}, new int[]{5, -5}, new int[]{0, 0}, new int[]{18, 30}));
            e.add(s -> new Delay(15));
            e.add(s -> { setActive(s, new int[]{36, 37, 38}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { hide(s, new int[]{36, 37, 38}); return null; });
            e.add(s -> { setActive(s, new int[]{39, 40}, new int[]{1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { hide(s, new int[]{39, 40}); return null; });
            e.add(s -> { setActive(s, new int[]{41, 42, 43, 44, 45}, new int[]{1, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { hide(s, new int[]{41, 42, 43, 44, 45}); return null; });
            e.add(s -> { setActive(s, new int[]{57, 58, 59}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> { s.effect.startCircle(0, 0, 120, 160, 25); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{57, 58, 59}); return null; });
            e.add(s -> { hide(s, new int[]{32, 33}); return null; });
            e.add(s -> { s.effect.stopParticles(); return null; });
            e.add(s -> { s.effect.startCircle(0, 1, 120, 160, 160); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Mấy trăm năm sau, cuộc chiến kết thúc, Bạch Long cùng Hắc Long đều tan biến.", false);
                return null;
            });
            e.add(s -> new Delay(50));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(95, 280, 240, 320);
                s.markWorldTransition(0, 0, 0);
                s.reloadBlankRoomCenteredOnActor(9);
                return null;
            });
            e.add(s -> { setActive(s, new int[]{9}, new int[]{1}); return null; });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Không lâu sau đó, thế gian xuất hiện hai Bảo Châu, một trắng, một đen. Người ta tin rằng đây chính là linh hồn của các vị thần cổ đại, có năng lượng vô tận.", false);
                return null;
            });
            e.add(s -> new Delay(140));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Về sau, hai Bảo Châu này, một bay lên Thiên Giới, một rơi xuống nhân gian, tiếp tục sứ mệnh bảo vệ thế giới.", false);
                return null;
            });
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startParticles(80); return null; });
            e.add(s -> new Move(new int[]{9}, new int[]{0}, new int[]{-2}, new int[]{0}, new int[]{52}));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(340, 412, 240, 320);
                s.markWorldTransition(5, 3, 36);
                s.loadScene5Room3(340, 412);
                return null;
            });
            e.add(s -> { setActive(s, new int[]{41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}); return null; });
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Thiên Giới và Địa Giới có mối liên hệ duy nhất thông đạo Thiên Giới Bạch Long Thần Điện cùng Địa Giới Hắc Long Thần Điện. Cứ sau một trăm năm, hai tòa thần điện mở lối đi thông nhau vào một ngày để người hai giới có thể gặp gỡ. Nhưng một trăm năm mới có một cơ hội nên có thể nói đây cũng không hẳn đã là niềm vui cho nhân loại.", false);
                return null;
            });
            e.add(s -> new CameraPanPoint(340, 412, 2));
            e.add(s -> new Delay(200));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFFHắc Thạch Thành Mã Đầu: Ha ha! Tuy là trăm năm mới có một dịp nhưng đây cũng là cơ hội tốt. Ý trời đã định! Chúng quân nghe lệnh!", false);
                return null;
            });
            e.add(s -> new Delay(110));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Hắc Long Quân:!", false);
                return null;
            });
            e.add(s -> new Delay(35));
            e.add(s -> {
                s.text = TextBox.full(30, 90, "#FFFFFF Ngày nào đó, Tất cả thiên không thần điện cũng không thể thoát khỏi kiếp định này. Đây không phải chiến tranh, cuộc chiến của một phe, căn bản chính là... Chết chóc.", true);
                return waitForText();
            });
            e.add(s -> { s.effect.startBars(12, 1, 1, 240, 10, 50); return s.effect::doneBars; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.loadRoom1(340, 412);
                s.text = TextBox.full(30, 90, "#FFFFFF Một ngày sau đó, trước một ngôi đền hoang ...", true);
                return sc -> {
                    if (sc.text != null && sc.text.readyForKey && sc.key0) {
                        sc.text.disposed = true;
                        sc.effect.clearOverlay();
                        return true;
                    }
                    return false;
                };
            });
            e.add(s -> { s.effect.startFireParticles(100); return null; });
            e.add(s -> new Delay(8));
            e.add(s -> { setActive(s, new int[]{29, 28, 30}, new int[]{1, 1, 3}); return null; });
            e.add(s -> { s.followActor(30); return null; });
            e.add(s -> new Move(new int[]{30}, new int[]{-5}, new int[]{1}, new int[]{84}, new int[]{84}));
            e.add(s -> { hide(s, new int[]{28}); return null; });
            e.add(s -> new Move(new int[]{30, 29}, new int[]{-5, -5}, new int[]{-2, -2}, new int[]{50, 50}, new int[]{50, 50}));
            e.add(s -> { s.stopCameraFollow(); return null; });
            e.add(s -> { s.effect.stopParticles(); return null; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(199, 79, 240, 320);
                s.markWorldTransition(1, 3, 0);
                s.loadScene1Room3Entry(199, 79);
                return null;
            });
            e.add(s -> {
                s.text = TextBox.full(60, 90, "#FFFFFF Sáu năm sau ...", true);
                return waitForText();
            });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{48, 49, 50}, new int[]{1, 2, 2}); return null; });
            e.add(s -> new CameraPan(49, 0));
            e.add(s -> new Delay(15));
            e.add(s -> new CameraPan(48, 10));
            e.add(dialog("Neil", "Đến đây đi! Sophie ~ Tìm không thấy ta đâu~~~ Ha ha"));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{0, 0}, new int[]{4, 4}, new int[]{13, 13}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{3, 3}, new int[]{4, 4}, new int[]{13, 13}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{0, 0}, new int[]{4, 4}, new int[]{23, 23}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{3, 3}, new int[]{4, 4}, new int[]{20, 20}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{2, 2}, new int[]{4, 4}, new int[]{18, 18}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{1, 1}, new int[]{3, 3}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{1, 1}, new int[]{1, 1}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{2, 2}, new int[]{2, 2}));
            e.add(s -> { s.spawnActorEffect(49, 14); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Sophie", "... Hê hê ... ông trốn sau đá Peepna của tôi nhìn lén chứ gì? ~ Mau ra đây ~"));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{0}, new int[]{4}, new int[]{6}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{1}, new int[]{4}, new int[]{13}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{8}));
            e.add(dialog("Neil", "Ặc, sao phát hiện giỏi vậy ta?..."));
            e.add(s -> { s.spawnActorEffect(49, 7); return null; });
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(dialog("Sophie", "Hun? Thật đấy ~"));
            e.add(dialog("Neil", "Ách ... sớm biết không phải."));
            e.add(dialog("Sophie", "Hì hì ~ thời gian không còn sớm, chúng ta mau trở về ~"));
            e.add(s -> new TimedAction(new int[]{48, 49, 50}, new int[]{0, 0, 0}, new int[]{4, 4, 4}, new int[]{13, 13, 13}));
            e.add(s -> new TimedAction(new int[]{48, 49, 50}, new int[]{1, 1, 1}, new int[]{4, 4, 4}, new int[]{20, 20, 20}));
            e.add(s -> new ActionSet(new int[]{48}, new int[]{2}, new int[]{2}));
            e.add(dialog("Neil", "Sophie, đã qua vài năm tôi muốn gặp cha mẹ cậu."));
            e.add(s -> new ActionSet(new int[]{49}, new int[]{0}, new int[]{0}));
            e.add(dialog("Sophie", "Ai? Vì sao chứ?"));
            e.add(s -> { s.spawnActorEffect(48, 8); return null; });
            e.add(dialog("Neil", "Đương nhiên là bởi vì chưa từng gặp họ!"));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{2, 2}, new int[]{4, 4}, new int[]{16, 16}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(dialog("Sophie", "Thực ra..., chính ta cũng chưa từng được gặp họ. Mọi người đều nói cha mẹ ta đã mất trong chiến tranh. Tất cả những gì còn lại của họ chỉ có chiếc vòng cổ này."));
            e.add(dialog("Neil", "..."));
            e.add(dialog("Sophie", "Neil, trông bộ dạng có vẻ tâm trạng thế hả?"));
            e.add(dialog("Neil", "Ờ thì người ta đồng cảm với cảnh ngộ của cậu! Đáng thương quá. Hix"));
            e.add(dialog("Sophie", "Ta không cảm thấy vậy. Mặc dù ta cũng muốn có cha mẹ, nhưng ta có gia gia, có Neil làm bạn thế là đã quá đủ rồi! Nhất là được sống một nơi với Neil là niềm vui lớn nhất của ta!"));
            e.add(s -> new Delay(15));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{10}));
            e.add(dialog("Neil", "Vậy chúng ta sẽ cùng nhau đi đến bất cứ đâu."));
            e.add(s -> { s.spawnActorEffect(49, 5); return null; });
            e.add(dialog("Sophie", "Thật sự sao? Neil cùng với Sophie sao?"));
            e.add(dialog("Neil", "..."));
            e.add(dialog("Sophie", "Nói lại đi ~ Neil thật sự sẽ cùng với Sophie sao?"));
            e.add(dialog("Neil", "Đương nhiên! Nam nhân đại trượng phu nói một lời không thay đổi! Ta sẽ ở bên, bảo vệ, không cho bất cứ ai làm tổn thương Sophie!"));
            e.add(s -> { s.spawnActorEffect(49, 14); return null; });
            e.add(dialog("Sophie", " Hay quá, ta ước được cùng Neil sống chung một nơi, vĩnh viễn không xa rời nhau."));
            e.add(dialog("Neil", "Ừ, nhất định."));
            e.add(s -> { setActive(s, new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}); return null; });
            e.add(s -> new TimedAction(new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}, new int[]{4, 4, 4, 4}, new int[]{23, 23, 23, 23}));
            e.add(s -> new TimedAction(new int[]{53, 54, 55, 56}, new int[]{3, 3, 3, 3}, new int[]{4, 4, 4, 4}, new int[]{15, 15, 15, 15}));
            e.add(s -> new ActionSet(new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}));
            e.add(dialog("??", "Tìm được rồi! Rốt cuộc đã tìm được! Người mang dấu ấn màu hồng!"));
            e.add(s -> { s.spawnActorEffect(48, 7); return null; });
            e.add(s -> { s.spawnActorEffect(49, 7); return null; });
            e.add(s -> new ActionSet(new int[]{49}, new int[]{2}, new int[]{2}));
            e.add(dialog("Neil", "Các ngươi muốn làm gì!?"));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(s -> new TimedAction(new int[]{53, 49, 56, 50}, new int[]{0, 0, 0, 0}, new int[]{6, 4, 6, 4}, new int[]{4, 4, 4, 4}));
            e.add(s -> new TimedAction(new int[]{53, 49, 56, 50}, new int[]{2, 2, 2, 2}, new int[]{4, 4, 4, 4}, new int[]{6, 6, 6, 6}));
            e.add(dialog("Sophie", "A a a! Thả ta ra! Neil!"));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{6}));
            e.add(dialog("Neil", "Hỗn xược! Buông Sophie ra!"));
            e.add(s -> new ActionSet(new int[]{53, 56, 49}, new int[]{0, 0, 0}, new int[]{0, 0, 0}));
            e.add(dialog("??", "Ái chà, xem ra tiểu tử này muốn làm anh hùng cứu mỹ nhân đây."));
            e.add(dialog("??", "Giải quyết nhanh tên này trở về phục mệnh."));
            e.add(s -> new ScriptedBattleStub(
                    56,
                    new int[]{5, 20, 4},
                    new int[]{1, 1},
                    new int[]{0, 2},
                    new int[]{78, 78, 0}));
            e.add(s -> { hide(s, new int[]{50}); return null; });
            e.add(dialog("??", "Ải ải, không phải ta khi dễ ngươi, là ngươi không biết tự lượng sức mình muốn cùng ta đấu một chuyến."));
            e.add(dialog("Sophie", "Neil! Cậu làm sao...?!"));
            e.add(dialog("Neil", "Yên tâm, Ta còn có thể..."));
            e.add(s -> { s.spawnActorEffect(49, 6); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Sophie", "Neil! Neil! Chạy mau đi!"));
            e.add(dialog("??", "Đi thôi, không có thời gian đùa với tên tiểu tử đó."));
            e.add(s -> new TimedAction(new int[]{49, 53, 54, 55, 56}, new int[]{1, 1, 1, 1, 1}, new int[]{4, 4, 4, 4, 4}, new int[]{15, 15, 15, 15, 15}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{4}));
            e.add(s -> new TimedAction(new int[]{49, 53, 54, 55, 56}, new int[]{2, 2, 2, 2, 2}, new int[]{4, 4, 4, 4, 4}, new int[]{23, 23, 23, 23, 23}));
            e.add(s -> { hide(s, new int[]{49, 53, 54, 55, 56}); return null; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> new Opcode34Counter(70, 0, 0));
            e.add(s -> {
                s.text = TextBox.box(20, 220, 200, 40, "#FFFFFF Tức thật! Sophie! Trả Sophie lại cho ta ...!(vừa mới thề sẽ bảo vệ nàng. Vừa mới hứa hẹn đi đâu cũng có nhau, vĩnh viễn đem lại niềm vui cho Sophie. Thế mà...)", true);
                return waitForText();
            });
            e.add(s -> new Delay(30));
            e.add(s -> {
                s.text = TextBox.full(60, 90, "#FFFFFF Đám xấc xược này! Hãy khoan!", true);
                return waitForText();
            });
            e.add(s -> { s.spawnActorEffect(48, 1); return null; });
            e.add(dialog("Neil", "Đó là ... cái gì ...?"));
            e.add(s -> { s.effect.startCircle(0, 0, 120, 100, 10); return s.effect::doneOverlay; });
            e.add(dialog("Neil", "Sophie, vòng cổ ..."));
            e.add(s -> { s.spawnActorEffect(48, 13); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Neil", "Không, là ta không đủ mạnh... một ngày nào đó ... một ngày nào đó!!!"));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(199, 218, 240, 320);
                s.markWorldTransition(1, 0, -1);
                return null;
            });
            e.add(s -> {
                s.loadScene1Room0(s.transitionCenterX, s.transitionCenterY);
                return null;
            });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            return e;
        }

        private int transitionCenterX;
        private int transitionCenterY;
        private int transitionWidth = W;
        private int transitionHeight = H;
        private int nextWorldF = -1;
        private int nextWorldG = -1;
        private int nextWorldActor = -1;

        private void prepareTransition(int centerX, int centerY, int width, int height) {
            transitionCenterX = centerX;
            transitionCenterY = centerY;
            transitionWidth = width;
            transitionHeight = height;
        }

        private void markWorldTransition(int worldF, int worldG, int actorIndex) {
            nextWorldF = worldF;
            nextWorldG = worldG;
            nextWorldActor = actorIndex;
        }

        private void reloadBlankRoom(int cameraCenterX, int cameraCenterY) {
            useMap = false;
            mapRenderer = null;
            followActorId = -1;
            Actor[] fresh = makeActors();
            for (int i = 0; i < fresh.length; i++) {
                actors[i] = fresh[i];
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        private void reloadBlankRoomCenteredOnActor(int actorId) {
            reloadBlankRoom(0, 0);
            Actor target = actors[actorId];
            setCameraCenter(target.x, target.y);
        }

        private void loadScene7Room2(int cameraCenterX, int cameraCenterY) {
            useMap = true;
            mapRenderer = loadMapRenderer(20);
            followActorId = -1;
            int[][] rows = {
                    {32, 85, 0, 56, 173, 0},
                    {33, 84, 0, 522, 278, 0},
                    {36, 264, 0, 286, 196, 0},
                    {37, 264, 0, 236, 218, 0},
                    {38, 264, 0, 343, 202, 0},
                    {39, 265, 0, 320, 260, 0},
                    {40, 265, 0, 265, 209, 0},
                    {41, 266, 0, 252, 248, 0},
                    {42, 266, 0, 288, 202, 0},
                    {43, 266, 0, 329, 270, 0},
                    {44, 266, 0, 310, 236, 0},
                    {45, 266, 0, 337, 185, 0},
                    {57, 262, 0, 226, 219, 0},
                    {58, 262, 0, 286, 213, 0},
                    {59, 262, 0, 343, 226, 0}
            };
            for (int i = 0; i < actors.length; i++) {
                actors[i] = null;
            }
            for (int[] row : rows) {
                Actor actor = new Actor(row[0], row[1], row[2], row[3], row[4]);
                actor.visible = row[5] == 1;
                actors[row[0]] = actor;
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        private void loadRoom1(int cameraCenterX, int cameraCenterY) {
            useMap = true;
            mapRenderer = loadMapRenderer(100);
            followActorId = -1;
            int[][] rows = {
                    {0, 247, 0, 167, 201, 1},
                    {1, 282, 0, 176, 224, 1},
                    {2, 282, 0, 239, 79, 1},
                    {3, 282, 1, 48, 127, 1},
                    {4, 282, 2, 65, 110, 1},
                    {5, 282, 2, 528, 127, 1},
                    {6, 282, 2, 336, 287, 1},
                    {7, 282, 4, 64, 160, 1},
                    {8, 282, 4, 464, 112, 1},
                    {9, 282, 4, 208, 207, 1},
                    {10, 282, 5, 498, 158, 1},
                    {11, 282, 5, 464, 255, 1},
                    {12, 282, 5, 289, 191, 1},
                    {13, 282, 5, 193, 79, 1},
                    {14, 282, 18, 30, 286, 1},
                    {15, 282, 18, 49, 316, 1},
                    {16, 282, 18, 30, 347, 1},
                    {17, 282, 18, 63, 349, 1},
                    {18, 282, 19, 237, 314, 1},
                    {19, 282, 19, 237, 345, 1},
                    {20, 282, 19, 240, 412, 1},
                    {21, 282, 19, 272, 429, 1},
                    {22, 282, 19, 304, 429, 1},
                    {23, 284, 1, 385, 142, 1},
                    {24, 284, 4, 385, 142, 1},
                    {25, 247, 0, 406, 154, 1},
                    {26, 247, 0, 439, 204, 1},
                    {27, 247, 0, 600, 317, 1},
                    {28, 259, 8, 214, 135, 0},
                    {29, 326, 0, 220, 155, 0},
                    {30, 83, 1, 633, 70, 0}
            };
            for (Actor actor : actors) {
                if (actor != null) {
                    actor.visible = false;
                }
            }
            for (int[] row : rows) {
                Actor actor = new Actor(row[0], row[1], row[2], row[3], row[4]);
                actor.visible = row[5] == 1;
                actors[row[0]] = actor;
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        private void loadScene5Room3(int cameraCenterX, int cameraCenterY) {
            useMap = true;
            mapRenderer = loadMapRenderer(64);
            followActorId = -1;
            tempSprites.clear();
            int[][] rows = {
                    {0, 270, 0, 335, 160, 1, 0, 1},
                    {1, 270, 3, 110, 384, 1, 0, 1},
                    {2, 270, 1, 584, 350, 1, 0, 1},
                    {3, 271, 0, 110, 393, 1, 0, 0},
                    {4, 314, 0, 584, 351, 1, 0, 0},
                    {5, 273, 20, 61, 129, 1, 0, 1},
                    {6, 273, 20, 159, 128, 1, 0, 1},
                    {7, 273, 20, 511, 127, 1, 0, 1},
                    {8, 273, 20, 608, 129, 1, 0, 1},
                    {9, 243, 0, 104, 81, 1, 0, 1},
                    {10, 243, 0, 552, 80, 1, 0, 1},
                    {11, 273, 0, 24, 110, 1, 0, 1},
                    {12, 273, 1, 41, 192, 1, 0, 1},
                    {13, 273, 1, 23, 223, 1, 0, 1},
                    {14, 273, 14, 69, 207, 1, 0, 1},
                    {15, 273, 14, 55, 219, 1, 0, 1},
                    {16, 273, 3, 60, 240, 1, 0, 1},
                    {17, 273, 19, 100, 187, 1, 0, 1},
                    {18, 275, 0, 97, 202, 1, 0, 1},
                    {19, 275, 0, 80, 234, 1, 0, 1},
                    {20, 273, 3, 285, 252, 1, 0, 1},
                    {21, 273, 1, 310, 255, 1, 0, 1},
                    {22, 275, 0, 331, 248, 1, 0, 1},
                    {23, 273, 1, 359, 256, 1, 0, 1},
                    {24, 273, 3, 384, 254, 1, 0, 1},
                    {25, 273, 16, 135, 476, 1, 0, 1},
                    {26, 273, 17, 199, 475, 1, 0, 1},
                    {27, 273, 12, 370, 478, 1, 0, 1},
                    {28, 273, 13, 306, 479, 1, 0, 1},
                    {29, 273, 16, 471, 475, 1, 0, 1},
                    {30, 273, 17, 537, 474, 1, 0, 1},
                    {31, 243, 0, 663, 303, 1, 0, 1},
                    {32, 273, 15, 645, 430, 1, 0, 1},
                    {33, 275, 0, 624, 425, 1, 0, 1},
                    {34, 273, 0, 631, 465, 1, 0, 1},
                    {35, 273, 0, 663, 463, 1, 0, 1},
                    {36, 289, 8, 337, 368, 1, 0, 1},
                    {37, 223, 1, 167, 481, 1, 1, 2},
                    {38, 223, 1, 339, 478, 1, 1, 2},
                    {39, 223, 1, 503, 481, 1, 1, 2},
                    {40, 271, 0, 335, 163, 1, 0, 2},
                    {41, 65, 0, 340, 382, 0, 0, 1},
                    {42, 31, 2, 279, 416, 0, 0, 1},
                    {43, 31, 2, 343, 416, 0, 0, 1},
                    {44, 31, 2, 376, 417, 0, 0, 1},
                    {45, 31, 2, 409, 418, 0, 0, 1},
                    {46, 31, 2, 311, 416, 0, 0, 1},
                    {47, 30, 2, 279, 447, 0, 0, 1},
                    {48, 30, 2, 312, 450, 0, 0, 1},
                    {49, 30, 2, 345, 448, 0, 0, 1},
                    {50, 30, 2, 379, 449, 0, 0, 1},
                    {51, 30, 2, 410, 449, 0, 0, 1},
                    {53, 13, 0, 52, 398, 1, 1, 1},
                    {54, 30, 2, 279, 473, 0, 0, 0},
                    {55, 30, 2, 410, 474, 0, 0, 0},
                    {56, 7, 1, 360, 192, 0, 1, 1},
                    {57, 342, 1, 111, 344, 1, 0, 0},
                    {58, 342, 0, 586, 318, 1, 0, 0},
                    {59, 17, 0, 168, 401, 1, 1, 1},
                    {60, 30, 4, 120, 207, 1, 3, 1},
                    {61, 31, 4, 137, 222, 1, 3, 1},
                    {62, 32, 1, 57, 433, 1, 3, 1},
                    {63, 29, 1, 42, 448, 1, 1, 1},
                    {64, 29, 1, 122, 239, 1, 0, 1},
                    {65, 31, 0, 600, 174, 1, 1, 1},
                    {66, 31, 2, 601, 210, 1, 1, 1}
            };
            for (int i = 0; i < actors.length; i++) {
                actors[i] = null;
            }
            for (int[] row : rows) {
                Actor actor = new Actor(row[0], row[1], row[2], row[3], row[4], row[6], row[7]);
                actor.visible = row[5] == 1;
                actors[row[0]] = actor;
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        private void loadScene1Room3Entry(int cameraCenterX, int cameraCenterY) {
            useMap = true;
            mapRenderer = loadMapRenderer(8);
            followActorId = -1;
            tempSprites.clear();
            int[][] rows = {
                    {0, 244, 0, 329, 32, 1, 1, 2},
                    {1, 205, 0, 135, 26, 1, 0, 1},
                    {2, 328, 0, 25, 10, 1, 0, 1},
                    {3, 328, 0, 89, 41, 1, 0, 1},
                    {4, 328, 0, 40, 265, 1, 0, 1},
                    {5, 204, 1, 44, 45, 1, 0, 1},
                    {6, 205, 1, 8, 60, 1, 0, 1},
                    {7, 205, 0, 391, 183, 1, 0, 1},
                    {8, 204, 0, 393, 43, 1, 0, 1},
                    {9, 204, 1, 26, 250, 1, 0, 1},
                    {10, 328, 0, 120, 328, 1, 0, 1},
                    {11, 205, 1, 216, 281, 1, 0, 1},
                    {12, 200, 1, 247, 51, 1, 0, 1},
                    {13, 225, 1, 121, 32, 1, 0, 2},
                    {14, 225, 1, 56, 46, 1, 0, 2},
                    {15, 225, 1, 133, 62, 1, 0, 2},
                    {16, 225, 1, 10, 161, 1, 0, 2},
                    {17, 225, 1, 7, 303, 1, 0, 2},
                    {18, 225, 1, 80, 284, 1, 0, 2},
                    {19, 225, 1, 164, 316, 1, 0, 2},
                    {20, 225, 1, 358, 53, 1, 0, 2},
                    {21, 225, 1, 388, 121, 1, 0, 2},
                    {22, 225, 1, 205, 246, 1, 0, 2},
                    {23, 225, 1, 250, 208, 1, 0, 2},
                    {24, 223, 0, 201, 29, 1, 1, 0},
                    {25, 223, 3, 397, 273, 1, 1, 3},
                    {26, 200, 1, 247, 82, 1, 0, 1},
                    {27, 200, 0, 67, 339, 1, 0, 1},
                    {28, 200, 0, 147, 290, 1, 0, 1},
                    {29, 200, 0, 196, 355, 1, 0, 1},
                    {30, 200, 1, 183, 178, 1, 0, 1},
                    {31, 200, 1, 231, 177, 1, 0, 1},
                    {32, 200, 1, 279, 177, 1, 0, 1},
                    {33, 203, 0, 65, 98, 1, 1, 0},
                    {34, 200, 1, 183, 226, 1, 0, 1},
                    {35, 200, 1, 278, 228, 1, 0, 1},
                    {36, 200, 1, 279, 273, 1, 0, 1},
                    {37, 204, 1, 284, 297, 1, 0, 1},
                    {38, 200, 1, 294, 324, 1, 0, 1},
                    {39, 200, 1, 343, 323, 1, 0, 1},
                    {40, 200, 1, 390, 323, 1, 0, 1},
                    {41, 200, 0, 418, 69, 1, 0, 1},
                    {42, 202, 1, 357, 223, 1, 0, 1},
                    {43, 289, 5, 26, 143, 1, 0, 1},
                    {44, 289, 4, 107, 141, 1, 0, 1},
                    {45, 289, 5, 25, 191, 1, 0, 1},
                    {46, 289, 4, 107, 193, 1, 0, 1},
                    {47, 243, 0, 233, 191, 1, 0, 2},
                    {48, 0, 1, 13, 186, 0, 1, 1},
                    {49, 8, 2, 196, 90, 0, 1, 1},
                    {50, 83, 2, 182, 85, 0, 1, 1},
                    {51, 148, 0, 361, 98, 1, 1, 1},
                    {52, 81, 0, 354, 112, 1, 1, 1},
                    {53, 30, 0, 188, 52, 0, 1, 1},
                    {54, 30, 0, 188, 22, 0, 1, 1},
                    {55, 30, 0, 214, 22, 0, 1, 1},
                    {56, 30, 0, 214, 52, 0, 1, 1},
                    {57, 200, 0, 40, 304, 1, 0, 1},
                    {58, 8, 0, 143, 168, 0, 1, 1},
                    {59, 0, 2, 144, 236, 0, 1, 1},
                    {60, 7, 0, 76, 192, 0, 1, 1},
                    {61, 7, 0, 68, 55, 0, 1, 1},
                    {62, 83, 1, 222, 106, 0, 0, 2}
            };
            for (int i = 0; i < actors.length; i++) {
                actors[i] = null;
            }
            for (int[] row : rows) {
                Actor actor = new Actor(row[0], row[1], row[2], row[3], row[4], row[6], row[7]);
                actor.visible = row[5] == 1;
                actors[row[0]] = actor;
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        private void loadScene1Room0(int cameraCenterX, int cameraCenterY) {
            useMap = true;
            mapRenderer = loadMapRenderer(2);
            followActorId = -1;
            tempSprites.clear();
            int[][] rows = {
                    {0, 208, 1, 354, 150, 1, 0, 1},
                    {1, 208, 1, 52, 224, 1, 0, 1},
                    {2, 208, 1, 201, 86, 1, 0, 1},
                    {3, 213, 0, 192, 81, 1, 0, 0},
                    {4, 213, 0, 45, 220, 1, 0, 0},
                    {5, 213, 0, 346, 145, 1, 0, 0},
                    {6, 339, 0, 41, 30, 1, 0, 1},
                    {7, 339, 0, 42, 80, 1, 0, 1},
                    {8, 200, 1, 121, 48, 1, 0, 1},
                    {9, 200, 1, 282, 49, 1, 0, 1},
                    {10, 200, 1, 326, 49, 1, 0, 1},
                    {11, 200, 1, 370, 49, 1, 0, 1},
                    {12, 200, 1, 414, 50, 1, 0, 1},
                    {13, 225, 1, 73, 17, 1, 0, 2},
                    {14, 225, 0, 313, 63, 1, 0, 2},
                    {15, 225, 1, 392, 224, 1, 0, 2},
                    {16, 225, 0, 297, 273, 1, 0, 2},
                    {17, 225, 0, 122, 113, 1, 0, 2},
                    {18, 202, 0, 136, 127, 1, 0, 1},
                    {19, 202, 1, 260, 128, 1, 0, 1},
                    {20, 209, 0, 297, 145, 1, 0, 1},
                    {21, 230, 0, 53, 146, 1, 0, 0},
                    {22, 200, 1, -5, 278, 1, 0, 1},
                    {23, 200, 1, -5, 319, 1, 0, 1},
                    {24, 200, 1, 40, 321, 1, 0, 1},
                    {25, 200, 1, 89, 321, 1, 0, 1},
                    {26, 200, 1, 136, 321, 1, 0, 1},
                    {27, 200, 1, 326, 321, 1, 0, 1},
                    {28, 198, 0, 198, 272, 1, 0, 1},
                    {29, 202, 3, 292, 249, 1, 0, 1},
                    {30, 223, 3, 408, 273, 1, 1, 3},
                    {31, 223, 1, 200, 318, 1, 1, 2},
                    {32, 209, 0, 231, 98, 1, 0, 1},
                    {33, 69, 0, 127, 79, 1, 1, 1},
                    {34, 66, 0, 88, 234, 1, 1, 1},
                    {35, 66, 0, 294, 175, 1, 1, 1},
                    {36, 81, 1, 152, 212, 0, 1, 1},
                    {37, 201, 0, 200, 316, 1, 0, 1},
                    {38, 66, 1, 153, 233, 0, 1, 1},
                    {39, 52, 1, 148, 179, 0, 1, 1},
                    {40, 81, 1, 142, 249, 0, 1, 1},
                    {41, 23, 1, 124, 186, 0, 1, 1},
                    {42, 50, 1, 134, 202, 0, 1, 1},
                    {43, 53, 0, 158, 143, 0, 1, 1},
                    {44, 69, 1, 128, 231, 0, 1, 1},
                    {45, 54, 0, 242, 152, 0, 1, 1},
                    {46, 23, 1, 263, 170, 0, 1, 1},
                    {47, 69, 0, 280, 139, 0, 1, 1},
                    {48, 25, 0, 180, 153, 0, 1, 1},
                    {49, 23, 0, 208, 131, 0, 1, 1},
                    {50, 17, 0, 223, 160, 0, 1, 1},
                    {51, 66, 0, 386, 190, 1, 1, 1},
                    {52, 51, 0, 200, 190, 1, 1, 1},
                    {53, 137, 0, 149, 153, 0, 1, 1},
                    {54, 102, 0, 198, 148, 0, 1, 1},
                    {55, 92, 0, 246, 155, 0, 1, 1},
                    {56, 51, 0, 61, 234, 0, 1, 1},
                    {57, 8, 2, 224, 224, 0, 1, 1}
            };
            for (int i = 0; i < actors.length; i++) {
                actors[i] = null;
            }
            for (int[] row : rows) {
                Actor actor = new Actor(row[0], row[1], row[2], row[3], row[4], row[6], row[7]);
                actor.visible = row[5] == 1;
                actors[row[0]] = actor;
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        private void spawnActorEffect(int actorId, int animation) {
            if (actorId >= 0 && actorId < actors.length && actors[actorId] != null) {
                tempSprites.add(new TempSprite(actorId, animation, 120));
            }
        }

        private static void setActive(Scene s, int[] ids, int[] dirs) {
            for (int i = 0; i < ids.length; i++) {
                Actor a = s.actors[ids[i]];
                if (a != null) {
                    a.direction = dirs[i];
                    a.visible = true;
                }
            }
        }

        private static void hide(Scene s, int[] ids) {
            for (int id : ids) {
                if (s.actors[id] != null) {
                    s.actors[id].visible = false;
                }
            }
        }

        private static Event dialog(String speaker, String text) {
            return s -> {
                s.text = TextBox.dialog(s.font, speaker, text, 0);
                return waitForText();
            };
        }

        private static Event dialog(String speaker, String text, int mode) {
            return s -> {
                s.text = TextBox.dialog(s.font, speaker, text, mode);
                return waitForText();
            };
        }

        private static Blocking waitForText() {
            return sc -> {
                if (sc.text != null && sc.text.readyForKey && sc.key0) {
                    return sc.text.confirm();
                }
                return false;
            };
        }
    }

    private interface Event {
        Blocking start(Scene s);
    }

    private interface Blocking {
        boolean tick(Scene s);
    }

    private static final class Delay implements Blocking {
        private int left;

        private Delay(int left) {
            this.left = left;
        }

        @Override
        public boolean tick(Scene s) {
            return left-- <= 0;
        }
    }

    private static final class Opcode34Counter implements Blocking {
        private int n;
        private final int step;
        private int left;
        private boolean started;

        private Opcode34Counter(int n, int step, int left) {
            this.n = n;
            this.step = step;
            this.left = left;
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                return false;
            }
            left--;
            n -= step;
            if (left > 0) {
                return false;
            }
            left = 0;
            return true;
        }
    }

    private static final class Move implements Blocking {
        private final int[] ids, dx, dy, tx, ty;

        private Move(int[] ids, int[] dx, int[] dy, int[] tx, int[] ty) {
            this.ids = ids;
            this.dx = dx;
            this.dy = dy;
            this.tx = tx;
            this.ty = ty;
        }

        @Override
        public boolean tick(Scene s) {
            boolean done = true;
            for (int i = 0; i < ids.length; i++) {
                if (tx[i] > 0 || ty[i] > 0) {
                    done = false;
                    tx[i]--;
                    ty[i]--;
                    Actor a = s.actors[ids[i]];
                    a.x += dx[i];
                    a.y += dy[i];
                }
            }
            return done;
        }
    }

    private static final class ActionSet implements Blocking {
        private final int[] ids;
        private final int[] modes;
        private final int[] dirs;
        private final int[] waited;
        private final boolean[] done;
        private boolean started;

        private ActionSet(int[] ids, int[] modes, int[] dirs) {
            this.ids = ids;
            this.modes = modes;
            this.dirs = dirs;
            this.waited = new int[ids.length];
            this.done = new boolean[ids.length];
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                for (int i = 0; i < ids.length; i++) {
                    Actor actor = s.actors[ids[i]];
                    if (actor != null) {
                        actor.direction = dirs[i];
                        actor.applyMode(modes[i]);
                    }
                }
                return false;
            }
            boolean allDone = true;
            for (int i = 0; i < ids.length; i++) {
                if (done[i]) {
                    continue;
                }
                Actor actor = s.actors[ids[i]];
                waited[i]++;
                if (actor == null || actor.consumeCycleComplete() || waited[i] > 45) {
                    if (actor != null) {
                        actor.applyMode(0);
                    }
                    done[i] = true;
                } else {
                    allDone = false;
                }
            }
            return allDone;
        }
    }

    private static final class TimedAction implements Blocking {
        private final int[] ids;
        private final int[] dirs;
        private final int[] speeds;
        private final int[] durations;
        private final int[] remaining;
        private boolean started;
        private int left;

        private TimedAction(int[] ids, int[] dirs, int[] speeds, int[] durations) {
            this.ids = ids;
            this.dirs = dirs;
            this.speeds = speeds;
            this.durations = durations;
            this.remaining = Arrays.copyOf(durations, durations.length);
            for (int duration : durations) {
                left = Math.max(left, duration);
            }
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                for (int i = 0; i < ids.length; i++) {
                    Actor actor = s.actors[ids[i]];
                    if (actor != null) {
                        actor.direction = dirs[i];
                        actor.applyMode(3);
                    }
                }
                return false;
            }
            boolean allDone = true;
            for (int i = 0; i < ids.length; i++) {
                if (remaining[i] <= 0) {
                    continue;
                }
                Actor actor = s.actors[ids[i]];
                if (actor != null) {
                    actor.step(speeds[i]);
                }
                remaining[i]--;
                if (remaining[i] > 0) {
                    allDone = false;
                }
            }
            if (!allDone) {
                return false;
            }
            for (int id : ids) {
                Actor actor = s.actors[id];
                if (actor != null) {
                    actor.applyMode(0);
                }
            }
            return true;
        }
    }

    private static final class Path implements Blocking {
        private final int[] ids;
        private final int[][] xs;
        private final int[][] ys;
        private int step;

        private Path(int[] ids, int[][] xs, int[][] ys) {
            this.ids = ids;
            this.xs = xs;
            this.ys = ys;
        }

        @Override
        public boolean tick(Scene s) {
            if (step >= xs[0].length) {
                return true;
            }
            for (int i = 0; i < ids.length; i++) {
                Actor a = s.actors[ids[i]];
                a.x = xs[i][step];
                a.y = ys[i][step];
            }
            step++;
            return false;
        }
    }

    private static final class CameraPan implements Blocking {
        private final int actorId;
        private final int speed;

        private CameraPan(int actorId, int speed) {
            this.actorId = actorId;
            this.speed = speed;
        }

        @Override
        public boolean tick(Scene s) {
            Actor actor = s.actors[actorId];
            s.moveCameraToward(actor.x, actor.y, speed);
            return speed <= 0 || s.cameraCenteredOn(actor.x, actor.y);
        }
    }

    private static final class CameraPanPoint implements Blocking {
        private final int x;
        private final int y;
        private final int speed;

        private CameraPanPoint(int x, int y, int speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }

        @Override
        public boolean tick(Scene s) {
            s.moveCameraToward(x, y, speed);
            return speed <= 0 || s.cameraCenteredOn(x, y);
        }
    }

    private static final class ScriptedBattleStub implements Blocking {
        private final int actorId;
        private final int[] encounter;
        private final int[] flags;
        private final int[] battleMode;
        private final int[] branchTargets;
        private int phase;
        private int wait;

        private ScriptedBattleStub(int actorId, int[] encounter, int[] flags, int[] battleMode, int[] branchTargets) {
            this.actorId = actorId;
            this.encounter = encounter;
            this.flags = flags;
            this.battleMode = battleMode;
            this.branchTargets = branchTargets;
        }

        @Override
        public boolean tick(Scene s) {
            switch (phase) {
                case 0:
                    s.worldEventActor = actorId;
                    s.battleEventActor = actorId;
                    s.battleEncounter = Arrays.copyOf(encounter, encounter.length);
                    s.battleCanLose = flags.length > 0 && flags[0] == 0;
                    s.battleScriptLocksInput = flags.length > 1 && flags[1] == 0;
                    s.battleMode = battleMode.length > 0 ? battleMode[0] : -1;
                    s.battleBackgroundMode = battleMode.length > 1 ? battleMode[1] : -1;
                    s.battleResultIndex = 0;
                    s.battleBranchTarget = resolveBranch(s.battleResultIndex);
                    s.effect.startFade(2, 0);
                    phase = 1;
                    return false;
                case 1:
                    if (!s.effect.doneOverlay(s)) {
                        return false;
                    }
                    s.battleOverlayTicks = 90;
                    phase = 2;
                    return false;
                case 2:
                    s.battleOverlayTicks--;
                    if (s.battleOverlayTicks > 0) {
                        return false;
                    }
                    wait = 12;
                    phase = 3;
                    return false;
                case 3:
                    if (wait-- > 0) {
                        return false;
                    }
                    s.battleOverlayTicks = 0;
                    s.effect.startFade(1, 0);
                    phase = 4;
                    return false;
                case 4:
                    return s.effect.doneOverlay(s);
                default:
                    return true;
            }
        }

        private int resolveBranch(int resultIndex) {
            if (branchTargets.length == 0) {
                return -1;
            }
            int index = Math.max(0, resultIndex);
            if (index >= branchTargets.length) {
                index = branchTargets.length - 1;
            }
            return branchTargets[index];
        }
    }

    private static final class Actor {
        private final SpriteAnim anim;
        private final int variant;
        private final int layer;
        private int x, y;
        private int direction;
        private boolean visible;
        private boolean cycleComplete;

        private Actor(int id, int spriteIndex, int state, int x, int y) {
            this(id, spriteIndex, state, x, y, 0, 1);
        }

        private Actor(int id, int spriteIndex, int state, int x, int y, int variant) {
            this(id, spriteIndex, state, x, y, variant, 1);
        }

        private Actor(int id, int spriteIndex, int state, int x, int y, int variant, int layer) {
            this.anim = SpriteAnim.load(spriteIndex);
            this.anim.setState(state);
            this.variant = variant;
            this.layer = layer;
            this.x = x;
            this.y = y;
        }

        private void tick() {
            if (visible) {
                if (anim.tick()) {
                    cycleComplete = true;
                }
            }
        }

        private void setState(int state) {
            anim.setState(state);
        }

        private void applyMode(int mode) {
            cycleComplete = false;
            if (variant == 1 || variant == 18) {
                int h = mode / 3;
                if (h == 0) {
                    setState(direction == 3 ? 1 : direction);
                } else if (h == 1) {
                    setState(h * 3 + (direction == 3 ? 1 : direction));
                }
            } else {
                setState(mode);
            }
        }

        private boolean consumeCycleComplete() {
            boolean done = cycleComplete;
            cycleComplete = false;
            return done;
        }

        private void step(int speed) {
            int amount = Math.max(1, Math.abs(speed));
            switch (direction) {
                case 0:
                    y += amount;
                    break;
                case 1:
                    x += amount;
                    break;
                case 2:
                    y -= amount;
                    break;
                case 3:
                    x -= amount;
                    break;
                default:
                    break;
            }
        }

        private void render(Graphics2D g, int camX, int camY) {
            anim.draw(g, x - camX, y - camY, direction == 3 ? 1 : 0);
        }
    }

    private static final class TempSprite {
        private final int actorId;
        private final SpriteAnim anim = SpriteAnim.load(259);
        private int left;

        private TempSprite(int actorId, int animation, int duration) {
            this.actorId = actorId;
            this.left = duration;
            anim.setState(animation);
        }

        private boolean tick(Scene scene) {
            boolean cycleDone = anim.tick();
            return cycleDone || left-- <= 0 || actorId >= scene.actors.length || scene.actors[actorId] == null;
        }

        private void render(Graphics2D g, Scene scene) {
            Actor actor = scene.actors[actorId];
            if (actor != null && actor.visible) {
                anim.draw(g, actor.x - scene.cameraX, actor.y - scene.cameraY - 24, 0);
            }
        }
    }

    private static final class SpriteAnim {
        private static final int[][] SPRITE_TO_IMGS;
        private static final Map<Integer, SpriteData> CACHE = new HashMap<>();
        private final SpriteData data;
        private int state;
        private int cursor;
        private int delay;

        static {
            SPRITE_TO_IMGS = new int[400][];
            int[][] rows = {
                    {0, 0, 100}, {8, 8, 108}, {30, 30, 126},
                    {13, 13, 113}, {17, 17, 117}, {29, 29, 126}, {31, 31, 126}, {32, 32, 126},
                    {7, 7, 108}, {81, 81, 159}, {148, 148, 529},
                    {200, 200, 219}, {202, 202, 222}, {203, 203, 221},
                    {204, 204, 221}, {205, 205, 221}, {223, 223, 10023},
                    {225, 225, 218}, {243, 243, 232}, {244, 244, 232},
                    {289, 289, 259}, {328, 328, 820},
                    {65, 65, 145}, {270, 270, 250}, {271, 271, 249},
                    {273, 273, 251}, {275, 275, 254}, {314, 314, 249}, {342, 342, 839},
                    {84, 84, 162}, {85, 85, 163}, {101, 101, 604}, {117, 117, 605},
                    {133, 133, 606}, {149, 149, 607}, {161, 161, 608}, {173, 173, 609},
                    {185, 185, 610}, {262, 262, 300}, {264, 264, 305}, {266, 266, 303},
                    {83, 83, 161}, {247, 247, 238}, {259, 259, 811}, {282, 282, 261},
                    {284, 284, 261}, {265, 265, 301}, {267, 267, 307}, {326, 326, 164},
                    {327, 327, 818, 819}
            };
            for (int[] r : rows) {
                SPRITE_TO_IMGS[r[0]] = Arrays.copyOfRange(r, 2, r.length);
            }
        }

        private SpriteAnim(SpriteData data) {
            this.data = data;
            resetDelay();
        }

        private static SpriteAnim load(int spriteIndex) {
            int sprId = spriteIndex;
            SpriteData data = CACHE.computeIfAbsent(sprId, SpriteData::load);
            return new SpriteAnim(data);
        }

        private void setState(int state) {
            if (state >= 0 && state < data.anim.length) {
                this.state = state;
            } else {
                this.state = 0;
            }
            cursor = 0;
            resetDelay();
        }

        private boolean tick() {
            if (data.anim.length == 0) {
                return false;
            }
            if (delay > 0) {
                delay--;
                return false;
            }
            cursor++;
            boolean completed = false;
            if (cursor >= data.anim[state].length / 2) {
                cursor = 0;
                completed = true;
            }
            resetDelay();
            return completed;
        }

        private void resetDelay() {
            if (data.anim.length == 0 || data.anim[state].length == 0) {
                delay = 0;
                return;
            }
            delay = Math.max(0, data.anim[state][cursor * 2] - 1);
        }

        private void draw(Graphics2D g, int x, int y, int orientation) {
            if (data.anim.length == 0 || data.anim[state].length == 0) {
                return;
            }
            int cellId = data.anim[state][cursor * 2 + 1];
            if (cellId < 0 || cellId >= data.cells.length) {
                return;
            }
            int[] transformMap = orientation == 1
                    ? new int[]{2, 4, 1, 7, 0, 5, 3, 6}
                    : new int[]{0, 5, 3, 6, 2, 4, 1, 7};
            short[] cells = data.cells[cellId];
            for (int i = 0; i < cells.length; i += 4) {
                int frameId = cells[i];
                int ox = cells[i + 1];
                int oy = cells[i + 2];
                int tr = transformMap[cells[i + 3] & 7];
                if (orientation == 1) {
                    int w = data.frames[frameId][3];
                    int h = data.frames[frameId][4];
                    int adjust = (cells[i + 3] % 2 == 1) ? h : w;
                    drawRegion(g, data.imageForFrame(frameId), data.frames[frameId], tr, x - ox - adjust, y + oy);
                } else {
                    drawRegion(g, data.imageForFrame(frameId), data.frames[frameId], tr, x + ox, y + oy);
                }
            }
        }

        private static void drawRegion(Graphics2D g, BufferedImage img, short[] f, int transform, int x, int y) {
            int sx = f[1], sy = f[2], w = f[3], h = f[4];
            if (w <= 0 || h <= 0) {
                return;
            }
            BufferedImage sub = img.getSubimage(sx, sy, w, h);
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            switch (transform) {
                case 0:
                    break;
                case 1:
                    at.translate(w, 0);
                    at.scale(-1, 1);
                    break;
                case 2:
                    at.translate(w, 0);
                    at.scale(-1, 1);
                    break;
                case 3:
                    at.translate(w, h);
                    at.rotate(Math.PI);
                    break;
                case 4:
                    at.rotate(-Math.PI / 2);
                    at.scale(-1, 1);
                    break;
                case 5:
                    at.translate(h, 0);
                    at.rotate(Math.PI / 2);
                    break;
                case 6:
                    at.translate(0, w);
                    at.rotate(-Math.PI / 2);
                    break;
                case 7:
                    at.translate(h, w);
                    at.rotate(Math.PI / 2);
                    at.scale(-1, 1);
                    break;
                default:
                    break;
            }
            g.drawImage(sub, at, null);
        }
    }

    private static final class SpriteData {
        private final short[][] frames;
        private final short[][] cells;
        private final short[][] anim;
        private final BufferedImage[] images;

        private SpriteData(short[][] frames, short[][] cells, short[][] anim, BufferedImage[] images) {
            this.frames = frames;
            this.cells = cells;
            this.anim = anim;
            this.images = images;
        }

        private BufferedImage imageForFrame(int frameId) {
            int slot = frames[frameId][0];
            if (slot < 0 || slot >= images.length) {
                throw new IllegalStateException("Frame " + frameId + " references missing image slot " + slot);
            }
            return images[slot];
        }

        private static SpriteData load(int sprId) {
            try {
                byte[] bytes = readAll("/spr_" + sprId + "_all(r)");
                Cursor c = new Cursor();
                short[][] frames = asRows(readFlat(bytes, c));
                short[][] cells = readMatrix(bytes, c);
                short[][] anim = readMatrix(bytes, c);
                if (sprId >= 86 && sprId <= 185) {
                    short[][] special = new short[5][4];
                    short[] offset = {0, 10, 3, 7, -10};
                    for (int i = 0; i < special.length; i++) {
                        for (int j = 0; j < 4; j++) {
                            special[i][j] = j == 1 ? (short) (cells[0][j] + offset[i]) : cells[0][j];
                        }
                    }
                    cells = special;
                    anim = new short[][]{
                            {2, 0},
                            {1, 0, 1, 1, 1, 2, 1, 3, 1, 2},
                            {5, 0, 5, 4}
                    };
                }
                readFlat(bytes, c);
                readFlat(bytes, c);
                int[] imgIds = SpriteAnim.SPRITE_TO_IMGS[sprId];
                if (imgIds == null || imgIds.length == 0) {
                    throw new IOException("Missing image mapping for sprite " + sprId);
                }
                BufferedImage[] images = new BufferedImage[imgIds.length];
                for (int i = 0; i < imgIds.length; i++) {
                    images[i] = ImageIO.read(SpriteData.class.getResource("/img/" + imgIds[i] + ".png"));
                    if (images[i] == null) {
                        throw new IOException("Missing image /img/" + imgIds[i] + ".png");
                    }
                }
                return new SpriteData(frames, cells, anim, images);
            } catch (Exception ex) {
                return blank();
            }
        }

        private static SpriteData blank() {
            return new SpriteData(new short[0][0], new short[0][0], new short[0][0], new BufferedImage[0]);
        }

        private static short[][] asRows(short[] flat) {
            short[][] out = new short[flat.length / 5][5];
            for (int i = 0; i < out.length; i++) {
                System.arraycopy(flat, i * 5, out[i], 0, 5);
            }
            return out;
        }

        private static short[] readFlat(byte[] b, Cursor c) {
            int rows = readShort(b, c);
            int cols = readShort(b, c);
            if (rows == 0) {
                return null;
            }
            short[] out = new short[rows * cols];
            for (int i = 0; i < out.length; i++) {
                out[i] = (short) readShort(b, c);
            }
            return out;
        }

        private static short[][] readMatrix(byte[] b, Cursor c) {
            int count = readShort(b, c);
            int cols = readShort(b, c);
            if (count == 0) {
                return null;
            }
            short[][] out = new short[count][];
            for (int i = 0; i < count; i++) {
                int len = readShort(b, c);
                out[i] = new short[len * cols];
                for (int j = 0; j < out[i].length; j++) {
                    out[i][j] = (short) readShort(b, c);
                }
            }
            return out;
        }

        private static int readShort(byte[] b, Cursor c) {
            int v = ((b[c.pos++] & 0xFF) << 8) | (b[c.pos++] & 0xFF);
            return v >= 0x8000 ? v - 0x10000 : v;
        }
    }

    private static final class Cursor {
        private int pos;
    }

    private static final class FontBitmap {
        private final Map<Character, Integer> index = new HashMap<>();
        private final int[] widths;
        private final int[] offsets;
        private final byte[][] pixels;
        private final int height;
        private final int spaceWord;

        private FontBitmap() {
            try (DataInputStream in = new DataInputStream(VqsvIntroDemo.class.getResourceAsStream("/font.bin"))) {
                String chars = in.readUTF();
                height = in.readByte();
                widths = new int[chars.length()];
                offsets = new int[chars.length()];
                int total = 0;
                for (int i = 0; i < chars.length(); i++) {
                    widths[i] = in.readByte();
                    offsets[i] = total;
                    total += widths[i];
                    index.put(chars.charAt(i), i);
                }
                pixels = new byte[height][total];
                int bit = 7;
                int cur = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < total; x++) {
                        if (++bit >= 8) {
                            bit = 0;
                            cur = in.readByte();
                        }
                        if ((cur & 1) != 0) {
                            pixels[y][x] = 1;
                        }
                        cur >>= 1;
                    }
                }
                spaceWord = width("nhung1");
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private int charWidth(char c) {
            Integer i = index.get(c);
            return i == null ? 0 : widths[i];
        }

        private int width(String s) {
            int w = 0;
            for (int i = 0; i < s.length(); i++) {
                w += charWidth(s.charAt(i));
            }
            return w;
        }

        private void drawChar(Graphics2D g, char c, int x, int y) {
            Integer idx = index.get(c);
            if (idx == null) {
                return;
            }
            int off = offsets[idx];
            int w = widths[idx];
            for (int py = 0; py < height; py++) {
                for (int px = 0; px < w; px++) {
                    if (pixels[py][off + px] != 0) {
                        g.drawLine(x + px, y + py, x + px, y + py);
                    }
                }
            }
        }

        private void drawTagged(Graphics2D g, String s, int x, int y, int maxWidth, int visibleChars) {
            int cx = x;
            int cy = y;
            int color = 0xFFFFFF;
            g.setColor(new Color(color));
            int shown = 0;
            int softLimit = maxWidth - spaceWord;
            for (int i = 0; i < s.length() && shown < visibleChars; i++) {
                char ch = s.charAt(i);
                if (ch == '#' && i + 6 < s.length()) {
                    String hex = s.substring(i + 1, i + 7);
                    color = Integer.parseInt(hex, 16);
                    g.setColor(new Color(color));
                    i += 6;
                    continue;
                }
                int cw = charWidth(ch);
                int nx = cx + cw;
                if (nx > x + maxWidth - 10 || ch == ' ' && nx > x + softLimit) {
                    cx = x;
                    cy += height + 1;
                    if (ch == ' ') {
                        shown++;
                        continue;
                    }
                }
                drawChar(g, ch, cx, cy);
                cx += cw;
                shown++;
            }
        }
    }

    private static final class TextBox {
        private final int x, y, w, h;
        private final String text;
        private final List<String> pages;
        private final boolean waitKey;
        private final boolean fullBackdrop;
        private final boolean boxBackdrop;
        private final boolean dialogBackdrop;
        private final String speaker;
        private final int dialogMode;
        private int pageIndex;
        private int visibleChars;
        private int doneTicks;
        private boolean readyForKey;
        private boolean disposed;

        private TextBox(int x, int y, int w, int h, String text, boolean waitKey) {
            this(x, y, w, h, text, null, waitKey, false, false, false, "", -1);
        }

        private TextBox(int x, int y, int w, int h, String text, boolean waitKey, boolean fullBackdrop, boolean boxBackdrop) {
            this(x, y, w, h, text, null, waitKey, fullBackdrop, boxBackdrop, false, "", -1);
        }

        private TextBox(int x, int y, int w, int h, String text, List<String> pages, boolean waitKey,
                        boolean fullBackdrop, boolean boxBackdrop, boolean dialogBackdrop,
                        String speaker, int dialogMode) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.text = text;
            this.pages = pages;
            this.waitKey = waitKey;
            this.fullBackdrop = fullBackdrop;
            this.boxBackdrop = boxBackdrop;
            this.dialogBackdrop = dialogBackdrop;
            this.speaker = speaker;
            this.dialogMode = dialogMode;
        }

        private static TextBox full(int x, int y, String text, boolean waitKey) {
            return new TextBox(x, y, W - 2 * x, H - y, text, waitKey, true, false);
        }

        private static TextBox box(int x, int y, int w, int h, String text, boolean waitKey) {
            return new TextBox(x, y, w, h, text, waitKey);
        }

        private static TextBox dialog(FontBitmap font, String speaker, String text, int mode) {
            String tagged = "#000000" + text;
            List<String> pages = paginateTagged(font, tagged, 230, 4);
            return new TextBox(6, 264, 230, 52, tagged, pages, true,
                    false, false, true, speaker, mode);
        }

        private void tick() {
            int total = visibleLength(currentText());
            if (visibleChars < total) {
                visibleChars = Math.min(total, visibleChars + 2);
                doneTicks = 0;
            } else {
                doneTicks++;
                if (waitKey && doneTicks > 38) {
                    readyForKey = true;
                }
            }
        }

        private void render(Graphics2D g, FontBitmap font) {
            if (fullBackdrop) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, W, H);
            } else if (boxBackdrop) {
                g.setColor(Color.BLACK);
                g.fillRect(x - 4, y - 4, w + 8, h + 8);
                g.setColor(Color.WHITE);
                g.drawRect(x - 4, y - 4, w + 7, h + 7);
            } else if (dialogBackdrop) {
                renderDialogFrame(g, font);
            }
            font.drawTagged(g, currentText(), x, y, w, visibleChars);
            if (readyForKey && (doneTicks / 5) % 2 == 0) {
                if (dialogBackdrop) {
                    g.setColor(Color.BLACK);
                    int[] xs = {226, 234, 230};
                    int[] ys = {307, 307, 313};
                    g.fillPolygon(xs, ys, 3);
                } else {
                    String prompt = "Nhấn nút 0 để tiếp tục";
                    g.setColor(Color.WHITE);
                    int px = (W - font.width(prompt)) / 2;
                    font.drawTagged(g, prompt, px, H - 18, W, prompt.length());
                }
            }
        }

        private boolean confirm() {
            int total = visibleLength(currentText());
            if (visibleChars < total) {
                visibleChars = total;
                doneTicks = 39;
                readyForKey = true;
                return false;
            }
            if (pages != null && pageIndex + 1 < pages.size()) {
                pageIndex++;
                visibleChars = 0;
                doneTicks = 0;
                readyForKey = false;
                return false;
            }
            disposed = true;
            return true;
        }

        private String currentText() {
            if (pages == null || pages.isEmpty()) {
                return text;
            }
            return pages.get(pageIndex);
        }

        private void renderDialogFrame(Graphics2D g, FontBitmap font) {
            Color border = new Color(0, 174, 205);
            g.setColor(Color.WHITE);
            g.fillRect(0, 256, 240, 64);
            g.setColor(border);
            g.drawRect(0, 256, 239, 63);
            g.drawLine(1, 257, 238, 257);

            if (dialogMode == 0 || dialogMode == 1) {
                int tabX = dialogMode == 0 ? 1 : 178;
                g.setColor(Color.WHITE);
                g.fillRect(tabX, 231, 62, 25);
                g.setColor(border);
                g.drawRect(tabX, 231, 62, 25);
                g.drawLine(tabX + 1, 255, tabX + 61, 255);
                if (speaker != null && speaker.length() > 0 && !"??".equals(speaker)) {
                    font.drawTagged(g, "#000000" + speaker, tabX + 5, 239, 54, speaker.length());
                } else if ("??".equals(speaker)) {
                    font.drawTagged(g, "#000000??", tabX + 22, 239, 54, 2);
                }
            }
        }

        private static int visibleLength(String s) {
            int n = 0;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '#' && i + 6 < s.length()) {
                    i += 6;
                } else {
                    n++;
                }
            }
            return n;
        }

        private static List<String> paginateTagged(FontBitmap font, String s, int maxWidth, int maxLines) {
            List<String> out = new ArrayList<>();
            StringBuilder page = new StringBuilder();
            String color = "#FFFFFF";
            int line = 0;
            int width = 0;
            int softLimit = maxWidth - font.spaceWord;
            page.append(color);
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '#' && i + 6 < s.length()) {
                    color = s.substring(i, i + 7);
                    page.append(color);
                    i += 6;
                    continue;
                }
                int cw = font.charWidth(ch);
                int next = width + cw;
                if (next > maxWidth - 10 || ch == ' ' && next > softLimit) {
                    line++;
                    width = 0;
                    if (line >= maxLines) {
                        out.add(page.toString());
                        page = new StringBuilder();
                        page.append(color);
                        line = 0;
                    }
                    if (ch == ' ') {
                        continue;
                    }
                }
                page.append(ch);
                width += cw;
            }
            if (visibleLength(page.toString()) > 0 || out.isEmpty()) {
                out.add(page.toString());
            }
            return out;
        }
    }

    private static final class Effect {
        private int overlayType = -1;
        private int solidColor;
        private int tick;
        private int flashLimit;
        private int flashMode;
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
        private final int[] circleColors = {0xFFFFFF, 9115396};
        private BufferedImage[] particleImages;
        private Particle[] particles = new Particle[0];
        private final Random random = new Random(7);

        private void clearOverlay() {
            overlayType = -1;
            overlayDone = true;
        }

        private void startSolid(int color) {
            overlayType = 9;
            solidColor = color;
            overlayDone = false;
        }

        private void startFlash(int limit, int mode) {
            overlayType = 10;
            tick = 0;
            flashLimit = limit;
            flashMode = mode;
            overlayDone = false;
        }

        private void startFade(int type, int color) {
            overlayType = type;
            fadeType = type;
            fadeColor = color & 0xFFFFFF;
            fadeAlpha = type == 1 ? 255 : 0;
            tick = 0;
            overlayDone = false;
        }

        private void startBars(int type, int total, int step, int width, int top, int bottom) {
            barsMode = type;
            barsTotal = Math.max(1, total);
            barsStep = step;
            barsWidth = width;
            barsTop = top;
            barsBottom = bottom;
            barsProgress = 0;
            barsDone = false;
        }

        private void startCircle(int colorIndex, int state, int x, int y, int radius) {
            overlayType = 17;
            circleMode = colorIndex;
            circleState = state;
            circleX = x;
            circleY = y;
            circleR = radius;
            tick = 0;
            overlayDone = false;
        }

        private void startParticles(int count) {
            clearOverlay();
            try {
                particleImages = new BufferedImage[]{
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star0.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star1.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star2.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star3.png"))
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

        private void startFireParticles(int count) {
            clearOverlay();
            try {
                particleImages = new BufferedImage[]{
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/fire0.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/fire1.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/fire2.png"))
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

        private void stopParticles() {
            particleImages = null;
            particles = new Particle[0];
        }

        private boolean doneBars(Scene ignored) {
            return barsDone;
        }

        private boolean doneOverlay(Scene ignored) {
            return overlayDone;
        }

        private void tick() {
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
            }
        }

        private void renderParticles(Graphics2D g) {
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

        private void renderOverlay(Graphics2D g) {
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
            } else if (overlayType == 17) {
                g.setColor(new Color(circleColors[Math.max(0, Math.min(circleMode, 1))]));
                g.fillOval(circleX - circleR, circleY - circleR, circleR * 2, circleR * 2);
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

        private void resetParticle(Particle p) {
            int roll = random.nextInt(100);
            if (particleImages != null && particleImages.length == 3) {
                p.img = roll < 20 ? 2 : roll < 55 ? 1 : 0;
            } else {
                p.img = roll < 3 ? 3 : roll < 15 ? 2 : roll < 50 ? 1 : 0;
            }
            p.x = random.nextInt(W);
            p.y = random.nextInt(H);
            p.speed = random.nextInt(EffectSpeed[p.img][1] - EffectSpeed[p.img][0]) + EffectSpeed[p.img][0];
        }

        private static final int[][] EffectSpeed = {{1, 3}, {1, 4}, {2, 5}, {2, 6}};
    }

    private static final class Particle {
        private int img, x, y, speed;
    }

    private static byte[] readAll(String resource) throws IOException {
        try (InputStream in = VqsvIntroDemo.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing resource " + resource);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }
}

