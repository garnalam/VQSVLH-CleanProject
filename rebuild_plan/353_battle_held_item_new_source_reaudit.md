# 353 - Battle Held Item New Source Reaudit

Date: 2026-07-14

Status: REAUDITED / SOURCE-BACKED / SMOKE-VERIFIED / NO RUNTIME PATCH NEEDED.

## Scope

User request:

- Re-audit held item / equipment effects against the current merged source.
- If rebuild logic is wrong, change it back to source.
- If a branch is too complex or policy-like, keep it explicit for user decision.

This audit covers:

- `aq.c[3][0..11]` held item rows from the current `db.mid`.
- Panel equipment ownership path `q.L -> pet c[5]`.
- Battle consumers for held item effects.
- Focused smoke coverage.

No battle runtime code was changed by this audit because the current rebuild
behavior matched the source-backed hooks checked below.

## Current Source Files Used

After the S60 merge, the clearest current-source battle callsites are in:

- `modules/source_code/decoded/decompiled_source_cfr/game/a.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/script/decoded/data__script__db.mid.json`

Historical docs often say `game.d`/`game.b`. The source role mapping shifted
after the current decode, but the actual hooks are still the same source pattern:

```text
pet.c[5] / source payload[2] = held item id
pet.f((byte)id) = held item check
aq.c[3][id] = held item metadata and params
```

## Source Data Rows

Rows from current `aq.c[3][0..11]`:

| id | name | icon | raw params | source meaning |
| ---: | --- | ---: | --- | --- |
| 0 | Man Da La Thach | 1 | `[5,1,30,100]` | if HP <= 30%, attack part +100% |
| 1 | Hong Sac Hai Loa | 2 | `[5,1,10]` | attack +10% |
| 2 | Quy Xac Toai Phien | 3 | `[5,1,15]` | defense +15% |
| 3 | O Nha Ue | 4 | `[5,1,20]` | debuff resist 20% |
| 4 | Vien Co Long Cot | 5 | `[5,1,10]` | crit chance +10 points |
| 5 | Mat Phong Sao | 6 | `[5,1,20]` | direct participant EXP +20% |
| 6 | Ky Cu Giai Xac | 7 | `[5,1,100]` | reserve nonparticipant EXP share |
| 7 | Linh Trung Thi Hai | 8 | `[5,1]` | turn priority/order hook |
| 8 | Hap Huyet Dang Man | 9 | `[5,1,10,20]` | 10% leech chance, heal 20% damage |
| 9 | Ca Thon Bon | 10 | `[5,1]` | attacks cannot miss |
| 10 | Cam Lam Chi Diep | 11 | `[5,1,10]` | target HP floor 10 after hit |
| 11 | Sung vat loi dat | 12 | `[5,1,20]` | catch chance bonus 20% |

Vietnamese text is source-backed in runtime through `chs.mid`; names here are
ASCII-transliterated to keep this audit file stable.

## Source Hook Matrix

| id | Current source proof | Rebuild mapping | Status |
| ---: | --- | --- | --- |
| 0 | `game/i.java` damage stat path checks `f((byte)0)` and HP threshold `aq.c[3][0][5]`, bonus `aq.c[3][0][6]`. | `BattleUnit.baseAttack()` low-HP attack boost; smoke `battle_status_form0_low_hp_attack_boost`. | PORTED |
| 1 | `game/i.java` checks `f((byte)1)` and applies `aq.c[3][1][5]` attack boost. | `BattleUnit.baseAttack()` attack boost; smoke `battle_held_item1_attack_boost`. | PORTED |
| 2 | `game/i.java` checks defender `f((byte)2)` and applies `aq.c[3][2][5]` defense boost. | `BattleUnit.baseAttack()`/target defense path; smoke `battle_held_item2_defense_boost`. | PORTED |
| 3 | `game/i.java` / battle debuff plan reduces debuff chance by `aq.c[3][3][5]`. | `VqsvBattleEffectLogic.planTargetDebuff()`; smoke `battle_held_item3_debuff_resist`. | PORTED |
| 4 | `game/i.java` damage roll adds `aq.c[3][4][5]` to crit chance. | `BattleUnit.computeDamage()` crit chance; smoke `battle_held_item4_crit_window`. | PORTED |
| 5 | `game/a.java` EXP award checks participant `f((byte)5)`, multiplies by `(100 + aq.c[3][5][5]) / 100`. | P8 EXP participant multiplier; smoke `battle_held_item5_exp_multiplier`. | PORTED |
| 6 | `game/a.java` reserve EXP branch checks nonparticipant `f((byte)6)` and grants `/1000` share. | P8 reserve EXP share; smoke `battle_held_item6_reserve_exp_share`. | PORTED |
| 7 | `game/a.java Q()` reorders battle queue when a unit has `f((byte)7)`. | `heldItem7PriorityPlayerFirst()` in 1v1 routes; smoke `battle_held_item7_turn_priority`. | PORTED/PARTIAL |
| 8 | `game/a.java V()` post-hit checks attacker `f((byte)8)`, roll `<= aq.c[3][8][5]`, heal damage `* aq.c[3][8][6] / 100`. | P7 post-damage leech; smoke `battle_held_item8_leech_heal`. | PORTED |
| 9 | `game/a.java U()` miss path forces miss chance to zero when attacker `f((byte)9)`. | `sourceP7MissChance()`; smoke `battle_held_item9_no_miss`. | PORTED |
| 10 | `game/a.java R()/U()` floors target HP to `aq.c[3][10][5]` when attacker `f((byte)10)`. | `applyP7HeldItem10HpFloor()`; smoke `battle_held_item10_hp_floor`. | PORTED |
| 11 | `game/a.java m(item)` catch chance sets status factor index to held path and multiplies by `(100 + aq.c[3][11][5]) / 100`. | Catch chance debug/menu path; smoke `battle_held_item11_catch_chance`. | PORTED |

## Equipment Ownership / UI Matrix

| Area | Source | Rebuild | Status |
| --- | --- | --- | --- |
| Inventory vector | `q.L` rows `[equipmentId, equippedFlag, 0]` for id `< 12`. | `Scene.sourceEquipmentItems`. | PORTED/PARTIAL |
| Pet slot | source pet `c[5]`; serialized payload index `2`. | `SourcePetState.sourcePayload[2]`. | PORTED/PARTIAL |
| Render list | `choice.ui`, icon/name/description from `aq.c[3]`. | `sourceEquipmentChoiceView()`. | PORTED/PARTIAL |
| Equip | `q.f(itemId, petIndex)` clears old owner, marks selected row worn. | `sourceEquipEquipment(...)`. | PORTED/PARTIAL |
| Unequip | `q.l(itemId)` clears worn flag and selected pet slot. | `sourceUnequipEquipment(...)`. | PORTED/PARTIAL |
| Save/load | RMS stores source vectors; pet payload stores `c[5]`. | `VqsvSaveRuntime` stores `equipment.count` and rows. | PORTED/PARTIAL |

## Focused Verification

Ran from `rebuild_game`:

```text
panel_petstate_held_item0_widget_59_60
panel_petstate_petsetting_equipment_choice_equip_success_msg
panel_petstate_petsetting_equipment_choice_transfer_success_msg
panel_petstate_petsetting_equipment_save_load_qL
battle_status_form0_low_hp_attack_boost
battle_held_item1_attack_boost
battle_held_item2_defense_boost
battle_held_item3_debuff_resist
battle_held_item4_crit_window
battle_held_item5_exp_multiplier
battle_held_item6_reserve_exp_share
battle_held_item7_turn_priority
battle_held_item8_leech_heal
battle_held_item9_no_miss
battle_held_item10_hp_floor
battle_held_item11_catch_chance
```

Result:

```text
Focused held-item/equipment recheck PASS 16/16
```

Output directory:

```text
rebuild_game/build/smoke/held_item_reaudit/
```

## Honest Remaining Gaps

| Gap | Status | Decision |
| --- | --- | --- |
| Internal naming still uses `STAT_FORM` / `hasSourceFormStatus()` for source `c[5]`. | PORTED/PARTIAL naming debt | Safe to leave for now; rename later only as refactor. |
| Historical traces/docs still mention `game.d` in some strings. | DOCUMENTATION/TRACE DEBT | Not gameplay-breaking; update only when touching those checkpoints. |
| Held item 7 exact multi-active/multi-slot order. | PORTED/PARTIAL | Current routes are 1v1; full party queue parity can wait until multi-active battles are rebuilt. |
| Bank/storage-side equipment ownership. | PENDING | Current source-shaped implementation covers party pets and q.L save/load; full bank transfer parity is a future storage slice. |
| Original RMS byte/vector parity. | PENDING | Current save is PC source-shaped properties, not byte-perfect RMS. |
| Original-vs-rebuild pixel compare for equipment UI. | PENDING | PNG smoke verifies current render behavior, not original-client pixel-perfectness. |

## Conclusion

No runtime patch is needed for held item/equipment effects in this slice.

The current rebuild logic is source-shaped and smoke-verified for held item ids
`0..11`. The next work should continue skill/effect completion, unless the user
chooses to spend a separate slice on one of the remaining gaps above.

## Next

Recommended next step:

```text
Return to skill/effect roadmap. Keep held-item smoke checkpoints in regression
when touching damage formula, P7 post-damage, P8 EXP, P21/P17 catch chance, or
panel petsetting equipment.
```

