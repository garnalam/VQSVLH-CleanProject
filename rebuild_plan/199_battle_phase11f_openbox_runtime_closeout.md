# 199 Battle Phase 11F Openbox Runtime Closeout

## Scope

Phase 11F ports the battle catch-success `openbox.ui` runtime metadata without
changing catch logic, storage logic, intro/world scripts, or the battle command
state machine.

## Source Facts

| Source | Fact | Status |
| --- | --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | `b(String)` opens `/data/ui/openbox.ui` with sprite bank `257`, then calls the shared text writer. | PORTED |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | Private `e(String)` stores text, creates widget `1` sprite if missing, sets sprite `257`, animation state `9`, and resets text counter. | PORTED/PARTIAL |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | `aw()` closes `/data/ui/openbox.ui`; `ax()` reports the openbox is closed. | PORTED/PARTIAL |
| `modules/ui/decoded/data__ui__openbox.ui.json` | Widget `1`: frame visual at `x=45,y=147,w=150,h=-1,b=4`. | PORTED |
| `modules/ui/decoded/data__ui__openbox.ui.json` | Widget `2`: text visual at `x=47,y=154,w=146,h=-1,b=4`. | PORTED |

## Code Changes

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvOpenBoxView.java` | Added a small source-runtime view for `openbox.ui` widget ids, sprite bank `257`, animation state `9`, and message text. | PORTED |
| `rebuild_game/src/main/java/VqsvIntroDemo.java` | Added `Scene.battleOpenBox`. | PORTED |
| `rebuild_game/src/main/java/VqsvTextRenderer.java` | `TextBox.openBox(...)` and openbox render path now read widget `1`/`2` geometry from `openbox.ui` layout. | PORTED |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | P17 catch success, bank notice, and openbox close now populate/clear `Scene.battleOpenBox`. | PORTED |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | `battle_openbox_source_widget_catch_success` and generic catch storage assertions now verify `VqsvOpenBoxView` plus rendered frame/text pixels. | PORTED |

## Smoke

| Check | Result |
| --- | --- |
| `battle_openbox_source_widget_catch_success` | PASS, PNG: `rebuild_game/build/smoke/phase11/battle_openbox_source_widget_catch_success.png` |
| `battle_quick` | PASS, 14/14 checkpoints |

## Regression

| Command | Result |
| --- | --- |
| `powershell -ExecutionPolicy Bypass -File ./build.ps1` | PASS |
| `java -cp build/classes com.vqsv.rebuild.Main --check` | PASS |
| `java -cp build/classes VqsvBattleDamageFormulaCheck` | PASS |
| Java source encoding scan | PASS, no matches |

## Remaining

| Area | Status | Note |
| --- | --- | --- |
| Generic `game.h` UI framework for every `openbox.ui` caller | PARTIAL | Battle P17 catch success path is source-shaped; broader widget manager parity is still outside this slice. |
| Pixel compare against MIDP original | PENDING | Smoke uses source widget geometry and pixel assertions, not a MIDP capture diff. |
| Exact text counter `v` behavior inside `game.h.f()` | PARTIAL | Rebuild gates openbox text by sprite cursor readiness and source-shaped marquee timing. |

## Next Recommended Step

Phase 11 should continue with `petstate.ui` or remaining shared UI runtime
parity only if the next battle flow needs it. If staying on catch, the next
small slice is P17 animation/storage closeout; if moving by roadmap, Phase 12
should audit battle animation/effect gaps that remain outside the UI widgets.
