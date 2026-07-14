# 317 - Battle Lab All Skill Test List Closeout

Status: `PORTED/PARTIAL`

Scope: add a Battle Lab checkpoint that lets manual testers open a battle skill list containing every source skill id `0..69`, then pick a skill to see its real P3/P7 battle animation/effect path. This is a lab-only entrypoint and does not alter normal gameplay routes.

## What Changed

| Area | Change | Status |
| --- | --- | --- |
| Live lab checkpoint | Added `battle_lab_skill_test_all` to `setupLiveCheckpoint(...)`. | `PORTED` |
| Skill list | In lab mode, P3 `choiceskill.ui` lists all `aq.c[1]` skill ids `0..69`, with source names and PP labels. | `PORTED` |
| Skill execution | When a lab skill is confirmed, runtime temporarily installs the selected skill into player slot `0`, then uses normal P3/P6/P7 flow. | `PORTED/PARTIAL` |
| Smoke checkpoint | `--smoke-checkpoint battle_lab_skill_test_all` verifies P3, `choiceskill.ui`, 70 skill ids, source names/PP labels, and scroll behavior. | `PORTED` |

## Commands

Manual Battle Lab:

```text
java -cp build\classes VqsvIntroDemo --play-checkpoint battle_lab_skill_test_all
```

Smoke PNG:

```text
java -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_lab_skill_test_all build\smoke\battle_lab_skill_test_all.png
```

## Test Flow

1. Open the checkpoint.
2. The battle starts directly in skill list state `P3`.
3. Use up/down or mouse wheel to browse all skills.
4. Click/confirm a skill to run the normal battle flow:
   - P3 skill list
   - P6 target select if source formation requires it
   - P7 animation/effect/damage
5. Reopen the checkpoint when you want a clean reset.

## Notes

- Player and enemy are both seeded at level `50` for a more useful lab battle than story-level values.
- The lab does not claim every skill is complete. It exposes every skill through the current runtime so visual/effect gaps can be inspected quickly.
- Strong skills can still end the battle; a future lab slice can add no-KO/reset-HP/no-enemy-turn controls if manual testing needs a longer sandbox loop.
- This checkpoint is intentionally lab-gated and should not be used as evidence that a skill is fully ported. Full skill closeout still requires source audit, debug numbers, and dedicated smoke PNGs.

## Verification

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_lab_skill_test_all build\smoke\battle_lab_skill_test_all.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|�|Há»|DÆ|Ä" src\main\java
```

Result: build/check/formula/lab smoke passed. Mojibake scan returned no matches.

## Next

Use the lab to inspect the next lane starting from skill `10`, but keep the closeout process unchanged: source row, effect row, logic, debug before/during/after, and dedicated smoke PNGs.
