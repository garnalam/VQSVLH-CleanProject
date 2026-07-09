# 135 Battle P7 Actor Recoil Source Audit

## Scope

Audit the remaining P7 hit/recoil approximation after `134_battle_p7_hit_recoil_blood_timing.md`.

Goal:

- identify what `S.a/S.b` does during P7;
- identify where actor hit/recoil actually comes from;
- decide whether rebuild `lungeAmplitude/recoilAmplitude` is source-backed.

## Source Files Checked

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/d.java`
- `modules/source_code/decoded/bytecode_javap/game__d.javap.txt`
- `modules/source_code/decoded/bytecode_javap/game__b.javap.txt`
- `modules/source_code/decoded/bytecode_javap/f.javap.txt`

## Important Decompiler Note

There are two different obfuscated classes named `f`:

- default-package `f`, superclass of `game.b`;
- package class `game.f`, unrelated menu/runtime class.

For actor animation, the correct superclass is default-package `f`.

`game__d.javap.txt` confirms P7 calls:

```text
game/b.d:(B)V
game/b.p:()B
game/b.b:()Z
game/h.a:(Lgame/b;Z)Z
game/h.b:(Lgame/b;Z)Z
game/h.a:(Lgame/b;)V
game/h.b:(Lgame/b;)V
```

The `game/b.b:()Z` call resolves through default-package `f.b()`, which is:

```text
f.b() -> this.a.e()
d.e() -> current animation frame index q >= last frame r - 1
```

So `target.b()` in P7 means "target sprite animation has reached its last frame", not a movement/recoil offset helper.

## What `S.a/S.b` Means

`S` is `game.h`.

`game.h.a(b, boolean)` is the player-side battle HUD HP/energy refresh/tween.
`game.h.b(b, boolean)` is the enemy-side battle HUD HP refresh/tween.

Both methods:

- compute one tween step as `abs(displayHp - realHp) / 11`, minimum `1`;
- wait for `G < 4` before the tween starts, showing old/new percent widgets first;
- update `b.N()` through `b.u(int)`;
- update source `/data/ui/battle.ui` widgets:
  - player side: `38`, `40`, `41`, `55`, `11`, `9`, `12`, `13`, `17`;
  - enemy side: `39`, `42`, `56`, `14`, `15`, `16`, `18`, `19`;
- return `true` only when display HP reaches real HP.

Status: `PORTED/PARTIAL` in rebuild. HP mutation exists and HUD is source-shaped, but full `game.h + battle.ui` runtime remains partial.

## Where Actor Hit/Recoil Comes From

P7 damage section in `game.d` does this after damage text is queued:

```text
S.k = 0
if target.r() == 0:
    S.a(target)
else:
    S.b(target)
...
wait for:
    V() floating text queue complete
    S.a/S.b(target, false) HP tween complete
```

Target actor state is driven earlier by source flags:

```text
if target.u animation reaches condition:
    target.d((byte)2)
```

`game.b.d(byte)` state meanings relevant here:

| State | Source behavior | Status |
| --- | --- | --- |
| `0` | base/idle animation, `d.a(state, -1, true)` | PORTED/PARTIAL |
| `1` | action/attack animation, may create species-specific `L` effect | PORTED/PARTIAL |
| `2` | hit/recoil animation, `d.a(state, 0, true)` | PORTED/PARTIAL |
| `3` | death effect path, creates `ah` type 16 when battle mode allows | PARTIAL |
| `4` | base/alternate animation, `d.a(state, -1, true)` | PARTIAL |

Default-package `f` is the actual sprite wrapper:

```text
f.a() -> if visible, tick d.d()
f.b() -> d.e(), true when current animation reaches last frame
f.a(Graphics, x, y) -> draw d at fixed actor i/j
f.b(int,int) -> set actor i/j
```

The default-package `d` sprite animator:

```text
d.a(byte state, byte nextState, boolean reset)
d.d() advances frame q using per-frame delays from sprite anim table
d.e() returns q >= r - 1
d.a(Graphics, i, j, orientation) draws the current state/cell
```

## Rebuild Finding

Current rebuild had synthetic P7 offsets:

```text
lungeAmplitude = {0,2,5,8,10,8,5,2,0}
recoilAmplitude = {0,5,4,3,3,2,2,1,0}
```

No equivalent numeric offset table or helper was found in source P7.

Source actor motion is source-backed through:

- actor fixed `pos.mid` anchor;
- sprite state `1`/`2`/`3`;
- frame/cell offsets embedded in sprite/anim data;
- source `d.d()` frame timing.

Therefore the rebuild synthetic lunge/recoil offset arrays are `APPROX`, and keeping them risks adding motion that does not exist in source.

## Code Decision

Smallest source-backed fix:

- remove synthetic P7 lunge/recoil offsets;
- keep source actor animation states/cursors;
- keep HP tween and `blood.mid` damage text from previous slices.

Status after this slice:

| Area | Status | Note |
| --- | --- | --- |
| `S.a/S.b` meaning | PORTED/AUDITED | They are HP HUD tween/update, not actor recoil. |
| P7 actor anchor | PORTED/PARTIAL | Uses source `pos.mid` actor position. |
| P7 actor hit state `2` | PORTED/PARTIAL | Rebuild sets base state `2`; exact source sprite frame timing depends on full sprite runtime. |
| Synthetic lunge/recoil offsets | REMOVED | Not source-backed. |
| Exact sprite frame timing | PORTED/PARTIAL | Rebuild uses sprite animation cursors, but original-vs-rebuild frame compare still pending. |
| Pixel-perfect recoil | PENDING | Requires original frame capture compare. |

## Smoke Plan

Focused PNG:

- `battle_elder_p7_anim_start`
- `battle_elder_p7_lunge_peak`
- `battle_elder_p7_actor_u21_trigger_hit`
- `battle_elder_p7_damage_frame`
- `battle_elder_p7_recoil_peak`
- `battle_elder_p7_actor_u21_recover`

Regression:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Required checks:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- `git diff --check`

## Next Recommended Slice

Audit/port source sprite frame cursor timing for state `1` and state `2` by comparing default-package `d.d()/d.e()` against `SpriteAnim` cursor advancement in rebuild.
