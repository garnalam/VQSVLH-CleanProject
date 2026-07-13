# 215 - battle KO/result/EXP flow current audit

## Scope
- Request: audit/fix real battle KO/result flow.
- Focus:
  - If active player pet dies and reserve pet is alive, open P5 petstate instead of ending battle.
  - If all player pets are dead, route to P9 lose.
  - EXP must only be awarded from P8 win after enemy-side KO.
  - Keep behavior generic, not Elder-only.

## Source anchors
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - `game.d.q()` post-damage/result consumer.
  - Related helpers around state 5/8/9 and active queue death.

## Source facts
- `game.d.q()` checks the side of the dead battle unit after damage/status resolution.
- Player-side dead unit:
  - If another player pet is alive, source enters state `5` and opens replacement selection through `petstate.ui`.
  - If no player pet remains alive, source enters state `9` lose/result.
- Enemy-side dead unit:
  - If enemy party still has replacements, source goes through replacement flow.
  - If no enemy remains, source calls `game.d.X()` and enters state `8` win.
- EXP is downstream of state `8`; player-side KO must not enter P8 and must not commit EXP.

## Rebuild status
- `VqsvBattleRuntime.handleDeadBattleUnit(...)` is the shared generic handler.
- Current KO consumers route through it:
  - P7 post-damage death.
  - P12/P13 active queue/status death.
  - P1 direct dead check paths.
- Player-side KO:
  - live reserve -> `P5_PET_SWITCH`, `forcedPetSwitch=true`, petstate menu prepared.
  - no live reserve -> `P9_LOSE`.
- Enemy-side KO:
  - enemy replacement exists -> `P15_ENEMY_REPLACEMENT`.
  - no replacement -> `P8_WIN`.
- `tickLose()` persists active pet state and exits without P8 EXP.

## Smoke evidence
- `battle_elder_switched_bunny_ko_forced_p5_no_exp`
  - Player switched to Bunny, Bunny KO, Dien Mieu alive.
  - Result: `P5`, no EXP mutation.
- `battle_elder_all_player_pets_ko_p9_no_exp`
  - Bunny KO and no alive player reserve.
  - Result: `P9/EXIT`, no P8 EXP.
- `battle_p13_queue_death_to_p5`
  - Generic active queue/status death with reserve.
  - Result: `P5`.
- `battle_p13_queue_death_to_p9`
  - Generic active queue/status death without reserve.
  - Result: `P9`.
- `battle_exp_vector_active_only_regression`
  - Enemy KO enters P8 and awards active pet EXP.
- `battle_exp_vector_p5_switch_two_participants`
  - Legitimate enemy KO after P5 switch enters P8 and awards participant EXP.
- `battle_exp_levelup_ui`
  - P8 level-up UI path remains reachable.

## Classification
- KO side routing: PORTED for current P7/P12/P13/P1 consumers.
- Player KO with reserve -> forced P5: PORTED.
- All player pets KO -> P9 lose: PORTED.
- Enemy KO -> P8 EXP path: PORTED/PARTIAL.
- EXP animation/UI: PORTED/PARTIAL; source-shaped level-up UI exists, but not claimed pixel-perfect.
- Full source `game.d.q()` status/follow-up parity: PORTED/PARTIAL. Skill 63/69 and buff12 follow-up are represented, but exact full global state is not claimed pixel-perfect.

## Current conclusion
- No new code patch was required in this audit pass; the current runtime already contains the generic fix from slice 211.
- The important regression guard is to keep the two Elder KO smoke checkpoints and two P13 queue death checkpoints in future battle changes.

## Next
- Next battle slice should audit NPC battle intro/result UI after `npcEnemy.ui`: enemy replacement `P15`, win/lose message timing, and P8 EXP bar/level-up animation polish.
