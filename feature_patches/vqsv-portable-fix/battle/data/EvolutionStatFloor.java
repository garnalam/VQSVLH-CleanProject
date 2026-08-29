package vqsv.battle.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;
import vqsv.pet.data.UnifiedV4PetCatalog;
import vqsv.progression.UnifiedEvolutionCatalog;

public final class EvolutionStatFloor {
   private static final int SPECIES_COUNT = 279;
   private static final int[][] ANCESTORS = buildAncestors();

   private EvolutionStatFloor() {
   }

   static int apply(VqsvBattleTables var0, int var1, int var2, int var3, int var4, int var5) {
      PetStatProfileVersion var6 = var0.petStatProfileVersion();
      if (var6 != PetStatProfileVersion.UNIFIED_PET_GROWTH_V5 && var6 != PetStatProfileVersion.UNIFIED_PET_GROWTH_V6) {
         return var5;
      } else if (var6 == PetStatProfileVersion.UNIFIED_PET_GROWTH_V5 && UnifiedV4PetCatalog.isV4Species(var1)) {
         return var5;
      } else if (!LietHoaMutationCatalog.isSpecies(var1) && !ThienLuanMutationCatalog.isSpecies(var1)) {
         if (var1 >= 0 && var1 < ANCESTORS.length) {
            int var7 = var5;

            for(int var11 : ANCESTORS[var1]) {
               BattleSpeciesRow var12 = var0.species(var11);
               if (var12 == null || !var12.validForBattle()) {
                  throw new IllegalStateException("Evolution ancestor has no battle row: " + var11);
               }

               var7 = Math.max(var7, var12.rawStat(var2, var3, var4));
            }

            return var7;
         } else {
            throw new IllegalArgumentException("Evolution stat floor has no runtime species ID " + var1);
         }
      } else {
         return var5;
      }
   }

   static int ancestorCount(int var0) {
      return var0 >= 0 && var0 < ANCESTORS.length ? ANCESTORS[var0].length : 0;
   }

   public static int[] ancestors(int var0) {
      return var0 >= 0 && var0 < ANCESTORS.length ? (int[])ANCESTORS[var0].clone() : new int[0];
   }

   private static int[][] buildAncestors() {
      LinkedHashMap<Integer, List<Integer>> var0 = new LinkedHashMap<>();

      for(UnifiedEvolutionCatalog.Rule var2 : UnifiedEvolutionCatalog.instance().rules()) {
         var0.computeIfAbsent(var2.toSpeciesId, (var0x) -> new ArrayList<>()).add(var2.fromSpeciesId);
      }

      for(int var4 = 202; var4 < 274; ++var4) {
         for(UnifiedV4PetCatalog.EvolutionRule var3 : UnifiedV4PetCatalog.instance().evolutionsFrom(var4)) {
            var0.computeIfAbsent(var3.toSpeciesId, (var0x) -> new ArrayList<>()).add(var3.fromSpeciesId);
         }
      }

      var0.computeIfAbsent(HoaDiemHauVuongCatalog.RUNTIME_ID, (unused) -> new ArrayList<>()).add(HoaDiemHauVuongCatalog.SOURCE_SPECIES_ID);
      var0.computeIfAbsent(ThienViemHoCatalog.RUNTIME_ID, (unused) -> new ArrayList<>()).add(ThienViemHoCatalog.SOURCE_SPECIES_ID);

      int[][] var5 = new int[SPECIES_COUNT][];

      for(int var7 = 0; var7 < SPECIES_COUNT; ++var7) {
         LinkedHashSet<Integer> var8 = new LinkedHashSet<>();
         collectAncestors(var7, var0, var8, new ArrayDeque<>());
         var5[var7] = var8.stream().mapToInt((value) -> (Integer)value).toArray();
      }

      return var5;
   }

   private static void collectAncestors(int var0, Map<Integer, List<Integer>> var1, Set<Integer> var2, ArrayDeque<Integer> var3) {
      if (var3.contains(var0)) {
         String var10002 = String.valueOf(var3);
         throw new IllegalStateException("Cycle in unified evolution topology: " + var10002 + " -> " + var0);
      } else {
         var3.addLast(var0);

         for(int var5 : var1.getOrDefault(var0, Collections.emptyList())) {
            collectAncestors(var5, var1, var2, var3);
            var2.add(var5);
         }

         var3.removeLast();
      }
   }
}
