# 341 - Battle Skill + Animation Current Roadmap

Date: 2026-07-14

Status: ACTIVE ROADMAP / VISUAL PARITY BLOCKER.

This roadmap exists because the project is at risk of looping: skill logic keeps
getting added, but the user-visible battle animation still looks wrong. The
work must now split into two explicit lanes with gates.

## Current Reality

### What Is Relatively Solid

- Battle state backbone exists: P20/P3/P6/P7/P8/P9 plus item/catch/pet switch
  slices.
- Damage result flow has hit/miss/crit and forced smoke coverage.
- Buff/debuff tables have broad numeric coverage.
- Held items 0..11 have significant smoke coverage.
- Skill rows are increasingly audited by source data.
- Fire/Wood/Earth skill classes exist and smoke harnesses were split out of the
  monolithic smoke file.

### What Is Not Solid

- Visual battle animation parity is REOPENED.
- Fire skill visual closeouts are PARTIAL, not done.
- Pet action states and skill actor effects still do not feel like the original.
- Many skills appear visually identical in smoke/live testing.
- AH effects are source-shaped but not pixel-parity.
- Actor `u` effect timing/draw order may still differ from original MIDP.
- Hit/recover/dead state timing is not fully closed.

The user has explicitly said the animation is still wrong. Treat that as the
current primary bug.

## Two Active Lanes

### Lane A - Skill Logic Parity

Purpose: prove what each skill does numerically and in battle state.

For every skill:

1. Audit source rows:
   - `aq.c[1][skill]`
   - `effect.mid[skill]`
   - `speffect.mid` rows used by `effect.mid`
   - `aq.c[6]` buff or `aq.c[7]` debuff rows if applicable
   - Vietnamese source name/description from `chs.mid`
2. Explain plain behavior.
3. Prove parameters:
   - power
   - PP
   - target side
   - chance
   - buff/debuff id
   - duration
   - numeric formula
4. Smoke before/during/after:
   - before: HP/PP/effects before confirm
   - during: P7 actor/H frame and damage text frame
   - after: settled HP/PP/effects
   - expiry: if buff/debuff applies
5. Add/update battle lab entry only after smoke is meaningful.

Status: CONTINUE ONLY AFTER Lane B gate for the relevant animation family.

### Lane B - Animation / Visual Parity

Purpose: prove the shared renderer/runtime plays pet and skill animation in the
same source order and timing.

This lane has priority now.

Required areas:

1. P7 actor action `u` pipeline:
   - `game.d.n()`
   - `game.b.a(short,byte)`
   - `ah.java` actor ids `20..34`
   - `d.java` sprite actor animation timing
   - `game.b.a(Graphics)` draw order
   - rebuild `P7ActorAnimation`
   - rebuild `drawP7ActorEffect`
2. Pet base action states:
   - attacker state
   - target hit state
   - recover state
   - dead state
   - state reset to idle
3. AH special effects:
   - type 1
   - type 7
   - type 8
   - type 9
   - type 12
   - any additional type used by current skill rows
4. Body-attached status effects:
   - P12/P13 active queue visuals
   - buff/debuff icon and duration slots
5. Blood/damage text:
   - damage number timing
   - crit text/timing
   - miss text/timing
   - HP bar tween vs final settled HP

Status: BLOCKING full skill visual closeout.

## Gates

### Gate 0 - No Client By Default

Default validation is PNG/headless only. Do not open the live client unless the
user explicitly asks.

### Gate 1 - Animation Family Before Skill Family

Before closing a skill family, close or classify its animation family:

| Skill family | Animation family gate |
|---|---|
| Fire direct skills 0/1/3/6/7/9 | `u20/state0` actor action timing/draw order |
| Fire skills 2/8 | `u20/state0` plus `speffect14/AH12` |
| Fire self-buffs 4/5 | `speffect16/AH9` plus `speffect15/AH1` |
| Wood direct actor skills | their actor id/state rows |
| Earth direct actor skills | their actor id/state rows |
| No-damage buffs | producer H/u visual plus P12/P13 active visual |

If the animation family is PARTIAL, the skill can be LOGIC-PORTED but not
VISUAL-PORTED.

### Gate 2 - Frame Strip Must Show Correct Side

Frame strips must not crop the wrong side:

- target damage effects: enemy/player target crop depending on target;
- self-buffs: caster/player crop;
- multi-target or random-target effects: full frame plus target crop;
- active queue body effect: affected unit crop.

### Gate 3 - Do Not Hide Identical Source Rows

If source rows are identical, report that clearly. But do not use that as proof
that the full live presentation is done. Later chunks, state transitions, hit
recover, body effects, and UI text can still differ.

## Immediate Phase Plan

### Phase B0 - Freeze Skill Expansion

Status: ACTIVE.

Do not add new skill closeouts until shared animation audit has a clean result.
Exception: smoke-only diagnostics that help animation audit are allowed.

Done when:

- The new chat can explain why previous Fire skill visual closeouts are reopened.
- The new chat can show current frame-strip PNG and identify which parts are
  source-backed vs still visually wrong.

### Phase B1 - Actor `u` Pipeline Audit

Create:

```text
342_battle_p7_actor_sprite_timing_draw_order_matrix.md
```

Matrix columns:

| Source method/file | Meaning | Rebuild equivalent | Current status | Evidence | Gap |
|---|---|---|---|---|---|

Must include:

- `game.d.n()`
- `game.d` P7 update case
- `game.b.a(short,byte)`
- `game.b.a(Graphics)`
- `ah.java` actor action fallback
- `d.java a(byte,byte,boolean)`
- `d.java d()/e()/b(frame)`
- `VqsvBattleRuntime.P7ActorAnimation`
- `VqsvBattleRenderer.drawP7ActorEffect`
- `VqsvSpriteRenderer.SpriteAnim.tickHoldLast`

Done when:

- We know whether `SpriteAnim.tickHoldLast()` matches source `d.d()`.
- We know whether draw order matches `game.b.a(Graphics)`.
- We know whether target/caster base states are set at the same source trigger.
- We know whether the next code patch is justified.

### Phase B2 - Patch Actor Timing/Draw Order Only If Proven

Allowed patches:

- shared actor animation timing;
- shared actor draw order;
- shared target hit/recover state timing;
- shared frame trigger `[4]/[5]/[6]` handling;
- smoke harness side/crop correctness.

Forbidden patches:

- per-skill fake visual differences;
- hardcoded Fire-only offsets unless source proves Fire-only behavior;
- visual debug overlays in release path.

Done when:

- `battle_fire_live_frame_strip` clearly shows source-stage frames.
- `battle_fire_source_stage_animation` proves attacker/target/hit/idle states.
- `battle_quick` still passes.

### Phase B3 - AH Special Re-audit

Focus order:

1. AH type 12: `speffect14`, used by Fire skill 2/8 and other debuff visuals.
2. AH type 1: `speffect15`, used by Fire skill 4/5.
3. AH type 9: `speffect16`, used by Fire skill 4/5.
4. AH type 7/8 if current skill lane uses them.

Done when:

- each AH type has a source row matrix;
- renderer behavior is source-shaped;
- frame strip shows the effect on the correct actor/crop;
- remaining pixel/color gaps are documented.

### Phase B4 - Revalidate Fire Lane

Only after B1/B2/B3:

- Reopen Fire skills 0..9 one by one.
- For each skill, report two statuses:
  - Logic status
  - Visual status
- If source first chunk is identical, say so.
- If visual still looks wrong, keep PARTIAL.

Done when:

- Fire contact sheet is useful by eye.
- Skill 2/8 AH12 is visible enough to judge.
- Skill 4/5 self-buff is visible on player crop.
- The user can inspect PNGs and decide whether visual is acceptable.

### Phase C - Resume Skill Lanes

After Fire lane revalidation:

1. Continue Earth lane from the last safe point.
2. Re-audit Wood/Earth actor ids before trusting prior closeouts.
3. Use the same before/during/after structure.
4. Add battle lab entries only after smoke is useful and not misleading.

## Current Recommended Next Prompt

```text
Create 342_battle_p7_actor_sprite_timing_draw_order_matrix.md.
Audit source-first:
- game.d P7 update case and draw case;
- game.b.a(short,byte), game.b.a(Graphics), pet states d(byte)/p()/b();
- ah.java actor action ids 20..34;
- d.java animation timing d()/e()/b(frame)/a(Graphics);
- rebuild P7ActorAnimation, SpriteAnim.tickHoldLast, drawP7ActorEffect.
No client. No code unless the matrix proves a mismatch.
Report which exact mismatch should be patched first.
```

## Reporting Format For Each Slice

Use this structure:

```text
Source facts:
- ...

What I changed:
- ...

Smoke PNG:
- path
- what to look at

Result:
- PORTED:
- PARTIAL:
- PENDING:

Regression:
- build
- smoke suite
- --check
- formula
- mojibake scan

Next roadmap step:
- ...
```

## Current Regression Baseline

Last known passing commands:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_u20_offset_fix
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_u20_offset_fix
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\battle_quick_after_u20_offset_fix
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Há»|Ä" src\main\java
```

`rg` returning exit code `1` with no output means no mojibake matches and is OK.
