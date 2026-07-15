# 348 - S60 Resource Merge Into Modules Status

Date: 2026-07-14

Status: MERGED INTO `modules/` / BUILD CHECK PASS / BATTLE REGRESSION PARTIAL.

User direction:

```text
Merge both resource versions. If duplicate, overwrite with the S60 version.
Keep old-only files because the old set may still be needed; debug later if
something breaks.
```

## Merge Method

Base folder:

```text
modules/
```

Overlay folder:

```text
modules_aowvn_240x320/
```

Merge semantics:

```text
Copy every file from modules_aowvn_240x320 into modules.
If target exists, overwrite it.
If old modules has a file missing from S60, keep the old file.
```

Inventory snapshot:

```text
build/merge_s60_snapshot_20260714_210605/modules_before.csv
build/merge_s60_snapshot_20260714_210605/candidate.csv
build/merge_s60_snapshot_20260714_210605/modules_after.csv
```

Merge result:

```text
merged_files=1627
overwritten=1255
added=372
```

## Post-merge Module Counts

```text
event        original=  16 decoded=  14
img          original= 308 decoded= 308
logo         original=   3 decoded=   2
map          original= 102 decoded= 102
mod          original=   8 decoded=   8
reports      original=   0 decoded=  12
root_misc    original=  51 decoded=   1
script       original=  19 decoded=  15
sound        original=   7 decoded=   0
source_code  original= 119 decoded= 259
spr          original= 325 decoded=   0
tex          original=  25 decoded=  25
ui           original=  42 decoded=  42
```

Important:

```text
tex/menu.mid and decoded data__tex__menu.mid.png remain from the old modules,
because the S60 JAR does not include data/tex/menu.mid. This is now a mixed
resource root by design.
```

## Key Resource Changes

S60 additions retained in `modules/`:

```text
event/minimap.mid
event/scene_8.mid
event/worldEvt.mid
script/layer.mid
script/media.mid
ui/menu.ui
ui/menu1.ui
sound/1.mid..6.mid
66 added img files
26 added spr files
8 added map files
```

`img_305` changed significantly:

```text
old decoded img_305 ~= 2,951 bytes
merged decoded img_305 = 13,789 bytes
```

This is important for skill 20 Hat Bui. After merge, `img_305` contains a much
larger effect atlas with visible dust/wind-wave art.

## Verification

### Build

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Result:

```text
PASS
```

### Main check

```text
java -cp build\classes com.vqsv.rebuild.Main --check
```

Result:

```text
PASS
```

Key output:

```text
imgInventory=original:308 decoded:308 missingDecoded:0 missingOriginal:0
spriteTable=rows:345
map0=mod:1 sizeTiles:23x23
bootFlowMenuNewGame=verified:opensSkipIntroPrompt
```

### Formula

```text
java -cp build\classes VqsvBattleDamageFormulaCheck
```

Result:

```text
PASS
battle-damage-formula-check-ok
```

### Hat Bui smoke

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill20_hat_bui_source_stage_animation build\smoke\after_s60_merge_hat_bui_verify2
```

Result:

```text
PASS
actorEffect=22 sprite=264 state=0 side=enemy
```

PNG:

```text
rebuild_game/build/smoke/after_s60_merge_hat_bui_verify2/battle_skill20_hat_bui_source_stage_animation_zoom.png
```

Visual observation:

```text
Hat Bui now shows a much clearer dust/wind-wave presentation from the S60
img_305 atlas. This is not pixel-perfect claimed, but it fixes the obvious
"tiny falling rocks only" symptom.
```

## Known Regressions After Merge

### `battle_quick`

Command:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\after_s60_merge_battle_quick
```

Result:

```text
PARTIAL FAIL
```

First checkpoint passed:

```text
route_sophie_after_battle_branch PASS
```

Then Bunny route failed:

```text
Checkpoint current did not finish in 3000 ticks
state=P1
hp=124/150:36/104
log=Di Lac tho tho ... bi thuong, nhanh su dung phong an cau
command=1
```

Interpretation:

```text
The S60 resource tables changed battle stats/damage/HP enough that the old Bunny
auto-smoke route no longer reaches the expected tutorial/catch path. This is a
real regression to re-audit, not a build/resource-load failure.
```

### Fire animation contact sheet

Command:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_animation_contact_sheet build\smoke\after_s60_merge_fire_contact
```

Result:

```text
PARTIAL FAIL
```

Reason:

```text
skill1 source row changed:
old expected effect=[0,0,20,0,-1,-1,0]
S60 effect=[0,0,20,1,-1,-1,0]
```

This is good evidence that the S60 tables have more per-skill animation
variation.

## Fire Effect Rows After Merge

```text
skill0 effect=[0,0,20,0,-1,-1,0]
skill1 effect=[0,0,20,1,-1,-1,0]
skill2 effect=[0,0,20,2,-1,-1,0,0,0,20,3,1,-1,0,0,1,0,0,0,-1,1]
skill3 effect=[0,0,20,4,-1,-1,0]
skill4 effect=[0,0,30,0,0,-1,0,0,1,16,0,-1,-1,0,0,1,15,0,-1,-1,0]
skill5 effect=[0,0,31,0,0,-1,0,0,1,16,0,-1,-1,0,0,1,15,0,-1,-1,0]
skill6 effect=[0,0,20,5,-1,-1,0]
skill7 effect=[0,0,20,6,-1,-1,0]
skill8 effect=[0,0,20,7,-1,-1,0,0,0,20,3,1,-1,0,0,1,0,0,0,-1,1]
skill9 effect=[0,0,20,8,-1,-1,0]
```

Conclusion:

```text
The old conclusion "many Fire skills share u20/state0" is no longer valid after
the S60 merge. The merged S60 effect table differentiates many Fire skill actor
states and even adds extra chunks for skill2/8.
```

## Mojibake Scan

The previous simple `rg` command hit PowerShell quoting issues with Unicode
patterns. A char-code scan found existing Java source matches in smoke/trace and
cutscene text files.

Status:

```text
WARNING / EXISTING SOURCE TEXT DEBT
```

This was not fixed in the resource merge slice.

## Next Recommended Step

Do not immediately patch broad gameplay.

Recommended next slice:

```text
Rebaseline battle smoke against the merged S60 tables:
1. update Fire skill expected effect rows from S60 source;
2. rebuild fire frame/contact smoke for skill0..9;
3. re-audit Bunny tutorial route with new HP/damage/stat tables;
4. run battle_quick again after Bunny route fix;
5. only then continue skill animation parity.
```

