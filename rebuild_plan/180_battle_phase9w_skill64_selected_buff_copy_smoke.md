# 180 Battle Phase 9-W Skill 64 Selected Buff Copy Smoke

Status date: 2026-07-09

Status: PHASE 9-W / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Wire source-selected target slot for skill `64`.
- Smoke the source behavior audited in `179_battle_phase9v_skill64_selected_index_audit.md`.
- Prove skill `64` copies selected target source buffs, clears target buffs, stores selected target slot in buff `11`, and does not create fake damage.

## Source Facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`

Rows:

```text
skill 64 = [6,181,593,0,1,10,1,11,-1,0]
buff 11  = [344,359,3,-1,-1]
```

Source behavior:

| Area | Source-backed behavior |
| --- | --- |
| P6 confirm | `game.d.i()` sets `h.I` from `h.H[C]`, where `h.H` contains the real `game.d.d[]` target slot. |
| q() case 64 | `game.d.q()` calls `h.a((byte)skill[7], h.I, skillId)`. |
| buff 11 | `game.b.a(11, selectedSlot, skill64)` stores selected slot in `v[11][1]`, copies selected target active source buffs onto caster, then calls target `D()` to clear target buffs. |
| Damage | `skill[3] == 0`, so P7 skips normal damage text/apply path. |

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | `commitSelectedTarget()` now writes `targetSlots[selectedTargetIndex]` into `player.battleUnit.selectedTargetSlot`, matching source `h.I`. Added smoke-only accessors for source buff stored values. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_phase9w_skill64_selected_buff_copy`. |
| `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md` | Updated skill `64` status. |
| `rebuild_plan/179_battle_phase9v_skill64_selected_index_audit.md` | Corrected wording: copied source buff values are passed to helper, but individual buff cases may recompute stored value. |

Smoke setup:

1. Start elder battle P7 with player skill `64`.
2. Preload enemy source buff `2` through the existing Phase 9-N helper.
3. Advance to P7 phase 3.
4. Assert:
   - enemy buff `2` is cleared.
   - player receives copied buff `2`.
   - player receives buff `11`.
   - buff `11` stores selected target slot `0`.
   - no `battle P7 damage frame skill=64` trace exists.
   - `battle P7 no-damage skill=64` trace exists.
   - `game.d.q postEffect skill=64` trace exists.

Note:

- In the smoke, copied buff `2` stores value `5` on the player, not the prepared enemy value `10`.
- This matches the source-shaped behavior better than the first assert: `game.b.a()` receives the copied value, but buff `2` recomputes its stored value from the caster's stats.

## Smoke

Output:

```text
rebuild_game/build/smoke/phase9w/battle_phase9w_skill64_selected_buff_copy.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9w_skill64_selected_buff_copy` | PASS |

## Regression

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Regression PNG folder:

```text
rebuild_game/build/smoke/phase9w_regression/
```

## Status Decision

| Area | Status |
| --- | --- |
| Skill `64` selected slot wiring | PORTED/PARTIAL, smoke-covered. |
| Skill `64` copy selected target buffs | PORTED/PARTIAL, smoke-covered for one selected enemy target. |
| Skill `64` clear selected target buffs | PORTED/PARTIAL, smoke-covered. |
| Skill `64` no-damage P7 path | PORTED/PARTIAL, smoke-covered. |
| Multi-target/formation selected slot parity | PENDING, because current smoke has one enemy target slot `0`. |
| Exact visual/pixel parity for skill `64` effect chunks | PARTIAL/PENDING, not claimed by this slice. |

## Next Roadmap Step

Recommended:

```text
Phase 9-X: audit no-damage/default effectMode 1 rows 4/5/14/24/25/34/35/44/65.
```

Reason:

- Skill `64` was the last unique selected-index q() branch in the current Phase 9 matrix.
- The remaining Phase 9 skill gaps are mostly default no-damage effectMode rows and broader visual/UI parity.
