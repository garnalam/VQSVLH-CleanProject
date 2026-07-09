# 187 Battle Phase 9-AD Skill Coverage Closeout

Status date: 2026-07-09

Status: PHASE 9-AD / AUDIT-CLOSEOUT / NO RUNTIME CODE CHANGE.

Purpose:

- Audit the full Phase 9 skill coverage roadmap after docs `155..186`.
- Confirm `156_battle_phase9_skill_coverage_matrix.md` no longer has skill rows marked `MISSING`, `AUDITED/PENDING`, or `UNKNOWN`.
- Separate what Phase 9 actually closes from broader battle-engine work that remains.

## Sources Read

Primary:

- `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`
- `rebuild_plan/157_battle_phase9_crit_dodge_miss_result_flow_audit.md`
- `rebuild_plan/158_battle_phase9b_direct_smoke_coverage.md`
- `rebuild_plan/159_battle_phase9c_formula_variant_smoke_coverage.md`
- `rebuild_plan/160_battle_phase9d_post_hit_followup_miss_coverage.md`
- `rebuild_plan/161_battle_phase9e_debuff1_family_coverage.md`
- `rebuild_plan/162_battle_phase9f_debuff2_family_coverage.md`
- `rebuild_plan/164_battle_phase9g_debuff3_family_coverage.md`
- `rebuild_plan/165_battle_phase9h_debuff4_family_coverage.md`
- `rebuild_plan/166_battle_phase9i_debuff5_family_coverage.md`
- `rebuild_plan/167_battle_phase9j_debuff6_family_coverage.md`
- `rebuild_plan/168_battle_phase9k_debuff7_family_coverage.md`
- `rebuild_plan/169_battle_phase9l_debuff10_family_coverage.md`
- `rebuild_plan/170_battle_phase9m_zero_power_debuff8_9_coverage.md`
- `rebuild_plan/171_battle_phase9n_target_buff_clear_coverage.md`
- `rebuild_plan/172_battle_phase9o_hp_percent_scaling_coverage.md`
- `rebuild_plan/173_battle_phase9p_direct_simple_gap_cleanup.md`
- `rebuild_plan/174_battle_phase9q_source_switch_gap_audit.md`
- `rebuild_plan/175_battle_phase9r_raw_damage_self_buff_smoke.md`
- `rebuild_plan/176_battle_phase9s_skill67_effectmode2_audit.md`
- `rebuild_plan/177_battle_phase9t_skill67_raw_visual_smoke.md`
- `rebuild_plan/178_battle_phase9u_skill68_direct_debuff_selfbuff_smoke.md`
- `rebuild_plan/179_battle_phase9v_skill64_selected_index_audit.md`
- `rebuild_plan/180_battle_phase9w_skill64_selected_buff_copy_smoke.md`
- `rebuild_plan/181_battle_phase9x_no_damage_effectmode1_audit.md`
- `rebuild_plan/182_battle_phase9y_no_damage_buff_producer_smoke.md`
- `rebuild_plan/183_battle_phase9z_cleanse_protection_producer_smoke.md`
- `rebuild_plan/184_battle_phase9aa_defensive_hook_producer_smoke.md`
- `rebuild_plan/185_battle_phase9ab_skill65_producer_to_consumer_smoke.md`
- `rebuild_plan/186_battle_phase9ac_missing_formula_smoke_closeout.md`

Harness:

- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Matrix Result

Scan:

```text
rg -n "\| `\d+` \|.*\| (MISSING|AUDITED|PENDING|UNKNOWN)|\| `\d+` \|.*MISSING|PENDING PRODUCER|AUDITED/PENDING" 156_battle_phase9_skill_coverage_matrix.md
```

Result:

```text
no skill-row matches
```

Meaning:

- Every skill id `0..69` now has a matrix row with a checkpoint or explicit source classification.
- No skill row is left as `MISSING`.
- No skill row is left as `AUDITED/PENDING`.
- `NOT_REACHED` is used only where source proves the table field is not consumed from that path, especially skill `54/55` debuff ids `8/9` under P7 zero-power guard and skill `67` effect id `5`.

## Coverage By Family

| Family / area | Skill ids | Phase/docs | Closeout status |
| --- | --- | --- | --- |
| Direct-simple no-extra-effect | `0,6,10,16,20,26,30,36,40,46,50,56,60,66` | 158, 173 | PORTED/PARTIAL, smoke-covered. |
| Plus-divisor | `1,7` | 159, 186 | PORTED/PARTIAL, smoke-covered. |
| Conditional debuff0/debuff1 damage | `3,9,23,29` | 159, 186 | PORTED/PARTIAL, smoke-covered. |
| Explicit debuff chance id1/id10 | `2,8,22,28,41,47` | 161, 169 | PORTED/PARTIAL, smoke-covered. |
| Implicit debuff families | `12,13,18,19,31,32,33,37,38,39,51,57,61` | 162, 164, 165, 166, 167, 168 | PORTED/PARTIAL, smoke-covered. |
| Zero-power guard | `54,55` | 170 | PORTED/PARTIAL no-damage guard; table debuff ids `8/9` are NOT_REACHED from P7. |
| Target buff clear | `43,49` | 171 | PORTED/PARTIAL, smoke-covered. |
| HP percent scaling | `53,59` | 172 | PORTED/PARTIAL, smoke-covered. |
| Heal/leech/follow-up q() | `11,17,52,58,63,69` | 160 | PORTED/PARTIAL, smoke-covered including miss interactions. |
| Bytecode source-switch gap | `21,27,42,48,62,67` | 174, 175, 176, 177 | PORTED/PARTIAL, smoke-covered; skill `67` no q()/no debuff side effect is source-backed. |
| Skill68 direct + debuff10 + self-buff10 | `68` | 178 | PORTED/PARTIAL, smoke-covered. |
| Selected-index buff copy/clear | `64` | 179, 180 | PORTED/PARTIAL, smoke-covered for one selected enemy target. |
| No-damage/default buff producer | `4,5,14,24,25,34,35,44,65` | 181, 182, 183, 184, 185 | PORTED/PARTIAL, smoke-covered. |
| Skill15/45 effect anchors | `15,45` | earlier P7 docs plus 156 anchors | PARTIAL but smoke-covered as P7/effect regression anchors. |

## What Phase 9 Closes

Phase 9 closes:

- Skill table coverage for `aq.c[1][0..69]` at the battle-result/producer level.
- Dedicated smoke or source-backed `NOT_REACHED` classification for every skill row.
- Crit/miss result flow at a source-shaped level.
- Debuff producer families used by skills.
- Key post-skill `game.d.q()` consumers:
  - heal
  - leech
  - follow-up
  - self-buff
  - selected-index buff copy
  - buff12 producer-to-P13-to-follow-up
- Important formula variants:
  - plus-divisor
  - conditional debuff damage
  - HP percent scaling
  - target buff clear
  - bytecode-default raw damage.

## What Phase 9 Does Not Close

Do not overclaim these as complete:

| Area | Status | Why it remains outside Phase 9 |
| --- | --- | --- |
| Exact RNG stream parity | PARTIAL | Most smoke uses deterministic hooks to prove branch behavior, not full Java ME RNG stream equivalence. |
| Pixel-perfect P7 animation/effect parity | PARTIAL/PENDING | Phase 9 validates logic/result/effect row traces; exact MIDP visual matching remains separate. |
| Full HUD status icon parity | PENDING | Status icon sprite/resource work is referenced separately, e.g. sprite 325/img804 audit. |
| Full multi-target/formation parity | PENDING | Skill64 selected-index smoke covers one enemy target slot, not all source formations. |
| Passive hooks beyond covered branches | PARTIAL/PENDING | Some passives are represented by hooks but not exhaustively wired to global/save parity. |
| Full command/item/catch/pet UI runtime parity | PARTIAL/PENDING | Phase 9 is skill coverage, not full `game.h` widget runtime parity. |
| Full battle engine completion | NOT CLAIMED | EXP, level-up, evolution, item/pet/catch, and event integration have their own phases and docs. |

## Verification Performed In This Closeout

This closeout did not change runtime code.

Checks from Phase 9-AC just before this audit:

| Check | Result |
| --- | --- |
| Phase 9-AC five smoke checkpoints | PASS |
| Matrix skill-row stale status scan | PASS, no matches |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| Route Sophie/Bunny/Elder smoke regression | PASS |

## Closeout Decision

Phase 9 skill coverage is complete enough to move on.

Classification:

```text
Phase 9 = CLOSED AS PORTED/PARTIAL + SMOKE-COVERED
```

This means:

- Good to stop adding more skill-row smoke just to reduce matrix gaps.
- Not good to claim the whole battle engine is 100% complete.
- Remaining work should be handled as new phases with narrower names, not as more Phase 9 drift.

## Recommended Next Phase

Recommended:

```text
Phase 10: Battle Visual / Status UI Parity
```

First slice:

```text
Phase 10-A: status icon/effect overlay audit and port plan.
```

Why:

- User already noticed status/effect visuals may draw on pets.
- Phase 9 repeatedly leaves `HUD status icon sprite 325 / img804` and pixel-perfect effect parity as pending.
- This is the clean next boundary after skill logic coverage: visual presentation of statuses/effects, not more formula logic.

Alternative if gameplay logic is preferred first:

```text
Phase 10-A alt: exact RNG/passive hook parity audit.
```

But visual/status UI is more visible to manual testing and less likely to reopen every skill formula.
