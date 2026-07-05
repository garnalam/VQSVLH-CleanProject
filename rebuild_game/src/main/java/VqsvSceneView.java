import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;

final class VqsvSceneView {
    private VqsvSceneView() {
    }

    static void render(VqsvIntroDemo.Scene s, Graphics2D g) {
        g.setColor(s.useMap ? Color.BLACK : new Color(8, 16, 80));
        g.fillRect(0, 0, VqsvIntroDemo.W, VqsvIntroDemo.H);
        if (s.useMap) {
            renderMapLayer(s, g, 1);
            renderMapLayer(s, g, 2);
        }

        renderActorLayer(s, g, 2, false);
        renderActorLayer(s, g, 1, true);
        renderPlayer(s, g);
        for (TempSprite sprite : s.tempSprites) {
            sprite.render(g, s);
        }
        if (s.useMap) {
            renderMapLayer(s, g, 3);
        }
        renderActorLayer(s, g, 0, false);

        s.effect.renderParticles(g);
        s.effect.renderOverlay(g);
        VqsvBattleRenderer.render(s, g);
        s.worldUi.render(g, s.useMap);
        if (s.text != null) {
            s.text.render(g, s.font);
        }
        if (s.choice != null) {
            s.choice.render(g, s.font);
        }
    }

    static void setCameraCenter(VqsvIntroDemo.Scene s, int cx, int cy) {
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.centerCameraOn(cx, cy);
            s.cameraX = s.mapRenderer.cameraX();
            s.cameraY = s.mapRenderer.cameraY();
        } else {
            s.cameraX = clamp(cx - VqsvIntroDemo.W / 2, 0, 640 - VqsvIntroDemo.W);
            s.cameraY = clamp(cy - VqsvIntroDemo.H / 2, 0, 480 - VqsvIntroDemo.H);
        }
    }

    static void moveCameraToward(VqsvIntroDemo.Scene s, int cx, int cy, int speed) {
        int targetX;
        int targetY;
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.centerCameraOn(cx, cy);
            targetX = s.mapRenderer.cameraX();
            targetY = s.mapRenderer.cameraY();
        } else {
            targetX = clamp(cx - VqsvIntroDemo.W / 2, 0, 640 - VqsvIntroDemo.W);
            targetY = clamp(cy - VqsvIntroDemo.H / 2, 0, 480 - VqsvIntroDemo.H);
        }
        if (speed <= 0) {
            s.cameraX = targetX;
            s.cameraY = targetY;
        } else {
            int dx = targetX - s.cameraX;
            int dy = targetY - s.cameraY;
            int distance = (int) Math.sqrt(dx * dx + dy * dy);
            if (distance <= speed) {
                s.cameraX = targetX;
                s.cameraY = targetY;
            } else {
                s.cameraX += dx * speed / distance;
                s.cameraY += dy * speed / distance;
            }
        }
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.setCamera(s.cameraX, s.cameraY);
            s.cameraX = s.mapRenderer.cameraX();
            s.cameraY = s.mapRenderer.cameraY();
        }
    }

    static boolean cameraCenteredOn(VqsvIntroDemo.Scene s, int cx, int cy) {
        int oldX = s.cameraX;
        int oldY = s.cameraY;
        if (s.useMap && s.mapRenderer != null) {
            s.mapRenderer.centerCameraOn(cx, cy);
            boolean same = oldX == s.mapRenderer.cameraX() && oldY == s.mapRenderer.cameraY();
            s.mapRenderer.setCamera(oldX, oldY);
            return same;
        }
        return oldX == clamp(cx - VqsvIntroDemo.W / 2, 0, 640 - VqsvIntroDemo.W)
                && oldY == clamp(cy - VqsvIntroDemo.H / 2, 0, 480 - VqsvIntroDemo.H);
    }

    static void followActor(VqsvIntroDemo.Scene s, int actorId) {
        s.followActorId = actorId;
        updateCameraFollow(s);
    }

    static void stopCameraFollow(VqsvIntroDemo.Scene s) {
        s.followActorId = -1;
    }

    static void updateCameraFollow(VqsvIntroDemo.Scene s) {
        if (s.followActorId < 0 || s.followActorId >= s.actors.length || s.actors[s.followActorId] == null) {
            return;
        }
        Actor actor = s.actors[s.followActorId];
        setCameraCenter(s, actor.x, actor.y);
    }

    private static void renderMapLayer(VqsvIntroDemo.Scene s, Graphics2D g, int layerIndex) {
        if (s.useMap && s.mapRenderer != null && s.mapRenderer.hasLayer(layerIndex)) {
            s.mapRenderer.renderLayer(g, layerIndex);
        }
    }

    private static void renderActorLayer(VqsvIntroDemo.Scene s, Graphics2D g, int layer, boolean sortByY) {
        ArrayList<Actor> draw = new ArrayList<>();
        for (Actor a : s.actors) {
            if (a != null && a.visible && a.layer == layer) {
                draw.add(a);
            }
        }
        if (sortByY) {
            draw.sort(Comparator.comparingInt(a -> a.y));
        }
        for (Actor a : draw) {
            a.render(g, s.cameraX, s.cameraY);
        }
    }

    private static void renderPlayer(VqsvIntroDemo.Scene s, Graphics2D g) {
        if (s.useMap && s.player.visible) {
            s.player.render(g, s.cameraX, s.cameraY);
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
