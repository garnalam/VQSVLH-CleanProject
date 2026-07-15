import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

final class ShadowSkill implements Skill {
    static final ShadowSkill INSTANCE = new ShadowSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL50_59_SHADOW_CLOSEOUT_SUITE = {
            "battle_shadow_skills_50_59_closeout"
    };

    private ShadowSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_shadow_skills_50_59_closeout".equals(suite)) {
            return SKILL50_59_SHADOW_CLOSEOUT_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runShadowSkills50To59CloseoutSmokeIfNeeded(checkpoint, outPath);
    }

    private static boolean runShadowSkills50To59CloseoutSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_shadow_skills_50_59_closeout".equals(checkpoint)) {
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

            assertShadowSkills50To59SourceRows(checkpoint);
            ShadowSkillCaseResult skill50 = runShadowSkillCase(
                    50, "anh_thu", -1, false, dir);
            ShadowSkillCaseResult skill51 = runShadowSkillCase(
                    51, "chu_oan", 7, false, dir);
            ShadowSkillCaseResult skill52 = runShadowSkillCase(
                    52, "quy_doc", -1, true, dir);
            ShadowSkillCaseResult skill53 = runShadowSkillCase(
                    53, "con_ac_mong", -1, false, dir);
            ShadowSkillCaseResult skill54 = runShadowSkillCase(
                    54, "mi_anh", 8, false, dir);
            ShadowSkillCaseResult skill55 = runShadowSkillCase(
                    55, "hon_loan", 9, false, dir);
            ShadowSkillCaseResult skill56 = runShadowSkillCase(
                    56, "doc_anh_thu", -1, false, dir);
            ShadowSkillCaseResult skill57 = runShadowSkillCase(
                    57, "chu_phuoc_quy_lao", 7, false, dir);
            ShadowSkillCaseResult skill58 = runShadowSkillCase(
                    58, "quy_doc_tin_nguong", -1, true, dir);
            ShadowSkillCaseResult skill59 = runShadowSkillCase(
                    59, "loi_nguyen_cuoi_cung", -1, false, dir);

            ShadowSkillCaseResult[] results = new ShadowSkillCaseResult[]{
                    skill50, skill51, skill52, skill53, skill54,
                    skill55, skill56, skill57, skill58, skill59
            };
            StringBuilder debug = new StringBuilder();
            debug.append("checkpoint=").append(checkpoint).append('\n');
            debug.append("source=aq.c[1][50..59] + effect.mid[50..59] from S60 merged tables\n");
            debug.append("status=PORTED/PARTIAL runtime source row/effect/HP/PP/status verified; pixel-perfect pending\n");
            for (ShadowSkillCaseResult result : results) {
                debug.append(result.describe());
            }
            Files.write(new java.io.File(dir,
                            "battle_shadow_skills_50_59_closeout_debug.txt").toPath(),
                    debug.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            copySummaryPng(out, dir, skill59);

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill50Damage=" + skill50.damage
                    + " skill51Damage=" + skill51.damage + "/debuff7:" + skill51.enemyDebuffActive
                    + " skill52Damage=" + skill52.damage + "/heal=" + skill52.playerHeal
                    + " skill53Damage=" + skill53.damage
                    + " skill54Debuff8=" + skill54.enemyDebuffActive
                    + " skill55Debuff9=" + skill55.enemyDebuffActive
                    + " skill56Damage=" + skill56.damage
                    + " skill57Damage=" + skill57.damage + "/debuff7:" + skill57.enemyDebuffActive
                    + " skill58Damage=" + skill58.damage + "/heal=" + skill58.playerHeal
                    + " skill59Damage=" + skill59.damage
                    + " images=skill50..skill59 before/effect/result");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static ShadowSkillCaseResult runShadowSkillCase(int skillId, String slug,
                                                            int expectedEnemyDebuffId,
                                                            boolean forceLeechPass,
                                                            java.io.File dir)
            throws java.io.IOException {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        if (row == null) {
            throw new IllegalStateException("Missing skill row " + skillId);
        }
        if (forceLeechPass) {
            runtime.debugSetPlayerAttackForSmoke(s, 80);
            runtime.debugSetPlayerHpForSmoke(s, Math.max(1, s.battlePlayerMaxHp - 40));
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        String prefix = "battle_skill" + skillId + "_" + slug + "_timeline_";
        writeScenePng(s, new java.io.File(dir, prefix + "before.png"));

        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        runtime.debugSetNextDamageDebuffRollForSmoke(0);
        if (forceLeechPass) {
            runtime.debugSetNextLeechRollForSmoke(0);
        }
        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 160);
        tickUntilBattleP7Phase(s, 1, 260);
        for (int i = 0; i < 56
                && !s.battleP7ActorEffectVisible
                && !s.battleP7SpecialVisible; i++) {
            s.tick();
        }
        assertShadowSkillFirstVisual(s, runtime, skillId, beforeEnemyHp, beforePp);
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
                        + " trace=" + tailTrace(s, 160));
            }
        }

        int guard = 0;
        while ("P7".equals(s.battleStateName) && s.battleP7Phase < 3 && guard++ < 820) {
            s.tick();
        }

        boolean enemyDebuffActive = expectedEnemyDebuffId >= 0
                && runtime.debugEnemyHasDebuffForSmoke(expectedEnemyDebuffId);
        int enemyDebuffDuration = expectedEnemyDebuffId >= 0
                ? runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId) : 0;
        if (expectedEnemyDebuffId >= 0 && !enemyDebuffActive) {
            throw new IllegalStateException("Expected skill" + skillId + " enemy debuff"
                    + " id=" + expectedEnemyDebuffId
                    + " duration=" + enemyDebuffDuration
                    + " trace=" + tailTrace(s, 160));
        }
        int playerHeal = s.battlePlayerHp - beforePlayerHp;
        if (forceLeechPass && playerHeal <= 0) {
            throw new IllegalStateException("Expected skill" + skillId + " forced leech heal"
                    + " beforePlayerHp=" + beforePlayerHp
                    + " afterPlayerHp=" + s.battlePlayerHp
                    + " damage=" + damage
                    + " trace=" + tailTrace(s, 180));
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
                    + " trace=" + tailTrace(s, 120));
        }
        writeScenePng(s, new java.io.File(dir, prefix + "result.png"));

        return new ShadowSkillCaseResult(skillId, sourceSkillName(skillId),
                beforePlayerHp, s.battlePlayerHp,
                beforeEnemyHp, s.battleEnemyHp,
                beforePp, runtime.debugPlayerSkillPpForSmoke(0),
                damage, playerHeal,
                expectedEnemyDebuffId, enemyDebuffActive,
                enemyDebuffDuration,
                java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)));
    }

    private static void assertShadowSkillFirstVisual(VqsvIntroDemo.Scene s,
                                                       SourceBattleRuntime runtime,
                                                       int skillId,
                                                       int beforeEnemyHp,
                                                       int beforePp) {
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        boolean expectedSpecial = effect.length >= 2 && effect[1] == 1;
        boolean expectedPlayerSide = false;
        boolean ok;
        if (expectedSpecial) {
            short[] speffect = VqsvBattleAnimationTables.instance().speffectRow(effect[2]);
            int expectedType = speffect.length == 0 ? -1 : speffect[0];
            ok = s.battleP7SpecialVisible
                    && s.battleP7SpecialType == expectedType
                    && s.battleP7SpecialOnPlayerSide == expectedPlayerSide;
        } else {
            ok = s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSpriteId == 267
                    && s.battleP7ActorEffectState == effect[3]
                    && s.battleP7ActorEffectOnPlayerSide == expectedPlayerSide;
        }
        if (!ok
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1
                || !traceContains(s, "battle P7 source n() skill=" + skillId)) {
            throw new IllegalStateException("Expected shadow skill" + skillId + " first visual from effect.mid"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " specialType=" + s.battleP7SpecialType
                    + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 140));
        }
    }

    private static void assertShadowSkills50To59SourceRows(String checkpoint) {
        assertShadowSkillSourceRow(checkpoint, 50, 167, 579, 100, 0, 45, 0, -1, -1, 0,
                new byte[]{0, 0, 25, 0, 5, -1, 0, 0, 1, 9, 0, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 51, 168, 580, 80, 0, 45, 2, 7, 20, 0,
                new byte[]{0, 0, 25, 1, 4, -1, 0,
                        0, 1, 8, 0, -1, -1, 0,
                        0, 1, 11, 0, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 52, 169, 581, 80, 0, 45, 0, -1, 5, 0,
                new byte[]{0, 0, 25, 2, 7, -1, 0,
                        0, 1, 8, 0, -1, -1, 0,
                        1, 0, 25, 3, 4, -1, 0,
                        1, 1, 10, 0, -1, -1, 1});
        assertShadowSkillSourceRow(checkpoint, 53, 170, 582, 200, 1, 30, 0, -1, 200, 0,
                new byte[]{0, 0, 25, 4, 2, -1, 0, 0, 1, 9, 0, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 54, 171, 583, 0, 1, 10, 2, 8, 40, 0,
                new byte[]{0, 1, 0, 0, -1, -1, 0, 0, 0, 25, 5, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 55, 172, 584, 0, 1, 10, 2, 9, -1, 0,
                new byte[]{0, 1, 12, 0, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 56, 173, 585, 150, 2, 30, 0, -1, -1, 0,
                new byte[]{0, 0, 25, 6, 3, -1, 0,
                        0, 1, 8, 0, -1, -1, 0,
                        0, 1, 9, 0, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 57, 174, 586, 120, 2, 30, 2, 7, 20, 0,
                new byte[]{0, 0, 25, 7, 3, -1, 0,
                        0, 1, 14, 0, -1, -1, 0,
                        0, 1, 11, 0, -1, -1, 0});
        assertShadowSkillSourceRow(checkpoint, 58, 175, 587, 100, 3, 15, 0, -1, 8, 0,
                new byte[]{0, 0, 25, 8, -1, -1, 0,
                        0, 1, 13, 0, -1, -1, 0,
                        1, 0, 25, 9, 5, -1, 0,
                        1, 1, 10, 0, -1, -1, 1});
        assertShadowSkillSourceRow(checkpoint, 59, 176, 588, 250, 3, 15, 0, -1, 250, 0,
                new byte[]{0, 0, 25, 10, -1, -1, 0});

        BattleDebuffRow debuff7 = VqsvBattleTables.instance().debuff(7);
        BattleDebuffRow debuff8 = VqsvBattleTables.instance().debuff(8);
        BattleDebuffRow debuff9 = VqsvBattleTables.instance().debuff(9);
        short[] speffect0 = VqsvBattleAnimationTables.instance().speffectRow(0);
        short[] speffect8 = VqsvBattleAnimationTables.instance().speffectRow(8);
        short[] speffect9 = VqsvBattleAnimationTables.instance().speffectRow(9);
        short[] speffect10 = VqsvBattleAnimationTables.instance().speffectRow(10);
        short[] speffect11 = VqsvBattleAnimationTables.instance().speffectRow(11);
        short[] speffect12 = VqsvBattleAnimationTables.instance().speffectRow(12);
        short[] speffect13 = VqsvBattleAnimationTables.instance().speffectRow(13);
        short[] speffect14 = VqsvBattleAnimationTables.instance().speffectRow(14);
        if (debuff7 == null || debuff7.duration != 3
                || debuff8 == null || debuff8.duration != 4
                || debuff9 == null || debuff9.duration != 1
                || speffect0.length == 0 || speffect0[0] != 9
                || speffect8.length == 0 || speffect8[0] != 9
                || speffect9.length == 0 || speffect9[0] != 9
                || speffect10.length == 0 || speffect10[0] != 9
                || speffect11.length == 0 || speffect11[0] != 1
                || speffect12.length == 0 || speffect12[0] != 12
                || speffect13.length == 0 || speffect13[0] != 1
                || speffect14.length == 0 || speffect14[0] != 12) {
            throw new IllegalStateException(checkpoint + " shadow skill 50..59 status/effect table mismatch"
                    + " debuff7=" + (debuff7 == null ? "null" : java.util.Arrays.toString(debuff7.raw))
                    + " debuff8=" + (debuff8 == null ? "null" : java.util.Arrays.toString(debuff8.raw))
                    + " debuff9=" + (debuff9 == null ? "null" : java.util.Arrays.toString(debuff9.raw))
                    + " speffect0=" + java.util.Arrays.toString(speffect0)
                    + " speffect8=" + java.util.Arrays.toString(speffect8)
                    + " speffect9=" + java.util.Arrays.toString(speffect9)
                    + " speffect10=" + java.util.Arrays.toString(speffect10)
                    + " speffect11=" + java.util.Arrays.toString(speffect11)
                    + " speffect12=" + java.util.Arrays.toString(speffect12)
                    + " speffect13=" + java.util.Arrays.toString(speffect13)
                    + " speffect14=" + java.util.Arrays.toString(speffect14));
        }
    }

    private static void assertShadowSkillSourceRow(String checkpoint, int skillId,
                                                     int nameTextId, int descriptionTextId,
                                                     int powerPercent, int tier, int ppMax,
                                                     int effectMode, int effectId,
                                                     int chanceOrParam, int targetSide,
                                                     byte[] expectedEffect) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        if (row == null
                || row.elementFamily != 5
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
            case 50:
                return "Anh thu";
            case 51:
                return "Chu oan";
            case 52:
                return "Quy doc";
            case 53:
                return "Con ac mong";
            case 54:
                return "Mi anh";
            case 55:
                return "Hon loan";
            case 56:
                return "Doc anh thu";
            case 57:
                return "Chu Phuoc Quy Lao";
            case 58:
                return "Quy doc tin nguong";
            case 59:
                return "Loi nguyen cuoi cung";
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
                                       ShadowSkillCaseResult result) throws java.io.IOException {
        String name = "battle_skill" + result.skillId + "_loi_nguyen_cuoi_cung_timeline_result.png";
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

    private static final class ShadowSkillCaseResult {
        final int skillId;
        final String name;
        final int beforePlayerHp;
        final int afterPlayerHp;
        final int beforeEnemyHp;
        final int afterEnemyHp;
        final int beforePp;
        final int afterPp;
        final int damage;
        final int playerHeal;
        final int enemyDebuffId;
        final boolean enemyDebuffActive;
        final int enemyDebuffDuration;
        final String effectRow;

        ShadowSkillCaseResult(int skillId, String name,
                                int beforePlayerHp, int afterPlayerHp,
                                int beforeEnemyHp, int afterEnemyHp,
                                int beforePp, int afterPp,
                                int damage, int playerHeal,
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
            this.playerHeal = playerHeal;
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
                    + " playerHeal=" + playerHeal
                    + " enemyDebuff=" + enemyDebuffId + ":" + enemyDebuffActive
                    + "/" + enemyDebuffDuration
                    + " effect.mid=" + effectRow
                    + "\n";
        }
    }
}

