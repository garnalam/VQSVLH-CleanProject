# 284 - Battle Debuff2 Quan Quanh Closeout

Scope: debuff table `aq.c[7][2]`, producer skills `12/18`,
P12/P13 visual/tick, catch multiplier, battle command locks, normal damage
formula downstream, and buff14 block regression.

## Plain Gameplay Explanation

`Quan Quanh` is the battle bind status.

In normal play it means:

- the affected player's active pet cannot use items;
- the affected player's active pet cannot switch to another pet;
- the affected player's active pet cannot run away;
- the status does not stop skill use;
- it does not stop catch command or shop command in the source command handler;
- it has no HP damage tick by itself;
- it makes catch chance better when the enemy target has it;
- source `game.b.B()` also makes the bound target use higher effective defense
  when taking normal direct damage.

This last defense point is unintuitive, but it is source-backed:
`target.p(2)` applies `targetDefense * (100 + aq.c[3][2][5]) / 100`.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`
- `modules/script/decoded/data__script__chs.mid.json`

Rows:

```text
debuff2 = [313,324,3]
status/held row aq.c[3][2] = [215,3,239,5,1,15]
skill12 = [1,129,541,50,0,45,2,2,-1,0]
skill18 = [1,135,547,150,3,15,2,2,-1,0]
bufDebuf ar[1][2] = [0,21,0,0,1,6,0,0]
```

Source text rows:

```text
313 = Quan Quanh
324 = cannot switch pet / cannot run / cannot use item, lasts Y turns
541 = low damage, applies Quan Quanh 3 turns
547 = higher damage, applies Quan Quanh 3 turns
```

Command consumer proof:

- `game.h.d(b v1)` command case `2` checks `v1.p(2)` and opens
  `msgwarm.ui`: cannot use item.
- `game.h.d(b v1)` command case `3` checks `v1.p(2)` and opens
  `msgwarm.ui`: cannot switch pet.
- `game.h.d(b v1)` command case `5` checks `v1.p(2)` and opens
  `msgwarm.ui`: cannot run.
- `game.h.d(b v1)` command case `0` skill, case `1` catch, and case `4`
  shop do not check `v1.p(2)`.

Tick consumer proof:

- `game.b.q(2)` returns immediately.
- Duration is still decremented and cleared by the shared debuff tick path.

Formula proof:

- `game.b.B()` uses the opponent/target debuff flag:
  if target has `p(2)`, target defense is multiplied by
  `(100 + aq.c[3][2][5]) / 100`.
- `aq.c[3][2][5] = 15`, so a defense smoke target with defense `50`
  uses effective defense `57` by integer math.

## Implemented / Tightened

Runtime:

- `BattleUnit.baseAttack()` now applies the source-backed debuff2 target
  defense modifier for normal damage formula.
- Existing command locks for item/pet/run are now smoke-locked against source
  behavior.
- Skill/catch/shop non-lock behavior is smoke-locked.

Focused smoke checkpoints:

```text
battle_status_debuff2_before_no_effect
battle_status_debuff2_skill12_producer_apply
battle_status_debuff2_skill18_producer_apply
battle_status_debuff2_body_visual_speffect6_type8
battle_status_debuff2_tick_noop_duration
battle_status_debuff2_expiry_clears_icon
battle_status_debuff2_catch_multiplier
battle_status_debuff2_defense_formula_reduces_damage
battle_status_debuff2_command_locks_item_pet_run
battle_status_debuff2_allows_skill_catch_shop
battle_status_debuff2_buff14_blocks_skill12
```

These checkpoints are now part of fixed `battle_quick` regression.

## Measured Smoke Results

Focused PNG output:

```text
rebuild_game/build_intro_demo/debuff2_closeout/
```

Numeric before / during / after:

| Slice | Before | During | After |
| --- | --- | --- | --- |
| no effect | enemy icon count `0`, no debuff2 | no active queue | unchanged |
| skill12 producer | no debuff2 | skill12 hits and applies implicit debuff id `2` | enemy icon `3/137`, source skill `12`, duration `3` |
| skill18 producer | no debuff2 | skill18 hits and applies implicit debuff id `2` | enemy icon `3/137`, source skill `18`, duration `3` |
| body visual | enemy has debuff2 icon `3/137` | P12 visual uses actor segment then speffect `6`, AH type `8` | actor remains anchored |
| tick no-op | HP `50`, duration `3` | no HP/stat tick damage | HP `50`, duration `2`, icon `3/136` |
| expiry | HP `50`, duration `3` | three shared debuff ticks | HP `50`, duration `0`, icon cleared |
| catch multiplier | item1 base chance from P21 | debuff2 active on target | chance = `base * 12 / 10` |
| defense formula | attack `140`, target defense `50` | debuff2 raises effective defense to `57` | normal skill10 damage reduced; smoke image shows bound hit `83` |
| command lock | player has debuff2 | command item/pet/run confirmed | all route to `WARN` with source warning text |
| non-lock command | player has debuff2 | command skill/catch/shop confirmed | skill `P3`, catch `P21`, shop `P11` |
| buff14 block | target has buff14 | skill12 hits | debuff2 not applied, buff14 icon remains |

## Verification

```text
build.ps1 PASS
com.vqsv.rebuild.Main --check PASS
VqsvBattleDamageFormulaCheck PASS
focused debuff2 closeout PNG smoke 11/11 PASS
battle_quick PASS 165/165
```

## Classification

```text
Debuff2 core flag/duration logic: PORTED
Producer skills 12/18: PORTED
P12/P13 body visual speffect6 type8: PORTED
No-op tick and expiry clear: PORTED
Catch multiplier 12/10: PORTED
Command locks item/pet/run: PORTED
Skill/catch/shop allowed while bound: PORTED
Normal target-defense formula modifier: PORTED
Buff14 block regression: PORTED
Pixel-perfect: NOT CLAIMED
```

Pixel-perfect is not claimed because there is no original-vs-rebuild frame
compare for the speffect6/type8 active queue animation.

## Next Step

Move to debuff3 dedicated closeout in table order: `Thuc Loai`, producer
skills `13/19`, delayed damage at expiry, body visual, buff14 block, and
numeric before/during/after smoke.
