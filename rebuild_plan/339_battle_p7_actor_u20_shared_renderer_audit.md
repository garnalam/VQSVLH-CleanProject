# 339 - Battle P7 Actor u20 Shared Renderer Audit

Status: PORTED/PARTIAL. One shared renderer bug fixed; Fire visual parity remains reopened.

## Source Facts

`game.d.n()` reads `/data/script/effect.mid` in 7-byte chunks.

For actor chunks where `chunk[1] == 0`, source calls:

```java
game.b.a(short effectId, byte state)
```

`game.b.a(short, byte)` then creates:

```java
this.u = new ah();
this.u.a(new short[]{effectId, state, this.n});
this.u.b(this.i, this.j);
this.u.c(true);
```

Special vertical placement exists only for:

```java
effectId == 20 && state == 3
effectId == 22 && state == 4
```

Fire skill actor chunks use `effectId=20`, `state=0`, so that special vertical offset does not apply.

`ah.java` maps actor effect ids through:

```java
x = {262,263,264,265,266,267,268,299,300,301,304,306,307,308,309}
sprite = x[effectId - 20]
```

So:

| Source actor id | Sprite id | Meaning in rebuild |
|---:|---:|---|
| 20 | 262 | Fire/basic actor hit effect used by many Fire rows |

`game.b.a(Graphics)` draw order:

1. optional species `L` before actor;
2. base pet actor;
3. optional species `L` after actor;
4. actor action `u.a(graphics)`.

So `u20` is drawn attached to the target pet, over the base pet, not as a separate skill-specific projectile.

## Fire Rows

These Fire skills really share the same first actor chunk in source data:

| Skill | effect.mid row | Source interpretation |
|---:|---|---|
| 0 | `[0,0,20,0,-1,-1,0]` | target-side `u20/state0` |
| 1 | `[0,0,20,0,-1,-1,0]` | target-side `u20/state0`, debuff logic differs after hit |
| 2 | `[0,0,20,0,0,0,-1, 0,1,14,0,0,0,-1]` | target-side `u20/state0`, then `speffect14/AH12` |
| 3 | `[0,0,20,0,-1,-1,0]` | target-side `u20/state0`, conditional damage differs |
| 6 | `[0,0,20,0,-1,-1,0]` | target-side `u20/state0`, power differs |
| 7 | `[0,0,20,0,-1,-1,0]` | target-side `u20/state0`, debuff logic differs after hit |
| 8 | `[0,0,20,0,0,-1,1, 0,1,14,0,0,-1,1]` | target-side `u20/state0`, then `speffect14/AH12` |
| 9 | `[0,0,20,0,-1,-1,0]` | target-side `u20/state0`, conditional damage differs |

Conclusion: it is source-backed that many Fire skills share `u20/state0` as the first actor chunk. The visual difference for some of them must come from later chunks, status/body overlays, hit state, text, or effects not yet fully pixel-parity, not from inventing separate actor ids.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
|---|---|---|
| `effectId=20 -> sprite262` | `P7ActorAnimation.SOURCE_AH_ACTOR_SPRITES[0] = 262` | PORTED |
| source state byte from effect row | `P7ActorAnimation.state` | PORTED |
| source direction `this.n` | `sourceBattleOrientation(playerSide)` | PORTED/PARTIAL |
| draw at pet `i,j` | `sourceBattleActorX/Y + sideOffset` | PORTED after this fix |
| special vertical offset for `20/state3`, `22/state4` | `drawP7ActorEffect()` branch | PORTED/PARTIAL |
| exact Java ME sprite transform/delay parity | `SpriteAnim.tickHoldLast()` and Java2D renderer | PARTIAL |

## Fixed In This Slice

`VqsvBattleRenderer.drawP7ActorEffect()` was adding battle motion offset twice:

```text
sourceBattleActorX + sideOffsetX + playerOffsetX/enemyOffsetX
```

Base actor draw uses only:

```text
sourceBattleActorX + sideOffsetX
```

This was corrected so actor actions, including `u20`, use the same anchored position as the base actor.

## Smoke

Headless PNG only:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_u20_offset_fix
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_u20_offset_fix
```

Representative output:

```text
rebuild_game/build/smoke/fire_live_frame_strip_after_u20_offset_fix/battle_fire_live_frame_strip.png
rebuild_game/build/smoke/fire_source_stage_after_u20_offset_fix/battle_fire_source_stage_animation_zoom.png
```

## Remaining Gaps

| Gap | Status | Next work |
|---|---|---|
| `u20/state0` exact MIDP frame timing | PARTIAL | compare `SpriteAnim` delay/tick behavior to `d.d()/d.e()` and adjust if a source-backed mismatch is found |
| Fire live screenshot shows richer visual than current smoke | PARTIAL | identify exact live skill id and inspect whether visible layer is AH/status/hit/recover, not `u20` itself |
| target hit/recover state exact timing after P7 damage | PARTIAL | continue actor state matrix work; do not hardcode Fire skill visuals |
| AH12 strength/color | PARTIAL | continue `speffect14` drawRGB alpha/color audit |
