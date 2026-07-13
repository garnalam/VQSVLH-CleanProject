import com.vqsv.rebuild.render.MapRenderer;
import com.vqsv.rebuild.debug.VqsvDebugLog;

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
import java.awt.event.MouseWheelEvent;
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
        if (args.length > 0 && "--smoke-suite".equals(args[0])) {
            String suite = args.length > 1 ? args[1] : "battle_quick";
            String outDir = args.length > 2 ? args[2] : "build/smoke/suites/" + suite;
            VqsvSmokeHarness.runSmokeSuite(suite, outDir);
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
        if (args.length > 0 && "--play-checkpoint".equals(args[0])) {
            String checkpoint = args.length > 1 ? args[1] : "battle_elder_command_ui";
            openWindow(checkpoint);
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
        openWindow(f, panel);
    }

    private static void openWindow(String checkpoint) {
        JFrame f = new JFrame("VQSV Liet Hoa - Battle Checkpoint - " + checkpoint);
        VqsvIntroDemo panel = new VqsvIntroDemo(checkpoint);
        openWindow(f, panel);
    }

    private static void openWindow(JFrame f, VqsvIntroDemo panel) {
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

    private VqsvIntroDemo(String checkpoint) {
        this(0, "", 0, checkpoint);
    }

    private VqsvIntroDemo(int preloadTicks, String route, int postTicks) {
        this(preloadTicks, route, postTicks, "");
    }

    private VqsvIntroDemo(int preloadTicks, String route, int postTicks, String checkpoint) {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setFocusable(true);
        scene = new Scene();
        if (checkpoint != null && !checkpoint.isEmpty()) {
            VqsvSmokeHarness.setupLiveCheckpoint(scene, checkpoint);
        } else {
            VqsvSmokeHarness.tickSceneFastForward(scene, preloadTicks);
            if (route != null && !route.isEmpty()) {
                VqsvSmokeHarness.driveRoute(scene, route);
            }
            VqsvSmokeHarness.tickSceneFastForward(scene, postTicks);
        }
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
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                scene.click(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                scene.hover(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                scene.hover(e.getX(), e.getY());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                requestFocusInWindow();
                scene.mouseWheel(e.getWheelRotation());
            }
        };
        addMouseListener(pointer);
        addMouseMotionListener(pointer);
        addMouseWheelListener(pointer);
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
        static int room1BunnyOp13EventIndex = -1;
        final FontBitmap font = new FontBitmap();
        final Effect effect = new Effect();
        final Actor[] actors = VqsvSceneActors.makeActors();
        final List<Event> events = makeEvents();
        final List<TempSprite> tempSprites = new ArrayList<>();
        final Actor player = new Actor(-1, 0, 0, 0, 0, 1, 1);
        final WorldUi worldUi = new WorldUi();
        final VqsvPanelRuntime panelRuntime = new VqsvPanelRuntime();
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
        boolean keyBack;
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
        BufferedImage battleBackgroundSnapshot;
        String battleEnemyName = "";
        String battlePlayerName = "";
        String battleLog = "";
        String battleUiMode = "command";
        int battleUiModeStartTick = 0;
        int battleLevelUpTicks = 0;
        String battleMenuTitle = "";
        String battleMenuSubtitle = "";
        String battleMenuAction = "";
        String[] battleMenuNames = new String[0];
        String[] battleMenuValues = new String[0];
        String[] battleMenuDescriptions = new String[0];
        int[] battleMenuIds = new int[0];
        int[] battleMenuIconIds = new int[0];
        int battleMenuIndex = 0;
        int battleMenuScroll = 0;
        VqsvChoiceUiView battleChoiceUi = VqsvChoiceUiView.EMPTY;
        int battleShopConfirmItemId = -1;
        int battleShopConfirmQuantity = 1;
        int battleShopConfirmTotal = 0;
        int battleShopConfirmCurrency = 0;
        VqsvBattlePetStateView[] battlePetStateRows = VqsvBattlePetStateView.EMPTY_ARRAY;
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
        boolean battleP7DamageCritical = false;
        String battleP7DebuffText = "";
        String battleP7MissText = "";
        boolean battleP7PostEffectVisible = false;
        boolean battleP7PostEffectPlayerSide = false;
        String battleP7PostEffectText = "";
        boolean battleP7SpecialVisible = false;
        boolean battleP7SpecialOnPlayerSide = false;
        boolean battleP7BaseHiddenPlayerSide = false;
        boolean battleP7BaseHiddenEnemySide = false;
        int battleP7BaseStatePlayerSide = 0;
        int battleP7BaseStateEnemySide = 0;
        int battleP7BaseCursorPlayerSide = -1;
        int battleP7BaseCursorEnemySide = -1;
        int battleP7PlayerOffsetX = 0;
        int battleP7PlayerOffsetY = 0;
        int battleP7EnemyOffsetX = 0;
        int battleP7EnemyOffsetY = 0;
        boolean battleP7DeathEffectVisible = false;
        boolean battleP7DeathEffectPlayerSide = false;
        int battleP7DeathEffectSpriteId = -1;
        int battleP7DeathEffectTick = 0;
        int battleP7DeathEffectDuration = 0;
        boolean battleGroundMarkersVisible = false;
        boolean battleActiveMarkerVisible = false;
        boolean battleActiveMarkerPlayerSide = false;
        int battleEnemyMarkerX = 144;
        int battleEnemyMarkerY = 85;
        int battlePlayerMarkerX = 36;
        int battlePlayerMarkerY = 206;
        boolean battleLVisible = false;
        boolean battleLPlayerSide = false;
        boolean battleLDrawAfter = false;
        int battleLType = -1;
        int battleLSpriteId = -1;
        int battleLFrame = 0;
        int battleLDirection = 0;
        short[] battleLRow = new short[0];
        boolean battleP7ActorEffectVisible = false;
        boolean battleP7ActorEffectOnPlayerSide = false;
        int battleP7ActorEffectSpriteId = -1;
        int battleP7ActorEffectState = 0;
        int battleP7ActorEffectCursor = 0;
        int battleAnimationTick = 0;
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
        short[] battleP7SpecialRow = new short[0];
        boolean battleActiveQueueVisible = false;
        boolean battleActiveQueuePlayerSide = false;
        int battleActiveQueueBank = -1;
        int battleActiveQueueEffectId = -1;
        int battleActiveQueueBuffId = -1;
        int battleActiveQueueSegment = -1;
        int battleActiveQueueTicks = 0;
        int battlePlayerStatusCount = 0;
        int[] battlePlayerStatusIconCells = new int[6];
        int[] battlePlayerStatusDurationCells = new int[]{145, 145, 145, 145, 145, 145};
        int battleEnemyStatusCount = 0;
        int[] battleEnemyStatusIconCells = new int[6];
        int[] battleEnemyStatusDurationCells = new int[]{145, 145, 145, 145, 145, 145};
        String battleWarningTitle = "";
        String battleWarningPrompt = "";
        VqsvMsgWarmView battleMsgWarm = VqsvMsgWarmView.EMPTY;
        VqsvOpenBoxView battleOpenBox = VqsvOpenBoxView.EMPTY;
        int battleCatchSpriteId = -1;
        int battleCatchPhase = -1;
        int battleCatchTicks = 0;
        int battleCatchAnimCursor = 0;
        int battleCatchItemId = -1;
        int battleCatchChance = 0;
        int battleCatchRoll = -1;
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
        boolean battleEnemyOwnedSpecies = false;
        int battleEnemyPowerPercent = 100;
        int battlePlayerPowerPercent = 100;
        boolean battleNpcEnemyEntryVisible = false;
        int battleNpcEnemyEntryTick = 0;
        int battleNpcEnemyEntryStep = -1;
        int battleNpcEnemyEntryFrame = -1;
        int battleNpcEnemyPlayerCount = 0;
        int battleNpcEnemyEnemyCount = 0;
        int battleNpcEnemyPlayerVisualId = -1;
        int battleNpcEnemyEnemyVisualId = -1;
        int battleEnemyMaxHp;
        int battleEnemyHp;
        int battlePlayerMaxHp;
        int battlePlayerHp;
        int battleTurn;
        String battleStateName = "";
        int battleCommandIndex = 0;
        int battleClickX = -1;
        int battleClickY = -1;
        int battleHoverX = -1;
        int battleHoverY = -1;
        int battlePlayerEnergy;
        int battlePlayerMaxEnergy = 1;
        boolean battleCaptureTutorial;
        int battleTutorialU = -1;
        int battleTutorialV = 0;
        VqsvBattleLevelUpView battleLevelUpView = VqsvBattleLevelUpView.EMPTY;
        int sourceMoney;
        int sourceBadges;
        int sourceAvoidMonsterTicks;
        int sourceAvoidMonsterElapsed;
        boolean sourceBattleLoseReviveArmed;
        int sourceBattleLoseWorldMode;
        boolean sourceEggActive;
        int sourceEggType;
        int sourceEggProgress;
        final int[] sourceEggKnownSpecies = new int[]{-1, -1, -1, -1, -1};
        final int[] sourceRideBlocked = new int[]{0, 0, 0, 0};
        int sourceRideActiveIndex = -1;
        int sourcePlayerMoveSpeed = 4;
        final Map<Integer, BagItem> sourceBagItems = VqsvSourceOps.initialSourceBagItems();
        final Map<Integer, SourceSpecialReward> sourceSpecialRewards = VqsvSourceOps.initialSourceSpecialRewards();
        final java.util.List<SourceEquipmentItem> sourceEquipmentItems =
                VqsvSourceOps.initialSourceEquipmentItems();
        final VqsvEventState eventState = new VqsvEventState();
        final List<SourcePetState> sourcePets = new ArrayList<>();
        final List<SourcePetState> sourcePetBank = new ArrayList<>();
        final byte[][] sourceGlobalState = new byte[8][2];
        final List<SourceEvolutionNotice> sourceEvolutionQueue = new ArrayList<>();
        final int[] sourceEvolutionL = new int[]{-1, -1};
        int sourceEvolutionI = 0;
        int sourceEvolutionNoticeIndex = 0;
        boolean sourceEvolutionK = false;
        int sourceEvolutionTutorialU = -1;
        boolean sourceEvolutionTutorialPending = false;
        final List<String> sourceStateTrace = eventState.trace;
        boolean sourceGameCF = false;
        int sourcePetRefreshOps = 0;
        boolean panelTitleResetRequested = false;
        boolean savePromptVisible = false;
        String savePromptMessage = "";
        String savePromptStatus = "";
        int savePromptSelected = 0;
        int savePromptClickX = -1;
        int savePromptClickY = -1;
        boolean worldPetstateVisible = false;
        boolean sourcePetSettingVisible = false;
        int sourcePetSettingIndex = 0;
        int sourcePetSettingCount = 5;
        boolean sourceSkillVisible = false;
        int sourceSkillIndex = 0;
        int sourceSkillCount = 5;
        boolean sourceItemChoiceVisible = false;
        int sourceItemChoiceIndex = 0;
        int sourceItemChoiceScroll = 0;
        int sourceItemChoiceMessageMode = 0;
        boolean sourceEquipmentChoiceVisible = false;
        int sourceEquipmentChoiceIndex = 0;
        int sourceEquipmentChoiceScroll = 0;
        int sourceEquipmentChoiceMessageMode = 0;
        boolean sourceReleaseConfirmVisible = false;
        String sourceReleaseConfirmMessage = "";
        String sourceReleaseConfirmAction = "";
        int sourceReleaseWarningMode = 0;
        int sourcePetSettingActiveWarningMode = 0;
        boolean sourceEvolveVisible = false;
        int panelBagState17ItemId = -1;
        int panelBagState17MessageMode = 0;
        int sourceEvolvePetIndex = -1;
        SourceEvolutionNotice sourceEvolveNotice;
        int[] sourceEvolveOldStats = new int[]{0, 0, 0, 0};
        int[] sourceEvolveNewStats = new int[]{0, 0, 0, 0};
        int sourceEvolveOldVisualId = -1;
        int sourceEvolveNewVisualId = -1;
        int sourceEvolvePhase = 0;
        int sourceEvolveEffectTicks = 0;
        boolean sourceEvolveSucceeded = false;
        int debugTickCounter = 0;

        void press0() {
            key0 = true;
        }

        void click(int screenX, int screenY) {
            int x = screenX / SCALE;
            int y = screenY / SCALE;
            if (battleOverlayTicks > 0) {
                battleClickX = x;
                battleClickY = y;
                key0 = true;
                return;
            }
            if (panelRuntime.visible) {
                panelRuntime.click(this, x, y);
                return;
            }
            if (savePromptVisible) {
                savePromptClickX = x;
                savePromptClickY = y;
                key0 = true;
                return;
            }
            if (sourceEvolveVisible) {
                if (x >= 190 && y >= 288) {
                    keyBack = true;
                } else {
                    key0 = true;
                }
                return;
            }
            if (sourceSkillVisible) {
                if (x >= 190 && y >= 288) {
                    keyBack = true;
                } else {
                    key0 = true;
                }
                return;
            }
            if (sourceItemChoiceVisible) {
                if (x >= 190 && y >= 288) {
                    keyBack = true;
                } else {
                    key0 = true;
                }
                return;
            }
            if (sourceEquipmentChoiceVisible) {
                if (x >= 190 && y >= 288) {
                    keyBack = true;
                } else {
                    key0 = true;
                }
                return;
            }
            if (sourceReleaseConfirmVisible) {
                if (x >= 190 && y >= 288) {
                    keyBack = true;
                } else {
                    key0 = true;
                }
                return;
            }
            if (worldPetstateVisible) {
                clickWorldPetstate(x, y);
                key0 = true;
                return;
            }
            if (worldUi.visible && useMap && canOpenSourcePanel()) {
                int worldButton = worldUi.buttonAt(x, y);
                if (worldButton == WorldUi.BUTTON_SYSTEM) {
                    panelRuntime.openGameSystemFromWorld(this);
                    return;
                }
                if (worldButton == WorldUi.BUTTON_MENU) {
                    panelRuntime.open(this);
                    sourceStateTrace.add("PORTED/PARTIAL world.ui right softkey"
                            + " source game.k P=0 key=262144 -> P=6 game.h.k gamemenu.ui open");
                    return;
                }
            }
            if (choice != null && choice.click(x, y)) {
                key0 = true;
                return;
            }
            key0 = true;
        }

        void hover(int screenX, int screenY) {
            int x = screenX / SCALE;
            int y = screenY / SCALE;
            if (battleOverlayTicks > 0) {
                battleHoverX = x;
                battleHoverY = y;
                return;
            }
            if (panelRuntime.visible) {
                panelRuntime.hover(this, x, y);
                return;
            }
            if (sourceSkillVisible) {
                hoverSourceSkill(x, y);
                return;
            }
            if (sourceItemChoiceVisible) {
                hoverSourceItemChoice(x, y);
                return;
            }
            if (sourceEquipmentChoiceVisible) {
                hoverSourceEquipmentChoice(x, y);
                return;
            }
            if (sourcePetSettingVisible) {
                hoverSourcePetSetting(x, y);
                return;
            }
            if (worldPetstateVisible) {
                hoverWorldPetstate(x, y);
            }
        }

        void mouseWheel(int wheelRotation) {
            if (wheelRotation == 0) {
                return;
            }
            int clamped = Math.max(-5, Math.min(5, wheelRotation));
            if (current instanceof SourceBattleRuntime
                    && ((SourceBattleRuntime) current).mouseWheelScrollList(this, clamped)) {
                return;
            }
            if (panelRuntime.visible) {
                panelRuntime.mouseWheel(this, clamped);
                return;
            }
            if (sourceSkillVisible) {
                mouseWheelSourceSkill(clamped);
                return;
            }
            if (sourceItemChoiceVisible) {
                mouseWheelSourceItemChoice(clamped);
                return;
            }
            if (sourceEquipmentChoiceVisible) {
                mouseWheelSourceEquipmentChoice(clamped);
                return;
            }
            if (worldPetstateVisible) {
                mouseWheelWorldPetstate(clamped);
            }
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
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_BACK_SPACE:
                    keyBack = pressed;
                    break;
                default:
                    break;
            }
        }

        void clearInputForManualCheckpoint() {
            key0 = false;
            keyUp = false;
            keyDown = false;
            keyLeft = false;
            keyRight = false;
            keyBack = false;
            battleClickX = -1;
            battleClickY = -1;
            battleHoverX = -1;
            battleHoverY = -1;
            savePromptClickX = -1;
            savePromptClickY = -1;
        }

        void skipIntroToTenYearsLaterForRelease() {
            if (tenYearsEventIndex < 0) {
                throw new IllegalStateException("Ten-years-later event index was not registered");
            }
            prepareTransition(199, 218, 240, 320, 2);
            markWorldTransition(1, 0, -1);
            loadScene1Room0(transitionCenterX, transitionCenterY);
            current = null;
            text = null;
            choice = null;
            battleOverlayTicks = 0;
            eventIndex = tenYearsEventIndex;
            sourceStateTrace.add("REBUILD_POLICY new-game skip intro -> source transition scene1 room0"
                    + " center=[199,218] group0 ten-years eventIndex=" + eventIndex);
        }

        void requestPanelTitleResetFromSourceOption() {
            panelTitleResetRequested = true;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.n option.ui confirm c=0"
                    + " reset game.i.a/b=0 game.g.y=false route game.i.a(7)->game.f.d/state8");
        }

        boolean consumePanelTitleResetRequestForRelease() {
            boolean requested = panelTitleResetRequested;
            panelTitleResetRequested = false;
            return requested;
        }

        String debugSnapshotForRelease() {
            return "scene=[" + currentSceneId + "," + currentRoomIndex + "]"
                    + " eventIndex=" + eventIndex + "/" + events.size()
                    + " current=" + (current == null ? "null" : current.getClass().getSimpleName())
                    + " player=[" + player.x + "," + player.y + "," + player.direction + "]"
                    + " camera=[" + cameraX + "," + cameraY + "]"
                    + " keys={0:" + key0 + ",U:" + keyUp + ",D:" + keyDown
                    + ",L:" + keyLeft + ",R:" + keyRight + ",B:" + keyBack + "}"
                    + " ui={text:" + (text == null ? "none" : text.text)
                    + ",choice:" + (choice != null)
                    + ",save:" + savePromptVisible
                    + ",saveStatus:" + savePromptStatus
                    + ",battleOverlay:" + battleOverlayTicks
                    + ",worldPet:" + worldPetstateVisible
                    + ",petsetting:" + sourcePetSettingVisible
                    + ",skill:" + sourceSkillVisible
                    + ",itemChoice:" + sourceItemChoiceVisible
                    + ",itemMsg:" + sourceItemChoiceMessageMode
                    + ",equipChoice:" + sourceEquipmentChoiceVisible
                    + ",equipMsg:" + sourceEquipmentChoiceMessageMode
                    + ",releaseConfirm:" + sourceReleaseConfirmVisible
                    + ",evolve:" + sourceEvolveVisible
                    + ",panel:" + panelRuntime.visible
                    + ",panelSelected:" + panelRuntime.selected + "}"
                    + " resetTitle=" + panelTitleResetRequested
                    + " states={100:" + sourceEventState(1, 0, 0)
                    + ",111:" + sourceEventState(1, 1, 1)
                    + ",110:" + sourceEventState(1, 1, 0) + "}"
                    + " op13Index=" + room1BunnyOp13EventIndex;
        }

        void tick() {
            debugTickCounter++;
            if (debugTickCounter % 30 == 0
                    || key0 || keyUp || keyDown || keyLeft || keyRight || keyBack
                    || savePromptVisible
                    || currentSceneId == 1 && currentRoomIndex == 1) {
                VqsvDebugLog.log("scene tick " + debugSnapshotForRelease());
            }
            effect.tick();
            if (battleOverlayTicks > 0 || worldPetstateVisible
                    || sourcePetSettingVisible || sourceSkillVisible
                    || sourceItemChoiceVisible || sourceEquipmentChoiceVisible
                    || sourceReleaseConfirmVisible
                    || sourceEvolveVisible) {
                battleAnimationTick++;
            }
            if ("levelup".equals(battleUiMode)) {
                battleLevelUpTicks++;
            } else {
                battleLevelUpTicks = 0;
            }
            if (text != null) {
                text.tick(font);
                if (text.disposed) {
                    text = null;
                }
            }
            if (!panelRuntime.visible && keyBack && canOpenSourcePanel()) {
                panelRuntime.open(this);
                keyBack = false;
                key0 = false;
                return;
            }
            if (panelRuntime.visible) {
                panelRuntime.tick(this);
                return;
            }
            if (sourceEvolveVisible) {
                tickSourceEvolve();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourceSkillVisible) {
                tickSourceSkill();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                keyLeft = false;
                keyRight = false;
                return;
            }
            if (sourceItemChoiceMessageMode != 0) {
                tickSourceItemChoiceMessage();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourceEquipmentChoiceMessageMode != 0) {
                tickSourceEquipmentChoiceMessage();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourceReleaseWarningMode != 0) {
                tickSourceReleaseWarningMessage();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourcePetSettingActiveWarningMode != 0) {
                tickSourcePetSettingActiveWarningMessage();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (panelBagState17MessageMode != 0) {
                tickPanelBagState17Message();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourceItemChoiceVisible) {
                tickSourceItemChoice();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourceEquipmentChoiceVisible) {
                tickSourceEquipmentChoice();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourceReleaseConfirmVisible) {
                tickSourceReleaseConfirm();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (sourcePetSettingVisible) {
                tickSourcePetSetting();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            if (worldPetstateVisible) {
                tickWorldPetstate();
                key0 = false;
                keyBack = false;
                keyUp = false;
                keyDown = false;
                return;
            }
            for (int i = tempSprites.size() - 1; i >= 0; i--) {
                if (tempSprites.get(i).tick(this)) {
                    tempSprites.remove(i);
                }
            }
            resumeRoom1BunnyOp13IfStranded();
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
            if (current == null && startSourceEvolutionTutorialBridgeIfReady()) {
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
            if (current == null && startSourceEvolutionNoticeIfReady()) {
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
            int guard = 0;
            while (current == null && eventIndex < events.size() && guard++ < 8) {
                current = events.get(eventIndex++).start(this);
                if (current != null && !current.tick(this)) {
                    break;
                }
                current = null;
            }
            key0 = false;
            keyBack = false;
            for (Actor a : actors) {
                if (a != null) {
                    a.tick();
                }
            }
            player.tick();
            updateCameraFollow();
        }

        private boolean canOpenSourcePanel() {
            if (!useMap || battleOverlayTicks > 0 || text != null || choice != null
                    || savePromptVisible || worldPetstateVisible || sourceSkillVisible
                    || sourceItemChoiceVisible || sourceEquipmentChoiceVisible
                    || sourceReleaseConfirmVisible
                    || sourceEvolveVisible) {
                return false;
            }
            return current == null
                    || current instanceof Op13FreeWorldTrigger
                    || current instanceof ActorTransitionFreeWorldTrigger
                    || current instanceof ActorInteractionFreeWorldTrigger
                    || current instanceof Room0PostGroup6FreeWorld;
        }

        private void resumeRoom1BunnyOp13IfStranded() {
            if (current != null || room1BunnyOp13EventIndex < 0
                    || currentSceneId != 1 || currentRoomIndex != 1
                    || !sourceEventStateComplete(1, 1, 1)
                    || sourceEventStateComplete(1, 1, 0)
                    || text != null || choice != null || savePromptVisible
                    || battleOverlayTicks > 0 || worldPetstateVisible
                    || sourceSkillVisible || sourceItemChoiceVisible
                    || sourceEquipmentChoiceVisible || sourceReleaseConfirmVisible
                    || sourceEvolveVisible) {
                return;
            }
            if (eventIndex != room1BunnyOp13EventIndex
                    && eventIndex != room1BunnyOp13EventIndex + 1) {
                return;
            }
            eventIndex = room1BunnyOp13EventIndex + 1;
            current = VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13.wrap(
                    new Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32));
            sourceStateTrace.add("PORTED/PARTIAL recovered stranded room1 Bunny op13 free-world blocker"
                    + " eventIndex=" + eventIndex
                    + " player=[" + player.x + "," + player.y + "]");
        }

        private boolean startSourceEvolutionNoticeIfReady() {
            if (sourceEvolutionI != 0 || sourceEvolutionQueue.isEmpty()) {
                return false;
            }
            if (text != null || choice != null || savePromptVisible || worldPetstateVisible
                    || sourcePetSettingVisible || sourceSkillVisible || sourceItemChoiceVisible
                    || sourceEquipmentChoiceVisible
                    || sourceReleaseConfirmVisible
                    || battleOverlayTicks > 0 || eventIndex < events.size()) {
                return false;
            }
            if (sourceEvolutionNoticeIndex >= sourceEvolutionQueue.size()) {
                sourceEvolutionQueue.clear();
                sourceEvolutionNoticeIndex = 0;
                sourceEvolutionI = 1;
                sourceStateTrace.add("PORTED/PARTIAL game.k evolution notice queue exhausted"
                        + " game.k.H.clear ac=0 game.k.I=1");
                return false;
            }

            SourceEvolutionNotice notice = sourceEvolutionQueue.get(sourceEvolutionNoticeIndex);
            boolean detailed = sourceEvolutionNoticeIndex == sourceEvolutionQueue.size() - 1
                    && sourceEvolutionL[0] != -1;
            String action = notice.targetKind == 3 ? VqsvText.Evolution.MUTATE : VqsvText.Evolution.EVOLVE;
            String petName = sourceEvolutionPetName(notice);
            text = detailed
                    ? TextBox.msgWarm(VqsvText.Evolution.noticeDetailed(petName, action),
                            VqsvText.Evolution.CONTINUE_PROMPT_5)
                    : TextBox.openBox(VqsvText.Evolution.noticeSimple(petName, action));
            current = new SourceEvolutionNoticeBlocking(detailed);
            sourceStateTrace.add("PORTED/PARTIAL game.k evolution notice consume ac="
                    + sourceEvolutionNoticeIndex
                    + " species=" + notice.currentSpeciesId
                    + " target=" + notice.targetSpeciesId
                    + " targetKind=" + notice.targetKind
                    + " detail=" + detailed
                    + " text=" + (detailed ? "S.a/msgwarm-shaped" : "S.b/openbox-shaped")
                    + " prompt=" + (detailed ? VqsvText.Evolution.CONTINUE_PROMPT_5 : "none")
                    + " evolve.ui=PENDING");
            sourceEvolutionNoticeIndex++;
            return true;
        }

        private boolean startSourceEvolutionTutorialBridgeIfReady() {
            if (!sourceEvolutionTutorialPending || sourceEvolutionK || sourceEvolutionL[0] == -1) {
                return false;
            }
            if (text != null || choice != null || savePromptVisible || worldPetstateVisible
                    || sourcePetSettingVisible || sourceSkillVisible || sourceItemChoiceVisible
                    || sourceEquipmentChoiceVisible
                    || sourceReleaseConfirmVisible
                    || battleOverlayTicks > 0 || eventIndex < events.size()) {
                return false;
            }
            if (!key0) {
                return false;
            }
            sourceEvolutionTutorialPending = false;
            sourceEvolutionK = true;
            sourceEvolutionTutorialU = 4;
            openWorldPetstate();
            int row = findSourceEvolutionPetIndex();
            if (row >= 0) {
                battleMenuIndex = row;
            }
            sourceStateTrace.add("PORTED/PARTIAL game.k evolution tutorial bridge"
                    + " U=4 K=true L=[" + sourceEvolutionL[0] + "," + sourceEvolutionL[1] + "]"
                    + " selectedPetIndex=" + battleMenuIndex
                    + " next=evolve.ui-on-confirm");
            return true;
        }

        private int findSourceEvolutionPetIndex() {
            for (int i = 0; i < sourcePets.size(); i++) {
                SourcePetState pet = sourcePets.get(i);
                if (pet.level == sourceEvolutionL[0] && pet.speciesId == sourceEvolutionL[1]) {
                    return i;
                }
            }
            return -1;
        }

        private String sourceEvolutionPetName(SourceEvolutionNotice notice) {
            BattleSpeciesRow row = VqsvBattleTables.instance().species(notice.currentSpeciesId);
            if (row != null && row.validForBattle()) {
                return row.name("Pet " + notice.currentSpeciesId);
            }
            return "Pet " + notice.currentSpeciesId;
        }

        private static final class SourceEvolutionNoticeBlocking implements Blocking {
            final boolean detailed;

            SourceEvolutionNoticeBlocking(boolean detailed) {
                this.detailed = detailed;
            }

            public boolean tick(Scene s) {
                if (s.text != null && s.text.readyForKey && s.key0) {
                    s.text.confirm();
                    s.key0 = false;
                    if (detailed) {
                        s.sourceEvolutionTutorialPending = true;
                    }
                    return true;
                }
                return false;
            }
        }

        void render(Graphics2D g) {
            VqsvSceneView.render(this, g);
        }

        void openWorldPetstate() {
            panelBagState17ItemId = -1;
            openWorldPetstateInternal();
        }

        void openPanelBagState17Petstate(int itemId) {
            panelBagState17ItemId = itemId;
            openWorldPetstateInternal();
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac default itemId=" + itemId
                    + " this.s=itemId o.a(17) close bag.ui -> game.h.W petstate.ui"
                    + " c=" + battleMenuIndex);
        }

        private void openWorldPetstateInternal() {
            worldPetstateVisible = true;
            battleUiModeStartTick = battleAnimationTick;
            battleMenuTitle = VqsvText.Battle.PETSTATE_TITLE;
            battleMenuSubtitle = "";
            battleMenuAction = "";
            battleMenuNames = new String[sourcePets.size()];
            battleMenuValues = new String[sourcePets.size()];
            battleMenuDescriptions = new String[0];
            battleMenuIds = new int[sourcePets.size()];
            battleMenuIconIds = new int[sourcePets.size()];
            for (int i = 0; i < sourcePets.size(); i++) {
                SourceBattleUnit unit = SourceBattleUnit.playerFromSourcePets(sourcePets.subList(i, i + 1));
                battleMenuIds[i] = i;
                battleMenuIconIds[i] = -1;
                battleMenuNames[i] = unit.name;
                battleMenuValues[i] = (unit.alive() ? "lv" + unit.level : "KO")
                        + " " + unit.hp + "/" + unit.maxHp;
            }
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                battleMenuIndex = 0;
            }
            keepWorldPetstateSelectionVisible();
            rebuildWorldPetstateRows();
            sourceStateTrace.add("PORTED/PARTIAL world petstate.ui open owner=game.k rows="
                    + java.util.Arrays.toString(battleMenuIds));
        }

        private void tickWorldPetstate() {
            if (keyUp && battleMenuIndex > 0) {
                battleMenuIndex--;
                keepWorldPetstateSelectionVisible();
                rebuildWorldPetstateRows();
                if (panelBagState17ItemId >= 0) {
                    sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z key=4100"
                            + " itemId=" + panelBagState17ItemId
                            + " selectedPet=" + battleMenuIndex);
                }
            } else if (keyDown && battleMenuIndex < battleMenuIds.length - 1) {
                battleMenuIndex++;
                keepWorldPetstateSelectionVisible();
                rebuildWorldPetstateRows();
                if (panelBagState17ItemId >= 0) {
                    sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z key=8448"
                            + " itemId=" + panelBagState17ItemId
                            + " selectedPet=" + battleMenuIndex);
                }
            }
            if (keyBack) {
                worldPetstateVisible = false;
                if (panelBagState17ItemId >= 0) {
                    int itemId = panelBagState17ItemId;
                    panelBagState17ItemId = -1;
                    panelRuntime.returnToBagFromState17Back(this, itemId);
                } else if (sourceEvolutionTutorialU != 4) {
                    panelRuntime.openMenuAt(this, 1, "game.h.X back petstate.ui -> P=6");
                }
                sourceStateTrace.add("PORTED/PARTIAL game.h.X back close petstate.ui"
                        + " selected=" + battleMenuIndex);
                return;
            }
            if (key0) {
                if (panelBagState17ItemId >= 0) {
                    confirmPanelBagState17();
                    return;
                }
                if (sourceEvolutionTutorialU == 4 && battleMenuIndex >= 0
                        && battleMenuIndex < sourcePets.size()) {
                    SourcePetState pet = sourcePets.get(battleMenuIndex);
                    if (pet.level == sourceEvolutionL[0] && pet.speciesId == sourceEvolutionL[1]) {
                        openSourceEvolveUi(battleMenuIndex);
                        return;
                    }
                    sourceStateTrace.add("PORTED/PARTIAL game.k U=4 petstate wrong selection index="
                            + battleMenuIndex + " species=" + pet.speciesId + " level=" + pet.level
                            + " expected L=[" + sourceEvolutionL[0] + "," + sourceEvolutionL[1] + "]");
                    return;
                }
                if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                    sourceStateTrace.add("PENDING panel game.h.X petstate confirm ignored empty/invalid"
                            + " index=" + battleMenuIndex + " pets=" + sourcePets.size());
                    return;
                }
                openSourcePetSettingFromPetstate();
            }
        }

        private void keepWorldPetstateSelectionVisible() {
            int maxScroll = Math.max(0, battleMenuIds.length - 6);
            if (battleMenuIndex < battleMenuScroll) {
                battleMenuScroll = battleMenuIndex;
            } else if (battleMenuIndex >= battleMenuScroll + 6) {
                battleMenuScroll = battleMenuIndex - 5;
            }
            battleMenuScroll = Math.max(0, Math.min(maxScroll, battleMenuScroll));
        }

        private void rebuildWorldPetstateRows() {
            battlePetStateRows = new VqsvBattlePetStateView[6];
            int start = Math.max(0, Math.min(battleMenuScroll,
                    Math.max(0, battleMenuIds.length - battlePetStateRows.length)));
            for (int row = 0; row < battlePetStateRows.length; row++) {
                int menuIndex = start + row;
                if (menuIndex >= battleMenuIds.length) {
                    battlePetStateRows[row] = VqsvBattlePetStateView.empty(row);
                    continue;
                }
                int petIndex = battleMenuIds[menuIndex];
                if (petIndex < 0 || petIndex >= sourcePets.size()) {
                    battlePetStateRows[row] = VqsvBattlePetStateView.empty(row);
                    continue;
                }
                battlePetStateRows[row] = VqsvBattlePetStateView.fromPet(row, petIndex,
                        sourcePets.get(petIndex), sourcePets.get(petIndex).sourceK());
            }
        }

        private void mouseWheelWorldPetstate(int steps) {
            int maxScroll = Math.max(0, battleMenuIds.length - 6);
            if (maxScroll <= 0) {
                return;
            }
            int before = battleMenuScroll;
            battleMenuScroll = Math.max(0, Math.min(maxScroll, battleMenuScroll + steps));
            if (battleMenuScroll != before) {
                rebuildWorldPetstateRows();
                sourceStateTrace.add("PC_QOL mouse wheel world petstate scrollbar"
                        + " scroll=" + battleMenuScroll
                        + " selectedPet=" + battleMenuIndex
                        + " rows=" + battleMenuIds.length);
            }
        }

        private void confirmPanelBagState17() {
            int itemId = panelBagState17ItemId;
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                beginPanelBagState17Warning(VqsvText.Battle.NO_PET_TARGET, 1,
                        "invalid selectedPet=" + battleMenuIndex);
                return;
            }
            BattleUnit target = BattleUnit.fromSourcePet(sourcePets.get(battleMenuIndex), (byte) 0);
            int validation = target.validateBattleItem(itemId);
            if (validation != -1) {
                beginPanelBagState17Warning(sourceItemChoiceWarning(validation), 1,
                        "game.b.x itemId=" + itemId
                                + " validation=" + validation
                                + " selectedPet=" + battleMenuIndex);
                return;
            }
            if (!VqsvSourceOps.sourceCanRemoveItem(this, itemId, 1)) {
                beginPanelBagState17Warning(VqsvText.Battle.NO_ITEM_COUNT, 2,
                        "q.b itemId=" + itemId + " qty=1 false selectedPet=" + battleMenuIndex);
                return;
            }
            BattleItemRow row = VqsvBattleTables.instance().item(itemId);
            int behavior = row == null ? -1 : row.behavior;
            if (behavior < 1 || behavior > 6) {
                beginPanelBagState17Warning(VqsvText.Battle.ITEM_CANNOT_USE, 1,
                        "unsupported behavior=" + behavior + " itemId=" + itemId);
                return;
            }
            BattleItemUseResult result = target.applyBattleItem(itemId);
            VqsvSourceOps.sourceRemoveItem(this, itemId, 1);
            sourcePets.get(battleMenuIndex).persistBattleUnit(target);
            openPanelBagState17Petstate(itemId);
            text = TextBox.msgWarm(VqsvText.Battle.ITEM_USED, VqsvText.Evolution.CONTINUE_PROMPT_5);
            panelBagState17MessageMode = 1;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z->bo state17 success"
                    + " game.b.w itemId=" + itemId
                    + " behavior=" + behavior
                    + " selectedPet=" + battleMenuIndex
                    + " hp=" + result.hpBefore + "->" + result.hpAfter
                    + " pp=" + result.ppBefore + "->" + result.ppAfter
                    + " debuffs=" + result.debuffsBefore + "->" + result.debuffsAfter
                    + " state6=" + result.sourceStateFlag
                    + " remaining=" + VqsvSourceOps.sourceItemCount(this, itemId)
                    + " e(c) refresh petstate.ui msgwarm.ui f=1");
        }

        private void beginPanelBagState17Warning(String message, int mode, String reason) {
            text = TextBox.msgWarm(message, VqsvText.Evolution.CONTINUE_PROMPT_5);
            panelBagState17MessageMode = mode;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z->bo state17 msgwarm.ui"
                    + " f=" + mode + " reason=" + reason + " message=" + message);
        }

        private void tickPanelBagState17Message() {
            if (text != null && text.readyForKey && key0) {
                text.confirm();
                if (text.disposed) {
                    text = null;
                    int closedMode = panelBagState17MessageMode;
                    if (closedMode == 2) {
                        int itemId = panelBagState17ItemId;
                        panelBagState17ItemId = -1;
                        panelBagState17MessageMode = 0;
                        worldPetstateVisible = false;
                        panelRuntime.returnToBagFromState17Back(this, itemId);
                        sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z->bo state17"
                                + " close msgwarm.ui+petstate.ui f=2->0 return state8/bag.ui"
                                + " itemId=" + itemId
                                + " selectedPet=" + battleMenuIndex);
                    } else {
                        sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z->bo state17"
                                + " close msgwarm.ui f=1->0 stay petstate.ui"
                                + " itemId=" + panelBagState17ItemId
                                + " selectedPet=" + battleMenuIndex);
                        panelBagState17MessageMode = 0;
                    }
                }
            }
        }

        private void openSourcePetSettingFromPetstate() {
            SourceEvolutionNotice notice = VqsvSourceEvolutionRuntime.noticeForPet(this, battleMenuIndex);
            sourcePetSettingCount = notice == null || notice.sourceR == 0 ? 5 : 6;
            sourcePetSettingIndex = Math.max(0, Math.min(sourcePetSettingIndex, sourcePetSettingCount - 1));
            sourcePetSettingVisible = true;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petstate confirm"
                    + " -> petsetting.ui f=1 c=" + sourcePetSettingIndex
                    + " rows=" + sourcePetSettingCount
                    + " evolutionR=" + (notice == null ? 0 : notice.sourceR)
                    + " selectedPet=" + battleMenuIndex);
        }

        private void tickSourcePetSetting() {
            if (keyUp && sourcePetSettingIndex > 0) {
                sourcePetSettingIndex--;
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting key=4100 c="
                        + sourcePetSettingIndex);
            } else if (keyDown && sourcePetSettingIndex < sourcePetSettingCount - 1) {
                sourcePetSettingIndex++;
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting key=8448 c="
                        + sourcePetSettingIndex);
            } else if (keyBack) {
                sourcePetSettingVisible = false;
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X back close petsetting.ui"
                        + " -> petstate.ui c=" + sourcePetSettingIndex);
            } else if (key0) {
                if (sourcePetSettingIndex == 0) {
                    openSourceItemChoiceFromPetSetting();
                    return;
                }
                if (sourcePetSettingIndex == 1) {
                    confirmSourcePetSettingActivePet();
                    return;
                }
                if (sourcePetSettingIndex == 2) {
                    openSourceEquipmentChoiceFromPetSetting();
                    return;
                }
                if (sourcePetSettingIndex == 3) {
                    openSourceReleaseConfirmFromPetSetting();
                    return;
                }
                if (sourcePetSettingIndex == 4) {
                    openSourceSkillUiFromPetSetting();
                    return;
                }
                if (sourcePetSettingIndex == 5 && sourcePetSettingCount == 6) {
                    openSourceEvolveUiFromPetSetting();
                    return;
                }
                sourceStateTrace.add("PENDING panel game.h.X petsetting confirm c="
                        + sourcePetSettingIndex
                        + " action=" + sourcePetSettingActionLabel(sourcePetSettingIndex)
                        + " subflow not mutated in petsetting shell slice");
            }
        }

        private void openSourceEvolveUiFromPetSetting() {
            SourceEvolutionNotice notice = VqsvSourceEvolutionRuntime.noticeForPet(this, battleMenuIndex);
            if (notice == null || notice.sourceR == 0) {
                sourceStateTrace.add("PENDING panel game.h.X petsetting c=5"
                        + " ignored no source R selectedPet=" + battleMenuIndex
                        + " rows=" + sourcePetSettingCount);
                return;
            }
            sourcePetSettingVisible = false;
            worldPetstateVisible = false;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=5"
                    + " o.m(); bg(); label=" + sourcePetSettingEvolutionLabel()
                    + " selectedPet=" + battleMenuIndex
                    + " species=" + notice.currentSpeciesId
                    + " target=" + notice.targetSpeciesId
                    + " R=" + notice.sourceR);
            openSourceEvolveUi(battleMenuIndex);
        }

        private void confirmSourcePetSettingActivePet() {
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                sourcePetSettingVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=1"
                        + " ignored invalid selectedPet=" + battleMenuIndex
                        + " pets=" + sourcePets.size());
                return;
            }
            SourcePetState selected = sourcePets.get(battleMenuIndex);
            if (!sourcePetLiving(selected)) {
                sourcePetSettingVisible = false;
                sourcePetSettingActiveWarningMode = 1;
                battleMenuIndex = 0;
                openWorldPetstate();
                text = TextBox.msgWarm(VqsvText.Battle.PET_CANNOT_BATTLE,
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceStateTrace.add("PORTED panel game.h.X petsetting c=1"
                        + " !q.z[b].S() -> msgwarm.ui f=2 close petsetting.ui"
                        + " b=0 selectedPetDead=true");
                return;
            }
            if (battleMenuIndex == 0) {
                sourcePetSettingVisible = false;
                sourcePetSettingActiveWarningMode = 2;
                battleMenuIndex = 0;
                openWorldPetstate();
                text = TextBox.msgWarm(VqsvText.Battle.PET_ALREADY_DEPLOYED,
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceStateTrace.add("PORTED panel game.h.X petsetting c=1"
                        + " b==0 -> msgwarm.ui f=2 close petsetting.ui"
                        + " already deployed");
                return;
            }
            int selectedIndex = battleMenuIndex;
            int selectedSpecies = selected.speciesId;
            sourcePets.remove(selectedIndex);
            sourcePets.add(0, selected);
            for (int i = 0; i < sourcePets.size(); i++) {
                sourcePets.get(i).slot = i;
            }
            battleMenuIndex = 0;
            sourcePetSettingIndex = 0;
            sourcePetSettingVisible = false;
            openWorldPetstate();
            sourceStateTrace.add("PORTED panel game.h.X petsetting c=1"
                    + " game.g.p move selected to front selectedIndex=" + selectedIndex
                    + " species=" + selectedSpecies
                    + " f=0 b=0 refresh petstate.ui close petsetting.ui");
        }

        private void tickSourcePetSettingActiveWarningMessage() {
            if (text != null && text.readyForKey && key0) {
                text.confirm();
                if (text.disposed) {
                    text = null;
                    int closedMode = sourcePetSettingActiveWarningMode;
                    sourcePetSettingActiveWarningMode = 0;
                    openWorldPetstate();
                    sourceStateTrace.add("PORTED panel game.h.X petsetting c=1 msgwarm key=131104"
                            + " close msgwarm.ui f=2->0 mode=" + closedMode
                            + " return petstate.ui selectedPet=" + battleMenuIndex);
                }
            }
        }

        private void openSourceReleaseConfirmFromPetSetting() {
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                sourcePetSettingVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=3"
                        + " release ignored invalid selectedPet=" + battleMenuIndex
                        + " pets=" + sourcePets.size());
                return;
            }
            SourcePetState selected = sourcePets.get(battleMenuIndex);
            if (sourceReleaseProtectedPet(selected)) {
                sourcePetSettingVisible = false;
                sourceReleaseConfirmVisible = false;
                sourceReleaseWarningMode = 2;
                text = TextBox.msgWarm(VqsvText.Battle.RELEASE_PROTECTED,
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=3"
                        + " protected aq.c[0][species][22]==2"
                        + " -> msgwarm.ui f=3 close petsetting.ui"
                        + " selectedPet=" + battleMenuIndex
                        + " species=" + selected.speciesId);
                return;
            }
            sourceReleaseConfirmVisible = true;
            sourceReleaseConfirmMessage = "B\u1ea1n mu\u1ed1n ph\u00f3ng sinh s\u1ee7ng v\u1eadt n\u00e0y?";
            sourceReleaseConfirmAction = "X\u00e1c nh\u1eadn";
            sourcePetSettingVisible = false;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=3"
                    + " -> msgconfirm.ui f=2 close petsetting.ui"
                    + " message=Ban muon phong sinh sung vat nay?"
                    + " selectedPet=" + battleMenuIndex
                    + " mutation=PENDING");
        }

        private void tickSourceReleaseConfirm() {
            if (keyBack) {
                sourceReleaseConfirmVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X release msgconfirm key=786432"
                        + " close msgconfirm.ui f=2->0 return petstate.ui selectedPet=" + battleMenuIndex);
            } else if (key0) {
                confirmSourceReleasePet();
            }
        }

        private void confirmSourceReleasePet() {
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                sourceReleaseConfirmVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X release msgconfirm key=131072"
                        + " ignored invalid selectedPet=" + battleMenuIndex
                        + " pets=" + sourcePets.size());
                return;
            }
            if (!sourceHasOtherLivingPet(battleMenuIndex)) {
                sourceReleaseConfirmVisible = false;
                sourceReleaseWarningMode = 1;
                text = TextBox.msgWarm(VqsvText.Battle.RELEASE_LAST_ALIVE,
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X release msgconfirm key=131072"
                        + " q.o(selectedPet)=false -> msgwarm.ui f=3 close msgconfirm.ui"
                        + " selectedPet=" + battleMenuIndex
                        + " pets=" + sourcePets.size());
                return;
            }
            SourcePetState removed = sourcePets.get(battleMenuIndex);
            int removedSpecies = removed.speciesId;
            int equipmentId = sourcePetEquipmentId(removed);
            if (equipmentId >= 0) {
                sourceUnequipEquipment(equipmentId, battleMenuIndex);
            } else {
                setSourcePetEquipmentId(removed, -1);
            }
            sourcePets.remove(battleMenuIndex);
            for (int i = 0; i < sourcePets.size(); i++) {
                sourcePets.get(i).slot = i;
            }
            if (battleMenuIndex >= sourcePets.size()) {
                battleMenuIndex = Math.max(0, sourcePets.size() - 1);
            }
            sourceReleaseConfirmVisible = false;
            openWorldPetstate();
            sourceStateTrace.add("PORTED panel game.h.X release msgconfirm key=131072"
                    + " q.o(selectedPet)=true game.g.l equipmentId=" + equipmentId
                    + " game.g.m remove selected species=" + removedSpecies
                    + " refresh petstate.ui close msgconfirm.ui"
                    + " selectedPet=" + battleMenuIndex
                    + " pets=" + sourcePets.size());
        }

        private void tickSourceReleaseWarningMessage() {
            if (text != null && text.readyForKey && key0) {
                text.confirm();
                if (text.disposed) {
                    text = null;
                    int closedMode = sourceReleaseWarningMode;
                    sourceReleaseWarningMode = 0;
                    openWorldPetstate();
                    sourceStateTrace.add("PORTED/PARTIAL panel game.h.X release msgwarm key=131104"
                            + " close msgwarm.ui f=3->0 mode=" + closedMode
                            + " return petstate.ui selectedPet=" + battleMenuIndex);
                }
            }
        }

        private boolean sourceHasOtherLivingPet(int selectedIndex) {
            for (int i = 0; i < sourcePets.size(); i++) {
                if (i == selectedIndex) {
                    continue;
                }
                if (sourcePetLiving(sourcePets.get(i))) {
                    return true;
                }
            }
            return false;
        }

        private boolean sourcePetLiving(SourcePetState pet) {
            if (pet == null) {
                return false;
            }
            if (pet.sourcePayload == null || pet.sourcePayload.length <= 6) {
                pet.sourcePayload = pet.toSourcePayload();
            }
            return pet.sourcePayload.length > 6 && pet.sourcePayload[6] > 0;
        }

        private boolean sourceReleaseProtectedPet(SourcePetState pet) {
            if (pet == null) {
                return false;
            }
            short[] row = VqsvBattleTables.instance().row(0, pet.speciesId);
            return row != null && row.length > 22 && row[22] == 2;
        }

        private void openSourceEquipmentChoiceFromPetSetting() {
            sourceEquipmentChoiceIndex = clampSourceChoiceIndex(sourceEquipmentChoiceIndex,
                    sourceEquipmentChoiceSize());
            sourceEquipmentChoiceScroll = clampSourceChoiceScroll(sourceEquipmentChoiceScroll,
                    sourceEquipmentChoiceIndex, sourceEquipmentChoiceSize());
            sourceEquipmentChoiceVisible = true;
            sourcePetSettingVisible = false;
            worldPetstateVisible = false;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=2"
                    + " -> choice.ui f=2 r=0 close petsetting.ui+petstate.ui"
                    + " title=Vat pham trang suc subtitle=Trang thai q.L-mapped rows="
                    + sourceEquipmentChoiceSize()
                    + " selectedPet=" + battleMenuIndex
                    + " petEquipment=" + selectedSourceEquipmentId());
        }

        private void tickSourceEquipmentChoice() {
            int size = sourceEquipmentChoiceSize();
            if (keyUp && size > 0) {
                sourceEquipmentChoiceIndex--;
                if (sourceEquipmentChoiceIndex < 0) {
                    sourceEquipmentChoiceIndex = size - 1;
                    sourceEquipmentChoiceScroll = Math.max(0, size - 5);
                } else if (sourceEquipmentChoiceIndex < sourceEquipmentChoiceScroll) {
                    sourceEquipmentChoiceScroll = sourceEquipmentChoiceIndex;
                }
                sourceEquipmentChoiceScroll = clampSourceChoiceScroll(sourceEquipmentChoiceScroll,
                        sourceEquipmentChoiceIndex, size);
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment key=4100"
                        + " h=" + sourceEquipmentChoiceIndex + " w=" + sourceEquipmentChoiceScroll);
            } else if (keyDown && size > 0) {
                sourceEquipmentChoiceIndex++;
                if (sourceEquipmentChoiceIndex >= size) {
                    sourceEquipmentChoiceIndex = 0;
                    sourceEquipmentChoiceScroll = 0;
                } else if (sourceEquipmentChoiceIndex >= sourceEquipmentChoiceScroll + 5) {
                    sourceEquipmentChoiceScroll++;
                }
                sourceEquipmentChoiceScroll = clampSourceChoiceScroll(sourceEquipmentChoiceScroll,
                        sourceEquipmentChoiceIndex, size);
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment key=8448"
                        + " h=" + sourceEquipmentChoiceIndex + " w=" + sourceEquipmentChoiceScroll);
            } else if (keyBack) {
                sourceEquipmentChoiceVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment key=262144"
                        + " e(b) refresh petstate.ui close choice.ui selectedPet=" + battleMenuIndex);
            } else if (key0) {
                confirmSourceEquipmentChoice();
            }
        }

        private void confirmSourceEquipmentChoice() {
            int equipmentId = sourceEquipmentChoiceItemIdAt(sourceEquipmentChoiceIndex);
            if (equipmentId < 0 || battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment confirm ignored"
                        + " q.L emptyOrNoPet h=" + sourceEquipmentChoiceIndex
                        + " selectedPet=" + battleMenuIndex);
                return;
            }
            if (selectedSourceEquipmentId() == equipmentId) {
                sourceUnequipEquipment(equipmentId, battleMenuIndex);
                text = TextBox.msgWarm(VqsvText.Battle.EQUIPMENT_REMOVED,
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceEquipmentChoiceMessageMode = 3;
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment success"
                        + " game.g.l itemId=" + equipmentId
                        + " pet=" + battleMenuIndex
                        + " message=Thanh cong do xuong"
                        + " f=3 keep choice.ui until confirm");
                return;
            }
            int previousPet = sourcePetIndexWearingEquipment(equipmentId);
            int oldEquipment = selectedSourceEquipmentId();
            sourceEquipEquipment(equipmentId, battleMenuIndex);
            text = TextBox.msgWarm(VqsvText.Battle.EQUIPMENT_WORN,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            sourceEquipmentChoiceMessageMode = 3;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment success"
                    + " game.g.f itemId=" + equipmentId
                    + " pet=" + battleMenuIndex
                    + " oldEquipment=" + oldEquipment
                    + " previousPet=" + previousPet
                    + " message=Thanh cong mang theo"
                    + " f=3 keep choice.ui until confirm");
        }

        private void tickSourceEquipmentChoiceMessage() {
            if (text != null && text.readyForKey && key0) {
                text.confirm();
                if (text.disposed) {
                    text = null;
                    sourceEquipmentChoiceVisible = false;
                    openWorldPetstate();
                    sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui equipment"
                            + " close msgwarm.ui f=3->2 refresh petstate.ui close choice.ui"
                            + " selectedPet=" + battleMenuIndex);
                    sourceEquipmentChoiceMessageMode = 0;
                }
            }
        }

        private void sourceUnequipEquipment(int equipmentId, int petIndex) {
            SourceEquipmentItem row = sourceEquipmentRow(equipmentId);
            if (row != null) {
                row.equippedFlag = false;
            }
            if (petIndex >= 0 && petIndex < sourcePets.size()) {
                setSourcePetEquipmentId(sourcePets.get(petIndex), -1);
            }
        }

        private void sourceEquipEquipment(int equipmentId, int petIndex) {
            if (petIndex < 0 || petIndex >= sourcePets.size()) {
                return;
            }
            int oldEquipment = selectedSourceEquipmentId();
            if (oldEquipment >= 0) {
                SourceEquipmentItem oldRow = sourceEquipmentRow(oldEquipment);
                if (oldRow != null) {
                    oldRow.equippedFlag = false;
                }
                setSourcePetEquipmentId(sourcePets.get(petIndex), -1);
            }
            int previousPet = sourcePetIndexWearingEquipment(equipmentId);
            if (previousPet >= 0 && previousPet < sourcePets.size()) {
                setSourcePetEquipmentId(sourcePets.get(previousPet), -1);
            }
            SourceEquipmentItem target = sourceEquipmentRow(equipmentId);
            if (target != null) {
                target.equippedFlag = true;
            }
            setSourcePetEquipmentId(sourcePets.get(petIndex), equipmentId);
        }

        private SourceEquipmentItem sourceEquipmentRow(int equipmentId) {
            for (SourceEquipmentItem item : sourceEquipmentItems) {
                if (item.id == equipmentId) {
                    return item;
                }
            }
            return null;
        }

        private int sourcePetIndexWearingEquipment(int equipmentId) {
            for (int i = 0; i < sourcePets.size(); i++) {
                SourcePetState pet = sourcePets.get(i);
                if (sourcePetEquipmentId(pet) == equipmentId) {
                    return i;
                }
            }
            return -1;
        }

        private void setSourcePetEquipmentId(SourcePetState pet, int equipmentId) {
            if (pet.sourcePayload == null || pet.sourcePayload.length <= 2) {
                pet.sourcePayload = pet.toSourcePayload();
            }
            if (pet.sourcePayload.length > 2) {
                pet.sourcePayload[2] = equipmentId;
            }
        }

        private int sourcePetEquipmentId(SourcePetState pet) {
            if (pet == null) {
                return -1;
            }
            if (pet.sourcePayload == null || pet.sourcePayload.length <= 2) {
                pet.sourcePayload = pet.toSourcePayload();
            }
            return pet.sourcePayload.length > 2 ? pet.sourcePayload[2] : -1;
        }

        private void openSourceItemChoiceFromPetSetting() {
            sourceItemChoiceIndex = clampSourceChoiceIndex(sourceItemChoiceIndex, sourceItemChoiceSize());
            sourceItemChoiceScroll = clampSourceChoiceScroll(sourceItemChoiceScroll, sourceItemChoiceIndex,
                    sourceItemChoiceSize());
            sourceItemChoiceVisible = true;
            sourcePetSettingVisible = false;
            worldPetstateVisible = false;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=0"
                    + " -> choice.ui f=2 r=0 close petsetting.ui+petstate.ui"
                    + " title=Dao cu subtitle=So luong q.J-mapped rows=" + sourceItemChoiceSize()
                    + " selectedPet=" + battleMenuIndex);
        }

        private void tickSourceItemChoice() {
            int size = sourceItemChoiceSize();
            if (keyUp && size > 0) {
                sourceItemChoiceIndex--;
                if (sourceItemChoiceIndex < 0) {
                    sourceItemChoiceIndex = size - 1;
                    sourceItemChoiceScroll = Math.max(0, size - 5);
                } else if (sourceItemChoiceIndex < sourceItemChoiceScroll) {
                    sourceItemChoiceScroll = sourceItemChoiceIndex;
                }
                sourceItemChoiceScroll = clampSourceChoiceScroll(sourceItemChoiceScroll,
                        sourceItemChoiceIndex, size);
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item key=4100"
                        + " r=" + sourceItemChoiceIndex + " w=" + sourceItemChoiceScroll);
            } else if (keyDown && size > 0) {
                sourceItemChoiceIndex++;
                if (sourceItemChoiceIndex >= size) {
                    sourceItemChoiceIndex = 0;
                    sourceItemChoiceScroll = 0;
                } else if (sourceItemChoiceIndex >= sourceItemChoiceScroll + 5) {
                    sourceItemChoiceScroll++;
                }
                sourceItemChoiceScroll = clampSourceChoiceScroll(sourceItemChoiceScroll,
                        sourceItemChoiceIndex, size);
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item key=8448"
                        + " r=" + sourceItemChoiceIndex + " w=" + sourceItemChoiceScroll);
            } else if (keyBack) {
                sourceItemChoiceVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item key=262144"
                        + " e(b) refresh petstate.ui close choice.ui selectedPet=" + battleMenuIndex);
            } else if (key0) {
                confirmSourceItemChoice();
            }
        }

        private void confirmSourceItemChoice() {
            int itemId = sourceItemChoiceItemIdAt(sourceItemChoiceIndex);
            if (itemId < 0) {
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item confirm ignored"
                        + " q.J empty selectedPet=" + battleMenuIndex);
                return;
            }
            BagItem item = sourceBagItems.get(itemId);
            if (item == null || item.count <= 0) {
                beginSourceItemChoiceWarning(VqsvText.Battle.NO_ITEM_COUNT, 3,
                        "missing-count itemId=" + itemId);
                return;
            }
            if (itemId == 13 || itemId == 14) {
                beginSourceItemChoiceWarning(VqsvText.Battle.ITEM_CANNOT_USE, 3,
                        "source forbidden itemId=" + itemId);
                return;
            }
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                beginSourceItemChoiceWarning(VqsvText.Battle.NO_PET_TARGET, 3,
                        "invalid selectedPet=" + battleMenuIndex);
                return;
            }
            BattleUnit target = BattleUnit.fromSourcePet(sourcePets.get(battleMenuIndex), (byte) 0);
            int validation = target.validateBattleItem(itemId);
            if (validation != -1) {
                beginSourceItemChoiceWarning(sourceItemChoiceWarning(validation), 3,
                        "game.b.x itemId=" + itemId
                                + " validation=" + validation
                                + " selectedPet=" + battleMenuIndex);
                return;
            }
            BattleItemRow row = VqsvBattleTables.instance().item(itemId);
            int behavior = row == null ? -1 : row.behavior;
            if (behavior < 1 || behavior > 6) {
                beginSourceItemChoiceWarning(VqsvText.Battle.ITEM_CANNOT_USE, 3,
                        "unsupported behavior=" + behavior + " itemId=" + itemId);
                return;
            }
            BattleItemUseResult result = target.applyBattleItem(itemId);
            VqsvSourceOps.sourceRemoveItem(this, itemId, 1);
            sourcePets.get(battleMenuIndex).persistBattleUnit(target);
            sourceItemChoiceVisible = false;
            openWorldPetstate();
            text = TextBox.msgWarm(VqsvText.Battle.ITEM_USED, VqsvText.Evolution.CONTINUE_PROMPT_5);
            sourceItemChoiceMessageMode = 4;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item success"
                    + " game.b.w itemId=" + itemId
                    + " behavior=" + behavior
                    + " selectedPet=" + battleMenuIndex
                    + " hp=" + result.hpBefore + "->" + result.hpAfter
                    + " pp=" + result.ppBefore + "->" + result.ppAfter
                    + " debuffs=" + result.debuffsBefore + "->" + result.debuffsAfter
                    + " state6=" + result.sourceStateFlag
                    + " remaining=" + VqsvSourceOps.sourceItemCount(this, itemId)
                    + " close choice.ui refresh petstate.ui f=4");
        }

        private void beginSourceItemChoiceWarning(String message, int mode, String reason) {
            text = TextBox.msgWarm(message, VqsvText.Evolution.CONTINUE_PROMPT_5);
            sourceItemChoiceMessageMode = mode;
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item msgwarm"
                    + " f=" + mode + " reason=" + reason + " message=" + message);
        }

        private void tickSourceItemChoiceMessage() {
            if (text != null && text.readyForKey && key0) {
                text.confirm();
                if (text.disposed) {
                    text = null;
                    if (sourceItemChoiceMessageMode == 3) {
                        sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item"
                                + " close msgwarm.ui f=3->2 return choice.ui");
                    } else if (sourceItemChoiceMessageMode == 4) {
                        sourceStateTrace.add("PORTED/PARTIAL panel game.h.X choice.ui item"
                                + " close msgwarm.ui f=4->0 stay petstate.ui");
                    }
                    sourceItemChoiceMessageMode = 0;
                }
            }
        }

        private static String sourceItemChoiceWarning(int validation) {
            switch (validation) {
                case 0:
                    return VqsvText.Battle.ITEM_TARGET_DEAD_STRICT;
                case 1:
                    return VqsvText.Battle.NO_PET_TARGET;
                case 2:
                    return VqsvText.Battle.ITEM_HP_FULL;
                case 3:
                    return VqsvText.Battle.ITEM_PP_FULL;
                case 4:
                    return VqsvText.Battle.ITEM_NO_DEBUFF;
                case 5:
                    return VqsvText.Battle.ITEM_ALREADY_EXCITED;
                case 7:
                    return VqsvText.Battle.ITEM_HP_PP_FULL;
                case 8:
                    return VqsvText.Battle.ITEM_TARGET_DEAD;
                default:
                    return VqsvText.Battle.ITEM_CANNOT_USE;
            }
        }

        VqsvChoiceUiView sourceItemChoiceView() {
            java.util.List<String> names = new ArrayList<>();
            java.util.List<String> values = new ArrayList<>();
            java.util.List<String> descriptions = new ArrayList<>();
            java.util.List<Integer> ids = new ArrayList<>();
            java.util.List<Integer> icons = new ArrayList<>();
            for (BagItem item : sourceItemChoiceRows()) {
                SourceItem source = VqsvSourceOps.sourceItem(item.id);
                names.add(source.name);
                values.add(String.valueOf(item.count));
                descriptions.add(source.description);
                ids.add(item.id);
                icons.add(source.iconCell);
            }
            return VqsvChoiceUiView.battle("\u0110\u1ea1o c\u1ee5", "S\u1ed1 l\u01b0\u1ee3ng",
                    "S\u1eed d\u1ee5ng", names, values, descriptions, ids, icons,
                    sourceItemChoiceIndex, sourceItemChoiceScroll)
                    .withAlternateSoftkeys("S\u1eed d\u1ee5ng")
                    .withSourceCursor(sourceItemChoiceIndex, sourceItemChoiceScroll);
        }

        int sourceItemChoiceSize() {
            return sourceItemChoiceRows().size();
        }

        int sourceItemChoiceItemIdAt(int index) {
            java.util.List<BagItem> rows = sourceItemChoiceRows();
            if (index < 0 || index >= rows.size()) {
                return -1;
            }
            return rows.get(index).id;
        }

        private java.util.List<BagItem> sourceItemChoiceRows() {
            java.util.List<BagItem> rows = new ArrayList<>();
            for (BagItem item : sourceBagItems.values()) {
                if (item.bagChannel == 0 || item.count <= 0) {
                    continue;
                }
                rows.add(item);
            }
            rows.sort(java.util.Comparator.comparingInt(item -> item.id));
            return rows;
        }

        VqsvChoiceUiView sourceEquipmentChoiceView() {
            java.util.List<String> names = new ArrayList<>();
            java.util.List<String> values = new ArrayList<>();
            java.util.List<String> descriptions = new ArrayList<>();
            java.util.List<Integer> ids = new ArrayList<>();
            java.util.List<Integer> icons = new ArrayList<>();
            for (SourceEquipmentItem item : sourceEquipmentChoiceRows()) {
                names.add(VqsvSourceOps.sourceEquipmentName(item.id));
                values.add(sourceEquipmentStatusText(item));
                descriptions.add(VqsvSourceOps.sourceEquipmentDescription(item.id));
                ids.add(item.id);
                icons.add(VqsvSourceOps.sourceEquipmentIconCell(item.id));
            }
            return VqsvChoiceUiView.battle("V\u1eadt ph\u1ea9m trang s\u1ee9c", "Tr\u1ea1ng th\u00e1i",
                    sourceEquipmentActionText(), names, values, descriptions, ids, icons,
                    sourceEquipmentChoiceIndex, sourceEquipmentChoiceScroll)
                    .withAlternateSoftkeys(sourceEquipmentActionText())
                    .withSourceCursor(sourceEquipmentChoiceIndex, sourceEquipmentChoiceScroll);
        }

        int sourceEquipmentChoiceSize() {
            return sourceEquipmentChoiceRows().size();
        }

        int sourceEquipmentChoiceItemIdAt(int index) {
            java.util.List<SourceEquipmentItem> rows = sourceEquipmentChoiceRows();
            if (index < 0 || index >= rows.size()) {
                return -1;
            }
            return rows.get(index).id;
        }

        private java.util.List<SourceEquipmentItem> sourceEquipmentChoiceRows() {
            return new ArrayList<>(sourceEquipmentItems);
        }

        private String sourceEquipmentStatusText(SourceEquipmentItem item) {
            if (item == null) {
                return "";
            }
            if (selectedSourceEquipmentId() == item.id) {
                return "\u0110\u00e3 mang theo";
            }
            return item.equippedFlag ? "B\u1ecb mang theo" : "";
        }

        private String sourceEquipmentActionText() {
            int selectedId = sourceEquipmentChoiceItemIdAt(sourceEquipmentChoiceIndex);
            return selectedId >= 0 && selectedSourceEquipmentId() == selectedId
                    ? "D\u1ee1 xu\u1ed1ng" : "Mang theo";
        }

        int selectedSourceEquipmentId() {
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                return -1;
            }
            return sourcePetEquipmentId(sourcePets.get(battleMenuIndex));
        }

        private static int clampSourceChoiceIndex(int index, int size) {
            if (size <= 0) {
                return 0;
            }
            return Math.max(0, Math.min(size - 1, index));
        }

        private static int clampSourceChoiceScroll(int scroll, int selected, int size) {
            int max = Math.max(0, size - 5);
            int result = Math.max(0, Math.min(max, scroll));
            if (selected < result) {
                result = selected;
            } else if (selected >= result + 5) {
                result = selected - 4;
            }
            return Math.max(0, Math.min(max, result));
        }

        private void mouseWheelSourceItemChoice(int steps) {
            int size = sourceItemChoiceSize();
            int maxScroll = Math.max(0, size - 5);
            if (maxScroll <= 0) {
                return;
            }
            sourceItemChoiceScroll = Math.max(0, Math.min(maxScroll, sourceItemChoiceScroll + steps));
            sourceItemChoiceIndex = clampIndexIntoVisible(sourceItemChoiceIndex, sourceItemChoiceScroll, size, 5);
            sourceStateTrace.add("PC_QOL mouse wheel choice.ui item scrollbar"
                    + " r=" + sourceItemChoiceIndex + " w=" + sourceItemChoiceScroll);
        }

        private void mouseWheelSourceEquipmentChoice(int steps) {
            int size = sourceEquipmentChoiceSize();
            int maxScroll = Math.max(0, size - 5);
            if (maxScroll <= 0) {
                return;
            }
            sourceEquipmentChoiceScroll = Math.max(0,
                    Math.min(maxScroll, sourceEquipmentChoiceScroll + steps));
            sourceEquipmentChoiceIndex = clampIndexIntoVisible(sourceEquipmentChoiceIndex,
                    sourceEquipmentChoiceScroll, size, 5);
            sourceStateTrace.add("PC_QOL mouse wheel choice.ui equipment scrollbar"
                    + " h=" + sourceEquipmentChoiceIndex + " w=" + sourceEquipmentChoiceScroll);
        }

        private void mouseWheelSourceSkill(int steps) {
            int maxScroll = Math.max(0, sourceSkillCount - 5);
            if (maxScroll <= 0) {
                return;
            }
            int before = sourceSkillIndex;
            sourceSkillIndex = Math.max(0, Math.min(sourceSkillCount - 1, sourceSkillIndex + steps));
            if (sourceSkillIndex != before) {
                sourceStateTrace.add("PC_QOL mouse wheel skill.ui scrollbar"
                        + " index=" + sourceSkillIndex
                        + " rows=" + sourceSkillCount);
            }
        }

        private static int clampIndexIntoVisible(int index, int scroll, int size, int visibleRows) {
            if (size <= 0) {
                return 0;
            }
            int result = Math.max(0, Math.min(size - 1, index));
            if (result < scroll) {
                result = scroll;
            } else if (result >= scroll + visibleRows) {
                result = scroll + visibleRows - 1;
            }
            return Math.max(0, Math.min(size - 1, result));
        }

        private void openSourceSkillUiFromPetSetting() {
            sourceSkillIndex = 0;
            sourceSkillCount = 5;
            sourceSkillVisible = true;
            sourcePetSettingVisible = false;
            worldPetstateVisible = false;
            SourcePetState pet = selectedSourceSkillPet();
            sourceStateTrace.add("PORTED/PARTIAL panel game.h.X petsetting c=4"
                    + " -> skill.ui f=2 r=0 close petsetting.ui+petstate.ui"
                    + " selectedPet=" + battleMenuIndex
                    + " species=" + (pet == null ? -1 : pet.speciesId)
                    + " skills=" + java.util.Arrays.toString(pet == null ? new int[0] : pet.skillIds)
                    + " uiRows=5 sourcePetSlots=" + (pet == null ? 0 : pet.skillIds.length));
        }

        private void tickSourceSkill() {
            if ((keyUp || keyLeft) && sourceSkillIndex > 0) {
                sourceSkillIndex--;
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X skill.ui p.a.b(prev) r="
                        + sourceSkillIndex + " desc=" + !sourceSkillDescription().isEmpty());
            } else if ((keyDown || keyRight) && sourceSkillIndex < sourceSkillCount - 1) {
                sourceSkillIndex++;
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X skill.ui p.a.b(next) r="
                        + sourceSkillIndex + " desc=" + !sourceSkillDescription().isEmpty());
            } else if (keyBack) {
                sourceSkillVisible = false;
                openWorldPetstate();
                sourceStateTrace.add("PORTED/PARTIAL panel game.h.X skill.ui key=262144"
                        + " e(b) refresh petstate.ui close skill.ui selectedPet=" + battleMenuIndex);
            } else if (key0) {
                sourceStateTrace.add("PENDING panel game.h.X skill.ui confirm"
                        + " no source mutation in read-only slice r=" + sourceSkillIndex
                        + " skillId=" + sourceSkillIdAt(sourceSkillIndex));
            }
        }

        SourcePetState selectedSourceSkillPet() {
            if (battleMenuIndex < 0 || battleMenuIndex >= sourcePets.size()) {
                return null;
            }
            return sourcePets.get(battleMenuIndex);
        }

        String sourceSkillPetName() {
            SourcePetState pet = selectedSourceSkillPet();
            if (pet == null) {
                return "";
            }
            BattleSpeciesRow row = VqsvBattleTables.instance().species(pet.speciesId);
            return row == null ? "Pet " + pet.speciesId : row.name("Pet " + pet.speciesId);
        }

        int sourceSkillPetLevel() {
            SourcePetState pet = selectedSourceSkillPet();
            return pet == null ? 0 : pet.level;
        }

        int sourceSkillPetVisualId() {
            SourcePetState pet = selectedSourceSkillPet();
            if (pet == null) {
                return -1;
            }
            BattleSpeciesRow row = VqsvBattleTables.instance().species(pet.speciesId);
            return row == null ? -1 : row.spriteId;
        }

        int sourceSkillIdAt(int slot) {
            SourcePetState pet = selectedSourceSkillPet();
            if (pet == null || slot < 0 || slot >= pet.skillIds.length) {
                return -1;
            }
            return pet.skillIds[slot];
        }

        String sourceSkillNameAt(int slot) {
            int skillId = sourceSkillIdAt(slot);
            if (skillId == -1) {
                return "";
            }
            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            return row == null ? "Skill " + skillId : row.name("Skill " + skillId);
        }

        String sourceSkillDescription() {
            int skillId = sourceSkillIdAt(sourceSkillIndex);
            if (skillId == -1) {
                return "";
            }
            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            return row == null ? "" : row.description("");
        }

        String sourcePetSettingEvolutionLabel() {
            SourceEvolutionNotice notice = VqsvSourceEvolutionRuntime.noticeForPet(this, battleMenuIndex);
            if (notice == null || notice.sourceR == 0) {
                return "";
            }
            return notice.sourceR == 2 ? "D\u1ecb ho\u00e1" : "Ti\u1ebfn h\u00f3a";
        }

        String sourcePetSettingActionLabel(int index) {
            switch (index) {
                case 0:
                    return "\u0110\u1ea1o c\u1ee5";
                case 1:
                    return "Chi\u1ebfn \u0111\u1ea5u";
                case 2:
                    return "V\u1eadt ph\u1ea9m trang s\u1ee9c";
                case 3:
                    return "Ph\u00f3ng sinh";
                case 4:
                    return "K\u1ef9 n\u0103ng";
                case 5:
                    return sourcePetSettingEvolutionLabel();
                default:
                    return "";
            }
        }

        private void openSourceEvolveUi(int petIndex) {
            sourceEvolveNotice = VqsvSourceEvolutionRuntime.noticeForPet(this, petIndex);
            sourceEvolveVisible = true;
            sourceEvolvePetIndex = petIndex;
            sourceEvolvePhase = 0;
            sourceEvolveEffectTicks = 0;
            sourceEvolveSucceeded = false;
            worldPetstateVisible = false;
            refreshSourceEvolvePanelFromPet();
            sourceStateTrace.add("PORTED/PARTIAL game.h.bg evolve.ui open petIndex=" + petIndex
                    + " notice=" + (sourceEvolveNotice == null ? "none"
                    : sourceEvolveNotice.currentSpeciesId + "->" + sourceEvolveNotice.targetSpeciesId)
                    + " widget=10/38/40/45/46 stats=19..22/31..34");
        }

        private void tickSourceEvolve() {
            if (sourceEvolvePhase == 1) {
                sourceEvolveEffectTicks++;
                if (sourceEvolveEffectTicks >= sourceEvolveType10Duration()) {
                    SourceEvolutionNotice completedNotice = sourceEvolveNotice;
                    VqsvSourceEvolutionRuntime.mutatePet(this, sourceEvolvePetIndex, completedNotice);
                    String action = completedNotice.targetKind == 3
                            ? VqsvText.Evolution.MUTATE : VqsvText.Evolution.EVOLVE;
                    String targetName = sourceEvolutionTargetName(completedNotice);
                    refreshSourceEvolvePanelFromPet();
                    text = TextBox.msgWarm(action + " th\u00e0nh #2" + targetName,
                            VqsvText.Evolution.CONTINUE_PROMPT_5);
                    sourceEvolvePhase = 2;
                    sourceEvolveSucceeded = true;
                    sourceStateTrace.add("PORTED/PARTIAL game.h.bh ah effect complete successMsg target="
                            + completedNotice.targetSpeciesId
                            + " refresh current="
                            + (sourceEvolveNotice == null ? "none"
                            : sourceEvolveNotice.currentSpeciesId + "->" + sourceEvolveNotice.targetSpeciesId));
                }
                return;
            }
            if (text != null) {
                if (text.readyForKey && key0) {
                    text.confirm();
                    key0 = false;
                    if (sourceEvolvePhase == 2) {
                        if (sourceEvolveSucceeded) {
                            sourceEvolvePhase = 0;
                            sourceStateTrace.add("PORTED/PARTIAL game.h.bh close msgwarm success=true return f=2 evolve.ui");
                        } else {
                            sourceEvolvePhase = 0;
                            sourceStateTrace.add("PORTED/PARTIAL game.h.bh close msgwarm warning return f=2 evolve.ui");
                        }
                    }
                }
                return;
            }
            if (keyBack && sourceEvolvePhase < 2) {
                closeSourceEvolveUi(false);
                sourceStateTrace.add("PORTED game.h.bh back key closes evolve.ui f<3");
                return;
            }
            if (!key0) {
                return;
            }
            if (sourceEvolveNotice == null || sourceEvolveNotice.targetSpeciesId < 0) {
                text = TextBox.msgWarm(VqsvText.Evolution.CANNOT_EVOLVE, VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceEvolvePhase = 2;
                return;
            }
            if (sourcePets.get(sourceEvolvePetIndex).level < sourceEvolveNotice.requiredLevel) {
                text = TextBox.msgWarm(VqsvText.Evolution.levelTooLow(sourceEvolveNotice.requiredLevel),
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceEvolvePhase = 2;
                return;
            }
            int materialCount = VqsvSourceEvolutionRuntime.materialCount(this, sourceEvolveNotice.materialId);
            if (materialCount < sourceEvolveNotice.materialNeed) {
                String message = sourceEvolveNotice.targetKind == 3
                        ? VqsvText.Evolution.MATERIAL_MISSING_MUTATE
                        : VqsvText.Evolution.MATERIAL_MISSING_EVOLVE;
                text = TextBox.msgWarm(message, VqsvText.Evolution.CONTINUE_PROMPT_5);
                sourceEvolvePhase = 2;
                return;
            }
            VqsvSourceEvolutionRuntime.consumeMaterial(this,
                    sourceEvolveNotice.materialId, sourceEvolveNotice.materialNeed);
            sourceEvolvePhase = 1;
            sourceEvolveEffectTicks = 0;
            sourceStateTrace.add("PORTED/PARTIAL game.h.bh start ah type10 row=[0,0,10,0,0,"
                    + sourceEvolveOldVisualId + ",0,0," + sourceEvolveNewVisualId + ",0,0]"
                    + " t=[0," + sourceEvolveNewVisualId + ",0,0]"
                    + " consume material=" + sourceEvolveNotice.materialId
                    + " qty=" + sourceEvolveNotice.materialNeed
                    + " remaining=" + VqsvSourceEvolutionRuntime.materialCount(this, sourceEvolveNotice.materialId));
        }

        private void refreshSourceEvolvePanelFromPet() {
            if (sourceEvolvePetIndex < 0 || sourceEvolvePetIndex >= sourcePets.size()) {
                sourceEvolveNotice = null;
                sourceEvolveOldStats = new int[]{0, 0, 0, 0};
                sourceEvolveNewStats = new int[]{0, 0, 0, 0};
                sourceEvolveOldVisualId = -1;
                sourceEvolveNewVisualId = -1;
                return;
            }
            SourcePetState pet = sourcePets.get(sourceEvolvePetIndex);
            sourceEvolveNotice = VqsvSourceEvolutionRuntime.noticeForPet(this, sourceEvolvePetIndex);
            sourceEvolveOldStats = VqsvSourceEvolutionRuntime.visibleStats(pet);
            sourceEvolveNewStats = sourceEvolveNotice == null
                    ? new int[]{0, 0, 0, 0}
                    : VqsvSourceEvolutionRuntime.targetVisibleStats(pet, sourceEvolveNotice.targetSpeciesId);
            BattleSpeciesRow current = VqsvBattleTables.instance().species(pet.speciesId);
            BattleSpeciesRow target = sourceEvolveNotice == null ? null
                    : VqsvBattleTables.instance().species(sourceEvolveNotice.targetSpeciesId);
            sourceEvolveOldVisualId = current == null ? -1 : VqsvBattleTables.get(current.raw, 17, -1);
            sourceEvolveNewVisualId = target == null ? -1 : VqsvBattleTables.get(target.raw, 17, -1);
            sourceStateTrace.add("PORTED/PARTIAL game.h.bh refresh evolve.ui current="
                    + pet.speciesId
                    + " next=" + (sourceEvolveNotice == null ? -1 : sourceEvolveNotice.targetSpeciesId)
                    + " material=" + (sourceEvolveNotice == null ? -1 : sourceEvolveNotice.materialId)
                    + " count=" + (sourceEvolveNotice == null ? 0
                    : VqsvSourceEvolutionRuntime.materialCount(this, sourceEvolveNotice.materialId))
                    + "/" + (sourceEvolveNotice == null ? 0 : sourceEvolveNotice.materialNeed));
        }

        private int sourceEvolveType10Duration() {
            return Math.max(1, sourceEvolveNewVisualId);
        }

        private void closeSourceEvolveUi(boolean success) {
            sourceEvolveVisible = false;
            sourceEvolvePetIndex = -1;
            sourceEvolveNotice = null;
            sourceEvolvePhase = 0;
            sourceEvolveEffectTicks = 0;
            sourceEvolveSucceeded = false;
            sourceEvolutionL[0] = -1;
            sourceEvolutionL[1] = -1;
            sourceEvolutionTutorialU = -1;
            sourceStateTrace.add("PORTED/PARTIAL game.h.bh close evolve.ui success=" + success
                    + " reset game.k.L and tutorial U");
        }

        private String sourceEvolutionTargetName(SourceEvolutionNotice notice) {
            BattleSpeciesRow row = notice == null ? null
                    : VqsvBattleTables.instance().species(notice.targetSpeciesId);
            if (row != null && row.validForBattle()) {
                return row.name("Pet " + notice.targetSpeciesId);
            }
            return notice == null ? "" : "Pet " + notice.targetSpeciesId;
        }

        private void hoverWorldPetstate(int x, int y) {
            int index = worldPetstateIndexAt(x, y);
            if (index >= 0 && index < battleMenuIds.length) {
                battleMenuIndex = index;
            }
        }

        private void clickWorldPetstate(int x, int y) {
            int index = worldPetstateIndexAt(x, y);
            if (index >= 0 && index < battleMenuIds.length) {
                battleMenuIndex = index;
                return;
            }
            if (x >= 150 && y >= 235) {
                worldPetstateVisible = false;
            }
        }

        private int worldPetstateIndexAt(int x, int y) {
            int start = Math.max(0, Math.min(battleMenuScroll, Math.max(0, battleMenuIds.length - 6)));
            int visibleRows = Math.min(6, battleMenuIds.length - start);
            for (int row = 0; row < visibleRows; row++) {
                int rowY = 86 + row * 15;
                if (x >= 43 && x <= 197 && y >= rowY && y <= rowY + 14) {
                    return start + row;
                }
            }
            return -1;
        }

        private void hoverSourceSkill(int x, int y) {
            VqsvUiLayout layout = VqsvUiLayout.load("skill.ui");
            int[] widgets = {18, 19, 20, 21, 22};
            for (int i = 0; i < widgets.length && i < sourceSkillCount; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(widgets[i]);
                if (row != null && x >= row.x - 4 && x <= row.x + Math.max(100, row.w) + 8
                        && y >= row.y - 2 && y <= row.y + 14) {
                    sourceSkillIndex = i;
                    return;
                }
            }
        }

        private void hoverSourcePetSetting(int x, int y) {
            VqsvUiLayout layout = VqsvUiLayout.load("petsetting.ui");
            int[] widgets = {5, 6, 7, 8, 10, 9};
            for (int i = 0; i < widgets.length && i < sourcePetSettingCount; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(widgets[i]);
                if (row != null && x >= row.x - 18 && x <= row.x + Math.max(76, row.w) + 8
                        && y >= row.y - 2 && y <= row.y + 14) {
                    sourcePetSettingIndex = i;
                    return;
                }
            }
        }

        private void hoverSourceItemChoice(int x, int y) {
            int index = sourceChoiceIndexAt(x, y, sourceItemChoiceScroll, sourceItemChoiceSize());
            if (index >= 0) {
                sourceItemChoiceIndex = index;
            }
        }

        private void hoverSourceEquipmentChoice(int x, int y) {
            int index = sourceChoiceIndexAt(x, y, sourceEquipmentChoiceScroll, sourceEquipmentChoiceSize());
            if (index >= 0) {
                sourceEquipmentChoiceIndex = index;
            }
        }

        private int sourceChoiceIndexAt(int x, int y, int scroll, int size) {
            VqsvUiLayout layout = VqsvUiLayout.load("choice.ui");
            for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
                VqsvUiLayout.UiWidget row = layout.widget(11 + rowIndex * 5);
                if (row != null && x >= row.x - 4 && x <= row.x + 136
                        && y >= row.y - 2 && y <= row.y + 14) {
                    int index = scroll + rowIndex;
                    return index >= 0 && index < size ? index : -1;
                }
            }
            return -1;
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


