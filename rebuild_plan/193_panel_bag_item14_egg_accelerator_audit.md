# 193 - Panel bag itemId 14 egg accelerator audit

## Scope

Audit-only. No runtime code was changed in this slice.

Target branch:

- Top-level panel `gamemenu b=2 -> P=8 -> /data/ui/bag.ui`
- `game.h.Y()` opens bag tab `b=0`
- `game.h.ac()` confirms `bag.ui` item tab row with `itemId == 14`

This is not the full egg hatch action. Source item 14 only accelerates/sets egg hatch progress. The actual hatch action is in a different branch: `bag.ui` tab `b == 3`, `q.N` row `case 0`.

## Source files read

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

## Source branch: bag.ui itemId 14

From `game.h.ac()`, tab `b == 0`, selected item row from `q.K + q.J`:

```java
case 14: {
    if (this.f != 0) break;
    if (this.q.k(0)
            && (this.q.I == 0 && game.k.q < 10
            || this.q.I > 0 && game.k.q < 30)) {
        if (!this.q.b(v1[0], 1, (byte)0)) break;
        game.k.q = this.q.I == 0 ? 10 : 30;
        this.q.d(v1[0], 1, (byte)0);
        // clamp h/w, refresh bk()
        this.p.a("/data/ui/msgwarm.ui", 257, this);
        this.a("Thanh cong su dung, tranh thu thoi gian di ap trung trung sung vat a!",
                "Nhan nut 5 de tiep tuc");
        this.f = 1;
        break;
    }
    this.p.a("/data/ui/msgwarm.ui", 257, this);
    this.a("Khong co trung co the ap trung", "Nhan nut 5 de tiep tuc");
    this.f = 1;
    break;
}
```

Exact decompiled Unicode strings:

- Success: `Thành công sử dụng, tranh thủ thời gian đi ấp trứng trứng sủng vật a!`
- Warning: `Không có trứng có thể ấp trứng`
- Prompt: `Nhấn nút 5 để tiếp tục`

## Source state facts

| Source | Meaning | Notes |
| --- | --- | --- |
| `q.K` / `q.J` | item tab rows | `q.K` if `aq.c[4][id][5] == 0`, else `q.J`. |
| `q.b(item,1,0)` | has enough item | Uses `game.g.b(...)`, dispatching by `aq.c[4][item][5]`. |
| `q.d(item,1,0)` | remove one item | Removes row if count <= 0 and row flag is removable. |
| `q.N` | special/egg/ride-like rows | `q.k(0)` scans this vector, not `q.K/q.J`. |
| `q.k(0)` | active egg available | True when `q.N` contains row `[0,1,*]`. |
| `q.I` | egg/hatch type/progress tier | Saved by source `game.k`; `0` uses threshold 10, `>0` uses threshold 30. |
| `game.k.q` | hatch progress counter | Saved by source `game.k`; reset on game static init; incremented elsewhere. |
| `game.k.r()` | full hatch-ready predicate | `q.I == 0 && game.k.q >= 10 || q.I > 0 && game.k.q >= 30`. |

## How egg progress normally changes

Source `game.d` after battle/EXP processing:

```java
if (p.k(0)) {
    if (game.d.p.I == 0) {
        if (b3.s() >= 30 && ++game.k.q >= 10) {
            game.k.q = 10;
            return;
        }
    } else if (b3.s() >= 40 && ++game.k.q >= 30) {
        game.k.q = 30;
    }
}
```

So item 14 is a shortcut:

- if active egg type `q.I == 0`, set `game.k.q = 10`;
- if active egg type `q.I > 0`, set `game.k.q = 30`.

It does not directly add a pet or run hatch RNG.

## Where active egg comes from

Source `game.g`:

- `N` rows are special rows.
- `i(0)` adds `[0,0,0]`.
- `k(0)` returns true only if row id `0` has state `1`.
- `e(0, -1)` can switch row id `0` to state `1`.

Source `game.h.aP()` egg shop path:

```java
if (this.q.t(5000)) {
    if (this.q.k(0)) {
        msgwarm "Da co trung..., khong can mua sam"
    } else {
        this.q.e(0, -1);
        msgwarm "Da thanh cong mua sam #2 trung..."
    }
}
```

This confirms item 14 should not invent an egg. If `q.k(0)` is false, source shows the warning and does not consume item 14.

## Actual hatch action is separate

Still in `game.h.ac()`, but tab `b == 3`, selected `q.N` row `case 0`:

1. If `!q.k(0)`: no hatch action.
2. If `game.k.a().r()` is true:
   - if `q.y() == 2`: show space warning.
   - else set `game.k.q = 0`;
   - update map actor state if current map is `(4,5)`;
   - `q.j(0)` closes egg state back to `[0,0,*]`;
   - refresh `bl()`;
   - show `Ấp trứng thành công`;
   - set `f = 2`.
3. On next confirm when `f == 2`:
   - if `q.I == 0`: hatch species `58`;
   - else random weighted species from `{0,56,58,95,72}` using weights `{76,52,28,4,0}`;
   - add to bag/bank/release according to `q.y()`;
   - display openbox-style result text;
   - set `f = 3`, then close/refresh.

This hatch flow is broader than item 14 and should not be included in the first item 14 port.

## Source item data

From `VqsvBattleTables.instance().item(14)` / source `aq.c[4][14]`:

| Field | Value |
| --- | --- |
| `nameTextId` | `277` |
| `iconId` | `41` |
| `descriptionTextId` | `294` |
| `priceOrValue` | `1` |
| `currencyOrType` | `1` |
| `behavior` / `aq.c[4][14][5]` | `9` |
| `paramA/B/C` | `0/0/0` |

Rebuild `sourceItem(14)` is currently still fallback unless patched.

## Current rebuild status

| Piece | Status |
| --- | --- |
| `bag.ui` open/render/navigation/back | PORTED/PARTIAL |
| `bag.ui` item ids `0..3` cannot-use warning | PORTED |
| `bag.ui` item id `13` avoid-monster pill | PORTED/PARTIAL |
| `sourceItem(14)` metadata | PENDING |
| `sourceEggActive` / `q.k(0)` state | PENDING |
| `sourceEggType` / `q.I` | PENDING |
| `sourceEggProgress` / `game.k.q` | PENDING |
| item 14 success/warning loop | PENDING |
| save/load egg progress state | PENDING |
| after-battle increment of `game.k.q` | PENDING |
| tab `b == 3` hatch action | PENDING |
| hatch RNG/add pet/bank/release result | PENDING |

## Recommended implementation slice

Smallest safe slice:

1. Add source-shaped fields:
   - `sourceEggActive` for `q.k(0)`.
   - `sourceEggType` for `q.I`.
   - `sourceEggProgress` for `game.k.q`.
2. Save/load these fields because source `game.k` persists both `q.I` and `game.k.q`.
3. Source-back `sourceItem(14)` from `VqsvBattleTables`.
4. In `VqsvPanelRuntime.tickBag()` add only item 14 branch:
   - if `!sourceEggActive`: show `Không có trứng có thể ấp trứng`, no consume.
   - if `sourceEggType == 0 && sourceEggProgress < 10`: consume one item 14, set progress 10, show success.
   - if `sourceEggType > 0 && sourceEggProgress < 30`: consume one item 14, set progress 30, show success.
   - otherwise show warning, no consume. Source collapses already-ready into the same warning because the top-level `if` fails.
5. Keep `msgwarm.ui` close behavior identical to item 13: close warning and remain in `bag.ui`.

Do not implement in this slice:

- after-battle `game.k.q++`;
- `bag.ui b == 3` hatch action;
- species hatch RNG;
- add pet/bank/release mutation.

## Smoke plan for first port slice

Focused PNG checkpoints:

- `panel_bag_item14_no_egg_warning`
  - seed item14 count 1, `sourceEggActive=false`
  - assert warning text, item count unchanged, progress unchanged.

- `panel_bag_item14_type0_success`
  - seed item14 count 1, `sourceEggActive=true`, `sourceEggType=0`, `sourceEggProgress=0`
  - assert count decremented, progress becomes `10`, success message.

- `panel_bag_item14_type1_success`
  - seed item14 count 1, `sourceEggActive=true`, `sourceEggType=1`, `sourceEggProgress=0`
  - assert count decremented, progress becomes `30`, success message.

- `panel_bag_item14_already_ready_warning`
  - seed item14 count 1, `sourceEggActive=true`, `sourceEggType=0`, `sourceEggProgress=10`
  - assert warning text, count unchanged.

- `panel_bag_item14_success_returns_bag`
  - confirm success message, assert `msgwarm.ui` closes and `bag.ui` remains open.

Regression after code:

- existing item13 focused smoke;
- item `0..3` cannot-use smoke;
- `panel_bag_open_from_gamemenu`;
- save prompt smoke;
- petsetting item/equipment/skill/release/active/evolve smoke;
- `route_sophie_after_battle_branch`;
- `route_bunny_after_battle_task`;
- `route_elder_after_battle_reward_state`.

## Decision

Item 14 is safe to port as a narrow `bag.ui` warning/success loop only after adding source-shaped egg progress fields.

It is not safe to claim full egg runtime until `bag.ui b == 3`, `game.k.r()`, `q.j(0)`, `q.a(short)`, hatch RNG, add-to-bag/bank/release, and after-battle progress increments are audited/ported separately.
