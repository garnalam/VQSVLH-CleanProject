# 155 Battle Phase 9 Skill Coverage Entry Plan

Status date: 2026-07-09

Status: PHASE 9 ENTRY / AUDIT-FIRST / NO CODE CHANGE.

Purpose:

- Move from Phase 8 wrapper/event integration to Phase 9 broad skill coverage.
- Define the first safe Phase 9 slice before any new skill behavior/effect code.
- Prevent broad skill work from becoming random visual/effect patching.

## Why Phase 9 Now

Recent closeouts:

- `150_battle_phase8_state10_freeworld_resume_audit.md`
- `151_battle_phase8_world_resume_descriptor_closeout.md`
- `152_battle_phase8_bunny_return_world_resume_descriptor_closeout.md`
- `153_battle_phase8_group6_freeworld_resume_descriptor_closeout.md`
- `154_battle_phase8_world_resume_wrappers_summary.md`

These close the current manual Phase 8 world-resume boundaries:

| Boundary | Status |
| --- | --- |
| room1 save -> op13 Bunny | `PORTED/PARTIAL` trace-only |
| room1 Bunny complete -> room0 transition | `PORTED/PARTIAL` trace-only |
| room0 group6 complete -> post-group6 free-world | `PORTED/PARTIAL` trace-only |

Phase 8 is not a full generic event VM, but the current battle/save/free-world
route boundaries are protected. Unless a concrete missing free-world trigger is
chosen, the next roadmap phase is Phase 9.

## Phase 9 Scope

Phase 9 target:

```text
Broad skill coverage for aq.c[1][0..69].
```

This does not mean porting all 70 skills at once. It means building a coverage
matrix and then selecting small source-backed slices.

Primary source/audit inputs:

- `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`
- `rebuild_plan/73_battle_rebuild_mapping_next_code_tasks.md`
- `rebuild_plan/141_battle_p7_game_d_q_followup_branches_audit.md`
- `rebuild_plan/142_battle_p7_phase6_closeout_and_next_phase.md`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/effect/decoded` / `modules/speffect/decoded` if a selected skill
  actually calls P7 effects.

## Current Skill Families From Doc 72

| Family | Skill ids | Current status |
| --- | --- | --- |
| Direct simple | `0,6,10,11,12,13,16,17,18,19,20,26,30,31,32,33,36,37,38,39,40,46,50,51,52,54,55,56,57,58,60,61,63,66,68,69` | Formula mostly `PORTED/PARTIAL`; smoke coverage incomplete. |
| Direct plus divisor | `1,7` | Formula `PORTED/PARTIAL`; debuff lifecycle coverage needed. |
| Explicit debuff chance | `2,8,22,28,41,47` | Debuff application source-shaped; ids `8/9/10` still need proof/coverage. |
| Conditional target debuff | `3,9,23,29` | Formula exists; needs deterministic setup/smoke for target debuff condition. |
| Clear target buffs | `43,49` | Logic exists partially; needs active-buff setup/smoke. |
| HP percent scaling | `53,59` | Formula exists; needs source clamp/min-damage check. |
| Post-hit heal / leech | `11,17,52,58` | `PARTIAL`; needs P7/q result lifecycle smoke. |
| Self buff / effect mode 1 | `21,27,42,48,62,68` and default effect-mode rows | `PARTIAL/PENDING`; do not guess `SOURCE_SWITCH_GAP` damage. |
| Selected-index buff | `64` | `PENDING`; needs target-index parity before code. |
| Source switch gap | `21,27,42,48,62,67` | `UNKNOWN/PENDING`; requires bytecode/control-flow validation. |

## Rules For Phase 9

- Logic source first, UI/effect second.
- A skill is not `PORTED` just because damage appears on screen.
- For each selected skill, prove:
  - `aq.c[1][skill]` row;
  - source method path in `game.b` and/or `game.d.q()`;
  - target mode via `skill[9]`;
  - P7 effect row only if source calls it;
  - buff/debuff/passive queue consumer if any;
  - smoke setup and expected branch/result.
- Do not reopen P7 pixel parity unless the selected skill requires a source
  effect path not already covered.
- Do not port `SOURCE_SWITCH_GAP` skills by table shape alone.
- Do not touch intro/world/scene scripts unless a selected skill route proves a
  battle exit regression.

## First Phase 9 Artifact

Create the actual coverage matrix:

```text
156_battle_phase9_skill_coverage_matrix.md
```

Required columns:

| Column | Meaning |
| --- | --- |
| Skill id | `aq.c[1]` row index. |
| Source family | From doc `72`, verified against source if needed. |
| Current rebuild logic | `PORTED / PARTIAL / PENDING / UNKNOWN`. |
| Current smoke | Existing checkpoint name, or `MISSING`. |
| Needed setup | Enemy/pet/status/buff/debuff state required for deterministic test. |
| UI/effect dependency | P3/P6/P7/P12/P13/P8/etc. |
| Risk | Low/medium/high. |
| First action | Audit only / smoke only / code slice. |

## Recommended First Slices After Matrix

Recommended order:

1. **Smoke-only coverage for already-ported direct formula families.**
   - Goal: prove no hidden regression before adding new logic.
   - Candidate skills: `0/10`, `1/7`, `3/23`, `43`, `53`.

2. **Post-hit heal/leech lifecycle.**
   - Candidate skills: `11`, `17`, `52`, `58`.
   - Reason: source behavior is known, visible, and route-safe.

3. **Conditional debuff formula coverage.**
   - Candidate skills: `3/9/23/29`.
   - Requires deterministic target debuff setup.

4. **Clear target buffs.**
   - Candidate skills: `43/49`.
   - Requires deterministic active target buff setup.

5. **Only then handle `SOURCE_SWITCH_GAP` skills.**
   - Candidate skills: `21/27/42/48/62/67`.
   - Requires dedicated bytecode/control-flow audit first.

## Verification Bundle For Future Code Slices

After any Phase 9 code slice:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java -cp .\build\classes com.vqsv.rebuild.Main --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- Java mojibake literal scan
- skill-specific smoke PNG
- route regressions:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Decision

Phase 9 starts with `156_battle_phase9_skill_coverage_matrix.md`.

Do not code a skill yet. The next step is to build the matrix, then pick the
smallest source-backed skill family slice from it.
