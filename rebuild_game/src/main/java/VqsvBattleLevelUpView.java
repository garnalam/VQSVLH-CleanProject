final class VqsvBattleLevelUpView {
    static final VqsvBattleLevelUpView EMPTY = new VqsvBattleLevelUpView(false, false, "", -1, -1,
            0, 0, 1, 0, new int[4], new int[4], "");

    final boolean visible;
    final boolean leveled;
    final String name;
    final int visualId;
    final int elementId;
    final int level;
    final int expValue;
    final int expMax;
    final int expPercent;
    final int[] oldStats;
    final int[] newStats;
    final String message;

    VqsvBattleLevelUpView(boolean visible, boolean leveled, String name, int visualId, int elementId,
                          int level, int expValue, int expMax, int expPercent,
                          int[] oldStats, int[] newStats, String message) {
        this.visible = visible;
        this.leveled = leveled;
        this.name = name == null ? "" : name;
        this.visualId = visualId;
        this.elementId = elementId;
        this.level = level;
        this.expValue = Math.max(0, expValue);
        this.expMax = Math.max(1, expMax);
        this.expPercent = Math.max(0, Math.min(100, expPercent));
        this.oldStats = oldStats == null ? new int[4] : java.util.Arrays.copyOf(oldStats, 4);
        this.newStats = newStats == null ? new int[4] : java.util.Arrays.copyOf(newStats, 4);
        this.message = message == null ? "" : message;
    }
}
