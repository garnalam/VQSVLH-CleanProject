package vqsv.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import vqsv.battle.ui.VqsvBattleUiMode;
import vqsv.battlepass.BattlePassState;
import vqsv.charm.RainbowCharmState;
import vqsv.fashion.WardrobeState;
import vqsv.inventory.BagItemState;
import vqsv.inventory.CurrencyState;
import vqsv.inventory.EquipmentState;
import vqsv.inventory.MaterialStack;
import vqsv.inventory.SpecialRewardState;
import vqsv.progression.BadgeProgression;
import vqsv.progression.EggProgression;
import vqsv.progression.EvolutionProgression;
import vqsv.progression.PetCollectionProgression;
import vqsv.progression.PlayTimeProgression;
import vqsv.progression.RegionalRematchState;
import vqsv.progression.RideProgression;
import vqsv.quest.BranchTask;
import vqsv.quest.QuestMarker;
import vqsv.world.RecoveryCheckpointState;
import vqsv.world.StoryRetryCheckpointState;
import vqsv.world.WorldResumeMode;
import vqsv.world.event.VqsvEventState;

public final class GameSession<A, U> {
   public final WorldState world;
   public final StoryState story = new StoryState();
   public final PetState pets = new PetState();
   public final InventoryState inventory = new InventoryState();
   public final ProgressionState progression = new ProgressionState();
   public final WardrobeState fashion = new WardrobeState();
   public final RuntimeState<A, U> runtime;

   public GameSession(int var1, int var2, U var3) {
      this.world = new WorldState(var1, var2);
      this.runtime = new RuntimeState<A, U>(var3);
   }

   public static final class WorldState {
      public int eventIndex;
      public int currentSceneId = -1;
      public int currentRoomIndex = -1;
      public int cameraX;
      public int cameraY;
      public int playerX;
      public int playerY;
      public boolean transmitConfirmed;
      public int transmitScene = -1;
      public int transmitRoom = -1;
      public int transmitX = -1;
      public int transmitY = -1;
      public int transmitG = -1;
      public int transmitT;
      public boolean useMap;
      public int followActorId = -1;
      public int worldEventActor = -1;
      public int battleEventActor = -1;
      public int transitionCenterX;
      public int transitionCenterY;
      public int transitionWidth;
      public int transitionHeight;
      public int transitionDirection;
      public int nextWorldF = -1;
      public int nextWorldG = -1;
      public int nextWorldActor = -1;
      public WorldResumeMode resumeMode;
      public final Set<String> openedChests;
      public final Set<Integer> sourceWorldFlags;
      public final Map<String, WorldActorPosition> actorPositionOverrides;

      public void rememberActorPosition(int var1, int var2, int var3, int var4, int var5) {
         WorldActorPosition var6 = new WorldActorPosition(var1, var2, var3, var4, var5);
         this.actorPositionOverrides.put(var6.key(), var6);
      }

      public WorldActorPosition actorPosition(int var1, int var2, int var3) {
         return (WorldActorPosition)this.actorPositionOverrides.get(GameSession.WorldActorPosition.key(var1, var2, var3));
      }

      public void forgetActorPosition(int var1, int var2, int var3) {
         this.actorPositionOverrides.remove(GameSession.WorldActorPosition.key(var1, var2, var3));
      }

      private WorldState(int var1, int var2) {
         this.resumeMode = WorldResumeMode.LINEAR_EVENTS;
         this.openedChests = new HashSet();
         this.sourceWorldFlags = new HashSet();
         this.actorPositionOverrides = new HashMap();
         this.transitionWidth = var1;
         this.transitionHeight = var2;
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

      public String key() {
         return key(this.sceneId, this.roomIndex, this.actorId);
      }

      private static String key(int var0, int var1, int var2) {
         return var0 + ":" + var1 + ":" + var2;
      }
   }

   public static final class StoryState {
      public final VqsvEventState eventState = new VqsvEventState();
      public int mainTaskProgress;
      public final List<BranchTask> branchTasks = new ArrayList();
      public final List<QuestMarker> questMarkers = new ArrayList();

      public List<String> trace() {
         return this.eventState.trace();
      }
   }

   public static final class PetState {
      public final List<vqsv.pet.PetState> roster = new ArrayList();
      public final List<vqsv.pet.PetState> bank = new ArrayList();
      public int refreshOperations;
      public int companionPetSlot = -1;
   }

   public static final class InventoryState {
      public final CurrencyState currency = new CurrencyState();
      public final Map<Integer, BagItemState> bagItems = new HashMap();
      public final Map<Integer, SpecialRewardState> specialRewards = new HashMap();
      public final List<EquipmentState> equipmentItems = new ArrayList();
      public final List<MaterialStack> materialItems = new ArrayList();
   }

   public static final class ProgressionState {
      public final BadgeProgression badges = new BadgeProgression();
      public final PetCollectionProgression collection = new PetCollectionProgression();
      public final PlayTimeProgression playTime = new PlayTimeProgression();
      public int avoidMonsterTicks;
      public int avoidMonsterElapsed;
      public int encounterStepsRemaining = -1;
      public boolean battleLoseReviveArmed;
      public int battleLoseWorldMode;
      public final RecoveryCheckpointState recoveryCheckpoint = new RecoveryCheckpointState();
      public final StoryRetryCheckpointState storyRetryCheckpoint = new StoryRetryCheckpointState();
      public long dailyBadgeLastClaimEpochMillis;
      public long dailyBadgeLastClaimPlayTimeMillis;
      public long nguyenMocRaceWindowStartEpochMillis;
      public int nguyenMocRaceAttemptCount;
      public boolean nguyenMocRacePetRewardClaimed;
      public long regionalCommissionWindowStartEpochMillis;
      public int regionalCommissionCount;
      public final RegionalRematchState regionalRematches = new RegionalRematchState();
      public int petBankExpansionPurchases;
      public final RainbowCharmState rainbowCharms = new RainbowCharmState();
      public final BattlePassState battlePass = new BattlePassState();
      public final Set<String> redeemedGiftCodes = new TreeSet();
      public final EggProgression egg = new EggProgression();
      public final RideProgression ride = new RideProgression();
      public boolean gameCf;
      public final EvolutionProgression evolution = new EvolutionProgression();
      public int evolutionMode;
      public int evolutionNoticeIndex;
      public boolean evolutionNoticeArmed;
      public int evolutionTutorialU = -1;
      public boolean evolutionTutorialPending;
   }

   public static final class RuntimeState<A, U> {
      public A activity;
      public final U ui;
      public int battleOverlayTicks;
      public VqsvBattleUiMode battleUiMode;
      public int battleUiModeStartTick;
      public String battleStateName;
      public boolean speedX2;
      public boolean nguyenMocRaceActive;
      public int nguyenMocRaceCountdownValue;
      public int nguyenMocRaceCountdownTicks;
      public long nguyenMocRaceStartMillis;

      private RuntimeState(U var1) {
         this.battleUiMode = VqsvBattleUiMode.COMMAND;
         this.battleStateName = "";
         this.ui = var1;
      }
   }
}
