# Gameplay / Battle / Save Notes

Follow-up chi tiet: battle state machine, state phu `18/19/24/101/102/104`, skill/effect table semantics, va damage formula da tach/dong trong [17_battle_state_machine.md](17_battle_state_machine.md).

Nguon doc trong pass nay:

- `source_code/decoded/decompiled_source_cfr/game/g.java`
- `source_code/decoded/decompiled_source_cfr/game/b.java`
- `source_code/decoded/decompiled_source_cfr/game/d.java`
- `source_code/decoded/decompiled_source_cfr/game/h.java`
- `source_code/decoded/decompiled_source_cfr/game/k.java`
- `source_code/decoded/decompiled_source_cfr/ar.java`
- `source_code/decoded/decompiled_source_cfr/aq.java`
- `source_code/decoded/decompiled_source_cfr/an.java`
- `source_code/decoded/decompiled_source_cfr/ae.java`

Trang thai: PARTIAL for the whole Gameplay/Battle/Save area, but battle core itself has a newer closed audit in [17_battle_state_machine.md](17_battle_state_machine.md).

## 1. Tong Quan

Core gameplay data flow:

```text
aq loads static DB/scripts
  -> game.g owns player/runtime inventory/pets/progression
      -> game.b instances are pets/battle creatures
          -> game.d uses game.g active pets + battle setup to run battle
              -> game.h supplies battle/menu/bag/shop UI
                  -> game.k saves/loads all long-lived state through ar RMS wrappers
```

Important split:

- `game.g` = player/world gameplay model.
- `game.b` = individual pet/creature/battle unit model.
- `game.d` = battle engine/state machine.
- `game.h` = UI workflows that mutate `game.g` and `game.b`.
- `game.k` = world controller + save/load coordinator.
- `ar` = low-level RMS record wrapper only.

## 2. Static Data: `aq`

`aq.a()` loads major database tables:

```text
/data/script/sprite.mid
/data/script/chs.mid
/data/script/npcDialog.mid
/data/script/db.mid
```

Observed table usage:

| Table access | Meaning observed | Status |
|---|---|---|
| `aq.c[0]` | Pet/creature base data: name, element, stats, sprite/evolution/skill refs; battle relation/catch class `[22]` is closed in file 17. | VERIFIED |
| `aq.c[1]` | Skill data used in battle damage/effects; columns `[0..9]` are closed in file 17. | VERIFIED |
| `aq.c[2]` | Passive/badge battle bonuses; main battle usage is closed in file 17, wider gameplay naming can still be refined. | VERIFIED/PARTIAL |
| `aq.c[3]` | Status/form config used by battle formulas/status icons; battle usage is closed in file 17. | VERIFIED |
| `aq.c[4]` | Normal item DB used by inventory/shop. | VERIFIED |
| `aq.c[5]` | Task/reward/special item group. | PARTIAL |
| `aq.c[6]` | Buff/self effect config: name, desc, duration, param1, param2. Runtime mapping to `game.b.v` is closed in file 17. | VERIFIED |
| `aq.c[7]` | Debuff/target effect config: name, desc, duration. Runtime mapping to `game.b.w` is closed in file 17. | VERIFIED |
| `aq.c[8]` | Skill learn thresholds by species learn group/tier. | VERIFIED |

Do not port gameplay formulas without porting `aq` DB access first.

## 3. Player Model: `game.g`

`game.g` extends `f` and is singleton:

```text
game.g.o()
```

Core fields:

| Field | Observed role | Status |
|---|---|---|
| `z` | Active `game.b` pets, max 6. | VERIFIED |
| `A` | Active pet count. | VERIFIED |
| `J` | Item vector group, category 0 split by `aq.c[4][id][5] != 0`. | PARTIAL |
| `K` | Item vector group, category 0 split by `aq.c[4][id][5] == 0`; starts with `{0,0,1}`. | PARTIAL |
| `L` | Unique/special low-id category-2 list. | PARTIAL |
| `M` | Stack vector for category-2 ids `>= 12`. | PARTIAL |
| `N` | Task/temp progression vector `[id, marker, qty]`. | PARTIAL |
| `O` | Pet bank/storage, max 100. | VERIFIED |
| `T` | 21 boolean event/game flags. | VERIFIED |
| `af` | Money/gold-like value; UI text says `kim`. | VERIFIED |
| `ag` | Badge/secondary currency; UI text says `huy hieu`. | VERIFIED |
| `B/C/D/E/P/Q/R` | Player progression/equipment/element-slot-like arrays, saved in RMS. | PARTIAL |

Inventory helper semantics:

```text
a(id, qty, category) -> can receive / capacity check
b(id, qty, category) -> has enough
c(id, qty, category) -> add
d(id, qty, category) -> remove
a(id, category)      -> quantity
```

Currency:

```text
s(delta) -> money += delta
t(value) -> money >= value
u(delta) -> badge += delta
v(value) -> badge >= value
```

Pet capacity:

```text
y() == 0 -> active pet slot available
y() == 1 -> active full, bank has space
y() == 2 -> no active/bank space
```

Pet add/remove:

- `a(id, level, element/quality, nature?, data)` adds active pet.
- Overloads can insert at index, add from serialized int array, or add to bank.
- `n(id)` removes active pet by species/id.
- `b(int[] data)` stores serialized pet data into bank `O`.

## 4. Pet/Battle Unit: `game.b`

`game.b` extends `f`; it is both pet data and battle unit.

Initializer:

```text
a(speciesId, level, attachedItemOrMark, side/type, quality, extraByte)
```

Stat formula from `game.b.a(...)`:

```text
qualityMultiplier = M[quality - 1] where M = {90,95,100,110,125}
maxHP   = (base5 + grow6 * level + add7) * multiplier / 100
attack  = (base8 + grow9 * level + add10) * multiplier / 100
defense = (base11 + grow12 * level / 10 + add13) * multiplier / 100
speed   = (base14 + grow15 * level / 10 + add16) * multiplier / 100
sprite  = aq.c[0][speciesId][17]
```

Serialized active pet format `P()`:

```text
[0] speciesId
[1] level
[2] c[5]
[3] d[6]
[4] quality c[0]
[5] W
[6] current HP d[1]
[7] exp/progress S
[8] E
[9] skillCount O
[10 .. 10+O-1] skill ids z[]
[10+O .. 10+2O-1] skill power/pp y[]
```

Battle calculations:

| Area | Observed behavior | Status |
|---|---|---|
| `B()` | Computes attack minus target defense, with status/passive modifiers. | VERIFIED |
| `a(b target)` | Element advantage relation; returns `0`, `1`, or `-1`. | VERIFIED |
| `a(b target)` | Element/relation helper; bytecode verified, including `aq.c[0][22]` class behavior. | VERIFIED |
| `b(b target)` | Damage package calculation; CFR damaged in source but bytecode verified in file 17. Returns `{damage, critFlag, appliedDebuffId}`. | VERIFIED |
| `S()` | Alive check: `d[1] > 0`. | VERIFIED |
| `P()/Q()` | Serialization for active/bank skill data. | VERIFIED |
| `T()/y(int)` | Element display names. | VERIFIED |

Important pet fields:

| Field | Observed role | Status |
|---|---|---|
| `V` | Species id. | VERIFIED |
| `T` | Level. | VERIFIED |
| `c[]` | Base/current max stats. | VERIFIED |
| `d[]` | Runtime stats/current HP/status-related values. | VERIFIED |
| `z[]` | Skill ids. | VERIFIED |
| `y[]` | Skill PP/current uses. | VERIFIED |
| `v/w/x/N` | Buff/debuff active banks and queues; closed in file 17. | VERIFIED |
| `G/H` | Target candidate vectors in battle. | VERIFIED/PARTIAL |

## 5. Battle Engine: `game.d`

Battle entry already audited from event VM:

```text
opcode 37/54 -> game.d.a().a(int[][] setup)
opcode 32    -> set game.d.a/b/c and game.i state 12
game.i state 12 -> game.d.d()
```

`game.d.d()` initialization:

- Sets `p = game.g.o()`.
- Builds `f[]` active pet index list from `p.A`.
- Creates enemy/player unit array `d[]`.
- Loads battle scripts:

```text
/data/script/pos.mid
/data/script/cpos.mid
/data/script/effect.mid
/data/script/speffect.mid
/data/script/blood.mid
/data/script/bufDebuf.mid
```

- Creates `al[]` helper sprites using sprite 294.
- Creates battle participants from setup `s`.
- Calls `T()` to sort turn order.
- Loads blood textures `blood_0..2`.
- Saves original active pet HP into `A`.
- Enters state `0`.

Battle state machine:

| State | Observed role | Status |
|---|---|---|
| `0` | Entry/position animation into battle. | VERIFIED |
| `1` | Select next active unit in turn vector `v`. | VERIFIED |
| `12/13` | Pre-turn buff/debuff tick states for enemy/player side. | VERIFIED |
| `2` | Select/execute skill state. | VERIFIED |
| `7` | Execute chosen attack/skill: target setup, effect setup, hit/miss flag. | VERIFIED |
| `3/4/5/6/16/17/18/20/21/22/23/24` | Battle UI/effect/capture/skill/result substates; state `5` still needs end-to-end UI validation, but core state meanings are closed in file 17. | VERIFIED/PARTIAL |
| `8` | Battle won / return path with player pet HP restore-ish logic. | VERIFIED/PARTIAL |
| `9` | Battle exit/lose/return to world state 10 or follow event flag. | VERIFIED/PARTIAL |
| `101/102/104` | SMS purchase helper substates: master ball, money, badge. | VERIFIED |

Turn order:

```text
T() sorts d[] by speed c[4], with special status f(7) override.
v = ordered Vector of active battle units.
e[] maps turn order index -> participant index.
```

Targeting:

- `b(byte skillId)` fills `h.G/H` target candidates.
- Skill target group comes from `game.b.b(skillId, 9)`.
- `0` means enemy-side target; `1` means same-side target.

Damage/effects:

- `q()` and surrounding code apply skill effect based on `aq.c[1][skillId]`.
- Status effect ids like `21,27,42,48,62,64,68` have special branches.
- Buff/debuff arrays `ap/aq/ar` come from `bufDebuf.mid`.
- Floating damage text/effects use `a(String,... )` and `blood.mid`.

Current boundary: battle core loop, state matrix, damage formula, and skill/effect table semantics are audited in detail in file 17. Remaining battle work is implementation regression, animation timing, and split UI edge cases such as pet switch state `5`.

## 6. Save/Load: `ar` + `game.k`

`ar` is a low-level RMS wrapper:

```text
new ar(recordName)
a()                    -> read record 1 payload
a(ByteArrayOutputStream) -> write payload
a(byte[] extra)        -> append extra payload when staged
b()                    -> delete record store
```

Special combined format:

```text
byte 1
byte 44
byte 79
int primaryLength
primary bytes
extra bytes
```

`game.k` owns 10 RMS slots:

```text
0 PK6_RMS_ACTOR
1 PK6_RMS_WORLD
2 PK6_RMS_EVENT
3 PK6_RMS_RMS
4 PK6_RMS_SMS
5 PK6_RMS_CNTSMS
6 PK6_RMS_GOLD
7 PK6_RMS_POKPET
8 PK6_RMS_CONITEM
9 PK6_RMS_PETBALL
```

Observed slot contents:

| Slot | Save method | Load method | Contents | Status |
|---|---|---|---|---|
| `0 ACTOR` | `c(g)` via `a(g)` + extra bank data | `d(g)` via `b(g)` | Player position/facing, progression arrays, `L/M/N/T`, active/banked pet extra, flags, time. | VERIFIED/PARTIAL |
| `1 WORLD` | `X()` | `Y()` | Current world/room plus `ag/ah` actor/world persisted arrays. | VERIFIED |
| `2 EVENT` | `Z()` | `aa()` | Event VM persisted `M.b`, `game.c.t/u/s`, and event timer/progress `M.k()/M.p`. | VERIFIED/PARTIAL |
| `3 RMS` | `h()` | `i()` | Global flags `W/x/D/G`. | PARTIAL |
| `4 SMS` | `ab()` | `ac()` | SMS/payment flag `X`. | PARTIAL |
| `5 CNTSMS` | not fully audited in this pass | not fully audited | SMS count/payment side effect. | UNKNOWN/PARTIAL |
| `6 GOLD` | `ad()` | `ae()` | Money `af` and badge `ag`. | VERIFIED |
| `7 POKPET` | `j()` | `aj()` | Active pet count and each active pet `game.b.P()`. | VERIFIED |
| `8 CONITEM` | `ah()` | `ai()` | Inventory vector `J`. | VERIFIED/PARTIAL |
| `9 PETBALL` | `af()` | `ag()` | Inventory vector `K`. | VERIFIED/PARTIAL |

Full save call:

```text
game.k.k()
  -> c(this.c)   // slot 0, includes active/banked pet staging
  -> X()         // slot 1 world
  -> Z()         // slot 2 event
  -> game.k.h()  // slot 3 global flags
  -> game.k.ab() // slot 4 SMS flag
```

Partial save call:

```text
game.k.n()
  -> game.k.ab()
  -> ad()  // slot 6 gold/currency
  -> af()  // slot 9 K inventory
```

Delete/reset:

```text
game.k.o()
  -> delete all slots except slot 4 if present
```

Important save detail:

`game.k.c(g)` sets `af[0].a = true`, writes primary actor state, then appends bank pet vector `O` as extra bytes through `ar.a(byte[])`. On load, `ar.a()` separates primary bytes and extra bytes into `ar.b`; `game.k.d(g)` reads bank pet storage from `af[0].b`.

## 7. UI Coupling

`game.h` mutates gameplay directly:

- Bag/shop use `game.g.a/b/c/d(id, qty, category)` and currency checks.
- Pet UI uses active pets `q.z[index]`, pet skill/evolve data from `game.b` and `aq`.
- Battle UI uses `game.d` current actor `h`, command states, and target vectors `G/H`.
- Task UI reads vectors `N/L/M/K/J` and event flags.

Port rule: UI cannot be rebuilt as a passive view only. It owns many command handlers and must call the gameplay model.

## 8. Rebuild Order For This Area

Recommended order:

1. Port `aq` static DB loaders enough for `aq.c` access.
2. Port `game.b` data model and stat formulas, including `P()/Q()`.
3. Port `game.g` inventory/pet/currency model.
4. Port `ar` RMS abstraction as file-backed save stores in Java SE/MIDP rebuild.
5. Port `game.k` save/load slot schema.
6. Port `game.h` bag/shop/task/pet workflows on top of UI runtime.
7. Port `game.d` battle init and basic turn/attack loop.
8. Add skill/status-specific battle effects incrementally from `aq.c[1]` and `bufDebuf.mid`.

## 9. Remaining Audit

- Pixel/timing validation for battle animation scripts (`effect.mid`, `speffect.mid`, `bufDebuf.mid`, `blood.mid`).
- End-to-end UI validation for battle pet switch/state `5` and item return paths.
- Exact widget-id maps for bag/shop/task/battle UI and which `game.h` method writes each id.
- Save slot `5 PK6_RMS_CNTSMS` side effects.
- Cross-check `game.k` save/load with actual RMS from emulator if available.
- Re-run battle classes through another decompiler if possible; several `game.d` blocks are CFR-damaged.
