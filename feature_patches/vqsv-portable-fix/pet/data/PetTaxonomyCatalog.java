package vqsv.pet.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class PetTaxonomyCatalog {
   private static final int LIET_HOA_COUNT = 100;
   private static final int PET_COUNT = 278;
   private static final String RESOURCE = "/vqsv/data/unified/pet-traits/unified-pet-traits-v2/taxonomy.csv";
   private static final PetTaxonomyCatalog INSTANCE = load(PetTraitCatalog.instance());
   private final PetTaxonomyEntry[] entries;

   private PetTaxonomyCatalog(PetTaxonomyEntry[] var1) {
      this.entries = var1;
   }

   public static PetTaxonomyCatalog instance() {
      return INSTANCE;
   }

   public PetTraitProfileVersion profileVersion() {
      return PetTraitProfileVersion.UNIFIED_PET_TRAITS_V2;
   }

   public int recordCount() {
      return 202;
   }

   public PetTaxonomyEntry byRuntimeSpeciesId(int var1) {
      return var1 >= 0 && var1 < this.entries.length ? this.entries[var1] : null;
   }

   private static PetTaxonomyCatalog load(PetTraitCatalog var0) {
      List<List<String>> var1 = CanonicalCsvResource.read("/vqsv/data/unified/pet-traits/unified-pet-traits-v2/taxonomy.csv", "runtime_id", "pet_key", "combat_element_id", "combat_element_key", "species_id", "species_key", "species_trait_id", "species_trait_key", "provenance");
      if (var1.size() != 100) {
         throw new IllegalStateException("Unified Liệt Hỏa taxonomy requires exactly 100 rows.");
      } else {
         PetTaxonomyEntry[] var2 = new PetTaxonomyEntry[278];
         HashMap var3 = new HashMap();
         HashSet var4 = new HashSet();

         for(List var6 : var1) {
            int var7 = integer(var6, 0, "runtime_id");
            String var8 = String.format("LH-%03d", var7);
            if (var7 >= 0 && var7 < 100 && var2[var7] == null && var8.equals(var6.get(1))) {
               PetCombatElement var9 = PetCombatElement.require(integer(var6, 2, "combat_element_id"), required(var6, 3, "combat_element_key"));
               PetSpecies var10 = PetSpecies.require(integer(var6, 4, "species_id"), required(var6, 5, "species_key"));
               int var11 = integer(var6, 6, "species_trait_id");
               String var12 = required(var6, 7, "species_trait_key");
               PetTraitDefinition var13 = var0.speciesTrait(var11);
               if (var13 != null && var13.stableKey.equals(var12) && var13.species == var10) {
                  String var14 = required(var6, 8, "provenance");
                  if (!"unified_design".equals(var14)) {
                     throw new IllegalStateException("Liệt Hỏa taxonomy lacks unified_design provenance for " + var8);
                  }

                  var2[var7] = new PetTaxonomyEntry(var7, var8, var9, var10, var11, var12, var14);
                  var3.put(var10, (Integer)var3.getOrDefault(var10, 0) + 1);
                  var4.add(var12);
                  continue;
               }

               throw new IllegalStateException("Liệt Hỏa taxonomy trait/species mismatch for " + var8);
            }

            throw new IllegalStateException("Liệt Hỏa taxonomy stable-ID mismatch at " + (String)var6.get(1));
         }

         requireCount(var3, PetSpecies.THUC_VAT, 12);
         requireCount(var3, PetSpecies.KIM_LOAI, 17);
         requireCount(var3, PetSpecies.PHI_HANH, 16);
         requireCount(var3, PetSpecies.MANH_THU, 42);
         requireCount(var3, PetSpecies.HAI_DUONG, 13);
         if (var4.size() != 20) {
            throw new IllegalStateException("All 20 species traits must be used by the Liệt Hỏa taxonomy.");
         } else {
            for(UnifiedPetRecord var17 : UnifiedPetCatalog.instance().records()) {
               PetCombatElement var18 = PetCombatElement.require(var17.elementId, var17.elementKey);
               PetSpecies var19 = PetSpecies.require(var17.speciesId, var17.speciesKey);
               PetTraitDefinition var20 = var0.speciesTrait(var17.speciesTraitId);
               if (var20 == null || !var20.stableKey.equals(var17.speciesTraitKey) || var20.species != var19 || var2[var17.runtimeId] != null) {
                  throw new IllegalStateException("Cau Vong taxonomy trait/species mismatch for " + var17.stableKey);
               }

               var2[var17.runtimeId] = new PetTaxonomyEntry(var17.runtimeId, var17.stableKey, var18, var19, var17.speciesTraitId, var17.speciesTraitKey, "unified_design");
            }

            var2[274] = LietHoaMutationCatalog.taxonomyEntry();
            var2[275] = ThienLuanMutationCatalog.taxonomyEntry();
            var2[276] = HoaDiemHauVuongCatalog.taxonomyEntry();
            var2[277] = ThienViemHoCatalog.taxonomyEntry();

            for(int var16 = 0; var16 < 202; ++var16) {
               if (var2[var16] == null) {
                  throw new IllegalStateException("Missing unified taxonomy runtime ID " + var16);
               }
            }

            if (var2[274] == null) {
               throw new IllegalStateException("Missing mutation taxonomy runtime ID 274");
            } else if (var2[275] == null) {
               throw new IllegalStateException("Missing Thiên Luân mutation taxonomy runtime ID 275");
            } else if (var2[276] == null) {
               throw new IllegalStateException("Missing Hỏa Diễm Hầu Vương taxonomy runtime ID 276");
            } else if (var2[277] == null) {
               throw new IllegalStateException("Missing Thiên Viêm Hồ taxonomy runtime ID 277");
            } else {
               return new PetTaxonomyCatalog(var2);
            }
         }
      }
   }

   private static void requireCount(Map<PetSpecies, Integer> var0, PetSpecies var1, int var2) {
      if ((Integer)var0.getOrDefault(var1, 0) != var2) {
         throw new IllegalStateException("Unexpected Liệt Hỏa taxonomy count for " + var1.stableKey);
      }
   }

   private static String required(List<String> var0, int var1, String var2) {
      String var3 = (String)var0.get(var1);
      if (var3 != null && !var3.isEmpty()) {
         return var3;
      } else {
         throw new IllegalStateException("Pet taxonomy resource is missing " + var2 + ".");
      }
   }

   private static int integer(List<String> var0, int var1, String var2) {
      try {
         return Integer.parseInt((String)var0.get(var1));
      } catch (NumberFormatException var4) {
         throw new IllegalStateException("Invalid Pet taxonomy integer field " + var2, var4);
      }
   }
}
