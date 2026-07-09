# 198 Battle Phase 11-E MsgWarm Runtime Closeout

Status: IMPLEMENTED / PORTED-PARTIAL.

Scope:

- Port a small runtime view for `/data/ui/msgwarm.ui`.
- Keep P21/P4/P16 warning logic unchanged.
- Keep existing `TextBox.msgWarm()` rendering path.
- Add runtime parity coverage for source widgets `6` and `7`.
- Verification is headless PNG smoke only; no client/JAR launch.

## Source Facts

| Source | Fact |
|---|---|
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `E()` | Opens `/data/ui/msgwarm.ui` with sprite/ui bank `257`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `F()` | Closes `/data/ui/msgwarm.ui`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `a(String,String)` | Writes prompt to widget `6` and message to widget `7`. |
| `modules/ui/decoded/data__ui__msgwarm.ui.json` | Widget `7` is message at `x=85,y=119,w=70`; widget `6` is prompt at `x=89,y=170,w=60`; frame widget `8` uses cell `128/mode2`. |

## Code Changes

| File | Change |
|---|---|
| `rebuild_game/src/main/java/VqsvMsgWarmView.java` | New runtime model for source widget ids `6/7/8`, message, prompt, and frame cell. |
| `rebuild_game/src/main/java/VqsvIntroDemo.java` | Added `Scene.battleMsgWarm`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | `enterWarning()` and level-up learn-skill warning now populate `battleMsgWarm`. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | Fallback battle warning renderer now uses `VqsvMsgWarmView` and decoded `msgwarm.ui` layout. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | `battle_msgwarm_source_widget_warning` now asserts `VqsvMsgWarmView` message/prompt parity. |

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
| `battle_msgwarm_source_widget_warning` | `build/smoke/phase11/battle_msgwarm_source_widget_warning.png` | PASS |
| `battle_p4_blocked_item_warning` | `build/smoke/phase11/battle_p4_blocked_item_warning.png` | PASS |
| `battle_p16_item_hp_full_warning` | `build/smoke/phase11/battle_p16_item_hp_full_warning.png` | PASS |
| `battle_p16_item_success_msgwarm` | `build/smoke/phase11/battle_p16_item_success_msgwarm.png` | PASS |

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
| Source widget `7` message runtime | PORTED-PARTIAL |
| Source widget `6` prompt runtime | PORTED-PARTIAL |
| Decoded layout-backed frame/text positions | PORTED-PARTIAL |
| P21/P4/P16 warning return logic | Existing PORTED-PARTIAL, regression-covered |
| Full generic `game.h` widget tree runtime | PENDING |
| Pixel-perfect MIDP comparison | PENDING |

## Next Roadmap Step

Phase 11 can now move to `openbox.ui` catch success runtime parity, because:

- P21 catch warning uses `msgwarm.ui`;
- P4 blocked item warning uses `msgwarm.ui`;
- P16 item warning/success uses `msgwarm.ui`;
- all focused warning checkpoints and `battle_quick` pass.
