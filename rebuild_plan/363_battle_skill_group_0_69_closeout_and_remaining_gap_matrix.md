# 363 - Battle Skill Group 0..69 Closeout And Remaining Gap Matrix

Date: 2026-07-15

## Purpose

This is the closeout document for visible battle skills `0..69` after the S60
resource/table merge and the Battle Lab skill group integration.

Controlling rule:

- Source first: skill metadata is read from `aq.c[1][skillId]` through
  `VqsvBattleTables.skill(id)`.
- Visual producer data is read from `script/original/effect.mid` through
  `VqsvBattleAnimationTables.effectRow(id)`.
- Rebuild behavior is verified through PNG smoke, HP/PP/status/follow-up
  assertions, and Battle Lab scenarios.
- No pixel-perfect claim is made here. Exact original-vs-rebuild frame compare
  remains pending.

## Executive Closeout

Status: `CLOSED / PORTED-PARTIAL + SMOKE-COVERED`.

Meaning:

- All skills `0..69` have Battle Lab manual scenarios.
- All seven 10-skill group suites pass after a fresh build.
- Existing focused closeout suites for Earth 25..29, Water 30..39, Electric
  40..49, Shadow 50..59, and Wind 60..69 pass.
- `battle_quick` regression passes after the closeout run.
- Remaining work is not broad "missing skill coverage"; it is exact pixel
  parity, rare route parity, and intentionally accepted gameplay deviations.

## Battle Lab Group Suite Result

Fresh run command:

```powershell
cd rebuild_game
.\build.ps1
$suites=@(
  'fire_skills_0_9',
  'wood_skills_10_19',
  'earth_skills_20_29',
  'water_skills_30_39',
  'electric_skills_40_49',
  'shadow_skills_50_59',
  'wind_skills_60_69'
)
foreach($suite in $suites){
  .\run_battle_lab_suite_smoke.cmd -Lane npc -Suite $suite -NoBuild
}
```

| Group | Suite | Result | PNG root |
| --- | --- | --- | --- |
| Skill 0..9 | `fire_skills_0_9` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/fire_skills_0_9` |
| Skill 10..19 | `wood_skills_10_19` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/wood_skills_10_19` |
| Skill 20..29 | `earth_skills_20_29` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/earth_skills_20_29` |
| Skill 30..39 | `water_skills_30_39` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/water_skills_30_39` |
| Skill 40..49 | `electric_skills_40_49` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/electric_skills_40_49` |
| Skill 50..59 | `shadow_skills_50_59` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/shadow_skills_50_59` |
| Skill 60..69 | `wind_skills_60_69` | PASS 10/10 | `rebuild_game/build_intro_demo/battle_lab_suites/npc/wind_skills_60_69` |

These group suites mainly prove Battle Lab selection/list integration and that
each skill is reachable/renderable as a manual test scenario.

## Focused Closeout Suite Result

Fresh run command:

```powershell
cd rebuild_game
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_earth_skills_25_29_closeout build_intro_demo\skill_0_69_closeout\earth25_29
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_water_skills_30_39_closeout build_intro_demo\skill_0_69_closeout\water
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_electric_skills_40_49_closeout build_intro_demo\skill_0_69_closeout\electric
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_shadow_skills_50_59_closeout build_intro_demo\skill_0_69_closeout\shadow
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_wind_skills_60_69_closeout build_intro_demo\skill_0_69_closeout\wind
```

| Focus | Suite | Result | Key asserted data |
| --- | --- | --- | --- |
| Earth 25..29 | `battle_earth_skills_25_29_closeout` | PASS | skill25 buff14, skill27 buff4, skill28 debuff1, skill29 preloaded debuff1 |
| Water 30..39 | `battle_water_skills_30_39_closeout` | PASS | debuff4/5/6 producers, buff5, buff6, direct water damage |
| Electric 40..49 | `battle_electric_skills_40_49_closeout` | PASS | debuff10, buff7, buff8, buff9, direct electric damage |
| Shadow 50..59 | `battle_shadow_skills_50_59_closeout` | PASS | debuff7, skill52/58 leech heal, skill54 debuff8, skill55 debuff9 |
| Wind 60..69 | `battle_wind_skills_60_69_closeout` | PASS | debuff5, buff10, skill63/69 follow-up, skill64 buff steal, skill65 buff12, skill67 source oddity |

Fire 0..9 and Wood 10..19 currently do not have the same one-shot focused
closeout suite shape. They are covered by their group Battle Lab suites and
their source-row/effect-row audit docs. This is an honest coverage asymmetry,
not a hidden failure.

## Skill Group Status

| Range | Family | Current status | Controlling docs |
| --- | --- | --- | --- |
| 0..9 | Fire | `PORTED/PARTIAL + SMOKE-COVERED` | `355_battle_fire_skill_effect_mid_s60_reaudit.md` |
| 10..19 | Wood | `PORTED/PARTIAL + SMOKE-COVERED` | `356_battle_wood_skill_10_19_s60_reaudit_and_battle_lab.md`, `357_battle_p7_attacker_action_before_target_effect_audit.md` |
| 20..29 | Earth | `PORTED/PARTIAL + SMOKE-COVERED` | `358_battle_earth_skill_20_29_s60_reaudit_and_battle_lab.md` |
| 30..39 | Water | `PORTED/PARTIAL + SMOKE-COVERED` | `359_battle_water_skill_30_39_s60_reaudit_and_battle_lab.md` |
| 40..49 | Electric | `PORTED/PARTIAL + SMOKE-COVERED` | `360_battle_electric_skill_40_49_s60_reaudit_and_battle_lab.md` |
| 50..59 | Shadow | `PORTED/PARTIAL + SMOKE-COVERED` | `361_battle_shadow_skill_50_59_s60_reaudit_and_battle_lab.md` |
| 60..69 | Wind | `PORTED/PARTIAL + SMOKE-COVERED` | `362_battle_wind_skill_60_69_s60_reaudit_and_battle_lab.md` |

## Important Exceptions And Non-Source Decisions

| Item | Classification | Closeout note |
| --- | --- | --- |
| Pixel-perfect animation timing | `PENDING` | PNG smoke proves visible runtime behavior, not exact original-client frame parity. |
| Original RNG full-game stream | `PENDING` | Trace tooling exists, but global boot/world-to-battle RNG parity is not claimed. |
| Skill 67 | `SOURCE_ODDITY` | S60 row advertises `effectMode=2,effectId=5,chance=5`, but bytecode-backed runtime treats it as raw/default damage and applies no debuff5. Do not patch without new executable source proof. |
| Buff6 Kien Nhan | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` | User-approved mechanic: when hit, 50% chance to reduce incoming damage by 50%. |
| Buff10 Man Luc | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` | User-approved mechanic: 3 turns of outgoing damage boost, 15%, then 10%, then 5%, then expires. |
| Debuff8 Quy Mi | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` | User-approved mechanic: target gains 10% damage but has high random-route risk, 55% self-hit / 45% enemy-hit in 1v1. |
| Debuff9 Hon Loan | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` | User-approved mechanic: 1v1 random target route, self or enemy. Switch is allowed; only Quan Quanh blocks switch. |

## Verification Run

Run after skill closeout:

```powershell
cd rebuild_game
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo\battle_quick_after_skill_0_69_closeout
git -C .. diff --check -- rebuild_game/src/main/java rebuild_plan
rg -n "<current mojibake marker pattern>" src/main/java
```

Result:

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| `battle_quick` | PASS 227/227 |
| `git diff --check` | PASS; only line-ending warnings |
| Java mojibake scan | PASS |

Note: `??` is intentionally used in some rebuild dialogue paths as unknown
speaker text, so it is not treated as mojibake in this closeout.

## Remaining Gap Matrix

| Gap | Status | Why it remains |
| --- | --- | --- |
| Original-vs-rebuild frame compare for every skill | `PENDING` | Requires original-client capture for each relevant route/frame window. |
| Exact MIDP draw order and sub-frame cursor timing | `PORTED/PARTIAL` | Current runtime follows source rows/effect rows and PNG checkpoints, but not every frame has original compare. |
| Rare route interactions across multi-active parties | `PENDING` | Current Battle Lab is mostly controlled 1v1 / route-local. |
| Fire/Wood dedicated one-shot closeout suite parity | `PENDING` | Coverage exists via group suites and docs, but focused suite shape is not symmetric with 25..69. |
| Full source parity for intentional gameplay fixes | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` | These mechanics intentionally follow user-approved PC rebuild gameplay instead of opaque or undesirable source behavior. |

## Closeout Decision

Broad skill coverage is closed. Do not keep reopening `0..69` as a whole unless
one of these happens:

1. Manual testing finds a concrete skill mismatch.
2. Original-client capture proves a visual/timing mismatch.
3. New source proof changes the classification of a specific skill/effect row.

The next skill/effect work should be narrow and evidence-driven:

- original-vs-rebuild frame compare for a named skill, or
- a missing item/skill interaction discovered through Battle Lab, or
- remaining item runtime completion if the roadmap moves away from skill visuals.

## Next Recommended Step

Move to the next roadmap layer instead of dragging the whole skill phase:

1. If staying in battle skills: build an original-vs-rebuild frame compare
   harness for one named skill mismatch at a time.
2. If moving forward on gameplay: continue item full completion from
   `227_battle_item_full_completion_matrix.md`.
