# 334 Battle Shared Animation Parity Audit

## Scope

Stop per-skill porting until the shared P7 animation pipeline is source-shaped enough for live gameplay.

The user reported that all skill animations look wrong in game, including actor movement while using skills and hit/recover while taking damage. Current smoke coverage proved rows/logic, but it did not prove the shared actor/effect draw pipeline.

## Source Facts

### `game.b.a(short, byte)`

Source does not treat normal P7 actor effects as a free-floating battle overlay.

`game.b.a(short, byte)` creates `this.u = new ah()` with row:

```text
[sourceEffectId, effectState, actorDirection]
```

Then it places `u` at the actor's own `i,j`. Special cases:

```text
sourceEffectId == 20 && effectState == 3
sourceEffectId == 22 && effectState == 4
```

move `u` upward by the base actor frame height.

### `game.b.d(byte)`

The real pet actor state is separate from `u`:

```text
0 idle
1 action / attack
2 hit / recover
3 death / special
4 alternate state path
```

### `game.b.a(Graphics)`

Draw order is:

```text
L effect before actor when needed
base actor if visible
L effect after actor when needed
u actor-attached effect
```

So `u` is drawn after the actor, at source actor coordinates, with source orientation.

### `game.d` P7

P7 sequences chunks from `effect.mid`:

```text
chunk[1] == 1 -> H special effect from speffect.mid
chunk[1] != 1 -> actor-attached u from game.b.a(short, byte)
chunk[4] frame trigger may advance next chunk
chunk[5]/[6] may switch actor state at a frame
H start hides target/attacker with b(false)
H complete restores actor with b(true)
target hit/recover uses d((byte)2)
attacker is reset with d((byte)0)
```

## Rebuild Mismatch Found

| Area | Current rebuild | Source expectation | Status |
|---|---|---|---|
| Normal P7 actor effect | `drawP7ActorEffect()` draws inside hardcoded side rectangles | Draw `ah.u` at actor `i,j` from pos/cpos with actor orientation | FIXED in first slice |
| `u` special y offset | Not applied | effect 20/state3 and 22/state4 shift upward by actor base frame height | FIXED in first slice |
| Base actor action state | Runtime sets state 1/2/0 but smoke did not visually validate enough | Must be visible together with `u/H` sequencing | PARTIAL |
| `H` hide/restore | Runtime has hidden flags | Needs visual smoke around H start/end | PARTIAL |
| Exact MIDP `ah` timing | Source-shaped by frame cursor/tick, not pixel-compared | Needs original MIDP frame compare later | PENDING |
| All skill animations | Per-skill closeouts claimed row/effect, but shared pipeline was not validated enough | Revalidate after shared pipeline fix | PENDING |

## First Patch Slice

Change `VqsvBattleRenderer.drawP7ActorEffect()`:

- use `drawBattleSpriteAtSource(...)` instead of hardcoded rectangle draw.
- anchor at `sourceBattleActorX/Y + side offset + P7 offset`.
- use `sourceBattleOrientation(playerSide)`.
- apply source `game.b.a(short, byte)` upward offset for `effectId/state` pairs `(20,3)` and `(22,4)`.

This does not claim full pixel parity, but it removes the biggest shared positioning mismatch.

## Required Representative Smoke

Do not continue skill lane until these pass and the PNGs are inspected:

| Smoke | Why |
|---|---|
| `battle_skill20_hat_bui_timeline` | direct actor-only `u22` + damage |
| `battle_skill23_nham_bang_timeline` | actor `u22` + speffect6/AH8 |
| `battle_skill24_nguoi_bao_ve_dia_gioi_timeline` | no-damage self actor `u22` + speffect17/AH1 |
| `battle_quick` | regression quick gate |

## Next Work

1. Add explicit smoke checkpoints for base actor state 1 -> 0 and target state 2 -> 0 using actual rendered pixels/cells.
2. Add H hide/restore visual assertion for one source-backed skill.
3. Audit `d`/`SpriteAnim` timing versus source `ah.a.e()`, `ah.d()`, `ah.a(frame)` for normal actor `u`.
4. Only then resume skill-by-skill roadmap.
