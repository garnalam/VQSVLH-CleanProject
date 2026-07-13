# 235 - Item/inventory type family matrix

Date: 2026-07-13

Scope: audit-only map of item/inventory families after closing the normal
`aq.c[4]` item formulas and deciding to defer q.N special rows for now.

This document answers: "besides the first 14 normal items, what item-like
families exist, which source table owns them, which UI route uses them, and
what should we work on next?"

No runtime code was changed for this document.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - inventory vectors: `J`, `K`, `L`, `M`, `N`.
  - generic add/check/remove helpers:
    - `a(id, qty, byte)`
    - `b(id, qty, byte)`
    - `c(id, qty, byte)`
    - `d(id, qty, byte)`
  - equipment helpers:
    - `l(equipmentId)`
    - `f(equipmentId, petIndex)`
  - q.N special helpers:
    - `i(id)`, `c(id, qty)`, `e(id, petIndex)`, `j(id)`, `k(id)`.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `Y()`, `bj()`, `bk()`, `bl()` render `/data/ui/bag.ui`.
  - `ac()` confirms rows in `bag.ui`.
  - `be()` and `bd()` render `/data/ui/choice.ui` for petsetting item and
    equipment lists.
  - `bg()/bh()` handle `/data/ui/evolve.ui`.
  - battle item/catch/shop UI paths also read item rows from `aq.c[4]`.
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `x(itemId)` validates normal item use.
  - `w(itemId)` applies normal item behavior formulas.
- `modules/script/decoded/data__script__db.mid.json`
  - `aq.c[3]`: equipment/material-style metadata.
  - `aq.c[4]`: normal item table.
  - `aq.c[5]`: q.N special/meta table.

Related docs:

- `rebuild_plan/227_battle_item_full_completion_matrix.md`
- `rebuild_plan/231_battle_panel_item_full_table_audit.md`
- `rebuild_plan/234_panel_qN_special_items_remaining_type_matrix.md`
- `rebuild_plan/177_panel_petsetting_choice_item_equipment_audit.md`
- `rebuild_plan/182_panel_petsetting_equipment_confirm_slice_closeout.md`

## Source table groups

`data__script__db.mid.json` has nine groups:

| aq.c group | Row count | Primary role | Inventory family? |
|---:|---:|---|---|
| `0` | `100` | species/base pet data, evolution target/material fields | not directly; drives evolve material requirements |
| `1` | `70` | skill table | no; battle skill system |
| `2` | `8` | buff/passive table | no; battle/unit status |
| `3` | `18` | equipment/material metadata | yes: `q.L` and `q.M` |
| `4` | `15` | normal item table | yes: `q.K` balls and `q.J` normal items |
| `5` | `11` | q.N special/meta rows | yes: `q.N`, deferred for now |
| `6` | `15` | buff/debuff active effect table | no; battle status/effect |
| `7` | `11` | debuff/status hook table | no; battle formula/status |
| `8` | `4` | species/skill learn/evolution helper table | no direct inventory route seen in this audit |

Important rule: do not treat every `aq.c` table as an item table. Current
inventory families proven by source are `aq.c[3]`, `aq.c[4]`, and `aq.c[5]`.

## Source owner vectors

| Source vector | Source table | Row shape | Meaning | Primary UI routes | Current rebuild status |
|---|---|---|---|---|---|
| `q.K` | `aq.c[4]` where behavior `row[5] == 0` | `[itemId, count, flag]` | ball/catch items | battle P21/P17, bag tab 0, P11 shop | PORTED/PARTIAL |
| `q.J` | `aq.c[4]` where behavior `row[5] != 0` | `[itemId, count, flag]` | normal usable items | battle P4/P16, bag state17, petsetting c0, P11 shop | PORTED/SMOKE-LOCKED for formulas, PORTED/PARTIAL for UI/runtime parity |
| `q.L` | `aq.c[3]` ids `< 12` | `[equipmentId, equippedFlag, 0]` | equipment/accessories | bag tab 1, petsetting c2 choice.ui | PORTED/PARTIAL |
| `q.M` | `aq.c[3]` ids `>= 12` | `[materialId, count, flag]` | material/key/token stack | bag tab 2, evolve material checks | PORTED/PARTIAL for evolve material count/consume, broader q.M routes PENDING |
| `q.N` | `aq.c[5]` | `[specialId, activeFlag, stackCount]` | egg, ride, stones, badge/record, transmit | bag tab 3 and special states | DEFERRED; see doc 234 |

Source add/remove route by byte parameter:

| Source call | Bucket decision |
|---|---|
| `q.c(id, qty, (byte)0)` | if `aq.c[4][id][5] == 0`, add to `K`; otherwise add to `J` |
| `q.d(id, qty, (byte)0)` | remove from `K` or `J` by the same behavior rule |
| `q.c(id, qty, (byte)2)` | if `id < 12`, add equipment row to `L`; if `id >= 12`, stack in `M` |
| `q.d(id, qty, (byte)2)` | remove/consume from `M`; equipment rows use equip/unequip helpers instead |

## UI family matrix

| Family | Source UI/state | Source data | Core logic | Current status | Next useful slice |
|---|---|---|---|---|---|
| Ball/catch items | battle P21/P17, `choice.ui`, bag tab 0 cannot-use | `q.K`, `aq.c[4]` behavior `0` | catch chance param `row[6]`, consume ball, caught-pet storage | PORTED/PARTIAL | only return when catch RNG/animation/storage parity is target |
| Normal heal/PP/revive/clear items | battle P4/P16, panel state17, petsetting c0 | `q.J`, `aq.c[4]` behaviors `1..5` | `game.b.x/w` formula/validation/consume | PORTED/SMOKE-LOCKED for formulas and sensitive warnings | continue only for route/UI parity, not formula |
| Top-level special normal items | bag tab 0 | `aq.c[4]` behaviors `9/10` item `14/13` | item 13 avoid timer; item 14 egg accelerator | PORTED/SMOKE-LOCKED for item-use branches | item 14 after-battle egg progress remains separate |
| Equipment/accessories | bag tab 1, petsetting c2 | `q.L`, `aq.c[3]` ids `< 12` | equip, unequip, transfer, pet `c[5]` | PORTED/PARTIAL | battle stat/passive effect from equipment, or route acquisition/save regression if needed |
| Materials/keys/tokens | bag tab 2, evolve.ui | `q.M`, `aq.c[3]` ids `>= 12` | count/consume material; key/token display | PORTED/PARTIAL for evolve material count/consume only | audit q.M remaining rows and key/state routes, if material family is next |
| q.N special/meta | bag tab 3 | `q.N`, `aq.c[5]` | egg/ride/stones/badge/transmit | DEFERRED | state 12 or state 24 audit later, not now |
| Shop/purchase wrappers | P11 battle shop, panel shops, SMS source branches | `aq.c[4]`, sometimes `aq.c[5][0]` egg | add item/special row; source had money/SMS checks | PORTED/PARTIAL, PC policy removes SMS payment | audit only when a specific shop/NPC is targeted |
| Event rewards | decoded event ops/source script helpers | mostly `aq.c[4]`, `aq.c[3]`, `aq.c[5]` | add/remove item/equipment/special counts | PORTED/PARTIAL for scripted routes | generic event VM later, not item-family work |

## Normal item family recap

This family is already the most complete one.

| Source item ids | Bucket | Behavior | Meaning | Status |
|---|---|---:|---|---|
| `0..3` | `q.K` | `0` | catch balls | PORTED/PARTIAL |
| `4,5` | `q.J` | `1` | HP heal formula | PORTED/SMOKE-LOCKED |
| `6,7` | `q.J` | `2` | PP/skill value restore | PORTED/SMOKE-LOCKED |
| `8,9` | `q.J` | `3` | HP + PP restore | PORTED/SMOKE-LOCKED |
| `10` | `q.J` | `5` | clear bad effects/debuffs | PORTED/SMOKE-LOCKED |
| `11,12` | `q.J` | `4` | revive + HP/PP restore | PORTED/SMOKE-LOCKED |
| `13` | `q.J` | `10` | avoid-monster top-level item | PORTED/SMOKE-LOCKED |
| `14` | `q.J` | `9` | egg accelerator top-level item | PORTED/SMOKE-LOCKED for item-use branch |

Current conclusion: do not keep improving normal item formulas unless a bug is
found. The formulas and warnings have enough smoke coverage to move on.

## Equipment family (`q.L`, `aq.c[3]` ids `< 12`)

Source facts:

- `bag.ui` tab `b == 1` renders `q.L`.
- `petsetting c == 2` opens `choice.ui` and renders the same `q.L` rows.
- `q.L` rows are `[equipmentId, equippedFlag, 0]`.
- `game.g.f(equipmentId, petIndex)`:
  - clears selected pet's old `c[5]`;
  - if target equipment is worn elsewhere, clears previous holder;
  - marks selected `q.L` row flag to `1`;
  - sets selected pet `c[5] = equipmentId`.
- `game.g.l(equipmentId)` sets the `q.L` row equipped flag back to `0`.

Current rebuild status:

- `petsetting c=2` choice render/confirm/equip/unequip/transfer:
  PORTED/PARTIAL.
- q.L save/load has focused smoke in the current codebase.
- battle stat/passive effect from equipment is still PENDING unless later docs
  prove otherwise.
- bank/storage equipment transfer remains PARTIAL.

Recommended next if choosing equipment:

1. Create an audit for `aq.c[3]` ids `< 12` field meaning.
2. Map each equipment row to stat/passive effect in source `game.b/game.g`.
3. Only then wire battle stat effects.

## Material/key family (`q.M`, `aq.c[3]` ids `>= 12`)

Source facts:

- `bag.ui` tab `b == 2` renders `q.M`.
- `q.M` rows are count stacks from `q.c(id, qty, (byte)2)` when `id >= 12`.
- Render uses:
  - icon/name/description from `aq.c[3][id]`;
  - special name override for id `17`: `Chia khoa vang`;
  - count text from row `[1]`.
- `evolve.ui` uses species table fields:
  - material id = `aq.c[0][species][20] + 12`;
  - material need = `aq.c[0][species][21]`;
  - target species = `aq.c[0][species][19]`.
- On successful evolution/mutation source consumes material with
  `q.d(materialId, materialNeed, (byte)2)`.

Current rebuild status:

- Evolution UI material count/consume is PORTED/PARTIAL.
- Warning branches for no target, low level, and missing material have smoke.
- q.M bag tab 2 as a broader inventory UI family is not fully audited here.
- Non-evolution uses for q.M keys/tokens are UNKNOWN/PENDING.

Recommended next if choosing material/key family:

1. Create `236_panel_qM_material_key_rows_audit.md`.
2. List `aq.c[3]` ids `12..17` raw rows, names, descriptions, and every source
   consumer.
3. Split evolution materials from key/quest tokens before coding.

## q.N family (`aq.c[5]`)

Deferred by user decision.

Current summary:

- id `0`: egg lifecycle, PORTED/PARTIAL.
- ids `1..4`: ride unlock rows, PORTED/PARTIAL.
- id `5`: ride UI opener, PORTED/PARTIAL.
- ids `7/8/9`: pet-target special stones, PORTED/PARTIAL.
- id `6`: badge/record state 12, PENDING.
- id `10`: transmit state 24, PENDING.

Use `rebuild_plan/234_panel_qN_special_items_remaining_type_matrix.md` when we
return to this layer.

## Non-item tables that can look item-like

| Table | Why it looks item-like | Actual current role |
|---|---|---|
| `aq.c[2]` | has text/icon-ish rows and numeric params | passive/buff formula table used by battle/unit logic |
| `aq.c[6]` | has text/effect rows | active buff/debuff queue behavior |
| `aq.c[7]` | has status/debuff rows | battle formula/status hooks |
| `aq.c[8]` | compact helper rows | species/skill learn/evolution helper, not a bag inventory owner |

Do not port these under the item/inventory roadmap unless a source method shows
they are rendered in bag/shop/panel inventory UI.

## Recommended next work

Since q.N is deferred and normal item formulas are already smoke-locked, the
cleanest next item-family branch is:

1. **q.M material/key audit**
   - Create `236_panel_qM_material_key_rows_audit.md`.
   - Audit `aq.c[3]` ids `12..17`.
   - Read `bag.ui b == 2`, evolution consumers, and any key/quest/state routes.
   - No code first.

Alternative if the goal is battle accuracy instead of panel inventory:

1. **equipment battle-effect audit**
   - Audit `aq.c[3]` ids `< 12`.
   - Find where equipped `pet.c[5]` affects battle stats/passives.
   - Only code after the effect matrix is proven.

Do not do next:

- do not reopen normal heal/PP item formulas without a concrete bug;
- do not continue q.N state 12/24 until the user explicitly returns to q.N;
- do not port `aq.c[2/6/7/8]` as inventory items.

## Verification status

Audit-only document. No build, check, or smoke was required because no runtime
code changed.
