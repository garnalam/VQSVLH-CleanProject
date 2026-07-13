import java.util.Arrays;

final class VqsvBattleEventDescriptor {
    static final VqsvBattleEventDescriptor SCENE1_ROOM1_GROUP0_BUNNY =
            new VqsvBattleEventDescriptor(
                    "scene1 room1 group0 Bunny",
                    1,
                    1,
                    0,
                    -1,
                    new int[]{34, 5, 1},
                    new int[]{0, 1},
                    new int[]{0, 0},
                    new int[]{12, 0, 0},
                    false,
                    false);

    static final VqsvBattleEventDescriptor SCENE1_ROOM0_GROUP6_ELDER =
            new VqsvBattleEventDescriptor(
                    "scene1 room0 group6 Elder",
                    1,
                    0,
                    6,
                    52,
                    new int[]{68, 5, 1},
                    new int[0],
                    new int[]{0, 2},
                    new int[]{10, 10, 0},
                    true,
                    true);

    static final VqsvBattleEventDescriptor SCENE1_ROOM3_GROUP0_SOPHIE =
            new VqsvBattleEventDescriptor(
                    "scene1 room3 group0 Sophie",
                    1,
                    3,
                    0,
                    56,
                    new int[]{5, 20, 4},
                    new int[]{1, 1},
                    new int[]{0, 2},
                    new int[]{78, 78, 0},
                    false,
                    true);

    final String label;
    final int sceneId;
    final int roomIndex;
    final int groupIndex;
    final int op67ActorId;
    final int[] op37Encounter;
    final int[] battleFlags;
    final int[] op32Mode;
    final int[] op47BranchTargets;
    final boolean sourceBattleSlice;
    final boolean npcEnemyEntry;

    private VqsvBattleEventDescriptor(String label, int sceneId, int roomIndex, int groupIndex,
                                      int op67ActorId, int[] op37Encounter, int[] battleFlags,
                                      int[] op32Mode, int[] op47BranchTargets,
                                      boolean sourceBattleSlice, boolean npcEnemyEntry) {
        this.label = label;
        this.sceneId = sceneId;
        this.roomIndex = roomIndex;
        this.groupIndex = groupIndex;
        this.op67ActorId = op67ActorId;
        this.op37Encounter = Arrays.copyOf(op37Encounter, op37Encounter.length);
        this.battleFlags = Arrays.copyOf(battleFlags, battleFlags.length);
        this.op32Mode = Arrays.copyOf(op32Mode, op32Mode.length);
        this.op47BranchTargets = Arrays.copyOf(op47BranchTargets, op47BranchTargets.length);
        this.sourceBattleSlice = sourceBattleSlice;
        this.npcEnemyEntry = npcEnemyEntry;
    }

    SourceBattleRuntime runtime(VqsvIntroDemo.Scene s, int forcedResultIndex) {
        applyEventTrace(s, forcedResultIndex);
        return new SourceBattleRuntime(
                sourceActorId(),
                Arrays.copyOf(op37Encounter, op37Encounter.length),
                Arrays.copyOf(battleFlags, battleFlags.length),
                Arrays.copyOf(op32Mode, op32Mode.length),
                Arrays.copyOf(op47BranchTargets, op47BranchTargets.length),
                forcedResultIndex,
                sourceBattleSlice,
                npcEnemyEntry,
                label);
    }

    int sourceActorId() {
        return op67ActorId >= 0 ? op67ActorId : op37Encounter[0];
    }

    int op32Mode(int index) {
        return index >= 0 && index < op32Mode.length ? op32Mode[index] : 0;
    }

    void consumeOp47(VqsvIntroDemo.Scene s) {
        int result = s.battleResultIndex;
        if (result == -1) {
            if (s.battleBranchTarget != -1) {
                throw new IllegalStateException("Source op47 skip expected branch -1 for " + label
                        + " actual=" + s.battleBranchTarget);
            }
            s.sourceStateTrace.add("PORTED/PARTIAL source BattleEventDescriptor " + label
                    + " op47 skip result=-1 branch=-1 no eventIndex mutation");
            return;
        }
        if (result < 0 || result >= op47BranchTargets.length) {
            throw new IllegalStateException("Source op47 result out of range for " + label
                    + " result=" + result
                    + " targets=" + Arrays.toString(op47BranchTargets));
        }
        int rawTarget = op47BranchTargets[result];
        int sourceCursor = rawTarget - 2;
        if (s.battleBranchTarget != rawTarget) {
            throw new IllegalStateException("Source op47 raw target mismatch for " + label
                    + " result=" + result
                    + " rawTarget=" + rawTarget
                    + " battleBranchTarget=" + s.battleBranchTarget);
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source BattleEventDescriptor " + label
                + " op47 result=" + result
                + " rawTarget=" + rawTarget
                + " sourceCursor=" + sourceCursor
                + " branchTarget=" + s.battleBranchTarget
                + " no eventIndex mutation");
    }

    private void applyEventTrace(VqsvIntroDemo.Scene s, int forcedResultIndex) {
        if (op67ActorId >= 0) {
            s.worldEventActor = op67ActorId;
            s.battleEventActor = op67ActorId;
            s.sourceStateTrace.add("PORTED source BattleEventDescriptor " + label
                    + " op67 game.k.v=" + op67ActorId);
        }
        s.sourceStateTrace.add("PORTED source BattleEventDescriptor " + label
                + " op37 encounter=" + Arrays.toString(op37Encounter)
                + " game.d.a(int[][])");
        s.sourceStateTrace.add("PORTED/PARTIAL source BattleEventDescriptor " + label
                + " op32 mode=" + Arrays.toString(op32Mode)
                + " source game.d.a/b/c then game.i state12");
        s.sourceStateTrace.add("PORTED/PARTIAL source BattleEventDescriptor " + label
                + " op47 targets=" + Arrays.toString(op47BranchTargets)
                + " forcedResult=" + forcedResultIndex
                + " bridge=runtime battleResultIndex->battleBranchTarget");
        s.sourceStateTrace.add((npcEnemyEntry ? "PORTED/PARTIAL" : "PORTED")
                + " source BattleEventDescriptor " + label
                + " npcEnemyEntry=" + npcEnemyEntry
                + " source gate=op67 NPC/enemy actor route; Bunny wild battle remains false");
    }
}
