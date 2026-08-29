package vqsv.game;

import vqsv.pet.PetState;
import vqsv.source.PetSourceAdapter;

/** Headless verification for selecting, following, hiding, and toggling a world pet. */
public final class WorldPetCompanionSmokeTest {
   private WorldPetCompanionSmokeTest() {}

   public static void main(String[] args) {
      VqsvGameRuntime.Scene scene = new VqsvGameRuntime.Scene();
      PetState pet = PetSourceAdapter.create(3, 4, 15, 3, 2, -1, -1);
      scene.session.pets.roster.add(pet);
      scene.session.world.useMap = true;
      scene.session.world.currentSceneId = 1;
      scene.session.world.currentRoomIndex = 0;
      scene.player.visible = true;
      scene.player.x = 100;
      scene.player.y = 100;

      WorldPetCompanionRuntime.toggleSelected(scene, 0);
      require(scene.session.pets.companionPetSlot == 3, "selection was not stored by pet slot");
      require(scene.petCompanion.visible, "companion is not visible");
      require(scene.petCompanion.visualSpriteIndex == pet.visualSpriteId, "wrong companion sprite");
      int idleStartCursor = scene.petCompanion.anim.cursor();
      for (int i = 0; i < 20; ++i) WorldPetCompanionRuntime.tick(scene);
      require(scene.petCompanion.anim.cursor() != idleStartCursor, "idle animation was reset every tick");
      int startX = scene.petCompanion.x;
      for (int i = 0; i < 24; ++i) {
         scene.player.x += 2;
         WorldPetCompanionRuntime.tick(scene);
      }
      require(scene.petCompanion.x > startX, "companion did not follow the player trail");
      require(scene.petCompanion.x < scene.player.x, "companion did not remain behind the player");

      scene.session.runtime.battleOverlayTicks = 1;
      WorldPetCompanionRuntime.tick(scene);
      require(!scene.petCompanion.visible, "companion remained visible during battle");
      scene.session.runtime.battleOverlayTicks = 0;
      WorldPetCompanionRuntime.toggleSelected(scene, 0);
      require(scene.session.pets.companionPetSlot == -1, "companion could not be disabled");
      System.out.println("WORLD_PET_COMPANION_SMOKE_TEST_OK");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new IllegalStateException(message);
   }
}
