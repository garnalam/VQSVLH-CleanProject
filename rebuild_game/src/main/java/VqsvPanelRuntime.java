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
        PORTABLE_SHOP_SERVICE_CONFIRM
    }

    private static final int[] MENU_ROW_WIDGETS = {15, 5, 6, 7, 8, 9};
    private static final int[] SYSTEM_ROW_WIDGETS = {6, 7, 8, 9};
    private static final int[] PORTABLE_SHOP_ROW_WIDGETS = {16, 8, 9, 10};
    private static final int[] SHOPBUY_ROW_BACKGROUNDS = {12, 17, 22, 27, 32};
    private static final int[] SHOPBUY_ROW_ICONS = {51, 52, 53, 54, 55};
    private static final int[] SHOPBUY_ROW_NAMES = {14, 19, 24, 29, 34};
    private static final int[] SHOPBUY_ROW_PRICES = {15, 20, 25, 30, 35};
    private static final int[] SHOPBUY_ROW_CURRENCIES = {45, 46, 47, 48, 49};
    private static final int[] BAG_ROW_BACKGROUNDS = {17, 22, 27, 32, 37};
    private static final int[] BAG_ROW_ICONS = {18, 23, 28, 33, 38};
    private static final int[] BAG_ROW_NAMES = {19, 24, 29, 34, 39};
    private static final int[] BAG_ROW_COUNTS = {20, 25, 30, 35, 40};
    private static final int[] BAG_EQUIP_ROW_BACKGROUNDS = {58, 63, 68, 73, 78};
    private static final int[] BAG_EQUIP_ROW_ICONS = {59, 64, 69, 74, 79};
    private static final int[] BAG_EQUIP_ROW_NAMES = {60, 65, 70, 75, 80};
    private static final int[] BAG_EQUIP_ROW_STATUS = {61, 66, 71, 76, 81};
    private static final int[] BAG_MATERIAL_ROW_BACKGROUNDS = {97, 102, 107, 112, 117};
    private static final int[] BAG_MATERIAL_ROW_ICONS = {98, 103, 108, 113, 118};
    private static final int[] BAG_MATERIAL_ROW_NAMES = {99, 104, 109, 114, 119};
    private static final int[] BAG_MATERIAL_ROW_COUNTS = {100, 105, 110, 115, 120};
    private static final int[] BAG_SPECIAL_ROW_ICONS = {137, 142, 147, 152, 157};
    private static final int[] BAG_SPECIAL_ROW_NAMES = {138, 143, 148, 153, 158};
    private static final int[] BAG_SPECIAL_ROW_COUNTS = {139, 144, 149, 154, 159};
    private static final int[] TRANSMIT_ROW_WIDGETS = {5, 6, 7, 8, 9};
    private static final String[] TRANSMIT_DESTINATIONS = {
            "Th\u1ee7y Kimura",
            "B\u00edch Th\u1ee7y th\u00e0nh",
            "Nguy\u00ean M\u1ed9c Th\u00e0nh",
            "Ni\u00eam Th\u1ed5 Th\u00e0nh",
            "H\u1eafc Th\u1ea1ch th\u00e0nh",
            "Thi\u00ean kh\u00f4ng",
            "Xa c\u1ed5"
    };
    private static final int[][] TRANSMIT_TARGETS = {
            {1, 0, 196, 208, 0},
            {2, 1, 196, 208, 0},
            {3, 3, 196, 208, 0},
            {4, 5, 320, 352, 0},
            {5, 3, 320, 196, 0},
            {7, 2, 288, 112, 0},
            {8, 0, 160, 144, 0}
    };
    private static final int[] EGG_HATCH_RANDOM_THRESHOLDS = {76, 52, 28, 4, 0};
    private static final int[] EGG_HATCH_RANDOM_SPECIES = {0, 56, 58, 95, 72};
    private static final VqsvSourceRandom PANEL_RANDOM = VqsvSourceRandom.lazySourceSeeded();
    private static final int[] TASK_ROW_BACKGROUNDS = {11, 16, 21, 26, 31};
    private static final int[] TASK_ROW_NUMBERS = {12, 17, 22, 27, 32};
    private static final int[] TASK_ROW_NAMES = {13, 18, 23, 28, 33};
    private static final int[] TASK_ROW_STATUS = {14, 19, 24, 29, 34};
    private static final int TASK_TAB_SELECTED_COLOR = 11290624;
    private static final int[] PETMAP_TAB_CELLS = {6, 7, 8, 9, 10, 11, 12};
    private static final int[] PETMAP_TAB_LABELS = {13, 14, 15, 16, 17, 18, 19};
    private static final int[] PETMAP_ROW_BACKGROUNDS = {25, 29, 33, 37, 41};
    private static final int[] PETMAP_ROW_MARKERS = {44, 45, 46, 47, 48};
    private static final int[] PETMAP_ROW_NAMES = {27, 31, 35, 39, 43};
    private static final int[] BADGE_SLOT_WIDGETS = {17, 18, 19, 20, 21, 22, 23, 24};
    private static final int[] BADGE_ICON_WIDGETS = {25, 26, 27, 28, 29, 30, 31, 32};
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
    private static final String[] PORTABLE_SHOP_LABELS = {
            "Th\u01b0\u01a1ng \u0111i\u1ebfm b\u00ecnh d\u00e2n",
            "Th\u0103ng c\u1ea5p s\u1ee7ng v\u1eadt",
            "Mua s\u1eafm huy hi\u1ec7u",
            "Mua s\u1eafm kim ti\u1ec1n"
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
    private int taskSelectedBeforeOption;
    private boolean taskOptionReturnToTask;
    private int taskOptionBranchTaskId = -1;
    private TaskOptionData taskOptionData = TaskOptionData.smokeDefault();
    private String taskSelectedLabelCache = "Nhi\u1ec7m v\u1ee5";
    private int recordSelected;
    private int petmapTab;
    private int savePhase;
    private int helpPage;
    private int settingsLevel;
    private int bagTab;
    private boolean badgeReturnToBag;
    private int badgeReturnBagSelected;
    private int badgeReturnBagScroll;
    private int transmitReturnBagSelected;
    private int transmitReturnBagScroll;
    private int rideSelected;
    private int listScroll;
    private int bagMessageMode;
    private int rideMessageMode;
    private int shopConfirmItemId = -1;
    private int shopConfirmQuantity = 1;
    private int shopConfirmTotal;
    private int shopConfirmCurrency;
    private int serviceProductId = -1;
    private String serviceConfirmTitle = "";
    private String serviceConfirmPrompt = "";
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

    void openTaskOptionForSmoke(VqsvIntroDemo.Scene s, boolean returnToTask) {
        visible = true;
        taskSelectedBeforeOption = selected;
        taskOptionReturnToTask = returnToTask;
        taskOptionBranchTaskId = -1;
        mode = Mode.TASK_OPTION;
        selected = 0;
        openedTicks = 0;
        taskOptionData = TaskOptionData.smokeDefault();
        s.sourceStateTrace.add("PORTED/PARTIAL panel event opcode49"
                + " game.k.a taskOption.ui open"
                + " rewards=" + taskOptionData.rewards.length
                + " options=" + taskOptionData.options.length
                + " returnToTask=" + returnToTask);
    }

    void openBranchTaskAcceptOption(VqsvIntroDemo.Scene s, int taskId, boolean returnToTask) {
        visible = true;
        taskSelectedBeforeOption = selected;
        taskOptionReturnToTask = returnToTask;
        taskOptionBranchTaskId = taskId;
        mode = Mode.TASK_OPTION;
        selected = 0;
        openedTicks = 0;
        taskOptionData = TaskOptionData.branchTask(taskId);
        s.sourceStateTrace.add("PORTED/PARTIAL source game.e opcode49 taskOption.ui open"
                + " taskId=" + taskId
                + " rewards=" + taskOptionData.rewards.length
                + " options=" + taskOptionData.options.length
                + " returnToTask=" + returnToTask);
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
        if (mode == Mode.TASK_OPTION) {
            tickTaskOption(s);
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
        if (mode == Mode.BADGE) {
            tickBadge(s);
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
        if (mode == Mode.TRANSMIT) {
            tickTransmit(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP) {
            tickPortableShop(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            tickPortableShopBuy(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            tickPortableShopConfirm(s);
            consumeKeys(s);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            tickPortableShopServiceConfirm(s);
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
        if (isScrollablePanelList() && maxScroll > 0) {
            int before = listScroll;
            listScroll = clamp(listScroll + steps, 0, maxScroll);
            if (listScroll != before) {
                s.sourceStateTrace.add("PC_QOL mouse wheel panel list scrollbar"
                        + " mode=" + mode
                        + " scroll=" + listScroll
                        + " selected=" + selected
                        + " rows=" + rowCount);
            }
            return;
        }
        moveSelectionByMouseWheel(s, steps);
    }

    private boolean isScrollablePanelList() {
        return mode == Mode.BAG || mode == Mode.TASK || mode == Mode.PETMAP
                || mode == Mode.TRANSMIT
                || mode == Mode.PORTABLE_SHOP_BUY;
    }

    private void moveSelectionByMouseWheel(VqsvIntroDemo.Scene s, int steps) {
        int direction = steps < 0 ? -1 : 1;
        if (mode == Mode.RIDE) {
            int before = rideSelected;
            rideSelected = clamp(rideSelected + direction, 0, RIDE_LABELS.length - 1);
            if (rideSelected != before) {
                s.sourceStateTrace.add("PC_QOL mouse wheel panel selection"
                        + " mode=" + mode
                        + " selected=" + rideSelected);
            }
            return;
        }
        if (!isMouseWheelSelectableMode()) {
            return;
        }
        int rowCount = wheelRowCount(s);
        if (rowCount <= 0) {
            return;
        }
        int before = selected;
        selected = clamp(selected + direction, 0, rowCount - 1);
        if (isScrollablePanelList()) {
            keepSelectedVisible(rowCount);
        }
        if (mode == Mode.TASK) {
            updateTaskSelectedLabel(s);
        }
        if (selected != before) {
            openedTicks = 0;
            s.sourceStateTrace.add("PC_QOL mouse wheel panel selection"
                    + " mode=" + mode
                    + " selected=" + selected
                    + " rows=" + rowCount);
        }
    }

    private boolean isMouseWheelSelectableMode() {
        return mode == Mode.GAMEMENU
                || mode == Mode.GAMESYSTEM
                || mode == Mode.BAG
                || mode == Mode.TASK
                || mode == Mode.TASK_OPTION
                || mode == Mode.PETMAP
                || mode == Mode.TRANSMIT
                || mode == Mode.PORTABLE_SHOP
                || mode == Mode.PORTABLE_SHOP_BUY;
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
        if (mode == Mode.TASK_OPTION) {
            renderTaskOption(g, font);
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
        if (mode == Mode.BADGE) {
            renderBadge(g, font, s);
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
        if (mode == Mode.TRANSMIT) {
            renderTransmit(g, font);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP) {
            renderPortableShop(g, font);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            renderPortableShopBuy(g, font, s);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            renderPortableShopBuy(g, font, s);
            renderPortableShopConfirm(g, font);
            return;
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            renderPortableShop(g, font);
            VqsvBattleRenderer.drawSmsInfoOverlay(g, font, serviceConfirmTitle, serviceConfirmPrompt);
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
        drawSoftkey(g, font, layout, ui, 12, layout.text(12, "Xac dinh"), 0xd0010e);
        drawSoftkey(g, font, layout, ui, 11, layout.text(11, "Quay lai"), 0xd0010e);
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
            int[] rowBackgrounds = bagRowBackgrounds(bagTab);
            for (int i = 0; i < rowBackgrounds.length; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(rowBackgrounds[i]);
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
            VqsvUiLayout.UiWidget mainTab = layout.widget(6);
            if (mainTab != null && x >= mainTab.x - 4 && x <= mainTab.x + 68
                    && y >= mainTab.y - 4 && y <= mainTab.y + 22) {
                s.keyLeft = true;
                return true;
            }
            VqsvUiLayout.UiWidget branchTab = layout.widget(7);
            if (branchTab != null && x >= branchTab.x - 4 && x <= branchTab.x + 68
                    && y >= branchTab.y - 4 && y <= branchTab.y + 22) {
                s.keyRight = true;
                return true;
            }
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
        if (mode == Mode.TASK_OPTION) {
            VqsvUiLayout layout = VqsvUiLayout.load("taskOption.ui");
            int[] rowWidgets = {10, 11};
            for (int i = 0; i < rowWidgets.length && i < taskOptionData.options.length; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(rowWidgets[i]);
                if (row != null && x >= row.x - 4 && x <= row.x + 84
                        && y >= row.y - 2 && y <= row.y + 24) {
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
        if (mode == Mode.TRANSMIT) {
            VqsvUiLayout layout = VqsvUiLayout.load("transmit.ui");
            int start = visibleListStart(TRANSMIT_DESTINATIONS.length);
            for (int i = 0; i < TRANSMIT_ROW_WIDGETS.length; i++) {
                VqsvUiLayout.UiWidget row = layout.widget(TRANSMIT_ROW_WIDGETS[i]);
                if (row != null && x >= row.x - 4 && x <= row.x + Math.max(59, row.w) + 12
                        && y >= row.y - 2 && y <= row.y + 18) {
                    selected = clamp(start + i, 0, TRANSMIT_DESTINATIONS.length - 1);
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
            int row = widgetRowAt("bag.ui", bagRowBackgrounds(bagTab), x, y, 136);
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
        if (mode == Mode.TASK_OPTION) {
            int row = widgetRowAt("taskOption.ui", new int[]{10, 11}, x, y, 84);
            if (row >= 0) {
                selected = clamp(row, 0, Math.max(0, taskOptionData.options.length - 1));
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
        if (mode == Mode.TRANSMIT) {
            int row = widgetRowAt("transmit.ui", TRANSMIT_ROW_WIDGETS, x, y, 59);
            if (row >= 0) {
                selected = clamp(visibleListStart(TRANSMIT_DESTINATIONS.length) + row,
                        0, TRANSMIT_DESTINATIONS.length - 1);
            }
            return true;
        }
        if (mode == Mode.PORTABLE_SHOP) {
            int row = widgetRowAt("bodyShop.ui", PORTABLE_SHOP_ROW_WIDGETS, x, y, 108);
            if (row >= 0) {
                selected = clamp(row, 0, PORTABLE_SHOP_LABELS.length - 1);
            }
            return true;
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            int row = widgetRowAt("shopbuy.ui", SHOPBUY_ROW_BACKGROUNDS, x, y, 136);
            if (row >= 0) {
                int size = portableShopItemCount();
                selected = clamp(visibleListStart(size) + row, 0, Math.max(0, size - 1));
            }
            return true;
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            if (x >= 122 && x <= 158 && y >= 162 && y <= 183) {
                s.key0 = true;
                return true;
            }
            if (x >= 96 && x <= 158 && y >= 184 && y <= 207) {
                s.keyBack = true;
                return true;
            }
            return true;
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            if (x <= 90 && y >= 232) {
                s.key0 = true;
                return true;
            }
            if (x >= 145 && y >= 232) {
                s.keyBack = true;
                return true;
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
            return taskSelectedLabelCache;
        }
        if (mode == Mode.TASK_OPTION) {
            return taskOptionData.option(clamp(selected, 0, taskOptionData.options.length - 1));
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
        if (mode == Mode.TRANSMIT) {
            return TRANSMIT_DESTINATIONS[clamp(selected, 0, TRANSMIT_DESTINATIONS.length - 1)];
        }
        if (mode == Mode.PORTABLE_SHOP) {
            return PORTABLE_SHOP_LABELS[selected];
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            BattleItemRow row = VqsvBattleTables.instance().item(selected);
            return row == null ? "Mua" : row.name("Item " + selected);
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            BattleItemRow row = VqsvBattleTables.instance().item(shopConfirmItemId);
            return row == null ? "X\u00e1c nh\u1eadn" : row.name("Item " + shopConfirmItemId)
                    + " * " + shopConfirmQuantity;
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            return portableShopServiceTitle(serviceProductId);
        }
        return labels()[selected];
    }

    String modeName() {
        return mode.name();
    }

    int bagTabForSmoke() {
        return bagTab;
    }

    private void confirm(VqsvIntroDemo.Scene s) {
        if (mode == Mode.GAMEMENU) {
            if (selected == 0) {
                mode = Mode.PORTABLE_SHOP;
                selected = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.l confirm selected=0"
                        + " close gamemenu.ui -> P=14 game.k.aC bodyShop.ui open");
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
                taskTab = 0;
                listScroll = 0;
                selected = mainTaskCursor(s);
                keepSelectedVisible(taskRowsForRender(s, taskTab).size());
                openedTicks = 0;
                updateTaskSelectedLabel(s);
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
                                + " q.O case0 f=2 close msgwarm.ui -> openbox.ui"
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
                if (bagTab == 1) {
                    s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=1 q.M confirm"
                            + " equipmentId=" + row.item.id
                            + " name=" + row.item.name
                            + " status=" + row.statusText()
                            + " render/navigate/back only; equip action stays in petsetting.ui");
                    return;
                }
                if (bagTab == 2) {
                    s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=2 q.N material confirm"
                            + " itemId=" + row.item.id
                            + " name=" + row.item.name
                            + " count=" + row.count
                            + " render/navigate/back only; q.N action branches pending");
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

    void returnToBagFromSpecialUseBack(VqsvIntroDemo.Scene s, int specialId) {
        visible = true;
        mode = Mode.BAG;
        bagTab = 3;
        openedTicks = 0;
        selected = clamp(selected, 0, Math.max(0, bagRows(s, bagTab).size() - 1));
        keepSelectedVisible(bagRows(s, bagTab).size());
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ab back/close state19"
                + " specialId=" + specialId
                + " o.a(8) close petstate.ui -> bag.ui"
                + " b=3 selected=" + selected);
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
        SourceSpecialReward avoidSideEffect = VqsvSourceOps.sourceStackSpecialReward(s, 1, 1);
        List<BagRow> rowsAfter = bagRows(s, bagTab);
        selected = clamp(selected, 0, Math.max(0, rowsAfter.size() - 1));
        s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_AVOID_SUCCESS,
                VqsvText.Evolution.CONTINUE_PROMPT_5);
        bagMessageMode = 15;
        s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=0 itemId=13"
                + " q.d(item,1,0) count=" + VqsvSourceOps.sourceItemCount(s, 13)
                + " q.x=aq.c[4][13][6]=" + duration
                + " q.w=0 q.c(1) stack=" + avoidSideEffect.stackCount
                + " -> msgwarm.ui f=1 selected=" + selected);
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
        s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=0 itemId=14"
                + " q.k(0)=true q.I=" + s.sourceEggType
                + " game.k.q=" + s.sourceEggProgress
                + " q.d(item,1,0) count=" + VqsvSourceOps.sourceItemCount(s, 14)
                + " -> msgwarm.ui f=1 selected=" + selected);
    }

    private void useEggHatchAction(VqsvIntroDemo.Scene s, BagRow row) {
        if (!row.specialEgg) {
            s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 confirm specialId="
                    + row.item.id + " only q.O case0 hatch ported");
            return;
        }
        if (!s.sourceEggActive) {
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case0"
                    + " q.k(0)=false -> source breaks without UI");
            return;
        }
        if (!sourceEggReady(s)) {
            s.text = TextBox.msgWarm(VqsvText.Battle.PANEL_BAG_EGG_HATCH_NOT_READY,
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            bagMessageMode = 20;
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=3 q.O case0"
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
            s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=3 q.O case0"
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
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case0"
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
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case5"
                        + " confirm -> o.a(11) game.h.ad ride.ui"
                        + " sourceRow=[" + row.item.textId + "," + row.item.iconCell
                        + "," + row.item.descriptionTextId + "]"
                        + " close bag.ui selectedRide=0");
                return;
            case 6:
                badgeReturnToBag = true;
                badgeReturnBagSelected = selected;
                badgeReturnBagScroll = listScroll;
                mode = Mode.BADGE;
                selected = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=3 q.O case6"
                        + " confirm -> o.a(12) game.h.W badge.ui"
                        + " sourceRow=[" + row.item.textId + "," + row.item.iconCell
                        + "," + row.item.descriptionTextId + "]"
                        + " close bag.ui previousState=8");
                return;
            case 10:
                transmitReturnBagSelected = selected;
                transmitReturnBagScroll = listScroll;
                mode = Mode.TRANSMIT;
                selected = 0;
                listScroll = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED panel game.h.ac bagTab=3 q.O case10"
                        + " confirm -> o.a(24) game.k.h transmit.ui"
                        + " sourceRow=[" + row.item.textId + "," + row.item.iconCell
                        + "," + row.item.descriptionTextId + "]"
                        + " close bag.ui previousState=8 destinations=" + TRANSMIT_DESTINATIONS.length);
                return;
            case 7:
            case 8:
            case 9:
                visible = false;
                s.openPanelBagSpecialUsePetstate(row.specialId);
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.ac bagTab=3 q.O case" + row.specialId
                        + " confirm -> s=id o.a(19) close bag.ui open petstate.ui"
                        + " stack=" + row.count);
                return;
            default:
                s.sourceStateTrace.add("PENDING panel game.h.ac bagTab=3 q.O case" + row.specialId
                        + " confirm source behavior not audited in current route");
        }
    }

    private void tickTransmit(VqsvIntroDemo.Scene s) {
        int maxSelected = TRANSMIT_DESTINATIONS.length - 1;
        selected = clamp(selected, 0, maxSelected);
        if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, maxSelected);
            keepSelectedVisible(TRANSMIT_DESTINATIONS.length);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED panel game.k.i transmit key=4100"
                        + " selected=" + selected
                        + " label=" + TRANSMIT_DESTINATIONS[selected]);
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, maxSelected);
            keepSelectedVisible(TRANSMIT_DESTINATIONS.length);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED panel game.k.i transmit key=8448"
                        + " selected=" + selected
                        + " label=" + TRANSMIT_DESTINATIONS[selected]);
            }
        } else if (s.keyBack) {
            mode = Mode.BAG;
            bagTab = 3;
            selected = transmitReturnBagSelected;
            listScroll = transmitReturnBagScroll;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED panel game.k.i transmit back"
                    + " o.a(8) close transmit.ui -> P=8 bag.ui"
                    + " tab=3 selected=" + selected);
        } else if (s.key0) {
            int[] target = TRANSMIT_TARGETS[clamp(selected, 0, maxSelected)];
            s.sourceTransmitScene = target[0];
            s.sourceTransmitRoom = target[1];
            s.sourceTransmitX = target[2];
            s.sourceTransmitY = target[3];
            s.sourceTransmitG = target[4];
            s.sourceTransmitT = -1;
            s.sourceTransmitConfirmed = true;
            visible = false;
            s.sourceStateTrace.add("PORTED panel game.k.i transmit confirm"
                    + " h=" + selected
                    + " label=" + TRANSMIT_DESTINATIONS[selected]
                    + " -> game.l.B().p/q/r/s=[" + target[0] + "," + target[1]
                    + "," + target[2] + "," + target[3] + "]"
                    + " game.l.G=" + target[4]
                    + " game.l.B().t=-1 game.f.B().a(9)"
                    + " no q.O consume");
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
                listScroll = 0;
                selected = mainTaskCursor(s);
                keepSelectedVisible(taskRowsForRender(s, taskTab).size());
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
            visible = false;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.V key=10"
                    + " close task.ui -> P=0 world");
        }
        updateTaskSelectedLabel(s);
    }

    private void updateTaskSelectedLabel(VqsvIntroDemo.Scene s) {
        List<TaskRow> rows = taskRowsForRender(s, taskTab);
        taskSelectedLabelCache = rows.isEmpty()
                ? "Nhi\u1ec7m v\u1ee5"
                : rows.get(clamp(selected, 0, rows.size() - 1)).title;
    }

    private void tickTaskOption(VqsvIntroDemo.Scene s) {
        int maxSelected = Math.max(0, taskOptionData.options.length - 1);
        selected = clamp(selected, 0, maxSelected);
        if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, maxSelected);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aG key=4100"
                        + " taskOption selected=" + selected);
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, maxSelected);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aG key=8448"
                        + " taskOption selected=" + selected);
            }
        } else if (s.key0) {
            int result = selected;
            closeTaskOption(s, "confirm result=" + result, result);
        } else if (s.keyBack) {
            closeTaskOption(s, "back result=1", 1);
        }
    }

    private void closeTaskOption(VqsvIntroDemo.Scene s, String reason) {
        closeTaskOption(s, reason, -1);
    }

    private void closeTaskOption(VqsvIntroDemo.Scene s, String reason, int result) {
        if (taskOptionBranchTaskId >= 0) {
            if (result == 0) {
                s.sourceAcceptBranchTask(taskOptionBranchTaskId);
            } else {
                s.sourceStateTrace.add("PORTED source game.e opcode49 branch task not accepted"
                        + " taskId=" + taskOptionBranchTaskId + " result=" + result);
            }
        }
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aG " + reason
                + " close taskOption.ui");
        taskOptionBranchTaskId = -1;
        if (taskOptionReturnToTask) {
            mode = Mode.TASK;
            selected = taskSelectedBeforeOption;
            openedTicks = 0;
            updateTaskSelectedLabel(s);
            return;
        }
        visible = false;
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
                badgeReturnToBag = false;
                mode = Mode.BADGE;
                selected = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.O confirm c=1"
                        + " -> o.a(12) game.h.R badge.ui open");
            }
        }
    }

    private void tickBadge(VqsvIntroDemo.Scene s) {
        int before = selected;
        if (s.keyUp) {
            selected = clamp(selected - 4, 0, 7);
        } else if (s.keyDown) {
            selected = clamp(selected + 4, 0, 7);
        } else if (s.keyLeft) {
            selected = selected % 4 == 0 ? selected : selected - 1;
        } else if (s.keyRight) {
            selected = selected % 4 == 3 ? selected : selected + 1;
        } else if (s.keyBack) {
            if (badgeReturnToBag) {
                mode = Mode.BAG;
                bagTab = 3;
                selected = badgeReturnBagSelected;
                listScroll = badgeReturnBagScroll;
                openedTicks = 0;
                badgeReturnToBag = false;
                s.sourceStateTrace.add("PORTED panel game.h.X back"
                        + " o.b=8 close badge.ui -> P=8 bag.ui"
                        + " tab=3 selected=" + selected);
                return;
            }
            mode = Mode.RECORD;
            selected = 0;
            recordSelected = 1;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.X back"
                    + " close badge.ui -> P=9 record.ui selected=1");
            return;
        }
        if (selected != before) {
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.X navigation"
                    + " badgeIndex=" + selected
                    + " status=" + badgeStatusText(s, selected));
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
            mode = Mode.GAMESYSTEM;
            selected = 2;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.h.x confirm key=131072"
                    + " save game.g.B().k=" + settingsLevel
                    + " close help.ui -> P=13 gamesystem.ui selected=2");
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
        drawSoftkey(g, font, layout, ui, 10, layout.text(10, "Xac dinh"), 0xffffff);
        drawSoftkey(g, font, layout, ui, 11, layout.text(11, "Quay lai"), 0xffffff);
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
                            + "#nN\u00fat 1: Xem nhi\u1ec7m v\u1ee5"
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
        if (bagTab == 1) {
            drawText(g, font, layout, 55, layout.text(55, "Vat pham"),
                    color(layout.widget(55), 0x1c6c91));
            drawText(g, font, layout, 56, layout.text(56, "Trang thai"),
                    color(layout.widget(56), 0x1c6c91));
        } else if (bagTab == 2) {
            drawText(g, font, layout, 94, layout.text(94, "Vat pham"),
                    color(layout.widget(94), 0x1c6c91));
            drawText(g, font, layout, 95, layout.text(95, "So luong"),
                    color(layout.widget(95), 0x1c6c91));
        } else {
            drawText(g, font, layout, 14, layout.text(14, "Vat pham"),
                    color(layout.widget(14), 0x1c6c91));
            drawText(g, font, layout, 15, layout.text(15, "So luong"),
                    color(layout.widget(15), 0x1c6c91));
        }

        List<BagRow> rows = bagRows(s, bagTab);
        int first = visibleListStart(rows.size());
        int[] rowBackgrounds = bagRowBackgrounds(bagTab);
        int[] rowIcons = bagRowIcons(bagTab);
        int[] rowNames = bagRowNames(bagTab);
        int[] rowCounts = bagRowCounts(bagTab);
        for (int i = 0; i < rowBackgrounds.length; i++) {
            int rowIndex = first + i;
            VqsvUiLayout.UiWidget bg = layout.widget(rowBackgrounds[i]);
            if (bg != null) {
                int cell = rowIndex == selected ? bg.altId : bg.imageId;
                drawCellTopLeft(ui, g, cell, bg.x, bg.y);
            }
            if (rowIndex >= rows.size()) {
                continue;
            }
            BagRow row = rows.get(rowIndex);
            int iconWidget = rowIcons[i];
            int nameWidget = rowNames[i];
            int countWidget = rowCounts[i];
            drawCellTopLeft(itemIcons, g, row.item.iconCell,
                    layout.x(iconWidget, 57),
                    layout.y(iconWidget, 125 + i * 15));
            drawTextMarquee(g, font, layout, nameWidget, row.item.name,
                    layout.w(nameWidget, 60),
                    rowIndex == selected ? 0xffa500 : color(layout.widget(nameWidget), 0x1c6c91),
                    openedTicks);
            drawText(g, font, layout, countWidget, row.statusText(),
                    color(layout.widget(countWidget), 0x1c6c91));
        }
        String description = "";
        if (!rows.isEmpty()) {
            description = rows.get(clamp(selected, 0, rows.size() - 1)).item.description;
        }
        int descriptionWidget = bagDescriptionWidget(bagTab);
        drawText(g, font, layout, descriptionWidget, description,
                color(layout.widget(descriptionWidget), 0xd0010e));
        drawBagScrollbar(g, layout, rows.size(), first, selected, bagTab);
    }

    private void renderTransmit(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("transmit.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        fillBand(g, layout, 2, 0xc6f0ff, 7);
        fillBand(g, layout, 3, 0xbee7f2, 102);
        fillBand(g, layout, 4, 0x6cb7bb, 8);
        drawCell(layout, ui, g, 1);
        drawTextWide(g, font, layout, 11, layout.text(11, "G\u1eedi \u0111i"),
                0, layout.w(11, 48), color(layout.widget(11), 0xd0010e));

        int first = visibleListStart(TRANSMIT_DESTINATIONS.length);
        for (int i = 0; i < TRANSMIT_ROW_WIDGETS.length; i++) {
            int rowIndex = first + i;
            VqsvUiLayout.UiWidget row = layout.widget(TRANSMIT_ROW_WIDGETS[i]);
            if (row == null) {
                continue;
            }
            drawCellState(layout, ui, g, TRANSMIT_ROW_WIDGETS[i], rowIndex == selected);
            if (rowIndex < TRANSMIT_DESTINATIONS.length) {
                drawTextMarquee(g, font, layout, TRANSMIT_ROW_WIDGETS[i],
                        TRANSMIT_DESTINATIONS[rowIndex],
                        layout.w(TRANSMIT_ROW_WIDGETS[i], 59),
                        rowIndex == selected ? colorSelected(row) : color(row, 0x1c6c92),
                        openedTicks);
            }
        }
        drawTransmitScrollbar(g, layout);
        drawSoftkey(g, font, layout, ui, 14, layout.text(14, "X\u00e1c \u0111\u1ecbnh"), 0xffffff);
        drawSoftkey(g, font, layout, ui, 15, layout.text(15, "Quay l\u1ea1i"), 0xffffff);
    }

    private void renderPortableShop(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("bodyShop.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawCell(layout, ui, g, 1);
        fillBand(g, layout, 2, 0xbee7f2, 167);
        fillBand(g, layout, 3, 0x51d069, 72);
        drawCell(layout, ui, g, 14);
        drawCell(layout, ui, g, 15);
        drawTextWide(g, font, layout, 4, "Giao d\u1ecbch \u1edf c\u00e1c th\u01b0\u01a1ng \u0111i\u1ebfm", -46, 150,
                color(layout.widget(4), 0xd0010e));
        for (int i = 0; i < PORTABLE_SHOP_ROW_WIDGETS.length; i++) {
            int widgetId = PORTABLE_SHOP_ROW_WIDGETS[i];
            drawCellState(layout, ui, g, widgetId, i == selected);
            drawTextWide(g, font, layout, widgetId, PORTABLE_SHOP_LABELS[i], 0,
                    layout.w(widgetId, 108),
                    i == selected ? 0xffa500 : color(layout.widget(widgetId), 0x1c6c91));
        }
        drawTextMarquee(g, font, layout, 11, portableShopDescription(selected),
                layout.w(11, 130), 0x1c6c91, openedTicks);
        drawSoftkey(g, font, layout, ui, 12, layout.text(12, "Mua s\u1eafm"), 0xffffff);
        drawSoftkey(g, font, layout, ui, 13, layout.text(13, "Quay l\u1ea1i"), 0xffffff);
    }

    private void renderPortableShopBuy(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("shopbuy.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        SpriteAnim itemIcons = SpriteAnim.load(258);
        drawCell(layout, ui, g, 4);
        fillBand(g, layout, 1, 0xbee7f2, 160);
        fillBand(g, layout, 2, 0x51d069, 10);
        fillBand(g, layout, 3, 0xc6f0ff, 8);
        fillBand(g, layout, 8, 0x51d8e9, 91);
        drawTextWide(g, font, layout, 5, "Mua", 0, layout.w(5, 100),
                color(layout.widget(5), 0xffffff));
        drawTextWide(g, font, layout, 9, "V\u1eadt ph\u1ea9m", 0, 48,
                color(layout.widget(9), 0x1c6c91));
        drawTextWide(g, font, layout, 10, "Gi\u00e1 b\u00e1n", -6, 52,
                color(layout.widget(10), 0x1c6c91));

        int size = portableShopItemCount();
        int first = visibleListStart(size);
        for (int i = 0; i < SHOPBUY_ROW_BACKGROUNDS.length; i++) {
            int rowIndex = first + i;
            VqsvUiLayout.UiWidget bg = layout.widget(SHOPBUY_ROW_BACKGROUNDS[i]);
            if (bg != null) {
                int cell = rowIndex == selected ? bg.altId : bg.imageId;
                drawCellTopLeft(ui, g, cell, bg.x, bg.y);
            }
            if (rowIndex >= size) {
                continue;
            }
            BattleItemRow row = VqsvBattleTables.instance().item(rowIndex);
            if (row == null) {
                continue;
            }
            drawCellTopLeft(itemIcons, g, row.iconId,
                    layout.x(SHOPBUY_ROW_ICONS[i], 56),
                    layout.y(SHOPBUY_ROW_ICONS[i], 100 + i * 18));
            int rowColor = rowIndex == selected ? 0xffa500 : color(layout.widget(SHOPBUY_ROW_NAMES[i]), 0x1c6c91);
            drawTextMarquee(g, font, layout, SHOPBUY_ROW_NAMES[i], row.name("Item " + rowIndex),
                    layout.w(SHOPBUY_ROW_NAMES[i], 48), rowColor, openedTicks);
            drawTextWide(g, font, layout, SHOPBUY_ROW_PRICES[i],
                    String.valueOf(portableShopPrice(row)), 0,
                    layout.w(SHOPBUY_ROW_PRICES[i], 36), rowColor);
            drawCellTopLeft(ui, g, shopCurrencyCell(row.currencyOrType),
                    layout.x(SHOPBUY_ROW_CURRENCIES[i], 170),
                    layout.y(SHOPBUY_ROW_CURRENCIES[i], 101 + i * 18));
        }

        BattleItemRow selectedRow = VqsvBattleTables.instance().item(selected);
        String description = selectedRow == null ? "" : selectedRow.description("");
        drawTextMarquee(g, font, layout, 56, description,
                layout.w(56, 125), color(layout.widget(56), 0x1c6c91), openedTicks);
        drawCell(layout, ui, g, 41);
        drawCell(layout, ui, g, 42);
        drawText(g, font, layout, 43, String.valueOf(s.sourceBadges),
                color(layout.widget(43), 0x1c6c91));
        drawText(g, font, layout, 44, String.valueOf(s.sourceMoney),
                color(layout.widget(44), 0x1c6c91));
        drawShopbuyScrollbar(g, layout, size, first);
        drawSoftkey(g, font, layout, ui, 57, layout.text(57, "Mua s\u1eafm"), 0xffffff);
        drawSoftkey(g, font, layout, ui, 58, layout.text(58, "Quay l\u1ea1i"), 0xffffff);
    }

    private void renderPortableShopConfirm(Graphics2D g, FontBitmap font) {
        VqsvBattleRenderer.drawShopConfirmOverlay(g, font,
                shopConfirmQuantity, shopConfirmTotal, shopConfirmCurrency, openedTicks);
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
                taskTab == 0 ? TASK_TAB_SELECTED_COLOR : color(layout.widget(8), 0x00009a));
        drawTextWide(g, font, layout, 9, layout.text(9, "Nhiem vu phu"), -2, 64,
                taskTab == 1 ? TASK_TAB_SELECTED_COLOR : color(layout.widget(9), 0x00009a));
        drawSoftkey(g, font, layout, ui, 41, layout.text(41, "Xac dinh"), 0xffffff);
        drawSoftkey(g, font, layout, ui, 42, layout.text(42, "Quay lai"), 0xffffff);

        List<TaskRow> rows = taskRowsForRender(s, taskTab);
        selected = clamp(selected, 0, Math.max(0, rows.size() - 1));
        keepSelectedVisible(rows.size());
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
            int textColor = rowIndex == selected
                    ? activeTextColor(layout.widget(TASK_ROW_NAMES[i]), 0xef29cc)
                    : color(layout.widget(TASK_ROW_NAMES[i]), 0x1c6c91);
            drawText(g, font, layout, TASK_ROW_NUMBERS[i], String.valueOf(row.number),
                    rowIndex == selected
                            ? activeTextColor(layout.widget(TASK_ROW_NUMBERS[i]), 0xef29cc)
                            : color(layout.widget(TASK_ROW_NUMBERS[i]), 0x1c6c91));
            VqsvUiLayout.UiWidget nameWidget = layout.widget(TASK_ROW_NAMES[i]);
            VqsvUiLayout.UiWidget statusWidget = layout.widget(TASK_ROW_STATUS[i]);
            int nameWidth = nameWidget == null || statusWidget == null
                    ? layout.w(TASK_ROW_NAMES[i], 72)
                    : Math.max(1, statusWidget.x - nameWidget.x - 10);
            drawTextMarquee(g, font, layout, TASK_ROW_NAMES[i], row.title,
                    nameWidth, textColor, rowIndex == selected ? openedTicks : 0);
            drawTextWide(g, font, layout, TASK_ROW_STATUS[i], row.completed ? "Ho\u00e0n th\u00e0nh" : "",
                    0, layout.w(TASK_ROW_STATUS[i], 24),
                    rowIndex == selected
                            ? activeTextColor(layout.widget(TASK_ROW_STATUS[i]), 0xef29cc)
                            : color(layout.widget(TASK_ROW_STATUS[i]), 0x1c6c91));
        }
        String detail = "";
        if (!rows.isEmpty()) {
            detail = rows.get(clamp(selected, 0, rows.size() - 1)).detail;
        }
        drawWrappedTextBoxScrolled(g, font, layout, 36, detail,
                layout.w(36, 128), 30, activeTextColor(layout.widget(36), 0x000000),
                openedTicks);
        drawText(g, font, layout, 37, taskTab == 0
                        ? "\u0110\u1ea7u m\u1ed1i ch\u00ednh ho\u00e0n th\u00e0nh \u0111\u1ed9: "
                        : "Chi nh\u00e1nh ho\u00e0n th\u00e0nh \u0111\u1ed9: ",
                color(layout.widget(37), 0x1c6c91));
        drawText(g, font, layout, 38, taskProgressText(s, taskTab),
                color(layout.widget(38), 0xffffff));
        drawTaskScrollbar(g, layout, rows.size(), selected);
    }

    private void tickPortableShop(VqsvIntroDemo.Scene s) {
        int before = selected;
        if (s.keyUp) {
            selected = clamp(selected - 1, 0, PORTABLE_SHOP_LABELS.length - 1);
        } else if (s.keyDown) {
            selected = clamp(selected + 1, 0, PORTABLE_SHOP_LABELS.length - 1);
        } else if (s.keyBack) {
            mode = Mode.GAMEMENU;
            selected = 0;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui back"
                    + " -> P=6 gamemenu.ui selected=0");
            return;
        } else if (s.key0) {
            if (selected == 0) {
                mode = Mode.PORTABLE_SHOP_BUY;
                selected = 0;
                listScroll = 0;
                openedTicks = 0;
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui confirm c=0"
                        + " -> P=26 game.k.a(4,0) shopbuy.ui rows=" + portableShopItemCount());
                return;
            }
            if (selected == 1) {
                openPortableShopLevelUpConfirm(s);
                return;
            }
            if (selected == 2) {
                openPortableShopBadgeConfirm(s);
                return;
            }
            if (selected == 3) {
                openPortableShopMoneyConfirm(s);
                return;
            }
            s.text = TextBox.msgWarm("Ch\u1ee9c n\u0103ng c\u00f2n ch\u01b0a m\u1edf",
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            s.sourceStateTrace.add("PARTIAL panel game.k.aD bodyShop.ui premium branch c="
                    + selected + " not ported in portable shop item slice");
            return;
        }
        if (selected != before) {
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui key selected="
                    + selected + " description=" + portableShopDescription(selected));
        }
    }

    private void tickPortableShopBuy(VqsvIntroDemo.Scene s) {
        int size = portableShopItemCount();
        selected = clamp(selected, 0, Math.max(0, size - 1));
        if (s.keyUp) {
            int before = selected;
            selected = clamp(selected - 1, 0, Math.max(0, size - 1));
            keepSelectedVisible(size);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) shopbuy.ui key=4100"
                        + " selected=" + selected);
            }
        } else if (s.keyDown) {
            int before = selected;
            selected = clamp(selected + 1, 0, Math.max(0, size - 1));
            keepSelectedVisible(size);
            if (selected != before) {
                s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) shopbuy.ui key=8448"
                        + " selected=" + selected);
            }
        } else if (s.keyBack) {
            mode = Mode.PORTABLE_SHOP;
            selected = 0;
            listScroll = 0;
            openedTicks = 0;
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) shopbuy.ui back"
                    + " -> P=14 bodyShop.ui");
        } else if (s.key0) {
            openPortableShopConfirm(s);
        }
    }

    private void tickPortableShopConfirm(VqsvIntroDemo.Scene s) {
        BattleItemRow row = VqsvBattleTables.instance().item(shopConfirmItemId);
        if (row == null) {
            closePortableShopConfirm(s, "missing row");
            return;
        }
        int maxQty = portableShopMaxQuantity(s, shopConfirmItemId);
        if (s.keyLeft) {
            shopConfirmQuantity--;
            if (shopConfirmQuantity <= 0) {
                shopConfirmQuantity = Math.max(1, maxQty);
            }
            syncPortableShopConfirm(s);
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) msgyn.ui key=16400"
                    + " item=" + shopConfirmItemId
                    + " qty=" + shopConfirmQuantity
                    + " total=" + shopConfirmTotal);
            return;
        }
        if (s.keyRight) {
            shopConfirmQuantity++;
            if (shopConfirmQuantity > Math.max(1, maxQty)) {
                shopConfirmQuantity = 1;
            }
            syncPortableShopConfirm(s);
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) msgyn.ui key=32832"
                    + " item=" + shopConfirmItemId
                    + " qty=" + shopConfirmQuantity
                    + " total=" + shopConfirmTotal);
            return;
        }
        if (s.keyBack) {
            closePortableShopConfirm(s, "back");
            return;
        }
        if (s.key0) {
            commitPortableShopItem(s);
        }
    }

    private void openPortableShopLevelUpConfirm(VqsvIntroDemo.Scene s) {
        if (allPortableShopPetsMaxLevel(s)) {
            s.text = TextBox.msgWarm("Trong ba l\u00f4 s\u1ee7ng v\u1eadt \u0111\u1ec1u \u0111\u00e3 max level",
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            s.sourceStateTrace.add("PORTED panel game.k.aD bodyShop.ui product3 all-max warning"
                    + " pets=" + s.sourcePets.size()
                    + " msgwarm.ui f=1");
            return;
        }
        serviceConfirmTitle = "Th\u0103ng c\u1ea5p ch\u1eadm ch\u1ea1p, k\u1ebb \u0111\u1ecbch l\u1ea1i qu\u00e1 m\u1ea1nh? "
                + "T\u1ea5t c\u1ea3 s\u1ee7ng v\u1eadt trong ba l\u00f4 c\u1ee7a b\u1ea1n \u0111\u1ec1u \u0111\u01b0\u1ee3c th\u0103ng l\u00ean 5 c\u1ea5p.";
        serviceConfirmPrompt = "Mi\u1ec5n ph\u00ed";
        serviceProductId = 3;
        mode = Mode.PORTABLE_SHOP_SERVICE_CONFIRM;
        selected = 1;
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui product3"
                + " -> smsInfo.ui source-shaped PC-free confirm"
                + " pets=" + s.sourcePets.size());
    }

    private void openPortableShopBadgeConfirm(VqsvIntroDemo.Scene s) {
        serviceConfirmTitle = "Ki\u1ebfm Huy hi\u1ec7u kh\u00f3 kh\u0103n? "
                + "B\u1ea1n s\u1ebd \u0111\u1ea1t \u0111\u01b0\u1ee3c 10 Huy hi\u1ec7u.";
        serviceConfirmPrompt = "Mi\u1ec5n ph\u00ed";
        serviceProductId = 4;
        mode = Mode.PORTABLE_SHOP_SERVICE_CONFIRM;
        selected = 2;
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui product4"
                + " -> smsInfo.ui source-shaped PC-free confirm"
                + " badges=" + s.sourceBadges);
    }

    private void openPortableShopMoneyConfirm(VqsvIntroDemo.Scene s) {
        serviceConfirmTitle = "Ki\u1ebfm ti\u1ec1n v\u1ea5t v\u1ea3, v\u1eadt ph\u1ea9m \u0111\u1eaft \u0111\u1ecf? "
                + "B\u1ea1n s\u1ebd \u0111\u1ea1t \u0111\u01b0\u1ee3c 10000 kim ti\u1ec1n.";
        serviceConfirmPrompt = "Mi\u1ec5n ph\u00ed";
        serviceProductId = 2;
        mode = Mode.PORTABLE_SHOP_SERVICE_CONFIRM;
        selected = 3;
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD bodyShop.ui product2"
                + " -> smsInfo.ui source-shaped PC-free confirm"
                + " money=" + s.sourceMoney);
    }

    private void tickPortableShopServiceConfirm(VqsvIntroDemo.Scene s) {
        if (s.keyBack) {
            closePortableShopServiceConfirm(s, "back");
            return;
        }
        if (s.key0) {
            if (serviceProductId == 3) {
                applyPortableShopLevelUpProduct3(s);
                s.text = TextBox.msgWarm("S\u1ee7ng v\u1eadt trong ba l\u00f4 \u0111\u00e3 th\u0103ng c\u1ea5p",
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
            } else if (serviceProductId == 4) {
                applyPortableShopBadgeProduct4(s);
                s.text = TextBox.msgWarm("\u0110\u00e3 nh\u1eadn 10 Huy hi\u1ec7u",
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
            } else if (serviceProductId == 2) {
                applyPortableShopMoneyProduct2(s);
                s.text = TextBox.msgWarm("\u0110\u00e3 nh\u1eadn 10000 kim ti\u1ec1n",
                        VqsvText.Evolution.CONTINUE_PROMPT_5);
            }
            closePortableShopServiceConfirm(s, "success");
        }
    }

    private void closePortableShopServiceConfirm(VqsvIntroDemo.Scene s, String reason) {
        mode = Mode.PORTABLE_SHOP;
        selected = portableShopProductRow(serviceProductId);
        openedTicks = 0;
        int closedProduct = serviceProductId;
        serviceProductId = -1;
        serviceConfirmTitle = "";
        serviceConfirmPrompt = "";
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.aD close smsInfo.ui"
                + " product=" + closedProduct
                + " return bodyShop.ui reason=" + reason);
    }

    private static boolean allPortableShopPetsMaxLevel(VqsvIntroDemo.Scene s) {
        if (s.sourcePets.isEmpty()) {
            return true;
        }
        for (SourcePetState pet : s.sourcePets) {
            if (pet.level < 50) {
                return false;
            }
        }
        return true;
    }

    private static void applyPortableShopLevelUpProduct3(VqsvIntroDemo.Scene s) {
        s.sourceEvolutionQueue.clear();
        s.sourceEvolutionNoticeIndex = 0;
        s.sourceEvolutionL[0] = -1;
        s.sourceEvolutionL[1] = -1;
        s.sourceEvolutionI = 0;
        int changed = 0;
        for (int i = 0; i < s.sourcePets.size(); i++) {
            SourcePetState pet = s.sourcePets.get(i);
            int before = pet.level;
            if (before < 50) {
                pet.level = Math.min(50, before + 5);
                pet.slot = i;
                pet.refreshFromSourceDb();
                changed++;
            } else {
                pet.level = 50;
                pet.slot = i;
                pet.refreshFromSourceDb();
            }
            SourceEvolutionNotice notice = VqsvSourceEvolutionRuntime.noticeForPet(s, i);
            if (notice != null && notice.sourceR > 0 && pet.level >= notice.requiredLevel) {
                s.sourceEvolutionQueue.add(notice);
                if (s.sourceEvolutionL[0] == -1) {
                    s.sourceEvolutionL[0] = notice.currentLevel;
                    s.sourceEvolutionL[1] = notice.currentSpeciesId;
                }
                s.sourceStateTrace.add("PORTED/PARTIAL panel product3 evolution candidate"
                        + " petIndex=" + i
                        + " species=" + notice.currentSpeciesId
                        + " target=" + notice.targetSpeciesId
                        + " level=" + notice.currentLevel + "/" + notice.requiredLevel
                        + " material=" + notice.materialId
                        + " count=" + notice.materialCount + "/" + notice.materialNeed
                        + " materialBlocksConfirm=" + !notice.materialEnough);
            }
            s.sourceStateTrace.add("PORTED panel an.b(true) product3 pet level"
                    + " index=" + i
                    + " species=" + pet.speciesId
                    + " level=" + before + "->" + pet.level);
        }
        int sourceG = s.sourceEvolutionQueue.isEmpty() ? 2 : 1;
        s.sourceStateTrace.add("PORTED/PARTIAL panel an.b(true) product3 complete"
                + " changed=" + changed
                + " queue=" + s.sourceEvolutionQueue.size()
                + " game.k.G=" + sourceG
                + " game.k.L=[" + s.sourceEvolutionL[0] + "," + s.sourceEvolutionL[1] + "]");
    }

    private static void applyPortableShopBadgeProduct4(VqsvIntroDemo.Scene s) {
        int before = s.sourceBadges;
        s.sourceBadges = Math.max(0, s.sourceBadges + 10);
        s.sourceStateTrace.add("PORTED panel an.b(true) product4 badges"
                + " source game.g.o().u(10)"
                + " badges=" + before + "->" + s.sourceBadges);
    }

    private static void applyPortableShopMoneyProduct2(VqsvIntroDemo.Scene s) {
        int before = s.sourceMoney;
        s.sourceMoney = Math.max(0, s.sourceMoney + 10000);
        s.sourceStateTrace.add("PORTED panel an.b(true) product2 money"
                + " source game.g.o().s(10000)"
                + " money=" + before + "->" + s.sourceMoney);
    }

    private void renderTaskOption(Graphics2D g, FontBitmap font) {
        VqsvUiLayout layout = VqsvUiLayout.load("taskOption.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        SpriteAnim itemIcons = SpriteAnim.load(258);
        drawTaskOptionFrame(g, layout, ui);
        int[] rowBgs = {10, 11};
        int[] rowCursors = {7, 8};
        int[] rowTexts = {17, 18};
        for (int i = 0; i < rowBgs.length && i < taskOptionData.options.length; i++) {
            VqsvUiLayout.UiWidget bg = layout.widget(rowBgs[i]);
            if (bg != null) {
                int cell = bg.altId >= 0 ? bg.altId : bg.imageId;
                drawCellTopLeft(ui, g, cell, bg.x, bg.y);
                if (i == selected) {
                    VqsvUiLayout.UiWidget cursor = layout.widget(rowCursors[i]);
                    if (cursor != null && cursor.imageId >= 0) {
                        drawCellTopLeft(ui, g, cursor.imageId, cursor.x, cursor.y);
                    }
                }
            }
            drawTextWide(g, font, layout, rowTexts[i], taskOptionData.option(i), 0,
                    layout.w(rowTexts[i], 60),
                    i == selected ? colorSelected(layout.widget(rowTexts[i]))
                            : color(layout.widget(rowTexts[i]), 0x1c6c91));
        }
        drawText(g, font, layout, 12, layout.text(12, "Thuong"),
                color(layout.widget(12), 0xd0010e));
        int[] rewardIcons = {13, 15};
        int[] rewardTexts = {14, 16};
        for (int i = 0; i < rewardIcons.length && i < taskOptionData.rewards.length; i++) {
            TaskOptionReward reward = taskOptionData.rewards[i];
            VqsvUiLayout.UiWidget iconWidget = layout.widget(rewardIcons[i]);
            if (iconWidget != null) {
                if (reward.iconSprite == 258) {
                    drawCellTopLeft(itemIcons, g, reward.iconCell, iconWidget.x, iconWidget.y);
                } else {
                    drawCellTopLeft(ui, g, reward.iconCell, iconWidget.x, iconWidget.y);
                }
            }
            drawTextWide(g, font, layout, rewardTexts[i], reward.label, 0,
                    layout.w(rewardTexts[i], 48),
                    color(layout.widget(rewardTexts[i]), 0xd0010e));
        }
        if (taskOptionData.summary != null && !taskOptionData.summary.isEmpty()) {
            drawTextWide(g, font, layout, 21, taskOptionData.summary, 0,
                    layout.w(21, 72), color(layout.widget(21), 0xd0010e));
        }
        drawSoftkey(g, font, layout, ui, 19, layout.text(19, "Xac dinh"), 0xffffff);
        drawSoftkey(g, font, layout, ui, 20, layout.text(20, "Quay lai"), 0xffffff);
    }

    private void renderRecord(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("record.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawRecordFrame(g, font, layout, ui, recordSelected);
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
            drawTextWide(g, font, layout, PETMAP_TAB_LABELS[i], PETMAP_TAB_NAMES[i], -1,
                    Math.max(1, layout.w(PETMAP_TAB_LABELS[i], 12) - 6),
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

    private void renderBadge(Graphics2D g, FontBitmap font, VqsvIntroDemo.Scene s) {
        VqsvUiLayout layout = VqsvUiLayout.load("badge.ui");
        SpriteAnim ui = SpriteAnim.load(257);
        drawBadgeFrame(g, layout, ui);
        drawTextWide(g, font, layout, 5, layout.text(5, "Huy hieu"), 0,
                layout.w(5, 100), color(layout.widget(5), 0xd0010e));
        drawSoftkey(g, font, layout, ui, 6, layout.text(6, "Quay lai"), 0xffffff);
        for (int i = 0; i < BADGE_SLOT_WIDGETS.length; i++) {
            VqsvUiLayout.UiWidget slot = layout.widget(BADGE_SLOT_WIDGETS[i]);
            if (slot != null) {
                int cell = i == selected && slot.altId >= 0 ? slot.altId : slot.imageId;
                drawCellTopLeft(ui, g, cell, slot.x, slot.y);
            }
            VqsvUiLayout.UiWidget icon = layout.widget(BADGE_ICON_WIDGETS[i]);
            if (icon != null) {
                int cell = sourceBadgeAchieved(s, i) ? 46 + i
                        : (icon.altId >= 0 ? icon.altId : icon.imageId);
                drawCellTopLeft(ui, g, cell, icon.x, icon.y);
            }
        }
        drawTextWide(g, font, layout, 13, badgeName(selected), 0,
                layout.w(13, 48), color(layout.widget(13), 0x1c6c91));
        drawWrappedTextBox(g, font, layout, 14, badgeDescription(s, selected),
                layout.w(14, 135), 45, activeTextColor(layout.widget(14), 0x1c6c91));
        drawTextMarquee(g, font, layout, 15, layout.text(15, "Trang thai"),
                layout.w(15, 24), color(layout.widget(15), 0x204954), openedTicks,
                -4);
        drawTextMarquee(g, font, layout, 16, badgeStatusText(s, selected),
                layout.w(16, 40), color(layout.widget(16), 0x204954), openedTicks,
                -4);
        if (!sourceBadgeAchieved(s, selected)) {
            drawTextWide(g, font, layout, 33, badgeName(selected), 0,
                    layout.w(33, 36), color(layout.widget(33), 0x1c6c91));
        }
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

    private void drawTaskOptionFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        fillBand(g, layout, 2, 0xC6F0FF, 7);
        fillBand(g, layout, 3, 0xBDE8D7, 99);
        fillBand(g, layout, 4, 0x51D069, 8);
        drawCell(layout, ui, g, 1);
        drawCell(layout, ui, g, 9);
        drawCell(layout, ui, g, 19);
        drawCell(layout, ui, g, 20);
    }

    private void drawRecordFrame(Graphics2D g, FontBitmap font, VqsvUiLayout layout, SpriteAnim ui,
                                 int selectedOption) {
        drawCell(layout, ui, g, 1);
        fillBand(g, layout, 2, 0xBDE8D7, 7);
        fillBand(g, layout, 3, 0x51D069, 132);
        VqsvUiLayout.UiWidget left = layout.widget(4);
        VqsvUiLayout.UiWidget cursor = layout.widget(6);
        if (left != null && cursor != null) {
            g.setColor(new Color(0x51D069));
            g.fillRect(left.x - 8, left.y - 5, 144,
                    Math.max(34, cursor.y - left.y + 18));
        }
        drawRecordOptionButton(g, font, layout, ui, 4, 6,
                "H\u1ec7 th\u1ed1ng", "S\u1ee7ng v\u1eadt", selectedOption == 0);
        drawRecordOptionButton(g, font, layout, ui, 32, 7,
                "H\u1ec7 th\u1ed1ng", "Huy ch\u01b0\u01a1ng", selectedOption == 1);
        drawCell(layout, ui, g, 33);
        drawCell(layout, ui, g, 34);
    }

    private static void drawRecordOptionButton(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                               SpriteAnim ui, int widgetId, int cursorWidgetId,
                                               String line1, String line2, boolean selectedOption) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        g.setColor(new Color(selectedOption ? 0xffe866 : 0xfff29a));
        g.fillRect(widget.x, widget.y, Math.max(1, widget.w), 24);
        g.setColor(new Color(selectedOption ? 0xff7a00 : 0xb97000));
        g.drawRect(widget.x, widget.y, Math.max(1, widget.w), 24);
        drawCenteredLine(g, font, line1, widget.x, widget.y + 3, Math.max(1, widget.w), 0x162c27);
        drawCenteredLine(g, font, line2, widget.x, widget.y + 13, Math.max(1, widget.w), 0x162c27);
        if (selectedOption) {
            VqsvUiLayout.UiWidget cursor = layout.widget(cursorWidgetId);
            if (cursor != null && cursor.imageId >= 0) {
                drawCellTopLeft(ui, g, cursor.imageId, cursor.x, cursor.y);
            }
        }
    }

    private static void drawCenteredLine(Graphics2D g, FontBitmap font, String text,
                                         int x, int y, int width, int color) {
        int textWidth = font.taggedWidth(text);
        int textX = x + Math.max(0, (width - textWidth) / 2);
        font.drawTaggedLine(g, text, textX, y,
                TextBox.visibleLength(TextBox.decodeMojibake(text)), color);
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

    private void drawBadgeFrame(Graphics2D g, VqsvUiLayout layout, SpriteAnim ui) {
        fillBand(g, layout, 3, 0xC6F0FF, 9);
        fillBand(g, layout, 1, 0xBEE7F2, 159);
        fillBand(g, layout, 2, 0x6CB7BB, 8);
        drawCell(layout, ui, g, 4);
        drawCell(layout, ui, g, 6);
        drawCell(layout, ui, g, 8);
        drawCell(layout, ui, g, 9);
        drawCell(layout, ui, g, 10);
        drawCell(layout, ui, g, 11);
        drawCell(layout, ui, g, 12);
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

    private static void drawBagScrollbar(Graphics2D g, VqsvUiLayout layout, int rowCount,
                                         int scroll, int selectedRow, int tab) {
        int trackWidget = tab == 1 ? 83 : tab == 2 ? 122 : 42;
        int thumbWidget = tab == 1 ? 84 : tab == 2 ? 123 : 43;
        VqsvUiLayout.UiWidget track = layout.widget(trackWidget);
        VqsvUiLayout.UiWidget thumb = layout.widget(thumbWidget);
        if (track == null || thumb == null) {
            return;
        }
        int trackHeight = 72;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x51d069 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        int thumbH = rowCount > 5 ? Math.max(8, trackHeight * 5 / rowCount) : trackHeight;
        int y;
        if ((tab == 1 || tab == 2) && rowCount > 0) {
            y = track.y + Math.max(0, Math.min(selectedRow, rowCount - 1)) * trackHeight / rowCount;
            y = Math.min(y, track.y + trackHeight - thumbH);
        } else {
            int maxScroll = Math.max(1, rowCount - 5);
            y = track.y + (trackHeight - thumbH) * Math.max(0, Math.min(scroll, maxScroll)) / maxScroll;
        }
        g.setColor(new Color((thumb.jColor == 0 || thumb.jColor == -1
                ? 0xc6f0ff : thumb.jColor) & 0xffffff));
        g.fillRect(thumb.x, y, Math.max(1, thumb.w), Math.max(8, thumbH));
    }

    private static void drawTaskScrollbar(Graphics2D g, VqsvUiLayout layout, int rowCount, int selectedRow) {
        VqsvUiLayout.UiWidget track = layout.widget(39);
        VqsvUiLayout.UiWidget thumb = layout.widget(40);
        if (track == null || thumb == null) {
            return;
        }
        int trackHeight = 72;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x51d069 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        int total = Math.max(1, rowCount);
        int thumbH = rowCount > 5 ? Math.max(8, trackHeight * 5 / total) : trackHeight;
        int y = track.y;
        if (rowCount > 5) {
            y = 104 + (Math.max(0, Math.min(selectedRow, total - 1)) << 6) / total;
            y = Math.max(track.y, Math.min(track.y + trackHeight - thumbH, y));
        }
        g.setColor(new Color((thumb.jColor == 0 || thumb.jColor == -1
                ? 0xc6f0ff : thumb.jColor) & 0xffffff));
        g.fillRect(thumb.x, y, Math.max(1, thumb.w), Math.max(8, thumbH));
    }

    private static void drawShopbuyScrollbar(Graphics2D g, VqsvUiLayout layout, int rowCount, int scroll) {
        VqsvUiLayout.UiWidget track = layout.widget(38);
        if (track == null || rowCount <= 5) {
            return;
        }
        int trackHeight = 84;
        int thumbH = Math.max(8, trackHeight * 5 / Math.max(1, rowCount));
        int maxScroll = Math.max(1, rowCount - 5);
        int y = track.y + (trackHeight - thumbH) * Math.max(0, Math.min(scroll, maxScroll)) / maxScroll;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x51d8e9 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        g.setColor(new Color(0xc6f3ff));
        g.fillRect(track.x, y, Math.max(1, track.w), thumbH);
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

    private void drawTransmitScrollbar(Graphics2D g, VqsvUiLayout layout) {
        VqsvUiLayout.UiWidget track = layout.widget(12);
        VqsvUiLayout.UiWidget thumb = layout.widget(13);
        if (track == null || thumb == null) {
            return;
        }
        int trackHeight = 88;
        g.setColor(new Color((track.jColor == 0 || track.jColor == -1
                ? 0x006b63 : track.jColor) & 0xffffff));
        g.fillRect(track.x, track.y, Math.max(1, track.w), trackHeight);
        int thumbHeight = Math.max(10, trackHeight * 5 / TRANSMIT_DESTINATIONS.length);
        int y = 109 + clamp(selected, 0, TRANSMIT_DESTINATIONS.length - 1)
                * trackHeight / TRANSMIT_DESTINATIONS.length;
        y = Math.min(track.y + trackHeight - thumbHeight, Math.max(track.y, y));
        g.setColor(new Color((thumb.jColor == 0 || thumb.jColor == -1
                ? 0xc6f0ff : thumb.jColor) & 0xffffff));
        g.fillRect(thumb.x, y, Math.max(1, thumb.w), thumbHeight);
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

    private static void drawWrappedTextBox(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                           int widgetId, String text, int width, int height, int color) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        int w = Math.max(1, width);
        int h = Math.max(font.height + 1, height);
        Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y, w, h);
        int y = widget.y;
        for (String paragraph : text.split("#n", -1)) {
            for (String line : wrapText(font, paragraph, w)) {
                if (y > widget.y + h - font.height) {
                    g.setClip(oldClip);
                    return;
                }
                if (!line.isEmpty()) {
                    font.drawTaggedLine(g, line, widget.x, y,
                            TextBox.visibleLength(TextBox.decodeMojibake(line)), color);
                }
                y += font.height + 1;
            }
        }
        g.setClip(oldClip);
    }

    private static void drawWrappedTextBoxScrolled(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                                   int widgetId, String text, int width, int height,
                                                   int color, int tick) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        int w = Math.max(1, width);
        int h = Math.max(font.height + 1, height);
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("#n", -1)) {
            lines.addAll(wrapText(font, paragraph, w));
        }
        int lineStep = font.height + 1;
        int totalHeight = lines.size() * lineStep;
        int offset = 0;
        if (totalHeight > h) {
            int maxOffset = Math.max(1, totalHeight - h + lineStep);
            offset = Math.max(0, tick - 18) / 3;
            offset %= maxOffset;
        }
        Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y, w, h);
        int y = widget.y - offset;
        for (String line : lines) {
            if (y > widget.y + h) {
                break;
            }
            if (y >= widget.y - font.height && !line.isEmpty()) {
                font.drawTaggedLine(g, line, widget.x, y,
                        TextBox.visibleLength(TextBox.decodeMojibake(line)), color);
            }
            y += lineStep;
        }
        g.setClip(oldClip);
    }

    private static List<String> wrapText(FontBitmap font, String text, int width) {
        List<String> lines = new ArrayList<>();
        String clean = TextBox.decodeMojibake(text == null ? "" : text).trim();
        if (clean.isEmpty()) {
            lines.add("");
            return lines;
        }
        StringBuilder line = new StringBuilder();
        for (String word : clean.split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (font.taggedWidth(candidate) <= width || line.length() == 0) {
                line.setLength(0);
                line.append(candidate);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static void drawTextMarquee(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                        int widgetId, String text, int width, int color, int tick) {
        drawTextMarquee(g, font, layout, widgetId, text, width, color, tick, 0);
    }

    private static void drawTextMarquee(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                        int widgetId, String text, int width, int color, int tick,
                                        int dy) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null || text == null || text.isEmpty()) {
            return;
        }
        int w = Math.max(1, width);
        int textWidth = font.taggedWidth(text);
        Shape oldClip = g.getClip();
        int y = widget.y + dy;
        g.clipRect(widget.x, y - 1, w, Math.max(12, layout.h(widgetId, 12) - Math.min(0, dy)));
        int x = widget.x;
        if (textWidth > w) {
            int scroll = Math.max(0, tick * 2);
            if (scroll > textWidth - w) {
                scroll = -w + (scroll - (textWidth - w)) % Math.max(1, textWidth + w);
            }
            x = widget.x - scroll;
        } else if (widget.b == 4) {
            x = widget.x + Math.max(0, (w - textWidth) / 2);
        }
        font.drawTaggedLine(g, text, x, y,
                TextBox.visibleLength(TextBox.decodeMojibake(text)), color);
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

    private static void drawSoftkey(Graphics2D g, FontBitmap font, VqsvUiLayout layout,
                                    SpriteAnim ui, int widgetId, String text, int fallbackColor) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        if (widget == null) {
            return;
        }
        int cell = widget.altId >= 0 ? widget.altId : widget.imageId;
        int[] bounds = ui.cellBounds(cell);
        int width = bounds == null ? Math.max(1, layout.w(widgetId, 43)) : bounds[2];
        int height = bounds == null ? Math.max(12, layout.h(widgetId, 20)) : bounds[3];
        drawCellTopLeft(ui, g, cell, widget.x, widget.y);
        if (text == null || text.isEmpty()) {
            return;
        }
        Shape oldClip = g.getClip();
        g.clipRect(widget.x, widget.y - 1, Math.max(1, width), Math.max(12, height));
        int textWidth = font.taggedWidth(text);
        int x = widget.x + (width - textWidth) / 2;
        int y = widget.y + Math.max(0, (height - font.height) / 2) + 1;
        int color = widget.lColor == -1 ? 0xffffff : color(widget, fallbackColor);
        font.drawTaggedLine(g, text, x, y,
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

    private static int activeTextColor(VqsvUiLayout.UiWidget widget, int fallback) {
        if (widget == null || widget.gColor == 0 || widget.gColor == -1) {
            return fallback;
        }
        return widget.gColor & 0xffffff;
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
        if (bagTab == 2) {
            return "";
        }
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
        if (mode == Mode.TASK_OPTION) {
            return "game.k.aG";
        }
        if (mode == Mode.RECORD) {
            return "game.h.O";
        }
        if (mode == Mode.PETMAP) {
            return "game.h.Q";
        }
        if (mode == Mode.BADGE) {
            return "game.h.X";
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
        if (mode == Mode.TRANSMIT) {
            return "game.k.i";
        }
        if (mode == Mode.PORTABLE_SHOP) {
            return "game.k.aD";
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            return "game.k.a(byte4,0)";
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            return "game.k.a(byte4,0).msgyn";
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            return "game.k.aD.product" + serviceProductId + ".smsInfo";
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
        if (mode == Mode.TRANSMIT) {
            return "transmit.ui";
        }
        if (mode == Mode.PORTABLE_SHOP) {
            return "bodyShop.ui";
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            return "shopbuy.ui";
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            return "msgyn.ui";
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            return "smsInfo.ui";
        }
        if (mode == Mode.BAG) {
            return "bag.ui";
        }
        if (mode == Mode.TASK) {
            return "task.ui";
        }
        if (mode == Mode.TASK_OPTION) {
            return "taskOption.ui";
        }
        if (mode == Mode.RECORD) {
            return "record.ui";
        }
        if (mode == Mode.PETMAP) {
            return "petmap.ui";
        }
        if (mode == Mode.BADGE) {
            return "badge.ui";
        }
        return "gamemenu.ui";
    }

    private int[] rowWidgets() {
        if (mode == Mode.GAMESYSTEM) {
            return SYSTEM_ROW_WIDGETS;
        }
        if (mode == Mode.BAG) {
            return bagRowBackgrounds(bagTab);
        }
        if (mode == Mode.TASK) {
            return TASK_ROW_BACKGROUNDS;
        }
        if (mode == Mode.TASK_OPTION) {
            return new int[]{10, 11};
        }
        if (mode == Mode.PETMAP) {
            return PETMAP_ROW_BACKGROUNDS;
        }
        if (mode == Mode.RIDE) {
            return new int[]{4, 5, 6, 7};
        }
        if (mode == Mode.TRANSMIT) {
            return TRANSMIT_ROW_WIDGETS;
        }
        if (mode == Mode.PORTABLE_SHOP) {
            return PORTABLE_SHOP_ROW_WIDGETS;
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            return SHOPBUY_ROW_BACKGROUNDS;
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            return new int[]{13, 14};
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
        if (mode == Mode.TASK_OPTION) {
            return taskOptionData.options;
        }
        if (mode == Mode.RECORD) {
            return new String[]{"Minh h\u1ecda", "K\u1ef7 l\u1ee5c"};
        }
        if (mode == Mode.PETMAP) {
            return new String[]{"Minh h\u1ecda"};
        }
        if (mode == Mode.BADGE) {
            return new String[]{"Huy hi\u1ec7u"};
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
        if (mode == Mode.TRANSMIT) {
            return TRANSMIT_DESTINATIONS;
        }
        if (mode == Mode.PORTABLE_SHOP) {
            return PORTABLE_SHOP_LABELS;
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            return new String[]{"Mua"};
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            return new String[]{"X\u00e1c nh\u1eadn", "Kh\u00f4ng"};
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            return new String[]{"X\u00e1c nh\u1eadn", "Ph\u1ea3n h\u1ed3i"};
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
        if (mode == Mode.TASK_OPTION) {
            return taskOptionData.options.length;
        }
        if (mode == Mode.PETMAP) {
            return petmapRowsForRender(s, petmapTab).size();
        }
        if (mode == Mode.TRANSMIT) {
            return TRANSMIT_DESTINATIONS.length;
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            return portableShopItemCount();
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            return 2;
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            return 2;
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
        if (mode == Mode.TASK_OPTION) {
            return "game.k.aG close taskOption.ui";
        }
        if (mode == Mode.RECORD) {
            return "game.h.O close record.ui -> P=0";
        }
        if (mode == Mode.PETMAP) {
            return "game.h.Q close petmap.ui -> P=0";
        }
        if (mode == Mode.BADGE) {
            return "game.h.X close badge.ui -> P=0";
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
        if (mode == Mode.TRANSMIT) {
            return "game.k.i close transmit.ui -> P=8";
        }
        if (mode == Mode.PORTABLE_SHOP) {
            return "game.k.aD close bodyShop.ui -> P=6";
        }
        if (mode == Mode.PORTABLE_SHOP_BUY) {
            return "game.k.a(byte4,0) close shopbuy.ui -> P=14";
        }
        if (mode == Mode.PORTABLE_SHOP_CONFIRM) {
            return "game.k.a(byte4,0) close msgyn.ui -> shopbuy.ui";
        }
        if (mode == Mode.PORTABLE_SHOP_SERVICE_CONFIRM) {
            return "game.k.aD close smsInfo.ui -> bodyShop.ui";
        }
        return "game.h.l back close gamemenu.ui -> P=0";
    }

    private static String portableShopDescription(int row) {
        switch (row) {
            case 1:
                return "Th\u0103ng c\u1ea5p s\u1ee7ng v\u1eadt b\u1eb1ng d\u1ecbch v\u1ee5 th\u01b0\u01a1ng \u0111i\u1ebfm.";
            case 2:
                return "Mua s\u1eafm Huy hi\u1ec7u d\u00f9ng cho c\u00e1c giao d\u1ecbch \u0111\u1eb7c bi\u1ec7t.";
            case 3:
                return "Mua s\u1eafm kim ti\u1ec1n trong th\u01b0\u01a1ng \u0111i\u1ebfm.";
            default:
                return "T\u00f9y th\u1eddi mua s\u1eafm c\u00e1c lo\u1ea1i \u0111\u1ea1o c\u1ee5, gi\u00e0 tr\u1ebb kh\u00f4ng g\u1ea1t.";
        }
    }

    private static String portableShopServiceTitle(int productId) {
        if (productId == 3) {
            return "Mua \u0111\u1eb3ng c\u1ea5p";
        }
        if (productId == 4) {
            return "Mua s\u1eafm huy hi\u1ec7u";
        }
        if (productId == 2) {
            return "Mua s\u1eafm kim ti\u1ec1n";
        }
        return "X\u00e1c nh\u1eadn";
    }

    private static int portableShopProductRow(int productId) {
        if (productId == 3) {
            return 1;
        }
        if (productId == 4) {
            return 2;
        }
        if (productId == 2) {
            return 3;
        }
        return 0;
    }

    private static int portableShopItemCount() {
        VqsvBattleTables tables = VqsvBattleTables.instance();
        int count = 0;
        while (tables.item(count) != null) {
            count++;
        }
        return count;
    }

    private static int portableShopPrice(BattleItemRow row) {
        if (row == null) {
            return 0;
        }
        return row.currencyOrType == 0 ? row.priceOrValue * 3 / 2 : row.priceOrValue;
    }

    private static int shopCurrencyCell(int currency) {
        if (currency == 1) {
            return 83;
        }
        if (currency == 2) {
            return 74;
        }
        return 84;
    }

    private void openPortableShopConfirm(VqsvIntroDemo.Scene s) {
        BattleItemRow row = VqsvBattleTables.instance().item(selected);
        if (row == null) {
            s.text = TextBox.msgWarm(VqsvText.Battle.NO_SHOP_ITEMS, VqsvText.Evolution.CONTINUE_PROMPT_5);
            return;
        }
        int count = VqsvSourceOps.sourceItemCount(s, selected);
        if (count >= 99) {
            s.text = TextBox.msgWarm(VqsvText.Battle.SHOP_ITEM_FULL, VqsvText.Evolution.CONTINUE_PROMPT_5);
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) shop full"
                    + " item=" + selected + " count=" + count);
            return;
        }
        shopConfirmItemId = selected;
        shopConfirmQuantity = 1;
        syncPortableShopConfirm(s);
        mode = Mode.PORTABLE_SHOP_CONFIRM;
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) open msgyn.ui"
                + " item=" + shopConfirmItemId
                + " qty=" + shopConfirmQuantity
                + " total=" + shopConfirmTotal
                + " currency=" + shopConfirmCurrency);
    }

    private void closePortableShopConfirm(VqsvIntroDemo.Scene s, String reason) {
        mode = Mode.PORTABLE_SHOP_BUY;
        shopConfirmItemId = -1;
        shopConfirmQuantity = 1;
        shopConfirmTotal = 0;
        shopConfirmCurrency = 0;
        openedTicks = 0;
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.a(byte4,0) close msgyn.ui"
                + " return shopbuy.ui reason=" + reason);
    }

    private void syncPortableShopConfirm(VqsvIntroDemo.Scene s) {
        int maxQty = portableShopMaxQuantity(s, shopConfirmItemId);
        BattleItemRow row = VqsvBattleTables.instance().item(shopConfirmItemId);
        shopConfirmQuantity = Math.max(1, Math.min(shopConfirmQuantity, Math.max(1, maxQty)));
        shopConfirmCurrency = row == null ? 0 : row.currencyOrType;
        shopConfirmTotal = portableShopConfirmTotal(row, shopConfirmQuantity);
    }

    private static int portableShopMaxQuantity(VqsvIntroDemo.Scene s, int itemId) {
        return Math.max(0, 99 - VqsvSourceOps.sourceItemCount(s, itemId));
    }

    private static int portableShopConfirmTotal(BattleItemRow row, int qty) {
        if (row == null || row.currencyOrType == 2) {
            return 0;
        }
        return portableShopPrice(row) * Math.max(1, qty);
    }

    private void commitPortableShopItem(VqsvIntroDemo.Scene s) {
        BattleItemRow row = VqsvBattleTables.instance().item(shopConfirmItemId);
        if (row == null) {
            s.text = TextBox.msgWarm(VqsvText.Battle.NO_SHOP_ITEMS, VqsvText.Evolution.CONTINUE_PROMPT_5);
            closePortableShopConfirm(s, "missing row on commit");
            return;
        }
        int maxQty = portableShopMaxQuantity(s, shopConfirmItemId);
        int qty = Math.max(1, Math.min(shopConfirmQuantity, Math.max(1, maxQty)));
        int total = portableShopConfirmTotal(row, qty);
        if (maxQty <= 0) {
            s.text = TextBox.msgWarm(VqsvText.Battle.SHOP_ITEM_FULL, VqsvText.Evolution.CONTINUE_PROMPT_5);
            closePortableShopConfirm(s, "full on commit");
            return;
        }
        if (row.currencyOrType == 0 && s.sourceMoney < total) {
            s.text = TextBox.msgWarm("Kim ti\u1ec1n ch\u01b0a \u0111\u1ee7", VqsvText.Evolution.CONTINUE_PROMPT_5);
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.b(byte4,0) shop not-enough money"
                    + " item=" + shopConfirmItemId + " qty=" + qty
                    + " total=" + total + " money=" + s.sourceMoney);
            closePortableShopConfirm(s, "not enough money");
            return;
        }
        if (row.currencyOrType == 1 && s.sourceBadges < total) {
            s.text = TextBox.msgWarm("S\u1ed1 l\u01b0\u1ee3ng Huy hi\u1ec7u ch\u01b0a \u0111\u1ee7",
                    VqsvText.Evolution.CONTINUE_PROMPT_5);
            s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.b(byte4,0) shop not-enough badge"
                    + " item=" + shopConfirmItemId + " qty=" + qty
                    + " total=" + total + " badges=" + s.sourceBadges);
            closePortableShopConfirm(s, "not enough badge");
            return;
        }
        if (row.currencyOrType == 0) {
            s.sourceMoney -= total;
        } else if (row.currencyOrType == 1) {
            s.sourceBadges -= total;
        }
        VqsvSourceOps.sourceAddItem(s, shopConfirmItemId, qty);
        s.text = TextBox.msgWarm(VqsvText.Battle.SHOP_BUY_SUCCESS_PREFIX
                + row.name("Item " + shopConfirmItemId)
                + VqsvText.Battle.SHOP_BUY_SUCCESS_MIDDLE
                + qty, VqsvText.Evolution.CONTINUE_PROMPT_5);
        s.sourceStateTrace.add("PORTED/PARTIAL panel game.k.b(byte4,0) shop buy"
                + " item=" + shopConfirmItemId
                + " qty=" + qty
                + " total=" + total
                + " currency=" + row.currencyOrType
                + " money=" + s.sourceMoney
                + " badges=" + s.sourceBadges
                + " count=" + VqsvSourceOps.sourceItemCount(s, shopConfirmItemId)
                + (row.currencyOrType == 2 ? " SMS_FREE" : ""));
        closePortableShopConfirm(s, "success");
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
        if (bagTab == 1) {
            for (SourceEquipmentItem item : s.sourceEquipmentItems) {
                SourceItem source = sourceEquipmentItem(item.id);
                rows.add(new BagRow(source, 1, false, item.id,
                        item.equippedFlag ? "\u0110\u00e3 mang theo" : ""));
            }
            rows.sort(Comparator.comparingInt(row -> row.item.id));
            return rows;
        }
        if (bagTab == 2) {
            for (SourceMaterialItem item : s.sourceMaterialItems) {
                if (item.count <= 0) {
                    continue;
                }
                rows.add(new BagRow(sourceMaterialItem(item.id), item.count));
            }
            rows.sort(Comparator.comparingInt(row -> row.item.id));
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

    private static SourceItem sourceEquipmentItem(int equipmentId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(equipmentId);
        int textId = row == null ? -1 : row.nameTextId;
        int iconCell = VqsvSourceOps.sourceEquipmentIconCell(equipmentId);
        int descriptionTextId = row == null ? -1 : row.descriptionTextId;
        return new SourceItem(equipmentId, textId, iconCell, descriptionTextId,
                VqsvSourceOps.sourceEquipmentName(equipmentId),
                VqsvSourceOps.sourceEquipmentDescription(equipmentId), 1);
    }

    private static SourceItem sourceMaterialItem(int materialId) {
        BattleHeldItemRow row = VqsvBattleTables.instance().heldItem(materialId);
        int textId = row == null ? -1 : row.nameTextId;
        int iconCell = VqsvSourceOps.sourceMaterialIconCell(materialId);
        int descriptionTextId = row == null ? -1 : row.descriptionTextId;
        return new SourceItem(materialId, textId, iconCell, descriptionTextId,
                VqsvSourceOps.sourceMaterialName(materialId),
                VqsvSourceOps.sourceMaterialDescription(materialId), 2);
    }

    private static int[] bagRowBackgrounds(int tab) {
        if (tab == 1) {
            return BAG_EQUIP_ROW_BACKGROUNDS;
        }
        if (tab == 2) {
            return BAG_MATERIAL_ROW_BACKGROUNDS;
        }
        return BAG_ROW_BACKGROUNDS;
    }

    private static int[] bagRowIcons(int tab) {
        if (tab == 1) {
            return BAG_EQUIP_ROW_ICONS;
        }
        if (tab == 2) {
            return BAG_MATERIAL_ROW_ICONS;
        }
        if (tab == 3) {
            return BAG_SPECIAL_ROW_ICONS;
        }
        return BAG_ROW_ICONS;
    }

    private static int[] bagRowNames(int tab) {
        if (tab == 1) {
            return BAG_EQUIP_ROW_NAMES;
        }
        if (tab == 2) {
            return BAG_MATERIAL_ROW_NAMES;
        }
        if (tab == 3) {
            return BAG_SPECIAL_ROW_NAMES;
        }
        return BAG_ROW_NAMES;
    }

    private static int[] bagRowCounts(int tab) {
        if (tab == 1) {
            return BAG_EQUIP_ROW_STATUS;
        }
        if (tab == 2) {
            return BAG_MATERIAL_ROW_COUNTS;
        }
        if (tab == 3) {
            return BAG_SPECIAL_ROW_COUNTS;
        }
        return BAG_ROW_COUNTS;
    }

    private static int bagDescriptionWidget(int tab) {
        if (tab == 1) {
            return 85;
        }
        if (tab == 2) {
            return 124;
        }
        if (tab == 3) {
            return 163;
        }
        return 46;
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
        if (tab == 1) {
            List<SourceBranchTask> branchTasks = sourceBranchTasksForRender(s);
            int visibleCount = Math.min(half, branchTasks.size());
            for (int i = 0; i < visibleCount; i++) {
                SourceBranchTask task = branchTasks.get(i);
                int taskId = clamp(task.taskId, 0, half - 1);
                String title = source.get(taskId);
                String detail = taskId + half < source.size() ? source.get(taskId + half) : title;
                rows.add(new TaskRow(i + 1, title, detail, task.status == 3));
            }
            return rows;
        }
        int visibleCount = Math.min(half, Math.max(1, mainTaskCursor(s) + 1));
        for (int i = 0; i < visibleCount; i++) {
            String title = source.get(i);
            String detail = i + half < source.size() ? source.get(i + half) : title;
            boolean completed = i < mainTaskCursor(s);
            rows.add(new TaskRow(i + 1, title, detail, completed));
        }
        return rows;
    }

    private static int mainTaskCursor(VqsvIntroDemo.Scene s) {
        if (s == null) {
            return 0;
        }
        return Math.max(0, s.sourceMainTaskProgress);
    }

    private static List<SourceBranchTask> sourceBranchTasksForRender(VqsvIntroDemo.Scene s) {
        if (s == null) {
            return java.util.Collections.emptyList();
        }
        return s.sourceBranchQuests.tasks();
    }

    static String taskProgressTextForSmoke(VqsvIntroDemo.Scene s, int tab) {
        if (tab == 0) {
            List<String> tasks = loadMainTasks();
            int half = Math.max(1, tasks.size() / 2);
            int value = mainTaskCursor(s) * 1000 / half;
            if (s != null && s.sourcePremiumUiPercent) {
                return value / 10 + "." + value % 10 + "%";
            }
            int decimal = value % 10;
            if (decimal == 0) {
                decimal = 1;
            }
            return value / 50 + "." + decimal + "%";
        }
        List<String> tasks = loadBranchTasks();
        int half = Math.max(1, tasks.size() / 2);
        int done = 0;
        for (SourceBranchTask task : sourceBranchTasksForRender(s)) {
            if (task.status == 3) {
                done++;
            }
        }
        int value = done * 1000 / half;
        return value / 10 + "." + value % 10 + "%";
    }

    private static String taskProgressText(VqsvIntroDemo.Scene s, int tab) {
        return taskProgressTextForSmoke(s, tab);
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

    private static final class TaskOptionData {
        final TaskOptionReward[] rewards;
        final String[] options;
        final String summary;

        TaskOptionData(TaskOptionReward[] rewards, String[] options, String summary) {
            this.rewards = rewards;
            this.options = options;
            this.summary = summary;
        }

        String option(int index) {
            if (options.length == 0) {
                return "";
            }
            return options[clamp(index, 0, options.length - 1)];
        }

        static TaskOptionData smokeDefault() {
            SourceItem sandwich = VqsvSourceOps.sourceItem(4);
            return new TaskOptionData(
                    new TaskOptionReward[]{
                            TaskOptionReward.item(sandwich, "x1"),
                            TaskOptionReward.money(200)
                    },
                    new String[]{"Nh\u1eadn", "T\u1eeb ch\u1ed1i"},
                    ""
            );
        }

        static TaskOptionData branchTask(int taskId) {
            if (taskId == 0) {
                return new TaskOptionData(
                        new TaskOptionReward[]{
                                new TaskOptionReward(257, 84, "x3")
                        },
                        new String[]{"Ti\u1ebfp nh\u1eadn", "T\u1eeb ch\u1ed1i"},
                        ""
                );
            }
            if (taskId == 1) {
                return new TaskOptionData(
                        new TaskOptionReward[]{
                                new TaskOptionReward(257, 84, "x3")
                        },
                        new String[]{"Ti\u1ebfp nh\u1eadn", "T\u1eeb ch\u1ed1i"},
                        ""
                );
            }
            return smokeDefault();
        }
    }

    private static final class TaskOptionReward {
        final int iconSprite;
        final int iconCell;
        final String label;

        TaskOptionReward(int iconSprite, int iconCell, String label) {
            this.iconSprite = iconSprite;
            this.iconCell = iconCell;
            this.label = label;
        }

        static TaskOptionReward item(SourceItem item, String suffix) {
            return new TaskOptionReward(258, item.iconCell, item.name + " " + suffix);
        }

        static TaskOptionReward money(int amount) {
            return new TaskOptionReward(257, 84, String.valueOf(amount));
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

    private static boolean sourceBadgeAchieved(VqsvIntroDemo.Scene s, int badgeId) {
        return s != null && badgeId >= 0 && badgeId < Math.max(0, s.sourceBadges);
    }

    private static String badgeName(int badgeId) {
        short[] row = VqsvBattleTables.instance().row(2, badgeId);
        int textId = VqsvBattleTables.get(row, 0, -1);
        return VqsvBattleTables.instance().text(textId, "Huy hi\u1ec7u " + (badgeId + 1));
    }

    private static String badgeDescription(VqsvIntroDemo.Scene s, int badgeId) {
        short[] row = VqsvBattleTables.instance().row(2, badgeId);
        int level = sourceBadgeAchieved(s, badgeId) ? 1 : 0;
        int textId = VqsvBattleTables.get(row, 2 + level, -1);
        return VqsvBattleTables.instance().text(textId, "");
    }

    private static String badgeStatusText(VqsvIntroDemo.Scene s, int badgeId) {
        return sourceBadgeAchieved(s, badgeId) ? "\u0110\u00e3 \u0111\u1ea1t \u0111\u01b0\u1ee3c" : "Ch\u01b0a \u0111\u1ea1t";
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
