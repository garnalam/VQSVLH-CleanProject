# 174 Battle Phase 9-Q Source Switch Gap Audit

Status date: 2026-07-09

Status: PHASE 9-Q / AUDIT-ONLY / BYTECODE-RESOLVED / NO CODE CHANGES.

Follow-up:

- Phase 9-R smoke for `21/27/42/48/62` is recorded in
  `rebuild_plan/175_battle_phase9r_raw_damage_self_buff_smoke.md`.
- Phase 9-S effectMode `2` audit for skill `67` is recorded in
  `rebuild_plan/176_battle_phase9s_skill67_effectmode2_audit.md`.

Purpose:

- Audit `SOURCE_SWITCH_GAP` skills `21/27/42/48/62/67`.
- Prove whether they enter direct formula branches, default raw-damage branch,
  or post-skill `game.d.q()` behavior.
- Do not change battle code in this slice.

## Sources Read

Primary:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/original/game/b.class`, inspected with `javap -c -p`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`

Important context:

- CFR marks `game.b.b(game.b)` as `Unable to fully structure code`.
- Because of that, the decompiled Java alone was not enough to classify
  `21/27/42/48/62/67`.
- `javap` bytecode was used to resolve the tableswitch.

## Source Skill Rows

Rows from `db.mid` group 1:

| Skill | Row | Initial table meaning |
| --- | --- | --- |
| `21` | `[2,138,550,80,0,45,1,4,10,0]` | element 2, power 80, effectMode 1, buff/effect id 4, param 10, targetMode 0. |
| `27` | `[2,144,556,100,2,30,1,4,10,0]` | element 2, power 100, effectMode 1, buff/effect id 4, param 10, targetMode 0. |
| `42` | `[4,159,571,90,0,45,1,7,5,0]` | element 4, power 90, effectMode 1, buff/effect id 7, param 5, targetMode 0. |
| `48` | `[4,165,577,130,3,15,1,7,5,0]` | element 4, power 130, effectMode 1, buff/effect id 7, param 5, targetMode 0. |
| `62` | `[6,179,591,80,0,45,1,10,5,0]` | element 6, power 80, effectMode 1, buff/effect id 10, param 5, targetMode 0. |
| `67` | `[6,184,596,110,2,30,2,5,5,0]` | element 6, power 110, effectMode 2, effect id 5, param 5, targetMode 0. |

Do not infer damage formula from `skill[3]` alone for these rows.

## Bytecode Result

`javap -c -p game.b`, method `public final int[] b(game.b)`, contains a
`tableswitch 0..69`.

Confirmed switch targets:

| Skill | Bytecode switch target | Meaning in `game.b.b(target)` |
| --- | --- | --- |
| `21` | `706` | `default`: set `effectId = -1`, do not multiply by `skill[3]`. |
| `27` | `706` | `default`: set `effectId = -1`, do not multiply by `skill[3]`. |
| `42` | `706` | `default`: set `effectId = -1`, do not multiply by `skill[3]`. |
| `48` | `706` | `default`: set `effectId = -1`, do not multiply by `skill[3]`. |
| `62` | `706` | `default`: set `effectId = -1`, do not multiply by `skill[3]`. |
| `67` | `706` | `default`: set `effectId = -1`, do not multiply by `skill[3]`. |

For comparison:

| Target offset | Used by | Meaning |
| --- | --- | --- |
| `424` | direct-simple family | `damage = raw * skill[3] / 100`. |
| `447` | skills `1/7` | plus-divisor formula. |
| `487` | explicit debuff chance family. |
| `524`, `579` | conditional debuff formula families. |
| `634` | target buff clear `43/49`. |
| `661` | HP-percent scaling `53/59`. |
| `706` | default/raw branch. |

Therefore the old label `SOURCE_SWITCH_GAP` was valid as a decompiler warning,
but bytecode resolves the formula side:

```text
21/27/42/48/62/67 use default raw-damage path in game.b.b(target).
They do not use skill[3] as a percent multiplier in the formula producer.
They also do not produce target debuff ids from game.b.b(target).
```

The raw damage still continues through shared global modifiers after the switch:
buff/debuff modifiers, relation multiplier, min clamp, jitter, and target buff5
storage.

## P7 State Flow

From `game.d` state `7`:

- On P7 entry, if attacker and target sides allow damage, source calls:

```text
this.Z = this.h.b((b)this.h.p)
```

- Later P7 uses:

```text
Z[0] = damage
Z[1] = crit flag
Z[2] = applied debuff id
```

- If `skill[3] == 0`, P7 skips the damage popup path.
- These six skills have non-zero `skill[3]`, so they are eligible for the P7
  damage path, but their bytecode formula branch leaves damage as raw/base
  rather than `raw * skill[3] / 100`.

## game.d.q() Post-Skill Behavior

`game.d.q()` has a source-backed post-skill switch:

| Skill | `game.d.q()` branch | Source side effect |
| --- | --- | --- |
| `21` | case `21/27/42/48/62/68` | `h.a(skill[7], -1, skillId)` applies self buff id `4`. |
| `27` | case `21/27/42/48/62/68` | `h.a(skill[7], -1, skillId)` applies self buff id `4`. |
| `42` | case `21/27/42/48/62/68` | `h.a(skill[7], -1, skillId)` applies self buff id `7`. |
| `48` | case `21/27/42/48/62/68` | `h.a(skill[7], -1, skillId)` applies self buff id `7`. |
| `62` | case `21/27/42/48/62/68` | `h.a(skill[7], -1, skillId)` applies self buff id `10`. |
| `67` | no explicit case; `skill[6] == 2`, not the default `skill[6] == 1` route | No source-backed `q()` buff/effect found in the audited slice. |

When `skill[6] == 1`, `game.d.q()` also emits the buff name text using
`aq.c[6][buffId][0]`.

Buff table rows read:

| Buff id | Row |
| --- | --- |
| `4` | `[337,352,2,-1,-1]` |
| `7` | `[340,355,2,-1,-1]` |
| `10` | `[343,358,2,-1,-1]` |
| `5` | `[338,353,3,30,-1]` |

Skill `67` references effect id `5`, but Phase 9-S confirms `game.d.q()` does
not apply it for `skill[6] == 2`. Treat skill `67` as
`RAW_DAMAGE_PLUS_P7_VISUAL_ONLY`, not as self-buff or target debuff.

## Rebuild Mapping

Current rebuild state:

| Area | Rebuild file | Status |
| --- | --- | --- |
| Formula producer | `VqsvBattleUnit.computeDamage()` | Source-shaped for default/raw branch: unrecognized formula families leave `damage = raw` and `effectId = -1`. |
| Post-skill q() self buff | `VqsvBattleRuntime.applyP7PostSkillBehavior()` | Has explicit cases `21/27/42/48/62/68`, applies `row.effectId` to attacker. |
| Skill `67` post effect | `VqsvBattleRuntime.applyP7PostSkillBehavior()` | No explicit `67` case; Phase 9-S confirms no q() buff/debuff consumer. |
| Dedicated smoke | `VqsvSmokeHarness` | Phase 9-R covers `21/27/42/48/62`; `67` remains pending. |

No code changes were made in Phase 9-Q.

## Classification

| Skill | Formula classification | Post-skill classification | Audit status |
| --- | --- | --- | --- |
| `21` | `BYTECODE_DEFAULT_RAW_DAMAGE` | `DQ_SELF_BUFF buff4` | AUDITED, needs smoke. |
| `27` | `BYTECODE_DEFAULT_RAW_DAMAGE` | `DQ_SELF_BUFF buff4` | AUDITED, needs smoke. |
| `42` | `BYTECODE_DEFAULT_RAW_DAMAGE` | `DQ_SELF_BUFF buff7` | AUDITED, needs smoke. |
| `48` | `BYTECODE_DEFAULT_RAW_DAMAGE` | `DQ_SELF_BUFF buff7` | AUDITED, needs smoke. |
| `62` | `BYTECODE_DEFAULT_RAW_DAMAGE` | `DQ_SELF_BUFF buff10` | AUDITED, needs smoke. |
| `67` | `BYTECODE_DEFAULT_RAW_DAMAGE` | No `q()` side effect; Phase 9-S classifies `skill[7] == 5` as NOT_REACHED for buff/debuff logic | AUDITED/PARTIAL, needs raw/no-debuff smoke. |

## Next Code Slice

Recommended Phase 9-R:

```text
Smoke bytecode-default raw-damage + q() self-buff skills 21/27/42/48/62.
```

Scope:

- Add deterministic smoke checkpoints for `21/27/42/48/62`.
- Assert P7 damage appears.
- Assert damage is raw/default shape, not `raw * skill[3] / 100`.
- Assert `game.d.q()` post text/buff is applied to attacker.
- Assert target receives no debuff from `Z[2]`.

Keep `67` separate:

```text
Phase 9-S: audit skill 67 effectMode 2 behavior.
```

Reason:

- Bytecode confirms raw damage in `game.b.b(target)`.
- Phase 9-S confirms `skill[7] == 5` is not consumed as buff/debuff logic.
- Next action is smoke `67` as raw damage with no debuff/no q() post-effect.
