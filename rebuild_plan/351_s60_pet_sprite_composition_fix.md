# 351 - S60 Pet Sprite Composition Fix

Date: 2026-07-14

Status: FIXED / SOURCE-BACKED / NO PIXEL-PERFECT CLAIM.

Scope:

- pet/battle species sprite assembly after the S60 resource merge
- `spr_86..185` composition in runtime sprite loaders
- focused PNG proof and regression smoke

## Problem

After merging the AowVN S60 240x320 resources into `modules/`, raw decoded PNG
atlases looked healthy, but runtime pet rendering was broken:

```text
Pets disappeared or only showed a few body parts.
```

The previous visual audit only proved image atlases and maps were readable. It
did not prove sprite cell composition.

## Root Cause

Two sprite loaders still had an old approximation for `sprId 86..185`:

```text
VqsvSpriteRenderer.SpriteData.load(...)
com.vqsv.rebuild.render.SpriteMetadata.load(...)
```

Old behavior:

```text
If sprId is 86..185:
  ignore the real source cell rows and source animation rows
  synthesize 5 fake cells from cells[0]
  use a tiny hard-coded 3-row animation table
```

That approximation was no longer valid with the S60 resource set. S60
`spr_86..185` files contain real cell/animation data, and the override caused
many species to render as only one sprite fragment.

Classification:

```text
old spr_86..185 synthesize path: APPROX_OLD_RESOURCE / REMOVED
new spr_86..185 path: PORTED, reads source sprite cells/animations
```

## Proof

Audit tool:

```text
build/visual_audit_tools/SpritePetCompositionAudit.java
```

Before fix output:

```text
build/visual_audit/sprite_composition/pet_sprite_current_vs_raw_source.png
build/visual_audit/sprite_composition/pet_sprite_current_vs_raw_source.csv
```

Observation:

```text
current override column showed partial body parts.
raw source column showed complete pets.
```

After fix output:

```text
build/visual_audit/sprite_composition_after_fix/pet_sprite_current_vs_raw_source.png
build/visual_audit/sprite_composition_after_fix/pet_sprite_current_vs_raw_source.csv
```

Observation:

```text
current runtime column now matches raw source composition for the audited pet
species. Pets render as full bodies again.
```

## Code Changes

Runtime renderer:

```text
rebuild_game/src/main/java/VqsvSpriteRenderer.java
```

Changed:

```text
Read frames, cells, animations directly from spr_*.
Do not special-case sprId 86..185.
```

Package renderer:

```text
rebuild_game/src/main/java/com/vqsv/rebuild/render/SpriteMetadata.java
```

Changed:

```text
Read cells and animations directly from source sprite metadata.
Removed SPECIAL_OFFSETS, SPECIAL_ANIMS, and synthesizeSpecialCells.
```

Check label:

```text
rebuild_game/src/main/java/com/vqsv/rebuild/render/SpriteSmokeCheck.java
```

Changed:

```text
sprite86Special -> sprite86Source
```

## Verification

Build:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
PASS
```

Main check:

```text
java -cp build\classes com.vqsv.rebuild.Main --check
PASS
sprite86Source=frames:3 cells:8 anims:3
```

Formula:

```text
java -cp build\classes VqsvBattleDamageFormulaCheck
PASS
```

Focused PNG:

```text
world_petstate_ui_bunny_selected PASS
battle_p5_switch_preserves_hp_pp_buff_debuff_state PASS
battle_elder_command_ui PASS
```

Regression:

```text
battle_quick PASS 227/227
panel_wheel PASS 8/8
Battle Lab catch all PASS 30/30
Battle Lab NPC all PASS 33/33
```

Note:

```text
The Battle Lab catch and NPC suites were first launched in parallel. NPC build
printed a jar FileAlreadyExistsException because both commands touched the same
jar, but the NPC suite continued and finished PASS 33/33. Prefer running full
suite builds sequentially.
```

## Remaining Visual Debt

This fix restores full pet bodies. It does not claim pixel-perfect placement.

Remaining after switching to true source sprite bounds:

```text
Battle actor anchor/ground placement may need a separate source-backed audit.
Some pets now appear with their true larger bounds, which can expose old
approximate placement offsets.
```

Recommended next step:

```text
Audit battle actor anchor placement using source cell bounds and battle position
tables, then patch only placement/anchor if PNG or source route proves a real
misalignment.
```
