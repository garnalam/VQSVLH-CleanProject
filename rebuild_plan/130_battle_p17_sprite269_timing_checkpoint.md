# 130 Battle P17 Sprite 269 Timing Checkpoint

Status date: 2026-07-08

Status: SOURCE-BACKED SMOKE CHECKPOINT ADDED.

Scope:

- Tighten P17 catch animation parity around sprite `269`.
- No live client/JAR.
- No battle runtime behavior change.
- No intro/world/panel changes.

## Source Chain

Source files/resources:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/d.java`
- `modules/script/original/sprite.mid`
- `modules/script/decoded/data__script__sprite.mid.json`
- `modules/spr/original/spr_269_all(r)`
- `modules/img/original/img_309.mid`
- `modules/img/decoded/data__img__img_309.mid.png`

Source facts:

| Source | Fact | Status |
| --- | --- | --- |
| `sprite.mid` row `269` | Row is `[269, 309]`: sprite metadata `spr_269_all(r)` uses image `img_309`. | `PORTED` |
| `game.d case 17` enter | Creates/uses `new f()`, loads sprite `269`, positions it at enemy, calls `e((byte)0)`. | `PORTED/PARTIAL` |
| `f.b()` | Delegates to internal `d.e()`, which returns true when current frame cursor is at the last frame. | `PORTED/PARTIAL` |
| source `d.d()` | Ticks frame delay; if next-state mode is `-2`, holds the last frame after completion. | `PORTED/PARTIAL` |
| rebuild `SpriteAnim.tick()` | Uses `delay = frameDelay - 1`; this matches source `d.c()/d.d()` for `spr_269` delay values. | `PORTED` |
| rebuild `catchAnimAtLastFrame()` | Checks `cursor >= frameCount - 1`, matching source `d.e()` end gate. | `PORTED/PARTIAL` |

## `spr_269` Animation Matrix

Parsed directly from `modules/spr/original/spr_269_all(r)`.

| P17 phase | Source anim state | Frame pairs `[delay, cell]` | Frame count | Source tick total |
| --- | ---: | --- | ---: | ---: |
| q0 throw | `0` | `[1,1] [1,2] [1,3] [1,4] [1,5] [1,6] [1,7] [1,8]` | 8 | 8 |
| q1 closed ball / capture start | `1` | `[1,9]` | 1 | 1 |
| q2 rumble | `2` | `[1,10] [1,11] [2,12] [2,13] [1,14] [1,15] [1,14] [1,16] [1,14] [1,15] [1,14] [1,16] [1,14] [1,15] [1,14] [1,16] [2,17] [2,12] [2,18] [1,10]` | 20 | 25 |
| q3 success flash/drop | `3` | `[1,19] [1,20] [1,21] [1,22] [1,23] [1,24] [1,25] [1,26] [1,27] [1,28] [5,23]` | 11 | 15 |
| q4 fail escape | `4` | `[1,29] [1,30] [1,31] [1,32] [1,33] [3,34] [1,35] [1,36] [1,37] [1,38] [1,39] [1,40] [1,39] [1,41] [1,39] [1,40] [1,39] [1,41] [1,39] [1,40] [1,39] [1,41] [1,42] [1,37] [1,43] [1,35] [1,44] [1,45] [1,46] [1,47] [1,48] [1,49] [1,50] [1,51] [1,52] [1,53] [1,48]` | 37 | 39 |

Additional parsed counts:

- Frames: `30`
- Cells: `54`
- Anim states: `5`
- Hit/collision boxes: none in this sprite file.

## Rebuild Change

File changed:

- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Added focused checkpoint:

- `battle_p17_sprite269_timing_matrix`

Checkpoint behavior:

1. Loads `SpriteAnim.load(269)`.
2. Asserts source matrix counts: `frames=30`, `cells=54`, `anims=5`.
3. Asserts all q0..q4 animation arrays match the parsed source rows exactly.
4. Asserts source tick totals `[8, 1, 25, 15, 39]`.
5. Drives a generic catch success route into P17.
6. Verifies runtime uses sprite `269`, item `0`, caught success, and reaches q1/q2/q3 with source-matrix guard.

## Classification

| Area | Status | Note |
| --- | --- | --- |
| Sprite 269 source matrix | `PORTED` | Checkpoint asserts exact source arrays from `spr_269_all(r)`. |
| Rebuild frame-delay formula | `PORTED` | `delay - 1` matches source `d.c()` pre-decrement behavior for this sprite. |
| P17 q0/q1/q2/q3 route checkpoint | `PORTED/PARTIAL` | Runtime reaches source-shaped phases. |
| q4 fail route | `PORTED/PARTIAL` | Already covered by `battle_p17_q4_fail_restore_enemy` and `battle_bunny_first_catch_fail_escape_effect`; matrix now locks q4 source rows. |
| Exact pixel parity | `PENDING` | No original-vs-rebuild pixel compare was performed. |
| Exact `ah/l/e/drawRGB` bitmap parity | `PENDING` | This checkpoint covers sprite 269 timing, not full catch effect bitmap generation. |

## Required Verification

After this code slice:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp .\build\classes com.vqsv.rebuild.Main --check
$bad = Select-String -Path .\src\main\java\*.java -Pattern 'ï¿½|Ãƒ|Ã‚|Ä‚|Ã„|Ã†' -AllMatches
if ($bad) { $bad | Select-Object -First 20; exit 1 }
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_p17_sprite269_timing_matrix build_intro_demo\p17_sprite269_timing_matrix.png
```

`VqsvBattleDamageFormulaCheck` is not required by this slice because battle
runtime/formula code was not changed. It is still safe to run if doing a broader
regression batch.

## Next

Recommended next slice:

- `ah` type 8 catch effect parity tightening.

Reason:

- Sprite `269` timing is now guarded by source matrix smoke.
- The remaining visual uncertainty in P17 is the `ah` target copy/scale/drawRGB
  effect path, especially q1 capture shrink and q4 escape restoration.
