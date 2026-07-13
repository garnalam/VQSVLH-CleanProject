# 211 - Generic battle KO result flow: side0 P5/P9, side1 P15/P8

Date: 2026-07-10

## Scope

Bug report: in Elder battle, after switching from Dien Mieu to Bunny, if Elder KOs Bunny, rebuild ended the battle and awarded EXP/level-up to Dien Mieu and Bunny.

Follow-up requirement: this must be handled for all battle KO paths, not only Elder.

## Source audit

- Source anchor: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`, `game.d.q()`.
- Source side handling:
  - Enemy-side KO (`r() == 1`) can advance enemy replacement / win flow.
  - Player-side KO (`r() == 0`) checks whether the player has another alive pet.
  - If another player pet is alive, source enters state 5 (`petstate.ui`) for forced replacement.
  - Only when no player pet remains should the battle go to lose/result handling.
- EXP is downstream of state 8 win. Therefore a player-side KO with live reserves must not enter state 8 and must not commit EXP.
- The same side-based rule appears in nearby source helpers:
  - `game.d.b(b, boolean)` routes no alive player pet to state 9, live reserve to state 5.
  - `game.d.g(b)` returns side0 dead with reserve as 1, side1 dead with enemy reserve as 2.

## Rebuild issue

`VqsvBattleRuntime.finishP7()` used `currentActorPlayer ? P8_WIN : P9_LOSE` when `p7Target` died.

In Elder battle, enemy KO of the player pet entered `P9_LOSE`; `tickLose()` has Elder-specific route handling that turns Elder lose into `P8_WIN`, causing false EXP/level-up.

## Fix

- Added shared `VqsvBattleRuntime.handleDeadBattleUnit(...)`.
- All current rebuild KO consumers now use the same side-based handler:
  - `P1` dispatch direct dead check.
  - `P12/P13` active queue/status death.
  - `P7` post-damage death.
- Generic runtime behavior:
  - side0/player pet KO:
    - live reserve exists -> `P5_PET_SWITCH`, forced pet menu.
    - no live reserve -> `P9_LOSE`.
  - side1/enemy pet KO:
    - enemy replacement exists -> `P15_ENEMY_REPLACEMENT`.
    - no enemy replacement -> `P8_WIN`.
- `VqsvBattleRuntime.tickLose()` no longer special-cases Elder lose into `P8_WIN`.
  Source state 9 is the lose path and does not commit win EXP.
- Added trace:
  - `PORTED battle generic KO side0 -> P5`
  - `PORTED battle generic KO side0 -> P9`
  - `PORTED battle generic KO side1 -> P15`
  - `PORTED battle generic KO side1 -> P8`

## Smoke

New focused checkpoint:

- `battle_elder_switched_bunny_ko_forced_p5_no_exp`
- `battle_elder_all_player_pets_ko_p9_no_exp`
- `battle_p13_queue_death_to_p5`
- `battle_p13_queue_death_to_p9`

They verify:

- Elder battle starts with Dien Mieu + Bunny.
- Player switches to Bunny.
- If Dien Mieu remains alive and Elder KOs Bunny, runtime enters `P5` / `petstate.ui`.
- If Dien Mieu is also dead and Elder KOs Bunny, runtime enters `P9` / `EXIT`.
- Bunny HP persists as 0.
- Battle does not enter P8/levelup.
- Dien Mieu EXP and Bunny EXP do not change.
- Generic P13 active-queue/status death uses the same handler and reaches P5/P9 according to reserve availability.

## Verification

Passed:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Mojibake/SMS scan: no matches
- `battle_elder_switched_bunny_ko_forced_p5_no_exp`
- `battle_elder_all_player_pets_ko_p9_no_exp`
- `battle_p13_queue_death_to_p5`
- `battle_p13_queue_death_to_p9`
- `battle_p5_forced_menu_visibility`
- `battle_p5_forced_replacement_success`
- `battle_p5_forced_dead_warning`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Status

- KO branch: PORTED for this source-proven P7 player-side KO slice.
- Elder false EXP after switched pet KO: fixed.
- EXP animation on real enemy KO: still separate visual/runtime polish if user sees it missing on legitimate wins.

## Recommended next step

Audit/verify legitimate enemy-KO EXP animation path (`P8` -> `game.h.am/an/ao/aq`, `levelUp.ui`) with a dedicated smoke that kills Elder pet and captures the EXP bar/level-up frames.
