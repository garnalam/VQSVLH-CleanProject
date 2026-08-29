package vqsv.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vqsv.battle.BattleRequest;
import vqsv.battle.data.BadgeRow;
import vqsv.battle.data.BattleHeldItemRow;
import vqsv.battle.data.BattleItemRow;
import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.SpecialRewardRow;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.battlepass.BattlePassCatalog;
import vqsv.battlepass.BattlePassEngine;
import vqsv.battlepass.BattlePassLevel;
import vqsv.battlepass.BattlePassMission;
import vqsv.battlepass.BattlePassReward;
import vqsv.battlepass.BattlePassState;
import vqsv.charm.RainbowCharmCatalog;
import vqsv.charm.RainbowCharmService;
import vqsv.core.GameConfig;
import vqsv.data.ItemDefinition;
import vqsv.data.UnifiedBadgeCatalog;
import vqsv.data.UnifiedItemAliasRecord;
import vqsv.data.UnifiedItemCatalog;
import vqsv.data.UnifiedItemInventoryKind;
import vqsv.data.UnifiedItemRecord;
import vqsv.fashion.FashionAcquisitionService;
import vqsv.fashion.FashionEconomyCatalog;
import vqsv.fashion.FashionEconomyEntry;
import vqsv.fashion.FashionUiAssetCatalog;
import vqsv.fashion.SourceFashionCatalog;
import vqsv.fashion.SourceFashionRecord;
import vqsv.gameplay.DailyBadgeRewardService;
import vqsv.gameplay.EggService;
import vqsv.gameplay.PanelDecisionService;
import vqsv.gameplay.PetBankExpansionService;
import vqsv.gameplay.PortableShopService;
import vqsv.gameplay.RegionalChallengeService;
import vqsv.gameplay.WorldItemShopService;
import vqsv.gameplay.WorldPetRecoveryService;
import vqsv.gameplay.WorldRealtimeClock;
import vqsv.inventory.BagItemState;
import vqsv.inventory.EquipmentState;
import vqsv.inventory.MaterialStack;
import vqsv.inventory.SpecialRewardState;
import vqsv.pet.PetState;
import vqsv.progression.EvolutionCandidate;
import vqsv.progression.RegionalRematchState;
import vqsv.progression.RideProgression;
import vqsv.progression.UnifiedEvolutionCatalog;
import vqsv.quest.BranchTask;
import vqsv.quest.SourceTaskOfferCatalog;
import vqsv.render.SpriteAnimator;
import vqsv.render.UnifiedBadgeIconRenderer;
import vqsv.render.UnifiedItemIconRenderer;
import vqsv.resource.AssetPaths;
import vqsv.resource.ResourceLocator;
import vqsv.source.PetSourceAdapter;
import vqsv.text.VqsvText;
import vqsv.ui.layout.UiScrollbarMath;
import vqsv.ui.layout.VqsvUiLayout;
import vqsv.ui.text.TextBox;
import vqsv.ui.text.UiFont;
import vqsv.world.EggPreviewCatalog;
import vqsv.world.V4EggCatalog;
import vqsv.world.WorldTransmitEngine;

final class VqsvPanelRuntime {
   private static final int[] MENU_ROW_WIDGETS = new int[]{15, 5, 6, 7, 8, 9};
   private static final int GAME_MENU_VISIBLE_ROWS = 6;
   private static final int GAME_MENU_SCROLL_X = 157;
   private static final int GAME_MENU_SCROLL_Y = 105;
   private static final int GAME_MENU_SCROLL_W = 4;
   private static final int GAME_MENU_SCROLL_H = 100;
   private static final int[] SYSTEM_ROW_WIDGETS = new int[]{6, 7, 8, 9};
   private static final int[] PORTABLE_SHOP_ROW_WIDGETS = new int[]{16, 8, 9, 10};
   private static final int[] SOURCE_EAST_WHARF_ROW_WIDGETS = new int[]{5, 6, 7};
   private static final int[] SOURCE_TEMPLE_WHARF_ROW_WIDGETS = new int[]{5, 6, 7, 8, 9};
   private static final int[] SOURCE_CONVENIENCE_ROW_WIDGETS = new int[]{5, 6, 7, 8, 9};
   private static final int[] SOURCE_PET_BANK_ROW_WIDGETS = new int[]{6, 7, 9};
   private static final int[] SOURCE_WORLD_SHOP_ROW_WIDGETS = new int[]{6, 7, 9, 8};
   private static final int[] SHOPBUY_ROW_BACKGROUNDS = new int[]{12, 17, 22, 27, 32};
   private static final int[] SHOPBUY_ROW_ICONS = new int[]{51, 52, 53, 54, 55};
   private static final int[] SHOPBUY_ROW_NAMES = new int[]{14, 19, 24, 29, 34};
   private static final int[] SHOPBUY_ROW_PRICES = new int[]{15, 20, 25, 30, 35};
   private static final int[] SHOPBUY_ROW_CURRENCIES = new int[]{45, 46, 47, 48, 49};
   private static final int[] BAG_ROW_BACKGROUNDS = new int[]{17, 22, 27, 32, 37};
   private static final int[] BAG_ROW_ICONS = new int[]{18, 23, 28, 33, 38};
   private static final int[] BAG_ROW_NAMES = new int[]{19, 24, 29, 34, 39};
   private static final int[] BAG_ROW_COUNTS = new int[]{20, 25, 30, 35, 40};
   private static final int[] BAG_TAB_WIDGETS = new int[]{9, 10, 11, 12};
   private static final int[] BAG_EQUIP_ROW_BACKGROUNDS = new int[]{58, 63, 68, 73, 78};
   private static final int[] BAG_EQUIP_ROW_ICONS = new int[]{59, 64, 69, 74, 79};
   private static final int[] BAG_EQUIP_ROW_NAMES = new int[]{60, 65, 70, 75, 80};
   private static final int[] BAG_EQUIP_ROW_STATUS = new int[]{61, 66, 71, 76, 81};
   private static final int[] BAG_MATERIAL_ROW_BACKGROUNDS = new int[]{97, 102, 107, 112, 117};
   private static final int[] BAG_MATERIAL_ROW_ICONS = new int[]{98, 103, 108, 113, 118};
   private static final int[] BAG_MATERIAL_ROW_NAMES = new int[]{99, 104, 109, 114, 119};
   private static final int[] BAG_MATERIAL_ROW_COUNTS = new int[]{100, 105, 110, 115, 120};
   private static final int[] BAG_SPECIAL_ROW_ICONS = new int[]{137, 142, 147, 152, 157};
   private static final int[] BAG_SPECIAL_ROW_NAMES = new int[]{138, 143, 148, 153, 158};
   private static final int[] BAG_SPECIAL_ROW_COUNTS = new int[]{139, 144, 149, 154, 159};
   private static final int[] TRANSMIT_ROW_WIDGETS = new int[]{5, 6, 7, 8, 9};
   private static final String[] TRANSMIT_DESTINATIONS = new String[]{"Thủy Kimura", "Bích Thủy thành", "Nguyên Mộc Thành", "Niêm Thổ Thành", "Hắc Thạch thành", "Thiên không", "Xa cổ"};
   private static final EggService EGG_SERVICE = new EggService();
   private static final PanelDecisionService PANEL_DECISIONS = new PanelDecisionService();
   private static final PortableShopService PORTABLE_SHOP_SERVICE = new PortableShopService();
   private static final DailyBadgeRewardService DAILY_BADGE_REWARD_SERVICE = new DailyBadgeRewardService();
   private static final FashionAcquisitionService FASHION_ACQUISITION_SERVICE = new FashionAcquisitionService();
   private static final WorldItemShopService WORLD_ITEM_SHOP_SERVICE = new WorldItemShopService();
   private static final WorldPetRecoveryService WORLD_PET_RECOVERY_SERVICE = new WorldPetRecoveryService();
   private static final WorldTransmitEngine TRANSMIT_ENGINE = new WorldTransmitEngine();
   private static final RegionalChallengeService REGIONAL_CHALLENGES = new RegionalChallengeService();
   private static final GiftCodeService GIFT_CODE_SERVICE = new GiftCodeService();
   private static final BattlePassEngine BATTLE_PASS_ENGINE = new BattlePassEngine();
   private static final int[] TASK_ROW_BACKGROUNDS = new int[]{11, 16, 21, 26, 31};
   private static final int[] TASK_ROW_NUMBERS = new int[]{12, 17, 22, 27, 32};
   private static final int[] TASK_ROW_NAMES = new int[]{13, 18, 23, 28, 33};
   private static final int[] TASK_ROW_STATUS = new int[]{14, 19, 24, 29, 34};
   private static final int TASK_TAB_SELECTED_COLOR = 11290624;
   static final int RIDE_CLOSE_X = 6;
   static final int RIDE_CLOSE_Y = 6;
   static final int RIDE_CLOSE_SIZE = 22;
   private static final int[] PETMAP_TAB_CELLS = new int[]{6, 7, 8, 9, 10, 11, 12};
   private static final int[] PETMAP_TAB_LABELS = new int[]{13, 14, 15, 16, 17, 18, 19};
   private static final int[] PETMAP_ROW_BACKGROUNDS = new int[]{25, 29, 33, 37, 41};
   private static final int[] PETMAP_ROW_MARKERS = new int[]{44, 45, 46, 47, 48};
   private static final int[] PETMAP_ROW_NAMES = new int[]{27, 31, 35, 39, 43};
   private static final int[] BADGE_SLOT_WIDGETS = new int[]{17, 18, 19, 20, 21, 22, 23, 24};
   private static final int[] BADGE_ICON_WIDGETS = new int[]{25, 26, 27, 28, 29, 30, 31, 32};
   private static final int BADGE_GRID_COLUMNS = 5;
   private static final int BADGE_GRID_ROWS = 2;
   private static final int BADGE_GRID_CAPACITY = 10;
   private static final int BADGE_GRID_X = 50;
   private static final int BADGE_GRID_Y = 99;
   private static final int BADGE_GRID_CELL_SIZE = 30;
   private static final int BADGE_GRID_ICON_SIZE = 28;
   private static final int BADGE_GRID_X_STEP = 28;
   private static final int BADGE_GRID_Y_STEP = 33;
   private static final int BADGE_PREVIEW_X = 125;
   private static final int BADGE_PREVIEW_Y = 174;
   private static final int BADGE_PREVIEW_SIZE = 64;
   private static final Map<Integer, BufferedImage> BADGE_PREVIEW_CACHE = new HashMap();
   private static final String[] PETMAP_TAB_NAMES = new String[]{"Hỏa", "Mộc", "Thổ", "Thủy", "Điện", "Quỷ", "Phong"};
   private static final Pattern TASK_TEXT_PATTERN = Pattern.compile("\\[\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\]");
   private static final int TASK_ROUTE_UNKNOWN_INDEX = 20;
   private static final int[][] TASK_ROUTE_LINKS = new int[][]{{0, 1}, {0, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {5, 7}, {7, 8}, {7, 9}, {9, 10}, {10, 11}, {10, 12}, {10, 13}, {10, 14}, {11, 17}, {14, 15}, {14, 16}, {16, 18}, {9, 19}};
   private static final TaskMapNode[] TASK_ROUTE_NODES = new TaskMapNode[]{new TaskMapNode(1, 0, "Thủy Mộc Thôn", 12, 97, new String[]{"Thủy Mộc", "Thôn"}), new TaskMapNode(1, 1, "Đường nhỏ phía Đông", 56, 97, new String[]{"Đường", "phía Đông"}), new TaskMapNode(1, 2, "Đường nhỏ phía Nam", 12, 121, new String[]{"Đường", "phía Nam"}), new TaskMapNode(1, 3, "Đường nhỏ Thủy Mộc", 56, 121, new String[]{"Đường", "Mộc Thủy"}), new TaskMapNode(1, 5, "Bến tàu Thủy Mộc", 100, 121, new String[]{"Bến tàu", "Thủy Mộc"}), new TaskMapNode(2, 1, "Bích Thủy Thành", 144, 121, new String[]{"Bích Thủy", "Thành"}), new TaskMapNode(9, 0, "Bích Thủy Đạo Quán", 188, 121, new String[]{"Đạo quán", "Bích Thủy"}), new TaskMapNode(3, 0, "Nguyên Mộc Thành", 144, 145, new String[]{"Nguyên Mộc", "Thành"}), new TaskMapNode(9, 1, "Nguyên Mộc Đạo Quán", 100, 145, new String[]{"Đạo quán", "Nguyên Mộc"}), new TaskMapNode(4, 0, "Bến tàu phía Đông", 144, 169, new String[]{"Bến Đông", "Nguyên Mộc"}), new TaskMapNode(4, 5, "Trung tâm Niêm Thổ", 188, 169, new String[]{"Trung tâm", "Niêm Thổ"}), new TaskMapNode(4, 6, "Khu chợ phía Nam", 12, 169, new String[]{"Chợ Nam", "Niêm Thổ"}), new TaskMapNode(4, 8, "Căn cứ Phi Không Đĩnh", 56, 169, new String[]{"Phi Không", "Đĩnh"}), new TaskMapNode(9, 2, "Niêm Thổ Đạo Quán", 12, 193, new String[]{"Đạo quán", "Niêm Thổ"}), new TaskMapNode(5, 1, "Hắc Thạch Thành", 56, 193, new String[]{"Hắc Thạch", "Thành"}), new TaskMapNode(9, 4, "Hắc Thạch Đạo Quán", 100, 193, new String[]{"Đạo quán", "Hắc Thạch"}), new TaskMapNode(5, 4, "Địa lao Hắc Thạch", 144, 193, new String[]{"Địa lao", "Hắc Thạch"}), new TaskMapNode(4, 4, "Mỏ quặng Niêm Thổ", 100, 97, new String[]{"Mỏ quặng", "Niêm Thổ"}), new TaskMapNode(10, 13, "Phượng Hoàng Di Tích", 12, 145, new String[]{"Phượng", "Di Tích"}), new TaskMapNode(6, 1, "Hắc Long Thần Điện", 188, 193, new String[]{"Hắc Long", "Thần Điện"}), new TaskMapNode(-1, -1, "Chưa định tuyến", 188, 97, new String[]{"Chưa", "định tuyến"})};
   private static final String[] MENU_LABELS = new String[]{"Tùy thân cửa hàng", "Sủng vật", "Lưng bao", "Đồ giám", "Nhiệm vụ", "Lưu dữ liệu", "Thời trang", "Tái đấu & Giftcode", "Thẻ Fan Cứng VQSV"};
   private static final String[] SYSTEM_LABELS = new String[]{"Tiếp tục trò chơi", "Trợ giúp chơi", "Thiết lập trò chơi", "Trở lại menu chính"};
   private static final String[] PORTABLE_SHOP_LABELS = new String[]{"Thương điếm bình dân", "Nhận 10 huy hiệu mỗi ngày", "Thương điếm thời trang", "Cửa hàng tài liệu"};
   private static final String[] SOURCE_CONVENIENCE_LABELS = new String[]{"Dẫn thưởng", "Tiến hóa", "Dị hóa", "Tài liệu", "Cách mở"};
   private static final String[] BICH_THUY_ENVOY_CONVENIENCE_LABELS = new String[]{"Dẫn thưởng", "Tiến hóa", "Dị hóa", "Tài liệu", "Bùa Hộ Trận", "Mở rộng kho Pet", "Cách mở"};
   private static final String[] SOURCE_WHARF_TITLES = new String[]{"Bến tàu phía Nam", "Bến tàu phía Đông", "Bến tàu phía Tây", "Hắc Thạch Mã Đầu", "Thần Điện Mã Đầu"};
   private static final String[][] SOURCE_WHARF_LABELS = new String[][]{{"Hắc Thạch Thành", "Hắc Long Thần Điện", "Không ra hàng"}, {"Niêm Thổ Thành", "Hắc Long Thần Điện", "Không ra hàng"}, {"Nguyên Mộc Thành", "Hắc Long Thần Điện", "Không ra hàng"}, {"Bắc Bích Thủy Thành", "Hắc Long Thần Điện", "Không ra hàng"}, {"Bắc Bích Thủy Thành", "Nguyên Mộc Thành", "Niêm Thổ Thành", "Hắc Thạch Thành", "Không ra hàng"}};
   private static final int[][][] SOURCE_WHARF_ROUTES = new int[][][]{{{5, 6, 1, 5, 2, 112, 224, 2, 2}, {1, 0, 10, 6, 0, 112, 224, 2, 0}}, {{3, 6, 3, 4, 0, 48, 176, 2, 2}, {1, 0, 10, 6, 0, 112, 224, 2, 2}}, {{3, 6, 3, 3, 6, 288, 224, 3, 0}, {1, 0, 10, 6, 0, 112, 224, 2, 2}}, {{5, 6, 1, 1, 5, 272, 128, 3, 0}, {1, 0, 10, 6, 0, 112, 224, 2, 0}}, {{0, 0, 0, 1, 5, 272, 128, 3, 2}, {0, 0, 0, 3, 6, 288, 224, 3, 0}, {0, 0, 0, 4, 0, 48, 176, 2, 0}, {0, 0, 0, 5, 2, 112, 224, 2, 2}}};
   private static final String[] SOURCE_PET_BANK_LABELS = new String[]{"Gởi lại", "Lấy ra", "Phóng sinh"};
   private static final String[] SOURCE_WORLD_SHOP_LABELS = new String[]{"Mua sắm", "Bán đi", "Khôi phục", "Rời đi"};
   private static final int MATERIAL_SHOP_BODY_ROW = 3;
   private static final int MATERIAL_SHOP_BODY_ROW_X = 70;
   private static final int MATERIAL_SHOP_BODY_ROW_Y = 158;
   private static final int MATERIAL_SHOP_BODY_ROW_W = 108;
   private static final String[] RIDE_LABELS = new String[]{"Lục đi điểu", "Hư không hành giả", "Hải âu", "Nham sơn long"};
   private static final String[] MENU_TITLE_TOKENS = new String[]{"#P605", "#P606", "#P607", "#P608", "#P609", "#P610", "Thời trang", "Quà tặng", "Thẻ Fan Cứng"};
   private static final int WARDROBE_VISIBLE_ROWS = 5;
   private static final int WARDROBE_LIST_X = 6;
   private static final int WARDROBE_LIST_Y = 38;
   private static final int WARDROBE_LIST_W = 112;
   private static final int WARDROBE_ROW_H = 38;
   private static final int WARDROBE_SCROLL_X = 120;
   private static final int WARDROBE_SCROLL_W = 6;
   private static final int WARDROBE_PREVIEW_X = 130;
   private static final int WARDROBE_PREVIEW_Y = 38;
   private static final int WARDROBE_PREVIEW_W = 104;
   private static final int WARDROBE_PREVIEW_H = 145;
   private static final String[] FASHION_SHOP_LABELS = new String[]{"Mua túi mù - 500 K", "Mở túi", "Đổi mảnh", "Rời đi"};
   private static final int[][] FASHION_MENU_CARDS = new int[][]{{106, 66, 126, 48}, {106, 118, 126, 48}, {106, 170, 126, 48}, {8, 250, 224, 28}};
   private static final int[] FASHION_REVEAL_FRAME_TICKS = new int[]{4, 2, 2, 2, 1, 2, 2, 3, 6};
   private static final int FASHION_REVEAL_TOTAL_TICKS = 24;
   private static final int FASHION_CONFIRM_ACTION_X = 42;
   private static final int FASHION_CONFIRM_ACTION_Y = 196;
   private static final int FASHION_CONFIRM_ACTION_W = 156;
   private static final int FASHION_CONFIRM_ACTION_H = 32;
   private static final int FASHION_EXCHANGE_ACTION_X = 128;
   private static final int FASHION_EXCHANGE_ACTION_Y = 226;
   private static final int FASHION_EXCHANGE_ACTION_W = 104;
   private static final int FASHION_EXCHANGE_ACTION_H = 30;
   private static final int FASHION_EXCHANGE_VISIBLE_ROWS = 5;
   private static final int FASHION_EXCHANGE_LIST_X = 8;
   private static final int FASHION_EXCHANGE_LIST_Y = 64;
   private static final int FASHION_EXCHANGE_LIST_W = 112;
   private static final int FASHION_EXCHANGE_ROW_H = 36;
   private static final int FASHION_EXCHANGE_SCROLL_X = 121;
   private static final int FASHION_EXCHANGE_SCROLL_W = 4;
   boolean visible;
   int selected;
   int openedTicks;
   private Mode mode;
   private int taskTab;
   private int taskMapReturnTab;
   private int taskMapReturnSelected;
   private int taskMapReturnScroll;
   private String taskMapTitle;
   private String taskMapDetail;
   private int taskMapNumber;
   private boolean taskReturnToWorld;
   private int taskSelectedBeforeOption;
   private boolean taskOptionReturnToTask;
   private int taskOptionBranchTaskId;
   private TaskOptionData taskOptionData;
   private String taskSelectedLabelCache;
   private int recordSelected;
   private int recordMessageMode;
   private int petmapTab;
   private boolean petmapReturnToBag;
   private int petmapReturnBagSelected;
   private int petmapReturnBagScroll;
   private int savePhase;
   private int helpPage;
   private int settingsLevel;
   private int bagTab;
   private boolean badgeReturnToBag;
   private boolean badgeAwardReturnToWorld;
   private int badgeReturnBagSelected;
   private int badgeReturnBagScroll;
   private int transmitReturnBagSelected;
   private int transmitReturnBagScroll;
   private int rideSelected;
   private int listScroll;
   private int sourceWharfIndex;
   private boolean sourceConvenienceBankExpansionEnabled;
   private int rainbowCharmTab;
   private int rainbowCharmPendingId;
   private String rainbowCharmMessage;
   private boolean rainbowCharmReturnToBag;
   private int rainbowCharmReturnBagSelected;
   private int rainbowCharmReturnBagScroll;
   private int battlePassTrack;
   private String battlePassMessage;
   private Mode scrollbarDragMode;
   private int scrollbarGrabOffset;
   private int bagMessageMode;
   private boolean eggPickerOpen;
   private int[] eggPickerRewardIds;
   private boolean eggPreviewOpen;
   private int eggPreviewItemId;
   private int eggPreviewScroll;
   private int rideMessageMode;
   private int shopConfirmItemId;
   private int shopConfirmQuantity;
   private int shopConfirmTotal;
   private int shopConfirmCurrency;
   private int shopTable;
   private byte shopBucket;
   private boolean portableShopReturnToWorld;
   private boolean portableShopReturnToSourceWorldShop;
   private boolean portableShopReturnToSourceConvenience;
   private int sourceSellItemId;
   private int sourceSellQuantity;
   private int sourceSellTotal;
   private int sourceSellCurrency;
   private String sourceSellSelectedLabel;
   private int sourceRecoveryCost;
   private int sourceRecoverySavePhase;
   private String wardrobePreviewKey;
   private SpriteAnimator wardrobePreviewAnimator;
   private final Map<String, SpriteAnimator> wardrobeThumbnailAnimators;
   private int serviceProductId;
   private String serviceConfirmTitle;
   private String serviceConfirmPrompt;
   private int fashionPurchaseQuantity;
   private FashionAcquisitionService.PurchaseQuote fashionPurchaseQuote;
   private FashionAcquisitionService.OpenPlan fashionOpenPlan;
   private FashionAcquisitionService.ExchangePlan fashionExchangePlan;
   private String fashionRevealKey;
   private String fashionRevealTier;
   private String fashionRevealTitle;
   private String fashionRevealDetail;
   private boolean fashionRevealReturnExchange;
   private String pendingBagOpenBoxMessage;
   private int pendingHatchSpecies;
   private int pendingHatchStorageResult;
   private String saveMessage;
   private int challengeRegion;
   private String giftCodeInput;
   private String giftCodeMessage;
   private boolean titlePanelMode;
   private static List<String> mainTaskRows;
   private static List<String> branchTaskRows;
   private static List<String> chsRows;

   VqsvPanelRuntime() {
      this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
      this.taskMapTitle = "Nhiệm vụ";
      this.taskMapDetail = "";
      this.taskOptionBranchTaskId = -1;
      this.taskOptionData = VqsvPanelRuntime.TaskOptionData.empty();
      this.taskSelectedLabelCache = "Nhiệm vụ";
      this.rainbowCharmPendingId = -1;
      this.rainbowCharmMessage = "";
      this.battlePassMessage = "";
      this.eggPickerRewardIds = new int[0];
      this.shopConfirmItemId = -1;
      this.shopConfirmQuantity = 1;
      this.shopTable = 4;
      this.shopBucket = 0;
      this.sourceSellItemId = -1;
      this.sourceSellQuantity = 1;
      this.sourceSellSelectedLabel = "Bán ra";
      this.sourceRecoveryCost = -1;
      this.wardrobeThumbnailAnimators = new HashMap();
      this.serviceProductId = -1;
      this.serviceConfirmTitle = "";
      this.serviceConfirmPrompt = "";
      this.fashionPurchaseQuantity = 1;
      this.fashionRevealTitle = "";
      this.fashionRevealDetail = "";
      this.pendingBagOpenBoxMessage = "";
      this.pendingHatchSpecies = -1;
      this.pendingHatchStorageResult = -1;
      this.saveMessage = "";
      this.challengeRegion = -1;
      this.giftCodeInput = "";
      this.giftCodeMessage = "Nhập mã do nhà phát hành cung cấp.";
   }

   void open(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
      this.selected = clamp(this.selected, 0, MENU_LABELS.length - 1);
      this.resetGameMenuViewport();
      this.openedTicks = 0;
      this.titlePanelMode = false;
      this.badgeAwardReturnToWorld = false;
      this.taskReturnToWorld = false;
      var1.session.story.trace().add("PORTED/PARTIAL panel game.k P=6 game.h.k gamemenu.ui open selected=" + this.selected + " titleToken=" + MENU_TITLE_TOKENS[this.selected] + " money=" + var1.session.inventory.currency.money + " badges=" + var1.session.inventory.currency.badges);
   }

   void openSourceBadgeAward(VqsvGameRuntime.Scene var1, int var2, String var3) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.BADGE;
      this.selected = badgeDisplayIndexForRuntimeId(var1, var2);
      this.openedTicks = 0;
      this.titlePanelMode = false;
      this.badgeReturnToBag = false;
      this.badgeAwardReturnToWorld = true;
      var1.session.story.trace().add("PORTED source game.e opcode53 badge.ui award badgeId=" + var2 + " displayIndex=" + this.selected + " description=" + var3 + " return=blocking-world-event");
   }

   void openMenuAt(VqsvGameRuntime.Scene var1, int var2, String var3) {
      this.selected = clamp(var2, 0, MENU_LABELS.length - 1);
      this.open(var1);
      var1.session.story.trace().add("PORTED/PARTIAL panel gamemenu reopened selected=" + this.selected + " reason=" + var3);
   }

   void openBranchTaskAcceptOption(VqsvGameRuntime.Scene var1, int var2, boolean var3) {
      this.visible = true;
      this.taskSelectedBeforeOption = this.selected;
      this.taskOptionReturnToTask = var3;
      this.taskOptionBranchTaskId = var2;
      this.mode = VqsvPanelRuntime.Mode.TASK_OPTION;
      this.selected = 0;
      this.openedTicks = 0;
      this.taskOptionData = VqsvPanelRuntime.TaskOptionData.branchTask(var2);
      var1.session.story.trace().add("PORTED/PARTIAL source game.e opcode49 taskOption.ui open taskId=" + var2 + " rewards=" + this.taskOptionData.rewards.length + " options=" + this.taskOptionData.options.length + " returnToTask=" + var3);
   }

   void openGameSystemFromWorld(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.GAMESYSTEM;
      this.selected = 0;
      this.openedTicks = 0;
      this.titlePanelMode = false;
      var1.session.story.trace().add("PORTED/PARTIAL world.ui left softkey source game.k P=0 key=131072 -> P=13 game.h.m gamesystem.ui open");
   }

   void openRideFromWorld(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.RIDE;
      this.rideSelected = var1.session.progression.ride.activeIndex >= 0 ? var1.session.progression.ride.activeIndex : 0;
      this.rideMessageMode = 0;
      this.openedTicks = 0;
      this.titlePanelMode = false;
      var1.session.story.trace().add("PORTED source key9=512 opens ride.ui selectedRide=" + this.rideSelected + " scene=[" + var1.session.world.currentSceneId + "," + var1.session.world.currentRoomIndex + "]");
   }

   void openTaskFromWorld(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.TASK;
      this.taskTab = 0;
      this.listScroll = 0;
      this.selected = mainTaskCursor(var1);
      this.keepSelectedVisible(taskRowsForRender(var1, this.taskTab).size());
      this.openedTicks = 0;
      this.titlePanelMode = false;
      this.taskReturnToWorld = true;
      this.updateTaskSelectedLabel(var1);
      var1.session.story.trace().add("PORTED source world.ui task shortcut animation=6 widget=2 -> task.ui main tab selected=" + this.selected);
   }

   void openTitleHelp(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.HELP;
      this.selected = 0;
      this.helpPage = 0;
      this.openedTicks = 0;
      this.titlePanelMode = true;
      var1.session.story.trace().add("PC_QOL title menu opens source-style help1.ui");
   }

   void openTitleSettings(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.SETTINGS;
      this.selected = 0;
      this.openedTicks = 0;
      this.titlePanelMode = true;
      var1.session.story.trace().add("PC_QOL title menu opens source-style help.ui settings speedX2=" + var1.session.runtime.speedX2);
   }

   void openSourceMaterialShopFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.titlePanelMode = false;
      this.openPortableShopBuy(var1, 3, (byte)2, "PORTED/PARTIAL world source actor type20 actor=" + var2 + " scene=[" + var1.session.world.currentSceneId + "," + var1.session.world.currentRoomIndex + "] -> game.l case2 -> game.k.a(3,(byte)2) shopbuy.ui", true);
   }

   void openSourceVillageItemShopFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.titlePanelMode = false;
      this.openPortableShopBuy(var1, 4, (byte)0, "PORTED/PARTIAL world source scene11 room6 actor sprite24 actor=" + var2 + " scene=[" + var1.session.world.currentSceneId + "," + var1.session.world.currentRoomIndex + "] -> game.l case2 -> game.k.a(4,(byte)0) shopbuy.ui", true);
   }

   void openSourceSouthWharfShopFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.openSourceWorldShopFromWorld(var1, var2, 4, (byte)0);
   }

   void openSourceWorldShopFromWorld(VqsvGameRuntime.Scene var1, int var2, int var3, byte var4) {
      if (var3 == 3 && var4 == 2 || var3 == 4 && var4 == 0) {
         this.visible = true;
         this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP;
         this.selected = 0;
         this.listScroll = 0;
         this.openedTicks = 0;
         this.titlePanelMode = false;
         this.shopTable = var3;
         this.shopBucket = var4;
         this.portableShopReturnToWorld = false;
         this.portableShopReturnToSourceWorldShop = false;
         this.portableShopReturnToSourceConvenience = false;
         var1.session.world.worldEventActor = var2;
         var1.session.story.trace().add("PORTED source world shop actor=" + var2 + " -> game.l state1 game.k.F shop.ui buyRoute=[" + var3 + "," + var4 + "] rows=[Mua sam,Ban di,Khoi phuc,Roi di]");
      } else {
         throw new IllegalArgumentException("Unsupported source world shop route " + var3 + "," + var4);
      }
   }

   void openSourceNguyenMocConvenienceFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.openSourceConvenienceFromWorld(var1, var2);
   }

   void openSourceConvenienceFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP;
      this.selected = 0;
      this.listScroll = 0;
      this.openedTicks = 0;
      this.titlePanelMode = false;
      this.portableShopReturnToWorld = false;
      this.portableShopReturnToSourceWorldShop = false;
      this.portableShopReturnToSourceConvenience = false;
      this.sourceConvenienceBankExpansionEnabled = BichThuyHubRuntime.isPetBankExpansionEnvoy(var1, var2);
      var1.session.world.worldEventActor = var2;
      List var10000 = var1.session.story.trace();
      String var10001 = Arrays.toString(this.sourceConvenienceLabels());
      var10000.add("PORTED source sprite17 convenience shop -> game.l state27 game.k.aS wharf2.ui title=Tien loi diem rows=" + var10001 + " actor=" + var2);
   }

   void openSourceWharfFromWorld(VqsvGameRuntime.Scene var1, int var2, int var3) {
      if (var3 >= 0 && var3 < SOURCE_WHARF_LABELS.length) {
         this.visible = true;
         this.mode = VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF;
         this.sourceWharfIndex = var3;
         this.selected = 0;
         this.listScroll = 0;
         this.openedTicks = 0;
         this.titlePanelMode = false;
         var1.session.world.worldEventActor = var2;
         var1.session.story.trace().add("PORTED source sprite68 wharf captain -> game.l state28 game.k.aP " + (var3 == 4 ? "wharf2.ui" : "wharf1.ui") + " Q=" + var3 + " R=0 titleRow=" + (616 + var3) + " actor=" + var2);
      } else {
         throw new IllegalArgumentException("Source wharf index must be Q0-Q4");
      }
   }

   void reopenSourceNguyenMocConvenienceAfterPetstate(VqsvGameRuntime.Scene var1, int var2) {
      this.openSourceNguyenMocConvenienceFromWorld(var1, var1.session.world.worldEventActor);
      this.selected = clamp(var2, 1, 2);
      var1.session.story.trace().add("PORTED source game.l state7 petstate back -> state27 wharf2.ui selected=" + this.selected + " actor=" + var1.session.world.worldEventActor);
   }

   void openSourceVillagePetBankFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.SOURCE_PET_BANK;
      this.selected = 0;
      this.openedTicks = 0;
      this.titlePanelMode = false;
      var1.session.world.worldEventActor = var2;
      var1.session.story.trace().add("PORTED/PARTIAL world source actor sprite25 pet bank actor=" + var2 + " scene=[" + var1.session.world.currentSceneId + "," + var1.session.world.currentRoomIndex + "] -> game.l case16 -> game.k.D shop.ui title=Ngân hàng Sủng vật");
   }

   void openSourceBichThuyHeldItemShopFromWorld(VqsvGameRuntime.Scene var1, int var2) {
      this.openSourceWorldShopFromWorld(var1, var2, 3, (byte)2);
      var1.session.story.trace().add("REPAIR scene11 room8 tutorial held-item service actor=" + var2 + " -> full shop.ui menu before material shopbuy.ui");
   }

   void close(VqsvGameRuntime.Scene var1) {
      if (this.visible) {
         this.visible = false;
         this.titlePanelMode = false;
         this.badgeAwardReturnToWorld = false;
         List var10000 = var1.session.story.trace();
         String var10001 = this.closeTrace();
         var10000.add("PORTED/PARTIAL panel " + var10001 + " selected=" + this.selected);
      }
   }

   void suspendForSourceSkillLearn(VqsvGameRuntime.Scene var1, boolean var2) {
      if (this.visible) {
         this.visible = false;
         if (var2) {
            this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP;
            this.selected = portableShopProductRow(3);
         }

         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel suspended for source choiceskill.ui returnPortableShop=" + var2 + " selected=" + this.selected);
      }
   }

   void reopenPortableShopAfterSourceSkillLearn(VqsvGameRuntime.Scene var1) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP;
      this.selected = portableShopProductRow(3);
      this.openedTicks = 0;
      this.titlePanelMode = false;
      var1.session.story.trace().add("PORTED/PARTIAL panel reopen bodyShop.ui after source choiceskill.ui selected=" + this.selected);
   }

   void tick(VqsvGameRuntime.Scene var1) {
      if (this.visible) {
         ++this.openedTicks;
         if (var1.text == null || this.mode == VqsvPanelRuntime.Mode.BAG && this.bagMessageMode != 0 || this.mode == VqsvPanelRuntime.Mode.RECORD && this.recordMessageMode != 0) {
            if (this.mode == VqsvPanelRuntime.Mode.SAVE) {
               this.tickSave(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
               if (!this.eggPreviewOpen) {
                  this.tickBag(var1);
                  consumeKeys(var1);
               } else {
                  if (!var1.keyBack && !var1.key0) {
                     if (!var1.keyUp && !var1.keyLeft) {
                        if (var1.keyDown || var1.keyRight) {
                           this.eggPreviewScroll = Math.min(this.eggPreviewMaxScroll(), this.eggPreviewScroll + 1);
                        }
                     } else {
                        this.eggPreviewScroll = Math.max(0, this.eggPreviewScroll - 1);
                     }
                  } else {
                     this.eggPreviewOpen = false;
                     this.eggPreviewScroll = 0;
                     var1.session.story.trace().add("UNIFIED-DESIGN egg preview close itemId=" + this.eggPreviewItemId);
                  }

                  consumeKeys(var1);
               }
            } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
               this.tickTask(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
               this.tickTaskMap(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
               this.tickTaskOption(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
               this.tickRecord(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
               this.tickPetmap(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
               this.tickBadge(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
               this.tickHelp(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
               this.tickSettings(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
               this.tickOptionConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
               this.tickRide(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
               this.tickTransmit(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
               this.tickPortableShop(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
               this.tickPortableShopBuy(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
               this.tickPortableShopConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
               this.tickPortableShopServiceConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
               this.tickFashionShop(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
               this.tickFashionBuyConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM) {
               this.tickFashionOpenConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
               this.tickFashionReveal(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
               this.tickFashionExchange(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
               this.tickFashionExchangeConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
               this.tickSourceEastWharf(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
               this.tickSourceConvenienceShop(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.RAINBOW_CHARM) {
               this.tickRainbowCharm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.RAINBOW_CHARM_CONFIRM) {
               this.tickRainbowCharmConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
               this.tickSourceWorldShop(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
               this.tickSourceWorldShopSell(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
               this.tickSourceWorldShopSellConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
               this.tickSourceWorldShopRecoverConfirm(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
               this.tickSourcePetBank(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
               this.tickWardrobe(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.CHALLENGE) {
               this.tickChallenge(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.GIFT_CODE) {
               this.tickGiftCode(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS) {
               this.tickBattlePass(var1);
               consumeKeys(var1);
            } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS_HELP) {
               this.tickBattlePassHelp(var1);
               consumeKeys(var1);
            } else {
               int var4 = this.labels().length - 1;
               if (var1.keyUp) {
                  int var3 = this.selected;
                  this.selected = clamp(this.selected - 1, 0, var4);
                  if (this.selected != var3) {
                     List var10000 = var1.session.story.trace();
                     String var10001 = this.sourceTickMethod();
                     var10000.add("PORTED/PARTIAL panel " + var10001 + " key=4100 selected=" + this.selected + this.titleTraceSuffix());
                  }
               } else if (var1.keyDown) {
                  int var5 = this.selected;
                  this.selected = clamp(this.selected + 1, 0, var4);
                  if (this.selected != var5) {
                     List var6 = var1.session.story.trace();
                     String var7 = this.sourceTickMethod();
                     var6.add("PORTED/PARTIAL panel " + var7 + " key=8448 selected=" + this.selected + this.titleTraceSuffix());
                  }
               } else if (var1.key0) {
                  this.confirm(var1);
               } else if (var1.keyBack) {
                  this.close(var1);
               }

               if (this.mode == VqsvPanelRuntime.Mode.GAMEMENU) {
                  this.keepSelectedVisible(MENU_LABELS.length);
               }

               consumeKeys(var1);
            }
         } else {
            if ((var1.key0 || var1.keyBack) && (var1.text.confirm() || var1.text.disposed)) {
               var1.text = null;
               if (this.mode == VqsvPanelRuntime.Mode.RIDE && this.rideMessageMode != 0) {
                  int var2 = this.rideMessageMode;
                  this.rideMessageMode = 0;
                  var1.session.story.trace().add("PORTED panel game.h.ae ride msgwarm close mode=" + var2 + " return ride.ui selected=" + this.rideSelected);
               }
            }

            consumeKeys(var1);
         }
      }
   }

   void mouseWheel(VqsvGameRuntime.Scene var1, int var2) {
      if (this.visible && var2 != 0 && this.mode != VqsvPanelRuntime.Mode.SAVE && this.mode != VqsvPanelRuntime.Mode.HELP && this.mode != VqsvPanelRuntime.Mode.SETTINGS && this.mode != VqsvPanelRuntime.Mode.OPTION_CONFIRM && this.mode != VqsvPanelRuntime.Mode.TASK_MAP) {
         int var3 = this.wheelRowCount(var1);
         int var4 = this.visibleRowsForCurrentList();
         int var5 = Math.max(0, var3 - var4);
         if (this.isScrollablePanelList() && var5 > 0) {
            int var6 = this.listScroll;
            int var7 = this.selected;
            this.listScroll = clamp(this.listScroll + var2, 0, var5);
            this.selected = clampIndexIntoVisible(this.selected, this.listScroll, var3, var4);
            if (this.mode == VqsvPanelRuntime.Mode.TASK) {
               this.updateTaskSelectedLabel(var1);
            }

            if (this.selected != var7) {
               this.openedTicks = 0;
            }

            if (this.listScroll != var6 || this.selected != var7) {
               var1.session.story.trace().add("PC_QOL mouse wheel panel list scrollbar mode=" + String.valueOf(this.mode) + " scroll=" + this.listScroll + " selected=" + this.selected + " rows=" + var3);
            }

         } else {
            this.moveSelectionByMouseWheel(var1, var2);
         }
      }
   }

   boolean pointerPressedScrollbar(VqsvGameRuntime.Scene var1, int var2, int var3) {
      int[] var4 = this.scrollbarGeometry(var1);
      if (var4 != null && var4[4] > var4[5] && UiScrollbarMath.trackContains(var2, var3, var4[0], var4[1], var4[2], var4[3])) {
         int var5 = UiScrollbarMath.thumbHeight(var4[3], var4[4], var4[5], var4[6]);
         int var6 = UiScrollbarMath.thumbY(var4[1], var4[3], var5, var4[4], var4[5], this.listScroll);
         if (UiScrollbarMath.thumbContains(var2, var3, var4[0], var4[2], var6, var5)) {
            this.scrollbarDragMode = this.mode;
            this.scrollbarGrabOffset = var3 - var6;
            List var10000 = var1.session.story.trace();
            String var10001 = String.valueOf(this.mode);
            var10000.add("PC_QOL panel scrollbar drag begin mode=" + var10001 + " scroll=" + this.listScroll);
         } else {
            int var7 = UiScrollbarMath.pageScroll(var3, this.listScroll, var4[1], var4[3], var4[4], var4[5], var4[6]);
            this.applyScrollbarScroll(var1, var7, "track");
         }

         return true;
      } else {
         return false;
      }
   }

   boolean scrollbarDragging() {
      return this.visible && this.scrollbarDragMode != null && this.scrollbarDragMode == this.mode;
   }

   boolean dragScrollbar(VqsvGameRuntime.Scene var1, int var2, int var3) {
      if (!this.scrollbarDragging()) {
         this.scrollbarDragMode = null;
         return false;
      } else {
         int[] var4 = this.scrollbarGeometry(var1);
         if (var4 != null && var4[4] > var4[5]) {
            int var5 = UiScrollbarMath.dragScroll(var3, this.scrollbarGrabOffset, var4[1], var4[3], var4[4], var4[5], var4[6]);
            this.applyScrollbarScroll(var1, var5, "drag");
            return true;
         } else {
            this.scrollbarDragMode = null;
            return false;
         }
      }
   }

   void releaseScrollbar(VqsvGameRuntime.Scene var1) {
      if (this.scrollbarDragMode != null) {
         List var10000 = var1.session.story.trace();
         String var10001 = String.valueOf(this.scrollbarDragMode);
         var10000.add("PC_QOL panel scrollbar drag end mode=" + var10001 + " scroll=" + this.listScroll);
      }

      this.scrollbarDragMode = null;
      this.scrollbarGrabOffset = 0;
   }

   private void applyScrollbarScroll(VqsvGameRuntime.Scene var1, int var2, String var3) {
      int var4 = this.wheelRowCount(var1);
      int var5 = this.visibleRowsForCurrentList();
      int var6 = Math.max(0, var4 - var5);
      int var7 = this.listScroll;
      int var8 = this.selected;
      this.listScroll = clamp(var2, 0, var6);
      this.selected = clampIndexIntoVisible(this.selected, this.listScroll, var4, var5);
      if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         this.updateTaskSelectedLabel(var1);
      }

      if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         this.updateSourceSellSelectedLabel(var1);
      }

      if (this.selected != var8) {
         this.openedTicks = 0;
      }

      if (this.listScroll != var7 || this.selected != var8) {
         var1.session.story.trace().add("PC_QOL panel scrollbar " + var3 + " mode=" + String.valueOf(this.mode) + " scroll=" + this.listScroll + " selected=" + this.selected + " rows=" + var4);
      }

   }

   private int[] scrollbarGeometry(VqsvGameRuntime.Scene var1) {
      if (this.visible && var1.text == null && this.isScrollablePanelList()) {
         byte var6 = 8;
         if (this.mode == VqsvPanelRuntime.Mode.GAMEMENU) {
            int var16 = UiScrollbarMath.thumbHeight(100, MENU_LABELS.length, 6, 18);
            return new int[]{157, 105, 4, 100, MENU_LABELS.length, 6, var16};
         } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
            int var15 = bagRows(var1, this.bagTab).size();
            int var20 = UiScrollbarMath.thumbHeight(108, var15, 5, 9);
            return new int[]{221, 115, 6, 108, var15, 5, var20};
         } else {
            String var2;
            byte var3;
            byte var4;
            byte var5;
            if (this.mode == VqsvPanelRuntime.Mode.TASK) {
               var2 = "task.ui";
               var3 = 39;
               var4 = 40;
               var5 = 72;
            } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
               var2 = "petmap.ui";
               var3 = 22;
               var4 = 23;
               var5 = 72;
            } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
               var2 = "transmit.ui";
               var3 = 12;
               var4 = 13;
               var5 = 88;
               var6 = 10;
            } else {
               if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
                  int var14 = this.portableShopItemCount();
                  int var19 = UiScrollbarMath.thumbHeight(103, var14, 5, 9);
                  return new int[]{217, 99, 6, 103, var14, 5, var19};
               }

               if (this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
                  if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
                     int var13 = SourceFashionCatalog.instance().records().size();
                     short var18 = 190;
                     int var22 = UiScrollbarMath.thumbHeight(var18, var13, 5, 8);
                     return new int[]{120, 38, 6, var18, var13, 5, var22};
                  }

                  if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
                     int var12 = FashionEconomyCatalog.instance().entries().size();
                     short var17 = 180;
                     int var21 = UiScrollbarMath.thumbHeight(var17, var12, 5, 8);
                     return new int[]{121, 64, 4, var17, var12, 5, var21};
                  }

                  return null;
               }

               var2 = "shopbuy.ui";
               var3 = 38;
               var4 = -1;
               var5 = 84;
            }

            VqsvUiLayout var7 = VqsvUiLayout.load(var2);
            VqsvUiLayout.UiWidget var8 = var7.widget(var3);
            VqsvUiLayout.UiWidget var9 = var4 < 0 ? null : var7.widget(var4);
            if (var8 == null) {
               return null;
            } else {
               int var10 = var9 == null ? var8.x : Math.min(var8.x, var9.x);
               int var11 = Math.max(var8.x + Math.max(1, var8.w), var9 == null ? var8.x + 4 : var9.x + Math.max(1, var9.w));
               return new int[]{var10, var8.y, Math.max(1, var11 - var10), var5, this.wheelRowCount(var1), 5, var6};
            }
         }
      } else {
         return null;
      }
   }

   private boolean isScrollablePanelList() {
      return this.mode == VqsvPanelRuntime.Mode.GAMEMENU || this.mode == VqsvPanelRuntime.Mode.BAG || this.mode == VqsvPanelRuntime.Mode.TASK || this.mode == VqsvPanelRuntime.Mode.PETMAP || this.mode == VqsvPanelRuntime.Mode.TRANSMIT || this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP || this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY || this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL || this.mode == VqsvPanelRuntime.Mode.WARDROBE || this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE || this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS;
   }

   private void moveSelectionByMouseWheel(VqsvGameRuntime.Scene var1, int var2) {
      if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         int var5 = this.rideSelected;
         this.rideSelected = clamp(this.rideSelected + var2, 0, RIDE_LABELS.length - 1);
         if (this.rideSelected != var5) {
            List var10000 = var1.session.story.trace();
            String var10001 = String.valueOf(this.mode);
            var10000.add("PC_QOL mouse wheel panel selection mode=" + var10001 + " selected=" + this.rideSelected);
         }

      } else if (this.isMouseWheelSelectableMode()) {
         int var3 = this.wheelRowCount(var1);
         if (var3 > 0) {
            int var4 = this.selected;
            this.selected = clamp(this.selected + var2, 0, var3 - 1);
            if (this.isScrollablePanelList()) {
               this.keepSelectedVisible(var3);
            }

            if (this.mode == VqsvPanelRuntime.Mode.TASK) {
               this.updateTaskSelectedLabel(var1);
            }

            if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
               this.updateSourceSellSelectedLabel(var1);
            }

            if (this.selected != var4) {
               this.openedTicks = 0;
               var1.session.story.trace().add("PC_QOL mouse wheel panel selection mode=" + String.valueOf(this.mode) + " selected=" + this.selected + " rows=" + var3);
            }

         }
      }
   }

   private boolean isMouseWheelSelectableMode() {
      return this.mode == VqsvPanelRuntime.Mode.GAMEMENU || this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM || this.mode == VqsvPanelRuntime.Mode.BAG || this.mode == VqsvPanelRuntime.Mode.TASK || this.mode == VqsvPanelRuntime.Mode.TASK_OPTION || this.mode == VqsvPanelRuntime.Mode.PETMAP || this.mode == VqsvPanelRuntime.Mode.TRANSMIT || this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP || this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF || this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP || this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY || this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP || this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL || this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK || this.mode == VqsvPanelRuntime.Mode.WARDROBE || this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP || this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE || this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS;
   }

   private int visibleListStart(int var1) {
      return clamp(this.listScroll, 0, Math.max(0, var1 - 5));
   }

   private void keepSelectedVisible(int var1) {
      int var2 = this.visibleRowsForCurrentList();
      int var3 = Math.max(0, var1 - var2);
      if (this.selected < this.listScroll) {
         this.listScroll = this.selected;
      } else if (this.selected >= this.listScroll + var2) {
         this.listScroll = this.selected - var2 + 1;
      }

      this.listScroll = clamp(this.listScroll, 0, var3);
   }

   private int visibleRowsForCurrentList() {
      return this.mode == VqsvPanelRuntime.Mode.GAMEMENU ? 6 : 5;
   }

   private void resetGameMenuViewport() {
      this.listScroll = 0;
      this.keepSelectedVisible(MENU_LABELS.length);
   }

   private static int clampIndexIntoVisible(int var0, int var1, int var2, int var3) {
      if (var2 <= 0) {
         return 0;
      } else {
         int var4 = clamp(var0, 0, var2 - 1);
         if (var4 < var1) {
            var4 = var1;
         } else if (var4 >= var1 + var3) {
            var4 = var1 + var3 - 1;
         }

         return clamp(var4, 0, var2 - 1);
      }
   }

   void render(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      if (this.visible) {
         if (this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM) {
            this.renderGameSystem(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.SAVE) {
            this.renderSave(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
            this.renderBag(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
            this.renderTask(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
            this.renderTaskMap(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
            this.renderTaskOption(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
            this.renderRecord(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
            this.renderPetmap(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
            this.renderBadge(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
            this.renderHelp(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
            this.renderSettings(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
            this.renderOptionConfirm(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
            this.renderRide(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
            this.renderTransmit(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
            this.renderPortableShop(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
            this.renderPortableShopBuy(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
            this.renderPortableShopBuy(var1, var2, var3);
            this.renderPortableShopConfirm(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            this.renderPortableShop(var1, var2, var3);
            VqsvBattleRenderer.drawSmsInfoOverlay(var1, var2, this.serviceConfirmTitle, this.serviceConfirmPrompt);
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
            this.renderFashionShop(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
            this.renderFashionBuyConfirm(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM) {
            this.renderFashionOpenConfirm(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
            this.renderFashionReveal(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
            this.renderFashionExchange(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
            this.renderFashionExchangeConfirm(var1, var2, var3);
         } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
            this.renderSourceEastWharf(var1, var2);
         } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
            this.renderSourceConvenienceShop(var1, var2);
         } else if (this.mode != VqsvPanelRuntime.Mode.RAINBOW_CHARM && this.mode != VqsvPanelRuntime.Mode.RAINBOW_CHARM_CONFIRM) {
            if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
               this.renderSourceWorldShop(var1, var2);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
               this.renderSourceWorldShopSell(var1, var2, var3);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
               this.renderSourceWorldShopSell(var1, var2, var3);
               VqsvBattleRenderer.drawShopConfirmOverlay(var1, var2, this.sourceSellQuantity, this.sourceSellTotal, this.sourceSellCurrency, this.openedTicks);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
               this.renderSourceWorldShopRecoverConfirm(var1, var2, var3);
            } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
               this.renderSourcePetBank(var1, var2);
            } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
               this.renderWardrobe(var1, var2, var3);
            } else if (this.mode == VqsvPanelRuntime.Mode.CHALLENGE) {
               this.renderChallenge(var1, var2, var3);
            } else if (this.mode == VqsvPanelRuntime.Mode.GIFT_CODE) {
               this.renderGiftCode(var1, var2);
            } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS) {
               this.renderBattlePass(var1, var2, var3);
            } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS_HELP) {
               this.renderBattlePassHelp(var1, var2);
            } else {
               VqsvUiLayout var6 = VqsvUiLayout.load("gamemenu.ui");
               SpriteAnimator var5 = SpriteAnimator.load(257);
               this.drawMenuFrame(var1, var6, var5);
               this.drawGameMenuRows(var1, var2, var6, var5);
               this.drawGameMenuScrollbar(var1);
               drawText(var1, var2, var6, 10, var6.text(10, "Menu tro choi"), color(var6.widget(10), 13631758));
               drawText(var1, var2, var6, 14, MENU_TITLE_TOKENS[this.selected], color(var6.widget(14), 13631758));
               drawSoftkey(var1, var2, var6, var5, 12, var6.text(12, "Xac dinh"), 13631758);
               drawSoftkey(var1, var2, var6, var5, 11, var6.text(11, "Quay lai"), 13631758);
               drawCell(var6, var5, var1, 16);
               drawCell(var6, var5, var1, 17);
               drawText(var1, var2, var6, 18, String.valueOf(var3.session.inventory.currency.badges), color(var6.widget(18), 1862801));
               drawText(var1, var2, var6, 19, String.valueOf(var3.session.inventory.currency.money), color(var6.widget(19), 1862801));
            }
         } else {
            this.renderRainbowCharm(var1, var2, var3);
            if (this.mode == VqsvPanelRuntime.Mode.RAINBOW_CHARM_CONFIRM) {
               UnifiedItemRecord var4 = UnifiedItemCatalog.instance().byRuntime(UnifiedItemInventoryKind.EQUIPMENT, this.rainbowCharmPendingId);
               VqsvBattleRenderer.drawSmsInfoOverlay(var1, var2, this.rainbowCharmTab == 1 ? "Xác nhận chế tác" : "Xác nhận cường hóa", var4 == null ? "Bùa Hộ Trận" : var4.name + " - Enter để xác nhận");
            }

         }
      }
   }

   boolean click(VqsvGameRuntime.Scene var1, int var2, int var3) {
      if (!this.visible) {
         return false;
      } else if (var1.text != null) {
         if (var1.text.confirmClickHit(var2, var3)) {
            var1.key0 = true;
         } else if (this.mode == VqsvPanelRuntime.Mode.RIDE && this.rideMessageMode != 0 && rightSoftkeyHit(var2, var3)) {
            var1.keyBack = true;
         }

         return true;
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         int var28 = fashionMenuRowAt(var2, var3);
         if (var28 >= 0) {
            this.selectFromHover(var1, var28);
            var1.key0 = true;
            return true;
         } else {
            if (leftSoftkeyHit(var2, var3)) {
               var1.key0 = true;
            } else if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
            }

            return true;
         }
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
         if (fashionQuantityLeftHit(var2, var3)) {
            var1.keyLeft = true;
         } else if (fashionQuantityRightHit(var2, var3)) {
            var1.keyRight = true;
         } else if (fashionConfirmActionHit(var2, var3)) {
            var1.key0 = true;
         } else if (leftSoftkeyHit(var2, var3)) {
            var1.key0 = true;
         } else if (rightSoftkeyHit(var2, var3)) {
            var1.keyBack = true;
         }

         return true;
      } else if (this.mode != VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM && this.mode != VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
         if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
            if (leftSoftkeyHit(var2, var3) || rightSoftkeyHit(var2, var3)) {
               var1.key0 = true;
            }

            return true;
         } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
            int var27 = fashionExchangeRowAt(var2, var3);
            if (var27 >= 0) {
               int var42 = FashionEconomyCatalog.instance().entries().size();
               this.selectFromHover(var1, clamp(this.listScroll + var27, 0, var42 - 1));
               this.ensureFashionExchangePreview();
               var1.key0 = true;
               return true;
            } else {
               if (!fashionExchangeActionHit(var2, var3) && !leftSoftkeyHit(var2, var3)) {
                  if (rightSoftkeyHit(var2, var3)) {
                     var1.keyBack = true;
                  }
               } else {
                  var1.key0 = true;
               }

               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.SAVE) {
            if (leftSoftkeyHit(var2, var3)) {
               var1.key0 = true;
               return true;
            } else if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
               return true;
            } else {
               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
            int var26 = wardrobeRowAt(var2, var3);
            if (var26 >= 0) {
               int var41 = SourceFashionCatalog.instance().records().size();
               this.selectFromHover(var1, clamp(this.listScroll + var26, 0, var41 - 1));
               return true;
            } else if (leftSoftkeyHit(var2, var3)) {
               var1.key0 = true;
               return true;
            } else if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
               return true;
            } else {
               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.CHALLENGE) {
            if (var3 >= 77 && var3 <= 115 && var2 >= 18 && var2 <= 222) {
               this.selected = 0;
               var1.key0 = true;
               return true;
            } else if (var3 >= 124 && var3 <= 162 && var2 >= 18 && var2 <= 222) {
               this.selected = 1;
               var1.key0 = true;
               return true;
            } else if (var3 >= 43 && var3 <= 65 && var2 >= 48 && var2 <= 192) {
               if (var2 < 120) {
                  var1.keyLeft = true;
               } else {
                  var1.keyRight = true;
               }

               return true;
            } else if (!VqsvShopUiLayout.actionHit(var2, var3) && !leftSoftkeyHit(var2, var3)) {
               if (!VqsvShopUiLayout.backHit(var2, var3) && !rightSoftkeyHit(var2, var3)) {
                  return true;
               } else {
                  var1.keyBack = true;
                  return true;
               }
            } else {
               var1.key0 = true;
               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.GIFT_CODE) {
            if (!VqsvShopUiLayout.actionHit(var2, var3) && !leftSoftkeyHit(var2, var3)) {
               if (VqsvShopUiLayout.backHit(var2, var3) || rightSoftkeyHit(var2, var3)) {
                  var1.keyBack = true;
               }
            } else {
               var1.key0 = true;
            }

            return true;
         } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
            if (this.eggPreviewOpen) {
               if (var2 >= 10 && var2 < 230 && var3 >= 40 && var3 < 290) {
                  var1.keyBack = true;
               }

               return true;
            } else {
               int var25 = VqsvBagUiLayout.tabAt(var2, var3);
               if (var25 >= 0) {
                  int var40 = this.bagTab;
                  this.bagTab = var25;
                  this.selected = 0;
                  this.listScroll = 0;
                  var1.session.story.trace().add("PC_QOL panel bag.ui tab click b=" + var40 + "->" + this.bagTab + " title=" + bagTabTitle(this.bagTab));
                  return true;
               } else {
                  int var39 = bagRows(var1, this.bagTab).size();
                  int var50 = this.visibleListStart(var39);
                  int var57 = VqsvBagUiLayout.rowIndexAt(var2, var3, var39, var50);
                  if (var57 >= 0) {
                     this.selected = var57;
                     BagRow var59 = (BagRow)bagRows(var1, this.bagTab).get(var57);
                     if (var59.specialEgg && var59.count > 0 && eggPreviewHit(var2, var3)) {
                        this.eggPreviewOpen = true;
                        this.eggPreviewItemId = var59.specialId;
                        this.eggPreviewScroll = 0;
                        List var10000 = var1.session.story.trace();
                        int var10001 = this.eggPreviewItemId;
                        var10000.add("UNIFIED-DESIGN egg preview open itemId=" + var10001 + " count=" + EggPreviewCatalog.entries(this.eggPreviewItemId).size());
                        return true;
                     } else {
                        var1.key0 = true;
                        return true;
                     }
                  } else if (!VqsvBagUiLayout.actionHit(var2, var3) && !bagLeftSoftkeyHit(var2, var3) && !leftSoftkeyHit(var2, var3)) {
                     if (!VqsvBagUiLayout.backHit(var2, var3) && !bagRightSoftkeyHit(var2, var3) && !rightSoftkeyHit(var2, var3)) {
                        return true;
                     } else {
                        var1.keyBack = true;
                        return true;
                     }
                  } else {
                     var1.key0 = true;
                     return true;
                  }
               }
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
            VqsvUiLayout var24 = VqsvUiLayout.load("task.ui");
            VqsvUiLayout.UiWidget var38 = var24.widget(6);
            if (var38 != null && var2 >= var38.x - 4 && var2 <= var38.x + 68 && var3 >= var38.y - 4 && var3 <= var38.y + 22) {
               var1.keyLeft = true;
               return true;
            } else {
               VqsvUiLayout.UiWidget var49 = var24.widget(7);
               if (var49 != null && var2 >= var49.x - 4 && var2 <= var49.x + 68 && var3 >= var49.y - 4 && var3 <= var49.y + 22) {
                  var1.keyRight = true;
                  return true;
               } else {
                  int var56 = taskRowsForRender(var1, this.taskTab).size();
                  int var58 = this.visibleListStart(var56);

                  for(int var60 = 0; var60 < TASK_ROW_BACKGROUNDS.length; ++var60) {
                     VqsvUiLayout.UiWidget var61 = var24.widget(TASK_ROW_BACKGROUNDS[var60]);
                     if (var61 != null && var2 >= var61.x - 4 && var2 <= var61.x + 136 && var3 >= var61.y - 2 && var3 <= var61.y + 14) {
                        this.selected = clamp(var58 + var60, 0, Math.max(0, var56 - 1));
                        var1.key0 = true;
                        return true;
                     }
                  }

                  if (leftSoftkeyHit(var2, var3)) {
                     var1.key0 = true;
                     return true;
                  } else if (rightSoftkeyHit(var2, var3)) {
                     var1.keyBack = true;
                     return true;
                  } else {
                     return true;
                  }
               }
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
            if (!rightSoftkeyHit(var2, var3) && !taskMapBackButtonHit(var2, var3)) {
               return true;
            } else {
               var1.keyBack = true;
               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
            VqsvUiLayout var23 = VqsvUiLayout.load("taskOption.ui");
            int[] var37 = new int[]{10, 11};

            for(int var48 = 0; var48 < var37.length && var48 < this.taskOptionData.options.length; ++var48) {
               VqsvUiLayout.UiWidget var55 = var23.widget(var37[var48]);
               if (var55 != null && var2 >= var55.x - 4 && var2 <= var55.x + 84 && var3 >= var55.y - 2 && var3 < var55.y + Math.max(15, var55.h) + 2) {
                  this.selected = var48;
                  var1.key0 = true;
                  return true;
               }
            }

            if (leftSoftkeyHit(var2, var3)) {
               var1.key0 = true;
               return true;
            } else if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
               return true;
            } else {
               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
            if (var2 < 120 && var3 >= 210 && var3 <= 260) {
               this.recordSelected = 0;
               var1.key0 = true;
               return true;
            } else if (var2 >= 120 && var3 >= 210 && var3 <= 260) {
               this.recordSelected = 1;
               var1.key0 = true;
               return true;
            } else if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
               return true;
            } else {
               return true;
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
            VqsvUiLayout var22 = VqsvUiLayout.load("petmap.ui");
            int var36 = petmapTabAt(var22, var2, var3);
            if (var36 >= 0) {
               int var47 = this.petmapTab;
               this.petmapTab = var36;
               this.selected = 0;
               this.listScroll = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PC_QOL panel petmap.ui tab click tab=" + var47 + "->" + this.petmapTab + " name=" + PETMAP_TAB_NAMES[this.petmapTab]);
               return true;
            } else {
               int var46 = petmapRowsForRender(var1, this.petmapTab).size();
               int var54 = this.visibleListStart(var46);

               for(int var8 = 0; var8 < PETMAP_ROW_BACKGROUNDS.length; ++var8) {
                  VqsvUiLayout.UiWidget var9 = var22.widget(PETMAP_ROW_BACKGROUNDS[var8]);
                  if (var9 != null && var2 >= var9.x - 4 && var2 <= var9.x + 136 && var3 >= var9.y - 2 && var3 <= var9.y + 14) {
                     int var10 = this.selected;
                     this.selected = clamp(var54 + var8, 0, Math.max(0, var46 - 1));
                     this.openedTicks = 0;
                     var1.session.story.trace().add("PC_QOL panel petmap.ui row click selected=" + var10 + "->" + this.selected + " tab=" + this.petmapTab);
                     return true;
                  }
               }

               if (rightSoftkeyHit(var2, var3)) {
                  var1.keyBack = true;
                  return true;
               } else {
                  return true;
               }
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
            if (this.badgeAwardReturnToWorld) {
               if (leftSoftkeyHit(var2, var3)) {
                  var1.key0 = true;
               } else if (rightSoftkeyHit(var2, var3)) {
                  var1.keyBack = true;
               }

               return true;
            } else {
               VqsvUiLayout var21 = VqsvUiLayout.load("badge.ui");
               int var35 = this.badgeSlotAt(var1, var21, var2, var3);
               if (var35 >= 0) {
                  int var45 = this.selected;
                  this.selected = var35;
                  this.openedTicks = 0;
                  int var53 = badgeRuntimeIdAtDisplayIndex(var1, this.selected);
                  var1.session.story.trace().add("PC_QOL panel badge.ui slot click displayIndex=" + var45 + "->" + this.selected + " badgeId=" + var53 + " status=" + badgeStatusText(var1, var53));
                  return true;
               } else if (rightSoftkeyHit(var2, var3)) {
                  var1.keyBack = true;
                  return true;
               } else {
                  return true;
               }
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
            if (rideCloseHit(var2, var3)) {
               var1.keyBack = true;
               return true;
            } else {
               VqsvUiLayout var20 = VqsvUiLayout.load("ride.ui");
               int var34 = rideSlotAt(var20, var2, var3);
               if (var34 >= 0) {
                  this.rideSelected = var34;
                  var1.key0 = true;
                  return true;
               } else if (leftSoftkeyHit(var2, var3)) {
                  var1.key0 = true;
                  return true;
               } else if (rightSoftkeyHit(var2, var3)) {
                  var1.keyBack = true;
                  return true;
               } else {
                  return true;
               }
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
            VqsvUiLayout var19 = VqsvUiLayout.load("help1.ui");
            VqsvUiLayout.UiWidget var33 = var19.widget(6);
            if (var33 != null && var2 >= var33.x - 10 && var2 <= var33.x + Math.max(48, var33.w) + 12 && var3 >= var33.y - 8 && var3 <= var33.y + 22) {
               var1.keyBack = true;
               return true;
            } else {
               VqsvUiLayout.UiWidget var44 = var19.widget(37);
               if (var44 != null && var2 >= var44.x - 12 && var2 <= var44.x + 24 && var3 >= var44.y - 12 && var3 <= var44.y + 24) {
                  var1.keyLeft = true;
                  return true;
               } else {
                  VqsvUiLayout.UiWidget var52 = var19.widget(38);
                  if (var52 != null && var2 >= var52.x - 12 && var2 <= var52.x + 24 && var3 >= var52.y - 12 && var3 <= var52.y + 24) {
                     var1.keyRight = true;
                     return true;
                  } else if (rightSoftkeyHit(var2, var3)) {
                     var1.keyBack = true;
                     return true;
                  } else {
                     return true;
                  }
               }
            }
         } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
            if (settingsSpeedHit(var2, var3)) {
               var1.toggleSpeedX2("settings.ui speed checkbox");
               return true;
            } else if (var2 >= 112 && var2 <= 166 && var3 >= 136 && var3 <= 174) {
               int var18 = this.settingsLevel;
               if (var2 < 133) {
                  this.settingsLevel = 1;
               } else if (var2 < 147) {
                  this.settingsLevel = 2;
               } else {
                  this.settingsLevel = 3;
               }

               if (this.settingsLevel != var18) {
                  var1.session.story.trace().add("PC_QOL settings.ui volume click g=" + var18 + "->" + this.settingsLevel);
               }

               return true;
            } else if (leftSoftkeyHit(var2, var3)) {
               var1.toggleSpeedX2("settings.ui left softkey speed checkbox");
               return true;
            } else if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
               return true;
            } else {
               return true;
            }
         } else if (this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY && this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
            if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
               int var17 = VqsvShopUiLayout.serviceIndexAt(var2, var3);
               if (var17 >= 0) {
                  this.selected = var17;
                  var1.key0 = true;
                  return true;
               } else if (!VqsvShopUiLayout.actionHit(var2, var3) && !leftSoftkeyHit(var2, var3)) {
                  if (!VqsvShopUiLayout.backHit(var2, var3) && !rightSoftkeyHit(var2, var3)) {
                     return true;
                  } else {
                     var1.keyBack = true;
                     return true;
                  }
               } else {
                  var1.key0 = true;
                  return true;
               }
            } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
               VqsvUiLayout var16 = VqsvUiLayout.load("transmit.ui");
               int var32 = this.visibleListStart(TRANSMIT_DESTINATIONS.length);

               for(int var43 = 0; var43 < TRANSMIT_ROW_WIDGETS.length; ++var43) {
                  VqsvUiLayout.UiWidget var51 = var16.widget(TRANSMIT_ROW_WIDGETS[var43]);
                  if (var51 != null && var2 >= var51.x - 4 && var2 <= var51.x + Math.max(59, var51.w) + 12 && var3 >= var51.y - 2 && var3 <= var51.y + 18) {
                     this.selected = clamp(var32 + var43, 0, TRANSMIT_DESTINATIONS.length - 1);
                     var1.key0 = true;
                     return true;
                  }
               }

               if (leftSoftkeyHit(var2, var3)) {
                  var1.key0 = true;
                  return true;
               } else if (rightSoftkeyHit(var2, var3)) {
                  var1.keyBack = true;
                  return true;
               } else {
                  return true;
               }
            } else if (this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM && this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
               if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
                  if (smsInfoConfirmHit(var2, var3)) {
                     var1.key0 = true;
                     return true;
                  } else if (smsInfoCancelHit(var2, var3)) {
                     var1.keyBack = true;
                     return true;
                  } else {
                     return true;
                  }
               } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
                  if (leftSoftkeyHit(var2, var3)) {
                     var1.key0 = true;
                  } else if (rightSoftkeyHit(var2, var3)) {
                     var1.keyBack = true;
                  }

                  return true;
               } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
                  int var15 = widgetRowAt("shop.ui", SOURCE_WORLD_SHOP_ROW_WIDGETS, var2, var3, 92);
                  if (var15 >= 0) {
                     this.selected = clamp(var15, 0, SOURCE_WORLD_SHOP_LABELS.length - 1);
                     var1.key0 = true;
                     return true;
                  } else if (leftSoftkeyHit(var2, var3)) {
                     var1.key0 = true;
                     return true;
                  } else if (rightSoftkeyHit(var2, var3)) {
                     var1.keyBack = true;
                     return true;
                  } else {
                     return true;
                  }
               } else if (this.mode != VqsvPanelRuntime.Mode.RAINBOW_CHARM && this.mode != VqsvPanelRuntime.Mode.RAINBOW_CHARM_CONFIRM) {
                  if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
                     int var14 = widgetRowAt("shop.ui", SOURCE_PET_BANK_ROW_WIDGETS, var2, var3, 92);
                     if (var14 >= 0) {
                        this.selected = clamp(var14, 0, SOURCE_PET_BANK_LABELS.length - 1);
                        var1.key0 = true;
                        return true;
                     } else if (leftSoftkeyHit(var2, var3)) {
                        var1.key0 = true;
                        return true;
                     } else if (rightSoftkeyHit(var2, var3)) {
                        var1.keyBack = true;
                        return true;
                     } else {
                        return true;
                     }
                  } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS) {
                     if (var3 >= 99 && var3 <= 126) {
                        if (var2 >= 194 && var2 <= 225) {
                           this.mode = VqsvPanelRuntime.Mode.BATTLE_PASS_HELP;
                           this.openedTicks = 0;
                           return true;
                        } else {
                           this.battlePassTrack = var2 >= 82 ? 1 : 0;
                           if (var2 >= 82 && var2 < 194) {
                              var1.key0 = true;
                           }

                           return true;
                        }
                     } else if (var3 >= 127 && var3 < 252) {
                        int var13 = (var3 - 127) / 25;
                        int var31 = this.listScroll + var13 + 1;
                        if (var13 >= 0 && var13 < 5 && var31 <= 50) {
                           this.selected = var31;
                           var1.key0 = true;
                        }

                        return true;
                     } else {
                        if (leftSoftkeyHit(var2, var3)) {
                           var1.key0 = true;
                        } else if (rightSoftkeyHit(var2, var3)) {
                           var1.keyBack = true;
                        }

                        return true;
                     }
                  } else if (this.mode != VqsvPanelRuntime.Mode.BATTLE_PASS_HELP) {
                     int[] var12 = this.rowWidgets();
                     VqsvUiLayout var30 = VqsvUiLayout.load(this.uiName());

                     for(int var6 = 0; var6 < var12.length; ++var6) {
                        VqsvUiLayout.UiWidget var7 = var30.widget(var12[var6]);
                        if (var7 != null && var2 >= var7.x - 4 && var2 <= var7.x + Math.max(59, var7.w) + 12 && var3 >= var7.y - 2 && var3 <= var7.y + 14) {
                           this.selected = this.mode == VqsvPanelRuntime.Mode.GAMEMENU ? this.gameMenuIndexAtSlot(var6) : (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP ? this.listScroll + var6 : var6);
                           var1.key0 = true;
                           return true;
                        }
                     }

                     if (leftSoftkeyHit(var2, var3)) {
                        var1.key0 = true;
                        return true;
                     } else if (rightSoftkeyHit(var2, var3)) {
                        var1.keyBack = true;
                        return true;
                     } else {
                        return true;
                     }
                  } else {
                     if (rightSoftkeyHit(var2, var3) || var2 >= 0 && var3 >= 280) {
                        var1.keyBack = true;
                     }

                     return true;
                  }
               } else if (this.mode == VqsvPanelRuntime.Mode.RAINBOW_CHARM_CONFIRM) {
                  if (smsInfoConfirmHit(var2, var3)) {
                     var1.key0 = true;
                  }

                  if (smsInfoCancelHit(var2, var3)) {
                     var1.keyBack = true;
                  }

                  return true;
               } else if (var3 >= 65 && var3 <= 87 && var2 >= 20 && var2 <= 224) {
                  this.rainbowCharmTab = clamp((var2 - 20) / 67, 0, 2);
                  this.selected = 0;
                  this.listScroll = 0;
                  return true;
               } else {
                  List var11 = this.rainbowCharmRows(var1);
                  int var29 = VqsvShopUiLayout.itemIndexAt(var2, var3, var11.size(), this.visibleListStart(var11.size()));
                  if (var29 >= 0) {
                     this.selected = var29;
                     return true;
                  } else {
                     if (VqsvShopUiLayout.actionHit(var2, var3) || leftSoftkeyHit(var2, var3)) {
                        var1.key0 = true;
                     }

                     if (VqsvShopUiLayout.backHit(var2, var3) || rightSoftkeyHit(var2, var3)) {
                        var1.keyBack = true;
                     }

                     return true;
                  }
               }
            } else if ((this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM || !VqsvShopUiLayout.quantityLeftHit(var2, var3)) && !msgynQuantityLeftHit(var2, var3)) {
               if ((this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM || !VqsvShopUiLayout.quantityRightHit(var2, var3)) && !msgynQuantityRightHit(var2, var3)) {
                  if ((this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM || !VqsvShopUiLayout.confirmYesHit(var2, var3)) && !msgynConfirmHit(var2, var3)) {
                     if ((this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM || !VqsvShopUiLayout.confirmNoHit(var2, var3)) && !msgynCancelHit(var2, var3)) {
                        if (leftSoftkeyHit(var2, var3)) {
                           var1.key0 = true;
                           return true;
                        } else if (rightSoftkeyHit(var2, var3)) {
                           var1.keyBack = true;
                           return true;
                        } else {
                           return true;
                        }
                     } else {
                        var1.keyBack = true;
                        return true;
                     }
                  } else {
                     var1.key0 = true;
                     return true;
                  }
               } else {
                  var1.keyRight = true;
                  return true;
               }
            } else {
               var1.keyLeft = true;
               return true;
            }
         } else {
            int var4 = this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY ? this.portableShopItemCount() : sourceWorldShopSellRows(var1).size();
            int var5 = this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY ? VqsvShopUiLayout.itemIndexAt(var2, var3, var4, this.visibleListStart(var4)) : (shopbuyRowAt(var2, var3) < 0 ? -1 : this.visibleListStart(var4) + shopbuyRowAt(var2, var3));
            if (var5 >= 0) {
               this.selected = clamp(var5, 0, Math.max(0, var4 - 1));
               var1.key0 = true;
               return true;
            } else if ((this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY || !VqsvShopUiLayout.actionHit(var2, var3)) && !leftSoftkeyHit(var2, var3)) {
               if ((this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY || !VqsvShopUiLayout.backHit(var2, var3)) && !rightSoftkeyHit(var2, var3)) {
                  return true;
               } else {
                  var1.keyBack = true;
                  return true;
               }
            } else {
               var1.key0 = true;
               return true;
            }
         }
      } else {
         if (!fashionConfirmActionHit(var2, var3) && !leftSoftkeyHit(var2, var3)) {
            if (rightSoftkeyHit(var2, var3)) {
               var1.keyBack = true;
            }
         } else {
            var1.key0 = true;
         }

         return true;
      }
   }

   private static boolean leftSoftkeyHit(int var0, int var1) {
      return var0 <= 90 && var1 >= 280;
   }

   private static boolean rightSoftkeyHit(int var0, int var1) {
      return var0 >= 145 && var1 >= 280;
   }

   private static boolean taskMapBackButtonHit(int var0, int var1) {
      return var0 >= 74 && var0 <= 166 && var1 >= 276 && var1 <= 302;
   }

   private static boolean bagLeftSoftkeyHit(int var0, int var1) {
      return var0 >= 0 && var0 <= 78 && var1 >= 286;
   }

   private static boolean bagRightSoftkeyHit(int var0, int var1) {
      return var0 >= 162 && var0 <= 240 && var1 >= 286;
   }

   private static boolean msgynConfirmHit(int var0, int var1) {
      return var0 >= 74 && var0 <= 160 && var1 >= 158 && var1 <= 184;
   }

   private static boolean msgynCancelHit(int var0, int var1) {
      return var0 >= 74 && var0 <= 160 && var1 >= 184 && var1 <= 210;
   }

   private static boolean msgynQuantityLeftHit(int var0, int var1) {
      return var0 >= 74 && var0 <= 115 && var1 >= 116 && var1 <= 154;
   }

   private static boolean msgynQuantityRightHit(int var0, int var1) {
      return var0 >= 116 && var0 <= 160 && var1 >= 116 && var1 <= 154;
   }

   private static boolean smsInfoConfirmHit(int var0, int var1) {
      return var0 >= 44 && var0 <= 116 && var1 >= 228 && var1 <= 262 || leftSoftkeyHit(var0, var1);
   }

   private static boolean smsInfoCancelHit(int var0, int var1) {
      return var0 >= 124 && var0 <= 224 && var1 >= 228 && var1 <= 262 || rightSoftkeyHit(var0, var1);
   }

   private static boolean settingsSpeedHit(int var0, int var1) {
      return var0 >= 80 && var0 <= 166 && var1 >= 174 && var1 <= 207;
   }

   private static int bagTabAt(VqsvUiLayout var0, int var1, int var2) {
      for(int var3 = 0; var3 < BAG_TAB_WIDGETS.length; ++var3) {
         VqsvUiLayout.UiWidget var4 = var0.widget(BAG_TAB_WIDGETS[var3]);
         if (var4 != null && var1 >= var4.x - 6 && var1 <= var4.x + Math.max(32, var4.w) + 8 && var2 >= var4.y - 6 && var2 <= var4.y + 30) {
            return var3;
         }
      }

      return -1;
   }

   private static int petmapTabAt(VqsvUiLayout var0, int var1, int var2) {
      for(int var3 = 0; var3 < PETMAP_TAB_CELLS.length; ++var3) {
         VqsvUiLayout.UiWidget var4 = var0.widget(PETMAP_TAB_CELLS[var3]);
         if (var4 != null && var1 >= var4.x && var1 < var4.x + Math.max(16, var4.w) && var2 >= var4.y - 2 && var2 < var4.y + 20) {
            return var3;
         }
      }

      return -1;
   }

   private int badgeSlotAt(VqsvGameRuntime.Scene var1, VqsvUiLayout var2, int var3, int var4) {
      int var5 = badgeDisplayCount(var1);
      if (var5 > BADGE_SLOT_WIDGETS.length) {
         int var11 = badgeGridFirst(this.selected, var5);
         int var12 = Math.min(10, var5 - var11);

         for(int var8 = 0; var8 < var12; ++var8) {
            int var9 = badgeGridX(var8, var12);
            int var10 = badgeGridY(var8);
            if (var3 >= var9 && var3 < var9 + 30 && var4 >= var10 && var4 < var10 + 30) {
               return var11 + var8;
            }
         }

         return -1;
      } else {
         for(int var6 = 0; var6 < BADGE_SLOT_WIDGETS.length; ++var6) {
            VqsvUiLayout.UiWidget var7 = var2.widget(BADGE_SLOT_WIDGETS[var6]);
            if (var7 != null && var3 >= var7.x && var3 < var7.x + Math.max(30, var7.w) && var4 >= var7.y && var4 < var7.y + 31) {
               return var6;
            }
         }

         return -1;
      }
   }

   boolean hover(VqsvGameRuntime.Scene var1, int var2, int var3) {
      if (!this.visible) {
         return false;
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         int var19 = fashionMenuRowAt(var2, var3);
         if (var19 >= 0) {
            this.selectFromHover(var1, var19);
         }

         return true;
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
         int var18 = fashionExchangeRowAt(var2, var3);
         if (var18 >= 0) {
            int var27 = FashionEconomyCatalog.instance().entries().size();
            this.selectFromHover(var1, clamp(this.listScroll + var18, 0, var27 - 1));
            this.ensureFashionExchangePreview();
         }

         return true;
      } else if (this.mode != VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM && this.mode != VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM && this.mode != VqsvPanelRuntime.Mode.FASHION_REVEAL && this.mode != VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
         if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
            int var17 = wardrobeRowAt(var2, var3);
            if (var17 >= 0) {
               int var26 = SourceFashionCatalog.instance().records().size();
               this.selectFromHover(var1, clamp(this.listScroll + var17, 0, var26 - 1));
            }

            return true;
         } else if (this.mode != VqsvPanelRuntime.Mode.CHALLENGE) {
            if (this.mode == VqsvPanelRuntime.Mode.GIFT_CODE) {
               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
               int var16 = bagRows(var1, this.bagTab).size();
               int var25 = VqsvBagUiLayout.rowIndexAt(var2, var3, var16, this.visibleListStart(var16));
               if (var25 >= 0) {
                  this.selectFromHover(var1, var25);
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
               int var15 = widgetRowAt("task.ui", TASK_ROW_BACKGROUNDS, var2, var3, 136);
               if (var15 >= 0) {
                  int var24 = taskRowsForRender(var1, this.taskTab).size();
                  this.selectFromHover(var1, clamp(this.visibleListStart(var24) + var15, 0, Math.max(0, var24 - 1)));
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
               int var14 = widgetRowAt("taskOption.ui", new int[]{10, 11}, var2, var3, 84);
               if (var14 >= 0) {
                  this.selectFromHover(var1, clamp(var14, 0, Math.max(0, this.taskOptionData.options.length - 1)));
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
               int var13 = widgetRowAt("petmap.ui", PETMAP_ROW_BACKGROUNDS, var2, var3, 136);
               if (var13 >= 0) {
                  int var23 = petmapRowsForRender(var1, this.petmapTab).size();
                  this.selectFromHover(var1, clamp(this.visibleListStart(var23) + var13, 0, Math.max(0, var23 - 1)));
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
               VqsvUiLayout var12 = VqsvUiLayout.load("badge.ui");
               int var22 = this.badgeSlotAt(var1, var12, var2, var3);
               if (var22 >= 0) {
                  this.selectFromHover(var1, var22);
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
               VqsvUiLayout var11 = VqsvUiLayout.load("ride.ui");
               int var21 = rideSlotAt(var11, var2, var3);
               if (var21 >= 0) {
                  this.rideSelected = var21;
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
               int var10 = widgetRowAt("transmit.ui", TRANSMIT_ROW_WIDGETS, var2, var3, 59);
               if (var10 >= 0) {
                  this.selectFromHover(var1, clamp(this.visibleListStart(TRANSMIT_DESTINATIONS.length) + var10, 0, TRANSMIT_DESTINATIONS.length - 1));
               }

               return true;
            } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
               int var9 = VqsvShopUiLayout.serviceIndexAt(var2, var3);
               if (var9 >= 0) {
                  this.selectFromHover(var1, clamp(var9, 0, PORTABLE_SHOP_LABELS.length - 1));
               }

               return true;
            } else if (this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY && this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
               if (this.mode != VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM && this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
                  if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
                     return true;
                  } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
                     return true;
                  } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
                     int var8 = widgetRowAt("shop.ui", SOURCE_WORLD_SHOP_ROW_WIDGETS, var2, var3, 92);
                     if (var8 >= 0) {
                        this.selectFromHover(var1, clamp(var8, 0, SOURCE_WORLD_SHOP_LABELS.length - 1));
                     }

                     return true;
                  } else {
                     int var7 = widgetRowAt(this.uiName(), this.rowWidgets(), var2, var3, 59);
                     if (var7 >= 0) {
                        int var20 = this.mode == VqsvPanelRuntime.Mode.GAMEMENU ? this.gameMenuIndexAtSlot(var7) : (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP ? this.listScroll + var7 : var7);
                        this.selectFromHover(var1, clamp(var20, 0, this.labels().length - 1));
                     }

                     return true;
                  }
               } else {
                  return true;
               }
            } else {
               int var4 = this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY ? this.portableShopItemCount() : sourceWorldShopSellRows(var1).size();
               int var5 = this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY ? -1 : shopbuyRowAt(var2, var3);
               int var6 = this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY ? VqsvShopUiLayout.itemIndexAt(var2, var3, var4, this.visibleListStart(var4)) : (var5 < 0 ? -1 : this.visibleListStart(var4) + var5);
               if (var6 >= 0) {
                  this.selectFromHover(var1, clamp(var6, 0, Math.max(0, var4 - 1)));
                  return true;
               } else {
                  return true;
               }
            }
         } else {
            if (var3 >= 77 && var3 <= 115 && var2 >= 18 && var2 <= 222) {
               this.selectFromHover(var1, 0);
            } else if (var3 >= 124 && var3 <= 162 && var2 >= 18 && var2 <= 222) {
               this.selectFromHover(var1, 1);
            }

            return true;
         }
      } else {
         return true;
      }
   }

   private int gameMenuIndexAtSlot(int var1) {
      int var2 = clamp(this.listScroll, 0, Math.max(0, MENU_LABELS.length - 6));
      return clamp(var2 + var1, 0, MENU_LABELS.length - 1);
   }

   private void selectFromHover(VqsvGameRuntime.Scene var1, int var2) {
      int var3 = this.selected;
      this.selected = var2;
      if (this.selected != var3) {
         this.openedTicks = 0;
         if (this.mode == VqsvPanelRuntime.Mode.TASK) {
            this.updateTaskSelectedLabel(var1);
         }

         if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
            this.updateSourceSellSelectedLabel(var1);
         }

         List var10000 = var1.session.story.trace();
         String var10001 = String.valueOf(this.mode);
         var10000.add("PC_QOL panel hover preview mode=" + var10001 + " selected=" + this.selected);
      }

   }

   private static int widgetRowAt(String var0, int[] var1, int var2, int var3, int var4) {
      VqsvUiLayout var5 = VqsvUiLayout.load(var0);

      for(int var6 = 0; var6 < var1.length; ++var6) {
         VqsvUiLayout.UiWidget var7 = var5.widget(var1[var6]);
         if (var7 != null && var2 >= var7.x - 4 && var2 <= var7.x + Math.max(var4, var7.w) + 12 && var3 >= var7.y - 2 && var3 <= var7.y + 14) {
            return var6;
         }
      }

      return -1;
   }

   private static int shopbuyRowAt(int var0, int var1) {
      VqsvUiLayout var2 = VqsvUiLayout.load("shopbuy.ui");

      for(int var3 = 0; var3 < SHOPBUY_ROW_BACKGROUNDS.length; ++var3) {
         VqsvUiLayout.UiWidget var4 = var2.widget(SHOPBUY_ROW_BACKGROUNDS[var3]);
         if (var4 != null && var0 >= var4.x - 8 && var0 <= var4.x + 146 && var1 >= var4.y - 5 && var1 <= var4.y + 23) {
            return var3;
         }
      }

      return -1;
   }

   String selectedLabel() {
      if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return "Lưng bao";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return this.taskSelectedLabelCache;
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return this.taskMapTitle;
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return this.taskOptionData.option(clamp(this.selected, 0, this.taskOptionData.options.length - 1));
      } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
         return this.recordSelected == 0 ? "Minh họa" : "Kỷ lục";
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         List var5 = petmapRows(this.petmapTab);
         return var5.isEmpty() ? "Minh họa" : ((PetmapRow)var5.get(clamp(this.selected, 0, var5.size() - 1))).name;
      } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
         return "Trợ giúp " + (this.helpPage + 1) + "/3";
      } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
         return "Tùy chọn " + this.settingsLevel + "/3";
      } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
         return this.selected == 1 ? "Không" : "Có";
      } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         return RIDE_LABELS[this.rideSelected];
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return TRANSMIT_DESTINATIONS[clamp(this.selected, 0, TRANSMIT_DESTINATIONS.length - 1)];
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
         return PORTABLE_SHOP_LABELS[this.selected];
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         ItemDefinition var4 = this.portableShopSourceItem(this.selected);
         return var4 == null ? "Mua" : var4.name;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         ItemDefinition var3 = this.portableShopSourceItem(this.shopConfirmItemId);
         return var3 == null ? "Xác nhận" : var3.name + " * " + this.shopConfirmQuantity;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
         return portableShopServiceTitle(this.serviceProductId);
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
         return this.sourceWharfLabels()[this.selected];
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
         return this.sourceConvenienceLabels()[this.selected];
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
         return SOURCE_WORLD_SHOP_LABELS[this.selected];
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         return this.sourceSellSelectedLabel;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         return this.sourceSellItemId < 0 ? "Xác nhận" : this.sourceSellSelectedLabel + " * " + this.sourceSellQuantity;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
         return "Khôi phục " + Math.max(0, this.sourceRecoveryCost);
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
         return SOURCE_PET_BANK_LABELS[this.selected];
      } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
         List var2 = SourceFashionCatalog.instance().records();
         return fashionDisplayName((SourceFashionRecord)var2.get(clamp(this.selected, 0, var2.size() - 1)));
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         return FASHION_SHOP_LABELS[clamp(this.selected, 0, FASHION_SHOP_LABELS.length - 1)];
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
         return "Mua " + this.fashionPurchaseQuantity + " túi mù";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM) {
         return "Mở túi mù";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
         return this.fashionRevealTitle;
      } else if (this.mode != VqsvPanelRuntime.Mode.FASHION_EXCHANGE && this.mode != VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
         return this.labels()[this.selected];
      } else {
         List var1 = FashionEconomyCatalog.instance().entries();
         return var1.isEmpty() ? "Đổi mảnh" : fashionDisplayName(((FashionEconomyEntry)var1.get(clamp(this.selected, 0, var1.size() - 1))).sourceFashion);
      }
   }

   String modeName() {
      return this.mode.name();
   }

   String giftCodeInputForTest() {
      return this.giftCodeInput;
   }

   String giftCodeMessageForTest() {
      return this.giftCodeMessage;
   }

   int badgeDisplayCountForTest(VqsvGameRuntime.Scene var1) {
      return badgeDisplayCount(var1);
   }

   int badgeRuntimeIdAtDisplayIndexForTest(VqsvGameRuntime.Scene var1, int var2) {
      return badgeRuntimeIdAtDisplayIndex(var1, var2);
   }

   int selectedBadgeRuntimeIdForTest(VqsvGameRuntime.Scene var1) {
      return this.mode == VqsvPanelRuntime.Mode.BADGE ? badgeRuntimeIdAtDisplayIndex(var1, this.selected) : -1;
   }

   String badgeNameForTest(int var1) {
      return badgeName(var1);
   }

   String badgeDescriptionForTest(VqsvGameRuntime.Scene var1, int var2) {
      return badgeDescription(var1, var2);
   }

   void openBadgeGridForTest(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.BADGE;
      this.selected = badgeDisplayIndexForRuntimeId(var1, var2);
      this.openedTicks = 0;
      this.titlePanelMode = false;
      this.badgeReturnToBag = false;
      this.badgeAwardReturnToWorld = false;
   }

   BufferedImage badgePreviewImageForTest(int var1) {
      return badgePreviewImage(SpriteAnimator.load(257), var1);
   }

   int[] selectedBadgeGridBoundsForTest(VqsvGameRuntime.Scene var1) {
      int var2 = badgeDisplayCount(var1);
      if (var2 <= BADGE_SLOT_WIDGETS.length) {
         VqsvUiLayout.UiWidget var6 = VqsvUiLayout.load("badge.ui").widget(BADGE_SLOT_WIDGETS[this.selected]);
         return var6 == null ? null : new int[]{var6.x, var6.y, Math.max(30, var6.w), 31};
      } else {
         int var3 = badgeGridFirst(this.selected, var2);
         int var4 = Math.min(10, var2 - var3);
         int var5 = this.selected - var3;
         return new int[]{badgeGridX(var5, var4), badgeGridY(var5), 30, 30};
      }
   }

   private void confirm(VqsvGameRuntime.Scene var1) {
      if (this.mode == VqsvPanelRuntime.Mode.GAMEMENU) {
         if (this.selected == 0) {
            this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP;
            this.selected = 0;
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.l confirm selected=0 close gamemenu.ui -> P=14 game.k.aC bodyShop.ui open");
         } else if (this.selected == 1) {
            this.visible = false;
            var1.openWorldPetstate();
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.l confirm selected=1 c=0 o.m -> P=7 game.h.W petstate.ui open");
         } else if (this.selected == 2) {
            this.mode = VqsvPanelRuntime.Mode.BAG;
            this.selected = 0;
            this.bagTab = 0;
            this.listScroll = 0;
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.l confirm selected=2 o.m -> P=8 game.h.Y bag.ui open b=0 title=Vat pham");
         } else if (this.selected == 3) {
            this.mode = VqsvPanelRuntime.Mode.RECORD;
            this.selected = 0;
            this.recordSelected = 0;
            this.recordMessageMode = 0;
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.l confirm selected=3 c=0 o.a(9) -> game.h.N record.ui open");
         } else if (this.selected == 4) {
            this.mode = VqsvPanelRuntime.Mode.TASK;
            this.taskReturnToWorld = false;
            this.taskTab = 0;
            this.listScroll = 0;
            this.selected = mainTaskCursor(var1);
            this.keepSelectedVisible(taskRowsForRender(var1, this.taskTab).size());
            this.openedTicks = 0;
            this.updateTaskSelectedLabel(var1);
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.l confirm selected=4 b=0 o.a(10) -> game.h.R task.ui open main tab");
         } else if (this.selected == 5) {
            this.mode = VqsvPanelRuntime.Mode.SAVE;
            this.savePhase = 0;
            this.sourceRecoverySavePhase = 0;
            this.saveMessage = "Có lưu dữ liệu không?";
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.l confirm selected=5 hide widgets 11/12 -> P=22 game.h.H msgtip.ui open text=Co luu du lieu khong?");
         } else if (this.selected == 6) {
            this.mode = VqsvPanelRuntime.Mode.WARDROBE;
            this.selected = wardrobeSelectedIndex(var1);
            this.listScroll = 0;
            this.keepSelectedVisible(SourceFashionCatalog.instance().records().size());
            this.wardrobePreviewKey = null;
            this.wardrobePreviewAnimator = null;
            this.openedTicks = 0;
            List var4 = var1.session.story.trace();
            int var7 = var1.session.fashion.ownedCount();
            var4.add("FASH-I2 wardrobe open owned=" + var7 + " selected=" + var1.session.fashion.selectedStableKey());
         } else if (this.selected == 7) {
            this.mode = VqsvPanelRuntime.Mode.CHALLENGE;
            this.selected = 0;
            this.challengeRegion = REGIONAL_CHALLENGES.resolveRegion(var1.session.world.currentSceneId, var1.session.world.currentRoomIndex, var1.session.progression.badges);
            this.openedTicks = 0;
            List var3 = var1.session.story.trace();
            int var6 = this.challengeRegion;
            var3.add("UNIFIED-DESIGN repeatable challenge open region=" + var6 + " commissionUi=removed giftcodeUi=enabled unlocked=" + REGIONAL_CHALLENGES.unlockedRegions(var1.session.progression.badges).size());
         } else if (this.selected == 8) {
            this.mode = VqsvPanelRuntime.Mode.BATTLE_PASS;
            this.selected = 1;
            this.battlePassTrack = 0;
            this.battlePassMessage = "";
            this.listScroll = 0;
            this.openedTicks = 0;
            var1.session.story.trace().add("BATTLE-PASS season=vqsv-fan-card-s1 open");
         } else {
            List var2 = var1.session.story.trace();
            int var5 = this.selected;
            var2.add("PENDING panel game.h.l confirm selected=" + var5 + " label=" + MENU_LABELS[this.selected] + " sourceTargetP=" + sourceTargetState(this.selected) + " subpage not implemented in gamemenu slice");
         }
      } else if (this.selected == 0) {
         this.visible = false;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n confirm selected=0 close gamesystem.ui -> P=0");
      } else if (this.selected == 1) {
         this.mode = VqsvPanelRuntime.Mode.HELP;
         this.helpPage = 0;
         this.selected = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n confirm selected=1 o.a(20) close gamesystem.ui -> game.h.u help1.ui open r=0");
      } else if (this.selected == 2) {
         this.mode = VqsvPanelRuntime.Mode.SETTINGS;
         this.selected = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n confirm selected=2 o.a(21) close gamesystem.ui -> game.h.w help.ui settings open g=" + this.settingsLevel);
      } else if (this.selected == 3) {
         this.mode = VqsvPanelRuntime.Mode.OPTION_CONFIRM;
         this.selected = 1;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n confirm selected=3 f=0 open option.ui c=1 widget12=Co widget13=Khong");
      } else {
         List var10000 = var1.session.story.trace();
         int var10001 = this.selected;
         var10000.add("PENDING panel game.h.n confirm selected=" + var10001 + " label=" + SYSTEM_LABELS[this.selected] + " sourceTargetP=" + sourceSystemTargetState(this.selected) + " subpage not implemented in gamesystem slice");
      }
   }

   private void tickBag(VqsvGameRuntime.Scene var1) {
      if (this.eggPickerOpen) {
         this.tickEggPicker(var1);
      } else if (this.bagMessageMode != 0) {
         if (var1.text != null && var1.text.readyForKey && var1.key0) {
            var1.text.confirm();
            if (var1.text.disposed) {
               int var5 = this.bagMessageMode;
               if (var5 == 18) {
                  var1.text = TextBox.openBox(this.pendingBagOpenBoxMessage);
                  this.bagMessageMode = 19;
                  var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case0 f=2 close msgwarm.ui -> openbox.ui species=" + this.pendingHatchSpecies + " storageResult=" + this.pendingHatchStorageResult);
               } else {
                  var1.text = null;
                  this.bagMessageMode = 0;
                  String var6 = var5 == 19 ? "f=3->0" : "f=1->0";
                  String var10 = var5 == 19 ? "openbox.ui" : "msgwarm.ui";
                  var1.session.story.trace().add("PORTED panel game.h.ac bag " + var10.replace(".ui", "") + " key=196640 close " + var10 + " " + var6 + " mode=" + var5 + " remain bag.ui b=" + this.bagTab + " selected=" + this.selected);
               }
            }
         }

      } else {
         List var2 = bagRows(var1, this.bagTab);
         int var3 = Math.max(0, var2.size() - 1);
         this.selected = clamp(this.selected, 0, var3);
         if (!var1.keyLeft && !var1.keyRight) {
            if (var1.keyUp) {
               int var7 = this.selected;
               this.selected = clamp(this.selected - 1, 0, var3);
               this.keepSelectedVisible(var2.size());
               if (this.selected != var7) {
                  var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac key=4100 bagTab=" + this.bagTab + " selected=" + this.selected);
               }
            } else if (var1.keyDown) {
               int var8 = this.selected;
               this.selected = clamp(this.selected + 1, 0, var3);
               this.keepSelectedVisible(var2.size());
               if (this.selected != var8) {
                  var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac key=8448 bagTab=" + this.bagTab + " selected=" + this.selected);
               }
            } else if (var1.keyBack) {
               this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
               this.selected = 2;
               this.resetGameMenuViewport();
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac back b=" + this.bagTab + " o.a(6) close bag.ui -> gamemenu.ui selected=2");
            } else if (var1.key0) {
               if (var2.isEmpty()) {
                  var1.session.story.trace().add("PENDING panel game.h.ac confirm bag empty bagTab=" + this.bagTab + " item use not implemented in bag slice");
               } else {
                  BagRow var9 = (BagRow)var2.get(this.selected);
                  if (this.bagTab == 3) {
                     this.useSpecialBagRow(var1, var9);
                     return;
                  }

                  if (this.bagTab == 1) {
                     if (!var9.item.mechanicsImplemented) {
                        this.showUnifiedItemUnavailable(var1, var9, "bag-equipment");
                        return;
                     }

                     if (RainbowCharmCatalog.instance().byRuntimeId(var9.item.id) != null) {
                        if (var1.session.story.mainTaskProgress < 5) {
                           var1.text = TextBox.msgWarm("Hoàn thành hướng dẫn trang bị Pet để mở Bùa Hộ Trận.", "Nhấn nút 5 để tiếp tục");
                           var1.session.story.trace().add("UNIFIED RAINBOW-CHARM bag manager blocked task=" + var1.session.story.mainTaskProgress + " runtimeId=" + var9.item.id);
                           return;
                        }

                        this.rainbowCharmReturnToBag = true;
                        this.rainbowCharmReturnBagSelected = this.selected;
                        this.rainbowCharmReturnBagScroll = this.listScroll;
                        this.mode = VqsvPanelRuntime.Mode.RAINBOW_CHARM;
                        this.rainbowCharmTab = 0;
                        this.selected = 0;
                        this.listScroll = 0;
                        this.rainbowCharmMessage = "Quản lý bùa đội đang sở hữu.";
                        var1.session.story.trace().add("UNIFIED RAINBOW-CHARM bag equipment -> manager runtimeId=" + var9.item.id);
                        return;
                     }

                     List var10000 = var1.session.story.trace();
                     int var10001 = var9.item.id;
                     var10000.add("PORTED/PARTIAL panel game.h.ac bagTab=1 q.M confirm equipmentId=" + var10001 + " name=" + var9.item.name + " status=" + var9.statusText() + " render/navigate/back only; equip action stays in petsetting.ui");
                     return;
                  }

                  if (this.bagTab == 2) {
                     if (!var9.item.mechanicsImplemented) {
                        this.showUnifiedItemUnavailable(var1, var9, "bag-material");
                        return;
                     }

                     var1.session.story.trace().add("PORTED panel game.h.ac bagTab=2 q.N material confirm itemId=" + var9.item.id + " name=" + var9.item.name + " count=" + var9.count + " source b=2 has no confirm action; render/navigate/back only");
                     return;
                  }

                  if (!var9.item.mechanicsImplemented) {
                     this.showUnifiedItemUnavailable(var1, var9, "bag-item");
                     return;
                  }

                  if (var9.item.id >= 0 && var9.item.id <= 3) {
                     var1.text = TextBox.msgWarm("Không thể sử dụng", "Nhấn nút 5 để tiếp tục");
                     this.bagMessageMode = 1;
                     var1.session.story.trace().add("PORTED panel game.h.ac bagTab=0 itemId=" + var9.item.id + " case 0..3 -> msgwarm.ui f=1 text=Khong the su dung count=" + var9.count + " no inventory mutation");
                     return;
                  }

                  if (var9.item.id == 13) {
                     this.useAvoidMonsterItem(var1, var9);
                     return;
                  }

                  if (var9.item.id == 14) {
                     this.useEggAcceleratorItem(var1, var9);
                     return;
                  }

                  this.visible = false;
                  var1.openPanelBagState17Petstate(var9.item.id);
                  var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac confirm itemId=" + var9.item.id + " name=" + var9.item.name + " count=" + var9.count + " bagTab=0 default -> this.s=itemId P=17 navigation/back only; confirm game.h.bo PENDING");
               }
            }
         } else {
            int var4 = this.bagTab;
            if (var1.keyRight) {
               this.bagTab = (this.bagTab + 1) % 4;
            } else {
               this.bagTab = (this.bagTab + 3) % 4;
            }

            this.selected = 0;
            this.listScroll = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac tab key b=" + var4 + "->" + this.bagTab + " title=" + bagTabTitle(this.bagTab));
         }

      }
   }

   void returnToBagFromState17Back(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.BAG;
      this.bagTab = 0;
      this.openedTicks = 0;
      this.selected = clamp(this.selected, 0, Math.max(0, bagRows(var1, this.bagTab).size() - 1));
      this.keepSelectedVisible(bagRows(var1, this.bagTab).size());
      var1.session.story.trace().add("PORTED/PARTIAL panel game.h.Z back itemId=" + var2 + " o.a(8) close petstate.ui -> bag.ui b=0 selected=" + this.selected);
   }

   void returnToBagFromSpecialUseBack(VqsvGameRuntime.Scene var1, int var2) {
      this.visible = true;
      this.mode = VqsvPanelRuntime.Mode.BAG;
      this.bagTab = 3;
      this.openedTicks = 0;
      this.selected = clamp(this.selected, 0, Math.max(0, bagRows(var1, this.bagTab).size() - 1));
      this.keepSelectedVisible(bagRows(var1, this.bagTab).size());
      var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ab back/close state19 specialId=" + var2 + " o.a(8) close petstate.ui -> bag.ui b=3 selected=" + this.selected);
   }

   private void useAvoidMonsterItem(final VqsvGameRuntime.Scene var1, BagRow var2) {
      BattleItemRow var3 = VqsvBattleTables.instance().item(13);
      int var4 = var3 == null ? 0 : var3.paramA;
      PanelDecisionService.AvoidMonsterResult var5 = PANEL_DECISIONS.useAvoidMonsterItem(new PanelDecisionService.AvoidMonsterPort() {
         {
            Objects.requireNonNull(VqsvPanelRuntime.this);
         }

         public int ticks() {
            return var1.session.progression.avoidMonsterTicks;
         }

         public void activate(int var1x) {
            var1.session.progression.avoidMonsterTicks = var1x;
            var1.session.progression.avoidMonsterElapsed = 0;
         }
      }, var1.session.world.currentSceneId, var1.session.world.currentRoomIndex, 13, var4, 1, var1.session.inventory.bagItems, var1.session.inventory.specialRewards);
      PanelDecisionService.AvoidMonsterRoute var6 = PANEL_DECISIONS.routeAvoidMonster(var5);
      if (var6.outcome == PanelDecisionService.AvoidMonsterRouteOutcome.ALREADY_ACTIVE_WARNING) {
         var1.text = TextBox.msgWarm("Đã có được thời gian ngắn tránh quái hiệu quả", "Nhấn nút 5 để tiếp tục");
         this.bagMessageMode = var6.bagMessageMode;
         var1.session.story.trace().add("PORTED panel game.h.ac bagTab=0 itemId=13 q.y=" + var5.ticksAfter + " already-active -> msgwarm.ui f=1 no inventory mutation");
      } else if (var6.outcome == PanelDecisionService.AvoidMonsterRouteOutcome.FORBIDDEN_LOCATION_WARNING) {
         var1.text = TextBox.msgWarm("Nơi này không cách nào sử dụng tránh quái hoàn", "Nhấn nút 5 để tiếp tục");
         this.bagMessageMode = var6.bagMessageMode;
         var1.session.story.trace().add("PORTED panel game.h.ac bagTab=0 itemId=13 source room game.k.a().f/g=3/7 forbidden -> msgwarm.ui f=1 no inventory mutation");
      } else if (var6.outcome == PanelDecisionService.AvoidMonsterRouteOutcome.MISSING_ITEM_NO_UI) {
         var1.session.story.trace().add("PENDING panel game.h.ac bagTab=0 itemId=13 source q.b(item,1,0)=false unexpected visible row count=" + var2.count);
      } else {
         if (var6.clampSelection) {
            List var7 = bagRows(var1, this.bagTab);
            this.selected = clamp(this.selected, 0, Math.max(0, var7.size() - 1));
         }

         var1.text = TextBox.msgWarm("Thành công sử dụng đạo cụ, cũng có thời gian ngắn tránh quái hiệu quả", "Nhấn nút 5 để tiếp tục");
         this.bagMessageMode = var6.bagMessageMode;
         var1.session.story.trace().add("PORTED panel game.h.ac bagTab=0 itemId=13 q.d(item,1,0) count=" + var5.inventoryAfter + " q.y=aq.c[4][13][6]=" + var5.ticksAfter + " q.x=0 q.c(1) stack=" + var5.rewardStackAfter + " -> msgwarm.ui f=1 selected=" + this.selected);
      }
   }

   private void useEggAcceleratorItem(VqsvGameRuntime.Scene var1, BagRow var2) {
      List var3 = this.availableEggRewardIds(var1);
      if (var3.isEmpty()) {
         var1.text = TextBox.msgWarm("Không có trứng có thể ấp trứng", "Nhấn nút 5 để tiếp tục");
         this.bagMessageMode = 16;
         var1.session.story.trace().add("PORTED panel game.h.ac item14 no egg reward available");
      } else if (var3.size() > 1) {
         this.eggPickerRewardIds = var3.stream().mapToInt((value) -> (Integer)value).toArray();
         String[] var7 = new String[this.eggPickerRewardIds.length];

         for(int var5 = 0; var5 < this.eggPickerRewardIds.length; ++var5) {
            int var6 = this.eggPickerRewardIds[var5];
            var7[var5] = V4EggCatalog.isV4Egg(var6) ? V4EggCatalog.instance().name(var6) : "Trứng sủng vật";
         }

         this.eggPickerOpen = true;
         var1.choice = ChoiceBox.optionUi(0, var7);
         var1.session.story.trace().add("UNIFIED-DESIGN panel item14 egg picker options=" + String.valueOf(var3));
      } else {
         int var4 = (Integer)var3.get(0);
         if (var1.session.progression.egg.active && var3.contains(var1.session.progression.egg.activeEggItemId)) {
            var4 = var1.session.progression.egg.activeEggItemId;
         }

         this.applyEggAccelerator(var1, var4, var2);
      }
   }

   private void tickEggPicker(VqsvGameRuntime.Scene var1) {
      if (var1.choice == null) {
         this.eggPickerOpen = false;
      } else {
         if (var1.keyUp) {
            var1.choice.move(-1);
         } else if (var1.keyDown) {
            var1.choice.move(1);
         } else if (var1.keyBack) {
            var1.choice = null;
            this.eggPickerOpen = false;
            this.eggPickerRewardIds = new int[0];
            var1.session.story.trace().add("UNIFIED-DESIGN panel item14 egg picker cancelled");
         } else if (var1.key0) {
            int var2 = this.eggPickerRewardIds[var1.choice.selectedIndex()];
            var1.choice = null;
            this.eggPickerOpen = false;
            this.eggPickerRewardIds = new int[0];
            this.applyEggAccelerator(var1, var2, (BagRow)null);
         }

      }
   }

   private List<Integer> availableEggRewardIds(VqsvGameRuntime.Scene var1) {
      ArrayList var2 = new ArrayList();
      int[] var3 = new int[]{0, 11, 12};

      for(int var7 : var3) {
         SpecialRewardState var8 = (SpecialRewardState)var1.session.inventory.specialRewards.get(var7);
         if (var8 != null && var8.stackCount > 0) {
            var2.add(var7);
         }
      }

      return var2;
   }

   private void applyEggAccelerator(VqsvGameRuntime.Scene var1, int var2, BagRow var3) {
      if (var1.session.progression.egg.selectForIncubation(var2)) {
         EggService.AccelerationResult var4 = EGG_SERVICE.accelerate(var1.session.progression.egg, var1.session.inventory.bagItems, 14);
         EggService.AccelerationRoute var5 = EGG_SERVICE.routeAcceleration(var4);
         if (var5.outcome == EggService.AccelerationRouteOutcome.NOT_AVAILABLE_WARNING) {
            var1.text = TextBox.msgWarm("Không có trứng có thể ấp trứng", "Nhấn nút 5 để tiếp tục");
            this.bagMessageMode = var5.bagMessageMode;
            var1.session.story.trace().add("PORTED panel game.h.ac bagTab=0 itemId=14 q.k(0)=" + var4.eggActive + " q.I=" + var4.eggType + " game.k.q=" + var4.progressAfter + " target=" + var4.targetProgress + " -> msgwarm.ui f=1 no inventory mutation");
         } else if (var5.outcome == EggService.AccelerationRouteOutcome.MISSING_ITEM_NO_UI) {
            List var10000 = var1.session.story.trace();
            int var10001 = var3 == null ? 0 : var3.count;
            var10000.add("PENDING panel game.h.ac bagTab=0 itemId=14 source q.b(item,1,0)=false unexpected visible row count=" + var10001);
         } else {
            if (var5.clampSelection) {
               List var6 = bagRows(var1, this.bagTab);
               this.selected = clamp(this.selected, 0, Math.max(0, var6.size() - 1));
            }

            var1.text = TextBox.msgWarm("Thành công sử dụng, tranh thủ thời gian đi ấp trứng trứng sủng vật a!", "Nhấn nút 5 để tiếp tục");
            this.bagMessageMode = var5.bagMessageMode;
            var1.session.story.trace().add("PORTED panel game.h.ac bagTab=0 itemId=14 q.k(0)=true eggItemId=" + var2 + " q.I=" + var4.eggType + " game.k.q=" + var4.progressAfter + " q.d(item,1,0) count=" + var4.inventoryAfter + " -> msgwarm.ui f=1 selected=" + this.selected);
         }
      }
   }

   private void useEggHatchAction(VqsvGameRuntime.Scene var1, BagRow var2) {
      if (!var2.specialEgg) {
         var1.session.story.trace().add("PENDING panel game.h.ac bagTab=3 confirm specialId=" + var2.item.id + " only q.O case0 hatch ported");
      } else {
         if (!var1.session.progression.egg.active) {
            var1.session.progression.egg.selectForIncubation(var2.specialId);
         } else if (var1.session.progression.egg.activeEggItemId != var2.specialId) {
            var1.text = TextBox.msgWarm("Trứng này chưa được chọn để ấp.", "Nhấn nút 5 để tiếp tục");
            this.bagMessageMode = 20;
            var1.session.story.trace().add("UNIFIED-DESIGN panel egg hatch wrong active item row=" + var2.specialId + " active=" + var1.session.progression.egg.activeEggItemId);
            return;
         }

         int var3 = var1.session.progression.egg.activeEggItemId;
         EggService.RandomPort var4 = (var1x, var2x) -> {
            int roll = var1.session.progression.egg.previewDraw(var1x, var2x);
            var1.session.story.trace().add("DETERMINISTIC panel.bag.eggHatch." + var1x + " drawIndex=" + var1.session.progression.egg.drawIndex() + " bound=" + var2x + " roll=" + roll);
            return roll;
         };
         EggService.HatchResult var5;
         if (V4EggCatalog.isV4Egg(var3)) {
            var5 = EGG_SERVICE.hatchFromPool(var1.session.progression.egg, var1.session.pets.roster, var1.session.pets.bank, (new PetBankExpansionService()).capacity(var1.session), var4, (var1x) -> var1.session.progression.collection.collected(var1x), V4EggCatalog.instance().species(var3), "egg.v4.species." + var3, (var1x, var2x) -> sourceHatchedPet(var1, var1x, var2x), (SpecialRewardState)var1.session.inventory.specialRewards.get(var3));
         } else {
            var5 = EGG_SERVICE.hatchUnified(var1.session.progression.egg, var1.session.pets.roster, var1.session.pets.bank, (new PetBankExpansionService()).capacity(var1.session), var4, (var1x) -> var1.session.progression.collection.collected(var1x), true, (var1x, var2x) -> sourceHatchedPet(var1, var1x, var2x), (SpecialRewardState)var1.session.inventory.specialRewards.get(0));
         }

         EggService.HatchRoute var6 = EGG_SERVICE.routeHatch(var5);
         if (var6.outcome == EggService.HatchRouteOutcome.INACTIVE_NO_UI) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case0 q.k(0)=false -> source breaks without UI");
         } else if (var6.outcome == EggService.HatchRouteOutcome.NOT_READY_WARNING) {
            var1.text = TextBox.msgWarm("Vẫn chưa thể ấp trứng", "Nhấn nút 5 để tiếp tục");
            this.bagMessageMode = var6.bagMessageMode;
            var1.session.story.trace().add("PORTED panel game.h.ac bagTab=3 q.O case0 q.k(0)=true game.k.r=false q.I=" + var5.eggTypeBefore + " game.k.q=" + var5.progress + " -> msgwarm.ui f=1");
         } else if (var6.outcome == EggService.HatchRouteOutcome.CREATION_FAILED_NO_UI) {
            var1.session.story.trace().add("PENDING panel game.h.ac bagTab=3 q.O case0 hatch factory returned null species=" + var5.speciesId + " storageResult=" + var5.storageResult + " no egg mutation");
         } else {
            NiemThoEggQuestProgress.recordHatch(var1, var5);
            var1.session.progression.collection.markCollected(var5.speciesId);
            BattleSpeciesRow var7 = VqsvBattleTables.instance().species(var5.speciesId);
            String var8 = var7 == null ? "Pet " + var5.speciesId : var7.name("Pet " + var5.speciesId);
            if (var6.clampSelection) {
               this.selected = clamp(this.selected, 0, Math.max(0, bagRows(var1, this.bagTab).size() - 1));
            }

            if (var6.prepareOpenBox) {
               this.pendingHatchSpecies = var5.speciesId;
               this.pendingHatchStorageResult = var5.storageResult;
               this.pendingBagOpenBoxMessage = VqsvText.Battle.panelBagEggHatchResult(var8, var5.storageResult);
            }

            var1.text = TextBox.msgWarm("Ấp trứng thành công", "Nhấn nút 5 để tiếp tục");
            this.bagMessageMode = var6.bagMessageMode;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case0 game.k.r=true q.y()=" + var5.storageResult + " game.k.q=0 q.j(0) q.k(0)=false species=" + var5.speciesId + " q.I=" + var5.eggTypeAfter + " party=" + var5.rosterSize + " bank=" + var5.bankSize + " -> msgwarm.ui f=2 then openbox.ui f=3");
         }
      }
   }

   private void useSpecialBagRow(VqsvGameRuntime.Scene var1, BagRow var2) {
      if (!var2.item.mechanicsImplemented) {
         this.showUnifiedItemUnavailable(var1, var2, "bag-special");
      } else if (var2.specialEgg) {
         this.useEggHatchAction(var1, var2);
      } else {
         switch (var2.specialId) {
            case 1:
            case 2:
            case 3:
            case 4:
               var1.session.story.trace().add("PORTED panel game.h.ac bagTab=3 q.O case" + var2.specialId + " confirm source has no action branch source render disables widget7; render/navigate/back only");
               return;
            case 5:
               this.petmapReturnToBag = true;
               this.petmapReturnBagSelected = this.selected;
               this.petmapReturnBagScroll = this.listScroll;
               this.mode = VqsvPanelRuntime.Mode.PETMAP;
               this.selected = 0;
               this.petmapTab = 0;
               this.listScroll = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED panel game.k.af bagTab=3 q.O case5 confirm -> o.a(11) game.k.S petmap.ui sourceRow=[" + var2.item.textId + "," + var2.item.iconCell + "," + var2.item.descriptionTextId + "] close bag.ui petmapTab=0 returnState=8");
               return;
            case 6:
               this.badgeReturnToBag = true;
               this.badgeReturnBagSelected = this.selected;
               this.badgeReturnBagScroll = this.listScroll;
               this.mode = VqsvPanelRuntime.Mode.BADGE;
               this.selected = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED panel game.h.ac bagTab=3 q.O case6 confirm -> o.a(12) game.h.W badge.ui sourceRow=[" + var2.item.textId + "," + var2.item.iconCell + "," + var2.item.descriptionTextId + "] close bag.ui previousState=8");
               return;
            case 7:
            case 8:
            case 9:
               this.visible = false;
               var1.openPanelBagSpecialUsePetstate(var2.specialId);
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case" + var2.specialId + " confirm -> s=id o.a(19) close bag.ui open petstate.ui stack=" + var2.count);
               return;
            case 10:
               this.transmitReturnBagSelected = this.selected;
               this.transmitReturnBagScroll = this.listScroll;
               this.mode = VqsvPanelRuntime.Mode.TRANSMIT;
               this.selected = 0;
               this.listScroll = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED panel game.h.ac bagTab=3 q.O case10 confirm -> o.a(24) game.k.h transmit.ui sourceRow=[" + var2.item.textId + "," + var2.item.iconCell + "," + var2.item.descriptionTextId + "] close bag.ui previousState=8 destinations=" + TRANSMIT_DESTINATIONS.length);
               return;
            default:
               var1.session.story.trace().add("PENDING panel game.h.ac bagTab=3 q.O case" + var2.specialId + " confirm invalid special reward id; no source group5 row");
         }
      }
   }

   private void showUnifiedItemUnavailable(VqsvGameRuntime.Scene var1, BagRow var2, String var3) {
      var1.text = TextBox.msgWarm("Chức năng của vật phẩm này chưa được mở.", "Nhấn nút 5 để tiếp tục");
      this.bagMessageMode = 1;
      var1.session.story.trace().add("UNIFIED_ITEM feature-not-open route=" + var3 + " stableKey=" + var2.item.stableKey + " mechanic=" + var2.item.name + " runtimeId=" + var2.item.id + " count=" + var2.count + " mutation=zero");
   }

   private void tickTransmit(VqsvGameRuntime.Scene var1) {
      int var2 = TRANSMIT_ENGINE.destinationCount() - 1;
      this.selected = clamp(this.selected, 0, var2);
      if (var1.keyUp) {
         int var3 = this.selected;
         this.selected = clamp(this.selected - 1, 0, var2);
         this.keepSelectedVisible(TRANSMIT_DESTINATIONS.length);
         if (this.selected != var3) {
            var1.session.story.trace().add("PORTED panel game.k.i transmit key=4100 selected=" + this.selected + " label=" + TRANSMIT_DESTINATIONS[this.selected]);
         }
      } else if (var1.keyDown) {
         int var4 = this.selected;
         this.selected = clamp(this.selected + 1, 0, var2);
         this.keepSelectedVisible(TRANSMIT_DESTINATIONS.length);
         if (this.selected != var4) {
            var1.session.story.trace().add("PORTED panel game.k.i transmit key=8448 selected=" + this.selected + " label=" + TRANSMIT_DESTINATIONS[this.selected]);
         }
      } else if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.BAG;
         this.bagTab = 3;
         this.selected = this.transmitReturnBagSelected;
         this.listScroll = this.transmitReturnBagScroll;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED panel game.k.i transmit back o.a(8) close transmit.ui -> P=8 bag.ui tab=3 selected=" + this.selected);
      } else if (var1.key0) {
         WorldTransmitEngine.TransmitRequest var5 = TRANSMIT_ENGINE.request(this.selected);
         this.selected = var5.destinationIndex;
         var1.session.world.transmitScene = var5.sceneId;
         var1.session.world.transmitRoom = var5.roomIndex;
         var1.session.world.transmitX = var5.x;
         var1.session.world.transmitY = var5.y;
         var1.session.world.transmitG = var5.worldG;
         var1.session.world.transmitT = var5.worldT;
         var1.session.world.transmitConfirmed = true;
         this.visible = false;
         var1.session.story.trace().add("PORTED panel game.k.i transmit confirm h=" + this.selected + " label=" + TRANSMIT_DESTINATIONS[this.selected] + " -> game.l.B().p/q/r/s=[" + var5.sceneId + "," + var5.roomIndex + "," + var5.x + "," + var5.y + "] game.l.G=" + var5.worldG + " game.l.B().t=" + var5.worldT + " game.f.B().a(9) no q.O consume");
      }

   }

   private static boolean sourceEggReady(VqsvGameRuntime.Scene var0) {
      return var0.session.progression.egg.ready();
   }

   private static PetState sourceHatchedPet(VqsvGameRuntime.Scene var0, int var1, int var2) {
      BattleSpeciesRow var3 = VqsvBattleTables.instance().species(var1);
      int var4 = var3 == null ? 0 : var3.element * 10;
      PetState var5 = PetSourceAdapter.create(0, var1, 5, -1, 2, var4, -1);
      var0.session.story.trace().add("PORTED/PARTIAL panel game.h.g hatch addPet species=" + var1 + " level=5 sourceQuality=-1 resolvedQuality=" + var5.quality + " nature=2 skillPayload=[1," + var5.skillIds[0] + "," + var5.skillCooldowns[0] + "] q.y()=" + var2);
      return var5;
   }

   private void tickTask(VqsvGameRuntime.Scene var1) {
      List var2 = taskRowsForRender(var1, this.taskTab);
      int var3 = Math.max(0, var2.size() - 1);
      this.selected = clamp(this.selected, 0, var3);
      this.keepSelectedVisible(var2.size());
      if (var1.keyUp) {
         int var4 = this.selected;
         this.selected = clamp(this.selected - 1, 0, var3);
         this.keepSelectedVisible(var2.size());
         if (this.selected != var4) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.S key=4100 taskTab=" + this.taskTab + " selected=" + this.selected);
         }
      } else if (var1.keyDown) {
         int var5 = this.selected;
         this.selected = clamp(this.selected + 1, 0, var3);
         this.keepSelectedVisible(var2.size());
         if (this.selected != var5) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.S key=8448 taskTab=" + this.taskTab + " selected=" + this.selected);
         }
      } else if (var1.keyLeft) {
         if (this.taskTab != 0) {
            this.taskTab = 0;
            this.listScroll = 0;
            this.selected = mainTaskCursor(var1);
            this.keepSelectedVisible(taskRowsForRender(var1, this.taskTab).size());
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.S key=16400 task tab main b=0 ba/bb");
         }
      } else if (var1.keyRight) {
         if (this.taskTab != 1) {
            this.taskTab = 1;
            this.selected = 0;
            this.listScroll = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.S key=32832 task tab branch b=1 ba/bb");
         }
      } else if (var1.keyBack) {
         if (this.taskReturnToWorld) {
            this.visible = false;
            this.taskReturnToWorld = false;
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED source task.ui shortcut back -> P=0 world.ui");
            this.updateTaskSelectedLabel(var1);
            return;
         }

         this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
         this.selected = 4;
         this.resetGameMenuViewport();
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.S back close task.ui -> P=6 gamemenu selected=4");
      } else if (var1.key0) {
         this.openTaskMap(var1, var2);
      }

      this.updateTaskSelectedLabel(var1);
   }

   private void openTaskMap(VqsvGameRuntime.Scene var1, List<TaskRow> var2) {
      this.taskMapReturnTab = this.taskTab;
      this.taskMapReturnSelected = this.selected;
      this.taskMapReturnScroll = this.listScroll;
      if (var2.isEmpty()) {
         this.taskMapNumber = -1;
         this.taskMapTitle = "Nhiệm vụ";
         this.taskMapDetail = "Chưa có nhiệm vụ để chỉ đường.";
      } else {
         TaskRow var3 = (TaskRow)var2.get(clamp(this.selected, 0, var2.size() - 1));
         this.taskMapNumber = var3.number;
         this.taskMapTitle = var3.title;
         this.taskMapDetail = var3.detail;
      }

      this.mode = VqsvPanelRuntime.Mode.TASK_MAP;
      this.openedTicks = 0;
      var1.session.story.trace().add("PC_QOL panel task.ui confirm opens task map hint taskTab=" + this.taskMapReturnTab + " selected=" + this.taskMapReturnSelected + " scene=[" + var1.session.world.currentSceneId + "," + var1.session.world.currentRoomIndex + "] player=[" + var1.player.x + "," + var1.player.y + "]");
   }

   private void tickTaskMap(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack || var1.key0) {
         this.mode = VqsvPanelRuntime.Mode.TASK;
         this.taskTab = this.taskMapReturnTab;
         this.selected = this.taskMapReturnSelected;
         this.listScroll = this.taskMapReturnScroll;
         this.openedTicks = 0;
         this.updateTaskSelectedLabel(var1);
         var1.session.story.trace().add("PC_QOL panel task map hint back -> task.ui taskTab=" + this.taskTab + " selected=" + this.selected);
      }

   }

   private void updateTaskSelectedLabel(VqsvGameRuntime.Scene var1) {
      List var2 = taskRowsForRender(var1, this.taskTab);
      this.taskSelectedLabelCache = var2.isEmpty() ? "Nhiệm vụ" : ((TaskRow)var2.get(clamp(this.selected, 0, var2.size() - 1))).title;
   }

   private void tickTaskOption(VqsvGameRuntime.Scene var1) {
      int var2 = Math.max(0, this.taskOptionData.options.length - 1);
      this.selected = clamp(this.selected, 0, var2);
      if (var1.keyUp) {
         int var3 = this.selected;
         this.selected = clamp(this.selected - 1, 0, var2);
         if (this.selected != var3) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.k.aG key=4100 taskOption selected=" + this.selected);
         }
      } else if (var1.keyDown) {
         int var4 = this.selected;
         this.selected = clamp(this.selected + 1, 0, var2);
         if (this.selected != var4) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.k.aG key=8448 taskOption selected=" + this.selected);
         }
      } else if (var1.key0) {
         int var5 = this.selected;
         this.closeTaskOption(var1, "confirm result=" + var5, var5);
      } else if (var1.keyBack) {
         this.closeTaskOption(var1, "back result=1", 1);
      }

   }

   private void closeTaskOption(VqsvGameRuntime.Scene var1, String var2) {
      this.closeTaskOption(var1, var2, -1);
   }

   private void closeTaskOption(VqsvGameRuntime.Scene var1, String var2, int var3) {
      PanelDecisionService.TaskOptionResult var4 = PANEL_DECISIONS.resolveTaskOption(this.taskOptionBranchTaskId, var3);
      if (var4.outcome == PanelDecisionService.TaskOptionOutcome.ACCEPT) {
         var1.sourceAcceptBranchTask(var4.branchTaskId);
      } else if (var4.outcome == PanelDecisionService.TaskOptionOutcome.DECLINE) {
         var1.session.story.trace().add("PORTED source game.e opcode49 branch task not accepted taskId=" + var4.branchTaskId + " result=" + var4.result);
      }

      var1.session.story.trace().add("PORTED/PARTIAL panel game.k.aG " + var2 + " close taskOption.ui");
      this.taskOptionBranchTaskId = -1;
      if (this.taskOptionReturnToTask) {
         this.mode = VqsvPanelRuntime.Mode.TASK;
         this.selected = this.taskSelectedBeforeOption;
         this.openedTicks = 0;
         this.updateTaskSelectedLabel(var1);
      } else {
         this.visible = false;
      }
   }

   private void tickRecord(VqsvGameRuntime.Scene var1) {
      if (this.recordMessageMode != 0) {
         if (var1.text != null && var1.text.readyForKey && var1.key0) {
            var1.text.confirm();
            if (var1.text.disposed) {
               var1.text = null;
               int var3 = this.recordMessageMode;
               this.recordMessageMode = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O record msgwarm key=196640 close msgwarm.ui f=" + var3 + "->0 stay record.ui selected=" + this.recordSelected);
            }
         }

      } else {
         if (var1.keyLeft) {
            if (this.recordSelected != 0) {
               this.recordSelected = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O key=16400 record selected=0");
            }
         } else if (var1.keyRight) {
            if (this.recordSelected != 1) {
               this.recordSelected = 1;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O key=32832 record selected=1");
            }
         } else if (var1.keyBack) {
            this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
            this.selected = 3;
            this.resetGameMenuViewport();
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O back b=3 o.a(6) close record.ui -> gamemenu.ui selected=3");
         } else if (var1.key0) {
            if (this.recordSelected == 0) {
               PanelDecisionService.RecordAccessResult var2 = PANEL_DECISIONS.recordAccess(var1.session.inventory.specialRewards, 5);
               if (var2.outcome == PanelDecisionService.RecordAccessOutcome.LOCKED) {
                  var1.text = TextBox.msgWarm("Không đạt được sủng vật sách tranh đạo cụ", "Nhấn nút 5 để tiếp tục");
                  this.recordMessageMode = 1;
                  var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O confirm c=0 game.j.p().l(5)=false -> msgwarm.ui f=1 stay record.ui selected=0");
                  return;
               }

               this.petmapReturnToBag = false;
               this.mode = VqsvPanelRuntime.Mode.PETMAP;
               this.selected = 0;
               this.petmapTab = 0;
               this.listScroll = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O confirm c=0 -> o.a(11) game.h.P petmap.ui open");
            } else {
               this.badgeReturnToBag = false;
               this.mode = VqsvPanelRuntime.Mode.BADGE;
               this.selected = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.O confirm c=1 -> o.a(12) game.h.R badge.ui open");
            }
         }

      }
   }

   private void tickBadge(VqsvGameRuntime.Scene var1) {
      int var2 = badgeDisplayCount(var1);
      this.selected = clamp(this.selected, 0, var2 - 1);
      if (!this.badgeAwardReturnToWorld || !var1.key0 && !var1.keyBack) {
         if (var1.keyBack) {
            if (this.badgeReturnToBag) {
               this.mode = VqsvPanelRuntime.Mode.BAG;
               this.bagTab = 3;
               this.selected = this.badgeReturnBagSelected;
               this.listScroll = this.badgeReturnBagScroll;
               this.openedTicks = 0;
               this.badgeReturnToBag = false;
               var1.session.story.trace().add("PORTED panel game.h.X back o.b=8 close badge.ui -> P=8 bag.ui tab=3 selected=" + this.selected);
            } else {
               this.mode = VqsvPanelRuntime.Mode.RECORD;
               this.selected = 0;
               this.recordSelected = 1;
               this.recordMessageMode = 0;
               this.openedTicks = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.X back close badge.ui -> P=9 record.ui selected=1");
            }
         } else {
            int var3 = this.selected;
            int var4 = (var2 - 1) / 5 * 5;
            if (var1.keyUp && this.selected >= 5) {
               this.selected -= 5;
            } else if (var1.keyDown && this.selected < var4) {
               this.selected = Math.min(var2 - 1, this.selected + 5);
            } else if (var1.keyLeft && this.selected % 5 != 0) {
               --this.selected;
            } else if (var1.keyRight && this.selected % 5 != 4 && this.selected + 1 < var2) {
               ++this.selected;
            }

            if (this.selected != var3) {
               this.openedTicks = 0;
               int var5 = badgeRuntimeIdAtDisplayIndex(var1, this.selected);
               List var10000 = var1.session.story.trace();
               int var10001 = this.selected;
               var10000.add("PORTED/PARTIAL panel game.h.X navigation displayIndex=" + var10001 + " badgeId=" + var5 + " status=" + badgeStatusText(var1, var5));
            }

         }
      } else {
         this.visible = false;
         this.badgeAwardReturnToWorld = false;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.e opcode53 badge.ui close -> resume blocking world event badgeIndex=" + this.selected);
      }
   }

   private void tickPetmap(VqsvGameRuntime.Scene var1) {
      List var2 = petmapRowsForRender(var1, this.petmapTab);
      int var3 = Math.max(0, var2.size() - 1);
      this.selected = clamp(this.selected, 0, var3);
      if (var1.keyUp) {
         int var4 = this.selected;
         this.selected = clamp(this.selected - 1, 0, var3);
         this.keepSelectedVisible(var2.size());
         if (this.selected != var4) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.Q key=4100 petmapTab=" + this.petmapTab + " selected=" + this.selected);
         }
      } else if (var1.keyDown) {
         int var5 = this.selected;
         this.selected = clamp(this.selected + 1, 0, var3);
         this.keepSelectedVisible(var2.size());
         if (this.selected != var5) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.Q key=8448 petmapTab=" + this.petmapTab + " selected=" + this.selected);
         }
      } else if (var1.keyLeft) {
         int var6 = this.petmapTab;
         this.petmapTab = clamp(this.petmapTab - 1, 0, PETMAP_TAB_NAMES.length - 1);
         if (this.petmapTab != var6) {
            this.selected = 0;
            this.listScroll = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.Q key=16400 petmapTab=" + this.petmapTab + " aY/aZ");
         }
      } else if (var1.keyRight) {
         int var7 = this.petmapTab;
         this.petmapTab = clamp(this.petmapTab + 1, 0, PETMAP_TAB_NAMES.length - 1);
         if (this.petmapTab != var7) {
            this.selected = 0;
            this.listScroll = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.Q key=32832 petmapTab=" + this.petmapTab + " aY/aZ");
         }
      } else if (var1.keyBack) {
         if (this.petmapReturnToBag) {
            this.mode = VqsvPanelRuntime.Mode.BAG;
            this.bagTab = 3;
            this.selected = clamp(this.petmapReturnBagSelected, 0, Math.max(0, bagRows(var1, this.bagTab).size() - 1));
            this.listScroll = this.petmapReturnBagScroll;
            this.keepSelectedVisible(bagRows(var1, this.bagTab).size());
            this.petmapReturnToBag = false;
            var1.session.story.trace().add("PORTED panel game.k.T petmap back source caller state=8 -> bag.ui tab=3 selected=" + this.selected);
         } else {
            this.mode = VqsvPanelRuntime.Mode.RECORD;
            this.selected = 0;
            this.recordSelected = 0;
            this.recordMessageMode = 0;
            var1.session.story.trace().add("PORTED panel game.k.T petmap back source caller state!=8 -> record.ui selected=0");
         }

         this.openedTicks = 0;
      } else if (var1.key0) {
         var1.session.story.trace().add("VERIFIED source-inert panel game.h.Q confirm petmap entry source game.k.T handles navigation/back only");
      }

   }

   private void tickSave(VqsvGameRuntime.Scene var1) {
      if (this.sourceRecoverySavePhase != 0) {
         this.tickSourceWorldShopRecoverySave(var1);
      } else if (this.savePhase == 0) {
         if (var1.key0) {
            this.savePhase = 1;
            this.saveMessage = "Đang lưu...";
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.K f=0 confirm text=Dang luu hide widgets 3/4");
         } else if (var1.keyBack) {
            this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
            this.selected = 5;
            this.resetGameMenuViewport();
            this.openedTicks = 0;
            this.saveMessage = "";
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.K f=0 back close msgtip.ui -> P=6 gamemenu selected=5");
         }

      } else if (this.savePhase == 1) {
         VqsvSaveRuntime.SaveResult var2 = VqsvSaveRuntime.save(var1, VqsvSaveRuntime.SaveRequest.manual("game-menu-softkey"));
         if (var2.success) {
            this.savePhase = 2;
            this.saveMessage = var2.cloudSynced ? "Lưu thành công" : "Đã lưu máy; lỗi cloud";
            List var10000 = var1.session.story.trace();
            boolean var10001 = var2.cloudSynced;
            var10000.add("PORTED panel game.h.K f=1 canonical save/write/read-back success text=Luu thanh cong cloudSynced=" + var10001 + (var2.cloudSynced ? "" : " error=" + var2.detail));
         } else {
            this.savePhase = 2;
            this.saveMessage = "Lưu thất bại";
            var1.session.story.trace().add("PENDING panel game.h.K f=1 canonical save failed text=Luu that bai error=" + var2.detail);
         }

      } else {
         if (this.savePhase == 2) {
            if (!var1.key0 && !var1.keyBack) {
               return;
            }

            this.visible = false;
            this.saveMessage = "";
            this.savePhase = 0;
            var1.session.story.trace().add("PORTED panel game.h.K f=2 acknowledged result; close msgtip.ui and gamemenu.ui -> P=0");
         }

      }
   }

   private void tickHelp(VqsvGameRuntime.Scene var1) {
      if (var1.keyLeft) {
         int var2 = this.helpPage;
         this.helpPage = clamp(this.helpPage - 1, 0, 2);
         if (this.helpPage != var2) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.v key=16400 help1.ui r=" + this.helpPage + " game.h.d(r)");
         }
      } else if (var1.keyRight) {
         int var3 = this.helpPage;
         this.helpPage = clamp(this.helpPage + 1, 0, 2);
         if (this.helpPage != var3) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.v key=32832 help1.ui r=" + this.helpPage + " game.h.d(r)");
         }
      } else if (var1.keyBack) {
         if (this.titlePanelMode) {
            this.visible = false;
            this.titlePanelMode = false;
            this.helpPage = 0;
            this.openedTicks = 0;
            var1.session.story.trace().add("PC_QOL title menu close source-style help1.ui");
            return;
         }

         this.mode = VqsvPanelRuntime.Mode.GAMESYSTEM;
         this.selected = 1;
         this.helpPage = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.v back close help1.ui -> P=13 gamesystem.ui selected=1");
      } else if (var1.key0) {
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.v confirm ignored help1.ui source handles page/back only");
      }

   }

   private void tickSettings(VqsvGameRuntime.Scene var1) {
      if (var1.keyLeft) {
         int var2 = this.settingsLevel;
         this.settingsLevel = clamp(this.settingsLevel - 1, 0, 3);
         if (this.settingsLevel != var2) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.x key=16400 help.ui settings game.i.a().i g=" + this.settingsLevel);
         }
      } else if (var1.keyRight) {
         int var3 = this.settingsLevel;
         this.settingsLevel = clamp(this.settingsLevel + 1, 0, 3);
         if (this.settingsLevel != var3) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.x key=32832 help.ui settings game.i.a().h g=" + this.settingsLevel);
         }
      } else if (var1.keyBack) {
         if (this.titlePanelMode) {
            this.visible = false;
            this.titlePanelMode = false;
            this.openedTicks = 0;
            var1.session.story.trace().add("PC_QOL title menu close source-style help.ui settings g=" + this.settingsLevel + " speedX2=" + var1.session.runtime.speedX2);
            return;
         }

         this.mode = VqsvPanelRuntime.Mode.GAMESYSTEM;
         this.selected = 2;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.x back close help.ui -> P=13 gamesystem.ui selected=2 g=" + this.settingsLevel);
      } else if (var1.key0) {
         if (this.titlePanelMode) {
            this.visible = false;
            this.titlePanelMode = false;
            this.openedTicks = 0;
            var1.session.story.trace().add("PC_QOL title menu confirm source-style help.ui settings g=" + this.settingsLevel + " speedX2=" + var1.session.runtime.speedX2);
            return;
         }

         this.mode = VqsvPanelRuntime.Mode.GAMESYSTEM;
         this.selected = 2;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.x confirm key=131072 save game.g.B().k=" + this.settingsLevel + " close help.ui -> P=13 gamesystem.ui selected=2");
      }

   }

   private void tickOptionConfirm(VqsvGameRuntime.Scene var1) {
      if (var1.keyUp) {
         int var2 = this.selected;
         this.selected = 0;
         if (this.selected != var2) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n option.ui key=4100 c=0");
         }
      } else if (var1.keyDown) {
         int var3 = this.selected;
         this.selected = 1;
         if (this.selected != var3) {
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n option.ui key=8448 c=1");
         }
      } else if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.GAMESYSTEM;
         this.selected = 3;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n option.ui back close option.ui f=0 -> gamesystem.ui selected=3");
      } else if (var1.key0) {
         if (this.selected == 1) {
            this.mode = VqsvPanelRuntime.Mode.GAMESYSTEM;
            this.selected = 3;
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n option.ui confirm c=1 close option.ui f=0 no-reset -> gamesystem.ui selected=3");
         } else {
            this.visible = false;
            this.openedTicks = 0;
            var1.requestPanelTitleResetFromSourceOption();
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.n option.ui confirm c=0 close option.ui -> title menu without boot/intro reset");
         }
      }

   }

   private void tickRide(VqsvGameRuntime.Scene var1) {
      if (this.rideMessageMode != 0) {
         if (var1.text != null && var1.text.readyForKey && var1.key0) {
            var1.text.confirm();
            if (var1.text.disposed) {
               int var7 = this.rideMessageMode;
               var1.text = null;
               this.rideMessageMode = 0;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae ride msgwarm key=196640 close msgwarm.ui mode=" + var7 + " return ride.ui selected=" + this.rideSelected);
            }
         }

      } else {
         if (var1.keyLeft) {
            int var2 = this.rideSelected;
            this.rideSelected = clamp(this.rideSelected - 1, 0, 3);
            if (this.rideSelected != var2) {
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae key=16400 ride.ui selected=" + this.rideSelected + " label=" + RIDE_LABELS[this.rideSelected]);
            }
         } else if (var1.keyRight) {
            int var5 = this.rideSelected;
            this.rideSelected = clamp(this.rideSelected + 1, 0, 3);
            if (this.rideSelected != var5) {
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae key=32832 ride.ui selected=" + this.rideSelected + " label=" + RIDE_LABELS[this.rideSelected]);
            }
         } else if (var1.keyBack) {
            this.visible = false;
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae key=262144 close ride.ui -> P=0 selected=" + this.rideSelected);
         } else if (var1.key0) {
            boolean var6 = sourceRideUnlocked(var1, this.rideSelected);
            PanelDecisionService.RideResult var3 = PANEL_DECISIONS.selectRide(var1.session.progression.ride, this.rideSelected, var6);
            PanelDecisionService.RideRoute var4 = PANEL_DECISIONS.routeRide(var3);
            if (var4.outcome == PanelDecisionService.RideRouteOutcome.LOCKED_WARNING) {
               var1.text = TextBox.msgWarm("Chưa có sủng vật cưỡi này", "Nhấn nút 5 để tiếp tục");
               this.rideMessageMode = var4.messageMode;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae confirm ride q.f(" + this.rideSelected + ")=false -> msgwarm.ui text=Chua co sung vat cuoi nay");
               return;
            }

            if (var4.outcome == PanelDecisionService.RideRouteOutcome.BLOCKED_WARNING) {
               var1.text = TextBox.msgWarm("Nơi này không thể sử dụng sủng vật cưỡi", "Nhấn nút 5 để tiếp tục");
               this.rideMessageMode = var4.messageMode;
               var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae confirm ride q.f(" + this.rideSelected + ")=true q.g=false -> msgwarm.ui text=Noi nay khong the su dung sung vat cuoi");
               return;
            }

            if (var4.resetPlayerMode) {
               VqsvRideRuntime.applySelectedRide(var1, "ride.ui-confirm");
            }

            if (var4.closePanel) {
               this.visible = false;
               this.openedTicks = 0;
            }

            var1.session.story.trace().add("PORTED/PARTIAL panel game.h.ae confirm ride q.f(" + this.rideSelected + ")=true q.g=true q.h(" + this.rideSelected + ") activeRide=" + var3.activeBefore + "->" + var3.activeAfter + " d[0]=" + var3.speedBefore + "->" + var3.speedAfter + " close ride.ui -> P=0 PORTED visual super.a(" + (this.rideSelected + 1) + ",false) visualSprite=" + var1.player.visualSpriteIndex);
         }

      }
   }

   private void openFashionShop(VqsvGameRuntime.Scene var1) {
      this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
      this.selected = 0;
      this.listScroll = 0;
      this.openedTicks = 0;
      this.fashionPurchaseQuote = null;
      this.fashionOpenPlan = null;
      this.fashionExchangePlan = null;
      this.fashionRevealKey = null;
      this.fashionRevealReturnExchange = false;
      this.wardrobePreviewKey = null;
      this.wardrobePreviewAnimator = null;
      List var10000 = var1.session.story.trace();
      int var10001 = var1.session.inventory.currency.money;
      var10000.add("FASH-I3-I3 fashion shop open money=" + var10001 + " bags=" + var1.session.fashion.economy().blindBagCount() + " fragments=" + var1.session.fashion.economy().fragmentCount());
   }

   private void tickFashionShop(VqsvGameRuntime.Scene var1) {
      this.ensureEquippedFashionPreview(var1);
      if (this.wardrobePreviewAnimator != null) {
         this.wardrobePreviewAnimator.tick();
      }

      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, FASHION_SHOP_LABELS.length - 1);
         this.openedTicks = 0;
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, FASHION_SHOP_LABELS.length - 1);
         this.openedTicks = 0;
      } else if (!var1.keyBack && (!var1.key0 || this.selected != 3)) {
         if (var1.key0 && this.selected == 0) {
            int var2 = 99 - var1.session.fashion.economy().blindBagCount();
            if (var2 <= 0) {
               showFashionMessage(var1, "Túi mù đã đầy");
               return;
            }

            this.fashionPurchaseQuantity = 1;
            this.syncFashionPurchaseQuote(var1);
            this.mode = VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM;
            this.openedTicks = 0;
         } else if (var1.key0 && this.selected == 1) {
            this.fashionOpenPlan = FASHION_ACQUISITION_SERVICE.planOpen(var1.session.fashion);
            if (this.fashionOpenPlan.outcome != FashionAcquisitionService.OpenOutcome.READY) {
               showFashionMessage(var1, fashionOpenFailureText(this.fashionOpenPlan.outcome));
               return;
            }

            this.mode = VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM;
            this.openedTicks = 0;
         } else if (var1.key0 && this.selected == 2) {
            this.mode = VqsvPanelRuntime.Mode.FASHION_EXCHANGE;
            this.selected = 0;
            this.listScroll = 0;
            this.openedTicks = 0;
            this.ensureFashionExchangePreview();
         }
      } else {
         this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP;
         this.selected = 3;
         this.listScroll = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("FASH-I3-I3 fashion shop close -> portable shop");
      }

   }

   private void tickFashionBuyConfirm(VqsvGameRuntime.Scene var1) {
      int var2 = Math.max(1, 99 - var1.session.fashion.economy().blindBagCount());
      if (var1.keyLeft) {
         --this.fashionPurchaseQuantity;
         if (this.fashionPurchaseQuantity < 1) {
            this.fashionPurchaseQuantity = var2;
         }

         this.syncFashionPurchaseQuote(var1);
         this.openedTicks = 0;
      } else if (var1.keyRight) {
         ++this.fashionPurchaseQuantity;
         if (this.fashionPurchaseQuantity > var2) {
            this.fashionPurchaseQuantity = 1;
         }

         this.syncFashionPurchaseQuote(var1);
         this.openedTicks = 0;
      } else if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
         this.selected = 0;
         this.openedTicks = 0;
      } else if (var1.key0) {
         this.fashionPurchaseQuote = FASHION_ACQUISITION_SERVICE.planPurchase(var1.session.inventory.currency, var1.session.fashion, this.fashionPurchaseQuantity);
         FashionAcquisitionService.PurchaseReceipt var3 = FASHION_ACQUISITION_SERVICE.commitPurchase(var1.session.inventory.currency, var1.session.fashion, this.fashionPurchaseQuote);
         if (var3.outcome == FashionAcquisitionService.PurchaseOutcome.PURCHASED) {
            this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
            this.selected = 0;
            this.openedTicks = 0;
            showFashionMessage(var1, "Đã mua " + var3.quantity + " túi mù - " + var3.totalPrice + " K");
            var1.session.story.trace().add("FASH-I3-I3 purchase committed qty=" + var3.quantity + " money=" + var3.moneyBefore + "->" + var3.moneyAfter + " bags=" + var3.bagBefore + "->" + var3.bagAfter);
         } else {
            showFashionMessage(var1, fashionPurchaseFailureText(var3.outcome));
         }
      }

   }

   private void tickFashionOpenConfirm(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
         this.selected = 1;
         this.openedTicks = 0;
      } else if (var1.key0) {
         FashionAcquisitionService.OpenReceipt var2 = FASHION_ACQUISITION_SERVICE.commitOpen(var1.session.fashion, this.fashionOpenPlan);
         if (var2.outcome != FashionAcquisitionService.OpenOutcome.OPENED_NEW && var2.outcome != FashionAcquisitionService.OpenOutcome.OPENED_DUPLICATE) {
            this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
            this.selected = 1;
            this.openedTicks = 0;
            showFashionMessage(var1, fashionOpenFailureText(var2.outcome));
         } else {
            this.fashionRevealKey = var2.stableKey;
            this.fashionRevealTier = var2.tierCode;
            this.fashionRevealTitle = var2.duplicate ? "Trang phục trùng" : "Trang phục mới!";
            this.fashionRevealDetail = var2.duplicate ? "Đổi thành " + (var2.fragmentAfter - var2.fragmentBefore) + " mảnh thời trang" : "Đã thêm vào tủ thời trang";
            this.fashionRevealReturnExchange = false;
            this.mode = VqsvPanelRuntime.Mode.FASHION_REVEAL;
            this.wardrobePreviewKey = null;
            this.wardrobePreviewAnimator = null;
            this.openedTicks = 0;
            var1.session.story.trace().add("FASH-I3-I3 open reveal key=" + var2.stableKey + " tier=" + var2.tierCode + " duplicate=" + var2.duplicate + " bags=" + var2.bagBefore + "->" + var2.bagAfter + " draw=" + var2.drawIndexBefore + "->" + var2.drawIndexAfter + " fragments=" + var2.fragmentBefore + "->" + var2.fragmentAfter);
         }
      }
   }

   private void tickFashionReveal(VqsvGameRuntime.Scene var1) {
      SourceFashionRecord var2 = SourceFashionCatalog.instance().byStableKey(this.fashionRevealKey);
      if (var2 != null) {
         this.ensureWardrobePreview(var2);
         this.wardrobePreviewAnimator.tick();
      }

      if (var1.key0 || var1.keyBack) {
         if (this.fashionRevealAnimationActive()) {
            this.openedTicks = 24;
            return;
         }

         if (this.fashionRevealReturnExchange) {
            this.mode = VqsvPanelRuntime.Mode.FASHION_EXCHANGE;
            this.keepSelectedVisible(FashionEconomyCatalog.instance().entries().size());
            this.ensureFashionExchangePreview();
         } else {
            this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
            this.selected = 1;
            this.listScroll = 0;
         }

         this.openedTicks = 0;
      }

   }

   private boolean fashionRevealAnimationActive() {
      return !this.fashionRevealReturnExchange && this.openedTicks < 24;
   }

   private static int fashionRevealFrameIndex(int var0) {
      int var1 = Math.max(0, var0);

      for(int var2 = 0; var2 < FASHION_REVEAL_FRAME_TICKS.length; ++var2) {
         if (var1 < FASHION_REVEAL_FRAME_TICKS[var2]) {
            return var2;
         }

         var1 -= FASHION_REVEAL_FRAME_TICKS[var2];
      }

      return FASHION_REVEAL_FRAME_TICKS.length - 1;
   }

   private void tickFashionExchange(VqsvGameRuntime.Scene var1) {
      List var2 = FashionEconomyCatalog.instance().entries();
      this.selected = clamp(this.selected, 0, var2.size() - 1);
      this.keepSelectedVisible(var2.size());
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, var2.size() - 1);
         this.keepSelectedVisible(var2.size());
         this.openedTicks = 0;
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, var2.size() - 1);
         this.keepSelectedVisible(var2.size());
         this.openedTicks = 0;
      } else {
         if (var1.keyBack) {
            this.mode = VqsvPanelRuntime.Mode.FASHION_SHOP;
            this.selected = 2;
            this.listScroll = 0;
            this.openedTicks = 0;
            return;
         }

         if (var1.key0) {
            FashionEconomyEntry var3 = (FashionEconomyEntry)var2.get(this.selected);
            this.fashionExchangePlan = FASHION_ACQUISITION_SERVICE.planExchange(var1.session.fashion, var3.stableKey);
            if (this.fashionExchangePlan.outcome != FashionAcquisitionService.ExchangeOutcome.READY) {
               showFashionMessage(var1, fashionExchangeFailureText(this.fashionExchangePlan.outcome));
               return;
            }

            this.mode = VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM;
            this.openedTicks = 0;
            return;
         }
      }

      this.ensureFashionExchangePreview();
      this.wardrobePreviewAnimator.tick();
   }

   private void tickFashionExchangeConfirm(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.FASHION_EXCHANGE;
         this.openedTicks = 0;
      } else if (var1.key0) {
         FashionAcquisitionService.ExchangeReceipt var2 = FASHION_ACQUISITION_SERVICE.commitExchange(var1.session.fashion, this.fashionExchangePlan);
         if (var2.outcome != FashionAcquisitionService.ExchangeOutcome.EXCHANGED) {
            this.mode = VqsvPanelRuntime.Mode.FASHION_EXCHANGE;
            this.openedTicks = 0;
            showFashionMessage(var1, fashionExchangeFailureText(var2.outcome));
         } else {
            this.fashionRevealKey = var2.stableKey;
            this.fashionRevealTier = var2.tierCode;
            this.fashionRevealTitle = "Đổi trang phục thành công";
            this.fashionRevealDetail = "Đã dùng " + var2.fragmentCost + " mảnh thời trang";
            this.fashionRevealReturnExchange = true;
            this.mode = VqsvPanelRuntime.Mode.FASHION_REVEAL;
            this.wardrobePreviewKey = null;
            this.wardrobePreviewAnimator = null;
            this.openedTicks = 0;
            var1.session.story.trace().add("FASH-I3-I3 exchange reveal key=" + var2.stableKey + " tier=" + var2.tierCode + " cost=" + var2.fragmentCost + " fragments=" + var2.fragmentBefore + "->" + var2.fragmentAfter);
         }
      }
   }

   private void syncFashionPurchaseQuote(VqsvGameRuntime.Scene var1) {
      this.fashionPurchaseQuote = FASHION_ACQUISITION_SERVICE.planPurchase(var1.session.inventory.currency, var1.session.fashion, this.fashionPurchaseQuantity);
   }

   private void ensureFashionExchangePreview() {
      List var1 = FashionEconomyCatalog.instance().entries();
      if (!var1.isEmpty()) {
         FashionEconomyEntry var2 = (FashionEconomyEntry)var1.get(clamp(this.selected, 0, var1.size() - 1));
         this.ensureWardrobePreview(var2.sourceFashion);
      }
   }

   private void ensureEquippedFashionPreview(VqsvGameRuntime.Scene var1) {
      SourceFashionRecord var2 = SourceFashionCatalog.instance().byStableKey(var1.session.fashion.selectedStableKey());
      if (var2 == null) {
         var2 = SourceFashionCatalog.instance().byStableKey("FASH-BASE-NEIL");
      }

      if (var2 != null) {
         this.ensureWardrobePreview(var2);
      }

   }

   private static void showFashionMessage(VqsvGameRuntime.Scene var0, String var1) {
      var0.text = TextBox.msgWarm(var1, "Nhấn nút 5 để tiếp tục");
   }

   private static String fashionPurchaseFailureText(FashionAcquisitionService.PurchaseOutcome var0) {
      if (var0 == FashionAcquisitionService.PurchaseOutcome.INSUFFICIENT_GOLD) {
         return "Không đủ kim tiền";
      } else if (var0 == FashionAcquisitionService.PurchaseOutcome.BAG_CAPACITY_EXCEEDED) {
         return "Túi mù đã đầy";
      } else {
         return var0 == FashionAcquisitionService.PurchaseOutcome.STALE_PLAN ? "Dữ liệu đã thay đổi, hãy xác nhận lại" : "Không thể mua túi mù";
      }
   }

   private static String fashionOpenFailureText(FashionAcquisitionService.OpenOutcome var0) {
      if (var0 == FashionAcquisitionService.OpenOutcome.NO_BAG) {
         return "Bạn chưa có túi mù";
      } else if (var0 == FashionAcquisitionService.OpenOutcome.FRAGMENT_CAPACITY_EXCEEDED) {
         return "Mảnh thời trang đã đầy";
      } else {
         return var0 == FashionAcquisitionService.OpenOutcome.STALE_PLAN ? "Dữ liệu đã thay đổi, hãy xác nhận lại" : "Không thể mở túi mù";
      }
   }

   private static String fashionExchangeFailureText(FashionAcquisitionService.ExchangeOutcome var0) {
      if (var0 == FashionAcquisitionService.ExchangeOutcome.ALREADY_OWNED) {
         return "Bạn đã sở hữu trang phục này";
      } else if (var0 == FashionAcquisitionService.ExchangeOutcome.INSUFFICIENT_FRAGMENTS) {
         return "Không đủ mảnh thời trang";
      } else {
         return var0 == FashionAcquisitionService.ExchangeOutcome.STALE_PLAN ? "Dữ liệu đã thay đổi, hãy xác nhận lại" : "Không thể đổi trang phục";
      }
   }

   private void renderFashionShop(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      renderFashionBackdrop(var1, var2, var3, "Thương điếm thời trang");
      this.renderEquippedFashionPreview(var1, var2, var3);

      for(int var4 = 0; var4 < FASHION_SHOP_LABELS.length; ++var4) {
         this.renderFashionMenuCard(var1, var2, var3, var4);
      }

      drawFashionOddsCompact(var1, var2, 229);
      drawFashionSoftkey(var1, var2, this.selected == 3 ? "Rời đi" : "Chọn", 0, 286, 112, true);
      drawFashionSoftkey(var1, var2, "Quay lại", 128, 286, 112, true);
   }

   private void renderEquippedFashionPreview(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      this.ensureEquippedFashionPreview(var3);
      SourceFashionRecord var4 = SourceFashionCatalog.instance().byStableKey(var3.session.fashion.selectedStableKey());
      if (var4 == null) {
         var4 = SourceFashionCatalog.instance().byStableKey("FASH-BASE-NEIL");
      }

      var1.setColor(new Color(863286));
      var1.fillRoundRect(8, 66, 90, 174, 8, 8);
      var1.setColor(new Color(5212563));
      var1.drawRoundRect(8, 66, 89, 173, 8, 8);
      drawBagCenteredText(var1, var2, "ĐANG MẶC", 16, 75, 74, new Color(10410454));
      var1.setColor(new Color(1391943));
      var1.fillOval(22, 99, 62, 62);
      var1.setColor(new Color(3235429));
      var1.fillOval(26, 190, 54, 7);
      if (this.wardrobePreviewAnimator != null) {
         Shape var5 = var1.getClip();
         var1.clipRect(11, 91, 84, 108);
         this.wardrobePreviewAnimator.drawAligned(var1, 12, 93, 82, 104, 7, 0);
         var1.setClip(var5);
      }

      if (var4 != null) {
         drawBagCenteredText(var1, var2, fashionDisplayName(var4), 13, 204, 80, new Color(16777215));
         String var6 = var4.sourceKind == SourceFashionRecord.SourceKind.CAU_VONG ? "Cầu Vồng" : "Liệt Hỏa";
         drawBagCenteredText(var1, var2, var6, 11, 221, 84, fashionAccentColor(var4));
      }

   }

   private void renderFashionMenuCard(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3, int var4) {
      int[] var5 = FASHION_MENU_CARDS[var4];
      int var6 = var5[0];
      int var7 = var5[1];
      int var8 = var5[2];
      int var9 = var5[3];
      boolean var10 = this.selected == var4;
      Color var11 = var4 == 3 ? new Color(var10 ? 2116437 : 1060665) : new Color(var10 ? 1790816 : 1060665);
      Color var12 = var10 ? new Color(15911010) : new Color(3235177);
      var1.setColor(var11);
      var1.fillRoundRect(var6, var7, var8, var9, 7, 7);
      var1.setColor(var12);
      var1.drawRoundRect(var6, var7, var8 - 1, var9 - 1, 7, 7);
      if (var4 < 3) {
         drawFashionActionIcon(var1, var4, var6 + 7, var7 + 8);
      }

      Color var13 = new Color(15399157);
      Color var14 = var10 ? new Color(16768131) : new Color(10208709);
      if (var4 == 0) {
         drawBagText(var1, var2, "Mua túi mù", var6 + 43, var7 + 8, var8 - 47, var13);
         drawBagText(var1, var2, "500 K / túi", var6 + 43, var7 + 25, var8 - 47, var14);
      } else if (var4 == 1) {
         drawBagText(var1, var2, "Mở túi", var6 + 43, var7 + 8, var8 - 47, var13);
         drawBagText(var1, var2, "Hiện có " + var3.session.fashion.economy().blindBagCount(), var6 + 43, var7 + 25, var8 - 47, var14);
      } else if (var4 == 2) {
         drawBagText(var1, var2, "Đổi mảnh", var6 + 43, var7 + 8, var8 - 47, var13);
         drawBagText(var1, var2, var3.session.fashion.economy().fragmentCount() + " mảnh • 35 mẫu", var6 + 43, var7 + 25, var8 - 47, var14);
      } else {
         drawBagCenteredText(var1, var2, "Rời cửa hàng", var6 + 8, var7 + 7, var8 - 16, var13);
      }

   }

   private static void drawFashionActionIcon(Graphics2D var0, int var1, int var2, int var3) {
      String var4 = var1 < 2 ? "FASH-UI-ICON-BLIND-BAG" : "FASH-UI-ICON-FASHION";
      drawFashionUiImage(var0, var4, var2, var3, 32, 32);
   }

   private void renderFashionBuyConfirm(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      this.renderFashionShop(var1, var2, var3);
      renderFashionConfirmCard(var1, var2, "Mua túi mù");
      drawFashionUiImage(var1, "FASH-UI-ICON-BLIND-BAG", 104, 98, 32, 32);
      int var4 = this.fashionPurchaseQuantity * 500;
      drawBagCenteredText(var1, var2, this.fashionPurchaseQuantity + " túi", 42, 132, 72, new Color(15201778));
      drawBagCenteredText(var1, var2, "= " + var4 + " K", 116, 132, 82, new Color(16768131));
      var1.setColor(new Color(3113616));
      var1.fillRoundRect(48, 151, 48, 32, 8, 8);
      var1.fillRoundRect(144, 151, 48, 32, 8, 8);
      drawBagCenteredText(var1, var2, "-", 50, 159, 44, new Color(16777215));
      drawBagCenteredText(var1, var2, "+", 146, 159, 44, new Color(16777215));
      drawBagCenteredText(var1, var2, String.valueOf(this.fashionPurchaseQuantity), 99, 159, 42, new Color(16777215));
      drawFashionConfirmAction(var1, var2, "Mua ngay", true);
      drawFashionSoftkey(var1, var2, "Mua", 0, 286, 112, true);
      drawFashionSoftkey(var1, var2, "Hủy", 128, 286, 112, true);
   }

   private void renderFashionOpenConfirm(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      this.renderFashionShop(var1, var2, var3);
      renderFashionConfirmCard(var1, var2, "Mở túi mù?");
      drawFashionUiImage(var1, "FASH-UI-ICON-BLIND-BAG", 104, 99, 32, 32);
      drawBagCenteredText(var1, var2, "Sẽ tiêu hao 1 túi mù", 42, 139, 156, new Color(15201778));
      drawBagCenteredText(var1, var2, "Hiện có: " + var3.session.fashion.economy().blindBagCount(), 42, 158, 156, new Color(8315553));
      drawFashionConfirmAction(var1, var2, "Mở 1 túi", true);
      drawFashionSoftkey(var1, var2, "Mở túi", 0, 286, 112, true);
      drawFashionSoftkey(var1, var2, "Hủy", 128, 286, 112, true);
   }

   private void renderFashionReveal(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      SourceFashionRecord var4 = SourceFashionCatalog.instance().byStableKey(this.fashionRevealKey);
      if (this.fashionRevealAnimationActive()) {
         renderFashionBackdrop(var1, var2, var3, "Mở túi mù");
         BufferedImage var7 = FashionUiAssetCatalog.instance().frame("FASH-UI-ANIM-BLIND-BAG-OPEN", fashionRevealFrameIndex(this.openedTicks));
         if (var7 != null) {
            var1.drawImage(var7, 56, 72, (ImageObserver)null);
         }

         drawBagCenteredText(var1, var2, "Đang mở...", 32, 218, 176, new Color(16768131));
         drawFashionSoftkey(var1, var2, "Bỏ qua", 0, 286, 240, true);
      } else {
         renderFashionBackdrop(var1, var2, var3, this.fashionRevealTitle);
         if (var4 != null) {
            this.ensureWardrobePreview(var4);
            Color var5 = fashionTierColor(this.fashionRevealTier);
            var1.setColor(new Color(863286));
            var1.fillRoundRect(42, 66, 156, 156, 12, 12);
            var1.setColor(var5);
            var1.drawRoundRect(42, 66, 155, 155, 12, 12);
            var1.setColor(new Color(1391943));
            var1.fillOval(78, 84, 84, 84);
            var1.setColor(new Color(3235429));
            var1.fillOval(84, 190, 72, 12);
            Shape var6 = var1.getClip();
            var1.clipRect(46, 70, 148, 138);
            this.wardrobePreviewAnimator.drawAligned(var1, 50, 72, 140, 132, 7, 0);
            var1.setClip(var6);
            drawBagCenteredText(var1, var2, fashionDisplayName(var4), 48, 226, 144, new Color(16777215));
            drawBagCenteredText(var1, var2, fashionTierLabel(this.fashionRevealTier), 48, 247, 144, var5);
         }

         drawBagCenteredText(var1, var2, this.fashionRevealDetail, 16, 268, 208, new Color(12445911));
         drawFashionSoftkey(var1, var2, "Tiếp tục", 0, 286, 240, true);
      }
   }

   private void renderFashionExchange(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      List var4 = FashionEconomyCatalog.instance().entries();
      this.selected = clamp(this.selected, 0, var4.size() - 1);
      this.keepSelectedVisible(var4.size());
      FashionEconomyEntry var5 = (FashionEconomyEntry)var4.get(this.selected);
      this.ensureWardrobePreview(var5.sourceFashion);
      renderFashionBackdrop(var1, var2, var3, "Đổi mảnh thời trang");
      int var6 = this.visibleListStart(var4.size());

      for(int var7 = 0; var7 < 5; ++var7) {
         int var8 = var6 + var7;
         if (var8 >= var4.size()) {
            break;
         }

         FashionEconomyEntry var9 = (FashionEconomyEntry)var4.get(var8);
         int var10 = 64 + var7 * 36;
         boolean var11 = var8 == this.selected;
         boolean var12 = var3.session.fashion.isOwned(var9.stableKey);
         var1.setColor(new Color(var11 ? 1790816 : 1060665));
         var1.fillRoundRect(8, var10, 112, 33, 5, 5);
         var1.setColor(var11 ? new Color(15911010) : new Color(3235177));
         var1.drawRoundRect(8, var10, 111, 32, 5, 5);
         var1.setColor(fashionTierColor(var9.tier.code));
         var1.fillRect(11, var10 + 4, 3, 25);
         drawBagText(var1, var2, fashionDisplayName(var9.sourceFashion), 12, var10 + 5, 104, new Color(15399157));
         drawBagText(var1, var2, var12 ? "Đã có" : var9.tier.exchangeFragments + " mảnh", 12, var10 + 18, 104, new Color(var12 ? 8315553 : 16768131));
      }

      this.drawFashionExchangeScrollbar(var1, var4.size());
      var1.setColor(new Color(863286));
      var1.fillRoundRect(128, 64, 104, 154, 8, 8);
      var1.setColor(fashionTierColor(var5.tier.code));
      var1.drawRoundRect(128, 64, 103, 153, 8, 8);
      drawBagCenteredText(var1, var2, fashionDisplayName(var5.sourceFashion), 134, 73, 92, new Color(16777215));
      var1.setColor(new Color(1391943));
      var1.fillOval(146, 89, 68, 68);
      Shape var13 = var1.getClip();
      var1.clipRect(132, 82, 96, 128);
      this.wardrobePreviewAnimator.drawAligned(var1, 134, 84, 92, 122, 7, 0);
      var1.setClip(var13);
      drawBagCenteredText(var1, var2, fashionTierLabel(var5.tier.code), 134, 197, 92, fashionTierColor(var5.tier.code));
      boolean var14 = !var3.session.fashion.isOwned(var5.stableKey);
      var1.setColor(new Color(var14 ? 1790816 : 2506308));
      var1.fillRoundRect(128, 226, 104, 30, 6, 6);
      var1.setColor(new Color(var14 ? 15911010 : 7439497));
      var1.drawRoundRect(128, 226, 103, 29, 6, 6);
      drawBagCenteredText(var1, var2, var14 ? "Đổi " + var5.tier.exchangeFragments + " mảnh" : "Đã sở hữu", 132, 234, 96, new Color(var14 ? 16777215 : 10135208));
      int var10002 = var3.session.fashion.economy().fragmentCount();
      drawBagCenteredText(var1, var2, "Mảnh: " + var10002, 128, 262, 104, new Color(12445911));
      drawFashionSoftkey(var1, var2, "Chọn đổi", 0, 286, 112, !var3.session.fashion.isOwned(var5.stableKey));
      drawFashionSoftkey(var1, var2, "Quay lại", 128, 286, 112, true);
   }

   private void renderFashionExchangeConfirm(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      this.renderFashionExchange(var1, var2, var3);
      renderFashionConfirmCard(var1, var2, "Xác nhận đổi");
      SourceFashionRecord var4 = this.fashionExchangePlan == null ? null : SourceFashionCatalog.instance().byStableKey(this.fashionExchangePlan.stableKey);
      drawBagCenteredText(var1, var2, var4 == null ? "Trang phục" : fashionDisplayName(var4), 42, 108, 156, new Color(15201778));
      int var10002 = this.fashionExchangePlan == null ? 0 : this.fashionExchangePlan.fragmentCost;
      drawBagCenteredText(var1, var2, "Chi phí: " + var10002 + " mảnh", 42, 136, 156, new Color(9067008));
      drawBagCenteredText(var1, var2, "Mảnh còn lại: " + (this.fashionExchangePlan == null ? 0 : this.fashionExchangePlan.fragmentAfter), 42, 164, 156, new Color(8315553));
      drawFashionConfirmAction(var1, var2, "Đổi trang phục", true);
      drawFashionSoftkey(var1, var2, "Đổi", 0, 286, 112, true);
      drawFashionSoftkey(var1, var2, "Hủy", 128, 286, 112, true);
   }

   private static void renderFashionBackdrop(Graphics2D var0, UiFont var1, VqsvGameRuntime.Scene var2, String var3) {
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(466730), 0.0F, 320.0F, new Color(1461335)));
      var0.fillRect(0, 0, 240, 320);
      var0.setColor(new Color(934222));
      var0.fillRect(0, 0, 240, 32);
      var0.setColor(new Color(6726312));
      var0.drawLine(0, 32, 240, 32);
      drawFashionUiImage(var0, "FASH-UI-ICON-FASHION", 7, 4, 24, 24);
      drawBagText(var0, var1, var3, 38, 10, 194, new Color(16777215));
      var0.setColor(new Color(665393));
      var0.fillRoundRect(8, 37, 224, 22, 6, 6);
      var0.setColor(new Color(3235177));
      var0.drawRoundRect(8, 37, 223, 21, 6, 6);
      drawFashionWalletChip(var0, var1, "K", var2.session.inventory.currency.money, (String)null, 9, 38, 69);
      drawFashionWalletChip(var0, var1, "", var2.session.fashion.economy().blindBagCount(), "FASH-UI-ICON-BLIND-BAG", 84, 38, 69);
      drawFashionWalletChip(var0, var1, "Mảnh", var2.session.fashion.economy().fragmentCount(), (String)null, 159, 38, 69);
      var0.setColor(new Color(3235177));
      var0.drawLine(81, 41, 81, 55);
      var0.drawLine(156, 41, 156, 55);
   }

   private static void drawFashionWalletChip(Graphics2D var0, UiFont var1, String var2, int var3, String var4, int var5, int var6, int var7) {
      if (var4 != null) {
         drawFashionUiImage(var0, var4, var5 + 5, var6 + 2, 18, 18);
         drawBagCenteredText(var0, var1, String.valueOf(var3), var5 + 25, var6 + 5, var7 - 27, new Color(15399157));
      } else {
         drawBagCenteredText(var0, var1, var2 + " " + var3, var5 + 3, var6 + 5, var7 - 6, new Color(15399157));
      }
   }

   private static void drawFashionUiImage(Graphics2D var0, String var1, int var2, int var3, int var4, int var5) {
      BufferedImage var6 = FashionUiAssetCatalog.instance().image(var1);
      if (var6 != null) {
         var0.drawImage(var6, var2, var3, var4, var5, (ImageObserver)null);
      }

   }

   private static void drawFashionSourceTexture(Graphics2D var0, SpriteAnimator var1, int var2, int var3, int var4, int var5, int var6) {
      int[] var7 = var1.cellBounds(var2);
      if (var7 != null && var7[2] > 0 && var7[3] > 0 && var5 > 0 && var6 > 0) {
         Shape var8 = var0.getClip();
         var0.clipRect(var3, var4, var5, var6);

         for(int var9 = var4; var9 < var4 + var6; var9 += var7[3]) {
            for(int var10 = var3; var10 < var3 + var5; var10 += var7[2]) {
               var1.drawCell(var0, var2, var10 - var7[0], var9 - var7[1], 0);
            }
         }

         var0.setClip(var8);
      }
   }

   private static void drawFashionSourceCellCentered(Graphics2D var0, SpriteAnimator var1, int var2, int var3, int var4, int var5, int var6) {
      int[] var7 = var1.cellBounds(var2);
      if (var7 != null) {
         int var8 = var3 + (var5 - var7[2]) / 2 - var7[0];
         int var9 = var4 + (var6 - var7[3]) / 2 - var7[1];
         var1.drawCell(var0, var2, var8, var9, 0);
      }
   }

   private static void renderFashionConfirmCard(Graphics2D var0, UiFont var1, String var2) {
      var0.setColor(new Color(0, 0, 0, 150));
      var0.fillRect(0, 58, 240, 222);
      var0.setColor(new Color(863286));
      var0.fillRoundRect(24, 70, 192, 168, 10, 10);
      var0.setColor(new Color(15911010));
      var0.drawRoundRect(24, 70, 191, 167, 10, 10);
      drawBagCenteredText(var0, var1, var2, 34, 80, 172, new Color(16777215));
      var0.setColor(new Color(3235177));
      var0.drawLine(38, 94, 202, 94);
   }

   private static void drawFashionConfirmAction(Graphics2D var0, UiFont var1, String var2, boolean var3) {
      var0.setColor(new Color(var3 ? 1790816 : 2506308));
      var0.fillRoundRect(42, 196, 156, 32, 7, 7);
      var0.setColor(new Color(var3 ? 8376527 : 7439497));
      var0.drawRoundRect(42, 196, 155, 31, 7, 7);
      drawBagCenteredText(var0, var1, var2, 46, 204, 148, new Color(var3 ? 16777215 : 10135208));
   }

   private static void drawFashionOddsCompact(Graphics2D var0, UiFont var1, int var2) {
      drawBagText(var0, var1, "TỶ LỆ", 106, var2 - 11, 126, new Color(7316904));
      drawBagCenteredText(var0, var1, "55% • 27% • 14% • 4%", 104, var2, 128, new Color(10208709));
   }

   private static void drawFashionOdds(Graphics2D var0, UiFont var1, int var2) {
      drawBagCenteredText(var0, var1, "Tỷ lệ: Phổ thông 55%  |  Hiếm 27%", 8, var2, 224, new Color(12445911));
      drawBagCenteredText(var0, var1, "Sử thi 14%  |  Huyền thoại 4%", 8, var2 + 15, 224, new Color(16767083));
   }

   private static Color fashionTierColor(String var0) {
      if ("LEGENDARY".equals(var0)) {
         return new Color(16752412);
      } else if ("EPIC".equals(var0)) {
         return new Color(12019924);
      } else {
         return "RARE".equals(var0) ? new Color(4166102) : new Color(5018976);
      }
   }

   private static String fashionTierLabel(String var0) {
      if ("LEGENDARY".equals(var0)) {
         return "Huyền thoại";
      } else if ("EPIC".equals(var0)) {
         return "Sử thi";
      } else {
         return "RARE".equals(var0) ? "Hiếm" : "Phổ thông";
      }
   }

   private static int fashionTierItemCell(String var0) {
      if ("LEGENDARY".equals(var0)) {
         return 18;
      } else if ("EPIC".equals(var0)) {
         return 16;
      } else {
         return "RARE".equals(var0) ? 21 : 20;
      }
   }

   private void drawFashionExchangeScrollbar(Graphics2D var1, int var2) {
      short var3 = 180;
      var1.setColor(new Color(3235177));
      var1.fillRoundRect(121, 64, 4, var3, 4, 4);
      int var4 = UiScrollbarMath.thumbHeight(var3, var2, 5, 8);
      int var5 = UiScrollbarMath.thumbY(64, var3, var4, var2, 5, this.listScroll);
      var1.setColor(new Color(16767083));
      var1.fillRoundRect(121, var5, 4, var4, 4, 4);
   }

   private void tickWardrobe(VqsvGameRuntime.Scene var1) {
      List var2 = SourceFashionCatalog.instance().records();
      this.selected = clamp(this.selected, 0, var2.size() - 1);
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, var2.size() - 1);
         this.keepSelectedVisible(var2.size());
         this.openedTicks = 0;
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, var2.size() - 1);
         this.keepSelectedVisible(var2.size());
         this.openedTicks = 0;
      } else if (var1.key0) {
         SourceFashionRecord var3 = (SourceFashionRecord)var2.get(this.selected);
         if (var1.session.fashion.equip(var3.stableKey)) {
            VqsvPlayerAppearanceRuntime.applyEffectiveAppearance(var1, "wardrobe-equip");
            var1.session.story.trace().add("FASH-I2 wardrobe equipped key=" + var3.stableKey);
         } else {
            var1.session.story.trace().add("FASH-I2 wardrobe equip blocked unowned key=" + var3.stableKey);
         }
      } else if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
         this.selected = 6;
         this.resetGameMenuViewport();
         this.wardrobePreviewKey = null;
         this.wardrobePreviewAnimator = null;
         this.openedTicks = 0;
         var1.session.story.trace().add("FASH-I2 wardrobe close -> gamemenu");
         return;
      }

      SourceFashionRecord var4 = (SourceFashionRecord)var2.get(this.selected);
      this.ensureWardrobePreview(var4);
      this.wardrobePreviewAnimator.tick();
   }

   private void renderWardrobe(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      List var4 = SourceFashionCatalog.instance().records();
      this.selected = clamp(this.selected, 0, var4.size() - 1);
      this.keepSelectedVisible(var4.size());
      SourceFashionRecord var5 = (SourceFashionRecord)var4.get(this.selected);
      this.ensureWardrobePreview(var5);
      var1.setColor(new Color(1062982));
      var1.fillRect(0, 0, 240, 320);
      var1.setColor(new Color(12445911));
      var1.fillRoundRect(3, 3, 234, 28, 8, 8);
      drawBagCenteredText(var1, var2, "Tủ thời trang", 8, 11, 174, new Color(1527657));
      var1.setColor(new Color(3113616));
      var1.fillRoundRect(194, 8, 35, 18, 9, 9);
      drawBagCenteredText(var1, var2, var3.session.fashion.ownedCount() + "/" + var4.size(), 196, 12, 31, new Color(16777215));
      int var6 = this.visibleListStart(var4.size());

      for(int var7 = 0; var7 < 5; ++var7) {
         int var8 = var6 + var7;
         if (var8 >= var4.size()) {
            break;
         }

         SourceFashionRecord var9 = (SourceFashionRecord)var4.get(var8);
         int var10 = 38 + var7 * 38;
         boolean var11 = var8 == this.selected;
         var1.setColor(new Color(var11 ? 16767083 : 12049638));
         var1.fillRoundRect(6, var10, 112, 35, 7, 7);
         var1.setColor(new Color(var11 ? 16742912 : 4618117));
         var1.drawRoundRect(6, var10, 111, 34, 7, 7);
         var1.setColor(fashionAccentColor(var9));
         var1.fillRoundRect(8, var10 + 3, 4, 29, 4, 4);
         var1.setColor(new Color(var11 ? 16773306 : 15267827));
         var1.fillRoundRect(14, var10 + 4, 28, 28, 6, 6);
         Shape var12 = var1.getClip();
         var1.clipRect(15, var10 + 5, 26, 26);
         this.wardrobeThumbnailAnimator(var9).drawAligned(var1, 15, var10 + 5, 26, 26, 4, 0);
         var1.setClip(var12);
         drawBagText(var1, var2, fashionDisplayName(var9), 46, var10 + 5, 68, new Color(1527657));
         String var13 = var9.stableKey.equals(var3.session.fashion.selectedStableKey()) ? "Đang dùng" : (var3.session.fashion.isOwned(var9.stableKey) ? "Đã có" : "Chưa mở");
         drawBagText(var1, var2, var13, 46, var10 + 19, 68, new Color(var3.session.fashion.isOwned(var9.stableKey) ? 2582586 : 9059131));
      }

      this.drawWardrobeScrollbar(var1, var4.size());
      var1.setColor(new Color(15267827));
      var1.fillRoundRect(130, 38, 104, 145, 10, 10);
      var1.setColor(new Color(14020329));
      var1.fillOval(148, 58, 68, 68);
      var1.setColor(new Color(9616821));
      var1.fillOval(153, 162, 58, 10);
      var1.setColor(fashionAccentColor(var5));
      var1.drawRoundRect(130, 38, 103, 144, 10, 10);
      Shape var14 = var1.getClip();
      var1.clipRect(132, 40, 100, 141);
      this.wardrobePreviewAnimator.drawAligned(var1, 134, 42, 96, 132, 7, 0);
      var1.setClip(var14);
      var1.setColor(new Color(12445911));
      var1.fillRoundRect(130, 188, 104, 90, 8, 8);
      var1.setColor(fashionAccentColor(var5));
      var1.fillRoundRect(130, 188, 4, 90, 4, 4);
      List var15 = wardrobePresentationLines(var5, var3.session.fashion.selectedStableKey(), var3.session.fashion.isOwned(var5.stableKey), var3.session.progression.ride.activeIndex >= 0);
      drawBagText(var1, var2, (String)var15.get(0), 138, 196, 90, new Color(1527657));
      boolean var16 = var3.session.fashion.isOwned(var5.stableKey);
      boolean var17 = var5.stableKey.equals(var3.session.fashion.selectedStableKey());
      var1.setColor(new Color(var17 ? 3113616 : (var16 ? 5018976 : 8095113)));
      var1.fillRoundRect(138, 216, 88, 20, 10, 10);
      drawBagCenteredText(var1, var2, (String)var15.get(1), 140, 221, 84, new Color(16777215));
      if (var15.size() > 2) {
         drawBagCenteredText(var1, var2, (String)var15.get(2), 137, 251, 90, new Color(9067008));
      }

      String var18 = var5.stableKey.equals(var3.session.fashion.selectedStableKey()) ? "Đang mang" : (var3.session.fashion.isOwned(var5.stableKey) ? "Mang" : "Chưa sở hữu");
      drawWardrobeSoftkey(var1, var2, var18, 0, 286, 112, var3.session.fashion.isOwned(var5.stableKey));
      drawWardrobeSoftkey(var1, var2, "Quay lại", 128, 286, 112, true);
   }

   private void ensureWardrobePreview(SourceFashionRecord var1) {
      if (!var1.stableKey.equals(this.wardrobePreviewKey) || this.wardrobePreviewAnimator == null) {
         this.wardrobePreviewKey = var1.stableKey;
         this.wardrobePreviewAnimator = SpriteAnimator.loadFashion(var1);
         this.wardrobePreviewAnimator.setState(var1.sourceKind == SourceFashionRecord.SourceKind.CAU_VONG ? 3 : 0);
      }
   }

   private SpriteAnimator wardrobeThumbnailAnimator(SourceFashionRecord var1) {
      SpriteAnimator var2 = (SpriteAnimator)this.wardrobeThumbnailAnimators.get(var1.stableKey);
      if (var2 != null) {
         return var2;
      } else {
         var2 = SpriteAnimator.loadFashion(var1);
         var2.setState(var1.sourceKind == SourceFashionRecord.SourceKind.CAU_VONG ? 3 : 0);
         this.wardrobeThumbnailAnimators.put(var1.stableKey, var2);
         return var2;
      }
   }

   private void drawWardrobeScrollbar(Graphics2D var1, int var2) {
      short var3 = 190;
      var1.setColor(new Color(3235177));
      var1.fillRoundRect(120, 38, 6, var3, 4, 4);
      int var4 = UiScrollbarMath.thumbHeight(var3, var2, 5, 8);
      int var5 = UiScrollbarMath.thumbY(38, var3, var4, var2, 5, this.listScroll);
      var1.setColor(new Color(16767083));
      var1.fillRoundRect(120, var5, 6, var4, 4, 4);
   }

   private static void drawWardrobeSoftkey(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, boolean var6) {
      SpriteAnimator var7 = SpriteAnimator.load(257);
      drawFashionSourceTexture(var0, var7, 28, var3 + 2, var4, var5 - 4, 28);
      if (!var6) {
         var0.setColor(new Color(69, 85, 89, 190));
         var0.fillRect(var3 + 2, var4, var5 - 4, 28);
      }

      var0.setColor(new Color(var6 ? 15911010 : 7439497));
      var0.drawRect(var3 + 2, var4, var5 - 5, 27);
      drawBagCenteredText(var0, var1, var2, var3 + 4, var4 + 8, var5 - 8, new Color(var6 ? 16777215 : 12765900));
   }

   private static void drawFashionSoftkey(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, boolean var6) {
      var0.setColor(new Color(var6 ? 1195079 : 2504765));
      var0.fillRect(var3 + 2, var4, var5 - 4, 28);
      var0.setColor(new Color(var6 ? 6266276 : 5400170));
      var0.drawRect(var3 + 2, var4, var5 - 5, 27);
      drawBagCenteredText(var0, var1, var2, var3 + 4, var4 + 8, var5 - 8, new Color(var6 ? 16777215 : 9280414));
   }

   private static int wardrobeRowAt(int var0, int var1) {
      return var0 >= 6 && var0 < 118 && var1 >= 38 && var1 < 228 ? (var1 - 38) / 38 : -1;
   }

   private static int fashionMenuRowAt(int var0, int var1) {
      for(int var2 = 0; var2 < FASHION_MENU_CARDS.length; ++var2) {
         int[] var3 = FASHION_MENU_CARDS[var2];
         if (var0 >= var3[0] && var0 < var3[0] + var3[2] && var1 >= var3[1] && var1 < var3[1] + var3[3]) {
            return var2;
         }
      }

      return -1;
   }

   private static int fashionExchangeRowAt(int var0, int var1) {
      if (var0 >= 8 && var0 < 120 && var1 >= 64 && var1 < 244) {
         int var2 = (var1 - 64) / 36;
         int var3 = 64 + var2 * 36;
         return var1 < var3 + 36 - 3 ? var2 : -1;
      } else {
         return -1;
      }
   }

   private static boolean fashionQuantityLeftHit(int var0, int var1) {
      return var0 >= 48 && var0 < 96 && var1 >= 151 && var1 < 183;
   }

   private static boolean fashionQuantityRightHit(int var0, int var1) {
      return var0 >= 144 && var0 < 192 && var1 >= 151 && var1 < 183;
   }

   private static boolean fashionConfirmActionHit(int var0, int var1) {
      return var0 >= 42 && var0 < 198 && var1 >= 196 && var1 < 228;
   }

   private static boolean fashionExchangeActionHit(int var0, int var1) {
      return var0 >= 128 && var0 < 232 && var1 >= 226 && var1 < 256;
   }

   private static int wardrobeSelectedIndex(VqsvGameRuntime.Scene var0) {
      List var1 = SourceFashionCatalog.instance().records();

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         if (((SourceFashionRecord)var1.get(var2)).stableKey.equals(var0.session.fashion.selectedStableKey())) {
            return var2;
         }
      }

      return 0;
   }

   private static String fashionDisplayName(SourceFashionRecord var0) {
      if (var0.sourceKind == SourceFashionRecord.SourceKind.BASE) {
         return "Neil mặc định";
      } else {
         int var1 = 0;

         for(SourceFashionRecord var3 : SourceFashionCatalog.instance().records()) {
            if (var3.sourceKind == var0.sourceKind) {
               ++var1;
            }

            if (var3.stableKey.equals(var0.stableKey)) {
               break;
            }
         }

         String var4 = var0.sourceKind == SourceFashionRecord.SourceKind.LIET_HOA ? "Liệt Hỏa" : "Cầu Vồng";
         return String.format("%s %02d", var4, var1);
      }
   }

   private static List<String> wardrobePresentationLines(SourceFashionRecord var0, String var1, boolean var2, boolean var3) {
      String var4 = var0.stableKey.equals(var1) ? "Đang sử dụng" : (var2 ? "Đã sở hữu" : "Chưa mở khóa");
      return var3 ? List.of(fashionDisplayName(var0), var4, "Khi cưỡi dùng Neil") : List.of(fashionDisplayName(var0), var4);
   }

   private static Color fashionAccentColor(SourceFashionRecord var0) {
      if (var0.sourceKind == SourceFashionRecord.SourceKind.BASE) {
         return new Color(5023338);
      } else {
         return var0.sourceKind == SourceFashionRecord.SourceKind.LIET_HOA ? new Color(15303211) : new Color(3768255);
      }
   }

   private void renderRide(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("ride.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      SpriteAnimator var6 = SpriteAnimator.load(260);
      drawCell(var4, var5, var1, 1);
      drawCell(var4, var5, var1, 2);
      drawCell(var4, var5, var1, 3);

      for(int var7 = 0; var7 < 4; ++var7) {
         boolean var8 = sourceRideUnlocked(var3, var7);
         boolean var9 = sourceRideUsable(var3, var7);
         int var10 = var8 ? (var7 == this.rideSelected ? var7 : var7 + 8) : var7 + 4;
         drawCellTopLeft(var6, var1, var10, var4.x(var7 + 4, 32 + var7 * 44), var4.y(var7 + 4, 270));
         drawTextWide(var1, var2, var4, var7 + 8, var7 == this.rideSelected && var8 ? RIDE_LABELS[var7] : "", 0, var4.w(var7 + 8, 44), color(var4.widget(var7 + 8), 1862801));
         if (var8 && !var9) {
            drawCellTopLeft(var5, var1, 131, var4.x(var7 + 16, 45 + var7 * 44), var4.y(var7 + 16, 298));
         }
      }

      drawRideCloseButton(var1);
   }

   private static void drawRideCloseButton(Graphics2D var0) {
      var0.setColor(new Color(1060669));
      var0.fillRoundRect(6, 6, 22, 22, 7, 7);
      var0.setColor(new Color(16044907));
      var0.drawRoundRect(6, 6, 22, 22, 7, 7);
      var0.setColor(Color.WHITE);
      byte var1 = 6;
      int var2 = 28 - var1;
      int var3 = 28 - var1;
      var0.drawLine(6 + var1, 6 + var1, var2, var3);
      var0.drawLine(var2, 6 + var1, 6 + var1, var3);
   }

   static boolean rideCloseHit(int var0, int var1) {
      return var0 >= 6 && var0 <= 28 && var1 >= 6 && var1 <= 28;
   }

   private void renderGameSystem(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("gamesystem.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      this.drawSystemFrame(var1, var3, var4);
      this.drawRows(var1, var2, var3, var4, SYSTEM_ROW_WIDGETS, SYSTEM_LABELS);
      drawText(var1, var2, var3, 2, var3.text(2, "He thong menu"), color(var3.widget(2), 13631758));
      drawSoftkey(var1, var2, var3, var4, 10, var3.text(10, "Xac dinh"), 16777215);
      drawSoftkey(var1, var2, var3, var4, 11, var3.text(11, "Quay lai"), 16777215);
   }

   private void renderHelp(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("help1.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      SpriteAnimator var5 = SpriteAnimator.load(325);
      this.drawHelpFrame(var1, var3, var4);
      drawText(var1, var2, var3, 5, "Trợ giúp", color(var3.widget(5), 13631758));
      drawText(var1, var2, var3, 6, var3.text(6, "Quay lai"), color(var3.widget(6), 16777215));
      drawText(var1, var2, var3, 39, this.helpPage + 1 + "/3", color(var3.widget(39), 1862801));
      this.drawHelpPageArrows(var1, var2, var3, var4);
      if (this.helpPage == 0) {
         drawMultilineText(var1, var2, var3, 8, "Nhấn nút 2, 4, 6, 8 để di chuyển#nNút 5: công kích, đối thoại, xác nhận#nNút 1: Xem nhiệm vụ#nNút 9: lựa chọn sủng vật cưỡi#nNút 0: Xem bản đồ#nNút mềm trái: menu hệ thống#nNút mềm phải: menu trò chơi", color(var3.widget(8), 1862801));
      } else {
         for(int var6 = 0; var6 < 14; ++var6) {
            int var7 = (this.helpPage - 1) * 14 + var6;
            int var8 = 9 + (var6 << 1);
            int var9 = var8 + 1;
            if (var7 < 26) {
               VqsvUiLayout.UiWidget var10 = var3.widget(var8);
               if (var10 != null) {
                  drawCellTopLeft(var5, var1, var7 + 1, var10.x, var10.y);
               }

               drawTextWide(var1, var2, var3, var9, helpEffectText(var7), 0, 44, color(var3.widget(var9), 1862801));
            }
         }

      }
   }

   private void drawHelpPageArrows(Graphics2D var1, UiFont var2, VqsvUiLayout var3, SpriteAnimator var4) {
      if (this.helpPage > 0) {
         drawCell(var3, var4, var1, 37);
      }

      if (this.helpPage < 2) {
         drawCell(var3, var4, var1, 38);
      }

   }

   private void renderSettings(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("help.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      this.drawHelpFrame(var1, var4, var5);
      drawCell(var4, var5, var1, 7);
      drawText(var1, var2, var4, 5, "Tùy chọn", color(var4.widget(5), 13631758));
      drawTextWide(var1, var2, var4, 7, var3.session.runtime.speedX2 ? "x2: Bật" : "x2: Tắtt", 0, 52, color(var4.widget(7), 16777215));
      drawTextWide(var1, var2, var4, 9, var4.text(9, "Am luong"), 0, 42, color(var4.widget(9), 1862801));

      for(int var6 = 10; var6 <= 12; ++var6) {
         VqsvUiLayout.UiWidget var7 = var4.widget(var6);
         if (var7 != null) {
            int var8 = var6 - 9;
            int var9 = var8 <= this.settingsLevel ? 16775068 : 8235140;
            var1.setColor(new Color(var9 & 16777215));
            var1.fillRect(var7.x, var7.y, Math.max(5, var7.w), 12);
            var1.setColor(new Color(3232363));
            var1.drawRect(var7.x, var7.y, Math.max(5, var7.w), 12);
         }
      }

      drawSettingsSpeedCheckbox(var1, var2, var3.session.runtime.speedX2);
   }

   private static void drawSettingsSpeedCheckbox(Graphics2D var0, UiFont var1, boolean var2) {
      byte var3 = 88;
      short var4 = 184;
      var0.setColor(new Color(14219263));
      var0.fillRect(var3 - 4, var4 - 5, 74, 20);
      var0.setColor(new Color(3232363));
      var0.drawRect(var3 - 4, var4 - 5, 74, 20);
      var0.setColor(new Color(var2 ? 13631758 : 1862801));
      var0.drawRect(var3, var4, 10, 10);
      if (var2) {
         var0.drawLine(var3 + 2, var4 + 5, var3 + 4, var4 + 8);
         var0.drawLine(var3 + 4, var4 + 8, var3 + 9, var4 + 2);
      }

      String var5 = "Speed x2";
      var1.drawTaggedLine(var0, var5, var3 + 16, var4 + 1, var5.length(), var2 ? 13631758 : 1862801);
   }

   private void renderOptionConfirm(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("option.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      this.drawOptionRow(var1, var2, var3, var4, 0, 10, 12, 8, "Có");
      this.drawOptionRow(var1, var2, var3, var4, 1, 11, 13, 9, "Không");
   }

   private void renderSave(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("msgtip.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      drawCell(var3, var4, var1, 1);
      drawText(var1, var2, var3, 2, this.saveMessage, color(var3.widget(2), 1862801));
      if (this.savePhase == 0) {
         drawCell(var3, var4, var1, 3);
         drawCell(var3, var4, var1, 4);
      }

   }

   private void renderBag(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      SpriteAnimator var4 = SpriteAnimator.load(258);
      List var5 = bagRows(var3, this.bagTab);
      int var6 = this.visibleListStart(var5.size());
      var1.setColor(new Color(0, 18, 28, 132));
      var1.fillRect(0, 32, 240, 263);
      var1.setColor(new Color(466733));
      var1.fillRoundRect(12, 46, 222, 246, 12, 12);
      var1.setColor(new Color(13233651));
      var1.fillRoundRect(9, 42, 222, 246, 11, 11);
      var1.setColor(new Color(1527657));
      var1.drawRoundRect(9, 42, 221, 245, 11, 11);
      var1.setColor(new Color(1064790));
      var1.fillRoundRect(12, 45, 216, 25, 8, 8);
      var1.fillRect(12, 62, 216, 8);
      var1.setColor(new Color(16773482));
      var1.fillRect(12, 68, 216, 2);
      drawBagCenteredText(var1, var2, "LƯNG BAO", 22, 52, 112, Color.WHITE);
      drawBagPill(var1, var2, var5.size() + " mục", 167, 50, 52, 15, 16773482, 6244376);
      String[] var7 = new String[]{"Tiêu hao", "Trang sức", "Tài liệu", "Đặc thù"};
      int[] var8 = new int[]{2792387, 9136315, 4301676, 12679989};

      for(int var9 = 0; var9 < var7.length; ++var9) {
         int var10 = 15 + var9 * 53;
         boolean var11 = var9 == this.bagTab;
         var1.setColor(new Color(var11 ? var8[var9] : 11064799));
         var1.fillRoundRect(var10, 74, 51, 21, 6, 6);
         var1.setColor(new Color(var11 ? 16773482 : 6134698));
         var1.drawRoundRect(var10, 74, 50, 20, 6, 6);
         if (var11) {
            var1.fillRect(var10 + 5, 92, 41, 2);
         }

         drawBagCenteredText(var1, var2, var7[var9], var10 + 2, 80, 47, var11 ? Color.WHITE : new Color(1527657));
      }

      var1.setColor(new Color(3111056));
      var1.fillRoundRect(16, 98, 208, 15, 5, 5);
      drawBagText(var1, var2, "VẬT PHẨM", 24, 101, 110, Color.WHITE);
      drawBagCenteredText(var1, var2, this.bagTab == 1 ? "TRẠNG THÁI" : "SỐ LƯỢNG", 161, 101, 57, Color.WHITE);
      int var18 = var8[clamp(this.bagTab, 0, var8.length - 1)];

      for(int var19 = 0; var19 < 5; ++var19) {
         int var21 = var6 + var19;
         int var12 = 115 + var19 * 22;
         boolean var13 = var21 < var5.size() && var21 == this.selected;
         if (var13) {
            var1.setColor(new Color(601920));
            var1.fillRoundRect(18, var12 + 2, 204, 20, 6, 6);
         }

         var1.setColor(new Color(var13 ? darkerBagColor(var18) : 15267834));
         var1.fillRoundRect(16, var12, 204, 20, 6, 6);
         var1.setColor(new Color(var13 ? 16773482 : 8633546));
         var1.drawRoundRect(16, var12, 203, 19, 6, 6);
         var1.setColor(new Color(var13 ? 16773482 : var18));
         var1.fillRoundRect(16, var12, 5, 20, 5, 5);
         if (var21 >= var5.size()) {
            drawBagCenteredText(var1, var2, "·", 24, var12 + 5, 188, new Color(9222081));
         } else {
            BagRow var14 = (BagRow)var5.get(var21);
            byte var15 = 23;
            int var16 = var12 + 3;
            var1.setColor(new Color(var13 ? 16769946 : 12969965));
            var1.fillRoundRect(var15 - 2, var16 - 2, 18, 18, 5, 5);
            var1.setColor(new Color(1527657));
            var1.drawRoundRect(var15 - 2, var16 - 2, 17, 17, 5, 5);
            if (!UnifiedItemIconRenderer.draw(var1, var14.item.iconResource, var15, var16, 14, 14)) {
               drawCellTopLeft(var4, var1, var14.item.iconCell, var15, var16);
            }

            drawBagMarquee(var1, var2, var14.item.name, 43, var12 + 6, 115, var13 ? Color.WHITE : new Color(1527657), this.openedTicks);
            int var17 = var14.specialEgg && var14.count > 0 ? 38 : 49;
            drawBagPill(var1, var2, var14.statusText(), 164, var12 + 3, var17, 14, var13 ? 16773482 : 1523542, var13 ? 6309911 : 16777215);
            if (var14.specialEgg && var14.count > 0) {
               drawBagPill(var1, var2, "?", 200, var12 + 2, 18, 16, var13 ? 16773482 : 2792387, var13 ? 6309911 : 16777215);
            }
         }
      }

      String var20 = "Chưa có vật phẩm trong nhóm này.";
      if (!var5.isEmpty()) {
         BagRow var22 = (BagRow)var5.get(clamp(this.selected, 0, var5.size() - 1));
         var20 = var22.item.description;
         RainbowCharmCatalog.Definition var23 = RainbowCharmCatalog.instance().byRuntimeId(var22.item.id);
         if (var23 != null) {
            int var24 = var3.session.progression.rainbowCharms.tier(var23.runtimeId);
            var20 = rainbowCharmEffectDetail(var23, Math.max(1, var24));
         }
      }

      var1.setColor(new Color(1523542));
      var1.fillRoundRect(16, 226, 208, 35, 7, 7);
      var1.setColor(new Color(var18));
      var1.fillRoundRect(16, 226, 49, 12, 6, 6);
      var1.setColor(new Color(16773482));
      var1.drawRoundRect(16, 226, 207, 34, 7, 7);
      drawBagCenteredText(var1, var2, "CHI TIẾT", 19, 228, 43, Color.WHITE);
      drawBagWrappedFixed(var1, var2, var20, 70, 229, 148, 3, Color.WHITE);
      drawModernBagScrollbar(var1, var5.size(), var6);
      drawBagButton(var1, var2, this.bagActionLabel(var3), 18, 265, 78, 19, true, var18);
      drawBagButton(var1, var2, "Rời đi", 144, 265, 78, 19, false, var18);
      if (this.eggPreviewOpen) {
         this.renderEggPreview(var1, var2);
      }

   }

   private void renderEggPreview(Graphics2D var1, UiFont var2) {
      List var3 = EggPreviewCatalog.entries(this.eggPreviewItemId);
      byte var4 = 12;
      byte var5 = 44;
      short var6 = 216;
      short var7 = 242;
      var1.setColor(new Color(466733));
      var1.fillRoundRect(var4 + 3, var5 + 4, var6, var7, 10, 10);
      var1.setColor(new Color(13233651));
      var1.fillRoundRect(var4, var5, var6, var7, 10, 10);
      var1.setColor(new Color(1527657));
      var1.drawRoundRect(var4, var5, var6 - 1, var7 - 1, 10, 10);
      var1.setColor(new Color(1064790));
      var1.fillRoundRect(var4 + 4, var5 + 4, var6 - 8, 24, 7, 7);
      drawBagText(var1, var2, EggPreviewCatalog.eggName(this.eggPreviewItemId), var4 + 12, var5 + 10, var6 - 54, Color.WHITE);
      drawBagPill(var1, var2, "?", var4 + var6 - 36, var5 + 8, 22, 16, 16773482, 6309911);
      int var8 = Math.min(this.eggPreviewScroll, Math.max(0, var3.size() - 5));
      byte var9 = 38;

      for(int var10 = 0; var10 < 5; ++var10) {
         int var11 = var8 + var10;
         int var12 = var5 + 34 + var10 * var9;
         var1.setColor(new Color(15267834));
         var1.fillRoundRect(var4 + 8, var12, var6 - 16, var9 - 3, 6, 6);
         var1.setColor(new Color(8633546));
         var1.drawRoundRect(var4 + 8, var12, var6 - 17, var9 - 4, 6, 6);
         if (var11 < var3.size()) {
            EggPreviewCatalog.Entry var13 = (EggPreviewCatalog.Entry)var3.get(var11);
            var1.setColor(new Color(12969965));
            var1.fillRoundRect(var4 + 12, var12 + 4, 30, 28, 5, 5);
            Shape var14 = var1.getClip();
            var1.clipRect(var4 + 13, var12 + 5, 28, 26);
            SpriteAnimator var15 = SpriteAnimator.load(var13.visualId);
            var15.setState(0);
            var15.drawAligned(var1, var4 + 13, var12 + 5, 28, 26, 4, 0);
            var1.setClip(var14);
            drawBagMarquee(var1, var2, var13.name, var4 + 50, var12 + 12, var6 - 66, new Color(1527657), this.openedTicks);
         }
      }

      drawBagButton(var1, var2, "Đóng", var4 + 73, var5 + var7 - 25, var6 - 146, 18, true, 2792387);
      if (var8 > 0) {
         drawBagCenteredText(var1, var2, "▲", var4 + var6 - 24, var5 + 35, 14, new Color(1527657));
      }

      if (var8 < var3.size() - 5) {
         drawBagCenteredText(var1, var2, "▼", var4 + var6 - 24, var5 + var7 - 47, 14, new Color(1527657));
      }

   }

   private int eggPreviewMaxScroll() {
      return Math.max(0, EggPreviewCatalog.entries(this.eggPreviewItemId).size() - 5);
   }

   private static boolean eggPreviewHit(int var0, int var1) {
      short var2 = 197;
      return var0 >= var2 && var0 < var2 + 20;
   }

   private static void drawModernBagScrollbar(Graphics2D var0, int var1, int var2) {
      if (var1 > 5) {
         var0.setColor(new Color(1208209));
         var0.fillRoundRect(222, 115, 4, 108, 4, 4);
         int var3 = UiScrollbarMath.thumbHeight(108, var1, 5, 9);
         int var4 = UiScrollbarMath.thumbY(115, 108, var3, var1, 5, var2);
         var0.setColor(new Color(16773482));
         var0.fillRoundRect(221, var4, 6, var3, 4, 4);
      }
   }

   private static void drawBagButton(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, boolean var7, int var8) {
      if (var2 != null && !var2.isEmpty()) {
         var0.setColor(new Color(731192));
         var0.fillRoundRect(var3 + 2, var4 + 2, var5, var6, 7, 7);
         var0.setColor(new Color(var7 ? var8 : 12049638));
         var0.fillRoundRect(var3, var4, var5, var6, 7, 7);
         var0.setColor(new Color(var7 ? 16773482 : 4618117));
         var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 7, 7);
         drawBagCenteredText(var0, var1, var2, var3 + 3, var4 + 5, var5 - 6, var7 ? Color.WHITE : new Color(1527657));
      }
   }

   private static void drawBagPill(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      var0.setColor(new Color(var7));
      var0.fillRoundRect(var3, var4, var5, var6, 7, 7);
      var0.setColor(new Color(var7 == 16773482 ? 16777215 : 7845829));
      var0.drawRoundRect(var3, var4, var5 - 1, var6 - 1, 7, 7);
      drawBagCenteredText(var0, var1, var2, var3 + 2, var4 + 3, var5 - 4, new Color(var8));
   }

   private static void drawBagText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6) {
      String var7 = TextBox.decodeMojibake(var2 == null ? "" : var2);
      String var8 = fitBagText(var1, var7, var5);
      Shape var9 = var0.getClip();
      var0.clipRect(var3, var4 - 1, Math.max(1, var5), var1.height + 2);
      var1.drawTaggedLine(var0, var8, var3, var4, TextBox.visibleLength(var8), var6.getRGB() & 16777215);
      var0.setClip(var9);
   }

   private static void drawBagCenteredText(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6) {
      String var7 = TextBox.decodeMojibake(var2 == null ? "" : var2);
      String var8 = fitBagText(var1, var7, var5);
      int var9 = var3 + Math.max(0, (var5 - var1.taggedWidth(var8)) / 2);
      drawBagText(var0, var1, var8, var9, var4, Math.max(1, var5 - (var9 - var3)), var6);
   }

   private static void drawBagMarquee(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, Color var6, int var7) {
      String var8 = TextBox.decodeMojibake(var2 == null ? "" : var2);
      int var9 = var1.taggedWidth(var8);
      int var10 = 0;
      if (var9 > var5) {
         int var11 = var9 - var5;
         int var12 = Math.max(0, var7 - 16) / 3;
         int var13 = Math.max(1, var11 * 2 + 32);
         int var14 = var12 % var13;
         var10 = var14 <= var11 ? var14 : Math.max(0, var13 - var14 - 16);
         var10 = Math.min(var11, var10);
      }

      Shape var16 = var0.getClip();
      var0.clipRect(var3, var4 - 1, Math.max(1, var5), var1.height + 2);
      var1.drawTaggedLine(var0, var8, var3 - var10, var4, TextBox.visibleLength(var8), var6.getRGB() & 16777215);
      var0.setClip(var16);
   }

   private static void drawBagWrappedFixed(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, Color var7) {
      List var8 = wrapText(var1, TextBox.decodeMojibake(var2), var5);
      Shape var9 = var0.getClip();
      var0.clipRect(var3, var4 - 1, Math.max(1, var5), var6 * (var1.height + 1));

      for(int var10 = 0; var10 < Math.min(var6, var8.size()); ++var10) {
         String var11 = (String)var8.get(var10);
         if (var10 == var6 - 1 && var8.size() > var6) {
            var11 = fitBagText(var1, var11 + "...", var5);
         } else {
            var11 = fitBagText(var1, var11, var5);
         }

         var1.drawTaggedLine(var0, var11, var3, var4 + var10 * (var1.height + 1), TextBox.visibleLength(var11), var7.getRGB() & 16777215);
      }

      var0.setClip(var9);
   }

   private static String fitBagText(UiFont var0, String var1, int var2) {
      if (var1 != null && !var1.isEmpty() && var0.taggedWidth(var1) > var2) {
         String var3 = "...";

         int var4;
         for(var4 = var1.length(); var4 > 0; --var4) {
            String var10001 = var1.substring(0, var4);
            if (var0.taggedWidth(var10001 + var3) <= var2) {
               break;
            }
         }

         return var4 <= 0 ? var3 : var1.substring(0, var4).trim() + var3;
      } else {
         return var1 == null ? "" : var1;
      }
   }

   private static int darkerBagColor(int var0) {
      int var1 = (var0 >> 16 & 255) * 62 / 100;
      int var2 = (var0 >> 8 & 255) * 62 / 100;
      int var3 = (var0 & 255) * 62 / 100;
      return var1 << 16 | var2 << 8 | var3;
   }

   private void renderTransmit(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("transmit.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      fillBand(var1, var3, 2, 13037823, 7);
      fillBand(var1, var3, 3, 12511218, 102);
      fillBand(var1, var3, 4, 7124923, 8);
      drawCell(var3, var4, var1, 1);
      drawTextWide(var1, var2, var3, 11, var3.text(11, "Gửi đi"), 0, var3.w(11, 48), color(var3.widget(11), 13631758));
      int var5 = this.visibleListStart(TRANSMIT_DESTINATIONS.length);

      for(int var6 = 0; var6 < TRANSMIT_ROW_WIDGETS.length; ++var6) {
         int var7 = var5 + var6;
         VqsvUiLayout.UiWidget var8 = var3.widget(TRANSMIT_ROW_WIDGETS[var6]);
         if (var8 != null) {
            drawCellState(var3, var4, var1, TRANSMIT_ROW_WIDGETS[var6], var7 == this.selected);
            if (var7 < TRANSMIT_DESTINATIONS.length) {
               drawTextMarquee(var1, var2, var3, TRANSMIT_ROW_WIDGETS[var6], TRANSMIT_DESTINATIONS[var7], var3.w(TRANSMIT_ROW_WIDGETS[var6], 59), var7 == this.selected ? colorSelected(var8) : color(var8, 1862802), this.openedTicks);
            }
         }
      }

      this.drawTransmitScrollbar(var1, var3, var5);
      drawSoftkey(var1, var2, var3, var4, 14, var3.text(14, "Xác định"), 16777215);
      drawSoftkey(var1, var2, var3, var4, 15, var3.text(15, "Quay lại"), 16777215);
   }

   private void renderPortableShop(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      int var4 = 8083617;
      drawShopPanelBase(var1, var2, "SỦNG VẬT THƯƠNG ĐIẾM", var4);
      drawBagPill(var1, var2, "", 158, 51, 60, 15, 15911010, 6047770);
      VqsvShopCurrencyRenderer.draw(var1, var2, var3.session.inventory.currency.money, 0, 158, 51, 60, 15, new Color(6047770));
      String[] var5 = new String[]{"Vật phẩm", "Hàng ngày", "Thời trang", "Tài liệu"};
      int[] var6 = new int[]{3054715, 13674554, 4100549, 8083617};

      for(int var7 = 0; var7 < PORTABLE_SHOP_LABELS.length; ++var7) {
         int var8 = 79 + var7 * 29;
         boolean var9 = var7 == this.selected;
         if (var9) {
            var1.setColor(new Color(466733));
            var1.fillRoundRect(23, var8 + 3, 200, 27, 7, 7);
         }

         var1.setColor(new Color(var9 ? darkerBagColor(var6[var7]) : 15924216));
         var1.fillRoundRect(20, var8, 200, 27, 7, 7);
         var1.setColor(new Color(var9 ? 15911010 : 7711401));
         var1.drawRoundRect(20, var8, 199, 26, 7, 7);
         var1.setColor(new Color(var6[var7]));
         var1.fillRoundRect(20, var8, 7, 27, 7, 7);
         int var10 = var7 == 0 ? 0 : var7 + 1;
         drawPortableShopServiceIcon(var1, var10, 38, var8 + 6, var6[var7], var9);
         drawBagMarquee(var1, var2, PORTABLE_SHOP_LABELS[var7], 62, var8 + 8, 112, var9 ? Color.WHITE : new Color(1527657), this.openedTicks);
         drawBagPill(var1, var2, var5[var7], 178, var8 + 6, 37, 15, var9 ? 15911010 : 1523542, var9 ? 6047770 : 16777215);
         if (var9) {
            Shape var11 = var1.getClip();
            var1.clipRect(28, var8 + 2, 184, 23);
            int var12 = 32 + Math.max(0, this.openedTicks) * 2 % Math.max(1, 176);
            var1.setColor(new Color(255, 255, 255, 38));
            var1.fillRect(var12, var8 + 2, 8, 23);
            var1.setClip(var11);
         }
      }

      var1.setColor(new Color(1523542));
      var1.fillRoundRect(20, 226, 200, 31, 7, 7);
      var1.setColor(new Color(15911010));
      var1.drawRoundRect(20, 226, 199, 30, 7, 7);
      drawBagWrappedFixed(var1, var2, portableShopDescription(var3, this.selected), 28, 231, 184, 2, Color.WHITE);
      drawBagButton(var1, var2, "Mở quầy", 25, 264, 76, 19, true, var4);
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, false, var4);
   }

   private void renderChallenge(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      int var4 = 13138476;
      RegionalChallengeService.ChallengePlan var5 = this.challengeRegion < 0 ? null : REGIONAL_CHALLENGES.rematchPlan(this.challengeRegion);
      drawShopPanelBase(var1, var2, "TÁI ĐẤU & QUÀ TẶNG", var4);
      if (this.challengeRegion < 0) {
         this.drawChallengeRow(var1, var2, 0, 77, "Tái đấu Thủ lĩnh", "Chưa mở");
      } else {
         RegionalRematchState.Status var6 = regionalRematchStatus(var3, this.challengeRegion);
         drawBagPill(var1, var2, "◀ " + var5.regionName + " ▶", 48, 43, 144, 20, 15911010, 6047770);
         this.drawChallengeRow(var1, var2, 0, 77, "Tái đấu Thủ lĩnh", var6.remainingUses() + "/3 lượt");
      }

      this.drawChallengeRow(var1, var2, 1, 124, "Giftcode", "Nhập mã nhận quà");
      String var7 = this.selected == 1 ? "Mỗi Giftcode chỉ nhận một lần trên một save. Quà được lưu ngay sau khi nhận." : (this.challengeRegion < 0 ? "Cần huy chương Hội quán để mở tái đấu." : rematchDetail(var3, this.challengeRegion, var5.rewardMoney));
      drawBagWrappedFixed(var1, var2, var7, 18, 174, 204, 3, Color.WHITE);
      drawBagButton(var1, var2, this.selected == 1 ? "Nhập mã" : "Chiến đấu", 25, 264, 76, 19, true, var4);
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, false, var4);
   }

   private void renderGiftCode(Graphics2D var1, UiFont var2) {
      int var3 = 4944578;
      drawShopPanelBase(var1, var2, "GIFT CODE", var3);
      drawBagWrappedFixed(var1, var2, "Nhập mã bằng bàn phím hoặc dán với Ctrl+V.", 18, 46, 204, 2, new Color(1527657));
      var1.setColor(new Color(16252159));
      var1.fillRoundRect(18, 82, 204, 38, 8, 8);
      var1.setColor(new Color(4944578));
      var1.drawRoundRect(18, 82, 203, 37, 8, 8);
      String var4 = this.giftCodeInput.isEmpty() ? "NHẬP GIFT CODE" : this.giftCodeInput;
      int var5 = this.giftCodeInput.isEmpty() ? 8559016 : 1523542;
      var2.drawTaggedLine(var1, var4, 28, 96, var4.length(), var5);
      if (!this.giftCodeInput.isEmpty() && this.openedTicks / 8 % 2 == 0) {
         int var6 = Math.min(211, 28 + var2.taggedWidth(this.giftCodeInput));
         var1.setColor(new Color(4944578));
         var1.fillRect(var6 + 1, 94, 1, 13);
      }

      var1.setColor(new Color(15200759));
      var1.fillRoundRect(18, 132, 204, 78, 8, 8);
      var1.setColor(new Color(7711401));
      var1.drawRoundRect(18, 132, 203, 77, 8, 8);
      drawBagWrappedFixed(var1, var2, this.giftCodeMessage, 28, 143, 184, 4, new Color(1527657));
      drawBagButton(var1, var2, "Đổi quà", 25, 264, 76, 19, true, var3);
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, false, var3);
   }

   private void drawChallengeRow(Graphics2D var1, UiFont var2, int var3, int var4, String var5, String var6) {
      boolean var7 = this.selected == var3;
      var1.setColor(new Color(var7 ? 7619358 : 15924216));
      var1.fillRoundRect(18, var4, 204, 38, 8, 8);
      var1.setColor(new Color(var7 ? 15911010 : 7711401));
      var1.drawRoundRect(18, var4, 203, 37, 8, 8);
      var2.drawTaggedLine(var1, var5, 29, var4 + 7, var5.length(), var7 ? 16777215 : 1527657);
      var2.drawTaggedLine(var1, var6, 29, var4 + 23, var6.length(), var7 ? 15911010 : 9132576);
   }

   private static void drawShopPanelBase(Graphics2D var0, UiFont var1, String var2, int var3) {
      var0.setColor(new Color(0, 18, 28, 132));
      var0.fillRect(0, 32, 240, 263);
      var0.setColor(new Color(466733));
      var0.fillRoundRect(15, 46, 216, 246, 12, 12);
      var0.setColor(new Color(13167080));
      var0.fillRoundRect(12, 42, 216, 246, 11, 11);
      var0.setColor(new Color(1527657));
      var0.drawRoundRect(12, 42, 215, 245, 11, 11);
      var0.setColor(new Color(darkerBagColor(var3)));
      var0.fillRoundRect(15, 45, 210, 27, 8, 8);
      var0.fillRect(15, 64, 210, 8);
      var0.setColor(new Color(15911010));
      var0.fillRect(15, 70, 210, 2);
      drawBagCenteredText(var0, var1, var2, 22, 53, 132, Color.WHITE);
   }

   private static void drawPortableShopServiceIcon(Graphics2D var0, int var1, int var2, int var3, int var4, boolean var5) {
      var0.setColor(new Color(var5 ? 15911010 : 14216942));
      var0.fillRoundRect(var2 - 3, var3 - 3, 23, 20, 6, 6);
      var0.setColor(new Color(var4));
      switch (var1) {
         case 0:
            var0.fillRect(var2 + 1, var3 + 4, 13, 8);
            var0.drawRect(var2 + 4, var3 + 1, 7, 4);
            break;
         case 1:
            var0.fillRect(var2 + 6, var3, 3, 14);
            var0.fillRect(var2, var3 + 6, 15, 3);
            break;
         case 2:
            var0.fillOval(var2 + 1, var3, 13, 13);
            var0.setColor(new Color(var5 ? 15911010 : 14216942));
            var0.fillOval(var2 + 5, var3 + 4, 5, 5);
            break;
         case 3:
            var0.fillOval(var2, var3 + 2, 10, 10);
            var0.drawOval(var2 + 5, var3, 10, 10);
            break;
         default:
            int[] var6 = new int[]{var2 + 7, var2 + 14, var2 + 10, var2 + 4, var2};
            int[] var7 = new int[]{var3, var3 + 6, var3 + 14, var3 + 14, var3 + 6};
            var0.fillPolygon(var6, var7, var6.length);
      }

   }

   private void renderSourceEastWharf(Graphics2D var1, UiFont var2) {
      boolean var3 = this.sourceWharfIndex == 4;
      VqsvUiLayout var4 = VqsvUiLayout.load(var3 ? "wharf2.ui" : "wharf1.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      fillBand(var1, var4, 2, 13037823, 7);
      fillBand(var1, var4, 3, 12511218, 102);
      fillBand(var1, var4, 4, 7124923, 8);
      drawCell(var4, var5, var1, 1);
      this.drawRows(var1, var2, var4, var5, this.sourceWharfRowWidgets(), this.sourceWharfLabels());
      int var6 = var3 ? 10 : 8;
      int var7 = var3 ? 12 : 9;
      int var8 = var3 ? 11 : 10;
      drawTextWide(var1, var2, var4, var6, SOURCE_WHARF_TITLES[this.sourceWharfIndex], -8, var4.w(var6, 72), color(var4.widget(var6), 13631758));
      drawSoftkey(var1, var2, var4, var5, var7, var4.text(var7, "Di chuyển nhanh"), 16777215);
      drawSoftkey(var1, var2, var4, var5, var8, var4.text(var8, "Quay lại"), 16777215);
   }

   private String[] sourceWharfLabels() {
      return SOURCE_WHARF_LABELS[clamp(this.sourceWharfIndex, 0, SOURCE_WHARF_LABELS.length - 1)];
   }

   private int[] sourceWharfRowWidgets() {
      return this.sourceWharfIndex == 4 ? SOURCE_TEMPLE_WHARF_ROW_WIDGETS : SOURCE_EAST_WHARF_ROW_WIDGETS;
   }

   private void renderSourceConvenienceShop(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("wharf2.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      fillBand(var1, var3, 2, 13037823, 7);
      fillBand(var1, var3, 3, 12511218, 102);
      fillBand(var1, var3, 4, 7124923, 8);
      drawCell(var3, var4, var1, 1);
      String[] var5 = this.sourceConvenienceLabels();
      int var6 = this.visibleListStart(var5.length);

      for(int var7 = 0; var7 < SOURCE_CONVENIENCE_ROW_WIDGETS.length; ++var7) {
         int var8 = var6 + var7;
         if (var8 >= var5.length) {
            break;
         }

         int var9 = SOURCE_CONVENIENCE_ROW_WIDGETS[var7];
         VqsvUiLayout.UiWidget var10 = var3.widget(var9);
         if (var10 != null) {
            int var11 = var8 == this.selected ? var10.altId : var10.imageId;
            drawCellTopLeft(var4, var1, var11, var10.x, var10.y);
            drawText(var1, var2, var3, var9, var5[var8], var8 == this.selected ? colorSelected(var10) : color(var10, 1862802));
         }
      }

      drawTextWide(var1, var2, var3, 10, "Tiện lợi điếm", -8, var3.w(10, 72), color(var3.widget(10), 13631758));
      drawSoftkey(var1, var2, var3, var4, 12, "Tiến vào", 16777215);
      drawSoftkey(var1, var2, var3, var4, 11, var3.text(11, "Quay lại"), 16777215);
   }

   private static void drawPortableShopDescription(Graphics2D var0, UiFont var1, String var2) {
      if (var2 != null && !var2.isEmpty()) {
         Shape var3 = var0.getClip();
         var0.clipRect(36, 223, 168, 28);
         int var4 = 224;

         for(String var6 : wrapText(var1, TextBox.decodeMojibake(var2), 168)) {
            if (var4 > 252 - var1.height) {
               break;
            }

            int var7 = 36 + Math.max(0, (168 - var1.taggedWidth(var6)) / 2);
            var1.drawTaggedLine(var0, var6, var7, var4, TextBox.visibleLength(TextBox.decodeMojibake(var6)), 1862801);
            var4 += var1.height + 1;
         }

         var0.setClip(var3);
      }
   }

   private void drawPortableShopMaterialRow(Graphics2D var1, UiFont var2, SpriteAnimator var3, boolean var4) {
      drawCellTopLeft(var3, var1, var4 ? 126 : 127, 70, 158);
      Shape var5 = var1.getClip();
      var1.clipRect(70, 157, 114, 14);
      String var6 = PORTABLE_SHOP_LABELS[3];
      int var7 = var2.taggedWidth(var6);
      int var8 = 70 + Math.max(0, (108 - var7) / 2);
      var2.drawTaggedLine(var1, var6, var8, 158, TextBox.visibleLength(TextBox.decodeMojibake(var6)), var4 ? 16753920 : 1862801);
      var1.setClip(var5);
   }

   private void renderPortableShopBuy(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      SpriteAnimator var4 = SpriteAnimator.load(258);
      int var5 = this.shopTable == 3 && this.shopBucket == 2 ? 8083617 : 3054715;
      String var6 = this.shopTable == 3 && this.shopBucket == 2 ? "CỬA HÀNG TÀI LIỆU" : "THƯƠNG ĐIẾM BÌNH DÂN";
      drawShopPanelBase(var1, var2, var6, var5);
      var1.setColor(new Color(2318176));
      var1.fillRoundRect(20, 76, 200, 19, 6, 6);
      drawBagText(var1, var2, "KIM TIỀN", 28, 81, 48, new Color(12577250));
      drawBagText(var1, var2, String.valueOf(var3.session.inventory.currency.money), 79, 81, 48, new Color(15911010));
      drawBagText(var1, var2, "HUY HIỆU", 136, 81, 51, new Color(12577250));
      drawBagText(var1, var2, String.valueOf(var3.session.inventory.currency.badges), 191, 81, 23, new Color(15911010));
      int var7 = this.portableShopItemCount();
      int var8 = this.visibleListStart(var7);

      for(int var9 = 0; var9 < 5; ++var9) {
         int var10 = var8 + var9;
         int var11 = 99 + var9 * 21;
         boolean var12 = var10 < var7 && var10 == this.selected;
         if (var12) {
            var1.setColor(new Color(466733));
            var1.fillRoundRect(23, var11 + 2, 196, 19, 6, 6);
         }

         var1.setColor(new Color(var12 ? darkerBagColor(var5) : 15793144));
         var1.fillRoundRect(20, var11, 196, 19, 6, 6);
         var1.setColor(new Color(var12 ? 15911010 : 7711401));
         var1.drawRoundRect(20, var11, 195, 18, 6, 6);
         var1.setColor(new Color(var12 ? 15911010 : var5));
         var1.fillRoundRect(20, var11, 5, 19, 5, 5);
         if (var10 < var7) {
            ItemDefinition var13 = this.portableShopSourceItem(var10);
            if (var13 != null) {
               byte var14 = 29;
               int var15 = var11 + 3;
               if (!UnifiedItemIconRenderer.draw(var1, var13.iconResource, var14, var15, 13, 13)) {
                  drawCellTopLeft(var4, var1, var13.iconCell, var14, var15);
               }

               drawBagMarquee(var1, var2, var13.name, 48, var11 + 5, 102, var12 ? Color.WHITE : new Color(1527657), this.openedTicks);
               drawPanelShopPricePill(var1, var2, this.portableShopPrice(var10), this.portableShopCurrency(var10), 157, var11 + 3, 51, 14, var12);
            }
         }
      }

      ItemDefinition var16 = this.portableShopSourceItem(this.selected);
      String var17 = var16 == null ? "Chưa có món hàng trong quầy này." : var16.description;
      var1.setColor(new Color(1523542));
      var1.fillRoundRect(20, 205, 200, 46, 7, 7);
      var1.setColor(new Color(15911010));
      var1.drawRoundRect(20, 205, 199, 45, 7, 7);
      String var18 = var16 == null ? "Không có hàng" : var16.name;
      drawBagMarquee(var1, var2, var18, 28, 210, 125, new Color(15911010), this.openedTicks);
      if (var16 != null) {
         drawBagPill(var1, var2, "Có " + this.portableShopCurrentCount(var3, this.selected), 165, 208, 47, 15, 3054715, 16777215);
      }

      drawBagWrappedFixed(var1, var2, var17, 28, 223, 184, 2, Color.WHITE);
      drawModernPanelShopScrollbar(var1, var7, var8);
      drawBagButton(var1, var2, "Mua", 25, 264, 76, 19, true, var5);
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, false, var5);
   }

   private static void drawPanelShopPricePill(Graphics2D var0, UiFont var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      var0.setColor(new Color(var8 ? 15911010 : 1523542));
      var0.fillRoundRect(var4, var5, var6, var7, 6, 6);
      var0.setColor(new Color(var8 ? 16777215 : 6924964));
      var0.drawRoundRect(var4, var5, var6 - 1, var7 - 1, 6, 6);
      VqsvShopCurrencyRenderer.draw(var0, var1, var2, var3, var4 + 2, var5, var6 - 4, var7, var8 ? new Color(6047770) : Color.WHITE);
   }

   private static void drawModernPanelShopScrollbar(Graphics2D var0, int var1, int var2) {
      if (var1 > 5) {
         var0.setColor(new Color(2383207));
         var0.fillRoundRect(218, 99, 4, 103, 4, 4);
         int var3 = UiScrollbarMath.thumbHeight(103, var1, 5, 9);
         int var4 = UiScrollbarMath.thumbY(99, 103, var3, var1, 5, var2);
         var0.setColor(new Color(15911010));
         var0.fillRoundRect(217, var4, 6, var3, 4, 4);
      }
   }

   private void renderPortableShopConfirm(Graphics2D var1, UiFont var2) {
      VqsvBattleRenderer.drawShopConfirmOverlay(var1, var2, this.shopConfirmQuantity, this.shopConfirmTotal, this.shopConfirmCurrency, this.openedTicks);
   }

   private void renderSourceWorldShopSell(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("shopbuy.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      SpriteAnimator var6 = SpriteAnimator.load(258);
      drawCell(var4, var5, var1, 4);
      fillBand(var1, var4, 1, 12511218, 160);
      fillBand(var1, var4, 2, 5361769, 10);
      fillBand(var1, var4, 3, 13037823, 8);
      fillBand(var1, var4, 8, 5363945, 91);
      drawTextWide(var1, var2, var4, 5, "Bán ra", 0, var4.w(5, 100), color(var4.widget(5), 16777215));
      drawTextWide(var1, var2, var4, 9, "Vật phẩm", 0, 48, color(var4.widget(9), 1862801));
      drawTextWide(var1, var2, var4, 10, "Giá bán", -6, 52, color(var4.widget(10), 1862801));
      List var7 = sourceWorldShopSellRows(var3);
      int var8 = var7.size();
      int var9 = this.visibleListStart(var8);

      for(int var10 = 0; var10 < SHOPBUY_ROW_BACKGROUNDS.length; ++var10) {
         int var11 = var9 + var10;
         VqsvUiLayout.UiWidget var12 = var4.widget(SHOPBUY_ROW_BACKGROUNDS[var10]);
         if (var12 != null) {
            int var13 = var11 == this.selected ? var12.altId : var12.imageId;
            drawCellTopLeft(var5, var1, var13, var12.x, var12.y);
         }

         if (var11 < var8) {
            SourceSellRow var17 = (SourceSellRow)var7.get(var11);
            drawCellTopLeft(var6, var1, var17.item.iconCell, var4.x(SHOPBUY_ROW_ICONS[var10], 56), var4.y(SHOPBUY_ROW_ICONS[var10], 100 + var10 * 18));
            int var14 = var11 == this.selected ? 16753920 : color(var4.widget(SHOPBUY_ROW_NAMES[var10]), 1862801);
            drawTextMarquee(var1, var2, var4, SHOPBUY_ROW_NAMES[var10], var17.item.name, var4.w(SHOPBUY_ROW_NAMES[var10], 48), var14, this.openedTicks);
            drawTextWide(var1, var2, var4, SHOPBUY_ROW_PRICES[var10], String.valueOf(var17.unitPrice), 0, var4.w(SHOPBUY_ROW_PRICES[var10], 36), var14);
            drawCellTopLeft(var5, var1, shopCurrencyCell(var17.currency), var4.x(SHOPBUY_ROW_CURRENCIES[var10], 170), var4.y(SHOPBUY_ROW_CURRENCIES[var10], 101 + var10 * 18));
         }
      }

      SourceSellRow var15 = var7.isEmpty() ? null : (SourceSellRow)var7.get(clamp(this.selected, 0, var7.size() - 1));
      String var16 = var15 == null ? "" : var15.item.description;
      drawWrappedTextBoxScrolled(var1, var2, var4, 56, var16, var4.w(56, 125), 44, color(var4.widget(56), 1862801), this.openedTicks);
      drawCell(var4, var5, var1, 41);
      drawCell(var4, var5, var1, 42);
      drawText(var1, var2, var4, 43, String.valueOf(var3.session.inventory.currency.badges), color(var4.widget(43), 1862801));
      drawText(var1, var2, var4, 44, String.valueOf(var3.session.inventory.currency.money), color(var4.widget(44), 1862801));
      drawShopbuyScrollbar(var1, var4, var8, var9);
      drawSoftkey(var1, var2, var4, var5, 57, var4.text(57, "Bán đi"), 16777215);
      drawSoftkey(var1, var2, var4, var5, 58, var4.text(58, "Quay lại"), 16777215);
   }

   private void renderSourceWorldShop(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("shop.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      this.drawMenuFrame(var1, var3, var4);
      drawTextWide(var1, var2, var3, 5, "Thương điếm", -8, var3.w(5, 120), color(var3.widget(5), 13631758));
      this.drawRows(var1, var2, var3, var4, SOURCE_WORLD_SHOP_ROW_WIDGETS, SOURCE_WORLD_SHOP_LABELS);
      drawSoftkey(var1, var2, var3, var4, 10, var3.text(10, "Xác định"), 16777215);
      drawSoftkey(var1, var2, var3, var4, 11, var3.text(11, "Quay lại"), 16777215);
   }

   private void renderSourceWorldShopRecoverConfirm(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("msgRecover.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      drawCell(var4, var5, var1, 3);
      drawTextWide(var1, var2, var4, 4, "Có khôi phục trạng thái ba lô sủng vật không?", 0, var4.w(4, 132), color(var4.widget(4), 1862801));
      drawTextWide(var1, var2, var4, 5, "Cần tiền tài: ", 0, var4.w(5, 60), color(var4.widget(5), 1862801));
      drawTextWide(var1, var2, var4, 6, String.valueOf(Math.max(0, this.sourceRecoveryCost)), 0, var4.w(6, 40), color(var4.widget(6), 15673492));
      drawCell(var4, var5, var1, 7);
      drawTextWide(var1, var2, var4, 8, String.valueOf(var3.session.inventory.currency.money), 0, var4.w(8, 48), color(var4.widget(8), 15673492));
      drawSoftkey(var1, var2, var4, var5, 1, var4.text(1, "Xác định"), 16777215);
      drawSoftkey(var1, var2, var4, var5, 2, var4.text(2, "Quay lại"), 16777215);
   }

   private void renderSourcePetBank(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("shop.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      this.drawMenuFrame(var1, var3, var4);
      drawTextWide(var1, var2, var3, 5, "Ngân hàng Sủng vật", -8, var3.w(5, 120), color(var3.widget(5), 13631758));
      this.drawRows(var1, var2, var3, var4, SOURCE_PET_BANK_ROW_WIDGETS, SOURCE_PET_BANK_LABELS);
      drawSoftkey(var1, var2, var3, var4, 10, var3.text(10, "Xác định"), 16777215);
      drawSoftkey(var1, var2, var3, var4, 11, var3.text(11, "Quay lại"), 16777215);
   }

   private void renderTask(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("task.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      this.drawTaskFrame(var1, var4, var5);
      drawTextWide(var1, var2, var4, 2, var4.text(2, "Nhiem vu"), -28, 80, color(var4.widget(2), 13631758));
      drawCellState(var4, var5, var1, 6, this.taskTab == 0);
      drawCellState(var4, var5, var1, 7, this.taskTab == 1);
      drawTextWide(var1, var2, var4, 8, var4.text(8, "Nhiem vu chinh"), -2, 64, this.taskTab == 0 ? 11290624 : color(var4.widget(8), 154));
      drawTextWide(var1, var2, var4, 9, var4.text(9, "Nhiem vu phu"), -2, 64, this.taskTab == 1 ? 11290624 : color(var4.widget(9), 154));
      drawSoftkey(var1, var2, var4, var5, 41, var4.text(41, "Xac dinh"), 16777215);
      drawSoftkey(var1, var2, var4, var5, 42, var4.text(42, "Quay lai"), 16777215);
      List var6 = taskRowsForRender(var3, this.taskTab);
      this.selected = clamp(this.selected, 0, Math.max(0, var6.size() - 1));
      int var7 = this.visibleListStart(var6.size());

      for(int var8 = 0; var8 < TASK_ROW_BACKGROUNDS.length; ++var8) {
         int var9 = var7 + var8;
         VqsvUiLayout.UiWidget var10 = var4.widget(TASK_ROW_BACKGROUNDS[var8]);
         if (var10 != null) {
            int var11 = var9 == this.selected ? var10.altId : var10.imageId;
            drawCellTopLeft(var5, var1, var11, var10.x, var10.y);
         }

         if (var9 < var6.size()) {
            TaskRow var17 = (TaskRow)var6.get(var9);
            int var12 = var9 == this.selected ? activeTextColor(var4.widget(TASK_ROW_NAMES[var8]), 15673804) : color(var4.widget(TASK_ROW_NAMES[var8]), 1862801);
            drawText(var1, var2, var4, TASK_ROW_NUMBERS[var8], String.valueOf(var17.number), var9 == this.selected ? activeTextColor(var4.widget(TASK_ROW_NUMBERS[var8]), 15673804) : color(var4.widget(TASK_ROW_NUMBERS[var8]), 1862801));
            VqsvUiLayout.UiWidget var13 = var4.widget(TASK_ROW_NAMES[var8]);
            VqsvUiLayout.UiWidget var14 = var4.widget(TASK_ROW_STATUS[var8]);
            int var15 = var13 != null && var14 != null ? Math.max(1, var14.x - var13.x - 10) : var4.w(TASK_ROW_NAMES[var8], 72);
            drawTextMarquee(var1, var2, var4, TASK_ROW_NAMES[var8], var17.title, var15, var12, var9 == this.selected ? this.openedTicks : 0);
            drawTextWide(var1, var2, var4, TASK_ROW_STATUS[var8], var17.completed ? "Hoàn thành" : "", 0, var4.w(TASK_ROW_STATUS[var8], 24), var9 == this.selected ? activeTextColor(var4.widget(TASK_ROW_STATUS[var8]), 15673804) : color(var4.widget(TASK_ROW_STATUS[var8]), 1862801));
         }
      }

      String var16 = "";
      if (!var6.isEmpty()) {
         var16 = ((TaskRow)var6.get(clamp(this.selected, 0, var6.size() - 1))).detail;
      }

      drawWrappedTextBoxScrolled(var1, var2, var4, 36, var16, var4.w(36, 128), 30, activeTextColor(var4.widget(36), 0), this.openedTicks);
      drawText(var1, var2, var4, 37, this.taskTab == 0 ? "Đầu mối chính hoàn thành độ: " : "Chi nhánh hoàn thành độ: ", color(var4.widget(37), 1862801));
      drawText(var1, var2, var4, 38, taskProgressText(var3, this.taskTab), color(var4.widget(38), 16777215));
      drawTaskScrollbar(var1, var4, var6.size(), var7);
   }

   private void renderTaskMap(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      TaskRouteHint var4 = this.taskRouteHint(var3);
      SpriteAnimator var5 = SpriteAnimator.load(257);
      var1.setColor(new Color(472902));
      var1.fillRect(0, 0, 240, 320);
      drawTaskMapBackdrop(var1);
      drawCellTopLeft(var5, var1, 113, 14, 7);
      drawTaskMapLine(var1, var2, "BẢN ĐỒ NHIỆM VỤ", 62, 11, 16777215);
      drawTaskMapMarquee(var1, var2, this.taskMapTitle, 62, 27, 158, 16772981, this.openedTicks);
      drawTaskMapLocationCard(var1, var2, var5, 12, 52, 104, 38, "BẠN ĐANG Ở", var4.currentLabel, 18, 1473729);
      drawTaskMapLocationCard(var1, var2, var5, 124, 52, 104, 38, "ĐIỂM ĐẾN", var4.targetLabel, 21, 12947737);
      drawTaskRouteBlocks(var1, var2, var5, var4, this.openedTicks);
      var1.setColor(new Color(743016));
      var1.fillRoundRect(10, 228, 220, 43, 8, 8);
      var1.setColor(new Color(6671058));
      var1.drawRoundRect(10, 228, 220, 43, 8, 8);
      drawCellTopLeft(var5, var1, var4.currentIndex == var4.targetIndex ? 75 : 22, 17, 238);
      drawTaskMapLine(var1, var2, var4.currentIndex == var4.targetIndex ? "ĐÃ ĐẾN NƠI" : "BƯỚC TIẾP THEO", 43, 234, 10482929);
      drawTaskMapTwoLineLabel(var1, var2, var4.nextLabel, 43, 248, 174, 16777215);
      var1.setColor(new Color(539467));
      var1.fillRoundRect(74, 278, 92, 25, 8, 8);
      var1.setColor(new Color(7462115));
      var1.drawRoundRect(74, 278, 92, 25, 8, 8);
      drawCellTopLeft(var5, var1, 87, 87, 287);
      drawTaskMapCentered(var1, var2, "Quay lại", 74, 285, 92, 16777215);
   }

   private static void drawTaskMapBackdrop(Graphics2D var0) {
      var0.setColor(new Color(204331));
      var0.fillRoundRect(5, 4, 230, 304, 13, 13);
      var0.setColor(new Color(881024));
      var0.fillRoundRect(7, 6, 226, 300, 11, 11);
      var0.setColor(new Color(6017239));
      var0.drawRoundRect(7, 6, 226, 300, 11, 11);
      var0.setColor(new Color(611173));
      var0.fillRoundRect(9, 8, 222, 41, 9, 9);
      var0.setColor(new Color(11069926));
      var0.drawLine(12, 48, 228, 48);
      var0.setColor(new Color(12049865));
      var0.fillRoundRect(9, 93, 222, 132, 9, 9);
      var0.setColor(new Color(9619901));
      var0.fillOval(18, 102, 76, 47);
      var0.fillOval(72, 121, 96, 55);
      var0.fillOval(143, 101, 76, 51);
      var0.fillOval(33, 168, 91, 48);
      var0.fillOval(126, 167, 91, 48);
      var0.setColor(new Color(7911605));

      for(int var1 = 104; var1 <= 214; var1 += 18) {
         var0.drawArc(13, var1, 30, 8, 15, 150);
         var0.drawArc(196, var1 + 5, 25, 7, 15, 150);
      }

      var0.setColor(new Color(15135960));
      var0.drawRoundRect(9, 93, 222, 132, 9, 9);
   }

   private static void drawTaskMapLocationCard(Graphics2D var0, UiFont var1, SpriteAnimator var2, int var3, int var4, int var5, int var6, String var7, String var8, int var9, int var10) {
      var0.setColor(new Color(405054));
      var0.fillRoundRect(var3 + 2, var4 + 2, var5, var6, 7, 7);
      var0.setColor(new Color(15268847));
      var0.fillRoundRect(var3, var4, var5, var6, 7, 7);
      var0.setColor(new Color(var10));
      var0.fillRoundRect(var3, var4, var5, 13, 7, 7);
      var0.fillRect(var3, var4 + 7, var5, 6);
      drawCellTopLeft(var2, var0, var9, var3 + 5, var4 + 2);
      drawTaskMapLine(var0, var1, var7, var3 + 20, var4 + 2, 16777215);
      drawTaskMapTwoLineLabel(var0, var1, var8, var3 + 5, var4 + 16, var5 - 10, 1527129);
   }

   private static void drawTaskRouteBlocks(Graphics2D var0, UiFont var1, SpriteAnimator var2, TaskRouteHint var3, int var4) {
      Stroke var5 = var0.getStroke();
      var0.setStroke(new BasicStroke(3.0F, 1, 1));
      var0.setColor(new Color(5672841));

      for(int[] var9 : TASK_ROUTE_LINKS) {
         drawTaskRouteLink(var0, var9[0], var9[1]);
      }

      var0.setStroke(new BasicStroke(5.0F, 1, 1));
      var0.setColor(new Color(1464941));

      for(int var19 = 0; var19 < var3.path.length - 1; ++var19) {
         drawTaskRouteLink(var0, var3.path[var19], var3.path[var19 + 1]);
      }

      var0.setStroke(new BasicStroke(2.0F, 1, 1));
      var0.setColor(new Color(16770936));

      for(int var20 = 0; var20 < var3.path.length - 1; ++var20) {
         drawTaskRouteLink(var0, var3.path[var20], var3.path[var20 + 1]);
      }

      var0.setStroke(var5);

      for(int var21 = 0; var21 < TASK_ROUTE_NODES.length; ++var21) {
         TaskMapNode var22 = TASK_ROUTE_NODES[var21];
         boolean var23 = var21 == var3.currentIndex;
         boolean var24 = var21 == var3.targetIndex;
         boolean var10 = var21 == var3.nextIndex;
         boolean var11 = indexIn(var3.path, var21) >= 0;
         int var12 = var23 ? 1342914 : (var24 ? 12618774 : (var10 ? 1475685 : (var11 ? 3571591 : 5602681)));
         int var13 = var23 ? 16777215 : (var24 ? 16772981 : (var10 ? 10747856 : (var11 ? 13957087 : 10406594)));
         var0.setColor(new Color(1526864));
         var0.fillRoundRect(var22.x + 2, var22.y + 3, 40, 22, 8, 8);
         var0.setColor(new Color(var12));
         var0.fillRoundRect(var22.x, var22.y, 40, 22, 8, 8);
         var0.setColor(new Color(var13));
         var0.drawRoundRect(var22.x, var22.y, 40, 22, 8, 8);
         if ((var23 || var24 || var10) && var4 / 8 % 2 == 0) {
            var0.drawRoundRect(var22.x - 2, var22.y - 2, 44, 26, 10, 10);
         }

         int var14 = var22.y + 5;

         for(String var18 : var22.lines) {
            drawTaskMapCentered(var0, var1, var18, var22.x + 2, var14, 36, !var23 && !var24 && !var10 ? 15202280 : 16777215);
            var14 += 11;
         }

         if (var23) {
            drawCellTopLeft(var2, var0, 18, var22.x + 40 - 7, var22.y - 5);
         } else if (var24) {
            drawCellTopLeft(var2, var0, 21, var22.x + 40 - 6, var22.y - 4);
         } else if (var10) {
            drawCellTopLeft(var2, var0, 22, var22.x + 40 - 11, var22.y - 2);
         }
      }

   }

   private static void drawTaskRouteLink(Graphics2D var0, int var1, int var2) {
      TaskMapNode var3 = TASK_ROUTE_NODES[var1];
      TaskMapNode var4 = TASK_ROUTE_NODES[var2];
      var0.drawLine(var3.x + 20, var3.y + 11, var4.x + 20, var4.y + 11);
   }

   private TaskRouteHint taskRouteHint(VqsvGameRuntime.Scene var1) {
      int var2 = taskRouteNodeIndex(var1.session.world.currentSceneId, var1.session.world.currentRoomIndex);
      int var3 = this.taskTargetNodeIndex();
      int[] var4 = routePath(var2, var3);
      int var5 = nextRouteNode(var4, var2, var3);
      String var6 = var2 == 20 ? sceneRoomRouteLabel(var1.session.world.currentSceneId, var1.session.world.currentRoomIndex) : TASK_ROUTE_NODES[var2].label;
      String var7 = var3 == 20 ? this.taskTargetLabelFallback() : TASK_ROUTE_NODES[var3].label;
      String var8;
      if (var2 == var3) {
         var8 = "Đang ở đúng khu vực";
      } else if (var2 != 20 && var3 != 20) {
         var8 = TASK_ROUTE_NODES[var5].label;
      } else {
         var8 = "Chưa định tuyến phòng này";
      }

      return new TaskRouteHint(var2, var3, var5, var4, var6, var7, var8);
   }

   private int taskTargetNodeIndex() {
      String var1 = (this.taskMapTitle + " " + this.taskMapDetail).toLowerCase();
      if (this.taskMapReturnTab == 0) {
         int var2 = mainTaskTargetNodeIndex(this.taskMapNumber);
         if (var2 != 20) {
            return var2;
         }
      }

      if (!var1.contains("bunny") && !var1.contains("thỏ di lặc") && !var1.contains("đường nhỏ phía đông")) {
         if (var1.contains("đường nhỏ phía nam")) {
            return 2;
         } else if (var1.contains("hắc long thần")) {
            return 19;
         } else if (!var1.contains("phượng hoàng") && !var1.contains("truy ức chi sâm")) {
            if (!var1.contains("địa lao hắc thạch") && !var1.contains("hắc tinh thạch")) {
               if (var1.contains("hắc thạch đạo quán")) {
                  return 15;
               } else if (var1.contains("hắc thạch")) {
                  return 14;
               } else if (!var1.contains("mỏ quặng") && !var1.contains("quặng mỏ")) {
                  if (!var1.contains("niêm thổ đạo quán") && !var1.contains("niêm thổ đạo quán")) {
                     if (var1.contains("phi không đĩnh")) {
                        return 12;
                     } else if (var1.contains("khu chợ phía nam")) {
                        return 11;
                     } else if (!var1.contains("trung tâm niêm thổ") && !var1.contains("niêm thổ thành")) {
                        if (var1.contains("bến tàu phía đông")) {
                           return 9;
                        } else if (var1.contains("nguyên mộc đạo quán")) {
                           return 8;
                        } else if (var1.contains("nguyên mộc")) {
                           return 7;
                        } else if (var1.contains("bích thủy đạo quán")) {
                           return 6;
                        } else if (var1.contains("bích thủy")) {
                           return 5;
                        } else if (var1.contains("bến tàu")) {
                           return 4;
                        } else {
                           return var1.contains("thủy mộc") ? 0 : 20;
                        }
                     } else {
                        return 10;
                     }
                  } else {
                     return 13;
                  }
               } else {
                  return 17;
               }
            } else {
               return 16;
            }
         } else {
            return 18;
         }
      } else {
         return 1;
      }
   }

   private static int taskRouteNodeIndex(int var0, int var1) {
      if (var0 == 1) {
         switch (var1) {
            case 0:
               return 0;
            case 1:
               return 1;
            case 2:
               return 2;
            case 3:
               return 3;
            case 4:
            case 5:
            case 6:
               return 4;
            default:
               return 20;
         }
      } else if (var0 == 2) {
         return var1 >= 0 && var1 <= 7 ? 5 : 20;
      } else if (var0 == 3 && var1 >= 0 && var1 <= 7) {
         return 7;
      } else if (var0 == 4) {
         switch (var1) {
            case 0:
            case 1:
               return 9;
            case 2:
            case 3:
            case 4:
               return 17;
            case 5:
               return 10;
            case 6:
            case 7:
               return 11;
            case 8:
               return 12;
            case 9:
            case 10:
            case 11:
            case 12:
               return 14;
            default:
               return 20;
         }
      } else if (var0 == 5) {
         switch (var1) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
               return 14;
            case 4:
               return 16;
            default:
               return 20;
         }
      } else if (var0 == 6) {
         return var1 != 0 && var1 != 1 ? 20 : 19;
      } else if (var0 != 7 && var0 != 8) {
         if (var0 == 9) {
            switch (var1) {
               case 0:
                  return 6;
               case 1:
                  return 8;
               case 2:
               case 3:
                  return 13;
               case 4:
               case 5:
                  return 15;
               case 6:
                  return 19;
               case 7:
                  return 20;
               default:
                  return 20;
            }
         } else if (var0 != 10) {
            if (var0 == 11) {
               switch (var1) {
                  case 0:
                  case 1:
                  case 12:
                  case 13:
                  case 16:
                     return 7;
                  case 2:
                  case 15:
                  default:
                     return 20;
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 28:
                     return 18;
                  case 8:
                  case 9:
                  case 10:
                  case 11:
                  case 14:
                     return 5;
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  case 21:
                  case 22:
                     return 10;
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 27:
                     return 14;
               }
            } else {
               for(int var2 = 0; var2 < TASK_ROUTE_NODES.length; ++var2) {
                  if (TASK_ROUTE_NODES[var2].sceneId == var0 && TASK_ROUTE_NODES[var2].roomIndex == var1) {
                     return var2;
                  }
               }

               return 20;
            }
         } else {
            return var1 != 13 && var1 != 14 ? 20 : 18;
         }
      } else {
         return 20;
      }
   }

   private static int mainTaskTargetNodeIndex(int var0) {
      switch (var0) {
         case 1:
            return 1;
         case 2:
            return 0;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 10:
            return 5;
         case 8:
         case 9:
            return 6;
         case 11:
            return 5;
         case 12:
            return 3;
         case 13:
         case 17:
            return 8;
         case 14:
         case 15:
         case 16:
         case 18:
            return 7;
         case 19:
            return 9;
         case 20:
         case 24:
            return 10;
         case 21:
            return 11;
         case 22:
            return 13;
         case 23:
         case 33:
            return 12;
         case 25:
         case 27:
            return 14;
         case 26:
         case 28:
            return 15;
         case 29:
            return 16;
         case 30:
         case 31:
            return 18;
         case 32:
            return 19;
         case 34:
         case 37:
         case 40:
         case 42:
            return 18;
         case 35:
         case 36:
         case 38:
         case 39:
         case 41:
         case 43:
         case 44:
         case 45:
         case 46:
         default:
            return 20;
         case 47:
         case 48:
            return 17;
      }
   }

   private String taskTargetLabelFallback() {
      Matcher var1 = Pattern.compile("\\[([^\\]]+)\\]").matcher(this.taskMapDetail);
      if (var1.find()) {
         return stripTaskMarkup(var1.group(1));
      } else {
         String var2 = stripTaskMarkup(this.taskMapDetail);
         return var2.isEmpty() ? "Chưa định tuyến" : var2;
      }
   }

   private static String stripTaskMarkup(String var0) {
      return var0 == null ? "" : var0.replace("#0", "").replace("#1", "").replace("#2", "").replace("[", "").replace("]", "").trim();
   }

   private static String sceneRoomRouteLabel(int var0, int var1) {
      return "Scene " + var0 + " phòng " + var1;
   }

   private static int[] routePath(int var0, int var1) {
      if (var0 != 20 && var1 != 20) {
         int var2 = TASK_ROUTE_NODES.length;
         int[] var3 = new int[var2];
         boolean[] var4 = new boolean[var2];
         int[] var5 = new int[var2];

         for(int var6 = 0; var6 < var2; ++var6) {
            var3[var6] = -1;
         }

         int var14 = 0;
         int var7 = 0;
         var5[var7++] = var0;
         var4[var0] = true;

         while(var14 < var7) {
            int var8 = var5[var14++];
            if (var8 == var1) {
               break;
            }

            for(int[] var12 : TASK_ROUTE_LINKS) {
               int var13 = -1;
               if (var12[0] == var8) {
                  var13 = var12[1];
               } else if (var12[1] == var8) {
                  var13 = var12[0];
               }

               if (var13 >= 0 && !var4[var13]) {
                  var4[var13] = true;
                  var3[var13] = var8;
                  var5[var7++] = var13;
               }
            }
         }

         if (!var4[var1]) {
            return new int[]{var0, var1};
         } else {
            int var16 = 1;

            for(int var17 = var1; var17 != var0; var17 = var3[var17]) {
               ++var16;
            }

            int[] var18 = new int[var16];
            int var19 = var1;

            for(int var20 = var16 - 1; var20 >= 0; --var20) {
               var18[var20] = var19;
               var19 = var3[var19];
            }

            return var18;
         }
      } else {
         return new int[]{var0};
      }
   }

   private static int nextRouteNode(int[] var0, int var1, int var2) {
      return var1 != var2 && var0.length > 1 && var2 != 20 ? var0[1] : var1;
   }

   private static int indexIn(int[] var0, int var1) {
      for(int var2 = 0; var2 < var0.length; ++var2) {
         if (var0[var2] == var1) {
            return var2;
         }
      }

      return -1;
   }

   private void tickSourcePetBank(VqsvGameRuntime.Scene var1) {
      int var2 = this.selected;
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, SOURCE_PET_BANK_LABELS.length - 1);
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, SOURCE_PET_BANK_LABELS.length - 1);
      } else {
         if (var1.keyBack) {
            this.close(var1);
            var1.session.story.trace().add("PORTED/PARTIAL panel game.k.E shop.ui back source pet bank -> world");
            return;
         }

         if (var1.key0) {
            if (this.selected == 0) {
               this.visible = false;
               var1.openSourcePetBankDepositPetstate();
               var1.session.story.trace().add("PORTED/PARTIAL panel game.k.E shop.ui confirm b=0 c=0 o.a(7) close shop.ui -> petstate.ui action=Gởi lại");
               return;
            }

            this.visible = false;
            if (this.selected == 1) {
               var1.openSourcePetBankWithdrawPetstate();
            } else {
               var1.openSourcePetBankReleasePetstate();
            }

            var1.session.story.trace().add("PORTED/PARTIAL panel game.k.E shop.ui confirm b=" + this.selected + " label=" + SOURCE_PET_BANK_LABELS[this.selected] + " o.a(15) close shop.ui -> bank petstate.ui");
            return;
         }
      }

      if (this.selected != var2) {
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.k.E shop.ui key selected=" + this.selected + " label=" + SOURCE_PET_BANK_LABELS[this.selected]);
      }

   }

   private void tickSourceWorldShop(VqsvGameRuntime.Scene var1) {
      int var2 = this.selected;
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, SOURCE_WORLD_SHOP_LABELS.length - 1);
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, SOURCE_WORLD_SHOP_LABELS.length - 1);
      } else {
         if (var1.keyBack || var1.key0 && this.selected == 3) {
            this.close(var1);
            this.portableShopReturnToSourceWorldShop = false;
            var1.session.story.trace().add("PORTED source game.k.G shop.ui exit selected=" + this.selected + " -> world state0");
            return;
         }

         if (var1.key0) {
            if (this.selected == 0) {
               this.openPortableShopBuy(var1, this.shopTable, this.shopBucket, "PORTED source game.k.G shop.ui buy selected=0 route=[" + this.shopTable + "," + this.shopBucket + "] -> game.l state2 shopbuy.ui");
               this.portableShopReturnToSourceWorldShop = true;
               return;
            }

            if (this.selected == 1) {
               this.openSourceWorldShopSell(var1);
               return;
            }

            if (this.selected == 2) {
               this.openSourceWorldShopRecover(var1);
               return;
            }
         }
      }

      if (this.selected != var2) {
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.k.G shop.ui key selected=" + this.selected + " label=" + SOURCE_WORLD_SHOP_LABELS[this.selected]);
      }

   }

   private void openSourceWorldShopSell(VqsvGameRuntime.Scene var1) {
      this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL;
      this.selected = 0;
      this.listScroll = 0;
      this.openedTicks = 0;
      this.sourceSellItemId = -1;
      this.sourceSellQuantity = 1;
      this.sourceSellTotal = 0;
      this.sourceSellCurrency = 0;
      this.updateSourceSellSelectedLabel(var1);
      var1.session.story.trace().add("PORTED source game.k.G shop.ui sell selected=1 -> state3 game.k.O shopbuy.ui rows=" + sourceWorldShopSellRows(var1).size());
   }

   private void tickSourceWorldShopSell(VqsvGameRuntime.Scene var1) {
      List var2 = sourceWorldShopSellRows(var1);
      int var3 = var2.size();
      this.selected = clamp(this.selected, 0, Math.max(0, var3 - 1));
      int var4 = this.selected;
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, Math.max(0, var3 - 1));
         this.keepSelectedVisible(var3);
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, Math.max(0, var3 - 1));
         this.keepSelectedVisible(var3);
      } else {
         if (var1.keyBack) {
            this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP;
            this.selected = 1;
            this.listScroll = 0;
            this.openedTicks = 0;
            this.sourceSellSelectedLabel = "Bán ra";
            var1.session.story.trace().add("PORTED source game.k.P shopbuy.ui back -> state1 game.k.F shop.ui selected=1");
            return;
         }

         if (var1.key0 && !var2.isEmpty()) {
            this.openSourceWorldShopSellConfirm(var1, (SourceSellRow)var2.get(this.selected));
            return;
         }
      }

      this.updateSourceSellSelectedLabel(var1);
      if (this.selected != var4) {
         this.openedTicks = 0;
         SourceSellRow var5 = (SourceSellRow)var2.get(this.selected);
         var1.session.story.trace().add("PORTED source game.k.P shopbuy.ui key selected=" + this.selected + " item=" + var5.item.id + " count=" + var5.count + " unitPrice=" + var5.unitPrice);
      }

   }

   private void openSourceWorldShopSellConfirm(VqsvGameRuntime.Scene var1, SourceSellRow var2) {
      WorldItemShopService.Quote var3 = WORLD_ITEM_SHOP_SERVICE.quote(var2 != null, var2 == null ? -1 : var2.item.id, 1, var2 == null ? 0 : var2.sourcePrice, var2 == null ? 0 : var2.count);
      if (var3.outcome == WorldItemShopService.QuoteOutcome.READY) {
         this.sourceSellItemId = var3.itemId;
         this.sourceSellQuantity = var3.quantity;
         this.sourceSellTotal = var3.total;
         this.sourceSellCurrency = var2.currency;
         this.sourceSellSelectedLabel = var2.item.name;
         this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.k.P sell open msgyn.ui item=" + this.sourceSellItemId + " qty=" + this.sourceSellQuantity + " unitPrice=" + var3.unitPrice + " total=" + this.sourceSellTotal);
      }
   }

   private void tickSourceWorldShopSellConfirm(VqsvGameRuntime.Scene var1) {
      SourceSellRow var2 = sourceWorldShopSellRowById(var1, this.sourceSellItemId);
      if (var2 == null) {
         this.closeSourceWorldShopSellConfirm(var1, "missing row");
      } else if (var1.keyLeft) {
         --this.sourceSellQuantity;
         if (this.sourceSellQuantity <= 0) {
            this.sourceSellQuantity = var2.count;
         }

         this.syncSourceWorldShopSellConfirm(var2);
         var1.session.story.trace().add("PORTED source game.k.P sell msgyn.ui key=16400 item=" + this.sourceSellItemId + " qty=" + this.sourceSellQuantity + " total=" + this.sourceSellTotal);
      } else if (var1.keyRight) {
         ++this.sourceSellQuantity;
         if (this.sourceSellQuantity > var2.count) {
            this.sourceSellQuantity = 1;
         }

         this.syncSourceWorldShopSellConfirm(var2);
         var1.session.story.trace().add("PORTED source game.k.P sell msgyn.ui key=32832 item=" + this.sourceSellItemId + " qty=" + this.sourceSellQuantity + " total=" + this.sourceSellTotal);
      } else if (var1.keyBack) {
         this.closeSourceWorldShopSellConfirm(var1, "back");
      } else {
         if (var1.key0) {
            this.commitSourceWorldShopSell(var1, var2);
         }

      }
   }

   private void syncSourceWorldShopSellConfirm(SourceSellRow var1) {
      WorldItemShopService.Quote var2 = WORLD_ITEM_SHOP_SERVICE.quote(true, var1.item.id, this.sourceSellQuantity, var1.sourcePrice, var1.count);
      this.sourceSellQuantity = var2.quantity;
      this.sourceSellTotal = var2.total;
   }

   private void commitSourceWorldShopSell(VqsvGameRuntime.Scene var1, SourceSellRow var2) {
      int var3 = this.sourceSellItemId;
      int var4 = this.sourceSellQuantity;
      WorldItemShopService.SellResult var5 = WORLD_ITEM_SHOP_SERVICE.sell(var2 != null, var3, var4, var2 == null ? 0 : var2.sourcePrice, VqsvSourceOps.sourceItemTableMaterialInventoryId(var3), var1.session.inventory.bagItems, var1.session.inventory.materialItems, var1.session.inventory.currency);
      if (var5.outcome != WorldItemShopService.SellOutcome.SOLD) {
         this.closeSourceWorldShopSellConfirm(var1, "invalid commit");
      } else {
         var1.session.story.trace().add("PORTED source game.k.P sell commit item=" + var3 + " qty=" + var5.quote.quantity + " sourcePrice=" + var2.sourcePrice + " unitPrice=" + var5.quote.unitPrice + " total=" + var5.quote.total + " count=" + var5.quote.currentCount + "->" + var5.countAfter + " money=" + var5.moneyBefore + "->" + var5.moneyAfter);
         this.closeSourceWorldShopSellConfirm(var1, "sold");
      }
   }

   private void closeSourceWorldShopSellConfirm(VqsvGameRuntime.Scene var1, String var2) {
      this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL;
      this.sourceSellItemId = -1;
      this.sourceSellQuantity = 1;
      this.sourceSellTotal = 0;
      this.sourceSellCurrency = 0;
      List var3 = sourceWorldShopSellRows(var1);
      this.selected = clamp(this.selected, 0, Math.max(0, var3.size() - 1));
      this.keepSelectedVisible(var3.size());
      this.openedTicks = 0;
      this.updateSourceSellSelectedLabel(var1);
      List var10000 = var1.session.story.trace();
      int var10001 = var3.size();
      var10000.add("PORTED source game.k.P close msgyn.ui refresh shopbuy.ui rows=" + var10001 + " reason=" + var2);
   }

   private void updateSourceSellSelectedLabel(VqsvGameRuntime.Scene var1) {
      List var2 = sourceWorldShopSellRows(var1);
      this.sourceSellSelectedLabel = var2.isEmpty() ? "Bán ra" : ((SourceSellRow)var2.get(clamp(this.selected, 0, var2.size() - 1))).item.name;
   }

   private void openSourceWorldShopRecover(VqsvGameRuntime.Scene var1) {
      WorldPetRecoveryService.Quote var2 = WORLD_PET_RECOVERY_SERVICE.quote(var1.session.pets.roster, this.sourceWorldShopRecoveryPort(var1));
      if (var2.outcome == WorldPetRecoveryService.QuoteOutcome.NO_RECOVERY_NEEDED) {
         this.sourceRecoveryCost = -1;
         this.selected = 2;
         var1.text = TextBox.msgWarm("Toàn bộ trạng thái đã đầy, không cần khôi phục", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PORTED source game.k.G recovery quote=-1 -> warning; shop.ui selected=2; no mutation");
      } else {
         this.sourceRecoveryCost = var2.cost;
         this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM;
         this.selected = 0;
         this.listScroll = 0;
         this.openedTicks = 0;
         List var10000 = var1.session.story.trace();
         int var10001 = this.sourceRecoveryCost;
         var10000.add("PORTED source game.k.G recovery open msgRecover.ui cost=" + var10001 + " money=" + var1.session.inventory.currency.money + " pets=" + var1.session.pets.roster.size());
      }
   }

   private void tickSourceWorldShopRecoverConfirm(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.closeSourceWorldShopRecoverConfirm(var1, "back");
      } else if (var1.key0) {
         WorldPetRecoveryService.RecoveryResult var2 = WORLD_PET_RECOVERY_SERVICE.recover(var1.session.pets.roster, var1.session.inventory.currency, this.sourceWorldShopRecoveryPort(var1));
         if (var2.outcome == WorldPetRecoveryService.RecoveryOutcome.INSUFFICIENT_MONEY) {
            var1.text = TextBox.msgWarm("Kim tiền chưa đủ", "Nhấn nút 5 để tiếp tục");
            var1.session.story.trace().add("PORTED source game.k.G recovery insufficient cost=" + var2.quote.cost + " money=" + var2.moneyBefore + " -> no mutation");
            this.closeSourceWorldShopRecoverConfirm(var1, "insufficient money");
         } else if (var2.outcome == WorldPetRecoveryService.RecoveryOutcome.NO_RECOVERY_NEEDED) {
            var1.text = TextBox.msgWarm("Toàn bộ trạng thái đã đầy, không cần khôi phục", "Nhấn nút 5 để tiếp tục");
            var1.session.story.trace().add("PORTED source game.k.G recovery re-quote=-1 -> warning; no mutation");
            this.closeSourceWorldShopRecoverConfirm(var1, "no recovery needed");
         } else {
            var1.text = TextBox.msgWarm("Ba lô sủng vật trạng thái toàn bộ khôi phục", "Nhấn nút 5 để tiếp tục");
            var1.session.story.trace().add("PORTED source game.k.G recovery commit cost=" + var2.quote.cost + " money=" + var2.moneyBefore + "->" + var2.moneyAfter + " pets=" + var2.petsRecovered + " refreshOps=" + var1.session.pets.refreshOperations);
            this.beginSourceWorldShopRecoverySave(var1);
         }
      }
   }

   private void beginSourceWorldShopRecoverySave(VqsvGameRuntime.Scene var1) {
      this.mode = VqsvPanelRuntime.Mode.SAVE;
      this.selected = 2;
      this.listScroll = 0;
      this.openedTicks = 0;
      this.sourceRecoveryCost = -1;
      this.sourceRecoverySavePhase = 1;
      this.savePhase = 1;
      this.saveMessage = "";
      var1.session.story.trace().add("PORTED source game.k.G recovery f=3 close msgRecover.ui; await recovery-success openbox -> source post-recovery save owner");
   }

   private void tickSourceWorldShopRecoverySave(VqsvGameRuntime.Scene var1) {
      if (this.sourceRecoverySavePhase == 1) {
         this.sourceRecoverySavePhase = 2;
         this.saveMessage = "Đang lưu...";
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.k.G recovery f=3->4 success notice closed; msgtip.ui text=Dang luu...");
      } else if (this.sourceRecoverySavePhase == 2) {
         VqsvSaveRuntime.SaveResult var3 = VqsvSaveRuntime.save(var1, VqsvSaveRuntime.SaveRequest.manual("world-shop-recovery"));
         this.sourceRecoverySavePhase = var3.success ? 3 : 4;
         this.saveMessage = var3.success ? (var3.cloudSynced ? "Lưu thành công" : "Đã lưu máy; lỗi cloud") : "Lưu thất bại";
         var1.session.story.trace().add(var3.success ? "PORTED source game.k.G recovery f=4->5 canonical save success" : "PENDING source game.k.G recovery canonical save failed error=" + var3.detail);
      } else if (var1.key0 || var1.keyBack) {
         boolean var2 = this.sourceRecoverySavePhase == 3;
         this.sourceRecoverySavePhase = 0;
         this.savePhase = 0;
         this.saveMessage = "";
         this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP;
         this.selected = 2;
         this.listScroll = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.k.G recovery save result acknowledged success=" + var2 + " -> shop.ui selected=2; recovery transaction not replayed");
      }
   }

   private void closeSourceWorldShopRecoverConfirm(VqsvGameRuntime.Scene var1, String var2) {
      this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP;
      this.selected = 2;
      this.listScroll = 0;
      this.openedTicks = 0;
      this.sourceRecoveryCost = -1;
      this.sourceRecoverySavePhase = 0;
      var1.session.story.trace().add("PORTED source game.k.G close msgRecover.ui -> shop.ui selected=2 reason=" + var2);
   }

   private WorldPetRecoveryService.PetRecoveryPort sourceWorldShopRecoveryPort(final VqsvGameRuntime.Scene var1) {
      return new WorldPetRecoveryService.PetRecoveryPort() {
         {
            Objects.requireNonNull(VqsvPanelRuntime.this);
         }

         public int maxHp(PetState var1x) {
            return PetSourceAdapter.maxHp(var1x);
         }

         public void recoverFull(PetState var1x) {
            PetSourceAdapter.reviveFull(var1x);
            ++var1.session.pets.refreshOperations;
         }
      };
   }

   private void tickPortableShop(VqsvGameRuntime.Scene var1) {
      int var2 = this.selected;
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, PORTABLE_SHOP_LABELS.length - 1);
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, PORTABLE_SHOP_LABELS.length - 1);
      } else {
         if (var1.keyBack) {
            this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
            this.selected = 0;
            this.resetGameMenuViewport();
            this.openedTicks = 0;
            var1.session.story.trace().add("PORTED/PARTIAL panel game.k.aD bodyShop.ui back -> P=6 gamemenu.ui selected=0");
            return;
         }

         if (var1.key0) {
            if (this.selected == 0) {
               this.openPortableShopBuy(var1, 4, (byte)0, "PORTED/PARTIAL panel game.k.aD bodyShop.ui confirm c=0 -> P=26 game.k.a(4,0) shopbuy.ui");
               return;
            }

            if (this.selected == 1) {
               this.openPortableShopServiceConfirm(var1, 4);
               return;
            }

            if (this.selected == 2) {
               this.openFashionShop(var1);
               return;
            }

            if (this.selected == 3) {
               this.openPortableShopBuy(var1, 3, (byte)2, "PC_BRIDGE panel game.k.aD bodyShop.ui confirm c=material -> game.k.a(3,(byte)2) shopbuy.ui");
               return;
            }

            var1.text = TextBox.msgWarm("Chức năng còn chưa mở", "Nhấn nút 5 để tiếp tục");
            var1.session.story.trace().add("PARTIAL panel game.k.aD bodyShop.ui premium branch c=" + this.selected + " not ported in portable shop item slice");
            return;
         }
      }

      if (this.selected != var2) {
         this.openedTicks = 0;
         List var10000 = var1.session.story.trace();
         int var10001 = this.selected;
         var10000.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui key selected=" + var10001 + " description=" + portableShopDescription(var1, this.selected));
      }

   }

   private void tickSourceEastWharf(VqsvGameRuntime.Scene var1) {
      String[] var2 = this.sourceWharfLabels();
      int var3 = this.selected;
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, var2.length - 1);
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, var2.length - 1);
      } else {
         if (var1.keyBack || var1.key0 && this.selected == var2.length - 1) {
            this.close(var1);
            var1.session.story.trace().add("PORTED source game.k.aP wharf exit Q=" + this.sourceWharfIndex + " selected=" + this.selected + " -> world state0");
            return;
         }

         if (var1.key0) {
            int[] var4 = SOURCE_WHARF_ROUTES[this.sourceWharfIndex][this.selected];
            boolean var5 = var1.session.story.eventState.sourceEventStateComplete(var4[0], var4[1], var4[2]);
            if (!var5) {
               this.showClosedWaterRoute(var1, this.selected, false, true);
               return;
            }

            this.travelSourceWharfRoute(var1, var4, var2[this.selected]);
            return;
         }
      }

      if (this.selected != var3) {
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.k.aP wharf key Q=" + this.sourceWharfIndex + " selected=" + this.selected + " label=" + var2[this.selected]);
      }

   }

   private void travelSourceWharfRoute(VqsvGameRuntime.Scene var1, int[] var2, String var3) {
      this.close(var1);
      var1.session.runtime.activity = (new VqsvWharfTravelRuntime(var2, var3));
      var1.session.story.trace().add("PORTED source game.k.aP wharf travel request Q=" + this.sourceWharfIndex + " selected=" + this.selected + " label=" + var3 + " gate=[" + var2[0] + "," + var2[1] + "," + var2[2] + "] destination=[" + var2[3] + "," + var2[4] + ",-1] player=[" + var2[5] + "," + var2[6] + "] direction=" + var2[7] + " boatMode=" + var2[8] + " -> state29/game.f-state23 map101 sprite343");
   }

   private void showClosedWaterRoute(VqsvGameRuntime.Scene var1, int var2, boolean var3, boolean var4) {
      var1.text = TextBox.msgWarm("Đường thủy chưa mở", "Nhấn nút 5 để tiếp tục");
      var1.session.story.trace().add("PORTED source game.k.aP wharf closed route selected=" + var2 + " label=" + this.sourceWharfLabels()[var2] + " sourceGate=" + var3 + " targetCommissioned=" + var4 + " no world mutation");
   }

   private void tickSourceConvenienceShop(VqsvGameRuntime.Scene var1) {
      String[] var2 = this.sourceConvenienceLabels();
      int var3 = this.selected;
      if (var1.keyUp) {
         this.selected = clamp(this.selected - 1, 0, var2.length - 1);
         this.keepSelectedVisible(var2.length);
      } else if (var1.keyDown) {
         this.selected = clamp(this.selected + 1, 0, var2.length - 1);
         this.keepSelectedVisible(var2.length);
      } else {
         if (var1.keyBack || var1.key0 && this.selected == var2.length - 1) {
            this.close(var1);
            this.portableShopReturnToSourceConvenience = false;
            var1.session.story.trace().add("PORTED source game.k.aT wharf2.ui exit selected=" + this.selected + " -> world state0");
            return;
         }

         if (var1.key0) {
            if (this.selected == 0) {
               this.claimSourceConvenienceReward(var1);
               return;
            }

            if (this.selected == 1 || this.selected == 2) {
               boolean var4 = this.sourceConvenienceBankExpansionEnabled && this.selected == 2;
               if (!var1.session.progression.evolutionNoticeArmed && !var4) {
                  var1.text = TextBox.msgWarm("Công năng theo đạo học sau mở ra", "Nhấn nút 5 để tiếp tục");
                  var1.session.story.trace().add("PORTED source game.k.aT wharf2.ui selected=" + this.selected + " label=" + var2[this.selected] + " blocked game.l.U=false -> msgwarm.ui stay state27");
                  return;
               }

               int var5 = this.selected;
               this.visible = false;
               var1.openSourceConveniencePetstate(var5);
               var1.session.story.trace().add("PORTED source game.k.aT wharf2.ui selected=" + var5 + " label=" + var2[var5] + " -> game.l state7 petstate.ui c=0");
               return;
            }

            if (this.selected == 3) {
               this.openPortableShopBuy(var1, 3, (byte)2, "PORTED source game.k.aT wharf2.ui selected=3 Tai lieu -> game.l state32 game.k.a(3,(byte)2) shopbuy.ui", false);
               this.portableShopReturnToSourceConvenience = true;
               return;
            }

            if (this.sourceConvenienceBankExpansionEnabled && this.selected == 4) {
               this.openRainbowCharm(var1);
               return;
            }

            if (this.sourceConvenienceBankExpansionEnabled && this.selected == 5) {
               this.purchasePetBankExpansion(var1);
               return;
            }
         }
      }

      if (this.selected != var3) {
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED source game.k.aT wharf2.ui key selected=" + this.selected + " label=" + var2[this.selected]);
      }

   }

   private void renderRainbowCharm(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      int var4 = 3054715;
      drawShopPanelBase(var1, var2, "BÙA HỘ TRẬN CẦU VỒNG", var4);
      String[] var5 = new String[]{"Trang bị", "Chế tác", "Cường hóa"};

      for(int var6 = 0; var6 < var5.length; ++var6) {
         int var7 = 22 + var6 * 67;
         boolean var8 = var6 == this.rainbowCharmTab;
         var1.setColor(new Color(var8 ? 15911010 : 2383207));
         var1.fillRoundRect(var7, 67, 63, 17, 5, 5);
         drawBagText(var1, var2, var5[var6], var7 + 2, 71, 59, new Color(var8 ? 1523542 : 16777215));
      }

      drawBagPill(var1, var2, "Kim " + var3.session.inventory.currency.money, 20, 87, 90, 15, 1523542, 15911010);
      drawBagPill(var1, var2, "HH " + (new RainbowCharmService()).achievedBadgeCount(var3.session), 114, 87, 50, 15, 1523542, 16777215);
      List var16 = this.rainbowCharmRows(var3);
      int var17 = this.visibleListStart(var16.size());

      for(int var18 = 0; var18 < 5; ++var18) {
         int var9 = var17 + var18;
         int var10 = 99 + var18 * 21;
         boolean var11 = var9 < var16.size() && var9 == this.selected;
         var1.setColor(new Color(var11 ? 15398900 : 16252155));
         var1.fillRoundRect(20, var10, 196, 19, 5, 5);
         var1.setColor(new Color(var11 ? 15911010 : 7711401));
         var1.drawRoundRect(20, var10, 195, 18, 5, 5);
         if (var9 < var16.size()) {
            RainbowCharmCatalog.Definition var12 = (RainbowCharmCatalog.Definition)var16.get(var9);
            UnifiedItemRecord var13 = var12.item();
            UnifiedItemIconRenderer.draw(var1, var13.iconResource, 27, var10 + 2, 15, 15);
            drawBagMarquee(var1, var2, var13.name, 47, var10 + 5, 105, var11 ? new Color(1523542) : new Color(1862801), this.openedTicks);
            int var14 = var3.session.progression.rainbowCharms.tier(var12.runtimeId);
            String var15 = var14 > 0 ? RainbowCharmCatalog.romanTier(var14) : (var12.badgeUnlock > 0 ? var12.badgeUnlock + " HH" : "Mở sẵn");
            drawBagPill(var1, var2, var15, 159, var10 + 2, 48, 15, var11 ? 3054715 : 2383207, 16777215);
         }
      }

      drawModernPanelShopScrollbar(var1, var16.size(), var17);
      RainbowCharmCatalog.Definition var19 = var16.isEmpty() ? null : (RainbowCharmCatalog.Definition)var16.get(clamp(this.selected, 0, var16.size() - 1));
      var1.setColor(new Color(1523542));
      var1.fillRoundRect(20, 205, 200, 46, 6, 6);
      var1.setColor(new Color(15911010));
      var1.drawRoundRect(20, 205, 199, 45, 6, 6);
      if (var19 != null) {
         int var20 = var3.session.progression.rainbowCharms.tier(var19.runtimeId);
         String var22 = var19.slot == RainbowCharmCatalog.Slot.SURVIVAL ? "Sinh tồn" : (var19.slot == RainbowCharmCatalog.Slot.TACTICAL ? "Chiến thuật" : "Thám hiểm");
         drawBagMarquee(var1, var2, var19.item().name, 27, 209, 126, new Color(15911010), this.openedTicks);
         drawBagPill(var1, var2, var22, 161, 208, 52, 14, 3054715, 16777215);
         byte var23 = 27;
         short var24 = 186;
         short var25 = 205;
         String var10000 = var3.session.progression.rainbowCharms.activeId(var19.slot) == var19.runtimeId ? "Đang dùng" : "Chưa dùng";
         String var26 = var10000 + " | Cấp " + RainbowCharmCatalog.romanTier(Math.max(1, var20));
         drawBagText(var1, var2, var26, var23, var25 + 18, var24, new Color(16777215));
         drawBagMarquee(var1, var2, rainbowCharmEffectDetail(var19, Math.max(1, var20)), var23, var25 + 27, var24, new Color(15911010), this.openedTicks);
         String var27 = this.rainbowCharmTab == 0 ? "Giới hạn: 1 bùa mỗi nhóm" : rainbowCharmCostLine(var3, var19, var20);
         drawBagMarquee(var1, var2, var27, var23, var25 + 36, var24, new Color(16777215), this.openedTicks);
      }

      if (!this.rainbowCharmMessage.isEmpty()) {
         drawBagText(var1, var2, this.rainbowCharmMessage, 22, 253, 196, new Color(15911010));
      }

      String var21 = this.rainbowCharmTab == 0 ? "Trang bị" : (this.rainbowCharmTab == 1 ? "Chế tác" : "Cường hóa");
      drawBagButton(var1, var2, var21, 25, 264, 76, 19, true, var4);
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, false, var4);
   }

   private void tickBattlePass(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
         this.selected = 8;
         this.listScroll = 0;
      } else {
         if (var1.keyLeft || var1.keyRight) {
            this.battlePassTrack = this.battlePassTrack == 0 ? 1 : 0;
         }

         if (var1.keyUp) {
            this.selected = Math.max(1, this.selected - 1);
         }

         if (var1.keyDown) {
            this.selected = Math.min(50, this.selected + 1);
         }

         if (var1.key0) {
            if (this.battlePassTrack == 1 && !var1.session.progression.battlePass.vipUnlocked) {
               boolean var3 = BATTLE_PASS_ENGINE.purchaseVip(var1.session, var1.session.progression.battlePass);
               this.battlePassMessage = var3 ? "Vé VIP đã mở khóa (-20 huy hiệu)." : "Cần 20 huy hiệu để mở Vé VIP.";
            } else {
               BattlePassEngine.ClaimResult var2 = BATTLE_PASS_ENGINE.claim(var1.session, var1.session.progression.battlePass, this.selected, this.battlePassTrack == 1);
               this.battlePassMessage = var2.claimed ? "Đã nhận quà cấp " + this.selected + "." : "Chưa nhận được: " + var2.reason;
            }

            this.openedTicks = 0;
         }

         this.listScroll = clamp(this.selected - 3, 0, 45);
      }
   }

   private void tickBattlePassHelp(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack || var1.key0) {
         this.mode = VqsvPanelRuntime.Mode.BATTLE_PASS;
         this.openedTicks = 0;
      }

   }

   private void renderBattlePass(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      BattlePassState var4 = var3.session.progression.battlePass;
      int var5 = 2916241;
      drawShopPanelBase(var1, var2, "THẺ FAN CỨNG VQSV", var5);
      drawBagWrappedFixed(var1, var2, "MÙA 1 • THẦN THÚ THỨC TỈNH", 18, 45, 204, 1, new Color(1527657));
      int var10002 = var4.daysRemaining(System.currentTimeMillis());
      drawBagWrappedFixed(var1, var2, "Còn " + var10002 + " ngày", 18, 59, 204, 1, new Color(9132576));
      var10002 = var4.level();
      drawBagWrappedFixed(var1, var2, "Lv " + var10002 + "   " + var4.levelXp() + "/1000 EXP", 18, 73, 204, 1, new Color(1527657));
      var1.setColor(new Color(5400440));
      var1.fillRoundRect(18, 89, 204, 8, 4, 4);
      var1.setColor(new Color(4380077));
      var1.fillRoundRect(18, 89, Math.max(2, 204 * var4.levelXp() / 1000), 8, 4, 4);
      drawBagButton(var1, var2, this.battlePassTrack == 0 ? "FREE" : "VIP", 18, 101, 62, 22, this.battlePassTrack == 0, this.battlePassTrack == 0 ? 2916241 : 12946226);
      drawBagButton(var1, var2, var4.vipUnlocked ? "VIP ĐÃ MỞ" : "MỞ VIP • 20 HH", 86, 101, 108, 22, this.battlePassTrack == 1, 12946226);
      drawBagButton(var1, var2, "?", 198, 101, 24, 22, false, 2916241);
      int var6 = this.listScroll;

      for(int var7 = 0; var7 < 5 && var6 + var7 < 50; ++var7) {
         int var8 = var6 + var7 + 1;
         BattlePassLevel var9 = BattlePassCatalog.level(var8);
         int var10 = 127 + var7 * 25;
         boolean var11 = this.selected == var8;
         var1.setColor(new Color(var11 ? 7619358 : 15924216));
         var1.fillRoundRect(14, var10, 212, 24, 5, 5);
         var1.setColor(new Color(var11 ? 15911010 : 7711401));
         var1.drawRoundRect(14, var10, 211, 23, 5, 5);
         List var12 = this.battlePassTrack == 0 ? var9.freeRewards : var9.vipRewards;
         String var13 = ((BattlePassReward)var12.get(0)).label;
         String var14 = battlePassRewardSummary(var12, 1);
         String var15 = var4.claimed(this.battlePassTrack == 1, var8) ? "DA NHAN" : (var8 <= var4.level() ? "NHẬN" : "KHÓA");
         var2.drawTaggedLine(var1, "Lv" + var8, 20, var10 + 5, 5, var11 ? 16777215 : 1527657);
         drawBattlePassRewardLine(var1, var2, var13, 51, var10 + 3, var11 ? 16777215 : 1527657);
         if (!var14.isEmpty()) {
            drawBattlePassRewardLine(var1, var2, "+ " + var14, 51, var10 + 14, var11 ? 15911010 : 3765386);
         }

         var2.drawTaggedLine(var1, var15, 178, var10 + 5, var15.length(), var11 ? 15911010 : (var15.equals("KHÓA") ? 8559016 : 2133357));
      }

      BattlePassLevel var16 = BattlePassCatalog.level(this.selected);
      String var17 = var16 == null ? "" : battlePassRewardSummary(this.battlePassTrack == 0 ? var16.freeRewards : var16.vipRewards, 0);
      drawBagWrappedFixed(var1, var2, this.battlePassMessage.isEmpty() ? "Quà Lv" + this.selected + ": " + var17 : this.battlePassMessage, 18, 252, 204, 2, new Color(1527657));
      drawBagButton(var1, var2, "Xác nhận", 25, 264, 76, 19, true, var5);
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, false, var5);
   }

   private void renderBattlePassHelp(Graphics2D var1, UiFont var2) {
      int var3 = 2916241;
      drawShopPanelBase(var1, var2, "EXP THẺ FAN CỨNG", var3);
      drawBagWrappedFixed(var1, var2, "Làm nhiệm vụ và chơi game để nhận EXP. Mỗi mốc đủ EXP sẽ tự lên cấp.", 18, 45, 204, 2, new Color(1527657));
      int var4 = 72;

      for(BattlePassMission var6 : BattlePassCatalog.missions()) {
         String var7 = var6.title + "  +" + var6.xp + " EXP";
         drawBagWrappedFixed(var1, var2, var7, 18, var4, 204, 1, new Color(1527657));
         var4 += 25;
      }

      drawBagWrappedFixed(var1, var2, "Trận thắng +100 EXP | hoàn thành trận +25 | dùng Skill +5 | ấp trứng +50 | tiến hóa +80 | đăng nhập +20", 18, 226, 204, 3, new Color(9132576));
      drawBagButton(var1, var2, "Quay lại", 140, 264, 75, 19, true, var3);
   }

   private static void drawBattlePassRewardLine(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5) {
      String var6 = fitBagText(var1, var2, 122);
      var1.drawTaggedLine(var0, var6, var3, var4, TextBox.visibleLength(var6), var5);
   }

   private static String battlePassRewardSummary(List<BattlePassReward> var0, int var1) {
      StringBuilder var2 = new StringBuilder();

      for(int var3 = Math.max(0, var1); var3 < var0.size(); ++var3) {
         if (var2.length() > 0) {
            var2.append(" | ");
         }

         var2.append(((BattlePassReward)var0.get(var3)).label);
      }

      return var2.toString();
   }

   private static String activeCharmDetail(VqsvGameRuntime.Scene var0, RainbowCharmCatalog.Definition var1, int var2) {
      boolean var3 = var0.session.progression.rainbowCharms.activeId(var1.slot) == var1.runtimeId;
      return (var3 ? "Đang dùng" : "Chưa dùng") + " | Cấp " + RainbowCharmCatalog.romanTier(Math.max(1, var2)) + " | " + rainbowCharmEffectDetail(var1, Math.max(1, var2));
   }

   private static String rainbowCharmRecipeDetail(VqsvGameRuntime.Scene var0, RainbowCharmCatalog.Definition var1, int var2) {
      RainbowCharmService var3 = new RainbowCharmService();
      RainbowCharmService.Recipe var4 = var2 <= 0 ? var3.craftRecipe(var1.runtimeId) : var3.nextUpgradeRecipe(var1, var2);
      if (var4 == null) {
         String var10000 = rainbowCharmEffectDetail(var1, var2);
         return var10000 + " | Bùa đã đạt cấp V.";
      } else {
         StringBuilder var5 = new StringBuilder();
         var5.append(rainbowCharmEffectDetail(var1, Math.max(1, var2))).append(" | ");
         if (var4.badgesRequired > 0) {
            var5.append("Cần ").append(var4.badgesRequired).append(" HH | ");
         }

         var5.append(var4.money).append(" kim");

         for(Map.Entry var7 : var4.materials.entrySet()) {
            UnifiedItemRecord var8 = UnifiedItemCatalog.instance().byRuntime(UnifiedItemInventoryKind.MATERIAL, (Integer)var7.getKey());
            var5.append(" | ").append(var8 == null ? "NL " + String.valueOf(var7.getKey()) : var8.name).append(' ').append(var7.getValue());
         }

         return var5.toString();
      }
   }

   private static String rainbowCharmCostLine(VqsvGameRuntime.Scene var0, RainbowCharmCatalog.Definition var1, int var2) {
      RainbowCharmService var3 = new RainbowCharmService();
      RainbowCharmService.Recipe var4 = var2 <= 0 ? var3.craftRecipe(var1.runtimeId) : var3.nextUpgradeRecipe(var1, var2);
      if (var4 == null) {
         return "Đã đạt cấp V";
      } else {
         StringBuilder var5 = new StringBuilder("Cần ");
         if (var4.badgesRequired > 0) {
            var5.append(var4.badgesRequired).append(" HH | ");
         }

         var5.append(var4.money).append(" kim");

         for(Map.Entry var7 : var4.materials.entrySet()) {
            var5.append(" | ").append(VqsvSourceOps.sourceMaterialName((Integer)var7.getKey())).append('x').append(var7.getValue());
         }

         return var5.toString();
      }
   }

   private static String rainbowCharmEffectDetail(RainbowCharmCatalog.Definition var0, int var1) {
      int var2 = var0.valueAtTier(var1);
      switch (var0.mechanic) {
         case EMERGENCY_HEAL:
            return "Hồi " + var2 + "% HP khi dưới 50%, 1 lần/trận";
         case SKILL_SEAL:
            return var2 + "% phong 3 Skill đối thủ trong 2 hiệp";
         case CORROSION_POWER:
            return "+" + var2 + "% sát thương Hủ Thực cho Thực Vật";
         case PARALYSIS_DURATION:
            return var2 + "% cộng " + (var1 >= 3 ? 2 : 1) + " hiệp Tê Liệt cho Mãnh Thú";
         case METAL_DEFENSE:
            return "+" + var2 + "% Phòng ngự cho Kim Loại";
         case ENEMY_PP_DRAIN:
            return var2 + "% trừ thêm 1 PP đối thủ cho Phi Hành";
         case SELF_PP_REFUND:
            return var2 + "% hoàn 1 PP cho Hải Dương";
         case BATTLE_REVIVE:
            int var3 = (new int[]{0, 20, 25, 30, 40, 50})[var1];
            return var2 + "% hồi sinh với " + var3 + "% HP, 1 lần/trận";
         case RARE_ENCOUNTER:
            return "+" + var2 + " điểm lượt xác định độ hiếm";
         case PREVENT_CAPTURE_FLEE:
            return "+" + var2 + " điểm bắt sau mỗi lần trượt, cộng dồn";
         case ESCAPE_RETRY:
            return var2 + "% phá cấm chạy ở trận Pet hoang thường";
         case EVASION:
            return var2 + "% né hoàn toàn đòn đánh";
         default:
            return "Hiệu lực " + var2 + "%";
      }
   }

   private String[] sourceConvenienceLabels() {
      return this.sourceConvenienceBankExpansionEnabled ? BICH_THUY_ENVOY_CONVENIENCE_LABELS : SOURCE_CONVENIENCE_LABELS;
   }

   private void openRainbowCharm(VqsvGameRuntime.Scene var1) {
      if (var1.session.story.mainTaskProgress < 5) {
         var1.text = TextBox.msgWarm("Hoàn thành hướng dẫn trang bị Pet để mở Bùa Hộ Trận.", "Nhấn nút 5 để tiếp tục");
      } else {
         this.rainbowCharmMessage = "Chọn bùa để chế tác, cường hóa hoặc trang bị.";
         this.mode = VqsvPanelRuntime.Mode.RAINBOW_CHARM;
         this.rainbowCharmReturnToBag = false;
         this.rainbowCharmTab = 0;
         this.selected = 0;
         this.listScroll = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("UNIFIED RAINBOW-CHARM UI open actor=62 autoGrant=false");
      }
   }

   private void tickRainbowCharm(VqsvGameRuntime.Scene var1) {
      List var2 = this.rainbowCharmRows(var1);
      if (!var1.keyLeft && !var1.keyRight) {
         if (var1.keyUp) {
            this.selected = clamp(this.selected - 1, 0, Math.max(0, var2.size() - 1));
            this.keepSelectedVisible(var2.size());
         } else if (var1.keyDown) {
            this.selected = clamp(this.selected + 1, 0, Math.max(0, var2.size() - 1));
            this.keepSelectedVisible(var2.size());
         } else if (var1.keyBack) {
            if (this.rainbowCharmReturnToBag) {
               this.mode = VqsvPanelRuntime.Mode.BAG;
               this.bagTab = 1;
               this.selected = this.rainbowCharmReturnBagSelected;
               this.listScroll = this.rainbowCharmReturnBagScroll;
               this.rainbowCharmReturnToBag = false;
            } else {
               this.openSourceConvenienceFromWorld(var1, var1.session.world.worldEventActor);
               this.selected = 4;
               this.keepSelectedVisible(BICH_THUY_ENVOY_CONVENIENCE_LABELS.length);
            }
         } else if (var1.key0 && !var2.isEmpty()) {
            RainbowCharmCatalog.Definition var3 = (RainbowCharmCatalog.Definition)var2.get(clamp(this.selected, 0, var2.size() - 1));
            RainbowCharmService var4 = new RainbowCharmService();
            if (this.rainbowCharmTab == 0) {
               RainbowCharmService.Result var5 = var4.equip(var1.session, var3.runtimeId);
               this.rainbowCharmMessage = charmResultMessage(var5);
               traceRainbowCharmResult(var1, var5);
            } else if (this.rainbowCharmTab == 1 && var1.session.progression.rainbowCharms.owns(var3.runtimeId)) {
               this.rainbowCharmMessage = "Bùa này đã có trong kho.";
            } else if (this.rainbowCharmTab == 2 && var1.session.progression.rainbowCharms.tier(var3.runtimeId) >= 5) {
               this.rainbowCharmMessage = "Bùa đã đạt cấp V.";
            } else {
               this.rainbowCharmPendingId = var3.runtimeId;
               this.mode = VqsvPanelRuntime.Mode.RAINBOW_CHARM_CONFIRM;
            }
         }
      } else {
         this.rainbowCharmTab = (this.rainbowCharmTab + (var1.keyRight ? 1 : 2)) % 3;
         this.selected = 0;
         this.listScroll = 0;
         this.rainbowCharmMessage = "";
      }
   }

   private void tickRainbowCharmConfirm(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.RAINBOW_CHARM;
         this.rainbowCharmPendingId = -1;
      } else if (var1.key0) {
         RainbowCharmService var2 = new RainbowCharmService();
         RainbowCharmService.Result var3 = this.rainbowCharmTab == 1 ? var2.craft(var1.session, this.rainbowCharmPendingId) : var2.upgrade(var1.session, this.rainbowCharmPendingId);
         this.rainbowCharmMessage = charmResultMessage(var3);
         traceRainbowCharmResult(var1, var3);
         this.mode = VqsvPanelRuntime.Mode.RAINBOW_CHARM;
         this.rainbowCharmPendingId = -1;
         List var4 = this.rainbowCharmRows(var1);
         this.selected = clamp(this.selected, 0, Math.max(0, var4.size() - 1));
         this.keepSelectedVisible(var4.size());
      }
   }

   private List<RainbowCharmCatalog.Definition> rainbowCharmRows(VqsvGameRuntime.Scene var1) {
      ArrayList var2 = new ArrayList();

      for(RainbowCharmCatalog.Definition var4 : RainbowCharmCatalog.instance().definitions()) {
         boolean var5 = var1.session.progression.rainbowCharms.owns(var4.runtimeId);
         if ((this.rainbowCharmTab != 0 || var5) && (this.rainbowCharmTab != 2 || var5)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   private static String charmResultMessage(RainbowCharmService.Result var0) {
      switch (var0.outcome) {
         case STARTER_GRANTED -> {
            return "Đã nhận bùa khởi đầu.";
         }
         case CRAFTED -> {
            return "Chế tác thành công bùa cấp I.";
         }
         case UPGRADED -> {
            return "Cường hóa thành công lên cấp " + RainbowCharmCatalog.romanTier(var0.tierAfter) + ".";
         }
         case EQUIPPED -> {
            return "Đã trang bị bùa vào ô đội.";
         }
         case LOCKED -> {
            return "Chưa đủ số huy hiệu tiến trình yêu cầu.";
         }
         case INSUFFICIENT_MONEY -> {
            return "Không đủ kim tiền.";
         }
         case INSUFFICIENT_MATERIALS -> {
            return "Không đủ nguyên liệu.";
         }
         case ALREADY_MAX -> {
            return "Bùa đã đạt cấp V.";
         }
         case ALREADY_OWNED -> {
            return "Bùa này đã có trong kho.";
         }
         case NOT_OWNED -> {
            return "Chưa sở hữu bùa này.";
         }
         default -> {
            return "Không thể thực hiện thao tác.";
         }
      }
   }

   private static void traceRainbowCharmResult(VqsvGameRuntime.Scene var0, RainbowCharmService.Result var1) {
      List var10000 = var0.session.story.trace();
      String var10001 = String.valueOf(var1.outcome);
      var10000.add("UNIFIED RAINBOW-CHARM transaction outcome=" + var10001 + " runtimeId=" + var1.runtimeId + " tier=" + var1.tierBefore + "->" + var1.tierAfter);
   }

   private void purchasePetBankExpansion(VqsvGameRuntime.Scene var1) {
      PetBankExpansionService var2 = new PetBankExpansionService();
      PetBankExpansionService.PurchaseResult var3 = var2.purchase(var1.session.inventory.currency, var1.session.progression.petBankExpansionPurchases);
      if (var3.outcome == PetBankExpansionService.Outcome.SUCCESS) {
         var1.session.progression.petBankExpansionPurchases = var3.purchasesAfter;
         var1.text = TextBox.msgWarm("Mở rộng thành công. Kho Pet hiện có " + var3.capacityAfter + " ô.", "Nhấn nút 5 để tiếp tục");
      } else if (var3.outcome == PetBankExpansionService.Outcome.ALREADY_MAX) {
         var1.text = TextBox.msgWarm("Kho Pet đã đạt tối đa 400 ô.", "Nhấn nút 5 để tiếp tục");
      } else {
         var1.text = TextBox.msgWarm("Cần 10 Huy hiệu để mở thêm 100 ô kho Pet.", "Nhấn nút 5 để tiếp tục");
      }

      List var10000 = var1.session.story.trace();
      String var10001 = String.valueOf(var3.outcome);
      var10000.add("UNIFIED BANK-EXPANSION-V1 actor=62 outcome=" + var10001 + " purchases=" + var3.purchasesBefore + "->" + var3.purchasesAfter + " capacity=" + var3.capacityAfter + " badges=" + var3.badgesBefore + "->" + var3.badgesAfter);
   }

   private void claimSourceConvenienceReward(VqsvGameRuntime.Scene var1) {
      int var2 = var1.session.progression.collection.nextConvenienceRewardTier();
      int var3 = var1.session.progression.collection.convenienceRewardMilestone(var2);
      int var4 = var1.session.progression.collection.convenienceRewardCollectedCount(var2);
      if (var3 < 0) {
         var1.text = TextBox.msgWarm("Tất cả dẫn thưởng đã nhận.", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PORTED source game.l state31 reward tiers exhausted collected=" + var4);
      } else if (!var1.session.progression.collection.claimConvenienceReward(var2)) {
         var1.text = TextBox.msgWarm("Hiện đã thu thập " + var4 + " loài sủng vật. Cần " + var3 + " loài để nhận dẫn thưởng tiếp theo.", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PORTED source game.l state31 reward not ready tier=" + var2 + " collected=" + var4 + " required=" + var3);
      } else if (var2 == 6) {
         boolean var5 = var1.session.progression.badges.grant(7);
         var1.text = TextBox.msgWarm("Đạt được hoàng kim huy hiệu", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PORTED source game.l state31 final reward tier=6 collected=" + var4 + " goldBadgeGranted=" + var5);
      } else {
         VqsvSourceOps.sourceAdjustCurrencyFloorZero(var1, 1, 1);
         var1.text = TextBox.msgWarm("Đạt được 1 huy hiệu", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PORTED source game.l state31 reward tier=" + var2 + " milestone=" + var3 + " badges=" + var1.session.inventory.currency.badges);
      }
   }

   private void tickPortableShopBuy(VqsvGameRuntime.Scene var1) {
      int var2 = this.portableShopItemCount();
      this.selected = clamp(this.selected, 0, Math.max(0, var2 - 1));
      if (var1.keyUp) {
         int var3 = this.selected;
         this.selected = clamp(this.selected - 1, 0, Math.max(0, var2 - 1));
         this.keepSelectedVisible(var2);
         if (this.selected != var3) {
            List var10000 = var1.session.story.trace();
            String var10001 = this.sourceShopTraceContext();
            var10000.add("PORTED/PARTIAL panel game.k.a(" + var10001 + ") shopbuy.ui key=4100 selected=" + this.selected);
         }
      } else if (var1.keyDown) {
         int var4 = this.selected;
         this.selected = clamp(this.selected + 1, 0, Math.max(0, var2 - 1));
         this.keepSelectedVisible(var2);
         if (this.selected != var4) {
            List var5 = var1.session.story.trace();
            String var6 = this.sourceShopTraceContext();
            var5.add("PORTED/PARTIAL panel game.k.a(" + var6 + ") shopbuy.ui key=8448 selected=" + this.selected);
         }
      } else if (var1.keyBack) {
         if (this.portableShopReturnToWorld) {
            this.close(var1);
            var1.session.story.trace().add("PORTED/PARTIAL panel game.k.a(" + this.sourceShopTraceContext() + ") shopbuy.ui back -> source world state1 actor shop resume");
            return;
         }

         if (this.portableShopReturnToSourceWorldShop) {
            this.mode = VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP;
            this.selected = 0;
            this.listScroll = 0;
            this.openedTicks = 0;
            this.portableShopReturnToSourceWorldShop = false;
            var1.session.story.trace().add("PORTED source game.k.a(" + this.sourceShopTraceContext() + ") shopbuy.ui back -> game.k.F shop.ui selected=0");
            return;
         }

         if (this.portableShopReturnToSourceConvenience) {
            this.mode = VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP;
            this.selected = 3;
            this.listScroll = 0;
            this.openedTicks = 0;
            this.portableShopReturnToSourceConvenience = false;
            var1.session.story.trace().add("PORTED source game.k.a(3,2) shopbuy.ui back -> game.l state27 game.k.aS wharf2.ui selected=3");
            return;
         }

         this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP;
         this.selected = this.shopTable == 3 && this.shopBucket == 2 ? 3 : 0;
         this.listScroll = 0;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.k.a(" + this.sourceShopTraceContext() + ") shopbuy.ui back -> P=14 bodyShop.ui");
      } else if (var1.key0) {
         this.openPortableShopConfirm(var1);
      }

   }

   private void tickPortableShopConfirm(VqsvGameRuntime.Scene var1) {
      ItemDefinition var2 = this.portableShopSourceItem(this.shopConfirmItemId);
      if (var2 == null) {
         this.closePortableShopConfirm(var1, "missing row");
      } else {
         int var3 = this.portableShopMaxQuantity(var1, this.shopConfirmItemId);
         if (var1.keyLeft) {
            --this.shopConfirmQuantity;
            if (this.shopConfirmQuantity <= 0) {
               this.shopConfirmQuantity = Math.max(1, var3);
            }

            this.syncPortableShopConfirm(var1);
            List var4 = var1.session.story.trace();
            String var5 = this.sourceShopTraceContext();
            var4.add("PORTED/PARTIAL panel game.k.a(" + var5 + ") msgyn.ui key=16400 item=" + this.shopConfirmItemId + " qty=" + this.shopConfirmQuantity + " total=" + this.shopConfirmTotal);
         } else if (var1.keyRight) {
            ++this.shopConfirmQuantity;
            if (this.shopConfirmQuantity > Math.max(1, var3)) {
               this.shopConfirmQuantity = 1;
            }

            this.syncPortableShopConfirm(var1);
            List var10000 = var1.session.story.trace();
            String var10001 = this.sourceShopTraceContext();
            var10000.add("PORTED/PARTIAL panel game.k.a(" + var10001 + ") msgyn.ui key=32832 item=" + this.shopConfirmItemId + " qty=" + this.shopConfirmQuantity + " total=" + this.shopConfirmTotal);
         } else if (var1.keyBack) {
            this.closePortableShopConfirm(var1, "back");
         } else {
            if (var1.key0) {
               this.commitPortableShopItem(var1);
            }

         }
      }
   }

   private void openPortableShopServiceConfirm(VqsvGameRuntime.Scene var1, int var2) {
      long var3 = WorldRealtimeClock.nowMillis();
      if (var2 == 4) {
         DailyBadgeRewardService.Status var5 = DAILY_BADGE_REWARD_SERVICE.status(var1.session.progression.dailyBadgeLastClaimEpochMillis, var3);
         if (!var5.available) {
            var1.text = TextBox.msgWarm("Có thể nhận lại sau " + WorldRealtimeClock.displayRemaining(var5.remainingMillis) + ".", "Nhấn nút 5 để tiếp tục");
            var1.session.story.trace().add("UNIFIED-DESIGN daily badge reward blocked nowEpoch=" + var3 + " lastClaimEpoch=" + var1.session.progression.dailyBadgeLastClaimEpochMillis + " remaining=" + var5.remainingMillis);
            return;
         }
      }

      PortableShopService.ServiceProductOpenResult var6 = PORTABLE_SHOP_SERVICE.planServiceProductOpen(var2, allPortableShopPetsMaxLevel(var1));
      if (var6.outcome == PortableShopService.ServiceProductOpenOutcome.ALL_PETS_MAX_LEVEL) {
         if (!this.openPortableShopLevelUpSkillLearnRepair(var1, "portable shop product3 all-max repair")) {
            var1.text = TextBox.msgWarm("Trong ba lô sủng vật đều đã max level", "Nhấn nút 5 để tiếp tục");
            var1.session.story.trace().add("PORTED panel game.k.aD bodyShop.ui product3 all-max warning pets=" + var1.session.pets.roster.size() + " msgwarm.ui f=1");
         }
      } else if (var6.outcome != PortableShopService.ServiceProductOpenOutcome.OPEN_CONFIRM) {
         var1.text = TextBox.msgWarm("Chức năng còn chưa mở", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PARTIAL panel game.k.aD bodyShop.ui premium branch c=" + var2 + " service product not ported");
      } else {
         this.serviceConfirmTitle = portableShopServiceConfirmTitle(var2);
         this.serviceConfirmPrompt = var2 == 4 ? "Nhận 20 Huy hiệu" : "Miễn phí";
         this.serviceProductId = var2;
         this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM;
         this.selected = var6.selectedRow;
         this.openedTicks = 0;
         var1.session.story.trace().add("PORTED/PARTIAL panel game.k.aD bodyShop.ui product" + var2 + " -> smsInfo.ui source-shaped PC-free confirm" + portableShopServiceTraceMetric(var1, var2));
      }
   }

   private void tickPortableShopServiceConfirm(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.closePortableShopServiceConfirm(var1, "back");
      } else {
         if (var1.key0) {
            if (this.serviceProductId == 4) {
               this.claimDailyBadges(var1);
               return;
            }

            PortableShopService.ServiceProductApplyResult var2 = PORTABLE_SHOP_SERVICE.applyServiceProductConfirm(this.serviceProductId, var1.session.pets.roster, var1.session.progression.evolution, var1.session.inventory.currency, PetSourceAdapter::refresh, (var1x) -> VqsvSourceEvolutionRuntime.noticeForPet(var1, var1x));
            if (var2.resetEvolutionRuntimeState) {
               var1.session.progression.evolutionNoticeIndex = 0;
               var1.session.progression.evolutionMode = 0;
            }

            if (var2.outcome == PortableShopService.ServiceProductApplyOutcome.LEVEL_UP_ROSTER) {
               tracePortableShopLevelUpProduct3(var1, var2.levelUp);
               if (!this.openPortableShopLevelUpSkillLearn(var1, var2.levelUp)) {
                  var1.text = TextBox.msgWarm("Sủng vật trong ba lô đã thăng cấp", "Nhấn nút 5 để tiếp tục");
               }
            } else if (var2.outcome == PortableShopService.ServiceProductApplyOutcome.GRANTED_BADGES) {
               tracePortableShopBadgeProduct4(var1, var2.currencyReward);
               var1.text = TextBox.msgWarm("Đã nhận 20 Huy hiệu", "Nhấn nút 5 để tiếp tục");
            } else if (var2.outcome == PortableShopService.ServiceProductApplyOutcome.GRANTED_MONEY) {
               tracePortableShopMoneyProduct2(var1, var2.currencyReward);
               var1.text = TextBox.msgWarm("Đã nhận 10000 kim tiền", "Nhấn nút 5 để tiếp tục");
            } else {
               var1.text = TextBox.msgWarm("Chức năng còn chưa mở", "Nhấn nút 5 để tiếp tục");
            }

            this.closePortableShopServiceConfirm(var1, "success");
         }

      }
   }

   private boolean openPortableShopLevelUpSkillLearn(VqsvGameRuntime.Scene var1, PortableShopService.LevelUpResult var2) {
      if (var2 == null) {
         return false;
      } else {
         ArrayList var3 = new ArrayList();
         ArrayList var4 = new ArrayList();

         for(PortableShopService.LevelUpPetResult var6 : var2.pets) {
            var3.add(var6.petIndex);
            var4.add(var6.levelBefore);
         }

         return var1.openSourceSkillLearnQueueForLevelTransitions(var3, var4, true, "portable shop product3");
      }
   }

   private boolean openPortableShopLevelUpSkillLearnRepair(VqsvGameRuntime.Scene var1, String var2) {
      ArrayList var3 = new ArrayList();

      for(int var4 = 0; var4 < var1.session.pets.roster.size(); ++var4) {
         var3.add(var4);
      }

      return var1.openSourceSkillLearnQueue(var3, true, var2);
   }

   private static String portableShopServiceConfirmTitle(int var0) {
      if (var0 == 3) {
         return "Thăng cấp chậm chạp, kẻ địch lại quá mạnh? Tất cả sủng vật trong ba lô của bạn đều được thăng lên 5 cấp.";
      } else if (var0 == 4) {
         return "Nhận 20 Huy hiệu mỗi ngày theo giờ thực tế.";
      } else {
         return var0 == 2 ? "Kiếm tiền vất vả, vật phẩm đắt đỏ? Bạn sẽ đạt được 10000 kim tiền." : "";
      }
   }

   private void tickChallenge(VqsvGameRuntime.Scene var1) {
      if ((var1.keyLeft || var1.keyRight) && this.selected == 0 && this.challengeRegion >= 0) {
         this.challengeRegion = REGIONAL_CHALLENGES.cycleRegion(var1.session.progression.badges, this.challengeRegion, var1.keyLeft ? -1 : 1);
         this.openedTicks = 0;
      } else if (var1.keyUp) {
         this.selected = 0;
      } else if (var1.keyDown) {
         this.selected = 1;
      } else if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.GAMEMENU;
         this.selected = 7;
         this.keepSelectedVisible(MENU_LABELS.length);
         this.openedTicks = 0;
      } else if (var1.key0) {
         if (this.selected == 1) {
            this.mode = VqsvPanelRuntime.Mode.GIFT_CODE;
            this.selected = 0;
            this.giftCodeInput = "";
            this.giftCodeMessage = "Nhập mã do nhà phát hành cung cấp.";
            this.openedTicks = 0;
            var1.session.story.trace().add("UNIFIED-DESIGN Giftcode input open activeCatalog=" + GiftCodeCatalog.instance().activeCount());
         } else if (this.challengeRegion < 0) {
            var1.text = TextBox.msgWarm("Cần hoàn thành một Đạo quán để mở thử thách.", "Tiếp tục");
         } else {
            long var2 = WorldRealtimeClock.nowMillis();
            RegionalRematchState.Status var4 = regionalRematchStatus(var1, this.challengeRegion, var2);
            if (!var4.available) {
               String var10 = var4.clockRollback ? "Thời gian thiết bị không hợp lệ. Chưa thể tái đấu." : "Hôm nay đã dùng đủ 3 lượt với Thủ lĩnh này. Reset sau " + WorldRealtimeClock.displayRemaining(var4.remainingMillis) + ".";
               var1.text = TextBox.msgWarm(var10, "Tiếp tục");
               var1.session.story.trace().add("UNIFIED-DESIGN regional rematch blocked npc=" + this.challengeRegion + " used=" + var4.usedCount + " remaining=" + var4.remainingMillis + " rollback=" + var4.clockRollback);
            } else {
               VqsvSourceStoryState.ensureInitialDienMieu(var1, "regional rematch battle entry");
               RegionalChallengeService.ChallengePlan var5 = REGIONAL_CHALLENGES.rematchPlan(this.challengeRegion);
               VqsvBattleEventDescriptor var6 = VqsvBattleEventDescriptor.byDescriptorSymbol(var5.descriptorSymbol);
               if (var6 == null) {
                  throw new IllegalStateException("Missing repeatable descriptor " + var5.descriptorSymbol);
               } else {
                  int var10002 = var5.rewardMoney;
                  String var10003 = String.valueOf(var5.kind);
                  BattleRequest var7 = var6.repeatableRequest(var1, var10002, "unified-repeatable-economy-v2:" + var10003 + ":" + var5.regionIndex);
                  Blocking var8 = (Blocking)var1.session.runtime.activity;
                  this.visible = false;
                  var1.session.runtime.activity = (new BattleThenResumeWorldRuntime(new BattleEntryTransitionThenRuntime(VqsvBattleRuntimeFactory.create(var7), 6, var7.mode().backgroundMode), var8, var1.session.world.resumeMode, "repeatable-challenge"));
                  RegionalRematchState.Status var9 = var1.session.progression.regionalRematches.recordStart(this.challengeRegion, var2);
                  List var10000 = var1.session.story.trace();
                  String var10001 = String.valueOf(var5.kind);
                  var10000.add("UNIFIED-DESIGN repeatable challenge start kind=" + var10001 + " region=" + var5.regionName + " encounter=" + var5.encounterKey + " reward=" + var5.rewardMoney + " rematchUsed=" + var9.usedCount + " rematchRemaining=" + var9.remainingUses() + " commissionUsed=" + var1.session.progression.regionalCommissionCount + " resumeActivity=" + (var8 == null ? "null" : var8.getClass().getSimpleName()));
               }
            }
         }
      }
   }

   private static RegionalRematchState.Status regionalRematchStatus(VqsvGameRuntime.Scene var0, int var1) {
      return regionalRematchStatus(var0, var1, WorldRealtimeClock.nowMillis());
   }

   private static RegionalRematchState.Status regionalRematchStatus(VqsvGameRuntime.Scene var0, int var1, long var2) {
      return var0.session.progression.regionalRematches.status(var1, var2);
   }

   private static String rematchDetail(VqsvGameRuntime.Scene var0, int var1, int var2) {
      RegionalRematchState.Status var3 = regionalRematchStatus(var0, var1);
      if (var3.clockRollback) {
         return "Thời gian thiết bị không hợp lệ; tái đấu tạm khóa.";
      } else if (var3.available) {
         int var10000 = var3.remainingUses();
         return "Mỗi Thủ lĩnh có 3 lượt mỗi ngày. NPC này còn " + var10000 + " lượt. Thưởng +" + var2 + " kim khi thắng.";
      } else {
         return "Đã dùng đủ 3 lượt với NPC này. Reset sau " + WorldRealtimeClock.displayRemaining(var3.remainingMillis) + ".";
      }
   }

   private void tickGiftCode(VqsvGameRuntime.Scene var1) {
      if (var1.keyBack) {
         this.mode = VqsvPanelRuntime.Mode.CHALLENGE;
         this.selected = 1;
         this.openedTicks = 0;
      } else if (var1.key0) {
         GiftCodeService.Result var2 = GIFT_CODE_SERVICE.redeem(var1, this.giftCodeInput, WorldRealtimeClock.nowMillis());
         this.giftCodeMessage = var2.message;
         this.openedTicks = 0;
         List var10000 = var1.session.story.trace();
         String var10001 = String.valueOf(var2.outcome);
         var10000.add("UNIFIED-DESIGN Giftcode UI result=" + var10001 + " code=" + var2.code);
      }
   }

   boolean textEntryActive() {
      return this.visible && this.mode == VqsvPanelRuntime.Mode.GIFT_CODE;
   }

   void typeText(VqsvGameRuntime.Scene var1, String var2) {
      if (this.textEntryActive() && var2 != null && !var2.isEmpty()) {
         boolean var3 = false;

         for(int var4 = 0; var4 < var2.length(); ++var4) {
            char var5 = var2.charAt(var4);
            if (var5 != '\b' && var5 != 127) {
               var5 = Character.toUpperCase(var5);
               boolean var6 = var5 >= 'A' && var5 <= 'Z' || var5 >= '0' && var5 <= '9' || var5 == '-' || var5 == '_';
               if (var6 && this.giftCodeInput.length() < 32) {
                  this.giftCodeInput = this.giftCodeInput + var5;
                  var3 = true;
               }
            } else if (!this.giftCodeInput.isEmpty()) {
               this.giftCodeInput = this.giftCodeInput.substring(0, this.giftCodeInput.length() - 1);
               var3 = true;
            }
         }

         if (var3) {
            this.giftCodeMessage = "Nhấn Enter hoặc Đổi quà để kiểm tra mã.";
            this.openedTicks = 0;
            var1.session.story.trace().add("PC_QOL Giftcode text edit length=" + this.giftCodeInput.length());
         }

      }
   }

   private void claimDailyBadges(VqsvGameRuntime.Scene var1) {
      long var2 = WorldRealtimeClock.nowMillis();
      DailyBadgeRewardService.ClaimResult var4 = DAILY_BADGE_REWARD_SERVICE.claim(var1.session.inventory.currency, var1.session.progression.dailyBadgeLastClaimEpochMillis, var2);
      if (var4.granted) {
         var1.session.progression.dailyBadgeLastClaimEpochMillis = var4.lastClaimEpochMillis;
         var1.text = TextBox.msgWarm("Đã nhận 20 Huy hiệu. Mời quay lại vào ngày mai.", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("UNIFIED-DESIGN daily badge reward granted nowEpoch=" + var2 + " badges=" + var4.badgesBefore + "->" + var4.badgesAfter);
      } else {
         var1.text = TextBox.msgWarm("Có thể nhận lại sau " + WorldRealtimeClock.displayRemaining(var4.status.remainingMillis) + ".", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("UNIFIED-DESIGN daily badge reward duplicate blocked nowEpoch=" + var2 + " lastClaimEpoch=" + var1.session.progression.dailyBadgeLastClaimEpochMillis + " remaining=" + var4.status.remainingMillis);
      }

      this.closePortableShopServiceConfirm(var1, var4.granted ? "daily-claim" : "daily-blocked");
   }

   private static String portableShopServiceTraceMetric(VqsvGameRuntime.Scene var0, int var1) {
      if (var1 == 3) {
         return " pets=" + var0.session.pets.roster.size();
      } else if (var1 == 4) {
         return " badges=" + var0.session.inventory.currency.badges;
      } else {
         return var1 == 2 ? " money=" + var0.session.inventory.currency.money : "";
      }
   }

   private void closePortableShopServiceConfirm(VqsvGameRuntime.Scene var1, String var2) {
      this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP;
      this.selected = portableShopProductRow(this.serviceProductId);
      this.openedTicks = 0;
      int var3 = this.serviceProductId;
      this.serviceProductId = -1;
      this.serviceConfirmTitle = "";
      this.serviceConfirmPrompt = "";
      var1.session.story.trace().add("PORTED/PARTIAL panel game.k.aD close smsInfo.ui product=" + var3 + " return bodyShop.ui reason=" + var2);
   }

   private static boolean allPortableShopPetsMaxLevel(VqsvGameRuntime.Scene var0) {
      return PORTABLE_SHOP_SERVICE.allPetsMaxLevel(var0.session.pets.roster);
   }

   private static void tracePortableShopLevelUpProduct3(VqsvGameRuntime.Scene var0, PortableShopService.LevelUpResult var1) {
      for(PortableShopService.LevelUpPetResult var3 : var1.pets) {
         EvolutionCandidate var4 = var3.candidate;
         if (var3.enqueued) {
            List var10000 = var0.session.story.trace();
            int var10001 = var3.petIndex;
            var10000.add("PORTED/PARTIAL panel product3 evolution candidate petIndex=" + var10001 + " species=" + var4.currentSpeciesId + " target=" + var4.targetSpeciesId + " level=" + var4.currentLevel + "/" + var4.requiredLevel + " materials=" + VqsvSourceEvolutionRuntime.materialSummary(var4) + " counts=" + VqsvSourceEvolutionRuntime.materialCountSummary(var0, var4));
         }

         var0.session.story.trace().add("PORTED panel an.b(true) product3 pet level index=" + var3.petIndex + " species=" + var3.speciesId + " level=" + var3.levelBefore + "->" + var3.levelAfter);
      }

      var0.session.story.trace().add("PORTED/PARTIAL panel an.b(true) product3 complete changed=" + var1.changed + " queue=" + var1.queueSize + " game.k.G=" + var1.sourceG + " game.k.L=[" + var1.selectionLevel + "," + var1.selectionSpecies + "]");
   }

   private static void tracePortableShopBadgeProduct4(VqsvGameRuntime.Scene var0, PortableShopService.CurrencyRewardResult var1) {
      var0.session.story.trace().add("PORTED panel an.b(true) product4 badges source game.g.o().u(10) badges=" + var1.before + "->" + var1.after);
   }

   private static void tracePortableShopMoneyProduct2(VqsvGameRuntime.Scene var0, PortableShopService.CurrencyRewardResult var1) {
      var0.session.story.trace().add("PORTED panel an.b(true) product2 money source game.g.o().s(10000) money=" + var1.before + "->" + var1.after);
   }

   private void renderTaskOption(Graphics2D var1, UiFont var2) {
      VqsvUiLayout var3 = VqsvUiLayout.load("taskOption.ui");
      SpriteAnimator var4 = SpriteAnimator.load(257);
      SpriteAnimator var5 = SpriteAnimator.load(258);
      this.drawTaskOptionFrame(var1, var3, var4);
      int[] var6 = new int[]{10, 11};
      int[] var7 = new int[]{7, 8};
      int[] var8 = new int[]{17, 18};

      for(int var9 = 0; var9 < var6.length && var9 < this.taskOptionData.options.length; ++var9) {
         VqsvUiLayout.UiWidget var10 = var3.widget(var6[var9]);
         if (var10 != null) {
            int var11 = var10.altId >= 0 ? var10.altId : var10.imageId;
            drawCellTopLeft(var4, var1, var11, var10.x, var10.y);
            if (var9 == this.selected) {
               VqsvUiLayout.UiWidget var12 = var3.widget(var7[var9]);
               if (var12 != null && var12.imageId >= 0) {
                  drawCellTopLeft(var4, var1, var12.imageId, var12.x, var12.y);
               }
            }
         }

         drawTextWide(var1, var2, var3, var8[var9], this.taskOptionData.option(var9), 0, var3.w(var8[var9], 60), var9 == this.selected ? colorSelected(var3.widget(var8[var9])) : color(var3.widget(var8[var9]), 1862801));
      }

      drawText(var1, var2, var3, 12, var3.text(12, "Thuong"), color(var3.widget(12), 13631758));
      int[] var14 = new int[]{13, 15};
      int[] var15 = new int[]{14, 16};

      for(int var16 = 0; var16 < var14.length && var16 < this.taskOptionData.rewards.length; ++var16) {
         TaskOptionReward var17 = this.taskOptionData.rewards[var16];
         VqsvUiLayout.UiWidget var13 = var3.widget(var14[var16]);
         if (var13 != null) {
            if (var17.iconSprite == 258) {
               drawCellTopLeft(var5, var1, var17.iconCell, var13.x, var13.y);
            } else {
               drawCellTopLeft(var4, var1, var17.iconCell, var13.x, var13.y);
            }
         }

         drawTextWide(var1, var2, var3, var15[var16], var17.label, 0, var3.w(var15[var16], 48), color(var3.widget(var15[var16]), 13631758));
      }

      if (this.taskOptionData.summary != null && !this.taskOptionData.summary.isEmpty()) {
         drawTextWide(var1, var2, var3, 21, this.taskOptionData.summary, 0, var3.w(21, 72), color(var3.widget(21), 13631758));
      }

      drawSoftkey(var1, var2, var3, var4, 19, var3.text(19, "Xac dinh"), 16777215);
      drawSoftkey(var1, var2, var3, var4, 20, var3.text(20, "Quay lai"), 16777215);
   }

   private void renderRecord(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("record.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      this.drawRecordFrame(var1, var2, var4, var5, this.recordSelected);
      drawTextWide(var1, var2, var4, 11, var4.text(11, "Hinh Kam"), -8, 156, color(var4.widget(11), 13631758));
      drawText(var1, var2, var4, 12, var4.text(12, "Bat duoc sung vat"), color(var4.widget(12), 1862801));
      drawText(var1, var2, var4, 14, String.valueOf(var3.session.pets.roster.size() + var3.session.pets.bank.size()), color(var4.widget(14), 16711680));
      drawText(var1, var2, var4, 15, var4.text(15, "Bat duoc sung vat"), color(var4.widget(15), 1862801));
      drawText(var1, var2, var4, 17, String.valueOf(var3.session.progression.collection.collectedSpeciesCount()), color(var4.widget(17), 16711680));
      drawText(var1, var2, var4, 18, var4.text(18, "Dat duoc sung vat hiem"), color(var4.widget(18), 1862801));
      drawText(var1, var2, var4, 20, String.valueOf(collectedSpeciesByRelationClass(var3, 1)), color(var4.widget(20), 16711680));
      drawText(var1, var2, var4, 24, var4.text(24, "Dat duoc than thu"), color(var4.widget(24), 1862801));
      drawText(var1, var2, var4, 26, String.valueOf(collectedSpeciesByRelationClass(var3, 2)), color(var4.widget(26), 16711680));
      drawText(var1, var2, var4, 27, var4.text(27, "Dat duoc huy hieu"), color(var4.widget(27), 1862801));
      drawText(var1, var2, var4, 29, String.valueOf(var3.session.inventory.currency.badges), color(var4.widget(29), 16711680));
      drawText(var1, var2, var4, 30, var4.text(30, "Tong thoi gian choi"), color(var4.widget(30), 1862801));
      drawText(var1, var2, var4, 31, sourcePlayTime(var3.session.progression.playTime.elapsedMillis()), color(var4.widget(31), 16711680));
      drawText(var1, var2, var4, 33, var4.text(33, "Xac dinh"), color(var4.widget(33), 16777215));
      drawText(var1, var2, var4, 34, var4.text(34, "Quay lai"), color(var4.widget(34), 16777215));
      this.drawRecordSelection(var1, var4, this.recordSelected);
   }

   private void renderPetmap(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("petmap.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      this.drawPetmapFrame(var1, var4, var5);
      drawTextWide(var1, var2, var4, 2, var4.text(2, "Minh hoa"), -28, 84, color(var4.widget(2), 13631758));

      for(int var6 = 0; var6 < PETMAP_TAB_CELLS.length; ++var6) {
         drawCellState(var4, var5, var1, PETMAP_TAB_CELLS[var6], var6 == this.petmapTab);
         drawTextWide(var1, var2, var4, PETMAP_TAB_LABELS[var6], PETMAP_TAB_NAMES[var6], -1, Math.max(1, var4.w(PETMAP_TAB_LABELS[var6], 12) - 6), var6 == this.petmapTab ? colorSelected(var4.widget(PETMAP_TAB_LABELS[var6])) : color(var4.widget(PETMAP_TAB_LABELS[var6]), 154));
      }

      List var12 = petmapRowsForRender(var3, this.petmapTab);
      int var7 = this.visibleListStart(var12.size());

      for(int var8 = 0; var8 < PETMAP_ROW_BACKGROUNDS.length; ++var8) {
         int var9 = var7 + var8;
         VqsvUiLayout.UiWidget var10 = var4.widget(PETMAP_ROW_BACKGROUNDS[var8]);
         if (var10 != null) {
            int var11 = var9 == this.selected ? var10.altId : var10.imageId;
            drawCellTopLeft(var5, var1, var11, var10.x, var10.y);
         }

         if (var9 < var12.size()) {
            PetmapRow var14 = (PetmapRow)var12.get(var9);
            drawCellTopLeft(var5, var1, var14.owned ? 101 : 102, var4.x(PETMAP_ROW_MARKERS[var8], 50), var4.y(PETMAP_ROW_MARKERS[var8], 101));
            drawText(var1, var2, var4, PETMAP_ROW_NAMES[var8], var14.name, var9 == this.selected ? 16753920 : color(var4.widget(PETMAP_ROW_NAMES[var8]), 1862801));
         }
      }

      if (!var12.isEmpty()) {
         PetmapRow var13 = (PetmapRow)var12.get(clamp(this.selected, 0, var12.size() - 1));
         drawText(var1, var2, var4, 20, PETMAP_TAB_NAMES[this.petmapTab] + " " + ownedCount(var12) + "/" + var12.size(), color(var4.widget(20), 1862801));
         if (var13.seen && var13.spriteId >= 0) {
            drawSpriteIdleTopLeft(var1, var13.spriteId, var4.x(21, 104), var4.y(21, 172));
         }
      }

      drawPetmapScrollbar(var1, var4, var12.size(), var7);
      drawText(var1, var2, var4, 49, var4.text(49, "Xac dinh"), color(var4.widget(49), 16777215));
      drawText(var1, var2, var4, 50, var4.text(50, "Quay lai"), color(var4.widget(50), 16777215));
   }

   private void renderBadge(Graphics2D var1, UiFont var2, VqsvGameRuntime.Scene var3) {
      VqsvUiLayout var4 = VqsvUiLayout.load("badge.ui");
      SpriteAnimator var5 = SpriteAnimator.load(257);
      this.drawBadgeFrame(var1, var4, var5);
      drawTextWide(var1, var2, var4, 5, var4.text(5, "Huy hieu"), 0, var4.w(5, 100), color(var4.widget(5), 13631758));
      drawSoftkey(var1, var2, var4, var5, 6, var4.text(6, "Quay lai"), 16777215);
      int var6 = badgeDisplayCount(var3);
      if (var6 > BADGE_SLOT_WIDGETS.length) {
         this.renderExtendedBadgeGrid(var1, var3, var5, var4);
      } else {
         for(int var7 = 0; var7 < BADGE_SLOT_WIDGETS.length; ++var7) {
            VqsvUiLayout.UiWidget var8 = var4.widget(BADGE_SLOT_WIDGETS[var7]);
            if (var8 != null) {
               int var9 = var7 == this.selected && var8.altId >= 0 ? var8.altId : var8.imageId;
               drawCellTopLeft(var5, var1, var9, var8.x, var8.y);
            }

            VqsvUiLayout.UiWidget var13 = var4.widget(BADGE_ICON_WIDGETS[var7]);
            if (var13 != null) {
               int var10 = sourceBadgeAchieved(var3, var7) ? 46 + var7 : (var13.altId >= 0 ? var13.altId : var13.imageId);
               drawCellTopLeft(var5, var1, var10, var13.x, var13.y);
            }
         }
      }

      int var11 = badgeRuntimeIdAtDisplayIndex(var3, this.selected);
      drawTextWide(var1, var2, var4, 13, badgeName(var11), 0, 68, color(var4.widget(13), 1862801));
      drawWrappedTextBox(var1, var2, var4, 14, badgeDescription(var3, var11), 68, 45, activeTextColor(var4.widget(14), 1862801));
      drawTextMarquee(var1, var2, var4, 15, var4.text(15, "Trang thai"), var4.w(15, 24), color(var4.widget(15), 2115924), this.openedTicks, -4);
      drawTextMarquee(var1, var2, var4, 16, badgeStatusText(var3, var11), var4.w(16, 40), color(var4.widget(16), 2115924), this.openedTicks, -4);
      if (sourceBadgeAchieved(var3, var11)) {
         BufferedImage var12 = badgePreviewImage(var5, var11);
         if (var12 != null) {
            var1.drawImage(var12, 125, 174, (ImageObserver)null);
         }
      } else {
         drawTextWide(var1, var2, var4, 33, badgeName(var11), 0, var4.w(33, 36), color(var4.widget(33), 1862801));
      }

   }

   private void drawMenuFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      fillBand(var1, var2, 2, 13037823, 7);
      fillBand(var1, var2, 3, 12511218, 102);
      fillBand(var1, var2, 4, 7124923, 8);
      drawCell(var2, var3, var1, 1);
      drawCell(var2, var3, var1, 13);
   }

   private void drawSystemFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      fillBand(var1, var2, 3, 13037823, 7);
      fillBand(var1, var2, 4, 12511218, 102);
      fillBand(var1, var2, 5, 7124923, 8);
      drawCell(var2, var3, var1, 1);
   }

   private void drawHelpFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      fillBand(var1, var2, 3, 13037823, 9);
      fillBand(var1, var2, 1, 12511218, 158);
      fillBand(var1, var2, 2, 7124923, 8);
      drawCell(var2, var3, var1, 4);
      drawCell(var2, var3, var1, 6);
   }

   private void drawOptionRow(Graphics2D var1, UiFont var2, VqsvUiLayout var3, SpriteAnimator var4, int var5, int var6, int var7, int var8, String var9) {
      VqsvUiLayout.UiWidget var10 = var3.widget(var6);
      if (var10 != null && var10.altId >= 0) {
         drawCellTopLeft(var4, var1, var10.altId, var10.x, var10.y);
         if (var5 == this.selected) {
            var1.setColor(new Color(16753920));
            var1.drawRect(var10.x - 1, var10.y - 1, Math.max(1, var10.w), 23);
         }
      }

      VqsvUiLayout.UiWidget var11 = var3.widget(var8);
      if (var5 == this.selected && var11 != null && var11.imageId >= 0) {
         drawCellTopLeft(var4, var1, var11.imageId, var11.x, var11.y);
      }

      drawText(var1, var2, var3, var7, var9, var5 == this.selected ? 16753920 : color(var3.widget(var7), 1862801));
   }

   private void drawBagFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      fillBand(var1, var2, 3, 13037823, 9);
      fillBand(var1, var2, 1, 12511218, 159);
      fillBand(var1, var2, 2, 7124923, 8);
      drawCell(var2, var3, var1, 4);
      drawCell(var2, var3, var1, 6);
      drawCell(var2, var3, var1, 7);

      for(int var4 = 9; var4 <= 12; ++var4) {
         drawCell(var2, var3, var1, var4);
      }

      fillBand(var1, var2, 13, 12441807, 14);
      drawCell(var2, var3, var1, 44);
      drawCell(var2, var3, var1, 45);
      drawCell(var2, var3, var1, 41);
      fillBand(var1, var2, 42, 5361769, 72);
   }

   private void drawTaskFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      drawCell(var2, var3, var1, 1);
      fillBand(var1, var2, 3, 12445911, 10);
      fillBand(var1, var2, 4, 12445911, 13);
      fillBand(var1, var2, 5, 12445911, 93);
      drawCell(var2, var3, var1, 35);
      fillBand(var1, var2, 39, 5361769, 72);
      drawCell(var2, var3, var1, 41);
      drawCell(var2, var3, var1, 42);
   }

   private void drawTaskOptionFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      fillBand(var1, var2, 2, 13037823, 7);
      fillBand(var1, var2, 3, 12445911, 99);
      fillBand(var1, var2, 4, 5361769, 8);
      drawCell(var2, var3, var1, 1);
      drawCell(var2, var3, var1, 9);
      drawCell(var2, var3, var1, 19);
      drawCell(var2, var3, var1, 20);
   }

   private void drawRecordFrame(Graphics2D var1, UiFont var2, VqsvUiLayout var3, SpriteAnimator var4, int var5) {
      drawCell(var3, var4, var1, 1);
      fillBand(var1, var3, 2, 12445911, 7);
      fillBand(var1, var3, 3, 5361769, 132);
      VqsvUiLayout.UiWidget var6 = var3.widget(4);
      VqsvUiLayout.UiWidget var7 = var3.widget(6);
      if (var6 != null && var7 != null) {
         var1.setColor(new Color(5361769));
         var1.fillRect(var6.x - 8, var6.y - 5, 144, Math.max(34, var7.y - var6.y + 18));
      }

      drawRecordOptionButton(var1, var2, var3, var4, 4, 6, "Hệ thống", "Sủng vật", var5 == 0);
      drawRecordOptionButton(var1, var2, var3, var4, 32, 7, "Hệ thống", "Huy chương", var5 == 1);
      drawCell(var3, var4, var1, 33);
      drawCell(var3, var4, var1, 34);
   }

   private static void drawRecordOptionButton(Graphics2D var0, UiFont var1, VqsvUiLayout var2, SpriteAnimator var3, int var4, int var5, String var6, String var7, boolean var8) {
      VqsvUiLayout.UiWidget var9 = var2.widget(var4);
      if (var9 != null) {
         var0.setColor(new Color(var8 ? 16771174 : 16773786));
         var0.fillRect(var9.x, var9.y, Math.max(1, var9.w), 24);
         var0.setColor(new Color(var8 ? 16742912 : 12152832));
         var0.drawRect(var9.x, var9.y, Math.max(1, var9.w), 24);
         drawCenteredLine(var0, var1, var6, var9.x, var9.y + 3, Math.max(1, var9.w), 1453095);
         drawCenteredLine(var0, var1, var7, var9.x, var9.y + 13, Math.max(1, var9.w), 1453095);
         if (var8) {
            VqsvUiLayout.UiWidget var10 = var2.widget(var5);
            if (var10 != null && var10.imageId >= 0) {
               drawCellTopLeft(var3, var0, var10.imageId, var10.x, var10.y);
            }
         }

      }
   }

   private static void drawCenteredLine(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6) {
      int var7 = var1.taggedWidth(var2);
      int var8 = var3 + Math.max(0, (var5 - var7) / 2);
      var1.drawTaggedLine(var0, var2, var8, var4, TextBox.visibleLength(TextBox.decodeMojibake(var2)), var6);
   }

   private void drawRecordSelection(Graphics2D var1, VqsvUiLayout var2, int var3) {
      VqsvUiLayout.UiWidget var4 = var2.widget(4);
      VqsvUiLayout.UiWidget var5 = var2.widget(32);
      var1.setColor(new Color(16753920));
      if (var3 == 0 && var4 != null) {
         var1.drawRect(var4.x - 1, var4.y - 1, 65, 22);
      } else if (var3 == 1 && var5 != null) {
         var1.drawRect(var5.x - 1, var5.y - 1, 65, 22);
      }

   }

   private void drawPetmapFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      drawCell(var2, var3, var1, 1);
      fillBand(var1, var2, 3, 12445911, 15);
      fillBand(var1, var2, 4, 12445911, 8);
      fillBand(var1, var2, 5, 12445911, 93);
      fillBand(var1, var2, 22, 5361769, 72);
      drawCell(var2, var3, var1, 49);
      drawCell(var2, var3, var1, 50);
   }

   private void drawBadgeFrame(Graphics2D var1, VqsvUiLayout var2, SpriteAnimator var3) {
      fillBand(var1, var2, 3, 13037823, 9);
      fillBand(var1, var2, 1, 12511218, 159);
      fillBand(var1, var2, 2, 7124923, 8);
      drawCell(var2, var3, var1, 4);
      drawCell(var2, var3, var1, 6);
      drawCell(var2, var3, var1, 8);
      drawCell(var2, var3, var1, 9);
      drawCell(var2, var3, var1, 10);
      drawCell(var2, var3, var1, 11);
      drawCell(var2, var3, var1, 12);
   }

   private void drawRows(Graphics2D var1, UiFont var2, VqsvUiLayout var3, SpriteAnimator var4, int[] var5, String[] var6) {
      for(int var7 = 0; var7 < var5.length; ++var7) {
         int var8 = var5[var7];
         VqsvUiLayout.UiWidget var9 = var3.widget(var8);
         if (var9 != null) {
            int var10 = var7 == this.selected ? var9.altId : var9.imageId;
            drawCellTopLeft(var4, var1, var10, var9.x, var9.y);
            drawText(var1, var2, var3, var8, var6[var7], var7 == this.selected ? colorSelected(var9) : color(var9, 1862802));
         }
      }

   }

   private void drawGameMenuRows(Graphics2D var1, UiFont var2, VqsvUiLayout var3, SpriteAnimator var4) {
      int var5 = clamp(this.listScroll, 0, Math.max(0, MENU_LABELS.length - 6));

      for(int var6 = 0; var6 < 6; ++var6) {
         int var7 = var5 + var6;
         if (var7 >= MENU_LABELS.length) {
            break;
         }

         int var8 = MENU_ROW_WIDGETS[var6];
         VqsvUiLayout.UiWidget var9 = var3.widget(var8);
         if (var9 != null) {
            int var10 = var7 == this.selected ? var9.altId : var9.imageId;
            drawCellTopLeft(var4, var1, var10, var9.x, var9.y);
            drawText(var1, var2, var3, var8, MENU_LABELS[var7], var7 == this.selected ? colorSelected(var9) : color(var9, 1862802));
         }
      }

   }

   private void drawGameMenuScrollbar(Graphics2D var1) {
      if (MENU_LABELS.length > 6) {
         var1.setColor(new Color(7124923));
         var1.fillRect(157, 105, 4, 100);
         int var2 = UiScrollbarMath.thumbHeight(100, MENU_LABELS.length, 6, 18);
         int var3 = UiScrollbarMath.thumbY(105, 100, var2, MENU_LABELS.length, 6, this.listScroll);
         var1.setColor(new Color(13037823));
         var1.fillRect(157, var3, 4, var2);
         var1.setColor(new Color(1862801));
         var1.drawRect(157, var3, 3, Math.max(1, var2 - 1));
      }
   }

   private static void fillBand(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      VqsvUiLayout.UiWidget var5 = var1.widget(var2);
      if (var5 != null) {
         int var6 = var1.bandHeight(var2, var4);
         int var7 = var5.jColor != 0 && var5.jColor != -1 ? var5.jColor & 16777215 : var3;
         var0.setColor(new Color(var7));
         var0.fillRect(var5.x, var5.y, Math.max(1, var5.w), Math.max(1, var6));
      }
   }

   private static void drawBagScrollbar(Graphics2D var0, VqsvUiLayout var1, int var2, int var3, int var4) {
      int var5 = var4 == 1 ? 83 : (var4 == 2 ? 122 : 42);
      int var6 = var4 == 1 ? 84 : (var4 == 2 ? 123 : 43);
      VqsvUiLayout.UiWidget var7 = var1.widget(var5);
      VqsvUiLayout.UiWidget var8 = var1.widget(var6);
      if (var7 != null && var8 != null) {
         byte var9 = 72;
         var0.setColor(new Color((var7.jColor != 0 && var7.jColor != -1 ? var7.jColor : 5361769) & 16777215));
         var0.fillRect(var7.x, var7.y, Math.max(1, var7.w), var9);
         int var10 = UiScrollbarMath.thumbHeight(var9, var2, 5, 8);
         int var11 = UiScrollbarMath.thumbY(var7.y, var9, var10, var2, 5, var3);
         var0.setColor(new Color((var8.jColor != 0 && var8.jColor != -1 ? var8.jColor : 13037823) & 16777215));
         var0.fillRect(var8.x, var11, Math.max(1, var8.w), Math.max(8, var10));
      }
   }

   private static void drawTaskScrollbar(Graphics2D var0, VqsvUiLayout var1, int var2, int var3) {
      VqsvUiLayout.UiWidget var4 = var1.widget(39);
      VqsvUiLayout.UiWidget var5 = var1.widget(40);
      if (var4 != null && var5 != null) {
         byte var6 = 72;
         var0.setColor(new Color((var4.jColor != 0 && var4.jColor != -1 ? var4.jColor : 5361769) & 16777215));
         var0.fillRect(var4.x, var4.y, Math.max(1, var4.w), var6);
         int var7 = UiScrollbarMath.thumbHeight(var6, var2, 5, 8);
         int var8 = UiScrollbarMath.thumbY(var4.y, var6, var7, var2, 5, var3);
         var0.setColor(new Color((var5.jColor != 0 && var5.jColor != -1 ? var5.jColor : 13037823) & 16777215));
         var0.fillRect(var5.x, var8, Math.max(1, var5.w), Math.max(8, var7));
      }
   }

   private static void drawShopbuyScrollbar(Graphics2D var0, VqsvUiLayout var1, int var2, int var3) {
      VqsvUiLayout.UiWidget var4 = var1.widget(38);
      if (var4 != null && var2 > 5) {
         byte var5 = 84;
         int var6 = UiScrollbarMath.thumbHeight(var5, var2, 5, 8);
         int var7 = UiScrollbarMath.thumbY(var4.y, var5, var6, var2, 5, var3);
         var0.setColor(new Color((var4.jColor != 0 && var4.jColor != -1 ? var4.jColor : 5363945) & 16777215));
         var0.fillRect(var4.x, var4.y, Math.max(1, var4.w), var5);
         var0.setColor(new Color(13038591));
         var0.fillRect(var4.x, var7, Math.max(1, var4.w), var6);
      }
   }

   private static void drawPetmapScrollbar(Graphics2D var0, VqsvUiLayout var1, int var2, int var3) {
      VqsvUiLayout.UiWidget var4 = var1.widget(22);
      VqsvUiLayout.UiWidget var5 = var1.widget(23);
      if (var4 != null && var5 != null) {
         byte var6 = 72;
         var0.setColor(new Color((var4.jColor != 0 && var4.jColor != -1 ? var4.jColor : 5361769) & 16777215));
         var0.fillRect(var4.x, var4.y, Math.max(1, var4.w), var6);
         int var7 = UiScrollbarMath.thumbHeight(var6, var2, 5, 8);
         int var8 = UiScrollbarMath.thumbY(var4.y, var6, var7, var2, 5, var3);
         var0.setColor(new Color((var5.jColor != 0 && var5.jColor != -1 ? var5.jColor : 13037823) & 16777215));
         var0.fillRect(var5.x, var8, Math.max(1, var5.w), Math.max(8, var7));
      }
   }

   private void drawTransmitScrollbar(Graphics2D var1, VqsvUiLayout var2, int var3) {
      VqsvUiLayout.UiWidget var4 = var2.widget(12);
      VqsvUiLayout.UiWidget var5 = var2.widget(13);
      if (var4 != null && var5 != null) {
         byte var6 = 88;
         var1.setColor(new Color((var4.jColor != 0 && var4.jColor != -1 ? var4.jColor : 27491) & 16777215));
         var1.fillRect(var4.x, var4.y, Math.max(1, var4.w), var6);
         int var7 = UiScrollbarMath.thumbHeight(var6, TRANSMIT_DESTINATIONS.length, 5, 10);
         int var8 = UiScrollbarMath.thumbY(var4.y, var6, var7, TRANSMIT_DESTINATIONS.length, 5, var3);
         var1.setColor(new Color((var5.jColor != 0 && var5.jColor != -1 ? var5.jColor : 13037823) & 16777215));
         var1.fillRect(var5.x, var8, Math.max(1, var5.w), var7);
      }
   }

   private static void drawText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5) {
      drawTextWide(var0, var1, var2, var3, var4, 0, var2.w(var3, Math.max(1, var1.taggedWidth(var4))), var5);
   }

   private static void drawTaskMapLine(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5) {
      if (var2 != null && !var2.isEmpty()) {
         String var6 = TextBox.decodeMojibake(var2);
         var1.drawTaggedLine(var0, var6, var3, var4, TextBox.visibleLength(var6), var5);
      }
   }

   private static void drawTaskMapCentered(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6) {
      if (var2 != null && !var2.isEmpty()) {
         String var7 = TextBox.decodeMojibake(var2);
         int var8 = var3 + Math.max(0, (var5 - var1.taggedWidth(var7)) / 2);
         var1.drawTaggedLine(var0, var7, var8, var4, TextBox.visibleLength(var7), var6);
      }
   }

   private static void drawTaskMapTwoLineLabel(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6) {
      Shape var7 = var0.getClip();
      var0.clipRect(var3, var4, Math.max(1, var5), var1.height * 2 + 2);
      List var8 = wrapText(var1, var2, var5);
      int var9 = var4;

      for(int var10 = 0; var10 < Math.min(2, var8.size()); ++var10) {
         drawTaskMapCentered(var0, var1, (String)var8.get(var10), var3, var9, var5, var6);
         var9 += var1.height + 1;
      }

      var0.setClip(var7);
   }

   private static void drawTaskMapMarquee(Graphics2D var0, UiFont var1, String var2, int var3, int var4, int var5, int var6, int var7) {
      if (var2 != null && !var2.isEmpty()) {
         String var8 = TextBox.decodeMojibake(var2);
         int var9 = var1.taggedWidth(var8);
         int var10 = 0;
         if (var9 > var5) {
            int var11 = var9 - var5;
            int var12 = Math.max(0, var7 - 18) / 3;
            int var13 = Math.max(1, var11 * 2 + 36);
            int var14 = var12 % var13;
            var10 = var14 <= var11 ? var14 : Math.max(0, var13 - var14 - 18);
            var10 = Math.min(var11, var10);
         }

         Shape var16 = var0.getClip();
         var0.clipRect(var3, var4 - 1, Math.max(1, var5), var1.height + 2);
         var1.drawTaggedLine(var0, var8, var3 - var10, var4, TextBox.visibleLength(var8), var6);
         var0.setClip(var16);
      }
   }

   private static void drawMultilineText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5) {
      VqsvUiLayout.UiWidget var6 = var2.widget(var3);
      if (var6 != null && var4 != null && !var4.isEmpty()) {
         Shape var7 = var0.getClip();
         var0.clipRect(var6.x, var6.y - 1, Math.max(1, var6.w) + 8, Math.max(12, var2.h(var3, 112)));
         String[] var8 = var4.split("#n", -1);
         int var9 = var6.y;

         for(String var13 : var8) {
            if (!var13.isEmpty()) {
               var1.drawTaggedLine(var0, var13, var6.x, var9, TextBox.visibleLength(TextBox.decodeMojibake(var13)), var5);
            }

            var9 += 14;
         }

         var0.setClip(var7);
      }
   }

   private static void drawWrappedTextBox(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, int var6, int var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var3);
      if (var8 != null && var4 != null && !var4.isEmpty()) {
         int var9 = Math.max(1, var5);
         int var10 = Math.max(var1.height + 1, var6);
         Shape var11 = var0.getClip();
         var0.clipRect(var8.x, var8.y, var9, var10);
         int var12 = var8.y;

         for(String var16 : var4.split("#n", -1)) {
            for(String var18 : wrapText(var1, var16, var9)) {
               if (var12 > var8.y + var10 - var1.height) {
                  var0.setClip(var11);
                  return;
               }

               if (!var18.isEmpty()) {
                  var1.drawTaggedLine(var0, var18, var8.x, var12, TextBox.visibleLength(TextBox.decodeMojibake(var18)), var7);
               }

               var12 += var1.height + 1;
            }
         }

         var0.setClip(var11);
      }
   }

   private static void drawWrappedTextBoxScrolled(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, int var6, int var7, int var8) {
      VqsvUiLayout.UiWidget var9 = var2.widget(var3);
      if (var9 != null && var4 != null && !var4.isEmpty()) {
         int var10 = Math.max(1, var5);
         int var11 = Math.max(var1.height + 1, var6);
         ArrayList<String> var12 = new ArrayList<>();

         for(String var16 : var4.split("#n", -1)) {
            var12.addAll(wrapText(var1, var16, var10));
         }

         int var20 = var1.height + 1;
         int var21 = var12.size() * var20;
         int var22 = 0;
         if (var21 > var11) {
            int var24 = Math.max(1, var21 - var11 + var20);
            var22 = Math.max(0, var8 - 18) / 3;
            var22 %= var24;
         }

         Shape var25 = var0.getClip();
         var0.clipRect(var9.x, var9.y, var10, var11);
         int var17 = var9.y - var22;

         for(String var19 : var12) {
            if (var17 > var9.y + var11) {
               break;
            }

            if (var17 >= var9.y - var1.height && !var19.isEmpty()) {
               var1.drawTaggedLine(var0, var19, var9.x, var17, TextBox.visibleLength(TextBox.decodeMojibake(var19)), var7);
            }

            var17 += var20;
         }

         var0.setClip(var25);
      }
   }

   private static List<String> wrapText(UiFont var0, String var1, int var2) {
      ArrayList var3 = new ArrayList();
      String var4 = TextBox.decodeMojibake(var1 == null ? "" : var1).trim();
      if (var4.isEmpty()) {
         var3.add("");
         return var3;
      } else {
         StringBuilder var5 = new StringBuilder();

         for(String var9 : var4.split("\\s+")) {
            String var10 = var5.length() == 0 ? var9 : String.valueOf(var5) + " " + var9;
            if (var0.taggedWidth(var10) > var2 && var5.length() != 0) {
               var3.add(var5.toString());
               var5.setLength(0);
               var5.append(var9);
            } else {
               var5.setLength(0);
               var5.append(var10);
            }
         }

         if (var5.length() > 0) {
            var3.add(var5.toString());
         }

         return var3;
      }
   }

   private static void drawTextMarquee(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, int var6, int var7) {
      drawTextMarquee(var0, var1, var2, var3, var4, var5, var6, var7, 0);
   }

   private static void drawTextMarquee(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, int var6, int var7, int var8) {
      VqsvUiLayout.UiWidget var9 = var2.widget(var3);
      if (var9 != null && var4 != null && !var4.isEmpty()) {
         int var10 = Math.max(1, var5);
         int var11 = var1.taggedWidth(var4);
         Shape var12 = var0.getClip();
         int var13 = var9.y + var8;
         var0.clipRect(var9.x, var13 - 1, var10, Math.max(12, var2.h(var3, 12) - Math.min(0, var8)));
         int var14 = var9.x;
         if (var11 > var10) {
            int var15 = Math.max(0, var7 * 2);
            if (var15 > var11 - var10) {
               var15 = -var10 + (var15 - (var11 - var10)) % Math.max(1, var11 + var10);
            }

            var14 = var9.x - var15;
         } else if (var9.b == 4) {
            var14 = var9.x + Math.max(0, (var10 - var11) / 2);
         }

         var1.drawTaggedLine(var0, var4, var14, var13, TextBox.visibleLength(TextBox.decodeMojibake(var4)), var6);
         var0.setClip(var12);
      }
   }

   private static void drawTextWide(Graphics2D var0, UiFont var1, VqsvUiLayout var2, int var3, String var4, int var5, int var6, int var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var3);
      if (var8 != null && var4 != null && !var4.isEmpty()) {
         int var9 = Math.max(1, var6);
         Shape var10 = var0.getClip();
         var0.clipRect(var8.x + var5, var8.y - 1, var9 + 6, Math.max(12, var2.h(var3, 12)));
         int var11 = var8.x + var5;
         if (var8.b == 4) {
            int var12 = var1.taggedWidth(var4);
            var11 = var8.x + var5 + Math.max(0, (var9 - var12) / 2);
         }

         var1.drawTaggedLine(var0, var4, var11, var8.y, TextBox.visibleLength(TextBox.decodeMojibake(var4)), var7);
         var0.setClip(var10);
      }
   }

   private static void drawBagBottomActionText(Graphics2D var0, UiFont var1, VqsvUiLayout var2, SpriteAnimator var3, int var4, String var5, int var6, boolean var7) {
      VqsvUiLayout.UiWidget var8 = var2.widget(var4);
      if (var8 != null && var5 != null && !var5.isEmpty()) {
         String var9 = TextBox.decodeMojibake(var5);
         int var10 = var8.altId >= 0 ? var8.altId : var8.imageId;
         int[] var11 = var3 != null && var10 >= 0 ? var3.cellBounds(var10) : null;
         int var12 = var8.x;
         int var13 = var11 == null ? Math.max(1, var2.w(var4, 43)) : var11[2];
         int var14 = var11 == null ? Math.max(14, var2.h(var4, 22)) : var11[3];
         int var15 = var8.y + Math.max(0, (var14 - var1.height) / 2) + 1;
         Shape var16 = var0.getClip();
         var0.clipRect(var12, var8.y, var13, Math.max(12, var14));
         int var17 = var1.taggedWidth(var9);
         int var18 = var12 + Math.max(0, (var13 - var17) / 2);
         var1.drawTaggedLine(var0, var9, var18, var15, TextBox.visibleLength(var9), var6);
         var0.setClip(var16);
      }
   }

   private static void drawSoftkey(Graphics2D var0, UiFont var1, VqsvUiLayout var2, SpriteAnimator var3, int var4, String var5, int var6) {
      VqsvUiLayout.UiWidget var7 = var2.widget(var4);
      if (var7 != null) {
         int var8 = var7.altId >= 0 ? var7.altId : var7.imageId;
         int[] var9 = var3.cellBounds(var8);
         int var10 = var9 == null ? Math.max(1, var2.w(var4, 43)) : var9[2];
         int var11 = var9 == null ? Math.max(12, var2.h(var4, 20)) : var9[3];
         drawCellTopLeft(var3, var0, var8, var7.x, var7.y);
         if (var5 != null && !var5.isEmpty()) {
            Shape var12 = var0.getClip();
            var0.clipRect(var7.x, var7.y - 1, Math.max(1, var10), Math.max(12, var11));
            int var13 = var1.taggedWidth(var5);
            int var14 = var7.x + (var10 - var13) / 2;
            int var15 = var7.y + Math.max(0, (var11 - var1.height) / 2) + 1;
            int var16 = var7.lColor == -1 ? 16777215 : color(var7, var6);
            var1.drawTaggedLine(var0, var5, var14, var15, TextBox.visibleLength(TextBox.decodeMojibake(var5)), var16);
            var0.setClip(var12);
         }
      }
   }

   private static void drawCell(VqsvUiLayout var0, SpriteAnimator var1, Graphics2D var2, int var3) {
      VqsvUiLayout.UiWidget var4 = var0.widget(var3);
      if (var4 != null) {
         int var5 = var4.altId >= 0 ? var4.altId : var4.imageId;
         drawCellTopLeft(var1, var2, var5, var4.x, var4.y);
      }
   }

   private static void drawCellState(VqsvUiLayout var0, SpriteAnimator var1, Graphics2D var2, int var3, boolean var4) {
      VqsvUiLayout.UiWidget var5 = var0.widget(var3);
      if (var5 != null) {
         int var6 = var4 && var5.altId >= 0 ? var5.altId : var5.imageId;
         drawCellTopLeft(var1, var2, var6, var5.x, var5.y);
      }
   }

   private static void drawCellTopLeft(SpriteAnimator var0, Graphics2D var1, int var2, int var3, int var4) {
      int[] var5 = var0.cellBounds(var2);
      if (var5 != null) {
         var0.drawCell(var1, var2, var3 - var5[0], var4 - var5[1], 0);
      }
   }

   private static void drawSpriteCellTopLeft(Graphics2D var0, int var1, int var2, int var3, int var4) {
      SpriteAnimator var5 = SpriteAnimator.load(var1);
      int[] var6 = var5.cellBounds(var2);
      if (var6 != null) {
         var5.drawCell(var0, var2, var3 - var6[0], var4 - var6[1], 0);
      }
   }

   private static void drawSpriteIdleTopLeft(Graphics2D var0, int var1, int var2, int var3) {
      SpriteAnimator var4 = SpriteAnimator.load(var1);
      var4.setState(0);
      int var5 = var4.cellIdAtFrame(0);
      int[] var6 = var4.cellBounds(var5);
      if (var6 != null) {
         var4.drawCell(var0, var5, var2 - var6[0], var3 - var6[1], 0);
      }
   }

   private static int color(VqsvUiLayout.UiWidget var0, int var1) {
      return var0 != null && var0.lColor != 0 && var0.lColor != -1 ? var0.lColor & 16777215 : var1;
   }

   private static int colorSelected(VqsvUiLayout.UiWidget var0) {
      return var0 != null && var0.jColor != 0 && var0.jColor != -1 ? var0.jColor & 16777215 : 16753920;
   }

   private static int activeTextColor(VqsvUiLayout.UiWidget var0, int var1) {
      return var0 != null && var0.gColor != 0 && var0.gColor != -1 ? var0.gColor & 16777215 : var1;
   }

   private static int sourceTargetState(int var0) {
      switch (var0) {
         case 0 -> {
            return 14;
         }
         case 1 -> {
            return 7;
         }
         case 2 -> {
            return 8;
         }
         case 3 -> {
            return 9;
         }
         case 4 -> {
            return 10;
         }
         case 5 -> {
            return 22;
         }
         default -> {
            return -1;
         }
      }
   }

   private static int sourceSystemTargetState(int var0) {
      switch (var0) {
         case 0 -> {
            return 0;
         }
         case 1 -> {
            return 20;
         }
         case 2 -> {
            return 21;
         }
         case 3 -> {
            return 7;
         }
         default -> {
            return -1;
         }
      }
   }

   private static String bagTabTitle(int var0) {
      switch (var0) {
         case 0 -> {
            return "Vat pham";
         }
         case 1 -> {
            return "Trang suc";
         }
         case 2 -> {
            return "Tai lieu";
         }
         case 3 -> {
            return "Dac thu";
         }
         default -> {
            return "Unknown";
         }
      }
   }

   private String bagActionLabel(VqsvGameRuntime.Scene var1) {
      List var2 = bagRows(var1, this.bagTab);
      if (!var2.isEmpty()) {
         BagRow var3 = (BagRow)var2.get(clamp(this.selected, 0, var2.size() - 1));
         if (!var3.item.mechanicsImplemented) {
            return "Su dung";
         }
      }

      if (this.bagTab == 2) {
         return "";
      } else if (this.bagTab != 3) {
         return "Sử dụng";
      } else if (var2.isEmpty()) {
         return "";
      } else {
         BagRow var4 = (BagRow)var2.get(clamp(this.selected, 0, var2.size() - 1));
         if (var4.specialEgg) {
            return var1.session.progression.egg.active ? "Ap trung" : "Mo ra";
         } else if (var4.specialId != 5 && var4.specialId != 6 && var4.specialId != 10) {
            return var4.specialId != 7 && var4.specialId != 8 && var4.specialId != 9 ? "" : "Su dung";
         } else {
            return "Mo ra";
         }
      }
   }

   private String sourceTickMethod() {
      if (this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM) {
         return "game.h.n";
      } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return "game.h.ac";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return "game.h.S";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return "task.map";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return "game.k.aG";
      } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
         return "game.h.O";
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         return "game.h.Q";
      } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
         return "game.h.X";
      } else if (this.mode == VqsvPanelRuntime.Mode.SAVE) {
         return "game.h.K";
      } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
         return "game.h.v";
      } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
         return "game.h.x";
      } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
         return "game.h.n";
      } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         return "game.h.ae";
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return "game.k.i";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
         return "game.k.aD";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         return "game.k.a(" + this.sourceShopTraceContext() + ")";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         return "game.k.a(" + this.sourceShopTraceContext() + ").msgyn";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
         return "game.k.aD.product" + this.serviceProductId + ".smsInfo";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
         return "game.k.aP";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
         return "game.k.aT";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
         return "game.k.G";
      } else if (this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL && this.mode != VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
            return "game.k.G";
         } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
            return "game.k.E";
         } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
            return "FASH-I2";
         } else if (this.mode != VqsvPanelRuntime.Mode.FASHION_SHOP && this.mode != VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM && this.mode != VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM && this.mode != VqsvPanelRuntime.Mode.FASHION_REVEAL && this.mode != VqsvPanelRuntime.Mode.FASHION_EXCHANGE && this.mode != VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
            if (this.mode == VqsvPanelRuntime.Mode.CHALLENGE) {
               return "UNIFIED-REPEATABLE-ECONOMY";
            } else {
               return this.mode == VqsvPanelRuntime.Mode.GIFT_CODE ? "UNIFIED-GIFTCODE" : "game.h.l";
            }
         } else {
            return "FASH-I3-I3";
         }
      } else {
         return "game.k.P";
      }
   }

   private void renderExtendedBadgeGrid(Graphics2D var1, VqsvGameRuntime.Scene var2, SpriteAnimator var3, VqsvUiLayout var4) {
      VqsvUiLayout.UiWidget var5 = var4.widget(BADGE_SLOT_WIDGETS[0]);
      int var6 = var5 == null ? 36 : var5.imageId;
      int var7 = var5 != null && var5.altId >= 0 ? var5.altId : 37;
      int[] var8 = badgeDisplayIds(var2);
      int var9 = badgeGridFirst(this.selected, var8.length);
      int var10 = Math.min(10, var8.length - var9);

      for(int var11 = 0; var11 < var10; ++var11) {
         int var12 = var9 + var11;
         int var13 = var8[var12];
         int var14 = badgeGridX(var11, var10);
         int var15 = badgeGridY(var11);
         drawCellTopLeft(var3, var1, var12 == this.selected ? var7 : var6, var14, var15);
         UnifiedBadgeCatalog.Record var16 = UnifiedBadgeCatalog.instance().byRuntimeId(var13);
         if (var16 != null) {
            BufferedImage var17 = UnifiedBadgeIconRenderer.image(var16);
            if (var17 != null) {
               RenderingHints var18 = (RenderingHints)var1.getRenderingHints().clone();
               var1.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
               var1.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               var1.drawImage(var17, var14 + 1, var15 + 1, 28, 28, (ImageObserver)null);
               var1.setRenderingHints(var18);
            }
         } else {
            int var19 = sourceBadgeAchieved(var2, var13) ? 46 + var13 : 54 + var13;
            int[] var20 = var3.cellBounds(var19);
            if (var20 != null) {
               drawCellTopLeft(var3, var1, var19, var14 + (30 - var20[2]) / 2, var15 + (30 - var20[3]) / 2);
            }
         }
      }

   }

   private static BufferedImage badgePreviewImage(SpriteAnimator var0, int var1) {
      BufferedImage var2 = (BufferedImage)BADGE_PREVIEW_CACHE.get(var1);
      if (var2 != null) {
         return var2;
      } else {
         UnifiedBadgeCatalog.Record var3 = UnifiedBadgeCatalog.instance().byRuntimeId(var1);
         BufferedImage var4;
         if (var3 != null) {
            var4 = scaleBadgeImage(UnifiedBadgeIconRenderer.image(var3), 64, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         } else {
            if (var1 < 0 || var1 >= 8) {
               return null;
            }

            var4 = sourceBadgeCellImage(var0, 46 + var1, 64);
         }

         if (var4 != null) {
            BADGE_PREVIEW_CACHE.put(var1, var4);
         }

         return var4;
      }
   }

   private static BufferedImage scaleBadgeImage(BufferedImage var0, int var1, Object var2) {
      if (var0 != null && var0.getWidth() > 0 && var0.getHeight() > 0) {
         int var3 = Math.max(1, var1 - 4);
         double var4 = Math.min((double)var3 / (double)var0.getWidth(), (double)var3 / (double)var0.getHeight());
         int var6 = Math.max(1, (int)Math.round((double)var0.getWidth() * var4));
         int var7 = Math.max(1, (int)Math.round((double)var0.getHeight() * var4));
         BufferedImage var8 = new BufferedImage(var1, var1, 2);
         Graphics2D var9 = var8.createGraphics();

         try {
            var9.setRenderingHint(RenderingHints.KEY_INTERPOLATION, var2);
            var9.drawImage(var0, (var1 - var6) / 2, (var1 - var7) / 2, var6, var7, (ImageObserver)null);
         } finally {
            var9.dispose();
         }

         return var8;
      } else {
         return null;
      }
   }

   private static BufferedImage sourceBadgeCellImage(SpriteAnimator var0, int var1, int var2) {
      int[] var3 = var0.cellBounds(var1);
      if (var3 != null && var3[2] > 0 && var3[3] > 0) {
         BufferedImage var4 = new BufferedImage(var3[2], var3[3], 2);
         Graphics2D var5 = var4.createGraphics();

         try {
            var0.drawCell(var5, var1, -var3[0], -var3[1], 0);
         } finally {
            var5.dispose();
         }

         int var6 = Math.max(1, var2 - 4);
         double var7 = Math.min((double)var6 / (double)var3[2], (double)var6 / (double)var3[3]);
         int var9 = Math.max(1, (int)Math.round((double)var3[2] * var7));
         int var10 = Math.max(1, (int)Math.round((double)var3[3] * var7));
         BufferedImage var11 = new BufferedImage(var2, var2, 2);
         Graphics2D var12 = var11.createGraphics();

         try {
            var12.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            var12.drawImage(var4, (var2 - var9) / 2, (var2 - var10) / 2, var9, var10, (ImageObserver)null);
         } finally {
            var12.dispose();
         }

         return var11;
      } else {
         return null;
      }
   }

   private String titleTraceSuffix() {
      return this.mode == VqsvPanelRuntime.Mode.GAMEMENU ? " titleToken=" + MENU_TITLE_TOKENS[this.selected] : "";
   }

   private String uiName() {
      if (this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM) {
         return "gamesystem.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SAVE) {
         return "msgtip.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
         return "help1.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
         return "help.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
         return "option.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         return "ride.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return "transmit.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
         return "bodyShop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         return "shopbuy.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         return "msgyn.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
         return "smsInfo.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
         return this.sourceWharfIndex == 4 ? "wharf2.ui" : "wharf1.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
         return "wharf2.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
         return "shop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         return "shopbuy.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         return "msgyn.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
         return "msgRecover.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
         return "shop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         return "fashionShop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
         return "fashionBuyConfirm.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM) {
         return "fashionOpenConfirm.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
         return "fashionReveal.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
         return "fashionExchange.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
         return "fashionExchangeConfirm.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.CHALLENGE) {
         return "gamemenu.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.GIFT_CODE) {
         return "giftcode.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS) {
         return "gamemenu.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return "bag.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return "task.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return "taskMap.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return "taskOption.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
         return "record.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         return "petmap.ui";
      } else {
         return this.mode == VqsvPanelRuntime.Mode.BADGE ? "badge.ui" : "gamemenu.ui";
      }
   }

   private int[] rowWidgets() {
      if (this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM) {
         return SYSTEM_ROW_WIDGETS;
      } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return bagRowBackgrounds(this.bagTab);
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return TASK_ROW_BACKGROUNDS;
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return new int[0];
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return new int[]{10, 11};
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         return PETMAP_ROW_BACKGROUNDS;
      } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         return new int[]{4, 5, 6, 7};
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return TRANSMIT_ROW_WIDGETS;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
         return PORTABLE_SHOP_ROW_WIDGETS;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         return SHOPBUY_ROW_BACKGROUNDS;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         return new int[]{13, 14};
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
         return this.sourceWharfRowWidgets();
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
         return SOURCE_CONVENIENCE_ROW_WIDGETS;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
         return SOURCE_WORLD_SHOP_ROW_WIDGETS;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         return SHOPBUY_ROW_BACKGROUNDS;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         return new int[]{13, 14};
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
         return new int[0];
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
         return SOURCE_PET_BANK_ROW_WIDGETS;
      } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
         return new int[0];
      } else if (this.mode != VqsvPanelRuntime.Mode.CHALLENGE && this.mode != VqsvPanelRuntime.Mode.GIFT_CODE) {
         return this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS ? new int[0] : MENU_ROW_WIDGETS;
      } else {
         return new int[0];
      }
   }

   private String[] labels() {
      if (this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM) {
         return SYSTEM_LABELS;
      } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return new String[]{"Lưng bao"};
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return new String[]{"Nhiệm vụ"};
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return new String[]{"Bản đồ nhiệm vụ"};
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return this.taskOptionData.options;
      } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
         return new String[]{"Minh họa", "Kỷ lục"};
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         return new String[]{"Minh họa"};
      } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
         return new String[]{"Huy hiệu"};
      } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
         return new String[]{"Trợ giúp"};
      } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
         return new String[]{"Tùy chọn"};
      } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
         return new String[]{"Có", "Không"};
      } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         return RIDE_LABELS;
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return TRANSMIT_DESTINATIONS;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
         return PORTABLE_SHOP_LABELS;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         return new String[]{"Mua"};
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         return new String[]{"Xác nhận", "Không"};
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
         return new String[]{"Xác nhận", "Phản hồi"};
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
         return this.sourceWharfLabels();
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
         return this.sourceConvenienceLabels();
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
         return SOURCE_WORLD_SHOP_LABELS;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         return new String[]{"Bán đi"};
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         return new String[]{"Xác nhận", "Không"};
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
         return new String[]{"Xác định", "Quay lại"};
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
         return SOURCE_PET_BANK_LABELS;
      } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
         return new String[]{"Tủ thời trang"};
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         return FASHION_SHOP_LABELS;
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
         return new String[]{"Mua", "Hủy"};
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM) {
         return new String[]{"Mở túi", "Hủy"};
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
         return new String[]{"Tiếp tục"};
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
         return new String[]{"Đổi mảnh"};
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM) {
         return new String[]{"Đổi", "Hủy"};
      } else if (this.mode == VqsvPanelRuntime.Mode.CHALLENGE) {
         return new String[]{"Tái đấu Thủ lĩnh", "Giftcode"};
      } else if (this.mode == VqsvPanelRuntime.Mode.GIFT_CODE) {
         return new String[]{"Giftcode"};
      } else {
         return this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS ? new String[]{"Thẻ Fan Cứng VQSV"} : MENU_LABELS;
      }
   }

   private int wheelRowCount(VqsvGameRuntime.Scene var1) {
      if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return bagRows(var1, this.bagTab).size();
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return taskRowsForRender(var1, this.taskTab).size();
      } else if (this.mode == VqsvPanelRuntime.Mode.BATTLE_PASS) {
         return 50;
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return 1;
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return this.taskOptionData.options.length;
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         return petmapRowsForRender(var1, this.petmapTab).size();
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return TRANSMIT_DESTINATIONS.length;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         return this.portableShopItemCount();
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         return 2;
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
         return 2;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         return sourceWorldShopSellRows(var1).size();
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         return 2;
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
         return 2;
      } else if (this.mode == VqsvPanelRuntime.Mode.WARDROBE) {
         return SourceFashionCatalog.instance().records().size();
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         return FASHION_SHOP_LABELS.length;
      } else {
         return this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE ? FashionEconomyCatalog.instance().entries().size() : this.labels().length;
      }
   }

   private String closeTrace() {
      if (this.mode == VqsvPanelRuntime.Mode.GAMESYSTEM) {
         return "game.h.n close gamesystem.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.BAG) {
         return "game.h.ac close bag.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK) {
         return "game.h.S close task.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_MAP) {
         return "task.map close -> task.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.TASK_OPTION) {
         return "game.k.aG close taskOption.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.RECORD) {
         return "game.h.O close record.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.PETMAP) {
         return "game.h.Q close petmap.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.BADGE) {
         return "game.h.X close badge.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.SAVE) {
         return "game.h.K close msgtip.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.HELP) {
         return "game.h.v close help1.ui -> P=13";
      } else if (this.mode == VqsvPanelRuntime.Mode.SETTINGS) {
         return "game.h.x close help.ui -> P=13";
      } else if (this.mode == VqsvPanelRuntime.Mode.OPTION_CONFIRM) {
         return "game.h.n close option.ui f=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.RIDE) {
         return "game.h.ae close ride.ui -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.TRANSMIT) {
         return "game.k.i close transmit.ui -> P=8";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP) {
         return "game.k.aD close bodyShop.ui -> P=6";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY) {
         return "game.k.a(" + this.sourceShopTraceContext() + ") close shopbuy.ui -> P=14";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM) {
         return "game.k.a(" + this.sourceShopTraceContext() + ") close msgyn.ui -> shopbuy.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
         return "game.k.aD close smsInfo.ui -> bodyShop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_EAST_WHARF) {
         return "game.k.aP close " + (this.sourceWharfIndex == 4 ? "wharf2.ui" : "wharf1.ui") + " -> world state0";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_CONVENIENCE_SHOP) {
         return "game.k.aT close wharf2.ui -> world state0";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP) {
         return "game.k.G close shop.ui source world shop -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL) {
         return "game.k.P close shopbuy.ui source sell -> shop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_SELL_CONFIRM) {
         return "game.k.P close msgyn.ui source sell -> shopbuy.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_WORLD_SHOP_RECOVER_CONFIRM) {
         return "game.k.G close msgRecover.ui source recovery -> shop.ui selected=2";
      } else if (this.mode == VqsvPanelRuntime.Mode.SOURCE_PET_BANK) {
         return "game.k.E close shop.ui pet bank -> P=0";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_SHOP) {
         return "FASH-I3-I3 close fashionShop.ui -> bodyShop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_BUY_CONFIRM) {
         return "FASH-I3-I3 close fashionBuyConfirm.ui -> fashionShop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_OPEN_CONFIRM) {
         return "FASH-I3-I3 close fashionOpenConfirm.ui -> fashionShop.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_REVEAL) {
         return "FASH-I3-I3 close fashionReveal.ui";
      } else if (this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE) {
         return "FASH-I3-I3 close fashionExchange.ui -> fashionShop.ui";
      } else {
         return this.mode == VqsvPanelRuntime.Mode.FASHION_EXCHANGE_CONFIRM ? "FASH-I3-I3 close fashionExchangeConfirm.ui -> fashionExchange.ui" : "game.h.l back close gamemenu.ui -> P=0";
      }
   }

   private static String portableShopDescription(VqsvGameRuntime.Scene var0, int var1) {
      switch (var1) {
         case 1:
            long var2 = WorldRealtimeClock.nowMillis();
            DailyBadgeRewardService.Status var4 = DAILY_BADGE_REWARD_SERVICE.status(var0.session.progression.dailyBadgeLastClaimEpochMillis, var2);
            String var10000 = var4.available ? "Có thể nhận ngay." : "Nhận lại sau " + WorldRealtimeClock.displayRemaining(var4.remainingMillis) + ".";
            return "Mỗi ngày nhận 20 Huy hiệu theo giờ thực tế. " + var10000;
         case 2:
            return "Mua túi mù, mở trang phục và đổi mảnh thời trang.";
         case 3:
            return "Mua tài liệu, trang sức và nguyên liệu tiến hóa.";
         default:
            return "Tùy thời mua sắm các loại đạo cụ, già trẻ không gạt.";
      }
   }

   private static int portableShopSyntheticRowAt(int var0, int var1) {
      return var0 >= 66 && var0 <= 190 && var1 >= 156 && var1 <= 174 ? 3 : -1;
   }

   private static String portableShopServiceTitle(int var0) {
      if (var0 == 3) {
         return "Mua đẳng cấp";
      } else if (var0 == 4) {
         return "Nhận huy hiệu hàng ngày";
      } else {
         return var0 == 2 ? "Mua sắm kim tiền" : "Xác nhận";
      }
   }

   private static int portableShopProductRow(int var0) {
      return var0 == 4 ? 1 : 0;
   }

   private void openPortableShopBuy(VqsvGameRuntime.Scene var1, int var2, byte var3, String var4) {
      this.openPortableShopBuy(var1, var2, var3, var4, false);
   }

   private void openPortableShopBuy(VqsvGameRuntime.Scene var1, int var2, byte var3, String var4, boolean var5) {
      this.shopTable = var2;
      this.shopBucket = var3;
      this.portableShopReturnToWorld = var5;
      this.portableShopReturnToSourceWorldShop = false;
      this.portableShopReturnToSourceConvenience = false;
      this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY;
      this.selected = 0;
      this.listScroll = 0;
      this.openedTicks = 0;
      var1.session.story.trace().add(var4 + " rows=" + this.portableShopItemCount() + " table=" + this.shopTable + " bucket=" + this.shopBucket);
   }

   private int portableShopItemCount() {
      int var1;
      for(var1 = 0; this.portableShopSourceItem(var1) != null; ++var1) {
      }

      return var1;
   }

   private String sourceShopTraceContext() {
      if (this.shopTable == 4 && this.shopBucket == 0) {
         return "byte4,0";
      } else {
         return this.shopTable == 3 && this.shopBucket == 2 ? "3,2" : this.shopTable + "," + this.shopBucket;
      }
   }

   private ItemDefinition portableShopSourceItem(int var1) {
      if (var1 < 0) {
         return null;
      } else if (this.shopTable == 3 && this.shopBucket == 2) {
         int var5 = this.portableShopLegacyHeldCount();
         if (var1 < var5) {
            return portableShopHeldItemEquipment(var1) ? sourceEquipmentItem(var1) : VqsvSourceOps.sourceMaterialItem(var1);
         } else {
            UnifiedEvolutionCatalog.MaterialSource var6 = this.portableShopEvolutionOverlay(var1);
            return var6 == null ? null : VqsvSourceOps.sourceMaterialItem(var6.runtimeMaterialId);
         }
      } else if (this.shopTable == 4 && this.shopBucket == 0) {
         int var2 = this.portableShopLegacyItemCount();
         if (var1 < var2) {
            return VqsvSourceOps.sourceItem(portableShopLegacySourceItemId(var1));
         } else {
            UnifiedItemAliasRecord var3 = this.portableShopOrdinaryAliasOffer(var1);
            if (var3 == null) {
               return null;
            } else {
               UnifiedItemRecord var4 = UnifiedItemCatalog.instance().requireActive(var3.canonicalKey);
               return new ItemDefinition(var3, var4, 0);
            }
         }
      } else {
         return null;
      }
   }

   private int portableShopOriginalPrice(int var1) {
      return PORTABLE_SHOP_SERVICE.originalPrice(this.portableShopStockKind(var1), this.portableShopSourcePrice(var1), this.portableShopCurrency(var1));
   }

   private int portableShopSourcePrice(int var1) {
      if (this.shopTable == 3 && this.shopBucket == 2) {
         UnifiedEvolutionCatalog.MaterialSource var4 = this.portableShopEvolutionOverlay(var1);
         if (var4 != null) {
            return var4.price;
         } else {
            BattleHeldItemRow var3 = VqsvBattleTables.instance().heldItem(var1);
            return var3 == null ? 0 : var3.priceOrValue;
         }
      } else if (this.shopTable == 4 && this.shopBucket == 0) {
         BattleItemRow var2 = VqsvBattleTables.instance().item(this.portableShopCanonicalItemId(var1));
         return var2 == null ? 0 : var2.priceOrValue;
      } else {
         return 0;
      }
   }

   private int portableShopPrice(int var1) {
      return this.portableShopSourceItem(var1) == null ? 0 : PORTABLE_SHOP_SERVICE.unitPrice(this.portableShopPricingPolicy(var1), this.portableShopOriginalPrice(var1), this.portableShopCurrency(var1));
   }

   private int portableShopCurrency(int var1) {
      if (this.shopTable == 3 && this.shopBucket == 2) {
         UnifiedEvolutionCatalog.MaterialSource var4 = this.portableShopEvolutionOverlay(var1);
         if (var4 != null) {
            return var4.currencyCode;
         } else {
            BattleHeldItemRow var3 = VqsvBattleTables.instance().heldItem(var1);
            return var3 == null ? 0 : var3.currencyOrType;
         }
      } else if (this.shopTable == 4 && this.shopBucket == 0) {
         BattleItemRow var2 = VqsvBattleTables.instance().item(this.portableShopCanonicalItemId(var1));
         return var2 == null ? 0 : var2.currencyOrType;
      } else {
         return 0;
      }
   }

   private static int shopCurrencyCell(int var0) {
      return VqsvShopCurrencyRenderer.iconCell(var0);
   }

   private void openPortableShopConfirm(VqsvGameRuntime.Scene var1) {
      ItemDefinition var2 = this.portableShopSourceItem(this.selected);
      PortableShopService.ConfirmOpenResult var3 = PORTABLE_SHOP_SERVICE.planConfirmOpen(var2 != null, this.portableShopStockKind(this.selected), this.portableShopInventoryItemId(this.selected), this.portableShopSourcePrice(this.selected), this.portableShopCurrency(this.selected), this.portableShopPricingPolicy(this.selected), var1.session.inventory.bagItems, var1.session.inventory.materialItems, var1.session.inventory.equipmentItems);
      if (var3.outcome == PortableShopService.ConfirmOpenOutcome.INVALID_ROW) {
         var1.text = TextBox.msgWarm("Không có đạo cụ bán", "Nhấn nút 5 để tiếp tục");
      } else if (var3.outcome == PortableShopService.ConfirmOpenOutcome.FULL) {
         var1.text = TextBox.msgWarm("Đạo cụ này đã đủ", "Nhấn nút 5 để tiếp tục");
         var1.session.story.trace().add("PORTED/PARTIAL panel game.k.a(" + this.sourceShopTraceContext() + ") shop full item=" + this.selected + " count=" + var3.quote.currentCount);
      } else {
         this.shopConfirmItemId = this.selected;
         this.shopConfirmQuantity = var3.quote.quantity;
         this.shopConfirmCurrency = var3.quote.sourceCurrencyCode;
         this.shopConfirmTotal = var3.quote.total;
         this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP_CONFIRM;
         this.openedTicks = 0;
         List var10000 = var1.session.story.trace();
         String var10001 = this.sourceShopTraceContext();
         var10000.add("PORTED/PARTIAL panel game.k.a(" + var10001 + ") open msgyn.ui item=" + this.shopConfirmItemId + " qty=" + this.shopConfirmQuantity + " total=" + this.shopConfirmTotal + " currency=" + this.shopConfirmCurrency);
      }
   }

   private void closePortableShopConfirm(VqsvGameRuntime.Scene var1, String var2) {
      this.mode = VqsvPanelRuntime.Mode.PORTABLE_SHOP_BUY;
      this.shopConfirmItemId = -1;
      this.shopConfirmQuantity = 1;
      this.shopConfirmTotal = 0;
      this.shopConfirmCurrency = 0;
      this.openedTicks = 0;
      List var10000 = var1.session.story.trace();
      String var10001 = this.sourceShopTraceContext();
      var10000.add("PORTED/PARTIAL panel game.k.a(" + var10001 + ") close msgyn.ui return shopbuy.ui reason=" + var2);
   }

   private void syncPortableShopConfirm(VqsvGameRuntime.Scene var1) {
      PortableShopService.Quote var2 = this.portableShopQuote(var1, this.shopConfirmItemId, this.shopConfirmQuantity);
      this.shopConfirmQuantity = var2.outcome == PortableShopService.QuoteOutcome.READY ? var2.quantity : 1;
      this.shopConfirmCurrency = var2.sourceCurrencyCode;
      this.shopConfirmTotal = var2.total;
   }

   private int portableShopCurrentCount(VqsvGameRuntime.Scene var1, int var2) {
      return PORTABLE_SHOP_SERVICE.currentCount(this.portableShopStockKind(var2), var1.session.inventory.bagItems, var1.session.inventory.materialItems, var1.session.inventory.equipmentItems, this.portableShopInventoryItemId(var2));
   }

   private int portableShopMaxQuantity(VqsvGameRuntime.Scene var1, int var2) {
      return PORTABLE_SHOP_SERVICE.maxQuantity(this.portableShopStockKind(var2), var1.session.inventory.bagItems, var1.session.inventory.materialItems, var1.session.inventory.equipmentItems, this.portableShopInventoryItemId(var2));
   }

   private PortableShopService.StockKind portableShopStockKind(int var1) {
      if (this.shopTable == 3 && this.shopBucket == 2) {
         return portableShopHeldItemEquipment(var1) ? PortableShopService.StockKind.EQUIPMENT : PortableShopService.StockKind.MATERIAL;
      } else {
         return this.shopTable == 4 && this.shopBucket == 0 ? PortableShopService.StockKind.BAG_ITEM : null;
      }
   }

   private int portableShopInventoryItemId(int var1) {
      if (this.shopTable == 3 && this.shopBucket == 2 && !portableShopHeldItemEquipment(var1)) {
         UnifiedEvolutionCatalog.MaterialSource var2 = this.portableShopEvolutionOverlay(var1);
         return var2 != null ? var2.runtimeMaterialId : VqsvSourceOps.sourceHeldTableMaterialInventoryId(var1);
      } else {
         return this.shopTable == 4 && this.shopBucket == 0 ? this.portableShopCanonicalItemId(var1) : var1;
      }
   }

   private PortableShopService.Quote portableShopQuote(VqsvGameRuntime.Scene var1, int var2, int var3) {
      ItemDefinition var4 = this.portableShopSourceItem(var2);
      return PORTABLE_SHOP_SERVICE.quote(var4 != null, this.portableShopStockKind(var2), this.portableShopInventoryItemId(var2), var3, this.portableShopSourcePrice(var2), this.portableShopCurrency(var2), this.portableShopPricingPolicy(var2), var1.session.inventory.bagItems, var1.session.inventory.materialItems, var1.session.inventory.equipmentItems);
   }

   private void commitPortableShopItem(VqsvGameRuntime.Scene var1) {
      ItemDefinition var2 = this.portableShopSourceItem(this.shopConfirmItemId);
      if (var2 == null) {
         var1.text = TextBox.msgWarm("Không có đạo cụ bán", "Nhấn nút 5 để tiếp tục");
         this.closePortableShopConfirm(var1, "missing row on commit");
      } else {
         PortableShopService.PurchaseResult var3 = PORTABLE_SHOP_SERVICE.purchase(true, this.portableShopStockKind(this.shopConfirmItemId), this.portableShopInventoryItemId(this.shopConfirmItemId), this.shopConfirmQuantity, this.portableShopSourcePrice(this.shopConfirmItemId), this.portableShopCurrency(this.shopConfirmItemId), var2.bagChannel, this.portableShopPricingPolicy(this.shopConfirmItemId), var1.session.inventory.bagItems, var1.session.inventory.materialItems, var1.session.inventory.equipmentItems, var1.session.inventory.currency);
         PortableShopService.PurchaseRoute var4 = PORTABLE_SHOP_SERVICE.routePurchase(var3);
         if (var4.outcome == PortableShopService.PurchaseRouteOutcome.FULL) {
            var1.text = TextBox.msgWarm("Đạo cụ này đã đủ", "Nhấn nút 5 để tiếp tục");
            this.closePortableShopConfirm(var1, var4.closeReason);
         } else if (var4.outcome == PortableShopService.PurchaseRouteOutcome.INSUFFICIENT_CURRENCY) {
            String var5 = this.portableShopCurrency(this.shopConfirmItemId) == 0 ? "Kim tiền chưa đủ" : "Số lượng Huy hiệu chưa đủ";
            var1.text = TextBox.msgWarm(var5, "Nhấn nút 5 để tiếp tục");
            this.closePortableShopConfirm(var1, var4.closeReason);
         } else if (var4.outcome != PortableShopService.PurchaseRouteOutcome.PURCHASED) {
            var1.text = TextBox.msgWarm("Không có đạo cụ bán", "Nhấn nút 5 để tiếp tục");
            this.closePortableShopConfirm(var1, var4.closeReason);
         } else {
            var1.text = TextBox.msgWarm("Đã thành công mua sắm #2" + var2.name + " * " + var3.quote.storedQuantity, "Nhấn nút 5 để tiếp tục");
            List var10000 = var1.session.story.trace();
            String var10001 = this.sourceShopTraceContext();
            var10000.add("PORTED/PARTIAL panel game.k.b(" + var10001 + ") shop buy item=" + this.shopConfirmItemId + " qty=" + var3.quote.quantity + " storedQty=" + var3.quote.storedQuantity + " total=" + var3.quote.total + " currency=" + var3.quote.sourceCurrencyCode + " originalPrice=" + var3.quote.originalPrice + " money=" + var3.moneyAfter + " badges=" + var3.badgesAfter + " count=" + var3.countAfter + " table=" + this.shopTable + " bucket=" + this.shopBucket + " stockKind=" + String.valueOf(this.portableShopStockKind(this.shopConfirmItemId)) + " pricing=" + String.valueOf(this.portableShopPricingPolicy(this.shopConfirmItemId)));
            this.closePortableShopConfirm(var1, var4.closeReason);
         }
      }
   }

   private static boolean portableShopHeldItemEquipment(int var0) {
      return var0 >= 0 && var0 <= 11;
   }

   private int portableShopLegacyHeldCount() {
      if (this.shopTable == 3 && this.shopBucket == 2) {
         int var1;
         for(var1 = 0; VqsvBattleTables.instance().heldItem(var1) != null; ++var1) {
         }

         return var1;
      } else {
         return 0;
      }
   }

   private int portableShopLegacyItemCount() {
      if (this.shopTable == 4 && this.shopBucket == 0) {
         int var1;
         for(var1 = 0; VqsvBattleTables.instance().item(var1) != null; ++var1) {
         }

         return Math.max(0, var1 - 1);
      } else {
         return 0;
      }
   }

   private static int portableShopLegacySourceItemId(int var0) {
      return var0 + 1;
   }

   private UnifiedItemAliasRecord portableShopOrdinaryAliasOffer(int var1) {
      if (this.shopTable == 4 && this.shopBucket == 0) {
         int var2 = var1 - this.portableShopLegacyItemCount();
         List var3 = UnifiedItemCatalog.instance().ordinaryShopAliases();
         return var2 >= 0 && var2 < var3.size() ? (UnifiedItemAliasRecord)var3.get(var2) : null;
      } else {
         return null;
      }
   }

   private int portableShopCanonicalItemId(int var1) {
      if (this.shopTable == 4 && this.shopBucket == 0 && var1 >= 0 && var1 < this.portableShopLegacyItemCount()) {
         return portableShopLegacySourceItemId(var1);
      } else {
         UnifiedItemAliasRecord var2 = this.portableShopOrdinaryAliasOffer(var1);
         return var2 == null ? var1 : UnifiedItemCatalog.instance().requireActive(var2.canonicalKey).runtimeId;
      }
   }

   private UnifiedEvolutionCatalog.MaterialSource portableShopEvolutionOverlay(int var1) {
      return this.shopTable == 3 && this.shopBucket == 2 ? UnifiedEvolutionCatalog.instance().rainbowShopOverlay(var1 - this.portableShopLegacyHeldCount()) : null;
   }

   private PortableShopService.PricingPolicy portableShopPricingPolicy(int var1) {
      return PortableShopService.PricingPolicy.SOURCE_PRICE;
   }

   private static boolean portableShopItemTableMaterial(int var0) {
      BattleItemRow var1 = VqsvBattleTables.instance().item(var0);
      return var1 != null && var1.behavior == 4;
   }

   private static List<SourceSellRow> sourceWorldShopSellRows(VqsvGameRuntime.Scene var0) {
      TreeSet<Integer> var1 = new TreeSet<>();

      for(BagItemState var3 : var0.session.inventory.bagItems.values()) {
         if (var3.count > 0) {
            var1.add(var3.id);
         }
      }

      for(MaterialStack var9 : var0.session.inventory.materialItems) {
         if (var9.count > 0 && VqsvSourceOps.sourceItemTableMaterialInventoryIdUsed(var9.id)) {
            var1.add(VqsvSourceOps.sourceItemTableMaterialSourceId(var9.id));
         }
      }

      ArrayList var8 = new ArrayList();

      for(int var4 : var1) {
         BattleItemRow var5 = VqsvBattleTables.instance().item(var4);
         if (var5 != null) {
            int var6 = WORLD_ITEM_SHOP_SERVICE.currentCount(var0.session.inventory.bagItems, var0.session.inventory.materialItems, var4, VqsvSourceOps.sourceItemTableMaterialInventoryId(var4));
            if (var6 > 0) {
               var8.add(new SourceSellRow(VqsvSourceOps.sourceItem(var4), var6, var5.priceOrValue, var5.currencyOrType, WORLD_ITEM_SHOP_SERVICE.unitSalePrice(var5.priceOrValue)));
            }
         }
      }

      return var8;
   }

   private static SourceSellRow sourceWorldShopSellRowById(VqsvGameRuntime.Scene var0, int var1) {
      for(SourceSellRow var3 : sourceWorldShopSellRows(var0)) {
         if (var3.item.id == var1) {
            return var3;
         }
      }

      return null;
   }

   private static List<BagRow> bagRows(VqsvGameRuntime.Scene var0, int var1) {
      ArrayList<BagRow> var2 = new ArrayList<>();
      if (var1 != 3) {
         if (var1 == 1) {
            for(EquipmentState var13 : var0.session.inventory.equipmentItems) {
               ItemDefinition var14 = sourceEquipmentItem(var13.id);
               var2.add(new BagRow(var14, 1, false, var13.id, VqsvSourceOps.sourceEquipmentEquipped(var13) ? "Đã mang theo" : ""));
            }

            var2.sort(Comparator.comparingInt((var0x) -> var0x.item.id));
            return var2;
         } else if (var1 == 2) {
            for(MaterialStack var12 : var0.session.inventory.materialItems) {
               if (var12.count > 0 && !VqsvSourceOps.sourceItemTableMaterialInventoryIdUsed(var12.id)) {
                  var2.add(new BagRow(sourceMaterialItem(var12.id), var12.count));
               }
            }

            var2.sort(Comparator.comparingInt((var0x) -> var0x.item.id));
            return var2;
         } else if (var1 != 0) {
            return var2;
         } else {
            for(BagItemState var10 : var0.session.inventory.bagItems.values()) {
               if (var10.count > 0) {
                  var2.add(new BagRow(VqsvSourceOps.sourceItem(var10.id), VqsvSourceOps.sourceItemCount(var0, var10.id)));
               }
            }

            for(MaterialStack var11 : var0.session.inventory.materialItems) {
               if (var11.count > 0 && VqsvSourceOps.sourceItemTableMaterialInventoryIdUsed(var11.id)) {
                  int var5 = VqsvSourceOps.sourceItemTableMaterialSourceId(var11.id);
                  if (!var0.session.inventory.bagItems.containsKey(var5)) {
                     var2.add(new BagRow(VqsvSourceOps.sourceItem(var5), VqsvSourceOps.sourceItemCount(var0, var5)));
                  }
               }
            }

            var2.sort(Comparator.comparingInt((var0x) -> var0x.item.id));
            return var2;
         }
      } else {
         if (var0.session.progression.egg.active || var0.session.inventory.specialRewards.containsKey(0)) {
            var2.add(sourceEggSpecialRow(var0));
         }

         for(SpecialRewardState var4 : var0.session.inventory.specialRewards.values()) {
            if (var4.id != 0 && sourceSpecialVisible(var4)) {
               var2.add(sourceSpecialRewardRow(var0, var4));
            }
         }

         var2.sort(Comparator.comparingInt((var0x) -> var0x.specialId));
         return var2;
      }
   }

   private static ItemDefinition sourceEquipmentItem(int var0) {
      return VqsvSourceOps.sourceEquipmentItem(var0);
   }

   private static ItemDefinition sourceMaterialItem(int var0) {
      return VqsvSourceOps.sourceMaterialItem(var0);
   }

   private static int[] bagRowBackgrounds(int var0) {
      if (var0 == 1) {
         return BAG_EQUIP_ROW_BACKGROUNDS;
      } else {
         return var0 == 2 ? BAG_MATERIAL_ROW_BACKGROUNDS : BAG_ROW_BACKGROUNDS;
      }
   }

   private static int[] bagRowIcons(int var0) {
      if (var0 == 1) {
         return BAG_EQUIP_ROW_ICONS;
      } else if (var0 == 2) {
         return BAG_MATERIAL_ROW_ICONS;
      } else {
         return var0 == 3 ? BAG_SPECIAL_ROW_ICONS : BAG_ROW_ICONS;
      }
   }

   private static int[] bagRowNames(int var0) {
      if (var0 == 1) {
         return BAG_EQUIP_ROW_NAMES;
      } else if (var0 == 2) {
         return BAG_MATERIAL_ROW_NAMES;
      } else {
         return var0 == 3 ? BAG_SPECIAL_ROW_NAMES : BAG_ROW_NAMES;
      }
   }

   private static int[] bagRowCounts(int var0) {
      if (var0 == 1) {
         return BAG_EQUIP_ROW_STATUS;
      } else if (var0 == 2) {
         return BAG_MATERIAL_ROW_COUNTS;
      } else {
         return var0 == 3 ? BAG_SPECIAL_ROW_COUNTS : BAG_ROW_COUNTS;
      }
   }

   private static int bagDescriptionWidget(int var0) {
      if (var0 == 1) {
         return 85;
      } else if (var0 == 2) {
         return 124;
      } else {
         return var0 == 3 ? 163 : 46;
      }
   }

   private static BagRow sourceEggSpecialRow(VqsvGameRuntime.Scene var0) {
      SpecialRewardRow var1 = VqsvBattleTables.instance().specialReward(0);
      int var2 = var1 == null ? -1 : var1.nameTextId;
      int var3 = var1 == null ? 0 : var1.iconCell;
      int var4 = var0.session.progression.egg.active ? (var1 == null ? -1 : var1.descriptionTextId) : 634;
      VqsvBattleTables var5 = VqsvBattleTables.instance();
      String var6 = var5.text(var2, "Ấp trứng");
      String var7 = var5.text(var4, "");
      SpecialRewardState var8 = (SpecialRewardState)var0.session.inventory.specialRewards.get(0);
      int var9 = var8 == null ? 0 : Math.max(0, var8.stackCount);
      String var10;
      if (!var0.session.progression.egg.active) {
         var10 = "0 cái";
      } else if (sourceEggReady(var0)) {
         var10 = "Hoàn thành - " + var9 + " cái";
      } else {
         var10 = var9 + " cái";
      }

      ItemDefinition var11 = new ItemDefinition(0, var2, var3, var4, var6, var7, 3);
      return new BagRow(var11, var9, true, 0, var10);
   }

   private static boolean sourceSpecialVisible(SpecialRewardState var0) {
      return VqsvSourceOps.sourceSpecialVisible(var0);
   }

   private static boolean sourceRideUnlocked(VqsvGameRuntime.Scene var0, int var1) {
      return VqsvRideRuntime.rideUnlocked(var0, var1);
   }

   private static boolean sourceRideUsable(VqsvGameRuntime.Scene var0, int var1) {
      return var0.session.progression.ride.selectionOutcome(var1, sourceRideUnlocked(var0, var1)) == RideProgression.SelectionOutcome.READY;
   }

   private static int rideSlotAt(VqsvUiLayout var0, int var1, int var2) {
      int var3 = -1;
      int var4 = Integer.MAX_VALUE;

      for(int var5 = 0; var5 < RIDE_LABELS.length; ++var5) {
         VqsvUiLayout.UiWidget var6 = var0.widget(var5 + 4);
         if (var6 != null) {
            int var7 = Math.max(34, var6.w);
            if (var1 >= var6.x - 4 && var1 <= var6.x + var7 + 8 && var2 >= var6.y - 28 && var2 <= var6.y + 24) {
               int var8 = Math.abs(var1 - (var6.x + var7 / 2));
               if (var8 < var4) {
                  var3 = var5;
                  var4 = var8;
               }
            }
         }
      }

      return var3;
   }

   private static BagRow sourceSpecialRewardRow(VqsvGameRuntime.Scene var0, SpecialRewardState var1) {
      ItemDefinition var2 = VqsvSourceOps.sourceSpecialRewardItem(var1.id);
      int var3 = VqsvSourceOps.sourceSpecialDisplayCount(var1);
      boolean var4 = V4EggCatalog.isV4Egg(var1.id);
      String var5 = specialStatus(var1);
      if (var4 && var0.session.progression.egg.active && var0.session.progression.egg.activeEggItemId == var1.id) {
         var5 = var0.session.progression.egg.ready() ? "Hoàn thành - " + var3 + " cái" : "Đang ấp - " + var3 + " cái";
      }

      return new BagRow(var2, var3, var4, var1.id, var5);
   }

   private static String specialStatus(SpecialRewardState var0) {
      if (var0.id == 0) {
         return String.valueOf(Math.max(0, var0.stackCount));
      } else {
         return VqsvSourceOps.sourceSpecialStackable(var0.id) ? String.valueOf(Math.max(0, var0.stackCount)) : "";
      }
   }

   private static int value(short[] var0, int var1, int var2) {
      return var0 != null && var1 >= 0 && var1 < var0.length ? var0[var1] : var2;
   }

   private static List<TaskRow> taskRowsForRender(VqsvGameRuntime.Scene var0, int var1) {
      List var2 = var1 == 0 ? loadMainTasks() : loadBranchTasks();
      ArrayList var3 = new ArrayList();
      if (var2.isEmpty()) {
         return var3;
      } else {
         int var4 = Math.max(1, var2.size() / 2);
         if (var1 == 1) {
            List var13 = sourceBranchTasksForRender(var0);
            int var14 = Math.min(var4, var13.size());

            for(int var15 = 0; var15 < var14; ++var15) {
               BranchTask var16 = (BranchTask)var13.get(var15);
               int var17 = clamp(var16.taskId, 0, var4 - 1);
               String var10 = (String)var2.get(var17);
               String var11 = var17 + var4 < var2.size() ? (String)var2.get(var17 + var4) : var10;
               SourceTaskOfferCatalog.Offer var12 = SourceTaskOfferCatalog.offer(var16.taskId);
               if (var0 != null && var12.grantedWorldFlag >= 0 && var0.session.world.sourceWorldFlags.contains(var12.grantedWorldFlag) && !var12.grantedQuestTokenLabel.isEmpty()) {
                  var11 = var11 + "\nVật phẩm nhiệm vụ: " + var12.grantedQuestTokenLabel;
               }

               var3.add(new TaskRow(var15 + 1, var10, var11, var16.status == 3));
            }

            return var3;
         } else {
            int var5 = Math.min(var4, Math.max(1, mainTaskCursor(var0) + 1));

            for(int var6 = 0; var6 < var5; ++var6) {
               String var7 = (String)var2.get(var6);
               String var8 = var6 + var4 < var2.size() ? (String)var2.get(var6 + var4) : var7;
               boolean var9 = var6 < mainTaskCursor(var0);
               var3.add(new TaskRow(var6 + 1, var7, var8, var9));
            }

            return var3;
         }
      }
   }

   private static int mainTaskCursor(VqsvGameRuntime.Scene var0) {
      return var0 == null ? 0 : Math.max(0, var0.session.story.mainTaskProgress);
   }

   private static List<BranchTask> sourceBranchTasksForRender(VqsvGameRuntime.Scene var0) {
      return var0 == null ? Collections.emptyList() : var0.session.story.branchTasks;
   }

   private static String taskProgressText(VqsvGameRuntime.Scene var0, int var1) {
      if (var1 == 0) {
         int var6 = Math.max(1, loadMainTasks().size() / 2);
         int var7 = mainTaskCursor(var0) * 1000 / var6;
         if (var0 != null && var0.sourcePremiumUiPercent) {
            return var7 / 10 + "." + var7 % 10 + "%";
         } else {
            int var9 = var7 % 10;
            if (var9 == 0) {
               var9 = 1;
            }

            return var7 / 50 + "." + var9 + "%";
         }
      } else {
         int var2 = Math.max(1, loadBranchTasks().size() / 2);
         int var3 = 0;

         for(BranchTask var5 : sourceBranchTasksForRender(var0)) {
            if (var5.status == 3) {
               ++var3;
            }
         }

         int var8 = var3 * 1000 / var2;
         return var8 / 10 + "." + var8 % 10 + "%";
      }
   }

   private static List<String> loadMainTasks() {
      if (mainTaskRows == null) {
         mainTaskRows = loadTaskScript("data__script__mTask.mid.json");
      }

      return mainTaskRows;
   }

   private static List<String> loadBranchTasks() {
      if (branchTaskRows == null) {
         branchTaskRows = loadTaskScript("data__script__bTask.mid.json");
      }

      return branchTaskRows;
   }

   private static String helpEffectText(int var0) {
      int var1 = var0 <= 10 ? 311 + var0 : 333 + var0 - 11;
      List var2 = loadChsRows();
      return var1 >= 0 && var1 < var2.size() ? (String)var2.get(var1) : "an.f(" + var1 + ")";
   }

   private static List<String> loadChsRows() {
      if (chsRows == null) {
         chsRows = loadTaskScript("data__script__chs.mid.json");
      }

      return chsRows;
   }

   private static List<String> loadTaskScript(String var0) {
      ArrayList var1 = new ArrayList();

      try {
         AssetPaths var2 = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
         java.nio.file.Path var3 = var2.modulesRoot().resolve("script").resolve("decoded").resolve(var0);
         String var4 = (new ResourceLocator(var2)).readUtf8(var3);
         Matcher var5 = TASK_TEXT_PATTERN.matcher(var4);

         while(var5.find()) {
            String var6 = var5.group(1);
            var1.add(TextBox.decodeMojibake(var6));
         }
      } catch (RuntimeException var7) {
         var1.clear();
      }

      return var1;
   }

   private static List<PetmapRow> petmapRows(int var0) {
      ArrayList var1 = new ArrayList();
      VqsvBattleTables var2 = VqsvBattleTables.instance();

      for(int var3 = 0; var3 < 202; ++var3) {
         BattleSpeciesRow var4 = var2.species(var3);
         if (var4 != null && var4.validForBattle() && var4.element == var0) {
            var1.add(new PetmapRow(var3, var4.name("Pet " + var3), var4.spriteId, false, false));
         }
      }

      return var1;
   }

   private static List<PetmapRow> petmapRowsForRender(VqsvGameRuntime.Scene var0, int var1) {
      List var2 = petmapRows(var1);

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         PetmapRow var4 = (PetmapRow)var2.get(var3);
         byte var5 = var0 == null ? 0 : var0.session.progression.collection.status(var4.speciesId);
         var2.set(var3, new PetmapRow(var4.speciesId, var4.name, var4.spriteId, var5 > 0, var5 == 2));
      }

      return var2;
   }

   private static int ownedCount(List<PetmapRow> var0) {
      int var1 = 0;

      for(PetmapRow var3 : var0) {
         if (var3.owned) {
            ++var1;
         }
      }

      return var1;
   }

   private static int collectedSpeciesByRelationClass(VqsvGameRuntime.Scene var0, int var1) {
      return var0.session.progression.collection.countCollected((var1x) -> {
         BattleSpeciesRow var2 = VqsvBattleTables.instance().species(var1x);
         return var2 != null && var2.relationClass == var1;
      });
   }

   private static String sourcePlayTime(long var0) {
      long var2 = Math.max(0L, var0) / 1000L;
      long var4 = var2 % 60L;
      long var6 = var2 / 60L % 60L;
      long var8 = var2 / 3600L;
      return var8 + "'" + var6 + "\"" + (var4 < 10L ? "0" : "") + var4;
   }

   private static boolean sourceBadgeAchieved(VqsvGameRuntime.Scene var0, int var1) {
      return var0 != null && var0.session.progression.badges.achieved(var1);
   }

   private static String badgeName(int var0) {
      UnifiedBadgeCatalog.Record var1 = UnifiedBadgeCatalog.instance().byRuntimeId(var0);
      if (var1 != null) {
         return var1.name;
      } else {
         BadgeRow var2 = VqsvBattleTables.instance().badge(var0);
         return var2 == null ? "Huy hiệu " + (var0 + 1) : var2.name("Huy hiệu " + (var0 + 1));
      }
   }

   private static String badgeDescription(VqsvGameRuntime.Scene var0, int var1) {
      UnifiedBadgeCatalog.Record var2 = UnifiedBadgeCatalog.instance().byRuntimeId(var1);
      if (var2 != null) {
         return var2.description;
      } else {
         BadgeRow var3 = VqsvBattleTables.instance().badge(var1);
         return var3 == null ? "" : var3.description(sourceBadgeEnhanced(var0, var1), "");
      }
   }

   private static String badgeStatusText(VqsvGameRuntime.Scene var0, int var1) {
      if (!sourceBadgeAchieved(var0, var1)) {
         return "Chưa đạt";
      } else if (!var0.session.progression.badges.enhanceable(var1)) {
         return "Đã đạt - Không thể cường hóa";
      } else {
         return sourceBadgeEnhanced(var0, var1) ? "Đã đạt - Đã cường hóa" : "Đã đạt - Chưa cường hóa";
      }
   }

   private static boolean sourceBadgeEnhanced(VqsvGameRuntime.Scene var0, int var1) {
      return var0 != null && var0.session.progression.badges.enhanced(var1);
   }

   private static int badgeDisplayCount(VqsvGameRuntime.Scene var0) {
      return badgeDisplayIds(var0).length;
   }

   private static int[] badgeDisplayIds(VqsvGameRuntime.Scene var0) {
      int var1 = 0;

      for(UnifiedBadgeCatalog.Record var3 : UnifiedBadgeCatalog.instance().records()) {
         if (!var3.hiddenUntilOwned || sourceBadgeAchieved(var0, var3.runtimeId)) {
            ++var1;
         }
      }

      int[] var6 = new int[BADGE_SLOT_WIDGETS.length + var1];

      for(int var7 = 0; var7 < BADGE_SLOT_WIDGETS.length; var6[var7] = var7++) {
      }

      int var8 = BADGE_SLOT_WIDGETS.length;

      for(UnifiedBadgeCatalog.Record var5 : UnifiedBadgeCatalog.instance().records()) {
         if (!var5.hiddenUntilOwned || sourceBadgeAchieved(var0, var5.runtimeId)) {
            var6[var8++] = var5.runtimeId;
         }
      }

      return var6;
   }

   private static int badgeDisplayIndexForRuntimeId(VqsvGameRuntime.Scene var0, int var1) {
      int[] var2 = badgeDisplayIds(var0);

      for(int var3 = 0; var3 < var2.length; ++var3) {
         if (var2[var3] == var1) {
            return var3;
         }
      }

      return 0;
   }

   private static int badgeRuntimeIdAtDisplayIndex(VqsvGameRuntime.Scene var0, int var1) {
      int[] var2 = badgeDisplayIds(var0);
      return var2[clamp(var1, 0, var2.length - 1)];
   }

   private static int badgeGridFirst(int var0, int var1) {
      int var2 = (var1 + 5 - 1) / 5;
      int var3 = clamp(var0, 0, var1 - 1) / 5;
      int var4 = clamp(var3 - 2 + 1, 0, Math.max(0, var2 - 2));
      return var4 * 5;
   }

   private static int badgeGridX(int var0, int var1) {
      int var2 = var0 / 5;
      int var3 = var0 % 5;
      int var4 = Math.min(5, var1 - var2 * 5);
      int var5 = (5 - var4) * 28 / 2;
      return 50 + var5 + var3 * 28;
   }

   private static int badgeGridY(int var0) {
      return 99 + var0 / 5 * 33;
   }

   private static void consumeKeys(VqsvGameRuntime.Scene var0) {
      var0.key0 = false;
      var0.keyBack = false;
      var0.keyUp = false;
      var0.keyDown = false;
      var0.keyLeft = false;
      var0.keyRight = false;
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static enum Mode {
      GAMEMENU,
      GAMESYSTEM,
      BAG,
      TASK,
      TASK_MAP,
      TASK_OPTION,
      RECORD,
      PETMAP,
      BADGE,
      SAVE,
      HELP,
      SETTINGS,
      OPTION_CONFIRM,
      RIDE,
      TRANSMIT,
      PORTABLE_SHOP,
      PORTABLE_SHOP_BUY,
      PORTABLE_SHOP_CONFIRM,
      PORTABLE_SHOP_SERVICE_CONFIRM,
      FASHION_SHOP,
      FASHION_BUY_CONFIRM,
      FASHION_OPEN_CONFIRM,
      FASHION_REVEAL,
      FASHION_EXCHANGE,
      FASHION_EXCHANGE_CONFIRM,
      SOURCE_EAST_WHARF,
      SOURCE_CONVENIENCE_SHOP,
      RAINBOW_CHARM,
      RAINBOW_CHARM_CONFIRM,
      SOURCE_WORLD_SHOP,
      SOURCE_WORLD_SHOP_SELL,
      SOURCE_WORLD_SHOP_SELL_CONFIRM,
      SOURCE_WORLD_SHOP_RECOVER_CONFIRM,
      SOURCE_PET_BANK,
      WARDROBE,
      CHALLENGE,
      GIFT_CODE,
      BATTLE_PASS,
      BATTLE_PASS_HELP;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{GAMEMENU, GAMESYSTEM, BAG, TASK, TASK_MAP, TASK_OPTION, RECORD, PETMAP, BADGE, SAVE, HELP, SETTINGS, OPTION_CONFIRM, RIDE, TRANSMIT, PORTABLE_SHOP, PORTABLE_SHOP_BUY, PORTABLE_SHOP_CONFIRM, PORTABLE_SHOP_SERVICE_CONFIRM, FASHION_SHOP, FASHION_BUY_CONFIRM, FASHION_OPEN_CONFIRM, FASHION_REVEAL, FASHION_EXCHANGE, FASHION_EXCHANGE_CONFIRM, SOURCE_EAST_WHARF, SOURCE_CONVENIENCE_SHOP, RAINBOW_CHARM, RAINBOW_CHARM_CONFIRM, SOURCE_WORLD_SHOP, SOURCE_WORLD_SHOP_SELL, SOURCE_WORLD_SHOP_SELL_CONFIRM, SOURCE_WORLD_SHOP_RECOVER_CONFIRM, SOURCE_PET_BANK, WARDROBE, CHALLENGE, GIFT_CODE, BATTLE_PASS, BATTLE_PASS_HELP};
      }
   }

   private static final class SourceSellRow {
      final ItemDefinition item;
      final int count;
      final int sourcePrice;
      final int currency;
      final int unitPrice;

      SourceSellRow(ItemDefinition var1, int var2, int var3, int var4, int var5) {
         this.item = var1;
         this.count = var2;
         this.sourcePrice = var3;
         this.currency = var4;
         this.unitPrice = var5;
      }
   }

   private static final class BagRow {
      final ItemDefinition item;
      final int count;
      final boolean specialEgg;
      final int specialId;
      final String status;

      BagRow(ItemDefinition var1, int var2) {
         this(var1, var2, false, var1.id, (String)null);
      }

      BagRow(ItemDefinition var1, int var2, boolean var3, int var4, String var5) {
         this.item = var1;
         this.count = var2;
         this.specialEgg = var3;
         this.specialId = var4;
         this.status = var5;
      }

      String statusText() {
         return this.status == null ? String.valueOf(this.count) : this.status;
      }
   }

   private static final class TaskRow {
      final int number;
      final String title;
      final String detail;
      final boolean completed;

      TaskRow(int var1, String var2, String var3, boolean var4) {
         this.number = var1;
         this.title = var2;
         this.detail = var3;
         this.completed = var4;
      }
   }

   private static final class TaskMapNode {
      static final int W = 40;
      static final int H = 22;
      final int sceneId;
      final int roomIndex;
      final String label;
      final int x;
      final int y;
      final String[] lines;

      TaskMapNode(int var1, int var2, String var3, int var4, int var5, String... var6) {
         this.sceneId = var1;
         this.roomIndex = var2;
         this.label = var3;
         this.x = var4;
         this.y = var5;
         this.lines = var6;
      }
   }

   private static final class TaskRouteHint {
      final int currentIndex;
      final int targetIndex;
      final int nextIndex;
      final int[] path;
      final String currentLabel;
      final String targetLabel;
      final String nextLabel;

      TaskRouteHint(int var1, int var2, int var3, int[] var4, String var5, String var6, String var7) {
         this.currentIndex = var1;
         this.targetIndex = var2;
         this.nextIndex = var3;
         this.path = var4;
         this.currentLabel = var5;
         this.targetLabel = var6;
         this.nextLabel = var7;
      }
   }

   private static final class TaskOptionData {
      final TaskOptionReward[] rewards;
      final String[] options;
      final String summary;

      TaskOptionData(TaskOptionReward[] var1, String[] var2, String var3) {
         this.rewards = var1;
         this.options = var2;
         this.summary = var3;
      }

      String option(int var1) {
         return this.options.length == 0 ? "" : this.options[VqsvPanelRuntime.clamp(var1, 0, this.options.length - 1)];
      }

      static TaskOptionData empty() {
         return new TaskOptionData(new TaskOptionReward[0], new String[0], "");
      }

      static TaskOptionData branchTask(int var0) {
         SourceTaskOfferCatalog.Offer var1 = SourceTaskOfferCatalog.offer(var0);
         ArrayList var2 = new ArrayList();
         String var3 = "";

         for(SourceTaskOfferCatalog.Reward var7 : var1.rewards) {
            if (var7.type == SourceTaskOfferCatalog.RewardType.ITEM) {
               var2.add(VqsvPanelRuntime.TaskOptionReward.item(VqsvSourceOps.sourceItem(var7.id), "×" + var7.quantity));
            } else if (var7.type == SourceTaskOfferCatalog.RewardType.MATERIAL) {
               var2.add(VqsvPanelRuntime.TaskOptionReward.item(VqsvSourceOps.sourceMaterialItem(var7.id), "×" + var7.quantity));
            } else if (var7.type == SourceTaskOfferCatalog.RewardType.SPECIAL_REWARD) {
               var2.add(VqsvPanelRuntime.TaskOptionReward.item(VqsvSourceOps.sourceSpecialRewardItem(var7.id), "×" + var7.quantity));
            } else if (var7.type == SourceTaskOfferCatalog.RewardType.FIXED_UI_ICON) {
               var2.add(VqsvPanelRuntime.TaskOptionReward.fixedUiIcon(84, "×" + var7.quantity));
            } else if (var7.type == SourceTaskOfferCatalog.RewardType.MONEY) {
               var2.add(VqsvPanelRuntime.TaskOptionReward.fixedUiIcon(84, var7.quantity + " kim tiền"));
            } else if (var7.type == SourceTaskOfferCatalog.RewardType.PET_SPECIES) {
               BattleSpeciesRow var8 = VqsvBattleTables.instance().species(var7.id);
               String var10000 = var8 == null ? "Pet " + var7.id : var8.name("Pet " + var7.id);
               var3 = "#2" + var10000 + " #0x" + var7.quantity;
            }
         }

         if (var1.rewards.length == 0) {
            var3 = "Không có phần thưởng vật phẩm";
         }

         return new TaskOptionData((TaskOptionReward[])var2.toArray(new TaskOptionReward[0]), var1.options, var3);
      }
   }

   private static final class TaskOptionReward {
      final int iconSprite;
      final int iconCell;
      final String label;

      TaskOptionReward(int var1, int var2, String var3) {
         this.iconSprite = var1;
         this.iconCell = var2;
         this.label = var3;
      }

      static TaskOptionReward item(ItemDefinition var0, String var1) {
         return new TaskOptionReward(258, var0.iconCell, var0.name + " " + var1);
      }

      static TaskOptionReward fixedUiIcon(int var0, String var1) {
         return new TaskOptionReward(257, var0, var1);
      }
   }

   private static final class PetmapRow {
      final int speciesId;
      final String name;
      final int spriteId;
      final boolean seen;
      final boolean owned;

      PetmapRow(int var1, String var2, int var3, boolean var4, boolean var5) {
         this.speciesId = var1;
         this.name = var2;
         this.spriteId = var3;
         this.seen = var4;
         this.owned = var5;
      }
   }
}
