# 195 Battle Phase 11-B Choice UI List Controller Closeout

Status: IMPLEMENTED / PORTED-PARTIAL.

Scope:

- Port a small source-shaped list controller for battle `choice.ui`.
- Focus on source `al.a` / `z` cursor fields:
  - `z.e` visible offset.
  - `z.f` selected index.
  - `z.a(int)` list mode used by `game.h.be()`.
- Keep P21/P4 battle logic unchanged.
- Keep verification headless: PNG smoke only, no client/JAR launch.

## Source Facts

| Source | Fact |
|---|---|
| `modules/source_code/decoded/decompiled_source_cfr/al.java` | `al.a` is a `z` list/controller object. |
| `modules/source_code/decoded/decompiled_source_cfr/z.java` | `z.e` is visible offset, `z.f` is selected index; `a(int,w)` / `b(int,w)` move down/up and update offset. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `ah()` | P21 sets `((al)choice[0]).a.f = this.b`, list mode `a(0)`, size `a.a = q.K.size()`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `be()` | P4 sets list mode `a(1)` when `q.J.size() > 5`, then reads `e/f`, clamps selected, and applies `if (w > 0 && h - w < 4) --w`. |

## Code Changes

| File | Change |
|---|---|
| `rebuild_game/src/main/java/VqsvChoiceUiView.java` | Added source-shaped list controller fields/methods: `sourceListMode`, `visibleRows`, `moveUpSource()`, `moveDownSource()`, `withSourceCursor()`, `scrollbarThumbY()`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | `choice` menu input now uses `moveUpSource()` / `moveDownSource()`; legacy `battleMenuIndex/Scroll` are mirrored from the view. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | Renderer reconciles direct legacy cursor changes through `withSourceCursor()` and uses `scrollbarThumbY()`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_choice_ui_source_be_offset_up`. |

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
| `battle_choice_ui_source_be_offset_up` | `build/smoke/phase11/battle_choice_ui_source_be_offset_up.png` | PASS |
| `battle_choice_ui_scroll_source_rows` | `build/smoke/phase11/battle_choice_ui_scroll_source_rows.png` | PASS |
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
| P21/P4 `choice.ui` data model | PORTED-PARTIAL |
| P21/P4 cursor/scroll `e/f` parity | PORTED-PARTIAL |
| P4 `be()` upward offset rule | PORTED-PARTIAL, smoke-covered |
| Scrollbar thumb based on selected `f` | PORTED-PARTIAL |
| Generic source `z`/`al` interpreter | PENDING |
| Petsetting alternate softkeys `59/60` | PENDING |
| Pixel-perfect MIDP comparison | PENDING |

## Next Roadmap Step

Phase 11-C should stay in battle UI widget runtime parity:

- audit/port `choice.ui` widget mutation visibility more generically:
  `p.a.a(id).h().a`, `a(false/true)`, row icon lifecycle `m.a(258,...)`;
- keep P21/P4 logic unchanged;
- do not broaden into `petstate.ui` or `msgwarm.ui` unless the source transition
  requires a boundary check.
