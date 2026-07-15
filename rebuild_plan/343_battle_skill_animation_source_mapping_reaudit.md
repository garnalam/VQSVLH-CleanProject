# 343 - Battle Skill Animation Source Mapping Re-audit

Date: 2026-07-14

Status: SOURCE MAPPING RE-AUDITED / HAT BUI VISUAL REOPENED / VISUAL PARITY WRONG-PARTIAL.

This audit responds to the reopened visual concern: battle skills must not be
treated as "one animation per class" in rebuild code. Source-first conclusion:
the original game does not choose a skill animation from the Java skill class.
It chooses it from `effect.mid[skillId]`, then `game.d.n()` dispatches each
7-byte chunk to either actor action `u` or special AH effect `H`.

No client was opened. Only source files and PNG smoke were used.

## Source Animation Chain

### Skill definition

Source skill rows live in `aq.c[1]`, loaded from `modules/script/original/db.mid`.
The decoded equivalent is:

```text
modules/script/decoded/data__script__db.mid.json
```

`game.h` shows skill UI text comes from:

```java
an.f(aq.c[1][skillId][1]) // skill name
an.f(aq.c[1][skillId][2]) // skill description
aq.c[1][skillId][5]       // PP max
```

`an.f(int)` simply returns `aq.d[index]`, where `aq.d` is loaded from
`chs.mid`.

### Animation row

Source P7 runtime loads battle skill animation rows in:

```java
game.d.ao = ae.b(ae.a("/data/script/effect.mid"));
game.d.m = ae.a(ae.a("/data/script/speffect.mid"));
```

At P7, `game.d.n()` reads:

```text
O = ao[h.D]
chunk = O[J * 7 .. J * 7 + 6]
```

Chunk meaning used by current audit:

```text
[ownerSide, branch, id, state, nextFrameTrigger, stateFrameTrigger, stateValue]
```

- `branch == 0`: actor action `u`.
- `branch == 1`: special AH effect `H` from `speffect.mid[id]`.
- `ownerSide == 0`: target side.
- `ownerSide == 1`: attacker side.

### Actor action path

For actor branch chunks, source calls:

```java
((b)this.h.p).a(effectId, state); // target side
this.h.a(effectId, state);        // attacker side
```

`game.b.a(short, byte)` creates:

```java
this.u = new ah();
this.u.a(new short[]{effectId, state, this.n});
this.u.b(this.i, this.j);
this.u.c(true);
```

Then `ah.java` maps actor ids:

| Source actor id | Sprite id |
|---:|---:|
| 20 | 262 |
| 21 | 263 |
| 22 | 264 |
| 23 | 265 |
| 24 | 266 |
| 25 | 267 |
| 26 | 268 |
| 27 | 299 |
| 28 | 300 |
| 29 | 301 |
| 30 | 304 |
| 31 | 306 |
| 32 | 307 |
| 33 | 308 |
| 34 | 309 |

Rebuild equivalent:

```java
P7ActorAnimation.SOURCE_AH_ACTOR_SPRITES
```

Status: PORTED for id-to-sprite mapping.

## Hất Bụi Case - Reopened

Skill `Hất bụi` is not guessed from text. Source mapping:

| Field | Value |
|---|---|
| skill id | `20` |
| name row | `aq.c[1][20][1] = 137 -> chs[137] = Hất bụi` |
| description row | `aq.c[1][20][2] = 549` |
| skill row | `[2,137,549,100,0,45,0,-1,-1,0]` |
| element/class | `2` Earth |
| power | `100` |
| PP | `45` |
| logic | direct damage, no buff/debuff/post-effect |
| effect row | `effect.mid[20] = [0,0,22,0,-1,-1,0]` |
| visual branch | target-side actor action |
| actor id | `22` |
| actor sprite | `264` |
| actor state | `0` |

Source mapping:

```text
Hất bụi = target enemy u22/state0/sprite264, then direct damage.
```

This proves only the route to actor sprite `264`. It does **not** prove the
current rebuild visual is correct.

Re-audit after user correction:

- User-visible original: Hất bụi should look like a dust wave, not falling rock
  chunks.
- Rebuild smoke currently shows falling rocks/debris prominently.
- `sprite264` contact sheet shows why: `state0` has four cursors:
  - cursor 0: rock/debris high above the target;
  - cursor 1: smaller debris;
  - cursor 2: the beige dust-wave burst;
  - cursor 3: debris spread.
- Therefore the earlier visual conclusion in this file was wrong. The mapping
  `skill20 -> u22/sprite264` is source-backed, but the visible presentation is
  still wrong/PARTIAL.

New status:

- Hất bụi source row: PORTED.
- Hất bụi actor sprite id: PORTED.
- Hất bụi visual parity: WRONG/PARTIAL.
- Suspected gap: actor sprite cursor/timing/start-frame/placement for
  `sprite264 state0`, or a missing source display gate that makes the dust-wave
  cursor dominate in the original.

## Current Rebuild Check

Focused smoke run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill20_hat_bui_source_stage_animation build\smoke\skill20_hat_bui_reaudit_source_stage
java -cp build\classes VqsvIntroDemo --smoke-suite battle_direct_base_one_chunk build\smoke\direct_base_one_chunk_reaudit
```

Smoke output proved only mapping:

```text
actorEffect=22 sprite=264 state=0 side=enemy
```

PNG evidence:

```text
rebuild_game/build/smoke/skill20_hat_bui_reaudit_source_stage/battle_skill20_hat_bui_source_stage_animation_zoom.png
rebuild_game/build/smoke/direct_base_one_chunk_reaudit/battle_skill20_direct_actor_u22_start.png
```

Visual observation from PNG after user correction:

- Skill 0 Fire direct uses orange/fire `u20/sprite262`.
- Skill 20 Hất bụi uses `u22/sprite264`, but the current visible first frames
  are falling rocks/debris.
- Skill 30 Water direct uses water bubble `u23/sprite265`.
- Hất bụi remains visual-wrong because the original should present a dust wave.

So the current focused rebuild checkpoint is not mapping all direct skills to
the same sprite. Hất bụi specifically is using the source actor id/sprite.

Additional asset evidence:

```text
rebuild_game/build/smoke/sprite264_audit/sprite264_states.png
rebuild_game/build/smoke/asset_audit/img260_310_contact.png
```

`img_305` contains both rock chunks and the beige dust-wave burst. The bug is
not "sprite264 missing"; the bug is that rebuild currently shows the wrong
portion/timing/placement for Hất bụi compared with the original.

## All Skill Animation Signature Summary

This matrix is the current source signature from `effect.mid`. It explains why
some skills in one element look related: many rows intentionally share the first
actor chunk. That does not mean every full skill presentation is complete.

| Skill ids | Source signature | Meaning | Status |
|---|---|---|---|
| 0,1,3,6,7,9 | `u20/state0/sprite262` | Fire direct first actor chunk shared. | PORTED/PARTIAL |
| 2,8 | `u20/state0/sprite262 -> H14` | Fire direct plus AH12 special. | PORTED/PARTIAL |
| 4,5 | `H16 -> H15` | Fire self buff producer. | PORTED/PARTIAL |
| 10,14,16 | `u21/state1/sprite263` | Wood direct actor variant. | PORTED/PARTIAL |
| 11,17 | `u21/state1/sprite263 -> H10` | Wood direct plus attacker-side special/heal visual. | PORTED/PARTIAL |
| 12 | `u21/state0/sprite263 -> H6` | Wood direct plus special. | PORTED/PARTIAL |
| 13,18,19 | `u21/state0/sprite263` | Wood actor variant without extra H. | PORTED/PARTIAL |
| 15 | `u33/state0/sprite308 -> H7` | Wood special actor id, not generic Wood. | PORTED/PARTIAL |
| 20,22,28,29 | `u22/state0/sprite264` | Earth direct first actor chunk shared. Hất bụi belongs here, but visible cursor/timing is wrong for skill20. | WRONG/PARTIAL for Hất bụi visual |
| 21 | `u22/state0/sprite264 -> H5` | Earth direct plus buff/special. | PORTED/PARTIAL |
| 23,26 | `u22/state0/sprite264 -> H6` | Earth direct plus special. | PORTED/PARTIAL |
| 24 | `u22/state0/sprite264 -> H17` | Earth producer with extra special. | PORTED/PARTIAL |
| 25 | `H4 -> H17` | Earth buff producer. | PORTED/PARTIAL |
| 27 | `u22/state0/sprite264 -> H7` | Earth direct plus special. | PORTED/PARTIAL |
| 30,31,33,36,39 | `u23/state0/sprite265` | Water direct first actor chunk shared. | PORTED/PARTIAL |
| 32 | `u23/state0/sprite265 -> H1` | Water direct plus special. | PORTED/PARTIAL |
| 34 | `H4` | Water buff producer. | PORTED/PARTIAL |
| 35 | `H4 -> H17` | Water buff producer plus extra special. | PORTED/PARTIAL |
| 37 | `u23/state0/sprite265 -> H7 -> H6` | Water multi-stage. | PORTED/PARTIAL |
| 38 | `u23/state0/sprite265 -> H7` | Water multi-stage. | PORTED/PARTIAL |
| 40,46,49 | `u24/state0/sprite266` | Electric direct first actor chunk shared. | PORTED/PARTIAL |
| 41,43,47 | `u24/state0/sprite266 -> H4` | Electric direct plus special. | PORTED/PARTIAL |
| 42 | `u24/state0/sprite266 -> H1` | Electric direct plus special. | PORTED/PARTIAL |
| 44,45 | `H19 -> H15` | Electric buff/debuff producer. | PORTED/PARTIAL |
| 48 | `u24/state0/sprite266 -> H9` | Electric direct plus special. | PORTED/PARTIAL |
| 50 | `u25/state0/sprite267 -> H9` | Shadow direct plus special. | PORTED/PARTIAL |
| 51 | `u25/state0/sprite267 -> H8 -> H11` | Shadow multi-stage. | PORTED/PARTIAL |
| 52 | `u25/state1/sprite267 -> H8 -> H10` | Shadow state variant plus specials. | PORTED/PARTIAL |
| 53 | `u25/state2/sprite267 -> H9` | Shadow state variant plus special. | PORTED/PARTIAL |
| 54 | `H0` | Shadow zero-power/special route. | PORTED/PARTIAL |
| 55 | `H12` | Confusion special route. | PORTED/PARTIAL |
| 56 | `u25/state0/sprite267 -> H8 -> H9` | Shadow multi-stage. | PORTED/PARTIAL |
| 57 | `u25/state1/sprite267 -> H14 -> H11` | Shadow multi-stage. | PORTED/PARTIAL |
| 58 | `u25/state1/sprite267 -> H13 -> H10` | Shadow multi-stage. | PORTED/PARTIAL |
| 59 | `u25/state2/sprite267` | Shadow direct state variant. | PORTED/PARTIAL |
| 60,63,66,69 | `u26/state0/sprite268` | Wind direct first actor chunk shared. | PORTED/PARTIAL |
| 61,67 | `u26/state0/sprite268 -> H11` | Wind direct plus special. | PORTED/PARTIAL |
| 62,68 | `u26/state0/sprite268 -> H0 -> H15` | Wind multi-stage/buff route. | PORTED/PARTIAL |
| 64 | `H18 -> H15` | Wind buff/debuff producer. | PORTED/PARTIAL |
| 65 | `H16 -> H15` | Wind buff producer. | PORTED/PARTIAL |

## Important Correction To Previous Reasoning

Bad reasoning:

```text
"All skills in one element/class use one animation, so it is fine."
```

Correct reasoning:

```text
Every skill must be resolved by effect.mid[skillId].
If multiple skills share a first actor chunk, that is source data, but the full
presentation may still differ through H chunks, state, status visuals, damage
text, HP tween, active queue visuals, and timing.
```

## Current Risk Areas

| Area | Status | Why it matters |
|---|---|---|
| Direct actor id mapping | PORTED | Focused checks show different actor sprites for Fire/Earth/Water/etc. |
| H/speffect visibility | PARTIAL | Many skills differ only by later H chunks. If H type renderer is weak, skills look incorrectly similar. |
| Actor sprite timing | PARTIAL | `SpriteAnim.tickHoldLast()` is source-shaped but not proven pixel-exact against `d.d()`. |
| Hit/recover/death timing | PARTIAL | Skills can feel identical if target state/damage/HP timing is too generic. |
| Full original-vs-rebuild compare | PENDING | No pixel-perfect claim without original MIDP frame capture. |
| Per-skill source audit | PARTIAL | Signatures are listed, but each skill still needs a closeout with logic + visual smoke. |

## Next Step

Do not patch blindly. The next safe slice should be:

1. Create a focused `sprite264/Hất bụi` frame-timing audit:
   source `d.d()` tick order, actor `u.a()` start gate, first visible cursor,
   and source draw placement.
2. Patch only after proving whether the fix is:
   - skip/advance initial cursors for actor `u` in source-equivalent timing;
   - adjust actor placement/cell anchor;
   - or route Hất bụi through a different source visual layer.
3. Then build a full 70-skill source-signature contact sheet from `effect.mid`.
4. Do not use the old "Hất bụi is visually ok" conclusion again.
