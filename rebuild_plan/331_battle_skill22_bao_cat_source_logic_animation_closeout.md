# 331 - Battle Skill 22 Bao Cat Source / Logic / Animation Closeout

Scope: Earth Lane skill `22` only. Timeline code lives in:

```text
rebuild_game/src/main/java/EarthSkill.java
```

This continues Earth Lane after skill `20` / `Hat Bui` and skill `21` /
`Tho thuan`. No live client was opened; verification is smoke PNG/headless only.

## Source Facts

| Item | Source-backed value |
| --- | --- |
| Skill id | `22` |
| Name | `Bao cat` / `Bão cát` |
| Source row | `aq.c[1][22] = [2,139,551,50,0,45,2,1,25,0]` |
| Lane | Earth, `elementFamily=2` |
| PP | `45` |
| Damage | direct damage, `powerPercent=50` |
| Status producer | `effectMode=2`, `effectId=1` |
| Debuff chance | `25%`, from skill column `[8]` |
| Target | enemy side, target mode `0` |
| P7 effect row | `effect.mid[22] = [0,0,22,0,-1,-1,0]` |
| Actor visual | actor effect `22`, sprite `264`, state `0`, enemy side |
| P7 special | none for skill 22 itself |
| Debuff row | `aq.c[7][1] = [312,323,2]` |
| Debuff name | `Me Muoi` / `Mê Muội` |
| Debuff duration | `2` |
| Debuff value | `0`, flag/status only |
| HUD icon | debuff id + 1 = icon cell `2` |
| Duration cells | `134 + duration`, so `136 -> 135 -> clear` |
| P12/P13 body visual | `bufDebuf aq[1] = [1,14,0,-1]`, `speffect14`, AH type `12` |

## Runtime Timeline

| Stage | What the smoke proves | Result |
| --- | --- | --- |
| Before confirm | P3 skill list has skill `22`, PP full, no enemy debuff1 | PORTED |
| Actor frame | P7 starts actor action `22->264`, no damage yet, no P7 special | PORTED |
| Damage/debuff frame | forced hit, forced non-crit, forced debuff roll `0`; damage and debuff commit together | PORTED |
| HP settled | enemy HP is reduced, PP consumed once, debuff icon `2/136` visible | PORTED |
| P12 body visual | active queue starts debuff1 body visual via `speffect14` AH type `12` | PORTED/PARTIAL |
| Tick | debuff1 has no HP/stat tick; duration decrements `2->1` | PORTED |
| Expiry | next source tick clears debuff1 and icon | PORTED |

## Measured Smoke Numbers

Single deterministic run:

```text
Before:      player HP 134/134, enemy HP 109/109, PP 45
Actor:       enemy HP still 109/109, PP 44, actor sprite 264
Damage:      -13, enemy HP display still 109/109, crit=false, debuffText=Me Muoi
Settled:     enemy HP 96/109, PP 44, icon=2, durationCell=136
P12 tick:    enemy HP 96->96, duration 2->1, durationCell=135
Expired:     duration 0, enemy statusCount 0
```

The damage in this smoke comes from the current source-shaped formula path:
`raw * 50 / 100`, with deterministic forced hit/non-crit and a source RNG jitter.

## Smoke PNGs

```text
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_before.png
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_actor_u22_start.png
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_damage_debuff_frame.png
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_hp_settled_debuff_active.png
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_p12_body_visual_type12.png
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_tick_noop_duration1.png
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_expired.png
```

Debug file:

```text
rebuild_game/build/smoke/battle_skill22_bao_cat_timeline/battle_skill22_bao_cat_timeline_debug.txt
```

## Battle Lab

Skill `22` is available through the existing table-driven all-skill battle lab
path. No per-skill battle-lab branch was added; the dedicated one-run timeline
suite lives in `EarthSkill`.

## Classification

```text
Skill22 source row audit: PORTED
P7 actor row 22 / sprite 264: PORTED/PARTIAL
Direct damage + forced hit/non-crit/debuff commit: PORTED
Debuff1 duration/icon/value/sourceSkill: PORTED
P12/P13 speffect14 AH type12 body visual: PORTED/PARTIAL
No-op tick and expiry clear: PORTED
Exact MIDP pixel/timing parity: PENDING
```

`PORTED/PARTIAL` is used for visual/timing because this smoke proves source
rows and rebuild frame states, but does not compare against an original MIDP
frame capture.

## Verification

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill22_bao_cat_timeline build\smoke\battle_skill22_bao_cat_timeline
```

Latest focused result:

```text
smoke-suite-ok battle_skill22_bao_cat_timeline checkpoints=1
```

## Next Step

Continue Earth Lane in order with skill `23` / `Nham bang`. That skill is not a
plain debuff producer: it is conditional damage that becomes stronger when the
target already has debuff1 `Me Muoi`, so the next slice should include baseline
versus preloaded-debuff1 comparison and its `speffect6/AH8` visual.
