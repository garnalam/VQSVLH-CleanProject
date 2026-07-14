import java.util.Arrays;

final class VqsvBattleEffectLogic {
    private VqsvBattleEffectLogic() {
    }

    static int applySourceBuff(BattleUnit unit, int buffId, int value, int sourceSkill) {
        if (buffId < 0 || buffId >= unit.buffSlots.length) {
            return 0;
        }
        BattleBuffRow row = VqsvBattleTables.instance().buff(buffId);
        if (row == null) {
            return 0;
        }
        int heal = 0;
        int duration = row.duration;
        switch (buffId) {
            case 0:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] * row.paramA / 100);
                unit.buffSlots[buffId][2] = BattleUnit.toShort(
                        row.paramB * unit.sourceBaseAttackForCurrentTarget() / 100);
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[buffId][1]);
                break;
            case 1:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] * row.paramA / 100);
                unit.buffSlots[buffId][2] = BattleUnit.toShort(row.paramB);
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.buffSlots[buffId][1]);
                break;
            case 2:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] * row.paramA / 100);
                unit.buffSlots[buffId][2] = BattleUnit.toShort(row.paramB);
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[buffId][1]);
                break;
            case 3:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_HP] * row.paramA / 100);
                heal = unit.buffSlots[buffId][1];
                unit.heal(heal);
                break;
            case 4: {
                unit.effectScratch[4] = BattleUnit.toShort(sourceSkill);
                BattleSkillRow skill = VqsvBattleTables.instance().skill(sourceSkill);
                int param = skill == null ? 0 : skill.chanceOrParam;
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] * param / 100);
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[buffId][1]);
                break;
            }
            case 5:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(row.paramA);
                break;
            case 6:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(row.paramA);
                unit.buffSlots[buffId][2] = BattleUnit.toShort(row.paramB);
                break;
            case 7: {
                unit.effectScratch[7] = BattleUnit.toShort(sourceSkill);
                BattleSkillRow skill = VqsvBattleTables.instance().skill(sourceSkill);
                int param = skill == null ? 0 : skill.chanceOrParam;
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] * param / 100);
                unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] + unit.buffSlots[buffId][1]);
                break;
            }
            case 8:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(row.paramA);
                break;
            case 9:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] * row.paramA / 100);
                unit.buffSlots[buffId][2] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] * row.paramB / 100);
                unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] + unit.buffSlots[buffId][1]);
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.buffSlots[buffId][2]);
                break;
            case 10:
                duration = 3;
                applyManLucGameplayFix(unit, duration);
                break;
            case 11:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(value);
                break;
            case 12:
                unit.effectScratch[12] = 1;
                break;
            case 13:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_HP] * row.paramA / 100);
                heal = unit.buffSlots[buffId][1];
                unit.heal(heal);
                clearDebuffs(unit);
                break;
            case 14:
                clearDebuffs(unit);
                break;
            case 15:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(value * row.paramA);
                break;
            default:
                unit.buffSlots[buffId][1] = BattleUnit.toShort(value);
                break;
        }
        unit.addActiveBuffEffect(buffId);
        unit.buffSlots[buffId][0] = BattleUnit.toShort(duration);
        unit.buffSlots[buffId][3] = BattleUnit.toShort(sourceSkill);
        unit.buffSlots[buffId][4] = 1;
        return heal;
    }

    static int tickSourceBuff(BattleUnit unit, int buffId, int queueSlot) {
        if (!unit.hasBuff(buffId)) {
            return 0;
        }
        int heal = 0;
        switch (buffId) {
            case 1:
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.buffSlots[buffId][1]);
                break;
            case 2:
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[buffId][1]);
                break;
            case 3:
                heal = Math.max(0, unit.buffSlots[buffId][1]);
                unit.heal(heal);
                break;
            case 4:
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.currentStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[buffId][1]);
                break;
            case 7:
                unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] + unit.buffSlots[buffId][1]);
                break;
            case 9:
                unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] + unit.buffSlots[buffId][1]);
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.buffSlots[buffId][2]);
                break;
            case 10:
                applyManLucGameplayFix(unit, Math.max(0, unit.buffSlots[buffId][0] - 1));
                break;
            case 12:
                unit.effectScratch[12] = 2;
                break;
            case 13:
                heal = Math.max(0, unit.buffSlots[buffId][1]);
                unit.heal(heal);
                break;
            default:
                break;
        }
        tickSourceBuffDuration(unit, buffId, queueSlot);
        return heal;
    }

    static int tickSourceDebuff(BattleUnit unit, int debuffId, int queueSlot) {
        if (!unit.hasDebuff(debuffId)) {
            return 0;
        }
        int damage = 0;
        switch (debuffId) {
            case 0: {
                BattleSkillRow skill = VqsvBattleTables.instance().skill(unit.debuffSlots[debuffId][3]);
                int divisor = skill == null || skill.chanceOrParam == 0 ? 1 : skill.chanceOrParam;
                damage = Math.max(1, unit.debuffSlots[debuffId][1] / divisor);
                unit.damage(damage);
                break;
            }
            case 3: {
                if (unit.debuffSlots[debuffId][0] <= 1) {
                    BattleSkillRow skill = VqsvBattleTables.instance().skill(unit.debuffSlots[debuffId][3]);
                    int percent = skill == null ? 0 : skill.chanceOrParam;
                    damage = Math.max(1, unit.debuffSlots[debuffId][1] * percent / 100);
                    unit.damage(damage);
                }
                break;
            }
            case 5:
                unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_SPEED] - unit.debuffSlots[debuffId][1]);
                break;
            case 7:
                unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.debuffSlots[debuffId][1]);
                break;
            default:
                break;
        }
        tickSourceDebuffDuration(unit, debuffId, queueSlot);
        return damage;
    }

    static int applyDamageFormulaHooks(BattleUnit attacker, BattleUnit target,
                                       int damage, boolean pendingTargetClearBuffs) {
        if (attacker.hasBuff(0) && attacker.buffSlots[0][0] == 0) {
            damage += attacker.buffSlots[0][2];
        }
        if (attacker.hasBuff(1)) {
            damage += damage * attacker.buffSlots[1][2] / 100;
        }
        if (attacker.hasDebuff(6)) {
            int beforeDebuff6 = damage;
            damage -= damage * attacker.debuffSlots[6][1] / 100;
            BattleUnit.addSourceRandomTrace("PORTED battle debuff6 Nhut Chi outgoing damage down"
                    + " percent=" + attacker.debuffSlots[6][1]
                    + " damage=" + beforeDebuff6 + "->" + damage
                    + " source=game.b.b(target) attacker.p(6) formula");
        }
        if (attacker.hasDebuff(8)) {
            int beforeDebuff8 = damage;
            damage += damage * 10 / 100;
            BattleUnit.addSourceRandomTrace("INTENTIONAL battle debuff8 Quy Mi outgoing damage up"
                    + " percent=10"
                    + " damage=" + beforeDebuff8 + "->" + damage
                    + " sourceDeviation=GAMEPLAY_FIXED user-approved pet with Quy Mi gains damage");
        }
        if (!pendingTargetClearBuffs && target.hasBuff(6)) {
            int chance = Math.max(0, target.buffSlots[6][1]);
            int roll = attacker.sourceRandomPercent("damage.buff6");
            if (roll <= chance) {
                int beforeBuff6 = damage;
                damage = damage * 50 / 100;
                BattleUnit.addSourceRandomTrace("INTENTIONAL battle buff6 Kien nhan incoming damage reduction"
                        + " roll=" + roll
                        + " chance=" + chance
                        + " reductionPercent=50"
                        + " damage=" + beforeBuff6 + "->" + damage
                        + " sourceDeviation=target buff params used instead of attacker.v[6]");
            }
        }
        if (attacker.hasBuff(8)) {
            damage += damage * attacker.buffSlots[8][1] / 100;
        }
        return damage;
    }

    static BattlePendingDebuff planTargetDebuff(BattleUnit attacker, BattleUnit target,
                                                int skillId, int effectId,
                                                int explicitChance, int preSkillRaw) {
        if (effectId < 0 || effectId >= target.debuffSlots.length) {
            return null;
        }
        int chance = explicitChance;
        if (target.hasSourceFormStatus(3)) {
            int resistPercent = target.sourceHeldItemParam(3, 5, 0);
            if (attacker.sourceRandomPercent("damage.debuff") > chance * (100 - resistPercent) / 100) {
                return null;
            }
        } else if (target.hasBuff(14)) {
            return null;
        } else if (chance != -1 && attacker.sourceRandomPercent("damage.debuff") > chance) {
            return null;
        }
        return new BattlePendingDebuff(target, effectId, skillId, preSkillRaw);
    }

    static int targetDefenseForSourceFormula(BattleUnit target) {
        int targetDefense = target.currentStats[BattleUnit.STAT_DEFENSE];
        if (target.hasDebuff(2)) {
            targetDefense = targetDefense * (100 + target.sourceStatusParam(2, 5, 0)) / 100;
        }
        return targetDefense;
    }

    static int pendingReflectDamage(BattleUnit attacker, BattleUnit target,
                                    int finalDamage, boolean pendingTargetClearBuffs) {
        if (pendingTargetClearBuffs || !target.hasBuff(5)) {
            return 0;
        }
        return attacker.sourceRandomPercent("damage.buff5") <= target.buffSlots[5][1]
                ? finalDamage : 0;
    }

    static int applySkillPpHooks(BattleUnit unit, int currentPp) {
        int out = currentPp;
        if (unit.hasBuff(12) && unit.effectScratch[12] == 1) {
            out++;
        }
        if (unit.hasBuff(8)) {
            out--;
        }
        return out;
    }

    static void clearDebuffs(BattleUnit unit) {
        for (short[] slot : unit.debuffSlots) {
            slot[4] = 0;
        }
        resetEffectQueue(unit, BattleEffectRow.BANK_DEBUFF);
        restoreMutableStats(unit);
        reapplyActiveStatEffects(unit);
    }

    static void clearBuffs(BattleUnit unit) {
        for (short[] slot : unit.buffSlots) {
            slot[4] = 0;
        }
        resetEffectQueue(unit, BattleEffectRow.BANK_BUFF);
        restoreMutableStats(unit);
        reapplyActiveStatEffects(unit);
    }

    static boolean clearSourceBuffForSwitch(BattleUnit unit, int buffId) {
        if (!unit.hasBuff(buffId)) {
            return false;
        }
        clearSourceBuff(unit, buffId);
        return true;
    }

    static void restoreStatsForCheck(BattleUnit unit) {
        restoreMutableStats(unit);
        reapplyActiveStatEffects(unit);
    }

    static void restoreSourceStatusState(BattleUnit unit) {
        resetEffectQueue(unit, BattleEffectRow.BANK_BUFF);
        resetEffectQueue(unit, BattleEffectRow.BANK_DEBUFF);
        for (int i = 0; i < unit.buffSlots.length; i++) {
            if (unit.hasBuff(i)) {
                unit.addActiveBuffEffect(i);
            }
        }
        for (int i = 0; i < unit.debuffSlots.length; i++) {
            if (unit.hasDebuff(i)) {
                unit.addActiveDebuffEffect(i);
            }
        }
        restoreMutableStats(unit);
        reapplyActiveStatEffects(unit);
    }

    static void commitPendingDebuff(BattlePendingDebuff pending) {
        BattleUnit target = pending.target;
        int effectId = pending.effectId;
        BattleSkillRow skill = VqsvBattleTables.instance().skill(pending.skillId);
        int skillParam = skill == null ? 0 : skill.chanceOrParam;
        switch (effectId) {
            case 0:
            case 3:
                target.debuffSlots[effectId][1] = BattleUnit.toShort(pending.preSkillRaw);
                break;
            case 4:
            case 6:
                target.debuffSlots[effectId][1] = BattleUnit.toShort(skillParam);
                break;
            case 5:
                target.debuffSlots[effectId][1] = BattleUnit.toShort(
                        target.baseStats[BattleUnit.STAT_SPEED] * skillParam / 100);
                target.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                        target.baseStats[BattleUnit.STAT_SPEED] - target.debuffSlots[effectId][1]);
                break;
            case 7:
                target.debuffSlots[effectId][1] = BattleUnit.toShort(
                        target.baseStats[BattleUnit.STAT_DEFENSE] * skillParam / 100);
                target.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                        target.baseStats[BattleUnit.STAT_DEFENSE] - target.debuffSlots[effectId][1]);
                break;
            default:
                break;
        }
        target.addActiveDebuffEffect(effectId);
        BattleEffectRow debuff = VqsvBattleTables.instance().effect(BattleEffectRow.BANK_DEBUFF, effectId);
        int duration = debuff == null ? 0 : debuff.duration();
        if (target.ownerSide == 0 && target.sourcePassiveDebuffDurationHalve) {
            duration /= 2;
        }
        target.debuffSlots[effectId][0] = BattleUnit.toShort(duration);
        target.debuffSlots[effectId][3] = BattleUnit.toShort(pending.skillId);
        target.debuffSlots[effectId][4] = 1;
    }

    private static void tickSourceBuffDuration(BattleUnit unit, int buffId, int queueSlot) {
        if (!unit.hasBuff(buffId)) {
            return;
        }
        if (unit.buffSlots[buffId][0] > 0) {
            unit.buffSlots[buffId][0] = BattleUnit.toShort(unit.buffSlots[buffId][0] - 1);
        }
        if (unit.buffSlots[buffId][0] <= 0) {
            clearSourceBuff(unit, buffId);
            removeActiveEffect(unit, BattleEffectRow.BANK_BUFF, queueSlot);
        }
    }

    private static void tickSourceDebuffDuration(BattleUnit unit, int debuffId, int queueSlot) {
        if (!unit.hasDebuff(debuffId)) {
            return;
        }
        if (unit.debuffSlots[debuffId][0] > 0) {
            unit.debuffSlots[debuffId][0] = BattleUnit.toShort(unit.debuffSlots[debuffId][0] - 1);
        }
        if (unit.debuffSlots[debuffId][0] <= 0) {
            clearSourceDebuff(unit, debuffId);
            removeActiveEffect(unit, BattleEffectRow.BANK_DEBUFF, queueSlot);
        }
    }

    private static void clearSourceBuff(BattleUnit unit, int buffId) {
        if (buffId < 0 || buffId >= unit.buffSlots.length) {
            return;
        }
        unit.buffSlots[buffId][4] = 0;
        restoreMutableStats(unit);
        reapplyActiveStatEffects(unit);
    }

    private static void clearSourceDebuff(BattleUnit unit, int debuffId) {
        if (debuffId < 0 || debuffId >= unit.debuffSlots.length) {
            return;
        }
        unit.debuffSlots[debuffId][4] = 0;
        restoreMutableStats(unit);
        reapplyActiveStatEffects(unit);
    }

    private static void applyManLucGameplayFix(BattleUnit unit, int remainingTurns) {
        int percent = manLucGameplayFixPercent(remainingTurns);
        unit.buffSlots[10][1] = BattleUnit.toShort(unit.baseStats[BattleUnit.STAT_ATTACK] * percent / 100);
        unit.currentStats[BattleUnit.STAT_ATTACK] = BattleUnit.toShort(
                unit.baseStats[BattleUnit.STAT_ATTACK] + unit.buffSlots[10][1]);
    }

    private static int manLucGameplayFixPercent(int remainingTurns) {
        switch (remainingTurns) {
            case 3:
                return 15;
            case 2:
                return 10;
            case 1:
                return 5;
            default:
                return 0;
        }
    }

    private static void restoreMutableStats(BattleUnit unit) {
        unit.currentStats[BattleUnit.STAT_ATTACK] = unit.baseStats[BattleUnit.STAT_ATTACK];
        unit.currentStats[BattleUnit.STAT_DEFENSE] = unit.baseStats[BattleUnit.STAT_DEFENSE];
        unit.currentStats[BattleUnit.STAT_SPEED] = unit.baseStats[BattleUnit.STAT_SPEED];
    }

    private static void reapplyActiveStatEffects(BattleUnit unit) {
        if (unit.hasBuff(1)) {
            unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.buffSlots[1][1]);
        }
        if (unit.hasBuff(2)) {
            unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[2][1]);
        }
        if (unit.hasBuff(4)) {
            unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                    unit.currentStats[BattleUnit.STAT_DEFENSE] + unit.buffSlots[4][1]);
        }
        if (unit.hasBuff(7)) {
            unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_SPEED] + unit.buffSlots[7][1]);
        }
        if (unit.hasBuff(9)) {
            unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_SPEED] + unit.buffSlots[9][1]);
            unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.buffSlots[9][2]);
        }
        if (unit.hasBuff(10)) {
            unit.currentStats[BattleUnit.STAT_ATTACK] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_ATTACK] + unit.buffSlots[10][1]);
        }
        if (unit.hasDebuff(5)) {
            unit.currentStats[BattleUnit.STAT_SPEED] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_SPEED] - unit.debuffSlots[5][1]);
        }
        if (unit.hasDebuff(7)) {
            unit.currentStats[BattleUnit.STAT_DEFENSE] = BattleUnit.toShort(
                    unit.baseStats[BattleUnit.STAT_DEFENSE] - unit.debuffSlots[7][1]);
        }
    }

    private static void resetEffectQueue(BattleUnit unit, int bank) {
        Arrays.fill(unit.activeEffectQueue[bank], (byte) -1);
        unit.activeEffectCount[bank] = 0;
    }

    private static void removeActiveEffect(BattleUnit unit, int bank, int slot) {
        if (bank < 0 || bank >= unit.activeEffectQueue.length
                || slot < 0 || slot >= unit.activeEffectQueue[bank].length) {
            return;
        }
        if (unit.activeEffectQueue[bank][slot] != -1) {
            unit.activeEffectQueue[bank][slot] = -1;
            if (unit.activeEffectCount[bank] > 0) {
                unit.activeEffectCount[bank] = (byte) (unit.activeEffectCount[bank] - 1);
            }
        }
    }
}
