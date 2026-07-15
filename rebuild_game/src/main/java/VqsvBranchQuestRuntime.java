import java.util.ArrayList;
import java.util.List;

final class VqsvBranchQuestRuntime {
    private final List<SourceBranchTask> tasks = new ArrayList<>();
    private final List<SourceQuestMarker> markers = new ArrayList<>();

    List<SourceBranchTask> tasks() {
        return tasks;
    }

    List<SourceQuestMarker> markers() {
        return markers;
    }

    int size() {
        return tasks.size();
    }

    SourceBranchTask taskAt(int index) {
        return tasks.get(index);
    }

    void clearTasks() {
        tasks.clear();
    }

    void addRawTask(int taskId, int status) {
        tasks.add(new SourceBranchTask(taskId, status));
    }

    void accept(VqsvIntroDemo.Scene s, int taskId) {
        unlockOrUpdate(s, taskId, 1);
        s.sourceStateTrace.add("PORTED source game.e opcode49 accepted branch task"
                + " F[H]=[" + taskId + ",1] H=" + tasks.size());
    }

    void unlockOrUpdate(VqsvIntroDemo.Scene s, int taskId, int status) {
        for (SourceBranchTask task : tasks) {
            if (task.taskId == taskId) {
                task.status = status;
                s.sourceStateTrace.add("PORTED/PARTIAL source game.e.F branch task update"
                        + " taskId=" + taskId + " status=" + status
                        + " H=" + tasks.size());
                return;
            }
        }
        tasks.add(new SourceBranchTask(taskId, status));
        s.sourceStateTrace.add("PORTED/PARTIAL source game.e.F[H] branch task add"
                + " taskId=" + taskId + " status=" + status
                + " H=" + tasks.size());
    }

    void complete(VqsvIntroDemo.Scene s, int taskId) {
        for (SourceBranchTask task : tasks) {
            if (task.taskId == taskId) {
                task.status = 3;
                s.sourceStateTrace.add("PORTED/PARTIAL source game.e.m(" + taskId
                        + ") branch task complete status=3");
                return;
            }
        }
        unlockOrUpdate(s, taskId, 3);
        s.sourceStateTrace.add("PORTED/PARTIAL source game.e.m(" + taskId
                + ") complete added missing branch task");
    }

    int status(int taskId) {
        for (SourceBranchTask task : tasks) {
            if (task.taskId == taskId) {
                return task.status;
            }
        }
        return -1;
    }

    void refreshBqTaskMarkers(VqsvIntroDemo.Scene s) {
        markers.clear();
        if (s.currentSceneId == 2 && s.currentRoomIndex == 1) {
            refreshScene2Room1Markers(s);
            return;
        }
        if (s.currentSceneId != 1 || s.currentRoomIndex != 0) {
            s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask markers"
                    + " skipped scene=" + s.currentSceneId + " room=" + s.currentRoomIndex);
            return;
        }
        int task0Status = status(0);
        if (task0Status == 3) {
            if (s.sourceEventStateComplete(1, 0, 8)) {
                int task1Status = status(1);
                if (task1Status == 1) {
                    s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask row1 accepted"
                            + " clears room0 marker; aq[1]=[2,1,6] scene2 active marker pending");
                    return;
                }
                if (task1Status == 3) {
                    s.sourceStateTrace.add("PENDING source game.e.G bqTask row1 completed"
                            + " next row not ported yet");
                    return;
                }
                markers.add(new SourceQuestMarker(35, 7,
                        "bqTask ap[1]=[1,0,9] opcode43 available branch task1"));
                s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask ap marker"
                        + " taskId=1 actor=35 sprite=259 anim=7"
                        + " after task0 group8 complete");
                return;
            }
            s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask task0 completed"
                    + " marker cleared actor=35");
            return;
        }
        if (task0Status == 1) {
            markers.add(new SourceQuestMarker(35, 15,
                    "bqTask aq[0]=[1,0,8] opcode44 active branch task"));
            s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask aq marker"
                    + " taskId=0 status=1 actor=35 sprite=259 anim=15");
            return;
        }
        if (s.sourceEventStateComplete(1, 0, 6)
                || s.sourceEventStateComplete(1, 0, 2)
                || s.sourceEventStateComplete(1, 0, 3)) {
            markers.add(new SourceQuestMarker(35, 7,
                    "bqTask ap[0]=[1,0,7] opcode43 available branch task"));
            s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask ap marker"
                    + " taskId=0 actor=35 sprite=259 anim=7");
            return;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask no marker"
                + " taskId=0 prereq incomplete");
    }

    private void refreshScene2Room1Markers(VqsvIntroDemo.Scene s) {
        int task1Status = status(1);
        if (task1Status == 1) {
            boolean ready = s.sourcePetRecordObtained(4, 68);
            int anim = ready ? 1 : 15;
            markers.add(new SourceQuestMarker(73, anim,
                    ready
                            ? "bqTask aq[1]=[2,1,6] ready branch task1 objective species68"
                            : "bqTask aq[1]=[2,1,6] active branch task1"));
            s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask aq marker"
                    + " taskId=1 status=1 actor=73 sprite=259 anim=" + anim
                    + " objective category=4 species=68 ready=" + ready);
            return;
        }
        if (task1Status == 3) {
            s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask task1 completed"
                    + " scene2 room1 marker cleared actor=73");
            return;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source game.e.G bqTask scene2 room1"
                + " task1 unavailable status=" + task1Status);
    }
}
