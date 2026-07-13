# 228 - Panel State17 / Petsetting Item Behavior Smoke Matrix

Status: SOURCE-BACKED SMOKE PLAN + CHECKPOINT IMPLEMENTED / PORTED-PARTIAL.

Purpose:

- Lock focused PNG coverage for item behaviors that are already source-proven
  and already reachable through the current rebuild item table/shop flow.
- Close the weakest item-completion area identified by
  `227_battle_item_full_completion_matrix.md`: panel `state17` and
  `petsetting c=0` normal item behavior coverage.
- Avoid coding random item logic before proving which existing branch actually
  fails.

This document is path-portable. Do not add machine-specific absolute paths.

## Source Anchors

Primary source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/script/decoded/data__script__db.mid.json`

Rebuild/audit anchors:

- `rebuild_plan/200_panel_bag_default_state17_audit.md`
- `rebuild_plan/204_panel_bag_default_state17_item_behavior_audit.md`
- `rebuild_plan/205_panel_item_5_12_source_metadata_closeout.md`
- `rebuild_plan/206_battle_p11_shop_item_reachability_audit.md`
- `rebuild_plan/207_battle_p11_shopbuy_msgyn_polish_closeout.md`
- `rebuild_plan/227_battle_item_full_completion_matrix.md`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`

## Source Flow Recap

### Panel state17

Source path:

```text
bag.ui b=0 default item row
-> game.h.ac()
   -> this.s = itemId
   -> game.k state 17
   -> close /data/ui/bag.ui
-> game.k state 17 entry
   -> S.l = false
   -> S.c = 0
   -> S.W()
-> game.h.W()/e(c)
   -> open /data/ui/petstate.ui
-> game.h.Z()
   -> confirm calls bo()
-> game.h.bo()
   -> selected pet validates x(this.s)
   -> inventory check q.b(this.s,1,0)
   -> pet.w(this.s)
   -> refresh petstate
   -> open /data/ui/msgwarm.ui success/warning
```

State17 does not use `choice.ui`.

### Petsetting c=0

Source-shaped rebuild path:

```text
petstate -> petsetting c=0 item choice
-> /data/ui/choice.ui
-> selected item validates against selected pet
-> item behavior applies through source-shaped game.b.x/w equivalent
-> /data/ui/msgwarm.ui success/warning
-> returns to petstate / item choice according to source-shaped loop
```

This path differs from state17 in UI shell and selected-pet ownership, so it
needs separate smoke for behaviors that can regress there.

## Source Item Rows In Scope

From `aq.c[4]` / `data__script__db.mid.json` group `4`:

| itemId | raw source row | behavior | params | Source meaning |
| ---: | --- | ---: | --- | --- |
| `6` | `[267,31,284,100,0,2,25]` | `2` | `25` | Restore PP/skill value by 25. |
| `8` | `[269,33,286,250,0,3,50,50,20]` | `3` | `50,50,20` | Restore HP and PP. |
| `9` | `[270,34,287,500,0,3,100,0,45]` | `3` | `100,0,45` | Stronger HP and PP restore. |
| `10` | `[271,35,288,50,0,5]` | `5` | none | Clear bad effects/debuffs. |
| `12` | `[273,37,290,750,0,4,100,0,45]` | `4` | `100,0,45` | Stronger revive, HP + PP. |

Items `6/8/9/10/12` are P11 source-reachable. Source metadata for items
`5..12` is already backed by `VqsvBattleTables.item(id)` per doc `205`.

Behavior `6` exists in `game.b.x/w`, but no current source item row in
`aq.c[4]` uses behavior `6`. Do not invent a behavior-6 item.

## Validation / Mutation Requirements

Source `game.b.x(itemId)` and `game.b.w(itemId)`:

| Behavior | Source validation | Source mutation | Existing rebuild status |
| ---: | --- | --- | --- |
| `2` | if all skill PP full, warning code `3`; dead pet warning code `8`. | Restore PP by `paramA`. | PORTED/PARTIAL through P16, needs panel state17/petsetting smoke. |
| `3` | if HP and PP both full, warning code `7`; valid if either HP or PP needs refill. | Heal by `% maxHP + flat`; restore PP by `paramC`. | PORTED/PARTIAL through P16, needs panel state17/petsetting smoke. |
| `4` | alive pet invalid, warning code `1`; dead pet valid. | Revive/reset, set HP, restore PP. | PORTED/PARTIAL; item11 has state17 smoke, item12 needs stronger-revive smoke. |
| `5` | if no active bad effects, warning code `4`. | Clear debuffs via `C()`. | PORTED/PARTIAL through P16, needs panel state17/petsetting success/warning smoke. |

Warning codes to assert where possible:

| Code | Meaning |
| ---: | --- |
| `1` | Revive item used on living pet / invalid target. |
| `3` | Skill PP full. |
| `4` | No bad effect to clear. |
| `7` | HP and PP both full. |
| `8` | Dead pet cannot use non-revive item. |

## Current Smoke Coverage Already Present

Existing checkpoint family in `VqsvSmokeHarness`:

| Area | Current checkpoint(s) | Status |
| --- | --- | --- |
| state17 open/nav/back | `panel_bag_default_item_state17_open_petstate`, `navigation`, `back_returns_bag` | PORTED/PARTIAL |
| state17 generic warnings | `panel_bag_default_item_state17_warning_hp_full`, `warning_dead`, `warning_returns_petstate` | PORTED/PARTIAL |
| state17 item4 success/missing | `panel_bag_default_item_state17_success_msg`, `success_returns_petstate`, `missing_count_warning`, `missing_count_returns_bag` | PORTED/PARTIAL |
| state17 item11 revive | `panel_bag_default_item_state17_revival_item11_success_msg`, `returns_petstate` | PORTED/PARTIAL |
| petsetting item loop | `panel_petstate_petsetting_item_choice_open`, `navigation`, `back_returns_petstate`, `confirm_pending`, `warning_hp_full`, `warning_returns_choice`, `success_msg`, `success_returns_petstate` | PORTED/PARTIAL |
| battle P16 item behavior | `battle_p16_item_pp_restore`, `battle_p16_item_hp_pp`, `battle_p16_item_revive`, `battle_p16_item_clear_debuff`, warning family | PORTED/PARTIAL |
| shop reachability | P11 buy item `5..12` then P16 use | PORTED/PARTIAL |

Gap: state17 and petsetting do not yet have focused per-behavior smoke for
items `6/8/9/10/12`.

## Proposed New Checkpoints

### Panel state17

| Checkpoint | Setup | Assertions | Status |
| --- | --- | --- | --- |
| `panel_bag_state17_item6_pp_restore_success_msg` | Add item6 to normal bag; selected pet has reduced PP and alive HP. | opens petstate; confirm item6; PP increases; item count decrements once; success `msgwarm.ui`. | DONE |
| `panel_bag_state17_item6_pp_restore_returns_petstate` | Continue previous success. | confirm message returns to `petstate.ui`; selected pet remains same; PP mutation persists. | TODO |
| `panel_bag_state17_item8_hp_pp_success_msg` | Add item8; selected pet has reduced HP and reduced PP. | HP and PP increase according to behavior 3; item count decrements once; success `msgwarm.ui`. | DONE |
| `panel_bag_state17_item8_hp_pp_returns_petstate` | Continue previous success. | returns to `petstate.ui`; HP/PP mutation persists. | TODO |
| `panel_bag_state17_item10_clear_debuff_success_msg` | Add item10; selected pet has at least one active debuff/bad effect. | debuff cleared; item count decrements once; success `msgwarm.ui`. | DONE |
| `panel_bag_state17_item10_no_debuff_warning` | Add item10; selected pet has no debuff. | warning code `4`; item count does not decrement; stays in state17 loop after confirm. | DONE |
| `panel_bag_state17_item12_revive_success_msg` | Add item12; selected pet is dead. | pet revives with HP above zero and PP restored; item count decrements once; success `msgwarm.ui`. | DONE |
| `panel_bag_state17_item12_revive_returns_petstate` | Continue previous success. | returns to `petstate.ui`; revived HP/PP persists. | TODO |
| `panel_bag_state17_item8_both_full_warning7` | Add item8; selected pet HP and PP are full. | warning code `7`; item count does not decrement; returns to petstate loop. | DONE |
| `panel_bag_state17_item9_both_full_warning7` | Add item9; selected pet HP and PP are full. | warning code `7`; item count does not decrement; returns to petstate loop. | DONE |

### Petsetting c=0

Only add matching petsetting checkpoints where the source loop differs from
state17: `choice.ui` list ownership, selected pet owner, warning return to
choice, and success return to petstate.

| Checkpoint | Setup | Assertions | Status |
| --- | --- | --- | --- |
| `panel_petsetting_item6_pp_restore_success_msg` | Petsetting selected pet; item6 in normal bag; PP reduced. | choice row selects item6; PP increases; item count decrements once; success msg. | DONE |
| `panel_petsetting_item8_hp_pp_success_msg` | Selected pet has reduced HP/PP; item8 available. | HP/PP increase; count decrements; success msg. | DONE |
| `panel_petsetting_item10_clear_debuff_success_msg` | Selected pet has active debuff; item10 available. | debuff clears; count decrements; success msg. | DONE |
| `panel_petsetting_item10_no_debuff_warning` | Selected pet has no debuff; item10 available. | warning code `4`; no consume; confirm returns to item choice. | DONE |
| `panel_petsetting_item12_revive_success_msg` | Selected pet dead; item12 available. | revive succeeds; count decrements; success msg. | DONE |
| `panel_petsetting_item8_both_full_warning7` | Selected pet full HP/PP; item8 available. | warning code `7`; no consume; returns to choice. | DONE |
| `panel_petsetting_item9_both_full_warning7` | Selected pet full HP/PP; item9 available. | warning code `7`; no consume; returns to choice. | DONE |

Keep item `13/14` forbidden regression in scope only if the item-choice list is
changed. Do not mix it into the first behavior smoke slice unless a regression
appears.

## Implementation Guidance For The Next Code Slice

This doc does not change runtime code. The next code slice should be a
checkpoint/harness slice first.

Recommended order:

1. Add helper setup in `VqsvSmokeHarness` only for explicit smoke checkpoints:
   - seed item count in `sourceBagItems`;
   - seed selected pet HP/PP/debuff/dead state;
   - open the existing state17 or petsetting flow.
2. Do not add new behavior to `VqsvPanelRuntime` unless a checkpoint fails.
3. If a checkpoint fails, patch only the source-proven branch:
   - selected item id propagation;
   - selected pet target propagation;
   - warning code mapping;
   - item count consume-once ownership;
   - HP/PP/debuff/revive mutation persistence.
4. Do not touch battle P7, route scripts, Bunny/elder events, or release
   launcher for this slice.

## PNG Smoke Plan

First minimal batch:

```text
panel_bag_state17_item6_pp_restore_success_msg
panel_bag_state17_item8_hp_pp_success_msg
panel_bag_state17_item10_clear_debuff_success_msg
panel_bag_state17_item10_no_debuff_warning
panel_bag_state17_item12_revive_success_msg
panel_bag_state17_item8_both_full_warning7
panel_petsetting_item6_pp_restore_success_msg
panel_petsetting_item8_hp_pp_success_msg
panel_petsetting_item10_clear_debuff_success_msg
panel_petsetting_item10_no_debuff_warning
panel_petsetting_item12_revive_success_msg
panel_petsetting_item8_both_full_warning7
```

Second batch, if first batch passes:

```text
panel_bag_state17_item6_pp_restore_returns_petstate
panel_bag_state17_item8_hp_pp_returns_petstate
panel_bag_state17_item12_revive_returns_petstate
panel_bag_state17_item9_both_full_warning7
panel_petsetting_item9_both_full_warning7
```

## Required Regression After Code

For checkpoint-only changes:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java mojibake scan
focused new PNG checkpoints
```

If any panel/list input code changes:

```text
VqsvIntroDemo --smoke-suite panel_wheel
```

If any shared item behavior changes:

```text
VqsvIntroDemo --smoke-suite battle_quick
run_battle_lab_suite_smoke.cmd -Lane npc -Suite items_shop_exp
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
```

## Done Criteria

This item-coverage slice is done when:

- state17 has PNG proof for item `6`, `8`, `10`, `12`, and warning `7`;
- petsetting c0 has PNG proof for the same behavior groups where its loop
  differs from state17;
- item counts decrement exactly once on success and not on warning;
- HP/PP/debuff/revive mutations persist after closing `msgwarm.ui`;
- all claims remain `PORTED/PARTIAL`, not pixel-perfect UI parity;
- no unrelated battle/world route behavior changed.

## Implementation Closeout

Implemented in `rebuild_game/src/main/java/VqsvSmokeHarness.java`,
`rebuild_game/src/main/java/VqsvSourceModels.java`, and
`rebuild_game/src/main/java/VqsvBattleUnit.java`.

Runtime note:

- `SourcePetState` now keeps transient source-shaped debuff slots and bridges
  them into/out of `BattleUnit`. This is needed because source panel item use
  calls `game.b.x/w` on the pet object, where debuff state is live.
- No panel route, battle route, P7 effect, Bunny/Elder/Sophie script, or release
  launcher behavior was intentionally changed.

PNG output directory:

```text
rebuild_game/build_intro_demo/228_item_behavior/
```

Focused checkpoint result:

```text
panel_bag_state17_item6_pp_restore_success_msg       PASS
panel_bag_state17_item8_hp_pp_success_msg            PASS
panel_bag_state17_item10_clear_debuff_success_msg    PASS
panel_bag_state17_item10_no_debuff_warning           PASS
panel_bag_state17_item12_revive_success_msg          PASS
panel_bag_state17_item8_both_full_warning7           PASS
panel_bag_state17_item9_both_full_warning7           PASS
panel_petsetting_item6_pp_restore_success_msg        PASS
panel_petsetting_item8_hp_pp_success_msg             PASS
panel_petsetting_item10_clear_debuff_success_msg     PASS
panel_petsetting_item10_no_debuff_warning            PASS
panel_petsetting_item12_revive_success_msg           PASS
panel_petsetting_item8_both_full_warning7            PASS
panel_petsetting_item9_both_full_warning7            PASS
```

Regression run:

```text
build.ps1                                           PASS
com.vqsv.rebuild.Main --check                      PASS
VqsvBattleDamageFormulaCheck                       PASS
Java mojibake scan                                 PASS, no matches
VqsvIntroDemo --smoke-suite panel_wheel            PASS 8/8
VqsvIntroDemo --smoke-suite battle_quick           PASS 20/20
run_battle_lab_suite_smoke.ps1 npc items_shop_exp  PASS 15/15
```

## Remaining After This Slice

Even after these checkpoints pass, these remain outside this slice:

- full `petstate.ui`, `bag.ui`, `choice.ui`, and `msgwarm.ui` binary widget VM;
- exact item visual effects from `game.b.w()` such as heal text/effect
  `l(amount)`;
- equipment `q.L` save/load parity;
- remaining `q.N` special rows beyond current egg/ride slices;
- behavior `6`, because no current source item row reaches it;
- full original RMS inventory vector parity.
