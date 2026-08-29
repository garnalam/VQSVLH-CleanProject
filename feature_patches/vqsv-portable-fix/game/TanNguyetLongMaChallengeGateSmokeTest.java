package vqsv.game;

import vqsv.pet.PetState;
import vqsv.source.PetSourceAdapter;

/** Verifies the six level-50 pet gate before the custom boss battle. */
public final class TanNguyetLongMaChallengeGateSmokeTest {
   private TanNguyetLongMaChallengeGateSmokeTest() {}

   public static void main(String[] args) {
      VqsvGameRuntime.Scene fivePets = bossScene();
      addPets(fivePets, 5, 50);
      interact(fivePets);
      require(fivePets.session.runtime.activity == null, "five pets incorrectly started battle");
      require(fivePets.text != null, "missing warning for five pets");

      VqsvGameRuntime.Scene oneBelowLevel = bossScene();
      addPets(oneBelowLevel, 5, 50);
      addPets(oneBelowLevel, 1, 49);
      interact(oneBelowLevel);
      require(oneBelowLevel.session.runtime.activity == null, "level-49 pet incorrectly passed gate");
      require(oneBelowLevel.text != null, "missing warning for level-49 pet");

      VqsvGameRuntime.Scene ready = bossScene();
      addPets(ready, 6, 50);
      interact(ready);
      require(ready.session.runtime.activity != null, "six level-50 pets did not start battle");
      require(ready.text == null, "eligible party received warning");

      System.out.println("TAN_NGUYET_LONG_MA_CHALLENGE_GATE_OK party=6 requiredLevel=50");
   }

   private static VqsvGameRuntime.Scene bossScene() {
      VqsvGameRuntime.Scene scene = new VqsvGameRuntime.Scene();
      VqsvSceneLoaders.loadWorldRoomWithoutMapLoading(scene, 5, 3, 337, 368);
      scene.player.visible = true;
      scene.player.x = 337;
      scene.player.y = 390;
      return scene;
   }

   private static void addPets(VqsvGameRuntime.Scene scene, int count, int level) {
      for (int i = 0; i < count; i++) {
         int slot = scene.session.pets.roster.size();
         PetState pet = PetSourceAdapter.create(slot, 4, level, 5, 2, -1, -1);
         scene.session.pets.roster.add(pet);
      }
   }

   private static void interact(VqsvGameRuntime.Scene scene) {
      scene.key0 = true;
      TanNguyetLongMaBossRuntime.tick(scene);
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new IllegalStateException(message);
   }
}
