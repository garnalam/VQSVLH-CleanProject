package vqsv.game;

import vqsv.pet.PetState;

/** Keeps one roster pet on a delayed world-position trail behind the player. */
final class WorldPetCompanionRuntime {
   private static final int TRAIL_TICKS = 10;
   private static final int TELEPORT_DISTANCE = 160;
   private static final int FOLLOW_SPEED = 3;

   private WorldPetCompanionRuntime() {}

   static void tick(VqsvGameRuntime.Scene scene) {
      PetState selected = selectedPet(scene);
      boolean worldVisible = selected != null && scene.session.world.useMap && scene.player.visible && scene.session.runtime.battleOverlayTicks <= 0;
      if (!worldVisible) {
         scene.petCompanion.visible = false;
         scene.petCompanionTrail.clear();
         return;
      }

      boolean reset = scene.petCompanionBoundSlot != selected.slot
         || scene.petCompanionScene != scene.session.world.currentSceneId
         || scene.petCompanionRoom != scene.session.world.currentRoomIndex
         || distance(scene.petCompanion.x, scene.petCompanion.y, scene.player.x, scene.player.y) > TELEPORT_DISTANCE;
      if (reset) {
         scene.petCompanionBoundSlot = selected.slot;
         scene.petCompanionScene = scene.session.world.currentSceneId;
         scene.petCompanionRoom = scene.session.world.currentRoomIndex;
         scene.petCompanion.setVisualSpriteIndex(selected.visualSpriteId);
         scene.petCompanion.x = scene.player.x - directionDx(scene.player.direction, 22);
         scene.petCompanion.y = scene.player.y - directionDy(scene.player.direction, 22);
         scene.petCompanion.direction = scene.player.direction;
         scene.petCompanionTrail.clear();
      }

      int[] last = scene.petCompanionTrail.peekLast();
      if (last == null || last[0] != scene.player.x || last[1] != scene.player.y) {
         scene.petCompanionTrail.addLast(new int[]{scene.player.x, scene.player.y, scene.player.direction});
      }
      while (scene.petCompanionTrail.size() > TRAIL_TICKS) {
         int[] target = scene.petCompanionTrail.removeFirst();
         moveToward(scene.petCompanion, target[0], target[1], target[2]);
      }

      boolean moving = last != null && (last[0] != scene.player.x || last[1] != scene.player.y);
      int animationState = moving ? 1 : 0;
      if (scene.petCompanion.anim.state() != animationState) {
         scene.petCompanion.setState(animationState);
      }
      scene.petCompanion.visible = true;
      scene.petCompanion.tick();
   }

   static void toggleSelected(VqsvGameRuntime.Scene scene, int rosterIndex) {
      if (rosterIndex < 0 || rosterIndex >= scene.session.pets.roster.size()) return;
      PetState pet = scene.session.pets.roster.get(rosterIndex);
      scene.session.pets.companionPetSlot = scene.session.pets.companionPetSlot == pet.slot ? -1 : pet.slot;
      scene.petCompanionBoundSlot = Integer.MIN_VALUE;
      scene.petCompanionTrail.clear();
      tick(scene);
   }

   static boolean selected(VqsvGameRuntime.Scene scene, int rosterIndex) {
      return rosterIndex >= 0 && rosterIndex < scene.session.pets.roster.size()
         && scene.session.pets.roster.get(rosterIndex).slot == scene.session.pets.companionPetSlot;
   }

   private static PetState selectedPet(VqsvGameRuntime.Scene scene) {
      for (PetState pet : scene.session.pets.roster) {
         if (pet.slot == scene.session.pets.companionPetSlot) return pet;
      }
      return null;
   }

   private static void moveToward(Actor actor, int targetX, int targetY, int fallbackDirection) {
      int dx = targetX - actor.x;
      int dy = targetY - actor.y;
      if (Math.abs(dx) >= Math.abs(dy) && dx != 0) actor.direction = dx > 0 ? 1 : 3;
      else if (dy != 0) actor.direction = dy > 0 ? 0 : 2;
      else actor.direction = fallbackDirection;
      actor.x += Integer.signum(dx) * Math.min(FOLLOW_SPEED, Math.abs(dx));
      actor.y += Integer.signum(dy) * Math.min(FOLLOW_SPEED, Math.abs(dy));
   }

   private static int directionDx(int direction, int amount) {
      return direction == 1 ? amount : direction == 3 ? -amount : 0;
   }

   private static int directionDy(int direction, int amount) {
      return direction == 0 ? amount : direction == 2 ? -amount : 0;
   }

   private static int distance(int x1, int y1, int x2, int y2) {
      return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
   }
}
