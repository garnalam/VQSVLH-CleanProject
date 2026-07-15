import java.awt.Graphics2D;
final class Actor {
    final SpriteAnim anim;
    final int spriteIndex;
    final int variant;
    final int layer;
    int x, y;
    int direction;
    boolean visible;
    boolean cycleComplete;
    int appliedMode = Integer.MIN_VALUE;
    int appliedDirection = Integer.MIN_VALUE;

    Actor(int id, int spriteIndex, int state, int x, int y) {
        this(id, spriteIndex, state, x, y, 0, 1);
    }

    Actor(int id, int spriteIndex, int state, int x, int y, int variant) {
        this(id, spriteIndex, state, x, y, variant, 1);
    }

    Actor(int id, int spriteIndex, int state, int x, int y, int variant, int layer) {
        this.spriteIndex = spriteIndex;
        this.anim = SpriteAnim.load(spriteIndex);
        this.variant = variant;
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.direction = state;
        if (variant == 1 || variant == 18) {
            applyMode(0);
        } else {
            this.anim.setState(state);
        }
    }

    void tick() {
        if (visible) {
            if (anim.tick()) {
                cycleComplete = true;
            }
        }
    }

    void setState(int state) {
        anim.setState(state);
    }

    void applyMode(int mode) {
        if (appliedMode == mode && appliedDirection == direction) {
            return;
        }
        appliedMode = mode;
        appliedDirection = direction;
        cycleComplete = false;
        if (variant == 1 || variant == 18) {
            int h = mode / 3;
            if (h == 0) {
                setState(direction == 3 ? 1 : direction);
            } else if (h == 1) {
                setState(h * 3 + (direction == 3 ? 1 : direction));
            }
        } else {
            setState(mode);
        }
    }

    boolean consumeCycleComplete() {
        boolean done = cycleComplete;
        cycleComplete = false;
        return done;
    }

    void step(int speed) {
        int amount = Math.max(1, Math.abs(speed));
        switch (direction) {
            case 0:
                y += amount;
                break;
            case 1:
                x += amount;
                break;
            case 2:
                y -= amount;
                break;
            case 3:
                x -= amount;
                break;
            default:
                break;
        }
    }

    void render(Graphics2D g, int camX, int camY) {
        anim.draw(g, x - camX, y - camY, direction == 3 ? 1 : 0);
    }

    short[] collisionMask() {
        return anim.currentCollisionMask();
    }

    short[] hitMask() {
        return anim.currentHitMask();
    }
}


final class TempSprite {
    final int actorId;
    final SpriteAnim anim = SpriteAnim.load(259);
    final boolean fixedPosition;
    final int x;
    final int y;
    int left;

    TempSprite(int actorId, int animation, int duration) {
        this.actorId = actorId;
        this.fixedPosition = false;
        this.x = 0;
        this.y = 0;
        this.left = duration;
        anim.setState(animation);
    }

    TempSprite(int x, int y, int animation, int duration) {
        this.actorId = -2;
        this.fixedPosition = true;
        this.x = x;
        this.y = y;
        this.left = duration;
        anim.setState(animation);
    }

    boolean tick(VqsvIntroDemo.Scene scene) {
        boolean cycleDone = anim.tick();
        return cycleDone || left-- <= 0
                || !fixedPosition && actorId >= 0
                && (actorId >= scene.actors.length || scene.actors[actorId] == null);
    }

    void render(Graphics2D g, VqsvIntroDemo.Scene scene) {
        if (fixedPosition) {
            anim.draw(g, x - scene.cameraX, y - scene.cameraY, 0);
            return;
        }
        Actor actor = actorId == -1 ? scene.player : scene.actors[actorId];
        if (actor != null && actor.visible) {
            anim.draw(g, actor.x - scene.cameraX, actor.y - scene.cameraY - 24, 0);
        }
    }
}

final class WorldUi {
    static final int BUTTON_NONE = 0;
    static final int BUTTON_SYSTEM = 1;
    static final int BUTTON_MENU = 2;
    final SpriteAnim ui = SpriteAnim.load(257);
    boolean visible;

    void render(Graphics2D g, boolean worldVisible) {
        if (!visible || !worldVisible) {
            return;
        }
        VqsvUiLayout layout = VqsvUiLayout.load("world.ui");
        drawWidgetCell(g, layout, 7, 167, 1, 303);
        drawWidgetCell(g, layout, 5, 68, 222, 303);
    }

    void drawCellTopLeft(Graphics2D g, int cellId, int x, int y) {
        int[] bounds = ui.cellBounds(cellId);
        if (bounds == null) {
            return;
        }
        ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }

    private void drawWidgetCell(Graphics2D g, VqsvUiLayout layout, int widgetId,
                                int fallbackCell, int fallbackX, int fallbackY) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int cell = widget != null && widget.altId >= 0 ? widget.altId : fallbackCell;
        int x = widget == null ? fallbackX : widget.x;
        int y = widget == null ? fallbackY : widget.y;
        drawCellTopLeft(g, cell, x, y);
    }

    int buttonAt(int x, int y) {
        if (!visible) {
            return BUTTON_NONE;
        }
        VqsvUiLayout layout = VqsvUiLayout.load("world.ui");
        if (widgetHit(layout, 7, x, y, 1, 303, 18, 17)
                || leftSoftkeyHit(x, y)) {
            return BUTTON_SYSTEM;
        }
        if (widgetHit(layout, 5, x, y, 222, 303, 16, 17)
                || rightSoftkeyHit(x, y)) {
            return BUTTON_MENU;
        }
        return BUTTON_NONE;
    }

    private static boolean leftSoftkeyHit(int x, int y) {
        return x <= 48 && y >= 288;
    }

    private static boolean rightSoftkeyHit(int x, int y) {
        return x >= 188 && y >= 288;
    }

    private static boolean widgetHit(VqsvUiLayout layout, int widgetId, int x, int y,
                                     int fallbackX, int fallbackY, int fallbackW, int fallbackH) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int left = widget == null ? fallbackX : widget.x;
        int top = widget == null ? fallbackY : widget.y;
        int width = widget == null ? fallbackW : Math.max(fallbackW, widget.w);
        int height = widget == null ? fallbackH : Math.max(fallbackH, widget.h);
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
}

final class ChoiceBox {
    static final int OPTION_FRAME_CELL = 155;
    static final int OPTION_MARKER_CELL = 31;
    static final int OPTION_X = 78;
    static final int OPTION_W = 88;
    static final int TEXT_X = 84;
    static final int TEXT_W = 74;
    static final int[] OPTION_Y = {131, 172};
    static final int[] TEXT_Y = {135, 176};
    static final int[] MARKER_Y = {138, 179};
    final SpriteAnim ui = SpriteAnim.load(257);
    final String[] options;
    int selected;

    ChoiceBox(int selected, String[] options) {
        this.options = options;
        this.selected = Math.max(0, Math.min(options.length - 1, selected));
    }

    static ChoiceBox optionUi(int selected, String[] options) {
        return new ChoiceBox(selected, options);
    }

    void move(int delta) {
        if (options.length == 0) {
            selected = 0;
            return;
        }
        selected = Math.max(0, Math.min(options.length - 1, selected + delta));
    }

    boolean click(int x, int y) {
        for (int i = 0; i < options.length && i < OPTION_Y.length; i++) {
            if (x >= OPTION_X && x <= OPTION_X + OPTION_W && y >= OPTION_Y[i] && y <= OPTION_Y[i] + 34) {
                selected = i;
                return true;
            }
        }
        return false;
    }

    int selectedIndex() {
        return selected;
    }

    void render(Graphics2D g, FontBitmap font) {
        for (int i = 0; i < options.length && i < OPTION_Y.length; i++) {
            drawCellTopLeft(g, OPTION_FRAME_CELL, OPTION_X, OPTION_Y[i]);
            int textWidth = font.width(options[i]);
            font.drawTaggedLine(g, options[i], TEXT_X + (TEXT_W - textWidth) / 2, TEXT_Y[i],
                    options[i].length(), 0x1C6C91);
            if (i == selected) {
                drawCellTopLeft(g, OPTION_MARKER_CELL, 150, MARKER_Y[i]);
            }
        }
    }

    void drawCellTopLeft(Graphics2D g, int cellId, int x, int y) {
        int[] bounds = ui.cellBounds(cellId);
        if (bounds == null) {
            return;
        }
        ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
    }
}

