final class VqsvWorldResumeDescriptor {
    static final VqsvWorldResumeDescriptor SCENE1_ROOM1_AFTER_SAVE_TO_OP13 =
            new VqsvWorldResumeDescriptor(
                    "scene1 room1 after save -> op13 Bunny",
                    1,
                    1,
                    1,
                    1,
                    1,
                    "Op13FreeWorldTrigger");

    static final VqsvWorldResumeDescriptor SCENE1_ROOM1_AFTER_BUNNY_TO_ROOM0 =
            new VqsvWorldResumeDescriptor(
                    "scene1 room1 after Bunny group0 -> room0 transition",
                    1,
                    1,
                    1,
                    1,
                    0,
                    "ActorTransitionFreeWorldTrigger");

    static final VqsvWorldResumeDescriptor SCENE1_ROOM0_AFTER_GROUP6_FREEWORLD =
            new VqsvWorldResumeDescriptor(
                    "scene1 room0 after group6 -> post-group6 free-world",
                    1,
                    0,
                    1,
                    0,
                    6,
                    "Room0PostGroup6FreeWorld");

    private final String label;
    private final int sceneId;
    private final int roomIndex;
    private final int completedSceneId;
    private final int completedRoomIndex;
    private final int completedGroupIndex;
    private final String nextBlockingName;

    private VqsvWorldResumeDescriptor(String label,
                                      int sceneId,
                                      int roomIndex,
                                      int completedSceneId,
                                      int completedRoomIndex,
                                      int completedGroupIndex,
                                      String nextBlockingName) {
        this.label = label;
        this.sceneId = sceneId;
        this.roomIndex = roomIndex;
        this.completedSceneId = completedSceneId;
        this.completedRoomIndex = completedRoomIndex;
        this.completedGroupIndex = completedGroupIndex;
        this.nextBlockingName = nextBlockingName;
    }

    Blocking wrap(Blocking next) {
        return new WorldResumeTraceBlocking(this, next);
    }

    private void traceAndAssert(VqsvIntroDemo.Scene s, Blocking next) {
        int eventIndex = s.eventIndex;
        if (s.currentSceneId != sceneId || s.currentRoomIndex != roomIndex) {
            throw new IllegalStateException("World resume descriptor room mismatch for " + label
                    + " expected=[" + sceneId + "," + roomIndex + "]"
                    + " actual=[" + s.currentSceneId + "," + s.currentRoomIndex + "]");
        }
        if (!s.sourceEventStateComplete(completedSceneId, completedRoomIndex, completedGroupIndex)) {
            throw new IllegalStateException("World resume descriptor source event incomplete for " + label
                    + " event=[" + completedSceneId + "," + completedRoomIndex + "," + completedGroupIndex + "]"
                    + " state=" + s.sourceEventState(completedSceneId, completedRoomIndex, completedGroupIndex));
        }
        String actualName = next == null ? "null" : next.getClass().getSimpleName();
        if (!nextBlockingName.equals(actualName)) {
            throw new IllegalStateException("World resume descriptor next blocker mismatch for " + label
                    + " expected=" + nextBlockingName + " actual=" + actualName);
        }
        if (s.eventIndex != eventIndex) {
            throw new IllegalStateException("World resume descriptor mutated eventIndex for " + label
                    + " before=" + eventIndex + " after=" + s.eventIndex);
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source WorldResumeDescriptor " + label
                + " game.i state10->11 game.k.p()->P0"
                + " scene=[" + sceneId + "," + roomIndex + "]"
                + " completed=[" + completedSceneId + "," + completedRoomIndex + "," + completedGroupIndex + "]"
                + " next=" + actualName
                + " trace-only no eventIndex mutation");
    }

    private static final class WorldResumeTraceBlocking implements Blocking {
        private final VqsvWorldResumeDescriptor descriptor;
        private final Blocking next;
        private boolean traced;

        WorldResumeTraceBlocking(VqsvWorldResumeDescriptor descriptor, Blocking next) {
            this.descriptor = descriptor;
            this.next = next;
        }

        @Override
        public boolean tick(VqsvIntroDemo.Scene s) {
            if (!traced) {
                traced = true;
                descriptor.traceAndAssert(s, next);
            }
            return next.tick(s);
        }
    }
}
