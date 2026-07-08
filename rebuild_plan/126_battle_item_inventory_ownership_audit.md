# 126 Battle Item Inventory Ownership Audit

Status: SOURCE AUDIT ONLY / NO GAMEPLAY CODE CHANGE NEEDED.

Purpose:

- Audit P4/P16 item count ownership around `q.b(item,1,0)` and `game.g.o().d(item,1,0)`.
- Decide whether rebuild should consume item once in runtime, move consume into `BattleUnit.applyBattleItem()`, or change nothing.
- Scope is item ownership/count only. Do not change item formulas, P16 UI, catch animation, intro/world/panel.

## Source Files

- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

## Source Facts

| Source | Lines | Fact | Meaning |
| --- | ---: | --- | --- |
| `game.h` field/constructor | 19, 70..73 | `h.q` is assigned `game.g.o()`. | UI facade and battle unit methods share the same singleton inventory. |
| `game.g.o()` | 58..63 | Returns singleton `game.g`. | `this.q` and `game.g.o()` point to the same global state object. |
| `game.g.J/K/M` | 37..40 | `J`, `K`, `M` are inventory vectors. | `J` is normal items, `K` is balls when item behavior is 0, `M` is another resource group. |
| `game.g.b(int,int,Vector)` | 1216..1223 | Only checks `count - qty >= 0`; does not mutate. | `q.b(item,1,0)` is availability check only. |
| `game.g.d(int,int,Vector)` | 1241..1251 | Subtracts quantity and removes vector row if count <= 0 and flag == 0. | `q.d`/`game.g.o().d` is the real consume path. |
| `game.g.b(item,qty,byte)` | 1278..1285 | For byte 0, behavior 0 goes to `K`, all other item behaviors go to `J`. | Balls and normal items are separate source buckets. |
| `game.g.d(item,qty,byte)` | 1318..1325 | Same bucket routing as check path. | Consume uses same bucket as availability check. |

## P16 Source Chain

Source `game.h.bo()`:

| Step | Source line | Behavior |
| --- | ---: | --- |
| Check selected item count | 4092 | `this.q.b(this.s, 1, (byte)0)` |
| Apply to world pet | 4093..4094 | `this.q.z[this.c].w(this.s)` |
| Apply to battle pet | 4095..4097 | `((d)this.o).h.J = true`, then `this.q.z[((d)this.o).f[this.c]].w(this.s)` |
| Refresh target UI | 4099 | `this.e(this.c)` |
| Success msgwarm | 4100..4103 | Shows `Thành công sử dụng đạo cụ` |
| Missing item | 4106..4108 | `f = 2`, warning text |

Source `game.b.w(item)`:

| Step | Source line | Behavior |
| --- | ---: | --- |
| Apply behavior 1..6 | 1121..1158 | Heal/PP/revive/clear debuff/state flag |
| Consume global inventory | 1159 | `game.g.o().d(n2, 1, (byte)0)` |

Important conclusion:

- P16 source does **not** call `q.d(item,1,0)` in `game.h.bo()` directly.
- Instead, `game.h.bo()` checks availability through `q.b(...)`, then `game.b.w(item)` applies the item and consumes exactly once through `game.g.o().d(...)`.
- Because `h.q == game.g.o()`, this consumes from the same inventory checked by `q.b(...)`.

## Compare With Other Source Paths

| Flow | Source | Ownership pattern |
| --- | --- | --- |
| P21 catch list | `game.h.ai()` lines 3965..3974 | Check `q.b(ball,1,0)`, then explicitly `q.d(ball,1,0)`, then enter P17. |
| World special item behavior 13/14 | `game.h` lines 3112..3115, 3143..3145 | Check `q.b`, then explicitly `q.d`, because these paths do not call `game.b.w(item)`. |
| P16 item apply | `game.h.bo()` + `game.b.w()` | Check `q.b`, then `game.b.w` consumes via `game.g.o().d`. |

This explains why P16 looks different from catch/world-use paths: item-on-pet delegates consume to `game.b.w()`.

## Rebuild Mapping

| Rebuild | Source equivalent | Current status |
| --- | --- | --- |
| `s.sourceBagItems` | `game.g.J/K` compact source bucket proxy | PORTED/PARTIAL |
| `VqsvSourceOps.sourceCanRemoveItem()` / manual count check | `game.g.b(item,qty,byte)` | PORTED |
| `VqsvSourceOps.sourceRemoveItem()` | `game.g.d(item,qty,byte)` | PORTED |
| `BattleUnit.applyBattleItem()` | `game.b.w(item)` behavior body, excluding global singleton consume | PORTED/PARTIAL |
| `VqsvBattleRuntime.tickItemTarget()` calls `sourceRemoveItem()` once after `applyBattleItem()` | `game.b.w(item)` final `game.g.o().d(item,1,0)` | PORTED |
| `VqsvBattleRuntime.tickCatchList()` consumes ball before P17 | `game.h.ai()` explicit `q.d(ball,1,0)` | PORTED |

## Decision

Do **not** change P16 consume behavior right now.

Current rebuild consume-once behavior is source-correct for P16:

1. Runtime checks item count before apply.
2. `BattleUnit.applyBattleItem()` applies behavior only.
3. Runtime calls `VqsvSourceOps.sourceRemoveItem(..., 1)` exactly once.
4. Success msgwarm is shown after refresh, from closeout 125.

This maps to source:

1. `game.h.bo()` calls `q.b(item,1,0)`.
2. `game.b.w(item)` applies behavior.
3. `game.b.w(item)` calls `game.g.o().d(item,1,0)` exactly once.
4. `game.h.bo()` refreshes petstate and shows success msgwarm.

## Do Not Do

- Do not also consume inside `BattleUnit.applyBattleItem()` unless the runtime call is removed at the same time.
- Do not call `sourceRemoveItem()` before validation.
- Do not consume on validation warning.
- Do not reuse P21 catch consume timing for P16; source uses different ownership path.

## Remaining Gaps

| Gap | Status | Note |
| --- | --- | --- |
| `sourceBagItems` does not physically split normal item `J` and ball `K` vectors | PARTIAL | Behavior 0 rows are filtered into catch UI and non-zero rows into item UI, so early gameplay behavior is equivalent. |
| `BagItem.keepAtZero` for item0 SMS/free policy | REBUILD_POLICY | Already documented in catch closeout; not part of normal P16 item use. |
| Full save parity for vector row third field `nArray[2]` | PENDING | Source removal only deletes if count <= 0 and row flag == 0. Rebuild approximates with `keepAtZero`. |
| Full inventory UI grouping between `K + J` in world item UI | PENDING | Not needed for current P16 battle flow. |

## Next Roadmap Step

Phase 5 can continue without changing P16 item consume.

Next sensible slice:

1. Either close P4/P16 UI parity by moving toward full `choice.ui/petstate.ui/msgwarm.ui` widget runtime, or
2. Return to battle phase 5 roadmap item still open after P4/P16: P21/P17 remaining pixel/timing parity or P5 residual UI/runtime parity.

Recommendation:

- If staying on P4/P16, do `choice.ui` / `petstate.ui` widget-runtime parity next.
- If moving broader phase 5, audit remaining `P21/P17` catch animation/openbox/message runtime parity next.
