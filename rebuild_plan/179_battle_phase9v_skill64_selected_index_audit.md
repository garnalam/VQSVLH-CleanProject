# 179 Battle Phase 9-V Skill 64 Selected-Index Audit

Status date: 2026-07-09

Status: PHASE 9-V / AUDIT-ONLY / NO CODE CHANGE.

Purpose:

- Audit skill `64` before implementation/smoke.
- Prove what the selected index is in source.
- Prove what buff `11` does.
- Compare source behavior with the current rebuild mapping.

Rules:

- Logic source first.
- No smoke/client in this slice.
- Do not treat row shape alone as enough proof.

## Sources

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`

## Source Rows

Decoded `aq.c` rows:

```text
skill 64 = [6,181,593,0,1,10,1,11,-1,0]
buff 11  = [344,359,3,-1,-1]
```

Interpretation:

| Field | Value | Meaning in this audit |
| --- | ---: | --- |
| `skill[3]` | `0` | No normal damage power. |
| `skill[6]` | `1` | Effect mode is buff/effect, consumed by `game.d.q()`. |
| `skill[7]` | `11` | Buff id `11`. |
| `skill[8]` | `-1` | No chance gate for the q() branch. |
| `skill[9]` | `0` | Target opposing side in `game.d.b(byte skillId)`. |

## Source Matrix

| Step | Source | Behavior | Status |
| --- | --- | --- | --- |
| Target vector build | `game.d.b(byte)` | Clears `h.G/h.H`, then pushes each valid target unit into `h.G` and its real `d[]` slot as string into `h.H`. For target mode `0`, it selects living opposing-side units. | PORTED conceptually. |
| P6 cursor move | `game.d` state `6` | Cursor `C` indexes `h.G/h.H`. Moving cursor calls `a(Integer.parseInt(h.H[C]), true)` and redraws target marks. | PORTED/PARTIAL in rebuild P6. |
| P6 confirm | `game.d.i()` | Sets `h.p = h.G[C]` and `h.I = Byte.parseByte(h.H[C])`; then starts execution. | SOURCE-BACKED. |
| Auto target | `game.d` state `2` | Random/auto target paths also set `h.I = Byte.parseByte(h.H[randomIndex])`. | SOURCE-BACKED. |
| q() special case | `game.d.q()` case `64` | Calls `h.a((byte)skill[7], (int)h.I, skillId)`, so buff `11` receives the selected real `d[]` slot. | SOURCE-BACKED. |
| q() text | `game.d.q()` after `by == 1` | For `21/27/42/48/62/64/68`, text is drawn on attacker side with `an.f(aq.c[6][buffId][0])`. | SOURCE-BACKED. |

Important conclusion:

```text
h.I is not just UI row index.
h.I is the selected unit slot from game.d.d[], stored in h.H by game.d.b(byte).
```

## Buff 11 Behavior

Source `game.b.a(byte by, int n2, int n3)` case `11`:

| Source action | Meaning |
| --- | --- |
| `v[11][1] = (short)n2` | Store selected source unit slot in buff 11. |
| `b2 = game.d.a().d[n2]` | Read the selected unit from the battle unit array. |
| Loop `i < b2.N[0]` | Iterate selected unit's active buff queue. |
| `this.a(b2.x[0][i], b2.v[...][1], game.d.a().d[n2].K[i])` | Re-apply each active source buff id from selected unit onto the caster, passing the copied buff's stored value and scratch/source skill into the generic buff apply helper. Individual buff cases may recompute their stored value from the caster's stats. |
| `b2.D()` | Clear all buffs from selected unit. |

Source `game.b.o(int)` case `11` repeats the copy/clear behavior when buff `11` ticks.

Source `game.b.D()`:

- Clears every active source buff in `v[][]`.
- Resets mutable stats through `n(buffId)`.
- Clears active buff queue entries in `x[0][]`.

## Rebuild Mapping

| Source concept | Rebuild equivalent | Status |
| --- | --- | --- |
| `game.b.v[][]` | `BattleUnit.buffSlots[][]` | PORTED/PARTIAL. |
| `game.b.x[0][]`, `N[0]` | `BattleUnit.activeEffectQueue[0][]`, `activeEffectCount[0]` | PORTED/PARTIAL. |
| `game.b.D()` | `BattleUnit.clearBuffs()` | PORTED/PARTIAL. Clears buff slots, active queue, restores mutable stats, reapplies remaining effects. |
| `game.b.a(11, selectedSlot, skill64)` | `BattleUnit.copySourceBuffsFrom(source, selectedIndex, sourceSkill)` plus `applySourceBuff(11, selectedIndex, sourceSkill)` | PORTED/PARTIAL. Behavior shape is present. |
| `h.G/h.H` target vector | `targetUnits[]` and `targetSlots[]` in `VqsvBattleRuntime` | PORTED/PARTIAL. |
| `h.I` assignment | `BattleUnit.selectedTargetSlot` | GAP: declaration exists, but no assignment site was found in current source scan. |
| P7 q() case `64` | `VqsvBattleRuntime.applyP7PostSkillEffects()` case `64` | PARTIAL. It calls `copySourceBuffsFrom`, but currently depends on `selectedTargetSlot`. |

Current code gap:

```text
BattleUnit.selectedTargetSlot is declared, but current scan found no assignment.
For one-target player-vs-enemy smoke, the default 0 may accidentally match.
For source parity, commitSelectedTarget() must write targetSlots[selectedTargetIndex]
into the attacker's BattleUnit.selectedTargetSlot.
```

## Status Decision

| Area | Status | Reason |
| --- | --- | --- |
| Skill 64 source classification | PORTED/AUDITED | Source row and `game.d.q()` branch are proven. |
| Selected index meaning | PORTED/AUDITED | `h.I` is proven to be selected `d[]` unit slot from `h.H`. |
| Buff 11 source behavior | PORTED/AUDITED | Copy selected target's active buffs to caster, then clear target buffs. |
| Rebuild copy/clear helper | PARTIAL | Helper exists and resembles source, but needs deterministic smoke. |
| Rebuild selected slot wiring | MISSING/PENDING | `selectedTargetSlot` is not assigned in the current scan. |
| Skill 64 smoke coverage | MISSING | No dedicated checkpoint yet. |
| Multi-target/formation parity | PENDING | Current rebuild battle often has one player and one enemy; source supports `d[]` slots. |

## Required Next Code Slice

Recommended next roadmap step:

```text
Phase 9-W: smoke skill 64 selected-index copy/clear behavior.
```

Scope:

1. Wire selected slot:
   - In `commitSelectedTarget()`, set `player.battleUnit.selectedTargetSlot`
     to `targetSlots[selectedTargetIndex]` when confirming a target.
2. Add read-only/debug helpers only if needed:
   - Check player has copied buff.
   - Check selected target no longer has copied buff.
   - Check player buff `11` stores selected slot.
3. Create deterministic smoke:
   - Give target one active buff.
   - Force player skill `64`.
   - Confirm no normal damage.
   - Assert copied buff appears on player.
   - Assert target buffs are cleared.
   - Assert buff `11` stores selected source slot.

Suggested smoke PNG:

```text
battle_phase9w_skill64_selected_buff_copy.png
```

Regression after code:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java mojibake scan
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
```

## Next Roadmap Step After Phase 9-W

After skill `64` is smoke-covered, continue Phase 9 with:

```text
Audit no-damage/default effectMode 1 rows: 4/5/14/24/25/34/35/44/65.
```

Reason:

- Skill `64` is the last unique selected-index q() branch in the current Phase 9 matrix.
- The remaining Phase 9 gaps are mostly no-damage buff/effectMode rows and broader UI/effect parity.
