# 201 - Panel Bag Default State17 Open/Navigation Closeout

Date: 2026-07-10

Scope: first implementation slice for top-level `bag.ui b=0` default item-use path.

## Source target

Source path from `game.h.ac()`:

```java
this.s = v1[0];
this.o.a((byte)17);
this.p.a("/data/ui/bag.ui");
```

Then `game.k` state 17 entry falls through to `S.W()`, which opens `/data/ui/petstate.ui`.

## Implemented

- PORTED/PARTIAL: top-level `bag.ui b=0` default item row stores selected `itemId` as state17 item.
- PORTED/PARTIAL: closes panel `bag.ui` and opens source-backed `petstate.ui` target selector.
- PORTED/PARTIAL: state17 navigation traces `game.h.Z key=4100/8448`.
- PORTED/PARTIAL: back closes `petstate.ui` and returns to `bag.ui b=0`.

## Explicitly not implemented in this slice

- PENDING: confirm `game.h.Z -> bo()` validation.
- PENDING: `game.b.x(itemId)` warning loop from this exact top-level state17 path.
- PENDING: `game.b.w(itemId)` mutation and inventory decrement from this exact path.
- PENDING: missing-count `f=2` return behavior.

Those behaviors already exist in a separate petsetting `c=0 -> choice.ui` path, but state17 must be wired separately because the source UI route is different.

## Verification

Build/check:

- `build.ps1`: pass.
- `java -Dvqsv.modules=..\modules -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: no matches.
- `git diff --check`: no whitespace errors; CRLF warnings only.

Focused PNG smoke:

- `panel_bag_default_item_state17_open_petstate`: pass.
- `panel_bag_default_item_state17_navigation`: pass.
- `panel_bag_default_item_state17_back_returns_bag`: pass.

Regression PNG smoke:

- `panel_bag_open_from_gamemenu`: pass.
- `panel_bag_navigation`: pass.
- `panel_bag_back_returns_gamemenu`: pass.
- `panel_bag_item_cannot_use_warning`: pass.
- `panel_bag_item13_success_msg`: pass.
- `panel_bag_item14_type0_success`: pass.
- `panel_petstate_petsetting_skill_open`: pass.
- `panel_save_success_status`: pass.
- `route_sophie_after_battle_branch`: pass.
- `route_bunny_after_battle_task`: pass.
- `route_elder_after_battle_reward_state`: pass.

## Next recommended slice

Implement state17 confirm warnings only:

- use stored `itemId`;
- selected pet from `petstate.ui`;
- call source-shaped validation equivalent of `game.b.x(itemId)`;
- show `msgwarm.ui`;
- close warning back to `petstate.ui`;
- no item mutation yet.
