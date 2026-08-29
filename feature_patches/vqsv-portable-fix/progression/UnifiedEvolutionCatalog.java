package vqsv.progression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import vqsv.data.UnifiedItemCatalog;
import vqsv.data.UnifiedItemInventoryKind;
import vqsv.data.UnifiedItemRecord;

public final class UnifiedEvolutionCatalog {
   public static final String RESOURCE_ROOT = "/vqsv/data/unified/evolutions/unified-evolution-v1/";
   public static final String VERSION = "unified-evolution-v1";
   public static final int EVOLUTION_COUNT = 88;
   public static final int INGREDIENT_COUNT = 239;
   public static final int MATERIAL_COUNT = 20;
   public static final int RAINBOW_SHOP_OVERLAY_COUNT = 15;
   private static final UnifiedEvolutionCatalog INSTANCE = load();
   private final Map<String, Rule> byKey;
   private final Map<Integer, List<Rule>> bySpecies;
   private final Map<Integer, MaterialSource> materialSources;
   private final List<MaterialSource> rainbowShopOverlay;
   private final Properties manifest;

   private UnifiedEvolutionCatalog(Map<String, Rule> var1, Map<Integer, List<Rule>> var2, Map<Integer, MaterialSource> var3, List<MaterialSource> var4, Properties var5) {
      this.byKey = Collections.unmodifiableMap(var1);
      this.bySpecies = freezeLists(var2);
      this.materialSources = Collections.unmodifiableMap(var3);
      this.rainbowShopOverlay = Collections.unmodifiableList(new ArrayList(var4));
      this.manifest = var5;
   }

   public static UnifiedEvolutionCatalog instance() {
      return INSTANCE;
   }

   public List<Rule> rules() {
      return Collections.unmodifiableList(new ArrayList(this.byKey.values()));
   }

   public Rule byStableKey(String var1) {
      return var1 == null ? null : (Rule)this.byKey.get(var1);
   }

   public List<Rule> evolutionsFrom(int var1) {
      return (List)this.bySpecies.getOrDefault(var1, Collections.emptyList());
   }

   public Rule evolutionFrom(int var1, EvolutionCandidate.Kind var2) {
      for(Rule var4 : this.evolutionsFrom(var1)) {
         if (var2 == null || var4.kind == var2) {
            return var4;
         }
      }

      return null;
   }

   public List<MaterialSource> materialSources() {
      return Collections.unmodifiableList(new ArrayList(this.materialSources.values()));
   }

   public MaterialSource materialSource(int var1) {
      return (MaterialSource)this.materialSources.get(var1);
   }

   public List<MaterialSource> rainbowShopOverlay() {
      return this.rainbowShopOverlay;
   }

   public MaterialSource rainbowShopOverlay(int var1) {
      return var1 >= 0 && var1 < this.rainbowShopOverlay.size() ? (MaterialSource)this.rainbowShopOverlay.get(var1) : null;
   }

   public String snapshotSha256() {
      return this.manifest.getProperty("snapshot_sha256", "");
   }

   private static UnifiedEvolutionCatalog load() {
      Properties var0 = properties("manifest.properties");
      require(var0, "schema_version", "1.0.0");
      require(var0, "data_version", "unified-2026-07-28");
      require(var0, "item_balance_version", "unified-item-v1");
      require(var0, "evolution_contract_version", "unified-evolution-v1");
      require(var0, "snapshot_sha256", "bceb7c514cc5285717739bfa418b774e1fab2a24aff8fc444bb54b3457cf1d76");
      require(var0, "evolution_count", String.valueOf(88));
      require(var0, "ingredient_count", String.valueOf(239));
      require(var0, "material_count", String.valueOf(20));
      require(var0, "recipe_audit_count", String.valueOf(88));
      require(var0, "liet_hoa_evolution_count", "38");
      require(var0, "rainbow_evolution_count", "50");
      require(var0, "liet_hoa_ingredient_count", "97");
      require(var0, "rainbow_ingredient_count", "142");
      Map<String, List<Ingredient>> var1 = loadIngredients();
      LinkedHashMap<String, Rule> var2 = new LinkedHashMap<>();
      LinkedHashMap<Integer, List<Rule>> var3 = new LinkedHashMap<>();

      try {
         BufferedReader var4 = reader("evolutions.tsv");

         try {
            Header var5 = UnifiedEvolutionCatalog.Header.read(var4);

            String var6;
            while((var6 = var4.readLine()) != null) {
               if (!var6.isEmpty()) {
                  String[] var7 = var6.split("\\t", -1);
                  String var8 = var5.text(var7, "evolution_key");
                  EvolutionCandidate.Kind var9 = "mutation".equals(var5.text(var7, "evolution_type")) ? EvolutionCandidate.Kind.MUTATION : EvolutionCandidate.Kind.EVOLUTION;
                  Rule var10 = new Rule(var8, var5.text(var7, "from_pet_key"), var5.integer(var7, "from_runtime_id"), var5.text(var7, "to_pet_key"), var5.integer(var7, "to_runtime_id"), var9, var5.integer(var7, "required_level"), var5.integer(var7, "cost_rank"), var5.text(var7, "topology_provenance"), var5.text(var7, "recipe_provenance"), (List)var1.getOrDefault(var8, Collections.emptyList()));
                  validate(var10);
                  if (var2.put(var8, var10) != null) {
                     throw new IllegalStateException("Duplicate unified evolution " + var8);
                  }

                  var3.computeIfAbsent(var10.fromSpeciesId, (var0x) -> new ArrayList<>()).add(var10);
               }
            }
         } catch (Throwable var15) {
            if (var4 != null) {
               try {
                  var4.close();
               } catch (Throwable var12) {
                  var15.addSuppressed(var12);
               }
            }

            throw var15;
         }

         if (var4 != null) {
            var4.close();
         }
      } catch (IOException var16) {
         throw new IllegalStateException("Cannot load unified evolutions", var16);
      }

      if (var2.size() == 88 && var1.size() == 88) {
         LinkedHashMap<Integer, MaterialSource> var17 = new LinkedHashMap<>();
         ArrayList<MaterialSource> var18 = new ArrayList<>();

         try {
            BufferedReader var19 = reader("material-sources.tsv");

            try {
               Header var22 = UnifiedEvolutionCatalog.Header.read(var19);

               String var24;
               while((var24 = var19.readLine()) != null) {
                  if (!var24.isEmpty()) {
                     String[] var26 = var24.split("\\t", -1);
                     MaterialSource var28 = new MaterialSource(var22.text(var26, "item_key"), var22.integer(var26, "runtime_material_id"), var22.text(var26, "name"), var22.text(var26, "source_key"), var22.text(var26, "acquisition_route"), var22.integer(var26, "shop_order"), var22.integer(var26, "tier"), var22.integer(var26, "price"), var22.integer(var26, "currency_code"), UnifiedEvolutionCatalog.PricingPolicy.valueOf(var22.text(var26, "pricing_policy")), var22.integer(var26, "repeatable") != 0, var22.integer(var26, "unlock_task"), var22.text(var26, "provenance"));
                     validate(var28);
                     if (var17.put(var28.runtimeMaterialId, var28) != null) {
                        throw new IllegalStateException("Duplicate evolution material ID " + var28.runtimeMaterialId);
                     }

                     if ("cau-vong".equals(var28.sourceKey)) {
                        var18.add(var28);
                     }
                  }
               }
            } catch (Throwable var13) {
               if (var19 != null) {
                  try {
                     var19.close();
                  } catch (Throwable var11) {
                     var13.addSuppressed(var11);
                  }
               }

               throw var13;
            }

            if (var19 != null) {
               var19.close();
            }
         } catch (IOException var14) {
            throw new IllegalStateException("Cannot load evolution material sources", var14);
         }

         var18.sort(Comparator.comparingInt((var0x) -> var0x.shopOrder));
         if (var17.size() == 20 && var18.size() == 15) {
            for(int var20 = 0; var20 < var18.size(); ++var20) {
               if (((MaterialSource)var18.get(var20)).shopOrder != var20) {
                  throw new IllegalStateException("Evolution shop overlay order has a gap");
               }
            }

            for(Rule var23 : var2.values()) {
               for(Ingredient var27 : var23.ingredients) {
                  if (!var17.containsKey(var27.runtimeMaterialId)) {
                     throw new IllegalStateException("Evolution ingredient has no acquisition route: " + var27.itemKey);
                  }
               }
            }

            return new UnifiedEvolutionCatalog(var2, var3, var17, var18, var0);
         } else {
            throw new IllegalStateException("Unified evolution material coverage is not 20/15");
         }
      } else {
         throw new IllegalStateException("Unified evolution coverage is not 88/88");
      }
   }

   private static Map<String, List<Ingredient>> loadIngredients() {
      LinkedHashMap<String, List<Ingredient>> var0 = new LinkedHashMap<>();
      int var1 = 0;

      try {
         BufferedReader var2 = reader("ingredients.tsv");

         try {
            Header var3 = UnifiedEvolutionCatalog.Header.read(var2);

            String var4;
            while((var4 = var2.readLine()) != null) {
               if (!var4.isEmpty()) {
                  String[] var5 = var4.split("\\t", -1);
                  Ingredient var6 = new Ingredient(var3.text(var5, "item_key"), var3.integer(var5, "runtime_material_id"), var3.text(var5, "name"), var3.integer(var5, "quantity"), var3.text(var5, "role"), var3.integer(var5, "position"), var3.text(var5, "provenance"));
                  if (var6.quantity <= 0 || var6.position < 0 || var6.itemKey.isEmpty() || var6.name.isEmpty()) {
                     throw new IllegalStateException("Invalid unified evolution ingredient " + var6.itemKey);
                  }

                  UnifiedItemRecord var7 = UnifiedItemCatalog.instance().byStableKey(var6.itemKey);
                  if (var7 == null || var7.inventoryKind != UnifiedItemInventoryKind.MATERIAL || var7.runtimeId != var6.runtimeMaterialId) {
                     throw new IllegalStateException("Unfrozen evolution Item mapping " + var6.itemKey);
                  }

                  var0.computeIfAbsent(var3.text(var5, "evolution_key"), (var0x) -> new ArrayList<>()).add(var6);
                  ++var1;
               }
            }
         } catch (Throwable var9) {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (var2 != null) {
            var2.close();
         }
      } catch (IOException var10) {
         throw new IllegalStateException("Cannot load unified evolution ingredients", var10);
      }

      if (var1 != 239) {
         throw new IllegalStateException("Expected 239 evolution ingredients, got " + var1);
      } else {
         for(List<Ingredient> var12 : var0.values()) {
            var12.sort(Comparator.comparingInt((var0x) -> var0x.position));
         }

         return var0;
      }
   }

   private static void validate(Rule var0) {
      if (var0.stableKey.isEmpty() || var0.fromPetKey.isEmpty() || var0.toPetKey.isEmpty() || var0.fromSpeciesId < 0 || var0.fromSpeciesId >= 202 || var0.toSpeciesId < 0 || var0.toSpeciesId >= 202 || var0.fromSpeciesId == var0.toSpeciesId || var0.requiredLevel <= 0 || var0.ingredients.isEmpty() || var0.topologyProvenance.isEmpty() || !"unified_design".equals(var0.recipeProvenance)) {
         throw new IllegalStateException("Invalid unified evolution " + var0.stableKey);
      }
   }

   private static void validate(MaterialSource var0) {
      UnifiedItemRecord var1 = UnifiedItemCatalog.instance().byStableKey(var0.itemKey);
      if (var1 != null && var1.inventoryKind == UnifiedItemInventoryKind.MATERIAL && var1.runtimeId == var0.runtimeMaterialId && !var0.name.isEmpty() && !var0.acquisitionRoute.isEmpty() && var0.repeatable && var0.unlockTask == 0 && var0.price >= 0 && var0.currencyCode >= 0 && var0.currencyCode <= 2) {
         if (var0.pricingPolicy == UnifiedEvolutionCatalog.PricingPolicy.SOURCE_PRICE) {
            boolean var2 = var0.tier >= 1 && var0.tier <= 3 && var0.price > 0 && var0.currencyCode <= 1;
            boolean var3 = "liet-hoa".equals(var0.sourceKey) && var0.shopOrder == -1 && "legacy_material_shop".equals(var0.acquisitionRoute) && "decoded_source".equals(var0.provenance);
            boolean var4 = "cau-vong".equals(var0.sourceKey) && var0.shopOrder >= 0 && var0.shopOrder < 15 && "unified_material_shop_overlay".equals(var0.acquisitionRoute) && "unified_design".equals(var0.provenance);
            if (!var2 || !var3 && !var4) {
               throw new IllegalStateException("Invalid priced material shop source " + var0.itemKey);
            }
         }

      } else {
         throw new IllegalStateException("Invalid evolution material source " + var0.itemKey);
      }
   }

   private static <K, V> Map<K, List<V>> freezeLists(Map<K, List<V>> var0) {
      LinkedHashMap var1 = new LinkedHashMap();

      for(Map.Entry var3 : var0.entrySet()) {
         var1.put(var3.getKey(), Collections.unmodifiableList(new ArrayList((Collection)var3.getValue())));
      }

      return Collections.unmodifiableMap(var1);
   }

   private static Properties properties(String var0) {
      Properties var1 = new Properties();

      try {
         InputStream var2 = resource(var0);

         Properties var3;
         try {
            var1.load(new InputStreamReader(var2, StandardCharsets.UTF_8));
            var3 = var1;
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
         throw new IllegalStateException("Cannot load unified evolution manifest", var7);
      }
   }

   private static BufferedReader reader(String var0) {
      return new BufferedReader(new InputStreamReader(resource(var0), StandardCharsets.UTF_8));
   }

   private static InputStream resource(String var0) {
      InputStream var1 = UnifiedEvolutionCatalog.class.getResourceAsStream("/vqsv/data/unified/evolutions/unified-evolution-v1/" + var0);
      if (var1 == null) {
         throw new IllegalStateException("Missing unified evolution resource " + var0);
      } else {
         return var1;
      }
   }

   private static void require(Properties var0, String var1, String var2) {
      if (!var2.equals(var0.getProperty(var1))) {
         throw new IllegalStateException("Unified evolution manifest mismatch for " + var1);
      }
   }

   public static enum PricingPolicy {
      PC_FREE,
      SOURCE_PRICE;

      // $FF: synthetic method
      private static PricingPolicy[] $values() {
         return new PricingPolicy[]{PC_FREE, SOURCE_PRICE};
      }
   }

   public static final class Rule {
      public final String stableKey;
      public final String fromPetKey;
      public final int fromSpeciesId;
      public final String toPetKey;
      public final int toSpeciesId;
      public final EvolutionCandidate.Kind kind;
      public final int requiredLevel;
      public final int costRank;
      public final String topologyProvenance;
      public final String recipeProvenance;
      public final List<Ingredient> ingredients;

      private Rule(String var1, String var2, int var3, String var4, int var5, EvolutionCandidate.Kind var6, int var7, int var8, String var9, String var10, List<Ingredient> var11) {
         this.stableKey = var1;
         this.fromPetKey = var2;
         this.fromSpeciesId = var3;
         this.toPetKey = var4;
         this.toSpeciesId = var5;
         this.kind = var6;
         this.requiredLevel = var7;
         this.costRank = var8;
         this.topologyProvenance = var9;
         this.recipeProvenance = var10;
         this.ingredients = Collections.unmodifiableList(new ArrayList(var11));
      }
   }

   public static final class Ingredient {
      public final String itemKey;
      public final int runtimeMaterialId;
      public final String name;
      public final int quantity;
      public final String role;
      public final int position;
      public final String provenance;

      private Ingredient(String var1, int var2, String var3, int var4, String var5, int var6, String var7) {
         this.itemKey = var1;
         this.runtimeMaterialId = var2;
         this.name = var3;
         this.quantity = var4;
         this.role = var5;
         this.position = var6;
         this.provenance = var7;
      }
   }

   public static final class MaterialSource {
      public final String itemKey;
      public final int runtimeMaterialId;
      public final String name;
      public final String sourceKey;
      public final String acquisitionRoute;
      public final int shopOrder;
      public final int tier;
      public final int price;
      public final int currencyCode;
      public final PricingPolicy pricingPolicy;
      public final boolean repeatable;
      public final int unlockTask;
      public final String provenance;

      private MaterialSource(String var1, int var2, String var3, String var4, String var5, int var6, int var7, int var8, int var9, PricingPolicy var10, boolean var11, int var12, String var13) {
         this.itemKey = var1;
         this.runtimeMaterialId = var2;
         this.name = var3;
         this.sourceKey = var4;
         this.acquisitionRoute = var5;
         this.shopOrder = var6;
         this.tier = var7;
         this.price = var8;
         this.currencyCode = var9;
         this.pricingPolicy = var10;
         this.repeatable = var11;
         this.unlockTask = var12;
         this.provenance = var13;
      }
   }

   private static final class Header {
      private final Map<String, Integer> indexes = new LinkedHashMap();

      static Header read(BufferedReader var0) throws IOException {
         String var1 = var0.readLine();
         if (var1 == null) {
            throw new IllegalStateException("Empty unified evolution resource");
         } else {
            Header var2 = new Header();
            String[] var3 = var1.split("\\t", -1);

            for(int var4 = 0; var4 < var3.length; ++var4) {
               var2.indexes.put(var3[var4], var4);
            }

            return var2;
         }
      }

      String text(String[] var1, String var2) {
         Integer var3 = (Integer)this.indexes.get(var2);
         if (var3 != null && var3 < var1.length) {
            return var1[var3];
         } else {
            throw new IllegalStateException("Missing unified evolution column " + var2);
         }
      }

      int integer(String[] var1, String var2) {
         return Integer.parseInt(this.text(var1, var2));
      }
   }
}
