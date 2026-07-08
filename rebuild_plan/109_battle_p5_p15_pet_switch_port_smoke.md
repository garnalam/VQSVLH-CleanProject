# 109 Battle P5/P15 Pet Switch Port Smoke

Status: PORTED/PARTIAL.

Scope:

- P5 pet switch menu tri-state validation.
- Source-shaped party order equivalent to `game.d.f[]`.
- Minimal P15 player switch transition driven by `cpos.mid` row offsets.
- Smoke-only verification, no client launch.

## Source Facts Used

| Source | Fact |
| --- | --- |
| `game.h.X()` | In battle P5, confirm calls `game.d.a(selectedRow)`. |
| `game.d.a(int)` | Returns `0` when selected pet cannot battle, `1` when already active, `-1` when valid. |
| `game.d.a(int)` | Valid switch moves `f[selectedRow]` to `f[0]`, shifts earlier entries right. |
| `game.h.X()` | Valid switch then enters `game.d` state `15`; warning cases open `msgwarm.ui`. |
| `game.d` case `15` | Incoming pet transition advances through `cpos.mid` rows. |

## Rebuild Mapping

| Original Behavior | Rebuild Equivalent | Status |
| --- | --- | --- |
| P5 opens `petstate.ui` rows in party order | `preparePetMenu()` builds rows from current source pet order | PORTED/PARTIAL |
| `game.d.a(int)` dead check | `sourcePetAlive(...)` warning `PET_CANNOT_BATTLE` | PORTED |
| `game.d.a(int)` already-active check | selected source index `0` warning `PET_ALREADY_ACTIVE` | PORTED |
| `f[]` reorder | selected `SourcePetState` moved to index `0` | PORTED/PARTIAL |
| valid switch enters P15 | `enterPlayerSwitchTransition(...)` enters `P15_PLAYER_SWITCH` | PORTED/PARTIAL |
| P15 cpos movement | player render offset follows cpos row delta | PARTIAL |
| P5 back | `keyBack` and back click return to P20 command | PORTED |

## Smoke PNGs

Generated under `rebuild_game/build/smoke/`:

- `battle_p5_pet_switch_list.png`
- `battle_p5_current_warning.png`
- `battle_p5_dead_warning.png`
- `battle_p5_valid_p15_transition.png`
- `battle_p5_back_to_command.png`
- extra assertion: `battle_p5_valid_after_switch.png`

## Verification

- `build.ps1`: pass.
- `com.vqsv.rebuild.Main --check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: pass.
- Route regression:
  - `route_sophie_after_battle_branch`: pass.
  - `route_bunny_after_battle_task`: pass.
  - `route_elder_after_battle_reward_state`: pass.

## Remaining Partial/Pending

- P15 transition is source-shaped with `cpos.mid` offsets, but not pixel-compared against MIDP.
- Full source vector fields (`x`, `J`, old active status-11 cleanup) are represented by current source pet order/runtime state only; full status-link parity remains part of broader battle engine cleanup.
