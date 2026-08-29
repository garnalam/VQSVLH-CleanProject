package vqsv.game;

import vqsv.battle.data.BattleSpeciesRow;
import vqsv.battle.data.UnifiedSkillLearnsetCatalog;
import vqsv.battle.data.VqsvBattleTables;
import vqsv.pet.PetState;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.PetTaxonomyCatalog;
import vqsv.progression.EvolutionCandidate;
import vqsv.progression.EvolutionProgression;
import vqsv.progression.PetCollectionProgression;
import vqsv.render.SpriteAnimator;
import vqsv.source.PetSourceAdapter;

/** Headless verification for the project-local LH-064 evolution. */
public final class HoaDiemEvolutionSmokeTest {
   private HoaDiemEvolutionSmokeTest() {}

   public static void main(String[] args) {
      EvolutionCandidate candidate = VqsvSourceEvolutionRuntime.noticeForSpecies(64, 40, EvolutionCandidate.Kind.EVOLUTION);
      require(candidate != null, "missing evolution candidate");
      require(candidate.targetSpeciesId == HoaDiemHauVuongCatalog.RUNTIME_ID, "wrong target species");
      require(candidate.requiredLevel == 40, "wrong required level");
      require(candidate.ingredients.size() == 3, "wrong ingredient recipe");

      BattleSpeciesRow target = VqsvBattleTables.instance().species(candidate.targetSpeciesId);
      require(target != null && target.validForBattle(), "target has no battle row");
      require(HoaDiemHauVuongCatalog.NAME.equals(target.name("")), "wrong target name");
      require(target.spriteId == HoaDiemHauVuongCatalog.VISUAL_ID, "wrong target sprite");
      require(target.statHp(40, 4) > 0 && target.statAttack(40, 4) > 0, "invalid target stats");
      require(PetTaxonomyCatalog.instance().byRuntimeSpeciesId(candidate.targetSpeciesId) != null, "missing taxonomy");
      require(UnifiedSkillLearnsetCatalog.instance().entries(HoaDiemHauVuongCatalog.STABLE_KEY).size() == 15, "wrong learnset");
      require(SpriteAnimator.load(HoaDiemHauVuongCatalog.VISUAL_ID) != null, "sprite load failed");

      PetState pet = PetSourceAdapter.create(0, 64, 40, 4, 2, -1, -1);
      int preservedExperience = pet.experience;
      EvolutionProgression progression = new EvolutionProgression();
      progression.apply(pet, candidate, target.statHp(40, 4), target.spriteId, target.defaultQuality());
      require(pet.speciesId == HoaDiemHauVuongCatalog.RUNTIME_ID, "evolution did not change species");
      require(pet.visualSpriteId == HoaDiemHauVuongCatalog.VISUAL_ID, "evolution did not change sprite");
      require(pet.experience == preservedExperience, "evolution did not preserve experience");

      PetCollectionProgression collection = new PetCollectionProgression();
      require(collection.markCollected(HoaDiemHauVuongCatalog.RUNTIME_ID), "collection rejected new species");
      require(collection.snapshot().length >= 277, "collection snapshot is not expanded");
      System.out.println("HOA_DIEM_EVOLUTION_SMOKE_TEST_OK");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new IllegalStateException(message);
   }
}
