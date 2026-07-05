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
        return s.text == null ? null : VqsvIntroDemo.Scene.waitForText();
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
        return s.text == null ? null : VqsvIntroDemo.Scene.waitForText();
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
        return VqsvIntroDemo.Scene.waitForText();
    }

    static Map<Integer, BagItem> initialSourceBagItems() {
        Map<Integer, BagItem> items = new HashMap<>();
        items.put(0, new BagItem(0, 0, 0, true));
        return items;
    }

    static Map<Integer, SourceSpecialReward> initialSourceSpecialRewards() {
        return new HashMap<>();
    }

    private static TextBox sourceInventoryPopup(String message, int qty) {
        String suffix = qty > 0 ? " x " + qty : "";
        return TextBox.openBox(message + suffix);
    }

    private static boolean sourceCanAddItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        BagItem entry = s.sourceBagItems.get(itemId);
        if (entry != null) {
            return entry.count < 99;
        }
        return qty <= 99;
    }

    private static boolean sourceCanRemoveItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        BagItem entry = s.sourceBagItems.get(itemId);
        return entry != null && entry.count - qty >= 0;
    }

    private static void sourceAddItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        SourceItem item = sourceItem(itemId);
        BagItem entry = s.sourceBagItems.get(itemId);
        if (entry == null) {
            s.sourceBagItems.put(itemId, new BagItem(itemId, Math.min(qty, 99), item.bagChannel, false));
            return;
        }
        entry.count = Math.min(entry.count + qty, 99);
    }

    private static void sourceRemoveItem(VqsvIntroDemo.Scene s, int itemId, int qty) {
        BagItem entry = s.sourceBagItems.get(itemId);
        if (entry == null) {
            return;
        }
        entry.count -= qty;
        if (entry.count <= 0 && !entry.keepAtZero) {
            s.sourceBagItems.remove(itemId);
        }
    }

    private static int sourceItemCount(VqsvIntroDemo.Scene s, int itemId) {
        BagItem entry = s.sourceBagItems.get(itemId);
        return entry == null ? 0 : entry.count;
    }

    private static SourceItem sourceItem(int itemId) {
        // Source data: aq.c[4][itemId][0] -> aq.d[textId], plus aq.c[4][itemId][5] bag channel.
        switch (itemId) {
            case 0:
                return new SourceItem(0, 261, VqsvText.Items.TAT_TRUNG_CAU, 0);
            case 1:
                return new SourceItem(1, 262, VqsvText.Items.PHONG_AN_CAU, 0);
            case 4:
                return new SourceItem(4, 265, VqsvText.Items.BANH_SANDWICH, 1);
            case 11:
                return new SourceItem(11, 272, VqsvText.Items.SINH_MENH_THACH, 4);
            default:
                return new SourceItem(itemId, 0, "", 0);
        }
    }
}
