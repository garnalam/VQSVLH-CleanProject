# 163 Battle Status Icon Sprite 325 / Img 804 Audit

Status: SOURCE AUDIT COMPLETE, CODE CHANGE NOT DONE IN THIS SLICE

## Question

`modules/img/decoded/data__img__img_804.mid.png` looks like a battle effect/status sheet.
Need to verify whether the original game draws this as an extra effect on the pet body when a skill applies buff/debuff/status.

## Asset Chain

| Asset | Source fact | Meaning | Status |
|---|---|---|---|
| `img_804.mid.png` | small sheet of status/effect-looking icons | source bitmap | VERIFIED |
| `sprite.mid` row | `script/decoded/data__script__sprite.mid.json:1318..1319` = `[325,804]` | image 804 is used by sprite id 325 | VERIFIED |
| `spr_325_all(r)` | `modules/spr/original/spr_325_all(r)` exists | sprite metadata/cells for icon sheet | VERIFIED |

## Original Source Callers

| Source | Lines | What it does | Status |
|---|---:|---|---|
| `game.h.d(int)` help/status page | `game/h.java:397..401` | creates sprite `325`, then selects cell `(page * 14 + row + 1)` for help/status icon list | VERIFIED |
| `game.h.a(b)` | `game/h.java:3564..3592` | enemy/right-side battle status slots: reset slot sprite `325` to cell `0`, then active buff uses `buffId + 12`, active debuff uses `debuffId + 1` | VERIFIED |
| `game.h.b(b)` | `game/h.java:3657..3673` and following same pattern | player/left-side battle status slots: same sprite `325` slot logic | VERIFIED |

## Actor Body Draw Path

| Source | Lines | Meaning | Sprite 325 involved? | Status |
|---|---:|---|---|---|
| `game.b.a(Graphics)` | `game/b.java:225..282` | draws species L effect before/after actor, actor sprite, and `u` actor/effect action | No direct sprite `325` call | VERIFIED |
| `game.d.b(Graphics)` P7/P12/P13 | `game/d.java:1841..1873` | draws battlefield, optional `H` special effect, actors, then floating text | No direct sprite `325` call | VERIFIED |
| `game.d.b(Graphics)` P17 catch | `game/d.java:1894..1902` | draws catch animation/effect via `H`/`aj` | No direct sprite `325` call | VERIFIED |

Conclusion: `img_804` / sprite `325` is not proven to be an effect attached directly to the pet body. Source-backed usage is battle UI status icon slots and help/status icon list.

## How Buff/Debuff Icons Work In Source

`game.b` stores active effect queues:

| Field | Source | Meaning | Status |
|---|---|---|---|
| `v[16][5]` | `game/b.java:60` | buff slots | VERIFIED |
| `w[11][5]` | `game/b.java:61` | debuff slots | VERIFIED |
| `x[2][3]` | `game/b.java:62` | active icon/order queues: `x[0]` buffs, `x[1]` debuffs | VERIFIED |
| queue add | `game/b.java:781..799` | adds buff/debuff id into first free queue slot, max 3 tracked | VERIFIED |

Source UI mapping in `game.h`:

| Active queue | Condition | Sprite 325 cell | Status |
|---|---|---:|---|
| empty slot | reset loop | `0` | VERIFIED |
| buff | `v1.x[0][i] != -1 && v1.v[id][0] > 0` | `id + 12` | VERIFIED |
| debuff | `v1.x[1][i] != -1 && v1.w[id][0] > 0` | `id + 1` | VERIFIED |

So when a skill applies a buff/debuff, the original game should show the corresponding small icon in the battle HUD status slots, not on top of the pet sprite itself.

## Rebuild Equivalent

| Rebuild file | Current behavior | Status |
|---|---|---|
| `VqsvBattleRenderer.drawStatusSlots(...)` | draws six slot backgrounds with sprite `325` cell `0` plus UI cell `145` | PARTIAL |
| `VqsvBattleUnit` | has `buffSlots`, `debuffSlots`, and `activeEffectQueue` equivalent to source `v/w/x` | PORTED/PARTIAL |
| `VqsvBattleRenderer` HUD | no proven rendering yet for actual active buff/debuff icon cells `buffId + 12` / `debuffId + 1` | MISSING |

## Answer To Current Question

There are two separate visual systems:

1. Skill/battle animation on or around pet:
   - Uses `game.d.H`, `game.b.L`, `game.b.u`, `ah.java`, `speffect.mid`, `effect.mid`.
   - This is where body/scene animation belongs.

2. Status icon UI:
   - Uses sprite `325` from `img_804`.
   - Drawn into battle HUD status slots by `game.h.a(b)` and `game.h.b(b)`.
   - Source does not show sprite `325` being attached to the pet body.

## Recommended Next Code Slice

Before continuing Phase 9-G skill coverage, add source-backed HUD status icon rendering:

1. Expose active buff/debuff icon cells from `VqsvBattleUnit`/runtime to `Scene`.
2. Update `VqsvBattleRenderer.drawStatusSlots(...)` to draw:
   - empty: cell `0`
   - active buff: `buffId + 12`
   - active debuff: `debuffId + 1`
3. Add smoke PNGs:
   - `battle_status_icon_debuff1_enemy.png`
   - `battle_status_icon_debuff2_enemy.png`
   - `battle_status_icon_buff9_player.png`
4. Regression:
   - build
   - `--check`
   - `VqsvBattleDamageFormulaCheck`
   - mojibake scan
   - route Sophie/Bunny/Elder smoke

## Open / Pending

| Item | Status |
|---|---|
| Pixel compare with MIDP original status slot placement | PENDING |
| Exact `m` sprite mode behavior for sprite 325 | PENDING |
| Whether all buff/debuff ids have correct visible icon cell in current sprite decoder | PENDING |
| Body-attached effect animation for each skill | separate Phase 9/P7 `effect.mid` + `speffect.mid` work, not sprite 325 | PENDING |
