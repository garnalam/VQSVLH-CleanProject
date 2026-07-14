# 333 - Battle Skill 24 Nguoi Bao Ve Dia Gioi Source / Logic / Animation Closeout

Scope: Earth Lane skill `24` only. Timeline code lives in:

```text
rebuild_game/src/main/java/EarthSkill.java
```

No live client was opened. This closeout uses deterministic smoke PNGs only.

## Source Facts

| Item | Source-backed value |
| --- | --- |
| Skill id | `24` |
| Name | `Nguoi bao ve Dia Gioi` / `Người bảo vệ Địa Giới` |
| Source row | `aq.c[1][24] = [2,141,553,0,1,10,1,13,-1,1]` |
| Lane | Earth, `elementFamily=2` |
| PP | `10` |
| Damage | none, `power=0` |
| Effect route | `effectMode=1`, `effectId=13` |
| Target | same side/self, target mode `1` |
| P7 effect row | `effect.mid[24] = [0,0,22,0,-1,-1,0, 0,1,17,0,-1,-1,0]` |
| Actor visual | actor effect `22`, sprite `264`, state `0`, player side |
| Special visual | `speffect17 = [1,0,7,1,0,0]`, AH type `1`, player side |
| Buff row | `aq.c[6][13] = [346,361,3,20,-1]` |
| Buff name | `Thach Hoa` / `Thạch Hóa` |
| Buff duration | `3` |
| Heal amount | `maxHp * 20 / 100` on apply and each active tick |
| Cleanse | clears all player debuffs through source-shaped `C()` behavior |
| HUD icon | buff id + 12 = icon cell `25` |
| Duration cells | `137 -> 136 -> 135 -> clear` |
| P13 body visual | active queue visual row `ap id=13 [1,17,0,-1]`, AH type `1` |

## Runtime Timeline

The smoke starts with the player wounded and carrying debuff5. Skill24 then
uses a no-damage P7 route:

| Stage | What the smoke proves | Result |
| --- | --- | --- |
| Before | player HP is low, debuff5 active, no buff13 | PORTED |
| Actor frame | P7 starts actor `22->264` on player side; no damage yet | PORTED |
| Special frame | P7 starts `speffect17/AH1` on player side; still no damage | PORTED/PARTIAL |
| Apply | `game.d.q()` applies buff13, heals, clears debuff5, consumes PP once | PORTED |
| P13 body visual | active queue runs buff13 visual row `ap id=13` / `speffect17` | PORTED/PARTIAL |
| P13 tick | buff13 heals again and duration decrements `3->2` | PORTED |
| Expiry | remaining ticks decrement `2->1->0`, icon clears, debuff stays cleared | PORTED |

## Measured Smoke Numbers

Single deterministic run:

```text
Before:
  Player HP 67/134
  Enemy HP 109/109
  PP 10
  Debuff5 active

Apply:
  Heal = 134 * 20 / 100 = 26
  Player HP 67 -> 93
  Enemy HP 109 -> 109
  PP 10 -> 9
  Debuff5 cleared
  Buff13 value 26, duration 3
  Icon 25, durationCell 137

P13 active tick:
  Runtime enemy turn happens between apply and P13 in this route, so HP is 88 before the tick.
  Player HP 88 -> 114
  Duration 3 -> 2
  Icon 25, durationCell 136

Expiry:
  Duration after next debug tick: 1
  Final duration: 0
  Status count: 0
```

Important: skill24 itself never damages the enemy and never rolls hit/miss.
The smoke asserts no `battle P7 damage frame skill=24` and no
`battle P7 hitroll skill=24`.

## Smoke PNGs

```text
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_before_wounded_debuff5.png
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_actor_u22_start.png
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_speffect17_type1.png
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_after_apply_cleanse_heal.png
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_p13_body_visual_type1.png
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_p13_heal_tick_duration2.png
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_expired.png
```

Debug file:

```text
rebuild_game/build/smoke/battle_skill24_nguoi_bao_ve_dia_gioi_timeline/battle_skill24_nguoi_bao_ve_dia_gioi_timeline_debug.txt
```

## Battle Lab

Skill `24` is available through the table-driven all-skill battle lab path.
No per-skill lab branch was added; the automated timeline suite lives in
`EarthSkill`.

## Classification

```text
Skill24 source row audit: PORTED
P7 actor row 22 / sprite 264 player-side: PORTED/PARTIAL
P7 speffect17 AH type1: PORTED/PARTIAL
No-damage route / no hitroll: PORTED
Buff13 apply heal 20% maxHP: PORTED
Debuff cleanse: PORTED
Buff13 P13 body visual and tick heal: PORTED/PARTIAL
Buff13 expiry/icon clear: PORTED
Exact MIDP pixel/timing parity: PENDING
```

`PORTED/PARTIAL` remains for visual/timing because source rows and rebuild
frames are proven, but no original MIDP frame compare was performed.

## Verification

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill24_nguoi_bao_ve_dia_gioi_timeline build\smoke\battle_skill24_nguoi_bao_ve_dia_gioi_timeline
```

Focused result:

```text
smoke-suite-ok battle_skill24_nguoi_bao_ve_dia_gioi_timeline checkpoints=1
hp=67/134->93->114
enemyHp=109->109
pp=10->9
heal=26
debuff5Cleared=true
buff13Expired=true
```

## Next Step

Continue Earth Lane with skill `25` / `Thach phu thuat`. That skill is also a
no-damage cleanse route, but it applies buff14 protection instead of heal-over-
time. It must audit/verify `speffect4/AH7`, `speffect17/AH1`, debuff clear, and
the later debuff-block hook.
