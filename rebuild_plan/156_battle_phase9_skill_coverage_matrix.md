# 156 Battle Phase 9 Skill Coverage Matrix

Status date: 2026-07-09

Status: PHASE 9 / AUDIT MATRIX / NO CODE CHANGE.

Purpose:

- Start Phase 9 broad skill coverage for `aq.c[1][0..69]`.
- Convert the classification in `72_battle_full_skill_status_behavior_classification.md`
  into a practical smoke/code planning matrix.
- Pick small source-backed slices instead of randomly porting effects or visuals.

Rules for this phase:

- Logic source first, UI/effect second.
- A skill is not `PORTED` just because damage appears in a smoke PNG.
- Every selected skill must prove: source row, source family, target mode,
  RNG gates, P7/P12/P13/P8 consumer, and smoke setup.
- Do not port `SOURCE_SWITCH_GAP` skills from row shape alone.
- Do not reopen P7 visual work unless a selected skill proves a concrete gap.

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`
- `rebuild_plan/74_battle_game_d_state_full_matrix.md`
- `rebuild_plan/75_battle_game_d_q_post_skill_matrix.md`
- `rebuild_plan/134_battle_p7_hit_recoil_blood_timing.md`
- `rebuild_plan/142_battle_p7_phase6_closeout_and_next_phase.md`

## Source Skill Families

`game.b.b(target)` is the main formula/result producer.

| Family | Skill ids | Source behavior | Current rebuild status |
| --- | --- | --- | --- |
| `DIRECT_SIMPLE` | `0,6,10,11,12,13,16,17,18,19,20,26,30,31,32,33,36,37,38,39,40,46,50,51,52,54,55,56,57,58,60,61,63,66,68,69` | Damage = attack base * `skill[3] / 100`; optional debuff from `skill[7]`. | PORTED/PARTIAL. Formula exists; coverage uneven. |
| `DIRECT_PLUS_RAW_DIVISOR` | `1,7` | Damage = direct percent + raw attack / `skill[8]`; debuff id `0`. | PORTED/PARTIAL. Debuff lifecycle has smoke but not full broad coverage. |
| `DIRECT_EXPLICIT_DEBUFF_CHANCE` | `2,8,22,28,41,47` | Direct damage; debuff chance from `skill[8]`. | PORTED/PARTIAL. Debuff id `1` and debuff id `10` families are smoke-covered; exact RNG stream parity remains partial. |
| `CONDITIONAL_IF_TARGET_DEBUFF0` | `3,9` | Uses alternate percent `skill[8]` if target has debuff `0`. | PORTED/PARTIAL. Deterministic condition smoke-covered by Phase 9-C/AC. |
| `CONDITIONAL_IF_TARGET_DEBUFF1` | `23,29` | Uses alternate percent `skill[8]` if target has debuff `1`. | PORTED/PARTIAL. Deterministic condition smoke-covered by Phase 9-C/AC. |
| `DIRECT_AND_CLEAR_TARGET_BUFFS` | `43,49` | Direct damage, then `target.D()` clears target buffs. | PORTED/PARTIAL. Covered by Phase 9-N active target buff setup smoke. |
| `HP_PERCENT_SCALING` | `53,59` | Damage depends on attacker HP percent. | PORTED/PARTIAL. Covered by Phase 9-O low/high HP, min-clamp, and miss smoke. |
| `DQ_HEAL_ATTACKER` | `11,17` | `game.d.q()` heals attacker after damage/miss lifecycle. | PORTED/PARTIAL. Skills 11/17 have hit/miss interaction smoke. |
| `DQ_DAMAGE_LEECH_IF_AA` | `52,58` | Chance flag `aa`, then heal from damage in `game.d.q()`; source `q()` does not check hit. | PORTED/PARTIAL. Skills 52/58 have hit/miss interaction smoke; skill52 visible heal can round to 0. |
| `DQ_SELF_BUFF` | `21,27,42,48,62,68` | `game.d.q()` applies self buff; `21/27/42/48/62` use bytecode-default raw damage before q(); `68` uses direct formula and also target debuff10. | PORTED/PARTIAL for all listed ids after Phase 9-R/9-U smoke. |
| `DQ_BUFF_WITH_SELECTED_INDEX` | `64` | Applies buff id `11` using selected index/target. | PENDING. Needs selected-index parity. |
| `SOURCE_SWITCH_GAP` | `21,27,42,48,62,67` | Decompiled switch was ambiguous; Phase 9-Q bytecode shows all six go to default raw-damage branch in `game.b.b(target)`. | PORTED/PARTIAL smoke for all six; `21/27/42/48/62` have q() self-buff smoke, `67` has no-debuff/no-q visual smoke. |

## RNG Gates To Track

| Gate | Source location | Meaning | Rebuild coverage |
| --- | --- | --- | --- |
| Critical | `game.b.b(target)` before formula family switch | Base chance 5, final-form chance 30, plus speed/2 and status 4. | PORTED/PARTIAL in `BattleDamageResult.critFlag`. |
| Dodge/miss | `game.d` P7 damage frame | Target speed minus attacker avoid base, clamped `0..20`; status 9 disables miss. | PORTED/PARTIAL. Hit/miss path exists; passive hook 4 still pending. |
| Debuff chance | `game.b.b(target)` after family switch | Explicit chance, anti-debuff status 3, buff 14 block, duration/passive duration modifier. | PORTED/PARTIAL. Debuff ids used by skill table are family-smoke-covered; exact RNG stream parity remains partial. |
| Damage jitter | `game.b.b(target)` after relation/passives | `ae.a(100)` changes very small damage by +/-1, with min clamp. | PORTED/PARTIAL. Trace exists; exact RNG stream parity is approximate. |
| Buff5/buff6 hooks | `game.b.b(target)` final modifiers | Stored reflect/conversion hooks. | PORTED/PARTIAL. Phase 9-AA smoke-covers buff5 reflect and buff6 source-odd formula hook. |
| Follow-up turn | `game.d.q()` | Buff12 or skill `63/69` can route back to P2. | PORTED/PARTIAL. Buff12, skill63, and skill69 are smoked; miss interactions for 63/69 are smoked. |
| Heal/leech | `game.d.q()` | Skills `11/17/52/58` and status 8 can heal attacker. | PORTED/PARTIAL. Hit/miss interactions for 11/17/52/58 are smoked; status8 broad coverage pending. |

## Current Smoke Coverage Anchors

Known useful checkpoints:

| Area | Existing smoke checkpoint(s) | Coverage meaning |
| --- | --- | --- |
| Direct P7 skill 10 | `battle_elder_p7_damage_frame`, `battle_elder_p7_damage_hp_tween_step`, `battle_elder_p7_damage_text_lifecycle` | P7 damage/tween/text path for a direct skill. |
| Debuff id 0 via skill 1 | `battle_elder_p7_damage_result_debuff`, `battle_p7_to_p12_queue_order_debuff0`, `battle_p12_debuff0_damage_text` | Damage result preserves debuff and P12 queue consumes it. |
| Heal skill 11 | `battle_elder_p7_q_heal_skill11` | `game.d.q()` hardcoded heal branch. |
| Buff skill 45 | `battle_elder_p7_q_buff_skill45`, `battle_p7_to_p13_queue_order_skill45`, `battle_elder_p7_speffect45_*` | No-damage buff/effect route plus P13 queue. |
| Leech skill 58 | `battle_elder_p7_q_leech_skill58` | `aa` leech branch visible. |
| Skill 15 chunk/effect | `battle_elder_p7_skill15_start`, `battle_elder_p7_skill15_chunk4_trigger`, `battle_elder_p7_skill15_after` | P7 no-damage actor/speffect chunk trigger. |
| Follow-up | `battle_p7_q_buff12_followup_p2`, `battle_p7_q_skill63_followup_p2` | P7 post-skill can return to P2. |
| Phase 9-D sibling/miss | `battle_phase9d_hit_heal_skill_17`, `battle_phase9d_hit_leech_skill_52`, `battle_phase9d_hit_followup_skill_69`, `battle_phase9d_miss_*` | Sibling skills 17/52/69 and miss interactions for 11/17/52/58/63/69. |
| Phase 9-E debuff1 family | `battle_phase9e_debuff1_success_skill_2/8/22/28`, `battle_phase9e_debuff1_buff14_block_skill_2`, `battle_phase9e_debuff1_status3_reduced_block_skill_2`, `battle_phase9e_debuff1_miss_queue_skill_2` | Explicit debuff id 1 family and source miss/queue behavior. |
| Phase 9-F debuff2 family | `battle_phase9f_debuff2_success_skill_12/18`, `battle_phase9f_debuff2_buff14_block_skill_12`, `battle_phase9f_debuff2_status3_block_skill_12`, `battle_phase9f_debuff2_miss_queue_skill_12` | Implicit debuff id 2 family and source miss/queue behavior. |
| Phase 9-G debuff3 family | `battle_phase9g_debuff3_success_skill_13/19`, `battle_phase9g_debuff3_buff14_block_skill_13`, `battle_phase9g_debuff3_status3_block_skill_13`, `battle_phase9g_debuff3_miss_queue_skill_13`, `battle_p12_debuff3_*` | Implicit delayed-damage debuff id 3 family and source miss/queue behavior. |
| Phase 9-H debuff4 family | `battle_phase9h_debuff4_success_skill_31/37`, `battle_phase9h_debuff4_buff14_block_skill_31`, `battle_phase9h_debuff4_status3_block_skill_31`, `battle_phase9h_debuff4_miss_queue_skill_31`, `battle_phase9h_debuff4_miss_chance_skill_31` | Implicit debuff id 4 family, source miss/queue behavior, and P7 miss chance consumer. |
| Phase 9-I debuff5 family | `battle_phase9i_debuff5_success_skill_32/38/61`, `battle_phase9i_debuff5_buff14_block_skill_32`, `battle_phase9i_debuff5_status3_block_skill_32`, `battle_phase9i_debuff5_miss_queue_skill_32`, `battle_phase9i_debuff5_stat_consumer_skill_32` | Implicit speed-down debuff id 5 family, source miss/queue behavior, and stat consumer. |
| Phase 9-J debuff6 family | `battle_phase9j_debuff6_success_skill_33/39`, `battle_phase9j_debuff6_buff14_block_skill_33`, `battle_phase9j_debuff6_status3_block_skill_33`, `battle_phase9j_debuff6_miss_queue_skill_33`, `battle_phase9j_debuff6_damage_reduction_skill_33` | Implicit damage-reduction debuff id 6 family, source miss/queue behavior, and formula consumer. |
| Route regressions | `route_sophie_after_battle_branch`, `route_bunny_after_battle_task`, `route_elder_after_battle_reward_state` | Story-route safety after battle changes. |

## Skill Coverage Matrix

Legend:

- `Logic`: current rebuild behavior, not full engine status.
- `Smoke`: `MISSING` means no dedicated deterministic checkpoint is known from
  current docs/harness.
- `RNG/deps`: source gates or queue/UI dependencies that must be proven in a
  selected slice.
- `First action`: what to do before code.

| Skill | Family | Logic | Smoke | Needed setup | RNG/deps | Risk | First action |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_0` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `1` | DIRECT_PLUS_RAW_DIVISOR + debuff0 | PORTED/PARTIAL | `battle_p7_to_p12_queue_order_debuff0` | target can receive debuff0 | crit, dodge, debuff chance, jitter, P12 | Medium | Add hit/miss-aware regression after dodge slice. |
| `2` | DIRECT_EXPLICIT_DEBUFF_CHANCE debuff1 | PORTED/PARTIAL | `battle_phase9e_debuff1_success_skill_2`, block/miss Phase 9-E smokes | target can receive debuff1 | crit, dodge, explicit debuff chance | Medium | Covered for success/block/miss queue; exact RNG parity later. |
| `3` | CONDITIONAL_IF_TARGET_DEBUFF0 | PORTED/PARTIAL | `battle_phase9c_cond_debuff0_skill_3` | target has debuff0 before hit | crit, dodge, conditional damage | Medium | Phase 9-AC re-smoke closes matrix gap; conditional debuff0 branch is covered. |
| `4` | DEFAULT_EFFECTMODE1 buff0 | PORTED/PARTIAL | `battle_phase9y_no_damage_buff_skill_4` | same-side target routing via targetMode 1 | P7 no-damage, P13, formula hook | Medium | Phase 9-Y smoke-covers producer no-damage + buff0 active + defense up; later formula hook at duration counter `0` remains broader coverage. |
| `5` | DEFAULT_EFFECTMODE1 buff1 | PORTED/PARTIAL | `battle_phase9y_no_damage_buff_skill_5` | same-side target routing via targetMode 1 | P7 no-damage, P13, formula hook | Medium | Phase 9-Y smoke-covers producer no-damage + buff1 active + defense down; damage boost input active. |
| `6` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_6` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `7` | DIRECT_PLUS_RAW_DIVISOR + debuff0 | PORTED/PARTIAL | `battle_phase9c_plus_divisor_skill_7` | target can receive debuff0 | crit, dodge, debuff chance, P12 | Medium | Phase 9-AC re-smoke closes matrix gap; plus-divisor sibling is covered. |
| `8` | DIRECT_EXPLICIT_DEBUFF_CHANCE debuff1 | PORTED/PARTIAL | `battle_phase9e_debuff1_success_skill_8` | target can receive debuff1 | crit, dodge, explicit debuff chance | Medium | Covered for success; block/miss represented by skill2 family smoke. |
| `9` | CONDITIONAL_IF_TARGET_DEBUFF0 | PORTED/PARTIAL | `battle_phase9c_cond_debuff0_skill_9` | target has debuff0 | crit, dodge, conditional damage | Medium | Phase 9-AC re-smoke closes matrix gap; conditional debuff0 sibling is covered. |
| `10` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_elder_p7_damage_frame`, `battle_phase9b_direct_skill_10` | current elder route | crit, dodge, jitter | Low | Covered by route anchor and Phase 9-P forced-hit smoke. |
| `11` | DIRECT_SIMPLE + DQ_HEAL_ATTACKER | PORTED/PARTIAL | `battle_elder_p7_q_heal_skill11`, `battle_phase9d_miss_heal_skill_11` | attacker can heal | crit, dodge, heal q() | Medium | Covered for hit/miss; broad status8 interaction later. |
| `12` | DIRECT_SIMPLE + debuff2 | PORTED/PARTIAL | `battle_phase9f_debuff2_success_skill_12`, block/miss Phase 9-F smokes | target can receive debuff2 | crit, dodge, implicit debuff, P12 | Medium | Covered for success/block/miss queue; exact RNG parity later. |
| `13` | DIRECT_SIMPLE + debuff3 | PORTED/PARTIAL | `battle_phase9g_debuff3_success_skill_13`, block/miss Phase 9-G smokes | target can receive debuff3 | crit, dodge, implicit debuff, P12 delayed damage | Medium | Covered for success/block/miss queue; exact RNG parity later. |
| `14` | DEFAULT_EFFECTMODE1 buff2 | PORTED/PARTIAL | `battle_phase9y_no_damage_buff_skill_14` | same-side no-damage buff route | P13, formula hook | Medium | Phase 9-Y smoke-covers producer no-damage + buff2 active + defense up. |
| `15` | DEFAULT_EFFECTMODE1 buff3 + P7 chunk | PARTIAL | `battle_elder_p7_skill15_*` | elder skill15 checkpoint | P7 actor/speffect, P13 | Medium | Keep as regression anchor. |
| `16` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_16` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `17` | DIRECT_SIMPLE + DQ_HEAL_ATTACKER | PORTED/PARTIAL | `battle_phase9d_hit_heal_skill_17`, `battle_phase9d_miss_heal_skill_17` | attacker can heal | crit, dodge, heal q() | Medium | Covered for hit/miss; broad status8 interaction later. |
| `18` | DIRECT_SIMPLE + debuff2 | PORTED/PARTIAL | `battle_phase9f_debuff2_success_skill_18` | target can receive debuff2 | crit, dodge, implicit debuff, P12 | Medium | Covered for success; block/miss represented by skill12 family smoke. |
| `19` | DIRECT_SIMPLE + debuff3 | PORTED/PARTIAL | `battle_phase9g_debuff3_success_skill_19` | target can receive debuff3 | crit, dodge, implicit debuff, P12 delayed damage | Medium | Covered for success; block/miss represented by skill13 family smoke. |
| `20` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_20` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `21` | BYTECODE_DEFAULT_RAW_DAMAGE + DQ_SELF_BUFF buff4 | PORTED/PARTIAL | `battle_phase9r_raw_self_buff_skill_21` | default raw damage plus attacker self-buff | crit, dodge, q() buff4 text | High | Covered by Phase 9-R raw damage + self-buff smoke. |
| `22` | DIRECT_EXPLICIT_DEBUFF_CHANCE debuff1 | PORTED/PARTIAL | `battle_phase9e_debuff1_success_skill_22` | target can receive debuff1 | crit, dodge, explicit chance | Medium | Covered for success; block/miss represented by skill2 family smoke. |
| `23` | CONDITIONAL_IF_TARGET_DEBUFF1 | PORTED/PARTIAL | `battle_phase9c_cond_debuff1_skill_23` | target has debuff1 | crit, dodge, conditional damage | Medium | Phase 9-AC re-smoke closes matrix gap; conditional debuff1 branch is covered. |
| `24` | DEFAULT_EFFECTMODE1 buff13 | PORTED/PARTIAL | `battle_phase9z_cleanse_protect_skill_24` | same-side no-damage heal/clear route | P13, clear debuffs | Medium | Phase 9-Z smoke-covers producer no-damage + heal + debuff clear. |
| `25` | DEFAULT_EFFECTMODE1 buff14 | PORTED/PARTIAL | `battle_phase9z_cleanse_protect_skill_25` | same-side no-damage clear/immunity route | P13, debuff block | Medium | Phase 9-Z smoke-covers producer no-damage + debuff clear + later debuff block. |
| `26` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_26` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `27` | BYTECODE_DEFAULT_RAW_DAMAGE + DQ_SELF_BUFF buff4 | PORTED/PARTIAL | `battle_phase9r_raw_self_buff_skill_27` | default raw damage plus attacker self-buff | crit, dodge, q() buff4 text | High | Covered by Phase 9-R raw damage + self-buff smoke. |
| `28` | DIRECT_EXPLICIT_DEBUFF_CHANCE debuff1 | PORTED/PARTIAL | `battle_phase9e_debuff1_success_skill_28` | target can receive debuff1 | crit, dodge, explicit chance | Medium | Covered for success; block/miss represented by skill2 family smoke. |
| `29` | CONDITIONAL_IF_TARGET_DEBUFF1 | PORTED/PARTIAL | `battle_phase9c_cond_debuff1_skill_29` | target has debuff1 | crit, dodge, conditional damage | Medium | Phase 9-AC re-smoke closes matrix gap; conditional debuff1 sibling is covered. |
| `30` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_30` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `31` | DIRECT_SIMPLE + debuff4 | PORTED/PARTIAL | `battle_phase9h_debuff4_success_skill_31`, block/miss/missChance Phase 9-H smokes | target can receive debuff4 | crit, dodge, implicit debuff, P7 miss chance | Medium | Covered for success/block/miss queue and missChance consumer; exact RNG parity later. |
| `32` | DIRECT_SIMPLE + debuff5 | PORTED/PARTIAL | `battle_phase9i_debuff5_success_skill_32`, block/miss/stat Phase 9-I smokes | target speed debuff | crit, dodge, implicit debuff, P12 stat reassert | Medium | Covered for success/block/miss queue and stat consumer; exact RNG parity later. |
| `33` | DIRECT_SIMPLE + debuff6 | PORTED/PARTIAL | `battle_phase9j_debuff6_success_skill_33`, block/miss/formula Phase 9-J smokes | target damage-reduction debuff | crit, dodge, implicit debuff, damage formula consumer | Medium | Covered for success/block/miss queue and formula consumer; exact RNG parity later. |
| `34` | DEFAULT_EFFECTMODE1 buff5 | PORTED/PARTIAL | `battle_phase9aa_defensive_hook_skill_34` | same-side no-damage buff route | buff5 stored-damage hook | Medium | Phase 9-AA smoke-covers producer no-damage buff5 and later attacker `K[5]` reflect consume hook. |
| `35` | DEFAULT_EFFECTMODE1 buff6 | PORTED/PARTIAL | `battle_phase9aa_defensive_hook_skill_35` | same-side no-damage buff route | buff6 source oddity | Medium | Phase 9-AA smoke-covers producer no-damage buff6 and controlled source-odd formula reduction. |
| `36` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_36` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `37` | DIRECT_SIMPLE + debuff4 | PORTED/PARTIAL | `battle_phase9h_debuff4_success_skill_37` | target can receive debuff4 | crit, dodge, implicit debuff, P7 miss chance | Medium | Covered for success; block/miss represented by skill31 family smoke. |
| `38` | DIRECT_SIMPLE + debuff5 | PORTED/PARTIAL | `battle_phase9i_debuff5_success_skill_38` | target speed debuff | crit, dodge, implicit debuff, P12 stat reassert | Medium | Covered for success; block/miss/stat represented by skill32 family smoke. |
| `39` | DIRECT_SIMPLE + debuff6 | PORTED/PARTIAL | `battle_phase9j_debuff6_success_skill_39` | target damage-reduction debuff | crit, dodge, implicit debuff, damage formula consumer | Medium | Covered for success; block/miss/formula represented by skill33 family smoke. |
| `40` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_40` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `41` | DIRECT_EXPLICIT_DEBUFF_CHANCE debuff10 | PORTED/PARTIAL | `battle_phase9l_debuff10_success_skill_41`, block/miss/visual/catch Phase 9-L smokes | target can receive debuff10; catch chance reads debuff10 | crit, dodge, explicit chance, P12 visual, catch chance consumer | High | Covered for success/block/status3/miss/visual/catch consumer; exact RNG/pixel parity later. |
| `42` | BYTECODE_DEFAULT_RAW_DAMAGE + DQ_SELF_BUFF buff7 | PORTED/PARTIAL | `battle_phase9r_raw_self_buff_skill_42` | default raw damage plus attacker self-buff | crit, dodge, q() buff7 text | High | Covered by Phase 9-R raw damage + self-buff smoke. |
| `43` | DIRECT_AND_CLEAR_TARGET_BUFFS | PORTED/PARTIAL | `battle_phase9n_clear_buff_success_skill_43`, miss/no-buff Phase 9-N smokes | target has active buffs | crit, dodge, target.D(), active buff queue clear | Medium | Covered for hit, miss, no-buff, buff slot and active queue clear; exact RNG parity later. |
| `44` | DEFAULT_EFFECTMODE1 buff8 | PORTED/PARTIAL | `battle_phase9y_no_damage_buff_skill_44` | same-side no-damage buff route | PP/damage buff | Medium | Phase 9-Y smoke-covers producer no-damage + buff8 active value; deeper PP/damage hook remains PARTIAL. |
| `45` | DEFAULT_EFFECTMODE1 buff9 + speffect | PARTIAL | `battle_elder_p7_speffect45_*`, `battle_p7_to_p13_queue_order_skill45` | elder skill45 checkpoint | P7 chunks, P13 buff9 | Medium | Keep as regression anchor. |
| `46` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_46` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `47` | DIRECT_EXPLICIT_DEBUFF_CHANCE debuff10 | PORTED/PARTIAL | `battle_phase9l_debuff10_success_skill_47` | target can receive debuff10; catch chance reads debuff10 | crit, dodge, explicit chance, P12 visual, catch chance consumer | High | Covered for success; block/status3/miss/visual/catch represented by skill41 family smoke. |
| `48` | BYTECODE_DEFAULT_RAW_DAMAGE + DQ_SELF_BUFF buff7 | PORTED/PARTIAL | `battle_phase9r_raw_self_buff_skill_48` | default raw damage plus attacker self-buff | crit, dodge, q() buff7 text | High | Covered by Phase 9-R raw damage + self-buff smoke. |
| `49` | DIRECT_AND_CLEAR_TARGET_BUFFS | PORTED/PARTIAL | `battle_phase9n_clear_buff_success_skill_49` | target has active buffs | crit, dodge, target.D(), active buff queue clear | Medium | Covered for hit; miss/no-buff represented by skill43 family smoke. |
| `50` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_50` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `51` | DIRECT_SIMPLE + debuff7 | PORTED/PARTIAL | `battle_phase9k_debuff7_success_skill_51`, block/miss/stat Phase 9-K smokes | target defense debuff | crit, dodge, immediate/stat consumer debuff7 | Medium | Covered for success/block/miss queue and defense stat consumer; exact RNG parity later. |
| `52` | DIRECT_SIMPLE + DQ_DAMAGE_LEECH_IF_AA | PORTED/PARTIAL | `battle_phase9d_hit_leech_skill_52`, `battle_phase9d_miss_leech_skill_52` | source `aa` gate; heal may round to 0 | crit, dodge, aa roll, q() leech | Medium | Covered for hit/miss; exact RNG parity later. |
| `53` | HP_PERCENT_SCALING | PORTED/PARTIAL | `battle_phase9o_hp_scaling_low_high_skill_53`, `battle_phase9o_hp_scaling_min_clamp_skill_53`, `battle_phase9o_hp_scaling_miss_skill_53` | attacker varied HP percent | HP percent factor, min clamp, miss interaction | Medium | Covered; exact RNG stream parity later. |
| `54` | ZERO_POWER_NO_DAMAGE_GUARD, table debuff8 | PORTED/PARTIAL | `battle_phase9m_zero_power_success_skill_54`, gate/no-visual Phase 9-M smokes | P7 skips damage/debuff path because `skill[3] == 0` | no hitroll, no damage text, no debuff apply, no active queue visual | Medium | Source table has debuff8, but P7 guard makes it NOT_REACHED for this skill. |
| `55` | ZERO_POWER_NO_DAMAGE_GUARD, table debuff9 | PORTED/PARTIAL | `battle_phase9m_zero_power_success_skill_55`, no-visual Phase 9-M smoke | P7 skips damage/debuff path because `skill[3] == 0` | no hitroll, no damage text, no debuff apply, no active queue visual | Medium | Source table has debuff9, but P7 guard makes it NOT_REACHED for this skill. |
| `56` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_56` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `57` | DIRECT_SIMPLE + debuff7 | PORTED/PARTIAL | `battle_phase9k_debuff7_success_skill_57` | target defense debuff | crit, dodge, immediate/stat consumer debuff7 | Medium | Covered for success; block/miss/stat represented by skill51 family smoke. |
| `58` | DIRECT_SIMPLE + DQ_DAMAGE_LEECH_IF_AA | PORTED/PARTIAL | `battle_elder_p7_q_leech_skill58`, `battle_phase9d_miss_leech_skill_58` | leech roll forced | crit, dodge, aa roll | Medium | Covered for hit/miss; exact RNG parity later. |
| `59` | HP_PERCENT_SCALING | PORTED/PARTIAL | `battle_phase9o_hp_scaling_low_high_skill_59` | attacker varied HP percent | HP percent factor | Medium | Covered for low/high HP; min clamp and miss represented by skill53 family smoke. |
| `60` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_60` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `61` | DIRECT_SIMPLE + debuff5 | PORTED/PARTIAL | `battle_phase9i_debuff5_success_skill_61` | target speed debuff | crit, dodge, implicit debuff, P12 stat reassert | Medium | Covered for success; block/miss/stat represented by skill32 family smoke. |
| `62` | BYTECODE_DEFAULT_RAW_DAMAGE + DQ_SELF_BUFF buff10 | PORTED/PARTIAL | `battle_phase9r_raw_self_buff_skill_62` | default raw damage plus attacker self-buff | crit, dodge, q() buff10 text | High | Covered by Phase 9-R raw damage + self-buff smoke. |
| `63` | DIRECT_SIMPLE + follow-up | PORTED/PARTIAL | `battle_p7_q_skill63_followup_p2`, `battle_phase9d_miss_followup_skill_63` | target survives, forced follow-up roll | crit, dodge, q() follow-up | Medium | Covered for hit/miss; no-follow-up negative case later. |
| `64` | DQ_BUFF_WITH_SELECTED_INDEX | PORTED/PARTIAL | `battle_phase9w_skill64_selected_buff_copy` | selected target/index parity | P6/P7/q(), buff11 copy | High | Phase 9-W wires `selectedTargetSlot`, smoke-covers selected enemy buff copy, target buff clear, buff11 selected slot, and no-damage P7 path; multi-target slot parity remains pending. |
| `65` | DEFAULT_EFFECTMODE1 buff12 | PORTED/PARTIAL | `battle_phase9ab_skill65_producer_to_followup`, `battle_p7_q_buff12_followup_p2` | same-side no-damage buff route | P13, q() follow-up | Medium | Phase 9-AB smoke-covers skill65 producer -> `K12=1`, P13 active queue -> `K12=2`, then next skill q() follow-up P2 without forced helper. |
| `66` | DIRECT_SIMPLE | PORTED/PARTIAL | `battle_phase9b_direct_skill_66` | basic one-target battle | crit, dodge, jitter | Low | Covered by Phase 9-P forced-hit smoke. |
| `67` | BYTECODE_DEFAULT_RAW_DAMAGE + P7_VISUAL_EFFECT_ONLY | PORTED/PARTIAL | `battle_phase9t_raw_visual_skill_67` | default raw damage; effect id 5 is NOT_REACHED as buff/debuff; effect.mid row 67 visual chunks 26/11 | crit, dodge, P7 visual row | High | Covered by Phase 9-T raw/no-debuff/no-q visual smoke. |
| `68` | DIRECT_SIMPLE + target debuff10 + DQ_SELF_BUFF buff10 | PORTED/PARTIAL | `battle_phase9u_direct_self_buff_skill_68` | direct damage, target debuff10, attacker self-buff10 | crit, dodge, q() buff10 | High | Covered by Phase 9-U direct/debuff/self-buff smoke. |
| `69` | DIRECT_SIMPLE + follow-up | PORTED/PARTIAL | `battle_phase9d_hit_followup_skill_69`, `battle_phase9d_miss_followup_skill_69` | target survives, follow-up roll | crit, dodge, q() follow-up | Medium | Covered for hit/miss; no-follow-up negative case later. |

## Coverage Buckets

| Bucket | Skills | Status | Next useful slice |
| --- | --- | --- | --- |
| Already route-smoked direct/P7 backbone | `10`, plus route battles | PORTED/PARTIAL | Keep as route regression anchor. |
| Already smoke-covered post-skill paths | `11`, `17`, `45`, `52`, `58`, `63`, `65`, `69` | PORTED/PARTIAL | Move to debuff-family coverage by debuff id. |
| Direct-simple no-extra-effect smoke | `0,6,10,16,20,26,30,36,40,46,50,56,60,66` | PORTED/PARTIAL | Covered by Phase 9-P forced-hit smoke; exact RNG stream parity later. |
| Debuff families | `1/2/7/8/12/13/18/19/22/28/31/32/33/37/38/39/41/47/51/54/55/57/61` | PARTIAL | Debuff ids 1,2,3,4,5,6,7,10 are covered by family smoke; skills 54/55 audited as zero-power no-damage guard, so table debuff8/9 are NOT_REACHED from P7. |
| Conditional damage | `3/9/23/29` | PORTED/PARTIAL | Covered by Phase 9-C/AC deterministic target debuff setup. |
| Target buff clear | `43/49` | PORTED/PARTIAL | Covered by Phase 9-N hit/miss/no-buff smoke. |
| HP percent scaling | `53/59` | PORTED/PARTIAL | Covered by Phase 9-O low/high HP, min-clamp, and miss smoke. |
| Source switch gap | `21/27/42/48/62/67` | PORTED/PARTIAL | Phase 9-R smoke-covered raw damage + self-buff for `21/27/42/48/62`; Phase 9-T smoke-covered skill `67` raw damage/no debuff/no q() post-effect. |

## Selected First Phase 9 Slice

The first slice was:

```text
Audit crit + dodge/miss result flow.
```

Reason:

- Crit is already produced by `BattleDamageResult`, but needs coverage as a
  first-class Phase 9 result flag.
- Dodge/miss is source P7 logic, not a skill-family special case.
- Current rebuild applies damage unconditionally, so every broad skill smoke
  would overclaim correctness until miss is represented.
- This slice improves the shared result lifecycle for all damaging skills
  before adding more skill-specific behavior.

Deliverables:

- `157_battle_phase9_crit_dodge_miss_result_flow_audit.md`
- implementation slice in `VqsvBattleRuntime`, `VqsvBattleRenderer`, `VqsvIntroDemo`,
  `VqsvText`, and `VqsvSmokeHarness`.

Current result:

- Crit result flow remains `PORTED/PARTIAL`.
- Dodge/miss P7 consumer is now `PORTED/PARTIAL`.
- Passive hook 4 in miss chance remains `PENDING`.
- Forced hit/miss/crit smoke PNGs pass.

## Phase 9-B Direct Smoke Coverage

Implemented in:

- `rebuild_plan/158_battle_phase9b_direct_smoke_coverage.md`

Covered direct-simple no-extra-effect skills:

```text
0, 6, 10, 16, 20, 26, 30, 36, 40, 46, 50, 56, 60, 66
```

Status:

- generic smoke checkpoint `battle_phase9b_direct_skill_<id>` exists;
- all listed skills pass forced-hit P7 damage smoke;
- route regressions Sophie/Bunny/Elder pass.

Not covered by Phase 9-B:

- direct skills with debuff/heal/leech/follow-up;
- clear-buff and HP-scaling formulas;
- `SOURCE_SWITCH_GAP` skills.

## Phase 9-C Formula Variant Coverage

Implemented in:

- `rebuild_plan/159_battle_phase9c_formula_variant_smoke_coverage.md`

Covered deterministic formula variants:

| Family | Skills | Status |
| --- | --- | --- |
| Plus-divisor | `1,7` | PASS |
| Conditional debuff0 | `3,9` | PASS |
| Conditional debuff1 | `23,29` | PASS |
| Clear target buffs | `43,49` | PASS |
| HP-percent scaling | `53,59` | PASS |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9c/
```

Still not covered:

- broad debuff families by debuff id;
- zero-power debuff ids `54/55`;
- `SOURCE_SWITCH_GAP` skills.

## Phase 9-D Post-Hit Siblings And Miss Interactions

Implemented in:

- `rebuild_plan/160_battle_phase9d_post_hit_followup_miss_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Heal sibling and miss interaction | `11,17` | PASS |
| Source `aa` leech and miss interaction | `52,58` | PASS |
| Follow-up sibling and miss interaction | `63,69` | PASS |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9d/
```

Important result:

- Rebuild no longer gates `52/58` leech behind `p7AttackHit`; source `game.d.q()`
  checks `aa`, and `aa` is set before the miss gate.
- Skill `52` can pass `aa` while visible heal rounds to zero; this is covered
  by trace rather than requiring a `+0` UI.

## Phase 9-E Debuff Id 1 Family Coverage

Implemented in:

- `rebuild_plan/161_battle_phase9e_debuff1_family_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Explicit debuff id 1 success | `2,8,22,28` | PASS |
| Buff14 block | `2` representative | PASS |
| Status3 reduced chance | `2` representative | PASS |
| Miss interaction | `2` representative | PASS: P7 debuff text hidden, source-applied debuff queue can still reach P12. |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9e/
```

## Phase 9-F Debuff Id 2 Family Coverage

Implemented in:

- `rebuild_plan/162_battle_phase9f_debuff2_family_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Implicit debuff id 2 success | `12,18` | PASS |
| Buff14 block | `12` representative | PASS |
| Status3 block | `12` representative | PASS |
| Miss interaction | `12` representative | PASS: P7 debuff text hidden, source-applied debuff queue can still reach P12. |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9f/
```

## Phase 9-G Debuff Id 3 Family Coverage

Implemented in:

- `rebuild_plan/164_battle_phase9g_debuff3_family_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Implicit delayed-damage debuff id 3 success | `13,19` | PASS |
| Buff14 block | `13` representative | PASS |
| Status3 block | `13` representative | PASS |
| Miss interaction | `13` representative | PASS: P7 debuff text hidden, source-applied debuff queue can still reach P12. |
| P12 delayed-damage consumer | `13` representative | PASS via existing `battle_p12_debuff3_*` checkpoints. |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9g/
```

## Phase 9-H Debuff Id 4 Family Coverage

Implemented in:

- `rebuild_plan/165_battle_phase9h_debuff4_family_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Implicit debuff id 4 success | `31,37` | PASS |
| Buff14 block | `31` representative | PASS |
| Status3 block | `31` representative | PASS |
| Miss interaction | `31` representative | PASS: P7 debuff text hidden, source-applied debuff queue can still apply. |
| P7 miss chance consumer | `31` representative | PASS via trace `debuff4Value=1`. |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9h/
```

## Phase 9-I Debuff Id 5 Family Coverage

Implemented in:

- `rebuild_plan/166_battle_phase9i_debuff5_family_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Implicit speed-down debuff id 5 success | `32,38,61` | PASS |
| Buff14 block | `32` representative | PASS |
| Status3 block | `32` representative | PASS |
| Miss interaction | `32` representative | PASS: P7 debuff text hidden, source-applied debuff queue can still apply. |
| P12 stat consumer | `32` representative | PASS: no visual, speed transition trace. |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9i/
```

## Phase 9-J Debuff Id 6 Family Coverage

Implemented in:

- `rebuild_plan/167_battle_phase9j_debuff6_family_coverage.md`

Covered:

| Area | Skills | Status |
| --- | --- | --- |
| Implicit damage-reduction debuff id 6 success | `33,39` | PASS |
| Buff14 block | `33` representative | PASS |
| Status3 block | `33` representative | PASS |
| Miss interaction | `33` representative | PASS: P7 debuff text hidden, source-applied debuff queue can still apply. |
| Damage formula consumer | `33` representative | PASS: preloaded debuff6 reduces outgoing skill10 damage. |

Smoke output folder:

```text
rebuild_game/build/smoke/phase9j/
```

## Phase 9 Closeout

Closed in:

- `rebuild_plan/187_battle_phase9ad_skill_coverage_closeout.md`

Closeout decision:

```text
Phase 9 = CLOSED AS PORTED/PARTIAL + SMOKE-COVERED
```

Phase 9 closeout confirms:

- every `aq.c[1][0..69]` row has a dedicated smoke, family smoke plus sibling
  proof, or source-backed `NOT_REACHED` classification;
- no skill row remains `MISSING`, `AUDITED/PENDING`, or `UNKNOWN`;
- crit/miss/debuff/jitter/follow-up result flow is represented at
  source-shaped smoke level;
- `SOURCE_SWITCH_GAP` formula side is bytecode/control-flow validated and
  smoke-covered;
- route regressions Sophie/Bunny/Elder passed after the final Phase 9-AC
  closeout slice.

Still not claimed:

- exact RNG stream parity;
- pixel-perfect P7 animation/effect parity;
- full HUD status icon/widget parity;
- full battle engine completion outside skill coverage.
