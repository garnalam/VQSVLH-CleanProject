package com.vqsv.rebuild.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class MidpTransform {
    public static final int TRANS_NONE = 0;
    public static final int TRANS_MIRROR_ROT180 = 1;
    public static final int TRANS_MIRROR = 2;
    public static final int TRANS_ROT180 = 3;
    public static final int TRANS_MIRROR_ROT270 = 4;
    public static final int TRANS_ROT90 = 5;
    public static final int TRANS_ROT270 = 6;
    public static final int TRANS_MIRROR_ROT90 = 7;

    private MidpTransform() {
    }

    public static void drawRegion(Graphics2D graphics, BufferedImage image, int sx, int sy, int width, int height,
                                  int transform, int x, int y) {
        BufferedImage region = image.getSubimage(sx, sy, width, height);
        BufferedImage transformed = transform(region, transform);
        graphics.drawImage(transformed, x, y, null);
    }

    public static BufferedImage transform(BufferedImage source, int transform) {
        int srcW = source.getWidth();
        int srcH = source.getHeight();
        int dstW = swapsAxes(transform) ? srcH : srcW;
        int dstH = swapsAxes(transform) ? srcW : srcH;
        BufferedImage dest = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < srcH; y++) {
            for (int x = 0; x < srcW; x++) {
                int[] point = mapPoint(x, y, srcW, srcH, transform);
                dest.setRGB(point[0], point[1], source.getRGB(x, y));
            }
        }
        return dest;
    }

    private static boolean swapsAxes(int transform) {
        return transform == TRANS_ROT90
                || transform == TRANS_ROT270
                || transform == TRANS_MIRROR_ROT90
                || transform == TRANS_MIRROR_ROT270;
    }

    private static int[] mapPoint(int x, int y, int width, int height, int transform) {
        switch (transform) {
            case TRANS_NONE:
                return new int[]{x, y};
            case TRANS_ROT90:
                return new int[]{height - 1 - y, x};
            case TRANS_ROT180:
                return new int[]{width - 1 - x, height - 1 - y};
            case TRANS_ROT270:
                return new int[]{y, width - 1 - x};
            case TRANS_MIRROR:
                return new int[]{width - 1 - x, y};
            case TRANS_MIRROR_ROT180:
                return new int[]{x, height - 1 - y};
            case TRANS_MIRROR_ROT90:
                return new int[]{height - 1 - y, width - 1 - x};
            case TRANS_MIRROR_ROT270:
                return new int[]{y, x};
            default:
                throw new IllegalArgumentException("Unsupported MIDP transform: " + transform);
        }
    }
}
