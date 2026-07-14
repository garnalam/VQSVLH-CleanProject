# 340 - New Dev Chat Handoff: Skill Logic + Battle Animation Reopened

Date: 2026-07-14

Status: NEW CHAT HANDOFF / VISUAL PARITY REOPENED.

This handoff supersedes the active "continue skill lane" instruction from older
handoffs. The current user-visible problem is not only skill logic. There are
two active work lanes now:

1. Skill logic parity: source rows, damage, PP, buff/debuff, hit/miss/crit,
   post-effect, active queue, expiry.
2. Battle animation parity: pet action states, actor `u` effects, AH effects,
   hit/recover/dead states, draw order, frame timing, and visual side/crop.

The second lane is currently blocking confidence in the first lane. The user
has tested visually and says the animations are still wrong: many skills look
the same and pet/skill animation timing still does not feel like the original.
Treat every previous Fire skill visual closeout as REOPENED/PARTIAL until the
shared animation pipeline is re-audited and fixed.

Keep paths project-relative in docs and reports. Do not hardcode local absolute
paths in code.

## Copy-Paste Prompt For New Dev Chat

```text
You are taking over VQSV/Liet Hoa battle skill/effect + battle animation parity.

Critical current state:
- Skill logic has many smoke checks, but visual animation parity is REOPENED.
- Do not continue closing new skills as "done" just because source rows and
  numeric smoke pass.
- The user visually tested and reports that many skill animations still look
  identical/wrong. Treat this as a real bug, not a misunderstanding.
- Current priority is audit/fix the shared P7 animation pipeline before
  continuing skill-by-skill completion.

Supreme rules:
- Source first. Do not guess animation, effect id, timing, draw order, UI, RNG,
  formula, or skill behavior.
- Do not open the live client/game/JAR unless the user explicitly asks. Default
  is headless PNG smoke only.
- Never add release-visible debug overlays. Smoke-only traces/checkpoints are ok.
- Every claim must be classified: PORTED / PARTIAL / PENDING / UNKNOWN /
  SOURCE_ODDITY / INTENTIONAL_DEVIATION.
- Do not claim pixel-perfect unless there is original MIDP-vs-rebuild frame
  comparison.
- If visual smoke still looks wrong, say it is wrong/PARTIAL. Do not explain it
  away.
- Never hardcode per-skill fake animation just to make screenshots look different.
  Fix shared source-backed renderer/runtime first.

Current hard problem:
- Fire skills 0/1/3/6/7/9 share source actor chunk `u20/state0`.
- That source fact does not mean the whole live presentation is complete.
- The correct next work is to audit the shared actor/pet animation pipeline:
  `game.d` P7 -> `game.b.a(short,byte)` -> `ah.java` actor action -> `d.java`
  sprite animation -> `game.b.a(Graphics)` draw order -> rebuild runtime/renderer.

Before coding, read the docs and source files below, then complete the entry
exercise. Only after the exercise should you patch code.
```

## Required Reading Order

Read these docs first:

1. `rebuild_plan/340_new_dev_chat_handoff_skill_animation_reopened.md`
2. `rebuild_plan/341_battle_skill_animation_current_roadmap.md`
3. `rebuild_plan/339_battle_p7_actor_u20_shared_renderer_audit.md`
4. `rebuild_plan/338_battle_fireskill_live_frame_strip_reaudit.md`
5. `rebuild_plan/337_battle_fireskill_live_animation_reaudit_followup.md`
6. `rebuild_plan/335_battle_fireskill_animation_reaudit.md`
7. `rebuild_plan/304_new_dev_chat_handoff_battle_skill_effect_current.md`
8. `rebuild_plan/303_battle_all_skill_source_logic_animation_audit.md`
9. `rebuild_plan/302_battle_skill_direct_base_animation_audit.md`
10. `rebuild_plan/301_battle_skill_grouped_logic_animation_roadmap.md`
11. `rebuild_plan/264_battle_p7_result_flow_hit_miss_commit_matrix.md`
12. `rebuild_plan/263_battle_buff_debuff_effect_current_matrix.md`
13. `rebuild_plan/battle_engine_master_roadmap_progress.md`

Then read source:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P7 update/draw cases.
  - `game.d.n()` effect chunk loading.
  - chunk trigger `[4]/[5]/[6]`.
  - `game.d.q()` post-skill behavior.
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `a(short, byte)` actor action creation.
  - `a(Graphics)` draw order.
  - state methods `d(byte)`, `p()`, `b()`, hit/dead render behavior.
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
  - actor action ids `20..34`.
  - AH type 1/7/8/9/12 and any row used by current skills.
- `modules/source_code/decoded/decompiled_source_cfr/d.java`
  - Java ME sprite actor animation `a(byte,byte,boolean)`, `d()`, `e()`,
    `b(frame)`, `a(Graphics,x,y,dir)`.
- `modules/source_code/decoded/decompiled_source_cfr/l.java`
  - drawRGB image transform/alpha/texture behavior.
- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/blood.mid`
- `modules/script/original/bufDebuf.mid`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__chs.mid.json`

Then read rebuild code:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvSpriteRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleAnimationTables.java`
- `rebuild_game/src/main/java/FireSkill.java`
- `rebuild_game/src/main/java/EarthSkill.java`
- `rebuild_game/src/main/java/WoodSkill.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Entry Exercise

Before coding, answer these in the new chat:

1. For Fire skills `0/1/3/6/7/9`, prove from `effect.mid` whether the first
   actor chunk is identical or different.
2. Prove what source actor id `20` maps to in `ah.java`, and how rebuild maps it.
3. Explain why "same `u20/state0` source chunk" does not prove the whole skill
   animation is complete.
4. Show the exact source draw order for `game.b.a(Graphics)` and compare it to
   rebuild `VqsvBattleRenderer.renderSourceLikeBattleUi()`.
5. Explain the latest fixed bug: `drawP7ActorEffect()` previously double-applied
   battle offsets.
6. List what remains PARTIAL after that fix.
7. State which smoke PNGs you will run and why.

If the answer says "Fire animation is done" or "all Fire skills should look the
same" without the PARTIAL caveat, it has failed the exercise.

## Current Known Facts

### Fire Source Rows

Many Fire skills share the first actor chunk:

| Skill | Source path | Current interpretation |
|---:|---|---|
| 0 | `u20/state0` | source-backed same first chunk |
| 1 | `u20/state0` + debuff0 | visual should also include status/debuff result |
| 2 | `u20/state0 -> speffect14/AH12` | two-stage, AH12 still visually weak/PARTIAL |
| 3 | `u20/state0` + conditional damage | same first chunk, logic differs |
| 4 | `speffect16/AH9 -> speffect15/AH1` | self-buff; crop player side |
| 5 | `speffect16/AH9 -> speffect15/AH1` | same visual producer as skill4, buff differs |
| 6 | `u20/state0` | same first chunk, higher damage |
| 7 | `u20/state0` + debuff0 | visual should also include status/debuff result |
| 8 | `u20/state0 -> speffect14/AH12` | two-stage, chunk trigger differs from skill2 |
| 9 | `u20/state0` + conditional damage | same first chunk, logic differs |

### Latest Fix

`VqsvBattleRenderer.drawP7ActorEffect()` was fixed so actor effects are anchored
like base actors:

```text
sourceBattleActorX/Y + sideOffset
```

It no longer adds `playerOffsetX/enemyOffsetX` a second time.

### Latest Smoke

Headless only:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_u20_offset_fix
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_u20_offset_fix
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\battle_quick_after_u20_offset_fix
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Há»|Ä" src\main\java
```

All passed in the previous chat. Mojibake scan returns no matches with exit code
`1`, which is OK for `rg`.

## Current Do / Don't

Do:

- Audit shared animation pipeline before adding more skill closeouts.
- Prefer source matrices and frame strips over single screenshots.
- Keep PNG smoke small but visually useful: full frame + player crop + enemy crop
  + effect-side crop.
- Separate skill logic result from animation result in reports.
- Tell the user exactly what remains PARTIAL.

Do not:

- Open the game/client/JAR automatically.
- Continue adding new lane skills while Fire shared animation is unresolved.
- Claim "source says same animation, therefore user is wrong." Source may only
  prove the first chunk, while live presentation includes later layers/states.
- Hardcode fake different Fire visuals per skill.
- Hide visual failures behind passing numeric smoke.

## Immediate Next Recommended Slice

Create an audit doc:

```text
342_battle_p7_actor_sprite_timing_draw_order_matrix.md
```

Scope:

- `game.d` P7 actor update order.
- `game.b.a(short,byte)` and `game.b.a(Graphics)`.
- `ah.java` actor id `20..34`.
- `d.java` animation timing: `d.d()`, `d.e()`, `d.b(frame)`.
- `SpriteAnim.tickHoldLast()` vs source behavior.
- current rebuild draw order.

Then code only if the audit proves a mismatch.
