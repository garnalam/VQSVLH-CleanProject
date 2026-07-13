# 212 - Battle entry NPC UI/cpos power percent closeout

Date: 2026-07-10

## Scope

Audit and port the battle entry UI/animation that plays when an NPC battle starts.

## Source audit

Primary source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

Source chain:

- `game.d.d()` loads `/data/script/pos.mid`, `/data/script/cpos.mid`, `/data/script/effect.mid`, `/data/script/speffect.mid`, `/data/script/blood.mid`, then enters state 0 with `this.a((byte)0)`.
- State 0 in `game.d.b()` advances `game.d.an[this.r][this.G]` from `/data/script/cpos.mid`.
- While cpos runs, source calls `game.h.a(this.d[1], this.d[0], this.d[this.G], this.E[this.G] + 1, frameCount)`.
- `game.h.a(b,b,b,int,int)` writes `battle.ui` widget 58/59 percent strings:
  - Favorable player relation: player side `59` tweens 100% -> 300%, enemy side `58` tweens 100% -> 60%.
  - Unfavorable player relation: player side `59` tweens 100% -> 60%, enemy side `58` tweens 100% -> 300%.
  - Neutral relation: both stay 100%.
- `game.h.a(b,b)` sets final/static relation percent after entry.

## Rebuild changes

- `VqsvBattleRuntime` now resets battle power percent to 100/100 at battle entry.
- During P0 cpos entry, rebuild updates `battlePlayerPowerPercent` / `battleEnemyPowerPercent` with the same source-shaped interpolation as `game.h.a(b,b,b,int,int)`.
- After P0 completes, rebuild restores final relation percent.
- Existing cpos actor entry from `/data/script/cpos.mid` remains source-backed/partial; this slice tightens the missing `battle.ui` widget 58/59 percent animation.

## Status

- P0 cpos actor movement: PORTED/PARTIAL.
- `battle.ui` widget 58/59 percent tween during entry: PORTED.
- Full original-client pixel-perfect compare for P0: PENDING.

## Smoke

Added:

- `battle_entry_power_percent_ui`

Existing regression smoke:

- `battle_entry_enemy_cpos`
- `battle_entry_player_cpos`
- `battle_entry_both_landed`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Verification

Passed:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Mojibake/SMS scan: no matches
- Focused entry PNG smoke listed above
- Sophie/Bunny/Elder route regressions

## Recommended next step

Audit the remaining P0 visual parity gap: compare original-client frames for NPC battle entry, especially sprite 294 marker state/position and exact cpos timing for enemy/player spawn.
