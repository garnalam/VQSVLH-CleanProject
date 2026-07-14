# Battle Buff6 Kien Nhan Source Oddity Audit

Date: 2026-07-13

Scope: source-first audit for buff6 `Kien nhan`, producer skill `35`.

Status: SOURCE_ODDITY PROVED, then superseded by user-approved
INTENTIONAL_DEVIATION / GAMEPLAY_FIXED.

This audit intentionally does not change gameplay code. Its job is to prove what
the original source actually does before adding dedicated buff6 closeout smokes.

## Files Read

| File | Purpose |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | Prove skill row `35` and buff row `6`. |
| `modules/script/decoded/data__script__effect.mid.json` | Prove producer visual row for skill35. |
| `modules/script/decoded/data__script__speffect.mid.json` | Prove speffect rows used by skill35 chunks. |
| `modules/script/original/bufDebuf.mid` | Check whether buff6 has a possible active queue visual row. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | Prove buff6 apply values and damage hook oddity. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | Prove no-damage producer route, post-effect text, and P12/P13 visual gate. |
| `modules/source_code/decoded/decompiled_source_cfr/ah.java` | Prove AH type1/type7 visual behavior shape. |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Current rebuild formula status. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Current smoke-only buff6 source-odd helper. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Current Phase9AA defensive hook checkpoint. |

## Raw Source Rows

From `data__script__db.mid.json`:

```text
skill35 = [3,152,564,0,1,10,1,6,-1,1]
buff6   = [339,354,3,50,-1]
```

Meaning:

- skill35 is a no-damage/default `effectMode == 1` producer.
- skill35 targets same side/self route and applies buff id `6`.
- buff6 duration is `3`.
- buff6 stores chance/value `50`.
- buff6 stores second param `-1`.

From `data__script__effect.mid.json`:

```text
effect.mid[35] =
  [0,1,4,0,-1,-1,0,
   0,1,17,0,-1,-1,0]
```

This is not identical to skill34. Skill35 has two visual chunks:

| Chunk | Row | Speffect |
| ---: | --- | --- |
| 0 | `[0,1,4,0,-1,-1,0]` | `speffect[4] = [7,0,4,2,9,10,11,10]` |
| 1 | `[0,1,17,0,-1,-1,0]` | `speffect[17] = [1,0,7,1,0,0]` |

Visual interpretation:

- chunk0 is AH type `7`: source `ah.java` creates a clone of the actor image,
  scales it to width `9/10` and height `11/10`, then alternates scaled clone and
  original by tick interval.
- chunk1 is AH type `1`: source `ah.java` creates an actor image plus a texture
  image (`aq.e[...]`) and applies the type1 texture/scroll transform path.
- Rebuild may smoke these rows, but without original-client capture this remains
  PORTED-PARTIAL, not pixel-perfect.

## Apply Logic

Source `game.b.a(byte by, int n2, int n3)` case `6`:

```text
case 6:
    this.v[6][1] = aq.c[6][6][3]   // 50
    this.v[6][2] = aq.c[6][6][4]   // -1
```

So the source stores:

```text
v[6][0] = duration = 3
v[6][1] = 50
v[6][2] = -1
v[6][4] = active flag
```

No immediate stat mutation is proven for case6. Source tick/current apply also has
case6 as a no-op.

## Damage Hook Oddity

Source `game.b.b(target)`:

```text
if (target.m(6) && ae.a(100) <= this.v[6][1]) {
    damage = damage * this.v[6][2] / 100;
}
```

Important: `target.m(6)` checks whether the defender has buff6, but the hook reads
`this.v[6][1]` and `this.v[6][2]` from the attacker.

This is the source oddity:

- Normal intuitive logic would read `target.v[6][1]` and `target.v[6][2]`.
- Source does not do that.
- If the attacker does not also have buff6 params loaded, attacker `v[6][1]` may
  be `0`, so the roll gate is effectively unreachable.
- If the attacker has buff6 params loaded, chance `50` can pass and the multiplier
  is `-1`, so the raw formula can become negative before later minimum clamp.

Do not "fix" this to intended design unless an original-client capture or another
source path proves the decompiler/source interpretation is wrong.

Current rebuild mirrors the odd shape in `BattleUnit.computeDamage()`:

```text
if target.hasBuff(6) && randomPercent("damage.buff6") <= attacker.buffSlots[6][1]:
    damage = damage * attacker.buffSlots[6][2] / 100
```

## P12/P13 Active Body Visual

Source `game.d` has:

```text
ai = {{3,5,13}, {0,1,2,3,8,9,10}}
```

Buff bank id `6` is not in `ai[0]`.

`bufDebuf.mid` does contain a possible row:

```text
ar[0][6] = 5
ap[5] = [1,4,0,-1,1,17,0,-1]
```

But source gate skips buff6 visuals because id `6` is not in `ai[0]`. Therefore:

- P12/P13 body visual for buff6: PENDING/NOT USED BY SOURCE GATE.
- Do not render `ap[5]` for buff6 active queue unless source gate evidence changes.

## Current Rebuild Coverage

Existing historical checkpoint:

```text
battle_phase9aa_defensive_hook_skill_35
```

It already proves, in a broad Phase9AA way:

- skill35 applies same-side buff6 through no-damage `game.d.q()` route;
- post-effect text appears;
- player has buff6 active;
- no P7 damage frame / no P7 hitroll for skill35;
- controlled source-odd damage hook can reduce/alter damage when attacker-side
  buff6 params are deliberately loaded.

Current gap:

- No dedicated table-order closeout for buff6.
- No dedicated skill35 visual timeline smoke for chunk0 `speffect4` plus chunk1
  `speffect17`.
- No dedicated before / producer / reduction success / reduction fail / expiry
  checkpoints in the current buff0-5 style.
- No explicit smoke asserting P12/P13 does not render buff6 body visual despite
  `bufDebuf.mid` having a row.

## Proposed Next Code Slice

Add dedicated buff6 smoke coverage without changing source-shaped behavior:

| Checkpoint | Purpose |
| --- | --- |
| `battle_status_buff6_before_no_effect` | Baseline: no buff6 value/duration/icon. |
| `battle_status_buff6_producer_visual` | Skill35 uses `effect.mid[35]`, applies buff6 value `50`, param `-1`, duration `3`, icon `18`; no damage/hitroll. |
| `battle_status_buff6_visual_chunk0_type7` | Assert chunk0 `speffect=4`, AH type7 visible. |
| `battle_status_buff6_visual_chunk1_type1` | Assert chunk1 `speffect=17`, AH type1 visible. |
| `battle_status_buff6_damage_reduction_success` | Force/seed roll so the user-approved gameplay reduction triggers; document numeric result honestly. |
| `battle_status_buff6_damage_reduction_fail` | Force/seed fail so target buff6 keeps baseline damage. |
| `battle_status_buff6_p12_no_body_visual` | Assert active queue skips body visual because `ai[0]` excludes id `6`. |
| `battle_status_buff6_expiry_clears_icon` | Duration/icon sequence `18/137 -> 18/136 -> 18/135 -> clear`. |

## Required Regression After Code Slice

```cmd
.\build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
java "-Dvqsv.modules=..\modules" -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo\suite_battle_quick
git diff --check
```

Also run focused buff6 smoke PNGs and mojibake scan for touched Java files.

## Classification

| Area | Status |
| --- | --- |
| Source rows | PROVED |
| Producer route | PROVED |
| Producer visual rows | PROVED |
| Buff apply values | PROVED |
| Damage hook formula | SOURCE_ODDITY / PROVED |
| P12/P13 body visual | PROVED skipped by source gate |
| Current rebuild broad support | PORTED-PARTIAL |
| Dedicated buff6 closeout | PASS |
| Pixel-perfect visual parity | PENDING |

## Closeout Result

Implemented dedicated smoke coverage:

- `battle_status_buff6_before_no_effect`
- `battle_status_buff6_producer_visual`
- `battle_status_buff6_visual_chunk0_type7`
- `battle_status_buff6_visual_chunk1_type1`
- `battle_status_buff6_damage_reduction_success`
- `battle_status_buff6_damage_reduction_fail`
- `battle_status_buff6_p12_no_body_visual`
- `battle_status_buff6_expiry_clears_icon`

Important correction found during smoke:

- Chunk0/chunk1 are visual-before-apply frames. The buff slot/icon is not yet
  active at those frames; the producer checkpoint asserts apply/value/icon at
  P7 phase 3 after `game.d.q()` postEffect.
- Source-oddity success/fail probes force baseline `damage.buff6` roll as well
  as the tested roll so the jitter RNG stream is comparable.
- P12 no-body visual must use the real skill35 route. Direct debug buff setup
  prepares the slot but does not drive the active queue.

## User-Approved Gameplay Override

After reviewing the source oddity, the user approved a cleaner gameplay rule:

- if the target has buff6 `Kien nhan`;
- roll `<= 50`;
- reduce incoming damage by `50%`;
- do not require attacker-side buff6 params.

New deterministic smoke result:

- `battle_status_buff6_damage_reduction_success`: baseline `80`, forced roll
  `0`, result `41` after half-damage plus normal jitter.
- `battle_status_buff6_damage_reduction_fail`: baseline `80`, forced roll
  `99`, result `80`.

Current honest status: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED. The original
source oddity remains documented above for archaeology, but the active rebuild
behavior intentionally follows the user-approved readable mechanic. Original-vs-
rebuild pixel comparison for the producer visual remains PENDING.

Next recommended step: follow table order with buff7 Linh Xao dedicated
closeout, source-first.
