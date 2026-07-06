# 55 Battle P6 Target Select Matrix

Status: PORTED/PARTIAL as of P6 target-select slice.

## Source Facts

- `game.h.f(b)` confirms a skill from `choiceskill.ui`.
- After confirm it calls `game.d.b(v1.z[this.e])` to build `h.G/H` target vectors.
- If `game.d.a == 0`, source calls `game.d.i()` immediately. This is 1v1/auto-target behavior.
- If `game.d.a != 0`, source switches to `game.d` state 6.
- `game.d.b(byte skillId)` reads `aq.c[1][skillId][9]`:
  - `1`: collect living units on same side as attacker.
  - `0`: collect living units on the opposite side.
- `game.d case 6` handles directional input to move target index `C`, confirm calls `game.d.i()`, back returns to state 3.
- `game.d.i()` sets selected target `h.p`, target slot `h.I`, selected skill, hides the selector, then proceeds into execution.

## Rebuild Port

- `BattleRuntimeState.P6_TARGET_SELECT`: added.
- `SourceBattleRuntime.prepareTargetList()`: source-shaped target vector from `BattleSkillRow.targetSide`.
- `SourceBattleRuntime.tickTargetSelect()`: direction/click/confirm/back handling.
- `SourceBattleRuntime.commitSelectedTarget()`: sets selected target and consumes PP via `BattleUnit.selectSkill()`.
- `VqsvBattleRenderer.drawTargetCursor()`: draws one source UI hand cursor over selected target.

## Smoke

- `battle_elder_p6_target_select.png`: reaches P6 and shows one target cursor.
- `battle_elder_p6_confirm_to_p7.png`: confirms target and reaches P7 before real battle animation.
- Regression route smoke:
  - Sophie battle -> branch 78.
  - Bunny battle -> result -1 and return task.
  - Elder battle -> reward state.

## Still Partial

- Current battle smoke has one player pet and one enemy, so P6 multi-target cycling is source-shaped but not visually proven with multiple battle units.
- P7 animation/effect is still pending; P6 now hands selected target to the existing resolve path.
- The target cursor uses the source hand cell already used by command bar, but exact `pos.mid/cpos.mid` cursor coordinates for every formation are not fully ported yet.
