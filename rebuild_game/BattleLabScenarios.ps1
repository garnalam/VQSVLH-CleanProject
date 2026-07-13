function Get-VqsvBattleLabScenarios {
    $npc = [ordered]@{
        "vs_entry" = "battle_entry_vs_elder_ui"
        "entry_enemy_throw" = "battle_entry_enemy_cpos"
        "entry_player_throw" = "battle_entry_player_cpos"
        "entry_both_landed" = "battle_entry_both_landed"
        "entry_power_percent" = "battle_entry_power_percent_ui"
        "command" = "battle_elder_command_ui"
        "command_right" = "battle_elder_command_ui_right"
        "skill_list" = "battle_elder_p3_skill_list"
        "target_select" = "battle_elder_p6_target_select"
        "p7_damage_frame" = "battle_elder_p7_damage_frame"
        "p7_death_to_exp" = "battle_elder_p7_death_to_p8_after_effect"
        "status_icons" = "battle_phase10a_status_icons_mixed_order"
        "skill64_copy_buff" = "battle_phase9w_skill64_selected_buff_copy"
        "skill68_self_buff" = "battle_phase9u_direct_self_buff_skill_68"
        "skill54_zero_power" = "battle_phase9m_zero_power_success_skill_54"
        "skill65_followup" = "battle_phase9ab_skill65_producer_to_followup"
        "p7_type7_overlay" = "battle_phase10b_p7_type7_skill34_overlay"
        "p7_type8_overlay" = "battle_phase10b_p7_type8_skill12_overlay"
        "p7_type12_overlay" = "battle_phase10b_p7_type12_skill55_overlay"
        "p5_open" = "battle_p5_petstate_source_rows"
        "p5_current_warning" = "battle_p5_current_warning"
        "p5_dead_warning" = "battle_p5_dead_warning"
        "p5_forced_switch" = "battle_elder_switched_bunny_ko_forced_p5_no_exp"
        "all_pets_ko" = "battle_elder_all_player_pets_ko_p9_no_exp"
        "p16_target" = "battle_p16_target_petstate_ui"
        "p16_heal_hp" = "battle_p16_item_heal_hp"
        "p16_pp_restore" = "battle_p16_item_pp_restore"
        "p16_hp_pp" = "battle_p16_item_hp_pp"
        "p16_revive" = "battle_p16_item_revive"
        "p16_clear_debuff" = "battle_p16_item_clear_debuff"
        "shop_rows" = "battle_p11_shop_full_source_item_rows"
        "shop_hover" = "battle_p11_shop_hover_preview_no_confirm"
        "shop_mouse_wheel" = "battle_p11_shop_mouse_wheel_no_confirm"
        "shop_msgyn" = "battle_p11_shop_msgyn_open"
        "shop_buy_qty2" = "battle_p11_shop_buy_qty2_money"
        "shop_success_click_no_rebuy" = "battle_p11_shop_success_click_no_rebuy"
        "shop_buy_badge" = "battle_p11_shop_buy_badge_item3"
        "exp_frame0" = "battle_exp_p8_frame0"
        "exp_frame1" = "battle_exp_p8_frame1"
        "exp_mid" = "battle_exp_p8_mid"
        "exp_target_hold" = "battle_exp_p8_target_hold"
        "exp_normal_gain" = "battle_exp_normal_gain_no_levelup_anim"
        "exp_levelup" = "battle_exp_levelup_ui"
        "exp_learn_skill" = "battle_exp_levelup_learn_skill_done"
        "lose_revive_prompt" = "battle_p24_revive_prompt"
        "lose_revive_pay" = "battle_p24_revive_pay_full_restore"
        "lose_insufficient_money" = "battle_p24_insufficient_money_warning"
    }

    $catch = [ordered]@{
        "entry_no_npc_ui" = "battle_entry_vs_bunny_no_npc_ui"
        "command" = "battle_bunny_command_ui"
        "owned_marker" = "battle_bunny_owned_marker"
        "skill_list" = "battle_bunny_p3_skill_list"
        "weak_prompt" = "battle_bunny_weak_prompt_tasktip"
        "p21_list" = "battle_bunny_catch_p21"
        "manual_p21_idle_guard" = "battle_lab_manual_bunny_p21_idle_guard"
        "mouse_wheel_p21" = "battle_mouse_wheel_p21_list"
        "first_forced_fail" = "battle_bunny_first_catch_forced_fail"
        "first_fail_escape" = "battle_bunny_first_catch_fail_escape_effect"
        "first_fail_counterattack" = "battle_bunny_first_fail_enemy_counterattack"
        "first_fail_rumble" = "battle_bunny_first_catch_q2_rumble"
        "retry_p21_item0" = "battle_bunny_retry_p21_item0"
        "retry_prompt" = "battle_bunny_retry_prompt_tasktip"
        "p17_anim_or_result" = "battle_bunny_catch_p17_anim_or_result"
        "generic_fail" = "battle_catch_fail_or_warning"
        "generic_success" = "battle_catch_generic_roll_success"
        "success_flash" = "battle_catch_success_flash_phase"
        "success_flash_mid" = "battle_catch_success_q3_flash_mid"
        "success_openbox" = "battle_bunny_catch_success_openbox_visual"
        "source_openbox" = "battle_openbox_source_widget_catch_success"
        "missing_count" = "battle_catch_missing_count_warning"
        "missing_count_return" = "battle_catch_missing_count_warning_return_p21"
        "p21_back_to_command" = "battle_catch_p21_back_to_command"
        "not_allowed_warning" = "battle_catch_not_allowed_warning"
        "storage_bag" = "battle_catch_storage_bag"
        "storage_bank" = "battle_catch_storage_bank"
        "storage_full_release" = "battle_catch_storage_full_release"
        "caught_low_hp_state" = "battle_bunny_caught_pet_low_hp_state"
        "caught_p5_low_hp" = "battle_bunny_caught_pet_p5_low_hp"
        "world_petstate_party" = "world_petstate_ui_source_party"
        "world_petstate_bunny" = "world_petstate_ui_bunny_selected"
        "route_after_catch" = "battle_bunny_after_catch_route"
        "rng_trace_p17" = "battle_rng_trace_p17_catch"
        "chance_status_multipliers" = "battle_catch_chance_status_multipliers"
        "q1_capture_shrink" = "battle_p17_ah_type8_q1_capture_shrink"
        "q4_escape_effect" = "battle_p17_ah_type8_q4_escape_effect"
        "q4_restore_enemy" = "battle_p17_q4_fail_restore_enemy"
    }

    return [ordered]@{
        "npc" = $npc
        "catch" = $catch
    }
}

function Show-VqsvBattleLabScenarios {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Scenarios
    )

    foreach ($lane in $Scenarios.Keys) {
        Write-Host "[$lane]"
        foreach ($entry in $Scenarios[$lane].GetEnumerator()) {
            Write-Host ("  {0,-28} -> {1}" -f $entry.Key, $entry.Value)
        }
    }
}

function Get-VqsvBattleLabSuites {
    $npc = [ordered]@{
        "core" = @(
            "vs_entry",
            "entry_power_percent",
            "command",
            "skill_list",
            "target_select",
            "p7_damage_frame"
        )
        "petstate" = @(
            "p5_open",
            "p5_current_warning",
            "p5_dead_warning",
            "p5_forced_switch",
            "all_pets_ko"
        )
        "items_shop_exp" = @(
            "p16_target",
            "p16_heal_hp",
            "p16_pp_restore",
            "p16_revive",
            "p16_clear_debuff",
            "shop_rows",
            "shop_hover",
            "shop_mouse_wheel",
            "shop_msgyn",
            "shop_buy_qty2",
            "shop_success_click_no_rebuy",
            "shop_buy_badge",
            "exp_normal_gain",
            "exp_levelup",
            "exp_learn_skill"
        )
        "exp_animation" = @(
            "exp_frame0",
            "exp_frame1",
            "exp_mid",
            "exp_target_hold",
            "exp_levelup",
            "exp_learn_skill"
        )
        "loss" = @(
            "all_pets_ko",
            "lose_revive_prompt",
            "lose_revive_pay",
            "lose_insufficient_money"
        )
    }
    $npc["all"] = @($npc["core"] + $npc["petstate"] + $npc["items_shop_exp"] + $npc["exp_animation"] + $npc["loss"] | Select-Object -Unique)

    $catch = [ordered]@{
        "core" = @(
            "entry_no_npc_ui",
            "command",
            "weak_prompt",
            "p21_list",
            "manual_p21_idle_guard",
            "mouse_wheel_p21",
            "p17_anim_or_result"
        )
        "tutorial" = @(
            "weak_prompt",
            "first_forced_fail",
            "first_fail_escape",
            "first_fail_counterattack",
            "first_fail_rumble",
            "retry_p21_item0",
            "retry_prompt"
        )
        "capture_visual" = @(
            "p17_anim_or_result",
            "success_flash",
            "success_flash_mid",
            "success_openbox",
            "q1_capture_shrink",
            "q4_escape_effect",
            "q4_restore_enemy"
        )
        "inventory_storage" = @(
            "p21_list",
            "missing_count",
            "missing_count_return",
            "p21_back_to_command",
            "not_allowed_warning",
            "storage_bag",
            "storage_bank",
            "storage_full_release",
            "caught_low_hp_state",
            "caught_p5_low_hp"
        )
        "world_petstate" = @(
            "world_petstate_party",
            "world_petstate_bunny"
        )
    }
    $catch["all"] = @($catch["core"] + $catch["tutorial"] + $catch["capture_visual"] + $catch["inventory_storage"] + $catch["world_petstate"] | Select-Object -Unique)

    return [ordered]@{
        "npc" = $npc
        "catch" = $catch
    }
}

function Show-VqsvBattleLabSuites {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Suites
    )

    foreach ($lane in $Suites.Keys) {
        Write-Host "[$lane suites]"
        foreach ($entry in $Suites[$lane].GetEnumerator()) {
            Write-Host ("  {0,-18} -> {1}" -f $entry.Key, ($entry.Value -join ", "))
        }
    }
}
