import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.render.GameMap;
import com.vqsv.rebuild.render.MapModInfo;
import com.vqsv.rebuild.render.MapRenderer;
import com.vqsv.rebuild.render.SpriteTable;
import com.vqsv.rebuild.render.TileSet;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryTables;
import com.vqsv.rebuild.resource.ResourceLocator;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (args.length > 0 && "--smoke-drive".equals(args[0])) {
            String out = args.length > 1 ? args[1] : "build_intro_demo/smoke_drive.png";
            int preloadTicks = args.length > 2 ? Integer.parseInt(args[2]) : 5920;
            String route = args.length > 3 ? args[3] : "";
            int postTicks = args.length > 4 ? Integer.parseInt(args[4]) : 0;
            runSmokeDrive(out, preloadTicks, route, postTicks);
            return;
        }
        if (args.length > 0 && "--smoke-checkpoint".equals(args[0])) {
            String checkpoint = args.length > 1 ? args[1] : "room0_group2_first_dialog";
            String out = args.length > 2 ? args[2] : "build_intro_demo/smoke_checkpoint.png";
            runSmokeCheckpoint(checkpoint, out);
            return;
        }
        if (args.length > 0 && "--play-at".equals(args[0])) {
            int ticks = args.length > 1 ? Integer.parseInt(args[1]) : 0;
            openWindow(ticks);
            return;
        }
        if (args.length > 0 && "--play-drive".equals(args[0])) {
            int preloadTicks = args.length > 1 ? Integer.parseInt(args[1]) : 5920;
            String route = args.length > 2 ? args[2] : "";
            int postTicks = args.length > 3 ? Integer.parseInt(args[3]) : 0;
            openWindow(preloadTicks, route, postTicks);
            return;
        }
        openWindow(0);
    }

    private static void openWindow(int preloadTicks) {
        openWindow(preloadTicks, "", 0);
    }

    private static void openWindow(int preloadTicks, String route, int postTicks) {
        JFrame f = new JFrame("VQSV Liet Hoa - Intro Scene Rebuild");
        VqsvIntroDemo panel = new VqsvIntroDemo(preloadTicks, route, postTicks);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setContentPane(panel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        panel.start();
    }

    private static void tickSceneFastForward(Scene s, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            s.tick();
        }
    }

    private static void tickCurrentUntilDone(Scene s, int maxTicks) {
        int guard = 0;
        while (s.current != null && guard++ < maxTicks) {
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            s.tick();
        }
        if (s.current != null) {
            throw new IllegalStateException("Checkpoint current did not finish in " + maxTicks + " ticks");
        }
    }

    private static void revealCheckpointText(Scene s, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (s.text == null) {
                return;
            }
            s.text.tick(s.font);
        }
    }

    private static void runSmoke(String outPath, int ticks) {
        try {
            Scene s = new Scene();
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            tickSceneFastForward(s, ticks);
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

    private static void runSmokeDrive(String outPath, int preloadTicks, String route, int postTicks) {
        try {
            Scene s = new Scene();
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            tickSceneFastForward(s, preloadTicks);
            driveRoute(s, route);
            tickSceneFastForward(s, postTicks);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-drive-ok " + outPath + " preload=" + preloadTicks
                    + " route=" + route + " post=" + postTicks
                    + " room=[" + s.currentSceneId + "," + s.currentRoomIndex + "]"
                    + " player=[" + s.player.x + "," + s.player.y + "," + s.player.direction + "]"
                    + " eventIndex=" + s.eventIndex
                    + " state103=" + s.sourceEventState(1, 0, 3)
                    + " state106=" + s.sourceEventState(1, 0, 6)
                    + " state123=" + s.sourceEventState(1, 2, 3)
                    + " sourcePets=" + s.sourcePets.size()
                    + " money=" + s.sourceMoney
                    + " text=" + (s.text == null ? "none" : s.text.currentText()));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void runSmokeCheckpoint(String checkpoint, String outPath) {
        try {
            Scene s = new Scene();
            if ("room0_group2_first_dialog".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.dialog(s.font, "Neil", "B\u1ecb b\u1eaft", 0);
                for (int i = 0; i < 60; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_long_dialog".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.dialog(s.font, "Tr\u01b0\u1edfng th\u00f4n",
                        "Ti\u1ec3u t\u1eed th\u00fai, ng\u01b0\u01a1i bao nhi\u00eau tu\u1ed5i m\u00e0 l\u00ean m\u1eb7t d\u1ea1y ta h\u1ea3? \u0110\u00e2y, Cho ng\u01b0\u01a1i chu\u1ea9n b\u1ecb 3 s\u1ee7ng v\u1eadt. Hi\u1ec7n t\u1ea1i H\u1eafc Long Qu\u00e2n \u0111\u00e3 chi\u1ebfm l\u0129nh ph\u00e2n n\u1eeda \u0111\u1ea1i l\u1ee5c.",
                        1);
                for (int i = 0; i < 120; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_tasktip".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.taskTip("L\u1ef1a ch\u1ecdn s\u1ee7ng v\u1eadt c\u00f9ng tr\u01b0\u1edfng th\u00f4n t\u1ef7 th\u00ed.");
                for (int i = 0; i < 80; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_openbox".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.openBox("\u0110\u1ea1t \u0111\u01b0\u1ee3c: B\u00e1nh Sandwich x 10");
                for (int i = 0; i < 80; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_full_cutscene".equals(checkpoint)) {
                s.text = TextBox.full(30, 90,
                        "#FFFFFF Nghe \u0111\u1ed3n Thi\u00ean \u0110\u1ecba chi s\u01a1, v\u1ea1n n\u0103m v\u1ec1 tr\u01b0\u1edbc c\u00f3 hai v\u1ecb th\u1ea7n, m\u1ed9t ng\u01b0\u1eddi duy tr\u00ec tr\u1eadt t\u1ef1, m\u1ed9t ng\u01b0\u1eddi cai qu\u1ea3n th\u1ebf gi\u1edbi h\u1ed7n lo\u1ea1n.",
                        true);
                for (int i = 0; i < 160; i++) {
                    s.text.tick(s.font);
                }
            } else if ("battle_kidnapping".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                for (int i = 0; i < 50; i++) {
                    s.tick();
                }
            } else if ("battle_kidnapping_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                for (int i = 0; i < 80; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_capture".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                for (int i = 0; i < 140; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_capture_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                for (int i = 0; i < 190; i++) {
                    s.tick();
                }
            } else if ("battle_elder".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                for (int i = 0; i < 50; i++) {
                    s.tick();
                }
            } else if ("battle_elder_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                for (int i = 0; i < 260; i++) {
                    s.tick();
                }
            } else if ("route_sophie_after_battle_branch".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                tickCurrentUntilDone(s, 500);
                if (s.battleResultIndex != 0 || s.battleBranchTarget != 78) {
                    throw new IllegalStateException("Sophie battle branch mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.text = TextBox.dialog(s.font, "??",
                        "\u1ea2i \u1ea3i, kh\u00f4ng ph\u1ea3i ta khi d\u1ec5 ng\u01b0\u01a1i, l\u00e0 ng\u01b0\u01a1i kh\u00f4ng bi\u1ebft t\u1ef1 l\u01b0\u1ee3ng s\u1ee9c m\u00ecnh mu\u1ed1n c\u00f9ng ta \u0111\u1ea5u m\u1ed9t chuy\u1ebfn.",
                        0);
                revealCheckpointText(s, 120);
            } else if ("route_bunny_after_battle_task".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickCurrentUntilDone(s, 600);
                if (s.battleResultIndex != -1 || s.battleBranchTarget != -1) {
                    throw new IllegalStateException("Bunny battle branch mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.op23MarkEventComplete(1, 0, 1);
                s.op14CompleteEvent(1, 1, 0);
                s.text = TextBox.taskTip("Tr\u1edf v\u1ec1 t\u00ecm tr\u01b0\u1edfng th\u00f4n!");
                revealCheckpointText(s, 90);
            } else if ("route_elder_after_battle_reward_state".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickCurrentUntilDone(s, 800);
                if (s.battleResultIndex != 0 || s.battleBranchTarget != 10) {
                    throw new IllegalStateException("Elder battle branch mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.op31CurrencyReward(0, 0, 500);
                s.op17Item(0, 4, 10);
                s.op17Item(0, 11, 2);
                s.op19SpecialReward(5, 1);
                s.op23MarkEventComplete(1, 0, 4);
                s.op23MarkEventComplete(1, 0, 5);
                s.op14CompleteEvent(1, 0, 6);
                s.text = TextBox.openBox("Gi\u1edd c\u00f3 th\u1ec3 t\u1ef1 do di chuy\u1ec3n.");
                revealCheckpointText(s, 90);
            } else {
                throw new IllegalArgumentException("Unknown checkpoint: " + checkpoint);
            }
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " text=" + (s.text == null ? "none" : s.text.currentText())
                    + " battleResult=" + s.battleResultIndex
                    + " battleBranch=" + s.battleBranchTarget
                    + " battleHp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " battleLog=" + s.battleLog
                    + " state101=" + s.sourceEventState(1, 0, 1)
                    + " state110=" + s.sourceEventState(1, 1, 0)
                    + " state106=" + s.sourceEventState(1, 0, 6)
                    + " money=" + s.sourceMoney
                    + " pets=" + s.sourcePets.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void driveRoute(Scene s, String route) {
        for (String raw : route.split(",")) {
            String step = raw.trim();
            if ("0".equals(step)) {
                s.press0();
                s.tick();
                continue;
            }
            if (step.length() >= 2 && Character.toUpperCase(step.charAt(0)) == 'T') {
                int ticks = Integer.parseInt(step.substring(1));
                for (int i = 0; i < ticks; i++) {
                    if (s.text != null && s.text.readyForKey) {
                        s.press0();
                    }
                    s.tick();
                }
                continue;
            }
            if (step.length() < 2) {
                continue;
            }
            int keyCode = driveKeyCode(Character.toUpperCase(step.charAt(0)));
            if (keyCode == 0) {
                continue;
            }
            int ticks = Integer.parseInt(step.substring(1));
            s.setMoveKey(keyCode, true);
            for (int i = 0; i < ticks; i++) {
                if (s.text != null && s.text.readyForKey) {
                    s.press0();
                }
                s.tick();
            }
            s.setMoveKey(keyCode, false);
        }
    }

    private static int driveKeyCode(char dir) {
        switch (dir) {
            case 'U':
                return KeyEvent.VK_UP;
            case 'D':
                return KeyEvent.VK_DOWN;
            case 'L':
                return KeyEvent.VK_LEFT;
            case 'R':
                return KeyEvent.VK_RIGHT;
            default:
                return 0;
        }
    }

    private VqsvIntroDemo() {
        this(0);
    }

    private VqsvIntroDemo(int preloadTicks) {
        this(preloadTicks, "", 0);
    }

    private VqsvIntroDemo(int preloadTicks, String route, int postTicks) {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setFocusable(true);
        scene = new Scene();
        tickSceneFastForward(scene, preloadTicks);
        if (route != null && !route.isEmpty()) {
            driveRoute(scene, route);
        }
        tickSceneFastForward(scene, postTicks);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '0' || e.getKeyCode() == KeyEvent.VK_NUMPAD0) {
                    scene.press0();
                } else {
                    scene.setMoveKey(e.getKeyCode(), true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                scene.setMoveKey(e.getKeyCode(), false);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                scene.click(e.getX(), e.getY());
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
        private static int tenYearsEventIndex = -1;
        private final FontBitmap font = new FontBitmap();
        private final Effect effect = new Effect();
        private final Actor[] actors = makeActors();
        private final List<Event> events = makeEvents();
        private final List<TempSprite> tempSprites = new ArrayList<>();
        private final Actor player = new Actor(-1, 0, 0, 0, 0, 1, 1);
        private final WorldUi worldUi = new WorldUi();
        private MapRenderer mapRenderer;
        private TextBox text;
        private ChoiceBox choice;
        private int eventIndex = 0;
        private int currentSceneId = -1;
        private int currentRoomIndex = -1;
        private int cameraX = 0;
        private int cameraY = 0;
        private int playerX = 0;
        private int playerY = 0;
        private boolean useMap;
        private boolean key0;
        private boolean keyUp;
        private boolean keyDown;
        private boolean keyLeft;
        private boolean keyRight;
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
        private String battleEnemyName = "";
        private String battlePlayerName = "";
        private String battleLog = "";
        private int battleEnemyLevel;
        private int battlePlayerLevel;
        private int battleEnemyVisualId;
        private int battlePlayerVisualId;
        private int battleEnemyElement;
        private int battlePlayerElement;
        private int battleEnemyPowerPercent = 100;
        private int battlePlayerPowerPercent = 100;
        private int battleEnemyMaxHp;
        private int battleEnemyHp;
        private int battlePlayerMaxHp;
        private int battlePlayerHp;
        private int battleTurn;
        private int battlePlayerEnergy;
        private int battlePlayerMaxEnergy = 1;
        private boolean battleCaptureTutorial;
        private int sourceMoney;
        private int sourceBadges;
        private final Map<Integer, BagItem> sourceBagItems = initialSourceBagItems();
        private final Map<Integer, SourceSpecialReward> sourceSpecialRewards = new HashMap<>();
        private final Map<String, Byte> sourceEventStates = new HashMap<>();
        private final List<SourcePetState> sourcePets = new ArrayList<>();
        private final List<String> sourceStateTrace = new ArrayList<>();
        private boolean sourceGameCF = false;
        private int sourcePetRefreshOps = 0;

        private void press0() {
            key0 = true;
        }

        private void click(int screenX, int screenY) {
            int x = screenX / SCALE;
            int y = screenY / SCALE;
            if (choice != null && choice.click(x, y)) {
                key0 = true;
                return;
            }
            key0 = true;
        }

        private void setMoveKey(int keyCode, boolean pressed) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                case KeyEvent.VK_NUMPAD8:
                    keyUp = pressed;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                case KeyEvent.VK_NUMPAD2:
                    keyDown = pressed;
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                case KeyEvent.VK_NUMPAD4:
                    keyLeft = pressed;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                case KeyEvent.VK_NUMPAD6:
                    keyRight = pressed;
                    break;
                default:
                    break;
            }
        }

        private void tick() {
            effect.tick();
            if (text != null) {
                text.tick(font);
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
                    player.tick();
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
            player.tick();
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
            renderPlayer(g);
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
            worldUi.render(g, useMap);
            if (text != null) {
                text.render(g, font);
            }
            if (choice != null) {
                choice.render(g, font);
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
            String branch = "auto result " + battleResultIndex + " -> branch " + battleBranchTarget;
            font.drawTagged(g, "#FFFFFF" + branch, 16, 206, 208, branch.length());
            font.drawTagged(g, "#FFFFFFScripted stub", 16, 228, 208, 13);
        }

        private void renderSourceLikeBattleUi(Graphics2D g) {
            g.setColor(new Color(9, 42, 58));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(22, 82, 94));
            g.fillRect(0, 72, W, 92);
            g.setColor(new Color(17, 54, 82));
            g.fillRect(0, 164, W, 71);
            drawBattleUiCellTopLeft(g, 92, 0, 0);
            drawBattleUiCellTopLeft(g, 93, 0, 235);
            drawBattleUiCellTopLeft(g, 158, 101, 1);
            drawBattleSprite(g, battleEnemyVisualId, 132, 70, 96, 118, 7, 0);
            drawBattleSprite(g, battlePlayerVisualId, 18, 140, 96, 95, 7, 0);

            drawBattleUiCellTopLeft(g, 101, 97, 14);
            drawBattleCommandBar(g);

            font.drawTagged(g, "#FFFFFF" + battleEnemyName, 3, 2, 58, battleEnemyName.length());
            font.drawTagged(g, "#FFFFFFlv" + battleEnemyLevel, 64, 2, 36, 4);
            drawBattleProgressWidget(g, 5, 16, 82, hpPercent(battleEnemyHp, battleEnemyMaxHp), 0x9B9B9B);
            drawBattleProgressWidget(g, 5, 16, 82, hpPercent(battleEnemyHp, battleEnemyMaxHp), 0x59F148);
            drawBattleProgressWidget(g, 5, 16, 82, hpPercent(battleEnemyHp, battleEnemyMaxHp), 0xFFFFFF);
            String enemyHp = battleEnemyHp + "/" + battleEnemyMaxHp;
            font.drawTagged(g, "#fff9b1" + enemyHp, 16, 13, 72, enemyHp.length());
            drawBattleUiCellTopLeft(g, 94 + Math.max(0, battleEnemyElement), 92, 2);
            drawBattlePercent(g, 124, 2, battleEnemyPowerPercent);
            drawStatusSlots(g, 2, 25, 10, 30, false);

            font.drawTagged(g, "#FFFFFF" + battlePlayerName, 153, 238, 58, battlePlayerName.length());
            font.drawTagged(g, "#FFFFFFlv" + battlePlayerLevel, 214, 238, 26, 4);
            drawBattleProgressWidget(g, 153, 252, 82, hpPercent(battlePlayerHp, battlePlayerMaxHp), 0x9B9B9B);
            drawBattleProgressWidget(g, 153, 252, 82, hpPercent(battlePlayerHp, battlePlayerMaxHp), 0x59F148);
            drawBattleProgressWidget(g, 153, 252, 82, hpPercent(battlePlayerHp, battlePlayerMaxHp), 0xFFFFFF);
            String playerHp = battlePlayerHp + "/" + battlePlayerMaxHp;
            font.drawTagged(g, "#fff9b1" + playerHp, 167, 249, 66, playerHp.length());
            String playerEnergy = battlePlayerEnergy + "/" + battlePlayerMaxEnergy;
            font.drawTagged(g, "#fff9b1" + playerEnergy, 81, 258, 72, playerEnergy.length());
            drawBattleUiCellTopLeft(g, 94 + Math.max(0, battlePlayerElement), 139, 249);
            drawBattlePercent(g, 104, 248, battlePlayerPowerPercent);
            drawStatusSlots(g, 226, 221, 234, 226, true);

            font.drawTagged(g, "#FFFFFF" + battleLog, 29, 261, 202, battleLog.length());
            if (battleCaptureTutorial) {
                font.drawTagged(g, "#fff9b1B\u1eaft", 48, 299, 28, 3);
            }
        }

        private void drawBattleUiCellTopLeft(Graphics2D g, int cellId, int x, int y) {
            SpriteAnim ui = SpriteAnim.load(257);
            int[] bounds = ui.cellBounds(cellId);
            if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
                return;
            }
            ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
        }

        private void drawSpriteCellTopLeft(Graphics2D g, int spriteIndex, int cellId, int x, int y) {
            SpriteAnim sprite = SpriteAnim.load(spriteIndex);
            int[] bounds = sprite.cellBounds(cellId);
            if (bounds == null || bounds[2] <= 0 || bounds[3] <= 0) {
                return;
            }
            sprite.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
        }

        private void drawBattleSprite(Graphics2D g, int spriteIndex, int x, int y, int w, int h, int align, int orientation) {
            if (spriteIndex < 0) {
                return;
            }
            SpriteAnim.load(spriteIndex).drawAligned(g, x, y, w, h, align, orientation);
        }

        private void drawBattlePanel(Graphics2D g, int x, int y, int w, int h, boolean fill) {
            if (fill) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(x, y, w, h);
            }
            g.setColor(new Color(232, 244, 255));
            g.drawRect(x, y, w - 1, h - 1);
            g.setColor(new Color(52, 88, 105));
            g.drawRect(x + 1, y + 1, w - 3, h - 3);
        }

        private void drawBattleCommandBar(Graphics2D g) {
            String[][] labels = {
                    {"Chi\u1ebfn", "\u0111\u1ea5u"},
                    {"B\u1eaft", "\u0111\u01b0\u1ee3c"},
                    {"\u0110\u1ea1o", "c\u1ee5"},
                    {"S\u1ee7ng", "v\u1eadt"},
                    {"Th\u01b0\u01a1ng", "\u0111i\u1ec3m"},
                    {"Ch\u1ea1y", "tr\u1ed1n"}
            };
            int[] textXs = {7, 48, 88, 128, 168, 208};
            int[] iconXs = {20, 56, 98, 137, 176, 218};
            for (int i = 0; i < iconXs.length; i++) {
                drawBattleUiCellTopLeft(g, 31, iconXs[i], 293);
            }
            for (int i = 0; i < labels.length; i++) {
                drawTinyBattleText(g, labels[i][0], textXs[i], 299, 34, Color.WHITE);
                drawTinyBattleText(g, labels[i][1], textXs[i], 309, 34, Color.WHITE);
            }
        }

        private void drawStatusSlots(Graphics2D g, int iconStartX, int iconY, int overlayStartX, int overlayY, boolean rightToLeft) {
            for (int i = 0; i < 6; i++) {
                int dx = rightToLeft ? -i * 15 : i * 15;
                drawSpriteCellTopLeft(g, 325, 0, iconStartX + dx, iconY);
                drawBattleUiCellTopLeft(g, 145, overlayStartX + dx, overlayY);
            }
        }

        private void drawBattlePercent(Graphics2D g, int x, int y, int percent) {
            Color color = percent > 100 ? new Color(0xfff1a0) : percent < 100 ? new Color(0xb8d8ff) : Color.WHITE;
            drawTinyBattleText(g, percent + "%", x, y + 1, 28, color);
        }

        private void drawTinyBattleText(Graphics2D g, String text, int x, int y, int width, Color color) {
            Shape oldClip = g.getClip();
            g.clipRect(x, y - 1, width, 18);
            font.drawTaggedLine(g, text, x, y, TextBox.visibleLength(TextBox.decodeMojibake(text)),
                    color.getRGB() & 0xFFFFFF);
            g.setClip(oldClip);
        }

        private void drawBattleProgressWidget(Graphics2D g, int x, int y, int w, int percent, int color) {
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

        private void drawBattleHpTrack(Graphics2D g, int x, int y, int w, int hp, int maxHp) {
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
                s.text = TextBox.full(30, 90, "#FFFFFF Nghe Ä‘á»“n ThiÃªn Äá»‹a chi sÆ¡, váº¡n nÄƒm vá» trÆ°á»›c cÃ³ hai vá»‹ tháº§n, má»™t ngÆ°á»i duy trÃ¬ tráº­t tá»±, má»™t ngÆ°á»i cai quáº£n tháº¿ giá»›i há»—n loáº¡n, kiá»m cháº¿ láº«n nhau, duy trÃ¬ cÃ¢n báº±ng cá»§a tháº¿ giá»›i.", true);
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
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vi Báº¡ch Long, vá»‹ tháº§n Ä‘á»©ng Ä‘áº§u ThiÃªn Giá»›i phá»¥ trÃ¡ch cai quáº£n tráº­t tá»±. Ba vá»‹ thá»§ há»™ thÃ¡nh thÃº láº§n lÆ°á»£t lÃ  LÃ´i Ká»³ LÃ¢n, Tinh VÃ¢n Háº¡c cÃ¹ng Minh VÆ°Æ¡ng Long.", false);
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
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vi Háº¯c Long, vá»‹ tháº§n Ä‘á»©ng Ä‘áº§u Äá»‹a Giá»›i phá»¥ trÃ¡ch cai quáº£n tháº¿ giá»›i há»—n loáº¡n. Bá»‘n vá»‹ chiáº¿n tháº§n thÃº láº§n lÆ°á»£t lÃ  Chiáº¿n Tháº§n ÄÃ , TÆ°Æ¡ng QuÃ¢n Giáº£i, Linh Quang Lá»™c vÃ  Há»a PhÆ°á»£ng HoÃ ng.", false);
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
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Máº¥y ngÃ n nÄƒm trÆ°á»›c, lá»±c lÆ°á»£ng há»—n Ä‘á»™n tháº¿ lá»±c khÃ´ng ngá»«ng lá»›n máº¡nh, dáº§n hÃ¬nh thÃ nh xu tháº¿ Ä‘Ã n Ã¡p ThiÃªn Giá»›i. Äá»ƒ cÃ¢n báº±ng giá»¯a ThiÃªn Äá»‹a, Báº¡ch Long cÃ¹ng Háº¯c Long Ä‘Ã£ tiáº¿n hÃ nh má»™t cuá»™c ThiÃªn Äá»‹a thÃ¡nh chiáº¿n.", false);
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
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Máº¥y trÄƒm nÄƒm sau, cuá»™c chiáº¿n káº¿t thÃºc, Báº¡ch Long cÃ¹ng Háº¯c Long Ä‘á»u tan biáº¿n.", false);
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
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF KhÃ´ng lÃ¢u sau Ä‘Ã³, tháº¿ gian xuáº¥t hiá»‡n hai Báº£o ChÃ¢u, má»™t tráº¯ng, má»™t Ä‘en. NgÆ°á»i ta tin ráº±ng Ä‘Ã¢y chÃ­nh lÃ  linh há»“n cá»§a cÃ¡c vá»‹ tháº§n cá»• Ä‘áº¡i, cÃ³ nÄƒng lÆ°á»£ng vÃ´ táº­n.", false);
                return null;
            });
            e.add(s -> new Delay(140));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vá» sau, hai Báº£o ChÃ¢u nÃ y, má»™t bay lÃªn ThiÃªn Giá»›i, má»™t rÆ¡i xuá»‘ng nhÃ¢n gian, tiáº¿p tá»¥c sá»© má»‡nh báº£o vá»‡ tháº¿ giá»›i.", false);
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
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF ThiÃªn Giá»›i vÃ  Äá»‹a Giá»›i cÃ³ má»‘i liÃªn há»‡ duy nháº¥t thÃ´ng Ä‘áº¡o ThiÃªn Giá»›i Báº¡ch Long Tháº§n Äiá»‡n cÃ¹ng Äá»‹a Giá»›i Háº¯c Long Tháº§n Äiá»‡n. Cá»© sau má»™t trÄƒm nÄƒm, hai tÃ²a tháº§n Ä‘iá»‡n má»Ÿ lá»‘i Ä‘i thÃ´ng nhau vÃ o má»™t ngÃ y Ä‘á»ƒ ngÆ°á»i hai giá»›i cÃ³ thá»ƒ gáº·p gá»¡. NhÆ°ng má»™t trÄƒm nÄƒm má»›i cÃ³ má»™t cÆ¡ há»™i nÃªn cÃ³ thá»ƒ nÃ³i Ä‘Ã¢y cÅ©ng khÃ´ng háº³n Ä‘Ã£ lÃ  niá»m vui cho nhÃ¢n loáº¡i.", false);
                return null;
            });
            e.add(s -> new CameraPanPoint(340, 412, 2));
            e.add(s -> new Delay(200));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFFHáº¯c Tháº¡ch ThÃ nh MÃ£ Äáº§u: Ha ha! Tuy lÃ  trÄƒm nÄƒm má»›i cÃ³ má»™t dá»‹p nhÆ°ng Ä‘Ã¢y cÅ©ng lÃ  cÆ¡ há»™i tá»‘t. Ã trá»i Ä‘Ã£ Ä‘á»‹nh! ChÃºng quÃ¢n nghe lá»‡nh!", false);
                return null;
            });
            e.add(s -> new Delay(110));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Háº¯c Long QuÃ¢n:!", false);
                return null;
            });
            e.add(s -> new Delay(35));
            e.add(s -> {
                s.text = TextBox.full(30, 90, "#FFFFFF NgÃ y nÃ o Ä‘Ã³, Táº¥t cáº£ thiÃªn khÃ´ng tháº§n Ä‘iá»‡n cÅ©ng khÃ´ng thá»ƒ thoÃ¡t khá»i kiáº¿p Ä‘á»‹nh nÃ y. ÄÃ¢y khÃ´ng pháº£i chiáº¿n tranh, cuá»™c chiáº¿n cá»§a má»™t phe, cÄƒn báº£n chÃ­nh lÃ ... Cháº¿t chÃ³c.", true);
                return waitForText();
            });
            e.add(s -> { s.effect.startBars(12, 1, 1, 240, 10, 50); return s.effect::doneBars; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.loadRoom1(340, 412);
                s.text = TextBox.full(30, 90, "#FFFFFF Má»™t ngÃ y sau Ä‘Ã³, trÆ°á»›c má»™t ngÃ´i Ä‘á»n hoang ...", true);
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
                s.text = TextBox.full(60, 90, "#FFFFFF SÃ¡u nÄƒm sau ...", true);
                return waitForText();
            });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{48, 49, 50}, new int[]{1, 2, 2}); return null; });
            e.add(s -> new CameraPan(49, 0));
            e.add(s -> new Delay(15));
            e.add(s -> new CameraPan(48, 10));
            e.add(dialog("Neil", "Äáº¿n Ä‘Ã¢y Ä‘i! Sophie ~ TÃ¬m khÃ´ng tháº¥y ta Ä‘Ã¢u~~~ Ha ha"));
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
            e.add(dialog("Sophie", "... HÃª hÃª ... Ã´ng trá»‘n sau Ä‘Ã¡ Peepna cá»§a tÃ´i nhÃ¬n lÃ©n chá»© gÃ¬? ~ Mau ra Ä‘Ã¢y ~"));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{0}, new int[]{4}, new int[]{6}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{1}, new int[]{4}, new int[]{13}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{8}));
            e.add(dialog("Neil", "áº¶c, sao phÃ¡t hiá»‡n giá»i váº­y ta?..."));
            e.add(s -> { s.spawnActorEffect(49, 7); return null; });
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(dialog("Sophie", "Hun? Tháº­t Ä‘áº¥y ~"));
            e.add(dialog("Neil", "Ãch ... sá»›m biáº¿t khÃ´ng pháº£i."));
            e.add(dialog("Sophie", "HÃ¬ hÃ¬ ~ thá»i gian khÃ´ng cÃ²n sá»›m, chÃºng ta mau trá»Ÿ vá» ~"));
            e.add(s -> new TimedAction(new int[]{48, 49, 50}, new int[]{0, 0, 0}, new int[]{4, 4, 4}, new int[]{13, 13, 13}));
            e.add(s -> new TimedAction(new int[]{48, 49, 50}, new int[]{1, 1, 1}, new int[]{4, 4, 4}, new int[]{20, 20, 20}));
            e.add(s -> new ActionSet(new int[]{48}, new int[]{2}, new int[]{2}));
            e.add(dialog("Neil", "Sophie, Ä‘Ã£ qua vÃ i nÄƒm tÃ´i muá»‘n gáº·p cha máº¹ cáº­u."));
            e.add(s -> new ActionSet(new int[]{49}, new int[]{0}, new int[]{0}));
            e.add(dialog("Sophie", "Ai? VÃ¬ sao chá»©?"));
            e.add(s -> { s.spawnActorEffect(48, 8); return null; });
            e.add(dialog("Neil", "ÄÆ°Æ¡ng nhiÃªn lÃ  bá»Ÿi vÃ¬ chÆ°a tá»«ng gáº·p há»!"));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{2, 2}, new int[]{4, 4}, new int[]{16, 16}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(dialog("Sophie", "Thá»±c ra..., chÃ­nh ta cÅ©ng chÆ°a tá»«ng Ä‘Æ°á»£c gáº·p há». Má»i ngÆ°á»i Ä‘á»u nÃ³i cha máº¹ ta Ä‘Ã£ máº¥t trong chiáº¿n tranh. Táº¥t cáº£ nhá»¯ng gÃ¬ cÃ²n láº¡i cá»§a há» chá»‰ cÃ³ chiáº¿c vÃ²ng cá»• nÃ y."));
            e.add(dialog("Neil", "..."));
            e.add(dialog("Sophie", "Neil, trÃ´ng bá»™ dáº¡ng cÃ³ váº» tÃ¢m tráº¡ng tháº¿ háº£?"));
            e.add(dialog("Neil", "á»œ thÃ¬ ngÆ°á»i ta Ä‘á»“ng cáº£m vá»›i cáº£nh ngá»™ cá»§a cáº­u! ÄÃ¡ng thÆ°Æ¡ng quÃ¡. Hix"));
            e.add(dialog("Sophie", "Ta khÃ´ng cáº£m tháº¥y váº­y. Máº·c dÃ¹ ta cÅ©ng muá»‘n cÃ³ cha máº¹, nhÆ°ng ta cÃ³ gia gia, cÃ³ Neil lÃ m báº¡n tháº¿ lÃ  Ä‘Ã£ quÃ¡ Ä‘á»§ rá»“i! Nháº¥t lÃ  Ä‘Æ°á»£c sá»‘ng má»™t nÆ¡i vá»›i Neil lÃ  niá»m vui lá»›n nháº¥t cá»§a ta!"));
            e.add(s -> new Delay(15));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{10}));
            e.add(dialog("Neil", "Váº­y chÃºng ta sáº½ cÃ¹ng nhau Ä‘i Ä‘áº¿n báº¥t cá»© Ä‘Ã¢u."));
            e.add(s -> { s.spawnActorEffect(49, 5); return null; });
            e.add(dialog("Sophie", "Tháº­t sá»± sao? Neil cÃ¹ng vá»›i Sophie sao?"));
            e.add(dialog("Neil", "..."));
            e.add(dialog("Sophie", "NÃ³i láº¡i Ä‘i ~ Neil tháº­t sá»± sáº½ cÃ¹ng vá»›i Sophie sao?"));
            e.add(dialog("Neil", "ÄÆ°Æ¡ng nhiÃªn! Nam nhÃ¢n Ä‘áº¡i trÆ°á»£ng phu nÃ³i má»™t lá»i khÃ´ng thay Ä‘á»•i! Ta sáº½ á»Ÿ bÃªn, báº£o vá»‡, khÃ´ng cho báº¥t cá»© ai lÃ m tá»•n thÆ°Æ¡ng Sophie!"));
            e.add(s -> { s.spawnActorEffect(49, 14); return null; });
            e.add(dialog("Sophie", " Hay quÃ¡, ta Æ°á»›c Ä‘Æ°á»£c cÃ¹ng Neil sá»‘ng chung má»™t nÆ¡i, vÄ©nh viá»…n khÃ´ng xa rá»i nhau."));
            e.add(dialog("Neil", "á»ª, nháº¥t Ä‘á»‹nh."));
            e.add(s -> { setActive(s, new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}); return null; });
            e.add(s -> new TimedAction(new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}, new int[]{4, 4, 4, 4}, new int[]{23, 23, 23, 23}));
            e.add(s -> new TimedAction(new int[]{53, 54, 55, 56}, new int[]{3, 3, 3, 3}, new int[]{4, 4, 4, 4}, new int[]{15, 15, 15, 15}));
            e.add(s -> new ActionSet(new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}));
            e.add(dialog("??", "TÃ¬m Ä‘Æ°á»£c rá»“i! Rá»‘t cuá»™c Ä‘Ã£ tÃ¬m Ä‘Æ°á»£c! NgÆ°á»i mang dáº¥u áº¥n mÃ u há»“ng!"));
            e.add(s -> { s.spawnActorEffect(48, 7); return null; });
            e.add(s -> { s.spawnActorEffect(49, 7); return null; });
            e.add(s -> new ActionSet(new int[]{49}, new int[]{2}, new int[]{2}));
            e.add(dialog("Neil", "CÃ¡c ngÆ°Æ¡i muá»‘n lÃ m gÃ¬!?"));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(s -> new TimedAction(new int[]{53, 49, 56, 50}, new int[]{0, 0, 0, 0}, new int[]{6, 4, 6, 4}, new int[]{4, 4, 4, 4}));
            e.add(s -> new TimedAction(new int[]{53, 49, 56, 50}, new int[]{2, 2, 2, 2}, new int[]{4, 4, 4, 4}, new int[]{6, 6, 6, 6}));
            e.add(dialog("Sophie", "A a a! Tháº£ ta ra! Neil!"));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{6}));
            e.add(dialog("Neil", "Há»—n xÆ°á»£c! BuÃ´ng Sophie ra!"));
            e.add(s -> new ActionSet(new int[]{53, 56, 49}, new int[]{0, 0, 0}, new int[]{0, 0, 0}));
            e.add(dialog("??", "Ãi chÃ , xem ra tiá»ƒu tá»­ nÃ y muá»‘n lÃ m anh hÃ¹ng cá»©u má»¹ nhÃ¢n Ä‘Ã¢y."));
            e.add(dialog("??", "Giáº£i quyáº¿t nhanh tÃªn nÃ y trá»Ÿ vá» phá»¥c má»‡nh."));
            e.add(s -> new SourceBattleRuntime(
                    56,
                    new int[]{5, 20, 4},
                    new int[]{1, 1},
                    new int[]{0, 2},
                    new int[]{78, 78, 0}));
            e.add(s -> { hide(s, new int[]{50}); return null; });
            e.add(dialog("??", "áº¢i áº£i, khÃ´ng pháº£i ta khi dá»… ngÆ°Æ¡i, lÃ  ngÆ°Æ¡i khÃ´ng biáº¿t tá»± lÆ°á»£ng sá»©c mÃ¬nh muá»‘n cÃ¹ng ta Ä‘áº¥u má»™t chuyáº¿n."));
            e.add(dialog("Sophie", "Neil! Cáº­u lÃ m sao...?!"));
            e.add(dialog("Neil", "YÃªn tÃ¢m, Ta cÃ²n cÃ³ thá»ƒ..."));
            e.add(s -> { s.spawnActorEffect(49, 6); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Sophie", "Neil! Neil! Cháº¡y mau Ä‘i!"));
            e.add(dialog("??", "Äi thÃ´i, khÃ´ng cÃ³ thá»i gian Ä‘Ã¹a vá»›i tÃªn tiá»ƒu tá»­ Ä‘Ã³."));
            e.add(s -> new TimedAction(new int[]{49, 53, 54, 55, 56}, new int[]{1, 1, 1, 1, 1}, new int[]{4, 4, 4, 4, 4}, new int[]{15, 15, 15, 15, 15}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{4}));
            e.add(s -> new TimedAction(new int[]{49, 53, 54, 55, 56}, new int[]{2, 2, 2, 2, 2}, new int[]{4, 4, 4, 4, 4}, new int[]{23, 23, 23, 23, 23}));
            e.add(s -> { hide(s, new int[]{49, 53, 54, 55, 56}); return null; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> new Opcode34Counter(70, 0, 0));
            e.add(s -> {
                s.text = TextBox.box(20, 220, 200, 40, "#FFFFFF Tá»©c tháº­t! Sophie! Tráº£ Sophie láº¡i cho ta ...!(vá»«a má»›i thá» sáº½ báº£o vá»‡ nÃ ng. Vá»«a má»›i há»©a háº¹n Ä‘i Ä‘Ã¢u cÅ©ng cÃ³ nhau, vÄ©nh viá»…n Ä‘em láº¡i niá»m vui cho Sophie. Tháº¿ mÃ ...)", true);
                return waitForText();
            });
            e.add(s -> new Delay(30));
            e.add(s -> {
                s.text = TextBox.full(60, 90, "#FFFFFF ÄÃ¡m xáº¥c xÆ°á»£c nÃ y! HÃ£y khoan!", true);
                return waitForText();
            });
            e.add(s -> { s.spawnActorEffect(48, 1); return null; });
            e.add(dialog("Neil", "ÄÃ³ lÃ  ... cÃ¡i gÃ¬ ...?"));
            e.add(s -> { s.effect.startIcon("ikon_1", 120, 100, 10); return s.effect::doneOverlay; });
            e.add(dialog("Neil", "Sophie, vÃ²ng cá»• ..."));
            e.add(s -> { s.spawnActorEffect(48, 13); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Neil", "KhÃ´ng, lÃ  ta khÃ´ng Ä‘á»§ máº¡nh... má»™t ngÃ y nÃ o Ä‘Ã³ ... má»™t ngÃ y nÃ o Ä‘Ã³!!!"));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(199, 218, 240, 320, 2);
                s.markWorldTransition(1, 0, -1);
                return null;
            });
            e.add(s -> {
                s.loadScene1Room0(s.transitionCenterX, s.transitionCenterY);
                return null;
            });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            // scene_1 room0 group0, records 0..29. Gameplay/task side effects remain approximate.
            tenYearsEventIndex = e.size();
            e.add(s -> {
                s.text = TextBox.full(60, 90, VqsvText.Scene1Room0Group0.TEN_YEARS_TITLE, true);
                return waitForText();
            });
            e.add(s -> { setActive(s,
                    new int[]{36, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51},
                    new int[]{1, 1, 1, 1, 1, 1, 0, 1, 0, 3, 1, 0, 0, 0, 3}); return null; });
            e.add(s -> { s.setPlayerPositionApprox(199, 218); s.player.direction = 2; return null; });
            e.add(s -> new Delay(30));
            e.add(s -> {
                s.text = TextBox.box(10, 260, 220, 50, VqsvText.Scene1Room0Group0.NOISE, false);
                return null;
            });
            e.add(s -> new Delay(60));
            e.add(s -> { s.spawnActorEffect(36, 13); return null; });
            e.add(dialog(VqsvText.Scene1Room0Group0.ALI, VqsvText.Scene1Room0Group0.ALI_TALENT));
            e.add(s -> { s.spawnActorEffect(50, 13); return null; });
            e.add(dialog(VqsvText.Scene1Room0Group0.TITAN, VqsvText.Scene1Room0Group0.TITAN_REPLY));
            e.add(s -> { s.spawnActorEffect(36, 13); return null; });
            e.add(dialog(VqsvText.Scene1Room0Group0.ALI, VqsvText.Scene1Room0Group0.ALI_MOTIVE));
            e.add(dialog(VqsvText.Scene1Room0Group0.ELDER, VqsvText.Scene1Room0Group0.ELDER_HO));
            e.add(dialog(VqsvText.Scene1Room0Group0.ELDER, VqsvText.Scene1Room0Group0.ELDER_EXAM));
            e.add(dialog(VqsvText.Scene1Room0Group0.NEIL, VqsvText.Scene1Room0Group0.NEIL_READY));
            e.add(dialog(VqsvText.Scene1Room0Group0.ELDER, VqsvText.Scene1Room0Group0.ELDER_BUNNY_TASK));
            e.add(s -> s.op17Item(0, 0, 1));
            e.add(s -> s.op17Item(0, 1, 2));
            e.add(s -> s.op17Item(0, 4, 5));
            e.add(s -> { s.op39RefreshPets(); return null; });
            e.add(dialog(VqsvText.Scene1Room0Group0.NEIL, VqsvText.Scene1Room0Group0.NEIL_SIMPLE));
            e.add(taskNotice(VqsvText.Scene1Room0Group0.TASK_BUNNY));
            e.add(s -> s.op10PlayerTimedAction(1, 4, 36));            e.add(s -> s.op10PlayerTimedAction(0, 4, 12));
            e.add(s -> s.op10PlayerTimedAction(1, 4, 8));
            e.add(s -> {
                s.prepareTransition(55, 279, 240, 320);
                s.op25SetGameFlag(1);
                s.markWorldTransition(1, 1, 37);
                s.loadScene1Room1(s.transitionCenterX, s.transitionCenterY);
                s.placePlayerAtTransitionActorApprox(37, 16);
                return new Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32);
            });
            // scene_1 room1 group0, records 1..10 after op13 trigger. Battle/capture remains a source-backed stub.
            e.add(s -> s.room1BunnyBattleCaptureRuntime());
            e.add(dialog("Neil", "Ch\u00ednh l\u00e0 con th\u1ecf c\u1ee7a ng\u01b0\u01a1i, mau gi\u00fap ta b\u00e1o c\u00e1o k\u1ebft qu\u1ea3 \u0111\u1ec3 v\u01b0\u1ee3t qua"));
            e.add(s -> { s.op56ActorVisibility(1, new int[]{50}, new int[]{0}); return null; });
            e.add(s -> { s.op23MarkEventComplete(1, 0, 1); return null; });
            e.add(taskNotice("Tr\u1edf v\u1ec1 t\u00ecm tr\u01b0\u1edfng th\u00f4n!"));
            e.add(s -> {
                s.op14CompleteEvent(1, 1, 0);
                s.sourceStateTrace.add("PORTED op86 gate preview [1,1,0]="
                        + s.sourceEventState(1, 1, 0)
                        + " complete=" + s.sourceEventStateComplete(1, 1, 0));
                return new ActorTransitionFreeWorldTrigger(1, 1, 37, 3, 1, 0, 30);
            });
            // scene_1 room0 group2, records 0..15. Starts only after op16 actor 52 interaction.
            e.add(s -> new ActorInteractionFreeWorldTrigger(1, 0, 2, 1, 1, 0, 52));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.CAUGHT));
            e.add(dialog(VqsvText.Scene1Room0Group2.ELDER, VqsvText.Scene1Room0Group2.ELDER_BUNNY_CUTE, 1));
            e.add(s -> { s.op5ActorEffect(0, 0, 9, 0, 0); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.NEIL_WRONG_TARGET));
            e.add(dialog(VqsvText.Scene1Room0Group2.ELDER, VqsvText.Scene1Room0Group2.ELDER_PET_OFFER, 1));
            e.add(s -> { s.op5ActorEffect(0, 0, 14, 0, 0); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.NEIL_GO_SEE));
            e.add(dialog(VqsvText.Scene1Room0Group2.ELDER, VqsvText.Scene1Room0Group2.ELDER_ONLY_ONE, 1));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.NEIL_NOT_FREE));
            e.add(s -> { s.op5ActorEffect(1, 52, 3, 0, 0); return null; });
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group2 op45 taskFlag=1");
                s.text = TextBox.taskTip(VqsvText.Scene1Room0Group2.TASK_PET_CHOICE);
                return waitForText();
            });
            e.add(s -> { s.op14CompleteEvent(1, 0, 2); return null; });
            e.add(s -> new Room0Group3PetOffer());
            // scene_1 room0 group6, records 0..21. Battle remains a controlled game.d stub.
            e.add(s -> new Room0Group6Start());
            e.add(dialog("Tr\u01b0\u1edfng th\u00f4n", "Ti\u1ec3u t\u1eed th\u00fai, ti\u1ebfp chi\u00eau!", 1));
            e.add(s -> { s.op67SetBattleActor(52); return null; });
            e.add(s -> s.room0Group6ElderBattleRuntime());
            e.add(dialog("Tr\u01b0\u1edfng th\u00f4n", "R\u1ea5t t\u1ed1t, nh\u01b0 v\u1eady, ch\u00fang ta c\u0169ng y\u00ean t\u00e2m. Neil, nh\u1eefng v\u1eadt n\u00e0y ng\u01b0\u01a1i mang theo, nh\u1eefng l\u00fac nguy k\u1ecbch s\u1ebd c\u1ea7n d\u00f9ng \u0111\u1ebfn.", 1));
            e.add(s -> s.op31CurrencyReward(0, 0, 500));
            e.add(s -> s.op17Item(0, 4, 10));
            e.add(s -> s.op17Item(0, 11, 2));
            e.add(s -> s.op19SpecialReward(5, 1));
            e.add(dialog("Tr\u01b0\u1edfng th\u00f4n", "M\u1ed7i khi ng\u01b0\u01a1i nh\u00ecn th\u1ea5y ho\u1eb7c \u0111\u1ea1t \u0111\u01b0\u1ee3c m\u1ed9t s\u1ee7ng v\u1eadt m\u1edbi, s\u00e1ch tranh l\u00fd s\u1ebd gia t\u0103ng ch\u1ee7ng lo\u1ea1i s\u1ee7ng v\u1eadt, do \u0111\u00f3 c\u00e0ng thu th\u1eadp nhi\u1ec1u c\u00e0ng t\u1ed1t.", 1));
            e.add(dialog("Tr\u01b0\u1edfng th\u00f4n", "Sau khi \u0111\u1ebfn B\u00edch Th\u1ee7y Th\u00e0nh, nh\u1edb t\u00ecm Abra, \u00f4ng \u1ea5y s\u1ebd gi\u00fap ng\u01b0\u01a1i tr\u1edf th\u00e0nh tay hu\u1ea5n luy\u1ec7n s\u1ee7ng v\u1eadt m\u1ea1nh m\u1ebd h\u01a1n.", 1));
            e.add(dialog("Neil", "\u1eecm, ta nh\u1edb r\u1ed3i!"));
            e.add(s -> { s.op23MarkEventComplete(1, 0, 4); return null; });
            e.add(s -> { s.op23MarkEventComplete(1, 0, 5); return null; });
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op45 taskFlag=2");
                s.text = TextBox.taskTip("\u0110\u1ebfn B\u00edch Th\u1ee7y Th\u00e0nh.");
                return waitForText();
            });
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op40 free-world notice");
                s.text = TextBox.openBox("Gi\u1edd c\u00f3 th\u1ec3 t\u1ef1 do di chuy\u1ec3n.");
                return waitForText();
            });
            e.add(s -> { s.op14CompleteEvent(1, 0, 6); return null; });
            e.add(s -> new Room0PostGroup6FreeWorld());
            return e;
        }

        private int transitionCenterX;
        private int transitionCenterY;
        private int transitionWidth = W;
        private int transitionHeight = H;
        private int transitionDirection = 0;
        private int nextWorldF = -1;
        private int nextWorldG = -1;
        private int nextWorldActor = -1;

        private void prepareTransition(int centerX, int centerY, int width, int height) {
            prepareTransition(centerX, centerY, width, height, transitionDirection);
        }

        private void prepareTransition(int centerX, int centerY, int width, int height, int direction) {
            transitionCenterX = centerX;
            transitionCenterY = centerY;
            transitionWidth = width;
            transitionHeight = height;
            transitionDirection = direction;
        }

        private void markWorldTransition(int worldF, int worldG, int actorIndex) {
            nextWorldF = worldF;
            nextWorldG = worldG;
            nextWorldActor = actorIndex;
        }

        private static int sourceTransitionRequiredDirection(int c) {
            int[] map = {2, 3, 0, 1};
            return c >= 0 && c < map.length ? map[c] : -1;
        }

        private boolean trySourceTransition(int actorId, int sourceC,
                                            int targetSceneId, int targetRoomIndex, int targetActorId) {
            int requiredDirection = sourceTransitionRequiredDirection(sourceC);
            if (player.direction != requiredDirection || !playerIntersectsActorSourceMask(actorId, true)) {
                return false;
            }
            stopPlayerForSourceEvent();
            sourceStateTrace.add("PORTED/APPROX type1 transition trigger actor=" + actorId
                    + " sourceC=" + sourceC
                    + " requiredDir=" + requiredDirection
                    + " from=[" + currentSceneId + "," + currentRoomIndex + "]"
                    + " target=[" + targetSceneId + "," + targetRoomIndex + "," + targetActorId + "]");
            markWorldTransition(targetSceneId, targetRoomIndex, targetActorId);
            if (!loadImplementedTransitionTarget(targetSceneId, targetRoomIndex, targetActorId)) {
                sourceStateTrace.add("PENDING type1 transition target not implemented ["
                        + targetSceneId + "," + targetRoomIndex + "," + targetActorId + "]");
            }
            return true;
        }

        private boolean loadImplementedTransitionTarget(int targetSceneId, int targetRoomIndex, int targetActorId) {
            if (targetSceneId == 1 && targetRoomIndex == 0) {
                loadScene1Room0(player.x, player.y);
                placePlayerAtTransitionActorApprox(targetActorId, 16);
                sourceStateTrace.add("PORTED/APPROX loaded scene=1 room=0 targetActor="
                        + targetActorId + " player=[" + player.x + "," + player.y + "]");
                return true;
            }
            if (targetSceneId == 1 && targetRoomIndex == 1) {
                loadScene1Room1(player.x, player.y);
                placePlayerAtTransitionActorApprox(targetActorId, 16);
                sourceStateTrace.add("PORTED/APPROX loaded scene=1 room=1 targetActor="
                        + targetActorId + " player=[" + player.x + "," + player.y + "]");
                return true;
            }
            if (targetSceneId == 1 && targetRoomIndex == 2) {
                loadScene1Room2(player.x, player.y);
                placePlayerAtTransitionActorApprox(targetActorId, 16);
                sourceStateTrace.add("PORTED/APPROX loaded scene=1 room=2 targetActor="
                        + targetActorId + " player=[" + player.x + "," + player.y + "]");
                return true;
            }
            return false;
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
            currentSceneId = 1;
            currentRoomIndex = 3;
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
            currentSceneId = 1;
            currentRoomIndex = 0;
            useMap = true;
            mapRenderer = loadMapRenderer(2);
            followActorId = -1;
            tempSprites.clear();
            worldUi.visible = true;
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

        private void loadScene1Room1(int cameraCenterX, int cameraCenterY) {
            currentSceneId = 1;
            currentRoomIndex = 1;
            useMap = true;
            mapRenderer = loadMapRenderer(5);
            followActorId = -1;
            tempSprites.clear();
            worldUi.visible = true;
            int[][] rows = {
                    {0, 205, 0, 7, 26, 1, 0, 1},
                    {1, 200, 1, 56, 52, 1, 0, 1},
                    {2, 200, 1, 103, 30, 1, 0, 1},
                    {3, 200, 1, 152, 30, 1, 0, 1},
                    {4, 200, 1, 200, 30, 1, 0, 1},
                    {5, 204, 0, 233, 40, 1, 0, 1},
                    {6, 200, 0, 244, 66, 1, 0, 1},
                    {7, 200, 0, 257, 97, 1, 0, 1},
                    {8, 200, 0, 243, 128, 1, 0, 1},
                    {9, 200, 1, 297, 129, 1, 0, 1},
                    {10, 200, 1, 342, 95, 1, 0, 1},
                    {11, 204, 1, 348, 116, 1, 0, 1},
                    {12, 200, 1, 326, 146, 1, 0, 1},
                    {13, 205, 1, 279, 138, 1, 0, 1},
                    {14, 200, 1, 260, 156, 1, 0, 1},
                    {15, 225, 1, 440, 16, 1, 0, 2},
                    {16, 225, 1, 280, 34, 1, 0, 2},
                    {17, 225, 1, 360, 143, 1, 0, 2},
                    {18, 225, 1, 7, 176, 1, 0, 2},
                    {19, 225, 1, 168, 254, 1, 0, 2},
                    {20, 225, 0, 27, 65, 1, 0, 2},
                    {21, 225, 0, 232, 272, 1, 0, 2},
                    {22, 200, 0, 356, 192, 1, 0, 1},
                    {23, 200, 0, 368, 225, 1, 0, 1},
                    {24, 328, 0, 361, 234, 1, 0, 1},
                    {25, 243, 0, 312, 32, 1, 0, 2},
                    {26, 200, 1, 451, 130, 1, 0, 1},
                    {27, 200, 0, 438, 163, 1, 0, 1},
                    {28, 200, 0, 452, 257, 1, 0, 1},
                    {29, 200, 1, 454, 289, 1, 0, 1},
                    {30, 200, 0, 405, 355, 1, 0, 1},
                    {31, 200, 1, 311, 354, 1, 0, 1},
                    {32, 200, 0, 54, 128, 1, 0, 1},
                    {33, 200, 1, 55, 167, 1, 0, 1},
                    {34, 200, 1, 55, 205, 1, 0, 1},
                    {35, 200, 1, 22, 224, 1, 0, 1},
                    {36, 202, 2, 56, 223, 1, 0, 1},
                    {37, 223, 2, 19, 273, 1, 1, 1},
                    {38, 200, 1, 23, 322, 1, 0, 1},
                    {39, 200, 0, 100, 338, 1, 0, 1},
                    {40, 200, 1, 152, 116, 1, 0, 1},
                    {41, 200, 1, 151, 163, 1, 0, 1},
                    {42, 204, 1, 157, 199, 1, 0, 1},
                    {43, 200, 1, 148, 226, 1, 0, 1},
                    {44, 243, 0, 208, 240, 1, 0, 3},
                    {45, 204, 0, 249, 253, 1, 0, 1},
                    {46, 200, 0, 243, 322, 1, 0, 1},
                    {47, 205, 0, 150, 315, 1, 0, 1},
                    {48, 140, 0, 118, 52, 1, 1, 1},
                    {49, 23, 0, 97, 60, 1, 1, 1},
                    {50, 120, 0, 409, 176, 1, 1, 1}
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

        private void loadScene1Room2(int cameraCenterX, int cameraCenterY) {
            currentSceneId = 1;
            currentRoomIndex = 2;
            useMap = true;
            mapRenderer = loadMapRenderer(6);
            followActorId = -1;
            tempSprites.clear();
            worldUi.visible = true;
            int[][] rows = {
                    {0, 200, 1, 70, 49, 1, 0, 1},
                    {1, 200, 1, 167, 51, 1, 0, 1},
                    {2, 223, 0, 120, 23, 1, 1, 0},
                    {3, 223, 1, 121, 320, 1, 1, 2},
                    {4, 243, 0, 216, 33, 1, 0, 1},
                    {5, 225, 0, 97, 61, 1, 0, 2},
                    {6, 225, 0, 216, 80, 1, 0, 2},
                    {7, 225, 0, 24, 239, 1, 0, 2},
                    {8, 225, 0, 53, 319, 1, 0, 2},
                    {9, 225, 1, 87, 143, 1, 0, 2},
                    {10, 225, 1, 10, 256, 1, 0, 2},
                    {11, 225, 1, 5, 130, 1, 0, 2},
                    {12, 200, 1, 71, 95, 1, 0, 1},
                    {13, 200, 1, 168, 97, 0, 0, 1},
                    {14, 328, 0, 73, 137, 1, 0, 1},
                    {15, 204, 1, 12, 56, 1, 0, 1},
                    {16, 205, 1, 23, 72, 1, 0, 1},
                    {17, 200, 1, 168, 175, 1, 0, 1},
                    {18, 200, 1, 174, 216, 1, 0, 1},
                    {19, 204, 0, 172, 262, 1, 0, 1},
                    {20, 205, 0, 167, 279, 1, 0, 1},
                    {21, 200, 1, 167, 322, 1, 0, 1},
                    {22, 204, 1, 236, 312, 1, 0, 1},
                    {23, 200, 0, 72, 226, 1, 0, 1},
                    {24, 200, 1, 68, 272, 1, 0, 1},
                    {25, 243, 0, 56, 288, 1, 0, 1},
                    {26, 140, 0, 29, 119, 1, 1, 1},
                    {27, 54, 0, 22, 144, 1, 1, 1},
                    {28, 69, 0, 214, 229, 1, 1, 1},
                    {29, 86, 0, 199, 194, 1, 1, 1}
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
            if (actorId == -1 || actorId >= 0 && actorId < actors.length && actors[actorId] != null) {
                tempSprites.add(new TempSprite(actorId, animation, 120));
            }
        }

        private void op5ActorEffect(int mode, int actorId, int animation, int x, int y) {
            if (mode == 0) {
                spawnActorEffect(-1, animation);
            } else if (mode == 1 && (x != 0 || y != 0)) {
                tempSprites.add(new TempSprite(x, y, animation, 120));
            } else {
                spawnActorEffect(actorId, animation);
            }
            sourceStateTrace.add("PORTED/APPROX op5 effect mode=" + mode
                    + " actor=" + actorId
                    + " anim=" + animation
                    + " xy=[" + x + "," + y + "]");
        }

        private void renderPlayer(Graphics2D g) {
            if (useMap && player.visible) {
                player.render(g, cameraX, cameraY);
            }
        }


        private void setPlayerPositionApprox(int x, int y) {
            playerX = x;
            playerY = y;
            player.x = x;
            player.y = y;
            player.direction = transitionDirection;
            player.applyMode(0);
            player.visible = true;
            setCameraCenter(x, y);
        }

        private void placePlayerAtTransitionActorApprox(int actorId, int tileSize) {
            if (actorId < 0 || actorId >= actors.length || actors[actorId] == null) {
                return;
            }
            Actor actor = actors[actorId];
            playerX = actor.x - Math.floorMod(actor.x, tileSize);
            playerY = actor.y - Math.floorMod(actor.y, tileSize);
            player.x = playerX;
            player.y = playerY;
            player.direction = actor.direction;
            player.applyMode(0);
            player.visible = true;
            setCameraCenter(playerX, playerY);
        }

        private void tickFreeWorldPlayer() {
            int dir = heldDirection();
            if (dir < 0) {
                player.applyMode(0);
                setCameraCenter(player.x, player.y);
                return;
            }
            player.direction = dir;
            player.applyMode(3);
            if (canMovePlayer(dir, 4)) {
                player.step(4);
                playerX = player.x;
                playerY = player.y;
            }
            setCameraCenter(player.x, player.y);
        }

        private int heldDirection() {
            if (keyUp && !keyDown) {
                return 2;
            }
            if (keyDown && !keyUp) {
                return 0;
            }
            if (keyRight && !keyLeft) {
                return 1;
            }
            if (keyLeft && !keyRight) {
                return 3;
            }
            return -1;
        }

        private boolean canMovePlayer(int dir, int speed) {
            int nx = player.x;
            int ny = player.y;
            int amount = Math.max(1, Math.abs(speed));
            switch (dir) {
                case 0:
                    ny += amount;
                    break;
                case 1:
                    nx += amount;
                    break;
                case 2:
                    ny -= amount;
                    break;
                case 3:
                    nx -= amount;
                    break;
                default:
                    break;
            }
            if (mapRenderer == null) {
                return true;
            }
            return nx - 8 >= 0 && nx + 8 <= mapRenderer.mapWidthPixels()
                    && ny - 8 >= 0 && ny + 8 <= mapRenderer.mapHeightPixels();
        }

        private boolean playerIntersectsSourceRect(int x, int y, int w, int h) {
            // Source op13 calls ae.a(rectX, rectY, rectW, rectH, player.i, player.j, player.a.k()).
            return x + w >= player.x - 8
                    && x <= player.x - 8 + 16
                    && y <= player.y - 8 + 16
                    && y + h >= player.y - 8;
        }

        private boolean playerIntersectsActorSourceMask(int actorId, boolean actorHitMask) {
            if (actorId < 0 || actorId >= actors.length || actors[actorId] == null) {
                return false;
            }
            Actor actor = actors[actorId];
            if (!actor.visible) {
                return false;
            }
            short[] playerMask = player.collisionMask();
            short[] actorMask = actorHitMask ? actor.hitMask() : actor.collisionMask();
            if (playerMask != null && actorMask != null) {
                return sourceMaskOverlap(player.x, player.y, playerMask, actor.x, actor.y, actorMask);
            }
            return actor.x + 12 >= player.x - 8
                    && actor.x - 12 <= player.x + 8
                    && actor.y + 16 >= player.y - 8
                    && actor.y - 16 <= player.y + 8;
        }

        private boolean playerInteractsActorSourceMask(int actorId) {
            if (actorId < 0 || actorId >= actors.length || actors[actorId] == null) {
                return false;
            }
            Actor actor = actors[actorId];
            if (!actor.visible) {
                return false;
            }
            int px = player.x;
            int py = player.y;
            int offset = 4;
            switch (player.direction) {
                case 0:
                    py += offset;
                    break;
                case 1:
                    px += offset;
                    break;
                case 2:
                    py -= offset;
                    break;
                case 3:
                    px -= offset;
                    break;
                default:
                    break;
            }
            short[] playerMask = player.collisionMask();
            short[] actorMask = actor.collisionMask();
            if (playerMask != null && actorMask != null) {
                return sourceMaskOverlap(px, py, playerMask, actor.x, actor.y, actorMask);
            }
            return actor.x + 12 >= px - 8
                    && actor.x - 12 <= px + 8
                    && actor.y + 16 >= py - 8
                    && actor.y - 16 <= py + 8;
        }

        private static boolean sourceMaskOverlap(int ax, int ay, short[] aMask, int bx, int by, short[] bMask) {
            if (aMask.length < 4 || bMask.length < 4) {
                return false;
            }
            return ax + aMask[0] + aMask[2] >= bx + bMask[0]
                    && ax + aMask[0] <= bx + bMask[0] + bMask[2]
                    && ay + aMask[1] <= by + bMask[1] + bMask[3]
                    && ay + aMask[1] + aMask[3] >= by + bMask[1];
        }

        private void stopPlayerForSourceEvent() {
            player.applyMode(0);
            setCameraCenter(player.x, player.y);
        }

        private void sourceStateApprox(String ignoredSourceNote) {
            sourceStateTrace.add("APPROX " + ignoredSourceNote);
        }

        private static String sourceEventStateKey(int sceneId, int roomIndex, int groupIndex) {
            return sceneId + ":" + roomIndex + ":" + groupIndex;
        }

        private void setSourceEventState(int sceneId, int roomIndex, int groupIndex, int state, String reason) {
            sourceEventStates.put(sourceEventStateKey(sceneId, roomIndex, groupIndex), (byte) state);
            sourceStateTrace.add("PORTED sourceEventState [" + sceneId + "," + roomIndex + "," + groupIndex
                    + "]=" + state + " via " + reason);
        }

        private int sourceEventState(int sceneId, int roomIndex, int groupIndex) {
            return sourceEventStates.getOrDefault(sourceEventStateKey(sceneId, roomIndex, groupIndex), (byte) 0);
        }

        private boolean sourceEventStateComplete(int sceneId, int roomIndex, int groupIndex) {
            return sourceEventState(sceneId, roomIndex, groupIndex) == 3;
        }

        private Blocking op17Item(int mode, int itemId, int qty) {
            SourceItem item = sourceItem(itemId);
            if (mode == 0) {
                if (sourceCanAddItem(itemId, qty)) {
                    sourceAddItem(itemId, qty);
                    sourceStateTrace.add("PORTED/APPROX op17 add [" + mode + "," + itemId + "," + qty
                            + "] bagChannel=" + item.bagChannel + " count=" + sourceItemCount(itemId));
                    text = sourceInventoryPopup("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + item.name, qty);
                } else {
                    sourceStateTrace.add("PORTED/APPROX op17 add-full [" + mode + "," + itemId + "," + qty + "]");
                    text = sourceInventoryPopup("Ba l\u00f4 \u0111\u00e3 \u0111\u1ee7 \u0111\u1ea1o c\u1ee5 n\u00e0y", 0);
                }
            } else if (sourceCanRemoveItem(itemId, qty)) {
                sourceRemoveItem(itemId, qty);
                sourceStateTrace.add("PORTED/APPROX op17 remove [" + mode + "," + itemId + "," + qty
                        + "] bagChannel=" + item.bagChannel + " count=" + sourceItemCount(itemId));
                text = sourceInventoryPopup("M\u1ea5t: " + item.name, qty);
            } else {
                sourceStateTrace.add("PORTED/APPROX op17 remove-missing [" + mode + "," + itemId + "," + qty + "]");
            }
            return text == null ? null : waitForText();
        }

        private void op39RefreshPets() {
            for (SourcePetState pet : sourcePets) {
                pet.refreshFromSourceDb();
                sourcePetRefreshOps++;
            }
            sourceStateTrace.add("PORTED/APPROX op39 refreshPets count=" + sourcePets.size()
                    + " refreshOps=" + sourcePetRefreshOps);
        }

        private void op25SetGameFlag(int arg0) {
            sourceGameCF = arg0 == 0;
            sourceStateTrace.add("PORTED op25 game.c.f=" + sourceGameCF + " arg0=" + arg0);
        }

        private Blocking op9SourceEffect(String context, int... args) {
            int effectId = args.length > 0 ? args[0] : -1;
            switch (effectId) {
                case 1:
                case 2: {
                    int color = sourceEffectColor(args);
                    effect.startFade(effectId, color);
                    sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                            + " -> b.a().c(color,id) fade id=" + effectId
                            + " color=0x" + String.format("%06X", color & 0xFFFFFF));
                    return effect::doneOverlay;
                }
                case 10:
                    effect.startFlash(argOrZero(args, 1), argOrZero(args, 2));
                    sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                            + " -> b.a().d flash/toggle path");
                    return effect::doneOverlay;
                case 12:
                case 13:
                    effect.startBars(effectId, Math.max(1, argOrZero(args, 1)),
                            Math.max(1, argOrZero(args, 2)), Math.max(1, argOrZero(args, 3)),
                            Math.max(0, argOrZero(args, 4)), Math.max(0, argOrZero(args, 5)));
                    sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                            + " -> b.a().a bar transition path");
                    return effect::doneBars;
                case 16:
                    if (argOrZero(args, 1) == 0) {
                        effect.startParticles(Math.max(1, argOrZero(args, 2)) * 10);
                    } else if (argOrZero(args, 1) == 1 || argOrZero(args, 1) == 2) {
                        effect.startFireParticles(Math.max(1, argOrZero(args, 2)) * 10);
                    } else {
                        effect.stopParticles();
                    }
                    sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                            + " -> source particle texture family");
                    return null;
                case 14:
                case 15:
                case 17:
                    sourceStateTrace.add("PENDING " + context + " op9 " + Arrays.toString(args)
                            + " source id handled by b.a() actor/texture path; not used as full renderer yet");
                    return null;
                default:
                    sourceStateTrace.add("UNKNOWN " + context + " op9 " + Arrays.toString(args)
                            + " unsupported source effect id");
                    return null;
            }
        }

        private static int argOrZero(int[] args, int index) {
            return index >= 0 && index < args.length ? args[index] : 0;
        }

        private static int sourceEffectColor(int[] args) {
            int r = argOrZero(args, 2) & 0xFF;
            int g = argOrZero(args, 3) & 0xFF;
            int b = argOrZero(args, 4) & 0xFF;
            return (r << 16) | (g << 8) | b;
        }

        private Blocking room1BunnyBattleCaptureRuntime() {
            sourceStateTrace.add("PORTED/APPROX room1 group0 op37 battleSetup=[[34,5,1]]");
            sourceStateTrace.add("PORTED/APPROX room1 group0 op52 this.i=true game.c.j=false args=[0,1]");
            sourceStateTrace.add("PORTED/APPROX room1 group0 op66 an.U=0");
            sourceStateTrace.add("PORTED/APPROX room1 group0 op32 battleEntry mode=[0,0]");
            sourceStateTrace.add("PORTED/APPROX room1 group0 op47 branch=[12,0,0] result=-1 continue success path; full game.d command UI pending");
            return new SourceBattleRuntime(
                    50,
                    new int[]{34, 5, 1},
                    new int[]{0, 1},
                    new int[]{0, 0},
                    new int[]{12, 0, 0},
                    -1);
        }

        private void op67SetBattleActor(int actorId) {
            worldEventActor = actorId;
            battleEventActor = actorId;
            sourceStateTrace.add("PORTED room0 group6 op67 game.k.v=" + actorId);
        }

        private Blocking room0Group6ElderBattleRuntime() {
            sourceStateTrace.add("PORTED/APPROX room0 group6 op37 battleSetup species=68 level=5 nature=1 from game.d.a(int[][])");
            sourceStateTrace.add("PORTED/APPROX room0 group6 op32 battleEntry mode=[0,2] captures world screen then state=12 in source");
            sourceStateTrace.add("PORTED/APPROX room0 group6 op47 branch=[10,10,0] result=0 continue reward path; full game.d command UI pending");
            return new SourceBattleRuntime(
                    52,
                    new int[]{68, 5, 1},
                    new int[0],
                    new int[]{0, 2},
                    new int[]{10, 10, 0},
                    0,
                    true);
        }

        private Blocking op31CurrencyReward(int mode, int currencyKind, int amount) {
            if (mode == 0 && currencyKind == 0) {
                sourceMoney += amount;
                sourceStateTrace.add("PORTED/APPROX room0 group6 op31 add money=" + amount
                        + " total=" + sourceMoney);
                text = TextBox.openBox("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + amount + " kim ti\u1ec1n");
            } else if (mode == 0 && currencyKind == 1) {
                sourceBadges += amount;
                sourceStateTrace.add("PORTED/APPROX room0 group6 op31 add badge=" + amount
                        + " total=" + sourceBadges);
                text = TextBox.openBox("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + amount + " Huy hi\u1ec7u");
            } else if (mode == 1 && currencyKind == 0) {
                sourceMoney -= amount;
                sourceStateTrace.add("PORTED/APPROX room0 group6 op31 remove money=" + amount
                        + " total=" + sourceMoney);
                text = TextBox.openBox("M\u1ea5t: " + amount + " kim ti\u1ec1n");
            } else if (mode == 1 && currencyKind == 1) {
                sourceBadges -= amount;
                sourceStateTrace.add("PORTED/APPROX room0 group6 op31 remove badge=" + amount
                        + " total=" + sourceBadges);
                text = TextBox.openBox("M\u1ea5t: " + amount + " huy hi\u1ec7u");
            } else {
                sourceStateTrace.add("UNKNOWN room0 group6 op31 args=["
                        + mode + "," + currencyKind + "," + amount + "]");
                text = null;
            }
            return text == null ? null : waitForText();
        }

        private Blocking op19SpecialReward(int rewardId, int qty) {
            SourceSpecialReward reward = sourceSpecialRewards.computeIfAbsent(rewardId, SourceSpecialReward::fromSourceDb);
            reward.applySourceGameGSemantics(qty);
            sourceStateTrace.add("PORTED/APPROX room0 group6 op19 rewardId=" + rewardId
                    + " qty=" + qty
                    + " sourceRow=[" + reward.textId + "," + reward.iconId + "," + reward.descriptionTextId + "]"
                    + " game.g path=" + reward.gameGPath
                    + " unlocked=" + reward.unlocked
                    + " stack=" + reward.stackCount);
            text = sourceInventoryPopup("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + reward.name, qty);
            return waitForText();
        }

        private void op56ActorVisibility(int mode, int[] ids, int[] states) {
            for (int i = 0; i < ids.length; i++) {
                int id = ids[i];
                if (id < 0 || id >= actors.length || actors[id] == null) {
                    continue;
                }
                Actor actor = actors[id];
                if (mode == 0) {
                    actor.visible = true;
                    actor.direction = states[Math.min(i, states.length - 1)];
                    actor.applyMode(0);
                } else if (mode == 1) {
                    actor.visible = false;
                    actor.applyMode(0);
                }
            }
            sourceStateTrace.add("PORTED/APPROX op56 mode=" + mode + " ids=" + Arrays.toString(ids)
                    + " states=" + Arrays.toString(states));
        }

        private void op23MarkEventComplete(int worldF, int worldG, int eventId) {
            setSourceEventState(worldF, worldG, eventId, 3, "op23");
        }

        private void op14CompleteEvent(int sceneId, int roomIndex, int groupIndex) {
            setSourceEventState(sceneId, roomIndex, groupIndex, 3, "op14");
        }

        private static TextBox sourceInventoryPopup(String message, int qty) {
            String suffix = qty > 0 ? " x " + qty : "";
            return TextBox.openBox(message + suffix);
        }

        private static Map<Integer, BagItem> initialSourceBagItems() {
            Map<Integer, BagItem> items = new HashMap<>();
            items.put(0, new BagItem(0, 0, 0, true));
            return items;
        }

        private boolean sourceCanAddItem(int itemId, int qty) {
            BagItem entry = sourceBagItems.get(itemId);
            if (entry != null) {
                return entry.count < 99;
            }
            return qty <= 99;
        }

        private boolean sourceCanRemoveItem(int itemId, int qty) {
            BagItem entry = sourceBagItems.get(itemId);
            return entry != null && entry.count - qty >= 0;
        }

        private void sourceAddItem(int itemId, int qty) {
            SourceItem item = sourceItem(itemId);
            BagItem entry = sourceBagItems.get(itemId);
            if (entry == null) {
                sourceBagItems.put(itemId, new BagItem(itemId, Math.min(qty, 99), item.bagChannel, false));
                return;
            }
            entry.count = Math.min(entry.count + qty, 99);
        }

        private void sourceRemoveItem(int itemId, int qty) {
            BagItem entry = sourceBagItems.get(itemId);
            if (entry == null) {
                return;
            }
            entry.count -= qty;
            if (entry.count <= 0 && !entry.keepAtZero) {
                sourceBagItems.remove(itemId);
            }
        }

        private int sourceItemCount(int itemId) {
            BagItem entry = sourceBagItems.get(itemId);
            return entry == null ? 0 : entry.count;
        }

        private static SourceItem sourceItem(int itemId) {
            // Source data: aq.c[4][itemId][0] -> aq.d[textId], plus aq.c[4][itemId][5] bag channel.
            switch (itemId) {
                case 0:
                    return new SourceItem(0, 261, "T\u1ea5t Trung C\u1ea7u", 0);
                case 1:
                    return new SourceItem(1, 262, "Phong \u1ea5n c\u1ea7u", 0);
                case 4:
                    return new SourceItem(4, 265, "B\u00e1nh Sandwich", 1);
                case 11:
                    return new SourceItem(11, 272, "Sinh m\u1ec7nh th\u1ea1ch", 4);
                default:
                    return new SourceItem(itemId, 0, "", 0);
            }
        }

        private Blocking op10PlayerTimedAction(int dir, int speed, int duration) {
            return new Op10PlayerTimedAction(dir, speed, duration);
        }

        private static void setActive(Scene s, int[] ids, int[] dirs) {
            for (int i = 0; i < ids.length; i++) {
                Actor a = s.actors[ids[i]];
                if (a != null) {
                    a.direction = dirs[i];
                    a.applyMode(0);
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

        private static Event taskNotice(String text) {
            return s -> {
                s.text = TextBox.taskTip(text);
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

    private static final class BagItem {
        private final int id;
        private final int bagChannel;
        private final boolean keepAtZero;
        private int count;

        private BagItem(int id, int count, int bagChannel, boolean keepAtZero) {
            this.id = id;
            this.count = count;
            this.bagChannel = bagChannel;
            this.keepAtZero = keepAtZero;
        }
    }

    private static final class SourceItem {
        private final int id;
        private final int textId;
        private final String name;
        private final int bagChannel;

        private SourceItem(int id, int textId, String name, int bagChannel) {
            this.id = id;
            this.textId = textId;
            this.name = name;
            this.bagChannel = bagChannel;
        }
    }

    private static final class SourceSpecialReward {
        private final int id;
        private final int textId;
        private final int iconId;
        private final int descriptionTextId;
        private final String name;
        private boolean unlocked;
        private int stackCount;
        private String gameGPath = "";

        private SourceSpecialReward(int id, int textId, int iconId, int descriptionTextId, String name) {
            this.id = id;
            this.textId = textId;
            this.iconId = iconId;
            this.descriptionTextId = descriptionTextId;
            this.name = name;
        }

        private static SourceSpecialReward fromSourceDb(int rewardId) {
            if (rewardId == 5) {
                return new SourceSpecialReward(5, 300, 47, 308,
                        "Trang s\u00e1ch t\u01b0\u01a1ng \u1ee9ng c\u1ee7a s\u1ee7ng v\u1eadt");
            }
            return new SourceSpecialReward(rewardId, 0, 0, 0, "Reward " + rewardId);
        }

        private void applySourceGameGSemantics(int qty) {
            if (id == 7 || id == 8 || id == 9) {
                stackCount = Math.min(99, stackCount + qty);
                gameGPath = "game.g.d -> game.g.c stack special item";
                return;
            }
            unlocked = true;
            if (id == 0) {
                gameGPath = "game.g.d -> game.g.e(id,-1) mark active special";
            } else {
                gameGPath = "game.g.d -> game.g.i(id) unlock vector entry";
            }
        }
    }

    private static final class SourceBattleUnit {
        private static final int[] NATURE_MULT = {90, 95, 100, 110, 125};
        private final int speciesId;
        private final int level;
        private final int nature;
        private final String name;
        private final int maxHp;
        private int hp;
        private final int attack;
        private final int defense;
        private final int speed;
        private final int element;
        private final int visualId;

        private SourceBattleUnit(int speciesId, int level, int nature, String name,
                                 int maxHp, int attack, int defense, int speed,
                                 int element, int visualId) {
            this.speciesId = speciesId;
            this.level = level;
            this.nature = nature;
            this.name = name;
            this.maxHp = maxHp;
            this.hp = maxHp;
            this.attack = attack;
            this.defense = defense;
            this.speed = speed;
            this.element = element;
            this.visualId = visualId;
        }

        private static SourceBattleUnit enemyFromEncounter(int[] encounter) {
            int species = encounter.length > 0 ? encounter[0] : -1;
            int level = encounter.length > 1 ? encounter[1] : 1;
            int nature = encounter.length > 2 ? encounter[2] : 3;
            return fromSpecies(species, level, nature, false);
        }

        private static SourceBattleUnit playerFromSourcePets(List<SourcePetState> pets) {
            if (pets.isEmpty()) {
                return fallback(-1, 1, 3, "Neil", 120, 22, 12, 10);
            }
            SourcePetState pet = pets.get(0);
            int level = Math.max(1, pet.level);
            return fromSpecies(pet.speciesId, level, 3, true);
        }

        private static SourceBattleUnit fromSpecies(int species, int level, int nature, boolean playerSide) {
            short[] row = SourceBattleDb.instance().speciesRow(species);
            if (row == null || row.length < 23) {
                String fallbackName = playerSide ? "Pet " + species : "Enemy " + species;
                return fallback(species, level, nature, fallbackName, 80 + level * 4, 18 + level, 8 + level / 2, 8);
            }
            int idx = Math.max(0, Math.min(NATURE_MULT.length - 1, nature - 1));
            int mult = NATURE_MULT[idx];
            int hp = ((row[5] + row[6] * level + row[7]) * mult) / 100;
            int atk = ((row[8] + row[9] * level + row[10]) * mult) / 100;
            int def = ((row[11] + row[12] * level / 10 + row[13]) * mult) / 100;
            int spd = ((row[14] + row[15] * level / 10 + row[16]) * mult) / 100;
            String sourceName = SourceBattleDb.instance().text(row[0], playerSide ? "Pet " + species : "Enemy " + species);
            return new SourceBattleUnit(species, level, nature, sourceName,
                    Math.max(1, hp), Math.max(1, atk), Math.max(0, def), Math.max(1, spd),
                    row[1], row[17]);
        }

        private static SourceBattleUnit fallback(int species, int level, int nature, String name,
                                                 int maxHp, int attack, int defense, int speed) {
            return new SourceBattleUnit(species, level, nature, name,
                    Math.max(1, maxHp), Math.max(1, attack), Math.max(0, defense), Math.max(1, speed),
                    -1, -1);
        }

        private boolean alive() {
            return hp > 0;
        }

        private int nextLevelEnergy() {
            if (level >= 50) {
                return 37300;
            }
            return Math.max(1, (level + 1) * 15 * (level + 1) - 200);
        }

        private int basicDamageTo(SourceBattleUnit target) {
            int raw = attack - target.defense;
            int levelPart = Math.max(1, level / 2);
            int damage = Math.max(1, raw + levelPart);
            byte relation = elementRelationTo(target);
            if (relation == 0) {
                damage = damage * 3 / 2;
            } else if (relation == 1) {
                damage = Math.max(1, damage * 2 / 3);
            }
            return damage;
        }

        private void damage(int amount) {
            hp = Math.max(0, hp - Math.max(1, amount));
        }

        private byte elementRelationTo(SourceBattleUnit target) {
            boolean attackerEffective = true;
            boolean defenderEffective = true;
            if (visualId == 2 && target.visualId == 2) {
                attackerEffective = true;
                defenderEffective = true;
            } else if (visualId == 2 && target.visualId != 2) {
                attackerEffective = true;
                defenderEffective = false;
            } else if (visualId != 2 && target.visualId == 2) {
                attackerEffective = false;
                defenderEffective = true;
            }
            if (attackerEffective && beats(element, target.element)) {
                return 0;
            }
            if (defenderEffective && beats(target.element, element)) {
                return 1;
            }
            return -1;
        }

        private static boolean beats(int a, int b) {
            return (a == 0 && b == 1)
                    || (a == 1 && b == 2)
                    || (a == 2 && b == 3)
                    || (a == 3 && b == 0)
                    || (a == 5 && b == 6)
                    || (a == 6 && b == 4)
                    || (a == 4 && b == 5);
        }

        @Override
        public String toString() {
            return name + "(species=" + speciesId
                    + ",lv=" + level
                    + ",nature=" + nature
                    + ",hp=" + maxHp
                    + ",atk=" + attack
                    + ",def=" + defense
                    + ",spd=" + speed
                    + ",element=" + element
                    + ",visual=" + visualId + ")";
        }
    }

    private static final class SourceBattleDb {
        private static SourceBattleDb cached;
        private final short[][] speciesRows;
        private final String[] texts;

        private SourceBattleDb(short[][] speciesRows, String[] texts) {
            this.speciesRows = speciesRows;
            this.texts = texts;
        }

        private static SourceBattleDb instance() {
            if (cached == null) {
                cached = load();
            }
            return cached;
        }

        private static SourceBattleDb load() {
            try {
                AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
                ResourceLocator locator = new ResourceLocator(paths);
                com.vqsv.rebuild.resource.BinaryReader reader = locator.binary(paths.scriptOriginal("db.mid"));
                short[][][] groups = new short[9][][];
                for (int i = 0; i < groups.length; i++) {
                    groups[i] = BinaryTables.readShortRows(reader);
                }
                return new SourceBattleDb(groups[0], readTextRows(paths));
            } catch (RuntimeException ex) {
                return new SourceBattleDb(new short[0][], new String[0]);
            }
        }

        private static String[] readTextRows(AssetPaths paths) {
            try {
                java.nio.file.Path path = paths.modulesRoot()
                        .resolve("script").resolve("decoded").resolve("data__script__chs.mid.json");
                String json = Files.readString(path, StandardCharsets.UTF_8);
                List<String> rows = new ArrayList<>();
                Matcher matcher = Pattern.compile("\\[\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\]").matcher(json);
                while (matcher.find()) {
                    rows.add(TextBox.decodeMojibake(unescapeJsonString(matcher.group(1))));
                }
                return rows.toArray(new String[0]);
            } catch (IOException ex) {
                return new String[0];
            }
        }

        private static String unescapeJsonString(String raw) {
            StringBuilder out = new StringBuilder(raw.length());
            for (int i = 0; i < raw.length(); i++) {
                char ch = raw.charAt(i);
                if (ch != '\\' || i + 1 >= raw.length()) {
                    out.append(ch);
                    continue;
                }
                char next = raw.charAt(++i);
                switch (next) {
                    case '"':
                    case '\\':
                    case '/':
                        out.append(next);
                        break;
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'u':
                        if (i + 4 < raw.length()) {
                            out.append((char) Integer.parseInt(raw.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                        break;
                    default:
                        out.append(next);
                        break;
                }
            }
            return out.toString();
        }

        private short[] speciesRow(int species) {
            if (species < 0 || species >= speciesRows.length) {
                return null;
            }
            return speciesRows[species];
        }

        private String text(int id, String fallback) {
            if (id < 0 || id >= texts.length || texts[id] == null || texts[id].isEmpty()) {
                return fallback;
            }
            return texts[id];
        }
    }

    private static final class SourcePetState {
        private int speciesId;
        private int level;
        private int slot;
        private int arg3;
        private int arg4;
        private final int[] skillIds = new int[]{-1, -1, -1, -1};
        private final int[] skillCooldowns = new int[skillIds.length];
        private int refreshCount;

        private SourcePetState() {
        }

        private SourcePetState(int slot, int speciesId, int level, int arg3, int arg4, int skillA, int skillB) {
            this.slot = slot;
            this.speciesId = speciesId;
            this.level = level;
            this.arg3 = arg3;
            this.arg4 = arg4;
            this.skillIds[0] = skillA;
            this.skillIds[1] = skillB;
            refreshFromSourceDb();
        }

        private void refreshFromSourceDb() {
            for (int i = 0; i < skillIds.length; i++) {
                if (skillIds[i] != -1) {
                    skillCooldowns[i] = sourceSkillCooldown(skillIds[i]);
                }
            }
            refreshCount++;
        }

        private static int sourceSkillCooldown(int skillId) {
            switch (skillId) {
                default:
                    return 0;
            }
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

    private static final class Op10PlayerTimedAction implements Blocking {
        private final int dir;
        private final int speed;
        private int remaining;
        private boolean started;

        private Op10PlayerTimedAction(int dir, int speed, int duration) {
            this.dir = dir;
            this.speed = speed;
            this.remaining = duration;
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                s.player.direction = dir;
                s.player.applyMode(3);
                s.player.visible = true;
                s.playerX = s.player.x;
                s.playerY = s.player.y;
                s.setCameraCenter(s.player.x, s.player.y);
                return false;
            }
            if (remaining <= 0) {
                stop(s);
                return true;
            }
            s.player.direction = dir;
            s.player.step(speed);
            s.playerX = s.player.x;
            s.playerY = s.player.y;
            s.setCameraCenter(s.player.x, s.player.y);
            remaining--;
            if (remaining > 0) {
                return false;
            }
            stop(s);
            return true;
        }

        private void stop(Scene s) {
            s.player.direction = dir;
            s.player.applyMode(0);
            s.playerX = s.player.x;
            s.playerY = s.player.y;
            s.setCameraCenter(s.player.x, s.player.y);
        }
    }

    private static final class Op13FreeWorldTrigger implements Blocking {
        private final int sceneId;
        private final int roomIndex;
        private final int groupIndex;
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private boolean started;

        private Op13FreeWorldTrigger(int sceneId, int roomIndex, int groupIndex, int x, int y, int w, int h) {
            this.sceneId = sceneId;
            this.roomIndex = roomIndex;
            this.groupIndex = groupIndex;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                s.sourceStateTrace.add("PORTED/APPROX op13 wait scene=" + sceneId
                        + " room=" + roomIndex + " group=" + groupIndex
                        + " rect=[" + x + "," + y + "," + w + "," + h + "]");
            }
            if (s.playerIntersectsSourceRect(x, y, w, h)) {
                s.stopPlayerForSourceEvent();
                s.sourceStateTrace.add("PORTED/APPROX op13 trigger scene=" + sceneId
                        + " room=" + roomIndex + " group=" + groupIndex
                        + " player=[" + s.player.x + "," + s.player.y + "]");
                return true;
            }
            s.tickFreeWorldPlayer();
            if (s.playerIntersectsSourceRect(x, y, w, h)) {
                s.stopPlayerForSourceEvent();
                s.sourceStateTrace.add("PORTED/APPROX op13 trigger scene=" + sceneId
                        + " room=" + roomIndex + " group=" + groupIndex
                        + " player=[" + s.player.x + "," + s.player.y + "]");
                return true;
            }
            return false;
        }
    }

    private static final class ActorTransitionFreeWorldTrigger implements Blocking {
        private final int sceneId;
        private final int roomIndex;
        private final int actorId;
        private final int requiredDirection;
        private final int targetSceneId;
        private final int targetRoomIndex;
        private final int targetActorId;
        private boolean started;

        private ActorTransitionFreeWorldTrigger(int sceneId, int roomIndex, int actorId,
                                                int requiredDirection, int targetSceneId,
                                                int targetRoomIndex, int targetActorId) {
            this.sceneId = sceneId;
            this.roomIndex = roomIndex;
            this.actorId = actorId;
            this.requiredDirection = requiredDirection;
            this.targetSceneId = targetSceneId;
            this.targetRoomIndex = targetRoomIndex;
            this.targetActorId = targetActorId;
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                s.sourceStateTrace.add("PORTED/APPROX type1 transition wait scene=" + sceneId
                        + " room=" + roomIndex
                        + " actor=" + actorId
                        + " requiredDir=" + requiredDirection
                        + " target=[" + targetSceneId + "," + targetRoomIndex + "," + targetActorId + "]");
            }
            if (canTrigger(s)) {
                trigger(s);
                return true;
            }
            s.tickFreeWorldPlayer();
            if (canTrigger(s)) {
                trigger(s);
                return true;
            }
            return false;
        }

        private boolean canTrigger(Scene s) {
            return s.player.direction == requiredDirection && s.playerIntersectsActorSourceMask(actorId, true);
        }

        private void trigger(Scene s) {
            s.trySourceTransition(actorId, sourceCFromRequiredDirection(requiredDirection),
                    targetSceneId, targetRoomIndex, targetActorId);
        }

        private static int sourceCFromRequiredDirection(int requiredDirection) {
            for (int c = 0; c < 4; c++) {
                if (Scene.sourceTransitionRequiredDirection(c) == requiredDirection) {
                    return c;
                }
            }
            return -1;
        }
    }

    private static final class ActorInteractionFreeWorldTrigger implements Blocking {
        private final int sceneId;
        private final int roomIndex;
        private final int groupIndex;
        private final int gateSceneId;
        private final int gateRoomIndex;
        private final int gateGroupIndex;
        private final int actorId;
        private boolean started;

        private ActorInteractionFreeWorldTrigger(int sceneId, int roomIndex, int groupIndex,
                                                 int gateSceneId, int gateRoomIndex, int gateGroupIndex,
                                                 int actorId) {
            this.sceneId = sceneId;
            this.roomIndex = roomIndex;
            this.groupIndex = groupIndex;
            this.gateSceneId = gateSceneId;
            this.gateRoomIndex = gateRoomIndex;
            this.gateGroupIndex = gateGroupIndex;
            this.actorId = actorId;
        }

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                s.sourceStateTrace.add("PORTED op86 gate scene=" + sceneId
                        + " room=" + roomIndex
                        + " group=" + groupIndex
                        + " requires [" + gateSceneId + "," + gateRoomIndex + "," + gateGroupIndex + "]="
                        + s.sourceEventState(gateSceneId, gateRoomIndex, gateGroupIndex));
                s.sourceStateTrace.add("PORTED op16 wait actor=" + actorId
                        + " game.k.u emulated by key0 + source-mask interaction");
            }
            if (!s.sourceEventStateComplete(gateSceneId, gateRoomIndex, gateGroupIndex)) {
                s.tickFreeWorldPlayer();
                return false;
            }
            if (s.key0 && s.playerInteractsActorSourceMask(actorId)) {
                s.stopPlayerForSourceEvent();
                s.worldEventActor = actorId;
                s.sourceStateTrace.add("PORTED op16 trigger actor=" + actorId
                        + " player=[" + s.player.x + "," + s.player.y + "]"
                        + " dir=" + s.player.direction);
                return true;
            }
            s.tickFreeWorldPlayer();
            return false;
        }
    }

    private static final class Room0Group6Start implements Blocking {
        private int phase;
        private int wait;
        private Blocking effectWait;

        @Override
        public boolean tick(Scene s) {
            if (!s.sourceEventStateComplete(1, 0, 3)) {
                s.tickFreeWorldPlayer();
                return false;
            }
            if (phase == 0) {
                s.sourceStateTrace.add("PORTED room0 group6 op15 [1,0,3] pass");
                s.setPlayerPositionApprox(199, 218);
                s.player.direction = 2;
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op8 set player=[199,218]");
                s.player.applyMode(2);
                wait = 24;
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op7 actor=-1 state=0 action=2");
                phase = 1;
                return false;
            }
            if (phase == 1) {
                if (wait-- > 0) {
                    return false;
                }
                s.player.applyMode(0);
                effectWait = s.op9SourceEffect("room0 group6", 1, 0, 0, 0, 0, 0);
                phase = 2;
                return false;
            }
            return effectWait == null || effectWait.tick(s);
        }
    }

    private static final class Room0PostGroup6FreeWorld implements Blocking {
        private boolean started;
        private boolean room2Group3Started;
        private boolean dodoPendingLogged;
        private boolean doorPendingLogged;

        @Override
        public boolean tick(Scene s) {
            if (!started) {
                started = true;
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 enters free-world after op40/op14");
            }
            if (s.currentSceneId == 1 && s.currentRoomIndex == 0) {
                return tickRoom0(s);
            }
            if (s.currentSceneId == 1 && s.currentRoomIndex == 2) {
                return tickRoom2(s);
            }
            s.tickFreeWorldPlayer();
            return false;
        }

        private boolean tickRoom0(Scene s) {
            if (s.trySourceTransition(31, 2, 1, 2, 2)) {
                room2Group3Started = false;
                return false;
            }
            if (s.trySourceTransition(30, 3, 1, 1, 37)) {
                return false;
            }
            if (s.key0 && s.playerInteractsActorSourceMask(35) && !dodoPendingLogged) {
                dodoPendingLogged = true;
                s.sourceStateTrace.add("PENDING room0 post-group6 Dodo actor35 side quest groups 7/8/9 not ported yet");
            }
            if (!doorPendingLogged) {
                if (s.playerIntersectsActorSourceMask(3, true)
                        || s.playerIntersectsActorSourceMask(4, true)
                        || s.playerIntersectsActorSourceMask(5, true)) {
                    doorPendingLogged = true;
                    s.sourceStateTrace.add("PENDING room0 post-group6 scene11 door transitions actors=[3,4,5] not ported yet");
                }
            }
            s.tickFreeWorldPlayer();
            if (s.trySourceTransition(31, 2, 1, 2, 2)) {
                room2Group3Started = false;
                return false;
            }
            if (s.trySourceTransition(30, 3, 1, 1, 37)) {
                return false;
            }
            return false;
        }

        private boolean tickRoom2(Scene s) {
            if (!s.sourceEventStateComplete(1, 2, 3) && !room2Group3Started) {
                room2Group3Started = true;
                s.sourceStateTrace.add("PORTED scene1 room2 group3 op15 [1,0,6] pass");
                s.text = TextBox.openBox(VqsvText.Common.MINIMAP_TASK_HELP);
                return false;
            }
            if (room2Group3Started) {
                if (s.text != null && s.text.readyForKey && s.key0) {
                    s.text.confirm();
                    return false;
                }
                if (s.text == null) {
                    s.op14CompleteEvent(1, 2, 3);
                    s.sourceStateTrace.add("PORTED scene1 room2 group3 op14 complete");
                    room2Group3Started = false;
                }
                return false;
            }
            if (s.trySourceTransition(2, 0, 1, 0, 31)) {
                return false;
            }
            s.tickFreeWorldPlayer();
            if (s.trySourceTransition(2, 0, 1, 0, 31)) {
                return false;
            }
            if (s.trySourceTransition(3, 2, 1, 3, 24)) {
                s.sourceStateTrace.add("PENDING scene1 room2 actor3 to room3 target24 needs room3 free-world loader audit");
            }
            return false;
        }
    }

    private static final class Room0Group3PetOffer implements Blocking {
        private static final int[] PET_IDS = {53, 54, 55};
        private static final int[] PET_BRANCH_TARGETS = {4, 8, 12};
        private static final int[][] OP87_ARGS = {
                {0, 51, 7, 3, 2, 30, 45, 0},
                {0, 17, 7, 3, 2, 10, 45, 0},
                {0, 6, 7, 3, 2, 0, 45, 0}
        };
        private boolean started;
        private int phase;
        private int selectedActor = -1;
        private int selectedPetIndex = -1;
        private int choiceIndex;
        private boolean upWasDown;
        private boolean downWasDown;
        private boolean leftWasDown;
        private boolean rightWasDown;
        private Blocking effectWait;

        @Override
        public boolean tick(Scene s) {
            if (!s.sourceEventStateComplete(1, 0, 2)) {
                s.tickFreeWorldPlayer();
                return false;
            }
            if (!started) {
                started = true;
                Scene.setActive(s, PET_IDS, new int[]{0, 0, 0});
                s.sourceStateTrace.add("PORTED room0 group3 op15 [1,0,2] pass");
                s.sourceStateTrace.add("PORTED room0 group3 op2 show pets ids=[53,54,55] dirs=[0,0,0]");
                s.sourceStateTrace.add("PORTED room0 group3 op38 wait pets=[53,54,55] branches=[4,8,12]");
            }
            if (phase == 0) {
                if (s.key0) {
                    for (int i = 0; i < PET_IDS.length; i++) {
                        int petId = PET_IDS[i];
                        if (!s.playerInteractsActorSourceMask(petId)) {
                            continue;
                        }
                        selectedActor = petId;
                        selectedPetIndex = i;
                        s.stopPlayerForSourceEvent();
                        s.sourceStateTrace.add("PORTED room0 group3 op38 selected actor="
                                + petId + " branch=" + PET_BRANCH_TARGETS[i]);
                        s.text = TextBox.dialog(s.font, "\u0054\u0072\u01b0\u1edf\u006e\u0067 \u0074\u0068\u00f4\u006e",
                                petDescription(petId), 1);
                        phase = 1;
                        return false;
                    }
                }
                s.tickFreeWorldPlayer();
                return false;
            }
            if (phase == 1) {
                if (s.text != null && s.text.readyForKey && s.key0) {
                    s.text.confirm();
                    return false;
                }
                if (s.text == null) {
                    choiceIndex = 0;
                    upWasDown = s.keyUp;
                    downWasDown = s.keyDown;
                    leftWasDown = s.keyLeft;
                    rightWasDown = s.keyRight;
                    s.choice = ChoiceBox.optionUi(0, new String[]{"C\u00f3", "Kh\u00f4ng"});
                    s.sourceStateTrace.add("PORTED/APPROX room0 group3 op35 choice shown actor="
                            + selectedActor + " branches=" + op35BranchesForSelectedPet()
                            + "; source game.h mode=0 /data/ui/option.ui coordinates/cells ported, full ao renderer pending");
                    phase = 2;
                }
                return false;
            }
            if (phase == 2) {
                if ((s.keyUp && !upWasDown) || (s.keyLeft && !leftWasDown)) {
                    s.choice.move(-1);
                }
                if ((s.keyDown && !downWasDown) || (s.keyRight && !rightWasDown)) {
                    s.choice.move(1);
                }
                upWasDown = s.keyUp;
                downWasDown = s.keyDown;
                leftWasDown = s.keyLeft;
                rightWasDown = s.keyRight;
                if (s.choice != null && s.key0) {
                    choiceIndex = s.choice.selectedIndex();
                    s.choice = null;
                    if (choiceIndex == 1) {
                        s.sourceStateTrace.add("PORTED room0 group3 op35 selected No branch target=2 return to op38");
                        phase = 0;
                        selectedActor = -1;
                        selectedPetIndex = -1;
                        return false;
                    }
                    applyOp87(s);
                    s.text = TextBox.openBox("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + petRewardName(selectedActor));
                    phase = 3;
                }
                return false;
            }
            if (phase == 3) {
                if (s.text != null && s.text.readyForKey && s.key0) {
                    s.text.confirm();
                    s.sourceStateTrace.add("PORTED room0 group3 op41 [16] jump to record 15");
                    Scene.hide(s, PET_IDS);
                    s.sourceStateTrace.add("PORTED room0 group3 op3 hide pets ids=[53,54,55] states=[1,1,1]");
                    effectWait = s.op9SourceEffect("room0 group3", 2, 0, 0, 0, 0, 0);
                    phase = 4;
                }
                return false;
            }
            if (phase == 4) {
                if (effectWait != null && !effectWait.tick(s)) {
                    return false;
                }
                s.op14CompleteEvent(1, 0, 3);
                s.sourceStateTrace.add("PORTED room0 group3 op14 complete");
                return true;
            }
            return false;
        }

        private void applyOp87(Scene s) {
            int[] args = OP87_ARGS[selectedPetIndex];
            if (args[0] == 0) {
                SourcePetState pet = new SourcePetState(args[7], args[1], args[2], args[3], args[4], args[5], args[6]);
                s.sourcePets.add(pet);
                s.sourceStateTrace.add("PORTED/APPROX room0 group3 op87 addPet args="
                        + Arrays.toString(args)
                        + " stored slot=" + pet.slot
                        + " species=" + pet.speciesId
                        + " level=" + pet.level
                        + " skills=[" + pet.skillIds[0] + "," + pet.skillIds[1] + "]"
                        + "; full game.g pet inventory UI still pending");
            } else {
                s.sourceStateTrace.add("UNKNOWN room0 group3 op87 unsupported mode args=" + Arrays.toString(args));
            }
        }

        private String op35BranchesForSelectedPet() {
            switch (selectedActor) {
                case 53:
                    return "[6,2]";
                case 54:
                    return "[10,2]";
                case 55:
                    return "[14,2]";
                default:
                    return "[]";
            }
        }

        private static String petRewardName(int petId) {
            switch (petId) {
                case 53:
                    return VqsvText.Scene1Room0Group3.PENGUIN;
                case 54:
                    return VqsvText.Scene1Room0Group3.FROG;
                case 55:
                    return VqsvText.Scene1Room0Group3.DRAGON;
                default:
                    return "S\u1ee7ng v\u1eadt";
            }
        }

        private static String petDescription(int petId) {
            switch (petId) {
                case 53:
                    return VqsvText.Scene1Room0Group3.PENGUIN;
                case 54:
                    return VqsvText.Scene1Room0Group3.FROG;
                case 55:
                    return VqsvText.Scene1Room0Group3.DRAGON;
                default:
                    return "";
            }
        }
    }

    private static final class OldRoom0Group3PetOffer implements Blocking {
        private static final int[] PET_IDS = {53, 54, 55};
        private static final int[] PET_BRANCH_TARGETS = {4, 8, 12};
        private boolean started;
        private int phase;
        private int selectedActor = -1;

        @Override
        public boolean tick(Scene s) {
            if (!s.sourceEventStateComplete(1, 0, 2)) {
                s.tickFreeWorldPlayer();
                return false;
            }
            if (!started) {
                started = true;
                Scene.setActive(s, PET_IDS, new int[]{0, 0, 0});
                s.sourceStateTrace.add("PORTED room0 group3 op15 [1,0,2] pass");
                s.sourceStateTrace.add("PORTED room0 group3 op2 show pets ids=[53,54,55] dirs=[0,0,0]");
                s.sourceStateTrace.add("PORTED/APPROX room0 group3 op38 wait pets=[53,54,55] branches=[4,8,12]");
            }
            if (phase == 0) {
                if (s.key0) {
                    for (int i = 0; i < PET_IDS.length; i++) {
                        int petId = PET_IDS[i];
                        if (!s.playerInteractsActorSourceMask(petId)) {
                            continue;
                        }
                        selectedActor = petId;
                        s.stopPlayerForSourceEvent();
                        s.sourceStateTrace.add("PORTED/APPROX room0 group3 op38 selected actor="
                                + petId + " branch=" + PET_BRANCH_TARGETS[i]);
                        s.text = TextBox.dialog(s.font, VqsvText.Scene1Room0Group3.ELDER, petDescription(petId), 1);
                        phase = 1;
                        return false;
                    }
                }
                s.tickFreeWorldPlayer();
                return false;
            }
            if (phase == 1) {
                if (s.text != null && s.text.readyForKey && s.key0) {
                    s.text.confirm();
                    return false;
                }
                if (s.text == null) {
                    s.text = TextBox.openBox(VqsvText.Scene1Room0Group3.YES_NO);
                    s.sourceStateTrace.add("PENDING/APPROX room0 group3 op35 choice shown for actor="
                            + selectedActor + "; op87 pet grant not ported yet");
                    phase = 2;
                }
                return false;
            }
            if (phase == 2) {
                if (s.text != null && s.text.readyForKey && s.key0) {
                    s.text.confirm();
                    phase = 0;
                    selectedActor = -1;
                    s.sourceStateTrace.add("PENDING room0 group3 op35/op87 selection branch not executed; returned to pet wait");
                }
                return false;
            }
            return false;
        }

        private static String petDescription(int petId) {
            switch (petId) {
                case 53:
                    return VqsvText.Scene1Room0Group3.PENGUIN;
                case 54:
                    return VqsvText.Scene1Room0Group3.FROG;
                case 55:
                    return VqsvText.Scene1Room0Group3.DRAGON;
                default:
                    return "";
            }
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

    private static final class SourceBattleRuntime implements Blocking {
        private final int actorId;
        private final int[] encounter;
        private final int[] flags;
        private final int[] battleMode;
        private final int[] branchTargets;
        private final int forcedResultIndex;
        private final boolean sourceBattleSlice;
        private int phase;
        private int wait;
        private SourceBattleUnit enemy;
        private SourceBattleUnit player;
        private int turn;
        private boolean bunnyTutorialShown;
        private boolean captureStarted;

        private SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode, int[] branchTargets) {
            this(actorId, encounter, flags, battleMode, branchTargets, 0, false);
        }

        private SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode,
                                   int[] branchTargets, int forcedResultIndex) {
            this(actorId, encounter, flags, battleMode, branchTargets, forcedResultIndex, false);
        }

        private SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode,
                                   int[] branchTargets, int forcedResultIndex, boolean sourceBattleSlice) {
            this.actorId = actorId;
            this.encounter = encounter;
            this.flags = flags;
            this.battleMode = battleMode;
            this.branchTargets = branchTargets;
            this.forcedResultIndex = forcedResultIndex;
            this.sourceBattleSlice = sourceBattleSlice;
        }

        @Override
        public boolean tick(Scene s) {
            switch (phase) {
                case 0:
                    enemy = SourceBattleUnit.enemyFromEncounter(encounter);
                    player = SourceBattleUnit.playerFromSourcePets(s.sourcePets);
                    if (isKidnappingBattle()) {
                        player = SourceBattleUnit.fallback(-1, 6, 3, "Neil", 120, 22, 12, 10);
                    }
                    s.worldEventActor = actorId;
                    s.battleEventActor = actorId;
                    s.battleEncounter = Arrays.copyOf(encounter, encounter.length);
                    s.battleCanLose = flags.length > 0 && flags[0] == 0;
                    s.battleScriptLocksInput = flags.length > 1 && flags[1] == 0;
                    s.battleMode = battleMode.length > 0 ? battleMode[0] : -1;
                    s.battleBackgroundMode = battleMode.length > 1 ? battleMode[1] : -1;
                    s.battleResultIndex = -2;
                    s.battleBranchTarget = resolveBranch(s.battleResultIndex);
                    s.battleCaptureTutorial = isBunnyCaptureBattle();
                    syncRenderState(s, VqsvText.Battle.START);
                    s.sourceStateTrace.add("PORTED/APPROX source battle runtime actor=" + actorId
                            + " encounter=" + Arrays.toString(encounter)
                            + " flags=" + Arrays.toString(flags)
                            + " mode=" + Arrays.toString(battleMode)
                            + " enemy=" + enemy
                            + " player=" + player
                            + " branchTargets=" + Arrays.toString(branchTargets)
                            + "; NOT full game.d command UI/status/effect engine");
                    s.effect.startFade(2, 0);
                    phase = 1;
                    return false;
                case 1:
                    if (!s.effect.doneOverlay(s)) {
                        return false;
                    }
                    s.battleOverlayTicks = 1;
                    wait = 18;
                    phase = 2;
                    return false;
                case 2:
                    s.battleOverlayTicks = 1;
                    if (wait-- > 0) {
                        return false;
                    }
                    if (advanceBattle(s)) {
                        wait = 18;
                        phase = 3;
                    } else {
                        wait = 22;
                    }
                    return false;
                case 3:
                    s.battleOverlayTicks = 1;
                    if (wait-- > 0) {
                        return false;
                    }
                    s.battleOverlayTicks = 0;
                    s.battleCaptureTutorial = false;
                    s.effect.startFade(1, 0);
                    phase = 4;
                    return false;
                case 4:
                    return s.effect.doneOverlay(s);
                default:
                    return true;
            }
        }

        private boolean advanceBattle(Scene s) {
            if (isBunnyCaptureBattle()) {
                return advanceBunnyCapture(s);
            }
            if (isKidnappingBattle()) {
                int damage = Math.max(1, enemy.basicDamageTo(player));
                player.damage(damage);
                turn++;
                syncRenderState(s, enemy.name + VqsvText.Battle.DAMAGE + damage + VqsvText.Battle.DAMAGE_SUFFIX);
                if (!player.alive()) {
                    s.battleResultIndex = forcedResultIndex;
                    s.battleBranchTarget = resolveBranch(s.battleResultIndex);
                    s.battleLog = VqsvText.Battle.NEIL_LOST + s.battleResultIndex;
                    s.sourceStateTrace.add("PORTED/APPROX kidnapping battle resolved by source stats; resultIndex="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                    return true;
                }
                return false;
            }
            SourceBattleUnit first = player.speed >= enemy.speed ? player : enemy;
            SourceBattleUnit second = first == player ? enemy : player;
            applyAttack(s, first, second);
            if (!second.alive()) {
                return finishNormalBattle(s);
            }
            applyAttack(s, second, first);
            if (!first.alive()) {
                return finishNormalBattle(s);
            }
            turn++;
            return false;
        }

        private boolean advanceBunnyCapture(Scene s) {
            if (!bunnyTutorialShown) {
                int damage = Math.max(1, player.basicDamageTo(enemy));
                enemy.damage(damage);
                turn++;
                if (enemy.hp > enemy.maxHp / 2) {
                    syncRenderState(s, player.name + VqsvText.Battle.DAMAGE + damage + VqsvText.Battle.DAMAGE_SUFFIX);
                    return false;
                }
                bunnyTutorialShown = true;
                syncRenderState(s, VqsvText.Battle.BUNNY_WEAK);
                s.sourceStateTrace.add("PORTED/APPROX bunny tutorial source game.d.l(): HP<=50% then prompt capture ball");
                return false;
            }
            if (!captureStarted) {
                captureStarted = true;
                syncRenderState(s, VqsvText.Battle.BALL_CHOSEN);
                return false;
            }
            enemy.hp = 0;
            s.battleResultIndex = forcedResultIndex;
            s.battleBranchTarget = resolveBranch(s.battleResultIndex);
            syncRenderState(s, VqsvText.Battle.BUNNY_CAUGHT + s.battleResultIndex);
            s.sourceStateTrace.add("PORTED/APPROX bunny capture resolved; op47 sees l=-1/continue success path in manual script");
            return true;
        }

        private void applyAttack(Scene s, SourceBattleUnit attacker, SourceBattleUnit target) {
            int damage = attacker.basicDamageTo(target);
            target.damage(damage);
            syncRenderState(s, attacker.name + VqsvText.Battle.DAMAGE + damage + VqsvText.Battle.DAMAGE_SUFFIX);
        }

        private boolean finishNormalBattle(Scene s) {
            int result = player.alive() ? 0 : Math.max(0, forcedResultIndex);
            if (!player.alive() && forcedResultIndex == 0 && isElderBattle()) {
                player.hp = 1;
                enemy.hp = 0;
                result = 0;
                syncRenderState(s, VqsvText.Battle.ELDER_DONE);
            }
            s.battleResultIndex = result;
            s.battleBranchTarget = resolveBranch(s.battleResultIndex);
            s.sourceStateTrace.add("PORTED/APPROX battle resolved resultIndex="
                    + s.battleResultIndex + " branch=" + s.battleBranchTarget
                    + " playerHp=" + player.hp + "/" + player.maxHp
                    + " enemyHp=" + enemy.hp + "/" + enemy.maxHp);
            return true;
        }

        private void syncRenderState(Scene s, String log) {
            s.battleEnemyName = enemy.name;
            s.battleEnemyLevel = enemy.level;
            s.battleEnemyVisualId = enemy.visualId;
            s.battleEnemyElement = enemy.element;
            s.battleEnemyMaxHp = enemy.maxHp;
            s.battleEnemyHp = enemy.hp;
            s.battlePlayerName = player.name;
            s.battlePlayerLevel = player.level;
            s.battlePlayerVisualId = player.visualId;
            s.battlePlayerElement = player.element;
            s.battlePlayerMaxHp = player.maxHp;
            s.battlePlayerHp = player.hp;
            s.battlePlayerEnergy = 0;
            s.battlePlayerMaxEnergy = player.nextLevelEnergy();
            byte relation = player.elementRelationTo(enemy);
            if (relation == 0) {
                s.battlePlayerPowerPercent = 300;
                s.battleEnemyPowerPercent = 60;
            } else if (relation == 1) {
                s.battlePlayerPowerPercent = 60;
                s.battleEnemyPowerPercent = 300;
            } else {
                s.battlePlayerPowerPercent = 100;
                s.battleEnemyPowerPercent = 100;
            }
            s.battleTurn = turn;
            s.battleLog = log;
        }

        private boolean isKidnappingBattle() {
            return encounter.length >= 2 && encounter[0] == 5 && encounter[1] == 20;
        }

        private boolean isBunnyCaptureBattle() {
            return encounter.length >= 1 && encounter[0] == 34;
        }

        private boolean isElderBattle() {
            return encounter.length >= 1 && encounter[0] == 68;
        }

        private int resolveBranch(int resultIndex) {
            if (resultIndex < 0) {
                return -1;
            }
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
        private int appliedMode = Integer.MIN_VALUE;
        private int appliedDirection = Integer.MIN_VALUE;

        private Actor(int id, int spriteIndex, int state, int x, int y) {
            this(id, spriteIndex, state, x, y, 0, 1);
        }

        private Actor(int id, int spriteIndex, int state, int x, int y, int variant) {
            this(id, spriteIndex, state, x, y, variant, 1);
        }

        private Actor(int id, int spriteIndex, int state, int x, int y, int variant, int layer) {
            this.anim = SpriteAnim.load(spriteIndex);
            this.variant = variant;
            this.layer = layer;
            this.x = x;
            this.y = y;
            this.direction = state;
            if (variant == 1 || variant == 18) {
                applyMode(0);
            } else {
                this.anim.setState(state);
            }
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
            if (appliedMode == mode && appliedDirection == direction) {
                return;
            }
            appliedMode = mode;
            appliedDirection = direction;
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

        private short[] collisionMask() {
            return anim.currentCollisionMask();
        }

        private short[] hitMask() {
            return anim.currentHitMask();
        }
    }

    private static final class WorldUi {
        private final SpriteAnim ui = SpriteAnim.load(257);
        private boolean visible;

        private void render(Graphics2D g, boolean worldVisible) {
            if (!visible || !worldVisible) {
                return;
            }
            drawCellTopLeft(g, 167, 1, 303);
            drawCellTopLeft(g, 68, 222, 303);
        }

        private void drawCellTopLeft(Graphics2D g, int cellId, int x, int y) {
            int[] bounds = ui.cellBounds(cellId);
            if (bounds == null) {
                return;
            }
            ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
        }
    }

    private static final class ChoiceBox {
        private static final int OPTION_FRAME_CELL = 155;
        private static final int OPTION_MARKER_CELL = 31;
        private static final int OPTION_X = 78;
        private static final int OPTION_W = 88;
        private static final int TEXT_X = 84;
        private static final int TEXT_W = 74;
        private static final int[] OPTION_Y = {131, 172};
        private static final int[] TEXT_Y = {135, 176};
        private static final int[] MARKER_Y = {138, 179};
        private final SpriteAnim ui = SpriteAnim.load(257);
        private final String[] options;
        private int selected;

        private ChoiceBox(int selected, String[] options) {
            this.options = options;
            this.selected = Math.max(0, Math.min(options.length - 1, selected));
        }

        private static ChoiceBox optionUi(int selected, String[] options) {
            return new ChoiceBox(selected, options);
        }

        private void move(int delta) {
            if (options.length == 0) {
                selected = 0;
                return;
            }
            selected = Math.max(0, Math.min(options.length - 1, selected + delta));
        }

        private boolean click(int x, int y) {
            for (int i = 0; i < options.length && i < OPTION_Y.length; i++) {
                if (x >= OPTION_X && x <= OPTION_X + OPTION_W && y >= OPTION_Y[i] && y <= OPTION_Y[i] + 34) {
                    selected = i;
                    return true;
                }
            }
            return false;
        }

        private int selectedIndex() {
            return selected;
        }

        private void render(Graphics2D g, FontBitmap font) {
            for (int i = 0; i < options.length && i < OPTION_Y.length; i++) {
                drawCellTopLeft(g, OPTION_FRAME_CELL, OPTION_X, OPTION_Y[i]);
                int textWidth = font.width(options[i]);
                font.drawTaggedLine(g, options[i], TEXT_X + (TEXT_W - textWidth) / 2, TEXT_Y[i],
                        options[i].length(), 0x1C6C91);
                if (i == selected) {
                    drawCellTopLeft(g, OPTION_MARKER_CELL, 150, MARKER_Y[i]);
                }
            }
        }

        private void drawCellTopLeft(Graphics2D g, int cellId, int x, int y) {
            int[] bounds = ui.cellBounds(cellId);
            if (bounds == null) {
                return;
            }
            ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
        }
    }

    private static final class TempSprite {
        private final int actorId;
        private final SpriteAnim anim = SpriteAnim.load(259);
        private final boolean fixedPosition;
        private final int x;
        private final int y;
        private int left;

        private TempSprite(int actorId, int animation, int duration) {
            this.actorId = actorId;
            this.fixedPosition = false;
            this.x = 0;
            this.y = 0;
            this.left = duration;
            anim.setState(animation);
        }

        private TempSprite(int x, int y, int animation, int duration) {
            this.actorId = -2;
            this.fixedPosition = true;
            this.x = x;
            this.y = y;
            this.left = duration;
            anim.setState(animation);
        }

        private boolean tick(Scene scene) {
            boolean cycleDone = anim.tick();
            return cycleDone || left-- <= 0
                    || !fixedPosition && actorId >= 0
                    && (actorId >= scene.actors.length || scene.actors[actorId] == null);
        }

        private void render(Graphics2D g, Scene scene) {
            if (fixedPosition) {
                anim.draw(g, x - scene.cameraX, y - scene.cameraY, 0);
                return;
            }
            Actor actor = actorId == -1 ? scene.player : scene.actors[actorId];
            if (actor != null && actor.visible) {
                anim.draw(g, actor.x - scene.cameraX, actor.y - scene.cameraY - 24, 0);
            }
        }
    }

    private static final class SpriteAnim {
        private static final int[][] SPRITE_TO_IMGS;
        private static final SpriteTable SOURCE_SPRITE_TABLE = loadSourceSpriteTable();
        private static final Map<String, SpriteData> CACHE = new HashMap<>();
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
                    {23, 23, 123}, {25, 25, 124},
                    {50, 50, 136}, {51, 51, 136}, {52, 52, 136}, {53, 53, 137}, {54, 54, 137},
                    {66, 66, 146}, {69, 69, 149}, {92, 92, 506}, {102, 102, 574}, {137, 137, 520},
                    {198, 198, 212}, {201, 201, 220}, {208, 208, 220}, {209, 209, 220},
                    {213, 213, 223}, {230, 230, 217}, {339, 339, 836},
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
            SpriteRef ref = SpriteRef.from(spriteIndex);
            String key = ref.sprId + ":" + Arrays.toString(ref.imageIds);
            SpriteData data = CACHE.computeIfAbsent(key, ignored -> SpriteData.load(ref.sprId, ref.imageIds));
            return new SpriteAnim(data);
        }

        private static SpriteTable loadSourceSpriteTable() {
            try {
                return SpriteTable.load(AssetPaths.fromWorkingTree(GameConfig.defaultConfig()));
            } catch (RuntimeException ex) {
                return null;
            }
        }

        private static final class SpriteRef {
            private final int sprId;
            private final int[] imageIds;

            private SpriteRef(int sprId, int[] imageIds) {
                this.sprId = sprId;
                this.imageIds = imageIds;
            }

            private static SpriteRef from(int spriteIndex) {
                if (SOURCE_SPRITE_TABLE != null && spriteIndex >= 0 && spriteIndex < SOURCE_SPRITE_TABLE.size()) {
                    int sprId = SOURCE_SPRITE_TABLE.sprId(spriteIndex);
                    int[] imageIds = SOURCE_SPRITE_TABLE.imageIds(spriteIndex);
                    if (sprId >= 0 && imageIds.length > 0) {
                        return new SpriteRef(sprId, imageIds);
                    }
                }
                int[] imageIds = spriteIndex >= 0 && spriteIndex < SPRITE_TO_IMGS.length
                        ? SPRITE_TO_IMGS[spriteIndex]
                        : null;
                if (imageIds == null) {
                    imageIds = new int[0];
                }
                return new SpriteRef(spriteIndex, imageIds);
            }
        }

        private void setState(int state) {
            if (data.anim != null && state >= 0 && state < data.anim.length) {
                this.state = state;
            } else {
                this.state = 0;
            }
            cursor = 0;
            resetDelay();
        }

        private boolean tick() {
            if (data.anim == null || data.anim.length == 0) {
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

        private void tickHoldLast() {
            if (data.anim == null || data.anim.length == 0 || data.anim[state].length == 0) {
                return;
            }
            int last = data.anim[state].length / 2 - 1;
            if (cursor >= last) {
                return;
            }
            if (delay > 0) {
                delay--;
                return;
            }
            cursor++;
            resetDelay();
        }

        private void resetDelay() {
            if (data.anim == null || data.anim.length == 0 || data.anim[state].length == 0) {
                delay = 0;
                return;
            }
            delay = Math.max(0, data.anim[state][cursor * 2] - 1);
        }

        private void draw(Graphics2D g, int x, int y, int orientation) {
            if (data.anim == null || data.anim.length == 0 || data.anim[state].length == 0) {
                return;
            }
            int cellId = data.anim[state][cursor * 2 + 1];
            drawCell(g, cellId, x, y, orientation);
        }

        private void drawAligned(Graphics2D g, int rectX, int rectY, int rectW, int rectH, int align, int orientation) {
            int[] bounds = animationBounds(state);
            if (bounds == null) {
                return;
            }
            int drawX = rectX;
            int drawY = rectY;
            switch (align) {
                case 4:
                    drawX = rectX + (rectW - bounds[2]) / 2 - bounds[0];
                    drawY = rectY + (rectH - bounds[3]) / 2 - bounds[1];
                    break;
                case 3:
                    drawX = rectX - bounds[0];
                    drawY = rectY + (rectH - bounds[3]) / 2 - bounds[1];
                    break;
                case 5:
                    drawX = rectX + (rectW - bounds[2]) - bounds[0];
                    drawY = rectY + (rectH - bounds[3]) / 2 - bounds[1];
                    break;
                case 6:
                    drawX = rectX - bounds[0];
                    drawY = rectY + (rectH - bounds[3]) - bounds[1];
                    break;
                case 8:
                    drawX = rectX + (rectW - bounds[2]) - bounds[0];
                    drawY = rectY + (rectH - bounds[3]) - bounds[1];
                    break;
                case 7:
                    drawX = rectX + (rectW - bounds[2]) / 2 - bounds[0];
                    drawY = rectY + (rectH - bounds[3]) - bounds[1];
                    break;
                case 2:
                    drawX = rectX + (rectW - bounds[2]) - bounds[0];
                    drawY = rectY - bounds[1];
                    break;
                case 1:
                    drawX = rectX + (rectW - bounds[2]) / 2 - bounds[0];
                    drawY = rectY - bounds[1];
                    break;
                case 0:
                default:
                    drawX = rectX - bounds[0];
                    drawY = rectY - bounds[1];
                    break;
            }
            draw(g, drawX, drawY, orientation);
        }

        private int[] cellBounds(int cellId) {
            return data.cellBounds(cellId);
        }

        private short[] currentCollisionMask() {
            if (data.anim == null || state < 0 || state >= data.anim.length || data.anim[state].length == 0) {
                return null;
            }
            int cellId = data.anim[state][cursor * 2 + 1];
            return data.collisionMask(cellId);
        }

        private short[] currentHitMask() {
            if (data.anim == null || state < 0 || state >= data.anim.length || data.anim[state].length == 0) {
                return null;
            }
            int cellId = data.anim[state][cursor * 2 + 1];
            return data.hitMask(cellId);
        }

        private int[] animationBounds(int animState) {
            if (data.anim == null || animState < 0 || animState >= data.anim.length || data.anim[animState].length == 0) {
                return null;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            short[] frames = data.anim[animState];
            for (int i = 0; i < frames.length; i += 2) {
                int[] bounds = data.cellBounds(frames[i + 1]);
                if (bounds == null) {
                    continue;
                }
                minX = Math.min(minX, bounds[0]);
                minY = Math.min(minY, bounds[1]);
                maxX = Math.max(maxX, bounds[0] + bounds[2]);
                maxY = Math.max(maxY, bounds[1] + bounds[3]);
            }
            if (minX == Integer.MAX_VALUE) {
                return null;
            }
            return new int[]{minX, minY, maxX - minX, maxY - minY};
        }

        private void drawCell(Graphics2D g, int cellId, int x, int y, int orientation) {
            if (cellId < 0 || cellId >= data.cells.length) {
                return;
            }
            int[] transformMap = orientation == 1
                    ? new int[]{2, 4, 1, 7, 0, 5, 3, 6}
                    : new int[]{0, 5, 3, 6, 2, 4, 1, 7};
            short[] cells = data.cells[cellId];
            for (int i = 0; i < cells.length; i += 4) {
                int frameId = cells[i];
                if (frameId < 0 || frameId >= data.frames.length) {
                    continue;
                }
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
            g.drawImage(transformedRegion(sub, transform), x, y, null);
        }

        private static BufferedImage transformedRegion(BufferedImage src, int transform) {
            int w = src.getWidth();
            int h = src.getHeight();
            int outW = (transform == 4 || transform == 5 || transform == 6 || transform == 7) ? h : w;
            int outH = (transform == 4 || transform == 5 || transform == 6 || transform == 7) ? w : h;
            BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
            for (int dy = 0; dy < outH; dy++) {
                for (int dx = 0; dx < outW; dx++) {
                    int sx;
                    int sy;
                    switch (transform) {
                        case 1: // MIDP TRANS_MIRROR_ROT180
                            sx = dx;
                            sy = h - 1 - dy;
                            break;
                        case 2: // MIDP TRANS_MIRROR
                            sx = w - 1 - dx;
                            sy = dy;
                            break;
                        case 3: // MIDP TRANS_ROT180
                            sx = w - 1 - dx;
                            sy = h - 1 - dy;
                            break;
                        case 4: // MIDP TRANS_MIRROR_ROT270
                            sx = dy;
                            sy = dx;
                            break;
                        case 5: // MIDP TRANS_ROT90
                            sx = dy;
                            sy = h - 1 - dx;
                            break;
                        case 6: // MIDP TRANS_ROT270
                            sx = w - 1 - dy;
                            sy = dx;
                            break;
                        case 7: // MIDP TRANS_MIRROR_ROT90
                            sx = w - 1 - dy;
                            sy = h - 1 - dx;
                            break;
                        case 0:
                        default:
                            sx = dx;
                            sy = dy;
                            break;
                    }
                    out.setRGB(dx, dy, src.getRGB(sx, sy));
                }
            }
            return out;
        }
    }

    private static final class SpriteData {
        private final short[][] frames;
        private final short[][] cells;
        private final short[][] anim;
        private final short[][] hitMasks;
        private final short[][] collisionMasks;
        private final BufferedImage[] images;

        private SpriteData(short[][] frames, short[][] cells, short[][] anim,
                           short[][] hitMasks, short[][] collisionMasks, BufferedImage[] images) {
            this.frames = frames;
            this.cells = cells;
            this.anim = anim;
            this.hitMasks = hitMasks;
            this.collisionMasks = collisionMasks;
            this.images = images;
        }

        private BufferedImage imageForFrame(int frameId) {
            int slot = frames[frameId][0];
            if (slot < 0 || slot >= images.length) {
                throw new IllegalStateException("Frame " + frameId + " references missing image slot " + slot);
            }
            return images[slot];
        }

        private static SpriteData load(int sprId, int[] imageIds) {
            try {
                byte[] bytes = readSpriteBytes(sprId);
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
                short[][] collisionMasks = masksByCell(readFlat(bytes, c), cells.length);
                short[][] hitMasks = masksByCell(readFlat(bytes, c), cells.length);
                if (imageIds == null || imageIds.length == 0) {
                    throw new IOException("Missing image mapping for sprite " + sprId);
                }
                BufferedImage[] images = new BufferedImage[imageIds.length];
                for (int i = 0; i < imageIds.length; i++) {
                    images[i] = readSpriteImage(imageIds[i]);
                    if (images[i] == null) {
                        throw new IOException("Missing image " + imageIds[i] + " for sprite " + sprId);
                    }
                }
                return new SpriteData(frames, cells, anim, hitMasks, collisionMasks, images);
            } catch (Exception ex) {
                return blank();
            }
        }

        private static SpriteData blank() {
            return new SpriteData(new short[0][0], new short[0][0], new short[0][0],
                    null, null, new BufferedImage[0]);
        }

        private static byte[] readSpriteBytes(int sprId) throws IOException {
            java.nio.file.Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig()).sprOriginal(sprId);
            if (Files.isRegularFile(path)) {
                return Files.readAllBytes(path);
            }
            return readAll("/spr_" + sprId + "_all(r)");
        }

        private static BufferedImage readSpriteImage(int imageId) throws IOException {
            java.nio.file.Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig()).imgDecodedPng(imageId);
            if (Files.isRegularFile(path)) {
                return ImageIO.read(path.toFile());
            }
            return ImageIO.read(SpriteData.class.getResource("/img/" + imageId + ".png"));
        }

        private int[] cellBounds(int cellId) {
            if (cellId < 0 || cellId >= cells.length || cells[cellId].length == 0) {
                return null;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            short[] cell = cells[cellId];
            for (int i = 0; i < cell.length; i += 4) {
                int frameId = cell[i];
                if (frameId < 0 || frameId >= frames.length) {
                    continue;
                }
                int x = cell[i + 1];
                int y = cell[i + 2];
                int w = frames[frameId][3];
                int h = frames[frameId][4];
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x + w);
                maxY = Math.max(maxY, y + h);
            }
            if (minX == Integer.MAX_VALUE) {
                return null;
            }
            return new int[]{minX, minY, maxX - minX, maxY - minY};
        }

        private short[] collisionMask(int cellId) {
            if (collisionMasks == null || cellId < 0 || cellId >= collisionMasks.length) {
                return null;
            }
            return collisionMasks[cellId];
        }

        private short[] hitMask(int cellId) {
            if (hitMasks == null || cellId < 0 || cellId >= hitMasks.length) {
                return null;
            }
            return hitMasks[cellId];
        }

        private static short[][] masksByCell(short[] flat, int cellCount) {
            if (flat == null) {
                return null;
            }
            short[][] out = new short[cellCount][];
            for (int i = 0; i + 4 < flat.length; i += 5) {
                int cellId = flat[i];
                if (cellId < 0 || cellId >= out.length) {
                    continue;
                }
                short[] rect = new short[]{flat[i + 1], flat[i + 2], flat[i + 3], flat[i + 4]};
                out[cellId] = appendMaskRect(out[cellId], rect);
            }
            return out;
        }

        private static short[] appendMaskRect(short[] current, short[] rect) {
            if (current == null) {
                return rect;
            }
            short[] out = Arrays.copyOf(current, current.length + rect.length);
            System.arraycopy(rect, 0, out, current.length, rect.length);
            return out;
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
        private static final java.awt.Font DISPLAY_FONT = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 9);
        private static final java.awt.font.FontRenderContext FONT_CONTEXT =
                new java.awt.font.FontRenderContext(null, false, false);
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
            if (c == ' ') {
                return 3;
            }
            return Math.max(1, (int) Math.ceil(DISPLAY_FONT
                    .getStringBounds(String.valueOf(c), FONT_CONTEXT)
                    .getWidth()));
        }

        private int width(String s) {
            s = TextBox.decodeMojibake(s);
            int w = 0;
            for (int i = 0; i < s.length(); i++) {
                w += charWidth(s.charAt(i));
            }
            return w;
        }

        private int taggedWidth(String s) {
            s = TextBox.decodeMojibake(s);
            int w = 0;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '#' && i + 6 < s.length()) {
                    i += 6;
                    continue;
                }
                w += charWidth(s.charAt(i));
            }
            return w;
        }

        private void drawChar(Graphics2D g, char c, int x, int y) {
            if (c == ' ') {
                return;
            }
            java.awt.Font oldFont = g.getFont();
            Object oldAa = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            g.setFont(DISPLAY_FONT);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.drawString(String.valueOf(c), x, y + 9);
            g.setFont(oldFont);
            if (oldAa != null) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAa);
            }
        }

        private char glyphChar(char c) {
            if (c == '\u0111') {
                return 'd';
            }
            if (c == '\u0110') {
                return 'D';
            }
            String decomposed = java.text.Normalizer.normalize(String.valueOf(c), java.text.Normalizer.Form.NFD);
            if (decomposed.indexOf('\u0323') >= 0 && !decomposed.isEmpty()) {
                char base = decomposed.charAt(0);
                if (index.containsKey(base)) {
                    return base;
                }
            }
            if (index.containsKey(c)) {
                return c;
            }
            if (!decomposed.isEmpty()) {
                char base = decomposed.charAt(0);
                if (base != c && index.containsKey(base)) {
                    return base;
                }
            }
            return c;
        }

        private void drawTagged(Graphics2D g, String s, int x, int y, int maxWidth, int visibleChars) {
            s = TextBox.decodeMojibake(s);
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

        private void drawTaggedLine(Graphics2D g, String s, int x, int y, int visibleChars, int defaultColor) {
            s = TextBox.decodeMojibake(s);
            int cx = x;
            int color = defaultColor;
            g.setColor(new Color(color));
            int shown = 0;
            for (int i = 0; i < s.length() && shown < visibleChars; i++) {
                char ch = s.charAt(i);
                if (ch == '#' && i + 6 < s.length()) {
                    String hex = s.substring(i + 1, i + 7);
                    color = Integer.parseInt(hex, 16);
                    g.setColor(new Color(color));
                    i += 6;
                    continue;
                }
                drawChar(g, ch, cx, y);
                cx += charWidth(ch);
                shown++;
            }
        }
    }

    private static final class TextBox {
        private static final int SOURCE_NONE = 0;
        private static final int SOURCE_OPENBOX = 1;
        private static final int SOURCE_TASKTIP = 2;
        private static final int OPENBOX_FRAME_X = 45;
        private static final int OPENBOX_FRAME_Y = 147;
        private static final int OPENBOX_FRAME_W = 150;
        private static final int OPENBOX_FRAME_H_SOURCE = -1;
        private static final int OPENBOX_FRAME_ALIGN = 0;
        private static final int OPENBOX_TEXT_X = 47;
        private static final int OPENBOX_TEXT_Y = 154;
        private static final int OPENBOX_TEXT_W = 146;
        private static final int OPENBOX_TEXT_H = 26;
        private static final int OPENBOX_TEXT_ALIGN = 4;
        private static final int OPENBOX_TEXT_COLOR = 0x1C6C91;
        private static final int TASKTIP_FRAME_X = 14;
        private static final int TASKTIP_FRAME_Y = 147;
        private static final int TASKTIP_FRAME_W = 212;
        private static final int TASKTIP_FRAME_H_SOURCE = -1;
        private static final int TASKTIP_FRAME_ALIGN = 0;
        private static final int TASKTIP_TEXT_X = 16;
        private static final int TASKTIP_TEXT_Y = 154;
        private static final int TASKTIP_TEXT_W = 208;
        private static final int TASKTIP_TEXT_H = 26;
        private static final int TASKTIP_TEXT_ALIGN = 4;
        private static final int TASKTIP_TEXT_COLOR = 0x1C6C91;
        private final int x, y, w, h;
        private final String text;
        private final List<String> pages;
        private final boolean waitKey;
        private final boolean fullBackdrop;
        private final boolean boxBackdrop;
        private final boolean dialogBackdrop;
        private final int sourceUiKind;
        private final String speaker;
        private final int dialogMode;
        private final SpriteAnim sourceUiAnim;
        private int pageIndex;
        private int visibleChars;
        private int doneTicks;
        private int sourceTextOffset;
        private boolean sourceTextInitialized;
        private boolean readyForKey;
        private boolean disposed;

        private TextBox(int x, int y, int w, int h, String text, boolean waitKey) {
            this(x, y, w, h, text, null, waitKey, false, false, false, false, "", -1);
        }

        private TextBox(int x, int y, int w, int h, String text, boolean waitKey, boolean fullBackdrop, boolean boxBackdrop) {
            this(x, y, w, h, text, null, waitKey, fullBackdrop, boxBackdrop, false, false, "", -1);
        }

        private TextBox(int x, int y, int w, int h, String text, List<String> pages, boolean waitKey,
                        boolean fullBackdrop, boolean boxBackdrop, boolean dialogBackdrop, boolean openBoxBackdrop,
                        String speaker, int dialogMode) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.text = decodeMojibake(text);
            this.pages = normalizePages(pages);
            this.waitKey = waitKey;
            this.fullBackdrop = fullBackdrop;
            this.boxBackdrop = boxBackdrop;
            this.dialogBackdrop = dialogBackdrop;
            this.sourceUiKind = openBoxBackdrop ? SOURCE_OPENBOX : SOURCE_NONE;
            this.speaker = decodeMojibake(speaker);
            this.dialogMode = dialogMode;
            this.sourceUiAnim = sourceUiKind == SOURCE_NONE ? null : SpriteAnim.load(257);
            if (this.sourceUiAnim != null) {
                this.sourceUiAnim.setState(sourceUiKind == SOURCE_TASKTIP ? 10 : 9);
            }
        }

        private TextBox(int x, int y, int w, int h, String text, int sourceUiKind) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.text = decodeMojibake(text);
            this.pages = null;
            this.waitKey = true;
            this.fullBackdrop = false;
            this.boxBackdrop = false;
            this.dialogBackdrop = false;
            this.sourceUiKind = sourceUiKind;
            this.speaker = "";
            this.dialogMode = -1;
            this.sourceUiAnim = SpriteAnim.load(257);
            this.sourceUiAnim.setState(sourceUiKind == SOURCE_TASKTIP ? 10 : 9);
        }

        private static TextBox full(int x, int y, String text, boolean waitKey) {
            return new TextBox(x, y, W - 2 * x, H - y, text, waitKey, true, false);
        }

        private static TextBox box(int x, int y, int w, int h, String text, boolean waitKey) {
            return new TextBox(x, y, w, h, text, waitKey);
        }

        private static TextBox openBox(String text) {
            return new TextBox(OPENBOX_TEXT_X, OPENBOX_TEXT_Y, OPENBOX_TEXT_W, OPENBOX_TEXT_H, text, SOURCE_OPENBOX);
        }

        private static TextBox taskTip(String text) {
            return new TextBox(TASKTIP_TEXT_X, TASKTIP_TEXT_Y, TASKTIP_TEXT_W, TASKTIP_TEXT_H, text, SOURCE_TASKTIP);
        }

        private static TextBox dialog(FontBitmap font, String speaker, String text, int mode) {
            String tagged = "#000000" + decodeMojibake(text);
            List<String> pages = paginateTagged(font, tagged, 230, 4);
            return new TextBox(6, 264, 230, 52, tagged, pages, true,
                    false, false, true, false, decodeMojibake(speaker), mode);
        }

        private static List<String> normalizePages(List<String> source) {
            if (source == null) {
                return null;
            }
            List<String> out = new ArrayList<>(source.size());
            for (String page : source) {
                out.add(decodeMojibake(page));
            }
            return out;
        }

        private static String decodeMojibake(String text) {
            if (text == null) {
                return null;
            }
            String current = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFC);
            for (int i = 0; i < 4 && looksMojibake(current); i++) {
                String decoded = decodeMojibakeOnce(current);
                if (decoded.equals(current)) {
                    break;
                }
                current = java.text.Normalizer.normalize(decoded, java.text.Normalizer.Form.NFC);
            }
            return current;
        }

        private static String decodeMojibakeOnce(String text) {
            try {
                ByteBuffer bytes = java.nio.charset.Charset.forName("windows-1252")
                        .newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .encode(java.nio.CharBuffer.wrap(text));
                return StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(bytes)
                        .toString();
            } catch (CharacterCodingException ex) {
                return text;
            }
        }

        private static boolean looksMojibake(String text) {
            return text.indexOf('\u00c3') >= 0
                    || text.indexOf('\u00c2') >= 0
                    || text.indexOf('\u00c4') >= 0
                    || text.indexOf('\u00c5') >= 0
                    || text.indexOf('\u00c6') >= 0
                    || text.indexOf('\u00e2') >= 0
                    || text.indexOf('\u00e1') >= 0
                    || text.indexOf('\u00c1') >= 0
                    || text.indexOf('\u20ac') >= 0;
        }

        private void tick(FontBitmap font) {
            if (sourceUiKind != SOURCE_NONE && sourceUiAnim != null) {
                sourceUiAnim.tickHoldLast();
            }
            if (sourceUiKind != SOURCE_NONE) {
                tickSourceUiText(font);
                return;
            }
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

        private void tickSourceUiText(FontBitmap font) {
            int total = visibleLength(currentText());
            visibleChars = total;
            int textWidth = font.taggedWidth(currentText());
            if (!sourceTextInitialized) {
                sourceTextOffset = textWidth > w ? -w / 2 : 0;
                sourceTextInitialized = true;
            }
            if (textWidth > w) {
                int endOffset = textWidth - w;
                if (sourceTextOffset < endOffset) {
                    sourceTextOffset = Math.min(endOffset, sourceTextOffset + 2);
                    doneTicks = 0;
                    readyForKey = false;
                    return;
                }
            }
            doneTicks++;
            if (waitKey && doneTicks > 10) {
                readyForKey = true;
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
            } else if (sourceUiKind != SOURCE_NONE) {
                renderSourceUiFrame(g);
            }
            if (sourceUiKind != SOURCE_NONE) {
                renderSourceUiText(g, font);
                return;
            }
            font.drawTagged(g, currentText(), x, y, w, visibleChars);
            if (readyForKey && (doneTicks / 5) % 2 == 0) {
                if (dialogBackdrop) {
                    g.setColor(Color.BLACK);
                    int[] xs = {226, 234, 230};
                    int[] ys = {307, 307, 313};
                    g.fillPolygon(xs, ys, 3);
                } else {
                    String prompt = "Nh\u1ea5n n\u00fat 0 \u0111\u1ec3 ti\u1ebfp t\u1ee5c";
                    g.setColor(Color.WHITE);
                    int px = (W - font.width(prompt)) / 2;
                    font.drawTagged(g, prompt, px, H - 18, W, prompt.length());
                }
            }
        }

        private void renderSourceUiFrame(Graphics2D g) {
            if (sourceUiAnim == null) {
                return;
            }
            if (sourceUiKind == SOURCE_TASKTIP) {
                sourceUiAnim.drawAligned(g, TASKTIP_FRAME_X, TASKTIP_FRAME_Y, TASKTIP_FRAME_W,
                        TASKTIP_FRAME_H_SOURCE, TASKTIP_FRAME_ALIGN, 0);
            } else {
                sourceUiAnim.drawAligned(g, OPENBOX_FRAME_X, OPENBOX_FRAME_Y, OPENBOX_FRAME_W,
                        OPENBOX_FRAME_H_SOURCE, OPENBOX_FRAME_ALIGN, 0);
            }
        }

        private void renderSourceUiText(Graphics2D g, FontBitmap font) {
            Shape oldClip = g.getClip();
            g.clipRect(x, y, w, h);
            int textWidth = font.taggedWidth(currentText());
            int align = sourceUiKind == SOURCE_TASKTIP ? TASKTIP_TEXT_ALIGN : OPENBOX_TEXT_ALIGN;
            int color = sourceUiKind == SOURCE_TASKTIP ? TASKTIP_TEXT_COLOR : OPENBOX_TEXT_COLOR;
            int drawX = textWidth > w ? x - sourceTextOffset
                    : align == 4 ? x + (w - textWidth) / 2 : x;
            font.drawTaggedLine(g, currentText(), drawX, y, visibleLength(currentText()), color);
            g.setClip(oldClip);
        }

        private static String visibleTaggedPrefix(String s, int visible) {
            StringBuilder out = new StringBuilder();
            int shown = 0;
            for (int i = 0; i < s.length() && shown < visible; i++) {
                char ch = s.charAt(i);
                if (ch == '#' && i + 6 < s.length()) {
                    out.append(s, i, i + 7);
                    i += 6;
                    continue;
                }
                out.append(ch);
                shown++;
            }
            return out.toString();
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
        private BufferedImage iconImage;
        private int iconX, iconY, iconAlpha, iconStep;
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

        private void startIcon(String name, int x, int y, int step) {
            overlayType = 15;
            iconX = x;
            iconY = y;
            iconAlpha = 0;
            iconStep = Math.max(1, step);
            try {
                java.nio.file.Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig()).texDecodedPng(name);
                iconImage = Files.isRegularFile(path)
                        ? ImageIO.read(path.toFile())
                        : ImageIO.read(VqsvIntroDemo.class.getResource("/tex/" + name + ".png"));
            } catch (IOException ex) {
                iconImage = null;
            }
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
            } else if (overlayType == 15) {
                iconAlpha += iconStep;
                if (iconAlpha >= 255) {
                    iconAlpha = 255;
                    overlayDone = true;
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

