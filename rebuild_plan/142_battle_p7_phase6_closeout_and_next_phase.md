# 142 Battle P7 Phase 6 Closeout And Next Phase

## Purpose

Close the current P7 work instead of continuing to loop on visual/P7 edge debt.

This closeout covers:

- P7 effect animation engine for current story-route battles;
- P7 hit/damage/HP/text timing;
- P7 KO/death state `3`;
- P7 `game.d.q()` post-skill/follow-up branches proven so far;
- explicit remaining `PENDING` items that should not block moving on.

## Source Scope Covered

Primary source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/source_code/decoded/decompiled_source_cfr/l.java`

Primary data/assets:

- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/blood.mid`
- `modules/script/original/pos.mid`
- `modules/script/original/cpos.mid`
- `modules/spr/original/*`

Recent audit chain:

- `134_battle_p7_hit_recoil_blood_timing.md`
- `135_battle_p7_actor_recoil_source_audit.md`
- `136_battle_p7_sprite_cursor_timing_audit.md`
- `137_battle_p7_original_vs_rebuild_frame_compare.md`
- `138_battle_p7_damage_text_hp_tween_audit.md`
- `139_battle_p7_death_state3_transition_audit.md`
- `140_battle_p7_death_original_compare_and_queue_transition.md`
- `141_battle_p7_game_d_q_followup_branches_audit.md`

## Closeout Matrix

| Area | Status | Evidence |
| --- | --- | --- |
| P7 source effect row sequencing | PORTED/PARTIAL | effect.mid chunk progression, chunk[4]/[5]/[6] trigger hooks, skill 15/45 smoke. |
| P7 base actor state `1` attack | PORTED/PARTIAL | Source sprite/cpos compare smoke for Elder/Bunny. |
| P7 base actor state `2` hit | PORTED/PARTIAL | Source sprite/cpos compare smoke for Elder/Bunny. |
| Synthetic recoil/lunge offsets | REMOVED | Source audit proved `S.a/S.b` are HP bar helpers, not recoil offsets. |
| Damage formula bridge | PORTED/PARTIAL | `BattleDamageResult` preserved through P7; formula check passes. |
| Damage text `blood.mid` lifecycle | PORTED/PARTIAL | Text lifetime follows `blood.mid` row frame count. |
| HP HUD tween | PORTED/PARTIAL | Source-shaped `game.h.a/b` delay/step model, display HP separated from real HP. |
| Death state `3` timing | PORTED/PARTIAL | KO waits until text/HP tween complete, then enters state `3`. |
| AH type16 death visual | PORTED/PARTIAL | Source row shape and actor hide/wait are ported; exact MIDP drawRGB parity pending. |
| Dead actor hidden after death effect | PORTED/PARTIAL | Dead base actor stays hidden through P8/P9; P15 replacement resets. |
| P7 -> P12/P13 queue order | PORTED/PARTIAL | Trace-order smoke for player buff queue and enemy debuff queue. |
| Buff12 `K12=2` follow-up P2 | PORTED/PARTIAL | Source branch added and smoke-covered. |
| Skill63/69 follow-up P2 roll | PORTED/PARTIAL | Source RNG roll path added; skill63 smoke covers shared path. |
| Route regressions | PASS | Sophie, Bunny, Elder route checkpoints pass after latest P7 patches. |

## Focused Smoke Bundle

Representative checkpoints for this closeout:

| Checkpoint | Purpose |
| --- | --- |
| `battle_elder_p7_state1_attack_source_compare` | Base attack state/cursor/anchor. |
| `battle_elder_p7_state2_hit_source_compare` | Base hit state/cursor/anchor. |
| `battle_bunny_p7_state1_attack_source_compare` | Bunny route base attack compare. |
| `battle_bunny_p7_state2_hit_source_compare` | Bunny route base hit compare. |
| `battle_elder_p7_damage_hp_delay` | Initial HP tween delay. |
| `battle_elder_p7_damage_hp_tween_step` | HP tween step begins after source-shaped delay. |
| `battle_elder_p7_damage_text_lifecycle` | Damage text lifetime completes before transition. |
| `battle_elder_p7_death_state3_effect_start` | KO enters state `3` after HP/text complete. |
| `battle_elder_p7_death_to_p8_after_effect` | P8 happens after death effect wait. |
| `battle_p7_to_p13_queue_order_skill45` | P7 post-effect precedes P13 active queue. |
| `battle_p7_to_p12_queue_order_debuff0` | P7 damage/debuff precedes P12 active queue. |
| `battle_p7_q_buff12_followup_p2` | Buff12 K12 follow-up branch. |
| `battle_p7_q_skill63_followup_p2` | Skill63/69 shared follow-up branch. |

Route regressions:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Honest Remaining Debt

These are real, but they should not keep Phase 6/P7 open:

| Debt | Status | Why it does not block closeout |
| --- | --- | --- |
| Original-client AH type16 pixel compare | PENDING | No original battle KO capture exists in repo. Cannot claim or fix pixel parity without source capture. |
| Exact Java ME `drawRGB(..., true)` blending | PENDING | Rebuild is source-shaped Java2D, not MIDP pixel-certified. |
| Full source turn-vector `i++/p()` parity | PARTIAL | Current routes and focused P7 follow-up branches are smoked; rare multi-unit edges belong to later broad state/skill coverage. |
| Skill69 dedicated smoke | PARTIAL | Same code path as skill63 with different source chance; add only when a real route/coverage matrix needs it. |
| Broad skill coverage for all `aq.c[1]` rows | LATER | This is Phase 9, not a reason to keep P7 phase open. |

## Phase Decision

P7/Phase 6 is closed at:

```text
PORTED/PARTIAL for current story-route behavior.
PENDING only for original-client pixel parity and broad rare-route coverage.
```

Do not continue adding P7 visual tweaks unless:

1. a same-route original-client frame capture exists; or
2. a source route exposes a concrete P7 state/skill mismatch; or
3. the Phase 9 skill coverage matrix explicitly selects a P7 skill row.

## Next Phase

Move to **Phase 8: Battle Entry/Exit + Event Integration**, because current
Phase 5/6/7 route behavior is stable enough that route transitions are no
longer masking core P7 bugs.

Recommended next concrete slice:

1. Audit battle entry/exit result integration for the next user-visible route.
2. Compare event opcode result writes, save/task state, free-world return state,
   and post-battle UI consumer.
3. Patch only the smallest source-proven state transition or event consumer.

If the user wants broad battle-engine internals instead of route integration,
the alternate next phase is **Phase 9 skill coverage**, starting with a source
matrix for currently unsmoked `aq.c[1]` skill rows. Do not start Phase 9 before
writing that matrix.
