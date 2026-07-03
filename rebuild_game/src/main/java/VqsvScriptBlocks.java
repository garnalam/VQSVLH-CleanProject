import java.util.Arrays;

interface Event {
    Blocking start(VqsvIntroDemo.Scene s);
}

interface Blocking {
    boolean tick(VqsvIntroDemo.Scene s);
}

final class Delay implements Blocking {
    private int left;

    Delay(int left) {
        this.left = left;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        return left-- <= 0;
    }
}

final class Opcode34Counter implements Blocking {
    private int n;
    private final int step;
    private int left;
    private boolean started;

    Opcode34Counter(int n, int step, int left) {
        this.n = n;
        this.step = step;
        this.left = left;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            return false;
        }
        left--;
        n -= step;
        if (left > 0) {
            return false;
        }
        left = 0;
        return true;
    }
}

final class Move implements Blocking {
    private final int[] ids, dx, dy, tx, ty;

    Move(int[] ids, int[] dx, int[] dy, int[] tx, int[] ty) {
        this.ids = ids;
        this.dx = dx;
        this.dy = dy;
        this.tx = tx;
        this.ty = ty;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        boolean done = true;
        for (int i = 0; i < ids.length; i++) {
            if (tx[i] > 0 || ty[i] > 0) {
                done = false;
                tx[i]--;
                ty[i]--;
                Actor a = s.actors[ids[i]];
                a.x += dx[i];
                a.y += dy[i];
            }
        }
        return done;
    }
}

final class ActionSet implements Blocking {
    private final int[] ids;
    private final int[] modes;
    private final int[] dirs;
    private final int[] waited;
    private final boolean[] done;
    private boolean started;

    ActionSet(int[] ids, int[] modes, int[] dirs) {
        this.ids = ids;
        this.modes = modes;
        this.dirs = dirs;
        this.waited = new int[ids.length];
        this.done = new boolean[ids.length];
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            for (int i = 0; i < ids.length; i++) {
                Actor actor = s.actors[ids[i]];
                if (actor != null) {
                    actor.direction = dirs[i];
                    actor.applyMode(modes[i]);
                }
            }
            return false;
        }
        boolean allDone = true;
        for (int i = 0; i < ids.length; i++) {
            if (done[i]) {
                continue;
            }
            Actor actor = s.actors[ids[i]];
            waited[i]++;
            if (actor == null || actor.consumeCycleComplete() || waited[i] > 45) {
                if (actor != null) {
                    actor.applyMode(0);
                }
                done[i] = true;
            } else {
                allDone = false;
            }
        }
        return allDone;
    }
}

final class TimedAction implements Blocking {
    private final int[] ids;
    private final int[] dirs;
    private final int[] speeds;
    private final int[] durations;
    private final int[] remaining;
    private boolean started;
    private int left;

    TimedAction(int[] ids, int[] dirs, int[] speeds, int[] durations) {
        this.ids = ids;
        this.dirs = dirs;
        this.speeds = speeds;
        this.durations = durations;
        this.remaining = Arrays.copyOf(durations, durations.length);
        for (int duration : durations) {
            left = Math.max(left, duration);
        }
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            for (int i = 0; i < ids.length; i++) {
                Actor actor = s.actors[ids[i]];
                if (actor != null) {
                    actor.direction = dirs[i];
                    actor.applyMode(3);
                }
            }
            return false;
        }
        boolean allDone = true;
        for (int i = 0; i < ids.length; i++) {
            if (remaining[i] <= 0) {
                continue;
            }
            Actor actor = s.actors[ids[i]];
            if (actor != null) {
                actor.step(speeds[i]);
            }
            remaining[i]--;
            if (remaining[i] > 0) {
                allDone = false;
            }
        }
        if (!allDone) {
            return false;
        }
        for (int id : ids) {
            Actor actor = s.actors[id];
            if (actor != null) {
                actor.applyMode(0);
            }
        }
        return true;
    }
}

final class Op10PlayerTimedAction implements Blocking {
    private final int dir;
    private final int speed;
    private int remaining;
    private boolean started;

    Op10PlayerTimedAction(int dir, int speed, int duration) {
        this.dir = dir;
        this.speed = speed;
        this.remaining = duration;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            s.player.direction = dir;
            s.player.applyMode(3);
            s.player.visible = true;
            s.playerX = s.player.x;
            s.playerY = s.player.y;
            s.setCameraCenter(s.player.x, s.player.y);
            return false;
        }
        if (remaining <= 0) {
            stop(s);
            return true;
        }
        s.player.direction = dir;
        s.player.step(speed);
        s.playerX = s.player.x;
        s.playerY = s.player.y;
        s.setCameraCenter(s.player.x, s.player.y);
        remaining--;
        if (remaining > 0) {
            return false;
        }
        stop(s);
        return true;
    }

    private void stop(VqsvIntroDemo.Scene s) {
        s.player.direction = dir;
        s.player.applyMode(0);
        s.playerX = s.player.x;
        s.playerY = s.player.y;
        s.setCameraCenter(s.player.x, s.player.y);
    }
}

final class Path implements Blocking {
    private final int[] ids;
    private final int[][] xs;
    private final int[][] ys;
    private int step;

    Path(int[] ids, int[][] xs, int[][] ys) {
        this.ids = ids;
        this.xs = xs;
        this.ys = ys;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (step >= xs[0].length) {
            return true;
        }
        for (int i = 0; i < ids.length; i++) {
            Actor a = s.actors[ids[i]];
            a.x = xs[i][step];
            a.y = ys[i][step];
        }
        step++;
        return false;
    }
}

final class CameraPan implements Blocking {
    private final int actorId;
    private final int speed;

    CameraPan(int actorId, int speed) {
        this.actorId = actorId;
        this.speed = speed;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        Actor actor = s.actors[actorId];
        s.moveCameraToward(actor.x, actor.y, speed);
        return speed <= 0 || s.cameraCenteredOn(actor.x, actor.y);
    }
}

final class CameraPanPoint implements Blocking {
    private final int x;
    private final int y;
    private final int speed;

    CameraPanPoint(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        s.moveCameraToward(x, y, speed);
        return speed <= 0 || s.cameraCenteredOn(x, y);
    }
}

final class Op13FreeWorldTrigger implements Blocking {
    private final int sceneId;
    private final int roomIndex;
    private final int groupIndex;
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private boolean started;

    Op13FreeWorldTrigger(int sceneId, int roomIndex, int groupIndex, int x, int y, int w, int h) {
        this.sceneId = sceneId;
        this.roomIndex = roomIndex;
        this.groupIndex = groupIndex;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            s.sourceStateTrace.add("PORTED/APPROX op13 wait scene=" + sceneId
                    + " room=" + roomIndex + " group=" + groupIndex
                    + " rect=[" + x + "," + y + "," + w + "," + h + "]");
        }
        if (s.playerIntersectsSourceRect(x, y, w, h)) {
            s.stopPlayerForSourceEvent();
            s.sourceStateTrace.add("PORTED/APPROX op13 trigger scene=" + sceneId
                    + " room=" + roomIndex + " group=" + groupIndex
                    + " player=[" + s.player.x + "," + s.player.y + "]");
            return true;
        }
        s.tickFreeWorldPlayer();
        if (s.playerIntersectsSourceRect(x, y, w, h)) {
            s.stopPlayerForSourceEvent();
            s.sourceStateTrace.add("PORTED/APPROX op13 trigger scene=" + sceneId
                    + " room=" + roomIndex + " group=" + groupIndex
                    + " player=[" + s.player.x + "," + s.player.y + "]");
            return true;
        }
        return false;
    }
}

final class ActorTransitionFreeWorldTrigger implements Blocking {
    private final int sceneId;
    private final int roomIndex;
    private final int actorId;
    private final int requiredDirection;
    private final int targetSceneId;
    private final int targetRoomIndex;
    private final int targetActorId;
    private boolean started;

    ActorTransitionFreeWorldTrigger(int sceneId, int roomIndex, int actorId,
                                    int requiredDirection, int targetSceneId,
                                    int targetRoomIndex, int targetActorId) {
        this.sceneId = sceneId;
        this.roomIndex = roomIndex;
        this.actorId = actorId;
        this.requiredDirection = requiredDirection;
        this.targetSceneId = targetSceneId;
        this.targetRoomIndex = targetRoomIndex;
        this.targetActorId = targetActorId;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            s.sourceStateTrace.add("PORTED/APPROX type1 transition wait scene=" + sceneId
                    + " room=" + roomIndex
                    + " actor=" + actorId
                    + " requiredDir=" + requiredDirection
                    + " target=[" + targetSceneId + "," + targetRoomIndex + "," + targetActorId + "]");
        }
        if (canTrigger(s)) {
            trigger(s);
            return true;
        }
        s.tickFreeWorldPlayer();
        if (canTrigger(s)) {
            trigger(s);
            return true;
        }
        return false;
    }

    private boolean canTrigger(VqsvIntroDemo.Scene s) {
        return s.player.direction == requiredDirection && s.playerIntersectsActorSourceMask(actorId, true);
    }

    private void trigger(VqsvIntroDemo.Scene s) {
        s.trySourceTransition(actorId, sourceCFromRequiredDirection(requiredDirection),
                targetSceneId, targetRoomIndex, targetActorId);
    }

    private static int sourceCFromRequiredDirection(int requiredDirection) {
        for (int c = 0; c < 4; c++) {
            if (VqsvIntroDemo.Scene.sourceTransitionRequiredDirection(c) == requiredDirection) {
                return c;
            }
        }
        return -1;
    }
}

final class ActorInteractionFreeWorldTrigger implements Blocking {
    private final int sceneId;
    private final int roomIndex;
    private final int groupIndex;
    private final int gateSceneId;
    private final int gateRoomIndex;
    private final int gateGroupIndex;
    private final int actorId;
    private boolean started;

    ActorInteractionFreeWorldTrigger(int sceneId, int roomIndex, int groupIndex,
                                     int gateSceneId, int gateRoomIndex, int gateGroupIndex,
                                     int actorId) {
        this.sceneId = sceneId;
        this.roomIndex = roomIndex;
        this.groupIndex = groupIndex;
        this.gateSceneId = gateSceneId;
        this.gateRoomIndex = gateRoomIndex;
        this.gateGroupIndex = gateGroupIndex;
        this.actorId = actorId;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            s.sourceStateTrace.add("PORTED op86 gate scene=" + sceneId
                    + " room=" + roomIndex
                    + " group=" + groupIndex
                    + " requires [" + gateSceneId + "," + gateRoomIndex + "," + gateGroupIndex + "]="
                    + s.sourceEventState(gateSceneId, gateRoomIndex, gateGroupIndex));
            s.sourceStateTrace.add("PORTED op16 wait actor=" + actorId
                    + " game.k.u emulated by key0 + source-mask interaction");
        }
        if (!s.op86CheckEventState(gateSceneId, gateRoomIndex, gateGroupIndex)) {
            s.tickFreeWorldPlayer();
            return false;
        }
        if (s.key0 && s.playerInteractsActorSourceMask(actorId)) {
            s.stopPlayerForSourceEvent();
            s.worldEventActor = actorId;
            s.sourceStateTrace.add("PORTED op16 trigger actor=" + actorId
                    + " player=[" + s.player.x + "," + s.player.y + "]"
                    + " dir=" + s.player.direction);
            return true;
        }
        s.tickFreeWorldPlayer();
        return false;
    }
}
