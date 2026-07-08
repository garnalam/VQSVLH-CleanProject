final class VqsvSourceEvolutionRuntime {
    private static final int[] SOURCE_LEVELS = {12, 30, 5};

    private VqsvSourceEvolutionRuntime() {
    }

    static SourceEvolutionNotice noticeForPet(VqsvIntroDemo.Scene s, int petIndex) {
        if (petIndex < 0 || petIndex >= s.sourcePets.size()) {
            return null;
        }
        SourcePetState pet = s.sourcePets.get(petIndex);
        BattleSpeciesRow current = VqsvBattleTables.instance().species(pet.speciesId);
        if (current == null || !current.validForBattle()) {
            return null;
        }
        int targetSpecies = VqsvBattleTables.get(current.raw, 19, -1);
        if (targetSpecies < 0) {
            return null;
        }
        BattleSpeciesRow target = VqsvBattleTables.instance().species(targetSpecies);
        if (target == null || !target.validForBattle()) {
            return null;
        }
        int targetKind = VqsvBattleTables.get(target.raw, 2, -1);
        int sourceR = sourceEvolutionKind(targetKind);
        int requiredLevel = sourceEvolutionRequiredLevel(targetKind);
        int materialId = VqsvBattleTables.get(current.raw, 20, -13) + 12;
        int materialNeed = VqsvBattleTables.get(current.raw, 21, 0);
        int materialCount = materialCount(s, materialId);
        return new SourceEvolutionNotice(pet.speciesId,
                VqsvBattleTables.get(current.raw, 0, -1),
                pet.level,
                targetSpecies,
                VqsvBattleTables.get(target.raw, 0, -1),
                targetKind,
                requiredLevel,
                materialId,
                materialNeed,
                materialCount,
                sourceR,
                materialNeed <= 0 || materialCount >= materialNeed);
    }

    static int[] visibleStats(SourcePetState pet) {
        if (pet == null) {
            return new int[]{0, 0, 0, 0};
        }
        BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
        return unit.sourceVisibleStats();
    }

    static int[] targetVisibleStats(SourcePetState pet, int targetSpecies) {
        if (pet == null || targetSpecies < 0) {
            return new int[]{0, 0, 0, 0};
        }
        SourcePetState target = new SourcePetState(pet.slot, targetSpecies, pet.level,
                pet.arg3, pet.arg4, -1, -1);
        BattleUnit unit = BattleUnit.fromSourcePet(target, (byte) 0);
        return unit.sourceVisibleStats();
    }

    static String materialName(SourceEvolutionNotice notice) {
        if (notice == null || notice.materialId < 0) {
            return "";
        }
        BattleItemRow item = VqsvBattleTables.instance().item(notice.materialId);
        return item == null ? "T\u00e0i li\u1ec7u " + notice.materialId : item.name("T\u00e0i li\u1ec7u " + notice.materialId);
    }

    static int materialCount(VqsvIntroDemo.Scene s, int materialId) {
        if (materialId < 0) {
            return 0;
        }
        SourceSpecialReward reward = s.sourceSpecialRewards.get(materialId);
        return reward == null ? 0 : Math.max(0, reward.stackCount);
    }

    static void consumeMaterial(VqsvIntroDemo.Scene s, int materialId, int amount) {
        if (materialId < 0 || amount <= 0) {
            return;
        }
        SourceSpecialReward reward = s.sourceSpecialRewards.computeIfAbsent(materialId, SourceSpecialReward::fromSourceDb);
        reward.stackCount = Math.max(0, reward.stackCount - amount);
    }

    static void mutatePet(VqsvIntroDemo.Scene s, int petIndex, SourceEvolutionNotice notice) {
        if (notice == null || petIndex < 0 || petIndex >= s.sourcePets.size()) {
            return;
        }
        SourcePetState pet = s.sourcePets.get(petIndex);
        int oldExp = pet.sourcePayload != null && pet.sourcePayload.length > 7 ? pet.sourcePayload[7] : 0;
        pet.speciesId = notice.targetSpeciesId;
        BattleSpeciesRow target = VqsvBattleTables.instance().species(notice.targetSpeciesId);
        int visual = target == null ? -1 : VqsvBattleTables.get(target.raw, 17, -1);
        BattleUnit targetUnit = BattleUnit.fromSourcePet(pet, (byte) 0);
        pet.sourcePayload = pet.toSourcePayload();
        pet.sourcePayload[0] = notice.targetSpeciesId;
        pet.sourcePayload[6] = targetUnit.maxHp();
        pet.sourcePayload[7] = oldExp;
        pet.sourcePayload[8] = visual;
        pet.refreshCount++;
        s.sourceStateTrace.add("PORTED/PARTIAL game.h.bh mutate pet index=" + petIndex
                + " species=" + notice.currentSpeciesId + "->" + notice.targetSpeciesId
                + " visual=" + visual
                + " hp=" + pet.sourcePayload[6]
                + " expPreserved=" + oldExp);
    }

    static int sourceEvolutionKind(int targetKind) {
        if (targetKind == 1 || targetKind == 2) {
            return 1;
        }
        if (targetKind == 3) {
            return 2;
        }
        return 0;
    }

    static int sourceEvolutionRequiredLevel(int targetKind) {
        int index = targetKind - 1;
        if (index < 0 || index >= SOURCE_LEVELS.length) {
            return Integer.MAX_VALUE;
        }
        return SOURCE_LEVELS[index];
    }
}
