import com.vqsv.rebuild.render.MapRenderer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class VqsvIntroDemo extends JPanel {
    static final int W = 240;
    static final int H = 320;
    static final int SCALE = 2;
    private final BufferedImage frame = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
    private final Scene scene;

    public static void main(String[] args) {
        if (args.length > 0 && "--smoke".equals(args[0])) {
            int ticks = args.length > 2 ? Integer.parseInt(args[2]) : 360;
            VqsvSmokeHarness.runSmoke(args.length > 1 ? args[1] : "build_intro_demo/smoke.png", ticks);
            return;
        }
        if (args.length > 0 && "--smoke-drive".equals(args[0])) {
            String out = args.length > 1 ? args[1] : "build_intro_demo/smoke_drive.png";
            int preloadTicks = args.length > 2 ? Integer.parseInt(args[2]) : 5920;
            String route = args.length > 3 ? args[3] : "";
            int postTicks = args.length > 4 ? Integer.parseInt(args[4]) : 0;
            VqsvSmokeHarness.runSmokeDrive(out, preloadTicks, route, postTicks);
            return;
        }
        if (args.length > 0 && "--smoke-checkpoint".equals(args[0])) {
            String checkpoint = args.length > 1 ? args[1] : "room0_group2_first_dialog";
            String out = args.length > 2 ? args[2] : "build_intro_demo/smoke_checkpoint.png";
            VqsvSmokeHarness.runSmokeCheckpoint(checkpoint, out);
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
        VqsvSmokeHarness.tickSceneFastForward(scene, preloadTicks);
        if (route != null && !route.isEmpty()) {
            VqsvSmokeHarness.driveRoute(scene, route);
        }
        VqsvSmokeHarness.tickSceneFastForward(scene, postTicks);
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

    static final class Scene {
        static int tenYearsEventIndex = -1;
        final FontBitmap font = new FontBitmap();
        final Effect effect = new Effect();
        final Actor[] actors = VqsvSceneActors.makeActors();
        final List<Event> events = makeEvents();
        final List<TempSprite> tempSprites = new ArrayList<>();
        final Actor player = new Actor(-1, 0, 0, 0, 0, 1, 1);
        final WorldUi worldUi = new WorldUi();
        MapRenderer mapRenderer;
        TextBox text;
        ChoiceBox choice;
        int eventIndex = 0;
        int currentSceneId = -1;
        int currentRoomIndex = -1;
        int cameraX = 0;
        int cameraY = 0;
        int playerX = 0;
        int playerY = 0;
        boolean useMap;
        boolean key0;
        boolean keyUp;
        boolean keyDown;
        boolean keyLeft;
        boolean keyRight;
        Blocking current;
        int followActorId = -1;
        int worldEventActor = -1;
        int battleEventActor = -1;
        int[] battleEncounter = new int[0];
        boolean battleCanLose = false;
        boolean battleScriptLocksInput = false;
        int battleMode = -1;
        int battleBackgroundMode = -1;
        int battleResultIndex = -1;
        int battleBranchTarget = -1;
        int battleOverlayTicks = 0;
        String battleEnemyName = "";
        String battlePlayerName = "";
        String battleLog = "";
        String battleUiMode = "command";
        String battleMenuTitle = "";
        String battleMenuSubtitle = "";
        String battleMenuAction = "";
        String[] battleMenuNames = new String[0];
        String[] battleMenuValues = new String[0];
        int[] battleMenuIds = new int[0];
        int[] battleMenuIconIds = new int[0];
        int battleMenuIndex = 0;
        String[] battleSkillNames = new String[0];
        String[] battleSkillPpLabels = new String[0];
        int[] battleSkillIds = new int[0];
        int battleSkillIndex = 0;
        int battleSkillScroll = 0;
        String battleSkillDescription = "";
        String[] battleTargetNames = new String[0];
        int[] battleTargetSlots = new int[0];
        int battleTargetIndex = 0;
        int battleTargetCount = 0;
        boolean battleTargetPlayerSide = false;
        int battleP7Phase = 0;
        int battleP7Ticks = 0;
        int battleP7EffectAnimState = -1;
        int battleP7EffectAnimCursor = 0;
        boolean battleP7EffectOnPlayerSide = false;
        boolean battleP7AttackerPlayerSide = false;
        boolean battleP7TargetPlayerSide = false;
        boolean battleP7DamageVisible = false;
        String battleP7DamageText = "";
        boolean battleP7SpecialVisible = false;
        boolean battleP7SpecialOnPlayerSide = false;
        int battleP7SpecialType = -1;
        int battleP7SpecialAlpha = 0;
        int battleP7SpecialRed = 0;
        int battleP7SpecialGreen = 0;
        int battleP7SpecialBlue = 0;
        int battleP7SpecialDuration = 0;
        int battleP7SpecialInterval = 1;
        int battleP7SpecialTextureId = -1;
        int battleP7SpecialBlendMode = 0;
        int battleP7SpecialScrollMode = 0;
        String battleWarningTitle = "";
        String battleWarningPrompt = "";
        int battleCatchSpriteId = -1;
        int battleCatchPhase = -1;
        int battleCatchTicks = 0;
        int battleCatchAnimCursor = 0;
        int battleCatchItemId = -1;
        int battleCatchChance = 0;
        boolean battleCatchCaught = false;
        boolean battleCatchVisible = false;
        boolean battleCatchEffectVisible = false;
        int battleCatchEffectDx = 0;
        int battleCatchEffectDy = 0;
        int battleCatchEffectScale10 = 10;
        boolean battleEnemyHiddenByCatch = false;
        int battleEnemyLevel;
        int battlePlayerLevel;
        int battleEnemyVisualId;
        int battlePlayerVisualId;
        int battleEnemyElement;
        int battlePlayerElement;
        int battleEnemyPowerPercent = 100;
        int battlePlayerPowerPercent = 100;
        int battleEnemyMaxHp;
        int battleEnemyHp;
        int battlePlayerMaxHp;
        int battlePlayerHp;
        int battleTurn;
        String battleStateName = "";
        int battleCommandIndex = 0;
        int battleClickX = -1;
        int battleClickY = -1;
        int battlePlayerEnergy;
        int battlePlayerMaxEnergy = 1;
        boolean battleCaptureTutorial;
        int sourceMoney;
        int sourceBadges;
        final Map<Integer, BagItem> sourceBagItems = VqsvSourceOps.initialSourceBagItems();
        final Map<Integer, SourceSpecialReward> sourceSpecialRewards = VqsvSourceOps.initialSourceSpecialRewards();
        private final VqsvEventState eventState = new VqsvEventState();
        final List<SourcePetState> sourcePets = new ArrayList<>();
        final List<SourcePetState> sourcePetBank = new ArrayList<>();
        final List<String> sourceStateTrace = eventState.trace;
        boolean sourceGameCF = false;
        int sourcePetRefreshOps = 0;

        void press0() {
            key0 = true;
        }

        private void click(int screenX, int screenY) {
            int x = screenX / SCALE;
            int y = screenY / SCALE;
            if (battleOverlayTicks > 0) {
                battleClickX = x;
                battleClickY = y;
                key0 = true;
                return;
            }
            if (choice != null && choice.click(x, y)) {
                key0 = true;
                return;
            }
            key0 = true;
        }

        void setMoveKey(int keyCode, boolean pressed) {
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

        void tick() {
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

        void render(Graphics2D g) {
            VqsvSceneView.render(this, g);
        }

        void setCameraCenter(int cx, int cy) {
            VqsvSceneView.setCameraCenter(this, cx, cy);
        }

        void moveCameraToward(int cx, int cy, int speed) {
            VqsvSceneView.moveCameraToward(this, cx, cy, speed);
        }

        boolean cameraCenteredOn(int cx, int cy) {
            return VqsvSceneView.cameraCenteredOn(this, cx, cy);
        }

        void followActor(int actorId) {
            VqsvSceneView.followActor(this, actorId);
        }

        void stopCameraFollow() {
            VqsvSceneView.stopCameraFollow(this);
        }

        void updateCameraFollow() {
            VqsvSceneView.updateCameraFollow(this);
        }

        private static List<Event> makeEvents() {
            ArrayList<Event> e = new ArrayList<>();
            Scene0IntroScript.appendTo(e);
            Scene1Room3EntryScript.appendTo(e);
            Scene1Room0Group0Script.appendTo(e);
            Scene1Room1BunnyScript.appendTo(e);
            Scene1Room0Group2ElderScript.appendTo(e);
            Scene1Room0Group3PetScript.appendTo(e);
            Scene1Room0Group6ElderBattleScript.appendTo(e);
            return e;
        }

        int transitionCenterX;
        int transitionCenterY;
        int transitionWidth = W;
        int transitionHeight = H;
        int transitionDirection = 0;
        int nextWorldF = -1;
        int nextWorldG = -1;
        int nextWorldActor = -1;

        void prepareTransition(int centerX, int centerY, int width, int height) {
            VqsvFreeWorldRuntime.prepareTransition(this, centerX, centerY, width, height);
        }

        void prepareTransition(int centerX, int centerY, int width, int height, int direction) {
            VqsvFreeWorldRuntime.prepareTransition(this, centerX, centerY, width, height, direction);
        }

        void markWorldTransition(int worldF, int worldG, int actorIndex) {
            VqsvFreeWorldRuntime.markWorldTransition(this, worldF, worldG, actorIndex);
        }

        static int sourceTransitionRequiredDirection(int c) {
            return VqsvFreeWorldRuntime.sourceTransitionRequiredDirection(c);
        }

        boolean trySourceTransition(int actorId, int sourceC,
                                            int targetSceneId, int targetRoomIndex, int targetActorId) {
            return VqsvFreeWorldRuntime.trySourceTransition(this, actorId, sourceC,
                    targetSceneId, targetRoomIndex, targetActorId);
        }

        private void reloadBlankRoom(int cameraCenterX, int cameraCenterY) {
            useMap = false;
            mapRenderer = null;
            followActorId = -1;
            Actor[] fresh = VqsvSceneActors.makeActors();
            for (int i = 0; i < fresh.length; i++) {
                actors[i] = fresh[i];
            }
            setCameraCenter(cameraCenterX, cameraCenterY);
        }

        void reloadBlankRoomCenteredOnActor(int actorId) {
            reloadBlankRoom(0, 0);
            Actor target = actors[actorId];
            setCameraCenter(target.x, target.y);
        }

        void loadScene7Room2(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadScene7Room2(this, cameraCenterX, cameraCenterY);
        }

        void loadRoom1(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadRoom1(this, cameraCenterX, cameraCenterY);
        }

        void loadScene5Room3(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadScene5Room3(this, cameraCenterX, cameraCenterY);
        }

        void loadScene1Room3Entry(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadScene1Room3Entry(this, cameraCenterX, cameraCenterY);
        }

        void loadScene1Room0(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadScene1Room0(this, cameraCenterX, cameraCenterY);
        }

        void loadScene1Room1(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadScene1Room1(this, cameraCenterX, cameraCenterY);
        }

        void loadScene1Room2(int cameraCenterX, int cameraCenterY) {
            VqsvSceneLoaders.loadScene1Room2(this, cameraCenterX, cameraCenterY);
        }

        void spawnActorEffect(int actorId, int animation) {
            if (actorId == -1 || actorId >= 0 && actorId < actors.length && actors[actorId] != null) {
                tempSprites.add(new TempSprite(actorId, animation, 120));
            }
        }

        void op5ActorEffect(int mode, int actorId, int animation, int x, int y) {
            VqsvSourceEffects.op5ActorEffect(this, mode, actorId, animation, x, y);
        }

        void setPlayerPositionApprox(int x, int y) {
            VqsvFreeWorldRuntime.setPlayerPositionApprox(this, x, y);
        }

        void placePlayerAtTransitionActorApprox(int actorId, int tileSize) {
            VqsvFreeWorldRuntime.placePlayerAtTransitionActorApprox(this, actorId, tileSize);
        }

        void tickFreeWorldPlayer() {
            VqsvFreeWorldRuntime.tickFreeWorldPlayer(this);
        }

        boolean playerIntersectsSourceRect(int x, int y, int w, int h) {
            return VqsvFreeWorldRuntime.playerIntersectsSourceRect(this, x, y, w, h);
        }

        boolean playerIntersectsActorSourceMask(int actorId, boolean actorHitMask) {
            return VqsvFreeWorldRuntime.playerIntersectsActorSourceMask(this, actorId, actorHitMask);
        }

        boolean playerInteractsActorSourceMask(int actorId) {
            return VqsvFreeWorldRuntime.playerInteractsActorSourceMask(this, actorId);
        }

        void stopPlayerForSourceEvent() {
            VqsvFreeWorldRuntime.stopPlayerForSourceEvent(this);
        }

        int sourceEventState(int sceneId, int roomIndex, int groupIndex) {
            return eventState.sourceEventState(sceneId, roomIndex, groupIndex);
        }

        boolean sourceEventStateComplete(int sceneId, int roomIndex, int groupIndex) {
            return eventState.sourceEventStateComplete(sceneId, roomIndex, groupIndex);
        }

        boolean op15CheckEventState(int sceneId, int roomIndex, int groupIndex) {
            return eventState.op15CheckEventState(sceneId, roomIndex, groupIndex);
        }

        boolean op86CheckEventState(int sceneId, int roomIndex, int groupIndex) {
            return eventState.op86CheckEventState(sceneId, roomIndex, groupIndex);
        }

        Blocking op17Item(int mode, int itemId, int qty) {
            return VqsvSourceOps.op17Item(this, mode, itemId, qty);
        }

        void op39RefreshPets() {
            VqsvSourceEffects.op39RefreshPets(this);
        }

        void op25SetGameFlag(int arg0) {
            VqsvSourceEffects.op25SetGameFlag(this, arg0);
        }

        Blocking op9SourceEffect(String context, int... args) {
            return VqsvSourceEffects.op9SourceEffect(this, context, args);
        }

        void op67SetBattleActor(int actorId) {
            VqsvSourceEffects.op67SetBattleActor(this, actorId);
        }

        Blocking op31CurrencyReward(int mode, int currencyKind, int amount) {
            return VqsvSourceOps.op31CurrencyReward(this, mode, currencyKind, amount);
        }

        Blocking op19SpecialReward(int rewardId, int qty) {
            return VqsvSourceOps.op19SpecialReward(this, rewardId, qty);
        }

        void op56ActorVisibility(int mode, int[] ids, int[] states) {
            VqsvSourceEffects.op56ActorVisibility(this, mode, ids, states);
        }

        void op23MarkEventComplete(int worldF, int worldG, int eventId) {
            eventState.op23MarkEventComplete(worldF, worldG, eventId);
        }

        void op14CompleteEvent(int sceneId, int roomIndex, int groupIndex) {
            eventState.op14CompleteEvent(sceneId, roomIndex, groupIndex);
        }

        Blocking op10PlayerTimedAction(int dir, int speed, int duration) {
            return new Op10PlayerTimedAction(dir, speed, duration);
        }

    }

}


