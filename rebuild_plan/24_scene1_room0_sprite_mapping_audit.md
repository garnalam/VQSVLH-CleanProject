# Scene 1 Room 0 Sprite Mapping Audit

Scope: current manual `loadScene1Room0()` actor table in
`rebuild_game/src/main/java/VqsvIntroDemo.java`, lines 1010..1083.

Purpose: identify which room0 actors can render with current rebuild resources
and which must stay blank/safe until the source-proven mapping is ported into
the rebuild.
No code or visual mapping was changed by this audit.

## Evidence Read

- Actor table: `rebuild_game/src/main/java/VqsvIntroDemo.java`, `loadScene1Room0()`.
- Current manual mappings: `VqsvIntroDemo.java`, `SpriteAnim.SPRITE_TO_IMGS`, lines 1572..1593.
- Runtime sprite loader behavior: `SpriteData.load()`, lines 1763..1776; missing sprite/image mapping returns blank data.
- Rebuild plan source facts: `modules/rebuild_plan/05_resource_format_specs.md`,
  `07_resource_renderer_notes.md`, `09_world_event_notes.md`, `19_world_tick_actor_matrix.md`.
- Source code facts: `modules/source_code/decoded/decompiled_source_cfr/aq.java` loads
  `/data/script/sprite.mid` into `aq.a`; `d.java` uses `aq.a[spriteIndex][0]`
  as `sprId` and `aq.a[spriteIndex][1..]` as image ids; `game/a.java` passes
  actor `record[1]` to the sprite renderer as a sprite table index.
- Source sprite table: `modules/script/decoded/data__script__sprite.mid.json`.
- Original sprite files checked in `modules/spr/original`.
- Decoded image files checked in `modules/img/decoded`.
- Runtime resources checked in `rebuild_game/src/main/resources`.

## Status Rules

- `READY`: spr file exists in original and runtime resources, mapping exists in code, and mapped image exists in decoded images and runtime resources.
- `NEED_MAPPING`: original/source mapping is still not known. Do not add image ids by guessing.
- `NEED_RESOURCE`: mapping is source-proven, but the current rebuild lacks code mapping and/or runtime spr/image resources.
- `UNKNOWN`: original spr evidence is missing or insufficient.

## Room0 Unique Sprite Audit

| Sprite index | Source row `[sprId,img...]` | Actor ids | Initially visible actor ids | spr original | spr runtime resource | mapping in code | image decoded | image runtime resource | Status |
|---:|---|---|---|---|---|---|---|---|---|
| 8 | `[8,108]` | 57 | - | yes | yes | yes | yes | yes | READY |
| 17 | `[17,117]` | 50 | - | yes | yes | yes | yes | yes | READY |
| 23 | `[23,123]` | 41,46,49 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 25 | `[25,124]` | 48 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 50 | `[50,136]` | 42 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 51 | `[51,136]` | 52,56 | 52 | yes | yes | yes | yes | yes | READY |
| 52 | `[52,136]` | 39 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 53 | `[53,137]` | 43 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 54 | `[54,137]` | 45 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 66 | `[66,146]` | 34,35,38,51 | 34,35,51 | yes | no | no | yes | no | NEED_RESOURCE |
| 69 | `[69,149]` | 33,44,47 | 33 | yes | no | no | yes | no | NEED_RESOURCE |
| 81 | `[81,159]` | 36,40 | - | yes | yes | yes | yes | yes | READY |
| 92 | `[92,506]` | 55 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 102 | `[102,574]` | 54 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 137 | `[137,520]` | 53 | - | yes | no | no | yes | no | NEED_RESOURCE |
| 198 | `[198,212]` | 28 | 28 | yes | no | no | yes | no | NEED_RESOURCE |
| 200 | `[200,219]` | 8,9,10,11,12,22,23,24,25,26,27 | 8,9,10,11,12,22,23,24,25,26,27 | yes | yes | yes | yes | yes | READY |
| 201 | `[201,220]` | 37 | 37 | yes | no | no | yes | no | NEED_RESOURCE |
| 202 | `[202,222]` | 18,19,29 | 18,19,29 | yes | yes | yes | yes | yes | READY |
| 208 | `[208,220]` | 0,1,2 | 0,1,2 | yes | no | no | yes | no | NEED_RESOURCE |
| 209 | `[209,220]` | 20,32 | 20,32 | yes | no | no | yes | no | NEED_RESOURCE |
| 213 | `[213,223]` | 3,4,5 | 3,4,5 | yes | no | no | yes | no | NEED_RESOURCE |
| 223 | `[223,10023]` | 30,31 | 30,31 | yes | yes | yes | yes | yes | READY |
| 225 | `[225,218]` | 13,14,15,16,17 | 13,14,15,16,17 | yes | yes | yes | yes | yes | READY |
| 230 | `[230,217]` | 21 | 21 | yes | no | no | yes | no | NEED_RESOURCE |
| 339 | `[339,836]` | 6,7 | 6,7 | yes | no | no | yes | no | NEED_RESOURCE |

## Totals

- Unique room0 sprite indexes: 26
- READY: 8
- NEED_MAPPING: 0
- NEED_RESOURCE: 18
- UNKNOWN: 0

## Visible Blank Risk

The following initially visible room0 actors use sprite indexes that have
source-proven rows in `sprite.mid`, but lack current rebuild code/resource
ports and can render blank safely under the current loader:

| Sprite index | Source row | Visible actors |
|---:|---|
| 66 | `[66,146]` | 34,35,51 |
| 69 | `[69,149]` | 33 |
| 198 | `[198,212]` | 28 |
| 201 | `[201,220]` | 37 |
| 208 | `[208,220]` | 0,1,2 |
| 209 | `[209,220]` | 20,32 |
| 213 | `[213,223]` | 3,4,5 |
| 230 | `[230,217]` | 21 |
| 339 | `[339,836]` | 6,7 |

## Safe Next Steps

1. Port the remaining 18 source-proven `sprite.mid` rows into `SPRITE_TO_IMGS`, or preferably replace the manual table with a real `sprite.mid` loader matching `aq.a`.
2. Copy only the matching source-proven `spr_<sprId>_all(r)` and `img_<id>.png` resources into `rebuild_game/src/main/resources`.
3. Re-run build/check and a room0 smoke after any code/resource change.
4. Keep actors blank until their own source-proven row/resource is ported; do not substitute nearby-looking sprites.

## 2026-07-03 Update: Actor 52 / Sprite Index 51

Ported source-backed sprite index `51` for actor `52`:

- `sprite.mid` row: `[51,136]`
- sprite metadata copied: `modules/spr/original/spr_51_all(r)` ->
  `rebuild_game/src/main/resources/spr_51_all(r)`
- image copied: `modules/img/decoded/data__img__img_136.mid.png` ->
  `rebuild_game/src/main/resources/img/136.png`
- fallback manual row added in `VqsvIntroDemo.SpriteAnim`: `{51,51,136}`

Verification:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\sprite51_room0_5100.png" 5100
```

Status: `READY` for actor 52's current source sprite resource. Pixel-perfect
actor animation is still limited by the existing sprite renderer parity level.

## 2026-07-03 Update: Room0 Visible And Pet Resources

Ported the next source-backed room0 sprite resources into runtime resources and
fallback mapping in `VqsvIntroDemo.SpriteAnim`.

Source facts:

- `VqsvIntroDemo` already tries the real `sprite.mid` table through
  `SpriteTable.load(...)` when modules are available.
- The fallback manual mapping is still needed for packaged/runtime resource
  use when the source modules are unavailable.
- Every row below comes directly from
  `modules/script/decoded/data__script__sprite.mid.json`.

Ported rows:

| Sprite index | Source row | Status |
|---:|---|---|
| 23 | `[23,123]` | READY |
| 25 | `[25,124]` | READY |
| 50 | `[50,136]` | READY |
| 52 | `[52,136]` | READY |
| 53 | `[53,137]` | READY |
| 54 | `[54,137]` | READY |
| 66 | `[66,146]` | READY |
| 69 | `[69,149]` | READY |
| 92 | `[92,506]` | READY |
| 102 | `[102,574]` | READY |
| 137 | `[137,520]` | READY |
| 198 | `[198,212]` | READY |
| 201 | `[201,220]` | READY |
| 208 | `[208,220]` | READY |
| 209 | `[209,220]` | READY |
| 213 | `[213,223]` | READY |
| 230 | `[230,217]` | READY |
| 339 | `[339,836]` | READY |

Runtime resources copied:

- `spr_23_all(r)`, `spr_25_all(r)`, `spr_50_all(r)`, `spr_52_all(r)`,
  `spr_53_all(r)`, `spr_54_all(r)`, `spr_66_all(r)`, `spr_69_all(r)`,
  `spr_92_all(r)`, `spr_102_all(r)`, `spr_137_all(r)`, `spr_198_all(r)`,
  `spr_201_all(r)`, `spr_208_all(r)`, `spr_209_all(r)`, `spr_213_all(r)`,
  `spr_230_all(r)`, `spr_339_all(r)`.
- `img/123.png`, `img/124.png`, `img/136.png`, `img/137.png`,
  `img/146.png`, `img/149.png`, `img/212.png`, `img/217.png`,
  `img/220.png`, `img/223.png`, `img/506.png`, `img/520.png`,
  `img/574.png`, `img/836.png`.

Verification:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_after_return_no_dialog.png" 5920 "R90,U20,L330,D15,L20" 0
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_group2_first_dialog.png" 5920 "R90,U20,L330,D15,L20,L30,U20,0" 5
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_group3_pet_offer.png" 5920 "R90,U20,L330,D15,L20,L30,U20,0" 650
```

Observed:

- `room0_after_return_no_dialog.png`: visible room0 actors now render for the
  priority sprite set.
- `room0_group2_first_dialog.png`: actor 52 interaction still reaches group2.
- `room0_group3_pet_offer.png`: pet actors `53`, `54`, `55` render with their
  source sprite resources.

Still pending:

- `room0_pet_choice_ui.png` and `room0_after_pet_choice.png`, because
  `op35/op87/op41/op3/op9/op14` for room0 group3 are not fully ported yet.
