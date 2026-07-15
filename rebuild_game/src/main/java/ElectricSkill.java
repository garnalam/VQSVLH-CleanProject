import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

final class ElectricSkill implements Skill {
    static final ElectricSkill INSTANCE = new ElectricSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL40_49_ELECTRIC_CLOSEOUT_SUITE = {
            "battle_electric_skills_40_49_closeout"
    };

    private ElectricSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_electric_skills_40_49_closeout".equals(suite)) {
            return SKILL40_49_ELECTRIC_CLOSEOUT_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runElectricSkills40To49CloseoutSmokeIfNeeded(checkpoint, outPath);
    }

    private static boolean runElectricSkills40To49CloseoutSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_electric_skills_40_49_closeout".equals(checkpoint)) {
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

            assertElectricSkills40To49SourceRows(checkpoint);
            ElectricSkillCaseResult skill40 = runElectricSkillCase(
                    40, "dien_giat", -1, -1, dir);
            ElectricSkillCaseResult skill41 = runElectricSkillCase(
                    41, "loi_thiem", -1, 10, dir);
            ElectricSkillCaseResult skill42 = runElectricSkillCase(
                    42, "nap_dien", 7, -1, dir);
            ElectricSkillCaseResult skill43 = runElectricSkillCase(
                    43, "song_dien_tu", -1, -1, dir);
            ElectricSkillCaseResult skill44 = runElectricSkillCase(
                    44, "doat_menh_cao_ap", 8, -1, dir);
            ElectricSkillCaseResult skill45 = runElectricSkillCase(
                    45, "dien_nang_chuyen_doi", 9, -1, dir);
            ElectricSkillCaseResult skill46 = runElectricSkillCase(
                    46, "tia_lua_dien", -1, -1, dir);
            ElectricSkillCaseResult skill47 = runElectricSkillCase(
                    47, "cham_sam_sat", -1, 10, dir);
            ElectricSkillCaseResult skill48 = runElectricSkillCase(
                    48, "dien_quang_thach_hoa", 7, -1, dir);
            ElectricSkillCaseResult skill49 = runElectricSkillCase(
                    49, "cam_ung_dien_tu", -1, -1, dir);

            ElectricSkillCaseResult[] results = new ElectricSkillCaseResult[]{
                    skill40, skill41, skill42, skill43, skill44,
                    skill45, skill46, skill47, skill48, skill49
            };
            StringBuilder debug = new StringBuilder();
            debug.append("checkpoint=").append(checkpoint).append('\n');
            debug.append("source=aq.c[1][40..49] + effect.mid[40..49] from S60 merged tables\n");
            debug.append("status=PORTED/PARTIAL runtime source row/effect/HP/PP/status verified; pixel-perfect pending\n");
            for (ElectricSkillCaseResult result : results) {
                debug.append(result.describe());
            }
            Files.write(new java.io.File(dir,
                            "battle_electric_skills_40_49_closeout_debug.txt").toPath(),
                    debug.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            copySummaryPng(out, dir, skill49);

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill40Damage=" + skill40.damage
                    + " skill41Damage=" + skill41.damage + "/debuff10:" + skill41.enemyDebuffActive
                    + " skill42Damage=" + skill42.damage + "/buff7:" + skill42.playerBuffActive
                    + " skill43Damage=" + skill43.damage
                    + " skill44=buff8:" + skill44.playerBuffActive
                    + " skill45=buff9:" + skill45.playerBuffActive
                    + " skill46Damage=" + skill46.damage
                    + " skill47Damage=" + skill47.damage + "/debuff10:" + skill47.enemyDebuffActive
                    + " skill48Damage=" + skill48.damage + "/buff7:" + skill48.playerBuffActive
                    + " skill49Damage=" + skill49.damage
                    + " images=skill40..skill49 before/effect/result");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static ElectricSkillCaseResult runElectricSkillCase(int skillId, String slug,
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
        tickUntilBattleP7Phase(s, 1, 260);
        for (int i = 0; i < 56
                && !s.battleP7ActorEffectVisible
                && !s.battleP7SpecialVisible; i++) {
            s.tick();
        }
        assertElectricSkillFirstVisual(s, runtime, skillId, beforeEnemyHp, beforePp);
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
        for (int i = 0; i < 20; i++) {
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
                    + " trace=" + tailTrace(s, 160));
        }
        if (expectedEnemyDebuffId >= 0 && !enemyDebuffActive) {
            throw new IllegalStateException("Expected skill" + skillId + " enemy debuff"
                    + " id=" + expectedEnemyDebuffId
                    + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId)
                    + " trace=" + tailTrace(s, 160));
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

        return new ElectricSkillCaseResult(skillId, sourceSkillName(skillId),
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

    private static void assertElectricSkillFirstVisual(VqsvIntroDemo.Scene s,
                                                       SourceBattleRuntime runtime,
                                                       int skillId,
                                                       int beforeEnemyHp,
                                                       int beforePp) {
        int expectedSprite = skillId == 44 || skillId == 45 ? 299 : 266;
        int expectedState = VqsvBattleAnimationTables.instance().effectRow(skillId)[3];
        boolean expectedPlayerSide = skillId == 44 || skillId == 45;
        boolean ok = s.battleP7ActorEffectVisible
                && s.battleP7ActorEffectSpriteId == expectedSprite
                && s.battleP7ActorEffectState == expectedState
                && s.battleP7ActorEffectOnPlayerSide == expectedPlayerSide;
        if (!ok
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1
                || !traceContains(s, "battle P7 source n() skill=" + skillId)) {
            throw new IllegalStateException("Expected electric skill" + skillId + " first visual from effect.mid"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " specialType=" + s.battleP7SpecialType
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 140));
        }
    }

    private static void assertElectricSkills40To49SourceRows(String checkpoint) {
        assertElectricSkillSourceRow(checkpoint, 40, 157, 569, 100, 0, 45, 0, -1, -1, 0,
                new byte[]{0, 0, 24, 0, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 41, 158, 570, 90, 0, 45, 2, 10, 10, 0,
                new byte[]{0, 0, 24, 1, 8, -1, 0, 0, 1, 4, 0, -1, -1, 1});
        assertElectricSkillSourceRow(checkpoint, 42, 159, 571, 90, 0, 45, 1, 7, 5, 0,
                new byte[]{0, 0, 24, 0, 3, -1, 0,
                        1, 0, 32, 0, 1, -1, 0,
                        1, 1, 1, 0, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 43, 160, 572, 100, 1, 30, 0, -1, -1, 0,
                new byte[]{0, 0, 24, 3, 4, -1, 0, 0, 1, 4, 0, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 44, 161, 573, 0, 1, 10, 1, 8, -1, 1,
                new byte[]{0, 0, 27, 0, 0, -1, 0,
                        0, 1, 19, 0, -1, -1, 0,
                        0, 1, 15, 0, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 45, 162, 574, 0, 1, 10, 1, 9, -1, 1,
                new byte[]{0, 0, 27, 0, 0, -1, 0,
                        0, 1, 19, 0, -1, -1, 0,
                        0, 1, 15, 0, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 46, 163, 575, 150, 2, 30, 0, -1, -1, 0,
                new byte[]{0, 0, 24, 4, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 47, 164, 576, 130, 2, 30, 2, 10, 10, 0,
                new byte[]{0, 0, 24, 5, 10, -1, 0, 0, 1, 4, 0, -1, -1, 1});
        assertElectricSkillSourceRow(checkpoint, 48, 165, 577, 130, 3, 15, 1, 7, 5, 0,
                new byte[]{0, 0, 24, 4, -1, -1, 0,
                        1, 0, 32, 0, 1, -1, 0,
                        1, 1, 9, 0, -1, -1, 0});
        assertElectricSkillSourceRow(checkpoint, 49, 166, 578, 180, 3, 15, 0, -1, -1, 0,
                new byte[]{0, 0, 24, 6, -1, -1, 0});

        BattleBuffRow buff7 = VqsvBattleTables.instance().buff(7);
        BattleBuffRow buff8 = VqsvBattleTables.instance().buff(8);
        BattleBuffRow buff9 = VqsvBattleTables.instance().buff(9);
        BattleDebuffRow debuff10 = VqsvBattleTables.instance().debuff(10);
        short[] speffect1 = VqsvBattleAnimationTables.instance().speffectRow(1);
        short[] speffect4 = VqsvBattleAnimationTables.instance().speffectRow(4);
        short[] speffect9 = VqsvBattleAnimationTables.instance().speffectRow(9);
        short[] speffect15 = VqsvBattleAnimationTables.instance().speffectRow(15);
        short[] speffect19 = VqsvBattleAnimationTables.instance().speffectRow(19);
        if (buff7 == null || buff7.duration != 2
                || buff8 == null || buff8.duration != 4 || buff8.raw[3] != 30
                || buff9 == null || buff9.duration != 3 || buff9.raw[3] != 50
                || debuff10 == null || debuff10.duration != 4
                || speffect1.length == 0 || speffect1[0] != 9
                || speffect4.length == 0 || speffect4[0] != 7
                || speffect9.length == 0 || speffect9[0] != 9
                || speffect15.length == 0 || speffect15[0] != 1
                || speffect19.length == 0 || speffect19[0] != 9) {
            throw new IllegalStateException(checkpoint + " electric skill 40..49 status/effect table mismatch"
                    + " buff7=" + (buff7 == null ? "null" : java.util.Arrays.toString(buff7.raw))
                    + " buff8=" + (buff8 == null ? "null" : java.util.Arrays.toString(buff8.raw))
                    + " buff9=" + (buff9 == null ? "null" : java.util.Arrays.toString(buff9.raw))
                    + " debuff10=" + (debuff10 == null ? "null" : java.util.Arrays.toString(debuff10.raw))
                    + " speffect1=" + java.util.Arrays.toString(speffect1)
                    + " speffect4=" + java.util.Arrays.toString(speffect4)
                    + " speffect9=" + java.util.Arrays.toString(speffect9)
                    + " speffect15=" + java.util.Arrays.toString(speffect15)
                    + " speffect19=" + java.util.Arrays.toString(speffect19));
        }
    }

    private static void assertElectricSkillSourceRow(String checkpoint, int skillId,
                                                     int nameTextId, int descriptionTextId,
                                                     int powerPercent, int tier, int ppMax,
                                                     int effectMode, int effectId,
                                                     int chanceOrParam, int targetSide,
                                                     byte[] expectedEffect) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        if (row == null
                || row.elementFamily != 4
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
            case 40:
                return "Dien giat";
            case 41:
                return "Loi thiem";
            case 42:
                return "Nap dien";
            case 43:
                return "Song dien tu";
            case 44:
                return "Doat menh cao ap";
            case 45:
                return "Dien nang chuyen doi";
            case 46:
                return "Tia lua dien";
            case 47:
                return "Cham sam sat";
            case 48:
                return "Dien quang thach hoa";
            case 49:
                return "Cam ung dien tu";
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
                                       ElectricSkillCaseResult result) throws java.io.IOException {
        String name = "battle_skill" + result.skillId + "_cam_ung_dien_tu_timeline_result.png";
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

    private static final class ElectricSkillCaseResult {
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

        ElectricSkillCaseResult(int skillId, String name,
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
