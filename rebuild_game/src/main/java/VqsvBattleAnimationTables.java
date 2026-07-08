import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryReader;
import com.vqsv.rebuild.resource.BinaryTables;
import com.vqsv.rebuild.resource.ResourceLocator;

import java.util.Arrays;

final class VqsvBattleAnimationTables {
    private static VqsvBattleAnimationTables cached;

    private final byte[][] effectRows;
    private final short[][] speffectRows;
    private final short[][] bloodRows;
    private final byte[][] bufBuffRows;
    private final byte[][] bufDebuffRows;
    private final byte[][] bufRowMaps;
    private final short[][] posRows;
    private final short[][][] cposGroups;

    private VqsvBattleAnimationTables(byte[][] effectRows, short[][] speffectRows, short[][] bloodRows,
                                      byte[][] bufBuffRows, byte[][] bufDebuffRows, byte[][] bufRowMaps,
                                      short[][] posRows, short[][][] cposGroups) {
        this.effectRows = effectRows;
        this.speffectRows = speffectRows;
        this.bloodRows = bloodRows;
        this.bufBuffRows = bufBuffRows;
        this.bufDebuffRows = bufDebuffRows;
        this.bufRowMaps = bufRowMaps;
        this.posRows = posRows;
        this.cposGroups = cposGroups;
    }

    static VqsvBattleAnimationTables instance() {
        if (cached == null) {
            cached = load();
        }
        return cached;
    }

    byte[] effectRow(int skillId) {
        if (skillId < 0 || skillId >= effectRows.length || effectRows[skillId] == null) {
            return new byte[0];
        }
        return Arrays.copyOf(effectRows[skillId], effectRows[skillId].length);
    }

    short[] bloodRow(int id) {
        if (id < 0 || id >= bloodRows.length || bloodRows[id] == null) {
            return new short[0];
        }
        return Arrays.copyOf(bloodRows[id], bloodRows[id].length);
    }

    short[] speffectRow(int id) {
        if (id < 0 || id >= speffectRows.length || speffectRows[id] == null) {
            return new short[0];
        }
        return Arrays.copyOf(speffectRows[id], speffectRows[id].length);
    }

    byte[] bufDebufVisualRow(int bank, int effectId) {
        if (bank < 0 || bank >= bufRowMaps.length || effectId < 0 || effectId >= bufRowMaps[bank].length) {
            return new byte[0];
        }
        int rowId = bufRowMaps[bank][effectId];
        byte[][] rows = bank == 0 ? bufBuffRows : bufDebuffRows;
        if (rowId < 0 || rowId >= rows.length || rows[rowId] == null) {
            return new byte[0];
        }
        return Arrays.copyOf(rows[rowId], rows[rowId].length);
    }

    short[] cposRow(int group, int row) {
        if (group < 0 || group >= cposGroups.length || cposGroups[group] == null
                || row < 0 || row >= cposGroups[group].length || cposGroups[group][row] == null) {
            return new short[0];
        }
        return Arrays.copyOf(cposGroups[group][row], cposGroups[group][row].length);
    }

    short[] posRow(int group) {
        if (group < 0 || group >= posRows.length || posRows[group] == null) {
            return new short[0];
        }
        return Arrays.copyOf(posRows[group], posRows[group].length);
    }

    static String sourceSummary(int skillId) {
        VqsvBattleAnimationTables tables = instance();
        return "effectRows=" + tables.effectRows.length
                + " speffectRows=" + tables.speffectRows.length
                + " bloodRows=" + tables.bloodRows.length
                + " bufBuffRows=" + tables.bufBuffRows.length
                + " bufDebuffRows=" + tables.bufDebuffRows.length
                + " skill" + skillId + "=" + Arrays.toString(tables.effectRow(skillId));
    }

    private static VqsvBattleAnimationTables load() {
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            ResourceLocator locator = new ResourceLocator(paths);
            byte[][] effectRows = readByteRows(locator.binary(paths.scriptOriginal("effect.mid")));
            short[][] speffectRows = BinaryTables.readShortRows(locator.binary(paths.scriptOriginal("speffect.mid")));
            short[][] bloodRows = BinaryTables.readShortRows(locator.binary(paths.scriptOriginal("blood.mid")));
            BinaryReader bufReader = locator.binary(paths.scriptOriginal("bufDebuf.mid"));
            byte[][] bufBuffRows = readByteRows(bufReader);
            byte[][] bufDebuffRows = readByteRows(bufReader);
            byte[][] bufRowMaps = readByteRows(bufReader);
            short[][] posRows = BinaryTables.readShortRows(locator.binary(paths.scriptOriginal("pos.mid")));
            BinaryReader cposReader = locator.binary(paths.scriptOriginal("cpos.mid"));
            short[][][] cposGroups = new short[3][][];
            for (int i = 0; i < cposGroups.length; i++) {
                cposGroups[i] = BinaryTables.readShortRows(cposReader);
            }
            return new VqsvBattleAnimationTables(effectRows, speffectRows, bloodRows,
                    bufBuffRows, bufDebuffRows, bufRowMaps, posRows, cposGroups);
        } catch (RuntimeException ex) {
            return new VqsvBattleAnimationTables(new byte[0][], new short[0][], new short[0][],
                    new byte[0][], new byte[0][], new byte[0][], new short[0][], new short[0][][]);
        }
    }

    private static byte[][] readByteRows(BinaryReader reader) {
        int rowCount = reader.readShort();
        if (rowCount < 0) {
            throw new IllegalStateException("Negative byte row count: " + rowCount);
        }
        byte[][] rows = new byte[rowCount][];
        for (int row = 0; row < rowCount; row++) {
            int length = reader.readShort();
            if (length < 0) {
                throw new IllegalStateException("Negative byte row length: " + length);
            }
            rows[row] = reader.readBytes(length);
        }
        return rows;
    }
}
