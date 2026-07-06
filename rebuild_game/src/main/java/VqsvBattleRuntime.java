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

final class SourceBattleRuntime implements Blocking {
    private static final int SHORT_WAIT = 6;
    private static final int RESOLVE_WAIT = 10;
    private static final int EXIT_WAIT = 12;
    private static final int P7_START_TICKS = 8;
    private static final int P7_DAMAGE_TICKS = 12;
    private static final int P7_EXIT_TICKS = 6;

    private final int actorId;
    private final int[] encounter;
    private final int[] flags;
    private final int[] battleMode;
    private final int[] branchTargets;
    private final int forcedResultIndex;
    private final boolean sourceBattleSlice;

    private BattleRuntimeState state = BattleRuntimeState.P0_ENTRY;
    private int wait;
    private SourceBattleUnit enemy;
    private SourceBattleUnit player;
    private int turn;
    private boolean entered;
    private boolean currentActorPlayer;
    private boolean playerActionThisRound;
    private boolean enemyActionThisRound;
    private boolean bunnyTutorialShown;
    private boolean bunnyCaptureQueued;
    private boolean exitFadeStarted;
    private boolean wasLeftPressed;
    private boolean wasRightPressed;
    private boolean wasUpPressed;
    private boolean wasDownPressed;
    private boolean commandConfirmQueued;
    private BattleRuntimeState warningReturnState = BattleRuntimeState.P20_COMMAND;
    private String warningReturnLog = VqsvText.Battle.START;
    private int selectedItemId = -1;
    private int selectedPetIndex = -1;
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
    private boolean p7Prepared;
    private boolean p7DamageApplied;
    private byte[] p7EffectRow = new byte[0];
    private short[] p7SpeffectRow = new short[0];
    private int p7EffectChunk;
    private int p7SpecialType = -1;
    private int catchPhase = -1;
    private int catchPhaseTicks = 0;
    private int catchChance = 0;
    private boolean catchCaught;
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
        s.battleCommandIndex = 0;
        syncRenderState(s, VqsvText.Battle.START);
        s.sourceStateTrace.add("PORTED/PARTIAL battle state machine actor=" + actorId
                + " encounter=" + Arrays.toString(encounter)
                + " flags=" + Arrays.toString(flags)
                + " mode=" + Arrays.toString(battleMode)
                + " enemy=" + enemy
                + " player=" + player
                + " branchTargets=" + Arrays.toString(branchTargets)
                + " sourceSlice=" + sourceBattleSlice
                + " states=P0/P20/P3/P6/P2/P7/P1/P8/P9; command UI/catch/items/animation still pending; "
                + VqsvBattleTables.sourceSummary());
        entered = true;
        enterState(s, BattleRuntimeState.P0_ENTRY, VqsvText.Battle.START, SHORT_WAIT);
        s.effect.startFade(2, 0);
    }

    private boolean tickEntry(VqsvIntroDemo.Scene s) {
        if (!s.effect.doneOverlay(s) || countdown()) {
            return false;
        }
        enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.START, SHORT_WAIT);
        return false;
    }

    private boolean tickDispatch(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        if (!player.alive()) {
            enterState(s, BattleRuntimeState.P9_LOSE, VqsvText.Battle.NEIL_LOST + forcedResultIndex, SHORT_WAIT);
            return false;
        }
        if (!enemy.alive()) {
            enterState(s, BattleRuntimeState.P8_WIN, battleWinLog(), SHORT_WAIT);
            return false;
        }
        if (isKidnappingBattle()) {
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, enemy.name, SHORT_WAIT);
            return false;
        }
        if (!enemyActionThisRound && !playerActionThisRound && enemy.speed > player.speed) {
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, enemy.name, SHORT_WAIT);
            return false;
        }
        if (!playerActionThisRound) {
            currentActorPlayer = true;
            enterCommandState(s, VqsvText.Battle.START, SHORT_WAIT);
            return false;
        }
        if (!enemyActionThisRound) {
            currentActorPlayer = false;
            enterState(s, BattleRuntimeState.P2_SELECT_EXECUTE, enemy.name, SHORT_WAIT);
            return false;
        }
        playerActionThisRound = false;
        enemyActionThisRound = false;
        turn++;
        enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.START, SHORT_WAIT);
        return false;
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
        int clicked = menuIndexAt(s.battleClickX, s.battleClickY);
        boolean clickedBack = s.battleClickX >= 144 && s.battleClickX <= 198
                && s.battleClickY >= 232 && s.battleClickY <= 255;
        s.battleClickX = -1;
        s.battleClickY = -1;
        if (clickedBack) {
            return MenuAction.BACK;
        }
        if (clicked >= 0 && clicked < s.battleMenuNames.length) {
            s.battleMenuIndex = clicked;
            return MenuAction.CONFIRM;
        }

        boolean upEdge = s.keyUp && !wasUpPressed;
        boolean downEdge = s.keyDown && !wasDownPressed;
        wasUpPressed = s.keyUp;
        wasDownPressed = s.keyDown;
        if (upEdge && s.battleMenuNames.length > 0) {
            s.battleMenuIndex = (s.battleMenuIndex + s.battleMenuNames.length - 1) % s.battleMenuNames.length;
            return MenuAction.NONE;
        }
        if (downEdge && s.battleMenuNames.length > 0) {
            s.battleMenuIndex = (s.battleMenuIndex + 1) % s.battleMenuNames.length;
            return MenuAction.NONE;
        }
        return s.key0 ? MenuAction.CONFIRM : MenuAction.NONE;
    }

    private int menuIndexAt(int x, int y) {
        if (x < 44 || x > 195 || y < 92 || y > 226) {
            return -1;
        }
        int index = (y - 92) / 26;
        return index >= 0 && index < 5 ? index : -1;
    }

    private MenuAction handleSkillInput(VqsvIntroDemo.Scene s) {
        int clicked = skillIndexAt(s.battleClickX, s.battleClickY, s);
        boolean clickedBack = s.battleClickX >= 152 && s.battleClickX <= 198
                && s.battleClickY >= 232 && s.battleClickY <= 255;
        s.battleClickX = -1;
        s.battleClickY = -1;
        if (clickedBack) {
            return MenuAction.BACK;
        }
        if (clicked >= 0) {
            s.battleSkillIndex = clicked;
            updateSkillScrollAndDescription(s);
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
        return s.key0 ? MenuAction.CONFIRM : MenuAction.NONE;
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
            if (item.count > 0 && row != null && row.behavior == 0) {
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
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> icons = new java.util.ArrayList<>();
        for (BagItem item : s.sourceBagItems.values()) {
            BattleItemRow row = VqsvBattleTables.instance().item(item.id);
            if (item.count > 0 && row != null && row.behavior != 0) {
                ids.add(item.id);
                icons.add(row.iconId);
                names.add(itemName(item.id));
                values.add(String.valueOf(item.count));
            }
        }
        setMenu(s, "\u0110\u1ea1o c\u1ee5", "S\u1ed1 l\u01b0\u1ee3ng", "S\u1eed d\u1ee5ng", names, values, ids, icons);
    }

    private void prepareItemTargetMenu(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        if (s.sourcePets.isEmpty()) {
            ids.add(0);
            names.add(player.name);
            values.add(player.hp + "/" + player.maxHp);
        } else {
            for (int i = 0; i < s.sourcePets.size(); i++) {
                SourceBattleUnit unit = SourceBattleUnit.playerFromSourcePets(s.sourcePets.subList(i, i + 1));
                ids.add(i);
                names.add(unit.name);
                values.add(unit.hp + "/" + unit.maxHp);
            }
        }
        setMenu(s, itemName(selectedItemId), "M\u1ee5c ti\u00eau", "S\u1eed d\u1ee5ng", names, values, ids);
    }

    private void preparePetMenu(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
        for (int i = 1; i < s.sourcePets.size(); i++) {
            SourceBattleUnit unit = SourceBattleUnit.playerFromSourcePets(s.sourcePets.subList(i, i + 1));
            ids.add(i);
            names.add(unit.name);
            values.add("lv" + unit.level);
        }
        setMenu(s, "S\u1ee7ng v\u1eadt", "Thay \u0111\u1ed5i", "S\u1eed d\u1ee5ng", names, values, ids);
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
        s.battleUiMode = "choice";
        s.battleMenuTitle = title;
        s.battleMenuSubtitle = subtitle;
        s.battleMenuAction = action;
        s.battleMenuNames = names.toArray(new String[0]);
        s.battleMenuValues = values.toArray(new String[0]);
        s.battleMenuIds = new int[ids.size()];
        s.battleMenuIconIds = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            s.battleMenuIds[i] = ids.get(i);
            s.battleMenuIconIds[i] = i < icons.size() ? icons.get(i) : -1;
        }
        if (s.battleMenuIndex < 0 || s.battleMenuIndex >= s.battleMenuNames.length) {
            s.battleMenuIndex = 0;
        }
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
            return clickedBack ? MenuAction.BACK : MenuAction.NONE;
        }
        if (clicked >= 0) {
            selectedTargetIndex = clicked;
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
        return s.key0 ? MenuAction.CONFIRM : MenuAction.NONE;
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
            enterWarning(s, VqsvText.Battle.NO_BALLS, BattleRuntimeState.P21_CATCH_LIST);
            return false;
        }
        VqsvSourceOps.sourceRemoveItem(s, itemId, 1);
        selectedItemId = itemId;
        initCatchResult(s, itemId);
        enterState(s, BattleRuntimeState.P17_CATCH_RESULT, VqsvText.Battle.BALL_CHOSEN, 0);
        return false;
    }

    private boolean tickCatchResult(VqsvIntroDemo.Scene s) {
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
            setHp(enemy, 0);
            enterState(s, BattleRuntimeState.P8_WIN, applyCatchStorage(s), SHORT_WAIT);
        } else if (catchPhase == 4 && animEnded && tickCatchEffectSourceLike()) {
            clearCatchVisuals(s);
            playerActionThisRound = true;
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.CATCH_FAILED, SHORT_WAIT);
        }
        tickCatchObjects();
        syncCatchRenderState(s, catchPhase);
        return false;
    }

    private void initCatchResult(VqsvIntroDemo.Scene s, int itemId) {
        catchPhase = 0;
        catchPhaseTicks = 0;
        catchChance = catchChance(itemId);
        catchCaught = itemId == 0 || isBunnyCaptureBattle() || catchChance >= 50;
        catchTraceWritten = false;
        catchWinLog = null;
        catchAnim = SpriteAnim.load(269);
        setCatchAnimState(0, false);
        clearCatchEffect();
        s.battleUiMode = "catch_anim";
        s.battleCatchSpriteId = 269;
        s.battleCatchItemId = itemId;
        s.battleCatchChance = catchChance;
        s.battleCatchCaught = catchCaught;
        s.battleCatchVisible = true;
        syncCatchRenderState(s, catchPhase);
        s.sourceStateTrace.add("PORTED battle P21 confirm item=" + itemId
                + " consumed=1 next=P17 sprite=269 chance=" + catchChance);
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
        s.battleCatchCaught = catchCaught;
        s.battleEnemyHiddenByCatch = phase >= 1;
        syncCatchEffectRender(s);
        if (!catchTraceWritten && phase == 0) {
            catchTraceWritten = true;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P17 source-timed q=0..4 item=" + selectedItemId
                    + " chance=" + catchChance
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

    private String applyCatchStorage(VqsvIntroDemo.Scene s) {
        SourcePetState caught = SourcePetState.caughtFromBattleUnit(Math.min(s.sourcePets.size(), 5), enemy);
        if (s.sourcePets.size() < 6) {
            s.sourcePets.add(caught);
            s.sourceStateTrace.add("PORTED battle P17 storage game.g.y=0 add bag species="
                    + caught.speciesId + " bagSize=" + s.sourcePets.size()
                    + " payloadLen=" + (caught.sourcePayload == null ? 0 : caught.sourcePayload.length));
            catchWinLog = VqsvText.Battle.CATCH_SUCCESS + enemy.name;
            return catchWinLog;
        }
        if (s.sourcePetBank.size() < 100) {
            caught.slot = s.sourcePetBank.size();
            s.sourcePetBank.add(caught);
            s.sourceStateTrace.add("PORTED battle P17 storage game.g.y=1 add bank species="
                    + caught.speciesId + " bankSize=" + s.sourcePetBank.size()
                    + " payloadLen=" + (caught.sourcePayload == null ? 0 : caught.sourcePayload.length));
            catchWinLog = VqsvText.Battle.CATCH_SENT_BANK;
            return catchWinLog;
        }
        s.sourceStateTrace.add("PORTED battle P17 storage game.g.y=2 full release species="
                + caught.speciesId);
        catchWinLog = VqsvText.Battle.CATCH_RELEASED_FULL;
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
            enterWarning(s, VqsvText.Battle.NO_PET_TARGET, BattleRuntimeState.P4_ITEM_LIST);
            return false;
        }
        BagItem item = s.sourceBagItems.get(selectedItemId);
        if (item == null || item.count <= 0) {
            enterWarning(s, VqsvText.Battle.NO_ITEM_COUNT, BattleRuntimeState.P4_ITEM_LIST);
            return false;
        }
        BattleItemRow row = VqsvBattleTables.instance().item(selectedItemId);
        int behavior = row == null ? -1 : row.behavior;
        if (behavior == 1 || behavior == 3 || behavior == 4) {
            int before = player.hp;
            int heal = Math.max(1, player.maxHp * Math.max(1, row.paramA) / 100);
            setHp(player, player.hp + heal);
            VqsvSourceOps.sourceRemoveItem(s, selectedItemId, 1);
            playerActionThisRound = true;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P16 item=" + selectedItemId
                    + " behavior=" + behavior + " heal=" + before + "->" + player.hp);
            syncRenderState(s, VqsvText.Battle.ITEM_USED);
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.ITEM_USED, SHORT_WAIT);
        } else if (behavior == 2) {
            VqsvSourceOps.sourceRemoveItem(s, selectedItemId, 1);
            playerActionThisRound = true;
            s.sourceStateTrace.add("PORTED/PARTIAL battle P16 item=" + selectedItemId
                    + " behavior=2 PP restore approximated");
            syncRenderState(s, VqsvText.Battle.ITEM_USED);
            enterState(s, BattleRuntimeState.P1_DISPATCH, VqsvText.Battle.ITEM_USED, SHORT_WAIT);
        } else {
            enterWarning(s, VqsvText.Battle.ITEM_NOT_IN_BATTLE, BattleRuntimeState.P4_ITEM_LIST);
        }
        return false;
    }

    private boolean tickPetSwitch(VqsvIntroDemo.Scene s) {
        if (countdown()) {
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
        if (selectedPetIndex <= 0 || selectedPetIndex >= s.sourcePets.size()) {
            enterWarning(s, VqsvText.Battle.NO_SWITCH_PET, BattleRuntimeState.P5_PET_SWITCH);
            return false;
        }
        SourcePetState next = s.sourcePets.remove(selectedPetIndex);
        s.sourcePets.add(0, next);
        player = SourceBattleUnit.playerFromSourcePets(s.sourcePets);
        playerActionThisRound = true;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P5 pet switch selectedIndex="
                + selectedPetIndex + " newPlayer=" + player.name);
        syncRenderState(s, VqsvText.Battle.PET_SWITCHED + player.name);
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
        } else if (warningReturnState == BattleRuntimeState.P5_PET_SWITCH) {
            preparePetMenu(s);
            enterState(s, BattleRuntimeState.P5_PET_SWITCH, VqsvText.Battle.COMMAND_PET_PENDING, SHORT_WAIT);
        } else if (warningReturnState == BattleRuntimeState.P11_SHOP) {
            prepareShopMenu(s);
            enterState(s, BattleRuntimeState.P11_SHOP, VqsvText.Battle.COMMAND_SHOP_PENDING, SHORT_WAIT);
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
            syncP7RenderState(s, p7SkillName());
            if (p7Ticks < p7CurrentEffectDuration()) {
                return false;
            }
            if (advanceP7EffectChunk(s)) {
                return false;
            }
            if (p7NoDamageSkill()) {
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
            syncP7RenderState(s, p7Attacker.name + VqsvText.Battle.DAMAGE
                    + p7Damage + VqsvText.Battle.DAMAGE_SUFFIX);
            return false;
        }
        if (p7Phase == 2) {
            p7Ticks++;
            syncP7RenderState(s, s.battleLog);
            if (p7Ticks < P7_DAMAGE_TICKS) {
                return false;
            }
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
        p7EffectChunk = 0;
        prepareP7SpecialEffect(s);
        p7Phase = 0;
        p7Ticks = 0;
        p7Damage = 0;
        p7DamageApplied = false;
        p7Prepared = true;
        clearP7RenderState(s);
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
        boolean ported = p7SpecialType == 9 || p7SpecialType == 1;
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
        }
        return P7_START_TICKS;
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

    private boolean advanceP7EffectChunk(VqsvIntroDemo.Scene s) {
        int chunkCount = p7EffectRow.length / 7;
        if (p7EffectChunk + 1 >= chunkCount) {
            return false;
        }
        p7EffectChunk++;
        p7Ticks = 0;
        prepareP7SpecialEffect(s);
        syncP7RenderState(s, p7SkillName());
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
        int index = p7EffectChunk * 7 + offset;
        return index >= 0 && index < p7EffectRow.length ? p7EffectRow[index] : -1;
    }

    private void applyP7Damage(VqsvIntroDemo.Scene s) {
        if (p7DamageApplied) {
            return;
        }
        p7Damage = Math.max(1, p7Attacker.basicDamageTo(p7Target));
        p7Target.damage(p7Damage);
        markP7ActionUsed();
        p7DamageApplied = true;
        s.sourceStateTrace.add("PORTED/PARTIAL battle P7 damage frame skill=" + p7SkillId
                + " damage=" + p7Damage
                + " target=" + p7Target.name
                + " hp=" + p7Target.hp + "/" + p7Target.maxHp
                + " bloodRow0Len=" + VqsvBattleAnimationTables.instance().bloodRow(0).length);
    }

    private boolean finishP7(VqsvIntroDemo.Scene s) {
        clearP7RenderState(s);
        if (isBunnyCaptureBattle() && currentActorPlayer && !bunnyTutorialShown && enemy.hp <= enemy.maxHp / 2) {
            bunnyTutorialShown = true;
            s.battleCommandIndex = 1;
            syncRenderState(s, VqsvText.Battle.BUNNY_WEAK);
            s.sourceStateTrace.add("PORTED/PARTIAL bunny tutorial game.d.l(): HP<=50%, next P20 selects catch and opens P21; P17 animation still partial");
            enterCommandState(s, VqsvText.Battle.BUNNY_WEAK, RESOLVE_WAIT);
            return false;
        }
        if (!p7Target.alive()) {
            enterState(s, currentActorPlayer ? BattleRuntimeState.P8_WIN : BattleRuntimeState.P9_LOSE,
                    currentActorPlayer ? battleWinLog() : VqsvText.Battle.NEIL_LOST + forcedResultIndex,
                    SHORT_WAIT);
            return false;
        }
        enterState(s, BattleRuntimeState.P1_DISPATCH, s.battleLog, SHORT_WAIT);
        return false;
    }

    private void syncP7RenderState(VqsvIntroDemo.Scene s, String log) {
        syncRenderState(s, log);
        s.battleP7Phase = p7Phase;
        s.battleP7Ticks = p7Ticks;
        s.battleP7AttackerPlayerSide = p7Attacker == player;
        s.battleP7TargetPlayerSide = p7Target == player;
        boolean effectOnTarget = p7EffectValue(0) == 0;
        s.battleP7EffectOnPlayerSide = effectOnTarget ? s.battleP7TargetPlayerSide : s.battleP7AttackerPlayerSide;
        s.battleP7EffectAnimState = p7EffectValue(1) == 0 ? p7EffectValue(2) : 0;
        s.battleP7EffectAnimCursor = Math.max(0, p7Ticks / 2);
        s.battleP7DamageVisible = p7Phase == 2 && p7DamageApplied;
        s.battleP7DamageText = s.battleP7DamageVisible ? "-" + p7Damage : "";
        boolean showSpecial = p7Phase == 1
                && (p7SpecialType == 9 || p7SpecialType == 1)
                && p7Ticks <= p7CurrentEffectDuration();
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
    }

    private void clearP7RenderState(VqsvIntroDemo.Scene s) {
        s.battleP7Phase = 0;
        s.battleP7Ticks = 0;
        s.battleP7EffectAnimState = -1;
        s.battleP7EffectAnimCursor = 0;
        s.battleP7DamageVisible = false;
        s.battleP7DamageText = "";
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
    }

    private boolean tickWin(VqsvIntroDemo.Scene s) {
        if (countdown()) {
            return false;
        }
        int result = isBunnyCaptureBattle() ? forcedResultIndex : 0;
        s.battleResultIndex = result;
        s.battleBranchTarget = resolveBranch(s.battleResultIndex);
        syncRenderState(s, battleWinLog());
        s.sourceStateTrace.add("PORTED/PARTIAL battle P8 resultIndex="
                + s.battleResultIndex + " branch=" + s.battleBranchTarget
                + " playerHp=" + player.hp + "/" + player.maxHp
                + " enemyHp=" + enemy.hp + "/" + enemy.maxHp);
        enterState(s, BattleRuntimeState.EXIT_FADE, s.battleLog, EXIT_WAIT);
        return false;
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
            s.battleUiMode = "command";
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

    private void syncRenderState(VqsvIntroDemo.Scene s, String log) {
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

    private void setHp(SourceBattleUnit unit, int hp) {
        if (unit.battleUnit != null) {
            unit.battleUnit.setHp(hp);
            unit.hp = unit.battleUnit.hp();
        } else {
            unit.hp = Math.max(0, Math.min(unit.maxHp, hp));
        }
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
