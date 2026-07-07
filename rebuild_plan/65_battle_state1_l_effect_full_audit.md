# 65 - Battle State 1 `L` Effect Full Audit

Status: AUDITED/PARTIAL. All species-specific `L` effect rows reached by `game.b.d(1)` now have dedicated rebuild smoke coverage: species `0`, `10`, `62`, `75`, `87`, `91`, `92`, `97`, and `98`. Generic `L` pixel parity is still pending.

Scope: species-specific `L` effects created by `game.b.d(1)` during battle action state.

## Source Chain

1. `game.d.d()` loads `/data/script/speffect.mid` into `public static short[][] m`.
   - Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`, lines 211..219.
   - Parser: `ae.a(InputStream)` reads a `short[][]`: row count, row length, then signed shorts.
2. `game.b.d(byte)` state `1` sets base actor animation to state `1`, then creates `L` only for specific species ids.
   - Source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, lines 143..185.
3. `game.b.z(int speffectId)` builds an `ah` payload from `game.d.m[speffectId]`.
   - Source: `game/b.java`, lines 132..141.
   - Header passed to `ah`:
     - `sArray[0]`: AH type from `speffect.mid`
     - actor `x`
     - actor `y`
     - `aq.c[0][species][17]`: battle sprite id
     - cell/frame `0`
     - actor orientation `n`
   - Remaining payload is `speffect.mid` row excluding its first type value.
4. `game.b.a(Graphics)` starts and draws `L`.
   - Source: `game/b.java`, lines 224..279.
   - Start condition: if `L != null`, current unit state `U == 1`, and base sprite animation reports frame `1` via `this.a.b(1)`, call `L.a()`.
   - Draw order:
     - If `L != null && Z == 0`, draw `L` before the base actor.
     - Draw base actor.
     - If `L != null && Z == 1`, draw `L` after the base actor.
   - Only species `10` sets `Z = 1` in the audited state-1 switch.

## Species To Speffect Matrix

| Species id | `aq.c[0][species][17]` battle sprite | `game.b.d(1)` action | `speffect.mid` id | AH type | Draw order | Status |
| --- | ---: | --- | ---: | ---: | --- | --- |
| `0` | `86` | `z(27)` | `27` | `11` | before actor (`Z=0`) | AUDITED |
| `10` | `96` | `Z=1; z(28)` | `28` | `15` | after actor (`Z=1`) | AUDITED |
| `91` | `177` | `z(26)` | `26` | `14` | before actor | AUDITED |
| `92` | `178` | `z(25)` | `25` | `11` | before actor | AUDITED |
| `97` | `183` | `z(23)` | `23` | `13` | before actor | AUDITED |
| `98` | `184` | `z(23)` | `23` | `13` | before actor | AUDITED |
| `62` | `148` | `z(24)` | `24` | `13` | before actor | AUDITED |
| `75` | `161` | `z(20)` | `20` | `11` | before actor | AUDITED |
| `87` | `173` | `z(21)` | `21` | `12` | before actor | AUDITED |

Current Elder smoke battle does not use one of these species ids, so no `L` effect should appear there if we stay source-faithful.

## Raw `speffect.mid` Rows Used By `L`

Rows are from `modules/script/decoded/data__script__speffect.mid.json`.

```text
speffect[20] = [11,4,200,209,209,155,200,239,224,142,200,255,254,227,0,3,0,0,-12,0,-27,1,-41,1,5,0,-18,1,-41,1,11,0,22,1,34,1]
speffect[21] = [12,2,255,180,0,6,0,1,-17,-9,11,2,21,6,4,12,-10,3,0,0,17,-9,-19,-3,-22,10,12,12,17,3,-11,0]
speffect[23] = [13,2,120,100,0,3,0,1,6,6,-19,-18,22,17,8,10,-2,-2,-8,-6]
speffect[24] = [13,3,120,100,80,0,10,0,0,-19,0,-6,5,7,10,-16,-3,-27,-7,-36,-10,-51,-13,-51,-13,-51,-13,-10,1,-26,-6,-38,-14,23,15,23,15,23,15,-19,0,-6,5,7,10,-16,-3,-27,-7,-36,-10,-51,-13,-51,-13,-51,-13,-10,1,-26,-6,-38,-14,23,15,23,15,23,15]
speffect[25] = [11,3,255,152,176,232,255,192,231,243,0,9,0,0,-5,-11,2,-11,2,-11,13,-11,17,-11,11,-11,3,-11,2,-11,0,-11,0,-11,-5,-11,2,-11,2,-11,13,-11,17,-11,11,-11,3,-11,2,-11]
speffect[26] = [14,2,1,50,0,0,0,4,0,0,-26,-25,10,8,-26,-25,10,8]
speffect[27] = [11,2,100,0,0,0,0,2,0,1,-15,0,0,0]
speffect[28] = [15,3,150,222,120,28,150,146,28,23,0,7,0,1,1,0,0,1,0,-25,1,0,0,2,0,3,1,0,-25,2,0,3,1,0,0]
```

## AH Type Semantics Relevant To `L`

Source: `modules/source_code/decoded/decompiled_source_cfr/ah.java`.

### Shared lifecycle

- `ah.a(short[])` parses the payload and prepares one or more transformed `e` bitmaps.
- `ah.a()` starts the effect by setting inherited visible/active flags via `a(true)` and `b(true)`.
- `ah.e()` advances the effect.
- For AH types `11`, `12`, `13`, `14`, `15`, update logic is shared:
  - `t[2]` increments until `t[3]`.
  - Then `t[0]` frame index advances.
  - When `t[0] >= t[1]`, the effect stops and frees bitmaps.

### Type `11`

- Init branch: `ah.java`, lines 175..199.
- Creates `b[0]` from actor sprite cell `0`, then clones/transforms `b[1..n-1]`.
- Transform for each clone uses `l.a(e, r, g, b, alpha?)` from payload quads.
- Draw branch: `ah.java`, lines 536..546.
- Draws clone images `b[1..]` at per-frame offsets from `t`.
- Horizontal sign flips when actor orientation `n == 1`.

### Type `12`

- Init branch: `ah.java`, lines 201..220.
- Creates multiple copies and applies `l.b(e, value)` to each copy.
- Draw branch: `ah.java`, lines 548..556.
- Draws two layers using current frame offset and another offset indexed by `t[1]`.
- Horizontal sign flips when orientation `n == 1`.

### Type `13`

- Init branch: `ah.java`, lines 222..240.
- Creates `b.length` copies and applies `l.b(e, value)` to each.
- Draw branch: `ah.java`, lines 558..566.
- Draws each copy at per-frame per-copy offsets.
- Horizontal sign flips when orientation `n == 1`.

### Type `14`

- Init branch shares type `11` setup but uses `l.b(e, x, y)` for clones.
- Draw branch shares type `11` draw path.

### Type `15`

- Init branch: `ah.java`, lines 242..258.
- Creates `b[0]` from actor sprite and clone transforms via `l.a(e, r,g,b,alpha?)`.
- Draw branch: `ah.java`, lines 568..575.
- Per frame picks a bitmap index from `t[n]` and draws it at an offset.
- Horizontal sign flips when orientation `n == 1`.
- This is the one used by species `10`, and source draws it after the base actor (`Z=1`).

## What This Means For Rebuild Port

Minimum faithful slice:

1. Add a generic battle `L` effect state separate from P7 chunk effects.
2. Trigger only when the active battle unit enters base state `1` and its base sprite animation cursor reaches frame `1`.
3. Use the exact species whitelist above.
4. Use `aq.c[0][species][17]` as source sprite id and actor orientation for horizontal sign behavior.
5. Draw `L` before base actor except species `10`, where source sets `Z=1` and draws after base actor.
6. Implement AH types `11`, `12`, `13`, `14`, `15` sufficiently for these rows.

## Rebuild Smoke Slice Added

Implemented after this audit:

- Dedicated smoke battle uses player pet species `0`, level `30`, visual sprite `86`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `75`, level `30`, visual sprite `161`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `87`, level `30`, visual sprite `173`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `91`, level `30`, visual sprite `177`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `10`, level `30`, visual sprite `96`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `92`, level `30`, visual sprite `178`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `97`, level `30`, visual sprite `183`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `98`, level `30`, visual sprite `184`, and enemy species `34`.
- Dedicated smoke battle also covers player pet species `62`, level `30`, visual sprite `148`, and enemy species `34`.
- This is intentionally separate from Elder battle. Elder does not trigger `L`.
- `VqsvBattleRuntime` now starts a state-1 `L` effect only when:
  - P7 attacker is in base state `1`;
  - attacker species maps to a known `L` speffect;
  - the base actor animation cursor reaches frame `1`.
- `VqsvBattleRenderer` currently renders:
  - AH type `11` enough for species `0` / speffect `27`.
  - AH type `11` enough for species `75` / speffect `20`.
  - AH type `11` enough for species `92` / speffect `25`.
  - AH type `12` enough for species `87` / speffect `21`.
  - AH type `14` enough for species `91` / speffect `26`.
  - AH type `15` enough for species `10` / speffect `28`, including source `Z=1` after-actor draw order.
  - AH type `13` enough for species `97` / speffect `23`.
  - AH type `13` enough for species `98` / speffect `23`.
  - AH type `13` long-row case for species `62` / speffect `24`.

Smoke PNG checkpoints:

```text
rebuild_game/build_intro_demo/battle_state1_l_species0_start.png
rebuild_game/build_intro_demo/battle_state1_l_species0_active.png
rebuild_game/build_intro_demo/battle_state1_l_species0_after.png
rebuild_game/build_intro_demo/battle_state1_l_species75_start.png
rebuild_game/build_intro_demo/battle_state1_l_species75_active.png
rebuild_game/build_intro_demo/battle_state1_l_species75_after.png
rebuild_game/build_intro_demo/battle_state1_l_species87_start.png
rebuild_game/build_intro_demo/battle_state1_l_species87_active.png
rebuild_game/build_intro_demo/battle_state1_l_species87_after.png
rebuild_game/build_intro_demo/battle_state1_l_species91_start.png
rebuild_game/build_intro_demo/battle_state1_l_species91_active.png
rebuild_game/build_intro_demo/battle_state1_l_species91_after.png
rebuild_game/build_intro_demo/battle_state1_l_species10_start.png
rebuild_game/build_intro_demo/battle_state1_l_species10_active.png
rebuild_game/build_intro_demo/battle_state1_l_species10_after.png
rebuild_game/build_intro_demo/battle_state1_l_species92_start.png
rebuild_game/build_intro_demo/battle_state1_l_species92_active.png
rebuild_game/build_intro_demo/battle_state1_l_species92_after.png
rebuild_game/build_intro_demo/battle_state1_l_species97_start.png
rebuild_game/build_intro_demo/battle_state1_l_species97_active.png
rebuild_game/build_intro_demo/battle_state1_l_species97_after.png
rebuild_game/build_intro_demo/battle_state1_l_species98_start.png
rebuild_game/build_intro_demo/battle_state1_l_species98_active.png
rebuild_game/build_intro_demo/battle_state1_l_species98_after.png
rebuild_game/build_intro_demo/battle_state1_l_species62_start.png
rebuild_game/build_intro_demo/battle_state1_l_species62_active.png
rebuild_game/build_intro_demo/battle_state1_l_species62_after.png
```

Status matrix for the smoke slice:

| Feature | Status | Notes |
| --- | --- | --- |
| Species 0 battle setup | PORTED | Uses `SourcePetState(0)` and source species row sprite `86`. |
| Speffect 27 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| Species 75 battle setup | PORTED | Uses `SourcePetState(75)` and source species row sprite `161`. |
| L trigger by state 1 + cursor frame 1 | PORTED/PARTIAL | Source-shaped, not yet MIDP frame-compared. |
| Speffect 20 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| AH type 11 draw | PORTED/PARTIAL | Tinted clone images and per-frame offsets are rendered; exact Java ME pixel behavior still unverified. |
| Species 87 battle setup | PORTED | Uses `SourcePetState(87)` and source species row sprite `173`. |
| Speffect 21 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| AH type 12 draw | PORTED/PARTIAL | Two alpha layers and source offset pattern are rendered; exact Java ME pixel behavior still unverified. |
| Species 91 battle setup | PORTED | Uses `SourcePetState(91)` and source species row sprite `177`. |
| Speffect 26 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| AH type 14 draw | PORTED/PARTIAL | Shares source type-11 offset path and uses `l.b(e, multiplier, add)`-style RGB adjustment for clone images; exact Java ME pixel behavior still unverified. |
| Species 10 battle setup | PORTED | Uses `SourcePetState(10)` and source species row sprite `96`. |
| Speffect 28 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| AH type 15 draw | PORTED/PARTIAL | Uses source frame table to pick bitmap index and offset per frame; exact Java ME pixel behavior still unverified. |
| Species 10 `Z=1` after-actor draw | PORTED/PARTIAL | Render order and smoke assert confirm effect is drawn after base actor in rebuild; exact MIDP layering still unverified. |
| Species 92 battle setup | PORTED | Uses `SourcePetState(92)` and source species row sprite `178`. |
| Speffect 25 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| Species 97 battle setup | PORTED | Uses `SourcePetState(97)` and source species row sprite `183`. |
| Speffect 23 payload | PORTED/PARTIAL | Reads original `speffect.mid` row. |
| AH type 13 draw | PORTED/PARTIAL | Multiple alpha layers and source offset pattern are rendered for the short row; exact Java ME pixel behavior still unverified. |
| Species 98 battle setup | PORTED | Uses `SourcePetState(98)` and source species row sprite `184`. |
| Species 62 battle setup | PORTED | Uses `SourcePetState(62)` and source species row sprite `148`. |
| Speffect 24 payload | PORTED/PARTIAL | Reads original long `speffect.mid` row. |
| AH type 13 long row | PORTED/PARTIAL | Same renderer covers the longer species62 row; exact Java ME pixel behavior still unverified. |
| AH type 15 | PORTED/PARTIAL | Rendered for species `10`; still needs MIDP pixel compare. |

## Unknowns / Pending Before Claiming Pixel Parity

- Java ME `l.a(...)` / `l.b(...)` color-alpha helper semantics are now source-shaped in rebuild; see `66_battle_java_me_drawrgb_color_alpha_audit.md`.
- Exact Java ME `drawRGB(..., processAlpha=true)` compositing still needs MIDP frame capture and visual parity checks.
- Need to map rebuild battle units to original species ids in all battles; this audit only proves dedicated smoke coverage for every species-specific `L` row in `game.b.d(1)`.
- Dedicated smoke battles now cover species `0`, `10`, `62`, `75`, `87`, `91`, `92`, `97`, and `98`; Elder battle is not valid for this feature.
- Need frame-by-frame comparison against MIDP original before claiming pixel-perfect timing and draw order.
