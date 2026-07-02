import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class VqsvIntroDemo extends JPanel {
    private static final int W = 240;
    private static final int H = 320;
    private static final int SCALE = 2;
    private final BufferedImage frame = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
    private final Scene scene;

    public static void main(String[] args) {
        if (args.length > 0 && "--smoke".equals(args[0])) {
            int ticks = args.length > 2 ? Integer.parseInt(args[2]) : 360;
            runSmoke(args.length > 1 ? args[1] : "build_intro_demo/smoke.png", ticks);
            return;
        }
        JFrame f = new JFrame("VQSV Liet Hoa - Intro Scene Rebuild");
        VqsvIntroDemo panel = new VqsvIntroDemo();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setContentPane(panel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        panel.start();
    }

    private static void runSmoke(String outPath, int ticks) {
        try {
            Scene s = new Scene();
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < ticks; i++) {
                if (s.text != null && s.text.readyForKey) {
                    s.press0();
                }
                s.tick();
            }
            Graphics2D g = img.createGraphics();
            s.render(g);
            g.dispose();
            ImageIO.write(img, "png", new java.io.File(outPath));
            System.out.println("smoke-ok " + outPath + " ticks=" + ticks);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private VqsvIntroDemo() {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setFocusable(true);
        scene = new Scene();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '0' || e.getKeyCode() == KeyEvent.VK_NUMPAD0) {
                    scene.press0();
                }
            }
        });
    }

    private void start() {
        requestFocusInWindow();
        new Timer(66, e -> {
            scene.tick();
            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D fg = frame.createGraphics();
        fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        scene.render(fg);
        fg.dispose();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(frame, 0, 0, W * SCALE, H * SCALE, null);
        g2.dispose();
    }

    private static final class Scene {
        private final FontBitmap font = new FontBitmap();
        private final Effect effect = new Effect();
        private final Actor[] actors = makeActors();
        private final List<Event> events = makeEvents();
        private TextBox text;
        private int eventIndex = 0;
        private int cameraX = 0;
        private int cameraY = 0;
        private boolean key0;
        private Blocking current;

        private void press0() {
            key0 = true;
        }

        private void tick() {
            effect.tick();
            if (text != null) {
                text.tick();
                if (text.disposed) {
                    text = null;
                }
            }
            if (current != null) {
                if (!current.tick(this)) {
                    key0 = false;
                    for (Actor a : actors) {
                        a.tick();
                    }
                    return;
                }
                current = null;
            }
            int guard = 0;
            while (current == null && eventIndex < events.size() && guard++ < 8) {
                current = events.get(eventIndex++).start(this);
                if (current != null && !current.tick(this)) {
                    break;
                }
                current = null;
            }
            key0 = false;
            for (Actor a : actors) {
                a.tick();
            }
        }

        private void render(Graphics2D g) {
            g.setColor(new Color(8, 16, 80));
            g.fillRect(0, 0, W, H);

            ArrayList<Actor> draw = new ArrayList<>();
            for (Actor a : actors) {
                if (a.visible) {
                    draw.add(a);
                }
            }
            draw.sort(Comparator.comparingInt(a -> a.y));
            for (Actor a : draw) {
                a.render(g, cameraX, cameraY);
            }

            effect.renderParticles(g);
            effect.renderOverlay(g);
            if (text != null) {
                text.render(g, font);
            }
        }

        private void setCameraCenter(int cx, int cy) {
            cameraX = clamp(cx - W / 2, 0, 640 - W);
            cameraY = clamp(cy - H / 2, 0, 480 - H);
        }

        private static int clamp(int v, int lo, int hi) {
            return Math.max(lo, Math.min(hi, v));
        }

        private static Actor[] makeActors() {
            int[][] rows = {
                    {0, 84, 0, 384, 168, 0},
                    {1, 85, 0, 9, 274, 0},
                    {2, 161, 0, 53, 192, 0},
                    {3, 173, 0, 187, 419, 0},
                    {4, 185, 0, 383, 191, 0},
                    {5, 101, 0, 15, 246, 0},
                    {6, 117, 0, 354, 110, 0},
                    {7, 133, 0, 26, 91, 0},
                    {8, 149, 0, 378, 238, 0},
                    {9, 327, 2, 124, 357, 0},
                    {10, 266, 0, 618, 130, 0},
                    {11, 266, 0, 617, 144, 0},
                    {12, 266, 0, 617, 145, 0},
                    {13, 266, 0, 617, 145, 0},
                    {14, 267, 2, 255, 223, 0},
                    {15, 267, 2, 196, 239, 0},
                    {16, 267, 2, 156, 211, 0},
                    {17, 262, 0, 602, 199, 0},
                    {18, 262, 0, 132, 227, 0},
                    {19, 262, 0, 201, 222, 0},
                    {20, 262, 0, 264, 219, 0},
                    {21, 262, 0, 601, 199, 0},
                    {22, 264, 0, 148, 201, 0},
                    {23, 264, 0, 204, 221, 0},
                    {24, 264, 0, 262, 205, 0},
                    {25, 266, 0, 615, 149, 0},
                    {26, 267, 0, 153, 199, 0},
                    {27, 267, 0, 188, 249, 0},
                    {28, 267, 0, 260, 233, 0},
                    {29, 266, 0, 615, 149, 0},
                    {30, 266, 0, 616, 149, 0},
                    {31, 266, 0, 617, 149, 0},
                    {32, 266, 0, 158, 210, 0},
                    {33, 266, 0, 211, 257, 0},
                    {34, 266, 0, 617, 148, 0},
                    {35, 266, 0, 251, 203, 0},
                    {36, 262, 0, 603, 196, 0},
                    {37, 262, 0, 601, 197, 0},
                    {38, 262, 0, 602, 197, 0},
                    {39, 85, 0, 46, 166, 0}
            };
            Actor[] out = new Actor[40];
            for (int[] r : rows) {
                Actor a = new Actor(r[0], r[1], r[2], r[3], r[4]);
                a.visible = r[5] == 1;
                out[r[0]] = a;
            }
            return out;
        }

        private static List<Event> makeEvents() {
            ArrayList<Event> e = new ArrayList<>();
            e.add(s -> {
                s.effect.startSolid(0);
                s.text = TextBox.full(30, 90, "#FFFFFF Nghe đồn Thiên Địa chi sơ, vạn năm về trước có hai vị thần, một người duy trì trật tự, một người cai quản thế giới hỗn loạn, kiềm chế lẫn nhau, duy trì cân bằng của thế giới.", true);
                return sc -> {
                    if (sc.text != null && sc.text.readyForKey && sc.key0) {
                        sc.text.disposed = true;
                        sc.effect.clearOverlay();
                        return true;
                    }
                    return false;
                };
            });
            e.add(s -> { s.effect.startBars(13, 1, 1, 240, 10, 10); return s.effect::doneBars; });
            e.add(s -> { s.effect.startParticles(80); return null; });
            e.add(s -> { s.setCameraCenter(190, 0); return null; });
            e.add(s -> { setActive(s, new int[]{1, 0}, new int[]{1, 3}); return null; });
            e.add(s -> new Move(new int[]{0, 1}, new int[]{-26, 23}, new int[]{0, 0}, new int[]{5, 5}, new int[]{0, 0}));
            e.add(s -> new Move(new int[]{0, 1}, new int[]{0, 0}, new int[]{3, -3}, new int[]{0, 0}, new int[]{20, 20}));
            e.add(s -> new Move(new int[]{0, 1}, new int[]{0, 0}, new int[]{-3, 3}, new int[]{0, 0}, new int[]{10, 10}));
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{18, 19, 20, 22, 23, 24}, new int[]{1, 3, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> { hide(s, new int[]{18, 19, 20, 22, 23, 24}); return null; });
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{26, 27, 28, 14, 15, 16}, new int[]{1, 1, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { hide(s, new int[]{26, 27, 28, 14, 15, 16}); return null; });
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{32, 33, 35}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { s.effect.startCircle(0, 0, 120, 160, 25); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{32, 33, 35}); return null; });
            e.add(s -> new Move(new int[]{0}, new int[]{75}, new int[]{0}, new int[]{2}, new int[]{0}));
            e.add(s -> new Path(new int[]{1}, new int[][]{{185}}, new int[][]{{200}}));
            e.add(s -> new Move(new int[]{1}, new int[]{0}, new int[]{4}, new int[]{0}, new int[]{6}));
            e.add(s -> { s.effect.startCircle(0, 1, 120, 160, 160); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startBars(13, 1, 1, 240, 10, 50); return s.effect::doneBars; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vi Bạch Long, vị thần đứng đầu Thiên Giới phụ trách cai quản trật tự. Ba vị thủ hộ thánh thú lần lượt là Lôi Kỳ Lân, Tinh Vân Hạc cùng Minh Vương Long.", false);
                return null;
            });
            e.add(s -> new Move(new int[]{1}, new int[]{0}, new int[]{-4}, new int[]{0}, new int[]{18}));
            e.add(s -> new Delay(50));
            e.add(s -> { setActive(s, new int[]{2, 3, 4}, new int[]{1, 1, 3}); return null; });
            e.add(s -> new Move(new int[]{2}, new int[]{35}, new int[]{0}, new int[]{2}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{4}, new int[]{-30}, new int[]{0}, new int[]{4}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{3}, new int[]{0}, new int[]{-32}, new int[]{0}, new int[]{5}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{1, 2, 3, 4}); return null; });
            e.add(s -> new Move(new int[]{0}, new int[]{-100}, new int[]{-14}, new int[]{2}, new int[]{2}));
            e.add(s -> { s.effect.startParticles(90); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, "#FFFFFF Vi Hắc Long, vị thần đứng đầu Địa Giới phụ trách cai quản thế giới hỗn loạn. Bốn vị chiến thần thú lần lượt là Chiến Thần Đà, Tương Quân Giải, Linh Quang Lộc và Hỏa Phượng Hoàng.", false);
                return null;
            });
            e.add(s -> new Delay(60));
            e.add(s -> { setActive(s, new int[]{5, 6, 7, 8}, new int[]{1, 1, 1, 1}); return null; });
            e.add(s -> new Move(new int[]{7}, new int[]{30}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{8}, new int[]{-38}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{6}, new int[]{-33}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{5}, new int[]{38}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Delay(5));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> { hide(s, new int[]{5, 6, 7, 8}); return null; });
            e.add(s -> null);
            e.add(s -> null);
            e.add(s -> { if (s.text != null) { s.text.disposed = true; } return null; });
            return e;
        }

        private static void setActive(Scene s, int[] ids, int[] dirs) {
            for (int i = 0; i < ids.length; i++) {
                Actor a = s.actors[ids[i]];
                a.direction = dirs[i];
                a.visible = true;
            }
        }

        private static void hide(Scene s, int[] ids) {
            for (int id : ids) {
                s.actors[id].visible = false;
            }
        }
    }

    private interface Event {
        Blocking start(Scene s);
    }

    private interface Blocking {
        boolean tick(Scene s);
    }

    private static final class Delay implements Blocking {
        private int left;

        private Delay(int left) {
            this.left = left;
        }

        @Override
        public boolean tick(Scene s) {
            return left-- <= 0;
        }
    }

    private static final class Move implements Blocking {
        private final int[] ids, dx, dy, tx, ty;

        private Move(int[] ids, int[] dx, int[] dy, int[] tx, int[] ty) {
            this.ids = ids;
            this.dx = dx;
            this.dy = dy;
            this.tx = tx;
            this.ty = ty;
        }

        @Override
        public boolean tick(Scene s) {
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

    private static final class Path implements Blocking {
        private final int[] ids;
        private final int[][] xs;
        private final int[][] ys;
        private int step;

        private Path(int[] ids, int[][] xs, int[][] ys) {
            this.ids = ids;
            this.xs = xs;
            this.ys = ys;
        }

        @Override
        public boolean tick(Scene s) {
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

    private static final class Actor {
        private final SpriteAnim anim;
        private int x, y;
        private int direction;
        private boolean visible;

        private Actor(int id, int spriteIndex, int state, int x, int y) {
            this.anim = SpriteAnim.load(spriteIndex);
            this.anim.setState(state);
            this.x = x;
            this.y = y;
        }

        private void tick() {
            if (visible) {
                anim.tick();
            }
        }

        private void render(Graphics2D g, int camX, int camY) {
            anim.draw(g, x - camX, y - camY, direction == 3 ? 1 : 0);
        }
    }

    private static final class SpriteAnim {
        private static final int[] SPRITE_TO_IMG;
        private static final Map<Integer, SpriteData> CACHE = new HashMap<>();
        private final SpriteData data;
        private int state;
        private int cursor;
        private int delay;

        static {
            SPRITE_TO_IMG = new int[400];
            Arrays.fill(SPRITE_TO_IMG, -1);
            int[][] rows = {
                    {84, 84, 162}, {85, 85, 163}, {101, 101, 604}, {117, 117, 605},
                    {133, 133, 606}, {149, 149, 607}, {161, 161, 608}, {173, 173, 609},
                    {185, 185, 610}, {262, 262, 300}, {264, 264, 305}, {266, 266, 303},
                    {267, 267, 307}, {327, 327, 818}
            };
            for (int[] r : rows) {
                SPRITE_TO_IMG[r[0]] = r[2];
            }
        }

        private SpriteAnim(SpriteData data) {
            this.data = data;
            resetDelay();
        }

        private static SpriteAnim load(int spriteIndex) {
            int sprId = spriteIndex;
            SpriteData data = CACHE.computeIfAbsent(sprId, SpriteData::load);
            return new SpriteAnim(data);
        }

        private void setState(int state) {
            if (state >= 0 && state < data.anim.length) {
                this.state = state;
            } else {
                this.state = 0;
            }
            cursor = 0;
            resetDelay();
        }

        private void tick() {
            if (data.anim.length == 0) {
                return;
            }
            if (delay > 0) {
                delay--;
                return;
            }
            cursor++;
            if (cursor >= data.anim[state].length / 2) {
                cursor = 0;
            }
            resetDelay();
        }

        private void resetDelay() {
            if (data.anim.length == 0 || data.anim[state].length == 0) {
                delay = 0;
                return;
            }
            delay = Math.max(0, data.anim[state][cursor * 2] - 1);
        }

        private void draw(Graphics2D g, int x, int y, int orientation) {
            if (data.anim.length == 0 || data.anim[state].length == 0) {
                return;
            }
            int cellId = data.anim[state][cursor * 2 + 1];
            if (cellId < 0 || cellId >= data.cells.length) {
                return;
            }
            int[] transformMap = orientation == 1
                    ? new int[]{2, 4, 1, 7, 0, 5, 3, 6}
                    : new int[]{0, 5, 3, 6, 2, 4, 1, 7};
            short[] cells = data.cells[cellId];
            for (int i = 0; i < cells.length; i += 4) {
                int frameId = cells[i];
                int ox = cells[i + 1];
                int oy = cells[i + 2];
                int tr = transformMap[cells[i + 3] & 7];
                if (orientation == 1) {
                    int w = data.frames[frameId][3];
                    int h = data.frames[frameId][4];
                    int adjust = (cells[i + 3] % 2 == 1) ? h : w;
                    drawRegion(g, data.image, data.frames[frameId], tr, x - ox - adjust, y + oy);
                } else {
                    drawRegion(g, data.image, data.frames[frameId], tr, x + ox, y + oy);
                }
            }
        }

        private static void drawRegion(Graphics2D g, BufferedImage img, short[] f, int transform, int x, int y) {
            int sx = f[1], sy = f[2], w = f[3], h = f[4];
            if (w <= 0 || h <= 0) {
                return;
            }
            BufferedImage sub = img.getSubimage(sx, sy, w, h);
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            switch (transform) {
                case 0:
                    break;
                case 1:
                    at.translate(w, 0);
                    at.scale(-1, 1);
                    break;
                case 2:
                    at.translate(w, 0);
                    at.scale(-1, 1);
                    break;
                case 3:
                    at.translate(w, h);
                    at.rotate(Math.PI);
                    break;
                case 4:
                    at.rotate(-Math.PI / 2);
                    at.scale(-1, 1);
                    break;
                case 5:
                    at.translate(h, 0);
                    at.rotate(Math.PI / 2);
                    break;
                case 6:
                    at.translate(0, w);
                    at.rotate(-Math.PI / 2);
                    break;
                case 7:
                    at.translate(h, w);
                    at.rotate(Math.PI / 2);
                    at.scale(-1, 1);
                    break;
                default:
                    break;
            }
            g.drawImage(sub, at, null);
        }
    }

    private static final class SpriteData {
        private final short[][] frames;
        private final short[][] cells;
        private final short[][] anim;
        private final BufferedImage image;

        private SpriteData(short[][] frames, short[][] cells, short[][] anim, BufferedImage image) {
            this.frames = frames;
            this.cells = cells;
            this.anim = anim;
            this.image = image;
        }

        private static SpriteData load(int sprId) {
            try {
                byte[] bytes = readAll("/spr_" + sprId + "_all(r)");
                Cursor c = new Cursor();
                short[][] frames = asRows(readFlat(bytes, c));
                short[][] cells = readMatrix(bytes, c);
                short[][] anim = readMatrix(bytes, c);
                if (sprId >= 86 && sprId <= 185) {
                    short[][] special = new short[5][4];
                    short[] offset = {0, 10, 3, 7, -10};
                    for (int i = 0; i < special.length; i++) {
                        for (int j = 0; j < 4; j++) {
                            special[i][j] = j == 1 ? (short) (cells[0][j] + offset[i]) : cells[0][j];
                        }
                    }
                    cells = special;
                    anim = new short[][]{
                            {2, 0},
                            {1, 0, 1, 1, 1, 2, 1, 3, 1, 2},
                            {5, 0, 5, 4}
                    };
                }
                readFlat(bytes, c);
                readFlat(bytes, c);
                int imgId = SpriteAnim.SPRITE_TO_IMG[sprId];
                BufferedImage image = ImageIO.read(SpriteData.class.getResource("/img/" + imgId + ".png"));
                return new SpriteData(frames, cells, anim, image);
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot load sprite " + sprId, ex);
            }
        }

        private static short[][] asRows(short[] flat) {
            short[][] out = new short[flat.length / 5][5];
            for (int i = 0; i < out.length; i++) {
                System.arraycopy(flat, i * 5, out[i], 0, 5);
            }
            return out;
        }

        private static short[] readFlat(byte[] b, Cursor c) {
            int rows = readShort(b, c);
            int cols = readShort(b, c);
            if (rows == 0) {
                return null;
            }
            short[] out = new short[rows * cols];
            for (int i = 0; i < out.length; i++) {
                out[i] = (short) readShort(b, c);
            }
            return out;
        }

        private static short[][] readMatrix(byte[] b, Cursor c) {
            int count = readShort(b, c);
            int cols = readShort(b, c);
            if (count == 0) {
                return null;
            }
            short[][] out = new short[count][];
            for (int i = 0; i < count; i++) {
                int len = readShort(b, c);
                out[i] = new short[len * cols];
                for (int j = 0; j < out[i].length; j++) {
                    out[i][j] = (short) readShort(b, c);
                }
            }
            return out;
        }

        private static int readShort(byte[] b, Cursor c) {
            int v = ((b[c.pos++] & 0xFF) << 8) | (b[c.pos++] & 0xFF);
            return v >= 0x8000 ? v - 0x10000 : v;
        }
    }

    private static final class Cursor {
        private int pos;
    }

    private static final class FontBitmap {
        private final Map<Character, Integer> index = new HashMap<>();
        private final int[] widths;
        private final int[] offsets;
        private final byte[][] pixels;
        private final int height;
        private final int spaceWord;

        private FontBitmap() {
            try (DataInputStream in = new DataInputStream(VqsvIntroDemo.class.getResourceAsStream("/font.bin"))) {
                String chars = in.readUTF();
                height = in.readByte();
                widths = new int[chars.length()];
                offsets = new int[chars.length()];
                int total = 0;
                for (int i = 0; i < chars.length(); i++) {
                    widths[i] = in.readByte();
                    offsets[i] = total;
                    total += widths[i];
                    index.put(chars.charAt(i), i);
                }
                pixels = new byte[height][total];
                int bit = 7;
                int cur = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < total; x++) {
                        if (++bit >= 8) {
                            bit = 0;
                            cur = in.readByte();
                        }
                        if ((cur & 1) != 0) {
                            pixels[y][x] = 1;
                        }
                        cur >>= 1;
                    }
                }
                spaceWord = width("nhung1");
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private int charWidth(char c) {
            Integer i = index.get(c);
            return i == null ? 0 : widths[i];
        }

        private int width(String s) {
            int w = 0;
            for (int i = 0; i < s.length(); i++) {
                w += charWidth(s.charAt(i));
            }
            return w;
        }

        private void drawChar(Graphics2D g, char c, int x, int y) {
            Integer idx = index.get(c);
            if (idx == null) {
                return;
            }
            int off = offsets[idx];
            int w = widths[idx];
            for (int py = 0; py < height; py++) {
                for (int px = 0; px < w; px++) {
                    if (pixels[py][off + px] != 0) {
                        g.drawLine(x + px, y + py, x + px, y + py);
                    }
                }
            }
        }

        private void drawTagged(Graphics2D g, String s, int x, int y, int maxWidth, int visibleChars) {
            int cx = x;
            int cy = y;
            int color = 0xFFFFFF;
            g.setColor(new Color(color));
            int shown = 0;
            int softLimit = maxWidth - spaceWord;
            for (int i = 0; i < s.length() && shown < visibleChars; i++) {
                char ch = s.charAt(i);
                if (ch == '#' && i + 6 < s.length()) {
                    String hex = s.substring(i + 1, i + 7);
                    color = Integer.parseInt(hex, 16);
                    g.setColor(new Color(color));
                    i += 6;
                    continue;
                }
                int cw = charWidth(ch);
                int nx = cx + cw;
                if (nx > x + maxWidth - 10 || ch == ' ' && nx > x + softLimit) {
                    cx = x;
                    cy += height + 1;
                    if (ch == ' ') {
                        shown++;
                        continue;
                    }
                }
                drawChar(g, ch, cx, cy);
                cx += cw;
                shown++;
            }
        }
    }

    private static final class TextBox {
        private final int x, y, w, h;
        private final String text;
        private final boolean waitKey;
        private int visibleChars;
        private int doneTicks;
        private boolean readyForKey;
        private boolean disposed;

        private TextBox(int x, int y, int w, int h, String text, boolean waitKey) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.text = text;
            this.waitKey = waitKey;
        }

        private static TextBox full(int x, int y, String text, boolean waitKey) {
            return new TextBox(x, y, W - 2 * x, H - y, text, waitKey);
        }

        private static TextBox box(int x, int y, int w, int h, String text, boolean waitKey) {
            return new TextBox(x, y, w, h, text, waitKey);
        }

        private void tick() {
            int total = visibleLength(text);
            if (visibleChars < total) {
                visibleChars = Math.min(total, visibleChars + 2);
                doneTicks = 0;
            } else {
                doneTicks++;
                if (waitKey && doneTicks > 38) {
                    readyForKey = true;
                }
            }
        }

        private void render(Graphics2D g, FontBitmap font) {
            font.drawTagged(g, text, x, y, w, visibleChars);
            if (readyForKey && (doneTicks / 5) % 2 == 0) {
                String prompt = "Nhấn nút 0 để tiếp tục";
                g.setColor(Color.WHITE);
                int px = (W - font.width(prompt)) / 2;
                font.drawTagged(g, prompt, px, H - 18, W, prompt.length());
            }
        }

        private static int visibleLength(String s) {
            int n = 0;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '#' && i + 6 < s.length()) {
                    i += 6;
                } else {
                    n++;
                }
            }
            return n;
        }
    }

    private static final class Effect {
        private int overlayType = -1;
        private int solidColor;
        private int tick;
        private int flashLimit;
        private int flashMode;
        private int barsMode;
        private int barsProgress;
        private int barsTotal;
        private int barsStep;
        private int barsWidth;
        private int barsTop;
        private int barsBottom;
        private boolean barsDone = true;
        private boolean overlayDone = true;
        private int circleMode;
        private int circleState;
        private int circleX, circleY, circleR;
        private int fadeColor;
        private int fadeAlpha;
        private int fadeType;
        private final int[] circleColors = {0xFFFFFF, 9115396};
        private BufferedImage[] particleImages;
        private Particle[] particles = new Particle[0];
        private final Random random = new Random(7);

        private void clearOverlay() {
            overlayType = -1;
            overlayDone = true;
        }

        private void startSolid(int color) {
            overlayType = 9;
            solidColor = color;
            overlayDone = false;
        }

        private void startFlash(int limit, int mode) {
            overlayType = 10;
            tick = 0;
            flashLimit = limit;
            flashMode = mode;
            overlayDone = false;
        }

        private void startFade(int type, int color) {
            overlayType = type;
            fadeType = type;
            fadeColor = color & 0xFFFFFF;
            fadeAlpha = type == 1 ? 255 : 0;
            tick = 0;
            overlayDone = false;
        }

        private void startBars(int type, int total, int step, int width, int top, int bottom) {
            barsMode = type;
            barsTotal = Math.max(1, total);
            barsStep = step;
            barsWidth = width;
            barsTop = top;
            barsBottom = bottom;
            barsProgress = 0;
            barsDone = false;
        }

        private void startCircle(int colorIndex, int state, int x, int y, int radius) {
            overlayType = 17;
            circleMode = colorIndex;
            circleState = state;
            circleX = x;
            circleY = y;
            circleR = radius;
            tick = 0;
            overlayDone = false;
        }

        private void startParticles(int count) {
            clearOverlay();
            try {
                particleImages = new BufferedImage[]{
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star0.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star1.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star2.png")),
                        ImageIO.read(VqsvIntroDemo.class.getResource("/tex/star3.png"))
                };
                particles = new Particle[count];
                for (int i = 0; i < count; i++) {
                    particles[i] = new Particle();
                    resetParticle(particles[i]);
                }
            } catch (IOException ex) {
                particles = new Particle[0];
            }
        }

        private boolean doneBars(Scene ignored) {
            return barsDone;
        }

        private boolean doneOverlay(Scene ignored) {
            return overlayDone;
        }

        private void tick() {
            if (!barsDone) {
                barsProgress += barsStep;
                if (barsProgress > barsTotal) {
                    barsProgress = barsTotal;
                    barsDone = true;
                    if (barsMode == 12) {
                        barsMode = -1;
                    }
                }
            }
            if (overlayType == 10) {
                tick++;
                if (tick > flashLimit) {
                    clearOverlay();
                }
            } else if (overlayType == 1 || overlayType == 2) {
                tick++;
                if (fadeType == 1) {
                    fadeAlpha -= 17;
                    if (fadeAlpha <= 0) {
                        clearOverlay();
                    }
                } else {
                    fadeAlpha += 17;
                    if (fadeAlpha >= 255) {
                        fadeAlpha = 255;
                        overlayDone = true;
                    }
                }
            } else if (overlayType == 17) {
                tick++;
                if (circleState == 0) {
                    int dx = W - circleX;
                    int dy = H - circleY;
                    if (dx * dx + dy * dy < circleR * circleR) {
                        overlayDone = true;
                    }
                    circleR += 10;
                } else if (circleState == 1) {
                    circleR -= 10;
                    if (circleR <= 0) {
                        clearOverlay();
                    }
                } else {
                    if (tick <= 10) {
                        circleR += 10;
                    } else if (tick <= 20) {
                        circleR -= 10;
                    } else {
                        clearOverlay();
                    }
                }
            }
        }

        private void renderParticles(Graphics2D g) {
            if (particleImages == null) {
                return;
            }
            for (Particle p : particles) {
                BufferedImage img = particleImages[p.img];
                g.drawImage(img, p.x, p.y, null);
                p.x -= p.speed;
                p.y -= p.speed;
                if (p.x < -img.getWidth() || p.y < -img.getHeight()) {
                    resetParticle(p);
                    p.x = W + random.nextInt(80);
                    p.y = random.nextInt(H);
                }
            }
        }

        private void renderOverlay(Graphics2D g) {
            if (overlayType == 9) {
                g.setColor(new Color(solidColor));
                g.fillRect(0, 0, W, H);
            } else if (overlayType == 1 || overlayType == 2) {
                int a = Math.max(0, Math.min(255, fadeAlpha));
                g.setColor(new Color((a << 24) | fadeColor, true));
                g.fillRect(0, 0, W, H);
            } else if (overlayType == 10) {
                if (tick <= flashLimit) {
                    if (tick % 3 / (flashMode + 1) == 0) {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, W, H);
                    } else if (tick % 3 / (flashMode + 1) == 1) {
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, W, H);
                    }
                }
            } else if (overlayType == 17) {
                g.setColor(new Color(circleColors[Math.max(0, Math.min(circleMode, 1))]));
                g.fillOval(circleX - circleR, circleY - circleR, circleR * 2, circleR * 2);
            }
            if (barsMode == 12 || barsMode == 13) {
                g.setColor(Color.BLACK);
                if (barsMode == 13) {
                    int top = barsProgress * barsTop / barsTotal;
                    int bottom = barsProgress * barsBottom / barsTotal;
                    g.fillRect(0, 0, barsWidth, top);
                    g.fillRect(0, H - bottom, barsWidth, bottom);
                } else {
                    int top = barsTop - barsProgress * barsTop / barsTotal;
                    int bottom = barsBottom - barsProgress * barsBottom / barsTotal;
                    g.fillRect(0, 0, barsWidth, top);
                    g.fillRect(0, H - bottom, barsWidth, bottom);
                }
            }
        }

        private void resetParticle(Particle p) {
            int roll = random.nextInt(100);
            p.img = roll < 3 ? 3 : roll < 15 ? 2 : roll < 50 ? 1 : 0;
            p.x = random.nextInt(W);
            p.y = random.nextInt(H);
            p.speed = random.nextInt(EffectSpeed[p.img][1] - EffectSpeed[p.img][0]) + EffectSpeed[p.img][0];
        }

        private static final int[][] EffectSpeed = {{1, 3}, {1, 4}, {2, 5}, {2, 6}};
    }

    private static final class Particle {
        private int img, x, y, speed;
    }

    private static byte[] readAll(String resource) throws IOException {
        try (InputStream in = VqsvIntroDemo.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing resource " + resource);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }
}
