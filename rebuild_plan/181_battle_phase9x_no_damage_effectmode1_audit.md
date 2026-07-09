# 181 Battle Phase 9-X No-Damage EffectMode1 Audit

Status date: 2026-07-09

Status: PHASE 9-X / AUDIT-ONLY / NO CODE CHANGE.

Purpose:

- Audit remaining no-damage/default `effectMode == 1` skills:
  `4/5/14/24/25/34/35/44/65`.
- Prove source routing before adding smoke or code.
- Separate logic parity from P7 special-effect visual parity.

## Sources

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`
- `modules/script/decoded/data__script__speffect.mid.json`
- `rebuild_plan/78_battle_active_effect_lifecycle_full_matrix.md`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`

## Common Source Route

All audited skills have this shape:

```text
skill[3] == 0
skill[6] == 1
skill[9] == 1
```

Source route:

| Step | Source | Behavior |
| --- | --- | --- |
| Target vector | `game.d.b(byte skillId)` | For `skill[9] == 1`, target vector `h.G/h.H` includes living same-side units. |
| P6 confirm | `game.d.i()` | Sets `h.p` to selected same-side target and `h.I` to selected `d[]` slot. |
| P7 no-damage | `game.d.q()` | Default branch applies `((b)h.p).a((byte)skill[7], -1, skillId)` when `skill[6] == 1`. |
| Text placement | `game.d.q()` | Default `effectMode1` rows draw buff text on `h.p`, not attacker hardcoded. |
| Damage | P7 damage path | `skill[3] == 0`, so these should not create normal damage or hit/miss result. |

Rebuild current route:

| Area | Rebuild equivalent | Audit status |
| --- | --- | --- |
| Same-side target list | `VqsvBattleRuntime.prepareTargetList()` targetSide `1` adds current player/enemy same-side unit. | PORTED/PARTIAL for one active unit. |
| Selected slot | `commitSelectedTarget()` now writes `selectedTargetSlot`; mostly relevant to skill64, but target vector trace is shared. | PORTED/PARTIAL. |
| Default q() | `applyP7PostSkillEffects()` default `row.effectMode == 1` applies source buff to `p7Target`. | PORTED/PARTIAL. |
| No-damage guard | `p7NoDamageSkill()` skips damage path for `powerPercent == 0`. | PORTED/PARTIAL. |
| P7 special renderer | only AH type `9` and `1` are currently ported for normal P7 special chunks. | PARTIAL; several Phase 9-X visuals are pending. |

## Skill Matrix

| Skill | Row | Buff | Logic source behavior | Effect row | Visual status | Rebuild logic status | Needed smoke |
| ---: | --- | ---: | --- | --- | --- | --- | --- |
| `4` | `[0,121,533,0,1,10,1,0,-1,1]` | `0` | Apply buff0 to selected ally/self. Buff0 raises defense immediately and later can add damage when duration counter reaches `0`. | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | speffect16 AH type9 + speffect15 AH type1, both PORTED/PARTIAL. | PARTIAL; apply/formula hook exists, producer smoke missing. | Smoke no-damage + player buff0 active + defense up + later formula hook if feasible. |
| `5` | `[0,122,534,0,1,10,1,1,-1,1]` | `1` | Apply buff1 to selected ally/self. Lowers own defense, increases outgoing damage percent. | same as skill4 | speffect16/15 PORTED/PARTIAL. | PARTIAL; apply/formula hook exists, producer smoke missing. | Smoke no-damage + player buff1 active + defense down + damage boost. |
| `14` | `[1,131,543,0,1,10,1,2,-1,1]` | `2` | Apply buff2 to selected ally/self. Raises defense; source formula also checks target buff2 as a reflect/damage-side hook. | `[0,0,21,1,-1,-1,0]` | Actor action `u21`, no AH chunk. Actor action parity is PORTED/PARTIAL. | PARTIAL; apply and current formula hook exist, producer smoke missing. | Smoke no-damage + player buff2 active + defense up. |
| `24` | `[2,141,553,0,1,10,1,13,-1,1]` | `13` | Heal selected ally/self by max HP percent and clear all debuffs. Buff13 also heals on tick. | `[0,0,22,0,-1,-1,0, 0,1,17,0,-1,-1,0]` | actor action then speffect17 AH type1 PORTED/PARTIAL. | PARTIAL; heal+clearDebuffs exists, producer smoke missing. | Smoke no-damage + heal text + debuffs cleared. |
| `25` | `[2,142,554,0,1,10,1,14,-1,1]` | `14` | Clear selected ally/self debuffs and set buff14. Source target debuff application checks buff14 and blocks new debuffs. | `[0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]` | speffect4 AH type7 PENDING, speffect17 AH type1 PORTED/PARTIAL. | PARTIAL; clearDebuffs and debuff-block hook exist, producer smoke missing. | Smoke no-damage + debuffs cleared + subsequent debuff blocked. |
| `34` | `[3,151,563,0,1,10,1,5,-1,1]` | `5` | Apply buff5 to selected ally/self. Source damage formula: if target has buff5 and chance passes, attacker stores reflected/returned damage in `K[5]`; `game.d.q()` later subtracts it from attacker. | `[0,1,4,0,-1,-1,0]` | speffect4 AH type7 PENDING. | PARTIAL; stored-damage hook exists but broad parity pending. | Smoke no-damage + player buff5 active + later attacker HP loss hook. |
| `35` | `[3,152,564,0,1,10,1,6,-1,1]` | `6` | Apply buff6 to selected ally/self. Source has odd formula shape: checks `target.m(6)` but reads attacker's `v[6][1]/[2]`. Must preserve source oddity. | `[0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]` | speffect4 AH type7 PENDING, speffect17 AH type1 PORTED/PARTIAL. | PARTIAL; rebuild formula currently preserves the odd shape, producer smoke missing. | Smoke no-damage + buff6 active + formula hook with deterministic roll. |
| `44` | `[4,161,573,0,1,10,1,8,-1,1]` | `8` | Apply buff8 to selected ally/self. Source: extra skill PP consumption and outgoing damage boost. | `[0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | speffect19 AH type9 + speffect15 AH type1 PORTED/PARTIAL. | PARTIAL; PP and damage hooks exist, producer smoke missing. | Smoke no-damage + player buff8 active + PP/damage hook. |
| `65` | `[6,182,594,0,1,10,1,12,-1,1]` | `12` | Apply buff12 to selected ally/self. Apply sets `K[12]=1`; tick sets `K[12]=2`; `game.d.q()` can route follow-up P2 when `K[12] == 2`. | `[1,1,16,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | attacker-side speffect16 AH type9 + speffect15 AH type1 PORTED/PARTIAL. | PARTIAL; consumer smoke exists, producer smoke missing. | Smoke skill65 producer + active queue tick to `K12=2` + follow-up P2. |

## Buff Rows

Relevant `aq.c[6]` rows:

```text
buff0  = [333,348,2,30,190]
buff1  = [334,349,3,50,50]
buff2  = [335,350,3,30,10]
buff5  = [338,353,3,30,-1]
buff6  = [339,354,3,50,-1]
buff8  = [341,356,4,30,-1]
buff12 = [345,360,2,-1,-1]
buff13 = [346,361,3,20,-1]
buff14 = [347,362,3,-1,-1]
```

## Visual Gap Matrix

| AH type / actor action | Used by | Current status |
| --- | --- | --- |
| AH type `9` | skills `4/5/44/65` chunk0 | PORTED/PARTIAL. |
| AH type `1` | skills `4/5/24/25/35/44/65` chunk1 | PORTED/PARTIAL. |
| AH type `7` | skills `25/34/35` chunk0 via speffect4 | PENDING for normal P7 special renderer. |
| AH type `11` | skill `24` uses speffect22? Not in this Phase 9-X row set after exact effect row read; no direct chunk currently. | PENDING globally, not a Phase 9-X blocker unless a row calls it. |
| Actor action `u21/u22` | skills `14/24` actor chunks | PORTED/PARTIAL/source-shaped; not pixel-perfect. |

Correction note:

- Initial root JSON indexing of `effect.mid` returned blank because the decoded file is `{format, rows}`.
- The rows above use `rows[skillId]`.

## Current Rebuild Gaps

| Gap | Impact |
| --- | --- |
| No dedicated producer smoke for skills `4/5/14/24/25/34/35/44/65`. | Existing logic may work, but Phase 9 cannot mark these rows smoke-covered yet. |
| Skill `65` has consumer smoke for buff12 follow-up, not producer smoke for applying buff12. | Matrix should not overclaim skill65 full coverage. |
| AH type `7` is pending for P7 special chunks. | Skills `25/34/35` can be logic-smoked before visual parity is complete, but visual status must remain PARTIAL/PENDING. |
| Same-side target vector currently covers one active player/enemy in rebuild. | Multi-pet/multi-target same-side parity is still PENDING. |
| Exact buff5/buff6 RNG/source oddity broad coverage is incomplete. | Skills `34/35` should get deterministic smoke before moving on. |

## Recommended Code/Smoke Order

Do not port all nine at once. Suggested slices:

1. **Phase 9-Y: easy producer smoke for 4/5/14/44**
   - Buff0/1/2/8 are formula/stat hooks already represented.
   - Use no-damage P7, assert active buff and no damage frame.
2. **Phase 9-Z: cleanse/protection producer smoke for 24/25**
   - Seed player debuff, use skill24/25, assert clearDebuffs.
   - For skill25, also assert subsequent debuff block through buff14.
3. **Phase 9-AA: defensive hook producer smoke for 34/35**
   - Seed deterministic follow-up damage scenario.
   - Assert buff5 stored damage and buff6 formula hook.
4. **Phase 9-AB: skill65 producer-to-consumer**
   - Use skill65 to apply buff12.
   - Let active queue tick set `K12=2`.
   - Assert next q() routes to P2 follow-up.

## Status Decision

| Area | Status |
| --- | --- |
| Source route for all Phase 9-X rows | AUDITED. |
| Logic classification for all nine skills | AUDITED. |
| Current rebuild logic mapping | PARTIAL; shape exists, producer smoke missing. |
| Visual parity | PARTIAL/PENDING, especially AH type7 for skills `25/34/35`. |
| Code changes | NONE in this audit slice. |
