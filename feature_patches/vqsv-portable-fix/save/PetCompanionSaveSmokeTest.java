package vqsv.save;

import java.util.Collections;
import java.util.Properties;
import vqsv.battle.data.PetStatProfileVersion;
import vqsv.pet.PetState;
import vqsv.session.GameSession;
import vqsv.source.PetSourceAdapter;

/** Verifies that the chosen world companion survives a properties save round-trip. */
public final class PetCompanionSaveSmokeTest {
   private PetCompanionSaveSmokeTest() {}

   public static void main(String[] args) {
      GameSession<Object, Object> session = new GameSession<>(240, 320, new Object());
      PetState pet = PetSourceAdapter.create(5, 4, 15, 3, 2, -1, -1);
      session.pets.roster.add(pet);
      session.pets.companionPetSlot = 5;
      SaveSessionSnapshotMapper mapper = new SaveSessionSnapshotMapper();
      SaveSnapshot snapshot = mapper.capture(session, new SaveSnapshot.Player(10, 20, 0, true), Collections.emptyList(), 0, PetStatProfileVersion.UNIFIED_PET_GROWTH_V6, new SaveSessionSnapshotMapper.PetHpSnapshotResolver() {
         public int currentHpForSave(PetState value) { return value.currentHp; }
         public int maxHpForSave(PetState value) { return value.currentHp; }
      });
      SavePropertiesCodec codec = new SavePropertiesCodec();
      Properties properties = codec.encode(snapshot);
      SaveSnapshot restored = codec.decode(properties);
      require(restored.pets.companionPetSlot == 5, "companion slot was not saved");
      require(restored.pets.roster.size() == 1, "roster changed during save round-trip");
      System.out.println("PET_COMPANION_SAVE_SMOKE_TEST_OK");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new IllegalStateException(message);
   }
}
