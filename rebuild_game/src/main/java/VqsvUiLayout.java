import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VqsvUiLayout {
    private static final Map<String, VqsvUiLayout> CACHE = new HashMap<>();
    private static final Pattern ALT_PATTERN = Pattern.compile(
            "\"alt_image_ref\"\\s*:\\s*\\{\\s*\"id\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"mode\"\\s*:\\s*(-?\\d+)",
            Pattern.DOTALL);
    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "\"image_ref\"\\s*:\\s*\\{\\s*\"id\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"mode\"\\s*:\\s*(-?\\d+)",
            Pattern.DOTALL);

    final String name;
    final boolean binarySource;
    private final Map<Integer, UiWidget> widgets;

    private VqsvUiLayout(String name, boolean binarySource, Map<Integer, UiWidget> widgets) {
        this.name = name;
        this.binarySource = binarySource;
        this.widgets = widgets;
    }

    static VqsvUiLayout load(String uiName) {
        VqsvUiLayout cached = CACHE.get(uiName);
        if (cached != null) {
            return cached;
        }
        VqsvUiLayout loaded = loadBinary(uiName);
        if (loaded.widgets.isEmpty()) {
            loaded = loadDecoded(uiName);
        }
        CACHE.put(uiName, loaded);
        return loaded;
    }

    UiWidget widget(int id) {
        return widgets.get(id);
    }

    int x(int id, int fallback) {
        UiWidget widget = widget(id);
        return widget == null ? fallback : widget.x;
    }

    int y(int id, int fallback) {
        UiWidget widget = widget(id);
        return widget == null ? fallback : widget.y;
    }

    int w(int id, int fallback) {
        UiWidget widget = widget(id);
        return widget == null || widget.w <= 0 ? fallback : widget.w;
    }

    int h(int id, int fallback) {
        UiWidget widget = widget(id);
        return widget == null || widget.h <= 0 ? fallback : widget.h;
    }

    int bandHeight(int id, int fallback) {
        UiWidget widget = widget(id);
        if (widget == null) {
            return fallback;
        }
        if (widget.h > 0) {
            return widget.h;
        }
        int nextY = Integer.MAX_VALUE;
        for (UiWidget other : widgets.values()) {
            if (other.id == widget.id || other.y <= widget.y) {
                continue;
            }
            if (other.x == widget.x && Math.abs(other.w - widget.w) <= 1) {
                nextY = Math.min(nextY, other.y);
            }
        }
        if (nextY != Integer.MAX_VALUE) {
            return Math.max(1, nextY - widget.y);
        }
        return fallback;
    }

    String text(int id, String fallback) {
        UiWidget widget = widget(id);
        if (widget == null || widget.text == null || widget.text.isEmpty()) {
            return fallback;
        }
        return widget.text;
    }

    int widgetCount() {
        return widgets.size();
    }

    private static VqsvUiLayout loadBinary(String uiName) {
        Map<Integer, UiWidget> result = new HashMap<>();
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            BinaryReader in = new BinaryReader(Files.readAllBytes(paths.uiOriginal(uiName)));
            in.readShort();
            in.readShort();
            in.readUnsignedByte();
            in.readShort();
            in.readShort();
            in.readShort();
            in.readShort();
            in.readShort();
            parseContainer(in, result);
            if (!in.isAtEnd()) {
                throw new IllegalStateException("ui parser did not consume " + uiName
                        + " offset=" + in.offset + " total=" + in.bytes.length);
            }
        } catch (IOException | RuntimeException ex) {
            result.clear();
        }
        return new VqsvUiLayout(uiName, true, result);
    }

    private static void parseContainer(BinaryReader in, Map<Integer, UiWidget> result) {
        int navCount = in.readUnsignedByte();
        for (int i = 0; i < navCount; i++) {
            in.readUnsignedByte();
            in.readUnsignedByte();
            in.readUnsignedByte();
            in.readUnsignedByte();
        }
        int styleCount = in.readUnsignedByte();
        for (int i = 0; i < styleCount; i++) {
            skipStyle(in);
        }
        int childCount = in.readShort();
        for (int i = 0; i < childCount; i++) {
            int type = in.readUnsignedByte();
            if (type == 0) {
                in.readShort();
                in.readShort();
                in.readShort();
                in.readShort();
                in.readShort();
                parseContainer(in, result);
            } else if (type == 1) {
                parseVisual(in, result);
            } else if (type == 2) {
                skipGrid(in);
            } else {
                throw new IllegalStateException("Unsupported ui widget type " + type);
            }
        }
    }

    private static void skipStyle(BinaryReader in) {
        in.readUnsignedByte();
        in.readUnsignedByte();
        in.readShort();
        int textCount = in.readShort();
        in.readUnsignedByte();
        in.readUnsignedByte();
        for (int i = 0; i < textCount; i++) {
            in.readShort();
            in.skip(in.readShort());
        }
        int stateGroupCount = in.readShort();
        for (int i = 0; i < stateGroupCount; i++) {
            in.readShort();
            int rowCount = in.readShort();
            for (int row = 0; row < rowCount; row++) {
                in.readShort();
                in.readShort();
                in.readShort();
                in.readShort();
                in.readShort();
            }
        }
    }

    private static void parseVisual(BinaryReader in, Map<Integer, UiWidget> result) {
        int id = in.readShort();
        int x = in.readShort();
        int y = in.readShort();
        int w = in.readShort();
        int h = in.readShort();
        String text = in.readUtf16String(in.readShort());
        int b = in.readByte();
        int c = in.readByte();
        boolean wraps = in.readByte() != 0;
        int eColor = in.readInt();
        int fColor = in.readInt();
        int gColor = in.readInt();
        int imageId = in.readShort();
        int imageMode = in.readByte();
        int jColor = in.readInt();
        int kColor = in.readInt();
        int lColor = in.readInt();
        int altId = in.readShort();
        int altMode = in.readByte();
        int animByte = in.readByte();
        result.put(id, new UiWidget(id, "visual", x, y, w, h, b, c, text,
                imageId, imageMode, altId, altMode, wraps, eColor, fColor, gColor,
                jColor, kColor, lColor, animByte));
        in.readByte();
        in.readByte();
    }

    private static void skipGrid(BinaryReader in) {
        in.readShort();
        in.readShort();
        in.readShort();
        for (int i = 0; i < 16; i++) {
            in.readByte();
        }
        in.readInt();
        in.readShort();
        in.readByte();
        in.readShort();
        in.readByte();
        in.readShort();
        int mode = in.readByte();
        if (mode == 1) {
            int count = in.readShort();
            for (int i = 0; i < count; i++) {
                in.readShort();
                in.readShort();
                in.readByte();
                in.readShort();
                in.readShort();
                in.readShort();
                in.readShort();
            }
        }
    }

    private static VqsvUiLayout loadDecoded(String uiName) {
        Map<Integer, UiWidget> result = new HashMap<>();
        try {
            AssetPaths paths = AssetPaths.fromWorkingTree(GameConfig.defaultConfig());
            Path path = paths.modulesRoot().resolve("ui").resolve("decoded")
                    .resolve("data__ui__" + uiName + ".json");
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (!lines.get(i).trim().equals("{")) {
                    continue;
                }
                if (i + 1 >= lines.size()
                        || !lines.get(i + 1).contains("\"type\": \"visual\"")) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                int depth = 0;
                boolean started = false;
                for (; i < lines.size(); i++) {
                    String line = lines.get(i);
                    builder.append(line).append('\n');
                    for (int cIndex = 0; cIndex < line.length(); cIndex++) {
                        char ch = line.charAt(cIndex);
                        if (ch == '{') {
                            depth++;
                            started = true;
                        } else if (ch == '}') {
                            depth--;
                        }
                    }
                    if (started && depth <= 0) {
                        break;
                    }
                }
                String object = builder.toString();
                int id = intValue(object, "id", Integer.MIN_VALUE);
                if (id == Integer.MIN_VALUE) {
                    continue;
                }
                int altId = -1;
                int altMode = -1;
                int imageId = -1;
                int imageMode = -1;
                Matcher image = IMAGE_PATTERN.matcher(object);
                if (image.find()) {
                    imageId = Integer.parseInt(image.group(1));
                    imageMode = Integer.parseInt(image.group(2));
                }
                Matcher alt = ALT_PATTERN.matcher(object);
                if (alt.find()) {
                    altId = Integer.parseInt(alt.group(1));
                    altMode = Integer.parseInt(alt.group(2));
                }
                result.put(id, new UiWidget(id, "visual",
                        intValue(object, "x", 0),
                        intValue(object, "y", 0),
                        intValue(object, "w", 0),
                        intValue(object, "h", intValue(object, "h_px", -1)),
                        intValue(object, "b", -1),
                        intValue(object, "c", -1),
                        stringValue(object, "text", ""),
                        imageId, imageMode, altId, altMode,
                        Boolean.parseBoolean(stringValue(object, "d", "false")),
                        intValue(object, "e_color", 0),
                        intValue(object, "f_color", 0),
                        intValue(object, "g_color", 0),
                        intValue(object, "j_color", 0),
                        intValue(object, "k_color", 0),
                        intValue(object, "l_color", 0),
                        intValue(object, "a_byte", -1)));
            }
        } catch (IOException | RuntimeException ex) {
            result.clear();
        }
        return new VqsvUiLayout(uiName, false, result);
    }

    private static int intValue(String text, String key, int fallback) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static String stringValue(String text, String key, String fallback) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    static final class UiWidget {
        final int id;
        final String type;
        final int x;
        final int y;
        final int w;
        final int h;
        final int b;
        final int c;
        final String text;
        final int imageId;
        final int imageMode;
        final int altId;
        final int altMode;
        final boolean wraps;
        final int eColor;
        final int fColor;
        final int gColor;
        final int jColor;
        final int kColor;
        final int lColor;
        final int animByte;

        UiWidget(int id, String type, int x, int y, int w, int h, int b, int c,
                 String text, int imageId, int imageMode, int altId, int altMode,
                 boolean wraps, int eColor, int fColor, int gColor,
                 int jColor, int kColor, int lColor, int animByte) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.b = b;
            this.c = c;
            this.text = text;
            this.imageId = imageId;
            this.imageMode = imageMode;
            this.altId = altId;
            this.altMode = altMode;
            this.wraps = wraps;
            this.eColor = eColor;
            this.fColor = fColor;
            this.gColor = gColor;
            this.jColor = jColor;
            this.kColor = kColor;
            this.lColor = lColor;
            this.animByte = animByte;
        }
    }

    private static final class BinaryReader {
        private final byte[] bytes;
        private int offset;

        BinaryReader(byte[] bytes) {
            this.bytes = bytes;
        }

        int readByte() {
            require(1);
            return bytes[offset++];
        }

        int readUnsignedByte() {
            return readByte() & 0xff;
        }

        int readShort() {
            require(2);
            return (short) (((bytes[offset++] & 0xff) << 8) | (bytes[offset++] & 0xff));
        }

        int readInt() {
            require(4);
            return ((bytes[offset++] & 0xff) << 24)
                    | ((bytes[offset++] & 0xff) << 16)
                    | ((bytes[offset++] & 0xff) << 8)
                    | (bytes[offset++] & 0xff);
        }

        String readUtf16String(int byteLen) {
            if (byteLen <= 0) {
                return "";
            }
            require(byteLen);
            String value = new String(bytes, offset, byteLen, StandardCharsets.UTF_16BE);
            offset += byteLen;
            return value;
        }

        void skip(int count) {
            int safeCount = Math.max(0, count);
            require(safeCount);
            offset += safeCount;
        }

        boolean isAtEnd() {
            return offset == bytes.length;
        }

        private void require(int count) {
            if (offset + count > bytes.length) {
                throw new IllegalStateException("ui binary read past EOF offset=" + offset
                        + " count=" + count + " total=" + bytes.length);
            }
        }
    }
}
