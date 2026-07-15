# 345 - Battle Skill 20 Hat Bui Original Source Asset Chain Audit

Date: 2026-07-14

Status: SOURCE ASSET CHAIN PROVED / VISUAL STILL WRONG-PARTIAL / NO CODE PATCH.

This audit replaces the premature "trace cursor and patch timing" direction for
skill 20. The current question is simpler and more important:

```text
In the original/source game, which code path, table rows, sprite data, and image
asset are involved when skill 20 Hat Bui is cast?
```

No client was opened. No code was changed in this slice.

## User Visual Ground Truth

User correction:

```text
Hat Bui is a dust-wave animation, not falling rocks.
```

Treat current rebuild visual as wrong/partial until proven by original-vs-rebuild
frame compare. Passing numeric damage smoke does not prove visual parity.

## Short Answer

Yes, the original has a real visual asset for this skill path.

If the source route is followed literally, skill 20 gets its battle visual from:

```text
aq.c[1][20]
  -> effect.mid[20]
  -> game.d.n()
  -> target game.b.u actor effect
  -> ah actor id 22
  -> sprite.mid[264]
  -> spr_264_all(r)
  -> img_305
```

The image asset is:

```text
modules/img/decoded/data__img__img_305.mid.png
```

Important: `img_305` contains both brown rock chunks and a beige dust-wave burst.
Therefore the current rebuild bug is not "missing the skill image." The likely
bug is that rebuild presents the wrong cell/cursor/placement/timing emphasis for
the `sprite264` actor animation, making rocks dominate instead of the dust wave.

## Source Tables

### Skill row

Decoded source:

```text
modules/script/decoded/data__script__db.mid.json
group 1, row 20 = [2,137,549,100,0,45,0,-1,-1,0]
```

Meaning:

| Field | Value | Meaning |
|---|---:|---|
| element/class | 2 | Earth |
| name row | 137 | Hat Bui |
| description row | 549 | Low damage text |
| power | 100 | direct base power |
| PP max | 45 | max PP |
| effect mode | 0 | direct damage |
| buff/debuff/post | -1 / -1 / 0 | no extra status/special logic from skill row |

Status: PORTED for row lookup.

### Effect row

Decoded source:

```text
modules/script/decoded/data__script__effect.mid.json
row 20 = [0,0,22,0,-1,-1,0]
```

Chunk interpretation used by `game.d.n()`:

| Offset | Value | Meaning |
|---:|---:|---|
| 0 | 0 | target side |
| 1 | 0 | actor branch, not `speffect` branch |
| 2 | 22 | actor effect id |
| 3 | 0 | actor state |
| 4 | -1 | no next-chunk cursor trigger |
| 5 | -1 | no actor-state trigger |
| 6 | 0 | unused because trigger is -1 |

Status: PORTED for row lookup.

## Source Runtime Chain

### `game.d.n()` chooses actor effect, not Java skill class

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/game/d.java
```

Relevant source behavior:

```text
line 431: O = ao[h.D]
line 432: if chunk branch == 1, create special H from speffect.mid
line 461: else if chunk side == 0, call ((b)h.p).a(effectId, state)
line 464: else call h.a(effectId, state)
```

For skill 20:

```text
h.D = 20
O = effect.mid[20] = [0,0,22,0,-1,-1,0]
branch = 0
side = 0
```

So source calls the target pet:

```java
((b)this.h.p).a((short)22, (byte)0);
```

Status: PORTED/PARTIAL in rebuild P7 runtime.

### `game.b.a(short, byte)` creates actor `u`

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/game/b.java
```

Relevant behavior:

```text
line 119: public final void a(short s2, byte by)
line 121: this.u = null
line 122: this.u = new ah()
line 123: this.u.a(new short[]{s2, by, by2})
line 124: this.u.b(this.i, this.j)
line 129: this.u.c(true)
```

For skill 20:

```text
s2 = 22
by = 0
by2 = target direction
```

There is a special y-offset only for:

```text
s2 == 20 && by == 3
s2 == 22 && by == 4
```

Skill 20 uses `s2 == 22 && by == 0`, so that special y-offset does not apply.

Status: source route proven; exact rebuild placement still PARTIAL.

### `ah.java` maps actor id 22 to sprite 264

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/ah.java
```

Relevant behavior:

```text
line 16: x = {262,263,264,265,266,267,268,299,300,301,304,306,307,308,309}
line 261: n = direction
line 262: a.a(x[u - 20], false)
line 263: a.a(state, 0, true)
line 578: default draw path calls this.a.a(graphics, i, j, n)
```

For actor id 22:

```text
u - 20 = 2
x[2] = 264
```

So actor id 22 means:

```text
sprite 264, state 0
```

Status: PORTED in rebuild mapping:

```text
VqsvBattleRuntime.P7ActorAnimation.SOURCE_AH_ACTOR_SPRITES
```

## Sprite/Image Chain

Decoded source:

```text
modules/script/decoded/data__script__sprite.mid.json
row 264 = [264,305]
```

Meaning:

```text
sprite index 264
  -> spr file id 264
  -> image id 305
```

Files:

```text
modules/spr/original/spr_264_all(r)
modules/img/original/img_305.mid
modules/img/decoded/data__img__img_305.mid.png
```

Image dimensions observed from decoded PNG:

```text
img_305 = 68x79
```

Current decoded contact sheet evidence:

```text
rebuild_game/build/smoke/sprite264_audit/sprite264_states.png
```

Observed sprite264 state 0 metadata:

```text
states = 1
cells = 4
frames = 2
state 0 row = [2,0, 2,1, 1,2, 1,3]
```

Visual cell notes from the contact sheet:

| Cursor | Cell | Visual content | Rebuild risk |
|---:|---:|---|---|
| 0 | 0 | large brown rock/debris high above target | wrong if original user-visible start is dust wave |
| 1 | 1 | smaller falling debris | may still look like rock-fall |
| 2 | 2 | beige dust-wave burst with debris | closest to user correction |
| 3 | 3 | debris spread/trailing pieces | tail frame |

Status:

- image asset exists: PORTED.
- source sprite row exists: PORTED.
- current visible presentation: WRONG/PARTIAL.

## Source Sprite Timing Involved, But Not Patched Here

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/d.java
```

Relevant behavior:

```text
line 100: a(state, loopState, reset) sets animation state
line 116: c(cursor) sets q = cursor and frame delay
line 138: d() advances cursor after delay
line 161: e() reports q >= r - 1
line 327: draw uses current q to pick frame/cell
line 379: drawRegion draws the sprite cell from img table
```

This proves timing/cursor is involved. It does not yet prove which patch is
correct. Do not patch cursor 0/1/2 blindly.

Open question:

```text
Does original draw cursor 0 long enough for the player to perceive falling rock,
or does source update/draw order/capture timing make cursor 2, the dust-wave
cell, dominate the live animation?
```

Status: PENDING frame parity.

## Current Rebuild Chain

Rebuild source:

```text
rebuild_game/src/main/java/VqsvBattleRuntime.java
rebuild_game/src/main/java/VqsvBattleRenderer.java
rebuild_game/src/main/java/VqsvSpriteRenderer.java
rebuild_game/src/main/java/EarthSkill.java
```

Current rebuild facts:

```text
P7ActorAnimation maps source effect 22 -> sprite 264.
SpriteAnim loads sprite.mid row 264 -> spr 264/image 305 when source table is available.
Renderer draws battleP7ActorEffectSpriteId at the target side.
EarthSkill smoke verifies source rows and captures skill20 stages.
```

Focused smoke debug:

```text
checkpoint=battle_skill20_hat_bui_source_stage_animation
aq.c[1][20]=[2,137,549,100,0,45,0,-1,-1,0]
effect.mid[20]=[0,0,22,0,-1,-1,0]
actor cursor=0->3 side=enemy sourceId=22 sprite=264 state=0
damage=25
```

This proves mapping only. It does not prove the visual is correct.

## Classification

| Component | Status | Reason |
|---|---|---|
| Skill row `aq.c[1][20]` | PORTED | Source row read and smoke-asserted. |
| Name/description rows | PORTED | Skill UI path uses db/chs rows; text encoding in logs may be mojibake only. |
| Effect row `effect.mid[20]` | PORTED | Source row read and smoke-asserted. |
| Branch selection | PORTED | Source branch is target actor `u`, not AH/speffect. |
| Actor id 22 -> sprite 264 | PORTED | `ah.x[2] = 264`, rebuild mapping matches. |
| Sprite row 264 -> image 305 | PORTED | `sprite.mid[264] = [264,305]`, decoded image exists. |
| Asset contains dust wave | PORTED | `img_305` includes beige dust-wave burst. |
| Current rebuild Hat Bui visual | WRONG/PARTIAL | Rebuild emphasizes falling rock cells; user says original is dust wave. |
| Pixel-perfect claim | PENDING | No original-vs-rebuild frame compare for this skill. |

## What This Means

The source-backed route does not currently support the idea that Hat Bui should
use a totally different `effect.mid` row, a Java `EarthSkill` hardcoded
animation, or a missing `speffect` chunk. The original image asset for the route
already contains the dust-wave art.

The actual bug is narrower:

```text
skill20 uses the right source asset chain, but rebuild is presenting the wrong
visible part/timing/placement of that asset.
```

Possible causes to audit next, without guessing:

1. source `d.d()` update-before-draw cadence vs rebuild `SpriteAnim.tickHoldLast()`;
2. first visible cursor in source battle context;
3. actor anchor/orientation for target-side actor `u`;
4. whether the original capture/user memory corresponds to cursor 2 as the key
   visible frame, while rebuild smoke is emphasizing cursor 0/1.

## Next Safe Slice

Do not patch all skills. Do not fake a one-off new animation for Hat Bui.

Next safe slice should be one of these:

1. Build a source-backed `sprite264` frame-strip smoke that labels cursor 0, 1,
   2, 3 on the actual battle target position, with no gameplay noise.
2. Add trace-only frame logs for skill20 actor `u22`: tick, cursor before/after,
   visible flag, draw cursor, and target anchor.
3. Compare with an original-client capture if available.
4. Only then patch the smallest proven mismatch.

Until that is done:

```text
Hat Bui visual parity remains WRONG/PARTIAL.
```

