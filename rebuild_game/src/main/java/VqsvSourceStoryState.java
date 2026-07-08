final class VqsvSourceStoryState {
    private VqsvSourceStoryState() {
    }

    static SourcePetState initialDienMieuPet() {
        // Source chain: game.k initial player spawn -> game.g.I() -> a(68,7,(byte)2,(short)2,{1,40,45}).
        return new SourcePetState(0, 68, 7, 2, 2, 40, 45);
    }

    static boolean ensureInitialDienMieu(VqsvIntroDemo.Scene s, String reason) {
        if (!s.sourcePets.isEmpty()) {
            return false;
        }
        SourcePetState pet = initialDienMieuPet();
        s.sourcePets.add(pet);
        s.sourceStateTrace.add("PORTED source initial pet game.k->game.g.I species=68 level=7"
                + " quality=2 nature=2 skills=[40,45] reason=" + reason);
        return true;
    }
}
