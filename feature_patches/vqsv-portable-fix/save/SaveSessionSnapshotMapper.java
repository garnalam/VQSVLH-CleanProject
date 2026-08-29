package vqsv.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vqsv.battle.data.PetStatProfileVersion;
import vqsv.charm.RainbowCharmCatalog;
import vqsv.data.UnifiedItemCatalog;
import vqsv.data.UnifiedItemInventoryKind;
import vqsv.data.UnifiedItemRecord;
import vqsv.inventory.BagItemState;
import vqsv.inventory.CurrencyEngine;
import vqsv.inventory.EquipmentState;
import vqsv.inventory.MaterialStack;
import vqsv.inventory.SpecialRewardState;
import vqsv.pet.PetState;
import vqsv.pet.data.PetTraitCatalog;
import vqsv.pet.data.PetTraitProfileVersion;
import vqsv.pet.data.PetTraitRoller;
import vqsv.quest.BranchTask;
import vqsv.session.GameSession;
import vqsv.status.StatusLifecycle;

public final class SaveSessionSnapshotMapper {
   private final CurrencyEngine currencyEngine = new CurrencyEngine();

   public SaveSnapshot capture(GameSession<?, ?> var1, SaveSnapshot.Player var2, List<SaveSnapshot.Actor> var3, int var4, PetStatProfileVersion var5, PetHpSnapshotResolver var6) {
      ArrayList var7 = new ArrayList();

      for(BranchTask var9 : var1.story.branchTasks) {
         var7.add(new SaveSnapshot.BranchTask(var9.taskId, var9.status));
      }

      HashMap var15 = new HashMap();

      for(Map.Entry var10 : var1.inventory.bagItems.entrySet()) {
         BagItemState var11 = (BagItemState)var10.getValue();
         var15.put((Integer)var10.getKey(), new SaveSnapshot.BagEntry(var11.id, var11.count, var11.bagChannel, var11.keepAtZero));
      }

      ArrayList var17 = new ArrayList();

      for(EquipmentState var20 : var1.inventory.equipmentItems) {
         var17.add(new SaveSnapshot.EquipmentEntry(var20.id, var20.equipped));
      }

      ArrayList var19 = new ArrayList();

      for(MaterialStack var12 : var1.inventory.materialItems) {
         var19.add(new SaveSnapshot.MaterialEntry(var12.id, var12.count));
      }

      HashMap var22 = new HashMap();

      for(Map.Entry var13 : var1.inventory.specialRewards.entrySet()) {
         SpecialRewardState var14 = (SpecialRewardState)var13.getValue();
         var22.put((Integer)var13.getKey(), new SaveSnapshot.SpecialRewardEntry(var14.id, var14.unlocked, var14.stackCount));
      }

      ArrayList var24 = new ArrayList();

      for(GameSession.WorldActorPosition var26 : var1.world.actorPositionOverrides.values()) {
         var24.add(new SaveSnapshot.WorldActorPosition(var26.sceneId, var26.roomIndex, var26.actorId, var26.x, var26.y));
      }

      return new SaveSnapshot(21, var5, PetTraitProfileVersion.UNIFIED_PET_TRAITS_V2, "unified-status-v1", new SaveSnapshot.World(var1.world.eventIndex, var1.world.currentSceneId, var1.world.currentRoomIndex, true, var1.world.resumeMode, var1.world.cameraX, var1.world.cameraY, new ArrayList(var1.world.openedChests), new ArrayList(var1.world.sourceWorldFlags), var24), var2, var3, new SaveSnapshot.Story(var1.story.mainTaskProgress, var4, var1.story.eventState.snapshotStates(), var7), new SaveSnapshot.Inventory(var1.inventory.currency.money, var1.inventory.currency.badges, var15, var17, var19, var22), new SaveSnapshot.Fashion(var1.fashion.profileVersion(), var1.fashion.selectedStableKey(), var1.fashion.ownedStableKeys(), new SaveSnapshot.FashionEconomy(var1.fashion.economy().profileVersion(), var1.fashion.economy().poolVersion(), var1.fashion.economy().blindBagCount(), var1.fashion.economy().fragmentCount(), var1.fashion.economy().drawSeed(), var1.fashion.economy().drawIndex())), new SaveSnapshot.Progression(var1.progression.badges.achievedSnapshot(), var1.progression.badges.enhancedSnapshot(), var1.progression.collection.snapshot(), var1.progression.collection.convenienceRewardsSnapshot(), var1.progression.playTime.elapsedMillis(), var1.progression.gameCf, var1.progression.avoidMonsterTicks, var1.progression.avoidMonsterElapsed, var1.progression.battleLoseReviveArmed, var1.progression.battleLoseWorldMode, var1.progression.dailyBadgeLastClaimEpochMillis, var1.progression.dailyBadgeLastClaimPlayTimeMillis, var1.progression.nguyenMocRaceWindowStartEpochMillis, var1.progression.nguyenMocRaceAttemptCount, var1.progression.nguyenMocRacePetRewardClaimed, var1.progression.regionalCommissionWindowStartEpochMillis, var1.progression.regionalCommissionCount, var1.progression.regionalRematches.encode(), var1.progression.petBankExpansionPurchases, new SaveSnapshot.RainbowCharms(var1.progression.rainbowCharms.tiers(), var1.progression.rainbowCharms.activeId(RainbowCharmCatalog.Slot.SURVIVAL), var1.progression.rainbowCharms.activeId(RainbowCharmCatalog.Slot.TACTICAL), var1.progression.rainbowCharms.activeId(RainbowCharmCatalog.Slot.EXPLORATION), var1.progression.rainbowCharms.starterClaimed()), var1.progression.battlePass.encode(), new ArrayList(var1.progression.redeemedGiftCodes), var1.progression.evolutionNoticeArmed, var1.progression.egg.active, var1.progression.egg.activeEggItemId, var1.progression.egg.type, var1.progression.egg.progress, var1.progression.egg.drawSeed(), var1.progression.egg.drawIndex(), var1.progression.egg.knownSpecies, var1.progression.ride.blocked, var1.progression.ride.activeIndex, var1.progression.ride.playerMoveSpeed, new SaveSnapshot.RecoveryCheckpoint(var1.progression.recoveryCheckpoint.checkpointId, var1.progression.recoveryCheckpoint.sceneId, var1.progression.recoveryCheckpoint.roomIndex, var1.progression.recoveryCheckpoint.transitionActorId), new SaveSnapshot.StoryRetryCheckpoint(var1.progression.storyRetryCheckpoint.checkpointId, var1.progression.storyRetryCheckpoint.battleDescriptorKey, var1.progression.storyRetryCheckpoint.tableauKey, var1.progression.storyRetryCheckpoint.state, var1.progression.storyRetryCheckpoint.sceneId, var1.progression.storyRetryCheckpoint.roomIndex, var1.progression.storyRetryCheckpoint.groupIndex, var1.progression.storyRetryCheckpoint.retryPhase, var1.progression.storyRetryCheckpoint.playerX, var1.progression.storyRetryCheckpoint.playerY, var1.progression.storyRetryCheckpoint.playerDirection, var1.progression.storyRetryCheckpoint.companionRoster())), new SaveSnapshot.Pets(var1.pets.refreshOperations, var1.pets.companionPetSlot, capturePets(var1.pets.roster, var6), capturePets(var1.pets.bank, var6)), new SaveSnapshot.Runtime(var1.runtime.speedX2));
   }

   public SessionRestoreReport restoreSession(GameSession<?, ?> var1, SaveSnapshot var2, PetStatProfileVersion var3, PetSaveHpMigration var4, PetSaveTraitMigration var5) {
      List var6 = migratePetTraits(var2.pets.roster, var2, PetSaveTraitMigration.ContainerKind.ROSTER, var5);
      List var7 = migratePetTraits(var2.pets.bank, var2, PetSaveTraitMigration.ContainerKind.BANK, var5);
      List var8 = migratePetHp(var2.pets.roster, var6, var2, var3, var4);
      List var9 = migratePetHp(var2.pets.bank, var7, var2, var3, var4);
      this.currencyEngine.restore(var1.inventory.currency, var2.inventory.money, var2.inventory.badges);
      var1.progression.badges.restore(var2.progression.badgeAchieved(), var2.progression.badgeEnhanced());
      var1.fashion.restore(var2.fashion.profileVersion, var2.fashion.ownedStableKeys, var2.fashion.selectedStableKey);
      var1.fashion.economy().restore(var2.fashion.economy.profileVersion, var2.fashion.economy.poolVersion, var2.fashion.economy.blindBagCount, var2.fashion.economy.fragmentCount, var2.fashion.economy.drawSeed, var2.fashion.economy.drawIndex);
      var1.progression.collection.restore(var2.progression.collectionStates());
      var1.progression.collection.restoreConvenienceRewards(var2.progression.convenienceRewardsClaimed());
      var1.progression.playTime.restore(var2.progression.playTimeMillis);
      var1.progression.gameCf = var2.progression.gameCf;
      var1.pets.refreshOperations = var2.pets.refreshOperations;
      var1.pets.companionPetSlot = var2.pets.companionPetSlot;
      var1.progression.avoidMonsterTicks = var2.progression.avoidMonsterTicks;
      var1.progression.avoidMonsterElapsed = var2.progression.avoidMonsterElapsed;
      var1.progression.battleLoseReviveArmed = var2.progression.battleLoseReviveArmed;
      var1.progression.battleLoseWorldMode = var2.progression.battleLoseWorldMode;
      var1.progression.recoveryCheckpoint.restore(var2.progression.recoveryCheckpoint.checkpointId, var2.progression.recoveryCheckpoint.sceneId, var2.progression.recoveryCheckpoint.roomIndex, var2.progression.recoveryCheckpoint.transitionActorId);
      var1.progression.storyRetryCheckpoint.restore(var2.progression.storyRetryCheckpoint.checkpointId, var2.progression.storyRetryCheckpoint.battleDescriptorKey, var2.progression.storyRetryCheckpoint.tableauKey, var2.progression.storyRetryCheckpoint.state, var2.progression.storyRetryCheckpoint.sceneId, var2.progression.storyRetryCheckpoint.roomIndex, var2.progression.storyRetryCheckpoint.groupIndex, var2.progression.storyRetryCheckpoint.retryPhase, var2.progression.storyRetryCheckpoint.playerX, var2.progression.storyRetryCheckpoint.playerY, var2.progression.storyRetryCheckpoint.playerDirection, var2.progression.storyRetryCheckpoint.companionRoster());
      var1.progression.dailyBadgeLastClaimEpochMillis = var2.progression.dailyBadgeLastClaimEpochMillis;
      var1.progression.dailyBadgeLastClaimPlayTimeMillis = var2.progression.dailyBadgeLastClaimPlayTimeMillis;
      var1.progression.nguyenMocRaceWindowStartEpochMillis = var2.progression.nguyenMocRaceWindowStartEpochMillis;
      var1.progression.nguyenMocRaceAttemptCount = var2.progression.nguyenMocRaceAttemptCount;
      var1.progression.nguyenMocRacePetRewardClaimed = var2.progression.nguyenMocRacePetRewardClaimed;
      var1.progression.regionalCommissionWindowStartEpochMillis = var2.progression.regionalCommissionWindowStartEpochMillis;
      var1.progression.regionalCommissionCount = var2.progression.regionalCommissionCount;
      var1.progression.regionalRematches.decode(var2.progression.regionalRematchState);
      var1.progression.petBankExpansionPurchases = var2.progression.petBankExpansionPurchases;
      var1.progression.rainbowCharms.restore(var2.progression.rainbowCharms.tiers, var2.progression.rainbowCharms.survivalId, var2.progression.rainbowCharms.tacticalId, var2.progression.rainbowCharms.explorationId, var2.progression.rainbowCharms.starterClaimed);
      var1.progression.battlePass.decode(var2.progression.battlePassState);
      boolean var10 = var1.progression.rainbowCharms.removeLegacyAutoStarter(5037);
      var1.progression.redeemedGiftCodes.clear();
      var1.progression.redeemedGiftCodes.addAll(var2.progression.redeemedGiftCodes());
      var1.progression.evolutionNoticeArmed = var2.progression.evolutionNoticeArmed;
      var1.progression.encounterStepsRemaining = -1;
      var1.progression.egg.active = var2.progression.eggActive;
      var1.progression.egg.activeEggItemId = var2.progression.eggActiveItemId;
      var1.progression.egg.type = var2.progression.eggType;
      var1.progression.egg.progress = var2.progression.eggProgress;
      var1.progression.egg.restoreDrawState(var2.progression.eggDrawSeed, var2.progression.eggDrawIndex);
      var1.progression.egg.restoreKnownSpecies(var2.progression.eggKnownSpecies());
      var1.progression.ride.activeIndex = var2.progression.rideActiveIndex;
      var1.progression.ride.playerMoveSpeed = var2.progression.playerMoveSpeed;
      var1.runtime.speedX2 = var2.runtime.speedX2;
      var1.story.mainTaskProgress = var2.story.mainTaskProgress;
      var1.world.openedChests.clear();
      var1.world.openedChests.addAll(var2.world.openedChests);
      var1.world.sourceWorldFlags.clear();
      var1.world.sourceWorldFlags.addAll(var2.world.sourceWorldFlags);
      var1.world.actorPositionOverrides.clear();

      for(SaveSnapshot.WorldActorPosition var12 : var2.world.actorPositionOverrides) {
         var1.world.rememberActorPosition(var12.sceneId, var12.roomIndex, var12.actorId, var12.x, var12.y);
      }

      var1.story.eventState.restoreStates(var2.story.eventStates);
      var1.story.branchTasks.clear();

      for(SaveSnapshot.BranchTask var20 : var2.story.branchTasks) {
         var1.story.branchTasks.add(new BranchTask(var20.taskId, var20.status));
      }

      var1.inventory.bagItems.clear();

      for(SaveSnapshot.BagEntry var21 : var2.inventory.bagItems.values()) {
         var1.inventory.bagItems.put(var21.id, new BagItemState(var21.id, var21.count, var21.bagChannel, var21.keepAtZero));
      }

      var1.inventory.equipmentItems.clear();
      Set var19 = restoreEquipment(var2.inventory.equipmentItems, var1.inventory.equipmentItems);
      if (var10) {
         var1.inventory.equipmentItems.removeIf((var0) -> var0.id == 5037);
         var19.remove(5037);
      }

      var1.inventory.materialItems.clear();

      for(SaveSnapshot.MaterialEntry var13 : var2.inventory.materialItems) {
         var1.inventory.materialItems.add(new MaterialStack(var13.id, var13.count));
      }

      var1.inventory.specialRewards.clear();

      for(SaveSnapshot.SpecialRewardEntry var25 : var2.inventory.specialRewards.values()) {
         var1.inventory.specialRewards.put(var25.id, new SpecialRewardState(var25.id, var25.unlocked, var25.stackCount));
      }

      SpecialRewardState var24 = (SpecialRewardState)var1.inventory.specialRewards.get(0);
      SpecialRewardState var26 = (SpecialRewardState)var1.inventory.specialRewards.get(var1.progression.egg.activeEggItemId);
      if (var1.progression.egg.activeEggItemId > 0) {
         if (var26 == null || var26.stackCount <= 0 || !var1.progression.egg.active) {
            var1.progression.egg.active = false;
            var1.progression.egg.activeEggItemId = 0;
            var1.progression.egg.progress = 0;
         }
      } else if (var24 != null) {
         if (var2.schemaVersion < 17 && var24.stackCount <= 0 && (var1.progression.egg.active || var24.unlocked && var1.progression.egg.type == 0)) {
            var24.stackCount = 1;
         }

         if (var24.stackCount > 0) {
            var1.progression.egg.activateFromSpecialReward();
         } else if (var2.schemaVersion >= 17) {
            var1.progression.egg.active = false;
            var1.progression.egg.progress = 0;
         }
      } else if (var2.schemaVersion >= 17) {
         var1.progression.egg.active = false;
         var1.progression.egg.progress = 0;
      }

      HashSet var14 = new HashSet();
      restorePets(var2.pets.roster, var6, var8, var1.pets.roster, var19, var14);
      restorePets(var2.pets.bank, var7, var9, var1.pets.bank, var19, var14);
      migrateLegacyRainbowCharmSelection(var1, var2);
      synchronizeEquipmentFlags(var1.inventory.equipmentItems, var14);

      for(PetState var16 : var1.pets.roster) {
         var1.progression.collection.markCollected(var16.speciesId);
      }

      for(PetState var28 : var1.pets.bank) {
         var1.progression.collection.markCollected(var28.speciesId);
      }

      return new SessionRestoreReport(var1.story.mainTaskProgress, var1.story.branchTasks.size(), var2.petStatProfileVersion, var3, var2.petTraitProfileVersion, var5.targetProfileVersion(), changedTraitCount(var2.pets.roster, var6) + changedTraitCount(var2.pets.bank, var7), changedHpCount(var2.pets.roster, var8) + changedHpCount(var2.pets.bank, var9));
   }

   private static List<SaveSnapshot.Pet> capturePets(List<PetState> var0, PetHpSnapshotResolver var1) {
      ArrayList var2 = new ArrayList();

      for(PetState var4 : var0) {
         var4.ensureStatusStateFromLegacy();
         int var5 = PetTraitRoller.instance().requireValid(var4.physicalTraitId);
         var5 = PetTraitCatalog.instance().sanitizePhysicalTraitId(var5, var4.speciesId);
         var4.physicalTraitId = var5;
         int var6 = var1.currentHpForSave(var4);
         int var7 = var1.maxHpForSave(var4);
         if (var6 < 0 || var7 <= 0) {
            throw new IllegalStateException("Cannot save Pet HP: slot=" + var4.slot + ", species=" + var4.speciesId + ", current=" + var6 + ", max=" + var7 + ".");
         }

         var2.add(new SaveSnapshot.Pet(var4.slot, var4.speciesId, var4.level, var4.quality, var4.nature, var5, var4.refreshCount, var4.sourceSpecialUseId, var4.heldEquipmentId, var4.battleSideFlag, var6, var7, var4.experience, var4.visualSpriteId, var4.skillIds, var4.skillCooldowns, new short[16][5], var4.sourceDebuffSlots, StatusLifecycle.durableDebuffs(var4.statusState)));
      }

      return var2;
   }

   private static List<Integer> migratePetHp(List<SaveSnapshot.Pet> var0, List<Integer> var1, SaveSnapshot var2, PetStatProfileVersion var3, PetSaveHpMigration var4) {
      ArrayList var5 = new ArrayList(var0.size());

      for(int var6 = 0; var6 < var0.size(); ++var6) {
         SaveSnapshot.Pet var7 = (SaveSnapshot.Pet)var0.get(var6);
         var5.add(var4.migrateCurrentHp(var2.schemaVersion, var2.petStatProfileVersion, var3, (Integer)var1.get(var6), var7));
      }

      return var5;
   }

   private static List<Integer> migratePetTraits(List<SaveSnapshot.Pet> var0, SaveSnapshot var1, PetSaveTraitMigration.ContainerKind var2, PetSaveTraitMigration var3) {
      ArrayList var4 = new ArrayList(var0.size());

      for(int var5 = 0; var5 < var0.size(); ++var5) {
         var4.add(var3.migratePhysicalTraitId(var1.schemaVersion, var1.petTraitProfileVersion, var2, var5, (SaveSnapshot.Pet)var0.get(var5)));
      }

      return var4;
   }

   private static int changedTraitCount(List<SaveSnapshot.Pet> var0, List<Integer> var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < var0.size(); ++var3) {
         if (((SaveSnapshot.Pet)var0.get(var3)).physicalTraitId != (Integer)var1.get(var3)) {
            ++var2;
         }
      }

      return var2;
   }

   private static int changedHpCount(List<SaveSnapshot.Pet> var0, List<Integer> var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < var0.size(); ++var3) {
         if (((SaveSnapshot.Pet)var0.get(var3)).currentHp != (Integer)var1.get(var3)) {
            ++var2;
         }
      }

      return var2;
   }

   private static void restorePets(List<SaveSnapshot.Pet> var0, List<Integer> var1, List<Integer> var2, List<PetState> var3, Set<Integer> var4, Set<Integer> var5) {
      var3.clear();

      for(int var6 = 0; var6 < var0.size(); ++var6) {
         SaveSnapshot.Pet var7 = (SaveSnapshot.Pet)var0.get(var6);
         PetState var8 = new PetState();
         var8.slot = var7.slot;
         var8.speciesId = var7.speciesId;
         var8.level = var7.level;
         var8.quality = var7.quality;
         var8.nature = var7.nature;
         var8.physicalTraitId = (Integer)var1.get(var6);
         var8.refreshCount = var7.refreshCount;
         var8.sourceSpecialUseId = var7.specialUseId;
         int var9 = var7.heldEquipmentId;
         if (var9 < 0 || RainbowCharmCatalog.instance().byRuntimeId(var9) != null || !var4.contains(var9) || !var5.add(var9)) {
            var9 = -1;
         }

         var8.heldEquipmentId = var9;
         var8.battleSideFlag = var7.battleSideFlag;
         var8.currentHp = (Integer)var2.get(var6);
         var8.experience = var7.experience;
         var8.visualSpriteId = var7.visualSpriteId;
         copyInto(var7.skillIds(), var8.skillIds);
         copyInto(var7.skillCooldowns(), var8.skillCooldowns);
         StatusLifecycle.restoreDurableDebuffs(var8.statusState, var7.durableStatuses());
         if (var8.currentHp <= 0) {
            StatusLifecycle.onDeath(var8.statusState);
         }

         var8.projectStatusStateToLegacy();
         var3.add(var8);
      }

   }

   private static void migrateLegacyRainbowCharmSelection(GameSession<?, ?> var0, SaveSnapshot var1) {
      if (var1.schemaVersion < 21) {
         ArrayList<SaveSnapshot.Pet> var2 = new ArrayList<>();
         var2.addAll(var1.pets.roster);
         var2.addAll(var1.pets.bank);

         for(SaveSnapshot.Pet var4 : var2) {
            RainbowCharmCatalog.Definition var5 = RainbowCharmCatalog.instance().byRuntimeId(var4.heldEquipmentId);
            if (var5 != null && var0.progression.rainbowCharms.activeId(var5.slot) < 0 && var0.progression.rainbowCharms.owns(var5.runtimeId)) {
               var0.progression.rainbowCharms.equip(var5.runtimeId);
            }
         }

      }
   }

   private static Set<Integer> restoreEquipment(List<SaveSnapshot.EquipmentEntry> var0, List<EquipmentState> var1) {
      LinkedHashMap<Integer, Boolean> var2 = new LinkedHashMap<>();
      HashSet var3 = new HashSet();
      UnifiedItemCatalog var4 = UnifiedItemCatalog.instance();

      for(SaveSnapshot.EquipmentEntry var6 : var0) {
         if (var6 != null && var6.id >= 0) {
            UnifiedItemRecord var7 = var4.byRuntime(UnifiedItemInventoryKind.EQUIPMENT, var6.id);
            if (var7 != null) {
               boolean var8 = (Boolean)var2.getOrDefault(var6.id, false) || var6.equipped;
               var2.put(var6.id, var8);
               if (var7.mechanicsImplemented) {
                  var3.add(var6.id);
               }
            }
         }
      }

      for(Map.Entry var10 : var2.entrySet()) {
         var1.add(new EquipmentState((Integer)var10.getKey(), (Boolean)var10.getValue()));
      }

      return var3;
   }

   private static void synchronizeEquipmentFlags(List<EquipmentState> var0, Set<Integer> var1) {
      for(EquipmentState var3 : var0) {
         var3.equipped = var1.contains(var3.id);
      }

   }

   private static void copyInto(int[] var0, int[] var1) {
      for(int var2 = 0; var2 < var1.length && var2 < var0.length; ++var2) {
         var1[var2] = var0[var2];
      }

   }

   private static void copyInto(short[][] var0, short[][] var1) {
      for(int var2 = 0; var2 < var1.length && var2 < var0.length; ++var2) {
         for(int var3 = 0; var3 < var1[var2].length && var3 < var0[var2].length; ++var3) {
            var1[var2][var3] = var0[var2][var3];
         }
      }

   }

   public static final class SessionRestoreReport {
      public final int mainTaskProgress;
      public final int branchTaskCount;
      public final PetStatProfileVersion savedPetStatProfile;
      public final PetStatProfileVersion activePetStatProfile;
      public final PetTraitProfileVersion savedPetTraitProfile;
      public final PetTraitProfileVersion activePetTraitProfile;
      public final int migratedPetTraitCount;
      public final int migratedPetHpCount;

      private SessionRestoreReport(int var1, int var2, PetStatProfileVersion var3, PetStatProfileVersion var4, PetTraitProfileVersion var5, PetTraitProfileVersion var6, int var7, int var8) {
         this.mainTaskProgress = var1;
         this.branchTaskCount = var2;
         this.savedPetStatProfile = var3;
         this.activePetStatProfile = var4;
         this.savedPetTraitProfile = var5;
         this.activePetTraitProfile = var6;
         this.migratedPetTraitCount = var7;
         this.migratedPetHpCount = var8;
      }
   }

   public interface PetHpSnapshotResolver {
      int currentHpForSave(PetState var1);

      int maxHpForSave(PetState var1);
   }
}
