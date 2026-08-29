package vqsv.battle.data;

import java.util.List;
import java.util.Objects;
import vqsv.pet.PetQuality;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;
import vqsv.pet.data.TanNguyetLongMaCatalog;
import vqsv.pet.data.UnifiedPetCatalog;
import vqsv.pet.data.UnifiedPetRecord;
import vqsv.pet.data.UnifiedV4PetCatalog;

public final class BattleSpeciesRow {
   public final int id;
   public final int nameTextId;
   public final int element;
   public final int evolutionKind;
   public final int quality;
   public final int rarity;
   public final int baseHp;
   public final int spriteId;
   public final int learnGroup;
   public final int evolutionSpeciesId;
   public final int evolutionMaterialId;
   public final int evolutionMaterialNeed;
   public final int relationClass;
   private final short[] raw;
   private final VqsvBattleTables tables;
   private final UnifiedPetRecord unified;
   private final LietHoaMutationCatalog.Record mutation;
   private final ThienLuanMutationCatalog.Record thienLuanMutation;
   private final HoaDiemHauVuongCatalog.Record hoaDiemEvolution;
   private final ThienViemHoCatalog.Record thienViemEvolution;
   private final TanNguyetLongMaCatalog.Record customBoss;

   BattleSpeciesRow(VqsvBattleTables var1, int var2, short[] var3) {
      this.tables = var1;
      this.id = var2;
      this.raw = var3;
      this.unified = null;
      this.mutation = null;
      this.thienLuanMutation = null;
      this.hoaDiemEvolution = null;
      this.thienViemEvolution = null;
      this.customBoss = null;
      this.nameTextId = VqsvBattleTables.get(var3, 0, -1);
      this.element = VqsvBattleTables.get(var3, 1, -1);
      this.evolutionKind = VqsvBattleTables.get(var3, 2, -1);
      this.quality = VqsvBattleTables.get(var3, 3, 1);
      this.rarity = VqsvBattleTables.get(var3, 4, 5);
      this.baseHp = VqsvBattleTables.get(var3, 5, 0);
      this.spriteId = VqsvBattleTables.get(var3, 17, -1);
      this.learnGroup = VqsvBattleTables.get(var3, 18, -1);
      this.evolutionSpeciesId = VqsvBattleTables.get(var3, 19, -1);
      this.evolutionMaterialId = VqsvBattleTables.get(var3, 20, -13) + 12;
      this.evolutionMaterialNeed = VqsvBattleTables.get(var3, 21, 0);
      this.relationClass = VqsvBattleTables.get(var3, 22, 0);
   }

   BattleSpeciesRow(VqsvBattleTables var1, UnifiedPetRecord var2) {
      this.tables = var1;
      this.id = var2.runtimeId;
      this.raw = null;
      this.unified = var2;
      this.mutation = null;
      this.thienLuanMutation = null;
      this.hoaDiemEvolution = null;
      this.thienViemEvolution = null;
      this.customBoss = null;
      this.nameTextId = -1;
      this.element = var2.elementId;
      this.evolutionKind = 0;
      this.quality = var2.defaultQuality;
      this.rarity = var2.rarity;
      this.baseHp = var2.stat(0, 1, 3);
      this.spriteId = var2.visualId;
      this.learnGroup = var2.sourceGameId;
      int var3 = -1;
      int var4 = -1;
      int var5 = 0;
      if (UnifiedV4PetCatalog.isV4Species(var2.runtimeId)) {
         List var6 = UnifiedV4PetCatalog.instance().evolutionsFrom(var2.runtimeId);
         UnifiedV4PetCatalog.EvolutionRule var7 = var6.isEmpty() ? null : (UnifiedV4PetCatalog.EvolutionRule)var6.get(0);
         var3 = var7 == null ? -1 : var7.toSpeciesId;
         UnifiedV4PetCatalog.Ingredient var8 = var7 != null && !var7.ingredients.isEmpty() ? (UnifiedV4PetCatalog.Ingredient)var7.ingredients.get(0) : null;
         var4 = var8 == null ? -1 : var8.runtimeMaterialId;
         var5 = var8 == null ? 0 : var8.quantity;
      } else {
         List var12 = UnifiedPetCatalog.instance().evolutionsFrom(var2.runtimeId);
         UnifiedPetCatalog.EvolutionRule var13 = var12.isEmpty() ? null : (UnifiedPetCatalog.EvolutionRule)var12.get(0);
         var3 = var13 == null ? -1 : var13.toSpeciesId;
         UnifiedPetCatalog.EvolutionIngredient var14 = var13 != null && !var13.ingredients.isEmpty() ? (UnifiedPetCatalog.EvolutionIngredient)var13.ingredients.get(0) : null;
         var4 = var14 == null ? -1 : var14.runtimeMaterialId;
         var5 = var14 == null ? 0 : var14.quantity;
      }

      this.evolutionSpeciesId = var3;
      this.evolutionMaterialId = var4;
      this.evolutionMaterialNeed = var5;
      this.relationClass = 0;
   }

   BattleSpeciesRow(VqsvBattleTables var1, LietHoaMutationCatalog.Record var2) {
      this.tables = var1;
      Objects.requireNonNull(var2);
      this.id = 274;
      this.raw = null;
      this.unified = null;
      this.mutation = var2;
      this.nameTextId = -1;
      this.thienLuanMutation = null;
      this.hoaDiemEvolution = null;
      this.thienViemEvolution = null;
      this.customBoss = null;
      Objects.requireNonNull(var2);
      this.element = 0;
      this.evolutionKind = 3;
      Objects.requireNonNull(var2);
      this.quality = 1;
      Objects.requireNonNull(var2);
      this.rarity = 3;
      Objects.requireNonNull(var2);
      this.baseHp = var2.stat(0, 1, 1);
      Objects.requireNonNull(var2);
      this.spriteId = 3000;
      this.learnGroup = -1;
      this.evolutionSpeciesId = -1;
      this.evolutionMaterialId = -1;
      this.evolutionMaterialNeed = 0;
      this.relationClass = 0;
   }

   BattleSpeciesRow(VqsvBattleTables var1, ThienLuanMutationCatalog.Record var2) {
      this.tables = var1;
      Objects.requireNonNull(var2);
      this.id = 275;
      this.raw = null;
      this.unified = null;
      this.mutation = null;
      this.thienLuanMutation = var2;
      this.hoaDiemEvolution = null;
      this.thienViemEvolution = null;
      this.customBoss = null;
      this.nameTextId = -1;
      Objects.requireNonNull(var2);
      this.element = 6;
      this.evolutionKind = 3;
      Objects.requireNonNull(var2);
      this.quality = 1;
      Objects.requireNonNull(var2);
      this.rarity = 3;
      Objects.requireNonNull(var2);
      this.baseHp = var2.stat(0, 1, 1);
      Objects.requireNonNull(var2);
      this.spriteId = 3001;
      this.learnGroup = -1;
      this.evolutionSpeciesId = -1;
      this.evolutionMaterialId = -1;
      this.evolutionMaterialNeed = 0;
      this.relationClass = 0;
   }

   BattleSpeciesRow(VqsvBattleTables tables, HoaDiemHauVuongCatalog.Record record) {
      this.tables = tables;
      this.id = HoaDiemHauVuongCatalog.RUNTIME_ID;
      this.raw = null;
      this.unified = null;
      this.mutation = null;
      this.thienLuanMutation = null;
      this.hoaDiemEvolution = Objects.requireNonNull(record);
      this.thienViemEvolution = null;
      this.customBoss = null;
      this.nameTextId = -1;
      this.element = record.elementId;
      this.evolutionKind = 0;
      this.quality = record.defaultQuality;
      this.rarity = record.rarity;
      this.baseHp = record.stat(0, 1, record.defaultQuality);
      this.spriteId = record.visualId;
      this.learnGroup = -1;
      this.evolutionSpeciesId = -1;
      this.evolutionMaterialId = -1;
      this.evolutionMaterialNeed = 0;
      this.relationClass = 0;
   }

   BattleSpeciesRow(VqsvBattleTables tables, ThienViemHoCatalog.Record record) {
      this.tables = tables;
      this.id = ThienViemHoCatalog.RUNTIME_ID;
      this.raw = null;
      this.unified = null;
      this.mutation = null;
      this.thienLuanMutation = null;
      this.hoaDiemEvolution = null;
      this.thienViemEvolution = Objects.requireNonNull(record);
      this.customBoss = null;
      this.nameTextId = -1;
      this.element = record.elementId;
      this.evolutionKind = 0;
      this.quality = record.defaultQuality;
      this.rarity = record.rarity;
      this.baseHp = record.stat(0, 1, record.defaultQuality);
      this.spriteId = record.visualId;
      this.learnGroup = -1;
      this.evolutionSpeciesId = -1;
      this.evolutionMaterialId = -1;
      this.evolutionMaterialNeed = 0;
      this.relationClass = 0;
   }

   BattleSpeciesRow(VqsvBattleTables tables, TanNguyetLongMaCatalog.Record record) {
      this.tables = tables;
      this.id = TanNguyetLongMaCatalog.RUNTIME_ID;
      this.raw = null;
      this.unified = null;
      this.mutation = null;
      this.thienLuanMutation = null;
      this.hoaDiemEvolution = null;
      this.thienViemEvolution = null;
      this.customBoss = Objects.requireNonNull(record);
      this.nameTextId = -1;
      this.element = record.elementId;
      this.evolutionKind = 0;
      this.quality = record.defaultQuality;
      this.rarity = record.rarity;
      this.baseHp = record.stat(0, 50, record.defaultQuality);
      this.spriteId = record.visualId;
      this.learnGroup = -1;
      this.evolutionSpeciesId = -1;
      this.evolutionMaterialId = -1;
      this.evolutionMaterialNeed = 0;
      this.relationClass = 2;
   }

   public boolean validForBattle() {
      return this.unified != null || this.mutation != null || this.thienLuanMutation != null || this.hoaDiemEvolution != null || this.thienViemEvolution != null || this.customBoss != null || this.raw != null && this.raw.length >= 23;
   }

   public boolean releaseProtected() {
      return this.relationClass == 2;
   }

   public int defaultQuality() {
      return PetQuality.resolve(this.quality, 3);
   }

   public int resolveQuality(int var1) {
      return PetQuality.resolve(var1, this.defaultQuality());
   }

   public String name(String var1) {
      if (this.customBoss != null) {
         return TanNguyetLongMaCatalog.NAME;
      } else if (this.thienViemEvolution != null) {
         return ThienViemHoCatalog.NAME;
      } else if (this.hoaDiemEvolution != null) {
         return HoaDiemHauVuongCatalog.NAME;
      } else if (this.mutation != null) {
         Objects.requireNonNull(this.mutation);
         return "Hỏa Vu Mộc Linh";
      } else if (this.thienLuanMutation != null) {
         Objects.requireNonNull(this.thienLuanMutation);
         return "Thiên Luân Di Lặc";
      } else {
         return this.unified == null ? this.tables.text(this.nameTextId, var1) : this.unified.name;
      }
   }

   public int statHp(int var1, int var2) {
      return this.stat(0, var1, var2);
   }

   public int statAttack(int var1, int var2) {
      return this.stat(1, var1, var2);
   }

   public int statDefense(int var1, int var2) {
      return this.stat(2, var1, var2);
   }

   public int statSpeed(int var1, int var2) {
      return this.stat(3, var1, var2);
   }

   private int stat(int var1, int var2, int var3) {
      int var4 = this.resolveQuality(var3);
      int var5 = this.rawStat(var1, var2, var4);
      return EvolutionStatFloor.apply(this.tables, this.id, var1, var2, var4, var5);
   }

   int rawStat(int var1, int var2, int var3) {
      int var4 = this.resolveQuality(var3);
      if (this.unified != null) {
         return this.tables.petStatProfileVersion() == PetStatProfileVersion.UNIFIED_PET_GROWTH_V6 && UnifiedV4PetCatalog.isV4Species(this.id) ? V4PetStatProfileCatalog.instance().stat(this.id, var1, var2, var4) : this.unified.stat(var1, var2, var4);
      } else if (this.mutation != null) {
         return this.mutation.stat(var1, var2, var4);
      } else if (this.thienLuanMutation != null) {
         return this.thienLuanMutation.stat(var1, var2, var4);
      } else if (this.hoaDiemEvolution != null) {
         return this.hoaDiemEvolution.stat(var1, var2, var4);
      } else if (this.thienViemEvolution != null) {
         return this.thienViemEvolution.stat(var1, var2, var4);
      } else if (this.customBoss != null) {
         return this.customBoss.stat(var1, var2, var4);
      } else {
         switch (var1) {
            case 0 -> {
               return this.tables.petStatProfileOwner().hp(this.id, this.raw, var2, var4);
            }
            case 1 -> {
               return this.tables.petStatProfileOwner().strength(this.id, this.raw, var2, var4);
            }
            case 2 -> {
               return this.tables.petStatProfileOwner().defense(this.id, this.raw, var2, var4);
            }
            case 3 -> {
               return this.tables.petStatProfileOwner().agility(this.id, this.raw, var2, var4);
            }
            default -> throw new IllegalArgumentException("Unknown Pet stat index " + var1);
         }
      }
   }

   public String shortDebugName() {
      String var10000 = this.name("species " + this.id);
      return var10000 + "[id=" + this.id + ",element=" + this.element + ",sprite=" + this.spriteId + ",relationClass=" + this.relationClass + "]";
   }
}
