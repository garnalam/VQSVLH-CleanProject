import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;

final class VqsvPanelRuntime {
    private enum Mode {
        GAMEMENU,
        GAMESYSTEM,
        BAG,
        TASK,
        RECORD,
        PETMAP,
        SAVE,
        HELP,
        SETTINGS,
        OPTION_CONFIRM,
        RIDE
    }

    private static final int[] MENU_ROW_WIDGETS = {15, 5, 6, 7, 8, 9};
    private static final int[] SYSTEM_ROW_WIDGETS = {6, 7, 8, 9};
    private static final int[] BAG_ROW_BACKGROUNDS = {17, 22, 27, 32, 37};
    private static final int[] BAG_ROW_ICONS = {18, 23, 28, 33, 38};
    private static final int[] BAG_ROW_NAMES = {19, 24, 29, 34, 39};
    private static final int[] BAG_ROW_COUNTS = {20, 25, 30, 35, 40};
    private static final int[] BAG_SPECIAL_ROW_ICONS = {137, 142, 147, 152, 157};
    private static final int[] BAG_SPECIAL_ROW_NAMES = {138, 143, 148, 153, 158};
    private static final int[] BAG_SPECIAL_ROW_COUNTS = {139, 144, 149, 154, 159};
    private static final int[] EGG_HATCH_RANDOM_THRESHOLDS = {76, 52, 28, 4, 0};
    private static final int[] EGG_HATCH_RANDOM_SPECIES = {0, 56, 58, 95, 72};
    private static final VqsvSourceRandom PANEL_RANDOM = VqsvSourceRandom.lazySourceSeeded();
    private static final int[] TASK_ROW_BACKGROUNDS = {11, 16, 21, 26, 31};
    private static final int[] TASK_ROW_NUMBERS = {12, 17, 22, 27, 32};
    private static final int[] TASK_ROW_NAMES = {13, 18, 23, 28, 33};
    private static final int[] TASK_ROW_STATUS = {14, 19, 24, 29, 34};
    private static final int[] PETMAP_TAB_CELLS = {6, 7, 8, 9, 10, 11, 12};
    private static final int[] PETMAP_TAB_LABELS = {13, 14, 15, 16, 17, 18, 19};
    private static final int[] PETMAP_ROW_BACKGROUNDS = {25, 29, 33, 37, 41};
    private static final int[] PETMAP_ROW_MARKERS = {44, 45, 46, 47, 48};
    private static final int[] PETMAP_ROW_NAMES = {27, 31, 35, 39, 43};
    private static final String[] PETMAP_TAB_NAMES = {
            "H\u1ecfa", "M\u1ed9c", "Th\u1ed5", "Th\u1ee7y", "\u0110i\u1ec7n", "Qu\u1ef7", "Phong"
    };
    private static final Pattern TASK_TEXT_PATTERN = Pattern.compile("\"([^\"]*)\"");
    private static final String[] MENU_LABELS = {
            "T\u00f9y th\u00e2n c\u1eeda h\u00e0ng",
            "S\u1ee7ng v\u1eadt",
            "L\u01b0ng bao",
            "\u0110\u1ed3 gi\u00e1m",
            "Nhi\u1ec7m v\u1ee5",
            "L\u01b0u d\u1eef li\u1ec7u"
    };
    private static final String[] SYSTEM_LABELS = {
            "Ti\u1ebfp t\u1ee5c tr\u00f2 ch\u01a1i",
            "Tr\u1ee3 gi\u00fap ch\u01a1i",
            "Thi\u1ebft l\u1eadp tr\u00f2 ch\u01a1i",
            "Tr\u1edf l\u1ea1i menu ch\u00ednh"
    };
    private static final String[] RIDE_LABELS = {
            "L\u1ee5c \u0111i \u0111i\u1ec3u",
            "H\u01b0 kh\u00f4ng h\u00e0nh gi\u1ea3",
            "H\u1ea3i \u00e2u",
            "Nham s\u01a1n long"
    };
    private static final String[] MENU_TITLE_TOKENS = {
            "#P605", "#P606", "#P607", "#P608", "#P609", "#P610"
    };

    boolean visible;
    int selected;
    int openedTicks;
    private Mode mode = Mode.GAMEMENU;
    private int taskTab;
    private int recordSelected;
    private int petmapTab;
    private int savePhase;
    private int helpPage;
    private int settingsLevel;
    private int bagTab;
    private int rideSelected;
    private int listScroll;
    private int bagMessageMode;
    private int rideMessageMode;
    private String pendingBagOpenBoxMessage = "";
    private int pendingHatchSpecies = -1;
    private int pendingHatchStorageResult = -1;
    private String saveMessage = "";
    private static List<String> mainTaskRows;
    private static List<String> branchTaskRows;
    private static List<String> chsRows;

    void open(VqsvIntroDemo.Scene s) {
        visible = true;
        mode = Mode.GAMEMENU;
        selected = clamp(selected, 0, MENU_LABELS.length - 1);
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k P=6 game.h.k gamemenu.ui open"
                + " selected=" + selected
                + " titleToken=" + MENU_TITLE_TOKENS[selected]
                + " money=" + s.sourceMoney
                + " badges=" + s.sourceBadges);
    }

    void openMenuAt(VqsvIntroDemo.Scene s, int selectedRow, String sourceReason) {
        selected = clamp(selectedRow, 0, MENU_LABELS.length - 1);
        open(s);
        s.sourceStateTrace.add("PORTED/PARTIAL panel gamemenu reopened selected="
                + selected + " reason=" + sourceReason);
    }

    void openGameSystemFromWorld(VqsvIntroDemo.Scene s) {
        visible = true;
        mode = Mode.GAMESYSTEM;
        selected = 0;
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL world.ui left softkey"
                + " source game.k P=0 key=131072 -> P=13 game.h.m gamesystem.ui open");
    }

    void close(VqsvIntroDemo.Scene s) {
        if (!visible) {
            return;
        }
        visible = false;
        s.sourceStateTrace.add("PORTED/PARTIAL panel " + closeTrace()
                + " selected=" + selected);
    }

    void tick(VqsvIntroDemo.Scene s) {
        if (!visible) {
            return;
        }
        openedTicks++;
        if (mode == Mode.SAVE) {
            tickSave(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.BAG) {
            tickBag(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.TASK) {
            tickTask(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.RECORD) {
            tickRecord(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.PETMAP) {
            tickPetmap(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.HELP) {
            tickHelp(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.SETTINGS) {
            tickSettings(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.OPTION_CONFIRM) {
            tickOptionConfirm(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.RIDE) {
            tickRide(s);
            consumeKeys(s);
            return;
        }
        int maxSelected = labels().length - 1;
        if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, maxSelected);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel "
                        + sourceTickMethod() + " key=4100 selected=" + selected
                        + titleTraceSuffix());
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, maxSelected);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel "
                        + sourceTickMethod() + " key=8448 selected=" + selected
                        + titleTraceSuffix());
            }
        } else if (s.key0) {
            confirm(s);
        } else if (s.keyBack) {
            close(s);
        }
        consumeKeys(s);
    }

    void mouseWheel(VqsvIntroDemo.Scene s, int steps) {
        if (!visible || steps == 0 || mode == Mode.SAVE
                || mode == Mode.HELP || mode == Mode.SETTINGS
                || mode == Mode.OPTION_CONFIRM) {
            return;
        }
        int rowCount = wheelRowCount(s);
        int maxScroll = Math.max(0, rowCount - 5);
        if (!isScrollablePanelList() || maxScroll <= 0) {
            return;
        }
        int before = listScroll;
        listScroll = clamp(listScroll + steps, 0, maxScroll);
        if (listScroll != before) {
            s.sourceStateTrace.add("PC_QOL mouse wheel panel list scrollbar"
                    + " mode=" + mode
                    + " scroll=" + listScroll
                    + " selected=" + selected
                    + " rows=" + rowCount);
        }
    }

    private boolean isScrollablePanelList() {
        return mode == Mode.BAG || mode == Mode.TASK || mode == Mode.PETMAP;
    }

    private int visibleListStart(int rowCount) {
        return clamp(listScroll, 0, Math.max(0, rowCount - 5));
    }

    private void keepSelectedVisible(int rowCount) {
        int maxScroll = Math.max(0, rowCount - 5);
        if (selected < listScroll) {
            listScroll = selected;
        } else if (selected >= listScroll + 5) {
            listScroll = selected - 4;
        }
        listScroll = clamp(listScroll, 0, maxScroll);
    }

    void render(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        if (!visible) {
            return;
        }
        if (mode == Mode.GAMESYSTEM) {
            renderGameSystem(g, font);
            return;
        }
        if (mode == Mode.SAVE) {
            renderSave(g, font);
            return;
        }
        if (mode == Mode.BAG) {
            renderBag(g, font, s);
            return;
        }
        if (mode == Mode.TASK) {
            renderTask(g, font, s);
            return;
        }
        if (mode == Mode.RECORD) {
            renderRecord(g, font, s);
            return;
        }
        if (mode == Mode.PETMAP) {
            renderPetmap(g, font, s);
            return;
        }
        if (mode == Mode.HELP) {
            renderHelp(g, font);
            return;
        }
        if (mode == Mode.SETTINGS) {
            renderSettings(g, font);
            return;
        }
        if (mode == Mode.OPTION_CONFIRM) {
            renderOptionConfirm(g, font);
            return;
        }
        if (mode == Mode.RIDE) {
            renderRide(g, font, s);
            return;
        }
        VqsvUiLayout layout = VqsvUiLayout.load("gamemenu.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawMenuFrame(g, layout, ui);
        drawRows(g, font, layout, ui, MENU_ROW_WIDGETS, MENU_LABELS);
        drawText(g, font, layout, 10, layout.text(10, "Menu tro choi"),
                color(layout.widget(10), 0xd0010e));
        drawText(g, font, layout, 14, MENU_TITLE_TOKENS[selected],
                color(layout.widget(14), 0xd0010e));
        drawText(g, font, layout, 12, layout.text(12, "Xac dinh"),
                color(layout.widget(12), 0xd0010e));
        drawText(g, font, layout, 11, layout.text(11, "Quay lai"),
                color(layout.widget(11), 0xd0010e));
        drawCell(layout, ui, g, 16);
        drawCell(layout, ui, g, 17);
        drawText(g, font, layout, 18, String.valueOf(s.sourceMoney),
                color(layout.widget(18), 0x1c6c91));
        drawText(g, font, layout, 19, String.valueOf(s.sourceBadges),
                color(layout.widget(19), 0x1c6c91));
    }

    boolean click(VqsvIntroDemo.Scene s, int x, int y) {
        if (!visible) {
            return false;
        }
        if (mode == Mode.SAVE) {
            if (x <= 48 && y >= 288) {
                s.key0 = true;
                return true;
            }
            if (x >= 188 && y >= 288) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        if (mode == Mode.BAG) {
            VqsvUiLayout layout = VqsvUiLayout.load("bag.ui");
            int size = bagRows(s, bagTab).size();
            int start = visibleListStart(size);
            for (int i = 0; i < BAG_ROW_BACKGROUNDS.length; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(BAG_ROW_BACKGROUNDS[i]);
                if (row != null && x >= row.x - 4 && x <= row.x + 136
                        && y >= row.y - 2 && y <= row.y + 14) {
                    selected = clamp(start + i, 0, Math.max(0, size - 1));
                    s.key0 = true;
                    return true;
                }
            }
            if (x <= 48 && y >= 288) {
                s.key0 = true;
                return true;
            }
            if (x >= 188 && y >= 288) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        if (mode == Mode.TASK) {
            VqsvUiLayout layout = VqsvUiLayout.load("task.ui");
            int size = taskRowsForRender(s, taskTab).size();
            int start = visibleListStart(size);
            for (int i = 0; i < TASK_ROW_BACKGROUNDS.length; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(TASK_ROW_BACKGROUNDS[i]);
                if (row != null && x >= row.x - 4 && x <= row.x + 136
                        && y >= row.y - 2 && y <= row.y + 14) {
                    selected = clamp(start + i, 0, Math.max(0, size - 1));
                    s.key0 = true;
                    return true;
                }
            }
            if (x <= 48 && y >= 288) {
                s.key0 = true;
                return true;
            }
            if (x >= 188 && y >= 288) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        if (mode == Mode.RECORD) {
            if (x < VqsvIntroDemo.W / 2 && y >= 210 && y <= 260) {
                recordSelected = 0;
                s.key0 = true;
                return true;
            }
            if (x >= VqsvIntroDemo.W / 2 && y >= 210 && y <= 260) {
                recordSelected = 1;
                s.key0 = true;
                return true;
            }
            if (x >= 188 && y >= 288) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        if (mode == Mode.PETMAP) {
            VqsvUiLayout layout = VqsvUiLayout.load("petmap.ui");
            int size = petmapRowsForRender(s, petmapTab).size();
            int start = visibleListStart(size);
            for (int i = 0; i < PETMAP_ROW_BACKGROUNDS.length; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(PETMAP_ROW_BACKGROUNDS[i]);
                if (row != null && x >= row.x - 4 && x <= row.x + 136
                        && y >= row.y - 2 && y <= row.y + 14) {
                    selected = clamp(start + i, 0, Math.max(0, size - 1));
                    s.key0 = true;
                    return true;
                }
            }
            if (x >= 188 && y >= 288) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        if (mode == Mode.RIDE) {
            VqsvUiLayout layout = VqsvUiLayout.load("ride.ui");
            for (int i = 0; i < 4; i++) {
                VqsvUiLayout.UiWidget slot = layout.widget(i + 4);
                if (slot != null && x >= slot.x - 4 && x <= slot.x + Math.max(34, slot.w) + 8
                        && y >= slot.y - 28 && y <= slot.y + 24) {
                    rideSelected = i;
                    s.key0 = true;
                    return true;
                }
            }
            if (x <= 48 && y >= 288) {
                s.key0 = true;
                return true;
            }
            if (x >= 188 && y >= 288) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        int[] rowWidgets = rowWidgets();
        VqsvUiLayout layout = VqsvUiLayout.load(uiName());
        for (int i = 0; i < rowWidgets.length; i++) {
            VqsvUiLayout.UiWidget row = layout.widget(rowWidgets[i]);
            if (row != null && x >= row.x - 4 && x <= row.x + Math.max(59, row.w) + 12
                    && y >= row.y - 2 && y <= row.y + 14) {
                selected = i;
                s.key0 = true;
                return true;
            }
        }
        if (x <= 48 && y >= 288) {
            s.key0 = true;
            return true;
        }
        if (x >= 188 && y >= 288) {
            s.keyBack = true;
            return true;
        }
        return true;
    }

    boolean hover(VqsvIntroDemo.Scene s, int x, int y) {
        if (!visible) {
            return false;
        }
        if (mode == Mode.BAG) {
            int row = widgetRowAt("bag.ui", BAG_ROW_BACKGROUNDS, x, y, 136);
            if (row >= 0) {
                int size = bagRows(s, bagTab).size();
                selected = clamp(visibleListStart(size) + row, 0, Math.max(0, size - 1));
            }
            return true;
        }
        if (mode == Mode.TASK) {
            int row = widgetRowAt("task.ui", TASK_ROW_BACKGROUNDS, x, y, 136);
            if (row >= 0) {
                int size = taskRowsForRender(s, taskTab).size();
                selected = clamp(visibleListStart(size) + row, 0, Math.max(0, size - 1));
            }
            return true;
        }
        if (mode == Mode.PETMAP) {
            int row = widgetRowAt("petmap.ui", PETMAP_ROW_BACKGROUNDS, x, y, 136);
            if (row >= 0) {
                int size = petmapRowsForRender(s, petmapTab).size();
                selected = clamp(visibleListStart(size) + row, 0, Math.max(0, size - 1));
            }
            return true;
        }
        if (mode == Mode.RIDE) {
            VqsvUiLayout layout = VqsvUiLayout.load("ride.ui");
            for (int i = 0; i < 4; i++) {
                VqsvUiLayout.UiWidget slot = layout.widget(i + 4);
                if (slot != null && x >= slot.x - 4 && x <= slot.x + Math.max(34, slot.w) + 8
                        && y >= slot.y - 28 && y <= slot.y + 24) {
                    rideSelected = i;
                    return true;
                }
            }
            return true;
        }
        int row = widgetRowAt(uiName(), rowWidgets(), x, y, 59);
        if (row >= 0) {
            selected = clamp(row, 0, labels().length - 1);
        }
        return true;
    }

    private static int widgetRowAt(String uiName, int[] rowWidgets, int x, int y, int fallbackWidth) {
        VqsvUiLayout layout = VqsvUiLayout.load(uiName);
        for (int i = 0; i < rowWidgets.length; i++) {
            VqsvUiLayout.UiWidget row = layout.widget(rowWidgets[i]);
            if (row != null && x >= row.x - 4 && x <= row.x + Math.max(fallbackWidth, row.w) + 12
                    && y >= row.y - 2 && y <= row.y + 14) {
                return i;
            }
        }
        return -1;
    }

    String selectedLabel() {
        if (mode == Mode.BAG) {
            return "L\u01b0ng bao";
        }
        if (mode == Mode.TASK) {
            List<TaskRow> rows = taskRowsForRender(null, taskTab);
            return rows.isEmpty() ? "Nhi\u1ec7m v\u1ee5" : rows.get(clamp(selected, 0, rows.size() - 1)).title;
        }
        if (mode == Mode.RECORD) {
            return recordSelected == 0 ? "Minh h\u1ecda" : "K\u1ef7 l\u1ee5c";
        }
        if (mode == Mode.PETMAP) {
            List<PetmapRow> rows = petmapRows(petmapTab);
            return rows.isEmpty() ? "Minh h\u1ecda" : rows.get(clamp(selected, 0, rows.size() - 1)).name;
        }
        if (mode == Mode.HELP) {
            return "Tr\u1ee3 gi\u00fap " + (helpPage + 1) + "/3";
        }
        if (mode == Mode.SETTINGS) {
            return "T\u00f9y ch\u1ecdn " + settingsLevel + "/3";
        }
        if (mode == Mode.OPTION_CONFIRM) {
            return selected == 1 ? "Kh\u00f4ng" : "PENDING reset";
        }
        if (mode == Mode.RIDE) {
            return RIDE_LABELS[rideSelected];
        }
        return labels()[selected];
    }

    String modeName() {
        return mode.name();
    }

    private void confirm(VqsvIntroDemo.Scene s) {
        if (mode == Mode.GAMEMENU) {
            if (selected == 0) {
                mode = Mode.GAMESYSTEM;
                selected = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=0"
                        + " close gamemenu.ui -> P=14 game.h.m gamesystem.ui open");
                return;
            }
            if (selected == 1) {
                visible = false;
                s.openWorldPetstate();
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=1"
                        + " c=0 o.m -> P=7 game.h.W petstate.ui open");
                return;
            }
            if (selected == 2) {
                mode = Mode.BAG;
                selected = 0;
                bagTab = 0;
                listScroll = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=2"
                        + " o.m -> P=8 game.h.Y bag.ui open b=0 title=Vat pham");
                return;
            }
            if (selected == 3) {
                mode = Mode.RECORD;
                selected = 0;
                recordSelected = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=3"
                        + " c=0 o.a(9) -> game.h.N record.ui open");
                return;
            }
            if (selected == 4) {
                mode = Mode.TASK;
                selected = 0;
                taskTab = 0;
                listScroll = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=4"
                        + " b=0 o.a(10) -> game.h.R task.ui open main tab");
                return;
            }
            if (selected == 5) {
                mode = Mode.SAVE;
                savePhase = 0;
                saveMessage = "C\u00f3 l\u01b0u d\u1eef li\u1ec7u kh\u00f4ng?";
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=5"
                        + " hide widgets 11/12 -> P=22 game.h.H msgtip.ui open"
                        + " text=Co luu du lieu khong?");
                return;
            }
            s.sourceStateTrace.add("PENDING panel game.h.l confirm selected=" + selected
                    + " label=" + MENU_LABELS[selected]
                    + " sourceTargetP=" + sourceTargetState(selected)
                    + " subpage not implemented in gamemenu slice");
            return;
        }

        if (selected == 0) {
            visible = false;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n confirm selected=0"
                    + " close gamesystem.ui -> P=0");
            return;
        }
        if (selected == 1) {
            mode = Mode.HELP;
            helpPage = 0;
            selected = 0;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n confirm selected=1"
                    + " o.a(20) close gamesystem.ui -> game.h.u help1.ui open r=0");
            return;
        }
        if (selected == 2) {
            mode = Mode.SETTINGS;
            selected = 0;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n confirm selected=2"
                    + " o.a(21) close gamesystem.ui -> game.h.w help.ui settings open g="
                    + settingsLevel);
            return;
        }
        if (selected == 3) {
            mode = Mode.OPTION_CONFIRM;
            selected = 1;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n confirm selected=3"
                    + " f=0 open option.ui c=1 widget12='' widget13=Khong");
            return;
        }
        s.sourceStateTrace.add("PENDING panel game.h.n confirm selected=" + selected
                + " label=" + SYSTEM_LABELS[selected]
                + " sourceTargetP=" + sourceSystemTargetState(selected)
                + " subpage not implemented in gamesystem slice");
    }

    private void tickBag(VqsvIntroDemo.Scene s) {
        if (bagMessageMode != 0) {
            if (s.text != null && s.text.readyForKey && s.key0) {
                s.text.confirm();
                if (s.text.disposed) {
                    int closedMode = bagMessageMode;
                    if (closedMode == 18) {
                        s.text = TextBox.openBox(pendingBagOpenBoxMessage);
                        bagMessageMode = 19;
                        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3"
                                + " q.N case0 f=2 close msgwarm.ui -> openbox.ui"
                                + " species=" + pendingHatchSpecies
                                + " storageResult=" + pendingHatchStorageResult);
                    } else {
                        s.text = null;
                        bagMessageMode = 0;
                        String closeState = closedMode == 19 ? "f=3->0" : "f=1->0";
                        String ui = closedMode == 19 ? "openbox.ui" : "msgwarm.ui";
                        s.sourceStateTrace.add("PORTED panel game.h.ac bag " + ui.replace(".ui", "")
                                + " key=196640 close " + ui + " " + closeState
                                + " mode=" + closedMode
                                + " remain bag.ui b=" + bagTab
                                + " selected=" + selected);
                    }
                }
            }
            return;
        }
        List<BagRow> rows = bagRows(s, bagTab);
        int maxSelected = Math.max(0, rows.size() - 1);
        selected = clamp(selected, 0, maxSelected);
        if (s.keyLeft || s.keyRight) {
            int before = bagTab;
            if (s.keyRight) {
                bagTab = (bagTab + 1) % 4;
            } else {
                bagTab = (bagTab + 3) % 4;
            }
            selected = 0;
            listScroll = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac tab key"
                    + " b=" + before + "->" + bagTab
                    + " title=" + bagTabTitle(bagTab));
        } else if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, maxSelected);
            keepSelectedVisible(rows.size());
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac key=4100"
                        + " bagTab=" + bagTab + " selected=" + selected);
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, maxSelected);
            keepSelectedVisible(rows.size());
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac key=8448"
                        + " bagTab=" + bagTab + " selected=" + selected);
            }
        } else if (s.keyBack) {
            mode = Mode.GAMEMENU;
            selected = 2;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac back"
                    + " b=" + bagTab + " o.a(6) close bag.ui -> gamemenu.ui selected=2");
        } else if (s.key0) {
            if (rows.isEmpty()) {
                s.sourceStateTrace.add("PENDING panel game.h.ac confirm bag empty"
                        + " bagTab=" + bagTab + " item use not implemented in bag slice");
            } else {
                BagRow row = rows.get(selected);
                if (bagTab == 3) {
                    useSpecialBagRow(s, row);
                    return;
                }
                if (row.item.id >= 0 && row.item.id <= 3) {
                    s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_CANNOT_USE,
                            VqsvText.Evolution.CONTINUE_PROMPT_5);
                    bagMessageMode = 1;
                    s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=0 itemId=" + row.item.id
                            + " case 0..3 -> msgwarm.ui f=1 text=Khong the su dung"
                            + " count=" + row.count
                            + " no inventory mutation");
                    return;
                }
                if (row.item.id == 13) {
                    useAvoidMonsterItem(s, row);
                    return;
                }
                if (row.item.id == 14) {
                    useEggAcceleratorItem(s, row);
                    return;
                }
                visible = false;
                s.openPanelBagState17Petstate(row.item.id);
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac confirm itemId=" + row.item.id
                        + " name=" + row.item.name
                        + " count=" + row.count
                        + " bagTab=0 default -> this.s=itemId P=17"
                        + " navigation/back only; confirm game.h.bo PENDING");
            }
        }
    }

    void returnToBagFromState17Back(VqsvIntroDemo.Scene s, int itemId) {
        visible = true;
        mode = Mode.BAG;
        bagTab = 0;
        openedTicks = 0;
        selected = clamp(selected, 0, Math.max(0, bagRows(s, bagTab).size() - 1));
        keepSelectedVisible(bagRows(s, bagTab).size());
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.Z back itemId=" + itemId
                + " o.a(8) close petstate.ui -> bag.ui"
                + " b=0 selected=" + selected);
    }

    private void useAvoidMonsterItem(VqsvIntroDemo.Scene s, BagRow row) {
        if (s.sourceAvoidMonsterTicks > 0) {
            s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_AVOID_ALREADY,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            bagMessageMode = 13;
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=0 itemId=13"
                    + " q.x=" + s.sourceAvoidMonsterTicks
                    + " already-active -> msgwarm.ui f=1 no inventory mutation");
            return;
        }
        if (s.currentSceneId == 3 && s.currentRoomIndex == 7) {
            s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_AVOID_FORBIDDEN,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            bagMessageMode = 14;
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=0 itemId=13"
                    + " source room game.k.a().f/g=3/7 forbidden -> msgwarm.ui f=1"
                    + " no inventory mutation");
            return;
        }
        if (!VqsvSourceOps.sourceCanRemoveItem(s, 13, 1)) {
            s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=0 itemId=13"
                    + " source q.b(item,1,0)=false unexpected visible row count=" + row.count);
            return;
        }
        BattleItemRow sourceRow = VqsvBattleTables.instance().item(13);
        int duration = sourceRow == null ? 0 : sourceRow.paramA;
        VqsvSourceOps.sourceRemoveItem(s, 13, 1);
        s.sourceAvoidMonsterTicks = duration;
        s.sourceAvoidMonsterElapsed = 0;
        List<BagRow> rowsAfter = bagRows(s, bagTab);
        selected = clamp(selected, 0, Math.max(0, rowsAfter.size() - 1));
        s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_AVOID_SUCCESS,
                VqsvText.Evolution.CONTINUE_PROMPT_5);
        bagMessageMode = 15;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=0 itemId=13"
                + " q.d(item,1,0) count=" + VqsvSourceOps.sourceItemCount(s, 13)
                + " q.x=aq.c[4][13][6]=" + duration
                + " q.w=0 q.c(1) noted -> msgwarm.ui f=1 selected=" + selected);
    }

    private void useEggAcceleratorItem(VqsvIntroDemo.Scene s, BagRow row) {
        int targetProgress = s.sourceEggType == 0 ? 10 : 30;
        if (!s.sourceEggActive || s.sourceEggProgress >= targetProgress) {
            s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_EGG_ACCEL_WARNING,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            bagMessageMode = 16;
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=0 itemId=14"
                    + " q.k(0)=" + s.sourceEggActive
                    + " q.I=" + s.sourceEggType
                    + " game.k.q=" + s.sourceEggProgress
                    + " target=" + targetProgress
                    + " -> msgwarm.ui f=1 no inventory mutation");
            return;
        }
        if (!VqsvSourceOps.sourceCanRemoveItem(s, 14, 1)) {
            s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=0 itemId=14"
                    + " source q.b(item,1,0)=false unexpected visible row count=" + row.count);
            return;
        }
        VqsvSourceOps.sourceRemoveItem(s, 14, 1);
        s.sourceEggProgress = targetProgress;
        List<BagRow> rowsAfter = bagRows(s, bagTab);
        selected = clamp(selected, 0, Math.max(0, rowsAfter.size() - 1));
        s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_EGG_ACCEL_SUCCESS,
                VqsvText.Evolution.CONTINUE_PROMPT_5);
        bagMessageMode = 17;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=0 itemId=14"
                + " q.k(0)=true q.I=" + s.sourceEggType
                + " game.k.q=" + s.sourceEggProgress
                + " q.d(item,1,0) count=" + VqsvSourceOps.sourceItemCount(s, 14)
                + " -> msgwarm.ui f=1 selected=" + selected
                + " hatch action b=3 case0 still pending");
    }

    private void useEggHatchAction(VqsvIntroDemo.Scene s, BagRow row) {
        if (!row.specialEgg) {
            s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 confirm specialId="
                    + row.item.id + " only q.N case0 hatch ported");
            return;
        }
        if (!s.sourceEggActive) {
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.N case0"
                    + " q.k(0)=false -> source breaks without UI");
            return;
        }
        if (!sourceEggReady(s)) {
            s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_EGG_HATCH_NOT_READY,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            bagMessageMode = 20;
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=3 q.N case0"
                    + " q.k(0)=true game.k.r=false q.I=" + s.sourceEggType
                    + " game.k.q=" + s.sourceEggProgress
                    + " -> msgwarm.ui f=1");
            return;
        }
        int storageResult = sourceStorageResult(s);
        if (storageResult == 2) {
            s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_EGG_HATCH_SPACE_FULL,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            bagMessageMode = 21;
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=3 q.N case0"
                    + " game.k.r=true q.y()=2 -> msgwarm.ui f=1"
                    + " party=" + s.sourcePets.size()
                    + " bank=" + s.sourcePetBank.size()
                    + " no egg mutation");
            return;
        }

        int species = selectHatchSpecies(s);
        BattleSpeciesRow speciesRow = VqsvBattleTables.instance().species(species);
        String petName = speciesRow == null ? "Pet " + species : speciesRow.name("Pet " + species);
        SourcePetState hatched = sourceHatchedPet(s, species, storageResult);
        if (storageResult == 0) {
            hatched.slot = s.sourcePets.size();
            s.sourcePets.add(hatched);
        } else if (storageResult == 1) {
            hatched.slot = s.sourcePetBank.size();
            s.sourcePetBank.add(hatched);
        }
        rememberHatchedSpecies(s, species);
        s.sourceEggProgress = 0;
        s.sourceEggActive = false;
        selected = clamp(selected, 0, Math.max(0, bagRows(s, bagTab).size() - 1));
        pendingHatchSpecies = species;
        pendingHatchStorageResult = storageResult;
        pendingBagOpenBoxMessage = VqsvText.Battle.panelBagEggHatchResult(petName, storageResult);
        s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_EGG_HATCH_SUCCESS,
                VqsvText.Evolution.CONTINUE_PROMPT_5);
        bagMessageMode = 18;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.N case0"
                + " game.k.r=true q.y()=" + storageResult
                + " game.k.q=0 q.j(0) q.k(0)=false"
                + " species=" + species
                + " q.I=" + s.sourceEggType
                + " party=" + s.sourcePets.size()
                + " bank=" + s.sourcePetBank.size()
                + " -> msgwarm.ui f=2 then openbox.ui f=3");
    }

    private void useSpecialBagRow(VqsvIntroDemo.Scene s, BagRow row) {
        if (row.specialEgg) {
            useEggHatchAction(s, row);
            return;
        }
        switch (row.specialId) {
            case 5:
                mode = Mode.RIDE;
                rideSelected = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.N case5"
                        + " confirm -> o.a(11) game.h.ad ride.ui"
                        + " sourceRow=[" + row.item.textId + "," + row.item.iconCell
                        + "," + row.item.descriptionTextId + "]"
                        + " close bag.ui selectedRide=0");
                return;
            case 6:
                s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 q.N case6"
                        + " confirm -> o.a(12) bag.ui subruntime not ported");
                return;
            case 10:
                s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 q.N case10"
                        + " confirm -> o.a(24) bag.ui subruntime not ported");
                return;
            case 7:
            case 8:
            case 9:
                s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 q.N case" + row.specialId
                        + " confirm -> s=id o.a(19) bag.ui special-use state not ported"
                        + " stack=" + row.count);
                return;
            default:
                s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 q.N case" + row.specialId
                        + " confirm source behavior not audited in current route");
        }
    }

    private static boolean sourceEggReady(VqsvIntroDemo.Scene s) {
        return (s.sourceEggType == 0 && s.sourceEggProgress >= 10)
                || (s.sourceEggType > 0 && s.sourceEggProgress >= 30);
    }

    private static int sourceStorageResult(VqsvIntroDemo.Scene s) {
        if (s.sourcePets.size() < 6) {
            return 0;
        }
        if (s.sourcePetBank.size() < 100) {
            return 1;
        }
        return 2;
    }

    private static int selectHatchSpecies(VqsvIntroDemo.Scene s) {
        if (s.sourceEggType == 0) {
            return 58;
        }
        int roll = PANEL_RANDOM.a("panel.bag.eggHatch.species", 100, s.sourceStateTrace);
        int index = 0;
        while (index < EGG_HATCH_RANDOM_THRESHOLDS.length
                && roll < EGG_HATCH_RANDOM_THRESHOLDS[index]) {
            index++;
        }
        index = clamp(index, 0, EGG_HATCH_RANDOM_SPECIES.length - 1);
        return EGG_HATCH_RANDOM_SPECIES[index];
    }

    private static void rememberHatchedSpecies(VqsvIntroDemo.Scene s, int species) {
        for (int i = 0; i < s.sourceEggType && i < s.sourceEggKnownSpecies.length; i++) {
            if (s.sourceEggKnownSpecies[i] == species) {
                return;
            }
        }
        if (s.sourceEggType < s.sourceEggKnownSpecies.length) {
            s.sourceEggKnownSpecies[s.sourceEggType] = species;
        }
        s.sourceEggType = Math.min(s.sourceEggType + 1, s.sourceEggKnownSpecies.length);
    }

    private static SourcePetState sourceHatchedPet(VqsvIntroDemo.Scene s, int species, int storageResult) {
        BattleSpeciesRow speciesRow = VqsvBattleTables.instance().species(species);
        int firstSkill = speciesRow == null ? 0 : speciesRow.element * 10;
        BattleSkillRow skillRow = VqsvBattleTables.instance().skill(firstSkill);
        int firstSkillPp = skillRow == null ? 0 : skillRow.ppMax;
        SourcePetState pet = new SourcePetState(0, species, 5, 2, -1, firstSkill, -1);
        pet.skillCooldowns[0] = firstSkillPp;
        pet.sourcePayload = pet.toSourcePayload();
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.g hatch addPet"
                + " species=" + species
                + " level=5 arg3=2 arg4=-1"
                + " skillPayload=[1," + firstSkill + "," + firstSkillPp + "]"
                + " q.y()=" + storageResult);
        return pet;
    }

    private void tickTask(VqsvIntroDemo.Scene s) {
        List<TaskRow> rows = taskRowsForRender(s, taskTab);
        int maxSelected = Math.max(0, rows.size() - 1);
        selected = clamp(selected, 0, maxSelected);
        if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, maxSelected);
            keepSelectedVisible(rows.size());
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.S key=4100"
                        + " taskTab=" + taskTab + " selected=" + selected);
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, maxSelected);
            keepSelectedVisible(rows.size());
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.S key=8448"
                        + " taskTab=" + taskTab + " selected=" + selected);
            }
        } else if (s.keyLeft) {
            if (taskTab != 0) {
                taskTab = 0;
                selected = 0;
                listScroll = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.S key=16400"
                        + " task tab main b=0 ba/bb");
            }
        } else if (s.keyRight) {
            if (taskTab != 1) {
                taskTab = 1;
                selected = 0;
                listScroll = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.S key=32832"
                        + " task tab branch b=1 ba/bb");
            }
        } else if (s.keyBack) {
            mode = Mode.GAMEMENU;
            selected = 4;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.S back"
                    + " close task.ui -> P=6 gamemenu selected=4");
        } else if (s.key0) {
            s.sourceStateTrace.add("PENDING panel game.h.S confirm task row"
                    + " taskOption/details not implemented in first task slice");
        }
    }

    private void tickRecord(VqsvIntroDemo.Scene s) {
        if (s.keyLeft) {
            if (recordSelected != 0) {
                recordSelected = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.O key=16400 record selected=0");
            }
        } else if (s.keyRight) {
            if (recordSelected != 1) {
                recordSelected = 1;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.O key=32832 record selected=1");
            }
        } else if (s.keyBack) {
            mode = Mode.GAMEMENU;
            selected = 3;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.O back"
                    + " b=3 o.a(6) close record.ui -> gamemenu.ui selected=3");
        } else if (s.key0) {
            if (recordSelected == 0) {
                mode = Mode.PETMAP;
                selected = 0;
                petmapTab = 0;
                listScroll = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.O confirm c=0"
                        + " -> o.a(11) game.h.P petmap.ui open");
            } else {
                s.sourceStateTrace.add("PENDING panel game.h.O confirm c=1"
                        + " record branch not implemented in petmap first slice");
            }
        }
    }

    private void tickPetmap(VqsvIntroDemo.Scene s) {
        List<PetmapRow> rows = petmapRowsForRender(s, petmapTab);
        int maxSelected = Math.max(0, rows.size() - 1);
        selected = clamp(selected, 0, maxSelected);
        if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, maxSelected);
            keepSelectedVisible(rows.size());
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.Q key=4100"
                        + " petmapTab=" + petmapTab + " selected=" + selected);
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, maxSelected);
            keepSelectedVisible(rows.size());
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.Q key=8448"
                        + " petmapTab=" + petmapTab + " selected=" + selected);
            }
        } else if (s.keyLeft) {
            int before = petmapTab;
            petmapTab = clamp(petmapTab - 1, 0, PETMAP_TAB_NAMES.length - 1);
            if (petmapTab != before) {
                selected = 0;
                listScroll = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.Q key=16400"
                        + " petmapTab=" + petmapTab + " aY/aZ");
            }
        } else if (s.keyRight) {
            int before = petmapTab;
            petmapTab = clamp(petmapTab + 1, 0, PETMAP_TAB_NAMES.length - 1);
            if (petmapTab != before) {
                selected = 0;
                listScroll = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.Q key=32832"
                        + " petmapTab=" + petmapTab + " aY/aZ");
            }
        } else if (s.keyBack) {
            mode = Mode.RECORD;
            selected = 0;
            recordSelected = 0;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.Q back"
                    + " close petmap.ui -> P=9 record.ui selected=0");
        } else if (s.key0) {
            s.sourceStateTrace.add("PENDING panel game.h.Q confirm petmap entry"
                    + " details not implemented in first petmap slice");
        }
    }

    private void tickSave(VqsvIntroDemo.Scene s) {
        if (savePhase == 0) {
            if (s.key0) {
                savePhase = 1;
                saveMessage = "\u0110ang l\u01b0u...";
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.K f=0 confirm"
                        + " text=Dang luu hide widgets 3/4");
            } else if (s.keyBack) {
                mode = Mode.GAMEMENU;
                selected = 5;
                openedTicks = 0;
                saveMessage = "";
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.K f=0 back"
                        + " close msgtip.ui -> P=6 gamemenu selected=5");
            }
            return;
        }
        if (savePhase == 1) {
            if (VqsvSaveRuntime.save(s)) {
                savePhase = 2;
                saveMessage = "L\u01b0u th\u00e0nh c\u00f4ng";
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.K f=1"
                        + " game.k.k save success text=Luu thanh cong");
            } else {
                savePhase = 2;
                saveMessage = "L\u01b0u th\u1ea5t b\u1ea1i";
                s.sourceStateTrace.add("PENDING panel game.h.K f=1"
                        + " game.k.k save failed text=Luu that bai");
            }
            return;
        }
        if (savePhase == 2) {
            visible = false;
            saveMessage = "";
            savePhase = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.K f=2"
                    + " close msgtip.ui and gamemenu.ui -> P=0");
        }
    }

    private void tickHelp(VqsvIntroDemo.Scene s) {
        if (s.keyLeft) {
            int before = helpPage;
            helpPage = clamp(helpPage - 1, 0, 2);
            if (helpPage != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.v key=16400"
                        + " help1.ui r=" + helpPage + " game.h.d(r)");
            }
        } else if (s.keyRight) {
            int before = helpPage;
            helpPage = clamp(helpPage + 1, 0, 2);
            if (helpPage != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.v key=32832"
                        + " help1.ui r=" + helpPage + " game.h.d(r)");
            }
        } else if (s.keyBack) {
            mode = Mode.GAMESYSTEM;
            selected = 1;
            helpPage = 0;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.v back"
                    + " close help1.ui -> P=13 gamesystem.ui selected=1");
        } else if (s.key0) {
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.v confirm ignored"
                    + " help1.ui source handles page/back only");
        }
    }

    private void tickSettings(VqsvIntroDemo.Scene s) {
        if (s.keyLeft) {
            int before = settingsLevel;
            settingsLevel = clamp(settingsLevel - 1, 0, 3);
            if (settingsLevel != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.x key=16400"
                        + " help.ui settings game.i.a().i g=" + settingsLevel);
            }
        } else if (s.keyRight) {
            int before = settingsLevel;
            settingsLevel = clamp(settingsLevel + 1, 0, 3);
            if (settingsLevel != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.x key=32832"
                        + " help.ui settings game.i.a().h g=" + settingsLevel);
            }
        } else if (s.keyBack) {
            mode = Mode.GAMESYSTEM;
            selected = 2;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.x back"
                    + " close help.ui -> P=13 gamesystem.ui selected=2 g=" + settingsLevel);
        } else if (s.key0) {
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.x confirm ignored"
                    + " settings source handles left/right/back only g=" + settingsLevel);
        }
    }

    private void tickOptionConfirm(VqsvIntroDemo.Scene s) {
        if (s.keyUp) {
            int before = selected;
            selected = 0;
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n option.ui key=4100 c=0");
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = 1;
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n option.ui key=8448 c=1");
            }
        } else if (s.keyBack) {
            mode = Mode.GAMESYSTEM;
            selected = 3;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n option.ui back"
                    + " close option.ui f=0 -> gamesystem.ui selected=3");
        } else if (s.key0) {
            if (selected == 1) {
                mode = Mode.GAMESYSTEM;
                selected = 3;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.n option.ui confirm c=1"
                        + " close option.ui f=0 no-reset -> gamesystem.ui selected=3");
            } else {
                visible = false;
                mode = Mode.GAMEMENU;
                openedTicks = 0;
                s.requestPanelTitleResetFromSourceOption();
            }
        }
    }

    private void tickRide(VqsvIntroDemo.Scene s) {
        if (rideMessageMode != 0) {
            if (s.text != null && s.text.readyForKey && s.key0) {
                s.text.confirm();
                if (s.text.disposed) {
                    int closedMode = rideMessageMode;
                    s.text = null;
                    rideMessageMode = 0;
                    s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae ride msgwarm"
                            + " key=196640 close msgwarm.ui mode=" + closedMode
                            + " return ride.ui selected=" + rideSelected);
                }
            }
            return;
        }
        if (s.keyLeft) {
            int before = rideSelected;
            rideSelected = clamp(rideSelected - 1, 0, 3);
            if (rideSelected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae key=16400"
                        + " ride.ui selected=" + rideSelected
                        + " label=" + RIDE_LABELS[rideSelected]);
            }
        } else if (s.keyRight) {
            int before = rideSelected;
            rideSelected = clamp(rideSelected + 1, 0, 3);
            if (rideSelected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae key=32832"
                        + " ride.ui selected=" + rideSelected
                        + " label=" + RIDE_LABELS[rideSelected]);
            }
        } else if (s.keyBack) {
            visible = false;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae key=262144"
                    + " close ride.ui -> P=0 selected=" + rideSelected);
        } else if (s.key0) {
            if (!sourceRideUnlocked(s, rideSelected)) {
                s.text = TextBox.msgWarm("Ch\u01b0a c\u00f3 s\u1ee7ng v\u1eadt c\u01b0\u1ee1i n\u00e0y",
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                rideMessageMode = 1;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae confirm ride"
                        + " q.f(" + rideSelected + ")=false"
                        + " -> msgwarm.ui text=Chua co sung vat cuoi nay");
                return;
            }
            if (!sourceRideUsable(s, rideSelected)) {
                s.text = TextBox.msgWarm("N\u01a1i n\u00e0y kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng s\u1ee7ng v\u1eadt c\u01b0\u1ee1i",
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
                rideMessageMode = 2;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae confirm ride"
                        + " q.f(" + rideSelected + ")=true q.g=false"
                        + " -> msgwarm.ui text=Noi nay khong the su dung sung vat cuoi");
                return;
            }
            int beforeRide = s.sourceRideActiveIndex;
            int beforeSpeed = s.sourcePlayerMoveSpeed;
            s.sourceRideActiveIndex = rideSelected;
            s.sourcePlayerMoveSpeed = rideSpeed(rideSelected);
            s.player.applyMode(0);
            visible = false;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ae confirm ride"
                    + " q.f(" + rideSelected + ")=true q.g=true"
                    + " q.h(" + rideSelected + ") activeRide=" + beforeRide + "->" + s.sourceRideActiveIndex
                    + " d[0]=" + beforeSpeed + "->" + s.sourcePlayerMoveSpeed
                    + " close ride.ui -> P=0"
                    + " PENDING visual super.a(" + (rideSelected + 1) + ",false)");
        }
    }

    private void renderRide(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("ride.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        SpriteAnim rideIcons = SpriteAnim.load(260);
        drawCell(layout, ui, g, 1);
        drawCell(layout, ui, g, 2);
        drawCell(layout, ui, g, 3);
        for (int i = 0; i < 4; i++) {
            boolean unlocked = sourceRideUnlocked(s, i);
            boolean usable = sourceRideUsable(s, i);
            int iconCell = unlocked ? (i == rideSelected ? i : i + 8) : i + 4;
            drawCellTopLeft(rideIcons, g, iconCell,
                    layout.x(i + 4, 32 + i * 44),
                    layout.y(i + 4, 270));
            drawTextWide(g, font, layout, i + 8,
                    i == rideSelected && unlocked ? RIDE_LABELS[i] : "",
                    0, layout.w(i + 8, 44),
                    color(layout.widget(i + 8), 0x1c6c91));
            if (unlocked && !usable) {
                drawCellTopLeft(ui, g, 131,
                        layout.x(i + 16, 45 + i * 44),
                        layout.y(i + 16, 298));
            }
        }
    }

    private void renderGameSystem(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("gamesystem.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawSystemFrame(g, layout, ui);
        drawRows(g, font, layout, ui, SYSTEM_ROW_WIDGETS, SYSTEM_LABELS);
        drawText(g, font, layout, 2, layout.text(2, "He thong menu"),
                color(layout.widget(2), 0xd0010e));
        drawText(g, font, layout, 10, layout.text(10, "Xac dinh"),
                color(layout.widget(10), 0xd0010e));
        drawText(g, font, layout, 11, layout.text(11, "Quay lai"),
                color(layout.widget(11), 0xd0010e));
    }

    private void renderHelp(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("help1.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        SpriteAnim effectIcons = SpriteAnim.load(325);
        drawHelpFrame(g, layout, ui);
        drawText(g, font, layout, 5, "Tr\u1ee3 gi\u00fap",
                color(layout.widget(5), 0xd0010e));
        drawText(g, font, layout, 6, layout.text(6, "Quay lai"),
                color(layout.widget(6), 0xffffff));
        drawText(g, font, layout, 39, (helpPage + 1) + "/3",
                color(layout.widget(39), 0x1c6c91));
        if (helpPage == 0) {
            drawMultilineText(g, font, layout, 8,
                    "Nh\u1ea5n n\u00fat 2, 4, 6, 8 \u0111\u1ec3 di chuy\u1ec3n"
                            + "#nN\u00fat 5: c\u00f4ng k\u00edch, \u0111\u1ed1i tho\u1ea1i, x\u00e1c nh\u1eadn"
                            + "#nN\u00fat 1, 3: Xem nhi\u1ec7m v\u1ee5"
                            + "#nN\u00fat 9: l\u1ef1a ch\u1ecdn s\u1ee7ng v\u1eadt c\u01b0\u1ee1i"
                            + "#nN\u00fat 0: Xem b\u1ea3n \u0111\u1ed3"
                            + "#nN\u00fat m\u1ec1m tr\u00e1i: menu h\u1ec7 th\u1ed1ng"
                            + "#nN\u00fat m\u1ec1m ph\u1ea3i: menu tr\u00f2 ch\u01a1i",
                    color(layout.widget(8), 0x1c6c91));
            return;
        }
        for (int row = 0; row < 14; row++) {
            int entry = (helpPage - 1) * 14 + row;
            int iconWidget = 9 + (row << 1);
            int textWidget = iconWidget + 1;
            if (entry >= 26) {
                continue;
            }
            VqsvUiLayout.UiWidget icon = layout.widget(iconWidget);
            if (icon != null) {
                drawCellTopLeft(effectIcons, g, entry + 1, icon.x, icon.y);
            }
            drawTextWide(g, font, layout, textWidget, helpEffectText(entry), 0, 44,
                    color(layout.widget(textWidget), 0x1c6c91));
        }
    }

    private void renderSettings(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("help.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawHelpFrame(g, layout, ui);
        drawCell(layout, ui, g, 7);
        drawText(g, font, layout, 5, "T\u00f9y ch\u1ecdn",
                color(layout.widget(5), 0xd0010e));
        drawTextWide(g, font, layout, 7, layout.text(7, "Xac dinh"), 0, 52,
                color(layout.widget(7), 0xffffff));
        drawTextWide(g, font, layout, 9, layout.text(9, "Am luong"), 0, 42,
                color(layout.widget(9), 0x1c6c91));
        for (int id = 10; id <= 12; id++) {
            VqsvUiLayout.UiWidget widget = layout.widget(id);
            if (widget == null) {
                continue;
            }
            int level = id - 9;
            int color = level <= settingsLevel ? 0xfff79c : 0x7da884;
            g.setColor(new Color(color & 0xffffff));
            g.fillRect(widget.x, widget.y, Math.max(5, widget.w), 12);
            g.setColor(new Color(0x31526b));
            g.drawRect(widget.x, widget.y, Math.max(5, widget.w), 12);
        }
    }

    private void renderOptionConfirm(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("option.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawOptionRow(g, font, layout, ui, 0, 10, 12, 8, "");
        drawOptionRow(g, font, layout, ui, 1, 11, 13, 9, "Kh\u00f4ng");
    }

    private void renderSave(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgtip.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawCell(layout, ui, g, 1);
        drawText(g, font, layout, 2, saveMessage, color(layout.widget(2), 0x1c6c91));
        if (savePhase == 0) {
            drawCell(layout, ui, g, 3);
            drawCell(layout, ui, g, 4);
        }
    }

    private void renderBag(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("bag.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        SpriteAnim itemIcons = SpriteAnim.load(258);
        drawBagFrame(g, layout, ui);
        drawText(g, font, layout, 5, layout.text(5, "Lung bao"),
                color(layout.widget(5), 0xd0010e));
        drawText(g, font, layout, 6, layout.text(6, "Roi di"),
                color(layout.widget(6), 0xffffff));
        drawText(g, font, layout, 7, bagActionLabel(s),
                color(layout.widget(7), 0xffffff));
        drawText(g, font, layout, 9, layout.text(9, "Tieu hao"),
                bagTab == 0 ? colorSelected(layout.widget(9)) : color(layout.widget(9), 0x00009a));
        drawText(g, font, layout, 10, layout.text(10, "Trang suc"),
                bagTab == 1 ? colorSelected(layout.widget(10)) : color(layout.widget(10), 0x00009a));
        drawText(g, font, layout, 11, layout.text(11, "Tai lieu"),
                bagTab == 2 ? colorSelected(layout.widget(11)) : color(layout.widget(11), 0x00009a));
        drawText(g, font, layout, 12, layout.text(12, "Dac thu"),
                bagTab == 3 ? colorSelected(layout.widget(12)) : color(layout.widget(12), 0x00009a));
        drawText(g, font, layout, 14, layout.text(14, "Vat pham"),
                color(layout.widget(14), 0x1c6c91));
        drawText(g, font, layout, 15, layout.text(15, "So luong"),
                color(layout.widget(15), 0x1c6c91));

        List<BagRow> rows = bagRows(s, bagTab);
        int first = visibleListStart(rows.size());
        for (int i = 0; i < BAG_ROW_BACKGROUNDS.length; i++) {
            int rowIndex = first + i;
            VqsvUiLayout.UiWidget bg = layout.widget(BAG_ROW_BACKGROUNDS[i]);
            if (bg != null) {
                int cell = rowIndex == selected ? bg.altId : bg.imageId;
                drawCellTopLeft(ui, g, cell, bg.x, bg.y);
            }
            if (rowIndex >= rows.size()) {
                continue;
            }
            BagRow row = rows.get(rowIndex);
            int iconWidget = bagTab == 3 ? BAG_SPECIAL_ROW_ICONS[i] : BAG_ROW_ICONS[i];
            int nameWidget = bagTab == 3 ? BAG_SPECIAL_ROW_NAMES[i] : BAG_ROW_NAMES[i];
            int countWidget = bagTab == 3 ? BAG_SPECIAL_ROW_COUNTS[i] : BAG_ROW_COUNTS[i];
            drawCellTopLeft(itemIcons, g, row.item.iconCell,
                    layout.x(iconWidget, layout.x(BAG_ROW_ICONS[i], 57)),
                    layout.y(iconWidget, layout.y(BAG_ROW_ICONS[i], 125)));
            drawText(g, font, layout, nameWidget, row.item.name,
                    rowIndex == selected ? 0xffa500 : color(layout.widget(nameWidget), 0x1c6c91));
            drawText(g, font, layout, countWidget, row.statusText(),
                    color(layout.widget(countWidget), 0x1c6c91));
        }
        String description = "";
        if (!rows.isEmpty()) {
            description = rows.get(clamp(selected, 0, rows.size() - 1)).item.description;
        }
        int descriptionWidget = bagTab == 3 ? 163 : 46;
        drawText(g, font, layout, descriptionWidget, description,
                color(layout.widget(descriptionWidget), 0xd0010e));
        drawBagScrollbar(g, layout, rows.size(), first);
    }

    private void renderTask(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("task.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawTaskFrame(g, layout, ui);
        drawTextWide(g, font, layout, 2, layout.text(2, "Nhiem vu"), -28, 80,
                color(layout.widget(2), 0xd0010e));
        drawCellState(layout, ui, g, 6, taskTab == 0);
        drawCellState(layout, ui, g, 7, taskTab == 1);
        drawTextWide(g, font, layout, 8, layout.text(8, "Nhiem vu chinh"), -2, 64,
                taskTab == 0 ? colorSelected(layout.widget(8)) : color(layout.widget(8), 0x00009a));
        drawTextWide(g, font, layout, 9, layout.text(9, "Nhiem vu phu"), -2, 64,
                taskTab == 1 ? colorSelected(layout.widget(9)) : color(layout.widget(9), 0x00009a));
        drawText(g, font, layout, 41, layout.text(41, "Xac dinh"),
                color(layout.widget(41), 0xffffff));
        drawText(g, font, layout, 42, layout.text(42, "Quay lai"),
                color(layout.widget(42), 0xffffff));

        List<TaskRow> rows = taskRowsForRender(s, taskTab);
        int first = visibleListStart(rows.size());
        for (int i = 0; i < TASK_ROW_BACKGROUNDS.length; i++) {
            int rowIndex = first + i;
            VqsvUiLayout.UiWidget bg = layout.widget(TASK_ROW_BACKGROUNDS[i]);
            if (bg != null) {
                int cell = rowIndex == selected ? bg.altId : bg.imageId;
                drawCellTopLeft(ui, g, cell, bg.x, bg.y);
            }
            if (rowIndex >= rows.size()) {
                continue;
            }
            TaskRow row = rows.get(rowIndex);
            int textColor = rowIndex == selected ? 0xffa500 : color(layout.widget(TASK_ROW_NAMES[i]), 0x1c6c91);
            drawText(g, font, layout, TASK_ROW_NUMBERS[i], String.valueOf(row.number),
                    color(layout.widget(TASK_ROW_NUMBERS[i]), 0x1c6c91));
            drawText(g, font, layout, TASK_ROW_NAMES[i], row.title, textColor);
            drawText(g, font, layout, TASK_ROW_STATUS[i], row.completed ? "Ho\u00e0n th\u00e0nh" : "",
                    color(layout.widget(TASK_ROW_STATUS[i]), 0x1c6c91));
        }
        String detail = "";
        if (!rows.isEmpty()) {
            detail = rows.get(clamp(selected, 0, rows.size() - 1)).detail;
        }
        drawText(g, font, layout, 36, detail, color(layout.widget(36), 0xd0010e));
        drawText(g, font, layout, 37, taskTab == 0
                        ? "\u0110\u1ea7u m\u1ed1i ch\u00ednh ho\u00e0n th\u00e0nh \u0111\u1ed9: "
                        : "Chi nh\u00e1nh ho\u00e0n th\u00e0nh \u0111\u1ed9: ",
                color(layout.widget(37), 0x1c6c91));
        drawText(g, font, layout, 38, taskProgressText(s, taskTab),
                color(layout.widget(38), 0xffffff));
        drawTaskScrollbar(g, layout, rows.size(), first);
    }

    private void renderRecord(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("record.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawRecordFrame(g, layout, ui);
        drawTextWide(g, font, layout, 11, layout.text(11, "Hinh Kam"), -8, 156,
                color(layout.widget(11), 0xd0010e));
        drawText(g, font, layout, 12, layout.text(12, "Bat duoc sung vat"),
                color(layout.widget(12), 0x1c6c91));
        drawText(g, font, layout, 14, String.valueOf(s.sourcePets.size() + s.sourcePetBank.size()),
                color(layout.widget(14), 0xff0000));
        drawText(g, font, layout, 15, layout.text(15, "Bat duoc sung vat"),
                color(layout.widget(15), 0x1c6c91));
        drawText(g, font, layout, 17, String.valueOf(distinctOwnedSpecies(s)),
                color(layout.widget(17), 0xff0000));
        drawText(g, font, layout, 18, layout.text(18, "Dat duoc sung vat hiem"),
                color(layout.widget(18), 0x1c6c91));
        drawText(g, font, layout, 20, String.valueOf(rareOwnedSpecies(s)),
                color(layout.widget(20), 0xff0000));
        drawText(g, font, layout, 24, layout.text(24, "Dat duoc than thu"),
                color(layout.widget(24), 0x1c6c91));
        drawText(g, font, layout, 26, "0", color(layout.widget(26), 0xff0000));
        drawText(g, font, layout, 27, layout.text(27, "Dat duoc huy hieu"),
                color(layout.widget(27), 0x1c6c91));
        drawText(g, font, layout, 29, String.valueOf(s.sourceBadges),
                color(layout.widget(29), 0xff0000));
        drawText(g, font, layout, 30, layout.text(30, "Tong thoi gian choi"),
                color(layout.widget(30), 0x1c6c91));
        drawText(g, font, layout, 31, "00:00", color(layout.widget(31), 0xff0000));
        drawText(g, font, layout, 33, layout.text(33, "Xac dinh"),
                color(layout.widget(33), 0xffffff));
        drawText(g, font, layout, 34, layout.text(34, "Quay lai"),
                color(layout.widget(34), 0xffffff));
        drawRecordSelection(g, layout, recordSelected);
    }

    private void renderPetmap(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("petmap.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawPetmapFrame(g, layout, ui);
        drawTextWide(g, font, layout, 2, layout.text(2, "Minh hoa"), -28, 84,
                color(layout.widget(2), 0xd0010e));
        for (int i = 0; i < PETMAP_TAB_CELLS.length; i++) {
            drawCellState(layout, ui, g, PETMAP_TAB_CELLS[i], i == petmapTab);
            drawTextWide(g, font, layout, PETMAP_TAB_LABELS[i], PETMAP_TAB_NAMES[i], -1, 18,
                    i == petmapTab ? colorSelected(layout.widget(PETMAP_TAB_LABELS[i]))
                            : color(layout.widget(PETMAP_TAB_LABELS[i]), 0x00009a));
        }
        List<PetmapRow> rows = petmapRowsForRender(s, petmapTab);
        int first = visibleListStart(rows.size());
        for (int i = 0; i < PETMAP_ROW_BACKGROUNDS.length; i++) {
            int rowIndex = first + i;
            VqsvUiLayout.UiWidget bg = layout.widget(PETMAP_ROW_BACKGROUNDS[i]);
            if (bg != null) {
                int cell = rowIndex == selected ? bg.altId : bg.imageId;
                drawCellTopLeft(ui, g, cell, bg.x, bg.y);
            }
            if (rowIndex >= rows.size()) {
                continue;
            }
            PetmapRow row = rows.get(rowIndex);
            drawCellTopLeft(ui, g, row.owned ? 101 : 102,
                    layout.x(PETMAP_ROW_MARKERS[i], 50), layout.y(PETMAP_ROW_MARKERS[i], 101));
            drawText(g, font, layout, PETMAP_ROW_NAMES[i], row.name,
                    rowIndex == selected ? 0xffa500 : color(layout.widget(PETMAP_ROW_NAMES[i]), 0x1c6c91));
        }
        if (!rows.isEmpty()) {
            PetmapRow row = rows.get(clamp(selected, 0, rows.size() - 1));
            drawText(g, font, layout, 20, PETMAP_TAB_NAMES[petmapTab] + " "
                            + ownedCount(rows) + "/" + rows.size(),
                    color(layout.widget(20), 0x1c6c91));
            if (row.owned && row.spriteId >= 0) {
                drawSpriteCellTopLeft(g, row.spriteId, 0, layout.x(21, 104), layout.y(21, 172));
            }
        }
        drawPetmapScrollbar(g, layout, rows.size(), first);
        drawText(g, font, layout, 49, layout.text(49, "Xac dinh"),
                color(layout.widget(49), 0xffffff));
        drawText(g, font, layout, 50, layout.text(50, "Quay lai"),
                color(layout.widget(50), 0xffffff));
    }

    private void drawMenuFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        fillBand(g, layout, 2, 0xC6F0FF, 7);
        fillBand(g, layout, 3, 0xBEE7F2, 102);
        fillBand(g, layout, 4, 0x6CB7BB, 8);
        drawCell(layout, ui, g, 1);
        drawCell(layout, ui, g, 13);
    }

    private void drawSystemFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        fillBand(g, layout, 3, 0xC6F0FF, 7);
        fillBand(g, layout, 4, 0xBEE7F2, 102);
        fillBand(g, layout, 5, 0x6CB7BB, 8);
        drawCell(layout, ui, g, 1);
    }

    private void drawHelpFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        fillBand(g, layout, 3, 0xC6F0FF, 9);
        fillBand(g, layout, 1, 0xBEE7F2, 158);
        fillBand(g, layout, 2, 0x6CB7BB, 8);
        drawCell(layout, ui, g, 4);
        drawCell(layout, ui, g, 6);
    }

    private void drawOptionRow(Graphics2D g, FontBitmap font, VqsvUiLayout layout, SpriteAnim ui,
                               int row, int bgWidget, int textWidget, int iconWidget, String text) {
        VqsvUiLayout.UiWidget bg = layout.widget(bgWidget);
        if (bg != null && bg.altId >= 0) {
            drawCellTopLeft(ui, g, bg.altId, bg.x, bg.y);
            if (row == selected) {
                g.setColor(new Color(0xffa500));
                g.drawRect(bg.x - 1, bg.y - 1, Math.max(1, bg.w), 23);
            }
        }
        VqsvUiLayout.UiWidget icon = layout.widget(iconWidget);
        if (icon != null && icon.imageId >= 0) {
            drawCellTopLeft(ui, g, icon.imageId, icon.x, icon.y);
        }
        drawText(g, font, layout, textWidget, text, row == selected
                ? 0xffa500 : color(layout.widget(textWidget), 0x1c6c91));
    }

    private void drawBagFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        fillBand(g, layout, 3, 0xC6F0FF, 9);
        fillBand(g, layout, 1, 0xBEE7F2, 159);
        fillBand(g, layout, 2, 0x6CB7BB, 8);
        drawCell(layout, ui, g, 4);
        drawCell(layout, ui, g, 6);
        drawCell(layout, ui, g, 7);
        for (int id = 9; id <= 12; id++) {
            drawCell(layout, ui, g, id);
        }
        fillBand(g, layout, 13, 0xBDD8CF, 14);
        drawCell(layout, ui, g, 44);
        drawCell(layout, ui, g, 45);
        drawCell(layout, ui, g, 41);
        fillBand(g, layout, 42, 0x51d069, 72);
    }

    private void drawTaskFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        drawCell(layout, ui, g, 1);
        fillBand(g, layout, 3, 0xBDE8D7, 10);
        fillBand(g, layout, 4, 0xBDE8D7, 13);
        fillBand(g, layout, 5, 0xBDE8D7, 93);
        drawCell(layout, ui, g, 35);
        fillBand(g, layout, 39, 0x51d069, 72);
        drawCell(layout, ui, g, 41);
        drawCell(layout, ui, g, 42);
    }

    private void drawRecordFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        drawCell(layout, ui, g, 1);
        fillBand(g, layout, 2, 0xBDE8D7, 7);
        fillBand(g, layout, 3, 0x51D069, 132);
        drawCell(layout, ui, g, 4);
        drawCell(layout, ui, g, 32);
        drawCell(layout, ui, g, 33);
        drawCell(layout, ui, g, 34);
    }

    private void drawRecordSelection(Graphics2D g, VqsvUiLayout layout, int selectedOption) {
        VqsvUiLayout.UiWidget left = layout.widget(4);
        VqsvUiLayout.UiWidget right = layout.widget(32);
        g.setColor(new Color(0xffa500));
        if (selectedOption == 0 && left != null) {
            g.drawRect(left.x - 1, left.y - 1, 65, 22);
        } else if (selectedOption == 1 && right != null) {
            g.drawRect(right.x - 1, right.y - 1, 65, 22);
        }
    }

    private void drawPetmapFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        drawCell(layout, ui, g, 1);
        fillBand(g, layout, 3, 0xBDE8D7, 15);
        fillBand(g, layout, 4, 0xBDE8D7, 8);
        fillBand(g, layout, 5, 0xBDE8D7, 93);
        fillBand(g, layout, 22, 0x51D069, 72);
        drawCell(layout, ui, g, 49);
        drawCell(layout, ui, g, 50);
    }

    private void drawRows(Graphics2D g, FontBitmap font, VqsvUiLayout layout, SpriteAnim ui,
                          int[] rowWidgets, String[] labels) {
        for (int i = 0; i < rowWidgets.length; i++) {
            int id = rowWidgets[i];
            VqsvUiLayout.UiWidget widget = layout.widget(id);
            if (widget == null) {
                continue;
            }
            int cell = i == selected ? widget.altId : widget.imageId;
            drawCellTopLeft(ui, g, cell, widget.x, widget.y);
            drawText(g, font, layout, id, labels[i],
                    i == selected ? colorSelected(widget) : color(widget, 0x1c6c92));
        }
    }

    private static void fillBand(Graphics2D g, VqsvUiLayout layout, int widgetId,
                                 int fallbackColor, int fallbackHeight) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int h = layout.bandHeight(widgetId, fallbackHeight);
        int color = widget.jColor == 0 || widget.jColor == -1
                ? fallbackColor : widget.jColor & 0xffffff;
        g.setColor(new Color(color));
        g.fillRect(widget.x, widget.y, Math.max(1, widget.w), Math.max(1, h));
    }

    private static void drawBagScrollbar(Graphics2D g, VqsvUiLayout layout, int rowCount, int scroll) {
        VqsvUiLayout.UiWidget track = layout.widget(42);
        VqsvUiLayout.UiWidget thumb = layout.widget(43);
        if (track == null || thumb == null) {
            return;
        }
        int trackHeight = 72;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x51d069 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        int thumbH = rowCount > 5 ? Math.max(8, trackHeight * 5 / rowCount) : trackHeight;
        int maxScroll = Math.max(1, rowCount - 5);
        int y = track.y + (trackHeight - thumbH) * Math.max(0, Math.min(scroll, maxScroll)) / maxScroll;
        g.setColor(new Color((thumb.jColor == 0 || thumb.jColor == -1
                ? 0xc6f0ff : thumb.jColor) & 0xffffff));
        g.fillRect(thumb.x, y, Math.max(1, thumb.w), Math.max(8, thumbH));
    }

    private static void drawTaskScrollbar(Graphics2D g, VqsvUiLayout layout, int rowCount, int scroll) {
        VqsvUiLayout.UiWidget track = layout.widget(39);
        VqsvUiLayout.UiWidget thumb = layout.widget(40);
        if (track == null || thumb == null) {
            return;
        }
        int trackHeight = 72;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x51d069 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        int thumbH = rowCount > 5 ? Math.max(8, trackHeight * 5 / rowCount) : trackHeight;
        int maxScroll = Math.max(1, rowCount - 5);
        int y = track.y + (trackHeight - thumbH) * Math.max(0, Math.min(scroll, maxScroll)) / maxScroll;
        g.setColor(new Color((thumb.jColor == 0 || thumb.jColor == -1
                ? 0xc6f0ff : thumb.jColor) & 0xffffff));
        g.fillRect(thumb.x, y, Math.max(1, thumb.w), Math.max(8, thumbH));
    }

    private static void drawPetmapScrollbar(Graphics2D g, VqsvUiLayout layout, int rowCount, int scroll) {
        VqsvUiLayout.UiWidget track = layout.widget(22);
        VqsvUiLayout.UiWidget thumb = layout.widget(23);
        if (track == null || thumb == null) {
            return;
        }
        int trackHeight = 72;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x51d069 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        int thumbH = rowCount > 5 ? Math.max(8, trackHeight * 5 / rowCount) : trackHeight;
        int maxScroll = Math.max(1, rowCount - 5);
        int y = track.y + (trackHeight - thumbH) * Math.max(0, Math.min(scroll, maxScroll)) / maxScroll;
        g.setColor(new Color((thumb.jColor == 0 || thumb.jColor == -1
                ? 0xc6f0ff : thumb.jColor) & 0xffffff));
        g.fillRect(thumb.x, y, Math.max(1, thumb.w), Math.max(8, thumbH));
    }

    private static void drawText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                 int widgetId, String text, int color) {
        drawTextWide(g, font, layout, widgetId, text, 0,
                layout.w(widgetId, Math.max(1, font.taggedWidth(text))), color);
    }

    private static void drawMultilineText(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                          int widgetId, String text, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y - 1, Math.max(1, widget.w) + 8,
                Math.max(12, layout.h(widgetId, 112)));
        String[] lines = text.split("#n", -1);
        int y = widget.y;
        for (String line : lines) {
            if (!line.isEmpty()) {
                font.drawTaggedLine(g, line, widget.x, y,
                        TextBox.visibleLength(TextBox.decodeMojibake(line)), color);
            }
            y += 14;
        }
        g.setClip(oldClip);
    }

    private static void drawTextWide(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                     int widgetId, String text, int dx, int width, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        int w = Math.max(1, width);
        Shape oldClip = g.getClip();
        g.clipRect(widget.x + dx, widget.y - 1, w + 6, Math.max(12, layout.h(widgetId, 12)));
        int x = widget.x + dx;
        if (widget.b == 4) {
            int textWidth = font.taggedWidth(text);
            x = widget.x + dx + Math.max(0, (w - textWidth) / 2);
        }
        font.drawTaggedLine(g, text, x, widget.y,
                TextBox.visibleLength(TextBox.decodeMojibake(text)), color);
        g.setClip(oldClip);
    }

    private static void drawCell(VqsvUiLayout layout, SpriteAnim ui, Graphics2D g, int widgetId) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int cell = widget.altId >= 0 ? widget.altId : widget.imageId;
        drawCellTopLeft(ui, g, cell, widget.x, widget.y);
    }

    private static void drawCellState(VqsvUiLayout layout, SpriteAnim ui, Graphics2D g,
                                      int widgetId, boolean selectedState) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int cell = selectedState && widget.altId >= 0 ? widget.altId : widget.imageId;
        drawCellTopLeft(ui, g, cell, widget.x, widget.y);
    }

    private static void drawCellTopLeft(SpriteAnim ui, Graphics2D g, int cellId, int x, int y) {
        int[] bounds = ui.cellBounds(cellId);
        if (bounds == null) {
            return;
        }
        ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }

    private static void drawSpriteCellTopLeft(Graphics2D g, int spriteId, int cellId, int x, int y) {
        SpriteAnim sprite = SpriteAnim.load(spriteId);
        int[] bounds = sprite.cellBounds(cellId);
        if (bounds == null) {
            return;
        }
        sprite.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }

    private static int color(VqsvUiLayout.UiWidget widget, int fallback) {
        if (widget == null || widget.lColor == 0 || widget.lColor == -1) {
            return fallback;
        }
        return widget.lColor & 0xffffff;
    }

    private static int colorSelected(VqsvUiLayout.UiWidget widget) {
        if (widget == null || widget.jColor == 0 || widget.jColor == -1) {
            return 0xffa500;
        }
        return widget.jColor & 0xffffff;
    }

    private static int sourceTargetState(int selected) {
        switch (selected) {
            case 0:
                return 14;
            case 1:
                return 7;
            case 2:
                return 8;
            case 3:
                return 9;
            case 4:
                return 10;
            case 5:
                return 22;
            default:
                return -1;
        }
    }

    private static int sourceSystemTargetState(int selected) {
        switch (selected) {
            case 0:
                return 0;
            case 1:
                return 20;
            case 2:
                return 21;
            case 3:
                return 7;
            default:
                return -1;
        }
    }

    private static String bagTabTitle(int tab) {
        switch (tab) {
            case 0:
                return "Vat pham";
            case 1:
                return "Trang suc";
            case 2:
                return "Tai lieu";
            case 3:
                return "Dac thu";
            default:
                return "Unknown";
        }
    }

    private String bagActionLabel(VqsvIntroDemo.Scene s) {
        if (bagTab != 3) {
            return "Su dung";
        }
        List<BagRow> rows = bagRows(s, bagTab);
        if (rows.isEmpty()) {
            return "";
        }
        BagRow row = rows.get(clamp(selected, 0, rows.size() - 1));
        if (row.specialEgg) {
            return s.sourceEggActive ? "Ap trung" : "Mo ra";
        }
        if (row.specialId == 5 || row.specialId == 6 || row.specialId == 10) {
            return "Mo ra";
        }
        if (row.specialId == 7 || row.specialId == 8 || row.specialId == 9) {
            return "Su dung";
        }
        return "";
    }

    private String sourceTickMethod() {
        if (mode == Mode.GAMESYSTEM) {
            return "game.h.n";
        }
        if (mode == Mode.BAG) {
            return "game.h.ac";
        }
        if (mode == Mode.TASK) {
            return "game.h.S";
        }
        if (mode == Mode.RECORD) {
            return "game.h.O";
        }
        if (mode == Mode.PETMAP) {
            return "game.h.Q";
        }
        if (mode == Mode.SAVE) {
            return "game.h.K";
        }
        if (mode == Mode.HELP) {
            return "game.h.v";
        }
        if (mode == Mode.SETTINGS) {
            return "game.h.x";
        }
        if (mode == Mode.OPTION_CONFIRM) {
            return "game.h.n";
        }
        if (mode == Mode.RIDE) {
            return "game.h.ae";
        }
        return "game.h.l";
    }

    private String titleTraceSuffix() {
        return mode == Mode.GAMEMENU ? " titleToken=" + MENU_TITLE_TOKENS[selected] : "";
    }

    private String uiName() {
        if (mode == Mode.GAMESYSTEM) {
            return "gamesystem.ui";
        }
        if (mode == Mode.SAVE) {
            return "msgtip.ui";
        }
        if (mode == Mode.HELP) {
            return "help1.ui";
        }
        if (mode == Mode.SETTINGS) {
            return "help.ui";
        }
        if (mode == Mode.OPTION_CONFIRM) {
            return "option.ui";
        }
        if (mode == Mode.RIDE) {
            return "ride.ui";
        }
        if (mode == Mode.BAG) {
            return "bag.ui";
        }
        if (mode == Mode.TASK) {
            return "task.ui";
        }
        if (mode == Mode.RECORD) {
            return "record.ui";
        }
        if (mode == Mode.PETMAP) {
            return "petmap.ui";
        }
        return "gamemenu.ui";
    }

    private int[] rowWidgets() {
        if (mode == Mode.GAMESYSTEM) {
            return SYSTEM_ROW_WIDGETS;
        }
        if (mode == Mode.BAG) {
            return BAG_ROW_BACKGROUNDS;
        }
        if (mode == Mode.TASK) {
            return TASK_ROW_BACKGROUNDS;
        }
        if (mode == Mode.PETMAP) {
            return PETMAP_ROW_BACKGROUNDS;
        }
        if (mode == Mode.RIDE) {
            return new int[]{4, 5, 6, 7};
        }
        return MENU_ROW_WIDGETS;
    }

    private String[] labels() {
        if (mode == Mode.GAMESYSTEM) {
            return SYSTEM_LABELS;
        }
        if (mode == Mode.BAG) {
            return new String[]{"L\u01b0ng bao"};
        }
        if (mode == Mode.TASK) {
            return new String[]{"Nhi\u1ec7m v\u1ee5"};
        }
        if (mode == Mode.RECORD) {
            return new String[]{"Minh h\u1ecda", "K\u1ef7 l\u1ee5c"};
        }
        if (mode == Mode.PETMAP) {
            return new String[]{"Minh h\u1ecda"};
        }
        if (mode == Mode.HELP) {
            return new String[]{"Tr\u1ee3 gi\u00fap"};
        }
        if (mode == Mode.SETTINGS) {
            return new String[]{"T\u00f9y ch\u1ecdn"};
        }
        if (mode == Mode.OPTION_CONFIRM) {
            return new String[]{"", "Kh\u00f4ng"};
        }
        if (mode == Mode.RIDE) {
            return RIDE_LABELS;
        }
        return MENU_LABELS;
    }

    private int wheelRowCount(VqsvIntroDemo.Scene s) {
        if (mode == Mode.BAG) {
            return bagRows(s, bagTab).size();
        }
        if (mode == Mode.TASK) {
            return taskRowsForRender(s, taskTab).size();
        }
        if (mode == Mode.PETMAP) {
            return petmapRowsForRender(s, petmapTab).size();
        }
        return labels().length;
    }

    private String closeTrace() {
        if (mode == Mode.GAMESYSTEM) {
            return "game.h.n close gamesystem.ui -> P=0";
        }
        if (mode == Mode.BAG) {
            return "game.h.ac close bag.ui -> P=0";
        }
        if (mode == Mode.TASK) {
            return "game.h.S close task.ui -> P=0";
        }
        if (mode == Mode.RECORD) {
            return "game.h.O close record.ui -> P=0";
        }
        if (mode == Mode.PETMAP) {
            return "game.h.Q close petmap.ui -> P=0";
        }
        if (mode == Mode.SAVE) {
            return "game.h.K close msgtip.ui -> P=0";
        }
        if (mode == Mode.HELP) {
            return "game.h.v close help1.ui -> P=13";
        }
        if (mode == Mode.SETTINGS) {
            return "game.h.x close help.ui -> P=13";
        }
        if (mode == Mode.OPTION_CONFIRM) {
            return "game.h.n close option.ui f=0";
        }
        if (mode == Mode.RIDE) {
            return "game.h.ae close ride.ui -> P=0";
        }
        return "game.h.l back close gamemenu.ui -> P=0";
    }

    private static List<BagRow> bagRows(VqsvIntroDemo.Scene s, int bagTab) {
        List<BagRow> rows = new ArrayList<>();
        if (bagTab == 3) {
            rows.add(sourceEggSpecialRow(s));
            for (SourceSpecialReward reward : s.sourceSpecialRewards.values()) {
                if (reward.id == 0 || !sourceSpecialVisible(reward)) {
                    continue;
                }
                rows.add(sourceSpecialRewardRow(reward));
            }
            rows.sort(Comparator.comparingInt(row -> row.specialId));
            return rows;
        }
        if (bagTab != 0) {
            return rows;
        }
        for (BagItem item : s.sourceBagItems.values()) {
            if (item.count <= 0) {
                continue;
            }
            rows.add(new BagRow(VqsvSourceOps.sourceItem(item.id), item.count));
        }
        rows.sort(Comparator.comparingInt(row -> row.item.id));
        return rows;
    }

    private static BagRow sourceEggSpecialRow(VqsvIntroDemo.Scene s) {
        short[] row = VqsvBattleTables.instance().row(5, 0);
        int textId = value(row, 0, -1);
        int iconCell = value(row, 1, 0);
        int descId = s.sourceEggActive ? value(row, 2, -1) : 634;
        VqsvBattleTables tables = VqsvBattleTables.instance();
        String name = tables.text(textId, "\u1ea4p tr\u1ee9ng");
        String description = tables.text(descId, "");
        String status;
        if (!s.sourceEggActive) {
            status = "0 c\u00e1i";
        } else if (sourceEggReady(s)) {
            status = "Ho\u00e0n th\u00e0nh";
        } else {
            status = "1 c\u00e1i";
        }
        SourceItem item = new SourceItem(0, textId, iconCell, descId, name, description, 3);
        return new BagRow(item, s.sourceEggActive ? 1 : 0, true, 0, status);
    }

    private static boolean sourceSpecialVisible(SourceSpecialReward reward) {
        return reward.unlocked || reward.stackCount > 0;
    }

    private static boolean sourceRideUnlocked(VqsvIntroDemo.Scene s, int rideIndex) {
        SourceSpecialReward reward = s.sourceSpecialRewards.get(rideIndex + 1);
        return reward != null && reward.unlocked;
    }

    private static boolean sourceRideUsable(VqsvIntroDemo.Scene s, int rideIndex) {
        return sourceRideUnlocked(s, rideIndex)
                && rideIndex >= 0
                && rideIndex < s.sourceRideBlocked.length
                && s.sourceRideBlocked[rideIndex] != 1;
    }

    private static int rideSpeed(int rideIndex) {
        return rideIndex == 0 || rideIndex == 1 ? 8 : 4;
    }

    private static BagRow sourceSpecialRewardRow(SourceSpecialReward reward) {
        short[] row = VqsvBattleTables.instance().row(5, reward.id);
        int textId = value(row, 0, reward.textId);
        int iconCell = value(row, 1, reward.iconId);
        int descId = value(row, 2, reward.descriptionTextId);
        VqsvBattleTables tables = VqsvBattleTables.instance();
        String name = tables.text(textId, reward.name);
        String description = tables.text(descId, "");
        int count = reward.id == 7 || reward.id == 8 || reward.id == 9
                ? Math.max(0, reward.stackCount)
                : reward.unlocked ? 1 : 0;
        SourceItem item = new SourceItem(reward.id, textId, iconCell, descId, name, description, 3);
        return new BagRow(item, count, false, reward.id, specialStatus(reward));
    }

    private static String specialStatus(SourceSpecialReward reward) {
        if (reward.id == 7 || reward.id == 8 || reward.id == 9) {
            return String.valueOf(Math.max(0, reward.stackCount));
        }
        return "";
    }

    private static int value(short[] row, int index, int fallback) {
        return row == null || index < 0 || index >= row.length ? fallback : row[index];
    }

    private static final class BagRow {
        final SourceItem item;
        final int count;
        final boolean specialEgg;
        final int specialId;
        final String status;

        BagRow(SourceItem item, int count) {
            this(item, count, false, item.id, null);
        }

        BagRow(SourceItem item, int count, boolean specialEgg, int specialId, String status) {
            this.item = item;
            this.count = count;
            this.specialEgg = specialEgg;
            this.specialId = specialId;
            this.status = status;
        }

        String statusText() {
            return status == null ? String.valueOf(count) : status;
        }
    }

    private static List<TaskRow> taskRowsForRender(VqsvIntroDemo.Scene s, int tab) {
        List<String> source = tab == 0 ? loadMainTasks() : loadBranchTasks();
        List<TaskRow> rows = new ArrayList<>();
        if (source.isEmpty()) {
            return rows;
        }
        int half = Math.max(1, source.size() / 2);
        int visibleCount = tab == 0 ? Math.min(half, Math.max(1, mainTaskCursor(s) + 1))
                : Math.min(half, Math.max(1, branchTaskCount(s)));
        for (int i = 0; i < visibleCount; i++) {
            String title = source.get(i);
            String detail = i + half < source.size() ? source.get(i + half) : title;
            boolean completed = tab == 0 ? i < mainTaskCursor(s) : branchTaskCompleted(s, i);
            rows.add(new TaskRow(i + 1, title, detail, completed));
        }
        return rows;
    }

    private static int mainTaskCursor(VqsvIntroDemo.Scene s) {
        if (s == null) {
            return 0;
        }
        if (s.sourceEventStateComplete(1, 0, 6)) {
            return 2;
        }
        if (s.sourceEventStateComplete(1, 1, 0)
                || s.sourceEventStateComplete(1, 0, 3)
                || s.sourceEventStateComplete(1, 0, 2)) {
            return 1;
        }
        return 0;
    }

    private static int branchTaskCount(VqsvIntroDemo.Scene s) {
        if (s == null) {
            return 1;
        }
        return s.sourceEventStateComplete(1, 1, 0) ? 1 : 1;
    }

    private static boolean branchTaskCompleted(VqsvIntroDemo.Scene s, int index) {
        return s != null && index == 0 && s.sourceEventStateComplete(1, 1, 0);
    }

    private static String taskProgressText(VqsvIntroDemo.Scene s, int tab) {
        if (tab == 0) {
            List<String> tasks = loadMainTasks();
            int half = Math.max(1, tasks.size() / 2);
            int value = mainTaskCursor(s) * 1000 / half;
            return value / 10 + "." + value % 10 + "%";
        }
        List<String> tasks = loadBranchTasks();
        int half = Math.max(1, tasks.size() / 2);
        int done = branchTaskCompleted(s, 0) ? 1 : 0;
        int value = done * 1000 / half;
        return value / 10 + "." + value % 10 + "%";
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

    private static String helpEffectText(int entry) {
        int token = entry <= 10 ? 311 + entry : 333 + entry - 11;
        List<String> rows = loadChsRows();
        return token >= 0 && token < rows.size() ? rows.get(token) : "an.f(" + token + ")";
    }

    private static List<String> loadChsRows() {
        if (chsRows == null) {
            chsRows = loadTaskScript("data__script__chs.mid.json");
        }
        return chsRows;
    }

    private static List<String> loadTaskScript(String name) {
        List<String> result = new ArrayList<>();
        try {
            Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig())
                    .modulesRoot().resolve("script").resolve("decoded").resolve(name);
            String text = Files.readString(path, StandardCharsets.UTF_8);
            Matcher matcher = TASK_TEXT_PATTERN.matcher(text);
            while (matcher.find()) {
                String row = matcher.group(1);
                if ("format".equals(row) || "string_matrix".equals(row) || "rows".equals(row)) {
                    continue;
                }
                result.add(TextBox.decodeMojibake(row));
            }
        } catch (IOException | RuntimeException ex) {
            result.clear();
        }
        return result;
    }

    private static final class TaskRow {
        final int number;
        final String title;
        final String detail;
        final boolean completed;

        TaskRow(int number, String title, String detail, boolean completed) {
            this.number = number;
            this.title = title;
            this.detail = detail;
            this.completed = completed;
        }
    }

    private static List<PetmapRow> petmapRows(int tab) {
        List<PetmapRow> rows = new ArrayList<>();
        VqsvBattleTables tables = VqsvBattleTables.instance();
        for (int speciesId = 0; speciesId < tables.rowCount(0); speciesId++) {
            BattleSpeciesRow species = tables.species(speciesId);
            if (species == null || !species.validForBattle() || species.element != tab) {
                continue;
            }
            rows.add(new PetmapRow(speciesId, species.name("Pet " + speciesId),
                    species.spriteId, false));
        }
        return rows;
    }

    private static List<PetmapRow> petmapRowsForRender(VqsvIntroDemo.Scene s, int tab) {
        List<PetmapRow> rows = petmapRows(tab);
        for (int i = 0; i < rows.size(); i++) {
            PetmapRow row = rows.get(i);
            rows.set(i, new PetmapRow(row.speciesId, row.name, row.spriteId,
                    s != null && ownsSpecies(s, row.speciesId)));
        }
        return rows;
    }

    private static boolean ownsSpecies(VqsvIntroDemo.Scene s, int speciesId) {
        for (SourcePetState pet : s.sourcePets) {
            if (pet.speciesId == speciesId) {
                return true;
            }
        }
        for (SourcePetState pet : s.sourcePetBank) {
            if (pet.speciesId == speciesId) {
                return true;
            }
        }
        return false;
    }

    private static int ownedCount(List<PetmapRow> rows) {
        int count = 0;
        for (PetmapRow row : rows) {
            if (row.owned) {
                count++;
            }
        }
        return count;
    }

    private static int distinctOwnedSpecies(VqsvIntroDemo.Scene s) {
        List<Integer> ids = new ArrayList<>();
        for (SourcePetState pet : s.sourcePets) {
            if (!ids.contains(pet.speciesId)) {
                ids.add(pet.speciesId);
            }
        }
        for (SourcePetState pet : s.sourcePetBank) {
            if (!ids.contains(pet.speciesId)) {
                ids.add(pet.speciesId);
            }
        }
        return ids.size();
    }

    private static int rareOwnedSpecies(VqsvIntroDemo.Scene s) {
        int count = 0;
        for (SourcePetState pet : s.sourcePets) {
            BattleSpeciesRow row = VqsvBattleTables.instance().species(pet.speciesId);
            if (row != null && row.quality >= 3) {
                count++;
            }
        }
        for (SourcePetState pet : s.sourcePetBank) {
            BattleSpeciesRow row = VqsvBattleTables.instance().species(pet.speciesId);
            if (row != null && row.quality >= 3) {
                count++;
            }
        }
        return count;
    }

    private static final class PetmapRow {
        final int speciesId;
        final String name;
        final int spriteId;
        final boolean owned;

        PetmapRow(int speciesId, String name, int spriteId, boolean owned) {
            this.speciesId = speciesId;
            this.name = name;
            this.spriteId = spriteId;
            this.owned = owned;
        }
    }

    private static void consumeKeys(VqsvIntroDemo.Scene s) {
        s.key0 = false;
        s.keyBack = false;
        s.keyUp = false;
        s.keyDown = false;
        s.keyLeft = false;
        s.keyRight = false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
