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

    private VqsvBattleAnimationTables(byte[][] effectRows, short[][] speffectRows, short[][] bloodRows) {
        this.effectRows = effectRows;
        this.speffectRows = speffectRows;
        this.bloodRows = bloodRows;
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

    static String sourceSummary(int skillId) {
        VqsvBattleAnimationTables tables = instance();
        return "effectRows=" + tables.effectRows.length
                + " speffectRows=" + tables.speffectRows.length
                + " bloodRows=" + tables.bloodRows.length
                + " skill" + skillId + "=" + Arrays.toString(tables.effectRow(skillId));
    }

    private static VqsvBattleAnimationTables load() {
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            ResourceLocator locator = new ResourceLocator(paths);
            byte[][] effectRows = readByteRows(locator.binary(paths.scriptOriginal("effect.mid")));
            short[][] speffectRows = BinaryTables.readShortRows(locator.binary(paths.scriptOriginal("speffect.mid")));
            short[][] bloodRows = BinaryTables.readShortRows(locator.binary(paths.scriptOriginal("blood.mid")));
            return new VqsvBattleAnimationTables(effectRows, speffectRows, bloodRows);
        } catch (RuntimeException ex) {
            return new VqsvBattleAnimationTables(new byte[0][], new short[0][], new short[0][]);
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
