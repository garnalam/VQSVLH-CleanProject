# 94 Battle P0 Entry Cpos Pet Release Audit

## Scope

Audit and port the battle-entry pet release / actor entrance motion.

## Source Chain

- `game.d` loads `/data/script/cpos.mid` into `game.d.an[0..2]`.
- Constructor creates marker sprites `al[]` from sprite `294`.
- Battle actor slots are initialized from `an[r][slot][0..3]`.
- `game.d.a(byte 0)` enters source P0.
- `game.d.b()` P0 tick:
  - copies current frame index into `D[G]`;
  - moves actor `d[G]` to `an[r][G][E[G]*4 + 0..1]`;
  - moves marker `al[G]` to `an[r][G][E[G]*4 + 2..3]`;
  - advances frame roughly every two ticks;
  - advances `G` through battle actors, then enters P20.

Current story battles use group `0`:

| Row | First frame | Last frame | Meaning |
| --- | --- | --- | --- |
| `an[0][0]` | `(400,33,240,81)` | `(177,103,144,85)` | Enemy actor enters from right. |
| `an[0][1]` | `(-400,175,-80,200)` | `(70,223,36,206)` | Player pet enters from left. |

## Rebuild Change

- `SourceBattleRuntime` now prepares P0 cpos rows with `VqsvBattleAnimationTables.cposRow(group,row)`.
- P0 keeps battle in `P0_ENTRY` while row `0` then row `1` are played.
- Actor motion is mapped as offset from the final cpos point into the existing battle renderer positions:
  - row `0` -> enemy offset.
  - row `1` -> player offset.
- Entry offsets are cleared before leaving P0 for normal dispatch/command flow.

## Status

| Area | Status | Note |
| --- | --- | --- |
| `/data/script/cpos.mid` load | PORTED | Existing table loader reused. |
| P0 enemy entry row | PORTED/PARTIAL | Uses source frame coordinates as offsets; absolute renderer anchors are rebuild-side. |
| P0 player pet entry row | PORTED/PARTIAL | Player pet now enters from left via `an[0][1]`. |
| Marker `al[]` sprite `294` | PENDING | Source marker positions are audited but not rendered in this slice. |
| Pixel-perfect entry | PENDING | No original-vs-rebuild frame compare yet. |

## Verification

- `build.ps1`: pass.
- `--check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- `rg -n "Ã|Â|�" rebuild_game/src/main/java`: no Java source hits.
- `git diff --check`: pass.
- PNG smoke:
  - `battle_entry_enemy_cpos`
  - `battle_entry_player_cpos`
  - `battle_elder_command_ui`
  - `battle_bunny_retry_p21_item0`
  - `route_bunny_after_battle_task`
