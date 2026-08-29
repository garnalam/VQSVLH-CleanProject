package vqsv.game;

import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.pet.data.TanNguyetLongMaCatalog;
import vqsv.render.SpriteAnimator;

public final class TanNguyetLongMaBossSmokeTest {
   public static void main(String[] args) {
      BattleSpeciesRow boss = VqsvBattleTables.instance().species(TanNguyetLongMaCatalog.RUNTIME_ID);
      require(boss != null && boss.validForBattle(), "boss battle row missing");
      require(TanNguyetLongMaCatalog.NAME.equals(boss.name("")), "boss name mismatch");
      require(boss.element == 5, "boss is not Quỷ element");
      require(boss.statHp(50, 5) == 5000, "HP mismatch");
      require(boss.statAttack(50, 5) == 600, "strength mismatch");
      require(boss.statDefense(50, 5) == 300, "defense mismatch");
      require(boss.statSpeed(50, 5) == 40, "agility mismatch");
      SpriteAnimator sprite = SpriteAnimator.load(TanNguyetLongMaCatalog.VISUAL_ID);
      require(sprite != null, "boss sprite missing");
      require(VqsvBattleRenderer.battlePetOrientation(TanNguyetLongMaCatalog.VISUAL_ID, false) == 1,
         "boss is not mirrored to face left in battle");
      sprite.setState(0);
      for (int i = 0; i < 30; i++) sprite.tick();
      sprite.setState(1);
      for (int i = 0; i < 30; i++) sprite.tick();
      VqsvGameRuntime.Scene scene = new VqsvGameRuntime.Scene();
      scene.session.world.useMap = true;
      scene.player.visible = true;
      scene.player.x = 30;
      scene.player.y = 30;
      scene.session.world.currentSceneId = 5;
      scene.session.world.currentRoomIndex = 3;
      scene.actors[0] = new Actor(0, 270, 0, 335, 160, 0, 1);
      scene.actors[0].visible = true;
      scene.actors[36] = new Actor(36, 289, 8, 337, 368, 0, 1);
      scene.actors[36].visible = true;
      TanNguyetLongMaBossRuntime.tick(scene);
      require(!scene.actors[36].visible, "statue actor was not hidden");
      require(scene.tanNguyetLongMaBoss.visible, "boss actor is hidden");
      require(scene.tanNguyetLongMaBoss.x == 337 && scene.tanNguyetLongMaBoss.y == 368, "boss is not over the statue position");
      System.out.println("PASS Tàn Nguyệt Long Ma: lv50 HP=5000 STR=600 DEF=300 AGI=40, idle/action animated");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new IllegalStateException(message);
   }
}
