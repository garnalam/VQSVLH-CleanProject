import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

final class VqsvSmokeHarness {
    private static final int W = VqsvIntroDemo.W;
    private static final int H = VqsvIntroDemo.H;

    private VqsvSmokeHarness() {
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
                Blocking trigger = new Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32);
                if (!trigger.tick(s)) {
                    throw new IllegalStateException("op13 smoke trigger did not complete");
                }
                s.text = TextBox.taskTip(VqsvText.Scene1Room0Group0.TASK_BUNNY);
                revealCheckpointText(s, 80);
            } else if ("return_room0_transition".equals(checkpoint)) {
                s.loadScene1Room1(370, 176);
                placePlayerForActorMaskSmoke(s, 37, true, 3);
                Blocking trigger = new ActorTransitionFreeWorldTrigger(1, 1, 37, 3, 1, 0, 30);
                if (!trigger.tick(s)) {
                    throw new IllegalStateException("return room0 smoke trigger did not complete");
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
                Blocking freeWorld = new Room0PostGroup6FreeWorld();
                freeWorld.tick(s);
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
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                for (int i = 0; i < 50; i++) {
                    s.tick();
                }
            } else if ("battle_kidnapping_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                for (int i = 0; i < 80; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_capture".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                for (int i = 0; i < 140; i++) {
                    s.tick();
                }
            } else if ("battle_bunny_command_ui".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
            } else if ("battle_bunny_p3_skill_list".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 20;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P3", 80);
            } else if ("battle_bunny_capture_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                for (int i = 0; i < 190; i++) {
                    s.tick();
                }
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
            } else if ("battle_elder_p7_actor_u21_trigger_hit".equals(checkpoint)) {
                enterElderP7FromFight(s);
                tickUntilBattleP7Phase(s, 2, 120);
                if (s.battleP7BaseStateEnemySide != 2 && s.battleP7BaseStateEnemySide != 3) {
                    throw new IllegalStateException("Expected target hit/dead base state after skill10 u action, state="
                            + s.battleP7BaseStateEnemySide);
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
            } else if ("battle_elder_p7_damage_result_debuff".equals(checkpoint)) {
                enterElderP7WithSkills(s, new int[]{1, 45}, 0);
                tickUntilBattleP7Phase(s, 2, 140);
                if (!s.battleP7DamageVisible || s.battleP7DebuffText.isEmpty()) {
                    throw new IllegalStateException("Expected P7 full damage result debuff text, visible="
                            + s.battleP7DamageVisible + " text=" + s.battleP7DebuffText);
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
                if (!"P5".equals(s.battleStateName) || s.battleMenuIds.length == 0) {
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
                if (!"P9".equals(s.battleStateName)) {
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
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
            } else if ("battle_bunny_catch_p17_anim_or_result".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
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
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickBattleAutoUntilDone(s, 3000);
                if (s.battleResultIndex != -1 || s.battleBranchTarget != -1) {
                    throw new IllegalStateException("Bunny catch route mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.text = TextBox.taskTip(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER);
                revealCheckpointText(s, 90);
            } else if ("battle_catch_fail_or_warning".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(1, new BagItem(1, 1, 0, false));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 56;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P21", 80);
                for (int i = 0; i < 18 && !"P17".equals(s.battleStateName); i++) {
                    s.press0();
                    s.tick();
                }
                tickUntilBattleState(s, "P17", 80);
                for (int i = 0; i < 58 && "P17".equals(s.battleStateName); i++) {
                    s.tick();
                }
            } else if ("battle_catch_storage_bank".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedSourcePets(s, 6);
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runCatchToDone(s, 3000);
                if (s.sourcePets.size() != 6 || s.sourcePetBank.size() != 1) {
                    throw new IllegalStateException("Catch bank storage mismatch bag="
                            + s.sourcePets.size() + " bank=" + s.sourcePetBank.size());
                }
                s.text = TextBox.openBox(VqsvText.Battle.CATCH_SENT_BANK);
                revealCheckpointText(s, 120);
            } else if ("battle_catch_storage_full_release".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                seedSourcePets(s, 6);
                seedSourceBank(s, 100);
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 0}, new int[]{10, 10, 0}, 0, true);
                runCatchToDone(s, 3000);
                if (s.sourcePets.size() != 6 || s.sourcePetBank.size() != 100) {
                    throw new IllegalStateException("Catch full storage mismatch bag="
                            + s.sourcePets.size() + " bank=" + s.sourcePetBank.size());
                }
                s.text = TextBox.openBox(VqsvText.Battle.CATCH_RELEASED_FULL);
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
            } else if ("battle_elder_item_target_p16".equals(checkpoint)) {
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
            } else if ("battle_elder_pet_p5".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickUntilBattleState(s, "P20", 120);
                s.battleClickX = 137;
                s.battleClickY = 300;
                tickUntilBattleState(s, "P5", 80);
            } else if ("battle_p5_voluntary_switch_success".equals(checkpoint)) {
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
            } else if ("battle_p5_switch_transition".equals(checkpoint)) {
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
            } else if ("battle_p5_current_warning".equals(checkpoint)) {
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
            } else if ("battle_p5_dead_warning".equals(checkpoint)) {
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
                if (s.battleMenuIds.length != 1 || s.battleMenuIds[0] != 1) {
                    throw new IllegalStateException("Expected forced P5 to list only reserve alive pet, ids="
                            + java.util.Arrays.toString(s.battleMenuIds)
                            + " trace=" + tailTrace(s, 12));
                }
                press0UntilAnyBattleState(s, 100, "P1", "WARN");
                if (!"P1".equals(s.battleStateName)
                        || s.sourcePets.get(0).speciesId != 92
                        || !traceContains(s, "forced=true")) {
                    throw new IllegalStateException("Expected forced P5 replacement to species92, state="
                            + s.battleStateName + " species0=" + s.sourcePets.get(0).speciesId
                            + " trace=" + tailTrace(s, 14));
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
            } else if ("route_sophie_after_battle_branch".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                        new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
                tickCurrentUntilDone(s, 500);
                if (s.battleResultIndex != 0 || s.battleBranchTarget != 78) {
                    throw new IllegalStateException("Sophie battle branch mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.text = TextBox.dialog(s.font, VqsvText.Common.UNKNOWN_SPEAKER,
                        VqsvText.Scene1Room3BeforeTenYears.TEXT[26],
                        0);
                revealCheckpointText(s, 120);
            } else if ("route_bunny_after_battle_task".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickBattleAutoUntilDone(s, 3000);
                if (s.battleResultIndex != -1 || s.battleBranchTarget != -1) {
                    throw new IllegalStateException("Bunny battle branch mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.op23MarkEventComplete(1, 0, 1);
                s.op14CompleteEvent(1, 1, 0);
                s.text = TextBox.taskTip(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER);
                revealCheckpointText(s, 90);
            } else if ("route_elder_after_battle_reward_state".equals(checkpoint)) {
                s.eventIndex = s.events.size();
                s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
                s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                        new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
                tickBattleAutoUntilDone(s, 3000);
                if (s.battleResultIndex != 0 || s.battleBranchTarget != 10) {
                    throw new IllegalStateException("Elder battle branch mismatch result="
                            + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                }
                s.op31CurrencyReward(0, 0, 500);
                s.op17Item(0, 4, 10);
                s.op17Item(0, 11, 2);
                s.op19SpecialReward(5, 1);
                s.op23MarkEventComplete(1, 0, 4);
                s.op23MarkEventComplete(1, 0, 5);
                s.op14CompleteEvent(1, 0, 6);
                s.text = TextBox.openBox(VqsvText.Scene1Room0Group6.FREE_WORLD);
                revealCheckpointText(s, 90);
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

    static void setupLiveCheckpoint(VqsvIntroDemo.Scene s, String checkpoint) {
        s.eventIndex = s.events.size();
        if ("battle_bunny_command_ui".equals(checkpoint)) {
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.sourceBagItems.put(0, new BagItem(0, 1, 0, true));
            s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                    new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
            tickUntilBattleState(s, "P20", 120);
        } else if ("battle_elder_command_ui".equals(checkpoint)) {
            s.sourcePets.add(new SourcePetState(0, 17, 7, 3, 2, 10, 45));
            s.sourcePets.add(new SourcePetState(1, 92, 5, 3, 2, 10, 45));
            s.current = new SourceBattleRuntime(52, new int[]{68, 5, 1},
                    new int[0], new int[]{0, 2}, new int[]{10, 10, 0}, 0, true);
            tickUntilBattleState(s, "P20", 120);
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
        } else if ("battle_sophie_command_ui".equals(checkpoint)) {
            s.current = new SourceBattleRuntime(56, new int[]{5, 20, 4},
                    new int[]{1, 1}, new int[]{0, 2}, new int[]{78, 78, 0});
            tickUntilBattleState(s, "P2", 120);
        } else {
            throw new IllegalArgumentException("Unknown live checkpoint: " + checkpoint);
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

    private static void driveItemUse(VqsvIntroDemo.Scene s) {
        driveItemUseToTarget(s, 0);
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

    private static void seedSourcePets(VqsvIntroDemo.Scene s, int count) {
        for (int i = 0; i < count; i++) {
            s.sourcePets.add(new SourcePetState(i, 17 + i, 7, 3, 2, 10, 45));
        }
    }

    private static void seedSourceBank(VqsvIntroDemo.Scene s, int count) {
        for (int i = 0; i < count; i++) {
            s.sourcePetBank.add(new SourcePetState(i, 17 + i % 20, 7, 3, 2, 10, 45));
        }
    }

    private static String tailTrace(VqsvIntroDemo.Scene s, int count) {
        int start = Math.max(0, s.sourceStateTrace.size() - count);
        return s.sourceStateTrace.subList(start, s.sourceStateTrace.size()).toString();
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
