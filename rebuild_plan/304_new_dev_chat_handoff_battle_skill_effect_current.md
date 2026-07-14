# 304 - New Dev Chat Handoff: Battle Skill / Effect Current Roadmap

Date: 2026-07-14

Status: NEW CHAT HANDOFF / CURRENT SKILL-EFFECT PHASE.

This handoff supersedes the "next step" section of
`265_new_dev_chat_handoff_battle_skill_effect_roadmap.md`. Older docs are still
source references, but the current active lane has moved past buff/debuff table
closeout and past the first skill10 direct-base smoke slice.

Keep this file path-portable. Do not hardcode absolute local paths in code,
docs, scripts, prompts, or reports. Use project-relative paths such as
`modules/`, `rebuild_game/`, and `rebuild_plan/`.

## Copy-Paste Prompt For New Dev Chat

```text
You are taking over VQSV/Liet Hoa battle skill/effect work.

Current lane:
- Battle skill/effect parity after buff/debuff closeout.
- Buff table aq.c[6] rows 0..14 is closed.
- Debuff table aq.c[7] rows 0..10 is closed, with documented partials/deviations.
- Current skill phase is direct/base P7 animation and skill-family completion.
- Latest focused slice: skill10 / Diep Toan direct-base smoke harness.

Supreme rules:
- Source first. Do not guess skill logic, UI, effect, animation, RNG, state flow, formula, or asset id.
- Read original source and decoded data before coding.
- Do not open the live client unless the user explicitly asks. Default is PNG smoke/headless.
- Do not add release-visible debug overlays. Smoke-only traces/checkpoints are allowed.
- Every claim must be classified: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN / SOURCE_ODDITY / INTENTIONAL_DEVIATION / GAMEPLAY_FIXED / NOT_REACHED.
- Do not claim pixel-perfect unless there is original-vs-rebuild frame comparison.
- Every skill/effect slice must show:
  1. plain explanation of what the skill/effect does;
  2. source audit proving rows/methods/assets;
  3. PNG smoke before / during / after;
  4. numeric state before / during / after, especially HP, PP, effect metadata, buff/debuff values;
  5. final audit: what is ported, what is partial, what is pending;
  6. next recommended roadmap step.
- For P7 damage skills, never merge "damage text frame" with "HP settled frame".
  They are different checkpoints. Damage text can appear while HUD HP is still
  tweening. The smoke must include both if the slice talks about final HP.

Before coding, read the required docs/source files below and answer the entry
exercise in chat.
```

## Required Reading Order

Read these docs first:

1. `rebuild_plan/304_new_dev_chat_handoff_battle_skill_effect_current.md`
2. `rebuild_plan/303_battle_all_skill_source_logic_animation_audit.md`
3. `rebuild_plan/302_battle_skill_direct_base_animation_audit.md`
4. `rebuild_plan/301_battle_skill_grouped_logic_animation_roadmap.md`
5. `rebuild_plan/300_battle_debuff_table_0_10_closeout.md`
6. `rebuild_plan/280_battle_buff_table_0_14_closeout.md`
7. `rebuild_plan/263_battle_buff_debuff_effect_current_matrix.md`
8. `rebuild_plan/264_battle_p7_result_flow_hit_miss_commit_matrix.md`
9. `rebuild_plan/battle_engine_master_roadmap_progress.md`

Then read source and data for the current slice:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P7 resolve.
  - `game.d.n()` effect row loading.
  - `game.d.q()` post-skill behavior.
  - P12/P13 active effect queue.
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - damage formula.
  - buff/debuff apply/tick/clear.
  - PP/stat hooks.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - battle HUD/status/UI facade.
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
  - AH special effect renderer.
- `modules/source_code/decoded/decompiled_source_cfr/l.java`
  - drawRGB / transform helpers when visual parity is touched.
- `modules/script/decoded/data__script__db.mid.json`
  - `aq.c[1]` skill rows, `aq.c[6]` buff rows, `aq.c[7]` debuff rows.
- `modules/script/decoded/data__script__chs.mid.json`
  - skill/effect names and descriptions.
- `modules/script/decoded/data__script__effect.mid.json`
  - P7 effect rows.
- `modules/script/decoded/data__script__speffect.mid.json`
  - AH special rows.
- `modules/script/decoded/data__script__bufDebuf.mid.json`
  - P12/P13 active body effect rows.

Then inspect rebuild files:

- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvBattleAnimationTables.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Current Status Summary

Battle engine broad state:

- Phase 1..9 battle foundations are closed or source-shaped for current routes.
- Phase 10/11 UI/runtime foundations are partial but usable for battle work.
- Buff/debuff effect phase is closed for the current table scope.
- Current active work is skill-by-skill / skill-family animation and logic
  tightening, starting from direct base skills.

Effect table status:

| Area | Status | Notes |
| --- | --- | --- |
| `aq.c[6]` buff rows 0..14 | CLOSED / PORTED or documented partial/deviation | See `280` and `263`. |
| Buff6 Kien Nhan | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | User approved 50% proc, 50% incoming damage reduction. |
| Buff10 Man Luc | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | User approved 3-turn attack boost: +15%, +10%, +5%, clear. |
| Buff11 Thau Thu | PORTED/PARTIAL | Donor copy/clear is smoke-covered; broader multi-enemy/full source slot remains partial. |
| Buff12 Gia Toc | PORTED/PARTIAL | Follow-up/PP conservation smoke-covered; full global turn-vector parity remains partial. |
| `aq.c[7]` debuff rows 0..10 | CLOSED / PORTED or documented partial/deviation | See `300` and `263`. |
| Debuff8 Quy Mi | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | User approved +10% outgoing damage and 55% self / 45% opponent route. |
| Debuff9 Hon Loan | PORTED/PARTIAL | Random target consumer smoke-covered; full multi-active route parity pending. Pet switch is allowed. |
| Debuff10 Te Liet | PORTED/PARTIAL | Producer/icon/P12 visual/catch/expiry covered; action-delay scheduling not found in PC source. |

Skill audit status:

- Full skill table `aq.c[1][0..69]` is documented in `303`.
- Grouped development order is documented in `301`.
- Direct base representative audit is documented in `302`.
- First concrete direct-base smoke slice for skill10 / Diep Toan is implemented
  in `VqsvSmokeHarness`.

## Latest Completed Skill Slice: Skill10 / Diep Toan

Skill10 is the current proof template for direct-base P7 slices.

Source facts:

```text
aq.c[1][10] = [1,127,539,100,0,45,0,-1,-1,0]
effect.mid[10] = [0,0,21,1,-1,-1,0]
```

Plain behavior:

- Direct low-damage wood/leaf lane attack.
- Costs 1 PP.
- No buff/debuff side effect.
- P7 first plays actor action effect id `21`, state `1`.
- Rebuild source map resolves effect id `21` to actor sprite `263`.
- Damage text appears in P7 phase 2.
- HUD HP then tweens down; the final settled HP is not the same frame as the
  damage text frame.

Focused smoke suite:

```text
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_skill10_direct_animation .\build_intro_demo\battle_skill10_direct_animation
```

Current checkpoints:

| Checkpoint | Meaning |
| --- | --- |
| `battle_skill10_direct_before` | P3 skill list before confirming skill10. HP full, PP 45. |
| `battle_skill10_direct_actor_u21_start` | P7 actor visual started. HP unchanged, PP 44, actor sprite 263/state 1. |
| `battle_skill10_direct_damage_frame` | Damage text is visible. HP HUD can still be full because tween has not settled. |
| `battle_skill10_direct_hp_settled` | HP display has settled to `maxHp - damage`. This replaced the misleading old `finish` meaning. |

Latest focused result:

```text
battle_skill10_direct_before       state=P3  hp=134/134:109/109
battle_skill10_direct_actor_u21_start state=P7 hp=134/134:109/109
battle_skill10_direct_damage_frame state=P7  hp=134/134:109/109 damage=23
battle_skill10_direct_hp_settled   state=P7  hp=134/134:86/109 damage=23
```

Important lesson:

- The old final checkpoint once captured `107/109`. That was only the first HP
  tween step, not the final damage result.
- New rule: if a report says "after damage", prove the `hp_settled` checkpoint.
- For skill10 sample above, `109 - 23 = 86`, so `86/109` is the correct settled
  HP for that run.

Current classification:

| Item | Status |
| --- | --- |
| Skill10 source row / PP / no status side effect | PORTED |
| Skill10 effect row loading | PORTED |
| Actor sprite/state smoke metadata | PORTED |
| Damage frame vs HP-settled timing smoke | PORTED |
| Exact original-client actor cursor/pixel parity | PENDING |
| Full direct-base family parity for all skills | PORTED/PARTIAL |

## Rules For Future Skill/Effect Slices

Every slice must start with a short explanation for the user:

- What is the skill/effect in plain language?
- Is it direct damage, buff, debuff, heal, leech, follow-up, clear, catch/status,
  random target, or special no-damage route?
- What should the player visibly see?
- What numbers should change?

Then source audit:

- `aq.c[1][skill]` raw skill row.
- Text ids and source name/description.
- `effect.mid[skill]` split into 7-value chunks.
- Any `speffect.mid` rows referenced by chunks with `chunk[1] == 1`.
- Buff/debuff row if `effectMode` and `effectId` point into `aq.c[6]` or `aq.c[7]`.
- Source methods that consume it:
  - `game.d.n()` for P7 visual rows.
  - `game.b.b(target)` for damage/debuff formula.
  - `game.b.a(byte,int,int)` for buff apply.
  - `game.b.o(int)` / `game.b.q(int)` for active ticks.
  - `game.d.q()` for post-skill heal/leech/follow-up/buff routes.
  - `game.h` only if UI/status display is involved.

Then smoke plan:

- before checkpoint;
- producer/actor/special visual checkpoint;
- damage or no-damage resolution checkpoint;
- HP/PP/stat/effect value checkpoint;
- P12/P13 active queue checkpoint if source says active body visual/tick exists;
- expiry/clear checkpoint if a duration effect is involved;
- regression checkpoint or suite.

For P7 direct damage skills, use this standard checkpoint shape:

```text
<skill>_before
<skill>_actor_or_special_start
<skill>_damage_frame
<skill>_hp_settled
```

Do not call the HP tween start "finish". If the HUD HP is still tweening, say
that clearly.

## Current Skill Roadmap

Do not implement all 70 skills one by one in numeric order. Use source family
order from `301` and `303`.

### Lane A - Direct Simple / Base Damage

Goal: make representative direct P7 animation/timing reliable before touching
complex multi-chunk skills.

Representative skills:

```text
0, 10, 20, 30, 40, 50, 60
```

Current state:

| Skill | Name | P7 row | Status | Next |
| ---: | --- | --- | --- | --- |
| 0 | Hoa trao | `[0,0,20,0,-1,-1,0]` | PORTED/PARTIAL | Add same before/actor/damage/hp_settled smoke as skill10. |
| 10 | Diep Toan | `[0,0,21,1,-1,-1,0]` | PORTED/PARTIAL | First focused smoke slice complete; exact pixel parity pending. |
| 20 | Hat bui | `[0,0,22,0,-1,-1,0]` | PORTED/PARTIAL | Add same direct-base smoke. |
| 30 | Bong bong | `[0,0,23,0,-1,-1,0]` | PORTED/PARTIAL | Add same direct-base smoke. |
| 40 | Dien giat | `[0,0,24,0,-1,-1,0]` | PORTED/PARTIAL | Add same direct-base smoke. |
| 50 | Anh thu | `[0,0,25,0,-1,-1,0, 0,1,9,0,-1,-1,0]` | PENDING_AUDIT | Separate two-chunk actor + AH type9 audit; do not mix with simple one-chunk lane. |
| 60 | Phong nhan | `[0,0,26,0,-1,-1,0]` | PORTED/PARTIAL | Add same direct-base smoke. |

Recommended next practical work:

1. Update/extend the direct-base representative suite for one-chunk rows
   `0,20,30,40,60`, using the skill10 standard.
2. Only after those pass, audit skill50 as a separate two-chunk direct-base
   visual case: actor chunk then AH type9 chunk.
3. Do not claim pixel-perfect until original-client frame capture exists.

### Lane B - Direct Plus Post-Skill `game.d.q()`

Examples:

```text
11,17,21,27,42,48,52,58,62,63,68,69
```

Focus:

- Damage frame first.
- Then post-skill heal/leech/buff/follow-up route.
- Prove ordering against `game.d.q()`.
- Smoke must show numeric before/during/after:
  - HP damage;
  - heal/leech amount;
  - buff values;
  - PP cost/refund if relevant.

### Lane C - Direct Plus Debuff/Status

Examples:

```text
1,2,7,8,12,13,18,19,22,28,31,32,33,37,38,39,41,47,51,57,61
```

Focus:

- Producer visual.
- Hit/miss side-effect commit.
- Buff14 block regression.
- P12/P13 body visual/tick when source gate says yes.
- Expiry/clear.

Most logic is already ported through debuff closeout; future work here is mainly
P7 animation/timing and route-specific visual parity.

### Lane D - No-Damage Buff Producers

Examples:

```text
4,5,14,15,24,25,34,35,44,45,64,65
```

Focus:

- No fake HP damage.
- Producer visual from `effect.mid`.
- Correct buff apply values.
- Status icon/duration cells.
- P12/P13 active body visual only when source gate includes the id.

### Lane E - Special / Zero-Power / Gameplay-Fixed Routes

Examples:

```text
54,55
```

Rules:

- Do not re-enable ordinary debuff producers for skill54/55.
- Source proves their ordinary producer path is `NOT_REACHED` under zero-power
  P7 guard.
- Debuff8 is `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`.
- Debuff9 is `PORTED/PARTIAL`.

## Verification Commands

Run from `rebuild_game/`.

Focused smoke first:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_skill10_direct_animation .\build_intro_demo\battle_skill10_direct_animation
```

After code changes:

```powershell
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvBattleDamageFormulaCheck
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_quick .\build_intro_demo\battle_quick
git diff --check
rg -n "[\x80-\xFF]|Tr\?n|th\?t|h\?" src/main/java
```

If a build command runs in parallel with classpath checks, wait for build to
finish before running Java checks. A previous false failure happened because
classes were being rebuilt while checks started.

## Report Format Expected By User

Keep reports concrete:

1. Explain the skill/effect in simple terms.
2. Source audit summary:
   - raw skill row;
   - effect row;
   - source methods;
   - exact formulas/values.
3. PNG previews:
   - before;
   - visual/actor/special;
   - damage frame;
   - HP settled or final state.
4. Numeric state:
   - HP before/during/after;
   - PP before/during/after;
   - actor/special metadata;
   - buff/debuff values if any.
5. Status:
   - PORTED / PORTED-PARTIAL / PENDING / INTENTIONAL_DEVIATION.
6. Verification commands and results.
7. Next recommended step.

In final chat responses, show PNGs with absolute paths generated on the user's
machine. In docs, use project-relative paths only.

## Compulsory Entry Exercise

Before coding, the new dev chat must answer in Vietnamese:

1. Which docs and source files did you read?
2. Prove skill10 / Diep Toan from source:
   - raw `aq.c[1][10]`;
   - raw `effect.mid[10]`;
   - actor effect id, sprite id, and state.
3. Explain why `damage_frame` and `hp_settled` are different.
   Use the latest smoke numbers: damage `23`, enemy HP `109 -> 86`.
4. What is the status of buff/debuff tables now?
   Name at least three intentional/partial exceptions: buff6, buff10, debuff8,
   debuff9, debuff10.
5. Build a candidate matrix for the next direct-base slice:
   skills `0,20,30,40,60`, and why skill50 must be separate.
6. Write the PNG-only smoke plan for the next slice.
7. List files allowed to edit for the next small slice, and files not to touch.
8. State exactly what remains PENDING before anyone can claim pixel-perfect.

If the answer is uncertain, do more source audit first. Do not code by guessing.

## One-Line Current Next Step

Next recommended work:

```text
Extend the direct-base representative smoke suite from skill10 to the remaining
one-chunk direct-base skills 0,20,30,40,60, using separate damage_frame and
hp_settled checkpoints. Then audit skill50 as its own actor + AH type9
two-chunk visual slice.
```
