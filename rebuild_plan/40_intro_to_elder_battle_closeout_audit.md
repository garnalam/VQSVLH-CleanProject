# Intro To Elder Battle Closeout Audit

Date: 2026-07-03

Scope: rebuild runtime from boot/new-game intro through `scene_1 room0 group6`
elder battle completion.

This audit closes the current implementation state honestly. It does not claim
pixel-perfect parity for subsystems that are still manual/partial.

## Source References

- `modules/event/decoded/data__event__scene_0.mid.json`
- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- Prior audits `22`, `28`, `35`, `36`, `37`, `39`.

## New Closeout Fixes

### Text Encoding Runtime Fix

Status: PORTED/APPROX.

`VqsvIntroDemo.TextBox` now decodes UTF-8 mojibake literals at the renderer
entry point. Existing `\uXXXX` strings remain unchanged.

This fixes visible strings such as:

- `SÃ¡u nÄƒm sau` -> `Sáu năm sau`
- `MÆ°á»i nÄƒm sau` -> `Mười năm sau`
- `TrÆ°á»Ÿng thÃ´n` -> `Trưởng thôn`
- group2/group3 task/dialog strings.

The bitmap font renderer now normalizes Vietnamese diacritics to ASCII base
glyphs before falling back to any exact non-ASCII glyph. This avoids corrupted
font shapes from old mojibake paths.

Update: dialog/text rendering now uses a Unicode-capable Java2D display font
for visible glyphs while keeping `font.bin` loaded for source audit/checks.
The current temporary choice is `Tahoma` size 9 with antialiasing disabled,
because it is closer to small MIDP text than the earlier Java `Dialog` font.
This fixes the visible `Bị bắt` corruption that previously read like
`Ba/Bá bắt`, while avoiding the visibly wrong larger desktop font.

Honesty note: this is a readability fix, not pixel-perfect MIDP text. Exact
font/glyph parity remains APPROX/PENDING.

### Build Encoding Cleanup

Status: PORTED.

Removed the UTF-8 BOM from `VqsvIntroDemo.java` and removed invalid old
mojibake `char` cases in `FontBitmap.glyphChar()`. The project now builds
cleanly with the existing `javac -encoding UTF-8` path.

## Phase Status

| Phase | Current Status | Evidence / Notes |
|---|---|---|
| Boot logos / panel / new game entry | PORTED/APPROX | Prior user-approved behavior retained. No closeout edit changed boot logic/timing. |
| Scene 0 legacy intro | PORTED/APPROX | Existing manual scene remains intact. Text now passes through mojibake decoder, but scene logic/timing was not altered. |
| Scene 1 room3 entry cutscene | PORTED/APPROX + STUB battle | Manual records exist for group0. Dialog text now decodes. Actor movement/effects remain approximate; mid-cutscene battle remains stub. |
| Scene 1 room0 group0 tutorial intro | PORTED/APPROX | Source record order and side-effect slices exist. Text decode fixed. Timed player movement is source-directed but not full `game.g/game.k`. |
| Room1 Bunny path | PORTED/APPROX + STUB battle | op13 trigger, Bunny capture success, op23/op14 state writes, return path exist. Bunny battle/capture is not real `game.d`. |
| Return room0 group2 elder interaction | PORTED/APPROX | op86 gate and op16 actor52 interaction are source-backed. Actor52 sprite resource is ready. Text now decodes. |
| Room0 group3 pet selection | PORTED/APPROX | Pet actors/resources visible, op38 interaction, op35 option.ui choice, op87 minimal pet grant, op41/op3/op9/op14 implemented. Full `game.g` pet inventory remains partial. |
| Room0 group6 elder battle/reward | PORTED/APPROX + STUB battle | Source battle setup/reward path represented. `state[1,0,6]=3`, `sourcePets=1`, `money=500` verified. Real turn engine is pending. |
| Post-group6 free-world | PORTED/APPROX | Room0 free-world loop and room2 transition slice exist. Room0 building doors/Dodo side quest remain later content, outside this closeout scope. |

## Smoke Results

Commands run from `rebuild_game` with `-Dvqsv.modules=..\modules`.

Build/check:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check
```

Result: PASS.

Generated closeout screenshots:

- `build_intro_demo/final_audit_bi_bat.png`
  - checkpoint-only font proof for visible `Bị bắt`.
- `build_intro_demo/final_audit_long_dialog.png`
  - checkpoint-only long dialog font proof.
- `build_intro_demo/final_audit_tasktip.png`
  - checkpoint-only taskTip text proof.
- `build_intro_demo/final_audit_full_cutscene.png`
  - checkpoint-only full cutscene text proof.
- `build_intro_demo/closeout_room0_group2_first_dialog_after_fontfix.png`
  - route reaches actor52 interaction and first group2 dialog.
  - trace text: `#FFFFFF#000000Bị bắt`.
- Superseded font proof:
  `build_intro_demo/closeout_room0_group2_first_dialog_unicode_checkpoint.png`
  renders the same checkpoint with visible text `Bị bắt`.
  This checkpoint is for font verification only; it is not used as gameplay
  route proof.
- `build_intro_demo/closeout_room0_pet_choice_ui_confirmed.png`
  - verifies pet actors visible and `option.ui` choice rendered.
- `build_intro_demo/closeout_elder_done_after_fontfix.png`
  - verifies final state after elder battle/reward:
    `state103=3`, `state106=3`, `sourcePets=1`, `money=500`.

Important route correction after text/font fix:

- Old fixed-tick smoke routes such as preload `5920`/`6500` are no longer
  reliable route proof after font timing/width changes.
- Use checkpoint images for font verification only.
- For gameplay proof, rerun route smoke with freshly calibrated movement/timing
  or with a source-state/debug checkpoint that is explicitly labeled as such.

Verified final route:

```powershell
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\closeout_elder_done_after_fontfix.png" 6500 "R90,U20,L330,D15,L20,L30,U20,0,T120,0,T120,0,T120,0,T120,0,T120,0,T120,0,T120,0,T180,U12,0,T180,0,T1600" 0
```

## Not Yet Complete

These are still not allowed to be called finished:

| Area | Status | Why |
|---|---|---|
| Real `game.d` battle engine | STUB/PENDING | Bunny and elder battle preserve source setup/branch/reward but do not run command UI, turn order, AI, damage, status, EXP/result flow. |
| Generic `game.c` event VM | APPROX/PENDING | Current flow is manual source-backed scripting, not a general opcode runner. |
| Actor movement/action engine | APPROX | `op10` and `op7` are source-directed but do not fully model original actor state/path/collision completion. |
| Opcode 9 effect renderer | APPROX/PENDING | Handled ids used here are represented; full root `b.a()` effect semantics and pixel timing are not ported. |
| Full UI stack `game.h/ao/al` | APPROX/PENDING | `option.ui`, `taskTip.ui`, `openbox.ui` are used directly where needed, but not the full source widget runtime. |
| MIDP bitmap font pixel parity | APPROX/PENDING | Encoding and readable Unicode rendering are fixed for current smoke, including `Bị bắt`. Current visible font is `Tahoma 9` no-AA, chosen as a closer MIDP-like temporary font. This is not pixel-perfect `s.java`/MIDP `Graphics.drawString` parity; original font behavior still needs a separate audit. |
| Full `game.g` inventory/pet model | APPROX/PENDING | Minimal item/currency/pet side effects exist for this story slice only. |

## Closeout Decision

The playable/manual flow from intro to elder battle reward is currently
source-backed and smoke-passing at the story/state level.

It is not 100% original-engine complete. The largest remaining blocker is the
real `game.d/game.b/game.g/game.h` battle subsystem. If the next requirement is
"no stub at all", the next slice must be battle engine porting, not more story
event scripting.
