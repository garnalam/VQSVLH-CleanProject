package vqsv.pet.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import vqsv.battle.data.UnifiedSkillLearnsetCatalog;

/** Project-local evolution of LH-004. HP intentionally matches its source exactly. */
public final class ThienViemHoCatalog {
   public static final String STABLE_KEY = "LH-004-EVO-01";
   public static final String NAME = "Thiên Viêm Hồ";
   public static final int SOURCE_SPECIES_ID = 4;
   public static final int RUNTIME_ID = 277;
   public static final int VISUAL_ID = 3003;
   public static final int REQUIRED_LEVEL = 15;
   public static final int DEFAULT_QUALITY = 3;
   private static final int[][] L1 = new int[][]{{84, 94, 6, 14}, {89, 99, 6, 16}, {94, 105, 6, 16}, {103, 115, 7, 18}, {117, 131, 8, 21}};
   private static final int[][] L50 = new int[][]{{702, 315, 85, 61}, {741, 333, 90, 65}, {780, 351, 95, 68}, {858, 385, 104, 74}, {975, 438, 118, 84}};
   private static final List<SkillGrant> SKILLS = Collections.unmodifiableList(Arrays.asList(
      new SkillGrant("CV-SKILL-15", 85, 1, "beast"),
      new SkillGrant("LH-SKILL-02", 2, 5, "fire"),
      new SkillGrant("LH-SKILL-08", 8, 8, "fire"),
      new SkillGrant("CV-SKILL-16", 86, 10, "beast"),
      new SkillGrant("LH-SKILL-00", 0, 15, "fire"),
      new SkillGrant("LH-SKILL-07", 7, 18, "fire"),
      new SkillGrant("CV-SKILL-17", 87, 20, "beast"),
      new SkillGrant("LH-SKILL-01", 1, 25, "fire"),
      new SkillGrant("LH-SKILL-05", 5, 28, "fire"),
      new SkillGrant("CV-SKILL-18", 88, 30, "beast"),
      new SkillGrant("LH-SKILL-03", 3, 35, "fire"),
      new SkillGrant("CV-SKILL-31", 134, 38, "fire"),
      new SkillGrant("CV-SKILL-19", 89, 40, "beast"),
      new SkillGrant("LH-SKILL-04", 4, 45, "fire"),
      new SkillGrant("LH-SKILL-06", 6, 50, "fire")
   ));

   private ThienViemHoCatalog() {}
   public static boolean isSpecies(int id) { return id == RUNTIME_ID; }
   public static boolean isSourceSpecies(int id) { return id == SOURCE_SPECIES_ID; }
   public static boolean isVisual(int id) { return id == VISUAL_ID; }
   public static Record record() { return new Record(); }
   public static List<SkillGrant> skills() { return SKILLS; }

   public static PetTaxonomyEntry taxonomyEntry() {
      return new PetTaxonomyEntry(RUNTIME_ID, STABLE_KEY, PetCombatElement.HOA, PetSpecies.MANH_THU, 10, "MT-03", "unified_design");
   }

   public static final class Record {
      public final int runtimeId = RUNTIME_ID;
      public final String stableKey = STABLE_KEY;
      public final String name = NAME;
      public final int elementId = 0;
      public final int speciesId = 3;
      public final int defaultQuality = DEFAULT_QUALITY;
      public final int rarity = 3;
      public final int visualId = VISUAL_ID;

      public int stat(int stat, int level, int quality) {
         int q = Math.max(1, Math.min(5, quality)) - 1;
         int lv = Math.max(1, Math.min(50, level));
         return L1[q][stat] + (L50[q][stat] - L1[q][stat]) * (lv - 1) / 49;
      }
   }

   public static final class SkillGrant {
      public final String skillKey;
      public final int runtimeSkillId;
      public final int unlockLevel;
      public final String grantSet;
      SkillGrant(String key, int id, int level, String set) {
         this.skillKey = key; this.runtimeSkillId = id; this.unlockLevel = level; this.grantSet = set;
      }
      public UnifiedSkillLearnsetCatalog.Entry entry(int order) {
         return UnifiedSkillLearnsetCatalog.Entry.of(STABLE_KEY, skillKey, runtimeSkillId, grantSet, "evolution", STABLE_KEY, unlockLevel, "unified_design", grantSet, false, order, "unified_design");
      }
   }
}
