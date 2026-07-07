# 60 Battle P7 Chunk Trigger Hooks Matrix

Status: AUDIT ONLY. No Java code was changed in this step.

Scope: close the source understanding gap for `effect.mid` chunk offsets
`[4]/[5]/[6]` and the P7 `H.i()/H.e()` start/update conditions before porting
the next effect runtime slice.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - `game.d.n()`
  - `game.d case 7`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
  - `ah.a(short[])`
  - `ah.a()`, `ah.b()`, `ah.i()`, `ah.e()`
- `modules/script/decoded/data__script__effect.mid.json`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_plan/58_battle_speffect_type9_port.md`

## Effect Chunk Format

Each `effect.mid[skill]` row is consumed as 7-byte chunks:

```text
[0] side
[1] special flag
[2] animation id or speffect id
[3] animation param/state
[4] trigger frame for advancing to next chunk
[5] trigger frame for actor state change
[6] actor state applied when [5] triggers
```

Source-backed meanings from `game.d.n()` and `game.d case 7`:

| Offset | Meaning | Source evidence | Current rebuild status |
| --- | --- | --- | --- |
| `0` | Actor side. `0` uses target actor data; non-zero uses attacker actor data. | `game.d.n()` branch on `O[J*7]` | PORTED/PARTIAL |
| `1` | Special flag. `1` creates `H = new ah()` from `speffect.mid`; `0` applies actor animation via actor `u`. | `game.d.n()` | PORTED/PARTIAL |
| `2` | `speffect.mid` id if `[1] == 1`; actor animation id if `[1] == 0`. | `game.d.n()` | PORTED/PARTIAL |
| `3` | Actor animation parameter/state for non-special chunks. | `actor.a(anim, param)` calls in `game.d.n()` | PORTED/PARTIAL |
| `4` | Trigger frame/hook for advancing to next chunk while current actor animation is still active. | `u.a(O[J*7+4])` checks in P7 | PENDING |
| `5` | Trigger frame for actor state change while current actor animation is active. | `u.a(O[J*7+5])` checks in P7 | PENDING |
| `6` | Actor state passed to `actor.d(...)` when offset `[5]` triggers. | `actor.d(O[J*7+6])` | PENDING, only meaningful with `[5] != -1` in observed code |

## Source Flow: `game.d.n()`

`game.d.n()` is the only P7 chunk-entry helper currently audited.

Source shape:

```text
J = I
O = ao[h.D]                         // effect.mid row for selected skill

if O[J*7 + 1] == 1:
    H = new ah()
    choose actor runtime data by O[J*7]:
        0 -> target actor ((b)h.p)
        1 -> attacker h
    speffect = m[O[J*7 + 2]]
    runtimeRow = [speffect[0], actorX, actorY, actorSpriteId, actorAnimState, actorOrientation]
                 + speffect[1..]
    H.a(runtimeRow)
    H.c(true)
else:
    choose actor by O[J*7]:
        0 -> target actor
        1 -> attacker
    actor.a(O[J*7 + 2], O[J*7 + 3])

I++
```

Implications:

- `J` is the chunk currently being processed.
- `I` is the next chunk index.
- Current rebuild now starts chunk `0` and advances sequentially, but does not
  yet reproduce all source-trigger paths.

## Source Flow: P7 Actor Animation Branches

The source has two separate branches depending on which actor owns active
animation `u`.

### Attacker actor `h.u != null`

Relevant source shape:

```text
if h.u != null:
    if h.p() == 0:
        if h.u.i():
            if h.u.a.e():
                h.u.b()
                h.u = null
                if I > O.length/7 - 1 || W():
                    A = true
                    B = true
                else:
                    n()
                    if h.u != null: h.u.a()
                    if H != null: M = true
            else if O[J*7+4] != -1 && h.u.a(O[J*7+4]):
                h.u.b()
                if I < O.length/7 - 1 || W():
                    n()
                    if H != null: M = true
            else if O[J*7+5] != -1 && h.u.a(O[J*7+5]):
                h.d(O[J*7+6])
        else:
            N = false
            h.u.a()
    else if h.p() == 1 && h.a.e():
        h.d(0)
```

Observed meaning:

- `u.i()` seems to indicate the animation has started/entered an active state.
- `u.a.e()` is a nested animation completion check.
- `u.a(frame)` is a frame trigger check.
- `chunk[4]` can force early chunk transition before the actor animation fully
  completes.
- `chunk[5]/[6]` can change actor state at a frame trigger.
- `h.u.a()` starts/resumes the animation when `u.i()` is false.
- `h.u.b()` stops/cleans the current animation object.

Current rebuild:

- Does not model actor `u.i()`, `u.a.e()`, `u.a(frame)`, or `u.b()`.
- Uses simple per-chunk durations instead.
- Therefore this branch is PARTIAL.

### Target actor `((b)h.p).u != null`

Relevant source shape:

```text
else if target.u != null:
    if target.u.i():
        if target.u.d():
            target.u.b()
            if I > O.length/7 - 1 || W():
                target.d(2)
            else if O[I*7] == 1:
                target.d(2)
            else:
                K = 0
                target.u = null
                n()
                if target.u != null: target.u.a()
                if H != null: M = true
        else:
            if O[J*7+5] != -1:
                K = J
            if O[K*7+5] != -1 && target.u.a(O[K*7+5]):
                target.d(O[K*7+6])
                K = 0
            if O[J*7+4] != -1 && target.u.a(O[J*7+4]):
                n()
                if H != null: M = true
    else if h.p() == 1 && h.b() || N:
        h.d(0)
        target.u.a()
        N = false
    else if target.p() == 2 && target.b():
        z = true
        target.u = null
        if I > O.length/7 - 1 || W():
            A = true
        else:
            n()
            if H != null: M = true
```

Observed meaning:

- Target-side animation has different finish behavior from attacker-side:
  source calls `target.u.d()` instead of `h.u.a.e()`.
- If next chunk side `O[I*7] == 1`, target may be forced into state `2`.
- `K` stores the chunk index whose `[5]/[6]` state trigger is pending.
- `chunk[4]` can call `n()` without stopping the current target animation first.
- `N` and actor states `p()==1`, `p()==2`, `b()` gate start/exit behavior.

Current rebuild:

- Does not model `K`, target animation finish semantics, `target.d(2)`, or the
  `O[I*7] == 1` transition rule.
- Therefore this branch is PARTIAL.

## Source Flow: `H/ah` Special Effect Branch

Relevant source shape:

```text
if H != null && !H.i() && (h.p() == 1 && h.b() || M || h.p() == 0):
    if J == 0:
        N = true
    h.d(0)
    H.a()
    L = J
    if O[J*7] == 0:
        target.b(false)
    else:
        h.b(false)

if H != null && H.i() && !H.e():
    H = null
    M = false
    if O[L*7] == 0:
        target.b(true)
    else:
        h.b(true)
    if target.u == null && h.u == null:
        if I > O.length/7 - 1 || W():
            if O[J*7] == 0:
                z = true
            B = true
            A = true
        else:
            if O[I*7] == 1:
                z = true
            n()
            if H != null:
                M = true
    L = 0
```

Observed meaning:

- `H.i()` indicates whether the special effect is active/started.
- `H.a()` starts the special effect.
- `H.e()` ticks/updates the special effect and returns false when the effect is
  complete.
- `L` remembers which chunk owns the currently running `H`.
- While `H` is active, the source hides or disables the owning actor via
  `actor.b(false)`, then restores it with `actor.b(true)`.
- If no actor animation is active after `H` completes, source either:
  - marks finish flags `A/B/z`, or
  - calls `n()` to start the next chunk.
- `M` can force a special effect to start immediately after a new chunk is
  created.
- `N` is set when first chunk `J == 0` starts a special effect; later target
  animation start logic consumes it.

Current rebuild:

- Starts special rendering immediately when chunk enters phase.
- Does not model `H.i()` vs `H.a()` start gating.
- Does not model actor hidden/disabled state with `actor.b(false/true)`.
- Does not model `L`, `M`, `N`, or `z/A/B` exactly.
- Therefore `H.i()` start condition remains PARTIAL.

## Hook Field Scan

Scan source: `script/decoded/data__script__effect.mid.json`.

Rows with non-default hook-like fields:

```text
hook_hit_count = 8
skill_count = 8
```

| Skill | Chunk | `[0]` side | `[1]` flag | `[2]` id | `[3]` param | `[4]` next trigger | `[5]` state trigger | `[6]` state | Notes |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| `2` | `1` | `0` | `1` | `14` | `0` | `0` | `0` | `-1` | Both trigger offsets are `0`; state `-1`. Needs source-frame test before port. |
| `8` | `1` | `0` | `1` | `14` | `0` | `0` | `-1` | `1` | `[6]` is likely unused because `[5] == -1`. |
| `15` | `0` | `0` | `0` | `33` | `0` | `0` | `-1` | `0` | Actor animation chunk, early next trigger at frame `0`. |
| `37` | `1` | `0` | `1` | `7` | `0` | `-1` | `0` | `0` | State trigger at frame `0` while special/animation sequencing runs. |
| `41` | `1` | `0` | `1` | `4` | `0` | `-1` | `-1` | `1` | `[6]` likely unused because `[5] == -1`. |
| `47` | `1` | `0` | `1` | `4` | `0` | `-1` | `-1` | `1` | Same shape as skill `41`. |
| `52` | `2` | `1` | `1` | `10` | `0` | `-1` | `-1` | `1` | Attacker-side special; `[6]` likely unused. |
| `58` | `2` | `1` | `1` | `10` | `0` | `-1` | `-1` | `1` | Same late attacker-side special shape as `52`. |

Important caution:

- Source only calls `actor.d(O[J*7+6])` inside a guard where
  `O[...+5] != -1`.
- Therefore `[6] != 0` by itself is not enough to claim a state change.
- Rows with `[5] == -1` and `[6] == 1` must be treated as `PENDING/LIKELY_UNUSED`
  until bytecode path proves otherwise.

## Current Rebuild Gap Matrix

| Source behavior | Current rebuild equivalent | Status |
| --- | --- | --- |
| `game.d.n()` starts chunk `J = I`, increments `I` | P7 starts at chunk `0`, advances sequentially | PORTED/PARTIAL |
| Special `chunk[1] == 1` creates `H = new ah()` | Type `9` and type `1` are rendered for skill `45`; other AH types pending | PORTED/PARTIAL |
| Actor animation `chunk[1] == 0` calls actor `a(anim,param)` | Renderer uses `battleP7EffectAnimState` for simple actor animation | PARTIAL |
| `chunk[4]` can advance next chunk at frame trigger | Not modeled | PENDING |
| `chunk[5]/[6]` can change actor state at frame trigger | Not modeled | PENDING |
| `H.i()` gates special effect start | Not modeled exactly; current rebuild starts by phase/chunk | PARTIAL |
| `H.e()` ticks and returns false on completion | Rebuild uses source-shaped duration per AH type, not real `H.e()` object state | PARTIAL |
| `actor.b(false/true)` hide/disable during `H` | Not modeled | PENDING |
| `M/N/L/K/z/A/B` sequencing flags | Not modeled exactly | PENDING |
| No-damage skill rows `aq.c[1][skill][3] == 0` | Skill `45` no-damage fixed | PORTED for skill `45`, partial globally |

## Candidate Hooks To Port First

Do not port all hooks at once. Recommended slices:

### Slice 1: `chunk[4]` next-trigger for actor animation chunks

Candidate:

```text
skill 15 effect.mid row:
[0,0,33,0,0,-1,0, 0,1,7,0,-1,-1,0]
```

Why:

- Starts with non-special actor animation chunk.
- `chunk[4] == 0` should immediately advance to the next chunk when
  `u.a(0)` fires.
- This tests early chunk transition without also needing `[5]/[6]`.

Risk:

- Need a smoke route/pet skill seed for skill `15`.
- Must avoid making all `chunk[4] == 0` transitions immediate unless the
  actor animation frame trigger model is source-backed.

### Slice 2: `[5]/[6]` state trigger

Candidate:

```text
skill 37 effect.mid row:
[0,0,23,0,-1,-1,0, 0,1,7,0,-1,0,0, 0,1,6,0,-1,-1,0]
```

Why:

- Has `chunk[5] == 0` and `chunk[6] == 0`.
- Tests actor state change trigger guard.

Risk:

- Source branch differs depending on attacker/target active animation `u`.
- Need to know whether current chunk has active actor animation or `H`.

### Slice 3: Attacker-side special after trigger behavior

Candidate:

```text
skill 52 effect.mid row:
[0,0,25,1,-1,-1,0, 0,1,8,0,-1,-1,0, 1,1,10,0,-1,-1,1]
```

Why:

- Includes attacker-side special chunk after target-side chunks.
- Good after `chunk[4]/[5]` are better modeled.

Risk:

- `[6] == 1` is likely unused because `[5] == -1`.
- Do not treat it as a state trigger without proof.

## Recommended Next Implementation Plan

Before code:

1. Add a focused audit for candidate skill `15` or `37`:
   - `aq.c[1]` row
   - `effect.mid` row
   - `speffect.mid` row if any
   - expected side/target/attacker
   - expected damage/no-damage
2. Add smoke checkpoint that seeds a pet with that skill and reaches P7.
3. Record baseline PNG before changing runtime behavior.

Implementation slice:

1. Add a small P7 frame-trigger abstraction in `VqsvBattleRuntime`.
2. Start with `chunk[4]` only.
3. Only after `chunk[4]` smoke is stable, port `[5]/[6]`.

Required regression:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java source mojibake scan
battle_elder_p7_speffect45_start
battle_elder_p7_speffect45_overlay
battle_elder_p7_speffect45_type1
battle_elder_p7_speffect45_after
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
```

## Honest Conclusion

This audit closes the source understanding enough to plan the next code slice,
but the hooks are not implemented yet.

Current truth:

- Skill `45` chunk0/chunk1 and no-damage behavior remain source-shaped and smoke
  backed.
- `chunk[4]/[5]/[6]`, exact `H.i()` start gating, actor hide/restore, and
  source flags `M/N/L/K/z/A/B` remain PARTIAL/PENDING.
- The next safest code step is a focused `chunk[4]` trigger slice using a skill
  with a visible smoke route, not a broad rewrite of P7.
