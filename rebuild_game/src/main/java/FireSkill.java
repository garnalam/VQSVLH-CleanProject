import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

final class FireSkill implements Skill {
    static final FireSkill INSTANCE = new FireSkill();
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private static final String[] SKILL0_DIRECT_TIMELINE_SUITE = {
            "battle_skill0_direct_timeline"
    };
    private static final String[] SKILL6_DIRECT_TIMELINE_SUITE = {
            "battle_skill6_direct_timeline"
    };
    private static final String[] SKILL7_CHUOC_NHIET_CHI_XUC_TIMELINE_SUITE = {
            "battle_skill7_chuoc_nhiet_chi_xuc_timeline"
    };
    private static final String[] SKILL8_LIET_DIEM_PHONG_BAO_TIMELINE_SUITE = {
            "battle_skill8_liet_diem_phong_bao_timeline"
    };
    private static final String[] SKILL1_DUONG_VIEM_TIMELINE_SUITE = {
            "battle_skill1_duong_viem_timeline"
    };
    private static final String[] SKILL2_DIEM_KICH_TIMELINE_SUITE = {
            "battle_skill2_diem_kich_timeline"
    };
    private static final String[] SKILL3_HOA_VAN_TRIEU_TIMELINE_SUITE = {
            "battle_skill3_hoa_van_trieu_timeline"
    };
    private static final String[] SKILL4_THIEN_HOA_TE_TIMELINE_SUITE = {
            "battle_skill4_thien_hoa_te_timeline"
    };
    private static final String[] SKILL5_VIEM_LOI_PHA_TIMELINE_SUITE = {
            "battle_skill5_viem_loi_pha_timeline"
    };
    private static final String[] SKILL9_VINH_HANG_HOA_ANH_TIMELINE_SUITE = {
            "battle_skill9_vinh_hang_hoa_anh_timeline"
    };
    private static final String[] FIRE_ANIMATION_CONTACT_SHEET_SUITE = {
            "battle_fire_animation_contact_sheet"
    };
    private static final String[] FIRE_SOURCE_STAGE_ANIMATION_SUITE = {
            "battle_fire_source_stage_animation"
    };
    private static final String[] FIRE_LIVE_FRAME_STRIP_SUITE = {
            "battle_fire_live_frame_strip"
    };

    private FireSkill() {
    }

    @Override
    public String[] checkpointsForSuite(String suite) {
        if ("battle_skill0_direct_timeline".equals(suite)) {
            return SKILL0_DIRECT_TIMELINE_SUITE;
        }
        if ("battle_skill6_direct_timeline".equals(suite)) {
            return SKILL6_DIRECT_TIMELINE_SUITE;
        }
        if ("battle_skill7_chuoc_nhiet_chi_xuc_timeline".equals(suite)) {
            return SKILL7_CHUOC_NHIET_CHI_XUC_TIMELINE_SUITE;
        }
        if ("battle_skill8_liet_diem_phong_bao_timeline".equals(suite)) {
            return SKILL8_LIET_DIEM_PHONG_BAO_TIMELINE_SUITE;
        }
        if ("battle_skill1_duong_viem_timeline".equals(suite)) {
            return SKILL1_DUONG_VIEM_TIMELINE_SUITE;
        }
        if ("battle_skill2_diem_kich_timeline".equals(suite)) {
            return SKILL2_DIEM_KICH_TIMELINE_SUITE;
        }
        if ("battle_skill3_hoa_van_trieu_timeline".equals(suite)) {
            return SKILL3_HOA_VAN_TRIEU_TIMELINE_SUITE;
        }
        if ("battle_skill4_thien_hoa_te_timeline".equals(suite)) {
            return SKILL4_THIEN_HOA_TE_TIMELINE_SUITE;
        }
        if ("battle_skill5_viem_loi_pha_timeline".equals(suite)) {
            return SKILL5_VIEM_LOI_PHA_TIMELINE_SUITE;
        }
        if ("battle_skill9_vinh_hang_hoa_anh_timeline".equals(suite)) {
            return SKILL9_VINH_HANG_HOA_ANH_TIMELINE_SUITE;
        }
        if ("battle_fire_animation_contact_sheet".equals(suite)) {
            return FIRE_ANIMATION_CONTACT_SHEET_SUITE;
        }
        if ("battle_fire_source_stage_animation".equals(suite)) {
            return FIRE_SOURCE_STAGE_ANIMATION_SUITE;
        }
        if ("battle_fire_live_frame_strip".equals(suite)) {
            return FIRE_LIVE_FRAME_STRIP_SUITE;
        }
        return null;
    }

    @Override
    public boolean runTimeline(String checkpoint, String outPath) {
        return runSkill5ViemLoiPhaTimelineSmokeIfNeeded(checkpoint, outPath)
                || runFireLiveFrameStripSmokeIfNeeded(checkpoint, outPath)
                || runFireSourceStageAnimationSmokeIfNeeded(checkpoint, outPath)
                || runFireAnimationContactSheetSmokeIfNeeded(checkpoint, outPath)
                || runSkill9VinhHangHoaAnhTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill4ThienHoaTeTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill3HoaVanTrieuTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill2DiemKichTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill8LietDiemPhongBaoTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill7ChuocNhietChiXucTimelineSmokeIfNeeded(checkpoint, outPath)
                || runSkill1DuongViemTimelineSmokeIfNeeded(checkpoint, outPath)
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

    static boolean runFireLiveFrameStripSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_fire_live_frame_strip".equals(checkpoint)) {
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
            java.io.File framesDir = new java.io.File(dir, "fire_live_frame_strip_frames");
            if (!framesDir.exists() && !framesDir.mkdirs()) {
                throw new IllegalStateException("Could not create frame-strip directory " + framesDir);
            }

            FireFrameStripResult[] results = new FireFrameStripResult[10];
            for (int skillId = 0; skillId <= 9; skillId++) {
                java.io.File skillDir = new java.io.File(framesDir, "skill" + skillId);
                if (!skillDir.exists() && !skillDir.mkdirs()) {
                    throw new IllegalStateException("Could not create frame-strip skill directory " + skillDir);
                }
                results[skillId] = runSingleFireLiveFrameStrip(skillId, checkpoint, skillDir);
            }
            writeFireFrameStripMaster(results, out);
            writeFireFrameStripDebug(results, new java.io.File(dir, "battle_fire_live_frame_strip_debug.txt"));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skills=0..9 perSkillStrips=" + framesDir.getPath()
                    + " note=visual-parity-reopened frame-strip, no gameplay mutation");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static FireFrameStripResult runSingleFireLiveFrameStrip(
            int skillId, String checkpoint, java.io.File dir) throws Exception {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
        assertFireSourceStageRows(skillId, checkpoint);
        runtime.debugSetSourceRandomSeedForSmoke(2026071400L + skillId);
        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        if (skillId == 2 || skillId == 8) {
            runtime.debugSetNextDamageDebuffRollForSmoke(0);
        }

        java.util.ArrayList<java.io.File> frames = new java.util.ArrayList<>();
        java.util.ArrayList<String> labels = new java.util.ArrayList<>();
        captureFireFrameStripFrame(s, dir, skillId, frames, labels, "p3_before", "P3 before");

        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
        tickUntilBattleP7Phase(s, 1, 120);
        int guard = 0;
        while ("P7".equals(s.battleStateName) && s.battleP7Phase == 1 && guard < 36 && frames.size() < 18) {
            captureFireFrameStripFrame(s, dir, skillId, frames, labels,
                    String.format("p7_%02d", guard), fireFrameStripLabel(s));
            int step = s.battleP7SpecialVisible
                    ? Math.max(1, Math.max(1, s.battleP7SpecialDuration) / 4)
                    : 2;
            for (int j = 0; j < step && "P7".equals(s.battleStateName) && s.battleP7Phase == 1; j++) {
                s.tick();
                guard++;
            }
        }
        if ("P7".equals(s.battleStateName) && s.battleP7Phase == 2) {
            for (int i = 0; i < 5 && "P7".equals(s.battleStateName) && s.battleP7Phase == 2; i++) {
                captureFireFrameStripFrame(s, dir, skillId, frames, labels,
                        "damage_" + i, fireFrameStripLabel(s));
                s.tick();
            }
        }
        int settleGuard = 0;
        while ("P7".equals(s.battleStateName) && settleGuard++ < 420) {
            s.tick();
            if (s.battleP7Phase >= 3
                    && !s.battleP7DamageVisible
                    && !s.battleP7ActorEffectVisible
                    && !s.battleP7SpecialVisible
                    && !s.battleP7BaseHiddenPlayerSide
                    && !s.battleP7BaseHiddenEnemySide
                    && s.battleP7BaseStatePlayerSide == 0
                    && s.battleP7BaseStateEnemySide == 0) {
                break;
            }
        }
        captureFireFrameStripFrame(s, dir, skillId, frames, labels, "settled", "settled idle");

        java.io.File strip = new java.io.File(dir, "battle_fire_live_frame_strip_skill" + skillId + ".png");
        writeSingleFireFrameStrip(skillId, fireSourceStageName(skillId), frames, labels, strip);
        return new FireFrameStripResult(skillId, fireSourceStageName(skillId),
                frames.toArray(new java.io.File[0]), labels.toArray(new String[0]), strip,
                tailTrace(s, 36));
    }

    private static String fireFrameStripLabel(VqsvIntroDemo.Scene s) {
        if (s.battleP7ActorEffectVisible) {
            return "u" + s.battleP7ActorEffectSourceId
                    + "/st" + s.battleP7ActorEffectState
                    + "/c" + s.battleP7ActorEffectCursor;
        }
        if (s.battleP7SpecialVisible) {
            int speffect = -1;
            for (String trace : s.sourceStateTrace) {
                int at = trace.indexOf("speffect=");
                if (trace.contains("battle P7 speffect") && at >= 0) {
                    int end = trace.indexOf(' ', at + 9);
                    String value = end < 0 ? trace.substring(at + 9) : trace.substring(at + 9, end);
                    try {
                        speffect = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                        speffect = -1;
                    }
                }
            }
            return "H" + s.battleP7SpecialType
                    + "/sp" + speffect
                    + "/t" + s.battleP7Ticks;
        }
        if (s.battleP7DamageVisible) {
            return s.battleP7MissText.isEmpty()
                    ? "damage " + s.battleP7DamageText
                    : "miss";
        }
        return "P" + s.battleP7Phase
                + " base " + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide;
    }

    private static void captureFireFrameStripFrame(VqsvIntroDemo.Scene s, java.io.File dir, int skillId,
                                                   java.util.ArrayList<java.io.File> frames,
                                                   java.util.ArrayList<String> labels,
                                                   String suffix, String label) throws java.io.IOException {
        java.io.File png = new java.io.File(dir,
                "battle_fire_live_frame_strip_skill" + skillId + "_" + frames.size() + "_" + suffix + ".png");
        writeScenePng(s, png);
        frames.add(png);
        labels.add(label);
    }

    private static void writeSingleFireFrameStrip(int skillId, String name,
                                                  java.util.List<java.io.File> frames,
                                                  java.util.List<String> labels,
                                                  java.io.File out) throws java.io.IOException {
        int count = Math.max(1, frames.size());
        int scale = 2;
        int cropW = 96;
        int cropH = 100;
        int fullW = 96;
        int fullH = 128;
        int cellW = cropW * scale;
        int headerH = 50;
        int rowGap = 18;
        int sheetW = Math.max(520, count * cellW + 16);
        int sheetH = headerH + fullH + rowGap + cropH * scale * 2 + 36;
        BufferedImage sheet = new BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(0x101820));
        g.fillRect(0, 0, sheetW, sheetH);
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString("Fire skill " + skillId + " - " + name + " live frame strip", 8, 18);
        g.drawString("Rows: full frame / player body crop / enemy body crop", 8, 36);
        for (int i = 0; i < count; i++) {
            BufferedImage img = ImageIO.read(frames.get(i));
            int x = 8 + i * cellW;
            g.setColor(new java.awt.Color(0x9fd7ff));
            g.drawString(shortFrameLabel(labels.get(i)), x, headerH - 6);
            g.drawImage(img, x, headerH, fullW, fullH, null);
            drawZoomCrop(g, img, 18, 132, cropW, cropH, x, headerH + fullH + rowGap, scale);
            drawZoomCrop(g, img, 126, 54, cropW, cropH,
                    x, headerH + fullH + rowGap + cropH * scale, scale);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    private static void writeFireFrameStripMaster(FireFrameStripResult[] results,
                                                  java.io.File out) throws java.io.IOException {
        int scale = 2;
        int cropW = 96;
        int cropH = 92;
        int maxFrames = 10;
        int labelW = 118;
        int cellW = cropW * scale;
        int rowH = cropH * scale + 46;
        int headerH = 44;
        int sheetW = labelW + maxFrames * cellW + 24;
        int sheetH = headerH + results.length * rowH + 16;
        BufferedImage sheet = new BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(0x101820));
        g.fillRect(0, 0, sheetW, sheetH);
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString("Fire live frame strip - source effect-side body crop by P7 timeline", 12, 20);
        g.drawString("Self-buff skills 4/5 crop player body; attack skills crop enemy body. Visual audit, not parity closeout.", 12, 36);
        for (FireFrameStripResult result : results) {
            int y = headerH + result.skillId * rowH;
            g.setColor(new java.awt.Color(0x24303a));
            g.drawRect(6, y + 4, sheetW - 12, rowH - 8);
            g.setColor(new java.awt.Color(0xe8f4ff));
            g.drawString("Skill " + result.skillId, 14, y + 24);
            g.drawString(result.name, 14, y + 40);
            boolean playerCrop = fireFrameStripMasterUsesPlayerCrop(result.skillId);
            g.setColor(new java.awt.Color(0x9fd7ff));
            g.drawString(playerCrop ? "crop: player" : "crop: enemy", 14, y + 56);
            int count = Math.min(maxFrames, result.frames.length);
            for (int i = 0; i < count; i++) {
                BufferedImage img = ImageIO.read(result.frames[i]);
                int x = labelW + i * cellW;
                g.setColor(new java.awt.Color(0x9fd7ff));
                g.drawString(shortFrameLabel(result.labels[i]), x + 4, y + 20);
                if (playerCrop) {
                    drawZoomCrop(g, img, 18, 132, cropW, cropH, x, y + 28, scale);
                } else {
                    drawZoomCrop(g, img, 126, 54, cropW, cropH, x, y + 28, scale);
                }
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    private static boolean fireFrameStripMasterUsesPlayerCrop(int skillId) {
        return skillId == 4 || skillId == 5;
    }

    private static String shortFrameLabel(String label) {
        if (label == null) {
            return "";
        }
        return label.length() <= 18 ? label : label.substring(0, 18);
    }

    private static void writeFireFrameStripDebug(FireFrameStripResult[] results,
                                                 java.io.File out) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("battle_fire_live_frame_strip\n");
        sb.append("Status: VISUAL PARITY REOPENED / smoke-only audit.\n");
        for (FireFrameStripResult result : results) {
            sb.append("skill=").append(result.skillId)
                    .append(" name=").append(result.name)
                    .append(" strip=").append(result.stripPng.getPath())
                    .append("\n");
            for (int i = 0; i < result.frames.length; i++) {
                sb.append("  frame").append(i)
                        .append(" label=").append(result.labels[i])
                        .append(" png=").append(result.frames[i].getPath())
                        .append("\n");
            }
            sb.append("  trace=").append(result.traceTail).append("\n");
        }
        Files.write(out.toPath(), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static boolean runFireSourceStageAnimationSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_fire_source_stage_animation".equals(checkpoint)) {
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
            java.io.File stagesDir = new java.io.File(dir, "fire_source_stage_frames");
            if (!stagesDir.exists() && !stagesDir.mkdirs()) {
                throw new IllegalStateException("Could not create stage directory " + stagesDir);
            }

            FireSourceStageResult[] results = new FireSourceStageResult[10];
            for (int skillId = 0; skillId <= 9; skillId++) {
                java.io.File skillDir = new java.io.File(stagesDir, "skill" + skillId);
                if (!skillDir.exists() && !skillDir.mkdirs()) {
                    throw new IllegalStateException("Could not create stage skill directory " + skillDir);
                }
                results[skillId] = runSingleFireSourceStageAnimation(skillId, checkpoint, skillDir);
            }
            writeFireSourceStageContactSheet(results, out);
            java.io.File zoomOut = new java.io.File(dir, "battle_fire_source_stage_animation_zoom.png");
            writeFireSourceStageZoomSheet(results, zoomOut);
            writeFireSourceStageDebug(results, new java.io.File(dir,
                    "battle_fire_source_stage_animation_debug.txt"));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " zoom=" + zoomOut.getPath()
                    + " skills=0..9 sourceStages=before,attacker_state1,target_u_or_h,target_hit_state2,settled_idle");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static FireSourceStageResult runSingleFireSourceStageAnimation(
            int skillId, String checkpoint, java.io.File dir) throws Exception {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
        assertFireSourceStageRows(skillId, checkpoint);
        runtime.debugSetSourceRandomSeedForSmoke(20260714L + skillId);
        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        if (skillId == 2 || skillId == 8) {
            runtime.debugSetNextDamageDebuffRollForSmoke(0);
        }
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != skillId
                || s.battleP7BaseStatePlayerSide != 0
                || s.battleP7BaseStateEnemySide != 0
                || s.battleP7ActorEffectVisible
                || s.battleP7SpecialVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " skill" + skillId + " expected clean P3 before"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " special=" + s.battleP7SpecialVisible
                    + " damage=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 24));
        }
        java.io.File beforePng = new java.io.File(dir, fireSourceStagePngName(skillId, "0_before"));
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
        if (s.battleP7BaseStatePlayerSide != 1) {
            throw new IllegalStateException(checkpoint + " skill" + skillId
                    + " expected attacker base state1 before/while source P7 action"
                    + " playerBase=" + s.battleP7BaseStatePlayerSide
                    + " enemyBase=" + s.battleP7BaseStateEnemySide
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " special=" + s.battleP7SpecialVisible
                    + " trace=" + tailTrace(s, 64));
        }
        java.io.File attackerPng = new java.io.File(dir, fireSourceStagePngName(skillId, "1_attacker_state1"));
        writeScenePng(s, attackerPng);

        FireSourceStageKind kind = fireSourceStageKind(skillId);
        java.io.File targetEffectPng = new java.io.File(dir, fireSourceStagePngName(skillId, "2_target_u_or_h"));
        if (kind == FireSourceStageKind.ACTOR_ONLY) {
            tickUntilActorEffect(s, skillId, 20, directBaseActorState(skillId), 80);
            assertFireTargetActorEffect(s, runtime, checkpoint, skillId);
        } else if (kind == FireSourceStageKind.ACTOR_THEN_H) {
            tickUntilActorEffect(s, skillId, 20, directBaseActorState(skillId), 80);
            assertFireTargetActorEffect(s, runtime, checkpoint, skillId);
            writeScenePng(s, new java.io.File(dir, fireSourceStagePngName(skillId, "2a_target_u20_primary")));
            tickUntilActorEffect(s, skillId, 20, 3, 160);
            assertFireTargetActorState(s, runtime, checkpoint, skillId, 20, 262, 3, false);
            writeScenePng(s, new java.io.File(dir, fireSourceStagePngName(skillId, "2b_target_u20_state3")));
            tickUntilSpecialEffect(s, skillId, 9, 0, 220);
            assertFireTargetSpecialEffect(s, checkpoint, skillId, 9, 0);
        } else {
            int selfActorId = selfBuffActorEffectId(skillId);
            tickUntilActorEffect(s, skillId, selfActorId, 0, 120);
            assertFireTargetActorState(s, runtime, checkpoint, skillId,
                    selfActorId, fireSourceEffectSpriteId(selfActorId), 0, true);
            writeScenePng(s, new java.io.File(dir, fireSourceStagePngName(skillId, "2a_self_actor_u" + selfActorId)));
            tickUntilSpecialEffect(s, skillId, 9, 16, 120);
            assertFireTargetSpecialEffect(s, checkpoint, skillId, 9, 16);
            if (!s.battleP7SpecialOnPlayerSide || !s.battleP7BaseHiddenPlayerSide) {
                throw new IllegalStateException(checkpoint + " skill" + skillId
                        + " expected self H speffect16 to hide player base"
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " playerHidden=" + s.battleP7BaseHiddenPlayerSide
                        + " enemyHidden=" + s.battleP7BaseHiddenEnemySide
                        + " trace=" + tailTrace(s, 72));
            }
            writeScenePng(s, new java.io.File(dir, fireSourceStagePngName(skillId, "2a_self_H16_type9")));
            tickUntilSpecialEffect(s, skillId, 1, 15, 220);
            assertFireTargetSpecialEffect(s, checkpoint, skillId, 1, 15);
            if (!s.battleP7SpecialOnPlayerSide || !s.battleP7BaseHiddenPlayerSide) {
                throw new IllegalStateException(checkpoint + " skill" + skillId
                        + " expected self H speffect15/AH1 after speffect16"
                        + " specialSidePlayer=" + s.battleP7SpecialOnPlayerSide
                        + " playerHidden=" + s.battleP7BaseHiddenPlayerSide
                        + " enemyHidden=" + s.battleP7BaseHiddenEnemySide
                        + " trace=" + tailTrace(s, 96));
            }
        }
        writeScenePng(s, targetEffectPng);

        java.io.File hitPng = new java.io.File(dir, fireSourceStagePngName(skillId, "3_target_hit_state2"));
        boolean noDamage = skillId == 4 || skillId == 5;
        int damage = -1;
        if (!noDamage) {
            tickUntilBattleP7Phase(s, 2, 260);
            damage = latestTraceDamage(s, "battle P7 damage frame skill=" + skillId);
            if (!s.battleP7DamageVisible
                    || s.battleP7BaseStateEnemySide != 2
                    || s.battleP7ActorEffectVisible
                    || s.battleP7SpecialVisible
                    || damage <= 0
                    || !traceContains(s, "battle P7 damage frame skill=" + skillId)
                    || !traceContains(s, "hit=true")) {
                throw new IllegalStateException(checkpoint + " skill" + skillId
                        + " expected target hit/recover state2 at damage frame"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " damage=" + damage
                        + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                        + " actor=" + s.battleP7ActorEffectVisible
                        + " special=" + s.battleP7SpecialVisible
                        + " trace=" + tailTrace(s, 96));
            }
        } else {
            tickUntilBattleP7Phase(s, 3, 360);
            if (s.battleP7DamageVisible
                    || traceContains(s, "battle P7 damage frame skill=" + skillId)
                    || traceContains(s, "battle P7 hitroll skill=" + skillId)
                    || s.battleP7BaseStateEnemySide == 2
                    || !traceContains(s, "battle P7 no-damage skill=" + skillId)) {
                throw new IllegalStateException(checkpoint + " skill" + skillId
                        + " expected no target hit/damage for self buff"
                        + " damageVisible=" + s.battleP7DamageVisible
                        + " enemyBase=" + s.battleP7BaseStateEnemySide
                        + " trace=" + tailTrace(s, 96));
            }
        }
        writeScenePng(s, hitPng);

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
                    && s.battleP7BaseStateEnemySide == 0) {
                break;
            }
        }
        if (s.battleP7ActorEffectVisible
                || s.battleP7SpecialVisible
                || s.battleP7BaseHiddenPlayerSide
                || s.battleP7BaseHiddenEnemySide
                || s.battleP7BaseStatePlayerSide != 0
                || s.battleP7BaseStateEnemySide != 0) {
            throw new IllegalStateException(checkpoint + " skill" + skillId
                    + " expected settled idle state after P7 animation"
                    + " phase=" + s.battleP7Phase
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " hidden=" + s.battleP7BaseHiddenPlayerSide + "/" + s.battleP7BaseHiddenEnemySide
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " special=" + s.battleP7SpecialVisible
                    + " trace=" + tailTrace(s, 96));
        }
        java.io.File settledPng = new java.io.File(dir, fireSourceStagePngName(skillId, "4_settled_idle"));
        writeScenePng(s, settledPng);
        return new FireSourceStageResult(skillId, fireSourceStageName(skillId), kind,
                beforePng, attackerPng, targetEffectPng, hitPng, settledPng,
                damage, s.battleP7BaseStatePlayerSide, s.battleP7BaseStateEnemySide,
                tailTrace(s, 24));
    }

    private static void tickUntilActorEffect(VqsvIntroDemo.Scene s, int skillId, int sourceId,
                                             int state, int maxTicks) {
        for (int i = 0; i < maxTicks; i++) {
            if (s.battleP7ActorEffectVisible
                    && s.battleP7ActorEffectSourceId == sourceId
                    && s.battleP7ActorEffectState == state) {
                return;
            }
            s.tick();
        }
        throw new IllegalStateException("skill" + skillId + " actor effect u" + sourceId
                + " state=" + state + " not reached"
                + " actorVisible=" + s.battleP7ActorEffectVisible
                + " source=" + s.battleP7ActorEffectSourceId
                + " actorState=" + s.battleP7ActorEffectState
                + " trace=" + tailTrace(s, 80));
    }

    private static void tickUntilSpecialEffect(VqsvIntroDemo.Scene s, int skillId, int specialType,
                                               int speffectId, int maxTicks) {
        String speffectTrace = "speffect=" + speffectId;
        for (int i = 0; i < maxTicks; i++) {
            if (s.battleP7SpecialVisible
                    && s.battleP7SpecialType == specialType
                    && traceContains(s, "battle P7 speffect skill=" + skillId)
                    && traceContains(s, speffectTrace)) {
                return;
            }
            s.tick();
        }
        throw new IllegalStateException("skill" + skillId + " H speffect" + speffectId
                + " AH type " + specialType + " not reached"
                + " specialVisible=" + s.battleP7SpecialVisible
                + " type=" + s.battleP7SpecialType
                + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                + " trace=" + tailTrace(s, 96));
    }

    private static void assertFireTargetActorEffect(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint, int skillId) {
        assertFireTargetActorState(s, runtime, checkpoint, skillId,
                20, 262, directBaseActorState(skillId), false);
    }

    private static void assertFireTargetActorState(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                   String checkpoint, int skillId,
                                                   int sourceId, int spriteId, int state,
                                                   boolean playerSide) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSourceId != sourceId
                || s.battleP7ActorEffectSpriteId != spriteId
                || s.battleP7ActorEffectState != state
                || s.battleP7ActorEffectOnPlayerSide != playerSide
                || s.battleP7BaseStatePlayerSide != 1
                || s.battleP7BaseStateEnemySide != 0
                || s.battleP7SpecialVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != fireExpectedPpAfterUse(skillId)
                || !traceContains(s, "battle P7 actor u.a() start skill=" + skillId)) {
            throw new IllegalStateException(checkpoint + " skill" + skillId
                    + " expected actor u" + sourceId + " state=" + state
                    + " playerSide=" + playerSide
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " source=" + s.battleP7ActorEffectSourceId
                    + " sprite=" + s.battleP7ActorEffectSpriteId
                    + " state=" + s.battleP7ActorEffectState
                    + " sidePlayer=" + s.battleP7ActorEffectOnPlayerSide
                    + " base=" + s.battleP7BaseStatePlayerSide + "/" + s.battleP7BaseStateEnemySide
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " expectedPp=" + fireExpectedPpAfterUse(skillId)
                    + " trace=" + tailTrace(s, 72));
        }
    }

    private static void assertFireTargetSpecialEffect(VqsvIntroDemo.Scene s, String checkpoint,
                                                      int skillId, int specialType, int speffectId) {
        if (!s.battleP7SpecialVisible
                || s.battleP7SpecialType != specialType
                || s.battleP7SpecialRow.length == 0
                || !traceContainsAll(s, "battle P7 speffect skill=" + skillId,
                "speffect=" + speffectId, "AH type " + specialType)) {
            throw new IllegalStateException(checkpoint + " skill" + skillId
                    + " expected H speffect" + speffectId + " AH type " + specialType
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " type=" + s.battleP7SpecialType
                    + " sidePlayer=" + s.battleP7SpecialOnPlayerSide
                    + " hidden=" + s.battleP7BaseHiddenPlayerSide + "/" + s.battleP7BaseHiddenEnemySide
                    + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                    + " trace=" + tailTrace(s, 96));
        }
    }

    private static void assertFireSourceStageRows(int skillId, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(skillId);
        if (row == null || !java.util.Arrays.equals(effect, fireExpectedEffectRow(skillId))) {
            throw new IllegalStateException(checkpoint + " skill" + skillId + " source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expected=" + java.util.Arrays.toString(fireExpectedEffectRow(skillId)));
        }
    }

    private static byte[] fireExpectedEffectRow(int skillId) {
        switch (skillId) {
            case 0:
            case 1:
            case 3:
            case 6:
            case 7:
            case 9:
                return directBaseExpectedEffectRow(skillId);
            case 2:
                return new byte[]{0, 0, 20, 2, -1, -1, 0,
                        0, 0, 20, 3, 1, -1, 0,
                        0, 1, 0, 0, 0, -1, 1};
            case 8:
                return new byte[]{0, 0, 20, 7, -1, -1, 0,
                        0, 0, 20, 3, 1, -1, 0,
                        0, 1, 0, 0, 0, -1, 1};
            case 4:
                return new byte[]{0, 0, 30, 0, 0, -1, 0,
                        0, 1, 16, 0, -1, -1, 0,
                        0, 1, 15, 0, -1, -1, 0};
            case 5:
                return new byte[]{0, 0, 31, 0, 0, -1, 0,
                        0, 1, 16, 0, -1, -1, 0,
                        0, 1, 15, 0, -1, -1, 0};
            default:
                throw new IllegalArgumentException("Not a Fire skill: " + skillId);
        }
    }

    private static int selfBuffActorEffectId(int skillId) {
        if (skillId == 4) {
            return 30;
        }
        if (skillId == 5) {
            return 31;
        }
        throw new IllegalArgumentException("Not a Fire self-buff skill: " + skillId);
    }

    private static int fireSourceEffectSpriteId(int sourceEffectId) {
        switch (sourceEffectId) {
            case 20:
                return 262;
            case 30:
                return 304;
            case 31:
                return 306;
            default:
                return sourceEffectId + 242;
        }
    }

    private static int fireExpectedPpAfterUse(int skillId) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        if (row == null) {
            throw new IllegalArgumentException("Missing fire skill row " + skillId);
        }
        return Math.max(0, row.ppMax - 1);
    }

    private static FireSourceStageKind fireSourceStageKind(int skillId) {
        if (skillId == 2 || skillId == 8) {
            return FireSourceStageKind.ACTOR_THEN_H;
        }
        if (skillId == 4 || skillId == 5) {
            return FireSourceStageKind.H_ONLY;
        }
        return FireSourceStageKind.ACTOR_ONLY;
    }

    private static String fireSourceStageName(int skillId) {
        switch (skillId) {
            case 0:
                return "Hoa trao";
            case 1:
                return "Duong viem";
            case 2:
                return "Diem kich";
            case 3:
                return "Hoa Van trieu";
            case 4:
                return "Thien Hoa te";
            case 5:
                return "Viem loi pha";
            case 6:
                return "Hoa diem dao";
            case 7:
                return "Chuoc nhiet chi xuc";
            case 8:
                return "Liet diem phong bao";
            case 9:
                return "Vinh hang hoa anh";
            default:
                return "Skill " + skillId;
        }
    }

    private static String fireSourceStagePngName(int skillId, String suffix) {
        return "battle_fire_source_stage_skill" + skillId + "_" + suffix + ".png";
    }

    private static void writeFireSourceStageDebug(FireSourceStageResult[] results,
                                                   java.io.File out) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("battle_fire_source_stage_animation\n");
        for (FireSourceStageResult result : results) {
            sb.append("skill=").append(result.skillId)
                    .append(" name=").append(result.name)
                    .append(" kind=").append(result.kind)
                    .append(" damage=").append(result.damage)
                    .append(" settledBase=").append(result.settledPlayerBase)
                    .append("/").append(result.settledEnemyBase)
                    .append("\n");
            sb.append("  before=").append(result.beforePng.getPath()).append("\n");
            sb.append("  attacker=").append(result.attackerPng.getPath()).append("\n");
            sb.append("  target=").append(result.targetEffectPng.getPath()).append("\n");
            sb.append("  hit=").append(result.hitPng.getPath()).append("\n");
            sb.append("  settled=").append(result.settledPng.getPath()).append("\n");
            sb.append("  trace=").append(result.traceTail).append("\n");
        }
        Files.write(out.toPath(), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static void writeFireSourceStageContactSheet(FireSourceStageResult[] results,
                                                          java.io.File out) throws java.io.IOException {
        int cellW = 240;
        int cellH = 335;
        int cols = 2;
        int rows = 5;
        BufferedImage sheet = new BufferedImage(cols * cellW, rows * cellH,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(0x111820));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setColor(new java.awt.Color(0xe8f4ff));
        for (int i = 0; i < results.length; i++) {
            FireSourceStageResult result = results[i];
            int x = (i % cols) * cellW;
            int y = (i / cols) * cellH;
            g.setColor(new java.awt.Color(0x26313b));
            g.drawRect(x + 5, y + 5, cellW - 10, cellH - 10);
            g.setColor(new java.awt.Color(0xe8f4ff));
            g.drawString("Skill " + result.skillId + " - " + result.name, x + 12, y + 22);
            g.drawString(result.kind.toString() + " dmg=" + result.damage, x + 12, y + 38);
            drawStageThumb(g, result.beforePng, x + 12, y + 48, "before");
            drawStageThumb(g, result.attackerPng, x + 122, y + 48, "atk state1");
            drawStageThumb(g, result.targetEffectPng, x + 12, y + 190, "target u/H");
            drawStageThumb(g, result.hitPng, x + 122, y + 190, "hit/no-hit");
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    private static void writeFireSourceStageZoomSheet(FireSourceStageResult[] results,
                                                       java.io.File out) throws java.io.IOException {
        String[] labels = {"before", "atk1", "u/H", "hit", "idle"};
        int scale = 2;
        int cropW = 92;
        int cropH = 86;
        int thumbW = cropW * scale;
        int thumbH = cropH * scale;
        int labelW = 116;
        int rowH = thumbH * 2 + 60;
        int headerH = 44;
        int sheetW = labelW + labels.length * thumbW + 24;
        int sheetH = headerH + results.length * rowH + 16;
        BufferedImage sheet = new BufferedImage(sheetW, sheetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(0x101820));
        g.fillRect(0, 0, sheetW, sheetH);
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString("Fire source-stage zoom: player body + enemy body", 12, 20);
        g.drawString("Columns: before / attacker state1 / target u-or-H / hit-or-no-hit / settled idle", 12, 36);
        for (int i = 0; i < labels.length; i++) {
            g.drawString(labels[i], labelW + i * thumbW + 8, headerH - 8);
        }
        for (int i = 0; i < results.length; i++) {
            FireSourceStageResult result = results[i];
            int y = headerH + i * rowH;
            g.setColor(new java.awt.Color(0x24303a));
            g.drawRect(6, y + 4, sheetW - 12, rowH - 8);
            g.setColor(new java.awt.Color(0xe8f4ff));
            g.drawString("Skill " + result.skillId, 14, y + 24);
            g.drawString(result.name, 14, y + 40);
            g.drawString(result.kind.toString(), 14, y + 56);
            g.drawString("player", 14, y + 82);
            g.drawString("enemy", 14, y + 82 + thumbH);
            java.io.File[] stagePngs = {
                    result.beforePng, result.attackerPng, result.targetEffectPng, result.hitPng, result.settledPng
            };
            for (int stage = 0; stage < stagePngs.length; stage++) {
                BufferedImage img = ImageIO.read(stagePngs[stage]);
                drawZoomCrop(g, img, 18, 132, cropW, cropH,
                        labelW + stage * thumbW, y + 66, scale);
                drawZoomCrop(g, img, 126, 54, cropW, cropH,
                        labelW + stage * thumbW, y + 66 + thumbH, scale);
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
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

    private static void drawStageThumb(Graphics2D g, java.io.File png, int x, int y, String label)
            throws java.io.IOException {
        BufferedImage img = ImageIO.read(png);
        g.drawImage(img, x, y + 12, 96, 128, null);
        g.setColor(new java.awt.Color(0xe8f4ff));
        g.drawString(label, x, y + 10);
    }

    private enum FireSourceStageKind {
        ACTOR_ONLY,
        ACTOR_THEN_H,
        H_ONLY
    }

    private static final class FireSourceStageResult {
        final int skillId;
        final String name;
        final FireSourceStageKind kind;
        final java.io.File beforePng;
        final java.io.File attackerPng;
        final java.io.File targetEffectPng;
        final java.io.File hitPng;
        final java.io.File settledPng;
        final int damage;
        final int settledPlayerBase;
        final int settledEnemyBase;
        final String traceTail;

        FireSourceStageResult(int skillId, String name, FireSourceStageKind kind,
                              java.io.File beforePng, java.io.File attackerPng,
                              java.io.File targetEffectPng, java.io.File hitPng,
                              java.io.File settledPng, int damage,
                              int settledPlayerBase, int settledEnemyBase, String traceTail) {
            this.skillId = skillId;
            this.name = name;
            this.kind = kind;
            this.beforePng = beforePng;
            this.attackerPng = attackerPng;
            this.targetEffectPng = targetEffectPng;
            this.hitPng = hitPng;
            this.settledPng = settledPng;
            this.damage = damage;
            this.settledPlayerBase = settledPlayerBase;
            this.settledEnemyBase = settledEnemyBase;
            this.traceTail = traceTail;
        }
    }

    private static final class FireFrameStripResult {
        final int skillId;
        final String name;
        final java.io.File[] frames;
        final String[] labels;
        final java.io.File stripPng;
        final String traceTail;

        FireFrameStripResult(int skillId, String name, java.io.File[] frames, String[] labels,
                             java.io.File stripPng, String traceTail) {
            this.skillId = skillId;
            this.name = name;
            this.frames = frames;
            this.labels = labels;
            this.stripPng = stripPng;
            this.traceTail = traceTail;
        }
    }

    static boolean runSkillDirectTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        int skillId;
        if ("battle_skill0_direct_timeline".equals(checkpoint)) {
            skillId = 0;
        } else if ("battle_skill6_direct_timeline".equals(checkpoint)) {
            skillId = 6;
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

    static boolean runSkill1DuongViemTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill1_duong_viem_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 1;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
            assertSkill1DuongViemSourceRows(s, checkpoint);
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
            int playerAttack = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_ATTACK);
            int enemyDefense = runtime.debugEnemyCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            writeScenePng(s, new java.io.File(dir, skill1DuongViemPngName("before")));

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
            assertDirectBaseP7ActorVisible(s, runtime, checkpoint, skillId);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill1DuongViemPngName("actor_u20_start")));

            tickUntilBattleP7Phase(s, 2, 180);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=1");
            assertSkill1DuongViemDamageDebuffFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            int storedRaw = runtime.debugEnemyDebuffValueForSmoke(0);
            int debuffDuration = runtime.debugEnemyDebuffDurationForSmoke(0);
            int debuffSourceSkill = runtime.debugEnemyDebuffSourceSkillForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill1DuongViemPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 240) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || runtime.debugEnemyDebuffDurationForSmoke(0) != 3
                    || runtime.debugEnemyDebuffValueForSmoke(0) != storedRaw
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(0) != 1
                    || !traceContains(s, "battle P7 source n() skill=1")
                    || !traceContains(s, "battle P7 actor u.a() start skill=1")
                    || !traceContains(s, "battle P7 damage frame skill=1")) {
                throw new IllegalStateException(checkpoint + " expected skill1 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                        + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(0)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(0)
                        + " trace=" + tailTrace(s, 40));
            }
            writeScenePng(s, new java.io.File(dir, skill1DuongViemPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue apply bank=1 id=0", 700);
            int tickDamage = hpBeforeTick - s.battleEnemyHp;
            if (tickDamage != Math.max(1, storedRaw / 4)
                    || runtime.debugEnemyDebuffDurationForSmoke(0) != 2
                    || !s.battleP7PostEffectText.startsWith("-")) {
                throw new IllegalStateException(checkpoint + " expected debuff0 tick damage storedRaw/4"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " tickDamage=" + tickDamage
                        + " expected=" + Math.max(1, storedRaw / 4)
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                        + " postText=" + s.battleP7PostEffectText
                        + " trace=" + tailTrace(s, 40));
            }
            writeScenePng(s, new java.io.File(dir, skill1DuongViemPngName("tick_damage_duration2")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 0);
            int durationAfterSecondTick = runtime.debugEnemyDebuffDurationForSmoke(0);
            runtime.debugTickEnemySourceDebuffForSmoke(s, 0);
            if (runtime.debugEnemyDebuffDurationForSmoke(0) != 0
                    || s.battleEnemyStatusCount != 0
                    || durationAfterSecondTick != 1) {
                throw new IllegalStateException(checkpoint + " expected debuff0 to expire after three ticks"
                        + " durationAfterSecondTick=" + durationAfterSecondTick
                        + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                        + " value=" + runtime.debugEnemyDebuffValueForSmoke(0)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 40));
            }
            writeScenePng(s, new java.io.File(dir, skill1DuongViemPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(1);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(0);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=1 name=Duong viem description=Low fire damage plus debuff0 HP drain for 3 turns\n"
                    + "aq.c[1][1]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[1]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(1)) + "\n"
                    + "aq.c[7][0]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "actorEffect=20 actorSprite=262 actorState=0 actorSide=enemy\n"
                    + "formula=raw*50/100 + raw/4; debuffTick=storedRaw/4; duration=3\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " playerAttack=" + playerAttack
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp + "\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " storedRaw=" + storedRaw
                    + " debuffDuration=" + debuffDuration
                    + " debuffSourceSkill=" + debuffSourceSkill + "\n"
                    + "hpSettled hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0) + "\n"
                    + "tick1 damage=" + tickDamage
                    + " expected=" + Math.max(1, storedRaw / 4)
                    + " duration=2\n"
                    + "expiry finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                    + " finalValue=" + runtime.debugEnemyDebuffValueForSmoke(0)
                    + " statusCount=" + s.battleEnemyStatusCount
                    + " note=slot value is residual after inactive flag clear\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 32) + "\n";
            Files.write(new java.io.File(dir, "battle_skill1_duong_viem_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " storedRaw=" + storedRaw
                    + " tickDamage=" + tickDamage
                    + " images=before,actor_u20_start,damage_debuff_frame,hp_settled_debuff_active,tick_damage_duration2,expired");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill7ChuocNhietChiXucTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill7_chuoc_nhiet_chi_xuc_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 7;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
            assertSkill7ChuocNhietChiXucSourceRows(s, checkpoint);
            assertSkill7P3BeforeConfirm(s, runtime, checkpoint);

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
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("before")));

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
            assertSkill7P7ActorVisible(s, runtime, checkpoint);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("actor_u20_start")));

            tickUntilBattleP7Phase(s, 2, 180);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=7");
            assertSkill7ChuocNhietChiXucDamageDebuffFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            int storedRaw = runtime.debugEnemyDebuffValueForSmoke(0);
            int debuffDuration = runtime.debugEnemyDebuffDurationForSmoke(0);
            int debuffSourceSkill = runtime.debugEnemyDebuffSourceSkillForSmoke(0);
            int expectedDamageBase = storedRaw * 75 / 100 + storedRaw / 3;
            if (damage < expectedDamageBase - 1
                    || damage > expectedDamageBase + 1
                    || !traceContains(s, "battle.P7.skill7.damage.jitter")) {
                throw new IllegalStateException(checkpoint + " expected skill7 formula raw*75/100+raw/3 plus source jitter"
                        + " storedRaw=" + storedRaw
                        + " damage=" + damage
                        + " expectedBase=" + expectedDamageBase
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 240) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 29
                    || runtime.debugEnemyDebuffDurationForSmoke(0) != 3
                    || runtime.debugEnemyDebuffValueForSmoke(0) != storedRaw
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(0) != 7
                    || !traceContains(s, "battle P7 source n() skill=7")
                    || !traceContains(s, "battle P7 actor u.a() start skill=7")
                    || !traceContains(s, "battle P7 damage frame skill=7")) {
                throw new IllegalStateException(checkpoint + " expected skill7 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                        + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(0)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(0)
                        + " trace=" + tailTrace(s, 64));
            }
            assertPhase10AStatusSlots(s, false, "skill7 debuff0 active after HP settled",
                    new int[]{1}, new int[]{137});
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=0", 700);
            tickUntilTraceContains(s, "speffect=18", 700);
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 0
                    || !s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 9
                    || !traceContainsAll(s, "battle P12 active queue visual",
                    "bank=1", "debuff=0", "speffect=18")) {
                throw new IllegalStateException(checkpoint + " expected skill7 debuff0 P12 body visual speffect18"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("p12_body_visual_speffect18")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=0", 700);
            int tickDamage = hpBeforeTick - s.battleEnemyHp;
            int expectedTickDamage = Math.max(1, storedRaw / 3);
            if (tickDamage != expectedTickDamage
                    || runtime.debugEnemyDebuffDurationForSmoke(0) != 2
                    || !s.battleP7PostEffectText.equals("-" + expectedTickDamage)) {
                throw new IllegalStateException(checkpoint + " expected debuff0 tick damage storedRaw/3"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " tickDamage=" + tickDamage
                        + " expected=" + expectedTickDamage
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                        + " postText=" + s.battleP7PostEffectText
                        + " trace=" + tailTrace(s, 64));
            }
            assertPhase10AStatusSlots(s, false, "skill7 debuff0 after first tick",
                    new int[]{1}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("tick_damage_duration2")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 0);
            int durationAfterSecondTick = runtime.debugEnemyDebuffDurationForSmoke(0);
            int hpAfterSecondTick = s.battleEnemyHp;
            runtime.debugTickEnemySourceDebuffForSmoke(s, 0);
            if (runtime.debugEnemyDebuffDurationForSmoke(0) != 0
                    || s.battleEnemyStatusCount != 0
                    || durationAfterSecondTick != 1
                    || hpAfterSecondTick != expectedEnemyHp - tickDamage - expectedTickDamage
                    || s.battleEnemyHp != expectedEnemyHp - tickDamage - expectedTickDamage * 2) {
                throw new IllegalStateException(checkpoint + " expected debuff0 skill7 to expire after three /3 ticks"
                        + " durationAfterSecondTick=" + durationAfterSecondTick
                        + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                        + " hpAfterSecond=" + hpAfterSecondTick
                        + " finalHp=" + s.battleEnemyHp
                        + " expectedTick=" + expectedTickDamage
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 64));
            }
            writeScenePng(s, new java.io.File(dir, skill7ChuocNhietChiXucPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(7);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(0);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=7 name=Chuoc nhiet chi xuc description=Fire damage plus stronger debuff0 HP drain for 3 turns\n"
                    + "aq.c[1][7]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[7]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(7)) + "\n"
                    + "aq.c[7][0]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "bufDebuf aq[0]=[1,18,0,-1] speffect18="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(18)) + "\n"
                    + "actorEffect=20 actorSprite=262 actorState=0 actorSide=enemy\n"
                    + "formula=raw*75/100 + raw/3; debuffTick=storedRaw/3; duration=3\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " playerAttack=" + playerAttack
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp + "\n"
                    + "damageFrame damage=" + damage
                    + " expectedDamageBase=" + expectedDamageBase
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " storedRaw=" + storedRaw
                    + " debuffDuration=" + debuffDuration
                    + " debuffSourceSkill=" + debuffSourceSkill + "\n"
                    + "hpSettled hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0) + "\n"
                    + "tick1 damage=" + tickDamage
                    + " expected=" + expectedTickDamage
                    + " duration=2\n"
                    + "expiry hpAfterSecond=" + hpAfterSecondTick
                    + " finalHp=" + s.battleEnemyHp
                    + " finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                    + " statusCount=" + s.battleEnemyStatusCount
                    + " note=slot value is residual after inactive flag clear\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 40) + "\n";
            Files.write(new java.io.File(dir, "battle_skill7_chuoc_nhiet_chi_xuc_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " storedRaw=" + storedRaw
                    + " tickDamage=" + tickDamage
                    + " images=before,actor_u20_start,damage_debuff_frame,hp_settled_debuff_active,"
                    + "p12_body_visual_speffect18,tick_damage_duration2,expired");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill2DiemKichTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill2_diem_kich_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 2;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
            assertSkill2DiemKichSourceRows(s, checkpoint);
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
            int playerAttack = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_ATTACK);
            int enemyDefense = runtime.debugEnemyCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("before")));

            runtime.debugSetSourceRandomSeedForSmoke(20260714L);
            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);
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
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("actor_u20_start")));

            for (int i = 0; i < 180 && !s.battleP7SpecialVisible; i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 9
                    || s.battleP7SpecialRow.length == 0
                    || !traceContainsAll(s, "battle P7 speffect skill=2",
                    "chunk=2", "speffect=0", "AH type 9")) {
                throw new IllegalStateException(checkpoint + " expected skill2 S60 P7 speffect0 AH type9"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " type=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 60));
            }
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("speffect0_type9")));

            tickUntilBattleP7Phase(s, 2, 220);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=2");
            assertSkill2DiemKichDamageDebuffFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            int debuffDuration = runtime.debugEnemyDebuffDurationForSmoke(1);
            int debuffSourceSkill = runtime.debugEnemyDebuffSourceSkillForSmoke(1);
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 240) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 44
                    || runtime.debugEnemyDebuffDurationForSmoke(1) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(1) != 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(1) != 2
                    || !traceContains(s, "SMOKE battle forced damage.debuff roll=0")
                    || !traceContains(s, "battle P7 source n() skill=2")
                    || !traceContains(s, "battle P7 actor u.a() start skill=2")
                    || !traceContains(s, "battle P7 damage frame skill=2")) {
                throw new IllegalStateException(checkpoint + " expected skill2 HP/debuff to settle"
                        + " state=" + s.battleStateName
                        + " phase=" + s.battleP7Phase
                        + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                        + " expectedHp=" + expectedEnemyHp
                        + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                        + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(1)
                        + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(1)
                        + " trace=" + tailTrace(s, 60));
            }
            assertPhase10AStatusSlots(s, false, "skill2 debuff1 active",
                    new int[]{2}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=1", 700);
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
                        + " trace=" + tailTrace(s, 80));
            }
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("p12_body_visual_type12")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=1", 700);
            if (s.battleEnemyHp != hpBeforeTick
                    || runtime.debugEnemyDebuffDurationForSmoke(1) != 1
                    || s.battleP7PostEffectVisible) {
                throw new IllegalStateException(checkpoint + " expected debuff1 tick no HP/stat delta"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " trace=" + tailTrace(s, 80));
            }
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("tick_noop_duration1")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 1);
            if (runtime.debugEnemyDebuffDurationForSmoke(1) != 0
                    || s.battleEnemyStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected debuff1 to expire after duration 2"
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 60));
            }
            writeScenePng(s, new java.io.File(dir, skill2DiemKichPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(2);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=2 name=Diem kich description=Low fire damage, 10 percent chance Me Muoi for 2 turns\n"
                    + "aq.c[1][2]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[2]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(2)) + "\n"
                    + "aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "speffect0=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(0)) + "\n"
                    + "speffect14_debuffTick=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(14)) + "\n"
                    + "actorEffect=20 actorSprite=262 actorState=2 actorSide=enemy\n"
                    + "producerSpecialEffect=0 ahType=9\n"
                    + "formula=raw*100/100; debuffChance=10; debuffDuration=2; debuffTick=noop\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " playerAttack=" + playerAttack
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp + "\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " debuffDuration=" + debuffDuration
                    + " debuffSourceSkill=" + debuffSourceSkill + "\n"
                    + "hpSettled hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0) + "\n"
                    + "tick1 hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                    + " duration=1\n"
                    + "expiry finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 36) + "\n";
            Files.write(new java.io.File(dir, "battle_skill2_diem_kich_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuffChance=10 forcedRoll=0"
                    + " images=before,actor_u20_start,speffect0_type9,damage_debuff_frame,hp_settled_debuff_active,p12_body_visual_type12,tick_noop_duration1,expired");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill8LietDiemPhongBaoTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill8_liet_diem_phong_bao_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 8;
            VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
            SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
            assertSkill8LietDiemPhongBaoSourceRows(s, checkpoint);
            assertSkill8P3BeforeConfirm(s, runtime, checkpoint);

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
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("before")));

            runtime.debugSetSourceRandomSeedForSmoke(20260714L);
            runtime.debugSetNextDamageCritRollForSmoke(99);
            runtime.debugSetNextDamageDebuffRollForSmoke(0);
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
            assertSkill8P7ActorVisible(s, runtime, checkpoint);
            int actorEnemyHp = s.battleEnemyHp;
            int actorPp = runtime.debugPlayerSkillPpForSmoke(0);
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("actor_u20_start")));

            for (int i = 0; i < 180 && !s.battleP7SpecialVisible; i++) {
                s.tick();
            }
            if (!s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 9
                    || s.battleP7SpecialRow.length == 0
                    || !traceContainsAll(s, "battle P7 speffect skill=8",
                    "chunk=2", "speffect=0", "AH type 9")) {
                throw new IllegalStateException(checkpoint + " expected skill8 S60 P7 speffect0 AH type9"
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " type=" + s.battleP7SpecialType
                        + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                        + " trace=" + tailTrace(s, 80));
            }
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("speffect0_type9")));

            tickUntilBattleP7Phase(s, 2, 240);
            int damage = latestTraceDamage(s, "battle P7 damage frame skill=8");
            assertSkill8LietDiemPhongBaoDamageDebuffFrame(s, runtime, checkpoint, damage);
            int damageFrameEnemyHp = s.battleEnemyHp;
            String damageText = s.battleP7DamageText;
            String debuffText = s.battleP7DebuffText;
            int debuffDuration = runtime.debugEnemyDebuffDurationForSmoke(1);
            int debuffSourceSkill = runtime.debugEnemyDebuffSourceSkillForSmoke(1);
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("damage_debuff_frame")));

            int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
            int guard = 0;
            while ("P7".equals(s.battleStateName)
                    && s.battleEnemyHp > expectedEnemyHp
                    && guard++ < 240) {
                s.tick();
            }
            if (s.battleEnemyHp != expectedEnemyHp
                    || runtime.debugPlayerSkillPpForSmoke(0) != 14
                    || runtime.debugEnemyDebuffDurationForSmoke(1) != 2
                    || runtime.debugEnemyDebuffValueForSmoke(1) != 0
                    || runtime.debugEnemyDebuffSourceSkillForSmoke(1) != 8
                    || !traceContains(s, "SMOKE battle forced damage.debuff roll=0")
                    || !traceContains(s, "battle P7 source n() skill=8")
                    || !traceContains(s, "battle P7 actor u.a() start skill=8")
                    || !traceContains(s, "battle P7 damage frame skill=8")) {
                throw new IllegalStateException(checkpoint + " expected skill8 HP/debuff to settle"
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
            assertPhase10AStatusSlots(s, false, "skill8 debuff1 active",
                    new int[]{2}, new int[]{136});
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("hp_settled_debuff_active")));

            int hpBeforeTick = s.battleEnemyHp;
            tickUntilTraceContains(s, "active queue visual start bank=1 id=1", 700);
            if (!s.battleActiveQueueVisible
                    || s.battleActiveQueueBank != 1
                    || s.battleActiveQueueEffectId != 1
                    || !s.battleP7SpecialVisible
                    || s.battleP7SpecialType != 12
                    || !traceContainsAll(s, "battle P12 active queue visual",
                    "bank=1", "debuff=1", "speffect=14")) {
                throw new IllegalStateException(checkpoint + " expected skill8 debuff1 active queue speffect14"
                        + " activeVisible=" + s.battleActiveQueueVisible
                        + " bank=" + s.battleActiveQueueBank
                        + " effectId=" + s.battleActiveQueueEffectId
                        + " specialVisible=" + s.battleP7SpecialVisible
                        + " specialType=" + s.battleP7SpecialType
                        + " trace=" + tailTrace(s, 96));
            }
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("p12_body_visual_type12")));

            tickUntilTraceContains(s, "active queue apply bank=1 id=1", 700);
            if (s.battleEnemyHp != hpBeforeTick
                    || runtime.debugEnemyDebuffDurationForSmoke(1) != 1
                    || s.battleP7PostEffectVisible) {
                throw new IllegalStateException(checkpoint + " expected skill8 debuff1 tick no HP/stat delta"
                        + " hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " postVisible=" + s.battleP7PostEffectVisible
                        + " trace=" + tailTrace(s, 96));
            }
            assertPhase10AStatusSlots(s, false, "skill8 debuff1 after tick",
                    new int[]{2}, new int[]{135});
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("tick_noop_duration1")));

            runtime.debugTickEnemySourceDebuffForSmoke(s, 1);
            if (runtime.debugEnemyDebuffDurationForSmoke(1) != 0
                    || s.battleEnemyStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected skill8 debuff1 to expire"
                        + " duration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                        + " statusCount=" + s.battleEnemyStatusCount
                        + " trace=" + tailTrace(s, 80));
            }
            writeScenePng(s, new java.io.File(dir, skill8LietDiemPhongBaoPngName("expired")));
            writeScenePng(s, out);

            BattleSkillRow row = VqsvBattleTables.instance().skill(8);
            BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=8 name=Liet diem phong bao description=High fire damage, 20 percent chance Me Muoi for 2 turns\n"
                    + "aq.c[1][8]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[8]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(8)) + "\n"
                    + "aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw) + "\n"
                    + "speffect0=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(0)) + "\n"
                    + "speffect14_debuffTick=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(14)) + "\n"
                    + "actorEffect=20 actorSprite=262 actorState=7 actorSide=enemy\n"
                    + "producerSpecialEffect=0 ahType=9\n"
                    + "formula=raw*200/100 plus source jitter; debuffChance=20 forcedRoll=0; debuffDuration=2; debuffTick=noop\n"
                    + "before hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp
                    + " playerAttack=" + playerAttack
                    + " enemyDefense=" + enemyDefense + "\n"
                    + "actor hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + actorEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + actorPp + "\n"
                    + "damageFrame damage=" + damage
                    + " text=" + damageText
                    + " debuffText=" + debuffText
                    + " hpDisplay=" + damageFrameEnemyHp + "/" + s.battleEnemyMaxHp
                    + " debuffDuration=" + debuffDuration
                    + " debuffSourceSkill=" + debuffSourceSkill + "\n"
                    + "hpSettled hp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0) + "\n"
                    + "tick1 hp=" + hpBeforeTick + "->" + s.battleEnemyHp
                    + " duration=1\n"
                    + "expiry finalDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                    + " statusCount=" + s.battleEnemyStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "traceTail=" + tailTrace(s, 44) + "\n";
            Files.write(new java.io.File(dir, "battle_skill8_liet_diem_phong_bao_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " battleState=" + s.battleStateName
                    + " hp=" + beforePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + beforeEnemyHp + "/" + s.battleEnemyMaxHp
                    + "->" + expectedEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + beforePp + "->" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuffChance=20 forcedRoll=0"
                    + " images=before,actor_u20_start,speffect0_type9,damage_debuff_frame,"
                    + "hp_settled_debuff_active,p12_body_visual_type12,tick_noop_duration1,expired");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill3HoaVanTrieuTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill3_hoa_van_trieu_timeline".equals(checkpoint)) {
            return false;
        }
        try {
            int skillId = 3;
            java.io.File out = new java.io.File(outPath);
            java.io.File dir = out.getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Could not create smoke directory " + dir);
            }

            Skill3TimelineResult baseline = runSkill3HoaVanTrieuSingleTimeline(
                    checkpoint, dir, "baseline", false, 20260715L);
            Skill3TimelineResult conditional = runSkill3HoaVanTrieuSingleTimeline(
                    checkpoint, dir, "conditional_debuff0", true, 20260715L);
            if (conditional.damage <= baseline.damage
                    || conditional.enemyHpAfter >= baseline.enemyHpAfter
                    || !conditional.hadDebuff0
                    || baseline.hadDebuff0) {
                throw new IllegalStateException(checkpoint + " expected skill3 debuff0 branch to hit harder"
                        + " baselineDamage=" + baseline.damage
                        + " conditionalDamage=" + conditional.damage
                        + " baselineHpAfter=" + baseline.enemyHpAfter
                        + " conditionalHpAfter=" + conditional.enemyHpAfter
                        + " baselineHadDebuff0=" + baseline.hadDebuff0
                        + " conditionalHadDebuff0=" + conditional.hadDebuff0);
            }
            java.nio.file.Files.copy(
                    new java.io.File(dir, skill3HoaVanTrieuPngName("conditional_debuff0_hp_settled")).toPath(),
                    out.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            BattleSkillRow row = VqsvBattleTables.instance().skill(3);
            BattleDebuffRow debuff0 = VqsvBattleTables.instance().debuff(0);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=3 name=Hoa Van trieu description=Low fire damage; stronger if target has debuff0/Gieo Hat\n"
                    + "aq.c[1][3]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[3]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(3)) + "\n"
                    + "aq.c[7][0]=" + java.util.Arrays.toString(debuff0.raw) + "\n"
                    + "actorEffect=20 actorSprite=262 actorState=0 actorSide=enemy\n"
                    + "formulaNoDebuff0=raw*100/100\n"
                    + "formulaWithDebuff0=raw*120/100\n"
                    + "baseline before hp=" + baseline.beforePlayerHp + "/" + baseline.playerMaxHp
                    + ":" + baseline.beforeEnemyHp + "/" + baseline.enemyMaxHp
                    + " pp=" + baseline.beforePp + "\n"
                    + "baseline damage=" + baseline.damage
                    + " hpAfter=" + baseline.enemyHpAfter + "/" + baseline.enemyMaxHp
                    + " ppAfter=" + baseline.afterPp
                    + " debuffText=" + baseline.debuffText
                    + " hadDebuff0=" + baseline.hadDebuff0 + "\n"
                    + "conditional before hp=" + conditional.beforePlayerHp + "/" + conditional.playerMaxHp
                    + ":" + conditional.beforeEnemyHp + "/" + conditional.enemyMaxHp
                    + " pp=" + conditional.beforePp
                    + " preloadedDebuff0=true\n"
                    + "conditional damage=" + conditional.damage
                    + " hpAfter=" + conditional.enemyHpAfter + "/" + conditional.enemyMaxHp
                    + " ppAfter=" + conditional.afterPp
                    + " debuffText=" + conditional.debuffText
                    + " hadDebuff0=" + conditional.hadDebuff0 + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "baselineTraceTail=" + baseline.traceTail + "\n"
                    + "conditionalTraceTail=" + conditional.traceTail + "\n";
            Files.write(new java.io.File(dir, "battle_skill3_hoa_van_trieu_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " baselineDamage=" + baseline.damage
                    + " conditionalDamage=" + conditional.damage
                    + " hp=" + baseline.beforePlayerHp + "/" + baseline.playerMaxHp
                    + ":" + baseline.beforeEnemyHp + "/" + baseline.enemyMaxHp
                    + "->" + conditional.enemyHpAfter + "/" + conditional.enemyMaxHp
                    + " pp=" + baseline.beforePp + "->" + conditional.afterPp
                    + " images=baseline_before,baseline_actor_u20_start,baseline_damage_frame,baseline_hp_settled,"
                    + "conditional_debuff0_before,conditional_debuff0_actor_u20_start,conditional_debuff0_damage_frame,conditional_debuff0_hp_settled");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill9VinhHangHoaAnhTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill9_vinh_hang_hoa_anh_timeline".equals(checkpoint)) {
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

            Skill3TimelineResult baseline = runSkill9VinhHangHoaAnhSingleTimeline(
                    checkpoint, dir, "baseline", false, 20260715L);
            Skill3TimelineResult conditional = runSkill9VinhHangHoaAnhSingleTimeline(
                    checkpoint, dir, "conditional_debuff0", true, 20260715L);
            if (conditional.damage <= baseline.damage
                    || conditional.enemyHpAfter >= baseline.enemyHpAfter
                    || !conditional.hadDebuff0
                    || baseline.hadDebuff0) {
                throw new IllegalStateException(checkpoint + " expected skill9 debuff0 branch to hit harder"
                        + " baselineDamage=" + baseline.damage
                        + " conditionalDamage=" + conditional.damage
                        + " baselineHpAfter=" + baseline.enemyHpAfter
                        + " conditionalHpAfter=" + conditional.enemyHpAfter
                        + " baselineHadDebuff0=" + baseline.hadDebuff0
                        + " conditionalHadDebuff0=" + conditional.hadDebuff0);
            }
            Files.copy(new java.io.File(dir, skill9VinhHangHoaAnhPngName("baseline_actor_u20_start")).toPath(),
                    new java.io.File(dir, skill9VinhHangHoaAnhPngName("actor_u20_start")).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new java.io.File(dir, skill9VinhHangHoaAnhPngName("conditional_debuff0_hp_settled")).toPath(),
                    out.toPath(), StandardCopyOption.REPLACE_EXISTING);

            BattleSkillRow row = VqsvBattleTables.instance().skill(9);
            BattleDebuffRow debuff0 = VqsvBattleTables.instance().debuff(0);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=9 name=Vinh hang hoa anh description=High damage; stronger if target has debuff0/Gieo Hat\n"
                    + "aq.c[1][9]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[9]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(9)) + "\n"
                    + "aq.c[7][0]=" + java.util.Arrays.toString(debuff0.raw) + "\n"
                    + "actorEffect=20 actorSprite=262 actorState=0 actorSide=enemy\n"
                    + "formulaNoDebuff0=raw*200/100\n"
                    + "formulaWithDebuff0=raw*250/100\n"
                    + "baseline before hp=" + baseline.beforePlayerHp + "/" + baseline.playerMaxHp
                    + ":" + baseline.beforeEnemyHp + "/" + baseline.enemyMaxHp
                    + " pp=" + baseline.beforePp + "\n"
                    + "baseline damage=" + baseline.damage
                    + " hpAfter=" + baseline.enemyHpAfter + "/" + baseline.enemyMaxHp
                    + " ppAfter=" + baseline.afterPp
                    + " debuffText=" + baseline.debuffText
                    + " hadDebuff0=" + baseline.hadDebuff0 + "\n"
                    + "conditional before hp=" + conditional.beforePlayerHp + "/" + conditional.playerMaxHp
                    + ":" + conditional.beforeEnemyHp + "/" + conditional.enemyMaxHp
                    + " pp=" + conditional.beforePp
                    + " preloadedDebuff0=true\n"
                    + "conditional damage=" + conditional.damage
                    + " hpAfter=" + conditional.enemyHpAfter + "/" + conditional.enemyMaxHp
                    + " ppAfter=" + conditional.afterPp
                    + " debuffText=" + conditional.debuffText
                    + " hadDebuff0=" + conditional.hadDebuff0 + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "baselineTraceTail=" + baseline.traceTail + "\n"
                    + "conditionalTraceTail=" + conditional.traceTail + "\n";
            Files.write(new java.io.File(dir, "battle_skill9_vinh_hang_hoa_anh_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " baselineDamage=" + baseline.damage
                    + " conditionalDamage=" + conditional.damage
                    + " hp=" + baseline.beforePlayerHp + "/" + baseline.playerMaxHp
                    + ":" + baseline.beforeEnemyHp + "/" + baseline.enemyMaxHp
                    + "->" + conditional.enemyHpAfter + "/" + conditional.enemyMaxHp
                    + " pp=" + baseline.beforePp + "->" + conditional.afterPp
                    + " images=baseline_before,baseline_actor_u20_start,baseline_damage_frame,baseline_hp_settled,"
                    + "conditional_debuff0_before,conditional_debuff0_actor_u20_start,conditional_debuff0_damage_frame,conditional_debuff0_hp_settled");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runFireAnimationContactSheetSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_fire_animation_contact_sheet".equals(checkpoint)) {
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
            java.io.File timelines = new java.io.File(dir, "fire_skill_timelines");
            if (!timelines.exists() && !timelines.mkdirs()) {
                throw new IllegalStateException("Could not create fire timeline directory " + timelines);
            }

            runSkillDirectTimelineSmokeIfNeeded("battle_skill0_direct_timeline",
                    new java.io.File(timelines, "skill0/battle_skill0_direct_timeline.png").getPath());
            runSkill1DuongViemTimelineSmokeIfNeeded("battle_skill1_duong_viem_timeline",
                    new java.io.File(timelines, "skill1/battle_skill1_duong_viem_timeline.png").getPath());
            runSkill2DiemKichTimelineSmokeIfNeeded("battle_skill2_diem_kich_timeline",
                    new java.io.File(timelines, "skill2/battle_skill2_diem_kich_timeline.png").getPath());
            runSkill3HoaVanTrieuTimelineSmokeIfNeeded("battle_skill3_hoa_van_trieu_timeline",
                    new java.io.File(timelines, "skill3/battle_skill3_hoa_van_trieu_timeline.png").getPath());
            runSkill4ThienHoaTeTimelineSmokeIfNeeded("battle_skill4_thien_hoa_te_timeline",
                    new java.io.File(timelines, "skill4/battle_skill4_thien_hoa_te_timeline.png").getPath());
            runSkill5ViemLoiPhaTimelineSmokeIfNeeded("battle_skill5_viem_loi_pha_timeline",
                    new java.io.File(timelines, "skill5/battle_skill5_viem_loi_pha_timeline.png").getPath());
            runSkillDirectTimelineSmokeIfNeeded("battle_skill6_direct_timeline",
                    new java.io.File(timelines, "skill6/battle_skill6_direct_timeline.png").getPath());
            runSkill7ChuocNhietChiXucTimelineSmokeIfNeeded("battle_skill7_chuoc_nhiet_chi_xuc_timeline",
                    new java.io.File(timelines, "skill7/battle_skill7_chuoc_nhiet_chi_xuc_timeline.png").getPath());
            runSkill8LietDiemPhongBaoTimelineSmokeIfNeeded("battle_skill8_liet_diem_phong_bao_timeline",
                    new java.io.File(timelines, "skill8/battle_skill8_liet_diem_phong_bao_timeline.png").getPath());
            runSkill9VinhHangHoaAnhTimelineSmokeIfNeeded("battle_skill9_vinh_hang_hoa_anh_timeline",
                    new java.io.File(timelines, "skill9/battle_skill9_vinh_hang_hoa_anh_timeline.png").getPath());

            FireAnimationCell[] cells = {
                    new FireAnimationCell(0, "Hoa trao", "actor-only",
                            "effect.mid[0]: u20 -> sprite262",
                            new java.io.File(timelines, "skill0/" + directTimelinePngName(0, "actor_u20_start"))),
                    new FireAnimationCell(1, "Duong viem", "actor-only + debuff0 later",
                            "effect.mid[1]: u20 -> sprite262",
                            new java.io.File(timelines, "skill1/" + skill1DuongViemPngName("actor_u20_start"))),
                    new FireAnimationCell(2, "Diem kich", "actor + speffect",
                            "u20 states 2/3, then speffect0 / AH9",
                            new java.io.File(timelines, "skill2/" + skill2DiemKichPngName("speffect0_type9"))),
                    new FireAnimationCell(3, "Hoa Van trieu", "actor-only conditional",
                            "effect.mid[3]: u20 -> sprite262",
                            new java.io.File(timelines, "skill3/" + skill3HoaVanTrieuPngName("baseline_actor_u20_start"))),
                    new FireAnimationCell(4, "Thien Hoa te", "self buff speffect",
                            "speffect16 / AH9 -> speffect15 / AH1",
                            new java.io.File(timelines, "skill4/" + skill4ThienHoaTePngName("speffect16_type9"))),
                    new FireAnimationCell(5, "Viem loi pha", "self buff speffect",
                            "speffect16 / AH9 -> speffect15 / AH1",
                            new java.io.File(timelines, "skill5/" + skill5ViemLoiPhaPngName("speffect16_type9"))),
                    new FireAnimationCell(6, "Hoa diem dao", "actor-only",
                            "effect.mid[6]: u20 -> sprite262",
                            new java.io.File(timelines, "skill6/" + directTimelinePngName(6, "actor_u20_start"))),
                    new FireAnimationCell(7, "Chuoc nhiet chi xuc", "actor-only + debuff0 later",
                            "effect.mid[7]: u20 -> sprite262",
                            new java.io.File(timelines, "skill7/" + skill7ChuocNhietChiXucPngName("actor_u20_start"))),
                    new FireAnimationCell(8, "Liet diem phong bao", "actor + speffect",
                            "u20 states 7/3, then speffect0 / AH9",
                            new java.io.File(timelines, "skill8/" + skill8LietDiemPhongBaoPngName("speffect0_type9"))),
                    new FireAnimationCell(9, "Vinh hang hoa anh", "actor-only conditional",
                            "effect.mid[9]: u20 -> sprite262",
                            new java.io.File(timelines, "skill9/" + skill9VinhHangHoaAnhPngName("actor_u20_start")))
            };
            writeFireAnimationContactSheet(cells, out);
            writeFireAnimationContactSheetNotes(cells, new java.io.File(dir, "battle_fire_animation_contact_sheet_notes.md"));
            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " cells=10 groups=actor-only,actor+speffect,self-buff-speffect");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    static boolean runSkill4ThienHoaTeTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill4_thien_hoa_te_timeline".equals(checkpoint)) {
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

            Skill4ProducerResult producer = runSkill4ThienHoaTeProducerTimeline(checkpoint, dir);
            Skill4HookResult baselineHook = runSkill4Buff0HookDamageFrame(
                    checkpoint, dir, "baseline_no_buff0_hook", false);
            Skill4HookResult hooked = runSkill4Buff0HookDamageFrame(
                    checkpoint, dir, "hook_duration0", true);
            Skill4ExpiryResult expiry = runSkill4Buff0ExpiryTimeline(checkpoint, dir);
            if (baselineHook.damage != 80
                    || hooked.damage != 308
                    || hooked.damage != baselineHook.damage + hooked.storedExtra
                    || producer.storedExtra != 38
                    || expiry.afterDefense != expiry.baseDefense
                    || expiry.afterStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected skill4 buff0 hook/expiry numeric parity"
                        + " baselineDamage=" + baselineHook.damage
                        + " hookedDamage=" + hooked.damage
                        + " producerStoredExtra=" + producer.storedExtra
                        + " hookStoredExtra=" + hooked.storedExtra
                        + " expiryDefense=" + expiry.afterDefense
                        + " baseDefense=" + expiry.baseDefense
                        + " statusCount=" + expiry.afterStatusCount);
            }

            java.nio.file.Files.copy(
                    new java.io.File(dir, skill4ThienHoaTePngName("after_apply_icon")).toPath(),
                    out.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            BattleSkillRow row = VqsvBattleTables.instance().skill(4);
            BattleBuffRow buff = VqsvBattleTables.instance().buff(0);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=4 name=Thien Hoa te description=No-damage buff0/Suc Luc producer\n"
                    + "aq.c[1][4]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[4]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(4)) + "\n"
                    + "speffect16=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(16)) + "\n"
                    + "speffect15=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(15)) + "\n"
                    + "aq.c[6][0]=" + java.util.Arrays.toString(buff.raw) + "\n"
                    + "logic=no damage; apply buff0 to player/self; defense += baseDefense*30/100; storedExtra=190%*rawAttackSnapshot\n"
                    + "producer before hp=" + producer.beforePlayerHp + "/" + producer.playerMaxHp
                    + ":" + producer.beforeEnemyHp + "/" + producer.enemyMaxHp
                    + " pp=" + producer.beforePp
                    + " defense=" + producer.baseDefense + "\n"
                    + "producer after hp=" + producer.afterPlayerHp + "/" + producer.playerMaxHp
                    + ":" + producer.afterEnemyHp + "/" + producer.enemyMaxHp
                    + " pp=" + producer.afterPp
                    + " defense=" + producer.currentDefense
                    + " buffValue=" + producer.buffValue
                    + " storedExtra=" + producer.storedExtra
                    + " duration=" + producer.duration
                    + " icon=12 durationCell=136\n"
                    + "hook baselineDamage=" + baselineHook.damage
                    + " hookedDamage=" + hooked.damage
                    + " hookStoredExtra=" + hooked.storedExtra
                    + " formula=80+228\n"
                    + "expiry duration=2->1->0 defense=" + expiry.baseDefense + "->"
                    + expiry.midDefense + "->" + expiry.afterDefense
                    + " statusCountAfter=" + expiry.afterStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "producerTraceTail=" + producer.traceTail + "\n"
                    + "hookTraceTail=" + hooked.traceTail + "\n"
                    + "expiryTraceTail=" + expiry.traceTail + "\n";
            Files.write(new java.io.File(dir, "battle_skill4_thien_hoa_te_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill4Pp=" + producer.beforePp + "->" + producer.afterPp
                    + " defense=" + producer.baseDefense + "->" + producer.currentDefense
                    + " storedExtra=" + producer.storedExtra
                    + " baselineDamage=" + baselineHook.damage
                    + " hookedDamage=" + hooked.damage
                    + " expiryDefense=" + expiry.afterDefense
                    + " images=before,speffect16_type9,speffect15_type1,after_apply_icon,"
                    + "baseline_no_buff0_hook_damage_frame,hook_duration0_damage_frame,"
                    + "expiry_before_tick,expiry_after_first_tick,expiry_after_clear");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static Skill4ProducerResult runSkill4ThienHoaTeProducerTimeline(
            String checkpoint, java.io.File dir) throws Exception {
        int skillId = 4;
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
        runtime.debugSetPlayerAttackForSmoke(s, 120);
        runtime.debugSetPlayerDefenseForSmoke(s, 100);
        assertSkill4ThienHoaTeSourceRows(s, checkpoint);
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != skillId
                || runtime.debugPlayerSkillPpForSmoke(0) != 10
                || runtime.debugPlayerHasBuffForSmoke(0)
                || runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 100) {
            throw new IllegalStateException(checkpoint + " expected skill4 before confirm"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasBuff0=" + runtime.debugPlayerHasBuffForSmoke(0)
                    + " defense=" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                    + " trace=" + tailTrace(s, 24));
        }
        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        int baseDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("before")));

        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
        tickUntilBattleP7Phase(s, 1, 120);
        for (int i = 0; i < 60 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 9); i++) {
            s.tick();
        }
        if (!s.battleP7SpecialVisible
                || s.battleP7SpecialType != 9
                || !s.battleP7SpecialOnPlayerSide
                || runtime.debugPlayerSkillPpForSmoke(0) != 9
                || !traceContains(s, "battle P7 speffect skill=4")
                || !traceContains(s, "speffect=16")) {
            throw new IllegalStateException(checkpoint + " expected skill4 speffect16 AH type9"
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " type=" + s.battleP7SpecialType
                    + " playerSide=" + s.battleP7SpecialOnPlayerSide
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                    + " trace=" + tailTrace(s, 48));
        }
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("speffect16_type9")));

        for (int i = 0; i < 180 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 1); i++) {
            s.tick();
        }
        if (!s.battleP7SpecialVisible
                || s.battleP7SpecialType != 1
                || !s.battleP7SpecialOnPlayerSide
                || !traceContains(s, "battle P7 speffect skill=4")
                || !traceContains(s, "speffect=15")) {
            throw new IllegalStateException(checkpoint + " expected skill4 speffect15 AH type1"
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " type=" + s.battleP7SpecialType
                    + " playerSide=" + s.battleP7SpecialOnPlayerSide
                    + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                    + " trace=" + tailTrace(s, 48));
        }
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("speffect15_type1")));

        tickUntilBattleP7Phase(s, 3, 340);
        int currentDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        int buffValue = runtime.debugPlayerBuffValueForSmoke(0);
        int storedExtra = runtime.debugPlayerBuffSecondaryValueForSmoke(0);
        int duration = runtime.debugPlayerBuffDurationForSmoke(0);
        if (!s.battleP7PostEffectVisible
                || !s.battleP7PostEffectPlayerSide
                || !runtime.debugPlayerHasBuffForSmoke(0)
                || buffValue != 30
                || storedExtra != 38
                || duration != 2
                || currentDefense != 130
                || s.battlePlayerHp != beforePlayerHp
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 9
                || traceContains(s, "battle P7 damage frame skill=4")
                || traceContains(s, "battle P7 hitroll skill=4")
                || !traceContains(s, "battle P7 no-damage skill=4")
                || !traceContains(s, "game.d.q postEffect skill=4")
                || !traceContains(s, "buffId=0")) {
            throw new IllegalStateException(checkpoint + " expected skill4 to apply buff0 without damage"
                    + " postVisible=" + s.battleP7PostEffectVisible
                    + " playerSide=" + s.battleP7PostEffectPlayerSide
                    + " hasBuff0=" + runtime.debugPlayerHasBuffForSmoke(0)
                    + " value=" + buffValue
                    + " storedExtra=" + storedExtra
                    + " duration=" + duration
                    + " defense=" + baseDefense + "->" + currentDefense
                    + " hp=" + s.battlePlayerHp + "/" + s.battleEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 72));
        }
        assertPhase10AStatusSlots(s, true, "skill4 buff0 after apply",
                new int[]{12}, new int[]{136});
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("after_apply_icon")));
        return new Skill4ProducerResult(beforePlayerHp, s.battlePlayerMaxHp,
                beforeEnemyHp, s.battleEnemyMaxHp, beforePp,
                s.battlePlayerHp, s.battleEnemyHp, runtime.debugPlayerSkillPpForSmoke(0),
                baseDefense, currentDefense, buffValue, storedExtra, duration,
                tailTrace(s, 48));
    }

    private static Skill4HookResult runSkill4Buff0HookDamageFrame(
            String checkpoint, java.io.File dir, String label, boolean forceDurationZero) throws Exception {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        s.eventIndex = s.events.size();
        s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
        SourceBattleRuntime runtime = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
        s.current = runtime;
        tickUntilBattleState(s, "P20", 120);
        runtime.debugSetPlayerAttackForSmoke(s, 120);
        runtime.debugSetPlayerDefenseForSmoke(s, 100);
        runtime.debugSetEnemyDefenseForSmoke(s, 40);
        if (forceDurationZero) {
            runtime.debugPlayerSourceBuffForSmoke(s, 0, 0, 4);
            runtime.debugSetPlayerBuffDurationForSmoke(s, 0, 0);
        }
        BattleUnit.setDamageRandomSeedForChecks(0L);
        runtime.debugSetNextDamageCritRollForSmoke(99);
        runtime.debugSetNextP7HitRollForSmoke(99);
        s.battleClickX = 20;
        s.battleClickY = 300;
        tickUntilBattleState(s, "P3", 80);
        for (int i = 0; i < 10; i++) {
            s.tick();
        }
        for (int i = 0; i < 18 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
        tickUntilBattleP7Phase(s, 2, 180);
        int damage = latestTraceDamage(s, "battle P7 damage frame skill=10");
        int expected = forceDurationZero ? 308 : 80;
        if (damage != expected
                || !s.battleP7DamageVisible
                || !traceContains(s, "battle P7 damage frame skill=10")
                || !traceContains(s, "hit=true")
                || (forceDurationZero && (!runtime.debugPlayerHasBuffForSmoke(0)
                || runtime.debugPlayerBuffSecondaryValueForSmoke(0) != 228))) {
            throw new IllegalStateException(checkpoint + " expected skill4 buff0 hook frame"
                    + " label=" + label
                    + " forceDurationZero=" + forceDurationZero
                    + " damage=" + damage
                    + " expected=" + expected
                    + " visible=" + s.battleP7DamageVisible
                    + " hasBuff0=" + runtime.debugPlayerHasBuffForSmoke(0)
                    + " storedExtra=" + runtime.debugPlayerBuffSecondaryValueForSmoke(0)
                    + " trace=" + tailTrace(s, 72));
        }
        if (forceDurationZero) {
            s.sourceStateTrace.add("SMOKE verified skill4 buff0 duration0 damage hook"
                    + " baseline=80"
                    + " hooked=" + damage
                    + " storedExtra=" + runtime.debugPlayerBuffSecondaryValueForSmoke(0)
                    + " formula=80+228"
                    + " source=game.b.b(target) v[0][0]==0 hook");
        }
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName(label + "_damage_frame")));
        return new Skill4HookResult(damage, runtime.debugPlayerBuffSecondaryValueForSmoke(0),
                tailTrace(s, 36));
    }

    private static Skill4ExpiryResult runSkill4Buff0ExpiryTimeline(
            String checkpoint, java.io.File dir) throws Exception {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = VqsvSmokeHarness.setupPhase10AStatusBattle(s);
        runtime.debugSetPlayerAttackForSmoke(s, 120);
        runtime.debugSetPlayerDefenseForSmoke(s, 100);
        runtime.debugPlayerSourceBuffForSmoke(s, 0, 0, 4);
        int baseDefense = runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE);
        int beforeDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (!runtime.debugPlayerHasBuffForSmoke(0)
                || beforeDefense != 130
                || runtime.debugPlayerBuffDurationForSmoke(0) != 2) {
            throw new IllegalStateException(checkpoint + " expected skill4 expiry setup"
                    + " baseDefense=" + baseDefense
                    + " beforeDefense=" + beforeDefense
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(0)
                    + " trace=" + tailTrace(s, 36));
        }
        assertPhase10AStatusSlots(s, true, "skill4 expiry before tick",
                new int[]{12}, new int[]{136});
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("expiry_before_tick")));
        runtime.debugTickPlayerSourceBuffForSmoke(s, 0);
        int midDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (!runtime.debugPlayerHasBuffForSmoke(0)
                || runtime.debugPlayerBuffDurationForSmoke(0) != 1
                || midDefense != 130) {
            throw new IllegalStateException(checkpoint + " expected skill4 first expiry tick"
                    + " midDefense=" + midDefense
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(0)
                    + " trace=" + tailTrace(s, 36));
        }
        assertPhase10AStatusSlots(s, true, "skill4 expiry after first tick",
                new int[]{12}, new int[]{135});
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("expiry_after_first_tick")));
        runtime.debugTickPlayerSourceBuffForSmoke(s, 0);
        int afterDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (runtime.debugPlayerHasBuffForSmoke(0)
                || runtime.debugPlayerBuffDurationForSmoke(0) != 0
                || afterDefense != baseDefense
                || s.battlePlayerStatusCount != 0) {
            throw new IllegalStateException(checkpoint + " expected skill4 buff0 expiry clear"
                    + " active=" + runtime.debugPlayerHasBuffForSmoke(0)
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(0)
                    + " defense=" + afterDefense
                    + " statusCount=" + s.battlePlayerStatusCount
                    + " trace=" + tailTrace(s, 48));
        }
        writeScenePng(s, new java.io.File(dir, skill4ThienHoaTePngName("expiry_after_clear")));
        return new Skill4ExpiryResult(baseDefense, beforeDefense, midDefense, afterDefense,
                s.battlePlayerStatusCount, tailTrace(s, 36));
    }

    private static String skill4ThienHoaTePngName(String suffix) {
        return "battle_skill4_thien_hoa_te_timeline_" + suffix + ".png";
    }

    private static void assertSkill4ThienHoaTeSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(4);
        BattleBuffRow buff = VqsvBattleTables.instance().buff(0);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(4);
        byte[] expectedEffect = new byte[]{0, 0, 30, 0, 0, -1, 0, 0, 1, 16, 0, -1, -1, 0, 0, 1, 15, 0, -1, -1, 0};
        short[] speffect16 = VqsvBattleAnimationTables.instance().speffectRow(16);
        short[] speffect15 = VqsvBattleAnimationTables.instance().speffectRow(15);
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 121
                || row.descriptionTextId != 533
                || row.powerPercent != 0
                || row.ppMax != 10
                || row.effectMode != 1
                || row.effectId != 0
                || row.chanceOrParam != -1
                || row.targetSide != 1
                || buff == null
                || buff.duration != 2
                || buff.paramA != 30
                || buff.paramB != 190
                || speffect16.length == 0
                || speffect16[0] != 9
                || speffect15.length == 0
                || speffect15[0] != 1
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill4 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " buff=" + (buff == null ? "null" : java.util.Arrays.toString(buff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect16=" + java.util.Arrays.toString(speffect16)
                    + " speffect15=" + java.util.Arrays.toString(speffect15)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill4 Thien Hoa te source rows"
                + " aq.c[1][4]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[6][0]=" + java.util.Arrays.toString(buff.raw)
                + " effect.mid[4]=" + java.util.Arrays.toString(effect)
                + " speffect16=" + java.util.Arrays.toString(speffect16)
                + " speffect15=" + java.util.Arrays.toString(speffect15)
                + " name=" + row.name("skill4")
                + " buffName=" + buff.name("buff0"));
    }

    private static final class Skill4ProducerResult {
        final int beforePlayerHp;
        final int playerMaxHp;
        final int beforeEnemyHp;
        final int enemyMaxHp;
        final int beforePp;
        final int afterPlayerHp;
        final int afterEnemyHp;
        final int afterPp;
        final int baseDefense;
        final int currentDefense;
        final int buffValue;
        final int storedExtra;
        final int duration;
        final String traceTail;

        Skill4ProducerResult(int beforePlayerHp, int playerMaxHp, int beforeEnemyHp, int enemyMaxHp,
                             int beforePp, int afterPlayerHp, int afterEnemyHp, int afterPp,
                             int baseDefense, int currentDefense, int buffValue, int storedExtra,
                             int duration, String traceTail) {
            this.beforePlayerHp = beforePlayerHp;
            this.playerMaxHp = playerMaxHp;
            this.beforeEnemyHp = beforeEnemyHp;
            this.enemyMaxHp = enemyMaxHp;
            this.beforePp = beforePp;
            this.afterPlayerHp = afterPlayerHp;
            this.afterEnemyHp = afterEnemyHp;
            this.afterPp = afterPp;
            this.baseDefense = baseDefense;
            this.currentDefense = currentDefense;
            this.buffValue = buffValue;
            this.storedExtra = storedExtra;
            this.duration = duration;
            this.traceTail = traceTail;
        }
    }

    private static final class Skill4HookResult {
        final int damage;
        final int storedExtra;
        final String traceTail;

        Skill4HookResult(int damage, int storedExtra, String traceTail) {
            this.damage = damage;
            this.storedExtra = storedExtra;
            this.traceTail = traceTail;
        }
    }

    private static final class Skill4ExpiryResult {
        final int baseDefense;
        final int beforeDefense;
        final int midDefense;
        final int afterDefense;
        final int afterStatusCount;
        final String traceTail;

        Skill4ExpiryResult(int baseDefense, int beforeDefense, int midDefense,
                           int afterDefense, int afterStatusCount, String traceTail) {
            this.baseDefense = baseDefense;
            this.beforeDefense = beforeDefense;
            this.midDefense = midDefense;
            this.afterDefense = afterDefense;
            this.afterStatusCount = afterStatusCount;
            this.traceTail = traceTail;
        }
    }

    static boolean runSkill5ViemLoiPhaTimelineSmokeIfNeeded(String checkpoint, String outPath) {
        if (!"battle_skill5_viem_loi_pha_timeline".equals(checkpoint)) {
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

            Skill5ProducerResult producer = runSkill5ViemLoiPhaProducerTimeline(checkpoint, dir);
            Skill5DamageProbeResult baseline = runSkill5Buff1DamageProbe(
                    checkpoint, dir, "baseline_no_buff1", false, false, 99, 99, false);
            Skill5DamageProbeResult forcedHit = runSkill5Buff1DamageProbe(
                    checkpoint, dir, "forced_hit_buff1", true, false, 99, 99, false);
            Skill5DamageProbeResult forcedMiss = runSkill5Buff1DamageProbe(
                    checkpoint, dir, "forced_miss_buff1", true, false, 99, 0, true);
            Skill5DamageProbeResult forcedCrit = runSkill5Buff1DamageProbe(
                    checkpoint, dir, "forced_crit_buff1", true, false, 0, 99, false);
            Skill5DamageProbeResult expired = runSkill5Buff1DamageProbe(
                    checkpoint, dir, "expired_buff1", true, true, 99, 99, false);
            Skill5ExpiryResult expiry = runSkill5Buff1ExpiryTimeline(checkpoint, dir);

            if (baseline.damage != 80
                    || forcedHit.damage != 120
                    || forcedMiss.enemyHpAfter != forcedMiss.enemyMaxHp
                    || !forcedMiss.miss
                    || forcedCrit.damage <= forcedHit.damage
                    || !forcedCrit.critical
                    || expired.damage != baseline.damage
                    || expired.currentDefense != expired.baseDefense
                    || producer.currentDefense != 50
                    || producer.buffValue != 50
                    || producer.damageBonusPercent != 50
                    || producer.duration != 3
                    || expiry.afterDefense != expiry.baseDefense
                    || expiry.afterStatusCount != 0) {
                throw new IllegalStateException(checkpoint + " expected skill5 buff1 numeric parity"
                        + " baselineDamage=" + baseline.damage
                        + " forcedHit=" + forcedHit.damage
                        + " forcedMissHp=" + forcedMiss.enemyHpAfter + "/" + forcedMiss.enemyMaxHp
                        + " forcedMiss=" + forcedMiss.miss
                        + " forcedCrit=" + forcedCrit.damage
                        + " forcedCritFlag=" + forcedCrit.critical
                        + " expiredDamage=" + expired.damage
                        + " expiredDefense=" + expired.currentDefense + "/" + expired.baseDefense
                        + " producerDefense=" + producer.currentDefense
                        + " producerValue=" + producer.buffValue
                        + " producerBonus=" + producer.damageBonusPercent
                        + " producerDuration=" + producer.duration
                        + " expiryDefense=" + expiry.afterDefense
                        + " statusCount=" + expiry.afterStatusCount);
            }

            java.nio.file.Files.copy(
                    new java.io.File(dir, skill5ViemLoiPhaPngName("after_apply_icon")).toPath(),
                    out.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            BattleSkillRow row = VqsvBattleTables.instance().skill(5);
            BattleBuffRow buff = VqsvBattleTables.instance().buff(1);
            String debug = ""
                    + "checkpoint=" + checkpoint + "\n"
                    + "skill=5 name=Viem loi pha description=No-damage buff1/Pha Phu producer\n"
                    + "aq.c[1][5]=" + java.util.Arrays.toString(row.raw) + "\n"
                    + "effect.mid[5]="
                    + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().effectRow(5)) + "\n"
                    + "speffect16=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(16)) + "\n"
                    + "speffect15=" + java.util.Arrays.toString(VqsvBattleAnimationTables.instance().speffectRow(15)) + "\n"
                    + "aq.c[6][1]=" + java.util.Arrays.toString(buff.raw) + "\n"
                    + "logic=no damage; apply buff1 to player/self; defense -= baseDefense*50/100; outgoing damage += damage*50/100\n"
                    + "producer before hp=" + producer.beforePlayerHp + "/" + producer.playerMaxHp
                    + ":" + producer.beforeEnemyHp + "/" + producer.enemyMaxHp
                    + " pp=" + producer.beforePp
                    + " defense=" + producer.baseDefense + "\n"
                    + "producer after hp=" + producer.afterPlayerHp + "/" + producer.playerMaxHp
                    + ":" + producer.afterEnemyHp + "/" + producer.enemyMaxHp
                    + " pp=" + producer.afterPp
                    + " defense=" + producer.currentDefense
                    + " buffValue=" + producer.buffValue
                    + " damageBonusPercent=" + producer.damageBonusPercent
                    + " duration=" + producer.duration
                    + " icon=13 durationCell=137\n"
                    + "damage baseline=" + baseline.damage
                    + " forcedHitBuff1=" + forcedHit.damage
                    + " formula=80+80*50/100\n"
                    + "forcedMiss hp=" + forcedMiss.enemyHpAfter + "/" + forcedMiss.enemyMaxHp
                    + " damageText='" + forcedMiss.damageText + "' missText='" + forcedMiss.missText + "'\n"
                    + "forcedCrit damage=" + forcedCrit.damage
                    + " critical=" + forcedCrit.critical + "\n"
                    + "expired damage=" + expired.damage
                    + " defense=" + expired.currentDefense
                    + " duration=" + expired.duration + "\n"
                    + "expiry duration=3->2->1->0 defense=" + expiry.baseDefense + "->"
                    + expiry.firstDefense + "->" + expiry.secondDefense + "->" + expiry.afterDefense
                    + " statusCountAfter=" + expiry.afterStatusCount + "\n"
                    + "status=PORTED/PARTIAL exact MIDP pixel parity pending\n"
                    + "producerTraceTail=" + producer.traceTail + "\n"
                    + "hitTraceTail=" + forcedHit.traceTail + "\n"
                    + "missTraceTail=" + forcedMiss.traceTail + "\n"
                    + "critTraceTail=" + forcedCrit.traceTail + "\n"
                    + "expiryTraceTail=" + expiry.traceTail + "\n";
            Files.write(new java.io.File(dir, "battle_skill5_viem_loi_pha_timeline_debug.txt").toPath(),
                    debug.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("smoke-checkpoint-ok " + checkpoint + " " + outPath
                    + " skill5Pp=" + producer.beforePp + "->" + producer.afterPp
                    + " defense=" + producer.baseDefense + "->" + producer.currentDefense
                    + " damage=" + baseline.damage + "->" + forcedHit.damage
                    + " missHp=" + forcedMiss.enemyHpAfter + "/" + forcedMiss.enemyMaxHp
                    + " critDamage=" + forcedCrit.damage
                    + " expiryDefense=" + expiry.afterDefense
                    + " images=before,speffect16_type9,speffect15_type1,after_apply_icon,"
                    + "baseline_no_buff1_damage_frame,forced_hit_buff1_damage_frame,"
                    + "forced_miss_buff1_damage_frame,forced_crit_buff1_damage_frame,"
                    + "expiry_before_tick,expiry_after_first_tick,expiry_after_second_tick,expiry_after_clear");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return true;
        }
    }

    private static Skill5ProducerResult runSkill5ViemLoiPhaProducerTimeline(
            String checkpoint, java.io.File dir) throws Exception {
        int skillId = 5;
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
        runtime.debugSetPlayerAttackForSmoke(s, 120);
        runtime.debugSetPlayerDefenseForSmoke(s, 100);
        assertSkill5ViemLoiPhaSourceRows(s, checkpoint);
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != skillId
                || runtime.debugPlayerSkillPpForSmoke(0) != 10
                || runtime.debugPlayerHasBuffForSmoke(1)
                || runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE) != 100) {
            throw new IllegalStateException(checkpoint + " expected skill5 before confirm"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hasBuff1=" + runtime.debugPlayerHasBuffForSmoke(1)
                    + " defense=" + runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE)
                    + " trace=" + tailTrace(s, 24));
        }
        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        int baseDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("before")));

        for (int i = 0; i < 24 && !"P7".equals(s.battleStateName); i++) {
            s.press0();
            s.tick();
        }
        tickUntilBattleState(s, "P7", 120);
        tickUntilBattleP7Phase(s, 1, 120);
        for (int i = 0; i < 60 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 9); i++) {
            s.tick();
        }
        if (!s.battleP7SpecialVisible
                || s.battleP7SpecialType != 9
                || !s.battleP7SpecialOnPlayerSide
                || runtime.debugPlayerSkillPpForSmoke(0) != 9
                || !traceContains(s, "battle P7 speffect skill=5")
                || !traceContains(s, "speffect=16")) {
            throw new IllegalStateException(checkpoint + " expected skill5 speffect16 AH type9"
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " type=" + s.battleP7SpecialType
                    + " playerSide=" + s.battleP7SpecialOnPlayerSide
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                    + " trace=" + tailTrace(s, 48));
        }
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("speffect16_type9")));

        for (int i = 0; i < 180 && (!s.battleP7SpecialVisible || s.battleP7SpecialType != 1); i++) {
            s.tick();
        }
        if (!s.battleP7SpecialVisible
                || s.battleP7SpecialType != 1
                || !s.battleP7SpecialOnPlayerSide
                || !traceContains(s, "battle P7 speffect skill=5")
                || !traceContains(s, "speffect=15")) {
            throw new IllegalStateException(checkpoint + " expected skill5 speffect15 AH type1"
                    + " specialVisible=" + s.battleP7SpecialVisible
                    + " type=" + s.battleP7SpecialType
                    + " playerSide=" + s.battleP7SpecialOnPlayerSide
                    + " row=" + java.util.Arrays.toString(s.battleP7SpecialRow)
                    + " trace=" + tailTrace(s, 48));
        }
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("speffect15_type1")));

        tickUntilBattleP7Phase(s, 3, 340);
        int currentDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        int buffValue = runtime.debugPlayerBuffValueForSmoke(1);
        int damageBonusPercent = runtime.debugPlayerBuffSecondaryValueForSmoke(1);
        int duration = runtime.debugPlayerBuffDurationForSmoke(1);
        if (!s.battleP7PostEffectVisible
                || !s.battleP7PostEffectPlayerSide
                || !runtime.debugPlayerHasBuffForSmoke(1)
                || buffValue != 50
                || damageBonusPercent != 50
                || duration != 3
                || currentDefense != 50
                || s.battlePlayerHp != beforePlayerHp
                || s.battleEnemyHp != beforeEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 9
                || traceContains(s, "battle P7 damage frame skill=5")
                || traceContains(s, "battle P7 hitroll skill=5")
                || !traceContains(s, "battle P7 no-damage skill=5")
                || !traceContains(s, "game.d.q postEffect skill=5")
                || !traceContains(s, "buffId=1")) {
            throw new IllegalStateException(checkpoint + " expected skill5 to apply buff1 without damage"
                    + " postVisible=" + s.battleP7PostEffectVisible
                    + " playerSide=" + s.battleP7PostEffectPlayerSide
                    + " hasBuff1=" + runtime.debugPlayerHasBuffForSmoke(1)
                    + " value=" + buffValue
                    + " damageBonus=" + damageBonusPercent
                    + " duration=" + duration
                    + " defense=" + baseDefense + "->" + currentDefense
                    + " hp=" + s.battlePlayerHp + "/" + s.battleEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 72));
        }
        assertPhase10AStatusSlots(s, true, "skill5 buff1 after apply",
                new int[]{13}, new int[]{137});
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("after_apply_icon")));
        return new Skill5ProducerResult(beforePlayerHp, s.battlePlayerMaxHp,
                beforeEnemyHp, s.battleEnemyMaxHp, beforePp,
                s.battlePlayerHp, s.battleEnemyHp, runtime.debugPlayerSkillPpForSmoke(0),
                baseDefense, currentDefense, buffValue, damageBonusPercent, duration,
                tailTrace(s, 48));
    }

    private static Skill5DamageProbeResult runSkill5Buff1DamageProbe(
            String checkpoint, java.io.File dir, String label, boolean applyBuff1,
            boolean expireBeforeUse, int critRoll, int hitRoll, boolean forceMissSetup) throws Exception {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        int[] probe = VqsvSmokeHarness.statusBuff1Skill10Probe(s, applyBuff1, expireBeforeUse,
                critRoll, hitRoll, forceMissSetup);
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName(label + "_damage_frame")));
        return new Skill5DamageProbeResult(
                probe[0], s.battleEnemyHp, s.battleEnemyMaxHp,
                probe[3], probe[4], probe[5],
                s.battleP7DamageCritical,
                VqsvText.Battle.DODGE.equals(s.battleP7MissText),
                s.battleP7DamageText,
                s.battleP7MissText,
                tailTrace(s, 42));
    }

    private static Skill5ExpiryResult runSkill5Buff1ExpiryTimeline(
            String checkpoint, java.io.File dir) throws Exception {
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = VqsvSmokeHarness.setupPhase10AStatusBattle(s);
        runtime.debugSetPlayerDefenseForSmoke(s, 100);
        runtime.debugPlayerSourceBuffForSmoke(s, 1, 0, 5);
        int baseDefense = runtime.debugPlayerBaseStatForSmoke(BattleUnit.STAT_DEFENSE);
        int beforeDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (!runtime.debugPlayerHasBuffForSmoke(1)
                || beforeDefense != 50
                || runtime.debugPlayerBuffDurationForSmoke(1) != 3) {
            throw new IllegalStateException(checkpoint + " expected skill5 expiry setup"
                    + " baseDefense=" + baseDefense
                    + " beforeDefense=" + beforeDefense
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(1)
                    + " trace=" + tailTrace(s, 36));
        }
        assertPhase10AStatusSlots(s, true, "skill5 expiry before tick",
                new int[]{13}, new int[]{137});
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("expiry_before_tick")));

        runtime.debugTickPlayerSourceBuffForSmoke(s, 1);
        int firstDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (!runtime.debugPlayerHasBuffForSmoke(1)
                || runtime.debugPlayerBuffDurationForSmoke(1) != 2
                || firstDefense != 50) {
            throw new IllegalStateException(checkpoint + " expected skill5 first expiry tick"
                    + " defense=" + firstDefense
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(1)
                    + " trace=" + tailTrace(s, 36));
        }
        assertPhase10AStatusSlots(s, true, "skill5 expiry after first tick",
                new int[]{13}, new int[]{136});
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("expiry_after_first_tick")));

        runtime.debugTickPlayerSourceBuffForSmoke(s, 1);
        int secondDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (!runtime.debugPlayerHasBuffForSmoke(1)
                || runtime.debugPlayerBuffDurationForSmoke(1) != 1
                || secondDefense != 50) {
            throw new IllegalStateException(checkpoint + " expected skill5 second expiry tick"
                    + " defense=" + secondDefense
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(1)
                    + " trace=" + tailTrace(s, 36));
        }
        assertPhase10AStatusSlots(s, true, "skill5 expiry after second tick",
                new int[]{13}, new int[]{135});
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("expiry_after_second_tick")));

        runtime.debugTickPlayerSourceBuffForSmoke(s, 1);
        int afterDefense = runtime.debugPlayerCurrentStatForSmoke(BattleUnit.STAT_DEFENSE);
        if (runtime.debugPlayerHasBuffForSmoke(1)
                || runtime.debugPlayerBuffDurationForSmoke(1) != 0
                || afterDefense != baseDefense
                || s.battlePlayerStatusCount != 0) {
            throw new IllegalStateException(checkpoint + " expected skill5 buff1 expiry clear"
                    + " active=" + runtime.debugPlayerHasBuffForSmoke(1)
                    + " duration=" + runtime.debugPlayerBuffDurationForSmoke(1)
                    + " defense=" + afterDefense
                    + " statusCount=" + s.battlePlayerStatusCount
                    + " trace=" + tailTrace(s, 48));
        }
        writeScenePng(s, new java.io.File(dir, skill5ViemLoiPhaPngName("expiry_after_clear")));
        return new Skill5ExpiryResult(baseDefense, beforeDefense, firstDefense, secondDefense,
                afterDefense, s.battlePlayerStatusCount, tailTrace(s, 42));
    }

    private static String skill5ViemLoiPhaPngName(String suffix) {
        return "battle_skill5_viem_loi_pha_timeline_" + suffix + ".png";
    }

    private static void assertSkill5ViemLoiPhaSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(5);
        BattleBuffRow buff = VqsvBattleTables.instance().buff(1);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(5);
        byte[] expectedEffect = new byte[]{0, 0, 31, 0, 0, -1, 0, 0, 1, 16, 0, -1, -1, 0, 0, 1, 15, 0, -1, -1, 0};
        short[] speffect16 = VqsvBattleAnimationTables.instance().speffectRow(16);
        short[] speffect15 = VqsvBattleAnimationTables.instance().speffectRow(15);
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 122
                || row.descriptionTextId != 534
                || row.powerPercent != 0
                || row.ppMax != 10
                || row.effectMode != 1
                || row.effectId != 1
                || row.chanceOrParam != -1
                || row.targetSide != 1
                || buff == null
                || buff.duration != 3
                || buff.paramA != 50
                || buff.paramB != 50
                || speffect16.length == 0
                || speffect16[0] != 9
                || speffect15.length == 0
                || speffect15[0] != 1
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill5 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " buff=" + (buff == null ? "null" : java.util.Arrays.toString(buff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect16=" + java.util.Arrays.toString(speffect16)
                    + " speffect15=" + java.util.Arrays.toString(speffect15)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill5 Viem loi pha source rows"
                + " aq.c[1][5]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[6][1]=" + java.util.Arrays.toString(buff.raw)
                + " effect.mid[5]=" + java.util.Arrays.toString(effect)
                + " speffect16=" + java.util.Arrays.toString(speffect16)
                + " speffect15=" + java.util.Arrays.toString(speffect15)
                + " name=" + row.name("skill5")
                + " buffName=" + buff.name("buff1"));
    }

    private static final class Skill5ProducerResult {
        final int beforePlayerHp;
        final int playerMaxHp;
        final int beforeEnemyHp;
        final int enemyMaxHp;
        final int beforePp;
        final int afterPlayerHp;
        final int afterEnemyHp;
        final int afterPp;
        final int baseDefense;
        final int currentDefense;
        final int buffValue;
        final int damageBonusPercent;
        final int duration;
        final String traceTail;

        Skill5ProducerResult(int beforePlayerHp, int playerMaxHp, int beforeEnemyHp, int enemyMaxHp,
                             int beforePp, int afterPlayerHp, int afterEnemyHp, int afterPp,
                             int baseDefense, int currentDefense, int buffValue, int damageBonusPercent,
                             int duration, String traceTail) {
            this.beforePlayerHp = beforePlayerHp;
            this.playerMaxHp = playerMaxHp;
            this.beforeEnemyHp = beforeEnemyHp;
            this.enemyMaxHp = enemyMaxHp;
            this.beforePp = beforePp;
            this.afterPlayerHp = afterPlayerHp;
            this.afterEnemyHp = afterEnemyHp;
            this.afterPp = afterPp;
            this.baseDefense = baseDefense;
            this.currentDefense = currentDefense;
            this.buffValue = buffValue;
            this.damageBonusPercent = damageBonusPercent;
            this.duration = duration;
            this.traceTail = traceTail;
        }
    }

    private static final class Skill5DamageProbeResult {
        final int damage;
        final int enemyHpAfter;
        final int enemyMaxHp;
        final int baseDefense;
        final int currentDefense;
        final int duration;
        final boolean critical;
        final boolean miss;
        final String damageText;
        final String missText;
        final String traceTail;

        Skill5DamageProbeResult(int damage, int enemyHpAfter, int enemyMaxHp,
                                int baseDefense, int currentDefense, int duration,
                                boolean critical, boolean miss,
                                String damageText, String missText, String traceTail) {
            this.damage = damage;
            this.enemyHpAfter = enemyHpAfter;
            this.enemyMaxHp = enemyMaxHp;
            this.baseDefense = baseDefense;
            this.currentDefense = currentDefense;
            this.duration = duration;
            this.critical = critical;
            this.miss = miss;
            this.damageText = damageText;
            this.missText = missText;
            this.traceTail = traceTail;
        }
    }

    private static final class Skill5ExpiryResult {
        final int baseDefense;
        final int beforeDefense;
        final int firstDefense;
        final int secondDefense;
        final int afterDefense;
        final int afterStatusCount;
        final String traceTail;

        Skill5ExpiryResult(int baseDefense, int beforeDefense, int firstDefense,
                           int secondDefense, int afterDefense, int afterStatusCount,
                           String traceTail) {
            this.baseDefense = baseDefense;
            this.beforeDefense = beforeDefense;
            this.firstDefense = firstDefense;
            this.secondDefense = secondDefense;
            this.afterDefense = afterDefense;
            this.afterStatusCount = afterStatusCount;
            this.traceTail = traceTail;
        }
    }

    private static Skill3TimelineResult runSkill3HoaVanTrieuSingleTimeline(
            String checkpoint, java.io.File dir, String label, boolean preloadDebuff0, long seed) throws Exception {
        int skillId = 3;
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
        assertSkill3HoaVanTrieuSourceRows(s, checkpoint);
        assertDirectBaseP3BeforeConfirm(s, runtime, checkpoint, skillId);
        if (preloadDebuff0) {
            runtime.debugStatusIconForSmoke(s, false, 1, 0, 3, 24, 1);
            if (!runtime.debugEnemyHasDebuffForSmoke(0)) {
                throw new IllegalStateException(checkpoint + " expected skill3 formula preload to set debuff0 flag"
                        + " trace=" + tailTrace(s, 36));
            }
            assertPhase10AStatusSlots(s, false, "skill3 preloaded debuff0 visible",
                    new int[]{1}, new int[]{137});
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        writeScenePng(s, new java.io.File(dir, skill3HoaVanTrieuPngName(label + "_before")));

        runtime.debugSetSourceRandomSeedForSmoke(seed);
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
        assertDirectBaseP7ActorVisible(s, runtime, checkpoint, skillId);
        writeScenePng(s, new java.io.File(dir, skill3HoaVanTrieuPngName(label + "_actor_u20_start")));

        tickUntilBattleP7Phase(s, 2, 180);
        int damage = latestTraceDamage(s, "battle P7 damage frame skill=3");
        assertSkill3HoaVanTrieuDamageFrame(s, runtime, checkpoint, damage, preloadDebuff0);
        String debuffText = s.battleP7DebuffText;
        writeScenePng(s, new java.io.File(dir, skill3HoaVanTrieuPngName(label + "_damage_frame")));

        int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
        int guard = 0;
        while ("P7".equals(s.battleStateName)
                && s.battleEnemyHp > expectedEnemyHp
                && guard++ < 240) {
            s.tick();
        }
        if (s.battleEnemyHp != expectedEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || !traceContains(s, "battle P7 source n() skill=3")
                || !traceContains(s, "battle P7 actor u.a() start skill=3")
                || !traceContains(s, "battle P7 damage frame skill=3")
                || traceContains(s, "appliedDebuffId=0")) {
            throw new IllegalStateException(checkpoint + " expected skill3 " + label + " HP to settle"
                    + " state=" + s.battleStateName
                    + " phase=" + s.battleP7Phase
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " expectedHp=" + expectedEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 60));
        }
        writeScenePng(s, new java.io.File(dir, skill3HoaVanTrieuPngName(label + "_hp_settled")));

        return new Skill3TimelineResult(beforePlayerHp, s.battlePlayerMaxHp,
                beforeEnemyHp, s.battleEnemyMaxHp, beforePp,
                damage, s.battleEnemyHp, runtime.debugPlayerSkillPpForSmoke(0),
                debuffText, preloadDebuff0, tailTrace(s, 36));
    }

    private static Skill3TimelineResult runSkill9VinhHangHoaAnhSingleTimeline(
            String checkpoint, java.io.File dir, String label, boolean preloadDebuff0, long seed) throws Exception {
        int skillId = 9;
        VqsvIntroDemo.Scene s = new VqsvIntroDemo.Scene();
        SourceBattleRuntime runtime = enterElderP3DirectBaseBeforeConfirm(s, skillId);
        assertSkill9VinhHangHoaAnhSourceRows(s, checkpoint);
        assertDirectBaseP3BeforeConfirm(s, runtime, checkpoint, skillId);
        if (preloadDebuff0) {
            runtime.debugStatusIconForSmoke(s, false, 1, 0, 3, 24, 1);
            if (!runtime.debugEnemyHasDebuffForSmoke(0)) {
                throw new IllegalStateException(checkpoint + " expected skill9 formula preload to set debuff0 flag"
                        + " trace=" + tailTrace(s, 36));
            }
            assertPhase10AStatusSlots(s, false, "skill9 preloaded debuff0 visible",
                    new int[]{1}, new int[]{137});
        }

        int beforePlayerHp = s.battlePlayerHp;
        int beforeEnemyHp = s.battleEnemyHp;
        int beforePp = runtime.debugPlayerSkillPpForSmoke(0);
        writeScenePng(s, new java.io.File(dir, skill9VinhHangHoaAnhPngName(label + "_before")));

        runtime.debugSetSourceRandomSeedForSmoke(seed);
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
        assertDirectBaseP7ActorVisible(s, runtime, checkpoint, skillId);
        writeScenePng(s, new java.io.File(dir, skill9VinhHangHoaAnhPngName(label + "_actor_u20_start")));

        tickUntilBattleP7Phase(s, 2, 180);
        int damage = latestTraceDamage(s, "battle P7 damage frame skill=9");
        assertSkill9VinhHangHoaAnhDamageFrame(s, runtime, checkpoint, damage);
        String debuffText = s.battleP7DebuffText;
        writeScenePng(s, new java.io.File(dir, skill9VinhHangHoaAnhPngName(label + "_damage_frame")));

        int expectedEnemyHp = Math.max(0, beforeEnemyHp - damage);
        int guard = 0;
        while ("P7".equals(s.battleStateName)
                && s.battleEnemyHp > expectedEnemyHp
                && guard++ < 240) {
            s.tick();
        }
        if (s.battleEnemyHp != expectedEnemyHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 14
                || !traceContains(s, "battle P7 source n() skill=9")
                || !traceContains(s, "battle P7 actor u.a() start skill=9")
                || !traceContains(s, "battle P7 damage frame skill=9")
                || traceContains(s, "appliedDebuffId=0")) {
            throw new IllegalStateException(checkpoint + " expected skill9 " + label + " HP to settle"
                    + " state=" + s.battleStateName
                    + " phase=" + s.battleP7Phase
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " expectedHp=" + expectedEnemyHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " trace=" + tailTrace(s, 60));
        }
        writeScenePng(s, new java.io.File(dir, skill9VinhHangHoaAnhPngName(label + "_hp_settled")));

        return new Skill3TimelineResult(beforePlayerHp, s.battlePlayerMaxHp,
                beforeEnemyHp, s.battleEnemyMaxHp, beforePp,
                damage, s.battleEnemyHp, runtime.debugPlayerSkillPpForSmoke(0),
                debuffText, preloadDebuff0, tailTrace(s, 36));
    }

    private static String skill3HoaVanTrieuPngName(String suffix) {
        return "battle_skill3_hoa_van_trieu_timeline_" + suffix + ".png";
    }

    private static String skill9VinhHangHoaAnhPngName(String suffix) {
        return "battle_skill9_vinh_hang_hoa_anh_timeline_" + suffix + ".png";
    }

    private static void assertSkill3HoaVanTrieuSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(3);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(3);
        byte[] expectedEffect = new byte[]{0, 0, 20, 4, -1, -1, 0};
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 120
                || row.descriptionTextId != 532
                || row.powerPercent != 100
                || row.ppMax != 30
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != 120
                || row.targetSide != 0
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill3 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill3 Hoa Van trieu source rows"
                + " aq.c[1][3]=" + java.util.Arrays.toString(row.raw)
                + " effect.mid[3]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill3"));
    }

    private static void assertSkill3HoaVanTrieuDamageFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                           String checkpoint, int damage, boolean preloadDebuff0) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugEnemyHasDebuffForSmoke(0) != preloadDebuff0
                || !traceContains(s, "battle P7 damage frame skill=3")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=-1")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill3 damage frame"
                    + " preloadDebuff0=" + preloadDebuff0
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " hasDebuff0=" + runtime.debugEnemyHasDebuffForSmoke(0)
                    + " trace=" + tailTrace(s, 64));
        }
    }

    private static void assertSkill9VinhHangHoaAnhSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(9);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(9);
        byte[] expectedEffect = new byte[]{0, 0, 20, 8, -1, -1, 0};
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 126
                || row.descriptionTextId != 538
                || row.powerPercent != 200
                || row.ppMax != 15
                || row.effectMode != 0
                || row.effectId != -1
                || row.chanceOrParam != 250
                || row.targetSide != 0
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill9 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill9 Vinh hang hoa anh source rows"
                + " aq.c[1][9]=" + java.util.Arrays.toString(row.raw)
                + " effect.mid[9]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill9"));
    }

    private static void assertSkill9VinhHangHoaAnhDamageFrame(VqsvIntroDemo.Scene s,
                                                               SourceBattleRuntime runtime,
                                                               String checkpoint,
                                                               int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 14
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || !traceContains(s, "battle P7 damage frame skill=9")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill9 damage frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " actorVisible=" + s.battleP7ActorEffectVisible
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " trace=" + tailTrace(s, 60));
        }
    }

    private static final class FireAnimationCell {
        final int skillId;
        final String name;
        final String group;
        final String source;
        final java.io.File image;

        FireAnimationCell(int skillId, String name, String group, String source, java.io.File image) {
            this.skillId = skillId;
            this.name = name;
            this.group = group;
            this.source = source;
            this.image = image;
        }
    }

    private static void writeFireAnimationContactSheet(FireAnimationCell[] cells, java.io.File out)
            throws java.io.IOException {
        int cellW = 500;
        int cellH = 390;
        int cols = 2;
        int rows = (cells.length + cols - 1) / cols;
        int titleH = 70;
        BufferedImage sheet = new BufferedImage(cols * cellW, titleH + rows * cellH,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new java.awt.Color(18, 22, 28));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setColor(new java.awt.Color(245, 245, 245));
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        g.drawString("Fire lane animation comparison - skills 0..9", 24, 30);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        g.setColor(new java.awt.Color(190, 200, 210));
        g.drawString("Grouped by source P7 effect path: actor-only / actor + speffect / self buff speffect", 24, 52);

        for (int i = 0; i < cells.length; i++) {
            FireAnimationCell cell = cells[i];
            int col = i % cols;
            int row = i / cols;
            int x = col * cellW;
            int y = titleH + row * cellH;
            g.setColor(new java.awt.Color(32, 38, 46));
            g.fillRect(x + 10, y + 10, cellW - 20, cellH - 20);
            g.setColor(new java.awt.Color(90, 102, 116));
            g.drawRect(x + 10, y + 10, cellW - 20, cellH - 20);
            g.setColor(new java.awt.Color(255, 255, 255));
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
            g.drawString("Skill " + cell.skillId + " - " + cell.name, x + 24, y + 34);
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
            g.setColor(groupColor(cell.group));
            g.drawString(cell.group, x + 24, y + 55);
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
            g.setColor(new java.awt.Color(210, 215, 220));
            g.drawString(cell.source, x + 24, y + 74);
            BufferedImage frame = ImageIO.read(cell.image);
            if (frame == null) {
                throw new java.io.IOException("Cannot read fire contact sheet frame " + cell.image);
            }
            g.drawImage(frame, x + 130, y + 88, W, H, null);
            g.setColor(new java.awt.Color(140, 150, 160));
            g.drawRect(x + 130, y + 88, W, H);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    private static java.awt.Color groupColor(String group) {
        if (group.contains("self buff")) {
            return new java.awt.Color(123, 220, 150);
        }
        if (group.contains("speffect")) {
            return new java.awt.Color(120, 190, 255);
        }
        return new java.awt.Color(255, 205, 110);
    }

    private static void writeFireAnimationContactSheetNotes(FireAnimationCell[] cells, java.io.File out)
            throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Fire Animation Contact Sheet\n\n");
        sb.append("Status: smoke-only visual comparison. No live client was opened.\n\n");
        sb.append("| Skill | Name | Group | Representative source path |\n");
        sb.append("| --- | --- | --- | --- |\n");
        for (FireAnimationCell cell : cells) {
            sb.append("| ").append(cell.skillId)
                    .append(" | ").append(cell.name)
                    .append(" | ").append(cell.group)
                    .append(" | ").append(cell.source)
                    .append(" |\n");
        }
        sb.append("\n");
        sb.append("Source conclusion: skills 0/1/3/6/7/9 intentionally share actor effect u20/sprite262. ");
        sb.append("S60 skills 2/8 add producer speffect0/AH9 after a second u20 actor chunk; their debuff tick still uses speffect14/AH12. Skills 4/5 start with u30/u31 before self-buff speffect16 then speffect15.\n");
        Files.write(out.toPath(), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }


    private static final class Skill3TimelineResult {
        final int beforePlayerHp;
        final int playerMaxHp;
        final int beforeEnemyHp;
        final int enemyMaxHp;
        final int beforePp;
        final int damage;
        final int enemyHpAfter;
        final int afterPp;
        final String debuffText;
        final boolean hadDebuff0;
        final String traceTail;

        Skill3TimelineResult(int beforePlayerHp, int playerMaxHp,
                             int beforeEnemyHp, int enemyMaxHp, int beforePp,
                             int damage, int enemyHpAfter, int afterPp,
                             String debuffText, boolean hadDebuff0, String traceTail) {
            this.beforePlayerHp = beforePlayerHp;
            this.playerMaxHp = playerMaxHp;
            this.beforeEnemyHp = beforeEnemyHp;
            this.enemyMaxHp = enemyMaxHp;
            this.beforePp = beforePp;
            this.damage = damage;
            this.enemyHpAfter = enemyHpAfter;
            this.afterPp = afterPp;
            this.debuffText = debuffText;
            this.hadDebuff0 = hadDebuff0;
            this.traceTail = traceTail;
        }
    }

    private static String skill2DiemKichPngName(String suffix) {
        return "battle_skill2_diem_kich_timeline_" + suffix + ".png";
    }

    private static void assertSkill2DiemKichSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(2);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(2);
        byte[] expectedEffect = new byte[]{0, 0, 20, 2, -1, -1, 0, 0, 0, 20, 3, 1, -1, 0, 0, 1, 0, 0, 0, -1, 1};
        short[] speffect14 = VqsvBattleAnimationTables.instance().speffectRow(14);
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 119
                || row.descriptionTextId != 531
                || row.powerPercent != 100
                || row.ppMax != 45
                || row.effectMode != 2
                || row.effectId != 1
                || row.chanceOrParam != 10
                || row.targetSide != 0
                || debuff == null
                || debuff.duration != 2
                || speffect14.length == 0
                || speffect14[0] != 12
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill2 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect14=" + java.util.Arrays.toString(speffect14)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill2 Diem kich source rows"
                + " aq.c[1][2]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[2]=" + java.util.Arrays.toString(effect)
                + " speffect14=" + java.util.Arrays.toString(speffect14)
                + " name=" + row.name("skill2")
                + " debuffName=" + debuff.name("debuff1"));
    }

    private static void assertSkill2DiemKichDamageDebuffFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
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
                || runtime.debugEnemyDebuffSourceSkillForSmoke(1) != 2
                || !traceContains(s, "SMOKE battle forced damage.debuff roll=0")
                || !traceContains(s, "battle P7 damage frame skill=2")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=1")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill2 damage/debuff frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " hasDebuff1=" + runtime.debugEnemyHasDebuffForSmoke(1)
                    + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(1)
                    + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(1)
                    + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(1)
                    + " trace=" + tailTrace(s, 64));
        }
    }

    private static String skill8LietDiemPhongBaoPngName(String suffix) {
        return "battle_skill8_liet_diem_phong_bao_timeline_" + suffix + ".png";
    }

    private static void assertSkill8LietDiemPhongBaoSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(8);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(1);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(8);
        byte[] expectedEffect = new byte[]{0, 0, 20, 7, -1, -1, 0, 0, 0, 20, 3, 1, -1, 0, 0, 1, 0, 0, 0, -1, 1};
        short[] speffect14 = VqsvBattleAnimationTables.instance().speffectRow(14);
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 125
                || row.descriptionTextId != 537
                || row.powerPercent != 200
                || row.ppMax != 15
                || row.effectMode != 2
                || row.effectId != 1
                || row.chanceOrParam != 20
                || row.targetSide != 0
                || debuff == null
                || debuff.duration != 2
                || speffect14.length == 0
                || speffect14[0] != 12
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill8 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " speffect14=" + java.util.Arrays.toString(speffect14)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill8 Liet diem phong bao source rows"
                + " aq.c[1][8]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][1]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[8]=" + java.util.Arrays.toString(effect)
                + " speffect14=" + java.util.Arrays.toString(speffect14)
                + " name=" + row.name("skill8")
                + " debuffName=" + debuff.name("debuff1"));
    }

    private static void assertSkill8P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 8
                || runtime.debugPlayerSkillPpForSmoke(0) != 15
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill8"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static void assertSkill8P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                   String checkpoint) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 262
                || s.battleP7ActorEffectState != directBaseActorState(8)
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 14
                || !traceContains(s, "battle P7 source n() skill=8")
                || !traceContains(s, "id=20")
                || !traceContains(s, "param=" + directBaseActorState(8))
                || !traceContains(s, "battle P7 actor u.a() start skill=8")
                || traceContains(s, "battle P7 damage frame skill=8")) {
            throw new IllegalStateException(checkpoint + " expected skill8 actor effect"
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

    private static void assertSkill8LietDiemPhongBaoDamageDebuffFrame(VqsvIntroDemo.Scene s,
                                                                       SourceBattleRuntime runtime,
                                                                       String checkpoint,
                                                                       int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || s.battleP7DebuffText.isEmpty()
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 14
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || !runtime.debugEnemyHasDebuffForSmoke(1)
                || runtime.debugEnemyDebuffDurationForSmoke(1) != 2
                || runtime.debugEnemyDebuffValueForSmoke(1) != 0
                || runtime.debugEnemyDebuffSourceSkillForSmoke(1) != 8
                || !traceContains(s, "SMOKE battle forced damage.debuff roll=0")
                || !traceContains(s, "battle P7 damage frame skill=8")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=1")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill8 damage/debuff frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
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

    private static String skill1DuongViemPngName(String suffix) {
        return "battle_skill1_duong_viem_timeline_" + suffix + ".png";
    }

    private static void assertSkill1DuongViemSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(1);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(0);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(1);
        byte[] expectedEffect = new byte[]{0, 0, 20, 1, -1, -1, 0};
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 118
                || row.descriptionTextId != 530
                || row.powerPercent != 50
                || row.ppMax != 45
                || row.effectMode != 2
                || row.effectId != 0
                || row.chanceOrParam != 4
                || row.targetSide != 0
                || debuff == null
                || debuff.duration != 3
                || !java.util.Arrays.equals(effect, expectedEffect)) {
            throw new IllegalStateException(checkpoint + " skill1 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect));
        }
        s.sourceStateTrace.add("SMOKE verified skill1 Duong viem source rows"
                + " aq.c[1][1]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][0]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[1]=" + java.util.Arrays.toString(effect)
                + " name=" + row.name("skill1")
                + " debuffName=" + debuff.name("debuff0"));
    }

    private static void assertSkill1DuongViemDamageDebuffFrame(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                              String checkpoint, int damage) {
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !"Gieo Hạt".equals(s.battleP7DebuffText)
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 44
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugEnemyDebuffDurationForSmoke(0) != 3
                || runtime.debugEnemyDebuffSourceSkillForSmoke(0) != 1
                || runtime.debugEnemyDebuffValueForSmoke(0) <= 0
                || !traceContains(s, "battle P7 damage frame skill=1")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=0")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill1 damage/debuff frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " missText=" + s.battleP7MissText
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                    + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(0)
                    + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(0)
                    + " trace=" + tailTrace(s, 44));
        }
    }

    private static String skill7ChuocNhietChiXucPngName(String suffix) {
        return "battle_skill7_chuoc_nhiet_chi_xuc_timeline_" + suffix + ".png";
    }

    private static void assertSkill7ChuocNhietChiXucSourceRows(VqsvIntroDemo.Scene s, String checkpoint) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(7);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(0);
        byte[] effect = VqsvBattleAnimationTables.instance().effectRow(7);
        byte[] expectedEffect = new byte[]{0, 0, 20, 6, -1, -1, 0};
        short[] speffect18 = VqsvBattleAnimationTables.instance().speffectRow(18);
        if (row == null
                || row.elementFamily != 0
                || row.nameTextId != 124
                || row.descriptionTextId != 536
                || row.powerPercent != 75
                || row.ppMax != 30
                || row.effectMode != 2
                || row.effectId != 0
                || row.chanceOrParam != 3
                || row.targetSide != 0
                || debuff == null
                || debuff.duration != 3
                || !java.util.Arrays.equals(effect, expectedEffect)
                || speffect18.length == 0
                || speffect18[0] != 9) {
            throw new IllegalStateException(checkpoint + " skill7 source row mismatch"
                    + " skill=" + (row == null ? "null" : java.util.Arrays.toString(row.raw))
                    + " debuff=" + (debuff == null ? "null" : java.util.Arrays.toString(debuff.raw))
                    + " effect=" + java.util.Arrays.toString(effect)
                    + " expectedEffect=" + java.util.Arrays.toString(expectedEffect)
                    + " speffect18=" + java.util.Arrays.toString(speffect18));
        }
        s.sourceStateTrace.add("SMOKE verified skill7 Chuoc nhiet chi xuc source rows"
                + " aq.c[1][7]=" + java.util.Arrays.toString(row.raw)
                + " aq.c[7][0]=" + java.util.Arrays.toString(debuff.raw)
                + " effect.mid[7]=" + java.util.Arrays.toString(effect)
                + " speffect18=" + java.util.Arrays.toString(speffect18)
                + " name=" + row.name("skill7")
                + " debuffName=" + debuff.name("debuff0"));
    }

    private static void assertSkill7P3BeforeConfirm(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                    String checkpoint) {
        if (!"P3".equals(s.battleStateName)
                || s.battleSkillIds.length == 0
                || s.battleSkillIds[0] != 7
                || runtime.debugPlayerSkillPpForSmoke(0) != 30
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || s.battleP7ActorEffectVisible
                || s.battleP7DamageVisible) {
            throw new IllegalStateException(checkpoint + " expected P3 pre-confirm skill7"
                    + " state=" + s.battleStateName
                    + " skills=" + java.util.Arrays.toString(s.battleSkillIds)
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " actor=" + s.battleP7ActorEffectVisible
                    + " damageVisible=" + s.battleP7DamageVisible
                    + " trace=" + tailTrace(s, 24));
        }
    }

    private static void assertSkill7P7ActorVisible(VqsvIntroDemo.Scene s, SourceBattleRuntime runtime,
                                                   String checkpoint) {
        if (!s.battleP7ActorEffectVisible
                || s.battleP7ActorEffectSpriteId != 262
                || s.battleP7ActorEffectState != directBaseActorState(7)
                || s.battleP7ActorEffectOnPlayerSide
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || !traceContains(s, "battle P7 source n() skill=7")
                || !traceContains(s, "id=20")
                || !traceContains(s, "param=" + directBaseActorState(7))
                || !traceContains(s, "battle P7 actor u.a() start skill=7")
                || traceContains(s, "battle P7 damage frame skill=7")) {
            throw new IllegalStateException(checkpoint + " expected skill7 actor effect"
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

    private static void assertSkill7ChuocNhietChiXucDamageDebuffFrame(VqsvIntroDemo.Scene s,
                                                                       SourceBattleRuntime runtime,
                                                                       String checkpoint,
                                                                       int damage) {
        String expectedDebuffText = VqsvBattleTables.instance().debuff(0).name("debuff0");
        if (!s.battleP7DamageVisible
                || s.battleP7DamageText.isEmpty()
                || !expectedDebuffText.equals(s.battleP7DebuffText)
                || !s.battleP7MissText.isEmpty()
                || s.battleP7ActorEffectVisible
                || runtime.debugPlayerSkillPpForSmoke(0) != 29
                || damage <= 0
                || s.battleEnemyHp != s.battleEnemyMaxHp
                || runtime.debugEnemyDebuffDurationForSmoke(0) != 3
                || runtime.debugEnemyDebuffSourceSkillForSmoke(0) != 7
                || runtime.debugEnemyDebuffValueForSmoke(0) <= 0
                || !traceContains(s, "battle P7 damage frame skill=7")
                || !traceContains(s, "hit=true")
                || !traceContains(s, "appliedDebuffId=0")
                || !traceContains(s, "sideEffectsCommitted=true")) {
            throw new IllegalStateException(checkpoint + " expected skill7 damage/debuff frame"
                    + " visible=" + s.battleP7DamageVisible
                    + " damageText=" + s.battleP7DamageText
                    + " debuffText=" + s.battleP7DebuffText
                    + " expectedDebuffText=" + expectedDebuffText
                    + " missText=" + s.battleP7MissText
                    + " hp=" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " pp=" + runtime.debugPlayerSkillPpForSmoke(0)
                    + " damage=" + damage
                    + " debuffDuration=" + runtime.debugEnemyDebuffDurationForSmoke(0)
                    + " debuffValue=" + runtime.debugEnemyDebuffValueForSmoke(0)
                    + " sourceSkill=" + runtime.debugEnemyDebuffSourceSkillForSmoke(0)
                    + " trace=" + tailTrace(s, 64));
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
            case 0:
            case 1:
            case 2:
            case 3:
            case 6:
            case 7:
            case 8:
            case 9:
                return 20;
            default:
                throw new IllegalArgumentException("Not a fire smoke skill: " + skillId);
        }
    }

    private static int directBaseActorSpriteId(int skillId) {
        return directBaseActorEffectId(skillId) + 242;
    }

    private static int directBaseActorState(int skillId) {
        switch (skillId) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 4;
            case 6:
                return 5;
            case 7:
                return 6;
            case 8:
                return 7;
            case 9:
                return 8;
            default:
                throw new IllegalArgumentException("Not a fire smoke skill: " + skillId);
        }
    }

    private static int directBaseExpectedPower(int skillId) {
        switch (skillId) {
            case 9:
                return 200;
            case 6:
                return 150;
            default:
                return 100;
        }
    }

    private static int directBaseExpectedPp(int skillId) {
        if (skillId == 3) {
            return 30;
        }
        if (skillId == 9) {
            return 15;
        }
        return directBaseExpectedPower(skillId) == 150 ? 30 : 45;
    }

    private static int directBaseExpectedPpAfterUse(int skillId) {
        return Math.max(0, directBaseExpectedPp(skillId) - 1);
    }

    private static String directBaseAsciiName(int skillId) {
        switch (skillId) {
            case 0:
                return "Hoa trao";
            case 1:
                return "Duong viem";
            case 2:
                return "Diem kich";
            case 3:
                return "Hoa Van trieu";
            case 6:
                return "Hoa diem dao";
            case 9:
                return "Vinh hang hoa anh";
            default:
                return "Skill " + skillId;
        }
    }

    private static String directBaseAsciiDescription(int skillId) {
        switch (skillId) {
            case 0:
                return "Thuong ton thap.";
            case 1:
                return "Thuong ton thap va gay Gieo Hat.";
            case 2:
                return "Thuong ton thap, 10 phan tram gay Me Muoi.";
            case 3:
                return "Thuong ton thap; tang damage neu muc tieu co Gieo Hat.";
            case 6:
                return "Ty le thuong ton gia tang kha cao.";
            case 9:
                return "Thuong ton cao; tang damage neu muc tieu co Gieo Hat.";
            default:
                return "";
        }
    }


}
