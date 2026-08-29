package vqsv.game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import vqsv.battle.data.BattleItemRow;
import vqsv.battle.data.BattleSkillRow;
import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.UnifiedSkillLearnsetCatalog;
import vqsv.battle.data.UnifiedSkillProfile;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.battle.model.BattleItemUseResult;
import vqsv.battle.model.BattleRenderUnit;
import vqsv.battle.model.BattleUnit;
import vqsv.battle.model.PetBattleAdapter;
import vqsv.battle.model.VqsvBattleRenderAdapter;
import vqsv.battle.ui.PetBattleViewAdapter;
import vqsv.battle.ui.PetStateDetailUiState;
import vqsv.battle.ui.RainbowSkillAnimationView;
import vqsv.battle.ui.VqsvBattleCatchRenderState;
import vqsv.battle.ui.VqsvBattleCommandRenderState;
import vqsv.battle.ui.VqsvBattleLevelUpView;
import vqsv.battle.ui.VqsvBattleNoticeRenderState;
import vqsv.battle.ui.VqsvBattleNpcEnemyEntryRenderState;
import vqsv.battle.ui.VqsvBattlePetStateView;
import vqsv.battle.ui.VqsvBattleRenderState;
import vqsv.battle.ui.VqsvBattleShopConfirmRenderState;
import vqsv.battle.ui.VqsvBattleSkillRenderState;
import vqsv.battle.ui.VqsvBattleTargetRenderState;
import vqsv.battle.ui.VqsvBattleUiMode;
import vqsv.battle.ui.VqsvMsgWarmView;
import vqsv.battle.ui.VqsvOpenBoxView;
import vqsv.data.ItemDefinition;
import vqsv.data.UnifiedItemCatalog;
import vqsv.data.UnifiedItemInventoryKind;
import vqsv.data.UnifiedItemRecord;
import vqsv.gameplay.PetBankExpansionService;
import vqsv.gameplay.PetItemService;
import vqsv.gameplay.PetManagementService;
import vqsv.gameplay.PetQualityUpgradeService;
import vqsv.inventory.BagItemState;
import vqsv.inventory.EquipmentState;
import vqsv.pet.PetState;
import vqsv.progression.EvolutionCandidate;
import vqsv.progression.EvolutionProgression;
import vqsv.pvp.PvpMatchSession;
import vqsv.quest.QuestMarker;
import vqsv.render.MapRenderer;
import vqsv.render.effect.ScreenEffect;
import vqsv.session.GameSession;
import vqsv.source.PetSourceAdapter;
import vqsv.source.VqsvSourceRandom;
import vqsv.text.VqsvText;
import vqsv.ui.layout.UiScrollbarMath;
import vqsv.ui.layout.VqsvUiLayout;
import vqsv.ui.text.TextBox;
import vqsv.ui.text.UiFont;
import vqsv.world.RegionalShopRespawnPolicy;
import vqsv.world.WorldResumeMode;

final class VqsvGameRuntime {
   static final int W = 240;
   static final int H = 320;
   static final int SCALE = 2;

   private VqsvGameRuntime() {
   }

   static final class Scene {
      private static final int POINTER_CAPTURE_NONE = 0;
      private static final int POINTER_CAPTURE_PANEL = 1;
      private static final int POINTER_CAPTURE_SOURCE_ITEM = 2;
      private static final int SOURCE_EVOLVE_AH_DURATION_TICKS = 10;
      private static final int POINTER_CAPTURE_SOURCE_EQUIPMENT = 3;
      private static final int POINTER_CAPTURE_BATTLE = 4;
      private static final int SOURCE_PET_BANK_PARTY_CAPACITY = 6;
      private static final int SOURCE_PET_BANK_LIST_NONE = 0;
      private static final int SOURCE_PET_BANK_LIST_WITHDRAW = 1;
      private static final int SOURCE_PET_BANK_LIST_RELEASE = 2;
      static final int SOURCE_PET_SETTING_VISIBLE_ROWS = 6;
      static final int SOURCE_SKILL_VISIBLE_ROWS = 4;
      static final int SOURCE_SKILL_STATE_EQUIPPED = 0;
      static final int SOURCE_SKILL_STATE_AVAILABLE = 1;
      static final int SOURCE_SKILL_STATE_LEVEL_LOCKED = 2;
      static final int SOURCE_SKILL_STATE_EVOLUTION_LOCKED = 3;
      static final int SOURCE_SKILL_STATE_ALTERNATE_BRANCH = 4;
      static final int SOURCE_SKILL_RELEARN_BADGE_COST = 1;
      private static final int DIALOGUE_SKIP_FAST_FORWARD_LIMIT = 256;
      static int tenYearsEventIndex = -1;
      static int room1BunnyOp13EventIndex = -1;
      final UiFont font = new UiFont();
      final ScreenEffect effect = new ScreenEffect();
      final VqsvMapLoadingRuntime sourceMapLoading = new VqsvMapLoadingRuntime();
      final Actor[] actors = VqsvSceneActors.makeActors();
      final List<Event> events = makeEvents();
      final List<TempSprite> tempSprites = new ArrayList();
      final Actor player = new Actor(-1, 0, 0, 0, 0, 1, 1);
      final Actor petCompanion = new Actor(-2, 0, 0, 0, 0, 0, 1);
      final Actor tanNguyetLongMaBoss = new Actor(-3, 3004, 0, 118, 160, 0, 1);
      final ArrayDeque<int[]> petCompanionTrail = new ArrayDeque<>();
      int petCompanionBoundSlot = Integer.MIN_VALUE;
      int petCompanionScene = Integer.MIN_VALUE;
      int petCompanionRoom = Integer.MIN_VALUE;
      final WorldUi worldUi = new WorldUi();
      final GameSession<Blocking, VqsvPanelRuntime> session = new GameSession<Blocking, VqsvPanelRuntime>(240, 320, new VqsvPanelRuntime());
      MapRenderer mapRenderer;
      boolean sourceCameraLightAnchorActive;
      int sourceCameraLightAnchorX;
      int sourceCameraLightAnchorY;
      TextBox text;
      ChoiceBox choice;
      boolean dialogueSkipActive;
      Blocking dialogueSkipOwner;
      boolean key0;
      boolean keyUp;
      boolean keyDown;
      boolean keyLeft;
      boolean keyRight;
      boolean keyBack;
      int[] battleEncounter = new int[0];
      boolean battleCanLose = false;
      boolean battleScriptLocksInput = false;
      int battleMode = -1;
      int battleBackgroundMode = -1;
      int battleResultIndex = -1;
      int battleBranchTarget = -1;
      RegionalShopRespawnPolicy.Destination pendingBattleLossRespawn;
      boolean pendingStoryRetryRecovery;
      boolean pendingQuestPetRetryRecovery;
      BufferedImage battleBackgroundSnapshot;
      VqsvBattleRenderState battleRenderState;
      String battleEnemyName;
      String battlePlayerName;
      String battleLog;
      int battleLevelUpTicks;
      String battleMenuTitle;
      String battleMenuSubtitle;
      String battleMenuAction;
      String[] battleMenuNames;
      String[] battleMenuValues;
      String[] battleMenuDescriptions;
      int[] battleMenuIds;
      int[] battleMenuIconIds;
      int battleMenuIndex;
      int battleMenuScroll;
      VqsvChoiceUiView battleChoiceUi;
      int battleShopConfirmItemId;
      int battleShopConfirmQuantity;
      int battleShopConfirmTotal;
      int battleShopConfirmCurrency;
      VqsvBattleShopConfirmRenderState battleShopConfirmRenderState;
      VqsvBattlePetStateView[] battlePetStateRows;
      final PetStateDetailUiState battlePetStateDetailUi;
      String[] battleSkillNames;
      String[] battleSkillPpLabels;
      int[] battleSkillIds;
      int battleSkillIndex;
      int battleSkillScroll;
      String battleSkillDescription;
      VqsvBattleSkillRenderState battleSkillRenderState;
      VqsvBattleTargetRenderState battleTargetRenderState;
      String[] battleTargetNames;
      int[] battleTargetSlots;
      int battleTargetIndex;
      int battleTargetCount;
      boolean battleTargetPlayerSide;
      int battleP7Phase;
      int battleP7Ticks;
      int battleP7EffectAnimState;
      int battleP7EffectAnimCursor;
      boolean battleP7EffectOnPlayerSide;
      boolean battleP7AttackerPlayerSide;
      boolean battleP7TargetPlayerSide;
      boolean battleP7DamagePlayerSide;
      boolean battleP7DamageVisible;
      String battleP7DamageText;
      boolean battleP7DamageCritical;
      String battleP7DebuffText;
      String battleP7MissText;
      boolean battleP7PostEffectVisible;
      boolean battleP7PostEffectPlayerSide;
      String battleP7PostEffectText;
      boolean battleP7SpecialVisible;
      boolean battleP7SpecialOnPlayerSide;
      boolean battleP7BaseHiddenPlayerSide;
      boolean battleP7BaseHiddenEnemySide;
      int battleP7BaseStatePlayerSide;
      int battleP7BaseStateEnemySide;
      int battleP7BaseCursorPlayerSide;
      int battleP7BaseCursorEnemySide;
      int battleP7PlayerOffsetX;
      int battleP7PlayerOffsetY;
      int battleP7EnemyOffsetX;
      int battleP7EnemyOffsetY;
      boolean battleP7DeathEffectVisible;
      boolean battleP7DeathEffectPlayerSide;
      int battleP7DeathEffectSpriteId;
      int battleP7DeathEffectTick;
      int battleP7DeathEffectDuration;
      boolean battleGroundMarkersVisible;
      boolean battleActiveMarkerVisible;
      boolean battleActiveMarkerPlayerSide;
      int battleEnemyMarkerX;
      int battleEnemyMarkerY;
      int battlePlayerMarkerX;
      int battlePlayerMarkerY;
      boolean battleLVisible;
      boolean battleLPlayerSide;
      boolean battleLDrawAfter;
      int battleLType;
      int battleLSpriteId;
      int battleLFrame;
      int battleLDirection;
      short[] battleLRow;
      boolean battleP7ActorEffectVisible;
      boolean battleP7ActorEffectOnPlayerSide;
      int battleP7ActorEffectSourceId;
      int battleP7ActorEffectSpriteId;
      int battleP7ActorEffectState;
      int battleP7ActorEffectCursor;
      int battleAnimationTick;
      int battleP7SpecialType;
      int battleP7SpecialAlpha;
      int battleP7SpecialRed;
      int battleP7SpecialGreen;
      int battleP7SpecialBlue;
      int battleP7SpecialDuration;
      int battleP7SpecialInterval;
      int battleP7SpecialTextureId;
      int battleP7SpecialBlendMode;
      int battleP7SpecialScrollMode;
      short[] battleP7SpecialRow;
      RainbowSkillAnimationView battleRainbowSkillAnimation;
      boolean battleActiveQueueVisible;
      boolean battleActiveQueuePlayerSide;
      int battleActiveQueueBank;
      int battleActiveQueueEffectId;
      int battleActiveQueueBuffId;
      int battleActiveQueueSegment;
      int battleActiveQueueTicks;
      int battlePlayerStatusCount;
      int[] battlePlayerStatusIconCells;
      int[] battlePlayerStatusDurationCells;
      int battleEnemyStatusCount;
      int[] battleEnemyStatusIconCells;
      int[] battleEnemyStatusDurationCells;
      String battleWarningTitle;
      String battleWarningPrompt;
      VqsvMsgWarmView battleMsgWarm;
      VqsvBattleNoticeRenderState battleNoticeRenderState;
      VqsvOpenBoxView battleOpenBox;
      VqsvBattleCatchRenderState battleCatchRenderState;
      int battleCatchSpriteId;
      int battleCatchPhase;
      int battleCatchTicks;
      int battleCatchAnimCursor;
      int battleCatchItemId;
      int battleCatchChance;
      int battleCatchRoll;
      boolean battleCatchCaught;
      boolean battleCatchVisible;
      boolean battleCatchEffectVisible;
      int battleCatchEffectDx;
      int battleCatchEffectDy;
      int battleCatchEffectScale10;
      boolean battleEnemyHiddenByCatch;
      int battleEnemyLevel;
      int battlePlayerLevel;
      int battleEnemyVisualId;
      int battlePlayerVisualId;
      int battleEnemyElement;
      int battlePlayerElement;
      boolean battleEnemyOwnedSpecies;
      int battleEnemyPowerPercent;
      int battlePlayerPowerPercent;
      boolean battleNpcEnemyEntryVisible;
      int battleNpcEnemyEntryTick;
      int battleNpcEnemyEntryStep;
      int battleNpcEnemyEntryFrame;
      int battleNpcEnemyPlayerCount;
      int battleNpcEnemyEnemyCount;
      int battleNpcEnemyPlayerVisualId;
      int battleNpcEnemyEnemyVisualId;
      boolean battlePvpOnline;
      int battlePvpTurn;
      int battlePvpRemainingSeconds;
      boolean battlePvpTimerActive;
      boolean battlePvpWaiting;
      VqsvBattleNpcEnemyEntryRenderState battleNpcEnemyEntryRenderState;
      int battleEnemyMaxHp;
      int battleEnemyHp;
      int battlePlayerMaxHp;
      int battlePlayerHp;
      int battleTurn;
      int battleCommandIndex;
      VqsvBattleCommandRenderState battleCommandRenderState;
      int battleClickX;
      int battleClickY;
      int battleHoverX;
      int battleHoverY;
      int battleShopBuyBadges;
      int battleShopBuyMoney;
      private int pointerCapture;
      private BattleRuntimeAdapter activeBattleInputOwner;
      private int sourceScrollbarGrabOffset;
      private boolean worldActorPointerActivation;
      private int worldActorPointerX;
      private int worldActorPointerY;
      int battlePlayerEnergy;
      int battlePlayerMaxEnergy;
      boolean battleCaptureTutorial;
      int battleTutorialU;
      int battleTutorialV;
      VqsvBattleLevelUpView battleLevelUpView;
      boolean sourcePremiumUiPercent;
      int sourceLeafRideActor;
      int sourceLeafRideDirection;
      int sourceLeafRidePhase;
      int sourceLeafRideTicks;
      int[] sourceTreePromptActors;
      int[] sourceTreeCutActors;
      boolean sourceTreeCutActive;
      final PetManagementService petManagement;
      final PetItemService petItems;
      final PetQualityUpgradeService petQualityUpgrade;
      final VqsvSourceRandom qualityUpgradeRandom;
      final VqsvBranchQuestRuntime sourceBranchQuests;
      boolean panelTitleResetRequested;
      boolean savePromptVisible;
      String savePromptMessage;
      String savePromptStatus;
      int savePromptSelected;
      int savePromptClickX;
      int savePromptClickY;
      boolean worldPetstateVisible;
      int sourceConveniencePetstateRow;
      int sourceConveniencePetstateWarningRow;
      boolean sourcePetBankDepositMode;
      int sourcePetBankListMode;
      int sourcePetBankDepositMessageMode;
      boolean sourcePetBankReleaseConfirmMode;
      boolean sourcePetSettingVisible;
      int sourcePetSettingIndex;
      int sourcePetSettingCount;
      int sourcePetSettingScroll;
      boolean sourceSkillVisible;
      int sourceSkillIndex;
      int sourceSkillScroll;
      int sourceSkillCount;
      int sourceSkillBrowseTab;
      boolean sourceSkillLearnMode;
      boolean sourceSkillLearnReturnPortableShop;
      boolean sourceSkillLearnReturnPetstate;
      boolean sourceSkillLearnReturnBrowse;
      boolean sourceSkillRelearnMode;
      boolean sourceSkillRelearnNotice;
      int sourceSkillRelearnBrowseIndex;
      boolean sourceSkillLearnConfirm;
      boolean sourceSkillLearnDeclineConfirm;
      boolean sourceSkillLearnReplaceMode;
      boolean sourceSkillLearnReplaceConfirm;
      int sourceSkillLearnQueueIndex;
      int[] sourceSkillLearnPetIndices;
      int[] sourceSkillLearnQueueSkillIds;
      int[] sourceSkillLearnIds;
      int[] sourceSkillLearnCandidateIds;
      int sourceSkillLearnSelectedId;
      boolean sourceItemChoiceVisible;
      int sourceItemChoiceIndex;
      int sourceItemChoiceScroll;
      int sourceItemChoiceMessageMode;
      boolean sourceEquipmentChoiceVisible;
      int sourceEquipmentChoiceIndex;
      int sourceEquipmentChoiceScroll;
      int sourceEquipmentChoiceMessageMode;
      boolean sourceReleaseConfirmVisible;
      String sourceReleaseConfirmMessage;
      String sourceReleaseConfirmAction;
      int sourceReleaseWarningMode;
      int sourcePetSettingActiveWarningMode;
      boolean sourceEvolveVisible;
      boolean qualityUpgradeVisible;
      boolean qualityUpgradePickerVisible;
      PetState qualityUpgradeMain;
      final PetQualityUpgradeService.PetReference[] qualityUpgradeFodders;
      final List<PetQualityUpgradeService.PetReference> qualityUpgradeCandidates;
      int qualityUpgradeFocus;
      int qualityUpgradePickerIndex;
      int qualityUpgradePickerScroll;
      int qualityUpgradePhase;
      int qualityUpgradeTicks;
      int qualityUpgradeRateBasisPoints;
      String qualityUpgradeStatus;
      PetQualityUpgradeService.AttemptResult qualityUpgradeResult;
      int panelBagState17ItemId;
      int panelBagState17MessageMode;
      int panelBagSpecialUseId;
      int panelBagSpecialUseMessageMode;
      boolean bufferedSourceTextConfirm;
      int sourceEvolvePetIndex;
      EvolutionCandidate sourceEvolveNotice;
      EvolutionCandidate.Kind sourceEvolvePreferredKind;
      int[] sourceEvolveOldStats;
      int[] sourceEvolveNewStats;
      int sourceEvolveOldVisualId;
      int sourceEvolveNewVisualId;
      int sourceEvolvePhase;
      int sourceEvolveEffectTicks;
      boolean sourceEvolveSucceeded;
      boolean rideShortcutPendingAfterMapLoad;
      int rideShortcutPostLoadDelay;

      Scene() {
         this.battleRenderState = VqsvBattleRenderState.EMPTY;
         this.battleEnemyName = "";
         this.battlePlayerName = "";
         this.battleLog = "";
         this.battleLevelUpTicks = 0;
         this.battleMenuTitle = "";
         this.battleMenuSubtitle = "";
         this.battleMenuAction = "";
         this.battleMenuNames = new String[0];
         this.battleMenuValues = new String[0];
         this.battleMenuDescriptions = new String[0];
         this.battleMenuIds = new int[0];
         this.battleMenuIconIds = new int[0];
         this.battleMenuIndex = 0;
         this.battleMenuScroll = 0;
         this.battleChoiceUi = VqsvChoiceUiView.EMPTY;
         this.battleShopConfirmItemId = -1;
         this.battleShopConfirmQuantity = 1;
         this.battleShopConfirmTotal = 0;
         this.battleShopConfirmCurrency = 0;
         this.battleShopConfirmRenderState = VqsvBattleShopConfirmRenderState.EMPTY;
         this.battlePetStateRows = VqsvBattlePetStateView.EMPTY_ARRAY;
         this.battlePetStateDetailUi = new PetStateDetailUiState();
         this.battleSkillNames = new String[0];
         this.battleSkillPpLabels = new String[0];
         this.battleSkillIds = new int[0];
         this.battleSkillIndex = 0;
         this.battleSkillScroll = 0;
         this.battleSkillDescription = "";
         this.battleSkillRenderState = VqsvBattleSkillRenderState.EMPTY;
         this.battleTargetRenderState = VqsvBattleTargetRenderState.EMPTY;
         this.battleTargetNames = new String[0];
         this.battleTargetSlots = new int[0];
         this.battleTargetIndex = 0;
         this.battleTargetCount = 0;
         this.battleTargetPlayerSide = false;
         this.battleP7Phase = 0;
         this.battleP7Ticks = 0;
         this.battleP7EffectAnimState = -1;
         this.battleP7EffectAnimCursor = 0;
         this.battleP7EffectOnPlayerSide = false;
         this.battleP7AttackerPlayerSide = false;
         this.battleP7TargetPlayerSide = false;
         this.battleP7DamagePlayerSide = false;
         this.battleP7DamageVisible = false;
         this.battleP7DamageText = "";
         this.battleP7DamageCritical = false;
         this.battleP7DebuffText = "";
         this.battleP7MissText = "";
         this.battleP7PostEffectVisible = false;
         this.battleP7PostEffectPlayerSide = false;
         this.battleP7PostEffectText = "";
         this.battleP7SpecialVisible = false;
         this.battleP7SpecialOnPlayerSide = false;
         this.battleP7BaseHiddenPlayerSide = false;
         this.battleP7BaseHiddenEnemySide = false;
         this.battleP7BaseStatePlayerSide = 0;
         this.battleP7BaseStateEnemySide = 0;
         this.battleP7BaseCursorPlayerSide = -1;
         this.battleP7BaseCursorEnemySide = -1;
         this.battleP7PlayerOffsetX = 0;
         this.battleP7PlayerOffsetY = 0;
         this.battleP7EnemyOffsetX = 0;
         this.battleP7EnemyOffsetY = 0;
         this.battleP7DeathEffectVisible = false;
         this.battleP7DeathEffectPlayerSide = false;
         this.battleP7DeathEffectSpriteId = -1;
         this.battleP7DeathEffectTick = 0;
         this.battleP7DeathEffectDuration = 0;
         this.battleGroundMarkersVisible = false;
         this.battleActiveMarkerVisible = false;
         this.battleActiveMarkerPlayerSide = false;
         this.battleEnemyMarkerX = 144;
         this.battleEnemyMarkerY = 85;
         this.battlePlayerMarkerX = 36;
         this.battlePlayerMarkerY = 206;
         this.battleLVisible = false;
         this.battleLPlayerSide = false;
         this.battleLDrawAfter = false;
         this.battleLType = -1;
         this.battleLSpriteId = -1;
         this.battleLFrame = 0;
         this.battleLDirection = 0;
         this.battleLRow = new short[0];
         this.battleP7ActorEffectVisible = false;
         this.battleP7ActorEffectOnPlayerSide = false;
         this.battleP7ActorEffectSourceId = -1;
         this.battleP7ActorEffectSpriteId = -1;
         this.battleP7ActorEffectState = 0;
         this.battleP7ActorEffectCursor = 0;
         this.battleAnimationTick = 0;
         this.battleP7SpecialType = -1;
         this.battleP7SpecialAlpha = 0;
         this.battleP7SpecialRed = 0;
         this.battleP7SpecialGreen = 0;
         this.battleP7SpecialBlue = 0;
         this.battleP7SpecialDuration = 0;
         this.battleP7SpecialInterval = 1;
         this.battleP7SpecialTextureId = -1;
         this.battleP7SpecialBlendMode = 0;
         this.battleP7SpecialScrollMode = 0;
         this.battleP7SpecialRow = new short[0];
         this.battleRainbowSkillAnimation = RainbowSkillAnimationView.EMPTY;
         this.battleActiveQueueVisible = false;
         this.battleActiveQueuePlayerSide = false;
         this.battleActiveQueueBank = -1;
         this.battleActiveQueueEffectId = -1;
         this.battleActiveQueueBuffId = -1;
         this.battleActiveQueueSegment = -1;
         this.battleActiveQueueTicks = 0;
         this.battlePlayerStatusCount = 0;
         this.battlePlayerStatusIconCells = new int[6];
         this.battlePlayerStatusDurationCells = new int[]{145, 145, 145, 145, 145, 145};
         this.battleEnemyStatusCount = 0;
         this.battleEnemyStatusIconCells = new int[6];
         this.battleEnemyStatusDurationCells = new int[]{145, 145, 145, 145, 145, 145};
         this.battleWarningTitle = "";
         this.battleWarningPrompt = "";
         this.battleMsgWarm = VqsvMsgWarmView.EMPTY;
         this.battleNoticeRenderState = VqsvBattleNoticeRenderState.EMPTY;
         this.battleOpenBox = VqsvOpenBoxView.EMPTY;
         this.battleCatchRenderState = VqsvBattleCatchRenderState.EMPTY;
         this.battleCatchSpriteId = -1;
         this.battleCatchPhase = -1;
         this.battleCatchTicks = 0;
         this.battleCatchAnimCursor = 0;
         this.battleCatchItemId = -1;
         this.battleCatchChance = 0;
         this.battleCatchRoll = -1;
         this.battleCatchCaught = false;
         this.battleCatchVisible = false;
         this.battleCatchEffectVisible = false;
         this.battleCatchEffectDx = 0;
         this.battleCatchEffectDy = 0;
         this.battleCatchEffectScale10 = 10;
         this.battleEnemyHiddenByCatch = false;
         this.battleEnemyOwnedSpecies = false;
         this.battleEnemyPowerPercent = 100;
         this.battlePlayerPowerPercent = 100;
         this.battleNpcEnemyEntryVisible = false;
         this.battleNpcEnemyEntryTick = 0;
         this.battleNpcEnemyEntryStep = -1;
         this.battleNpcEnemyEntryFrame = -1;
         this.battleNpcEnemyPlayerCount = 0;
         this.battleNpcEnemyEnemyCount = 0;
         this.battleNpcEnemyPlayerVisualId = -1;
         this.battleNpcEnemyEnemyVisualId = -1;
         this.battleNpcEnemyEntryRenderState = VqsvBattleNpcEnemyEntryRenderState.EMPTY;
         this.battleCommandIndex = 0;
         this.battleCommandRenderState = VqsvBattleCommandRenderState.EMPTY;
         this.battleClickX = -1;
         this.battleClickY = -1;
         this.battleHoverX = -1;
         this.battleHoverY = -1;
         this.battleShopBuyBadges = 0;
         this.battleShopBuyMoney = 0;
         this.worldActorPointerX = -1;
         this.worldActorPointerY = -1;
         this.battlePlayerMaxEnergy = 1;
         this.battleTutorialU = -1;
         this.battleTutorialV = 0;
         this.battleLevelUpView = VqsvBattleLevelUpView.EMPTY;
         this.sourceLeafRideActor = -1;
         this.sourceLeafRideDirection = -1;
         this.sourceTreePromptActors = new int[0];
         this.sourceTreeCutActors = new int[0];
         this.petManagement = new PetManagementService();
         this.petItems = new PetItemService();
         this.petQualityUpgrade = new PetQualityUpgradeService();
         this.qualityUpgradeRandom = VqsvSourceRandom.lazySourceSeeded();
         this.sourceBranchQuests = new VqsvBranchQuestRuntime(this.session.story.branchTasks, this.session.story.questMarkers);
         this.session.inventory.bagItems.putAll(VqsvSourceOps.initialSourceBagItems());
         this.session.inventory.specialRewards.putAll(VqsvSourceOps.initialSourceSpecialRewards());
         this.session.inventory.equipmentItems.addAll(VqsvSourceOps.initialSourceEquipmentItems());
         this.session.inventory.materialItems.addAll(VqsvSourceOps.initialSourceMaterialItems());
         this.panelTitleResetRequested = false;
         this.savePromptVisible = false;
         this.savePromptMessage = "";
         this.savePromptStatus = "";
         this.savePromptSelected = 0;
         this.savePromptClickX = -1;
         this.savePromptClickY = -1;
         this.worldPetstateVisible = false;
         this.sourceConveniencePetstateRow = -1;
         this.sourceConveniencePetstateWarningRow = -1;
         this.sourcePetBankDepositMode = false;
         this.sourcePetBankListMode = 0;
         this.sourcePetBankDepositMessageMode = 0;
         this.sourcePetBankReleaseConfirmMode = false;
         this.sourcePetSettingVisible = false;
         this.sourcePetSettingIndex = 0;
         this.sourcePetSettingCount = 6;
         this.sourcePetSettingScroll = 0;
         this.sourceSkillVisible = false;
         this.sourceSkillIndex = 0;
         this.sourceSkillScroll = 0;
         this.sourceSkillCount = 5;
         this.sourceSkillBrowseTab = 0;
         this.sourceSkillLearnMode = false;
         this.sourceSkillLearnReturnPortableShop = false;
         this.sourceSkillLearnReturnPetstate = false;
         this.sourceSkillLearnReturnBrowse = false;
         this.sourceSkillRelearnMode = false;
         this.sourceSkillRelearnNotice = false;
         this.sourceSkillRelearnBrowseIndex = 0;
         this.sourceSkillLearnConfirm = false;
         this.sourceSkillLearnDeclineConfirm = false;
         this.sourceSkillLearnReplaceMode = false;
         this.sourceSkillLearnReplaceConfirm = false;
         this.sourceSkillLearnQueueIndex = -1;
         this.sourceSkillLearnPetIndices = new int[0];
         this.sourceSkillLearnQueueSkillIds = new int[0];
         this.sourceSkillLearnIds = new int[0];
         this.sourceSkillLearnCandidateIds = new int[0];
         this.sourceSkillLearnSelectedId = -1;
         this.sourceItemChoiceVisible = false;
         this.sourceItemChoiceIndex = 0;
         this.sourceItemChoiceScroll = 0;
         this.sourceItemChoiceMessageMode = 0;
         this.sourceEquipmentChoiceVisible = false;
         this.sourceEquipmentChoiceIndex = 0;
         this.sourceEquipmentChoiceScroll = 0;
         this.sourceEquipmentChoiceMessageMode = 0;
         this.sourceReleaseConfirmVisible = false;
         this.sourceReleaseConfirmMessage = "";
         this.sourceReleaseConfirmAction = "";
         this.sourceReleaseWarningMode = 0;
         this.sourcePetSettingActiveWarningMode = 0;
         this.sourceEvolveVisible = false;
         this.qualityUpgradeVisible = false;
         this.qualityUpgradePickerVisible = false;
         this.qualityUpgradeFodders = new PetQualityUpgradeService.PetReference[2];
         this.qualityUpgradeCandidates = new ArrayList();
         this.qualityUpgradeFocus = 0;
         this.qualityUpgradePickerIndex = 0;
         this.qualityUpgradePickerScroll = 0;
         this.qualityUpgradePhase = 0;
         this.qualityUpgradeTicks = 0;
         this.qualityUpgradeRateBasisPoints = 0;
         this.qualityUpgradeStatus = "";
         this.panelBagState17ItemId = -1;
         this.panelBagState17MessageMode = 0;
         this.panelBagSpecialUseId = -1;
         this.panelBagSpecialUseMessageMode = 0;
         this.bufferedSourceTextConfirm = false;
         this.sourceEvolvePetIndex = -1;
         this.sourceEvolveOldStats = new int[]{0, 0, 0, 0};
         this.sourceEvolveNewStats = new int[]{0, 0, 0, 0};
         this.sourceEvolveOldVisualId = -1;
         this.sourceEvolveNewVisualId = -1;
         this.sourceEvolvePhase = 0;
         this.sourceEvolveEffectTicks = 0;
         this.sourceEvolveSucceeded = false;
         this.rideShortcutPendingAfterMapLoad = false;
         this.rideShortcutPostLoadDelay = 0;
      }

      void press0() {
         this.key0 = true;
         if (this.text != null && this.text.acceptsBufferedConfirm() && !this.text.readyForKey) {
            this.bufferedSourceTextConfirm = true;
         }

      }

      void pressRideShortcut() {
         if (this.session.progression.ride.activeIndex >= 0) {
            this.rideShortcutPendingAfterMapLoad = false;
            this.rideShortcutPostLoadDelay = 0;
            this.dispatchRideShortcut("key9-hard-dismount");
         } else if (this.sourceMapLoading.active()) {
            this.rideShortcutPendingAfterMapLoad = true;
            this.rideShortcutPostLoadDelay = 1;
            this.session.story.trace().add("PC_QOL key9 queued during source map loading scene=[" + this.session.world.currentSceneId + "," + this.session.world.currentRoomIndex + "]");
         } else {
            boolean var1 = this.dispatchRideShortcut("key9-shortcut");
            this.rideShortcutPendingAfterMapLoad = !var1;
            this.rideShortcutPostLoadDelay = var1 ? 0 : 1;
            if (!var1) {
               this.session.story.trace().add("PC_QOL key9 ride UI request queued behind transient lock scene=[" + this.session.world.currentSceneId + "," + this.session.world.currentRoomIndex + "]");
            }

         }
      }

      private boolean dispatchRideShortcut(String var1) {
         if (this.session.progression.ride.activeIndex >= 0) {
            VqsvRideRuntime.dismount(this, var1);
            return true;
         } else if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
            return "RIDE".equals(((VqsvPanelRuntime)this.session.runtime.ui).modeName());
         } else if (!this.canOpenSourcePanel()) {
            return false;
         } else {
            ((VqsvPanelRuntime)this.session.runtime.ui).openRideFromWorld(this);
            return true;
         }
      }

      boolean consumeConfirm() {
         if (this.bufferedSourceTextConfirm) {
            if (this.text != null && this.text.acceptsBufferedConfirm() && this.text.readyForKey) {
               this.bufferedSourceTextConfirm = false;
               this.key0 = false;
               return true;
            }

            if (this.text == null || !this.text.acceptsBufferedConfirm()) {
               this.bufferedSourceTextConfirm = false;
            }
         }

         if (!this.key0) {
            return false;
         } else {
            this.key0 = false;
            return true;
         }
      }

      boolean actorActivationRequested(int var1) {
         if (!this.key0 && !this.worldActorPointerActivation) {
            return false;
         } else {
            return this.worldActorPointerActivation ? VqsvFreeWorldRuntime.worldPointerHitsActor(this, var1, this.worldActorPointerX, this.worldActorPointerY) : this.playerInteractsActorSourceMask(var1);
         }
      }

      boolean sourceObstacleActivationRequested(int var1) {
         if (!this.key0 && !this.worldActorPointerActivation) {
            return false;
         } else {
            if (this.worldActorPointerActivation) {
               Actor var2 = var1 >= 0 && var1 < this.actors.length ? this.actors[var1] : null;
               boolean var3 = var2 != null && var2.sourceObstacleMarker != null && this.worldActorPointerX >= var2.x - 12 && this.worldActorPointerX < var2.x + 12 && this.worldActorPointerY >= var2.y - 42 && this.worldActorPointerY < var2.y - 18;
               if (!var3 && !VqsvFreeWorldRuntime.worldPointerHitsActor(this, var1, this.worldActorPointerX, this.worldActorPointerY)) {
                  return false;
               }
            }

            return this.playerInteractsActorSourceMask(var1);
         }
      }

      boolean chestActivationRequested(int var1) {
         if (!this.key0 && !this.worldActorPointerActivation) {
            return false;
         } else {
            return this.worldActorPointerActivation && !VqsvFreeWorldRuntime.worldPointerHitsActor(this, var1, this.worldActorPointerX, this.worldActorPointerY) ? false : VqsvFreeWorldRuntime.playerInteractsSourceChestMask(this, var1);
         }
      }

      void consumeActorActivation() {
         this.key0 = false;
         this.clearWorldActorPointerActivation();
      }

      void clearWorldActorPointerActivation() {
         this.worldActorPointerActivation = false;
         this.worldActorPointerX = -1;
         this.worldActorPointerY = -1;
      }

      private void armWorldActorPointerActivation(int var1, int var2) {
         this.worldActorPointerActivation = true;
         this.worldActorPointerX = var1 + this.session.world.cameraX;
         this.worldActorPointerY = var2 + this.session.world.cameraY;
      }

      void click(int var1, int var2) {
         int var3 = var1 / 2;
         int var4 = var2 / 2;
         this.clickGame(var3, var4);
      }

      void pointerPressedGame(int var1, int var2) {
         if (!this.sourceMapLoading.active()) {
            if (this.pointerCapture != 0) {
               this.pointerReleasedGame(var1, var2);
            }

            if (this.session.runtime.battleOverlayTicks > 0) {
               BattleRuntimeAdapter var3 = this.battleInputOwner();
               if (var3 != null && var3.pointerPressedScrollbar(this, var1, var2)) {
                  if (var3.scrollbarDragging()) {
                     this.pointerCapture = 4;
                  }

               } else {
                  this.clickGame(var1, var2);
               }
            } else if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
               if (((VqsvPanelRuntime)this.session.runtime.ui).pointerPressedScrollbar(this, var1, var2)) {
                  if (((VqsvPanelRuntime)this.session.runtime.ui).scrollbarDragging()) {
                     this.pointerCapture = 1;
                  }

               } else {
                  this.clickGame(var1, var2);
               }
            } else if (this.sourceItemChoiceVisible) {
               if (this.sourceItemChoiceMessageMode != 0 || !this.pointerPressedSourceChoiceScrollbar(true, var1, var2)) {
                  this.clickGame(var1, var2);
               }
            } else if (this.sourceEquipmentChoiceVisible) {
               if (this.sourceEquipmentChoiceMessageMode != 0 || !this.pointerPressedSourceChoiceScrollbar(false, var1, var2)) {
                  this.clickGame(var1, var2);
               }
            } else {
               this.clickGame(var1, var2);
            }
         }
      }

      void pointerMovedGame(int var1, int var2) {
         if (!this.sourceMapLoading.active()) {
            if (this.pointerCapture == 1) {
               if (!((VqsvPanelRuntime)this.session.runtime.ui).dragScrollbar(this, var1, var2)) {
                  this.pointerCapture = 0;
               }

            } else if (this.pointerCapture != 4) {
               if (this.pointerCapture != 2 && this.pointerCapture != 3) {
                  this.hoverGame(var1, var2);
               } else {
                  this.dragSourceChoiceScrollbar(var2);
               }
            } else {
               BattleRuntimeAdapter var3 = this.battleInputOwner();
               if (var3 == null || !var3.dragScrollbar(this, var1, var2)) {
                  this.pointerCapture = 0;
               }

            }
         }
      }

      void pointerReleasedGame(int var1, int var2) {
         if (this.sourceMapLoading.active()) {
            this.pointerCapture = 0;
            this.sourceScrollbarGrabOffset = 0;
         } else {
            if (this.pointerCapture == 1) {
               ((VqsvPanelRuntime)this.session.runtime.ui).releaseScrollbar(this);
            } else if (this.pointerCapture == 4) {
               BattleRuntimeAdapter var3 = this.battleInputOwner();
               if (var3 != null) {
                  var3.releaseScrollbar(this);
               }
            } else if (this.pointerCapture == 2 || this.pointerCapture == 3) {
               this.session.story.trace().add("PC_QOL source choice scrollbar drag end type=" + (this.pointerCapture == 2 ? "item" : "equipment"));
            }

            this.pointerCapture = 0;
            this.sourceScrollbarGrabOffset = 0;
         }
      }

      boolean pointerCaptureActive() {
         return this.pointerCapture != 0;
      }

      void clickGame(int var1, int var2) {
         if (!this.sourceMapLoading.active()) {
            if (this.text != null) {
               if (this.text.skipClickHit(var1, var2)) {
                  this.text.prepareDialogueSkip();
                  this.dialogueSkipActive = true;
                  this.dialogueSkipOwner = (Blocking)this.session.runtime.activity;
                  this.key0 = true;
                  this.session.story.trace().add("PC_QOL dialogue skip requested");
               } else {
                  if (this.text.confirmClickHit(var1, var2)) {
                     this.key0 = true;
                     if (this.text.acceptsBufferedConfirm() && !this.text.readyForKey) {
                        this.bufferedSourceTextConfirm = true;
                     }
                  }

               }
            } else if (this.session.runtime.battleOverlayTicks > 0) {
               this.battleClickX = var1;
               this.battleClickY = var2;
               this.key0 = true;
            } else if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
               ((VqsvPanelRuntime)this.session.runtime.ui).click(this, var1, var2);
            } else if (this.savePromptVisible) {
               this.savePromptClickX = var1;
               this.savePromptClickY = var2;
               this.key0 = true;
            } else if (this.qualityUpgradeVisible) {
               this.clickQualityUpgrade(var1, var2);
            } else if (this.sourceEvolveVisible) {
               if (sourceRightSoftkeyHit(var1, var2)) {
                  this.keyBack = true;
               } else if (sourceLeftSoftkeyHit(var1, var2)) {
                  this.key0 = true;
               }

            } else if (!this.sourceSkillVisible) {
               if (this.sourceItemChoiceVisible) {
                  if (sourceRightSoftkeyHit(var1, var2)) {
                     this.keyBack = true;
                  } else {
                     int var8 = this.sourceChoiceIndexAt(var1, var2, this.sourceItemChoiceScroll, this.sourceItemChoiceSize());
                     if (var8 >= 0) {
                        this.sourceItemChoiceIndex = var8;
                        this.key0 = true;
                     } else if (sourceLeftSoftkeyHit(var1, var2)) {
                        this.key0 = true;
                     }
                  }

               } else if (this.sourceEquipmentChoiceVisible) {
                  if (sourceRightSoftkeyHit(var1, var2)) {
                     this.keyBack = true;
                  } else {
                     int var7 = this.sourceChoiceIndexAt(var1, var2, this.sourceEquipmentChoiceScroll, this.sourceEquipmentChoiceSize());
                     if (var7 >= 0) {
                        this.sourceEquipmentChoiceIndex = var7;
                        this.key0 = true;
                     } else if (sourceLeftSoftkeyHit(var1, var2)) {
                        this.key0 = true;
                     }
                  }

               } else if (this.sourceReleaseConfirmVisible) {
                  if (sourceRightSoftkeyHit(var1, var2)) {
                     this.keyBack = true;
                  } else if (sourceLeftSoftkeyHit(var1, var2)) {
                     this.key0 = true;
                  }

               } else if (this.sourcePetSettingVisible) {
                  this.clickSourcePetSetting(var1, var2);
               } else if (this.worldPetstateVisible) {
                  this.clickWorldPetstate(var1, var2);
               } else {
                  if (this.worldUi.visible && this.session.world.useMap) {
                     int var6 = this.worldUi.buttonAt(var1, var2);
                     if (var6 != 0) {
                        if (var6 == 7) {
                           this.worldUi.toggleShortcutMenu();
                           this.session.story.trace().add("PC_QOL world.ui shortcut menu " + (this.worldUi.shortcutMenuExpanded() ? "expanded" : "collapsed"));
                           return;
                        }

                        if (var6 == 5) {
                           this.pressRideShortcut();
                           return;
                        }

                        if (this.canOpenSourcePanel()) {
                           if (var6 == 1) {
                              ((VqsvPanelRuntime)this.session.runtime.ui).openGameSystemFromWorld(this);
                              return;
                           }

                           if (var6 == 3) {
                              this.toggleSpeedX2("world.ui speed checkbox");
                              return;
                           }

                           if (var6 == 4) {
                              ((VqsvPanelRuntime)this.session.runtime.ui).openTaskFromWorld(this);
                              return;
                           }

                           if (var6 == 6) {
                              this.session.story.trace().add("SOURCE world.ui map animation button pressed scene=[" + this.session.world.currentSceneId + "," + this.session.world.currentRoomIndex + "]");
                              return;
                           }

                           if (var6 == 2) {
                              ((VqsvPanelRuntime)this.session.runtime.ui).open(this);
                              this.session.story.trace().add("PORTED/PARTIAL world.ui right softkey source game.k P=0 key=262144 -> P=6 game.h.k gamemenu.ui open");
                              return;
                           }
                        }

                        this.session.story.trace().add("PARTIAL world.ui softkey click blocked button=" + var6);
                        return;
                     }
                  }

                  if (this.choice != null && this.choice.click(var1, var2)) {
                     this.key0 = true;
                  } else {
                     if (this.session.world.useMap && this.worldUi.visible) {
                        this.armWorldActorPointerActivation(var1, var2);
                     }

                  }
               }
            } else {
               if (sourceRightSoftkeyHit(var1, var2)) {
                  this.keyBack = true;
               } else {
                  int var3 = this.sourceSkillLearnMode ? -1 : this.sourceSkillTabAt(var1, var2);
                  if (var3 >= 0) {
                     this.switchSourceSkillBrowseTab(var3, "mouse tab");
                     return;
                  }

                  int var4 = this.sourceSkillIndexAt(var1, var2);
                  if (var4 >= 0) {
                     boolean var5 = var4 == this.sourceSkillIndex && (this.sourceSkillLearnMode || this.sourceSkillCanRelearnAt(var4));
                     this.sourceSkillIndex = var4;
                     if (var5) {
                        this.key0 = true;
                     }
                  } else if (sourceLeftSoftkeyHit(var1, var2)) {
                     if (!this.sourceSkillLearnMode && !this.sourceSkillCanRelearnAt(this.sourceSkillIndex)) {
                        this.switchSourceSkillBrowseTab(this.sourceSkillBrowseTab == 0 ? 1 : 0, "left softkey");
                     } else {
                        this.key0 = true;
                     }
                  }
               }

            }
         }
      }

      void hover(int var1, int var2) {
         int var3 = var1 / 2;
         int var4 = var2 / 2;
         this.hoverGame(var3, var4);
      }

      void hoverGame(int var1, int var2) {
         if (!this.sourceMapLoading.active()) {
            if (this.session.runtime.battleOverlayTicks > 0) {
               this.battleHoverX = var1;
               this.battleHoverY = var2;
            } else if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
               ((VqsvPanelRuntime)this.session.runtime.ui).hover(this, var1, var2);
            } else if (this.qualityUpgradeVisible) {
               this.hoverQualityUpgrade(var1, var2);
            } else if (this.sourceSkillVisible) {
               this.hoverSourceSkill(var1, var2);
            } else if (this.sourceItemChoiceVisible) {
               this.hoverSourceItemChoice(var1, var2);
            } else if (this.sourceEquipmentChoiceVisible) {
               this.hoverSourceEquipmentChoice(var1, var2);
            } else if (this.sourcePetSettingVisible) {
               this.hoverSourcePetSetting(var1, var2);
            } else {
               if (this.worldPetstateVisible) {
                  this.hoverWorldPetstate(var1, var2);
               }

            }
         }
      }

      void mouseWheel(int var1) {
         if (!this.sourceMapLoading.active()) {
            if (var1 != 0) {
               int var2 = Math.max(-5, Math.min(5, var1));
               if (!this.routeBattleMouseWheel(var2)) {
                  if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
                     ((VqsvPanelRuntime)this.session.runtime.ui).mouseWheel(this, var2);
                  } else if (this.sourcePetSettingVisible) {
                     this.mouseWheelSourcePetSetting(var2);
                  } else if (this.sourceSkillVisible) {
                     this.mouseWheelSourceSkill(var2);
                  } else if (this.sourceItemChoiceVisible) {
                     this.mouseWheelSourceItemChoice(var2);
                  } else if (this.sourceEquipmentChoiceVisible) {
                     this.mouseWheelSourceEquipmentChoice(var2);
                  } else {
                     if (this.worldPetstateVisible) {
                        this.mouseWheelWorldPetstate(var2);
                     }

                  }
               }
            }
         }
      }

      private boolean pointerPressedSourceChoiceScrollbar(boolean var1, int var2, int var3) {
         int[] var4 = this.sourceChoiceScrollbarGeometry(var1);
         if (var4 != null && var4[4] > var4[5] && UiScrollbarMath.trackContains(var2, var3, var4[0], var4[1], var4[2], var4[3])) {
            int var5 = var1 ? this.sourceItemChoiceScroll : this.sourceEquipmentChoiceScroll;
            int var6 = var4[6];
            int var7 = UiScrollbarMath.thumbY(var4[1], var4[3], var6, var4[4], var4[5], var5);
            if (UiScrollbarMath.thumbContains(var2, var3, var4[0], var4[2], var7, var6)) {
               this.pointerCapture = var1 ? 2 : 3;
               this.sourceScrollbarGrabOffset = var3 - var7;
               this.session.story.trace().add("PC_QOL source choice scrollbar drag begin type=" + (var1 ? "item" : "equipment") + " scroll=" + var5);
            } else {
               int var8 = UiScrollbarMath.pageScrollWithThumbHeight(var3, var5, var4[1], var4[3], var6, var4[4], var4[5]);
               this.applySourceChoiceScrollbar(var1, var8, "track");
            }

            return true;
         } else {
            return false;
         }
      }

      private void dragSourceChoiceScrollbar(int var1) {
         boolean var2 = this.pointerCapture == 2;
         if (var2 || this.pointerCapture == 3) {
            if ((!var2 || this.sourceItemChoiceVisible && this.sourceItemChoiceMessageMode == 0) && (var2 || this.sourceEquipmentChoiceVisible && this.sourceEquipmentChoiceMessageMode == 0)) {
               int[] var3 = this.sourceChoiceScrollbarGeometry(var2);
               if (var3 != null && var3[4] > var3[5]) {
                  int var4 = UiScrollbarMath.dragScrollWithThumbHeight(var1, this.sourceScrollbarGrabOffset, var3[1], var3[3], var3[6], var3[4], var3[5]);
                  this.applySourceChoiceScrollbar(var2, var4, "drag");
               } else {
                  this.pointerCapture = 0;
                  this.sourceScrollbarGrabOffset = 0;
               }
            } else {
               this.pointerCapture = 0;
               this.sourceScrollbarGrabOffset = 0;
            }
         }
      }

      private void applySourceChoiceScrollbar(boolean var1, int var2, String var3) {
         int var4 = var1 ? this.sourceItemChoiceSize() : this.sourceEquipmentChoiceSize();
         int var5 = Math.max(0, var4 - 5);
         int var6 = var1 ? this.sourceItemChoiceScroll : this.sourceEquipmentChoiceScroll;
         int var7 = var1 ? this.sourceItemChoiceIndex : this.sourceEquipmentChoiceIndex;
         int var8 = Math.max(0, Math.min(var5, var2));
         int var9 = clampIndexIntoVisible(var7, var8, var4, 5);
         if (var1) {
            this.sourceItemChoiceScroll = var8;
            this.sourceItemChoiceIndex = var9;
         } else {
            this.sourceEquipmentChoiceScroll = var8;
            this.sourceEquipmentChoiceIndex = var9;
         }

         if (var8 != var6 || var9 != var7) {
            this.session.story.trace().add("PC_QOL source choice scrollbar " + var3 + " type=" + (var1 ? "item" : "equipment") + " scroll=" + var8 + " selected=" + var9 + " rows=" + var4);
         }

      }

      private int[] sourceChoiceScrollbarGeometry(boolean var1) {
         int var2 = var1 ? this.sourceItemChoiceSize() : this.sourceEquipmentChoiceSize();
         if (var2 <= 5) {
            return null;
         } else {
            VqsvUiLayout var3 = VqsvUiLayout.load("choice.ui");
            int var4 = var3.x(50, 183);
            int var5 = var3.x(51, 183);
            int var6 = Math.min(var4, var5);
            int var7 = Math.max(var4 + Math.max(1, var3.w(50, 3)), var5 + Math.max(1, var3.w(51, 4)));
            return new int[]{var6, var3.y(50, 98), Math.max(1, var7 - var6), 72, var2, 5, 8};
         }
      }

      void claimBattleInputOwner(BattleRuntimeAdapter var1) {
         this.activeBattleInputOwner = var1;
      }

      void releaseBattleInputOwner(BattleRuntimeAdapter var1) {
         if (this.activeBattleInputOwner == var1) {
            this.activeBattleInputOwner = null;
         }

      }

      private BattleRuntimeAdapter battleInputOwner() {
         if (this.session.runtime.activity instanceof BattleRuntimeAdapter) {
            return (BattleRuntimeAdapter)this.session.runtime.activity;
         } else {
            return this.session.runtime.battleOverlayTicks > 0 ? this.activeBattleInputOwner : null;
         }
      }

      private boolean routeBattleMouseWheel(int var1) {
         BattleRuntimeAdapter var2 = this.battleInputOwner();
         return var2 == null ? false : var2.handleMouseWheel(this, var1);
      }

      void setMoveKey(int var1, boolean var2) {
         switch (var1) {
            case 8:
            case 27:
               this.keyBack = var2;
               break;
            case 37:
            case 65:
            case 100:
               this.keyLeft = var2;
               break;
            case 38:
            case 87:
            case 104:
               this.keyUp = var2;
               break;
            case 39:
            case 68:
            case 102:
               this.keyRight = var2;
               break;
            case 40:
            case 83:
            case 98:
               this.keyDown = var2;
         }

      }

      boolean panelTextEntryActive() {
         return ((VqsvPanelRuntime)this.session.runtime.ui).visible && ((VqsvPanelRuntime)this.session.runtime.ui).textEntryActive();
      }

      void typePanelText(String var1) {
         if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
            ((VqsvPanelRuntime)this.session.runtime.ui).typeText(this, var1);
         }

      }

      void clearInputForManualCheckpoint() {
         this.key0 = false;
         this.keyUp = false;
         this.keyDown = false;
         this.keyLeft = false;
         this.keyRight = false;
         this.keyBack = false;
         this.battleClickX = -1;
         this.battleClickY = -1;
         this.battleHoverX = -1;
         this.battleHoverY = -1;
         this.savePromptClickX = -1;
         this.savePromptClickY = -1;
         this.clearWorldActorPointerActivation();
      }

      boolean speedX2Enabled() {
         return this.session.runtime.speedX2;
      }

      boolean sourceMapLoadingActive() {
         return this.sourceMapLoading.active();
      }

      void beginSourceMapLoading(int var1, int var2, int var3, int var4) {
         this.pointerCapture = 0;
         this.sourceScrollbarGrabOffset = 0;
         this.dialogueSkipActive = false;
         this.dialogueSkipOwner = null;
         this.clearInputForManualCheckpoint();
         this.effect.clearOverlay();
         this.sourceMapLoading.begin(this, var1, var2, var3, var4);
      }

      void setSpeedX2ForNewGame(boolean var1) {
         this.session.runtime.speedX2 = var1;
         this.session.story.trace().add("PC_QOL title menu speed x2 new-game enabled=" + var1);
      }

      void toggleSpeedX2(String var1) {
         this.session.runtime.speedX2 = !this.session.runtime.speedX2;
         this.session.story.trace().add("PC_QOL speed x2 toggle enabled=" + this.session.runtime.speedX2 + " source=" + var1);
      }

      void skipIntroToTenYearsLaterForRelease() {
         if (tenYearsEventIndex < 0) {
            throw new IllegalStateException("Ten-years-later event index was not registered");
         } else {
            this.prepareTransition(199, 218, 240, 320, 2);
            this.markWorldTransition(1, 0, -1);
            this.loadScene1Room0(this.session.world.transitionCenterX, this.session.world.transitionCenterY);
            this.session.runtime.activity = null;
            this.text = null;
            this.choice = null;
            this.dialogueSkipActive = false;
            this.dialogueSkipOwner = null;
            this.session.runtime.battleOverlayTicks = 0;
            this.session.world.eventIndex = tenYearsEventIndex;
            VqsvSourceStoryState.ensureInitialDienMieu(this, "release skip intro ten-years-after");
            this.session.story.trace().add("REBUILD_POLICY new-game skip intro -> source transition scene1 room0 center=[199,218] group0 ten-years eventIndex=" + this.session.world.eventIndex);
         }
      }

      void requestPanelTitleResetFromSourceOption() {
         this.panelTitleResetRequested = true;
         this.session.story.trace().add("PORTED/PARTIAL panel game.h.n option.ui confirm c=0 reset game.i.a/b=0 game.g.y=false route game.i.a(7)->game.f.d/state8");
      }

      boolean consumePanelTitleResetRequestForRelease() {
         boolean var1 = this.panelTitleResetRequested;
         this.panelTitleResetRequested = false;
         return var1;
      }

      void tick() {
         if (this.sourceMapLoading.tick(this)) {
            this.clearInputForManualCheckpoint();
         } else {
            RecoveryCheckpointService.observeReachedHub(this);
            this.effect.tick();
            this.worldUi.tick();
            WorldPetCompanionRuntime.tick(this);
            TanNguyetLongMaBossRuntime.tick(this);
            if (this.session.runtime.battleOverlayTicks > 0 || this.worldPetstateVisible || this.sourcePetSettingVisible || this.sourceSkillVisible || this.sourceItemChoiceVisible || this.sourceEquipmentChoiceVisible || this.sourceReleaseConfirmVisible || this.sourceEvolveVisible || this.qualityUpgradeVisible) {
               ++this.battleAnimationTick;
            }

            if (this.session.runtime.battleUiMode == VqsvBattleUiMode.LEVEL_UP) {
               ++this.battleLevelUpTicks;
            } else {
               this.battleLevelUpTicks = 0;
            }

            if (this.text != null) {
               this.text.tick(this.font);
               if (this.dialogueSkipActive && this.text.isSkippableDialogue()) {
                  this.text.prepareDialogueSkip();
                  this.key0 = true;
               }

               if (this.text.disposed) {
                  this.text = null;
                  this.bufferedSourceTextConfirm = false;
               }
            }

            if (this.rideShortcutPendingAfterMapLoad && !this.sourceMapLoading.active()) {
               if (this.session.progression.ride.activeIndex >= 0) {
                  this.rideShortcutPendingAfterMapLoad = false;
                  this.rideShortcutPostLoadDelay = 0;
                  this.session.story.trace().add("PC_QOL queued key9 ride UI request cleared because a ride became active before retry");
               } else if (this.rideShortcutPostLoadDelay > 0) {
                  --this.rideShortcutPostLoadDelay;
               } else {
                  boolean var1 = this.dispatchRideShortcut("key9-map-load-retry");
                  this.rideShortcutPendingAfterMapLoad = !var1;
                  if (!var1) {
                     this.rideShortcutPostLoadDelay = 1;
                  }

                  if (var1) {
                     this.session.story.trace().add("PC_QOL key9 map-loading retry dispatched=true scene=[" + this.session.world.currentSceneId + "," + this.session.world.currentRoomIndex + "]");
                  }
               }
            }

            if (!((VqsvPanelRuntime)this.session.runtime.ui).visible && this.keyBack && this.canOpenSourcePanel()) {
               ((VqsvPanelRuntime)this.session.runtime.ui).open(this);
               this.keyBack = false;
               this.key0 = false;
            } else if (((VqsvPanelRuntime)this.session.runtime.ui).visible) {
               ((VqsvPanelRuntime)this.session.runtime.ui).tick(this);
            } else if (this.qualityUpgradeVisible) {
               this.tickQualityUpgrade();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
               this.keyLeft = false;
               this.keyRight = false;
            } else if (this.sourceEvolveVisible) {
               this.tickSourceEvolve();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceSkillVisible) {
               this.tickSourceSkill();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
               this.keyLeft = false;
               this.keyRight = false;
            } else if (this.sourceItemChoiceMessageMode != 0) {
               this.tickSourceItemChoiceMessage();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceEquipmentChoiceMessageMode != 0) {
               this.tickSourceEquipmentChoiceMessage();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceReleaseWarningMode != 0) {
               this.tickSourceReleaseWarningMessage();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourcePetSettingActiveWarningMode != 0) {
               this.tickSourcePetSettingActiveWarningMessage();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.panelBagState17MessageMode != 0) {
               this.tickPanelBagState17Message();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.panelBagSpecialUseMessageMode != 0) {
               this.tickPanelBagSpecialUseMessage();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourcePetBankDepositMessageMode != 0) {
               this.tickSourcePetBankDepositMessage();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceConveniencePetstateWarningRow >= 0) {
               this.tickSourceConveniencePetstateWarning();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceItemChoiceVisible) {
               this.tickSourceItemChoice();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceEquipmentChoiceVisible) {
               this.tickSourceEquipmentChoice();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourceReleaseConfirmVisible) {
               this.tickSourceReleaseConfirm();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.sourcePetSettingVisible) {
               this.tickSourcePetSetting();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else if (this.worldPetstateVisible) {
               this.tickWorldPetstate();
               this.key0 = false;
               this.keyBack = false;
               this.keyUp = false;
               this.keyDown = false;
            } else {
               for(int var8 = this.tempSprites.size() - 1; var8 >= 0; --var8) {
                  if (((TempSprite)this.tempSprites.get(var8)).tick(this)) {
                     this.tempSprites.remove(var8);
                  }
               }

               for(int var9 = this.session.story.questMarkers.size() - 1; var9 >= 0; --var9) {
                  if (VqsvQuestMarkerRuntime.tick((QuestMarker)this.session.story.questMarkers.get(var9), this)) {
                     this.session.story.questMarkers.remove(var9);
                  }
               }

               VqsvTalkPromptRuntime.tick(this);
               this.resumeRoom1BunnyOp13IfStranded();
               Blocking var10 = (Blocking)this.session.runtime.activity;
               boolean var2 = var10 != null;
               if (var10 != null) {
                  boolean var3 = var10.tick(this);
                  var3 = this.fastForwardSkippedDialogue(var10, var3);
                  if (BattleLossWorldService.applyPending(this)) {
                     this.finishDialogueSkip("battle-loss");
                     this.clearInputForManualCheckpoint();
                     return;
                  }

                  if (this.sourceMapLoading.active()) {
                     if (var3 && this.session.runtime.activity == var10) {
                        this.session.runtime.activity = null;
                     }

                     this.clearInputForManualCheckpoint();
                     return;
                  }

                  if (!var3) {
                     this.finishDialogueSkipAtBoundary();
                     this.key0 = false;

                     for(Actor var7 : this.actors) {
                        if (var7 != null) {
                           var7.tick();
                        }
                     }

                     this.player.tick();
                     this.updateCameraFollow();
                     return;
                  }

                  if (this.session.runtime.activity == var10) {
                     this.session.runtime.activity = null;
                  }
               }

               this.finishDialogueSkipAtBoundary();
               if (this.session.runtime.activity == null && this.startSourceEvolutionTutorialBridgeIfReady()) {
                  this.key0 = false;

                  for(Actor var25 : this.actors) {
                     if (var25 != null) {
                        var25.tick();
                     }
                  }

                  this.player.tick();
                  this.updateCameraFollow();
               } else if (this.session.runtime.activity == null && this.startSourceEvolutionNoticeIfReady()) {
                  this.key0 = false;

                  for(Actor var24 : this.actors) {
                     if (var24 != null) {
                        var24.tick();
                     }
                  }

                  this.player.tick();
                  this.updateCameraFollow();
               } else {
                  if (!var2 && this.session.runtime.activity == null && this.session.world.eventIndex < this.events.size()) {
                     Blocking var12 = ((Event)this.events.get(this.session.world.eventIndex++)).start(this);
                     this.session.runtime.activity = var12;
                     if (var12 != null) {
                        if (this.dialogueSkipActive && this.text != null && this.text.isSkippableDialogue()) {
                           this.text.prepareDialogueSkip();
                           this.key0 = true;
                        }

                        boolean var4 = var12.tick(this);
                        var4 = this.fastForwardSkippedDialogue(var12, var4);
                        if (var4 && this.session.runtime.activity == var12) {
                           this.session.runtime.activity = null;
                        }
                     }

                     this.finishDialogueSkipAtBoundary();
                  }

                  if (this.sourceMapLoading.active()) {
                     this.clearInputForManualCheckpoint();
                  } else {
                     this.key0 = false;
                     this.keyBack = false;

                     for(Actor var6 : this.actors) {
                        if (var6 != null) {
                           var6.tick();
                        }
                     }

                     this.player.tick();
                     this.updateCameraFollow();
                  }
               }
            }
         }
      }

      private boolean fastForwardSkippedDialogue(Blocking var1, boolean var2) {
         if (this.dialogueSkipActive && var1 != null) {
            this.clearDisposedSkippedDialogue();
            int var3 = 0;

            while(!var2 && this.session.runtime.activity == var1 && this.text != null && this.text.isSkippableDialogue()) {
               if (var3++ >= 256) {
                  this.finishDialogueSkip("fast-forward-guard");
                  break;
               }

               this.text.prepareDialogueSkip();
               this.key0 = true;
               var2 = var1.tick(this);
               this.clearDisposedSkippedDialogue();
            }

            return var2;
         } else {
            return var2;
         }
      }

      private void clearDisposedSkippedDialogue() {
         if (this.dialogueSkipActive && this.text != null && this.text.disposed) {
            this.text = null;
            this.bufferedSourceTextConfirm = false;
         }

      }

      private void finishDialogueSkipAtBoundary() {
         if (this.dialogueSkipActive) {
            if (this.text != null && this.text.endsDialogueSkipChain()) {
               this.finishDialogueSkip("text-boundary");
            } else if (this.choice == null && this.session.runtime.battleOverlayTicks <= 0 && !((VqsvPanelRuntime)this.session.runtime.ui).visible && !this.savePromptVisible && !this.worldPetstateVisible && !this.sourceSkillVisible && !this.sourceItemChoiceVisible && !this.sourceEquipmentChoiceVisible && !this.sourceReleaseConfirmVisible && !this.sourceEvolveVisible && !this.qualityUpgradeVisible) {
               if (this.session.runtime.activity == null && this.text == null && this.session.world.eventIndex >= this.events.size()) {
                  this.finishDialogueSkip("script-complete");
               }

            } else {
               this.finishDialogueSkip("modal-boundary");
            }
         }
      }

      private void finishDialogueSkip(String var1) {
         if (this.dialogueSkipActive || this.dialogueSkipOwner != null) {
            this.dialogueSkipActive = false;
            this.dialogueSkipOwner = null;
            this.key0 = false;
            this.session.story.trace().add("PC_QOL dialogue skip completed reason=" + var1);
         }
      }

      private boolean canOpenSourcePanel() {
         if (this.session.world.useMap && this.session.runtime.battleOverlayTicks <= 0 && this.text == null && this.choice == null && !this.savePromptVisible && !this.worldPetstateVisible && !this.sourceSkillVisible && !this.sourceItemChoiceVisible && !this.sourceEquipmentChoiceVisible && !this.sourceReleaseConfirmVisible && !this.sourceEvolveVisible && !this.qualityUpgradeVisible && !this.sourceTreeCutActive && !NguyenMocC4RaceRuntime.blocksPanel(this) && !Scene2Room1FreeWorld.blocksPanel(this)) {
            return this.session.runtime.activity == null || this.session.runtime.activity instanceof SourceWorldPanelOpen;
         } else {
            return false;
         }
      }

      private void resumeRoom1BunnyOp13IfStranded() {
         if (this.session.runtime.activity == null && room1BunnyOp13EventIndex >= 0 && this.session.world.resumeMode == WorldResumeMode.BUNNY_OP13 && this.session.world.currentSceneId == 1 && this.session.world.currentRoomIndex == 1 && this.session.story.eventState.sourceEventStateComplete(1, 1, 1) && !this.session.story.eventState.sourceEventStateComplete(1, 1, 0) && this.text == null && this.choice == null && !this.savePromptVisible && this.session.runtime.battleOverlayTicks <= 0 && !this.worldPetstateVisible && !this.sourceSkillVisible && !this.sourceItemChoiceVisible && !this.sourceEquipmentChoiceVisible && !this.sourceReleaseConfirmVisible && !this.sourceEvolveVisible && !this.qualityUpgradeVisible) {
            if (this.session.world.eventIndex == room1BunnyOp13EventIndex || this.session.world.eventIndex == room1BunnyOp13EventIndex + 1) {
               this.session.runtime.activity = VqsvWorldResumeDescriptor.restoreActivity(this, WorldResumeMode.BUNNY_OP13);
               this.session.story.trace().add("PORTED/PARTIAL recovered stranded room1 Bunny op13 free-world blocker eventIndex=" + this.session.world.eventIndex + " player=[" + this.player.x + "," + this.player.y + "]");
            }
         }
      }

      private boolean startSourceEvolutionNoticeIfReady() {
         if (this.session.progression.evolutionMode == 0 && !this.session.progression.evolution.queue.isEmpty()) {
            if (this.text == null && this.choice == null && !this.savePromptVisible && !this.worldPetstateVisible && !this.sourcePetSettingVisible && !this.sourceSkillVisible && !this.sourceItemChoiceVisible && !this.sourceEquipmentChoiceVisible && !this.sourceReleaseConfirmVisible && this.session.runtime.battleOverlayTicks <= 0 && this.session.world.eventIndex >= this.events.size()) {
               if (this.session.progression.evolutionNoticeIndex >= this.session.progression.evolution.queue.size()) {
                  this.session.progression.evolution.queue.clear();
                  this.session.progression.evolutionNoticeIndex = 0;
                  this.session.progression.evolutionMode = 1;
                  this.session.story.trace().add("PORTED/PARTIAL game.k evolution notice queue exhausted game.k.H.clear ac=0 game.k.I=1");
                  this.restoreCommissionedWorldOwnerAfterEvolutionNotice();
                  return false;
               } else {
                  EvolutionCandidate var1 = (EvolutionCandidate)this.session.progression.evolution.queue.get(this.session.progression.evolutionNoticeIndex);
                  boolean var2 = this.session.progression.evolutionNoticeIndex == this.session.progression.evolution.queue.size() - 1 && this.session.progression.evolution.selection[0] != -1;
                  String var3 = var1.targetKind == 3 ? "Dị hoá" : "Tiến hóa";
                  String var4 = this.sourceEvolutionPetName(var1);
                  this.text = var2 ? TextBox.msgWarm(VqsvText.Evolution.noticeDetailed(var4, var3), "Nhấn nút 5 để tiếp tục") : TextBox.openBox(VqsvText.Evolution.noticeSimple(var4, var3));
                  this.session.runtime.activity = new EvolutionNoticeBlocking(var2);
                  this.session.story.trace().add("PORTED/PARTIAL game.k evolution notice consume ac=" + this.session.progression.evolutionNoticeIndex + " species=" + var1.currentSpeciesId + " target=" + var1.targetSpeciesId + " targetKind=" + var1.targetKind + " detail=" + var2 + " text=" + (var2 ? "S.a/msgwarm-shaped" : "S.b/openbox-shaped") + " prompt=" + (var2 ? "Nhấn nút 5 để tiếp tục" : "none") + " evolve.ui=PENDING");
                  ++this.session.progression.evolutionNoticeIndex;
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private void restoreCommissionedWorldOwnerAfterEvolutionNotice() {
         if (this.session.runtime.activity == null && !this.session.progression.evolutionTutorialPending && this.session.world.resumeMode == WorldResumeMode.SCENE2_ROOM1_FREE_WORLD) {
            this.session.runtime.activity = VqsvWorldResumeDescriptor.restoreActivity(this, this.session.world.resumeMode);
            List var10000 = this.session.story.trace();
            String var10001 = String.valueOf(this.session.world.resumeMode);
            var10000.add("PORTED source evolution notice preserves commissioned free-world owner resumeMode=" + var10001 + " world=[" + this.session.world.currentSceneId + "," + this.session.world.currentRoomIndex + "]");
         }
      }

      private boolean startSourceEvolutionTutorialBridgeIfReady() {
         if (this.session.progression.evolutionTutorialPending && !this.session.progression.evolutionNoticeArmed && this.session.progression.evolution.selection[0] != -1) {
            if (this.text == null && this.choice == null && !this.savePromptVisible && !this.worldPetstateVisible && !this.sourcePetSettingVisible && !this.sourceSkillVisible && !this.sourceItemChoiceVisible && !this.sourceEquipmentChoiceVisible && !this.sourceReleaseConfirmVisible && this.session.runtime.battleOverlayTicks <= 0 && this.session.world.eventIndex >= this.events.size()) {
               if (!this.key0) {
                  return false;
               } else {
                  this.session.progression.evolutionTutorialPending = false;
                  this.session.progression.evolutionNoticeArmed = true;
                  this.session.progression.evolutionTutorialU = 4;
                  this.openWorldPetstate();
                  int var1 = this.findSourceEvolutionPetIndex();
                  if (var1 >= 0) {
                     this.battleMenuIndex = var1;
                  }

                  List var10000 = this.session.story.trace();
                  int var10001 = this.session.progression.evolution.selection[0];
                  var10000.add("PORTED/PARTIAL game.k evolution tutorial bridge U=4 K=true L=[" + var10001 + "," + this.session.progression.evolution.selection[1] + "] selectedPetIndex=" + this.battleMenuIndex + " next=evolve.ui-on-confirm");
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private int findSourceEvolutionPetIndex() {
         for(int var1 = 0; var1 < this.session.pets.roster.size(); ++var1) {
            PetState var2 = (PetState)this.session.pets.roster.get(var1);
            if (var2.level == this.session.progression.evolution.selection[0] && var2.speciesId == this.session.progression.evolution.selection[1]) {
               return var1;
            }
         }

         return -1;
      }

      private String sourceEvolutionPetName(EvolutionCandidate var1) {
         BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1.currentSpeciesId);
         return var2 != null && var2.validForBattle() ? var2.name("Pet " + var1.currentSpeciesId) : "Pet " + var1.currentSpeciesId;
      }

      void render(Graphics2D var1) {
         VqsvSceneView.render(this, var1);
      }

      void startPvpMatch(PvpMatchSession var1) {
         if (var1 == null) {
            throw new IllegalArgumentException("PVP match session is required");
         } else {
            Blocking var2 = (Blocking)this.session.runtime.activity;
            this.clearInputForManualCheckpoint();
            this.worldUi.visible = false;
            this.player.visible = false;
            this.session.runtime.activity = new PvpBattleThenResumeWorldRuntime(new BattleEntryTransitionThenRuntime(VqsvBattleRuntimeFactory.createPvp(var1), 6, 0), var2);
            this.session.story.trace().add("PVP source battle runtime started renderer=VqsvBattleRenderer");
         }
      }

      void openWorldPetstate() {
         this.sourcePetBankDepositMode = false;
         this.sourcePetBankListMode = 0;
         this.sourcePetBankDepositMessageMode = 0;
         this.sourcePetBankReleaseConfirmMode = false;
         this.panelBagState17ItemId = -1;
         this.openWorldPetstateInternal(true);
      }

      void openSourceConveniencePetstate(int var1) {
         if (var1 != 1 && var1 != 2) {
            throw new IllegalArgumentException("Convenience petstate requires row 1 or 2");
         } else {
            this.sourceConveniencePetstateRow = var1;
            this.sourceConveniencePetstateWarningRow = -1;
            this.sourcePetBankDepositMode = false;
            this.sourcePetBankListMode = 0;
            this.sourcePetBankDepositMessageMode = 0;
            this.sourcePetBankReleaseConfirmMode = false;
            this.panelBagState17ItemId = -1;
            this.panelBagSpecialUseId = -1;
            this.openWorldPetstateInternal(true);
            this.battleMenuAction = var1 == 1 ? "Tiến hóa" : "Dị hoá";
         }
      }

      void openSourcePetBankDepositPetstate() {
         this.panelBagState17ItemId = -1;
         this.panelBagSpecialUseId = -1;
         this.sourcePetBankDepositMode = true;
         this.sourcePetBankListMode = 0;
         this.sourcePetBankDepositMessageMode = 0;
         this.sourcePetBankReleaseConfirmMode = false;
         this.openWorldPetstateInternal(false);
         this.battleMenuAction = "Gởi lại";
         List var10000 = this.session.story.trace();
         int var10001 = this.session.pets.roster.size();
         var10000.add("PORTED/PARTIAL panel game.h.Z source pet bank deposit state7 open petstate.ui b=1 action=Gởi lại roster=" + var10001 + " bank=" + this.session.pets.bank.size());
      }

      void openSourcePetBankWithdrawPetstate() {
         this.openSourcePetBankStoragePetstate(1, "Lấy ra");
      }

      void openSourcePetBankReleasePetstate() {
         this.openSourcePetBankStoragePetstate(2, "Phóng sinh");
      }

      private void openSourcePetBankStoragePetstate(int var1, String var2) {
         this.panelBagState17ItemId = -1;
         this.panelBagSpecialUseId = -1;
         this.sourcePetBankDepositMode = false;
         this.sourcePetBankListMode = var1;
         this.sourcePetBankDepositMessageMode = 0;
         this.sourcePetBankReleaseConfirmMode = false;
         this.openWorldPetstateInternal(false);
         this.battleMenuAction = var2;
         List var10000 = this.session.story.trace();
         int var10001 = this.sourcePetBankListMode;
         var10000.add("PORTED/PARTIAL panel game.k.B source pet bank storage state15 open petstate.ui mode=" + var10001 + " action=" + var2 + " roster=" + this.session.pets.roster.size() + " bank=" + this.session.pets.bank.size());
      }

      void openPanelBagState17Petstate(int var1) {
         this.panelBagState17ItemId = var1;
         this.openWorldPetstateInternal(true);
         this.session.story.trace().add("PORTED/PARTIAL panel game.h.ac default itemId=" + var1 + " this.s=itemId o.a(17) close bag.ui -> game.h.W petstate.ui c=" + this.battleMenuIndex);
      }

      void openPanelBagSpecialUsePetstate(int var1) {
         this.panelBagSpecialUseId = var1;
         this.openWorldPetstateInternal(true);
         this.battleMenuAction = "Sử dụng";
         this.session.story.trace().add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case" + var1 + " this.s=specialId o.a(19) close bag.ui -> game.h.W petstate.ui c=" + this.battleMenuIndex);
      }

      private void openWorldPetstateInternal(boolean var1) {
         if (var1) {
            VqsvSourceStoryState.ensureInitialDienMieu(this, "repair before world petstate open when party is empty");
         }

         List var2 = this.sourcePetBankListMode == 0 ? this.session.pets.roster : this.session.pets.bank;
         this.worldPetstateVisible = true;
         this.battlePetStateDetailUi.open();
         this.session.runtime.battleUiModeStartTick = this.battleAnimationTick;
         this.battleMenuTitle = "Sủng vật trong hành trang";
         this.battleMenuSubtitle = "";
         this.battleMenuAction = "";
         this.battleMenuNames = new String[var2.size()];
         this.battleMenuValues = new String[var2.size()];
         this.battleMenuDescriptions = new String[0];
         this.battleMenuIds = new int[var2.size()];
         this.battleMenuIconIds = new int[var2.size()];

         for(int var3 = 0; var3 < var2.size(); ++var3) {
            BattleUnit var4 = PetBattleAdapter.toBattleUnit((PetState)var2.get(var3), (byte)0, this.session.progression.badges);
            BattleRenderUnit var5 = VqsvBattleRenderAdapter.toRenderUnit(var4, true);
            this.battleMenuIds[var3] = var3;
            this.battleMenuIconIds[var3] = -1;
            this.battleMenuNames[var3] = var5.name;
            String[] var10000 = this.battleMenuValues;
            String var10002 = var4.alive() ? "lv" + var4.level : "KO";
            var10000[var3] = var10002 + " " + var4.hp() + "/" + var5.maxHp;
         }

         if (this.battleMenuIndex < 0 || this.battleMenuIndex >= var2.size()) {
            this.battleMenuIndex = 0;
         }

         this.keepWorldPetstateSelectionVisible();
         this.rebuildWorldPetstateRows();
         this.session.story.trace().add("PORTED/PARTIAL world petstate.ui open owner=game.k rows=" + Arrays.toString(this.battleMenuIds));
      }

      private void tickWorldPetstate() {
         if (this.battlePetStateDetailUi.handleKeyboard(this.keyLeft, this.keyRight)) {
            this.key0 = false;
         } else {
            if (this.keyUp && this.battleMenuIndex > 0) {
               --this.battleMenuIndex;
               this.keepWorldPetstateSelectionVisible();
               this.rebuildWorldPetstateRows();
               if (this.panelBagState17ItemId >= 0) {
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.Z key=4100 itemId=" + this.panelBagState17ItemId + " selectedPet=" + this.battleMenuIndex);
               }
            } else if (this.keyDown && this.battleMenuIndex < this.battleMenuIds.length - 1) {
               ++this.battleMenuIndex;
               this.keepWorldPetstateSelectionVisible();
               this.rebuildWorldPetstateRows();
               if (this.panelBagState17ItemId >= 0) {
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.Z key=8448 itemId=" + this.panelBagState17ItemId + " selectedPet=" + this.battleMenuIndex);
               }
            }

            if (this.keyBack) {
               this.closeWorldPetstateDetailUi();
               if (this.sourcePetBankDepositMode) {
                  this.sourcePetBankDepositMode = false;
                  ((VqsvPanelRuntime)this.session.runtime.ui).openSourceVillagePetBankFromWorld(this, this.session.world.worldEventActor);
                  this.session.story.trace().add("PORTED/PARTIAL game.h.C back source pet bank deposit close petstate.ui -> game.k.D shop.ui selected=" + this.battleMenuIndex);
               } else if (this.sourcePetBankListMode != 0) {
                  int var2 = this.sourcePetBankListMode;
                  this.sourcePetBankListMode = 0;
                  ((VqsvPanelRuntime)this.session.runtime.ui).openSourceVillagePetBankFromWorld(this, this.session.world.worldEventActor);
                  this.session.story.trace().add("PORTED/PARTIAL game.k.aa back source pet bank storage close petstate.ui mode=" + var2 + " -> game.k.D shop.ui selected=" + this.battleMenuIndex);
               } else if (this.sourceConveniencePetstateRow >= 0) {
                  int var3 = this.sourceConveniencePetstateRow;
                  this.sourceConveniencePetstateRow = -1;
                  this.sourceConveniencePetstateWarningRow = -1;
                  ((VqsvPanelRuntime)this.session.runtime.ui).reopenSourceNguyenMocConvenienceAfterPetstate(this, var3);
               } else if (this.panelBagState17ItemId >= 0) {
                  int var4 = this.panelBagState17ItemId;
                  this.panelBagState17ItemId = -1;
                  ((VqsvPanelRuntime)this.session.runtime.ui).returnToBagFromState17Back(this, var4);
               } else if (this.panelBagSpecialUseId >= 0) {
                  int var5 = this.panelBagSpecialUseId;
                  this.panelBagSpecialUseId = -1;
                  ((VqsvPanelRuntime)this.session.runtime.ui).returnToBagFromSpecialUseBack(this, var5);
               } else if (this.session.progression.evolutionTutorialU != 4) {
                  ((VqsvPanelRuntime)this.session.runtime.ui).openMenuAt(this, 1, "game.h.X back petstate.ui -> P=6");
               }

               this.session.story.trace().add("PORTED/PARTIAL game.h.X back close petstate.ui selected=" + this.battleMenuIndex);
            } else {
               if (this.key0) {
                  if (this.panelBagSpecialUseId >= 0) {
                     this.confirmPanelBagSpecialUse();
                     return;
                  }

                  if (this.panelBagState17ItemId >= 0) {
                     this.confirmPanelBagState17();
                     return;
                  }

                  if (this.sourcePetBankDepositMode) {
                     this.confirmSourcePetBankDeposit();
                     return;
                  }

                  if (this.sourcePetBankListMode == 1) {
                     this.confirmSourcePetBankWithdraw();
                     return;
                  }

                  if (this.sourcePetBankListMode == 2) {
                     this.openSourcePetBankReleaseConfirm();
                     return;
                  }

                  if (this.sourceConveniencePetstateRow >= 0) {
                     this.confirmSourceConveniencePetstate();
                     return;
                  }

                  if (this.session.progression.evolutionTutorialU == 4 && this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.roster.size()) {
                     PetState var1 = (PetState)this.session.pets.roster.get(this.battleMenuIndex);
                     if (var1.level == this.session.progression.evolution.selection[0] && var1.speciesId == this.session.progression.evolution.selection[1]) {
                        this.openSourceEvolveUi(this.battleMenuIndex);
                        return;
                     }

                     this.session.story.trace().add("PORTED/PARTIAL game.k U=4 petstate wrong selection index=" + this.battleMenuIndex + " species=" + var1.speciesId + " level=" + var1.level + " expected L=[" + this.session.progression.evolution.selection[0] + "," + this.session.progression.evolution.selection[1] + "]");
                     return;
                  }

                  if (this.battleMenuIndex < 0 || this.battleMenuIndex >= this.session.pets.roster.size()) {
                     List var10000 = this.session.story.trace();
                     int var10001 = this.battleMenuIndex;
                     var10000.add("VERIFIED defensive-inert panel game.h.X petstate confirm ignored empty/invalid index=" + var10001 + " pets=" + this.session.pets.roster.size());
                     return;
                  }

                  this.openSourcePetSettingFromPetstate();
               }

            }
         }
      }

      private void confirmSourceConveniencePetstate() {
         int var1 = this.sourceConveniencePetstateRow;
         EvolutionCandidate.Kind var2 = var1 == 1 ? EvolutionCandidate.Kind.EVOLUTION : EvolutionCandidate.Kind.MUTATION;
         EvolutionCandidate var3 = VqsvSourceEvolutionRuntime.noticeForPet(this, this.battleMenuIndex, var2);
         if (var3 != null && var3.available()) {
            this.sourceConveniencePetstateRow = -1;
            this.sourceConveniencePetstateWarningRow = -1;
            this.session.story.trace().add("PORTED source game.k.aa convenience petstate confirm serviceRow=" + var1 + " selectedPet=" + this.battleMenuIndex + " kind=" + String.valueOf(var3.kind) + " -> game.h.bl evolve.ui");
            this.openSourceEvolveUi(this.battleMenuIndex, var2);
         } else {
            String var4 = var1 == 1 ? "Sủng vật này không thể tiến hóa" : "Sủng vật này không thể dị hoá";
            this.text = TextBox.msgWarm(var4, "Nhấn nút 5 để tiếp tục");
            this.sourceConveniencePetstateWarningRow = var1;
            this.session.story.trace().add("PORTED source game.k.aa convenience petstate reject serviceRow=" + var1 + " selectedPet=" + this.battleMenuIndex + " actualKind=" + String.valueOf(var3 == null ? EvolutionCandidate.Kind.NONE : var3.kind) + " -> msgwarm.ui stay petstate.ui");
         }
      }

      private void tickSourceConveniencePetstateWarning() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               int var1 = this.sourceConveniencePetstateWarningRow;
               this.sourceConveniencePetstateWarningRow = -1;
               this.session.story.trace().add("PORTED source game.k.aa convenience msgwarm close serviceRow=" + var1 + " -> state7 petstate.ui selectedPet=" + this.battleMenuIndex);
            }
         }

      }

      private void keepWorldPetstateSelectionVisible() {
         int var1 = Math.max(0, this.battleMenuIds.length - 6);
         if (this.battleMenuIndex < this.battleMenuScroll) {
            this.battleMenuScroll = this.battleMenuIndex;
         } else if (this.battleMenuIndex >= this.battleMenuScroll + 6) {
            this.battleMenuScroll = this.battleMenuIndex - 5;
         }

         this.battleMenuScroll = Math.max(0, Math.min(var1, this.battleMenuScroll));
      }

      private void rebuildWorldPetstateRows() {
         boolean var1 = this.sourcePetBankListMode == 0;
         List var2 = var1 ? this.session.pets.roster : this.session.pets.bank;
         this.battlePetStateRows = new VqsvBattlePetStateView[6];
         int var3 = Math.max(0, Math.min(this.battleMenuScroll, Math.max(0, this.battleMenuIds.length - this.battlePetStateRows.length)));

         for(int var4 = 0; var4 < this.battlePetStateRows.length; ++var4) {
            int var5 = var3 + var4;
            if (var5 >= this.battleMenuIds.length) {
               this.battlePetStateRows[var4] = VqsvBattlePetStateView.empty(var4);
            } else {
               int var6 = this.battleMenuIds[var5];
               if (var6 >= 0 && var6 < var2.size()) {
                  PetState var7 = (PetState)var2.get(var6);
                  this.battlePetStateRows[var4] = PetBattleViewAdapter.toPetStateView(var4, var6, var7, var1 && (var6 == 0 || var7.sourceK()));
               } else {
                  this.battlePetStateRows[var4] = VqsvBattlePetStateView.empty(var4);
               }
            }
         }

         this.syncWorldPetstateChoiceView();
      }

      private void syncWorldPetstateChoiceView() {
         this.battleChoiceUi = VqsvChoiceUiView.battle(this.battleMenuTitle, this.battleMenuSubtitle, this.battleMenuAction, stringList(this.battleMenuNames), stringList(this.battleMenuValues), new ArrayList(), intList(this.battleMenuIds), intList(this.battleMenuIconIds), this.battleMenuIndex, this.battleMenuScroll).withVisibleRows(6).withViewportScroll(this.battleMenuIndex, this.battleMenuScroll);
      }

      private static List<String> stringList(String[] var0) {
         ArrayList var1 = new ArrayList();
         if (var0 != null) {
            for(String var5 : var0) {
               var1.add(var5);
            }
         }

         return var1;
      }

      private static List<Integer> intList(int[] var0) {
         ArrayList var1 = new ArrayList();
         if (var0 != null) {
            for(int var5 : var0) {
               var1.add(var5);
            }
         }

         return var1;
      }

      private void mouseWheelWorldPetstate(int var1) {
         int var2 = this.battleMenuIds.length;
         if (var2 > 0) {
            int var3 = Math.max(0, this.battleMenuIds.length - 6);
            if (var3 <= 0) {
               int var6 = this.battleMenuIndex;
               this.battleMenuIndex = Math.max(0, Math.min(var2 - 1, this.battleMenuIndex + var1));
               if (this.battleMenuIndex != var6) {
                  this.rebuildWorldPetstateRows();
                  this.session.story.trace().add("PC_QOL mouse wheel world petstate selection selectedPet=" + this.battleMenuIndex + " rows=" + var2);
               }

            } else {
               int var4 = this.battleMenuScroll;
               int var5 = this.battleMenuIndex;
               this.battleMenuScroll = Math.max(0, Math.min(var3, this.battleMenuScroll + var1));
               this.battleMenuIndex = clampIndexIntoVisible(this.battleMenuIndex, this.battleMenuScroll, var2, 6);
               if (this.battleMenuScroll != var4 || this.battleMenuIndex != var5) {
                  this.rebuildWorldPetstateRows();
                  this.session.story.trace().add("PC_QOL mouse wheel world petstate scrollbar scroll=" + this.battleMenuScroll + " selectedPet=" + this.battleMenuIndex + " rows=" + this.battleMenuIds.length);
               }

            }
         }
      }

      private void confirmSourcePetBankDeposit() {
         PetManagementService.BankDepositResult var1 = this.petManagement.depositToBank(this.session.pets.roster, this.session.inventory.equipmentItems, this.session.pets.bank, this.battleMenuIndex, (new PetBankExpansionService()).capacity(this.session), this::sourcePetLiving);
         if (var1.outcome == PetManagementService.BankDepositOutcome.BANK_FULL) {
            this.text = TextBox.msgWarm("Ngân hàng đã đầy, không thể gởi lại", "Nhấn nút 5 để tiếp tục");
            this.sourcePetBankDepositMessageMode = 1;
            this.session.story.trace().add("PORTED panel game.k.aa state7 deposit bank full q.A()=false bank=" + var1.bankSizeAfter + " capacity=" + var1.bankCapacity + " -> msgwarm.ui f=1");
         } else if (var1.outcome == PetManagementService.BankDepositOutcome.LAST_LIVING) {
            this.text = TextBox.msgWarm("Ba lô phải lưu ít nhất 1 sủng vật", "Nhấn nút 5 để tiếp tục");
            this.sourcePetBankDepositMessageMode = 1;
            this.session.story.trace().add("PORTED panel game.k.aa state7 deposit blocked last living q.i(b)=false selectedPet=" + this.battleMenuIndex + " species=" + var1.speciesId + " -> msgwarm.ui f=1");
         } else if (var1.outcome != PetManagementService.BankDepositOutcome.EMPTY_ROSTER && var1.outcome != PetManagementService.BankDepositOutcome.INVALID_SELECTION) {
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.session.story.trace().add("PORTED panel game.k.aa state7 deposit q.b(A[b].Q()) q.n(b) selectedPet=" + var1.selectedIndexBefore + " species=" + var1.speciesId + " roster=" + (var1.rosterSizeAfter + 1) + "->" + var1.rosterSizeAfter + " bank=" + (var1.bankSizeAfter - 1) + "->" + var1.bankSizeAfter);
            if (this.session.pets.roster.isEmpty()) {
               this.closeWorldPetstateDetailUi();
               this.sourcePetBankDepositMode = false;
               ((VqsvPanelRuntime)this.session.runtime.ui).openSourceVillagePetBankFromWorld(this, this.session.world.worldEventActor);
               this.session.story.trace().add("PORTED panel game.h.C state7 deposit q.P empty close petstate.ui -> game.k.D shop.ui");
            } else {
               this.openSourcePetBankDepositPetstate();
            }
         } else {
            List var10000 = this.session.story.trace();
            String var10001 = String.valueOf(var1.outcome);
            var10000.add("PORTED/PARTIAL panel game.h.C state7 deposit ignored outcome=" + var10001 + " selectedPet=" + this.battleMenuIndex + " roster=" + this.session.pets.roster.size() + " bank=" + this.session.pets.bank.size());
         }
      }

      private void confirmSourcePetBankWithdraw() {
         PetManagementService.BankWithdrawResult var1 = this.petManagement.withdrawFromBank(this.session.pets.roster, this.session.pets.bank, this.battleMenuIndex, 6);
         if (var1.outcome == PetManagementService.BankWithdrawOutcome.ROSTER_FULL) {
            this.text = TextBox.msgWarm("Ba lô Sủng vật đã đủ", "Nhấn nút 5 để tiếp tục");
            this.sourcePetBankDepositMessageMode = 1;
            this.session.story.trace().add("PORTED panel game.h.C state15 withdraw party full q.B=" + var1.rosterSizeAfter + " capacity=" + var1.rosterCapacity + " -> msgwarm.ui f=1");
         } else if (var1.outcome != PetManagementService.BankWithdrawOutcome.EMPTY_BANK && var1.outcome != PetManagementService.BankWithdrawOutcome.INVALID_SELECTION) {
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.session.story.trace().add("PORTED panel game.h.C state15 withdraw q.r(h) selectedBankPet=" + var1.selectedIndexBefore + " species=" + var1.speciesId + " roster=" + (var1.rosterSizeAfter - 1) + "->" + var1.rosterSizeAfter + " bank=" + (var1.bankSizeAfter + 1) + "->" + var1.bankSizeAfter);
            if (this.session.pets.bank.isEmpty()) {
               this.closeWorldPetstateDetailUi();
               this.sourcePetBankListMode = 0;
               ((VqsvPanelRuntime)this.session.runtime.ui).openSourceVillagePetBankFromWorld(this, this.session.world.worldEventActor);
               this.session.story.trace().add("PORTED panel game.h.C state15 withdraw q.P empty close petstate.ui -> game.k.D shop.ui");
            } else {
               this.openSourcePetBankWithdrawPetstate();
            }
         } else {
            List var10000 = this.session.story.trace();
            String var10001 = String.valueOf(var1.outcome);
            var10000.add("PORTED/PARTIAL panel game.h.C state15 withdraw ignored outcome=" + var10001 + " selectedBankPet=" + this.battleMenuIndex + " roster=" + this.session.pets.roster.size() + " bank=" + this.session.pets.bank.size());
         }
      }

      private void openSourcePetBankReleaseConfirm() {
         if (this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.bank.size()) {
            PetState var1 = (PetState)this.session.pets.bank.get(this.battleMenuIndex);
            if (this.sourceReleaseProtectedPet(var1)) {
               this.text = TextBox.msgWarm("Thần thú không thể phóng sinh", "Nhấn nút 5 để tiếp tục");
               this.sourcePetBankDepositMessageMode = 1;
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.C state15 release protected aq.c[0][species][22]==2 selectedBankPet=" + this.battleMenuIndex + " species=" + var1.speciesId + " -> msgwarm.ui f=2");
            } else {
               this.sourcePetBankReleaseConfirmMode = true;
               this.sourceReleaseConfirmVisible = true;
               this.sourceReleaseConfirmMessage = "Bạn muốn phóng sinh sủng vật này?";
               this.sourceReleaseConfirmAction = "Xác nhận";
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.C state15 release -> msgconfirm.ui f=1 selectedBankPet=" + this.battleMenuIndex + " species=" + var1.speciesId);
            }
         } else {
            List var10000 = this.session.story.trace();
            int var10001 = this.battleMenuIndex;
            var10000.add("PORTED/PARTIAL panel game.h.C state15 release ignored invalid selectedBankPet=" + var10001 + " bank=" + this.session.pets.bank.size());
         }
      }

      private void confirmSourcePetBankRelease() {
         PetManagementService.BankReleaseResult var1 = this.petManagement.releaseFromBank(this.session.pets.bank, this.battleMenuIndex, this::sourceReleaseProtectedPet);
         this.sourceReleaseConfirmVisible = false;
         this.sourcePetBankReleaseConfirmMode = false;
         if (var1.outcome == PetManagementService.BankReleaseOutcome.PROTECTED) {
            this.text = TextBox.msgWarm("Thần thú không thể phóng sinh", "Nhấn nút 5 để tiếp tục");
            this.sourcePetBankDepositMessageMode = 1;
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.C state15 release protected on confirm selectedBankPet=" + this.battleMenuIndex + " species=" + var1.speciesId);
         } else if (var1.outcome != PetManagementService.BankReleaseOutcome.EMPTY_BANK && var1.outcome != PetManagementService.BankReleaseOutcome.INVALID_SELECTION) {
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.session.story.trace().add("PORTED panel game.h.C state15 release confirm q.m(P[h][2]) q.q(h) selectedBankPet=" + var1.selectedIndexBefore + " species=" + var1.speciesId + " bank=" + (var1.bankSizeAfter + 1) + "->" + var1.bankSizeAfter);
            if (this.session.pets.bank.isEmpty()) {
               this.closeWorldPetstateDetailUi();
               this.sourcePetBankListMode = 0;
               ((VqsvPanelRuntime)this.session.runtime.ui).openSourceVillagePetBankFromWorld(this, this.session.world.worldEventActor);
            } else {
               this.openSourcePetBankReleasePetstate();
            }
         } else {
            List var10000 = this.session.story.trace();
            String var10001 = String.valueOf(var1.outcome);
            var10000.add("PORTED/PARTIAL panel game.h.C state15 release ignored outcome=" + var10001 + " selectedBankPet=" + this.battleMenuIndex + " bank=" + this.session.pets.bank.size());
            this.openSourcePetBankReleasePetstate();
         }
      }

      private void tickSourcePetBankDepositMessage() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               this.sourcePetBankDepositMessageMode = 0;
               this.session.story.trace().add("PORTED panel game.h.C state7 close msgwarm.ui f=1->0 stay petstate.ui selectedPet=" + this.battleMenuIndex);
            }
         }

      }

      private void confirmPanelBagSpecialUse() {
         int var1 = this.panelBagSpecialUseId;
         PetItemService.SpecialItemResult var2 = this.petItems.useSpecialItem(this.session.pets.roster, this.battleMenuIndex, this.session.inventory.specialRewards, var1);
         if (var2.outcome == PetItemService.SpecialItemOutcome.INVALID_SELECTION) {
            this.beginPanelBagSpecialUseWarning("Không có sủng vật", 2, "invalid selectedPet=" + this.battleMenuIndex);
         } else if (var2.outcome == PetItemService.SpecialItemOutcome.LEVEL_TOO_LOW) {
            this.beginPanelBagSpecialUseWarning("Chỉ có thể cho 50 cấp sủng vật sử dụng", 2, "level=" + var2.petLevel + " selectedPet=" + this.battleMenuIndex);
         } else if (var2.outcome == PetItemService.SpecialItemOutcome.MISSING_REWARD) {
            this.beginPanelBagSpecialUseWarning("Không có đủ đạo cụ", 2, "missing stack specialId=" + var1);
         } else {
            this.rebuildWorldPetstateRows();
            this.text = TextBox.msgWarm("Sử dụng thành công", "Nhấn nút 5 để tiếp tục");
            this.panelBagSpecialUseMessageMode = 1;
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.ab state19 success q.e(s,b)=true game.b.i(" + var1 + ") selectedPet=" + this.battleMenuIndex + " level=" + var2.petLevel + " specialUse=" + var2.specialUseBefore + "->" + var2.specialUseAfter + " stack=" + var2.stackBefore + "->" + var2.stackAfter + " msgwarm.ui f=1");
         }
      }

      private void beginPanelBagSpecialUseWarning(String var1, int var2, String var3) {
         this.text = TextBox.msgWarm(var1, "Nhấn nút 5 để tiếp tục");
         this.panelBagSpecialUseMessageMode = var2;
         this.session.story.trace().add("PORTED/PARTIAL panel game.h.ab state19 msgwarm.ui f=" + var2 + " specialId=" + this.panelBagSpecialUseId + " reason=" + var3 + " message=" + var1);
      }

      private void tickPanelBagSpecialUseMessage() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               if (this.panelBagSpecialUseMessageMode == 1) {
                  int var1 = this.panelBagSpecialUseId;
                  this.panelBagSpecialUseId = -1;
                  this.panelBagSpecialUseMessageMode = 0;
                  this.closeWorldPetstateDetailUi();
                  ((VqsvPanelRuntime)this.session.runtime.ui).returnToBagFromSpecialUseBack(this, var1);
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.ab state19 close msgwarm.ui+petstate.ui f=1->0 return state8/bag.ui specialId=" + var1 + " selectedPet=" + this.battleMenuIndex);
               } else {
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.ab state19 close msgwarm.ui f=2->0 stay petstate.ui specialId=" + this.panelBagSpecialUseId + " selectedPet=" + this.battleMenuIndex);
                  this.panelBagSpecialUseMessageMode = 0;
               }
            }
         }

      }

      private void confirmPanelBagState17() {
         int var1 = this.panelBagState17ItemId;
         BattleItemRow var2 = VqsvBattleTables.instance().item(var1);
         int var3 = var2 == null ? -1 : var2.behavior;
         BattleUnit var4 = this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.roster.size() ? PetBattleAdapter.toBattleUnit((PetState)this.session.pets.roster.get(this.battleMenuIndex), (byte)0, this.session.progression.badges) : null;
         PetItemService.BattleItemResult var5 = this.petItems.useBattleItem(battleItemTarget(var4), this.session.inventory.bagItems, var1, var3, false);
         if (var5.outcome == PetItemService.BattleItemOutcome.NO_TARGET) {
            this.beginPanelBagState17Warning("Sủng vật này không có, không thể sử dụng", 1, "invalid selectedPet=" + this.battleMenuIndex);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.TARGET_REJECTED) {
            this.beginPanelBagState17Warning(sourceItemChoiceWarning(var5.validation), 1, "game.b.x itemId=" + var1 + " validation=" + var5.validation + " selectedPet=" + this.battleMenuIndex);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.MISSING_ITEM) {
            this.beginPanelBagState17Warning("Đã không có đạo này cụ, thỉnh mua sắm", 2, "q.b itemId=" + var1 + " qty=1 false selectedPet=" + this.battleMenuIndex);
         } else if (var5.outcome != PetItemService.BattleItemOutcome.INVALID_ITEM && var5.outcome != PetItemService.BattleItemOutcome.FORBIDDEN && var5.outcome != PetItemService.BattleItemOutcome.UNSUPPORTED_BEHAVIOR) {
            BattlePetStateAdapter.persist((PetState)this.session.pets.roster.get(this.battleMenuIndex), var4);
            this.openPanelBagState17Petstate(var1);
            this.text = TextBox.msgWarm("Thành công sử dụng đạo cụ", "Nhấn nút 5 để tiếp tục");
            this.panelBagState17MessageMode = 1;
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.Z->bo state17 success game.b.w itemId=" + var1 + " behavior=" + var3 + " selectedPet=" + this.battleMenuIndex + " hp=" + var5.effect.hpBefore + "->" + var5.effect.hpAfter + " pp=" + var5.effect.ppBefore + "->" + var5.effect.ppAfter + " debuffs=" + var5.effect.debuffsBefore + "->" + var5.effect.debuffsAfter + " state6=" + var5.effect.sourceStateFlag + " remaining=" + var5.inventoryAfter + " e(c) refresh petstate.ui msgwarm.ui f=1");
         } else {
            this.beginPanelBagState17Warning("Đạo cụ này không thể sử dụng", 1, "unsupported behavior=" + var3 + " itemId=" + var1);
         }
      }

      private void beginPanelBagState17Warning(String var1, int var2, String var3) {
         this.text = TextBox.msgWarm(var1, "Nhấn nút 5 để tiếp tục");
         this.panelBagState17MessageMode = var2;
         this.session.story.trace().add("PORTED/PARTIAL panel game.h.Z->bo state17 msgwarm.ui f=" + var2 + " reason=" + var3 + " message=" + var1);
      }

      private void tickPanelBagState17Message() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               int var1 = this.panelBagState17MessageMode;
               if (var1 == 2) {
                  int var2 = this.panelBagState17ItemId;
                  this.panelBagState17ItemId = -1;
                  this.panelBagState17MessageMode = 0;
                  this.closeWorldPetstateDetailUi();
                  ((VqsvPanelRuntime)this.session.runtime.ui).returnToBagFromState17Back(this, var2);
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.Z->bo state17 close msgwarm.ui+petstate.ui f=2->0 return state8/bag.ui itemId=" + var2 + " selectedPet=" + this.battleMenuIndex);
               } else {
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.Z->bo state17 close msgwarm.ui f=1->0 stay petstate.ui itemId=" + this.panelBagState17ItemId + " selectedPet=" + this.battleMenuIndex);
                  this.panelBagState17MessageMode = 0;
               }
            }
         }

      }

      private void openSourcePetSettingFromPetstate() {
         EvolutionCandidate var1 = VqsvSourceEvolutionRuntime.noticeForPet(this, this.battleMenuIndex);
         this.sourcePetSettingCount = var1 != null && var1.available() ? 8 : 7;
         this.sourcePetSettingIndex = Math.max(0, Math.min(this.sourcePetSettingIndex, this.sourcePetSettingCount - 1));
         this.keepSourcePetSettingSelectionVisible();
         this.sourcePetSettingVisible = true;
         List var10000 = this.session.story.trace();
         int var10001 = this.sourcePetSettingIndex;
         var10000.add("PORTED/PARTIAL panel game.h.X petstate confirm -> petsetting.ui f=1 c=" + var10001 + " rows=" + this.sourcePetSettingCount + " evolutionKind=" + String.valueOf(var1 == null ? EvolutionCandidate.Kind.NONE : var1.kind) + " selectedPet=" + this.battleMenuIndex);
      }

      private void tickSourcePetSetting() {
         if (this.keyUp && this.sourcePetSettingIndex > 0) {
            --this.sourcePetSettingIndex;
            this.keepSourcePetSettingSelectionVisible();
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X petsetting key=4100 c=" + this.sourcePetSettingIndex);
         } else if (this.keyDown && this.sourcePetSettingIndex < this.sourcePetSettingCount - 1) {
            ++this.sourcePetSettingIndex;
            this.keepSourcePetSettingSelectionVisible();
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X petsetting key=8448 c=" + this.sourcePetSettingIndex);
         } else if (this.keyBack) {
            this.sourcePetSettingVisible = false;
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X back close petsetting.ui -> petstate.ui c=" + this.sourcePetSettingIndex);
         } else if (this.key0) {
            if (this.sourcePetSettingIndex == this.sourcePetSettingCount - 1) {
               boolean wasSelected = WorldPetCompanionRuntime.selected(this, this.battleMenuIndex);
               WorldPetCompanionRuntime.toggleSelected(this, this.battleMenuIndex);
               this.sourcePetSettingVisible = false;
               this.session.story.trace().add("PC_QOL pet companion " + (wasSelected ? "disabled" : "enabled") + " rosterIndex=" + this.battleMenuIndex + " slot=" + this.session.pets.companionPetSlot);
               return;
            }

            if (this.sourcePetSettingIndex == 0) {
               this.openSourceItemChoiceFromPetSetting();
               return;
            }

            if (this.sourcePetSettingIndex == 1) {
               this.confirmSourcePetSettingActivePet();
               return;
            }

            if (this.sourcePetSettingIndex == 2) {
               this.openSourceEquipmentChoiceFromPetSetting();
               return;
            }

            if (this.sourcePetSettingIndex == 3) {
               this.openSourceReleaseConfirmFromPetSetting();
               return;
            }

            if (this.sourcePetSettingIndex == 4) {
               this.openSourceSkillUiFromPetSetting();
               return;
            }

            if (this.sourcePetSettingIndex == 5) {
               this.openQualityUpgradeFromPetSetting();
               return;
            }

            if (this.sourcePetSettingIndex == 6 && this.sourcePetSettingCount == 8) {
               this.openSourceEvolveUiFromPetSetting();
               return;
            }

            List var10000 = this.session.story.trace();
            int var10001 = this.sourcePetSettingIndex;
            var10000.add("VERIFIED defensive-inert panel game.h.X petsetting confirm c=" + var10001 + " action=" + this.sourcePetSettingActionLabel(this.sourcePetSettingIndex) + " unreachable outside validated row count");
         }

      }

      private void openQualityUpgradeFromPetSetting() {
         if (this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.roster.size()) {
            this.qualityUpgradeMain = (PetState)this.session.pets.roster.get(this.battleMenuIndex);
            this.qualityUpgradeFodders[0] = null;
            this.qualityUpgradeFodders[1] = null;
            this.qualityUpgradeFocus = 0;
            this.qualityUpgradePhase = 0;
            this.qualityUpgradeTicks = 0;
            this.qualityUpgradeRateBasisPoints = 0;
            this.qualityUpgradeStatus = this.qualityUpgradeMain.quality >= 5 ? "Pet đã đạt phẩm tối đa" : "Chọn 1 hoặc 2 Pet phôi";
            this.qualityUpgradeResult = null;
            this.qualityUpgradePickerVisible = false;
            this.qualityUpgradeVisible = true;
            this.sourcePetSettingVisible = false;
            this.closeWorldPetstateDetailUi();
            this.session.story.trace().add("UNIFIED_DESIGN quality-upgrade open main=" + this.qualityUpgradeMain.speciesId + " quality=" + this.qualityUpgradeMain.quality);
         }
      }

      private void tickQualityUpgrade() {
         if (this.qualityUpgradePhase == 1) {
            ++this.qualityUpgradeTicks;
            if (this.qualityUpgradeTicks < 4) {
               this.qualityUpgradeStatus = "Đang khóa thẻ nâng phẩm...";
            } else if (this.qualityUpgradeTicks < 10) {
               this.qualityUpgradeStatus = "Lõi năng lượng đang nạp...";
            } else {
               this.qualityUpgradeStatus = "Đang truyền phẩm chất...";
            }

            if (this.qualityUpgradeTicks >= PetQualityUpgradeAssets.CHARGE_COMMIT_TICK) {
               this.commitQualityUpgrade();
            }

         } else if (this.qualityUpgradePhase != 2) {
            if (this.qualityUpgradePickerVisible) {
               this.tickQualityUpgradePicker();
            } else {
               if (this.keyLeft) {
                  this.qualityUpgradeFocus = (this.qualityUpgradeFocus + 2) % 3;
               } else if (this.keyRight) {
                  this.qualityUpgradeFocus = (this.qualityUpgradeFocus + 1) % 3;
               } else if (this.keyBack) {
                  this.closeQualityUpgradeToPetstate();
               } else if (this.key0) {
                  if (this.qualityUpgradeFocus < 2) {
                     this.openQualityUpgradePicker(this.qualityUpgradeFocus);
                  } else {
                     this.startQualityUpgradeAttempt();
                  }
               }

            }
         } else {
            ++this.qualityUpgradeTicks;
            int var1 = PetQualityUpgradeAssets.resultDurationTicks(this.qualityUpgradeResult != null && this.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS);
            if (this.qualityUpgradeTicks >= var1 && this.qualityUpgradeResult != null) {
               this.qualityUpgradeStatus = this.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.SUCCESS ? "Nâng phẩm thành công!" : "Nâng phẩm thất bại";
            }

            if (this.key0 && this.qualityUpgradeTicks >= var1) {
               this.closeQualityUpgradeToPetstate();
            }

         }
      }

      private void openQualityUpgradePicker(int var1) {
         this.qualityUpgradeFocus = Math.max(0, Math.min(1, var1));
         this.rebuildQualityUpgradeCandidates();
         this.qualityUpgradePickerIndex = 0;
         this.qualityUpgradePickerScroll = 0;
         this.qualityUpgradePickerVisible = true;
         this.qualityUpgradeStatus = this.qualityUpgradeCandidates.isEmpty() ? "Không có Pet phôi phù hợp" : "Chọn Pet phôi";
      }

      private void tickQualityUpgradePicker() {
         int var1 = this.qualityUpgradeCandidates.size();
         if (this.keyBack) {
            this.qualityUpgradePickerVisible = false;
            this.updateQualityUpgradeRate();
         } else if (var1 != 0) {
            if (this.keyUp) {
               this.qualityUpgradePickerIndex = (this.qualityUpgradePickerIndex + var1 - 1) % var1;
            } else if (this.keyDown) {
               this.qualityUpgradePickerIndex = (this.qualityUpgradePickerIndex + 1) % var1;
            } else if (this.key0) {
               this.qualityUpgradeFodders[this.qualityUpgradeFocus] = (PetQualityUpgradeService.PetReference)this.qualityUpgradeCandidates.get(this.qualityUpgradePickerIndex);
               this.qualityUpgradePickerVisible = false;
               this.updateQualityUpgradeRate();
               return;
            }

            if (this.qualityUpgradePickerIndex < this.qualityUpgradePickerScroll) {
               this.qualityUpgradePickerScroll = this.qualityUpgradePickerIndex;
            } else if (this.qualityUpgradePickerIndex >= this.qualityUpgradePickerScroll + 5) {
               this.qualityUpgradePickerScroll = this.qualityUpgradePickerIndex - 4;
            }

         }
      }

      private void rebuildQualityUpgradeCandidates() {
         this.qualityUpgradeCandidates.clear();
         if (this.qualityUpgradeMain != null && this.qualityUpgradeMain.quality < 5) {
            for(int var1 = 1; var1 < this.session.pets.roster.size(); ++var1) {
               this.addQualityUpgradeCandidate(new PetQualityUpgradeService.PetReference(PetQualityUpgradeService.Location.ROSTER, (PetState)this.session.pets.roster.get(var1)));
            }

            for(PetState var2 : this.session.pets.bank) {
               this.addQualityUpgradeCandidate(new PetQualityUpgradeService.PetReference(PetQualityUpgradeService.Location.BANK, var2));
            }

         }
      }

      private void addQualityUpgradeCandidate(PetQualityUpgradeService.PetReference var1) {
         ArrayList var2 = new ArrayList();
         int var3 = this.qualityUpgradeFocus == 0 ? 1 : 0;
         PetQualityUpgradeService.PetReference var4 = this.qualityUpgradeFodders[var3];
         if (var4 != null) {
            var2.add(var4);
         }

         var2.add(var1);
         PetQualityUpgradeService.Validation var5 = this.petQualityUpgrade.validate(this.session.pets.roster, this.session.pets.bank, this.qualityUpgradeMain, var2, this::sourceReleaseProtectedPet);
         if (var5.valid) {
            this.qualityUpgradeCandidates.add(var1);
         }

      }

      private List<PetQualityUpgradeService.PetReference> selectedQualityUpgradeFodders() {
         ArrayList var1 = new ArrayList();
         if (this.qualityUpgradeFodders[0] != null) {
            var1.add(this.qualityUpgradeFodders[0]);
         }

         if (this.qualityUpgradeFodders[1] != null) {
            var1.add(this.qualityUpgradeFodders[1]);
         }

         return var1;
      }

      private void updateQualityUpgradeRate() {
         List var1 = this.selectedQualityUpgradeFodders();
         PetQualityUpgradeService.Validation var2 = this.petQualityUpgrade.validate(this.session.pets.roster, this.session.pets.bank, this.qualityUpgradeMain, var1, this::sourceReleaseProtectedPet);
         this.qualityUpgradeRateBasisPoints = var2.valid ? var2.rateBasisPoints : 0;
         this.qualityUpgradeStatus = var2.valid ? "Sẵn sàng nâng phẩm" : qualityUpgradeReason(var2.reason);
      }

      int qualityUpgradeCandidateRateBasisPoints(PetQualityUpgradeService.PetReference var1) {
         return var1 == null ? 0 : this.petQualityUpgrade.contributionBasisPoints(this.qualityUpgradeMain, var1.pet);
      }

      int qualityUpgradeCandidateFormDistance(PetQualityUpgradeService.PetReference var1) {
         return var1 == null ? -1 : this.petQualityUpgrade.formDistance(this.qualityUpgradeMain, var1.pet);
      }

      private void startQualityUpgradeAttempt() {
         List var1 = this.selectedQualityUpgradeFodders();
         PetQualityUpgradeService.Validation var2 = this.petQualityUpgrade.validate(this.session.pets.roster, this.session.pets.bank, this.qualityUpgradeMain, var1, this::sourceReleaseProtectedPet);
         if (!var2.valid) {
            this.qualityUpgradeRateBasisPoints = 0;
            this.qualityUpgradeStatus = qualityUpgradeReason(var2.reason);
         } else {
            this.qualityUpgradeRateBasisPoints = var2.rateBasisPoints;
            this.qualityUpgradeStatus = "Đang khóa thẻ nâng phẩm...";
            this.qualityUpgradePhase = 1;
            this.qualityUpgradeTicks = 0;
         }
      }

      private void commitQualityUpgrade() {
         int var1 = this.qualityUpgradeRandom.nextIntBounded("unified.quality-upgrade", 10000, this.session.story.trace());
         this.qualityUpgradeResult = this.petQualityUpgrade.attempt(this.session.pets.roster, this.session.pets.bank, this.session.inventory.equipmentItems, this.qualityUpgradeMain, this.selectedQualityUpgradeFodders(), this::sourceReleaseProtectedPet, var1);
         if (this.qualityUpgradeResult.outcome == PetQualityUpgradeService.Outcome.INVALID) {
            this.qualityUpgradePhase = 0;
            this.qualityUpgradeTicks = 0;
            this.qualityUpgradeStatus = qualityUpgradeReason(this.qualityUpgradeResult.invalidReason);
         } else {
            this.qualityUpgradePhase = 2;
            this.qualityUpgradeTicks = 0;
            this.qualityUpgradeStatus = "Đang mở khóa phẩm mới...";
            List var10000 = this.session.story.trace();
            String var10001 = String.valueOf(this.qualityUpgradeResult.outcome);
            var10000.add("UNIFIED_DESIGN quality-upgrade result=" + var10001 + " rate=" + this.qualityUpgradeResult.rateBasisPoints + " roll=" + this.qualityUpgradeResult.roll + " quality=" + this.qualityUpgradeResult.qualityBefore + "->" + this.qualityUpgradeResult.qualityAfter + " consumed=" + this.qualityUpgradeResult.consumedCount);
         }
      }

      private void closeQualityUpgradeToPetstate() {
         PetState var1 = this.qualityUpgradeMain;
         this.qualityUpgradeVisible = false;
         this.qualityUpgradePickerVisible = false;
         this.qualityUpgradePhase = 0;
         this.qualityUpgradeTicks = 0;
         int var2 = 0;

         for(int var3 = 0; var3 < this.session.pets.roster.size(); ++var3) {
            if (this.session.pets.roster.get(var3) == var1) {
               var2 = var3;
               break;
            }
         }

         this.battleMenuIndex = var2;
         this.openWorldPetstate();
         this.battleMenuIndex = var2;
         this.keepWorldPetstateSelectionVisible();
         this.rebuildWorldPetstateRows();
      }

      private static String qualityUpgradeReason(PetQualityUpgradeService.InvalidReason var0) {
         if (var0 == null) {
            return "Không thể nâng phẩm";
         } else {
            switch (var0) {
               case ALREADY_MAX_QUALITY -> {
                  return "Pet đã đạt phẩm tối đa";
               }
               case NO_FODDER -> {
                  return "Hãy chọn ít nhất 1 Pet phôi";
               }
               case ACTIVE_PET -> {
                  return "Pet đang xuất chiến không thể làm phôi";
               }
               case PROTECTED_PET -> {
                  return "Pet bảo hộ không thể làm phôi";
               }
               case DIFFERENT_SPECIES_OR_FORM -> {
                  return "Chỉ dùng cùng form hoặc form tiến hóa trước";
               }
               case FODDER_QUALITY_TOO_HIGH -> {
                  return "Sao phôi không được cao hơn Pet chính";
               }
               case DUPLICATE_FODDER -> {
                  return "Hai ô không thể dùng cùng một Pet";
               }
               case FODDER_QUALITY_MISMATCH -> {
                  return "Hai Pet phôi phải cùng số sao";
               }
               default -> {
                  return "Không thể nâng phẩm với lựa chọn này";
               }
            }
         }
      }

      private void clickQualityUpgrade(int var1, int var2) {
         if (this.qualityUpgradePhase != 1) {
            if (this.qualityUpgradePhase == 2) {
               if (sourceLeftSoftkeyHit(var1, var2)) {
                  this.key0 = true;
               }

            } else if (sourceRightSoftkeyHit(var1, var2)) {
               this.keyBack = true;
            } else if (!this.qualityUpgradePickerVisible && sourceLeftSoftkeyHit(var1, var2)) {
               if (this.qualityUpgradeFocus == 2) {
                  this.qualityUpgradeFocus = 0;
               }

               this.key0 = true;
            } else if (this.qualityUpgradePickerVisible) {
               int var3 = this.qualityUpgradeCandidateIndexAt(var1, var2);
               if (var3 >= 0) {
                  this.qualityUpgradePickerIndex = var3;
                  this.key0 = true;
               }

            } else {
               if (var1 >= 8 && var1 <= 68 && var2 >= 74 && var2 <= 180) {
                  this.qualityUpgradeFocus = 0;
                  this.key0 = true;
               } else if (var1 >= 172 && var1 <= 232 && var2 >= 74 && var2 <= 180) {
                  this.qualityUpgradeFocus = 1;
                  this.key0 = true;
               } else if (var1 >= 70 && var1 <= 170 && var2 >= 249 && var2 <= 281) {
                  this.qualityUpgradeFocus = 2;
                  this.key0 = true;
               }

            }
         }
      }

      private void hoverQualityUpgrade(int var1, int var2) {
         if (this.qualityUpgradePhase == 0) {
            if (this.qualityUpgradePickerVisible) {
               int var3 = this.qualityUpgradeCandidateIndexAt(var1, var2);
               if (var3 >= 0) {
                  this.qualityUpgradePickerIndex = var3;
               }

            } else {
               if (var1 >= 8 && var1 <= 68 && var2 >= 74 && var2 <= 180) {
                  this.qualityUpgradeFocus = 0;
               } else if (var1 >= 172 && var1 <= 232 && var2 >= 74 && var2 <= 180) {
                  this.qualityUpgradeFocus = 1;
               } else if (var1 >= 70 && var1 <= 170 && var2 >= 249 && var2 <= 281) {
                  this.qualityUpgradeFocus = 2;
               }

            }
         }
      }

      private int qualityUpgradeCandidateIndexAt(int var1, int var2) {
         if (var1 >= 12 && var1 <= 228 && var2 >= 82 && var2 < 252) {
            int var3 = (var2 - 82) / 34;
            int var4 = this.qualityUpgradePickerScroll + var3;
            return var3 < 5 && var4 < this.qualityUpgradeCandidates.size() ? var4 : -1;
         } else {
            return -1;
         }
      }

      private void openSourceEvolveUiFromPetSetting() {
         EvolutionCandidate var1 = VqsvSourceEvolutionRuntime.noticeForPet(this, this.battleMenuIndex);
         if (var1 != null && var1.available()) {
            this.sourcePetSettingVisible = false;
            this.closeWorldPetstateDetailUi();
            List var10000 = this.session.story.trace();
            String var10001 = this.sourcePetSettingEvolutionLabel();
            var10000.add("PORTED/PARTIAL panel game.h.X petsetting c=6 o.m(); bg(); label=" + var10001 + " selectedPet=" + this.battleMenuIndex + " species=" + var1.currentSpeciesId + " target=" + var1.targetSpeciesId + " kind=" + String.valueOf(var1.kind));
            this.openSourceEvolveUi(this.battleMenuIndex);
         } else {
            this.session.story.trace().add("VERIFIED defensive-inert panel game.h.X petsetting c=6 ignored no source R selectedPet=" + this.battleMenuIndex + " rows=" + this.sourcePetSettingCount);
         }
      }

      private void confirmSourcePetSettingActivePet() {
         PetManagementService.ActivePetResult var1 = this.petManagement.selectActivePet(this.session.pets.roster, this.battleMenuIndex, this::sourcePetLiving);
         if (var1.outcome == PetManagementService.ActivePetOutcome.INVALID_SELECTION) {
            this.sourcePetSettingVisible = false;
            this.openWorldPetstate();
            List var10000 = this.session.story.trace();
            int var10001 = this.battleMenuIndex;
            var10000.add("PORTED/PARTIAL panel game.h.X petsetting c=1 ignored invalid selectedPet=" + var10001 + " pets=" + this.session.pets.roster.size());
         } else if (var1.outcome == PetManagementService.ActivePetOutcome.NOT_LIVING) {
            this.sourcePetSettingVisible = false;
            this.sourcePetSettingActiveWarningMode = 1;
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.openWorldPetstate();
            this.text = TextBox.msgWarm("Sủng vật này không thể tham chiến", "Nhấn nút 5 để tiếp tục");
            this.session.story.trace().add("PORTED panel game.h.X petsetting c=1 !q.z[b].S() -> msgwarm.ui f=2 close petsetting.ui b=0 selectedPetDead=true");
         } else if (var1.outcome == PetManagementService.ActivePetOutcome.ALREADY_ACTIVE) {
            this.sourcePetSettingVisible = false;
            this.sourcePetSettingActiveWarningMode = 2;
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.openWorldPetstate();
            this.text = TextBox.msgWarm("Sủng vật này đã xuất chiến", "Nhấn nút 5 để tiếp tục");
            this.session.story.trace().add("PORTED panel game.h.X petsetting c=1 b==0 -> msgwarm.ui f=2 close petsetting.ui already deployed");
         } else {
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.sourcePetSettingIndex = 0;
            this.sourcePetSettingVisible = false;
            this.openWorldPetstate();
            this.session.story.trace().add("PORTED panel game.h.X petsetting c=1 game.g.p move selected to front selectedIndex=" + var1.selectedIndexBefore + " species=" + var1.speciesId + " f=0 b=0 refresh petstate.ui close petsetting.ui");
         }
      }

      private void tickSourcePetSettingActiveWarningMessage() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               int var1 = this.sourcePetSettingActiveWarningMode;
               this.sourcePetSettingActiveWarningMode = 0;
               this.openWorldPetstate();
               this.session.story.trace().add("PORTED panel game.h.X petsetting c=1 msgwarm key=131104 close msgwarm.ui f=2->0 mode=" + var1 + " return petstate.ui selectedPet=" + this.battleMenuIndex);
            }
         }

      }

      private void openSourceReleaseConfirmFromPetSetting() {
         PetManagementService.ReleasePreparation var1 = this.petManagement.prepareRelease(this.session.pets.roster, this.battleMenuIndex, this::sourceReleaseProtectedPet);
         if (var1.outcome == PetManagementService.ReleasePreparationOutcome.INVALID_SELECTION) {
            this.sourcePetSettingVisible = false;
            this.openWorldPetstate();
            List var10000 = this.session.story.trace();
            int var10001 = this.battleMenuIndex;
            var10000.add("PORTED/PARTIAL panel game.h.X petsetting c=3 release ignored invalid selectedPet=" + var10001 + " pets=" + this.session.pets.roster.size());
         } else if (var1.outcome == PetManagementService.ReleasePreparationOutcome.PROTECTED) {
            this.sourcePetSettingVisible = false;
            this.sourceReleaseConfirmVisible = false;
            this.sourceReleaseWarningMode = 2;
            this.text = TextBox.msgWarm("Thần thú không thể phóng sinh", "Nhấn nút 5 để tiếp tục");
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X petsetting c=3 protected aq.c[0][species][22]==2 -> msgwarm.ui f=3 close petsetting.ui selectedPet=" + this.battleMenuIndex + " species=" + var1.speciesId);
         } else {
            this.sourceReleaseConfirmVisible = true;
            this.sourceReleaseConfirmMessage = "Bạn muốn phóng sinh sủng vật này?";
            this.sourceReleaseConfirmAction = "Xác nhận";
            this.sourcePetSettingVisible = false;
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X petsetting c=3 -> msgconfirm.ui f=2 close petsetting.ui message=Ban muon phong sinh sung vat nay? selectedPet=" + this.battleMenuIndex + " mutation=confirm-path-ported");
         }
      }

      private void tickSourceReleaseConfirm() {
         if (this.keyBack) {
            this.sourceReleaseConfirmVisible = false;
            if (this.sourcePetBankReleaseConfirmMode) {
               this.sourcePetBankReleaseConfirmMode = false;
               this.openSourcePetBankReleasePetstate();
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.C state15 release msgconfirm key=786432 close msgconfirm.ui f=1->0 return bank petstate.ui selectedBankPet=" + this.battleMenuIndex);
            } else {
               this.openWorldPetstate();
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.X release msgconfirm key=786432 close msgconfirm.ui f=2->0 return petstate.ui selectedPet=" + this.battleMenuIndex);
            }
         } else if (this.key0) {
            if (this.sourcePetBankReleaseConfirmMode) {
               this.confirmSourcePetBankRelease();
            } else {
               this.confirmSourceReleasePet();
            }
         }

      }

      private void confirmSourceReleasePet() {
         PetManagementService.ReleaseResult var1 = this.petManagement.confirmRelease(this.session.pets.roster, this.session.inventory.equipmentItems, this.battleMenuIndex, this::sourceReleaseProtectedPet, this::sourcePetLiving);
         if (var1.outcome == PetManagementService.ReleaseOutcome.INVALID_SELECTION) {
            this.sourceReleaseConfirmVisible = false;
            this.openWorldPetstate();
            List var3 = this.session.story.trace();
            int var5 = this.battleMenuIndex;
            var3.add("PORTED/PARTIAL panel game.h.X release msgconfirm key=131072 ignored invalid selectedPet=" + var5 + " pets=" + this.session.pets.roster.size());
         } else if (var1.outcome == PetManagementService.ReleaseOutcome.PROTECTED) {
            this.sourceReleaseConfirmVisible = false;
            this.sourceReleaseWarningMode = 2;
            this.text = TextBox.msgWarm("Thần thú không thể phóng sinh", "Nhấn nút 5 để tiếp tục");
            this.session.story.trace().add("PORTED panel game.h.X release confirm revalidated protected selectedPet=" + this.battleMenuIndex + " species=" + var1.removedSpeciesId + " -> msgwarm.ui f=3");
         } else if (var1.outcome == PetManagementService.ReleaseOutcome.LAST_LIVING) {
            this.sourceReleaseConfirmVisible = false;
            this.sourceReleaseWarningMode = 1;
            this.text = TextBox.msgWarm("Ba lô phải lưu ít nhất 1 sủng vật", "Nhấn nút 5 để tiếp tục");
            List var2 = this.session.story.trace();
            int var4 = this.battleMenuIndex;
            var2.add("PORTED/PARTIAL panel game.h.X release msgconfirm key=131072 q.o(selectedPet)=false -> msgwarm.ui f=3 close msgconfirm.ui selectedPet=" + var4 + " pets=" + this.session.pets.roster.size());
         } else {
            this.battleMenuIndex = var1.selectedIndexAfter;
            this.sourceReleaseConfirmVisible = false;
            this.openWorldPetstate();
            List var10000 = this.session.story.trace();
            int var10001 = var1.removedEquipmentId;
            var10000.add("PORTED panel game.h.X release msgconfirm key=131072 q.o(selectedPet)=true game.g.l equipmentId=" + var10001 + " game.g.m remove selected species=" + var1.removedSpeciesId + " refresh petstate.ui close msgconfirm.ui selectedPet=" + this.battleMenuIndex + " pets=" + this.session.pets.roster.size());
         }
      }

      private void tickSourceReleaseWarningMessage() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               int var1 = this.sourceReleaseWarningMode;
               this.sourceReleaseWarningMode = 0;
               this.openWorldPetstate();
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.X release msgwarm key=131104 close msgwarm.ui f=3->0 mode=" + var1 + " return petstate.ui selectedPet=" + this.battleMenuIndex);
            }
         }

      }

      private boolean sourcePetLiving(PetState var1) {
         if (var1 == null) {
            return false;
         } else {
            return PetSourceAdapter.ensureCurrentHp(var1) > 0;
         }
      }

      private boolean sourceReleaseProtectedPet(PetState var1) {
         if (var1 == null) {
            return false;
         } else {
            BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1.speciesId);
            return var2 != null && var2.releaseProtected();
         }
      }

      private void openSourceEquipmentChoiceFromPetSetting() {
         this.sourceEquipmentChoiceIndex = clampSourceChoiceIndex(this.sourceEquipmentChoiceIndex, this.sourceEquipmentChoiceSize());
         this.sourceEquipmentChoiceScroll = clampSourceChoiceScroll(this.sourceEquipmentChoiceScroll, this.sourceEquipmentChoiceIndex, this.sourceEquipmentChoiceSize());
         this.sourceEquipmentChoiceVisible = true;
         this.sourcePetSettingVisible = false;
         this.closeWorldPetstateDetailUi();
         List var10000 = this.session.story.trace();
         int var10001 = this.sourceEquipmentChoiceSize();
         var10000.add("PORTED/PARTIAL panel game.h.X petsetting c=2 -> choice.ui f=2 r=0 close petsetting.ui+petstate.ui title=Vat pham trang suc subtitle=Trang thai q.L-mapped rows=" + var10001 + " selectedPet=" + this.battleMenuIndex + " petEquipment=" + this.selectedSourceEquipmentId());
      }

      private void tickSourceEquipmentChoice() {
         int var1 = this.sourceEquipmentChoiceSize();
         if (this.keyUp && var1 > 0) {
            --this.sourceEquipmentChoiceIndex;
            if (this.sourceEquipmentChoiceIndex < 0) {
               this.sourceEquipmentChoiceIndex = var1 - 1;
               this.sourceEquipmentChoiceScroll = Math.max(0, var1 - 5);
            } else if (this.sourceEquipmentChoiceIndex < this.sourceEquipmentChoiceScroll) {
               this.sourceEquipmentChoiceScroll = this.sourceEquipmentChoiceIndex;
            }

            this.sourceEquipmentChoiceScroll = clampSourceChoiceScroll(this.sourceEquipmentChoiceScroll, this.sourceEquipmentChoiceIndex, var1);
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment key=4100 h=" + this.sourceEquipmentChoiceIndex + " w=" + this.sourceEquipmentChoiceScroll);
         } else if (this.keyDown && var1 > 0) {
            ++this.sourceEquipmentChoiceIndex;
            if (this.sourceEquipmentChoiceIndex >= var1) {
               this.sourceEquipmentChoiceIndex = 0;
               this.sourceEquipmentChoiceScroll = 0;
            } else if (this.sourceEquipmentChoiceIndex >= this.sourceEquipmentChoiceScroll + 5) {
               ++this.sourceEquipmentChoiceScroll;
            }

            this.sourceEquipmentChoiceScroll = clampSourceChoiceScroll(this.sourceEquipmentChoiceScroll, this.sourceEquipmentChoiceIndex, var1);
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment key=8448 h=" + this.sourceEquipmentChoiceIndex + " w=" + this.sourceEquipmentChoiceScroll);
         } else if (this.keyBack) {
            this.sourceEquipmentChoiceVisible = false;
            this.openWorldPetstate();
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment key=262144 e(b) refresh petstate.ui close choice.ui selectedPet=" + this.battleMenuIndex);
         } else if (this.key0) {
            this.confirmSourceEquipmentChoice();
         }

      }

      private void confirmSourceEquipmentChoice() {
         int var1 = this.sourceEquipmentChoiceItemIdAt(this.sourceEquipmentChoiceIndex);
         UnifiedItemRecord var2 = UnifiedItemCatalog.instance().byRuntime(UnifiedItemInventoryKind.EQUIPMENT, var1);
         if (var2 != null && !var2.mechanicsImplemented) {
            this.text = TextBox.msgWarm("Chức năng của vật phẩm này chưa được mở.", "Nhấn nút 5 để tiếp tục");
            this.sourceEquipmentChoiceMessageMode = 3;
            this.session.story.trace().add("UNIFIED_ITEM feature-not-open route=pet-equipment stableKey=" + var2.stableKey + " mechanic=" + var2.mechanicKey + " runtimeId=" + var2.runtimeId + " mutation=zero");
         } else {
            PetManagementService.EquipmentResult var3 = this.petManagement.selectEquipment(this.session.pets.roster, this.session.inventory.equipmentItems, this.battleMenuIndex, var1);
            if (var3.outcome == PetManagementService.EquipmentOutcome.INVALID_SELECTION) {
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment confirm ignored q.L emptyOrNoPet h=" + this.sourceEquipmentChoiceIndex + " selectedPet=" + this.battleMenuIndex);
            } else if (var3.outcome == PetManagementService.EquipmentOutcome.UNEQUIPPED) {
               this.text = TextBox.msgWarm("Thành công dỡ xuống", "Nhấn nút 5 để tiếp tục");
               this.sourceEquipmentChoiceMessageMode = 3;
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment success game.g.l itemId=" + var1 + " pet=" + this.battleMenuIndex + " message=Thanh cong do xuong f=3 keep choice.ui until confirm");
            } else {
               this.text = TextBox.msgWarm("Thành công mang theo", "Nhấn nút 5 để tiếp tục");
               this.sourceEquipmentChoiceMessageMode = 3;
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment success game.g.f itemId=" + var1 + " pet=" + this.battleMenuIndex + " oldEquipment=" + var3.previousEquipmentId + " previousPet=" + var3.previousPetIndex + " message=Thanh cong mang theo f=3 keep choice.ui until confirm");
            }
         }
      }

      private void tickSourceEquipmentChoiceMessage() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               this.sourceEquipmentChoiceVisible = false;
               this.openWorldPetstate();
               this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui equipment close msgwarm.ui f=3->2 refresh petstate.ui close choice.ui selectedPet=" + this.battleMenuIndex);
               this.sourceEquipmentChoiceMessageMode = 0;
            }
         }

      }

      private int sourcePetEquipmentId(PetState var1) {
         return var1 == null ? -1 : var1.heldEquipmentId;
      }

      private void openSourceItemChoiceFromPetSetting() {
         this.sourceItemChoiceIndex = clampSourceChoiceIndex(this.sourceItemChoiceIndex, this.sourceItemChoiceSize());
         this.sourceItemChoiceScroll = clampSourceChoiceScroll(this.sourceItemChoiceScroll, this.sourceItemChoiceIndex, this.sourceItemChoiceSize());
         this.sourceItemChoiceVisible = true;
         this.sourcePetSettingVisible = false;
         this.closeWorldPetstateDetailUi();
         List var10000 = this.session.story.trace();
         int var10001 = this.sourceItemChoiceSize();
         var10000.add("PORTED/PARTIAL panel game.h.X petsetting c=0 -> choice.ui f=2 r=0 close petsetting.ui+petstate.ui title=Dao cu subtitle=So luong q.J-mapped rows=" + var10001 + " selectedPet=" + this.battleMenuIndex);
      }

      private void tickSourceItemChoice() {
         int var1 = this.sourceItemChoiceSize();
         if (this.keyUp && var1 > 0) {
            --this.sourceItemChoiceIndex;
            if (this.sourceItemChoiceIndex < 0) {
               this.sourceItemChoiceIndex = var1 - 1;
               this.sourceItemChoiceScroll = Math.max(0, var1 - 5);
            } else if (this.sourceItemChoiceIndex < this.sourceItemChoiceScroll) {
               this.sourceItemChoiceScroll = this.sourceItemChoiceIndex;
            }

            this.sourceItemChoiceScroll = clampSourceChoiceScroll(this.sourceItemChoiceScroll, this.sourceItemChoiceIndex, var1);
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item key=4100 r=" + this.sourceItemChoiceIndex + " w=" + this.sourceItemChoiceScroll);
         } else if (this.keyDown && var1 > 0) {
            ++this.sourceItemChoiceIndex;
            if (this.sourceItemChoiceIndex >= var1) {
               this.sourceItemChoiceIndex = 0;
               this.sourceItemChoiceScroll = 0;
            } else if (this.sourceItemChoiceIndex >= this.sourceItemChoiceScroll + 5) {
               ++this.sourceItemChoiceScroll;
            }

            this.sourceItemChoiceScroll = clampSourceChoiceScroll(this.sourceItemChoiceScroll, this.sourceItemChoiceIndex, var1);
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item key=8448 r=" + this.sourceItemChoiceIndex + " w=" + this.sourceItemChoiceScroll);
         } else if (this.keyBack) {
            this.sourceItemChoiceVisible = false;
            this.openWorldPetstate();
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item key=262144 e(b) refresh petstate.ui close choice.ui selectedPet=" + this.battleMenuIndex);
         } else if (this.key0) {
            this.confirmSourceItemChoice();
         }

      }

      private void confirmSourceItemChoice() {
         int var1 = this.sourceItemChoiceItemIdAt(this.sourceItemChoiceIndex);
         BattleItemRow var2 = VqsvBattleTables.instance().item(var1);
         int var3 = var2 == null ? -1 : var2.behavior;
         BattleUnit var4 = this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.roster.size() ? PetBattleAdapter.toBattleUnit((PetState)this.session.pets.roster.get(this.battleMenuIndex), (byte)0, this.session.progression.badges) : null;
         PetItemService.BattleItemResult var5 = this.petItems.useBattleItem(battleItemTarget(var4), this.session.inventory.bagItems, var1, var3, var1 == 13 || var1 == 14);
         if (var5.outcome == PetItemService.BattleItemOutcome.INVALID_ITEM) {
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item confirm ignored q.J empty selectedPet=" + this.battleMenuIndex);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.FORBIDDEN) {
            this.beginSourceItemChoiceWarning("Đạo cụ này không thể sử dụng", 3, "source forbidden itemId=" + var1);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.NO_TARGET) {
            this.beginSourceItemChoiceWarning("Sủng vật này không có, không thể sử dụng", 3, "invalid selectedPet=" + this.battleMenuIndex);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.TARGET_REJECTED) {
            this.beginSourceItemChoiceWarning(sourceItemChoiceWarning(var5.validation), 3, "game.b.x itemId=" + var1 + " validation=" + var5.validation + " selectedPet=" + this.battleMenuIndex);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.MISSING_ITEM) {
            this.beginSourceItemChoiceWarning("Đã không có đạo này cụ, thỉnh mua sắm", 3, "missing-count itemId=" + var1);
         } else if (var5.outcome == PetItemService.BattleItemOutcome.UNSUPPORTED_BEHAVIOR) {
            this.beginSourceItemChoiceWarning("Đạo cụ này không thể sử dụng", 3, "unsupported behavior=" + var3 + " itemId=" + var1);
         } else {
            BattlePetStateAdapter.persist((PetState)this.session.pets.roster.get(this.battleMenuIndex), var4);
            this.sourceItemChoiceVisible = false;
            this.openWorldPetstate();
            this.text = TextBox.msgWarm("Thành công sử dụng đạo cụ", "Nhấn nút 5 để tiếp tục");
            this.sourceItemChoiceMessageMode = 4;
            this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item success game.b.w itemId=" + var1 + " behavior=" + var3 + " selectedPet=" + this.battleMenuIndex + " hp=" + var5.effect.hpBefore + "->" + var5.effect.hpAfter + " pp=" + var5.effect.ppBefore + "->" + var5.effect.ppAfter + " debuffs=" + var5.effect.debuffsBefore + "->" + var5.effect.debuffsAfter + " state6=" + var5.effect.sourceStateFlag + " remaining=" + var5.inventoryAfter + " close choice.ui refresh petstate.ui f=4");
         }
      }

      private void beginSourceItemChoiceWarning(String var1, int var2, String var3) {
         this.text = TextBox.msgWarm(var1, "Nhấn nút 5 để tiếp tục");
         this.sourceItemChoiceMessageMode = var2;
         this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item msgwarm f=" + var2 + " reason=" + var3 + " message=" + var1);
      }

      private void tickSourceItemChoiceMessage() {
         if (this.text != null && this.text.readyForKey && this.key0) {
            this.text.confirm();
            if (this.text.disposed) {
               this.text = null;
               if (this.sourceItemChoiceMessageMode == 3) {
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item close msgwarm.ui f=3->2 return choice.ui");
               } else if (this.sourceItemChoiceMessageMode == 4) {
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.X choice.ui item close msgwarm.ui f=4->0 stay petstate.ui");
               }

               this.sourceItemChoiceMessageMode = 0;
            }
         }

      }

      private static String sourceItemChoiceWarning(int var0) {
         switch (var0) {
            case 0:
               return "Sủng vật này đã tử vong, không thể sử dụng";
            case 1:
               return "Sủng vật này không có, không thể sử dụng";
            case 2:
               return "Máu đầy, không cần sử dụng";
            case 3:
               return "Kỹ năng giá trị đã đầy, không cần sử dụng";
            case 4:
               return "Trên người đều bị lợi hiệu quả";
            case 5:
               return "Trong hưng phấn, không thể dùng";
            case 6:
            default:
               return "Đạo cụ này không thể sử dụng";
            case 7:
               return "Máu và kỹ năng đều đã đầy, không cần sử dụng";
            case 8:
               return "Sủng vật đã chết, không thể sử dụng";
         }
      }

      private static PetItemService.BattleItemTarget battleItemTarget(BattleUnit var0) {
         if (var0 == null) return null;
         final BattleUnit target = var0;
         return new PetItemService.BattleItemTarget() {

            public int validateBattleItem(int var1) {
               return target.validateBattleItem(var1);
            }

            public PetItemService.BattleItemEffect applyBattleItem(int var1) {
               BattleItemUseResult var2 = target.applyBattleItem(var1);
               return new PetItemService.BattleItemEffect(var2.hpBefore, var2.hpAfter, var2.ppBefore, var2.ppAfter, var2.debuffsBefore, var2.debuffsAfter, var2.sourceStateFlag);
            }
         };
      }

      VqsvChoiceUiView sourceItemChoiceView() {
         ArrayList<String> var1 = new ArrayList<>();
         ArrayList var2 = new ArrayList();
         ArrayList var3 = new ArrayList();
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();
         ArrayList var6 = new ArrayList();

         for(BagItemState var8 : this.sourceItemChoiceRows()) {
            ItemDefinition var9 = VqsvSourceOps.sourceItem(var8.id);
            var1.add(var9.name);
            var2.add(String.valueOf(var8.count));
            var3.add(var9.description);
            var4.add(var8.id);
            var5.add(var9.iconCell);
            var6.add(var9.iconResource);
         }

         return VqsvChoiceUiView.battle("Đạo cụ", "Số lượng", "Sử dụng", var1, var2, var3, var4, var5, var6, this.sourceItemChoiceIndex, this.sourceItemChoiceScroll).withAlternateSoftkeys("Sử dụng").withSourceCursor(this.sourceItemChoiceIndex, this.sourceItemChoiceScroll);
      }

      int sourceItemChoiceSize() {
         return this.sourceItemChoiceRows().size();
      }

      int sourceItemChoiceItemIdAt(int var1) {
         List var2 = this.sourceItemChoiceRows();
         return var1 >= 0 && var1 < var2.size() ? ((BagItemState)var2.get(var1)).id : -1;
      }

      private List<BagItemState> sourceItemChoiceRows() {
         ArrayList<BagItemState> var1 = new ArrayList<>();

         for(BagItemState var3 : this.session.inventory.bagItems.values()) {
            if (var3.bagChannel != 0 && var3.count > 0) {
               var1.add(var3);
            }
         }

         var1.sort(Comparator.comparingInt((var0) -> var0.id));
         return var1;
      }

      VqsvChoiceUiView sourceEquipmentChoiceView() {
         ArrayList var1 = new ArrayList();
         ArrayList var2 = new ArrayList();
         ArrayList var3 = new ArrayList();
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();
         ArrayList var6 = new ArrayList();

         for(EquipmentState var8 : this.sourceEquipmentChoiceRows()) {
            ItemDefinition var9 = VqsvSourceOps.sourceEquipmentItem(var8.id);
            var1.add(var9.name);
            var2.add(this.sourceEquipmentStatusText(var8));
            var3.add(var9.description);
            var4.add(var8.id);
            var5.add(var9.iconCell);
            var6.add(var9.iconResource);
         }

         return VqsvChoiceUiView.battle("Vật phẩm trang sức", "Trạng thái", this.sourceEquipmentActionText(), var1, var2, var3, var4, var5, var6, this.sourceEquipmentChoiceIndex, this.sourceEquipmentChoiceScroll).withAlternateSoftkeys(this.sourceEquipmentActionText()).withSourceCursor(this.sourceEquipmentChoiceIndex, this.sourceEquipmentChoiceScroll);
      }

      int sourceEquipmentChoiceSize() {
         return this.sourceEquipmentChoiceRows().size();
      }

      int sourceEquipmentChoiceItemIdAt(int var1) {
         List var2 = this.sourceEquipmentChoiceRows();
         return var1 >= 0 && var1 < var2.size() ? ((EquipmentState)var2.get(var1)).id : -1;
      }

      private List<EquipmentState> sourceEquipmentChoiceRows() {
         return new ArrayList(this.session.inventory.equipmentItems);
      }

      private String sourceEquipmentStatusText(EquipmentState var1) {
         if (var1 == null) {
            return "";
         } else if (this.selectedSourceEquipmentId() == var1.id) {
            return "Đã mang theo";
         } else {
            return VqsvSourceOps.sourceEquipmentEquipped(var1) ? "Bị mang theo" : "";
         }
      }

      private String sourceEquipmentActionText() {
         int var1 = this.sourceEquipmentChoiceItemIdAt(this.sourceEquipmentChoiceIndex);
         return var1 >= 0 && this.selectedSourceEquipmentId() == var1 ? "Dỡ xuống" : "Mang theo";
      }

      int selectedSourceEquipmentId() {
         return this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.roster.size() ? this.sourcePetEquipmentId((PetState)this.session.pets.roster.get(this.battleMenuIndex)) : -1;
      }

      private static int clampSourceChoiceIndex(int var0, int var1) {
         return var1 <= 0 ? 0 : Math.max(0, Math.min(var1 - 1, var0));
      }

      private static int clampSourceChoiceScroll(int var0, int var1, int var2) {
         int var3 = Math.max(0, var2 - 5);
         int var4 = Math.max(0, Math.min(var3, var0));
         if (var1 < var4) {
            var4 = var1;
         } else if (var1 >= var4 + 5) {
            var4 = var1 - 4;
         }

         return Math.max(0, Math.min(var3, var4));
      }

      private void mouseWheelSourceItemChoice(int var1) {
         int var2 = this.sourceItemChoiceSize();
         if (var2 > 0) {
            int var3 = Math.max(0, var2 - 5);
            if (var3 <= 0) {
               int var6 = this.sourceItemChoiceIndex;
               this.sourceItemChoiceIndex = Math.max(0, Math.min(var2 - 1, this.sourceItemChoiceIndex + var1));
               if (this.sourceItemChoiceIndex != var6) {
                  this.session.story.trace().add("PC_QOL mouse wheel choice.ui item selection r=" + this.sourceItemChoiceIndex + " size=" + var2);
               }

            } else {
               int var4 = this.sourceItemChoiceScroll;
               int var5 = this.sourceItemChoiceIndex;
               this.sourceItemChoiceScroll = Math.max(0, Math.min(var3, this.sourceItemChoiceScroll + var1));
               this.sourceItemChoiceIndex = clampIndexIntoVisible(this.sourceItemChoiceIndex, this.sourceItemChoiceScroll, var2, 5);
               if (this.sourceItemChoiceScroll != var4 || this.sourceItemChoiceIndex != var5) {
                  this.session.story.trace().add("PC_QOL mouse wheel choice.ui item scrollbar r=" + this.sourceItemChoiceIndex + " w=" + this.sourceItemChoiceScroll);
               }

            }
         }
      }

      private void mouseWheelSourceEquipmentChoice(int var1) {
         int var2 = this.sourceEquipmentChoiceSize();
         if (var2 > 0) {
            int var3 = Math.max(0, var2 - 5);
            if (var3 <= 0) {
               int var6 = this.sourceEquipmentChoiceIndex;
               this.sourceEquipmentChoiceIndex = Math.max(0, Math.min(var2 - 1, this.sourceEquipmentChoiceIndex + var1));
               if (this.sourceEquipmentChoiceIndex != var6) {
                  this.session.story.trace().add("PC_QOL mouse wheel choice.ui equipment selection h=" + this.sourceEquipmentChoiceIndex + " size=" + var2);
               }

            } else {
               int var4 = this.sourceEquipmentChoiceScroll;
               int var5 = this.sourceEquipmentChoiceIndex;
               this.sourceEquipmentChoiceScroll = Math.max(0, Math.min(var3, this.sourceEquipmentChoiceScroll + var1));
               this.sourceEquipmentChoiceIndex = clampIndexIntoVisible(this.sourceEquipmentChoiceIndex, this.sourceEquipmentChoiceScroll, var2, 5);
               if (this.sourceEquipmentChoiceScroll != var4 || this.sourceEquipmentChoiceIndex != var5) {
                  this.session.story.trace().add("PC_QOL mouse wheel choice.ui equipment scrollbar h=" + this.sourceEquipmentChoiceIndex + " w=" + this.sourceEquipmentChoiceScroll);
               }

            }
         }
      }

      private void mouseWheelSourceSkill(int var1) {
         if (this.sourceSkillCount > 0) {
            if (this.sourceSkillCount <= 4) {
               int var5 = this.sourceSkillIndex;
               this.sourceSkillIndex = Math.max(0, Math.min(this.sourceSkillCount - 1, this.sourceSkillIndex + var1));
               if (this.sourceSkillIndex != var5) {
                  this.session.story.trace().add("PC_QOL mouse wheel skill.ui selection index=" + this.sourceSkillIndex + " rows=" + this.sourceSkillCount);
               }

            } else {
               int var2 = this.sourceSkillScroll;
               int var3 = this.sourceSkillIndex;
               int var4 = Math.max(0, this.sourceSkillCount - 4);
               this.sourceSkillScroll = Math.max(0, Math.min(var4, this.sourceSkillScroll + var1));
               this.sourceSkillIndex = clampIndexIntoVisible(this.sourceSkillIndex, this.sourceSkillScroll, this.sourceSkillCount, 4);
               if (this.sourceSkillScroll != var2 || this.sourceSkillIndex != var3) {
                  this.session.story.trace().add("PC_QOL mouse wheel skill.ui scrollbar index=" + this.sourceSkillIndex + " scroll=" + this.sourceSkillScroll + " rows=" + this.sourceSkillCount);
               }

            }
         }
      }

      private void mouseWheelSourcePetSetting(int var1) {
         if (this.sourcePetSettingCount > 0) {
            int var2 = this.sourcePetSettingIndex;
            int var3 = this.sourcePetSettingScroll;
            this.sourcePetSettingIndex = Math.max(0, Math.min(this.sourcePetSettingCount - 1, this.sourcePetSettingIndex + var1));
            this.keepSourcePetSettingSelectionVisible();
            if (this.sourcePetSettingIndex != var2 || this.sourcePetSettingScroll != var3) {
               this.session.story.trace().add("PC_QOL mouse wheel petsetting.ui selection index=" + this.sourcePetSettingIndex + " scroll=" + this.sourcePetSettingScroll + " rows=" + this.sourcePetSettingCount);
            }

         }
      }

      private static int clampIndexIntoVisible(int var0, int var1, int var2, int var3) {
         if (var2 <= 0) {
            return 0;
         } else {
            int var4 = Math.max(0, Math.min(var2 - 1, var0));
            if (var4 < var1) {
               var4 = var1;
            } else if (var4 >= var1 + var3) {
               var4 = var1 + var3 - 1;
            }

            return Math.max(0, Math.min(var2 - 1, var4));
         }
      }

      private void openSourceSkillUiFromPetSetting() {
         this.sourceSkillIndex = 0;
         this.sourceSkillScroll = 0;
         this.sourceSkillBrowseTab = 0;
         this.sourceSkillLearnMode = false;
         this.sourceSkillLearnConfirm = false;
         this.sourceSkillLearnDeclineConfirm = false;
         this.sourceSkillLearnReturnPortableShop = false;
         this.sourceSkillLearnReturnPetstate = false;
         this.sourceSkillLearnReturnBrowse = false;
         this.sourceSkillRelearnMode = false;
         this.sourceSkillRelearnNotice = false;
         this.sourceSkillRelearnBrowseIndex = 0;
         this.sourceSkillLearnQueueIndex = -1;
         this.sourceSkillLearnPetIndices = new int[0];
         this.sourceSkillLearnQueueSkillIds = new int[0];
         this.sourceSkillLearnIds = new int[0];
         this.sourceSkillLearnSelectedId = -1;
         this.sourceSkillVisible = true;
         this.sourcePetSettingVisible = false;
         this.closeWorldPetstateDetailUi();
         this.refreshSourceSkillBrowseRows();
         PetState var1 = this.selectedSourceSkillPet();
         List var10000 = this.session.story.trace();
         int var10001 = this.battleMenuIndex;
         var10000.add("PORTED/PARTIAL panel game.h.X petsetting c=4 -> skill.ui f=2 r=0 close petsetting.ui+petstate.ui selectedPet=" + var10001 + " species=" + (var1 == null ? -1 : var1.speciesId) + " skills=" + Arrays.toString(var1 == null ? new int[0] : var1.skillIds) + " uiRows=4 sourcePetSlots=" + (var1 == null ? 0 : var1.skillIds.length));
      }

      private void tickSourceSkill() {
         if (this.sourceSkillLearnMode) {
            this.tickSourceSkillLearn();
         } else if (this.sourceSkillRelearnNotice) {
            if (this.key0 || this.keyBack) {
               this.sourceSkillRelearnNotice = false;
               if (this.text != null && this.text.sourceUiKind == 3) {
                  this.text = null;
               }
            }

         } else {
            if (!this.keyLeft && !this.keyRight) {
               if (this.keyUp && this.sourceSkillIndex > 0) {
                  --this.sourceSkillIndex;
                  this.syncSourceSkillScrollToIndex();
                  List var1 = this.session.story.trace();
                  int var2 = this.sourceSkillIndex;
                  var1.add("PORTED/PARTIAL panel game.h.X skill.ui p.a.b(prev) r=" + var2 + " desc=" + !this.sourceSkillDescription().isEmpty());
               } else if (this.keyDown && this.sourceSkillIndex < this.sourceSkillCount - 1) {
                  ++this.sourceSkillIndex;
                  this.syncSourceSkillScrollToIndex();
                  List var10000 = this.session.story.trace();
                  int var10001 = this.sourceSkillIndex;
                  var10000.add("PORTED/PARTIAL panel game.h.X skill.ui p.a.b(next) r=" + var10001 + " desc=" + !this.sourceSkillDescription().isEmpty());
               } else if (this.keyBack) {
                  this.sourceSkillVisible = false;
                  this.openWorldPetstate();
                  this.session.story.trace().add("PORTED/PARTIAL panel game.h.X skill.ui key=262144 e(b) refresh petstate.ui close skill.ui selectedPet=" + this.battleMenuIndex);
               } else if (this.key0) {
                  this.beginSourceSkillRelearn();
               }
            } else {
               this.switchSourceSkillBrowseTab(this.keyLeft ? 0 : 1, "keyboard tab");
            }

         }
      }

      private boolean sourceSkillCanRelearnAt(int var1) {
         return this.sourceSkillPoolTab() && var1 >= 0 && var1 < this.sourceSkillCount && this.sourceSkillStateAt(var1) == 1;
      }

      String sourceSkillPrimaryActionLabel() {
         if (this.sourceSkillLearnMode) {
            return "Chọn";
         } else {
            return this.sourceSkillCanRelearnAt(this.sourceSkillIndex) ? "Học lại - 1 HH" : "Đổi tab";
         }
      }

      private void beginSourceSkillRelearn() {
         if (!this.sourceSkillCanRelearnAt(this.sourceSkillIndex)) {
            List var10000 = this.session.story.trace();
            int var10001 = this.sourceSkillIndex;
            var10000.add("UNIFIED_DESIGN Skill relearn blocked index=" + var10001 + " tab=" + this.sourceSkillBrowseTab + " state=" + (this.sourceSkillCount == 0 ? -1 : this.sourceSkillStateAt(this.sourceSkillIndex)));
         } else {
            int var1 = this.sourceSkillIdAt(this.sourceSkillIndex);
            this.sourceSkillRelearnBrowseIndex = this.sourceSkillIndex;
            if (!VqsvSourceOps.sourceCanPay(this, 1, 1)) {
               this.showSourceSkillRelearnNotice("Không đủ Huy hiệu để học lại kỹ năng.");
            } else {
               this.sourceSkillLearnReturnBrowse = true;
               this.sourceSkillRelearnMode = true;
               this.sourceSkillLearnMode = true;
               this.sourceSkillLearnConfirm = true;
               this.sourceSkillLearnDeclineConfirm = false;
               this.sourceSkillLearnReplaceMode = false;
               this.sourceSkillLearnReplaceConfirm = false;
               this.sourceSkillLearnQueueIndex = 0;
               this.sourceSkillLearnPetIndices = new int[]{this.battleMenuIndex};
               this.sourceSkillLearnQueueSkillIds = new int[]{var1};
               this.sourceSkillLearnIds = new int[]{var1};
               this.sourceSkillLearnCandidateIds = new int[]{var1};
               this.sourceSkillLearnSelectedId = var1;
               this.sourceSkillCount = 1;
               this.sourceSkillIndex = 0;
               this.sourceSkillScroll = 0;
               BattleSkillRow var2 = VqsvBattleTables.instance().skill(var1);
               String var3 = var2 == null ? "Skill " + var1 : var2.name("Skill " + var1);
               this.text = TextBox.msgWarm("Học lại " + var3 + " với giá 1 Huy hiệu?", "Nhấn nút 5 để tiếp tục");
               this.session.story.trace().add("UNIFIED_DESIGN Skill relearn confirm petIndex=" + this.battleMenuIndex + " skill=" + var1 + " cost=1 badges=" + this.session.inventory.currency.badges);
            }
         }
      }

      private boolean sourceSkillRelearnCanPay() {
         return !this.sourceSkillRelearnMode || VqsvSourceOps.sourceCanPay(this, 1, 1);
      }

      private void paySourceSkillRelearnIfNeeded(boolean var1) {
         if (this.sourceSkillRelearnMode && var1) {
            VqsvSourceOps.sourcePay(this, 1, 1);
         }

      }

      private void showSourceSkillRelearnNotice(String var1) {
         this.returnToSourceSkillBrowse("relearn blocked");
         this.sourceSkillRelearnNotice = true;
         this.text = TextBox.msgWarm(var1, "Nhấn nút 5 để tiếp tục");
         this.session.story.trace().add("UNIFIED_DESIGN Skill relearn notice message=" + var1 + " badges=" + this.session.inventory.currency.badges);
      }

      private void switchSourceSkillBrowseTab(int var1, String var2) {
         if (!this.sourceSkillLearnMode) {
            int var3 = var1 <= 0 ? 0 : 1;
            if (this.sourceSkillBrowseTab != var3 || this.sourceSkillCount <= 0) {
               this.sourceSkillBrowseTab = var3;
               this.sourceSkillIndex = 0;
               this.sourceSkillScroll = 0;
               this.refreshSourceSkillBrowseRows();
               this.session.story.trace().add("UNIFIED-V5 skill management tab tab=" + this.sourceSkillBrowseTab + " rows=" + this.sourceSkillCount + " reason=" + var2);
            }
         }
      }

      private void refreshSourceSkillBrowseRows() {
         if (!this.sourceSkillLearnMode) {
            this.sourceSkillCount = this.sourceSkillBrowseTab == 0 ? this.sourceSkillEquippedCount() : this.sourceSkillPoolEntries().size();
            this.sourceSkillIndex = Math.max(0, Math.min(Math.max(0, this.sourceSkillCount - 1), this.sourceSkillIndex));
            this.syncSourceSkillScrollToIndex();
         }
      }

      boolean openSourceSkillLearnQueue(List<Integer> var1, boolean var2, String var3) {
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();

         for(Integer var7 : var1) {
            int var8 = var7 == null ? -1 : var7;

            for(int var12 : this.sourceSkillLearnCandidatesForPet(var8)) {
               var4.add(var8);
               var5.add(var12);
            }
         }

         return this.openSourceSkillLearnQueue(var4, var5, var2, false, var3);
      }

      boolean openSourceSkillLearnQueueForLevelTransitions(List<Integer> var1, List<Integer> var2, boolean var3, String var4) {
         ArrayList var5 = new ArrayList();
         ArrayList var6 = new ArrayList();

         for(int var7 = 0; var7 < var1.size(); ++var7) {
            int var8 = var1.get(var7) == null ? -1 : (Integer)var1.get(var7);
            int var9 = var7 < var2.size() && var2.get(var7) != null ? (Integer)var2.get(var7) : -1;
            if (var8 >= 0 && var8 < this.session.pets.roster.size()) {
               PetState var10 = (PetState)this.session.pets.roster.get(var8);
               BattleUnit var11 = PetBattleAdapter.toBattleUnit(var10, (byte)0, this.session.progression.badges);

               for(int var15 : var11.sourceLearnCandidateSkillIdsBetween(var9, var10.level)) {
                  var5.add(var8);
                  var6.add(var15);
               }
            }
         }

         return this.openSourceSkillLearnQueue(var5, var6, var3, false, var4);
      }

      private boolean openSourceEvolutionSkillLearnQueue(int var1, int var2, String var3) {
         if (var1 >= 0 && var1 < this.session.pets.roster.size()) {
            PetState var4 = (PetState)this.session.pets.roster.get(var1);
            BattleUnit var5 = PetBattleAdapter.toBattleUnit(var4, (byte)0, this.session.progression.badges);
            int[] var6 = var5.sourceEvolutionLearnCandidateSkillIds(var2);
            ArrayList var7 = new ArrayList();
            ArrayList var8 = new ArrayList();

            for(int var12 : var6) {
               var7.add(var1);
               var8.add(var12);
            }

            return this.openSourceSkillLearnQueue(var7, var8, false, true, var3);
         } else {
            return false;
         }
      }

      private boolean openSourceSkillLearnQueue(List<Integer> var1, List<Integer> var2, boolean var3, boolean var4, String var5) {
         if (!var1.isEmpty() && var1.size() == var2.size()) {
            this.sourceSkillLearnPetIndices = new int[var1.size()];
            this.sourceSkillLearnQueueSkillIds = new int[var2.size()];

            for(int var6 = 0; var6 < var1.size(); ++var6) {
               this.sourceSkillLearnPetIndices[var6] = (Integer)var1.get(var6);
               this.sourceSkillLearnQueueSkillIds[var6] = (Integer)var2.get(var6);
            }

            this.sourceSkillLearnReturnPortableShop = var3;
            this.sourceSkillLearnReturnPetstate = var4;
            this.sourceSkillLearnReturnBrowse = false;
            this.sourceSkillRelearnMode = false;
            this.sourceSkillRelearnNotice = false;
            this.sourceSkillLearnMode = true;
            this.sourceSkillLearnConfirm = false;
            this.sourceSkillLearnDeclineConfirm = false;
            this.sourceSkillLearnReplaceMode = false;
            this.sourceSkillLearnReplaceConfirm = false;
            this.sourceSkillLearnQueueIndex = -1;
            this.sourceSkillLearnSelectedId = -1;
            this.sourceSkillVisible = true;
            this.sourcePetSettingVisible = false;
            this.closeWorldPetstateDetailUi();
            ((VqsvPanelRuntime)this.session.runtime.ui).suspendForSourceSkillLearn(this, var3);
            this.session.story.trace().add("PORTED/PARTIAL source product3 opens skill learn queue size=" + this.sourceSkillLearnPetIndices.length + " returnPortableShop=" + var3 + " reason=" + var5);
            return this.openNextSourceSkillLearnPet(var5);
         } else {
            this.session.story.trace().add("PORTED/PARTIAL source skill learn queue empty reason=" + var5);
            return false;
         }
      }

      private void tickSourceSkillLearn() {
         if (this.sourceSkillLearnReplaceMode) {
            this.tickSourceSkillReplacement();
         } else if (this.sourceSkillLearnDeclineConfirm) {
            if (this.keyBack) {
               this.sourceSkillLearnDeclineConfirm = false;
               if (this.text != null && this.text.sourceUiKind == 3) {
                  this.text = null;
               }

            } else if (this.key0) {
               this.key0 = false;
               this.sourceSkillLearnDeclineConfirm = false;
               if (this.text != null && this.text.sourceUiKind == 3) {
                  this.text = null;
               }

               this.session.story.trace().add("UNIFIED-V5 source give up Skill confirmed petIndex=" + this.battleMenuIndex + " skill=" + this.sourceSkillLearnSelectedId);
               this.openNextSourceSkillLearnPet("give up current skill");
            }
         } else if (this.sourceSkillLearnConfirm) {
            if (this.keyBack) {
               if (this.sourceSkillRelearnMode) {
                  this.returnToSourceSkillBrowse("relearn confirm cancelled");
               } else {
                  this.sourceSkillLearnConfirm = false;
                  this.sourceSkillLearnSelectedId = -1;
                  if (this.text != null && this.text.sourceUiKind == 3) {
                     this.text = null;
                  }

                  this.session.story.trace().add("PORTED/PARTIAL source choiceskill learn confirm back petIndex=" + this.battleMenuIndex);
               }
            } else if (this.key0) {
               this.key0 = false;
               if (this.text != null && this.text.sourceUiKind == 3) {
                  this.text = null;
               }

               if (!this.sourceSkillRelearnCanPay()) {
                  this.showSourceSkillRelearnNotice("Không đủ Huy hiệu để học lại kỹ năng.");
               } else {
                  if (this.learnSelectedSourceSkill()) {
                     this.openNextSourceSkillLearnPet("learned skill");
                  }

               }
            }
         } else {
            if ((this.keyUp || this.keyLeft) && this.sourceSkillIndex > 0) {
               --this.sourceSkillIndex;
               this.syncSourceSkillScrollToIndex();
               this.session.story.trace().add("PORTED/PARTIAL source choiceskill learn prev petIndex=" + this.battleMenuIndex + " index=" + this.sourceSkillIndex);
            } else if ((this.keyDown || this.keyRight) && this.sourceSkillIndex < this.sourceSkillCount - 1) {
               ++this.sourceSkillIndex;
               this.syncSourceSkillScrollToIndex();
               this.session.story.trace().add("PORTED/PARTIAL source choiceskill learn next petIndex=" + this.battleMenuIndex + " index=" + this.sourceSkillIndex);
            } else if (this.keyBack) {
               this.sourceSkillLearnSelectedId = this.sourceSkillIdAt(this.sourceSkillIndex);
               BattleSkillRow var1 = VqsvBattleTables.instance().skill(this.sourceSkillLearnSelectedId);
               String var2 = var1 == null ? "Skill " + this.sourceSkillLearnSelectedId : var1.name("Skill " + this.sourceSkillLearnSelectedId);
               this.text = TextBox.msgWarm("Từ bỏ học " + var2 + "?", "Nhấn nút 5 để tiếp tục");
               this.sourceSkillLearnDeclineConfirm = true;
            } else if (this.key0) {
               this.sourceSkillLearnSelectedId = this.sourceSkillIdAt(this.sourceSkillIndex);
               BattleSkillRow var3 = VqsvBattleTables.instance().skill(this.sourceSkillLearnSelectedId);
               String var4 = var3 == null ? "Skill " + this.sourceSkillLearnSelectedId : var3.name("Skill " + this.sourceSkillLearnSelectedId);
               this.text = TextBox.msgWarm("Học tập" + var4, "Nhấn nút 5 để tiếp tục");
               this.sourceSkillLearnConfirm = true;
               this.session.story.trace().add("PORTED/PARTIAL source choiceskill learn confirm petIndex=" + this.battleMenuIndex + " skill=" + this.sourceSkillLearnSelectedId);
            }

         }
      }

      private boolean openNextSourceSkillLearnPet(String var1) {
         this.sourceSkillLearnConfirm = false;
         this.sourceSkillLearnDeclineConfirm = false;
         this.sourceSkillLearnSelectedId = -1;

         while(++this.sourceSkillLearnQueueIndex < this.sourceSkillLearnPetIndices.length) {
            int var2 = this.sourceSkillLearnPetIndices[this.sourceSkillLearnQueueIndex];
            int var3 = this.sourceSkillLearnQueueSkillIds[this.sourceSkillLearnQueueIndex];
            if (var2 >= 0 && var2 < this.session.pets.roster.size() && var3 >= 0) {
               this.battleMenuIndex = var2;
               this.sourceSkillLearnIds = new int[]{var3};
               this.sourceSkillLearnCandidateIds = new int[]{var3};
               this.sourceSkillCount = 1;
               this.sourceSkillIndex = 0;
               this.sourceSkillScroll = 0;
               this.sourceSkillVisible = true;
               this.sourceSkillLearnMode = true;
               this.session.story.trace().add("PORTED/PARTIAL source levelUp.ui -> choiceskill.ui petIndex=" + var2 + " species=" + this.selectedSourceSkillSpeciesId() + " level=" + this.sourceSkillPetLevel() + " candidate=" + var3 + " reason=" + var1);
               return true;
            }

            this.session.story.trace().add("PORTED/PARTIAL source skill learn queue skip empty petIndex=" + var2 + " reason=" + var1);
         }

         this.closeSourceSkillLearnQueue(var1);
         return false;
      }

      private boolean learnSelectedSourceSkill() {
         PetState var1 = this.selectedSourceSkillPet();
         if (var1 != null && this.sourceSkillLearnSelectedId >= 0) {
            BattleUnit var2 = PetBattleAdapter.toBattleUnit(var1, (byte)0, this.session.progression.badges);
            if (!var2.hasFreeSkillSlot()) {
               this.beginSourceSkillReplacement(var1);
               return false;
            } else {
               boolean var3 = var2.learnSourceSkill(this.sourceSkillLearnSelectedId);
               if (var3) {
                  BattlePetStateAdapter.persist(var1, var2);
                  this.paySourceSkillRelearnIfNeeded(true);
               }

               this.session.story.trace().add("PORTED/PARTIAL source choiceskill learn apply petIndex=" + this.battleMenuIndex + " species=" + var1.speciesId + " skill=" + this.sourceSkillLearnSelectedId + " learned=" + var3 + " skills=" + Arrays.toString(var1.skillIds));
               return true;
            }
         } else {
            return true;
         }
      }

      private void beginSourceSkillReplacement(PetState var1) {
         this.sourceSkillLearnReplaceMode = true;
         this.sourceSkillLearnReplaceConfirm = false;
         this.sourceSkillLearnConfirm = false;
         this.sourceSkillLearnCandidateIds = Arrays.copyOf(this.sourceSkillLearnIds, this.sourceSkillLearnIds.length);
         ArrayList var2 = new ArrayList();

         for(int var6 : var1.skillIds) {
            if (var6 >= 0) {
               var2.add(var6);
            }
         }

         this.sourceSkillLearnIds = var2.stream().mapToInt((value) -> (Integer)value).toArray();
         this.sourceSkillCount = this.sourceSkillLearnIds.length;
         this.sourceSkillIndex = 0;
         this.sourceSkillScroll = 0;
         this.text = TextBox.taskTip("Chọn Skill cũ cần thay thế");
         List var10000 = this.session.story.trace();
         int var10001 = this.battleMenuIndex;
         var10000.add("UNIFIED-V5 Skill replace selection opened petIndex=" + var10001 + " incoming=" + this.sourceSkillLearnSelectedId + " equipped=" + Arrays.toString(this.sourceSkillLearnIds));
      }

      private void tickSourceSkillReplacement() {
         if (this.sourceSkillLearnReplaceConfirm) {
            if (this.keyBack) {
               this.sourceSkillLearnReplaceConfirm = false;
               if (this.text != null && this.text.sourceUiKind == 3) {
                  this.text = null;
               }

            } else if (this.key0) {
               this.key0 = false;
               if (this.text != null && this.text.sourceUiKind == 3) {
                  this.text = null;
               }

               if (!this.sourceSkillRelearnCanPay()) {
                  this.showSourceSkillRelearnNotice("Không đủ Huy hiệu để học lại kỹ năng.");
               } else {
                  if (this.replaceSelectedSourceSkill()) {
                     this.openNextSourceSkillLearnPet("replaced skill");
                  }

               }
            }
         } else {
            if ((this.keyUp || this.keyLeft) && this.sourceSkillIndex > 0) {
               --this.sourceSkillIndex;
               this.syncSourceSkillScrollToIndex();
            } else if ((this.keyDown || this.keyRight) && this.sourceSkillIndex < this.sourceSkillCount - 1) {
               ++this.sourceSkillIndex;
               this.syncSourceSkillScrollToIndex();
            } else if (this.keyBack) {
               if (this.sourceSkillRelearnMode) {
                  this.returnToSourceSkillBrowse("relearn replacement cancelled");
                  return;
               }

               this.sourceSkillLearnReplaceMode = false;
               this.sourceSkillLearnIds = Arrays.copyOf(this.sourceSkillLearnCandidateIds, this.sourceSkillLearnCandidateIds.length);
               this.sourceSkillCount = this.sourceSkillLearnIds.length;
               this.sourceSkillIndex = 0;
               this.sourceSkillScroll = 0;
               this.text = null;
            } else if (this.key0 && this.sourceSkillCount > 0) {
               int var1 = this.sourceSkillIdAt(this.sourceSkillIndex);
               BattleSkillRow var2 = VqsvBattleTables.instance().skill(var1);
               BattleSkillRow var3 = VqsvBattleTables.instance().skill(this.sourceSkillLearnSelectedId);
               String var4 = var2 == null ? "Skill " + var1 : var2.name("");
               String var5 = var3 == null ? "Skill " + this.sourceSkillLearnSelectedId : var3.name("");
               this.text = TextBox.msgWarm("Thay " + var4 + " bằng " + var5, "Nhấn nút 5 để tiếp tục");
               this.sourceSkillLearnReplaceConfirm = true;
            }

         }
      }

      private boolean replaceSelectedSourceSkill() {
         PetState var1 = this.selectedSourceSkillPet();
         if (var1 != null && this.sourceSkillLearnSelectedId >= 0) {
            BattleUnit var2 = PetBattleAdapter.toBattleUnit(var1, (byte)0, this.session.progression.badges);
            int var3 = this.sourceSkillIdAt(this.sourceSkillIndex);
            boolean var4 = var2.replaceSkillAt(this.sourceSkillIndex, this.sourceSkillLearnSelectedId);
            if (var4) {
               BattlePetStateAdapter.persist(var1, var2);
               this.paySourceSkillRelearnIfNeeded(true);
            }

            this.session.story.trace().add("UNIFIED-V5 Skill replacement petIndex=" + this.battleMenuIndex + " slot=" + this.sourceSkillIndex + " old=" + var3 + " new=" + this.sourceSkillLearnSelectedId + " replaced=" + var4 + " skills=" + Arrays.toString(var1.skillIds));
            return var4;
         } else {
            return false;
         }
      }

      private void closeSourceSkillLearnQueue(String var1) {
         boolean var2 = this.sourceSkillLearnReturnPortableShop;
         boolean var3 = this.sourceSkillLearnReturnPetstate;
         boolean var4 = this.sourceSkillLearnReturnBrowse;
         this.sourceSkillVisible = false;
         this.sourceSkillLearnMode = false;
         this.sourceSkillLearnReturnPortableShop = false;
         this.sourceSkillLearnReturnPetstate = false;
         this.sourceSkillLearnReturnBrowse = false;
         this.sourceSkillLearnConfirm = false;
         this.sourceSkillLearnDeclineConfirm = false;
         this.sourceSkillLearnReplaceMode = false;
         this.sourceSkillLearnReplaceConfirm = false;
         this.sourceSkillLearnQueueIndex = -1;
         this.sourceSkillLearnPetIndices = new int[0];
         this.sourceSkillLearnQueueSkillIds = new int[0];
         this.sourceSkillLearnIds = new int[0];
         this.sourceSkillLearnCandidateIds = new int[0];
         this.sourceSkillLearnSelectedId = -1;
         this.sourceSkillRelearnMode = false;
         this.sourceSkillCount = 5;
         this.sourceSkillIndex = 0;
         this.sourceSkillScroll = 0;
         this.session.story.trace().add("PORTED/PARTIAL source choiceskill learn queue complete reason=" + var1 + " reopenPortableShop=" + var2);
         if (var4) {
            this.returnToSourceSkillBrowse(var1);
         } else if (var2) {
            ((VqsvPanelRuntime)this.session.runtime.ui).reopenPortableShopAfterSourceSkillLearn(this);
         } else if (var3) {
            this.openWorldPetstate();
         }

      }

      private int[] sourceSkillLearnCandidatesForPet(int var1) {
         if (var1 >= 0 && var1 < this.session.pets.roster.size()) {
            BattleUnit var2 = PetBattleAdapter.toBattleUnit((PetState)this.session.pets.roster.get(var1), (byte)0, this.session.progression.badges);
            return var2.sourceCanLearnAfterLevelUp() ? var2.sourceLearnCandidateSkillIds() : new int[0];
         } else {
            return new int[0];
         }
      }

      private void syncSourceSkillScrollToIndex() {
         if (this.sourceSkillIndex < this.sourceSkillScroll) {
            this.sourceSkillScroll = this.sourceSkillIndex;
         } else if (this.sourceSkillIndex >= this.sourceSkillScroll + 4) {
            this.sourceSkillScroll = this.sourceSkillIndex - 4 + 1;
         }

         this.sourceSkillScroll = Math.max(0, Math.min(Math.max(0, this.sourceSkillCount - 4), this.sourceSkillScroll));
      }

      int sourceSkillDisplayIndexAt(int var1) {
         return this.sourceSkillScroll + var1;
      }

      PetState selectedSourceSkillPet() {
         return this.battleMenuIndex >= 0 && this.battleMenuIndex < this.session.pets.roster.size() ? (PetState)this.session.pets.roster.get(this.battleMenuIndex) : null;
      }

      private int selectedSourceSkillSpeciesId() {
         PetState var1 = this.selectedSourceSkillPet();
         return var1 == null ? -1 : var1.speciesId;
      }

      String sourceSkillPetName() {
         PetState var1 = this.selectedSourceSkillPet();
         if (var1 == null) {
            return "";
         } else {
            BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1.speciesId);
            return var2 == null ? "Pet " + var1.speciesId : var2.name("Pet " + var1.speciesId);
         }
      }

      int sourceSkillPetLevel() {
         PetState var1 = this.selectedSourceSkillPet();
         return var1 == null ? 0 : var1.level;
      }

      int sourceSkillPetVisualId() {
         PetState var1 = this.selectedSourceSkillPet();
         if (var1 == null) {
            return -1;
         } else {
            BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1.speciesId);
            return var2 == null ? -1 : var2.spriteId;
         }
      }

      int sourceSkillIdAt(int var1) {
         if (this.sourceSkillLearnMode) {
            return var1 >= 0 && var1 < this.sourceSkillLearnIds.length ? this.sourceSkillLearnIds[var1] : -1;
         } else if (this.sourceSkillBrowseTab != 1) {
            PetState var8 = this.selectedSourceSkillPet();
            if (var8 != null && var1 >= 0) {
               int var3 = 0;

               for(int var7 : var8.skillIds) {
                  if (var7 >= 0 && var3++ == var1) {
                     return var7;
                  }
               }

               return -1;
            } else {
               return -1;
            }
         } else {
            List var2 = this.sourceSkillPoolEntries();
            return var1 >= 0 && var1 < var2.size() ? ((UnifiedSkillLearnsetCatalog.Entry)var2.get(var1)).runtimeSkillId : -1;
         }
      }

      String sourceSkillNameAt(int var1) {
         int var2 = this.sourceSkillIdAt(var1);
         if (var2 == -1) {
            return "";
         } else {
            BattleSkillRow var3 = VqsvBattleTables.instance().skill(var2);
            return var3 == null ? "Skill " + var2 : var3.name("Skill " + var2);
         }
      }

      String sourceSkillDescription() {
         int var1 = this.sourceSkillIdAt(this.sourceSkillIndex);
         if (var1 == -1) {
            return "";
         } else {
            BattleSkillRow var2 = VqsvBattleTables.instance().skill(var1);
            String var3 = var2 == null ? "" : var2.description("");
            return this.sourceSkillCanRelearnAt(this.sourceSkillIndex) ? "Giá: 1 Huy hiệu. " + var3 : var3;
         }
      }

      boolean sourceSkillPoolTab() {
         return !this.sourceSkillLearnMode && this.sourceSkillBrowseTab == 1;
      }

      int sourceSkillEquippedCount() {
         PetState var1 = this.selectedSourceSkillPet();
         if (var1 == null) {
            return 0;
         } else {
            int var2 = 0;

            for(int var6 : var1.skillIds) {
               if (var6 >= 0) {
                  ++var2;
               }
            }

            return var2;
         }
      }

      int sourceSkillPoolTotalCount() {
         return this.sourceSkillPoolEntries().size();
      }

      int sourceSkillPoolUnlockedCount() {
         int var1 = 0;

         for(int var2 = 0; var2 < this.sourceSkillPoolEntries().size(); ++var2) {
            int var3 = this.sourceSkillStateAt(var2, true);
            if (var3 == 0 || var3 == 1) {
               ++var1;
            }
         }

         return var1;
      }

      int sourceSkillStateAt(int var1) {
         return this.sourceSkillStateAt(var1, this.sourceSkillPoolTab());
      }

      private int sourceSkillStateAt(int var1, boolean var2) {
         List var3 = this.sourceSkillPoolEntries();
         int var4 = var2 ? (var1 >= 0 && var1 < var3.size() ? ((UnifiedSkillLearnsetCatalog.Entry)var3.get(var1)).runtimeSkillId : -1) : this.sourceSkillIdAt(var1);
         PetState var5 = this.selectedSourceSkillPet();
         if (var5 != null && var4 >= 0) {
            for(int var9 : var5.skillIds) {
               if (var9 == var4) {
                  return 0;
               }
            }

            if (!var2) {
               return 1;
            } else {
               UnifiedSkillLearnsetCatalog.Entry var10 = (UnifiedSkillLearnsetCatalog.Entry)var3.get(var1);
               String var11 = UnifiedSkillLearnsetCatalog.petKey(var5.speciesId);
               UnifiedSkillLearnsetCatalog var12 = UnifiedSkillLearnsetCatalog.instance();
               if (var12.isAncestor(var10.requiredFormPetKey, var11)) {
                  return var10.unlockLevel <= var5.level ? 1 : 2;
               } else if (var12.isAncestor(var11, var10.requiredFormPetKey)) {
                  return 3;
               } else {
                  return 4;
               }
            }
         } else {
            return 4;
         }
      }

      String sourceSkillStatusAt(int var1) {
         int var2 = this.sourceSkillStateAt(var1);
         if (var2 == 0) {
            return "Đang dùng";
         } else if (var2 == 1) {
            return this.sourceSkillPoolTab() ? "Có thể học lại" : "Đã mở";
         } else {
            UnifiedSkillLearnsetCatalog.Entry var3 = this.sourceSkillPoolEntryAt(var1);
            if (var2 == 2) {
               return var3 == null ? "Chưa mở" : "Mở Lv." + var3.unlockLevel;
            } else if (var2 == 3) {
               return var3 == null ? "Cần tiến hóa" : "Cần " + this.sourceSkillGrantPetName(var3.requiredFormPetKey);
            } else {
               return "Khác nhánh";
            }
         }
      }

      String sourceSkillRowStatusAt(int var1) {
         int var2 = this.sourceSkillStateAt(var1);
         return var2 == 3 ? "Khóa tiến hóa" : this.sourceSkillStatusAt(var1);
      }

      String sourceSkillPpAt(int var1) {
         int var2 = this.sourceSkillIdAt(var1);
         BattleSkillRow var3 = VqsvBattleTables.instance().skill(var2);
         if (var3 == null) {
            return "PP --";
         } else {
            PetState var4 = this.selectedSourceSkillPet();
            if (var4 != null) {
               for(int var5 = 0; var5 < var4.skillIds.length; ++var5) {
                  if (var4.skillIds[var5] == var2) {
                     int var10000 = var4.skillCooldowns[var5];
                     return "PP " + var10000 + "/" + var3.ppMax;
                  }
               }
            }

            return "PP --/" + var3.ppMax;
         }
      }

      String sourceSkillMetaAt(int var1) {
         BattleSkillRow var2 = VqsvBattleTables.instance().skill(this.sourceSkillIdAt(var1));
         if (var2 == null) {
            return "";
         } else {
            String var3 = var2.powerPercent > 0 ? String.valueOf(var2.powerPercent) : "--";
            return "Hệ " + sourceSkillElementName(var2.elementFamily) + "  |  Lực " + var3 + "  |  " + this.sourceSkillPpAt(var1);
         }
      }

      private List<UnifiedSkillLearnsetCatalog.Entry> sourceSkillPoolEntries() {
         PetState var1 = this.selectedSourceSkillPet();
         return var1 != null && UnifiedSkillProfile.unifiedEnabled() ? UnifiedSkillLearnsetCatalog.instance().familyEntries(UnifiedSkillLearnsetCatalog.petKey(var1.speciesId)) : Collections.emptyList();
      }

      private UnifiedSkillLearnsetCatalog.Entry sourceSkillPoolEntryAt(int var1) {
         if (!this.sourceSkillPoolTab()) {
            return null;
         } else {
            List var2 = this.sourceSkillPoolEntries();
            return var1 >= 0 && var1 < var2.size() ? (UnifiedSkillLearnsetCatalog.Entry)var2.get(var1) : null;
         }
      }

      private String sourceSkillGrantPetName(String var1) {
         if (var1 != null && (var1.startsWith("LH-") || var1.startsWith("CV-"))) {
            try {
               int var2 = Integer.parseInt(var1.substring(3)) + (var1.startsWith("CV-") ? 100 : 0);
               BattleSpeciesRow var3 = VqsvBattleTables.instance().species(var2);
               return var3 == null ? var1 : var3.name(var1);
            } catch (NumberFormatException var4) {
               return var1;
            }
         } else {
            return "tiến hóa";
         }
      }

      private static String sourceSkillElementName(int var0) {
         switch (var0) {
            case 0 -> {
               return "Hỏa";
            }
            case 1 -> {
               return "Mộc";
            }
            case 2 -> {
               return "Thổ";
            }
            case 3 -> {
               return "Thủy";
            }
            case 4 -> {
               return "Điện";
            }
            case 5 -> {
               return "Quỷ";
            }
            case 6 -> {
               return "Phong";
            }
            default -> {
               return "Không";
            }
         }
      }

      String sourcePetSettingEvolutionLabel() {
         EvolutionCandidate var1 = VqsvSourceEvolutionRuntime.noticeForPet(this, this.battleMenuIndex);
         if (var1 != null && var1.available()) {
            return var1.kind == EvolutionCandidate.Kind.MUTATION ? "Dị hoá" : "Tiến hóa";
         } else {
            return "";
         }
      }

      String sourcePetSettingActionLabel(int var1) {
         if (var1 == this.sourcePetSettingCount - 1 && this.sourcePetSettingCount >= 7) {
            return WorldPetCompanionRuntime.selected(this, this.battleMenuIndex) ? "Ngừng đồng hành" : "Dắt đi theo";
         }
         switch (var1) {
            case 0 -> {
               return "Đạo cụ";
            }
            case 1 -> {
               return "Chiến đấu";
            }
            case 2 -> {
               return "Vật phẩm trang sức";
            }
            case 3 -> {
               return "Phóng sinh";
            }
            case 4 -> {
               return "Kỹ năng";
            }
            case 5 -> {
               return "Nâng phẩm";
            }
            case 6 -> {
               return this.sourcePetSettingEvolutionLabel();
            }
            default -> {
               return "";
            }
         }
      }

      private void openSourceEvolveUi(int var1) {
         this.openSourceEvolveUi(var1, (EvolutionCandidate.Kind)null);
      }

      private void openSourceEvolveUi(int var1, EvolutionCandidate.Kind var2) {
         this.sourceEvolvePreferredKind = var2;
         this.sourceEvolveNotice = VqsvSourceEvolutionRuntime.noticeForPet(this, var1, var2);
         this.sourceEvolveVisible = true;
         this.sourceEvolvePetIndex = var1;
         this.sourceEvolvePhase = 0;
         this.sourceEvolveEffectTicks = 0;
         this.sourceEvolveSucceeded = false;
         this.closeWorldPetstateDetailUi();
         this.refreshSourceEvolvePanelFromPet();
         this.session.story.trace().add("PORTED/PARTIAL game.h.bg evolve.ui open petIndex=" + var1 + " notice=" + (this.sourceEvolveNotice == null ? "none" : this.sourceEvolveNotice.currentSpeciesId + "->" + this.sourceEvolveNotice.targetSpeciesId) + " widget=10/38/40/45/46 stats=19..22/31..34");
      }

      private void tickSourceEvolve() {
         if (this.sourceEvolvePhase == 1) {
            ++this.sourceEvolveEffectTicks;
            if (this.sourceEvolveEffectTicks >= this.sourceEvolveType10Duration()) {
               EvolutionCandidate var6 = this.sourceEvolveNotice;
               VqsvSourceEvolutionRuntime.mutatePet(this, this.sourceEvolvePetIndex, var6);
               String var8 = var6.targetKind == 3 ? "Dị hoá" : "Tiến hóa";
               String var10 = this.sourceEvolutionTargetName(var6);
               this.sourceEvolveNotice = var6;
               this.sourceEvolveOldVisualId = this.sourceEvolveNewVisualId;
               this.text = TextBox.msgWarm(var8 + " thành #2" + var10, "Nhấn nút 5 để tiếp tục");
               this.sourceEvolvePhase = 2;
               this.sourceEvolveSucceeded = true;
               List var12 = this.session.story.trace();
               int var13 = var6.targetSpeciesId;
               var12.add("PORTED/PARTIAL game.h.bh ah effect complete successMsg target=" + var13 + " preserve evolve.ui stats current=" + (this.sourceEvolveNotice == null ? "none" : this.sourceEvolveNotice.currentSpeciesId + "->" + this.sourceEvolveNotice.targetSpeciesId));
            }

         } else if (this.text != null) {
            if (this.text.readyForKey && this.key0) {
               this.text.confirm();
               this.key0 = false;
               if (this.sourceEvolvePhase == 2) {
                  if (this.sourceEvolveSucceeded) {
                     int var5 = this.sourceEvolvePetIndex;
                     int var7 = this.sourceEvolveNotice == null ? -1 : this.sourceEvolveNotice.currentSpeciesId;
                     int var9 = var5 >= 0 && var5 < this.session.pets.roster.size() ? ((PetState)this.session.pets.roster.get(var5)).speciesId : -1;
                     this.text = null;
                     this.closeSourceEvolveUi(true);
                     boolean var11 = this.openSourceEvolutionSkillLearnQueue(var5, var7, "evolution " + var7 + "->" + var9);
                     if (!var11) {
                        this.openWorldPetstate();
                     }

                     this.session.story.trace().add("UNIFIED-V5 evolution success closeout species=" + var7 + "->" + var9 + " learnQueueOpened=" + var11);
                  } else {
                     this.sourceEvolvePhase = 0;
                     this.session.story.trace().add("PORTED/PARTIAL game.h.bh close msgwarm warning return f=2 evolve.ui");
                  }
               }
            }

         } else if (this.keyBack && this.sourceEvolvePhase < 2) {
            this.closeSourceEvolveUi(false);
            this.session.story.trace().add("PORTED game.h.bh back key closes evolve.ui f<3");
         } else if (this.key0) {
            int var1 = this.sourceEvolvePetIndex >= 0 && this.sourceEvolvePetIndex < this.session.pets.roster.size() ? ((PetState)this.session.pets.roster.get(this.sourceEvolvePetIndex)).level : -1;
            int var2 = this.sourceEvolvePetIndex >= 0 && this.sourceEvolvePetIndex < this.session.pets.roster.size() ? ((PetState)this.session.pets.roster.get(this.sourceEvolvePetIndex)).quality : 1;
            EvolutionProgression.ConfirmOutcome var3 = this.session.progression.evolution.confirm(this.sourceEvolveNotice, var1, var2, (var1x) -> VqsvSourceEvolutionRuntime.materialCount(this, var1x));
            if (var3 == EvolutionProgression.ConfirmOutcome.NO_TARGET) {
               this.text = TextBox.msgWarm("Không thể lại tiến hóa hoặc dị hoá", "Nhấn nút 5 để tiếp tục");
               this.sourceEvolvePhase = 2;
            } else if (var3 == EvolutionProgression.ConfirmOutcome.LEVEL_TOO_LOW) {
               this.text = TextBox.msgWarm(VqsvText.Evolution.levelTooLow(this.sourceEvolveNotice.requiredLevel), "Nhấn nút 5 để tiếp tục");
               this.sourceEvolvePhase = 2;
            } else if (var3 == EvolutionProgression.ConfirmOutcome.QUALITY_TOO_LOW) {
               this.text = TextBox.msgWarm(VqsvText.Evolution.qualityTooLow(this.sourceEvolveNotice.requiredQuality), "Nhấn nút 5 để tiếp tục");
               this.sourceEvolvePhase = 2;
            } else if (var3 == EvolutionProgression.ConfirmOutcome.MATERIAL_TOO_LOW) {
               String var4 = this.sourceEvolveNotice.kind == EvolutionCandidate.Kind.MUTATION ? "Tài liệu chưa đủ, không thể dị hoá" : "Tài liệu chưa đủ, không thể tiến hóa";
               this.text = TextBox.msgWarm(var4, "Nhấn nút 5 để tiếp tục");
               this.sourceEvolvePhase = 2;
            } else if (!VqsvSourceEvolutionRuntime.consumeMaterials(this, this.sourceEvolveNotice)) {
               this.text = TextBox.msgWarm(this.sourceEvolveNotice.kind == EvolutionCandidate.Kind.MUTATION ? "Tài liệu chưa đủ, không thể dị hoá" : "Tài liệu chưa đủ, không thể tiến hóa", "Nhấn nút 5 để tiếp tục");
               this.sourceEvolvePhase = 2;
               this.session.story.trace().add("UNIFIED-EVOLUTION transaction aborted zero mutation recipe=" + this.sourceEvolveNotice.evolutionKey);
            } else {
               this.sourceEvolvePhase = 1;
               this.sourceEvolveEffectTicks = 0;
               List var10000 = this.session.story.trace();
               int var10001 = this.sourceEvolveOldVisualId;
               var10000.add("PORTED/PARTIAL game.h.bh start ah type10 row=[0,0,10,0,0," + var10001 + ",0,0," + this.sourceEvolveNewVisualId + ",0,0] t=[0," + this.sourceEvolveNewVisualId + ",0,0] consume materials=" + VqsvSourceEvolutionRuntime.materialSummary(this.sourceEvolveNotice));
            }
         }
      }

      private void refreshSourceEvolvePanelFromPet() {
         if (this.sourceEvolvePetIndex >= 0 && this.sourceEvolvePetIndex < this.session.pets.roster.size()) {
            PetState var1 = (PetState)this.session.pets.roster.get(this.sourceEvolvePetIndex);
            this.sourceEvolveNotice = VqsvSourceEvolutionRuntime.noticeForPet(this, this.sourceEvolvePetIndex, this.sourceEvolvePreferredKind);
            this.sourceEvolveOldStats = VqsvSourceEvolutionRuntime.visibleStats(this, var1);
            this.sourceEvolveNewStats = this.sourceEvolveNotice == null ? new int[]{0, 0, 0, 0} : VqsvSourceEvolutionRuntime.targetVisibleStats(this, var1, this.sourceEvolveNotice.targetSpeciesId);
            BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1.speciesId);
            BattleSpeciesRow var3 = this.sourceEvolveNotice == null ? null : VqsvBattleTables.instance().species(this.sourceEvolveNotice.targetSpeciesId);
            this.sourceEvolveOldVisualId = var2 == null ? -1 : var2.spriteId;
            this.sourceEvolveNewVisualId = var3 == null ? -1 : var3.spriteId;
            List var10000 = this.session.story.trace();
            int var10001 = var1.speciesId;
            var10000.add("PORTED/PARTIAL game.h.bh refresh evolve.ui current=" + var10001 + " next=" + (this.sourceEvolveNotice == null ? -1 : this.sourceEvolveNotice.targetSpeciesId) + " materials=" + VqsvSourceEvolutionRuntime.materialSummary(this.sourceEvolveNotice) + " counts=" + VqsvSourceEvolutionRuntime.materialCountSummary(this, this.sourceEvolveNotice));
         } else {
            this.sourceEvolveNotice = null;
            this.sourceEvolveOldStats = new int[]{0, 0, 0, 0};
            this.sourceEvolveNewStats = new int[]{0, 0, 0, 0};
            this.sourceEvolveOldVisualId = -1;
            this.sourceEvolveNewVisualId = -1;
         }
      }

      private int sourceEvolveType10Duration() {
         return 10;
      }

      private void closeSourceEvolveUi(boolean var1) {
         this.sourceEvolveVisible = false;
         this.sourceEvolvePetIndex = -1;
         this.sourceEvolveNotice = null;
         this.sourceEvolvePreferredKind = null;
         this.sourceEvolvePhase = 0;
         this.sourceEvolveEffectTicks = 0;
         this.sourceEvolveSucceeded = false;
         this.session.progression.evolution.selection[0] = -1;
         this.session.progression.evolution.selection[1] = -1;
         this.session.progression.evolutionTutorialU = -1;
         this.session.story.trace().add("PORTED/PARTIAL game.h.bh close evolve.ui success=" + var1 + " reset game.k.L and tutorial U");
      }

      private String sourceEvolutionTargetName(EvolutionCandidate var1) {
         BattleSpeciesRow var2 = var1 == null ? null : VqsvBattleTables.instance().species(var1.targetSpeciesId);
         if (var2 != null && var2.validForBattle()) {
            return var2.name("Pet " + var1.targetSpeciesId);
         } else {
            return var1 == null ? "" : "Pet " + var1.targetSpeciesId;
         }
      }

      private void hoverWorldPetstate(int var1, int var2) {
         int var3 = this.worldPetstateIndexAt(var1, var2);
         if (var3 >= 0 && var3 < this.battleMenuIds.length && var3 != this.battleMenuIndex) {
            this.battleMenuIndex = var3;
            this.rebuildWorldPetstateRows();
         }

      }

      private void clickWorldPetstate(int var1, int var2) {
         if (this.battlePetStateDetailUi.handlePointer(var1, var2)) {
            this.key0 = false;
         } else if (VqsvPetStateUiLayout.headerBackHit(var1, var2)) {
            this.keyBack = true;
            this.session.story.trace().add("PC_QOL petstate.ui header back arrow click -> game.h.X back close petstate.ui");
         } else if (this.petstatePreviousArrowHit(var1, var2)) {
            if (this.battleMenuIndex > 0) {
               --this.battleMenuIndex;
               this.keepWorldPetstateSelectionVisible();
               this.rebuildWorldPetstateRows();
            }

            this.session.story.trace().add("PC_QOL petstate.ui previous arrow click selectedPet=" + this.battleMenuIndex);
         } else if (this.petstateNextArrowHit(var1, var2)) {
            if (this.battleMenuIndex < this.battleMenuIds.length - 1) {
               ++this.battleMenuIndex;
               this.keepWorldPetstateSelectionVisible();
               this.rebuildWorldPetstateRows();
            }

            this.session.story.trace().add("PC_QOL petstate.ui next arrow click selectedPet=" + this.battleMenuIndex);
         } else {
            int var3 = this.worldPetstateIndexAt(var1, var2);
            if (var3 >= 0 && var3 < this.battleMenuIds.length) {
               this.battleMenuIndex = var3;
               this.rebuildWorldPetstateRows();
            } else if (VqsvPetStateUiLayout.actionHit(var1, var2)) {
               this.key0 = true;
            } else {
               if (VqsvPetStateUiLayout.backHit(var1, var2)) {
                  this.keyBack = true;
               }

            }
         }
      }

      private void closeWorldPetstateDetailUi() {
         this.worldPetstateVisible = false;
         this.battlePetStateDetailUi.close();
      }

      private void clickSourcePetSetting(int var1, int var2) {
         int var3 = this.sourcePetSettingIndexAt(var1, var2);
         if (var3 >= 0) {
            this.sourcePetSettingIndex = var3;
            this.key0 = true;
         } else if (sourceRightSoftkeyHit(var1, var2)) {
            this.keyBack = true;
         } else {
            if (sourceLeftSoftkeyHit(var1, var2)) {
               this.key0 = true;
            }

         }
      }

      private int worldPetstateIndexAt(int var1, int var2) {
         int var3 = Math.max(0, Math.min(this.battleMenuScroll, Math.max(0, this.battleMenuIds.length - 6)));
         return VqsvPetStateUiLayout.petIndexAt(var1, var2, this.battleMenuIds.length, var3);
      }

      boolean petstatePreviousArrowHit(int var1, int var2) {
         return VqsvPetStateUiLayout.previousHit(var1, var2);
      }

      boolean petstateNextArrowHit(int var1, int var2) {
         return VqsvPetStateUiLayout.nextHit(var1, var2);
      }

      private void hoverSourceSkill(int var1, int var2) {
         int var3 = this.sourceSkillIndexAt(var1, var2);
         if (var3 >= 0) {
            this.sourceSkillIndex = var3;
         }

      }

      private int sourceSkillIndexAt(int var1, int var2) {
         if (var1 >= 18 && var1 < 222 && var2 >= 134 && var2 < 210) {
            int var3 = (var2 - 134) / 19;
            int var4 = this.sourceSkillScroll + var3;
            return var3 >= 0 && var3 < 4 && var4 >= 0 && var4 < this.sourceSkillCount ? var4 : -1;
         } else {
            return -1;
         }
      }

      private int sourceSkillTabAt(int var1, int var2) {
         if (var1 >= 18 && var1 < 222 && var2 >= 110 && var2 < 132) {
            return var1 < 120 ? 0 : 1;
         } else {
            return -1;
         }
      }

      private void hoverSourcePetSetting(int var1, int var2) {
         int var3 = this.sourcePetSettingIndexAt(var1, var2);
         if (var3 >= 0) {
            this.sourcePetSettingIndex = var3;
         }

      }

      private void returnToSourceSkillBrowse(String var1) {
         if (this.text != null && this.text.sourceUiKind == 3) {
            this.text = null;
         }

         this.sourceSkillVisible = true;
         this.sourceSkillLearnMode = false;
         this.sourceSkillLearnConfirm = false;
         this.sourceSkillLearnDeclineConfirm = false;
         this.sourceSkillLearnReplaceMode = false;
         this.sourceSkillLearnReplaceConfirm = false;
         this.sourceSkillLearnReturnBrowse = false;
         this.sourceSkillRelearnMode = false;
         this.sourceSkillBrowseTab = 1;
         this.sourceSkillIndex = this.sourceSkillRelearnBrowseIndex;
         this.sourceSkillScroll = 0;
         this.sourceSkillLearnQueueIndex = -1;
         this.sourceSkillLearnPetIndices = new int[0];
         this.sourceSkillLearnQueueSkillIds = new int[0];
         this.sourceSkillLearnIds = new int[0];
         this.sourceSkillLearnCandidateIds = new int[0];
         this.sourceSkillLearnSelectedId = -1;
         this.refreshSourceSkillBrowseRows();
         this.session.story.trace().add("UNIFIED_DESIGN Skill relearn return browse reason=" + var1 + " index=" + this.sourceSkillIndex + " badges=" + this.session.inventory.currency.badges);
      }

      private void keepSourcePetSettingSelectionVisible() {
         int var1 = Math.max(0, this.sourcePetSettingCount - 6);
         if (this.sourcePetSettingIndex < this.sourcePetSettingScroll) {
            this.sourcePetSettingScroll = this.sourcePetSettingIndex;
         } else if (this.sourcePetSettingIndex >= this.sourcePetSettingScroll + 6) {
            this.sourcePetSettingScroll = this.sourcePetSettingIndex - 6 + 1;
         }

         this.sourcePetSettingScroll = Math.max(0, Math.min(var1, this.sourcePetSettingScroll));
      }

      private int sourcePetSettingIndexAt(int var1, int var2) {
         VqsvUiLayout var3 = VqsvUiLayout.load("petsetting.ui");
         int[] var4 = new int[]{5, 6, 7, 8, 10, 9};

         for(int var5 = 0; var5 < var4.length; ++var5) {
            int var6 = this.sourcePetSettingScroll + var5;
            if (var6 >= this.sourcePetSettingCount) {
               break;
            }

            VqsvUiLayout.UiWidget var7 = var3.widget(var4[var5]);
            if (var7 != null && var1 >= var7.x - 18 && var1 <= var7.x + Math.max(76, var7.w) + 8 && var2 >= var7.y - 2 && var2 <= var7.y + 14) {
               return var6;
            }
         }

         return -1;
      }

      private void hoverSourceItemChoice(int var1, int var2) {
         int var3 = this.sourceChoiceIndexAt(var1, var2, this.sourceItemChoiceScroll, this.sourceItemChoiceSize());
         if (var3 >= 0) {
            this.sourceItemChoiceIndex = var3;
         }

      }

      private void hoverSourceEquipmentChoice(int var1, int var2) {
         int var3 = this.sourceChoiceIndexAt(var1, var2, this.sourceEquipmentChoiceScroll, this.sourceEquipmentChoiceSize());
         if (var3 >= 0) {
            this.sourceEquipmentChoiceIndex = var3;
         }

      }

      private int sourceChoiceIndexAt(int var1, int var2, int var3, int var4) {
         VqsvUiLayout var5 = VqsvUiLayout.load("choice.ui");

         for(int var6 = 0; var6 < 5; ++var6) {
            VqsvUiLayout.UiWidget var7 = var5.widget(11 + var6 * 5);
            if (var7 != null && var1 >= var7.x - 4 && var1 <= var7.x + 136 && var2 >= var7.y - 2 && var2 <= var7.y + 14) {
               int var8 = var3 + var6;
               return var8 >= 0 && var8 < var4 ? var8 : -1;
            }
         }

         return -1;
      }

      private static boolean sourceLeftSoftkeyHit(int var0, int var1) {
         return var0 >= 0 && var0 <= 50 && var1 >= 288 && var1 < 320;
      }

      private static boolean sourceRightSoftkeyHit(int var0, int var1) {
         return var0 >= 190 && var0 < 240 && var1 >= 288 && var1 < 320;
      }

      void setCameraCenter(int var1, int var2) {
         VqsvSceneView.setCameraCenter(this, var1, var2);
      }

      void moveCameraToward(int var1, int var2, int var3) {
         VqsvSceneView.moveCameraToward(this, var1, var2, var3);
      }

      boolean cameraCenteredOn(int var1, int var2) {
         return VqsvSceneView.cameraCenteredOn(this, var1, var2);
      }

      void followActor(int var1) {
         VqsvSceneView.followActor(this, var1);
      }

      void stopCameraFollow() {
         VqsvSceneView.stopCameraFollow(this);
      }

      void updateCameraFollow() {
         VqsvSceneView.updateCameraFollow(this);
      }

      private static List<Event> makeEvents() {
         ArrayList var0 = new ArrayList();
         Scene0IntroScript.appendTo(var0);
         Scene1Room3EntryScript.appendTo(var0);
         Scene1Room0Group0Script.appendTo(var0);
         Scene1Room1BunnyScript.appendTo(var0);
         Scene1Room0Group2ElderScript.appendTo(var0);
         Scene1Room0Group3PetScript.appendTo(var0);
         Scene1Room0Group6ElderBattleScript.appendTo(var0);
         return var0;
      }

      void prepareTransition(int var1, int var2, int var3, int var4) {
         VqsvFreeWorldRuntime.prepareTransition(this, var1, var2, var3, var4);
      }

      void prepareTransition(int var1, int var2, int var3, int var4, int var5) {
         VqsvFreeWorldRuntime.prepareTransition(this, var1, var2, var3, var4, var5);
      }

      void markWorldTransition(int var1, int var2, int var3) {
         VqsvFreeWorldRuntime.markWorldTransition(this, var1, var2, var3);
      }

      boolean trySourceTransition(int var1, int var2, int var3, int var4, int var5) {
         return VqsvFreeWorldRuntime.trySourceTransition(this, var1, var2, var3, var4, var5);
      }

      private void reloadBlankRoom(int var1, int var2) {
         this.session.world.useMap = false;
         this.mapRenderer = null;
         this.session.world.followActorId = -1;
         Actor[] var3 = VqsvSceneActors.makeActors();

         for(int var4 = 0; var4 < var3.length; ++var4) {
            this.actors[var4] = var3[var4];
         }

         this.setCameraCenter(var1, var2);
      }

      void reloadBlankRoomCenteredOnActor(int var1) {
         this.reloadBlankRoom(0, 0);
         Actor var2 = this.actors[var1];
         this.setCameraCenter(var2.x, var2.y);
      }

      void loadBlankWorldRoom(int var1, int var2, int var3, int var4) {
         this.session.world.currentSceneId = var1;
         this.session.world.currentRoomIndex = var2;
         this.tempSprites.clear();
         this.worldUi.visible = false;
         this.worldUi.clearRoomTitle();
         this.reloadBlankRoom(var3, var4);
      }

      void loadScene7Room2(int var1, int var2) {
         VqsvSceneLoaders.loadScene7Room2(this, var1, var2);
      }

      void loadRoom1(int var1, int var2) {
         VqsvSceneLoaders.loadRoom1(this, var1, var2);
      }

      void loadScene5Room3(int var1, int var2) {
         VqsvSceneLoaders.loadScene5Room3(this, var1, var2);
      }

      void loadScene1Room3Entry(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room3Entry(this, var1, var2);
      }

      void loadScene1Room3FreeWorld(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room3FreeWorld(this, var1, var2);
      }

      void loadScene1Room4(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room4(this, var1, var2);
      }

      void loadScene1Room5(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room5(this, var1, var2);
      }

      void loadScene1Room6(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room6(this, var1, var2);
      }

      void loadScene1Room0(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room0(this, var1, var2);
      }

      void loadScene1Room1(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room1(this, var1, var2);
      }

      void loadScene1Room2(int var1, int var2) {
         VqsvSceneLoaders.loadScene1Room2(this, var1, var2);
      }

      void loadScene2Room1(int var1, int var2) {
         VqsvSceneLoaders.loadScene2Room1(this, var1, var2);
      }

      void loadWorldRoom(int var1, int var2, int var3, int var4) {
         VqsvSceneLoaders.loadWorldRoom(this, var1, var2, var3, var4);
      }

      void loadWorldRoomWithoutMapLoading(int var1, int var2, int var3, int var4) {
         VqsvSceneLoaders.loadWorldRoomWithoutMapLoading(this, var1, var2, var3, var4);
      }

      void loadScene11Room4(int var1, int var2) {
         VqsvSceneLoaders.loadScene11Room4(this, var1, var2);
      }

      void loadScene11Room5(int var1, int var2) {
         VqsvSceneLoaders.loadScene11Room5(this, var1, var2);
      }

      void loadScene11Room6(int var1, int var2) {
         VqsvSceneLoaders.loadScene11Room6(this, var1, var2);
      }

      void loadScene11Room7(int var1, int var2) {
         VqsvSceneLoaders.loadScene11Room7(this, var1, var2);
      }

      void spawnActorEffect(int var1, int var2) {
         if (var1 == -1 || var1 >= 0 && var1 < this.actors.length && this.actors[var1] != null) {
            this.tempSprites.add(new TempSprite(var1, var2, 120));
         }

      }

      void op5ActorEffect(int var1, int var2, int var3, int var4, int var5) {
         VqsvSourceEffects.op5ActorEffect(this, var1, var2, var3, var4, var5);
      }

      Blocking op24ViewportShake(int var1, int var2, int var3) {
         return VqsvSourceEffects.op24ViewportShake(this, var1, var2, var3);
      }

      void setPlayerPositionApprox(int var1, int var2) {
         VqsvFreeWorldRuntime.setPlayerPositionApprox(this, var1, var2);
      }

      void placePlayerAtTransitionActorApprox(int var1, int var2) {
         VqsvFreeWorldRuntime.placePlayerAtTransitionActorApprox(this, var1, var2);
      }

      void tickFreeWorldPlayer() {
         VqsvFreeWorldRuntime.tickFreeWorldPlayer(this);
      }

      void tickFreeWorldPlayerWithoutEncounters() {
         VqsvFreeWorldRuntime.tickFreeWorldPlayerWithoutEncounters(this);
      }

      boolean playerIntersectsSourceRect(int var1, int var2, int var3, int var4) {
         return VqsvFreeWorldRuntime.playerIntersectsSourceRect(this, var1, var2, var3, var4);
      }

      boolean playerSourcePointInRect(int var1, int var2, int var3, int var4) {
         return VqsvFreeWorldRuntime.playerSourcePointInRect(this, var1, var2, var3, var4);
      }

      boolean playerIntersectsActorSourceMask(int var1, boolean var2) {
         return VqsvFreeWorldRuntime.playerIntersectsActorSourceMask(this, var1, var2);
      }

      boolean playerIntersectsSourceTransitionMask(int var1) {
         return VqsvFreeWorldRuntime.playerIntersectsSourceTransitionMask(this, var1);
      }

      boolean playerInteractsActorSourceMask(int var1) {
         return VqsvFreeWorldRuntime.playerInteractsActorSourceMask(this, var1);
      }

      void stopPlayerForSourceEvent() {
         VqsvFreeWorldRuntime.stopPlayerForSourceEvent(this);
      }

      Blocking op17Item(int var1, int var2, int var3) {
         return VqsvSourceOps.op17Item(this, var1, var2, var3);
      }

      Blocking op18Material(int var1, int var2, int var3) {
         return VqsvSourceOps.op18Material(this, var1, var2, var3);
      }

      void op39RefreshPets() {
         VqsvSourceEffects.op39RefreshPets(this);
      }

      void op25SetGameFlag(int var1) {
         VqsvSourceEffects.op25SetGameFlag(this, var1);
      }

      Blocking op9SourceEffect(String var1, int... var2) {
         return VqsvSourceEffects.op9SourceEffect(this, var1, var2);
      }

      void op67SetBattleActor(int var1) {
         VqsvSourceEffects.op67SetBattleActor(this, var1);
      }

      Blocking op31CurrencyReward(int var1, int var2, int var3) {
         return VqsvSourceOps.op31CurrencyReward(this, var1, var2, var3);
      }

      Blocking op19SpecialReward(int var1, int var2) {
         return VqsvSourceOps.op19SpecialReward(this, var1, var2);
      }

      void op56ActorVisibility(int var1, int[] var2, int[] var3) {
         VqsvSourceEffects.op56ActorVisibility(this, var1, var2, var3);
      }

      void sourceSetMainTaskProgress(int var1, String var2) {
         this.session.story.mainTaskProgress = Math.max(0, var1);
         this.session.story.trace().add("PORTED source game.e.G main task progress=" + this.session.story.mainTaskProgress + " via " + var2);
      }

      void sourceAcceptBranchTask(int var1) {
         this.sourceBranchQuests.accept(this, var1);
      }

      void sourceUnlockBranchTask(int var1, int var2) {
         this.sourceBranchQuests.unlockOrUpdate(this, var1, var2);
      }

      void sourceCompleteBranchTask(int var1) {
         this.sourceBranchQuests.complete(this, var1);
      }

      void sourceRefreshBqTaskMarkers() {
         this.sourceBranchQuests.refreshBqTaskMarkers(this);
      }

      int sourceBranchTaskStatus(int var1) {
         return this.sourceBranchQuests.status(var1);
      }

      boolean sourcePetRecordObtained(int var1, int var2) {
         boolean var3 = this.session.progression.collection.collected(var2);
         this.session.story.trace().add("PORTED source game.j.a(category,species)=" + (var3 ? 2 : this.session.progression.collection.status(var2)) + " category=" + var1 + " species=" + var2);
         return var3;
      }

      Blocking op10PlayerTimedAction(int var1, int var2, int var3) {
         return new Op10PlayerTimedAction(var1, var2, var3);
      }

      private static final class EvolutionNoticeBlocking implements Blocking {
         final boolean detailed;

         EvolutionNoticeBlocking(boolean var1) {
            this.detailed = var1;
         }

         public boolean tick(Scene var1) {
            if (var1.text != null && var1.text.readyForKey && var1.consumeConfirm()) {
               var1.text.confirm();
               if (this.detailed) {
                  var1.session.progression.evolutionTutorialPending = true;
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }
}
