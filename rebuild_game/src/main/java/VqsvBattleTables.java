import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryTables;
import com.vqsv.rebuild.resource.ResourceLocator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VqsvBattleTables {
    private static final int GROUP_COUNT = 9;
    private static VqsvBattleTables cached;

    private final short[][][] groups;
    private final String[] texts;

    private VqsvBattleTables(short[][][] groups, String[] texts) {
        this.groups = groups;
        this.texts = texts;
    }

    static VqsvBattleTables instance() {
        if (cached == null) {
            cached = load();
        }
        return cached;
    }

    static String sourceSummary() {
        VqsvBattleTables tables = instance();
        StringBuilder out = new StringBuilder("battleTables");
        for (int i = 0; i < GROUP_COUNT; i++) {
            out.append(" g").append(i).append('=').append(tables.rowCount(i));
        }
        BattleSpeciesRow bunny = tables.species(34);
        BattleSpeciesRow elder = tables.species(68);
        BattleSpeciesRow kidnapping = tables.species(5);
        out.append(" species34=").append(bunny == null ? "missing" : bunny.shortDebugName());
        out.append(" species68=").append(elder == null ? "missing" : elder.shortDebugName());
        out.append(" species5=").append(kidnapping == null ? "missing" : kidnapping.shortDebugName());
        return out.toString();
    }

    private static VqsvBattleTables load() {
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            ResourceLocator locator = new ResourceLocator(paths);
            com.vqsv.rebuild.resource.BinaryReader reader = locator.binary(paths.scriptOriginal("db.mid"));
            short[][][] groups = new short[GROUP_COUNT][][];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = BinaryTables.readShortRows(reader);
            }
            return new VqsvBattleTables(groups, readTextRows(paths));
        } catch (RuntimeException ex) {
            return new VqsvBattleTables(new short[GROUP_COUNT][][], new String[0]);
        }
    }

    int rowCount(int group) {
        short[][] rows = groupRows(group);
        return rows == null ? 0 : rows.length;
    }

    short[] row(int group, int row) {
        short[][] rows = groupRows(group);
        if (rows == null || row < 0 || row >= rows.length) {
            return null;
        }
        return rows[row] == null ? null : Arrays.copyOf(rows[row], rows[row].length);
    }

    BattleSpeciesRow species(int id) {
        short[] row = row(0, id);
        return row == null ? null : new BattleSpeciesRow(this, id, row);
    }

    BattleSkillRow skill(int id) {
        short[] row = row(1, id);
        return row == null ? null : new BattleSkillRow(this, id, row);
    }

    BattleStatusRow status(int id) {
        short[] row = row(3, id);
        return row == null ? null : new BattleStatusRow(this, id, row);
    }

    BattleItemRow item(int id) {
        short[] row = row(4, id);
        return row == null ? null : new BattleItemRow(this, id, row);
    }

    BattleBuffRow buff(int id) {
        short[] row = row(6, id);
        return row == null ? null : new BattleBuffRow(this, id, row);
    }

    BattleDebuffRow debuff(int id) {
        short[] row = row(7, id);
        return row == null ? null : new BattleDebuffRow(this, id, row);
    }

    String text(int id, String fallback) {
        if (id < 0 || id >= texts.length || texts[id] == null || texts[id].isEmpty()) {
            return fallback;
        }
        return texts[id];
    }

    private short[][] groupRows(int group) {
        if (group < 0 || group >= groups.length) {
            return null;
        }
        return groups[group];
    }

    private static String[] readTextRows(AssetPaths paths) {
        try {
            java.nio.file.Path path = paths.modulesRoot()
                    .resolve("script").resolve("decoded").resolve("data__script__chs.mid.json");
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<String> rows = new ArrayList<>();
            Matcher matcher = Pattern.compile("\\[\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\]").matcher(json);
            while (matcher.find()) {
                rows.add(decodeMojibake(unescapeJsonString(matcher.group(1))));
            }
            return rows.toArray(new String[0]);
        } catch (IOException ex) {
            return new String[0];
        }
    }

    private static String unescapeJsonString(String raw) {
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch != '\\' || i + 1 >= raw.length()) {
                out.append(ch);
                continue;
            }
            char next = raw.charAt(++i);
            switch (next) {
                case '"':
                case '\\':
                case '/':
                    out.append(next);
                    break;
                case 'n':
                    out.append('\n');
                    break;
                case 'r':
                    out.append('\r');
                    break;
                case 't':
                    out.append('\t');
                    break;
                case 'u':
                    if (i + 4 < raw.length()) {
                        out.append((char) Integer.parseInt(raw.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    break;
                default:
                    out.append(next);
                    break;
            }
        }
        return out.toString();
    }

    private static String decodeMojibake(String text) {
        if (text == null) {
            return null;
        }
        String current = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFC);
        for (int i = 0; i < 4 && looksMojibake(current); i++) {
            String decoded = decodeMojibakeOnce(current);
            if (decoded.equals(current)) {
                break;
            }
            current = java.text.Normalizer.normalize(decoded, java.text.Normalizer.Form.NFC);
        }
        return current;
    }

    private static String decodeMojibakeOnce(String text) {
        try {
            ByteBuffer bytes = java.nio.charset.Charset.forName("windows-1252")
                    .newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap(text));
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .decode(bytes)
                    .toString();
        } catch (CharacterCodingException ex) {
            return text;
        }
    }

    private static boolean looksMojibake(String text) {
        return text.indexOf('\u00c3') >= 0
                || text.indexOf('\u00c2') >= 0
                || text.indexOf('\u00c4') >= 0
                || text.indexOf('\u00c5') >= 0
                || text.indexOf('\u00c6') >= 0
                || text.indexOf('\u00e2') >= 0
                || text.indexOf('\u00e1') >= 0
                || text.indexOf('\u00c1') >= 0
                || text.indexOf('\u20ac') >= 0;
    }

    static int get(short[] row, int index, int fallback) {
        if (row == null || index < 0 || index >= row.length) {
            return fallback;
        }
        return row[index];
    }
}

final class BattleSpeciesRow {
    private static final int[] NATURE_MULT = {90, 95, 100, 110, 125};

    final int id;
    final int nameTextId;
    final int element;
    final int quality;
    final int spriteId;
    final int learnGroup;
    final int relationClass;
    final short[] raw;
    private final VqsvBattleTables tables;

    BattleSpeciesRow(VqsvBattleTables tables, int id, short[] raw) {
        this.tables = tables;
        this.id = id;
        this.raw = raw;
        this.nameTextId = VqsvBattleTables.get(raw, 0, -1);
        this.element = VqsvBattleTables.get(raw, 1, -1);
        this.quality = VqsvBattleTables.get(raw, 3, 1);
        this.spriteId = VqsvBattleTables.get(raw, 17, -1);
        this.learnGroup = VqsvBattleTables.get(raw, 18, -1);
        this.relationClass = VqsvBattleTables.get(raw, 22, 0);
    }

    boolean validForBattle() {
        return raw.length >= 23;
    }

    String name(String fallback) {
        return tables.text(nameTextId, fallback);
    }

    int statHp(int level, int nature) {
        return sourceStat(level, nature, 5, 6, 7);
    }

    int statAttack(int level, int nature) {
        return sourceStat(level, nature, 8, 9, 10);
    }

    int statDefense(int level, int nature) {
        int idx = natureIndex(nature);
        return ((VqsvBattleTables.get(raw, 11, 0)
                + VqsvBattleTables.get(raw, 12, 0) * level / 10
                + VqsvBattleTables.get(raw, 13, 0)) * NATURE_MULT[idx]) / 100;
    }

    int statSpeed(int level, int nature) {
        int idx = natureIndex(nature);
        return ((VqsvBattleTables.get(raw, 14, 0)
                + VqsvBattleTables.get(raw, 15, 0) * level / 10
                + VqsvBattleTables.get(raw, 16, 0)) * NATURE_MULT[idx]) / 100;
    }

    String shortDebugName() {
        return name("species " + id) + "[id=" + id
                + ",element=" + element
                + ",sprite=" + spriteId
                + ",relationClass=" + relationClass + "]";
    }

    private int sourceStat(int level, int nature, int baseIndex, int levelIndex, int addIndex) {
        int idx = natureIndex(nature);
        return ((VqsvBattleTables.get(raw, baseIndex, 0)
                + VqsvBattleTables.get(raw, levelIndex, 0) * level
                + VqsvBattleTables.get(raw, addIndex, 0)) * NATURE_MULT[idx]) / 100;
    }

    private static int natureIndex(int nature) {
        return Math.max(0, Math.min(NATURE_MULT.length - 1, nature - 1));
    }
}

final class BattleSkillRow {
    final int id;
    final int elementFamily;
    final int nameTextId;
    final int descriptionTextId;
    final int powerPercent;
    final int learnTier;
    final int ppMax;
    final int effectMode;
    final int effectId;
    final int chanceOrParam;
    final int targetSide;
    final short[] raw;
    private final VqsvBattleTables tables;

    BattleSkillRow(VqsvBattleTables tables, int id, short[] raw) {
        this.tables = tables;
        this.id = id;
        this.raw = raw;
        this.elementFamily = VqsvBattleTables.get(raw, 0, -1);
        this.nameTextId = VqsvBattleTables.get(raw, 1, -1);
        this.descriptionTextId = VqsvBattleTables.get(raw, 2, -1);
        this.powerPercent = VqsvBattleTables.get(raw, 3, 0);
        this.learnTier = VqsvBattleTables.get(raw, 4, 0);
        this.ppMax = VqsvBattleTables.get(raw, 5, 0);
        this.effectMode = VqsvBattleTables.get(raw, 6, 0);
        this.effectId = VqsvBattleTables.get(raw, 7, -1);
        this.chanceOrParam = VqsvBattleTables.get(raw, 8, -1);
        this.targetSide = VqsvBattleTables.get(raw, 9, 0);
    }

    String name(String fallback) {
        return tables.text(nameTextId, fallback);
    }

    String description(String fallback) {
        return tables.text(descriptionTextId, fallback);
    }
}

final class BattleStatusRow {
    final int id;
    final int nameTextId;
    final int iconOrType;
    final int descriptionTextId;
    final short[] raw;
    private final VqsvBattleTables tables;

    BattleStatusRow(VqsvBattleTables tables, int id, short[] raw) {
        this.tables = tables;
        this.id = id;
        this.raw = raw;
        this.nameTextId = VqsvBattleTables.get(raw, 0, -1);
        this.iconOrType = VqsvBattleTables.get(raw, 1, -1);
        this.descriptionTextId = VqsvBattleTables.get(raw, 2, -1);
    }

    String name(String fallback) {
        return tables.text(nameTextId, fallback);
    }
}

final class BattleItemRow {
    final int id;
    final int nameTextId;
    final int iconId;
    final int descriptionTextId;
    final int priceOrValue;
    final int currencyOrType;
    final int behavior;
    final int paramA;
    final int paramB;
    final int paramC;
    final short[] raw;
    private final VqsvBattleTables tables;

    BattleItemRow(VqsvBattleTables tables, int id, short[] raw) {
        this.tables = tables;
        this.id = id;
        this.raw = raw;
        this.nameTextId = VqsvBattleTables.get(raw, 0, -1);
        this.iconId = VqsvBattleTables.get(raw, 1, -1);
        this.descriptionTextId = VqsvBattleTables.get(raw, 2, -1);
        this.priceOrValue = VqsvBattleTables.get(raw, 3, 0);
        this.currencyOrType = VqsvBattleTables.get(raw, 4, 0);
        this.behavior = VqsvBattleTables.get(raw, 5, -1);
        this.paramA = VqsvBattleTables.get(raw, 6, 0);
        this.paramB = VqsvBattleTables.get(raw, 7, 0);
        this.paramC = VqsvBattleTables.get(raw, 8, 0);
    }

    String name(String fallback) {
        return tables.text(nameTextId, fallback);
    }
}

final class BattleBuffRow {
    final int id;
    final int nameTextId;
    final int descriptionTextId;
    final int duration;
    final int paramA;
    final int paramB;
    final short[] raw;
    private final VqsvBattleTables tables;

    BattleBuffRow(VqsvBattleTables tables, int id, short[] raw) {
        this.tables = tables;
        this.id = id;
        this.raw = raw;
        this.nameTextId = VqsvBattleTables.get(raw, 0, -1);
        this.descriptionTextId = VqsvBattleTables.get(raw, 1, -1);
        this.duration = VqsvBattleTables.get(raw, 2, 0);
        this.paramA = VqsvBattleTables.get(raw, 3, 0);
        this.paramB = VqsvBattleTables.get(raw, 4, 0);
    }

    String name(String fallback) {
        return tables.text(nameTextId, fallback);
    }
}

final class BattleDebuffRow {
    final int id;
    final int nameTextId;
    final int descriptionTextId;
    final int duration;
    final short[] raw;
    private final VqsvBattleTables tables;

    BattleDebuffRow(VqsvBattleTables tables, int id, short[] raw) {
        this.tables = tables;
        this.id = id;
        this.raw = raw;
        this.nameTextId = VqsvBattleTables.get(raw, 0, -1);
        this.descriptionTextId = VqsvBattleTables.get(raw, 1, -1);
        this.duration = VqsvBattleTables.get(raw, 2, 0);
    }

    String name(String fallback) {
        return tables.text(nameTextId, fallback);
    }
}
