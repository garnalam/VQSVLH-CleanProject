# 110 Battle P21/P17 Catch Edge Closeout

Status: PORTED/PARTIAL.

Scope:

- P21 catch-list back/warning return.
- P21/P101 SMS purchase bypass policy for item `0`.
- P17 storage routing bag/bank/full release.
- P17 caught pet payload shape.
- Existing fail/success catch animation smoke coverage.

## Source Facts Used

| Source | Fact |
| --- | --- |
| `game.h.ai()` | P21 confirm consumes selected ball and enters P17; missing count opens `msgwarm.ui`. |
| `game.h.ai()` | When warning state `f == 1` is confirmed and selected item id is `0`, source closes `choice.ui` and enters battle state `101`. |
| `game.d` state `101` | Enters `game.h.aH()` and updates through `game.h.aM()`, which is the SMS purchase/info path. |
| `game.h.ai()` | Back from P21 closes `choice.ui` and returns to command state. |
| `game.d` case `17` | Success phase uses `game.g.y()` to route storage result. |
| `game.g.y()` | Returns `0` when active bag has space, `1` when bank has space, `2` when both are full. |
| `game.b.P()` | Captured pet payload shape is `[species, level, form, side, quality, nature, hp, exp, visual, skillCount, skills..., pp...]`. |

## Rebuild Mapping

| Behavior | Rebuild Equivalent | Status |
| --- | --- | --- |
| P21 softkey/back | `tickCatchList()` handles `keyBack` and back click -> P20 | PORTED |
| Missing/no-count warning | `NO_BALLS` warning returns to P21 | PORTED/PARTIAL |
| P101 SMS purchase | For battle catch item `0`, rebuild policy grants one item for free and immediately continues into P17; no SMS UI/network is opened | PORTED/REBUILD_POLICY |
| P17 bag storage | Add caught `SourcePetState` to `sourcePets` when size `< 6` | PORTED/PARTIAL |
| P17 bank storage | Add caught `SourcePetState` to `sourcePetBank` when party full and bank `< 100` | PORTED/PARTIAL |
| P17 full release | No add when party full and bank full; open release notice | PORTED/PARTIAL |
| Captured payload | Smoke asserts `game.b.P()`-shaped payload including species, level, hp, visual, skill count | PORTED/PARTIAL |
| Fail/success animation | Existing q2/q3/q4 smoke verifies source-shaped phase path | PORTED/PARTIAL |

## New/Updated Checks

- Added P21 `keyBack` runtime handling.
- Added smoke checkpoint `battle_catch_p21_back_to_command`.
- Added smoke checkpoint `battle_catch_missing_count_warning_return_p21`.
- Added rebuild policy checkpoint `battle_catch_sms_free_item0_p17`.
- Strengthened `battle_catch_storage_bag` with caught payload assertion.
- Strengthened `battle_catch_storage_bank` with caught payload assertion.

## Smoke PNGs

Generated under `rebuild_game/build/smoke/`:

- `battle_catch_p21_back_to_command.png`
- `battle_catch_missing_count_warning_return_p21.png`
- `battle_catch_sms_free_item0_p17.png`
- `battle_catch_storage_bag.png`
- `battle_catch_storage_bank.png`
- `battle_catch_storage_full_release.png`
- `battle_bunny_first_catch_forced_fail.png`
- `battle_bunny_first_catch_fail_escape_effect.png`
- `battle_bunny_retry_p21_item0.png`
- `battle_catch_fail_or_warning.png`
- `battle_catch_generic_roll_success.png`
- `battle_catch_success_q3_flash_mid.png`

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

- Source SMS UI/network is intentionally bypassed by rebuild policy. The currently proven battle catch hook is item `0`; other non-battle SMS purchase screens should use the same free-grant policy only after their source call site is audited.
- P17 animation/effect is source-shaped, not pixel-compared against MIDP.
- Exact `game.h` widget runtime for `/data/ui/choice.ui`, `/data/ui/msgwarm.ui`, and `/data/ui/openbox.ui` remains PARTIAL.
- Full save/global parity of `game.g.a(...)` and `game.g.b(...)` remains broader save-runtime work; this slice asserts runtime payload shape only.
