package vqsv.pet.data;

public final class TanNguyetLongMaCatalog {
   public static final int RUNTIME_ID = 278;
   public static final int VISUAL_ID = 3004;
   public static final String STABLE_KEY = "BOSS-TAN-NGUYET-LONG-MA";
   public static final String NAME = "Tàn Nguyệt Long Ma";

   private TanNguyetLongMaCatalog() {}

   public static boolean isSpecies(int id) { return id == RUNTIME_ID; }
   public static boolean isVisual(int id) { return id == VISUAL_ID; }
   public static Record record() { return new Record(); }

   public static final class Record {
      public final int runtimeId = RUNTIME_ID;
      public final String stableKey = STABLE_KEY;
      public final String name = NAME;
      public final int elementId = 5;
      public final int defaultQuality = 5;
      public final int rarity = 5;
      public final int visualId = VISUAL_ID;

      public int stat(int stat, int level, int quality) {
         return switch (stat) {
            case 0 -> 5000;
            case 1 -> 600;
            case 2 -> 300;
            case 3 -> 40;
            default -> throw new IllegalArgumentException("Unknown stat " + stat);
         };
      }
   }
}
