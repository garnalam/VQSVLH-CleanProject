# 78 Battle Active Effect Lifecycle Full Matrix

Status: SOURCE AUDIT plus FIRST CODE SLICE for clear/reapply parity.

Scope: `game.b` active buff/debuff lifecycle used by `game.d` P12/P13 and P7
post-skill behavior.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java:482..582` | `a(byte buffId,int value,int skillId)` applies buffs into `v[buffId][0..4]`, inserts bank `x[0]`, stores duration from `aq.c[6][id][2]`. |
| `game/b.java:584..665` | `o(int buffId)` is the per-turn buff tick body used by P12/P13. |
| `game/b.java:672..710` | `C()`, `D()`, private `C(int)`, and queue removal `e(bank,slot)`. |
| `game/b.java:712..736` | `q(int debuffId)` is the per-turn debuff tick body used by P12/P13. |
| `game/b.java:738..759` | `c(debuffId,slot)` and `d(buffId,slot)` decrement duration, clear expired effect, remove queue slot. |
| `game/b.java:1368..1425` | Target debuff application writes `w[debuffId][0..4]`, inserts bank `x[1]`, applies immediate stat changes for debuff 5/7. |
| `game/b.java:1427..1440` | Damage formula hooks for active buff/debuff flags: buff 0/1/6/8 and debuff 6. |
| `game/b.java:1469..1470` | Buff 5 stores reflected damage through a post-damage chance path. |

## Slot Layout

| Source slot | Buff `v[id][slot]` | Debuff `w[id][slot]` |
| --- | --- | --- |
| `[0]` | remaining duration/counter | remaining duration/counter |
| `[1]` | primary stored value | primary stored value |
| `[2]` | secondary stored value | secondary stored value, rarely used |
| `[3]` | source skill/index | source skill id |
| `[4]` | active flag | active flag |

## Buff Lifecycle Matrix

| Buff id | Apply-time source behavior | P12/P13 `o(id)` tick | Formula/other hook | Clear behavior | Rebuild status |
| ---: | --- | --- | --- | --- | --- |
| 0 | Stores defense boost and extra damage value, raises defense immediately. | No per-turn body. | If active and duration counter is `0`, adds `v[0][2]` to damage. | `n(0)` clears active flag and resets attack/defense/speed to base. | PORTED/PARTIAL. Formula hook exists; no per-turn tick by source. |
| 1 | Lowers defense, stores percent damage boost value. | Reasserts defense = base defense - stored value. | Adds damage percent `v[1][2]`. | Same reset. | PORTED. |
| 2 | Raises defense. | Reasserts defense = base defense + stored value. | None found. | Same reset. | PORTED. |
| 3 | Heals by max HP percent at apply. | Heals by stored value each tick and returns heal text value. | None found. | Same reset. | PORTED. |
| 4 | Raises defense using source skill param. | Adds stored defense value to current defense. | None found. | Same reset. | PORTED/PARTIAL; cumulative behavior preserved in tick/reapply shape. |
| 5 | Stores chance value. | No per-turn body. | Post-damage reflect/store path if chance succeeds. | Same reset. | PORTED/PARTIAL; reflect hook exists through stored damage path, broad parity still pending. |
| 6 | Stores chance and damage percent. | No per-turn body. | Odd source behavior: target has buff6, but formula reads attacker `v[6][1]/[2]`. | Same reset. | PORTED with source oddity retained. |
| 7 | Raises speed using source skill param. | Reasserts speed = base speed + stored value. | None found. | Same reset. | PORTED. |
| 8 | Stores PP/damage value. | No per-turn body. | Extra skill PP consumption and damage boost. | Same reset. | PORTED/PARTIAL. |
| 9 | Raises speed, lowers defense. | Reasserts both speed up and defense down. | State dispatch can force P2 if active in rebuild; source also checks status paths. | Same reset. | PORTED/PARTIAL. |
| 10 | Raises attack. | Reasserts attack = base attack + stored value. | None found. | Same reset. | PORTED. |
| 11 | Copies active buffs from another unit and clears donor buffs. | Repeats copy/clear from referenced unit. | Multi-unit buff transfer. | Same reset. | PARTIAL/PENDING; rebuild has copy helper but no full source turn-vector donor parity. |
| 12 | Sets `K[12]=1`. | Sets `K[12]=2`. | Skill PP conservation state. | Same reset. | PORTED/PARTIAL via `effectScratch[12]`. |
| 13 | Heals and calls `C()` clear all debuffs. | Heals each tick. | Debuff cleanse. | Same reset. | PORTED. |
| 14 | Calls `C()` clear all debuffs. | No `o()` case. | Also blocks new target debuffs in source apply path. | Same reset. | PORTED/PARTIAL. |
| 15 | Stores `value * aq.c[6][15][3]`. | No visible `o()` case. | No confirmed hook in audited source slice. | Same reset. | PENDING/FLAG; no gameplay behavior added without source consumer. |

## Debuff Lifecycle Matrix

| Debuff id | Apply-time source behavior | P12/P13 `q(id)` tick | Formula/other hook | Clear behavior | Rebuild status |
| ---: | --- | --- | --- | --- | --- |
| 0 | Stores pre-skill raw damage. | Damage = `w[0][1] / aq.c[1][sourceSkill][8]`; dead unit enters state 3. | Conditional skills 3/9 use stronger damage if target has debuff0. | `C(0)` clears active flag and resets attack/defense/speed to base. | PORTED. |
| 1 | No stored value body. | Explicit return/no-op. | Conditional skills 23/29 use stronger damage if target has debuff1. | Same reset. | PORTED as flag/duration/visual only. |
| 2 | No stored value body. | Explicit return/no-op. | Source consumer not confirmed beyond active visual/flag. | Same reset. | PORTED as flag/duration/visual only; gameplay consumer PENDING. |
| 3 | Stores pre-skill raw damage. | If duration <= 1, damage = `w[3][1] * aq.c[1][sourceSkill][8] / 100`. | Delayed/late damage. | Same reset. | PORTED. |
| 4 | Stores source skill param. | Explicit return/no-op. | Source consumer not confirmed in this slice. | Same reset. | PARTIAL/FLAG. |
| 5 | Lowers speed immediately. | Reasserts speed = base speed - stored value. | None found. | Same reset. | PORTED. |
| 6 | Stores source skill param. | Explicit return/no-op. | Reduces incoming damage by `w[6][1]%`. | Same reset. | PORTED/PARTIAL. |
| 7 | Lowers defense immediately. | Reasserts defense = base defense - stored value. | None found. | Same reset. | PORTED. |
| 8 | No `q()` case in visible source. | Default no-op. | Source consumer not confirmed in this slice. | Same reset. | PORTED as flag/duration/visual only; gameplay consumer PENDING. |
| 9 | No `q()` case in visible source. | Default no-op. | Source consumer not confirmed in this slice. | Same reset. | PORTED as flag/duration/visual only; gameplay consumer PENDING. |
| 10 | No `q()` case in visible source. | Default no-op. | Source consumer not confirmed in this slice. | Same reset. | PORTED as flag/duration/visual only; gameplay consumer PENDING. |

## Clear / Reapply Semantics

Source `n(buffId)` and `C(debuffId)`:

- clear active flag;
- reset mutable stats `d[2]`, `d[3]`, `d[4]` to base `c[2]`, `c[3]`, `c[4]`.

Source `D()` clears all buffs and all buff queue slots. Source `C()` clears all
debuffs and all debuff queue slots.

Rebuild code slice:

- `clearBuffs()`, `clearDebuffs()`, `clearSourceBuff(id)`,
  `clearSourceDebuff(id)` now reset mutable stats and then reapply active
  stat effects that still have active flags.
- Reapply is intentionally limited to source tick stat cases:
  - buffs `1,2,4,7,9,10`;
  - debuffs `5,7`.
- It does not replay HP ticks, damage ticks, cleanse, copy, PP, or formula-only
  effects. Those are not stat reassertions in source `o/q`.

## Rebuild Code Mapping

| Rebuild method | Source equivalent | Status |
| --- | --- | --- |
| `BattleUnit.applySourceBuff(...)` | `game.b.a(byte,int,int)` | PORTED/PARTIAL; buff 11 donor vector parity still pending. |
| `BattleUnit.tickSourceBuff(...)` | `game.b.o(int)` + `d(int,slot)` | PORTED for visible source cases; no-op/flag ids preserved as no-op. |
| `BattleUnit.tickSourceDebuff(...)` | `game.b.q(int)` + `c(int,slot)` | PORTED for `0,3,5,7`; `1,2,8,9,10` intentionally no-op/flag. |
| `BattleUnit.clearBuffs()` | `game.b.D()` | PORTED/PARTIAL with deterministic reapply of remaining debuff stat effects. |
| `BattleUnit.clearDebuffs()` | `game.b.C()` | PORTED/PARTIAL with deterministic reapply of remaining buff stat effects. |
| `BattleUnit.clearSourceBuff/Debuff` | `game.b.n(int)` / private `C(int)` | PORTED/PARTIAL with reapply helper. |

## Code Evidence

- `VqsvBattleDamageFormulaCheck.checkActiveEffectClearReapplyStats()` asserts:
  - clearing debuffs preserves active buff10 attack boost;
  - clearing buffs preserves active debuff7 defense drop.
- Existing P12/P13 smoke coverage still passes after the reapply helper.

## Honest Remaining Gaps

- Buff 11 needs true multi-unit donor mapping from source battle vectors before
  it can be called full parity.
- Buff 15 and debuff `2/8/9/10` remain flags unless a source consumer is audited.
- Reapply order is source-shaped but deterministic; exact source turn-order
  interleaving can still differ when multiple stat effects expire in the same
  P12/P13 queue batch.
