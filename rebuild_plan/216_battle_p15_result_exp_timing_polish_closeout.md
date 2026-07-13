# 216 - battle P15/result/EXP timing polish closeout

## Scope
- Request: audit/polish P15 enemy replacement, win/lose/result timing, and P8 EXP bar animation.
- Source-first target:
  - `game.d.a(byte 15)` and `game.d` update case `15`.
  - `game.d.a(byte 8/9)`.
  - `game.h.am()/an()/ao()` for P8 EXP and level-up UI.

## Source facts
- P15 entry (`game.d.a(byte 15)`) immediately selects the replacement actor:
  - sets controller state flags,
  - stores `G = g`,
  - marks the replacement active,
  - updates the turn vector,
  - then update case `15` animates the replacement using `game.d.an[r][G]` from `/data/script/cpos.mid`.
- P15 update increments frames with a two-tick cadence and only after the final cpos frame does it resume dispatch:
  - P1/P20/P13 depending previous state and follow-up conditions.
- P8 entry runs after enemy-side win:
  - `game.d.X()` commits the EXP participant vector,
  - `game.h.am()` increments EXP display by `+8`,
  - key confirm fast-forwards to the current target,
  - level-up opens `/data/ui/levelUp.ui` through `game.h.an()/ao()`.
- P9 lose does not award P8 EXP.

## Rebuild changes
- `SourceBattleRuntime.prepareEnemyReplacement(...)` now swaps the active enemy at P15 entry, matching source `a(byte 15)` timing more closely.
- P15 enemy replacement now loads source cpos row:
  - `cposGroup = sourceCposGroup()`
  - `cposRow = 0` for the visible enemy slot in the current rebuild renderer.
- `tickEnemyReplacement(...)` now holds P15 until the cpos row finishes, applying enemy-side offsets each frame before returning to P1.
- Enemy replacement resets `enemyDisplayHp = enemy.hp` at entry so the new enemy does not briefly display stale/zero HP.
- Added focused smoke `battle_p15_enemy_replacement_cpos_mid` to prove:
  - state is still `P15`,
  - enemy has already swapped to Bunny,
  - enemy cpos offset is non-zero,
  - trace includes `sourceEntrySwapped=true` and `cposFrames=...`.

## Verification smoke
- `battle_p12_queue_death_to_p15`
- `battle_p15_enemy_replacement_cpos_mid`
- `battle_p15_enemy_replaced`
- `battle_exp_p8_confirm_fast_forward`
- `battle_exp_levelup_ui`
- `battle_exp_vector_active_only_regression`
- route regressions:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Classification
- P15 enemy replacement source entry timing: PORTED/PARTIAL.
- P15 enemy replacement cpos playback: PORTED/PARTIAL.
- P15 resume dispatch: PORTED/PARTIAL; current route returns to P1, full source P13/P20 follow-up parity is not claimed for every status/skill branch.
- P8 EXP bar increment `+8` and confirm fast-forward: PORTED.
- P8 level-up UI/runtime: PORTED/PARTIAL.
- P9 lose no-EXP behavior: PORTED for current battle result routes.
- Pixel-perfect claim: PENDING. No original-client frame compare was performed for P15 or levelUp.ui.

## Next
- If continuing battle visual debt: compare original-vs-rebuild frames for `levelUp.ui` widgets and EXP bar fill positions, then polish only proven widget/cell mismatches.
- If continuing battle logic: audit P9 lose/revive state 24 and post-loss world reset, because current PC rebuild still treats lose mostly as route result/exit.
