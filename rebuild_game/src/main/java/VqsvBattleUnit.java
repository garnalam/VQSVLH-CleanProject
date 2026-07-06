import java.util.Arrays;
import java.util.Random;

final class BattleUnit {
    private static final Random BATTLE_RANDOM = new Random(0x56515356L);
    private static final int[] SOURCE_FINAL_VISUAL_BY_ELEMENT = {-1, -1, -1, -1, -1, -1, -1};

    static final int STAT_QUALITY = 0;
    static final int STAT_HP = 1;
    static final int STAT_ATTACK = 2;
    static final int STAT_DEFENSE = 3;
    static final int STAT_SPEED = 4;
    static final int STAT_FORM = 5;
    static final int STAT_SIDE_FLAG = 6;

    final short[] baseStats = new short[23];       // game.b.c[]
    final short[] currentStats = new short[23];    // game.b.d[]
    final short[][] buffSlots = new short[16][5];  // game.b.v[][]
    final short[][] debuffSlots = new short[11][5];// game.b.w[][]
    final byte[][] activeEffectQueue = new byte[][]{{-1, -1, -1}, {-1, -1, -1}}; // game.b.x[][]
    final byte[] activeEffectCount = new byte[2];  // game.b.N[]
    final short[] skillPp = new short[5];          // game.b.y[]
    final byte[] skillIds = new byte[5];           // game.b.z[]
    final short[] previewStats = new short[4];     // game.b.P[]
    final short[] effectScratch = new short[16];   // game.b.K[]

    int speciesId;                 // game.b.V
    int level;                     // game.b.T
    int exp;                       // game.b.S
    int visualSpriteId;            // game.b.C
    int ownerSide;                 // game.b.X, 0 player / 1 enemy in current usage
    byte skillCount;               // game.b.O
    byte selectedSkillId = -1;     // game.b.D
    byte selectedTargetSlot;       // game.b.I
    byte natureType;               // game.b.W
    boolean turnUsed;              // game.b.J
    BattleUnit target;             // game.b.p
    boolean sourcePassiveTargetDefenseBoost;
    boolean sourcePassiveWorldDamageBoost3;
    boolean sourcePassiveDamageBoost6;
    boolean sourcePassiveDebuffDurationHalve;

    BattleUnit() {
        Arrays.fill(skillIds, (byte) -1);
    }

    static BattleUnit enemyFromEncounter(int[] encounter) {
        int species = encounter.length > 0 ? encounter[0] : -1;
        int level = encounter.length > 1 ? encounter[1] : 1;
        int nature = encounter.length > 2 ? encounter[2] : 3;
        return fromSpecies(species, level, (short) -1, (byte) 1, (short) nature, (byte) 0, null);
    }

    static BattleUnit playerFromSourcePets(java.util.List<SourcePetState> pets) {
        if (pets.isEmpty()) {
            return neilFallback();
        }
        SourcePetState pet = pets.get(0);
        BattleUnit unit = fromSpecies(pet.speciesId, Math.max(1, pet.level), (short) -1,
                (byte) 0, (short) 3, (byte) 0, pet);
        unit.ownerSide = 0;
        return unit;
    }

    static BattleUnit neilFallback() {
        BattleUnit unit = new BattleUnit();
        unit.speciesId = -1;
        unit.level = 1;
        unit.ownerSide = 0;
        unit.visualSpriteId = -1;
        unit.baseStats[STAT_QUALITY] = 3;
        unit.baseStats[STAT_HP] = 120;
        unit.baseStats[STAT_ATTACK] = 22;
        unit.baseStats[STAT_DEFENSE] = 12;
        unit.baseStats[STAT_SPEED] = 10;
        unit.copyBaseToCurrent();
        return unit;
    }

    static BattleUnit fromSpecies(int speciesId, int level, short form, byte sideFlag,
                                  short quality, byte natureType, SourcePetState sourcePet) {
        BattleUnit unit = new BattleUnit();
        unit.speciesId = speciesId;
        unit.level = level;
        unit.natureType = natureType;
        unit.ownerSide = sideFlag == 1 ? 1 : 0;

        BattleSpeciesRow row = VqsvBattleTables.instance().species(speciesId);
        if (row == null || !row.validForBattle()) {
            unit.baseStats[STAT_QUALITY] = quality <= 0 ? (short) 3 : quality;
            unit.baseStats[STAT_HP] = (short) (80 + level * 4);
            unit.baseStats[STAT_ATTACK] = (short) (18 + level);
            unit.baseStats[STAT_DEFENSE] = (short) (8 + level / 2);
            unit.baseStats[STAT_SPEED] = 8;
            unit.visualSpriteId = -1;
        } else {
            unit.baseStats[STAT_QUALITY] = quality <= 0 ? 3 : quality;
            unit.baseStats[STAT_HP] = toShort(row.statHp(level, unit.baseStats[STAT_QUALITY]));
            unit.baseStats[STAT_ATTACK] = toShort(row.statAttack(level, unit.baseStats[STAT_QUALITY]));
            unit.baseStats[STAT_DEFENSE] = toShort(row.statDefense(level, unit.baseStats[STAT_QUALITY]));
            unit.baseStats[STAT_SPEED] = toShort(row.statSpeed(level, unit.baseStats[STAT_QUALITY]));
            unit.baseStats[STAT_FORM] = form;
            unit.baseStats[STAT_SIDE_FLAG] = sideFlag;
            unit.visualSpriteId = row.spriteId;
        }

        unit.applyNatureType(natureType);
        unit.copyBaseToCurrent();
        if (sourcePet != null) {
            unit.loadSkills(sourcePet.skillIds, sourcePet.skillCooldowns);
        } else {
            unit.loadDefaultSkillsFromSpecies();
        }
        return unit;
    }

    SourceBattleUnit toRenderUnit(boolean playerSide) {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(speciesId);
        String fallback = speciesId < 0 && playerSide ? "Neil" : playerSide ? "Pet " + speciesId : "Enemy " + speciesId;
        String name = row == null ? fallback : row.name(fallback);
        int element = row == null ? -1 : row.element;
        int relationClass = row == null ? 0 : row.relationClass;
        return new SourceBattleUnit(speciesId, level, baseStats[STAT_QUALITY], name,
                Math.max(1, baseStats[STAT_HP]), Math.max(1, currentStats[STAT_ATTACK]),
                Math.max(0, currentStats[STAT_DEFENSE]), Math.max(1, currentStats[STAT_SPEED]),
                element, visualSpriteId, relationClass, this);
    }

    int maxHp() {
        return baseStats[STAT_HP];
    }

    int hp() {
        return currentStats[STAT_HP];
    }

    void setHp(int hp) {
        currentStats[STAT_HP] = toShort(Math.max(0, Math.min(maxHp(), hp)));
    }

    void damage(int amount) {
        setHp(hp() - Math.max(1, amount));
    }

    void heal(int amount) {
        setHp(hp() + Math.max(0, amount));
    }

    boolean alive() {
        return hp() > 0;
    }

    boolean hasBuff(int id) {
        return id >= 0 && id < buffSlots.length && buffSlots[id][4] == 1;
    }

    boolean hasDebuff(int id) {
        return id >= 0 && id < debuffSlots.length && debuffSlots[id][4] == 1;
    }

    boolean hasSkillPp(int slot) {
        return slot >= 0 && slot < skillIds.length && skillIds[slot] != -1 && skillPp[slot] > 0;
    }

    int skillAt(int slot) {
        if (slot < 0 || slot >= skillIds.length) {
            return -1;
        }
        return skillIds[slot];
    }

    int skillPpAt(int slot) {
        if (slot < 0 || slot >= skillPp.length) {
            return 0;
        }
        return skillPp[slot];
    }

    BattleDamageResult computeDamage(BattleUnit target) {
        this.target = target;
        int skillId = selectedSkillId >= 0 ? selectedSkillId : firstUsableSkillId();
        selectedSkillId = (byte) skillId;
        BattleSkillRow skill = VqsvBattleTables.instance().skill(skillId);

        int critFlag = 0;
        int critChance = 5;
        int raw = baseAttack();
        BattleSpeciesRow selfSpecies = VqsvBattleTables.instance().species(speciesId);
        if (selfSpecies != null && selfSpecies.element >= 0 && visualSpriteId == sourceFinalVisualForElement(selfSpecies.element)) {
            critChance = 30;
        }
        critChance += currentStats[STAT_SPEED] / 2;
        if (hasFormStatus((byte) 4)) {
            BattleStatusRow status = VqsvBattleTables.instance().status(4);
            critChance += statusParam(status, 5, 0);
        }
        if (randomPercent() <= critChance) {
            raw = raw * 3 / 2;
            critFlag = 1;
        }

        int effectId = skill == null ? -1 : skill.effectId;
        int explicitChance = -1;
        int preSkillRaw = raw <= 0 ? 1 : raw;
        int damage = raw;

        if (skill == null) {
            effectId = -1;
        } else if (isPowerPercentSkill(skillId)) {
            damage = raw * skill.powerPercent / 100;
        } else if (skillId == 1 || skillId == 7) {
            int divisor = skill.chanceOrParam == 0 ? 1 : skill.chanceOrParam;
            damage = raw * skill.powerPercent / 100 + raw / divisor;
        } else if (skillId == 2 || skillId == 8 || skillId == 22 || skillId == 28 || skillId == 41 || skillId == 47) {
            damage = raw * skill.powerPercent / 100;
            explicitChance = skill.chanceOrParam;
        } else if (skillId == 3 || skillId == 9) {
            damage = target.hasDebuff(0) ? raw * skill.chanceOrParam / 100 : raw * skill.powerPercent / 100;
        } else if (skillId == 23 || skillId == 29) {
            damage = target.hasDebuff(1) ? raw * skill.chanceOrParam / 100 : raw * skill.powerPercent / 100;
        } else if (skillId == 43 || skillId == 49) {
            damage = raw * skill.powerPercent / 100;
            target.clearBuffs();
        } else if (skillId == 53 || skillId == 59) {
            damage = raw * (skill.chanceOrParam - hpPercent()) / 100;
        } else {
            effectId = -1;
        }

        int appliedDebuffId = maybeApplyTargetDebuff(target, skillId, effectId, explicitChance, preSkillRaw);

        if (hasBuff(0) && buffSlots[0][0] == 0) {
            damage += buffSlots[0][2];
        }
        if (hasBuff(1)) {
            damage += damage * buffSlots[1][2] / 100;
        }
        if (hasDebuff(6)) {
            damage -= damage * debuffSlots[6][1] / 100;
        }
        if (target.hasBuff(6) && randomPercent() <= buffSlots[6][1]) {
            damage = damage * buffSlots[6][2] / 100;
        }
        if (hasBuff(8)) {
            damage += damage * buffSlots[8][1] / 100;
        }
        if (ownerSide == 0 && sourcePassiveWorldDamageBoost3) {
            BattleStatusRow status = VqsvBattleTables.instance().status(3);
            damage += damage * statusParam(status, 5, 0) / 100;
        }
        if (ownerSide == 0 && sourcePassiveDamageBoost6) {
            BattleStatusRow status = VqsvBattleTables.instance().status(6);
            damage += damage * statusParam(status, 5, 0) / 100;
        }

        int relation = relationTo(target);
        if (relation == 0) {
            damage *= 3;
        } else if (relation == 1) {
            damage = damage * 60 / 100;
        }

        if (damage <= 0) {
            damage = 1;
        } else {
            int jitterRoll = randomPercent();
            int delta = (damage << 1) / 100;
            if (jitterRoll > 50) {
                if (delta <= 0) {
                    damage++;
                }
            } else if (delta <= 0) {
                damage--;
            }
            if (damage <= 0) {
                damage = 1;
            }
        }

        if (target.hasBuff(5) && randomPercent() <= target.buffSlots[5][1]) {
            effectScratch[5] = toShort(damage);
        }
        return new BattleDamageResult(damage, critFlag, appliedDebuffId);
    }

    void selectSkill(int skillId, BattleUnit target) {
        this.target = target;
        this.selectedSkillId = (byte) skillId;
        consumeSkillPp(skillId);
    }

    void consumeSkillPp(int skillId) {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] != skillId) {
                continue;
            }
            skillPp[i] = toShort(skillPp[i] - 1);
            if (hasBuff(12) && effectScratch[12] == 1) {
                skillPp[i] = toShort(skillPp[i] + 1);
            }
            if (hasBuff(8)) {
                skillPp[i] = toShort(skillPp[i] - 1);
            }
            if (skillPp[i] < 0) {
                skillPp[i] = 0;
            }
            return;
        }
    }

    void clearDebuffs() {
        for (short[] slot : debuffSlots) {
            slot[4] = 0;
        }
        resetEffectQueue(1);
        restoreMutableStats();
    }

    void clearBuffs() {
        for (short[] slot : buffSlots) {
            slot[4] = 0;
        }
        resetEffectQueue(0);
        restoreMutableStats();
    }

    void addActiveEffect(int bank, int effectId) {
        if (bank < 0 || bank >= activeEffectQueue.length || effectId < 0) {
            return;
        }
        for (int i = 0; i < activeEffectQueue[bank].length; i++) {
            if (activeEffectQueue[bank][i] == effectId) {
                return;
            }
        }
        int count = activeEffectCount[bank];
        if (count >= activeEffectQueue[bank].length) {
            return;
        }
        activeEffectQueue[bank][count] = (byte) effectId;
        activeEffectCount[bank] = (byte) (count + 1);
    }

    int nextLevelEnergy() {
        if (level >= 50) {
            return 37300;
        }
        return Math.max(1, (level + 1) * 15 * (level + 1) - 200);
    }

    private void copyBaseToCurrent() {
        System.arraycopy(baseStats, 0, currentStats, 0, baseStats.length);
    }

    private void restoreMutableStats() {
        currentStats[STAT_ATTACK] = baseStats[STAT_ATTACK];
        currentStats[STAT_DEFENSE] = baseStats[STAT_DEFENSE];
        currentStats[STAT_SPEED] = baseStats[STAT_SPEED];
    }

    private void loadSkills(int[] ids, int[] pp) {
        int count = Math.min(skillIds.length, ids.length);
        int out = 0;
        for (int i = 0; i < count; i++) {
            if (ids[i] < 0) {
                continue;
            }
            skillIds[out] = (byte) ids[i];
            BattleSkillRow row = VqsvBattleTables.instance().skill(ids[i]);
            int sourcePp = i < pp.length ? Math.max(0, pp[i]) : row == null ? 0 : row.ppMax;
            skillPp[out] = toShort(sourcePp);
            out++;
        }
        skillCount = (byte) out;
    }

    private void loadDefaultSkillsFromSpecies() {
        BattleSpeciesRow species = VqsvBattleTables.instance().species(speciesId);
        int firstSkill = species == null || species.element < 0 ? 0 : species.element * 10;
        addSkill(firstSkill);
        if (species == null || level <= 5 || species.learnGroup < 0) {
            return;
        }
        int tier = learnTierForLevel(level);
        for (int id = firstSkill; id < firstSkill + 10 && skillCount < Math.min(skillIds.length, level / 10 + 1); id++) {
            BattleSkillRow row = VqsvBattleTables.instance().skill(id);
            short[] thresholdRow = VqsvBattleTables.instance().row(8, species.learnGroup);
            if (row == null || thresholdRow == null || tier >= thresholdRow.length || row.learnTier > thresholdRow[tier]) {
                continue;
            }
            addSkill(id);
        }
    }

    private void addSkill(int skillId) {
        if (skillCount >= skillIds.length) {
            return;
        }
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] == skillId) {
                return;
            }
        }
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        skillIds[skillCount] = (byte) skillId;
        skillPp[skillCount] = toShort(row == null ? 1 : row.ppMax);
        skillCount++;
    }

    private int firstUsableSkillId() {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] != -1 && skillPp[i] > 0) {
                return skillIds[i];
            }
        }
        return skillIds[0] == -1 ? 0 : skillIds[0];
    }

    private int baseAttack() {
        if (target == null) {
            return currentStats[STAT_ATTACK];
        }
        if (target.ownerSide == 0 && target.sourcePassiveTargetDefenseBoost) {
            BattleStatusRow status = VqsvBattleTables.instance().status(4);
            target.currentStats[STAT_DEFENSE] = toShort(target.baseStats[STAT_DEFENSE]
                    * (100 + statusParam(status, 5, 0)) / 100);
        }
        int targetDefense = target.currentStats[STAT_DEFENSE];
        if (target.hasFormStatus((byte) 2)) {
            BattleStatusRow status = VqsvBattleTables.instance().status(2);
            targetDefense = targetDefense * (100 + statusParam(status, 5, 0)) / 100;
        }
        int value = currentStats[STAT_ATTACK] - targetDefense;
        if (hasFormStatus((byte) 0)) {
            BattleStatusRow status = VqsvBattleTables.instance().status(0);
            int threshold = statusParam(status, 5, 0) * baseStats[STAT_HP] / 100;
            if (currentStats[STAT_HP] <= threshold) {
                value = currentStats[STAT_ATTACK] * (100 + statusParam(status, 6, 0)) / 100 - target.currentStats[STAT_DEFENSE];
            }
        } else if (hasFormStatus((byte) 1)) {
            BattleStatusRow status = VqsvBattleTables.instance().status(1);
            value = currentStats[STAT_ATTACK] * (100 + statusParam(status, 5, 0)) / 100 - target.currentStats[STAT_DEFENSE];
        }
        return value;
    }

    private int maybeApplyTargetDebuff(BattleUnit target, int skillId, int effectId, int explicitChance, int preSkillRaw) {
        if (effectId < 0 || effectId >= target.debuffSlots.length) {
            return -1;
        }
        int chance = explicitChance;
        if (target.hasFormStatus((byte) 3)) {
            BattleStatusRow status = VqsvBattleTables.instance().status(3);
            if (randomPercent() > chance * (100 - statusParam(status, 5, 0)) / 100) {
                return -1;
            }
        } else if (target.hasBuff(14)) {
            return -1;
        } else if (chance != -1 && randomPercent() > chance) {
            return -1;
        }

        BattleSkillRow skill = VqsvBattleTables.instance().skill(skillId);
        int skillParam = skill == null ? 0 : skill.chanceOrParam;
        switch (effectId) {
            case 0:
            case 3:
                target.debuffSlots[effectId][1] = toShort(preSkillRaw);
                break;
            case 4:
            case 6:
                target.debuffSlots[effectId][1] = toShort(skillParam);
                break;
            case 5:
                target.debuffSlots[effectId][1] = toShort(target.baseStats[STAT_SPEED] * skillParam / 100);
                target.currentStats[STAT_SPEED] = toShort(target.baseStats[STAT_SPEED] - target.debuffSlots[effectId][1]);
                break;
            case 7:
                target.debuffSlots[effectId][1] = toShort(target.baseStats[STAT_DEFENSE] * skillParam / 100);
                target.currentStats[STAT_DEFENSE] = toShort(target.baseStats[STAT_DEFENSE] - target.debuffSlots[effectId][1]);
                break;
            default:
                break;
        }
        target.addActiveEffect(1, effectId);
        BattleDebuffRow debuff = VqsvBattleTables.instance().debuff(effectId);
        int duration = debuff == null ? 0 : debuff.duration;
        if (target.ownerSide == 0 && target.sourcePassiveDebuffDurationHalve) {
            duration /= 2;
        }
        target.debuffSlots[effectId][0] = toShort(duration);
        target.debuffSlots[effectId][3] = toShort(skillId);
        target.debuffSlots[effectId][4] = 1;
        return effectId;
    }

    private int relationTo(BattleUnit target) {
        BattleSpeciesRow self = VqsvBattleTables.instance().species(speciesId);
        BattleSpeciesRow other = VqsvBattleTables.instance().species(target.speciesId);
        int selfClass = self == null ? 0 : self.relationClass;
        int otherClass = other == null ? 0 : other.relationClass;
        int selfElement = self == null ? -1 : self.element;
        int otherElement = other == null ? -1 : other.element;
        boolean attackerEffective = true;
        boolean defenderEffective = true;
        if (selfClass == 2 && otherClass == 2) {
            attackerEffective = true;
            defenderEffective = true;
        } else if (selfClass == 2 && otherClass != 2) {
            attackerEffective = true;
            defenderEffective = false;
        } else if (selfClass != 2 && otherClass == 2) {
            attackerEffective = false;
            defenderEffective = true;
        }
        if (attackerEffective && beats(selfElement, otherElement)) {
            return 0;
        }
        if (defenderEffective && beats(otherElement, selfElement)) {
            return 1;
        }
        return -1;
    }

    private int hpPercent() {
        return currentStats[STAT_HP] * 100 / Math.max(1, baseStats[STAT_HP]);
    }

    private boolean hasFormStatus(byte status) {
        return baseStats[STAT_FORM] == status;
    }

    private static boolean isPowerPercentSkill(int skillId) {
        switch (skillId) {
            case 0: case 6: case 10: case 11: case 12: case 13: case 16: case 17:
            case 18: case 19: case 20: case 26: case 30: case 31: case 32: case 33:
            case 36: case 37: case 38: case 39: case 40: case 46: case 50: case 51:
            case 52: case 54: case 55: case 56: case 57: case 58: case 60: case 61:
            case 63: case 66: case 68: case 69:
                return true;
            default:
                return false;
        }
    }

    private static int sourceFinalVisualForElement(int element) {
        if (element < 0 || element >= SOURCE_FINAL_VISUAL_BY_ELEMENT.length) {
            return -1;
        }
        return SOURCE_FINAL_VISUAL_BY_ELEMENT[element];
    }

    static void setSourceFinalVisualForElement(int element, int visualSpriteId) {
        if (element >= 0 && element < SOURCE_FINAL_VISUAL_BY_ELEMENT.length) {
            SOURCE_FINAL_VISUAL_BY_ELEMENT[element] = visualSpriteId;
        }
    }

    static void resetSourceBattleHooksForChecks() {
        Arrays.fill(SOURCE_FINAL_VISUAL_BY_ELEMENT, -1);
        BATTLE_RANDOM.setSeed(0x56515356L);
    }

    static void setDamageRandomSeedForChecks(long seed) {
        BATTLE_RANDOM.setSeed(seed);
    }

    private static int statusParam(BattleStatusRow row, int index, int fallback) {
        return row == null ? fallback : VqsvBattleTables.get(row.raw, index, fallback);
    }

    private static int learnTierForLevel(int level) {
        int[] tiers = {5, 10, 20, 30, 40};
        int tier = 0;
        for (int i = 0; i < tiers.length; i++) {
            if (level >= tiers[i]) {
                tier = i;
            }
        }
        return tier;
    }

    private static int randomPercent() {
        return BATTLE_RANDOM.nextInt(100);
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

    private void applyNatureType(byte type) {
        switch (type) {
            case 7:
                baseStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] * 90 / 100);
                baseStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + 7);
                baseStats[STAT_HP] = toShort(baseStats[STAT_HP] * 80 / 100);
                break;
            case 8:
                baseStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] * 130 / 100);
                baseStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] - 2);
                baseStats[STAT_HP] = toShort(baseStats[STAT_HP] * 80 / 100);
                break;
            case 9:
                baseStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] * 90 / 100);
                baseStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] - 2);
                baseStats[STAT_HP] = toShort(baseStats[STAT_HP] * 130 / 100);
                break;
            default:
                break;
        }
    }

    private void resetEffectQueue(int bank) {
        Arrays.fill(activeEffectQueue[bank], (byte) -1);
        activeEffectCount[bank] = 0;
    }

    private static short toShort(int value) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }
}

final class BattleDamageResult {
    final int damage;
    final int critFlag;
    final int appliedDebuffId;

    BattleDamageResult(int damage, int critFlag, int appliedDebuffId) {
        this.damage = damage;
        this.critFlag = critFlag;
        this.appliedDebuffId = appliedDebuffId;
    }
}
