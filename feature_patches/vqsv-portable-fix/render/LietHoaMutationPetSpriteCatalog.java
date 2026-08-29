package vqsv.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;

final class LietHoaMutationPetSpriteCatalog {
   private static final short IDLE_ORIGIN_Y = -55;
   private static final short ACTION_ORIGIN_Y = -69;

   static boolean owns(int var0) {
      return LietHoaMutationCatalog.isVisual(var0) || ThienLuanMutationCatalog.isVisual(var0) || HoaDiemHauVuongCatalog.isVisual(var0) || ThienViemHoCatalog.isVisual(var0);
   }

   static SpriteData load(int var0) {
      if (!owns(var0)) {
         throw new IllegalStateException("Unknown mutation visual " + var0);
      } else {
         return LietHoaMutationCatalog.isVisual(var0) ? loadHoaVu() : (ThienLuanMutationCatalog.isVisual(var0) ? loadThienLuan() : (HoaDiemHauVuongCatalog.isVisual(var0) ? loadHoaDiemHauVuong() : loadThienViemHo()));
      }
   }

   private static SpriteData loadHoaVu() {
      String var0 = "/vqsv/data/unified/liet-hoa-mutations/LH-023-MUT-01/";
      BufferedImage var1 = image(var0, "idle.png");
      BufferedImage var2 = image(var0, "action.png");
      if (var1.getWidth() == 180 && var1.getHeight() == 55 && var2.getWidth() == 316 && var2.getHeight() == 69) {
         short[][] var3 = new short[8][];
         short[][] var4 = new short[8][];

         for(int var5 = 0; var5 < 4; ++var5) {
            var3[var5] = new short[]{1, (short)(var5 * 45), 0, 45, 55};
            var4[var5] = new short[]{(short)var5, -22, -55, 0};
            var3[var5 + 4] = new short[]{0, (short)(var5 * 79), 0, 79, 69};
            var4[var5 + 4] = new short[]{(short)(var5 + 4), -39, -69, 0};
         }

         short[] var8 = new short[8];
         short[] var6 = new short[8];

         for(int var7 = 0; var7 < 4; ++var7) {
            var8[var7 * 2] = 4;
            var8[var7 * 2 + 1] = (short)var7;
            var6[var7 * 2] = 5;
            var6[var7 * 2 + 1] = (short)(var7 + 4);
         }

         return new SpriteData(var3, var4, new short[][]{var8, var6, var8}, (short[][])null, (short[][])null, new BufferedImage[]{var2, var1});
      } else {
         throw new IllegalStateException("LH-023-MUT-01 asset dimensions changed");
      }
   }

   private static SpriteData loadThienLuan() {
      String var0 = "/vqsv/data/unified/liet-hoa-mutations/LH-034-MUT-01/";
      BufferedImage var1 = image(var0, "idle.png");
      BufferedImage var2 = image(var0, "action.png");
      if (var1.getWidth() == 412 && var1.getHeight() == 96 && var2.getWidth() == 1824 && var2.getHeight() == 101) {
         short[][] var3 = new short[12][];
         short[][] var4 = new short[12][];

         for(int var5 = 0; var5 < 4; ++var5) {
            var3[var5] = new short[]{1, (short)(var5 * 103), 0, 103, 96};
            var4[var5] = new short[]{(short)var5, -58, -96, 0};
         }

         for(int var8 = 0; var8 < 8; ++var8) {
            int var6 = var8 + 4;
            var3[var6] = new short[]{0, (short)(var8 * 228), 0, 228, 101};
            var4[var6] = new short[]{(short)var6, -58, -96, 0};
         }

         short[] var9 = new short[8];
         short[] var10 = new short[16];

         for(int var7 = 0; var7 < 4; ++var7) {
            var9[var7 * 2] = 4;
            var9[var7 * 2 + 1] = (short)var7;
         }

         for(int var11 = 0; var11 < 8; ++var11) {
            var10[var11 * 2] = (short)(var11 == 7 ? 3 : 2);
            var10[var11 * 2 + 1] = (short)(var11 + 4);
         }

         return new SpriteData(var3, var4, new short[][]{var9, var10, var9}, (short[][])null, (short[][])null, new BufferedImage[]{var2, var1});
      } else {
         throw new IllegalStateException("LH-034-MUT-01 asset dimensions changed");
      }
   }

   private static SpriteData loadHoaDiemHauVuong() {
      String root = "/vqsv/data/unified/liet-hoa-mutations/LH-064-EVO-01/";
      BufferedImage idle = image(root, "idle.png");
      BufferedImage action = image(root, "action.png");
      if (idle.getWidth() != 300 || idle.getHeight() != 110 || action.getWidth() != 500 || action.getHeight() != 110) {
         throw new IllegalStateException("LH-064-EVO-01 asset dimensions changed");
      }
      short[][] frames = new short[9][];
      short[][] origins = new short[9][];
      for (int i = 0; i < 4; ++i) {
         frames[i] = new short[]{1, (short)(i * 75), 0, 75, 110};
         origins[i] = new short[]{(short)i, -37, -110, 0};
      }
      for (int i = 0; i < 5; ++i) {
         frames[i + 4] = new short[]{0, (short)(i * 100), 0, 100, 110};
         origins[i + 4] = new short[]{(short)(i + 4), -50, -110, 0};
      }
      // The cleaned frames share one torso/foot anchor. A slightly longer,
      // even cadence keeps the flame alive without making the body jitter.
      short[] idleSequence = new short[]{6, 0, 6, 1, 6, 2, 6, 3};
      short[] actionSequence = new short[]{2, 4, 2, 5, 2, 6, 2, 7, 3, 8};
      return new SpriteData(frames, origins, new short[][]{idleSequence, actionSequence, idleSequence}, null, null, new BufferedImage[]{action, idle});
   }

   private static SpriteData loadThienViemHo() {
      String root = "/vqsv/data/unified/liet-hoa-mutations/LH-004-EVO-01/";
      BufferedImage idle = image(root, "idle.png");
      BufferedImage action = image(root, "action.png");
      if (idle.getWidth() != 2048 || idle.getHeight() != 110 || action.getWidth() != 2048 || action.getHeight() != 110) {
         throw new IllegalStateException("LH-004-EVO-01 asset dimensions changed");
      }
      short[][] frames = new short[32][];
      short[][] origins = new short[32][];
      for (int i = 0; i < 16; ++i) {
         frames[i] = new short[]{1, (short)(i * 128), 0, 128, 110};
         origins[i] = new short[]{(short)i, -64, -110, 0};
         frames[i + 16] = new short[]{0, (short)(i * 128), 0, 128, 110};
         origins[i + 16] = new short[]{(short)(i + 16), -64, -110, 0};
      }
      short[] idleSequence = new short[32];
      short[] actionSequence = new short[32];
      for (int i = 0; i < 16; ++i) {
         idleSequence[i * 2] = 2;
         idleSequence[i * 2 + 1] = (short)i;
         actionSequence[i * 2] = 2;
         actionSequence[i * 2 + 1] = (short)(i + 16);
      }
      return new SpriteData(frames, origins, new short[][]{idleSequence, actionSequence, idleSequence}, null, null, new BufferedImage[]{action, idle});
   }

   private static BufferedImage image(String var0, String var1) {
      try {
         InputStream var2 = LietHoaMutationPetSpriteCatalog.class.getResourceAsStream(var0 + var1);

         BufferedImage var4;
         try {
            if (var2 == null) {
               throw new IllegalStateException("Missing mutation asset " + var1);
            }

            BufferedImage var3 = ImageIO.read(var2);
            if (var3 == null) {
               throw new IllegalStateException("Unreadable mutation asset " + var1);
            }

            var4 = var3;
         } catch (Throwable var6) {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (var2 != null) {
            var2.close();
         }

         return var4;
      } catch (IOException var7) {
         throw new IllegalStateException("Cannot read mutation asset " + var1, var7);
      }
   }
}
