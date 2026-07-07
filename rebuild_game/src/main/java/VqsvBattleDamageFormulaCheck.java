final class VqsvBattleDamageFormulaCheck {
    public static void main(String[] args) {
        run();
    }

    static void run() {
        VqsvBattleTables.instance();
        checkPowerSkill();
        checkSkillOneAndDebuff();
        checkConditionalSkills();
        checkClearBuffSkill();
        checkHpPercentSkill();
        checkDebuffBlockByBuff14();
        checkOddBuff6SourceBehavior();
        checkRelationMultiplier();
        checkStatusAttackDefenseModifiers();
        checkMinimumClamp();
        checkActiveEffectClearReapplyStats();
        checkBattleItemValidateApply();
        checkSourcePetVisualUsesSpeciesRow();
        System.out.println("battle-damage-formula-check-ok");
    }

    private static void checkPowerSkill() {
        BattleUnit attacker = unit(-1, 140, 20);
        BattleUnit target = unit(-1, 15, 10);
        BattleDamageResult result = damage(attacker, target, 0, 1001L);
        int raw = critAdjustedRaw(130, result);
        assertEquals("skill0 power", raw * skill(0).powerPercent / 100, result.damage);
        assertEquals("skill0 debuff", -1, result.appliedDebuffId);
    }

    private static void checkSkillOneAndDebuff() {
        BattleUnit attacker = unit(-1, 140, 20);
        BattleUnit target = unit(-1, 15, 10);
        BattleDamageResult result = damage(attacker, target, 1, 1002L);
        int raw = critAdjustedRaw(130, result);
        assertEquals("skill1 formula", raw * skill(1).powerPercent / 100 + raw / skill(1).chanceOrParam,
                result.damage);
        assertEquals("skill1 debuff id", skill(1).effectId, result.appliedDebuffId);
        assertEquals("skill1 debuff active", 1, target.debuffSlots[skill(1).effectId][4]);
        assertEquals("skill1 debuff value", raw <= 0 ? 1 : raw, target.debuffSlots[skill(1).effectId][1]);
    }

    private static void checkConditionalSkills() {
        BattleUnit attacker = unit(-1, 140, 20);
        BattleUnit target = unit(-1, 15, 10);
        BattleDamageResult normal = damage(attacker, target, 3, 1003L);
        int normalRaw = critAdjustedRaw(130, normal);
        assertEquals("skill3 normal", normalRaw * skill(3).powerPercent / 100, normal.damage);

        attacker = unit(-1, 140, 20);
        target = unit(-1, 15, 10);
        target.debuffSlots[0][4] = 1;
        BattleDamageResult boosted = damage(attacker, target, 3, 1004L);
        int boostedRaw = critAdjustedRaw(130, boosted);
        assertEquals("skill3 target debuff0", boostedRaw * skill(3).chanceOrParam / 100, boosted.damage);

        attacker = unit(-1, 140, 20);
        target = unit(-1, 15, 10);
        target.debuffSlots[1][4] = 1;
        BattleDamageResult skill23 = damage(attacker, target, 23, 1005L);
        int skill23Raw = critAdjustedRaw(130, skill23);
        assertEquals("skill23 target debuff1", skill23Raw * skill(23).chanceOrParam / 100, skill23.damage);
    }

    private static void checkClearBuffSkill() {
        BattleUnit attacker = unit(-1, 140, 20);
        BattleUnit target = unit(-1, 15, 10);
        target.buffSlots[1][4] = 1;
        BattleDamageResult result = damage(attacker, target, 43, 1006L);
        int raw = critAdjustedRaw(130, result);
        assertEquals("skill43 damage", raw * skill(43).powerPercent / 100, result.damage);
        assertEquals("skill43 clears target buff", 0, target.buffSlots[1][4]);
    }

    private static void checkHpPercentSkill() {
        BattleUnit attacker = unit(-1, 140, 20);
        attacker.setHp(50);
        BattleUnit target = unit(-1, 15, 10);
        BattleDamageResult result = damage(attacker, target, 53, 1007L);
        int raw = critAdjustedRaw(130, result);
        assertEquals("skill53 hp percent", raw * (skill(53).chanceOrParam - 25) / 100, result.damage);
    }

    private static void checkDebuffBlockByBuff14() {
        BattleUnit attacker = unit(-1, 140, 20);
        BattleUnit target = unit(-1, 15, 10);
        target.buffSlots[14][4] = 1;
        BattleDamageResult result = damage(attacker, target, 1, 1008L);
        assertEquals("buff14 blocks debuff id", -1, result.appliedDebuffId);
        assertEquals("buff14 blocks debuff active", 0, target.debuffSlots[skill(1).effectId][4]);
    }

    private static void checkOddBuff6SourceBehavior() {
        BattleUnit attacker = unit(-1, 140, 20);
        BattleUnit target = unit(-1, 15, 10);
        target.buffSlots[6][4] = 1;
        attacker.buffSlots[6][1] = 100;
        attacker.buffSlots[6][2] = 50;
        BattleDamageResult result = damage(attacker, target, 0, 1009L);
        int raw = critAdjustedRaw(130, result);
        int beforeBuff6 = raw * skill(0).powerPercent / 100;
        assertEquals("buff6 reads attacker slots", beforeBuff6 * 50 / 100, result.damage);
    }

    private static void checkRelationMultiplier() {
        BattleUnit attacker = unit(0, 140, 20);
        BattleUnit target = unit(16, 15, 10);
        BattleDamageResult strong = damage(attacker, target, 0, 1010L);
        int strongRaw = critAdjustedRaw(130, strong);
        assertEquals("strong relation", strongRaw * skill(0).powerPercent / 100 * 3, strong.damage);

        attacker = unit(16, 140, 20);
        target = unit(0, 15, 10);
        BattleDamageResult weak = damage(attacker, target, 0, 1011L);
        int weakRaw = critAdjustedRaw(130, weak);
        assertEquals("weak relation", weakRaw * skill(0).powerPercent / 100 * 60 / 100, weak.damage);
    }

    private static void checkStatusAttackDefenseModifiers() {
        BattleUnit attacker = unit(-1, 140, 20);
        attacker.baseStats[BattleUnit.STAT_FORM] = 1;
        BattleUnit target = unit(-1, 15, 10);
        BattleDamageResult attackBoost = damage(attacker, target, 0, 1012L);
        int boostedBase = attacker.currentStats[BattleUnit.STAT_ATTACK]
                * (100 + statusParam(1, 5)) / 100 - target.currentStats[BattleUnit.STAT_DEFENSE];
        assertEquals("status1 attack", critAdjustedRaw(boostedBase, attackBoost) * skill(0).powerPercent / 100,
                attackBoost.damage);

        attacker = unit(-1, 140, 20);
        target = unit(-1, 15, 10);
        target.baseStats[BattleUnit.STAT_FORM] = 2;
        BattleDamageResult defenseBoost = damage(attacker, target, 0, 1013L);
        int raisedDefense = target.currentStats[BattleUnit.STAT_DEFENSE]
                * (100 + statusParam(2, 5)) / 100;
        assertEquals("status2 target defense", critAdjustedRaw(140 - raisedDefense, defenseBoost)
                        * skill(0).powerPercent / 100,
                defenseBoost.damage);
    }

    private static void checkMinimumClamp() {
        BattleUnit attacker = unit(-1, 20, 5);
        BattleUnit target = unit(-1, 30, 200);
        BattleDamageResult result = damage(attacker, target, 0, 1014L);
        assertEquals("minimum damage", 1, result.damage);
    }

    private static void checkActiveEffectClearReapplyStats() {
        BattleUnit unit = unit(-1, 100, 40);
        unit.baseStats[BattleUnit.STAT_SPEED] = 30;
        unit.currentStats[BattleUnit.STAT_SPEED] = 30;

        unit.buffSlots[10][1] = 15;
        unit.buffSlots[10][4] = 1;
        unit.debuffSlots[5][1] = 8;
        unit.debuffSlots[5][4] = 1;
        unit.restoreStatsForCheck();
        unit.clearDebuffs();
        assertEquals("clearDebuffs keeps active buff10 attack",
                115, unit.currentStats[BattleUnit.STAT_ATTACK]);
        assertEquals("clearDebuffs removes debuff5 speed",
                30, unit.currentStats[BattleUnit.STAT_SPEED]);

        unit.debuffSlots[7][1] = 6;
        unit.debuffSlots[7][4] = 1;
        unit.clearBuffs();
        assertEquals("clearBuffs removes buff10 attack",
                100, unit.currentStats[BattleUnit.STAT_ATTACK]);
        assertEquals("clearBuffs keeps active debuff7 defense",
                34, unit.currentStats[BattleUnit.STAT_DEFENSE]);
    }

    private static void checkBattleItemValidateApply() {
        BattleUnit unit = unit(-1, 100, 40);
        unit.setHp(50);
        assertEquals("item4 hp validate damaged", -1, unit.validateBattleItem(4));
        BattleItemUseResult heal = unit.applyBattleItem(4);
        assertEquals("item4 heal source formula includes paramB", 200, heal.hpAfter);
        assertEquals("item4 hp full validation", 2, unit.validateBattleItem(4));

        unit.skillIds[0] = 10;
        unit.skillPp[0] = 0;
        assertEquals("item6 pp validate empty", -1, unit.validateBattleItem(6));
        BattleItemUseResult pp = unit.applyBattleItem(6);
        assertEquals("item6 pp restore", 25, pp.ppAfter);

        unit.setHp(40);
        unit.skillPp[0] = 0;
        BattleItemUseResult both = unit.applyBattleItem(8);
        assertEquals("item8 hp restore", 190, both.hpAfter);
        assertEquals("item8 pp restore", 20, both.ppAfter);

        unit.setHp(0);
        unit.skillPp[0] = 0;
        assertEquals("item11 revive valid only dead", -1, unit.validateBattleItem(11));
        BattleItemUseResult revive = unit.applyBattleItem(11);
        assertEquals("item11 revive hp set", 150, revive.hpAfter);
        assertEquals("item11 revive pp restore", 20, revive.ppAfter);
        assertEquals("item11 alive invalid", 1, unit.validateBattleItem(11));

        unit.debuffSlots[5][1] = 8;
        unit.debuffSlots[5][4] = 1;
        assertEquals("item10 clear debuff valid", -1, unit.validateBattleItem(10));
        BattleItemUseResult clear = unit.applyBattleItem(10);
        assertEquals("item10 clear debuffs", 0, clear.debuffsAfter);
        assertEquals("item10 no debuff invalid", 4, unit.validateBattleItem(10));
    }

    private static void checkSourcePetVisualUsesSpeciesRow() {
        SourcePetState pet = new SourcePetState(0, 17, 7, 3, 2, 10, 45);
        if (pet.sourcePayload == null || pet.sourcePayload.length <= 8 || pet.sourcePayload[8] != 0) {
            throw new AssertionError("Expected rebuilt default payload[8] test fixture to be 0");
        }
        BattleUnit unit = BattleUnit.fromSourcePet(pet, (byte) 0);
        assertEquals("source pet visual species17 row sprite", 103, unit.visualSpriteId);
    }

    private static BattleDamageResult damage(BattleUnit attacker, BattleUnit target, int skillId, long seed) {
        BattleUnit.resetSourceBattleHooksForChecks();
        BattleUnit.setDamageRandomSeedForChecks(seed);
        attacker.selectedSkillId = (byte) skillId;
        return attacker.computeDamage(target);
    }

    private static BattleUnit unit(int speciesId, int attack, int defense) {
        BattleUnit unit = new BattleUnit();
        unit.speciesId = speciesId;
        unit.level = 10;
        unit.visualSpriteId = -1;
        unit.ownerSide = 1;
        unit.baseStats[BattleUnit.STAT_QUALITY] = 3;
        unit.baseStats[BattleUnit.STAT_HP] = 200;
        unit.baseStats[BattleUnit.STAT_ATTACK] = (short) attack;
        unit.baseStats[BattleUnit.STAT_DEFENSE] = (short) defense;
        unit.baseStats[BattleUnit.STAT_SPEED] = 0;
        unit.baseStats[BattleUnit.STAT_FORM] = -1;
        System.arraycopy(unit.baseStats, 0, unit.currentStats, 0, unit.baseStats.length);
        return unit;
    }

    private static int critAdjustedRaw(int raw, BattleDamageResult result) {
        return result.critFlag == 1 ? raw * 3 / 2 : raw;
    }

    private static BattleSkillRow skill(int id) {
        BattleSkillRow row = VqsvBattleTables.instance().skill(id);
        if (row == null) {
            throw new AssertionError("Missing skill row " + id);
        }
        return row;
    }

    private static int statusParam(int id, int index) {
        BattleStatusRow row = VqsvBattleTables.instance().status(id);
        if (row == null) {
            throw new AssertionError("Missing status row " + id);
        }
        return VqsvBattleTables.get(row.raw, index, 0);
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(label + " expected=" + expected + " actual=" + actual);
        }
    }
}
