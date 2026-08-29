package vqsv.game;

import vqsv.pet.PetState;
import vqsv.source.PetSourceAdapter;

public final class TanNguyetLongMaBattleStartSmokeTest {
   public static void main(String[] args) {
      VqsvGameRuntime.Scene scene = new VqsvGameRuntime.Scene();
      VqsvSceneLoaders.loadWorldRoomWithoutMapLoading(scene, 5, 3, 337, 368);
      for (int slot = 0; slot < 6; slot++) {
         PetState pet = PetSourceAdapter.create(slot, 4, 50, 5, 2, -1, -1);
         scene.session.pets.roster.add(pet);
      }
      scene.player.visible = true;
      scene.player.x = 337;
      scene.player.y = 390;
      scene.key0 = true;
      TanNguyetLongMaBossRuntime.tick(scene);
      if (scene.session.runtime.activity == null) throw new IllegalStateException("boss interaction did not create battle activity");
      for (int i = 0; i < 30; i++) {
         Blocking activity = scene.session.runtime.activity;
         if (activity == null) throw new IllegalStateException("battle activity vanished at tick " + i);
         activity.tick(scene);
      }
      if (scene.session.runtime.battleOverlayTicks <= 0 && scene.battleRenderState == null) {
         throw new IllegalStateException("battle presentation did not start");
      }
      System.out.println("TAN_NGUYET_LONG_MA_BATTLE_START_OK activity=" + scene.session.runtime.activity.getClass().getSimpleName());
   }
}
