# 194 Battle Phase 11-A Choice UI Runtime Core Closeout

Status: IMPLEMENTED / PORTED-PARTIAL.

Scope:

- Add a dedicated `choice.ui` runtime view for battle P21/P4 menu data.
- Keep battle logic unchanged.
- Keep legacy `Scene.battleMenu*` fields mirrored for existing smoke/asserts.
- Keep verification headless: PNG smoke only, no client/JAR launch.

## Code Changes

| File | Change |
|---|---|
| `rebuild_game/src/main/java/VqsvChoiceUiView.java` | New runtime model for `choice.ui`: title, subtitle, action/back labels, row names/values/descriptions, ids/icons, cursor/scroll, visibility flags. |
| `rebuild_game/src/main/java/VqsvIntroDemo.java` | Added `Scene.battleChoiceUi`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | `setMenu()` now populates `VqsvChoiceUiView`; `syncMenuScroll()` keeps cursor/scroll mirrored. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | `drawChoiceOverlay()` renders from `VqsvChoiceUiView`, with compatibility fallback to legacy scene fields. |

## Source Boundary Preserved

| Flow | Source UI | Runtime status |
|---|---|---|
| P21 catch list | `game.h.ah/ai` + `/data/ui/choice.ui` | PORTED-PARTIAL through `VqsvChoiceUiView`. |
| P4 item list | `game.h.aj/ak/be` + `/data/ui/choice.ui` | PORTED-PARTIAL through `VqsvChoiceUiView`. |
| P16 item target | `/data/ui/petstate.ui` | Not changed. |
| P5 pet switch | `/data/ui/petstate.ui` | Not changed. |
| P3 skill list | `/data/ui/choiceskill.ui` | Not changed. |

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
| `battle_bunny_catch_p21` | `build/smoke/phase11/battle_bunny_catch_p21.png` | PASS |
| `battle_choice_ui_scroll_source_rows` | `build/smoke/phase11/battle_choice_ui_scroll_source_rows.png` | PASS |
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

## Remaining Phase 11 Gaps

PENDING / NOT CLAIMED:

- Full source `al` list object parity.
- Full generic widget tree mutation API equivalent to `p.a.a(id).h().a`,
  `a(false/true)`, and widget move calls.
- Generic `choice.ui` support for petsetting alternate softkeys `59/60`.
- Pixel comparison against MIDP original.

## Next Roadmap Step

Phase 11-B should stay UI-runtime focused:

- port `choice.ui` list-controller parity for source `al` cursor/scroll behavior,
  especially `be()` style list offset `e/f` and scrollbar thumb positioning;
- keep P21/P4 battle logic unchanged;
- rerun focused `choice.ui` smoke plus `battle_quick`.
