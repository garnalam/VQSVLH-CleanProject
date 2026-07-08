final class VqsvBattlePetStateView {
    static final VqsvBattlePetStateView[] EMPTY_ARRAY = new VqsvBattlePetStateView[0];

    private static final String[] ELEMENT_NAMES = {
            "M\u1ed9c h\u1ec7",
            "Th\u1ed5 h\u1ec7",
            "Th\u1ee7y h\u1ec7",
            "H\u1ecfa h\u1ec7",
            "Qu\u1ef7 h\u1ec7",
            "Phong h\u1ec7",
            "\u0110i\u1ec7n h\u1ec7"
    };

    final int rowIndex;
    final int petIndex;
    final boolean visible;
    final boolean alive;
    final boolean active;
    final int speciesId;
    final String name;
    final int visualId;
    final int level;
    final int hp;
    final int maxHp;
    final int hpPercent;
    final int expPercent;
    final int attack;
    final int defense;
    final int speed;
    final int elementId;
    final String elementName;
    final String relationText;
    final String evolutionText;
    final int heldItemId;
    final int heldItemIconId;
    final String heldItemName;
    final int filledStars;
    final int visibleStars;

    private VqsvBattlePetStateView(int rowIndex, int petIndex, boolean visible, boolean alive,
                                   boolean active, int speciesId, String name, int visualId,
                                   int level, int hp, int maxHp, int hpPercent, int expPercent,
                                   int attack, int defense, int speed, int elementId,
                                   String elementName, String relationText, String evolutionText,
                                   int heldItemId, int heldItemIconId, String heldItemName,
                                   int filledStars, int visibleStars) {
        this.rowIndex = rowIndex;
        this.petIndex = petIndex;
        this.visible = visible;
        this.alive = alive;
        this.active = active;
        this.speciesId = speciesId;
        this.name = name;
        this.visualId = visualId;
        this.level = level;
        this.hp = hp;
        this.maxHp = maxHp;
        this.hpPercent = hpPercent;
        this.expPercent = expPercent;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.elementId = elementId;
        this.elementName = elementName;
        this.relationText = relationText;
        this.evolutionText = evolutionText;
        this.heldItemId = heldItemId;
        this.heldItemIconId = heldItemIconId;
        this.heldItemName = heldItemName;
        this.filledStars = filledStars;
        this.visibleStars = visibleStars;
    }

    static VqsvBattlePetStateView empty(int rowIndex) {
        return new VqsvBattlePetStateView(rowIndex, -1, false, false, false, -1, "",
                -1, 0, 0, 1, 0, 0, 0, 0, 0, -1, "", "", "",
                -1, -1, "", 0, 0);
    }

    static VqsvBattlePetStateView fromPet(int rowIndex, int petIndex, SourcePetState pet, boolean active) {
        BattleUnit battle = BattleUnit.fromSourcePet(pet, (byte) 0);
        SourceBattleUnit render = battle.toRenderUnit(true);
        BattleSpeciesRow species = VqsvBattleTables.instance().species(pet.speciesId);
        int heldItemId = sourcePayloadValue(pet, 2, -1);
        BattleItemRow heldItem = heldItemId >= 0 ? VqsvBattleTables.instance().item(heldItemId) : null;
        int exp = sourcePayloadValue(pet, 7, battle.exp);
        int speciesRarity = species == null ? 5 : VqsvBattleTables.get(species.raw, 4, 5);
        int quality = sourcePayloadValue(pet, 4, battle.baseStats[BattleUnit.STAT_QUALITY]);
        int evolutionSpecies = species == null ? -1 : VqsvBattleTables.get(species.raw, 19, -1);

        return new VqsvBattlePetStateView(
                rowIndex,
                petIndex,
                true,
                battle.alive(),
                active,
                pet.speciesId,
                render.name,
                render.visualId,
                battle.level,
                battle.hp(),
                battle.maxHp(),
                percent(battle.hp(), battle.maxHp()),
                percent(exp, sourceNextExp(battle.level)),
                battle.currentStats[BattleUnit.STAT_ATTACK],
                battle.currentStats[BattleUnit.STAT_DEFENSE],
                battle.currentStats[BattleUnit.STAT_SPEED],
                render.element,
                elementName(render.element),
                elementName(render.element),
                evolutionText(evolutionSpecies),
                heldItemId,
                heldItem == null ? -1 : heldItem.iconId,
                heldItem == null ? "" : heldItem.name(""),
                clamp(quality, 0, 5),
                clamp(speciesRarity, 0, 5)
        );
    }

    private static String evolutionText(int evolutionSpecies) {
        if (evolutionSpecies < 0) {
            return "";
        }
        BattleSpeciesRow species = VqsvBattleTables.instance().species(evolutionSpecies);
        int evolutionKind = species == null ? -1 : VqsvBattleTables.get(species.raw, 2, -1);
        if (evolutionKind == 1 || evolutionKind == 2) {
            return "C\u00f3 th\u1ec3 ti\u1ebfn h\u00f3a";
        }
        if (evolutionKind == 3) {
            return "C\u00f3 th\u1ec3 d\u1ecb ho\u00e1";
        }
        return "";
    }

    private static String elementName(int elementId) {
        if (elementId < 0 || elementId >= ELEMENT_NAMES.length) {
            return "";
        }
        return ELEMENT_NAMES[elementId];
    }

    private static int sourcePayloadValue(SourcePetState pet, int index, int fallback) {
        if (pet == null || pet.sourcePayload == null || index < 0 || index >= pet.sourcePayload.length) {
            return fallback;
        }
        return pet.sourcePayload[index];
    }

    private static int sourceNextExp(int level) {
        int next = level >= 50 ? 50 : Math.max(1, level + 1);
        return Math.max(1, next * 15 * next - 200);
    }

    private static int percent(int value, int max) {
        return clamp(value * 100 / Math.max(1, max), 0, 100);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
