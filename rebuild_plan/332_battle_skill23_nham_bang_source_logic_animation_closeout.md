# 332 - Battle Skill 23 Nham Bang Source / Logic / Animation Closeout

Scope: Earth Lane skill `23` only. Timeline code lives in:

```text
rebuild_game/src/main/java/EarthSkill.java
```

No live client was opened. This closeout uses deterministic smoke PNGs only.

## Source Facts

| Item | Source-backed value |
| --- | --- |
| Skill id | `23` |
| Name | `Nham bang` / `Nham băng` |
| Source row | `aq.c[1][23] = [2,140,552,100,1,30,0,-1,250,0]` |
| Lane | Earth, `elementFamily=2` |
| PP | `30` |
| Baseline damage | direct damage, `powerPercent=100` |
| Conditional damage | if target has debuff1 `Me Muoi`, use param `250` |
| Status producer | none; `effectMode=0`, `effectId=-1` |
| Target | enemy side, target mode `0` |
| P7 effect row | `effect.mid[23] = [0,0,22,0,-1,-1,0, 0,1,6,0,-1,-1,0]` |
| Actor visual | actor effect `22`, sprite `264`, state `0`, enemy side |
| Special visual | `speffect6`, AH type `8`, target side |
| Condition row | debuff1 `aq.c[7][1] = [312,323,2]` |
| Debuff1 icon | icon cell `2`, duration cell `136` when preloaded with duration `2` |

## Runtime Timeline

Two deterministic runs are executed in one suite:

| Run | Setup | Expected behavior | Result |
| --- | --- | --- | --- |
| Baseline | enemy has no debuff1 | actor `22->264`, `speffect6/AH8`, damage by `raw*100/100`, no debuff apply | PORTED |
| Conditional | enemy starts with debuff1 `Me Muoi` | same visual chain, damage branch uses `raw*250/100`, no new debuff apply | PORTED |

The conditional branch keeps debuff1 visible as the prerequisite. Skill 23 does
not apply or refresh debuff1; it only reads the target status for damage.

## Measured Smoke Numbers

Same seed, forced hit, forced non-crit:

```text
Baseline:
  Before:  player HP 134/134, enemy HP 109/109, PP 30, no debuff1
  Visual:  actor sprite 264, then speffect6 AH type8
  Damage:  25
  After:   enemy HP 84/109, PP 29

Conditional:
  Before:  player HP 134/134, enemy HP 109/109, PP 30, debuff1 icon 2/136
  Visual:  actor sprite 264, then speffect6 AH type8
  Damage:  60
  After:   enemy HP 49/109, PP 29
```

Both runs assert:

```text
hit=true
appliedDebuffId=-1
sideEffectsCommitted=true
```

## Smoke PNGs

```text
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_baseline_no_debuff1_before.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_baseline_no_debuff1_actor_u22_start.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_baseline_no_debuff1_speffect6_type8.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_baseline_no_debuff1_damage_frame.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_baseline_no_debuff1_hp_settled.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_conditional_debuff1_before.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_conditional_debuff1_actor_u22_start.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_conditional_debuff1_speffect6_type8.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_conditional_debuff1_damage_frame.png
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_conditional_debuff1_hp_settled.png
```

Debug file:

```text
rebuild_game/build/smoke/battle_skill23_nham_bang_timeline/battle_skill23_nham_bang_timeline_debug.txt
```

## Battle Lab

Skill `23` is available through the table-driven all-skill battle lab path.
No per-skill lab branch was added; the automated timeline suite lives in
`EarthSkill`.

## Classification

```text
Skill23 source row audit: PORTED
P7 actor row 22 / sprite 264: PORTED/PARTIAL
P7 speffect6 AH type8: PORTED/PARTIAL
Baseline damage branch raw*100/100: PORTED
Conditional debuff1 damage branch raw*250/100: PORTED
No status producer / appliedDebuffId=-1: PORTED
Exact MIDP pixel/timing parity: PENDING
```

`PORTED/PARTIAL` remains for visual/timing because source rows and rebuild
frames are proven, but no original MIDP frame compare was performed.

## Verification

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill23_nham_bang_timeline build\smoke\battle_skill23_nham_bang_timeline
```

Focused result:

```text
smoke-suite-ok battle_skill23_nham_bang_timeline checkpoints=1
baselineDamage=25
conditionalDamage=60
```

## Next Step

Continue Earth Lane with skill `24` / `Nguoi bao ve Dia Gioi`. That skill is a
no-damage cleanse + heal-over-time buff path, so it should audit `aq.c[1][24]`,
buff13, clear-debuff behavior, heal amount, and producer visual before code.
