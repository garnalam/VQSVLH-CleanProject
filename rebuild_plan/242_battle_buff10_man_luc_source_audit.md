# 242 - Battle Buff10 Man Luc Source Audit

Date: 2026-07-13

Scope: focused audit for `aq.c[6][10]` / buff10 `Man Luc` after Slice A
status effectiveness smoke showed the HUD icon but no damage increase.

No gameplay code was changed by this audit.

## Question

Determine whether buff10 row parameter `-1` is:

- a sentinel that source interprets elsewhere;
- a signed/unsigned decode error;
- logic-specific data passed from the skill producer;
- or a real source/data contradiction.

## Source Facts

### Loader / signedness

Source loader:

- `source_code/decoded/decompiled_source_cfr/aq.java:78-84`
- `aq.b("/data/script/db.mid")` loads nine matrices with `ae.a(InputStream)`.
- `ae.a` / the project decoder use Java `DataInputStream.readShort()` semantics.

Decoder proof:

- `reports/decoded/tools/analyze_jar.py:210-218`
- `Reader.i2()` at `reports/decoded/tools/analyze_jar.py:49-50`
- `i2()` is signed big-endian short.

Original raw row proof:

```text
script/original/db.mid offset 7188
bytes: 0157 0166 0002 FFFF FFFF
signed short row: [343,358,2,-1,-1]
```

Therefore `-1` is not a JSON/export artifact. It is present in the original
resource as signed short `0xFFFF`.

### Buff row

Decoded row:

```text
aq.c[6][10] = [343,358,2,-1,-1]
```

Meaning by adjacent rows and source consumers:

| Index | Value | Meaning |
| ---: | ---: | --- |
| 0 | 343 | name text id |
| 1 | 358 | description text id |
| 2 | 2 | duration |
| 3 | -1 | paramA used by source case 10 |
| 4 | -1 | paramB unused by source case 10 |

Vietnamese text says the strength/attack value increases, but the numeric row
does not contain a positive percent.

### Bytecode / decompiled logic

Decompiled source:

- `source_code/decoded/decompiled_source_cfr/game/b.java:545-548`

```java
case 10: {
    this.v[by][1] = (short)(this.c[2] * aq.c[6][by][3] / 100);
    this.d[2] = n2 = (int)((short)(this.c[2] + this.v[by][1]));
    break;
}
```

Bytecode confirms the same:

- `source_code/decoded/bytecode_javap/game__b.javap.txt:3368-3407`

The bytecode reads:

```text
c[2] * aq.c[6][by][3] / 100
d[2] = c[2] + v[by][1]
```

There is no unsigned conversion and no special branch for `-1`.

### Tick / reassert logic

Decompiled source:

- `source_code/decoded/decompiled_source_cfr/game/b.java:642-645`

```java
case 10: {
    this.d[2] = (short)(this.c[2] + this.v[n2][1]);
    break;
}
```

So after initial application, source keeps reasserting the same stored
`v[10][1]`. It still does not reinterpret `-1`.

### Skill producer

Buff10 is produced by skill rows whose `effectMode == 1` and `effectId == 10`:

```text
skill 62: [6,179,591,80,0,45,1,10,5,0]
skill 68: [6,185,597,110,3,15,1,10,5,0]
```

Decompiled `game.d.q()`:

- `source_code/decoded/decompiled_source_cfr/game/d.java:1991-1998`
- `source_code/decoded/decompiled_source_cfr/game/d.java:2039-2050`

For skills `21,27,42,48,62,68`, source calls:

```java
d2.h.a((byte)aq.c[1][n2][7], -1, n2);
```

For buff10 this means:

```text
buffId = 10
value/n2 argument = -1
sourceSkill = 62 or 68
```

But `game.b.a(byte,int,int)` case 10 ignores both the `value` argument and the
skill's row param. It only uses `aq.c[6][10][3]`.

## Rebuild Mapping

Current rebuild:

- `rebuild_game/src/main/java/VqsvBattleUnit.java:510-513`

```java
case 10:
    buffSlots[buffId][1] = toShort(baseStats[STAT_ATTACK] * row.paramA / 100);
    currentStats[STAT_ATTACK] = toShort(baseStats[STAT_ATTACK] + buffSlots[buffId][1]);
    break;
```

This mirrors source. With `paramA == -1`, attack is reduced by roughly 1% due to
integer division instead of increased.

Slice A smoke measured this source-shaped behavior:

```text
baseline damage: 80
buff10 damage: 79
```

## Conclusion

Status: `PORTED-AS-SOURCE / SOURCE_ODDITY`

The audit did not find evidence that `-1` should be patched into a positive
attack-up percent:

- not a JSON decode error;
- not an unsigned/signed loader bug;
- not reinterpreted by bytecode;
- not supplied by skill 62/68 producer;
- not handled by tick/reassert logic.

The remaining contradiction is between Vietnamese description text and the
source data/code behavior. A gameplay patch that makes buff10 increase attack
would be an `APPROX/DESIGN_FIX`, not a source-backed port.

## Decision

Do not patch runtime damage semantics in the source-port path.

Keep buff10 marked as:

```text
PORTED-AS-SOURCE / SOURCE_ODDITY
```

If a later product decision wants user-facing behavior to match description
text, implement it behind an explicitly named non-source parity fix and update
the smoke expectation separately.

## Next Roadmap Step

Proceed with optional status-effect smoke expansion:

1. form0 low-HP attack boost;
2. form4 crit bonus;
3. form10 HP floor;
4. buff13 cleanse/heal visual;
5. debuff3 delayed damage;
6. debuff2 command-disable behavior.

These should remain smoke-only unless a checkpoint exposes a concrete source
gap.
