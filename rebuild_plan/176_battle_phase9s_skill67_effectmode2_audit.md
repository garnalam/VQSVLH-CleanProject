# 176 Battle Phase 9-S Skill 67 EffectMode 2 Audit

Status date: 2026-07-09

Status: PHASE 9-S / AUDIT-ONLY / SOURCE-BACKED / NO CODE CHANGES.

Follow-up:

- Phase 9-T smoke for skill `67` is recorded in
  `rebuild_plan/177_battle_phase9t_skill67_raw_visual_smoke.md`.

Purpose:

- Audit skill `67` after Phase 9-Q/R deliberately left it out.
- Prove whether `skill[6] == 2` and `skill[7] == 5` have a consumer.
- Do not port or smoke until the source path is classified.

## Sources Read

Primary:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/original/game/d.class`, inspected with `javap -c -p`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`
- `modules/script/decoded/data__script__speffect.mid.json`
- `rebuild_plan/174_battle_phase9q_source_switch_gap_audit.md`
- `rebuild_plan/175_battle_phase9r_raw_damage_self_buff_smoke.md`

## Skill Row

`db.mid` group 1:

```text
skill 67 = [6,184,596,110,2,30,2,5,5,0]
```

Column interpretation used by current battle docs:

| Column | Value | Meaning |
| --- | --- | --- |
| `0` | `6` | Element/family. |
| `3` | `110` | Power percent in table, but not automatically formula. |
| `6` | `2` | Effect mode 2. |
| `7` | `5` | Effect/debuff id 5 in table. |
| `8` | `5` | Param/chance value. |
| `9` | `0` | Target side. |

## Formula Producer

Phase 9-Q already proved with bytecode:

```text
game.b.b(target) tableswitch routes skill 67 -> offset 706.
offset 706 is default: effectId = -1.
```

Therefore:

```text
skill 67 uses BYTECODE_DEFAULT_RAW_DAMAGE.
skill 67 does not use raw * skill[3] / 100.
skill 67 does not keep skill[7] as an applied target debuff id.
```

This is different from other `skill[6] == 2` rows such as `2/8/12/13/31/32/41/51/61`,
which are explicitly routed by `game.b.b(target)` formula cases and can preserve
their effect/debuff id.

## game.d.q() Consumer

`javap -c -p game.d`, method `private boolean q()`, has a lookupswitch:

```text
11,17      -> heal
21,27,42,48,62,68 -> self buff
52,58      -> leech
64         -> selected-index buff
default    -> if skill[6] == 1, apply effect to target
```

Skill `67` is not in the explicit switch cases.

The default branch only applies an effect when:

```text
aq.c[1][skill][6] == 1
```

Skill `67` has:

```text
aq.c[1][67][6] == 2
```

So `game.d.q()` does not consume `skill[7] == 5` for skill `67`.

## Buff/Debuff Id 5 Meaning

`game.b.a(byte by, int n2, int n3)` shows buff id `5` behavior when it is
actually applied as a source buff:

```text
case 5:
    this.v[5][1] = aq.c[6][5][3]
```

`game.b.b(target)` later has a target buff `5` hook:

```text
if (target.m(5) && ae.a(100) <= target.v[5][1]) {
    attacker.K[5] = damage
}
```

`game.d.q()` later consumes `h.K[5]` if the target had buff 5 and stored damage.

However, Phase 9-S found no source path where skill `67` applies buff `5`.
The id exists and has behavior, but skill `67` does not reach the consumer paths
that would activate it.

## P7 Visual Effect

`effect.mid` row 67:

```text
[0,0,26,0,-1,-1,0, 0,1,11,0,-1,-1,0]
```

Chunks:

| Chunk | Meaning |
| --- | --- |
| `0` | Actor action/effect id `26`. |
| `1` | Special effect id `11`. |

`speffect.mid` rows:

```text
speffect[26] = [14,2,1,50,0,0,0,4,0,0,-26,-25,10,8,-26,-25,10,8]
speffect[11] = [1,0,6,2,0,1]
```

This proves skill `67` has its own P7 animation/effect chain. It does not prove
that `skill[7] == 5` is applied as buff/debuff logic.

## Classification

| Area | Status |
| --- | --- |
| Formula | `BYTECODE_DEFAULT_RAW_DAMAGE`, source-backed. |
| Target debuff from `skill[7] == 5` | NOT_REACHED for skill `67`; bytecode default sets effect id to `-1`. |
| `game.d.q()` post-skill effect | NOT_REACHED; `67` is not a q() explicit case and `skill[6] != 1`. |
| P7 visual animation/effect | AUDITED/PARTIAL; effect row 67 uses chunks `26` and `11`. |
| Runtime code change | NONE in Phase 9-S. |
| Dedicated smoke | Phase 9-T covers raw damage, no debuff id `5`, no q() post-effect, and visual row trace. |

Final audit decision:

```text
Skill 67 should be treated as raw/default damage + P7 visual effect only,
until another source path proves otherwise.
Do not apply buff/debuff id 5 from skill 67.
```

## Next Code Slice

Completed follow-up:

```text
Smoke skill 67 as BYTECODE_DEFAULT_RAW_DAMAGE with no target debuff and no q() post-effect.
```

Assertions:

- P7 damage frame appears.
- Trace contains `BYTECODE_DEFAULT_RAW_DAMAGE skill=67`.
- `appliedDebuffId=-1`.
- No target debuff `5`.
- No player/target q() post-effect from `skill[7]`.
- P7 visual row 67 starts/appears if current renderer supports the audited chunks.

Result:

```text
Phase 9-T passed. Do not implement buff 5 application for skill `67`.
```
