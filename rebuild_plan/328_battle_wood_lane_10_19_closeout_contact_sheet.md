# 328 - Battle Wood Lane 10..19 Closeout Contact Sheet

Date: 2026-07-14

Status: WOOD LANE CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes the Wood Lane skill pass for skills `10..19` at the current
source-shaped standard. Exact original MIDP pixel/frame comparison is still not
claimed.

## Contact Sheet

Generated PNG:

```text
rebuild_game/build/smoke/wood_lane_10_19_contact_sheet.png
```

The sheet has one row per skill and five columns:

```text
before -> actor/effect -> damage/apply -> tick/body -> final/after
```

## Skill Coverage

| Skill | Name | Source role | Timeline status |
| ---: | --- | --- | --- |
| `10` | Diep Toan | direct simple Wood damage | covered |
| `11` | Quang Phan | direct damage + post-hit heal | covered |
| `12` | Dang Phuoc | direct damage + debuff2 Quan Quanh | covered |
| `13` | Thao Chung | direct damage + debuff3 delayed `150%` | covered |
| `14` | Dang Chi Bich Luy | no-damage buff2 reflect/defense | covered |
| `15` | Thao Nguyen Thuat | no-damage buff3 heal over time | covered |
| `16` | Cham Diep Tram | higher direct Wood damage | covered |
| `17` | Diep Chi An Hue | direct damage + stronger post-hit heal | covered |
| `18` | Dang Man Trien Nhieu | high damage + debuff2 Quan Quanh | covered |
| `19` | Quang Hop Hieu Ung | high damage + debuff3 delayed `200%` | covered in `327` |

## Source/Runtime Facts Locked

- Wood Lane element id is `1`.
- Common actor family for most Wood attack chunks is actor effect `21`, sprite
  `263`.
- Skill `15` uses actor effect `33`, sprite `308`, then `speffect7/AH9`.
- Heal siblings:
  - skill `11`: post-hit heal param `10`;
  - skill `17`: post-hit heal param `40`.
- Debuff2 siblings:
  - skill `12`: low-power Quan Quanh;
  - skill `18`: high-power Quan Quanh.
- Debuff3 siblings:
  - skill `13`: final delayed damage `storedRaw * 150 / 100`;
  - skill `19`: final delayed damage `storedRaw * 200 / 100`.
- Buff2 skill `14` proves defense up, reflect on hit, no reflect on miss, and
  expiry.
- Buff3 skill `15` proves apply heal, active queue tick heal, icon/duration,
  and expiry.

## Battle Lab

The interactive skill lab is still table-driven:

```text
SourceBattleRuntime.enableSkillLabAllSkills
```

So skills `10..19` are available in `battle_lab_skill_test_all` without adding
timeline logic to `VqsvSmokeHarness`.

Dedicated timeline code lives in:

```text
rebuild_game/src/main/java/WoodSkill.java
```

## Verification

Latest pass after skill `19`:

```text
rebuild_game/build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill19_quang_hop_hieu_ung_timeline build\smoke\battle_skill19_quang_hop_hieu_ung_timeline
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Há»|Ä" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\skill19_after_battle_quick
```

Result:

```text
battle_skill19_quang_hop_hieu_ung_timeline: PASS
battle_quick: PASS 227/227
```

## Honest Status

PORTED:

- source rows, PP, power, effect mode, effect id, target side;
- P3/P6/P7 runtime path;
- direct damage and same-run HP settle;
- buff/debuff producer path;
- active queue P12/P13 tick behavior;
- HUD status icon/duration where applicable;
- dedicated before/during/after smoke PNGs for every Wood skill.

PARTIAL/PENDING:

- exact original MIDP pixel comparison;
- exact frame timing against original Java ME runtime;
- full visual parity for every AH/speffect renderer beyond the source-shaped
  frames currently smoked.

## Next Roadmap Step

Move to Earth Lane skills `20..29` from `303_battle_all_skill_source_logic_animation_audit.md`.

Recommended next slice:

```text
Earth Lane skill 20 - Hat Bui
```

Required format stays the same:

```text
audit source -> numeric logic -> animation/effect -> before/during/after smoke -> add/check battle lab
```

Keep timeline code in the lane class, not in `VqsvSmokeHarness`.
