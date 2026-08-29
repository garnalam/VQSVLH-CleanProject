package vqsv.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.UnifiedSkillLearnsetCatalog;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.battle.model.BattleUnit;
import vqsv.battle.model.PetBattleAdapter;
import vqsv.pet.PetState;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;
import vqsv.pet.data.UnifiedV4PetCatalog;
import vqsv.progression.EvolutionCandidate;
import vqsv.progression.EvolutionProgression;
import vqsv.progression.UnifiedEvolutionCatalog;
import vqsv.source.PetSourceAdapter;

final class VqsvSourceEvolutionRuntime {
   private static final int[] SOURCE_LEVELS = new int[]{12, 30, 5};

   private VqsvSourceEvolutionRuntime() {
   }

   static EvolutionCandidate noticeForPet(VqsvGameRuntime.Scene var0, int var1) {
      return noticeForPet(var0, var1, (EvolutionCandidate.Kind)null);
   }

   static EvolutionCandidate noticeForPet(VqsvGameRuntime.Scene var0, int var1, EvolutionCandidate.Kind var2) {
      if (var1 >= 0 && var1 < var0.session.pets.roster.size()) {
         PetState var3 = (PetState)var0.session.pets.roster.get(var1);
         return noticeForSpecies(var3.speciesId, var3.level, var2);
      } else {
         return null;
      }
   }

   static EvolutionCandidate noticeForSpecies(int var0, int var1, EvolutionCandidate.Kind var2) {
      if (ThienViemHoCatalog.isSourceSpecies(var0)) {
         if (var2 != null && var2 != EvolutionCandidate.Kind.EVOLUTION) {
            return null;
         }
         List<EvolutionCandidate.Ingredient> ingredients = new ArrayList<>();
         ingredients.add(new EvolutionCandidate.Ingredient("LH-ITEM-3-12", 12, "Tinh Nguyên Thạch", 1));
         ingredients.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-17", 3010, "Thú hồn", 1));
         return new EvolutionCandidate(var0, var1, ThienViemHoCatalog.RUNTIME_ID, 1, 1, EvolutionCandidate.Kind.EVOLUTION, ThienViemHoCatalog.REQUIRED_LEVEL, ThienViemHoCatalog.STABLE_KEY, ingredients);
      } else if (HoaDiemHauVuongCatalog.isSourceSpecies(var0)) {
         if (var2 != null && var2 != EvolutionCandidate.Kind.EVOLUTION) {
            return null;
         }
         List<EvolutionCandidate.Ingredient> ingredients = new ArrayList<>();
         ingredients.add(new EvolutionCandidate.Ingredient("LH-ITEM-3-14", 14, "Thiên Địa Thần Thạch", 1));
         ingredients.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-17", 3010, "Thú hồn", 3));
         ingredients.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-30", 3011, "Vương chi chứng minh", 1));
         return new EvolutionCandidate(var0, var1, HoaDiemHauVuongCatalog.RUNTIME_ID, 1, 1, EvolutionCandidate.Kind.EVOLUTION, HoaDiemHauVuongCatalog.REQUIRED_LEVEL, HoaDiemHauVuongCatalog.STABLE_KEY, ingredients);
      } else if (LietHoaMutationCatalog.isSourceSpecies(var0)) {
         if (var2 != null && var2 != EvolutionCandidate.Kind.MUTATION) {
            return null;
         } else {
            ArrayList var10 = new ArrayList();
            var10.add(new EvolutionCandidate.Ingredient("LH-ITEM-3-16", 16, "Quỷ Thần Tinh Thạch", 1));
            var10.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-18", 3000, "Thụ chướng thạch", 3));
            var10.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-19", 3001, "Chu quả", 3));
            return new EvolutionCandidate(var0, var1, 274, 3, 3, EvolutionCandidate.Kind.MUTATION, 40, "LH-023-MUT-01", var10);
         }
      } else if (ThienLuanMutationCatalog.isSourceSpecies(var0)) {
         if (var2 != null && var2 != EvolutionCandidate.Kind.MUTATION) {
            return null;
         } else {
            ArrayList var9 = new ArrayList();
            var9.add(new EvolutionCandidate.Ingredient("LH-ITEM-3-12", 12, "Tinh Nguyên Thạch", 2));
            var9.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-17", 3010, "Thú hồn", 3));
            var9.add(new EvolutionCandidate.Ingredient("CV-ITEM-0-26", 3008, "Bay lượn thiên thạch", 1));
            return new EvolutionCandidate(var0, var1, 275, 3, 3, EvolutionCandidate.Kind.MUTATION, 30, "LH-034-MUT-01", var9);
         }
      } else if (UnifiedV4PetCatalog.isV4Species(var0)) {
         UnifiedV4PetCatalog var8 = UnifiedV4PetCatalog.instance();
         UnifiedV4PetCatalog.EvolutionRule var11 = var2 == null ? firstV4Rule(var8.evolutionsFrom(var0)) : var8.evolutionFrom(var0, var2);
         if (var11 == null) {
            return null;
         } else {
            ArrayList var12 = new ArrayList();

            for(UnifiedV4PetCatalog.Ingredient var14 : var11.ingredients) {
               var12.add(new EvolutionCandidate.Ingredient(var14.itemKey, var14.runtimeMaterialId, var14.name, var14.quantity));
            }

            return new EvolutionCandidate(var0, var1, var11.toSpeciesId, var11.kind == EvolutionCandidate.Kind.MUTATION ? 3 : 1, var11.kind == EvolutionCandidate.Kind.MUTATION ? 3 : 1, var11.kind, var11.requiredLevel, var11.stableKey, var12);
         }
      } else {
         UnifiedEvolutionCatalog var3 = UnifiedEvolutionCatalog.instance();
         UnifiedEvolutionCatalog.Rule var4 = var2 == null ? firstRule(var3.evolutionsFrom(var0)) : var3.evolutionFrom(var0, var2);
         if (var4 == null) {
            return null;
         } else {
            ArrayList var5 = new ArrayList();

            for(UnifiedEvolutionCatalog.Ingredient var7 : var4.ingredients) {
               var5.add(new EvolutionCandidate.Ingredient(var7.itemKey, var7.runtimeMaterialId, var7.name, var7.quantity));
            }

            return new EvolutionCandidate(var0, var1, var4.toSpeciesId, var4.kind == EvolutionCandidate.Kind.MUTATION ? 3 : 1, var4.kind == EvolutionCandidate.Kind.MUTATION ? 3 : 1, var4.kind, var4.requiredLevel, var4.stableKey, var5);
         }
      }
   }

   static int[] visibleStats(VqsvGameRuntime.Scene var0, PetState var1) {
      if (var1 == null) {
         return new int[]{0, 0, 0, 0};
      } else {
         BattleUnit var2 = PetBattleAdapter.toBattleUnit(var1, (byte)0, var0.session.progression.badges);
         return var2.sourceVisibleStats();
      }
   }

   static int[] targetVisibleStats(VqsvGameRuntime.Scene var0, PetState var1, int var2) {
      if (var1 != null && var2 >= 0) {
         BattleSpeciesRow var3 = VqsvBattleTables.instance().species(var2);
         int var4 = var3 == null ? var1.quality : var3.defaultQuality();
         int var5 = EvolutionProgression.evolvedQuality(var1.quality, var4);
         PetState var6 = PetSourceAdapter.createWithPhysicalTrait(var1.slot, var2, var1.level, var5, var1.nature, var1.physicalTraitId, -1, -1);
         BattleUnit var7 = PetBattleAdapter.toBattleUnit(var6, (byte)0, var0.session.progression.badges);
         return var7.sourceVisibleStats();
      } else {
         return new int[]{0, 0, 0, 0};
      }
   }

   static String materialName(EvolutionCandidate var0) {
      if (var0 != null && var0.materialId >= 0) {
         UnifiedEvolutionCatalog.MaterialSource var1 = UnifiedEvolutionCatalog.instance().materialSource(var0.materialId);
         if (var1 != null) {
            return var1.name;
         } else {
            for(UnifiedV4PetCatalog.EvolutionRule var3 : UnifiedV4PetCatalog.instance().evolutionsFrom(var0.currentSpeciesId)) {
               for(UnifiedV4PetCatalog.Ingredient var5 : var3.ingredients) {
                  if (var5.runtimeMaterialId == var0.materialId) {
                     return var5.name;
                  }
               }
            }

            return VqsvSourceOps.sourceMaterialName(var0.materialId);
         }
      } else {
         return "";
      }
   }

   static String materialSummary(EvolutionCandidate var0) {
      if (var0 != null && !var0.ingredients.isEmpty()) {
         StringBuilder var1 = new StringBuilder();

         for(EvolutionCandidate.Ingredient var3 : var0.ingredients) {
            if (var1.length() > 0) {
               var1.append(" + ");
            }

            var1.append(var3.name.isEmpty() ? VqsvSourceOps.sourceMaterialName(var3.materialId) : var3.name).append(" x").append(var3.quantity);
         }

         return var1.toString();
      } else {
         return "";
      }
   }

   static String materialCountSummary(VqsvGameRuntime.Scene var0, EvolutionCandidate var1) {
      if (var1 != null && !var1.ingredients.isEmpty()) {
         StringBuilder var2 = new StringBuilder();

         for(EvolutionCandidate.Ingredient var4 : var1.ingredients) {
            if (var2.length() > 0) {
               var2.append("; ");
            }

            var2.append(materialCount(var0, var4.materialId)).append('/').append(var4.quantity);
         }

         return var2.toString();
      } else {
         return "";
      }
   }

   static int materialCount(VqsvGameRuntime.Scene var0, int var1) {
      return VqsvSourceOps.sourceMaterialCount(var0, var1) + VqsvSourceOps.sourceMaterialCount(var0, VqsvSourceOps.sourceHeldTableMaterialInventoryId(var1));
   }

   static void consumeMaterial(VqsvGameRuntime.Scene var0, int var1, int var2) {
      int var3 = VqsvSourceOps.sourceHeldTableMaterialInventoryId(var1);
      int var4 = VqsvSourceOps.sourceMaterialCount(var0, var3);
      int var5 = Math.min(Math.max(0, var2), var4);
      if (var5 > 0) {
         VqsvSourceOps.sourceRemoveMaterial(var0, var3, var5);
      }

      int var6 = var2 - var5;
      if (var6 > 0) {
         VqsvSourceOps.sourceRemoveMaterial(var0, var1, var6);
      }

   }

   static boolean consumeMaterials(VqsvGameRuntime.Scene var0, EvolutionCandidate var1) {
      if (var1 != null && !var1.ingredients.isEmpty()) {
         Map<Integer, Integer> var2 = aggregateIngredients(var1);

         for(Map.Entry var4 : var2.entrySet()) {
            if (materialCount(var0, (Integer)var4.getKey()) < (Integer)var4.getValue()) {
               return false;
            }
         }

         for(Map.Entry var6 : var2.entrySet()) {
            consumeMaterial(var0, (Integer)var6.getKey(), (Integer)var6.getValue());
         }

         return true;
      } else {
         return false;
      }
   }

   static void mutatePet(VqsvGameRuntime.Scene var0, int var1, EvolutionCandidate var2) {
      if (var2 != null && var1 >= 0 && var1 < var0.session.pets.roster.size()) {
         PetState var3 = (PetState)var0.session.pets.roster.get(var1);
         int var4 = var3.experience;
         int var5 = var3.quality;
         String var6 = UnifiedSkillLearnsetCatalog.petKey(var3.speciesId);
         BattleSpeciesRow var7 = VqsvBattleTables.instance().species(var2.targetSpeciesId);
         int var8 = var7 == null ? -1 : var7.spriteId;
         int var9 = var7 == null ? var3.quality : var7.defaultQuality();
         int var10 = EvolutionProgression.evolvedQuality(var3.quality, var9);
         PetState var11 = PetSourceAdapter.createWithPhysicalTrait(var3.slot, var2.targetSpeciesId, var3.level, var10, var3.nature, var3.physicalTraitId, -1, -1);
         var11.heldEquipmentId = var3.heldEquipmentId;
         var11.battleSideFlag = var3.battleSideFlag;
         var11.sourceSpecialUseId = var3.sourceSpecialUseId;
         BattleUnit var12 = PetBattleAdapter.toBattleUnit(var11, (byte)0, var0.session.progression.badges);
         int var13 = var12.maxHp();
         var0.session.progression.evolution.apply(var3, var2, var13, var8, var9);
         String var14 = UnifiedSkillLearnsetCatalog.petKey(var3.speciesId);
         if (!UnifiedSkillLearnsetCatalog.instance().inheritsSkills(var6, var14)) {
            PetSourceAdapter.resetLearnsetDeck(var3);
         }

         var0.session.progression.collection.markCollected(var2.targetSpeciesId);
         var0.session.story.trace().add("PORTED/PARTIAL game.h.bh mutate pet index=" + var1 + " species=" + var2.currentSpeciesId + "->" + var2.targetSpeciesId + " visual=" + var8 + " quality=" + var5 + "->" + var3.quality + " targetDefaultQuality=" + var9 + " hp=" + var3.currentHp + " expPreserved=" + var4);
      }
   }

   static EvolutionCandidate.Kind sourceEvolutionKind(int var0) {
      if (var0 != 1 && var0 != 2) {
         return var0 == 3 ? EvolutionCandidate.Kind.MUTATION : EvolutionCandidate.Kind.NONE;
      } else {
         return EvolutionCandidate.Kind.EVOLUTION;
      }
   }

   static int sourceEvolutionRequiredLevel(int var0) {
      int var1 = var0 - 1;
      return var1 >= 0 && var1 < SOURCE_LEVELS.length ? SOURCE_LEVELS[var1] : Integer.MAX_VALUE;
   }

   private static Map<Integer, Integer> aggregateIngredients(EvolutionCandidate var0) {
      LinkedHashMap<Integer, Integer> var1 = new LinkedHashMap<>();

      for(EvolutionCandidate.Ingredient var3 : var0.ingredients) {
         var1.merge(var3.materialId, var3.quantity, (left, right) -> (Integer)left + (Integer)right);
      }

      return var1;
   }

   private static UnifiedEvolutionCatalog.Rule firstRule(List<UnifiedEvolutionCatalog.Rule> var0) {
      for(UnifiedEvolutionCatalog.Rule var2 : var0) {
         if (var2.kind == EvolutionCandidate.Kind.EVOLUTION) {
            return var2;
         }
      }

      return var0.isEmpty() ? null : (UnifiedEvolutionCatalog.Rule)var0.get(0);
   }

   private static UnifiedV4PetCatalog.EvolutionRule firstV4Rule(List<UnifiedV4PetCatalog.EvolutionRule> var0) {
      for(UnifiedV4PetCatalog.EvolutionRule var2 : var0) {
         if (var2.kind == EvolutionCandidate.Kind.EVOLUTION) {
            return var2;
         }
      }

      return var0.isEmpty() ? null : (UnifiedV4PetCatalog.EvolutionRule)var0.get(0);
   }
}
