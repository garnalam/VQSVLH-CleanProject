# 410 - Panel Evolution Material Acquisition Route Audit

Date: 2026-07-16

Purpose: finish the follow-up from `409`: find source routes that award/sell
`aq.c[3]` material ids `12..16`, then patch rebuild callsites so material counts
land in `sourceMaterialItems` instead of normal bag or special rewards.

## Source Inventory Truth

Already locked in `409`:

```text
evolve.ui count/confirm -> game.j.a(materialId, (byte)2)
material table        -> aq.c[3]
rebuild bucket        -> Scene.sourceMaterialItems
```

This file audits acquisition routes only.

## Route 1: Event Opcode 18 Material Reward/Remove

Source: `modules/source_code/decoded/decompiled_source_cfr/game/e.java`, case `18`.

Source behavior:

```text
args = [mode, materialId, qty]

mode 0:
    if K.a(materialId, qty, (byte)2):
        show "Đạt được: " + aq.c[3][materialId][0]
        K.c(materialId, qty, (byte)2)
    else:
        show bag-full warning

mode 1:
    show "Mất: " + aq.c[3][materialId][0]
    K.d(materialId, qty, (byte)2)
```

Source `game.j.c(id, qty, (byte)2)` special case:

```text
id >= 12 -> stack in material/key vector
id == 17 -> stored qty is qty * 5
id < 12  -> held/equipment ownership row, not material stack
```

Decoded event usage found:

| Decoded event | Args | Meaning |
|---|---:|---|
| `modules/event/decoded/data__event__scene_5.mid.json` | `[0,15,1]` | Award `Hồn Tinh Thạch` x1 |
| `modules/event/decoded/data__event__scene_11.mid.json` | `[0,12,2]` | Award `Tinh Nguyên Thạch` x2 |

## Route 2: Material Shop

Source: `modules/source_code/decoded/decompiled_source_cfr/game/l.java`.

Observed calls:

```text
actor type 20 -> game.k.a(3, (byte)2)
state 32      -> game.k.a(3, (byte)2)
```

This opens generic `shopbuy.ui` over table `aq.c[3]`, not the PC rebuild
`bodyShop/shopbuy` route that sells `aq.c[4]` normal items.

Important decision:

- Do not merge `aq.c[3]` material shop into current `bodyShop` / portable shop.
- Numeric row ids overlap between tables, so treating `aq.c[4][12]` as
  `aq.c[3][12]` would be wrong.
- Rebuild must keep a separate source-shaped material shop context:
  `shopTable=3`, `shopBucket=2`.
- The current PC `bodyShop` entry remains `shopTable=4`, `shopBucket=0`.

## Implemented Runtime Patch

Code touched:

- `VqsvSourceOps`
  - added `op18Material(mode, materialId, qty)`
  - added `sourceMaterialCount`
  - added source-backed add-cap check for material stack
  - keeps id `17` source rule: `qty` stores as `qty * 5`
- `VqsvIntroDemo.Scene`
  - added `op18Material(...)` wrapper for scene scripts.
- `VqsvBattleRuntime`
  - battle level-up evolution queue now reads material count through
    `VqsvSourceEvolutionRuntime.materialCount()`, so P22 queue and panel
    `evolve.ui` agree on the same bucket.
- `VqsvSmokeHarness`
  - added `source_op18_material_add_remove_bucket`.
  - added `panel_material_shop_buy_tinh_nguyen_evolve_bucket`.
- `VqsvPanelRuntime`
  - added source shop context for `shopbuy.ui`: normal item shop remains
    `game.k.a(byte4,0)` / `aq.c[4]`; material shop uses
    `game.k.a(3,(byte)2)` / `aq.c[3]`.
  - material shop confirm-buy writes to `sourceMaterialItems`.
  - material id `17` keeps the source stored-quantity rule (`qty * 5`).
  - PC free-all policy is preserved; original source display price is retained
    in trace via `originalPrice`.

## Smoke Result

Command:

```powershell
cd E:\VQSVLH-CleanProject\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint source_op18_material_add_remove_bucket .\build_intro_demo\material_acquisition_410\source_op18_material_add_remove_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint panel_material_shop_buy_tinh_nguyen_evolve_bucket .\build_intro_demo\material_acquisition_410\panel_material_shop_buy_tinh_nguyen_evolve_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint world_evolution_confirm_success_mutate .\build_intro_demo\material_acquisition_410\world_success_material_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint panel_petstate_petsetting_evolve_success_mutate .\build_intro_demo\material_acquisition_410\panel_success_material_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint panel_petstate_petsetting_evolve_no_material_warning .\build_intro_demo\material_acquisition_410\panel_no_material_wrong_buckets.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
```

Result:

```text
source_op18_material_add_remove_bucket: PASS
  material12=1
  material17=5
  bag12=0

world_evolution_confirm_success_mutate: PASS
panel_petstate_petsetting_evolve_success_mutate: PASS
panel_petstate_petsetting_evolve_no_material_warning: PASS
panel_material_shop_buy_tinh_nguyen_evolve_bucket: PASS
  route=game.k.a(3,(byte)2)
  bought aq.c[3][12] Tinh Nguyen Thach x1
  sourceMaterialItems[12]=1
  normal bag item12=0
  Dien Mieu evolve notice material=1/1
release check: PASS
```

## Current Status

| Area | Status | Notes |
|---|---|---|
| Event material reward/remove opcode 18 | PORTED | Wrapper exists and smoke proves bucket split. |
| Battle P22 evolution queue material count | PORTED | Now uses same material bucket as panel evolve. |
| Panel/world evolve confirm count/consume | PORTED | Done in `409`. |
| Material shop `game.k.a(3,(byte)2)` shopbuy runtime | PORTED/PARTIAL | Source-shaped `shopTable=3`, `shopBucket=2`; smoke proves buy id12 feeds evolve bucket. |
| World actor/state bridge to material shop | PENDING | Rebuild does not yet expose the real actor type20/state32 route in normal world play. |
| Current PC bodyShop/shopbuy | DO NOT REPURPOSE | It is `aq.c[4]` normal item route, not material stones. |

## Next Recommended Slice

When the map/NPC route reaches source actor type `20` or state `32`, connect it
to the already-ported material shop context:

```text
world actor/type route -> game.k.a(3,(byte)2) -> shopbuy.ui over aq.c[3] -> sourceMaterialItems
```

Do not touch current `bodyShop` normal-item shop unless source route proves it is
the same table. Add a route smoke once the actual world actor bridge exists.
