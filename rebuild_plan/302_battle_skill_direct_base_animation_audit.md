# 302 Battle Skill Direct Base Animation Audit

Status: AUDIT_ONLY / NO_CODE_PATCH.

This audit starts Phase Skill-A from `301_battle_skill_grouped_logic_animation_roadmap.md`.
The goal is to prove the source path for representative direct base damage skills
before patching any visual/animation runtime.

For the current full-skill catalog across all `aq.c[1][0..69]` rows, use
`303_battle_all_skill_source_logic_animation_audit.md`. This file is only the
representative direct-base animation audit.

## Scope

Representative direct base skills:

```text
0, 10, 20, 30, 40, 50, 60
```

These cover the first base direct attack row for each element lane in
`aq.c[1]`. This audit intentionally does not cover:

- debuff/buff producer semantics already closed in the buff/debuff table docs;
- zero-power special skills;
- full pixel-perfect actor cursor parity;
- skill `50` special AH type9 patching, because that row has an extra special
  chunk and should be a separate visual slice.

## Plain Mechanism

For these skills, the source flow is:

```text
selected skill
  -> aq.c[1][skill] gives skill metadata and damage power
  -> game.d state P7
  -> game.d.n() loads effect.mid[skill]
  -> each 7-value effect chunk creates either:
       actor action u, if chunk[1] == 0
       special AH H, if chunk[1] == 1
  -> P7 waits for the actor/special effect sequence
  -> damage number / HP mutation / hit state happen after the visual gate
```

So the visual pass is not just "draw a hit". The hit and HP change must wait
until the current `effect.mid` sequence allows P7 to finish.

## Player-Facing Skill Matrix

Names and descriptions below are source-backed from:

- `aq.c[1][skill][1]`: name text id in `chs.mid`;
- `aq.c[1][skill][2]`: description text id in `chs.mid`.

All seven representatives are ordinary direct damage skills with source text
`Thương tổn thấp.`. In plain language: they are the first/basic attack for
their element lane. They should spend PP, play a short P7 attack animation,
then apply normal direct damage to the target if the hit path succeeds. They
should not apply buff/debuff side effects because `aq.c[1][skill][6..9]` is
`0,-1,-1,0`.

| Skill | Source name | Source description | Easy explanation | Source logic | P7 animation / visual | Current status |
| ---: | --- | --- | --- | --- | --- | --- |
| 0 | Hỏa trảo | Thương tổn thấp. | Basic fire-lane claw hit. It is a simple low-damage attack with no status effect. | `aq.c[1][0]` has power `100`, PP `45`, effect mode `0`; P7 uses `effect.mid[0]`; damage is ordinary `game.b.b(target)` direct-hit flow. | One actor `u` chunk: effect id `20`, state `0`, sprite `262`; no special AH. | PORTED/PARTIAL: source row known, exact actor cursor parity pending. |
| 10 | Diệp Toàn | Thương tổn thấp. | Basic leaf-lane swirl hit. This is the best first polish target because Elder route already uses it. | `aq.c[1][10]` has power `100`, PP `45`, effect mode `0`; P7 uses `effect.mid[10]`; HP must not drop until the actor animation gate completes. | One actor `u` chunk: effect id `21`, state `1`, sprite `263`; no special AH. | PORTED/PARTIAL: audit `68` proved sprite/state; exact frame timing still pending. |
| 20 | Hất bụi | Thương tổn thấp. | Basic dust/earth-lane hit. Direct damage only. | `aq.c[1][20]` has power `100`, PP `45`, effect mode `0`; ordinary direct damage path. | One actor `u` chunk: effect id `22`, state `0`, sprite `264`; no special AH. | PORTED/PARTIAL: source row known, exact actor cursor parity pending. |
| 30 | Bong bóng | Thương tổn thấp. | Basic water-lane bubble hit. Direct damage only. | `aq.c[1][30]` has power `100`, PP `45`, effect mode `0`; ordinary direct damage path. | One actor `u` chunk: effect id `23`, state `0`, sprite `265`; no special AH. | PORTED/PARTIAL: source row known, exact actor cursor parity pending. |
| 40 | Điện giật | Thương tổn thấp. | Basic electric-lane shock hit. Direct damage only. | `aq.c[1][40]` has power `100`, PP `45`, effect mode `0`; ordinary direct damage path. | One actor `u` chunk: effect id `24`, state `0`, sprite `266`; no special AH. | PORTED/PARTIAL: source row known, exact actor cursor parity pending. |
| 50 | Ảnh thứ | Thương tổn thấp. | Basic shadow-lane hit, but visually it is not as simple as the others because it adds a special effect after the actor action. | `aq.c[1][50]` has power `100`, PP `45`, effect mode `0`; ordinary direct damage path, but P7 visual row has two chunks. | Chunk0 actor `u`: effect id `25`, state `0`, sprite `267`; chunk1 special `H`: `speffect9 = [9,120,69,27,133,0,2,2]`. | PENDING_AUDIT for visual polish: do not merge with first skill10 slice. |
| 60 | Phong nhận | Thương tổn thấp. | Basic wind-lane blade hit. Direct damage only. | `aq.c[1][60]` has power `100`, PP `45`, effect mode `0`; ordinary direct damage path. | One actor `u` chunk: effect id `26`, state `0`, sprite `268`; no special AH. | PORTED/PARTIAL: source row known, exact actor cursor parity pending. |

## Source Logic In Detail

For this direct-base family, the important `aq.c[1]` columns are:

| Column | Meaning in current rebuild/source audit | Value for these representatives |
| ---: | --- | --- |
| `0` | element/type lane | `0..6` |
| `1` | skill name text id | `117,127,137,147,157,167,177` |
| `2` | skill description text id | `529,539,549,559,569,579,589` |
| `3` | power percent / direct damage power | `100` |
| `5` | max PP | `45` |
| `6` | effect mode | `0`, ordinary direct damage/no producer side effect |
| `7` | effect id / buff/debuff id | `-1`, none |
| `8` | chance or parameter | `-1`, none |
| `9` | target side / effect side | `0`, target-side/direct target |

Runtime order from source:

1. Battle selects a skill and stores it as `h.D`.
2. P7 starts and calls `game.d.n()`.
3. `game.d.n()` loads `effect.mid[h.D]` into `O`.
4. If a chunk has `chunk[1] == 0`, source calls actor action
   `target.a(effectId, state)` or `attacker.a(effectId, state)`.
5. If a chunk has `chunk[1] == 1`, source creates AH special object `H`.
6. P7 waits for actor `u` / special `H` to finish or hit frame triggers to fire.
7. Only after the visual gate does the source direct-hit path show damage text,
   mutate target HP, and set hit/death/recover state.

This is why the next code slice must prove both image timing and numeric timing:
the animation frame and the HP frame are tied together by P7.

## Source Files And Tables

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__chs.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`
- `modules/script/decoded/data__script__speffect.mid.json`

Source `game.d.n()`:

```text
O = ao[h.D]
if O[J * 7 + 1] == 1:
    H = new ah()
    H.a(speffect.mid row expanded with actor position/species/direction)
else if O[J * 7] == 0:
    target.a(effectId, state)
else:
    attacker.a(effectId, state)
I++
```

Source `ah` default actor-action branch maps effect ids `20..34` through:

```text
[262,263,264,265,266,267,268,299,300,301,304,306,307,308,309]
```

Therefore:

```text
20 -> sprite 262
21 -> sprite 263
22 -> sprite 264
23 -> sprite 265
24 -> sprite 266
25 -> sprite 267
26 -> sprite 268
```

## Representative Source Matrix

`aq.c[1]` rows are from `data__script__db.mid.json` group `1`.
`effect.mid` rows are from `data__script__effect.mid.json`.

| Skill | aq.c[1][skill] | effect.mid[skill] | P7 chunks | Actor sprite(s) | Special AH | Audit note |
| ---: | --- | --- | ---: | --- | --- | --- |
| 0 | `[0,117,529,100,0,45,0,-1,-1,0]` | `[0,0,20,0,-1,-1,0]` | 1 | `20 -> 262`, state `0` | none | Simple direct base actor action. |
| 10 | `[1,127,539,100,0,45,0,-1,-1,0]` | `[0,0,21,1,-1,-1,0]` | 1 | `21 -> 263`, state `1` | none | Best first patch candidate; Elder route/smoke already exercises it. |
| 20 | `[2,137,549,100,0,45,0,-1,-1,0]` | `[0,0,22,0,-1,-1,0]` | 1 | `22 -> 264`, state `0` | none | Simple direct base actor action. |
| 30 | `[3,147,559,100,0,45,0,-1,-1,0]` | `[0,0,23,0,-1,-1,0]` | 1 | `23 -> 265`, state `0` | none | Simple direct base actor action. |
| 40 | `[4,157,569,100,0,45,0,-1,-1,0]` | `[0,0,24,0,-1,-1,0]` | 1 | `24 -> 266`, state `0` | none | Simple direct base actor action. |
| 50 | `[5,167,579,100,0,45,0,-1,-1,0]` | `[0,0,25,0,-1,-1,0, 0,1,9,0,-1,-1,0]` | 2 | `25 -> 267`, state `0` | `speffect9 = [9,120,69,27,133,0,2,2]` | Not first patch; needs actor plus AH type9 sequencing. |
| 60 | `[6,177,589,100,0,45,0,-1,-1,0]` | `[0,0,26,0,-1,-1,0]` | 1 | `26 -> 268`, state `0` | none | Simple direct base actor action. |

Shared metadata pattern:

- row column `0` is the element/type lane for these representatives;
- row column `3` is `100`, so they are ordinary damage-power rows;
- row column `5` is `45`, matching the base direct family in `301`;
- row columns `6..9` are `0,-1,-1,0`, so these representatives do not
  intentionally produce buff/debuff rows.

## Current Rebuild Mapping

Relevant runtime:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
  - `enterP7SourceChunk(...)`
  - `tickP7SourceEffectSequence(...)`
  - `tickP7ActorAnimation(...)`
  - `tickP7SpecialEffect(...)`
  - `applyP7Damage(...)`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
  - `drawP7Damage(...)`

Current rebuild behavior:

- loads source `effect.mid` rows through `VqsvBattleAnimationTables`;
- treats 7-value chunks as source-shaped P7 chunks;
- creates `P7ActorAnimation` for `chunk[1] == 0`;
- creates special AH rendering for supported `chunk[1] == 1` types;
- applies P7 damage after the source-effect sequence;
- draws damage text using decoded `blood.mid` timing and source-shaped
  placement from audit `134`.

Existing skill10 status from audit `68`:

- `effect.mid[10] = [0,0,21,1,-1,-1,0]`;
- effect id `21` maps to sprite `263`;
- actor state is `1`;
- route smoke has checkpoints around actor start / hit trigger / recover;
- still `PORTED/PARTIAL`, because exact MIDP actor cursor timing is not fully
  proven against original-client frame capture.

## Gaps / Risks

| Area | Status | Why |
| --- | --- | --- |
| Direct base damage formula | PORTED/PARTIAL | Existing battle formula is source-shaped, but this audit is animation-focused. |
| P7 chunk loading | PORTED/PARTIAL | Rebuild follows 7-value chunks; complex multi-chunk rows still need per-row proof. |
| Single actor `u` rows 0/10/20/30/40/60 | PORTED/PARTIAL | Actor sprite ids are source-backed; exact cursor/frame parity remains pending. |
| Skill50 actor plus AH type9 | PENDING_AUDIT | It has a second special chunk and must not be merged into the first simple slice. |
| Damage text / HP timing | PORTED/PARTIAL | Source-shaped from `134`; no pixel-perfect original-vs-rebuild compare yet. |
| Pixel-perfect claim | PENDING | No original frame capture compare for this phase. |

## First Patch Candidate

Recommended first implementation/smoke slice:

```text
skill10 direct base animation timing
```

Why skill10:

- source row is a single chunk with no special AH:
  `[0,0,21,1,-1,-1,0]`;
- it already appears in Elder-related route/smoke coverage;
- effect id `21 -> sprite 263` is already source-proven in audit `68`;
- state `1` gives a useful non-zero actor state test without extra chunk
  triggers;
- fixing this path improves the common direct-base actor runtime without
  touching debuff/buff gameplay.

Do not start with skill50. It is valuable, but it needs a separate two-chunk
actor-plus-special audit:

```text
chunk0 actor: [0,0,25,0,-1,-1,0]
chunk1 AH:    [0,1,9,0,-1,-1,0]
```

## Proposed Smoke Plan For First Code Slice

Focused skill10 PNG checkpoints:

```text
battle_skill10_direct_before
battle_skill10_direct_actor_u21_start
battle_skill10_direct_actor_u21_mid
battle_skill10_direct_damage_frame
battle_skill10_direct_recover_or_finish
```

Required numeric/state assertions:

- skill id is `10`;
- `effect.mid` row equals `[0,0,21,1,-1,-1,0]`;
- P7 source chunk index starts at `0`;
- actor effect id is `21`;
- actor sprite id is `263`;
- actor state is `1`;
- HP is unchanged before the actor visual gate;
- HP decreases only at the P7 damage frame;
- battle exits/continues through the existing result state without changing
  route behavior.

Optional direct-base representative suite after skill10:

```text
battle_phase_skill_a_direct_0
battle_phase_skill_a_direct_10
battle_phase_skill_a_direct_20
battle_phase_skill_a_direct_30
battle_phase_skill_a_direct_40
battle_phase_skill_a_direct_50_actor_only
battle_phase_skill_a_direct_60
```

For skill50, the first smoke should explicitly separate actor chunk and AH type9
chunk so that it cannot accidentally pass by only drawing one of the two.

## Verification Required After Future Patch

After the first code slice, run:

```text
rebuild_game/build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java source mojibake scan
git diff --check
focused skill10 PNG smoke
battle_quick or the current fixed battle regression suite
```

Also show the user three things, following the current skill/debuff workflow:

- PNG before/during/after;
- numeric state before/during/after, especially HP and actor effect metadata;
- final audit note saying what is source-backed, what is still partial, and
  whether any intentional deviation exists.

## Current Decision

No code is patched by this audit.

Next concrete step:

```text
Implement the smallest skill10 direct base animation checkpoint/timing slice,
then smoke before/during/after. Do not touch skill50 or broad skill logic yet.
```
