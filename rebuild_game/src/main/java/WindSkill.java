import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

final class WindSkill implements Skill {
    static final WindSkill INSTANCE = new WindSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL60_69_WIND_CLOSEOUT_SUITE = {
            "battle_wind_skills_60_69_closeout"
    };

    private WindSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_wind_skills_60_69_closeout".equals(suite)) {
            return SKILL60_69_WIND_CLOSEOUT_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runWindSkills60To69CloseoutSmokeIfNeeded(checkpoint, outPath);
    }

    private static boolean runWindSkills60To69CloseoutSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_wind_skills_60_69_closeout".equals(checkpoint)) {
            return false;
        }
        try {
            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            assertWindSkills60To69SourceRows(checkpoint);
            WindSkillCaseResult skill60 = runWindSkillCase(
                    60, "phong_nhan", -1, -1, false, false, dir);
            WindSkillCaseResult skill61 = runWindSkillCase(
                    61, "phong_ap", -1, 5, false, false, dir);
            WindSkillCaseResult skill62 = runWindSkillCase(
                    62, "thuan_phong", 10, -1, false, false, dir);
            WindSkillCaseResult skill63 = runWindSkillCase(
                    63, "long_quyen", -1, -1, false, true, dir);
            WindSkillCaseResult skill64 = runWindSkillCase(
                    64, "nghich_phong_doat", 11, -1, true, false, dir);
            WindSkillCaseResult skill65 = runWindSkillCase(
                    65, "vo_liet_thuat", 12, -1, false, false, dir);
            WindSkillCaseResult skill66 = runWindSkillCase(
                    66, "yen_hoi_thiem", -1, -1, false, false, dir);
            WindSkillCaseResult skill67 = runWindSkillCase(
                    67, "phong_chi_tuyen_qua", -1, -1, false, false, dir);
            WindSkillCaseResult skill68 = runWindSkillCase(
                    68, "phong_chi_tu_hau", 10, -1, false, false, dir);
            WindSkillCaseResult skill69 = runWindSkillCase(
                    69, "phi_yen_hoan_sao", -1, -1, false, true, dir);
            WindFollowUpResult follow63 = runWindFollowUpCase(63, "long_quyen_followup_pass", dir);
            WindFollowUpResult follow69 = runWindFollowUpCase(69, "phi_yen_hoan_sao_followup_pass", dir);

            WindSkillCaseResult[] results = new WindSkillCaseResult[]{
                    skill60, skill61, skill62, skill63, skill64,
                    skill65, skill66, skill67, skill68, skill69
            };
            StringBuilder debug = new StringBuilder();
            debug.append("checkpoint=").append(checkpoint).append('\n');
            debug.append("source=aq.c[1][60..69] + effect.mid[60..69] from S60 merged tables\n");
            debug.append("status=PORTED/PARTIAL runtime source row/effect/HP/PP/status/follow-up verified; pixel-perfect pending\n");
            for (WindSkillCaseResult result : results) {
                debug.append(result.describe());
            }
            debug.append(follow63.describe()).append(follow69.describe());
            Files.write(new java.io.File(dir,
                            "battle_wind_skills_60_69_closeout_debug.txt").toPath(),
                    debug.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            copySummaryPng(out, dir, skill69);

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill60Damage=" + skill60.damage
                    + " skill61Damage=" + skill61.damage + "/debuff5:" + skill61.enemyDebuffActive
                    + " skill62Damage=" + skill62.damage + "/buff10:" + skill62.playerBuffActive
                    + " skill63Damage=" + skill63.damage + "/followFail:" + !skill63.followUpTriggered
                    + " skill64Buff11=" + skill64.playerBuffActive + "/copyBuff2:" + skill64.playerCopiedBuff2
                    + " skill65Buff12=" + skill65.playerBuffActive + "/k12:" + skill65.playerK12
                    + " skill66Damage=" + skill66.damage
                    + " skill67Damage=" + skill67.damage + "/sourceOddityNoDebuff:" + !skill67.enemyDebuffActive
                    + " skill68Damage=" + skill68.damage + "/buff10:" + skill68.playerBuffActive
                    + " skill69Damage=" + skill69.damage + "/followFail:" + !skill69.followUpTriggered
                    + " follow63Pass=" + follow63.followUpTriggered
                    + " follow69Pass=" + follow69.followUpTriggered
                    + " images=skill60..skill69 before/effect/result");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static WindSkillCaseResult runWindSkillCase(int skillId, String slug,
                                                        int expectedPlayerBuffId,
                                                        int expectedEnemyDebuffId,
                                                        boolean seedEnemyBuffForCopy,
                                                        boolean forceFollowUpFail,
                                                        java.io.File dir)
            throws java.io.IOException {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        if (row == null) {
            throw new IllegalStateException("Missing skill row " + skillId);
        }
        if (seedEnemyBuffForCopy) {
            runtime.debugEnemySourceBuffForClearSmoke(s, 2, 10, 14);
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        String prefix = "battle_skill" + skillId + "_" + slug + "_timeline_";
        writeScenePng(s, new java.io.File(dir, prefix + "before.png"));

        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        runtime.debugSetNextDamageDebuffRollForSmoke(0);
        if (forceFollowUpFail) {
            runtime.debugSetNextFollowUpRollForSmoke(99);
        }
        enterP7FromSkillList(s);
        tickUntilBattleP7Phase(s, 1, 260);
        for (int i = 0; i < 70
                && !s.battleP7ActorEffectVisible
                && !s.battleP7SpecialVisible; i++) {
            s.tick();
        }
        assertWindSkillFirstVisual(s, runtime, skillId, beforeEnemyHp, beforePp);
        writeScenePng(s, new java.io.File(dir, prefix + "effect_start.png"));

        int damage = 0;
        if (row.powerPercent > 0) {
            tickUntilBattleP7Phase(s, 2, 620);
            damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
            if (damage <= 0) {
                throw new IllegalStateException("Expected skill" + skillId + " to apply damage"
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " beforeEnemyHp=" + beforeEnemyHp
                        + " trace=" + tailTrace(s, 180));
            }
        }

        int guard = 0;
        while ("P7".equals(s.battleStateName) && s.battleP7Phase < 3 && guard++ < 900) {
            s.tick();
        }
        for (int i = 0; i < 24; i++) {
            s.tick();
        }

        boolean playerBuffActive = expectedPlayerBuffId >= 0
                && runtime.debugPlayerHasBuffForSmoke(expectedPlayerBuffId);
        boolean enemyDebuffActive = expectedEnemyDebuffId >= 0
                && runtime.debugEnemyHasDebuffForSmoke(expectedEnemyDebuffId);
        if (expectedPlayerBuffId >= 0 && !playerBuffActive) {
            throw new IllegalStateException("Expected skill" + skillId + " player buff"
                    + " id=" + expectedPlayerBuffId
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(expectedPlayerBuffId)
                    + " trace=" + tailTrace(s, 180));
        }
        if (expectedEnemyDebuffId >= 0 && !enemyDebuffActive) {
            throw new IllegalStateException("Expected skill" + skillId + " enemy debuff"
                    + " id=" + expectedEnemyDebuffId
                    + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId)
                    + " trace=" + tailTrace(s, 180));
        }
        if (skillId == 67 && (runtime.debugEnemyHasDebuffForSmoke(5)
                || !traceContains(s, "BYTECODE_DEFAULT_RAW_DAMAGE skill=67")
                || !traceContains(s, "appliedDebuffId=-1"))) {
            throw new IllegalStateException("Expected skill67 source oddity raw damage with no debuff5"
                    + " enemyDebuff5=" + runtime.debugEnemyHasDebuffForSmoke(5)
                    + " trace=" + tailTrace(s, 220));
        }
        boolean copiedBuff2 = false;
        if (skillId == 64) {
            copiedBuff2 = runtime.debugPlayerHasBuffForSmoke(2)
                    && !runtime.debugEnemyHasBuffForSmoke(2)
                    && runtime.debugPlayerHasBuffForSmoke(11)
                    && runtime.debugPlayerBuffValueForSmoke(11) == 0;
            if (!copiedBuff2 || traceContains(s, "battle P7 damage frame skill=64")) {
                throw new IllegalStateException("Expected skill64 to copy enemy buff2, clear donor, apply buff11, and skip damage"
                        + " playerBuff2=" + runtime.debugPlayerHasBuffForSmoke(2)
                        + " enemyBuff2=" + runtime.debugEnemyHasBuffForSmoke(2)
                        + " playerBuff11=" + runtime.debugPlayerHasBuffForSmoke(11)
                        + " playerBuff11Value=" + runtime.debugPlayerBuffValueForSmoke(11)
                        + " trace=" + tailTrace(s, 220));
            }
        }
        int playerK12 = skillId == 65 ? runtime.debugPlayerK12ForSmoke() : Integer.MIN_VALUE;
        if (skillId == 65 && playerK12 != 1) {
            throw new IllegalStateException("Expected skill65 buff12 K12=1 after producer"
                    + " k12=" + playerK12
                    + " trace=" + tailTrace(s, 220));
        }
        boolean followUpTriggered = traceContains(s, "follow-up P2 from skill=" + skillId);
        if (forceFollowUpFail && followUpTriggered) {
            throw new IllegalStateException("Expected skill" + skillId + " forced follow-up fail"
                    + " state=" + s.battleStateName
                    + " trace=" + tailTrace(s, 220));
        }
        if (row.powerPercent == 0 && s.battleEnemyHp != beforeEnemyHp) {
            throw new IllegalStateException("Expected skill" + skillId + " no damage"
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " beforeEnemyHp=" + beforeEnemyHp);
        }
        if (runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1) {
            throw new IllegalStateException("Expected skill" + skillId + " PP to decrement by 1"
                    + " beforePp=" + beforePp
                    + " afterPp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 160));
        }
        writeScenePng(s, new java.io.File(dir, prefix + "result.png"));

        return new WindSkillCaseResult(skillId, sourceSkillName(skillId),
                beforePlayerHp, s.battlePlayerHp,
                beforeEnemyHp, s.battleEnemyHp,
                beforePp, runtime.debugPlayerSkillPpForSmoke(0),
                damage,
                expectedPlayerBuffId, playerBuffActive,
                expectedPlayerBuffId >= 0 ? runtime.debugPlayerBuffDurationForSmoke(expectedPlayerBuffId) : 0,
                expectedEnemyDebuffId, enemyDebuffActive,
                expectedEnemyDebuffId >= 0 ? runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId) : 0,
                copiedBuff2, playerK12, followUpTriggered,
                java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)));
    }

    private static WindFollowUpResult runWindFollowUpCase(int skillId, String slug, java.io.File dir)
            throws java.io.IOException {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        runtime.debugSetNextFollowUpRollForSmoke(0);
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        enterP7FromSkillList(s);
        tickUntilBattleP7Phase(s, 3, 900);
        for (int i = 0; i < 36 && !"P2".equals(s.battleStateName); i++) {
            s.tick();
        }
        boolean followUp = "P2".equals(s.battleStateName)
                && traceContains(s, "follow-up P2 from skill=" + skillId);
        if (!followUp) {
            throw new IllegalStateException("Expected skill" + skillId + " forced follow-up pass to enter P2"
                    + " state=" + s.battleStateName
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 220));
        }
        java.io.File out = new java.io.File(dir, "battle_skill" + skillId + "_" + slug + ".png");
        writeScenePng(s, out);
        return new WindFollowUpResult(skillId, beforePp, runtime.debugPlayerSkillPpForSmoke(0), true);
    }

    private static void assertWindSkillFirstVisual(VqsvIntroDemo.Scene s,
                                                   SourceBattleRuntime runtime,
                                                   int skillId,
                                                   int beforeEnemyHp,
                                                   int beforePp) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        boolean expectedSpecial = effect.length >= 2 && effect[1] == 1;
        boolean expectedPlayerSide = effect.length > 0
                && (effect[0] == 1 || (row != null && row.targetSide == 1));
        boolean ok;
        if (expectedSpecial) {
            short[] speffect = VqsvBattleAnimationTables.instance().speffectRow(effect[2]);
            int expectedType = speffect.length == 0 ? -1 : speffect[0];
            ok = s.battleP7SpecialVisible
                    && s.battleP7SpecialType == expectedType
                    && s.battleP7SpecialOnPlayerSide == expectedPlayerSide;
        } else {
            ok = s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSpriteId == spriteForSourceEffect(effect[2])
                    && s.battleP7ActorEffectState == effect[3]
                    && s.battleP7ActorEffectOnPlayerSide == expectedPlayerSide;
        }
        if (!ok
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1
                || !traceContains(s, "battle P7 source n() skill=" + skillId)) {
            throw new IllegalStateException("Expected wind skill" + skillId + " first visual from effect.mid"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " specialType=" + s.battleP7SpecialType
                    + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                    + " expectedSidePlayer=" + expectedPlayerSide
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 180));
        }
    }

    private static int spriteForSourceEffect(int sourceEffectId) {
        switch (sourceEffectId) {
            case 26:
                return 268;
            case 32:
                return 307;
            case 34:
                return 309;
            default:
                return -1;
        }
    }

    private static void assertWindSkills60To69SourceRows(String checkpoint) {
        assertWindSkillSourceRow(checkpoint, 60, 177, 589, 100, 0, 45, 0, -1, -1, 0,
                new byte[]{0, 0, 26, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 61, 178, 590, 80, 0, 45, 2, 5, 5, 0,
                new byte[]{0, 0, 26, 1, -1, -1, 0, 0, 1, 11, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 62, 179, 591, 80, 0, 45, 1, 10, 5, 0,
                new byte[]{0, 0, 26, 2, 0, -1, 0,
                        0, 1, 0, 0, -1, -1, 0,
                        1, 1, 15, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 63, 180, 592, 100, 1, 30, 0, -1, 5, 0,
                new byte[]{0, 0, 26, 3, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 64, 181, 593, 0, 1, 10, 1, 11, -1, 0,
                new byte[]{0, 0, 34, 0, 0, -1, 0,
                        1, 1, 18, 0, -1, -1, 0,
                        1, 1, 15, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 65, 182, 594, 0, 1, 10, 1, 12, -1, 1,
                new byte[]{0, 0, 32, 0, 0, -1, 0,
                        1, 1, 16, 0, -1, -1, 0,
                        1, 1, 15, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 66, 183, 595, 150, 2, 30, 0, -1, -1, 0,
                new byte[]{0, 0, 26, 4, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 67, 184, 596, 110, 2, 30, 2, 5, 5, 0,
                new byte[]{0, 0, 26, 5, -1, -1, 0, 0, 1, 11, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 68, 185, 597, 110, 3, 15, 1, 10, 5, 0,
                new byte[]{0, 0, 26, 6, -1, -1, 0,
                        0, 1, 0, 0, -1, -1, 0,
                        1, 1, 15, 0, -1, -1, 0});
        assertWindSkillSourceRow(checkpoint, 69, 186, 598, 150, 3, 15, 0, -1, 8, 0,
                new byte[]{0, 0, 26, 7, -1, -1, 0});

        BattleBuffRow buff10 = VqsvBattleTables.instance().buff(10);
        BattleBuffRow buff11 = VqsvBattleTables.instance().buff(11);
        BattleBuffRow buff12 = VqsvBattleTables.instance().buff(12);
        BattleDebuffRow debuff5 = VqsvBattleTables.instance().debuff(5);
        short[] speffect0 = VqsvBattleAnimationTables.instance().speffectRow(0);
        short[] speffect11 = VqsvBattleAnimationTables.instance().speffectRow(11);
        short[] speffect15 = VqsvBattleAnimationTables.instance().speffectRow(15);
        short[] speffect16 = VqsvBattleAnimationTables.instance().speffectRow(16);
        short[] speffect18 = VqsvBattleAnimationTables.instance().speffectRow(18);
        if (buff10 == null || buff10.duration != 2
                || buff11 == null || buff11.duration != 3
                || buff12 == null || buff12.duration != 2
                || debuff5 == null || debuff5.duration != 3
                || speffect0.length == 0 || speffect0[0] != 9
                || speffect11.length == 0 || speffect11[0] != 1
                || speffect15.length == 0 || speffect15[0] != 1
                || speffect16.length == 0 || speffect16[0] != 9
                || speffect18.length == 0 || speffect18[0] != 9) {
            throw new IllegalStateException(checkpoint + " wind skill 60..69 status/effect table mismatch"
                    + " buff10=" + (buff10 == null ? "null" : java.util.Arrays.toString(buff10.raw))
                    + " buff11=" + (buff11 == null ? "null" : java.util.Arrays.toString(buff11.raw))
                    + " buff12=" + (buff12 == null ? "null" : java.util.Arrays.toString(buff12.raw))
                    + " debuff5=" + (debuff5 == null ? "null" : java.util.Arrays.toString(debuff5.raw))
                    + " speffect0=" + java.util.Arrays.toString(speffect0)
                    + " speffect11=" + java.util.Arrays.toString(speffect11)
                    + " speffect15=" + java.util.Arrays.toString(speffect15)
                    + " speffect16=" + java.util.Arrays.toString(speffect16)
                    + " speffect18=" + java.util.Arrays.toString(speffect18));
        }
    }

    private static void assertWindSkillSourceRow(String checkpoint, int skillId,
                                                 int nameTextId, int descriptionTextId,
                                                 int powerPercent, int tier, int ppMax,
                                                 int effectMode, int effectId,
                                                 int chanceOrParam, int targetSide,
                                                 byte[] expectedEffect) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        if (row == null
                || row.elementFamily != 6
                || row.nameTextId != nameTextId
                || row.descriptionTextId != descriptionTextId
                || row.powerPercent != powerPercent
                || row.learnTier != tier
                || row.ppMax != ppMax
                || row.effectMode != effectMode
                || row.effectId != effectId
                || row.chanceOrParam != chanceOrParam
                || row.targetSide != targetSide
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill" + skillId + " source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
    }

    private static String sourceSkillName(int skillId) {
        switch (skillId) {
            case 60:
                return "Phong nhan";
            case 61:
                return "Phong ap";
            case 62:
                return "Thuan phong";
            case 63:
                return "Long quyen";
            case 64:
                return "Nghich Phong Doat";
            case 65:
                return "Vo Liet Thuat";
            case 66:
                return "Yen Hoi Thiem";
            case 67:
                return "Phong Chi Tuyen Qua";
            case 68:
                return "Phong Chi Tu Hau";
            case 69:
                return "Phi Yen Hoan Sao";
            default:
                return "Skill " + skillId;
        }
    }

    private static void enterP7FromSkillList(VqsvIntroDemo.Scene s) {
        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 160);
    }

    private static void tickUntilBattleState(VqsvIntroDemo.Scene s, String stateName, int maxTicks) {
        VqsvSmokeHarness.tickUntilBattleState(s, stateName, maxTicks);
    }

    private static void tickUntilBattleP7Phase(VqsvIntroDemo.Scene s, int phase, int maxTicks) {
        VqsvSmokeHarness.tickUntilBattleP7Phase(s, phase, maxTicks);
    }

    private static boolean traceContains(VqsvIntroDemo.Scene s, String needle) {
        return VqsvSmokeHarness.traceContains(s, needle);
    }

    private static int latestTraceDamage(VqsvIntroDemo.Scene s, String needle) {
        return VqsvSmokeHarness.latestTraceDamage(s, needle);
    }

    private static String tailTrace(VqsvIntroDemo.Scene s, int count) {
        return VqsvSmokeHarness.tailTrace(s, count);
    }

    private static void writeScenePng(VqsvIntroDemo.Scene s, java.io.File out) throws java.io.IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        s.render(g);
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    private static void copySummaryPng(java.io.File out, java.io.File dir,
                                       WindSkillCaseResult result) throws java.io.IOException {
        String name = "battle_skill" + result.skillId + "_phi_yen_hoan_sao_timeline_result.png";
        java.nio.file.Files.copy(new java.io.File(dir, name).toPath(), out.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static SourceBattleRuntime enterElderP3BeforeConfirm(VqsvIntroDemo.Scene s, int skillId) {
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, skillId, 45));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        s.battleClickX = 20;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P3", 80);
        for (int i = 0; i < 10; i++) {
            s.tick();
        }
        return runtime;
    }

    private static final class WindSkillCaseResult {
        final int skillId;
        final String name;
        final int beforePlayerHp;
        final int afterPlayerHp;
        final int beforeEnemyHp;
        final int afterEnemyHp;
        final int beforePp;
        final int afterPp;
        final int damage;
        final int playerBuffId;
        final boolean playerBuffActive;
        final int playerBuffDuration;
        final int enemyDebuffId;
        final boolean enemyDebuffActive;
        final int enemyDebuffDuration;
        final boolean playerCopiedBuff2;
        final int playerK12;
        final boolean followUpTriggered;
        final String effectRow;

        WindSkillCaseResult(int skillId, String name,
                            int beforePlayerHp, int afterPlayerHp,
                            int beforeEnemyHp, int afterEnemyHp,
                            int beforePp, int afterPp,
                            int damage,
                            int playerBuffId, boolean playerBuffActive, int playerBuffDuration,
                            int enemyDebuffId, boolean enemyDebuffActive, int enemyDebuffDuration,
                            boolean playerCopiedBuff2, int playerK12, boolean followUpTriggered,
                            String effectRow) {
            this.skillId = skillId;
            this.name = name;
            this.beforePlayerHp = beforePlayerHp;
            this.afterPlayerHp = afterPlayerHp;
            this.beforeEnemyHp = beforeEnemyHp;
            this.afterEnemyHp = afterEnemyHp;
            this.beforePp = beforePp;
            this.afterPp = afterPp;
            this.damage = damage;
            this.playerBuffId = playerBuffId;
            this.playerBuffActive = playerBuffActive;
            this.playerBuffDuration = playerBuffDuration;
            this.enemyDebuffId = enemyDebuffId;
            this.enemyDebuffActive = enemyDebuffActive;
            this.enemyDebuffDuration = enemyDebuffDuration;
            this.playerCopiedBuff2 = playerCopiedBuff2;
            this.playerK12 = playerK12;
            this.followUpTriggered = followUpTriggered;
            this.effectRow = effectRow;
        }

        String describe() {
            return "skill=" + skillId
                    + " name=" + name
                    + " hpPlayer=" + beforePlayerHp + "->" + afterPlayerHp
                    + " hpEnemy=" + beforeEnemyHp + "->" + afterEnemyHp
                    + " pp=" + beforePp + "->" + afterPp
                    + " damage=" + damage
                    + " playerBuff=" + playerBuffId + ":" + playerBuffActive
                    + "/" + playerBuffDuration
                    + " enemyDebuff=" + enemyDebuffId + ":" + enemyDebuffActive
                    + "/" + enemyDebuffDuration
                    + " copiedBuff2=" + playerCopiedBuff2
                    + " k12=" + playerK12
                    + " followUp=" + followUpTriggered
                    + " effect.mid=" + effectRow
                    + "\n";
        }
    }

    private static final class WindFollowUpResult {
        final int skillId;
        final int beforePp;
        final int afterPp;
        final boolean followUpTriggered;

        WindFollowUpResult(int skillId, int beforePp, int afterPp, boolean followUpTriggered) {
            this.skillId = skillId;
            this.beforePp = beforePp;
            this.afterPp = afterPp;
            this.followUpTriggered = followUpTriggered;
        }

        String describe() {
            return "followup skill=" + skillId
                    + " pp=" + beforePp + "->" + afterPp
                    + " followUp=" + followUpTriggered
                    + "\n";
        }
    }
}
