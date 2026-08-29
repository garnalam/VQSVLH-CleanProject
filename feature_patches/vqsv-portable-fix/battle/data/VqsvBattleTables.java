package vqsv.battle.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vqsv.core.GameConfig;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;
import vqsv.pet.data.TanNguyetLongMaCatalog;
import vqsv.pet.data.UnifiedPetCatalog;
import vqsv.pet.data.UnifiedPetRecord;
import vqsv.pet.data.UnifiedV4PetCatalog;
import vqsv.resource.AssetPaths;
import vqsv.resource.BinaryReader;
import vqsv.resource.BinaryTables;
import vqsv.resource.ResourceLocator;
import vqsv.text.VqsvTextCodec;

public final class VqsvBattleTables {
   private static final int SOURCE_GROUP_COUNT = 9;
   private static final String UNIFIED_PET_GROWTH_V2_RESOURCE = "/vqsv/data/unified/pet-growth/unified-pet-growth-v2/db.mid";
   private static final String UNIFIED_PET_GROWTH_V3_RESOURCE = "/vqsv/data/unified/pet-growth/unified-pet-growth-v3/db.mid";
   private static final String UNIFIED_PET_GROWTH_V4_RESOURCE = "/vqsv/data/unified/pet-growth/unified-pet-growth-v4/db.mid";
   private static final Map<PetStatProfileVersion, VqsvBattleTables> PROFILE_CACHE = new EnumMap(PetStatProfileVersion.class);
   private static VqsvBattleTables cached;
   private final short[][][] groups;
   private final String[] texts;
   private final PetStatProfileOwner petStatProfileOwner;

   private VqsvBattleTables(short[][][] var1, String[] var2, PetStatProfileOwner var3) {
      this.groups = var1;
      this.texts = var2;
      this.petStatProfileOwner = var3;
   }

   public static VqsvBattleTables instance() {
      if (cached == null) {
         cached = load();
         synchronized(PROFILE_CACHE) {
            PROFILE_CACHE.put(cached.petStatProfileVersion(), cached);
         }
      }

      return cached;
   }

   public static VqsvBattleTables forProfile(PetStatProfileVersion var0) {
      Objects.requireNonNull(var0, "profile");
      VqsvBattleTables var1 = instance();
      if (var1.petStatProfileVersion() == var0) {
         return var1;
      } else {
         synchronized(PROFILE_CACHE) {
            VqsvBattleTables var3 = (VqsvBattleTables)PROFILE_CACHE.get(var0);
            if (var3 != null) {
               return var3;
            } else {
               VqsvBattleTables var4 = loadForProfile(var0, AssetPaths.fromWorkingTree(GameConfig.defaultConfig()));
               PROFILE_CACHE.put(var0, var4);
               return var4;
            }
         }
      }
   }

   public static String sourceSummary() {
      VqsvBattleTables var0 = instance();
      StringBuilder var1 = new StringBuilder("battleTables");
      var1.append(" profile=").append(var0.petStatProfileVersion().runtimeToken());

      for(int var2 = 0; var2 < 9; ++var2) {
         var1.append(" g").append(var2).append('=').append(var0.rowCount(var2));
      }

      BattleSpeciesRow var5 = var0.species(34);
      BattleSpeciesRow var3 = var0.species(68);
      BattleSpeciesRow var4 = var0.species(5);
      var1.append(" species34=").append(var5 == null ? "missing" : var5.shortDebugName());
      var1.append(" species68=").append(var3 == null ? "missing" : var3.shortDebugName());
      var1.append(" species5=").append(var4 == null ? "missing" : var4.shortDebugName());
      return var1.toString();
   }

   private static VqsvBattleTables load() {
      PetStatProfileVersion var0 = PetStatProfileVersion.runtimeSelection();

      try {
         return loadForProfile(var0, AssetPaths.fromWorkingTree(GameConfig.defaultConfig()));
      } catch (RuntimeException var2) {
         throw new IllegalStateException("Cannot load battle tables for profile " + var0.runtimeToken(), var2);
      }
   }

   static VqsvBattleTables loadForProfile(PetStatProfileVersion var0, AssetPaths var1) {
      BinaryReader var2 = var0 == PetStatProfileVersion.SOURCE_V1 ? (new ResourceLocator(var1)).binary(var1.scriptOriginal("db.mid")) : unifiedPetGrowthReader(var0);
      short[][][] var3 = new short[9][][];

      for(int var4 = 0; var4 < var3.length; ++var4) {
         var3[var4] = BinaryTables.readShortRows(var2);
      }

      PetStatProfileOwner var5;
      if (var0 == PetStatProfileVersion.SOURCE_V1) {
         var5 = PetStatProfileOwner.sourceV1();
      } else {
         var5 = PetStatProfileOwner.unifiedPetGrowth(var0, BinaryTables.readShortRows(var2));
      }

      if (var2.hasRemaining()) {
         throw new IllegalStateException("Pet stat db.mid contains unexpected trailing bytes for profile " + var0.runtimeToken());
      } else {
         return new VqsvBattleTables(var3, readTextRows(var1), var5);
      }
   }

   private static BinaryReader unifiedPetGrowthReader(PetStatProfileVersion var0) {
      String var1;
      if (var0 == PetStatProfileVersion.UNIFIED_PET_GROWTH_V2) {
         var1 = "/vqsv/data/unified/pet-growth/unified-pet-growth-v2/db.mid";
      } else if (var0 == PetStatProfileVersion.UNIFIED_PET_GROWTH_V3) {
         var1 = "/vqsv/data/unified/pet-growth/unified-pet-growth-v3/db.mid";
      } else {
         if (var0 != PetStatProfileVersion.UNIFIED_PET_GROWTH_V4 && var0 != PetStatProfileVersion.UNIFIED_PET_GROWTH_V5 && var0 != PetStatProfileVersion.UNIFIED_PET_GROWTH_V6) {
            throw new IllegalArgumentException("No unified Pet growth resource for " + var0.runtimeToken());
         }

         var1 = "/vqsv/data/unified/pet-growth/unified-pet-growth-v4/db.mid";
      }

      try {
         InputStream var2 = VqsvBattleTables.class.getResourceAsStream(var1);

         BinaryReader var3;
         try {
            if (var2 == null) {
               throw new IllegalStateException("Missing unified Pet growth resource: " + var1);
            }

            var3 = new BinaryReader(var2.readAllBytes(), var1);
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

         return var3;
      } catch (IOException var7) {
         throw new IllegalStateException("Cannot read unified Pet growth resource: " + var1, var7);
      }
   }

   public PetStatProfileVersion petStatProfileVersion() {
      return this.petStatProfileOwner.version();
   }

   PetStatProfileOwner petStatProfileOwner() {
      return this.petStatProfileOwner;
   }

   public int rowCount(int var1) {
      short[][] var2 = this.groupRows(var1);
      return var2 == null ? 0 : var2.length;
   }

   public short[] row(int var1, int var2) {
      short[][] var3 = this.groupRows(var1);
      if (var3 != null && var2 >= 0 && var2 < var3.length) {
         return var3[var2] == null ? null : Arrays.copyOf(var3[var2], var3[var2].length);
      } else {
         return null;
      }
   }

   public BattleSpeciesRow species(int var1) {
      short[] var2 = this.row(0, var1);
      if (var2 != null) {
         return new BattleSpeciesRow(this, var1, var2);
      } else if (this.petStatProfileVersion() != PetStatProfileVersion.SOURCE_V1 && UnifiedSkillProfile.unifiedEnabled()) {
         if (TanNguyetLongMaCatalog.isSpecies(var1)) {
            return new BattleSpeciesRow(this, TanNguyetLongMaCatalog.record());
         } else if (HoaDiemHauVuongCatalog.isSpecies(var1)) {
            return new BattleSpeciesRow(this, HoaDiemHauVuongCatalog.record());
         } else if (ThienViemHoCatalog.isSpecies(var1)) {
            return new BattleSpeciesRow(this, ThienViemHoCatalog.record());
         } else if (LietHoaMutationCatalog.isSpecies(var1)) {
            return new BattleSpeciesRow(this, LietHoaMutationCatalog.record());
         } else if (ThienLuanMutationCatalog.isSpecies(var1)) {
            return new BattleSpeciesRow(this, ThienLuanMutationCatalog.record());
         } else {
            UnifiedPetRecord var3 = UnifiedPetCatalog.instance().byRuntimeId(var1);
            if (var3 == null) {
               var3 = UnifiedV4PetCatalog.instance().byRuntimeId(var1);
            }

            return var3 == null ? null : new BattleSpeciesRow(this, var3);
         }
      } else {
         return null;
      }
   }

   public BattleSkillRow skill(int var1) {
      UnifiedSkillRecord var2 = UnifiedSkillProfile.unifiedEnabled() ? UnifiedSkillCatalog.instance().byRuntimeId(var1) : null;
      if (var2 != null) {
         return new BattleSkillRow(this, var2);
      } else {
         short[] var3 = this.row(1, var1);
         return var3 != null ? new BattleSkillRow(this, var1, var3) : null;
      }
   }

   public BadgeRow badge(int var1) {
      short[] var2 = this.row(2, var1);
      return var2 == null ? null : new BadgeRow(this, var1, var2);
   }

   public BattleStatusRow status(int var1) {
      short[] var2 = this.row(3, var1);
      return var2 == null ? null : new BattleStatusRow(this, var1, var2);
   }

   public BattleHeldItemRow heldItem(int var1) {
      short[] var2 = this.row(3, var1);
      return var2 == null ? null : new BattleHeldItemRow(this, var1, var2);
   }

   public BattleItemRow item(int var1) {
      short[] var2 = this.row(4, var1);
      return var2 == null ? null : new BattleItemRow(this, var1, var2);
   }

   public SpecialRewardRow specialReward(int var1) {
      short[] var2 = this.row(5, var1);
      return var2 == null ? null : new SpecialRewardRow(this, var1, var2);
   }

   public BattleBuffRow buff(int var1) {
      short[] var2 = this.row(6, var1);
      return var2 == null ? null : new BattleBuffRow(this, var1, var2);
   }

   public BattleDebuffRow debuff(int var1) {
      short[] var2 = this.row(7, var1);
      return var2 == null ? null : new BattleDebuffRow(this, var1, var2);
   }

   public LearnThresholdRow learnThreshold(int var1) {
      short[] var2 = this.row(8, var1);
      return var2 == null ? null : new LearnThresholdRow(var1, var2);
   }

   public BattleEffectRow effect(int var1, int var2) {
      if (var1 == 0) {
         return this.buff(var2);
      } else {
         return var1 == 1 ? this.debuff(var2) : null;
      }
   }

   public String text(int var1, String var2) {
      return var1 >= 0 && var1 < this.texts.length && this.texts[var1] != null && !this.texts[var1].isEmpty() ? this.texts[var1] : var2;
   }

   private short[][] groupRows(int var1) {
      return var1 >= 0 && var1 < this.groups.length ? this.groups[var1] : null;
   }

   private static String[] readTextRows(AssetPaths var0) {
      Path var1 = var0.modulesRoot().resolve("script").resolve("decoded").resolve("data__script__chs.mid.json");

      try {
         String var2 = Files.readString(var1, StandardCharsets.UTF_8);
         ArrayList var3 = new ArrayList();
         Matcher var4 = Pattern.compile("\\[\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\]").matcher(var2);

         while(var4.find()) {
            var3.add(VqsvTextCodec.decodeMojibake(unescapeJsonString(var4.group(1))));
         }

         return (String[])var3.toArray(new String[0]);
      } catch (IOException var5) {
         throw new IllegalStateException("Cannot read source battle text rows: " + String.valueOf(var1), var5);
      }
   }

   private static String unescapeJsonString(String var0) {
      StringBuilder var1 = new StringBuilder(var0.length());

      for(int var2 = 0; var2 < var0.length(); ++var2) {
         char var3 = var0.charAt(var2);
         if (var3 == '\\' && var2 + 1 < var0.length()) {
            ++var2;
            char var4 = var0.charAt(var2);
            switch (var4) {
               case '"':
               case '/':
               case '\\':
                  var1.append(var4);
                  break;
               case 'n':
                  var1.append('\n');
                  break;
               case 'r':
                  var1.append('\r');
                  break;
               case 't':
                  var1.append('\t');
                  break;
               case 'u':
                  if (var2 + 4 < var0.length()) {
                     var1.append((char)Integer.parseInt(var0.substring(var2 + 1, var2 + 5), 16));
                     var2 += 4;
                  }
                  break;
               default:
                  var1.append(var4);
            }
         } else {
            var1.append(var3);
         }
      }

      return var1.toString();
   }

   public static int get(short[] var0, int var1, int var2) {
      return var0 != null && var1 >= 0 && var1 < var0.length ? var0[var1] : var2;
   }
}
