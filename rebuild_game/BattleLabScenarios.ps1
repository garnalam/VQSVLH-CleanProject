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
        "fire_skill0_hoa_trao" = "battle_lab_fire_skill0"
        "fire_skill1_duong_viem" = "battle_lab_fire_skill1"
        "fire_skill2_diem_kich" = "battle_lab_fire_skill2"
        "fire_skill3_hoa_van_trieu" = "battle_lab_fire_skill3"
        "fire_skill4_thien_hoa_te" = "battle_lab_fire_skill4"
        "fire_skill5_viem_loi_pha" = "battle_lab_fire_skill5"
        "fire_skill6_hoa_diem_dao" = "battle_lab_fire_skill6"
        "fire_skill7_chuoc_nhiet_chi_xuc" = "battle_lab_fire_skill7"
        "fire_skill8_liet_diem_phong_bao" = "battle_lab_fire_skill8"
        "fire_skill9_vinh_hang_hoa_anh" = "battle_lab_fire_skill9"
        "wood_skill10_diep_toan" = "battle_lab_wood_skill10"
        "wood_skill11_quang_phan" = "battle_lab_wood_skill11"
        "wood_skill12_dang_phuoc" = "battle_lab_wood_skill12"
        "wood_skill13_thao_chung" = "battle_lab_wood_skill13"
        "wood_skill14_dang_chi_bich_luy" = "battle_lab_wood_skill14"
        "wood_skill15_thao_nguyen_thuat" = "battle_lab_wood_skill15"
        "wood_skill16_cham_diep_tram" = "battle_lab_wood_skill16"
        "wood_skill17_diep_chi_an_hue" = "battle_lab_wood_skill17"
        "wood_skill18_dang_man_trien_nhieu" = "battle_lab_wood_skill18"
        "wood_skill19_quang_hop_hieu_ung" = "battle_lab_wood_skill19"
        "earth_skill20_hat_bui" = "battle_lab_earth_skill20"
        "earth_skill21_tho_thuan" = "battle_lab_earth_skill21"
        "earth_skill22_bao_cat" = "battle_lab_earth_skill22"
        "earth_skill23_nham_bang" = "battle_lab_earth_skill23"
        "earth_skill24_nguoi_bao_ve_dia_gioi" = "battle_lab_earth_skill24"
        "earth_skill25_thach_phu_thuat" = "battle_lab_earth_skill25"
        "earth_skill26_nham_bao" = "battle_lab_earth_skill26"
        "earth_skill27_hang_rao_cat_da" = "battle_lab_earth_skill27"
        "earth_skill28_bao_cat" = "battle_lab_earth_skill28"
        "earth_skill29_tho_chi_loan_vu" = "battle_lab_earth_skill29"
        "water_skill30_bong_bang" = "battle_lab_water_skill30"
        "water_skill31_bang_lao" = "battle_lab_water_skill31"
        "water_skill32_tuyet_anh" = "battle_lab_water_skill32"
        "water_skill33_thuy_tri" = "battle_lab_water_skill33"
        "water_skill34_thuat_cau_nguyen" = "battle_lab_water_skill34"
        "water_skill35_thuy_bich" = "battle_lab_water_skill35"
        "water_skill36_bao_phong_tuyet" = "battle_lab_water_skill36"
        "water_skill37_la_chan_gia_tuyet" = "battle_lab_water_skill37"
        "water_skill38_bang_phong_ham_tinh" = "battle_lab_water_skill38"
        "water_skill39_ray_lanh" = "battle_lab_water_skill39"
        "electric_skill40_dien_giat" = "battle_lab_electric_skill40"
        "electric_skill41_loi_thiem" = "battle_lab_electric_skill41"
        "electric_skill42_nap_dien" = "battle_lab_electric_skill42"
        "electric_skill43_song_dien_tu" = "battle_lab_electric_skill43"
        "electric_skill44_doat_menh_cao_ap" = "battle_lab_electric_skill44"
        "electric_skill45_dien_nang_chuyen_doi" = "battle_lab_electric_skill45"
        "electric_skill46_tia_lua_dien" = "battle_lab_electric_skill46"
        "electric_skill47_cham_sam_sat" = "battle_lab_electric_skill47"
        "electric_skill48_dien_quang_thach_hoa" = "battle_lab_electric_skill48"
        "electric_skill49_cam_ung_dien_tu" = "battle_lab_electric_skill49"
        "shadow_skill50_anh_thu" = "battle_lab_shadow_skill50"
        "shadow_skill51_chu_oan" = "battle_lab_shadow_skill51"
        "shadow_skill52_quy_doc" = "battle_lab_shadow_skill52"
        "shadow_skill53_con_ac_mong" = "battle_lab_shadow_skill53"
        "shadow_skill54_mi_anh" = "battle_lab_shadow_skill54"
        "shadow_skill55_hon_loan" = "battle_lab_shadow_skill55"
        "shadow_skill56_doc_anh_thu" = "battle_lab_shadow_skill56"
        "shadow_skill57_chu_phuoc_quy_lao" = "battle_lab_shadow_skill57"
        "shadow_skill58_quy_doc_tin_nguong" = "battle_lab_shadow_skill58"
        "shadow_skill59_loi_nguyen_cuoi_cung" = "battle_lab_shadow_skill59"
        "wind_skill60_phong_nhan" = "battle_lab_wind_skill60"
        "wind_skill61_phong_ap" = "battle_lab_wind_skill61"
        "wind_skill62_thuan_phong" = "battle_lab_wind_skill62"
        "wind_skill63_long_quyen" = "battle_lab_wind_skill63"
        "wind_skill64_nghich_phong_doat" = "battle_lab_wind_skill64"
        "wind_skill65_vo_liet_thuat" = "battle_lab_wind_skill65"
        "wind_skill66_yen_hoi_thiem" = "battle_lab_wind_skill66"
        "wind_skill67_phong_chi_tuyen_qua" = "battle_lab_wind_skill67"
        "wind_skill68_phong_chi_tu_hau" = "battle_lab_wind_skill68"
        "wind_skill69_phi_yen_hoan_sao" = "battle_lab_wind_skill69"
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
        "throw_hits_enemy" = "battle_p17_throw_hits_enemy_anchor"
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
        "fire_skills_0_9" = @(
            "fire_skill0_hoa_trao",
            "fire_skill1_duong_viem",
            "fire_skill2_diem_kich",
            "fire_skill3_hoa_van_trieu",
            "fire_skill4_thien_hoa_te",
            "fire_skill5_viem_loi_pha",
            "fire_skill6_hoa_diem_dao",
            "fire_skill7_chuoc_nhiet_chi_xuc",
            "fire_skill8_liet_diem_phong_bao",
            "fire_skill9_vinh_hang_hoa_anh"
        )
        "wood_skills_10_19" = @(
            "wood_skill10_diep_toan",
            "wood_skill11_quang_phan",
            "wood_skill12_dang_phuoc",
            "wood_skill13_thao_chung",
            "wood_skill14_dang_chi_bich_luy",
            "wood_skill15_thao_nguyen_thuat",
            "wood_skill16_cham_diep_tram",
            "wood_skill17_diep_chi_an_hue",
            "wood_skill18_dang_man_trien_nhieu",
            "wood_skill19_quang_hop_hieu_ung"
        )
        "earth_skills_20_29" = @(
            "earth_skill20_hat_bui",
            "earth_skill21_tho_thuan",
            "earth_skill22_bao_cat",
            "earth_skill23_nham_bang",
            "earth_skill24_nguoi_bao_ve_dia_gioi",
            "earth_skill25_thach_phu_thuat",
            "earth_skill26_nham_bao",
            "earth_skill27_hang_rao_cat_da",
            "earth_skill28_bao_cat",
            "earth_skill29_tho_chi_loan_vu"
        )
        "water_skills_30_39" = @(
            "water_skill30_bong_bang",
            "water_skill31_bang_lao",
            "water_skill32_tuyet_anh",
            "water_skill33_thuy_tri",
            "water_skill34_thuat_cau_nguyen",
            "water_skill35_thuy_bich",
            "water_skill36_bao_phong_tuyet",
            "water_skill37_la_chan_gia_tuyet",
            "water_skill38_bang_phong_ham_tinh",
            "water_skill39_ray_lanh"
        )
        "electric_skills_40_49" = @(
            "electric_skill40_dien_giat",
            "electric_skill41_loi_thiem",
            "electric_skill42_nap_dien",
            "electric_skill43_song_dien_tu",
            "electric_skill44_doat_menh_cao_ap",
            "electric_skill45_dien_nang_chuyen_doi",
            "electric_skill46_tia_lua_dien",
            "electric_skill47_cham_sam_sat",
            "electric_skill48_dien_quang_thach_hoa",
            "electric_skill49_cam_ung_dien_tu"
        )
        "shadow_skills_50_59" = @(
            "shadow_skill50_anh_thu",
            "shadow_skill51_chu_oan",
            "shadow_skill52_quy_doc",
            "shadow_skill53_con_ac_mong",
            "shadow_skill54_mi_anh",
            "shadow_skill55_hon_loan",
            "shadow_skill56_doc_anh_thu",
            "shadow_skill57_chu_phuoc_quy_lao",
            "shadow_skill58_quy_doc_tin_nguong",
            "shadow_skill59_loi_nguyen_cuoi_cung"
        )
        "wind_skills_60_69" = @(
            "wind_skill60_phong_nhan",
            "wind_skill61_phong_ap",
            "wind_skill62_thuan_phong",
            "wind_skill63_long_quyen",
            "wind_skill64_nghich_phong_doat",
            "wind_skill65_vo_liet_thuat",
            "wind_skill66_yen_hoi_thiem",
            "wind_skill67_phong_chi_tuyen_qua",
            "wind_skill68_phong_chi_tu_hau",
            "wind_skill69_phi_yen_hoan_sao"
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
    $npc["all"] = @($npc["core"] + $npc["fire_skills_0_9"] + $npc["wood_skills_10_19"] + $npc["earth_skills_20_29"] + $npc["water_skills_30_39"] + $npc["electric_skills_40_49"] + $npc["shadow_skills_50_59"] + $npc["wind_skills_60_69"] + $npc["petstate"] + $npc["items_shop_exp"] + $npc["exp_animation"] + $npc["loss"] | Select-Object -Unique)

    $catch = [ordered]@{
        "core" = @(
            "entry_no_npc_ui",
            "command",
            "weak_prompt",
            "p21_list",
            "manual_p21_idle_guard",
            "mouse_wheel_p21",
            "throw_hits_enemy",
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
            "throw_hits_enemy",
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
