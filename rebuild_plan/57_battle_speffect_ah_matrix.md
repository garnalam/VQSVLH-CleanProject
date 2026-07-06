# 57 Battle Speffect AH Matrix

Status: AUDIT ONLY. No Java code was changed in this step.

Scope: audit battle skills whose `effect.mid` rows contain a 7-byte chunk with
`chunk[1] == 1`. In source this path creates an `ah` special-effect renderer
from `speffect.mid`.

## Source Facts

### P7 effect entry

Source path:

```text
game.d case 7
-> game.d.n()
-> O = ao[h.D]                    // selected skill effect.mid row
-> J = I                          // current effect chunk index
-> if O[J * 7 + 1] == 1:
     H = new ah()
     sourceSpeffect = speffect.mid[O[J * 7 + 2]]
     runtimePrefix =
       [sourceSpeffect[0], actorX, actorY, actorSpriteId, actorAnimState, actorOrientation]
     H.a(runtimePrefix + sourceSpeffect[1..])
     H.c(true)
```

Draw order observed in `game.d` P7 draw path:

```text
draw actors
if H != null: H.a(Graphics)
draw actors again
draw floating damage/blood text
```

`game.d.n()` actor-side selection:

| `effect.mid` chunk offset | Meaning | Source evidence | Status |
| --- | --- | --- | --- |
| `0` | Actor side for this effect step. `0` uses target actor runtime data; non-zero uses attacker actor runtime data. | `game.d.n()` branch on `O[J*7]` | PORTED FACT |
| `1` | Special flag. `1` creates `new ah()` from `speffect.mid`; `0` applies actor animation directly. | `game.d.n()` branch on `O[J*7+1]` | PORTED FACT |
| `2` | `speffect.mid` row id when offset 1 is `1`; actor animation id when offset 1 is `0`. | `game.d.n()` | PORTED FACT |
| `3` | Actor animation parameter/state for non-special chunks; still participates in P7 flow. | `game.d.n()` / P7 update | PARTIAL |
| `4` | Trigger frame/hook for sequencing. `-1` means none. | P7 update checks `O[J*7+4]` | PARTIAL |
| `5` | Trigger frame for actor state change. `-1` means none. | P7 update checks `O[J*7+5]` | PARTIAL |
| `6` | Actor state applied when offset 5 triggers. | P7 update calls actor `d(O[J*7+6])` | PARTIAL |

### `ah.a(short[])` prepended row format

The rows in `script/decoded/data__script__speffect.mid.json` do not directly
contain actor coordinates or actor sprite id. `game.d.n()` prepends those values
before calling `ah.a(short[])`.

Runtime row passed to `ah`:

```text
[0] speffect type                 // original speffect row[0]
[1] actorX
[2] actorY
[3] actorSpriteId                 // aq.c[0][actorSpecies][17]
[4] actorAnimState                // actor p()
[5] actorOrientation              // actor n
[6...] original speffect row[1..]
```

This means a renderer cannot draw `speffect.mid` alone. It needs the current
attacker/target actor snapshot from battle P7.

## Full Scan Summary

Input files:

- `script/decoded/data__script__effect.mid.json`
- `script/decoded/data__script__speffect.mid.json`
- `script/decoded/data__script__db.mid.json`, group `1` = `aq.c[1]` skill rows.

Scan result:

| Metric | Value |
| --- | ---: |
| Effect chunks with `chunk[1] == 1` | `57` |
| Distinct skill rows using at least one special chunk | `41` |
| `speffect.mid` row types present | `1, 7, 8, 9, 11, 12, 13, 14, 15` |

`speffect.mid` type distribution from decoded rows:

| Type | Speffect row ids |
| --- | --- |
| `1` | `11,13,15,17` |
| `7` | `4` |
| `8` | `2,3,6` |
| `9` | `0,1,5,7,8,9,10,16,18,19` |
| `11` | `20,22,25,27` |
| `12` | `12,14,21` |
| `13` | `23,24` |
| `14` | `26` |
| `15` | `28,29,30,31,32,33` |

No current `speffect.mid` row uses `ah` type `0`, `2..6`, `10`, `16`, `17`, or
the default `>=20` sprite-id map path, although those branches exist in
`ah.java`.

## AH Type Semantics

Grounded in `source_code/decoded/decompiled_source_cfr/ah.java`.

| AH type | Source behavior summary | Primary asset source | Status |
| --- | --- | --- | --- |
| `1` | Loads actor sprite, loads helper image `aq.e[t[2]]`, scrolls/composites helper image into cloned actor image. | actor sprite + `/data/tex/tex_*` via `aq.e` | PENDING |
| `7` | Loads actor sprite and scaled copy; draw alternates original/scaled frame. | actor sprite | PENDING |
| `8` | Loads actor sprite, color/transformed copy, applies offset triplets over time. | actor sprite | PENDING |
| `9` | Loads actor sprite, makes color-transformed copy using four color params, blinks overlay over duration/interval. | actor sprite | BEST FIRST PORT CANDIDATE |
| `11` | Multiple cloned actor sprites with color transforms. | actor sprite | PENDING |
| `12` | Multiple cloned actor sprites with palette/brightness transforms. | actor sprite | PENDING |
| `13` | Multiple cloned actor sprites with transform list. | actor sprite | PENDING |
| `14` | Multiple cloned actor sprites with alternate transform path. | actor sprite | PENDING |
| `15` | Multiple cloned actor sprites with color transforms and timed cycles. | actor sprite | PENDING |

Exact pixel parity for any type still requires auditing `l.a(...)`, `l.b(...)`,
and `e.drawRGB` transform behavior.

## Candidate Skill Matrix

These are selected from the 41 special-effect skill rows because they are either
already in our smoke route or easy to seed for a focused smoke.

| Skill id | `aq.c[1][skill]` row | `effect.mid[skill]` row | Special chunks | Renderer side | Status / reason |
| --- | --- | --- | --- | --- | --- |
| `45` | `[4,162,574,0,1,10,1,9,-1,1]` | `[0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | chunk0 -> `speffect 19`; chunk1 -> `speffect 15` | both target side (`chunk[0]=0`) | BEST NEXT SMOKE. Current smoke pet has skills `10,45`. |
| `44` | `[4,161,573,0,1,10,1,8,-1,1]` | same as skill `45` | chunk0 -> `19`; chunk1 -> `15` | target side | EASY comparison for same effect shape. |
| `11` | `[1,128,540,90,0,45,0,-1,10,0]` | `[0,0,21,1,-1,-1,0, 1,1,10,0,-1,-1,0]` | chunk1 -> `speffect 10` | attacker side (`chunk[0]=1`) | GOOD after type 9 target-side works. |
| `17` | `[1,134,546,130,2,30,0,-1,40,0]` | same as skill `11` | chunk1 -> `10` | attacker side | GOOD, but requires seeding skill or route where pet has it. |
| `21` | `[2,138,550,80,0,45,1,4,10,0]` | `[0,0,22,0,-1,-1,0, 1,1,5,0,-1,-1,0]` | chunk1 -> `speffect 5` | attacker side | LATER. Type 9 but different target-side skill semantics. |
| `32` | `[3,149,561,60,0,45,2,5,10,0]` | `[0,0,23,0,-1,-1,0, 0,1,1,0,-1,-1,0]` | chunk1 -> `speffect 1` | target side | LATER. Note: `speffect 1` is AH type `9`, not type `1`. |
| `54` | `[5,171,583,0,1,10,2,8,40,0]` | `[0,1,0,0,-1,-1,0]` | chunk0 -> `speffect 0` | target side | LATER. `speffect 0` is AH type `9`. |
| `55` | `[5,172,584,0,1,10,2,9,-1,0]` | `[0,1,12,0,-1,-1,0]` | chunk0 -> `speffect 12` | target side | LATER. AH type `12`, multi-clone behavior. |
| `64` | `[6,181,593,0,1,10,1,11,-1,0]` | `[1,1,18,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | chunk0 -> `18`; chunk1 -> `15` | attacker side | LATER. Self/attacker-side special effect. |
| `65` | `[6,182,594,0,1,10,1,12,-1,1]` | `[1,1,16,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | chunk0 -> `16`; chunk1 -> `15` | attacker side | LATER. Self/attacker-side special effect. |
| `68` | `[6,185,597,110,3,15,1,10,5,0]` | `[0,0,26,0,-1,-1,0, 0,1,0,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | chunk1 -> `0`; chunk2 -> `15` | target then attacker side | LATER. Useful for multi-side sequencing after first slice. |

## Speffect Rows For Immediate Candidates

### Skill 45 / 44

`effect.mid[45]`:

```text
[0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]
```

Chunk 0:

```text
effect chunk = [0,1,19,0,-1,-1,0]
speffect 19 = [9,120,255,255,255,0,6,2]
runtime ah row =
[9, targetX, targetY, targetSpriteId, targetAnimState, targetOrientation,
 120,255,255,255,0,6,2]
```

Source-shaped interpretation:

- AH type `9`.
- Load target actor sprite.
- Clone and color-transform sprite with params `120,255,255,255`.
- Blink transformed overlay for duration/interval values `6,2`.

Chunk 1:

```text
effect chunk = [0,1,15,0,-1,-1,0]
speffect 15 = [1,0,5,3,0,0]
runtime ah row =
[1, targetX, targetY, targetSpriteId, targetAnimState, targetOrientation,
 0,5,3,0,0]
```

Source-shaped interpretation:

- AH type `1`.
- Loads target actor sprite and a helper `aq.e[t[2]]` image.
- Composite/scroll behavior depends on `t[4]`.
- Do not approximate this yet unless `aq.e` and `l.a` composite behavior are audited.

### Skill 11 / 17

`effect.mid[11]` and `effect.mid[17]`:

```text
[0,0,21,1,-1,-1,0, 1,1,10,0,-1,-1,0]
```

Chunk 0 is normal actor animation. Chunk 1:

```text
effect chunk = [1,1,10,0,-1,-1,0]
speffect 10 = [9,120,218,217,169,0,4,2]
runtime ah row =
[9, attackerX, attackerY, attackerSpriteId, attackerAnimState, attackerOrientation,
 120,218,217,169,0,4,2]
```

Source-shaped interpretation:

- AH type `9`.
- Same renderer family as skill `45` chunk 0.
- Uses attacker side because `chunk[0]=1`.

## Recommended Next Implementation Slice

First port should be **skill 45 chunk0 / speffect 19 / AH type 9** only.

Reason:

- Current smoke pet already has skill ids `10` and `45`.
- Skill `45` can be reached by selecting the second row in P3 skill list.
- Chunk 0 uses target-side AH type `9`, the smallest useful special-effect branch:
  actor sprite clone + color transform + blinking overlay.
- Chunk 1 is AH type `1`, so it should remain PENDING until `aq.e` helper image
  and composite behavior are audited.

Minimum smoke plan for the next code slice:

```text
enter elder battle
open P3 skill list
move selection to skill 45
confirm
capture:
  battle_elder_p7_speffect45_start.png
  battle_elder_p7_speffect45_overlay.png
  battle_elder_p7_speffect45_after.png
```

## Not Done Yet

Do not mark `speffect.mid` or `ah` renderer complete after porting skill 45.

Remaining work:

- AH type `1` helper-image composite path.
- AH types `7,8,11,12,13,14,15`.
- Exact `l.a(...)` and `l.b(...)` pixel transforms.
- Multi-chunk sequencing through `H.e()`, `H.i()`, and P7 `chunk[4]/[5]/[6]`
  trigger hooks.
- Default `ah` sprite-map path, even though current `speffect.mid` scan did not
  find rows using type `>=20`.
- Pixel compare against original battle frames.
