import java.util.Arrays;

final class VqsvBattleRuntime {
    private VqsvBattleRuntime() {
    }
}

enum BattleRuntimeState {
    P0_ENTRY(0, "P0"),
    P20_COMMAND(20, "P20"),
    P3_SKILL_LIST(3, "P3"),
    P6_TARGET_SELECT(6, "P6"),
    P21_CATCH_LIST(21, "P21"),
    P17_CATCH_RESULT(17, "P17"),
    P4_ITEM_LIST(4, "P4"),
    P16_ITEM_TARGET(16, "P16"),
    P5_PET_SWITCH(5, "P5"),
    P11_SHOP(11, "P11"),
    P10_RUN(10, "P10"),
    WARNING(-3, "WARN"),
    P2_SELECT_EXECUTE(2, "P2"),
    P7_RESOLVE(7, "P7"),
    P12_ACTIVE_QUEUE(12, "P12"),
    P13_ACTIVE_QUEUE(13, "P13"),
    P15_PLAYER_SWITCH(15, "P15"),
    P15_ENEMY_REPLACEMENT(15, "P15"),
    P1_DISPATCH(1, "P1"),
    P8_WIN(8, "P8"),
    P9_LOSE(9, "P9"),
    EXIT_FADE(-1, "EXIT"),
    DONE(-2, "DONE");

    final int sourceId;
    final String label;

    BattleRuntimeState(int sourceId, String label) {
        this.sourceId = sourceId;
        this.label = label;
    }
}

enum MenuAction {
    NONE,
    CONFIRM,
    BACK
}

final class P7ActorAnimation {
    private static final int[] SOURCE_AH_ACTOR_SPRITES = {
            262, 263, 264, 265, 266, 267, 268, 299, 300, 301, 304, 306, 307, 308, 309
    };

    final boolean playerSide;
    final int sourceEffectId;
    final int spriteId;
    final int state;
    private final SpriteAnim anim;
    private boolean started;
    private boolean stopped;

    P7ActorAnimation(boolean playerSide, int sourceEffectId, int state) {
        this.playerSide = playerSide;
        this.sourceEffectId = sourceEffectId;
        this.spriteId = spriteForSourceEffect(sourceEffectId);
        this.state = Math.max(0, state);
        this.anim = spriteId >= 0 ? SpriteAnim.load(spriteId) : null;
        if (this.anim != null) {
            this.anim.setState(this.state);
        }
    }

    void start() {
        started = true;
    }

    boolean started() {
        return started;
    }

    boolean stopped() {
        return stopped;
    }

    void stop() {
        stopped = true;
    }

    boolean frame(int frame) {
        return started && !stopped && frame >= 0 && cursor() == frame;
    }

    boolean lastFrame() {
        if (anim == null || anim.data.anim == null || anim.data.anim.length == 0) {
            return true;
        }
        int frames = anim.data.anim[anim.state].length / 2;
        return frames <= 1 || anim.cursor >= frames - 1;
    }

    void tick() {
        if (started && !stopped && anim != null) {
            anim.tickHoldLast();
        }
    }

    int cursor() {
        return anim == null ? 0 : anim.cursor;
    }

    boolean complete() {
        return started && (anim == null || lastFrame());
    }

    private static int spriteForSourceEffect(int sourceEffectId) {
        int index = sourceEffectId - 20;
        if (index < 0 || index >= SOURCE_AH_ACTOR_SPRITES.length) {
            return -1;
        }
        return SOURCE_AH_ACTOR_SPRITES[index];
    }
}

final class SourceBattleRuntime implements Blocking {
    private static final int SHORT_WAIT = 6;
    private static final int RESOLVE_WAIT = 10;
    private static final int EXIT_WAIT = 12;
    private static final int P7_START_TICKS = 8;
    private static final int P7_DAMAGE_TICKS = 12;
    private static final int P7_EXIT_TICKS = 6;
    private static final VqsvSourceRandom SOURCE_RANDOM = VqsvSourceRandom.lazySourceSeeded();

    private final int actorId;
    private final int[] encounter;
    private final int[] flags;
    private final int[] battleMode;
    private final int[] branchTargets;
    private final int forcedResultIndex;
    private final boolean sourceBattleSlice;

    private BattleRuntimeState state = BattleRuntimeState.P0_ENTRY;
    private int wait;
    private SourceBattleUnit[] enemyParty = new SourceBattleUnit[0];
    private int activeEnemyIndex;
    private int pendingEnemyReplacementIndex = -1;
    private SourceBattleUnit enemy;
    private SourceBattleUnit player;
    private int[] sourcePetOrder = new int[0];
    private int playerDisplayHp;
    private int enemyDisplayHp;
    private int turn;
    private boolean entered;
    private boolean currentActorPlayer;
    private boolean playerActionThisRound;
    private boolean enemyActionThisRound;
    private boolean bunnyTutorialShown;
    private boolean bunnyTutorialWeakPromptActive;
    private boolean bunnyTutorialFirstCatchPending;
    private boolean bunnyTutorialForceFailActive;
    private boolean bunnyTutorialRetryPending;
    private boolean bunnyTutorialRetryPromptActive;
    private boolean bunnyTutorialRetryPromptAfterEnemy;
    private int bunnyTutorialU = -1;
    private int bunnyTutorialV = 0;
    private boolean bunnyCaptureQueued;
    private boolean exitFadeStarted;
    private boolean playerPetPersistedOnExit;
    private boolean wasLeftPressed;
    private boolean wasRightPressed;
    private boolean wasUpPressed;
    private boolean wasDownPressed;
    private boolean commandConfirmQueued;
    private short[][] battleEntryCposRows = new short[0][];
    private int battleEntryActorIndex;
    private int battleEntryFrame;
    private int battleEntryFrameTicks;
    private boolean battleEntryCposActive;
    private BattleRuntimeState warningReturnState = BattleRuntimeState.P20_COMMAND;
    private String warningReturnLog = VqsvText.Battle.START;
    private int selectedItemId = -1;
    private int selectedPetIndex = -1;
    private boolean forcedPetSwitch;
    private boolean playerSwitchWasForced;
    private int playerSwitchTicks;
    private short[] playerSwitchCposRow = new short[0];
    private int selectedSkillId = -1;
    private SourceBattleUnit[] targetUnits = new SourceBattleUnit[0];
    private int[] targetSlots = new int[0];
    private int selectedTargetIndex = 0;
    private SourceBattleUnit selectedTarget;
    private SourceBattleUnit p7Attacker;
    private SourceBattleUnit p7Target;
    private int p7SkillId = -1;
    private int p7Phase = 0;
    private int p7Ticks = 0;
    private int p7Damage = 0;
    private BattleDamageResult p7DamageResult;
    private boolean p7AttackHit = true;
    private int p7MissChance;
    private int p7HitRoll = -1;
    private int debugNextP7HitRoll = -1;
    private int debugNextLeechRoll = -1;
    private int debugNextFollowUpRoll = -1;
    private boolean p7PostEffectApplied;
    private String p7PostEffectText = "";
    private boolean p7PostEffectPlayerSide;
    private boolean p7Prepared;
    private boolean p7DamageApplied;
    private boolean p7HpTweenActive;
    private boolean p7HpTweenPlayerSide;
    private int p7HpTweenStep;
    private int p7HpTweenAccum;
    private int p7HpTweenDelay;
    private boolean p7DeathEffectActive;
    private boolean p7DeathEffectPlayerSide;
    private int p7DeathEffectSpriteId = -1;
    private int p7DeathEffectTicks;
    private int p7DeathEffectDuration;
    private byte[] p7EffectRow = new byte[0];
    private short[] p7SpeffectRow = new short[0];
    private int p7EffectChunk;
    private int p7SourceI;
    private int p7SourceJ;
    private int p7SourceK;
    private int p7SourceL;
    private boolean p7FlagM;
    private boolean p7FlagN;
    private boolean p7FlagZ;
    private boolean p7FlagA;
    private boolean p7FlagB;
    private int p7SpecialType = -1;
    private boolean p7SpecialPrepared;
    private boolean p7SpecialActive;
    private int p7SpecialTicks;
    private boolean p7LEffectActive;
    private boolean p7LEffectPlayerSide;
    private boolean p7LEffectDrawAfter;
    private int p7LEffectTicks;
    private int p7LEffectSpeffectId = -1;
    private short[] p7LEffectRow = new short[0];
    private boolean p7BaseHiddenPlayerSide;
    private boolean p7BaseHiddenEnemySide;
    private boolean p7KoBaseHiddenPlayerSide;
    private boolean p7KoBaseHiddenEnemySide;
    private int p7BaseStatePlayerSide;
    private int p7BaseStateEnemySide;
    private int p7BaseStateStartTickPlayerSide;
    private int p7BaseStateStartTickEnemySide;
    private P7ActorAnimation p7ActorAnimation;
    private SourceBattleUnit activeQueueUnit;
    private boolean activeQueuePlayerSide;
    private int activeQueueBank;
    private int activeQueueBuffId = -1;
    private int activeQueueDebuffId = -1;
    private int activeQueueSlot = -1;
    private final int[] activeQueueOrderBank = new int[6];
    private final int[] activeQueueOrderId = new int[6];
    private final int[] activeQueueOrderSlot = new int[6];
    private int activeQueueOrderCount;
    private int activeQueueOrderIndex;
    private byte[] activeQueueVisualRow = new byte[0];
    private int activeQueueSegment;
    private int activeQueueTicks;
    private boolean activeQueueApplied;
    private P7ActorAnimation activeQueueActorAnimation;
    private boolean playerActiveQueueProcessedThisRound;
    private boolean enemyActiveQueueProcessedThisRound;
    private int catchPhase = -1;
    private int catchPhaseTicks = 0;
    private int catchChance = 0;
    private int catchRoll = -1;
    private boolean catchCaught;
    private int debugNextCatchRoll = -1;
    private boolean catchTraceWritten;
    private SpriteAnim catchAnim;
    private boolean catchAnimHoldLast;
    private int catchEffectT0;
    private int catchEffectT1;
    private int catchEffectT2;
    private int catchEffectT3;
    private int catchEffectT4;
    private int[][] catchEffectSteps;
    private int catchEffectScale10;
    private int catchEffectDx;
    private int catchEffectDy;
    private String catchWinLog;
    private int catchStorageResult = -1;
    private int catchOpenBoxState = 0;
    private final java.util.ArrayList<SourcePetState> sourceExpParticipants = new java.util.ArrayList<>();
    private final java.util.ArrayList<SourcePetState> sourceExpDisplay = new java.util.ArrayList<>();
    private int expDisplayIndex;
    private SourcePetState expCurrentPet;
    private BattleUnit expCurrentUnit;
    private boolean expPrepared;
    private boolean expEligible;
    private int expAward;
    private int expDisplayValue;
    private int expHoldTicks;
    private int[] expOldStats = new int[4];
    private int[] expNewStats = new int[4];
    private boolean expLevelUpPending;
    private boolean expLevelUpApplied;
    private boolean expLearningSkill;
    private boolean expLearningConfirm;
    private int[] expLearnSkillIds = new int[0];
    private int expSelectedLearnSkill = -1;

    SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode, int[] branchTargets) {
        this(actorId, encounter, flags, battleMode, branchTargets, 0, false);
    }

    SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode,
                        int[] branchTargets, int forcedResultIndex) {
        this(actorId, encounter, flags, battleMode, branchTargets, forcedResultIndex, false);
    }

    SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode,
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
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!entered) {
            enterBattle(s);
        }
        s.battleStateName = state.label;
        if (state != BattleRuntimeState.DONE) {
            s.battleOverlayTicks = 1;
        }
        if (bunnyTutorialWeakPromptActive) {
            return tickBunnyTutorialWeakPrompt(s);
        }
        if (bunnyTutorialRetryPromptActive) {
            return tickBunnyTutorialRetryPrompt(s);
        }
        switch (state) {
            case P0_ENTRY:
                return tickEntry(s);
            case P20_COMMAND:
                return tickCommand(s);
            case P3_SKILL_LIST:
                return tickSkillList(s);
            case P6_TARGET_SELECT:
                return tickTargetSelect(s);
            case P21_CATCH_LIST:
                return tickCatchList(s);
            case P17_CATCH_RESULT:
                return tickCatchResult(s);
            case P4_ITEM_LIST:
                return tickItemList(s);
            case P16_ITEM_TARGET:
                return tickItemTarget(s);
            case P5_PET_SWITCH:
                return tickPetSwitch(s);
            case P11_SHOP:
                return tickShop(s);
            case P10_RUN:
                return tickRun(s);
            case WARNING:
                return tickWarning(s);
            case P2_SELECT_EXECUTE:
                return tickSelectExecute(s);
            case P7_RESOLVE:
                return tickResolve(s);
            case P12_ACTIVE_QUEUE:
            case P13_ACTIVE_QUEUE:
                return tickActiveQueue(s);
            case P15_PLAYER_SWITCH:
                return tickPlayerSwitchTransition(s);
            case P15_ENEMY_REPLACEMENT:
                return tickEnemyReplacement(s);
            case P1_DISPATCH:
                return tickDispatch(s);
            case P8_WIN:
                return tickWin(s);
            case P9_LOSE:
                return tickLose(s);
            case EXIT_FADE:
                return tickExit(s);
            case DONE:
                return true;
            default:
                return false;
        }
    }

    private void enterBattle(VqsvIntroDemo.Scene s) {
        enemyParty = enemyPartyFromEncounter(encounter);
        activeEnemyIndex = 0;
        pendingEnemyReplacementIndex = -1;
        enemy = enemyParty[activeEnemyIndex];
        p7KoBaseHiddenPlayerSide = false;
        p7KoBaseHiddenEnemySide = false;
        if (s.sourcePets.isEmpty()) {
            throw new IllegalStateException("Source battle entered without player pet state; "
                    + "source game.k/game.g.I or op36/op87 setup must run before encounter="
                    + Arrays.toString(encounter));
        }
        resetSourcePetOrder(s);
        setActiveSourcePetFlags(s, 0);
        player = SourceBattleUnit.playerFromSourcePets(s.sourcePets);
        playerDisplayHp = player.hp;
        enemyDisplayHp = enemy.hp;
        resetSourceExpVectors(s);
        addSourceExpParticipant(s, s.sourcePets.get(0), "battle entry active f[0]");
        s.worldEventActor = actorId;
        s.battleEventActor = actorId;
        s.battleEncounter = Arrays.copyOf(encounter, encounter.length);
        s.battleCanLose = flags.length > 0 && flags[0] == 0;
        s.battleScriptLocksInput = flags.length > 1 && flags[1] == 0;
        s.battleMode = battleMode.length > 0 ? battleMode[0] : -1;
        s.battleBackgroundMode = battleMode.length > 1 ? battleMode[1] : -1;
        s.battleBackgroundSnapshot = (s.useMap && s.mapRenderer != null)
                ? VqsvSceneView.captureBattleBackground(s)
                : null;
        s.battleResultIndex = -2;
        s.battleBranchTarget = resolveBranch(s.battleResultIndex);
        s.battleCaptureTutorial = isBunnyCaptureBattle();
        if (isBunnyCaptureBattle()) {
            setBunnyTutorialState(s, 0, 0, "battle entry");
        } else {
            setBunnyTutorialState(s, -1, 0, "non-bunny battle");
        }
        s.battleCommandIndex = 0;
        syncRenderState(s, VqsvText.Battle.START);
        s.sourceStateTrace.add("PORTED/PARTIAL battle state machine actor=" + actorId
                + " encounter=" + Arrays.toString(encounter)
                + " flags=" + Arrays.toString(flags)
                + " mode=" + Arrays.toString(battleMode)
                + " enemy=" + enemy
                + " enemyPartySize=" + enemyParty.length
                + " player=" + player
                + " sourcePetOrder=" + Arrays.toString(sourcePetOrder)
                + " branchTargets=" + Arrays.toString(branchTargets)
                + " sourceSlice=" + sourceBattleSlice
                + " battleBackground=PORTED/PARTIAL game.k captures world renderer into game.d.c"
                + " before game.i state 12; renderer falls back to black if snapshot missing"
                + " states=P0/P20/P3/P6/P2/P7/P1/P8/P9; command UI/catch/items/animation still pending; "
                + VqsvBattleTables.sourceSummary());
        entered = true;
        enterState(s, BattleRuntimeState.P0_ENTRY, VqsvText.Battle.START, SHORT_WAIT);
        prepareBattleEntryCpos(s);
        s.effect.startFade(2, 0);
    }

    private SourceBattleUnit[] enemyPartyFromEncounter(int[] rawEncounter) {
        if (rawEncounter == null || rawEncounter.length == 0) {
            return new SourceBattleUnit[]{SourceBattleUnit.enemyFromEncounter(new int[0])};
        }
        if (rawEncounter.length > 3 && rawEncounter.length % 3 == 0) {
            SourceBattleUnit[] party = new SourceBattleUnit[rawEncounter.length / 3];
            for (int i = 0; i < party.length; i++) {
                party[i] = SourceBattleUnit.enemyFromEncounter(new int[]{
                        rawEncounter[i * 3], rawEncounter[i * 3 + 1], rawEncounter[i * 3 + 2]
                });
            }
            return party;
        }
        return new SourceBattleUnit[]{SourceBattleUnit.enemyFromEncounter(rawEncounter)};
    }

    private boolean tickEntry(VqsvIntroDemo.Scene s) {
        if (!s.effect.doneOverlay(s) || countdown()) {
            applyBattleEntryCposOffset(s);
            return false;
        }
        if (battleEntryCposActive && !tickBattleEntryCpos(s)) {
            return false;
        }
        clearBattleEntryCposOffset(s);
        enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.START, SHORT_WAIT);
        return false;
    }

    private void prepareBattleEntryCpos(VqsvIntroDemo.Scene s) {
        int group = sourceCposGroup();
        java.util.ArrayList<short[]> rows = new java.util.ArrayList<>();
        for (int row = 0; row < 2; row++) {
            short[] cpos = VqsvBattleAnimationTables.instance().cposRow(group, row);
            if (cpos.length >= 4) {
                rows.add(cpos);
            }
        }
        battleEntryCposRows = rows.toArray(new short[rows.size()][]);
        battleEntryActorIndex = 0;
        battleEntryFrame = 0;
        battleEntryFrameTicks = 0;
        battleEntryCposActive = battleEntryCposRows.length > 0;
        applyBattleEntryCposOffset(s);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P0 entry cpos start group=" + group
                + " actors=" + battleEntryCposRows.length
                + " source=game.d.an[r][G] from /data/script/cpos.mid"
                + " marker/al sprite294 rendered as footprint marker with cpos offsets");
    }

    private boolean tickBattleEntryCpos(VqsvIntroDemo.Scene s) {
        applyBattleEntryCposOffset(s);
        battleEntryFrameTicks++;
        if (battleEntryFrameTicks <= 1) {
            return false;
        }
        battleEntryFrameTicks = 0;
        battleEntryFrame++;
        if (battleEntryFrame < battleEntryFrameCount(battleEntryActorIndex)) {
            return false;
        }
        battleEntryActorIndex++;
        battleEntryFrame = 0;
        if (battleEntryActorIndex < battleEntryCposRows.length) {
            applyBattleEntryCposOffset(s);
            return false;
        }
        battleEntryCposActive = false;
        clearBattleEntryCposOffset(s);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P0 entry cpos complete next=P1/P20"
                + " group=" + sourceCposGroup());
        return true;
    }

    private void applyBattleEntryCposOffset(VqsvIntroDemo.Scene s) {
        clearBattleEntryCposOffset(s);
        if (!battleEntryCposActive) {
            return;
        }
        applyBattleEntryActorCposOffset(s, 0, battleEntryActorIndex == 0 ? battleEntryFrame
                : battleEntryActorIndex > 0 ? Integer.MAX_VALUE : 0);
        applyBattleEntryActorCposOffset(s, 1, battleEntryActorIndex == 1 ? battleEntryFrame
                : battleEntryActorIndex > 1 ? Integer.MAX_VALUE : 0);
    }

    private void applyBattleEntryActorCposOffset(VqsvIntroDemo.Scene s, int actorIndex, int requestedFrame) {
        if (actorIndex < 0 || actorIndex >= battleEntryCposRows.length) {
            return;
        }
        short[] row = battleEntryCposRows[actorIndex];
        int frames = row.length / 4;
        if (frames <= 0) {
            return;
        }
        int frame = Math.max(0, Math.min(frames - 1, requestedFrame));
        int at = frame << 2;
        int finalAt = (frames - 1) << 2;
        int dx = row[at] - row[finalAt];
        int dy = row[at + 1] - row[finalAt + 1];
        if (actorIndex == 0) {
            s.battleP7EnemyOffsetX = dx;
            s.battleP7EnemyOffsetY = dy;
        } else if (actorIndex == 1) {
            s.battleP7PlayerOffsetX = dx;
            s.battleP7PlayerOffsetY = dy;
        }
    }

    private void clearBattleEntryCposOffset(VqsvIntroDemo.Scene s) {
        s.battleP7PlayerOffsetX = 0;
        s.battleP7PlayerOffsetY = 0;
        s.battleP7EnemyOffsetX = 0;
        s.battleP7EnemyOffsetY = 0;
    }

    private int battleEntryFrameCount(int actorIndex) {
        if (actorIndex < 0 || actorIndex >= battleEntryCposRows.length) {
            return 0;
        }
        return battleEntryCposRows[actorIndex].length / 4;
    }

    private boolean tickDispatch(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (!player.alive()) {
            if (hasSwitchPet(s)) {
                forcedPetSwitch = true;
                preparePetMenu(s);
                enterState(s, BattleRuntimeState.P5_PET_SWITCH, VqsvText.Battle.COMMAND_PET_PENDING, SHORT_WAIT);
            } else {
                enterState(s, BattleRuntimeState.P9_LOSE, VqsvText.Battle.NEIL_LOST + forcedResultIndex, SHORT_WAIT);
            }
            return false;
        }
        if (!enemy.alive()) {
            if (prepareEnemyReplacement(s)) {
                return false;
            }
            enterState(s, BattleRuntimeState.P8_WIN, battleWinLog(), SHORT_WAIT);
            return false;
        }
        if (isKidnappingBattle()) {
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, enemy.name, SHORT_WAIT);
            return false;
        }
        if (!enemyActionThisRound && !playerActionThisRound && enemy.speed > player.speed) {
            if (tryEnterActiveQueue(s, enemy, false)) {
                return false;
            }
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, enemy.name, SHORT_WAIT);
            return false;
        }
        if (!playerActionThisRound) {
            if (tryEnterActiveQueue(s, player, true)) {
                return false;
            }
            currentActorPlayer = true;
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (!enemyActionThisRound) {
            if (tryEnterActiveQueue(s, enemy, false)) {
                return false;
            }
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, enemy.name, SHORT_WAIT);
            return false;
        }
        playerActionThisRound = false;
        enemyActionThisRound = false;
        playerPetPersistedOnExit = false;
        playerActiveQueueProcessedThisRound = false;
        enemyActiveQueueProcessedThisRound = false;
        turn++;
        enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.START, SHORT_WAIT);
        return false;
    }

    private boolean tryEnterActiveQueue(VqsvIntroDemo.Scene s, SourceBattleUnit unit, boolean playerSide) {
        if (unit == null || unit.battleUnit == null) {
            return false;
        }
        if (playerSide && playerActiveQueueProcessedThisRound || !playerSide && enemyActiveQueueProcessedThisRound) {
            return false;
        }
        if (unit.battleUnit.hasBuff(13) || unit.battleUnit.hasBuff(14)) {
            unit.battleUnit.clearDebuffs();
        }
        if (!buildActiveQueueOrder(unit.battleUnit)) {
            return false;
        }
        activeQueueUnit = unit;
        activeQueuePlayerSide = playerSide;
        activeQueueOrderIndex = 0;
        BattleRuntimeState next = playerSide ? BattleRuntimeState.P13_ACTIVE_QUEUE : BattleRuntimeState.P12_ACTIVE_QUEUE;
        enterState(s, next, s.battleLog, 0);
        s.sourceStateTrace.add("PORTED/PARTIAL battle " + next.label
                + " active queue source order count=" + activeQueueOrderCount
                + " side=" + (playerSide ? "player" : "enemy"));
        startNextActiveQueueEntry(s, false);
        return true;
    }

    private boolean buildActiveQueueOrder(BattleUnit unit) {
        Arrays.fill(activeQueueOrderBank, -1);
        Arrays.fill(activeQueueOrderId, -1);
        Arrays.fill(activeQueueOrderSlot, -1);
        activeQueueOrderCount = 0;
        for (int bank = 0; bank <= 1; bank++) {
            for (int slot = 0; slot < 3; slot++) {
                int effectId = unit.activeEffectIdAt(bank, slot);
                if (effectId < 0 || activeQueueOrderCount >= activeQueueOrderBank.length) {
                    continue;
                }
                activeQueueOrderBank[activeQueueOrderCount] = bank;
                activeQueueOrderId[activeQueueOrderCount] = effectId;
                activeQueueOrderSlot[activeQueueOrderCount] = slot;
                activeQueueOrderCount++;
            }
        }
        return activeQueueOrderCount > 0;
    }

    private boolean tickActiveQueue(VqsvIntroDemo.Scene s) {
        if (activeQueueUnit == null || activeQueueUnit.battleUnit == null || activeQueueBank < 0) {
            finishActiveQueue(s, true);
            return false;
        }
        if (activeQueueSegment < activeQueueSegmentCount()) {
            activeQueueTicks++;
            tickActiveQueueActorAnimation(s);
            syncActiveQueueRenderState(s);
            if (activeQueueActorTriggerMatched() && activeQueueSegment + 1 < activeQueueSegmentCount()) {
                activeQueueSegment++;
                activeQueueTicks = 0;
                prepareActiveQueueSegment(s);
                syncActiveQueueRenderState(s);
                return false;
            }
            if (activeQueueVisualValue(0) == 0 && !activeQueueActorActionComplete()) {
                return false;
            }
            if (activeQueueVisualValue(0) != 0 && activeQueueTicks <= activeQueueSegmentDuration()) {
                return false;
            }
            activeQueueSegment++;
            activeQueueTicks = 0;
            if (activeQueueSegment < activeQueueSegmentCount()) {
                prepareActiveQueueSegment(s);
                syncActiveQueueRenderState(s);
                return false;
            }
        }
        if (!activeQueueApplied) {
            applyActiveQueueCurrentEntry(s, true);
            activeQueueApplied = true;
            activeQueueTicks = 0;
            return false;
        }
        activeQueueTicks++;
        syncRenderState(s, s.battleLog);
        syncActiveQueueBookkeeping(s);
        if (activeQueueTicks < P7_EXIT_TICKS) {
            return false;
        }
        activeQueueOrderIndex++;
        startNextActiveQueueEntry(s, true);
        return false;
    }

    private void startNextActiveQueueEntry(VqsvIntroDemo.Scene s, boolean afterVisualEntry) {
        while (activeQueueOrderIndex < activeQueueOrderCount) {
            activeQueueBank = activeQueueOrderBank[activeQueueOrderIndex];
            activeQueueBuffId = activeQueueBank == 0 ? activeQueueOrderId[activeQueueOrderIndex] : -1;
            activeQueueDebuffId = activeQueueBank == 1 ? activeQueueOrderId[activeQueueOrderIndex] : -1;
            activeQueueSlot = activeQueueOrderSlot[activeQueueOrderIndex];
            if (!activeQueueStillActive()) {
                activeQueueOrderIndex++;
                continue;
            }
            if (!activeQueueNeedsVisual(activeQueueBank, activeQueueEffectId())) {
                applyActiveQueueCurrentEntry(s, false);
                activeQueueOrderIndex++;
                if (!activeQueueUnit.alive()) {
                    finishActiveQueue(s, activeQueueOrderIndex >= activeQueueOrderCount);
                    return;
                }
                continue;
            }
            activeQueueVisualRow = VqsvBattleAnimationTables.instance()
                    .bufDebufVisualRow(activeQueueBank, activeQueueEffectId());
            activeQueueSegment = 0;
            activeQueueTicks = 0;
            activeQueueApplied = false;
            prepareActiveQueueSegment(s);
            syncActiveQueueRenderState(s);
            s.sourceStateTrace.add("PORTED battle " + state.label
                    + " active queue visual start bank=" + activeQueueBank
                    + " id=" + activeQueueEffectId()
                    + " slot=" + activeQueueSlot
                    + " order=" + activeQueueOrderIndex + "/" + activeQueueOrderCount
                    + " visual=" + activeQueueVisualLabel()
                    + " duration=" + activeQueueDuration());
            return;
        }
        finishActiveQueue(s, true);
    }

    private void applyActiveQueueCurrentEntry(VqsvIntroDemo.Scene s, boolean showHpText) {
        int beforeHp = activeQueueUnit.hp;
        int beforeSpeed = activeQueueUnit.battleUnit.currentStats[BattleUnit.STAT_SPEED];
        int beforeDefense = activeQueueUnit.battleUnit.currentStats[BattleUnit.STAT_DEFENSE];
        int beforeAttack = activeQueueUnit.battleUnit.currentStats[BattleUnit.STAT_ATTACK];
        int heal;
        int damage;
        if (activeQueueBank == 0) {
            heal = activeQueueUnit.battleUnit.tickSourceBuff(activeQueueBuffId, activeQueueSlot);
            damage = 0;
        } else {
            heal = 0;
            damage = activeQueueUnit.battleUnit.tickSourceDebuff(activeQueueDebuffId, activeQueueSlot);
        }
        activeQueueUnit.hp = activeQueueUnit.battleUnit.hp();
        clearActiveQueueSpecialRenderState(s);
        if (showHpText && (activeQueueUnit.hp < beforeHp || damage > 0)) {
            s.battleP7PostEffectVisible = true;
            s.battleP7PostEffectPlayerSide = activeQueuePlayerSide;
            s.battleP7PostEffectText = "" + Math.min(-1, activeQueueUnit.hp - beforeHp);
        } else if (showHpText && heal > 0) {
            s.battleP7PostEffectVisible = true;
            s.battleP7PostEffectPlayerSide = activeQueuePlayerSide;
            s.battleP7PostEffectText = "+" + heal;
        }
        syncRenderState(s, s.battleLog);
        syncActiveQueueBookkeeping(s);
        s.sourceStateTrace.add("PORTED battle " + state.label
                + " active queue apply bank=" + activeQueueBank
                + " id=" + activeQueueEffectId()
                + " slot=" + activeQueueSlot
                + " visualText=" + showHpText
                + " attack " + beforeAttack + "->" + activeQueueUnit.battleUnit.currentStats[BattleUnit.STAT_ATTACK]
                + " speed " + beforeSpeed + "->" + activeQueueUnit.battleUnit.currentStats[BattleUnit.STAT_SPEED]
                + " defense " + beforeDefense + "->" + activeQueueUnit.battleUnit.currentStats[BattleUnit.STAT_DEFENSE]
                + " hp " + beforeHp + "->" + activeQueueUnit.hp
                + " duration=" + activeQueueDuration()
                + " active=" + activeQueueStillActive());
    }

    private void finishActiveQueue(VqsvIntroDemo.Scene s, boolean fullQueueComplete) {
        if (activeQueuePlayerSide) {
            playerActiveQueueProcessedThisRound = true;
        } else {
            enemyActiveQueueProcessedThisRound = true;
        }
        SourceBattleUnit unit = activeQueueUnit;
        boolean playerSide = activeQueuePlayerSide;
        clearActiveQueueState(s);
        if (unit == null) {
            enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, SHORT_WAIT);
            return;
        }
        if (!unit.alive()) {
            if (playerSide) {
                if (hasSwitchPet(s)) {
                    forcedPetSwitch = true;
                    preparePetMenu(s);
                    enterState(s, BattleRuntimeState.P5_PET_SWITCH, VqsvText.Battle.COMMAND_PET_PENDING, SHORT_WAIT);
                } else {
                    enterState(s, BattleRuntimeState.P9_LOSE, VqsvText.Battle.NEIL_LOST + forcedResultIndex, SHORT_WAIT);
                }
            } else {
                if (prepareEnemyReplacement(s)) {
                    return;
                }
                enterState(s, BattleRuntimeState.P8_WIN, battleWinLog(), SHORT_WAIT);
            }
            return;
        }
        if (!fullQueueComplete) {
            enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, SHORT_WAIT);
            return;
        }
        if (playerSide) {
            if (unit.battleUnit != null && unit.battleUnit.hasDebuff(9)) {
                currentActorPlayer = true;
                enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, unit.name, SHORT_WAIT);
            } else {
                currentActorPlayer = true;
                enterCommandState(s, s.battleLog, SHORT_WAIT);
            }
        } else {
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, unit.name, SHORT_WAIT);
        }
    }

    private void prepareActiveQueueSegment(VqsvIntroDemo.Scene s) {
        int speffectId = activeQueueSpeffectId();
        p7SpeffectRow = VqsvBattleAnimationTables.instance().speffectRow(speffectId);
        p7SpecialType = p7SpeffectRow.length > 0 ? p7SpeffectRow[0] : -1;
        if (activeQueueVisualValue(0) == 0) {
            activeQueueActorAnimation = new P7ActorAnimation(activeQueuePlayerSide,
                    activeQueueVisualValue(1), activeQueueVisualValue(2));
            activeQueueActorAnimation.start();
        } else {
            activeQueueActorAnimation = null;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL battle " + state.label
                + " active queue visual bank=" + activeQueueBank
                + " buff=" + activeQueueBuffId
                + " debuff=" + activeQueueDebuffId
                + " segment=" + activeQueueSegment
                + " speffect=" + speffectId
                + " row=" + java.util.Arrays.toString(p7SpeffectRow));
    }

    private int activeQueueEffectId() {
        return activeQueueBank == 0 ? activeQueueBuffId : activeQueueDebuffId;
    }

    private boolean activeQueueNeedsVisual(int bank, int effectId) {
        int[] visualIds = bank == 0 ? new int[]{3, 5, 13} : new int[]{0, 1, 2, 3, 8, 9, 10};
        for (int id : visualIds) {
            if (id == effectId) {
                return true;
            }
        }
        return false;
    }

    private int activeQueueSegmentCount() {
        return activeQueueVisualRow.length / 4;
    }

    private int activeQueueSpeffectId() {
        return activeQueueVisualValue(0) == 1 ? activeQueueVisualValue(1) : -1;
    }

    private int activeQueueVisualValue(int offset) {
        int index = activeQueueSegment * 4 + offset;
        return index >= 0 && index < activeQueueVisualRow.length ? activeQueueVisualRow[index] : -1;
    }

    private String activeQueueVisualLabel() {
        return (activeQueueBank == 0 ? "ap" : "aq")
                + " id=" + activeQueueEffectId()
                + " row=" + Arrays.toString(activeQueueVisualRow);
    }

    private int activeQueueDuration() {
        if (activeQueueUnit == null || activeQueueUnit.battleUnit == null) {
            return 0;
        }
        if (activeQueueBank == 0 && activeQueueBuffId >= 0) {
            return activeQueueUnit.battleUnit.buffSlots[activeQueueBuffId][0];
        }
        if (activeQueueBank == 1 && activeQueueDebuffId >= 0) {
            return activeQueueUnit.battleUnit.debuffSlots[activeQueueDebuffId][0];
        }
        return 0;
    }

    private boolean activeQueueStillActive() {
        if (activeQueueUnit == null || activeQueueUnit.battleUnit == null) {
            return false;
        }
        if (activeQueueBank == 0 && activeQueueBuffId >= 0) {
            return activeQueueUnit.battleUnit.hasBuff(activeQueueBuffId);
        }
        if (activeQueueBank == 1 && activeQueueDebuffId >= 0) {
            return activeQueueUnit.battleUnit.hasDebuff(activeQueueDebuffId);
        }
        return false;
    }

    private int activeQueueSegmentDuration() {
        if (activeQueueVisualValue(0) == 0) {
            return P7_START_TICKS * 4;
        }
        if (p7SpecialType == 9 && p7SpeffectRow.length >= 8) {
            return Math.max(1, p7SpeffectRow[6]);
        }
        if (p7SpecialType == 1 && p7SpeffectRow.length >= 3) {
            return Math.max(1, p7SpeffectRow[2]);
        }
        if (p7SpecialType == 8 && p7SpeffectRow.length >= 3) {
            return Math.max(1, p7SpeffectRow[2]);
        }
        if (p7SpecialType == 12 && p7SpeffectRow.length >= 6) {
            return Math.max(1, p7SpeffectRow[5]);
        }
        return P7_START_TICKS;
    }

    private void syncActiveQueueRenderState(VqsvIntroDemo.Scene s) {
        syncRenderState(s, s.battleLog);
        boolean show = activeQueueUnit != null
                && activeQueueSegment < activeQueueSegmentCount()
                && activeQueueVisualValue(0) == 1
                && p7SpeffectRow.length > 0;
        s.battleP7Ticks = activeQueueTicks;
        s.battleP7SpecialVisible = show;
        s.battleP7SpecialOnPlayerSide = activeQueuePlayerSide;
        s.battleP7SpecialType = show ? p7SpecialType : -1;
        s.battleP7SpecialAlpha = show && p7SpecialType == 9 ? p7SpeffectRow[1] : 0;
        s.battleP7SpecialRed = show && p7SpecialType == 9 ? p7SpeffectRow[2] : 0;
        s.battleP7SpecialGreen = show && p7SpecialType == 9 ? p7SpeffectRow[3] : 0;
        s.battleP7SpecialBlue = show && p7SpecialType == 9 ? p7SpeffectRow[4] : 0;
        s.battleP7SpecialDuration = show ? activeQueueSegmentDuration() : 0;
        s.battleP7SpecialInterval = show && p7SpecialType == 9 && p7SpeffectRow.length >= 8
                ? Math.max(1, p7SpeffectRow[7]) : 1;
        s.battleP7SpecialTextureId = show && p7SpecialType == 1 && p7SpeffectRow.length >= 4
                ? p7SpeffectRow[3] : -1;
        s.battleP7SpecialBlendMode = show && p7SpecialType == 1 && p7SpeffectRow.length >= 5
                ? p7SpeffectRow[4] : 0;
        s.battleP7SpecialScrollMode = show && p7SpecialType == 1 && p7SpeffectRow.length >= 6
                ? p7SpeffectRow[5] : 0;
        s.battleP7SpecialRow = show ? Arrays.copyOf(p7SpeffectRow, p7SpeffectRow.length) : new short[0];
        s.battleP7BaseHiddenPlayerSide = show && activeQueuePlayerSide;
        s.battleP7BaseHiddenEnemySide = show && !activeQueuePlayerSide;
        boolean actorAction = activeQueueActorAnimation != null
                && activeQueueActorAnimation.started()
                && !activeQueueActorAnimation.stopped()
                && activeQueueVisualValue(0) == 0
                && activeQueueSegment < activeQueueSegmentCount();
        s.battleP7ActorEffectVisible = actorAction;
        s.battleP7ActorEffectOnPlayerSide = actorAction && activeQueueActorAnimation.playerSide;
        s.battleP7ActorEffectSpriteId = actorAction ? activeQueueActorAnimation.spriteId : -1;
        s.battleP7ActorEffectState = actorAction ? activeQueueActorAnimation.state : 0;
        s.battleP7ActorEffectCursor = actorAction ? activeQueueActorAnimation.cursor() : 0;
        syncActiveQueueBookkeeping(s);
    }

    private void tickActiveQueueActorAnimation(VqsvIntroDemo.Scene s) {
        if (activeQueueActorAnimation == null || activeQueueActorAnimation.stopped()) {
            return;
        }
        activeQueueActorAnimation.tick();
        if (activeQueueActorAnimation.complete()) {
            activeQueueActorAnimation.stop();
            s.sourceStateTrace.add("PORTED/PARTIAL battle " + state.label
                    + " active queue type0 ah actor action complete effect="
                    + activeQueueVisualValue(1)
                    + " state=" + activeQueueVisualValue(2)
                    + " cursor=" + activeQueueActorAnimation.cursor());
        }
    }

    private boolean activeQueueActorActionComplete() {
        return activeQueueActorAnimation == null || activeQueueActorAnimation.complete()
                || activeQueueActorAnimation.stopped();
    }

    private boolean activeQueueActorTriggerMatched() {
        int trigger = activeQueueVisualValue(3);
        return trigger >= 0
                && activeQueueActorAnimation != null
                && activeQueueActorAnimation.frame(trigger);
    }

    private void syncActiveQueueBookkeeping(VqsvIntroDemo.Scene s) {
        s.battleActiveQueueVisible = activeQueueUnit != null;
        s.battleActiveQueuePlayerSide = activeQueuePlayerSide;
        s.battleActiveQueueBank = activeQueueBank;
        s.battleActiveQueueEffectId = activeQueueBuffId >= 0 ? activeQueueBuffId : activeQueueDebuffId;
        s.battleActiveQueueBuffId = activeQueueBuffId >= 0 ? activeQueueBuffId : activeQueueDebuffId;
        s.battleActiveQueueSegment = activeQueueSegment;
        s.battleActiveQueueTicks = activeQueueTicks;
    }

    private void clearActiveQueueSpecialRenderState(VqsvIntroDemo.Scene s) {
        s.battleP7SpecialVisible = false;
        s.battleP7SpecialOnPlayerSide = false;
        s.battleP7SpecialType = -1;
        s.battleP7SpecialAlpha = 0;
        s.battleP7SpecialRed = 0;
        s.battleP7SpecialGreen = 0;
        s.battleP7SpecialBlue = 0;
        s.battleP7SpecialDuration = 0;
        s.battleP7SpecialInterval = 1;
        s.battleP7SpecialTextureId = -1;
        s.battleP7SpecialBlendMode = 0;
        s.battleP7SpecialScrollMode = 0;
        s.battleP7SpecialRow = new short[0];
        s.battleP7BaseHiddenPlayerSide = false;
        s.battleP7BaseHiddenEnemySide = false;
        s.battleP7BaseStatePlayerSide = 0;
        s.battleP7BaseStateEnemySide = 0;
        s.battleP7BaseCursorPlayerSide = -1;
        s.battleP7BaseCursorEnemySide = -1;
        s.battleP7ActorEffectVisible = false;
        s.battleP7ActorEffectOnPlayerSide = false;
        s.battleP7ActorEffectSpriteId = -1;
        s.battleP7ActorEffectState = 0;
        s.battleP7ActorEffectCursor = 0;
    }

    private void clearActiveQueueState(VqsvIntroDemo.Scene s) {
        activeQueueUnit = null;
        activeQueuePlayerSide = false;
        activeQueueBank = -1;
        activeQueueBuffId = -1;
        activeQueueDebuffId = -1;
        activeQueueSlot = -1;
        Arrays.fill(activeQueueOrderBank, -1);
        Arrays.fill(activeQueueOrderId, -1);
        Arrays.fill(activeQueueOrderSlot, -1);
        activeQueueOrderCount = 0;
        activeQueueOrderIndex = 0;
        activeQueueVisualRow = new byte[0];
        activeQueueSegment = 0;
        activeQueueTicks = 0;
        activeQueueApplied = false;
        activeQueueActorAnimation = null;
        p7SpeffectRow = new short[0];
        p7SpecialType = -1;
        clearActiveQueueSpecialRenderState(s);
        s.battleActiveQueueVisible = false;
        s.battleActiveQueuePlayerSide = false;
        s.battleActiveQueueBank = -1;
        s.battleActiveQueueEffectId = -1;
        s.battleActiveQueueBuffId = -1;
        s.battleActiveQueueSegment = -1;
        s.battleActiveQueueTicks = 0;
    }

    private boolean tickCommand(VqsvIntroDemo.Scene s) {
        queueCommandInput(s);
        if (countdown()) {
            return false;
        }
        if (!handleCommandInput(s)) {
            syncRenderState(s, commandPrompt(s));
            return false;
        }
        switch (s.battleCommandIndex) {
            case 0:
                prepareSkillMenu(s);
                enterState(s, BattleRuntimeState.P3_SKILL_LIST, VqsvText.Battle.COMMAND_FIGHT, SHORT_WAIT);
                break;
            case 1:
                if (battleMode.length > 0 && battleMode[0] == 2) {
                    enterWarning(s, VqsvText.Battle.CATCH_NOT_ALLOWED, BattleRuntimeState.P20_COMMAND);
                } else {
                    prepareCatchMenu(s);
                    enterState(s, BattleRuntimeState.P21_CATCH_LIST, VqsvText.Battle.COMMAND_CATCH_PENDING, SHORT_WAIT);
                }
                break;
            case 2:
                if (playerHasBindStatus()) {
                    enterWarning(s, VqsvText.Battle.ITEM_BIND_WARNING, BattleRuntimeState.P20_COMMAND);
                } else {
                    prepareItemMenu(s);
                    enterState(s, BattleRuntimeState.P4_ITEM_LIST, VqsvText.Battle.COMMAND_ITEM_PENDING, SHORT_WAIT);
                }
                break;
            case 3:
                if (playerHasBindStatus()) {
                    enterWarning(s, VqsvText.Battle.PET_BIND_WARNING, BattleRuntimeState.P20_COMMAND);
                } else {
                    preparePetMenu(s);
                    enterState(s, BattleRuntimeState.P5_PET_SWITCH, VqsvText.Battle.COMMAND_PET_PENDING, SHORT_WAIT);
                }
                break;
            case 4:
                prepareShopMenu(s);
                enterState(s, BattleRuntimeState.P11_SHOP, VqsvText.Battle.COMMAND_SHOP_PENDING, SHORT_WAIT);
                break;
            case 5:
                if (playerHasBindStatus()) {
                    enterWarning(s, VqsvText.Battle.RUN_BIND_WARNING, BattleRuntimeState.P20_COMMAND);
                } else {
                    enterState(s, BattleRuntimeState.P10_RUN, VqsvText.Battle.COMMAND_RUN_PENDING, SHORT_WAIT);
                }
                break;
            default:
                s.battleCommandIndex = 0;
                syncRenderState(s, commandPrompt(s));
                break;
        }
        return false;
    }

    private boolean handleCommandInput(VqsvIntroDemo.Scene s) {
        boolean leftEdge = s.keyLeft && !wasLeftPressed;
        boolean rightEdge = s.keyRight && !wasRightPressed;
        wasLeftPressed = s.keyLeft;
        wasRightPressed = s.keyRight;
        if (leftEdge) {
            s.battleCommandIndex = (s.battleCommandIndex + 5) % 6;
            return false;
        }
        if (rightEdge) {
            s.battleCommandIndex = (s.battleCommandIndex + 1) % 6;
            return false;
        }
        boolean upEdge = s.keyUp && !wasUpPressed;
        boolean downEdge = s.keyDown && !wasDownPressed;
        wasUpPressed = s.keyUp;
        wasDownPressed = s.keyDown;
        if (upEdge) {
            s.battleCommandIndex = (s.battleCommandIndex + 5) % 6;
            return false;
        }
        if (downEdge) {
            s.battleCommandIndex = (s.battleCommandIndex + 1) % 6;
            return false;
        }
        if (commandConfirmQueued || s.key0) {
            commandConfirmQueued = false;
            s.key0 = false;
            return true;
        }
        return false;
    }

    private void queueCommandInput(VqsvIntroDemo.Scene s) {
        int clicked = commandIndexAt(s.battleClickX, s.battleClickY);
        if (clicked >= 0) {
            s.battleCommandIndex = clicked;
            commandConfirmQueued = true;
        } else if (s.key0) {
            commandConfirmQueued = true;
        }
        s.battleClickX = -1;
        s.battleClickY = -1;
    }

    private int commandIndexAt(int x, int y) {
        if (x < 0 || y < 286 || y > 319) {
            return -1;
        }
        int[] centers = {20, 56, 98, 137, 176, 218};
        for (int i = 0; i < centers.length; i++) {
            if (x >= centers[i] - 18 && x <= centers[i] + 18) {
                return i;
            }
        }
        return -1;
    }

    private String commandPrompt(VqsvIntroDemo.Scene s) {
        int index = Math.max(0, Math.min(VqsvText.Battle.COMMAND_PROMPTS.length - 1, s.battleCommandIndex));
        return VqsvText.Battle.COMMAND_PROMPTS[index];
    }

    private MenuAction handleMenuInput(VqsvIntroDemo.Scene s) {
        int clicked = menuIndexAt(s, s.battleClickX, s.battleClickY);
        boolean clickedBack = s.battleClickX >= 144 && s.battleClickX <= 198
                && s.battleClickY >= 232 && s.battleClickY <= 255;
        s.battleClickX = -1;
        s.battleClickY = -1;
        if (clickedBack) {
            s.key0 = false;
            return MenuAction.BACK;
        }
        if (clicked >= 0 && clicked < s.battleMenuNames.length) {
            s.battleMenuIndex = clicked;
            syncMenuScroll(s);
            s.key0 = false;
            return MenuAction.CONFIRM;
        }

        boolean upEdge = s.keyUp && !wasUpPressed;
        boolean downEdge = s.keyDown && !wasDownPressed;
        wasUpPressed = s.keyUp;
        wasDownPressed = s.keyDown;
        if (upEdge && s.battleMenuNames.length > 0) {
            if ("choice".equals(s.battleUiMode)) {
                s.battleChoiceUi = s.battleChoiceUi.moveUpSource();
                syncLegacyMenuFromChoice(s);
            } else {
                s.battleMenuIndex = (s.battleMenuIndex + s.battleMenuNames.length - 1) % s.battleMenuNames.length;
                syncMenuScroll(s);
            }
            return MenuAction.NONE;
        }
        if (downEdge && s.battleMenuNames.length > 0) {
            if ("choice".equals(s.battleUiMode)) {
                s.battleChoiceUi = s.battleChoiceUi.moveDownSource();
                syncLegacyMenuFromChoice(s);
            } else {
                s.battleMenuIndex = (s.battleMenuIndex + 1) % s.battleMenuNames.length;
                syncMenuScroll(s);
            }
            return MenuAction.NONE;
        }
        if (s.key0) {
            s.key0 = false;
            return MenuAction.CONFIRM;
        }
        return MenuAction.NONE;
    }

    private int menuIndexAt(VqsvIntroDemo.Scene s, int x, int y) {
        if ("petstate".equals(s.battleUiMode)) {
            if (x < 45 || x > 197 || y < 86 || y > 176) {
                return -1;
            }
            int index = (y - 86) / 15;
            return index >= 0 && index < 6 ? index : -1;
        }
        if (x < 54 || x > 191 || y < 95 || y > 169) {
            return -1;
        }
        int row = (y - 95) / 15;
        if (row < 0 || row >= 5) {
            return -1;
        }
        return s.battleMenuScroll + row;
    }

    private void syncMenuScroll(VqsvIntroDemo.Scene s) {
        int maxScroll = Math.max(0, s.battleMenuNames.length - 5);
        if (s.battleMenuIndex < s.battleMenuScroll) {
            s.battleMenuScroll = s.battleMenuIndex;
        } else if (s.battleMenuIndex >= s.battleMenuScroll + 5) {
            s.battleMenuScroll = s.battleMenuIndex - 4;
        }
        s.battleMenuScroll = Math.max(0, Math.min(maxScroll, s.battleMenuScroll));
        if ("choice".equals(s.battleUiMode)) {
            s.battleChoiceUi = s.battleChoiceUi.withSourceCursor(s.battleMenuIndex, s.battleMenuScroll);
            syncLegacyMenuFromChoice(s);
        }
    }

    private void syncLegacyMenuFromChoice(VqsvIntroDemo.Scene s) {
        s.battleMenuIndex = s.battleChoiceUi.selectedIndex;
        s.battleMenuScroll = s.battleChoiceUi.scroll;
    }

    private MenuAction handleSkillInput(VqsvIntroDemo.Scene s) {
        int clicked = skillIndexAt(s.battleClickX, s.battleClickY, s);
        boolean clickedBack = s.battleClickX >= 152 && s.battleClickX <= 198
                && s.battleClickY >= 232 && s.battleClickY <= 255;
        s.battleClickX = -1;
        s.battleClickY = -1;
        if (clickedBack) {
            s.key0 = false;
            return MenuAction.BACK;
        }
        if (clicked >= 0) {
            s.battleSkillIndex = clicked;
            updateSkillScrollAndDescription(s);
            s.key0 = false;
            return MenuAction.CONFIRM;
        }

        boolean upEdge = s.keyUp && !wasUpPressed;
        boolean downEdge = s.keyDown && !wasDownPressed;
        wasUpPressed = s.keyUp;
        wasDownPressed = s.keyDown;
        if (upEdge && s.battleSkillIds.length > 0) {
            s.battleSkillIndex = (s.battleSkillIndex + s.battleSkillIds.length - 1) % s.battleSkillIds.length;
            updateSkillScrollAndDescription(s);
            return MenuAction.NONE;
        }
        if (downEdge && s.battleSkillIds.length > 0) {
            s.battleSkillIndex = (s.battleSkillIndex + 1) % s.battleSkillIds.length;
            updateSkillScrollAndDescription(s);
            return MenuAction.NONE;
        }
        if (s.key0) {
            s.key0 = false;
            return MenuAction.CONFIRM;
        }
        return MenuAction.NONE;
    }

    private int skillIndexAt(int x, int y, VqsvIntroDemo.Scene s) {
        if (x < 54 || x > 184 || y < 94 || y > 170) {
            return -1;
        }
        int visible = (y - 94) / 15;
        if (visible < 0 || visible >= 5) {
            return -1;
        }
        int index = s.battleSkillScroll + visible;
        return index >= 0 && index < s.battleSkillIds.length ? index : -1;
    }

    private void updateSkillScrollAndDescription(VqsvIntroDemo.Scene s) {
        if (s.battleSkillIds.length == 0) {
            s.battleSkillIndex = 0;
            s.battleSkillScroll = 0;
            s.battleSkillDescription = "";
            return;
        }
        s.battleSkillIndex = Math.max(0, Math.min(s.battleSkillIds.length - 1, s.battleSkillIndex));
        if (s.battleSkillIndex < s.battleSkillScroll) {
            s.battleSkillScroll = s.battleSkillIndex;
        } else if (s.battleSkillIndex >= s.battleSkillScroll + 5) {
            s.battleSkillScroll = s.battleSkillIndex - 4;
        }
        s.battleSkillScroll = Math.max(0, Math.min(Math.max(0, s.battleSkillIds.length - 5), s.battleSkillScroll));
        BattleSkillRow row = VqsvBattleTables.instance().skill(s.battleSkillIds[s.battleSkillIndex]);
        s.battleSkillDescription = row == null ? "" : row.description("");
    }

    private void prepareCatchMenu(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> icons = new java.util.ArrayList<>();
        for (BagItem item : s.sourceBagItems.values()) {
            BattleItemRow row = VqsvBattleTables.instance().item(item.id);
            boolean sourceListedCatchRow = item.count > 0 || (item.count == 0 && !item.keepAtZero);
            if (sourceListedCatchRow && row != null && row.behavior == 0) {
                ids.add(item.id);
                icons.add(row.iconId);
                names.add(itemName(item.id));
                values.add(catchChance(item.id) + "%");
            }
        }
        if (ids.isEmpty() && isBunnyCaptureBattle()) {
            ids.add(0);
            BattleItemRow row = VqsvBattleTables.instance().item(0);
            icons.add(row == null ? -1 : row.iconId);
            names.add(itemName(0));
            values.add("100%");
            s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
            s.sourceStateTrace.add("PORTED/APPROX battle tutorial seeded source ball item=0 for P21 path");
        }
        setMenu(s, "Pokemon ball", "T\u1ec9 l\u1ec7 b\u1eaft", "S\u1eed d\u1ee5ng", names, values, ids, icons);
        if (isBunnyCaptureBattle() && bunnyTutorialFirstCatchPending && !bunnyTutorialRetryPending) {
            if (bunnyTutorialU == 0 && bunnyTutorialV < 4) {
                setBunnyTutorialState(s, 0, 4, "game.d.l() V=3 choose Phong an cau prompt/list");
            }
            selectCatchMenuItem(s, 1);
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial U=0,V=3 guide P21 cursor item=1 Phong an cau");
        } else if (isBunnyCaptureBattle() && bunnyTutorialRetryPending) {
            if (bunnyTutorialU == 0 && bunnyTutorialV < 7) {
                setBunnyTutorialState(s, 0, 7, "game.d.l() V=6 re-enter P21 retry");
            }
            selectCatchMenuItem(s, 0);
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial U=0,V=6 retry P21 cursor item=0 Tat Trung Cau");
        }
    }

    private void selectCatchMenuItem(VqsvIntroDemo.Scene s, int itemId) {
        for (int i = 0; i < s.battleMenuIds.length; i++) {
            if (s.battleMenuIds[i] == itemId) {
                s.battleMenuIndex = i;
                syncMenuScroll(s);
                return;
            }
        }
    }

    private void prepareSkillMenu(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> ppLabels = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        BattleUnit unit = player.battleUnit;
        if (unit != null) {
            for (int i = 0; i < unit.skillIds.length; i++) {
                int skillId = unit.skillAt(i);
                if (skillId < 0) {
                    continue;
                }
                BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
                ids.add(skillId);
                names.add(row == null ? "Skill " + skillId : row.name("Skill " + skillId));
                int ppMax = row == null ? 0 : row.ppMax;
                ppLabels.add(unit.skillPpAt(i) + "/" + ppMax);
            }
        }
        s.battleUiMode = "choiceskill";
        s.battleSkillNames = names.toArray(new String[0]);
        s.battleSkillPpLabels = ppLabels.toArray(new String[0]);
        s.battleSkillIds = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            s.battleSkillIds[i] = ids.get(i);
        }
        if (s.battleSkillIndex < 0 || s.battleSkillIndex >= s.battleSkillIds.length) {
            s.battleSkillIndex = 0;
        }
        updateSkillScrollAndDescription(s);
        s.sourceStateTrace.add("PORTED battle P3 choiceskill.ui open skills="
                + java.util.Arrays.toString(s.battleSkillIds)
                + " pp=" + java.util.Arrays.toString(s.battleSkillPpLabels));
    }

    private void prepareItemMenu(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<String> descriptions = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> icons = new java.util.ArrayList<>();
        for (BagItem item : s.sourceBagItems.values()) {
            BattleItemRow row = VqsvBattleTables.instance().item(item.id);
            if (item.count > 0 && row != null && row.behavior != 0) {
                ids.add(item.id);
                icons.add(row.iconId);
                names.add(itemName(item.id));
                values.add(String.valueOf(item.count));
                descriptions.add(row.description(""));
            }
        }
        setMenu(s, "\u0110\u1ea1o c\u1ee5", "S\u1ed1 l\u01b0\u1ee3ng", "S\u1eed d\u1ee5ng",
                names, values, descriptions, ids, icons);
    }

    private void prepareItemTargetMenu(VqsvIntroDemo.Scene s) {
        prepareItemTargetMenu(s, true);
    }

    private void prepareItemTargetMenu(VqsvIntroDemo.Scene s, boolean resetCursor) {
        if (!s.sourcePets.isEmpty() && player != null && player.battleUnit != null) {
            s.sourcePets.get(0).persistBattleUnit(player.battleUnit);
        }
        if (resetCursor) {
            s.battleMenuIndex = 0;
            s.battleMenuScroll = 0;
        }
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        ensureSourcePetOrder(s);
        for (int row = 0; row < sourcePetOrder.length; row++) {
            int i = sourcePetOrder[row];
            if (i < 0 || i >= s.sourcePets.size()) {
                continue;
            }
            SourceBattleUnit unit = i == 0 && player != null
                    ? player
                    : SourceBattleUnit.playerFromSourcePets(s.sourcePets.subList(i, i + 1));
            ids.add(i);
            names.add(unit.name);
            values.add((unit.alive() ? "lv" + unit.level : "KO") + " " + unit.hp + "/" + unit.maxHp);
        }
        setMenu(s, VqsvText.Battle.PETSTATE_TITLE, itemName(selectedItemId),
                VqsvText.Battle.PETSTATE_USE, names, values, ids);
        s.battlePetStateRows = buildPetStateRows(s);
        s.battleUiMode = "petstate";
        s.battleUiModeStartTick = s.battleAnimationTick;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P16 petstate.ui open source=game.h.W/al/bo"
                + " resetCursor=" + resetCursor
                + " selectedItem=" + selectedItemId
                + " sourcePetOrder=" + java.util.Arrays.toString(sourcePetOrder)
                + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                + " names=" + java.util.Arrays.toString(s.battleMenuNames));
    }

    private void preparePetMenu(VqsvIntroDemo.Scene s) {
        preparePetMenu(s, true);
    }

    private void preparePetMenu(VqsvIntroDemo.Scene s, boolean resetCursor) {
        if (!s.sourcePets.isEmpty() && player != null && player.battleUnit != null) {
            s.sourcePets.get(0).persistBattleUnit(player.battleUnit);
        }
        if (resetCursor) {
            s.battleMenuIndex = 0;
            s.battleMenuScroll = 0;
        }
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        ensureSourcePetOrder(s);
        for (int row = 0; row < sourcePetOrder.length; row++) {
            int i = sourcePetOrder[row];
            if (i < 0 || i >= s.sourcePets.size()) {
                continue;
            }
            SourceBattleUnit unit = i == 0 && player != null
                    ? player
                    : SourceBattleUnit.playerFromSourcePets(s.sourcePets.subList(i, i + 1));
            ids.add(i);
            names.add(unit.name);
            values.add((unit.alive() ? "lv" + unit.level : "KO") + " " + unit.hp + "/" + unit.maxHp);
        }
        setMenu(s, "S\u1ee7ng v\u1eadt", "Thay \u0111\u1ed5i", VqsvText.Battle.PETSTATE_DEPLOY,
                names, values, ids);
        s.battlePetStateRows = buildPetStateRows(s);
        s.battleUiMode = "petstate";
        s.battleUiModeStartTick = s.battleAnimationTick;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P5 petstate.ui open sourceProxy=sourcePets-as-game.d.f"
                + " resetCursor=" + resetCursor
                + " forced=" + forcedPetSwitch
                + " sourcePetOrder=" + java.util.Arrays.toString(sourcePetOrder)
                + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                + " names=" + java.util.Arrays.toString(s.battleMenuNames));
    }

    private VqsvBattlePetStateView[] buildPetStateRows(VqsvIntroDemo.Scene s) {
        VqsvBattlePetStateView[] rows = new VqsvBattlePetStateView[6];
        for (int row = 0; row < rows.length; row++) {
            if (row >= s.battleMenuIds.length) {
                rows[row] = VqsvBattlePetStateView.empty(row);
                continue;
            }
            int petIndex = s.battleMenuIds[row];
            if (petIndex < 0 || petIndex >= s.sourcePets.size()) {
                rows[row] = VqsvBattlePetStateView.empty(row);
                continue;
            }
            rows[row] = VqsvBattlePetStateView.fromPet(row, petIndex, s.sourcePets.get(petIndex),
                    s.sourcePets.get(petIndex).sourceK());
        }
        return rows;
    }

    private boolean hasSwitchPet(VqsvIntroDemo.Scene s) {
        for (int i = 1; i < s.sourcePets.size(); i++) {
            if (sourcePetAlive(s.sourcePets.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void prepareShopMenu(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> icons = new java.util.ArrayList<>();
        int limit = Math.min(5, VqsvBattleTables.instance().rowCount(4));
        for (int i = 0; i < limit; i++) {
            BattleItemRow row = VqsvBattleTables.instance().item(i);
            if (row == null) {
                continue;
            }
            ids.add(i);
            icons.add(row.iconId);
            names.add(itemName(i));
            values.add(String.valueOf(i == 0 ? row.priceOrValue : row.priceOrValue << 1));
        }
        setMenu(s, "Mua s\u1eafm", "Gi\u00e1", "Mua", names, values, ids, icons);
    }

    private void setMenu(VqsvIntroDemo.Scene s, String title, String subtitle, String action,
                         java.util.List<String> names, java.util.List<String> values,
                         java.util.List<Integer> ids) {
        setMenu(s, title, subtitle, action, names, values, ids, java.util.Collections.emptyList());
    }

    private void setMenu(VqsvIntroDemo.Scene s, String title, String subtitle, String action,
                         java.util.List<String> names, java.util.List<String> values,
                         java.util.List<Integer> ids, java.util.List<Integer> icons) {
        setMenu(s, title, subtitle, action, names, values, java.util.Collections.emptyList(), ids, icons);
    }

    private void setMenu(VqsvIntroDemo.Scene s, String title, String subtitle, String action,
                         java.util.List<String> names, java.util.List<String> values,
                         java.util.List<String> descriptions, java.util.List<Integer> ids,
                         java.util.List<Integer> icons) {
        s.battleUiMode = "choice";
        s.battleMenuTitle = title;
        s.battleMenuSubtitle = subtitle;
        s.battleMenuAction = action;
        s.battleMenuNames = names.toArray(new String[0]);
        s.battleMenuValues = values.toArray(new String[0]);
        s.battleMenuDescriptions = descriptions.toArray(new String[0]);
        s.battleMenuIds = new int[ids.size()];
        s.battleMenuIconIds = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            s.battleMenuIds[i] = ids.get(i);
            s.battleMenuIconIds[i] = i < icons.size() ? icons.get(i) : -1;
        }
        if (s.battleMenuIndex < 0 || s.battleMenuIndex >= s.battleMenuNames.length) {
            s.battleMenuIndex = 0;
        }
        s.battleChoiceUi = VqsvChoiceUiView.battle(title, subtitle, action,
                names, values, descriptions, ids, icons, s.battleMenuIndex, s.battleMenuScroll);
        syncMenuScroll(s);
    }

    private String itemName(int itemId) {
        BattleItemRow row = VqsvBattleTables.instance().item(itemId);
        return row == null ? "Item " + itemId : row.name("Item " + itemId);
    }

    private int catchChance(int itemId) {
        if (itemId == 0) {
            return 100;
        }
        BattleItemRow item = VqsvBattleTables.instance().item(itemId);
        int chance = 1;
        if (enemy.hp <= enemy.maxHp * 15 / 100) {
            chance = 85;
        } else if (enemy.hp <= enemy.maxHp * 50 / 100) {
            chance = 45;
        } else if (enemy.hp <= enemy.maxHp) {
            chance = 20;
        }
        chance = chance * (item == null ? 100 : item.paramA) / 100;
        int[] quality = {110, 100, 95, 80, 70};
        int natureIndex = Math.max(0, Math.min(quality.length - 1, enemy.nature - 1));
        chance = chance * quality[natureIndex] / 100;
        int[] statusMultiplier = {10, 11, 12, 12, 12};
        int statusIndex = catchStatusIndex();
        chance = chance * statusMultiplier[statusIndex] / 10;
        if (catchAttackerHasForm11()) {
            int bonus = player != null && player.battleUnit != null
                    ? player.battleUnit.sourceStatusParam(11, 5, 0)
                    : 0;
            chance = chance * (100 + bonus) / 100;
        }
        int[] relation = {1000, 500, 1, 1000};
        int relationIndex = Math.max(0, Math.min(relation.length - 1, enemy.relationClass));
        chance = chance * relation[relationIndex] / 1000;
        if (enemy.level >= 20) {
            int[] caps = {0, 15, 35, 65};
            int capIndex = Math.max(0, Math.min(caps.length - 1, itemId));
            if (chance >= caps[capIndex]) {
                chance = caps[capIndex];
            }
        }
        return Math.max(1, Math.min(100, chance));
    }

    private int catchStatusIndex() {
        int status = 0;
        if (catchTargetHasDebuff(1)) {
            status = 1;
        }
        if (catchTargetHasDebuff(2)) {
            status = 2;
        }
        if (catchTargetHasDebuff(10)) {
            status = 3;
        }
        if (catchAttackerHasForm11()) {
            status = 4;
        }
        return status;
    }

    private boolean catchTargetHasDebuff(int debuffId) {
        return enemy != null && enemy.battleUnit != null && enemy.battleUnit.hasDebuff(debuffId);
    }

    private boolean catchAttackerHasForm11() {
        return player != null && player.battleUnit != null && player.battleUnit.hasSourceFormStatus(11);
    }

    private int runChancePercent() {
        if (player.speed > enemy.speed) {
            return 100;
        }
        if (player.speed == enemy.speed) {
            return 95;
        }
        return Math.max(15, 95 - (enemy.speed - player.speed) * 10);
    }

    private boolean playerHasBindStatus() {
        return player.battleUnit != null && player.battleUnit.hasDebuff(2);
    }

    private boolean tickSkillList(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        s.battleUiMode = "choiceskill";
        MenuAction action = handleSkillInput(s);
        if (action == MenuAction.BACK) {
            forcedPetSwitch = false;
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.COMMAND_FIGHT);
            return false;
        }
        if (s.battleSkillIds.length == 0 || player.battleUnit == null) {
            enterWarning(s, VqsvText.Battle.SKILL_EMPTY, BattleRuntimeState.P3_SKILL_LIST);
            return false;
        }
        selectedSkillId = s.battleSkillIds[s.battleSkillIndex];
        int slot = skillSlot(player.battleUnit, selectedSkillId);
        if (slot < 0 || player.battleUnit.skillPpAt(slot) <= 0) {
            enterWarning(s, VqsvText.Battle.SKILL_NO_PP, BattleRuntimeState.P3_SKILL_LIST);
            return false;
        }
        BattleSkillRow row = VqsvBattleTables.instance().skill(selectedSkillId);
        prepareTargetList(s, player, selectedSkillId);
        s.sourceStateTrace.add("PORTED battle P3 confirm skill=" + selectedSkillId
                + " name=" + (row == null ? "Skill " + selectedSkillId : row.name("Skill " + selectedSkillId))
                + " ppBefore=" + player.battleUnit.skillPpAt(slot)
                + " targetSide=" + (row == null ? -1 : row.targetSide)
                + " targetCount=" + targetUnits.length
                + " formation=" + sourceFormation());
        if (targetUnits.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_PET_TARGET, BattleRuntimeState.P3_SKILL_LIST);
            return false;
        }
        if (sourceFormation() == 0) {
            commitSelectedTarget(s, 0);
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE,
                    row == null ? player.name : row.name("Skill " + selectedSkillId),
                    SHORT_WAIT);
        } else {
            enterState(s, BattleRuntimeState.P6_TARGET_SELECT,
                    row == null ? player.name : row.name("Skill " + selectedSkillId),
                    SHORT_WAIT);
        }
        return false;
    }

    private void prepareTargetList(VqsvIntroDemo.Scene s, SourceBattleUnit actor, int skillId) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        int targetSide = row == null ? 0 : row.targetSide;
        java.util.ArrayList<SourceBattleUnit> units = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> slots = new java.util.ArrayList<>();
        boolean actorIsPlayer = actor == player;
        if (targetSide == 1) {
            SourceBattleUnit sameSide = actorIsPlayer ? player : enemy;
            if (sameSide.alive()) {
                units.add(sameSide);
                slots.add(actorIsPlayer ? 1 : 0);
            }
        } else {
            SourceBattleUnit otherSide = actorIsPlayer ? enemy : player;
            if (otherSide.alive()) {
                units.add(otherSide);
                slots.add(actorIsPlayer ? 0 : 1);
            }
        }
        targetUnits = units.toArray(new SourceBattleUnit[0]);
        targetSlots = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            targetSlots[i] = slots.get(i);
        }
        selectedTargetIndex = Math.max(0, Math.min(selectedTargetIndex, Math.max(0, targetUnits.length - 1)));
        syncTargetRenderState(s);
        s.sourceStateTrace.add("PORTED battle target vector game.d.b(skill): skill=" + skillId
                + " targetSide=" + targetSide
                + " slots=" + java.util.Arrays.toString(targetSlots)
                + " names=" + java.util.Arrays.toString(s.battleTargetNames));
    }

    private int sourceFormation() {
        return battleMode.length > 0 ? battleMode[0] : 0;
    }

    private void syncTargetRenderState(VqsvIntroDemo.Scene s) {
        s.battleTargetCount = targetUnits.length;
        s.battleTargetIndex = Math.max(0, Math.min(selectedTargetIndex, Math.max(0, targetUnits.length - 1)));
        s.battleTargetNames = new String[targetUnits.length];
        s.battleTargetSlots = java.util.Arrays.copyOf(targetSlots, targetSlots.length);
        s.battleTargetPlayerSide = false;
        for (int i = 0; i < targetUnits.length; i++) {
            s.battleTargetNames[i] = targetUnits[i].name;
            if (i == s.battleTargetIndex) {
                s.battleTargetPlayerSide = targetUnits[i] == player;
            }
        }
    }

    private boolean tickTargetSelect(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        s.battleUiMode = "target";
        MenuAction action = handleTargetInput(s);
        syncTargetRenderState(s);
        if (action == MenuAction.BACK) {
            prepareSkillMenu(s);
            enterState(s, BattleRuntimeState.P3_SKILL_LIST, VqsvText.Battle.COMMAND_FIGHT, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, s.battleLog);
            return false;
        }
        if (targetUnits.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_PET_TARGET, BattleRuntimeState.P3_SKILL_LIST);
            return false;
        }
        commitSelectedTarget(s, selectedTargetIndex);
        BattleSkillRow row = VqsvBattleTables.instance().skill(selectedSkillId);
        enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE,
                row == null ? player.name : row.name("Skill " + selectedSkillId),
                SHORT_WAIT);
        return false;
    }

    private MenuAction handleTargetInput(VqsvIntroDemo.Scene s) {
        int clicked = targetIndexAt(s.battleClickX, s.battleClickY);
        boolean clickedBack = s.battleClickX >= 0 && s.battleClickY >= 286
                && s.battleClickX <= 38 && s.battleClickY <= 319;
        s.battleClickX = -1;
        s.battleClickY = -1;
        if (clickedBack || s.keyLeft && !wasLeftPressed && targetUnits.length <= 1) {
            wasLeftPressed = s.keyLeft;
            if (clickedBack) {
                s.key0 = false;
            }
            return clickedBack ? MenuAction.BACK : MenuAction.NONE;
        }
        if (clicked >= 0) {
            selectedTargetIndex = clicked;
            s.key0 = false;
            return MenuAction.CONFIRM;
        }
        boolean previousEdge = (s.keyLeft && !wasLeftPressed) || (s.keyUp && !wasUpPressed);
        boolean nextEdge = (s.keyRight && !wasRightPressed) || (s.keyDown && !wasDownPressed);
        wasLeftPressed = s.keyLeft;
        wasRightPressed = s.keyRight;
        wasUpPressed = s.keyUp;
        wasDownPressed = s.keyDown;
        if (previousEdge && targetUnits.length > 0) {
            selectedTargetIndex = (selectedTargetIndex + targetUnits.length - 1) % targetUnits.length;
            return MenuAction.NONE;
        }
        if (nextEdge && targetUnits.length > 0) {
            selectedTargetIndex = (selectedTargetIndex + 1) % targetUnits.length;
            return MenuAction.NONE;
        }
        if (s.key0) {
            s.key0 = false;
            return MenuAction.CONFIRM;
        }
        return MenuAction.NONE;
    }

    private int targetIndexAt(int x, int y) {
        if (x >= 132 && x <= 228 && y >= 70 && y <= 188) {
            return targetIndexForUnit(enemy);
        }
        if (x >= 18 && x <= 114 && y >= 140 && y <= 235) {
            return targetIndexForUnit(player);
        }
        return -1;
    }

    private int targetIndexForUnit(SourceBattleUnit unit) {
        for (int i = 0; i < targetUnits.length; i++) {
            if (targetUnits[i] == unit) {
                return i;
            }
        }
        return -1;
    }

    private void commitSelectedTarget(VqsvIntroDemo.Scene s, int index) {
        selectedTargetIndex = Math.max(0, Math.min(index, Math.max(0, targetUnits.length - 1)));
        selectedTarget = targetUnits[selectedTargetIndex];
        if (player.battleUnit != null && selectedTarget != null && selectedTarget.battleUnit != null) {
            player.battleUnit.selectSkill(selectedSkillId, selectedTarget.battleUnit);
            player.battleUnit.selectedTargetSlot = (byte) (selectedTargetIndex < targetSlots.length
                    ? targetSlots[selectedTargetIndex] : -1);
        }
        syncTargetRenderState(s);
        s.sourceStateTrace.add("PORTED battle P6/game.d.i target confirm skill=" + selectedSkillId
                + " targetIndex=" + selectedTargetIndex
                + " targetSlot=" + (selectedTargetIndex < targetSlots.length ? targetSlots[selectedTargetIndex] : -1)
                + " target=" + (selectedTarget == null ? "none" : selectedTarget.name));
    }

    private int skillSlot(BattleUnit unit, int skillId) {
        for (int i = 0; i < unit.skillIds.length; i++) {
            if (unit.skillAt(i) == skillId) {
                return i;
            }
        }
        return -1;
    }

    private boolean tickCatchList(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (s.keyBack) {
            s.keyBack = false;
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        MenuAction action = handleMenuInput(s);
        if (action == MenuAction.BACK) {
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.COMMAND_CATCH_PENDING);
            return false;
        }
        if (s.battleMenuIds.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_BALLS, BattleRuntimeState.P21_CATCH_LIST);
            return false;
        }
        int itemId = s.battleMenuIds[s.battleMenuIndex];
        BagItem item = s.sourceBagItems.get(itemId);
        if (item == null || item.count <= 0) {
            if (itemId == 0) {
                VqsvSourceOps.sourceAddItem(s, itemId, 1);
                item = s.sourceBagItems.get(itemId);
                s.sourceStateTrace.add("PORTED/REBUILD_POLICY battle P21/P101 SMS purchase bypass item=0"
                        + " sourcePath=game.h.ai f=1 -> game.d.a(101) -> game.h.aH/aM"
                        + " grant=1 count=" + (item == null ? -1 : item.count));
            } else {
                s.sourceStateTrace.add("PORTED/PARTIAL battle P21 game.h.ai missing-count msgwarm.ui item="
                        + itemId + " count=" + (item == null ? -1 : item.count)
                        + "; item0 SMS-free hook not taken");
                enterWarning(s, VqsvText.Battle.NO_BALLS, BattleRuntimeState.P21_CATCH_LIST);
                return false;
            }
        }
        VqsvSourceOps.sourceRemoveItem(s, itemId, 1);
        selectedItemId = itemId;
        if (isBunnyCaptureBattle() && bunnyTutorialFirstCatchPending && itemId == 1) {
            bunnyTutorialFirstCatchPending = false;
            bunnyTutorialForceFailActive = true;
            setBunnyTutorialState(s, 0, 5, "game.d.m() V=4->5 first ball confirm force P17 fail");
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial game.d.m() U=0,V=4->5 first item=1 will force P17 fail");
        } else if (isBunnyCaptureBattle() && bunnyTutorialRetryPending && itemId == 0) {
            bunnyTutorialRetryPending = false;
            setBunnyTutorialState(s, 0, 8, "game.d.m() V=7->8 retry ball confirm");
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial game.d.m() U=0,V=7->8 retry item=0");
        }
        initCatchResult(s, itemId);
        enterState(s, BattleRuntimeState.P17_CATCH_RESULT, VqsvText.Battle.BALL_CHOSEN, 0);
        return false;
    }

    private boolean tickCatchResult(VqsvIntroDemo.Scene s) {
        if (catchOpenBoxState != 0) {
            clearCatchVisuals(s);
            if (tickCatchOpenBox(s)) {
                enterState(s, BattleRuntimeState.P8_WIN, battleWinLog(), SHORT_WAIT);
            }
            return false;
        }
        syncCatchRenderState(s, catchPhase);
        boolean animEnded = catchAnimAtLastFrame();
        if (catchPhase == 0 && animEnded) {
            advanceCatchPhase(s, 1, VqsvText.Battle.BALL_CHOSEN);
        } else if (catchPhase == 1 && animEnded && tickCatchEffectSourceLike()) {
            advanceCatchPhase(s, 2, VqsvText.Battle.BALL_CHOSEN);
        } else if (catchPhase == 2 && animEnded) {
            advanceCatchPhase(s, catchCaught ? 3 : 4,
                    catchCaught ? VqsvText.Battle.CATCH_SUCCESS + enemy.name : VqsvText.Battle.CATCH_FAILED);
        } else if (catchPhase == 3 && animEnded) {
            clearCatchVisuals(s);
            if (isBunnyCaptureBattle() && bunnyTutorialU == 0 && bunnyTutorialV == 8) {
                setBunnyTutorialState(s, -1, 0, "game.d.l() V=8 cleanup after Bunny catch success");
            }
            applyCatchStorage(s);
            setHp(enemy, 0);
            openCatchResultBox(s, catchWinLog, "game.d P17 q=3 S.b(openbox.ui)");
            return false;
        } else if (catchPhase == 4 && animEnded && tickCatchEffectSourceLike()) {
            clearCatchVisuals(s);
            if (isBunnyCaptureBattle() && bunnyTutorialForceFailActive) {
                bunnyTutorialForceFailActive = false;
                bunnyTutorialRetryPending = true;
                bunnyTutorialRetryPromptAfterEnemy = true;
                playerActionThisRound = true;
                s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial U=0,V=5 first catch failed; "
                        + "queue enemy action before retry taskTip");
                enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.CATCH_FAILED, SHORT_WAIT);
                return false;
            }
            playerActionThisRound = true;
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.CATCH_FAILED, SHORT_WAIT);
            return false;
        }
        tickCatchObjects();
        syncCatchRenderState(s, catchPhase);
        return false;
    }

    private boolean tickBunnyTutorialWeakPrompt(VqsvIntroDemo.Scene s) {
        syncRenderState(s, VqsvText.Battle.BUNNY_WEAK);
        if (s.text != null && s.text.readyForKey && s.key0) {
            s.text.confirm();
            s.key0 = false;
            if (s.text == null || s.text.disposed) {
                s.text = null;
                bunnyTutorialWeakPromptActive = false;
                setBunnyTutorialState(s, 0, 3, "game.d.l() V=1 prompt closed; guide catch command");
                s.battleCommandIndex = 1;
                s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial U=0,V=1 weak taskTip closed, "
                        + "return P20 with catch command selected");
                enterCommandState(s, VqsvText.Battle.BUNNY_WEAK, SHORT_WAIT);
            }
        }
        return false;
    }

    private boolean tickBunnyTutorialRetryPrompt(VqsvIntroDemo.Scene s) {
        syncRenderState(s, VqsvText.Battle.BUNNY_RETRY_TAT_TRUNG_CAU);
        if (s.text != null && s.text.readyForKey && s.key0) {
            s.text.confirm();
            s.key0 = false;
            if (s.text == null || s.text.disposed) {
                s.text = null;
                bunnyTutorialRetryPromptActive = false;
                setBunnyTutorialState(s, 0, 7, "game.d.l() V=6 taskTip closed");
                prepareCatchMenu(s);
                s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial U=0,V=6 prompt closed, re-enter P21");
                enterState(s, BattleRuntimeState.P21_CATCH_LIST, VqsvText.Battle.BUNNY_RETRY_TAT_TRUNG_CAU, SHORT_WAIT);
            }
        }
        return false;
    }

    private void initCatchResult(VqsvIntroDemo.Scene s, int itemId) {
        catchPhase = 0;
        catchPhaseTicks = 0;
        catchChance = catchChance(itemId);
        catchRoll = sourceCatchRollPercent(s);
        catchCaught = catchRoll < catchChance;
        if (bunnyTutorialForceFailActive) {
            catchCaught = false;
        }
        catchTraceWritten = false;
        catchWinLog = null;
        catchStorageResult = -1;
        catchOpenBoxState = 0;
        catchAnim = SpriteAnim.load(269);
        setCatchAnimState(0, false);
        clearCatchEffect();
        s.battleUiMode = "catch_anim";
        s.battleCatchSpriteId = 269;
        s.battleCatchItemId = itemId;
        s.battleCatchChance = catchChance;
        s.battleCatchRoll = catchRoll;
        s.battleCatchCaught = catchCaught;
        s.battleCatchVisible = true;
        syncCatchRenderState(s, catchPhase);
        s.sourceStateTrace.add("PORTED battle P21 confirm item=" + itemId
                + " consumed=1 next=P17 sprite=269 chance=" + catchChance
                + " roll=" + catchRoll
                + " caught=" + catchCaught
                + " forceFail=" + bunnyTutorialForceFailActive);
    }

    private int sourceCatchRollPercent(VqsvIntroDemo.Scene s) {
        if (debugNextCatchRoll >= 0) {
            int roll = Math.max(0, Math.min(99, debugNextCatchRoll));
            debugNextCatchRoll = -1;
            s.sourceStateTrace.add("RNG TRACE battle.P17.catch helper=debug-forced bound=100 raw=forced return="
                    + roll + " seed=none");
            return roll;
        }
        return SOURCE_RANDOM.a("battle.P17.catch", 100, s.sourceStateTrace);
    }

    private void advanceCatchPhase(VqsvIntroDemo.Scene s, int nextPhase, String log) {
        catchPhase = nextPhase;
        catchPhaseTicks = 0;
        if (nextPhase == 1) {
            setCatchAnimState(1, true);
            startCatchEffect(new int[][]{{10, 0, 0}, {7, 0, -10}, {4, 0, -20}}, 9, false);
        } else if (nextPhase == 2) {
            setCatchAnimState(2, false);
            clearCatchEffect();
        } else if (nextPhase == 3) {
            setCatchAnimState(3, true);
            clearCatchEffect();
        } else if (nextPhase == 4) {
            setCatchAnimState(1, true);
            startCatchEffect(new int[][]{{4, 0, -20}, {6, 0, -12}, {8, 0, -4}, {10, 0, 0}}, 8, true);
        }
        syncCatchRenderState(s, nextPhase);
        syncRenderState(s, log);
    }

    private void syncCatchRenderState(VqsvIntroDemo.Scene s, int phase) {
        s.battleUiMode = "catch_anim";
        s.battleCatchVisible = true;
        s.battleCatchPhase = phase;
        s.battleCatchTicks = catchPhaseTicks;
        s.battleCatchSpriteId = 269;
        s.battleCatchAnimCursor = catchAnim == null ? 0 : catchAnim.cursor;
        s.battleCatchItemId = selectedItemId;
        s.battleCatchChance = catchChance;
        s.battleCatchRoll = catchRoll;
        s.battleCatchCaught = catchCaught;
        s.battleEnemyHiddenByCatch = phase >= 1;
        syncCatchEffectRender(s);
        if (!catchTraceWritten && phase == 0) {
            catchTraceWritten = true;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P17 source-timed q=0..4 item=" + selectedItemId
                    + " chance=" + catchChance
                    + " roll=" + catchRoll
                    + " decision=ae.a(100)<chance"
                    + " forceFail=" + bunnyTutorialForceFailActive
                    + " caught=" + catchCaught
                    + " sprite=269 from source f.aj; H/ah type8 source scale/offset/timing ported");
        }
    }

    private void setCatchAnimState(int state, boolean holdLast) {
        catchAnimHoldLast = holdLast;
        if (catchAnim != null) {
            catchAnim.setState(state);
        }
    }

    private boolean catchAnimAtLastFrame() {
        if (catchAnim == null || catchAnim.data.anim == null
                || catchAnim.state < 0 || catchAnim.state >= catchAnim.data.anim.length) {
            return true;
        }
        short[] frames = catchAnim.data.anim[catchAnim.state];
        return frames.length == 0 || catchAnim.cursor >= frames.length / 2 - 1;
    }

    private void tickCatchObjects() {
        if (catchAnim != null) {
            if (catchAnimHoldLast && catchAnimAtLastFrame()) {
                catchAnim.tickHoldLast();
            } else {
                catchAnim.tick();
            }
        }
        catchPhaseTicks++;
    }

    private void startCatchEffect(int[][] steps, int duration, boolean resetEachTick) {
        catchEffectT0 = 0;
        catchEffectT1 = duration;
        catchEffectT2 = 1;
        catchEffectT3 = Math.max(1, steps.length);
        catchEffectT4 = resetEachTick ? 1 : 0;
        catchEffectSteps = steps;
        updateCatchEffectCurrentStep();
    }

    private void clearCatchEffect() {
        catchEffectT0 = 0;
        catchEffectT1 = 0;
        catchEffectT2 = 1;
        catchEffectT3 = 1;
        catchEffectT4 = 0;
        catchEffectSteps = null;
        catchEffectScale10 = 10;
        catchEffectDx = 0;
        catchEffectDy = 0;
    }

    private boolean catchEffectActive() {
        return catchEffectSteps != null && catchEffectT1 > 0;
    }

    private boolean tickCatchEffectSourceLike() {
        if (!catchEffectActive()) {
            return true;
        }
        if (catchEffectT0 < catchEffectT1 / catchEffectT3 * catchEffectT2) {
            updateCatchEffectCurrentStep();
        } else {
            catchEffectT2++;
            updateCatchEffectCurrentStep();
        }
        if (catchEffectT0 < catchEffectT1) {
            catchEffectT0++;
            return false;
        }
        clearCatchEffect();
        return true;
    }

    private void updateCatchEffectCurrentStep() {
        if (!catchEffectActive()) {
            catchEffectScale10 = 10;
            catchEffectDx = 0;
            catchEffectDy = 0;
            return;
        }
        int index = Math.max(0, Math.min(catchEffectSteps.length - 1, catchEffectT2 - 1));
        catchEffectScale10 = catchEffectSteps[index][0];
        catchEffectDx = catchEffectSteps[index][1];
        catchEffectDy = catchEffectSteps[index][2];
    }

    private void syncCatchEffectRender(VqsvIntroDemo.Scene s) {
        if (!catchEffectActive()) {
            s.battleCatchEffectVisible = false;
            s.battleCatchEffectDx = 0;
            s.battleCatchEffectDy = 0;
            s.battleCatchEffectScale10 = 10;
            return;
        }
        s.battleCatchEffectVisible = true;
        s.battleCatchEffectScale10 = catchEffectScale10;
        s.battleCatchEffectDx = catchEffectDx;
        s.battleCatchEffectDy = catchEffectDy;
    }

    private void clearCatchVisuals(VqsvIntroDemo.Scene s) {
        s.battleCatchVisible = false;
        s.battleCatchEffectVisible = false;
        s.battleEnemyHiddenByCatch = false;
        clearCatchEffect();
    }

    private void openCatchResultBox(VqsvIntroDemo.Scene s, String message, String sourceReason) {
        s.battleOpenBox = VqsvOpenBoxView.of(message);
        s.text = TextBox.openBox(message);
        catchOpenBoxState = catchStorageResult == 1 ? 2 : 1;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P17 " + sourceReason
                + " f=" + catchOpenBoxState
                + " storage=" + catchStorageResult
                + " text=" + TextBox.decodeMojibake(message));
    }

    private boolean tickCatchOpenBox(VqsvIntroDemo.Scene s) {
        syncRenderState(s, battleWinLog());
        if (s.text != null) {
            if (s.text.readyForKey && s.key0) {
                s.text.confirm();
                s.key0 = false;
                if (s.text.disposed) {
                    s.text = null;
                    s.battleOpenBox = VqsvOpenBoxView.EMPTY;
                }
            }
            if (s.text != null) {
                return false;
            }
        }
        if (catchOpenBoxState == 2) {
            catchOpenBoxState = 4;
            s.battleOpenBox = VqsvOpenBoxView.of(VqsvText.Battle.CATCH_SENT_BANK);
            s.text = TextBox.openBox(VqsvText.Battle.CATCH_SENT_BANK);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P17 S.f=2->4 second openbox.ui bank notice");
            return false;
        }
        if (catchOpenBoxState == 1 || catchOpenBoxState == 4) {
            s.sourceStateTrace.add("PORTED/PARTIAL battle P17 S.ax() closed f="
                    + catchOpenBoxState + " -> game.i.a().a(10)/battle exit");
            catchOpenBoxState = 0;
            s.battleOpenBox = VqsvOpenBoxView.EMPTY;
            return true;
        }
        return false;
    }

    private String applyCatchStorage(VqsvIntroDemo.Scene s) {
        SourcePetState caught = SourcePetState.caughtFromBattleUnit(Math.min(s.sourcePets.size(), 5), enemy);
        if (s.sourcePets.size() < 6) {
            s.sourcePets.add(caught);
            s.sourceStateTrace.add("PORTED battle P17 storage game.g.y=0 add bag species="
                    + caught.speciesId + " bagSize=" + s.sourcePets.size()
                    + " payloadLen=" + (caught.sourcePayload == null ? 0 : caught.sourcePayload.length));
            catchWinLog = VqsvText.Battle.CATCH_SUCCESS + enemy.name;
            catchStorageResult = 0;
            return catchWinLog;
        }
        if (s.sourcePetBank.size() < 100) {
            caught.slot = s.sourcePetBank.size();
            s.sourcePetBank.add(caught);
            s.sourceStateTrace.add("PORTED battle P17 storage game.g.y=1 add bank species="
                    + caught.speciesId + " bankSize=" + s.sourcePetBank.size()
                    + " payloadLen=" + (caught.sourcePayload == null ? 0 : caught.sourcePayload.length));
            catchWinLog = VqsvText.Battle.CATCH_SUCCESS + enemy.name;
            catchStorageResult = 1;
            return catchWinLog;
        }
        s.sourceStateTrace.add("PORTED battle P17 storage game.g.y=2 full release species="
                + caught.speciesId);
        catchWinLog = VqsvText.Battle.CATCH_RELEASED_FULL;
        catchStorageResult = 2;
        return catchWinLog;
    }

    private boolean tickItemList(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        MenuAction action = handleMenuInput(s);
        if (action == MenuAction.BACK) {
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.COMMAND_ITEM_PENDING);
            return false;
        }
        if (s.battleMenuIds.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_ITEMS, BattleRuntimeState.P4_ITEM_LIST);
            return false;
        }
        selectedItemId = s.battleMenuIds[s.battleMenuIndex];
        BattleItemRow row = VqsvBattleTables.instance().item(selectedItemId);
        int behavior = row == null ? -1 : row.behavior;
        if (behavior == 7 || behavior == 8 || behavior == 9 || behavior == 10) {
            enterWarning(s, VqsvText.Battle.ITEM_NOT_IN_BATTLE, BattleRuntimeState.P4_ITEM_LIST);
            return false;
        }
        prepareItemTargetMenu(s);
        enterState(s, BattleRuntimeState.P16_ITEM_TARGET, VqsvText.Battle.COMMAND_ITEM_PENDING, SHORT_WAIT);
        return false;
    }

    private boolean tickItemTarget(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (s.keyBack) {
            s.keyBack = false;
            prepareItemMenu(s);
            enterState(s, BattleRuntimeState.P4_ITEM_LIST, VqsvText.Battle.COMMAND_ITEM_PENDING, SHORT_WAIT);
            return false;
        }
        MenuAction action = handleMenuInput(s);
        if (action == MenuAction.BACK) {
            prepareItemMenu(s);
            enterState(s, BattleRuntimeState.P4_ITEM_LIST, VqsvText.Battle.COMMAND_ITEM_PENDING, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.COMMAND_ITEM_PENDING);
            return false;
        }
        if (s.battleMenuIds.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_PET_TARGET, BattleRuntimeState.P16_ITEM_TARGET);
            return false;
        }
        BagItem item = s.sourceBagItems.get(selectedItemId);
        if (item == null || item.count <= 0) {
            enterWarning(s, VqsvText.Battle.NO_ITEM_COUNT, BattleRuntimeState.P4_ITEM_LIST);
            return false;
        }
        int targetIndex = s.battleMenuIds[Math.max(0, Math.min(s.battleMenuIndex, s.battleMenuIds.length - 1))];
        BattleUnit targetUnit = itemTargetUnit(s, targetIndex);
        if (targetUnit == null) {
            enterWarning(s, VqsvText.Battle.NO_PET_TARGET, BattleRuntimeState.P16_ITEM_TARGET);
            return false;
        }
        int validation = targetUnit.validateBattleItem(selectedItemId);
        if (validation != -1) {
            s.sourceStateTrace.add("PORTED battle P16 game.b.x item=" + selectedItemId
                    + " targetIndex=" + targetIndex
                    + " validation=" + validation
                    + " warning=" + itemWarning(validation));
            enterWarning(s, itemWarning(validation), BattleRuntimeState.P16_ITEM_TARGET);
            return false;
        }
        BattleItemRow row = VqsvBattleTables.instance().item(selectedItemId);
        int behavior = row == null ? -1 : row.behavior;
        if (behavior >= 1 && behavior <= 6) {
            BattleItemUseResult result = targetUnit.applyBattleItem(selectedItemId);
            VqsvSourceOps.sourceRemoveItem(s, selectedItemId, 1);
            persistItemTarget(s, targetIndex, targetUnit);
            playerActionThisRound = true;
            s.sourceStateTrace.add("PORTED battle P16 game.b.w item=" + selectedItemId
                    + " behavior=" + behavior
                    + " targetIndex=" + targetIndex
                    + " hp=" + result.hpBefore + "->" + result.hpAfter
                    + " pp=" + result.ppBefore + "->" + result.ppAfter
                    + " debuffs=" + result.debuffsBefore + "->" + result.debuffsAfter
                    + " state6=" + result.sourceStateFlag
                    + " remaining=" + VqsvSourceOps.sourceItemCount(s, selectedItemId));
            syncRenderState(s, VqsvText.Battle.ITEM_USED);
            prepareItemTargetMenu(s, false);
            syncRenderState(s, VqsvText.Battle.ITEM_USED);
            enterWarning(s, VqsvText.Battle.ITEM_USED, BattleRuntimeState.P1_DISPATCH);
        } else {
            enterWarning(s, VqsvText.Battle.ITEM_NOT_IN_BATTLE, BattleRuntimeState.P4_ITEM_LIST);
        }
        return false;
    }

    private BattleUnit itemTargetUnit(VqsvIntroDemo.Scene s, int targetIndex) {
        if (targetIndex < 0) {
            return null;
        }
        if (targetIndex == 0 && player != null && player.battleUnit != null) {
            return player.battleUnit;
        }
        if (targetIndex >= s.sourcePets.size()) {
            return null;
        }
        return BattleUnit.fromSourcePet(s.sourcePets.get(targetIndex), (byte) 0);
    }

    private void persistItemTarget(VqsvIntroDemo.Scene s, int targetIndex, BattleUnit targetUnit) {
        if (targetIndex >= 0 && targetIndex < s.sourcePets.size()) {
            s.sourcePets.get(targetIndex).persistBattleUnit(targetUnit);
        }
        if (targetIndex == 0) {
            player = targetUnit.toRenderUnit(true);
        }
    }

    private String itemWarning(int validation) {
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
                return VqsvText.Battle.ITEM_NOT_IN_BATTLE;
        }
    }

    private boolean tickPetSwitch(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (s.keyBack) {
            s.keyBack = false;
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        MenuAction action = handleMenuInput(s);
        if (action == MenuAction.BACK) {
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.COMMAND_PET_PENDING);
            return false;
        }
        if (s.battleMenuIds.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_SWITCH_PET, BattleRuntimeState.P5_PET_SWITCH);
            return false;
        }
        selectedPetIndex = s.battleMenuIds[s.battleMenuIndex];
        if (selectedPetIndex < 0 || selectedPetIndex >= s.sourcePets.size()) {
            enterWarning(s, VqsvText.Battle.NO_SWITCH_PET, BattleRuntimeState.P5_PET_SWITCH);
            return false;
        }
        if (!sourcePetAlive(s.sourcePets.get(selectedPetIndex))) {
            s.sourceStateTrace.add("PORTED battle P5 game.d.a(slot) validation=0 dead selectedIndex="
                    + selectedPetIndex + " forced=" + forcedPetSwitch);
            enterWarning(s, VqsvText.Battle.PET_CANNOT_BATTLE, BattleRuntimeState.P5_PET_SWITCH);
            return false;
        }
        if (s.sourcePets.get(selectedPetIndex).sourceK()) {
            s.sourceStateTrace.add("PORTED battle P5 game.d.a(slot) validation=1 already-active selectedIndex="
                    + selectedPetIndex + " sourceK=true");
            enterWarning(s, VqsvText.Battle.PET_ALREADY_ACTIVE, BattleRuntimeState.P5_PET_SWITCH);
            return false;
        }
        int clearedStatus11 = clearEnemyBuff11ForPlayerSwitch();
        if (!s.sourcePets.isEmpty() && player != null && player.battleUnit != null) {
            s.sourcePets.get(0).persistBattleUnit(player.battleUnit);
        }
        SourcePetState next = s.sourcePets.remove(selectedPetIndex);
        s.sourcePets.add(0, next);
        resetSourcePetOrder(s);
        setActiveSourcePetFlags(s, 0);
        player = SourceBattleUnit.playerFromSourcePets(s.sourcePets);
        pruneSourceExpVectors(s, "P5 switch");
        addSourceExpParticipant(s, next, "P5 game.d.a(slot) switched-in pet");
        playerActionThisRound = true;
        boolean wasForced = forcedPetSwitch;
        forcedPetSwitch = false;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P5 game.d.a(slot) validation=-1 selectedIndex="
                + selectedPetIndex + " newPlayer=" + player.name
                + " forced=" + wasForced
                + " sourcePath=reorder f[slot]->f[0], mark active K/J then game.d.a(byte 15)"
                + " clearedEnemyBuff11=" + clearedStatus11
                + " sourcePetOrder=" + java.util.Arrays.toString(sourcePetOrder));
        enterPlayerSwitchTransition(s, wasForced);
        return false;
    }

    private void resetSourcePetOrder(VqsvIntroDemo.Scene s) {
        sourcePetOrder = new int[Math.min(6, s.sourcePets.size())];
        for (int i = 0; i < sourcePetOrder.length; i++) {
            sourcePetOrder[i] = i;
        }
    }

    private void ensureSourcePetOrder(VqsvIntroDemo.Scene s) {
        if (sourcePetOrder.length != Math.min(6, s.sourcePets.size())) {
            resetSourcePetOrder(s);
        }
    }

    private void setActiveSourcePetFlags(VqsvIntroDemo.Scene s, int activeIndex) {
        for (int i = 0; i < s.sourcePets.size(); i++) {
            SourcePetState pet = s.sourcePets.get(i);
            pet.sourceD(i == activeIndex);
            pet.sourceTurnUsed = false;
            pet.sourceF = 0;
        }
        if (activeIndex >= 0 && activeIndex < s.sourcePets.size()) {
            s.sourcePets.get(activeIndex).sourceTurnUsed = true;
        }
    }

    private int clearEnemyBuff11ForPlayerSwitch() {
        int cleared = 0;
        if (enemyParty == null) {
            return 0;
        }
        for (SourceBattleUnit unit : enemyParty) {
            if (unit != null && unit.battleUnit != null
                    && unit.battleUnit.clearSourceBuffForSwitch(11)) {
                cleared++;
            }
        }
        return cleared;
    }

    private boolean sourcePetAlive(SourcePetState pet) {
        return pet != null && BattleUnit.fromSourcePet(pet, (byte) 0).alive();
    }

    private void enterPlayerSwitchTransition(VqsvIntroDemo.Scene s, boolean wasForced) {
        playerSwitchWasForced = wasForced;
        playerSwitchTicks = 0;
        playerSwitchCposRow = VqsvBattleAnimationTables.instance().cposRow(playerSwitchCposGroup(), playerSwitchCposRow());
        targetUnits = new SourceBattleUnit[0];
        targetSlots = new int[0];
        selectedTarget = null;
        syncRenderState(s, VqsvText.Battle.PET_SWITCHED + player.name);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P15 player switch transition from game.h.X valid P5"
                + " forced=" + playerSwitchWasForced
                + " active=" + player.name
                + " hp=" + player.hp + "/" + player.maxHp
                + " cposGroup=" + playerSwitchCposGroup()
                + " cposRow=" + playerSwitchCposRow()
                + " cposFrames=" + (playerSwitchCposRow.length / 4));
        enterState(s, BattleRuntimeState.P15_PLAYER_SWITCH, s.battleLog, SHORT_WAIT);
        applyPlayerSwitchCposOffset(s);
    }

    private boolean tickPlayerSwitchTransition(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            applyPlayerSwitchCposOffset(s);
            return false;
        }
        ++playerSwitchTicks;
        syncRenderState(s, s.battleLog);
        applyPlayerSwitchCposOffset(s);
        if (playerSwitchTicks < playerSwitchCposFrameCount()) {
            return false;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL battle P15 player switch transition complete forced="
                + playerSwitchWasForced + " next=P1");
        enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, SHORT_WAIT);
        return false;
    }

    private void applyPlayerSwitchCposOffset(VqsvIntroDemo.Scene s) {
        int frames = playerSwitchCposFrameCount();
        if (frames <= 0) {
            s.battleP7PlayerOffsetX = 0;
            s.battleP7PlayerOffsetY = 0;
            return;
        }
        int frame = Math.max(0, Math.min(frames - 1, playerSwitchTicks));
        int finalAt = (frames - 1) << 2;
        int at = frame << 2;
        s.battleP7PlayerOffsetX = playerSwitchCposRow[at] - playerSwitchCposRow[finalAt];
        s.battleP7PlayerOffsetY = playerSwitchCposRow[at + 1] - playerSwitchCposRow[finalAt + 1];
    }

    private int playerSwitchCposFrameCount() {
        return playerSwitchCposRow.length / 4;
    }

    private int playerSwitchCposGroup() {
        return sourceCposGroup();
    }

    private int sourceCposGroup() {
        int sourceA = battleMode.length > 0 ? battleMode[0] : 0;
        int sourceB = battleMode.length > 1 ? battleMode[1] : 0;
        return sourceA == 0 ? (sourceB == 1 ? 2 : 0) : 1;
    }

    private int playerSwitchCposRow() {
        return playerSwitchCposGroup() == 1 ? 2 : 1;
    }

    private boolean prepareEnemyReplacement(VqsvIntroDemo.Scene s) {
        int next = nextAliveEnemyIndex();
        if (next < 0) {
            return false;
        }
        pendingEnemyReplacementIndex = next;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P15 source game.d.a(byte) replacement pending oldIndex="
                + activeEnemyIndex + " nextIndex=" + pendingEnemyReplacementIndex
                + " next=" + enemyParty[pendingEnemyReplacementIndex].name);
        enterState(s, BattleRuntimeState.P15_ENEMY_REPLACEMENT,
                VqsvText.Battle.START, SHORT_WAIT);
        return true;
    }

    private int nextAliveEnemyIndex() {
        for (int i = Math.max(0, activeEnemyIndex + 1); i < enemyParty.length; i++) {
            if (enemyParty[i] != null && enemyParty[i].alive()) {
                return i;
            }
        }
        return -1;
    }

    private boolean tickEnemyReplacement(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (pendingEnemyReplacementIndex < 0 || pendingEnemyReplacementIndex >= enemyParty.length) {
            enterState(s, BattleRuntimeState.P8_WIN, battleWinLog(), SHORT_WAIT);
            return false;
        }
        activeEnemyIndex = pendingEnemyReplacementIndex;
        pendingEnemyReplacementIndex = -1;
        enemy = enemyParty[activeEnemyIndex];
        p7KoBaseHiddenEnemySide = false;
        enemyActionThisRound = false;
        enemyActiveQueueProcessedThisRound = false;
        targetUnits = new SourceBattleUnit[0];
        targetSlots = new int[0];
        selectedTarget = null;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P15 source case15 swap enemy activeIndex="
                + activeEnemyIndex + " enemy=" + enemy.name
                + " hp=" + enemy.hp + "/" + enemy.maxHp);
        syncRenderState(s, s.battleLog);
        enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, SHORT_WAIT);
        return false;
    }

    private boolean tickShop(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        MenuAction action = handleMenuInput(s);
        if (action == MenuAction.BACK) {
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.COMMAND_SHOP_PENDING);
            return false;
        }
        if (s.battleMenuIds.length == 0) {
            enterWarning(s, VqsvText.Battle.NO_SHOP_ITEMS, BattleRuntimeState.P11_SHOP);
            return false;
        }
        int itemId = s.battleMenuIds[s.battleMenuIndex];
        BattleItemRow row = VqsvBattleTables.instance().item(itemId);
        int price = row == null ? 0 : (itemId == 0 ? row.priceOrValue : row.priceOrValue << 1);
        if (s.sourceMoney < price) {
            enterWarning(s, VqsvText.Battle.NOT_ENOUGH_MONEY, BattleRuntimeState.P11_SHOP);
            return false;
        }
        s.sourceMoney -= price;
        VqsvSourceOps.sourceAddItem(s, itemId, 1);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P11 shop buy item="
                + itemId + " price=" + price + " money=" + s.sourceMoney);
        enterWarning(s, VqsvText.Common.ITEM_REWARD_PREFIX
                + itemName(itemId) + " x 1", BattleRuntimeState.P11_SHOP);
        return false;
    }

    void debugQueueDebuffForSmoke(VqsvIntroDemo.Scene s, boolean playerSide,
                                  int debuffId, int storedRaw, int sourceSkill, int duration, int hp) {
        SourceBattleUnit unit = playerSide ? player : enemy;
        if (unit == null || unit.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires an entered source battle");
        }
        unit.battleUnit.debuffSlots[debuffId][0] = (short) duration;
        unit.battleUnit.debuffSlots[debuffId][1] = (short) storedRaw;
        unit.battleUnit.debuffSlots[debuffId][3] = (short) sourceSkill;
        unit.battleUnit.debuffSlots[debuffId][4] = 1;
        unit.battleUnit.addActiveEffect(1, debuffId);
        setHp(unit, hp);
        if (playerSide) {
            playerActionThisRound = false;
            enemyActionThisRound = true;
            playerActiveQueueProcessedThisRound = false;
        } else {
            playerActionThisRound = true;
            enemyActionThisRound = false;
            enemyActiveQueueProcessedThisRound = false;
        }
        enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, 0);
    }

    void debugSetEnemyHpForSmoke(VqsvIntroDemo.Scene s, int hp) {
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires an entered source battle enemy");
        }
        setHp(enemy, hp);
        enemyDisplayHp = enemy.hp;
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle debug enemy hp=" + enemy.hp + "/" + enemy.maxHp
                + " displayHp=" + enemyDisplayHp);
    }

    void debugSetPlayerHpForSmoke(VqsvIntroDemo.Scene s, int hp) {
        if (player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires an entered source battle player");
        }
        setHp(player, hp);
        playerDisplayHp = player.hp;
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle debug player hp=" + player.hp + "/" + player.maxHp
                + " displayHp=" + playerDisplayHp);
    }

    void debugEnemyPartyForSmoke(VqsvIntroDemo.Scene s, int[][] encounterRows) {
        if (!entered) {
            enterBattle(s);
        }
        if (encounterRows == null || encounterRows.length == 0) {
            throw new IllegalArgumentException("Smoke enemy party requires at least one encounter row");
        }
        enemyParty = new SourceBattleUnit[encounterRows.length];
        for (int i = 0; i < encounterRows.length; i++) {
            enemyParty[i] = SourceBattleUnit.enemyFromEncounter(encounterRows[i]);
        }
        activeEnemyIndex = 0;
        pendingEnemyReplacementIndex = -1;
        enemy = enemyParty[activeEnemyIndex];
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle enemy party injected size=" + enemyParty.length
                + " active=" + enemy.name);
    }

    void debugPlayerDebuffForItemSmoke(VqsvIntroDemo.Scene s, int debuffId, int value, int sourceSkill) {
        if (!entered) {
            enterBattle(s);
        }
        if (player == null || player.battleUnit == null) {
            throw new IllegalStateException("Item smoke requires active player battle unit");
        }
        BattleUnit unit = player.battleUnit;
        unit.debuffSlots[debuffId][0] = 3;
        unit.debuffSlots[debuffId][1] = (short) value;
        unit.debuffSlots[debuffId][3] = (short) sourceSkill;
        unit.debuffSlots[debuffId][4] = 1;
        unit.addActiveEffect(1, debuffId);
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle P16 item debuff target prepared id="
                + debuffId + " value=" + value + " skill=" + sourceSkill);
    }

    void debugEnemyDebuffForFormulaSmoke(VqsvIntroDemo.Scene s, int debuffId, int value, int sourceSkill) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Formula smoke requires enemy battle unit");
        }
        BattleUnit unit = enemy.battleUnit;
        unit.debuffSlots[debuffId][0] = 3;
        unit.debuffSlots[debuffId][1] = (short) value;
        unit.debuffSlots[debuffId][3] = (short) sourceSkill;
        unit.debuffSlots[debuffId][4] = 1;
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle Phase9C enemy debuff prepared id="
                + debuffId + " value=" + value + " skill=" + sourceSkill);
    }

    void debugStatusIconForSmoke(VqsvIntroDemo.Scene s, boolean playerSide,
                                 int bank, int effectId, int duration, int value, int sourceSkill) {
        if (!entered) {
            enterBattle(s);
        }
        SourceBattleUnit sourceUnit = playerSide ? player : enemy;
        if (sourceUnit == null || sourceUnit.battleUnit == null) {
            throw new IllegalStateException("Status icon smoke requires entered battle unit");
        }
        BattleUnit unit = sourceUnit.battleUnit;
        if (bank == 0) {
            unit.buffSlots[effectId][0] = (short) duration;
            unit.buffSlots[effectId][1] = (short) value;
            unit.buffSlots[effectId][3] = (short) sourceSkill;
            unit.buffSlots[effectId][4] = 1;
        } else if (bank == 1) {
            unit.debuffSlots[effectId][0] = (short) duration;
            unit.debuffSlots[effectId][1] = (short) value;
            unit.debuffSlots[effectId][3] = (short) sourceSkill;
            unit.debuffSlots[effectId][4] = 1;
        } else {
            throw new IllegalArgumentException("Status icon smoke bank must be 0 or 1, got " + bank);
        }
        unit.addActiveEffect(bank, effectId);
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle Phase10A status icon prepared side="
                + (playerSide ? "player" : "enemy")
                + " bank=" + bank
                + " id=" + effectId
                + " duration=" + duration
                + " sourceSkill=" + sourceSkill);
    }

    boolean debugEnemyHasDebuffForSmoke(int debuffId) {
        return enemy != null && enemy.battleUnit != null && enemy.battleUnit.hasDebuff(debuffId);
    }

    boolean debugPlayerHasDebuffForSmoke(int debuffId) {
        return player != null && player.battleUnit != null && player.battleUnit.hasDebuff(debuffId);
    }

    int debugEnemyTryDebuffPlayerForSmoke(VqsvIntroDemo.Scene s, int skillId, int debuffId) {
        if (enemy == null || enemy.battleUnit == null || player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires entered player/enemy units");
        }
        enemy.battleUnit.selectedSkillId = (byte) skillId;
        enemy.battleUnit.target = player.battleUnit;
        BattleUnit.setNextDebuffRollForChecks(0);
        BattleUnit.setSourceRandomTrace(SOURCE_RANDOM, s.sourceStateTrace, "battle.Phase9Z.block.skill" + skillId);
        BattleDamageResult result = enemy.battleUnit.computeDamage(player.battleUnit);
        BattleUnit.clearRandomTrace();
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle Phase9Z enemy debuff attempt skill=" + skillId
                + " expectedDebuff=" + debuffId
                + " appliedDebuff=" + result.appliedDebuffId
                + " playerHasBuff14=" + player.battleUnit.hasBuff(14)
                + " playerHasDebuff=" + player.battleUnit.hasDebuff(debuffId));
        return result.appliedDebuffId;
    }

    int debugEnemyAttackPlayerReflectHookForSmoke(VqsvIntroDemo.Scene s) {
        if (enemy == null || enemy.battleUnit == null || player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires entered player/enemy units");
        }
        enemy.battleUnit.selectedSkillId = 10;
        enemy.battleUnit.target = player.battleUnit;
        SOURCE_RANDOM.setSeed(3L);
        BattleUnit.setSourceRandomTrace(SOURCE_RANDOM, s.sourceStateTrace, "battle.Phase9AA.buff5Reflect");
        BattleDamageResult result = enemy.battleUnit.computeDamage(player.battleUnit);
        BattleUnit.clearRandomTrace();
        int reflected = enemy.battleUnit.consumeStoredReflectDamage();
        if (reflected > 0) {
            enemy.battleUnit.damage(reflected);
        }
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle Phase9AA buff5 reflect hook skill=34"
                + " enemySkill=10"
                + " damage=" + result.damage
                + " reflected=" + reflected
                + " playerHasBuff5=" + player.battleUnit.hasBuff(5)
                + " playerBuff5Chance=" + player.battleUnit.buffSlots[5][1]
                + " source=game.b.b(target) K[5] + game.d.q consume");
        return reflected;
    }

    int debugEnemyAttackPlayerBuff6ReductionForSmoke(VqsvIntroDemo.Scene s) {
        if (enemy == null || enemy.battleUnit == null || player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires entered player/enemy units");
        }
        enemy.battleUnit.selectedSkillId = 10;
        enemy.battleUnit.target = player.battleUnit;
        enemy.battleUnit.clearSourceBuffForSwitch(6);
        SOURCE_RANDOM.setSeed(12L);
        BattleUnit.setSourceRandomTrace(SOURCE_RANDOM, s.sourceStateTrace, "battle.Phase9AA.buff6Baseline");
        int baseline = enemy.battleUnit.computeDamage(player.battleUnit).damage;
        BattleUnit.clearRandomTrace();
        enemy.battleUnit.applySourceBuff(6, 0, 35);
        SOURCE_RANDOM.setSeed(12L);
        BattleUnit.setSourceRandomTrace(SOURCE_RANDOM, s.sourceStateTrace, "battle.Phase9AA.buff6Reduced");
        int reduced = enemy.battleUnit.computeDamage(player.battleUnit).damage;
        BattleUnit.clearRandomTrace();
        syncRenderState(s, s.battleLog);
        int delta = baseline - reduced;
        s.sourceStateTrace.add("SMOKE battle Phase9AA buff6 source-odd hook skill=35"
                + " enemySkill=10"
                + " baseline=" + baseline
                + " reduced=" + reduced
                + " delta=" + delta
                + " playerHasBuff6=" + player.battleUnit.hasBuff(6)
                + " enemyBuff6Chance=" + enemy.battleUnit.buffSlots[6][1]
                + " enemyBuff6Percent=" + enemy.battleUnit.buffSlots[6][2]
                + " source=game.b.b(target) checks target.m(6) but reads attacker.v[6]");
        return delta;
    }

    void debugEnemyFormStatusForSmoke(VqsvIntroDemo.Scene s, int statusId) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Formula smoke requires enemy battle unit");
        }
        enemy.battleUnit.baseStats[BattleUnit.STAT_FORM] = (short) statusId;
        s.sourceStateTrace.add("SMOKE battle Phase9E enemy form status="
                + statusId + " source=game.b.f(byte)");
    }

    void debugEnemyBuffForFormulaSmoke(VqsvIntroDemo.Scene s, int buffId, int value, int sourceSkill) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Formula smoke requires enemy battle unit");
        }
        BattleUnit unit = enemy.battleUnit;
        unit.buffSlots[buffId][0] = 3;
        unit.buffSlots[buffId][1] = (short) value;
        unit.buffSlots[buffId][3] = (short) sourceSkill;
        unit.buffSlots[buffId][4] = 1;
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle Phase9C enemy buff prepared id="
                + buffId + " value=" + value + " skill=" + sourceSkill);
    }

    void debugEnemySourceBuffForClearSmoke(VqsvIntroDemo.Scene s, int buffId, int value, int sourceSkill) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Clear-buff smoke requires enemy battle unit");
        }
        enemy.battleUnit.applySourceBuff(buffId, value, sourceSkill);
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle Phase9N enemy source buff prepared id="
                + buffId + " value=" + value + " skill=" + sourceSkill
                + " activeSlot=" + enemy.battleUnit.activeBuffSlot(buffId));
    }

    boolean debugEnemyHasBuffForSmoke(int buffId) {
        return enemy != null && enemy.battleUnit != null && enemy.battleUnit.hasBuff(buffId);
    }

    int debugEnemyActiveBuffSlotForSmoke(int buffId) {
        return enemy != null && enemy.battleUnit != null ? enemy.battleUnit.activeBuffSlot(buffId) : -1;
    }

    int debugEnemyBuffValueForSmoke(int buffId) {
        return enemy != null && enemy.battleUnit != null
                && buffId >= 0 && buffId < enemy.battleUnit.buffSlots.length
                ? enemy.battleUnit.buffSlots[buffId][1] : Integer.MIN_VALUE;
    }

    boolean debugPlayerHasBuffForSmoke(int buffId) {
        return player != null && player.battleUnit != null && player.battleUnit.hasBuff(buffId);
    }

    int debugPlayerActiveBuffSlotForSmoke(int buffId) {
        return player != null && player.battleUnit != null ? player.battleUnit.activeBuffSlot(buffId) : -1;
    }

    int debugPlayerBuffValueForSmoke(int buffId) {
        return player != null && player.battleUnit != null
                && buffId >= 0 && buffId < player.battleUnit.buffSlots.length
                ? player.battleUnit.buffSlots[buffId][1] : Integer.MIN_VALUE;
    }

    int debugPlayerBaseStatForSmoke(int statId) {
        return player != null && player.battleUnit != null
                && statId >= 0 && statId < player.battleUnit.baseStats.length
                ? player.battleUnit.baseStats[statId] : Integer.MIN_VALUE;
    }

    int debugPlayerCurrentStatForSmoke(int statId) {
        return player != null && player.battleUnit != null
                && statId >= 0 && statId < player.battleUnit.currentStats.length
                ? player.battleUnit.currentStats[statId] : Integer.MIN_VALUE;
    }

    void debugEnemyBuff11ForPetSwitchSmoke(VqsvIntroDemo.Scene s) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("P5 buff11 cleanup smoke requires enemy battle unit");
        }
        enemy.battleUnit.buffSlots[11][0] = 3;
        enemy.battleUnit.buffSlots[11][1] = 0;
        enemy.battleUnit.buffSlots[11][4] = 1;
        enemy.battleUnit.addActiveEffect(0, 11);
        s.sourceStateTrace.add("SMOKE prepared enemy source buff11 pointer to active player before P5 switch");
    }

    void debugSetNextCatchRollForSmoke(int roll) {
        debugNextCatchRoll = Math.max(0, Math.min(99, roll));
    }

    int debugCatchChanceForSmoke(int itemId) {
        if (!entered) {
            throw new IllegalStateException("Battle smoke hook requires an entered source battle");
        }
        return catchChance(itemId);
    }

    void debugSetSourceRandomSeedForSmoke(long seed) {
        SOURCE_RANDOM.setSeed(seed);
    }

    void debugSetNextP7HitRollForSmoke(int roll) {
        debugNextP7HitRoll = Math.max(0, Math.min(99, roll));
    }

    void debugSetNextLeechRollForSmoke(int roll) {
        debugNextLeechRoll = Math.max(0, Math.min(99, roll));
    }

    void debugSetNextFollowUpRollForSmoke(int roll) {
        debugNextFollowUpRoll = Math.max(0, Math.min(99, roll));
    }

    void debugSetNextDamageDebuffRollForSmoke(int roll) {
        BattleUnit.setNextDebuffRollForChecks(roll);
    }

    void debugSetPlayerSpeedForSmoke(VqsvIntroDemo.Scene s, int speed) {
        if (!entered) {
            enterBattle(s);
        }
        if (player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires player battle unit");
        }
        int safe = Math.max(0, Math.min(Short.MAX_VALUE, speed));
        player.battleUnit.currentStats[BattleUnit.STAT_SPEED] = (short) safe;
        s.sourceStateTrace.add("SMOKE battle debug player speed=" + safe
                + " source=game.b.d[4] crit/miss setup");
    }

    void debugSetPlayerAttackForSmoke(VqsvIntroDemo.Scene s, int attack) {
        if (!entered) {
            enterBattle(s);
        }
        if (player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires player battle unit");
        }
        int safe = Math.max(0, Math.min(Short.MAX_VALUE, attack));
        player.battleUnit.currentStats[BattleUnit.STAT_ATTACK] = (short) safe;
        player.battleUnit.baseStats[BattleUnit.STAT_ATTACK] = (short) safe;
        s.sourceStateTrace.add("SMOKE battle debug player attack=" + safe
                + " source=game.b.d[2]/c[2] formula setup");
    }

    void debugSetEnemyDefenseForSmoke(VqsvIntroDemo.Scene s, int defense) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires enemy battle unit");
        }
        int safe = Math.max(0, Math.min(Short.MAX_VALUE, defense));
        enemy.battleUnit.currentStats[BattleUnit.STAT_DEFENSE] = (short) safe;
        enemy.battleUnit.baseStats[BattleUnit.STAT_DEFENSE] = (short) safe;
        s.sourceStateTrace.add("SMOKE battle debug enemy defense=" + safe
                + " source=game.b.d[3]/c[3] formula setup");
    }

    void debugSetEnemySpeedForSmoke(VqsvIntroDemo.Scene s, int speed) {
        if (!entered) {
            enterBattle(s);
        }
        if (enemy == null || enemy.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires enemy battle unit");
        }
        int safe = Math.max(0, Math.min(Short.MAX_VALUE, speed));
        enemy.battleUnit.currentStats[BattleUnit.STAT_SPEED] = (short) safe;
        s.sourceStateTrace.add("SMOKE battle debug enemy speed=" + safe
                + " source=game.b.d[4] miss setup");
    }

    void debugSetPlayerBuff12KForSmoke(VqsvIntroDemo.Scene s, int kValue) {
        if (player == null || player.battleUnit == null) {
            throw new IllegalStateException("Battle smoke hook requires an entered source battle player");
        }
        player.battleUnit.applySourceBuff(12, -1, 65);
        player.battleUnit.effectScratch[12] = (short) Math.max(0, Math.min(2, kValue));
        s.sourceStateTrace.add("SMOKE battle debug player buff12 K12="
                + player.battleUnit.effectScratch[12]
                + " source=game.b.o(12)/game.d.q follow-up");
    }

    int debugPlayerK12ForSmoke() {
        return player != null && player.battleUnit != null ? player.battleUnit.effectScratch[12] : Integer.MIN_VALUE;
    }

    void debugSetCatchStatusForSmoke(VqsvIntroDemo.Scene s, int targetDebuffId, boolean attackerForm11) {
        if (!entered) {
            enterBattle(s);
        }
        if (targetDebuffId >= 0) {
            if (enemy == null || enemy.battleUnit == null
                    || targetDebuffId >= enemy.battleUnit.debuffSlots.length) {
                throw new IllegalStateException("Catch status smoke requires enemy debuff slot " + targetDebuffId);
            }
            enemy.battleUnit.debuffSlots[targetDebuffId][0] = 3;
            enemy.battleUnit.debuffSlots[targetDebuffId][4] = 1;
        }
        if (attackerForm11) {
            if (player == null || player.battleUnit == null) {
                throw new IllegalStateException("Catch status smoke requires player battle unit");
            }
            player.battleUnit.baseStats[BattleUnit.STAT_FORM] = 11;
        }
        syncRenderState(s, s.battleLog);
        s.sourceStateTrace.add("SMOKE battle catch chance status targetDebuff="
                + targetDebuffId + " attackerForm11=" + attackerForm11);
    }

    private boolean tickRun(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (battleMode.length > 0 && battleMode[0] > 0 || !s.sourceGameCF) {
            enterWarning(s, VqsvText.Battle.RUN_NOT_ALLOWED, BattleRuntimeState.P20_COMMAND);
            return false;
        }
        boolean success = runChancePercent() >= 50;
        if (success) {
            s.battleResultIndex = -1;
            s.battleBranchTarget = -1;
            persistActivePlayerPet(s, "P10 run success");
            s.sourceStateTrace.add("PORTED/PARTIAL battle P10 run success speed player="
                    + player.speed + " enemy=" + enemy.speed);
            enterState(s, BattleRuntimeState.EXIT_FADE, VqsvText.Battle.RUN_SUCCESS, EXIT_WAIT);
        } else {
            playerActionThisRound = true;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P10 run failed speed player="
                    + player.speed + " enemy=" + enemy.speed);
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.RUN_FAILED, SHORT_WAIT);
        }
        return false;
    }

    private boolean tickWarning(VqsvIntroDemo.Scene s) {
        s.battleUiMode = "warning";
        if (countdown()) {
            return false;
        }
        if (!s.key0) {
            return false;
        }
        s.key0 = false;
        if (s.text != null && s.text.sourceUiKind == TextBox.SOURCE_MSGWARM) {
            s.text = null;
        }
        if (warningReturnState == BattleRuntimeState.P20_COMMAND) {
            enterCommandState(s, warningReturnLog, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P6_TARGET_SELECT) {
            enterState(s, BattleRuntimeState.P6_TARGET_SELECT, warningReturnLog, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P21_CATCH_LIST) {
            prepareCatchMenu(s);
            enterState(s, BattleRuntimeState.P21_CATCH_LIST, VqsvText.Battle.COMMAND_CATCH_PENDING, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P3_SKILL_LIST) {
            prepareSkillMenu(s);
            enterState(s, BattleRuntimeState.P3_SKILL_LIST, VqsvText.Battle.COMMAND_FIGHT, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P4_ITEM_LIST) {
            prepareItemMenu(s);
            enterState(s, BattleRuntimeState.P4_ITEM_LIST, VqsvText.Battle.COMMAND_ITEM_PENDING, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P16_ITEM_TARGET) {
            prepareItemTargetMenu(s, false);
            enterState(s, BattleRuntimeState.P16_ITEM_TARGET, VqsvText.Battle.COMMAND_ITEM_PENDING, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P5_PET_SWITCH) {
            preparePetMenu(s, false);
            enterState(s, BattleRuntimeState.P5_PET_SWITCH, VqsvText.Battle.COMMAND_PET_PENDING, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P11_SHOP) {
            prepareShopMenu(s);
            enterState(s, BattleRuntimeState.P11_SHOP, VqsvText.Battle.COMMAND_SHOP_PENDING, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P1_DISPATCH) {
            enterState(s, BattleRuntimeState.P1_DISPATCH, warningReturnLog, SHORT_WAIT);
        } else {
            enterCommandState(s, warningReturnLog, SHORT_WAIT);
        }
        return false;
    }

    private boolean tickSelectExecute(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        prepareP7(s);
        enterState(s, BattleRuntimeState.P7_RESOLVE, p7SkillName(), 0);
        return false;
    }

    private boolean tickResolve(VqsvIntroDemo.Scene s) {
        if (!p7Prepared) {
            prepareP7(s);
        }
        if (p7Phase == 0) {
            p7Phase = 1;
            p7Ticks = 0;
            syncP7RenderState(s, p7SkillName());
            s.sourceStateTrace.add("PORTED/PARTIAL battle P7 enter skill=" + p7SkillId
                    + " sourceEffectRow=" + java.util.Arrays.toString(p7EffectRow)
                    + " chunk=" + p7EffectChunk
                    + " " + VqsvBattleAnimationTables.sourceSummary(p7SkillId));
            return false;
        }
        if (p7Phase == 1) {
            p7Ticks++;
            tickP7LEffect(s);
            if (tickP7SourceEffectSequence(s)) {
                syncP7RenderState(s, p7SkillName());
                return false;
            }
            syncP7RenderState(s, p7SkillName());
            if (!p7FlagA) {
                return false;
            }
            if (p7NoDamageSkill()) {
                applyP7PostSkillEffects(s);
                markP7ActionUsed();
                p7Phase = 3;
                p7Ticks = 0;
                syncP7RenderState(s, p7SkillName());
                s.sourceStateTrace.add("PORTED battle P7 no-damage skill=" + p7SkillId
                        + " because aq.c[1][skill][3] == 0; source game.d skips damage text/apply path");
                return false;
            }
            applyP7Damage(s);
            p7Phase = 2;
            p7Ticks = 0;
            syncP7RenderState(s, p7Phase2BattleLog());
            return false;
        }
        if (p7Phase == 2) {
            p7Ticks++;
            tickP7HpTween();
            if (p7Ticks == P7_DAMAGE_TICKS / 2 && p7Target.alive()) {
                setP7BaseState(s, p7Target == player, 0);
            }
            syncP7RenderState(s, s.battleLog);
            if (p7DamageTextActive() || p7HpTweenActive) {
                return false;
            }
            if (p7Target != null && !p7Target.alive() && !p7DeathEffectActive) {
                startP7DeathEffect(s, p7Target == player);
                syncP7RenderState(s, s.battleLog);
                return false;
            }
            if (p7DeathEffectActive) {
                tickP7DeathEffect();
                syncP7RenderState(s, s.battleLog);
                if (p7DeathEffectActive) {
                    return false;
                }
            }
            if (p7Target != null && !p7Target.alive()) {
                p7Phase = 3;
                p7Ticks = 0;
                syncP7RenderState(s, s.battleLog);
                return false;
            }
            applyP7PostSkillEffects(s);
            p7Phase = 3;
            p7Ticks = 0;
            syncP7RenderState(s, s.battleLog);
            return false;
        }
        p7Ticks++;
        syncP7RenderState(s, s.battleLog);
        if (p7Ticks < P7_EXIT_TICKS) {
            return false;
        }
        return finishP7(s);
    }

    private void prepareP7(VqsvIntroDemo.Scene s) {
        p7Attacker = currentActorPlayer ? player : enemy;
        p7Target = currentActorPlayer
                ? (selectedTarget != null ? selectedTarget : enemy)
                : player;
        if (!currentActorPlayer) {
            selectedTarget = player;
        }
        p7SkillId = selectedSkillFor(p7Attacker);
        p7EffectRow = VqsvBattleAnimationTables.instance().effectRow(p7SkillId);
        prepareP7SourceState(s);
        p7Phase = 0;
        p7Ticks = 0;
        p7Damage = 0;
        p7DamageResult = null;
        p7AttackHit = true;
        p7MissChance = 0;
        p7HitRoll = -1;
        p7PostEffectApplied = false;
        p7PostEffectText = "";
        p7PostEffectPlayerSide = false;
        p7DamageApplied = false;
        p7HpTweenActive = false;
        p7HpTweenPlayerSide = false;
        p7HpTweenStep = 0;
        p7HpTweenAccum = 0;
        p7HpTweenDelay = 0;
        p7DeathEffectActive = false;
        p7DeathEffectPlayerSide = false;
        p7DeathEffectSpriteId = -1;
        p7DeathEffectTicks = 0;
        p7DeathEffectDuration = 0;
        p7Prepared = true;
        clearP7RenderState(s);
    }

    private void prepareP7SourceState(VqsvIntroDemo.Scene s) {
        p7EffectChunk = 0;
        p7SourceI = 0;
        p7SourceJ = 0;
        p7SourceK = 0;
        p7SourceL = 0;
        p7FlagM = false;
        p7FlagN = false;
        p7FlagZ = false;
        p7FlagA = false;
        p7FlagB = false;
        p7SpecialPrepared = false;
        p7SpecialActive = false;
        p7SpecialTicks = 0;
        p7LEffectActive = false;
        p7LEffectPlayerSide = false;
        p7LEffectDrawAfter = false;
        p7LEffectTicks = 0;
        p7LEffectSpeffectId = -1;
        p7LEffectRow = new short[0];
        p7BaseHiddenPlayerSide = false;
        p7BaseHiddenEnemySide = false;
        p7BaseStatePlayerSide = 0;
        p7BaseStateEnemySide = 0;
        p7ActorAnimation = null;
        enterP7SourceChunk(s, "initial");
    }

    private void enterP7SourceChunk(VqsvIntroDemo.Scene s, String reason) {
        enterP7SourceChunk(s, reason, null);
    }

    private void enterP7SourceChunk(VqsvIntroDemo.Scene s, String reason, P7ActorAnimation preservedActorAnimation) {
        p7SourceJ = p7SourceI;
        p7EffectChunk = p7SourceJ;
        p7SpecialPrepared = false;
        p7SpecialActive = false;
        p7SpecialTicks = 0;
        p7ActorAnimation = preservedActorAnimation;
        prepareP7SpecialEffect(s);
        if ("initial".equals(reason)) {
            setP7BaseState(s, p7Attacker == player, p7EffectValue(0) == 0 ? 1 : 0);
        }
        if (p7EffectValue(1) == 1) {
            p7SpecialPrepared = true;
        } else {
            boolean targetSide = p7EffectValue(0) == 0;
            boolean playerSide = targetSide ? p7Target == player : p7Attacker == player;
            p7ActorAnimation = new P7ActorAnimation(playerSide, p7EffectValue(2), p7EffectValue(3));
        }
        p7SourceI++;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 source n() skill=" + p7SkillId
                + " chunk=" + p7SourceJ
                + " reason=" + reason
                + " side=" + p7EffectValue(0)
                + " special=" + p7EffectValue(1)
                + " id=" + p7EffectValue(2)
                + " param=" + p7EffectValue(3)
                + " nextI=" + p7SourceI);
    }

    private void prepareP7SpecialEffect(VqsvIntroDemo.Scene s) {
        p7SpeffectRow = new short[0];
        p7SpecialType = -1;
        if (p7EffectValue(1) != 1) {
            return;
        }
        int speffectId = p7EffectValue(2);
        p7SpeffectRow = VqsvBattleAnimationTables.instance().speffectRow(speffectId);
        p7SpecialType = p7SpeffectRow.length == 0 ? -1 : p7SpeffectRow[0];
        boolean ported = p7SpecialType == 9 || p7SpecialType == 1 || p7SpecialType == 7 || p7SpecialType == 8
                || p7SpecialType == 12;
        s.sourceStateTrace.add((ported ? "PORTED/PARTIAL" : "PENDING")
                + " battle P7 speffect skill=" + p7SkillId
                + " chunk=" + p7EffectChunk
                + " speffect=" + speffectId
                + " row=" + java.util.Arrays.toString(p7SpeffectRow)
                + " renderer=" + (ported ? "AH type " + p7SpecialType : "not ported in this slice"));
    }

    private int p7CurrentEffectDuration() {
        if (p7EffectValue(1) == 1 && p7SpeffectRow.length > 0) {
            if (p7SpecialType == 9 && p7SpeffectRow.length >= 8) {
                return Math.max(1, p7SpeffectRow[6]);
            }
            if (p7SpecialType == 1 && p7SpeffectRow.length >= 3) {
                return Math.max(1, p7SpeffectRow[2]);
            }
            if (p7SpecialType == 7 && p7SpeffectRow.length >= 3) {
                return Math.max(1, p7SpeffectRow[2]);
            }
            if (p7SpecialType == 8 && p7SpeffectRow.length >= 3) {
                return Math.max(1, p7SpeffectRow[2]);
            }
            if (p7SpecialType == 12 && p7SpeffectRow.length >= 6) {
                return Math.max(1, p7SpeffectRow[5]);
            }
        }
        return P7_START_TICKS;
    }

    private boolean tickP7SourceEffectSequence(VqsvIntroDemo.Scene s) {
        boolean actorBranchActive = false;
        if (p7ActorAnimation != null && !p7ActorAnimation.stopped()) {
            actorBranchActive = tickP7ActorAnimation(s);
        }
        if (p7SpecialPrepared) {
            return tickP7SpecialEffect(s) || actorBranchActive;
        }
        if (actorBranchActive) {
            return true;
        }
        if (p7FlagA) {
            return false;
        }
        p7FlagA = true;
        p7FlagB = true;
        return false;
    }

    private boolean tickP7ActorAnimation(VqsvIntroDemo.Scene s) {
        if (!p7ActorAnimation.started()) {
            p7ActorAnimation.start();
            p7FlagN = false;
            s.sourceStateTrace.add("PORTED battle P7 actor u.a() start skill=" + p7SkillId
                    + " chunk=" + p7SourceJ
                    + " sourceEffectId=" + p7ActorAnimation.sourceEffectId
                    + " sprite=" + p7ActorAnimation.spriteId
                    + " state=" + p7ActorAnimation.state
                    + " side=" + (p7ActorAnimation.playerSide ? "player" : "enemy"));
            return true;
        }
        int stateTrigger = p7EffectValue(5);
        if (stateTrigger != -1 && p7ActorAnimation.frame(stateTrigger)) {
            setP7BaseState(s, p7ActorAnimation.playerSide, p7EffectValue(6));
            p7SourceK = 0;
            s.sourceStateTrace.add("PORTED battle P7 chunk[5]/[6] state trigger skill=" + p7SkillId
                    + " chunk=" + p7SourceJ
                    + " frame=" + stateTrigger
                    + " state=" + p7EffectValue(6)
                    + " side=" + (p7ActorAnimation.playerSide ? "player" : "enemy"));
        }
        int nextTrigger = p7EffectValue(4);
        if (nextTrigger != -1 && p7ActorAnimation.frame(nextTrigger) && hasNextP7Chunk()) {
            boolean keepTargetActor = p7EffectValue(0) == 0;
            P7ActorAnimation preserved = keepTargetActor ? p7ActorAnimation : null;
            if (!keepTargetActor) {
                p7ActorAnimation.stop();
            }
            s.sourceStateTrace.add("PORTED battle P7 chunk[4] frame trigger skill=" + p7SkillId
                    + " chunk=" + p7SourceJ
                    + " frame=" + nextTrigger
                    + " cursor=" + p7ActorAnimation.cursor()
                    + " keepTargetActor=" + keepTargetActor);
            enterP7SourceChunk(s, "chunk4-frame-trigger", preserved);
            if (p7SpecialPrepared) {
                p7FlagM = true;
            }
            return true;
        }
        if (p7ActorAnimation.lastFrame()) {
            p7ActorAnimation.stop();
            if (hasNextP7Chunk()) {
                enterP7SourceChunk(s, "actor-animation-complete");
                if (p7ActorAnimation != null) {
                    p7ActorAnimation.start();
                }
                if (p7SpecialPrepared) {
                    p7FlagM = true;
                }
                return true;
            }
            p7FlagA = true;
            p7FlagB = true;
            return false;
        }
        p7ActorAnimation.tick();
        return true;
    }

    private boolean tickP7SpecialEffect(VqsvIntroDemo.Scene s) {
        if (!p7SpecialActive) {
            if (!(p7FlagM || p7ActorAnimation == null)) {
                return true;
            }
            if (p7SourceJ == 0) {
                p7FlagN = true;
            }
            setP7BaseState(s, p7Attacker == player, 0);
            p7SpecialActive = true;
            p7SpecialTicks = 0;
            p7SourceL = p7SourceJ;
            setP7BaseHidden(p7EffectValue(0) == 0 ? p7Target == player : p7Attacker == player, true);
            s.sourceStateTrace.add("PORTED battle P7 H.a() start skill=" + p7SkillId
                    + " chunk=" + p7SourceJ
                    + " ownerSide=" + p7EffectValue(0)
                    + " speffectType=" + p7SpecialType
                    + " actorHidden=" + (p7EffectValue(0) == 0 ? "target" : "attacker"));
        }
        p7SpecialTicks++;
        if (p7SpecialTicks <= p7CurrentEffectDuration()) {
            return true;
        }
        setP7BaseHidden(p7EffectValueAtChunk(p7SourceL, 0) == 0 ? p7Target == player : p7Attacker == player, false);
        p7SpecialPrepared = false;
        p7SpecialActive = false;
        p7FlagM = false;
        s.sourceStateTrace.add("PORTED battle P7 H.e() complete skill=" + p7SkillId
                + " chunk=" + p7SourceL
                + " restoreOwnerSide=" + p7EffectValueAtChunk(p7SourceL, 0));
        p7SourceL = 0;
        if (hasNextP7Chunk()) {
            if (p7EffectValueAtChunk(p7SourceI, 0) == 1) {
                p7FlagZ = true;
            }
            enterP7SourceChunk(s, "special-complete");
            if (p7SpecialPrepared) {
                p7FlagM = true;
            }
            return true;
        }
        if (p7EffectValue(0) == 0) {
            p7FlagZ = true;
        }
        p7FlagB = true;
        p7FlagA = true;
        return false;
    }

    private boolean hasNextP7Chunk() {
        return p7SourceI < p7EffectRow.length / 7;
    }

    private void setP7BaseState(VqsvIntroDemo.Scene s, boolean playerSide, int state) {
        int safe = Math.max(0, state);
        if (playerSide) {
            p7BaseStatePlayerSide = safe;
            p7BaseStateStartTickPlayerSide = s.battleAnimationTick;
        } else {
            p7BaseStateEnemySide = safe;
            p7BaseStateStartTickEnemySide = s.battleAnimationTick;
        }
    }

    private void setP7BaseHidden(boolean playerSide, boolean hidden) {
        if (playerSide) {
            p7BaseHiddenPlayerSide = hidden;
        } else {
            p7BaseHiddenEnemySide = hidden;
        }
    }

    private boolean p7NoDamageSkill() {
        BattleSkillRow row = VqsvBattleTables.instance().skill(p7SkillId);
        return row != null && row.powerPercent == 0;
    }

    private void markP7ActionUsed() {
        if (currentActorPlayer) {
            playerActionThisRound = true;
        } else {
            enemyActionThisRound = true;
        }
    }

    private int p7EffectAnimCursor() {
        return p7ActorAnimation == null ? 0 : p7ActorAnimation.cursor();
    }

    private boolean advanceP7EffectChunk(VqsvIntroDemo.Scene s, String reason) {
        int chunkCount = p7EffectRow.length / 7;
        if (p7EffectChunk + 1 >= chunkCount) {
            return false;
        }
        int previousChunk = p7EffectChunk;
        p7EffectChunk++;
        p7Ticks = 0;
        prepareP7SpecialEffect(s);
        syncP7RenderState(s, p7SkillName());
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 advance skill=" + p7SkillId
                + " chunk=" + previousChunk + "->" + p7EffectChunk
                + " reason=" + reason
                + " effectRow=" + java.util.Arrays.toString(p7EffectRow));
        return true;
    }

    private int selectedSkillFor(SourceBattleUnit unit) {
        if (unit != null && unit.battleUnit != null) {
            if (unit.battleUnit.selectedSkillId >= 0) {
                return unit.battleUnit.selectedSkillId;
            }
            for (int i = 0; i < unit.battleUnit.skillIds.length; i++) {
                int skill = unit.battleUnit.skillAt(i);
                if (skill >= 0) {
                    return skill;
                }
            }
        }
        return selectedSkillId >= 0 ? selectedSkillId : 0;
    }

    private String p7SkillName() {
        BattleSkillRow row = VqsvBattleTables.instance().skill(p7SkillId);
        return row == null ? (p7Attacker == null ? VqsvText.Battle.START : p7Attacker.name)
                : row.name("Skill " + p7SkillId);
    }

    private int p7EffectValue(int offset) {
        return p7EffectValueAtChunk(p7EffectChunk, offset);
    }

    private int p7EffectValueAtChunk(int chunk, int offset) {
        int index = chunk * 7 + offset;
        return index >= 0 && index < p7EffectRow.length ? p7EffectRow[index] : -1;
    }

    private String p7Phase2BattleLog() {
        if (!p7AttackHit) {
            return VqsvText.Battle.DODGE;
        }
        return p7Attacker.name + VqsvText.Battle.DAMAGE + p7Damage + VqsvText.Battle.DAMAGE_SUFFIX;
    }

    private void applyP7Damage(VqsvIntroDemo.Scene s) {
        if (p7DamageApplied) {
            return;
        }
        BattleUnit.setSourceRandomTrace(SOURCE_RANDOM, s.sourceStateTrace, "battle.P7.skill" + p7SkillId);
        try {
            p7DamageResult = p7Attacker.damageResultTo(p7Target);
        } finally {
            BattleUnit.clearRandomTrace();
        }
        p7Damage = Math.max(1, p7DamageResult.damage);
        p7MissChance = sourceP7MissChance(s);
        p7HitRoll = sourceP7HitRoll(s);
        p7AttackHit = p7HitRoll >= p7MissChance;
        if (p7AttackHit) {
            p7Target.damage(p7Damage);
            setP7BaseState(s, p7Target == player, 2);
        }
        startP7HpTween(p7Target == player);
        markP7ActionUsed();
        p7DamageApplied = true;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 hitroll skill=" + p7SkillId
                + " roll=" + p7HitRoll
                + " missChance=" + p7MissChance
                + " hit=" + p7AttackHit
                + " source=game.d case7 ae.a(100)>=missChance");
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 damage frame skill=" + p7SkillId
                + " damage=" + p7Damage
                + " critFlag=" + p7DamageResult.critFlag
                + " appliedDebuffId=" + p7DamageResult.appliedDebuffId
                + " debuffText=" + p7DebuffText()
                + " target=" + p7Target.name
                + " hp=" + p7Target.hp + "/" + p7Target.maxHp
                + " hit=" + p7AttackHit
                + " bloodRow0Len=" + VqsvBattleAnimationTables.instance().bloodRow(0).length);
    }

    private int sourceP7MissChance(VqsvIntroDemo.Scene s) {
        if (p7Attacker == null || p7Target == null
                || p7Attacker.battleUnit == null || p7Target.battleUnit == null) {
            return 0;
        }
        BattleUnit attacker = p7Attacker.battleUnit;
        BattleUnit target = p7Target.battleUnit;
        int attackerSpeed = attacker.currentStats[BattleUnit.STAT_SPEED];
        int sourceAttackerSpeed = attackerSpeed;
        if (attacker.hasDebuff(4)) {
            sourceAttackerSpeed = attackerSpeed - attacker.debuffSlots[4][1];
        }
        int missChance = (target.currentStats[BattleUnit.STAT_SPEED] - sourceAttackerSpeed) << 1;
        if (attacker.hasSourceFormStatus(9)) {
            missChance = 0;
        }
        if (missChance <= 0) {
            missChance = 0;
        } else if (missChance >= 20) {
            missChance = 20;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 missChance skill=" + p7SkillId
                + " targetSpeed=" + target.currentStats[BattleUnit.STAT_SPEED]
                + " attackerSpeed=" + attackerSpeed
                + " debuff4Value=" + (attacker.hasDebuff(4) ? attacker.debuffSlots[4][1] : 0)
                + " form9=" + attacker.hasSourceFormStatus(9)
                + " missChance=" + missChance
                + " passive4=PENDING");
        return missChance;
    }

    private int sourceP7HitRoll(VqsvIntroDemo.Scene s) {
        if (debugNextP7HitRoll >= 0) {
            int roll = debugNextP7HitRoll;
            debugNextP7HitRoll = -1;
            s.sourceStateTrace.add("SMOKE battle P7 forced hitroll roll=" + roll
                    + " source=game.d case7 ae.a(100)");
            return roll;
        }
        return SOURCE_RANDOM.a("battle.P7.skill" + p7SkillId + ".hitroll", 100, s.sourceStateTrace);
    }

    private void startP7HpTween(boolean playerSide) {
        p7HpTweenPlayerSide = playerSide;
        int display = playerSide ? playerDisplayHp : enemyDisplayHp;
        int actual = playerSide ? player.hp : enemy.hp;
        p7HpTweenStep = Math.max(1, Math.abs(display - actual) / 11);
        p7HpTweenAccum = 0;
        p7HpTweenDelay = 0;
        p7HpTweenActive = display != actual;
    }

    private void tickP7HpTween() {
        if (!p7HpTweenActive) {
            return;
        }
        int display = p7HpTweenPlayerSide ? playerDisplayHp : enemyDisplayHp;
        int actual = p7HpTweenPlayerSide ? player.hp : enemy.hp;
        if (display == actual) {
            p7HpTweenActive = false;
            p7HpTweenStep = 0;
            p7HpTweenAccum = 0;
            p7HpTweenDelay = 0;
            return;
        }
        p7HpTweenDelay++;
        if (p7HpTweenDelay < 4) {
            return;
        }
        p7HpTweenAccum += p7HpTweenStep;
        if (display < actual) {
            display = Math.min(actual, display + p7HpTweenAccum);
        } else {
            display = Math.max(actual, display - p7HpTweenAccum);
        }
        if (p7HpTweenPlayerSide) {
            playerDisplayHp = display;
        } else {
            enemyDisplayHp = display;
        }
        if (display == actual) {
            p7HpTweenActive = false;
            p7HpTweenStep = 0;
            p7HpTweenAccum = 0;
            p7HpTweenDelay = 0;
        }
    }

    private boolean p7DamageTextActive() {
        if (!p7DamageApplied) {
            return false;
        }
        return p7Ticks < p7DamageTextFrameCount();
    }

    private int p7DamageTextFrameCount() {
        if (!p7AttackHit) {
            return Math.max(1, VqsvBattleAnimationTables.instance().bloodRow(1).length / 2);
        }
        int frames = VqsvBattleAnimationTables.instance().bloodRow(0).length / 2;
        if (p7DamageResult != null && p7DamageResult.appliedDebuffId >= 0) {
            frames = Math.max(frames, VqsvBattleAnimationTables.instance().bloodRow(1).length / 2);
        }
        return Math.max(1, frames);
    }

    private void startP7DeathEffect(VqsvIntroDemo.Scene s, boolean playerSide) {
        SourceBattleUnit unit = playerSide ? player : enemy;
        if (unit == null) {
            return;
        }
        setP7BaseState(s, playerSide, 3);
        setP7BaseHidden(playerSide, true);
        if (playerSide) {
            p7KoBaseHiddenPlayerSide = true;
        } else {
            p7KoBaseHiddenEnemySide = true;
        }
        p7DeathEffectActive = true;
        p7DeathEffectPlayerSide = playerSide;
        p7DeathEffectSpriteId = unit.visualId;
        p7DeathEffectTicks = 0;
        p7DeathEffectDuration = p7DeathEffectDuration(unit.visualId);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 U() death state3 AH type16 start"
                + " side=" + (playerSide ? "player" : "enemy")
                + " visual=" + p7DeathEffectSpriteId
                + " duration=" + p7DeathEffectDuration
                + " sourceRow=[16,x,y,sprite,0,dir,0,0,4]");
    }

    private void tickP7DeathEffect() {
        if (!p7DeathEffectActive) {
            return;
        }
        p7DeathEffectTicks++;
        if (p7DeathEffectTicks >= p7DeathEffectDuration) {
            p7DeathEffectActive = false;
        }
    }

    private int p7DeathEffectDuration(int spriteId) {
        SpriteAnim anim = SpriteAnim.load(spriteId);
        int[] bounds = anim.animationBounds(0);
        if (bounds == null || bounds[3] <= 0) {
            return 8;
        }
        return Math.max(4, bounds[3] / 4);
    }

    private boolean finishP7(VqsvIntroDemo.Scene s) {
        clearP7RenderState(s);
        if (isBunnyCaptureBattle() && currentActorPlayer && !bunnyTutorialShown && enemy.hp <= enemy.maxHp / 2) {
            bunnyTutorialShown = true;
            bunnyTutorialWeakPromptActive = true;
            bunnyTutorialFirstCatchPending = true;
            setBunnyTutorialState(s, 0, 1, "game.d.l() V=0->1 Bunny HP <= 50%");
            s.battleCommandIndex = 1;
            s.text = TextBox.taskTip(VqsvText.Battle.BUNNY_WEAK);
            syncRenderState(s, VqsvText.Battle.BUNNY_WEAK);
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial game.d.l(): HP<=50%, "
                    + "taskTip.ui weak prompt before P20 catch cursor; P17 animation still partial");
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.BUNNY_WEAK, SHORT_WAIT);
            return false;
        }
        if (isBunnyCaptureBattle() && !currentActorPlayer && bunnyTutorialRetryPromptAfterEnemy) {
            bunnyTutorialRetryPromptAfterEnemy = false;
            bunnyTutorialRetryPromptActive = true;
            setBunnyTutorialState(s, 0, 6, "game.d.l() V=5->6 after Bunny counterattack retry taskTip");
            s.text = TextBox.taskTip(VqsvText.Battle.BUNNY_RETRY_TAT_TRUNG_CAU);
            syncRenderState(s, VqsvText.Battle.BUNNY_RETRY_TAT_TRUNG_CAU);
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial U=0,V=5->6 "
                    + "after enemy P7 counterattack, taskTip retry Tat Trung Cau");
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.BUNNY_RETRY_TAT_TRUNG_CAU, SHORT_WAIT);
            return false;
        }
        if (!p7Target.alive()) {
            consumeP7FollowUpDeadTargetMarker(s);
            if (currentActorPlayer && p7Target == enemy && prepareEnemyReplacement(s)) {
                return false;
            }
            enterState(s, currentActorPlayer ? BattleRuntimeState.P8_WIN : BattleRuntimeState.P9_LOSE,
                    currentActorPlayer ? battleWinLog() : VqsvText.Battle.NEIL_LOST + forcedResultIndex,
                    SHORT_WAIT);
            return false;
        }
        if (tryEnterP7FollowUpAction(s)) {
            return false;
        }
        enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, SHORT_WAIT);
        return false;
    }

    private void consumeP7FollowUpDeadTargetMarker(VqsvIntroDemo.Scene s) {
        if (p7Attacker == null || p7Attacker.battleUnit == null) {
            return;
        }
        if ((p7SkillId == 63 || p7SkillId == 69)
                && p7Attacker.battleUnit.hasBuff(12)
                && p7Attacker.battleUnit.effectScratch[12] > 0) {
            p7Attacker.battleUnit.effectScratch[12] = (short) Math.max(0,
                    p7Attacker.battleUnit.effectScratch[12] - 1);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P7 game.d.q skill63/69 dead-target "
                    + "decrement K12=" + p7Attacker.battleUnit.effectScratch[12]);
        }
    }

    private boolean tryEnterP7FollowUpAction(VqsvIntroDemo.Scene s) {
        if (p7Attacker == null || p7Attacker.battleUnit == null || p7Target == null || !p7Target.alive()) {
            return false;
        }
        if (p7Attacker.battleUnit.hasBuff(12) && p7Attacker.battleUnit.effectScratch[12] == 2) {
            p7Attacker.battleUnit.effectScratch[12] = 1;
            currentActorPlayer = p7Attacker == player;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P7 game.d.q follow-up P2 from buff12 K12=2->1"
                    + " actor=" + p7Attacker.name
                    + " targetAlive=" + p7Target.alive());
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, p7SkillName(), SHORT_WAIT);
            return true;
        }
        if (p7SkillId == 63 || p7SkillId == 69) {
            BattleSkillRow row = VqsvBattleTables.instance().skill(p7SkillId);
            int chance = row == null ? 0 : Math.max(0, row.chanceOrParam);
            int roll = sourceP7FollowUpRoll(s);
            if (roll <= chance) {
                currentActorPlayer = p7Attacker == player;
                s.sourceStateTrace.add("PORTED/PARTIAL battle P7 game.d.q follow-up P2 from skill="
                        + p7SkillId + " roll=" + roll + " chance=" + chance
                        + " actor=" + p7Attacker.name);
                enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, p7SkillName(), SHORT_WAIT);
                return true;
            }
            s.sourceStateTrace.add("PORTED/PARTIAL battle P7 game.d.q no follow-up skill="
                    + p7SkillId + " roll=" + roll + " chance=" + chance);
        }
        return false;
    }

    private int sourceP7FollowUpRoll(VqsvIntroDemo.Scene s) {
        if (debugNextFollowUpRoll >= 0) {
            int roll = debugNextFollowUpRoll;
            debugNextFollowUpRoll = -1;
            s.sourceStateTrace.add("SMOKE battle P7 forced follow-up roll=" + roll
                    + " source=game.d.q ae.a(100)");
            return roll;
        }
        return SOURCE_RANDOM.a("battle.P7.q.followup.skill" + p7SkillId, 100, s.sourceStateTrace);
    }

    private void syncP7RenderState(VqsvIntroDemo.Scene s, String log) {
        syncRenderState(s, log);
        s.battleP7Phase = p7Phase;
        s.battleP7Ticks = p7Ticks;
        s.battleP7AttackerPlayerSide = p7Attacker == player;
        s.battleP7TargetPlayerSide = p7Target == player;
        boolean effectOnTarget = p7EffectValue(0) == 0;
        s.battleP7EffectOnPlayerSide = effectOnTarget ? s.battleP7TargetPlayerSide : s.battleP7AttackerPlayerSide;
        s.battleP7EffectAnimState = p7ActorAnimation != null ? p7ActorAnimation.state : 0;
        s.battleP7EffectAnimCursor = p7EffectAnimCursor();
        s.battleP7BaseHiddenPlayerSide = p7BaseHiddenPlayerSide || p7KoBaseHiddenPlayerSide;
        s.battleP7BaseHiddenEnemySide = p7BaseHiddenEnemySide || p7KoBaseHiddenEnemySide;
        s.battleP7BaseStatePlayerSide = p7BaseStatePlayerSide;
        s.battleP7BaseStateEnemySide = p7BaseStateEnemySide;
        s.battleP7BaseCursorPlayerSide = p7BaseCursor(s, true);
        s.battleP7BaseCursorEnemySide = p7BaseCursor(s, false);
        s.battleP7DeathEffectVisible = p7DeathEffectActive;
        s.battleP7DeathEffectPlayerSide = p7DeathEffectActive && p7DeathEffectPlayerSide;
        s.battleP7DeathEffectSpriteId = p7DeathEffectActive ? p7DeathEffectSpriteId : -1;
        s.battleP7DeathEffectTick = p7DeathEffectActive ? p7DeathEffectTicks : 0;
        s.battleP7DeathEffectDuration = p7DeathEffectActive ? p7DeathEffectDuration : 0;
        syncP7MotionOffsets(s);
        syncP7LEffectRenderState(s);
        s.battleP7ActorEffectVisible = p7Phase == 1
                && p7ActorAnimation != null
                && p7ActorAnimation.started()
                && !p7ActorAnimation.stopped();
        s.battleP7ActorEffectOnPlayerSide = s.battleP7ActorEffectVisible && p7ActorAnimation.playerSide;
        s.battleP7ActorEffectSpriteId = s.battleP7ActorEffectVisible ? p7ActorAnimation.spriteId : -1;
        s.battleP7ActorEffectState = s.battleP7ActorEffectVisible ? p7ActorAnimation.state : 0;
        s.battleP7ActorEffectCursor = s.battleP7ActorEffectVisible ? p7ActorAnimation.cursor() : 0;
        s.battleP7DamageVisible = p7Phase == 2 && p7DamageTextActive();
        s.battleP7DamageText = s.battleP7DamageVisible && p7AttackHit ? "-" + p7Damage : "";
        s.battleP7DamageCritical = s.battleP7DamageVisible && p7AttackHit && p7DamageResult != null
                && p7DamageResult.critFlag == 1;
        s.battleP7DebuffText = s.battleP7DamageVisible && p7AttackHit ? p7DebuffText() : "";
        s.battleP7MissText = s.battleP7DamageVisible && !p7AttackHit ? VqsvText.Battle.DODGE : "";
        s.battleP7PostEffectVisible = p7Phase == 3 && !p7PostEffectText.isEmpty();
        s.battleP7PostEffectPlayerSide = p7PostEffectPlayerSide;
        s.battleP7PostEffectText = s.battleP7PostEffectVisible ? p7PostEffectText : "";
        boolean showSpecial = p7Phase == 1
                && (p7SpecialType == 9 || p7SpecialType == 1 || p7SpecialType == 7 || p7SpecialType == 8
                || p7SpecialType == 12)
                && p7SpecialActive
                && p7SpecialTicks <= p7CurrentEffectDuration();
        s.battleP7SpecialVisible = showSpecial;
        s.battleP7SpecialOnPlayerSide = s.battleP7EffectOnPlayerSide;
        s.battleP7SpecialType = showSpecial ? p7SpecialType : -1;
        s.battleP7SpecialAlpha = showSpecial && p7SpecialType == 9 ? p7SpeffectRow[1] : 0;
        s.battleP7SpecialRed = showSpecial && p7SpecialType == 9 ? p7SpeffectRow[2] : 0;
        s.battleP7SpecialGreen = showSpecial && p7SpecialType == 9 ? p7SpeffectRow[3] : 0;
        s.battleP7SpecialBlue = showSpecial && p7SpecialType == 9 ? p7SpeffectRow[4] : 0;
        s.battleP7SpecialDuration = showSpecial ? p7CurrentEffectDuration() : 0;
        s.battleP7SpecialInterval = showSpecial && p7SpecialType == 9 && p7SpeffectRow.length >= 8
                ? Math.max(1, p7SpeffectRow[7]) : 1;
        s.battleP7SpecialTextureId = showSpecial && p7SpecialType == 1 && p7SpeffectRow.length >= 4
                ? p7SpeffectRow[3] : -1;
        s.battleP7SpecialBlendMode = showSpecial && p7SpecialType == 1 && p7SpeffectRow.length >= 5
                ? p7SpeffectRow[4] : 0;
        s.battleP7SpecialScrollMode = showSpecial && p7SpecialType == 1 && p7SpeffectRow.length >= 6
                ? p7SpeffectRow[5] : 0;
        s.battleP7SpecialRow = showSpecial ? Arrays.copyOf(p7SpeffectRow, p7SpeffectRow.length) : new short[0];
    }

    private void clearP7RenderState(VqsvIntroDemo.Scene s) {
        s.battleP7Phase = 0;
        s.battleP7Ticks = 0;
        s.battleP7EffectAnimState = -1;
        s.battleP7EffectAnimCursor = 0;
        s.battleP7BaseHiddenPlayerSide = p7KoBaseHiddenPlayerSide;
        s.battleP7BaseHiddenEnemySide = p7KoBaseHiddenEnemySide;
        s.battleP7BaseStatePlayerSide = 0;
        s.battleP7BaseStateEnemySide = 0;
        s.battleP7PlayerOffsetX = 0;
        s.battleP7PlayerOffsetY = 0;
        s.battleP7EnemyOffsetX = 0;
        s.battleP7EnemyOffsetY = 0;
        s.battleP7DeathEffectVisible = false;
        s.battleP7DeathEffectPlayerSide = false;
        s.battleP7DeathEffectSpriteId = -1;
        s.battleP7DeathEffectTick = 0;
        s.battleP7DeathEffectDuration = 0;
        s.battleLVisible = false;
        s.battleLPlayerSide = false;
        s.battleLDrawAfter = false;
        s.battleLType = -1;
        s.battleLSpriteId = -1;
        s.battleLFrame = 0;
        s.battleLDirection = 0;
        s.battleLRow = new short[0];
        s.battleP7ActorEffectVisible = false;
        s.battleP7ActorEffectOnPlayerSide = false;
        s.battleP7ActorEffectSpriteId = -1;
        s.battleP7ActorEffectState = 0;
        s.battleP7ActorEffectCursor = 0;
        s.battleP7DamageVisible = false;
        s.battleP7DamageText = "";
        s.battleP7DamageCritical = false;
        s.battleP7DebuffText = "";
        s.battleP7MissText = "";
        s.battleP7PostEffectVisible = false;
        s.battleP7PostEffectPlayerSide = false;
        s.battleP7PostEffectText = "";
        s.battleP7SpecialVisible = false;
        s.battleP7SpecialOnPlayerSide = false;
        s.battleP7SpecialType = -1;
        s.battleP7SpecialAlpha = 0;
        s.battleP7SpecialRed = 0;
        s.battleP7SpecialGreen = 0;
        s.battleP7SpecialBlue = 0;
        s.battleP7SpecialDuration = 0;
        s.battleP7SpecialInterval = 1;
        s.battleP7SpecialTextureId = -1;
        s.battleP7SpecialBlendMode = 0;
        s.battleP7SpecialScrollMode = 0;
        s.battleP7SpecialRow = new short[0];
    }

    private void syncP7MotionOffsets(VqsvIntroDemo.Scene s) {
        s.battleP7PlayerOffsetX = 0;
        s.battleP7PlayerOffsetY = 0;
        s.battleP7EnemyOffsetX = 0;
        s.battleP7EnemyOffsetY = 0;
    }

    private String p7DebuffText() {
        if (p7DamageResult == null || p7DamageResult.appliedDebuffId < 0) {
            return "";
        }
        BattleDebuffRow row = VqsvBattleTables.instance().debuff(p7DamageResult.appliedDebuffId);
        return row == null ? "" : row.name("");
    }

    private void applyP7PostSkillEffects(VqsvIntroDemo.Scene s) {
        if (p7PostEffectApplied) {
            return;
        }
        p7PostEffectApplied = true;
        BattleSkillRow row = VqsvBattleTables.instance().skill(p7SkillId);
        if (row == null || p7Attacker == null) {
            return;
        }
        BattleUnit.setSourceRandomTrace(SOURCE_RANDOM, s.sourceStateTrace, "battle.P7.skill" + p7SkillId);
        int heal = 0;
        int buffId = -1;
        SourceBattleUnit buffOwner = p7Attacker;
        boolean sourceTargetSelf = true;
        boolean leechRollPassed = false;
        int attackerHpBefore = p7Attacker.hp;
        switch (p7SkillId) {
            case 11:
            case 17:
                if (p7Attacker.battleUnit != null) {
                    heal = Math.max(1, p7Attacker.battleUnit.sourceBaseAttackForCurrentTarget()
                            * row.chanceOrParam / 100);
                }
                break;
            case 21:
            case 27:
            case 42:
            case 48:
            case 62:
            case 68:
                buffId = row.effectId;
                break;
            case 52:
            case 58:
                leechRollPassed = sourceP7LeechRollPassed(s);
                if (leechRollPassed) {
                    heal = Math.max(0, p7Damage * row.chanceOrParam / 100);
                }
                buffId = row.effectId;
                break;
            case 64:
                buffId = row.effectId;
                break;
            default:
                if (row.effectMode == 1 && p7Target != null) {
                    buffId = row.effectId;
                    buffOwner = p7Target;
                    sourceTargetSelf = false;
                }
                break;
        }
        if (heal > 0 && p7Attacker.battleUnit != null) {
            p7Attacker.battleUnit.heal(heal);
            p7Attacker.hp = p7Attacker.battleUnit.hp();
        }
        int buffHeal = 0;
        if (buffId >= 0 && buffOwner != null) {
            int selectedIndex = p7SkillId == 64 && p7Attacker.battleUnit != null
                    ? p7Attacker.battleUnit.selectedTargetSlot : -1;
            if (p7SkillId == 64 && p7Attacker.battleUnit != null && p7Target != null && p7Target.battleUnit != null) {
                p7Attacker.battleUnit.copySourceBuffsFrom(p7Target.battleUnit, selectedIndex, p7SkillId);
                buffHeal = 0;
            } else {
                buffHeal = buffOwner.applySourceBuff(buffId, selectedIndex, p7SkillId);
            }
            BattleBuffRow buff = VqsvBattleTables.instance().buff(buffId);
            p7PostEffectText = buffHeal > 0 ? "+" + buffHeal : buff == null ? "" : buff.name("");
            p7PostEffectPlayerSide = buffOwner == player;
        }
        applyP7SourcePostDamageModifiers();
        BattleUnit.clearRandomTrace();
        if (p7Attacker.hp < attackerHpBefore) {
            p7PostEffectText = "-" + (attackerHpBefore - p7Attacker.hp);
            p7PostEffectPlayerSide = p7Attacker == player;
        } else if (p7PostEffectText.isEmpty() && heal > 0) {
            p7PostEffectText = "+" + heal;
            p7PostEffectPlayerSide = p7Attacker == player;
        }
        if (!p7PostEffectText.isEmpty()) {
            s.sourceStateTrace.add("PORTED/PARTIAL battle P7 game.d.q postEffect skill=" + p7SkillId
                    + " text=" + p7PostEffectText
                    + " selfTarget=" + sourceTargetSelf
                    + " owner=" + (p7PostEffectPlayerSide ? "player" : "enemy")
                    + " heal=" + heal
                    + " buffId=" + buffId
                    + " buffHeal=" + buffHeal
                    + " leechRollPassed=" + leechRollPassed);
        }
    }

    private boolean sourceP7LeechRollPassed(VqsvIntroDemo.Scene s) {
        int roll;
        if (debugNextLeechRoll >= 0) {
            roll = debugNextLeechRoll;
            debugNextLeechRoll = -1;
            s.sourceStateTrace.add("SMOKE battle P7 forced leech roll=" + roll
                    + " source=game.d case7 skill52/58 aa gate ae.a(100)");
        } else {
            roll = SOURCE_RANDOM.a("battle.P7.skill52_58.leechGate", 100, s.sourceStateTrace);
        }
        boolean passed = roll <= 30;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 source aa skill=" + p7SkillId
                + " roll=" + roll
                + " passed=" + passed
                + " hit=" + p7AttackHit
                + " source=game.d case7 sets aa before miss gate; game.d.q checks aa only");
        return passed;
    }

    private void applyP7SourcePostDamageModifiers() {
        if (!p7AttackHit) {
            return;
        }
        if (p7Attacker == null || p7Attacker.battleUnit == null || p7Target == null || p7Target.battleUnit == null) {
            return;
        }
        BattleSkillRow row = VqsvBattleTables.instance().skill(p7SkillId);
        if (row == null || row.targetSide != 0) {
            return;
        }
        BattleUnit attackerUnit = p7Attacker.battleUnit;
        BattleUnit targetUnit = p7Target.battleUnit;
        if (attackerUnit.hasSourceFormStatus(8)) {
            int chance = attackerUnit.sourceStatusParam(8, 5, 0);
            if (attackerUnit.rollSourceChance("damage.form8", chance)) {
                int heal = Math.max(0, p7Damage * attackerUnit.sourceStatusParam(8, 6, 0) / 100);
                attackerUnit.heal(heal);
                p7Attacker.hp = attackerUnit.hp();
            }
        }
        if (targetUnit.hasBuff(2)) {
            int reflect = Math.max(0, p7Damage * targetUnit.buffSlots[2][2] / 100);
            p7Attacker.damage(reflect);
        }
        if (targetUnit.hasBuff(5)) {
            int stored = attackerUnit.consumeStoredReflectDamage();
            if (stored > 0) {
                p7Attacker.damage(stored);
            }
        }
    }

    private void tickP7LEffect(VqsvIntroDemo.Scene s) {
        if (p7LEffectActive) {
            p7LEffectTicks++;
            if (p7LEffectTicks >= p7LEffectFrameCount()) {
                p7LEffectActive = false;
                s.sourceStateTrace.add("PORTED/PARTIAL battle state1 L complete species="
                        + p7Attacker.speciesId + " speffect=" + p7LEffectSpeffectId);
            }
            return;
        }
        if (p7Attacker == null || p7Attacker.visualId < 0 || p7BaseStateFor(p7Attacker == player) != 1) {
            return;
        }
        int speffectId = state1LEffectSpeffectId(p7Attacker.speciesId);
        if (speffectId < 0) {
            return;
        }
        int cursor = p7BaseCursor(s, p7Attacker == player);
        if (cursor != 1) {
            return;
        }
        p7LEffectRow = VqsvBattleAnimationTables.instance().speffectRow(speffectId);
        if (p7LEffectRow.length == 0) {
            return;
        }
        p7LEffectActive = true;
        p7LEffectPlayerSide = p7Attacker == player;
        p7LEffectDrawAfter = p7Attacker.speciesId == 10;
        p7LEffectTicks = 0;
        p7LEffectSpeffectId = speffectId;
        s.sourceStateTrace.add("PORTED/PARTIAL battle state1 L start species=" + p7Attacker.speciesId
                + " visual=" + p7Attacker.visualId
                + " speffect=" + speffectId
                + " row=" + java.util.Arrays.toString(p7LEffectRow)
                + " cursor=" + cursor
                + " drawAfter=" + p7LEffectDrawAfter);
    }

    private void syncP7LEffectRenderState(VqsvIntroDemo.Scene s) {
        boolean show = p7LEffectActive && p7LEffectRow.length > 0 && p7Phase == 1;
        s.battleLVisible = show;
        s.battleLPlayerSide = show && p7LEffectPlayerSide;
        s.battleLDrawAfter = show && p7LEffectDrawAfter;
        s.battleLType = show ? p7LEffectRow[0] : -1;
        s.battleLSpriteId = show && p7Attacker != null ? p7Attacker.visualId : -1;
        s.battleLFrame = show ? Math.max(0, Math.min(p7LEffectFrameCount() - 1, p7LEffectTicks)) : 0;
        s.battleLDirection = 0;
        s.battleLRow = show ? java.util.Arrays.copyOf(p7LEffectRow, p7LEffectRow.length) : new short[0];
    }

    private int p7BaseStateFor(boolean playerSide) {
        return playerSide ? p7BaseStatePlayerSide : p7BaseStateEnemySide;
    }

    private int p7BaseCursor(VqsvIntroDemo.Scene s, boolean playerSide) {
        SourceBattleUnit unit = playerSide ? player : enemy;
        if (unit == null || unit.visualId < 0) {
            return -1;
        }
        int startTick = playerSide ? p7BaseStateStartTickPlayerSide : p7BaseStateStartTickEnemySide;
        return battleSpriteCursor(unit.visualId, p7BaseStateFor(playerSide), s.battleAnimationTick - startTick);
    }

    private int p7LEffectFrameCount() {
        if (p7LEffectRow.length < 3) {
            return 0;
        }
        int type = p7LEffectRow[0];
        if (type == 11 || type == 14 || type == 15) {
            int count = Math.max(1, p7LEffectRow[1]);
            int tStart = 2 + ((count - 1) << 2);
            return tStart + 1 < p7LEffectRow.length ? Math.max(1, p7LEffectRow[tStart + 1]) : 1;
        }
        if (type == 12) {
            return p7LEffectRow.length > 5 ? Math.max(1, p7LEffectRow[5]) : 1;
        }
        if (type == 13) {
            int count = Math.max(1, p7LEffectRow[1]);
            int tStart = 2 + count;
            return tStart + 1 < p7LEffectRow.length ? Math.max(1, p7LEffectRow[tStart + 1]) : 1;
        }
        return 1;
    }

    private int state1LEffectSpeffectId(int species) {
        switch (species) {
            case 0:
                return 27;
            case 10:
                return 28;
            case 91:
                return 26;
            case 92:
                return 25;
            case 97:
            case 98:
                return 23;
            case 62:
                return 24;
            case 75:
                return 20;
            case 87:
                return 21;
            default:
                return -1;
        }
    }

    private int battleSpriteCursor(int spriteIndex, int state, int tick) {
        SpriteAnim anim = SpriteAnim.load(spriteIndex);
        anim.setState(Math.max(0, state));
        if (anim.data.anim == null || anim.data.anim.length == 0 || anim.data.anim[anim.state].length == 0) {
            return 0;
        }
        int elapsed = Math.max(0, tick);
        short[] frames = anim.data.anim[anim.state];
        int total = 0;
        for (int i = 0; i < frames.length; i += 2) {
            total += Math.max(1, frames[i]);
        }
        int wrapped = total <= 0 ? 0 : elapsed % total;
        int sum = 0;
        for (int i = 0; i < frames.length; i += 2) {
            sum += Math.max(1, frames[i]);
            if (wrapped < sum) {
                return i / 2;
            }
        }
        return 0;
    }

    private boolean tickWin(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (tickWinExpLevelUp(s)) {
            return false;
        }
        int result = isBunnyCaptureBattle() ? forcedResultIndex : 0;
        s.battleResultIndex = result;
        s.battleBranchTarget = resolveBranch(s.battleResultIndex);
        persistActivePlayerPet(s, "P8 win");
        s.battleLevelUpView = VqsvBattleLevelUpView.EMPTY;
        syncRenderState(s, battleWinLog());
        s.sourceStateTrace.add("PORTED/PARTIAL battle P8 resultIndex="
                + s.battleResultIndex + " branch=" + s.battleBranchTarget
                + " playerHp=" + player.hp + "/" + player.maxHp
                + " enemyHp=" + enemy.hp + "/" + enemy.maxHp);
        enterState(s, BattleRuntimeState.EXIT_FADE, s.battleLog, EXIT_WAIT);
        return false;
    }

    private boolean tickWinExpLevelUp(VqsvIntroDemo.Scene s) {
        if (!expPrepared) {
            prepareWinExp(s);
        }
        if (!expEligible || expCurrentPet == null || expCurrentUnit == null) {
            return false;
        }
        BattleUnit unit = expCurrentUnit;
        if (expLearningSkill) {
            return tickLevelUpSkillLearn(s, unit);
        }
        if (expLevelUpPending && !expLevelUpApplied) {
            expOldStats = unit.sourceVisibleStats();
            unit.sourceLevelUpOnce();
            SourceEvolutionNotice evolutionNotice = produceSourceEvolutionQueue(s, unit);
            expNewStats = unit.sourceVisibleStats();
            expDisplayValue = Math.min(unit.exp, unit.nextLevelEnergy());
            expLevelUpApplied = true;
            expLearnSkillIds = unit.sourceCanLearnAfterLevelUp() ? unit.sourceLearnCandidateSkillIds() : new int[0];
            expHoldTicks = 40;
            syncExpPetAfterExp(s, unit);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P22 game.h.an/ao levelUp species="
                    + unit.speciesId + " level=" + unit.level
                    + " jIndex=" + expDisplayIndex + "/" + sourceExpDisplay.size()
                    + " exp=" + unit.exp + "/" + unit.nextLevelEnergy()
                    + " oldStats=" + Arrays.toString(expOldStats)
                    + " newStats=" + Arrays.toString(expNewStats)
                    + " learnSkills=" + Arrays.toString(expLearnSkillIds)
                    + " evolutionQueue=" + (evolutionNotice == null ? "none" : "created")
                    + " evolution-ui/effect=PENDING");
        }
        if (expLevelUpApplied) {
            s.battleUiMode = "levelup";
            s.battleLevelUpView = levelUpView(s, unit, true);
            if (s.key0 || expHoldTicks-- <= 0) {
                s.key0 = false;
                if (expLearnSkillIds.length > 0) {
                    prepareLevelUpSkillMenu(s);
                    expLearningSkill = true;
                    expLevelUpApplied = false;
                    return true;
                }
                if (unit.canSourceLevelUp()) {
                    expLevelUpApplied = false;
                    expLevelUpPending = true;
                    return true;
                }
                return finishCurrentExpPet(s, "P8 levelUp");
            }
            return true;
        }
        int target = Math.min(unit.exp, unit.nextLevelEnergy());
        if (s.key0 && expDisplayValue < target) {
            s.key0 = false;
            expDisplayValue = target;
            s.battleUiMode = "levelup";
            s.battleLevelUpView = levelUpView(s, unit, false);
            s.sourceStateTrace.add("PORTED battle P8 game.h.am confirm fast-forward exp="
                    + expDisplayValue + "/" + unit.nextLevelEnergy()
                    + " jIndex=" + expDisplayIndex + "/" + sourceExpDisplay.size());
            return true;
        }
        expDisplayValue = Math.min(target, expDisplayValue + 8);
        s.battleUiMode = "levelup";
        s.battleLevelUpView = levelUpView(s, unit, false);
        if (expDisplayValue >= target) {
            if (unit.canSourceLevelUp()) {
                expLevelUpPending = true;
                return true;
            }
            if (s.key0 || expHoldTicks++ >= 10) {
                s.key0 = false;
                return finishCurrentExpPet(s, "P8 exp");
            }
        }
        return true;
    }

    private boolean tickLevelUpSkillLearn(VqsvIntroDemo.Scene s, BattleUnit unit) {
        if (expLearningConfirm) {
            s.battleUiMode = "warning";
            if (s.key0) {
                s.key0 = false;
                if (s.text != null && s.text.sourceUiKind == TextBox.SOURCE_MSGWARM) {
                    s.text = null;
                }
                boolean learned = unit.learnSourceSkill(expSelectedLearnSkill);
                syncExpPetAfterExp(s, unit);
                s.sourceStateTrace.add("PORTED/PARTIAL battle P23 game.h.aq learn skill="
                        + expSelectedLearnSkill + " learned=" + learned
                        + " skills=" + Arrays.toString(Arrays.copyOf(unit.skillIds, unit.skillCount)));
                expLearningSkill = false;
                expLearningConfirm = false;
                expSelectedLearnSkill = -1;
                expLearnSkillIds = new int[0];
                if (unit.canSourceLevelUp()) {
                    expLevelUpPending = true;
                    return true;
                }
                return finishCurrentExpPet(s, "P23 learn skill");
            }
            return true;
        }
        s.battleUiMode = "choiceskill";
        MenuAction action = handleSkillInput(s);
        if (action == MenuAction.BACK) {
            s.sourceStateTrace.add("APPROX battle P23 choiceskill back/skip; source aq() back path not proven");
            expLearningSkill = false;
            return finishCurrentExpPet(s, "P23 learn skill skipped");
        }
        if (action != MenuAction.CONFIRM) {
            syncRenderState(s, VqsvText.Battle.LEVEL_UP_LEARN_PENDING);
            return true;
        }
        if (s.battleSkillIds.length == 0) {
            expLearningSkill = false;
            return true;
        }
        int index = Math.max(0, Math.min(s.battleSkillIndex, s.battleSkillIds.length - 1));
        expSelectedLearnSkill = s.battleSkillIds[index];
        BattleSkillRow row = VqsvBattleTables.instance().skill(expSelectedLearnSkill);
        s.battleWarningTitle = VqsvText.Battle.LEARN_SKILL_PREFIX
                + (row == null ? "Skill " + expSelectedLearnSkill : row.name("Skill " + expSelectedLearnSkill));
        s.battleWarningPrompt = VqsvText.Battle.WARNING_PROMPT;
        s.battleUiMode = "warning";
        s.battleMsgWarm = VqsvMsgWarmView.of(s.battleWarningTitle, s.battleWarningPrompt);
        s.text = TextBox.msgWarm(s.battleWarningTitle, s.battleWarningPrompt);
        expLearningConfirm = true;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P23 game.h.aq confirm prompt skill="
                + expSelectedLearnSkill + " candidates=" + Arrays.toString(expLearnSkillIds));
        return true;
    }

    private void prepareLevelUpSkillMenu(VqsvIntroDemo.Scene s) {
        s.battleUiMode = "choiceskill";
        s.battleSkillIndex = 0;
        s.battleSkillScroll = 0;
        s.battleSkillIds = Arrays.copyOf(expLearnSkillIds, expLearnSkillIds.length);
        s.battleSkillNames = new String[s.battleSkillIds.length];
        s.battleSkillPpLabels = new String[s.battleSkillIds.length];
        for (int i = 0; i < s.battleSkillIds.length; i++) {
            BattleSkillRow row = VqsvBattleTables.instance().skill(s.battleSkillIds[i]);
            s.battleSkillNames[i] = row == null ? "Skill " + s.battleSkillIds[i]
                    : row.name("Skill " + s.battleSkillIds[i]);
            s.battleSkillPpLabels[i] = row == null ? "" : String.valueOf(row.ppMax);
        }
        updateSkillScrollAndDescription(s);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P23 game.h.ap choiceskill.ui candidates="
                + Arrays.toString(s.battleSkillIds));
    }

    private void prepareWinExp(VqsvIntroDemo.Scene s) {
        expPrepared = true;
        expEligible = enemy != null && enemy.hp <= 0 && player != null && player.battleUnit != null
                && !isBunnyCaptureBattle();
        if (!expEligible) {
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 EXP skipped eligible=false enemyHp="
                    + (enemy == null ? -1 : enemy.hp) + " bunny=" + isBunnyCaptureBattle());
            return;
        }
        if (!s.sourcePets.isEmpty() && player.battleUnit != null) {
            s.sourcePets.get(0).persistBattleUnit(player.battleUnit);
        }
        prepareSourceExpAwards(s);
        if (sourceExpDisplay.isEmpty()) {
            expEligible = false;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 EXP skipped game.d.j empty"
                    + " participants=" + sourceExpParticipants.size());
            return;
        }
        expDisplayIndex = 0;
        if (!selectCurrentExpDisplayPet(s)) {
            expEligible = false;
            return;
        }
        expHoldTicks = 0;
    }

    private void resetSourceExpVectors(VqsvIntroDemo.Scene s) {
        sourceExpParticipants.clear();
        sourceExpDisplay.clear();
        expDisplayIndex = 0;
        expCurrentPet = null;
        expCurrentUnit = null;
        for (SourcePetState pet : s.sourcePets) {
            pet.sourcePendingExp = 0;
            pet.sourceExpStart = 0;
            pet.sourceExpParticipant = false;
            pet.sourceExpDisplay = false;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL battle EXP reset source vectors game.d.x/game.d.j/game.b.B");
    }

    private void addSourceExpParticipant(VqsvIntroDemo.Scene s, SourcePetState pet, String reason) {
        if (pet == null || sourceExpParticipants.contains(pet)) {
            return;
        }
        sourceExpParticipants.add(pet);
        pet.sourceExpParticipant = true;
        s.sourceStateTrace.add("PORTED/PARTIAL battle EXP game.d.x add species="
                + pet.speciesId + " level=" + pet.level
                + " xSize=" + sourceExpParticipants.size()
                + " reason=" + reason);
    }

    private void pruneSourceExpVectors(VqsvIntroDemo.Scene s, String reason) {
        for (int i = 0; i < sourceExpParticipants.size(); i++) {
            SourcePetState pet = sourceExpParticipants.get(i);
            if (sourcePetAlive(pet)) {
                continue;
            }
            pet.sourceExpParticipant = false;
            pet.sourcePendingExp = 0;
            sourceExpParticipants.remove(i--);
            s.sourceStateTrace.add("PORTED/PARTIAL battle EXP game.d.x remove dead species="
                    + (pet == null ? -1 : pet.speciesId) + " reason=" + reason);
        }
        for (int i = 0; i < sourceExpDisplay.size(); i++) {
            SourcePetState pet = sourceExpDisplay.get(i);
            if (sourcePetAlive(pet)) {
                continue;
            }
            pet.sourceExpDisplay = false;
            pet.sourcePendingExp = 0;
            sourceExpDisplay.remove(i--);
            s.sourceStateTrace.add("PORTED/PARTIAL battle EXP game.d.j remove dead species="
                    + (pet == null ? -1 : pet.speciesId) + " reason=" + reason);
        }
    }

    private void prepareSourceExpAwards(VqsvIntroDemo.Scene s) {
        sourceExpDisplay.clear();
        for (SourcePetState pet : s.sourcePets) {
            pet.sourceExpDisplay = false;
        }
        pruneSourceExpVectors(s, "P8 prepare");
        if (sourceExpParticipants.isEmpty() && !s.sourcePets.isEmpty()) {
            addSourceExpParticipant(s, s.sourcePets.get(0), "P8 fallback active f[0]");
        }
        int participantCount = Math.max(1, Math.min(6, sourceExpParticipants.size()));
        BattleUnit lastDirectParticipant = null;
        for (SourcePetState pet : sourceExpParticipants) {
            BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
            if (!unit.alive()) {
                continue;
            }
            lastDirectParticipant = unit;
            pet.sourceExpStart = unit.exp;
            int award = sourceExpAward(enemy, unit, participantCount);
            if (unit.hasSourceFormStatus(5)) {
                int multiplier = sourceStatusParam(5, 5, 0);
                award = award * (multiplier + 100) / 100;
            }
            pet.sourcePendingExp += award;
            addSourceExpDisplayPet(pet);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 game.d.h direct EXP species="
                    + unit.speciesId + " level=" + unit.level
                    + " enemySpecies=" + enemy.speciesId
                    + " enemyLevel=" + enemy.level
                    + " award=" + award
                    + " B=" + pet.sourcePendingExp
                    + " expStart=" + pet.sourceExpStart
                    + " participants=" + participantCount
                    + " form5Multiplier=" + unit.hasSourceFormStatus(5));
        }
        int reserveLevelFactorLevel = lastDirectParticipant == null ? 1 : lastDirectParticipant.level;
        boolean globalShare = sourceGlobalState(s, 7, 0) == 2;
        for (SourcePetState pet : s.sourcePets) {
            if (pet == null || sourceExpParticipants.contains(pet)) {
                continue;
            }
            BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
            if (!unit.alive()) {
                continue;
            }
            int divisor;
            String reason;
            if (globalShare) {
                divisor = 3000;
                reason = "game.g.B[7][0]==2";
            } else if (unit.hasSourceFormStatus(6)) {
                divisor = 1000;
                reason = "reserve f(6)";
            } else {
                continue;
            }
            pet.sourceExpStart = unit.exp;
            int award = sourceExpAward(enemy, reserveLevelFactorLevel, participantCount, divisor);
            pet.sourcePendingExp += award;
            addSourceExpDisplayPet(pet);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 game.d.h reserve EXP species="
                    + unit.speciesId + " level=" + unit.level
                    + " enemySpecies=" + enemy.speciesId
                    + " enemyLevel=" + enemy.level
                    + " award=" + award
                    + " B=" + pet.sourcePendingExp
                    + " expStart=" + pet.sourceExpStart
                    + " participants=" + participantCount
                    + " divisor=" + divisor
                    + " levelFactorFromLastX=" + reserveLevelFactorLevel
                    + " reason=" + reason);
        }
        consumeSourceExpAwards(s);
    }

    private void consumeSourceExpAwards(VqsvIntroDemo.Scene s) {
        for (int i = 0; i < sourceExpDisplay.size(); i++) {
            SourcePetState pet = sourceExpDisplay.get(i);
            if (!sourcePetAlive(pet)) {
                pet.sourceExpDisplay = false;
                pet.sourcePendingExp = 0;
                sourceExpDisplay.remove(i--);
                s.sourceStateTrace.add("PORTED battle P8 game.d.X remove dead game.d.j species="
                        + (pet == null ? -1 : pet.speciesId));
                continue;
            }
            BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
            int pending = Math.max(0, pet.sourcePendingExp);
            unit.addSourceExp(pending);
            pet.sourcePendingExp = 0;
            pet.persistBattleUnit(unit);
            pet.sourceD(false);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 game.d.X commit B->S species="
                    + unit.speciesId + " exp=" + pet.sourceExpStart + "->" + unit.exp
                    + " pending=" + pending
                    + " jSize=" + sourceExpDisplay.size()
                    + " sourceD=false");
        }
        applySourcePostExpPassiveHeal(s);
    }

    void debugInjectSourceExpDisplayForSmoke(SourcePetState pet, int pending) {
        sourceExpDisplay.clear();
        if (pet == null) {
            return;
        }
        pet.sourcePendingExp = Math.max(0, pending);
        pet.sourceExpStart = sourcePetExp(pet);
        addSourceExpDisplayPet(pet);
    }

    void debugRunSourceExpConsumerForSmoke(VqsvIntroDemo.Scene s) {
        consumeSourceExpAwards(s);
    }

    private void addSourceExpDisplayPet(SourcePetState pet) {
        if (pet == null || sourceExpDisplay.contains(pet)) {
            return;
        }
        sourceExpDisplay.add(pet);
        pet.sourceExpDisplay = true;
    }

    private boolean selectCurrentExpDisplayPet(VqsvIntroDemo.Scene s) {
        while (expDisplayIndex < sourceExpDisplay.size()) {
            SourcePetState pet = sourceExpDisplay.get(expDisplayIndex);
            BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
            if (!unit.alive() || unit.level >= 50) {
                s.sourceStateTrace.add("PORTED/PARTIAL battle P8 game.h.a skip jIndex="
                        + expDisplayIndex + " species=" + unit.speciesId
                        + " alive=" + unit.alive() + " level=" + unit.level);
                expDisplayIndex++;
                continue;
            }
            expCurrentPet = pet;
            expCurrentUnit = unit;
            expAward = Math.max(0, unit.exp - Math.max(0, pet.sourceExpStart));
            expDisplayValue = Math.max(0, Math.min(pet.sourceExpStart, unit.nextLevelEnergy()));
            expOldStats = unit.sourceVisibleStats();
            expNewStats = unit.sourceVisibleStats();
            expLevelUpPending = false;
            expLevelUpApplied = false;
            expLearningSkill = false;
            expLearningConfirm = false;
            expLearnSkillIds = new int[0];
            expSelectedLearnSkill = -1;
            expHoldTicks = 0;
            syncExpPetAfterExp(s, unit);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 game.h.a select game.d.j index="
                    + expDisplayIndex + "/" + sourceExpDisplay.size()
                    + " species=" + unit.speciesId
                    + " level=" + unit.level
                    + " exp=" + expDisplayValue + "->" + unit.exp
                    + "/" + unit.nextLevelEnergy()
                    + " award=" + expAward);
            return true;
        }
        expCurrentPet = null;
        expCurrentUnit = null;
        return false;
    }

    private boolean finishCurrentExpPet(VqsvIntroDemo.Scene s, String reason) {
        if (expCurrentUnit != null) {
            syncExpPetAfterExp(s, expCurrentUnit);
        }
        s.sourceStateTrace.add("PORTED/PARTIAL battle P8 finish game.d.j index="
                + expDisplayIndex + "/" + sourceExpDisplay.size()
                + " reason=" + reason);
        expDisplayIndex++;
        expLevelUpPending = false;
        expLevelUpApplied = false;
        expLearningSkill = false;
        expLearningConfirm = false;
        expLearnSkillIds = new int[0];
        expSelectedLearnSkill = -1;
        if (selectCurrentExpDisplayPet(s)) {
            return true;
        }
        expEligible = false;
        return false;
    }

    private int sourceExpAward(SourceBattleUnit defeated, BattleUnit participant, int participantCount) {
        return sourceExpAward(defeated, Math.max(1, participant.level), participantCount, 1000);
    }

    private int sourceExpAward(SourceBattleUnit defeated, int levelFactorLevel,
                               int participantCount, int divisor) {
        int enemyLevel = Math.max(1, defeated.level);
        int quality = Math.max(1, Math.min(5, defeated.nature));
        int[] aG = {10, 11, 12, 13, 15};
        int[] aH = {10, 12, 13, 14, 15, 16};
        int[] aI = {105, 100, 80, 60, 40, 20, 5};
        int base = (((enemyLevel << 1) * enemyLevel + 50) * aG[quality - 1] / 10) + 400;
        int diff = Math.max(1, levelFactorLevel) - enemyLevel;
        int levelFactor;
        if (diff >= 6) {
            levelFactor = aI[6];
        } else if (diff > 0) {
            levelFactor = aI[diff];
        } else if (diff == 0) {
            levelFactor = aI[1];
        } else {
            levelFactor = aI[0];
        }
        int count = Math.max(1, Math.min(6, participantCount));
        return Math.max(0, base / count * aH[count - 1] * levelFactor / Math.max(1, divisor));
    }

    private int sourceStatusParam(int statusId, int index, int fallback) {
        BattleStatusRow row = VqsvBattleTables.instance().status(statusId);
        return row == null ? fallback : VqsvBattleTables.get(row.raw, index, fallback);
    }

    private int sourcePetExp(SourcePetState pet) {
        return pet != null && pet.sourcePayload != null && pet.sourcePayload.length > 7
                ? pet.sourcePayload[7]
                : 0;
    }

    private void applySourcePostExpPassiveHeal(VqsvIntroDemo.Scene s) {
        if (sourceGlobalState(s, 0, 0) != 2 || sourceGlobalState(s, 0, 1) != 1) {
            return;
        }
        for (SourcePetState pet : s.sourcePets) {
            if (!sourcePetAlive(pet)) {
                continue;
            }
            BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
            int before = unit.hp();
            int heal = sourcePostExpPassiveHealAmount(unit.speciesId);
            unit.heal(heal);
            pet.persistBattleUnit(unit);
            s.sourceStateTrace.add("PORTED/PARTIAL battle P8 game.d.X passive heal B[0][0/1]"
                    + " species=" + unit.speciesId
                    + " hp=" + before + "->" + unit.hp()
                    + " heal=" + heal
                    + " sourceQ=PENDING");
        }
    }

    private int sourcePostExpPassiveHealAmount(int speciesId) {
        BattleSpeciesRow species = VqsvBattleTables.instance().species(speciesId);
        short[] passiveRow = VqsvBattleTables.instance().row(2, 0);
        int baseHp = species == null ? 0 : VqsvBattleTables.get(species.raw, 5, 0);
        int percent = VqsvBattleTables.get(passiveRow, 6, 0);
        return Math.max(0, baseHp * percent / 100);
    }

    private int sourceGlobalState(VqsvIntroDemo.Scene s, int group, int slot) {
        if (s == null || group < 0 || group >= s.sourceGlobalState.length
                || slot < 0 || slot >= s.sourceGlobalState[group].length) {
            return 0;
        }
        return s.sourceGlobalState[group][slot];
    }

    private SourceEvolutionNotice produceSourceEvolutionQueue(VqsvIntroDemo.Scene s, BattleUnit unit) {
        SourceEvolutionNotice notice = sourceEvolutionNotice(s, unit);
        if (notice == null) {
            return null;
        }
        s.sourceEvolutionQueue.add(notice);
        s.sourceEvolutionL[0] = notice.currentLevel;
        s.sourceEvolutionL[1] = notice.currentSpeciesId;
        s.sourceEvolutionI = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P22 game.b.J evolution queue species="
                + notice.currentSpeciesId + " target=" + notice.targetSpeciesId
                + " sourceR=" + notice.sourceR
                + " level=" + notice.currentLevel + "/" + notice.requiredLevel
                + " material=" + notice.materialId + " count=" + notice.materialCount
                + "/" + notice.materialNeed
                + " materialBlocksConfirm=" + !notice.materialEnough
                + " game.k.H.size=" + s.sourceEvolutionQueue.size()
                + " game.k.L=[" + s.sourceEvolutionL[0] + "," + s.sourceEvolutionL[1] + "]"
                + " game.k.I=" + s.sourceEvolutionI
                + " ui/effect=PENDING");
        return notice;
    }

    private SourceEvolutionNotice sourceEvolutionNotice(VqsvIntroDemo.Scene s, BattleUnit unit) {
        BattleSpeciesRow current = VqsvBattleTables.instance().species(unit.speciesId);
        if (current == null || !current.validForBattle()) {
            return null;
        }
        int targetSpecies = VqsvBattleTables.get(current.raw, 19, -1);
        if (targetSpecies == -1) {
            return null;
        }
        BattleSpeciesRow target = VqsvBattleTables.instance().species(targetSpecies);
        if (target == null || !target.validForBattle()) {
            return null;
        }
        int targetKind = VqsvBattleTables.get(target.raw, 2, -1);
        int sourceR = sourceEvolutionKind(targetKind);
        if (sourceR <= 0) {
            return null;
        }
        int requiredLevel = sourceEvolutionRequiredLevel(targetKind);
        if (unit.level < requiredLevel) {
            return null;
        }
        int materialId = VqsvBattleTables.get(current.raw, 20, -13) + 12;
        int materialNeed = VqsvBattleTables.get(current.raw, 21, 0);
        int materialCount = sourceEvolutionMaterialCount(s, materialId);
        boolean materialEnough = materialNeed <= 0 || materialCount >= materialNeed;
        return new SourceEvolutionNotice(unit.speciesId,
                VqsvBattleTables.get(current.raw, 0, -1),
                unit.level,
                targetSpecies,
                VqsvBattleTables.get(target.raw, 0, -1),
                targetKind,
                requiredLevel,
                materialId,
                materialNeed,
                materialCount,
                sourceR,
                materialEnough);
    }

    private static int sourceEvolutionKind(int targetKind) {
        if (targetKind == 1 || targetKind == 2) {
            return 1;
        }
        if (targetKind == 3) {
            return 2;
        }
        return 0;
    }

    private static int sourceEvolutionRequiredLevel(int targetKind) {
        int[] sourceLevels = {12, 30, 5};
        int index = targetKind - 1;
        if (index < 0 || index >= sourceLevels.length) {
            return Integer.MAX_VALUE;
        }
        return sourceLevels[index];
    }

    private static int sourceEvolutionMaterialCount(VqsvIntroDemo.Scene s, int materialId) {
        if (materialId < 0) {
            return 0;
        }
        SourceSpecialReward reward = s.sourceSpecialRewards.get(materialId);
        return reward == null ? 0 : Math.max(0, reward.stackCount);
    }

    private VqsvBattleLevelUpView levelUpView(VqsvIntroDemo.Scene s, BattleUnit unit, boolean leveled) {
        int expMax = unit.nextLevelEnergy();
        int shown = Math.max(0, Math.min(expDisplayValue, expMax));
        SourceBattleUnit render = unit.toRenderUnit(true);
        return new VqsvBattleLevelUpView(true, leveled, render.name, render.visualId, render.element,
                unit.level, shown, expMax, shown * 100 / Math.max(1, expMax),
                expOldStats, expNewStats,
                leveled && expLearnSkillIds.length > 0 ? VqsvText.Battle.LEVEL_UP_LEARN_PENDING : "");
    }

    private void syncExpPetAfterExp(VqsvIntroDemo.Scene s, BattleUnit unit) {
        if (expCurrentPet != null) {
            expCurrentPet.persistBattleUnit(unit);
        }
        if (!s.sourcePets.isEmpty() && s.sourcePets.get(0) == expCurrentPet) {
            player = unit.toRenderUnit(true);
        }
        syncRenderState(s, battleWinLog());
    }

    private boolean tickLose(VqsvIntroDemo.Scene s) {
        if (isElderBattle()) {
            setHp(player, 1);
            setHp(enemy, 0);
            enterState(s, BattleRuntimeState.P8_WIN, VqsvText.Battle.ELDER_DONE, SHORT_WAIT);
            return false;
        }
        if (countdown()) {
            return false;
        }
        int result = Math.max(0, forcedResultIndex);
        s.battleResultIndex = result;
        s.battleBranchTarget = resolveBranch(s.battleResultIndex);
        persistActivePlayerPet(s, "P9 lose");
        syncRenderState(s, VqsvText.Battle.NEIL_LOST + s.battleResultIndex);
        s.sourceStateTrace.add("PORTED/PARTIAL battle P9 resultIndex="
                + s.battleResultIndex + " branch=" + s.battleBranchTarget
                + " playerHp=" + player.hp + "/" + player.maxHp
                + " enemyHp=" + enemy.hp + "/" + enemy.maxHp);
        enterState(s, BattleRuntimeState.EXIT_FADE, s.battleLog, EXIT_WAIT);
        return false;
    }

    private boolean tickExit(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (!exitFadeStarted) {
            s.battleCaptureTutorial = false;
            s.effect.startFade(1, 0);
            exitFadeStarted = true;
            return false;
        }
        if (!s.effect.doneOverlay(s)) {
            return false;
        }
        s.battleOverlayTicks = 0;
        s.battleBackgroundSnapshot = null;
        enterState(s, BattleRuntimeState.DONE, s.battleLog, 0);
        return true;
    }

    private void enterState(VqsvIntroDemo.Scene s, BattleRuntimeState next, String log, int waitTicks) {
        state = next;
        wait = waitTicks;
        s.battleStateName = next.label;
        if (next != BattleRuntimeState.P20_COMMAND) {
            commandConfirmQueued = false;
        }
        if (next == BattleRuntimeState.P20_COMMAND) {
            s.battleUiMode = "command";
        } else if (next == BattleRuntimeState.WARNING) {
            s.battleUiMode = "warning";
        } else if (next == BattleRuntimeState.P17_CATCH_RESULT) {
            s.battleUiMode = "catch_anim";
        } else if (next == BattleRuntimeState.P6_TARGET_SELECT) {
            s.battleUiMode = "target";
        } else if (next != BattleRuntimeState.P3_SKILL_LIST
                && next != BattleRuntimeState.P6_TARGET_SELECT
                && next != BattleRuntimeState.P21_CATCH_LIST
                && next != BattleRuntimeState.P4_ITEM_LIST
                && next != BattleRuntimeState.P16_ITEM_TARGET
                && next != BattleRuntimeState.P5_PET_SWITCH
                && next != BattleRuntimeState.P11_SHOP) {
            s.battleUiMode = "battle";
            s.battleCatchVisible = false;
            s.battleCatchEffectVisible = false;
            s.battleEnemyHiddenByCatch = false;
        }
        if (next != BattleRuntimeState.P7_RESOLVE) {
            p7Prepared = false;
            clearP7RenderState(s);
        }
        syncRenderState(s, log);
    }

    private void enterCommandState(VqsvIntroDemo.Scene s, String log, int waitTicks) {
        s.battleUiMode = "command";
        enterState(s, BattleRuntimeState.P20_COMMAND, log, waitTicks);
    }

    private void enterWarning(VqsvIntroDemo.Scene s, String message, BattleRuntimeState returnState) {
        warningReturnState = returnState;
        warningReturnLog = s.battleLog;
        s.battleUiMode = "warning";
        s.battleWarningTitle = message;
        s.battleWarningPrompt = VqsvText.Battle.WARNING_PROMPT;
        s.battleMsgWarm = VqsvMsgWarmView.of(message, s.battleWarningPrompt);
        s.text = TextBox.msgWarm(message, VqsvText.Battle.WARNING_PROMPT);
        s.sourceStateTrace.add("PORTED battle warning uses game.h.E()/a(text,prompt) /data/ui/msgwarm.ui"
                + " return=" + returnState.label);
        enterState(s, BattleRuntimeState.WARNING, message, SHORT_WAIT);
    }

    private boolean countdown() {
        if (wait > 0) {
            wait--;
            return true;
        }
        return false;
    }

    private String battleWinLog() {
        if (isBunnyCaptureBattle()) {
            return VqsvText.Battle.BUNNY_CAUGHT + forcedResultIndex;
        }
        if (catchWinLog != null) {
            return catchWinLog;
        }
        if (isElderBattle()) {
            return VqsvText.Battle.ELDER_DONE;
        }
        return VqsvText.Battle.NEIL_LOST + 0;
    }

    private void setBunnyTutorialState(VqsvIntroDemo.Scene s, int u, int v, String reason) {
        bunnyTutorialU = u;
        bunnyTutorialV = v;
        s.battleTutorialU = u;
        s.battleTutorialV = v;
        if (isBunnyCaptureBattle()) {
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial state U=" + u
                    + " V=" + v + " reason=" + reason);
        }
    }

    private void syncRenderState(VqsvIntroDemo.Scene s, String log) {
        if (!(p7Phase == 2 && p7DamageApplied)) {
            playerDisplayHp = player.hp;
            enemyDisplayHp = enemy.hp;
        }
        s.battleEnemyName = enemy.name;
        s.battleEnemyLevel = enemy.level;
        s.battleEnemyVisualId = enemy.visualId;
        s.battleEnemyElement = enemy.element;
        s.battleEnemyOwnedSpecies = sourceOwnsSpecies(s, enemy.speciesId);
        s.battleEnemyMaxHp = enemy.maxHp;
        s.battleEnemyHp = Math.max(0, Math.min(enemy.maxHp, enemyDisplayHp));
        s.battlePlayerName = player.name;
        s.battlePlayerLevel = player.level;
        s.battlePlayerVisualId = player.visualId;
        s.battlePlayerElement = player.element;
        s.battlePlayerMaxHp = player.maxHp;
        s.battlePlayerHp = Math.max(0, Math.min(player.maxHp, playerDisplayHp));
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
        s.battlePlayerStatusCount = syncStatusSlots(player.battleUnit,
                s.battlePlayerStatusIconCells, s.battlePlayerStatusDurationCells);
        s.battleEnemyStatusCount = syncStatusSlots(enemy.battleUnit,
                s.battleEnemyStatusIconCells, s.battleEnemyStatusDurationCells);
        syncBattleMarkerState(s);
    }

    private static int syncStatusSlots(BattleUnit unit, int[] iconCells, int[] durationCells) {
        Arrays.fill(iconCells, 0);
        Arrays.fill(durationCells, 145);
        if (unit == null) {
            return 0;
        }
        int visible = 0;
        for (int slot = 0; slot < 3 && visible < 6; slot++) {
            int buffId = statusQueueId(unit, 0, slot);
            if (buffId >= 0 && buffId < unit.buffSlots.length && unit.buffSlots[buffId][0] > 0) {
                iconCells[visible] = buffId + 12;
                durationCells[visible] = 134 + unit.buffSlots[buffId][0];
                visible++;
            }
            int debuffId = statusQueueId(unit, 1, slot);
            if (visible < 6 && debuffId >= 0 && debuffId < unit.debuffSlots.length
                    && unit.debuffSlots[debuffId][0] > 0) {
                iconCells[visible] = debuffId + 1;
                durationCells[visible] = 134 + unit.debuffSlots[debuffId][0];
                visible++;
            }
        }
        return visible;
    }

    private static int statusQueueId(BattleUnit unit, int bank, int slot) {
        if (bank < 0 || bank >= unit.activeEffectQueue.length
                || slot < 0 || slot >= unit.activeEffectQueue[bank].length) {
            return -1;
        }
        return unit.activeEffectQueue[bank][slot];
    }

    private boolean sourceOwnsSpecies(VqsvIntroDemo.Scene s, int speciesId) {
        for (SourcePetState pet : s.sourcePets) {
            if (pet.speciesId == speciesId) {
                return true;
            }
        }
        for (SourcePetState pet : s.sourcePetBank) {
            if (pet.speciesId == speciesId) {
                return true;
            }
        }
        return false;
    }

    private void syncBattleMarkerState(VqsvIntroDemo.Scene s) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(sourceCposGroup());
        int enemyAt = sourcePosQuadOffset(false);
        int playerAt = sourcePosQuadOffset(true);
        if (row.length >= enemyAt + 4 && row.length >= playerAt + 4) {
            s.battleEnemyMarkerX = row[enemyAt + 2];
            s.battleEnemyMarkerY = row[enemyAt + 3];
            s.battlePlayerMarkerX = row[playerAt + 2];
            s.battlePlayerMarkerY = row[playerAt + 3];
        } else {
            short[] enemyCpos = VqsvBattleAnimationTables.instance().cposRow(sourceCposGroup(), 0);
            short[] playerCpos = VqsvBattleAnimationTables.instance().cposRow(sourceCposGroup(),
                    sourceCposGroup() == 1 ? 2 : 1);
            if (enemyCpos.length >= 4) {
                int at = enemyCpos.length - 2;
                s.battleEnemyMarkerX = enemyCpos[at];
                s.battleEnemyMarkerY = enemyCpos[at + 1];
            }
            if (playerCpos.length >= 4) {
                int at = playerCpos.length - 2;
                s.battlePlayerMarkerX = playerCpos[at];
                s.battlePlayerMarkerY = playerCpos[at + 1];
            }
        }
        s.battleGroundMarkersVisible = state != BattleRuntimeState.EXIT_FADE
                && state != BattleRuntimeState.DONE;
        s.battleActiveMarkerVisible = state != BattleRuntimeState.P0_ENTRY
                && state != BattleRuntimeState.P1_DISPATCH
                && state != BattleRuntimeState.P8_WIN
                && state != BattleRuntimeState.P9_LOSE
                && state != BattleRuntimeState.EXIT_FADE
                && state != BattleRuntimeState.DONE;
        s.battleActiveMarkerPlayerSide = currentActorPlayer
                || state == BattleRuntimeState.P20_COMMAND
                || state == BattleRuntimeState.P3_SKILL_LIST
                || state == BattleRuntimeState.P6_TARGET_SELECT
                || state == BattleRuntimeState.P21_CATCH_LIST
                || state == BattleRuntimeState.P4_ITEM_LIST
                || state == BattleRuntimeState.P16_ITEM_TARGET
                || state == BattleRuntimeState.P5_PET_SWITCH;
    }

    private int sourcePosQuadOffset(boolean playerSide) {
        if (sourceCposGroup() == 1) {
            return playerSide ? 8 : 0;
        }
        return playerSide ? 4 : 0;
    }

    private void setHp(SourceBattleUnit unit, int hp) {
        if (unit.battleUnit != null) {
            unit.battleUnit.setHp(hp);
            unit.hp = unit.battleUnit.hp();
        } else {
            unit.hp = Math.max(0, Math.min(unit.maxHp, hp));
        }
    }

    private void persistActivePlayerPet(VqsvIntroDemo.Scene s, String reason) {
        if (playerPetPersistedOnExit || s.sourcePets.isEmpty() || player == null || player.battleUnit == null) {
            return;
        }
        s.sourcePets.get(0).persistBattleUnit(player.battleUnit);
        playerPetPersistedOnExit = true;
        int[] payload = s.sourcePets.get(0).sourcePayload;
        int hp = payload != null && payload.length > 6 ? payload[6] : -1;
        int skillCount = payload != null && payload.length > 9 ? payload[9] : 0;
        s.sourceStateTrace.add("PORTED/PARTIAL battle pet persistence game.d " + reason
                + " -> source party slot0 P() species=" + s.sourcePets.get(0).speciesId
                + " hpPayload6=" + hp
                + " skillCount=" + skillCount
                + " pending full game.d.x vector damage-delta parity");
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
