# 349 - S60 Battle Smoke Rebaseline Status

Date: 2026-07-14

Status: S60 RESOURCE BASELINE ACTIVE / BUILD PASS / BATTLE QUICK PASS.

This document follows:

- `347_aowvn_240x320_resource_candidate_decode_audit.md`
- `348_s60_resource_merge_into_modules_status.md`

## Rule

The merged S60 resource set is now the active baseline. Old smoke expectations
from the previous resource tables are not source truth anymore. If a checkpoint
fails after this merge, prove the new behavior from S60 tables/source before
patching.

No pixel-perfect claim is made here. These are rebuild PNG smoke checks only,
not original-client frame comparisons.

## Code Changes In This Rebaseline Slice

### Fire Skill Contact Sheet

File:

```text
rebuild_game/src/main/java/FireSkill.java
```

Rebaselined fire skill animation assertions to S60 `effect.mid` rows:

```text
skill0 = [0,0,20,0,-1,-1,0]
skill1 = [0,0,20,1,-1,-1,0]
skill2 = [0,0,20,2,-1,-1,0, 0,0,20,3,1,-1,0, 0,1,0,0,0,-1,1]
skill3 = [0,0,20,4,-1,-1,0]
skill4 = [0,0,30,0,0,-1,0, 0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]
skill5 = [0,0,31,0,0,-1,0, 0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]
skill6 = [0,0,20,5,-1,-1,0]
skill7 = [0,0,20,6,-1,-1,0]
skill8 = [0,0,20,7,-1,-1,0, 0,0,20,3,1,-1,0, 0,1,0,0,0,-1,1]
skill9 = [0,0,20,8,-1,-1,0]
```

Current source-backed conclusion:

```text
Skills 0/1/3/6/7/9 intentionally share actor effect u20/sprite262, but use
different actor states in S60.

Skills 2/8 add producer speffect0/AH9 after a second u20 actor chunk. Their
later debuff active tick still uses speffect14/AH12.

Skills 4/5 start with actor effects u30/u31, then self-buff speffect16/AH9
and speffect15/AH1.
```

### Text UI Readiness

File:

```text
rebuild_game/src/main/java/VqsvTextRenderer.java
```

`TextBox.sourceUiTextReady()` now clamps its ready cursor to the actual last
frame in the loaded UI sprite animation.

Reason:

```text
S60 taskTip.ui has a shorter frame sequence than the old expected cursor path.
The old hard-coded ready cursor could leave Bunny tutorial taskTip stuck even
though the UI animation had already reached its final frame.
```

### Smoke Harness Size Split

File:

```text
rebuild_game/src/main/java/VqsvSmokeHarness.java
```

`runSmokeCheckpoint()` exceeded the JVM method bytecode limit after recent
checkpoint growth. Phase10B/P7 skill overlay checkpoints were moved into:

```text
handlePhase10BCheckpointInExistingScene(...)
```

This is harness-only structure cleanup. It does not change battle runtime
behavior.

### S60 Visual Queue Rows

Several status/body visual assertions now follow S60 rows:

```text
buff5 producer skill34: actor effect 23 / sprite265 / state4
buff5 active visual: [0,23,4,-1]
debuff0 active visual: [0,28,0,0,1,18,0,-1], wait for speffect18
debuff3 active visual: [0,21,4,-1]
debuff8 active visual: [1,0,0,-1,0,25,5,-1]
debuff10 active visual: [0,27,0,0,1,19,0,-1,1,6,0,-1], wait for speffect19
```

## Verification

All commands were run from:

```text
E:\VQSVLH-CleanProject\rebuild_game
```

### Build

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Result:

```text
PASS
```

### Main Check

```text
java -cp build\classes com.vqsv.rebuild.Main --check
```

Result:

```text
PASS
imgInventory=original:308 decoded:308 missingDecoded:0 missingOriginal:0
spriteTable=rows:345
bootFlowMenuNewGame=verified:opensSkipIntroPrompt
```

### Formula Check

```text
java -cp build\classes VqsvBattleDamageFormulaCheck
```

Result:

```text
PASS
battle-damage-formula-check-ok
```

### Battle Quick

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\s60_rebaseline_battle_quick_after_skill34_patch
```

Result:

```text
PASS
checkpoints=227/227
```

PNG output:

```text
rebuild_game/build/smoke/s60_rebaseline_battle_quick_after_skill34_patch/
```

Important pass points:

```text
route_sophie_after_battle_branch PASS
route_bunny_after_battle_task PASS
route_elder_after_battle_reward_state PASS
buff/debuff closeout checkpoints PASS
held item checkpoints PASS
Phase10B skill34/12/55 overlays PASS
```

### Fire Contact Sheet

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_animation_contact_sheet build\smoke\s60_rebaseline_fire_contact_final_verify
```

Result:

```text
PASS
```

PNG and notes:

```text
rebuild_game/build/smoke/s60_rebaseline_fire_contact_final_verify/battle_fire_animation_contact_sheet.png
rebuild_game/build/smoke/s60_rebaseline_fire_contact_final_verify/battle_fire_animation_contact_sheet_notes.md
```

### Mojibake Scan

Command:

```text
rg -n "<common mojibake markers>|\?\?" src\main\java
```

Result:

```text
No new broken encoding markers were found in active Java source.
The scan only matched intentional "??" unknown-speaker placeholders and old
backup .bak files.
```

## Current Classification

```text
S60 modules merge: PORTED/PARTIAL
Battle quick on S60 baseline: PORTED smoke-regression PASS
Fire skill 0..9 smoke contact sheet: PORTED/PARTIAL, source-table backed
Bunny taskTip readiness: PORTED for S60 UI frame count
Phase10B skill34 S60 visual: PORTED/PARTIAL, smoke-backed actor effect row
Original-vs-rebuild pixel parity: PENDING
Full skill animation matrix across all skills: PENDING
Old-only mixed resources retained by design: PORTED/PARTIAL, needs later audit
```

## Known Caveats

- `modules/` is intentionally mixed: S60 overwrote duplicates, while old-only
  resources remain.
- Some smoke names still reference old labels, for example
  `battle_phase10b_p7_type7_skill34_overlay`, even though S60 now proves
  skill34 as actor effect 23/state4 rather than the old type7 expectation.
- Console output still shows Vietnamese text mojibake in terminal logs because
  of Windows code page rendering. That is separate from Java source encoding.
- Pixel-perfect is not claimed without original-client frame compare.

## Recommended Next Step

Continue the skill/effect roadmap on the S60 baseline:

1. Create a source-backed full skill animation matrix using S60 `effect.mid`,
   `speffect.mid`, `sprite.mid`, and decompiled `game.d/game.b/game.h/ah`.
2. Pick the smallest failing or unverified skill group.
3. For each skill slice, require:
   - source row proof,
   - easy mechanism explanation,
   - PNG before/during/after,
   - HP/PP/status numbers before/during/after,
   - `battle_quick` or focused suite regression when the slice touches shared
     battle runtime.
