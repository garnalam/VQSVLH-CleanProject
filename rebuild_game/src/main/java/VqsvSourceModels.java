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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BagItem {
    final int id;
    final int bagChannel;
    final boolean keepAtZero;
    int count;

    BagItem(int id, int count, int bagChannel, boolean keepAtZero) {
        this.id = id;
        this.count = count;
        this.bagChannel = bagChannel;
        this.keepAtZero = keepAtZero;
    }
}

final class SourceItem {
    final int id;
    final int textId;
    final String name;
    final int bagChannel;

    SourceItem(int id, int textId, String name, int bagChannel) {
        this.id = id;
        this.textId = textId;
        this.name = name;
        this.bagChannel = bagChannel;
    }
}

final class SourceSpecialReward {
    final int id;
    final int textId;
    final int iconId;
    final int descriptionTextId;
    final String name;
    boolean unlocked;
    int stackCount;
    String gameGPath = "";

    SourceSpecialReward(int id, int textId, int iconId, int descriptionTextId, String name) {
        this.id = id;
        this.textId = textId;
        this.iconId = iconId;
        this.descriptionTextId = descriptionTextId;
        this.name = name;
    }

    static SourceSpecialReward fromSourceDb(int rewardId) {
        if (rewardId == 5) {
            return new SourceSpecialReward(5, 300, 47, 308, VqsvText.Items.PET_BOOK_PAGE);
        }
        return new SourceSpecialReward(rewardId, 0, 0, 0, "Reward " + rewardId);
    }

    void applySourceGameGSemantics(int qty) {
        if (id == 7 || id == 8 || id == 9) {
            stackCount = Math.min(99, stackCount + qty);
            gameGPath = "game.g.d -> game.g.c stack special item";
            return;
        }
        unlocked = true;
        if (id == 0) {
            gameGPath = "game.g.d -> game.g.e(id,-1) mark active special";
        } else {
            gameGPath = "game.g.d -> game.g.i(id) unlock vector entry";
        }
    }
}

final class SourceBattleUnit {
    private static final int[] NATURE_MULT = {90, 95, 100, 110, 125};

    final int speciesId;
    final int level;
    final int nature;
    final String name;
    final int maxHp;
    int hp;
    final int attack;
    final int defense;
    final int speed;
    final int element;
    final int visualId;

    SourceBattleUnit(int speciesId, int level, int nature, String name,
                     int maxHp, int attack, int defense, int speed,
                     int element, int visualId) {
        this.speciesId = speciesId;
        this.level = level;
        this.nature = nature;
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.element = element;
        this.visualId = visualId;
    }

    static SourceBattleUnit enemyFromEncounter(int[] encounter) {
        int species = encounter.length > 0 ? encounter[0] : -1;
        int level = encounter.length > 1 ? encounter[1] : 1;
        int nature = encounter.length > 2 ? encounter[2] : 3;
        return fromSpecies(species, level, nature, false);
    }

    static SourceBattleUnit playerFromSourcePets(List<SourcePetState> pets) {
        if (pets.isEmpty()) {
            return fallback(-1, 1, 3, "Neil", 120, 22, 12, 10);
        }
        SourcePetState pet = pets.get(0);
        int level = Math.max(1, pet.level);
        return fromSpecies(pet.speciesId, level, 3, true);
    }

    static SourceBattleUnit fromSpecies(int species, int level, int nature, boolean playerSide) {
        short[] row = SourceBattleDb.instance().speciesRow(species);
        if (row == null || row.length < 23) {
            String fallbackName = playerSide ? "Pet " + species : "Enemy " + species;
            return fallback(species, level, nature, fallbackName, 80 + level * 4, 18 + level, 8 + level / 2, 8);
        }
        int idx = Math.max(0, Math.min(NATURE_MULT.length - 1, nature - 1));
        int mult = NATURE_MULT[idx];
        int hp = ((row[5] + row[6] * level + row[7]) * mult) / 100;
        int atk = ((row[8] + row[9] * level + row[10]) * mult) / 100;
        int def = ((row[11] + row[12] * level / 10 + row[13]) * mult) / 100;
        int spd = ((row[14] + row[15] * level / 10 + row[16]) * mult) / 100;
        String sourceName = SourceBattleDb.instance().text(row[0], playerSide ? "Pet " + species : "Enemy " + species);
        return new SourceBattleUnit(species, level, nature, sourceName,
                Math.max(1, hp), Math.max(1, atk), Math.max(0, def), Math.max(1, spd),
                row[1], row[17]);
    }

    static SourceBattleUnit fallback(int species, int level, int nature, String name,
                                     int maxHp, int attack, int defense, int speed) {
        return new SourceBattleUnit(species, level, nature, name,
                Math.max(1, maxHp), Math.max(1, attack), Math.max(0, defense), Math.max(1, speed),
                -1, -1);
    }

    boolean alive() {
        return hp > 0;
    }

    int nextLevelEnergy() {
        if (level >= 50) {
            return 37300;
        }
        return Math.max(1, (level + 1) * 15 * (level + 1) - 200);
    }

    int basicDamageTo(SourceBattleUnit target) {
        int raw = attack - target.defense;
        int levelPart = Math.max(1, level / 2);
        int damage = Math.max(1, raw + levelPart);
        byte relation = elementRelationTo(target);
        if (relation == 0) {
            damage = damage * 3 / 2;
        } else if (relation == 1) {
            damage = Math.max(1, damage * 2 / 3);
        }
        return damage;
    }

    void damage(int amount) {
        hp = Math.max(0, hp - Math.max(1, amount));
    }

    byte elementRelationTo(SourceBattleUnit target) {
        boolean attackerEffective = true;
        boolean defenderEffective = true;
        if (visualId == 2 && target.visualId == 2) {
            attackerEffective = true;
            defenderEffective = true;
        } else if (visualId == 2 && target.visualId != 2) {
            attackerEffective = true;
            defenderEffective = false;
        } else if (visualId != 2 && target.visualId == 2) {
            attackerEffective = false;
            defenderEffective = true;
        }
        if (attackerEffective && beats(element, target.element)) {
            return 0;
        }
        if (defenderEffective && beats(target.element, element)) {
            return 1;
        }
        return -1;
    }

    private static boolean beats(int a, int b) {
        return (a == 0 && b == 1)
                || (a == 1 && b == 2)
                || (a == 2 && b == 3)
                || (a == 3 && b == 0)
                || (a == 5 && b == 6)
                || (a == 6 && b == 4)
                || (a == 4 && b == 5);
    }

    @Override
    public String toString() {
        return name + "(species=" + speciesId
                + ",lv=" + level
                + ",nature=" + nature
                + ",hp=" + maxHp
                + ",atk=" + attack
                + ",def=" + defense
                + ",spd=" + speed
                + ",element=" + element
                + ",visual=" + visualId + ")";
    }
}

final class SourceBattleDb {
    private static SourceBattleDb cached;
    private final short[][] speciesRows;
    private final String[] texts;

    SourceBattleDb(short[][] speciesRows, String[] texts) {
        this.speciesRows = speciesRows;
        this.texts = texts;
    }

    static SourceBattleDb instance() {
        if (cached == null) {
            cached = load();
        }
        return cached;
    }

    private static SourceBattleDb load() {
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            ResourceLocator locator = new ResourceLocator(paths);
            com.vqsv.rebuild.resource.BinaryReader reader = locator.binary(paths.scriptOriginal("db.mid"));
            short[][][] groups = new short[9][][];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = BinaryTables.readShortRows(reader);
            }
            return new SourceBattleDb(groups[0], readTextRows(paths));
        } catch (RuntimeException ex) {
            return new SourceBattleDb(new short[0][], new String[0]);
        }
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

    short[] speciesRow(int species) {
        if (species < 0 || species >= speciesRows.length) {
            return null;
        }
        return speciesRows[species];
    }

    String text(int id, String fallback) {
        if (id < 0 || id >= texts.length || texts[id] == null || texts[id].isEmpty()) {
            return fallback;
        }
        return texts[id];
    }
}

final class SourcePetState {
    int speciesId;
    int level;
    int slot;
    int arg3;
    int arg4;
    final int[] skillIds = new int[]{-1, -1, -1, -1};
    final int[] skillCooldowns = new int[skillIds.length];
    int refreshCount;

    SourcePetState() {
    }

    SourcePetState(int slot, int speciesId, int level, int arg3, int arg4, int skillA, int skillB) {
        this.slot = slot;
        this.speciesId = speciesId;
        this.level = level;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.skillIds[0] = skillA;
        this.skillIds[1] = skillB;
        refreshFromSourceDb();
    }

    void refreshFromSourceDb() {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] != -1) {
                skillCooldowns[i] = sourceSkillCooldown(skillIds[i]);
            }
        }
        refreshCount++;
    }

    private static int sourceSkillCooldown(int skillId) {
        switch (skillId) {
            default:
                return 0;
        }
    }
}
