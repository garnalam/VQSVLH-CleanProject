# 160 Battle Phase 9-D Post-Hit Siblings And Miss Interactions

Status date: 2026-07-09

Status: PHASE 9-D / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Close the already-ported `game.d.q()` sibling gaps before moving to broader
  debuff families.
- Cover hit and miss interactions for hardcoded heal, leech, and follow-up
  branches.
- Keep this slice limited to battle P7 runtime and smoke checkpoints.

## Source Facts

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`

Relevant `aq.c[1]` rows from `db.mid` group 1:

| Skill | Row | Meaning used in this slice |
| --- | --- | --- |
| `11` | `[1,128,540,90,0,45,0,-1,10,0]` | Direct damage, `game.d.q()` heals attacker by base attack `* 10 / 100`. |
| `17` | `[1,134,546,130,2,30,0,-1,40,0]` | Direct damage sibling, `game.d.q()` heals attacker by base attack `* 40 / 100`. |
| `52` | `[5,169,581,80,0,45,0,-1,5,0]` | Direct damage sibling, `game.d` sets `aa` with 30% gate, `q()` leeches `Z[0] * 5 / 100`. |
| `58` | `[5,175,587,100,3,15,0,-1,8,0]` | Direct damage sibling, `game.d` sets `aa` with 30% gate, `q()` leeches `Z[0] * 8 / 100`. |
| `63` | `[6,180,592,100,1,30,0,-1,5,0]` | Direct damage, `q()` may route to P2 follow-up with chance `5`. |
| `69` | `[6,186,598,150,3,15,0,-1,8,0]` | Direct damage sibling, `q()` may route to P2 follow-up with chance `8`. |

Source `game.d` facts:

| Area | Source behavior | Rebuild status |
| --- | --- | --- |
| P7 miss gate | `game.b.b(target)` produces `Z[]`, then `game.d` rolls miss. Miss does not apply HP damage and shows `Né tránh`. | PORTED/PARTIAL from Phase 9-A. |
| `q()` execution | After P7 HP/tween wait, source calls `q()` regardless of whether the previous damage frame hit or missed. | PORTED/PARTIAL. |
| `11/17` | `q()` heals attacker by `h.B() * skill[8] / 100`, min 1. No hit check in `q()`. | PORTED/PARTIAL. |
| `52/58` | P7 sets `aa=false` only when `ae.a(100) > 30`; `q()` checks `aa`, not the hit flag. | PORTED/PARTIAL. Rebuild removed the previous incorrect `p7AttackHit` gate. |
| `63/69` | Follow-up roll is evaluated after `q()` and can route back to P2 if target survives. No hit check is present in this branch. | PORTED/PARTIAL. |

Important note:

- Skill `52` can pass the source `aa` gate while its heal display rounds to
  zero (`Z[0] * 5 / 100 == 0`). In that case there is no visible `+` text, but
  the source branch is still considered covered by trace. Skill `58` is used
  for a visible leech text smoke.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only deterministic rolls for leech/follow-up. Changed `52/58` leech to use source `aa` gate without `p7AttackHit` gating. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-D smoke checkpoints for hit siblings and miss interactions. |

New smoke-only helpers:

| Helper | Purpose |
| --- | --- |
| `debugSetNextLeechRollForSmoke(int)` | Deterministically force the source `aa` roll for `52/58`. |
| `debugSetNextFollowUpRollForSmoke(int)` | Deterministically force the source follow-up roll for `63/69`. |

These helpers are debug-only; normal runtime still uses `VqsvSourceRandom`.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9d/
```

| Checkpoint | Source behavior asserted | Result |
| --- | --- | --- |
| `battle_phase9d_hit_heal_skill_17` | Skill `17` mirrors skill `11`: hit path reaches `q()` heal and shows `+`. | PASS |
| `battle_phase9d_hit_leech_skill_52` | Skill `52` passes source `aa` gate on hit; visible `+` is not required if heal rounds to 0. | PASS |
| `battle_phase9d_hit_followup_skill_69` | Skill `69` mirrors skill `63`: hit path can route back to P2. | PASS |
| `battle_phase9d_miss_heal_skill_11` | Skill `11` miss still reaches source `q()` heal. | PASS |
| `battle_phase9d_miss_heal_skill_17` | Skill `17` miss still reaches source `q()` heal. | PASS |
| `battle_phase9d_miss_leech_skill_52` | Skill `52` miss still evaluates source `aa`; visible `+` may round to 0. | PASS |
| `battle_phase9d_miss_leech_skill_58` | Skill `58` miss still evaluates source `aa` and shows visible `+`. | PASS |
| `battle_phase9d_miss_followup_skill_63` | Skill `63` miss can still route to P2 follow-up. | PASS |
| `battle_phase9d_miss_followup_skill_69` | Skill `69` miss can still route to P2 follow-up. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9d checkpoint> build\smoke\phase9d\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-D smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| `battle_elder_p7_q_heal_skill11` | PASS |
| `battle_elder_p7_q_leech_skill58` | PASS |
| `battle_p7_q_skill63_followup_p2` | PASS |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

## Current Status

| Area | Status |
| --- | --- |
| Skill `17` sibling of `11` | PORTED/PARTIAL, hit and miss smoke-covered. |
| Skill `52` sibling of `58` | PORTED/PARTIAL, hit and miss smoke-covered; visible heal may round to 0. |
| Skill `69` sibling of `63` | PORTED/PARTIAL, hit and miss smoke-covered. |
| `52/58` source `aa` gate | PORTED/PARTIAL; no longer incorrectly gated by `p7AttackHit`. |
| Exact RNG stream parity | PARTIAL. Debug-only roll hooks are used for deterministic smoke. |
| Pixel-perfect P7 visuals | PENDING outside this slice. |
| Passive hook 4 in miss chance | PENDING from Phase 9-A. |

## Next Roadmap Step

Continue Phase 9 with debuff-family coverage by debuff id:

```text
Phase 9-E: direct explicit/implicit debuff families.
```

Recommended first slice:

1. Audit debuff id `1` family: skills `2/8/22/28`.
2. Prove source rows, debuff chance gate, anti-debuff status 3, buff14 block,
   and P12 lifecycle.
3. Add deterministic smoke for hit+debuff success, hit+debuff blocked, and
   miss interaction.
4. Source audit must decide whether miss suppresses only P7 text or also the
   debuff producer state. Do not assume queue suppression.

Do not start `SOURCE_SWITCH_GAP` skills yet.
