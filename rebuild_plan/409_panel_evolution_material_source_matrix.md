# 409 - Panel Evolution Material Source Matrix

Date: 2026-07-16

Purpose: audit source truth for evolution/mutation material requirements before any
runtime fix. This closes the bug class where a material-looking row exists in the
wrong inventory bucket, so `evolve.ui` still shows `0/1`.

## Source Chain

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
  - `game.k.bl()` / evolve open path binds material UI:
    - `materialId = aq.c[0][species][20] + 12`
    - `materialNeed = aq.c[0][species][21]`
    - widget `45` reads name from `aq.c[3][materialId][0]`
    - widget `46` reads count from `this.q.a(materialId, (byte)2)`
  - `game.k.bp()` / confirm path:
    - checks `this.q.a(materialId, (byte)2) >= materialNeed`
    - consumes `this.q.d(materialId, materialNeed, (byte)2)`
    - starts `ah type10` evolution effect and later mutates pet payload.
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
  - `game.b.J()`/eligibility also checks level and
    `game.j.p().a(materialId, (byte)2)`.
- `modules/source_code/decoded/decompiled_source_cfr/game/j.java`
  - `(byte)2` count path reads vector `N`.
  - `(byte)2` add path sends ids `>= 12` to vector `N`; ids `< 12` are a
    separate equipment/held ownership path.

Important naming correction:

- Older notes sometimes called rows `12..17` `q.M`.
- The currently loaded CFR source shows `j.N` for `(byte)2` stack count, and
  rebuild represents that as `Scene.sourceMaterialItems`.
- `bag.ui` docs after panel closeout call this tab `q.N` material/key. That is
  the name this audit follows.

## Runtime Decision

Evolution material count and consume must use only:

```text
Scene.sourceMaterialItems  // source q.N / (byte)2 / aq.c[3] material-key stack
```

It must not fall back to:

- `sourceBagItems` / `aq.c[4]` normal items
- `sourceSpecialRewards` / q.O style special rows

Reason: source `game.k.bp()` does not check normal bag or special reward tables
for evolution material validation.

## Material Rows

| Id | Source name | Row | Role |
|---:|---|---|---|
| 12 | Tinh Nguyên Thạch | `[225,13,249,1000,0]` | Normal evolution material |
| 13 | Thiên Giới Tinh Thạch | `[226,14,250,2,1]` | Higher evolution material |
| 14 | Thiên Địa Thần Thạch | `[227,15,251,10,1]` | Rare evolution material |
| 15 | Hồn Tinh Thạch | `[228,16,252,2,1]` | Mutation/special evolution material |
| 16 | Quỷ Thần Tinh Thạch | `[229,17,253,10,1]` | Rare mutation/special evolution material |
| 17 | Chìa khóa / Chìa khóa vàng | `[230,18,254,...]` | Material/key bucket row, not an evolution requirement found in `aq.c[0][20]` |

## Điện Miêu Case

Source row:

```text
68 Điện Miêu -> 69 Thiểm Điện Miêu
targetKind = 1
required level = 12
raw material column20 = 0
materialId = 0 + 12 = 12
material = Tinh Nguyên Thạch
need = 1
```

So if the UI says Điện Miêu needs `0/1`, the required source material is
`Tinh Nguyên Thạch`, not `Hồn Tinh Thạch`. Buying/owning `Hồn Tinh Thạch`
id `15` does not satisfy Điện Miêu.

## Full Species Material Matrix

| Species | Current -> Target | Kind | Level | Material |
|---:|---|---:|---:|---|
| 6 | Long bảo bối -> Phệ Hỏa Thú | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 7 | Phệ Hỏa Thú -> Bạo Long Thú | 2 | 30 | 13 Thiên Giới Tinh Thạch x1 |
| 9 | Ly Ngưu Ngưu -> Ly Ngưu Thản Khắc | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 11 | Nhiệt Bạo Phong Tốc Khuyển -> Tuyệt Đối Linh Độ Khuyển | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 13 | Hỏa Diễm Tường Vân Khuyển -> Tà Vân Khuyển Thần | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 16 | Diệp Tán Oa -> Thụ Tán Oa | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 17 | Thụ Tán Oa -> Long Oa | 2 | 30 | 13 Thiên Giới Tinh Thạch x1 |
| 19 | Tiên Nhân Cầu Bảo Bảo -> Tiên Nhân Chưởng Thú | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 20 | Tiên Nhân Chưởng Thú -> Thiết Tý Phách Vương Thụ | 2 | 30 | 13 Thiên Giới Tinh Thạch x1 |
| 24 | La Phục Thảo -> La phục Oa Oa | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 26 | Tây Quan Tiểu Hương Trư -> Trư Lộc Điệp | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 35 | Toản Địa Khâu Dẫn -> Toái nham Khâu Dẫn | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 37 | Nham Nham Quy -> Kiếm Giáp Hạn Quy | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 39 | Bạch Châm Bảo Bảo -> Hắc Châm Yển Bảo Bảo | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 41 | Nham Sơn Long -> Zombie Nham Sơn Long | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 43 | Thổ Lang Chu Chu -> Độc Lang Chu | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 45 | Sừng tê giác bạo long -> Khủng giác bạo long | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 48 | Tuyết Cầu Bảo Bảo -> Người tuyết | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 49 | Người tuyết -> Tuyết Sơn Cự Linh | 2 | 30 | 13 Thiên Giới Tinh Thạch x1 |
| 51 | Thủy thủ chim cánh cụt -> Bá tước chim cánh cụt | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 52 | Bá tước chim cánh cụt -> Võ thần chim cánh cụt | 2 | 30 | 14 Thiên Địa Thần Thạch x1 |
| 60 | Tấn Cá Kiếm -> Cốt Cá Kiếm | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 65 | Lôi Vân Miêu -> Điện Nhãn Miêu | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 66 | Điện Nhãn Miêu -> Thân Sĩ Miêu | 2 | 30 | 13 Thiên Giới Tinh Thạch x1 |
| 68 | Điện Miêu -> Thiểm Điện Miêu | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 70 | Phù Du Điện Long -> Phù Du Quỷ Long | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 72 | Chuột Điện -> Chuột Lôi Điện | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 76 | Độc Ba Lợi -> Kịch Độc Quái | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 77 | Kịch Độc Quái -> Kịch Độc Khủng Thú | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 79 | Đan nhãn thú -> Độc nhãn quái | 1 | 12 | 12 Tinh Nguyên Thạch x1 |
| 80 | Độc nhãn quái -> Độc nhãn cự thần | 2 | 30 | 13 Thiên Giới Tinh Thạch x1 |
| 82 | Túi quỷ -> Thi Đại Quỷ | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 84 | Kính ma -> Phá kính tà linh | 3 | 5 | 16 Quỷ Thần Tinh Thạch x1 |
| 89 | Chim xanh -> Tai tước | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 91 | Hải âu -> Dực thần | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 93 | Hồng nhạn -> Phi Dực Thú | 3 | 5 | 15 Hồn Tinh Thạch x1 |
| 95 | Đậu ưng -> Liệt ưng | 1 | 12 | 12 Tinh Nguyên Thạch x2 |
| 97 | Quáng thạch dực long -> Tà ma dực long | 3 | 5 | 15 Hồn Tinh Thạch x1 |

## Implemented In This Slice

- `VqsvSourceEvolutionRuntime.materialCount()` now reads only
  `sourceMaterialItems`.
- `VqsvSourceEvolutionRuntime.consumeMaterial()` now consumes only
  `sourceMaterialItems`.
- `VqsvSaveRuntime` now saves/restores `sourceMaterialItems` as a dedicated
  `material.*` section.
- Evolution smokes now seed material through `VqsvSourceOps.sourceAddMaterial()`,
  not `sourceSpecialRewards`.
- No-material smoke deliberately seeds wrong buckets (`sourceBagItems` id 12 and
  `sourceSpecialRewards` id 12) and verifies evolution still reports missing
  material. This locks the source bucket split.

## Verification

Commands run:

```powershell
cd E:\VQSVLH-CleanProject\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint panel_petstate_petsetting_evolve_no_material_warning .\build_intro_demo\evolution_material_bucket\panel_no_material_wrong_buckets.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint panel_petstate_petsetting_evolve_success_mutate .\build_intro_demo\evolution_material_bucket\panel_success_material_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint world_evolution_confirm_success_mutate .\build_intro_demo\evolution_material_bucket\world_success_material_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint world_evolution_confirm_no_material .\build_intro_demo\evolution_material_bucket\world_no_material.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint world_evolution_confirm_level_low .\build_intro_demo\evolution_material_bucket\world_level_low_material_bucket.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
```

Result: all passed.

## Still Pending

- Event opcode 18 acquisition is now audited/ported in
  `410_panel_evolution_material_acquisition_route_audit.md`.
- `shopbuy.ui` can now run in two source contexts:
  `game.k.a(byte4,0)` for normal `aq.c[4]` items, and
  `game.k.a(3,(byte)2)` for `aq.c[3]` material stones.
- Generic material shop runtime `game.k.a(3,(byte)2)` is audited/ported in
  `410`; actual world actor type20/state32 bridge is still pending until that
  map/NPC route is ported.
- Exact `ah type10` evolution animation remains separate visual debt from prior
  evolution docs.

Next recommended slice:

When a source world actor/state route reaches material shop, connect it to the
ported `shopTable=3`, `shopBucket=2` panel runtime and add route smoke.
