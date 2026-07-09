import java.util.Arrays;

final class BattleUnit {
    private static final VqsvSourceRandom FALLBACK_DAMAGE_RANDOM = VqsvSourceRandom.lazySourceSeeded();
    private static final int[] SOURCE_FINAL_VISUAL_BY_ELEMENT = {-1, -1, -1, -1, -1, -1, -1};
    private static VqsvSourceRandom activeDamageRandom = FALLBACK_DAMAGE_RANDOM;
    private static java.util.List<String> randomTrace;
    private static String randomTraceContext = "";
    private static int debugNextDebuffRoll = -1;

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
            throw new IllegalStateException("BattleUnit requires at least one source pet");
        }
        BattleUnit unit = fromSourcePet(pets.get(0), (byte) 0);
        unit.ownerSide = 0;
        return unit;
    }

    static BattleUnit fromSourcePet(SourcePetState pet, byte ownerSide) {
        if (pet == null) {
            throw new IllegalArgumentException("Source pet cannot be null");
        }
        short form = -1;
        short quality = (short) (pet.arg3 <= 0 ? 3 : pet.arg3);
        byte nature = (byte) pet.arg4;
        if (pet.sourcePayload != null) {
            if (pet.sourcePayload.length > 2) {
                form = (short) pet.sourcePayload[2];
            }
            if (pet.sourcePayload.length > 4 && pet.sourcePayload[4] > 0) {
                quality = (short) pet.sourcePayload[4];
            }
            if (pet.sourcePayload.length > 5) {
                nature = (byte) pet.sourcePayload[5];
            }
        }
        BattleUnit unit = fromSpecies(pet.speciesId, Math.max(1, pet.level), form,
                ownerSide, quality, nature, pet);
        unit.ownerSide = ownerSide == 1 ? 1 : 0;
        if (pet.sourcePayload != null) {
            if (pet.sourcePayload.length > 6 && pet.sourcePayload[6] >= 0) {
                unit.setHp(pet.sourcePayload[6]);
            }
            if (pet.sourcePayload.length > 7) {
                unit.exp = pet.sourcePayload[7];
            }
        }
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
        SourceBattleUnit render = new SourceBattleUnit(speciesId, level, baseStats[STAT_QUALITY], name,
                Math.max(1, baseStats[STAT_HP]), Math.max(1, currentStats[STAT_ATTACK]),
                Math.max(0, currentStats[STAT_DEFENSE]), Math.max(1, currentStats[STAT_SPEED]),
                element, visualSpriteId, relationClass, this);
        render.hp = hp();
        return render;
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

    void reviveTo(int hp) {
        setHp(Math.max(0, hp));
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

    int validateBattleItem(int itemId) {
        BattleItemRow item = VqsvBattleTables.instance().item(itemId);
        int behavior = item == null ? -1 : item.behavior;
        if (!alive() && behavior != 4) {
            return 8;
        }
        switch (behavior) {
            case 0:
                return 6;
            case 1:
                return hp() == maxHp() ? 2 : -1;
            case 2:
                return allSkillPpFull() ? 3 : -1;
            case 3: {
                int hpCode = -1;
                if (hp() == maxHp() || !alive()) {
                    hpCode = 2;
                }
                if (!allSkillPpFull()) {
                    return -1;
                }
                return hpCode == 2 ? 7 : -1;
            }
            case 4:
                return alive() ? 1 : -1;
            case 5:
                return hasAnyDebuff() ? -1 : 4;
            case 6:
                return currentStats[STAT_SIDE_FLAG] >= 2 ? 5 : -1;
            default:
                return -1;
        }
    }

    BattleItemUseResult applyBattleItem(int itemId) {
        BattleItemRow item = VqsvBattleTables.instance().item(itemId);
        int behavior = item == null ? -1 : item.behavior;
        int hpBefore = hp();
        int ppBefore = totalSkillPp();
        int debuffsBefore = activeDebuffTotal();
        switch (behavior) {
            case 1: {
                int heal = maxHp() * item.paramA / 100 + item.paramB;
                heal(heal);
                break;
            }
            case 2:
                restoreSkillPp(item.paramA);
                break;
            case 3: {
                int heal = maxHp() * item.paramA / 100 + item.paramB;
                heal(heal);
                restoreSkillPp(item.paramC);
                break;
            }
            case 4: {
                int heal = maxHp() * item.paramA / 100 + item.paramB;
                reviveTo(heal);
                restoreSkillPp(item.paramC);
                break;
            }
            case 5:
                clearDebuffs();
                break;
            case 6:
                currentStats[STAT_SIDE_FLAG] = 2;
                break;
            default:
                break;
        }
        return new BattleItemUseResult(itemId, behavior, hpBefore, hp(),
                ppBefore, totalSkillPp(), debuffsBefore, activeDebuffTotal(),
                currentStats[STAT_SIDE_FLAG]);
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
        if (randomPercent("damage.crit") <= critChance) {
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
            if (randomTrace != null) {
                randomTrace.add("PORTED battle formula POWER_PERCENT skill=" + skillId
                        + " raw=" + raw
                        + " powerPercent=" + skill.powerPercent
                        + " damageBeforeModifiers=" + damage
                        + " effectId=" + effectId
                        + " source=game.b.b(target) direct switch");
            }
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
            if (isBytecodeDefaultRawDamageSkill(skillId) && randomTrace != null) {
                randomTrace.add("PORTED battle formula BYTECODE_DEFAULT_RAW_DAMAGE skill=" + skillId
                        + " raw=" + raw
                        + " powerPercentIgnored=" + skill.powerPercent
                        + " effectIdIgnored=" + skill.effectId
                        + " source=javap game.b.b(target) tableswitch -> 706 default");
            }
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
        if (target.hasBuff(6) && randomPercent("damage.buff6") <= buffSlots[6][1]) {
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
            int jitterRoll = randomPercent("damage.jitter");
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

        if (target.hasBuff(5) && randomPercent("damage.buff5") <= target.buffSlots[5][1]) {
            effectScratch[5] = toShort(damage);
        }
        return new BattleDamageResult(damage, critFlag, appliedDebuffId);
    }

    private static boolean isBytecodeDefaultRawDamageSkill(int skillId) {
        switch (skillId) {
            case 21:
            case 27:
            case 42:
            case 48:
            case 62:
            case 67:
                return true;
            default:
                return false;
        }
    }

    int sourceBaseAttackForCurrentTarget() {
        return Math.max(1, baseAttack());
    }

    boolean hasSourceFormStatus(int statusId) {
        return hasFormStatus((byte) statusId);
    }

    boolean rollSourceChance(int chance) {
        return rollSourceChance("source.chance", chance);
    }

    boolean rollSourceChance(String label, int chance) {
        return randomPercent(label) <= chance;
    }

    int sourceStatusParam(int statusId, int index, int fallback) {
        return statusParam(VqsvBattleTables.instance().status(statusId), index, fallback);
    }

    int consumeStoredReflectDamage() {
        int damage = Math.max(0, effectScratch[5]);
        effectScratch[5] = 0;
        return damage;
    }

    int applySourceBuff(int buffId, int value, int sourceSkill) {
        if (buffId < 0 || buffId >= buffSlots.length) {
            return 0;
        }
        BattleBuffRow row = VqsvBattleTables.instance().buff(buffId);
        if (row == null) {
            return 0;
        }
        int heal = 0;
        switch (buffId) {
            case 0:
                buffSlots[buffId][1] = toShort(baseStats[STAT_DEFENSE] * row.paramA / 100);
                buffSlots[buffId][2] = toShort(row.paramB * sourceBaseAttackForCurrentTarget() / 100);
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] + buffSlots[buffId][1]);
                break;
            case 1:
                buffSlots[buffId][1] = toShort(baseStats[STAT_DEFENSE] * row.paramA / 100);
                buffSlots[buffId][2] = toShort(row.paramB);
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - buffSlots[buffId][1]);
                break;
            case 2:
                buffSlots[buffId][1] = toShort(baseStats[STAT_DEFENSE] * row.paramA / 100);
                buffSlots[buffId][2] = toShort(row.paramB);
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] + buffSlots[buffId][1]);
                break;
            case 3:
                buffSlots[buffId][1] = toShort(baseStats[STAT_HP] * row.paramA / 100);
                heal = buffSlots[buffId][1];
                heal(heal);
                break;
            case 4: {
                effectScratch[4] = toShort(sourceSkill);
                BattleSkillRow skill = VqsvBattleTables.instance().skill(sourceSkill);
                int param = skill == null ? 0 : skill.chanceOrParam;
                buffSlots[buffId][1] = toShort(baseStats[STAT_DEFENSE] * param / 100);
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] + buffSlots[buffId][1]);
                break;
            }
            case 5:
                buffSlots[buffId][1] = toShort(row.paramA);
                break;
            case 6:
                buffSlots[buffId][1] = toShort(row.paramA);
                buffSlots[buffId][2] = toShort(row.paramB);
                break;
            case 7: {
                effectScratch[7] = toShort(sourceSkill);
                BattleSkillRow skill = VqsvBattleTables.instance().skill(sourceSkill);
                int param = skill == null ? 0 : skill.chanceOrParam;
                buffSlots[buffId][1] = toShort(baseStats[STAT_SPEED] * param / 100);
                currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + buffSlots[buffId][1]);
                break;
            }
            case 8:
                buffSlots[buffId][1] = toShort(row.paramA);
                break;
            case 9:
                buffSlots[buffId][1] = toShort(baseStats[STAT_SPEED] * row.paramA / 100);
                buffSlots[buffId][2] = toShort(baseStats[STAT_DEFENSE] * row.paramB / 100);
                currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + buffSlots[buffId][1]);
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - buffSlots[buffId][2]);
                break;
            case 10:
                buffSlots[buffId][1] = toShort(baseStats[STAT_ATTACK] * row.paramA / 100);
                currentStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] + buffSlots[buffId][1]);
                break;
            case 11:
                buffSlots[buffId][1] = toShort(value);
                break;
            case 12:
                effectScratch[12] = 1;
                break;
            case 13:
                buffSlots[buffId][1] = toShort(baseStats[STAT_HP] * row.paramA / 100);
                heal = buffSlots[buffId][1];
                heal(heal);
                clearDebuffs();
                break;
            case 14:
                clearDebuffs();
                break;
            case 15:
                buffSlots[buffId][1] = toShort(value * row.paramA);
                break;
            default:
                buffSlots[buffId][1] = toShort(value);
                break;
        }
        addActiveEffect(0, buffId);
        buffSlots[buffId][0] = toShort(row.duration);
        buffSlots[buffId][3] = toShort(sourceSkill);
        buffSlots[buffId][4] = 1;
        return heal;
    }

    void copySourceBuffsFrom(BattleUnit source, int selectedIndex, int sourceSkill) {
        if (source == null) {
            applySourceBuff(11, selectedIndex, sourceSkill);
            return;
        }
        int count = Math.max(0, Math.min(source.activeEffectCount[0], source.activeEffectQueue[0].length));
        for (int i = 0; i < count; i++) {
            int buffId = source.activeEffectQueue[0][i];
            if (buffId < 0 || buffId >= source.buffSlots.length) {
                continue;
            }
            applySourceBuff(buffId, source.buffSlots[buffId][1], sourceSkill);
        }
        source.clearBuffs();
        applySourceBuff(11, selectedIndex, sourceSkill);
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

    private boolean allSkillPpFull() {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] == -1) {
                continue;
            }
            BattleSkillRow row = VqsvBattleTables.instance().skill(skillIds[i]);
            int max = row == null ? 0 : row.ppMax;
            if (skillPp[i] < max) {
                return false;
            }
        }
        return true;
    }

    private void restoreSkillPp(int amount) {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] == -1) {
                continue;
            }
            BattleSkillRow row = VqsvBattleTables.instance().skill(skillIds[i]);
            int max = row == null ? 0 : row.ppMax;
            skillPp[i] = toShort(Math.min(max, skillPp[i] + Math.max(0, amount)));
        }
    }

    private int totalSkillPp() {
        int total = 0;
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] != -1) {
                total += Math.max(0, skillPp[i]);
            }
        }
        return total;
    }

    private boolean hasAnyDebuff() {
        for (int i = 0; i < debuffSlots.length; i++) {
            if (hasDebuff(i)) {
                return true;
            }
        }
        return false;
    }

    private int activeDebuffTotal() {
        int total = 0;
        for (int i = 0; i < debuffSlots.length; i++) {
            if (hasDebuff(i)) {
                total++;
            }
        }
        return total;
    }

    void clearDebuffs() {
        for (short[] slot : debuffSlots) {
            slot[4] = 0;
        }
        resetEffectQueue(1);
        restoreMutableStats();
        reapplyActiveStatEffects();
    }

    void clearBuffs() {
        for (short[] slot : buffSlots) {
            slot[4] = 0;
        }
        resetEffectQueue(0);
        restoreMutableStats();
        reapplyActiveStatEffects();
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
        for (int i = 0; i < activeEffectQueue[bank].length; i++) {
            if (activeEffectQueue[bank][i] != -1) {
                continue;
            }
            activeEffectQueue[bank][i] = (byte) effectId;
            if (activeEffectCount[bank] < activeEffectQueue[bank].length) {
                activeEffectCount[bank] = (byte) (activeEffectCount[bank] + 1);
            }
            return;
        }
        activeEffectQueue[bank][0] = (byte) effectId;
    }

    int activeEffectIdAt(int bank, int slot) {
        if (bank < 0 || bank >= activeEffectQueue.length || slot < 0 || slot >= activeEffectQueue[bank].length) {
            return -1;
        }
        int id = activeEffectQueue[bank][slot];
        if (bank == 0) {
            return hasBuff(id) ? id : -1;
        }
        return hasDebuff(id) ? id : -1;
    }

    int activeBuffSlot(int buffId) {
        if (!hasBuff(buffId)) {
            return -1;
        }
        for (int i = 0; i < activeEffectQueue[0].length; i++) {
            if (activeEffectQueue[0][i] == buffId) {
                return i;
            }
        }
        return -1;
    }

    int activeDebuffSlot(int debuffId) {
        if (!hasDebuff(debuffId)) {
            return -1;
        }
        for (int i = 0; i < activeEffectQueue[1].length; i++) {
            if (activeEffectQueue[1][i] == debuffId) {
                return i;
            }
        }
        return -1;
    }

    int tickSourceBuff(int buffId, int queueSlot) {
        if (!hasBuff(buffId)) {
            return 0;
        }
        int heal = 0;
        switch (buffId) {
            case 1:
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - buffSlots[buffId][1]);
                break;
            case 2:
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] + buffSlots[buffId][1]);
                break;
            case 3:
                heal = Math.max(0, buffSlots[buffId][1]);
                heal(heal);
                break;
            case 4:
                currentStats[STAT_DEFENSE] = toShort(currentStats[STAT_DEFENSE] + buffSlots[buffId][1]);
                break;
            case 7:
                currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + buffSlots[buffId][1]);
                break;
            case 9:
                currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + buffSlots[buffId][1]);
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - buffSlots[buffId][2]);
                break;
            case 10:
                currentStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] + buffSlots[buffId][1]);
                break;
            case 12:
                effectScratch[12] = 2;
                break;
            case 13:
                heal = Math.max(0, buffSlots[buffId][1]);
                heal(heal);
                break;
            default:
                break;
        }
        tickSourceBuffDuration(buffId, queueSlot);
        return heal;
    }

    private void tickSourceBuffDuration(int buffId, int queueSlot) {
        if (!hasBuff(buffId)) {
            return;
        }
        if (buffSlots[buffId][0] > 0) {
            buffSlots[buffId][0] = toShort(buffSlots[buffId][0] - 1);
        }
        if (buffSlots[buffId][0] <= 0) {
            clearSourceBuff(buffId);
            removeActiveEffect(0, queueSlot);
        }
    }

    int tickSourceDebuff(int debuffId, int queueSlot) {
        if (!hasDebuff(debuffId)) {
            return 0;
        }
        int damage = 0;
        switch (debuffId) {
            case 0: {
                BattleSkillRow skill = VqsvBattleTables.instance().skill(debuffSlots[debuffId][3]);
                int divisor = skill == null || skill.chanceOrParam == 0 ? 1 : skill.chanceOrParam;
                damage = Math.max(1, debuffSlots[debuffId][1] / divisor);
                damage(damage);
                break;
            }
            case 3: {
                if (debuffSlots[debuffId][0] <= 1) {
                    BattleSkillRow skill = VqsvBattleTables.instance().skill(debuffSlots[debuffId][3]);
                    int percent = skill == null ? 0 : skill.chanceOrParam;
                    damage = Math.max(1, debuffSlots[debuffId][1] * percent / 100);
                    damage(damage);
                }
                break;
            }
            case 5:
                currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] - debuffSlots[debuffId][1]);
                break;
            case 7:
                currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - debuffSlots[debuffId][1]);
                break;
            default:
                break;
        }
        tickSourceDebuffDuration(debuffId, queueSlot);
        return damage;
    }

    private void tickSourceDebuffDuration(int debuffId, int queueSlot) {
        if (!hasDebuff(debuffId)) {
            return;
        }
        if (debuffSlots[debuffId][0] > 0) {
            debuffSlots[debuffId][0] = toShort(debuffSlots[debuffId][0] - 1);
        }
        if (debuffSlots[debuffId][0] <= 0) {
            clearSourceDebuff(debuffId);
            removeActiveEffect(1, queueSlot);
        }
    }

    private void clearSourceBuff(int buffId) {
        if (buffId < 0 || buffId >= buffSlots.length) {
            return;
        }
        buffSlots[buffId][4] = 0;
        restoreMutableStats();
        reapplyActiveStatEffects();
    }

    boolean clearSourceBuffForSwitch(int buffId) {
        if (!hasBuff(buffId)) {
            return false;
        }
        clearSourceBuff(buffId);
        return true;
    }

    private void clearSourceDebuff(int debuffId) {
        if (debuffId < 0 || debuffId >= debuffSlots.length) {
            return;
        }
        debuffSlots[debuffId][4] = 0;
        restoreMutableStats();
        reapplyActiveStatEffects();
    }

    private void removeActiveEffect(int bank, int slot) {
        if (bank < 0 || bank >= activeEffectQueue.length || slot < 0 || slot >= activeEffectQueue[bank].length) {
            return;
        }
        if (activeEffectQueue[bank][slot] != -1) {
            activeEffectQueue[bank][slot] = -1;
            if (activeEffectCount[bank] > 0) {
                activeEffectCount[bank] = (byte) (activeEffectCount[bank] - 1);
            }
        }
    }

    int nextLevelEnergy() {
        return sourceLevelThreshold(level >= 50 ? 50 : level + 1);
    }

    int[] sourceVisibleStats() {
        return new int[]{
                baseStats[STAT_HP],
                baseStats[STAT_ATTACK],
                baseStats[STAT_DEFENSE],
                baseStats[STAT_SPEED]
        };
    }

    boolean canSourceLevelUp() {
        return level < 50 && exp >= nextLevelEnergy();
    }

    void addSourceExp(int amount) {
        if (level >= 50) {
            return;
        }
        exp = Math.max(0, exp + Math.max(0, amount));
    }

    int[] sourceLearnCandidateSkillIds() {
        BattleSpeciesRow species = VqsvBattleTables.instance().species(speciesId);
        if (species == null || species.learnGroup < 0) {
            return new int[0];
        }
        java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
        int firstSkill = Math.max(0, species.element) * 10;
        short[] thresholdRow = VqsvBattleTables.instance().row(8, species.learnGroup);
        int tier = learnTierForLevel(level);
        int maxLearnTier = thresholdRow == null || tier >= thresholdRow.length ? Integer.MIN_VALUE : thresholdRow[tier];
        for (int id = firstSkill; id < firstSkill + 10; id++) {
            BattleSkillRow row = VqsvBattleTables.instance().skill(id);
            if (row == null || row.learnTier > maxLearnTier || hasSourceSkill(id)) {
                continue;
            }
            out.add(id);
        }
        int[] ids = new int[out.size()];
        for (int i = 0; i < out.size(); i++) {
            ids[i] = out.get(i);
        }
        return ids;
    }

    boolean sourceCanLearnAfterLevelUp() {
        return skillCount < skillIds.length && skillCount < level / 10 + 1
                && sourceLearnCandidateSkillIds().length > 0;
    }

    boolean learnSourceSkill(int skillId) {
        if (skillCount >= skillIds.length || hasSourceSkill(skillId)) {
            return false;
        }
        addSkill(skillId);
        return true;
    }

    void sourceLevelUpOnce() {
        if (!canSourceLevelUp()) {
            return;
        }
        int hpBefore = hp();
        level++;
        exp = Math.max(0, exp - sourceLevelThreshold(level));
        refreshBaseStatsForCurrentLevel();
        restoreSkillPpToSourceMax();
        setHp(hpBefore);
    }

    static int sourceLevelThreshold(int sourceLevel) {
        if (sourceLevel >= 50) {
            return 37300;
        }
        return Math.max(1, sourceLevel * 15 * sourceLevel - 200);
    }

    private void refreshBaseStatsForCurrentLevel() {
        BattleSpeciesRow row = VqsvBattleTables.instance().species(speciesId);
        short quality = baseStats[STAT_QUALITY] <= 0 ? 3 : baseStats[STAT_QUALITY];
        short form = baseStats[STAT_FORM];
        short sideFlag = baseStats[STAT_SIDE_FLAG];
        if (row == null || !row.validForBattle()) {
            baseStats[STAT_HP] = toShort(80 + level * 4);
            baseStats[STAT_ATTACK] = toShort(18 + level);
            baseStats[STAT_DEFENSE] = toShort(8 + level / 2);
            baseStats[STAT_SPEED] = 8;
        } else {
            baseStats[STAT_HP] = toShort(row.statHp(level, quality));
            baseStats[STAT_ATTACK] = toShort(row.statAttack(level, quality));
            baseStats[STAT_DEFENSE] = toShort(row.statDefense(level, quality));
            baseStats[STAT_SPEED] = toShort(row.statSpeed(level, quality));
        }
        baseStats[STAT_QUALITY] = quality;
        baseStats[STAT_FORM] = form;
        baseStats[STAT_SIDE_FLAG] = sideFlag;
        applyNatureType(natureType);
        restoreMutableStats();
        reapplyActiveStatEffects();
    }

    private void restoreSkillPpToSourceMax() {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] == -1) {
                continue;
            }
            BattleSkillRow row = VqsvBattleTables.instance().skill(skillIds[i]);
            skillPp[i] = toShort(row == null ? skillPp[i] : row.ppMax);
        }
    }

    private void copyBaseToCurrent() {
        System.arraycopy(baseStats, 0, currentStats, 0, baseStats.length);
    }

    private void restoreMutableStats() {
        currentStats[STAT_ATTACK] = baseStats[STAT_ATTACK];
        currentStats[STAT_DEFENSE] = baseStats[STAT_DEFENSE];
        currentStats[STAT_SPEED] = baseStats[STAT_SPEED];
    }

    void restoreStatsForCheck() {
        restoreMutableStats();
        reapplyActiveStatEffects();
    }

    private void reapplyActiveStatEffects() {
        if (hasBuff(1)) {
            currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - buffSlots[1][1]);
        }
        if (hasBuff(2)) {
            currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] + buffSlots[2][1]);
        }
        if (hasBuff(4)) {
            currentStats[STAT_DEFENSE] = toShort(currentStats[STAT_DEFENSE] + buffSlots[4][1]);
        }
        if (hasBuff(7)) {
            currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + buffSlots[7][1]);
        }
        if (hasBuff(9)) {
            currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] + buffSlots[9][1]);
            currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - buffSlots[9][2]);
        }
        if (hasBuff(10)) {
            currentStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] + buffSlots[10][1]);
        }
        if (hasDebuff(5)) {
            currentStats[STAT_SPEED] = toShort(baseStats[STAT_SPEED] - debuffSlots[5][1]);
        }
        if (hasDebuff(7)) {
            currentStats[STAT_DEFENSE] = toShort(baseStats[STAT_DEFENSE] - debuffSlots[7][1]);
        }
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
        if (hasSourceSkill(skillId)) {
            return;
        }
        BattleSkillRow row = VqsvBattleTables.instance().skill(skillId);
        skillIds[skillCount] = (byte) skillId;
        skillPp[skillCount] = toShort(row == null ? 1 : row.ppMax);
        skillCount++;
    }

    private boolean hasSourceSkill(int skillId) {
        for (int i = 0; i < skillIds.length; i++) {
            if (skillIds[i] == skillId) {
                return true;
            }
        }
        return false;
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
            if (randomPercent("damage.debuff") > chance * (100 - statusParam(status, 5, 0)) / 100) {
                return -1;
            }
        } else if (target.hasBuff(14)) {
            return -1;
        } else if (chance != -1 && randomPercent("damage.debuff") > chance) {
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
        FALLBACK_DAMAGE_RANDOM.setSeed(0x56515356L);
        activeDamageRandom = FALLBACK_DAMAGE_RANDOM;
        randomTrace = null;
        randomTraceContext = "";
    }

    static void setDamageRandomSeedForChecks(long seed) {
        FALLBACK_DAMAGE_RANDOM.setSeed(seed);
        activeDamageRandom = FALLBACK_DAMAGE_RANDOM;
    }

    static void setNextDebuffRollForChecks(int roll) {
        debugNextDebuffRoll = Math.max(0, Math.min(99, roll));
    }

    static void setRandomTrace(java.util.List<String> trace, String context) {
        setSourceRandomTrace(FALLBACK_DAMAGE_RANDOM, trace, context);
    }

    static void setSourceRandomTrace(VqsvSourceRandom sourceRandom, java.util.List<String> trace, String context) {
        activeDamageRandom = sourceRandom == null ? FALLBACK_DAMAGE_RANDOM : sourceRandom;
        randomTrace = trace;
        randomTraceContext = context == null ? "" : context;
    }

    static void clearRandomTrace() {
        activeDamageRandom = FALLBACK_DAMAGE_RANDOM;
        randomTrace = null;
        randomTraceContext = "";
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

    private static int randomPercent(String label) {
        String fullLabel = randomTraceContext.isEmpty() ? label : randomTraceContext + "." + label;
        if (label.endsWith("damage.debuff") && debugNextDebuffRoll >= 0) {
            int roll = debugNextDebuffRoll;
            debugNextDebuffRoll = -1;
            if (randomTrace != null) {
                randomTrace.add("SMOKE battle forced damage.debuff roll=" + roll
                        + " label=" + fullLabel
                        + " source=game.b.b(target) ae.a(100)");
            }
            return roll;
        }
        return activeDamageRandom.a(fullLabel, 100, randomTrace);
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

final class BattleItemUseResult {
    final int itemId;
    final int behavior;
    final int hpBefore;
    final int hpAfter;
    final int ppBefore;
    final int ppAfter;
    final int debuffsBefore;
    final int debuffsAfter;
    final int sourceStateFlag;

    BattleItemUseResult(int itemId, int behavior, int hpBefore, int hpAfter,
                        int ppBefore, int ppAfter, int debuffsBefore, int debuffsAfter,
                        int sourceStateFlag) {
        this.itemId = itemId;
        this.behavior = behavior;
        this.hpBefore = hpBefore;
        this.hpAfter = hpAfter;
        this.ppBefore = ppBefore;
        this.ppAfter = ppAfter;
        this.debuffsBefore = debuffsBefore;
        this.debuffsAfter = debuffsAfter;
        this.sourceStateFlag = sourceStateFlag;
    }
}
