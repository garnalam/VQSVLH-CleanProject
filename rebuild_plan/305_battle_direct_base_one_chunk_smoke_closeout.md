# 305 - Battle Direct Base One-Chunk Smoke Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closeout follows `304_new_dev_chat_handoff_battle_skill_effect_current.md`.
It extends the skill10 direct-base timing template to the remaining simple
one-chunk representative direct-base skills:

```text
0, 20, 30, 40, 60
```

Skill `50` is intentionally not included because its source row has two chunks:
actor `25` plus `speffect9/AH type9`. It must remain a separate audit/code
slice.

## Source Facts

All covered skills share this source shape:

```text
aq.c[1][skill] power=100, pp=45, effectMode=0, effectId=-1, param=-1, targetSide=0
effect.mid[skill] = [0,0,actorEffectId,state,-1,-1,0]
```

| Skill | Source role | `effect.mid` row | Actor sprite | Status |
| ---: | --- | --- | ---: | --- |
| 0 | Fire base direct | `[0,0,20,0,-1,-1,0]` | `262` | PORTED-PARTIAL |
| 20 | Earth base direct | `[0,0,22,0,-1,-1,0]` | `264` | PORTED-PARTIAL |
| 30 | Water base direct | `[0,0,23,0,-1,-1,0]` | `265` | PORTED-PARTIAL |
| 40 | Electric base direct | `[0,0,24,0,-1,-1,0]` | `266` | PORTED-PARTIAL |
| 60 | Wind base direct | `[0,0,26,0,-1,-1,0]` | `268` | PORTED-PARTIAL |

The actor sprite mapping is source-backed from `ah.java`:

```text
20->262, 21->263, 22->264, 23->265, 24->266, 25->267, 26->268
```

## Code Change

Changed:

```text
rebuild_game/src/main/java/VqsvSmokeHarness.java
```

Added:

```text
--smoke-suite battle_direct_base_one_chunk
```

The suite uses the same timing contract as skill10:

```text
<skill>_before
<skill>_actor_uXX_start
<skill>_damage_frame
<skill>_hp_settled
```

Important rule preserved:

```text
damage_frame != hp_settled
```

The damage text can be visible while the HUD HP still shows the old value. Each
`hp_settled` checkpoint waits until the HP display reaches the damage value
from that same run.

## Smoke Output

Command:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_direct_base_one_chunk .\build_intro_demo\battle_direct_base_one_chunk
```

Result:

```text
PASS, 20/20 checkpoints
```

Output directory:

```text
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/
```

Representative PNGs:

```text
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/battle_skill0_direct_before.png
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/battle_skill0_direct_actor_u20_start.png
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/battle_skill0_direct_damage_frame.png
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/battle_skill0_direct_hp_settled.png
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/battle_skill60_direct_actor_u26_start.png
rebuild_game/build_intro_demo/battle_direct_base_one_chunk/battle_skill60_direct_hp_settled.png
```

## Verification

Passed:

```text
rebuild_game/build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
--smoke-suite battle_skill10_direct_animation
--smoke-suite battle_direct_base_one_chunk
--smoke-suite battle_quick
```

`battle_quick` result:

```text
PASS, 227/227 checkpoints
```

Notes:

- `git diff --check` could not run because this workspace is not currently a
  Git repository.
- Java mojibake scan still reports existing legitimate UTF-8 Vietnamese source
  strings. This slice added ASCII-only Java strings.

## Classification

| Area | Status | Notes |
| --- | --- | --- |
| One-chunk direct-base source rows `0/20/30/40/60` | PORTED | Raw skill/effect rows are asserted by smoke. |
| Actor effect id / sprite / state metadata | PORTED | Smoke asserts actor id, sprite, state, side, PP, and no early HP drop. |
| Damage frame vs HP-settled timing | PORTED | Separate checkpoints exist for every covered skill. |
| Damage formula itself | PORTED-PARTIAL | Existing formula path is reused; this slice is animation/timing coverage. |
| Exact original MIDP pixel parity | PENDING | No original-vs-rebuild frame comparison in this slice. |

## Next Roadmap Step

Next recommended slice:

```text
Audit skill50 as its own two-chunk direct-base visual case:
effect.mid[50] = [0,0,25,0,-1,-1,0, 0,1,9,0,-1,-1,0]
```

Required checkpoints should separate:

```text
skill50_before
skill50_actor_u25_start
skill50_ah_type9_overlay
skill50_damage_frame
skill50_hp_settled
```

Do not merge skill50 into the one-chunk suite.
