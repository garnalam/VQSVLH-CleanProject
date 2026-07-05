final class VqsvFreeWorldRuntime {
    private VqsvFreeWorldRuntime() {
    }

    static void prepareTransition(VqsvIntroDemo.Scene s, int centerX, int centerY, int width, int height) {
        prepareTransition(s, centerX, centerY, width, height, s.transitionDirection);
    }

    static void prepareTransition(VqsvIntroDemo.Scene s, int centerX, int centerY,
                                  int width, int height, int direction) {
        s.transitionCenterX = centerX;
        s.transitionCenterY = centerY;
        s.transitionWidth = width;
        s.transitionHeight = height;
        s.transitionDirection = direction;
    }

    static void markWorldTransition(VqsvIntroDemo.Scene s, int worldF, int worldG, int actorIndex) {
        s.nextWorldF = worldF;
        s.nextWorldG = worldG;
        s.nextWorldActor = actorIndex;
    }

    static int sourceTransitionRequiredDirection(int c) {
        int[] map = {2, 3, 0, 1};
        return c >= 0 && c < map.length ? map[c] : -1;
    }

    static boolean trySourceTransition(VqsvIntroDemo.Scene s, int actorId, int sourceC,
                                       int targetSceneId, int targetRoomIndex, int targetActorId) {
        int requiredDirection = sourceTransitionRequiredDirection(sourceC);
        if (s.player.direction != requiredDirection || !playerIntersectsActorSourceMask(s, actorId, true)) {
            return false;
        }
        stopPlayerForSourceEvent(s);
        s.sourceStateTrace.add("PORTED/APPROX type1 transition trigger actor=" + actorId
                + " sourceC=" + sourceC
                + " requiredDir=" + requiredDirection
                + " from=[" + s.currentSceneId + "," + s.currentRoomIndex + "]"
                + " target=[" + targetSceneId + "," + targetRoomIndex + "," + targetActorId + "]");
        markWorldTransition(s, targetSceneId, targetRoomIndex, targetActorId);
        if (!loadImplementedTransitionTarget(s, targetSceneId, targetRoomIndex, targetActorId)) {
            s.sourceStateTrace.add("PENDING type1 transition target not implemented ["
                    + targetSceneId + "," + targetRoomIndex + "," + targetActorId + "]");
        }
        return true;
    }

    private static boolean loadImplementedTransitionTarget(VqsvIntroDemo.Scene s,
                                                           int targetSceneId, int targetRoomIndex,
                                                           int targetActorId) {
        if (targetSceneId == 1 && targetRoomIndex == 0) {
            s.loadScene1Room0(s.player.x, s.player.y);
            placePlayerAtTransitionActorApprox(s, targetActorId, 16);
            s.sourceStateTrace.add("PORTED/APPROX loaded scene=1 room=0 targetActor="
                    + targetActorId + " player=[" + s.player.x + "," + s.player.y + "]");
            return true;
        }
        if (targetSceneId == 1 && targetRoomIndex == 1) {
            s.loadScene1Room1(s.player.x, s.player.y);
            placePlayerAtTransitionActorApprox(s, targetActorId, 16);
            s.sourceStateTrace.add("PORTED/APPROX loaded scene=1 room=1 targetActor="
                    + targetActorId + " player=[" + s.player.x + "," + s.player.y + "]");
            return true;
        }
        if (targetSceneId == 1 && targetRoomIndex == 2) {
            s.loadScene1Room2(s.player.x, s.player.y);
            placePlayerAtTransitionActorApprox(s, targetActorId, 16);
            s.sourceStateTrace.add("PORTED/APPROX loaded scene=1 room=2 targetActor="
                    + targetActorId + " player=[" + s.player.x + "," + s.player.y + "]");
            return true;
        }
        return false;
    }

    static void setPlayerPositionApprox(VqsvIntroDemo.Scene s, int x, int y) {
        s.playerX = x;
        s.playerY = y;
        s.player.x = x;
        s.player.y = y;
        s.player.direction = s.transitionDirection;
        s.player.applyMode(0);
        s.player.visible = true;
        s.setCameraCenter(x, y);
    }

    static void placePlayerAtTransitionActorApprox(VqsvIntroDemo.Scene s, int actorId, int tileSize) {
        if (actorId < 0 || actorId >= s.actors.length || s.actors[actorId] == null) {
            return;
        }
        Actor actor = s.actors[actorId];
        s.playerX = actor.x - Math.floorMod(actor.x, tileSize);
        s.playerY = actor.y - Math.floorMod(actor.y, tileSize);
        s.player.x = s.playerX;
        s.player.y = s.playerY;
        s.player.direction = actor.direction;
        s.player.applyMode(0);
        s.player.visible = true;
        s.setCameraCenter(s.playerX, s.playerY);
    }

    static void tickFreeWorldPlayer(VqsvIntroDemo.Scene s) {
        int dir = heldDirection(s);
        if (dir < 0) {
            s.player.applyMode(0);
            s.setCameraCenter(s.player.x, s.player.y);
            return;
        }
        s.player.direction = dir;
        s.player.applyMode(3);
        if (canMovePlayer(s, dir, 4)) {
            s.player.step(4);
            s.playerX = s.player.x;
            s.playerY = s.player.y;
        }
        s.setCameraCenter(s.player.x, s.player.y);
    }

    private static int heldDirection(VqsvIntroDemo.Scene s) {
        if (s.keyUp && !s.keyDown) {
            return 2;
        }
        if (s.keyDown && !s.keyUp) {
            return 0;
        }
        if (s.keyRight && !s.keyLeft) {
            return 1;
        }
        if (s.keyLeft && !s.keyRight) {
            return 3;
        }
        return -1;
    }

    private static boolean canMovePlayer(VqsvIntroDemo.Scene s, int dir, int speed) {
        int nx = s.player.x;
        int ny = s.player.y;
        int amount = Math.max(1, Math.abs(speed));
        switch (dir) {
            case 0:
                ny += amount;
                break;
            case 1:
                nx += amount;
                break;
            case 2:
                ny -= amount;
                break;
            case 3:
                nx -= amount;
                break;
            default:
                break;
        }
        if (s.mapRenderer == null) {
            return true;
        }
        return nx - 8 >= 0 && nx + 8 <= s.mapRenderer.mapWidthPixels()
                && ny - 8 >= 0 && ny + 8 <= s.mapRenderer.mapHeightPixels();
    }

    static boolean playerIntersectsSourceRect(VqsvIntroDemo.Scene s, int x, int y, int w, int h) {
        // Source op13 calls ae.a(rectX, rectY, rectW, rectH, player.i, player.j, player.a.k()).
        return x + w >= s.player.x - 8
                && x <= s.player.x - 8 + 16
                && y <= s.player.y - 8 + 16
                && y + h >= s.player.y - 8;
    }

    static boolean playerIntersectsActorSourceMask(VqsvIntroDemo.Scene s, int actorId, boolean actorHitMask) {
        if (actorId < 0 || actorId >= s.actors.length || s.actors[actorId] == null) {
            return false;
        }
        Actor actor = s.actors[actorId];
        if (!actor.visible) {
            return false;
        }
        short[] playerMask = s.player.collisionMask();
        short[] actorMask = actorHitMask ? actor.hitMask() : actor.collisionMask();
        if (playerMask != null && actorMask != null) {
            return sourceMaskOverlap(s.player.x, s.player.y, playerMask, actor.x, actor.y, actorMask);
        }
        return actor.x + 12 >= s.player.x - 8
                && actor.x - 12 <= s.player.x + 8
                && actor.y + 16 >= s.player.y - 8
                && actor.y - 16 <= s.player.y + 8;
    }

    static boolean playerInteractsActorSourceMask(VqsvIntroDemo.Scene s, int actorId) {
        if (actorId < 0 || actorId >= s.actors.length || s.actors[actorId] == null) {
            return false;
        }
        Actor actor = s.actors[actorId];
        if (!actor.visible) {
            return false;
        }
        int px = s.player.x;
        int py = s.player.y;
        int offset = 4;
        switch (s.player.direction) {
            case 0:
                py += offset;
                break;
            case 1:
                px += offset;
                break;
            case 2:
                py -= offset;
                break;
            case 3:
                px -= offset;
                break;
            default:
                break;
        }
        short[] playerMask = s.player.collisionMask();
        short[] actorMask = actor.collisionMask();
        if (playerMask != null && actorMask != null) {
            return sourceMaskOverlap(px, py, playerMask, actor.x, actor.y, actorMask);
        }
        return actor.x + 12 >= px - 8
                && actor.x - 12 <= px + 8
                && actor.y + 16 >= py - 8
                && actor.y - 16 <= py + 8;
    }

    private static boolean sourceMaskOverlap(int ax, int ay, short[] aMask, int bx, int by, short[] bMask) {
        if (aMask.length < 4 || bMask.length < 4) {
            return false;
        }
        return ax + aMask[0] + aMask[2] >= bx + bMask[0]
                && ax + aMask[0] <= bx + bMask[0] + bMask[2]
                && ay + aMask[1] <= by + bMask[1] + bMask[3]
                && ay + aMask[1] + aMask[3] >= by + bMask[1];
    }

    static void stopPlayerForSourceEvent(VqsvIntroDemo.Scene s) {
        s.player.applyMode(0);
        s.setCameraCenter(s.player.x, s.player.y);
    }
}
