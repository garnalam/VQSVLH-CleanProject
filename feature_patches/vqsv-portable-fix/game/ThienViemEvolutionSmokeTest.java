package vqsv.game;

import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.UnifiedSkillLearnsetCatalog;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.pet.PetState;
import vqsv.pet.data.PetTaxonomyCatalog;
import vqsv.pet.data.ThienViemHoCatalog;
import vqsv.progression.EvolutionCandidate;
import vqsv.progression.EvolutionProgression;
import vqsv.progression.PetCollectionProgression;
import vqsv.render.SpriteAnimator;
import vqsv.source.PetSourceAdapter;

/** Headless verification for LH-004 -> Thiên Viêm Hồ. */
public final class ThienViemEvolutionSmokeTest {
   private ThienViemEvolutionSmokeTest() {}

   public static void main(String[] args) {
      EvolutionCandidate candidate = VqsvSourceEvolutionRuntime.noticeForSpecies(4, 15, EvolutionCandidate.Kind.EVOLUTION);
      require(candidate != null, "missing evolution candidate");
      require(candidate.targetSpeciesId == ThienViemHoCatalog.RUNTIME_ID, "wrong target species");
      require(candidate.requiredLevel == 15, "wrong required level");

      BattleSpeciesRow source = VqsvBattleTables.instance().species(4);
      BattleSpeciesRow target = VqsvBattleTables.instance().species(candidate.targetSpeciesId);
      require(source != null && target != null && target.validForBattle(), "missing battle row");
      require(ThienViemHoCatalog.NAME.equals(target.name("")), "wrong target name");
      require(target.spriteId == ThienViemHoCatalog.VISUAL_ID, "wrong target sprite");
      for (int quality = 1; quality <= 5; ++quality) {
         for (int level : new int[]{1, 15, 30, 50}) {
            require(target.statHp(level, quality) == source.statHp(level, quality), "HP changed at level " + level + " quality " + quality);
            require(target.statAttack(level, quality) > source.statAttack(level, quality), "strength not increased");
            require(target.statAttack(level, quality) <= source.statAttack(level, quality) * 5 / 4 + 1, "strength increased too much");
            require(target.statDefense(level, quality) == source.statDefense(level, quality), "defense changed");
            require(target.statSpeed(level, quality) > source.statSpeed(level, quality), "agility not increased");
         }
      }
      require(PetTaxonomyCatalog.instance().byRuntimeSpeciesId(candidate.targetSpeciesId) != null, "missing taxonomy");
      require(UnifiedSkillLearnsetCatalog.instance().entries(ThienViemHoCatalog.STABLE_KEY).size() == 15, "wrong learnset");
      require(SpriteAnimator.load(ThienViemHoCatalog.VISUAL_ID) != null, "sprite load failed");

      PetState pet = PetSourceAdapter.create(0, 4, 15, 3, 2, -1, -1);
      EvolutionProgression progression = new EvolutionProgression();
      progression.apply(pet, candidate, target.statHp(15, 3), target.spriteId, target.defaultQuality());
      require(pet.speciesId == ThienViemHoCatalog.RUNTIME_ID, "evolution did not change species");
      require(pet.visualSpriteId == ThienViemHoCatalog.VISUAL_ID, "evolution did not change sprite");

      PetCollectionProgression collection = new PetCollectionProgression();
      require(collection.markCollected(ThienViemHoCatalog.RUNTIME_ID), "collection rejected new species");
      require(collection.snapshot().length == 278, "collection snapshot is not expanded");
      System.out.println("THIEN_VIEM_EVOLUTION_SMOKE_TEST_OK");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new IllegalStateException(message);
   }
}
