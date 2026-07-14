import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

final class WoodSkill implements Skill {
    static final WoodSkill INSTANCE = new WoodSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL10_DIRECT_TIMELINE_SUITE = {
            "battle_skill10_direct_timeline"
    };
    private static final String[] SKILL16_DIRECT_TIMELINE_SUITE = {
            "battle_skill16_direct_timeline"
    };
    private static final String[] SKILL11_QUANG_PHAN_TIMELINE_SUITE = {
            "battle_skill11_quang_phan_timeline"
    };
    private static final String[] SKILL17_DIEP_CHI_AN_HUE_TIMELINE_SUITE = {
            "battle_skill17_diep_chi_an_hue_timeline"
    };
    private static final String[] SKILL12_DANG_PHUOC_TIMELINE_SUITE = {
            "battle_skill12_dang_phuoc_timeline"
    };
    private static final String[] SKILL18_DANG_MAN_TRIEN_NHIEU_TIMELINE_SUITE = {
            "battle_skill18_dang_man_trien_nhieu_timeline"
    };
    private static final String[] SKILL13_THAO_CHUNG_TIMELINE_SUITE = {
            "battle_skill13_thao_chung_timeline"
    };
    private static final String[] SKILL19_QUANG_HOP_HIEU_UNG_TIMELINE_SUITE = {
            "battle_skill19_quang_hop_hieu_ung_timeline"
    };
    private static final String[] SKILL14_DANG_CHI_BICH_LUY_TIMELINE_SUITE = {
            "battle_skill14_dang_chi_bich_luy_timeline"
    };
    private static final String[] SKILL15_THAO_NGUYEN_THUAT_TIMELINE_SUITE = {
            "battle_skill15_thao_nguyen_thuat_timeline"
    };

    private WoodSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_skill10_direct_timeline".equals(suite)) {
            return SKILL10_DIRECT_TIMELINE_SUITE;
        }
        if ("battle_skill16_direct_timeline".equals(suite)) {
            return SKILL16_DIRECT_TIMELINE_SUITE;
        }
        if ("battle_skill11_quang_phan_timeline".equals(suite)) {
            return SKILL11_QUANG_PHAN_TIMELINE_SUITE;
        }
        if ("battle_skill17_diep_chi_an_hue_timeline".equals(suite)) {
            return SKILL17_DIEP_CHI_AN_HUE_TIMELINE_SUITE;
        }
        if ("battle_skill12_dang_phuoc_timeline".equals(suite)) {
            return SKILL12_DANG_PHUOC_TIMELINE_SUITE;
        }
        if ("battle_skill18_dang_man_trien_nhieu_timeline".equals(suite)) {
            return SKILL18_DANG_MAN_TRIEN_NHIEU_TIMELINE_SUITE;
        }
        if ("battle_skill13_thao_chung_timeline".equals(suite)) {
            return SKILL13_THAO_CHUNG_TIMELINE_SUITE;
        }
        if ("battle_skill19_quang_hop_hieu_ung_timeline".equals(suite)) {
            return SKILL19_QUANG_HOP_HIEU_UNG_TIMELINE_SUITE;
        }
        if ("battle_skill14_dang_chi_bich_luy_timeline".equals(suite)) {
            return SKILL14_DANG_CHI_BICH_LUY_TIMELINE_SUITE;
        }
        if ("battle_skill15_thao_nguyen_thuat_timeline".equals(suite)) {
            return SKILL15_THAO_NGUYEN_THUAT_TIMELINE_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runSkill11QuangPhanTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill17DiepChiAnHueTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill12DangPhuocTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill18DangManTrienNhieuTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill13ThaoChungTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill19QuangHopHieuUngTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill14DangChiBichLuyTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill15ThaoNguyenThuatTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkillDirectTimelineSmokeIfNeeded(checkpoint, outPath);
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

    private static boolean traceContainsAll(VqsvIntroDemo.Scene s, String... needles) {
        return VqsvSmokeHarness.traceContainsAll(s, needles);
    }

    private static int latestTraceDamage(VqsvIntroDemo.Scene s, String needle) {
        return VqsvSmokeHarness.latestTraceDamage(s, needle);
    }

    private static void tickUntilTraceContains(VqsvIntroDemo.Scene s, String needle, int maxTicks) {
        VqsvSmokeHarness.tickUntilTraceContains(s, needle, maxTicks);
    }

    private static void assertPhase10AStatusSlots(VqsvIntroDemo.Scene s, boolean playerSide,
                                                  String label, int[] expectedIcons, int[] expectedDurations) {
        VqsvSmokeHarness.assertPhase10AStatusSlots(s, playerSide, label, expectedIcons, expectedDurations);
    }

    private static String tailTrace(VqsvIntroDemo.Scene s, int count) {
        return VqsvSmokeHarness.tailTrace(s, count);
    }

    private static SourceBattleRuntime setupPhase10AStatusBattle(VqsvIntroDemo.Scene s) {
        return VqsvSmokeHarness.setupPhase10AStatusBattle(s);
    }

    private static int[] statusBuff2Skill10ReflectProbe(VqsvIntroDemo.Scene s, boolean applyBuff2,
                                                        int critRoll, int hitRoll,
                                                        boolean forceMissSetup,
                                                        boolean expectReflect) {
        return VqsvSmokeHarness.statusBuff2Skill10ReflectProbe(s, applyBuff2, critRoll,
                hitRoll, forceMissSetup, expectReflect);
    }
    static boolean runSkillDirectTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        int skillId;
        if ("battle_skill10_direct_timeline".equals(checkpoint)) {
            skillId = 10;
        } else if ("battle_skill16_direct_timeline".equals(checkpoint)) {
            skillId = 16;
        } else {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
            assertDirectBaseSourceRows(s, checkpoint, skillId);
            assertDirectBaseP3BeforeConfirm(s, runtime, checkpoint, skillId);

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, directTimelinePngName(skillId, "before")));

            runtime.debugSetNextP7HitRollForSmoke(99);
            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 120);
            for (int i = 0; i < 20 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            assertDirectBaseP7ActorVisible(s, runtime, checkpoint, skillId);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            int actorCursor = s.battleP7ActorEffectCursor;
            writeScenePng(s, new java.io.File(dir, directTimelinePngName(skillId, "actor_u"
                    + directBaseActorEffectId(skillId) + "_start")));

            tickUntilBattleP7Phase(s, 2, 160);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
            assertDirectBaseDamageFrame(s, runtime, checkpoint, skillId, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            boolean critical = s.battleP7DamageCritical;
            String debuffText = s.battleP7DebuffText;
            String missText = s.battleP7MissText;
            writeScenePng(s, new java.io.File(dir, directTimelinePngName(skillId, "damage_frame")));

            int expectedEnemyHp = Math.max(0, s.battleEnemyMaxHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 240) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != directBaseExpectedPpAfterUse(skillId)
                    || damage <= 0
                    || !traceContains(s, "battle P7 source n() skill=" + skillId)
                    || !traceContains(s, "battle P7 actor u.a() start skill=" + skillId)
                    || !traceContains(s, "battle P7 damage frame skill=" + skillId)) {
                throw new IllegalStateException(checkpoint + " expected one-run skill" + skillId + " timeline to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " damage=" + damage
                        + " trace=" + tailTrace(s, 36));
            }
            writeScenePng(s, new java.io.File(dir, directTimelinePngName(skillId, "hp_settled")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=" + skillId + " name=" + directBaseAsciiName(skillId)
                    + " description=" + directBaseAsciiDescription(skillId) + "\n"
                    + "aq.c[1][" + skillId + "]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[" + skillId + "]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)) + "\n"
                    + "actorEffect=" + directBaseActorEffectId(skillId)
                    + " actorSprite=" + directBaseActorSpriteId(skillId)
                    + " actorState=" + directBaseActorState(skillId)
                    + " actorSide=enemy\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp + " cursor=" + actorCursor + "\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " critical=" + critical
                    + " debuffText=" + debuffText
                    + " missText=" + missText + "\n"
                    + "hpSettled hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " expectedEnemyHp=" + expectedEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0) + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 24) + "\n";
            Files.write(new java.io.File(dir, "battle_skill" + skillId + "_direct_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " actorSprite=" + directBaseActorSpriteId(skillId)
                    + " images=before,actor_u" + directBaseActorEffectId(skillId)
                    + "_start,damage_frame,hp_settled");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill11QuangPhanTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill11_quang_phan_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 11);
            assertSkill11QuangPhanSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 11
                    || runtime.debugPlayerSkillPpForSmoke(0) != 45) {
                throw new IllegalStateException(checkpoint + " expected P3 skill11 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            int startHp = Math.max(20, s.battlePlayerMaxHp / 2);
            runtime.debugSetPlayerHpForSmoke(s, startHp);
            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill11QuangPhanPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 1
                    || s.battleP7ActorEffectOnPlayerSide
                    || s.battleEnemyHp != s.battleEnemyMaxHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !traceContains(s, "battle P7 actor u.a() start skill=11")) {
                throw new IllegalStateException(checkpoint + " expected skill11 actor u21 before damage"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 48));
            }
            int actorPlayerHp = s.battlePlayerHp;
            int actorEnemyHp = s.battleEnemyHp;
            writeScenePng(s, new java.io.File(dir, skill11QuangPhanPngName("actor_u21_start")));

            for (int i = 0; i < 240 && !s.battleP7SpecialVisible; i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || !s.battleP7SpecialOnPlayerSide
                    || s.battleP7SpecialType != 9
                    || s.battleP7SpecialAlpha != 120
                    || !traceContains(s, "battle P7 speffect skill=11")
                    || !traceContains(s, "speffect=10")) {
                throw new IllegalStateException(checkpoint + " expected skill11 speffect10/AH9 on attacker side"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " type=" + s.battleP7SpecialType
                        + " alpha=" + s.battleP7SpecialAlpha
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill11QuangPhanPngName("speffect10_type9")));

            tickUntilBattleP7Phase(s, 2, 220);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=11");
            String damageText = s.battleP7DamageText;
            if (!s.battleP7DamageVisible
                    || s.battleP7DamageText.isEmpty()
                    || !s.battleP7DebuffText.isEmpty()
                    || !s.battleP7MissText.isEmpty()
                    || damage <= 0
                    || s.battleEnemyHp != s.battleEnemyMaxHp
                    || s.battlePlayerHp != beforePlayerHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !traceContains(s, "battle P7 damage frame skill=11")
                    || !traceContains(s, "hit=true")
                    || !traceContains(s, "appliedDebuffId=-1")) {
                throw new IllegalStateException(checkpoint + " expected skill11 damage frame before HP/heal settles"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damageText=" + s.battleP7DamageText
                        + " debuffText=" + s.battleP7DebuffText
                        + " missText=" + s.battleP7MissText
                        + " damage=" + damage
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill11QuangPhanPngName("damage_frame")));

            int expectedEnemyHp = Math.max(0, s.battleEnemyMaxHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 260) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp) {
                throw new IllegalStateException(checkpoint + " expected skill11 enemy HP to settle from same-run damage"
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expected=" + expectedEnemyHp
                        + " damage=" + damage
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill11QuangPhanPngName("hp_settled")));

            tickUntilBattleP7Phase(s, 3, 240);
            for (int i = 0; i < 40 && !s.battleP7PostEffectVisible; i++) {
                s.tick();
            }
            int heal = parsePlusText(s.battleP7PostEffectText);
            int expectedPlayerHp = Math.min(s.battlePlayerMaxHp, beforePlayerHp + heal);
            if (!s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || heal <= 0
                    || s.battlePlayerHp != expectedPlayerHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !traceContains(s, "game.d.q postEffect skill=11")
                    || !traceContains(s, "heal=" + heal)
                    || traceContains(s, "buffId=0")) {
                throw new IllegalStateException(checkpoint + " expected skill11 q() heal post effect"
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                        + " postText=" + s.battleP7PostEffectText
                        + " heal=" + heal
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + " expectedHp=" + expectedPlayerHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 84));
            }
            writeScenePng(s, new java.io.File(dir, skill11QuangPhanPngName("post_heal")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(11);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=11 name=Quang Phan description=Thuong ton thap, co the khoi phuc HP.\n"
                    + "aq.c[1][11]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[11]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(11)) + "\n"
                    + "speffect10=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(10)) + "\n"
                    + "actorEffect=21 actorSprite=263 actorState=1 actorSide=enemy\n"
                    + "healSpecial=AH9 attackerSide row=[9,120,218,217,169,0,4,2]\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + actorPlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=44\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " critical=false debuffText= missText=\n"
                    + "hpSettled enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp + "\n"
                    + "postHeal text=+" + heal
                    + " playerHp=" + beforePlayerHp + "->" + s.battlePlayerHp
                    + " sourceFormula=max(1,h.B()*10/100)\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 36) + "\n";
            Files.write(new java.io.File(dir, "battle_skill11_quang_phan_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " heal=" + heal
                    + " special=AH9");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill17DiepChiAnHueTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill17_diep_chi_an_hue_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 17);
            assertSkill17DiepChiAnHueSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 17
                    || runtime.debugPlayerSkillPpForSmoke(0) != 30) {
                throw new IllegalStateException(checkpoint + " expected P3 skill17 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            int startHp = Math.max(20, s.battlePlayerMaxHp / 2);
            runtime.debugSetPlayerHpForSmoke(s, startHp);
            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill17DiepChiAnHuePngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 1
                    || s.battleP7ActorEffectOnPlayerSide
                    || s.battleEnemyHp != s.battleEnemyMaxHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || !traceContains(s, "battle P7 actor u.a() start skill=17")) {
                throw new IllegalStateException(checkpoint + " expected skill17 actor u21 before damage"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 48));
            }
            int actorPlayerHp = s.battlePlayerHp;
            int actorEnemyHp = s.battleEnemyHp;
            writeScenePng(s, new java.io.File(dir, skill17DiepChiAnHuePngName("actor_u21_start")));

            for (int i = 0; i < 240 && !s.battleP7SpecialVisible; i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || !s.battleP7SpecialOnPlayerSide
                    || s.battleP7SpecialType != 9
                    || s.battleP7SpecialAlpha != 120
                    || !traceContains(s, "battle P7 speffect skill=17")
                    || !traceContains(s, "speffect=10")) {
                throw new IllegalStateException(checkpoint + " expected skill17 speffect10/AH9 on attacker side"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " type=" + s.battleP7SpecialType
                        + " alpha=" + s.battleP7SpecialAlpha
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill17DiepChiAnHuePngName("speffect10_type9")));

            tickUntilBattleP7Phase(s, 2, 220);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=17");
            String damageText = s.battleP7DamageText;
            if (!s.battleP7DamageVisible
                    || s.battleP7DamageText.isEmpty()
                    || !s.battleP7DebuffText.isEmpty()
                    || !s.battleP7MissText.isEmpty()
                    || damage <= 0
                    || s.battleEnemyHp != s.battleEnemyMaxHp
                    || s.battlePlayerHp != beforePlayerHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || !traceContains(s, "battle P7 damage frame skill=17")
                    || !traceContains(s, "hit=true")
                    || !traceContains(s, "appliedDebuffId=-1")) {
                throw new IllegalStateException(checkpoint + " expected skill17 damage frame before HP/heal settles"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damageText=" + s.battleP7DamageText
                        + " debuffText=" + s.battleP7DebuffText
                        + " missText=" + s.battleP7MissText
                        + " damage=" + damage
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill17DiepChiAnHuePngName("damage_frame")));

            int expectedEnemyHp = Math.max(0, s.battleEnemyMaxHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 260) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp) {
                throw new IllegalStateException(checkpoint + " expected skill17 enemy HP to settle from same-run damage"
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expected=" + expectedEnemyHp
                        + " damage=" + damage
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill17DiepChiAnHuePngName("hp_settled")));

            tickUntilBattleP7Phase(s, 3, 240);
            for (int i = 0; i < 40 && !s.battleP7PostEffectVisible; i++) {
                s.tick();
            }
            int heal = parsePlusText(s.battleP7PostEffectText);
            int expectedPlayerHp = Math.min(s.battlePlayerMaxHp, beforePlayerHp + heal);
            if (!s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || heal <= 0
                    || s.battlePlayerHp != expectedPlayerHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || !traceContains(s, "game.d.q postEffect skill=17")
                    || !traceContains(s, "heal=" + heal)
                    || !traceContains(s, "skill=17")
                    || traceContains(s, "buffId=0")) {
                throw new IllegalStateException(checkpoint + " expected skill17 q() heal post effect"
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                        + " postText=" + s.battleP7PostEffectText
                        + " heal=" + heal
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + " expectedHp=" + expectedPlayerHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 84));
            }
            writeScenePng(s, new java.io.File(dir, skill17DiepChiAnHuePngName("post_heal")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(17);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=17 name=Diep chi an hue description=Medium Wood damage and stronger attacker heal.\n"
                    + "aq.c[1][17]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[17]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(17)) + "\n"
                    + "speffect10=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(10)) + "\n"
                    + "actorEffect=21 actorSprite=263 actorState=1 actorSide=enemy\n"
                    + "healSpecial=AH9 attackerSide row=[9,120,218,217,169,0,4,2]\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + actorPlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=29\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " critical=false debuffText= missText=\n"
                    + "hpSettled enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp + "\n"
                    + "postHeal text=+" + heal
                    + " playerHp=" + beforePlayerHp + "->" + s.battlePlayerHp
                    + " sourceFormula=max(1,h.B()*40/100)\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 36) + "\n";
            Files.write(new java.io.File(dir, "battle_skill17_diep_chi_an_hue_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " heal=" + heal
                    + " special=AH9");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill12DangPhuocTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill12_dang_phuoc_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 12);
            assertSkill12DangPhuocSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 12
                    || runtime.debugPlayerSkillPpForSmoke(0) != 45) {
                throw new IllegalStateException(checkpoint + " expected P3 skill12 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 0
                    || s.battleP7ActorEffectOnPlayerSide
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !traceContains(s, "battle P7 source n() skill=12")
                    || !traceContains(s, "battle P7 actor u.a() start skill=12")
                    || !traceContains(s, "id=21")
                    || !traceContains(s, "param=0")) {
                throw new IllegalStateException(checkpoint + " expected skill12 actor u21/state0 before damage"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 56));
            }
            int actorEnemyHp = s.battleEnemyHp;
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("actor_u21_start")));

            for (int i = 0; i < 240 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 8); i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || s.battleP7SpecialOnPlayerSide
                    || s.battleP7SpecialType != 8
                    || s.battleP7SpecialRow.length < 9
                    || s.battleEnemyHp != beforeEnemyHp
                    || !traceContains(s, "battle P7 speffect skill=12")
                    || !traceContains(s, "speffect=6")) {
                throw new IllegalStateException(checkpoint + " expected skill12 speffect6/AH8 on target side"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " type=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " trace=" + tailTrace(s, 72));
            }
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("speffect6_type8")));

            tickUntilBattleP7Phase(s, 2, 260);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=12");
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            if (!s.battleP7DamageVisible
                    || s.battleP7DamageText.isEmpty()
                    || s.battleP7DebuffText.isEmpty()
                    || !s.battleP7MissText.isEmpty()
                    || damage <= 0
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 3
                    || runtime.debugEnemyDebuffValueForSmoke(2) != 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(2) != 12
                    || !traceContains(s, "battle P7 damage frame skill=12")
                    || !traceContains(s, "hit=true")
                    || !traceContains(s, "appliedDebuffId=2")) {
                throw new IllegalStateException(checkpoint + " expected skill12 damage frame with debuff2"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damageText=" + s.battleP7DamageText
                        + " debuffText=" + s.battleP7DebuffText
                        + " missText=" + s.battleP7MissText
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " hasDebuff2=" + runtime.debugEnemyHasDebuffForSmoke(2)
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " value=" + runtime.debugEnemyDebuffValueForSmoke(2)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(2)
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, false, "skill12 debuff2 damage frame",
                    new int[]{3}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 280) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 3
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(2) != 12) {
                throw new IllegalStateException(checkpoint + " expected skill12 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(2)
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill12 debuff2 active after HP settled",
                    new int[]{3}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=2", 800);
            for (int i = 0; i < 180 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 8); i++) {
                s.tick();
            }
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 2
                    || !s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 8
                    || !traceContainsAll(s, "battle P12 active queue visual",
                    "bank=1", "debuff=2", "speffect=6")) {
                throw new IllegalStateException(checkpoint + " expected skill12 debuff2 P12 body visual"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("p12_body_visual_speffect6_type8")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=2", 800);
            if (s.battleEnemyHp != hpBeforeTick
                    || !runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(2) != 0
                    || s.battleEnemyStatusCount != 1) {
                throw new IllegalStateException(checkpoint + " expected debuff2 tick to be HP/stat no-op"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " value=" + runtime.debugEnemyDebuffValueForSmoke(2)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill12 debuff2 after noop tick",
                    new int[]{3}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("tick_noop_duration2")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 2);
            int durationAfterSecondTick = runtime.debugEnemyDebuffDurationForSmoke(2);
            runtime.debugTickEnemySourceDebuffForSmoke(s, 2);
            if (runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 0
                    || durationAfterSecondTick != 1
                    || s.battleEnemyStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected debuff2 to expire after three turns"
                        + " active=" + runtime.debugEnemyHasDebuffForSmoke(2)
                        + " durationAfterSecondTick=" + durationAfterSecondTick
                        + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            writeScenePng(s, new java.io.File(dir, skill12DangPhuocPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(12);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(2);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=12 name=Dang Phuoc description=Low Wood damage plus Quan Quanh bind for 3 turns.\n"
                    + "aq.c[1][12]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[12]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(12)) + "\n"
                    + "speffect6=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(6)) + "\n"
                    + "aq.c[7][2]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "actorEffect=21 actorSprite=263 actorState=0 actorSide=enemy\n"
                    + "special=speffect6 AH8 targetSide\n"
                    + "debuff2=Quan Quanh duration=3 value=0 icon=3 durationCell=137\n"
                    + "logic=damage powerPercent 50; apply debuff2; blocks item/pet/run while active; no HP tick; duration decrements.\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=44\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " critical=false missText=\n"
                    + "hpSettled enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " debuffDuration=3\n"
                    + "tick1 hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                    + " duration=2 noHpDelta=true\n"
                    + "expiry durationAfterSecondTick=" + durationAfterSecondTick
                    + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir, "battle_skill12_dang_phuoc_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuff2Expired=" + !runtime.debugEnemyHasDebuffForSmoke(2)
                    + " special=AH8");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill18DangManTrienNhieuTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill18_dang_man_trien_nhieu_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 18);
            assertSkill18DangManTrienNhieuSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 18
                    || runtime.debugPlayerSkillPpForSmoke(0) != 15) {
                throw new IllegalStateException(checkpoint + " expected P3 skill18 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 0
                    || s.battleP7ActorEffectOnPlayerSide
                    || s.battleP7SpecialVisible
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || !traceContains(s, "battle P7 source n() skill=18")
                    || !traceContains(s, "battle P7 actor u.a() start skill=18")
                    || !traceContains(s, "id=21")
                    || !traceContains(s, "param=0")
                    || traceContains(s, "battle P7 speffect skill=18")) {
                throw new IllegalStateException(checkpoint + " expected skill18 actor u21/state0 one-chunk before damage"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 56));
            }
            int actorEnemyHp = s.battleEnemyHp;
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("actor_u21_start")));

            tickUntilBattleP7Phase(s, 2, 260);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=18");
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            if (!s.battleP7DamageVisible
                    || s.battleP7DamageText.isEmpty()
                    || s.battleP7DebuffText.isEmpty()
                    || !s.battleP7MissText.isEmpty()
                    || damage <= 0
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || !runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 3
                    || runtime.debugEnemyDebuffValueForSmoke(2) != 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(2) != 18
                    || !traceContains(s, "battle P7 damage frame skill=18")
                    || !traceContains(s, "hit=true")
                    || !traceContains(s, "appliedDebuffId=2")) {
                throw new IllegalStateException(checkpoint + " expected skill18 damage frame with debuff2"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damageText=" + s.battleP7DamageText
                        + " debuffText=" + s.battleP7DebuffText
                        + " missText=" + s.battleP7MissText
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " hasDebuff2=" + runtime.debugEnemyHasDebuffForSmoke(2)
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " value=" + runtime.debugEnemyDebuffValueForSmoke(2)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(2)
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, false, "skill18 debuff2 damage frame",
                    new int[]{3}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 280) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || !runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 3
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(2) != 18) {
                throw new IllegalStateException(checkpoint + " expected skill18 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(2)
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill18 debuff2 active after HP settled",
                    new int[]{3}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=2", 800);
            for (int i = 0; i < 180 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 8); i++) {
                s.tick();
            }
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 2
                    || !s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 8
                    || !traceContainsAll(s, "battle P12 active queue visual",
                    "bank=1", "debuff=2", "speffect=6")) {
                throw new IllegalStateException(checkpoint + " expected skill18 debuff2 P12 body visual"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("p12_body_visual_speffect6_type8")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=2", 800);
            if (s.battleEnemyHp != hpBeforeTick
                    || !runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(2) != 0
                    || s.battleEnemyStatusCount != 1) {
                throw new IllegalStateException(checkpoint + " expected skill18 debuff2 tick to be HP/stat no-op"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " value=" + runtime.debugEnemyDebuffValueForSmoke(2)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill18 debuff2 after noop tick",
                    new int[]{3}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("tick_noop_duration2")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 2);
            int durationAfterSecondTick = runtime.debugEnemyDebuffDurationForSmoke(2);
            runtime.debugTickEnemySourceDebuffForSmoke(s, 2);
            if (runtime.debugEnemyHasDebuffForSmoke(2)
                    || runtime.debugEnemyDebuffDurationForSmoke(2) != 0
                    || durationAfterSecondTick != 1
                    || s.battleEnemyStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected skill18 debuff2 to expire after three turns"
                        + " active=" + runtime.debugEnemyHasDebuffForSmoke(2)
                        + " durationAfterSecondTick=" + durationAfterSecondTick
                        + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            writeScenePng(s, new java.io.File(dir, skill18DangManTrienNhieuPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(18);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(2);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=18 name=Dang man trien nhieu description=High Wood damage plus Quan Quanh bind for 3 turns.\n"
                    + "aq.c[1][18]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[18]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(18)) + "\n"
                    + "speffect6=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(6)) + "\n"
                    + "aq.c[7][2]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "actorEffect=21 actorSprite=263 actorState=0 actorSide=enemy\n"
                    + "p7Special=none; activeQueueSpecial=speffect6 AH8 targetSide\n"
                    + "debuff2=Quan Quanh duration=3 value=0 icon=3 durationCell=137\n"
                    + "logic=damage powerPercent 150; apply debuff2; blocks item/pet/run while active; no HP tick; duration decrements.\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=14\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " critical=false missText=\n"
                    + "hpSettled enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " debuffDuration=3\n"
                    + "tick1 hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                    + " duration=2 noHpDelta=true\n"
                    + "expiry durationAfterSecondTick=" + durationAfterSecondTick
                    + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(2)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir, "battle_skill18_dang_man_trien_nhieu_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuff2Expired=" + !runtime.debugEnemyHasDebuffForSmoke(2)
                    + " p7Special=none activeQueueSpecial=AH8");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill13ThaoChungTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill13_thao_chung_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 13);
            assertSkill13ThaoChungSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 13
                    || runtime.debugPlayerSkillPpForSmoke(0) != 30) {
                throw new IllegalStateException(checkpoint + " expected P3 skill13 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 0
                    || s.battleP7ActorEffectOnPlayerSide
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || !traceContains(s, "battle P7 source n() skill=13")
                    || !traceContains(s, "battle P7 actor u.a() start skill=13")
                    || !traceContains(s, "id=21")
                    || !traceContains(s, "param=0")) {
                throw new IllegalStateException(checkpoint + " expected skill13 actor u21/state0 before damage"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 56));
            }
            int actorEnemyHp = s.battleEnemyHp;
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("actor_u21_start")));

            tickUntilBattleP7Phase(s, 2, 220);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=13");
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            int storedRaw = runtime.debugEnemyDebuffValueForSmoke(3);
            if (!s.battleP7DamageVisible
                    || s.battleP7DamageText.isEmpty()
                    || s.battleP7DebuffText.isEmpty()
                    || !s.battleP7MissText.isEmpty()
                    || damage <= 0
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 3
                    || storedRaw <= 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(3) != 13
                    || !traceContains(s, "battle P7 damage frame skill=13")
                    || !traceContains(s, "hit=true")
                    || !traceContains(s, "appliedDebuffId=3")) {
                throw new IllegalStateException(checkpoint + " expected skill13 damage frame with debuff3"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damageText=" + s.battleP7DamageText
                        + " debuffText=" + s.battleP7DebuffText
                        + " missText=" + s.battleP7MissText
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " hasDebuff3=" + runtime.debugEnemyHasDebuffForSmoke(3)
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + storedRaw
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(3)
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, false, "skill13 debuff3 damage frame",
                    new int[]{4}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 280) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 3
                    || runtime.debugEnemyDebuffValueForSmoke(3) != storedRaw
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(3) != 13) {
                throw new IllegalStateException(checkpoint + " expected skill13 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + runtime.debugEnemyDebuffValueForSmoke(3)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(3)
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill13 debuff3 active after HP settled",
                    new int[]{4}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("hp_settled_debuff_active")));

            int hpBeforeTick1 = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=3", 800);
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 3
                    || s.battleActiveQueueSegment != 0
                    || !s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 0
                    || s.battleP7SpecialVisible
                    || !traceContainsAll(s, "active queue visual start bank=1 id=3",
                    "visual=aq id=3", "row=[0, 21, 0, -1]")) {
                throw new IllegalStateException(checkpoint + " expected skill13 debuff3 P12 actor body visual"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " segment=" + s.battleActiveQueueSegment
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("p12_body_visual_actor21")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=3", 800);
            if (s.battleEnemyHp != hpBeforeTick1
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(3) != storedRaw
                    || s.battleEnemyStatusCount != 1) {
                throw new IllegalStateException(checkpoint + " expected debuff3 tick1 to be no damage"
                        + " hp=" + hpBeforeTick1 + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + runtime.debugEnemyDebuffValueForSmoke(3)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill13 debuff3 after tick1",
                    new int[]{4}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("tick1_no_damage_duration2")));

            int hpBeforeTick2 = s.battleEnemyHp;
            runtime.debugTickEnemySourceDebuffForSmoke(s, 3);
            if (s.battleEnemyHp != hpBeforeTick2
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 1
                    || runtime.debugEnemyDebuffValueForSmoke(3) != storedRaw) {
                throw new IllegalStateException(checkpoint + " expected debuff3 tick2 to be no damage"
                        + " hp=" + hpBeforeTick2 + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + runtime.debugEnemyDebuffValueForSmoke(3)
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill13 debuff3 after tick2",
                    new int[]{4}, new int[]{135});
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("tick2_no_damage_duration1")));

            int hpBeforeFinalTick = s.battleEnemyHp;
            int expectedDelayedDamage = Math.max(1, storedRaw * 150 / 100);
            runtime.debugTickEnemySourceDebuffForSmoke(s, 3);
            if (s.battleEnemyHp != Math.max(0, hpBeforeFinalTick - expectedDelayedDamage)
                    || runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 0
                    || s.battleEnemyStatusCount != 0
                    || !traceContains(s, "damage=" + expectedDelayedDamage)
                    || !traceContains(s, "duration 1->0")) {
                throw new IllegalStateException(checkpoint + " expected debuff3 final delayed damage"
                        + " storedRaw=" + storedRaw
                        + " expectedDelayedDamage=" + expectedDelayedDamage
                        + " hp=" + hpBeforeFinalTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " active=" + runtime.debugEnemyHasDebuffForSmoke(3)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            writeScenePng(s, new java.io.File(dir, skill13ThaoChungPngName("final_delayed_damage_expired")));

            VqsvIntroDemo.Scene finalVisual = new VqsvIntroDemo.Scene();
            SourceBattleRuntime finalVisualRuntime = setupPhase10AStatusBattle(finalVisual);
            int finalVisualStartHp = 80;
            finalVisualRuntime.debugQueueDebuffForSmoke(finalVisual, false, 3,
                    storedRaw, 13, 1, finalVisualStartHp);
            tickUntilTraceContains(finalVisual, "active queue apply bank=1 id=3", 800);
            if (finalVisual.battleEnemyHp != finalVisualStartHp - expectedDelayedDamage
                    || finalVisualRuntime.debugEnemyHasDebuffForSmoke(3)
                    || finalVisualRuntime.debugEnemyDebuffDurationForSmoke(3) != 0
                    || !finalVisual.battleP7PostEffectVisible
                    || !finalVisual.battleP7PostEffectText.equals("-" + expectedDelayedDamage)
                    || !traceContainsAll(finalVisual, "active queue apply bank=1 id=3",
                    "hp " + finalVisualStartHp + "->" + (finalVisualStartHp - expectedDelayedDamage),
                    "duration=0", "active=false")) {
                throw new IllegalStateException(checkpoint + " expected controlled debuff3 final visual text"
                        + " startHp=" + finalVisualStartHp
                        + " expectedDelayedDamage=" + expectedDelayedDamage
                        + " hp=" + finalVisual.battleEnemyHp
                        + " duration=" + finalVisualRuntime.debugEnemyDebuffDurationForSmoke(3)
                        + " postVisible=" + finalVisual.battleP7PostEffectVisible
                        + " postText=" + finalVisual.battleP7PostEffectText
                        + " trace=" + tailTrace(finalVisual, 72));
            }
            writeScenePng(finalVisual, new java.io.File(dir,
                    skill13ThaoChungPngName("controlled_final_tick_visual")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(13);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(3);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=13 name=Thao Chung description=Low Wood damage plus delayed Thuc Loai damage after 2 turns.\n"
                    + "aq.c[1][13]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[13]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(13)) + "\n"
                    + "aq.c[7][3]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "bufDebuf ar[1][3]=[0,21,0,-1]\n"
                    + "actorEffect=21 actorSprite=263 actorState=0 actorSide=enemy\n"
                    + "debuff3=Thuc Loai duration=3 storedRaw=" + storedRaw
                    + " icon=4 durationCell=137\n"
                    + "logic=damage powerPercent 50; apply debuff3; tick1/tick2 no damage;"
                    + " final delayed damage=max(1,storedRaw*150/100).\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=29\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " critical=false missText=\n"
                    + "hpSettled enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " debuffDuration=3 storedRaw=" + storedRaw + "\n"
                    + "tick1 hp=" + hpBeforeTick1 + "->" + hpBeforeTick1
                    + " duration=2 noHpDelta=true\n"
                    + "tick2 hp=" + hpBeforeTick2 + "->" + hpBeforeTick2
                    + " duration=1 noHpDelta=true\n"
                    + "finalDelayed hp=" + hpBeforeFinalTick + "->" + s.battleEnemyHp
                    + " damage=" + expectedDelayedDamage
                    + " formula=max(1," + storedRaw + "*150/100)"
                    + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "controlledFinalVisual hp=" + finalVisualStartHp + "->" + finalVisual.battleEnemyHp
                    + " postText=" + finalVisual.battleP7PostEffectText
                    + " duration=" + finalVisualRuntime.debugEnemyDebuffDurationForSmoke(3) + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir, "battle_skill13_thao_chung_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " storedRaw=" + storedRaw
                    + " delayedDamage=" + expectedDelayedDamage
                    + " debuff3Expired=" + !runtime.debugEnemyHasDebuffForSmoke(3));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill19QuangHopHieuUngTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill19_quang_hop_hieu_ung_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 19);
            assertSkill19QuangHopHieuUngSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 19
                    || runtime.debugPlayerSkillPpForSmoke(0) != 15) {
                throw new IllegalStateException(checkpoint + " expected P3 skill19 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 0
                    || s.battleP7ActorEffectOnPlayerSide
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || !traceContains(s, "battle P7 source n() skill=19")
                    || !traceContains(s, "battle P7 actor u.a() start skill=19")
                    || !traceContains(s, "id=21")
                    || !traceContains(s, "param=0")) {
                throw new IllegalStateException(checkpoint + " expected skill19 actor u21/state0 before damage"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 56));
            }
            int actorEnemyHp = s.battleEnemyHp;
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("actor_u21_start")));

            tickUntilBattleP7Phase(s, 2, 220);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=19");
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            int storedRaw = runtime.debugEnemyDebuffValueForSmoke(3);
            if (!s.battleP7DamageVisible
                    || s.battleP7DamageText.isEmpty()
                    || s.battleP7DebuffText.isEmpty()
                    || !s.battleP7MissText.isEmpty()
                    || damage <= 0
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 3
                    || storedRaw <= 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(3) != 19
                    || !traceContains(s, "battle P7 damage frame skill=19")
                    || !traceContains(s, "hit=true")
                    || !traceContains(s, "appliedDebuffId=3")) {
                throw new IllegalStateException(checkpoint + " expected skill19 damage frame with debuff3"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damageText=" + s.battleP7DamageText
                        + " debuffText=" + s.battleP7DebuffText
                        + " missText=" + s.battleP7MissText
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " hasDebuff3=" + runtime.debugEnemyHasDebuffForSmoke(3)
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + storedRaw
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(3)
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, false, "skill19 debuff3 damage frame",
                    new int[]{4}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 280) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 3
                    || runtime.debugEnemyDebuffValueForSmoke(3) != storedRaw
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(3) != 19) {
                throw new IllegalStateException(checkpoint + " expected skill19 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + runtime.debugEnemyDebuffValueForSmoke(3)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(3)
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill19 debuff3 active after HP settled",
                    new int[]{4}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("hp_settled_debuff_active")));

            int hpBeforeTick1 = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=3", 800);
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 3
                    || s.battleActiveQueueSegment != 0
                    || !s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 0
                    || s.battleP7SpecialVisible
                    || !traceContainsAll(s, "active queue visual start bank=1 id=3",
                    "visual=aq id=3", "row=[0, 21, 0, -1]")) {
                throw new IllegalStateException(checkpoint + " expected skill19 debuff3 P12 actor body visual"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " segment=" + s.battleActiveQueueSegment
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("p12_body_visual_actor21")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=3", 800);
            if (s.battleEnemyHp != hpBeforeTick1
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(3) != storedRaw
                    || s.battleEnemyStatusCount != 1) {
                throw new IllegalStateException(checkpoint + " expected skill19 debuff3 tick1 to be no damage"
                        + " hp=" + hpBeforeTick1 + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + runtime.debugEnemyDebuffValueForSmoke(3)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill19 debuff3 after tick1",
                    new int[]{4}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("tick1_no_damage_duration2")));

            int hpBeforeTick2 = s.battleEnemyHp;
            runtime.debugTickEnemySourceDebuffForSmoke(s, 3);
            if (s.battleEnemyHp != hpBeforeTick2
                    || !runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 1
                    || runtime.debugEnemyDebuffValueForSmoke(3) != storedRaw) {
                throw new IllegalStateException(checkpoint + " expected skill19 debuff3 tick2 to be no damage"
                        + " hp=" + hpBeforeTick2 + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " storedRaw=" + runtime.debugEnemyDebuffValueForSmoke(3)
                        + " trace=" + tailTrace(s, 72));
            }
            assertPhase10AStatusSlots(s, false, "skill19 debuff3 after tick2",
                    new int[]{4}, new int[]{135});
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("tick2_no_damage_duration1")));

            int hpBeforeFinalTick = s.battleEnemyHp;
            int expectedDelayedDamage = Math.max(1, storedRaw * 200 / 100);
            runtime.debugTickEnemySourceDebuffForSmoke(s, 3);
            if (s.battleEnemyHp != Math.max(0, hpBeforeFinalTick - expectedDelayedDamage)
                    || runtime.debugEnemyHasDebuffForSmoke(3)
                    || runtime.debugEnemyDebuffDurationForSmoke(3) != 0
                    || s.battleEnemyStatusCount != 0
                    || !traceContains(s, "damage=" + expectedDelayedDamage)
                    || !traceContains(s, "duration 1->0")) {
                throw new IllegalStateException(checkpoint + " expected skill19 final delayed damage"
                        + " storedRaw=" + storedRaw
                        + " expectedDelayedDamage=" + expectedDelayedDamage
                        + " hp=" + hpBeforeFinalTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                        + " active=" + runtime.debugEnemyHasDebuffForSmoke(3)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 72));
            }
            writeScenePng(s, new java.io.File(dir, skill19QuangHopHieuUngPngName("final_delayed_damage_expired")));

            VqsvIntroDemo.Scene finalVisual = new VqsvIntroDemo.Scene();
            SourceBattleRuntime finalVisualRuntime = setupPhase10AStatusBattle(finalVisual);
            int finalVisualStartHp = Math.min(100, finalVisual.battleEnemyMaxHp);
            finalVisualRuntime.debugQueueDebuffForSmoke(finalVisual, false, 3,
                    storedRaw, 19, 1, finalVisualStartHp);
            tickUntilTraceContains(finalVisual, "active queue apply bank=1 id=3", 800);
            int expectedFinalVisualHp = Math.max(0, finalVisualStartHp - expectedDelayedDamage);
            if (finalVisual.battleEnemyHp != expectedFinalVisualHp
                    || finalVisualRuntime.debugEnemyHasDebuffForSmoke(3)
                    || finalVisualRuntime.debugEnemyDebuffDurationForSmoke(3) != 0
                    || !finalVisual.battleP7PostEffectVisible
                    || !finalVisual.battleP7PostEffectText.equals("-" + expectedDelayedDamage)
                    || !traceContainsAll(finalVisual, "active queue apply bank=1 id=3",
                    "hp " + finalVisualStartHp + "->" + expectedFinalVisualHp,
                    "duration=0", "active=false")) {
                throw new IllegalStateException(checkpoint + " expected controlled skill19 final visual text"
                        + " startHp=" + finalVisualStartHp
                        + " expectedDelayedDamage=" + expectedDelayedDamage
                        + " hp=" + finalVisual.battleEnemyHp
                        + " duration=" + finalVisualRuntime.debugEnemyDebuffDurationForSmoke(3)
                        + " postVisible=" + finalVisual.battleP7PostEffectVisible
                        + " postText=" + finalVisual.battleP7PostEffectText
                        + " trace=" + tailTrace(finalVisual, 72));
            }
            writeScenePng(finalVisual, new java.io.File(dir,
                    skill19QuangHopHieuUngPngName("controlled_final_tick_visual")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(19);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(3);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=19 name=Quang hop hieu ung description=High Wood damage plus Thuc Loai;"
                    + " delayed damage after 2 turns.\n"
                    + "aq.c[1][19]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[19]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(19)) + "\n"
                    + "aq.c[7][3]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "bufDebuf ar[1][3]=[0,21,0,-1]\n"
                    + "actorEffect=21 actorSprite=263 actorState=0 actorSide=enemy\n"
                    + "debuff3=Thuc Loai duration=3 storedRaw=" + storedRaw
                    + " icon=4 durationCell=137\n"
                    + "logic=damage powerPercent 150; apply debuff3; tick1/tick2 no damage;"
                    + " final delayed damage=max(1,storedRaw*200/100).\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=14\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " critical=false missText=\n"
                    + "hpSettled enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " debuffDuration=3 storedRaw=" + storedRaw + "\n"
                    + "tick1 hp=" + hpBeforeTick1 + "->" + hpBeforeTick1
                    + " duration=2 noHpDelta=true\n"
                    + "tick2 hp=" + hpBeforeTick2 + "->" + hpBeforeTick2
                    + " duration=1 noHpDelta=true\n"
                    + "finalDelayed hp=" + hpBeforeFinalTick + "->" + s.battleEnemyHp
                    + " damage=" + expectedDelayedDamage
                    + " formula=max(1," + storedRaw + "*200/100)"
                    + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(3)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "controlledFinalVisual hp=" + finalVisualStartHp + "->" + finalVisual.battleEnemyHp
                    + " postText=" + finalVisual.battleP7PostEffectText
                    + " duration=" + finalVisualRuntime.debugEnemyDebuffDurationForSmoke(3) + "\n"
                    + "battleLab=covered by battle_lab_skill_test_all all-skill table; timeline suite lives in WoodSkill\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir, "battle_skill19_quang_hop_hieu_ung_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " storedRaw=" + storedRaw
                    + " delayedDamage=" + expectedDelayedDamage
                    + " debuff3Expired=" + !runtime.debugEnemyHasDebuffForSmoke(3));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill14DangChiBichLuyTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill14_dang_chi_bich_luy_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 14);
            assertSkill14DangChiBichLuySourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 14
                    || runtime.debugPlayerSkillPpForSmoke(0) != 10) {
                throw new IllegalStateException(checkpoint + " expected P3 skill14 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            runtime.debugSetPlayerDefenseForSmoke(s, 100);
            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            int beforeDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            writeScenePng(s, new java.io.File(dir, skill14DangChiBichLuyPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 32 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 263
                    || s.battleP7ActorEffectState != 1
                    || s.battleEnemyHp != beforeEnemyHp
                    || s.battlePlayerHp != beforePlayerHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 9
                    || !traceContains(s, "battle P7 source n() skill=14")
                    || !traceContains(s, "battle P7 actor u.a() start skill=14")
                    || !traceContains(s, "id=21")
                    || !traceContains(s, "param=1")) {
                throw new IllegalStateException(checkpoint + " expected skill14 actor u21/state1 before post effect"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 72));
            }
            boolean actorOnPlayerSide = s.battleP7ActorEffectOnPlayerSide;
            writeScenePng(s, new java.io.File(dir, skill14DangChiBichLuyPngName("actor_u21_state1")));

            tickUntilBattleP7Phase(s, 3, 340);
            int afterApplyDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            String postText = s.battleP7PostEffectText;
            if (!s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || postText.isEmpty()
                    || runtime.debugPlayerSkillPpForSmoke(0) != 9
                    || s.battleEnemyHp != beforeEnemyHp
                    || s.battlePlayerHp != beforePlayerHp
                    || !runtime.debugPlayerHasBuffForSmoke(2)
                    || runtime.debugPlayerBuffValueForSmoke(2) != 30
                    || runtime.debugPlayerBuffSecondaryValueForSmoke(2) != 10
                    || runtime.debugPlayerBuffDurationForSmoke(2) != 3
                    || runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE) != 100
                    || afterApplyDefense != 130
                    || traceContains(s, "battle P7 damage frame skill=14")
                    || traceContains(s, "battle P7 hitroll skill=14")
                    || traceContains(s, "battle P7 speffect skill=14")
                    || !traceContains(s, "battle P7 no-damage skill=14")
                    || !traceContains(s, "game.d.q postEffect skill=14")
                    || !traceContains(s, "buffId=2")) {
                throw new IllegalStateException(checkpoint + " expected skill14 to apply player buff2 only"
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                        + " postText=" + s.battleP7PostEffectText
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " hasBuff2=" + runtime.debugPlayerHasBuffForSmoke(2)
                        + " value=" + runtime.debugPlayerBuffValueForSmoke(2)
                        + " secondary=" + runtime.debugPlayerBuffSecondaryValueForSmoke(2)
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(2)
                        + " defense=" + runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + "->" + afterApplyDefense
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, true, "skill14 buff2 after apply",
                    new int[]{14}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill14DangChiBichLuyPngName("after_apply_icon")));

            VqsvIntroDemo.Scene forcedHitScene = new VqsvIntroDemo.Scene();
            int[] forcedHit = statusBuff2Skill10ReflectProbe(forcedHitScene, true, 99, 99, false, true);
            int expectedReflect = forcedHit[0] * 10 / 100;
            if (forcedHit[1] != expectedReflect
                    || forcedHit[3] != forcedHit[2] - expectedReflect
                    || forcedHit[4] != 40
                    || forcedHit[5] != 52
                    || forcedHit[6] != 3
                    || forcedHit[7] != 10
                    || !traceContains(forcedHitScene, "hit=true")
                    || !traceContains(forcedHitScene, "sideEffectsCommitted=true")
                    || !traceContains(forcedHitScene, "PORTED battle P7 buff2 Kinh Cuc reflect")) {
                throw new IllegalStateException(checkpoint + " expected forced hit reflect proof"
                        + " damage=" + forcedHit[0]
                        + " reflect=" + forcedHit[1]
                        + " expectedReflect=" + expectedReflect
                        + " playerHp=" + forcedHit[2] + "->" + forcedHit[3]
                        + " defense=" + forcedHit[4] + "->" + forcedHit[5]
                        + " trace=" + tailTrace(forcedHitScene, 72));
            }
            assertPhase10AStatusSlots(forcedHitScene, false, "skill14 forced hit reflect",
                    new int[]{14}, new int[]{137});
            writeScenePng(forcedHitScene, new java.io.File(dir,
                    skill14DangChiBichLuyPngName("forced_hit_reflect")));

            VqsvIntroDemo.Scene forcedMissScene = new VqsvIntroDemo.Scene();
            int[] forcedMiss = statusBuff2Skill10ReflectProbe(forcedMissScene, true, 99, 0, true, false);
            if (!forcedMissScene.battleP7DamageVisible
                    || !VqsvText.Battle.DODGE.equals(forcedMissScene.battleP7MissText)
                    || !forcedMissScene.battleP7DamageText.isEmpty()
                    || forcedMissScene.battleEnemyHp != forcedMissScene.battleEnemyMaxHp
                    || forcedMiss[3] != forcedMiss[2]
                    || forcedMiss[4] != 40
                    || forcedMiss[5] != 52
                    || !traceContains(forcedMissScene, "hit=false")
                    || !traceContains(forcedMissScene, "sideEffectsCommitted=false")
                    || traceContains(forcedMissScene, "PORTED battle P7 buff2 Kinh Cuc reflect")) {
                throw new IllegalStateException(checkpoint + " expected forced miss to avoid reflect"
                        + " missText=" + forcedMissScene.battleP7MissText
                        + " damageText=" + forcedMissScene.battleP7DamageText
                        + " enemyHp=" + forcedMissScene.battleEnemyHp + "/"
                        + forcedMissScene.battleEnemyMaxHp
                        + " playerHp=" + forcedMiss[2] + "->" + forcedMiss[3]
                        + " trace=" + tailTrace(forcedMissScene, 72));
            }
            writeScenePng(forcedMissScene, new java.io.File(dir,
                    skill14DangChiBichLuyPngName("forced_miss_no_reflect")));

            VqsvIntroDemo.Scene forcedCritScene = new VqsvIntroDemo.Scene();
            int[] forcedCrit = statusBuff2Skill10ReflectProbe(forcedCritScene, true, 0, 99, false, true);
            int expectedCritReflect = forcedCrit[0] * 10 / 100;
            if (forcedCrit[8] != 1
                    || forcedCrit[1] != expectedCritReflect
                    || forcedCrit[3] != forcedCrit[2] - expectedCritReflect
                    || !traceContains(forcedCritScene, "critFlag=1")
                    || !traceContains(forcedCritScene, "hit=true")
                    || !traceContains(forcedCritScene, "PORTED battle P7 buff2 Kinh Cuc reflect")) {
                throw new IllegalStateException(checkpoint + " expected forced crit to reflect from crit damage"
                        + " damage=" + forcedCrit[0]
                        + " reflect=" + forcedCrit[1]
                        + " expectedReflect=" + expectedCritReflect
                        + " playerHp=" + forcedCrit[2] + "->" + forcedCrit[3]
                        + " criticalAtDamageFrame=" + forcedCrit[8]
                        + " trace=" + tailTrace(forcedCritScene, 72));
            }
            writeScenePng(forcedCritScene, new java.io.File(dir,
                    skill14DangChiBichLuyPngName("forced_crit_reflect")));

            VqsvIntroDemo.Scene expiryScene = new VqsvIntroDemo.Scene();
            SourceBattleRuntime expiryRuntime = setupPhase10AStatusBattle(expiryScene);
            expiryRuntime.debugSetPlayerDefenseForSmoke(expiryScene, 100);
            expiryRuntime.debugPlayerSourceBuffForSmoke(expiryScene, 2, 0, 14);
            if (!expiryRuntime.debugPlayerHasBuffForSmoke(2)
                    || expiryRuntime.debugPlayerBuffValueForSmoke(2) != 30
                    || expiryRuntime.debugPlayerBuffSecondaryValueForSmoke(2) != 10
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(2) != 3
                    || expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 130) {
                throw new IllegalStateException(checkpoint + " expected expiry setup with buff2 active"
                        + " hasBuff2=" + expiryRuntime.debugPlayerHasBuffForSmoke(2)
                        + " value=" + expiryRuntime.debugPlayerBuffValueForSmoke(2)
                        + " secondary=" + expiryRuntime.debugPlayerBuffSecondaryValueForSmoke(2)
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(2)
                        + " defense=" + expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " trace=" + tailTrace(expiryScene, 48));
            }
            assertPhase10AStatusSlots(expiryScene, true, "skill14 expiry setup",
                    new int[]{14}, new int[]{137});
            expiryRuntime.debugTickPlayerSourceBuffForSmoke(expiryScene, 2);
            if (!expiryRuntime.debugPlayerHasBuffForSmoke(2)
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(2) != 2
                    || expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 130) {
                throw new IllegalStateException(checkpoint + " expected buff2 duration 2 still active"
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(2)
                        + " defense=" + expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " trace=" + tailTrace(expiryScene, 48));
            }
            assertPhase10AStatusSlots(expiryScene, true, "skill14 expiry duration2",
                    new int[]{14}, new int[]{136});
            writeScenePng(expiryScene, new java.io.File(dir,
                    skill14DangChiBichLuyPngName("expiry_duration2")));

            expiryRuntime.debugTickPlayerSourceBuffForSmoke(expiryScene, 2);
            if (!expiryRuntime.debugPlayerHasBuffForSmoke(2)
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(2) != 1
                    || expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 130) {
                throw new IllegalStateException(checkpoint + " expected buff2 duration 1 still active"
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(2)
                        + " defense=" + expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " trace=" + tailTrace(expiryScene, 48));
            }
            assertPhase10AStatusSlots(expiryScene, true, "skill14 expiry duration1",
                    new int[]{14}, new int[]{135});
            writeScenePng(expiryScene, new java.io.File(dir,
                    skill14DangChiBichLuyPngName("expiry_duration1")));

            expiryRuntime.debugTickPlayerSourceBuffForSmoke(expiryScene, 2);
            if (expiryRuntime.debugPlayerHasBuffForSmoke(2)
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(2) != 0
                    || expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 100
                    || expiryScene.battlePlayerStatusCount != 0
                    || !traceContains(expiryScene, "player source buff tick id=2")
                    || !traceContains(expiryScene, "duration 1->0")
                    || !traceContains(expiryScene, "active=false")) {
                throw new IllegalStateException(checkpoint + " expected buff2 expiry to restore defense and clear icon"
                        + " active=" + expiryRuntime.debugPlayerHasBuffForSmoke(2)
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(2)
                        + " defense=" + expiryRuntime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " statusCount=" + expiryScene.battlePlayerStatusCount
                        + " trace=" + tailTrace(expiryScene, 72));
            }
            writeScenePng(expiryScene, new java.io.File(dir,
                    skill14DangChiBichLuyPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(14);
            BattleBuffRow buff = VqsvBattleTables.instance().buff(2);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=14 name=Dang chi bich luy description=No damage; defense up 30%; reflect damage 10%; lasts 3 turns.\n"
                    + "aq.c[1][14]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[14]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(14)) + "\n"
                    + "aq.c[6][2]=" + java.util.Arrays.toString(buff.raw) + "\n"
                    + "actorEffect=21 actorSprite=263 actorState=1 actorSidePlayer="
                    + actorOnPlayerSide + "\n"
                    + "logic=no direct damage; no hitroll; no speffect; apply self buff2 Kinh Cuc; icon=14 durationCell=137.\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " defense=" + beforeDefense + "\n"
                    + "afterApply hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " defense=100->" + afterApplyDefense
                    + " postText=" + postText
                    + " buffValue=30 reflectPercent=10 duration=3\n"
                    + "forcedHit damage=" + forcedHit[0]
                    + " reflect=" + forcedHit[1]
                    + " expectedReflect=" + expectedReflect
                    + " playerHp=" + forcedHit[2] + "->" + forcedHit[3]
                    + " targetDefense=" + forcedHit[4] + "->" + forcedHit[5] + "\n"
                    + "forcedMiss missText=" + forcedMissScene.battleP7MissText
                    + " noReflectHp=" + forcedMiss[2] + "->" + forcedMiss[3]
                    + " targetDefense=" + forcedMiss[4] + "->" + forcedMiss[5] + "\n"
                    + "forcedCrit damage=" + forcedCrit[0]
                    + " reflect=" + forcedCrit[1]
                    + " expectedReflect=" + expectedCritReflect
                    + " playerHp=" + forcedCrit[2] + "->" + forcedCrit[3] + "\n"
                    + "expiry duration=3->2->1->0 defense=130->100 statusCount="
                    + expiryScene.battlePlayerStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir,
                            "battle_skill14_dang_chi_bich_luy_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " defense=100->" + afterApplyDefense
                    + " reflectHit=" + forcedHit[1]
                    + " reflectCrit=" + forcedCrit[1]
                    + " expired=" + !expiryRuntime.debugPlayerHasBuffForSmoke(2));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill15ThaoNguyenThuatTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill15_thao_nguyen_thuat_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, 15);
            assertSkill15ThaoNguyenThuatSourceRows(s, checkpoint);
            if (!"P3".equals(s.battleStateName)
                    || s.battleSkillIds.length == 0
                    || s.battleSkillIds[0] != 15
                    || runtime.debugPlayerSkillPpForSmoke(0) != 10) {
                throw new IllegalStateException(checkpoint + " expected P3 skill15 before confirm"
                        + " state=" + s.battleStateName
                        + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 24));
            }

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            int heal = Math.max(0, s.battlePlayerMaxHp * 5 / 100);
            int beforePlayerHp = Math.max(20, s.battlePlayerMaxHp / 2);
            runtime.debugSetPlayerHpForSmoke(s, beforePlayerHp);
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill15ThaoNguyenThuatPngName("before")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 32 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            if (!s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 308
                    || s.battleP7ActorEffectState != 0
                    || !s.battleP7ActorEffectOnPlayerSide
                    || runtime.debugPlayerSkillPpForSmoke(0) != 9
                    || s.battlePlayerHp != beforePlayerHp
                    || s.battleEnemyHp != beforeEnemyHp
                    || !traceContains(s, "battle P7 source n() skill=15")
                    || !traceContains(s, "battle P7 actor u.a() start skill=15")
                    || !traceContains(s, "id=33")
                    || !traceContains(s, "param=0")) {
                throw new IllegalStateException(checkpoint + " expected skill15 actor u33/state0 before heal"
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 72));
            }
            int actorCursor = s.battleP7ActorEffectCursor;
            writeScenePng(s, new java.io.File(dir, skill15ThaoNguyenThuatPngName("actor_u33_start")));

            for (int i = 0; i < 160 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 9); i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 9
                    || !s.battleP7SpecialOnPlayerSide
                    || s.battleP7SpecialRow.length < 8
                    || s.battlePlayerHp != beforePlayerHp
                    || s.battleEnemyHp != beforeEnemyHp
                    || !traceContains(s, "battle P7 speffect skill=15")
                    || !traceContains(s, "speffect=7")
                    || !traceContains(s, "AH type 9")) {
                throw new IllegalStateException(checkpoint + " expected skill15 speffect7/AH9 before apply heal"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill15ThaoNguyenThuatPngName("speffect7_ah9")));

            tickUntilBattleP7Phase(s, 3, 360);
            int afterApplyHp = Math.min(s.battlePlayerMaxHp, beforePlayerHp + heal);
            String postText = s.battleP7PostEffectText;
            if (!s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || !("+" + heal).equals(postText)
                    || s.battlePlayerHp != afterApplyHp
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 9
                    || !runtime.debugPlayerHasBuffForSmoke(3)
                    || runtime.debugPlayerBuffValueForSmoke(3) != heal
                    || runtime.debugPlayerBuffDurationForSmoke(3) != 3
                    || traceContains(s, "battle P7 damage frame skill=15")
                    || traceContains(s, "battle P7 hitroll skill=15")
                    || !traceContains(s, "battle P7 no-damage skill=15")
                    || !traceContains(s, "game.d.q postEffect skill=15")
                    || !traceContains(s, "buffId=3")) {
                throw new IllegalStateException(checkpoint + " expected skill15 apply heal and buff3"
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                        + " postText=" + s.battleP7PostEffectText
                        + " hp=" + beforePlayerHp + "->" + s.battlePlayerHp
                        + " expectedHp=" + afterApplyHp
                        + " heal=" + heal
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " hasBuff3=" + runtime.debugPlayerHasBuffForSmoke(3)
                        + " value=" + runtime.debugPlayerBuffValueForSmoke(3)
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(3)
                        + " trace=" + tailTrace(s, 96));
            }
            int hpAfterApplyObserved = s.battlePlayerHp;
            assertPhase10AStatusSlots(s, true, "skill15 buff3 after apply",
                    new int[]{15}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill15ThaoNguyenThuatPngName("after_apply_heal_icon")));

            tickUntilTraceContains(s, "active queue visual start bank=0 id=3", 900);
            if (!s.battleActiveQueueVisible
                    || !s.battleActiveQueuePlayerSide
                    || s.battleActiveQueueBank != 0
                    || s.battleActiveQueueEffectId != 3
                    || !traceContains(s, "active queue visual bank=0 buff=3")) {
                throw new IllegalStateException(checkpoint + " expected skill15 buff3 P12/P13 body visual start"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " side=" + s.battleActiveQueuePlayerSide
                        + " bank=" + s.battleActiveQueueBank
                        + " id=" + s.battleActiveQueueEffectId
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, true, "skill15 buff3 P12 visual start",
                    new int[]{15}, new int[]{137});
            int hpBeforeActiveTick = s.battlePlayerHp;
            writeScenePng(s, new java.io.File(dir, skill15ThaoNguyenThuatPngName("p12_body_visual_start")));

            tickUntilTraceContains(s, "active queue apply bank=0 id=3", 900);
            int afterTickHp = Math.min(s.battlePlayerMaxHp, hpBeforeActiveTick + heal);
            if (s.battlePlayerHp != afterTickHp
                    || !s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || !("+" + heal).equals(s.battleP7PostEffectText)
                    || !runtime.debugPlayerHasBuffForSmoke(3)
                    || runtime.debugPlayerBuffDurationForSmoke(3) != 2
                    || runtime.debugPlayerBuffValueForSmoke(3) != heal
                    || !traceContains(s, "active queue apply bank=0 id=3")
                    || !traceContains(s, "hp " + hpBeforeActiveTick + "->" + afterTickHp)
                    || !traceContains(s, "duration=2")) {
                throw new IllegalStateException(checkpoint + " expected skill15 P12 tick heal and duration 2"
                        + " hp=" + hpBeforeActiveTick + "->" + s.battlePlayerHp
                        + " expectedHp=" + afterTickHp
                        + " heal=" + heal
                        + " postText=" + s.battleP7PostEffectText
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(3)
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, true, "skill15 buff3 after first tick",
                    new int[]{15}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill15ThaoNguyenThuatPngName("tick_heal_duration2")));

            int expiryStartHp = Math.max(20, s.battlePlayerMaxHp / 2);
            VqsvIntroDemo.Scene expiryScene = new VqsvIntroDemo.Scene();
            SourceBattleRuntime expiryRuntime = setupPhase10AStatusBattle(expiryScene);
            expiryRuntime.debugSetPlayerHpForSmoke(expiryScene, expiryStartHp);
            expiryRuntime.debugPlayerSourceBuffForSmoke(expiryScene, 3, 0, 15);
            int expiryHeal = expiryScene.battlePlayerMaxHp * 5 / 100;
            int expiryAfterApply = Math.min(expiryScene.battlePlayerMaxHp, expiryStartHp + expiryHeal);
            if (!expiryRuntime.debugPlayerHasBuffForSmoke(3)
                    || expiryRuntime.debugPlayerBuffValueForSmoke(3) != expiryHeal
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(3) != 3
                    || expiryScene.battlePlayerHp != expiryAfterApply) {
                throw new IllegalStateException(checkpoint + " expected skill15 expiry setup active"
                        + " hp=" + expiryStartHp + "->" + expiryScene.battlePlayerHp
                        + " heal=" + expiryHeal
                        + " value=" + expiryRuntime.debugPlayerBuffValueForSmoke(3)
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(3)
                        + " trace=" + tailTrace(expiryScene, 48));
            }
            assertPhase10AStatusSlots(expiryScene, true, "skill15 expiry setup",
                    new int[]{15}, new int[]{137});
            expiryRuntime.debugTickPlayerSourceBuffForSmoke(expiryScene, 3);
            int expiryAfterTick1 = Math.min(expiryScene.battlePlayerMaxHp, expiryAfterApply + expiryHeal);
            if (!expiryRuntime.debugPlayerHasBuffForSmoke(3)
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(3) != 2
                    || expiryScene.battlePlayerHp != expiryAfterTick1) {
                throw new IllegalStateException(checkpoint + " expected skill15 expiry tick1"
                        + " hp=" + expiryScene.battlePlayerHp
                        + " expected=" + expiryAfterTick1
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(3)
                        + " trace=" + tailTrace(expiryScene, 48));
            }
            assertPhase10AStatusSlots(expiryScene, true, "skill15 expiry duration2",
                    new int[]{15}, new int[]{136});
            writeScenePng(expiryScene, new java.io.File(dir,
                    skill15ThaoNguyenThuatPngName("expiry_duration2")));

            expiryRuntime.debugTickPlayerSourceBuffForSmoke(expiryScene, 3);
            int expiryAfterTick2 = Math.min(expiryScene.battlePlayerMaxHp, expiryAfterTick1 + expiryHeal);
            if (!expiryRuntime.debugPlayerHasBuffForSmoke(3)
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(3) != 1
                    || expiryScene.battlePlayerHp != expiryAfterTick2) {
                throw new IllegalStateException(checkpoint + " expected skill15 expiry tick2"
                        + " hp=" + expiryScene.battlePlayerHp
                        + " expected=" + expiryAfterTick2
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(3)
                        + " trace=" + tailTrace(expiryScene, 48));
            }
            assertPhase10AStatusSlots(expiryScene, true, "skill15 expiry duration1",
                    new int[]{15}, new int[]{135});
            writeScenePng(expiryScene, new java.io.File(dir,
                    skill15ThaoNguyenThuatPngName("expiry_duration1")));

            expiryRuntime.debugTickPlayerSourceBuffForSmoke(expiryScene, 3);
            int expiryAfterTick3 = Math.min(expiryScene.battlePlayerMaxHp, expiryAfterTick2 + expiryHeal);
            if (expiryRuntime.debugPlayerHasBuffForSmoke(3)
                    || expiryRuntime.debugPlayerBuffDurationForSmoke(3) != 0
                    || expiryScene.battlePlayerHp != expiryAfterTick3
                    || expiryScene.battlePlayerStatusCount != 0
                    || !traceContains(expiryScene, "player source buff tick id=3")
                    || !traceContains(expiryScene, "duration 1->0")
                    || !traceContains(expiryScene, "active=false")
                    || !traceContains(expiryScene, "heal=" + expiryHeal)) {
                throw new IllegalStateException(checkpoint + " expected skill15 expiry to heal third time and clear"
                        + " active=" + expiryRuntime.debugPlayerHasBuffForSmoke(3)
                        + " duration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(3)
                        + " hp=" + expiryScene.battlePlayerHp
                        + " expectedHp=" + expiryAfterTick3
                        + " statusCount=" + expiryScene.battlePlayerStatusCount
                        + " trace=" + tailTrace(expiryScene, 72));
            }
            writeScenePng(expiryScene, new java.io.File(dir,
                    skill15ThaoNguyenThuatPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(15);
            BattleBuffRow buff = VqsvBattleTables.instance().buff(3);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=15 name=Thao nguyen thuat description=No damage; heals 5% maxHP on apply and each tick for 3 turns.\n"
                    + "aq.c[1][15]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[15]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(15)) + "\n"
                    + "speffect7=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(7)) + "\n"
                    + "aq.c[6][3]=" + java.util.Arrays.toString(buff.raw) + "\n"
                    + "actorEffect=33 actorSprite=308 actorState=0 actorSide=player actorCursorAtStart="
                    + actorCursor + "\n"
                    + "logic=no direct damage; no hitroll; apply self buff3 Khoi Phuc; heal=maxHp*5/100; icon=15 durationCell=137.\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "\n"
                    + "afterApply hp=" + beforePlayerHp + "->" + hpAfterApplyObserved
                    + " expected=" + afterApplyHp
                    + " heal=" + heal
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " postText=" + postText
                    + " duration=3\n"
                    + "tick1 hp=" + hpBeforeActiveTick + "->" + afterTickHp
                    + " heal=" + heal
                    + " duration=2\n"
                    + "expiry hp=" + expiryStartHp + "->" + expiryAfterApply
                    + "->" + expiryAfterTick1
                    + "->" + expiryAfterTick2
                    + "->" + expiryScene.battlePlayerHp
                    + " finalDuration=" + expiryRuntime.debugPlayerBuffDurationForSmoke(3)
                    + " statusCount=" + expiryScene.battlePlayerStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir,
                            "battle_skill15_thao_nguyen_thuat_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " heal=" + heal
                    + " tickHp=" + hpBeforeActiveTick + "->" + afterTickHp
                    + " expired=" + !expiryRuntime.debugPlayerHasBuffForSmoke(3));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static void assertSkill11QuangPhanSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(11);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(11);
        short[] speffect10 = VqsvBattleAnimationTables.instance().speffectRow(10);
        byte[] expectedEffect = new byte[]{0, 0, 21, 1, -1, -1, 0, 1, 1, 10, 0, -1, -1, 0};
        short[] expectedSpeffect = new short[]{9, 120, 218, 217, 169, 0, 4, 2};
        if (row == null
                || row.elementFamily != 1
                || row.nameTextId != 128
                || row.descriptionTextId != 540
                || row.powerPercent != 90
                || row.ppMax != 45
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != 10
                || row.targetSide != 0
                || !java.util.Arrays.equals(effect, expectedEffect)
                || !java.util.Arrays.equals(speffect10, expectedSpeffect)) {
            throw new IllegalStateException(checkpoint + " skill11 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect10=" + java.util.Arrays.toString(speffect10)
                    + " expectedSpeffect=" + java.util.Arrays.toString(expectedSpeffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill11 Quang Phan source rows"
                + " aq.c[1][11]=" + java.util.Arrays.toString(row.raw)
                + " effect.mid[11]=" + java.util.Arrays.toString(effect)
                + " speffect10=" + java.util.Arrays.toString(speffect10)
                + " name=" + row.name("skill11"));
    }

    private static String skill11QuangPhanPngName(String suffix) {
        return "battle_skill11_quang_phan_timeline_" + suffix + ".png";
    }

    private static void assertSkill17DiepChiAnHueSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(17);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(17);
        short[] speffect10 = VqsvBattleAnimationTables.instance().speffectRow(10);
        byte[] expectedEffect = new byte[]{0, 0, 21, 1, -1, -1, 0, 1, 1, 10, 0, -1, -1, 0};
        short[] expectedSpeffect = new short[]{9, 120, 218, 217, 169, 0, 4, 2};
        if (row == null
                || row.elementFamily != 1
                || row.nameTextId != 134
                || row.descriptionTextId != 546
                || row.powerPercent != 130
                || row.ppMax != 30
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != 40
                || row.targetSide != 0
                || !java.util.Arrays.equals(effect, expectedEffect)
                || !java.util.Arrays.equals(speffect10, expectedSpeffect)) {
            throw new IllegalStateException(checkpoint + " skill17 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect10=" + java.util.Arrays.toString(speffect10)
                    + " expectedSpeffect=" + java.util.Arrays.toString(expectedSpeffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill17 Diep chi an hue source rows"
                + " aq.c[1][17]=" + java.util.Arrays.toString(row.raw)
                + " effect.mid[17]=" + java.util.Arrays.toString(effect)
                + " speffect10=" + java.util.Arrays.toString(speffect10)
                + " name=" + row.name("skill17"));
    }

    private static String skill17DiepChiAnHuePngName(String suffix) {
        return "battle_skill17_diep_chi_an_hue_timeline_" + suffix + ".png";
    }

    private static void assertSkill12DangPhuocSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(12);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(2);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(12);
        short[] speffect6 = VqsvBattleAnimationTables.instance().speffectRow(6);
        byte[] expectedEffect = new byte[]{0, 0, 21, 0, -1, -1, 0, 0, 1, 6, 0, -1, -1, 0};
        short[] expectedSpeffect = new short[]{
                8, 0, 10, 1, 5, 1, 10, 0, 0,
                8, 0, -5, 10, 0, 0,
                8, 0, -5, 10, 0, 0};
        if (row == null
                || debuff == null
                || row.elementFamily != 1
                || row.nameTextId != 129
                || row.descriptionTextId != 541
                || row.powerPercent != 50
                || row.ppMax != 45
                || row.effectMode != 2
                || row.effectId != 2
                || row.chanceOrParam != -1
                || row.targetSide != 0
                || debuff.raw.length < 3
                || debuff.raw[0] != 313
                || debuff.raw[1] != 324
                || debuff.raw[2] != 3
                || !java.util.Arrays.equals(effect, expectedEffect)
                || !java.util.Arrays.equals(speffect6, expectedSpeffect)) {
            throw new IllegalStateException(checkpoint + " skill12 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff2=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect6=" + java.util.Arrays.toString(speffect6)
                    + " expectedSpeffect=" + java.util.Arrays.toString(expectedSpeffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill12 Dang Phuoc source rows"
                + " aq.c[1][12]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][2]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[12]=" + java.util.Arrays.toString(effect)
                + " speffect6=" + java.util.Arrays.toString(speffect6)
                + " name=" + row.name("skill12"));
    }

    private static String skill12DangPhuocPngName(String suffix) {
        return "battle_skill12_dang_phuoc_timeline_" + suffix + ".png";
    }

    private static void assertSkill18DangManTrienNhieuSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(18);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(2);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(18);
        short[] speffect6 = VqsvBattleAnimationTables.instance().speffectRow(6);
        byte[] expectedEffect = new byte[]{0, 0, 21, 0, -1, -1, 0};
        short[] expectedSpeffect = new short[]{
                8, 0, 10, 1, 5, 1, 10, 0, 0,
                8, 0, -5, 10, 0, 0,
                8, 0, -5, 10, 0, 0};
        if (row == null
                || debuff == null
                || row.elementFamily != 1
                || row.nameTextId != 135
                || row.descriptionTextId != 547
                || row.powerPercent != 150
                || row.ppMax != 15
                || row.effectMode != 2
                || row.effectId != 2
                || row.chanceOrParam != -1
                || row.targetSide != 0
                || debuff.raw.length < 3
                || debuff.raw[0] != 313
                || debuff.raw[1] != 324
                || debuff.raw[2] != 3
                || !java.util.Arrays.equals(effect, expectedEffect)
                || !java.util.Arrays.equals(speffect6, expectedSpeffect)) {
            throw new IllegalStateException(checkpoint + " skill18 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff2=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect6=" + java.util.Arrays.toString(speffect6)
                    + " expectedSpeffect=" + java.util.Arrays.toString(expectedSpeffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill18 Dang man trien nhieu source rows"
                + " aq.c[1][18]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][2]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[18]=" + java.util.Arrays.toString(effect)
                + " speffect6=" + java.util.Arrays.toString(speffect6)
                + " name=" + row.name("skill18"));
    }

    private static String skill18DangManTrienNhieuPngName(String suffix) {
        return "battle_skill18_dang_man_trien_nhieu_timeline_" + suffix + ".png";
    }

    private static void assertSkill13ThaoChungSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(13);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(3);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(13);
        byte[] expectedEffect = new byte[]{0, 0, 21, 0, -1, -1, 0};
        if (row == null
                || debuff == null
                || row.elementFamily != 1
                || row.nameTextId != 130
                || row.descriptionTextId != 542
                || row.powerPercent != 50
                || row.ppMax != 30
                || row.effectMode != 2
                || row.effectId != 3
                || row.chanceOrParam != 150
                || row.targetSide != 0
                || debuff.raw.length < 3
                || debuff.raw[0] != 314
                || debuff.raw[1] != 325
                || debuff.raw[2] != 3
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill13 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff3=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill13 Thao Chung source rows"
                + " aq.c[1][13]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][3]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[13]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill13"));
    }

    private static String skill13ThaoChungPngName(String suffix) {
        return "battle_skill13_thao_chung_timeline_" + suffix + ".png";
    }

    private static void assertSkill19QuangHopHieuUngSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(19);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(3);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(19);
        byte[] expectedEffect = new byte[]{0, 0, 21, 0, -1, -1, 0};
        if (row == null
                || debuff == null
                || row.elementFamily != 1
                || row.nameTextId != 136
                || row.descriptionTextId != 548
                || row.powerPercent != 150
                || row.ppMax != 15
                || row.effectMode != 2
                || row.effectId != 3
                || row.chanceOrParam != 200
                || row.targetSide != 0
                || debuff.raw.length < 3
                || debuff.raw[0] != 314
                || debuff.raw[1] != 325
                || debuff.raw[2] != 3
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill19 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff3=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill19 Quang hop hieu ung source rows"
                + " aq.c[1][19]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][3]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[19]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill19"));
    }

    private static String skill19QuangHopHieuUngPngName(String suffix) {
        return "battle_skill19_quang_hop_hieu_ung_timeline_" + suffix + ".png";
    }

    private static void assertSkill14DangChiBichLuySourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(14);
        BattleBuffRow buff = VqsvBattleTables.instance().buff(2);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(14);
        byte[] expectedEffect = new byte[]{0, 0, 21, 1, -1, -1, 0};
        if (row == null
                || buff == null
                || row.elementFamily != 1
                || row.nameTextId != 131
                || row.descriptionTextId != 543
                || row.powerPercent != 0
                || row.ppMax != 10
                || row.effectMode != 1
                || row.effectId != 2
                || row.chanceOrParam != -1
                || row.targetSide != 1
                || buff.raw.length < 5
                || buff.raw[0] != 335
                || buff.raw[1] != 350
                || buff.raw[2] != 3
                || buff.raw[3] != 30
                || buff.raw[4] != 10
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill14 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " buff2=" + (buff == null ? "null" : java.util.Arrays.toString(buff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill14 Dang chi bich luy source rows"
                + " aq.c[1][14]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[6][2]=" + java.util.Arrays.toString(buff.raw)
                + " effect.mid[14]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill14"));
    }

    private static String skill14DangChiBichLuyPngName(String suffix) {
        return "battle_skill14_dang_chi_bich_luy_timeline_" + suffix + ".png";
    }

    private static void assertSkill15ThaoNguyenThuatSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(15);
        BattleBuffRow buff = VqsvBattleTables.instance().buff(3);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(15);
        short[] speffect7 = VqsvBattleAnimationTables.instance().speffectRow(7);
        byte[] expectedEffect = new byte[]{0, 0, 33, 0, 0, -1, 0, 0, 1, 7, 0, -1, -1, 0};
        if (row == null
                || buff == null
                || row.elementFamily != 1
                || row.nameTextId != 132
                || row.descriptionTextId != 544
                || row.powerPercent != 0
                || row.ppMax != 10
                || row.effectMode != 1
                || row.effectId != 3
                || row.chanceOrParam != -1
                || row.targetSide != 1
                || buff.raw.length < 5
                || buff.raw[0] != 336
                || buff.raw[1] != 351
                || buff.raw[2] != 3
                || buff.raw[3] != 5
                || buff.raw[4] != -1
                || !java.util.Arrays.equals(effect, expectedEffect)
                || speffect7.length == 0
                || speffect7[0] != 9) {
            throw new IllegalStateException(checkpoint + " skill15 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " buff3=" + (buff == null ? "null" : java.util.Arrays.toString(buff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect7=" + java.util.Arrays.toString(speffect7));
        }
        s.sourceStateTrace.add("SMOKE verified skill15 Thao nguyen thuat source rows"
                + " aq.c[1][15]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[6][3]=" + java.util.Arrays.toString(buff.raw)
                + " effect.mid[15]=" + java.util.Arrays.toString(effect)
                + " speffect7=" + java.util.Arrays.toString(speffect7)
                + " name=" + row.name("skill15"));
    }

    private static String skill15ThaoNguyenThuatPngName(String suffix) {
        return "battle_skill15_thao_nguyen_thuat_timeline_" + suffix + ".png";
    }

    private static int parsePlusText(String text) {
        if (text == null || text.length() < 2 || text.charAt(0) != '+') {
            return -1;
        }
        try {
            return Integer.parseInt(text.substring(1));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String directTimelinePngName(int skillId, String suffix) {
        return "battle_skill" + skillId + "_direct_timeline_" + suffix + ".png";
    }

    private static void writeScenePng(VqsvIntroDemo.Scene s, java.io.File out) throws java.io.IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        s.render(g);
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    private static SourceBattleRuntime enterElderP3DirectBaseBeforeConfirm(VqsvIntroDemo.Scene s, int skillId) {
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

    private static void assertDirectBaseSourceRows(VqsvIntroDemo.Scene s, String checkpoint, int skillId) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        byte[] expectedEffect = directBaseExpectedEffectRow(skillId);
        if (row == null
                || row.powerPercent != directBaseExpectedPower(skillId)
                || row.ppMax != directBaseExpectedPp(skillId)
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != -1
                || row.targetSide != 0
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " direct-base skill" + skillId + " source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified direct-base source rows skill" + skillId
                + " aq.c[1]=" + java.util.Arrays.toString(row.raw)
                + " effect.mid=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill" + skillId));
    }

    private static void assertDirectBaseP3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                       String checkpoint, int skillId) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != skillId
                || runtime.debugPlayerSkillPpForSmoke(0) != directBaseExpectedPp(skillId)
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm direct-base skill" + skillId
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static void assertDirectBaseP7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                       String checkpoint, int skillId) {
        int effectId = directBaseActorEffectId(skillId);
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != directBaseActorSpriteId(skillId)
                || s.battleP7ActorEffectState != directBaseActorState(skillId)
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != directBaseExpectedPpAfterUse(skillId)
                || !traceContains(s, "battle P7 source n() skill=" + skillId)
                || !traceContains(s, "id=" + effectId)
                || !traceContains(s, "param=" + directBaseActorState(skillId))
                || !traceContains(s, "battle P7 actor u.a() start skill=" + skillId)
                || traceContains(s, "battle P7 damage frame skill=" + skillId)) {
            throw new IllegalStateException(checkpoint + " expected direct-base skill" + skillId + " actor"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorCursor=" + s.battleP7ActorEffectCursor
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 36));
        }
    }

    private static void assertDirectBaseDamageFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint, int skillId, int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != directBaseExpectedPpAfterUse(skillId)
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                || !traceContains(s, "hit=true")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected direct-base skill" + skillId + " damage frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " missText=" + s.battleP7MissText
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " trace=" + tailTrace(s, 36));
        }
    }

    private static byte[] directBaseExpectedEffectRow(int skillId) {
        return new byte[]{0, 0, (byte) directBaseActorEffectId(skillId),
                (byte) directBaseActorState(skillId), -1, -1, 0};
    }

    private static int directBaseActorEffectId(int skillId) {
        switch (skillId) {
            case 10:
            case 16:
                return 21;
            default:
                throw new IllegalArgumentException("Not a wood direct smoke skill: " + skillId);
        }
    }

    private static int directBaseActorSpriteId(int skillId) {
        return directBaseActorEffectId(skillId) + 242;
    }

    private static int directBaseActorState(int skillId) {
        return 1;
    }

    private static int directBaseExpectedPower(int skillId) {
        switch (skillId) {
            case 16:
                return 150;
            default:
                return 100;
        }
    }

    private static int directBaseExpectedPp(int skillId) {
        return directBaseExpectedPower(skillId) == 150 ? 30 : 45;
    }

    private static int directBaseExpectedPpAfterUse(int skillId) {
        return Math.max(0, directBaseExpectedPp(skillId) - 1);
    }

    private static String directBaseAsciiName(int skillId) {
        switch (skillId) {
            case 10:
                return "Diep Toan";
            case 16:
                return "Cham Diep Tram";
            default:
                return "Skill " + skillId;
        }
    }

    private static String directBaseAsciiDescription(int skillId) {
        switch (skillId) {
            case 10:
                return "Thuong ton thap.";
            case 16:
                return "Ty le thuong ton gia tang kha cao.";
            default:
                return "";
        }
    }


}
