import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class VqsvEventState {
    final List<String> trace = new ArrayList<>();
    private final Map<String, Byte> states = new HashMap<>();

    int sourceEventState(int sceneId, int roomIndex, int groupIndex) {
        return states.getOrDefault(key(sceneId, roomIndex, groupIndex), (byte) 0);
    }

    boolean sourceEventStateComplete(int sceneId, int roomIndex, int groupIndex) {
        return sourceEventState(sceneId, roomIndex, groupIndex) == 3;
    }

    boolean op15CheckEventState(int sceneId, int roomIndex, int groupIndex) {
        return sourceEventStateComplete(sceneId, roomIndex, groupIndex);
    }

    boolean op86CheckEventState(int sceneId, int roomIndex, int groupIndex) {
        return sourceEventStateComplete(sceneId, roomIndex, groupIndex);
    }

    Map<String, Byte> snapshotStates() {
        return new HashMap<>(states);
    }

    void restoreStates(Map<String, Byte> snapshot) {
        states.clear();
        if (snapshot != null) {
            states.putAll(snapshot);
        }
        trace.add("PORTED/PARTIAL save restore event states=" + states.size());
    }

    void op23MarkEventComplete(int worldF, int worldG, int eventId) {
        setSourceEventState(worldF, worldG, eventId, 3, "op23");
    }

    void op14CompleteEvent(int sceneId, int roomIndex, int groupIndex) {
        setSourceEventState(sceneId, roomIndex, groupIndex, 3, "op14");
    }

    private void setSourceEventState(int sceneId, int roomIndex, int groupIndex, int state, String reason) {
        states.put(key(sceneId, roomIndex, groupIndex), (byte) state);
        trace.add("PORTED sourceEventState [" + sceneId + "," + roomIndex + "," + groupIndex
                + "]=" + state + " via " + reason);
    }

    private static String key(int sceneId, int roomIndex, int groupIndex) {
        return sceneId + ":" + roomIndex + ":" + groupIndex;
    }
}
