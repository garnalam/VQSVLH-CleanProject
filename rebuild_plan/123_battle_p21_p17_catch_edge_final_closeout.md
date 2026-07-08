# 123 Battle P21/P17 Catch Edge Final Closeout

Status: PORTED/PARTIAL + CHECKPOINTS PASS.

Purpose:

- Close the source-backed P21/P17 catch edge behavior before moving on in Phase 5.
- Keep scope to battle catch list/result/storage/warnings.
- Do not touch intro/world/panel.
- No live client/game; PNG/headless checkpoints only.

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

Rebuild files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`

## Source Chain

### P21 `game.h.ah()/ai()`

| Source behavior | Rebuild status |
| --- | --- |
| `game.d case 21` sets catch target then calls `game.h.ah()` | PORTED |
| `game.h.ah()` opens `/data/ui/choice.ui`, title `Pokemon ball`, subtitle `Ti le bat`, action `Su dung` | PORTED/PARTIAL |
| Rows use `q.K`, item icon/name, and `game.d.b(item)` catch chance | PORTED/PARTIAL |
| Back closes `choice.ui` and returns command state | PORTED |
| Confirm with count consumes one ball, sets `game.d.l`, calls `o.m()`, enters P17 | PORTED |
| Missing count opens `/data/ui/msgwarm.ui` with no-balls warning | PORTED/PARTIAL |
| Missing count item `0` goes to state 101 purchase/SMS path | PORTED/REBUILD_POLICY as free grant, no SMS UI/network |

### P17 `game.d case 17`

| Source behavior | Rebuild status |
| --- | --- |
| Load catch sprite `269`, place at enemy, set `q=0` | PORTED/PARTIAL |
| Chance is `game.d.b(l)`, decision is `ae.a(100) < chance` | PORTED/PARTIAL with RNG trace |
| Bunny tutorial `U==0 && V==5` forces fail | PORTED/PARTIAL |
| q0/q1/q2/q3/q4 phase chain | PORTED/PARTIAL |
| q1/q4 H/ah effect draw with catch sprite | PORTED/PARTIAL |
| q4 fail restores enemy visibility and returns battle turn | PORTED/PARTIAL |
| q3 success routes storage through `game.g.y()` | PORTED |
| `game.g.y()==0` add bag | PORTED |
| `game.g.y()==1` add bank and second openbox notice | PORTED |
| `game.g.y()==2` full release message, no add | PORTED |
| Captured payload follows `game.b.P()` shape | PORTED/PARTIAL |

## Final Closeout Checkpoints

| Checkpoint | Purpose |
| --- | --- |
| `battle_catch_missing_count_warning` | Non-item0 missing count opens msgwarm warning. |
| `battle_catch_missing_count_warning_return_p21` | Warning confirm returns to P21. |
| `battle_catch_p21_back_to_command` | Back from P21 returns command UI. |
| `battle_catch_sms_free_item0_p17` | Item0 missing count follows source P101 hook, rebuild free-grants and enters P17. |
| `battle_catch_storage_bag` | Success storage result bag and payload shape. |
| `battle_catch_storage_bank` | Success storage result bank plus second openbox. |
| `battle_catch_storage_full_release` | Full bag/bank release message, no add. |
| `battle_p17_q1_h_effect_order` | P17 q1 effect visible while target hidden. |
| `battle_p17_q4_fail_restore_enemy` | P17 q4 fail clears catch visuals and restores enemy. |
| `route_bunny_after_battle_task` | Bunny tutorial route still reaches return-to-elder task. |

Checkpoint result:

| Checkpoint | Status | PNG |
| --- | --- | --- |
| `battle_catch_missing_count_warning` | PASS | `rebuild_game/build_intro_demo/closeout_catch_missing_count_warning.png` |
| `battle_catch_missing_count_warning_return_p21` | PASS | `rebuild_game/build_intro_demo/closeout_catch_missing_count_warning_return_p21.png` |
| `battle_catch_p21_back_to_command` | PASS | `rebuild_game/build_intro_demo/closeout_catch_p21_back_to_command.png` |
| `battle_catch_sms_free_item0_p17` | PASS | `rebuild_game/build_intro_demo/closeout_catch_sms_free_item0_p17.png` |
| `battle_catch_storage_bag` | PASS | `rebuild_game/build_intro_demo/closeout_catch_storage_bag.png` |
| `battle_catch_storage_bank` | PASS | `rebuild_game/build_intro_demo/closeout_catch_storage_bank.png` |
| `battle_catch_storage_full_release` | PASS | `rebuild_game/build_intro_demo/closeout_catch_storage_full_release.png` |
| `battle_p17_q1_h_effect_order` | PASS | `rebuild_game/build_intro_demo/closeout_catch_p17_q1_h_effect_order.png` |
| `battle_p17_q4_fail_restore_enemy` | PASS | `rebuild_game/build_intro_demo/closeout_catch_p17_q4_fail_restore_enemy.png` |
| `route_bunny_after_battle_task` | PASS | `rebuild_game/build_intro_demo/closeout_catch_route_bunny_after_battle_task.png` |

Regression run:

- `build.ps1`: PASS.
- Java mojibake scan: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- `battle_exp_levelup_learn_skill_done`: PASS.

## Still Partial By Design

| Item | Status | Reason |
| --- | --- | --- |
| Full state 101 SMS UI/network | BYPASSED BY USER POLICY | User policy: SMS purchase is free; no SMS send required. |
| Full generic `choice.ui` runtime | PARTIAL | Current renderer is source-shaped/manual, not complete widget VM. |
| Full generic `msgwarm.ui`/`openbox.ui` runtime | PARTIAL | Frames/text behavior covered; not every widget feature generic. |
| P17 exact MIDP pixel/frame parity | PARTIAL | Source-shaped q0..q4/effect exists; user will report visual mismatch if needed. |
| Exact RNG stream replay globally | PARTIAL | P17 roll has trace/deterministic smoke hook; whole-game RNG stream is broader work. |
| Save/global persistence of caught pets | PARTIAL | Runtime party/bank payload covered; full save persistence is broader save-runtime task. |

## Next Roadmap Step

After these checkpoints pass, P21/P17 catch edge behavior is closed enough to
leave unless the user finds a concrete visual mismatch. Phase 5 should then move
to either:

1. P4/P16 item flow parity, or
2. P5/P15 pet switch parity.
