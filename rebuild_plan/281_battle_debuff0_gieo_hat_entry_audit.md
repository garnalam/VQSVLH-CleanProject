# 281 - Battle Debuff0 Gieo Hat Entry Audit

Date: 2026-07-13

Scope: start of target-side debuff table `aq.c[7]`, row `0`.

This is an audit-first handoff. Do not add new debuff0 code until the missing
closeout smoke gaps below are accepted or proven unnecessary.

## Source Rows

```text
debuff0 = [311,322,3]
skill1  = [0,118,530,50,0,45,2,0,4,0]
skill7  = [0,124,536,75,2,30,2,0,3,0]
effect.mid[1] = [0,0,20,0,-1,-1,0]
effect.mid[7] = [0,0,20,0,-1,-1,0]
bufDebuf ar[1][0] = 0
bufDebuf aq[0] = [1,18,0,-1]
```

Decoded source text:

| Row | Name | Description |
| --- | --- | --- |
| `debuff0` | `Gieo Hat` | Each turn reduces HP by `X` for `Y` turns. |
| `skill1` | `Duong viem` | Low damage plus burn-like HP reduction for 3 turns. |
| `skill7` | `Chuoc nhiet chi xuc` | Low damage plus burn-like HP reduction for 3 turns. |

## Source Chain

| Source | Proof |
| --- | --- |
| `game.b.b(target)` damage path | Skills `1` and `7` compute direct damage as `B() * skill[3] / 100 + B() / skill[8]`; then target debuff id comes from skill column `[7] = 0`. |
| `game.b.b(target)` debuff apply | For debuff id `0`, source stores `target.w[0][1] = preSkillRawDamage`, queues bank `1`, stores duration from `aq.c[7][0][2]`, stores source skill id in `w[0][3]`, and marks `w[0][4]=1`. |
| `game.b.b(target)` immunity/chance | Buff14 blocks incoming debuffs before apply. If chance is not `-1`, source rolls `ae.a(100) > chance`; skills `1/7` use chance column `[8]` as divisor in their damage formula and no extra chance gate in the audited switch. |
| `game.b.q(0)` tick | Each P12/P13 tick damages by `w[0][1] / aq.c[1][w[0][3]][8]`; if HP reaches zero, actor state becomes dead state `3`. |
| `game.b.c(0,slot)` | Decrements duration, clears active flag and removes queue slot on expiry. |
| `game.d.ai[1]` | Debuff body visual gate includes id `0`, so P12/P13 body visual exists. |
| `bufDebuf.mid` | `ar[1][0] = 0`, `aq[0] = [1,18,0,-1]`, so body visual is speffect `18`. |
| `game.h` HUD status rule | Debuff icon cell is `id + 1`, so debuff0 icon is `1`; duration cells are `134 + duration`. |

## Formula Notes

For producer damage:

```text
preSkillRawDamage = B()
directDamage(skill1) = B() * 50 / 100 + B() / 4
directDamage(skill7) = B() * 75 / 100 + B() / 3
storedDebuffRaw = B()
tickDamage(skill1 source) = storedDebuffRaw / 4
tickDamage(skill7 source) = storedDebuffRaw / 3
duration = 3
```

The stored tick base is the raw `B()` snapshot before the skill-specific damage
scaling, matching `var6_8` in `game.b.b(target)`.

## Current Rebuild Status

| Area | Current status |
| --- | --- |
| Damage formula and apply slots | PORTED in `VqsvBattleUnit.computeDamage()` / pending debuff commit path. |
| P12/P13 active queue visual | PORTED for debuff0: speffect `18`. |
| Tick damage | PORTED: `BattleUnit.tickSourceDebuff(0, slot)` divides stored raw by source skill `skill[8]`. |
| Icon/duration | PORTED/PARTIAL: covered by generic status slot checks, but needs dedicated debuff0 closeout pass. |
| Producer skill1/skill7 visual | PORTED/PARTIAL: Phase 9 coverage exists, but no fresh debuff0-specific closeout doc/smoke set yet. |
| Expiry | PORTED/PARTIAL: lifecycle code exists, but needs dedicated debuff0 expiry checkpoint matching the new buff closeout standard. |
| Pixel-perfect visual | NOT CLAIMED. |

## Existing Smoke Anchors

Known existing checkpoints:

```text
battle_status_debuff0_damage_tick
battle_p12_debuff0_queue_start
battle_p12_debuff0_damage_text
battle_p12_debuff0_after_apply
battle_p7_to_p12_queue_order_debuff0
phase9c producer/conditional checkpoints around skills 1/7 and 3/9
```

These prove the runtime is not starting from zero. The next closeout should
wrap them into a dedicated debuff0 slice and add any missing focused checkpoints
instead of rewriting the whole system.

## Entry Verification

Re-run on 2026-07-13:

```text
battle_status_debuff0_damage_tick PASS
battle_p12_debuff0_queue_start PASS
battle_p12_debuff0_damage_text PASS
battle_p12_debuff0_after_apply PASS
battle_p7_to_p12_queue_order_debuff0 PASS
```

Generated PNGs:

```text
rebuild_game/build_intro_demo/debuff0_entry_verify/battle_status_debuff0_damage_tick.png
rebuild_game/build_intro_demo/debuff0_entry_verify/battle_p12_debuff0_queue_start.png
rebuild_game/build_intro_demo/debuff0_entry_verify/battle_p12_debuff0_damage_text.png
rebuild_game/build_intro_demo/debuff0_entry_verify/battle_p12_debuff0_after_apply.png
rebuild_game/build_intro_demo/debuff0_entry_verify/battle_p7_to_p12_queue_order_debuff0.png
```

## Recommended Debuff0 Closeout Slice

Add or verify focused PNG checkpoints:

| Checkpoint intent | Required proof |
| --- | --- |
| before/no debuff | No debuff0 icon, no active slot. |
| skill1 producer/apply | P7 producer uses `effect.mid[1]`, applies debuff0, stores source skill `1`, icon `1/137`. |
| skill7 producer/apply | P7 producer uses `effect.mid[7]`, applies debuff0, stores source skill `7`, icon `1/137`. |
| tick from skill1 | HP delta equals stored raw `/ 4`; duration `3 -> 2`, duration cell `136`. |
| tick from skill7 | HP delta equals stored raw `/ 3`; duration `3 -> 2`, duration cell `136`. |
| P12/P13 body visual | Speffect `18` appears because `game.d.ai[1]` includes debuff id `0`. |
| expiry | After 3 ticks, debuff0 clears and icon disappears. |
| buff14 block regression | With buff14 active, incoming debuff0 is blocked and no debuff0 icon appears. |

## Classification

```text
Runtime foundation: PORTED
Dedicated closeout state: PORTED
Pixel-perfect: NOT CLAIMED
```

Reason: dedicated debuff0 closeout is now complete in
`282_battle_debuff0_gieo_hat_closeout.md`.

## Next Step

Move to debuff1 dedicated closeout in table order. Do not claim pixel-perfect
for debuff0 speffect18 until there is original-vs-rebuild frame comparison.
