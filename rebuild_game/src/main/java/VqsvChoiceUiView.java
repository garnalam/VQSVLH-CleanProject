final class VqsvChoiceUiView {
    static final int ROW_ICON_SPRITE_ID = 258;
    static final int ROW_ICON_MODE = 2;

    static final VqsvChoiceUiView EMPTY = new VqsvChoiceUiView(
            "", "", "", VqsvText.Battle.PETSTATE_BACK,
            new String[0], new String[0], new String[0],
            new int[0], new int[0], 0, 0,
            -1, 5, true, true, false, false);

    final String title;
    final String subtitle;
    final String action;
    final String backAction;
    final String[] names;
    final String[] values;
    final String[] descriptions;
    final int[] ids;
    final int[] iconIds;
    final int selectedIndex;
    final int scroll;
    final int sourceListMode;
    final int visibleRows;
    final boolean actionVisible;
    final boolean backVisible;
    final boolean altActionVisible;
    final boolean altBackVisible;

    private VqsvChoiceUiView(String title, String subtitle, String action, String backAction,
                             String[] names, String[] values, String[] descriptions,
                             int[] ids, int[] iconIds, int selectedIndex, int scroll,
                             int sourceListMode, int visibleRows,
                             boolean actionVisible, boolean backVisible,
                             boolean altActionVisible, boolean altBackVisible) {
        this.title = title == null ? "" : title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.action = action == null ? "" : action;
        this.backAction = backAction == null ? "" : backAction;
        this.names = names == null ? new String[0] : names.clone();
        this.values = values == null ? new String[0] : values.clone();
        this.descriptions = descriptions == null ? new String[0] : descriptions.clone();
        this.ids = ids == null ? new int[0] : ids.clone();
        this.iconIds = iconIds == null ? new int[0] : iconIds.clone();
        this.sourceListMode = sourceListMode;
        this.visibleRows = Math.max(1, visibleRows);
        this.selectedIndex = clampIndex(selectedIndex, this.names.length);
        this.scroll = clampViewportScroll(scroll, this.names.length, this.visibleRows);
        this.actionVisible = actionVisible;
        this.backVisible = backVisible;
        this.altActionVisible = altActionVisible;
        this.altBackVisible = altBackVisible;
    }

    static VqsvChoiceUiView battle(String title, String subtitle, String action,
                                   java.util.List<String> names, java.util.List<String> values,
                                   java.util.List<String> descriptions,
                                   java.util.List<Integer> ids, java.util.List<Integer> iconIds,
                                   int selectedIndex, int scroll) {
        String[] nameArray = toStringArray(names);
        String[] valueArray = toStringArray(values);
        String[] descArray = toStringArray(descriptions);
        int[] idArray = toIntArray(ids, nameArray.length, -1);
        int[] iconArray = toIntArray(iconIds, nameArray.length, -1);
        return new VqsvChoiceUiView(title, subtitle, action, VqsvText.Battle.PETSTATE_BACK,
                nameArray, valueArray, descArray, idArray, iconArray,
                selectedIndex, scroll, sourceListMode(title, nameArray.length), 5,
                true, true, false, false);
    }

    static VqsvChoiceUiView fromScene(VqsvIntroDemo.Scene s) {
        if (s == null) {
            return EMPTY;
        }
        return new VqsvChoiceUiView(s.battleMenuTitle, s.battleMenuSubtitle, s.battleMenuAction,
                VqsvText.Battle.PETSTATE_BACK, s.battleMenuNames, s.battleMenuValues,
                s.battleMenuDescriptions, s.battleMenuIds, s.battleMenuIconIds,
                s.battleMenuIndex, s.battleMenuScroll,
                sourceListMode(s.battleMenuTitle, s.battleMenuNames.length), 5,
                true, true, false, false);
    }

    VqsvChoiceUiView withCursor(int selectedIndex, int scroll) {
        return new VqsvChoiceUiView(title, subtitle, action, backAction, names, values,
                descriptions, ids, iconIds, selectedIndex, scroll, sourceListMode, visibleRows,
                actionVisible, backVisible, altActionVisible, altBackVisible);
    }

    VqsvChoiceUiView withAlternateSoftkeys(String altAction) {
        return new VqsvChoiceUiView(title, subtitle, altAction, backAction, names, values,
                descriptions, ids, iconIds, selectedIndex, scroll, sourceListMode, visibleRows,
                false, false, true, true);
    }

    VqsvChoiceUiView withSourceCursor(int selectedIndex, int scroll) {
        int selected = clampIndex(selectedIndex, names.length);
        int offset = sourceBeOffset(clampScroll(scroll, selected, names.length, visibleRows), selected);
        return withCursor(selected, offset);
    }

    VqsvChoiceUiView withViewportScroll(int selectedIndex, int scroll) {
        int selected = clampIndex(selectedIndex, names.length);
        int offset = Math.max(0, Math.min(Math.max(0, names.length - visibleRows), scroll));
        return withCursor(selected, offset);
    }

    VqsvChoiceUiView moveUpSource() {
        if (names.length <= 1) {
            return withCursor(0, 0);
        }
        int selected = selectedIndex - 1;
        int offset = scroll;
        if (selected < 0) {
            selected = names.length - 1;
            offset = Math.max(0, names.length - visibleRows - (names.length - selected - 1));
        } else if (selected < offset) {
            offset = selected;
        }
        return withCursor(selected, sourceBeOffset(offset, selected));
    }

    VqsvChoiceUiView moveDownSource() {
        if (names.length <= 1) {
            return withCursor(0, 0);
        }
        int selected = selectedIndex + 1;
        int offset = scroll;
        if (selected >= names.length) {
            selected = 0;
            offset = 0;
        } else if (selected >= offset + visibleRows) {
            offset += 1;
            if (offset + visibleRows >= names.length) {
                offset = Math.max(0, names.length - visibleRows);
            }
        }
        return withCursor(selected, sourceBeOffset(offset, selected));
    }

    int size() {
        return names.length;
    }

    int visibleStart() {
        return Math.max(0, Math.min(scroll, Math.max(0, names.length - visibleRows)));
    }

    int visibleCount() {
        return Math.min(visibleRows, Math.max(0, names.length - visibleStart()));
    }

    int scrollbarThumbY(int trackY, int trackHeight) {
        if (names.length <= 0) {
            return trackY;
        }
        return trackY + Math.max(0, Math.min(selectedIndex, names.length - 1)) * trackHeight / names.length;
    }

    String nameAt(int index) {
        return index >= 0 && index < names.length ? names[index] : "";
    }

    String valueAt(int index) {
        return index >= 0 && index < values.length ? values[index] : "";
    }

    int iconAt(int index) {
        return index >= 0 && index < iconIds.length ? iconIds[index] : -1;
    }

    int idAt(int index) {
        return index >= 0 && index < ids.length ? ids[index] : -1;
    }

    String selectedDescription() {
        if (descriptions.length == 0) {
            return "";
        }
        int index = clampIndex(selectedIndex, descriptions.length);
        String text = descriptions[index];
        return text == null ? "" : text;
    }

    boolean isCatchMenu() {
        return "Pokemon ball".equals(TextBox.decodeMojibake(title));
    }

    boolean widgetVisible(int widgetId) {
        if (widgetId == 5) {
            return actionVisible;
        }
        if (widgetId == 6) {
            return backVisible;
        }
        if (widgetId == 59) {
            return altActionVisible;
        }
        if (widgetId == 60) {
            return altBackVisible;
        }
        if (widgetId == 52 || widgetId == 53) {
            return descriptionVisible();
        }
        int row = rowForWidget(widgetId);
        return row < 0 || row < visibleCount();
    }

    String widgetText(int widgetId, String fallback) {
        if (widgetId == 5) {
            return action.isEmpty() ? fallback : action;
        }
        if (widgetId == 6) {
            return backAction.isEmpty() ? fallback : backAction;
        }
        if (widgetId == 8) {
            return title;
        }
        if (widgetId == 9) {
            return subtitle;
        }
        if (widgetId == 59) {
            return action.isEmpty() ? fallback : action;
        }
        if (widgetId == 60) {
            return backAction.isEmpty() ? fallback : backAction;
        }
        int nameRow = rowForNameWidget(widgetId);
        if (nameRow >= 0) {
            return nameAt(visibleStart() + nameRow);
        }
        int valueRow = rowForValueWidget(widgetId);
        if (valueRow >= 0) {
            return valueAt(visibleStart() + valueRow);
        }
        return fallback;
    }

    boolean rowIconVisible(int visibleRow) {
        return visibleRow >= 0
                && visibleRow < visibleCount()
                && iconAt(visibleStart() + visibleRow) >= 0;
    }

    int rowIconCell(int visibleRow) {
        return iconAt(visibleStart() + visibleRow);
    }

    boolean descriptionVisible() {
        return isCatchMenu() || !selectedDescription().isEmpty();
    }

    private static String[] toStringArray(java.util.List<String> values) {
        return values == null ? new String[0] : values.toArray(new String[0]);
    }

    private static int[] toIntArray(java.util.List<Integer> values, int size, int fallback) {
        int[] result = new int[size];
        for (int i = 0; i < result.length; i++) {
            result[i] = values != null && i < values.size() && values.get(i) != null
                    ? values.get(i)
                    : fallback;
        }
        return result;
    }

    private static int clampIndex(int index, int size) {
        if (size <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(size - 1, index));
    }

    private int sourceBeOffset(int offset, int selected) {
        int result = clampScroll(offset, selected, names.length, visibleRows);
        if (sourceListMode == 1 && result > 0 && selected - result < visibleRows - 1) {
            result -= 1;
        }
        return clampScroll(result, selected, names.length, visibleRows);
    }

    private static int clampScroll(int scroll, int selectedIndex, int size, int visibleRows) {
        int maxScroll = Math.max(0, size - visibleRows);
        int result = Math.max(0, Math.min(maxScroll, scroll));
        if (selectedIndex < result) {
            result = selectedIndex;
        } else if (selectedIndex >= result + visibleRows) {
            result = selectedIndex - (visibleRows - 1);
        }
        return Math.max(0, Math.min(maxScroll, result));
    }

    private static int clampViewportScroll(int scroll, int size, int visibleRows) {
        return Math.max(0, Math.min(Math.max(0, size - visibleRows), scroll));
    }

    private static int sourceListMode(String title, int size) {
        if ("Pokemon ball".equals(TextBox.decodeMojibake(title))) {
            return -1;
        }
        return size > 5 ? 1 : -1;
    }

    private static int rowForWidget(int widgetId) {
        int nameRow = rowForNameWidget(widgetId);
        if (nameRow >= 0) {
            return nameRow;
        }
        int valueRow = rowForValueWidget(widgetId);
        if (valueRow >= 0) {
            return valueRow;
        }
        if (widgetId >= 54 && widgetId <= 58) {
            return widgetId - 54;
        }
        if (widgetId == 11 || widgetId == 16 || widgetId == 21 || widgetId == 26 || widgetId == 31) {
            return (widgetId - 11) / 5;
        }
        return -1;
    }

    private static int rowForNameWidget(int widgetId) {
        if (widgetId == 13 || widgetId == 18 || widgetId == 23 || widgetId == 28 || widgetId == 33) {
            return (widgetId - 13) / 5;
        }
        return -1;
    }

    private static int rowForValueWidget(int widgetId) {
        if (widgetId == 14 || widgetId == 19 || widgetId == 24 || widgetId == 29 || widgetId == 34) {
            return (widgetId - 14) / 5;
        }
        return -1;
    }
}
