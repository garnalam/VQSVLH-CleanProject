# 127 Battle choice/petstate/msgwarm UI Runtime Closeout

Status date: 2026-07-08

Scope:
- Make the battle UI runtime fuller for `/data/ui/choice.ui`, `/data/ui/petstate.ui`, and `/data/ui/msgwarm.ui`.
- Keep the slice UI-only: no intro/world/panel route changes, no battle logic changes.

Rules applied:
- Source UI data first: original binary UI is loaded before decoded JSON fallback.
- No live client/JAR was opened; verification used headless smoke PNG only.
- No full MIDP pixel-perfect claim without original-client pixel comparison.

## Source Files Read

- `modules/ui/original/choice.ui`
- `modules/ui/decoded/data__ui__choice.ui.json`
- `modules/ui/original/petstate.ui`
- `modules/ui/decoded/data__ui__petstate.ui.json`
- `modules/ui/original/msgwarm.ui`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`
- `rebuild_game/src/main/java/VqsvUiLayout.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvTextRenderer.java`

## Implementation Closeout

| Area | Source-backed behavior | Rebuild status |
| --- | --- | --- |
| UI loader | `VqsvUiLayout.load()` reads original `.ui` binary first, then decoded JSON fallback. | `PORTED/PARTIAL` |
| Widget text | Runtime can read widget text by id and fall back only when missing. Used for source labels where safe. | `PORTED` |
| Band height | Runtime now derives visual band height from widget data or the next same-width widget before using fallback. This removes some hardcoded `choice/petstate/msgwarm` band sizes. | `PORTED/PARTIAL` |
| `choice.ui` | Frame, header, subtitle, rows, icons, selected row, desc/count area, action/back, and scrollbar use decoded/source widget ids. | `PORTED/PARTIAL` |
| `petstate.ui` | Frame, top/body/footer bands, row frames, icons, HP/EXP bars, selected detail fields, stars, action/back labels use widget ids. | `PORTED/PARTIAL` |
| `msgwarm.ui` | Frame/fills/text/prompt use widget ids 1/2/3/5/6/7/8; overflow text stays one-line marquee. | `PORTED/PARTIAL` |

## Files Changed

- `rebuild_game/src/main/java/VqsvUiLayout.java`
  - Added `bandHeight(id,fallback)`.
  - Added `text(id,fallback)`.
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
  - `choice.ui` action/back and `petstate.ui` title/action/back now use widget text fallback where safe.
  - `choice.ui` and `petstate.ui` fill bands use runtime band height helper.
- `rebuild_game/src/main/java/VqsvTextRenderer.java`
  - `msgwarm.ui` fill bands use runtime band height helper.

## Verification

Build/check:
- `build.ps1`: passed.
- `com.vqsv.rebuild.Main --check`: passed.
- `VqsvBattleDamageFormulaCheck`: passed.
- Java mojibake scan: passed.

Headless smoke PNG checkpoints:
- `battle_elder_item_p4`
- `battle_choice_ui_scroll_source_rows`
- `battle_elder_item_target_p16`
- `battle_elder_pet_p5`
- `battle_msgwarm_source_widget_warning`
- `battle_p16_item_success_msgwarm`
- `battle_p4_blocked_item_warning`
- `battle_p16_item_hp_pp_full_warning`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`
- `battle_p16_success_confirm_to_p1`
- `battle_p16_warning_return_petstate_preserve_cursor`

Smoke PNG prefix:
- `rebuild_game/build_intro_demo/ui_runtime_fuller_*.png`

## Remaining Partial / Pending

- Full generic `game.h` widget runtime is still not 100% ported. Current runtime is source-widget driven for battle call sites, not a universal Java ME UI engine.
- Exact sprite mode-3 rendering and Java ME draw behavior still require original MIDP pixel comparison.
- Some text payloads are still supplied by rebuild battle view models because the original source mutates widget text at runtime; this is correct structurally but not a full `game.h` object graph.
- `petstate.ui` relation/equipment semantics still depend on current rebuild pet payload completeness, not a fully ported `game.k` pet model.

## Next Roadmap Step

Return to Battle Phase 5 flow parity:
1. Finish P4/P16 item flow details if any behavior-table edge remains.
2. Then continue P21/P17 catch edge cases and animation/openbox/message parity.
3. Keep using smoke PNG only unless the user explicitly asks to open the client.
