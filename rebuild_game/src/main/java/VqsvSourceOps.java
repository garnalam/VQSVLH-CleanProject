import java.util.HashMap;
import java.util.Map;

final class VqsvSourceOps {
    private VqsvSourceOps() {
    }

    static Blocking op17Item(VqsvIntroDemo.Scene s, int mode, int itemId, int qty) {
        SourceItem item = sourceItem(itemId);
        if (mode == 0) {
            if (sourceCanAddItem(s, itemId, qty)) {
                sourceAddItem(s, itemId, qty);
                s.sourceStateTrace.add("PORTED/APPROX op17 add [" + mode + "," + itemId + "," + qty
                        + "] bagChannel=" + item.bagChannel + " count=" + sourceItemCount(s, itemId));
                s.text = sourceInventoryPopup(VqsvText.Common.ITEM_REWARD_PREFIX + item.name, qty);
            } else {
                s.sourceStateTrace.add("PORTED/APPROX op17 add-full [" + mode + "," + itemId + "," + qty + "]");
                s.text = sourceInventoryPopup(VqsvText.Common.ITEM_BAG_FULL, 0);
            }
        } else if (sourceCanRemoveItem(s, itemId, qty)) {
            sourceRemoveItem(s, itemId, qty);
            s.sourceStateTrace.add("PORTED/APPROX op17 remove [" + mode + "," + itemId + "," + qty
                    + "] bagChannel=" + item.bagChannel + " count=" + sourceItemCount(s, itemId));
            s.text = sourceInventoryPopup(VqsvText.Common.ITEM_LOST_PREFIX + item.name, qty);
        } else {
            s.sourceStateTrace.add("PORTED/APPROX op17 remove-missing [" + mode + "," + itemId + "," + qty + "]");
        }
        return s.text == null ? null : VqsvSceneScriptSupport.waitForText();
    }

    static Blocking op31CurrencyReward(VqsvIntroDemo.Scene s, int mode, int currencyKind, int amount) {
        if (mode == 0 && currencyKind == 0) {
            s.sourceMoney += amount;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 op31 add money=" + amount
                    + " total=" + s.sourceMoney);
            s.text = TextBox.openBox(VqsvText.Common.MONEY_REWARD_PREFIX + amount
                    + VqsvText.Common.MONEY_REWARD_SUFFIX);
        } else if (mode == 0 && currencyKind == 1) {
            s.sourceBadges += amount;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 op31 add badge=" + amount
                    + " total=" + s.sourceBadges);
            s.text = TextBox.openBox(VqsvText.Common.MONEY_REWARD_PREFIX + amount
                    + VqsvText.Common.BADGE_REWARD_SUFFIX);
        } else if (mode == 1 && currencyKind == 0) {
            s.sourceMoney -= amount;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 op31 remove money=" + amount
                    + " total=" + s.sourceMoney);
            s.text = TextBox.openBox(VqsvText.Common.MONEY_LOST_PREFIX + amount
                    + VqsvText.Common.MONEY_REWARD_SUFFIX);
        } else if (mode == 1 && currencyKind == 1) {
            s.sourceBadges -= amount;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 op31 remove badge=" + amount
                    + " total=" + s.sourceBadges);
            s.text = TextBox.openBox(VqsvText.Common.MONEY_LOST_PREFIX + amount
                    + VqsvText.Common.BADGE_LOST_SUFFIX);
        } else {
            s.sourceStateTrace.add("UNKNOWN room0 group6 op31 args=["
                    + mode + "," + currencyKind + "," + amount + "]");
            s.text = null;
        }
        return s.text == null ? null : VqsvSceneScriptSupport.waitForText();
    }

    static Blocking op19SpecialReward(VqsvIntroDemo.Scene s, int rewardId, int qty) {
        SourceSpecialReward reward = s.sourceSpecialRewards.computeIfAbsent(rewardId, SourceSpecialReward::fromSourceDb);
        reward.applySourceGameGSemantics(qty);
        s.sourceStateTrace.add("PORTED/APPROX room0 group6 op19 rewardId=" + rewardId
                + " qty=" + qty
                + " sourceRow=[" + reward.textId + "," + reward.iconId + "," + reward.descriptionTextId + "]"
                + " game.g path=" + reward.gameGPath
                + " unlocked=" + reward.unlocked
                + " stack=" + reward.stackCount);
        s.text = sourceInventoryPopup(VqsvText.Common.ITEM_REWARD_PREFIX + reward.name, qty);
        return VqsvSceneScriptSupport.waitForText();
    }

    static Blocking op18Material(VqsvIntroDemo.Scene s, int mode, int materialId, int qty) {
        SourceItem material = sourceMaterialItem(materialId);
        if (mode == 0) {
            if (sourceCanAddMaterial(s, materialId, qty)) {
                int addQty = sourceMaterialStoredQuantity(materialId, qty);
                sourceAddMaterial(s, materialId, addQty);
                s.sourceStateTrace.add("PORTED op18 aq.c[3] add [" + mode + "," + materialId + "," + qty
                        + "] storedQty=" + addQty
                        + " count=" + sourceMaterialCount(s, materialId)
                        + " bucket=sourceMaterialItems");
                s.text = sourceInventoryPopup(VqsvText.Common.ITEM_REWARD_PREFIX + material.name, addQty);
            } else {
                s.sourceStateTrace.add("PORTED op18 aq.c[3] add-full [" + mode + "," + materialId + "," + qty
                        + "] count=" + sourceMaterialCount(s, materialId));
                s.text = sourceInventoryPopup(VqsvText.Common.ITEM_BAG_FULL, 0);
            }
        } else if (mode == 1) {
            int removeQty = Math.max(0, qty);
            sourceRemoveMaterial(s, materialId, removeQty);
            s.sourceStateTrace.add("PORTED op18 aq.c[3] remove [" + mode + "," + materialId + "," + qty
                    + "] count=" + sourceMaterialCount(s, materialId)
                    + " bucket=sourceMaterialItems");
            s.text = sourceInventoryPopup(VqsvText.Common.ITEM_LOST_PREFIX + material.name, removeQty);
        } else {
            s.sourceStateTrace.add("UNKNOWN op18 aq.c[3] args=[" + mode + "," + materialId + "," + qty + "]");
            s.text = null;
        }
        return s.text == null ? null : VqsvSceneScriptSupport.waitForText();
    }

    static Map<Integer, BagItem> initialSourceBagItems() {
        Map<Integer, BagItem> items = new HashMap<>();
        items.put(0, new BagItem(0, 0, 0, true));
        return items;
    }

    static Map<Integer, SourceSpecialReward> initialSourceSpecialRewards() {
        return new HashMap<>();
    }

    static java.util.List<SourceEquipmentItem> initialSourceEquipmentItems() {
        return new java.util.ArrayList<>();
    }

    static java.util.List<SourceMaterialItem> initialSourceMaterialItems() {
        return new java.util.ArrayList<>();
    }

    static void sourceAddMaterial(VqsvIntroDemo.Scene s, int materialId, int qty) {
        if (materialId < 0 || qty <= 0) {
            return;
        }
        for (SourceMaterialItem item : s.sourceMaterialItems) {
            if (item.id == materialId) {
                item.count = Math.min(99, Math.max(0, item.count + qty));
                return;
            }
        }
        s.sourceMaterialItems.add(new SourceMaterialItem(materialId, Math.min(99, qty)));
    }

    static void sourceRemoveMaterial(VqsvIntroDemo.Scene s, int materialId, int qty) {
        if (materialId < 0 || qty <= 0) {
            return;
        }
        for (SourceMaterialItem item : s.sourceMaterialItems) {
            if (item.id == materialId) {
                item.count = Math.max(0, item.count - qty);
                return;
            }
        }
    }

    static int sourceMaterialCount(VqsvIntroDemo.Scene s, int materialId) {
        if (materialId < 0) {
            return 0;
        }
        for (SourceMaterialItem item : s.sourceMaterialItems) {
            if (item.id == materialId) {
                return Math.max(0, item.count);
            }
        }
        return 0;
    }

    static boolean sourceCanAddMaterial(VqsvIntroDemo.Scene s, int materialId, int qty) {
        if (materialId < 0 || qty <= 0) {
            return false;
        }
        int addQty = sourceMaterialStoredQuantity(materialId, qty);
        int current = sourceMaterialCount(s, materialId);
        return current > 0 ? current < 99 : addQty <= 99;
    }

    private static int sourceMaterialStoredQuantity(int materialId, int qty) {
        return materialId == 17 ? qty * 5 : qty;
    }

    private static TextBox sourceInventoryPopup(String message, int qty) {
        String suffix = qty > 0 ? " x " + qty : "";
        return TextBox.openBox(message + suffix);
    }

    static boolean sourceCanAddItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        BagItem entry = s.sourceBagItems.get(itemId);
        if (entry != null) {
            return entry.count < 99;
        }
        return qty <= 99;
    }

    static boolean sourceCanRemoveItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        BagItem entry = s.sourceBagItems.get(itemId);
        return entry != null && entry.count - qty >= 0;
    }

    static void sourceAddItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        SourceItem item = sourceItem(itemId);
        BagItem entry = s.sourceBagItems.get(itemId);
        if (entry == null) {
            s.sourceBagItems.put(itemId, new BagItem(itemId, Math.min(qty, 99), item.bagChannel, false));
            return;
        }
        entry.count = Math.min(entry.count + qty, 99);
    }

    static void sourceRemoveItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        BagItem entry = s.sourceBagItems.get(itemId);
        if (entry == null) {
            return;
        }
        entry.count -= qty;
        if (entry.count <= 0 && !entry.keepAtZero) {
            s.sourceBagItems.remove(itemId);
        }
    }

    static SourceSpecialReward sourceStackSpecialReward(VqsvIntroDemo.Scene s, int rewardId, int qty) {
        SourceSpecialReward reward = s.sourceSpecialRewards.computeIfAbsent(rewardId, SourceSpecialReward::fromSourceDb);
        reward.stackCount = Math.min(99, Math.max(0, reward.stackCount + qty));
        reward.gameGPath = "game.g.c(id,qty) stack q.O special row";
        return reward;
    }

    static int sourceItemCount(VqsvIntroDemo.Scene s, int itemId) {
        BagItem entry = s.sourceBagItems.get(itemId);
        return entry == null ? 0 : entry.count;
    }

    static SourceItem sourceItem(int itemId) {
        // Source data: aq.c[4][itemId] -> name/icon/description/behavior.
        return sourceTableItem(itemId, fallbackItemName(itemId), itemId, 0);
    }

    static SourceItem sourceMaterialItem(int materialId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(materialId);
        if (row == null) {
            return new SourceItem(materialId, 0, materialId, 0,
                    "T\u00e0i li\u1ec7u " + materialId, "", 2);
        }
        return new SourceItem(materialId, row.nameTextId, row.iconCell, row.descriptionTextId,
                sourceMaterialName(materialId), row.description(""), 2);
    }

    private static SourceItem sourceTableItem(int itemId, String fallbackName,
                                              int fallbackIcon, int fallbackBehavior) {
        BattleItemRow row = VqsvBattleTables.instance().item(itemId);
        if (row == null) {
            return new SourceItem(itemId, 0, fallbackIcon, 0, fallbackName, "", fallbackBehavior);
        }
        return new SourceItem(itemId, row.nameTextId, row.iconId, row.descriptionTextId,
                row.name(fallbackName), row.description(""), row.behavior);
    }

    private static String fallbackItemName(int itemId) {
        switch (itemId) {
            case 0:
                return VqsvText.Items.TAT_TRUNG_CAU;
            case 1:
                return VqsvText.Items.PHONG_AN_CAU;
            case 4:
                return VqsvText.Items.BANH_SANDWICH;
            case 13:
                return "Tr\u00e1nh qu\u00e1i ho\u00e0n";
            case 14:
                return "Gia t\u1ed1c d\u01b0\u1ee3c";
            default:
                return "Item " + itemId;
        }
    }

    static int sourceEquipmentIconCell(int equipmentId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(equipmentId);
        return row == null ? equipmentId : row.iconCell;
    }

    static String sourceEquipmentName(int equipmentId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(equipmentId);
        return row == null ? "Trang s\u1ee9c " + equipmentId : row.name("Trang s\u1ee9c " + equipmentId);
    }

    static String sourceEquipmentDescription(int equipmentId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(equipmentId);
        return row == null ? "" : row.description("");
    }

    static int sourceMaterialIconCell(int materialId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(materialId);
        return row == null ? materialId : row.iconCell;
    }

    static String sourceMaterialName(int materialId) {
        if (materialId == 17) {
            return "Ch\u00eca kh\u00f3a v\u00e0ng";
        }
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(materialId);
        return row == null ? "T\u00e0i li\u1ec7u " + materialId : row.name("T\u00e0i li\u1ec7u " + materialId);
    }

    static String sourceMaterialDescription(int materialId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(materialId);
        return row == null ? "" : row.description("");
    }
}
