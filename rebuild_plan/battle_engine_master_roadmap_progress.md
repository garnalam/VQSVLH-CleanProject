# Battle Engine Master Roadmap Progress

Status: MASTER ROADMAP / PROGRESS ALIGNMENT.

This file aligns the attached battle-engine roadmap with the actual current
rebuild progress. It is the controlling order for battle work from this point:

`data model -> state machine -> UI -> formula/status -> item/catch/pet -> effect -> actor motion -> event integration -> skill coverage -> regression`

Rule: do not jump to visual polish or one-off skill effects before the roadmap
layer that calls that logic is audited and wired.

## Numbering Note

The roadmap draft names later docs as `72`, `73`, `74`, etc. In the actual
project, `72` and `73` are already used for Phase 1 follow-up docs:

- `72_battle_full_skill_status_behavior_classification.md`
- `73_battle_rebuild_mapping_next_code_tasks.md`

To avoid overwriting useful work, Phase 2+ docs should use the next available
numbers. The roadmap order is unchanged; only document numbers are shifted.

## Current Phase Summary

| Roadmap phase | Target | Current status | Actual docs / evidence | Next action |
| --- | --- | --- | --- | --- |
| Phase 1 | Battle Data Model | AUDIT DONE / CODE PARTIAL | `70`, `71`, `72`, `73` | Keep as reference; do not code random fields before Phase 2 state audit. |
| Phase 2 | `game.d` Turn/State Machine | PARTIAL / NEXT ACTIVE | Existing runtime has P0/P20/P3/P6/P7/P8/P9 plus shells | Create `74_battle_game_d_state_full_matrix.md`. |
| Phase 3 | `game.h` Command/UI Runtime | PARTIAL | UI exists source-shaped, not full widget runtime | After Phase 2, create `75_battle_ui_game_h_widget_matrix.md`. |
| Phase 4 | Damage Formula + Buff/Debuff | PARTIAL | `BattleUnit.computeDamage()` exists; buff/debuff lifecycle missing | After Phase 2/3 matrix, create `76_battle_damage_status_formula_full_matrix.md`, then code buff/debuff. |
| Phase 5 | Item/Catch/Pet Switch Full Behavior | PARTIAL/APPROX | P21/P17/P4/P16/P5 are source-shaped and smoke-covered for current routes; catch/petstate UI still partial | Continue from `79`, `82..99`; next choose a small missing consumer, not broad polish. |
| Phase 6 | P7 Effect Animation Engine | CLOSED FOR CURRENT ROUTES / PORTED-PARTIAL | `134..142`; P7 effect chunks, damage text/HP tween, death state, queue/follow-up branches smoke-covered | Do not reopen without original capture or a concrete source-route mismatch. |
| Phase 7 | Actor Motion / Hit / Recover / Dead | CLOSED FOR CURRENT ROUTES / PORTED-PARTIAL | `135..142`; synthetic recoil removed, state 1/2 source-asset compare, state 3 death timing/hidden actor smoke-covered | Exact MIDP pixel parity remains future capture work. |
| Phase 8 | Battle Entry/Exit + Event Integration | CLOSED FOR CURRENT MANUAL ROUTES / PORTED-PARTIAL | `143..154`; Sophie/Bunny/Elder route regressions pass, op47/downstream/save/world-resume wrappers cover current manual boundaries | Do not build generic decoded event VM unless a source-backed design task is chosen. |
| Phase 9 | Broad Skill Coverage | NEXT ACTIVE / PARTIAL | `72` classifies skills, but coverage/smoke matrix is not complete | Start with `155`, then create `156_battle_phase9_skill_coverage_matrix.md`. |
| Phase 10 | Regression Suite | CLOSED / PARTIAL | `191_battle_phase10c_regression_suite_matrix.md`, `192_battle_phase10_closeout.md`; `--smoke-suite battle_quick` passes 14/14 checkpoints | Use `battle_quick` after every battle code change; expand focused suites only when needed. |

## Phase 1 Progress Against Roadmap

Roadmap requirement:

- Audit all `game.b` fields.
- Map HP/EXP/level/species, skill ids/PP, buff/debuff slots, status flags,
  passive/hooks from `game.g/game.k`.
- Produce `70_battle_unit_full_field_matrix.md` and
  `71_battle_skill_status_table_matrix.md`.

Actual progress:

| Item | Status | Notes |
| --- | --- | --- |
| `game.b` field audit | DONE | `70_battle_unit_full_field_matrix.md`. |
| `aq.c` table audit | DONE/PARTIAL | `71_battle_skill_status_table_matrix.md`; table dependencies known. |
| Full row classification | DONE/PARTIAL | `72_battle_full_skill_status_behavior_classification.md`; every row is classified or explicitly pending/unknown. |
| Rebuild code mapping | DONE/PARTIAL | `73_battle_rebuild_mapping_next_code_tasks.md`; gaps are listed. |
| No important unit runtime field unknown | PARTIAL | Core fields known. Remaining unknowns are specific damaged source/control-flow areas: `SOURCE_SWITCH_GAP` skills, some status ids, `aq.c[5]`, passive ambiguity. |

Phase 1 conclusion:

- Phase 1 audit layer is complete enough to move to Phase 2.
- Phase 1 code parity is not complete; code gaps are now queued in `73`.
- Do not code Task 1 before Phase 2 state matrix unless explicitly choosing a
  small controlled code slice with known state impact.

## Phase 2 Current Target

Roadmap target doc name:

- Draft name: `72_battle_game_d_state_full_matrix.md`
- Actual project name: `74_battle_game_d_state_full_matrix.md`

Scope:

- Audit the full battle switch/state machine in `game.d`.
- For each state: source case -> UI/method -> input -> side effect -> next state.
- Cover at least:
  - P20 command.
  - P3 skill list.
  - P6 target select.
  - P7 resolve.
  - P21/P17 catch.
  - P4/P16 item.
  - P5 pet switch.
  - P10 run.
  - P11 shop.
  - warning/disabled states.
  - P1/P2/P8/P9 dispatch/result states.

Done criteria:

- Every main battle state has a matrix row.
- Every current rebuild state is classified `PORTED / PARTIAL / APPROX /
  STUB / MISSING / UNKNOWN`.
- No main state is silently treated as complete while still being a shell.
- The first code task after the matrix should be selected from the state gaps,
  not guessed.

Progress update:

- `74_battle_game_d_state_full_matrix.md` has been created.
- Code slice 1 is implemented: P7 now preserves full `BattleDamageResult`
  through damage frame, including `critFlag` and `appliedDebuffId`.
- Code slice 2 is implemented at the post-skill layer:
  `BattleUnit.applySourceBuff(...)` exists, `75_battle_game_d_q_post_skill_matrix.md`
  documents `game.d.q()` heal/buff/leech/reflect branches, and P7 calls the
  source-shaped post-skill resolver.
- Remaining Phase 2/4/5/8 gaps are still real: full participant EXP vector
  `game.d.x`, passive EXP share, exact `msgwarm.ui`/`choiceskill.ui` pixels,
  evolution queue `game.b.J()`/`game.k.H`, full item/catch/petstate widget
  runtime, and broad active-effect/skill coverage are not complete.

## Current Code Task Queue From Phase 1

These tasks remain valid, but they should now be scheduled inside Phase 2/4
order:

1. Preserve full `BattleDamageResult` through P7. DONE/PARTIAL.
2. Add `BattleUnit.applySourceBuff(...)` and P7 `game.d.q()` post-skill behavior. DONE/PARTIAL.
3. Add buff/debuff tick and duration lifecycle.
4. Wire passive hook context.
5. Complete item behavior `game.b.w/x`.
6. Validate `SOURCE_SWITCH_GAP` skills before porting them.
7. Build deterministic smoke matrix.

Recommended ordering now:

1. `74_battle_game_d_state_full_matrix.md` is complete.
2. The first Phase 2 code slice, `Preserve full BattleDamageResult through P7`,
   is implemented and smoke-checked.
3. `game.d.q()` post-skill buff/heal/leech/reflect behavior is now documented
   in `75_battle_game_d_q_post_skill_matrix.md` and smoke-covered.
4. P12/P13 active queue lifecycle has been source-audited in
   `76_battle_p12_p13_active_queue_lifecycle_matrix.md`.
5. First P12/P13 runtime slice is implemented for bank `0` buff id `9` from
   skill 45, including visual `ap[8] = speffect 19 -> 15`, `o(9)` stat tick,
   and `d(9,slot)` duration decrement/removal.
6. Second P12/P13 runtime slice is implemented for bank `1` debuff id `0`,
   including visual `aq[0] = speffect 18`, source-shaped `q(0)` HP damage,
   `c(0,slot)` duration decrement/removal, and negative HP-delta text smoke.
7. P12/P13 active queue is now generalized enough for current battle runtime:
   ordered bank/slot scan, source visual gate `ai`, original `bufDebuf.mid`
   loader, debuff `3`, stat debuff `5`, no-visual immediate ticks, and P8/P5/P9
   death transitions are smoke-covered.
8. P12/P13 actor-action type0 has a source-shaped runtime slice:
   kind `0` no longer fakes base actor state; it creates/ticks an AH-effect
   actor animation from the `bufDebuf` effect id/state and supports trigger
   checks. This is still PARTIAL against a full generic `ah` renderer.
9. P15 enemy replacement is now implemented in the rebuild runtime with enemy
   party support, active enemy index, P15 state, and reserve-enemy smoke.
   It is PORTED/PARTIAL because source turn arrays `d/v/e/t/s/u` are simplified.
10. P12/P13 kind `1` `H` special effects have been audited in
   `77_battle_p12_p13_h_speffect_matrix.md`. Source-reachable active queue
   AH types are `1`, `8`, `9`, and `12`; type8/type12 have first renderer
   slices and smoke coverage.
11. Active effect lifecycle has been audited in
   `78_battle_active_effect_lifecycle_full_matrix.md`. Clear/reapply stat
   parity has a first code slice: clearing one bank/effect resets mutable stats
   and reasserts remaining active source tick stat effects without replaying
   HP/damage/flag side effects.
12. Item/pet/catch Phase 5 states have been audited in
   `79_battle_item_pet_catch_state_matrix.md`. P4/P16/P5/P21/P17 source
   methods, UI files, item behavior table, catch storage, and rebuild gaps are
   mapped.
13. P16 item behavior parity is implemented for `game.b.x/w` behavior `1..6`
   at current rebuild granularity, including selected active/reserve pet target
   and HP/PP payload persistence.
14. P5 pet switch parity has a cleaned source-shaped slice: current/dead
   validation, forced-death list filtering, source pet reorder `f[slot] -> f[0]`,
   HP/PP persistence, battle-only `petstate` renderer with selected-pet detail
   panel and source `Xuất chiến` button label, and source state 15 transition
   before P1. It remains PARTIAL only for full `petstate.ui` widget runtime,
   equipment/relation/evolution detail data, and exact `game.d.an` transition
   frame playback.
15. P15 player-switch cpos transition has been audited and ported for the
   current elder P5 route in `80_battle_p15_cpos_transition_matrix.md`.
   Rebuild now reads original `script/original/cpos.mid`, maps the elder
   path to `an[0][1]`, and applies source frame offsets through P15 before
   returning to P1. Full `game.d.an` parity remains PARTIAL for P0 entry,
   enemy replacement, side-marker `al[]`, group `1` multi-unit layout, and
   absolute MIDP sprite anchoring.
16. P21/P17 catch, Bunny tutorial, RNG trace, catch storage/openbox, battle
   marker/HUD, pet persistence, P5/petstate, save prompt, and world petstate
   have been audited/ported in slices `82..99`. Current status is
   PORTED/PARTIAL for the route-smoked behavior, not pixel-perfect.
17. P8/P22 EXP and level-up are now ported for the active participant slice in
   `100_battle_exp_levelup_source_audit.md`: source EXP formula, threshold,
   stat refresh, `levelUp.ui` renderer, and focused smoke
   `battle_exp_levelup_ui`.
18. P23 learn-skill after level-up is now ported for the active-pet slice in
   `101_battle_levelup_learn_skill_evolution_audit.md`: candidate skill list
   from `game.b.F()`, `choiceskill.ui`, confirm prompt, and payload skill add.
   Evolution queue from `game.b.J()` remains PENDING.
19. P7/Phase 6 has been closed for current routes in
   `142_battle_p7_phase6_closeout_and_next_phase.md`. Recent P7 slices cover
   source-asset state 1/2 compare, HP tween and `blood.mid` text timing,
   death state 3/AH type16 source-shaped timing, dead actor hidden lifetime,
   P7 -> P12/P13 queue order, and `game.d.q()` buff12/skill63 follow-up
   branches. Remaining debt is original-client pixel parity and broad
   rare-route skill coverage, not a blocker for Phase 8.
20. Phase 8 entry/exit/event integration audit and first bridge slice are in
   `143_battle_phase8_entry_exit_event_integration_audit.md`. Source chain is
   mapped as `op67/op37 -> op32 -> game.i state12/13 -> game.d P8/P9/P22/P23
   -> world state10 -> op47`. Bunny and Elder now use a source-backed
   `BattleEventDescriptor` for op37/op32/op47 data. Current routes are
   PORTED/PARTIAL, while generic decoded-event `op47` consumption remains
   APPROX/PENDING.
21. The tiny `op47` consumer wrapper has been source-audited and implemented
   for Bunny/Elder in `144_battle_phase8_op47_consumer_wrapper_audit.md`. Key
   rule: source computes `sourceCursor = op47Args[l] - 2`, while current rebuild
   `battleBranchTarget` intentionally remains the raw logical target such as
   Elder `10` or Sophie `78`. The wrapper traces/asserts both values without
   mutating `Scene.eventIndex`.
22. Sophie room3 group0 descriptor/op47 coverage is implemented and verified in
   `145_battle_phase8_sophie_descriptor_op47_audit.md`. Source records are
   `op67 [56]`, `op37 [5,20,4]`, `op52 [1,1]`, `op32 [0,2]`,
   `op47 [78,78,0]`. `SCENE1_ROOM3_GROUP0_SOPHIE` now drives
   `Scene1Room3EntryScript` and focused smoke asserts
   `op47 result=0 rawTarget=78 sourceCursor=76`, while keeping
   `battleBranchTarget=78` and no `eventIndex` mutation.
23. Post-op47 downstream consumers are audited in
   `146_battle_phase8_post_op47_downstream_consumer_audit.md`. Current
   task/reward/save/free-world return behavior is source-aligned for current
   routes but still manual route-script driven. Descriptor/trace-only coverage
   for Bunny/Elder downstream consumers is implemented and verified; no generic
   event VM was added.

## Phase Dependency Rules

| Rule | Reason |
| --- | --- |
| Logic source first, UI second. | UI must appear only when `game.d/game.h` source calls it. |
| State machine before P7 side-effect code. | P7 result must know which state consumes crit/debuff/death/win/lose next. |
| Buff/debuff logic before broad skill coverage. | Many skills cannot be validated without correct status lifecycle. |
| Item/catch/pet after state and formula basics. | They depend on command state, inventory, target, and result branch. |
| Effect animation after source trigger matrix. | Effects must be called by real skill/state logic, not as decoration. |
| Regression after every phase slice. | User should not be forced to discover regressions by manually replaying. |

## Immediate Next Step

Phase 8 current-route wrappers are closed in `150..154`:

- room1 save -> Bunny `op13`;
- Bunny group0 complete -> room0 transition;
- Elder group6 complete -> post-group6 free-world.

Next roadmap-consistent target is now Phase 9 broad skill coverage:

```text
Create 156_battle_phase9_skill_coverage_matrix.md.
```

Rules for the next step:

- Do not code a skill before the Phase 9 matrix exists.
- Do not reopen P7 visual parity without a selected skill/source route that
  proves the need.
- Do not port `SOURCE_SWITCH_GAP` skills by guessing from `aq.c[1]` rows.
- Keep route regressions for Sophie/Bunny/Elder after every Phase 9 code slice.
- Generic decoded event VM remains out of scope unless a separate source-backed
  design task is chosen.
