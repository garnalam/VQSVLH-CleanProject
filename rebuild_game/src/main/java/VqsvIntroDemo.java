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
        int battleTutorialU = -1;
        int battleTutorialV = 0;
        VqsvBattleLevelUpView battleLevelUpView = VqsvBattleLevelUpView.EMPTY;
        int sourceMoney;
        int sourceBadges;
        final Map<Integer, BagItem> sourceBagItems = VqsvSourceOps.initialSourceBagItems();
        final Map<Integer, SourceSpecialReward> sourceSpecialRewards = VqsvSourceOps.initialSourceSpecialRewards();
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
        boolean savePromptVisible = false;
        String savePromptMessage = "";
        String savePromptStatus = "";
        int savePromptSelected = 0;
        int savePromptClickX = -1;
        int savePromptClickY = -1;
        boolean worldPetstateVisible = false;
        boolean sourceEvolveVisible = false;
        int sourceEvolvePetIndex = -1;
        SourceEvolutionNotice sourceEvolveNotice;
        int[] sourceEvolveOldStats = new int[]{0, 0, 0, 0};
        int[] sourceEvolveNewStats = new int[]{0, 0, 0, 0};
        int sourceEvolveOldVisualId = -1;
        int sourceEvolveNewVisualId = -1;
        int sourceEvolvePhase = 0;
        int sourceEvolveEffectTicks = 0;
        boolean sourceEvolveSucceeded = false;

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
            if (worldPetstateVisible) {
                clickWorldPetstate(x, y);
                key0 = true;
                return;
            }
            if (worldUi.visible && useMap && x <= 44 && y >= 296) {
                openWorldPetstate();
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
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_BACK_SPACE:
                    keyBack = pressed;
                    break;
                default:
                    break;
            }
        }

        void tick() {
            effect.tick();
            if (battleOverlayTicks > 0 || worldPetstateVisible || sourceEvolveVisible) {
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
            if (sourceEvolveVisible) {
                tickSourceEvolve();
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

        private boolean startSourceEvolutionNoticeIfReady() {
            if (sourceEvolutionI != 0 || sourceEvolutionQueue.isEmpty()) {
                return false;
            }
            if (text != null || choice != null || savePromptVisible || worldPetstateVisible
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
            battlePetStateRows = new VqsvBattlePetStateView[6];
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
            for (int row = 0; row < battlePetStateRows.length; row++) {
                if (row < sourcePets.size()) {
                    battlePetStateRows[row] = VqsvBattlePetStateView.fromPet(row, row, sourcePets.get(row), row == 0);
                } else {
                    battlePetStateRows[row] = VqsvBattlePetStateView.empty(row);
                }
            }
            sourceStateTrace.add("PORTED/PARTIAL world petstate.ui open owner=game.k rows="
                    + java.util.Arrays.toString(battleMenuIds));
        }

        private void tickWorldPetstate() {
            if (keyUp && battleMenuIndex > 0) {
                battleMenuIndex--;
            } else if (keyDown && battleMenuIndex < battleMenuIds.length - 1) {
                battleMenuIndex++;
            }
            if (key0) {
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
                worldPetstateVisible = false;
                sourceStateTrace.add("PORTED/PARTIAL world petstate.ui close");
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

        private void clickWorldPetstate(int x, int y) {
            for (int row = 0; row < Math.min(6, battleMenuIds.length); row++) {
                int rowY = 86 + row * 15;
                if (x >= 43 && x <= 103 && y >= rowY && y <= rowY + 14) {
                    battleMenuIndex = row;
                    return;
                }
            }
            if (x >= 150 && y >= 235) {
                worldPetstateVisible = false;
            }
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


