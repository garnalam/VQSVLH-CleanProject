# 96 Battle Pet State Persistence Matrix

## Scope

Current slice: source-backed pet HP/PP payload persistence and recovery.

Status key: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN.

## Source Proof

### game.b.P() serialization

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

`game.b.P()` serializes the current pet state:

- `[0]` species `V`
- `[1]` level `T`
- `[2]` form/current form `c[5]`
- `[3]` status/item side field `d[6]`
- `[4]` quality `c[0]`
- `[5]` nature `W`
- `[6]` current HP `d[1]`
- `[7]` exp `S`
- `[8]` visual/evolution field `E`
- `[9]` skill count `O`
- `[10..]` skill ids, followed by current PP values `y[]`

Conclusion: caught pet and party pet storage must keep current HP and PP, not always full HP.

### game.b.I() recovery

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

`game.b.I()`:

- loops each skill id `z[i]`
- restores `y[i] = aq.c[1][z[i]][5]`
- calls `g()`
- `g()` recalculates stats and calls `u(c[1])`, restoring HP to max
- calls `c()` for runtime visual/state refresh

Conclusion: event opcode 39 recovery refreshes HP=max and PP=max for each party pet. Full visual/runtime side effects from `c()` remain PORTED-PARTIAL in rebuild.

### game.c opcode 39

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/c.java`

`case 39` loops `this.x.z[i].I()`.

Conclusion: op39 is the source-backed story recovery point, including the "10 years later" style reset.

### game.d battle exit and catch

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

- Battle entry snapshots party pet HP into battle objects and static/vector lists.
- P8/P9 result paths write current battle pet HP back into the source pet vector.
- P17 q=3 catch success calls `game.d.p.a(((b)this.h.p).P())` for bag or `game.d.p.b(((b)this.h.p).P())` for bank.

Conclusion: battle exit must persist the active player pet. P17 storage must call `P()` before any local render cleanup that sets enemy HP to 0.

## Rebuild Mapping

### PORTED

- `SourcePetState.persistBattleUnit(BattleUnit)` stores species, level, quality, nature, current HP, exp, skill ids, and current PP into source payload.
- `SourcePetState.caughtFromBattleUnit(...)` stores caught enemy payload from current `BattleUnit`.
- `BattleUnit.fromSourcePet(...)` restores HP from payload `[6]` and PP from payload skill section.

### PORTED-PARTIAL

- `VqsvSourceEffects.op39RefreshPets(...)` now restores HP=max and PP=max in source payload, matching `game.b.I()` core data semantics.
- `SourceBattleRuntime.persistActivePlayerPet(...)` persists active player pet on P8/P9/P10 exit.
- P17 catch stores caught enemy before setting render HP to 0.

Remaining partials:

- Full `game.b.c()` visual/runtime refresh side effects are not fully modeled.
- Full `game.d.x` vector damage-delta parity is not pixel/byte exact; rebuild persists active slot payload directly.
- Buff/debuff queues are battle runtime state and are not fully serialized because source `P()` does not serialize every buff/debuff queue field.

## Smoke Coverage Added

- Sophie loss persists initial Dien Mieu HP as 0 in payload `[6]`.
- op39 recovery restores Dien Mieu HP to max and PP to max.
- Catch success stores Bunny with low HP payload, not 0 and not full.
- P5 pet switch menu reads caught Bunny low HP from payload.

