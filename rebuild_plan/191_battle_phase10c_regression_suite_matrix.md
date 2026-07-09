# 191 Battle Phase 10-C Regression Suite Matrix

Status: MATRIX CREATED / QUICK RUNNER IMPLEMENTED.

Purpose:

- Turn the scattered battle smoke checkpoints into a controlled regression
  suite.
- Prevent future work from reopening old bugs in battle UI, P7 effects,
  catch/item/pet, EXP/level-up/evolution, and route integration.
- Keep all verification headless: no client/JAR launch, PNG smoke only.

Scope:

- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java --smoke-checkpoint`
- Existing battle docs `74..190`

Initial matrix creation was doc-only. The follow-up runner slice added
`--smoke-suite battle_quick` and is closed out in
`192_battle_phase10_closeout.md`.

## Rules

1. Do not open the client/JAR for regression.
2. Every checkpoint runs through:

   ```text
   java -cp build/classes VqsvIntroDemo --smoke-checkpoint <checkpoint> <output.png>
   ```

3. Every code slice that touches battle runtime, renderer, battle UI, source
   text, inventory, pet data, or event battle bridge must run at least the
   Quick Gate.
4. Visual/effect work must run Quick Gate plus the matching focused visual pack.
5. Large roadmap phase closeouts must run Quick Gate plus Extended Gate.

## Preflight

Run before any suite:

```text
powershell -ExecutionPolicy Bypass -File ./build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
powershell -NoProfile -Command "$pattern = [string]::Join('|', @([char]0x00C3,[char]0x00C2,[char]0x00C6,[char]0x00D0,[char]0x00F0,[char]0x25A1,[char]0xFFFD,'mojibake')); rg -n $pattern src/main/java"
```

Expected:

- build passes;
- `--check` passes;
- `VqsvBattleDamageFormulaCheck` prints `battle-damage-formula-check-ok`;
- mojibake scan has no Java-source matches.

## Tier 0: Quick Gate

Run after every battle code change.

| Group | Checkpoint | Why |
|---|---|---|
| Route | `route_sophie_after_battle_branch` | Confirms Sophie battle result branches to source target `78`. |
| Route | `route_bunny_after_battle_task` | Confirms Bunny catch route, result `-1`, task state, and return text. |
| Route | `route_elder_after_battle_reward_state` | Confirms elder battle reward and `state[1,0,6]=3`. |
| Battle UI | `battle_elder_command_ui` | Confirms P20 command UI still opens. |
| Skill UI | `battle_elder_p3_skill_list` | Confirms P3 skill list. |
| Target | `battle_elder_p6_target_select` | Confirms P6 target select. |
| P7 | `battle_elder_p7_damage_frame` | Confirms P7 damage frame. |
| Formula | `battle_p7_hit_forced_direct_skill10` | Confirms deterministic hit result. |
| Formula | `battle_p7_miss_forced_skill10` | Confirms miss result flow. |
| Formula | `battle_p7_crit_forced_skill10` | Confirms crit result flow. |
| Status HUD | `battle_phase10a_status_icons_mixed_order` | Confirms Phase 10-A status icons. |
| P7 AH | `battle_phase10b_p7_type7_skill34_overlay` | Confirms normal P7 AH type7. |
| P7 AH | `battle_phase10b_p7_type8_skill12_overlay` | Confirms normal P7 AH type8. |
| P7 AH | `battle_phase10b_p7_type12_skill55_overlay` | Confirms normal P7 AH type12. |

Recommended output folder:

```text
build/smoke/regression_quick/
```

## Tier 1: Battle UI And Command Pack

Run after changes to `game.h`-like UI, command bar, choice UI, petstate UI,
input/click handling, or text rendering.

| Area | Checkpoint | Status |
|---|---|---|
| Battle entry | `battle_entry_enemy_cpos` | PORTED/PARTIAL |
| Battle entry | `battle_entry_player_cpos` | PORTED/PARTIAL |
| Battle entry | `battle_entry_both_landed` | PORTED/PARTIAL |
| Command | `battle_bunny_command_ui` | PORTED/PARTIAL |
| Command | `battle_elder_command_ui` | PORTED/PARTIAL |
| Command nav | `battle_elder_command_ui_right` | PORTED/PARTIAL |
| Command click | `battle_elder_command_ui_click_pet` | PORTED/PARTIAL |
| Skill list | `battle_bunny_p3_skill_list` | PORTED/PARTIAL |
| Skill list | `battle_elder_p3_skill_list` | PORTED/PARTIAL |
| Skill disabled | `battle_skill_no_pp_warning` | PORTED/PARTIAL |
| Target select | `battle_elder_p6_target_select` | PORTED/PARTIAL |
| Target confirm | `battle_elder_p6_confirm_to_p7` | PORTED/PARTIAL |
| HUD | `battle_hud_battle_ui_source_bars` | PORTED/PARTIAL |
| Background | `battle_background_game_d_c_snapshot` | PORTED/PARTIAL |
| Background | `battle_background_room0_village_snapshot` | PORTED/PARTIAL |

## Tier 2: P7 Resolve / Animation / Effect Pack

Run after touching `VqsvBattleRuntime` P7, `VqsvBattleRenderer`, AH effects,
damage text, HP tween, death state, or actor action timing.

| Area | Checkpoint | Why |
|---|---|---|
| P7 start | `battle_elder_p7_anim_start` | P7 entry. |
| Actor effect | `battle_elder_p7_actor_u21_start` | Actor action object. |
| Actor state | `battle_elder_p7_actor_u21_trigger_hit` | Frame trigger to hit state. |
| Recover | `battle_elder_p7_actor_u21_recover` | Recover transition. |
| Damage | `battle_elder_p7_damage_frame` | Damage frame. |
| HP delay | `battle_elder_p7_damage_hp_delay` | HP tween delay. |
| HP tween | `battle_elder_p7_damage_hp_tween_step` | HP tween step. |
| Text lifecycle | `battle_elder_p7_damage_text_lifecycle` | Damage text lifetime. |
| Death | `battle_elder_p7_death_state3_effect_start` | Death AH type16 start. |
| Death -> result | `battle_elder_p7_death_to_p8_after_effect` | Death-to-P8. |
| Post-skill heal | `battle_elder_p7_q_heal_skill11` | `game.d.q()` heal. |
| Post-skill buff | `battle_elder_p7_q_buff_skill45` | `game.d.q()` buff. |
| Buff12 follow-up | `battle_p7_q_buff12_followup_p2` | P2 follow-up. |
| Skill63 follow-up | `battle_p7_q_skill63_followup_p2` | P2 follow-up. |

### Phase 10-B AH Visual Subpack

| AH type | Checkpoint |
|---:|---|
| `1` | `battle_elder_p7_speffect45_type1` |
| `7` | `battle_phase10b_p7_type7_skill34_overlay` |
| `8` | `battle_phase10b_p7_type8_skill12_overlay` |
| `9` | `battle_elder_p7_speffect45_overlay` |
| `12` | `battle_phase10b_p7_type12_skill55_overlay` |

## Tier 3: P12/P13 Active Effect Queue Pack

Run after touching buff/debuff queues, `activeEffectQueue`, status slots,
P12/P13, or active-effect visuals.

| Area | Checkpoint | Why |
|---|---|---|
| Buff queue start | `battle_p13_buff9_queue_start` | Buff id9 queue entry. |
| Buff visual | `battle_p13_buff9_visual_speffect15` | AH visual path. |
| Buff apply | `battle_p13_buff9_after_apply` | Stat/slot apply. |
| P7 -> P13 order | `battle_p7_to_p13_queue_order_skill45` | Source order. |
| Debuff queue start | `battle_p12_debuff0_queue_start` | Debuff id0 queue entry. |
| Debuff damage | `battle_p12_debuff0_damage_text` | HP-delta text. |
| Debuff apply | `battle_p12_debuff0_after_apply` | Tick/apply. |
| P7 -> P12 order | `battle_p7_to_p12_queue_order_debuff0` | Source order. |
| Debuff type12 | `battle_p12_debuff1_type12_special` | AH type12 active queue. |
| Debuff type8 | `battle_p12_debuff2_type8_special` | AH type8 active queue. |
| Debuff3 | `battle_p12_debuff3_queue_start` | Debuff3 queue. |
| Debuff3 actor | `battle_p12_debuff3_type0_actor_mid` | type0 actor visual. |
| Debuff3 apply | `battle_p12_debuff3_after_apply` | Apply. |
| Stat skip visual | `battle_p12_debuff5_stat_skip_visual` | No-visual stat effect. |
| Death to P8 | `battle_p12_queue_death_to_p8` | Queue death branch. |
| Death to P5 | `battle_p13_queue_death_to_p5` | Player forced replacement. |
| Death to P9 | `battle_p13_queue_death_to_p9` | Player lose branch. |
| Death to P15 | `battle_p12_queue_death_to_p15` | Enemy replacement branch. |
| P15 enemy replace | `battle_p15_enemy_replaced` | Enemy party replacement. |

## Tier 4: Catch / Item / Pet Switch Pack

Run after touching P21/P17/P4/P16/P5/P10/P11, inventory, storage, petstate,
choice UI, msgwarm/openbox UI, or command action routing.

### Catch

| Area | Checkpoint |
|---|---|
| P21 list | `battle_bunny_catch_p21` |
| P17 result | `battle_bunny_catch_p17_anim_or_result` |
| Route after catch | `battle_bunny_after_catch_route` |
| Forced fail | `battle_bunny_first_catch_forced_fail` |
| Escape effect | `battle_bunny_first_catch_fail_escape_effect` |
| Counterattack | `battle_bunny_first_fail_enemy_counterattack` |
| Rumble | `battle_bunny_first_catch_q2_rumble` |
| RNG trace | `battle_bunny_pre_p17_rng_trace` |
| Retry item | `battle_bunny_retry_p21_item0` |
| Catch warning | `battle_catch_fail_or_warning` |
| Generic success | `battle_catch_generic_roll_success` |
| Success flash | `battle_catch_success_flash_phase` |
| Q3 flash | `battle_catch_success_q3_flash_mid` |
| Sprite timing | `battle_p17_sprite269_timing_matrix` |
| RNG formula trace | `battle_rng_trace_p17_catch` |
| Status multipliers | `battle_catch_chance_status_multipliers` |
| Missing count | `battle_catch_missing_count_warning` |
| Missing count return | `battle_catch_missing_count_warning_return_p21` |
| Back to command | `battle_catch_p21_back_to_command` |
| SMS-free item | `battle_catch_sms_free_item0_p17` |
| Catch forbidden | `battle_catch_not_allowed_warning` |
| Storage bag | `battle_catch_storage_bag` |
| Storage bank | `battle_catch_storage_bank` |
| Storage full | `battle_catch_storage_full_release` |
| Choice UI | `battle_choice_ui_scroll_source_rows` |
| Warning UI | `battle_msgwarm_source_widget_warning` |
| Openbox UI | `battle_openbox_source_widget_catch_success` |
| P17 order | `battle_p17_q1_h_effect_order` |
| P17 shrink | `battle_p17_ah_type8_q1_capture_shrink` |
| P17 escape | `battle_p17_ah_type8_q4_escape_effect` |
| P17 restore | `battle_p17_q4_fail_restore_enemy` |

### Item

| Area | Checkpoint |
|---|---|
| P4 list | `battle_elder_item_p4` |
| P16 target | `battle_elder_item_target_p16` |
| P16 petstate | `battle_p16_target_petstate_ui` |
| Heal HP | `battle_p16_item_heal_hp` |
| Restore PP | `battle_p16_item_pp_restore` |
| HP+PP | `battle_p16_item_hp_pp` |
| Revive | `battle_p16_item_revive` |
| Clear debuff | `battle_p16_item_clear_debuff` |
| HP full warning | `battle_p16_item_hp_full_warning` |
| PP full warning | `battle_p16_item_pp_full_warning` |
| No debuff warning | `battle_p16_item_no_debuff_warning` |
| Success msgwarm | `battle_p16_item_success_msgwarm` |
| Confirm to P1 | `battle_p16_success_confirm_to_p1` |
| Warning return | `battle_p16_warning_return_petstate_preserve_cursor` |
| Back to P4 | `battle_p16_back_returns_p4` |
| Blocked item | `battle_p4_blocked_item_warning` |

### Pet Switch

| Area | Checkpoint |
|---|---|
| P5 open | `battle_elder_pet_p5` |
| Source rows | `battle_p5_petstate_source_rows` |
| Text start | `battle_p5_petstate_text_start` |
| Text active | `battle_p5_petstate_text_active` |
| Voluntary switch | `battle_p5_voluntary_switch_success` |
| Active pet after switch | `battle_p5_after_switch_active_pet` |
| Click reserve | `battle_p5_click_reserve_success` |
| Transition | `battle_p5_switch_transition` |
| Valid transition | `battle_p5_valid_switch_transition` |
| Transition mid | `battle_p5_switch_transition_mid` |
| Current warning | `battle_p5_current_warning` |
| Dead warning | `battle_p5_dead_warning` |
| Back to command | `battle_p5_back_to_command` |
| Forced menu | `battle_p5_forced_menu_visibility` |
| Forced replacement | `battle_p5_forced_replacement_success` |
| Forced dead warning | `battle_p5_forced_dead_warning` |
| Status11 cleanup | `battle_p5_status11_cleanup` |

### Other Command Shells

| Area | Checkpoint |
|---|---|
| P11 shop | `battle_elder_shop_p11` |
| P10 run warning | `battle_elder_run_warning` |
| Elder result | `battle_elder_result` |

## Tier 5: EXP / Level-Up / Evolution Pack

Run after touching P8/P22/P23, EXP participant vector, level-up UI, learn skill,
world notice bridge, or evolve UI.

| Area | Checkpoint |
|---|---|
| Level-up UI | `battle_exp_levelup_ui` |
| P8 confirm | `battle_exp_p8_confirm_fast_forward` |
| Learn skill UI | `battle_exp_levelup_choiceskill_ui` |
| Learn skill done | `battle_exp_levelup_learn_skill_done` |
| Active-only EXP | `battle_exp_vector_active_only_regression` |
| P5 switch participants | `battle_exp_vector_p5_switch_two_participants` |
| Iterate second pet | `battle_exp_vector_j_iterates_second_pet` |
| Form5 multiplier | `battle_exp_vector_participant_form5_multiplier` |
| Reserve form6 share | `battle_exp_vector_reserve_form6_share` |
| Global state7 share | `battle_exp_vector_global_state7_share` |
| X clears active | `battle_exp_consumer_x_clears_active_marker` |
| X removes dead j | `battle_exp_consumer_x_removes_dead_j` |
| Passive heal | `battle_exp_consumer_x_passive_heal` |
| Evolution queue | `battle_levelup_evolution_queue_created` |
| World notice | `world_evolution_notice_after_levelup` |
| Notice exhausted | `world_evolution_notice_queue_exhausted` |
| Tutorial bridge | `world_evolution_tutorial_petstate_bridge` |
| Evolve UI | `world_evolution_evolve_ui_open` |
| Success mutate | `world_evolution_confirm_success_mutate` |
| Continue after success | `world_evolution_after_success_continue` |
| No material | `world_evolution_confirm_no_material` |
| Level low | `world_evolution_confirm_level_low` |
| No next target | `world_evolution_no_next_target_warning` |
| No next continue | `world_evolution_no_next_target_after_warning_continue` |
| No material continue | `world_evolution_confirm_no_material_after_warning_continue` |
| Level low continue | `world_evolution_confirm_level_low_after_warning_continue` |
| Back from evolve UI | `world_evolution_back_from_evolve_ui` |

## Tier 6: Phase 9 Skill Coverage Pack

Run when touching formula/status/skill behavior. This pack is intentionally
large; do not run it after every visual-only edit unless the edit changes P7
skill flow.

Representative required set:

| Family | Checkpoints |
|---|---|
| Direct | `battle_phase9b_direct_skill_0`, `battle_phase9b_direct_skill_6`, `battle_phase9b_direct_skill_10` |
| Formula variants | `battle_phase9c_plus_divisor_skill_7`, `battle_phase9c_cond_debuff0_skill_3`, `battle_phase9c_cond_debuff1_skill_23`, `battle_phase9c_clear_buff_skill_43`, `battle_phase9c_hp_scaling_skill_53` |
| Post-hit / miss | `battle_phase9d_hit_heal_skill_17`, `battle_phase9d_hit_leech_skill_52`, `battle_phase9d_hit_followup_skill_69`, `battle_phase9d_miss_heal_skill_17`, `battle_phase9d_miss_leech_skill_52`, `battle_phase9d_miss_followup_skill_69` |
| Debuff 1 | `battle_phase9e_debuff1_success_skill_2`, `battle_phase9e_debuff1_buff14_block_skill_2`, `battle_phase9e_debuff1_miss_queue_skill_2` |
| Debuff 2 | `battle_phase9f_debuff2_success_skill_12`, `battle_phase9f_debuff2_buff14_block_skill_12`, `battle_phase9f_debuff2_miss_queue_skill_12` |
| Debuff 3 | `battle_phase9g_debuff3_success_skill_13`, `battle_phase9g_debuff3_buff14_block_skill_13`, `battle_phase9g_debuff3_miss_queue_skill_13` |
| Debuff 4 | `battle_phase9h_debuff4_success_skill_31`, `battle_phase9h_debuff4_miss_chance_skill_31` |
| Debuff 5 | `battle_phase9i_debuff5_success_skill_32`, `battle_phase9i_debuff5_stat_consumer_skill_32` |
| Debuff 6 | `battle_phase9j_debuff6_success_skill_33`, `battle_phase9j_debuff6_damage_reduction_skill_33` |
| Debuff 7 | `battle_phase9k_debuff7_success_skill_51`, `battle_phase9k_debuff7_stat_consumer_skill_51` |
| Debuff 10 | `battle_phase9l_debuff10_success_skill_41`, `battle_phase9l_debuff10_catch_chance_after_skill41` |
| Zero-power | `battle_phase9m_zero_power_success_skill_54`, `battle_phase9m_debuff8_visual_consumer_skill54`, `battle_phase9m_debuff9_visual_consumer_skill55` |
| Buff clear / HP scaling | `battle_phase9n_clear_buff_success_skill_43`, `battle_phase9o_hp_scaling_low_high_skill_53`, `battle_phase9o_hp_scaling_min_clamp_skill_53` |
| Source-switch gap | `battle_phase9r_raw_self_buff_skill_21`, `battle_phase9t_raw_visual_skill_67`, `battle_phase9u_direct_self_buff_skill_68`, `battle_phase9w_skill64_selected_buff_copy` |
| No-damage buffs | `battle_phase9y_no_damage_buff_skill_4`, `battle_phase9z_cleanse_protect_skill_24`, `battle_phase9aa_defensive_hook_skill_34`, `battle_phase9ab_skill65_producer_to_followup` |

Full exhaustive Phase 9 remains defined by `156_battle_phase9_skill_coverage_matrix.md`
and follow-up docs `157..187`.

## Tier 7: World/Event Integration Pack

Run when touching battle descriptors, event bridge, op37/op32/op47, source state,
or world resume wrappers.

| Area | Checkpoint |
|---|---|
| Sophie route | `route_sophie_after_battle_branch` |
| Bunny route | `route_bunny_after_battle_task` |
| Elder route | `route_elder_after_battle_reward_state` |
| Sophie loss persistence | `battle_sophie_loss_persists_dien_mieu_ko` |
| Refresh restore | `op39_refresh_restores_pet_hp_pp` |
| Bunny caught low HP | `battle_bunny_caught_pet_low_hp_state` |
| Bunny low HP P5 | `battle_bunny_caught_pet_p5_low_hp` |
| Save prompt | `room1_bunny_save_prompt` |
| Save success | `room1_bunny_save_success` |
| Save resume | `room1_bunny_save_resume_state` |
| World petstate party | `world_petstate_ui_source_party` |
| World petstate Bunny | `world_petstate_ui_bunny_selected` |
| Title continue | `boot_title_continue_with_save` |

## Suite Selection Policy

| Change type | Required suite |
|---|---|
| Any battle code change | Preflight + Tier 0 |
| Battle renderer / P7 visual | Preflight + Tier 0 + Tier 2 |
| Status HUD / icons | Preflight + Tier 0 + Phase 10-A status icon checkpoints |
| Command/choice/petstate/msgwarm/openbox UI | Preflight + Tier 0 + Tier 1 + relevant Tier 4 UI checkpoints |
| Catch/item/pet switch | Preflight + Tier 0 + Tier 4 |
| EXP/level-up/evolution | Preflight + Tier 0 + Tier 5 |
| Formula/status/skill behavior | Preflight + Tier 0 + Tier 3 + Tier 6 representative set |
| Event bridge / battle route | Preflight + Tier 0 + Tier 7 |
| Phase closeout | Preflight + Tier 0 + affected tiers + route regressions |

## Runner Gap

Current state:

- `VqsvIntroDemo --smoke-checkpoint` can run one checkpoint at a time.
- There is no committed batch runner that accepts a suite name such as
  `battle_quick` or `battle_phase10_visual`.

Recommended next code slice:

```text
Add a headless smoke suite runner, without changing gameplay:

VqsvIntroDemo --smoke-suite battle_quick build/smoke/suites/battle_quick

The runner should:
- build output PNG path as <outputDir>/<checkpoint>.png;
- stop on first failure;
- print each checkpoint result;
- define suites from this matrix;
- never open client/JAR.
```

## Done Criteria For Phase 10

Phase 10 can be called CLOSED / PARTIAL when:

1. This matrix exists.
2. A smoke-suite runner exists for at least Quick Gate.
3. Quick Gate passes from a clean build.
4. A Phase 10 closeout doc records:
   - exact command used;
   - checkpoint list;
   - output folder;
   - remaining gaps.

Current status after runner slice:

```text
Phase 10-C matrix: DONE.
Smoke-suite runner: DONE for battle_quick.
Quick Gate full run through suite command: DONE.
Phase 10 closeout: DONE in 192_battle_phase10_closeout.md.
```

## Next Step

Implement the small headless suite runner for Tier 0 Quick Gate only.

Do not add broad exhaustive runners yet. Start with Quick Gate so future slices
have a reliable, short regression command.
