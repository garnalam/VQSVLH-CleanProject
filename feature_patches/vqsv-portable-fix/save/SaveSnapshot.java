package vqsv.save;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import vqsv.battle.data.PetStatProfileVersion;
import vqsv.pet.data.PetTraitProfileVersion;
import vqsv.status.StatusInstance;
import vqsv.status.StatusLifecycle;
import vqsv.status.StatusState;
import vqsv.world.StoryRetryCheckpointState;
import vqsv.world.WorldResumeMode;

public final class SaveSnapshot {
   public final int schemaVersion;
   public final PetStatProfileVersion petStatProfileVersion;
   public final PetTraitProfileVersion petTraitProfileVersion;
   public final String statusProfileVersion;
   public final World world;
   public final Player player;
   public final List<Actor> actors;
   public final Story story;
   public final Inventory inventory;
   public final Fashion fashion;
   public final Progression progression;
   public final Pets pets;
   public final Runtime runtime;

   public SaveSnapshot(int var1, PetStatProfileVersion var2, PetTraitProfileVersion var3, String var4, World var5, Player var6, List<Actor> var7, Story var8, Inventory var9, Fashion var10, Progression var11, Pets var12, Runtime var13) {
      this.schemaVersion = var1;
      this.petStatProfileVersion = (PetStatProfileVersion)Objects.requireNonNull(var2, "petStatProfileVersion");
      this.petTraitProfileVersion = (PetTraitProfileVersion)Objects.requireNonNull(var3, "petTraitProfileVersion");
      this.statusProfileVersion = (String)Objects.requireNonNull(var4, "statusProfileVersion");
      this.world = (World)Objects.requireNonNull(var5, "world");
      this.player = (Player)Objects.requireNonNull(var6, "player");
      this.actors = immutableList(var7);
      this.story = (Story)Objects.requireNonNull(var8, "story");
      this.inventory = (Inventory)Objects.requireNonNull(var9, "inventory");
      this.fashion = (Fashion)Objects.requireNonNull(var10, "fashion");
      this.progression = (Progression)Objects.requireNonNull(var11, "progression");
      this.pets = (Pets)Objects.requireNonNull(var12, "pets");
      this.runtime = (Runtime)Objects.requireNonNull(var13, "runtime");
   }

   private static List<StatusInstance> legacyDurableDebuffs(short[][] var0) {
      StatusState var1 = new StatusState();
      StatusLifecycle.restoreLegacy(var1, new short[0][0], var0, -1, -1);
      return var1.snapshot(1);
   }

   private static List<StatusInstance> copyStatuses(List<StatusInstance> var0) {
      if (var0 != null && !var0.isEmpty()) {
         ArrayList var1 = new ArrayList(var0.size());

         for(StatusInstance var3 : var0) {
            var1.add(var3.copy());
         }

         return Collections.unmodifiableList(var1);
      } else {
         return Collections.emptyList();
      }
   }

   private static int[] copy(int[] var0) {
      return var0 == null ? new int[0] : Arrays.copyOf(var0, var0.length);
   }

   private static byte[] copy(byte[] var0) {
      return var0 == null ? new byte[0] : Arrays.copyOf(var0, var0.length);
   }

   private static boolean[] copy(boolean[] var0) {
      return var0 == null ? new boolean[0] : Arrays.copyOf(var0, var0.length);
   }

   private static short[][] copy(short[][] var0) {
      if (var0 == null) {
         return new short[0][];
      } else {
         short[][] var1 = new short[var0.length][];

         for(int var2 = 0; var2 < var0.length; ++var2) {
            short[] var3 = var0[var2];
            var1[var2] = var3 == null ? new short[0] : Arrays.copyOf(var3, var3.length);
         }

         return var1;
      }
   }

   private static <T> List<T> immutableList(List<T> var0) {
      return Collections.unmodifiableList(new ArrayList((Collection)Objects.requireNonNull(var0, "values")));
   }

   private static <K, V> Map<K, V> immutableMap(Map<K, V> var0) {
      return Collections.unmodifiableMap(new LinkedHashMap((Map)Objects.requireNonNull(var0, "values")));
   }

   public static final class World {
      public final Integer eventIndex;
      public final int sceneId;
      public final int roomIndex;
      public final boolean hasTypedResumeMode;
      public final WorldResumeMode resumeMode;
      public final int cameraX;
      public final int cameraY;
      public final List<String> openedChests;
      public final List<Integer> sourceWorldFlags;
      public final List<WorldActorPosition> actorPositionOverrides;

      public World(Integer var1, int var2, int var3, boolean var4, WorldResumeMode var5, int var6, int var7, List<String> var8, List<Integer> var9, List<WorldActorPosition> var10) {
         this.eventIndex = var1;
         this.sceneId = var2;
         this.roomIndex = var3;
         this.hasTypedResumeMode = var4;
         this.resumeMode = (WorldResumeMode)Objects.requireNonNull(var5, "resumeMode");
         this.cameraX = var6;
         this.cameraY = var7;
         this.openedChests = SaveSnapshot.<String>immutableList(var8);
         this.sourceWorldFlags = SaveSnapshot.<Integer>immutableList(var9);
         this.actorPositionOverrides = SaveSnapshot.<WorldActorPosition>immutableList(var10);
      }
   }

   public static final class WorldActorPosition {
      public final int sceneId;
      public final int roomIndex;
      public final int actorId;
      public final int x;
      public final int y;

      public WorldActorPosition(int var1, int var2, int var3, int var4, int var5) {
         this.sceneId = var1;
         this.roomIndex = var2;
         this.actorId = var3;
         this.x = var4;
         this.y = var5;
      }
   }

   public static final class Player {
      public final Integer x;
      public final Integer y;
      public final Integer direction;
      public final Boolean visible;

      public Player(Integer var1, Integer var2, Integer var3, Boolean var4) {
         this.x = var1;
         this.y = var2;
         this.direction = var3;
         this.visible = var4;
      }
   }

   public static final class Actor {
      public final int index;
      public final Integer x;
      public final Integer y;
      public final Integer direction;
      public final Boolean visible;

      public Actor(int var1, Integer var2, Integer var3, Integer var4, Boolean var5) {
         this.index = var1;
         this.x = var2;
         this.y = var3;
         this.direction = var4;
         this.visible = var5;
      }
   }

   public static final class Story {
      public final int mainTaskProgress;
      public final int actorCount;
      public final Map<String, Byte> eventStates;
      public final List<BranchTask> branchTasks;

      public Story(int var1, int var2, Map<String, Byte> var3, List<BranchTask> var4) {
         this.mainTaskProgress = var1;
         this.actorCount = var2;
         this.eventStates = SaveSnapshot.<String, Byte>immutableMap(var3);
         this.branchTasks = SaveSnapshot.<BranchTask>immutableList(var4);
      }
   }

   public static final class BranchTask {
      public final int taskId;
      public final int status;

      public BranchTask(int var1, int var2) {
         this.taskId = var1;
         this.status = var2;
      }
   }

   public static final class Inventory {
      public final int money;
      public final int badges;
      public final Map<Integer, BagEntry> bagItems;
      public final List<EquipmentEntry> equipmentItems;
      public final List<MaterialEntry> materialItems;
      public final Map<Integer, SpecialRewardEntry> specialRewards;

      public Inventory(int var1, int var2, Map<Integer, BagEntry> var3, List<EquipmentEntry> var4, List<MaterialEntry> var5, Map<Integer, SpecialRewardEntry> var6) {
         this.money = var1;
         this.badges = var2;
         this.bagItems = SaveSnapshot.<Integer, BagEntry>immutableMap(var3);
         this.equipmentItems = SaveSnapshot.<EquipmentEntry>immutableList(var4);
         this.materialItems = SaveSnapshot.<MaterialEntry>immutableList(var5);
         this.specialRewards = SaveSnapshot.<Integer, SpecialRewardEntry>immutableMap(var6);
      }
   }

   public static final class Fashion {
      public final String profileVersion;
      public final String selectedStableKey;
      public final List<String> ownedStableKeys;
      public final FashionEconomy economy;

      public Fashion(String var1, String var2, List<String> var3, FashionEconomy var4) {
         this.profileVersion = (String)Objects.requireNonNull(var1, "profileVersion");
         this.selectedStableKey = (String)Objects.requireNonNull(var2, "selectedStableKey");
         this.ownedStableKeys = SaveSnapshot.<String>immutableList(var3);
         this.economy = (FashionEconomy)Objects.requireNonNull(var4, "economy");
      }
   }

   public static final class FashionEconomy {
      public final String profileVersion;
      public final String poolVersion;
      public final int blindBagCount;
      public final int fragmentCount;
      public final long drawSeed;
      public final long drawIndex;

      public FashionEconomy(String var1, String var2, int var3, int var4, long var5, long var7) {
         this.profileVersion = (String)Objects.requireNonNull(var1, "fashion economy profileVersion");
         this.poolVersion = (String)Objects.requireNonNull(var2, "fashion poolVersion");
         this.blindBagCount = var3;
         this.fragmentCount = var4;
         this.drawSeed = var5;
         this.drawIndex = var7;
      }
   }

   public static final class BagEntry {
      public final int id;
      public final int count;
      public final int bagChannel;
      public final boolean keepAtZero;

      public BagEntry(int var1, int var2, int var3, boolean var4) {
         this.id = var1;
         this.count = var2;
         this.bagChannel = var3;
         this.keepAtZero = var4;
      }
   }

   public static final class EquipmentEntry {
      public final int id;
      public final boolean equipped;

      public EquipmentEntry(int var1, boolean var2) {
         this.id = var1;
         this.equipped = var2;
      }
   }

   public static final class MaterialEntry {
      public final int id;
      public final int count;

      public MaterialEntry(int var1, int var2) {
         this.id = var1;
         this.count = var2;
      }
   }

   public static final class SpecialRewardEntry {
      public final int id;
      public final boolean unlocked;
      public final int stackCount;

      public SpecialRewardEntry(int var1, boolean var2, int var3) {
         this.id = var1;
         this.unlocked = var2;
         this.stackCount = var3;
      }
   }

   public static final class Progression {
      private final int[] badgeAchieved;
      private final int[] badgeEnhanced;
      private final byte[] collectionStates;
      private final boolean[] convenienceRewardsClaimed;
      public final long playTimeMillis;
      public final boolean gameCf;
      public final int avoidMonsterTicks;
      public final int avoidMonsterElapsed;
      public final boolean battleLoseReviveArmed;
      public final int battleLoseWorldMode;
      public final long dailyBadgeLastClaimEpochMillis;
      public final long dailyBadgeLastClaimPlayTimeMillis;
      public final long nguyenMocRaceWindowStartEpochMillis;
      public final int nguyenMocRaceAttemptCount;
      public final boolean nguyenMocRacePetRewardClaimed;
      public final long regionalCommissionWindowStartEpochMillis;
      public final int regionalCommissionCount;
      public final String regionalRematchState;
      public final int petBankExpansionPurchases;
      public final RainbowCharms rainbowCharms;
      public final String battlePassState;
      private final List<String> redeemedGiftCodes;
      public final boolean evolutionNoticeArmed;
      public final boolean eggActive;
      public final int eggActiveItemId;
      public final int eggType;
      public final int eggProgress;
      public final long eggDrawSeed;
      public final long eggDrawIndex;
      private final int[] eggKnownSpecies;
      private final int[] rideBlocked;
      public final int rideActiveIndex;
      public final int playerMoveSpeed;
      public final RecoveryCheckpoint recoveryCheckpoint;
      public final StoryRetryCheckpoint storyRetryCheckpoint;

      public Progression(int[] var1, int[] var2, byte[] var3, boolean[] var4, long var5, boolean var7, int var8, int var9, boolean var10, int var11, long var12, long var14, long var16, int var18, boolean var19, long var20, int var22, String var23, int var24, RainbowCharms var25, String var26, List<String> var27, boolean var28, boolean var29, int var30, int var31, int var32, long var33, long var35, int[] var37, int[] var38, int var39, int var40, RecoveryCheckpoint var41, StoryRetryCheckpoint var42) {
         this.badgeAchieved = SaveSnapshot.copy(var1);
         this.badgeEnhanced = SaveSnapshot.copy(var2);
         this.collectionStates = SaveSnapshot.copy(var3);
         this.convenienceRewardsClaimed = SaveSnapshot.copy(var4);
         this.playTimeMillis = Math.max(0L, var5);
         this.gameCf = var7;
         this.avoidMonsterTicks = var8;
         this.avoidMonsterElapsed = var9;
         this.battleLoseReviveArmed = var10;
         this.battleLoseWorldMode = var11;
         this.dailyBadgeLastClaimEpochMillis = Math.max(0L, var12);
         this.dailyBadgeLastClaimPlayTimeMillis = Math.max(0L, var14);
         this.nguyenMocRaceWindowStartEpochMillis = Math.max(0L, var16);
         this.nguyenMocRaceAttemptCount = Math.max(0, var18);
         this.nguyenMocRacePetRewardClaimed = var19;
         this.regionalCommissionWindowStartEpochMillis = Math.max(0L, var20);
         this.regionalCommissionCount = Math.max(0, var22);
         this.regionalRematchState = var23 == null ? "" : var23;
         if (var24 >= 0 && var24 <= 3) {
            this.petBankExpansionPurchases = var24;
            this.rainbowCharms = (RainbowCharms)Objects.requireNonNull(var25, "rainbowCharms");
            this.battlePassState = var26 == null ? "" : var26;
            this.redeemedGiftCodes = SaveSnapshot.<String>immutableList(var27);
            this.evolutionNoticeArmed = var28;
            this.eggActive = var29;
            this.eggActiveItemId = Math.max(0, var30);
            this.eggType = var31;
            this.eggProgress = var32;
            this.eggDrawSeed = var33;
            if (var35 < 0L) {
               throw new IllegalArgumentException("Invalid egg draw index.");
            } else {
               this.eggDrawIndex = var35;
               this.eggKnownSpecies = SaveSnapshot.copy(var37);
               this.rideBlocked = SaveSnapshot.copy(var38);
               this.rideActiveIndex = var39;
               this.playerMoveSpeed = var40;
               this.recoveryCheckpoint = (RecoveryCheckpoint)Objects.requireNonNull(var41, "recoveryCheckpoint");
               this.storyRetryCheckpoint = (StoryRetryCheckpoint)Objects.requireNonNull(var42, "storyRetryCheckpoint");
            }
         } else {
            throw new IllegalArgumentException("Invalid Pet-bank expansion purchases.");
         }
      }

      public int[] eggKnownSpecies() {
         return SaveSnapshot.copy(this.eggKnownSpecies);
      }

      public List<String> redeemedGiftCodes() {
         return this.redeemedGiftCodes;
      }

      public int[] badgeAchieved() {
         return SaveSnapshot.copy(this.badgeAchieved);
      }

      public int[] badgeEnhanced() {
         return SaveSnapshot.copy(this.badgeEnhanced);
      }

      public byte[] collectionStates() {
         return SaveSnapshot.copy(this.collectionStates);
      }

      public boolean[] convenienceRewardsClaimed() {
         return SaveSnapshot.copy(this.convenienceRewardsClaimed);
      }

      public int[] rideBlocked() {
         return SaveSnapshot.copy(this.rideBlocked);
      }
   }

   public static final class RainbowCharms {
      public final Map<Integer, Integer> tiers;
      public final int survivalId;
      public final int tacticalId;
      public final int explorationId;
      public final boolean starterClaimed;

      public RainbowCharms(Map<Integer, Integer> var1, int var2, int var3, int var4, boolean var5) {
         this.tiers = SaveSnapshot.<Integer, Integer>immutableMap(var1);
         this.survivalId = var2;
         this.tacticalId = var3;
         this.explorationId = var4;
         this.starterClaimed = var5;
      }
   }

   public static final class RecoveryCheckpoint {
      public final String checkpointId;
      public final int sceneId;
      public final int roomIndex;
      public final int transitionActorId;

      public RecoveryCheckpoint(String var1, int var2, int var3, int var4) {
         this.checkpointId = (String)Objects.requireNonNull(var1, "checkpointId");
         this.sceneId = var2;
         this.roomIndex = var3;
         this.transitionActorId = var4;
      }
   }

   public static final class StoryRetryCheckpoint {
      public final String checkpointId;
      public final String battleDescriptorKey;
      public final String tableauKey;
      public final StoryRetryCheckpointState.State state;
      public final int sceneId;
      public final int roomIndex;
      public final int groupIndex;
      public final int retryPhase;
      public final int playerX;
      public final int playerY;
      public final int playerDirection;
      private final int[] companionRoster;

      public StoryRetryCheckpoint(String var1, String var2, String var3, StoryRetryCheckpointState.State var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int[] var12) {
         this.checkpointId = (String)Objects.requireNonNull(var1, "checkpointId");
         this.battleDescriptorKey = (String)Objects.requireNonNull(var2, "battleDescriptorKey");
         this.tableauKey = (String)Objects.requireNonNull(var3, "tableauKey");
         this.state = (StoryRetryCheckpointState.State)Objects.requireNonNull(var4, "state");
         this.sceneId = var5;
         this.roomIndex = var6;
         this.groupIndex = var7;
         this.retryPhase = var8;
         this.playerX = var9;
         this.playerY = var10;
         this.playerDirection = var11;
         this.companionRoster = SaveSnapshot.copy(var12);
      }

      public int[] companionRoster() {
         return SaveSnapshot.copy(this.companionRoster);
      }

      public static StoryRetryCheckpoint none() {
         return new StoryRetryCheckpoint("", "", "", StoryRetryCheckpointState.State.NONE, -1, -1, -1, -1, 0, 0, 0, new int[0]);
      }
   }

   public static final class Pets {
      public final int refreshOperations;
      public final int companionPetSlot;
      public final List<Pet> roster;
      public final List<Pet> bank;

      public Pets(int var1, int companionPetSlot, List<Pet> var2, List<Pet> var3) {
         this.refreshOperations = var1;
         this.companionPetSlot = companionPetSlot;
         this.roster = SaveSnapshot.<Pet>immutableList(var2);
         this.bank = SaveSnapshot.<Pet>immutableList(var3);
      }
   }

   public static final class Runtime {
      public final boolean speedX2;

      public Runtime(boolean var1) {
         this.speedX2 = var1;
      }
   }

   public static final class Pet {
      public final int slot;
      public final int speciesId;
      public final int level;
      public final int quality;
      public final int nature;
      public final int physicalTraitId;
      public final int refreshCount;
      public final int specialUseId;
      public final int heldEquipmentId;
      public final int battleSideFlag;
      public final int currentHp;
      public final int maxHpAtSave;
      public final int experience;
      public final int visualSpriteId;
      private final int[] skillIds;
      private final int[] skillCooldowns;
      private final short[][] buffSlots;
      private final short[][] debuffSlots;
      private final List<StatusInstance> durableStatuses;

      public Pet(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int[] var15, int[] var16, short[][] var17, short[][] var18) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, var15, var16, var17, var18, SaveSnapshot.legacyDurableDebuffs(var18));
      }

      public Pet(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int[] var15, int[] var16, short[][] var17, short[][] var18, List<StatusInstance> var19) {
         this.slot = var1;
         this.speciesId = var2;
         this.level = var3;
         this.quality = var4;
         this.nature = var5;
         this.physicalTraitId = var6;
         this.refreshCount = var7;
         this.specialUseId = var8;
         this.heldEquipmentId = var9;
         this.battleSideFlag = var10;
         this.currentHp = var11;
         this.maxHpAtSave = var12;
         this.experience = var13;
         this.visualSpriteId = var14;
         this.skillIds = SaveSnapshot.copy(var15);
         this.skillCooldowns = SaveSnapshot.copy(var16);
         this.buffSlots = SaveSnapshot.copy(var17);
         this.debuffSlots = SaveSnapshot.copy(var18);
         this.durableStatuses = SaveSnapshot.copyStatuses(var19);
      }

      public int[] skillIds() {
         return SaveSnapshot.copy(this.skillIds);
      }

      public int[] skillCooldowns() {
         return SaveSnapshot.copy(this.skillCooldowns);
      }

      public short[][] buffSlots() {
         return SaveSnapshot.copy(this.buffSlots);
      }

      public short[][] debuffSlots() {
         return SaveSnapshot.copy(this.debuffSlots);
      }

      public List<StatusInstance> durableStatuses() {
         return SaveSnapshot.copyStatuses(this.durableStatuses);
      }
   }
}
