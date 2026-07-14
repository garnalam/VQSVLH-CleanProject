# 336 - Battle Skill 20 Hất bụi Animation Re-audit

## Status

`AUDITED / SOURCE-STAGE SMOKE ADDED`

This audit reopens skill 20 because the old smoke only captured a rough `actor_u22_start`
frame. That made the animation look wrong by eye: the first `u22` frame is not the
most readable dust frame.

## Source Facts

| Fact | Source | Result |
|---|---|---|
| Skill row | `aq.c[1][20]` | `[2,137,549,100,0,45,0,-1,-1,0]` |
| Meaning | `game.b` damage switch / skill table | Earth direct damage, power 100, PP 45, no buff/debuff, target enemy |
| Effect row | `effect.mid[20]` | `[0,0,22,0,-1,-1,0]` |
| P7 entry | `game.d case 7 -> n()` | effect chunk owner side `0`, special flag `0`, actor effect id `22`, state `0` |
| Actor action | `game.b.a(short, byte)` | creates `u = new ah([effectId,state,actor.n])` at target actor `i,j` |
| Actor sprite | `ah.java x[]` | effect id `22` maps to sprite `264` |
| Facing | `game.d` battle setup | player actor `n=0`, enemy actor `n=1` |

## Rebuild Assertions Added

New smoke suite:

```text
battle_skill20_hat_bui_source_stage_animation
```

It asserts:

- Before confirm: clean `P3`, skill 20 selected, PP `45`.
- P7 stage 1: attacker base state is `1`.
- Target actor effect: source id `22`, sprite `264`, state `0`, enemy side.
- Damage frame: target/enemy base state is `2`, no buff/debuff text, direct damage only.
- Settled frame: both actors return to idle state `0`, enemy HP reduced, PP consumed once.

## Visual Smoke

Generated files:

```text
rebuild_game/build/smoke/hat_bui_source_stage/battle_skill20_hat_bui_source_stage_animation.png
rebuild_game/build/smoke/hat_bui_source_stage/battle_skill20_hat_bui_source_stage_animation_zoom.png
rebuild_game/build/smoke/hat_bui_source_stage/skill20_hat_bui_source_stage_frames/
```

Important visual finding:

- `u22` start frame is visually weak and can look offset by eye.
- `u22mid` shows the actual rock/dust around the target.
- `u22late` shows the rocks scattering out from the target.

So the old `actor_u22_start` screenshot was not enough to judge the skill animation.
The new source-stage smoke must be used for skill 20 visual checks.

## Current Verdict

| Area | Status | Note |
|---|---|---|
| Skill data row | `PORTED` | Row and PP/power match source. |
| Direct damage logic | `PORTED` | Damage frame keeps hit/crit/miss result flow. |
| Animation effect selection | `PORTED` | Uses source effect id `22` -> sprite `264`, state `0`. |
| Source-stage smoke | `PORTED` | before / attacker state1 / u22 start-mid-late / hit state2 / idle. |
| Full battle actor motion feel | `PARTIAL` | Shared P7 actor movement is still source-shaped, not fully byte-for-byte actor VM. |

## Next Step

If live gameplay still looks wrong after this, do not patch skill 20 row. The next
debug target is shared P7 actor action timing/rendering:

1. Compare source `d.java` draw anchor/facing against `SpriteAnim.drawCell` for orientation `1`.
2. Audit whether rebuild advances `ah.a()` one frame too early/late before first draw.
3. Apply the fix in shared renderer/runtime, then rerun this suite and Fire source-stage smoke.
