package vqsv.game;

import vqsv.battle.BattleRequest;
import vqsv.battle.ai.BattleAiProfile;
import vqsv.pet.PetState;
import vqsv.pet.data.TanNguyetLongMaCatalog;
import vqsv.ui.text.TextBox;

final class TanNguyetLongMaBossRuntime {
   static final int SCENE_ID = 5;
   static final int ROOM_INDEX = 3;
   static final int X = 337;
   static final int Y = 368;
   static final int REQUIRED_PARTY_SIZE = 6;
   static final int REQUIRED_PET_LEVEL = 50;
   private static final int INTERACTION_DISTANCE = 34;

   private TanNguyetLongMaBossRuntime() {}

   static void tick(VqsvGameRuntime.Scene scene) {
      Actor statue = findStatueAnchor(scene);
      boolean bossRoom = isBossRoom(scene);
      boolean worldAvailable = scene.session.world.useMap
         && scene.player.visible
         && scene.session.runtime.battleOverlayTicks <= 0
         && !scene.sourceMapLoading.active()
         && !scene.session.runtime.ui.visible;
      scene.tanNguyetLongMaBoss.visible = bossRoom && worldAvailable;
      if (!bossRoom || !worldAvailable) return;

      Actor boss = scene.tanNguyetLongMaBoss;
      positionBoss(scene, boss);
      if (boss.anim.state() != 0) boss.setState(0);
      boss.tick();
      if (statue != null) statue.visible = false;

      int dx = scene.player.x - boss.x;
      int dy = scene.player.y - boss.y;
      if (scene.key0 && dx * dx + dy * dy <= INTERACTION_DISTANCE * INTERACTION_DISTANCE
         && scene.text == null && scene.choice == null) {
         String blockReason = challengeBlockReason(scene);
         if (blockReason == null) {
            startBattle(scene);
         } else {
            scene.text = TextBox.msgWarm(blockReason, "Nhấn nút 5 để tiếp tục");
            scene.session.story.trace().add("CUSTOM-BOSS blocked Tàn Nguyệt Long Ma reason=" + blockReason);
         }
         scene.key0 = false;
         scene.keyUp = scene.keyDown = scene.keyLeft = scene.keyRight = false;
      }
   }

   static String challengeBlockReason(VqsvGameRuntime.Scene scene) {
      int partySize = scene.session.pets.roster.size();
      if (partySize < REQUIRED_PARTY_SIZE) {
         return "Cần đủ 6 sủng vật trong đội hình để khiêu chiến Tàn Nguyệt Long Ma. Hiện có: "
            + partySize + "/6.";
      }
      int belowLevel = 0;
      for (int i = 0; i < REQUIRED_PARTY_SIZE; i++) {
         PetState pet = scene.session.pets.roster.get(i);
         if (pet == null || pet.level < REQUIRED_PET_LEVEL) belowLevel++;
      }
      return belowLevel == 0 ? null
         : "Cả 6 sủng vật phải đạt cấp 50 mới có thể khiêu chiến Tàn Nguyệt Long Ma. Chưa đạt: "
            + belowLevel + " pet.";
   }

   static boolean isBossRoom(VqsvGameRuntime.Scene scene) {
      return scene.session.world.currentSceneId == SCENE_ID
         && scene.session.world.currentRoomIndex == ROOM_INDEX
         || findStatueAnchor(scene) != null;
   }

   static void prepareForRender(VqsvGameRuntime.Scene scene) {
      if (!isBossRoom(scene) || !scene.session.world.useMap || !scene.player.visible
         || scene.session.runtime.battleOverlayTicks > 0) return;
      Actor statue = findStatueAnchor(scene);
      if (statue != null) statue.visible = false;
      positionBoss(scene, scene.tanNguyetLongMaBoss);
      scene.tanNguyetLongMaBoss.visible = true;
   }

   private static void positionBoss(VqsvGameRuntime.Scene scene, Actor boss) {
      Actor anchor = findStatueAnchor(scene);
      boss.x = anchor == null ? X : anchor.x;
      boss.y = anchor == null ? Y : anchor.y;
   }

   private static Actor findStatueAnchor(VqsvGameRuntime.Scene scene) {
      boolean giantGate = false;
      for (Actor actor : scene.actors) {
         if (actor != null && actor.spriteIndex == 270
            && Math.abs(actor.x - 335) <= 8 && Math.abs(actor.y - 160) <= 8) giantGate = true;
      }
      if (!giantGate && (scene.session.world.currentSceneId != SCENE_ID
         || scene.session.world.currentRoomIndex != ROOM_INDEX)) return null;
      for (Actor actor : scene.actors) {
         if (actor != null && actor.spriteIndex == 289 && actor.direction == 8
            && Math.abs(actor.x - X) <= 8 && Math.abs(actor.y - Y) <= 8) return actor;
      }
      return null;
   }

   private static void startBattle(VqsvGameRuntime.Scene scene) {
      Blocking previous = scene.session.runtime.activity;
      BattleRequest.RuntimeProfile profile = new BattleRequest.RuntimeProfile(
         -1, false, true, TanNguyetLongMaCatalog.NAME, false, new int[0], -1, -1,
         false, BattleAiProfile.defaultFor(true)
      ).withNpcVictoryReward(1200, "tan-nguyet-long-ma");
      BattleRequest request = new BattleRequest(
         -3,
         new BattleRequest.Encounter(TanNguyetLongMaCatalog.RUNTIME_ID, 50, 5, 5),
         new BattleRequest.Flags(0, 2),
         new BattleRequest.Mode(0, 2),
         new BattleRequest.BranchTargets(),
         profile
      );
      scene.session.runtime.activity = new BattleThenResumeWorldRuntime(
         new BattleEntryTransitionThenRuntime(VqsvBattleRuntimeFactory.create(request), 6, request.mode().backgroundMode),
         previous,
         scene.session.world.resumeMode,
         "tan-nguyet-long-ma"
      );
      scene.session.story.trace().add("CUSTOM-BOSS start Tàn Nguyệt Long Ma scene=5 room=3 map=64 level=50");
   }
}
