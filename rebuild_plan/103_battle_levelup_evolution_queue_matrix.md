# 103 Battle LevelUp Evolution Queue Matrix

Status: SOURCE AUDIT + SLICE 1 QUEUE PRODUCER IMPLEMENTED + SLICE 2 WORLD NOTICE CONSUMER IMPLEMENTED/PARTIAL.

This audit follows `102_new_dev_chat_handoff_battle_engine_after_levelup.md`.
The current rebuild has P8/P22/P23 EXP, level-up, and learn-skill slices, but
evolution/mutation is still mostly `PENDING`. This document proves why
evolution is a separate queue/UI path, records the implemented queue-producer
slice, and defines the next safe implementation slices.

Status key: `PORTED`, `PORTED/PARTIAL`, `APPROX`, `STUB`, `PENDING`,
`UNKNOWN`, `DAMAGED`.

## Scope

Audit only:

- `game.b.v()` after level-up.
- `game.b.J()` evolution queue producer.
- `game.k.H/L/I` queue consumer and tutorial/menu bridge.
- `game.h.bg()/bh()` `/data/ui/evolve.ui` UI/effect path.
- Resource tables used by the source path.
- Current rebuild gap and safe smoke plan.

Out of scope for the implemented Slice 1:

- Do not open or render `evolve.ui`.
- Do not invent an evolution animation.
- Do not reuse `levelUp.ui` as evolution UI.
- Do not touch intro/world/panel scripts.

## Files Read

Source:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

Resources:

- `modules/script/decoded/data__script__db.mid.json`
- `modules/ui/decoded/data__ui__evolve.ui.json`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`

Rebuild context:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleLevelUpView.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Source Chain Summary

Evolution is not part of the visible `levelUp.ui` overlay itself.

Source path:

```text
P8 EXP reaches threshold
-> game.d state 22
-> game.h.an()
-> game.b.v()
   -> level++
   -> subtract EXP threshold
   -> game.b.J()
      -> may enqueue game.k.H
      -> set game.k.L[0/1]
      -> set game.k.I = 0
-> game.h.ao()
   -> possibly P23 learn skill
   -> battle exits to world
-> game.k world/menu tick consumes game.k.H/L/I
-> game.h msgwarm/evolve prompt path
-> game.h.bg()/bh()
   -> /data/ui/evolve.ui
   -> ah effect
   -> mutate pet payload
```

Rebuild status:

- P8/P22/P23 active-pet slice: `PORTED/PARTIAL`.
- Evolution queue producer: `PORTED/PARTIAL`.
- Evolution queue world notice consumer: `PORTED/PARTIAL`; see
  `105_battle_evolution_world_notice_consumer_matrix.md`.
- Evolution `msgwarm.ui` prompt parity, `evolve.ui`, effect, and payload
  mutation: `PENDING`.
- Current `VqsvBattleRuntime.tickWinExpLevelUp()` traces
  `evolutionQueue=created` when the source-shaped `game.b.J()` equivalent
  creates a notice.

## Producer Matrix: `game.b.v()` and `game.b.J()`

Source `game.b.v()`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Increase level | `++this.T` | `PORTED/PARTIAL` in `BattleUnit.sourceLevelUpOnce()`. |
| Subtract threshold | `this.g(-game.b.A(this.T))` | `PORTED/PARTIAL`; rebuild subtracts `sourceLevelThreshold(level)`. |
| Enqueue evolution | `this.J()` | `PORTED/PARTIAL`; `SourceBattleRuntime` calls the queue producer immediately after level-up and fills `sourceEvolutionQueue/L/I`. |
| Restore PP | loops skills and restores `y[i] = aq.c[1][skill][5]` | `PORTED/PARTIAL`. |
| Refresh stats | `this.V()` | `PORTED/PARTIAL`. |

Source `game.b.J()`:

| Source field/table | Meaning | Behavior |
| --- | --- | --- |
| `game.k.H` | global evolution notice queue | Created if null. |
| `aq.c[0][species][19]` | evolution target species | If `-1`, return. |
| `aq.c[0][species][20] + 12` | required material/special item id | Used with `game.g.o().a(id, 2)`. |
| `aq.c[0][species][21]` | required material count | Compared against current count. |
| `game.b.t[aq.c[0][target][2] - 1]` | required level by target kind/type | Pet level must be high enough. |
| `this.R() > 0` | source eligibility/form check | Required before enqueue. |
| `game.k.K` | tutorial/evolution gate | Appears in the first material-count branch. |
| `game.k.L[0]` | source queued level | Set to current pet level. |
| `game.k.L[1]` | source queued species | Set to current species. |
| `game.k.I` | queue state flag | Set to `0` so `game.k` can consume. |

If conditions pass, source enqueues:

```text
game.k.H.addElement(new int[]{currentSpecies, aq.c[0][currentSpecies][0]})
game.k.L[0] = currentLevel
game.k.L[1] = currentSpecies
game.k.I = 0
```

Classification:

- Source producer behavior: `SOURCE-MAPPED`.
- Rebuild queue equivalent: `PORTED/PARTIAL` for producer/data shape, `PENDING`
  for world consumer and UI/effect.
- Important source oddity: the first branch checks material count when
  `!game.k.K`, but the fallback branch still enqueues when `R() > 0` and level
  is high enough. Material count belongs to the later `game.h.bh()` confirm
  path, not as a hard block for the queue producer.
- Full condition parity for `R()`: `PENDING` until broader pet form semantics
  are wired. Material inventory parity is stored as metadata for later confirm
  UI.

## Species / Evolution Data Samples

Decoded from `modules/script/decoded/data__script__db.mid.json`, group `0`.

| Species | Current route relevance | Target col19 | Material base col20 | Material need col21 | Kind col2 | Visual col17 | Status |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | --- |
| `0` | current early active pet fixture in several smokes | `-1` | `-1` | `-1` | `4` | `86` | No evolution target. Current route may not naturally reach evolution. |
| `6` | candidate fixture | `7` | `0` | `1` | `6` | `92` | Has target; good smoke candidate after source gate audit. |
| `17` | candidate fixture | `18` | `1` | `1` | `1` | `103` | Has target; good smoke candidate. |
| `54` | scripted/catch related species seen in source audits | `-1` | `-1` | `-1` | `0` | `140` | No evolution target. |
| `75` | L-effect smoke species | `-1` | `-1` | `-1` | `0` | `161` | No evolution target. |
| `87` | L-effect smoke species | `-1` | `-1` | `-1` | `0` | `173` | No evolution target. |
| `91` | L-effect smoke species | `92` | `0` | `2` | `0` | `177` | Has target; useful fixture but material need differs. |
| `92` | pet selection/smoke species | `-1` | `-1` | `-1` | `1` | `178` | No further target. |
| `97` | L-effect smoke species | `98` | `3` | `1` | `0` | `183` | Has target; useful fixture. |
| `98` | L-effect smoke species | `-1` | `-1` | `-1` | `3` | `184` | No further target. |

Conclusion:

- The current early route pet species `0` does not prove an evolution path.
- A deterministic smoke fixture will probably be needed for the first evolution
  queue implementation slice.
- Do not claim story route evolution is reachable until a route/source state
  proves a species with `col19 != -1` reaches level/material requirements.

## World Consumer Matrix: `game.k.H/L/I`

Source `game.k` static fields:

| Field | Meaning in audited path | Source behavior |
| --- | --- | --- |
| `game.k.H` | vector of queued evolution notices | Filled by `game.b.J()`, consumed in world/menu tick. |
| `game.k.I` | queue state flag | `0` means pending notice queue can be consumed; set `1` after queue exhausted. |
| `game.k.L[0]` | queued source level | Used by tutorial/menu bridge to find the evolved-capable pet. |
| `game.k.L[1]` | queued source species | Used by tutorial/menu bridge to find the pet. |
| `game.k.K` | tutorial/evolution gate | Affects whether detailed prompt path runs. |

World/message consumer:

| Condition | Source behavior | Status |
| --- | --- | --- |
| `P == 0` | only in normal world/menu state. | SOURCE-MAPPED. |
| `!M.h()` | no blocking map/event state. | SOURCE-MAPPED/PARTIAL naming. |
| `game.k.I == 0` | evolution queue is pending. | SOURCE-MAPPED. |
| `game.k.H != null && H.size() > 0` | there are pending notices. | SOURCE-MAPPED. |
| `S.ax()` | message/openbox gate ready. | SOURCE-MAPPED; exact UI runtime still `PENDING`. |
| queue element `{species, nameText}` | show `Tien hoa` or `Di hoa` based on target species kind col2. | SOURCE-MAPPED. |
| last element and `L[0] != -1` | opens `msgwarm.ui` using `S.E()` and `S.a(text,prompt)`. | SOURCE-MAPPED. |
| other elements | uses `S.b(text)` openbox/message helper. | SOURCE-MAPPED/PARTIAL; exact helper UI needs further audit. |
| queue exhausted | clears `H`, resets `ac`, sets `I = 1`. | SOURCE-MAPPED. |

Tutorial/menu bridge:

- Another `game.k` branch checks:

```text
!M.h() && !S.G() && !game.k.K && game.k.L[0] != -1 && k(32)
```

- It then sets tutorial state `U = 4`, `K = true`, resets `S.c`, enters state
  `7`, and closes `msgwarm.ui` with `S.F()`.
- The later tutorial state `U == 4` uses `L[0]/L[1]` to locate the pet and
  instruct the player to select it for evolution.

Classification:

- Queue notice consumer: `SOURCE-MAPPED`.
- Exact state labels and complete tutorial path: `PARTIAL/PENDING`.
- Rebuild world consumer: `PENDING`.

## UI Consumer Matrix: `game.h.bg()` / `game.h.bh()`

`game.h.bg()` opens the evolution panel.

| Source step | UI/resource | Widget ids / data | Status |
| --- | --- | --- | --- |
| Set mode | local `f = 2`, `r = 0` | starts evolve panel mode | SOURCE-MAPPED. |
| Open UI | `/data/ui/evolve.ui`, sprite `257` | closes `/data/ui/petsetting.ui` and `/data/ui/petstate.ui` | SOURCE-MAPPED. |
| Current pet sprite | widget `10` | current pet sprite `q.z[b].C` | SOURCE-MAPPED. |
| Current pet name | widget `38` | `an.f(aq.c[0][species][0])` | SOURCE-MAPPED. |
| Current pet level | widget `40` | `pet.s()` | SOURCE-MAPPED. |
| Required material name | widget `45` | `an.f(aq.c[3][materialId][0])` | SOURCE-MAPPED. |
| Required material count | widget `46` | current/required | SOURCE-MAPPED. |
| Old stats | widgets `19..22` | current pet stats `c[1..4]` | SOURCE-MAPPED. |
| New stats | widgets `31..34` | temp target species stats at same level | SOURCE-MAPPED. |

`game.h.bh()` handles confirm/effect/result.

| Source branch | Behavior | Status |
| --- | --- | --- |
| `game.k.n != null` and effect not done | tick/wait source `ah` effect | SOURCE-MAPPED/PENDING renderer parity. |
| effect complete | update widget `10` to new target sprite, update name, create new `game.b` target payload, apply to pet with `P()`, update materials, show success msgwarm | SOURCE-MAPPED. |
| confirm when no target | show cannot evolve/mutate warning | SOURCE-MAPPED. |
| confirm when level ok and material enough | hide widget `10`, create `game.k.n = new ah()`, start effect, consume material | SOURCE-MAPPED. |
| confirm when material missing | show material warning | SOURCE-MAPPED. |
| confirm when level too low | show level warning | SOURCE-MAPPED. |
| msgwarm confirm | if source owner state `Q` is `6` or `0`, close warning and call `o.m()`; if `Q == 27`, reload petstate and close evolve UI | SOURCE-MAPPED/PARTIAL owner-state labels. |
| back | closes `/data/ui/evolve.ui`, returns petstate | SOURCE-MAPPED. |

Important: this is a separate UI path from battle `levelUp.ui`. Rebuild must not
fake evolution inside P22 unless a source bridge proves it should appear there.

## `evolve.ui` Widget Map

Decoded from `modules/ui/decoded/data__ui__evolve.ui.json`.

The decoded static text contains mojibake in the JSON output, so use widget ids
and source strings from `game.h`/`VqsvText` when porting, not the decoded text
verbatim.

| Widget | Coord | Source meaning | Status |
| ---: | --- | --- | --- |
| `4` | `x43 y55 w158` | main frame, sprite 257 cell 1 mode 2 | SOURCE-MAPPED. |
| `5` | `x70 y60 w100` | title, static "Tien hoa" | SOURCE-MAPPED. |
| `6` | `x197 y296 w43` | right/back softkey | SOURCE-MAPPED. |
| `7` | `x1 y296 w43` | left/confirm softkey | SOURCE-MAPPED. |
| `10` | `x78 y90 w90` | pet sprite | SOURCE-MAPPED. |
| `15..18` | left stat labels | HP/attack/defense/speed labels | SOURCE-MAPPED. |
| `19..22` | left stat values | old stats | SOURCE-MAPPED. |
| `27..30` | right stat labels | HP/attack/defense/speed labels | SOURCE-MAPPED. |
| `31..34` | right stat values | new stats | SOURCE-MAPPED. |
| `38` | `x53 y84 w72` | pet/species name | SOURCE-MAPPED. |
| `40` | `x165 y82 w24` | level | SOURCE-MAPPED. |
| `42` | `x56 y226 w48` | required-material label | SOURCE-MAPPED. |
| `45` | `x114 y241 w48` | required material name | SOURCE-MAPPED. |
| `46` | `x164 y241 w24` | current/required material count | SOURCE-MAPPED. |

## Current Rebuild Mapping

| Rebuild area | Current behavior | Status |
| --- | --- | --- |
| `VqsvBattleRuntime.tickWinExpLevelUp()` | P8/P22 EXP/level-up overlay, learn-skill transition, then calls source-shaped evolution queue producer. | `PORTED/PARTIAL` for EXP/level-up and queue producer. |
| `BattleUnit.sourceLevelUpOnce()` | level++, subtract EXP, refresh stats/PP. Queue producer is intentionally in the runtime wrapper, immediately after this call, to mirror `game.b.v() -> J()`. | `PORTED/PARTIAL`. |
| `BattleUnit.sourceLearnCandidateSkillIds()` | source-shaped `game.b.F()` candidate list. | `PORTED/PARTIAL`. |
| `VqsvBattleRenderer.drawLevelUpOverlay()` | source-shaped `levelUp.ui`, not `evolve.ui`. | `PORTED/PARTIAL`. |
| `VqsvBattlePetStateView.evolutionText()` | shows possible evolution text in petstate detail if species has target. | `PORTED/PARTIAL` display hint only; not an evolution runtime. |
| World evolution notice queue | `Scene.sourceEvolutionQueue`, `sourceEvolutionL`, and `sourceEvolutionI` store the producer output. | `PORTED/PARTIAL`; consumer still `PENDING`. |
| `/data/ui/evolve.ui` renderer | no rebuild renderer yet. | `PENDING`. |
| `ah` evolution effect | no rebuild evolution effect runtime yet. | `PENDING`. |

## Gap Matrix

| Concern | Why it matters | Status |
| --- | --- | --- |
| Queue producer `game.b.J()` | Source level-up calls it before the battle exits. | `PORTED/PARTIAL`; created in rebuild after `sourceLevelUpOnce()`, before P22 overlay finishes. |
| Full `R()` meaning | Gate before queue creation. | `PORTED/PARTIAL` for visible target-kind mapping `1/2 -> 1`, `3 -> 2`; broader pet form semantics still `PENDING`. |
| Material inventory `game.g.o().a(id,2)` | Required for material-gated confirm path. | `PORTED/PARTIAL` as metadata count only; confirm UI/effect still `PENDING`. |
| World queue consumer `game.k.H/L/I` | Actual notification and tutorial path after battle. | `PENDING`. |
| `evolve.ui` renderer | Actual evolution UI, not level-up UI. | `PENDING`. |
| `game.k.n` `ah` effect | Actual animation before pet payload mutates. | `PENDING`. |
| Pet payload mutation | Source constructs new `game.b`, copies payload via `P()`, consumes material. | `PENDING`. |
| Story route reachability | Current early species `0` has no target. | `UNKNOWN/PENDING`; likely needs fixture first. |
| Pixel-perfect UI/effect | Original-vs-rebuild compare absent. | `PENDING`. |

## Recommended Implementation Slices

Do not implement all evolution at once.

### Slice 1: Queue Producer Only

Goal:

- Add a rebuild-side evolution notice queue equivalent to `game.k.H/L/I`.
- Call it from `BattleUnit.sourceLevelUpOnce()` or from the runtime wrapper
  immediately after level-up, matching the source `game.b.v() -> J()` point.
- Use source species columns `19/20/21` and level table `game.b.t`.
- Store material id/count/need for later, but do not block queue creation on
  material count because visible source `game.b.J()` still enqueues through its
  fallback branch.

Smoke:

- `battle_levelup_evolution_queue_created.png`

Expected trace:

```text
PORTED/PARTIAL battle P22 evolution queue species=<old> target=<new>
```

Do not show `evolve.ui` in this slice.

## Implementation Slice 2026-07-08

Implemented:

- Added `SourceEvolutionNotice`.
- Added rebuild equivalents for source queue fields:
  - `Scene.sourceEvolutionQueue` as `game.k.H`.
  - `Scene.sourceEvolutionL[0..1]` as `game.k.L`.
  - `Scene.sourceEvolutionI` as `game.k.I`.
- Added source-shaped producer in `SourceBattleRuntime` immediately after
  `BattleUnit.sourceLevelUpOnce()`.
- Producer uses:
  - species column `19` as target species;
  - target species column `2` through source level table `{12,30,5}`;
  - source `R()` semantics: target kind `1/2 -> 1`, kind `3 -> 2`;
  - species columns `20/21` only as stored material id/count metadata for later
    confirm UI.
- Added smoke checkpoint `battle_levelup_evolution_queue_created`.

Current classification after this slice:

| Area | Status | Notes |
| --- | --- | --- |
| Queue producer timing | `PORTED/PARTIAL` | Runs directly after level-up, matching `game.b.v() -> J()`. |
| `game.k.H/L/I` data shape | `PORTED/PARTIAL` | Stored in `Scene`; not consumed by world/UI yet. |
| Material count handling | `PORTED/PARTIAL` | Stored in notice; does not block queue, matching visible source fallback branch. |
| World notice consumer | `PENDING` | No `game.k` consumer yet. |
| `/data/ui/evolve.ui` | `PENDING` | No UI opened. |
| `ah` effect and payload mutation | `PENDING` | No animation or species change. |
| Save/resume of evolution queue | `PENDING` | Not part of producer-only slice. |

Smoke evidence:

- `rebuild_game/build/smoke/battle_levelup_evolution_queue_created.png`

### Slice 2: World Notice Consumer

Goal:

- Consume the queue after battle/world resume, mirroring the `game.k` notice
  branch.
- Show source-shaped `msgwarm.ui` or openbox style notice only.
- Do not mutate pet yet.

Smoke:

- `world_evolution_notice_after_levelup.png`

### Slice 3: `evolve.ui` Open Fixture

Goal:

- Add a source-backed `evolve.ui` renderer/view model.
- Fill widgets `10/38/40/45/46/19..22/31..34`.
- Use a deterministic fixture species such as `6`, `17`, `91`, or `97`.

Smoke:

- `evolve_ui_open_source_shape.png`

### Slice 4: Confirm + `ah` Effect + Payload Mutation

Goal:

- Port the source `bh()` confirm path for a material-sufficient fixture.
- Create/tick source-shaped `ah` effect based on:

```text
short[] {0,0,10,0,0,currentSprite,0,0,targetSprite,0,0}
```

- After effect completes, mutate pet payload using source-shaped `P()` data.
- Consume material count.

Smoke:

- `evolve_confirm_effect_start.png`
- `evolve_confirm_effect_done_payload.png`

### Slice 5: Failure Branches

Goal:

- No target.
- Level too low.
- Missing material.
- Mutation vs evolution wording.

Smoke:

- `evolve_warning_no_target.png`
- `evolve_warning_level_too_low.png`
- `evolve_warning_missing_material.png`

## Required PNG-Only Regression Plan

After any code slice touching battle level-up, pet payload, source inventory, or
world queue:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-rebuild-skeleton.jar --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
run the existing Java-source mojibake scan used by the current handoff
git diff --check
```

Focused smokes:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_exp_levelup_ui build\smoke\battle_exp_levelup_ui_regression.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_exp_levelup_choiceskill_ui build\smoke\battle_exp_levelup_choiceskill_ui_regression.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_exp_levelup_learn_skill_done build\smoke\battle_exp_levelup_learn_skill_done_regression.png
```

Route regressions:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\route_sophie_after_battle_branch_evolution_regression.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\route_bunny_after_battle_task_evolution_regression.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\route_elder_after_battle_reward_state_evolution_regression.png
```

Do not open the live client unless the user explicitly asks.

## Safety Statement

Files that may be edited in later code slices after this audit:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- a new evolution view/runtime helper if needed
- `rebuild_game/src/main/java/VqsvText.java` only for Unicode-safe source text

Files/areas not to touch for the first slice:

- intro/world scene scripts unrelated to battle result handoff
- P21/P17 catch
- P5/P16/P7 unless a shared pet payload helper is genuinely required
- title/panel/logo

No-guess rules:

- Do not draw evolution animation before `game.h.bh()` `ah` path is ported.
- Do not claim story route evolution unless a source route reaches a species
  with `col19 != -1`.
- Do not claim pixel-perfect `evolve.ui` without original comparison.
- Do not copy mojibake text from decoded JSON into Java.

## Current Answer To "What Next?"

Slice 1 **Queue Producer Only** is complete at `PORTED/PARTIAL`.

Next code slice should be **Slice 2: World Notice Consumer**.

Reason:

- It is the first source consumer after `game.b.J()` creates `game.k.H/L/I`.
- It can be smoke-tested as a notice/openbox state without opening `evolve.ui`.
- It keeps the evolution UI/effect/payload mutation isolated for later slices.
- It avoids guessing the `game.h.bg()/bh()` path before the `game.k` notice
  bridge exists.

Done criteria for Slice 2:

- After battle/world resume, the queued notice is consumed only when the source
  `game.k` conditions are represented in rebuild.
- A source-shaped notice is visible from the queued `{species, nameText}` data.
- Queue drain updates `sourceEvolutionI`/queue state consistently with the
  audited `game.k` path.
- `/data/ui/evolve.ui`, `ah` effect, and pet payload mutation remain explicitly
  `PENDING`.
