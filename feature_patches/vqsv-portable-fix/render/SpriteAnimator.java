package vqsv.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import vqsv.core.GameConfig;
import vqsv.fashion.SourceFashionRecord;
import vqsv.resource.AssetPaths;

public final class SpriteAnimator {
   private static final int[][] SPRITE_TO_IMGS = fallbackImageMappings();
   private static volatile SpriteTable sourceSpriteTable = loadSourceSpriteTable();
   private static final Map<String, SpriteData> CACHE = new HashMap();
   private final SpriteData data;
   private final boolean extendedAnimation;
   private int state;
   private int cursor;
   private int delay;
   private int endBehavior = -1;

   private SpriteAnimator(SpriteData var1, boolean var2) {
      this.data = var1;
      this.extendedAnimation = var2;
      this.resetDelay();
   }

   public static SpriteAnimator load(int var0) {
      return loadResolved(var0, sourceSpriteTable(), AssetPaths.fromWorkingTree(GameConfig.defaultConfig()), false);
   }

   public static SpriteAnimator load(AssetPaths var0, int var1, boolean var2) {
      SpriteTable var3;
      try {
         var3 = SpriteTable.load(var0);
      } catch (RuntimeException var5) {
         var3 = null;
      }

      return loadResolved(var1, var3, var0, var2);
   }

   public static SpriteAnimator loadFashion(SourceFashionRecord var0) {
      if (var0 == null) {
         throw new IllegalArgumentException("Fashion record is required.");
      } else if (var0.sourceKind == SourceFashionRecord.SourceKind.CAU_VONG) {
         String var1 = "fashion:" + var0.stableKey;
         SpriteData var2 = (SpriteData)CACHE.computeIfAbsent(var1, (var1x) -> RainbowFashionSpriteCatalog.load(var0));
         return new SpriteAnimator(var2, false);
      } else {
         return load(var0.resourceId);
      }
   }

   static SpriteAnimator missing(int var0, String var1) {
      String var2 = var1 != null && !var1.isBlank() ? var1 : "unknown";
      System.err.println("VQSV_PET_SPRITE_FALLBACK visual=" + var0 + " reason=" + var2);
      return new SpriteAnimator(SpriteData.blank("visual=" + var0 + " " + var2), false);
   }

   private static SpriteAnimator loadResolved(int var0, SpriteTable var1, AssetPaths var2, boolean var3) {
      if (CustomBossSpriteCatalog.owns(var0)) {
         String key = "custom-boss:" + var0;
         try {
            SpriteData data = CACHE.computeIfAbsent(key, unused -> CustomBossSpriteCatalog.load(var0));
            return new SpriteAnimator(data, false);
         } catch (RuntimeException error) {
            return missing(var0, error.getMessage());
         }
      } else if (LietHoaMutationPetSpriteCatalog.owns(var0)) {
         String var12 = "lh-mutation:" + var0;

         try {
            SpriteData var15 = (SpriteData)CACHE.computeIfAbsent(var12, (var1x) -> LietHoaMutationPetSpriteCatalog.load(var0));
            return new SpriteAnimator(var15, false);
         } catch (RuntimeException var7) {
            return missing(var0, var7.getMessage());
         }
      } else if (RainbowPetSpriteCatalog.owns(var0)) {
         String var11 = "rainbow:" + var0;

         try {
            SpriteData var14 = (SpriteData)CACHE.computeIfAbsent(var11, (var1x) -> RainbowPetSpriteCatalog.load(var0));
            return new SpriteAnimator(var14, false);
         } catch (RuntimeException var8) {
            return missing(var0, var8.getMessage());
         }
      } else if (V4PetSpriteCatalog.owns(var0)) {
         String var10 = "v4:" + var0;

         try {
            SpriteData var13 = (SpriteData)CACHE.computeIfAbsent(var10, (var1x) -> V4PetSpriteCatalog.load(var0));
            return new SpriteAnimator(var13, false);
         } catch (RuntimeException var9) {
            return missing(var0, var9.getMessage());
         }
      } else {
         SpriteRef var4 = SpriteAnimator.SpriteRef.from(var0, var1);
         int var10000 = var4.sprId;
         String var5 = var10000 + ":" + Arrays.toString(var4.imageIds);
         SpriteData var6 = (SpriteData)CACHE.computeIfAbsent(var5, (var2x) -> SpriteData.load(var2, var4.sprId, var4.imageIds));
         return new SpriteAnimator(var6, var3);
      }
   }

   private static SpriteTable sourceSpriteTable() {
      SpriteTable var0 = sourceSpriteTable;
      if (var0 != null) {
         return var0;
      } else {
         synchronized(SpriteAnimator.class) {
            var0 = sourceSpriteTable;
            if (var0 == null) {
               var0 = loadSourceSpriteTable();
               if (var0 != null) {
                  sourceSpriteTable = var0;
               }
            }

            return var0;
         }
      }
   }

   public void setState(int var1) {
      if (this.data.anim != null && var1 >= 0 && var1 < this.data.anim.length) {
         this.state = var1;
      } else {
         this.state = 0;
      }

      this.cursor = 0;
      this.resetDelay();
   }

   public void setAnimation(byte var1, byte var2, boolean var3) {
      if (this.state == var1 && !var3) {
         this.resetDelay();
      } else {
         this.setState(var1);
      }

      this.endBehavior = var2;
   }

   public int state() {
      return this.state;
   }

   public int cursor() {
      return this.cursor;
   }

   public void setCursor(int var1) {
      this.cursor = var1;
   }

   public void setCursorClamped(int var1) {
      this.cursor = Math.max(0, Math.min(var1, Math.max(0, this.frameCount() - 1)));
   }

   public boolean animationAvailable() {
      return this.animationAvailable(this.state);
   }

   public boolean animationAvailable(int var1) {
      return this.data.anim != null && var1 >= 0 && var1 < this.data.anim.length && this.data.anim[var1].length > 0;
   }

   public int frameCount() {
      return this.frameCount(this.state);
   }

   public int frameCount(int var1) {
      return this.animationAvailable(var1) ? this.data.anim[var1].length / this.frameStride() : 0;
   }

   public int lastFrameIndex() {
      return Math.max(0, this.frameCount() - 1);
   }

   public boolean isOnLastFrame() {
      int var1 = this.frameCount();
      return var1 <= 1 || this.cursor >= var1 - 1;
   }

   public int cellIdAtFrame(int var1) {
      return this.cellIdAtFrame(this.state, var1);
   }

   public int cellIdAtFrame(int var1, int var2) {
      int var3 = this.frameCount(var1);
      if (var3 <= 0) {
         return -1;
      } else {
         int var4 = Math.max(0, Math.min(var2, var3 - 1));
         return this.cellId(var1, var4);
      }
   }

   public int animationDurationTicks(int var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < this.frameCount(var1); ++var3) {
         var2 += Math.max(1, this.frameDuration(var1, var3));
      }

      return var2;
   }

   public int cursorAtElapsedTickHoldLast(int var1, int var2) {
      int var3 = this.frameCount(var1);
      if (var3 <= 0) {
         return 0;
      } else {
         int var4 = Math.max(0, var2);

         for(int var5 = 0; var5 < var3; ++var5) {
            int var6 = Math.max(1, this.frameDuration(var1, var5));
            if (var4 < var6) {
               return var5;
            }

            var4 -= var6;
         }

         return var3 - 1;
      }
   }

   public int cursorAtElapsedTick(int var1) {
      if (!this.animationAvailable()) {
         return 0;
      } else {
         int var2 = Math.max(0, var1);
         int var3 = 0;

         for(int var4 = 0; var4 < this.frameCount(); ++var4) {
            var3 += Math.max(1, this.frameDuration(this.state, var4));
         }

         int var7 = var3 <= 0 ? 0 : var2 % var3;
         int var5 = 0;

         for(int var6 = 0; var6 < this.frameCount(); ++var6) {
            var5 += Math.max(1, this.frameDuration(this.state, var6));
            if (var7 < var5) {
               return var6;
            }
         }

         return 0;
      }
   }

   public boolean tick() {
      if (!this.animationAvailable()) {
         return false;
      } else if (this.delay > 0) {
         --this.delay;
         return false;
      } else {
         ++this.cursor;
         if (this.cursor >= this.frameCount()) {
            if (this.endBehavior >= 0) {
               this.setState(this.endBehavior);
            } else if (this.endBehavior == -2) {
               this.cursor = Math.max(0, this.frameCount() - 1);
               this.resetDelay();
            } else {
               this.cursor = 0;
               this.resetDelay();
            }

            return true;
         } else {
            this.resetDelay();
            return false;
         }
      }
   }

   public void tickHoldLast() {
      if (this.animationAvailable()) {
         int var1 = this.frameCount() - 1;
         if (this.cursor < var1) {
            if (this.delay > 0) {
               --this.delay;
            } else {
               ++this.cursor;
               this.resetDelay();
            }
         }
      }
   }

   public void resetDelay() {
      if (this.animationAvailable() && this.cursor >= 0 && this.cursor < this.frameCount()) {
         this.delay = Math.max(0, this.frameDuration(this.state, this.cursor) - 1);
      } else {
         this.delay = 0;
      }
   }

   public void draw(Graphics2D var1, int var2, int var3, int var4) {
      if (this.animationAvailable()) {
         this.drawCell(var1, this.cellId(this.state, this.cursor), var2, var3, var4);
      }
   }

   public void drawAligned(Graphics2D var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      int[] var8 = this.animationBounds(this.state);
      if (var8 != null) {
         int var9;
         int var10;
         switch (var6) {
            case 0:
            default:
               var9 = var2 - var8[0];
               var10 = var3 - var8[1];
               break;
            case 1:
               var9 = var2 + (var4 - var8[2]) / 2 - var8[0];
               var10 = var3 - var8[1];
               break;
            case 2:
               var9 = var2 + (var4 - var8[2]) - var8[0];
               var10 = var3 - var8[1];
               break;
            case 3:
               var9 = var2 - var8[0];
               var10 = var3 + (var5 - var8[3]) / 2 - var8[1];
               break;
            case 4:
               var9 = var2 + (var4 - var8[2]) / 2 - var8[0];
               var10 = var3 + (var5 - var8[3]) / 2 - var8[1];
               break;
            case 5:
               var9 = var2 + (var4 - var8[2]) - var8[0];
               var10 = var3 + (var5 - var8[3]) / 2 - var8[1];
               break;
            case 6:
               var9 = var2 - var8[0];
               var10 = var3 + (var5 - var8[3]) - var8[1];
               break;
            case 7:
               var9 = var2 + (var4 - var8[2]) / 2 - var8[0];
               var10 = var3 + (var5 - var8[3]) - var8[1];
               break;
            case 8:
               var9 = var2 + (var4 - var8[2]) - var8[0];
               var10 = var3 + (var5 - var8[3]) - var8[1];
         }

         this.draw(var1, var9, var10, var7);
      }
   }

   public int[] cellBounds(int var1) {
      return this.data.cellBounds(var1);
   }

   public boolean cellAvailable(int var1) {
      return var1 >= 0 && var1 < this.data.cells.length;
   }

   public int cellPartCount(int var1) {
      return this.cellAvailable(var1) ? this.data.cells[var1].length / 4 : 0;
   }

   public int currentCellId() {
      if (!this.animationAvailable()) {
         return -1;
      } else {
         int var1 = Math.max(0, Math.min(this.cursor, this.frameCount() - 1));
         return this.cellId(this.state, var1);
      }
   }

   public int[] currentCellBounds() {
      return this.data.cellBounds(this.currentCellId());
   }

   public short[] currentCollisionMask() {
      return this.animationAvailable() ? this.data.collisionMask(this.cellId(this.state, this.cursor)) : null;
   }

   public short[] currentHitMask() {
      return this.animationAvailable() ? this.data.hitMask(this.cellId(this.state, this.cursor)) : null;
   }

   public int[] animationBounds(int var1) {
      if (!this.animationAvailable(var1)) {
         return null;
      } else {
         int var2 = Integer.MAX_VALUE;
         int var3 = Integer.MAX_VALUE;
         int var4 = Integer.MIN_VALUE;
         int var5 = Integer.MIN_VALUE;
         int var6 = this.frameCount(var1);

         for(int var7 = 0; var7 < var6; ++var7) {
            int[] var8 = this.data.cellBounds(this.cellId(var1, var7));
            if (var8 != null) {
               var2 = Math.min(var2, var8[0]);
               var3 = Math.min(var3, var8[1]);
               var4 = Math.max(var4, var8[0] + var8[2]);
               var5 = Math.max(var5, var8[1] + var8[3]);
            }
         }

         if (var2 == Integer.MAX_VALUE) {
            return null;
         } else {
            return new int[]{var2, var3, var4 - var2, var5 - var3};
         }
      }
   }

   public void drawCell(Graphics2D var1, int var2, int var3, int var4, int var5) {
      if (var2 >= 0 && var2 < this.data.cells.length) {
         int[] var6 = var5 == 1 ? new int[]{2, 4, 1, 7, 0, 5, 3, 6} : new int[]{0, 5, 3, 6, 2, 4, 1, 7};
         short[] var7 = this.data.cells[var2];

         for(int var8 = 0; var8 < var7.length; var8 += 4) {
            short var9 = var7[var8];
            if (var9 >= 0 && var9 < this.data.frames.length) {
               short var10 = var7[var8 + 1];
               short var11 = var7[var8 + 2];
               int var12 = var6[var7[var8 + 3] & 7];
               if (var5 == 1) {
                  short var13 = this.data.frames[var9][3];
                  short var14 = this.data.frames[var9][4];
                  short var15 = var7[var8 + 3] % 2 == 1 ? var14 : var13;
                  drawRegion(var1, this.data.imageForFrame(var9), this.data.frames[var9], var12, var3 - var10 - var15, var4 + var11);
               } else {
                  drawRegion(var1, this.data.imageForFrame(var9), this.data.frames[var9], var12, var3 + var10, var4 + var11);
               }
            }
         }

      }
   }

   private int frameStride() {
      return this.extendedAnimation ? 4 : 2;
   }

   private int frameDuration(int var1, int var2) {
      return this.data.anim[var1][var2 * this.frameStride()];
   }

   private int cellId(int var1, int var2) {
      return this.data.anim[var1][var2 * this.frameStride() + 1];
   }

   private static void drawRegion(Graphics2D var0, BufferedImage var1, short[] var2, int var3, int var4, int var5) {
      short var6 = var2[1];
      short var7 = var2[2];
      short var8 = var2[3];
      short var9 = var2[4];
      if (var8 > 0 && var9 > 0) {
         int var10 = var3 >= 0 && var3 <= 7 ? var3 : 0;
         MidpTransform.drawRegion(var0, var1, var6, var7, var8, var9, var10, var4, var5);
      }
   }

   private static SpriteTable loadSourceSpriteTable() {
      try {
         return SpriteTable.load(AssetPaths.fromWorkingTree(GameConfig.defaultConfig()));
      } catch (RuntimeException var1) {
         return null;
      }
   }

   private static int[][] fallbackImageMappings() {
      int[][] var0 = new int[400][];
      int[][] var1 = new int[][]{{0, 0, 100}, {8, 8, 108}, {30, 30, 126}, {13, 13, 113}, {17, 17, 117}, {29, 29, 126}, {31, 31, 126}, {32, 32, 126}, {7, 7, 108}, {81, 81, 159}, {148, 148, 529}, {200, 200, 219}, {202, 202, 222}, {203, 203, 221}, {204, 204, 221}, {205, 205, 221}, {223, 223, 10023}, {225, 225, 218}, {243, 243, 232}, {244, 244, 232}, {289, 289, 259}, {328, 328, 820}, {65, 65, 145}, {270, 270, 250}, {271, 271, 249}, {273, 273, 251}, {275, 275, 254}, {314, 314, 249}, {342, 342, 839}, {23, 23, 123}, {25, 25, 124}, {50, 50, 136}, {51, 51, 136}, {52, 52, 136}, {53, 53, 137}, {54, 54, 137}, {66, 66, 146}, {69, 69, 149}, {92, 92, 506}, {102, 102, 574}, {137, 137, 520}, {191, 191, 209}, {198, 198, 212}, {201, 201, 220}, {208, 208, 220}, {209, 209, 220}, {213, 213, 223}, {230, 230, 217}, {339, 339, 836}, {84, 84, 162}, {85, 85, 163}, {101, 101, 604}, {117, 117, 605}, {133, 133, 606}, {149, 149, 607}, {161, 161, 608}, {173, 173, 609}, {185, 185, 610}, {262, 262, 300}, {264, 264, 305}, {266, 266, 303}, {83, 83, 161}, {247, 247, 238}, {259, 259, 811}, {282, 282, 261}, {284, 284, 261}, {265, 265, 301}, {267, 267, 307}, {326, 326, 164}, {327, 327, 818, 819}};

      for(int[] var5 : var1) {
         var0[var5[0]] = Arrays.copyOfRange(var5, 2, var5.length);
      }

      return var0;
   }

   private static final class SpriteRef {
      final int sprId;
      final int[] imageIds;

      SpriteRef(int var1, int[] var2) {
         this.sprId = var1;
         this.imageIds = var2;
      }

      static SpriteRef from(int var0, SpriteTable var1) {
         if (var1 != null && var0 >= 0 && var0 < var1.size()) {
            int var2 = var1.sprId(var0);
            int[] var3 = var1.imageIds(var0);
            if (var2 >= 0 && var3.length > 0) {
               return new SpriteRef(var2, var3);
            }
         }

         int[] var4 = var0 >= 0 && var0 < SpriteAnimator.SPRITE_TO_IMGS.length ? SpriteAnimator.SPRITE_TO_IMGS[var0] : null;
         return new SpriteRef(var0, var4 == null ? new int[0] : var4);
      }
   }
}
