# 196 Battle Phase 11-C Choice UI Widget Mutation Closeout

Status: IMPLEMENTED / PORTED-PARTIAL.

Scope:

- Move battle `choice.ui` renderer closer to source widget mutation behavior.
- Focus only on P21/P4 `choice.ui`.
- Do not change P21/P4 battle logic.
- Do not touch `petstate.ui`, `msgwarm.ui`, `openbox.ui`, or battle command UI.
- Verification is headless PNG smoke only; no client/JAR launch.

## Source Facts

| Source | Fact |
|---|---|
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `ah()` | P21 opens `/data/ui/choice.ui`, sets widget text `8`, `9`, `5`, creates row icon `m` with sprite `258`, mode `2`, and hides widgets `59/60`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `bn()` | P21 writes selected count text into widget `53`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `aj()` | P4 opens `/data/ui/choice.ui`, sets widget text `8`, `9`, `5`, hides widgets `59/60`, then calls `be()`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `be()` | P4 writes row icon/name/value widgets and clears row icons/texts when a visible row has no item. |

## Code Changes

| File | Change |
|---|---|
| `rebuild_game/src/main/java/VqsvChoiceUiView.java` | Added widget mutation API: `widgetVisible(id)`, `widgetText(id, fallback)`, row icon lifecycle helpers, description visibility, and source constants `ROW_ICON_SPRITE_ID=258`, `ROW_ICON_MODE=2`. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | `drawChoiceOverlay()` now uses `VqsvChoiceUiView` widget mutation APIs for title/subtitle/action/back, row text/value, row icon visibility/cell, description box, and alternate softkeys. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_choice_ui_widget_mutation_runtime`. |

## Verification

Preflight:

```text
powershell -ExecutionPolicy Bypass -File ./build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
powershell -NoProfile -Command "$pattern = [string]::Join('|', @([char]0x00C3,[char]0x00C2,[char]0x00C6,[char]0x00D0,[char]0x00F0,[char]0x25A1,[char]0xFFFD,'mojibake')); rg -n $pattern src/main/java"
```

Result:

| Check | Result |
|---|---|
| Build | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java source mojibake scan | PASS, no matches |

Focused smoke PNG:

| Checkpoint | Output | Result |
|---|---|---|
| `battle_choice_ui_widget_mutation_runtime` | `build/smoke/phase11/battle_choice_ui_widget_mutation_runtime.png` | PASS |
| `battle_choice_ui_scroll_source_rows` | `build/smoke/phase11/battle_choice_ui_scroll_source_rows.png` | PASS |
| `battle_choice_ui_source_be_offset_up` | `build/smoke/phase11/battle_choice_ui_source_be_offset_up.png` | PASS |
| `battle_bunny_catch_p21` | `build/smoke/phase11/battle_bunny_catch_p21.png` | PASS |
| `battle_elder_item_p4` | `build/smoke/phase11/battle_elder_item_p4.png` | PASS |
| `battle_catch_missing_count_warning_return_p21` | `build/smoke/phase11/battle_catch_missing_count_warning_return_p21.png` | PASS |

Regression:

```text
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build/smoke/suites/battle_quick
```

Result:

```text
smoke-suite-ok battle_quick checkpoints=14
```

## Current Status

| Area | Status |
|---|---|
| P21/P4 widget text mutation | PORTED-PARTIAL |
| P21/P4 widget visibility for `5/6/59/60/52/53` | PORTED-PARTIAL |
| Row icon lifecycle sprite `258`, mode `2` | PORTED-PARTIAL |
| P4 clear empty visible row icon/text | PORTED-PARTIAL, smoke-covered |
| Generic `game.h` widget tree runtime | PENDING |
| Generic `m` sprite object runtime for UI widgets | PENDING |
| Petsetting alternate softkey flow with `59/60` visible | PENDING |
| Pixel-perfect MIDP comparison | PENDING |

## Next Roadmap Step

Phase 11-D should either:

1. audit and port `choice.ui` petsetting alternate softkey mode `59/60` as a
   source-backed UI runtime slice, without changing battle logic; or
2. close battle-facing `choice.ui` for now and move to the next battle UI
   target, likely `msgwarm.ui` or `openbox.ui`, because P21/P4 battle behavior
   now has data model, cursor/scroll, and widget mutation coverage.
