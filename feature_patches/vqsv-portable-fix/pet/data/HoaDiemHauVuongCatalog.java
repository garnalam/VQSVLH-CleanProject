package vqsv.pet.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import vqsv.battle.data.UnifiedSkillLearnsetCatalog;

/** Project-local evolution of LH-064, added without colliding with legacy IDs. */
public final class HoaDiemHauVuongCatalog {
   public static final String STABLE_KEY = "LH-064-EVO-01";
   public static final String NAME = "Hỏa Diễm Hầu Vương";
   public static final int SOURCE_SPECIES_ID = 64;
   public static final int RUNTIME_ID = 276;
   public static final int VISUAL_ID = 3002;
   public static final int REQUIRED_LEVEL = 40;
   public static final int DEFAULT_QUALITY = 4;
   private static final int[][] L1 = new int[][]{{108, 112, 78, 8}, {114, 118, 82, 8}, {120, 125, 87, 9}, {132, 137, 95, 10}, {150, 156, 108, 11}};
   private static final int[][] L50 = new int[][]{{1260, 306, 140, 39}, {1330, 323, 148, 41}, {1400, 340, 155, 43}, {1540, 374, 170, 47}, {1750, 425, 194, 54}};
   private static final List<SkillGrant> SKILLS = Collections.unmodifiableList(Arrays.asList(
      new SkillGrant("V4-SKILL-15", 116, 1, "beast"),
      new SkillGrant("LH-SKILL-01", 1, 5, "fire"),
      new SkillGrant("LH-SKILL-41", 41, 8, "lightning"),
      new SkillGrant("V4-SKILL-16", 117, 10, "beast"),
      new SkillGrant("LH-SKILL-03", 3, 15, "fire"),
      new SkillGrant("LH-SKILL-43", 43, 18, "lightning"),
      new SkillGrant("V4-SKILL-17", 118, 20, "beast"),
      new SkillGrant("LH-SKILL-07", 7, 25, "fire"),
      new SkillGrant("LH-SKILL-48", 48, 28, "lightning"),
      new SkillGrant("V4-SKILL-18", 119, 30, "beast"),
      new SkillGrant("LH-SKILL-09", 9, 35, "fire"),
      new SkillGrant("LH-SKILL-47", 47, 38, "lightning"),
      new SkillGrant("V4-SKILL-19", 120, 40, "beast"),
      new SkillGrant("LH-SKILL-49", 49, 45, "lightning"),
      new SkillGrant("LH-SKILL-08", 8, 50, "fire")
   ));

   private HoaDiemHauVuongCatalog() {}

   public static boolean isSpecies(int id) { return id == RUNTIME_ID; }
   public static boolean isSourceSpecies(int id) { return id == SOURCE_SPECIES_ID; }
   public static boolean isVisual(int id) { return id == VISUAL_ID; }
   public static Record record() { return new Record(); }
   public static List<SkillGrant> skills() { return SKILLS; }

   public static PetTaxonomyEntry taxonomyEntry() {
      return new PetTaxonomyEntry(RUNTIME_ID, STABLE_KEY, PetCombatElement.HOA, PetSpecies.MANH_THU, 11, "MT-04", "unified_design");
   }

   public static final class Record {
      public final int runtimeId = RUNTIME_ID;
      public final String stableKey = STABLE_KEY;
      public final String name = NAME;
      public final int elementId = 0;
      public final int speciesId = 3;
      public final int defaultQuality = DEFAULT_QUALITY;
      public final int rarity = 4;
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
         this.skillKey = key;
         this.runtimeSkillId = id;
         this.unlockLevel = level;
         this.grantSet = set;
      }

      public UnifiedSkillLearnsetCatalog.Entry entry(int order) {
         return UnifiedSkillLearnsetCatalog.Entry.of(STABLE_KEY, this.skillKey, this.runtimeSkillId, this.grantSet, "evolution", STABLE_KEY, this.unlockLevel, "unified_design", this.grantSet, false, order, "unified_design");
      }
   }
}
