package vqsv.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import vqsv.pet.data.TanNguyetLongMaCatalog;

final class CustomBossSpriteCatalog {
   private static final int FRAME_WIDTH = 128;
   private static final int FRAME_HEIGHT = 112;

   static boolean owns(int visualId) {
      return TanNguyetLongMaCatalog.isVisual(visualId);
   }

   static SpriteData load(int visualId) {
      if (!owns(visualId)) throw new IllegalStateException("Unknown custom boss visual " + visualId);
      String root = "/vqsv/data/unified/custom-bosses/TAN-NGUYET-LONG-MA/";
      BufferedImage idle = image(root + "idle.png");
      BufferedImage action = image(root + "action.png");
      if (idle.getWidth() != FRAME_WIDTH * 8 || idle.getHeight() != FRAME_HEIGHT
         || action.getWidth() != FRAME_WIDTH * 12 || action.getHeight() != FRAME_HEIGHT) {
         throw new IllegalStateException("TAN-NGUYET-LONG-MA asset dimensions changed");
      }
      short[][] frames = new short[20][];
      short[][] origins = new short[20][];
      for (int i = 0; i < 8; i++) {
         frames[i] = new short[]{1, (short)(i * FRAME_WIDTH), 0, FRAME_WIDTH, FRAME_HEIGHT};
         origins[i] = new short[]{(short)i, -64, -112, 0};
      }
      for (int i = 0; i < 12; i++) {
         frames[i + 8] = new short[]{0, (short)(i * FRAME_WIDTH), 0, FRAME_WIDTH, FRAME_HEIGHT};
         origins[i + 8] = new short[]{(short)(i + 8), -64, -112, 0};
      }
      short[] idleSequence = sequence(0, 8, 3);
      short[] actionSequence = sequence(8, 12, 2);
      return new SpriteData(frames, origins, new short[][]{idleSequence, actionSequence, idleSequence}, null, null, new BufferedImage[]{action, idle});
   }

   private static short[] sequence(int first, int count, int delay) {
      short[] result = new short[count * 2];
      for (int i = 0; i < count; i++) {
         result[i * 2] = (short)delay;
         result[i * 2 + 1] = (short)(first + i);
      }
      return result;
   }

   private static BufferedImage image(String path) {
      try (InputStream input = CustomBossSpriteCatalog.class.getResourceAsStream(path)) {
         if (input == null) throw new IllegalStateException("Missing custom boss asset " + path);
         BufferedImage image = ImageIO.read(input);
         if (image == null) throw new IllegalStateException("Unreadable custom boss asset " + path);
         return image;
      } catch (IOException error) {
         throw new IllegalStateException("Cannot read custom boss asset " + path, error);
      }
   }
}
