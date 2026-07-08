# 125 Battle P4/P16-A Flow Closeout

Status: PORTED/PARTIAL.

Scope:

- Close P16 item success/warning/back flow.
- Do not change `game.b.x(item)` validation formula.
- Do not change `game.b.w(item)` item apply formula.
- Do not touch intro/world/panel/catch/pet-switch logic.

## Source Facts Used

| Source | Fact |
| --- | --- |
| `game.d case 4` | P4 entry/update calls `S.aj()` / `S.ak()`. |
| `game.d case 16` | P16 entry sets `S.c = 0`, `S.l = false`, then calls `S.W()`; update calls `S.al()`. |
| `game.h.aj()/ak()` | P4 uses `/data/ui/choice.ui`; behavior `7..10` opens `msgwarm.ui` warning and does not enter P16. |
| `game.h.W()/e(c)` | P16 uses `/data/ui/petstate.ui`; battle rows follow `game.d.f[]` order. |
| `game.h.bo()` | Successful item use refreshes petstate, sets success state, and opens `msgwarm.ui`: `Thành công sử dụng đạo cụ`. It does not visually jump straight to P1. |
| `game.h.al()` | Back before success returns from P16 to P4. |

## Rebuild Changes

| Area | Change | Status |
| --- | --- | --- |
| `VqsvBattleRuntime.tickItemTarget()` | Added `keyBack` handling for P16 -> P4. | PORTED |
| `VqsvBattleRuntime.tickItemTarget()` | After valid apply, refresh P16 petstate, show `msgwarm.ui` success, defer turn transition. | PORTED/PARTIAL |
| `VqsvBattleRuntime.tickWarning()` | Added return path from success msgwarm to `P1_DISPATCH`. | PORTED/PARTIAL |
| `VqsvSmokeHarness` | Added P4/P16-A checkpoints for success, warning, back, blocked item, and HP+PP warning. | PORTED |

## Smoke Checkpoints

All passed:

- `battle_p16_item_success_msgwarm`
- `battle_p16_success_confirm_to_p1`
- `battle_p16_warning_return_petstate_preserve_cursor`
- `battle_p16_back_returns_p4`
- `battle_p4_blocked_item_warning`
- `battle_p16_item_hp_pp_full_warning`

PNG outputs:

- `rebuild_game/build_intro_demo/battle_p16_item_success_msgwarm.png`
- `rebuild_game/build_intro_demo/battle_p16_success_confirm_to_p1.png`
- `rebuild_game/build_intro_demo/battle_p16_warning_return_petstate_preserve_cursor.png`
- `rebuild_game/build_intro_demo/battle_p16_back_returns_p4.png`
- `rebuild_game/build_intro_demo/battle_p4_blocked_item_warning.png`
- `rebuild_game/build_intro_demo/battle_p16_item_hp_pp_full_warning.png`

## Regression

Passed:

- `build.ps1`
- Java mojibake scan
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`
- `battle_exp_p8_confirm_fast_forward`
- `battle_exp_levelup_ui`
- `battle_exp_levelup_choiceskill_ui`
- `battle_exp_levelup_learn_skill_done`
- `battle_exp_consumer_x_clears_active_marker`
- `battle_exp_consumer_x_passive_heal`

## Remaining P4/P16 Gaps

| Gap | Status |
| --- | --- |
| Full binary `/data/ui/choice.ui`, `/data/ui/petstate.ui`, `/data/ui/msgwarm.ui` widget runtime | PARTIAL |
| `game.g.o().d(item,1,0)` global inventory ownership parity | PARTIAL; rebuild consumes once in runtime |
| Behavior 6 real source item smoke | PENDING until source item/use case is selected |
| Success route nuance when item is used on non-active battle pet | PARTIAL; current rebuild routes success confirm to P1 like active battle use |

## Next Roadmap Step

Continue Phase 5 item/pet/catch closeout with focused P16 item parity:

1. Audit source inventory ownership around `game.g.o().d(item,1,0)` vs `q.b(item,1,0)`.
2. Then either close behavior 6 item smoke if a real row/use case exists, or move to full `choice.ui/petstate.ui/msgwarm.ui` widget runtime parity.
