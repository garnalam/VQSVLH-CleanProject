# 239 - Battle Status Effectiveness Closeout

Date: 2026-07-13

Scope: Slice A status effectiveness smoke-only.

Runtime gameplay logic was not broadly changed. This slice added smoke hooks and
headless checkpoint coverage to prove whether battle statuses are visually and
logically effective.

## Files Changed

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only helpers for player form/status, source buff setup, source debuff setup, and enemy stat probes/setup. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added 8 `battle_status_*` checkpoints and included them in `battle_quick`. |

## Smoke Results

All 8 checkpoints completed and exported PNGs. One checkpoint is intentionally
marked `PENDING` in trace because its icon/slot exists but gameplay effect does
not yet match source text.

| Checkpoint | Result | What it proves |
| --- | --- | --- |
| `battle_status_buff3_heal_tick` | PORTED | Buff3 `Khôi phục` applies, shows HUD icon `15`, runs P13 visual/tick, and HP heal text appears. |
| `battle_status_buff10_attack_up_damage` | PORTED-AS-SOURCE / SOURCE_ODDITY | Buff10 `Man Lực` shows HUD icon `22`; source row uses `-1`, so damage does not increase: baseline `80`, buffed `79`. See audit `242`. |
| `battle_status_buff14_blocks_debuff` | PORTED | Buff14 `Thạch Phu` shows HUD icon `26` and blocks incoming debuff application. |
| `battle_status_debuff0_damage_tick` | PORTED | Debuff0 `Gieo Hạt` shows HUD icon `1`, runs P12 visual/tick, and HP decreases. |
| `battle_status_debuff5_speed_down` | PORTED | Debuff5 `Chậm Chạp` shows HUD icon `6` and lowers speed when source formula has enough base stat to produce a delta. |
| `battle_status_debuff7_defense_down` | PORTED | Debuff7 `Phòng Ngự` shows HUD icon `8` and increases next damage taken. |
| `battle_status_debuff10_catch_multiplier` | PORTED/PARTIAL | Debuff10 `Tê Liệt` shows HUD icon `11`, applies from skill41, and raises catch chance. P12 visual remains covered by existing Phase 9L smoke. |
| `battle_status_form9_no_miss` | PORTED | Form/status9 `Cá Thờn Bơn` has no HUD queue icon and forces miss chance to `0`, so a high-speed target setup still hits. |

## PNG Outputs

```text
rebuild_game/build_intro_demo/status_effectiveness_battle_status_buff3_heal_tick.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_buff10_attack_up_damage.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_buff14_blocks_debuff.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_debuff0_damage_tick.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_debuff5_speed_down.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_debuff7_defense_down.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_debuff10_catch_multiplier.png
rebuild_game/build_intro_demo/status_effectiveness_battle_status_form9_no_miss.png
```

## Regression

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `java -cp build/classes com.vqsv.rebuild.Main --check` | PASS |
| `java -cp build/classes VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS for this slice; hits are existing valid Vietnamese cutscene text, not mojibake |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Regression PNGs:

```text
rebuild_game/build_intro_demo/status_effectiveness_regression_route_sophie_after_battle_branch.png
rebuild_game/build_intro_demo/status_effectiveness_regression_route_bunny_after_battle_task.png
rebuild_game/build_intro_demo/status_effectiveness_regression_route_elder_after_battle_reward_state.png
```

## Important Source Oddity

Buff10 `Man Lực` has been audited in
`242_battle_buff10_man_luc_source_audit.md`.

Facts:

- Vietnamese source text says attack/strength increases.
- `aq.c[6][10] = [343,358,2,-1,-1]`.
- Decompiled `game.b.a(byte,int,int)` case 10 uses
  `this.c[2] * aq.c[6][10][3] / 100`.
- Bytecode and original `db.mid` bytes confirm signed short `-1`; it is not a
  JSON decode error and not reinterpreted by skill producer `62/68`.
- Current rebuild mirrors source, so `-1` does not increase damage.
- Smoke measured baseline damage `80`, buff10 damage `79`.

Do not silently "fix" buff10 by inventing a positive percent. Any behavior that
makes buff10 increase attack should be marked `APPROX/DESIGN_FIX`, not
source-port parity.

## Next Recommended Slice

`240/241` and this closeout show the status layer is mostly effective, with one
real gap. Next step should be:

1. Add optional status smokes: form0 low HP attack, form4 crit bonus,
   form10 HP floor, buff13 cleanse/heal visual, debuff3 delayed damage,
   debuff2 command disable.
