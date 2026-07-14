# 265 - New Dev Chat Handoff: Battle Skill / Buff / Debuff Roadmap

Date: 2026-07-13

Status: NEW CHAT HANDOFF / CURRENT ACTIVE BATTLE SKILL-EFFECT ROADMAP.

Purpose:

- Give a new dev chat enough context to continue the exact current workflow.
- Prevent looping around unrelated Phase 5/UI/item work.
- Continue source-backed skill/buff/debuff completion in table order.
- Preserve the strict rule: logic first, source proof first, smoke PNG only, no live client.

This document must stay path-portable. Do not hardcode local absolute paths in
code, docs, scripts, prompts, or reports. Use project-relative paths such as
`modules/`, `rebuild_game/`, and `rebuild_plan/`.

## Copy-Paste Prompt For New Dev Chat

```text
You are taking over VQSV/Liet Hoa battle skill-effect work.

Current active lane:
- Battle skill/effect parity, specifically temporary battle effects from aq.c[6] buffs and aq.c[7] debuffs.
- Continue in strict table order from rebuild_plan/263_battle_buff_debuff_effect_current_matrix.md.
- Current completed/closed slices: buff0, buff1, buff2, buff3, buff4, buff5, buff7, buff8, buff9, buff10, buff11, buff12, buff13, buff14.
- Latest completed docs: debuff table closeout `300` and skill grouped logic/animation roadmap `301`. Latest completed code slice: debuff10 Te Liet source-backed closeout (`299`) after debuff9 Hon Loan closeout (`297`). Buff table `aq.c[6]` rows 0..14 are closed in table order; debuff table `aq.c[7]` rows 0..10 is closed in `300`. Debuff8 is intentionally `GAMEPLAY_FIXED`, not source-parity: `+10%` outgoing damage and 55% self-hit / 45% opponent-hit. Debuff10 is `PORTED/PARTIAL`: producer/icon/P12 visual/catch/expiry are smoke-covered, while action-delay scheduling remains `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF`. Next phase should follow `301`: skill work by grouped logic/effect/animation families, starting with direct base animation audit.

Supreme rules:
- Source first. Never guess logic, UI, assets, animation, RNG, state flow, or formulas.
- Read original source before coding: game.b, game.d, game.h, aq tables, effect.mid, speffect.mid, bufDebuf.mid, status icon sheet.
- UI/effect only when source actually calls it.
- Do not open the live client/JAR/game window unless the user explicitly asks. Default to smoke PNG/headless.
- Do not add debug overlays to gameplay/release UI. Smoke-only traces/checkpoints are fine.
- Do not use absolute local paths in docs/code.
- Classify honestly: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN / SOURCE_ODDITY / INTENTIONAL_DEVIATION.
- Each buff/debuff slice must prove:
  1. producer skill visual/effect,
  2. active logic with exact numbers,
  3. hit/miss/crit interaction if relevant,
  4. P12/P13 active queue behavior if source has it,
  5. icon/duration cells,
  6. expiry/clear behavior,
  7. regression does not break routes.
- After each prompt/slice, report the next roadmap step.

Before coding, read this handoff, read the required docs/source files, then answer the entry exercise at the end. Do not code until the exercise is answered.
```

## Required Reading Order

Read these docs first:

1. `rebuild_plan/265_new_dev_chat_handoff_battle_skill_effect_roadmap.md`
2. `rebuild_plan/263_battle_buff_debuff_effect_current_matrix.md`
3. `rebuild_plan/264_battle_p7_result_flow_hit_miss_commit_matrix.md`
4. `rebuild_plan/battle_engine_master_roadmap_progress.md`
5. `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`
6. `rebuild_plan/187_battle_phase9ad_skill_coverage_closeout.md`
7. `rebuild_plan/76_battle_p12_p13_active_queue_lifecycle_matrix.md`
8. `rebuild_plan/77_battle_p12_p13_h_speffect_matrix.md`
9. `rebuild_plan/78_battle_active_effect_lifecycle_full_matrix.md`

Then read source for the current slice:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `a(byte,int,int)` buff apply.
  - `o(int)` buff tick.
  - `d(int,slot)` / clear helpers.
  - `b(target)` damage formula hooks.
  - `q(int)` debuff tick.
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P7 resolve.
  - `game.d.q()` post-skill behavior.
  - P12/P13 active queue.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - battle HUD/status/icon/UI facade.
- `modules/source_code/decoded/decompiled_source_cfr/aq.java`
  - `aq.c` table access semantics.
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
  - AH special effect renderer types.
- `modules/source_code/decoded/decompiled_source_cfr/l.java`
  - drawRGB / transform helpers when visual exactness is touched.

Then inspect data/assets:

- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__chs.mid.json`
- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/bufDebuf.mid`
- `modules/img/decoded/data__img__img_804.mid.png`

Then inspect rebuild files:

- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvBattleAnimationTables.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Current Working Context

The current active work is not broad skill discovery anymore. Phase 9 broad
coverage classified every skill row. We are now doing source-tight completion
of temporary battle effects, one effect at a time, in `aq.c[6]` then `aq.c[7]`
order.

The controlling matrix is:

- `rebuild_plan/263_battle_buff_debuff_effect_current_matrix.md`

Current completed slices:

| Effect | Status | What is locked |
| --- | --- | --- |
| buff0 Suc Luc | PORTED | Producer visual, defense +30%, stored extra damage, duration-edge hook, expiry. |
| buff1 Pha Phu | PORTED | Producer visual, defense -50%, outgoing damage +50%, hit/miss/crit, expiry. |
| buff2 Kinh Cuc | PORTED | Producer visual, defense +30%, reflect 10%, miss no reflect, crit reflect, expiry. |
| buff3 Khoi phuc | PORTED | Producer visual, apply heal 5% maxHP, P12/P13 body visual, P12/P13 heal tick, expiry. |

Current recent/next slice summary:

| Effect | Producer skills | Required behavior |
| --- | --- | --- |
| buff4 Phong ngu | `21`, `27` | PORTED: duration `2`; source uses producer `skill[8]`; known skills use defense `+10% baseDefense`; icon `16`; no P12/P13 body visual; dedicated before/producer/hit/miss/crit/expiry smokes pass. |
| buff5 Vo hinh | `34` | PORTED: chance reflect from source row, P12/P13 actor body visual, success/fail/expiry smokes pass. |
| buff6 Kien nhan | `35` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED: user approved target-side 50% proc, 50% incoming damage reduction; dedicated before/producer/chunk0/chunk1/reduction success-fail/P12 no-body/expiry smokes pass. |
| buff7 Linh Xao | `42`, `48` | PORTED: raw damage plus speed self-buff, source uses producer `skill[8] = 5`, speed `+5%`; dedicated before/producer42/producer48/P12 no-body/expiry smokes pass. |
| buff10 Man Luc | `62`, `68` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED: source row `-1` lowers attack `100 -> 99`, but user approved 3-turn attack decay `+15% -> +10% -> +5% -> clear`; dedicated producer/gameplay-decay/P12 no-body/expiry smokes pass. |
| buff11 Thau Thu | `64` | PORTED/PARTIAL: cast-time selected donor copy/clear, active tick re-steal from `game.b.o(11)`, nonzero donor slot, donor switch cleanup, and stale status icon clear have focused smoke coverage. See `275` + `276`. Remaining partials: multi-enemy/full source `d[]` slot and broader KO/replacement cleanup. |

Buff table `aq.c[6]` is now closed through `280`. Debuff table `aq.c[7]`
has debuff0 `Gieo Hat` closed in `282`, debuff1 `Me Muoi` closed in `283`,
debuff2 `Quan Quanh` closed in `284`, debuff3 `Thuc Loai` closed in `286`, debuff4 `Muc` closed in `288`, debuff5 `Cham Chap` closed in `290`, and debuff6 `Nhut Chi` closed in `292`. The current lane starts after debuff6.

## Current Smoke/Regression State

Latest known good state after buff10:

- Build: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- `VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick`: PASS, `109/109`.
- Mojibake scan for Java + docs touched in this lane: no new hits.

Important recent smoke checkpoint groups:

| Group | Checkpoints |
| --- | --- |
| buff0 | `battle_status_buff0_producer_visual`, `battle_status_buff0_duration0_damage_hook`, `battle_status_buff0_expiry_clears_defense` |
| buff1 | `battle_status_buff1_producer_visual`, `battle_status_buff1_forced_hit_damage_defense`, `battle_status_buff1_forced_miss_no_damage`, `battle_status_buff1_forced_crit_damage`, `battle_status_buff1_expiry_clears_damage_defense` |
| buff2 | `battle_status_buff2_producer_visual`, `battle_status_buff2_forced_hit_reflect_defense`, `battle_status_buff2_forced_miss_no_reflect`, `battle_status_buff2_forced_crit_reflect`, `battle_status_buff2_expiry_clears_defense_reflect` |
| buff3 | `battle_status_buff3_producer_visual_apply_heal`, `battle_status_buff3_p12_body_visual_start`, `battle_status_buff3_p12_heal_tick`, `battle_status_buff3_expiry_clears_icon` |

## Development Law For Each Effect Slice

Every effect slice must follow this exact order.

### Step 1 - Source Audit

Before code, prove:

- Raw effect row from `aq.c[6]` or `aq.c[7]`.
- Vietnamese name/description from text table.
- Producer skill id(s) and raw `aq.c[1][skill]`.
- Whether source uses row params or producer skill params.
- Which `game.b` method applies it.
- Which `game.b` method ticks/clears it.
- Whether `game.d.q()` has post-skill behavior.
- Whether P12/P13 active queue runs for it.
- Whether `bufDebuf.mid` gives body visual.
- Whether P7 producer uses `effect.mid` / `speffect.mid` / actor action.
- Which HUD icon cell and duration cell should show.

### Step 2 - Define Required Smoke

Each effect should have dedicated smoke PNGs. Minimum:

- `battle_status_<effect>_producer_visual...png`
- active logic smoke with exact before/after numbers.
- miss smoke if effect can interact with damage commit.
- crit smoke if final damage changes the effect result.
- P12/P13 body visual smoke if source says body visual exists.
- expiry smoke.

If a category is not applicable, say why in the doc.

### Step 3 - Code Only The Proven Slice

Allowed files for most effect slices:

- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java` only if rendering state is proven needed.
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_plan/263_battle_buff_debuff_effect_current_matrix.md`

Avoid unrelated files. Do not touch intro/world/panel/item/catch unless the
current effect source path explicitly calls them.

### Step 4 - Regression

After code, run from `rebuild_game/`:

```powershell
.\build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick
rg -n "Ã|Â|�|\?i\?|Th\?" src/main/java "..\rebuild_plan\263_battle_buff_debuff_effect_current_matrix.md" "..\rebuild_plan\264_battle_p7_result_flow_hit_miss_commit_matrix.md"
```

Also run the individual new checkpoints first, before the full suite.

### Step 5 - Report

Final report must include:

- What is now PORTED.
- Exact source facts and formulas.
- Files changed.
- Smoke PNG paths and visible previews.
- Regression results.
- What remains PARTIAL/PENDING.
- The next roadmap step.

## Current Roadmap: Temporary Effects

### Buffs First - `aq.c[6]`

Continue this order exactly unless user redirects.

| Order | Effect | Producer skill(s) | Current status | Next action |
| ---: | --- | --- | --- | --- |
| 0 | Suc Luc | `4` | PORTED | Leave alone unless regression fails. |
| 1 | Pha Phu | `5` | PORTED | Leave alone unless regression fails. |
| 2 | Kinh Cuc | `14` | PORTED | Leave alone unless regression fails. |
| 3 | Khoi phuc | `15` | PORTED | Leave alone unless regression fails. |
| 4 | Phong ngu | `21`, `27` | PORTED | Leave alone unless regression fails. |
| 5 | Vo hinh | `34` | PORTED | Leave alone unless regression fails. |
| 6 | Kien nhan | `35` | INTENTIONAL_DEVIATION/GAMEPLAY_FIXED | User-approved 50% proc and 50% incoming damage reduction; original-vs-rebuild pixel comparison remains pending. |
| 7 | Linh Xao | `42`, `48` | PORTED | Dedicated closeout smoke pass; speed +5% from producer skill param. |
| 8 | Dien ap | `44` | PORTED | Already has dedicated coverage; revisit only if needed. |
| 9 | Hoa Thach | `45` | PORTED | Dedicated closeout smoke pass; speed +50%, defense -50%, P7 animation timeline, no P12 body visual. |
| 10 | Man Luc | `62`, `68` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | Source oddity is documented, but current runtime intentionally uses 3-turn attack boost decay `15/10/5` by user approval. |
| 11 | Thau Thu | `64` | PORTED/PARTIAL | Focused closeout pass in `276`; keep partial for multi-enemy/full source slot and broader KO/replacement cleanup. |
| 12 | Gia Toc | `65` | PORTED/PARTIAL | Closeout `277`: producer speffect16/15, K12 apply/tick/follow-up, PP conservation, icon/duration, no-body-visual, and expiry smokes pass. Full global turn-vector/multi-actor parity remains partial. |
| 13 | Thach Hoa | `24` | PORTED | Closeout `278`: skill24 actor22 and speffect17 producer, no-damage route, cleanse, `20% maxHP` heal on apply/tick, P13 body visual, icon/duration, and expiry smokes pass. Pixel-perfect original comparison remains pending. |
| 14 | Thach Phu | `25` | PORTED | Closeout `279`: skill25 speffect4/17 producer, no-damage route, cleanse, debuff-family immunity, P13 no-body-visual/pre-clear, icon/duration, and expiry smokes pass. Pixel-perfect original comparison remains pending. |

### Debuffs After Buffs - `aq.c[7]`

Do not start this group until buffs are closed or user explicitly redirects.

| Order | Effect | Producer skill(s) | Current status | Required focus |
| ---: | --- | --- | --- | --- |
| 0 | Gieo Hat | `1`, `7` | PORTED | DoT divisor, body visual, expiry. |
| 1 | Me Muoi | `2`, `8`, `22`, `28` | PORTED | Closeout `283`: producer skills, icon/duration, P12/P13 speffect14 type12, no-op tick/expiry, catch multiplier `11/10`, conditional skills `23/29`, and buff14 block pass. |
| 2 | Quan Quanh | `12`, `18` | PORTED | Closeout `284`: producer skills, icon/duration, P12/P13 speffect6 type8, no-op tick/expiry, catch multiplier `12/10`, defense formula modifier, item/pet/run command locks, skill/catch/shop allowed, and buff14 block pass. |
| 3 | Thuc Loai | `13`, `19` | PORTED | Closeout `286`: producer skills, actor body visual, no-damage tick timing, final `150%/200%` delayed damage, KO transition, and buff14 block pass. |
| 4 | Muc | `31`, `37` | PORTED | Closeout `288`: producer skills, stored values `1/2`, P12/P13 no-body-visual skip via `game.d.ai[1]`, no-op tick/expiry, miss chance values `12/14`, and buff14 block pass. |
| 5 | Cham Chap | `32`, `38`, `61`; `67` NOT_REACHED | PORTED | Closeout `290`: producers `32/38/61`, stored speed-down values `10/10/5`, skill67 no-debuff regression, P12/P13 no-body-visual speed reassert, expiry speed restore, miss chance consumer, and buff14 block pass. |
| 6 | Nhut Chi | `33`, `39` | PORTED | Closeout `292`: producers `33/39`, stored value `10`, source-immediate miss mutation, P12/P13 no-body-visual no-op, expiry clear, outgoing damage `80 -> 72`, and buff14 block pass. |
| 7 | Phong Ngu | `51`, `57` | PORTED | Closeout `294`: defense down `-20%` base defense, miss source-immediate mutation, no P12/P13 body visual, expiry restore, incoming damage-up consumer, and buff14 block pass. |
| 8 | Quy Mi | `54` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | Closeout `296`: skill54 zero-power no ordinary producer, active damage `+10%`, target route `55%` self / `45%` opponent, body visual, expiry. Smoke locks self-hit `101 -> 111` and enemy-hit `80 -> 88`. |
| 9 | Hon Loan | `55` | PORTED/PARTIAL | Closeout `297`: zero-power no ordinary producer, active random-target consumer via `game.d.f(attacker)` + `ae.a(G.size())`, body visual, expiry. P5 pet switch is allowed; only Quan Quanh/debuff2 blocks switching. |
| 10 | Te Liet | `41`, `47` | PORTED/PARTIAL | Catch/action-delay flag; exact delay timing pending. |

## Completed Slice Details: Buff4 Phong Ngu

Source facts already proven:

- Buff row: `aq.c[6][4] = [337,352,2,-1,-1]`.
- Producer skills: `21`, `27`.
- `game.b.a(byte,int,int)` case `4` stores source skill id in `K[4]`.
- Case `4` computes `v[4][1] = baseDefense * aq.c[1][sourceSkill][8] / 100`.
- Known producer `skill[8] = 10`, so defense `+10% baseDefense`.
- Duration is `2`.
- Icon cell is `4 + 12 = 16`.
- Duration cells are `134 + duration`, so duration `2 -> 136`, then `1 -> 135`, then clear.
- P12/P13 body visual gate says buff4 has no body visual.

Completed smoke checkpoints:

- `battle_status_buff4_before_no_effect`
- `battle_status_buff4_producer_visual_defense`
- `battle_status_buff4_forced_hit_target_defense`
- `battle_status_buff4_forced_miss_no_extra_side_effect`
- `battle_status_buff4_forced_crit_no_wrong_multiplier`
- `battle_status_buff4_expiry_clears_defense`

Closeout notes:

- Producer skill `21` dedicated smoke locks `effect.mid[21]` as `sourceEffectId=22` plus `speffect=5`.
- Skill `27` is source-audited as the same buff formula with `speffect=7`; broad Phase9R coverage remains the regression anchor unless a dedicated visual mismatch is found.
- Expiry follows source `game.b.o(4)`: apply `100 -> 110`, first tick `110 -> 120`, second tick clears to `100`.

## Completed Slice Details: Buff5 Vo Hinh

Source facts already proven:

- Buff row: `aq.c[6][5] = [338,353,3,30,-1]`.
- Producer skill: `34`.
- `game.b.a(byte,int,int)` case `5` stores the source chance/param.
- Damage-receive hook in `game.b` stores incoming damage in attacker `K[5]` when `ae.a(100) <= 30`.
- `game.d.q()` consumes `K[5]` and damages the attacker, then clears `K[5]`.
- P12/P13 body visual is expected because source `game.d.ai[0]` includes buff id `5`.
- Producer visual is `effect.mid[34] = [0,1,4,0,-1,-1,0]`.
- Active queue visual is `ar[0][5] -> ap[6] = [0,23,0,-1]`, a type0 actor action.

Completed smoke checkpoints:

- `battle_status_buff5_producer_visual`
- `battle_status_buff5_forced_reflect_success`
- `battle_status_buff5_forced_reflect_fail`
- `battle_status_buff5_p12_body_visual_start`
- `battle_status_buff5_expiry_clears_icon`

## Completed Slice: Buff6 Kien Nhan

Source facts now locked:

- Buff row: `aq.c[6][6] = [339,354,3,50,-1]`.
- Producer skill: `35`.
- Producer visual: `effect.mid[35] = [0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]`, so chunk0 uses speffect `4` / AH type7 and chunk1 uses speffect `17` / AH type1.
- Source branch is odd, but the user approved an intentional gameplay fix: if target has buff6 and roll `<= 50`, incoming damage is reduced by `50%`.
- Deterministic smoke result: baseline `80`, success roll `0` result `41` after half-damage plus normal jitter; fail roll `99` result `80`.
- P12/P13 body visual is not expected because source gate `game.d.ai[0]` excludes buff id `6`.

Passing buff6 smoke checkpoints:

- `battle_status_buff6_before_no_effect`
- `battle_status_buff6_producer_visual`
- `battle_status_buff6_visual_chunk0_type7`
- `battle_status_buff6_visual_chunk1_type1`
- `battle_status_buff6_damage_reduction_success`
- `battle_status_buff6_damage_reduction_fail`
- `battle_status_buff6_p12_no_body_visual`
- `battle_status_buff6_expiry_clears_icon`

## Completed Slice: Buff7 Linh Xao

Source facts now locked:

- Buff row: `aq.c[6][7] = [340,355,2,-1,-1]`.
- Producer skills: `42`, `48`.
- Skill42 row: `[4,159,571,90,0,45,1,7,5,0]`.
- Skill48 row: `[4,165,577,130,3,15,1,7,5,0]`.
- `game.b.a case 7` stores the source skill in `K[7]`, uses producer `skill[8]`, and sets speed to `baseSpeed + baseSpeed * skill[8] / 100`.
- Both producer skills use `skill[8] = 5`, so speed is `+5% baseSpeed`.
- Producer visual differs: both start with source effect id `24`; skill42 then uses speffect `1`, skill48 uses speffect `9`.
- P12/P13 body visual is not expected because source gate `game.d.ai[0]` excludes buff id `7`.

Passing buff7 smoke checkpoints:

- `battle_status_buff7_before_no_effect`
- `battle_status_buff7_producer_visual_speed_skill42`
- `battle_status_buff7_producer_visual_speed_skill48`
- `battle_status_buff7_p12_no_body_visual`
- `battle_status_buff7_expiry_clears_speed`

## Completed Slice: Buff10 Man Luc

Source facts now locked:

- Buff row: `aq.c[6][10] = [343,358,2,-1,-1]`.
- Producer skills: `62`, `68`.
- Source text says attack-up, but `game.b.a case 10` uses row param `-1` literally: `baseAttack * -1 / 100`.
- Historical source audit locks the source oddity `attack 100 -> 99` and sample damage `80 -> 79`.
- Current runtime intentionally deviates by user approval: `attack 100 -> 115 -> 110 -> 105 -> 100`; sample turn-1 damage `80 -> 98`.
- Producer skills `62/68` share `effect.mid` row: actor action `26`, then `speffect 0`, then `speffect 15`.
- P12/P13 body visual is not expected because source gate `game.d.ai[0]` excludes buff id `10`.

Passing buff10 smoke checkpoints:

- `battle_status_buff10_before_no_effect`
- `battle_status_buff10_skill62_start`
- `battle_status_buff10_skill62_actor26`
- `battle_status_buff10_skill62_speffect0`
- `battle_status_buff10_skill62_speffect15`
- `battle_status_buff10_skill62_after_apply`
- `battle_status_buff10_gameplay_decay_attack_up`
- `battle_status_buff10_p12_no_body_visual`
- `battle_status_buff10_expiry_clears_attack`

Next table-order note: buff14 `Thach Phu` is complete in `279_battle_buff14_thach_phu_audit_closeout.md`. Buff table `aq.c[6]` rows 0..14 are closed in `280_battle_buff_table_0_14_closeout.md`. Debuff0 `Gieo Hat` is complete in `282_battle_debuff0_gieo_hat_closeout.md`; debuff1 `Me Muoi` is complete in `283_battle_debuff1_me_muoi_closeout.md`; debuff2 `Quan Quanh` is complete in `284_battle_debuff2_quan_quanh_closeout.md`; debuff3 `Thuc Loai` is complete in `286_battle_debuff3_thuc_loai_closeout.md`; debuff4 `Muc` is complete in `288_battle_debuff4_muc_closeout.md`; debuff5 `Cham Chap` is complete in `290_battle_debuff5_cham_chap_closeout.md`; debuff6 `Nhut Chi` is complete in `292_battle_debuff6_nhut_chi_closeout.md`; debuff7 `Phong Ngu` is complete in `294_battle_debuff7_phong_ngu_closeout.md`; debuff8 `Quy Mi` is complete in `296_battle_debuff8_quy_mi_closeout.md` as `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`; debuff9 `Hon Loan` is complete in `297_battle_debuff9_hon_loan_closeout.md`; debuff10 `Te Liet` is complete for source-backed producer/icon/P12 visual/catch/expiry in `299_battle_debuff10_te_liet_closeout.md`. Action-delay scheduling stays `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF`.

## Important Pitfalls From Recent Work

- Do not assume every producer uses speffect chunks. Example: buff2 skill `14` used actor action `sourceEffectId=21`, not buff1's speffect path.
- Do not assume smoke helper state is automatically synced. A recent fix synced `player.hp = player.battleUnit.hp()` after smoke-only `debugPlayerSourceBuffForSmoke`.
- Do not apply side effects on miss. P7 must compute potential result first and commit side effects only on hit.
- Do not use `BattleUnit.damage(0)` expecting no damage; its source-shaped helper clamps damage to at least `1`. Guard zero-damage hooks.
- Do not call a body visual "missing" unless P12/P13 gate says it should exist. Buff body visuals currently exist for buff ids `3`, `5`, `13` only.
- Do not rewrite old docs broadly to fix mojibake; make targeted updates. The repo has historical encoding noise.
- Do not add in-game visible debug text for user/release. Use source trace and smoke assertions.

## Standard Smoke PNG Rule

The user expects images to be shown, not just filenames. In final reports,
include Markdown image previews using the absolute path produced in the active
workspace at report time. Do not hardcode that machine path into source code,
scripts, or handoff docs.

In docs, list PNGs project-relatively, for example:

```md
rebuild_game/build_intro_demo/battle_status_buff6_producer_visual.png
rebuild_game/build_intro_demo/battle_status_buff6_visual_chunk0_type7.png
rebuild_game/build_intro_demo/battle_status_buff6_visual_chunk1_type1.png
rebuild_game/build_intro_demo/battle_status_buff6_damage_reduction_success.png
rebuild_game/build_intro_demo/battle_status_buff6_damage_reduction_fail.png
rebuild_game/build_intro_demo/battle_status_buff6_p12_no_body_visual.png
rebuild_game/build_intro_demo/battle_status_buff6_expiry_clears_icon.png
rebuild_game/build_intro_demo/battle_status_buff7_producer_visual_speed_skill42.png
rebuild_game/build_intro_demo/battle_status_buff7_producer_visual_speed_skill48.png
rebuild_game/build_intro_demo/battle_status_buff7_p12_no_body_visual.png
rebuild_game/build_intro_demo/battle_status_buff7_expiry_clears_speed.png
```

## Compulsory Entry Exercise

Before coding, the new dev chat must answer these questions in Vietnamese:

1. What files/docs did you read, and which source methods prove buff6's apply and damage-reduction/source-oddity hooks?
2. What is `aq.c[6][6]`, which producer skill creates it, and which `effect.mid` row proves the producer visual?
3. Why is buff6 marked INTENTIONAL_DEVIATION/GAMEPLAY_FIXED, and what user-approved behavior replaces the source oddity?
4. Does buff6 need P12/P13 body visual? Which source gate proves it?
5. What source methods prove debuff7 defense-down, and why does it have no P12/P13 body visual despite a bufDebuf row?
6. Which regression commands will you run?
7. What remains PARTIAL/PENDING after debuff6 and why is pixel-perfect original comparison still not claimed?

If any answer is uncertain, audit source first. Do not code by guessing.

## One-Line Current Status For Handoff

As of the latest update, battle skill-effect work has closed the self-buff table
`aq.c[6]` rows `0..14`. Buff6 Kien nhan and buff10 Man Luc remain explicitly
user-approved `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`; buff11 and buff12 remain
`PORTED/PARTIAL` for broader source-vector parity. Buff closeout summary is
`280`; debuff0 `Gieo Hat` closeout is `282`; debuff1 `Me Muoi` closeout is
`283`; debuff2 `Quan Quanh` closeout is `284`; debuff3 `Thuc Loai`
closeout is `286`; debuff4 `Muc` closeout is `288`; debuff5 `Cham Chap`
closeout is `290`; debuff6 `Nhut Chi` closeout is `292`; debuff7 `Phong Ngu`
closeout is `294`; debuff8 `Quy Mi` closeout is `296`; debuff9 `Hon Loan`
closeout is `297`; debuff10 `Te Liet` closeout is `299`; debuff table closeout is `300`; skill grouped roadmap is `301`. Debuff8 and debuff9
remain `PORTED/PARTIAL` only for full multi-active visible target divergence;
their zero-power no-ordinary-producer paths, active consumers, body visuals,
and expiry are smoke-locked. Debuff9 P5 pet switch is `NOT_APPLICABLE /
USER_CONFIRMED_ALLOWED`; only debuff2 `Quan Quanh` blocks item/pet/run
commands. Debuff10 producer/icon/P12 visual/catch/expiry are smoke-locked;
action-delay scheduling remains `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF`
because `game.d h.f((byte)10)` is held item/passive id `10`, not debuff10. Keep
skill54/55 ordinary debuff producers `NOT_REACHED` under the zero-power P7
guard.

Next practical target: create `302_battle_skill_direct_base_animation_audit.md`
from `301`, starting with representative direct base skills `0,10,20,30,40,50,60`.
Do not reopen debuff10 unless a real source action-delay callsite is found.
