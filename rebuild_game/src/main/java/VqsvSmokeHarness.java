import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.input.InputSnapshot;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.state.BootFlowState;
import com.vqsv.rebuild.state.GameStateMachine;
import com.vqsv.rebuild.state.LegacyIntroDemoState;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;

final class VqsvSmokeHarness {
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;
    private static final String[] BATTLE_QUICK_SUITE = {
            "route_sophie_after_battle_branch",
            "route_bunny_after_battle_task",
            "route_elder_after_battle_reward_state",
            "battle_elder_command_ui",
            "battle_elder_p3_skill_list",
            "battle_elder_p6_target_select",
            "battle_elder_p7_damage_frame",
            "battle_p7_hit_forced_direct_skill10",
            "battle_p7_miss_forced_skill10",
            "battle_p7_crit_forced_skill10",
            "battle_exp_normal_gain_no_levelup_anim",
            "battle_choice_ui_p4_wheel_hover_click_viewport",
            "battle_choice_ui_p21_synthetic_wheel_hover_click_viewport",
            "battle_choice_ui_p16_audit_not_choice_ui",
            "battle_choiceskill_mouse_wheel_scrollbar_no_confirm",
            "battle_choiceskill_mouse_wheel_hover_click_viewport",
            "battle_phase10a_status_icons_mixed_order",
            "battle_phase10b_p7_type7_skill34_overlay",
            "battle_phase10b_p7_type8_skill12_overlay",
            "battle_phase10b_p7_type12_skill55_overlay"
    };
    private static final String[] PANEL_WHEEL_SUITE = {
            "panel_bag_mouse_wheel_scrollbar_no_confirm",
            "panel_bag_mouse_wheel_hover_click_viewport",
            "panel_task_mouse_wheel_non_scrollable_no_confirm",
            "panel_petstate_petsetting_skill_mouse_wheel_non_scrollable_no_confirm",
            "panel_petmap_mouse_wheel_scrollbar_no_confirm",
            "panel_petmap_mouse_wheel_hover_click_viewport",
            "world_petstate_mouse_wheel_scrollbar_no_confirm",
            "world_petstate_mouse_wheel_hover_click_viewport"
    };

    private VqsvSmokeHarness() {
    }

    private static void seedInitialDienMieu(VqsvIntroDemo.Scene s, String reason) {
        VqsvSourceStoryState.ensureInitialDienMieu(s, reason);
    }

    private static void setupBunnyMapBackedBattleEntry(VqsvIntroDemo.Scene s) {
        s.loadScene1Room1(370, 176);
        s.eventIndex = s.events.size();
        s.setPlayerPositionApprox(374, 180);
    }

    private static void assertActiveSourcePet(VqsvIntroDemo.Scene s, int speciesId, String label) {
        if (s.sourcePets.isEmpty() || s.sourcePets.get(0).speciesId != speciesId) {
            throw new IllegalStateException(label + " active pet mismatch expectedSpecies="
                    + speciesId + " pets=" + s.sourcePets.size()
                    + " actual=" + (s.sourcePets.isEmpty() ? -1 : s.sourcePets.get(0).speciesId)
                    + " trace=" + tailTrace(s, 12));
        }
        if ("Neil".equals(s.battlePlayerName)) {
            throw new IllegalStateException(label + " must not render Neil fallback"
                    + " battlePlayerName=" + s.battlePlayerName
                    + " trace=" + tailTrace(s, 12));
        }
        s.sourceStateTrace.add("SMOKE verified " + label + " active species=" + speciesId
                + " battlePlayer=" + s.battlePlayerName);
    }

    private static void assertEnemyOwnedMarker(VqsvIntroDemo.Scene s, boolean expected, String label) {
        if (s.battleEnemyOwnedSpecies != expected) {
            throw new IllegalStateException(label + " enemy owned marker mismatch expected="
                    + expected + " actual=" + s.battleEnemyOwnedSpecies
                    + " enemy=" + s.battleEnemyName
                    + " pets=" + s.sourcePets.size()
                    + " bankPets=" + s.sourcePetBank.size()
                    + " trace=" + tailTrace(s, 12));
        }
        s.sourceStateTrace.add("SMOKE verified " + label
                + " enemyOwned=" + s.battleEnemyOwnedSpecies
                + " source=game.h.b -> game.g.a(element,species)==2");
    }

    private static void assertBattleSnapshot(VqsvIntroDemo.Scene s, String label) {
        if (s.battleBackgroundSnapshot == null) {
            throw new IllegalStateException(label + " expected map-backed game.d.c snapshot"
                    + " useMap=" + s.useMap
                    + " mapRenderer=" + (s.mapRenderer == null ? "null" : "present")
                    + " trace=" + tailTrace(s, 12));
        }
        s.sourceStateTrace.add("SMOKE verified " + label + " game.d.c snapshot present");
    }

    private static int payloadHp(SourcePetState pet) {
        return pet != null && pet.sourcePayload != null && pet.sourcePayload.length > 6
                ? pet.sourcePayload[6] : -1;
    }

    private static void assertPayloadHp(SourcePetState pet, int expected, String label) {
        int actual = payloadHp(pet);
        if (actual != expected) {
            throw new IllegalStateException(label + " payload HP mismatch expected="
                    + expected + " actual=" + actual);
        }
    }

    private static SourcePetState findSourcePet(VqsvIntroDemo.Scene s, int speciesId) {
        for (SourcePetState pet : s.sourcePets) {
            if (pet.speciesId == speciesId) {
                return pet;
            }
        }
        return null;
    }

    private static void openEvolutionUiForSmoke(VqsvIntroDemo.Scene s, int speciesId, int level,
                                                int materialId, int materialCount) {
        s.eventIndex = s.events.size();
        SourcePetState pet = new SourcePetState(0, speciesId, level, 3, 2, 0, -1);
        s.sourcePets.add(pet);
        if (materialId >= 0) {
            SourceSpecialReward material = s.sourceSpecialRewards.computeIfAbsent(materialId,
                    SourceSpecialReward::fromSourceDb);
            material.stackCount = materialCount;
        }
        s.sourceEvolutionL[0] = level;
        s.sourceEvolutionL[1] = speciesId;
        s.sourceEvolutionTutorialPending = true;
        int guard = 0;
        while (!s.worldPetstateVisible && guard++ < 80) {
            s.press0();
            s.tick();
        }
        if (!s.worldPetstateVisible || s.battleMenuIndex != 0) {
            throw new IllegalStateException("Expected evolution smoke petstate bridge, visible="
                    + s.worldPetstateVisible
                    + " index=" + s.battleMenuIndex
                    + " trace=" + tailTrace(s, 20));
        }
        s.press0();
        s.tick();
        if (!s.sourceEvolveVisible) {
            throw new IllegalStateException("Expected evolve.ui open for smoke, visible="
                    + s.sourceEvolveVisible
                    + " trace=" + tailTrace(s, 20));
        }
    }

    private static int sourceMaxHp(SourcePetState pet) {
        return BattleUnit.fromSourcePet(pet, (byte) 0).maxHp();
    }

    static void tickSceneFastForward(VqsvIntroDemo.Scene s, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            if ("P20".equals(s.battleStateName)) {
                s.press0();
            }
            if ("P21".equals(s.battleStateName) || "P17".equals(s.battleStateName)
                    || "P4".equals(s.battleStateName) || "P16".equals(s.battleStateName)) {
                s.press0();
            }
            s.tick();
        }
    }

    private static void tickCurrentUntilDone(VqsvIntroDemo.Scene s, int maxTicks) {
        int guard = 0;
        while (s.current != null && guard++ < maxTicks) {
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            if ("P20".equals(s.battleStateName)) {
                s.press0();
            }
            if ("P21".equals(s.battleStateName) || "P17".equals(s.battleStateName)
                    || "P4".equals(s.battleStateName) || "P16".equals(s.battleStateName)) {
                s.press0();
            }
            s.tick();
        }
        if (s.current != null) {
            throw new IllegalStateException("Checkpoint current did not finish in " + maxTicks
                    + " ticks state=" + s.battleStateName
                    + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " log=" + s.battleLog
                    + " command=" + s.battleCommandIndex);
        }
    }

    private static void tickUntilBattleState(VqsvIntroDemo.Scene s, String stateName, int maxTicks) {
        int guard = 0;
        while (!stateName.equals(s.battleStateName) && guard++ < maxTicks) {
            s.tick();
        }
        if (!stateName.equals(s.battleStateName)) {
            throw new IllegalStateException("Battle state " + stateName
                    + " not reached in " + maxTicks + " ticks, current=" + s.battleStateName);
        }
    }

    private static void tickUntilNpcEnemyEntryStep(VqsvIntroDemo.Scene s, int minStep, int maxTicks) {
        int guard = 0;
        while ("NPCVS".equals(s.battleStateName)
                && s.battleNpcEnemyEntryStep < minStep
                && guard++ < maxTicks) {
            s.tick();
        }
        if (!"NPCVS".equals(s.battleStateName) || s.battleNpcEnemyEntryStep < minStep) {
            throw new IllegalStateException("NPCVS step " + minStep
                    + " not reached in " + maxTicks
                    + " currentState=" + s.battleStateName
                    + " stepFrame=" + s.battleNpcEnemyEntryStep + "/" + s.battleNpcEnemyEntryFrame);
        }
    }

    private static void tickUntilBattleEntryOffset(VqsvIntroDemo.Scene s, boolean playerSide, int maxTicks) {
        int guard = 0;
        while (guard++ < maxTicks) {
            s.tick();
            boolean active = playerSide
                    ? s.battleP7PlayerOffsetX != 0 || s.battleP7PlayerOffsetY != 0
                    : s.battleP7EnemyOffsetX != 0 || s.battleP7EnemyOffsetY != 0;
            if ("P0".equals(s.battleStateName) && active && battleEntryActorVisible(s, playerSide)) {
                s.sourceStateTrace.add("SMOKE verified battle P0 cpos "
                        + (playerSide ? "player" : "enemy")
                        + " offset=("
                        + (playerSide ? s.battleP7PlayerOffsetX : s.battleP7EnemyOffsetX)
                        + ","
                        + (playerSide ? s.battleP7PlayerOffsetY : s.battleP7EnemyOffsetY)
                        + ")");
                return;
            }
        }
        throw new IllegalStateException("Battle entry cpos visible offset not reached side="
                + (playerSide ? "player" : "enemy")
                + " state=" + s.battleStateName
                + " playerOffset=" + s.battleP7PlayerOffsetX + "," + s.battleP7PlayerOffsetY
                + " enemyOffset=" + s.battleP7EnemyOffsetX + "," + s.battleP7EnemyOffsetY
                + " trace=" + tailTrace(s, 12));
    }

    private static boolean battleEntryActorVisible(VqsvIntroDemo.Scene s, boolean playerSide) {
        int x = (playerSide ? 18 : 132) + (playerSide ? s.battleP7PlayerOffsetX : s.battleP7EnemyOffsetX);
        int y = (playerSide ? 140 : 70) + (playerSide ? s.battleP7PlayerOffsetY : s.battleP7EnemyOffsetY);
        int w = playerSide ? 96 : 96;
        int h = playerSide ? 95 : 118;
        return x < W && x + w > 0 && y < H && y + h > 0;
    }

    private static boolean entryPercentTweened(int start, int mid, int end) {
        if (end == 100) {
            return start == 100 && mid == 100;
        }
        int low = Math.min(start, end);
        int high = Math.max(start, end);
        return mid > low && mid < high;
    }

    private static void tickUntilBattleCatchPhase(VqsvIntroDemo.Scene s, int phase, int maxTicks) {
        int guard = 0;
        while (guard++ < maxTicks) {
            if ("P17".equals(s.battleStateName) && s.battleCatchPhase == phase) {
                s.sourceStateTrace.add("SMOKE verified battle P17 catch phase q=" + phase
                        + " item=" + s.battleCatchItemId
                        + " caught=" + s.battleCatchCaught
                        + " effect=" + s.battleCatchEffectVisible
                        + " cursor=" + s.battleCatchAnimCursor);
                return;
            }
            s.tick();
        }
        throw new IllegalStateException("Battle catch phase q=" + phase
                + " not reached in " + maxTicks
                + " ticks state=" + s.battleStateName
                + " currentPhase=" + s.battleCatchPhase
                + " item=" + s.battleCatchItemId
                + " caught=" + s.battleCatchCaught
                + " effect=" + s.battleCatchEffectVisible
                + " trace=" + tailTrace(s, 16));
    }

    private static void tickUntilBattleCatchPhaseCursor(VqsvIntroDemo.Scene s, int phase, int minCursor, int maxTicks) {
        int guard = 0;
        while (guard++ < maxTicks) {
            if ("P17".equals(s.battleStateName)
                    && s.battleCatchPhase == phase
                    && s.battleCatchAnimCursor >= minCursor) {
                s.sourceStateTrace.add("SMOKE verified battle P17 catch phase q=" + phase
                        + " cursor>=" + minCursor
                        + " actual=" + s.battleCatchAnimCursor
                        + " item=" + s.battleCatchItemId
                        + " caught=" + s.battleCatchCaught);
                return;
            }
            s.tick();
        }
        throw new IllegalStateException("Battle catch phase q=" + phase
                + " cursor>=" + minCursor
                + " not reached in " + maxTicks
                + " ticks state=" + s.battleStateName
                + " currentPhase=" + s.battleCatchPhase
                + " cursor=" + s.battleCatchAnimCursor
                + " item=" + s.battleCatchItemId
                + " caught=" + s.battleCatchCaught
                + " trace=" + tailTrace(s, 16));
    }

    private static void tickUntilCatchType8Step(VqsvIntroDemo.Scene s, int phase,
                                                int scale10, int dx, int dy, int maxTicks) {
        int guard = 0;
        while (guard++ < maxTicks) {
            if ("P17".equals(s.battleStateName)
                    && s.battleCatchPhase == phase
                    && s.battleCatchEffectVisible
                    && s.battleCatchEffectScale10 == scale10
                    && s.battleCatchEffectDx == dx
                    && s.battleCatchEffectDy == dy) {
                s.sourceStateTrace.add("SMOKE verified ah type8 P17 q=" + phase
                        + " scale10=" + scale10
                        + " dx=" + dx
                        + " dy=" + dy
                        + " source=ah.e case8 l.b(l.a(...),1,50)");
                return;
            }
            s.tick();
        }
        throw new IllegalStateException("P17 ah type8 step not reached phase=" + phase
                + " expected=[" + scale10 + "," + dx + "," + dy + "]"
                + " state=" + s.battleStateName
                + " currentPhase=" + s.battleCatchPhase
                + " visible=" + s.battleCatchEffectVisible
                + " actual=[" + s.battleCatchEffectScale10
                + "," + s.battleCatchEffectDx
                + "," + s.battleCatchEffectDy + "]"
                + " trace=" + tailTrace(s, 18));
    }

    private static void assertSprite269TimingMatrix(VqsvIntroDemo.Scene s) {
        SpriteAnim anim = SpriteAnim.load(269);
        if (anim.data.frames.length != 30 || anim.data.cells.length != 54 || anim.data.anim.length != 5) {
            throw new IllegalStateException("Sprite 269 source matrix mismatch frames="
                    + anim.data.frames.length + " cells=" + anim.data.cells.length
                    + " anims=" + anim.data.anim.length);
        }
        short[][] expected = new short[][]{
                {1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 8},
                {1, 9},
                {1, 10, 1, 11, 2, 12, 2, 13, 1, 14, 1, 15, 1, 14, 1, 16, 1, 14, 1, 15,
                        1, 14, 1, 16, 1, 14, 1, 15, 1, 14, 1, 16, 2, 17, 2, 12, 2, 18, 1, 10},
                {1, 19, 1, 20, 1, 21, 1, 22, 1, 23, 1, 24, 1, 25, 1, 26, 1, 27, 1, 28, 5, 23},
                {1, 29, 1, 30, 1, 31, 1, 32, 1, 33, 3, 34, 1, 35, 1, 36, 1, 37, 1, 38,
                        1, 39, 1, 40, 1, 39, 1, 41, 1, 39, 1, 40, 1, 39, 1, 41, 1, 39, 1, 40,
                        1, 39, 1, 41, 1, 42, 1, 37, 1, 43, 1, 35, 1, 44, 1, 45, 1, 46, 1, 47,
                        1, 48, 1, 49, 1, 50, 1, 51, 1, 52, 1, 53, 1, 48}
        };
        int[] sourceTicks = new int[expected.length];
        for (int i = 0; i < expected.length; i++) {
            if (!java.util.Arrays.equals(anim.data.anim[i], expected[i])) {
                throw new IllegalStateException("Sprite 269 anim state " + i
                        + " mismatch expected=" + java.util.Arrays.toString(expected[i])
                        + " actual=" + java.util.Arrays.toString(anim.data.anim[i]));
            }
            for (int j = 0; j < expected[i].length; j += 2) {
                sourceTicks[i] += Math.max(1, expected[i][j]);
            }
        }
        int[] expectedTicks = new int[]{8, 1, 25, 15, 39};
        if (!java.util.Arrays.equals(sourceTicks, expectedTicks)) {
            throw new IllegalStateException("Sprite 269 source tick totals mismatch expected="
                    + java.util.Arrays.toString(expectedTicks)
                    + " actual=" + java.util.Arrays.toString(sourceTicks));
        }
        s.sourceStateTrace.add("SMOKE verified sprite269 source anim matrix q0..q4 frames=[8,1,20,11,37]"
                + " sourceTicks=" + java.util.Arrays.toString(sourceTicks)
                + " sourceEndCheck=game.d.e() q>=r-1, tick=game.d.d()");
    }

    private static void tickUntilAnyBattleState(VqsvIntroDemo.Scene s, int maxTicks, String... stateNames) {
        int guard = 0;
        while (!isBattleState(s, stateNames) && guard++ < maxTicks) {
            if ("P20".equals(s.battleStateName)) {
                s.press0();
            }
            s.tick();
        }
        if (!isBattleState(s, stateNames)) {
            throw new IllegalStateException("Battle states " + java.util.Arrays.toString(stateNames)
                    + " not reached in " + maxTicks + " ticks, current=" + s.battleStateName
                    + " trace=" + tailTrace(s, 10));
        }
    }

    private static boolean isBattleState(VqsvIntroDemo.Scene s, String... stateNames) {
        for (String stateName : stateNames) {
            if (stateName.equals(s.battleStateName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean traceContains(VqsvIntroDemo.Scene s, String needle) {
        for (String line : s.sourceStateTrace) {
            if (line.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static int traceIndex(VqsvIntroDemo.Scene s, String needle) {
        for (int i = 0; i < s.sourceStateTrace.size(); i++) {
            if (s.sourceStateTrace.get(i).contains(needle)) {
                return i;
            }
        }
        return -1;
    }

    private static int latestTraceDamage(VqsvIntroDemo.Scene s, String needle) {
        for (int i = s.sourceStateTrace.size() - 1; i >= 0; i--) {
            String line = s.sourceStateTrace.get(i);
            if (!line.contains(needle)) {
                continue;
            }
            int at = line.indexOf("damage=");
            if (at < 0) {
                return -1;
            }
            int start = at + "damage=".length();
            int end = start;
            while (end < line.length() && Character.isDigit(line.charAt(end))) {
                end++;
            }
            if (end == start) {
                return -1;
            }
            return Integer.parseInt(line.substring(start, end));
        }
        return -1;
    }

    private static int traceCount(VqsvIntroDemo.Scene s, String needle) {
        int count = 0;
        for (String line : s.sourceStateTrace) {
            if (line.contains(needle)) {
                count++;
            }
        }
        return count;
    }

    private static void tickUntilTraceContains(VqsvIntroDemo.Scene s, String needle, int maxTicks) {
        int guard = 0;
        while (!traceContains(s, needle) && guard++ < maxTicks) {
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            s.tick();
        }
        if (!traceContains(s, needle)) {
            throw new IllegalStateException("Trace not reached: " + needle
                    + " in " + maxTicks + " ticks, state=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static int sourceExpectedExpAward(int enemyLevel, int enemyQuality,
                                              int participantLevel, int participantCount) {
        return sourceExpectedExpAward(enemyLevel, enemyQuality, participantLevel, participantCount, 1000);
    }

    private static int sourceExpectedExpAward(int enemyLevel, int enemyQuality,
                                              int participantLevel, int participantCount,
                                              int divisor) {
        int[] aG = {10, 11, 12, 13, 15};
        int[] aH = {10, 12, 13, 14, 15, 16};
        int[] aI = {105, 100, 80, 60, 40, 20, 5};
        int quality = Math.max(1, Math.min(5, enemyQuality));
        int count = Math.max(1, Math.min(6, participantCount));
        int base = (((enemyLevel << 1) * enemyLevel + 50) * aG[quality - 1] / 10) + 400;
        int diff = Math.max(1, participantLevel) - enemyLevel;
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
        return Math.max(0, base / count * aH[count - 1] * levelFactor / Math.max(1, divisor));
    }

    private static int sourceExpectedStatusParam(int statusId, int index, int fallback) {
        BattleStatusRow row = VqsvBattleTables.instance().status(statusId);
        return row == null ? fallback : VqsvBattleTables.get(row.raw, index, fallback);
    }

    private static int sourceExpectedPostExpPassiveHeal(int speciesId) {
        BattleSpeciesRow species = VqsvBattleTables.instance().species(speciesId);
        short[] passiveRow = VqsvBattleTables.instance().row(2, 0);
        int baseHp = species == null ? 0 : VqsvBattleTables.get(species.raw, 5, 0);
        int percent = VqsvBattleTables.get(passiveRow, 6, 0);
        return Math.max(0, baseHp * percent / 100);
    }

    private static int sourcePetExp(SourcePetState pet) {
        return pet != null && pet.sourcePayload != null && pet.sourcePayload.length > 7
                ? pet.sourcePayload[7]
                : 0;
    }

    private static void assertSourcePetExp(SourcePetState pet, int expected, String label) {
        int actual = sourcePetExp(pet);
        if (actual != expected) {
            throw new IllegalStateException(label + " EXP mismatch expected="
                    + expected + " actual=" + actual
                    + " species=" + (pet == null ? -1 : pet.speciesId)
                    + " level=" + (pet == null ? -1 : pet.level)
                    + " payload=" + (pet == null ? "null" : java.util.Arrays.toString(pet.sourcePayload)));
        }
    }

    private static SourcePetState setupNormalP8ExpInitialFrame(VqsvIntroDemo.Scene s, String label) {
        s.eventIndex = s.events.size();
        SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
        pet.sourcePayload[7] = 0;
        s.sourcePets.add(pet);
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
        int guard = 0;
        while ((!"P8".equals(s.battleStateName)
                || !"levelup".equals(s.battleUiMode)
                || s.battleLevelUpView == null
                || s.battleLevelUpView.leveled)
                && guard++ < 360) {
            s.tick();
        }
        if (!"P8".equals(s.battleStateName)
                || !"levelup".equals(s.battleUiMode)
                || s.battleLevelUpView == null
                || s.battleLevelUpView.leveled
                || s.battleLevelUpView.expValue != 0
                || !traceContains(s, "game.h.a initial render before game.h.am")) {
            throw new IllegalStateException(label + " expected P8 frame0 EXP"
                    + " state=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " value=" + (s.battleLevelUpView == null ? -1 : s.battleLevelUpView.expValue)
                    + " trace=" + tailTrace(s, 28));
        }
        return pet;
    }

    private static void handleBattleExpP8AnimationCheckpoint(VqsvIntroDemo.Scene s, String checkpoint) {
        SourcePetState pet = setupNormalP8ExpInitialFrame(s, checkpoint);
        int expected = sourceExpectedExpAward(5, 1, 7, 1);
        int expectedValue = 0;
        if ("battle_exp_p8_frame1".equals(checkpoint)) {
            s.tick();
            expectedValue = 8;
        } else if ("battle_exp_p8_mid".equals(checkpoint)) {
            for (int i = 0; i < 10; i++) {
                s.tick();
            }
            expectedValue = 80;
        } else if ("battle_exp_p8_target_hold".equals(checkpoint)) {
            int guard = 0;
            while (s.battleLevelUpView != null
                    && s.battleLevelUpView.expValue < expected
                    && guard++ < 120) {
                s.tick();
            }
            expectedValue = expected;
        }
        if (!"P8".equals(s.battleStateName)
                || !"levelup".equals(s.battleUiMode)
                || s.battleLevelUpView == null
                || s.battleLevelUpView.leveled
                || s.battleLevelUpView.expValue != expectedValue
                || traceContains(s, "P22 game.h.an/ao levelUp")) {
            throw new IllegalStateException(checkpoint + " expected P8 EXP value="
                    + expectedValue
                    + " actual=" + (s.battleLevelUpView == null ? -1 : s.battleLevelUpView.expValue)
                    + " state=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " trace=" + tailTrace(s, 30));
        }
        assertSourcePetExp(pet, expected, checkpoint + " source EXP commit");
        assertP8ExpPosMidRow0(s, checkpoint + " pos.mid");
        s.sourceStateTrace.add("SMOKE verified " + checkpoint
                + " expValue=" + expectedValue
                + " target=" + expected);
    }

    private static void tickUntilBattleP7Phase(VqsvIntroDemo.Scene s, int phase, int maxTicks) {
        int guard = 0;
        while (!"P7".equals(s.battleStateName) || s.battleP7Phase != phase) {
            if (guard++ >= maxTicks) {
                throw new IllegalStateException("Battle P7 phase " + phase
                        + " not reached in " + maxTicks
                        + " ticks, current=" + s.battleStateName
                        + " phase=" + s.battleP7Phase);
            }
            s.tick();
        }
    }

    private static void revealCheckpointText(VqsvIntroDemo.Scene s, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (s.text == null) {
                return;
            }
            s.text.tick(s.font);
        }
    }

    private static void placePlayerForActorMaskSmoke(VqsvIntroDemo.Scene s, int actorId,
                                                     boolean actorHitMask, int direction) {
        Actor actor = s.actors[actorId];
        if (actor == null) {
            throw new IllegalStateException("Missing smoke actor " + actorId);
        }
        for (int dy = -28; dy <= 28; dy += 2) {
            for (int dx = -28; dx <= 28; dx += 2) {
                placePlayerForSmoke(s, actor.x + dx, actor.y + dy, direction);
                if (s.playerIntersectsActorSourceMask(actorId, actorHitMask)) {
                    return;
                }
            }
        }
        throw new IllegalStateException("Could not place player on actor mask " + actorId);
    }

    private static void placePlayerForActorInteractionSmoke(VqsvIntroDemo.Scene s, int actorId) {
        Actor actor = s.actors[actorId];
        if (actor == null) {
            throw new IllegalStateException("Missing smoke actor " + actorId);
        }
        for (int dir = 0; dir < 4; dir++) {
            for (int dy = -28; dy <= 28; dy += 2) {
                for (int dx = -28; dx <= 28; dx += 2) {
                    placePlayerForSmoke(s, actor.x + dx, actor.y + dy, dir);
                    if (s.playerInteractsActorSourceMask(actorId)) {
                        return;
                    }
                }
            }
        }
        throw new IllegalStateException("Could not place player for actor interaction " + actorId);
    }

    private static void placePlayerForSmoke(VqsvIntroDemo.Scene s, int x, int y, int direction) {
        s.player.x = x;
        s.player.y = y;
        s.playerX = x;
        s.playerY = y;
        s.player.direction = direction;
        s.player.visible = true;
        s.player.applyMode(0);
        s.setCameraCenter(x, y);
    }

    private static boolean isPanelCheckpoint(String checkpoint) {
        return checkpoint.startsWith("panel_gamemenu_")
                || checkpoint.startsWith("panel_gamesystem_")
                || checkpoint.startsWith("panel_petstate_")
                || checkpoint.startsWith("panel_bag_")
                || checkpoint.startsWith("panel_task_")
                || checkpoint.startsWith("panel_petmap_")
                || checkpoint.startsWith("panel_save_")
                || checkpoint.startsWith("world_petstate_mouse_");
    }

    private static void handlePanelCheckpoint(VqsvIntroDemo.Scene s, String checkpoint) {
        if ("panel_gamemenu_open_from_world".equals(checkpoint)) {
            setupPanelSmokeWorld(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected gamemenu.ui panel open selected=0 visible="
                        + s.panelRuntime.visible
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 16));
            }
            if (!traceContains(s, "game.h.k gamemenu.ui open")) {
                throw new IllegalStateException("Expected source panel open trace, trace="
                        + tailTrace(s, 16));
            }
            return;
        }
        if ("panel_gamemenu_click_softkey_open".equals(checkpoint)) {
            setupPanelSmokeWorld(s);
            s.worldUi.visible = true;
            s.click(230 * VqsvIntroDemo.SCALE, 310 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected gamemenu.ui panel open from softkey visible="
                        + s.panelRuntime.visible
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 16));
            }
            if (!traceContains(s, "game.h.k gamemenu.ui open")) {
                throw new IllegalStateException("Expected source panel softkey open trace, trace="
                        + tailTrace(s, 16));
            }
            return;
        }
        if ("panel_gamesystem_click_softkey_open".equals(checkpoint)) {
            setupPanelSmokeWorld(s);
            s.worldUi.visible = true;
            s.click(10 * VqsvIntroDemo.SCALE, 310 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected gamesystem.ui panel open from left world softkey"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 16));
            }
            if (!traceContains(s, "world.ui left softkey")) {
                throw new IllegalStateException("Expected source world.ui left softkey trace, trace="
                        + tailTrace(s, 16));
            }
            return;
        }
        if ("panel_gamemenu_navigation".equals(checkpoint)) {
            setupPanelSmokeWorld(s);
            s.keyBack = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            if (!s.panelRuntime.visible || s.panelRuntime.selected != 2) {
                throw new IllegalStateException("Expected gamemenu.ui selected=2 visible="
                        + s.panelRuntime.visible
                        + " selected=" + s.panelRuntime.selected
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 20));
            }
            if (!traceContains(s, "key=8448 selected=2")) {
                throw new IllegalStateException("Expected panel down navigation trace, trace="
                        + tailTrace(s, 20));
            }
            return;
        }
        if ("panel_gamemenu_back_returns_world".equals(checkpoint)) {
            setupPanelSmokeWorld(s);
            s.keyBack = true;
            s.tick();
            s.keyBack = true;
            s.tick();
            int beforeX = s.player.x;
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            if (s.panelRuntime.visible || s.player.x <= beforeX) {
                throw new IllegalStateException("Expected panel close and world movement visible="
                        + s.panelRuntime.visible
                        + " beforeX=" + beforeX
                        + " afterX=" + s.player.x
                        + " trace=" + tailTrace(s, 20));
            }
            if (!traceContains(s, "back close gamemenu.ui -> P=0")) {
                throw new IllegalStateException("Expected panel back close trace, trace="
                        + tailTrace(s, 20));
            }
            return;
        }
        if ("panel_gamesystem_open_from_gamemenu".equals(checkpoint)) {
            openGameSystemPanelForSmoke(s);
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected gamesystem.ui open selected=0 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 18));
            }
            if (!traceContains(s, "P=14 game.h.m gamesystem.ui open")) {
                throw new IllegalStateException("Expected gamesystem open source trace, trace="
                        + tailTrace(s, 18));
            }
            return;
        }
        if ("panel_gamesystem_navigation".equals(checkpoint)) {
            openGameSystemPanelForSmoke(s);
            s.keyDown = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 2) {
                throw new IllegalStateException("Expected gamesystem.ui selected=2 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 22));
            }
            if (!traceContains(s, "game.h.n key=8448 selected=2")) {
                throw new IllegalStateException("Expected gamesystem down navigation trace, trace="
                        + tailTrace(s, 22));
            }
            return;
        }
        if ("panel_gamesystem_continue_returns_world".equals(checkpoint)) {
            openGameSystemPanelForSmoke(s);
            s.key0 = true;
            s.tick();
            int beforeX = s.player.x;
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            if (s.panelRuntime.visible || s.player.x <= beforeX) {
                throw new IllegalStateException("Expected gamesystem continue close and movement visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " beforeX=" + beforeX
                        + " afterX=" + s.player.x
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "game.h.n confirm selected=0 close gamesystem.ui -> P=0")) {
                throw new IllegalStateException("Expected gamesystem continue trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_gamesystem_back_returns_world".equals(checkpoint)) {
            openGameSystemPanelForSmoke(s);
            s.keyBack = true;
            s.tick();
            int beforeX = s.player.x;
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            if (s.panelRuntime.visible || s.player.x <= beforeX) {
                throw new IllegalStateException("Expected gamesystem back close and movement visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " beforeX=" + beforeX
                        + " afterX=" + s.player.x
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "game.h.n close gamesystem.ui -> P=0")) {
                throw new IllegalStateException("Expected gamesystem back close trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_gamesystem_help_open".equals(checkpoint)) {
            openGameSystemHelpForSmoke(s);
            if (!s.panelRuntime.visible || !"HELP".equals(s.panelRuntime.modeName())
                    || !"Tr\u1ee3 gi\u00fap 1/3".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected help1.ui open page 1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.u help1.ui open r=0")) {
                throw new IllegalStateException("Expected help open source trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_gamesystem_help_page_right".equals(checkpoint)) {
            openGameSystemHelpForSmoke(s);
            s.keyRight = true;
            s.tick();
            if (!s.panelRuntime.visible || !"HELP".equals(s.panelRuntime.modeName())
                    || !"Tr\u1ee3 gi\u00fap 2/3".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected help1.ui page 2 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 32));
            }
            if (!traceContains(s, "game.h.v key=32832 help1.ui r=1")) {
                throw new IllegalStateException("Expected help page-right source trace, trace="
                        + tailTrace(s, 32));
            }
            return;
        }
        if ("panel_gamesystem_help_back_returns_gamesystem".equals(checkpoint)) {
            openGameSystemHelpForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1) {
                throw new IllegalStateException("Expected help back to gamesystem selected=1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 32));
            }
            if (!traceContains(s, "game.h.v back close help1.ui -> P=13 gamesystem.ui selected=1")) {
                throw new IllegalStateException("Expected help back source trace, trace="
                        + tailTrace(s, 32));
            }
            return;
        }
        if ("panel_gamesystem_settings_open".equals(checkpoint)) {
            openGameSystemSettingsForSmoke(s);
            if (!s.panelRuntime.visible || !"SETTINGS".equals(s.panelRuntime.modeName())
                    || !"T\u00f9y ch\u1ecdn 0/3".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected help.ui settings open visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 30));
            }
            if (!traceContains(s, "game.h.w help.ui settings open g=0")) {
                throw new IllegalStateException("Expected settings open source trace, trace="
                        + tailTrace(s, 30));
            }
            return;
        }
        if ("panel_gamesystem_settings_adjust_right".equals(checkpoint)) {
            openGameSystemSettingsForSmoke(s);
            s.keyRight = true;
            s.tick();
            if (!s.panelRuntime.visible || !"SETTINGS".equals(s.panelRuntime.modeName())
                    || !"T\u00f9y ch\u1ecdn 1/3".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected help.ui settings level 1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 34));
            }
            if (!traceContains(s, "game.h.x key=32832 help.ui settings game.i.a().h g=1")) {
                throw new IllegalStateException("Expected settings right-adjust source trace, trace="
                        + tailTrace(s, 34));
            }
            return;
        }
        if ("panel_gamesystem_settings_back_returns_gamesystem".equals(checkpoint)) {
            openGameSystemSettingsForSmoke(s);
            s.keyRight = true;
            s.tick();
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 2) {
                throw new IllegalStateException("Expected settings back to gamesystem selected=2 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 36));
            }
            if (!traceContains(s, "game.h.x back close help.ui -> P=13 gamesystem.ui selected=2 g=1")) {
                throw new IllegalStateException("Expected settings back source trace, trace="
                        + tailTrace(s, 36));
            }
            return;
        }
        if ("panel_gamesystem_option_open".equals(checkpoint)) {
            openGameSystemOptionForSmoke(s);
            if (!s.panelRuntime.visible || !"OPTION_CONFIRM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1
                    || !"Kh\u00f4ng".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected option.ui open selected c=1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 34));
            }
            if (!traceContains(s, "open option.ui c=1 widget12='' widget13=Khong")) {
                throw new IllegalStateException("Expected option open source trace, trace="
                        + tailTrace(s, 34));
            }
            return;
        }
        if ("panel_gamesystem_option_navigate_up".equals(checkpoint)) {
            openGameSystemOptionForSmoke(s);
            s.keyUp = true;
            s.tick();
            if (!s.panelRuntime.visible || !"OPTION_CONFIRM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected option.ui selected c=0 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 38));
            }
            if (!traceContains(s, "option.ui key=4100 c=0")) {
                throw new IllegalStateException("Expected option up navigation trace, trace="
                        + tailTrace(s, 38));
            }
            return;
        }
        if ("panel_gamesystem_option_back_returns_gamesystem".equals(checkpoint)) {
            openGameSystemOptionForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 3) {
                throw new IllegalStateException("Expected option back to gamesystem selected=3 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 38));
            }
            if (!traceContains(s, "option.ui back close option.ui f=0 -> gamesystem.ui selected=3")) {
                throw new IllegalStateException("Expected option back source trace, trace="
                        + tailTrace(s, 38));
            }
            return;
        }
        if ("panel_gamesystem_option_confirm_no_returns_gamesystem".equals(checkpoint)) {
            openGameSystemOptionForSmoke(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 3) {
                throw new IllegalStateException("Expected option confirm no to gamesystem selected=3 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 38));
            }
            if (!traceContains(s, "option.ui confirm c=1 close option.ui f=0 no-reset")) {
                throw new IllegalStateException("Expected option no-confirm source trace, trace="
                        + tailTrace(s, 38));
            }
            return;
        }
        if ("panel_petstate_open_from_gamemenu".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            if (!s.worldPetstateVisible || s.panelRuntime.visible || s.battleMenuIndex != 0
                    || s.battlePetStateRows.length < 2) {
                throw new IllegalStateException("Expected petstate.ui from gamemenu visible="
                        + s.worldPetstateVisible
                        + " panel=" + s.panelRuntime.visible
                        + " index=" + s.battleMenuIndex
                        + " rows=" + s.battlePetStateRows.length
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "P=7 game.h.W petstate.ui open")) {
                throw new IllegalStateException("Expected petstate source path trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_petstate_navigation".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (!s.worldPetstateVisible || s.battleMenuIndex != 1) {
                throw new IllegalStateException("Expected petstate row 1 after down visible="
                        + s.worldPetstateVisible
                        + " index=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_petstate_hover_preview_no_confirm".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.hover(80 * VqsvIntroDemo.SCALE, 109 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.sourcePetSettingVisible
                    || s.battleMenuIndex != 1) {
                throw new IllegalStateException("Expected petstate hover row 1 without opening petsetting"
                        + " worldPet=" + s.worldPetstateVisible
                        + " panel=" + s.panelRuntime.visible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " index=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petstate_back_returns_gamemenu".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (s.worldPetstateVisible || !s.panelRuntime.visible
                    || !"GAMEMENU".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1) {
                throw new IllegalStateException("Expected petstate back to gamemenu row 1 visible="
                        + s.worldPetstateVisible
                        + " panel=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.X back close petstate.ui")) {
                throw new IllegalStateException("Expected petstate back source trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petstate_petsetting_open".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.key0 = true;
            s.tick();
            if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                    || s.sourcePetSettingIndex != 0 || s.sourcePetSettingCount < 5) {
                throw new IllegalStateException("Expected petsetting.ui open over petstate visible="
                        + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " index=" + s.sourcePetSettingIndex
                        + " rows=" + s.sourcePetSettingCount
                        + " trace=" + tailTrace(s, 34));
            }
            if (!traceContains(s, "petstate confirm -> petsetting.ui f=1")) {
                throw new IllegalStateException("Expected petsetting source open trace, trace="
                        + tailTrace(s, 34));
            }
            return;
        }
        if ("panel_petstate_petsetting_navigation".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.key0 = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            if (!s.sourcePetSettingVisible || s.sourcePetSettingIndex != 1) {
                throw new IllegalStateException("Expected petsetting index 1 visible="
                        + s.sourcePetSettingVisible
                        + " index=" + s.sourcePetSettingIndex
                        + " trace=" + tailTrace(s, 36));
            }
            if (!traceContains(s, "petsetting key=8448 c=1")) {
                throw new IllegalStateException("Expected petsetting navigation trace, trace="
                        + tailTrace(s, 36));
            }
            return;
        }
        if ("panel_petstate_petsetting_back_returns_petstate".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.key0 = true;
            s.tick();
            s.keyBack = true;
            s.tick();
            if (!s.worldPetstateVisible || s.sourcePetSettingVisible || s.panelRuntime.visible) {
                throw new IllegalStateException("Expected petsetting back to petstate worldPet="
                        + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " panel=" + s.panelRuntime.visible
                        + " trace=" + tailTrace(s, 36));
            }
            if (!traceContains(s, "back close petsetting.ui -> petstate.ui")) {
                throw new IllegalStateException("Expected petsetting back source trace, trace="
                        + tailTrace(s, 36));
            }
            return;
        }
        if ("panel_petstate_petsetting_confirm_pending".equals(checkpoint)) {
            openPanelPetstateForSmoke(s);
            s.key0 = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourcePetSettingActiveWarningMode != 2
                    || s.text == null
                    || !VqsvText.Battle.PET_ALREADY_DEPLOYED.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected legacy c=1 checkpoint to hit active warning visible="
                        + s.sourcePetSettingVisible
                        + " index=" + s.sourcePetSettingIndex
                        + " activeWarning=" + s.sourcePetSettingActiveWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 40));
            }
            if (!traceContains(s, "petsetting c=1 b==0 -> msgwarm.ui f=2")) {
                throw new IllegalStateException("Expected petsetting c=1 active warning trace, trace="
                        + tailTrace(s, 40));
            }
            return;
        }
        if ("panel_petstate_petsetting_active_switch_success".equals(checkpoint)) {
            openPanelActiveSwitchForSmoke(s, 1, false);
            int selectedSpecies = s.sourcePets.get(1).speciesId;
            int originalLeadSpecies = s.sourcePets.get(0).speciesId;
            s.key0 = true;
            s.tick();
            if (!s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourcePetSettingActiveWarningMode != 0
                    || s.text != null
                    || s.battleMenuIndex != 0
                    || s.sourcePets.get(0).speciesId != selectedSpecies
                    || s.sourcePets.get(1).speciesId != originalLeadSpecies
                    || s.sourcePets.get(0).slot != 0
                    || s.sourcePets.get(1).slot != 1) {
                throw new IllegalStateException("Expected c=1 active switch success"
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " activeWarning=" + s.sourcePetSettingActiveWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " index=" + s.battleMenuIndex
                        + " lead=" + s.sourcePets.get(0).speciesId
                        + " expectedLead=" + selectedSpecies
                        + " row1=" + s.sourcePets.get(1).speciesId
                        + " expectedRow1=" + originalLeadSpecies
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "petsetting c=1 game.g.p move selected to front")) {
                throw new IllegalStateException("Expected c=1 game.g.p trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_active_dead_warning".equals(checkpoint)) {
            openPanelActiveSwitchForSmoke(s, 1, true);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourcePetSettingActiveWarningMode != 1
                    || s.text == null
                    || s.battleMenuIndex != 0
                    || !VqsvText.Battle.PET_CANNOT_BATTLE.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected c=1 dead pet warning"
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " activeWarning=" + s.sourcePetSettingActiveWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " index=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "!q.z[b].S() -> msgwarm.ui f=2")) {
                throw new IllegalStateException("Expected c=1 dead source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_active_already_warning".equals(checkpoint)) {
            openPanelActiveSwitchForSmoke(s, 0, false);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourcePetSettingActiveWarningMode != 2
                    || s.text == null
                    || s.battleMenuIndex != 0
                    || !VqsvText.Battle.PET_ALREADY_DEPLOYED.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected c=1 already active warning"
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " activeWarning=" + s.sourcePetSettingActiveWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " index=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "petsetting c=1 b==0 -> msgwarm.ui f=2")) {
                throw new IllegalStateException("Expected c=1 already source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_evolve_open".equals(checkpoint)) {
            openPanelEvolveFromPetSettingForSmoke(s, 12, 0);
            if (!s.sourceEvolveVisible || s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourceEvolveNotice == null
                    || s.sourceEvolveNotice.currentSpeciesId != 6
                    || s.sourceEvolveNotice.targetSpeciesId != 7
                    || s.sourceEvolveNotice.sourceR != 1) {
                throw new IllegalStateException("Expected c=5 direct evolve.ui open visible="
                        + s.sourceEvolveVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " notice=" + (s.sourceEvolveNotice == null ? "null"
                        : s.sourceEvolveNotice.currentSpeciesId + "->" + s.sourceEvolveNotice.targetSpeciesId
                        + " R=" + s.sourceEvolveNotice.sourceR)
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "petsetting c=5 o.m(); bg();")
                    || !traceContains(s, "game.h.bg evolve.ui open petIndex=0")) {
                throw new IllegalStateException("Expected c=5 bg source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_evolve_no_material_warning".equals(checkpoint)) {
            openPanelEvolveFromPetSettingForSmoke(s, 12, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.sourceEvolveVisible || s.text == null
                    || s.sourceEvolvePhase != 2
                    || s.sourcePets.get(0).speciesId != 6
                    || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 0
                    || !s.text.currentText().contains(VqsvText.Evolution.MATERIAL_MISSING_EVOLVE)) {
                throw new IllegalStateException("Expected c=5 no-material warning visible="
                        + s.sourceEvolveVisible
                        + " phase=" + s.sourceEvolvePhase
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " species=" + s.sourcePets.get(0).speciesId
                        + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                        + " trace=" + tailTrace(s, 72));
            }
            s.text.sourceTextOffset = 70;
            return;
        }
        if ("panel_petstate_petsetting_evolve_success_mutate".equals(checkpoint)) {
            openPanelEvolveFromPetSettingForSmoke(s, 12, 1);
            s.key0 = true;
            int guard = 0;
            while ((s.text == null || s.sourceEvolvePhase != 2) && guard++ < 260) {
                s.tick();
            }
            if (!s.sourceEvolveVisible || s.text == null
                    || s.sourcePets.get(0).speciesId != 7
                    || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 0
                    || !traceContains(s, "game.h.bh mutate pet index=0 species=6->7")) {
                throw new IllegalStateException("Expected c=5 success mutate visible="
                        + s.sourceEvolveVisible
                        + " phase=" + s.sourceEvolvePhase
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " species=" + s.sourcePets.get(0).speciesId
                        + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                        + " trace=" + tailTrace(s, 84));
            }
            s.text.sourceTextOffset = 70;
            return;
        }
        if ("panel_petstate_petsetting_release_confirm_open".equals(checkpoint)) {
            openPanelReleaseConfirmForSmoke(s);
            if (!s.sourceReleaseConfirmVisible || !s.worldPetstateVisible
                    || s.sourcePetSettingVisible || s.panelRuntime.visible
                    || s.sourceReleaseConfirmMessage.isEmpty()) {
                throw new IllegalStateException("Expected release msgconfirm.ui open release="
                        + s.sourceReleaseConfirmVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " panel=" + s.panelRuntime.visible
                        + " message=" + s.sourceReleaseConfirmMessage
                        + " trace=" + tailTrace(s, 48));
            }
            if (!traceContains(s, "petsetting c=3 -> msgconfirm.ui f=2")) {
                throw new IllegalStateException("Expected release msgconfirm source trace, trace="
                        + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_petstate_petsetting_release_cancel_returns_petstate".equals(checkpoint)) {
            openPanelReleaseConfirmForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (s.sourceReleaseConfirmVisible || !s.worldPetstateVisible
                    || s.sourcePetSettingVisible || s.panelRuntime.visible) {
                throw new IllegalStateException("Expected release cancel back to petstate release="
                        + s.sourceReleaseConfirmVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " panel=" + s.panelRuntime.visible
                        + " trace=" + tailTrace(s, 52));
            }
            if (!traceContains(s, "release msgconfirm key=786432 close msgconfirm.ui")) {
                throw new IllegalStateException("Expected release cancel source trace, trace="
                        + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_petstate_petsetting_release_success_removes_pet".equals(checkpoint)) {
            openPanelReleaseConfirmForSmoke(s);
            int beforePets = s.sourcePets.size();
            int removedSpecies = s.sourcePets.get(s.battleMenuIndex).speciesId;
            int survivorSpecies = s.sourcePets.get(1).speciesId;
            s.sourceEquipmentItems.clear();
            s.sourceEquipmentItems.add(new SourceEquipmentItem(0, true));
            s.sourcePets.get(s.battleMenuIndex).sourcePayload = s.sourcePets.get(s.battleMenuIndex).toSourcePayload();
            s.sourcePets.get(s.battleMenuIndex).sourcePayload[2] = 0;
            s.key0 = true;
            s.tick();
            if (s.sourceReleaseConfirmVisible || !s.worldPetstateVisible
                    || s.sourcePetSettingVisible || s.sourceReleaseWarningMode != 0
                    || s.sourcePets.size() != beforePets - 1
                    || s.sourcePets.get(0).speciesId != survivorSpecies
                    || s.sourceEquipmentItems.get(0).equippedFlag
                    || equipmentPayloadId(s.sourcePets.get(0)) == 0) {
                throw new IllegalStateException("Expected release success remove/clear equipment"
                        + " release=" + s.sourceReleaseConfirmVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " warning=" + s.sourceReleaseWarningMode
                        + " beforePets=" + beforePets
                        + " pets=" + s.sourcePets.size()
                        + " removedSpecies=" + removedSpecies
                        + " survivorExpected=" + survivorSpecies
                        + " slot0Species=" + (s.sourcePets.isEmpty() ? -1 : s.sourcePets.get(0).speciesId)
                        + " row0Flag=" + s.sourceEquipmentItems.get(0).equippedFlag
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "game.g.m remove selected species=" + removedSpecies)
                    || !traceContains(s, "game.g.l equipmentId=0")) {
                throw new IllegalStateException("Expected release success source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_release_last_alive_warning".equals(checkpoint)) {
            openPanelReleaseConfirmForSmokeWithSingleLivingPet(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (s.sourceReleaseConfirmVisible || !s.worldPetstateVisible
                    || s.sourceReleaseWarningMode != 1 || s.text == null
                    || s.sourcePets.size() != 1
                    || !VqsvText.Battle.RELEASE_LAST_ALIVE.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected last-alive release warning"
                        + " release=" + s.sourceReleaseConfirmVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " warning=" + s.sourceReleaseWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " pets=" + s.sourcePets.size()
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "q.o(selectedPet)=false -> msgwarm.ui f=3")) {
                throw new IllegalStateException("Expected last-alive source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_release_warning_returns_petstate".equals(checkpoint)) {
            openPanelReleaseConfirmForSmokeWithSingleLivingPet(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (s.sourceReleaseWarningMode != 0 || s.text != null
                    || s.sourceReleaseConfirmVisible || !s.worldPetstateVisible
                    || s.sourcePets.size() != 1 || s.sourcePetSettingVisible) {
                throw new IllegalStateException("Expected release warning close to petstate"
                        + " warning=" + s.sourceReleaseWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " release=" + s.sourceReleaseConfirmVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " pets=" + s.sourcePets.size()
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "release msgwarm key=131104 close msgwarm.ui")) {
                throw new IllegalStateException("Expected release warning close trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_release_protected_warning".equals(checkpoint)) {
            openPanelReleaseProtectedWarningForSmoke(s);
            forceReadyText(s);
            if (s.sourceReleaseConfirmVisible || s.sourcePetSettingVisible
                    || !s.worldPetstateVisible || s.sourceReleaseWarningMode != 2
                    || s.text == null
                    || !VqsvText.Battle.RELEASE_PROTECTED.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected protected release warning"
                        + " release=" + s.sourceReleaseConfirmVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " warning=" + s.sourceReleaseWarningMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "protected aq.c[0][species][22]==2 -> msgwarm.ui f=3")) {
                throw new IllegalStateException("Expected protected source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_open".equals(checkpoint)) {
            openPanelItemChoiceForSmoke(s);
            if (!s.sourceItemChoiceVisible || s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourceItemChoiceIndex != 0 || s.sourceItemChoiceSize() < 2) {
                throw new IllegalStateException("Expected item choice.ui open only choiceVisible="
                        + s.sourceItemChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " index=" + s.sourceItemChoiceIndex
                        + " rows=" + s.sourceItemChoiceSize()
                        + " trace=" + tailTrace(s, 48));
            }
            if (!traceContains(s, "petsetting c=0 -> choice.ui f=2 r=0")) {
                throw new IllegalStateException("Expected item choice.ui open source trace, trace="
                        + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_navigation".equals(checkpoint)) {
            openPanelItemChoiceForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (!s.sourceItemChoiceVisible || s.sourceItemChoiceIndex != 1) {
                throw new IllegalStateException("Expected item choice index 1 visible="
                        + s.sourceItemChoiceVisible
                        + " index=" + s.sourceItemChoiceIndex
                        + " rows=" + s.sourceItemChoiceSize()
                        + " trace=" + tailTrace(s, 48));
            }
            if (!traceContains(s, "choice.ui item key=8448 r=1")) {
                throw new IllegalStateException("Expected item choice navigation trace, trace="
                        + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_back_returns_petstate".equals(checkpoint)) {
            openPanelItemChoiceForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (s.sourceItemChoiceVisible || !s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.panelRuntime.visible) {
                throw new IllegalStateException("Expected item choice back to petstate choice="
                        + s.sourceItemChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " panel=" + s.panelRuntime.visible
                        + " trace=" + tailTrace(s, 52));
            }
            if (!traceContains(s, "choice.ui item key=262144 e(b) refresh petstate.ui close choice.ui")) {
                throw new IllegalStateException("Expected item choice back source trace, trace="
                        + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_confirm_pending".equals(checkpoint)
                || "panel_petstate_petsetting_item_choice_warning_hp_full".equals(checkpoint)) {
            openPanelItemChoiceForSmoke(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.sourceItemChoiceVisible || s.sourceItemChoiceMessageMode != 3
                    || s.text == null) {
                throw new IllegalStateException("Expected item choice HP-full warning visible="
                        + s.sourceItemChoiceVisible
                        + " msgMode=" + s.sourceItemChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 52));
            }
            if (!traceContains(s, "game.b.x itemId=4 validation=2")) {
                throw new IllegalStateException("Expected item choice validation trace, trace="
                        + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_warning_returns_choice".equals(checkpoint)) {
            openPanelItemChoiceForSmoke(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.sourceItemChoiceVisible || s.sourceItemChoiceMessageMode != 0
                    || s.text != null || s.worldPetstateVisible) {
                throw new IllegalStateException("Expected item warning close back to choice visible="
                        + s.sourceItemChoiceVisible
                        + " msgMode=" + s.sourceItemChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " worldPet=" + s.worldPetstateVisible
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "close msgwarm.ui f=3->2 return choice.ui")) {
                throw new IllegalStateException("Expected item warning close trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_success_msg".equals(checkpoint)) {
            int[] before = openPanelItemChoiceForSmoke(s, true);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            int afterHp = payloadHp(s.sourcePets.get(0));
            int remaining = VqsvSourceOps.sourceItemCount(s, 4);
            if (s.sourceItemChoiceVisible || !s.worldPetstateVisible
                    || s.sourceItemChoiceMessageMode != 4 || s.text == null
                    || afterHp <= before[0] || remaining != before[1] - 1) {
                throw new IllegalStateException("Expected item success msg hp/count beforeHp="
                        + before[0]
                        + " afterHp=" + afterHp
                        + " beforeCount=" + before[1]
                        + " remaining=" + remaining
                        + " choice=" + s.sourceItemChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " msgMode=" + s.sourceItemChoiceMessageMode
                        + " trace=" + tailTrace(s, 60));
            }
            if (!traceContains(s, "choice.ui item success game.b.w itemId=4 behavior=1")) {
                throw new IllegalStateException("Expected item success source trace, trace="
                        + tailTrace(s, 60));
            }
            return;
        }
        if ("panel_petstate_petsetting_item_choice_success_returns_petstate".equals(checkpoint)) {
            openPanelItemChoiceForSmoke(s, true);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (s.sourceItemChoiceVisible || !s.worldPetstateVisible
                    || s.sourceItemChoiceMessageMode != 0 || s.text != null) {
                throw new IllegalStateException("Expected item success close to petstate choice="
                        + s.sourceItemChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " msgMode=" + s.sourceItemChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 64));
            }
            if (!traceContains(s, "close msgwarm.ui f=4->0 stay petstate.ui")) {
                throw new IllegalStateException("Expected item success close trace, trace="
                        + tailTrace(s, 64));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_open".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s);
            if (!s.sourceEquipmentChoiceVisible || s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourceEquipmentChoiceIndex != 0 || s.sourceEquipmentChoiceSize() != 3) {
                throw new IllegalStateException("Expected equipment choice.ui open only equipVisible="
                        + s.sourceEquipmentChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " index=" + s.sourceEquipmentChoiceIndex
                        + " rows=" + s.sourceEquipmentChoiceSize()
                        + " trace=" + tailTrace(s, 52));
            }
            if (!traceContains(s, "petsetting c=2 -> choice.ui f=2 r=0")) {
                throw new IllegalStateException("Expected equipment choice.ui open source trace, trace="
                        + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_navigation".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (!s.sourceEquipmentChoiceVisible || s.sourceEquipmentChoiceIndex != 1) {
                throw new IllegalStateException("Expected equipment choice index 1 visible="
                        + s.sourceEquipmentChoiceVisible
                        + " index=" + s.sourceEquipmentChoiceIndex
                        + " rows=" + s.sourceEquipmentChoiceSize()
                        + " trace=" + tailTrace(s, 52));
            }
            if (!traceContains(s, "choice.ui equipment key=8448 h=1")) {
                throw new IllegalStateException("Expected equipment choice navigation trace, trace="
                        + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_statuses".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s);
            VqsvChoiceUiView choice = s.sourceEquipmentChoiceView();
            if (!"\u0110\u00e3 mang theo".equals(choice.valueAt(0))
                    || !"B\u1ecb mang theo".equals(choice.valueAt(1))
                    || !choice.valueAt(2).isEmpty()
                    || !"D\u1ee1 xu\u1ed1ng".equals(choice.widgetText(59, ""))
                    || choice.widgetVisible(5)
                    || !choice.widgetVisible(59)) {
                throw new IllegalStateException("Expected source q.L equipment statuses values=["
                        + choice.valueAt(0) + "," + choice.valueAt(1) + "," + choice.valueAt(2)
                        + "] softkey59=" + choice.widgetText(59, "")
                        + " visible5/59=" + choice.widgetVisible(5) + "/" + choice.widgetVisible(59)
                        + " trace=" + tailTrace(s, 52));
            }
            s.sourceStateTrace.add("SMOKE verified source q.L equipment statuses"
                    + " current=Da mang theo other=Bi mang theo empty=blank");
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_back_returns_petstate".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (s.sourceEquipmentChoiceVisible || !s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.panelRuntime.visible) {
                throw new IllegalStateException("Expected equipment choice back to petstate choice="
                        + s.sourceEquipmentChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " panel=" + s.panelRuntime.visible
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "choice.ui equipment key=262144 e(b) refresh petstate.ui close choice.ui")) {
                throw new IllegalStateException("Expected equipment choice back source trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_unequip_success_msg".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.sourceEquipmentChoiceVisible || s.sourceEquipmentChoiceMessageMode != 3
                    || s.text == null
                    || equipmentPayloadId(s.sourcePets.get(0)) != -1
                    || s.sourceEquipmentItems.get(0).equippedFlag) {
                throw new IllegalStateException("Expected equipment unequip success message choice="
                        + s.sourceEquipmentChoiceVisible
                        + " msgMode=" + s.sourceEquipmentChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " pet0Eq=" + equipmentPayloadId(s.sourcePets.get(0))
                        + " row0Flag=" + s.sourceEquipmentItems.get(0).equippedFlag
                        + " trace=" + tailTrace(s, 64));
            }
            if (!traceContains(s, "game.g.l itemId=0")) {
                throw new IllegalStateException("Expected equipment game.g.l trace, trace="
                        + tailTrace(s, 64));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_equip_success_msg".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s, 2);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.sourceEquipmentChoiceVisible || s.sourceEquipmentChoiceMessageMode != 3
                    || s.text == null
                    || equipmentPayloadId(s.sourcePets.get(0)) != 2
                    || s.sourceEquipmentItems.get(0).equippedFlag
                    || !s.sourceEquipmentItems.get(2).equippedFlag) {
                throw new IllegalStateException("Expected equipment equip success message choice="
                        + s.sourceEquipmentChoiceVisible
                        + " msgMode=" + s.sourceEquipmentChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " pet0Eq=" + equipmentPayloadId(s.sourcePets.get(0))
                        + " row0Flag=" + s.sourceEquipmentItems.get(0).equippedFlag
                        + " row2Flag=" + s.sourceEquipmentItems.get(2).equippedFlag
                        + " trace=" + tailTrace(s, 64));
            }
            if (!traceContains(s, "game.g.f itemId=2")) {
                throw new IllegalStateException("Expected equipment game.g.f trace, trace="
                        + tailTrace(s, 64));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_transfer_success_msg".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s, 1);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.sourceEquipmentChoiceVisible || s.sourceEquipmentChoiceMessageMode != 3
                    || s.text == null
                    || equipmentPayloadId(s.sourcePets.get(0)) != 1
                    || equipmentPayloadId(s.sourcePets.get(1)) != -1
                    || s.sourceEquipmentItems.get(0).equippedFlag
                    || !s.sourceEquipmentItems.get(1).equippedFlag) {
                throw new IllegalStateException("Expected equipment transfer success message choice="
                        + s.sourceEquipmentChoiceVisible
                        + " msgMode=" + s.sourceEquipmentChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " pet0Eq=" + equipmentPayloadId(s.sourcePets.get(0))
                        + " pet1Eq=" + equipmentPayloadId(s.sourcePets.get(1))
                        + " row0Flag=" + s.sourceEquipmentItems.get(0).equippedFlag
                        + " row1Flag=" + s.sourceEquipmentItems.get(1).equippedFlag
                        + " trace=" + tailTrace(s, 64));
            }
            if (!traceContains(s, "game.g.f itemId=1")
                    || !traceContains(s, "previousPet=1")) {
                throw new IllegalStateException("Expected equipment transfer trace, trace="
                        + tailTrace(s, 64));
            }
            return;
        }
        if ("panel_petstate_petsetting_equipment_choice_success_returns_petstate".equals(checkpoint)) {
            openPanelEquipmentChoiceForSmoke(s, 2);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (s.sourceEquipmentChoiceVisible || !s.worldPetstateVisible
                    || s.sourceEquipmentChoiceMessageMode != 0 || s.text != null
                    || equipmentPayloadId(s.sourcePets.get(0)) != 2) {
                throw new IllegalStateException("Expected equipment success close to petstate choice="
                        + s.sourceEquipmentChoiceVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " msgMode=" + s.sourceEquipmentChoiceMessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " pet0Eq=" + equipmentPayloadId(s.sourcePets.get(0))
                        + " trace=" + tailTrace(s, 68));
            }
            if (!traceContains(s, "close msgwarm.ui f=3->2 refresh petstate.ui close choice.ui")) {
                throw new IllegalStateException("Expected equipment f=3->2 close trace, trace="
                        + tailTrace(s, 68));
            }
            return;
        }
        if ("panel_petstate_petsetting_skill_open".equals(checkpoint)) {
            openPanelSkillForSmoke(s);
            if (!s.sourceSkillVisible || s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.sourceSkillIndex != 0 || s.sourceSkillCount != 5) {
                throw new IllegalStateException("Expected skill.ui open only skillVisible="
                        + s.sourceSkillVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " index=" + s.sourceSkillIndex
                        + " rows=" + s.sourceSkillCount
                        + " trace=" + tailTrace(s, 44));
            }
            if (!traceContains(s, "petsetting c=4 -> skill.ui f=2 r=0")) {
                throw new IllegalStateException("Expected skill.ui source open trace, trace="
                        + tailTrace(s, 44));
            }
            return;
        }
        if ("panel_petstate_petsetting_skill_navigation".equals(checkpoint)) {
            openPanelSkillForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (!s.sourceSkillVisible || s.sourceSkillIndex != 1) {
                throw new IllegalStateException("Expected skill.ui index 1 visible="
                        + s.sourceSkillVisible
                        + " index=" + s.sourceSkillIndex
                        + " trace=" + tailTrace(s, 44));
            }
            if (!traceContains(s, "skill.ui p.a.b(next) r=1")) {
                throw new IllegalStateException("Expected skill.ui navigation trace, trace="
                        + tailTrace(s, 44));
            }
            return;
        }
        if ("panel_petstate_petsetting_skill_mouse_wheel_non_scrollable_no_confirm".equals(checkpoint)) {
            openPanelSkillForSmoke(s);
            int initialSelected = s.sourceSkillIndex;
            s.mouseWheel(3);
            s.tick();
            if (!s.sourceSkillVisible
                    || s.sourceSkillIndex != initialSelected
                    || s.worldPetstateVisible
                    || s.sourcePetSettingVisible
                    || s.text != null
                    || traceContains(s, "PC_QOL mouse wheel skill.ui scrollbar")) {
                throw new IllegalStateException("Expected current source skill.ui to ignore mouse wheel"
                        + " because source pet skill slots are not scrollable"
                        + " skillVisible=" + s.sourceSkillVisible
                        + " index=" + s.sourceSkillIndex
                        + " initial=" + initialSelected
                        + " rows=" + s.sourceSkillCount
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 48));
            }
            s.hover(90 * VqsvIntroDemo.SCALE, 221 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.sourceSkillVisible || s.sourceSkillIndex != 2 || s.text != null) {
                throw new IllegalStateException("Expected skill.ui hover to preview row 2 without confirm"
                        + " skillVisible=" + s.sourceSkillVisible
                        + " index=" + s.sourceSkillIndex
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_petstate_petsetting_skill_back_returns_petstate".equals(checkpoint)) {
            openPanelSkillForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (s.sourceSkillVisible || !s.worldPetstateVisible || s.sourcePetSettingVisible
                    || s.panelRuntime.visible) {
                throw new IllegalStateException("Expected skill.ui back to petstate skill="
                        + s.sourceSkillVisible
                        + " worldPet=" + s.worldPetstateVisible
                        + " petsetting=" + s.sourcePetSettingVisible
                        + " panel=" + s.panelRuntime.visible
                        + " trace=" + tailTrace(s, 48));
            }
            if (!traceContains(s, "skill.ui key=262144 e(b) refresh petstate.ui close skill.ui")) {
                throw new IllegalStateException("Expected skill.ui back source trace, trace="
                        + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_petstate_petsetting_skill_confirm_pending".equals(checkpoint)) {
            openPanelSkillForSmoke(s);
            s.key0 = true;
            s.tick();
            if (!s.sourceSkillVisible || s.sourceSkillIndex != 0) {
                throw new IllegalStateException("Expected skill.ui confirm to stay visible="
                        + s.sourceSkillVisible
                        + " index=" + s.sourceSkillIndex
                        + " trace=" + tailTrace(s, 48));
            }
            if (!traceContains(s, "PENDING panel game.h.X skill.ui confirm")) {
                throw new IllegalStateException("Expected skill.ui confirm pending trace, trace="
                        + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_bag_open_from_gamemenu".equals(checkpoint)) {
            openPanelBagForSmoke(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected bag.ui from gamemenu visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "P=8 game.h.Y bag.ui open")) {
                throw new IllegalStateException("Expected bag source path trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_bag_item5_12_metadata_source_backed".equals(checkpoint)) {
            setupPanelSmokeWorld(s);
            s.sourceBagItems.clear();
            for (int itemId = 5; itemId <= 12; itemId++) {
                SourceItem source = VqsvSourceOps.sourceItem(itemId);
                BattleItemRow row = VqsvBattleTables.instance().item(itemId);
                if (row == null) {
                    throw new IllegalStateException("Missing source item row aq.c[4][" + itemId + "]");
                }
                if (source.textId != row.nameTextId
                        || source.iconCell != row.iconId
                        || source.descriptionTextId != row.descriptionTextId
                        || source.bagChannel != row.behavior
                        || source.name.startsWith("Item ")
                        || source.description.isEmpty()) {
                    throw new IllegalStateException("Expected item " + itemId
                            + " source-backed metadata source=["
                            + source.textId + "," + source.iconCell + ","
                            + source.descriptionTextId + "," + source.bagChannel
                            + "] name=" + source.name
                            + " descLen=" + source.description.length()
                            + " row=[" + row.nameTextId + "," + row.iconId + ","
                            + row.descriptionTextId + "," + row.behavior + "]");
                }
                s.sourceBagItems.put(itemId, new BagItem(itemId, 1, source.bagChannel, false));
            }
            s.sourceStateTrace.add("SMOKE seed source-backed bag item metadata ids=5..12");
            s.keyBack = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            s.keyDown = true;
            s.tick();
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected bag.ui item5..12 metadata render"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 32));
            }
            return;
        }
        if ("panel_bag_navigation".equals(checkpoint)) {
            openPanelBagForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1) {
                throw new IllegalStateException("Expected bag.ui selected=1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.ac key=8448 bagTab=0 selected=1")) {
                throw new IllegalStateException("Expected bag down navigation trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_bag_hover_preview_no_confirm".equals(checkpoint)) {
            openPanelBagForSmoke(s);
            s.hover(60 * VqsvIntroDemo.SCALE, 145 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1
                    || s.worldPetstateVisible
                    || s.text != null) {
                throw new IllegalStateException("Expected bag hover to select row 1 without confirm"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " worldPet=" + s.worldPetstateVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_bag_mouse_wheel_scrollbar_no_confirm".equals(checkpoint)) {
            openPanelScrollableBagForSmoke(s);
            int initialSelected = s.panelRuntime.selected;
            s.mouseWheel(3);
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != initialSelected
                    || s.worldPetstateVisible
                    || s.text != null
                    || !traceContains(s, "PC_QOL mouse wheel panel list scrollbar mode=BAG scroll=3 selected=0")) {
                throw new IllegalStateException("Expected bag mouse wheel to move scrollbar only"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " initial=" + initialSelected
                        + " worldPet=" + s.worldPetstateVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 36));
            }
            return;
        }
        if ("panel_bag_mouse_wheel_hover_click_viewport".equals(checkpoint)) {
            openPanelScrollableBagForSmoke(s);
            s.mouseWheel(3);
            s.tick();
            s.hover(60 * VqsvIntroDemo.SCALE, 145 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 4
                    || s.worldPetstateVisible
                    || s.text != null) {
                throw new IllegalStateException("Expected bag hover after wheel to select viewport row index 4"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " worldPet=" + s.worldPetstateVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 42));
            }
            s.click(60 * VqsvIntroDemo.SCALE, 145 * VqsvIntroDemo.SCALE);
            s.tick();
            if (s.panelRuntime.visible
                    || !s.worldPetstateVisible
                    || s.panelBagState17ItemId != 6
                    || !traceContains(s, "confirm itemId=6")) {
                throw new IllegalStateException("Expected bag click after wheel to confirm viewport itemId=6"
                        + " panel=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " selected=" + s.panelRuntime.selected
                        + " itemId=" + s.panelBagState17ItemId
                        + " trace=" + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_bag_back_returns_gamemenu".equals(checkpoint)) {
            openPanelBagForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 2) {
                throw new IllegalStateException("Expected bag back to gamemenu row 2 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "close bag.ui -> gamemenu.ui selected=2")) {
                throw new IllegalStateException("Expected bag back source trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_bag_default_item_state17_open_petstate".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            if (s.panelRuntime.visible || !s.worldPetstateVisible
                    || s.panelBagState17ItemId != 4
                    || s.battleMenuIndex != 0) {
                throw new IllegalStateException("Expected bag default item to open state17 petstate"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " selectedPet=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 40));
            }
            if (!traceContains(s, "default itemId=4 this.s=itemId o.a(17) close bag.ui -> game.h.W petstate.ui")) {
                throw new IllegalStateException("Expected state17 open source trace, trace="
                        + tailTrace(s, 40));
            }
            return;
        }
        if ("panel_bag_default_item_state17_navigation".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (s.panelRuntime.visible || !s.worldPetstateVisible
                    || s.panelBagState17ItemId != 4
                    || s.battleMenuIndex != 1) {
                throw new IllegalStateException("Expected state17 petstate navigation to row 1"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " selectedPet=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 44));
            }
            if (!traceContains(s, "game.h.Z key=8448 itemId=4 selectedPet=1")) {
                throw new IllegalStateException("Expected state17 navigation trace, trace="
                        + tailTrace(s, 44));
            }
            return;
        }
        if ("panel_bag_default_item_state17_back_returns_bag".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.worldPetstateVisible
                    || s.panelBagState17ItemId != -1
                    || s.panelRuntime.selected != 2) {
                throw new IllegalStateException("Expected state17 back to return to bag row"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 48));
            }
            if (!traceContains(s, "game.h.Z back itemId=4 o.a(8) close petstate.ui -> bag.ui")) {
                throw new IllegalStateException("Expected state17 back source trace, trace="
                        + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_bag_default_item_state17_warning_hp_full".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 4
                    || s.panelBagState17MessageMode != 1
                    || s.text == null
                    || !VqsvText.Battle.ITEM_HP_FULL.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected state17 HP-full warning"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "state17 msgwarm.ui f=1 reason=game.b.x itemId=4 validation=2")) {
                throw new IllegalStateException("Expected state17 HP-full validation trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_bag_default_item_state17_warning_dead".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.sourcePets.get(0).sourcePayload[6] = 0;
            s.openPanelBagState17Petstate(4);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 4
                    || s.panelBagState17MessageMode != 1
                    || s.text == null
                    || !VqsvText.Battle.ITEM_TARGET_DEAD.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected state17 dead-pet warning"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "state17 msgwarm.ui f=1 reason=game.b.x itemId=4 validation=8")) {
                throw new IllegalStateException("Expected state17 dead validation trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_bag_default_item_state17_warning_returns_petstate".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 4
                    || s.panelBagState17MessageMode != 0
                    || s.text != null
                    || s.battleMenuIndex != 0) {
                throw new IllegalStateException("Expected state17 warning close back to petstate"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " selectedPet=" + s.battleMenuIndex
                        + " trace=" + tailTrace(s, 60));
            }
            if (!traceContains(s, "state17 close msgwarm.ui f=1->0 stay petstate.ui")) {
                throw new IllegalStateException("Expected state17 warning close trace, trace="
                        + tailTrace(s, 60));
            }
            return;
        }
        if ("panel_bag_default_item_state17_success_msg".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.sourcePets.get(0).sourcePayload[6] = 10;
            s.openPanelBagState17Petstate(4);
            int beforeHp = payloadHp(s.sourcePets.get(0));
            int beforeCount = VqsvSourceOps.sourceItemCount(s, 4);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            int afterHp = payloadHp(s.sourcePets.get(0));
            int remaining = VqsvSourceOps.sourceItemCount(s, 4);
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 4
                    || s.panelBagState17MessageMode != 1
                    || s.text == null
                    || !VqsvText.Battle.ITEM_USED.equals(s.text.currentText())
                    || afterHp <= beforeHp
                    || remaining != beforeCount - 1) {
                throw new IllegalStateException("Expected state17 item success msg"
                        + " beforeHp=" + beforeHp
                        + " afterHp=" + afterHp
                        + " beforeCount=" + beforeCount
                        + " remaining=" + remaining
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "state17 success game.b.w itemId=4 behavior=1")) {
                throw new IllegalStateException("Expected state17 success trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_bag_default_item_state17_success_returns_petstate".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.sourcePets.get(0).sourcePayload[6] = 10;
            s.openPanelBagState17Petstate(4);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 4
                    || s.panelBagState17MessageMode != 0
                    || s.text != null
                    || s.battleMenuIndex != 0
                    || VqsvSourceOps.sourceItemCount(s, 4) != 4) {
                throw new IllegalStateException("Expected state17 success close back to petstate"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " selectedPet=" + s.battleMenuIndex
                        + " remaining=" + VqsvSourceOps.sourceItemCount(s, 4)
                        + " trace=" + tailTrace(s, 76));
            }
            if (!traceContains(s, "state17 close msgwarm.ui f=1->0 stay petstate.ui")) {
                throw new IllegalStateException("Expected state17 success close trace, trace="
                        + tailTrace(s, 76));
            }
            return;
        }
        if ("panel_bag_default_item_state17_missing_count_warning".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.sourcePets.get(0).sourcePayload[6] = 10;
            s.sourceBagItems.get(4).count = 0;
            s.openPanelBagState17Petstate(4);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 4
                    || s.panelBagState17MessageMode != 2
                    || s.text == null
                    || !VqsvText.Battle.NO_ITEM_COUNT.equals(s.text.currentText())) {
                throw new IllegalStateException("Expected state17 missing-count warning"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "state17 msgwarm.ui f=2 reason=q.b itemId=4 qty=1 false")) {
                throw new IllegalStateException("Expected state17 missing-count trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_bag_default_item_state17_missing_count_returns_bag".equals(checkpoint)) {
            openPanelBagState17ForSmoke(s);
            s.sourcePets.get(0).sourcePayload[6] = 10;
            s.sourceBagItems.get(4).count = 0;
            s.openPanelBagState17Petstate(4);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.worldPetstateVisible
                    || s.panelBagState17ItemId != -1
                    || s.panelBagState17MessageMode != 0
                    || s.text != null
                    || VqsvSourceOps.sourceItemCount(s, 4) != 0) {
                throw new IllegalStateException("Expected state17 missing-count close back to bag"
                        + " panelVisible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 4)
                        + " trace=" + tailTrace(s, 80));
            }
            if (!traceContains(s, "close msgwarm.ui+petstate.ui f=2->0 return state8/bag.ui")) {
                throw new IllegalStateException("Expected state17 missing-count close trace, trace="
                        + tailTrace(s, 80));
            }
            return;
        }
        if ("panel_bag_default_item_state17_revival_item11_success_msg".equals(checkpoint)) {
            openPanelBagSingleItemState17ForSmoke(s, 11, 2);
            s.sourcePets.get(0).sourcePayload[6] = 0;
            s.openPanelBagState17Petstate(11);
            int beforeHp = payloadHp(s.sourcePets.get(0));
            int beforeCount = VqsvSourceOps.sourceItemCount(s, 11);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            int afterHp = payloadHp(s.sourcePets.get(0));
            int remaining = VqsvSourceOps.sourceItemCount(s, 11);
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 11
                    || s.panelBagState17MessageMode != 1
                    || s.text == null
                    || !VqsvText.Battle.ITEM_USED.equals(s.text.currentText())
                    || beforeHp != 0
                    || afterHp <= 0
                    || remaining != beforeCount - 1) {
                throw new IllegalStateException("Expected state17 item11 revive success"
                        + " beforeHp=" + beforeHp
                        + " afterHp=" + afterHp
                        + " beforeCount=" + beforeCount
                        + " remaining=" + remaining
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "state17 success game.b.w itemId=11 behavior=4")) {
                throw new IllegalStateException("Expected state17 item11 revive trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_bag_default_item_state17_revival_item11_returns_petstate".equals(checkpoint)) {
            openPanelBagSingleItemState17ForSmoke(s, 11, 2);
            s.sourcePets.get(0).sourcePayload[6] = 0;
            s.openPanelBagState17Petstate(11);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.worldPetstateVisible || s.panelRuntime.visible
                    || s.panelBagState17ItemId != 11
                    || s.panelBagState17MessageMode != 0
                    || s.text != null
                    || payloadHp(s.sourcePets.get(0)) <= 0
                    || VqsvSourceOps.sourceItemCount(s, 11) != 1) {
                throw new IllegalStateException("Expected state17 item11 close back to petstate"
                        + " hp=" + payloadHp(s.sourcePets.get(0))
                        + " remaining=" + VqsvSourceOps.sourceItemCount(s, 11)
                        + " panelVisible=" + s.panelRuntime.visible
                        + " worldPet=" + s.worldPetstateVisible
                        + " itemId=" + s.panelBagState17ItemId
                        + " msgMode=" + s.panelBagState17MessageMode
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 76));
            }
            if (!traceContains(s, "state17 close msgwarm.ui f=1->0 stay petstate.ui")) {
                throw new IllegalStateException("Expected state17 item11 close trace, trace="
                        + tailTrace(s, 76));
            }
            return;
        }
        if ("panel_bag_item_cannot_use_warning".equals(checkpoint)) {
            openPanelBagForSmoke(s);
            int beforeCount = VqsvSourceOps.sourceItemCount(s, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_CANNOT_USE.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 0) != beforeCount) {
                throw new IllegalStateException("Expected bag item 0 cannot-use warning"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " before=" + beforeCount
                        + " after=" + VqsvSourceOps.sourceItemCount(s, 0)
                        + " trace=" + tailTrace(s, 44));
            }
            if (!traceContains(s, "bagTab=0 itemId=0 case 0..3 -> msgwarm.ui f=1")) {
                throw new IllegalStateException("Expected bag cannot-use source trace, trace="
                        + tailTrace(s, 44));
            }
            return;
        }
        if ("panel_bag_item_cannot_use_returns_bag".equals(checkpoint)) {
            openPanelBagForSmoke(s);
            int beforeCount = VqsvSourceOps.sourceItemCount(s, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text != null
                    || s.panelRuntime.selected != 0
                    || VqsvSourceOps.sourceItemCount(s, 0) != beforeCount) {
                throw new IllegalStateException("Expected bag cannot-use close returns to bag"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " before=" + beforeCount
                        + " after=" + VqsvSourceOps.sourceItemCount(s, 0)
                        + " trace=" + tailTrace(s, 52));
            }
            if (!traceContains(s, "bag msgwarm key=196640 close msgwarm.ui f=1->0")) {
                throw new IllegalStateException("Expected bag msgwarm close trace, trace="
                        + tailTrace(s, 52));
            }
            return;
        }
        if ("panel_bag_item13_success_msg".equals(checkpoint)) {
            openPanelBagItem13ForSmoke(s, 1);
            int duration = sourceItemParamA(13);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_AVOID_SUCCESS.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 13) != 0
                    || s.sourceAvoidMonsterTicks != duration
                    || s.sourceAvoidMonsterElapsed != 0) {
                throw new IllegalStateException("Expected bag item13 success warning and source state"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 13)
                        + " ticks=" + s.sourceAvoidMonsterTicks
                        + " duration=" + duration
                        + " elapsed=" + s.sourceAvoidMonsterElapsed
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "itemId=13 q.d(item,1,0)")
                    || !traceContains(s, "q.x=aq.c[4][13][6]=" + duration)) {
                throw new IllegalStateException("Expected item13 success source trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_bag_item13_success_returns_bag".equals(checkpoint)) {
            openPanelBagItem13ForSmoke(s, 1);
            int duration = sourceItemParamA(13);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text != null
                    || VqsvSourceOps.sourceItemCount(s, 13) != 0
                    || s.sourceAvoidMonsterTicks != duration) {
                throw new IllegalStateException("Expected item13 close to remain in bag"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 13)
                        + " ticks=" + s.sourceAvoidMonsterTicks
                        + " trace=" + tailTrace(s, 60));
            }
            if (!traceContains(s, "bag msgwarm key=196640 close msgwarm.ui f=1->0")) {
                throw new IllegalStateException("Expected item13 msgwarm close trace, trace="
                        + tailTrace(s, 60));
            }
            return;
        }
        if ("panel_bag_item13_already_warning".equals(checkpoint)) {
            openPanelBagItem13ForSmoke(s, 1);
            s.sourceAvoidMonsterTicks = 12;
            s.sourceAvoidMonsterElapsed = 3;
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_AVOID_ALREADY.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 13) != 1
                    || s.sourceAvoidMonsterTicks != 12
                    || s.sourceAvoidMonsterElapsed != 3) {
                throw new IllegalStateException("Expected item13 already-active warning without mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 13)
                        + " ticks=" + s.sourceAvoidMonsterTicks
                        + " elapsed=" + s.sourceAvoidMonsterElapsed
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "itemId=13 q.x=12 already-active")) {
                throw new IllegalStateException("Expected item13 already-active source trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_bag_item13_forbidden_warning".equals(checkpoint)) {
            openPanelBagItem13ForSmoke(s, 1);
            s.currentSceneId = 3;
            s.currentRoomIndex = 7;
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_AVOID_FORBIDDEN.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 13) != 1
                    || s.sourceAvoidMonsterTicks != 0
                    || s.sourceAvoidMonsterElapsed != 0) {
                throw new IllegalStateException("Expected item13 forbidden-room warning without mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " room=[" + s.currentSceneId + "," + s.currentRoomIndex + "]"
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 13)
                        + " ticks=" + s.sourceAvoidMonsterTicks
                        + " elapsed=" + s.sourceAvoidMonsterElapsed
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "source room game.k.a().f/g=3/7 forbidden")) {
                throw new IllegalStateException("Expected item13 forbidden source trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_bag_item14_no_egg_warning".equals(checkpoint)) {
            openPanelBagItem14ForSmoke(s, 1, false, 0, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_ACCEL_WARNING.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 14) != 1
                    || s.sourceEggActive
                    || s.sourceEggProgress != 0) {
                throw new IllegalStateException("Expected item14 no-egg warning without mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 14)
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " trace=" + tailTrace(s, 56));
            }
            if (!traceContains(s, "itemId=14 q.k(0)=false")) {
                throw new IllegalStateException("Expected item14 no-egg source trace, trace="
                        + tailTrace(s, 56));
            }
            return;
        }
        if ("panel_bag_item14_type0_success".equals(checkpoint)) {
            openPanelBagItem14ForSmoke(s, 1, true, 0, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_ACCEL_SUCCESS.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 14) != 0
                    || !s.sourceEggActive
                    || s.sourceEggType != 0
                    || s.sourceEggProgress != 10) {
                throw new IllegalStateException("Expected item14 type0 success set game.k.q=10"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 14)
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " trace=" + tailTrace(s, 60));
            }
            if (!traceContains(s, "itemId=14 q.k(0)=true q.I=0 game.k.q=10")
                    || !traceContains(s, "q.d(item,1,0) count=0")) {
                throw new IllegalStateException("Expected item14 type0 success source trace, trace="
                        + tailTrace(s, 60));
            }
            return;
        }
        if ("panel_bag_item14_type1_success".equals(checkpoint)) {
            openPanelBagItem14ForSmoke(s, 1, true, 1, 12);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_ACCEL_SUCCESS.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 14) != 0
                    || !s.sourceEggActive
                    || s.sourceEggType != 1
                    || s.sourceEggProgress != 30) {
                throw new IllegalStateException("Expected item14 type1 success set game.k.q=30"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 14)
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " trace=" + tailTrace(s, 60));
            }
            if (!traceContains(s, "itemId=14 q.k(0)=true q.I=1 game.k.q=30")
                    || !traceContains(s, "q.d(item,1,0) count=0")) {
                throw new IllegalStateException("Expected item14 type1 success source trace, trace="
                        + tailTrace(s, 60));
            }
            return;
        }
        if ("panel_bag_item14_already_ready_warning".equals(checkpoint)) {
            openPanelBagItem14ForSmoke(s, 1, true, 0, 10);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_ACCEL_WARNING.equals(s.text.currentText())
                    || VqsvSourceOps.sourceItemCount(s, 14) != 1
                    || !s.sourceEggActive
                    || s.sourceEggType != 0
                    || s.sourceEggProgress != 10) {
                throw new IllegalStateException("Expected item14 already-ready warning without mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 14)
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " trace=" + tailTrace(s, 60));
            }
            if (!traceContains(s, "itemId=14 q.k(0)=true q.I=0 game.k.q=10 target=10")) {
                throw new IllegalStateException("Expected item14 already-ready source trace, trace="
                        + tailTrace(s, 60));
            }
            return;
        }
        if ("panel_bag_item14_success_returns_bag".equals(checkpoint)) {
            openPanelBagItem14ForSmoke(s, 1, true, 0, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text != null
                    || VqsvSourceOps.sourceItemCount(s, 14) != 0
                    || !s.sourceEggActive
                    || s.sourceEggType != 0
                    || s.sourceEggProgress != 10) {
                throw new IllegalStateException("Expected item14 close to remain in bag"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 14)
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " trace=" + tailTrace(s, 64));
            }
            if (!traceContains(s, "bag msgwarm key=196640 close msgwarm.ui f=1->0")) {
                throw new IllegalStateException("Expected item14 msgwarm close trace, trace="
                        + tailTrace(s, 64));
            }
            return;
        }
        if ("panel_bag_egg_tab_ready_render".equals(checkpoint)) {
            openPanelBagEggHatchForSmoke(s, true, 0, 10, 1, 0);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0
                    || s.text != null) {
                throw new IllegalStateException("Expected bag special egg tab ready render"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 64));
            }
            if (!traceContains(s, "b=2->3 title=Dac thu")) {
                throw new IllegalStateException("Expected bag tab3 source trace, trace="
                        + tailTrace(s, 64));
            }
            return;
        }
        if ("panel_bag_egg_hatch_not_ready_warning".equals(checkpoint)) {
            openPanelBagEggHatchForSmoke(s, true, 0, 9, 1, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_HATCH_NOT_READY.equals(s.text.currentText())
                    || !s.sourceEggActive
                    || s.sourceEggProgress != 9
                    || s.sourceEggType != 0) {
                throw new IllegalStateException("Expected egg hatch not-ready warning without mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "q.N case0 q.k(0)=true game.k.r=false")) {
                throw new IllegalStateException("Expected egg not-ready source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_bag_egg_hatch_success_msg".equals(checkpoint)) {
            openPanelBagEggHatchForSmoke(s, true, 0, 10, 1, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_HATCH_SUCCESS.equals(s.text.currentText())
                    || s.sourceEggActive
                    || s.sourceEggProgress != 0
                    || s.sourceEggType != 1
                    || s.sourcePets.size() != 2
                    || s.sourcePets.get(1).speciesId != 58) {
                throw new IllegalStateException("Expected egg hatch success msg and party mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " pets=" + s.sourcePets.size()
                        + " trace=" + tailTrace(s, 84));
            }
            if (!traceContains(s, "q.N case0 game.k.r=true q.y()=0")
                    || !traceContains(s, "game.h.g hatch addPet species=58")) {
                throw new IllegalStateException("Expected egg hatch party source trace, trace="
                        + tailTrace(s, 84));
            }
            return;
        }
        if ("panel_bag_egg_hatch_result_to_bag".equals(checkpoint)) {
            openPanelBagEggHatchForSmoke(s, true, 0, 10, 1, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            String expected = VqsvText.Battle.panelBagEggHatchResult(sourceSpeciesName(58), 0);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !expected.equals(s.text.currentText())
                    || s.sourcePets.size() != 2
                    || s.sourcePetBank.size() != 0) {
                throw new IllegalStateException("Expected egg hatch openbox result to bag"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " expected=" + expected
                        + " pets=" + s.sourcePets.size()
                        + " bank=" + s.sourcePetBank.size()
                        + " trace=" + tailTrace(s, 92));
            }
            if (!traceContains(s, "close msgwarm.ui -> openbox.ui species=58 storageResult=0")) {
                throw new IllegalStateException("Expected egg hatch openbox transition trace, trace="
                        + tailTrace(s, 92));
            }
            return;
        }
        if ("panel_bag_egg_hatch_result_to_bank".equals(checkpoint)) {
            openPanelBagEggHatchForSmoke(s, true, 0, 10, 6, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            String expected = VqsvText.Battle.panelBagEggHatchResult(sourceSpeciesName(58), 1);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !expected.equals(s.text.currentText())
                    || s.sourcePets.size() != 6
                    || s.sourcePetBank.size() != 1
                    || s.sourcePetBank.get(0).speciesId != 58) {
                throw new IllegalStateException("Expected egg hatch openbox result to bank"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " expected=" + expected
                        + " pets=" + s.sourcePets.size()
                        + " bank=" + s.sourcePetBank.size()
                        + " trace=" + tailTrace(s, 92));
            }
            if (!traceContains(s, "q.N case0 game.k.r=true q.y()=1")
                    || !traceContains(s, "storageResult=1")) {
                throw new IllegalStateException("Expected egg hatch bank source trace, trace="
                        + tailTrace(s, 92));
            }
            return;
        }
        if ("panel_bag_egg_hatch_space_warning".equals(checkpoint)) {
            openPanelBagEggHatchForSmoke(s, true, 0, 10, 6, 100);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !VqsvText.Battle.PANEL_BAG_EGG_HATCH_SPACE_FULL.equals(s.text.currentText())
                    || !s.sourceEggActive
                    || s.sourceEggProgress != 10
                    || s.sourceEggType != 0
                    || s.sourcePets.size() != 6
                    || s.sourcePetBank.size() != 100) {
                throw new IllegalStateException("Expected egg hatch full-space warning without mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " eggActive=" + s.sourceEggActive
                        + " eggType=" + s.sourceEggType
                        + " eggProgress=" + s.sourceEggProgress
                        + " pets=" + s.sourcePets.size()
                        + " bank=" + s.sourcePetBank.size()
                        + " trace=" + tailTrace(s, 92));
            }
            if (!traceContains(s, "q.N case0 game.k.r=true q.y()=2")) {
                throw new IllegalStateException("Expected egg hatch full-space source trace, trace="
                        + tailTrace(s, 92));
            }
            return;
        }
        if ("panel_bag_special_reward5_render".equals(checkpoint)) {
            openPanelBagSpecialRewardForSmoke(s, 5, true, 0);
            s.keyDown = true;
            s.tick();
            if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1
                    || s.text != null) {
                throw new IllegalStateException("Expected special reward5 q.N row render"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 72));
            }
            if (!traceContains(s, "SMOKE seed source q.N special reward id=5")
                    || !traceContains(s, "b=2->3 title=Dac thu")) {
                throw new IllegalStateException("Expected special reward5 source trace, trace="
                        + tailTrace(s, 72));
            }
            return;
        }
        if ("panel_bag_special_reward5_confirm_pending".equals(checkpoint)) {
            openPanelBagSpecialRewardForSmoke(s, 5, true, 0);
            s.keyDown = true;
            s.tick();
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"RIDE".equals(s.panelRuntime.modeName())
                    || !"L\u1ee5c \u0111i \u0111i\u1ec3u".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected special reward5 confirm to open ride.ui"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 84));
            }
            if (!traceContains(s, "q.N case5 confirm -> o.a(11) game.h.ad ride.ui")
                    || !traceContains(s, "close bag.ui selectedRide=0")) {
                throw new IllegalStateException("Expected special reward5 ride.ui source trace, trace="
                        + tailTrace(s, 84));
            }
            return;
        }
        if ("panel_bag_special_reward5_ride_navigation".equals(checkpoint)) {
            openPanelBagSpecialRewardForSmoke(s, 5, true, 0);
            s.keyDown = true;
            s.tick();
            s.key0 = true;
            s.tick();
            s.keyRight = true;
            s.tick();
            if (!s.panelRuntime.visible || !"RIDE".equals(s.panelRuntime.modeName())
                    || !"H\u01b0 kh\u00f4ng h\u00e0nh gi\u1ea3".equals(s.panelRuntime.selectedLabel())) {
                throw new IllegalStateException("Expected ride.ui navigation to second ride"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " trace=" + tailTrace(s, 96));
            }
            if (!traceContains(s, "game.h.ae key=32832 ride.ui selected=1")) {
                throw new IllegalStateException("Expected ride.ui right navigation trace, trace="
                        + tailTrace(s, 96));
            }
            return;
        }
        if ("panel_bag_special_reward5_ride_back_returns_world".equals(checkpoint)) {
            openPanelBagSpecialRewardForSmoke(s, 5, true, 0);
            s.keyDown = true;
            s.tick();
            s.key0 = true;
            s.tick();
            s.keyBack = true;
            s.tick();
            if (s.panelRuntime.visible) {
                throw new IllegalStateException("Expected ride.ui back to close panel"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " trace=" + tailTrace(s, 96));
            }
            if (!traceContains(s, "game.h.ae key=262144 close ride.ui -> P=0")) {
                throw new IllegalStateException("Expected ride.ui back close trace, trace="
                        + tailTrace(s, 96));
            }
            return;
        }
        if ("panel_bag_ride_unlocked_slots".equals(checkpoint)) {
            openPanelRideForSmoke(s, true, -1);
            if (!s.panelRuntime.visible || !"RIDE".equals(s.panelRuntime.modeName())
                    || !"L\u1ee5c \u0111i \u0111i\u1ec3u".equals(s.panelRuntime.selectedLabel())
                    || s.text != null) {
                throw new IllegalStateException("Expected ride.ui with seeded q.N ids 1..4 unlocked"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " label=" + s.panelRuntime.selectedLabel()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 112));
            }
            if (!traceContains(s, "SMOKE seed source ride unlock q.N ids=1..4")
                    || !traceContains(s, "q.N case5 confirm -> o.a(11) game.h.ad ride.ui")) {
                throw new IllegalStateException("Expected ride unlocked seed/open trace, trace="
                        + tailTrace(s, 112));
            }
            return;
        }
        if ("panel_bag_ride_locked_warning_msgwarm".equals(checkpoint)) {
            openPanelRideForSmoke(s, false, -1);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"RIDE".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !"Ch\u01b0a c\u00f3 s\u1ee7ng v\u1eadt c\u01b0\u1ee1i n\u00e0y".equals(s.text.currentText())) {
                throw new IllegalStateException("Expected locked ride msgwarm"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 112));
            }
            if (!traceContains(s, "q.f(0)=false -> msgwarm.ui")) {
                throw new IllegalStateException("Expected locked ride warning trace, trace="
                        + tailTrace(s, 112));
            }
            return;
        }
        if ("panel_bag_ride_unusable_warning_msgwarm".equals(checkpoint)) {
            openPanelRideForSmoke(s, true, 0);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            if (!s.panelRuntime.visible || !"RIDE".equals(s.panelRuntime.modeName())
                    || s.text == null
                    || !"N\u01a1i n\u00e0y kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng s\u1ee7ng v\u1eadt c\u01b0\u1ee1i".equals(s.text.currentText())) {
                throw new IllegalStateException("Expected unusable ride msgwarm"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 112));
            }
            if (!traceContains(s, "q.f(0)=true q.g=false -> msgwarm.ui")) {
                throw new IllegalStateException("Expected unusable ride warning trace, trace="
                        + tailTrace(s, 112));
            }
            return;
        }
        if ("panel_bag_ride_warning_close_returns_ride".equals(checkpoint)) {
            openPanelRideForSmoke(s, false, -1);
            s.key0 = true;
            s.tick();
            forceReadyText(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"RIDE".equals(s.panelRuntime.modeName())
                    || s.text != null) {
                throw new IllegalStateException("Expected ride warning close to return ride.ui"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " text=" + (s.text == null ? "null" : s.text.currentText())
                        + " trace=" + tailTrace(s, 128));
            }
            if (!traceContains(s, "ride msgwarm key=196640 close msgwarm.ui")) {
                throw new IllegalStateException("Expected ride warning close trace, trace="
                        + tailTrace(s, 128));
            }
            return;
        }
        if ("panel_bag_ride_confirm_success_mutation".equals(checkpoint)) {
            openPanelRideForSmoke(s, true, -1);
            s.key0 = true;
            s.tick();
            if (s.panelRuntime.visible || s.sourceRideActiveIndex != 0
                    || s.sourcePlayerMoveSpeed != 8) {
                throw new IllegalStateException("Expected q.h ride success mutation"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " activeRide=" + s.sourceRideActiveIndex
                        + " speed=" + s.sourcePlayerMoveSpeed
                        + " trace=" + tailTrace(s, 128));
            }
            if (!traceContains(s, "q.h(0) activeRide=-1->0 d[0]=4->8")) {
                throw new IllegalStateException("Expected q.h success trace, trace="
                        + tailTrace(s, 128));
            }
            return;
        }
        if ("panel_bag_ride_confirm_speed_world_movement".equals(checkpoint)) {
            openPanelRideForSmoke(s, true, -1);
            s.key0 = true;
            s.tick();
            int beforeX = s.player.x;
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            if (s.player.x - beforeX != 8 || s.sourceRideActiveIndex != 0
                    || s.sourcePlayerMoveSpeed != 8) {
                throw new IllegalStateException("Expected ride speed 8 world movement"
                        + " beforeX=" + beforeX
                        + " afterX=" + s.player.x
                        + " activeRide=" + s.sourceRideActiveIndex
                        + " speed=" + s.sourcePlayerMoveSpeed
                        + " trace=" + tailTrace(s, 144));
            }
            return;
        }
        if ("panel_task_open_from_gamemenu".equals(checkpoint)) {
            openPanelTaskForSmoke(s);
            if (!s.panelRuntime.visible || !"TASK".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected task.ui from gamemenu visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "o.a(10) -> game.h.R task.ui open")) {
                throw new IllegalStateException("Expected task source path trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_task_tab_branch".equals(checkpoint)) {
            openPanelTaskForSmoke(s);
            s.keyRight = true;
            s.tick();
            if (!s.panelRuntime.visible || !"TASK".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected task branch tab visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "task tab branch b=1")) {
                throw new IllegalStateException("Expected task branch tab trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_task_navigation".equals(checkpoint)) {
            openPanelTaskForSmoke(s);
            s.op14CompleteEvent(1, 1, 0);
            s.keyDown = true;
            s.tick();
            if (!s.panelRuntime.visible || !"TASK".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1) {
                throw new IllegalStateException("Expected task selected=1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.S key=8448 taskTab=0 selected=1")) {
                throw new IllegalStateException("Expected task down navigation trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_task_hover_preview_no_confirm".equals(checkpoint)) {
            openPanelTaskForSmoke(s);
            s.op14CompleteEvent(1, 1, 0);
            s.hover(80 * VqsvIntroDemo.SCALE, 126 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || !"TASK".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1
                    || s.text != null) {
                throw new IllegalStateException("Expected task hover to select row 1 without confirm"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_task_mouse_wheel_non_scrollable_no_confirm".equals(checkpoint)) {
            openPanelTaskForSmoke(s);
            int initialSelected = s.panelRuntime.selected;
            s.mouseWheel(3);
            s.tick();
            if (!s.panelRuntime.visible || !"TASK".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != initialSelected
                    || s.text != null
                    || traceContains(s, "PC_QOL mouse wheel panel list scrollbar mode=TASK")) {
                throw new IllegalStateException("Expected current source task list to ignore mouse wheel"
                        + " because it has no scrollbar"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " initial=" + initialSelected
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 36));
            }
            s.sourceStateTrace.add("SMOKE verified task.ui mouse wheel ignored for non-scrollable source list");
            return;
        }
        if ("panel_task_back_returns_gamemenu".equals(checkpoint)) {
            openPanelTaskForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 4) {
                throw new IllegalStateException("Expected task back to gamemenu row 4 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "close task.ui -> P=6 gamemenu selected=4")) {
                throw new IllegalStateException("Expected task back source trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petmap_record_open_from_gamemenu".equals(checkpoint)) {
            openPanelRecordForSmoke(s);
            if (!s.panelRuntime.visible || !"RECORD".equals(s.panelRuntime.modeName())) {
                throw new IllegalStateException("Expected record.ui from gamemenu visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "o.a(9) -> game.h.N record.ui open")) {
                throw new IllegalStateException("Expected record source path trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_petmap_open_from_record".equals(checkpoint)) {
            openPanelPetmapForSmoke(s);
            if (!s.panelRuntime.visible || !"PETMAP".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 0) {
                throw new IllegalStateException("Expected petmap.ui from record visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.P petmap.ui open")) {
                throw new IllegalStateException("Expected petmap source path trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petmap_navigation".equals(checkpoint)) {
            openPanelPetmapForSmoke(s);
            s.keyDown = true;
            s.tick();
            if (!s.panelRuntime.visible || !"PETMAP".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 1) {
                throw new IllegalStateException("Expected petmap selected=1 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.Q key=8448 petmapTab=0 selected=1")) {
                throw new IllegalStateException("Expected petmap down navigation trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petmap_tab_navigation".equals(checkpoint)) {
            openPanelPetmapForSmoke(s);
            s.keyRight = true;
            s.tick();
            if (!s.panelRuntime.visible || !"PETMAP".equals(s.panelRuntime.modeName())) {
                throw new IllegalStateException("Expected petmap after tab visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "game.h.Q key=32832 petmapTab=1")) {
                throw new IllegalStateException("Expected petmap tab trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petmap_mouse_wheel_scrollbar_no_confirm".equals(checkpoint)) {
            openPanelPetmapForSmoke(s);
            int initialSelected = s.panelRuntime.selected;
            s.mouseWheel(4);
            s.tick();
            if (!s.panelRuntime.visible || !"PETMAP".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != initialSelected
                    || s.text != null
                    || !traceContains(s, "PC_QOL mouse wheel panel list scrollbar mode=PETMAP scroll=4 selected=0")) {
                throw new IllegalStateException("Expected petmap mouse wheel to move scrollbar only"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " initial=" + initialSelected
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 36));
            }
            return;
        }
        if ("panel_petmap_mouse_wheel_hover_click_viewport".equals(checkpoint)) {
            openPanelPetmapForSmoke(s);
            s.mouseWheel(4);
            s.tick();
            s.hover(80 * VqsvIntroDemo.SCALE, 126 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || !"PETMAP".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 5
                    || s.text != null) {
                throw new IllegalStateException("Expected petmap hover after wheel to select viewport row index 5"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 42));
            }
            s.click(80 * VqsvIntroDemo.SCALE, 126 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.panelRuntime.visible || !"PETMAP".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 5
                    || !traceContains(s, "PENDING panel game.h.Q confirm petmap entry")) {
                throw new IllegalStateException("Expected petmap click after wheel to confirm selected viewport row 5"
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 48));
            }
            return;
        }
        if ("world_petstate_mouse_wheel_scrollbar_no_confirm".equals(checkpoint)) {
            openWorldScrollablePetstateForSmoke(s);
            int initialSelected = s.battleMenuIndex;
            s.mouseWheel(3);
            s.tick();
            if (!s.worldPetstateVisible
                    || s.panelRuntime.visible
                    || s.battleMenuIndex != initialSelected
                    || s.battleMenuScroll != 3
                    || s.sourcePetSettingVisible
                    || s.text != null
                    || !traceContains(s, "PC_QOL mouse wheel world petstate scrollbar scroll=3 selectedPet=0 rows=9")) {
                throw new IllegalStateException("Expected world petstate mouse wheel to move viewport only"
                        + " worldPet=" + s.worldPetstateVisible
                        + " panel=" + s.panelRuntime.visible
                        + " selectedPet=" + s.battleMenuIndex
                        + " initial=" + initialSelected
                        + " scroll=" + s.battleMenuScroll
                        + " petSetting=" + s.sourcePetSettingVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 42));
            }
            return;
        }
        if ("world_petstate_mouse_wheel_hover_click_viewport".equals(checkpoint)) {
            openWorldScrollablePetstateForSmoke(s);
            s.mouseWheel(3);
            s.tick();
            s.hover(60 * VqsvIntroDemo.SCALE, 101 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.worldPetstateVisible
                    || s.panelRuntime.visible
                    || s.battleMenuScroll != 3
                    || s.battleMenuIndex != 4
                    || s.sourcePetSettingVisible
                    || s.text != null) {
                throw new IllegalStateException("Expected world petstate hover after wheel to select viewport row index 4"
                        + " worldPet=" + s.worldPetstateVisible
                        + " panel=" + s.panelRuntime.visible
                        + " selectedPet=" + s.battleMenuIndex
                        + " scroll=" + s.battleMenuScroll
                        + " petSetting=" + s.sourcePetSettingVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 44));
            }
            s.click(60 * VqsvIntroDemo.SCALE, 116 * VqsvIntroDemo.SCALE);
            s.tick();
            if (!s.worldPetstateVisible
                    || s.panelRuntime.visible
                    || s.battleMenuScroll != 3
                    || s.battleMenuIndex != 5
                    || !s.sourcePetSettingVisible
                    || s.text != null
                    || !traceContains(s, "petstate confirm -> petsetting.ui")) {
                throw new IllegalStateException("Expected world petstate click after wheel to confirm viewport row index 5"
                        + " worldPet=" + s.worldPetstateVisible
                        + " panel=" + s.panelRuntime.visible
                        + " selectedPet=" + s.battleMenuIndex
                        + " scroll=" + s.battleMenuScroll
                        + " petSetting=" + s.sourcePetSettingVisible
                        + " text=" + (s.text == null ? "none" : "present")
                        + " trace=" + tailTrace(s, 48));
            }
            return;
        }
        if ("panel_petmap_back_returns_record".equals(checkpoint)) {
            openPanelPetmapForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"RECORD".equals(s.panelRuntime.modeName())) {
                throw new IllegalStateException("Expected petmap back to record visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "close petmap.ui -> P=9 record.ui selected=0")) {
                throw new IllegalStateException("Expected petmap back trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_petmap_record_back_returns_gamemenu".equals(checkpoint)) {
            openPanelRecordForSmoke(s);
            s.keyBack = true;
            s.tick();
            if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                    || s.panelRuntime.selected != 3) {
                throw new IllegalStateException("Expected record back to gamemenu row 3 visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " selected=" + s.panelRuntime.selected
                        + " trace=" + tailTrace(s, 28));
            }
            if (!traceContains(s, "close record.ui -> gamemenu.ui selected=3")) {
                throw new IllegalStateException("Expected record back trace, trace="
                        + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_save_prompt_from_gamemenu".equals(checkpoint)) {
            openPanelSavePromptForSmoke(s);
            if (!s.panelRuntime.visible || !"SAVE".equals(s.panelRuntime.modeName())) {
                throw new IllegalStateException("Expected panel save prompt visible="
                        + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " trace=" + tailTrace(s, 24));
            }
            if (!traceContains(s, "P=22 game.h.H msgtip.ui open")) {
                throw new IllegalStateException("Expected save prompt source trace, trace="
                        + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_save_saving_status".equals(checkpoint)) {
            openPanelSavePromptForSmoke(s);
            s.key0 = true;
            s.tick();
            if (!s.panelRuntime.visible || !"SAVE".equals(s.panelRuntime.modeName())
                    || !traceContains(s, "game.h.K f=0 confirm")) {
                throw new IllegalStateException("Expected panel save saving status mode="
                        + s.panelRuntime.modeName()
                        + " visible=" + s.panelRuntime.visible
                        + " trace=" + tailTrace(s, 24));
            }
            return;
        }
        if ("panel_save_success_status".equals(checkpoint)) {
            openPanelSavePromptForSmoke(s);
            s.key0 = true;
            s.tick();
            s.tick();
            if (!VqsvSaveRuntime.hasSave() || !s.panelRuntime.visible
                    || !"SAVE".equals(s.panelRuntime.modeName())
                    || !traceContains(s, "game.k.k save success text=Luu thanh cong")) {
                throw new IllegalStateException("Expected panel save success hasSave="
                        + VqsvSaveRuntime.hasSave()
                        + " visible=" + s.panelRuntime.visible
                        + " mode=" + s.panelRuntime.modeName()
                        + " trace=" + tailTrace(s, 28));
            }
            return;
        }
        if ("panel_save_success_closes_world".equals(checkpoint)) {
            openPanelSavePromptForSmoke(s);
            s.key0 = true;
            s.tick();
            s.tick();
            s.tick();
            int beforeX = s.player.x;
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            if (!VqsvSaveRuntime.hasSave() || s.panelRuntime.visible || s.player.x <= beforeX) {
                throw new IllegalStateException("Expected panel save close and movement hasSave="
                        + VqsvSaveRuntime.hasSave()
                        + " visible=" + s.panelRuntime.visible
                        + " beforeX=" + beforeX
                        + " afterX=" + s.player.x
                        + " trace=" + tailTrace(s, 30));
            }
            if (!traceContains(s, "game.h.K f=2 close msgtip.ui and gamemenu.ui -> P=0")) {
                throw new IllegalStateException("Expected panel save close trace, trace="
                        + tailTrace(s, 30));
            }
            return;
        }
        throw new IllegalArgumentException("Unknown panel checkpoint: " + checkpoint);
    }

    private static void setupPanelSmokeWorld(VqsvIntroDemo.Scene s) {
        s.loadScene1Room0(199, 218);
        s.setPlayerPositionApprox(200, 218);
        s.eventIndex = s.events.size();
        s.current = new Op13FreeWorldTrigger(1, 0, 0, 999, 999, 10, 10);
        s.sourceStateTrace.add("SMOKE setup source panel free-world P=0 surrogate");
    }

    private static void openGameSystemPanelForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        s.keyBack = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 0) {
            throw new IllegalStateException("Could not open gamemenu before gamesystem mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 18));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openGameSystemHelpForSmoke(VqsvIntroDemo.Scene s) {
        openGameSystemPanelForSmoke(s);
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 1) {
            throw new IllegalStateException("Could not select gamesystem help row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openGameSystemSettingsForSmoke(VqsvIntroDemo.Scene s) {
        openGameSystemPanelForSmoke(s);
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select gamesystem settings row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 26));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openGameSystemOptionForSmoke(VqsvIntroDemo.Scene s) {
        openGameSystemPanelForSmoke(s);
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMESYSTEM".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 3) {
            throw new IllegalStateException("Could not select gamesystem main-menu row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 30));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelPetstateForSmoke(VqsvIntroDemo.Scene s) {
        s.eventIndex = s.events.size();
        seedInitialDienMieu(s, "smoke panel petstate");
        seedRoom0Group0BunnyRewards(s);
        s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
        tickBattleAutoUntilDone(s, 3000);
        SourcePetState bunny = findSourcePet(s, 34);
        if (bunny == null) {
            throw new IllegalStateException("Expected source-caught Bunny before panel petstate, trace="
                    + tailTrace(s, 24));
        }
        setupPanelSmokeWorld(s);
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 1) {
            throw new IllegalStateException("Could not select pet row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelItemChoiceForSmoke(VqsvIntroDemo.Scene s) {
        openPanelItemChoiceForSmoke(s, false);
    }

    private static void forceReadyText(VqsvIntroDemo.Scene s) {
        int guard = 0;
        while (s.text != null && !s.text.readyForKey && guard++ < 160) {
            s.tick();
        }
        if (s.text == null || !s.text.readyForKey) {
            throw new IllegalStateException("Expected ready text, trace=" + tailTrace(s, 40));
        }
    }

    private static void openPanelActiveSwitchForSmoke(VqsvIntroDemo.Scene s, int selectedPet, boolean deadSelectedPet) {
        openPanelPetstateForSmoke(s);
        if (selectedPet < 0 || selectedPet >= s.sourcePets.size()) {
            throw new IllegalStateException("Expected selected pet index for active switch smoke selected="
                    + selectedPet + " pets=" + s.sourcePets.size());
        }
        SourcePetState pet = s.sourcePets.get(selectedPet);
        pet.sourcePayload = pet.toSourcePayload();
        if (deadSelectedPet) {
            pet.sourcePayload[6] = 0;
        }
        s.battleMenuIndex = selectedPet;
        s.openWorldPetstate();
        s.key0 = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 1
                || s.battleMenuIndex != selectedPet) {
            throw new IllegalStateException("Could not select c=1 active row"
                    + " worldPet=" + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " petsettingIndex=" + s.sourcePetSettingIndex
                    + " selectedPet=" + s.battleMenuIndex
                    + " target=" + selectedPet
                    + " trace=" + tailTrace(s, 64));
        }
    }

    private static void openPanelEvolveFromPetSettingForSmoke(VqsvIntroDemo.Scene s,
                                                              int level, int materialCount) {
        setupPanelSmokeWorld(s);
        SourcePetState pet = new SourcePetState(0, 6, level, 3, 2, 0, -1);
        s.sourcePets.add(pet);
        SourceSpecialReward material = s.sourceSpecialRewards.computeIfAbsent(12, SourceSpecialReward::fromSourceDb);
        material.stackCount = materialCount;
        s.battleMenuIndex = 0;
        s.openWorldPetstate();
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 5; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingCount != 6
                || s.sourcePetSettingIndex != 5
                || !"Ti\u1ebfn h\u00f3a".equals(s.sourcePetSettingActionLabel(5))) {
            throw new IllegalStateException("Could not select petsetting c=5 evolve row"
                    + " worldPet=" + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " rows=" + s.sourcePetSettingCount
                    + " index=" + s.sourcePetSettingIndex
                    + " label=" + s.sourcePetSettingActionLabel(5)
                    + " trace=" + tailTrace(s, 72));
        }
        s.key0 = true;
        s.tick();
    }

    private static int[] openPanelItemChoiceForSmoke(VqsvIntroDemo.Scene s, boolean damageSelectedPet) {
        openPanelPetstateForSmoke(s);
        s.sourceBagItems.put(4, new BagItem(4, 5, 1, false));
        s.sourceBagItems.put(5, new BagItem(5, 2, 1, false));
        int beforeHp = -1;
        if (!s.sourcePets.isEmpty()) {
            SourcePetState pet = s.sourcePets.get(0);
            pet.sourcePayload = pet.toSourcePayload();
            if (damageSelectedPet) {
                beforeHp = Math.max(1, sourceMaxHp(pet) / 2);
                pet.sourcePayload[6] = beforeHp;
            } else {
                pet.sourcePayload[6] = sourceMaxHp(pet);
            }
        }
        s.key0 = true;
        s.tick();
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 0) {
            throw new IllegalStateException("Could not open petsetting item row worldPet="
                    + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " index=" + s.sourcePetSettingIndex
                    + " trace=" + tailTrace(s, 44));
        }
        s.key0 = true;
        s.tick();
        return new int[]{beforeHp, VqsvSourceOps.sourceItemCount(s, 4)};
    }

    private static void openPanelReleaseConfirmForSmoke(VqsvIntroDemo.Scene s) {
        openPanelPetstateForSmoke(s);
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 3; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 3) {
            throw new IllegalStateException("Could not select petsetting release row worldPet="
                    + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " index=" + s.sourcePetSettingIndex
                    + " trace=" + tailTrace(s, 48));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelReleaseConfirmForSmokeWithSingleLivingPet(VqsvIntroDemo.Scene s) {
        openPanelPetstateForSmoke(s);
        while (s.sourcePets.size() > 1) {
            s.sourcePets.remove(s.sourcePets.size() - 1);
        }
        if (s.sourcePets.isEmpty()) {
            throw new IllegalStateException("Expected at least one pet for single release smoke");
        }
        s.battleMenuIndex = 0;
        s.sourcePets.get(0).sourcePayload = s.sourcePets.get(0).toSourcePayload();
        s.sourcePets.get(0).sourcePayload[6] = sourceMaxHp(s.sourcePets.get(0));
        s.openWorldPetstate();
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 3; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 3 || s.sourcePets.size() != 1) {
            throw new IllegalStateException("Could not select release row for single-pet smoke"
                    + " worldPet=" + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " index=" + s.sourcePetSettingIndex
                    + " pets=" + s.sourcePets.size()
                    + " trace=" + tailTrace(s, 60));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelReleaseProtectedWarningForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        int protectedSpecies = firstProtectedReleaseSpeciesForSmoke();
        SourcePetState protectedPet = new SourcePetState(0, protectedSpecies, 30, 3, 2, 10, 45);
        SourcePetState reserve = new SourcePetState(1, 17, 7, 3, 2, 10, 45);
        s.sourcePets.add(protectedPet);
        s.sourcePets.add(reserve);
        s.battleMenuIndex = 0;
        s.openWorldPetstate();
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 3; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 3) {
            throw new IllegalStateException("Could not select release row for protected smoke"
                    + " species=" + protectedSpecies
                    + " worldPet=" + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " index=" + s.sourcePetSettingIndex
                    + " trace=" + tailTrace(s, 60));
        }
        s.key0 = true;
        s.tick();
    }

    private static int firstProtectedReleaseSpeciesForSmoke() {
        VqsvBattleTables tables = VqsvBattleTables.instance();
        for (int species = 0; species < tables.rowCount(0); species++) {
            short[] row = tables.row(0, species);
            if (row != null && row.length > 22 && row[22] == 2) {
                BattleSpeciesRow speciesRow = tables.species(species);
                if (speciesRow != null && speciesRow.validForBattle()) {
                    return species;
                }
            }
        }
        throw new IllegalStateException("No protected release species found in source aq.c[0][*][22]");
    }

    private static void openPanelEquipmentChoiceForSmoke(VqsvIntroDemo.Scene s) {
        openPanelEquipmentChoiceForSmoke(s, 0);
    }

    private static void openPanelEquipmentChoiceForSmoke(VqsvIntroDemo.Scene s, int selectedRow) {
        openPanelPetstateForSmoke(s);
        seedPanelEquipmentChoiceForSmoke(s);
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 2; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 2) {
            throw new IllegalStateException("Could not select petsetting equipment row worldPet="
                    + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " index=" + s.sourcePetSettingIndex
                    + " trace=" + tailTrace(s, 48));
        }
        s.key0 = true;
        s.tick();
        int guard = 0;
        while (s.sourceEquipmentChoiceVisible && s.sourceEquipmentChoiceIndex != selectedRow
                && guard++ < 8) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.sourceEquipmentChoiceVisible || s.sourceEquipmentChoiceIndex != selectedRow) {
            throw new IllegalStateException("Could not select equipment choice row target="
                    + selectedRow
                    + " visible=" + s.sourceEquipmentChoiceVisible
                    + " index=" + s.sourceEquipmentChoiceIndex
                    + " trace=" + tailTrace(s, 48));
        }
    }

    private static void seedPanelEquipmentChoiceForSmoke(VqsvIntroDemo.Scene s) {
        s.sourceEquipmentItems.clear();
        s.sourceEquipmentItems.add(new SourceEquipmentItem(0, true));
        s.sourceEquipmentItems.add(new SourceEquipmentItem(1, true));
        s.sourceEquipmentItems.add(new SourceEquipmentItem(2, false));
        if (!s.sourcePets.isEmpty()) {
            SourcePetState pet = s.sourcePets.get(0);
            pet.sourcePayload = pet.toSourcePayload();
            pet.sourcePayload[2] = 0;
        }
        if (s.sourcePets.size() > 1) {
            SourcePetState pet = s.sourcePets.get(1);
            pet.sourcePayload = pet.toSourcePayload();
            pet.sourcePayload[2] = 1;
        }
        s.sourceStateTrace.add("SMOKE seed source q.L equipment rows=[0 worn-by-selected,1 worn-by-other,2 free]"
                + " selectedPetEquipment=0");
    }

    private static int equipmentPayloadId(SourcePetState pet) {
        return pet != null && pet.sourcePayload != null && pet.sourcePayload.length > 2
                ? pet.sourcePayload[2] : -1;
    }

    private static void openPanelSkillForSmoke(VqsvIntroDemo.Scene s) {
        openPanelPetstateForSmoke(s);
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 4; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.worldPetstateVisible || !s.sourcePetSettingVisible
                || s.sourcePetSettingIndex != 4) {
            throw new IllegalStateException("Could not select petsetting skill row worldPet="
                    + s.worldPetstateVisible
                    + " petsetting=" + s.sourcePetSettingVisible
                    + " index=" + s.sourcePetSettingIndex
                    + " trace=" + tailTrace(s, 44));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelBagForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
        s.sourceBagItems.put(1, new BagItem(1, 2, 0, false));
        s.sourceBagItems.put(4, new BagItem(4, 5, 1, false));
        s.sourceStateTrace.add("SMOKE seed source bag q.K-like tab0 items=[0x1,1x2]"
                + " q.J-like non-tab0 item=[4x5]");
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelScrollableBagForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        int[] itemIds = {0, 1, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        for (int itemId : itemIds) {
            SourceItem item = VqsvSourceOps.sourceItem(itemId);
            s.sourceBagItems.put(itemId, new BagItem(itemId, 2, item.bagChannel, itemId == 0));
        }
        s.sourceStateTrace.add("SMOKE seed scrollable source bag q.K-like tab0 items="
                + java.util.Arrays.toString(itemIds));
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row for scroll wheel smoke mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
        if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 0) {
            throw new IllegalStateException("Could not open scrollable bag for wheel smoke"
                    + " visible=" + s.panelRuntime.visible
                    + " mode=" + s.panelRuntime.modeName()
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 32));
        }
    }

    private static void openPanelBagState17ForSmoke(VqsvIntroDemo.Scene s) {
        openPanelBagForSmoke(s);
        s.sourcePets.clear();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
        s.battleMenuIndex = 0;
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select default item row for state17"
                    + " visible=" + s.panelRuntime.visible
                    + " mode=" + s.panelRuntime.modeName()
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 32));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelBagSingleItemState17ForSmoke(VqsvIntroDemo.Scene s, int itemId, int count) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        SourceItem item = VqsvSourceOps.sourceItem(itemId);
        s.sourceBagItems.put(itemId, new BagItem(itemId, count, item.bagChannel, false));
        s.sourcePets.clear();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
        s.battleMenuIndex = 0;
        s.sourceStateTrace.add("SMOKE seed source bag single state17 itemId=" + itemId
                + " count=" + count + " name=" + item.name);
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row for single state17 item"
                    + " mode=" + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
        if (!s.panelRuntime.visible || !"BAG".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 0) {
            throw new IllegalStateException("Could not open bag single item for state17"
                    + " visible=" + s.panelRuntime.visible
                    + " mode=" + s.panelRuntime.modeName()
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 32));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelBagItem13ForSmoke(VqsvIntroDemo.Scene s, int count) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        s.sourceBagItems.put(13, new BagItem(13, count, 0, false));
        SourceItem item = VqsvSourceOps.sourceItem(13);
        if (item.textId <= 0 || item.iconCell < 0 || item.name.startsWith("Item ")) {
            throw new IllegalStateException("Item13 must be source-backed before smoke"
                    + " textId=" + item.textId
                    + " icon=" + item.iconCell
                    + " name=" + item.name);
        }
        s.sourceAvoidMonsterTicks = 0;
        s.sourceAvoidMonsterElapsed = 0;
        s.sourceStateTrace.add("SMOKE seed source bag q.K-like tab0 item13 count=" + count
                + " sourceItem=[" + item.textId + "," + item.iconCell + ","
                + item.descriptionTextId + "] name=" + item.name);
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row for item13 mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelBagItem14ForSmoke(VqsvIntroDemo.Scene s, int count,
                                                   boolean eggActive, int eggType, int eggProgress) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        SourceItem item = VqsvSourceOps.sourceItem(14);
        s.sourceBagItems.put(14, new BagItem(14, count, item.bagChannel, false));
        if (item.textId <= 0 || item.iconCell < 0 || item.name.startsWith("Item ")) {
            throw new IllegalStateException("Item14 must be source-backed before smoke"
                    + " textId=" + item.textId
                    + " icon=" + item.iconCell
                    + " name=" + item.name);
        }
        s.sourceEggActive = eggActive;
        s.sourceEggType = eggType;
        s.sourceEggProgress = eggProgress;
        s.sourceStateTrace.add("SMOKE seed source bag q.J-like tab0 item14 count=" + count
                + " sourceItem=[" + item.textId + "," + item.iconCell + ","
                + item.descriptionTextId + "," + item.bagChannel + "] name=" + item.name
                + " q.k(0)=" + eggActive
                + " q.I=" + eggType
                + " game.k.q=" + eggProgress);
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row for item14 mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelBagEggHatchForSmoke(VqsvIntroDemo.Scene s, boolean eggActive,
                                                     int eggType, int eggProgress,
                                                     int partyCount, int bankCount) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        s.sourcePets.clear();
        s.sourcePetBank.clear();
        for (int i = 0; i < s.sourceEggKnownSpecies.length; i++) {
            s.sourceEggKnownSpecies[i] = -1;
        }
        s.sourceEggActive = eggActive;
        s.sourceEggType = eggType;
        s.sourceEggProgress = eggProgress;
        for (int i = 0; i < eggType && i < s.sourceEggKnownSpecies.length; i++) {
            s.sourceEggKnownSpecies[i] = i == 0 ? 58 : EGG_HATCH_TEST_SPECIES(i);
        }
        for (int i = 0; i < partyCount; i++) {
            s.sourcePets.add(new SourcePetState(i, 17 + i, 7, 3, 2, 10, 45));
        }
        for (int i = 0; i < bankCount; i++) {
            s.sourcePetBank.add(new SourcePetState(i, 30 + i % 20, 7, 3, 2, 10, 45));
        }
        short[] row = VqsvBattleTables.instance().row(5, 0);
        if (row == null || row.length < 3) {
            throw new IllegalStateException("Missing source aq.c[5][0] egg special row");
        }
        s.sourceStateTrace.add("SMOKE seed source bag q.N tab3 row0"
                + " sourceRow=[" + row[0] + "," + row[1] + "," + row[2] + "]"
                + " q.k(0)=" + eggActive
                + " q.I=" + eggType
                + " game.k.q=" + eggProgress
                + " party=" + partyCount
                + " bank=" + bankCount);
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row for egg hatch mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 3; i++) {
            s.keyRight = true;
            s.tick();
        }
    }

    private static void openPanelBagSpecialRewardForSmoke(VqsvIntroDemo.Scene s, int rewardId,
                                                          boolean unlocked, int stackCount) {
        setupPanelSmokeWorld(s);
        s.sourceBagItems.clear();
        s.sourceSpecialRewards.clear();
        SourceSpecialReward reward = s.sourceSpecialRewards.computeIfAbsent(rewardId,
                SourceSpecialReward::fromSourceDb);
        if (unlocked) {
            reward.applySourceGameGSemantics(Math.max(1, stackCount));
        } else {
            reward.stackCount = stackCount;
        }
        if (reward.textId <= 0 || reward.iconId < 0 || reward.descriptionTextId <= 0) {
            throw new IllegalStateException("Special reward must be source-backed before smoke"
                    + " id=" + rewardId
                    + " row=[" + reward.textId + "," + reward.iconId + ","
                    + reward.descriptionTextId + "]");
        }
        s.sourceStateTrace.add("SMOKE seed source q.N special reward"
                + " id=" + rewardId
                + " unlocked=" + reward.unlocked
                + " stack=" + reward.stackCount
                + " row=[" + reward.textId + "," + reward.iconId + ","
                + reward.descriptionTextId + "]"
                + " path=" + reward.gameGPath);
        s.keyBack = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        s.keyDown = true;
        s.tick();
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 2) {
            throw new IllegalStateException("Could not select bag row for special reward smoke mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
        for (int i = 0; i < 3; i++) {
            s.keyRight = true;
            s.tick();
        }
    }

    private static void openPanelRideForSmoke(VqsvIntroDemo.Scene s, boolean unlockRides,
                                              int blockedRideIndex) {
        openPanelBagSpecialRewardForSmoke(s, 5, true, 0);
        for (int i = 0; i < s.sourceRideBlocked.length; i++) {
            s.sourceRideBlocked[i] = 0;
        }
        if (blockedRideIndex >= 0 && blockedRideIndex < s.sourceRideBlocked.length) {
            s.sourceRideBlocked[blockedRideIndex] = 1;
        }
        int downToCase5 = 1;
        if (unlockRides) {
            for (int id = 1; id <= 4; id++) {
                SourceSpecialReward reward = s.sourceSpecialRewards.computeIfAbsent(id,
                        SourceSpecialReward::fromSourceDb);
                reward.applySourceGameGSemantics(1);
            }
            downToCase5 = 5;
            s.sourceStateTrace.add("SMOKE seed source ride unlock q.N ids=1..4"
                    + " maps game.g.i(id) -> P[id-1]=1"
                    + " blockedRideIndex=" + blockedRideIndex);
        }
        for (int i = 0; i < downToCase5; i++) {
            s.keyDown = true;
            s.tick();
        }
        s.key0 = true;
        s.tick();
    }

    private static int EGG_HATCH_TEST_SPECIES(int index) {
        int[] species = {56, 58, 95, 72};
        return species[Math.max(0, Math.min(species.length - 1, index - 1))];
    }

    private static String sourceSpeciesName(int speciesId) {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(speciesId);
        return row == null ? "Pet " + speciesId : row.name("Pet " + speciesId);
    }

    private static int sourceItemParamA(int itemId) {
        BattleItemRow row = VqsvBattleTables.instance().item(itemId);
        if (row == null) {
            throw new IllegalStateException("Missing source aq.c[4][" + itemId + "] row");
        }
        return row.paramA;
    }

    private static void openPanelTaskForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        s.op14CompleteEvent(1, 0, 2);
        s.sourceStateTrace.add("SMOKE seed source task state main cursor from room0 group2");
        s.keyBack = true;
        s.tick();
        for (int i = 0; i < 4; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 4) {
            throw new IllegalStateException("Could not select task row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelRecordForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        seedInitialDienMieu(s, "smoke panel petmap record");
        s.sourcePets.add(new SourcePetState(1, 34, 5, 3, 2, 10, 45));
        s.sourceStateTrace.add("SMOKE seed source petmap owned species=[17,34]");
        s.keyBack = true;
        s.tick();
        for (int i = 0; i < 3; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 3) {
            throw new IllegalStateException("Could not select record row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    private static void openPanelPetmapForSmoke(VqsvIntroDemo.Scene s) {
        openPanelRecordForSmoke(s);
        s.key0 = true;
        s.tick();
    }

    private static void openWorldScrollablePetstateForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        s.sourcePets.clear();
        seedSourcePets(s, 9);
        if (!s.sourcePets.isEmpty()) {
            s.sourcePets.get(0).sourceD(true);
        }
        s.battleMenuIndex = 0;
        s.battleMenuScroll = 0;
        s.openWorldPetstate();
        s.sourceStateTrace.add("SMOKE seed world petstate scrollable source party rows="
                + s.sourcePets.size());
    }

    private static void openPanelSavePromptForSmoke(VqsvIntroDemo.Scene s) {
        setupPanelSmokeWorld(s);
        s.sourceMoney = 123;
        s.sourceBadges = 4;
        s.keyBack = true;
        s.tick();
        for (int i = 0; i < 5; i++) {
            s.keyDown = true;
            s.tick();
        }
        if (!s.panelRuntime.visible || !"GAMEMENU".equals(s.panelRuntime.modeName())
                || s.panelRuntime.selected != 5) {
            throw new IllegalStateException("Could not select save row mode="
                    + s.panelRuntime.modeName()
                    + " visible=" + s.panelRuntime.visible
                    + " selected=" + s.panelRuntime.selected
                    + " trace=" + tailTrace(s, 24));
        }
        s.key0 = true;
        s.tick();
    }

    static void runSmoke(String outPath, int ticks) {
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
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

    static void runSmokeDrive(String outPath, int preloadTicks, String route, int postTicks) {
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
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
                    + " textState=" + (s.text == null ? "none" : "present"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    static void runSmokeCheckpoint(String checkpoint, String outPath) {
        if (runBattleEntryNpcSmokeIfNeeded(checkpoint, outPath)) {
            return;
        }
        if (runBattleP15SmokeIfNeeded(checkpoint, outPath)) {
            return;
        }
        if (runBattleP9P24SmokeIfNeeded(checkpoint, outPath)) {
            return;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            if ("room0_group2_first_dialog".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.dialog(s.font, VqsvText.Scene1Room0Group2.NEIL,
                        VqsvText.Scene1Room0Group2.CAUGHT, 0);
                for (int i = 0; i < 60; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_long_dialog".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.dialog(s.font, VqsvText.Scene1Room0Group2.ELDER,
                        VqsvText.Scene1Room0Group2.ELDER_PET_OFFER,
                        1);
                for (int i = 0; i < 120; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_tasktip".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.taskTip(VqsvText.Scene1Room0Group2.TASK_PET_CHOICE);
                for (int i = 0; i < 80; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_openbox".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                s.text = TextBox.openBox(VqsvText.Common.ITEM_REWARD_PREFIX
                        + VqsvText.Common.SMOKE_SANDWICH_X10);
                for (int i = 0; i < 80; i++) {
                    s.text.tick(s.font);
                }
            } else if ("font_full_cutscene".equals(checkpoint)) {
                s.text = TextBox.full(30, 90, VqsvText.Scene0Intro.TEXT[0], true);
                for (int i = 0; i < 160; i++) {
                    s.text.tick(s.font);
                }
            } else if ("room0_pet_choice_ui".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(200, 192);
                VqsvSceneScriptSupport.setActive(s, new int[]{53, 54, 55}, new int[]{0, 0, 0});
                s.choice = ChoiceBox.optionUi(0, VqsvText.Scene1Room0Group3.YES_NO_OPTIONS);
            } else if ("room1_op13_bunny_trigger".equals(checkpoint)) {
                s.loadScene1Room1(370, 176);
                s.setPlayerPositionApprox(374, 180);
                s.op14CompleteEvent(1, 1, 1);
                Blocking trigger = VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13.wrap(
                        new Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32));
                if (!trigger.tick(s)) {
                    throw new IllegalStateException("op13 smoke trigger did not complete");
                }
                if (!traceContains(s, "WorldResumeDescriptor scene1 room1 after save -> op13 Bunny")) {
                    throw new IllegalStateException("Expected world resume descriptor trace, trace="
                            + tailTrace(s, 16));
                }
                s.text = TextBox.taskTip(VqsvText.Scene1Room0Group0.TASK_BUNNY);
                revealCheckpointText(s, 80);
            } else if ("room1_bunny_save_prompt".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                Blocking prompt = new VqsvRoom1Group1SavePromptWrapper();
                prompt.tick(s);
                if (!traceContains(s, "room1 group1 save wrapper op15")
                        || !traceContains(s, "room1 group1 save wrapper op46")) {
                    throw new IllegalStateException("Expected room1 group1 save wrapper trace, trace="
                            + tailTrace(s, 16));
                }
            } else if ("room1_bunny_save_success".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                Blocking prompt = new VqsvRoom1Group1SavePromptWrapper();
                prompt.tick(s);
                s.key0 = true;
                prompt.tick(s);
                if (!VqsvSaveRuntime.hasSave() || !VqsvText.Common.SAVE_SUCCESS.equals(s.savePromptStatus)) {
                    throw new IllegalStateException("Expected save success status hasSave="
                            + VqsvSaveRuntime.hasSave() + " status=" + s.savePromptStatus
                            + " trace=" + tailTrace(s, 12));
                }
                if (!s.sourceEventStateComplete(1, 1, 1)
                        || !traceContains(s, "save-before-game.k.k complete [1,1,1]")) {
                    throw new IllegalStateException("Expected room1 group1 source event complete before save"
                            + " state=" + s.sourceEventState(1, 1, 1)
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("room1_bunny_save_resume_state".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                VqsvSaveRuntime.save(s);
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(199, 218);
                s.eventIndex = 0;
                if (!VqsvSaveRuntime.loadInto(s)) {
                    throw new IllegalStateException("Expected save load success");
                }
                if (s.currentSceneId != 1 || s.currentRoomIndex != 1
                        || s.eventIndex != room1BunnyOp13EventIndex()) {
                    throw new IllegalStateException("Bad resume target scene=" + s.currentSceneId
                            + " room=" + s.currentRoomIndex + " eventIndex=" + s.eventIndex);
                }
                if (s.sourcePets.isEmpty() || VqsvSourceOps.sourceItemCount(s, 1) < 2
                        || VqsvSourceOps.sourceItemCount(s, 4) < 5) {
                    throw new IllegalStateException("Bad resume inventory pets=" + s.sourcePets.size()
                            + " ball=" + VqsvSourceOps.sourceItemCount(s, 1)
                            + " sandwich=" + VqsvSourceOps.sourceItemCount(s, 4));
                }
            } else if ("room1_bunny_continue_free_move".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                s.setPlayerPositionApprox(330, 180);
                s.op14CompleteEvent(1, 1, 1);
                VqsvSaveRuntime.save(s);
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(199, 218);
                s.eventIndex = 0;
                if (!VqsvSaveRuntime.loadInto(s)) {
                    throw new IllegalStateException("Expected room1 Bunny free-move save load success");
                }
                if (s.currentSceneId != 1 || s.currentRoomIndex != 1
                        || s.eventIndex != room1BunnyOp13EventIndex()) {
                    throw new IllegalStateException("Free-move resume target mismatch scene="
                            + s.currentSceneId + " room=" + s.currentRoomIndex
                            + " eventIndex=" + s.eventIndex);
                }
                if (s.text != null || s.choice != null || s.savePromptVisible || s.battleOverlayTicks > 0) {
                    throw new IllegalStateException("Free-move resume unexpectedly blocked text="
                            + (s.text == null ? "none" : s.text.text)
                            + " choice=" + (s.choice != null)
                            + " save=" + s.savePromptVisible
                            + " battleOverlay=" + s.battleOverlayTicks);
                }
                int beforeX = s.player.x;
                s.setMoveKey(KeyEvent.VK_RIGHT, true);
                s.tick();
                s.setMoveKey(KeyEvent.VK_RIGHT, false);
                if (s.player.x <= beforeX) {
                    throw new IllegalStateException("Free-move resume did not move right before="
                            + beforeX + " after=" + s.player.x
                            + " current=" + (s.current == null ? "null" : s.current.getClass().getSimpleName())
                            + " trace=" + tailTrace(s, 18));
                }
                if (!traceContains(s, "WorldResumeDescriptor scene1 room1 after save -> op13 Bunny")) {
                    throw new IllegalStateException("Expected world resume descriptor while free-moving, trace="
                            + tailTrace(s, 18));
                }
                s.sourceStateTrace.add("SMOKE verified room1 Bunny continue free movement beforeOp13"
                        + " x=" + beforeX + "->" + s.player.x);
            } else if ("room1_bunny_continue_op13_trigger".equals(checkpoint)) {
                setupOldInvalidRoom1BunnySave(s);
                s.loadScene1Room0(199, 218);
                s.setPlayerPositionApprox(199, 218);
                s.eventIndex = 0;
                if (!VqsvSaveRuntime.loadInto(s)) {
                    throw new IllegalStateException("Expected old invalid room1 Bunny save load success");
                }
                if (s.currentSceneId != 1 || s.currentRoomIndex != 1
                        || s.eventIndex != room1BunnyOp13EventIndex()) {
                    throw new IllegalStateException("Old-save repair target mismatch scene="
                            + s.currentSceneId + " room=" + s.currentRoomIndex
                            + " eventIndex=" + s.eventIndex
                            + " trace=" + tailTrace(s, 18));
                }
                if (!s.playerIntersectsSourceRect(370, 176, 80, 32)) {
                    throw new IllegalStateException("Old-save repair did not place player in op13 rect player=["
                            + s.player.x + "," + s.player.y + "] trace=" + tailTrace(s, 18));
                }
                if (!traceContains(s, "save load unstuck room1 Bunny checkpoint")) {
                    throw new IllegalStateException("Expected old-save unstuck repair trace, trace="
                            + tailTrace(s, 18));
                }
                s.tick();
                if (!traceContains(s, "op13 trigger scene=1 room=1 group=0")) {
                    throw new IllegalStateException("Expected op13 trigger after repaired continue, current="
                            + (s.current == null ? "null" : s.current.getClass().getSimpleName())
                            + " eventIndex=" + s.eventIndex
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified old room1 Bunny continue repair triggers op13"
                        + " eventIndex=" + s.eventIndex);
            } else if ("room1_bunny_after_save_immediate_free_move".equals(checkpoint)) {
                seedInitialDienMieu(s, "smoke immediate after room1 save");
                s.sourceBagItems.put(1, new BagItem(1, 2, 0, false));
                s.sourceBagItems.put(4, new BagItem(4, 5, 1, false));
                s.op39RefreshPets();
                s.op14CompleteEvent(1, 0, 0);
                s.prepareTransition(55, 279, 240, 320);
                s.op25SetGameFlag(1);
                s.markWorldTransition(1, 1, 37);
                s.loadScene1Room1(s.transitionCenterX, s.transitionCenterY);
                s.placePlayerAtTransitionActorApprox(37, 16);
                s.eventIndex = room1BunnyOp13EventIndex() - 1;
                int startX = s.player.x;
                int startY = s.player.y;
                for (int i = 0; i < 160; i++) {
                    if (s.savePromptVisible && i > 0) {
                        s.press0();
                    }
                    s.tick();
                    if (s.current != null
                            && s.current.getClass().getSimpleName().contains("WorldResumeTraceBlocking")) {
                        break;
                    }
                }
                if (s.savePromptVisible || s.text != null || s.choice != null) {
                    throw new IllegalStateException("Immediate after-save still blocked save="
                            + s.savePromptVisible
                            + " text=" + (s.text == null ? "none" : s.text.text)
                            + " choice=" + (s.choice != null)
                            + " current=" + (s.current == null ? "null" : s.current.getClass().getSimpleName())
                            + " eventIndex=" + s.eventIndex
                            + " trace=" + tailTrace(s, 24));
                }
                if (s.current == null) {
                    throw new IllegalStateException("Immediate after-save has no free-world blocker"
                            + " eventIndex=" + s.eventIndex
                            + " events=" + s.events.size()
                            + " player=[" + s.player.x + "," + s.player.y + "]"
                            + " trace=" + tailTrace(s, 24));
                }
                int beforeX = s.player.x;
                int beforeY = s.player.y;
                s.setMoveKey(KeyEvent.VK_DOWN, true);
                for (int i = 0; i < 12; i++) {
                    s.tick();
                }
                s.setMoveKey(KeyEvent.VK_DOWN, false);
                if (s.player.y <= beforeY) {
                    throw new IllegalStateException("Immediate after-save did not move down"
                            + " transitionStart=[" + startX + "," + startY + "]"
                            + " before=[" + beforeX + "," + beforeY + "]"
                            + " after=[" + s.player.x + "," + s.player.y + "]"
                            + " current=" + (s.current == null ? "null" : s.current.getClass().getSimpleName())
                            + " eventIndex=" + s.eventIndex
                            + " trace=" + tailTrace(s, 28));
                }
                s.sourceStateTrace.add("SMOKE verified immediate after-save room1 free movement"
                        + " start=[" + startX + "," + startY + "]"
                        + " y=" + beforeY + "->" + s.player.y
                        + " eventIndex=" + s.eventIndex);
            } else if ("room1_bunny_after_save_stranded_recover".equals(checkpoint)) {
                seedInitialDienMieu(s, "smoke stranded room1 save recovery");
                s.sourceBagItems.put(1, new BagItem(1, 2, 0, false));
                s.sourceBagItems.put(4, new BagItem(4, 5, 1, false));
                s.op39RefreshPets();
                s.loadScene1Room1(55, 279);
                s.placePlayerAtTransitionActorApprox(37, 16);
                s.op14CompleteEvent(1, 0, 0);
                s.op14CompleteEvent(1, 1, 1);
                s.eventIndex = room1BunnyOp13EventIndex() + 1;
                s.current = null;
                int beforeY = s.player.y;
                s.setMoveKey(KeyEvent.VK_DOWN, true);
                for (int i = 0; i < 12; i++) {
                    s.tick();
                }
                s.setMoveKey(KeyEvent.VK_DOWN, false);
                if (s.player.y <= beforeY) {
                    throw new IllegalStateException("Stranded room1 Bunny recovery did not move"
                            + " beforeY=" + beforeY
                            + " afterY=" + s.player.y
                            + " current=" + (s.current == null ? "null" : s.current.getClass().getSimpleName())
                            + " eventIndex=" + s.eventIndex
                            + " trace=" + tailTrace(s, 28));
                }
                if (!traceContains(s, "recovered stranded room1 Bunny op13 free-world blocker")) {
                    throw new IllegalStateException("Expected stranded op13 recovery trace, trace="
                            + tailTrace(s, 28));
                }
                s.sourceStateTrace.add("SMOKE verified stranded room1 Bunny op13 recovery"
                        + " y=" + beforeY + "->" + s.player.y);
            } else if (isPanelCheckpoint(checkpoint)) {
                handlePanelCheckpoint(s, checkpoint);
            } else if ("world_petstate_ui_source_party".equals(checkpoint)
                    || "world_petstate_ui_bunny_selected".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke world petstate");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickBattleAutoUntilDone(s, 3000);
                SourcePetState bunny = findSourcePet(s, 34);
                if (bunny == null || payloadHp(bunny) <= 0 || payloadHp(bunny) >= sourceMaxHp(bunny)) {
                    throw new IllegalStateException("Expected caught low-HP Bunny before world petstate, bunny="
                            + bunny + " trace=" + tailTrace(s, 16));
                }
                s.loadScene1Room1(370, 176);
                s.setPlayerPositionApprox(374, 180);
                s.worldUi.visible = true;
                s.openWorldPetstate();
                if ("world_petstate_ui_bunny_selected".equals(checkpoint)) {
                    s.battleMenuIndex = 1;
                }
                if (!s.worldPetstateVisible || s.battlePetStateRows.length < 2) {
                    throw new IllegalStateException("World petstate did not open rows="
                            + s.battlePetStateRows.length + " trace=" + tailTrace(s, 12));
                }
            } else if ("boot_title_continue_with_save".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                VqsvSaveRuntime.save(s);
                AssetPaths assets = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
                BootFlowState boot = new BootFlowState(assets);
                GameStateMachine states = new GameStateMachine();
                states.replace(boot);
                tickBoot(boot, states, 20, emptyBootInput());
                tickBoot(boot, states, 20, emptyBootInput());
                boot.tick(bootInput(KeyEvent.VK_RIGHT), states);
                if (!"TITLE_MENU".equals(boot.phaseName()) || !boot.saveAvailableForSmoke()
                        || !"Ch\u01a1i ti\u1ebfp".equals(boot.selectedMenuLabelForSmoke())) {
                    throw new IllegalStateException("Boot continue menu mismatch phase="
                            + boot.phaseName()
                            + " save=" + boot.saveAvailableForSmoke()
                            + " label=" + boot.selectedMenuLabelForSmoke());
                }
                BufferedImage menu = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D menuGraphics = menu.createGraphics();
                boot.render(menuGraphics);
                menuGraphics.dispose();
                ImageIO.write(menu, "png", new java.io.File(outPath));
                boot.tick(bootInput(KeyEvent.VK_SPACE), states);
                if (!"LegacyIntroDemoState".equals(states.currentStateNameForSmoke())) {
                    throw new IllegalStateException("Boot continue did not route to save loader state="
                            + states.currentStateNameForSmoke());
                }
                System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                        + " label=" + boot.selectedMenuLabelForSmoke()
                        + " routed=" + states.currentStateNameForSmoke());
                return;
            } else if ("boot_panel_option_confirm_yes_routes_title_preserves_save".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                VqsvSaveRuntime.save(s);
                if (!VqsvSaveRuntime.hasSave()) {
                    throw new IllegalStateException("Expected save before option reset smoke");
                }
                LegacyIntroDemoState legacy = new LegacyIntroDemoState(true);
                GameStateMachine states = new GameStateMachine();
                states.replace(legacy);
                VqsvIntroDemo.Scene legacyScene = (VqsvIntroDemo.Scene) legacy.sceneForSmoke();
                openGameSystemOptionForSmoke(legacyScene);
                legacyScene.keyUp = true;
                legacyScene.tick();
                if (!legacyScene.panelRuntime.visible
                        || !"OPTION_CONFIRM".equals(legacyScene.panelRuntime.modeName())
                        || legacyScene.panelRuntime.selected != 0) {
                    throw new IllegalStateException("Expected option confirm c=0 before reset visible="
                            + legacyScene.panelRuntime.visible
                            + " mode=" + legacyScene.panelRuntime.modeName()
                            + " selected=" + legacyScene.panelRuntime.selected
                            + " trace=" + tailTrace(legacyScene, 40));
                }
                legacyScene.press0();
                states.tick(emptyBootInput());
                if (!"BootFlowState".equals(states.currentStateNameForSmoke())
                        || !(states.currentStateForSmoke() instanceof BootFlowState)) {
                    throw new IllegalStateException("Option confirm yes did not route to BootFlowState state="
                            + states.currentStateNameForSmoke()
                            + " snapshot=" + legacy.sceneDebugSnapshotForSmoke()
                            + " trace=" + tailTrace(legacyScene, 48));
                }
                if (!VqsvSaveRuntime.hasSave()) {
                    throw new IllegalStateException("Option confirm yes must preserve rebuild save file");
                }
                BootFlowState boot = (BootFlowState) states.currentStateForSmoke();
                tickBoot(boot, states, 20, emptyBootInput());
                tickBoot(boot, states, 20, emptyBootInput());
                boot.tick(bootInput(KeyEvent.VK_RIGHT), states);
                if (!"TITLE_MENU".equals(boot.phaseName()) || !boot.saveAvailableForSmoke()
                        || !"Ch\u01a1i ti\u1ebfp".equals(boot.selectedMenuLabelForSmoke())) {
                    throw new IllegalStateException("Option confirm yes title/save mismatch phase="
                            + boot.phaseName()
                            + " save=" + boot.saveAvailableForSmoke()
                            + " label=" + boot.selectedMenuLabelForSmoke()
                            + " trace=" + tailTrace(legacyScene, 48));
                }
                if (!traceContains(legacyScene, "option.ui confirm c=0 reset game.i.a/b=0")) {
                    throw new IllegalStateException("Expected option confirm yes source trace, trace="
                            + tailTrace(legacyScene, 48));
                }
                BufferedImage menu = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D menuGraphics = menu.createGraphics();
                boot.render(menuGraphics);
                menuGraphics.dispose();
                ImageIO.write(menu, "png", new java.io.File(outPath));
                System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                        + " label=" + boot.selectedMenuLabelForSmoke()
                        + " save=" + boot.saveAvailableForSmoke()
                        + " routed=" + states.currentStateNameForSmoke());
                return;
            } else if ("legacy_room1_wasd_bridge_free_move".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                s.setPlayerPositionApprox(330, 180);
                s.op14CompleteEvent(1, 1, 1);
                VqsvSaveRuntime.save(s);
                LegacyIntroDemoState legacy = new LegacyIntroDemoState(true);
                GameStateMachine states = new GameStateMachine();
                states.replace(legacy);
                String before = legacy.sceneDebugSnapshotForSmoke();
                int beforeX = snapshotPlayerCoord(before, 0);
                HashSet<Integer> down = new HashSet<>();
                down.add(KeyEvent.VK_D);
                InputSnapshot holdD = new InputSnapshot(down, new HashSet<>());
                for (int i = 0; i < 8; i++) {
                    states.tick(holdD);
                }
                String after = legacy.sceneDebugSnapshotForSmoke();
                int afterX = snapshotPlayerCoord(after, 0);
                if (afterX <= beforeX) {
                    throw new IllegalStateException("Legacy WASD bridge did not move right before="
                            + before + " after=" + after);
                }
                BufferedImage moved = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D movedGraphics = moved.createGraphics();
                states.render(movedGraphics);
                movedGraphics.dispose();
                ImageIO.write(moved, "png", new java.io.File(outPath));
                System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                        + " x=" + beforeX + "->" + afterX
                        + " snapshot=" + after);
                return;
            } else if ("boot_new_game_skip_intro_prompt".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                VqsvSaveRuntime.save(s);
                AssetPaths assets = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
                BootFlowState boot = new BootFlowState(assets);
                GameStateMachine states = new GameStateMachine();
                states.replace(boot);
                tickBoot(boot, states, 20, emptyBootInput());
                tickBoot(boot, states, 20, emptyBootInput());
                boot.tick(bootInput(KeyEvent.VK_RIGHT), states);
                boot.tick(bootInput(KeyEvent.VK_DOWN), states);
                if (!"Ch\u01a1i m\u1edbi".equals(boot.selectedMenuLabelForSmoke())) {
                    throw new IllegalStateException("Boot new game selection mismatch label="
                            + boot.selectedMenuLabelForSmoke());
                }
                boot.tick(bootInput(KeyEvent.VK_SPACE), states);
                if (!"SKIP_INTRO_PROMPT".equals(boot.phaseName())) {
                    throw new IllegalStateException("Expected skip intro prompt phase="
                            + boot.phaseName());
                }
                BufferedImage prompt = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D promptGraphics = prompt.createGraphics();
                boot.render(promptGraphics);
                promptGraphics.dispose();
                ImageIO.write(prompt, "png", new java.io.File(outPath));
                System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                        + " phase=" + boot.phaseName()
                        + " label=" + boot.selectedMenuLabelForSmoke());
                return;
            } else if ("boot_new_game_skip_intro_yes".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                VqsvSaveRuntime.save(s);
                AssetPaths assets = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
                BootFlowState boot = new BootFlowState(assets);
                GameStateMachine states = new GameStateMachine();
                states.replace(boot);
                tickBoot(boot, states, 20, emptyBootInput());
                tickBoot(boot, states, 20, emptyBootInput());
                boot.tick(bootInput(KeyEvent.VK_RIGHT), states);
                boot.tick(bootInput(KeyEvent.VK_DOWN), states);
                boot.tick(bootInput(KeyEvent.VK_SPACE), states);
                if (!"SKIP_INTRO_PROMPT".equals(boot.phaseName())) {
                    throw new IllegalStateException("Expected skip intro prompt before yes phase="
                            + boot.phaseName());
                }
                boot.tick(bootInput(KeyEvent.VK_SPACE), states);
                if (!"LegacyIntroDemoState".equals(states.currentStateNameForSmoke())
                        || !(states.currentStateForSmoke() instanceof LegacyIntroDemoState)) {
                    throw new IllegalStateException("Skip intro yes did not route to legacy state="
                            + states.currentStateNameForSmoke());
                }
                LegacyIntroDemoState legacy = (LegacyIntroDemoState) states.currentStateForSmoke();
                if (legacy.sceneEventIndexForSmoke() != VqsvIntroDemo.Scene.tenYearsEventIndex) {
                    throw new IllegalStateException("Skip intro event index mismatch expected="
                            + VqsvIntroDemo.Scene.tenYearsEventIndex
                            + " actual=" + legacy.sceneEventIndexForSmoke());
                }
                for (int i = 0; i < 40; i++) {
                    states.tick(emptyBootInput());
                }
                String text = legacy.sceneTextForSmoke();
                if (text == null || !text.contains("M\u01b0\u1eddi n\u0103m sau")) {
                    throw new IllegalStateException("Skip intro did not open ten-years text=" + text);
                }
                BufferedImage skipped = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D skippedGraphics = skipped.createGraphics();
                states.render(skippedGraphics);
                skippedGraphics.dispose();
                ImageIO.write(skipped, "png", new java.io.File(outPath));
                System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                        + " routed=" + states.currentStateNameForSmoke()
                        + " eventIndex=" + legacy.sceneEventIndexForSmoke()
                        + " text=" + text);
                return;
            } else if ("boot_new_game_skip_intro_noise".equals(checkpoint)) {
                setupRoom1BunnySavePoint(s);
                VqsvSaveRuntime.save(s);
                AssetPaths assets = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
                BootFlowState boot = new BootFlowState(assets);
                GameStateMachine states = new GameStateMachine();
                states.replace(boot);
                tickBoot(boot, states, 20, emptyBootInput());
                tickBoot(boot, states, 20, emptyBootInput());
                boot.tick(bootInput(KeyEvent.VK_RIGHT), states);
                boot.tick(bootInput(KeyEvent.VK_DOWN), states);
                boot.tick(bootInput(KeyEvent.VK_SPACE), states);
                boot.tick(bootInput(KeyEvent.VK_SPACE), states);
                if (!"LegacyIntroDemoState".equals(states.currentStateNameForSmoke())
                        || !(states.currentStateForSmoke() instanceof LegacyIntroDemoState)) {
                    throw new IllegalStateException("Skip intro noise did not route to legacy state="
                            + states.currentStateNameForSmoke());
                }
                LegacyIntroDemoState legacy = (LegacyIntroDemoState) states.currentStateForSmoke();
                for (int i = 0; i < 40; i++) {
                    states.tick(emptyBootInput());
                }
                for (int i = 0; i < 20; i++) {
                    states.tick(bootInput(KeyEvent.VK_SPACE));
                }
                for (int i = 0; i < 45; i++) {
                    states.tick(emptyBootInput());
                }
                String text = legacy.sceneTextForSmoke();
                if (text == null || !text.contains("Ti\u1ebfng huy\u00ean n\u00e1o")) {
                    throw new IllegalStateException("Skip intro did not reach noise text=" + text);
                }
                int sceneId = legacy.sceneIntFieldForSmoke("currentSceneId");
                int roomId = legacy.sceneIntFieldForSmoke("currentRoomIndex");
                if (sceneId != 1 || roomId != 0) {
                    throw new IllegalStateException("Skip intro noise wrong room scene="
                            + sceneId + " room=" + roomId);
                }
                BufferedImage skipped = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D skippedGraphics = skipped.createGraphics();
                states.render(skippedGraphics);
                skippedGraphics.dispose();
                ImageIO.write(skipped, "png", new java.io.File(outPath));
                System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                        + " scene=" + sceneId + " room=" + roomId
                        + " text=" + text);
                return;
            } else if ("room1_bunny_op32_flash_transition".equals(checkpoint)) {
                s.loadScene1Room1(370, 176);
                s.setPlayerPositionApprox(374, 180);
                Blocking battle = VqsvBattleScripts.room1BunnyBattleCaptureRuntime(s);
                for (int i = 0; i < 13; i++) {
                    if (battle.tick(s)) {
                        throw new IllegalStateException("Battle transition completed before flash smoke");
                    }
                    s.effect.tick();
                }
                if (s.battleOverlayTicks > 0 || !traceContains(s, "sourceEffect=6")) {
                    throw new IllegalStateException("Expected source op32 flash before battle overlay battleTicks="
                            + s.battleOverlayTicks
                            + " trace=" + tailTrace(s, 12));
                }
                if (!traceContains(s, "BattleEventDescriptor scene1 room1 group0 Bunny")) {
                    throw new IllegalStateException("Expected Bunny battle descriptor trace, trace="
                            + tailTrace(s, 12));
                }
            } else if ("return_room0_transition".equals(checkpoint)) {
                s.loadScene1Room1(370, 176);
                placePlayerForActorMaskSmoke(s, 37, true, 3);
                s.op14CompleteEvent(1, 1, 0);
                Blocking trigger = VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_BUNNY_TO_ROOM0.wrap(
                        new ActorTransitionFreeWorldTrigger(1, 1, 37, 3, 1, 0, 30));
                if (!trigger.tick(s)) {
                    throw new IllegalStateException("return room0 smoke trigger did not complete");
                }
                if (!traceContains(s, "WorldResumeDescriptor scene1 room1 after Bunny group0 -> room0 transition")) {
                    throw new IllegalStateException("Expected Bunny return world resume descriptor trace, trace="
                            + tailTrace(s, 16));
                }
                if (s.currentSceneId != 1 || s.currentRoomIndex != 0) {
                    throw new IllegalStateException("return room0 target mismatch scene="
                            + s.currentSceneId + " room=" + s.currentRoomIndex);
                }
            } else if ("actor52_interaction_group2".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.op14CompleteEvent(1, 1, 0);
                placePlayerForActorInteractionSmoke(s, 52);
                s.key0 = true;
                Blocking trigger = new ActorInteractionFreeWorldTrigger(1, 0, 2, 1, 1, 0, 52);
                if (!trigger.tick(s)) {
                    throw new IllegalStateException("actor52 smoke trigger did not complete");
                }
                if (s.worldEventActor != 52) {
                    throw new IllegalStateException("actor52 smoke worldEventActor mismatch "
                            + s.worldEventActor);
                }
                s.text = TextBox.dialog(s.font, VqsvText.Scene1Room0Group2.NEIL,
                        VqsvText.Scene1Room0Group2.CAUGHT, 0);
                revealCheckpointText(s, 80);
            } else if ("post_group6_room2_entry_tip".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.op14CompleteEvent(1, 0, 6);
                placePlayerForActorMaskSmoke(s, 31, true, 0);
                Blocking freeWorld = VqsvWorldResumeDescriptor.SCENE1_ROOM0_AFTER_GROUP6_FREEWORLD.wrap(
                        new Room0PostGroup6FreeWorld());
                freeWorld.tick(s);
                if (!traceContains(s, "WorldResumeDescriptor scene1 room0 after group6 -> post-group6 free-world")) {
                    throw new IllegalStateException("Expected group6 free-world resume descriptor trace, trace="
                            + tailTrace(s, 16));
                }
                if (s.currentSceneId != 1 || s.currentRoomIndex != 2) {
                    throw new IllegalStateException("post group6 room2 target mismatch scene="
                            + s.currentSceneId + " room=" + s.currentRoomIndex);
                }
                freeWorld.tick(s);
                if (s.text == null) {
                    throw new IllegalStateException("post group6 room2 tip did not open");
                }
                revealCheckpointText(s, 80);
            } else if ("post_group6_room0_back_from_room2".equals(checkpoint)) {
                s.loadScene1Room2(120, 23);
                s.op14CompleteEvent(1, 0, 6);
                s.op14CompleteEvent(1, 2, 3);
                placePlayerForActorMaskSmoke(s, 2, true, 2);
                Blocking freeWorld = new Room0PostGroup6FreeWorld();
                freeWorld.tick(s);
                if (s.currentSceneId != 1 || s.currentRoomIndex != 0) {
                    throw new IllegalStateException("post group6 return room0 target mismatch scene="
                            + s.currentSceneId + " room=" + s.currentRoomIndex);
                }
            } else if ("battle_kidnapping".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Sophie kidnapping battle");
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                for (int i = 0; i < 50; i++) {
                    s.tick();
                }
                assertActiveSourcePet(s, 68, "Sophie battle initial Dien Mieu");
            } else if ("battle_kidnapping_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Sophie kidnapping result");
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                for (int i = 0; i < 80; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_capture".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny capture");
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                for (int i = 0; i < 140; i++) {
                    s.tick();
                }
            } else if ("battle_entry_enemy_cpos".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleEntryOffset(s, false, 160);
            } else if ("battle_entry_player_cpos".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleEntryOffset(s, true, 180);
            } else if ("battle_entry_both_landed".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 180);
                if (!s.battleGroundMarkersVisible || !s.battleActiveMarkerVisible
                        || !s.battleActiveMarkerPlayerSide) {
                    throw new IllegalStateException("Expected landed battle markers under both actors, ground="
                            + s.battleGroundMarkersVisible
                            + " active=" + s.battleActiveMarkerVisible
                            + " activePlayer=" + s.battleActiveMarkerPlayerSide);
                }
            } else if ("battle_entry_power_percent_ui".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.tick();
                int startPlayer = s.battlePlayerPowerPercent;
                int startEnemy = s.battleEnemyPowerPercent;
                tickUntilBattleEntryOffset(s, false, 160);
                int enemyMid = s.battleEnemyPowerPercent;
                tickUntilBattleEntryOffset(s, true, 180);
                int playerMid = s.battlePlayerPowerPercent;
                tickUntilBattleState(s, "P20", 180);
                int finalPlayer = s.battlePlayerPowerPercent;
                int finalEnemy = s.battleEnemyPowerPercent;
                if (!traceContains(s, "powerPercent=game.h.a(b,b,b,i,total) widgets58/59")
                        || startPlayer != 100
                        || !entryPercentTweened(startEnemy, enemyMid, finalEnemy)
                        || !entryPercentTweened(100, playerMid, finalPlayer)) {
                    throw new IllegalStateException("Expected P0 battle.ui widgets 58/59 percent tween, "
                            + "start=" + startPlayer + "/" + startEnemy
                            + " mid=" + playerMid + "/" + enemyMid
                            + " final=" + finalPlayer + "/" + finalEnemy
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_bunny_command_ui".equals(checkpoint)) {
                setupBunnyMapBackedBattleEntry(s);
                seedInitialDienMieu(s, "smoke Bunny command UI");
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                assertBattleSnapshot(s, "Bunny command UI");
                assertActiveSourcePet(s, 68, "Bunny command UI initial Dien Mieu");
                assertEnemyOwnedMarker(s, false, "Bunny command UI not yet owned");
            } else if ("battle_lab_manual_bunny_p21_idle_guard".equals(checkpoint)) {
                setupLiveCheckpoint(s, "battle_bunny_catch_p21");
                for (int i = 0; i < 20; i++) {
                    s.tick();
                }
                if (!"P21".equals(s.battleStateName)
                        || s.battleCatchItemId != -1
                        || s.battleClickX != -1
                        || s.battleClickY != -1
                        || s.key0 || s.keyBack || s.keyUp || s.keyDown || s.keyLeft || s.keyRight) {
                    throw new IllegalStateException("Manual Bunny P21 should idle without auto confirm"
                            + " state=" + s.battleStateName
                            + " catchItem=" + s.battleCatchItemId
                            + " click=" + s.battleClickX + "," + s.battleClickY
                            + " keys={0:" + s.key0 + ",B:" + s.keyBack
                            + ",U:" + s.keyUp + ",D:" + s.keyDown
                            + ",L:" + s.keyLeft + ",R:" + s.keyRight + "}");
                }
                s.sourceStateTrace.add("SMOKE verified battle lab manual Bunny P21 idles without auto input");
            } else if ("battle_bunny_owned_marker".equals(checkpoint)) {
                setupBunnyMapBackedBattleEntry(s);
                seedInitialDienMieu(s, "smoke Bunny owned marker");
                s.sourcePets.add(new SourcePetState(1, 34, 5, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                assertBattleSnapshot(s, "Bunny owned marker");
                assertEnemyOwnedMarker(s, true, "Bunny command UI already owned");
            } else if ("battle_bunny_p3_skill_list".equals(checkpoint)) {
                setupBunnyMapBackedBattleEntry(s);
                seedInitialDienMieu(s, "smoke Bunny P3 skill list");
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                assertBattleSnapshot(s, "Bunny P3 skill list");
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
            } else if ("battle_bunny_capture_result".equals(checkpoint)) {
                setupBunnyMapBackedBattleEntry(s);
                seedInitialDienMieu(s, "smoke Bunny capture result");
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                for (int i = 0; i < 190; i++) {
                    s.tick();
                }
                assertBattleSnapshot(s, "Bunny capture result");
            } else if ("battle_elder".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                for (int i = 0; i < 50; i++) {
                    s.tick();
                }
            } else if ("battle_elder_command_ui".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
            } else if ("battle_hud_battle_ui_source_bars".equals(checkpoint)) {
                assertBattleUiBinaryLayout("battle HUD");
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battlePlayerEnergy = Math.max(1, s.battlePlayerMaxEnergy);
                s.sourceStateTrace.add("SMOKE renderer-only battle.ui energy #P fill stress"
                        + " energy=" + s.battlePlayerEnergy + "/" + s.battlePlayerMaxEnergy);
                assertRenderedColorPixels(s, "battle.ui enemy hp #P layer", 6, 17, 80, 6, 0xff6718, 80);
                assertRenderedColorPixels(s, "battle.ui player hp #P layer", 154, 253, 80, 6, 0xff6718, 80);
                assertRenderedColorPixels(s, "battle.ui player energy #P layer", 30, 262, 198, 6, 0x00ffb4, 120);
            } else if ("battle_background_game_d_c_snapshot".equals(checkpoint)) {
                s.loadScene1Room1(370, 176);
                s.eventIndex = s.events.size();
                s.setPlayerPositionApprox(374, 180);
                seedInitialDienMieu(s, "smoke battle background game.d.c snapshot");
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                if (s.battleBackgroundSnapshot == null) {
                    throw new IllegalStateException("battle background snapshot missing for map-backed battle"
                            + " trace=" + tailTrace(s, 16));
                }
                assertRenderedVisiblePixels(s, "game.d.c captured world snapshot",
                        0, 88, 80, 72, 500);
                assertRenderedDarkerThanSnapshot(s, "game.d.c battle darkened snapshot",
                        0, 88, 80, 72, 18);
            } else if ("battle_background_room0_village_snapshot".equals(checkpoint)) {
                s.loadScene1Room0(199, 218);
                s.eventIndex = s.events.size();
                s.setPlayerPositionApprox(199, 218);
                seedInitialDienMieu(s, "smoke battle background room0 village snapshot");
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                if (s.battleBackgroundSnapshot == null) {
                    throw new IllegalStateException("battle background snapshot missing for room0 village battle"
                            + " trace=" + tailTrace(s, 16));
                }
                assertRenderedVisiblePixels(s, "game.d.c room0 village snapshot",
                        0, 88, 80, 72, 500);
                assertRenderedDarkerThanSnapshot(s, "game.d.c room0 village darkened snapshot",
                        0, 88, 80, 72, 18);
            } else if ("battle_elder_p3_skill_list".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
            } else if ("battle_elder_p6_target_select".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{1, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 18 && !"P6".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P6", 80);
            } else if ("battle_elder_p6_confirm_to_p7".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{1, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 18 && !"P6".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P6", 80);
                for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P7", 80);
            } else if ("battle_elder_p7_anim_start".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_elder_p7_lunge_peak".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 4; i++) {
                    s.tick();
                }
            } else if ("battle_elder_p7_actor_u21_start".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 12 && !s.battleP7ActorEffectVisible; i++) {
                    s.tick();
                }
                if (!s.battleP7ActorEffectVisible
                        || s.battleP7ActorEffectSpriteId != 263
                        || s.battleP7ActorEffectState != 1
                        || s.battleP7ActorEffectOnPlayerSide) {
                    throw new IllegalStateException("Expected skill10 target AH u actor action type21, visible="
                            + s.battleP7ActorEffectVisible
                            + " sprite=" + s.battleP7ActorEffectSpriteId
                            + " state=" + s.battleP7ActorEffectState
                            + " playerSide=" + s.battleP7ActorEffectOnPlayerSide);
                }
            } else if ("battle_elder_p7_base_state1_local_cursor".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 1, 80);
                boolean side = s.battleP7AttackerPlayerSide;
                int state = side ? s.battleP7BaseStatePlayerSide : s.battleP7BaseStateEnemySide;
                int cursor = side ? s.battleP7BaseCursorPlayerSide : s.battleP7BaseCursorEnemySide;
                if (state != 1 || cursor != 1) {
                    throw new IllegalStateException("Expected P7 attacker base state1 local cursor after phase entry, side="
                            + (side ? "player" : "enemy") + " state=" + state + " cursor=" + cursor);
                }
            } else if ("battle_elder_p7_state1_attack_source_compare".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 1, 80);
                assertP7SourceSpriteFrame(s, "Elder P7 state1 attack", true, 1,
                        s.battleP7BaseCursorPlayerSide, s.battlePlayerVisualId, 0);
            } else if ("battle_elder_p7_state2_hit_source_compare".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                assertP7SourceSpriteFrame(s, "Elder P7 state2 hit", false,
                        s.battleP7BaseStateEnemySide, s.battleP7BaseCursorEnemySide,
                        s.battleEnemyVisualId, 24);
            } else if ("battle_elder_p7_actor_u21_trigger_hit".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                if (s.battleP7BaseStateEnemySide != 2 && s.battleP7BaseStateEnemySide != 3) {
                    throw new IllegalStateException("Expected target hit/dead base state after skill10 u action, state="
                            + s.battleP7BaseStateEnemySide);
                }
                if (s.battleP7BaseStateEnemySide == 2 && s.battleP7BaseCursorEnemySide != 0) {
                    throw new IllegalStateException("Expected target hit state2 cursor reset, cursor="
                            + s.battleP7BaseCursorEnemySide);
                }
            } else if ("battle_elder_p7_actor_u21_recover".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                for (int i = 0; i < 8; i++) {
                    s.tick();
                }
                if (s.battleP7BaseStateEnemySide != 0 && s.battleP7BaseStateEnemySide != 3) {
                    throw new IllegalStateException("Expected target recover/dead state after skill10 damage, state="
                            + s.battleP7BaseStateEnemySide);
                }
            } else if ("battle_elder_p7_damage_frame".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
            } else if ("battle_elder_p7_damage_hp_delay".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                int hpBefore = s.battleEnemyHp;
                if (hpBefore != s.battleEnemyMaxHp || !s.battleP7DamageVisible) {
                    throw new IllegalStateException("Expected P7 damage phase to keep old enemy HUD HP initially, hp="
                            + hpBefore + "/" + s.battleEnemyMaxHp
                            + " damageVisible=" + s.battleP7DamageVisible
                            + " trace=" + tailTrace(s, 16));
                }
                for (int i = 0; i < 3; i++) {
                    s.tick();
                    if (s.battleEnemyHp != hpBefore) {
                        throw new IllegalStateException("Expected source game.h.b G<4 HP delay, tick=" + i
                                + " hp=" + s.battleEnemyHp + " initial=" + hpBefore
                                + " trace=" + tailTrace(s, 16));
                    }
                }
            } else if ("battle_elder_p7_damage_hp_tween_step".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                int hpBefore = s.battleEnemyHp;
                for (int i = 0; i < 4; i++) {
                    s.tick();
                }
                if (s.battleEnemyHp >= hpBefore || !s.battleP7DamageVisible) {
                    throw new IllegalStateException("Expected source game.h.b HP tween to start after delay, hpBefore="
                            + hpBefore + " hpNow=" + s.battleEnemyHp
                            + " damageVisible=" + s.battleP7DamageVisible
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_elder_p7_damage_text_lifecycle".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                int guard = 0;
                while ("P7".equals(s.battleStateName)
                        && s.battleP7Phase == 2
                        && s.battleP7DamageVisible
                        && guard++ < 120) {
                    s.tick();
                }
                if (guard >= 120 || s.battleP7DamageVisible) {
                    throw new IllegalStateException("Expected blood.mid damage text lifecycle to complete, state="
                            + s.battleStateName + " phase=" + s.battleP7Phase
                            + " visible=" + s.battleP7DamageVisible
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_elder_p7_death_state3_effect_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                ((SourceBattleRuntime) s.current).debugSetEnemyHpForSmoke(s, 1);
                tickUntilBattleP7Phase(s, 2, 120);
                int guard = 0;
                while ("P7".equals(s.battleStateName)
                        && !s.battleP7DeathEffectVisible
                        && guard++ < 180) {
                    s.tick();
                }
                if (!s.battleP7DeathEffectVisible
                        || !s.battleP7BaseHiddenEnemySide
                        || s.battleP7BaseStateEnemySide != 3
                        || s.battleP7DeathEffectPlayerSide
                        || s.battleP7DeathEffectSpriteId != s.battleEnemyVisualId) {
                    throw new IllegalStateException("Expected P7 KO state3 AH type16 death effect, visible="
                            + s.battleP7DeathEffectVisible
                            + " hiddenEnemy=" + s.battleP7BaseHiddenEnemySide
                            + " state=" + s.battleP7BaseStateEnemySide
                            + " effectSidePlayer=" + s.battleP7DeathEffectPlayerSide
                            + " effectSprite=" + s.battleP7DeathEffectSpriteId
                            + " enemySprite=" + s.battleEnemyVisualId
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_elder_p7_death_to_p8_after_effect".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                ((SourceBattleRuntime) s.current).debugSetEnemyHpForSmoke(s, 1);
                int guard = 0;
                boolean sawDeathEffect = false;
                while (!"P8".equals(s.battleStateName) && guard++ < 420) {
                    sawDeathEffect |= s.battleP7DeathEffectVisible;
                    s.tick();
                }
                if (!"P8".equals(s.battleStateName) || !sawDeathEffect || !s.battleP7BaseHiddenEnemySide) {
                    throw new IllegalStateException("Expected P7 death effect before P8, state="
                            + s.battleStateName
                            + " sawDeathEffect=" + sawDeathEffect
                            + " hiddenEnemy=" + s.battleP7BaseHiddenEnemySide
                            + " trace=" + tailTrace(s, 20));
                }
            } else if ("battle_elder_p7_damage_result_debuff".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{1, 45}, 0);
                tickUntilBattleP7Phase(s, 2, 140);
                if (!s.battleP7DamageVisible || s.battleP7DebuffText.isEmpty()) {
                    throw new IllegalStateException("Expected P7 full damage result debuff text, visible="
                            + s.battleP7DamageVisible + " text=" + s.battleP7DebuffText);
                }
            } else if ("battle_p7_hit_forced_direct_skill10".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                ((SourceBattleRuntime) s.current).debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 140);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || !s.battleP7MissText.isEmpty()
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected forced hit P7 damage, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " missText=" + s.battleP7MissText
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_p7_miss_forced_skill10".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerSpeedForSmoke(s, 0);
                runtime.debugSetEnemySpeedForSmoke(s, 200);
                runtime.debugSetNextP7HitRollForSmoke(0);
                tickUntilBattleP7Phase(s, 2, 140);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DamageText.isEmpty()
                        || !s.battleP7DebuffText.isEmpty()
                        || s.battleEnemyHp != s.battleEnemyMaxHp
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected forced miss P7 dodge text and unchanged HP, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                            + " trace=" + tailTrace(s, 22));
                }
            } else if ("battle_p7_crit_forced_skill10".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerSpeedForSmoke(s, 300);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 140);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DamageCritical
                        || s.battleP7DamageText.isEmpty()
                        || !traceContains(s, "critFlag=1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected forced crit P7 damage style, visible="
                            + s.battleP7DamageVisible
                            + " critical=" + s.battleP7DamageCritical
                            + " damageText=" + s.battleP7DamageText
                            + " trace=" + tailTrace(s, 22));
                }
            } else if (checkpoint.startsWith("battle_phase9b_direct_skill_")) {
                int skillId = parsePhase9BSkillId(checkpoint);
                if (!isPhase9BDirectSimpleSmokeSkill(skillId)) {
                    throw new IllegalArgumentException("Phase9B direct smoke excludes skill " + skillId
                            + " because it has debuff/post-effect/unknown behavior needing a dedicated slice");
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                ((SourceBattleRuntime) s.current).debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 160);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || !s.battleP7MissText.isEmpty()
                        || !traceContains(s, "battle P7 hitroll skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9B direct skill forced-hit damage, skill="
                            + skillId
                            + " visible=" + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " missText=" + s.battleP7MissText
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9c_plus_divisor_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9c_plus_divisor_skill_");
                if (skillId != 1 && skillId != 7) {
                    throw new IllegalArgumentException("Phase9C plus-divisor only supports skill 1/7, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 160);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(0)
                        || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(s, "appliedDebuffId=0")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9C plus-divisor skill " + skillId
                            + " to hit and apply debuff0, damageVisible=" + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff0=" + runtime.debugEnemyHasDebuffForSmoke(0)
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9c_cond_debuff0_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9c_cond_debuff0_skill_");
                if (skillId != 3 && skillId != 9) {
                    throw new IllegalArgumentException("Phase9C conditional debuff0 only supports skill 3/9, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyDebuffForFormulaSmoke(s, 0, 12, 1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 160);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(0)
                        || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9C conditional debuff0 skill " + skillId
                            + " to use preloaded debuff0 and hit, damageVisible=" + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " hasDebuff0=" + runtime.debugEnemyHasDebuffForSmoke(0)
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9c_cond_debuff1_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9c_cond_debuff1_skill_");
                if (skillId != 23 && skillId != 29) {
                    throw new IllegalArgumentException("Phase9C conditional debuff1 only supports skill 23/29, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyDebuffForFormulaSmoke(s, 1, 0, 2);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 160);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(1)
                        || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9C conditional debuff1 skill " + skillId
                            + " to use preloaded debuff1 and hit, damageVisible=" + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9c_clear_buff_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9c_clear_buff_skill_");
                if (skillId != 43 && skillId != 49) {
                    throw new IllegalArgumentException("Phase9C clear-buff only supports skill 43/49, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 2, 10, 14);
                if (!runtime.debugEnemyHasBuffForSmoke(2)) {
                    throw new IllegalStateException("Expected smoke setup to preload enemy buff2");
                }
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 160);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || runtime.debugEnemyHasBuffForSmoke(2)
                        || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9C clear-buff skill " + skillId
                            + " to clear enemy buff2 and hit, damageVisible=" + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " hasBuff2=" + runtime.debugEnemyHasBuffForSmoke(2)
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9c_hp_scaling_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9c_hp_scaling_skill_");
                if (skillId != 53 && skillId != 59) {
                    throw new IllegalArgumentException("Phase9C HP-scaling only supports skill 53/59, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerHpForSmoke(s, Math.max(1, s.battlePlayerMaxHp / 4));
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 160);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9C HP-scaling skill " + skillId
                            + " to hit with low attacker HP setup, damageVisible=" + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " playerHp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9d_hit_heal_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9d_hit_heal_skill_");
                if (skillId != 17) {
                    throw new IllegalArgumentException("Phase9D hit heal sibling only supports skill17, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                ((SourceBattleRuntime) s.current).debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 3, 220);
                if (!s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectText.startsWith("+")
                        || !traceContains(s, "postEffect skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9D skill17 hit heal post-effect, visible="
                            + s.battleP7PostEffectVisible
                            + " text=" + s.battleP7PostEffectText
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9d_hit_leech_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9d_hit_leech_skill_");
                if (skillId != 52) {
                    throw new IllegalArgumentException("Phase9D hit leech sibling only supports skill52, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                runtime.debugSetNextLeechRollForSmoke(0);
                tickUntilBattleP7Phase(s, 3, 240);
                if (!traceContains(s, "source aa skill=" + skillId)
                        || !traceContains(s, "passed=true")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9D skill52 hit to pass source aa leech gate"
                            + " even if heal rounds to 0, visible="
                            + s.battleP7PostEffectVisible
                            + " text=" + s.battleP7PostEffectText
                            + " trace=" + tailTrace(s, 28));
                }
            } else if (checkpoint.startsWith("battle_phase9d_hit_followup_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9d_hit_followup_skill_");
                if (skillId != 69) {
                    throw new IllegalArgumentException("Phase9D hit follow-up sibling only supports skill69, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 3, 260);
                runtime.debugSetNextFollowUpRollForSmoke(0);
                tickUntilBattleState(s, "P2", 180);
                if (!traceContains(s, "follow-up P2 from skill=" + skillId)
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9D skill69 hit follow-up to P2, state="
                            + s.battleStateName
                            + " trace=" + tailTrace(s, 28));
                }
            } else if (checkpoint.startsWith("battle_phase9d_miss_heal_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9d_miss_heal_skill_");
                if (skillId != 11 && skillId != 17) {
                    throw new IllegalArgumentException("Phase9D miss heal only supports skill11/17, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 3, 240);
                if (!s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectText.startsWith("+")
                        || !traceContains(s, "postEffect skill=" + skillId)
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9D skill" + skillId
                            + " miss to still run source q() heal, visible="
                            + s.battleP7PostEffectVisible
                            + " text=" + s.battleP7PostEffectText
                            + " trace=" + tailTrace(s, 28));
                }
            } else if (checkpoint.startsWith("battle_phase9d_miss_leech_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9d_miss_leech_skill_");
                if (skillId != 52 && skillId != 58) {
                    throw new IllegalArgumentException("Phase9D miss leech only supports skill52/58, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                runtime.debugSetNextLeechRollForSmoke(0);
                tickUntilBattleP7Phase(s, 3, 260);
                boolean expectsVisibleLeech = skillId == 58;
                if ((expectsVisibleLeech && (!s.battleP7PostEffectVisible || !s.battleP7PostEffectText.startsWith("+")))
                        || !traceContains(s, "source aa skill=" + skillId)
                        || !traceContains(s, "passed=true")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9D skill" + skillId
                            + " miss to keep source aa leech path"
                            + (expectsVisibleLeech ? " with visible heal" : " even when heal rounds to 0")
                            + ", visible="
                            + s.battleP7PostEffectVisible
                            + " text=" + s.battleP7PostEffectText
                            + " trace=" + tailTrace(s, 30));
                }
            } else if (checkpoint.startsWith("battle_phase9d_miss_followup_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9d_miss_followup_skill_");
                if (skillId != 63 && skillId != 69) {
                    throw new IllegalArgumentException("Phase9D miss follow-up only supports skill63/69, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 3, 260);
                runtime.debugSetNextFollowUpRollForSmoke(0);
                tickUntilBattleState(s, "P2", 180);
                if (!traceContains(s, "follow-up P2 from skill=" + skillId)
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9D skill" + skillId
                            + " miss follow-up to still use source q() roll, state="
                            + s.battleStateName
                            + " trace=" + tailTrace(s, 30));
                }
            } else if (checkpoint.startsWith("battle_phase9e_debuff1_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9e_debuff1_success_skill_");
                if (!isPhase9EDebuff1Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9E debuff1 success only supports 2/8/22/28, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextDamageDebuffRollForSmoke(0);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(1)
                        || !traceContains(s, "appliedDebuffId=1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9E skill" + skillId
                            + " hit to apply debuff1 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9e_debuff1_buff14_block_skill_2".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{2, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(1)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9E buff14 to block skill2 debuff1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9e_debuff1_status3_reduced_block_skill_2".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{2, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(9);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(1)
                        || !traceContains(s, "forced damage.debuff roll=9")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9E status3 to reduce skill2 debuff chance and block roll9, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9e_debuff1_miss_queue_skill_2".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{2, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextDamageDebuffRollForSmoke(0);
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(1)
                        || !traceContains(s, "appliedDebuffId=1")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9E skill2 miss to hide debuff text but keep source-applied debuff1, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=1", 700);
            } else if (checkpoint.startsWith("battle_phase9f_debuff2_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9f_debuff2_success_skill_");
                if (!isPhase9FDebuff2Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9F debuff2 success only supports 12/18, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(2)
                        || !traceContains(s, "appliedDebuffId=2")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9F skill" + skillId
                            + " hit to apply implicit debuff2 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff2=" + runtime.debugEnemyHasDebuffForSmoke(2)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9f_debuff2_buff14_block_skill_12".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{12, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(2)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9F buff14 to block skill12 debuff2, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff2=" + runtime.debugEnemyHasDebuffForSmoke(2)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9f_debuff2_status3_block_skill_12".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{12, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(2)
                        || !traceContains(s, "forced damage.debuff roll=1")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9F status3 to block implicit skill12 debuff2 on roll1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff2=" + runtime.debugEnemyHasDebuffForSmoke(2)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9f_debuff2_miss_queue_skill_12".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{12, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(2)
                        || !traceContains(s, "appliedDebuffId=2")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9F skill12 miss to hide debuff text but keep source-applied debuff2, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff2=" + runtime.debugEnemyHasDebuffForSmoke(2)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=2", 700);
            } else if (checkpoint.startsWith("battle_phase9g_debuff3_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9g_debuff3_success_skill_");
                if (!isPhase9GDebuff3Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9G debuff3 success only supports 13/19, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(3)
                        || !traceContains(s, "appliedDebuffId=3")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9G skill" + skillId
                            + " hit to apply delayed-damage debuff3 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff3=" + runtime.debugEnemyHasDebuffForSmoke(3)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9g_debuff3_buff14_block_skill_13".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{13, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(3)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9G buff14 to block skill13 debuff3, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff3=" + runtime.debugEnemyHasDebuffForSmoke(3)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9g_debuff3_status3_block_skill_13".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{13, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(3)
                        || !traceContains(s, "forced damage.debuff roll=1")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9G status3 to block implicit skill13 debuff3 on roll1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff3=" + runtime.debugEnemyHasDebuffForSmoke(3)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9g_debuff3_miss_queue_skill_13".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{13, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(3)
                        || !traceContains(s, "appliedDebuffId=3")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9G skill13 miss to hide debuff text but keep source-applied debuff3, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff3=" + runtime.debugEnemyHasDebuffForSmoke(3)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=3", 700);
            } else if (checkpoint.startsWith("battle_phase9h_debuff4_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9h_debuff4_success_skill_");
                if (!isPhase9HDebuff4Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9H debuff4 success only supports 31/37, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(4)
                        || !traceContains(s, "appliedDebuffId=4")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9H skill" + skillId
                            + " hit to apply debuff4 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff4=" + runtime.debugEnemyHasDebuffForSmoke(4)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9h_debuff4_buff14_block_skill_31".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{31, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(4)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9H buff14 to block skill31 debuff4, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff4=" + runtime.debugEnemyHasDebuffForSmoke(4)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9h_debuff4_status3_block_skill_31".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{31, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(4)
                        || !traceContains(s, "forced damage.debuff roll=1")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9H status3 to block implicit skill31 debuff4 on roll1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff4=" + runtime.debugEnemyHasDebuffForSmoke(4)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9h_debuff4_miss_queue_skill_31".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{31, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(4)
                        || !traceContains(s, "appliedDebuffId=4")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9H skill31 miss to hide debuff text but keep source-applied debuff4, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff4=" + runtime.debugEnemyHasDebuffForSmoke(4)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=4", 700);
            } else if ("battle_phase9h_debuff4_miss_chance_skill_31".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugPlayerDebuffForItemSmoke(s, 4, 1, 31);
                runtime.debugSetPlayerSpeedForSmoke(s, 0);
                runtime.debugSetEnemySpeedForSmoke(s, 200);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!traceContains(s, "debuff4Value=1") || !traceContains(s, "missChance=")) {
                    throw new IllegalStateException("Expected Phase9H debuff4 to feed source P7 miss chance trace, trace="
                            + tailTrace(s, 40));
                }
            } else if (checkpoint.startsWith("battle_phase9i_debuff5_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9i_debuff5_success_skill_");
                if (!isPhase9IDebuff5Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9I debuff5 success only supports 32/38/61, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(5)
                        || !traceContains(s, "appliedDebuffId=5")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9I skill" + skillId
                            + " hit to apply speed debuff5 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff5=" + runtime.debugEnemyHasDebuffForSmoke(5)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9i_debuff5_buff14_block_skill_32".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{32, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(5)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9I buff14 to block skill32 debuff5, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff5=" + runtime.debugEnemyHasDebuffForSmoke(5)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9i_debuff5_status3_block_skill_32".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{32, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(5)
                        || !traceContains(s, "forced damage.debuff roll=1")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9I status3 to block implicit skill32 debuff5 on roll1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff5=" + runtime.debugEnemyHasDebuffForSmoke(5)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9i_debuff5_miss_queue_skill_32".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{32, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(5)
                        || !traceContains(s, "appliedDebuffId=5")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9I skill32 miss to hide debuff text but keep source-applied debuff5, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff5=" + runtime.debugEnemyHasDebuffForSmoke(5)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=5", 700);
            } else if ("battle_phase9i_debuff5_stat_consumer_skill_32".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{32, 45}, 0);
                tickUntilTraceContains(s, "active queue apply bank=1 id=5", 700);
                if (s.battleP7SpecialVisible
                        || !traceContains(s, "active queue apply bank=1 id=5")
                        || !traceContains(s, "speed ")) {
                    throw new IllegalStateException("Expected Phase9I debuff5 stat tick without visual per source ai gate, state="
                            + s.battleStateName
                            + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9j_debuff6_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9j_debuff6_success_skill_");
                if (!isPhase9JDebuff6Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9J debuff6 success only supports 33/39, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(6)
                        || !traceContains(s, "appliedDebuffId=6")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9J skill" + skillId
                            + " hit to apply damage-reduction debuff6 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff6=" + runtime.debugEnemyHasDebuffForSmoke(6)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9j_debuff6_buff14_block_skill_33".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{33, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(6)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9J buff14 to block skill33 debuff6, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff6=" + runtime.debugEnemyHasDebuffForSmoke(6)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9j_debuff6_status3_block_skill_33".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{33, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(6)
                        || !traceContains(s, "forced damage.debuff roll=1")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9J status3 to block implicit skill33 debuff6 on roll1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff6=" + runtime.debugEnemyHasDebuffForSmoke(6)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9j_debuff6_miss_queue_skill_33".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{33, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(6)
                        || !traceContains(s, "appliedDebuffId=6")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9J skill33 miss to hide debuff text but keep source-applied debuff6, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff6=" + runtime.debugEnemyHasDebuffForSmoke(6)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=6", 700);
            } else if ("battle_phase9j_debuff6_damage_reduction_skill_33".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugPlayerDebuffForItemSmoke(s, 6, 50, 33);
                runtime.debugSetPlayerSpeedForSmoke(s, 0);
                BattleUnit.setDamageRandomSeedForChecks(0L);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=10");
                if (damage <= 0 || damage > 20 || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9J preloaded debuff6 value=50 to reduce outgoing skill10 damage, damage="
                            + damage
                            + " text=" + s.battleP7DamageText
                            + " trace=" + tailTrace(s, 34));
                }
            } else if (checkpoint.startsWith("battle_phase9k_debuff7_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9k_debuff7_success_skill_");
                if (!isPhase9KDebuff7Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9K debuff7 success only supports 51/57, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(7)
                        || !traceContains(s, "appliedDebuffId=7")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9K skill" + skillId
                            + " hit to apply defense debuff7 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff7=" + runtime.debugEnemyHasDebuffForSmoke(7)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9k_debuff7_buff14_block_skill_51".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{51, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(7)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9K buff14 to block skill51 debuff7, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff7=" + runtime.debugEnemyHasDebuffForSmoke(7)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9k_debuff7_status3_block_skill_51".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{51, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(7)
                        || !traceContains(s, "forced damage.debuff roll=1")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9K status3 to block implicit skill51 debuff7 on roll1, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff7=" + runtime.debugEnemyHasDebuffForSmoke(7)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9k_debuff7_miss_queue_skill_51".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{51, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(7)
                        || !traceContains(s, "appliedDebuffId=7")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9K skill51 miss to hide debuff text but keep source-applied debuff7, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff7=" + runtime.debugEnemyHasDebuffForSmoke(7)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=7", 700);
            } else if ("battle_phase9k_debuff7_stat_consumer_skill_51".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{51, 45}, 0);
                tickUntilTraceContains(s, "active queue apply bank=1 id=7", 700);
                if (s.battleP7SpecialVisible
                        || !traceContains(s, "active queue apply bank=1 id=7")
                        || !traceContains(s, "defense ")) {
                    throw new IllegalStateException("Expected Phase9K debuff7 stat tick without visual per source ai gate, state="
                            + s.battleStateName
                            + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 24));
                }
            } else if (checkpoint.startsWith("battle_phase9l_debuff10_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9l_debuff10_success_skill_");
                if (!isPhase9LDebuff10Skill(skillId)) {
                    throw new IllegalArgumentException("Phase9L debuff10 success only supports 41/47, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextDamageDebuffRollForSmoke(0);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || s.battleP7DamageText.isEmpty()
                        || s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(10)
                        || !traceContains(s, "appliedDebuffId=10")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9L skill" + skillId
                            + " hit to apply catch-status debuff10 and show debuff text, visible="
                            + s.battleP7DamageVisible
                            + " damageText=" + s.battleP7DamageText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff10=" + runtime.debugEnemyHasDebuffForSmoke(10)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9l_debuff10_buff14_block_skill_41".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{41, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(10)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9L buff14 to block skill41 debuff10, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff10=" + runtime.debugEnemyHasDebuffForSmoke(10)
                            + " trace=" + tailTrace(s, 30));
                }
            } else if ("battle_phase9l_debuff10_status3_block_skill_41".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{41, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(9);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !s.battleP7DebuffText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(10)
                        || !traceContains(s, "forced damage.debuff roll=9")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9L status3 to reduce skill41 debuff10 chance and block roll9, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff10=" + runtime.debugEnemyHasDebuffForSmoke(10)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9l_debuff10_miss_queue_skill_41".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{41, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextDamageDebuffRollForSmoke(0);
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DebuffText.isEmpty()
                        || !runtime.debugEnemyHasDebuffForSmoke(10)
                        || !traceContains(s, "appliedDebuffId=10")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9L skill41 miss to hide debuff text but keep source-applied debuff10, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff10=" + runtime.debugEnemyHasDebuffForSmoke(10)
                            + " trace=" + tailTrace(s, 32));
                }
                tickUntilTraceContains(s, "active queue apply bank=1 id=10", 700);
            } else if ("battle_phase9l_debuff10_visual_consumer_skill_41".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{41, 45}, 0);
                ((SourceBattleRuntime) s.current).debugSetNextDamageDebuffRollForSmoke(0);
                tickUntilTraceContains(s, "active queue visual start bank=1 id=10", 700);
                if (!s.battleP7SpecialVisible
                        || s.battleP7SpecialType != 9
                        || !traceContains(s, "row=[1, 19, 0, -1, 1, 6, 0, -1]")) {
                    throw new IllegalStateException("Expected Phase9L debuff10 to use source visual row [1,19,0,-1,1,6,0,-1], state="
                            + s.battleStateName
                            + " special=" + s.battleP7SpecialVisible
                            + " type=" + s.battleP7SpecialType
                            + " trace=" + tailTrace(s, 34));
                }
            } else if ("battle_phase9l_debuff10_catch_chance_after_skill41".equals(checkpoint)) {
                VqsvIntroDemo.Scene base = setupCatchChanceStatusMenu(-1, false);
                int baseChance = catchMenuChanceForItem(base, 1);
                enterElderP7WithSkills(s, new int[]{41, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextDamageDebuffRollForSmoke(0);
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                int debuff10Chance = runtime.debugCatchChanceForSmoke(1);
                if (!runtime.debugEnemyHasDebuffForSmoke(10)
                        || debuff10Chance <= baseChance
                        || !traceContains(s, "appliedDebuffId=10")) {
                    throw new IllegalStateException("Expected Phase9L debuff10 from skill41 to raise catch chance, base="
                            + baseChance
                            + " debuff10=" + debuff10Chance
                            + " hasDebuff10=" + runtime.debugEnemyHasDebuffForSmoke(10)
                            + " trace=" + tailTrace(s, 32));
                }
                s.sourceStateTrace.add("SMOKE verified Phase9L debuff10 catch chance base="
                        + baseChance + " debuff10=" + debuff10Chance);
            } else if (checkpoint.startsWith("battle_phase9m_zero_power_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9m_zero_power_success_skill_");
                if (!isPhase9MZeroPowerDebuffSkill(skillId)) {
                    throw new IllegalArgumentException("Phase9M zero-power only supports 54/55, got " + skillId);
                }
                int debuffId = skillId == 54 ? 8 : 9;
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                tickUntilBattleP7Phase(s, 3, 260);
                if (s.battleP7DamageVisible
                        || runtime.debugEnemyHasDebuffForSmoke(debuffId)
                        || s.battleEnemyHp != s.battleEnemyMaxHp
                        || !traceContains(s, "no-damage skill=" + skillId)
                        || traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || traceContains(s, "appliedDebuffId=" + debuffId)
                        || traceContains(s, "battle P7 hitroll skill=" + skillId)) {
                    throw new IllegalStateException("Expected Phase9M skill" + skillId
                            + " zero-power source guard to skip damage/debuff path for debuff" + debuffId
                            + ", visible=" + s.battleP7DamageVisible
                            + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                            + " hasDebuff=" + runtime.debugEnemyHasDebuffForSmoke(debuffId)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9m_zero_power_buff14_block_skill_54".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{54, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyBuffForFormulaSmoke(s, 14, 0, 25);
                tickUntilBattleP7Phase(s, 3, 260);
                if (s.battleP7DamageVisible
                        || runtime.debugEnemyHasDebuffForSmoke(8)
                        || !traceContains(s, "no-damage skill=54")
                        || traceContains(s, "battle P7 damage frame skill=54")
                        || traceContains(s, "appliedDebuffId=")) {
                    throw new IllegalStateException("Expected Phase9M skill54 to skip debuff path before buff14 matters, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff8=" + runtime.debugEnemyHasDebuffForSmoke(8)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9m_zero_power_status3_block_skill_54".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{54, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemyFormStatusForSmoke(s, 3);
                runtime.debugSetNextDamageDebuffRollForSmoke(1);
                tickUntilBattleP7Phase(s, 3, 260);
                if (s.battleP7DamageVisible
                        || runtime.debugEnemyHasDebuffForSmoke(8)
                        || !traceContains(s, "no-damage skill=54")
                        || traceContains(s, "forced damage.debuff roll=1")
                        || traceContains(s, "appliedDebuffId=")) {
                    throw new IllegalStateException("Expected Phase9M skill54 to skip debuff path before status3 chance roll, visible="
                            + s.battleP7DamageVisible
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff8=" + runtime.debugEnemyHasDebuffForSmoke(8)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9m_zero_power_miss_queue_skill_54".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{54, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 3, 260);
                if (s.battleP7DamageVisible
                        || runtime.debugEnemyHasDebuffForSmoke(8)
                        || !traceContains(s, "no-damage skill=54")
                        || traceContains(s, "battle P7 hitroll skill=54")
                        || traceContains(s, "appliedDebuffId=")) {
                    throw new IllegalStateException("Expected Phase9M skill54 to skip hitroll/miss and debuff path, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " debuffText=" + s.battleP7DebuffText
                            + " hasDebuff8=" + runtime.debugEnemyHasDebuffForSmoke(8)
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9m_debuff8_visual_consumer_skill54".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{54, 45}, 0);
                tickUntilBattleP7Phase(s, 3, 260);
                for (int i = 0; i < 180; i++) {
                    s.tick();
                }
                if (traceContains(s, "active queue visual start bank=1 id=8")
                        || traceContains(s, "active queue apply bank=1 id=8")
                        || traceContains(s, "row=[1, 0, 0, -1, 0, 25, 0, -1]")) {
                    throw new IllegalStateException("Expected Phase9M debuff8 visual not to start because P7 no-damage guard skips debuff apply, state="
                            + s.battleStateName
                            + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 34));
                }
            } else if ("battle_phase9m_debuff9_visual_consumer_skill55".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{55, 45}, 0);
                tickUntilBattleP7Phase(s, 3, 260);
                for (int i = 0; i < 180; i++) {
                    s.tick();
                }
                if (traceContains(s, "active queue visual start bank=1 id=9")
                        || traceContains(s, "active queue apply bank=1 id=9")
                        || traceContains(s, "row=[1, 12, 0, -1]")) {
                    throw new IllegalStateException("Expected Phase9M debuff9 visual not to start because P7 no-damage guard skips debuff apply, state="
                            + s.battleStateName
                            + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 34));
                }
            } else if (checkpoint.startsWith("battle_phase9n_clear_buff_success_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9n_clear_buff_success_skill_");
                if (!isPhase9NClearBuffSkill(skillId)) {
                    throw new IllegalArgumentException("Phase9N clear-buff only supports 43/49, got " + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemySourceBuffForClearSmoke(s, 2, 10, 14);
                runtime.debugEnemySourceBuffForClearSmoke(s, 14, -1, 25);
                if (!runtime.debugEnemyHasBuffForSmoke(2)
                        || !runtime.debugEnemyHasBuffForSmoke(14)
                        || runtime.debugEnemyActiveBuffSlotForSmoke(2) < 0
                        || runtime.debugEnemyActiveBuffSlotForSmoke(14) < 0) {
                    throw new IllegalStateException("Expected Phase9N smoke setup to preload active buffs, trace="
                            + tailTrace(s, 18));
                }
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
                if (!s.battleP7DamageVisible
                        || damage <= 0
                        || runtime.debugEnemyHasBuffForSmoke(2)
                        || runtime.debugEnemyHasBuffForSmoke(14)
                        || runtime.debugEnemyActiveBuffSlotForSmoke(2) >= 0
                        || runtime.debugEnemyActiveBuffSlotForSmoke(14) >= 0
                        || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9N skill" + skillId
                            + " hit to damage and clear all target buffs, damage="
                            + damage
                            + " hasBuff2=" + runtime.debugEnemyHasBuffForSmoke(2)
                            + " hasBuff14=" + runtime.debugEnemyHasBuffForSmoke(14)
                            + " slot2=" + runtime.debugEnemyActiveBuffSlotForSmoke(2)
                            + " slot14=" + runtime.debugEnemyActiveBuffSlotForSmoke(14)
                            + " trace=" + tailTrace(s, 34));
                }
            } else if ("battle_phase9n_clear_buff_miss_skill_43".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{43, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemySourceBuffForClearSmoke(s, 2, 10, 14);
                runtime.debugEnemySourceBuffForClearSmoke(s, 14, -1, 25);
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DamageText.isEmpty()
                        || s.battleEnemyHp != s.battleEnemyMaxHp
                        || runtime.debugEnemyHasBuffForSmoke(2)
                        || runtime.debugEnemyHasBuffForSmoke(14)
                        || runtime.debugEnemyActiveBuffSlotForSmoke(2) >= 0
                        || runtime.debugEnemyActiveBuffSlotForSmoke(14) >= 0
                        || !traceContains(s, "battle P7 damage frame skill=43")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9N skill43 miss to keep HP but clear target buffs, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                            + " hasBuff2=" + runtime.debugEnemyHasBuffForSmoke(2)
                            + " hasBuff14=" + runtime.debugEnemyHasBuffForSmoke(14)
                            + " trace=" + tailTrace(s, 36));
                }
            } else if ("battle_phase9n_clear_buff_no_target_buff_skill_43".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{43, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetNextP7HitRollForSmoke(99);
                tickUntilBattleP7Phase(s, 2, 180);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=43");
                if (!s.battleP7DamageVisible
                        || damage <= 0
                        || runtime.debugEnemyHasBuffForSmoke(2)
                        || !traceContains(s, "battle P7 damage frame skill=43")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9N skill43 without target buff to still damage normally, damage="
                            + damage
                            + " trace=" + tailTrace(s, 30));
                }
            } else if (checkpoint.startsWith("battle_phase9o_hp_scaling_low_high_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9o_hp_scaling_low_high_skill_");
                if (!isPhase9OHpScalingSkill(skillId)) {
                    throw new IllegalArgumentException("Phase9O HP scaling only supports 53/59, got " + skillId);
                }
                VqsvIntroDemo.Scene low = new VqsvIntroDemo.Scene();
                int lowDamage = phase9OHpScalingDamage(low, skillId, 25, 120, 20);
                VqsvIntroDemo.Scene high = new VqsvIntroDemo.Scene();
                int highDamage = phase9OHpScalingDamage(high, skillId, 100, 120, 20);
                if (!(lowDamage > highDamage)
                        || !traceContains(low, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(high, "battle P7 damage frame skill=" + skillId)
                        || !traceContains(low, "hit=true")
                        || !traceContains(high, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9O skill" + skillId
                            + " low HP damage > high HP damage, low=" + lowDamage
                            + " high=" + highDamage
                            + " lowTrace=" + tailTrace(low, 24)
                            + " highTrace=" + tailTrace(high, 24));
                }
                s = low;
                s.sourceStateTrace.add("SMOKE verified Phase9O HP scaling skill="
                        + skillId + " lowDamage=" + lowDamage + " highDamage=" + highDamage);
            } else if ("battle_phase9o_hp_scaling_min_clamp_skill_53".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{53, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerAttackForSmoke(s, 10);
                runtime.debugSetEnemyDefenseForSmoke(s, 80);
                runtime.debugSetPlayerHpForSmoke(s, s.battlePlayerMaxHp);
                runtime.debugSetNextP7HitRollForSmoke(99);
                BattleUnit.setDamageRandomSeedForChecks(0L);
                tickUntilBattleP7Phase(s, 2, 180);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=53");
                if (!s.battleP7DamageVisible
                        || damage != 1
                        || !s.battleP7DamageText.equals("-1")
                        || !traceContains(s, "battle P7 damage frame skill=53")
                        || !traceContains(s, "hit=true")) {
                    throw new IllegalStateException("Expected Phase9O skill53 min clamp damage=1 with raw <= 0 setup, damage="
                            + damage
                            + " text=" + s.battleP7DamageText
                            + " trace=" + tailTrace(s, 32));
                }
            } else if ("battle_phase9o_hp_scaling_miss_skill_53".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{53, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerAttackForSmoke(s, 120);
                runtime.debugSetEnemyDefenseForSmoke(s, 20);
                runtime.debugSetPlayerHpForSmoke(s, Math.max(1, s.battlePlayerMaxHp / 4));
                forceNextP7Miss(s);
                tickUntilBattleP7Phase(s, 2, 180);
                if (!s.battleP7DamageVisible
                        || !VqsvText.Battle.DODGE.equals(s.battleP7MissText)
                        || !s.battleP7DamageText.isEmpty()
                        || s.battleEnemyHp != s.battleEnemyMaxHp
                        || !traceContains(s, "battle P7 damage frame skill=53")
                        || !traceContains(s, "hit=false")) {
                    throw new IllegalStateException("Expected Phase9O skill53 miss to compute result but keep HP unchanged, visible="
                            + s.battleP7DamageVisible
                            + " missText=" + s.battleP7MissText
                            + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                            + " trace=" + tailTrace(s, 34));
                }
            } else if (checkpoint.startsWith("battle_phase9r_raw_self_buff_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9r_raw_self_buff_skill_");
                if (!isPhase9RRawSelfBuffSkill(skillId)) {
                    throw new IllegalArgumentException("Phase9R only covers raw-damage self-buff skills 21/27/42/48/62, got "
                            + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerAttackForSmoke(s, 100);
                runtime.debugSetEnemyDefenseForSmoke(s, 40);
                runtime.debugSetNextP7HitRollForSmoke(99);
                BattleUnit.setDamageRandomSeedForChecks(0L);
                tickUntilBattleP7Phase(s, 3, 260);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
                int buffId = phase9RSelfBuffId(skillId);
                if (damage <= 0
                        || !s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectPlayerSide
                        || s.battleP7PostEffectText.isEmpty()
                        || !runtime.debugPlayerHasBuffForSmoke(buffId)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(buffId) < 0
                        || !traceContains(s, "BYTECODE_DEFAULT_RAW_DAMAGE skill=" + skillId)
                        || !traceContains(s, "powerPercentIgnored=")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || !traceContains(s, "game.d.q postEffect skill=" + skillId)
                        || !traceContains(s, "buffId=" + buffId)
                        || !traceContains(s, "selfTarget=true")) {
                    throw new IllegalStateException("Expected Phase9R skill" + skillId
                            + " raw/default damage plus attacker self-buff buffId=" + buffId
                            + ", damage=" + damage
                            + " postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " playerHasBuff=" + runtime.debugPlayerHasBuffForSmoke(buffId)
                            + " activeSlot=" + runtime.debugPlayerActiveBuffSlotForSmoke(buffId)
                            + " trace=" + tailTrace(s, 42));
                }
            } else if ("battle_phase9t_raw_visual_skill_67".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{67, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerAttackForSmoke(s, 100);
                runtime.debugSetEnemyDefenseForSmoke(s, 40);
                runtime.debugSetNextP7HitRollForSmoke(99);
                BattleUnit.setDamageRandomSeedForChecks(0L);
                tickUntilBattleP7Phase(s, 3, 280);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=67");
                if (damage <= 0
                        || s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectText.isEmpty()
                        || runtime.debugEnemyHasDebuffForSmoke(5)
                        || !traceContains(s, "BYTECODE_DEFAULT_RAW_DAMAGE skill=67")
                        || !traceContains(s, "powerPercentIgnored=110")
                        || !traceContains(s, "effectIdIgnored=5")
                        || !traceContains(s, "appliedDebuffId=-1")
                        || traceContains(s, "game.d.q postEffect skill=67")
                        || !traceContains(s, "battle P7 source n() skill=67 chunk=0")
                        || !traceContains(s, "id=26")
                        || !traceContains(s, "battle P7 speffect skill=67")
                        || !traceContains(s, "speffect=11")) {
                    throw new IllegalStateException("Expected Phase9T skill67 raw/default damage, no debuff5, no q() post-effect, and visual row 67 traces; damage="
                            + damage
                            + " postVisible=" + s.battleP7PostEffectVisible
                            + " postText=" + s.battleP7PostEffectText
                            + " enemyDebuff5=" + runtime.debugEnemyHasDebuffForSmoke(5)
                            + " trace=" + tailTrace(s, 48));
                }
            } else if ("battle_phase9u_direct_self_buff_skill_68".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{68, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerAttackForSmoke(s, 100);
                runtime.debugSetEnemyDefenseForSmoke(s, 40);
                runtime.debugSetNextP7HitRollForSmoke(99);
                BattleUnit.setDamageRandomSeedForChecks(0L);
                tickUntilBattleP7Phase(s, 3, 260);
                int damage = latestTraceDamage(s, "battle P7 damage frame skill=68");
                if (damage <= 0
                        || !s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectPlayerSide
                        || s.battleP7PostEffectText.isEmpty()
                        || !runtime.debugPlayerHasBuffForSmoke(10)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(10) < 0
                        || !runtime.debugEnemyHasDebuffForSmoke(10)
                        || !traceContains(s, "POWER_PERCENT skill=68")
                        || !traceContains(s, "powerPercent=110")
                        || traceContains(s, "BYTECODE_DEFAULT_RAW_DAMAGE skill=68")
                        || !traceContains(s, "appliedDebuffId=10")
                        || !traceContains(s, "game.d.q postEffect skill=68")
                        || !traceContains(s, "buffId=10")
                        || !traceContains(s, "selfTarget=true")) {
                    throw new IllegalStateException("Expected Phase9U skill68 direct power damage plus target debuff10 and attacker self-buff10, damage="
                            + damage
                            + " postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " playerHasBuff10=" + runtime.debugPlayerHasBuffForSmoke(10)
                            + " activeSlot10=" + runtime.debugPlayerActiveBuffSlotForSmoke(10)
                            + " enemyDebuff10=" + runtime.debugEnemyHasDebuffForSmoke(10)
                            + " trace=" + tailTrace(s, 48));
                }
            } else if ("battle_phase9w_skill64_selected_buff_copy".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{64, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugEnemySourceBuffForClearSmoke(s, 2, 10, 14);
                if (!runtime.debugEnemyHasBuffForSmoke(2)
                        || runtime.debugEnemyActiveBuffSlotForSmoke(2) < 0) {
                    throw new IllegalStateException("Expected Phase9W setup to preload enemy buff2, trace="
                            + tailTrace(s, 18));
                }
                tickUntilBattleP7Phase(s, 3, 260);
                if (!s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectPlayerSide
                        || s.battleP7PostEffectText.isEmpty()
                        || runtime.debugEnemyHasBuffForSmoke(2)
                        || runtime.debugEnemyActiveBuffSlotForSmoke(2) >= 0
                        || !runtime.debugPlayerHasBuffForSmoke(2)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(2) < 0
                        || runtime.debugPlayerBuffValueForSmoke(2) <= 0
                        || !runtime.debugPlayerHasBuffForSmoke(11)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(11) < 0
                        || runtime.debugPlayerBuffValueForSmoke(11) != 0
                        || traceContains(s, "battle P7 damage frame skill=64")
                        || !traceContains(s, "battle P7 no-damage skill=64")
                        || !traceContains(s, "game.d.q postEffect skill=64")
                        || !traceContains(s, "buffId=11")
                        || !traceContains(s, "selfTarget=true")
                        || !traceContains(s, "targetSlot=0")) {
                    throw new IllegalStateException("Expected Phase9W skill64 to copy selected target buff2, clear enemy buffs, store selected slot in buff11, and skip damage; "
                            + "postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " enemyHasBuff2=" + runtime.debugEnemyHasBuffForSmoke(2)
                            + " enemySlot2=" + runtime.debugEnemyActiveBuffSlotForSmoke(2)
                            + " playerHasBuff2=" + runtime.debugPlayerHasBuffForSmoke(2)
                            + " playerSlot2=" + runtime.debugPlayerActiveBuffSlotForSmoke(2)
                            + " playerBuff2Value=" + runtime.debugPlayerBuffValueForSmoke(2)
                            + " playerHasBuff11=" + runtime.debugPlayerHasBuffForSmoke(11)
                            + " playerSlot11=" + runtime.debugPlayerActiveBuffSlotForSmoke(11)
                            + " playerBuff11Value=" + runtime.debugPlayerBuffValueForSmoke(11)
                            + " trace=" + tailTrace(s, 52));
                }
            } else if (checkpoint.startsWith("battle_phase9y_no_damage_buff_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9y_no_damage_buff_skill_");
                int buffId = phase9YBuffId(skillId);
                if (buffId < 0) {
                    throw new IllegalArgumentException("Phase9Y only supports no-damage producer skills 4/5/14/44, got "
                            + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                tickUntilBattleP7Phase(s, 3, 300);
                int baseDefense = runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE);
                int currentDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
                boolean defenseHookOk = skillId == 4 || skillId == 14
                        ? currentDefense > baseDefense
                        : skillId == 5
                        ? currentDefense < baseDefense
                        : currentDefense == baseDefense;
                if (!s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectPlayerSide
                        || s.battleP7PostEffectText.isEmpty()
                        || !runtime.debugPlayerHasBuffForSmoke(buffId)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(buffId) < 0
                        || runtime.debugPlayerBuffValueForSmoke(buffId) <= 0
                        || !defenseHookOk
                        || traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || traceContains(s, "battle P7 hitroll skill=" + skillId)
                        || !traceContains(s, "battle P7 no-damage skill=" + skillId)
                        || !traceContains(s, "game.d.q postEffect skill=" + skillId)
                        || !traceContains(s, "buffId=" + buffId)
                        || !traceContains(s, "targetSide=1")
                        || !traceContains(s, "targetSlot=1")) {
                    throw new IllegalStateException("Expected Phase9Y skill" + skillId
                            + " to apply same-side buff" + buffId
                            + " without damage, baseDefense=" + baseDefense
                            + " currentDefense=" + currentDefense
                            + " postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " playerHasBuff=" + runtime.debugPlayerHasBuffForSmoke(buffId)
                            + " activeSlot=" + runtime.debugPlayerActiveBuffSlotForSmoke(buffId)
                            + " buffValue=" + runtime.debugPlayerBuffValueForSmoke(buffId)
                            + " trace=" + tailTrace(s, 52));
                }
            } else if (checkpoint.startsWith("battle_phase9z_cleanse_protect_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9z_cleanse_protect_skill_");
                if (skillId != 24 && skillId != 25) {
                    throw new IllegalArgumentException("Phase9Z only supports cleanse/protection producer skills 24/25, got "
                            + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                runtime.debugSetPlayerHpForSmoke(s, Math.max(1, s.battlePlayerMaxHp / 2));
                int hpBefore = s.battlePlayerHp;
                runtime.debugPlayerDebuffForItemSmoke(s, 5, 8, 32);
                if (!runtime.debugPlayerHasDebuffForSmoke(5)) {
                    throw new IllegalStateException("Expected Phase9Z setup to preload player debuff5, trace="
                            + tailTrace(s, 18));
                }
                tickUntilBattleP7Phase(s, 3, 320);
                int expectedBuff = skillId == 24 ? 13 : 14;
                boolean hpOk = skillId == 24 ? s.battlePlayerHp > hpBefore : s.battlePlayerHp == hpBefore;
                boolean blockOk = true;
                if (skillId == 25) {
                    blockOk = runtime.debugEnemyTryDebuffPlayerForSmoke(s, 2, 1) == -1
                            && !runtime.debugPlayerHasDebuffForSmoke(1);
                }
                if (!s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectPlayerSide
                        || s.battleP7PostEffectText.isEmpty()
                        || runtime.debugPlayerHasDebuffForSmoke(5)
                        || !runtime.debugPlayerHasBuffForSmoke(expectedBuff)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(expectedBuff) < 0
                        || !hpOk
                        || !blockOk
                        || traceContains(s, "battle P7 damage frame skill=" + skillId)
                        || traceContains(s, "battle P7 hitroll skill=" + skillId)
                        || !traceContains(s, "battle P7 no-damage skill=" + skillId)
                        || !traceContains(s, "game.d.q postEffect skill=" + skillId)
                        || !traceContains(s, "buffId=" + expectedBuff)
                        || !traceContains(s, "targetSide=1")
                        || !traceContains(s, "targetSlot=1")) {
                    throw new IllegalStateException("Expected Phase9Z skill" + skillId
                            + " to cleanse player debuffs and apply buff" + expectedBuff
                            + " without damage, hpBefore=" + hpBefore
                            + " hpAfter=" + s.battlePlayerHp
                            + " postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " playerDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                            + " playerHasBuff=" + runtime.debugPlayerHasBuffForSmoke(expectedBuff)
                            + " activeSlot=" + runtime.debugPlayerActiveBuffSlotForSmoke(expectedBuff)
                            + " blockOk=" + blockOk
                            + " trace=" + tailTrace(s, 60));
                }
            } else if (checkpoint.startsWith("battle_phase9aa_defensive_hook_skill_")) {
                int skillId = parseSkillIdSuffix(checkpoint, "battle_phase9aa_defensive_hook_skill_");
                if (skillId != 34 && skillId != 35) {
                    throw new IllegalArgumentException("Phase9AA only supports defensive hook producer skills 34/35, got "
                            + skillId);
                }
                enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                tickUntilBattleP7Phase(s, 3, 320);
                int expectedBuff = skillId == 34 ? 5 : 6;
                boolean producerOk = s.battleP7PostEffectVisible
                        && s.battleP7PostEffectPlayerSide
                        && !s.battleP7PostEffectText.isEmpty()
                        && runtime.debugPlayerHasBuffForSmoke(expectedBuff)
                        && runtime.debugPlayerActiveBuffSlotForSmoke(expectedBuff) >= 0
                        && runtime.debugPlayerBuffValueForSmoke(expectedBuff) > 0
                        && !traceContains(s, "battle P7 damage frame skill=" + skillId)
                        && !traceContains(s, "battle P7 hitroll skill=" + skillId)
                        && traceContains(s, "battle P7 no-damage skill=" + skillId)
                        && traceContains(s, "game.d.q postEffect skill=" + skillId)
                        && traceContains(s, "buffId=" + expectedBuff)
                        && traceContains(s, "targetSide=1")
                        && traceContains(s, "targetSlot=1");
                boolean hookOk;
                if (skillId == 34) {
                    int reflected = runtime.debugEnemyAttackPlayerReflectHookForSmoke(s);
                    hookOk = reflected > 0
                            && traceContains(s, "Phase9AA buff5 reflect hook skill=34")
                            && traceContains(s, "battle.Phase9AA.buff5Reflect.damage.buff5");
                } else {
                    int reduction = runtime.debugEnemyAttackPlayerBuff6ReductionForSmoke(s);
                    hookOk = reduction > 0
                            && traceContains(s, "Phase9AA buff6 source-odd hook skill=35")
                            && traceContains(s, "battle.Phase9AA.buff6Reduced.damage.buff6");
                }
                if (!producerOk || !hookOk) {
                    throw new IllegalStateException("Expected Phase9AA skill" + skillId
                            + " to apply same-side defensive buff" + expectedBuff
                            + " without damage and prove its defensive hook, producerOk=" + producerOk
                            + " hookOk=" + hookOk
                            + " postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " playerHasBuff=" + runtime.debugPlayerHasBuffForSmoke(expectedBuff)
                            + " activeSlot=" + runtime.debugPlayerActiveBuffSlotForSmoke(expectedBuff)
                            + " buffValue=" + runtime.debugPlayerBuffValueForSmoke(expectedBuff)
                            + " trace=" + tailTrace(s, 70));
                }
            } else if ("battle_elder_p7_q_heal_skill11".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{11, 45}, 0);
                tickUntilBattleP7Phase(s, 3, 180);
                if (!s.battleP7PostEffectVisible || !s.battleP7PostEffectText.startsWith("+")) {
                    throw new IllegalStateException("Expected P7 q heal text, visible="
                            + s.battleP7PostEffectVisible + " text=" + s.battleP7PostEffectText);
                }
            } else if ("battle_elder_p7_q_buff_skill45".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                tickUntilBattleP7Phase(s, 3, 220);
                if (!s.battleP7PostEffectVisible || s.battleP7PostEffectText.isEmpty()) {
                    throw new IllegalStateException("Expected P7 q buff text, visible="
                            + s.battleP7PostEffectVisible + " text=" + s.battleP7PostEffectText);
                }
            } else if ("battle_p7_q_buff12_followup_p2".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{10, 45}, 0);
                tickUntilBattleP7Phase(s, 3, 220);
                ((SourceBattleRuntime) s.current).debugSetPlayerBuff12KForSmoke(s, 2);
                tickUntilBattleState(s, "P2", 120);
                if (!traceContains(s, "follow-up P2 from buff12 K12=2->1")) {
                    throw new IllegalStateException("Expected P7 q buff12 K12 follow-up P2, state="
                            + s.battleStateName + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_phase9ab_skill65_producer_to_followup".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{65, 10}, 0);
                SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
                tickUntilBattleP7Phase(s, 3, 320);
                if (!s.battleP7PostEffectVisible
                        || !s.battleP7PostEffectPlayerSide
                        || !runtime.debugPlayerHasBuffForSmoke(12)
                        || runtime.debugPlayerActiveBuffSlotForSmoke(12) < 0
                        || runtime.debugPlayerK12ForSmoke() != 1
                        || traceContains(s, "battle P7 damage frame skill=65")
                        || traceContains(s, "battle P7 hitroll skill=65")
                        || !traceContains(s, "battle P7 no-damage skill=65")
                        || !traceContains(s, "game.d.q postEffect skill=65")
                        || !traceContains(s, "buffId=12")
                        || !traceContains(s, "targetSide=1")
                        || !traceContains(s, "targetSlot=1")) {
                    throw new IllegalStateException("Expected Phase9AB skill65 producer to apply buff12 K12=1 without damage, "
                            + "postVisible=" + s.battleP7PostEffectVisible
                            + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                            + " postText=" + s.battleP7PostEffectText
                            + " hasBuff12=" + runtime.debugPlayerHasBuffForSmoke(12)
                            + " activeSlot=" + runtime.debugPlayerActiveBuffSlotForSmoke(12)
                            + " K12=" + runtime.debugPlayerK12ForSmoke()
                            + " trace=" + tailTrace(s, 70));
                }
                tickUntilTraceContains(s, "active queue apply bank=0 id=12", 900);
                if (runtime.debugPlayerK12ForSmoke() != 2
                        || !traceContains(s, "battle P13 active queue source order count=")
                        || traceContains(s, "SMOKE battle debug player buff12 K12=")) {
                    throw new IllegalStateException("Expected Phase9AB P13 active queue to promote skill65 buff12 K12=2 without forced helper, "
                            + "K12=" + runtime.debugPlayerK12ForSmoke()
                            + " state=" + s.battleStateName
                            + " trace=" + tailTrace(s, 70));
                }
                tickUntilBattleState(s, "P20", 360);
                runtime.debugSetNextP7HitRollForSmoke(99);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 10; i++) {
                    s.tick();
                }
                s.setMoveKey(KeyEvent.VK_DOWN, true);
                s.tick();
                s.setMoveKey(KeyEvent.VK_DOWN, false);
                s.tick();
                for (int i = 0; i < 20 && !"P7".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleP7Phase(s, 3, 320);
                tickUntilBattleState(s, "P2", 180);
                if (runtime.debugPlayerK12ForSmoke() != 1
                        || !traceContains(s, "battle P3 confirm skill=10")
                        || !traceContains(s, "follow-up P2 from buff12 K12=2->1")) {
                    throw new IllegalStateException("Expected Phase9AB skill65-produced buff12 to drive next skill10 q() follow-up P2, "
                            + "state=" + s.battleStateName
                            + " K12=" + runtime.debugPlayerK12ForSmoke()
                            + " trace=" + tailTrace(s, 90));
                }
            } else if ("battle_p7_q_skill63_followup_p2".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{63, 45}, 0);
                tickUntilBattleP7Phase(s, 3, 260);
                ((SourceBattleRuntime) s.current).debugSetSourceRandomSeedForSmoke(18);
                tickUntilBattleState(s, "P2", 160);
                if (!traceContains(s, "follow-up P2 from skill=63")
                        || !traceContains(s, "RNG TRACE battle.P7.q.followup.skill63")) {
                    throw new IllegalStateException("Expected P7 q skill63 roll follow-up P2, state="
                            + s.battleStateName + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_p13_buff9_queue_start".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                int guard = 0;
                while (!traceContains(s, "active queue apply bank=0 id=9") && guard++ < 360) {
                    s.tick();
                }
                if (!traceContains(s, "active queue apply bank=0 id=9") || s.battleP7SpecialVisible) {
                    throw new IllegalStateException("Expected buff9 active queue to apply without visual per source ai gate, state="
                            + s.battleStateName + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p13_buff9_visual_speffect15".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                int guard = 0;
                while (!traceContains(s, "active queue apply bank=0 id=9") && guard++ < 360) {
                    s.tick();
                }
                if (!traceContains(s, "active queue apply bank=0 id=9") || s.battleP7SpecialVisible) {
                    throw new IllegalStateException("Expected buff9 to have no P12/P13 speffect segment, state="
                            + s.battleStateName + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p13_buff9_after_apply".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                int guard = 0;
                while (!traceContains(s, "active queue apply bank=0 id=9") && guard++ < 360) {
                    s.tick();
                }
                if (isBattleState(s, "P12", "P13") || s.battleActiveQueueVisible) {
                    throw new IllegalStateException("Expected source-skipped buff9 queue to finish, state="
                            + s.battleStateName + " visible=" + s.battleActiveQueueVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p7_to_p13_queue_order_skill45".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                tickUntilTraceContains(s, "active queue apply bank=0 id=9", 420);
                int post = traceIndex(s, "battle P7 game.d.q postEffect skill=45");
                int queue = traceIndex(s, "battle P13 active queue source order count=");
                int apply = traceIndex(s, "active queue apply bank=0 id=9");
                if (post < 0 || queue < 0 || apply < 0 || !(post < queue && queue < apply)) {
                    throw new IllegalStateException("Expected P7 skill45 postEffect before P13 active queue apply, "
                            + "post=" + post + " queue=" + queue + " apply=" + apply
                            + " state=" + s.battleStateName
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_phase10a_status_icons_enemy_debuff1".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupPhase10AStatusBattle(s);
                runtime.debugStatusIconForSmoke(s, false, 1, 1, 3, 12, 2);
                assertPhase10AStatusSlots(s, false, "enemy debuff1",
                        new int[]{2}, new int[]{137});
                assertRenderedVisiblePixels(s, "Phase10A enemy debuff1 icon", 2, 25, 14, 14, 10);
                assertRenderedVisiblePixels(s, "Phase10A enemy debuff1 duration", 10, 30, 10, 10, 2);
            } else if ("battle_phase10a_status_icons_enemy_buff9".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupPhase10AStatusBattle(s);
                runtime.debugStatusIconForSmoke(s, false, 0, 9, 3, 10, 45);
                assertPhase10AStatusSlots(s, false, "enemy buff9",
                        new int[]{21}, new int[]{137});
                assertRenderedVisiblePixels(s, "Phase10A enemy buff9 icon", 2, 25, 14, 14, 10);
                assertRenderedVisiblePixels(s, "Phase10A enemy buff9 duration", 10, 30, 10, 10, 2);
            } else if ("battle_phase10a_status_icons_player_debuff5".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupPhase10AStatusBattle(s);
                runtime.debugStatusIconForSmoke(s, true, 1, 5, 3, 8, 32);
                assertPhase10AStatusSlots(s, true, "player debuff5",
                        new int[]{6}, new int[]{137});
                assertRenderedVisiblePixels(s, "Phase10A player debuff5 icon", 226, 221, 14, 14, 10);
                assertRenderedVisiblePixels(s, "Phase10A player debuff5 duration", 234, 226, 10, 10, 2);
            } else if ("battle_phase10a_status_icons_mixed_order".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupPhase10AStatusBattle(s);
                runtime.debugStatusIconForSmoke(s, false, 0, 9, 3, 10, 45);
                runtime.debugStatusIconForSmoke(s, false, 1, 1, 3, 12, 2);
                runtime.debugStatusIconForSmoke(s, false, 0, 10, 3, 7, 68);
                assertPhase10AStatusSlots(s, false, "enemy mixed order",
                        new int[]{21, 2, 22}, new int[]{137, 137, 137});
                assertRenderedVisiblePixels(s, "Phase10A mixed slot0", 2, 25, 14, 14, 10);
                assertRenderedVisiblePixels(s, "Phase10A mixed slot1", 17, 25, 14, 14, 10);
                assertRenderedVisiblePixels(s, "Phase10A mixed slot2", 32, 25, 14, 14, 10);
            } else if ("battle_p12_debuff0_queue_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{1, 45}, 0);
                tickUntilAnyBattleState(s, 360, "P12", "P13");
                if (s.battleActiveQueueBank != 1 || s.battleActiveQueueEffectId != 0
                        || s.battleActiveQueueSegment != 0 || !s.battleP7SpecialVisible
                        || s.battleP7SpecialType != 9) {
                    throw new IllegalStateException("Expected P12/P13 debuff0 aq[0] speffect18 visible, state="
                            + s.battleStateName + " bank=" + s.battleActiveQueueBank
                            + " id=" + s.battleActiveQueueEffectId
                            + " segment=" + s.battleActiveQueueSegment
                            + " special=" + s.battleP7SpecialVisible
                            + " type=" + s.battleP7SpecialType
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff0_damage_text".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{1, 45}, 0);
                tickUntilAnyBattleState(s, 360, "P12", "P13");
                int guard = 0;
                while (!s.battleP7PostEffectVisible && guard++ < 160) {
                    s.tick();
                }
                if (!s.battleP7PostEffectVisible || !s.battleP7PostEffectText.startsWith("-")) {
                    throw new IllegalStateException("Expected P12/P13 debuff0 negative HP delta text, state="
                            + s.battleStateName + " visible=" + s.battleP7PostEffectVisible
                            + " text=" + s.battleP7PostEffectText
                            + " bank=" + s.battleActiveQueueBank
                            + " id=" + s.battleActiveQueueEffectId
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff0_after_apply".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{1, 45}, 0);
                tickUntilAnyBattleState(s, 360, "P12", "P13");
                int guard = 0;
                while ((isBattleState(s, "P12", "P13") || s.battleActiveQueueVisible) && guard++ < 220) {
                    s.tick();
                }
                if (isBattleState(s, "P12", "P13") || s.battleActiveQueueVisible) {
                    throw new IllegalStateException("Expected P12/P13 debuff0 to finish, state="
                            + s.battleStateName + " visible=" + s.battleActiveQueueVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p7_to_p12_queue_order_debuff0".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{1, 45}, 0);
                tickUntilTraceContains(s, "active queue apply bank=1 id=0", 520);
                int damage = traceIndex(s, "battle P7 damage frame skill=1");
                int queue = traceIndex(s, "battle P12 active queue source order count=");
                int apply = traceIndex(s, "active queue apply bank=1 id=0");
                if (damage < 0 || queue < 0 || apply < 0 || !(damage < queue && queue < apply)) {
                    throw new IllegalStateException("Expected P7 skill1 debuff damage before P12 active queue apply, "
                            + "damage=" + damage + " queue=" + queue + " apply=" + apply
                            + " state=" + s.battleStateName
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_p12_debuff1_type12_special".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 0, 6, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 1, 0, 1, 3, s.battleEnemyHp);
                tickUntilAnyBattleState(s, 120, "P12");
                int guard = 0;
                while ((!s.battleP7SpecialVisible || s.battleP7SpecialType != 12) && guard++ < 120) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 12
                        || s.battleP7SpecialRow.length == 0) {
                    throw new IllegalStateException("Expected debuff1 active queue H speffect14 AH type12, state="
                            + s.battleStateName + " special=" + s.battleP7SpecialVisible
                            + " type=" + s.battleP7SpecialType
                            + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff2_type8_special".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 0, 6, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 2, 0, 1, 3, s.battleEnemyHp);
                tickUntilAnyBattleState(s, 120, "P12");
                int guard = 0;
                while ((!s.battleP7SpecialVisible || s.battleP7SpecialType != 8) && guard++ < 160) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 8
                        || s.battleP7SpecialRow.length == 0
                        || !traceContains(s, "speffect=6")) {
                    throw new IllegalStateException("Expected debuff2 active queue H speffect6 AH type8 after type0 trigger, state="
                            + s.battleStateName + " special=" + s.battleP7SpecialVisible
                            + " type=" + s.battleP7SpecialType
                            + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff3_queue_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{13, 45}, 0);
                tickUntilAnyBattleState(s, 360, "P12", "P13");
                if (s.battleActiveQueueBank != 1 || s.battleActiveQueueEffectId != 3
                        || s.battleActiveQueueSegment != 0
                        || !s.battleP7ActorEffectVisible
                        || s.battleP7ActorEffectSpriteId != 263
                        || s.battleP7ActorEffectState != 0) {
                    throw new IllegalStateException("Expected P12/P13 debuff3 ah actor action row [0,21,0,-1], state="
                            + s.battleStateName + " bank=" + s.battleActiveQueueBank
                            + " id=" + s.battleActiveQueueEffectId
                            + " actorVisible=" + s.battleP7ActorEffectVisible
                            + " actorSprite=" + s.battleP7ActorEffectSpriteId
                            + " actorState=" + s.battleP7ActorEffectState
                            + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff3_type0_actor_mid".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{13, 45}, 0);
                tickUntilAnyBattleState(s, 360, "P12", "P13");
                int guard = 0;
                while (s.battleP7ActorEffectCursor == 0 && guard++ < 80) {
                    s.tick();
                }
                if (!s.battleP7ActorEffectVisible || s.battleP7ActorEffectSpriteId != 263
                        || s.battleP7ActorEffectCursor <= 0) {
                    throw new IllegalStateException("Expected type0 ah actor action to tick cursor, state="
                            + s.battleStateName
                            + " actorVisible=" + s.battleP7ActorEffectVisible
                            + " actorSprite=" + s.battleP7ActorEffectSpriteId
                            + " cursor=" + s.battleP7ActorEffectCursor
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff3_after_apply".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{13, 45}, 0);
                tickUntilAnyBattleState(s, 360, "P12", "P13");
                int guard = 0;
                while ((isBattleState(s, "P12", "P13") || s.battleActiveQueueVisible) && guard++ < 220) {
                    s.tick();
                }
                if (isBattleState(s, "P12", "P13") || s.battleActiveQueueVisible
                        || !traceContains(s, "active queue apply bank=1 id=3")) {
                    throw new IllegalStateException("Expected P12/P13 debuff3 to apply and finish, state="
                            + s.battleStateName + " visible=" + s.battleActiveQueueVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_debuff5_stat_skip_visual".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{32, 45}, 0);
                int guard = 0;
                while (!traceContains(s, "active queue apply bank=1 id=5") && guard++ < 420) {
                    s.tick();
                }
                if (!traceContains(s, "active queue apply bank=1 id=5") || s.battleP7SpecialVisible) {
                    throw new IllegalStateException("Expected debuff5 stat tick without visual per source ai gate, state="
                            + s.battleStateName + " special=" + s.battleP7SpecialVisible
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p12_queue_death_to_p8".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 0, 6, 3, 2, 1, 45));
                s.current = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P7", 120);
                int guard = 0;
                while (!"P8".equals(s.battleStateName) && guard++ < 520) {
                    s.tick();
                }
                if (!"P8".equals(s.battleStateName) || !traceContains(s, "active queue apply bank=1 id=0")) {
                    throw new IllegalStateException("Expected P12 debuff0 tick death to route P8, state="
                            + s.battleStateName + " enemyHp=" + s.battleEnemyHp
                            + " trace=" + tailTrace(s, 14));
                }
            } else if ("battle_levelup_evolution_queue_created".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 6, 11, 3, 2, 0, -1);
                pet.sourcePayload[7] = BattleUnit.sourceLevelThreshold(12) - 10;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                int guard = 0;
                while ((s.sourceEvolutionQueue.isEmpty()
                        || !traceContains(s, "game.b.J evolution queue species=6 target=7"))
                        && guard++ < 680) {
                    s.tick();
                }
                if (s.sourceEvolutionQueue.size() != 1
                        || s.sourceEvolutionL[0] != 12
                        || s.sourceEvolutionL[1] != 6
                        || s.sourceEvolutionI != 0
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || !s.battleLevelUpView.leveled) {
                    throw new IllegalStateException("Expected evolution queue producer only, queue="
                            + s.sourceEvolutionQueue.size()
                            + " L=" + java.util.Arrays.toString(s.sourceEvolutionL)
                            + " I=" + s.sourceEvolutionI
                            + " mode=" + s.battleUiMode
                            + " trace=" + tailTrace(s, 20));
                }
                SourceEvolutionNotice notice = s.sourceEvolutionQueue.get(0);
                if (notice.targetSpeciesId != 7
                        || notice.requiredLevel != 12
                        || notice.sourceR != 1
                        || notice.currentNameTextId != 16
                        || notice.targetNameTextId != 17) {
                    throw new IllegalStateException("Bad evolution notice species="
                            + notice.currentSpeciesId + " target=" + notice.targetSpeciesId
                            + " required=" + notice.requiredLevel
                            + " sourceR=" + notice.sourceR
                            + " names=" + notice.currentNameTextId + "/" + notice.targetNameTextId);
                }
                s.sourceStateTrace.add("SMOKE verified battle P22 evolution queue producer species=6 target=7"
                        + " L=" + java.util.Arrays.toString(s.sourceEvolutionL));
            } else if ("world_evolution_notice_after_levelup".equals(checkpoint)
                    || "world_evolution_notice_queue_exhausted".equals(checkpoint)
                    || "world_evolution_tutorial_petstate_bridge".equals(checkpoint)
                    || "world_evolution_evolve_ui_open".equals(checkpoint)
                    || "world_evolution_confirm_success_mutate".equals(checkpoint)
                    || "world_evolution_after_success_continue".equals(checkpoint)
                    || "world_evolution_confirm_no_material".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 6, 11, 3, 2, 0, -1);
                pet.sourcePayload[7] = BattleUnit.sourceLevelThreshold(12) - 10;
                s.sourcePets.add(pet);
                if ("world_evolution_confirm_success_mutate".equals(checkpoint)
                        || "world_evolution_after_success_continue".equals(checkpoint)) {
                    SourceSpecialReward material = s.sourceSpecialRewards.computeIfAbsent(12, SourceSpecialReward::fromSourceDb);
                    material.stackCount = 1;
                }
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                int guard = 0;
                while (s.current != null && guard++ < 1200) {
                    if (s.text != null && s.text.readyForKey) {
                        s.press0();
                    }
                    if ("levelup".equals(s.battleUiMode)
                            || "warning".equals(s.battleUiMode)
                            || "choiceskill".equals(s.battleUiMode)) {
                        s.press0();
                    }
                    s.tick();
                }
                if (s.current != null || s.sourceEvolutionQueue.isEmpty()
                        || s.sourceEvolutionI != 0) {
                    throw new IllegalStateException("Expected battle complete with pending evolution queue, current="
                            + (s.current == null ? "none" : s.current.getClass().getSimpleName())
                            + " queue=" + s.sourceEvolutionQueue.size()
                            + " I=" + s.sourceEvolutionI
                            + " trace=" + tailTrace(s, 20));
                }
                if ("world_evolution_notice_queue_exhausted".equals(checkpoint)) {
                    s.sourceEvolutionL[0] = -1;
                    s.sourceEvolutionL[1] = -1;
                    s.sourceStateTrace.add("SMOKE setup no-tutorial game.k.H drain: clear L before notice");
                }
                guard = 0;
                while ((s.text == null || !traceContains(s, "game.k evolution notice consume"))
                        && guard++ < 90) {
                    s.tick();
                }
                if (s.text == null
                        || s.sourceEvolutionNoticeIndex != 1
                        || s.sourceEvolutionQueue.size() != 1
                        || s.sourceEvolutionI != 0
                        || !traceContains(s, "game.k evolution notice consume ac=0 species=6 target=7")) {
                    throw new IllegalStateException("Expected world evolution notice consumer, text="
                            + (s.text == null ? "none" : s.text.currentText())
                            + " ac=" + s.sourceEvolutionNoticeIndex
                            + " queue=" + s.sourceEvolutionQueue.size()
                            + " I=" + s.sourceEvolutionI
                            + " trace=" + tailTrace(s, 20));
                }
                revealCheckpointText(s, 90);
                if ("world_evolution_notice_queue_exhausted".equals(checkpoint)) {
                    guard = 0;
                    while ((s.sourceEvolutionI != 1 || !s.sourceEvolutionQueue.isEmpty())
                            && guard++ < 120) {
                        if (s.text != null && s.text.readyForKey) {
                            s.press0();
                        }
                        s.tick();
                    }
                    if (s.sourceEvolutionI != 1 || !s.sourceEvolutionQueue.isEmpty()
                            || s.sourceEvolutionNoticeIndex != 0
                            || !traceContains(s, "evolution notice queue exhausted")) {
                        throw new IllegalStateException("Expected evolution queue exhausted, queue="
                                + s.sourceEvolutionQueue.size()
                                + " ac=" + s.sourceEvolutionNoticeIndex
                                + " I=" + s.sourceEvolutionI
                                + " trace=" + tailTrace(s, 20));
                    }
                }
                s.sourceStateTrace.add("SMOKE verified game.k.H/L/I Slice2 world notice consumer"
                        + " queue=" + s.sourceEvolutionQueue.size()
                        + " ac=" + s.sourceEvolutionNoticeIndex
                        + " I=" + s.sourceEvolutionI);
                if ("world_evolution_tutorial_petstate_bridge".equals(checkpoint)
                        || "world_evolution_evolve_ui_open".equals(checkpoint)
                        || "world_evolution_confirm_success_mutate".equals(checkpoint)
                        || "world_evolution_after_success_continue".equals(checkpoint)
                        || "world_evolution_confirm_no_material".equals(checkpoint)) {
                    guard = 0;
                    while (s.current != null && guard++ < 80) {
                        if (s.text != null && s.text.readyForKey) {
                            s.press0();
                        }
                        s.tick();
                    }
                    guard = 0;
                    while (!s.worldPetstateVisible && guard++ < 80) {
                        s.press0();
                        s.tick();
                    }
                    if (!s.worldPetstateVisible || !s.sourceEvolutionK
                            || s.sourceEvolutionTutorialU != 4 || s.battleMenuIndex != 0) {
                        throw new IllegalStateException("Expected evolution tutorial petstate bridge, visible="
                                + s.worldPetstateVisible
                                + " K=" + s.sourceEvolutionK
                                + " U=" + s.sourceEvolutionTutorialU
                                + " index=" + s.battleMenuIndex
                                + " trace=" + tailTrace(s, 20));
                    }
                    if ("world_evolution_evolve_ui_open".equals(checkpoint)
                            || "world_evolution_confirm_success_mutate".equals(checkpoint)
                            || "world_evolution_after_success_continue".equals(checkpoint)
                            || "world_evolution_confirm_no_material".equals(checkpoint)) {
                        s.press0();
                        s.tick();
                        if (!s.sourceEvolveVisible || s.sourceEvolveNotice == null
                                || s.sourceEvolveNotice.targetSpeciesId != 7) {
                            throw new IllegalStateException("Expected evolve.ui open, visible="
                                    + s.sourceEvolveVisible
                                    + " notice=" + (s.sourceEvolveNotice == null ? "null"
                                    : s.sourceEvolveNotice.currentSpeciesId + "->" + s.sourceEvolveNotice.targetSpeciesId)
                                    + " trace=" + tailTrace(s, 20));
                        }
                    }
                    if ("world_evolution_confirm_success_mutate".equals(checkpoint)
                            || "world_evolution_after_success_continue".equals(checkpoint)) {
                        s.press0();
                        guard = 0;
                        while ((s.text == null || s.sourceEvolvePhase != 2) && guard++ < 260) {
                            s.tick();
                        }
                        int materialLeft = VqsvSourceEvolutionRuntime.materialCount(s, 12);
                        if (s.sourcePets.get(0).speciesId != 7 || materialLeft != 0
                                || !s.sourceEvolveVisible
                                || s.text == null
                                || !traceContains(s, "game.h.bh mutate pet index=0 species=6->7")) {
                            throw new IllegalStateException("Expected evolution success mutate, species="
                                    + s.sourcePets.get(0).speciesId
                                    + " material=" + materialLeft
                                    + " visible=" + s.sourceEvolveVisible
                                    + " trace=" + tailTrace(s, 24));
                        }
                        s.text.sourceTextOffset = 70;
                        if ("world_evolution_after_success_continue".equals(checkpoint)) {
                            revealCheckpointText(s, 90);
                            s.press0();
                            s.tick();
                            s.tick();
                            if (!s.sourceEvolveVisible || s.text != null
                                    || !s.sourceEvolveSucceeded
                                    || s.sourcePets.get(0).speciesId != 7
                                    || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 0) {
                                throw new IllegalStateException("Expected continue after evolution success to return to evolve.ui, visible="
                                        + s.sourceEvolveVisible
                                        + " text=" + (s.text == null ? "none" : s.text.currentText())
                                        + " succeeded=" + s.sourceEvolveSucceeded
                                        + " species=" + s.sourcePets.get(0).speciesId
                                        + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                                        + " trace=" + tailTrace(s, 24));
                            }
                            s.sourceStateTrace.add("SMOKE verified game.h.bh success continue closes msgwarm and keeps evolve.ui");
                        }
                    } else if ("world_evolution_confirm_no_material".equals(checkpoint)) {
                        s.press0();
                        guard = 0;
                        while ((s.text == null || !s.text.readyForKey) && guard++ < 120) {
                            s.tick();
                        }
                        if (s.text == null
                                || !s.text.currentText().contains(VqsvText.Evolution.MATERIAL_MISSING_EVOLVE)
                                || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 0
                                || s.sourcePets.get(0).speciesId != 6) {
                            throw new IllegalStateException("Expected no-material msgwarm without mutation, text="
                                    + (s.text == null ? "none" : s.text.currentText())
                                    + " species=" + s.sourcePets.get(0).speciesId
                                    + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                                    + " trace=" + tailTrace(s, 24));
                        }
                        s.text.sourceTextOffset = 70;
                        s.sourceStateTrace.add("SMOKE verified game.h.bh no-material validation");
                    }
                }
            } else if ("world_evolution_confirm_level_low".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 6, 11, 3, 2, 0, -1);
                s.sourcePets.add(pet);
                SourceSpecialReward material = s.sourceSpecialRewards.computeIfAbsent(12, SourceSpecialReward::fromSourceDb);
                material.stackCount = 1;
                s.sourceEvolutionL[0] = 11;
                s.sourceEvolutionL[1] = 6;
                s.sourceEvolutionTutorialPending = true;
                int guard = 0;
                while (!s.worldPetstateVisible && guard++ < 80) {
                    s.press0();
                    s.tick();
                }
                if (!s.worldPetstateVisible || s.battleMenuIndex != 0) {
                    throw new IllegalStateException("Expected manual U=4 bridge for level-low smoke, visible="
                            + s.worldPetstateVisible
                            + " index=" + s.battleMenuIndex
                            + " trace=" + tailTrace(s, 20));
                }
                s.press0();
                s.tick();
                if (!s.sourceEvolveVisible || s.sourceEvolveNotice == null) {
                    throw new IllegalStateException("Expected evolve.ui open for level-low smoke, visible="
                            + s.sourceEvolveVisible
                            + " trace=" + tailTrace(s, 20));
                }
                s.press0();
                guard = 0;
                while ((s.text == null || !s.text.readyForKey) && guard++ < 120) {
                    s.tick();
                }
                String expected = VqsvText.Evolution.levelTooLow(12);
                if (s.text == null
                        || !s.text.currentText().contains(expected)
                        || s.sourcePets.get(0).speciesId != 6
                        || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 1) {
                    throw new IllegalStateException("Expected level-low msgwarm without mutation, text="
                            + (s.text == null ? "none" : s.text.currentText())
                            + " species=" + s.sourcePets.get(0).speciesId
                            + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                            + " trace=" + tailTrace(s, 24));
                }
                s.text.sourceTextOffset = 70;
                s.sourceStateTrace.add("SMOKE verified game.h.bh level-low validation");
            } else if ("world_evolution_no_next_target_warning".equals(checkpoint)
                    || "world_evolution_no_next_target_after_warning_continue".equals(checkpoint)) {
                openEvolutionUiForSmoke(s, 8, 12, -1, 0);
                if (s.sourceEvolveNotice != null) {
                    throw new IllegalStateException("Expected species 8 to have no next target, notice="
                            + s.sourceEvolveNotice.currentSpeciesId + "->" + s.sourceEvolveNotice.targetSpeciesId);
                }
                s.press0();
                int guard = 0;
                while ((s.text == null || !s.text.readyForKey) && guard++ < 120) {
                    s.tick();
                }
                if (s.text == null
                        || !s.text.currentText().contains(VqsvText.Evolution.CANNOT_EVOLVE)
                        || s.sourcePets.get(0).speciesId != 8) {
                    throw new IllegalStateException("Expected no-next-target msgwarm without mutation, text="
                            + (s.text == null ? "none" : s.text.currentText())
                            + " species=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 24));
                }
                s.text.sourceTextOffset = 70;
                if ("world_evolution_no_next_target_after_warning_continue".equals(checkpoint)) {
                    s.press0();
                    s.tick();
                    s.tick();
                    if (!s.sourceEvolveVisible || s.text != null || s.sourceEvolvePhase != 0
                            || s.sourcePets.get(0).speciesId != 8) {
                        throw new IllegalStateException("Expected no-target warning continue to return evolve.ui, visible="
                                + s.sourceEvolveVisible
                                + " text=" + (s.text == null ? "none" : s.text.currentText())
                                + " phase=" + s.sourceEvolvePhase
                                + " species=" + s.sourcePets.get(0).speciesId
                                + " trace=" + tailTrace(s, 24));
                    }
                }
                s.sourceStateTrace.add("SMOKE verified game.h.bh no-next-target warning path");
            } else if ("world_evolution_confirm_no_material_after_warning_continue".equals(checkpoint)) {
                openEvolutionUiForSmoke(s, 6, 12, 12, 0);
                s.press0();
                int guard = 0;
                while ((s.text == null || !s.text.readyForKey) && guard++ < 120) {
                    s.tick();
                }
                if (s.text == null
                        || !s.text.currentText().contains(VqsvText.Evolution.MATERIAL_MISSING_EVOLVE)) {
                    throw new IllegalStateException("Expected no-material warning before continue, text="
                            + (s.text == null ? "none" : s.text.currentText())
                            + " trace=" + tailTrace(s, 24));
                }
                s.press0();
                s.tick();
                s.tick();
                if (!s.sourceEvolveVisible || s.text != null || s.sourceEvolvePhase != 0
                        || s.sourcePets.get(0).speciesId != 6
                        || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 0) {
                    throw new IllegalStateException("Expected no-material continue to return evolve.ui, visible="
                            + s.sourceEvolveVisible
                            + " text=" + (s.text == null ? "none" : s.text.currentText())
                            + " phase=" + s.sourceEvolvePhase
                            + " species=" + s.sourcePets.get(0).speciesId
                            + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified game.h.bh no-material warning continue");
            } else if ("world_evolution_confirm_level_low_after_warning_continue".equals(checkpoint)) {
                openEvolutionUiForSmoke(s, 6, 11, 12, 1);
                s.press0();
                int guard = 0;
                while ((s.text == null || !s.text.readyForKey) && guard++ < 120) {
                    s.tick();
                }
                String expected = VqsvText.Evolution.levelTooLow(12);
                if (s.text == null || !s.text.currentText().contains(expected)) {
                    throw new IllegalStateException("Expected level-low warning before continue, text="
                            + (s.text == null ? "none" : s.text.currentText())
                            + " trace=" + tailTrace(s, 24));
                }
                s.press0();
                s.tick();
                s.tick();
                if (!s.sourceEvolveVisible || s.text != null || s.sourceEvolvePhase != 0
                        || s.sourcePets.get(0).speciesId != 6
                        || VqsvSourceEvolutionRuntime.materialCount(s, 12) != 1) {
                    throw new IllegalStateException("Expected level-low continue to return evolve.ui, visible="
                            + s.sourceEvolveVisible
                            + " text=" + (s.text == null ? "none" : s.text.currentText())
                            + " phase=" + s.sourceEvolvePhase
                            + " species=" + s.sourcePets.get(0).speciesId
                            + " material=" + VqsvSourceEvolutionRuntime.materialCount(s, 12)
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified game.h.bh level-low warning continue");
            } else if ("world_evolution_back_from_evolve_ui".equals(checkpoint)) {
                openEvolutionUiForSmoke(s, 6, 12, 12, 1);
                s.keyBack = true;
                s.tick();
                if (s.sourceEvolveVisible || s.text != null
                        || s.sourceEvolutionL[0] != -1
                        || s.sourceEvolutionTutorialU != -1) {
                    throw new IllegalStateException("Expected back from evolve.ui to close overlay, visible="
                            + s.sourceEvolveVisible
                            + " text=" + (s.text == null ? "none" : s.text.currentText())
                            + " L=" + java.util.Arrays.toString(s.sourceEvolutionL)
                            + " U=" + s.sourceEvolutionTutorialU
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified game.h.bh back closes evolve.ui");
            } else if ("battle_exp_p8_frame0".equals(checkpoint)
                    || "battle_exp_p8_frame1".equals(checkpoint)
                    || "battle_exp_p8_mid".equals(checkpoint)
                    || "battle_exp_p8_target_hold".equals(checkpoint)) {
                handleBattleExpP8AnimationCheckpoint(s, checkpoint);
            } else if ("battle_exp_normal_gain_no_levelup_anim".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.sourcePayload[7] = 0;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                int expected = sourceExpectedExpAward(5, 1, 7, 1);
                int threshold = BattleUnit.sourceLevelThreshold(8);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                int guard = 0;
                while ((!"P8".equals(s.battleStateName)
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || s.battleLevelUpView.leveled)
                        && guard++ < 360) {
                    s.tick();
                }
                if (!"P8".equals(s.battleStateName)
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || s.battleLevelUpView.leveled
                        || s.battleLevelUpView.expValue != 0
                        || s.battleLevelUpView.expValue > expected
                        || s.sourcePets.get(0).level != 7
                        || expected >= threshold
                        || traceContains(s, "P22 game.h.an/ao levelUp")
                        || !traceContains(s, "game.h.a initial render before game.h.am")) {
                    throw new IllegalStateException("Expected normal EXP gain animation without level-up"
                            + " state=" + s.battleStateName
                            + " mode=" + s.battleUiMode
                            + " level=" + s.sourcePets.get(0).level
                            + " expValue=" + (s.battleLevelUpView == null ? -1 : s.battleLevelUpView.expValue)
                            + " expected=" + expected
                            + " threshold=" + threshold
                            + " trace=" + tailTrace(s, 28));
                }
                assertSourcePetExp(pet, expected, "normal no-level EXP commit");
                assertP8ExpPosMidRow0(s, "normal no-level EXP pos.mid");
                s.sourceStateTrace.add("SMOKE verified normal P8 EXP animation no level-up"
                        + " expected=" + expected
                        + " threshold=" + threshold
                        + " expValue=" + s.battleLevelUpView.expValue);
            } else if ("battle_exp_levelup_ui".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 0, 5, 3, 2, 1, 45);
                pet.sourcePayload[7] = BattleUnit.sourceLevelThreshold(6) - 10;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                int guard = 0;
                while ((!"P8".equals(s.battleStateName)
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || !s.battleLevelUpView.leveled)
                        && guard++ < 620) {
                    s.tick();
                }
                if (!"P8".equals(s.battleStateName)
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || !s.battleLevelUpView.leveled
                        || s.sourcePets.get(0).level <= 5) {
                    throw new IllegalStateException("Expected P8 levelUp UI, state="
                            + s.battleStateName + " mode=" + s.battleUiMode
                            + " level=" + s.sourcePets.get(0).level
                            + " view=" + (s.battleLevelUpView == null ? "null" : s.battleLevelUpView.visible)
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_exp_p8_confirm_fast_forward".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.sourcePayload[7] = 0;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                int expected = sourceExpectedExpAward(5, 1, 7, 1);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                int guard = 0;
                while ((!"P8".equals(s.battleStateName)
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || s.battleLevelUpView.expValue <= 0
                        || s.battleLevelUpView.expValue >= expected)
                        && guard++ < 300) {
                    s.tick();
                }
                if (!"P8".equals(s.battleStateName)
                        || s.battleLevelUpView == null
                        || s.battleLevelUpView.expValue <= 0
                        || s.battleLevelUpView.expValue >= expected) {
                    throw new IllegalStateException("Expected partial P8 EXP before fast-forward"
                            + " value=" + (s.battleLevelUpView == null ? -1 : s.battleLevelUpView.expValue)
                            + " expected=" + expected
                            + " trace=" + tailTrace(s, 18));
                }
                s.press0();
                s.tick();
                if (s.battleLevelUpView == null
                        || s.battleLevelUpView.expValue != expected
                        || !traceContains(s, "game.h.am confirm fast-forward")) {
                    throw new IllegalStateException("Expected P8 confirm fast-forward to target"
                            + " value=" + (s.battleLevelUpView == null ? -1 : s.battleLevelUpView.expValue)
                            + " expected=" + expected
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified P8 confirm fast-forward expected=" + expected);
            } else if ("battle_exp_levelup_choiceskill_ui".equals(checkpoint)
                    || "battle_exp_levelup_learn_skill_done".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 0, 10, 3, 2, 0, -1);
                pet.sourcePayload[7] = BattleUnit.sourceLevelThreshold(11) - 10;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                int guard = 0;
                while ((!"P8".equals(s.battleStateName)
                        || !"levelup".equals(s.battleUiMode)
                        || s.battleLevelUpView == null
                        || !s.battleLevelUpView.leveled)
                        && guard++ < 620) {
                    s.tick();
                }
                s.press0();
                guard = 0;
                while (!"choiceskill".equals(s.battleUiMode) && guard++ < 80) {
                    s.tick();
                }
                if (!"choiceskill".equals(s.battleUiMode)
                        || s.battleSkillIds.length == 0
                        || !traceContains(s, "P23 game.h.ap choiceskill.ui")) {
                    throw new IllegalStateException("Expected level-up choiceskill UI, mode="
                            + s.battleUiMode + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                            + " trace=" + tailTrace(s, 18));
                }
                if ("battle_exp_levelup_learn_skill_done".equals(checkpoint)) {
                    int learnedSkill = s.battleSkillIds[0];
                    s.press0();
                    guard = 0;
                    while (!"warning".equals(s.battleUiMode) && guard++ < 40) {
                        s.tick();
                    }
                    s.press0();
                    guard = 0;
                    while (!traceContains(s, "P23 game.h.aq learn skill=" + learnedSkill)
                            && guard++ < 80) {
                        s.tick();
                    }
                    if (!sourcePetHasSkill(s.sourcePets.get(0), learnedSkill)) {
                        throw new IllegalStateException("Expected learned skill in payload skill="
                                + learnedSkill + " payload="
                                + java.util.Arrays.toString(s.sourcePets.get(0).sourcePayload)
                                + " trace=" + tailTrace(s, 18));
                    }
                }
            } else if ("battle_exp_vector_active_only_regression".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.sourcePayload[7] = 0;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                int expected = sourceExpectedExpAward(5, 1, 7, 1);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                tickUntilTraceContains(s, "battle P8 game.h.a select game.d.j index=0/1", 760);
                assertSourcePetExp(pet, expected, "active-only EXP vector");
                if (!traceContains(s, "reason=battle entry active f[0]")
                        || !traceContains(s, "participants=1")
                        || traceCount(s, "battle P8 game.d.X commit B->S") != 1) {
                    throw new IllegalStateException("Expected active-only EXP vector traces, exp="
                            + sourcePetExp(pet) + " expected=" + expected
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified SliceA active-only EXP vector expected=" + expected);
            } else if ("battle_exp_vector_p5_switch_two_participants".equals(checkpoint)
                    || "battle_exp_vector_j_iterates_second_pet".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState petA = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                SourcePetState petB = new SourcePetState(1, 92, 5, 3, 2, 10, 45);
                if ("battle_exp_vector_j_iterates_second_pet".equals(checkpoint)) {
                    petA.sourcePayload[7] = BattleUnit.sourceLevelThreshold(8) - 10;
                } else {
                    petA.sourcePayload[7] = 0;
                }
                petB.sourcePayload[7] = 0;
                s.sourcePets.add(petA);
                s.sourcePets.add(petB);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                drivePetCommandToP5(s);
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 120, "P1", "WARN");
                if (!"P1".equals(s.battleStateName) || s.sourcePets.get(0) != petB) {
                    throw new IllegalStateException("Expected P5 switch before EXP vector test, state="
                            + s.battleStateName
                            + " slot0Species=" + (s.sourcePets.isEmpty() ? -1 : s.sourcePets.get(0).speciesId)
                            + " trace=" + tailTrace(s, 18));
                }
                int expectedA = sourceExpectedExpAward(5, 1, 7, 2);
                int expectedB = sourceExpectedExpAward(5, 1, 5, 2);
                int startA = sourcePetExp(petA);
                int startB = sourcePetExp(petB);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                tickUntilTraceContains(s, "battle P8 game.h.a select game.d.j index=0/2", 760);
                tickUntilTraceContains(s, "battle P8 game.h.a select game.d.j index=1/2", 760);
                int finalExpectedA = startA + expectedA;
                if ("battle_exp_vector_j_iterates_second_pet".equals(checkpoint)) {
                    finalExpectedA -= BattleUnit.sourceLevelThreshold(8);
                }
                assertSourcePetExp(petA, finalExpectedA, "P5 switched share petA");
                assertSourcePetExp(petB, startB + expectedB, "P5 switched share petB");
                if (!traceContains(s, "reason=battle entry active f[0]")
                        || !traceContains(s, "reason=P5 game.d.a(slot) switched-in pet")
                        || traceCount(s, "participants=2") < 2
                        || traceCount(s, "battle P8 game.d.X commit B->S") != 2) {
                    throw new IllegalStateException("Expected two-participant EXP vector traces"
                            + " expA=" + sourcePetExp(petA) + " expectedA=" + finalExpectedA
                            + " expB=" + sourcePetExp(petB) + " expectedB=" + (startB + expectedB)
                            + " trace=" + tailTrace(s, 32));
                }
                if ("battle_exp_vector_j_iterates_second_pet".equals(checkpoint)
                        && (!traceContains(s, "battle P8 finish game.d.j index=0/2")
                        || petA.level <= 7)) {
                    throw new IllegalStateException("Expected first j pet to finish/level then select second"
                            + " levelA=" + petA.level
                            + " trace=" + tailTrace(s, 32));
                }
                s.sourceStateTrace.add("SMOKE verified SliceA two-participant EXP vector"
                        + " expectedA=" + expectedA + " expectedB=" + expectedB
                        + " checkpoint=" + checkpoint);
            } else if ("battle_exp_vector_participant_form5_multiplier".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.sourcePayload[2] = 5;
                pet.sourcePayload[7] = 0;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                int expected = sourceExpectedExpAward(5, 1, 7, 1);
                expected = expected * (sourceExpectedStatusParam(5, 5, 0) + 100) / 100;
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                tickUntilTraceContains(s, "form5Multiplier=true", 760);
                tickUntilTraceContains(s, "battle P8 game.h.a select game.d.j index=0/1", 760);
                assertSourcePetExp(pet, expected, "SliceB participant f(5) multiplier");
                s.sourceStateTrace.add("SMOKE verified SliceB participant f(5) multiplier expected=" + expected);
            } else if ("battle_exp_vector_reserve_form6_share".equals(checkpoint)
                    || "battle_exp_vector_global_state7_share".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState petA = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                SourcePetState petB = new SourcePetState(1, 92, 5, 3, 2, 10, 45);
                petA.sourcePayload[7] = 0;
                petB.sourcePayload[7] = 0;
                if ("battle_exp_vector_reserve_form6_share".equals(checkpoint)) {
                    petB.sourcePayload[2] = 6;
                } else {
                    s.sourceGlobalState[7][0] = 2;
                }
                s.sourcePets.add(petA);
                s.sourcePets.add(petB);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                int expectedA = sourceExpectedExpAward(5, 1, 7, 1);
                int divisor = "battle_exp_vector_global_state7_share".equals(checkpoint) ? 3000 : 1000;
                int expectedB = sourceExpectedExpAward(5, 1, 7, 1, divisor);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                tickUntilTraceContains(s, "battle P8 game.h.a select game.d.j index=0/2", 760);
                tickUntilTraceContains(s, "battle P8 game.h.a select game.d.j index=1/2", 760);
                assertSourcePetExp(petA, expectedA, "SliceB direct participant baseline");
                assertSourcePetExp(petB, expectedB, "SliceB reserve share");
                String expectedReason = "battle_exp_vector_global_state7_share".equals(checkpoint)
                        ? "reason=game.g.B[7][0]==2"
                        : "reason=reserve f(6)";
                if (!traceContains(s, expectedReason)
                        || !traceContains(s, "levelFactorFromLastX=7")
                        || traceCount(s, "battle P8 game.d.X commit B->S") != 2) {
                    throw new IllegalStateException("Expected SliceB reserve/share traces"
                            + " checkpoint=" + checkpoint
                            + " expA=" + sourcePetExp(petA) + " expectedA=" + expectedA
                            + " expB=" + sourcePetExp(petB) + " expectedB=" + expectedB
                            + " trace=" + tailTrace(s, 32));
                }
                s.sourceStateTrace.add("SMOKE verified SliceB reserve/share expectedA="
                        + expectedA + " expectedB=" + expectedB + " checkpoint=" + checkpoint);
            } else if ("battle_exp_consumer_x_clears_active_marker".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.sourcePayload[7] = 0;
                pet.sourceD(true);
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                tickUntilTraceContains(s, "sourceD=false", 760);
                if (pet.sourceK()) {
                    throw new IllegalStateException("Expected game.b.d(false) sourceActive clear"
                            + " trace=" + tailTrace(s, 24));
                }
                s.sourceStateTrace.add("SMOKE verified SliceC game.d.X clears sourceActive");
            } else if ("battle_exp_consumer_x_removes_dead_j".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.sourcePayload[6] = 0;
                pet.sourcePendingExp = 12;
                pet.sourceExpDisplay = true;
                s.sourcePets.add(pet);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                runtime.debugInjectSourceExpDisplayForSmoke(pet, 12);
                runtime.debugRunSourceExpConsumerForSmoke(s);
                if (pet.sourcePendingExp != 0 || pet.sourceExpDisplay
                        || !traceContains(s, "game.d.X remove dead game.d.j")) {
                    throw new IllegalStateException("Expected dead game.d.j entry removal"
                            + " pending=" + pet.sourcePendingExp
                            + " display=" + pet.sourceExpDisplay
                            + " trace=" + tailTrace(s, 24));
                }
                s.current = runtime;
                s.sourceStateTrace.add("SMOKE verified SliceC game.d.X removes dead j entry");
            } else if ("battle_exp_consumer_x_passive_heal".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState petA = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                SourcePetState petB = new SourcePetState(1, 92, 5, 3, 2, 10, 45);
                petA.sourcePayload[6] = Math.max(1, sourceMaxHp(petA) / 2);
                petB.sourcePayload[6] = Math.max(1, sourceMaxHp(petB) / 2);
                int startA = payloadHp(petA);
                int startB = payloadHp(petB);
                s.sourceGlobalState[0][0] = 2;
                s.sourceGlobalState[0][1] = 1;
                s.sourcePets.add(petA);
                s.sourcePets.add(petB);
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, false, 0, 40, 1, 1, 1);
                tickUntilTraceContains(s, "game.d.X passive heal B[0][0/1]", 760);
                int expectedA = Math.min(sourceMaxHp(petA), startA + sourceExpectedPostExpPassiveHeal(petA.speciesId));
                int expectedB = Math.min(sourceMaxHp(petB), startB + sourceExpectedPostExpPassiveHeal(petB.speciesId));
                assertPayloadHp(petA, expectedA, "SliceC passive heal petA");
                assertPayloadHp(petB, expectedB, "SliceC passive heal petB");
                s.sourceStateTrace.add("SMOKE verified SliceC game.d.X passive heal"
                        + " expectedA=" + expectedA + " expectedB=" + expectedB);
            } else if ("battle_p13_queue_death_to_p5".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, true, 0, 40, 1, 1, 3);
                tickUntilAnyBattleState(s, 120, "P13");
                int guard = 0;
                while (!"P5".equals(s.battleStateName) && guard++ < 260) {
                    s.tick();
                }
                if (!"P5".equals(s.battleStateName) || s.battleMenuIds.length == 0
                        || !traceContains(s, "generic KO side0 -> P5 context=P12/P13 active queue")) {
                    throw new IllegalStateException("Expected P13 player death with reserve pet to route P5, state="
                            + s.battleStateName + " menuIds=" + java.util.Arrays.toString(s.battleMenuIds)
                            + " trace=" + tailTrace(s, 14));
                }
            } else if ("battle_p13_queue_death_to_p9".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, true, 0, 40, 1, 1, 3);
                tickUntilAnyBattleState(s, 120, "P13");
                int guard = 0;
                while (!"P9".equals(s.battleStateName) && guard++ < 260) {
                    s.tick();
                }
                if (!"P9".equals(s.battleStateName)
                        || !traceContains(s, "generic KO side0 -> P9 context=P12/P13 active queue")) {
                    throw new IllegalStateException("Expected P13 player death without reserve pet to route P9, state="
                            + s.battleStateName + " trace=" + tailTrace(s, 14));
                }
            } else if ("battle_p12_queue_death_to_p15".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 0, 6, 3, 2, 1, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugEnemyPartyForSmoke(s, new int[][]{
                        {0, 1, 1},
                        {34, 1, 1}
                });
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P7", 120);
                int guard = 0;
                while (!"P15".equals(s.battleStateName) && guard++ < 520) {
                    s.tick();
                }
                if (!"P15".equals(s.battleStateName)
                        || !traceContains(s, "replacement pending")) {
                    throw new IllegalStateException("Expected first enemy death to route P15 with reserve enemy, state="
                            + s.battleStateName + " enemy=" + s.battleEnemyName
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_p15_enemy_replaced".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 0, 6, 3, 2, 1, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugEnemyPartyForSmoke(s, new int[][]{
                        {0, 1, 1},
                        {34, 1, 1}
                });
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P7", 120);
                int guard = 0;
                while (!traceContains(s, "P15 source case15 swap enemy") && guard++ < 620) {
                    s.tick();
                }
                if (!traceContains(s, "P15 source case15 swap enemy")
                        || "P8".equals(s.battleStateName)
                        || !s.battleEnemyName.contains("Bunny")) {
                    throw new IllegalStateException("Expected P15 to swap active enemy to Bunny, state="
                            + s.battleStateName + " enemy=" + s.battleEnemyName
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_elder_p7_q_leech_skill58".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{58, 45}, 0);
                BattleUnit.setDamageRandomSeedForChecks(0L);
                tickUntilBattleP7Phase(s, 3, 220);
                if (!s.battleP7PostEffectVisible || !s.battleP7PostEffectText.startsWith("+")) {
                    throw new IllegalStateException("Expected P7 q leech text, visible="
                            + s.battleP7PostEffectVisible + " text=" + s.battleP7PostEffectText
                            + " trace=" + tailTrace(s, 8));
                }
            } else if ("battle_elder_p7_recoil_peak".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                for (int i = 0; i < 2; i++) {
                    s.tick();
                }
            } else if ("battle_elder_p7_after_resolve".equals(checkpoint)) {
                enterElderP7FromFight(s);
                int guard = 0;
                while ("P7".equals(s.battleStateName) && guard++ < 180) {
                    s.tick();
                }
                if ("P7".equals(s.battleStateName)) {
                    throw new IllegalStateException("P7 did not resolve");
                }
            } else if ("battle_elder_p7_speffect45_start".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_elder_p7_speffect45_overlay".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 2; i++) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible) {
                    throw new IllegalStateException("Expected skill 45 AH type 9 overlay to be visible");
                }
            } else if ("battle_elder_p7_speffect45_type1".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 8 && s.battleP7SpecialType != 1; i++) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 1) {
                    throw new IllegalStateException("Expected skill 45 chunk1 AH type 1 overlay, type="
                            + s.battleP7SpecialType + " visible=" + s.battleP7SpecialVisible);
                }
            } else if ("battle_elder_p7_speffect45_after".equals(checkpoint)) {
                enterElderP7WithSkillIndex(s, 1);
                int guard = 0;
                while ("P7".equals(s.battleStateName) && guard++ < 180) {
                    s.tick();
                }
                if ("P7".equals(s.battleStateName)) {
                    throw new IllegalStateException("P7 speffect45 did not resolve");
                }
            } else if ("battle_phase10b_p7_type12_skill55_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{55, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_phase10b_p7_type12_skill55_overlay".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{55, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
                int guard = 0;
                while ((!s.battleP7SpecialVisible || s.battleP7SpecialType != 12) && guard++ < 80) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 12
                        || s.battleP7SpecialRow.length < 10
                        || !traceContains(s, "battle P7 speffect skill=55")) {
                    throw new IllegalStateException("Expected skill55 normal P7 AH type12 overlay, type="
                            + s.battleP7SpecialType
                            + " visible=" + s.battleP7SpecialVisible
                            + " rowLength=" + s.battleP7SpecialRow.length
                            + " trace=" + tailTrace(s, 14));
                }
                assertRenderedVisiblePixels(s, "Phase10B skill55 P7 AH type12 target overlay",
                        132, 70, 96, 118, 200);
            } else if ("battle_phase10b_p7_type12_skill55_after".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{55, 45}, 0);
                int guard = 0;
                while ("P7".equals(s.battleStateName) && guard++ < 180) {
                    s.tick();
                }
                if ("P7".equals(s.battleStateName)) {
                    throw new IllegalStateException("P7 skill55 type12 did not resolve");
                }
            } else if ("battle_phase10b_p7_type8_skill12_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{12, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_phase10b_p7_type8_skill12_overlay".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{12, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
                int guard = 0;
                while ((!s.battleP7SpecialVisible || s.battleP7SpecialType != 8) && guard++ < 160) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 8
                        || s.battleP7SpecialRow.length < 9
                        || !traceContains(s, "battle P7 speffect skill=12")) {
                    throw new IllegalStateException("Expected skill12 normal P7 AH type8 overlay, type="
                            + s.battleP7SpecialType
                            + " visible=" + s.battleP7SpecialVisible
                            + " rowLength=" + s.battleP7SpecialRow.length
                            + " trace=" + tailTrace(s, 16));
                }
                assertRenderedVisiblePixels(s, "Phase10B skill12 P7 AH type8 target overlay",
                        132, 70, 96, 118, 120);
            } else if ("battle_phase10b_p7_type8_skill12_after".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{12, 45}, 0);
                int guard = 0;
                while ("P7".equals(s.battleStateName) && guard++ < 240) {
                    s.tick();
                }
                if ("P7".equals(s.battleStateName)) {
                    throw new IllegalStateException("P7 skill12 type8 did not resolve");
                }
            } else if ("battle_phase10b_p7_type7_skill34_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{34, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_phase10b_p7_type7_skill34_overlay".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{34, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
                int guard = 0;
                while ((!s.battleP7SpecialVisible || s.battleP7SpecialType != 7) && guard++ < 80) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 7
                        || s.battleP7SpecialRow.length < 8
                        || !traceContains(s, "battle P7 speffect skill=34")) {
                    throw new IllegalStateException("Expected skill34 normal P7 AH type7 overlay, type="
                            + s.battleP7SpecialType
                            + " visible=" + s.battleP7SpecialVisible
                            + " rowLength=" + s.battleP7SpecialRow.length
                            + " trace=" + tailTrace(s, 16));
                }
                assertRenderedVisiblePixels(s, "Phase10B skill34 P7 AH type7 target overlay",
                        132, 70, 96, 118, 120);
            } else if ("battle_phase10b_p7_type7_skill34_after".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{34, 45}, 0);
                int guard = 0;
                while ("P7".equals(s.battleStateName) && guard++ < 180) {
                    s.tick();
                }
                if ("P7".equals(s.battleStateName)) {
                    throw new IllegalStateException("P7 skill34 type7 did not resolve");
                }
            } else if ("battle_elder_p7_skill15_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{15, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_elder_p7_actor_u33_start".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{15, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
                s.tick();
                if (!s.battleP7ActorEffectVisible
                        || s.battleP7ActorEffectSpriteId != 308
                        || s.battleP7ActorEffectState != 0
                        || !s.battleP7ActorEffectOnPlayerSide
                        || s.battleP7ActorEffectCursor != 0) {
                    throw new IllegalStateException("Expected skill15 target AH u33 frame-trigger actor, visible="
                            + s.battleP7ActorEffectVisible
                            + " sprite=" + s.battleP7ActorEffectSpriteId
                            + " state=" + s.battleP7ActorEffectState
                            + " playerSide=" + s.battleP7ActorEffectOnPlayerSide
                            + " cursor=" + s.battleP7ActorEffectCursor);
                }
            } else if ("battle_elder_p7_actor_u33_to_h7".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{15, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
                s.tick();
                if (!s.battleP7ActorEffectVisible
                        || s.battleP7ActorEffectSpriteId != 308
                        || !s.battleP7ActorEffectOnPlayerSide
                        || s.battleP7ActorEffectCursor != 0) {
                    throw new IllegalStateException("Expected skill15 u33 before frame trigger, visible="
                            + s.battleP7ActorEffectVisible
                            + " sprite=" + s.battleP7ActorEffectSpriteId
                            + " playerSide=" + s.battleP7ActorEffectOnPlayerSide
                            + " cursor=" + s.battleP7ActorEffectCursor);
                }
                for (int i = 0; i < 12 && s.battleP7SpecialType != 9; i++) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 9) {
                    throw new IllegalStateException("Expected skill15 u33 frame 0 trigger to enter H speffect7 type9, type="
                            + s.battleP7SpecialType + " visible=" + s.battleP7SpecialVisible);
                }
            } else if ("battle_elder_p7_skill15_chunk4_trigger".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{15, 45}, 0);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 12 && s.battleP7SpecialType != 9; i++) {
                    s.tick();
                }
                if (!s.battleP7SpecialVisible || s.battleP7SpecialType != 9) {
                    throw new IllegalStateException("Expected skill 15 chunk4 trigger to reach AH type 9, type="
                            + s.battleP7SpecialType + " visible=" + s.battleP7SpecialVisible);
                }
            } else if ("battle_elder_p7_skill15_after".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{15, 45}, 0);
                int guard = 0;
                while ("P7".equals(s.battleStateName) && guard++ < 180) {
                    s.tick();
                }
                if ("P7".equals(s.battleStateName)) {
                    throw new IllegalStateException("P7 skill15 did not resolve");
                }
                if (s.battleEnemyHp != s.battleEnemyMaxHp) {
                    throw new IllegalStateException("Skill 15 should not apply fake damage, enemy hp="
                            + s.battleEnemyHp + "/" + s.battleEnemyMaxHp);
                }
            } else if ("battle_state1_l_species0_start".equals(checkpoint)) {
                enterSpecies0LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species0_active".equals(checkpoint)) {
                enterSpecies0LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 11 || s.battleLSpriteId != 86) {
                    throw new IllegalStateException("Expected species0 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species0_after".equals(checkpoint)) {
                enterSpecies0LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 80; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species0 L effect to complete");
                }
            } else if ("battle_state1_l_species75_start".equals(checkpoint)) {
                enterSpecies75LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species75_active".equals(checkpoint)) {
                enterSpecies75LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 11 || s.battleLSpriteId != 161) {
                    throw new IllegalStateException("Expected species75 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species75_after".equals(checkpoint)) {
                enterSpecies75LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 80; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species75 L effect to complete");
                }
            } else if ("battle_state1_l_species87_start".equals(checkpoint)) {
                enterSpecies87LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species87_active".equals(checkpoint)) {
                enterSpecies87LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 12 || s.battleLSpriteId != 173) {
                    throw new IllegalStateException("Expected species87 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species87_after".equals(checkpoint)) {
                enterSpecies87LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 100; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species87 L effect to complete");
                }
            } else if ("battle_state1_l_species91_start".equals(checkpoint)) {
                enterSpecies91LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species91_active".equals(checkpoint)) {
                enterSpecies91LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 14 || s.battleLSpriteId != 177) {
                    throw new IllegalStateException("Expected species91 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species91_after".equals(checkpoint)) {
                enterSpecies91LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 100; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species91 L effect to complete");
                }
            } else if ("battle_state1_l_species10_start".equals(checkpoint)) {
                enterSpecies10LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species10_active".equals(checkpoint)) {
                enterSpecies10LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 15 || s.battleLSpriteId != 96
                        || !s.battleLDrawAfter) {
                    throw new IllegalStateException("Expected species10 L effect active after actor, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId
                            + " drawAfter=" + s.battleLDrawAfter);
                }
            } else if ("battle_state1_l_species10_after".equals(checkpoint)) {
                enterSpecies10LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 100; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species10 L effect to complete");
                }
            } else if ("battle_state1_l_species92_start".equals(checkpoint)) {
                enterSpecies92LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species92_active".equals(checkpoint)) {
                enterSpecies92LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 11 || s.battleLSpriteId != 178) {
                    throw new IllegalStateException("Expected species92 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species92_after".equals(checkpoint)) {
                enterSpecies92LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 100; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species92 L effect to complete");
                }
            } else if ("battle_state1_l_species97_start".equals(checkpoint)) {
                enterSpecies97LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species97_active".equals(checkpoint)) {
                enterSpecies97LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 13 || s.battleLSpriteId != 183) {
                    throw new IllegalStateException("Expected species97 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species97_after".equals(checkpoint)) {
                enterSpecies97LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 100; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species97 L effect to complete");
                }
            } else if ("battle_state1_l_species98_start".equals(checkpoint)) {
                enterSpecies98LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species98_active".equals(checkpoint)) {
                enterSpecies98LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 13 || s.battleLSpriteId != 184) {
                    throw new IllegalStateException("Expected species98 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species98_after".equals(checkpoint)) {
                enterSpecies98LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 100; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species98 L effect to complete");
                }
            } else if ("battle_state1_l_species62_start".equals(checkpoint)) {
                enterSpecies62LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
            } else if ("battle_state1_l_species62_active".equals(checkpoint)) {
                enterSpecies62LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 40 && !s.battleLVisible; i++) {
                    s.tick();
                }
                if (!s.battleLVisible || s.battleLType != 13 || s.battleLSpriteId != 148) {
                    throw new IllegalStateException("Expected species62 L effect active, visible="
                            + s.battleLVisible + " type=" + s.battleLType + " sprite=" + s.battleLSpriteId);
                }
            } else if ("battle_state1_l_species62_after".equals(checkpoint)) {
                enterSpecies62LP7(s);
                tickUntilBattleP7Phase(s, 1, 80);
                for (int i = 0; i < 120; i++) {
                    s.tick();
                    if (!s.battleLVisible && s.battleP7Phase >= 2) {
                        break;
                    }
                }
                if (s.battleLVisible) {
                    throw new IllegalStateException("Expected species62 L effect to complete");
                }
            } else if ("battle_skill_no_pp_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
                pet.skillCooldowns[0] = 0;
                pet.skillCooldowns[1] = 0;
                s.sourcePets.add(pet);
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                for (int i = 0; i < 18 && !"WARN".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "WARN", 80);
            } else if ("battle_skill_hover_preview_no_confirm".equals(checkpoint)) {
                setupElderShopBattleForSmoke(s, 6);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
                s.battleHoverX = 60;
                s.battleHoverY = 109;
                for (int i = 0; i < 20 && s.battleSkillIndex != 1; i++) {
                    s.tick();
                }
                if (!"P3".equals(s.battleStateName)
                        || s.battleSkillIndex != 1
                        || s.battleSkillDescription == null
                        || s.battleSkillDescription.isEmpty()) {
                    throw new IllegalStateException("Expected skill hover to preview row 1 without P7 confirm"
                            + " state=" + s.battleStateName
                            + " index=" + s.battleSkillIndex
                            + " desc=" + s.battleSkillDescription
                            + " trace=" + tailTrace(s, 18));
                }
            } else if ("battle_elder_command_ui_right".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.setMoveKey(KeyEvent.VK_RIGHT, true);
                for (int i = 0; i < 12; i++) {
                    s.tick();
                }
                s.setMoveKey(KeyEvent.VK_RIGHT, false);
            } else if ("battle_elder_command_ui_click_pet".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 137;
                s.battleClickY = 300;
                for (int i = 0; i < 12; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_catch_p21".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny P21");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                assertBattleMenuIds(s, "Bunny route P21 ball list", new int[]{0, 1});
                assertRenderedColorPixels(s, "choice.ui P21 body fill", 44, 78, 151, 160, 0xbde4ef, 1800);
                assertRenderedColorPixels(s, "choice.ui P21 footer strip", 44, 238, 151, 14, 0x82cafb, 350);
                assertRenderedColorPixels(s, "choice.ui P21 count text", 57, 180, 125, 12, 0xffffff, 6);
            } else if ("battle_mouse_wheel_p21_list".equals(checkpoint)) {
                setupLiveCheckpoint(s, "battle_bunny_catch_p21");
                for (int i = 0; i < 12; i++) {
                    s.tick();
                }
                int initialIndex = s.battleMenuIndex;
                int initialScroll = s.battleMenuScroll;
                s.mouseWheel(1);
                s.tick();
                if (!"P21".equals(s.battleStateName)
                        || s.battleMenuIndex != initialIndex
                        || s.battleMenuScroll != initialScroll
                        || s.battleCatchItemId != -1
                        || s.battleShopConfirmItemId != -1) {
                    throw new IllegalStateException("Mouse wheel down should not move non-scrollable P21 list"
                            + " state=" + s.battleStateName
                            + " index=" + s.battleMenuIndex
                            + " scroll=" + s.battleMenuScroll
                            + " catchItem=" + s.battleCatchItemId
                            + " confirmItem=" + s.battleShopConfirmItemId
                            + " trace=" + tailTrace(s, 16));
                }
                s.mouseWheel(-1);
                s.tick();
                if (!"P21".equals(s.battleStateName)
                        || s.battleMenuIndex != initialIndex
                        || s.battleMenuScroll != initialScroll
                        || s.battleCatchItemId != -1) {
                    throw new IllegalStateException("Mouse wheel up should not move non-scrollable P21 list"
                            + " state=" + s.battleStateName
                            + " index=" + s.battleMenuIndex
                            + " scroll=" + s.battleMenuScroll
                            + " catchItem=" + s.battleCatchItemId
                            + " trace=" + tailTrace(s, 16));
                }
                s.sourceStateTrace.add("SMOKE verified mouse wheel ignores non-scrollable P21 without confirm");
                revealCheckpointText(s, 60);
            } else if ("battle_bunny_p7_state1_attack_source_compare".equals(checkpoint)) {
                enterBunnyP7FromFight(s);
                tickUntilBattleP7Phase(s, 1, 100);
                assertP7SourceSpriteFrame(s, "Bunny P7 state1 attack", true, 1,
                        s.battleP7BaseCursorPlayerSide, s.battlePlayerVisualId, 0);
            } else if ("battle_bunny_p7_state2_hit_source_compare".equals(checkpoint)) {
                enterBunnyP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 140);
                assertP7SourceSpriteFrame(s, "Bunny P7 state2 hit", false,
                        s.battleP7BaseStateEnemySide, s.battleP7BaseCursorEnemySide,
                        s.battleEnemyVisualId, 24);
            } else if ("battle_bunny_catch_p17_anim_or_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny P17");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                assertBattleMenuIds(s, "Bunny route P21 ball list before P17", new int[]{0, 1});
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                for (int i = 0; i < 18; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_after_catch_route".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny after catch route");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickBattleAutoUntilDone(s, 3000);
                if (s.battleResultIndex != -1 || s.battleBranchTarget != -1) {
                    throw new IllegalStateException("Bunny catch route mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                if (s.battleTutorialU != -1 || s.battleTutorialV != 0) {
                    throw new IllegalStateException("Bunny tutorial cleanup mismatch U="
                            + s.battleTutorialU + " V=" + s.battleTutorialV
                            + " trace=" + tailTrace(s, 16));
                }
                s.text = TextBox.taskTip(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER);
                revealCheckpointText(s, 90);
            } else if ("battle_bunny_catch_success_openbox_visual".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny catch success openbox visual");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilRetryP21(s, 3000);
                int guard = 0;
                while (guard++ < 1200) {
                    if (s.text != null && s.text.sourceUiKind == TextBox.SOURCE_OPENBOX) {
                        break;
                    }
                    driveBunnyTutorialOneTick(s);
                }
                if (s.text == null
                        || s.text.sourceUiKind != TextBox.SOURCE_OPENBOX
                        || !s.battleEnemyHiddenByCatch
                        || !s.battleCatchVisible
                        || s.battleCatchPhase != 3
                        || s.battleCatchItemId != 0
                        || !s.battleCatchCaught) {
                    throw new IllegalStateException("Expected Bunny catch success openbox to keep enemy hidden in ball"
                            + " state=" + s.battleStateName
                            + " textKind=" + (s.text == null ? -1 : s.text.sourceUiKind)
                            + " hidden=" + s.battleEnemyHiddenByCatch
                            + " catchVisible=" + s.battleCatchVisible
                            + " phase=" + s.battleCatchPhase
                            + " item=" + s.battleCatchItemId
                            + " caught=" + s.battleCatchCaught
                            + " trace=" + tailTrace(s, 20));
                }
                revealCheckpointText(s, 120);
            } else if ("battle_bunny_weak_prompt_tasktip".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny weak prompt");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilWeakPrompt(s, 2200);
                revealCheckpointText(s, 18);
                if (s.text == null || s.text.sourceUiKind != TextBox.SOURCE_TASKTIP
                        || !s.text.text.contains("phong \u1ea5n c\u1ea7u")
                        || s.battleTutorialU != 0 || s.battleTutorialV != 1) {
                    throw new IllegalStateException("Bunny weak prompt mismatch text="
                            + (s.text == null ? "null" : s.text.text)
                            + " kind=" + (s.text == null ? -1 : s.text.sourceUiKind)
                            + " U=" + s.battleTutorialU + " V=" + s.battleTutorialV
                            + " state=" + s.battleStateName
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_bunny_first_catch_forced_fail".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny first catch forced fail");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilFirstCatchP17(s, 2500);
                if (s.battleCatchItemId != 1 || s.battleCatchCaught
                        || s.battleTutorialU != 0 || s.battleTutorialV != 5) {
                    throw new IllegalStateException("Bunny first catch force-fail mismatch item="
                            + s.battleCatchItemId + " caught=" + s.battleCatchCaught
                            + " U=" + s.battleTutorialU + " V=" + s.battleTutorialV
                            + " state=" + s.battleStateName + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_bunny_first_catch_fail_escape_effect".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny first catch fail escape");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilFirstCatchP17(s, 2500);
                tickUntilBattleCatchPhase(s, 4, 260);
                if (s.battleCatchItemId != 1 || s.battleCatchCaught
                        || !s.battleCatchEffectVisible) {
                    throw new IllegalStateException("Bunny forced fail escape effect mismatch item="
                            + s.battleCatchItemId + " caught=" + s.battleCatchCaught
                            + " phase=" + s.battleCatchPhase
                            + " effect=" + s.battleCatchEffectVisible
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_bunny_first_fail_enemy_counterattack".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny first fail enemy counterattack");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilEnemyCounterAfterFirstFail(s, 4000);
                if (!"P7".equals(s.battleStateName) || s.battleP7AttackerPlayerSide
                        || !s.battleP7TargetPlayerSide || !s.battleP7DamageVisible
                        || s.battleTutorialU != 0 || s.battleTutorialV != 5) {
                    throw new IllegalStateException("Bunny first fail should allow enemy P7 counterattack before retry prompt"
                            + " state=" + s.battleStateName
                            + " attackerPlayer=" + s.battleP7AttackerPlayerSide
                            + " targetPlayer=" + s.battleP7TargetPlayerSide
                            + " damageVisible=" + s.battleP7DamageVisible
                            + " U=" + s.battleTutorialU + " V=" + s.battleTutorialV
                            + " trace=" + tailTrace(s, 20));
                }
            } else if ("battle_bunny_first_catch_q2_rumble".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny first catch rumble");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilFirstCatchP17(s, 2500);
                tickUntilBattleCatchPhaseCursor(s, 2, 1, 260);
                if (s.battleCatchItemId != 1 || s.battleCatchCaught) {
                    throw new IllegalStateException("Bunny forced fail q2 rumble mismatch item="
                            + s.battleCatchItemId + " caught=" + s.battleCatchCaught
                            + " phase=" + s.battleCatchPhase
                            + " cursor=" + s.battleCatchAnimCursor
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_bunny_pre_p17_rng_trace".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny pre-P17 RNG trace");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilFirstCatchP17(s, 2500);
                String trace = tailTrace(s, 80);
                if (!trace.contains("RNG TRACE battle.P7.skill")
                        || !trace.contains(".damage.crit helper=ae.a(int)")
                        || !trace.contains(".damage.jitter helper=ae.a(int)")
                        || !trace.contains("RNG TRACE battle.P17.catch helper=ae.a(int)")
                        || s.battleCatchItemId != 1
                        || s.battleCatchCaught) {
                    throw new IllegalStateException("Bunny pre-P17 RNG trace mismatch item="
                            + s.battleCatchItemId + " caught=" + s.battleCatchCaught
                            + " trace=" + trace);
                }
                System.out.println("smoke-rng-trace-order " + rngTraceSummary(s));
                s.sourceStateTrace.add("SMOKE verified Bunny pre-P17 source RNG trace includes damage.crit, damage.jitter, P17 catch");
            } else if ("battle_bunny_retry_p21_item0".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny retry P21");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilRetryP21(s, 3500);
                assertBattleMenuIds(s, "Bunny retry P21 ball list", new int[]{0, 1});
                int selectedId = s.battleMenuIds.length == 0 ? -1 : s.battleMenuIds[s.battleMenuIndex];
                if (selectedId != 0 || s.battleTutorialU != 0 || s.battleTutorialV != 7) {
                    throw new IllegalStateException("Bunny retry P21 selected item mismatch selected="
                            + selectedId + " menuIndex=" + s.battleMenuIndex
                            + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                            + " U=" + s.battleTutorialU + " V=" + s.battleTutorialV
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_bunny_retry_prompt_tasktip".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedInitialDienMieu(s, "smoke Bunny retry prompt");
                seedRoom0Group0BunnyRewards(s);
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                driveBunnyTutorialUntilRetryPrompt(s, 3500);
                revealCheckpointText(s, 18);
                if (s.text == null || s.text.sourceUiKind != TextBox.SOURCE_TASKTIP
                        || !s.text.text.contains("T\u1ea5t tr\u00fang c\u1ea7u")) {
                    throw new IllegalStateException("Bunny retry prompt mismatch text="
                            + (s.text == null ? "null" : s.text.text)
                            + " kind=" + (s.text == null ? -1 : s.text.sourceUiKind)
                            + " state=" + s.battleStateName
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_catch_fail_or_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(99);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                if (s.battleCatchRoll != 99 || s.battleCatchCaught) {
                    throw new IllegalStateException("Catch generic roll-fail mismatch chance="
                            + s.battleCatchChance + " roll=" + s.battleCatchRoll
                            + " caught=" + s.battleCatchCaught
                            + " trace=" + tailTrace(s, 12));
                }
                for (int i = 0; i < 58 && "P17".equals(s.battleStateName); i++) {
                    s.tick();
                }
            } else if ("battle_catch_generic_roll_success".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                if (s.battleCatchRoll != 0 || !s.battleCatchCaught) {
                    throw new IllegalStateException("Catch generic roll-success mismatch chance="
                            + s.battleCatchChance + " roll=" + s.battleCatchRoll
                            + " caught=" + s.battleCatchCaught
                            + " trace=" + tailTrace(s, 12));
                }
                for (int i = 0; i < 18; i++) {
                    s.tick();
                }
            } else if ("battle_catch_success_flash_phase".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                tickUntilBattleCatchPhase(s, 3, 260);
                if (s.battleCatchItemId != 0 || !s.battleCatchCaught) {
                    throw new IllegalStateException("Catch success q3 mismatch item="
                            + s.battleCatchItemId + " caught=" + s.battleCatchCaught
                            + " phase=" + s.battleCatchPhase
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_catch_success_q3_flash_mid".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                tickUntilBattleCatchPhaseCursor(s, 3, 1, 260);
                if (s.battleCatchItemId != 0 || !s.battleCatchCaught) {
                    throw new IllegalStateException("Catch success q3 flash mid mismatch item="
                            + s.battleCatchItemId + " caught=" + s.battleCatchCaught
                            + " phase=" + s.battleCatchPhase
                            + " cursor=" + s.battleCatchAnimCursor
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_p17_sprite269_timing_matrix".equals(checkpoint)) {
                assertSprite269TimingMatrix(s);
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                press0UntilAnyBattleState(s, 120, "P17");
                if (s.battleCatchSpriteId != 269 || s.battleCatchItemId != 0 || !s.battleCatchCaught) {
                    throw new IllegalStateException("Expected P17 to start with sprite269 item0 success, sprite="
                            + s.battleCatchSpriteId + " item=" + s.battleCatchItemId
                            + " caught=" + s.battleCatchCaught + " trace=" + tailTrace(s, 16));
                }
                tickUntilBattleCatchPhase(s, 1, 80);
                tickUntilBattleCatchPhaseCursor(s, 2, 10, 120);
                tickUntilBattleCatchPhaseCursor(s, 3, 10, 120);
                s.sourceStateTrace.add("SMOKE verified battle P17 sprite269 runtime reaches q1/q2/q3"
                        + " with source matrix timing guard cursor=" + s.battleCatchAnimCursor);
            } else if ("battle_rng_trace_p17_catch".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                long seed = 0x56515356L;
                java.util.Random expected = new java.util.Random(seed);
                int raw = expected.nextInt();
                int expectedRoll = (raw >>> 1) % 100;
                runtime.debugSetSourceRandomSeedForSmoke(seed);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                if (s.battleCatchRoll != expectedRoll
                        || !traceContains(s, "RNG TRACE battle.P17.catch helper=ae.a(int) bound=100 raw=" + raw)
                        || !traceContains(s, "return=" + expectedRoll)
                        || !traceContains(s, "seed=" + seed)) {
                    throw new IllegalStateException("P17 RNG trace mismatch expectedRoll=" + expectedRoll
                            + " actual=" + s.battleCatchRoll
                            + " trace=" + tailTrace(s, 14));
                }
                s.sourceStateTrace.add("SMOKE verified P17 RNG trace seed=" + seed
                        + " raw=" + raw + " return=" + expectedRoll);
                for (int i = 0; i < 18; i++) {
                    s.tick();
                }
            } else if ("battle_catch_chance_status_multipliers".equals(checkpoint)) {
                VqsvIntroDemo.Scene base = setupCatchChanceStatusMenu(-1, false);
                VqsvIntroDemo.Scene debuff1 = setupCatchChanceStatusMenu(1, false);
                VqsvIntroDemo.Scene debuff2 = setupCatchChanceStatusMenu(2, false);
                VqsvIntroDemo.Scene debuff10 = setupCatchChanceStatusMenu(10, false);
                VqsvIntroDemo.Scene form11 = setupCatchChanceStatusMenu(-1, true);
                int baseChance = catchMenuChanceForItem(base, 1);
                int debuff1Chance = catchMenuChanceForItem(debuff1, 1);
                int debuff2Chance = catchMenuChanceForItem(debuff2, 1);
                int debuff10Chance = catchMenuChanceForItem(debuff10, 1);
                int form11Chance = catchMenuChanceForItem(form11, 1);
                if (!(debuff1Chance > baseChance
                        && debuff2Chance >= debuff1Chance
                        && debuff10Chance == debuff2Chance
                        && form11Chance >= debuff2Chance)) {
                    throw new IllegalStateException("Catch status multiplier mismatch base="
                            + baseChance + " debuff1=" + debuff1Chance
                            + " debuff2=" + debuff2Chance
                            + " debuff10=" + debuff10Chance
                            + " form11=" + form11Chance);
                }
                s = form11;
                s.sourceStateTrace.add("SMOKE verified catchChance status multipliers base="
                        + baseChance + " debuff1=" + debuff1Chance
                        + " debuff2=" + debuff2Chance
                        + " debuff10=" + debuff10Chance
                        + " form11=" + form11Chance);
            } else if ("battle_catch_missing_count_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 0, 0, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"WARN".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "WARN", 80);
                String trace = tailTrace(s, 12);
                if (!VqsvText.Battle.NO_BALLS.equals(s.battleWarningTitle)
                        || !trace.contains("item0 PC-free hook not taken")) {
                    throw new IllegalStateException("Catch missing-count warning mismatch title="
                            + s.battleWarningTitle + " trace=" + trace);
                }
            } else if ("battle_catch_missing_count_warning_return_p21".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 0, 0, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"WARN".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "WARN", 80);
                press0UntilAnyBattleState(s, 120, "P21");
                if (!"P21".equals(s.battleStateName)
                        || !traceContains(s, "item0 PC-free hook not taken")) {
                    throw new IllegalStateException("Catch missing-count warning should return P21, state="
                            + s.battleStateName + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_catch_p21_back_to_command".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                s.keyBack = true;
                tickUntilBattleState(s, "P20", 80);
                if (!"P20".equals(s.battleStateName)
                        || s.battleUiMode == null
                        || !"command".equals(s.battleUiMode)) {
                    throw new IllegalStateException("Expected P21 back to return command state, state="
                            + s.battleStateName + " ui=" + s.battleUiMode
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_catch_pc_free_item0_p17".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 0, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(99);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                assertBattleMenuIds(s, "PC-free item0 P21 list", new int[]{0});
                press0UntilAnyBattleState(s, 120, "P17", "WARN");
                if (!"P17".equals(s.battleStateName)
                        || s.battleCatchItemId != 0
                        || !s.battleCatchCaught
                        || VqsvSourceOps.sourceItemCount(s, 0) != 0
                        || !traceContains(s, "P21 PC-free purchase bypass item=0")) {
                    throw new IllegalStateException("Expected PC-free item0 to enter P17, state="
                            + s.battleStateName + " item=" + s.battleCatchItemId
                            + " caught=" + s.battleCatchCaught
                            + " count=" + VqsvSourceOps.sourceItemCount(s, 0)
                            + " trace=" + tailTrace(s, 14));
                }
            } else if ("battle_choice_ui_scroll_source_rows".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                for (int id = 4; id <= 11; id++) {
                    s.sourceBagItems.put(id, new BagItem(id, 1, 1, false));
                }
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 98;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P4", 80);
                s.battleMenuIndex = 6;
                s.battleMenuScroll = 2;
                s.tick();
                if (!"P4".equals(s.battleStateName)
                        || s.battleMenuIndex < 5
                        || s.battleMenuScroll <= 0) {
                    throw new IllegalStateException("Expected choice.ui 5-row scroll, state="
                            + s.battleStateName + " index=" + s.battleMenuIndex
                            + " scroll=" + s.battleMenuScroll
                            + " names=" + java.util.Arrays.toString(s.battleMenuNames));
                }
                assertRenderedColorPixels(s, "choice.ui scroll body fill", 44, 78, 151, 160, 0xbde4ef, 1800);
                assertRenderedColorPixels(s, "choice.ui scrollbar track", 183, 98, 3, 72, 0x51d8e9, 120);
                assertRenderedColorPixels(s, "choice.ui scrollbar thumb source index", 183, 152, 4, 8, 0xc6f1ff, 20);
            } else if ("battle_choice_ui_source_be_offset_up".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                for (int id = 4; id <= 11; id++) {
                    s.sourceBagItems.put(id, new BagItem(id, 1, 1, false));
                }
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 98;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P4", 80);
                s.battleMenuIndex = 5;
                s.battleMenuScroll = 1;
                s.battleChoiceUi = s.battleChoiceUi.withSourceCursor(s.battleMenuIndex, s.battleMenuScroll);
                s.battleChoiceUi = s.battleChoiceUi.moveUpSource();
                s.battleMenuIndex = s.battleChoiceUi.selectedIndex;
                s.battleMenuScroll = s.battleChoiceUi.scroll;
                s.tick();
                if (!"P4".equals(s.battleStateName)
                        || s.battleMenuIndex != 4
                        || s.battleMenuScroll != 0) {
                    throw new IllegalStateException("Expected choice.ui source be() up offset index=4 scroll=0, state="
                            + s.battleStateName + " index=" + s.battleMenuIndex
                            + " scroll=" + s.battleMenuScroll
                            + " choiceIndex=" + s.battleChoiceUi.selectedIndex
                            + " choiceScroll=" + s.battleChoiceUi.scroll
                            + " names=" + java.util.Arrays.toString(s.battleMenuNames));
                }
                assertRenderedColorPixels(s, "choice.ui be up selected row", 54, 155, 126, 15, 0x51dbeb, 800);
                assertRenderedColorPixels(s, "choice.ui be up scrollbar thumb", 183, 134, 4, 8, 0xc6f1ff, 20);
            } else if (runBattleChoiceInputCheckpoint(s, checkpoint)) {
            } else if ("battle_choiceskill_mouse_wheel_scrollbar_no_confirm".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupSyntheticScrollableSkillListForSmoke(s);
                int initialSelected = s.battleSkillIndex;
                s.mouseWheel(3);
                if (!"choiceskill".equals(s.battleUiMode)
                        || s.battleSkillScroll != 3
                        || s.battleSkillIndex != initialSelected
                        || !traceContains(s, "PC_QOL mouse wheel battle skill scrollbar index=0 scroll=3")) {
                    throw new IllegalStateException("Expected choiceskill mouse wheel to move scrollbar only"
                            + " ui=" + s.battleUiMode
                            + " index=" + s.battleSkillIndex
                            + " initial=" + initialSelected
                            + " scroll=" + s.battleSkillScroll
                            + " trace=" + tailTrace(s, 20));
                }
                runtime.debugHandleSkillInputForSmoke(s);
                if (s.battleSkillIndex != initialSelected || s.battleSkillScroll != 3) {
                    throw new IllegalStateException("Expected choiceskill idle input after wheel to keep cursor"
                            + " index=" + s.battleSkillIndex
                            + " scroll=" + s.battleSkillScroll
                            + " trace=" + tailTrace(s, 20));
                }
                assertRenderedColorPixels(s, "choiceskill scrollbar track", 183, 98, 3, 70, 0x00a5e3, 70);
            } else if ("battle_choiceskill_mouse_wheel_hover_click_viewport".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupSyntheticScrollableSkillListForSmoke(s);
                s.mouseWheel(3);
                s.battleHoverX = 60;
                s.battleHoverY = 110;
                runtime.debugHandleSkillInputForSmoke(s);
                if (s.battleSkillIndex != 4 || s.battleSkillScroll != 3) {
                    throw new IllegalStateException("Expected choiceskill hover after wheel to select viewport index 4"
                            + " index=" + s.battleSkillIndex
                            + " scroll=" + s.battleSkillScroll
                            + " trace=" + tailTrace(s, 24));
                }
                s.battleClickX = 60;
                s.battleClickY = 125;
                runtime.debugHandleSkillInputForSmoke(s);
                if (s.battleSkillIndex != 5
                        || s.battleSkillScroll != 3
                        || s.battleClickX != -1
                        || s.battleClickY != -1) {
                    throw new IllegalStateException("Expected choiceskill click after wheel to confirm viewport index 5"
                            + " index=" + s.battleSkillIndex
                            + " scroll=" + s.battleSkillScroll
                            + " click=" + s.battleClickX + "," + s.battleClickY
                            + " trace=" + tailTrace(s, 24));
                }
                assertRenderedColorPixels(s, "choiceskill selected viewport row", 54, 125, 130, 14, 0x51dbeb, 500);
            } else if ("battle_choice_ui_widget_mutation_runtime".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                s.current = new SourceBattleRuntime(37, new int[]{34, 3, 1},
                        new int[0], new int[]{1, 1}, new int[]{0}, -1, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                VqsvChoiceUiView catchUi = s.battleChoiceUi;
                if (!"Pokemon ball".equals(catchUi.widgetText(8, ""))
                        || catchUi.widgetText(9, "").isEmpty()
                        || !catchUi.widgetVisible(5)
                        || !catchUi.widgetVisible(6)
                        || catchUi.widgetVisible(59)
                        || catchUi.widgetVisible(60)
                        || !catchUi.widgetVisible(52)
                        || !catchUi.widgetVisible(53)
                        || !catchUi.rowIconVisible(0)
                        || catchUi.rowIconCell(0) < 0
                        || VqsvChoiceUiView.ROW_ICON_SPRITE_ID != 258
                        || VqsvChoiceUiView.ROW_ICON_MODE != 2) {
                    throw new IllegalStateException("Expected P21 choice.ui mutation runtime, title="
                            + catchUi.widgetText(8, "")
                            + " subtitle=" + catchUi.widgetText(9, "")
                            + " actionVisible=" + catchUi.widgetVisible(5)
                            + " backVisible=" + catchUi.widgetVisible(6)
                            + " alt=" + catchUi.widgetVisible(59) + "/" + catchUi.widgetVisible(60)
                            + " desc=" + catchUi.widgetVisible(52) + "/" + catchUi.widgetVisible(53)
                            + " icon=" + catchUi.rowIconVisible(0) + ":" + catchUi.rowIconCell(0));
                }
                s.current = null;
                s.battleClickX = -1;
                s.battleClickY = -1;
                s = new VqsvIntroDemo.Scene();
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(4, new BagItem(4, 1, 1, false));
                s.sourceBagItems.put(5, new BagItem(5, 1, 1, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 98;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P4", 80);
                VqsvChoiceUiView itemUi = s.battleChoiceUi;
                if (itemUi.size() != 2
                        || !itemUi.widgetVisible(54)
                        || !itemUi.widgetVisible(55)
                        || itemUi.widgetVisible(56)
                        || itemUi.rowIconVisible(2)
                        || !itemUi.widgetVisible(52)
                        || !itemUi.widgetVisible(53)) {
                    throw new IllegalStateException("Expected P4 choice.ui row icon lifecycle clear after row 1, size="
                            + itemUi.size()
                            + " rows=" + itemUi.widgetVisible(54) + "/" + itemUi.widgetVisible(55)
                            + "/" + itemUi.widgetVisible(56)
                            + " icon2=" + itemUi.rowIconVisible(2)
                            + " desc=" + itemUi.widgetVisible(52) + "/" + itemUi.widgetVisible(53));
                }
                assertRenderedColorPixels(s, "choice.ui widget mutation p4 frame", 41, 68, 158, 171, 0xffffff, 120);
            } else if ("battle_choice_ui_alt_softkey_59_60".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                java.util.ArrayList<String> names = new java.util.ArrayList<>();
                java.util.ArrayList<String> values = new java.util.ArrayList<>();
                java.util.ArrayList<String> descriptions = new java.util.ArrayList<>();
                java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
                java.util.ArrayList<Integer> icons = new java.util.ArrayList<>();
                names.add("Test item");
                values.add("1");
                descriptions.add("Source-shaped alternate softkey");
                ids.add(4);
                icons.add(0);
                s.battleUiMode = "choice";
                s.battleMenuTitle = VqsvText.Battle.COMMAND_ITEM_PENDING;
                s.battleMenuSubtitle = "S\u1ed1 l\u01b0\u1ee3ng";
                s.battleMenuAction = "S\u1eed d\u1ee5ng";
                s.battleMenuNames = names.toArray(new String[0]);
                s.battleMenuValues = values.toArray(new String[0]);
                s.battleMenuDescriptions = descriptions.toArray(new String[0]);
                s.battleMenuIds = new int[]{4};
                s.battleMenuIconIds = new int[]{0};
                s.battleMenuIndex = 0;
                s.battleMenuScroll = 0;
                s.battleChoiceUi = VqsvChoiceUiView.battle(s.battleMenuTitle, s.battleMenuSubtitle,
                        s.battleMenuAction, names, values, descriptions, ids, icons, 0, 0)
                        .withAlternateSoftkeys("S\u1eed d\u1ee5ng");
                if (s.battleChoiceUi.widgetVisible(5)
                        || s.battleChoiceUi.widgetVisible(6)
                        || !s.battleChoiceUi.widgetVisible(59)
                        || !s.battleChoiceUi.widgetVisible(60)
                        || !"S\u1eed d\u1ee5ng".equals(s.battleChoiceUi.widgetText(59, ""))
                        || s.battleChoiceUi.widgetText(60, "").isEmpty()) {
                    throw new IllegalStateException("Expected source petsetting alternate 59/60 softkeys, 5/6="
                            + s.battleChoiceUi.widgetVisible(5) + "/" + s.battleChoiceUi.widgetVisible(6)
                            + " 59/60=" + s.battleChoiceUi.widgetVisible(59) + "/"
                            + s.battleChoiceUi.widgetVisible(60)
                            + " text59=" + s.battleChoiceUi.widgetText(59, "")
                            + " text60=" + s.battleChoiceUi.widgetText(60, ""));
                }
                assertRenderedColorPixels(s, "choice.ui alt softkey 59 frame", 1, 296, 43, 20, 0x081050, 600);
                assertRenderedColorPixels(s, "choice.ui alt softkey 60 frame", 197, 296, 43, 20, 0x081050, 600);
            } else if ("battle_msgwarm_source_widget_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 0, 0, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                press0UntilAnyBattleState(s, 120, "WARN");
                if (!"WARN".equals(s.battleStateName)
                        || s.text == null
                        || s.text.sourceUiKind != TextBox.SOURCE_MSGWARM
                        || !VqsvText.Battle.NO_BALLS.equals(s.battleWarningTitle)
                        || s.battleMsgWarm == null
                        || !VqsvText.Battle.NO_BALLS.equals(s.battleMsgWarm.widgetText(VqsvMsgWarmView.MESSAGE_WIDGET_ID))
                        || !VqsvText.Battle.WARNING_PROMPT.equals(s.battleMsgWarm.widgetText(VqsvMsgWarmView.PROMPT_WIDGET_ID))) {
                    throw new IllegalStateException("Expected battle warning to use msgwarm.ui widget, state="
                            + s.battleStateName
                            + " title=" + s.battleWarningTitle
                            + " view=" + (s.battleMsgWarm == null ? "null"
                            : s.battleMsgWarm.widgetText(VqsvMsgWarmView.MESSAGE_WIDGET_ID)
                            + "/" + s.battleMsgWarm.widgetText(VqsvMsgWarmView.PROMPT_WIDGET_ID))
                            + " textKind=" + (s.text == null ? -1 : s.text.sourceUiKind)
                            + " trace=" + tailTrace(s, 14));
                }
                assertRenderedColorPixels(s, "msgwarm text", 85, 119, 70, 12, 0x196b91, 8);
                assertRenderedColorPixels(s, "msgwarm frame fill", 82, 116, 76, 54, 0x51d8e9, 200);
                s.sourceStateTrace.add("SMOKE verified battle warning renders through /data/ui/msgwarm.ui TextBox");
            } else if ("battle_openbox_source_widget_catch_success".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                runCatchToOpenBox(s, 320);
                if (s.text == null
                        || s.text.sourceUiKind != TextBox.SOURCE_OPENBOX
                        || !s.text.text.startsWith(TextBox.decodeMojibake(VqsvText.Battle.CATCH_SUCCESS))
                        || s.battleOpenBox == null
                        || !s.battleOpenBox.visible()
                        || !s.battleOpenBox.widgetText(VqsvOpenBoxView.TEXT_WIDGET_ID).equals(s.text.text)
                        || s.battleOpenBox.widgetX(VqsvOpenBoxView.FRAME_WIDGET_ID, -1) != TextBox.OPENBOX_FRAME_X
                        || s.battleOpenBox.widgetY(VqsvOpenBoxView.FRAME_WIDGET_ID, -1) != TextBox.OPENBOX_FRAME_Y
                        || s.battleOpenBox.widgetW(VqsvOpenBoxView.FRAME_WIDGET_ID, -1) != TextBox.OPENBOX_FRAME_W
                        || s.battleOpenBox.widgetX(VqsvOpenBoxView.TEXT_WIDGET_ID, -1) != TextBox.OPENBOX_TEXT_X
                        || s.battleOpenBox.widgetY(VqsvOpenBoxView.TEXT_WIDGET_ID, -1) != TextBox.OPENBOX_TEXT_Y
                        || s.battleOpenBox.widgetW(VqsvOpenBoxView.TEXT_WIDGET_ID, -1) != TextBox.OPENBOX_TEXT_W
                        || !s.battleEnemyHiddenByCatch
                        || !s.battleCatchVisible
                        || s.battleCatchPhase != 3) {
                    throw new IllegalStateException("Expected catch success to use openbox.ui, state="
                            + s.battleStateName
                            + " text=" + (s.text == null ? "null" : s.text.text)
                            + " view=" + (s.battleOpenBox == null ? "null"
                            : s.battleOpenBox.widgetText(VqsvOpenBoxView.TEXT_WIDGET_ID))
                            + " kind=" + (s.text == null ? -1 : s.text.sourceUiKind)
                            + " hidden=" + s.battleEnemyHiddenByCatch
                            + " catchVisible=" + s.battleCatchVisible
                            + " phase=" + s.battleCatchPhase
                            + " trace=" + tailTrace(s, 18));
                }
                revealCheckpointText(s, 120);
                assertRenderedColorPixels(s, "openbox source sprite fill", 45, 134, 150, 40, 0xffffff, 2000);
                assertRenderedColorPixels(s, "openbox source sprite edge", 45, 134, 150, 40, 0xc7f0fe, 250);
                assertRenderedColorPixels(s, "openbox text", 47, 149, 146, 12, 0x1c6c91, 8);
                s.sourceStateTrace.add("SMOKE verified battle catch success renders through /data/ui/openbox.ui TextBox");
            } else if ("battle_p17_q1_h_effect_order".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(99);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                press0UntilAnyBattleState(s, 120, "P17");
                tickUntilBattleCatchPhase(s, 1, 260);
                if (s.battleCatchPhase != 1
                        || !s.battleCatchEffectVisible
                        || !s.battleEnemyHiddenByCatch) {
                    throw new IllegalStateException("Expected P17 q1 H effect over hidden target, phase="
                            + s.battleCatchPhase
                            + " effect=" + s.battleCatchEffectVisible
                            + " hidden=" + s.battleEnemyHiddenByCatch
                            + " trace=" + tailTrace(s, 18));
                }
                s.sourceStateTrace.add("SMOKE verified battle P17 q1 draw order H then aj over hidden target");
            } else if ("battle_p17_ah_type8_q1_capture_shrink".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(99);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                press0UntilAnyBattleState(s, 120, "P17");
                tickUntilCatchType8Step(s, 1, 7, 0, -10, 160);
                assertRenderedVisiblePixels(s, "P17 q1 ah type8 shrink target copy",
                        120, 70, 100, 105, 80);
            } else if ("battle_p17_ah_type8_q4_escape_effect".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(99);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                press0UntilAnyBattleState(s, 120, "P17");
                tickUntilCatchType8Step(s, 4, 6, 0, -12, 360);
                assertRenderedVisiblePixels(s, "P17 q4 ah type8 escape target copy",
                        120, 70, 100, 110, 80);
            } else if ("battle_p17_q4_fail_restore_enemy".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(99);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                press0UntilAnyBattleState(s, 120, "P17");
                tickUntilBattleCatchPhase(s, 4, 320);
                int guard = 0;
                while ("P17".equals(s.battleStateName) && guard++ < 160) {
                    s.tick();
                }
                if ("P17".equals(s.battleStateName) || s.battleEnemyHiddenByCatch || s.battleCatchVisible) {
                    throw new IllegalStateException("Expected P17 q4 fail to restore enemy before next state, state="
                            + s.battleStateName
                            + " hidden=" + s.battleEnemyHiddenByCatch
                            + " catchVisible=" + s.battleCatchVisible
                            + " trace=" + tailTrace(s, 18));
                }
                s.sourceStateTrace.add("SMOKE verified battle P17 q4 fail clears H/aj and restores target");
            } else if ("battle_catch_not_allowed_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{2, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "WARN", 80);
                if (!VqsvText.Battle.CATCH_NOT_ALLOWED.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Catch forbidden warning mismatch title="
                            + s.battleWarningTitle + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_catch_storage_bag".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                runCatchToOpenBox(s, 3000);
                if (s.sourcePets.size() != 2 || s.sourcePetBank.size() != 0) {
                    throw new IllegalStateException("Catch bag storage mismatch bag="
                            + s.sourcePets.size() + " bank=" + s.sourcePetBank.size());
                }
                assertCaughtPayload(s.sourcePets.get(1), s.battleEnemyVisualId,
                        "Catch bag stored payload");
                assertOpenBoxText(s, VqsvText.Battle.CATCH_SUCCESS + s.battleEnemyName,
                        "Catch bag success openbox");
                revealCheckpointText(s, 120);
            } else if ("battle_catch_storage_bank".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedSourcePets(s, 6);
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                runCatchToOpenBox(s, 3000);
                if (s.sourcePets.size() != 6 || s.sourcePetBank.size() != 1) {
                    throw new IllegalStateException("Catch bank storage mismatch bag="
                            + s.sourcePets.size() + " bank=" + s.sourcePetBank.size());
                }
                assertCaughtPayload(s.sourcePetBank.get(0), s.battleEnemyVisualId,
                        "Catch bank stored payload");
                assertOpenBoxText(s, VqsvText.Battle.CATCH_SUCCESS + s.battleEnemyName,
                        "Catch bank first success openbox");
                closeOpenBoxAndWaitForNextOpenBox(s, 300);
                assertOpenBoxText(s, VqsvText.Battle.CATCH_SENT_BANK,
                        "Catch bank second notice openbox");
                revealCheckpointText(s, 120);
            } else if ("battle_catch_storage_full_release".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedSourcePets(s, 6);
                seedSourceBank(s, 100);
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runtime.debugSetNextCatchRollForSmoke(0);
                s.current = runtime;
                runCatchToOpenBox(s, 3000);
                if (s.sourcePets.size() != 6 || s.sourcePetBank.size() != 100) {
                    throw new IllegalStateException("Catch full storage mismatch bag="
                            + s.sourcePets.size() + " bank=" + s.sourcePetBank.size());
                }
                assertOpenBoxText(s, VqsvText.Battle.CATCH_RELEASED_FULL,
                        "Catch full release openbox");
                revealCheckpointText(s, 120);
            } else if ("battle_elder_item_p4".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(4, new BagItem(4, 2, 1, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 98;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P4", 80);
                if (s.battleMenuDescriptions.length == 0
                        || s.battleMenuDescriptions[Math.max(0, s.battleMenuIndex)].isEmpty()) {
                    throw new IllegalStateException("Expected P4 choice.ui widget 53 item description, descriptions="
                            + java.util.Arrays.toString(s.battleMenuDescriptions));
                }
                assertRenderedColorPixels(s, "choice.ui P4 body fill", 44, 78, 151, 160, 0xbde4ef, 1800);
                assertRenderedColorPixels(s, "choice.ui P4 footer strip", 44, 238, 151, 14, 0x82cafb, 350);
                assertRenderedColorPixels(s, "choice.ui P4 description text", 57, 180, 125, 12, 0xffffff, 6);
            } else if ("battle_elder_item_target_p16".equals(checkpoint)
                    || "battle_p16_target_petstate_ui".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(4, new BagItem(4, 2, 1, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 98;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P4", 80);
                for (int i = 0; i < 12 && !"P16".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P16", 80);
                if (!"petstate".equals(s.battleUiMode)
                        || !VqsvText.Battle.PETSTATE_USE.equals(s.battleMenuAction)
                        || !traceContains(s, "battle P16 petstate.ui open")) {
                    throw new IllegalStateException("Expected P16 to render petstate.ui with use action, state="
                            + s.battleStateName + " ui=" + s.battleUiMode
                            + " action=" + s.battleMenuAction
                            + " trace=" + tailTrace(s, 12));
                }
                assertPetStateBinaryLayout("P16 petstate.ui");
                assertRenderedColorPixels(s, "P16 petstate.ui body fill", 46, 87, 151, 160, 0xbde4ef, 1800);
                assertRenderedColorPixels(s, "P16 petstate.ui footer strip", 46, 247, 151, 13, 0x82cafb, 350);
                assertRenderedColorPixels(s, "P16 petstate.ui row hp source bar", 73, 88, 26, 4, 0xfb7249, 40);
            } else if ("battle_p16_item_heal_hp".equals(checkpoint)) {
                setupElderItemBattle(s, 4, 1, 20, -1);
                driveItemUse(s);
                int expectedHp = Math.min(s.battlePlayerMaxHp, 20 + s.battlePlayerMaxHp * 50 / 100 + 50);
                if (!traceContains(s, "P16 game.b.w item=4") || s.battlePlayerHp != expectedHp) {
                    throw new IllegalStateException("Expected item4 HP heal to " + expectedHp + ", hp="
                            + s.battlePlayerHp + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_pp_restore".equals(checkpoint)) {
                setupElderItemBattle(s, 6, 1, -1, 0);
                driveItemUse(s);
                if (!traceContains(s, "P16 game.b.w item=6")
                        || s.sourcePets.get(0).skillCooldowns[0] != 25) {
                    throw new IllegalStateException("Expected item6 PP restore to 25, pp="
                            + s.sourcePets.get(0).skillCooldowns[0]
                            + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_hp_pp".equals(checkpoint)) {
                setupElderItemBattle(s, 8, 1, 40, 0);
                driveItemUse(s);
                int expectedHp = Math.min(s.battlePlayerMaxHp, 40 + s.battlePlayerMaxHp * 50 / 100 + 50);
                if (!traceContains(s, "P16 game.b.w item=8")
                        || s.battlePlayerHp != expectedHp
                        || s.sourcePets.get(0).skillCooldowns[0] != 20) {
                    throw new IllegalStateException("Expected item8 HP+PP, hp="
                            + s.battlePlayerHp + " pp=" + s.sourcePets.get(0).skillCooldowns[0]
                            + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_revive".equals(checkpoint)) {
                setupElderItemBattleWithDeadReserve(s, 11, 1, 0);
                driveItemUseToTarget(s, 1);
                SourceBattleUnit revived = SourceBattleUnit.playerFromSourcePets(s.sourcePets.subList(1, 2));
                int expectedHp = Math.min(revived.maxHp, revived.maxHp * 50 / 100 + 50);
                if (!traceContains(s, "P16 game.b.w item=11")
                        || revived.hp != expectedHp
                        || s.sourcePets.get(1).skillCooldowns[0] != 20) {
                    throw new IllegalStateException("Expected item11 revive, hp="
                            + revived.hp + " pp=" + s.sourcePets.get(1).skillCooldowns[0]
                            + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_clear_debuff".equals(checkpoint)) {
                SourceBattleRuntime runtime = setupElderItemBattle(s, 10, 1, -1, -1);
                runtime.debugPlayerDebuffForItemSmoke(s, 5, 8, 1);
                driveItemUse(s);
                if (!traceContains(s, "P16 game.b.w item=10")
                        || !traceContains(s, "debuffs=1->0")) {
                    throw new IllegalStateException("Expected item10 clear debuff, trace="
                            + tailTrace(s, 12));
                }
            } else if ("battle_p16_item_hp_full_warning".equals(checkpoint)) {
                setupElderItemBattle(s, 4, 1, -1, -1);
                driveItemUse(s);
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.ITEM_HP_FULL.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected HP full warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_pp_full_warning".equals(checkpoint)) {
                setupElderItemBattle(s, 6, 1, -1, -1);
                driveItemUse(s);
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.ITEM_PP_FULL.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected PP full warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_no_debuff_warning".equals(checkpoint)) {
                setupElderItemBattle(s, 10, 1, -1, -1);
                driveItemUse(s);
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.ITEM_NO_DEBUFF.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected no-debuff warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 10));
                }
            } else if ("battle_p16_item_success_msgwarm".equals(checkpoint)) {
                setupElderItemBattle(s, 4, 1, 20, -1);
                driveItemUse(s);
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.ITEM_USED.equals(s.battleWarningTitle)
                        || !traceContains(s, "P16 game.b.w item=4")) {
                    throw new IllegalStateException("Expected P16 success msgwarm before P1, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 12));
                }
                revealCheckpointText(s, 90);
                assertRenderedColorPixels(s, "P16 success msgwarm frame", 82, 116, 76, 54, 0x51d8e9, 200);
                assertRenderedColorPixels(s, "P16 success msgwarm text", 85, 119, 70, 12, 0x196b91, 8);
            } else if ("battle_p16_success_confirm_to_p1".equals(checkpoint)) {
                setupElderItemBattle(s, 4, 1, 20, -1);
                driveItemUse(s);
                press0UntilAnyBattleState(s, 80, "P1");
                if (!traceContains(s, "P16 game.b.w item=4")) {
                    throw new IllegalStateException("Expected P16 success confirm to keep apply trace, trace="
                            + tailTrace(s, 12));
                }
            } else if ("battle_p16_warning_return_petstate_preserve_cursor".equals(checkpoint)) {
                setupElderItemBattleWithReserve(s, 4, 1);
                driveItemCommandToP16(s);
                s.battleMenuIndex = 1;
                s.battleMenuScroll = 0;
                if (s.battleMenuIndex != 1) {
                    throw new IllegalStateException("Expected P16 cursor on reserve before warning, index="
                            + s.battleMenuIndex);
                }
                press0UntilAnyBattleState(s, 80, "WARN");
                if (!VqsvText.Battle.ITEM_HP_FULL.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected reserve HP full warning, warning="
                            + s.battleWarningTitle + " trace=" + tailTrace(s, 12));
                }
                press0UntilAnyBattleState(s, 80, "P16");
                if (!"petstate".equals(s.battleUiMode) || s.battleMenuIndex != 1) {
                    throw new IllegalStateException("Expected warning confirm to return P16 preserving cursor, state="
                            + s.battleStateName + " ui=" + s.battleUiMode + " index=" + s.battleMenuIndex
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p16_back_returns_p4".equals(checkpoint)) {
                setupElderItemBattle(s, 4, 1, 20, -1);
                driveItemCommandToP16(s);
                s.keyBack = true;
                tickUntilBattleState(s, "P4", 80);
                if (!"choice".equals(s.battleUiMode)
                        || !VqsvText.Battle.COMMAND_ITEM_PENDING.equals(s.battleMenuTitle)) {
                    throw new IllegalStateException("Expected P16 back to return P4 item list, state="
                            + s.battleStateName + " ui=" + s.battleUiMode + " title=" + s.battleMenuTitle
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p4_blocked_item_warning".equals(checkpoint)) {
                setupElderItemBattle(s, 13, 1, -1, -1);
                s.battleClickX = 98;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P4", 80);
                press0UntilAnyBattleState(s, 80, "WARN");
                if (!VqsvText.Battle.ITEM_NOT_IN_BATTLE.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected behavior 10 item13 blocked in battle, warning="
                            + s.battleWarningTitle + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p16_item_hp_pp_full_warning".equals(checkpoint)) {
                setupElderItemBattle(s, 8, 1, -1, -1);
                driveItemUse(s);
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.ITEM_HP_PP_FULL.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected HP+PP full warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_elder_pet_p5".equals(checkpoint)
                    || "battle_p5_petstate_source_rows".equals(checkpoint)
                    || "battle_p5_petstate_text_start".equals(checkpoint)
                    || "battle_p5_petstate_text_active".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 137;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P5", 80);
                if ("battle_p5_petstate_text_active".equals(checkpoint)) {
                    for (int i = 0; i < 36; i++) {
                        s.tick();
                    }
                }
                if (s.battleMenuIndex != 0
                        || !traceContains(s, "sourceProxy=sourcePets-as-game.d.f")
                        || !traceContains(s, "resetCursor=true")) {
                    throw new IllegalStateException("Expected P5 source W() cursor reset/proxy trace, index="
                            + s.battleMenuIndex + " trace=" + tailTrace(s, 12));
                }
                assertPetStateBinaryLayout("P5 petstate.ui");
                assertRenderedColorPixels(s, "petstate.ui body fill", 46, 87, 151, 160, 0xbde4ef, 1800);
                assertRenderedColorPixels(s, "petstate.ui footer strip", 46, 247, 151, 13, 0x82cafb, 350);
                assertRenderedColorPixels(s, "petstate.ui row hp source bar", 73, 88, 26, 4, 0xfb7249, 40);
                assertRenderedColorPixels(s, "petstate.ui detail text", 53, 178, 72, 12, 0x1c6c91, 6);
            } else if ("battle_p5_voluntary_switch_success".equals(checkpoint)
                    || "battle_p5_after_switch_active_pet".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, false);
                drivePetCommandToP5(s);
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 80, "P1", "WARN");
                if (!"P1".equals(s.battleStateName)
                        || s.sourcePets.get(0).speciesId != 92
                        || !traceContains(s, "P5 game.d.a(slot) validation=-1")) {
                    throw new IllegalStateException("Expected voluntary P5 switch to species92, state="
                            + s.battleStateName + " species0=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_click_reserve_success".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, false);
                drivePetCommandToP5(s);
                s.battleClickX = 80;
                s.battleClickY = 103;
                tickUntilAnyBattleState(s, 120, "P1", "WARN");
                if (!"P1".equals(s.battleStateName)
                        || s.sourcePets.get(0).speciesId != 92
                        || !traceContains(s, "P5 game.d.a(slot) validation=-1")) {
                    throw new IllegalStateException("Expected click on petstate reserve row to switch to species92, state="
                            + s.battleStateName + " species0=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_switch_transition".equals(checkpoint)
                    || "battle_p5_valid_switch_transition".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, false);
                drivePetCommandToP5(s);
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 80, "P15", "WARN");
                if (!"P15".equals(s.battleStateName)
                        || s.sourcePets.get(0).speciesId != 92
                        || !traceContains(s, "P15 player switch transition")) {
                    throw new IllegalStateException("Expected P5 valid switch to enter source state 15 transition, state="
                            + s.battleStateName + " species0=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_switch_transition_mid".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, false);
                drivePetCommandToP5(s);
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 80, "P15", "WARN");
                for (int i = 0; i < 10 && "P15".equals(s.battleStateName); i++) {
                    s.tick();
                }
                if (!"P15".equals(s.battleStateName)
                        || s.sourcePets.get(0).speciesId != 92
                        || !traceContains(s, "cposGroup=0 cposRow=1")) {
                    throw new IllegalStateException("Expected P15 cpos transition mid-frame, state="
                            + s.battleStateName + " species0=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_current_warning".equals(checkpoint)
                    || "battle_p5_current_pet_warning".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, false);
                drivePetCommandToP5(s);
                s.battleMenuIndex = 0;
                press0UntilAnyBattleState(s, 80, "P1", "WARN");
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.PET_ALREADY_ACTIVE.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected current pet warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_dead_warning".equals(checkpoint)
                    || "battle_p5_dead_pet_warning".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, true);
                drivePetCommandToP5(s);
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 80, "P1", "WARN");
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.PET_CANNOT_BATTLE.equals(s.battleWarningTitle)) {
                    throw new IllegalStateException("Expected dead pet warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_back_to_command".equals(checkpoint)) {
                setupElderPetSwitchBattle(s, false);
                drivePetCommandToP5(s);
                s.keyBack = true;
                tickUntilBattleState(s, "P20", 80);
                if (!"P20".equals(s.battleStateName)
                        || s.battleUiMode == null
                        || !"command".equals(s.battleUiMode)) {
                    throw new IllegalStateException("Expected P5 back to return command state, state="
                            + s.battleStateName + " ui=" + s.battleUiMode
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_forced_menu_visibility".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, true, 0, 40, 1, 1, 3);
                tickUntilBattleState(s, "P5", 260);
                if (!java.util.Arrays.equals(s.battleMenuIds, new int[]{0, 1})) {
                    throw new IllegalStateException("Expected forced P5 visible rows [0,1], ids="
                            + java.util.Arrays.toString(s.battleMenuIds)
                            + " trace=" + tailTrace(s, 12));
                }
            } else if ("battle_p5_forced_replacement_success".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, true, 0, 40, 1, 1, 3);
                tickUntilBattleState(s, "P5", 260);
                if (!java.util.Arrays.equals(s.battleMenuIds, new int[]{0, 1})) {
                    throw new IllegalStateException("Expected forced P5 to keep source visible rows [0,1], ids="
                            + java.util.Arrays.toString(s.battleMenuIds)
                            + " trace=" + tailTrace(s, 12));
                }
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 100, "P1", "WARN");
                if (!"P1".equals(s.battleStateName)
                        || s.sourcePets.get(0).speciesId != 92
                        || !traceContains(s, "forced=true")) {
                    throw new IllegalStateException("Expected forced P5 replacement to species92, state="
                            + s.battleStateName + " species0=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 14));
                }
            } else if ("battle_p5_forced_dead_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugQueueDebuffForSmoke(s, true, 0, 40, 1, 1, 3);
                tickUntilBattleState(s, "P5", 260);
                if (!java.util.Arrays.equals(s.battleMenuIds, new int[]{0, 1})) {
                    throw new IllegalStateException("Expected forced P5 visible rows before dead warning, ids="
                            + java.util.Arrays.toString(s.battleMenuIds)
                            + " trace=" + tailTrace(s, 12));
                }
                s.battleMenuIndex = 0;
                press0UntilAnyBattleState(s, 100, "P1", "WARN");
                if (!"WARN".equals(s.battleStateName)
                        || !VqsvText.Battle.PET_CANNOT_BATTLE.equals(s.battleWarningTitle)
                        || !traceContains(s, "validation=0 dead selectedIndex=0 forced=true")) {
                    throw new IllegalStateException("Expected forced P5 dead active warning, state="
                            + s.battleStateName + " warning=" + s.battleWarningTitle
                            + " trace=" + tailTrace(s, 14));
                }
            } else if ("battle_elder_switched_bunny_ko_forced_p5_no_exp".equals(checkpoint)) {
                runElderSwitchedBunnyKoForcedP5NoExpSmoke(s);
            } else if ("battle_elder_all_player_pets_ko_p9_no_exp".equals(checkpoint)) {
                runElderAllPlayerPetsKoP9NoExpSmoke(s);
            } else if ("battle_p5_status11_cleanup".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                s.current = runtime;
                tickUntilBattleState(s, "P20", 120);
                runtime.debugEnemyBuff11ForPetSwitchSmoke(s);
                s.battleClickX = 137;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P5", 80);
                s.battleMenuIndex = 1;
                press0UntilAnyBattleState(s, 100, "P15", "WARN");
                if (!"P15".equals(s.battleStateName)
                        || !traceContains(s, "clearedEnemyBuff11=1")
                        || !traceContains(s, "sourcePetOrder=[0, 1]")
                        || !s.sourcePets.get(0).sourceK()
                        || s.sourcePets.get(1).sourceK()) {
                    throw new IllegalStateException("Expected P5 switch to clear source buff11 and set K flags, state="
                            + s.battleStateName
                            + " active0=" + s.sourcePets.get(0).sourceK()
                            + " active1=" + s.sourcePets.get(1).sourceK()
                            + " trace=" + tailTrace(s, 16));
                }
            } else if ("battle_elder_shop_p11".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourceMoney = 1000;
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 176;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P11", 80);
            } else if ("battle_lab_live_shop_rows_supported".equals(checkpoint)) {
                setupLiveCheckpoint(s, "battle_p11_shop_full_source_item_rows");
                if (!"P11".equals(s.battleStateName)
                        || !"shopbuy".equals(s.battleUiMode)
                        || s.battleMenuIds.length == 0
                        || s.battleShopConfirmItemId != -1) {
                    throw new IllegalStateException("Expected live shop_rows checkpoint to open P11 shopbuy"
                            + " state=" + s.battleStateName
                            + " ui=" + s.battleUiMode
                            + " rows=" + s.battleMenuIds.length
                            + " confirmItem=" + s.battleShopConfirmItemId
                            + " trace=" + tailTrace(s, 18));
                }
                s.sourceStateTrace.add("SMOKE verified live battle lab shop_rows checkpoint");
            } else if (isBattleP11ShopCheckpoint(checkpoint)) {
                handleBattleP11ShopCheckpoint(s, checkpoint);
            } else if ("battle_elder_run_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 218;
                s.battleClickY = 300;
                tickUntilBattleState(s, "WARN", 80);
            } else if ("battle_elder_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                for (int i = 0; i < 260; i++) {
                    s.tick();
                }
            } else if (runBattleRouteCheckpointInExistingScene(checkpoint, s)) {
            } else {
                throw new IllegalArgumentException("Unknown checkpoint: " + checkpoint);
            }
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " textState=" + (s.text == null ? "none" : "present")
                    + " battleResult=" + s.battleResultIndex
                    + " battleBranch=" + s.battleBranchTarget
                    + " battleState=" + s.battleStateName
                    + " battleHp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " enemyOwned=" + s.battleEnemyOwnedSpecies
                    + " battleLog=" + s.battleLog
                    + " state101=" + s.sourceEventState(1, 0, 1)
                    + " state110=" + s.sourceEventState(1, 1, 0)
                    + " state106=" + s.sourceEventState(1, 0, 6)
                    + " money=" + s.sourceMoney
                    + " pets=" + s.sourcePets.size()
                    + " bankPets=" + s.sourcePetBank.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean runBattleEntryNpcSmokeIfNeeded(String checkpoint, String outPath) {
        if ("battle_entry_vs_elder_ui".equals(checkpoint)) {
            runBattleEntryVsElderSmoke(checkpoint, outPath);
            return true;
        }
        if ("battle_entry_vs_sophie_ui".equals(checkpoint)) {
            runBattleEntryNpcDescriptorSmoke(checkpoint, outPath,
                    VqsvBattleEventDescriptor.SCENE1_ROOM3_GROUP0_SOPHIE,
                    1, 1, true);
            return true;
        }
        if ("battle_entry_vs_bunny_no_npc_ui".equals(checkpoint)) {
            runBattleEntryNpcDescriptorSmoke(checkpoint, outPath,
                    VqsvBattleEventDescriptor.SCENE1_ROOM1_GROUP0_BUNNY,
                    1, 1, false);
            return true;
        }
        return false;
    }

    private static boolean runBattleP15SmokeIfNeeded(String checkpoint, String outPath) {
        if ("battle_p15_enemy_replacement_cpos_mid".equals(checkpoint)) {
            runBattleP15EnemyReplacementCposSmoke(checkpoint, outPath);
            return true;
        }
        return false;
    }

    private static boolean runBattleP9P24SmokeIfNeeded(String checkpoint, String outPath) {
        if ("battle_p9_first_loss_world_reset_one_hp".equals(checkpoint)) {
            runBattleP9P24FocusedSmoke(checkpoint, outPath, false);
            return true;
        }
        if ("battle_p24_revive_pay_full_restore".equals(checkpoint)) {
            runBattleP9P24FocusedSmoke(checkpoint, outPath, true);
            return true;
        }
        if ("battle_p24_revive_prompt".equals(checkpoint)) {
            runBattleP24PromptFocusedSmoke(checkpoint, outPath);
            return true;
        }
        if ("battle_p24_insufficient_money_warning".equals(checkpoint)) {
            runBattleP24InsufficientMoneyWarningSmoke(checkpoint, outPath);
            return true;
        }
        return false;
    }

    private static void runBattleP9P24FocusedSmoke(String checkpoint, String outPath, boolean p24) {
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            if (p24) {
                runP24RevivePayFullRestoreSmoke(s);
            } else {
                runElderAllPlayerPetsKoP9NoExpSmoke(s);
            }
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " money=" + s.sourceMoney
                    + " sourceM.i=" + s.sourceBattleLoseReviveArmed
                    + " sourceM.l=" + s.sourceBattleLoseWorldMode
                    + " pets=" + s.sourcePets.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void runBattleP24PromptFocusedSmoke(String checkpoint, String outPath) {
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            driveBattleToP24PromptForSmoke(s);
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " money=" + s.sourceMoney
                    + " warning=" + s.battleWarningTitle);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void runBattleP24InsufficientMoneyWarningSmoke(String checkpoint, String outPath) {
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            driveBattleToP24PromptForSmoke(s);
            s.sourceMoney = 0;
            for (int i = 0; i < 8; i++) {
                s.tick();
            }
            s.press0();
            for (int i = 0; i < 12; i++) {
                s.tick();
            }
            if (!"P24".equals(s.battleStateName)
                    || s.text == null
                    || !VqsvText.Battle.LOSE_NOT_ENOUGH_MONEY.equals(s.battleWarningTitle)
                    || !traceContains(s, "q.t(10000) false")) {
                throw new IllegalStateException("Expected P24 insufficient money source warning, state="
                        + s.battleStateName + " ui=" + s.battleUiMode
                        + " warning=" + s.battleWarningTitle
                        + " trace=" + tailTrace(s, 24));
            }
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " money=" + s.sourceMoney
                    + " warning=" + s.battleWarningTitle);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean runBattleRouteCheckpointInExistingScene(String checkpoint, VqsvIntroDemo.Scene s) {
        if ("route_sophie_after_battle_branch".equals(checkpoint)) {
            s.eventIndex = s.events.size();
            seedInitialDienMieu(s, "smoke Sophie route branch");
            s.current = VqsvBattleEventDescriptor.SCENE1_ROOM3_GROUP0_SOPHIE.runtime(s, 0);
            tickCurrentUntilDone(s, 500);
            assertActiveSourcePet(s, 68, "Sophie route initial Dien Mieu");
            if (s.battleResultIndex != 0 || s.battleBranchTarget != 78) {
                throw new IllegalStateException("Sophie battle branch mismatch result="
                        + s.battleResultIndex + " branch=" + s.battleBranchTarget);
            }
            VqsvBattleEventDescriptor.SCENE1_ROOM3_GROUP0_SOPHIE.consumeOp47(s);
            if (!traceContains(s, "op47 result=0 rawTarget=78 sourceCursor=76")) {
                throw new IllegalStateException("Expected Sophie op47 trace, trace=" + tailTrace(s, 16));
            }
            s.text = TextBox.dialog(s.font, VqsvText.Common.UNKNOWN_SPEAKER,
                    VqsvText.Scene1Room3BeforeTenYears.TEXT[26], 0);
            revealCheckpointText(s, 120);
            return true;
        }
        if ("route_bunny_after_battle_task".equals(checkpoint)) {
            s.eventIndex = s.events.size();
            seedInitialDienMieu(s, "smoke Bunny route task");
            seedRoom0Group0BunnyRewards(s);
            s.current = VqsvBattleEventDescriptor.SCENE1_ROOM1_GROUP0_BUNNY.runtime(s, -1);
            tickBattleAutoUntilDone(s, 3000);
            assertActiveSourcePet(s, 68, "Bunny route initial Dien Mieu");
            if (s.battleResultIndex != -1 || s.battleBranchTarget != -1) {
                throw new IllegalStateException("Bunny battle branch mismatch result="
                        + s.battleResultIndex + " branch=" + s.battleBranchTarget);
            }
            VqsvBattleEventDescriptor.SCENE1_ROOM1_GROUP0_BUNNY.consumeOp47(s);
            if (!traceContains(s, "op47 skip result=-1")) {
                throw new IllegalStateException("Expected Bunny op47 skip trace, trace=" + tailTrace(s, 16));
            }
            s.op56ActorVisibility(1, new int[]{50}, new int[]{0});
            s.op23MarkEventComplete(1, 0, 1);
            s.op14CompleteEvent(1, 1, 0);
            VqsvPostBattleDownstreamDescriptor.SCENE1_ROOM1_GROUP0_BUNNY.traceAndAssert(s);
            s.text = TextBox.taskTip(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER);
            revealCheckpointText(s, 90);
            return true;
        }
        if ("battle_sophie_loss_persists_dien_mieu_ko".equals(checkpoint)) {
            s.eventIndex = s.events.size();
            seedInitialDienMieu(s, "smoke Sophie pet persistence");
            s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                    new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
            tickCurrentUntilDone(s, 500);
            assertActiveSourcePet(s, 68, "Sophie loss persisted Dien Mieu");
            assertPayloadHp(s.sourcePets.get(0), 1, "Sophie loss active Dien Mieu source P9 reset");
            if (!traceContains(s, "post-loss world reset")) {
                throw new IllegalStateException("Expected P9 post-loss reset trace, trace="
                        + tailTrace(s, 16));
            }
            return true;
        }
        if ("op39_refresh_restores_pet_hp_pp".equals(checkpoint)) {
            s.eventIndex = s.events.size();
            SourcePetState pet = VqsvSourceStoryState.initialDienMieuPet();
            pet.skillCooldowns[0] = 0;
            pet.skillCooldowns[1] = 0;
            pet.sourcePayload = pet.toSourcePayload();
            pet.sourcePayload[6] = 0;
            s.sourcePets.add(pet);
            s.op39RefreshPets();
            int maxHp = sourceMaxHp(s.sourcePets.get(0));
            int pp0 = VqsvBattleTables.instance().skill(40).ppMax;
            int pp1 = VqsvBattleTables.instance().skill(45).ppMax;
            assertPayloadHp(s.sourcePets.get(0), maxHp, "op39 recovered Dien Mieu");
            if (s.sourcePets.get(0).skillCooldowns[0] != pp0
                    || s.sourcePets.get(0).skillCooldowns[1] != pp1) {
                throw new IllegalStateException("op39 PP restore mismatch pp="
                        + s.sourcePets.get(0).skillCooldowns[0] + ","
                        + s.sourcePets.get(0).skillCooldowns[1]
                        + " expected=" + pp0 + "," + pp1);
            }
            s.text = TextBox.taskTip("SMOKE op39 pet HP/PP restored");
            revealCheckpointText(s, 40);
            return true;
        }
        if ("battle_bunny_caught_pet_low_hp_state".equals(checkpoint)
                || "battle_bunny_caught_pet_p5_low_hp".equals(checkpoint)) {
            runBunnyCaughtPetRouteSmoke(s, "battle_bunny_caught_pet_p5_low_hp".equals(checkpoint));
            return true;
        }
        if ("route_elder_after_battle_reward_state".equals(checkpoint)) {
            s.eventIndex = s.events.size();
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.current = VqsvBattleEventDescriptor.SCENE1_ROOM0_GROUP6_ELDER.runtime(s, 0);
            tickBattleAutoUntilDone(s, 3000);
            if (s.battleResultIndex != 0 || s.battleBranchTarget != 10) {
                throw new IllegalStateException("Elder battle branch mismatch result="
                        + s.battleResultIndex + " branch=" + s.battleBranchTarget);
            }
            VqsvBattleEventDescriptor.SCENE1_ROOM0_GROUP6_ELDER.consumeOp47(s);
            if (!traceContains(s, "op47 result=0 rawTarget=10 sourceCursor=8")) {
                throw new IllegalStateException("Expected Elder op47 trace, trace=" + tailTrace(s, 16));
            }
            s.op31CurrencyReward(0, 0, 500);
            s.op17Item(0, 4, 10);
            s.op17Item(0, 11, 2);
            s.op19SpecialReward(5, 1);
            s.op23MarkEventComplete(1, 0, 4);
            s.op23MarkEventComplete(1, 0, 5);
            s.op14CompleteEvent(1, 0, 6);
            VqsvPostBattleDownstreamDescriptor.SCENE1_ROOM0_GROUP6_ELDER.traceAndAssert(s);
            s.text = TextBox.openBox(VqsvText.Scene1Room0Group6.FREE_WORLD);
            revealCheckpointText(s, 90);
            return true;
        }
        return false;
    }

    private static void runBunnyCaughtPetRouteSmoke(VqsvIntroDemo.Scene s, boolean openP5) {
        s.eventIndex = s.events.size();
        seedInitialDienMieu(s, openP5 ? "smoke Bunny caught P5 low HP" : "smoke Bunny caught low HP state");
        seedRoom0Group0BunnyRewards(s);
        s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
        tickBattleAutoUntilDone(s, 3000);
        SourcePetState bunny = findSourcePet(s, 34);
        if (bunny == null) {
            throw new IllegalStateException("Caught Bunny not found pets=" + s.sourcePets.size()
                    + " trace=" + tailTrace(s, 16));
        }
        int hp = payloadHp(bunny);
        int maxHp = sourceMaxHp(bunny);
        if (hp <= 0 || hp >= maxHp) {
            throw new IllegalStateException("Caught Bunny should keep low non-full HP, hp="
                    + hp + "/" + maxHp + " trace=" + tailTrace(s, 18));
        }
        if (!openP5) {
            s.sourceStateTrace.add("SMOKE verified caught Bunny low HP payload=" + hp + "/" + maxHp);
            s.text = TextBox.taskTip(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER);
            revealCheckpointText(s, 90);
            return;
        }
        s.sourcePets.add(new SourcePetState(2, 17, 7, 3, 2, 10, 45));
        s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 137;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P5", 80);
        int bunnyRow = -1;
        for (int i = 0; i < s.battleMenuIds.length; i++) {
            if (s.battleMenuIds[i] == 1) {
                bunnyRow = i;
                break;
            }
        }
        if (bunnyRow < 0 || !s.battleMenuValues[bunnyRow].contains(hp + "/" + maxHp)) {
            throw new IllegalStateException("P5 should show caught Bunny low HP row, ids="
                    + java.util.Arrays.toString(s.battleMenuIds)
                    + " values=" + java.util.Arrays.toString(s.battleMenuValues)
                    + " expected=" + hp + "/" + maxHp
                    + " trace=" + tailTrace(s, 18));
        }
        s.battleMenuIndex = bunnyRow;
        s.sourceStateTrace.add("SMOKE verified P5 caught Bunny low HP row="
                + s.battleMenuValues[bunnyRow]);
    }

    private static void runBattleP15EnemyReplacementCposSmoke(String checkpoint, String outPath) {
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            s.eventIndex = s.events.size();
            s.sourcePets.add(new SourcePetState(0, 0, 6, 3, 2, 1, 45));
            SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                    new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
            s.current = runtime;
            tickUntilBattleState(s, "P20", 120);
            runtime.debugEnemyPartyForSmoke(s, new int[][]{
                    {0, 1, 1},
                    {34, 1, 1}
            });
            s.battleClickX = 20;
            s.battleClickY = 300;
            tickUntilBattleState(s, "P3", 80);
            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            int guard = 0;
            while (!"P15".equals(s.battleStateName) && guard++ < 520) {
                s.tick();
            }
            if (!"P15".equals(s.battleStateName)
                    || !s.battleEnemyName.contains("Bunny")
                    || (s.battleP7EnemyOffsetX == 0 && s.battleP7EnemyOffsetY == 0)
                    || !traceContains(s, "sourceEntrySwapped=true")
                    || !traceContains(s, "cposFrames=")) {
                throw new IllegalStateException("Expected P15 enemy replacement cpos mid-frame, state="
                        + s.battleStateName
                        + " enemy=" + s.battleEnemyName
                        + " offset=" + s.battleP7EnemyOffsetX + "," + s.battleP7EnemyOffsetY
                        + " trace=" + tailTrace(s, 20));
            }
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " enemy=" + s.battleEnemyName
                    + " enemyOffset=" + s.battleP7EnemyOffsetX + "," + s.battleP7EnemyOffsetY);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void runBattleEntryVsElderSmoke(String checkpoint, String outPath) {
        try {
            assertNpcEnemyUiBinaryLayout("battle NPC VS entry");
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            s.loadScene1Room0(199, 218);
            s.setPlayerPositionApprox(199, 218);
            s.eventIndex = s.events.size();
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                    new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
            tickUntilBattleState(s, "NPCVS", 20);
            tickUntilNpcEnemyEntryStep(s, 7, 24);
            if (!s.battleNpcEnemyEntryVisible
                    || s.battleNpcEnemyPlayerCount != 1
                    || s.battleNpcEnemyEnemyCount != 1
                    || !"npcEnemy".equals(s.battleUiMode)
                    || !traceContains(s, "ui=/data/ui/npcEnemy.ui sprite=296")) {
                throw new IllegalStateException("Expected npcEnemy.ui NPC VS entry visible"
                        + " mode=" + s.battleUiMode
                        + " visible=" + s.battleNpcEnemyEntryVisible
                        + " playerCount=" + s.battleNpcEnemyPlayerCount
                        + " enemyCount=" + s.battleNpcEnemyEnemyCount
                        + " trace=" + tailTrace(s, 18));
            }
            assertRenderedVisiblePixels(s, "npcEnemy.ui VS center panel", 60, 70, 125, 135, 120);
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " battleUiMode=" + s.battleUiMode
                    + " npcEnemyStepFrame=" + s.battleNpcEnemyEntryStep + "/" + s.battleNpcEnemyEntryFrame
                    + " npcEnemyCounts=" + s.battleNpcEnemyPlayerCount + "/" + s.battleNpcEnemyEnemyCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void runBattleEntryNpcDescriptorSmoke(String checkpoint, String outPath,
                                                         VqsvBattleEventDescriptor descriptor,
                                                         int expectedPlayerCount,
                                                         int expectedEnemyCount,
                                                         boolean expectedNpcEntry) {
        try {
            assertNpcEnemyUiBinaryLayout("battle NPC VS descriptor");
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            if (descriptor == VqsvBattleEventDescriptor.SCENE1_ROOM3_GROUP0_SOPHIE) {
                s.loadScene1Room3Entry(120, 160);
            } else if (descriptor == VqsvBattleEventDescriptor.SCENE1_ROOM1_GROUP0_BUNNY) {
                s.loadScene1Room1(370, 176);
            } else {
                s.loadScene1Room0(199, 218);
            }
            s.eventIndex = s.events.size();
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.current = descriptor.runtime(s, 0);
            tickUntilBattleState(s, expectedNpcEntry ? "NPCVS" : "P0", 24);
            if (expectedNpcEntry) {
                tickUntilNpcEnemyEntryStep(s, 7, 24);
                if (!s.battleNpcEnemyEntryVisible
                        || s.battleNpcEnemyPlayerCount != expectedPlayerCount
                        || s.battleNpcEnemyEnemyCount != expectedEnemyCount
                        || !"npcEnemy".equals(s.battleUiMode)
                        || !traceContains(s, "npcEnemyEntry=true")
                        || !traceContains(s, "timeline=A")) {
                    throw new IllegalStateException("Expected descriptor npcEnemy.ui entry"
                            + " checkpoint=" + checkpoint
                            + " mode=" + s.battleUiMode
                            + " visible=" + s.battleNpcEnemyEntryVisible
                            + " stepFrame=" + s.battleNpcEnemyEntryStep + "/" + s.battleNpcEnemyEntryFrame
                            + " counts=" + s.battleNpcEnemyPlayerCount + "/" + s.battleNpcEnemyEnemyCount
                            + " trace=" + tailTrace(s, 20));
                }
                assertRenderedVisiblePixels(s, "npcEnemy.ui descriptor panel", 60, 70, 125, 135, 120);
            } else if (s.battleNpcEnemyEntryVisible
                    || "npcEnemy".equals(s.battleUiMode)
                    || !traceContains(s, "npcEnemyEntry=false")) {
                throw new IllegalStateException("Expected descriptor without npcEnemy.ui entry"
                        + " checkpoint=" + checkpoint
                        + " mode=" + s.battleUiMode
                        + " visible=" + s.battleNpcEnemyEntryVisible
                        + " trace=" + tailTrace(s, 20));
            }
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " expectedNpcEntry=" + expectedNpcEntry
                    + " battleState=" + s.battleStateName
                    + " battleUiMode=" + s.battleUiMode
                    + " npcEnemyStepFrame=" + s.battleNpcEnemyEntryStep + "/" + s.battleNpcEnemyEntryFrame
                    + " npcEnemyCounts=" + s.battleNpcEnemyPlayerCount + "/" + s.battleNpcEnemyEnemyCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    static void runSmokeSuite(String suite, String outDir) {
        String[] checkpoints = smokeSuiteCheckpoints(suite);
        java.io.File directory = new java.io.File(outDir);
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("smoke-suite-fail " + suite + " cannot create " + outDir);
            System.exit(1);
        }
        long start = System.currentTimeMillis();
        System.out.println("smoke-suite-start " + suite
                + " checkpoints=" + checkpoints.length
                + " outDir=" + directory.getPath());
        for (int i = 0; i < checkpoints.length; i++) {
            String checkpoint = checkpoints[i];
            java.io.File out = new java.io.File(directory, checkpoint + ".png");
            System.out.println("smoke-suite-step " + suite
                    + " " + (i + 1) + "/" + checkpoints.length
                    + " checkpoint=" + checkpoint
                    + " out=" + out.getPath());
            runSmokeCheckpoint(checkpoint, out.getPath());
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("smoke-suite-ok " + suite
                + " checkpoints=" + checkpoints.length
                + " outDir=" + directory.getPath()
                + " elapsedMs=" + elapsed);
    }

    private static String[] smokeSuiteCheckpoints(String suite) {
        if ("battle_quick".equals(suite)) {
            return BATTLE_QUICK_SUITE;
        }
        if ("panel_wheel".equals(suite)) {
            return PANEL_WHEEL_SUITE;
        }
        throw new IllegalArgumentException("Unknown smoke suite: " + suite);
    }

    private static void assertRenderedColorPixels(VqsvIntroDemo.Scene s, String label,
                                                  int x, int y, int w, int h,
                                                  int rgb, int minPixels) {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        s.render(g);
        g.dispose();
        int count = 0;
        int expected = rgb & 0xFFFFFF;
        for (int yy = Math.max(0, y); yy < Math.min(H, y + h); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(W, x + w); xx++) {
                if ((img.getRGB(xx, yy) & 0xFFFFFF) == expected) {
                    count++;
                }
            }
        }
        if (count < minPixels) {
            throw new IllegalStateException("Rendered pixel assertion failed for " + label
                    + " expectedColor=#" + Integer.toHexString(expected)
                    + " count=" + count
                    + " min=" + minPixels
                    + " region=[" + x + "," + y + "," + w + "," + h + "]"
                    + " state=" + s.battleStateName
                    + " text=" + (s.text == null ? "null" : s.text.text)
                    + " colors=" + dominantRegionColors(img, x, y, w, h, 5)
                    + " trace=" + tailTrace(s, 14));
        }
        s.sourceStateTrace.add("SMOKE pixel verified " + label
                + " color=#" + Integer.toHexString(expected) + " count=" + count);
    }

    private static void assertRenderedVisiblePixels(VqsvIntroDemo.Scene s, String label,
                                                    int x, int y, int w, int h, int minPixels) {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        s.render(g);
        g.dispose();
        int count = 0;
        for (int yy = Math.max(0, y); yy < Math.min(H, y + h); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(W, x + w); xx++) {
                int argb = img.getRGB(xx, yy);
                int rgb = argb & 0xFFFFFF;
                if ((argb >>> 24) != 0
                        && rgb != 0x092a3a
                        && rgb != 0x16525e
                        && rgb != 0x113652
                        && rgb != 0x000000) {
                    count++;
                }
            }
        }
        if (count < minPixels) {
            throw new IllegalStateException("Rendered visible-pixel assertion failed for " + label
                    + " count=" + count
                    + " min=" + minPixels
                    + " region=[" + x + "," + y + "," + w + "," + h + "]"
                    + " state=" + s.battleStateName
                    + " phase=" + s.battleCatchPhase
                    + " effect=[" + s.battleCatchEffectScale10
                    + "," + s.battleCatchEffectDx
                    + "," + s.battleCatchEffectDy + "]"
                    + " colors=" + dominantRegionColors(img, x, y, w, h, 5)
                    + " trace=" + tailTrace(s, 14));
        }
        s.sourceStateTrace.add("SMOKE pixel verified " + label + " visibleCount=" + count);
    }

    private static void assertRenderedDarkerThanSnapshot(VqsvIntroDemo.Scene s, String label,
                                                         int x, int y, int w, int h, int minDelta) {
        if (s.battleBackgroundSnapshot == null) {
            throw new IllegalStateException(label + " missing source snapshot");
        }
        BufferedImage rendered = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rendered.createGraphics();
        s.render(g);
        g.dispose();
        long snapshotSum = 0;
        long renderedSum = 0;
        int count = 0;
        for (int yy = Math.max(0, y); yy < Math.min(H, y + h); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(W, x + w); xx++) {
                int source = s.battleBackgroundSnapshot.getRGB(xx, yy);
                int actual = rendered.getRGB(xx, yy);
                if (((source >>> 24) == 0) || ((actual >>> 24) == 0)) {
                    continue;
                }
                snapshotSum += brightness(source);
                renderedSum += brightness(actual);
                count++;
            }
        }
        if (count == 0) {
            throw new IllegalStateException(label + " no comparable pixels");
        }
        long snapshotAvg = snapshotSum / count;
        long renderedAvg = renderedSum / count;
        if (snapshotAvg - renderedAvg < minDelta) {
            throw new IllegalStateException(label + " expected darker render, snapshotAvg="
                    + snapshotAvg + " renderedAvg=" + renderedAvg + " minDelta=" + minDelta
                    + " colors=" + dominantRegionColors(rendered, x, y, w, h, 5)
                    + " trace=" + tailTrace(s, 14));
        }
        s.sourceStateTrace.add("SMOKE pixel verified " + label
                + " snapshotAvg=" + snapshotAvg + " renderedAvg=" + renderedAvg);
    }

    private static void assertP7SourceSpriteFrame(VqsvIntroDemo.Scene s, String label,
                                                  boolean playerSide, int expectedState,
                                                  int expectedCursor, int expectedVisualId,
                                                  int maxMismatch) {
        int visual = playerSide ? s.battlePlayerVisualId : s.battleEnemyVisualId;
        int state = playerSide ? s.battleP7BaseStatePlayerSide : s.battleP7BaseStateEnemySide;
        int cursor = playerSide ? s.battleP7BaseCursorPlayerSide : s.battleP7BaseCursorEnemySide;
        if (visual != expectedVisualId || state != expectedState || cursor != expectedCursor) {
            throw new IllegalStateException(label + " state/cursor mismatch side="
                    + (playerSide ? "player" : "enemy")
                    + " expectedVisual=" + expectedVisualId + " actualVisual=" + visual
                    + " expectedState=" + expectedState + " actualState=" + state
                    + " expectedCursor=" + expectedCursor + " actualCursor=" + cursor
                    + " trace=" + tailTrace(s, 16));
        }

        BufferedImage actual = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D actualG = actual.createGraphics();
        s.render(actualG);
        actualG.dispose();

        BufferedImage expected = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D expectedG = expected.createGraphics();
        SpriteAnim anim = SpriteAnim.load(visual);
        anim.setState(expectedState);
        int frames = anim.data.anim == null || anim.data.anim.length == 0
                ? 0 : anim.data.anim[anim.state].length / 2;
        anim.cursor = Math.max(0, Math.min(expectedCursor, Math.max(0, frames - 1)));
        anim.draw(expectedG, smokeSourceBattleActorX(s, playerSide) + smokeSideOffsetX(s, playerSide),
                smokeSourceBattleActorY(s, playerSide) + smokeSideOffsetY(s, playerSide),
                playerSide ? 0 : 1);
        expectedG.dispose();

        int compared = 0;
        int mismatch = 0;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int exp = expected.getRGB(x, y);
                int alpha = exp >>> 24;
                if (alpha < 250) {
                    continue;
                }
                compared++;
                if ((actual.getRGB(x, y) & 0xFFFFFF) != (exp & 0xFFFFFF)) {
                    mismatch++;
                }
            }
        }
        if (compared < 8 || mismatch > maxMismatch) {
            throw new IllegalStateException(label + " source sprite pixel compare failed"
                    + " side=" + (playerSide ? "player" : "enemy")
                    + " visual=" + visual
                    + " state=" + state
                    + " cursor=" + cursor
                    + " compared=" + compared
                    + " mismatch=" + mismatch
                    + " maxMismatch=" + maxMismatch
                    + " anchor=" + smokeSourceBattleActorX(s, playerSide)
                    + "," + smokeSourceBattleActorY(s, playerSide)
                    + " sourceCposGroup=" + smokeSourceCposGroup(s)
                    + " trace=" + tailTrace(s, 18));
        }
        s.sourceStateTrace.add("SMOKE source-frame compare " + label
                + " side=" + (playerSide ? "player" : "enemy")
                + " visual=" + visual
                + " state=" + state
                + " cursor=" + cursor
                + " compared=" + compared
                + " mismatch=" + mismatch
                + " source=SpriteAnim+pos/cpos");
    }

    private static int smokeSourceBattleActorX(VqsvIntroDemo.Scene s, boolean playerSide) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(smokeSourceCposGroup(s));
        int at = smokeSourcePosQuadOffset(s, playerSide);
        if (row.length >= at + 4) {
            return row[at];
        }
        return playerSide ? 70 : 177;
    }

    private static int smokeSourceBattleActorY(VqsvIntroDemo.Scene s, boolean playerSide) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(smokeSourceCposGroup(s));
        int at = smokeSourcePosQuadOffset(s, playerSide);
        if (row.length >= at + 4) {
            return row[at + 1];
        }
        return playerSide ? 223 : 103;
    }

    private static void assertP8ExpPosMidRow0(VqsvIntroDemo.Scene s, String label) {
        short[] row = VqsvBattleAnimationTables.instance().posRow(smokeSourceCposGroup(s));
        int at = smokeSourcePosQuadOffset(s, true);
        if (smokeSourceCposGroup(s) != 0
                || row.length < at + 4
                || row[at] != 70
                || row[at + 1] != 223
                || row[at + 2] != 36
                || row[at + 3] != 206) {
            throw new IllegalStateException(label + " expected game.d.am[0] player actor/marker"
                    + " actor=70,223 marker=36,206 but got group=" + smokeSourceCposGroup(s)
                    + " at=" + at
                    + " row=" + java.util.Arrays.toString(row)
                    + " trace=" + tailTrace(s, 24));
        }
        s.sourceStateTrace.add("SMOKE verified " + label
                + " game.h.a(am[0][4],am[0][5])=70,223"
                + " al[0].b(am[0][6],am[0][7])=36,206");
    }

    private static int smokeSourcePosQuadOffset(VqsvIntroDemo.Scene s, boolean playerSide) {
        if (smokeSourceCposGroup(s) == 1) {
            return playerSide ? 8 : 0;
        }
        return playerSide ? 4 : 0;
    }

    private static int smokeSourceCposGroup(VqsvIntroDemo.Scene s) {
        return s.battleMode == 0 ? (s.battleBackgroundMode == 1 ? 2 : 0) : 1;
    }

    private static int smokeSideOffsetX(VqsvIntroDemo.Scene s, boolean playerSide) {
        return playerSide ? s.battleP7PlayerOffsetX : s.battleP7EnemyOffsetX;
    }

    private static int smokeSideOffsetY(VqsvIntroDemo.Scene s, boolean playerSide) {
        return playerSide ? s.battleP7PlayerOffsetY : s.battleP7EnemyOffsetY;
    }

    private static int brightness(int argb) {
        return (((argb >> 16) & 0xff) + ((argb >> 8) & 0xff) + (argb & 0xff)) / 3;
    }

    private static void assertPetStateBinaryLayout(String label) {
        VqsvUiLayout layout = VqsvUiLayout.load("petstate.ui");
        int[] required = {1, 2, 3, 4, 5, 6, 14, 16, 17, 48, 51, 65, 75, 76};
        for (int id : required) {
            if (layout.widget(id) == null) {
                throw new IllegalStateException(label + " missing widget id=" + id
                        + " source=" + (layout.binarySource ? "binary" : "decoded")
                        + " count=" + layout.widgetCount());
            }
        }
        if (!layout.binarySource || layout.widgetCount() < 70) {
            throw new IllegalStateException(label + " expected binary petstate.ui layout, source="
                    + (layout.binarySource ? "binary" : "decoded")
                    + " count=" + layout.widgetCount());
        }
    }

    private static void assertBattleUiBinaryLayout(String label) {
        VqsvUiLayout layout = VqsvUiLayout.load("battle.ui");
        int[] required = {1, 2, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                38, 39, 40, 41, 42, 55, 56, 57, 58, 59};
        for (int id : required) {
            if (layout.widget(id) == null) {
                throw new IllegalStateException(label + " missing battle.ui widget id=" + id
                        + " source=" + (layout.binarySource ? "binary" : "decoded")
                        + " count=" + layout.widgetCount());
            }
        }
        if (!layout.binarySource || layout.widgetCount() < 59) {
            throw new IllegalStateException(label + " expected binary battle.ui layout, source="
                    + (layout.binarySource ? "binary" : "decoded")
                    + " count=" + layout.widgetCount());
        }
    }

    private static void assertNpcEnemyUiBinaryLayout(String label) {
        VqsvUiLayout layout = VqsvUiLayout.load("npcEnemy.ui");
        int[] required = {1, 2, 3, 4, 5, 6, 7, 18, 19, 30, 31, 32, 33, 34, 35, 36};
        for (int id : required) {
            if (layout.widget(id) == null) {
                throw new IllegalStateException(label + " missing npcEnemy.ui widget id=" + id
                        + " source=" + (layout.binarySource ? "binary" : "decoded")
                        + " count=" + layout.widgetCount());
            }
        }
        if (!layout.binarySource || layout.widgetCount() < 36) {
            throw new IllegalStateException(label + " expected binary npcEnemy.ui layout, source="
                    + (layout.binarySource ? "binary" : "decoded")
                    + " count=" + layout.widgetCount());
        }
    }

    private static String dominantRegionColors(BufferedImage img, int x, int y, int w, int h, int limit) {
        java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
        for (int yy = Math.max(0, y); yy < Math.min(H, y + h); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(W, x + w); xx++) {
                int rgb = img.getRGB(xx, yy) & 0xFFFFFF;
                counts.put(rgb, counts.getOrDefault(rgb, 0) + 1);
            }
        }
        return counts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> "#" + String.format("%06x", e.getKey()) + ":" + e.getValue())
                .collect(java.util.stream.Collectors.joining(","));
    }

    static void setupLiveCheckpoint(VqsvIntroDemo.Scene s, String checkpoint) {
        s.eventIndex = s.events.size();
        if ("battle_bunny_command_ui".equals(checkpoint)) {
            setupBunnyMapBackedBattleEntry(s);
            seedInitialDienMieu(s, "live Bunny command UI");
            seedRoom0Group0BunnyRewards(s);
            s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                    new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
            tickUntilBattleState(s, "P20", 120);
        } else if ("battle_bunny_catch_p21".equals(checkpoint)) {
            setupBunnyMapBackedBattleEntry(s);
            seedInitialDienMieu(s, "live Bunny P21");
            seedRoom0Group0BunnyRewards(s);
            s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                    new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
            tickUntilBattleState(s, "P20", 120);
            s.battleClickX = 56;
            s.battleClickY = 300;
            tickUntilBattleState(s, "P21", 80);
        } else if ("battle_bunny_catch_p17".equals(checkpoint)) {
            setupBunnyMapBackedBattleEntry(s);
            seedInitialDienMieu(s, "live Bunny P17");
            seedRoom0Group0BunnyRewards(s);
            s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                    new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
            driveBunnyTutorialUntilFirstCatchP17(s, 2500);
        } else if ("battle_elder_command_ui".equals(checkpoint)) {
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
            s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                    new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
            tickUntilBattleState(s, "P20", 120);
        } else if ("battle_levelup_command_ui".equals(checkpoint)) {
            SourcePetState pet = new SourcePetState(0, 6, 11, 3, 2, 0, -1);
            pet.sourcePayload[7] = BattleUnit.sourceLevelThreshold(12) - 10;
            s.sourcePets.add(pet);
            s.current = new SourceBattleRuntime(52, new int[]{0, 1, 1},
                    new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
            tickUntilBattleState(s, "P20", 120);
        } else if ("battle_exp_p8_frame0".equals(checkpoint)
                || "battle_exp_p8_frame1".equals(checkpoint)
                || "battle_exp_p8_mid".equals(checkpoint)
                || "battle_exp_p8_target_hold".equals(checkpoint)
                || "battle_exp_normal_gain_no_levelup_anim".equals(checkpoint)) {
            setupNormalP8ExpInitialFrame(s, "live " + checkpoint);
            if ("battle_exp_p8_frame1".equals(checkpoint)) {
                s.tick();
            } else if ("battle_exp_p8_mid".equals(checkpoint)) {
                for (int i = 0; i < 10; i++) {
                    s.tick();
                }
            } else if ("battle_exp_p8_target_hold".equals(checkpoint)
                    || "battle_exp_normal_gain_no_levelup_anim".equals(checkpoint)) {
                int expected = sourceExpectedExpAward(5, 1, 7, 1);
                int guard = 0;
                while (s.battleLevelUpView != null
                        && s.battleLevelUpView.expValue < expected
                        && guard++ < 120) {
                    s.tick();
                }
            }
        } else if ("battle_elder_pet_p5".equals(checkpoint)) {
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
            s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                    new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
            tickUntilBattleState(s, "P20", 120);
            s.battleClickX = 137;
            s.battleClickY = 300;
            tickUntilBattleState(s, "P5", 80);
        } else if ("battle_elder_skill15_p7".equals(checkpoint)) {
            enterElderP7WithSkills(s, new int[]{15, 45}, 0);
            tickUntilBattleP7Phase(s, 1, 80);
        } else if ("battle_elder_skill45_p7".equals(checkpoint)) {
            enterElderP7WithSkillIndex(s, 1);
            tickUntilBattleP7Phase(s, 1, 80);
        } else if ("battle_p11_shop_full_source_item_rows".equals(checkpoint)
                || "battle_p11_shop_hover_preview_no_confirm".equals(checkpoint)
                || "battle_p11_shop_mouse_wheel_no_confirm".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            openBattleShopP11(s);
        } else if ("battle_p11_shop_msgyn_open".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 5);
            openShopConfirmForItem(s, 5);
        } else if ("battle_sophie_command_ui".equals(checkpoint)) {
            s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                    new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
            tickUntilBattleState(s, "P2", 120);
        } else {
            throw new IllegalArgumentException("Unknown live checkpoint: " + checkpoint);
        }
        s.clearInputForManualCheckpoint();
        if (s.current instanceof SourceBattleRuntime) {
            ((SourceBattleRuntime) s.current).clearInputLatchForManualCheckpoint();
        }
    }

    static void driveRoute(VqsvIntroDemo.Scene s, String route) {
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

    private static void runCatchToDone(VqsvIntroDemo.Scene s, int maxTicks) {
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 56;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P21", 80);
        for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P17", 80);
        tickCurrentUntilDone(s, maxTicks);
    }

    private static void runCatchToOpenBox(VqsvIntroDemo.Scene s, int maxTicks) {
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 56;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P21", 80);
        for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P17", 80);
        int guard = 0;
        while (guard++ < maxTicks) {
            if (s.text != null && s.text.sourceUiKind == TextBox.SOURCE_OPENBOX) {
                return;
            }
            s.tick();
        }
        throw new IllegalStateException("Catch openbox not reached state=" + s.battleStateName
                + " result=" + s.battleResultIndex
                + " text=" + (s.text == null ? "null" : s.text.text)
                + " trace=" + tailTrace(s, 18));
    }

    private static void closeOpenBoxAndWaitForNextOpenBox(VqsvIntroDemo.Scene s, int maxTicks) {
        if (s.text == null || s.text.sourceUiKind != TextBox.SOURCE_OPENBOX) {
            throw new IllegalStateException("No openbox to close text="
                    + (s.text == null ? "null" : s.text.text));
        }
        String firstText = s.text.text;
        revealCheckpointText(s, 120);
        int guard = 0;
        while (s.text != null && firstText.equals(s.text.text) && guard++ < maxTicks) {
            s.press0();
            s.tick();
        }
        if (s.text != null && s.text.sourceUiKind == TextBox.SOURCE_OPENBOX
                && !firstText.equals(s.text.text)) {
            return;
        }
        throw new IllegalStateException("Next openbox not reached state=" + s.battleStateName
                + " text=" + (s.text == null ? "null" : s.text.text)
                + " trace=" + tailTrace(s, 18));
    }

    private static void assertOpenBoxText(VqsvIntroDemo.Scene s, String expected, String label) {
        String actual = s.text == null ? null : s.text.text;
        if (s.text == null || s.text.sourceUiKind != TextBox.SOURCE_OPENBOX
                || !TextBox.decodeMojibake(expected).equals(actual)) {
            throw new IllegalStateException(label + " mismatch expected="
                    + TextBox.decodeMojibake(expected)
                    + " actual=" + actual
                    + " kind=" + (s.text == null ? -1 : s.text.sourceUiKind)
                    + " state=" + s.battleStateName
                    + " trace=" + tailTrace(s, 18));
        }
        if (s.battleOpenBox == null
                || !s.battleOpenBox.visible()
                || !actual.equals(s.battleOpenBox.widgetText(VqsvOpenBoxView.TEXT_WIDGET_ID))) {
            throw new IllegalStateException(label + " openbox view mismatch expected="
                    + actual
                    + " view=" + (s.battleOpenBox == null ? "null"
                    : s.battleOpenBox.widgetText(VqsvOpenBoxView.TEXT_WIDGET_ID))
                    + " state=" + s.battleStateName
                    + " trace=" + tailTrace(s, 18));
        }
        s.sourceStateTrace.add("SMOKE verified " + label + " text=" + actual);
    }

    private static void setupRoom1BunnySavePoint(VqsvIntroDemo.Scene s) {
        s.eventIndex = room1BunnyOp13EventIndex();
        s.loadScene1Room1(370, 176);
        s.setPlayerPositionApprox(374, 180);
        seedInitialDienMieu(s, "smoke save point after elder Bunny task");
        s.sourceBagItems.put(1, new BagItem(1, 2, 0, false));
        s.sourceBagItems.put(4, new BagItem(4, 5, 1, false));
        s.op39RefreshPets();
        s.sourceGameCF = false;
        s.op14CompleteEvent(1, 0, 0);
        s.sourceStateTrace.add("SMOKE setup room1 Bunny save point eventIndex=" + s.eventIndex);
    }

    private static int room1BunnyOp13EventIndex() {
        if (VqsvIntroDemo.Scene.room1BunnyOp13EventIndex < 0) {
            throw new IllegalStateException("room1 Bunny op13 event index was not registered");
        }
        return VqsvIntroDemo.Scene.room1BunnyOp13EventIndex;
    }

    private static void setupOldInvalidRoom1BunnySave(VqsvIntroDemo.Scene s) {
        s.eventIndex = 250;
        s.loadScene1Room1(16, 272);
        s.setPlayerPositionApprox(16, 272);
        seedInitialDienMieu(s, "smoke old invalid room1 Bunny save");
        s.op14CompleteEvent(1, 1, 1);
        if (s.sourceEventStateComplete(1, 1, 0)) {
            throw new IllegalStateException("Old invalid save setup must not complete Bunny group0");
        }
        if (s.playerIntersectsSourceRect(370, 176, 80, 32)) {
            throw new IllegalStateException("Old invalid save setup unexpectedly intersects op13");
        }
        VqsvSaveRuntime.save(s);
        s.sourceStateTrace.add("SMOKE setup old invalid room1 Bunny save eventIndex=250"
                + " player=[16,272] state111=3 state110=0");
    }

    private static void tickBoot(BootFlowState state, GameStateMachine states, int count, InputSnapshot input) {
        for (int index = 0; index < count; index++) {
            state.tick(input, states);
        }
    }

    private static InputSnapshot emptyBootInput() {
        return new InputSnapshot(new HashSet<>(), new HashSet<>());
    }

    private static InputSnapshot bootInput(int keyCode) {
        HashSet<Integer> pressed = new HashSet<>();
        pressed.add(keyCode);
        return new InputSnapshot(pressed, pressed);
    }

    private static int snapshotPlayerCoord(String snapshot, int coordIndex) {
        int start = snapshot.indexOf("player=[");
        if (start < 0) {
            throw new IllegalStateException("Snapshot missing player field: " + snapshot);
        }
        start += "player=[".length();
        int end = snapshot.indexOf(']', start);
        if (end < 0) {
            throw new IllegalStateException("Snapshot malformed player field: " + snapshot);
        }
        String[] parts = snapshot.substring(start, end).split(",");
        if (coordIndex < 0 || coordIndex >= parts.length) {
            throw new IllegalStateException("Snapshot player coord missing index=" + coordIndex
                    + " snapshot=" + snapshot);
        }
        return Integer.parseInt(parts[coordIndex].trim());
    }

    private static VqsvIntroDemo.Scene setupCatchChanceStatusMenu(int targetDebuffId, boolean attackerForm11) {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        runtime.debugSetCatchStatusForSmoke(s, targetDebuffId, attackerForm11);
        s.battleClickX = 56;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P21", 80);
        return s;
    }

    private static int catchMenuChanceForItem(VqsvIntroDemo.Scene s, int itemId) {
        for (int i = 0; i < s.battleMenuIds.length; i++) {
            if (s.battleMenuIds[i] == itemId) {
                String value = i < s.battleMenuValues.length ? s.battleMenuValues[i] : "";
                if (!value.endsWith("%")) {
                    throw new IllegalStateException("Catch chance value missing percent item="
                            + itemId + " value=" + value);
                }
                return Integer.parseInt(value.substring(0, value.length() - 1));
            }
        }
        throw new IllegalStateException("Catch chance item not listed item=" + itemId
                + " ids=" + java.util.Arrays.toString(s.battleMenuIds));
    }

    private static SourceBattleRuntime setupPhase10AStatusBattle(VqsvIntroDemo.Scene s) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        return runtime;
    }

    private static void assertPhase10AStatusSlots(VqsvIntroDemo.Scene s, boolean playerSide,
                                                  String label, int[] expectedIcons,
                                                  int[] expectedDurations) {
        int count = playerSide ? s.battlePlayerStatusCount : s.battleEnemyStatusCount;
        int[] icons = playerSide ? s.battlePlayerStatusIconCells : s.battleEnemyStatusIconCells;
        int[] durations = playerSide ? s.battlePlayerStatusDurationCells : s.battleEnemyStatusDurationCells;
        if (count != expectedIcons.length) {
            throw new IllegalStateException("Expected Phase10A " + label
                    + " status count " + expectedIcons.length + " but got " + count
                    + " icons=" + java.util.Arrays.toString(icons)
                    + " durations=" + java.util.Arrays.toString(durations)
                    + " trace=" + tailTrace(s, 16));
        }
        for (int i = 0; i < expectedIcons.length; i++) {
            if (icons[i] != expectedIcons[i] || durations[i] != expectedDurations[i]) {
                throw new IllegalStateException("Expected Phase10A " + label
                        + " slot " + i + " icon/duration "
                        + expectedIcons[i] + "/" + expectedDurations[i]
                        + " but got " + icons[i] + "/" + durations[i]
                        + " icons=" + java.util.Arrays.toString(icons)
                        + " durations=" + java.util.Arrays.toString(durations)
                        + " trace=" + tailTrace(s, 16));
            }
        }
        s.sourceStateTrace.add("SMOKE Phase10A status slots verified " + label
                + " icons=" + java.util.Arrays.toString(expectedIcons)
                + " durations=" + java.util.Arrays.toString(expectedDurations));
    }

    private static SourceBattleRuntime setupElderItemBattle(VqsvIntroDemo.Scene s, int itemId,
                                                            int itemCount, int hp, int firstPp) {
        s.eventIndex = s.events.size();
        SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
        if (firstPp >= 0) {
            pet.skillCooldowns[0] = firstPp;
        }
        pet.sourcePayload = pet.toSourcePayload();
        if (hp >= 0) {
            pet.sourcePayload[6] = hp;
        }
        s.sourcePets.add(pet);
        s.sourceBagItems.put(itemId, new BagItem(itemId, itemCount, 1, false));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        return runtime;
    }

    private static boolean isBattleP11ShopCheckpoint(String checkpoint) {
        return checkpoint.startsWith("battle_p11_shop_")
                || checkpoint.startsWith("battle_p11_shopbuy_");
    }

    private static void handleBattleP11ShopCheckpoint(VqsvIntroDemo.Scene s, String checkpoint) {
        if ("battle_p11_shop_full_source_item_rows".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            openBattleShopP11(s);
            int expected = VqsvBattleTables.instance().rowCount(4);
            if (s.battleMenuIds.length != expected
                    || !battleMenuContains(s, 5)
                    || !battleMenuContains(s, 12)) {
                throw new IllegalStateException("Expected P11 shop to expose all source aq.c[4] rows"
                        + " expected=" + expected
                        + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                        + " trace=" + tailTrace(s, 12));
            }
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shopbuy_wallet_source_widgets".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            s.sourceMoney = 1234;
            s.sourceBadges = 7;
            openBattleShopP11(s);
            if (!"shopbuy".equals(s.battleUiMode)
                    || !traceContains(s, "game.h.a(4,0) shopbuy.ui")
                    || !traceContains(s, "money(q.E)=1234")
                    || !traceContains(s, "badges(q.G)=7")) {
                throw new IllegalStateException("Expected P11 shopbuy.ui wallet source trace, ui="
                        + s.battleUiMode + " trace=" + tailTrace(s, 18));
            }
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shop_hover_preview_no_confirm".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            openBattleShopP11(s);
            s.battleHoverX = 60;
            s.battleHoverY = 143;
            for (int i = 0; i < 20 && s.battleMenuIndex != 2; i++) {
                s.tick();
            }
            if (!"P11".equals(s.battleStateName)
                    || !"shopbuy".equals(s.battleUiMode)
                    || s.battleMenuIndex != 2
                    || s.battleShopConfirmItemId != -1) {
                throw new IllegalStateException("Expected P11 hover to preview row 2 without shopconfirm"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " confirmItem=" + s.battleShopConfirmItemId
                        + " trace=" + tailTrace(s, 18));
            }
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shop_mouse_wheel_no_confirm".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            openBattleShopP11(s);
            for (int i = 0; i < 12; i++) {
                s.tick();
            }
            s.mouseWheel(1);
            s.tick();
            if (!"P11".equals(s.battleStateName)
                    || !"shopbuy".equals(s.battleUiMode)
                    || s.battleMenuScroll != 1
                    || s.battleShopConfirmItemId != -1) {
                throw new IllegalStateException("P11 mouse wheel down should move scrollbar without confirm"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " scroll=" + s.battleMenuScroll
                        + " confirmItem=" + s.battleShopConfirmItemId
                        + " trace=" + tailTrace(s, 18));
            }
            s.mouseWheel(-1);
            s.tick();
            if (!"P11".equals(s.battleStateName)
                    || !"shopbuy".equals(s.battleUiMode)
                    || s.battleMenuScroll != 0
                    || s.battleShopConfirmItemId != -1) {
                throw new IllegalStateException("P11 mouse wheel up should move scrollbar back without confirm"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " scroll=" + s.battleMenuScroll
                        + " confirmItem=" + s.battleShopConfirmItemId
                        + " trace=" + tailTrace(s, 18));
            }
            s.mouseWheel(5);
            for (int i = 0; i < 5; i++) {
                s.tick();
            }
            if (!"P11".equals(s.battleStateName)
                    || !"shopbuy".equals(s.battleUiMode)
                    || s.battleMenuScroll < 4
                    || s.battleShopConfirmItemId != -1) {
                throw new IllegalStateException("P11 mouse wheel multi-step should move scrollbar without confirm"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " scroll=" + s.battleMenuScroll
                        + " confirmItem=" + s.battleShopConfirmItemId
                        + " trace=" + tailTrace(s, 18));
            }
            s.sourceStateTrace.add("SMOKE verified mouse wheel scrolls P11 shopbuy without confirm");
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shop_msgyn_open".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 5);
            openShopConfirmForItem(s, 5);
            if (!"shopconfirm".equals(s.battleUiMode)
                    || s.battleShopConfirmItemId != 5
                    || s.battleShopConfirmQuantity != 1
                    || s.battleShopConfirmTotal != 0
                    || s.battleShopConfirmCurrency != 0) {
                throw new IllegalStateException("Expected P11 msgyn.ui qty=1 PC-free total=0, ui="
                        + s.battleUiMode
                        + " item=" + s.battleShopConfirmItemId
                        + " qty=" + s.battleShopConfirmQuantity
                        + " total=" + s.battleShopConfirmTotal
                        + " currency=" + s.battleShopConfirmCurrency
                        + " trace=" + tailTrace(s, 18));
            }
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shop_buy_currency2_item0_free".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 0);
            s.sourceMoney = 12;
            s.sourceBadges = 3;
            openBattleShopP11(s);
            int item0Index = battleMenuIndexOf(s, 0);
            if (item0Index < 0 || !"0".equals(s.battleMenuValues[item0Index])) {
                throw new IllegalStateException("Expected currency type 2 item0 to display price 0"
                        + " index=" + item0Index
                        + " values=" + java.util.Arrays.toString(s.battleMenuValues)
                        + " trace=" + tailTrace(s, 18));
            }
            selectBattleShopItem(s, 0);
            press0UntilBattleUiMode(s, "shopconfirm", 80);
            if (s.battleShopConfirmItemId != 0
                    || s.battleShopConfirmQuantity != 1
                    || s.battleShopConfirmTotal != 0
                    || s.battleShopConfirmCurrency != 2) {
                throw new IllegalStateException("Expected currency type 2 item0 msgyn total 0, item="
                        + s.battleShopConfirmItemId
                        + " qty=" + s.battleShopConfirmQuantity
                        + " total=" + s.battleShopConfirmTotal
                        + " currency=" + s.battleShopConfirmCurrency
                        + " trace=" + tailTrace(s, 18));
            }
            confirmShopMsgyn(s);
            if (!"WARN".equals(s.battleStateName)
                    || VqsvSourceOps.sourceItemCount(s, 0) != 1
                    || s.sourceMoney != 12
                    || s.sourceBadges != 3
                    || !traceContains(s, "shop buy msgyn.ui item=0 qty=1 total=0 currency=2")) {
                throw new IllegalStateException("Expected currency type 2 item0 free purchase, count="
                        + VqsvSourceOps.sourceItemCount(s, 0)
                        + " money=" + s.sourceMoney
                        + " badges=" + s.sourceBadges
                        + " trace=" + tailTrace(s, 20));
            }
            revealCheckpointText(s, 90);
            return;
        }
        if ("battle_p11_shop_msgyn_quantity_right".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 5);
            openShopConfirmForItem(s, 5);
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            if (!"shopconfirm".equals(s.battleUiMode)
                    || s.battleShopConfirmQuantity != 2
                    || s.battleShopConfirmTotal != 0
                    || !traceContains(s, "msgyn.ui key=32832 qty=2 total=0")) {
                throw new IllegalStateException("Expected P11 msgyn.ui right qty=2 PC-free total=0, qty="
                        + s.battleShopConfirmQuantity + " total=" + s.battleShopConfirmTotal
                        + " trace=" + tailTrace(s, 18));
            }
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shop_buy_qty2_money".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            s.sourceMoney = 1000;
            openShopConfirmForItem(s, 6);
            s.keyRight = true;
            s.tick();
            s.keyRight = false;
            confirmShopMsgyn(s);
            if (!"WARN".equals(s.battleStateName)
                    || VqsvSourceOps.sourceItemCount(s, 6) != 2
                    || s.sourceMoney != 1000
                    || !traceContains(s, "shop buy msgyn.ui item=6 qty=2 total=0 currency=0")) {
                throw new IllegalStateException("Expected P11 qty2 money buy, count="
                        + VqsvSourceOps.sourceItemCount(s, 6)
                        + " money=" + s.sourceMoney
                        + " state=" + s.battleStateName
                        + " trace=" + tailTrace(s, 20));
            }
            revealCheckpointText(s, 90);
            return;
        }
        if ("battle_p11_shop_success_click_no_rebuy".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 6);
            s.sourceMoney = 1000;
            openShopConfirmForItem(s, 6);
            confirmShopMsgyn(s);
            if (!"WARN".equals(s.battleStateName)
                    || VqsvSourceOps.sourceItemCount(s, 6) != 1) {
                throw new IllegalStateException("Expected P11 success warning before click-through guard"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 6)
                        + " trace=" + tailTrace(s, 20));
            }
            for (int i = 0; i < 12 && "WARN".equals(s.battleStateName); i++) {
                s.tick();
            }
            s.battleClickX = 122;
            s.battleClickY = 162;
            s.key0 = true;
            for (int i = 0; i < 24; i++) {
                s.tick();
            }
            if (!"P11".equals(s.battleStateName)
                    || !"shopbuy".equals(s.battleUiMode)
                    || s.battleShopConfirmItemId != -1
                    || VqsvSourceOps.sourceItemCount(s, 6) != 1) {
                throw new IllegalStateException("P11 success dialog click leaked into shop row"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " confirmItem=" + s.battleShopConfirmItemId
                        + " count=" + VqsvSourceOps.sourceItemCount(s, 6)
                        + " click=" + s.battleClickX + "," + s.battleClickY
                        + " trace=" + tailTrace(s, 24));
            }
            s.sourceStateTrace.add("SMOKE verified P11 msgwarm click is consumed before returning shopbuy.ui");
            revealCheckpointText(s, 60);
            return;
        }
        if ("battle_p11_shop_buy_badge_item3".equals(checkpoint)) {
            setupElderShopBattleForSmoke(s, 3);
            s.sourceBadges = 5;
            openShopConfirmForItem(s, 3);
            confirmShopMsgyn(s);
            if (!"WARN".equals(s.battleStateName)
                    || VqsvSourceOps.sourceItemCount(s, 3) != 1
                    || s.sourceBadges != 5
                    || !traceContains(s, "shop buy msgyn.ui item=3 qty=1 total=0 currency=1")) {
                throw new IllegalStateException("Expected P11 badge buy item3, count="
                        + VqsvSourceOps.sourceItemCount(s, 3)
                        + " badges=" + s.sourceBadges
                        + " state=" + s.battleStateName
                        + " trace=" + tailTrace(s, 20));
            }
            revealCheckpointText(s, 90);
            return;
        }
        if (checkpoint.startsWith("battle_p11_shop_buy_item")
                && checkpoint.endsWith("_then_p16_use")) {
            int itemId = parseP11ShopBuyUseItemId(checkpoint);
            runP11PurchaseThenUseSmoke(s, itemId);
            return;
        }
        throw new IllegalArgumentException("Unknown P11 shop checkpoint: " + checkpoint);
    }

    private static SourceBattleRuntime setupElderShopBattleForSmoke(VqsvIntroDemo.Scene s, int itemId) {
        BattleItemRow item = VqsvBattleTables.instance().item(itemId);
        int behavior = item == null ? -1 : item.behavior;
        s.eventIndex = s.events.size();
        s.sourceMoney = 9999;

        SourcePetState active = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
        if (behavior == 2 || behavior == 3) {
            active.skillCooldowns[0] = 0;
        }
        active.sourcePayload = active.toSourcePayload();
        if (behavior == 1 || behavior == 3) {
            active.sourcePayload[6] = 20;
        }
        s.sourcePets.add(active);

        if (behavior == 4) {
            SourcePetState reserve = new SourcePetState(1, 92, 5, 3, 2, 10, 45);
            reserve.skillCooldowns[0] = 0;
            reserve.sourcePayload = reserve.toSourcePayload();
            reserve.sourcePayload[6] = 0;
            s.sourcePets.add(reserve);
        }

        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        if (behavior == 5) {
            runtime.debugPlayerDebuffForItemSmoke(s, 5, 8, 1);
        }
        return runtime;
    }

    private static void runP11PurchaseThenUseSmoke(VqsvIntroDemo.Scene s, int itemId) {
        BattleItemRow item = VqsvBattleTables.instance().item(itemId);
        if (item == null || itemId < 5 || itemId > 12) {
            throw new IllegalArgumentException("P11 buy/use smoke expects source item 5..12, got " + itemId);
        }
        setupElderShopBattleForSmoke(s, itemId);
        buyBattleShopItemViaP11(s, itemId);
        int targetIndex = item.behavior == 4 ? 1 : 0;
        driveItemUseToTarget(s, targetIndex);
        if (!traceContains(s, "battle P11 shop buy msgyn.ui item=" + itemId)
                || !traceContains(s, "P16 game.b.w item=" + itemId)) {
            throw new IllegalStateException("Expected P11 purchase followed by P16 use for item=" + itemId
                    + " trace=" + tailTrace(s, 18));
        }
        assertPurchasedItemUseEffect(s, itemId, item.behavior);
        revealCheckpointText(s, 80);
    }

    private static void buyBattleShopItemViaP11(VqsvIntroDemo.Scene s, int itemId) {
        openShopConfirmForItem(s, itemId);
        confirmShopMsgyn(s);
        if (!traceContains(s, "battle P11 shop buy msgyn.ui item=" + itemId)
                || VqsvSourceOps.sourceItemCount(s, itemId) <= 0) {
            throw new IllegalStateException("Expected source P11 purchase to add item=" + itemId
                    + " count=" + VqsvSourceOps.sourceItemCount(s, itemId)
                    + " state=" + s.battleStateName
                    + " warning=" + s.battleWarningTitle
                    + " trace=" + tailTrace(s, 18));
        }
        press0UntilAnyBattleState(s, 80, "P11");
        s.battleClickX = 170;
        s.battleClickY = 242;
        tickUntilBattleState(s, "P20", 80);
    }

    private static void openShopConfirmForItem(VqsvIntroDemo.Scene s, int itemId) {
        openBattleShopP11(s);
        selectBattleShopItem(s, itemId);
        press0UntilBattleUiMode(s, "shopconfirm", 80);
    }

    private static void selectBattleShopItem(VqsvIntroDemo.Scene s, int itemId) {
        int index = battleMenuIndexOf(s, itemId);
        if (index < 0) {
            throw new IllegalStateException("P11 shop missing source item=" + itemId
                    + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                    + " trace=" + tailTrace(s, 12));
        }
        s.battleMenuIndex = index;
        s.battleMenuScroll = Math.max(0, index - 4);
    }

    private static void confirmShopMsgyn(VqsvIntroDemo.Scene s) {
        press0UntilAnyBattleState(s, 80, "WARN");
    }

    private static void tickUntilBattleUiMode(VqsvIntroDemo.Scene s, String mode, int maxTicks) {
        int guard = 0;
        while (!mode.equals(s.battleUiMode) && guard++ < maxTicks) {
            s.tick();
        }
        if (!mode.equals(s.battleUiMode)) {
            throw new IllegalStateException("Battle UI mode " + mode
                    + " not reached in " + maxTicks
                    + " ticks, state=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " trace=" + tailTrace(s, 18));
        }
    }

    private static void press0UntilBattleUiMode(VqsvIntroDemo.Scene s, String mode, int maxTicks) {
        int guard = 0;
        while (!mode.equals(s.battleUiMode) && guard++ < maxTicks) {
            s.press0();
            s.tick();
        }
        if (!mode.equals(s.battleUiMode)) {
            throw new IllegalStateException("Battle UI mode " + mode
                    + " not reached in " + maxTicks
                    + " pressed ticks, state=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " trace=" + tailTrace(s, 18));
        }
    }

    private static void openBattleShopP11(VqsvIntroDemo.Scene s) {
        if (!"P20".equals(s.battleStateName)) {
            tickUntilBattleState(s, "P20", 120);
        }
        s.battleClickX = 176;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P11", 80);
    }

    private static void assertPurchasedItemUseEffect(VqsvIntroDemo.Scene s, int itemId, int behavior) {
        switch (behavior) {
            case 1:
                if (s.battlePlayerHp <= 20) {
                    throw new IllegalStateException("Expected purchased HP item=" + itemId
                            + " to heal active pet, hp=" + s.battlePlayerHp
                            + " trace=" + tailTrace(s, 14));
                }
                break;
            case 2:
                if (s.sourcePets.get(0).skillCooldowns[0] <= 0) {
                    throw new IllegalStateException("Expected purchased PP item=" + itemId
                            + " to restore skill PP, pp=" + s.sourcePets.get(0).skillCooldowns[0]
                            + " trace=" + tailTrace(s, 14));
                }
                break;
            case 3:
                if (s.battlePlayerHp <= 20 || s.sourcePets.get(0).skillCooldowns[0] <= 0) {
                    throw new IllegalStateException("Expected purchased HP+PP item=" + itemId
                            + " to restore both, hp=" + s.battlePlayerHp
                            + " pp=" + s.sourcePets.get(0).skillCooldowns[0]
                            + " trace=" + tailTrace(s, 14));
                }
                break;
            case 4: {
                if (s.sourcePets.size() < 2) {
                    throw new IllegalStateException("Expected reserve pet for purchased revive item=" + itemId);
                }
                SourceBattleUnit reserve = SourceBattleUnit.playerFromSourcePets(s.sourcePets.subList(1, 2));
                if (reserve.hp <= 0 || s.sourcePets.get(1).skillCooldowns[0] <= 0) {
                    throw new IllegalStateException("Expected purchased revive item=" + itemId
                            + " to revive reserve, hp=" + reserve.hp
                            + " pp=" + s.sourcePets.get(1).skillCooldowns[0]
                            + " trace=" + tailTrace(s, 14));
                }
                break;
            }
            case 5:
                if (!traceContains(s, "debuffs=1->0")) {
                    throw new IllegalStateException("Expected purchased debuff clear item=" + itemId
                            + " to clear debuff, trace=" + tailTrace(s, 14));
                }
                break;
            default:
                throw new IllegalStateException("Unsupported purchased item behavior=" + behavior
                        + " item=" + itemId);
        }
    }

    private static boolean battleMenuContains(VqsvIntroDemo.Scene s, int itemId) {
        return battleMenuIndexOf(s, itemId) >= 0;
    }

    private static int battleMenuIndexOf(VqsvIntroDemo.Scene s, int itemId) {
        for (int i = 0; i < s.battleMenuIds.length; i++) {
            if (s.battleMenuIds[i] == itemId) {
                return i;
            }
        }
        return -1;
    }

    private static int parseP11ShopBuyUseItemId(String checkpoint) {
        String prefix = "battle_p11_shop_buy_item";
        String suffix = "_then_p16_use";
        try {
            return Integer.parseInt(checkpoint.substring(prefix.length(),
                    checkpoint.length() - suffix.length()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid P11 buy/use checkpoint: " + checkpoint, ex);
        }
    }

    private static SourceBattleRuntime setupElderItemBattleWithDeadReserve(VqsvIntroDemo.Scene s, int itemId,
                                                                           int itemCount, int reserveFirstPp) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        SourcePetState reserve = new SourcePetState(1, 92, 5, 3, 2, 10, 45);
        reserve.skillCooldowns[0] = reserveFirstPp;
        reserve.sourcePayload = reserve.toSourcePayload();
        reserve.sourcePayload[6] = 0;
        s.sourcePets.add(reserve);
        s.sourceBagItems.put(itemId, new BagItem(itemId, itemCount, 1, false));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        return runtime;
    }

    private static SourceBattleRuntime setupElderItemBattleWithReserve(VqsvIntroDemo.Scene s, int itemId,
                                                                       int itemCount) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
        s.sourceBagItems.put(itemId, new BagItem(itemId, itemCount, 1, false));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        return runtime;
    }

    private static void driveItemUse(VqsvIntroDemo.Scene s) {
        driveItemUseToTarget(s, 0);
    }

    private static void driveItemCommandToP16(VqsvIntroDemo.Scene s) {
        s.battleClickX = 98;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P4", 80);
        press0UntilAnyBattleState(s, 40, "P16", "WARN");
        if (!"P16".equals(s.battleStateName)) {
            throw new IllegalStateException("Expected item command to enter P16, state="
                    + s.battleStateName + " warning=" + s.battleWarningTitle
                    + " trace=" + tailTrace(s, 12));
        }
    }

    private static void driveItemUseToTarget(VqsvIntroDemo.Scene s, int targetIndex) {
        s.battleClickX = 98;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P4", 80);
        press0UntilAnyBattleState(s, 40, "P16", "WARN");
        if ("WARN".equals(s.battleStateName)) {
            return;
        }
        s.battleMenuIndex = Math.max(0, Math.min(targetIndex, Math.max(0, s.battleMenuIds.length - 1)));
        for (int i = 0; i < targetIndex; i++) {
            s.setMoveKey(KeyEvent.VK_DOWN, true);
            s.tick();
            s.setMoveKey(KeyEvent.VK_DOWN, false);
            s.tick();
        }
        press0UntilAnyBattleState(s, 80, "P1", "WARN", "P4");
    }

    private static void setupElderPetSwitchBattle(VqsvIntroDemo.Scene s, boolean reserveDead) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        SourcePetState reserve = new SourcePetState(1, 92, 5, 3, 2, 10, 45);
        if (reserveDead) {
            reserve.sourcePayload = reserve.toSourcePayload();
            reserve.sourcePayload[6] = 0;
        }
        s.sourcePets.add(reserve);
        s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        tickUntilBattleState(s, "P20", 120);
    }

    private static void runElderSwitchedBunnyKoForcedP5NoExpSmoke(VqsvIntroDemo.Scene s) {
        setupElderPetSwitchBattle(s, false);
        SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
        SourcePetState dienMieu = s.sourcePets.get(0);
        SourcePetState bunny = s.sourcePets.get(1);
        int dienStartExp = sourcePetExp(dienMieu);
        int bunnyStartExp = sourcePetExp(bunny);
        switchToBunnyForElderKoSmoke(s);
        runtime.debugSetPlayerHpForSmoke(s, 1);
        runtime.debugSetNextP7HitRollForSmoke(99);
        tickUntilBattleState(s, "P1", 180);
        tickUntilBattleState(s, "P2", 180);
        tickUntilBattleState(s, "P7", 180);
        tickUntilAnyBattleState(s, 420, "P5", "P8", "P9");
        if (!"P5".equals(s.battleStateName)
                || !"petstate".equals(s.battleUiMode)
                || !traceContains(s, "generic KO side0 -> P5 context=P7 game.d.q")
                || traceContains(s, "battle P8 game.d.X commit B->S")
                || "levelup".equals(s.battleUiMode)) {
            throw new IllegalStateException("Expected Elder KO on switched Bunny to force P5 without EXP, state="
                    + s.battleStateName + " ui=" + s.battleUiMode
                    + " trace=" + tailTrace(s, 24));
        }
        assertSourcePetExp(dienMieu, dienStartExp, "Elder switched Bunny KO Dien Mieu no-win");
        assertSourcePetExp(bunny, bunnyStartExp, "Elder switched Bunny KO Bunny no-win");
        assertPayloadHp(bunny, 0, "Elder switched Bunny KO active Bunny");
    }

    private static void runElderAllPlayerPetsKoP9NoExpSmoke(VqsvIntroDemo.Scene s) {
        setupElderPetSwitchBattle(s, false);
        SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
        SourcePetState dienMieu = s.sourcePets.get(0);
        SourcePetState bunny = s.sourcePets.get(1);
        int dienStartExp = sourcePetExp(dienMieu);
        int bunnyStartExp = sourcePetExp(bunny);
        switchToBunnyForElderKoSmoke(s);
        if ("P15".equals(s.battleStateName)) {
            tickUntilBattleState(s, "P1", 180);
        }
        s.sourcePets.get(1).sourcePayload = s.sourcePets.get(1).toSourcePayload();
        s.sourcePets.get(1).sourcePayload[6] = 0;
        runtime.debugSetPlayerHpForSmoke(s, 1);
        runtime.debugSetNextP7HitRollForSmoke(99);
        tickUntilBattleState(s, "P2", 180);
        tickUntilBattleState(s, "P7", 180);
        tickUntilAnyBattleState(s, 420, "P5", "P8", "P9");
        if (!"P9".equals(s.battleStateName)
                || !traceContains(s, "generic KO side0 -> P9 context=P7 game.d.q")
                || "levelup".equals(s.battleUiMode)) {
            throw new IllegalStateException("Expected all player pets KO to enter P9 without EXP, state="
                    + s.battleStateName + " ui=" + s.battleUiMode
                    + " trace=" + tailTrace(s, 24));
        }
        tickUntilBattleState(s, "EXIT", 120);
        if ("P8".equals(s.battleStateName)
                || traceContains(s, "battle P8 game.d.X commit B->S")
                || traceContains(s, "battle P8 game.d.h direct EXP")) {
            throw new IllegalStateException("Expected all-dead P9 to avoid P8/EXP, state="
                    + s.battleStateName + " trace=" + tailTrace(s, 24));
        }
        assertSourcePetExp(dienMieu, dienStartExp, "Elder all-dead Dien Mieu no EXP");
        assertSourcePetExp(bunny, bunnyStartExp, "Elder all-dead Bunny no EXP");
        if (!s.sourceBattleLoseReviveArmed || s.sourceBattleLoseWorldMode != 1) {
            throw new IllegalStateException("Expected P9 to arm source M.i and set M.l=1"
                    + " armed=" + s.sourceBattleLoseReviveArmed
                    + " mode=" + s.sourceBattleLoseWorldMode
                    + " trace=" + tailTrace(s, 24));
        }
        if (!traceContains(s, "post-loss world reset")) {
            throw new IllegalStateException("Expected P9 post-loss world reset trace, trace="
                    + tailTrace(s, 24));
        }
        assertPayloadHp(bunny, 1, "Elder all-dead active Bunny source P9 reset");
        assertPayloadHp(dienMieu, 1, "Elder all-dead reserve Dien Mieu source P9 reset");
    }

    private static void runP24RevivePayFullRestoreSmoke(VqsvIntroDemo.Scene s) {
        driveBattleToP24PromptForSmoke(s);
        SourcePetState dienMieu = s.sourcePets.get(0);
        SourcePetState bunny = s.sourcePets.get(1);
        for (int i = 0; i < 8; i++) {
            s.tick();
        }
        s.press0();
        tickUntilBattleState(s, "P0", 120);
        if (s.sourceMoney != 0
                || payloadHp(dienMieu) != sourceMaxHp(dienMieu)
                || payloadHp(bunny) != sourceMaxHp(bunny)
                || !traceContains(s, "battle P24 revive all")) {
            throw new IllegalStateException("Expected P24 paid revive full restore"
                    + " money=" + s.sourceMoney
                    + " hp0=" + payloadHp(dienMieu) + "/" + sourceMaxHp(dienMieu)
                    + " hp1=" + payloadHp(bunny) + "/" + sourceMaxHp(bunny)
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static void driveBattleToP24PromptForSmoke(VqsvIntroDemo.Scene s) {
        setupElderPetSwitchBattle(s, false);
        SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
        s.sourceBattleLoseReviveArmed = true;
        s.sourceBattleLoseWorldMode = 1;
        s.sourceMoney = 10000;
        switchToBunnyForElderKoSmoke(s);
        if ("P15".equals(s.battleStateName)) {
            tickUntilBattleState(s, "P1", 180);
        }
        s.sourcePets.get(1).sourcePayload = s.sourcePets.get(1).toSourcePayload();
        s.sourcePets.get(1).sourcePayload[6] = 0;
        runtime.debugSetPlayerHpForSmoke(s, 1);
        runtime.debugSetNextP7HitRollForSmoke(99);
        tickUntilBattleState(s, "P2", 180);
        tickUntilBattleState(s, "P7", 180);
        tickUntilBattleState(s, "P24", 520);
        if (!"smsinfo".equals(s.battleUiMode)
                || !VqsvText.Battle.LOSE_REVIVE_PROMPT.equals(s.battleWarningTitle)
                || !traceContains(s, "game.h.aE")) {
            throw new IllegalStateException("Expected P24 revive prompt, state="
                    + s.battleStateName + " ui=" + s.battleUiMode
                    + " warning=" + s.battleWarningTitle
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static void switchToBunnyForElderKoSmoke(VqsvIntroDemo.Scene s) {
        drivePetCommandToP5(s);
        s.battleMenuIndex = 1;
        press0UntilAnyBattleState(s, 100, "P15", "P1", "WARN");
        if ("WARN".equals(s.battleStateName)) {
            throw new IllegalStateException("Expected Bunny switch before KO, warning="
                    + s.battleWarningTitle + " trace=" + tailTrace(s, 16));
        }
    }

    private static void drivePetCommandToP5(VqsvIntroDemo.Scene s) {
        s.battleClickX = 137;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P5", 80);
    }

    private static void press0UntilAnyBattleState(VqsvIntroDemo.Scene s, int maxTicks, String... stateNames) {
        int guard = 0;
        while (!isBattleState(s, stateNames) && guard++ < maxTicks) {
            s.press0();
            s.tick();
        }
        if (!isBattleState(s, stateNames)) {
            throw new IllegalStateException("Battle states " + java.util.Arrays.toString(stateNames)
                    + " not reached in " + maxTicks + " ticks, current=" + s.battleStateName
                    + " trace=" + tailTrace(s, 12));
        }
    }

    private static void enterElderP7FromFight(VqsvIntroDemo.Scene s) {
        enterElderP7WithSkillIndex(s, 0);
    }

    private static void enterBunnyP7FromFight(VqsvIntroDemo.Scene s) {
        s.eventIndex = s.events.size();
        seedInitialDienMieu(s, "smoke Bunny P7 frame compare");
        seedRoom0Group0BunnyRewards(s);
        s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 20;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P3", 80);
        for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
    }

    private static void enterElderP7WithSkillIndex(VqsvIntroDemo.Scene s, int skillIndex) {
        enterElderP7WithSkills(s, new int[]{10, 45}, skillIndex);
    }

    private static void enterElderP7WithSkills(VqsvIntroDemo.Scene s, int[] skills, int skillIndex) {
        s.eventIndex = s.events.size();
        int firstSkill = skills.length > 0 ? skills[0] : 10;
        int secondSkill = skills.length > 1 ? skills[1] : -1;
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, firstSkill, secondSkill));
        s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 20;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P3", 80);
        for (int i = 0; i < 10; i++) {
            s.tick();
        }
        for (int i = 0; i < skillIndex; i++) {
            s.setMoveKey(KeyEvent.VK_DOWN, true);
            s.tick();
            s.setMoveKey(KeyEvent.VK_DOWN, false);
            s.tick();
        }
        for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
    }

    private static void forceNextP7Miss(VqsvIntroDemo.Scene s) {
        SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
        runtime.debugSetPlayerSpeedForSmoke(s, 0);
        runtime.debugSetEnemySpeedForSmoke(s, 200);
        runtime.debugSetNextP7HitRollForSmoke(0);
    }

    private static int parsePhase9BSkillId(String checkpoint) {
        String prefix = "battle_phase9b_direct_skill_";
        return parseSkillIdSuffix(checkpoint, prefix);
    }

    private static int parseSkillIdSuffix(String checkpoint, String prefix) {
        try {
            return Integer.parseInt(checkpoint.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid skill checkpoint: " + checkpoint, ex);
        }
    }

    private static boolean isPhase9BDirectSimpleSmokeSkill(int skillId) {
        switch (skillId) {
            case 0:
            case 6:
            case 10:
            case 16:
            case 20:
            case 26:
            case 30:
            case 36:
            case 40:
            case 46:
            case 50:
            case 56:
            case 60:
            case 66:
                return true;
            default:
                return false;
        }
    }

    private static boolean isPhase9EDebuff1Skill(int skillId) {
        return skillId == 2 || skillId == 8 || skillId == 22 || skillId == 28;
    }

    private static boolean isPhase9FDebuff2Skill(int skillId) {
        return skillId == 12 || skillId == 18;
    }

    private static boolean isPhase9GDebuff3Skill(int skillId) {
        return skillId == 13 || skillId == 19;
    }

    private static boolean isPhase9HDebuff4Skill(int skillId) {
        return skillId == 31 || skillId == 37;
    }

    private static boolean isPhase9IDebuff5Skill(int skillId) {
        return skillId == 32 || skillId == 38 || skillId == 61;
    }

    private static boolean isPhase9JDebuff6Skill(int skillId) {
        return skillId == 33 || skillId == 39;
    }

    private static boolean isPhase9KDebuff7Skill(int skillId) {
        return skillId == 51 || skillId == 57;
    }

    private static boolean isPhase9LDebuff10Skill(int skillId) {
        return skillId == 41 || skillId == 47;
    }

    private static boolean isPhase9MZeroPowerDebuffSkill(int skillId) {
        return skillId == 54 || skillId == 55;
    }

    private static boolean isPhase9NClearBuffSkill(int skillId) {
        return skillId == 43 || skillId == 49;
    }

    private static int phase9YBuffId(int skillId) {
        switch (skillId) {
            case 4:
                return 0;
            case 5:
                return 1;
            case 14:
                return 2;
            case 44:
                return 8;
            default:
                return -1;
        }
    }

    private static boolean isPhase9OHpScalingSkill(int skillId) {
        return skillId == 53 || skillId == 59;
    }

    private static boolean isPhase9RRawSelfBuffSkill(int skillId) {
        return skillId == 21 || skillId == 27 || skillId == 42 || skillId == 48 || skillId == 62;
    }

    private static int phase9RSelfBuffId(int skillId) {
        switch (skillId) {
            case 21:
            case 27:
                return 4;
            case 42:
            case 48:
                return 7;
            case 62:
                return 10;
            default:
                throw new IllegalArgumentException("Not a Phase9R raw self-buff skill: " + skillId);
        }
    }

    private static int phase9OHpScalingDamage(VqsvIntroDemo.Scene s, int skillId,
                                              int hpPercent, int attack, int defense) {
        enterElderP7WithSkills(s, new int[]{skillId, 45}, 0);
        SourceBattleRuntime runtime = (SourceBattleRuntime) s.current;
        runtime.debugSetPlayerAttackForSmoke(s, attack);
        runtime.debugSetEnemyDefenseForSmoke(s, defense);
        int hp = Math.max(1, s.battlePlayerMaxHp * hpPercent / 100);
        runtime.debugSetPlayerHpForSmoke(s, hp);
        runtime.debugSetNextP7HitRollForSmoke(99);
        BattleUnit.setDamageRandomSeedForChecks(0L);
        tickUntilBattleP7Phase(s, 2, 180);
        return latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
    }

    private static void enterSpecies0LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 0);
    }

    private static void enterSpecies75LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 75);
    }

    private static void enterSpecies87LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 87);
    }

    private static void enterSpecies91LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 91);
    }

    private static void enterSpecies10LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 10);
    }

    private static void enterSpecies92LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 92);
    }

    private static void enterSpecies97LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 97);
    }

    private static void enterSpecies98LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 98);
    }

    private static void enterSpecies62LP7(VqsvIntroDemo.Scene s) {
        enterSpeciesLP7(s, 62);
    }

    private static void enterSpeciesLP7(VqsvIntroDemo.Scene s, int speciesId) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, speciesId, 30, 3, 2, 10, -1));
        s.current = new SourceBattleRuntime(52, new int[]{34, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        tickUntilBattleState(s, "P20", 160);
        s.battleClickX = 20;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P3", 80);
        for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
    }

    private static void tickBattleAutoUntilDone(VqsvIntroDemo.Scene s, int maxTicks) {
        for (int i = 0; i < maxTicks; i++) {
            if (s.current == null) {
                return;
            }
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            if ("P20".equals(s.battleStateName)) {
                if (s.battleCommandIndex == 1) {
                    s.battleClickX = 56;
                } else {
                    s.battleClickX = 20;
                }
                s.battleClickY = 300;
            } else if ("P3".equals(s.battleStateName)
                    || "P6".equals(s.battleStateName)
                    || "P21".equals(s.battleStateName)
                    || "WARN".equals(s.battleStateName)) {
                s.press0();
            }
            s.tick();
        }
        throw new IllegalStateException("Checkpoint current did not finish in " + maxTicks
                + " ticks state=" + s.battleStateName
                + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                + " log=" + s.battleLog
                + " command=" + s.battleCommandIndex);
    }

    private static void driveBunnyTutorialUntilFirstCatchP17(VqsvIntroDemo.Scene s, int maxTicks) {
        for (int i = 0; i < maxTicks; i++) {
            if ("P17".equals(s.battleStateName) && s.battleCatchItemId == 1) {
                return;
            }
            driveBunnyTutorialOneTick(s);
        }
        throw new IllegalStateException("Bunny first P17 not reached state=" + s.battleStateName
                + " item=" + s.battleCatchItemId
                + " caught=" + s.battleCatchCaught
                + " trace=" + tailTrace(s, 16));
    }

    private static void driveBunnyTutorialUntilRetryP21(VqsvIntroDemo.Scene s, int maxTicks) {
        boolean sawFirstP17 = false;
        for (int i = 0; i < maxTicks; i++) {
            if ("P17".equals(s.battleStateName) && s.battleCatchItemId == 1) {
                sawFirstP17 = true;
                if (s.battleCatchCaught) {
                    throw new IllegalStateException("Bunny first P17 unexpectedly caught trace="
                            + tailTrace(s, 16));
                }
            }
            if (sawFirstP17 && "P21".equals(s.battleStateName)
                    && s.battleMenuIds.length > 0
                    && s.battleMenuIds[s.battleMenuIndex] == 0) {
                return;
            }
            driveBunnyTutorialOneTick(s);
        }
        throw new IllegalStateException("Bunny retry P21 not reached state=" + s.battleStateName
                + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                + " index=" + s.battleMenuIndex
                + " trace=" + tailTrace(s, 16));
    }

    private static void driveBunnyTutorialUntilRetryPrompt(VqsvIntroDemo.Scene s, int maxTicks) {
        boolean sawFirstP17 = false;
        for (int i = 0; i < maxTicks; i++) {
            if ("P17".equals(s.battleStateName) && s.battleCatchItemId == 1) {
                sawFirstP17 = true;
            }
            if (sawFirstP17 && s.text != null && s.text.sourceUiKind == TextBox.SOURCE_TASKTIP) {
                return;
            }
            driveBunnyTutorialOneTick(s);
        }
        throw new IllegalStateException("Bunny retry taskTip not reached state=" + s.battleStateName
                + " text=" + (s.text == null ? "null" : s.text.text)
                + " trace=" + tailTrace(s, 16));
    }

    private static void driveBunnyTutorialUntilWeakPrompt(VqsvIntroDemo.Scene s, int maxTicks) {
        for (int i = 0; i < maxTicks; i++) {
            if (s.text != null && s.text.sourceUiKind == TextBox.SOURCE_TASKTIP
                    && s.text.text.contains("phong \u1ea5n c\u1ea7u")) {
                return;
            }
            driveBunnyTutorialOneTickNoTextConfirm(s);
        }
        throw new IllegalStateException("Bunny weak taskTip not reached state=" + s.battleStateName
                + " text=" + (s.text == null ? "null" : s.text.text)
                + " U=" + s.battleTutorialU + " V=" + s.battleTutorialV
                + " trace=" + tailTrace(s, 16));
    }

    private static void driveBunnyTutorialUntilEnemyCounterAfterFirstFail(VqsvIntroDemo.Scene s, int maxTicks) {
        boolean sawFirstP17 = false;
        boolean sawFailedCatch = false;
        for (int i = 0; i < maxTicks; i++) {
            if ("P17".equals(s.battleStateName) && s.battleCatchItemId == 1) {
                sawFirstP17 = true;
                if (!s.battleCatchCaught && s.battleCatchPhase == 4) {
                    sawFailedCatch = true;
                }
            }
            if (sawFirstP17 && sawFailedCatch
                    && "P7".equals(s.battleStateName)
                    && !s.battleP7AttackerPlayerSide
                    && s.battleP7TargetPlayerSide
                    && s.battleP7DamageVisible) {
                return;
            }
            driveBunnyTutorialOneTick(s);
        }
        throw new IllegalStateException("Bunny enemy counterattack after first fail not reached state="
                + s.battleStateName
                + " phase=" + s.battleCatchPhase
                + " attackerPlayer=" + s.battleP7AttackerPlayerSide
                + " targetPlayer=" + s.battleP7TargetPlayerSide
                + " damageVisible=" + s.battleP7DamageVisible
                + " text=" + (s.text == null ? "null" : s.text.text)
                + " trace=" + tailTrace(s, 20));
    }

    private static void driveBunnyTutorialOneTick(VqsvIntroDemo.Scene s) {
        if (s.text != null && s.text.readyForKey) {
            s.press0();
        }
        driveBunnyTutorialOneTickNoTextConfirm(s);
    }

    private static void driveBunnyTutorialOneTickNoTextConfirm(VqsvIntroDemo.Scene s) {
        if ("P20".equals(s.battleStateName)) {
            s.battleClickX = s.battleCommandIndex == 1 ? 56 : 20;
            s.battleClickY = 300;
        } else if ("P3".equals(s.battleStateName)
                || "P6".equals(s.battleStateName)
                || "P21".equals(s.battleStateName)
                || "WARN".equals(s.battleStateName)) {
            s.press0();
        }
        s.tick();
    }

    private static void seedRoom0Group0BunnyRewards(VqsvIntroDemo.Scene s) {
        s.op17Item(0, 0, 1);
        s.text = null;
        s.op17Item(0, 1, 2);
        s.text = null;
        s.op17Item(0, 4, 5);
        s.text = null;
        s.sourceStateTrace.add("SMOKE source route room0 group0 rewards before Bunny P21 ballCounts=["
                + VqsvSourceOps.sourceItemCount(s, 0) + ","
                + VqsvSourceOps.sourceItemCount(s, 1) + "]");
    }

    private static void assertBattleMenuIds(VqsvIntroDemo.Scene s, String label, int[] expected) {
        if (!java.util.Arrays.equals(s.battleMenuIds, expected)) {
            throw new IllegalStateException(label + " mismatch expected="
                    + java.util.Arrays.toString(expected)
                    + " actual=" + java.util.Arrays.toString(s.battleMenuIds)
                    + " names=" + java.util.Arrays.toString(s.battleMenuNames)
                    + " values=" + java.util.Arrays.toString(s.battleMenuValues)
                    + " counts=[" + VqsvSourceOps.sourceItemCount(s, 0)
                    + "," + VqsvSourceOps.sourceItemCount(s, 1) + "]"
                    + " trace=" + tailTrace(s, 12));
        }
        s.sourceStateTrace.add("SMOKE verified " + label + " menuIds="
                + java.util.Arrays.toString(s.battleMenuIds)
                + " counts=[" + VqsvSourceOps.sourceItemCount(s, 0)
                + "," + VqsvSourceOps.sourceItemCount(s, 1) + "]");
    }

    private static void assertCaughtPayload(SourcePetState pet, int expectedVisualId, String label) {
        if (pet == null || pet.sourcePayload == null || pet.sourcePayload.length < 10) {
            throw new IllegalStateException(label + " missing source payload");
        }
        int skillCount = pet.sourcePayload[9];
        if (pet.sourcePayload[0] != pet.speciesId
                || pet.sourcePayload[1] != pet.level
                || pet.sourcePayload[6] <= 0
                || pet.sourcePayload[8] != expectedVisualId
                || skillCount < 0
                || pet.sourcePayload.length != 10 + skillCount * 2) {
            throw new IllegalStateException(label + " bad game.b.P payload species="
                    + pet.speciesId + " visual=" + expectedVisualId
                    + " payload=" + java.util.Arrays.toString(pet.sourcePayload));
        }
    }

    private static void seedSourcePets(VqsvIntroDemo.Scene s, int count) {
        for (int i = 0; i < count; i++) {
            s.sourcePets.add(new SourcePetState(i, 17 + i, 7, 3, 2, 10, 45));
        }
    }

    private static boolean runBattleChoiceInputCheckpoint(VqsvIntroDemo.Scene s, String checkpoint) {
        if ("battle_choice_ui_p4_wheel_hover_click_viewport".equals(checkpoint)) {
            SourceBattleRuntime runtime = setupBattleP4ChoiceRowsForSmoke(s);
            int initialSelected = s.battleMenuIndex;
            s.mouseWheel(3);
            s.tick();
            if (!"P4".equals(s.battleStateName)
                    || !"choice".equals(s.battleUiMode)
                    || s.battleMenuScroll != 3
                    || s.battleMenuIndex != initialSelected
                    || !traceContains(s, "PC_QOL mouse wheel battle list scrollbar state=P4 ui=choice index=0 scroll=3")) {
                throw new IllegalStateException("Expected P4 choice.ui wheel to move viewport only"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " initial=" + initialSelected
                        + " scroll=" + s.battleMenuScroll
                        + " trace=" + tailTrace(s, 28));
            }
            s.battleHoverX = 60;
            s.battleHoverY = 125;
            runtime.debugHandleMenuInputForSmoke(s);
            if (s.battleMenuIndex != 5
                    || s.battleMenuScroll != 3
                    || s.battleMenuIds[s.battleMenuIndex] != 9) {
                throw new IllegalStateException("Expected P4 hover after wheel to select viewport index 5 item9"
                        + " index=" + s.battleMenuIndex
                        + " id=" + sourceMenuIdForTrace(s)
                        + " scroll=" + s.battleMenuScroll
                        + " trace=" + tailTrace(s, 30));
            }
            s.battleClickX = 60;
            s.battleClickY = 140;
            runtime.debugHandleMenuInputForSmoke(s);
            if (s.battleMenuIndex != 6
                    || s.battleMenuIds[s.battleMenuIndex] != 10
                    || s.battleClickX != -1
                    || s.battleClickY != -1) {
                throw new IllegalStateException("Expected P4 click after wheel to confirm viewport index 6 item10"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " id=" + sourceMenuIdForTrace(s)
                        + " click=" + s.battleClickX + "," + s.battleClickY
                        + " trace=" + tailTrace(s, 32));
            }
            assertRenderedColorPixels(s, "P4 choice selected viewport row", 54, 140, 130, 14, 0x51dbeb, 500);
            return true;
        }
        if ("battle_choice_ui_p21_synthetic_wheel_hover_click_viewport".equals(checkpoint)) {
            SourceBattleRuntime runtime = setupSyntheticP21ChoiceRowsForSmoke(s);
            int initialSelected = s.battleMenuIndex;
            s.mouseWheel(2);
            if (!"P21".equals(s.battleStateName)
                    || !"choice".equals(s.battleUiMode)
                    || s.battleMenuScroll != 2
                    || s.battleMenuIndex != initialSelected
                    || !traceContains(s, "PC_QOL mouse wheel battle list scrollbar state=P21 ui=choice index=0 scroll=2")) {
                throw new IllegalStateException("Expected P21 synthetic choice.ui wheel to move viewport only"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " index=" + s.battleMenuIndex
                        + " initial=" + initialSelected
                        + " scroll=" + s.battleMenuScroll
                        + " trace=" + tailTrace(s, 24));
            }
            s.battleHoverX = 60;
            s.battleHoverY = 110;
            runtime.debugHandleMenuInputForSmoke(s);
            if (s.battleMenuIndex != 3 || s.battleMenuIds[s.battleMenuIndex] != 3) {
                throw new IllegalStateException("Expected P21 hover after wheel to select viewport index 3"
                        + " index=" + s.battleMenuIndex
                        + " id=" + sourceMenuIdForTrace(s)
                        + " scroll=" + s.battleMenuScroll
                        + " trace=" + tailTrace(s, 26));
            }
            s.battleClickX = 60;
            s.battleClickY = 125;
            runtime.debugHandleMenuInputForSmoke(s);
            if (s.battleMenuIndex != 4
                    || s.battleMenuIds[s.battleMenuIndex] != 4
                    || s.battleClickX != -1
                    || s.battleClickY != -1) {
                throw new IllegalStateException("Expected P21 click after wheel to confirm viewport index 4"
                        + " index=" + s.battleMenuIndex
                        + " id=" + sourceMenuIdForTrace(s)
                        + " click=" + s.battleClickX + "," + s.battleClickY
                        + " trace=" + tailTrace(s, 28));
            }
            assertRenderedColorPixels(s, "P21 choice selected viewport row", 54, 125, 130, 14, 0x51dbeb, 500);
            return true;
        }
        if ("battle_choice_ui_p16_audit_not_choice_ui".equals(checkpoint)) {
            setupElderItemBattleWithReserve(s, 4, 1);
            driveItemCommandToP16(s);
            if (!"P16".equals(s.battleStateName)
                    || !"petstate".equals(s.battleUiMode)
                    || "choice".equals(s.battleUiMode)
                    || !traceContains(s, "battle P16 petstate.ui open")) {
                throw new IllegalStateException("Expected P16 item target to be petstate.ui, not choice.ui"
                        + " state=" + s.battleStateName
                        + " ui=" + s.battleUiMode
                        + " trace=" + tailTrace(s, 22));
            }
            s.sourceStateTrace.add("SMOKE verified P16 uses petstate.ui; choice.ui mapping not applicable");
            return true;
        }
        return false;
    }

    private static int sourceMenuIdForTrace(VqsvIntroDemo.Scene s) {
        return s.battleMenuIndex >= 0 && s.battleMenuIndex < s.battleMenuIds.length
                ? s.battleMenuIds[s.battleMenuIndex] : -1;
    }

    private static SourceBattleRuntime setupSyntheticScrollableSkillListForSmoke(VqsvIntroDemo.Scene s) {
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        s.battleOverlayTicks = 100;
        s.battleUiMode = "choiceskill";
        s.battleUiModeStartTick = s.battleAnimationTick;
        s.battlePlayerVisualId = 17;
        s.battleEnemyVisualId = 68;
        s.battlePlayerMaxHp = 134;
        s.battlePlayerHp = 134;
        s.battleEnemyMaxHp = 109;
        s.battleEnemyHp = 109;
        s.battleSkillIndex = 0;
        s.battleSkillScroll = 0;
        s.battleSkillIds = new int[]{10, 11, 12, 13, 14, 15, 16, 17};
        s.battleSkillNames = new String[s.battleSkillIds.length];
        s.battleSkillPpLabels = new String[s.battleSkillIds.length];
        for (int i = 0; i < s.battleSkillIds.length; i++) {
            BattleSkillRow row = VqsvBattleTables.instance().skill(s.battleSkillIds[i]);
            s.battleSkillNames[i] = row == null ? "Skill " + s.battleSkillIds[i]
                    : row.name("Skill " + s.battleSkillIds[i]);
            s.battleSkillPpLabels[i] = row == null ? "" : "1/" + row.ppMax;
        }
        BattleSkillRow selected = VqsvBattleTables.instance().skill(s.battleSkillIds[0]);
        s.battleSkillDescription = selected == null ? "" : selected.description("");
        s.sourceStateTrace.add("SMOKE synthetic choiceskill.ui scrollable ids="
                + java.util.Arrays.toString(s.battleSkillIds));
        return runtime;
    }

    private static SourceBattleRuntime setupBattleP4ChoiceRowsForSmoke(VqsvIntroDemo.Scene s) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        for (int id = 4; id <= 11; id++) {
            s.sourceBagItems.put(id, new BagItem(id, 1, 1, false));
        }
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 98;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P4", 80);
        if (!"choice".equals(s.battleUiMode) || s.battleMenuIds.length < 8) {
            throw new IllegalStateException("Expected P4 choice.ui scrollable item rows"
                    + " state=" + s.battleStateName
                    + " ui=" + s.battleUiMode
                    + " ids=" + java.util.Arrays.toString(s.battleMenuIds)
                    + " trace=" + tailTrace(s, 18));
        }
        return runtime;
    }

    private static SourceBattleRuntime setupSyntheticP21ChoiceRowsForSmoke(VqsvIntroDemo.Scene s) {
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        s.battleOverlayTicks = 100;
        runtime.debugSetStateForSmoke(s, BattleRuntimeState.P21_CATCH_LIST);
        s.battleUiMode = "choice";
        s.battleUiModeStartTick = s.battleAnimationTick;
        s.battlePlayerVisualId = 17;
        s.battleEnemyVisualId = 68;
        s.battlePlayerMaxHp = 134;
        s.battlePlayerHp = 134;
        s.battleEnemyMaxHp = 109;
        s.battleEnemyHp = 109;
        s.battleMenuTitle = "Pokemon ball";
        s.battleMenuSubtitle = "T\u1ec9 l\u1ec7 b\u1eaft";
        s.battleMenuAction = "S\u1eed d\u1ee5ng";
        s.battleMenuNames = new String[]{
                "Ball 0", "Ball 1", "Ball 2", "Ball 3", "Ball 4", "Ball 5", "Ball 6"
        };
        s.battleMenuValues = new String[]{"100%", "90%", "80%", "70%", "60%", "50%", "40%"};
        s.battleMenuDescriptions = new String[s.battleMenuNames.length];
        s.battleMenuIds = new int[]{0, 1, 2, 3, 4, 5, 6};
        s.battleMenuIconIds = new int[]{0, 1, 2, 3, 4, 5, 6};
        s.battleMenuIndex = 0;
        s.battleMenuScroll = 0;
        s.battleChoiceUi = VqsvChoiceUiView.fromScene(s);
        s.sourceStateTrace.add("SMOKE synthetic P21 choice.ui scrollable ids="
                + java.util.Arrays.toString(s.battleMenuIds));
        return runtime;
    }

    private static void seedSourceBank(VqsvIntroDemo.Scene s, int count) {
        for (int i = 0; i < count; i++) {
            s.sourcePetBank.add(new SourcePetState(i, 17 + i % 20, 7, 3, 2, 10, 45));
        }
    }

    private static boolean sourcePetHasSkill(SourcePetState pet, int skillId) {
        if (pet == null || pet.sourcePayload == null || pet.sourcePayload.length <= 10) {
            return false;
        }
        int count = Math.max(0, pet.sourcePayload[9]);
        for (int i = 0; i < count && 10 + i < pet.sourcePayload.length; i++) {
            if (pet.sourcePayload[10 + i] == skillId) {
                return true;
            }
        }
        return false;
    }

    private static String tailTrace(VqsvIntroDemo.Scene s, int count) {
        int start = Math.max(0, s.sourceStateTrace.size() - count);
        return s.sourceStateTrace.subList(start, s.sourceStateTrace.size()).toString();
    }

    private static String rngTraceSummary(VqsvIntroDemo.Scene s) {
        java.util.ArrayList<String> entries = new java.util.ArrayList<>();
        for (String line : s.sourceStateTrace) {
            if (line.contains("RNG TRACE ")) {
                entries.add(line);
            }
        }
        return entries.toString();
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
}
