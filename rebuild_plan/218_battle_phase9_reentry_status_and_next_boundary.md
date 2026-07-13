# 218 - Battle Phase 9 Re-entry Status And Next Boundary

Status: RE-ENTRY AUDIT / NO RUNTIME CODE CHANGE.

Purpose:

- User asked to move into Phase 9 broad skill coverage/current selected skill
  slice.
- Re-read the roadmap and discovered the master roadmap was stale: Phase 9 had
  already been completed in docs `155..187`.
- Verify the actual current state and update the master roadmap so a future
  chat does not repeat Phase 9.

## Source / Project Facts

| Item | Result |
| --- | --- |
| Phase 9 entry doc | `155_battle_phase9_skill_coverage_entry_plan.md` exists. |
| Phase 9 matrix | `156_battle_phase9_skill_coverage_matrix.md` exists. |
| Phase 9 closeout | `187_battle_phase9ad_skill_coverage_closeout.md` closes Phase 9. |
| Skill row stale scan | PASS: no `aq.c[1][0..69]` row remains `MISSING`, `AUDITED/PENDING`, `UNKNOWN`, or `PENDING PRODUCER`. |
| Runtime code change | None in this re-entry step. |

## Phase 9 Status

Classification:

```text
Phase 9 = CLOSED AS PORTED/PARTIAL + SMOKE-COVERED
```

Meaning:

- Every skill id `0..69` has a smoke checkpoint, family smoke plus sibling
  proof, or a source-backed `NOT_REACHED` classification.
- `SOURCE_SWITCH_GAP` skills are no longer guessed from table rows; they were
  bytecode/control-flow audited in `174` and smoke-covered in later slices.
- Skill coverage is closed at the producer/result level.

Not claimed:

- Exact RNG stream parity.
- Pixel-perfect P7 animation/effect parity.
- Full UI widget runtime parity.
- Full passive/global save-state parity.
- Complete battle engine parity outside skill coverage.

## Re-entry Smoke

Focused representative checkpoints were run after build:

| Checkpoint | Purpose | Result |
| --- | --- | --- |
| `battle_phase9w_skill64_selected_buff_copy` | Selected-index buff copy / skill64. | PASS |
| `battle_phase9u_direct_self_buff_skill_68` | Direct damage + debuff10 + self-buff10. | PASS |
| `battle_phase9m_zero_power_success_skill_54` | Zero-power source guard / debuff8 not reached. | PASS |
| `battle_phase9ab_skill65_producer_to_followup` | Buff12 producer -> P13 -> follow-up P2. | PASS |
| `battle_phase9l_debuff10_catch_chance_after_skill41` | Debuff10 catch chance consumer. | PASS |

PNG output folder:

```text
rebuild_game/build_intro_demo/phase9_reentry/
```

## Master Roadmap Update

Updated:

- `rebuild_plan/battle_engine_master_roadmap_progress.md`

Changes:

- Phase 9 row now points to `155..187` and is marked closed.
- Phase 10 row remains closed around `190..192` and `battle_quick`.
- Phase 11 row is now visible as the current UI widget runtime parity boundary,
  with docs `193..199`.
- Immediate next step no longer says to create `156`.

## Next Boundary

Recommended next choices:

1. Continue Phase 11 with `petstate.ui` runtime parity if the next battle flow
   needs pet switch/item target UI fidelity.
2. Start Phase 12 audit for remaining battle animation/effect gaps outside
   `choice.ui`, `msgwarm.ui`, and `openbox.ui`.
3. If gameplay logic is preferred, create a separate exact RNG/passive-hook
   parity audit. Do not mix it into Phase 9.

Rule:

- Do not reopen Phase 9 just to add more skill-row smoke unless a real
  source-route mismatch is found.
