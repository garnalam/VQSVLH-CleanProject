import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

final class EarthSkill implements Skill {
    static final EarthSkill INSTANCE = new EarthSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL20_HAT_BUI_TIMELINE_SUITE = {
            "battle_skill20_hat_bui_timeline"
    };
    private static final String[] SKILL20_HAT_BUI_SOURCE_STAGE_SUITE = {
            "battle_skill20_hat_bui_source_stage_animation"
    };
    private static final String[] SKILL21_THO_THUAN_TIMELINE_SUITE = {
            "battle_skill21_tho_thuan_timeline"
    };
    private static final String[] SKILL22_BAO_CAT_TIMELINE_SUITE = {
            "battle_skill22_bao_cat_timeline"
    };
    private static final String[] SKILL23_NHAM_BANG_TIMELINE_SUITE = {
            "battle_skill23_nham_bang_timeline"
    };
    private static final String[] SKILL24_NGUOI_BAO_VE_DIA_GIOI_TIMELINE_SUITE = {
            "battle_skill24_nguoi_bao_ve_dia_gioi_timeline"
    };
    private static final String[] SKILL25_29_EARTH_CLOSEOUT_SUITE = {
            "battle_earth_skills_25_29_closeout"
    };

    private EarthSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_skill20_hat_bui_timeline".equals(suite)) {
            return SKILL20_HAT_BUI_TIMELINE_SUITE;
        }
        if ("battle_skill20_hat_bui_source_stage_animation".equals(suite)) {
            return SKILL20_HAT_BUI_SOURCE_STAGE_SUITE;
        }
        if ("battle_skill21_tho_thuan_timeline".equals(suite)) {
            return SKILL21_THO_THUAN_TIMELINE_SUITE;
        }
        if ("battle_skill22_bao_cat_timeline".equals(suite)) {
            return SKILL22_BAO_CAT_TIMELINE_SUITE;
        }
        if ("battle_skill23_nham_bang_timeline".equals(suite)) {
            return SKILL23_NHAM_BANG_TIMELINE_SUITE;
        }
        if ("battle_skill24_nguoi_bao_ve_dia_gioi_timeline".equals(suite)) {
            return SKILL24_NGUOI_BAO_VE_DIA_GIOI_TIMELINE_SUITE;
        }
        if ("battle_earth_skills_25_29_closeout".equals(suite)) {
            return SKILL25_29_EARTH_CLOSEOUT_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runEarthSkills25To29CloseoutSmokeIfNeeded(checkpoint, outPath)
                || runSkill24NguoiBaoVeDiaGioiTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill23NhamBangTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill22BaoCatTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill21ThoThuanTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill20HatBuiSourceStageSmokeIfNeeded(checkpoint, outPath)
                || runSkill20HatBuiTimelineSmokeIfNeeded(checkpoint, outPath);
    }

    private static boolean runEarthSkills25To29CloseoutSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_earth_skills_25_29_closeout".equals(checkpoint)) {
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

            assertEarthSkills25To29SourceRows(checkpoint);
            EarthSkillCaseResult skill25 = runEarthSkill25To29Case(
                    25, "thach_phu_thuat", 14, -1, false, dir, false);
            EarthSkillCaseResult skill26 = runEarthSkill25To29Case(
                    26, "nham_bao", -1, -1, false, dir, false);
            EarthSkillCaseResult skill27 = runEarthSkill25To29Case(
                    27, "hang_rao_cat_da", 4, -1, false, dir, false);
            EarthSkillCaseResult skill28 = runEarthSkill25To29Case(
                    28, "bao_cat", -1, 1, false, dir, false);
            EarthSkillCaseResult skill29 = runEarthSkill25To29Case(
                    29, "tho_chi_loan_vu", -1, -1, true, dir, true);

            String debug = "checkpoint=" + checkpoint + "\n"
                    + "source=aq.c[1][25..29] + effect.mid[25..29] from S60 merged tables\n"
                    + "status=PORTED/PARTIAL runtime source row/effect/HP/PP/status verified; pixel-perfect pending\n"
                    + skill25.describe()
                    + skill26.describe()
                    + skill27.describe()
                    + skill28.describe()
                    + skill29.describe();
            Files.write(new java.io.File(dir,
                            "battle_earth_skills_25_29_closeout_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            writeScenePngForCheckpointSummary(out, new EarthSkillCaseResult[]{
                    skill25, skill26, skill27, skill28, skill29
            });

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill25=buff14:" + skill25.playerBuffActive
                    + " skill26Damage=" + skill26.damage
                    + " skill27Damage=" + skill27.damage + "/buff4:" + skill27.playerBuffActive
                    + " skill28Damage=" + skill28.damage + "/debuff1:" + skill28.enemyDebuffActive
                    + " skill29Damage=" + skill29.damage + "/preloadedDebuff1:" + skill29.preloadedDebuff1
                    + " images=skill25..skill29 before/effect/result");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static boolean runSkill24NguoiBaoVeDiaGioiTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill24_nguoi_bao_ve_dia_gioi_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 24;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
            assertSkill24NguoiBaoVeDiaGioiSourceRows(s, checkpoint);
            assertSkill24P3BeforeConfirm(s, runtime, checkpoint);

            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            int expectedHeal = Math.max(0, s.battlePlayerMaxHp * 20 / 100);
            int startHp = Math.max(1, s.battlePlayerMaxHp / 2);
            runtime.debugSetPlayerHpForSmoke(s, startHp);
            runtime.debugPlayerDebuffForItemSmoke(s, 5, 8, 32);
            if (!runtime.debugPlayerHasDebuffForSmoke(5)
                    || runtime.debugPlayerHasBuffForSmoke(13)
                    || s.battlePlayerHp != startHp) {
                throw new IllegalStateException(checkpoint + " expected skill24 setup wounded player with debuff5"
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + " hasDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                        + " hasBuff13=" + runtime.debugPlayerHasBuffForSmoke(13)
                        + " trace=" + tailTrace(s, 48));
            }

            int beforePlayerHp = s.battlePlayerHp;
            int beforeEnemyHp = s.battleEnemyHp;
            int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("before_wounded_debuff5")));

            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            assertSkill24P7ActorVisible(s, runtime, checkpoint, startHp);
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("actor_u22_start")));

            for (int i = 0; i < 260 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 1); i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || !s.battleP7SpecialOnPlayerSide
                    || s.battleP7SpecialType != 1
                    || s.battleP7SpecialRow.length == 0
                    || s.battlePlayerHp != startHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 9
                    || !runtime.debugPlayerHasDebuffForSmoke(5)
                    || runtime.debugPlayerHasBuffForSmoke(13)
                    || !traceContainsAll(s, "battle P7 speffect skill=24",
                    "chunk=1", "speffect=17", "AH type 1")
                    || traceContains(s, "battle P7 damage frame skill=24")) {
                throw new IllegalStateException(checkpoint + " expected skill24 speffect17/AH1 before apply"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " specialType=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " hasDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                        + " hasBuff13=" + runtime.debugPlayerHasBuffForSmoke(13)
                        + " trace=" + tailTrace(s, 120));
            }
            int specialType = s.battleP7SpecialType;
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("speffect17_type1")));

            tickUntilBattleP7Phase(s, 3, 420);
            int afterApplyHp = Math.min(s.battlePlayerMaxHp, startHp + expectedHeal);
            if (!s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || !s.battleP7PostEffectText.equals("+" + expectedHeal)
                    || s.battlePlayerHp != afterApplyHp
                    || s.battleEnemyHp != beforeEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 9
                    || !runtime.debugPlayerHasBuffForSmoke(13)
                    || runtime.debugPlayerActiveBuffSlotForSmoke(13) < 0
                    || runtime.debugPlayerBuffValueForSmoke(13) != expectedHeal
                    || runtime.debugPlayerBuffDurationForSmoke(13) != 3
                    || runtime.debugPlayerHasDebuffForSmoke(5)
                    || traceContains(s, "battle P7 damage frame skill=24")
                    || traceContains(s, "battle P7 hitroll skill=24")
                    || !traceContains(s, "battle P7 no-damage skill=24")
                    || !traceContains(s, "game.d.q postEffect skill=24")
                    || !traceContains(s, "buffId=13")
                    || !traceContains(s, "targetSide=1")) {
                throw new IllegalStateException(checkpoint + " expected skill24 apply to heal/cleanse/buff without damage"
                        + " expectedHeal=" + expectedHeal
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + " expectedHp=" + afterApplyHp
                        + " enemyHp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postText=" + s.battleP7PostEffectText
                        + " hasBuff13=" + runtime.debugPlayerHasBuffForSmoke(13)
                        + " buffValue=" + runtime.debugPlayerBuffValueForSmoke(13)
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(13)
                        + " hasDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                        + " trace=" + tailTrace(s, 140));
            }
            assertPhase10AStatusSlots(s, true, "skill24 buff13 after apply",
                    new int[]{25}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("after_apply_cleanse_heal")));

            tickUntilTraceContains(s, "active queue visual start bank=0 id=13", 1200);
            if (!s.battleActiveQueueVisible
                    || !s.battleActiveQueuePlayerSide
                    || s.battleActiveQueueBank != 0
                    || s.battleActiveQueueBuffId != 13
                    || !s.battleP7ActorEffectVisible
                    || s.battleP7ActorEffectSpriteId != 264
                    || s.battleP7ActorEffectState != 6
                    || !s.battleP7ActorEffectOnPlayerSide
                    || !traceContains(s, "visual=ap id=13")) {
                throw new IllegalStateException(checkpoint + " expected skill24 P13 buff13 body visual"
                        + " state=" + s.battleStateName
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " sidePlayer=" + s.battleActiveQueuePlayerSide
                        + " bank=" + s.battleActiveQueueBank
                        + " buffId=" + s.battleActiveQueueBuffId
                        + " actorVisible=" + s.battleP7ActorEffectVisible
                        + " actorSprite=" + s.battleP7ActorEffectSpriteId
                        + " actorState=" + s.battleP7ActorEffectState
                        + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " trace=" + tailTrace(s, 120));
            }
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("p13_body_visual_actor22")));

            int hpBeforeActiveTick = s.battlePlayerHp;
            tickUntilTraceContains(s, "active queue apply bank=0 id=13", 1200);
            int afterTickHp = Math.min(s.battlePlayerMaxHp, hpBeforeActiveTick + expectedHeal);
            if (!runtime.debugPlayerHasBuffForSmoke(13)
                    || runtime.debugPlayerBuffDurationForSmoke(13) != 2
                    || s.battlePlayerHp != afterTickHp
                    || !s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || !s.battleP7PostEffectText.equals("+" + expectedHeal)
                    || runtime.debugPlayerHasDebuffForSmoke(5)
                    || !traceContains(s, "active queue apply bank=0 id=13")
                    || !traceContains(s, "hp " + hpBeforeActiveTick + "->" + afterTickHp)) {
                throw new IllegalStateException(checkpoint + " expected skill24 P13 active tick heal"
                        + " hpBeforeTick=" + hpBeforeActiveTick
                        + " hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                        + " expectedHp=" + afterTickHp
                        + " expectedHeal=" + expectedHeal
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(13)
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postText=" + s.battleP7PostEffectText
                        + " hasDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                        + " trace=" + tailTrace(s, 140));
            }
            assertPhase10AStatusSlots(s, true, "skill24 buff13 after P13 tick",
                    new int[]{25}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("p13_heal_tick_duration2")));

            runtime.debugTickPlayerSourceBuffForSmoke(s, 13);
            int durationAfterSecondTick = runtime.debugPlayerBuffDurationForSmoke(13);
            runtime.debugTickPlayerSourceBuffForSmoke(s, 13);
            if (runtime.debugPlayerHasBuffForSmoke(13)
                    || runtime.debugPlayerBuffDurationForSmoke(13) != 0
                    || durationAfterSecondTick != 1
                    || s.battlePlayerStatusCount != 0
                    || runtime.debugPlayerHasDebuffForSmoke(5)) {
                throw new IllegalStateException(checkpoint + " expected skill24 buff13 to expire and keep debuff clear"
                        + " active=" + runtime.debugPlayerHasBuffForSmoke(13)
                        + " durationAfterSecondTick=" + durationAfterSecondTick
                        + " finalDuration=" + runtime.debugPlayerBuffDurationForSmoke(13)
                        + " statusCount=" + s.battlePlayerStatusCount
                        + " hasDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                        + " trace=" + tailTrace(s, 120));
            }
            writeScenePng(s, new java.io.File(dir, skill24NguoiBaoVeDiaGioiPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            BattleBuffRow buff = VqsvBattleTables.instance().buff(13);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=24 name=Nguoi bao ve Dia Gioi description=No damage; clear debuffs and heal over time.\n"
                    + "aq.c[1][24]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[24]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)) + "\n"
                    + "speffect17=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(17)) + "\n"
                    + "aq.c[6][13]=" + java.util.Arrays.toString(buff.raw) + "\n"
                    + "actorEffect=22 actorSprite=264 actorState=6 actorSide=player\n"
                    + "special=speffect17 AH type1 playerSide\n"
                    + "logic=no damage; game.d.q applies buff13; clearDebuffs; heal=maxHp*20/100="
                    + expectedHeal + "; duration=3.\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " debuff5=true\n"
                    + "afterApply hp=" + afterApplyHp + "/" + s.battlePlayerMaxHp
                    + " enemyHp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=9 buffValue=" + runtime.debugPlayerBuffValueForSmoke(13)
                    + " duration=3 icon=25 durationCell=137 debuff5=false"
                    + " specialType=" + specialType + "\n"
                    + "p13Tick hp=" + hpBeforeActiveTick + "->" + afterTickHp
                    + " duration=2 durationCell=136\n"
                    + "expiry durationAfterSecondTick=" + durationAfterSecondTick
                    + " finalDuration=" + runtime.debugPlayerBuffDurationForSmoke(13)
                    + " statusCount=" + s.battlePlayerStatusCount + "\n"
                    + "battleLab=covered by battle_lab_skill_test_all all-skill table; timeline suite lives in EarthSkill\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 48) + "\n";
            Files.write(new java.io.File(dir,
                            "battle_skill24_nguoi_bao_ve_dia_gioi_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + "->" + afterApplyHp + "->" + afterTickHp
                    + " enemyHp=" + beforeEnemyHp + "->" + s.battleEnemyHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " heal=" + expectedHeal
                    + " debuff5Cleared=true"
                    + " buff13Expired=" + !runtime.debugPlayerHasBuffForSmoke(13)
                    + " special=AH1"
                    + " images=before_wounded_debuff5,actor_u22_start,speffect17_type1,"
                    + "after_apply_cleanse_heal,p13_body_visual_actor22,p13_heal_tick_duration2,expired");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static boolean runSkill23NhamBangTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill23_nham_bang_timeline".equals(checkpoint)) {
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

            Skill23TimelineResult baseline = runSkill23NhamBangSingleTimeline(
                    checkpoint, dir, "baseline_no_debuff1", false, 20260715L);
            Skill23TimelineResult conditional = runSkill23NhamBangSingleTimeline(
                    checkpoint, dir, "conditional_debuff1", true, 20260715L);
            if (conditional.damage <= baseline.damage
                    || conditional.enemyHpAfter >= baseline.enemyHpAfter
                    || !conditional.hadDebuff1
                    || baseline.hadDebuff1) {
                throw new IllegalStateException(checkpoint + " expected skill23 conditional debuff1 branch"
                        + " baselineDamage=" + baseline.damage
                        + " conditionalDamage=" + conditional.damage
                        + " baselineHpAfter=" + baseline.enemyHpAfter
                        + " conditionalHpAfter=" + conditional.enemyHpAfter
                        + " baselineHadDebuff1=" + baseline.hadDebuff1
                        + " conditionalHadDebuff1=" + conditional.hadDebuff1);
            }
            Files.copy(new java.io.File(dir,
                            skill23NhamBangPngName("conditional_debuff1_hp_settled")).toPath(),
                    out.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            BattleSkillRow row = VqsvBattleTables.instance().skill(23);
            BattleDebuffRow debuff1 = VqsvBattleTables.instance().debuff(1);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=23 name=Nham bang description=Earth damage; stronger if target has Me Muoi/debuff1.\n"
                    + "aq.c[1][23]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[23]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(23)) + "\n"
                    + "speffect6=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(6)) + "\n"
                    + "aq.c[7][1]=" + java.util.Arrays.toString(debuff1.raw) + "\n"
                    + "actorEffect=22 actorSprite=264 actorState=0 actorSide=enemy\n"
                    + "special=speffect6 AH type8 targetSide\n"
                    + "formulaNoDebuff1=raw*100/100\n"
                    + "formulaWithDebuff1=raw*250/100\n"
                    + "baseline before hp=" + baseline.beforePlayerHp + "/" + baseline.playerMaxHp
                    + ":" + baseline.beforeEnemyHp + "/" + baseline.enemyMaxHp
                    + " pp=" + baseline.beforePp + "\n"
                    + "baseline damage=" + baseline.damage
                    + " hpAfter=" + baseline.enemyHpAfter + "/" + baseline.enemyMaxHp
                    + " ppAfter=" + baseline.afterPp
                    + " hadDebuff1=" + baseline.hadDebuff1
                    + " specialType=" + baseline.specialType + "\n"
                    + "conditional before hp=" + conditional.beforePlayerHp + "/" + conditional.playerMaxHp
                    + ":" + conditional.beforeEnemyHp + "/" + conditional.enemyMaxHp
                    + " pp=" + conditional.beforePp
                    + " preloadedDebuff1=true\n"
                    + "conditional damage=" + conditional.damage
                    + " hpAfter=" + conditional.enemyHpAfter + "/" + conditional.enemyMaxHp
                    + " ppAfter=" + conditional.afterPp
                    + " hadDebuff1=" + conditional.hadDebuff1
                    + " specialType=" + conditional.specialType + "\n"
                    + "battleLab=covered by battle_lab_skill_test_all all-skill table; timeline suite lives in EarthSkill\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "baselineTraceTail=" + baseline.traceTail + "\n"
                    + "conditionalTraceTail=" + conditional.traceTail + "\n";
            Files.write(new java.io.File(dir, "battle_skill23_nham_bang_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " baselineDamage=" + baseline.damage
                    + " conditionalDamage=" + conditional.damage
                    + " hp=" + baseline.beforePlayerHp + "/" + baseline.playerMaxHp
                    + ":" + baseline.beforeEnemyHp + "/" + baseline.enemyMaxHp
                    + "->" + conditional.enemyHpAfter + "/" + conditional.enemyMaxHp
                    + " pp=" + baseline.beforePp + "->" + conditional.afterPp
                    + " special=AH8"
                    + " images=baseline_no_debuff1_before,baseline_no_debuff1_actor_u22_start,"
                    + "baseline_no_debuff1_speffect6_type8,baseline_no_debuff1_damage_frame,"
                    + "baseline_no_debuff1_hp_settled,conditional_debuff1_before,"
                    + "conditional_debuff1_actor_u22_start,conditional_debuff1_speffect6_type8,"
                    + "conditional_debuff1_damage_frame,conditional_debuff1_hp_settled");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static Skill23TimelineResult runSkill23NhamBangSingleTimeline(
            String checkpoint, java.io.File dir, String label, boolean preloadDebuff1, long seed) throws Exception {
        int skillId = 23;
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        assertSkill23NhamBangSourceRows(s, checkpoint);
        assertSkill23P3BeforeConfirm(s, runtime, checkpoint);
        if (preloadDebuff1) {
            runtime.debugStatusIconForSmoke(s, false, 1, 1, 2, 0, 22);
            if (!runtime.debugEnemyHasDebuffForSmoke(1)) {
                throw new IllegalStateException(checkpoint + " expected skill23 preload to set enemy debuff1"
                        + " trace=" + tailTrace(s, 48));
            }
            assertPhase10AStatusSlots(s, false, "skill23 preloaded debuff1 visible",
                    new int[]{2}, new int[]{136});
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        writeScenePng(s, new java.io.File(dir, skill23NhamBangPngName(label + "_before")));

        runtime.debugSetSourceRandomSeedForSmoke(seed);
        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
        tickUntilBattleP7Phase(s, 1, 160);
        for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
            s.tick();
        }
        assertSkill23P7ActorVisible(s, runtime, checkpoint, preloadDebuff1);
        writeScenePng(s, new java.io.File(dir, skill23NhamBangPngName(label + "_actor_u22_start")));

        for (int i = 0; i < 240 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 8); i++) {
            s.tick();
        }
        if (!s.battleP7SpecialVisible
                || s.battleP7SpecialOnPlayerSide
                || s.battleP7SpecialType != 8
                || s.battleP7SpecialRow.length < 9
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugEnemyHasDebuffForSmoke(1) != preloadDebuff1
                || !traceContainsAll(s, "battle P7 speffect skill=23",
                "chunk=1", "speffect=6", "AH type 8")) {
            throw new IllegalStateException(checkpoint + " expected skill23 speffect6/AH8"
                    + " label=" + label
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                    + " type=" + s.battleP7SpecialType
                    + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " trace=" + tailTrace(s, 96));
        }
        int specialType = s.battleP7SpecialType;
        writeScenePng(s, new java.io.File(dir, skill23NhamBangPngName(label + "_speffect6_type8")));

        tickUntilBattleP7Phase(s, 2, 280);
        int damage = latestTraceDamage(s, "battle P7 damage frame skill=23");
        assertSkill23NhamBangDamageFrame(s, runtime, checkpoint, damage, preloadDebuff1);
        writeScenePng(s, new java.io.File(dir, skill23NhamBangPngName(label + "_damage_frame")));

        int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
        int guard = 0;
        while ("P7".equals(s.battleStateName)
                && s.battleEnemyHp > expectedEnemyHp
                && guard++ < 280) {
            s.tick();
        }
        if (s.battleEnemyHp != expectedEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || runtime.debugEnemyHasDebuffForSmoke(1) != preloadDebuff1
                || !traceContains(s, "battle P7 source n() skill=23")
                || !traceContains(s, "battle P7 actor u.a() start skill=23")
                || !traceContains(s, "battle P7 speffect skill=23")
                || !traceContains(s, "battle P7 damage frame skill=23")
                || traceContains(s, "appliedDebuffId=1")) {
            throw new IllegalStateException(checkpoint + " expected skill23 " + label + " HP to settle"
                    + " state=" + s.battleStateName
                    + " phase=" + s.battleP7Phase
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " expectedHp=" + expectedEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " trace=" + tailTrace(s, 80));
        }
        writeScenePng(s, new java.io.File(dir, skill23NhamBangPngName(label + "_hp_settled")));

        return new Skill23TimelineResult(beforePlayerHp, s.battlePlayerMaxHp,
                beforeEnemyHp, s.battleEnemyMaxHp, beforePp,
                damage, s.battleEnemyHp, runtime.debugPlayerSkillPpForSmoke(0),
                preloadDebuff1, specialType, tailTrace(s, 48));
    }

    private static boolean runSkill22BaoCatTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill22_bao_cat_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 22;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
            assertSkill22BaoCatSourceRows(s, checkpoint);
            assertSkill22P3BeforeConfirm(s, runtime, checkpoint);

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
            int playerAttack = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_ATTACK);
            int enemyDefense = runtime.debugEnemyCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("before")));

            runtime.debugSetSourceRandomSeedForSmoke(20260714L);
            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);
            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            assertSkill22P7ActorVisible(s, runtime, checkpoint);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            int actorCursor = s.battleP7ActorEffectCursor;
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("actor_u22_start")));

            tickUntilBattleP7Phase(s, 2, 240);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=22");
            assertSkill22DamageDebuffFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            boolean critical = s.battleP7DamageCritical;
            int debuffDuration = runtime.debugEnemyDebuffDurationForSmoke(1);
            int debuffSourceSkill = runtime.debugEnemyDebuffSourceSkillForSmoke(1);
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 280) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || runtime.debugEnemyDebuffDurationForSmoke(1) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(1) != 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(1) != 22
                    || !traceContains(s, "SMOKE battle forced damage.debuff roll=0")
                    || !traceContains(s, "battle P7 source n() skill=22")
                    || !traceContains(s, "battle P7 actor u.a() start skill=22")
                    || !traceContains(s, "battle P7 damage frame skill=22")) {
                throw new IllegalStateException(checkpoint + " expected skill22 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(1)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(1)
                        + " trace=" + tailTrace(s, 80));
            }
            assertPhase10AStatusSlots(s, false, "skill22 debuff1 active",
                    new int[]{2}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=1", 800);
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 1
                    || !s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 12
                    || !traceContainsAll(s, "battle P12 active queue visual",
                    "bank=1", "debuff=1", "speffect=14")) {
                throw new IllegalStateException(checkpoint + " expected debuff1 active queue speffect14 AH type12"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("p12_body_visual_type12")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=1", 800);
            if (s.battleEnemyHp != hpBeforeTick
                    || runtime.debugEnemyDebuffDurationForSmoke(1) != 1
                    || runtime.debugEnemyDebuffValueForSmoke(1) != 0
                    || s.battleP7PostEffectVisible) {
                throw new IllegalStateException(checkpoint + " expected skill22 debuff1 tick no HP/stat delta"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " value=" + runtime.debugEnemyDebuffValueForSmoke(1)
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, false, "skill22 debuff1 after tick",
                    new int[]{2}, new int[]{135});
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("tick_noop_duration1")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 1);
            if (runtime.debugEnemyDebuffDurationForSmoke(1) != 0
                    || s.battleEnemyStatusCount != 0
                    || runtime.debugEnemyHasDebuffForSmoke(1)) {
                throw new IllegalStateException(checkpoint + " expected skill22 debuff1 to expire after duration 2"
                        + " active=" + runtime.debugEnemyHasDebuffForSmoke(1)
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 80));
            }
            writeScenePng(s, new java.io.File(dir, skill22BaoCatPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=22 name=Bao cat description=Low Earth damage, 25 percent chance Me Muoi for 2 turns.\n"
                    + "aq.c[1][22]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[22]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)) + "\n"
                    + "aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "speffect14=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(14)) + "\n"
                    + "actorEffect=22 actorSprite=264 actorState=0 actorSide=enemy\n"
                    + "p7Special=none for skill22; debuff body visual appears in P12/P13 as speffect14 AH type12\n"
                    + "formula=raw*50/100; debuffChance=25 forcedRoll=0; debuffDuration=2; debuffTick=noop\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " playerAttack=" + playerAttack
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp
                    + " cursor=" + actorCursor + "\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " critical=" + critical
                    + " debuffDuration=" + debuffDuration
                    + " debuffSourceSkill=" + debuffSourceSkill + "\n"
                    + "hpSettled hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " icon=2 durationCell=136\n"
                    + "tick1 hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                    + " duration=1 durationCell=135 noHpStatDelta=true\n"
                    + "expiry finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "battleLab=covered by battle_lab_skill_test_all all-skill table; timeline suite lives in EarthSkill\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir, "battle_skill22_bao_cat_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuffChance=25 forcedRoll=0"
                    + " actorSprite=264"
                    + " images=before,actor_u22_start,damage_debuff_frame,hp_settled_debuff_active,p12_body_visual_type12,tick_noop_duration1,expired");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static boolean runSkill20HatBuiTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill20_hat_bui_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 20;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
            assertSkill20HatBuiSourceRows(s, checkpoint);
            assertSkill20P3BeforeConfirm(s, runtime, checkpoint);

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
            int playerAttack = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_ATTACK);
            int enemyDefense = runtime.debugEnemyCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            writeScenePng(s, new java.io.File(dir, skill20HatBuiPngName("before")));

            runtime.debugSetNextDamageCritRollForSmoke(99);
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
            assertSkill20P7ActorVisible(s, runtime, checkpoint);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            int actorCursor = s.battleP7ActorEffectCursor;
            writeScenePng(s, new java.io.File(dir, skill20HatBuiPngName("actor_u22_start")));

            tickUntilBattleP7Phase(s, 2, 180);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=20");
            assertSkill20DamageFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            boolean critical = s.battleP7DamageCritical;
            String debuffText = s.battleP7DebuffText;
            String missText = s.battleP7MissText;
            writeScenePng(s, new java.io.File(dir, skill20HatBuiPngName("damage_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 240) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || damage <= 0
                    || !traceContains(s, "battle P7 source n() skill=20")
                    || !traceContains(s, "battle P7 actor u.a() start skill=20")
                    || !traceContains(s, "battle P7 damage frame skill=20")) {
                throw new IllegalStateException(checkpoint + " expected skill20 timeline to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " damage=" + damage
                        + " trace=" + tailTrace(s, 40));
            }
            writeScenePng(s, new java.io.File(dir, skill20HatBuiPngName("hp_settled")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=20 name=Hat Bui description=Low Earth damage; direct hit only.\n"
                    + "aq.c[1][20]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[20]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)) + "\n"
                    + "actorEffect=22 actorSprite=264 actorState=0 actorSide=enemy\n"
                    + "logic=direct damage powerPercent=100; no buff; no debuff; no q() post-effect.\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " playerAttack=" + playerAttack
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp
                    + " cursor=" + actorCursor + "\n"
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
                    + "battleLab=covered by battle_lab_skill_test_all all-skill table; timeline suite lives in EarthSkill\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 28) + "\n";
            Files.write(new java.io.File(dir, "battle_skill20_hat_bui_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " actorSprite=264"
                    + " images=before,actor_u22_start,damage_frame,hp_settled");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static boolean runSkill20HatBuiSourceStageSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill20_hat_bui_source_stage_animation".equals(checkpoint)) {
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
            java.io.File stageDir = new java.io.File(dir, "skill20_hat_bui_source_stage_frames");
            if (!stageDir.exists() && !stageDir.mkdirs()) {
                throw new IllegalStateException("Could not create stage directory " + stageDir);
            }

            Skill20SourceStageResult result = runSkill20HatBuiSourceStage(checkpoint, stageDir);
            writeSkill20SourceStageSheet(result, out);
            java.io.File zoomOut = new java.io.File(dir,
                    "battle_skill20_hat_bui_source_stage_animation_zoom.png");
            writeSkill20SourceStageZoomSheet(result, zoomOut);
            writeSkill20SourceStageDebug(result, new java.io.File(dir,
                    "battle_skill20_hat_bui_source_stage_animation_debug.txt"));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " zoom=" + zoomOut.getPath()
                    + " sourceStages=before,attacker_state1,target_u22,target_hit_state2,settled_idle"
                    + " actorEffect=22 sprite=264 state=0 side=enemy");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static Skill20SourceStageResult runSkill20HatBuiSourceStage(String checkpoint,
                                                                         java.io.File dir) throws Exception {
        int skillId = 20;
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        assertSkill20HatBuiSourceRows(s, checkpoint);
        assertSkill20P3BeforeConfirm(s, runtime, checkpoint);
        runtime.debugSetSourceRandomSeedForSmoke(20260714L + skillId);
        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);

        if (!"P3".equals(s.battleStateName)
                || s.battleP7BaseStatePlayerSide != 0
                || s.battleP7BaseStateEnemySide != 0
                || s.battleP7ActorEffectVisible
                || s.battleP7SpecialVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected clean P3 before skill20"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " special=" + s.battleP7SpecialVisible
                    + " damage=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 32));
        }
        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        java.io.File beforePng = new java.io.File(dir, skill20SourceStagePngName("0_before"));
        writeScenePng(s, beforePng);

        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
        tickUntilBattleP7Phase(s, 1, 120);
        for (int i = 0; i < 20 && s.battleP7BaseStatePlayerSide != 1; i++) {
            s.tick();
        }
        if (s.battleP7BaseStatePlayerSide != 1
                || s.battleP7BaseStateEnemySide != 0
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected attacker state1 before skill20 u22"
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damage=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 64));
        }
        java.io.File attackerPng = new java.io.File(dir, skill20SourceStagePngName("1_attacker_state1"));
        writeScenePng(s, attackerPng);

        for (int i = 0; i < 80; i++) {
            if (s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSourceId == 22
                    && s.battleP7ActorEffectSpriteId == 264
                    && s.battleP7ActorEffectState == 0
                    && !s.battleP7ActorEffectOnPlayerSide) {
                break;
            }
            s.tick();
        }
        assertSkill20P7ActorVisible(s, runtime, checkpoint);
        if (s.battleP7BaseStatePlayerSide != 1
                || s.battleP7BaseStateEnemySide != 0
                || s.battleP7SpecialVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected target-side u22 while attacker state1"
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " actorSource=" + s.battleP7ActorEffectSourceId
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " special=" + s.battleP7SpecialVisible
                    + " damage=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 72));
        }
        int actorCursor = s.battleP7ActorEffectCursor;
        java.io.File targetEffectPng = new java.io.File(dir, skill20SourceStagePngName("2_target_u22"));
        writeScenePng(s, targetEffectPng);

        java.io.File targetEffectMidPng = new java.io.File(dir, skill20SourceStagePngName("2b_target_u22_mid"));
        java.io.File targetEffectLatePng = new java.io.File(dir, skill20SourceStagePngName("2c_target_u22_late"));
        boolean midSaved = false;
        int lateCursor = actorCursor;
        for (int i = 0; i < 80 && "P7".equals(s.battleStateName) && s.battleP7Phase == 1
                && s.battleP7ActorEffectVisible; i++) {
            s.tick();
            if (s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSourceId == 22
                    && s.battleP7ActorEffectCursor > actorCursor) {
                if (!midSaved) {
                    writeScenePng(s, targetEffectMidPng);
                    midSaved = true;
                }
                writeScenePng(s, targetEffectLatePng);
                lateCursor = s.battleP7ActorEffectCursor;
            }
        }
        if (!midSaved) {
            writeScenePng(s, targetEffectMidPng);
            writeScenePng(s, targetEffectLatePng);
        }

        tickUntilBattleP7Phase(s, 2, 260);
        int damage = latestTraceDamage(s, "battle P7 damage frame skill=20");
        assertSkill20DamageFrame(s, runtime, checkpoint, damage);
        if (s.battleP7BaseStatePlayerSide != 1
                || s.battleP7BaseStateEnemySide != 2
                || s.battleP7ActorEffectVisible
                || s.battleP7SpecialVisible
                || !traceContains(s, "hit=true")) {
            throw new IllegalStateException(checkpoint + " expected enemy hit state2 at skill20 damage frame"
                    + " damage=" + damage
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " special=" + s.battleP7SpecialVisible
                    + " trace=" + tailTrace(s, 96));
        }
        String damageText = s.battleP7DamageText;
        java.io.File hitPng = new java.io.File(dir, skill20SourceStagePngName("3_target_hit_state2"));
        writeScenePng(s, hitPng);

        int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
        int guard = 0;
        while ("P7".equals(s.battleStateName) && guard++ < 420) {
            s.tick();
            if (s.battleP7Phase >= 3
                    && !s.battleP7DamageVisible
                    && !s.battleP7ActorEffectVisible
                    && !s.battleP7SpecialVisible
                    && !s.battleP7BaseHiddenPlayerSide
                    && !s.battleP7BaseHiddenEnemySide
                    && s.battleP7BaseStatePlayerSide == 0
                    && s.battleP7BaseStateEnemySide == 0
                    && s.battleEnemyHp == expectedEnemyHp) {
                break;
            }
        }
        if (s.battleP7ActorEffectVisible
                || s.battleP7SpecialVisible
                || s.battleP7BaseHiddenPlayerSide
                || s.battleP7BaseHiddenEnemySide
                || s.battleP7BaseStatePlayerSide != 0
                || s.battleP7BaseStateEnemySide != 0
                || s.battleEnemyHp != expectedEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 44) {
            throw new IllegalStateException(checkpoint + " expected settled idle after skill20"
                    + " phase=" + s.battleP7Phase
                    + " state=" + s.battleStateName
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " hidden=" + s.battleP7BaseHiddenPlayerSide + "/" + s.battleP7BaseHiddenEnemySide
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " special=" + s.battleP7SpecialVisible
                    + " hp=" + s.battleEnemyHp + "/" + expectedEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 96));
        }
        java.io.File settledPng = new java.io.File(dir, skill20SourceStagePngName("4_settled_idle"));
        writeScenePng(s, settledPng);

        return new Skill20SourceStageResult(beforePng, attackerPng, targetEffectPng, hitPng, settledPng,
                targetEffectMidPng, targetEffectLatePng,
                beforePlayerHp, beforeEnemyHp, beforePp, damage, damageText, actorCursor,
                lateCursor, s.battleEnemyHp, runtime.debugPlayerSkillPpForSmoke(0), tailTrace(s, 40));
    }

    private static void writeSkill20SourceStageSheet(Skill20SourceStageResult result,
                                                      java.io.File out) throws java.io.IOException {
        String[] labels = {"before", "attacker state1", "target u22", "u22 mid", "u22 late",
                "target hit state2", "settled idle"};
        java.io.File[] pngs = result.stagePngs();
        int cellW = 240;
        int cellH = 360;
        BufferedImage sheet = new BufferedImage(cellW * 2, cellH * 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(0x101820));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString("Skill 20 Hat Bui source-stage audit", 12, 22);
        g.drawString("aq.c[1][20]=[2,137,549,100,0,45,0,-1,-1,0]", 12, 38);
        g.drawString("effect.mid[20]=[0,0,22,0,-1,-1,0] -> u22 sprite264 state0 on enemy", 12, 54);
        for (int i = 0; i < pngs.length; i++) {
            int x = (i % 2) * cellW;
            int y = 70 + (i / 2) * 185;
            g.setColor(new java.awt.Color(0x26313b));
            g.drawRect(x + 5, y - 18, cellW - 10, 175);
            drawStageThumb(g, pngs[i], x + 12, y, labels[i]);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    private static void writeSkill20SourceStageZoomSheet(Skill20SourceStageResult result,
                                                          java.io.File out) throws java.io.IOException {
        String[] labels = {"before", "atk1", "u22", "u22mid", "u22late", "hit2", "idle"};
        java.io.File[] pngs = result.stagePngs();
        int scale = 3;
        int cropW = 120;
        int cropH = 110;
        int thumbW = cropW * scale;
        int thumbH = cropH * scale;
        int labelW = 126;
        int headerH = 54;
        int sheetW = labelW + labels.length * thumbW + 22;
        int sheetH = headerH + thumbH * 2 + 46;
        BufferedImage sheet = new BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(0x101820));
        g.fillRect(0, 0, sheetW, sheetH);
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString("Skill 20 Hat Bui zoom: player body + enemy body", 12, 20);
        g.drawString("Expected source flow: attacker state1 -> enemy u22/sprite264 frames -> enemy hit state2", 12, 38);
        for (int i = 0; i < labels.length; i++) {
            g.drawString(labels[i], labelW + i * thumbW + 8, headerH - 8);
        }
        g.drawString("player", 14, headerH + 72);
        g.drawString("enemy", 14, headerH + 72 + thumbH);
        for (int stage = 0; stage < pngs.length; stage++) {
            BufferedImage img = ImageIO.read(pngs[stage]);
            drawZoomCrop(g, img, 8, 122, cropW, cropH,
                    labelW + stage * thumbW, headerH, scale);
            drawZoomCrop(g, img, 112, 34, cropW, cropH,
                    labelW + stage * thumbW, headerH + thumbH, scale);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    private static void writeSkill20SourceStageDebug(Skill20SourceStageResult result,
                                                      java.io.File out) throws java.io.IOException {
        String debug = ""
                + "checkpoint=battle_skill20_hat_bui_source_stage_animation\n"
                + "skill=20 name=Hat Bui / H\u1ea5t b\u1ee5i\n"
                + "aq.c[1][20]=[2,137,549,100,0,45,0,-1,-1,0]\n"
                + "effect.mid[20]=[0,0,22,0,-1,-1,0]\n"
                + "sourceFacts=game.d case7 calls n(); effect chunk side=0 means target actor; "
                + "game.b.a(short,byte) creates u=new ah([effectId,state,dir]) at target i,j; "
                + "ah maps source effect 22 to sprite 264.\n"
                + "expectedStages=before -> attacker state1 -> target u22 sprite264 state0 -> "
                + "target hit state2 + damage -> settled idle\n"
                + "before hp=" + result.beforePlayerHp + ":" + result.beforeEnemyHp
                + " pp=" + result.beforePp + "\n"
                + "actor cursor=" + result.actorCursor + "->" + result.actorLateCursor
                + " side=enemy sourceId=22 sprite=264 state=0\n"
                + "damage=" + result.damage + " text=" + result.damageText + "\n"
                + "settled enemyHp=" + result.settledEnemyHp + " pp=" + result.settledPp + "\n"
                + "status=SOURCE-STAGE AUDITED; visual parity still depends on shared sprite/orientation renderer.\n"
                + "traceTail=" + result.traceTail + "\n";
        Files.write(out.toPath(), debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static void drawStageThumb(Graphics2D g, java.io.File png, int x, int y, String label)
            throws java.io.IOException {
        BufferedImage img = ImageIO.read(png);
        g.drawImage(img, x, y + 16, 96, 128, null);
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString(label, x, y + 12);
    }

    private static void drawZoomCrop(Graphics2D g, BufferedImage img,
                                      int sx, int sy, int sw, int sh,
                                      int dx, int dy, int scale) {
        int safeX = Math.max(0, Math.min(img.getWidth() - 1, sx));
        int safeY = Math.max(0, Math.min(img.getHeight() - 1, sy));
        int safeW = Math.max(1, Math.min(sw, img.getWidth() - safeX));
        int safeH = Math.max(1, Math.min(sh, img.getHeight() - safeY));
        g.drawImage(img,
                dx, dy, dx + safeW * scale, dy + safeH * scale,
                safeX, safeY, safeX + safeW, safeY + safeH,
                null);
        g.setColor(new java.awt.Color(0x536777));
        g.drawRect(dx, dy, safeW * scale - 1, safeH * scale - 1);
    }

    private static String skill20SourceStagePngName(String suffix) {
        return "battle_skill20_hat_bui_source_stage_" + suffix + ".png";
    }

    private static final class Skill20SourceStageResult {
        final java.io.File beforePng;
        final java.io.File attackerPng;
        final java.io.File targetEffectPng;
        final java.io.File targetEffectMidPng;
        final java.io.File targetEffectLatePng;
        final java.io.File hitPng;
        final java.io.File settledPng;
        final int beforePlayerHp;
        final int beforeEnemyHp;
        final int beforePp;
        final int damage;
        final String damageText;
        final int actorCursor;
        final int actorLateCursor;
        final int settledEnemyHp;
        final int settledPp;
        final String traceTail;

        Skill20SourceStageResult(java.io.File beforePng, java.io.File attackerPng,
                                 java.io.File targetEffectPng, java.io.File hitPng,
                                 java.io.File settledPng, java.io.File targetEffectMidPng,
                                 java.io.File targetEffectLatePng, int beforePlayerHp, int beforeEnemyHp,
                                 int beforePp, int damage, String damageText, int actorCursor,
                                 int actorLateCursor, int settledEnemyHp, int settledPp, String traceTail) {
            this.beforePng = beforePng;
            this.attackerPng = attackerPng;
            this.targetEffectPng = targetEffectPng;
            this.targetEffectMidPng = targetEffectMidPng;
            this.targetEffectLatePng = targetEffectLatePng;
            this.hitPng = hitPng;
            this.settledPng = settledPng;
            this.beforePlayerHp = beforePlayerHp;
            this.beforeEnemyHp = beforeEnemyHp;
            this.beforePp = beforePp;
            this.damage = damage;
            this.damageText = damageText;
            this.actorCursor = actorCursor;
            this.actorLateCursor = actorLateCursor;
            this.settledEnemyHp = settledEnemyHp;
            this.settledPp = settledPp;
            this.traceTail = traceTail;
        }

        java.io.File[] stagePngs() {
            return new java.io.File[]{beforePng, attackerPng, targetEffectPng, targetEffectMidPng,
                    targetEffectLatePng, hitPng, settledPng};
        }
    }

    private static boolean runSkill21ThoThuanTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill21_tho_thuan_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 21;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
            runtime.debugSetPlayerDefenseForSmoke(s, 100);
            assertSkill21ThoThuanSourceRows(s, checkpoint);
            assertSkill21P3BeforeConfirm(s, runtime, checkpoint);

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
            int playerAttack = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_ATTACK);
            int playerBaseDefense = runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE);
            int playerCurrentDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            int enemyDefense = runtime.debugEnemyCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("before")));

            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextP7HitRollForSmoke(99);
            for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
                s.press0();
                s.tick();
            }
            tickUntilBattleState(s, "P7", 120);
            tickUntilBattleP7Phase(s, 1, 160);
            for (int i = 0; i < 24 && !s.battleP7ActorEffectVisible; i++) {
                s.tick();
            }
            assertSkill21P7ActorVisible(s, runtime, checkpoint);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            int actorCursor = s.battleP7ActorEffectCursor;
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("actor_u22_start")));

            for (int i = 0; i < 240 && !s.battleP7SpecialVisible; i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || !s.battleP7SpecialOnPlayerSide
                    || !traceContains(s, "battle P7 speffect skill=21")
                    || !traceContains(s, "speffect=5")) {
                throw new IllegalStateException(checkpoint + " expected skill21 speffect5 on player side"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " specialType=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 72));
            }
            int specialType = s.battleP7SpecialType;
            short[] specialRow = java.util.Arrays.copyOf(s.battleP7SpecialRow, s.battleP7SpecialRow.length);
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("speffect5")));

            tickUntilBattleP7Phase(s, 2, 220);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=21");
            assertSkill21DamageFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            boolean critical = s.battleP7DamageCritical;
            String debuffText = s.battleP7DebuffText;
            String missText = s.battleP7MissText;
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("damage_frame")));

            tickUntilBattleP7Phase(s, 3, 360);
            for (int i = 0; i < 40 && !s.battleP7PostEffectVisible; i++) {
                s.tick();
            }
            int expectedBuffValue = playerBaseDefense * 10 / 100;
            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            if (s.battleEnemyHp != expectedEnemyHp
                    || !s.battleP7PostEffectVisible
                    || !s.battleP7PostEffectPlayerSide
                    || s.battleP7PostEffectText.isEmpty()
                    || !runtime.debugPlayerHasBuffForSmoke(4)
                    || runtime.debugPlayerBuffValueForSmoke(4) != expectedBuffValue
                    || runtime.debugPlayerBuffDurationForSmoke(4) != 2
                    || runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE) != playerBaseDefense
                    || runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                    != playerBaseDefense + expectedBuffValue
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || !traceContains(s, "game.d.q postEffect skill=21")
                    || !traceContains(s, "buffId=4")
                    || !traceContains(s, "selfTarget=true")) {
                throw new IllegalStateException(checkpoint + " expected skill21 post-effect buff4"
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " postSidePlayer=" + s.battleP7PostEffectPlayerSide
                        + " postText=" + s.battleP7PostEffectText
                        + " hasBuff4=" + runtime.debugPlayerHasBuffForSmoke(4)
                        + " value=" + runtime.debugPlayerBuffValueForSmoke(4)
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(4)
                        + " defense=" + runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + "->" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, true, "skill21 buff4 after apply",
                    new int[]{16}, new int[]{136});
            String postText = s.battleP7PostEffectText;
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("after_apply_buff_icon")));

            runtime.debugTickPlayerSourceBuffForSmoke(s, 4);
            if (!runtime.debugPlayerHasBuffForSmoke(4)
                    || runtime.debugPlayerBuffDurationForSmoke(4) != 1
                    || runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                    != playerBaseDefense + expectedBuffValue * 2) {
                throw new IllegalStateException(checkpoint + " expected skill21 buff4 first tick"
                        + " active=" + runtime.debugPlayerHasBuffForSmoke(4)
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(4)
                        + " defense=" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " expected=" + (playerBaseDefense + expectedBuffValue * 2)
                        + " trace=" + tailTrace(s, 64));
            }
            assertPhase10AStatusSlots(s, true, "skill21 buff4 after tick1",
                    new int[]{16}, new int[]{135});
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("tick1_duration1")));

            runtime.debugTickPlayerSourceBuffForSmoke(s, 4);
            if (runtime.debugPlayerHasBuffForSmoke(4)
                    || runtime.debugPlayerBuffDurationForSmoke(4) != 0
                    || runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != playerBaseDefense
                    || s.battlePlayerStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected skill21 buff4 expiry"
                        + " active=" + runtime.debugPlayerHasBuffForSmoke(4)
                        + " duration=" + runtime.debugPlayerBuffDurationForSmoke(4)
                        + " defense=" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                        + " baseDefense=" + playerBaseDefense
                        + " statusCount=" + s.battlePlayerStatusCount
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill21ThoThuanPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
            BattleBuffRow buff = VqsvBattleTables.instance().buff(4);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=21 name=Tho thuan description=Earth raw damage plus self defense buff.\n"
                    + "aq.c[1][21]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[21]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)) + "\n"
                    + "aq.c[6][4]=" + java.util.Arrays.toString(buff.raw) + "\n"
                    + "speffect5=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(5)) + "\n"
                    + "actorEffect=22 actorSprite=264 actorState=0 actorSide=enemy\n"
                    + "specialType=" + specialType
                    + " specialRow=" + java.util.Arrays.toString(specialRow)
                    + " specialSide=player\n"
                    + "logic=raw/default damage; game.d.q applies self buff4;"
                    + " buffValue=baseDefense*skill[8]/100="
                    + playerBaseDefense + "*10/100=" + expectedBuffValue + ".\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " attack=" + playerAttack
                    + " playerDefense=" + playerCurrentDefense
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp
                    + " cursor=" + actorCursor + "\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " critical=" + critical
                    + " debuffText=" + debuffText
                    + " missText=" + missText + "\n"
                    + "afterApply enemyHp=" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " postText=" + postText
                    + " defense=" + playerBaseDefense + "->" + (playerBaseDefense + expectedBuffValue)
                    + " buffValue=" + expectedBuffValue
                    + " duration=2 icon=16 durationCell=136 pp=44\n"
                    + "tick1 defense=" + (playerBaseDefense + expectedBuffValue * 2)
                    + " duration=1 durationCell=135\n"
                    + "expired defense=" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(4)
                    + " statusCount=" + s.battlePlayerStatusCount + "\n"
                    + "battleLab=covered by battle_lab_skill_test_all all-skill table; timeline suite lives in EarthSkill\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 36) + "\n";
            Files.write(new java.io.File(dir, "battle_skill21_tho_thuan_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " buff4Value=" + expectedBuffValue
                    + " defense=" + playerBaseDefense + "->" + (playerBaseDefense + expectedBuffValue)
                    + "->" + (playerBaseDefense + expectedBuffValue * 2)
                    + "->" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static void assertSkill20HatBuiSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(20);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(20);
        byte[] expectedEffect = new byte[]{0, 0, 22, 0, -1, -1, 0};
        if (row == null
                || row.elementFamily != 2
                || row.nameTextId != 137
                || row.descriptionTextId != 549
                || row.powerPercent != 100
                || row.ppMax != 45
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != -1
                || row.targetSide != 0
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill20 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill20 Hat Bui source rows"
                + " aq.c[1][20]=" + java.util.Arrays.toString(row.raw)
                + " effect.mid[20]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill20"));
    }

    private static void assertSkill21ThoThuanSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(21);
        BattleBuffRow buff = VqsvBattleTables.instance().buff(4);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(21);
        short[] speffect5 = VqsvBattleAnimationTables.instance().speffectRow(5);
        byte[] expectedEffect = new byte[]{0, 0, 22, 1, -1, -1, 0,
                1, 1, 5, 0, -1, -1, 0,
                1, 0, 22, 2, 0, -1, 0};
        if (row == null
                || buff == null
                || row.elementFamily != 2
                || row.nameTextId != 138
                || row.descriptionTextId != 550
                || row.powerPercent != 80
                || row.ppMax != 45
                || row.effectMode != 1
                || row.effectId != 4
                || row.chanceOrParam != 10
                || row.targetSide != 0
                || buff.raw.length < 5
                || buff.raw[0] != 337
                || buff.raw[1] != 352
                || buff.raw[2] != 2
                || buff.raw[3] != -1
                || buff.raw[4] != -1
                || !java.util.Arrays.equals(effect, expectedEffect)
                || speffect5.length == 0) {
            throw new IllegalStateException(checkpoint + " skill21 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " buff4=" + (buff == null ? "null" : java.util.Arrays.toString(buff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect5=" + java.util.Arrays.toString(speffect5));
        }
        s.sourceStateTrace.add("SMOKE verified skill21 Tho thuan source rows"
                + " aq.c[1][21]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[6][4]=" + java.util.Arrays.toString(buff.raw)
                + " effect.mid[21]=" + java.util.Arrays.toString(effect)
                + " speffect5=" + java.util.Arrays.toString(speffect5)
                + " name=" + row.name("skill21")
                + " buffName=" + buff.name("buff4"));
    }

    private static void assertSkill22BaoCatSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(22);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(22);
        byte[] expectedEffect = new byte[]{0, 0, 22, 3, -1, -1, 0,
                0, 0, 22, 4, -1, -1, 1};
        short[] speffect14 = VqsvBattleAnimationTables.instance().speffectRow(14);
        if (row == null
                || row.elementFamily != 2
                || row.nameTextId != 139
                || row.descriptionTextId != 551
                || row.powerPercent != 50
                || row.ppMax != 45
                || row.effectMode != 2
                || row.effectId != 1
                || row.chanceOrParam != 25
                || row.targetSide != 0
                || debuff == null
                || debuff.duration != 2
                || speffect14.length == 0
                || speffect14[0] != 12
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill22 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect14=" + java.util.Arrays.toString(speffect14)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill22 Bao cat source rows"
                + " aq.c[1][22]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[22]=" + java.util.Arrays.toString(effect)
                + " speffect14=" + java.util.Arrays.toString(speffect14)
                + " name=" + row.name("skill22")
                + " debuffName=" + debuff.name("debuff1"));
    }

    private static void assertSkill23NhamBangSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(23);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(23);
        byte[] expectedEffect = new byte[]{0, 0, 22, 5, -1, -1, 0,
                0, 1, 6, 0, -1, -1, 0};
        short[] speffect6 = VqsvBattleAnimationTables.instance().speffectRow(6);
        if (row == null
                || row.elementFamily != 2
                || row.nameTextId != 140
                || row.descriptionTextId != 552
                || row.powerPercent != 100
                || row.ppMax != 30
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != 250
                || row.targetSide != 0
                || debuff == null
                || debuff.duration != 2
                || speffect6.length == 0
                || speffect6[0] != 8
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill23 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff1=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect6=" + java.util.Arrays.toString(speffect6)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill23 Nham bang source rows"
                + " aq.c[1][23]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[23]=" + java.util.Arrays.toString(effect)
                + " speffect6=" + java.util.Arrays.toString(speffect6)
                + " name=" + row.name("skill23")
                + " debuffName=" + debuff.name("debuff1"));
    }

    private static void assertSkill24NguoiBaoVeDiaGioiSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(24);
        BattleBuffRow buff = VqsvBattleTables.instance().buff(13);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(24);
        byte[] expectedEffect = new byte[]{0, 0, 22, 6, -1, -1, 0,
                0, 1, 17, 0, -1, -1, 0};
        short[] speffect17 = VqsvBattleAnimationTables.instance().speffectRow(17);
        if (row == null
                || row.elementFamily != 2
                || row.nameTextId != 141
                || row.descriptionTextId != 553
                || row.powerPercent != 0
                || row.ppMax != 10
                || row.effectMode != 1
                || row.effectId != 13
                || row.chanceOrParam != -1
                || row.targetSide != 1
                || buff == null
                || buff.raw.length < 5
                || buff.raw[0] != 346
                || buff.raw[1] != 361
                || buff.duration != 3
                || buff.raw[3] != 20
                || speffect17.length == 0
                || speffect17[0] != 1
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill24 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " buff13=" + (buff == null ? "null" : java.util.Arrays.toString(buff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect17=" + java.util.Arrays.toString(speffect17)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill24 Nguoi bao ve Dia Gioi source rows"
                + " aq.c[1][24]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[6][13]=" + java.util.Arrays.toString(buff.raw)
                + " effect.mid[24]=" + java.util.Arrays.toString(effect)
                + " speffect17=" + java.util.Arrays.toString(speffect17)
                + " name=" + row.name("skill24")
                + " buffName=" + buff.name("buff13"));
    }

    private static void assertEarthSkills25To29SourceRows(String checkpoint) {
        assertEarthSkillSourceRow(checkpoint, 25, 142, 554, 0, 1, 10, 1, 14, -1, 1,
                new byte[]{0, 1, 4, 0, -1, -1, 0, 0, 1, 17, 0, -1, -1, 0});
        assertEarthSkillSourceRow(checkpoint, 26, 143, 555, 150, 2, 30, 0, -1, -1, 0,
                new byte[]{0, 0, 22, 6, -1, -1, 0, 0, 1, 6, 0, -1, -1, 0});
        assertEarthSkillSourceRow(checkpoint, 27, 144, 556, 100, 2, 30, 1, 4, 10, 0,
                new byte[]{0, 0, 22, 7, -1, -1, 0,
                        1, 0, 32, 0, 0, -1, 0,
                        1, 1, 7, 0, -1, -1, 0});
        assertEarthSkillSourceRow(checkpoint, 28, 145, 557, 150, 3, 15, 2, 1, 25, 0,
                new byte[]{0, 0, 22, 5, -1, -1, 0, 0, 0, 22, 4, -1, -1, 1});
        assertEarthSkillSourceRow(checkpoint, 29, 146, 558, 180, 3, 15, 0, -1, 300, 0,
                new byte[]{0, 0, 22, 8, -1, -1, 0});

        BattleBuffRow buff4 = VqsvBattleTables.instance().buff(4);
        BattleBuffRow buff14 = VqsvBattleTables.instance().buff(14);
        BattleDebuffRow debuff1 = VqsvBattleTables.instance().debuff(1);
        short[] speffect4 = VqsvBattleAnimationTables.instance().speffectRow(4);
        short[] speffect6 = VqsvBattleAnimationTables.instance().speffectRow(6);
        short[] speffect7 = VqsvBattleAnimationTables.instance().speffectRow(7);
        short[] speffect17 = VqsvBattleAnimationTables.instance().speffectRow(17);
        if (buff4 == null || buff4.raw[0] != 337 || buff4.raw[2] != 2
                || buff14 == null || buff14.raw[0] != 347 || buff14.duration != 3
                || debuff1 == null || debuff1.duration != 2
                || speffect4.length == 0 || speffect4[0] != 7
                || speffect6.length == 0 || speffect6[0] != 8
                || speffect7.length == 0 || speffect7[0] != 9
                || speffect17.length == 0 || speffect17[0] != 1) {
            throw new IllegalStateException(checkpoint + " earth skill 25..29 status/effect table mismatch"
                    + " buff4=" + (buff4 == null ? "null" : java.util.Arrays.toString(buff4.raw))
                    + " buff14=" + (buff14 == null ? "null" : java.util.Arrays.toString(buff14.raw))
                    + " debuff1=" + (debuff1 == null ? "null" : java.util.Arrays.toString(debuff1.raw))
                    + " speffect4=" + java.util.Arrays.toString(speffect4)
                    + " speffect6=" + java.util.Arrays.toString(speffect6)
                    + " speffect7=" + java.util.Arrays.toString(speffect7)
                    + " speffect17=" + java.util.Arrays.toString(speffect17));
        }
    }

    private static void assertEarthSkillSourceRow(String checkpoint, int skillId,
                                                  int nameTextId, int descriptionTextId,
                                                  int powerPercent, int tier, int ppMax,
                                                  int effectMode, int effectId,
                                                  int chanceOrParam, int targetSide,
                                                  byte[] expectedEffect) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        if (row == null
                || row.elementFamily != 2
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

    private static EarthSkillCaseResult runEarthSkill25To29Case(int skillId, String slug,
                                                                 int expectedPlayerBuffId,
                                                                 int expectedEnemyDebuffId,
                                                                 boolean preloadDebuff1,
                                                                 java.io.File dir,
                                                                 boolean conditionalLabel)
            throws java.io.IOException {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3BeforeConfirm(s, skillId);
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        if (row == null) {
            throw new IllegalStateException("Missing skill row " + skillId);
        }
        if (preloadDebuff1) {
            runtime.debugEnemySourceDebuffForSmoke(s, 1, 0, 22);
            if (!runtime.debugEnemyHasDebuffForSmoke(1)) {
                throw new IllegalStateException("Expected skill" + skillId + " preload debuff1");
            }
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        String prefix = "battle_skill" + skillId + "_" + slug
                + (conditionalLabel ? "_debuff1" : "") + "_timeline_";
        writeScenePng(s, new java.io.File(dir, prefix + "before.png"));

        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        runtime.debugSetNextDamageDebuffRollForSmoke(0);
        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 160);
        tickUntilBattleP7Phase(s, 1, 180);
        for (int i = 0; i < 40
                && !s.battleP7ActorEffectVisible
                && !s.battleP7SpecialVisible; i++) {
            s.tick();
        }
        assertEarthSkill25To29FirstVisual(s, runtime, skillId, beforeEnemyHp, beforePp);
        writeScenePng(s, new java.io.File(dir, prefix + "effect_start.png"));

        int damage = 0;
        if (row.powerPercent > 0) {
            tickUntilBattleP7Phase(s, 2, 360);
            damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
            if (damage <= 0) {
                throw new IllegalStateException("Expected skill" + skillId + " to apply damage"
                        + " damage=" + damage
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " beforeEnemyHp=" + beforeEnemyHp
                        + " trace=" + tailTrace(s, 120));
            }
        }

        int guard = 0;
        while ("P7".equals(s.battleStateName) && s.battleP7Phase < 3 && guard++ < 520) {
            s.tick();
        }
        for (int i = 0; i < 12; i++) {
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
                    + " trace=" + tailTrace(s, 120));
        }
        if (expectedEnemyDebuffId >= 0 && !enemyDebuffActive) {
            throw new IllegalStateException("Expected skill" + skillId + " enemy debuff"
                    + " id=" + expectedEnemyDebuffId
                    + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId)
                    + " trace=" + tailTrace(s, 120));
        }
        if (skillId == 25 && s.battleEnemyHp != beforeEnemyHp) {
            throw new IllegalStateException("Expected skill25 no damage"
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " beforeEnemyHp=" + beforeEnemyHp);
        }
        if (runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1) {
            throw new IllegalStateException("Expected skill" + skillId + " PP to decrement by 1"
                    + " beforePp=" + beforePp
                    + " afterPp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 80));
        }
        writeScenePng(s, new java.io.File(dir, prefix + "result.png"));

        EarthSkillCaseResult result = new EarthSkillCaseResult(skillId, row.name("skill" + skillId),
                beforePlayerHp, s.battlePlayerHp,
                beforeEnemyHp, s.battleEnemyHp,
                beforePp, runtime.debugPlayerSkillPpForSmoke(0),
                damage,
                expectedPlayerBuffId, playerBuffActive,
                expectedPlayerBuffId >= 0 ? runtime.debugPlayerBuffDurationForSmoke(expectedPlayerBuffId) : 0,
                expectedEnemyDebuffId, enemyDebuffActive,
                expectedEnemyDebuffId >= 0 ? runtime.debugEnemyDebuffDurationForSmoke(expectedEnemyDebuffId) : 0,
                preloadDebuff1,
                java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(skillId)));
        s.sourceStateTrace.add("SMOKE verified earth skill" + skillId + " closeout " + result.describe());
        return result;
    }

    private static void assertEarthSkill25To29FirstVisual(VqsvIntroDemo.Scene s,
                                                           SourceBattleRuntime runtime,
                                                           int skillId,
                                                           int beforeEnemyHp,
                                                           int beforePp) {
        boolean ok;
        if (skillId == 25) {
            ok = s.battleP7SpecialVisible
                    && s.battleP7SpecialType == 7
                    && !s.battleP7ActorEffectVisible
                    && !traceContains(s, "battle P7 damage frame skill=25");
        } else {
            int expectedState;
            if (skillId == 26) {
                expectedState = 6;
            } else if (skillId == 27) {
                expectedState = 7;
            } else if (skillId == 28) {
                expectedState = 5;
            } else {
                expectedState = 8;
            }
            ok = s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSpriteId == 264
                    && s.battleP7ActorEffectState == expectedState
                    && !s.battleP7ActorEffectOnPlayerSide;
        }
        if (!ok
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != beforePp - 1
                || !traceContains(s, "battle P7 source n() skill=" + skillId)) {
            throw new IllegalStateException("Expected earth skill" + skillId + " first visual from effect.mid"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " specialType=" + s.battleP7SpecialType
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 96));
        }
    }

    private static void assertSkill20P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                     String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 20
                || runtime.debugPlayerSkillPpForSmoke(0) != 45
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill20"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static void assertSkill21P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                     String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 21
                || runtime.debugPlayerSkillPpForSmoke(0) != 45
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible
                || runtime.debugPlayerHasBuffForSmoke(4)
                || runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE) != 100
                || runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 100) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill21"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " hasBuff4=" + runtime.debugPlayerHasBuffForSmoke(4)
                    + " defense=" + runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE)
                    + "->" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                    + " trace=" + tailTrace(s, 32));
        }
    }

    private static void assertSkill22P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                     String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 22
                || runtime.debugPlayerSkillPpForSmoke(0) != 45
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible
                || runtime.debugEnemyHasDebuffForSmoke(1)) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill22"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " trace=" + tailTrace(s, 32));
        }
    }

    private static void assertSkill23P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                     String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 23
                || runtime.debugPlayerSkillPpForSmoke(0) != 30
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill23"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 32));
        }
    }

    private static void assertSkill24P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                     String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 24
                || runtime.debugPlayerSkillPpForSmoke(0) != 10
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible
                || runtime.debugPlayerHasBuffForSmoke(13)) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill24"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " enemyHp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " hasBuff13=" + runtime.debugPlayerHasBuffForSmoke(13)
                    + " trace=" + tailTrace(s, 32));
        }
    }

    private static void assertSkill20P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 264
                || s.battleP7ActorEffectState != 0
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || !traceContains(s, "battle P7 source n() skill=20")
                || !traceContains(s, "id=22")
                || !traceContains(s, "param=0")
                || !traceContains(s, "battle P7 actor u.a() start skill=20")
                || traceContains(s, "battle P7 damage frame skill=20")) {
            throw new IllegalStateException(checkpoint + " expected skill20 actor effect"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorCursor=" + s.battleP7ActorEffectCursor
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 44));
        }
    }

    private static void assertSkill21P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 264
                || s.battleP7ActorEffectState != 1
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || runtime.debugPlayerHasBuffForSmoke(4)
                || !traceContains(s, "battle P7 source n() skill=21")
                || !traceContains(s, "id=22")
                || !traceContains(s, "param=1")
                || !traceContains(s, "battle P7 actor u.a() start skill=21")
                || traceContains(s, "battle P7 damage frame skill=21")) {
            throw new IllegalStateException(checkpoint + " expected skill21 actor effect"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorCursor=" + s.battleP7ActorEffectCursor
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasBuff4=" + runtime.debugPlayerHasBuffForSmoke(4)
                    + " trace=" + tailTrace(s, 56));
        }
    }

    private static void assertSkill22P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 264
                || s.battleP7ActorEffectState != 3
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || runtime.debugEnemyHasDebuffForSmoke(1)
                || !traceContains(s, "battle P7 source n() skill=22")
                || !traceContains(s, "id=22")
                || !traceContains(s, "param=3")
                || !traceContains(s, "battle P7 actor u.a() start skill=22")
                || traceContains(s, "battle P7 speffect skill=22")
                || traceContains(s, "battle P7 damage frame skill=22")) {
            throw new IllegalStateException(checkpoint + " expected skill22 actor effect only before damage"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorCursor=" + s.battleP7ActorEffectCursor
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " trace=" + tailTrace(s, 64));
        }
    }

    private static void assertSkill23P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint, boolean preloadDebuff1) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 264
                || s.battleP7ActorEffectState != 5
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || runtime.debugEnemyHasDebuffForSmoke(1) != preloadDebuff1
                || !traceContains(s, "battle P7 source n() skill=23")
                || !traceContains(s, "id=22")
                || !traceContains(s, "param=5")
                || !traceContains(s, "battle P7 actor u.a() start skill=23")
                || traceContains(s, "battle P7 damage frame skill=23")) {
            throw new IllegalStateException(checkpoint + " expected skill23 actor effect before special/damage"
                    + " preloadDebuff1=" + preloadDebuff1
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorCursor=" + s.battleP7ActorEffectCursor
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " trace=" + tailTrace(s, 80));
        }
    }

    private static void assertSkill24P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint, int startHp) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 264
                || s.battleP7ActorEffectState != 6
                || !s.battleP7ActorEffectOnPlayerSide
                || s.battlePlayerHp != startHp
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 9
                || !runtime.debugPlayerHasDebuffForSmoke(5)
                || runtime.debugPlayerHasBuffForSmoke(13)
                || !traceContains(s, "battle P7 source n() skill=24")
                || !traceContains(s, "id=22")
                || !traceContains(s, "param=6")
                || !traceContains(s, "battle P7 actor u.a() start skill=24")
                || traceContains(s, "battle P7 damage frame skill=24")) {
            throw new IllegalStateException(checkpoint + " expected skill24 player-side actor before heal/cleanse"
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " actorSprite=" + s.battleP7ActorEffectSpriteId
                    + " actorState=" + s.battleP7ActorEffectState
                    + " actorCursor=" + s.battleP7ActorEffectCursor
                    + " actorSidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " playerHp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + " enemyHp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasDebuff5=" + runtime.debugPlayerHasDebuffForSmoke(5)
                    + " hasBuff13=" + runtime.debugPlayerHasBuffForSmoke(13)
                    + " trace=" + tailTrace(s, 96));
        }
    }

    private static void assertSkill20DamageFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                 String checkpoint, int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || !traceContains(s, "battle P7 damage frame skill=20")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=-1")) {
            throw new IllegalStateException(checkpoint + " expected skill20 direct damage frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " trace=" + tailTrace(s, 48));
        }
    }

    private static void assertSkill21DamageFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                 String checkpoint, int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerHasBuffForSmoke(4)
                || !traceContains(s, "battle P7 damage frame skill=21")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=-1")) {
            throw new IllegalStateException(checkpoint + " expected skill21 raw damage frame before q() buff"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " hasBuff4=" + runtime.debugPlayerHasBuffForSmoke(4)
                    + " trace=" + tailTrace(s, 72));
        }
    }

    private static void assertSkill22DamageDebuffFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                       String checkpoint, int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || !runtime.debugEnemyHasDebuffForSmoke(1)
                || runtime.debugEnemyDebuffDurationForSmoke(1) != 2
                || runtime.debugEnemyDebuffValueForSmoke(1) != 0
                || runtime.debugEnemyDebuffSourceSkillForSmoke(1) != 22
                || !traceContains(s, "SMOKE battle forced damage.debuff roll=0")
                || !traceContains(s, "battle P7 damage frame skill=22")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=1")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill22 damage/debuff frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                    + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(1)
                    + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(1)
                    + " trace=" + tailTrace(s, 80));
        }
    }

    private static void assertSkill23NhamBangDamageFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                         String checkpoint, int damage, boolean preloadDebuff1) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugEnemyHasDebuffForSmoke(1) != preloadDebuff1
                || !traceContains(s, "battle P7 damage frame skill=23")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=-1")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill23 conditional damage frame"
                    + " preloadDebuff1=" + preloadDebuff1
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " trace=" + tailTrace(s, 96));
        }
    }

    private static void assertPhase10AStatusSlots(VqsvIntroDemo.Scene s, boolean playerSide,
                                                  String label, int[] expectedIcons, int[] expectedDurations) {
        VqsvSmokeHarness.assertPhase10AStatusSlots(s, playerSide, label, expectedIcons, expectedDurations);
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

    private static void tickUntilTraceContains(VqsvIntroDemo.Scene s, String needle, int maxTicks) {
        VqsvSmokeHarness.tickUntilTraceContains(s, needle, maxTicks);
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

    private static void writeScenePngForCheckpointSummary(java.io.File out,
                                                          EarthSkillCaseResult[] results)
            throws java.io.IOException {
        java.io.File parent = out.getParentFile();
        if (parent == null) {
            parent = new java.io.File(".");
        }
        String lastResultName = "battle_skill"
                + results[results.length - 1].skillId
                + "_tho_chi_loan_vu_debuff1_timeline_result.png";
        java.nio.file.Files.copy(new java.io.File(parent, lastResultName).toPath(), out.toPath(),
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

    private static String skill20HatBuiPngName(String suffix) {
        return "battle_skill20_hat_bui_timeline_" + suffix + ".png";
    }

    private static String skill21ThoThuanPngName(String suffix) {
        return "battle_skill21_tho_thuan_timeline_" + suffix + ".png";
    }

    private static String skill22BaoCatPngName(String suffix) {
        return "battle_skill22_bao_cat_timeline_" + suffix + ".png";
    }

    private static String skill23NhamBangPngName(String suffix) {
        return "battle_skill23_nham_bang_timeline_" + suffix + ".png";
    }

    private static String skill24NguoiBaoVeDiaGioiPngName(String suffix) {
        return "battle_skill24_nguoi_bao_ve_dia_gioi_timeline_" + suffix + ".png";
    }

    private static final class Skill23TimelineResult {
        final int beforePlayerHp;
        final int playerMaxHp;
        final int beforeEnemyHp;
        final int enemyMaxHp;
        final int beforePp;
        final int damage;
        final int enemyHpAfter;
        final int afterPp;
        final boolean hadDebuff1;
        final int specialType;
        final String traceTail;

        Skill23TimelineResult(int beforePlayerHp, int playerMaxHp,
                              int beforeEnemyHp, int enemyMaxHp, int beforePp,
                              int damage, int enemyHpAfter, int afterPp,
                              boolean hadDebuff1, int specialType, String traceTail) {
            this.beforePlayerHp = beforePlayerHp;
            this.playerMaxHp = playerMaxHp;
            this.beforeEnemyHp = beforeEnemyHp;
            this.enemyMaxHp = enemyMaxHp;
            this.beforePp = beforePp;
            this.damage = damage;
            this.enemyHpAfter = enemyHpAfter;
            this.afterPp = afterPp;
            this.hadDebuff1 = hadDebuff1;
            this.specialType = specialType;
            this.traceTail = traceTail;
        }
    }

    private static final class EarthSkillCaseResult {
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
        final boolean preloadedDebuff1;
        final String effectRow;

        EarthSkillCaseResult(int skillId, String name,
                             int beforePlayerHp, int afterPlayerHp,
                             int beforeEnemyHp, int afterEnemyHp,
                             int beforePp, int afterPp,
                             int damage,
                             int playerBuffId, boolean playerBuffActive, int playerBuffDuration,
                             int enemyDebuffId, boolean enemyDebuffActive, int enemyDebuffDuration,
                             boolean preloadedDebuff1,
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
            this.preloadedDebuff1 = preloadedDebuff1;
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
                    + " preloadedDebuff1=" + preloadedDebuff1
                    + " effect.mid=" + effectRow
                    + "\n";
        }
    }
}
