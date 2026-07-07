# 68 - Battle Skill 10 Actor `u` Type 21 Port

Status: PORTED/PARTIAL.

Scope: first source-backed actor action `u` slice for one battle skill.

## Source Facts

Skill selected: `10`, the first Elder battle skill in current smoke.

From `modules/script/decoded/data__script__effect.mid.json`:

```text
effect[10] = [0,0,21,1,-1,-1,0]
```

Chunk shape from `game.d.n()`:

```text
[owner, specialFlag, effectId, state, nextFrameTrigger, stateFrameTrigger, stateToSet]
```

For skill `10`:

| Field | Value | Meaning |
| --- | ---: | --- |
| owner | `0` | apply actor action to current target `h.p` |
| specialFlag | `0` | do not create special AH `H`; create actor action `u` |
| effectId | `21` | call `target.a((short)21, (byte)1)` |
| state | `1` | actor action sprite state `1` |
| nextFrameTrigger | `-1` | no chunk advance frame trigger |
| stateFrameTrigger | `-1` | no base-state trigger during action |
| stateToSet | `0` | unused because trigger is `-1` |

Source call chain:

- `game.d.n()` sees `specialFlag == 0` and calls `target.a((short)effectId, state)`.
- `game.b.a(short,byte)` creates `u = new ah()` and calls `u.a(new short[]{effectId, state, direction})`.
- `ah.a(short[])` default branch for `effectId >= 20` loads sprite id from:

```text
x = [262,263,264,265,266,267,268,299,300,301,304,306,307,308,309]
sprite = x[effectId - 20]
```

So `effectId 21 -> sprite 263`.

The original AH object then uses:

- sprite `263`
- state `1`
- actor direction from target
- target coordinates

## Rebuild Changes

Files changed:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Runtime:

- Rebuild already had `P7ActorAnimation` mapping `effectId 21 -> sprite 263`.
- This slice prevents manual lunge offsets from stacking while a source actor action `u` is active.
- Target hit/recover still uses existing P7 damage phase/state handling.

Smoke checkpoints added:

```text
battle_elder_p7_actor_u21_start.png
battle_elder_p7_actor_u21_trigger_hit.png
battle_elder_p7_actor_u21_recover.png
```

Smoke assertions:

- `battleP7ActorEffectVisible == true`
- `battleP7ActorEffectSpriteId == 263`
- `battleP7ActorEffectState == 1`
- actor action is on enemy/target side in Elder smoke
- target base state reaches hit/dead after action
- target base state recovers or remains dead afterward

## Remaining Partial / Pending

- AH `u` default branch is source-shaped through `SpriteAnim`, but not yet byte-for-byte equivalent to MIDP `d.d()` / `d.e()` timing.
- Skill `10` has no `[4]` or `[5]/[6]` frame triggers, so this slice does not prove trigger behavior for other skills.
- Recoil/damage text timing remains PARTIAL because it still uses rebuild P7 phase timing after the actor action.
- Full actor `u` parity requires more skills with non-trivial frame triggers and default AH types beyond `21`.
