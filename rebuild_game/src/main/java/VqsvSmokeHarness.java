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
            s.tick();
        }
    }

    private static void tickCurrentUntilDone(VqsvIntroDemo.Scene s, int maxTicks) {
        int guard = 0;
        while (s.current != null && guard++ < maxTicks) {
            if (s.text != null && s.text.readyForKey) {
                s.press0();
            }
            s.tick();
        }
        if (s.current != null) {
            throw new IllegalStateException("Checkpoint current did not finish in " + maxTicks + " ticks");
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
                VqsvIntroDemo.Scene.setActive(s, new int[]{53, 54, 55}, new int[]{0, 0, 0});
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
                s.current = new SourceBattleRuntime(50, new int[]{34, 5, 1},
                        new int[]{0, 1}, new int[]{0, 0}, new int[]{12, 0, 0}, -1);
                tickCurrentUntilDone(s, 600);
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
                tickCurrentUntilDone(s, 800);
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
                    + " battleHp=" + s.battlePlayerHp + "/" + s.battlePlayerMaxHp
                    + ":" + s.battleEnemyHp + "/" + s.battleEnemyMaxHp
                    + " battleLog=" + s.battleLog
                    + " state101=" + s.sourceEventState(1, 0, 1)
                    + " state110=" + s.sourceEventState(1, 1, 0)
                    + " state106=" + s.sourceEventState(1, 0, 6)
                    + " money=" + s.sourceMoney
                    + " pets=" + s.sourcePets.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
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
