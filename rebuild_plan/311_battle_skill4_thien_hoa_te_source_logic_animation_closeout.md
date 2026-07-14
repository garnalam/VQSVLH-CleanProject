# 311 - Battle Skill 4 Thien Hoa te Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `4`. No live client was opened; verification used smoke PNG only.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][4]` | `[0,121,533,0,1,10,1,0,-1,1]` | Fire skill, no direct damage, learn tier `1`, PP `10`, effectMode `1`, buff/effect id `0`, self/ally target side `1`. |
| `effect.mid[4]` | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | Two special-effect chunks: `speffect16`, then `speffect15`. |
| `speffect[16]` | `[9,150,181,37,84,0,5,5]` | AH type `9` overlay, currently source-shaped. |
| `speffect[15]` | `[1,0,5,3,0,0]` | AH type `1` overlay, currently source-shaped. |
| `aq.c[6][0]` | `[333,348,2,30,190]` | Buff0 `Suc Luc`: duration `2`, defense `+30%`, stored extra damage `190% * B()` snapshot. |
| `game.b.a(byte,int,int)` | buff case `0` | Applies `v[0][1] = baseDefense * 30 / 100`, `v[0][2] = 190 * B() / 100`, then raises current defense. |
| `game.b.b(target)` | `m(0) && v[0][0] == 0` | Duration-edge source hook adds stored `v[0][2]` to outgoing damage. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(4)` | `PORTED` |
| Buff row parse | `BattleBuffRow` via `VqsvBattleTables.buff(0)` | `PORTED` |
| P7 no-damage path | `game.d.q postEffect skill=4`, no hitroll, no damage frame | `PORTED` |
| Animation chunk 0 | `speffect16` / AH type `9` | `PORTED/PARTIAL`; no MIDP pixel compare claim |
| Animation chunk 1 | `speffect15` / AH type `1` | `PORTED/PARTIAL`; no MIDP pixel compare claim |
| Buff active logic | defense `100 -> 130`, icon `12`, duration cell `136` | `PORTED` |
| Stored extra | Producer self-target snapshot stores `38`; forced hook probe stores `228` | `PORTED`; source-shaped numeric coverage |
| Expiry | duration `2 -> 1 -> 0`, defense returns to base, icon clears | `PORTED` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill4_thien_hoa_te_timeline build\smoke\battle_skill4_thien_hoa_te_timeline
```

Output summary:

```text
Before:
  HP 134/134 : 109/109
  PP 10
  defense 100

During:
  speffect16 AH type 9 visible
  speffect15 AH type 1 visible

After apply:
  HP unchanged 134/134 : 109/109
  PP 10 -> 9
  defense 100 -> 130
  buff0 value 30
  producer storedExtra 38
  duration 2
  icon cell 12, duration cell 136

Hook probe:
  baseline damage 80
  duration-edge hooked damage 308
  hook storedExtra 228

Expiry:
  duration 2 -> 1 -> 0
  defense 130 -> 130 -> 100
  buff icon clears
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_before.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_speffect16_type9.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_speffect15_type1.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_after_apply_icon.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_baseline_no_buff0_hook_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_hook_duration0_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_expiry_before_tick.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_expiry_after_first_tick.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_expiry_after_clear.png`
- `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_visual_compare_zoom.png`
- Debug text: `rebuild_game/build/smoke/battle_skill4_thien_hoa_te_timeline/battle_skill4_thien_hoa_te_timeline_debug.txt`

## Notes

- Skill `4` does not damage and does not run hit/miss/crit. Its key behavior is P7 special effect plus `game.d.q()` applying buff0.
- Producer `storedExtra=38` in the focused run is expected because skill4 targets self in that setup, so the raw snapshot is `20`, and `20 * 190 / 100 = 38`.
- The separate duration-edge hook probe sets a controlled outgoing attack/target-defense setup, producing raw `120`, storedExtra `228`, and damage `80 + 228 = 308`.
- No pixel-perfect MIDP parity is claimed for AH type `9` / type `1` timing or alpha.

## Next Roadmap Step

Continue sequential fire lane with skill `5` (`Viem loi pha`): no-damage buff1 producer. It needs visual `speffect16 -> speffect15`, defense `-50%`, outgoing damage `+50%`, forced hit/miss/crit assertions, and expiry.
