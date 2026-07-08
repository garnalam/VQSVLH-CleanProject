import java.util.List;

final class BagItem {
    final int id;
    final int bagChannel;
    final boolean keepAtZero;
    int count;

    BagItem(int id, int count, int bagChannel, boolean keepAtZero) {
        this.id = id;
        this.count = count;
        this.bagChannel = bagChannel;
        this.keepAtZero = keepAtZero;
    }
}

final class SourceItem {
    final int id;
    final int textId;
    final String name;
    final int bagChannel;

    SourceItem(int id, int textId, String name, int bagChannel) {
        this.id = id;
        this.textId = textId;
        this.name = name;
        this.bagChannel = bagChannel;
    }
}

final class SourceSpecialReward {
    final int id;
    final int textId;
    final int iconId;
    final int descriptionTextId;
    final String name;
    boolean unlocked;
    int stackCount;
    String gameGPath = "";

    SourceSpecialReward(int id, int textId, int iconId, int descriptionTextId, String name) {
        this.id = id;
        this.textId = textId;
        this.iconId = iconId;
        this.descriptionTextId = descriptionTextId;
        this.name = name;
    }

    static SourceSpecialReward fromSourceDb(int rewardId) {
        if (rewardId == 5) {
            return new SourceSpecialReward(5, 300, 47, 308, VqsvText.Items.PET_BOOK_PAGE);
        }
        return new SourceSpecialReward(rewardId, 0, 0, 0, "Reward " + rewardId);
    }

    void applySourceGameGSemantics(int qty) {
        if (id == 7 || id == 8 || id == 9) {
            stackCount = Math.min(99, stackCount + qty);
            gameGPath = "game.g.d -> game.g.c stack special item";
            return;
        }
        unlocked = true;
        if (id == 0) {
            gameGPath = "game.g.d -> game.g.e(id,-1) mark active special";
        } else {
            gameGPath = "game.g.d -> game.g.i(id) unlock vector entry";
        }
    }
}

final class SourceBattleUnit {
    final int speciesId;
    final int level;
    final int nature;
    final String name;
    final int maxHp;
    int hp;
    final int attack;
    final int defense;
    final int speed;
    final int element;
    final int visualId;
    final int relationClass;
    final BattleUnit battleUnit;

    SourceBattleUnit(int speciesId, int level, int nature, String name,
                     int maxHp, int attack, int defense, int speed,
                     int element, int visualId, int relationClass) {
        this(speciesId, level, nature, name, maxHp, attack, defense, speed,
                element, visualId, relationClass, null);
    }

    SourceBattleUnit(int speciesId, int level, int nature, String name,
                     int maxHp, int attack, int defense, int speed,
                     int element, int visualId, int relationClass, BattleUnit battleUnit) {
        this.speciesId = speciesId;
        this.level = level;
        this.nature = nature;
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.element = element;
        this.visualId = visualId;
        this.relationClass = relationClass;
        this.battleUnit = battleUnit;
    }

    static SourceBattleUnit enemyFromEncounter(int[] encounter) {
        return BattleUnit.enemyFromEncounter(encounter).toRenderUnit(false);
    }

    static SourceBattleUnit playerFromSourcePets(List<SourcePetState> pets) {
        if (pets.isEmpty()) {
            throw new IllegalStateException("SourceBattleUnit requires at least one source pet");
        }
        return BattleUnit.fromSourcePet(pets.get(0), (byte) 0).toRenderUnit(true);
    }

    static SourceBattleUnit fromSpecies(int species, int level, int nature, boolean playerSide) {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(species);
        if (row == null || !row.validForBattle()) {
            String fallbackName = playerSide ? "Pet " + species : "Enemy " + species;
            return fallback(species, level, nature, fallbackName, 80 + level * 4, 18 + level, 8 + level / 2, 8);
        }
        int hp = row.statHp(level, nature);
        int atk = row.statAttack(level, nature);
        int def = row.statDefense(level, nature);
        int spd = row.statSpeed(level, nature);
        String sourceName = row.name(playerSide ? "Pet " + species : "Enemy " + species);
        return new SourceBattleUnit(species, level, nature, sourceName,
                Math.max(1, hp), Math.max(1, atk), Math.max(0, def), Math.max(1, spd),
                row.element, row.spriteId, row.relationClass);
    }

    static SourceBattleUnit fallback(int species, int level, int nature, String name,
                                     int maxHp, int attack, int defense, int speed) {
        return new SourceBattleUnit(species, level, nature, name,
                Math.max(1, maxHp), Math.max(1, attack), Math.max(0, defense), Math.max(1, speed),
                -1, -1, 0);
    }

    boolean alive() {
        return battleUnit == null ? hp > 0 : battleUnit.alive();
    }

    int nextLevelEnergy() {
        if (level >= 50) {
            return 37300;
        }
        return Math.max(1, (level + 1) * 15 * (level + 1) - 200);
    }

    int basicDamageTo(SourceBattleUnit target) {
        return damageResultTo(target).damage;
    }

    BattleDamageResult damageResultTo(SourceBattleUnit target) {
        if (battleUnit != null && target.battleUnit != null) {
            return battleUnit.computeDamage(target.battleUnit);
        }
        int raw = attack - target.defense;
        int levelPart = Math.max(1, level / 2);
        int damage = Math.max(1, raw + levelPart);
        byte relation = elementRelationTo(target);
        if (relation == 0) {
            damage = damage * 3 / 2;
        } else if (relation == 1) {
            damage = Math.max(1, damage * 2 / 3);
        }
        return new BattleDamageResult(damage, 0, -1);
    }

    void damage(int amount) {
        if (battleUnit != null) {
            battleUnit.damage(amount);
            hp = battleUnit.hp();
            return;
        }
        hp = Math.max(0, hp - Math.max(1, amount));
    }

    int applySourceBuff(int buffId, int value, int sourceSkill) {
        if (battleUnit == null) {
            return 0;
        }
        int heal = battleUnit.applySourceBuff(buffId, value, sourceSkill);
        hp = battleUnit.hp();
        return heal;
    }

    byte elementRelationTo(SourceBattleUnit target) {
        boolean attackerEffective = true;
        boolean defenderEffective = true;
        if (relationClass == 2 && target.relationClass == 2) {
            attackerEffective = true;
            defenderEffective = true;
        } else if (relationClass == 2 && target.relationClass != 2) {
            attackerEffective = true;
            defenderEffective = false;
        } else if (relationClass != 2 && target.relationClass == 2) {
            attackerEffective = false;
            defenderEffective = true;
        }
        if (attackerEffective && beats(element, target.element)) {
            return 0;
        }
        if (defenderEffective && beats(target.element, element)) {
            return 1;
        }
        return -1;
    }

    private static boolean beats(int a, int b) {
        return (a == 0 && b == 1)
                || (a == 1 && b == 2)
                || (a == 2 && b == 3)
                || (a == 3 && b == 0)
                || (a == 5 && b == 6)
                || (a == 6 && b == 4)
                || (a == 4 && b == 5);
    }

    @Override
    public String toString() {
        return name + "(species=" + speciesId
                + ",lv=" + level
                + ",nature=" + nature
                + ",hp=" + maxHp
                + ",atk=" + attack
                + ",def=" + defense
                + ",spd=" + speed
                + ",element=" + element
                + ",visual=" + visualId
                + ",relationClass=" + relationClass + ")";
    }
}

final class SourcePetState {
    int speciesId;
    int level;
    int slot;
    int arg3;
    int arg4;
    final int[] skillIds = new int[]{-1, -1, -1, -1};
    final int[] skillCooldowns = new int[skillIds.length];
    int[] sourcePayload;
    int refreshCount;

    SourcePetState() {
    }

    SourcePetState(int slot, int speciesId, int level, int arg3, int arg4, int skillA, int skillB) {
        this.slot = slot;
        this.speciesId = speciesId;
        this.level = level;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.skillIds[0] = skillA;
        this.skillIds[1] = skillB;
        refreshFromSourceDb();
        sourcePayload = toSourcePayload();
    }

    static SourcePetState caughtFromBattleUnit(int slot, SourceBattleUnit unit) {
        SourcePetState pet = new SourcePetState();
        pet.slot = slot;
        pet.speciesId = unit.speciesId;
        pet.level = unit.level;
        pet.arg3 = unit.nature;
        pet.arg4 = 0;
        BattleUnit battle = unit.battleUnit;
        if (battle != null) {
            pet.arg3 = battle.baseStats[BattleUnit.STAT_QUALITY];
            pet.arg4 = battle.natureType;
            int count = Math.min(pet.skillIds.length, battle.skillCount);
            for (int i = 0; i < count; i++) {
                pet.skillIds[i] = battle.skillIds[i];
                pet.skillCooldowns[i] = battle.skillPp[i];
            }
            pet.sourcePayload = pet.toSourcePayloadFromBattleUnit(battle);
        } else {
            pet.sourcePayload = pet.toSourcePayload();
        }
        pet.refreshCount++;
        return pet;
    }

    void refreshFromSourceDb() {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] != -1) {
                skillCooldowns[i] = sourceSkillCooldown(skillIds[i]);
            }
        }
        sourcePayload = toSourcePayload();
        refreshCount++;
    }

    private static int sourceSkillCooldown(int skillId) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        return row == null ? 0 : row.ppMax;
    }

    int[] toSourcePayload() {
        int skillCount = 0;
        for (int skillId : skillIds) {
            if (skillId != -1) {
                skillCount++;
            }
        }
        int[] payload = new int[10 + skillCount * 2];
        payload[0] = speciesId;
        payload[1] = level;
        payload[2] = -1;
        payload[3] = -1;
        payload[4] = arg3;
        payload[5] = arg4;
        payload[6] = sourceMaxHp();
        payload[7] = 0;
        payload[8] = 0;
        payload[9] = skillCount;
        int out = 0;
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] == -1) {
                continue;
            }
            payload[10 + out] = skillIds[i];
            payload[10 + skillCount + out] = skillCooldowns[i];
            out++;
        }
        return payload;
    }

    private int sourceMaxHp() {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(speciesId);
        if (row == null || !row.validForBattle()) {
            return Math.max(1, 80 + Math.max(1, level) * 4);
        }
        int quality = arg3 <= 0 ? 3 : arg3;
        if (sourcePayload != null && sourcePayload.length > 4 && sourcePayload[4] > 0) {
            quality = sourcePayload[4];
        }
        return Math.max(1, row.statHp(Math.max(1, level), quality));
    }

    void persistBattleUnit(BattleUnit battle) {
        if (battle == null) {
            return;
        }
        speciesId = battle.speciesId;
        level = battle.level;
        arg3 = battle.baseStats[BattleUnit.STAT_QUALITY];
        arg4 = battle.natureType;
        for (int i = 0; i < skillIds.length; i++) {
            skillIds[i] = -1;
            skillCooldowns[i] = 0;
        }
        int count = Math.min(skillIds.length, battle.skillCount);
        for (int i = 0; i < count; i++) {
            skillIds[i] = battle.skillIds[i];
            skillCooldowns[i] = battle.skillPp[i];
        }
        sourcePayload = toSourcePayloadFromBattleUnit(battle);
        refreshCount++;
    }

    private int[] toSourcePayloadFromBattleUnit(BattleUnit battle) {
        int skillCount = Math.max(0, battle.skillCount);
        int[] payload = new int[10 + skillCount * 2];
        payload[0] = battle.speciesId;
        payload[1] = battle.level;
        payload[2] = battle.baseStats[BattleUnit.STAT_FORM];
        payload[3] = battle.currentStats[BattleUnit.STAT_SIDE_FLAG];
        payload[4] = battle.baseStats[BattleUnit.STAT_QUALITY];
        payload[5] = battle.natureType;
        payload[6] = battle.currentStats[BattleUnit.STAT_HP];
        payload[7] = battle.exp;
        payload[8] = battle.visualSpriteId;
        payload[9] = skillCount;
        for (int i = 0; i < skillCount; i++) {
            payload[10 + i] = battle.skillIds[i];
            payload[10 + skillCount + i] = battle.skillPp[i];
        }
        return payload;
    }
}
