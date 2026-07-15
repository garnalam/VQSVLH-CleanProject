import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

final class WaterSkill implements Skill {
    static final WaterSkill INSTANCE = new WaterSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL30_39_WATER_CLOSEOUT_SUITE = {
            "battle_water_skills_30_39_closeout"
    };

    private WaterSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_water_skills_30_39_closeout".equals(suite)) {
            return SKILL30_39_WATER_CLOSEOUT_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runWaterSkills30To39CloseoutSmokeIfNeeded(checkpoint, outPath);
    }

    private static boolean runWaterSkills30To39CloseoutSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_water_skills_30_39_closeout".equals(checkpoint)) {
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

            assertWaterSkills30To39SourceRows(checkpoint);
            WaterSkillCaseResult skill30 = runWaterSkillCase(
                    30, "bong_bang", -1, -1, dir);
            WaterSkillCaseResult skill31 = runWaterSkillCase(
                    31, "bang_lao", -1, 4, dir);
            WaterSkillCaseResult skill32 = runWaterSkillCase(
                    32, "tuyet_anh", -1, 5, dir);
            WaterSkillCaseResult skill33 = runWaterSkillCase(
                    33, "thuy_tri", -1, 6, dir);
            WaterSkillCaseResult skill34 = runWaterSkillCase(
                    34, "thuat_cau_nguyen", 5, -1, dir);
            WaterSkillCaseResult skill35 = runWaterSkillCase(
                    35, "thuy_bich", 6, -1, dir);
            WaterSkillCaseResult skill36 = runWaterSkillCase(
                    36, "bao_phong_tuyet", -1, -1, dir);
            WaterSkillCaseResult skill37 = runWaterSkillCase(
                    37, "la_chan_gia_tuyet", -1, 4, dir);
            WaterSkillCaseResult skill38 = runWaterSkillCase(
                    38, "bang_phong_ham_tinh", -1, 5, dir);
            WaterSkillCaseResult skill39 = runWaterSkillCase(
                    39, "ray_lanh", -1, 6, dir);

            WaterSkillCaseResult[] results = new WaterSkillCaseResult[]{
                    skill30, skill31, skill32, skill33, skill34,
                    skill35, skill36, skill37, skill38, skill39
            };
            StringBuilder debug = new StringBuilder();
            debug.append("checkpoint=").append(checkpoint).append('\n');
            debug.append("source=aq.c[1][30..39] + effect.mid[30..39] from S60 merged tables\n");
            debug.append("status=PORTED/PARTIAL runtime source row/effect/HP/PP/status verified; pixel-perfect pending\n");
            debug.append("note=buff6 mechanics follow current PC rebuild INTENTIONAL_DEVIATION decided earlier\n");
            for (WaterSkillCaseResult result : results) {
                debug.append(result.describe());
            }
            Files.write(new java.io.File(dir,
                            "battle_water_skills_30_39_closeout_debug.txt").toPath(),
                    debug.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            copySummaryPng(out, dir, skill39);

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill30Damage=" + skill30.damage
                    + " skill31Damage=" + skill31.damage + "/debuff4:" + skill31.enemyDebuffActive
                    + " skill32Damage=" + skill32.damage + "/debuff5:" + skill32.enemyDebuffActive
                    + " skill33Damage=" + skill33.damage + "/debuff6:" + skill33.enemyDebuffActive
                    + " skill34=buff5:" + skill34.playerBuffActive
                    + " skill35=buff6:" + skill35.playerBuffActive
                    + " skill36Damage=" + skill36.damage
                    + " skill37Damage=" + skill37.damage + "/debuff4:" + skill37.enemyDebuffActive
                    + " skill38Damage=" + skill38.damage + "/debuff5:" + skill38.enemyDebuffActive
                    + " skill39Damage=" + skill39.damage + "/debuff6:" + skill39.enemyDebuffActive
                    + " images=skill30..skill39 before/effect/result");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static WaterSkillCaseResult runWaterSkillCase(int skillId, String slug,
                                                          int expectedPlayerBuffId,
                                                          int expectedEnemyDebuffId,
                                                          java.io.File dir)
            throws java.io.IOException {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        if (row == null) {
            throw new IllegalStateException("Missing skill row " + skillId);
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        String prefix = "battle_skill" + skillId + "_" + slug + "_timeline_";
        writeScenePng(s, new java.io.File(dir, prefix + "before.png"));

        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        runtime.debugSetNextDamageDebuffRollForSmoke(0);
        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 160);
        tickUntilBattleP7Phase(s, 1, 220);
        for (int i = 0; i < 48
                && !s.battleP7ActorEffectVisible
                && !s.battleP7SpecialVisible; i++) {
            s.tick();
        }
        assertWaterSkillFirstVisual(s, runtime, skillId, beforeEnemyHp, beforePp);
        writeScenePng(s, new java.io.File(dir, prefix + "effect_start.png"));

        int damage = 0;
        if (row.powerPercent > 0) {
            tickUntilBattleP7Phase(s, 2, 520);
            damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
            if (damage <= 0) {
                throw new IllegalStateException("Expected skill" + skillId + " to apply damage"
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " beforeEnemyHp=" + beforeEnemyHp
                        + " trace=" + tailTrace(s, 140));
            }
        }

        int guard = 0;
        while ("P7".equals(s.battleStateName) && s.battleP7Phase < 3 && guard++ < 700) {
            s.tick();
        }
        for (int i = 0; i < 16; i++) {
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
                    + " trace=" + tailTrace(s, 140));
        }
        if (expectedEnemyDebuffId >= 0 && !enemyDebuffActive) {
            throw new IllegalStateException("Expected skill" + skillId + " enemy debuff"
                    + " id=" + expectedEnemyDebuffId
                    + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId)
                    + " trace=" + tailTrace(s, 140));
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
                    + " trace=" + tailTrace(s, 100));
        }
        writeScenePng(s, new java.io.File(dir, prefix + "result.png"));

        return new WaterSkillCaseResult(skillId, sourceSkillName(skillId),
                beforePlayerHp, s.battlePlayerHp,
                beforeEnemyHp, s.battleEnemyHp,
                beforePp, runtime.debugPlayerSkillPpForSmoke(0),
                damage,
                expectedPlayerBuffId, playerBuffActive,
                expectedPlayerBuffId >= 0 ? runtime.debugPlayerBuffDurationForSmoke(expectedPlayerBuffId) : 0,
                expectedEnemyDebuffId, enemyDebuffActive,
                expectedEnemyDebuffId >= 0 ? runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId) : 0,
                java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)));
    }

    private static void assertWaterSkillFirstVisual(VqsvIntroDemo.Scene s,
                                                    SourceBattleRuntime runtime,
                                                    int skillId,
                                                    int beforeEnemyHp,
                                                    int beforePp) {
        boolean ok;
        if (skillId == 35) {
            ok = s.battleP7SpecialVisible
                    && s.battleP7SpecialType == 7
                    && !s.battleP7ActorEffectVisible
                    && !traceContains(s, "battle P7 damage frame skill=35");
        } else {
            int expectedState = skillId <= 34 ? skillId - 30 : skillId - 31;
            ok = s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSpriteId == 265
                    && s.battleP7ActorEffectState == expectedState
                    && s.battleP7ActorEffectOnPlayerSide == (skillId == 34);
        }
        if (!ok
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1
                || !traceContains(s, "battle P7 source n() skill=" + skillId)) {
            throw new IllegalStateException("Expected water skill" + skillId + " first visual from effect.mid"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " specialType=" + s.battleP7SpecialType
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 120));
        }
    }

    private static void assertWaterSkills30To39SourceRows(String checkpoint) {
        assertWaterSkillSourceRow(checkpoint, 30, 147, 559, 100, 0, 45, 0, -1, -1, 0,
                new byte[]{0, 0, 23, 0, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 31, 148, 560, 60, 0, 45, 2, 4, 1, 0,
                new byte[]{0, 0, 23, 1, -1, -1, 0, 0, 0, 31, 0, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 32, 149, 561, 60, 0, 45, 2, 5, 10, 0,
                new byte[]{0, 0, 23, 2, 9, -1, 0, 0, 1, 1, 0, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 33, 150, 562, 100, 1, 30, 2, 6, 10, 0,
                new byte[]{0, 0, 23, 3, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 34, 151, 563, 0, 1, 10, 1, 5, -1, 1,
                new byte[]{0, 0, 23, 4, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 35, 152, 564, 0, 1, 10, 1, 6, -1, 1,
                new byte[]{0, 1, 4, 0, -1, -1, 0, 0, 1, 17, 0, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 36, 153, 565, 150, 2, 30, 0, -1, -1, 0,
                new byte[]{0, 0, 23, 5, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 37, 154, 566, 100, 2, 30, 2, 4, 2, 0,
                new byte[]{0, 0, 23, 6, 4, -1, 0,
                        0, 1, 7, 0, -1, 0, 0,
                        0, 0, 31, 0, 0, -1, 0,
                        0, 1, 6, 0, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 38, 155, 567, 150, 3, 15, 2, 5, 10, 0,
                new byte[]{0, 0, 23, 7, 4, -1, 0, 0, 1, 7, 0, -1, -1, 0});
        assertWaterSkillSourceRow(checkpoint, 39, 156, 568, 150, 3, 15, 2, 6, 10, 0,
                new byte[]{0, 0, 23, 8, -1, -1, 0});

        BattleBuffRow buff5 = VqsvBattleTables.instance().buff(5);
        BattleBuffRow buff6 = VqsvBattleTables.instance().buff(6);
        BattleDebuffRow debuff4 = VqsvBattleTables.instance().debuff(4);
        BattleDebuffRow debuff5 = VqsvBattleTables.instance().debuff(5);
        BattleDebuffRow debuff6 = VqsvBattleTables.instance().debuff(6);
        short[] speffect1 = VqsvBattleAnimationTables.instance().speffectRow(1);
        short[] speffect4 = VqsvBattleAnimationTables.instance().speffectRow(4);
        short[] speffect6 = VqsvBattleAnimationTables.instance().speffectRow(6);
        short[] speffect7 = VqsvBattleAnimationTables.instance().speffectRow(7);
        short[] speffect17 = VqsvBattleAnimationTables.instance().speffectRow(17);
        if (buff5 == null || buff5.duration != 3 || buff5.raw[3] != 30
                || buff6 == null || buff6.duration != 3 || buff6.raw[3] != 50
                || debuff4 == null || debuff4.duration != 3
                || debuff5 == null || debuff5.duration != 3
                || debuff6 == null || debuff6.duration != 3
                || speffect1.length == 0 || speffect1[0] != 9
                || speffect4.length == 0 || speffect4[0] != 7
                || speffect6.length == 0 || speffect6[0] != 8
                || speffect7.length == 0 || speffect7[0] != 9
                || speffect17.length == 0 || speffect17[0] != 1) {
            throw new IllegalStateException(checkpoint + " water skill 30..39 status/effect table mismatch"
                    + " buff5=" + (buff5 == null ? "null" : java.util.Arrays.toString(buff5.raw))
                    + " buff6=" + (buff6 == null ? "null" : java.util.Arrays.toString(buff6.raw))
                    + " debuff4=" + (debuff4 == null ? "null" : java.util.Arrays.toString(debuff4.raw))
                    + " debuff5=" + (debuff5 == null ? "null" : java.util.Arrays.toString(debuff5.raw))
                    + " debuff6=" + (debuff6 == null ? "null" : java.util.Arrays.toString(debuff6.raw))
                    + " speffect1=" + java.util.Arrays.toString(speffect1)
                    + " speffect4=" + java.util.Arrays.toString(speffect4)
                    + " speffect6=" + java.util.Arrays.toString(speffect6)
                    + " speffect7=" + java.util.Arrays.toString(speffect7)
                    + " speffect17=" + java.util.Arrays.toString(speffect17));
        }
    }

    private static void assertWaterSkillSourceRow(String checkpoint, int skillId,
                                                  int nameTextId, int descriptionTextId,
                                                  int powerPercent, int tier, int ppMax,
                                                  int effectMode, int effectId,
                                                  int chanceOrParam, int targetSide,
                                                  byte[] expectedEffect) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        if (row == null
                || row.elementFamily != 3
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
            case 30:
                return "Bong bang";
            case 31:
                return "Bang lao";
            case 32:
                return "Tuyet anh";
            case 33:
                return "Thuy tri";
            case 34:
                return "Thuat cau nguyen";
            case 35:
                return "Thuy bich";
            case 36:
                return "Bao Phong Tuyet";
            case 37:
                return "La chan gia tuyet";
            case 38:
                return "Bang Phong Ham Tinh";
            case 39:
                return "Ray lanh";
            default:
                return "Skill " + skillId;
        }
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
                                       WaterSkillCaseResult result) throws java.io.IOException {
        String name = "battle_skill" + result.skillId + "_ray_lanh_timeline_result.png";
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

    private static final class WaterSkillCaseResult {
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
        final String effectRow;

        WaterSkillCaseResult(int skillId, String name,
                             int beforePlayerHp, int afterPlayerHp,
                             int beforeEnemyHp, int afterEnemyHp,
                             int beforePp, int afterPp,
                             int damage,
                             int playerBuffId, boolean playerBuffActive, int playerBuffDuration,
                             int enemyDebuffId, boolean enemyDebuffActive, int enemyDebuffDuration,
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
                    + " effect.mid=" + effectRow
                    + "\n";
        }
    }
}
