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
| 51 | `[51,136]` | 52,56 | 52 | yes | no | no | yes | no | NEED_RESOURCE |
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
- READY: 7
- NEED_MAPPING: 0
- NEED_RESOURCE: 19
- UNKNOWN: 0

## Visible Blank Risk

The following initially visible room0 actors use sprite indexes that have
source-proven rows in `sprite.mid`, but lack current rebuild code/resource
ports and can render blank safely under the current loader:

| Sprite index | Source row | Visible actors |
|---:|---|
| 51 | `[51,136]` | 52 |
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

1. Port the 19 source-proven `sprite.mid` rows into `SPRITE_TO_IMGS`, or preferably replace the manual table with a real `sprite.mid` loader matching `aq.a`.
2. Copy only the matching source-proven `spr_<sprId>_all(r)` and `img_<id>.png` resources into `rebuild_game/src/main/resources`.
3. Re-run build/check and a room0 smoke after any code/resource change.
4. Keep actors blank until their own source-proven row/resource is ported; do not substitute nearby-looking sprites.
