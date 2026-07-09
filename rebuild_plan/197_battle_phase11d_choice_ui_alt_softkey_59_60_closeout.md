# 197 Battle Phase 11-D Choice UI Alternate Softkey 59/60 Closeout

Status: IMPLEMENTED / PORTED-PARTIAL.

Scope:

- Audit source `choice.ui` petsetting alternate softkey mode.
- Port runtime support for widgets `59/60`.
- Keep battle P21/P4 logic unchanged.
- Do not port full petsetting gameplay.
- Do not touch `petstate.ui`, `msgwarm.ui`, or `openbox.ui`.
- Verification is headless PNG smoke only; no client/JAR launch.

## Source Facts

| Source | Fact |
|---|---|
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` petsetting case `0` | Opens `/data/ui/choice.ui` with `petsetting.ui` and `petstate.ui`; if `o instanceof k`, hides widgets `5/6`, shows `59/60`, and writes action text to `59`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` petsetting case `2` | Same alternate softkey mode for jewelry list, with action text `Mang theo`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `bd()` | Jewelry list can update action text on widget `59` to source meanings `Do xuong` or `Mang theo`; widget `60` keeps back behavior. |
| `modules/ui/decoded/data__ui__choice.ui.json` | Widget `59` is at `x=1,y=296,w=43`; widget `60` is at `x=197,y=296,w=43`; both use alt image `28/mode2`. |

## Code Changes

| File | Change |
|---|---|
| `rebuild_game/src/main/java/VqsvChoiceUiView.java` | Added `withAlternateSoftkeys(String)` to model source `5/6` hidden and `59/60` visible. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | Renders source cell frame plus text for visible widgets `59/60`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_choice_ui_alt_softkey_59_60`. |

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
| `battle_choice_ui_alt_softkey_59_60` | `build/smoke/phase11/battle_choice_ui_alt_softkey_59_60.png` | PASS |
| `battle_choice_ui_widget_mutation_runtime` | `build/smoke/phase11/battle_choice_ui_widget_mutation_runtime.png` | PASS |
| `battle_choice_ui_scroll_source_rows` | `build/smoke/phase11/battle_choice_ui_scroll_source_rows.png` | PASS |
| `battle_choice_ui_source_be_offset_up` | `build/smoke/phase11/battle_choice_ui_source_be_offset_up.png` | PASS |
| `battle_bunny_catch_p21` | `build/smoke/phase11/battle_bunny_catch_p21.png` | PASS |
| `battle_elder_item_p4` | `build/smoke/phase11/battle_elder_item_p4.png` | PASS |

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
| Source `5/6` hidden, `59/60` visible mode | PORTED-PARTIAL |
| Widget `59/60` frame rendering via decoded `choice.ui` alt image `28/mode2` | PORTED-PARTIAL |
| Widget `59` action text runtime | PORTED-PARTIAL |
| Widget `60` back text runtime | PORTED-PARTIAL |
| Full petsetting flow using real `game.k` runtime | PENDING |
| Jewelry `bd()` data from `aq.c[3]` | PENDING |
| MIDP pixel compare | PENDING |

## Next Roadmap Step

Battle-facing `choice.ui` is now covered by:

- data model;
- source-shaped list controller;
- widget mutation/runtime;
- alternate softkey `59/60` support.

Next Phase 11 step should move to another battle UI widget target:

1. `msgwarm.ui` warning runtime parity; or
2. `openbox.ui` catch success runtime parity.

Prefer `msgwarm.ui` first because P21/P4/P16 warnings all depend on it.
