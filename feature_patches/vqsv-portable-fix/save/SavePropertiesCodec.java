package vqsv.save;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import vqsv.battle.data.PetStatProfileVersion;
import vqsv.charm.RainbowCharmCatalog;
import vqsv.fashion.FashionEconomyState;
import vqsv.fashion.WardrobeState;
import vqsv.pet.data.PetTraitProfileVersion;
import vqsv.pet.data.PetTraitRoller;
import vqsv.progression.RegionalRematchState;
import vqsv.status.StatusDefinition;
import vqsv.status.StatusInstance;
import vqsv.status.StatusLifecycle;
import vqsv.status.StatusState;
import vqsv.status.UnifiedStatusCatalog;
import vqsv.world.StoryRetryCheckpointState;
import vqsv.world.WorldResumeMode;

public final class SavePropertiesCodec {
   private static final int TAT_TRUNG_CAU_RECALL_VERSION = 14;
   private static final int DAILY_BADGE_PLAY_TIME_VERSION = 15;
   private static final int PET_BANK_EXPANSION_VERSION = 16;
   private static final int EGG_QUEUE_VERSION = 17;
   private static final int WIDE_SKILL_ID_VERSION = 18;
   private static final int V4_EGG_TYPE_VERSION = 19;
   private static final int RAINBOW_TEAM_CHARM_VERSION = 20;
   private static final int REGIONAL_REMATCH_DAILY_VERSION = 21;
   private static final int BATTLE_PASS_VERSION = 20;
   public static final int VERSION = 21;
   private static final int LEGACY_VERSION = 1;
   private static final int PROFILED_VERSION = 2;
   private static final int TRAIT_VERSION = 3;
   private static final int STATUS_VERSION = 4;
   private static final int UNIFIED_STATUS_VERSION = 5;
   private static final int UNIFIED_SPECIES_VERSION = 6;
   private static final int FASHION_VERSION = 7;
   private static final int FASHION_ECONOMY_VERSION = 8;
   private static final int DAILY_BADGE_REWARD_VERSION = 9;
   private static final int REPEATABLE_ECONOMY_VERSION = 10;
   private static final int CHECKPOINT_VERSION = 11;
   private static final int GIFT_CODE_VERSION = 12;
   private static final int EGG_DRAW_VERSION = 13;

   public Properties encode(SaveSnapshot var1) {
      Properties var2 = new Properties();
      var2.setProperty("version", String.valueOf(21));
      var2.setProperty("petStatProfileVersion", var1.petStatProfileVersion.runtimeToken());
      if (!var1.petTraitProfileVersion.persistable()) {
         throw new IllegalArgumentException("Cannot encode an unassigned Pet trait profile.");
      } else {
         var2.setProperty("petTraitProfileVersion", var1.petTraitProfileVersion.token());
         if (!"unified-status-v1".equals(var1.statusProfileVersion)) {
            throw new IllegalArgumentException("Cannot encode unsupported status profile " + var1.statusProfileVersion + ".");
         } else {
            var2.setProperty("statusProfileVersion", var1.statusProfileVersion);
            var2.setProperty("eventIndex", String.valueOf(var1.world.eventIndex == null ? 0 : var1.world.eventIndex));
            var2.setProperty("scene", String.valueOf(var1.world.sceneId));
            var2.setProperty("room", String.valueOf(var1.world.roomIndex));
            var2.setProperty("worldResumeMode", var1.world.resumeMode.name());
            var2.setProperty("camera", var1.world.cameraX + "," + var1.world.cameraY);
            int var10002 = value(var1.player.x, 0);
            var2.setProperty("player", var10002 + "," + value(var1.player.y, 0) + "," + value(var1.player.direction, 0) + "," + bool(var1.player.visible == null || var1.player.visible));
            var2.setProperty("sourceMoney", String.valueOf(var1.inventory.money));
            var2.setProperty("sourceBadges", String.valueOf(var1.inventory.badges));
            var2.setProperty("sourceBadgeAchieved", join(var1.progression.badgeAchieved()));
            var2.setProperty("sourceBadgeEnhanced", join(var1.progression.badgeEnhanced()));
            var2.setProperty("sourcePetCollection", join(var1.progression.collectionStates()));
            var2.setProperty("sourceConvenienceRewardsClaimed", join(var1.progression.convenienceRewardsClaimed()));
            var2.setProperty("sourcePlayTimeMillis", String.valueOf(var1.progression.playTimeMillis));
            var2.setProperty("sourceGameCF", bool(var1.progression.gameCf));
            var2.setProperty("sourcePetRefreshOps", String.valueOf(var1.pets.refreshOperations));
            var2.setProperty("sourceCompanionPetSlot", String.valueOf(var1.pets.companionPetSlot));
            var2.setProperty("sourceAvoidMonsterTicks", String.valueOf(var1.progression.avoidMonsterTicks));
            var2.setProperty("sourceAvoidMonsterElapsed", String.valueOf(var1.progression.avoidMonsterElapsed));
            var2.setProperty("sourceBattleLoseReviveArmed", bool(var1.progression.battleLoseReviveArmed));
            var2.setProperty("sourceBattleLoseWorldMode", String.valueOf(var1.progression.battleLoseWorldMode));
            var2.setProperty("recoveryCheckpointId", var1.progression.recoveryCheckpoint.checkpointId);
            var2.setProperty("recoveryCheckpointScene", String.valueOf(var1.progression.recoveryCheckpoint.sceneId));
            var2.setProperty("recoveryCheckpointRoom", String.valueOf(var1.progression.recoveryCheckpoint.roomIndex));
            var2.setProperty("recoveryCheckpointActor", String.valueOf(var1.progression.recoveryCheckpoint.transitionActorId));
            var2.setProperty("storyRetryCheckpointId", var1.progression.storyRetryCheckpoint.checkpointId);
            var2.setProperty("storyRetryBattleDescriptor", var1.progression.storyRetryCheckpoint.battleDescriptorKey);
            var2.setProperty("storyRetryTableau", var1.progression.storyRetryCheckpoint.tableauKey);
            var2.setProperty("storyRetryState", var1.progression.storyRetryCheckpoint.state.name());
            var2.setProperty("storyRetryScene", String.valueOf(var1.progression.storyRetryCheckpoint.sceneId));
            var2.setProperty("storyRetryRoom", String.valueOf(var1.progression.storyRetryCheckpoint.roomIndex));
            var2.setProperty("storyRetryGroup", String.valueOf(var1.progression.storyRetryCheckpoint.groupIndex));
            var2.setProperty("storyRetryPhase", String.valueOf(var1.progression.storyRetryCheckpoint.retryPhase));
            var2.setProperty("storyRetryPlayer", var1.progression.storyRetryCheckpoint.playerX + "," + var1.progression.storyRetryCheckpoint.playerY + "," + var1.progression.storyRetryCheckpoint.playerDirection);
            var2.setProperty("storyRetryCompanionRoster", join(var1.progression.storyRetryCheckpoint.companionRoster()));
            var2.setProperty("dailyBadgeLastClaimEpochMillis", String.valueOf(var1.progression.dailyBadgeLastClaimEpochMillis));
            var2.setProperty("dailyBadgeLastClaimPlayTimeMillis", String.valueOf(var1.progression.dailyBadgeLastClaimPlayTimeMillis));
            var2.setProperty("nguyenMocRaceWindowStartEpochMillis", String.valueOf(var1.progression.nguyenMocRaceWindowStartEpochMillis));
            var2.setProperty("nguyenMocRaceAttemptCount", String.valueOf(var1.progression.nguyenMocRaceAttemptCount));
            var2.setProperty("nguyenMocRacePetRewardClaimed", bool(var1.progression.nguyenMocRacePetRewardClaimed));
            var2.setProperty("regionalCommissionWindowStartEpochMillis", String.valueOf(var1.progression.regionalCommissionWindowStartEpochMillis));
            var2.setProperty("regionalCommissionCount", String.valueOf(var1.progression.regionalCommissionCount));
            String var3 = var1.progression.regionalRematchState;
            if (var3 == null || var3.isEmpty()) {
               var3 = (new RegionalRematchState()).encode();
            }

            var2.setProperty("regionalRematchState", var3);
            var2.setProperty("petBankExpansionPurchases", String.valueOf(var1.progression.petBankExpansionPurchases));
            writeRainbowCharms(var2, var1.progression.rainbowCharms);
            var2.setProperty("battlePassState", var1.progression.battlePassState);
            List var4 = var1.progression.redeemedGiftCodes();
            var2.setProperty("redeemedGiftCodeCount", String.valueOf(var4.size()));

            for(int var5 = 0; var5 < var4.size(); ++var5) {
               var2.setProperty("redeemedGiftCode." + var5, (String)var4.get(var5));
            }

            var2.setProperty("sourceEvolutionNoticeArmed", bool(var1.progression.evolutionNoticeArmed));
            var2.setProperty("sourceEggActive", bool(var1.progression.eggActive));
            var2.setProperty("sourceEggActiveItemId", String.valueOf(var1.progression.eggActiveItemId));
            var2.setProperty("sourceEggType", String.valueOf(var1.progression.eggType));
            var2.setProperty("sourceEggProgress", String.valueOf(var1.progression.eggProgress));
            var2.setProperty("sourceEggDrawSeed", String.valueOf(var1.progression.eggDrawSeed));
            var2.setProperty("sourceEggDrawIndex", String.valueOf(var1.progression.eggDrawIndex));
            var2.setProperty("sourceEggKnownSpecies", join(var1.progression.eggKnownSpecies()));
            var2.setProperty("sourceRideBlocked", join(var1.progression.rideBlocked()));
            var2.setProperty("sourceRideActiveIndex", String.valueOf(var1.progression.rideActiveIndex));
            var2.setProperty("sourcePlayerMoveSpeed", String.valueOf(var1.progression.playerMoveSpeed));
            var2.setProperty("runtimeSpeedX2", bool(var1.runtime.speedX2));
            var2.setProperty("sourceMainTaskProgress", String.valueOf(var1.story.mainTaskProgress));
            writeOpenedChests(var2, var1.world.openedChests);
            writeWorldFlags(var2, var1.world.sourceWorldFlags);
            writeActorPositionOverrides(var2, var1.world.actorPositionOverrides);
            writeActors(var2, var1);
            writeEventStates(var2, var1.story.eventStates);
            writeBranchTasks(var2, var1.story.branchTasks);
            writeBag(var2, var1.inventory.bagItems);
            writeEquipment(var2, var1.inventory.equipmentItems);
            writeMaterials(var2, var1.inventory.materialItems);
            writeSpecialRewards(var2, var1.inventory.specialRewards);
            writeFashion(var2, var1.fashion);
            writePets(var2, "pet", var1.pets.roster);
            writePets(var2, "bankPet", var1.pets.bank);
            return var2;
         }
      }
   }

   public SaveSnapshot decode(Properties var1) {
      int var2 = intProp(var1, "version", -1);
      if (var2 >= 1 && var2 <= 21) {
         PetStatProfileVersion var3 = var2 == 1 ? PetStatProfileVersion.SOURCE_V1 : PetStatProfileVersion.parse(var1.getProperty("petStatProfileVersion"));
         PetTraitProfileVersion var4 = var2 < 3 ? PetTraitProfileVersion.LEGACY_UNASSIGNED : PetTraitProfileVersion.parse(var1.getProperty("petTraitProfileVersion"));
         String var5 = var2 < 4 ? "lh-status-v1" : var1.getProperty("statusProfileVersion", "");
         if (var2 < 5) {
            if (!"lh-status-v1".equals(var5)) {
               throw new IllegalArgumentException("Unknown legacy status profile: " + var5);
            }
         } else if (!"unified-status-v1".equals(var5)) {
            throw new IllegalArgumentException("Unknown status profile: " + var5);
         }

         String var6 = "unified-status-v1";
         boolean var7 = var1.containsKey("worldResumeMode");
         int[] var8 = ints(var1.getProperty("camera", "0,0"));
         int[] var9 = ints(var1.getProperty("player", ""));
         SaveSnapshot.World var10 = new SaveSnapshot.World(optionalIntProp(var1, "eventIndex"), intProp(var1, "scene", -1), intProp(var1, "room", -1), var7, WorldResumeMode.fromSaveValue(var1.getProperty("worldResumeMode", "")), value(var8, 0, 0), value(var8, 1, 0), readOpenedChests(var1), readWorldFlags(var1), readActorPositionOverrides(var1));
         SaveSnapshot.Player var11 = new SaveSnapshot.Player(optionalValue(var9, 0), optionalValue(var9, 1), optionalValue(var9, 2), var9.length > 3 ? var9[3] != 0 : null);
         Map var12 = readEventStates(var1);
         List var13 = readBranchTasks(var1);
         SaveSnapshot.Story var14 = new SaveSnapshot.Story(intProp(var1, "sourceMainTaskProgress", 0), intProp(var1, "actor.count", 0), var12, var13);
         SaveSnapshot.Inventory var15 = new SaveSnapshot.Inventory(intProp(var1, "sourceMoney", 0), intProp(var1, "sourceBadges", 0), readBag(var1), readEquipment(var1), readMaterials(var1), readSpecialRewards(var1));
         SaveSnapshot.RainbowCharms var16 = readRainbowCharms(var1, var2, var15.equipmentItems);
         SaveSnapshot.Fashion var17 = readFashion(var1, var2);
         byte[] var18 = bytes(var1.getProperty("sourcePetCollection", ""));
         boolean var19 = var18.length > 54 && var18[54] > 0;
         SaveSnapshot.RecoveryCheckpoint var20 = var2 < 11 ? new SaveSnapshot.RecoveryCheckpoint("village", 1, 0, 4) : new SaveSnapshot.RecoveryCheckpoint(var1.getProperty("recoveryCheckpointId", "village"), intProp(var1, "recoveryCheckpointScene", 1), intProp(var1, "recoveryCheckpointRoom", 0), intProp(var1, "recoveryCheckpointActor", 4));
         SaveSnapshot.StoryRetryCheckpoint var21 = readStoryRetryCheckpoint(var1, var2);
         long var22 = longProp(var1, "sourcePlayTimeMillis", 0L);
         long var24 = var2 < 9 ? 0L : requiredNonNegativeLongProp(var1, "dailyBadgeLastClaimEpochMillis");
         long var26 = var2 < 15 ? (var24 > 0L ? var22 : 0L) : requiredNonNegativeLongProp(var1, "dailyBadgeLastClaimPlayTimeMillis");
         SaveSnapshot.Progression var28 = new SaveSnapshot.Progression(ints(var1.getProperty("sourceBadgeAchieved", "")), ints(var1.getProperty("sourceBadgeEnhanced", "")), var18, booleans(var1.getProperty("sourceConvenienceRewardsClaimed", "")), var22, boolProp(var1, "sourceGameCF", false), intProp(var1, "sourceAvoidMonsterTicks", 0), intProp(var1, "sourceAvoidMonsterElapsed", 0), boolProp(var1, "sourceBattleLoseReviveArmed", false), intProp(var1, "sourceBattleLoseWorldMode", 0), var24, var26, var2 < 10 ? 0L : requiredNonNegativeLongProp(var1, "nguyenMocRaceWindowStartEpochMillis"), var2 < 10 ? 0 : requiredNonNegativeIntProp(var1, "nguyenMocRaceAttemptCount"), var2 < 10 ? var19 : boolProp(var1, "nguyenMocRacePetRewardClaimed", false), var2 < 10 ? 0L : requiredNonNegativeLongProp(var1, "regionalCommissionWindowStartEpochMillis"), var2 < 10 ? 0 : requiredNonNegativeIntProp(var1, "regionalCommissionCount"), readRegionalRematchState(var1, var2), var2 < 16 ? 0 : requiredIntInRangeProp(var1, "petBankExpansionPurchases", 0, 3), var16, var2 < 20 ? "" : var1.getProperty("battlePassState", ""), var2 < 12 ? List.of() : readRedeemedGiftCodes(var1), boolProp(var1, "sourceEvolutionNoticeArmed", false), boolProp(var1, "sourceEggActive", false), var2 < 19 ? 0 : intProp(var1, "sourceEggActiveItemId", 0), intProp(var1, "sourceEggType", 0), intProp(var1, "sourceEggProgress", 0), var2 < 13 ? deriveLegacyEggSeed(var1) : requiredLongProp(var1, "sourceEggDrawSeed"), var2 < 13 ? Math.max(0L, (long)intProp(var1, "sourceEggType", 0)) : requiredNonNegativeLongProp(var1, "sourceEggDrawIndex"), ints(var1.getProperty("sourceEggKnownSpecies", "")), ints(var1.getProperty("sourceRideBlocked", "")), intProp(var1, "sourceRideActiveIndex", -1), intProp(var1, "sourcePlayerMoveSpeed", 4), var20, var21);
         SaveSnapshot.Pets var29 = new SaveSnapshot.Pets(intProp(var1, "sourcePetRefreshOps", 0), intProp(var1, "sourceCompanionPetSlot", -1), readPets(var1, "pet", var2), readPets(var1, "bankPet", var2));
         SaveSnapshot.Runtime var30 = new SaveSnapshot.Runtime(boolProp(var1, "runtimeSpeedX2", false));
         return new SaveSnapshot(var2, var3, var4, var6, var10, var11, readActors(var1), var14, var15, var17, var28, var29, var30);
      } else {
         return null;
      }
   }

   private static void writeRainbowCharms(Properties var0, SaveSnapshot.RainbowCharms var1) {
      var0.setProperty("rainbowCharm.count", String.valueOf(var1.tiers.size()));
      int var2 = 0;

      for(Integer var4 : new TreeSet<Integer>(var1.tiers.keySet())) {
         var0.setProperty("rainbowCharm." + var2, var4 + "," + String.valueOf(var1.tiers.get(var4)));
         ++var2;
      }

      var0.setProperty("rainbowCharm.active", var1.survivalId + "," + var1.tacticalId + "," + var1.explorationId);
      var0.setProperty("rainbowCharm.starterClaimed", bool(var1.starterClaimed));
   }

   private static SaveSnapshot.RainbowCharms readRainbowCharms(Properties var0, int var1, List<SaveSnapshot.EquipmentEntry> var2) {
      LinkedHashMap var3 = new LinkedHashMap();
      if (var1 < 20) {
         for(SaveSnapshot.EquipmentEntry var9 : var2) {
            if (RainbowCharmCatalog.instance().byRuntimeId(var9.id) != null) {
               var3.put(var9.id, 1);
            }
         }

         return new SaveSnapshot.RainbowCharms(var3, -1, -1, -1, false);
      } else {
         int var4 = requiredNonNegativeIntProp(var0, "rainbowCharm.count");
         if (var4 > RainbowCharmCatalog.instance().definitions().size()) {
            throw new IllegalStateException("Corrupted Rainbow charm count: " + var4);
         } else {
            for(int var5 = 0; var5 < var4; ++var5) {
               int[] var6 = ints(var0.getProperty("rainbowCharm." + var5, ""));
               if (var6.length != 2 || RainbowCharmCatalog.instance().byRuntimeId(var6[0]) == null || var6[1] < 1 || var6[1] > 5 || var3.put(var6[0], var6[1]) != null) {
                  throw new IllegalStateException("Corrupted Rainbow charm row " + var5 + ".");
               }
            }

            int[] var8 = ints(var0.getProperty("rainbowCharm.active", "-1,-1,-1"));
            if (var8.length != 3) {
               throw new IllegalStateException("Corrupted Rainbow charm active slots.");
            } else {
               return new SaveSnapshot.RainbowCharms(var3, var8[0], var8[1], var8[2], boolProp(var0, "rainbowCharm.starterClaimed", false));
            }
         }
      }
   }

   private static List<String> readRedeemedGiftCodes(Properties var0) {
      int var1 = requiredNonNegativeIntProp(var0, "redeemedGiftCodeCount");
      if (var1 > 4096) {
         throw new IllegalStateException("Corrupted gift-code count: " + var1);
      } else {
         TreeSet var2 = new TreeSet();

         for(int var3 = 0; var3 < var1; ++var3) {
            String var4 = var0.getProperty("redeemedGiftCode." + var3, "").trim();
            if (!var4.matches("[A-Z0-9_-]{4,32}") || !var2.add(var4)) {
               throw new IllegalStateException("Corrupted redeemed gift code at index " + var3 + ": " + var4);
            }
         }

         return new ArrayList(var2);
      }
   }

   private static SaveSnapshot.StoryRetryCheckpoint readStoryRetryCheckpoint(Properties var0, int var1) {
      if (var1 < 11) {
         return SaveSnapshot.StoryRetryCheckpoint.none();
      } else {
         StoryRetryCheckpointState.State var2;
         try {
            var2 = StoryRetryCheckpointState.State.valueOf(var0.getProperty("storyRetryState", "NONE"));
         } catch (IllegalArgumentException var4) {
            throw new IllegalArgumentException("Unknown story retry checkpoint state", var4);
         }

         int[] var3 = ints(var0.getProperty("storyRetryPlayer", "0,0,0"));
         return new SaveSnapshot.StoryRetryCheckpoint(var0.getProperty("storyRetryCheckpointId", ""), var0.getProperty("storyRetryBattleDescriptor", ""), var0.getProperty("storyRetryTableau", ""), var2, intProp(var0, "storyRetryScene", -1), intProp(var0, "storyRetryRoom", -1), intProp(var0, "storyRetryGroup", -1), intProp(var0, "storyRetryPhase", -1), value(var3, 0, 0), value(var3, 1, 0), value(var3, 2, 0), ints(var0.getProperty("storyRetryCompanionRoster", "")));
      }
   }

   private static void writeFashion(Properties var0, SaveSnapshot.Fashion var1) {
      WardrobeState var2 = new WardrobeState();
      var2.restore(var1.profileVersion, var1.ownedStableKeys, var1.selectedStableKey);
      var0.setProperty("fashionProfileVersion", var2.profileVersion());
      var0.setProperty("fashionSelected", var2.selectedStableKey());
      List var3 = var2.ownedStableKeys();
      var0.setProperty("fashionOwned.count", String.valueOf(var3.size()));

      for(int var4 = 0; var4 < var3.size(); ++var4) {
         var0.setProperty("fashionOwned." + var4, (String)var3.get(var4));
      }

      FashionEconomyState var5 = new FashionEconomyState();
      var5.restore(var1.economy.profileVersion, var1.economy.poolVersion, var1.economy.blindBagCount, var1.economy.fragmentCount, var1.economy.drawSeed, var1.economy.drawIndex);
      var0.setProperty("fashionEconomyProfileVersion", var5.profileVersion());
      var0.setProperty("fashionPoolVersion", var5.poolVersion());
      var0.setProperty("fashionBlindBagCount", String.valueOf(var5.blindBagCount()));
      var0.setProperty("fashionFragmentCount", String.valueOf(var5.fragmentCount()));
      var0.setProperty("fashionDrawSeed", String.valueOf(var5.drawSeed()));
      var0.setProperty("fashionDrawIndex", String.valueOf(var5.drawIndex()));
   }

   private static SaveSnapshot.Fashion readFashion(Properties var0, int var1) {
      if (var1 < 7) {
         return new SaveSnapshot.Fashion("source-fashion-v1", "FASH-BASE-NEIL", Collections.singletonList("FASH-BASE-NEIL"), readFashionEconomy(var0, var1));
      } else {
         String var2 = var0.getProperty("fashionProfileVersion", "").trim();
         String var3 = var0.getProperty("fashionSelected", "").trim();
         int var4 = intProp(var0, "fashionOwned.count", -1);
         if (var4 >= 0 && !var3.isEmpty()) {
            ArrayList var5 = new ArrayList();

            for(int var6 = 0; var6 < var4; ++var6) {
               String var7 = var0.getProperty("fashionOwned." + var6, "").trim();
               if (var7.isEmpty()) {
                  throw new IllegalArgumentException("Version-7 save is missing fashionOwned." + var6 + ".");
               }

               var5.add(var7);
            }

            WardrobeState var8 = new WardrobeState();
            var8.restore(var2, var5, var3);
            return new SaveSnapshot.Fashion(var8.profileVersion(), var8.selectedStableKey(), var8.ownedStableKeys(), readFashionEconomy(var0, var1));
         } else {
            throw new IllegalArgumentException("Version-7 save has malformed fashion state.");
         }
      }
   }

   private static SaveSnapshot.FashionEconomy readFashionEconomy(Properties var0, int var1) {
      if (var1 < 8) {
         return new SaveSnapshot.FashionEconomy("unified-fashion-economy-v1", "FASH-POOL-SOURCE-V1", 0, 0, deriveLegacyFashionSeed(var0), 0L);
      } else {
         String var2 = requiredTextProp(var0, "fashionEconomyProfileVersion");
         String var3 = requiredTextProp(var0, "fashionPoolVersion");
         int var4 = requiredIntProp(var0, "fashionBlindBagCount");
         int var5 = requiredIntProp(var0, "fashionFragmentCount");
         long var6 = requiredLongProp(var0, "fashionDrawSeed");
         long var8 = requiredLongProp(var0, "fashionDrawIndex");
         FashionEconomyState var10 = new FashionEconomyState();
         var10.restore(var2, var3, var4, var5, var6, var8);
         return new SaveSnapshot.FashionEconomy(var10.profileVersion(), var10.poolVersion(), var10.blindBagCount(), var10.fragmentCount(), var10.drawSeed(), var10.drawIndex());
      }
   }

   private static long deriveLegacyFashionSeed(Properties var0) {
      String[] var1 = new String[]{"version", "eventIndex", "scene", "room", "camera", "player", "sourceMainTaskProgress", "sourceMoney", "sourceBadges", "sourcePlayTimeMillis", "sourcePetRefreshOps", "fashionProfileVersion", "fashionSelected"};
      StringBuilder var2 = new StringBuilder("fashion-legacy-seed-v1\n");

      for(String var6 : var1) {
         var2.append(var6).append('=').append(var0.getProperty(var6, "").trim()).append('\n');
      }

      int var10 = intProp(var0, "fashionOwned.count", 0);
      TreeSet<String> var11 = new TreeSet<>();

      for(int var12 = 0; var12 < Math.max(0, var10); ++var12) {
         String var15 = var0.getProperty("fashionOwned." + var12, "").trim();
         if (!var15.isEmpty()) {
            var11.add(var15);
         }
      }

      for(String var16 : var11) {
         var2.append("fashionOwned=").append(var16).append('\n');
      }

      byte[] var14;
      try {
         var14 = MessageDigest.getInstance("SHA-256").digest(var2.toString().getBytes(StandardCharsets.UTF_8));
      } catch (NoSuchAlgorithmException var9) {
         throw new IllegalStateException("SHA-256 is unavailable.", var9);
      }

      long var17 = 0L;

      for(int var8 = 0; var8 < 8; ++var8) {
         var17 = var17 << 8 | (long)var14[var8] & 255L;
      }

      return var17;
   }

   private static long deriveLegacyEggSeed(Properties var0) {
      String[] var1 = new String[]{"version", "eventIndex", "scene", "room", "player", "sourceMainTaskProgress", "sourcePlayTimeMillis", "sourceEggActive", "sourceEggType", "sourceEggProgress", "sourceEggKnownSpecies", "fashionDrawSeed"};
      StringBuilder var2 = new StringBuilder("egg-legacy-seed-v1\n");

      for(String var6 : var1) {
         var2.append(var6).append('=').append(var0.getProperty(var6, "").trim()).append('\n');
      }

      byte[] var8;
      try {
         var8 = MessageDigest.getInstance("SHA-256").digest(var2.toString().getBytes(StandardCharsets.UTF_8));
      } catch (NoSuchAlgorithmException var7) {
         throw new IllegalStateException("SHA-256 is unavailable.", var7);
      }

      long var9 = 0L;

      for(int var10 = 0; var10 < 8; ++var10) {
         var9 = var9 << 8 | (long)var8[var10] & 255L;
      }

      return var9;
   }

   private static void writeActors(Properties var0, SaveSnapshot var1) {
      var0.setProperty("actor.count", String.valueOf(var1.story.actorCount));

      for(SaveSnapshot.Actor var3 : var1.actors) {
         String var10001 = "actor." + var3.index;
         int var10002 = value(var3.x, 0);
         var0.setProperty(var10001, var10002 + "," + value(var3.y, 0) + "," + value(var3.direction, 0) + "," + bool(var3.visible == null || var3.visible));
      }

   }

   private static void writeOpenedChests(Properties var0, List<String> var1) {
      ArrayList var2 = new ArrayList(var1);
      Collections.sort(var2);
      var0.setProperty("sourceOpenedChest.count", String.valueOf(var2.size()));

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         var0.setProperty("sourceOpenedChest." + var3, (String)var2.get(var3));
      }

   }

   private static List<String> readOpenedChests(Properties var0) {
      ArrayList var1 = new ArrayList();
      int var2 = intProp(var0, "sourceOpenedChest.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         String var4 = var0.getProperty("sourceOpenedChest." + var3, "").trim();
         if (!var4.isEmpty()) {
            var1.add(var4);
         }
      }

      return var1;
   }

   private static void writeWorldFlags(Properties var0, List<Integer> var1) {
      ArrayList var2 = new ArrayList(var1);
      Collections.sort(var2);
      var0.setProperty("sourceWorldFlag.count", String.valueOf(var2.size()));

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         var0.setProperty("sourceWorldFlag." + var3, String.valueOf(var2.get(var3)));
      }

   }

   private static List<Integer> readWorldFlags(Properties var0) {
      ArrayList var1 = new ArrayList();
      int var2 = intProp(var0, "sourceWorldFlag.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int var4 = intProp(var0, "sourceWorldFlag." + var3, -1);
         if (var4 >= 0) {
            var1.add(var4);
         }
      }

      return var1;
   }

   private static void writeActorPositionOverrides(Properties var0, List<SaveSnapshot.WorldActorPosition> var1) {
      ArrayList<SaveSnapshot.WorldActorPosition> var2 = new ArrayList<>(var1);
      var2.sort((var0x, var1x) -> {
         int order = Integer.compare(var0x.sceneId, var1x.sceneId);
         if (order == 0) {
            order = Integer.compare(var0x.roomIndex, var1x.roomIndex);
         }

         if (order == 0) {
            order = Integer.compare(var0x.actorId, var1x.actorId);
         }

         return order;
      });
      var0.setProperty("sourceActorPosition.count", String.valueOf(var2.size()));

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         SaveSnapshot.WorldActorPosition var4 = (SaveSnapshot.WorldActorPosition)var2.get(var3);
         var0.setProperty("sourceActorPosition." + var3, var4.sceneId + "," + var4.roomIndex + "," + var4.actorId + "," + var4.x + "," + var4.y);
      }

   }

   private static List<SaveSnapshot.WorldActorPosition> readActorPositionOverrides(Properties var0) {
      ArrayList var1 = new ArrayList();
      int var2 = intProp(var0, "sourceActorPosition.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] var4 = ints(var0.getProperty("sourceActorPosition." + var3, ""));
         if (var4.length >= 5 && var4[0] >= 0 && var4[1] >= 0 && var4[2] >= 0) {
            var1.add(new SaveSnapshot.WorldActorPosition(var4[0], var4[1], var4[2], var4[3], var4[4]));
         }
      }

      return var1;
   }

   private static List<SaveSnapshot.Actor> readActors(Properties var0) {
      ArrayList var1 = new ArrayList();

      for(String var3 : new TreeSet<String>(var0.stringPropertyNames())) {
         if (var3.startsWith("actor.")) {
            String var4 = var3.substring("actor.".length());
            if (!"count".equals(var4)) {
               int var5 = parseInt(var4, -1);
               if (var5 >= 0) {
                  int[] var6 = ints(var0.getProperty(var3, ""));
                  var1.add(new SaveSnapshot.Actor(var5, optionalValue(var6, 0), optionalValue(var6, 1), optionalValue(var6, 2), var6.length > 3 ? var6[3] != 0 : null));
               }
            }
         }
      }

      return var1;
   }

   private static void writeEventStates(Properties var0, Map<String, Byte> var1) {
      var0.setProperty("eventState.count", String.valueOf(var1.size()));
      int var2 = 0;

      for(String var4 : new TreeSet<String>(var1.keySet())) {
         var0.setProperty("eventState." + var2, var4 + "=" + String.valueOf(var1.get(var4)));
         ++var2;
      }

   }

   private static Map<String, Byte> readEventStates(Properties var0) {
      HashMap var1 = new HashMap();
      int var2 = intProp(var0, "eventState.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         String var4 = var0.getProperty("eventState." + var3, "");
         int var5 = var4.indexOf(61);
         if (var5 > 0) {
            var1.put(var4.substring(0, var5), (byte)parseInt(var4.substring(var5 + 1), 0));
         }
      }

      return var1;
   }

   private static void writeBranchTasks(Properties var0, List<SaveSnapshot.BranchTask> var1) {
      var0.setProperty("branchTask.count", String.valueOf(var1.size()));

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         SaveSnapshot.BranchTask var3 = (SaveSnapshot.BranchTask)var1.get(var2);
         var0.setProperty("branchTask." + var2, var3.taskId + "," + var3.status);
      }

   }

   private static List<SaveSnapshot.BranchTask> readBranchTasks(Properties var0) {
      ArrayList var1 = new ArrayList();
      int var2 = intProp(var0, "branchTask.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] var4 = ints(var0.getProperty("branchTask." + var3, ""));
         if (var4.length >= 2) {
            var1.add(new SaveSnapshot.BranchTask(var4[0], var4[1]));
         }
      }

      return var1;
   }

   private static void writeBag(Properties var0, Map<Integer, SaveSnapshot.BagEntry> var1) {
      var0.setProperty("bag.count", String.valueOf(var1.size()));
      int var2 = 0;

      for(Integer var4 : new TreeSet<Integer>(var1.keySet())) {
         SaveSnapshot.BagEntry var5 = (SaveSnapshot.BagEntry)var1.get(var4);
         String var10001 = "bag." + var2;
         int var10002 = var5.id;
         var0.setProperty(var10001, var10002 + "," + var5.count + "," + var5.bagChannel + "," + bool(var5.keepAtZero));
         ++var2;
      }

   }

   private static Map<Integer, SaveSnapshot.BagEntry> readBag(Properties var0) {
      HashMap var1 = new HashMap();
      int var2 = intProp(var0, "bag.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] var4 = ints(var0.getProperty("bag." + var3, ""));
         if (var4.length >= 4) {
            var1.put(var4[0], new SaveSnapshot.BagEntry(var4[0], var4[1], var4[2], var4[3] != 0));
         }
      }

      return var1;
   }

   private static void writeEquipment(Properties var0, List<SaveSnapshot.EquipmentEntry> var1) {
      var0.setProperty("equipment.count", String.valueOf(var1.size()));

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         SaveSnapshot.EquipmentEntry var3 = (SaveSnapshot.EquipmentEntry)var1.get(var2);
         String var10001 = "equipment." + var2;
         int var10002 = var3.id;
         var0.setProperty(var10001, var10002 + "," + bool(var3.equipped));
      }

   }

   private static List<SaveSnapshot.EquipmentEntry> readEquipment(Properties var0) {
      ArrayList var1 = new ArrayList();
      int var2 = intProp(var0, "equipment.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] var4 = ints(var0.getProperty("equipment." + var3, ""));
         if (var4.length >= 2) {
            var1.add(new SaveSnapshot.EquipmentEntry(var4[0], var4[1] != 0));
         }
      }

      return var1;
   }

   private static void writeMaterials(Properties var0, List<SaveSnapshot.MaterialEntry> var1) {
      var0.setProperty("material.count", String.valueOf(var1.size()));

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         SaveSnapshot.MaterialEntry var3 = (SaveSnapshot.MaterialEntry)var1.get(var2);
         var0.setProperty("material." + var2, var3.id + "," + var3.count);
      }

   }

   private static List<SaveSnapshot.MaterialEntry> readMaterials(Properties var0) {
      ArrayList var1 = new ArrayList();
      int var2 = intProp(var0, "material.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] var4 = ints(var0.getProperty("material." + var3, ""));
         if (var4.length >= 2) {
            var1.add(new SaveSnapshot.MaterialEntry(var4[0], var4[1]));
         }
      }

      return var1;
   }

   private static void writeSpecialRewards(Properties var0, Map<Integer, SaveSnapshot.SpecialRewardEntry> var1) {
      var0.setProperty("special.count", String.valueOf(var1.size()));
      int var2 = 0;

      for(Integer var4 : new TreeSet<Integer>(var1.keySet())) {
         SaveSnapshot.SpecialRewardEntry var5 = (SaveSnapshot.SpecialRewardEntry)var1.get(var4);
         String var10001 = "special." + var2;
         int var10002 = var5.id;
         var0.setProperty(var10001, var10002 + "," + bool(var5.unlocked) + "," + var5.stackCount);
         ++var2;
      }

   }

   private static Map<Integer, SaveSnapshot.SpecialRewardEntry> readSpecialRewards(Properties var0) {
      HashMap var1 = new HashMap();
      int var2 = intProp(var0, "special.count", 0);

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] var4 = ints(var0.getProperty("special." + var3, ""));
         if (var4.length >= 3) {
            var1.put(var4[0], new SaveSnapshot.SpecialRewardEntry(var4[0], var4[1] != 0, var4[2]));
         }
      }

      return var1;
   }

   private static void writePets(Properties var0, String var1, List<SaveSnapshot.Pet> var2) {
      var0.setProperty(var1 + ".count", String.valueOf(var2.size()));

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         SaveSnapshot.Pet var4 = (SaveSnapshot.Pet)var2.get(var3);
         var0.setProperty(var1 + "." + var3 + ".core", var4.slot + "," + var4.speciesId + "," + var4.level + "," + var4.quality + "," + var4.nature + "," + var4.refreshCount);
         var0.setProperty(var1 + "." + var3 + ".skills", join(var4.skillIds()));
         var0.setProperty(var1 + "." + var3 + ".cooldowns", join(var4.skillCooldowns()));
         var0.setProperty(var1 + "." + var3 + ".payload", join(petPayload(var4)));
         var0.setProperty(var1 + "." + var3 + ".maxHpAtSave", String.valueOf(var4.maxHpAtSave));
         PetTraitRoller.instance().requireValid(var4.physicalTraitId);
         var0.setProperty(var1 + "." + var3 + ".physicalTraitId", String.valueOf(var4.physicalTraitId));
         writeStatuses(var0, var1 + "." + var3, var4.durableStatuses());
         var0.setProperty(var1 + "." + var3 + ".specialUse", String.valueOf(var4.specialUseId));
      }

   }

   private static List<SaveSnapshot.Pet> readPets(Properties var0, String var1, int var2) {
      ArrayList<SaveSnapshot.Pet> var3 = new ArrayList<>();
      int var4 = intProp(var0, var1 + ".count", 0);

      for(int var5 = 0; var5 < var4; ++var5) {
         int[] var6 = ints(var0.getProperty(var1 + "." + var5 + ".core", ""));
         if (var6.length >= 5) {
            int var7 = var6[1];
            if (var7 < 0 || var7 > 277 || var2 < 6 && var7 >= 100) {
               throw new IllegalArgumentException("Saved Pet species ID is incompatible with schema v" + var2 + ": " + var7);
            }

            int[] var8 = ints(var0.getProperty(var1 + "." + var5 + ".payload", ""));
            int var9 = value(var8, 4, var6[3]);
            if (var9 <= 0) {
               var9 = var6[3];
            }

            int var10 = readPhysicalTraitId(var0, var1, var5, var2);
            short[][] var11 = var2 < 4 ? rows(ints(var0.getProperty(var1 + "." + var5 + ".buffSlots", "")), 16, 5) : new short[16][5];
            short[][] var12 = var2 < 4 ? rows(ints(var0.getProperty(var1 + "." + var5 + ".debuffSlots", "")), 11, 5) : new short[11][5];
            List var13 = var2 < 4 ? legacyDurableDebuffs(var12) : readStatuses(var0, var1 + "." + var5);
            var3.add(new SaveSnapshot.Pet(var6[0], var6[1], var6[2], var9, value(var8, 5, var6[4]), var10, value(var6, 5, 0), intProp(var0, var1 + "." + var5 + ".specialUse", -1), value(var8, 2, -1), value(var8, 3, -1), value(var8, 6, -1), var2 == 1 ? -1 : intProp(var0, var1 + "." + var5 + ".maxHpAtSave", -1), value(var8, 7, 0), value(var8, 8, 0), ints(var0.getProperty(var1 + "." + var5 + ".skills", "")), ints(var0.getProperty(var1 + "." + var5 + ".cooldowns", "")), var11, var12, var13));
         }
      }

      return var3;
   }

   private static void writeStatuses(Properties var0, String var1, List<StatusInstance> var2) {
      ArrayList<StatusInstance> var3 = new ArrayList<>();

      for(StatusInstance var5 : var2) {
         if (!var5.definition.durableOutsideBattle()) {
            throw new IllegalArgumentException("Current save schema cannot persist buff " + var5.definition.stableKey + ".");
         }

         StatusDefinition var6 = UnifiedStatusCatalog.instance().canonicalize(var5.definition);
         var3.add(new StatusInstance(var6, var5.remainingTurns, var5.remainingActions, var5.primaryValue, var5.secondaryValue, var5.stackCount, var5.sourceSide, var5.sourceSlot, var5.sourceSkillId, var5.flags));
      }

      var3.sort((var0x, var1x) -> {
         int order = Integer.compare(var0x.definition.sourceBank, var1x.definition.sourceBank);
         return order != 0 ? order : Integer.compare(var0x.definition.sourceNumericId, var1x.definition.sourceNumericId);
      });
      var0.setProperty(var1 + ".status.count", String.valueOf(var3.size()));

      for(int var7 = 0; var7 < var3.size(); ++var7) {
         StatusInstance var8 = (StatusInstance)var3.get(var7);
         var0.setProperty(var1 + ".status." + var7, var8.definition.stableKey + "," + var8.definition.sourceBank + "," + var8.definition.sourceNumericId + "," + var8.remainingTurns + "," + var8.remainingActions + "," + var8.primaryValue + "," + var8.secondaryValue + "," + var8.stackCount + "," + var8.sourceSide + "," + var8.sourceSlot + "," + var8.sourceSkillId + "," + var8.flags);
      }

   }

   private static List<StatusInstance> readStatuses(Properties var0, String var1) {
      int var2 = intProp(var0, var1 + ".status.count", 0);
      ArrayList var3 = new ArrayList(var2);
      HashSet var4 = new HashSet();

      for(int var5 = 0; var5 < var2; ++var5) {
         String var6 = var1 + ".status." + var5;
         String var7 = var0.getProperty(var6, "");
         String[] var8 = var7.split(",", -1);
         if (var8.length != 12) {
            throw new IllegalArgumentException("Malformed saved status row " + var6 + ".");
         }

         int var9 = parseInt(var8[1], -1);
         int var10 = parseInt(var8[2], -1);

         StatusDefinition var11;
         try {
            var11 = UnifiedStatusCatalog.instance().requirePersistedIdentity(var8[0], var9, var10);
         } catch (IllegalArgumentException var13) {
            throw new IllegalArgumentException("Saved status identity mismatch at " + var6 + ".", var13);
         }

         if (!var11.durableOutsideBattle()) {
            throw new IllegalArgumentException("Saved status identity mismatch at " + var6 + ".");
         }

         if (!var4.add(var11.semanticKey)) {
            throw new IllegalArgumentException("Duplicate saved status semantic " + var11.semanticKey + ".");
         }

         var3.add(new StatusInstance(var11, nonNegative(var8[3], var6 + " remainingTurns"), nonNegative(var8[4], var6 + " remainingActions"), parseInt(var8[5], 0), parseInt(var8[6], 0), Math.max(1, parseInt(var8[7], 1)), parseInt(var8[8], -1), parseInt(var8[9], -1), parseInt(var8[10], -1), parseInt(var8[11], 0)));
      }

      return var3;
   }

   private static List<StatusInstance> legacyDurableDebuffs(short[][] var0) {
      StatusState var1 = new StatusState();
      StatusLifecycle.restoreLegacy(var1, new short[0][0], var0, -1, -1);
      return StatusLifecycle.durableDebuffs(var1);
   }

   private static int nonNegative(String var0, String var1) {
      int var2 = parseInt(var0, -1);
      if (var2 < 0) {
         throw new IllegalArgumentException(var1 + " cannot be negative.");
      } else {
         return var2;
      }
   }

   private static int readPhysicalTraitId(Properties var0, String var1, int var2, int var3) {
      if (var3 < 3) {
         return -1;
      } else {
         String var4 = var1 + "." + var2 + ".physicalTraitId";
         if (!var0.containsKey(var4)) {
            throw new IllegalArgumentException("Version-3+ save is missing " + var4 + ".");
         } else {
            int var5 = intProp(var0, var4, -1);
            return PetTraitRoller.instance().requireCatalogRecord(var5);
         }
      }
   }

   private static int[] petPayload(SaveSnapshot.Pet var0) {
      int[] var1 = var0.skillIds();
      int[] var2 = var0.skillCooldowns();
      int var3 = 0;

      for(int var7 : var1) {
         if (var7 != -1) {
            ++var3;
         }
      }

      int[] var8 = new int[10 + var3 * 2];
      var8[0] = var0.speciesId;
      var8[1] = var0.level;
      var8[2] = var0.heldEquipmentId;
      var8[3] = var0.battleSideFlag;
      var8[4] = var0.quality;
      var8[5] = var0.nature;
      var8[6] = var0.currentHp;
      var8[7] = var0.experience;
      var8[8] = var0.visualSpriteId;
      var8[9] = var3;
      int var9 = 0;

      for(int var10 = 0; var10 < var1.length; ++var10) {
         if (var1[var10] != -1) {
            var8[10 + var9] = var1[var10];
            var8[10 + var3 + var9] = value(var2, var10, 0);
            ++var9;
         }
      }

      return var8;
   }

   private static short[][] rows(int[] var0, int var1, int var2) {
      short[][] var3 = new short[var1][var2];
      int var4 = 0;

      for(int var5 = 0; var5 < var1 && var4 < var0.length; ++var5) {
         for(int var6 = 0; var6 < var2 && var4 < var0.length; ++var6) {
            var3[var5][var6] = (short)var0[var4++];
         }
      }

      return var3;
   }

   private static String join(int[] var0) {
      StringBuilder var1 = new StringBuilder();

      for(int var2 = 0; var2 < var0.length; ++var2) {
         if (var2 > 0) {
            var1.append(',');
         }

         var1.append(var0[var2]);
      }

      return var1.toString();
   }

   private static String join(byte[] var0) {
      StringBuilder var1 = new StringBuilder();

      for(int var2 = 0; var2 < var0.length; ++var2) {
         if (var2 > 0) {
            var1.append(',');
         }

         var1.append(var0[var2]);
      }

      return var1.toString();
   }

   private static String join(boolean[] var0) {
      StringBuilder var1 = new StringBuilder();

      for(int var2 = 0; var2 < var0.length; ++var2) {
         if (var2 > 0) {
            var1.append(',');
         }

         var1.append(var0[var2] ? 1 : 0);
      }

      return var1.toString();
   }

   private static byte[] bytes(String var0) {
      int[] var1 = ints(var0);
      byte[] var2 = new byte[var1.length];

      for(int var3 = 0; var3 < var1.length; ++var3) {
         var2[var3] = (byte)var1[var3];
      }

      return var2;
   }

   private static boolean[] booleans(String var0) {
      int[] var1 = ints(var0);
      boolean[] var2 = new boolean[var1.length];

      for(int var3 = 0; var3 < var1.length; ++var3) {
         var2[var3] = var1[var3] != 0;
      }

      return var2;
   }

   private static String join(short[][] var0) {
      StringBuilder var1 = new StringBuilder();
      boolean var2 = true;

      for(short[] var6 : var0) {
         for(short var10 : var6) {
            if (!var2) {
               var1.append(',');
            }

            var1.append(var10);
            var2 = false;
         }
      }

      return var1.toString();
   }

   private static int[] ints(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String[] var1 = var0.split(",");
         ArrayList var2 = new ArrayList();

         for(String var6 : var1) {
            if (!var6.isEmpty()) {
               var2.add(parseInt(var6, 0));
            }
         }

         int[] var7 = new int[var2.size()];

         for(int var8 = 0; var8 < var2.size(); ++var8) {
            var7[var8] = (Integer)var2.get(var8);
         }

         return var7;
      } else {
         return new int[0];
      }
   }

   private static Integer optionalIntProp(Properties var0, String var1) {
      String var2 = var0.getProperty(var1);
      if (var2 == null) {
         return null;
      } else {
         try {
            return Integer.parseInt(var2.trim());
         } catch (RuntimeException var4) {
            return null;
         }
      }
   }

   private static int intProp(Properties var0, String var1, int var2) {
      return parseInt(var0.getProperty(var1), var2);
   }

   private static long longProp(Properties var0, String var1, long var2) {
      try {
         return Long.parseLong(var0.getProperty(var1, "").trim());
      } catch (RuntimeException var5) {
         return var2;
      }
   }

   private static String requiredTextProp(Properties var0, String var1) {
      String var2 = var0.getProperty(var1, "").trim();
      if (var2.isEmpty()) {
         throw new IllegalArgumentException("Missing required save property " + var1 + ".");
      } else {
         return var2;
      }
   }

   private static String readRegionalRematchState(Properties var0, int var1) {
      if (var1 < 21) {
         return "";
      } else {
         String var2 = requiredTextProp(var0, "regionalRematchState");
         (new RegionalRematchState()).decode(var2);
         return var2;
      }
   }

   private static int requiredIntProp(Properties var0, String var1) {
      String var2 = requiredTextProp(var0, var1);

      try {
         return Integer.parseInt(var2);
      } catch (NumberFormatException var4) {
         throw new IllegalArgumentException("Invalid integer save property " + var1 + ".", var4);
      }
   }

   private static long requiredLongProp(Properties var0, String var1) {
      String var2 = requiredTextProp(var0, var1);

      try {
         return Long.parseLong(var2);
      } catch (NumberFormatException var4) {
         throw new IllegalArgumentException("Invalid long save property " + var1 + ".", var4);
      }
   }

   private static long requiredNonNegativeLongProp(Properties var0, String var1) {
      long var2 = requiredLongProp(var0, var1);
      if (var2 < 0L) {
         throw new IllegalArgumentException("Save property " + var1 + " cannot be negative.");
      } else {
         return var2;
      }
   }

   private static int requiredNonNegativeIntProp(Properties var0, String var1) {
      int var2 = requiredIntProp(var0, var1);
      if (var2 < 0) {
         throw new IllegalArgumentException("Save property " + var1 + " cannot be negative.");
      } else {
         return var2;
      }
   }

   private static int requiredIntInRangeProp(Properties var0, String var1, int var2, int var3) {
      int var4 = requiredIntProp(var0, var1);
      if (var4 >= var2 && var4 <= var3) {
         return var4;
      } else {
         throw new IllegalArgumentException("Save property " + var1 + " must be between " + var2 + " and " + var3 + ".");
      }
   }

   private static boolean boolProp(Properties var0, String var1, boolean var2) {
      String var3 = var0.getProperty(var1);
      if (var3 == null) {
         return var2;
      } else {
         return "1".equals(var3) || "true".equalsIgnoreCase(var3);
      }
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0.trim());
      } catch (RuntimeException var3) {
         return var1;
      }
   }

   private static int value(int[] var0, int var1, int var2) {
      return var1 >= 0 && var1 < var0.length ? var0[var1] : var2;
   }

   private static int value(Integer var0, int var1) {
      return var0 == null ? var1 : var0;
   }

   private static Integer optionalValue(int[] var0, int var1) {
      return var1 >= 0 && var1 < var0.length ? var0[var1] : null;
   }

   private static String bool(boolean var0) {
      return var0 ? "1" : "0";
   }
}
