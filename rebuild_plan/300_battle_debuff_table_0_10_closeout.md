# 300 - Battle Debuff Table aq.c[7] Rows 0..10 Closeout

Date: 2026-07-14

Status: TABLE CLOSEOUT / PORTED-PARTIAL WHERE NOTED / SMOKE-COVERED.

Scope: target-side temporary debuffs from source table `aq.c[7]`, stored in
`game.b.w[id][0..4]`.

This document closes the table-level debuff pass after the dedicated closeouts:

- `282_battle_debuff0_gieo_hat_closeout.md`
- `283_battle_debuff1_me_muoi_closeout.md`
- `284_battle_debuff2_quan_quanh_closeout.md`
- `286_battle_debuff3_thuc_loai_closeout.md`
- `288_battle_debuff4_muc_closeout.md`
- `290_battle_debuff5_cham_chap_closeout.md`
- `292_battle_debuff6_nhut_chi_closeout.md`
- `294_battle_debuff7_phong_ngu_closeout.md`
- `296_battle_debuff8_quy_mi_closeout.md`
- `297_battle_debuff9_hon_loan_closeout.md`
- `299_battle_debuff10_te_liet_closeout.md`

## Table Rule

Source storage:

```text
w[id][0] = remaining duration
w[id][1] = primary stored value
w[id][2] = secondary value when source uses one
w[id][3] = source skill id
w[id][4] = active flag
```

HUD rule:

```text
debuff icon cell = id + 1
duration cell = 134 + w[id][0]
```

P12/P13 body visual gate from `game.d.ai[1]`:

```text
body visual exists for debuff ids: 0, 1, 2, 3, 8, 9, 10
no body visual for debuff ids: 4, 5, 6, 7
```

## Closeout Matrix

| Id | Name | Producers | Core logic | Tick/expiry | Visual/icon | Status | Main smoke anchors |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 0 | Gieo Hat | `1`, `7` | Stores pre-skill raw damage. Tick damage is `raw / skill[8]`: skill1 divisor `4`, skill7 divisor `3`. Enables conditional skills `3/9`. | Each active tick damages, duration decrements, then clears. | Icon `1`; P12/P13 body visual exists, speffect18 anchor. | PORTED | `battle_status_debuff0_skill1_producer_apply`, `battle_status_debuff0_skill7_producer_apply`, `battle_status_debuff0_tick_skill1_div4`, `battle_status_debuff0_tick_skill7_div3`, `battle_status_debuff0_body_visual_speffect18_anchor`, `battle_status_debuff0_expiry_clears_icon`, `battle_status_debuff0_buff14_blocks_skill1` |
| 1 | Me Muoi | `2`, `8`, `22`, `28` | Flag status. Conditional skills `23/29` use stronger branch when target has debuff1. Catch multiplier index `1 = 11/10`. | No HP/stat tick; duration decrements and clears. | Icon `2`; P12/P13 body visual exists, speffect14/AH type12. | PORTED | `battle_status_debuff1_skill2_producer_apply`, `battle_status_debuff1_skill8_producer_apply`, `battle_status_debuff1_skill22_producer_apply`, `battle_status_debuff1_skill28_producer_apply`, `battle_status_debuff1_body_visual_speffect14_type12`, `battle_status_debuff1_tick_noop_duration`, `battle_status_debuff1_expiry_clears_icon`, `battle_status_debuff1_catch_multiplier`, `battle_status_debuff1_conditional_skill23_damage`, `battle_status_debuff1_conditional_skill29_damage`, `battle_status_debuff1_buff14_blocks_skill2` |
| 2 | Quan Quanh | `12`, `18` | Bind/command-lock status. Blocks item/pet/run, but skill/catch/shop remain allowed. Catch multiplier index `2 = 12/10`. Target defense formula uses source held/passive table param `15%`. | No HP/stat tick; duration decrements and clears. | Icon `3`; P12/P13 body visual exists, speffect6/AH type8. | PORTED | `battle_status_debuff2_skill12_producer_apply`, `battle_status_debuff2_skill18_producer_apply`, `battle_status_debuff2_body_visual_speffect6_type8`, `battle_status_debuff2_tick_noop_duration`, `battle_status_debuff2_expiry_clears_icon`, `battle_status_debuff2_catch_multiplier`, `battle_status_debuff2_defense_formula_reduces_damage`, `battle_status_debuff2_command_locks_item_pet_run`, `battle_status_debuff2_allows_skill_catch_shop`, `battle_status_debuff2_buff14_blocks_skill12` |
| 3 | Thuc Loai | `13`, `19` | Delayed damage. Stores pre-skill raw damage; final tick applies `raw * skill[8] / 100`. Skill13 uses `150%`; skill19 uses `200%`. | Tick1/tick2 no damage; final tick damages and can KO, then clears. | Icon `4`; P12/P13 body visual exists through actor effect `21`. | PORTED | `battle_status_debuff3_skill13_producer_apply`, `battle_status_debuff3_skill19_producer_apply`, `battle_status_debuff3_body_visual_actor21`, `battle_status_debuff3_tick1_no_damage_duration2`, `battle_status_debuff3_tick2_no_damage_duration1`, `battle_status_debuff3_final_tick_damage_skill13`, `battle_status_debuff3_final_tick_damage_skill19`, `battle_status_debuff3_final_tick_ko_transition`, `battle_status_debuff3_buff14_blocks_skill13` |
| 4 | Muc | `31`, `37` | Stores `skill[8]`: skill31 stores `1`, skill37 stores `2`. P7 miss/evasion path subtracts this from affected unit speed when it attacks. | No HP/stat tick; duration decrements and clears. | Icon `5`; source has a bufDebuf row, but `game.d.ai[1]` excludes id4, so no body visual. | PORTED | `battle_status_debuff4_skill31_producer_apply`, `battle_status_debuff4_skill37_producer_apply`, `battle_status_debuff4_p12_no_body_visual_skip`, `battle_status_debuff4_tick_noop_duration2`, `battle_status_debuff4_expiry_clears_icon`, `battle_status_debuff4_miss_chance_value1`, `battle_status_debuff4_miss_chance_value2`, `battle_status_debuff4_buff14_blocks_skill31` |
| 5 | Cham Chap | `32`, `38`, `61`; skill67 is NOT_REACHED for debuff5 | Speed-down. Stores `baseSpeed * skill[8] / 100`: skills32/38 `10%`, skill61 `5%`. Skill67 table points to id5 but source bytecode routes it to raw visual/no-debuff path. | Active queue reasserts speed down; expiry restores base speed and clears icon. | Icon `6`; bufDebuf row exists, but `game.d.ai[1]` excludes id5, so no body visual. | PORTED | `battle_status_debuff5_skill32_producer_speed10`, `battle_status_debuff5_skill38_producer_speed10`, `battle_status_debuff5_skill61_producer_speed5`, `battle_status_debuff5_skill67_raw_no_debuff`, `battle_status_debuff5_p12_no_body_visual_reassert_speed`, `battle_status_debuff5_expiry_restores_speed`, `battle_status_debuff5_miss_chance_attacker_speed_down`, `battle_status_debuff5_buff14_blocks_skill32` |
| 6 | Nhut Chi | `33`, `39` | Outgoing damage down. Stores `skill[8] = 10`; affected unit outgoing damage becomes `damage - damage * 10 / 100`. Source-immediate mutation still commits on P7 miss, but HP/text are gated by hit. | No HP/stat tick; duration decrements and clears. | Icon `7`; bufDebuf row exists, but `game.d.ai[1]` excludes id6, so no body visual. | PORTED | `battle_status_debuff6_skill33_producer_damage_down10`, `battle_status_debuff6_skill39_producer_damage_down10`, `battle_status_debuff6_miss_queue_no_text`, `battle_status_debuff6_p12_no_body_visual_noop`, `battle_status_debuff6_expiry_clears_icon`, `battle_status_debuff6_outgoing_damage_down`, `battle_status_debuff6_buff14_blocks_skill33` |
| 7 | Phong Ngu | `51`, `57` | Defense down. Stores `baseDefense * 20 / 100`; incoming damage rises because target current defense is lower. Source-immediate mutation still commits on P7 miss. | Active queue reasserts defense down; expiry restores base defense and clears icon. | Icon `8`; no body visual. | PORTED | `battle_status_debuff7_skill51_producer_defense_down20`, `battle_status_debuff7_skill57_producer_defense_down20`, `battle_status_debuff7_miss_queue_no_text`, `battle_status_debuff7_p12_no_body_visual_reassert_defense`, `battle_status_debuff7_expiry_restores_defense`, `battle_status_debuff7_incoming_damage_up`, `battle_status_debuff7_buff14_blocks_skill51` |
| 8 | Quy Mi | `54` now applies debuff8 through no-damage post-effect producer | Source zero-power route was previously `NOT_REACHED`; this is now user-approved gameplay fix. Skill54 uses source chance `40`, is blocked by buff14, stores value `10`, and active debuff8 gives outgoing damage `+10%`; in 1v1 it rolls `55%` self-hit / `45%` opponent-hit. | No HP/stat tick; duration decrements and clears. | Icon `9`; P12/P13 body visual exists with row `[1,0,0,-1,0,25,0,-1]`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | `battle_status_debuff8_skill54_producer_apply`, `battle_status_debuff8_skill54_buff14_blocks`, `battle_status_debuff8_gameplay_fixed_self_hit_damage_up`, `battle_status_debuff8_gameplay_fixed_enemy_hit_damage_up`, `battle_status_debuff8_p12_body_visual_type1_actor25`, `battle_status_debuff8_expiry_clears_icon` |
| 9 | Hon Loan | `55` now applies debuff9 through no-damage post-effect producer | Source zero-power route was previously `NOT_REACHED`; this is now user-approved gameplay fix. Skill55 applies debuff9, then affected unit's next attack uses source-style target vector rebuild through `game.d.f(attacker)` and chooses by RNG. User confirmed P5 pet switch remains allowed; only debuff2 blocks pet switching. | No HP/stat tick; duration `1` then clears. | Icon `10`; P12/P13 body visual exists, speffect12/AH type12. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | `battle_status_debuff9_skill55_producer_apply`, `battle_status_debuff9_random_target_seeded_active`, `battle_status_debuff9_p12_body_visual_type12`, `battle_status_debuff9_expiry_clears_icon` |
| 10 | Te Liet | `41`, `47`; skill68 also combines direct damage + target debuff10 + self buff10 in older Phase 9-U | Explicit chance family, chance param `10`. Catch chance uses status index `3 = 12/10`. No proven source action-delay consumer for `w[10]`; `game.d h.f((byte)10)` is held/passive item id10, not debuff10. | No HP/stat tick; duration `4 -> 0` then clears. | Icon `11`; P12/P13 body visual row `[1,19,0,-1,1,6,0,-1]`, first visible type9. | PORTED/PARTIAL | `battle_status_debuff10_skill41_producer_apply`, `battle_status_debuff10_skill47_producer_apply`, `battle_status_debuff10_p12_body_visual_type9`, `battle_status_debuff10_catch_multiplier`, `battle_status_debuff10_expiry_clears_icon` |

## Cross-Cutting Rules Locked By The Table

### Buff14 Block

Target buff14 blocks ordinary debuff producers. This is covered per family by
dedicated smoke anchors such as:

- `battle_status_debuff0_buff14_blocks_skill1`
- `battle_status_debuff1_buff14_blocks_skill2`
- `battle_status_debuff2_buff14_blocks_skill12`
- `battle_status_debuff3_buff14_blocks_skill13`
- `battle_status_debuff4_buff14_blocks_skill31`
- `battle_status_debuff5_buff14_blocks_skill32`
- `battle_status_debuff6_buff14_blocks_skill33`
- `battle_status_debuff7_buff14_blocks_skill51`

For debuff8/debuff9, the source zero-power route is intentionally deviated from:
rebuild now applies their table debuffs in the no-damage post-effect producer.
Debuff8 still has a dedicated buff14 block checkpoint because skill54 uses the
normal target debuff gate with source chance `40`.

### P7 Miss And Source-Immediate Mutation

For ordinary debuff-producing direct skills, source audit showed that the
debuff slot/queue can be committed before the visible hit frame. Therefore:

- damage text and HP damage are hit-gated;
- debuff text is hidden on miss;
- the source debuff state can still be present after a miss.

This is intentionally smoke-covered for the families where it matters.

### Switch Lock

Only debuff2 `Quan Quanh` is the current switch/item/run lock.

Do not add switch lock to debuff9 `Hon Loan`; the user explicitly confirmed
that confused pets may still switch.

### Debuff10 Delay

Do not implement Tê Liệt action-delay unless a true source consumer for
`w[10]` / `p(10)` in action scheduling is found.

The suspicious source call:

```text
game.d h.f((byte)10)
```

is held/passive item id10, already documented in:

```text
261_battle_held_item10_cam_lam_chi_diep_hp_floor_audit.md
```

## Verification State

Latest verification after debuff10 closeout:

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| focused debuff10 PNG smoke | PASS, `6/6` |
| `battle_quick` | PASS, `227/227` |
| touched Java/docs mojibake scan | PASS, no matches |

## Closeout Decision

Debuff table `aq.c[7]` rows `0..10` is closed for the current battle roadmap as:

```text
PORTED / PORTED-PARTIAL / INTENTIONAL_DEVIATION where explicitly noted
```

Do not reopen this table just to reduce labels. Reopen only for:

- a concrete source-route mismatch;
- a failed smoke regression;
- original-vs-rebuild visual capture proof;
- newly found source callsite, especially for debuff10 action-delay.

## Next Step

Move to the skill/effect phase:

```text
301_battle_skill_grouped_logic_animation_roadmap.md
```

That phase should work by skill family, not by raw numeric order.
