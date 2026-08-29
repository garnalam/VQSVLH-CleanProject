package vqsv.battle.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;
import vqsv.pet.data.TanNguyetLongMaCatalog;
import vqsv.pet.data.UnifiedV4PetCatalog;

public final class UnifiedSkillLearnsetCatalog {
   public static final int EVOLUTION_COUNT = 133;
   private static final UnifiedSkillLearnsetCatalog INSTANCE = load();
   private final Map<String, List<Entry>> byPet;
   private final Map<String, List<String>> parentsByPet;
   private final Map<String, List<String>> childrenByPet;
   private final Set<String> nonInheritingEdges;
   private final int count;

   private UnifiedSkillLearnsetCatalog(Map<String, List<Entry>> var1, Map<String, List<String>> var2, Map<String, List<String>> var3, Set<String> var4, int var5) {
      this.byPet = Collections.unmodifiableMap(var1);
      this.parentsByPet = Collections.unmodifiableMap(var2);
      this.childrenByPet = Collections.unmodifiableMap(var3);
      this.nonInheritingEdges = Collections.unmodifiableSet(var4);
      this.count = var5;
   }

   public static UnifiedSkillLearnsetCatalog instance() {
      return INSTANCE;
   }

   public int count() {
      return this.count;
   }

   public Set<String> petKeys() {
      return this.byPet.keySet();
   }

   public List<Entry> entries(String var1) {
      if (ThienViemHoCatalog.STABLE_KEY.equals(var1)) {
         List<Entry> entries = new ArrayList<>();
         int order = 0;
         for(ThienViemHoCatalog.SkillGrant grant : ThienViemHoCatalog.skills()) {
            entries.add(grant.entry(order++));
         }
         return Collections.unmodifiableList(entries);
      } else if (HoaDiemHauVuongCatalog.STABLE_KEY.equals(var1)) {
         List<Entry> entries = new ArrayList<>();
         int order = 0;
         for(HoaDiemHauVuongCatalog.SkillGrant grant : HoaDiemHauVuongCatalog.skills()) {
            entries.add(grant.entry(order++));
         }
         return Collections.unmodifiableList(entries);
      } else if ("LH-023-MUT-01".equals(var1)) {
         List<Entry> var7 = new ArrayList<>();
         int var8 = 0;

         for(LietHoaMutationCatalog.SkillGrant var10 : LietHoaMutationCatalog.skills()) {
            var7.add(var10.entry(var8++));
         }

         return Collections.unmodifiableList(var7);
      } else if (!"LH-034-MUT-01".equals(var1)) {
         List<Entry> var6 = this.byPet.get(var1);
         return var6 == null ? Collections.emptyList() : var6;
      } else {
         List<Entry> var2 = new ArrayList<>();
         int var3 = 0;

         for(ThienLuanMutationCatalog.SkillGrant var5 : ThienLuanMutationCatalog.skills()) {
            var2.add(var5.entry(var3++));
         }

         return Collections.unmodifiableList(var2);
      }
   }

   public List<Entry> accessibleEntries(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         List<Entry> var2 = new ArrayList<>();
         HashSet<String> var3 = new HashSet<>();
         HashSet<Integer> var4 = new HashSet<>();
         this.collectAccessible(var1, var1, var3, var4, var2);
         var2.sort(Comparator.<Entry>comparingInt((var0) -> var0.unlockLevel).thenComparingInt((var0) -> var0.sortOrder).thenComparing((var0) -> var0.skillKey));
         return Collections.unmodifiableList(var2);
      } else {
         return Collections.emptyList();
      }
   }

   public List<Entry> progressionEntries(String var1) {
      List<Entry> var2 = new ArrayList<>(this.accessibleEntries(var1));
      HashSet<Integer> var3 = new HashSet<>();

      for(Entry var5 : var2) {
         var3.add(var5.runtimeSkillId);
      }

      this.collectDescendantEntries(var1, new HashSet<>(), var3, var2);
      var2.sort(Comparator.<Entry>comparingInt((var0) -> var0.unlockLevel).thenComparingInt((var0) -> var0.sortOrder).thenComparing((var0) -> var0.skillKey));
      return Collections.unmodifiableList(var2);
   }

   public List<Entry> familyEntries(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         LinkedHashSet<String> var2 = new LinkedHashSet<>();
         this.collectRoots(var1, new HashSet<>(), var2);
         List<Entry> var3 = new ArrayList<>();
         HashSet<Integer> var4 = new HashSet<>();

         for(String var6 : var2) {
            for(Entry var8 : this.entries(var6)) {
               if (var4.add(var8.runtimeSkillId)) {
                  var3.add(var8);
               }
            }

            this.collectDescendantEntries(var6, new HashSet<>(), var4, var3);
         }

         var3.sort(Comparator.<Entry>comparingInt((var0) -> var0.unlockLevel).thenComparingInt((var0) -> var0.sortOrder).thenComparing((var0) -> var0.skillKey));
         return Collections.unmodifiableList(var3);
      } else {
         return Collections.emptyList();
      }
   }

   public int[] unlockedCandidateIds(String var1, int var2, int[] var3) {
      List<Integer> var4 = new ArrayList<>();

      for(Entry var6 : this.accessibleEntries(var1)) {
         if (var6.unlockLevel <= var2 && !contains(var3, var6.runtimeSkillId)) {
            var4.add(var6.runtimeSkillId);
         }
      }

      return var4.stream().mapToInt((value) -> (Integer)value).toArray();
   }

   public int[] newlyUnlockedCandidateIds(String var1, int var2, int[] var3) {
      return this.newlyUnlockedCandidateIds(var1, var2 - 1, var2, var3);
   }

   public int[] newlyUnlockedCandidateIds(String var1, int var2, int var3, int[] var4) {
      List<Integer> var5 = new ArrayList<>();
      int var6 = Math.min(var2, var3);
      int var7 = Math.max(var2, var3);

      for(Entry var9 : this.accessibleEntries(var1)) {
         if (var9.unlockLevel > var6 && var9.unlockLevel <= var7 && !contains(var4, var9.runtimeSkillId)) {
            var5.add(var9.runtimeSkillId);
         }
      }

      return distinctIds(var5);
   }

   public int[] evolutionUnlockedCandidateIds(String var1, String var2, int var3, int[] var4) {
      HashSet<Integer> var5 = new HashSet<>();

      for(Entry var7 : this.accessibleEntries(var1)) {
         var5.add(var7.runtimeSkillId);
      }

      List<Integer> var9 = new ArrayList<>();

      for(Entry var8 : this.accessibleEntries(var2)) {
         if (var8.unlockLevel <= var3 && !var5.contains(var8.runtimeSkillId) && !contains(var4, var8.runtimeSkillId)) {
            var9.add(var8.runtimeSkillId);
         }
      }

      return distinctIds(var9);
   }

   public boolean inheritsSkills(String var1, String var2) {
      if (!"LH-023-MUT-01".equals(var2) && !"LH-034-MUT-01".equals(var2)) {
         return !this.nonInheritingEdges.contains(edgeKey(var1, var2));
      } else {
         return false;
      }
   }

   public int[] initialSkillIds(String var1, int var2) {
      List<Entry> var3 = this.accessibleEntries(var1);
      List<Integer> var4 = new ArrayList<>();

      for(Entry var6 : var3) {
         if (var6.unlockLevel <= var2) {
            var4.add(var6.runtimeSkillId);
         }
      }

      if (var4.isEmpty() && !var3.isEmpty()) {
         var4.add(var3.get(0).runtimeSkillId);
      }

      int[] var7 = distinctIds(var4);
      return var7.length <= 5 ? var7 : Arrays.copyOf(var7, 5);
   }

   public boolean isAncestor(String var1, String var2) {
      if (var1 != null && var2 != null) {
         HashSet<String> var3 = new HashSet<>();
         List<String> var4 = new ArrayList<>();
         var4.add(var2);

         while(!var4.isEmpty()) {
            String var5 = var4.remove(var4.size() - 1);
            if (var3.add(var5)) {
               if (var1.equals(var5)) {
                  return true;
               }

               for(String var7 : this.parentsByPet.getOrDefault(var5, Collections.emptyList())) {
                  if (!this.nonInheritingEdges.contains(edgeKey(var7, var5))) {
                     var4.add(var7);
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public int evolutionDistance(String var1, String var2) {
      if (var1 != null && !var1.isEmpty() && var2 != null && !var2.isEmpty()) {
         HashSet<String> var3 = new HashSet<>();
         List<String> var4 = new ArrayList<>();
         List<Integer> var5 = new ArrayList<>();
         var4.add(var2);
         var5.add(0);

         for(int var6 = 0; var6 < var4.size(); ++var6) {
            String var7 = var4.get(var6);
            int var8 = var5.get(var6);
            if (var3.add(var7)) {
               if (var1.equals(var7)) {
                  return var8;
               }

               for(String var10 : this.parentsByPet.getOrDefault(var7, Collections.emptyList())) {
                  var4.add(var10);
                  var5.add(var8 + 1);
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static String lietHoaPetKey(int var0) {
      return var0 >= 0 && var0 < 100 ? String.format("LH-%03d", var0) : "";
   }

   public static String petKey(int var0) {
      if (TanNguyetLongMaCatalog.isSpecies(var0)) {
         return "LH-071";
      } else if (ThienViemHoCatalog.isSpecies(var0)) {
         return ThienViemHoCatalog.STABLE_KEY;
      } else if (HoaDiemHauVuongCatalog.isSpecies(var0)) {
         return HoaDiemHauVuongCatalog.STABLE_KEY;
      } else if (LietHoaMutationCatalog.isSpecies(var0)) {
         return "LH-023-MUT-01";
      } else if (ThienLuanMutationCatalog.isSpecies(var0)) {
         return "LH-034-MUT-01";
      } else if (var0 >= 0 && var0 < 100) {
         return String.format("LH-%03d", var0);
      } else if (var0 >= 100 && var0 < 202) {
         return String.format("CV-%03d", var0 - 100);
      } else if (UnifiedV4PetCatalog.isV4Species(var0)) {
         String var1 = UnifiedV4PetCatalog.instance().petKey(var0);
         return var1 == null ? "" : var1;
      } else {
         return "";
      }
   }

   private static UnifiedSkillLearnsetCatalog load() {
      LinkedHashMap<String, List<Entry>> var0 = new LinkedHashMap<>();
      int var1 = 0;

      try {
         BufferedReader var2 = reader();

         try {
            String var3 = var2.readLine();
            if (var3 == null) {
               throw new IllegalStateException("Empty unified learnset resource");
            }

            Header var4 = new Header(var3);

            String var5;
            while((var5 = var2.readLine()) != null) {
               if (!var5.isEmpty()) {
                  Entry var6 = new Entry(var5.split("\\t", -1), var4);
                  validate(var6);
                  var0.computeIfAbsent(var6.petKey, (var0x) -> new ArrayList<>()).add(var6);
                  ++var1;
               }
            }
         } catch (Throwable var8) {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (var2 != null) {
            var2.close();
         }
      } catch (IOException var9) {
         throw new IllegalStateException("Cannot load unified learnsets", var9);
      }

      if (var1 != 2337) {
         throw new IllegalStateException("Expected 2337 learnset rows, got " + var1);
      } else {
         LinkedHashMap<String, List<Entry>> var10 = new LinkedHashMap<>();

         for(Map.Entry<String, List<Entry>> var13 : var0.entrySet()) {
            var10.put(var13.getKey(), Collections.unmodifiableList(var13.getValue()));
         }

         EvolutionTopology var12 = loadEvolutionTopology();
         return new UnifiedSkillLearnsetCatalog(var10, var12.parents, var12.children, var12.nonInheritingEdges, var1);
      }
   }

   private static EvolutionTopology loadEvolutionTopology() {
      LinkedHashMap<String, List<String>> var0 = new LinkedHashMap<>();
      LinkedHashMap<String, List<String>> var1 = new LinkedHashMap<>();
      HashSet<String> var2 = new HashSet<>();
      int var3 = 0;

      try {
         BufferedReader var4 = evolutionReader();

         try {
            String var5 = var4.readLine();
            if (var5 == null) {
               throw new IllegalStateException("Empty unified learnset evolution resource");
            }

            Header var6 = new Header(var5);

            String var7;
            while((var7 = var4.readLine()) != null) {
               if (!var7.isEmpty()) {
                  String[] var8 = var7.split("\\t", -1);
                  String var9 = var6.text(var8, "from_pet_key");
                  String var10 = var6.text(var8, "to_pet_key");
                  if (var9.isEmpty() || var10.isEmpty() || var9.equals(var10)) {
                     throw new IllegalStateException("Invalid unified evolution edge " + var9 + "->" + var10);
                  }

                  var0.computeIfAbsent(var10, (var0x) -> new ArrayList<>()).add(var9);
                  var1.computeIfAbsent(var9, (var0x) -> new ArrayList<>()).add(var10);
                  if ("0".equals(var6.text(var8, "inherits_skills"))) {
                     var2.add(edgeKey(var9, var10));
                  }

                  ++var3;
               }
            }
         } catch (Throwable var12) {
            if (var4 != null) {
               try {
                  var4.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (var4 != null) {
            var4.close();
         }
      } catch (IOException var13) {
         throw new IllegalStateException("Cannot load unified learnset evolution topology", var13);
      }

      if (var3 != 133) {
         throw new IllegalStateException("Expected 133 evolution edges, got " + var3);
      } else {
         return new EvolutionTopology(freezeTopology(var0), freezeTopology(var1), var2);
      }
   }

   private static Map<String, List<String>> freezeTopology(Map<String, List<String>> var0) {
      LinkedHashMap<String, List<String>> var1 = new LinkedHashMap<>();

      for(Map.Entry<String, List<String>> var3 : var0.entrySet()) {
         var1.put(var3.getKey(), Collections.unmodifiableList(var3.getValue()));
      }

      return Collections.unmodifiableMap(var1);
   }

   private static void validate(Entry var0) {
      if (!var0.petKey.isEmpty() && !var0.skillKey.isEmpty() && !var0.requiredFormPetKey.isEmpty() && var0.unlockLevel >= 1 && var0.unlockLevel <= 50) {
         if (!var0.grantSet.equals("legacy-v5") && !var0.grantSet.equals("vqsv4-additive-v1") && !var0.grantSet.equals("vqsv4-native-v1") && !var0.grantSet.equals("vqsv4-bridge-v1")) {
            throw new IllegalStateException("Invalid learnset grant set " + var0.grantSet);
         } else if (!var0.progressionGrade.equals("basic") && !var0.progressionGrade.equals("advanced") && !var0.progressionGrade.equals("elite") && !var0.progressionGrade.equals("ultimate")) {
            throw new IllegalStateException("Invalid learnset progression grade " + var0.progressionGrade);
         } else {
            if (!var0.skillKey.startsWith("CV-SKILL-") && !var0.skillKey.startsWith("V4-SKILL-")) {
               if (!var0.skillKey.startsWith("LH-SKILL-") || var0.runtimeSkillId < 0 || var0.runtimeSkillId >= 70) {
                  throw new IllegalStateException("Invalid Liệt Hỏa Skill in learnset " + var0.skillKey);
               }
            } else {
               UnifiedSkillRecord var1 = UnifiedSkillCatalog.instance().byRuntimeId(var0.runtimeSkillId);
               if (var1 == null || !var1.stableKey.equals(var0.skillKey)) {
                  throw new IllegalStateException("Inactive/unmapped CV Skill in learnset " + var0.skillKey);
               }
            }

         }
      } else {
         throw new IllegalStateException("Invalid unified learnset row " + var0.skillKey);
      }
   }

   private static BufferedReader reader() {
      InputStream var0 = UnifiedSkillLearnsetCatalog.class.getResourceAsStream("/vqsv/data/unified/skills/unified-skill-v8/learnsets.tsv");
      if (var0 == null) {
         throw new IllegalStateException("Missing unified learnsets.tsv");
      } else {
         return new BufferedReader(new InputStreamReader(var0, StandardCharsets.UTF_8));
      }
   }

   private static BufferedReader evolutionReader() {
      InputStream var0 = UnifiedSkillLearnsetCatalog.class.getResourceAsStream("/vqsv/data/unified/skills/unified-skill-v8/learnset-evolutions.tsv");
      if (var0 == null) {
         throw new IllegalStateException("Missing unified learnset-evolutions.tsv");
      } else {
         return new BufferedReader(new InputStreamReader(var0, StandardCharsets.UTF_8));
      }
   }

   private void collectAccessible(String var1, String var2, Set<String> var3, Set<Integer> var4, List<Entry> var5) {
      if (var3.add(var1)) {
         for(String var7 : this.parentsByPet.getOrDefault(var1, Collections.emptyList())) {
            if (!this.nonInheritingEdges.contains(edgeKey(var7, var1))) {
               this.collectAccessible(var7, var2, var3, var4, var5);
            }
         }

         for(Entry var9 : this.entries(var1)) {
            if (this.isAncestor(var9.requiredFormPetKey, var2) && var4.add(var9.runtimeSkillId)) {
               var5.add(var9);
            }
         }

      }
   }

   private void collectDescendantEntries(String var1, Set<String> var2, Set<Integer> var3, List<Entry> var4) {
      if (var2.add(var1)) {
         for(String var6 : this.childrenByPet.getOrDefault(var1, Collections.emptyList())) {
            if (!this.nonInheritingEdges.contains(edgeKey(var1, var6))) {
               for(Entry var8 : this.entries(var6)) {
                  if (var3.add(var8.runtimeSkillId)) {
                     var4.add(var8);
                  }
               }

               this.collectDescendantEntries(var6, var2, var3, var4);
            }
         }

      }
   }

   private void collectRoots(String var1, Set<String> var2, Set<String> var3) {
      if (var2.add(var1)) {
         List<String> var4 = this.parentsByPet.getOrDefault(var1, Collections.emptyList());
         List<String> var5 = new ArrayList<>();

         for(String var7 : var4) {
            if (!this.nonInheritingEdges.contains(edgeKey(var7, var1))) {
               var5.add(var7);
            }
         }

         if (var5.isEmpty()) {
            var3.add(var1);
         } else {
            for(String var9 : var5) {
               this.collectRoots(var9, var2, var3);
            }

         }
      }
   }

   private static int[] distinctIds(List<Integer> var0) {
      LinkedHashSet<Integer> var1 = new LinkedHashSet<>(var0);
      return var1.stream().mapToInt((value) -> (Integer)value).toArray();
   }

   private static String edgeKey(String var0, String var1) {
      return var0 + "->" + var1;
   }

   private static boolean contains(int[] var0, int var1) {
      if (var0 == null) {
         return false;
      } else {
         for(int var5 : var0) {
            if (var5 == var1) {
               return true;
            }
         }

         return false;
      }
   }

   private static final class EvolutionTopology {
      private final Map<String, List<String>> parents;
      private final Map<String, List<String>> children;
      private final Set<String> nonInheritingEdges;

      private EvolutionTopology(Map<String, List<String>> var1, Map<String, List<String>> var2, Set<String> var3) {
         this.parents = var1;
         this.children = var2;
         this.nonInheritingEdges = Collections.unmodifiableSet(new HashSet(var3));
      }
   }

   public static final class Entry {
      public final String petKey;
      public final String skillKey;
      public final int runtimeSkillId;
      public final String grantSet;
      public final String progressionGrade;
      public final String requiredFormPetKey;
      public final int unlockLevel;
      public final String sourceType;
      public final String combatRole;
      public final boolean signature;
      public final int sortOrder;
      public final String provenance;

      private Entry(String[] var1, Header var2) {
         this.petKey = var2.text(var1, "pet_key");
         this.skillKey = var2.text(var1, "skill_key");
         this.runtimeSkillId = var2.integer(var1, "runtime_skill_id");
         this.grantSet = var2.text(var1, "grant_set");
         this.progressionGrade = var2.text(var1, "progression_grade");
         this.requiredFormPetKey = var2.text(var1, "required_form_pet_key");
         this.unlockLevel = var2.integer(var1, "unlock_level");
         this.sourceType = var2.text(var1, "source_type");
         this.combatRole = var2.text(var1, "combat_role");
         this.signature = var2.integer(var1, "is_signature") == 1;
         this.sortOrder = var2.integer(var1, "sort_order");
         this.provenance = var2.text(var1, "provenance");
      }

      public static Entry of(String var0, String var1, int var2, String var3, String var4, String var5, int var6, String var7, String var8, boolean var9, int var10, String var11) {
         return new Entry(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
      }

      private Entry(String var1, String var2, int var3, String var4, String var5, String var6, int var7, String var8, String var9, boolean var10, int var11, String var12) {
         this.petKey = var1;
         this.skillKey = var2;
         this.runtimeSkillId = var3;
         this.grantSet = var4;
         this.progressionGrade = var5;
         this.requiredFormPetKey = var6;
         this.unlockLevel = var7;
         this.sourceType = var8;
         this.combatRole = var9;
         this.signature = var10;
         this.sortOrder = var11;
         this.provenance = var12;
      }
   }

   private static final class Header {
      private final Map<String, Integer> indexes = new LinkedHashMap();

      private Header(String var1) {
         String[] var2 = var1.split("\\t", -1);

         for(int var3 = 0; var3 < var2.length; ++var3) {
            this.indexes.put(var2[var3], var3);
         }

      }

      private String text(String[] var1, String var2) {
         Integer var3 = (Integer)this.indexes.get(var2);
         if (var3 != null && var3 < var1.length) {
            return var1[var3];
         } else {
            throw new IllegalStateException("Missing learnset column " + var2);
         }
      }

      private int integer(String[] var1, String var2) {
         return Integer.parseInt(this.text(var1, var2));
      }
   }
}
