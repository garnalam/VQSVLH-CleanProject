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
- Current completed clean slices: buff0, buff1, buff2, buff3.
- Next code slice: buff4 Phong ngu, producer skills 21/27, defense +10% from skill[8], duration 2, icon 16, no P12/P13 body visual.

Supreme rules:
- Source first. Never guess logic, UI, assets, animation, RNG, state flow, or formulas.
- Read original source before coding: game.b, game.d, game.h, aq tables, effect.mid, speffect.mid, bufDebuf.mid, status icon sheet.
- UI/effect only when source actually calls it.
- Do not open the live client/JAR/game window unless the user explicitly asks. Default to smoke PNG/headless.
- Do not add debug overlays to gameplay/release UI. Smoke-only traces/checkpoints are fine.
- Do not use absolute local paths in docs/code.
- Classify honestly: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN / SOURCE_ODDITY.
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

Current next slice:

| Effect | Producer skills | Required behavior |
| --- | --- | --- |
| buff4 Phong ngu | `21`, `27` | Duration `2`; row param is sentinel `-1`; source uses producer `skill[8]`; known skills use defense `+10% baseDefense`; icon `16`; no P12/P13 body visual. |

Do not skip ahead to debuffs until the remaining buffs are closed or the user
explicitly redirects.

## Current Smoke/Regression State

Latest known good state after buff3:

- Build: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- `VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick`: PASS, `61/61`.
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
| 4 | Phong ngu | `21`, `27` | PORTED/PARTIAL | NEXT ACTIVE SLICE. |
| 5 | Vo hinh | `34` | PORTED/PARTIAL | After buff4; must cover chance reflect + body visual. |
| 6 | Kien nhan | `35` | SOURCE_ODDITY/PARTIAL | Must audit carefully; do not "fix" negative/sentinel without proof. |
| 7 | Linh Xao | `42`, `48` | PORTED/PARTIAL | Source uses skill param, speed +5%. |
| 8 | Dien ap | `44` | PORTED | Already has dedicated coverage; revisit only if needed. |
| 9 | Hoa Thach | `45` | PORTED/PARTIAL | Need dedicated table-order closeout if not already enough. |
| 10 | Man Luc | `62`, `68` | PORTED-AS-SOURCE / SOURCE_ODDITY | Preserve source oddity unless proven otherwise. |
| 11 | Thau Thu | `64` | PARTIAL | Needs selected donor/multi-target vector care. |
| 12 | Gia Toc | `65` | PORTED/PARTIAL | Follow-up/PP conservation sensitive. |
| 13 | Thach Hoa | `24` | PORTED/PARTIAL | Cleanse + 20% maxHP heal + body visual. |
| 14 | Thach Phu | `25` | PORTED/PARTIAL | Cleanse + debuff immunity. |

### Debuffs After Buffs - `aq.c[7]`

Do not start this group until buffs are closed or user explicitly redirects.

| Order | Effect | Producer skill(s) | Current status | Required focus |
| ---: | --- | --- | --- | --- |
| 0 | Gieo Hat | `1`, `7` | PORTED | DoT divisor, body visual, expiry. |
| 1 | Me Muoi | `2`, `8`, `22`, `28` | PORTED/PARTIAL | Flag, conditional skills, catch multiplier. |
| 2 | Quan Quanh | `12`, `18` | PORTED/PARTIAL | Bind/command lock: item/pet/run disabled checks still important. |
| 3 | Thuc Loai | `13`, `19` | PORTED | Delayed damage at expiry. |
| 4 | Muc | `31`, `37` | PORTED/PARTIAL | Miss/evasion hook. |
| 5 | Cham Chap | `32`, `38`, `61`, `67` | PORTED | Speed down, skill67 already audited separately. |
| 6 | Nhut Chi | `33`, `39` | PORTED/PARTIAL | Outgoing damage -10%. |
| 7 | Phong Ngu | `51`, `57` | PORTED | Defense down -20%. |
| 8 | Quy Mi | `54` | FLAG/PENDING | Zero-power special route; audit before code. |
| 9 | Hon Loan | `55` | FLAG/PENDING | Switch-lock/P5 parity; audit before code. |
| 10 | Te Liet | `41`, `47` | PORTED/PARTIAL | Catch/action-delay flag; exact delay timing pending. |

## Next Slice Details: Buff4 Phong Ngu

Source facts to prove before coding:

- Buff row: `aq.c[6][4] = [337,352,2,-1,-1]`.
- Producer skills: `21`, `27`.
- `game.b.a(byte,int,int)` case `4` stores source skill id in `K[4]`.
- Case `4` computes `v[4][1] = baseDefense * aq.c[1][sourceSkill][8] / 100`.
- Known producer `skill[8] = 10`, so defense `+10% baseDefense`.
- Duration is `2`.
- Icon cell is `4 + 12 = 16`.
- Duration cells are `134 + duration`, so duration `2 -> 136`, then `1 -> 135`, then clear.
- P12/P13 body visual gate says buff4 has no body visual.

Suggested smoke checkpoints:

- `battle_status_buff4_producer_visual_defense`
- `battle_status_buff4_forced_hit_target_defense`
- `battle_status_buff4_forced_miss_no_extra_side_effect`
- `battle_status_buff4_forced_crit_no_wrong_multiplier`
- `battle_status_buff4_expiry_clears_defense`

Possible approach:

- Use player producer skill `21` first; add skill `27` smoke only if source row differs.
- For producer visual, start player at defense `100`, cast skill `21`, assert current defense `110`, buff value `10`, duration `2`, icon `16`.
- For defense impact, put buff4 on target and compare incoming damage baseline vs defense-up damage.
- For miss, force hit roll miss and assert no damage and no wrong side effects.
- For crit, force crit and assert crit damage is computed from the defense-up formula, not from a separate buff multiplier.
- For expiry, tick source buff twice and assert defense restored and icon cleared.

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
rebuild_game/build_intro_demo/battle_status_buff4_producer_visual_defense.png
```

## Compulsory Entry Exercise

Before coding, the new dev chat must answer these questions in Vietnamese:

1. What files/docs did you read, and which source methods prove buff4's formula?
2. Why does buff4 use producer `skill[8]` instead of raw row params `-1,-1`?
3. What are the expected icon and duration cells for buff4 at duration `2` and `1`?
4. Does buff4 need P12/P13 body visual? Why or why not?
5. What exact smoke checkpoints will you add for buff4?
6. Which regression commands will you run?
7. What is the next roadmap step after buff4 if it passes?

If any answer is uncertain, audit source first. Do not code by guessing.

## One-Line Current Status For Handoff

As of this handoff, battle skill-effect work is in temporary buff/debuff
completion. Buff0, buff1, buff2, and buff3 are PORTED with dedicated smoke
coverage. The next slice is buff4 Phong ngu, source-backed defense +10% from
producer skills `21/27`, duration `2`, icon `16`, no P12/P13 body visual, with
full producer/logic/miss/crit/expiry smoke required before moving on.
