# 117 Battle P21/P17 Catch Edge Recheck

Status: RECHECK / NO CODE CHANGE.

Scope: quay lại P21/P17 catch edge cases sau các lát `petstate.ui`,
`openbox.ui`, `msgwarm.ui`, P5/P16. Mục tiêu của file này là chốt lại thực tế
hiện tại, không tự claim quá tay.

## Source Facts

| Area | Source fact | Status |
| --- | --- | --- |
| P21 enter | `game.d case 21` set target `h.p = d[0]`, rồi gọi `game.h.ah()` | SOURCE CONFIRMED |
| P21 UI | `game.h.ah()` mở `/data/ui/choice.ui`, title `Pokemon ball`, subtitle `Tỉ lệ bắt`, action `Sử dụng` | SOURCE CONFIRMED |
| P21 rows | Rows lấy từ `q.K`, icon `aq.c[4][item][1]`, name `aq.c[4][item][0]`, chance `game.d.b(item)` | SOURCE CONFIRMED |
| P21 confirm count OK | `game.h.ai()` set `game.d.l`, gọi `o.m()`, consume `q.d(item,1,0)`, vào P17, đóng choice UI | SOURCE CONFIRMED |
| P21 missing count | Mở `/data/ui/msgwarm.ui`, text `Số lượng Pokemon ball không đủ`, local `f=1` | SOURCE CONFIRMED |
| P21 item 0 after warning | Nếu warning `f==1` và selected item id `0`, source đóng choice UI và vào state `101` purchase/SMS path | SOURCE CONFIRMED |
| Project policy | SMS purchase được coi là free, không cần gửi SMS; chỉ áp dụng khi source đã đi qua hook purchase/SMS | USER POLICY |
| P17 enter | `game.d case 17` load sprite `269`, đặt tại enemy, `q=0`, tính chance `b(l)`, force fail nếu `U==0 && V==5` | SOURCE CONFIRMED |
| P17 success | `game.g.y()` trả `0` bag, `1` bank, `2` full/release | SOURCE CONFIRMED |
| P17 success messages | Bag/bank: `Bắt thành công #2<name>`; bank có openbox thứ hai; full: release text | SOURCE CONFIRMED |
| P17 fail | q4 clear effect, restore enemy visible, nếu không catch thì tăng turn và về P1 | SOURCE CONFIRMED |

## Current Rebuild Mapping

| Behavior | Rebuild equivalent | Current status |
| --- | --- | --- |
| P21 choice list | `prepareCatchMenu()` + choice-style renderer | PORTED/PARTIAL |
| P21 move/confirm/back | `tickCatchList()` + shared menu input | PORTED |
| Missing non-item0 count | `NO_BALLS` warning then returns P21 | PORTED/PARTIAL |
| Item0 SMS/free policy | If selected item `0` count is 0, grants 1 ball and continues into P17 | PORTED/REBUILD_POLICY |
| P17 q0..q4 | `initCatchResult()` / `tickCatchResult()` phases | PORTED/PARTIAL |
| P17 q1/q4 H effect | Source-shaped type8/catch effect path | PORTED/PARTIAL |
| P17 fail restore | q4 clears catch visuals and restores enemy before P1 | PORTED/PARTIAL |
| Bag storage | Add caught `SourcePetState` to `sourcePets` when size `< 6` | PORTED/PARTIAL |
| Bank storage | Add caught `SourcePetState` to `sourcePetBank` when bag full and bank `< 100` | PORTED/PARTIAL |
| Full release | No add, show release openbox | PORTED/PARTIAL |
| Captured payload | `SourcePetState.caughtFromBattleUnit(...)` keeps `game.b.P()`-shaped payload | PORTED/PARTIAL |

## Recheck Smoke Status

These checkpoint PNGs were generated before this document, using smoke mode only:

- `rebuild_game/build_intro_demo/catch_edge_battle_catch_missing_count_warning.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_catch_missing_count_warning_return_p21.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_catch_p21_back_to_command.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_catch_sms_free_item0_p17.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_catch_storage_bag.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_catch_storage_bank.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_catch_storage_full_release.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_p17_q1_h_effect_order.png`
- `rebuild_game/build_intro_demo/catch_edge_battle_p17_q4_fail_restore_enemy.png`
- `rebuild_game/build_intro_demo/catch_edge_route_bunny_after_battle_task.png`

No live client should be opened for this slice. Future smoke should avoid the
`java -jar` path and use the safest available headless command only if the user
explicitly agrees.

## Remaining Partial / Pending

| Gap | Status | Why not complete |
| --- | --- | --- |
| Full state 101 SMS UI/runtime | PENDING/BYPASSED | Project policy says SMS can be free; full purchase UI still not ported. |
| Full generic `game.h` widget runtime for `choice.ui` | PARTIAL | Current renderer is source-shaped, not complete widget interpreter. |
| Full generic `msgwarm.ui`/`openbox.ui` runtime | PARTIAL | Recent slices improved frame/text behavior, but not every widget feature is generic. |
| P17 animation/effect pixel parity | PARTIAL | Source-shaped q0..q4 and type8 effect exist; exact MIDP frame/pixel comparison is not claimed. |
| Exact RNG stream parity | PARTIAL | Deterministic smoke hook exists; full original random stream replay remains broader RNG work. |
| Full `game.g` save/global inventory parity | PARTIAL | Runtime bag/bank behavior is covered; complete save/global model is broader than P17. |

## Next Roadmap Step

P21/P17 edge behavior is currently good enough to leave as PORTED/PARTIAL and
move forward. The next source-backed battle work should not be more catch polish
unless a user-visible bug appears. Roadmap-consistent next candidates:

1. Phase 5 UI parity: full `choice.ui` widget runtime if P21/P4/P5/P11 still
   feel visually off.
2. Phase 8/EXP/evolution continuation if the user is testing post-battle flow.
3. P17 exact animation/RNG only after a direct original-vs-rebuild comparison
   identifies concrete mismatch.

Do not touch intro/world/panel for this catch slice.
