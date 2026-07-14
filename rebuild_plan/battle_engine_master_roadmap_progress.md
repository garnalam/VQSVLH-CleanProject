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
| Phase 9 | Broad Skill Coverage | CLOSED / PORTED-PARTIAL + SMOKE-COVERED | `155..187`; `156` matrix covers `aq.c[1][0..69]`; `187` closeout confirms no skill row remains missing/pending/unknown | Do not reopen broad skill coverage unless a source-route mismatch is found. |
| Phase 10 | Regression / Visual Status Foundation | CLOSED / PARTIAL | `190..192`, `222`; `--smoke-suite battle_quick` passes 20/20 checkpoints | Use `battle_quick` after every battle code change; expand focused suites only when needed. |
| Phase 11 | Battle UI Widget Runtime Parity | ACTIVE / PARTIAL | `193..199`, `221`; choice/msgwarm/openbox runtime slices are source-backed for current battle flows; `choice.ui` wheel/hover/click mapping is verified for P4/P21 and P16 is verified not-applicable | Continue only with a chosen UI runtime gap such as `petstate.ui`, or move to Phase 12 animation/effect gaps if staying on battle visuals. |

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
24. P9/P24 lose/revive/world reset is audited and ported for current PC routes
   in `217_battle_p9_p24_loss_revival_world_reset_closeout.md`. P9 first-loss
   now applies source-shaped party reset to 1 HP/PP and arms the source
   `M.i/M.l` equivalent. P24 paid revive deducts 10000 and restores all party
   pets to full HP/PP before returning battle state `P0`. SMS/P102 is
   intentionally not ported for PC; exact `smsInfo.ui` pixel parity and full
   `game.h.bv()` coordinate restore variants remain PENDING/PARTIAL.
25. Phase 9 broad skill coverage is closed in `155..187`. The controlling
   matrix is `156_battle_phase9_skill_coverage_matrix.md`; the closeout
   `187_battle_phase9ad_skill_coverage_closeout.md` confirms every
   `aq.c[1][0..69]` skill row has a smoke checkpoint, family smoke plus sibling
   proof, or a source-backed `NOT_REACHED` classification. This is
   PORTED/PARTIAL + smoke-covered, not full pixel/RNG parity.
26. Phase 10 regression/visual-status foundation is closed in `190..192`.
   The required quick gate is `--smoke-suite battle_quick`, currently 20/20
   checkpoints.
   For input/list/panel UI changes, also run `--smoke-suite panel_wheel`.
   This suite is the fixed regression guard for mouse-wheel viewport scrolling
   and scroll-then-hover/click row mapping in panel lists.
27. Phase 11 battle UI widget runtime parity has started in `193..199`.
   Current source-backed slices cover `choice.ui`, `msgwarm.ui`, and
   `openbox.ui` for the current battle flows. Remaining UI parity should be
   chosen by concrete flow need, not broad polish.
28. `221_battle_choice_ui_wheel_hover_click_mapping_audit.md` verifies the
   battle `choice.ui` list-input slice:
   P4 real scrollable item list is fixed/verified, P21 real route remains
   non-scroll but synthetic long-list mapping is verified, and P16 is proven to
   use `petstate.ui` rather than `choice.ui`.
29. `222_battle_exp_normal_vs_forced_levelup_audit.md` separates normal EXP
   gain from forced level-up smoke setup. `battle_exp_normal_gain_no_levelup_anim`
   is now in `battle_quick`, and Battle Lab has `npc.exp_normal_gain` so manual
   testing no longer has to use the intentional threshold-forced `exp_levelup`
   scenario as the default EXP check.
30. `223_battle_exp_formula_and_source_flow_audit.md` locks the source EXP
   formula and flow:
   enemy KO -> `game.d.h()` pending `B` -> `game.d.X()` commit to `S` ->
   P8 `game.h.am()` visual increment -> P22 `levelUp.ui` only when threshold
   is reached. It also documents direct EXP assignment/load paths and the
   Elder normal example of 400 EXP with no level-up from level 7.
31. `224_battle_p8_exp_pos_mid_marker_placement_audit.md` decodes
   `game.d.am[0] = [177,103,144,85,70,223,36,206]`, maps P8 source calls
   `game.h.a(am[0][4],am[0][5]) = 70,223` and
   `al[0].b(am[0][6],am[0][7]) = 36,206`, and corrects normal P8 EXP to update
   the existing `battle.ui` player HUD bar/text (`widget 9/40`) instead of a
   floating panel. Exact original-client P8 pixel parity remains PENDING.
32. `225_battle_p8_exp_initial_frame_timing_audit.md` separates source state 8
   entry `game.h.a(...)` from the first `game.h.am()` visual increment. Rebuild
   now renders the first normal P8 HUD frame at the pre-increment value
   (`0/760` in the Elder-style smoke) before the next tick applies `+8`.
33. `226_new_dev_chat_handoff_battle_lab_items_skills.md` is the current
   new-chat handoff for Battle Lab, item completion, and deeper skill
   completion. It treats Battle Lab as a project testing module/workflow over
   the same runtime, not a fork, and sets the next concrete task as an item
   completion matrix before any broad item coding.
34. `227_battle_item_full_completion_matrix.md` is now the controlling item
   completion matrix. It maps source `aq.c[4]` rows `0..14`, battle P4/P16,
   catch P21/P17, P11 shop, panel state17, petsetting item/equipment,
   special q.N rows, rewards/op17, inventory ownership, and save/load. The
   next item slice should be smoke/verification for panel state17 and
   petsetting normal-item behaviors before changing logic.
35. `317_battle_lab_all_skill_test_list_closeout.md` adds Battle Lab checkpoint
   `battle_lab_skill_test_all`. It opens P3 `choiceskill.ui` with all source
   skill ids `0..69`, then lab-installs the selected skill into player slot `0`
   so manual testers can inspect current P7 animation/effect behavior quickly.
   This is lab-gated and does not replace dedicated source closeouts per skill.
36. `318_battle_skill10_diep_toan_source_logic_animation_closeout.md` starts
   the Wood Lane per-skill closeout pass. Skill `10` / `Diep Toan` is now
   covered by a one-run smoke timeline with source row `[1,127,539,100,0,45,0,
   -1,-1,0]`, effect row `[0,0,21,1,-1,-1,0]`, actor `21 -> sprite263`,
   state `1`, PP `45 -> 44`, and same-run damage/HP-settled consistency
   under source-shaped damage jitter.
   Next Wood Lane slice is skill `11`, which must be treated as direct damage
   plus `game.d.q()` heal, not as a plain direct-base skill.
37. `319_battle_skill11_quang_phan_source_logic_animation_closeout.md` closes
   Wood Lane skill `11` / `Quang Phan`. Source row
   `[1,128,540,90,0,45,0,-1,10,0]` and effect row
   `[0,0,21,1,-1,-1,0, 1,1,10,0,-1,-1,0]` are smoke-asserted. The one-run
   timeline covers actor `21 -> sprite263`, `speffect10/AH9` attacker-side
   heal visual, damage/HP-settled consistency, PP `45 -> 44`, and `game.d.q()`
   heal `+2` with player HP `67 -> 69` in the checked run. Next Wood Lane
   slice is skill `12`, direct damage plus debuff id `2` and `speffect6/AH8`.
38. `320_battle_skill12_dang_phuoc_source_logic_animation_closeout.md` closes
   Wood Lane skill `12` / `Dang Phuoc`. Source row
   `[1,129,541,50,0,45,2,2,-1,0]`, debuff row `[313,324,3]`, and effect row
   `[0,0,21,0,-1,-1,0, 0,1,6,0,-1,-1,0]` are smoke-asserted. The one-run
   timeline covers actor `21 -> sprite263/state0`, target-side
   `speffect6/AH8`, damage/HP-settled consistency, PP `45 -> 44`, debuff2
   `Quan Quanh` apply with icon `3/duration 137`, no-HP tick to duration `2`,
   and expiry to status count `0`. Next Wood Lane slice is skill `13`, which
   must be audited source-first before coding.
39. `321_battle_skill13_thao_chung_source_logic_animation_closeout.md` closes
   Wood Lane skill `13` / `Thao Chung`. Source row
   `[1,130,542,50,1,30,2,3,150,0]`, debuff row `[314,325,3]`, and effect row
   `[0,0,21,0,-1,-1,0]` are smoke-asserted. The one-run timeline covers actor
   `21 -> sprite263/state0`, immediate damage/HP-settled consistency, PP
   `30 -> 29`, debuff3 `Thuc Loai` apply with icon `4/duration 137`, two
   no-damage ticks to duration `1`, final delayed damage
   `storedRaw * 150 / 100`, icon clear, and a controlled active-queue frame
   showing floating text `-36`. Next Wood Lane slice is skill `14`, a no-damage
   buff2 producer, not a plain attack.
40. `322_battle_skill14_dang_chi_bich_luy_source_logic_animation_closeout.md`
   closes Wood Lane skill `14` / `Dang chi bich luy`. Source row
   `[1,131,543,0,1,10,1,2,-1,1]`, buff row `[335,350,3,30,10]`, and effect row
   `[0,0,21,1,-1,-1,0]` are smoke-asserted. The one-run timeline covers
   player-side actor `21 -> sprite263/state1`, no damage/no hitroll, PP
   `10 -> 9`, buff2 `Kinh Cuc` apply with icon `14/duration 137`, defense
   `100 -> 130`, forced-hit reflect `damage * 10 / 100`, forced-miss no reflect,
   forced-crit reflect from crit damage, and expiry back to defense `100`.
   Next Wood Lane slice is skill `15`, buff3 `Khoi phuc` heal-over-time.
41. `323_battle_skill15_thao_nguyen_thuat_source_logic_animation_closeout.md`
   closes Wood Lane skill `15` / `Thao nguyen thuat`. Source row
   `[1,132,544,0,1,10,1,3,-1,1]`, buff row `[336,351,3,5,-1]`, effect row
   `[0,0,33,0,0,-1,0, 0,1,7,0,-1,-1,0]`, and speffect row
   `[9,120,218,217,169,0,9,9]` are smoke-asserted. The one-run timeline covers
   player-side actor `33 -> sprite308/state0`, chunk `[4] == 0` frame trigger
   into `speffect7/AH9`, no damage/no hitroll, PP `10 -> 9`, buff3 `Khoi Phuc`
   apply heal `maxHP * 5 / 100` (`67 -> 73` in the smoke), active queue tick
   heal after the enemy turn (`66 -> 72`), icon `15/duration 137 -> 136`, and
   controlled expiry to status count `0`. Next Wood Lane slice is skill `16`,
   a direct higher-damage Wood attack.
42. `324_battle_skill16_cham_diep_tram_source_logic_animation_closeout.md`
   closes Wood Lane skill `16` / `Cham Diep Tram`. Source row
   `[1,133,545,150,2,30,0,-1,-1,0]` and effect row
   `[0,0,21,1,-1,-1,0]` are smoke-asserted. The one-run timeline covers
   enemy-side actor `21 -> sprite263/state1`, PP `30 -> 29`, direct damage with
   `powerPercent=150`, damage frame `-37` in the checked run, HP settle
   `109 -> 72`, and no buff/debuff/post-effect. Next Wood Lane slice is skill
   `17`, direct damage plus q() heal param `40`.
43. `325_battle_skill17_diep_chi_an_hue_source_logic_animation_closeout.md`
   closes Wood Lane skill `17` / `Diep chi an hue`. Source row
   `[1,134,546,130,2,30,0,-1,40,0]`, effect row
   `[0,0,21,1,-1,-1,0, 1,1,10,0,-1,-1,0]`, and speffect row
   `[9,120,218,217,169,0,4,2]` are smoke-asserted. The one-run timeline covers
   target-side actor `21 -> sprite263/state1`, attacker-side `speffect10/AH9`,
   PP `30 -> 29`, direct damage with same-run HP settle, no buff/debuff, and
   `game.d.q()` post-skill heal using param `40` (`+9` in the smoke). Next Wood
   Lane slice is skill `18`, stronger direct damage plus debuff id `2`.
44. `326_battle_skill18_dang_man_trien_nhieu_source_logic_animation_closeout.md`
   closes Wood Lane skill `18` / `Dang man trien nhieu`. Source row
   `[1,135,547,150,3,15,2,2,-1,0]`, debuff row `[313,324,3]`, effect row
   `[0,0,21,0,-1,-1,0]`, and active queue speffect row `6/AH8` are
   smoke-asserted. The one-run timeline covers target-side actor
   `21 -> sprite263/state0`, no P7 special chunk, PP `15 -> 14`, direct damage,
   debuff2 `Quan Quanh` with icon `3/duration 137`, source skill `18`, P12
   active queue `speffect6/AH8`, no-op HP/stat tick `74 -> 74`, duration
   `3 -> 2`, and expiry/clear after three ticks. Next Wood Lane slice is skill
   `19`, stronger direct damage plus delayed debuff id `3`.

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

Phase 9 broad skill coverage is closed in `155..187`, Phase 10 quick-gate
regression foundation is closed in `190..192`, and P8 EXP source formula/timing
is current through `222..225`.

The user has chosen the next active direction:

```text
1. Treat Battle Lab as the standard battle test module/workflow.
2. Finish item logic across battle, panel/bag, shop, rewards, equipment, and
   save/load.
3. Then continue deeper skill completion/parity beyond broad Phase 9 row
   coverage.
```

Immediate next concrete task:

```text
Create 228_panel_state17_petsetting_item_behavior_smoke_matrix.md.
```

This should add focused PNG checkpoints for source-proven existing item
behaviors before changing logic: item `6` PP restore, item `8` HP+PP restore,
item `10` debuff clear success/warning, item `12` stronger revive, and
item `8/9` warning code `7` in panel state17/petsetting paths.

Rules for the next step:

- Do not recreate Phase 9 or add more skill-row smoke just to reduce matrix
  anxiety; `187` is the controlling closeout.
- Do not code random item behavior before the `227` matrix and focused smoke
  plan are used.
- Keep `battle_quick` after every battle code change.
- Keep `panel_wheel` after every input/list/panel UI change.
- Use Battle Lab suites for focused NPC/catch item and skill testing, but keep
  behavior in shared runtime code.
- Keep route regressions for Sophie/Bunny/Elder after any battle state/route
  change.
- Generic decoded event VM remains out of scope unless a separate source-backed
  design task is chosen.
