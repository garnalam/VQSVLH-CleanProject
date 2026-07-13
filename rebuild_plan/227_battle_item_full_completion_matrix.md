# 227 - Battle Item Full Completion Matrix

Status: SOURCE AUDIT / ROADMAP MATRIX / NO RUNTIME CODE CHANGE.

Purpose:

- Create the controlling item roadmap before any more item coding.
- Map source item rows to every consumer path: battle, catch, panel bag,
  petsetting, shop, rewards, equipment, special rows, and save/load.
- Separate what is already `PORTED/PARTIAL` from what still needs focused
  smoke or source-backed code.

This is a handoff matrix for the next dev chat. Do not treat it as proof that
all item behavior is complete. It is the map for choosing the next small slice.

## Sources Read

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/script/decoded/data__script__db.mid.json`

Rebuild/runtime:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/BattleLabScenarios.ps1`

Relevant audits/closeouts:

- `79_battle_item_pet_catch_state_matrix.md`
- `114_battle_p16_source_reaudit_matrix.md`
- `126_battle_item_inventory_ownership_audit.md`
- `177_panel_petsetting_choice_item_equipment_audit.md`
- `179_panel_petsetting_item_choice_confirm_slice_closeout.md`
- `182_panel_petsetting_equipment_confirm_slice_closeout.md`
- `192_panel_bag_item13_avoid_monster_closeout.md`
- `193_panel_bag_item14_egg_accelerator_audit.md`
- `194_panel_bag_item14_egg_accelerator_closeout.md`
- `195_panel_bag_egg_hatch_qN_case0_closeout.md`
- `199_panel_ride_qh_mutation_closeout.md`
- `200_panel_bag_default_state17_audit.md`
- `203_panel_bag_default_state17_success_mutation_closeout.md`
- `204_panel_bag_default_state17_item_behavior_audit.md`
- `205_panel_item_5_12_source_metadata_closeout.md`
- `206_battle_p11_shop_item_reachability_audit.md`
- `207_battle_p11_shopbuy_msgyn_polish_closeout.md`
- `220_battle_lab_hub_npc_and_catch_plan.md`

## Source Item Table Schema

Source rows are `aq.c[4][itemId]`, decoded from db group `4`.

| Column | Meaning in current source paths |
| ---: | --- |
| `0` | text id for item name. |
| `1` | sprite/icon cell, rendered with sprite `258` in item/catch lists. |
| `2` | text id for item description. |
| `3` | price/value. Battle shop display uses source price basis, but PC rebuild policy makes current P11 free. |
| `4` | currency/type. Source uses money/badge/mobile-type branches. PC rebuild removes SMS/mobile payment. |
| `5` | item behavior/type. This decides inventory bucket and use path. |
| `6..8` | behavior params, depending on behavior. |

Source inventory bucket rule from `game.g.b/c/d(item, qty, byte 0)`:

- if `aq.c[4][item][5] == 0`, item lives in `q.K` ball vector;
- otherwise item lives in `q.J` normal item vector.

Equipment/accessory data is not group `4`; it uses group `3` and inventory
vector `q.L`.

Special/egg/ride tab rows are not group `4`; they use vector `q.N` and source
helpers such as `q.k(0)`, `q.h(ride)`, and `q.j(0)`.

## Full Source Item Rows

Raw rows from `data__script__db.mid.json` group `4`:

| itemId | raw source row | behavior | params | Primary meaning / use path |
| ---: | --- | ---: | --- | --- |
| `0` | `[261,25,278,2,2,0,9999]` | `0` | `9999` | Ball/catch item. P21/P17 catch. P11 source shop row. Top-level bag says cannot use. |
| `1` | `[262,26,279,50,0,0,100]` | `0` | `100` | Ball/catch item. Bunny route reward and P21/P17. Top-level bag says cannot use. |
| `2` | `[263,27,280,200,0,0,140]` | `0` | `140` | Stronger ball. Source-supported/P11 reachable; not current story-route reward. |
| `3` | `[264,28,281,1,1,0,200]` | `0` | `200` | Stronger ball / currency type 1 row. Source-supported/P11 reachable. |
| `4` | `[265,29,282,100,0,1,50,50]` | `1` | `50,50` | HP heal. Story rewards and state17/P16/petsetting item use. |
| `5` | `[266,30,283,250,0,1,100,0]` | `1` | `100,0` | Full/large HP heal. P11 reachable. |
| `6` | `[267,31,284,100,0,2,25]` | `2` | `25` | PP/skill-value restore. P11 reachable. |
| `7` | `[268,32,285,250,0,2,45]` | `2` | `45` | Stronger PP restore. P11 reachable. |
| `8` | `[269,33,286,250,0,3,50,50,20]` | `3` | `50,50,20` | HP + PP restore. P11 reachable. |
| `9` | `[270,34,287,500,0,3,100,0,45]` | `3` | `100,0,45` | Stronger HP + PP restore. P11 reachable. |
| `10` | `[271,35,288,50,0,5]` | `5` | none | Clear debuff/bad effects. P11 reachable. |
| `11` | `[272,36,289,300,0,4,50,50,20]` | `4` | `50,50,20` | Revive. Elder reward and P11 reachable. |
| `12` | `[273,37,290,750,0,4,100,0,45]` | `4` | `100,0,45` | Stronger revive. P11 reachable. |
| `13` | `[509,53,510,300,0,10,500]` | `10` | `500` | Avoid-monster pill. Special top-level bag branch, not state17. |
| `14` | `[277,41,294,1,1,9]` | `9` | none | Egg accelerator. Special top-level bag branch, not state17. |

Source `game.b.x/w` has behavior `6`, but current source item table group `4`
has no row with behavior `6`. Treat behavior `6` as source-supported but
table-unreachable until another source path proves an item row or generated
item can use it.

## Source Consumer Matrix

| Consumer path | Source entry | UI/assets | Inventory source | Mutation | Rebuild status | Main remaining gap |
| --- | --- | --- | --- | --- | --- | --- |
| Event reward add/remove | Event opcode `17`; current script helpers call `op17Item(mode,item,qty)`. | Reward/openbox/taskTip style UI. | `game.g.J/K` through item behavior bucket. | Add/remove item count. | `PORTED/PARTIAL` in `VqsvSourceOps.op17Item`. | Full decoded event VM consumer is still partial; current routes use script helpers. |
| Battle item list | `game.d` P4 -> `game.h.aj()/ak()`. | `/data/ui/choice.ui`, sprite `258`. | `q.J`, normal items only. | Choose item; behavior `7..10` blocked in battle. | `PORTED/PARTIAL`. | Full `choice.ui` VM/pixel parity partial; P4/P21 wheel/hover mapping verified. |
| Battle item target/use | `game.d` P16 -> `game.h.W()/al()/bo()`. | `/data/ui/petstate.ui`, `/data/ui/msgwarm.ui`. | `q.J`; `q.b` check, `game.b.w` consumes once. | `game.b.x/w` on selected pet, active turn side effect. | `PORTED/PARTIAL`. | Full petstate runtime and exact post-success active-turn routing remain partial. |
| Battle catch list | `game.d` P21 -> `game.h.ah()/ai()`. | `/data/ui/choice.ui`, sprite `258`, catch chance text. | `q.K`, ball items only. | Explicit `q.d(ball,1,0)` before P17. | `PORTED/PARTIAL`. | P101/mobile purchase removed/approximated for PC; full chance/RNG/pixel capture parity partial. |
| Battle catch result | `game.d` P17. | Battle scene, sprite `269`, `ah/H` effects, openbox/msgwarm. | Selected `game.d.l` ball id. | Add caught pet to bag/bank/full path through `game.g.y()`. | `PORTED/PARTIAL`. | Exact P17 animation/RNG and payload byte parity pending. |
| Battle shop | `game.d` P11 -> `game.h.a(4,0)`. | `/data/ui/shopbuy.ui`, `/data/ui/msgyn.ui`, `/data/ui/msgwarm.ui`. | Full `aq.c[4]` item table. | Source purchase adds item; PC rebuild policy makes all P11 rows cost `0`. | `PORTED/PARTIAL`. | Full global `shopbuy/msgyn` VM and pixel compare pending; SMS/mobile intentionally removed. |
| Panel bag tab item list | `game.h.Y()/ac()` with `b == 0`. | `/data/ui/bag.ui`, `/data/ui/msgwarm.ui`. | Combined `q.K + q.J`. | Balls `0..3` cannot-use; item `13/14` special; default goes state17. | `PORTED/PARTIAL`. | Exact `q.K/q.J` vector flags and full `bag.ui` VM partial. |
| Panel state17 item-on-pet | `bag.ui b=0 default` -> `game.k` state `17` -> `game.h.W()/Z()/bo()`. | `/data/ui/petstate.ui`, `/data/ui/msgwarm.ui`. | `q.J` normal item count. | `game.b.x/w`, refresh petstate, success/missing warning. | `PORTED/PARTIAL`. | Behavior coverage for P11-obtained items needs stronger panel/state17 smoke; exact UI partial. |
| Panel petsetting item use | `petsetting c=0` -> `choice.ui` -> confirm. | `/data/ui/choice.ui`, `/data/ui/msgwarm.ui`. | `q.J`; item `13/14` forbidden here. | `game.b.x/w` on selected pet, consume once. | `PORTED/PARTIAL`. | Exact `q.J` list vector and long-text/pixel parity partial. |
| Panel petsetting equipment | `petsetting c=2` -> `choice.ui`. | `/data/ui/choice.ui`, group `3` equipment icons/text. | `q.L`, not item group `4`. | `q.f(equipId,pet)`, `q.l(equipId)`, pet `c[5]`. | `PORTED/PARTIAL`. | Save/load of `q.L`, bank transfer, battle stat/passive effects pending. |
| Panel special egg/hatch | `bag.ui b=0 item14`, `bag.ui b=3 q.N case0`. | `/data/ui/bag.ui`, `/data/ui/msgwarm.ui`, `/data/ui/openbox.ui`. | `q.N`, `q.I`, `game.k.q`, item14 count. | Accelerator consumes item14; hatch adds pet/bank/full. | `PORTED/PARTIAL`. | After-battle progress increment and full `q.N` runtime pending. |
| Panel ride/special rows | `bag.ui b=3 q.N case5` -> `ride.ui` -> `q.h()`. | `/data/ui/ride.ui`, `/data/ui/msgwarm.ui`. | `q.N`, ride unlock state. | Active ride index and movement speed. | `PORTED/PARTIAL`. | Remaining q.N cases `6/10/7/8/9`, sprite swap/dismount/map object effects pending. |
| Save/load | Source RMS/game state; rebuild `VqsvSaveRuntime`. | N/A | Rebuild source fields. | Persist item counts, pets, egg, ride, avoid monster. | `PORTED/PARTIAL`. | Full RMS parity, `q.L` equipment inventory save, vector third flag parity pending. |

## Source Validation And Mutation Matrix

Source validation is `game.b.x(itemId)`. Source mutation is `game.b.w(itemId)`.

| Behavior | Source validation | Source mutation | Rebuild status | Required completion proof |
| ---: | --- | --- | --- | --- |
| `0` | Normal item use returns warning code `6`; ball items belong to catch list. | Not handled by `w()`. P21 consumes explicitly with `q.d`. | `PORTED/PARTIAL`. | P21/P17 catch smoke for ids `0/1`, P11 buy ball rows, top-level bag cannot-use for `0..3`. |
| `1` | HP full -> warning `2`; dead pet -> warning `8`. | Heal `maxHP * paramA / 100 + paramB`, show heal effect `l(amount)`, consume. | `PORTED/PARTIAL`. | P16, state17, petsetting item use for item `4/5`; save/load after use. |
| `2` | All skill PP full -> warning `3`; dead pet -> warning `8`. | Restore PP/skill value by `paramA`, consume. | `PORTED/PARTIAL`. | P16 has shop-buy use smoke; state17/petsetting panel smoke should explicitly cover `6/7`. |
| `3` | If HP and PP both full -> warning `7`; can still use if PP not full. | Heal by `paramA/paramB`, restore PP by `paramC`, consume. | `PORTED/PARTIAL`. | P16 has shop-buy use smoke; state17/petsetting smoke should explicitly cover `8/9` and warning `7`. |
| `4` | Alive pet invalid -> warning `1`; dead pet valid. | `c()` revive/reset, set HP, heal effect, restore PP, consume. | `PORTED/PARTIAL`. | Item `11` panel state17 smoke exists; item `12` stronger revive needs focused panel/state17 or P16 smoke coverage. |
| `5` | No active bad effect -> warning `4`. | Clear debuffs via `C()`, consume. | `PORTED/PARTIAL`. | P16 smoke exists; panel/state17/petsetting smoke should cover item `10` with debuff present and warning absent/present. |
| `6` | Already `d[6] >= 2` -> warning `5`. | Set `d[6] = 2`, consume. | Source code exists; source item row missing. | Do not invent an item. Mark `PENDING/NOT_REACHED_BY_AQ_C4` until source path proves reachability. |
| `7..10` | Not `game.b.x/w` normal item behavior in current rows. Battle P4 blocks these behavior ids. | Special top-level branches for `13` behavior `10` and `14` behavior `9` live in `game.h.ac()`, not `game.b.w`. | `PORTED/PARTIAL` for item 13/14. | Keep item 13/14 out of P16/state17/petsetting normal-use success path; verify forbidden/warning loops. |

Warning code mapping in `game.h.bo()`:

| Code | Source meaning |
| ---: | --- |
| `0` | Pet already dead; cannot use. |
| `1` | Pet missing / revive item used on living pet. |
| `2` | HP full. |
| `3` | Skill/PP full. |
| `4` | No bad effect to clear. |
| `5` | Already excited. |
| `7` | HP and skill/PP both full. |
| `8` | Dead pet cannot use non-revive item. |
| `-1` | Valid. |

## Per-Item Completion Matrix

| itemId | Behavior | Source UI paths | Current reachable path(s) | Current smoke evidence | Status | Remaining work |
| ---: | ---: | --- | --- | --- | --- | --- |
| `0` | `0` | P21/P17, P11, top-level bag cannot-use. | Room0 reward, Bunny P21, P11. | Bunny catch list/retry/storage, P11 item0 free row, bag cannot-use family. | `PORTED/PARTIAL`. | P17 exact RNG/pixel, P101/mobile purchase removed/PC-policy documented. |
| `1` | `0` | P21/P17, P11, top-level bag cannot-use. | Room0 reward, Bunny tutorial first fail, P11. | Bunny forced fail/retry, P21 row ids `[0,1]`. | `PORTED/PARTIAL`. | Same as item0; generic catch chance parity partial. |
| `2` | `0` | P21/P17, P11, top-level bag cannot-use. | P11 source row; not current story reward. | Shop row coverage; long-list P21 synthetic mapping where applicable. | `PORTED/PARTIAL`. | Add explicit catch lab scenario for item2 chance/use only if needed. |
| `3` | `0` | P21/P17, P11, top-level bag cannot-use. | P11 source row. | Shop row/badge row coverage. | `PORTED/PARTIAL`. | Add explicit catch lab scenario for item3 chance/use only if needed. |
| `4` | `1` | P4/P16, state17, petsetting c0, P11, op17. | Room0 reward, Elder reward, P11. | P16 HP heal, state17 success/warning, petsetting item success, route rewards. | `PORTED/PARTIAL`. | Save/load after item use should stay in regression; exact heal visual `l(amount)` partial. |
| `5` | `1` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item5 then P16 use; metadata smoke. | `PORTED/PARTIAL`. | Add panel state17/petsetting focused smoke for item5 if panel completion is target. |
| `6` | `2` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item6 then P16 use. | `PORTED/PARTIAL`. | Add state17/petsetting PP restore smoke and save/load check. |
| `7` | `2` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item7 then P16 use. | `PORTED/PARTIAL`. | Add state17/petsetting stronger PP restore smoke. |
| `8` | `3` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item8 then P16 use. | `PORTED/PARTIAL`. | Add state17/petsetting HP+PP success and warning `7` smoke. |
| `9` | `3` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item9 then P16 use. | `PORTED/PARTIAL`. | Add state17/petsetting stronger HP+PP smoke. |
| `10` | `5` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item10 then P16 use; no-debuff warning family exists. | `PORTED/PARTIAL`. | Add state17/petsetting debuff-present and no-debuff warning smoke. |
| `11` | `4` | P4/P16, state17, petsetting c0, P11, op17. | Elder reward, P11. | P16 revive, state17 item11 revive, route reward. | `PORTED/PARTIAL`. | Exact revive visual/actor reset partial; keep regression. |
| `12` | `4` | P4/P16, state17, petsetting c0, P11. | P11 source row. | P11 buy item12 then P16 use. | `PORTED/PARTIAL`. | Add state17/petsetting stronger revive smoke. |
| `13` | `10` | Top-level bag special branch; petsetting c0 forbidden; P4 blocked in battle. | Source item row; panel route. | item13 success/already/forbidden smoke. | `PORTED/PARTIAL`. | `q.c(1)` downstream side effect and global encounter timer parity pending. |
| `14` | `9` | Top-level bag special branch; petsetting c0 forbidden; P4 blocked in battle. | Source item row; panel route. | item14 no-egg/type0/type1/already-ready smoke; hatch q.N case0 smoke. | `PORTED/PARTIAL`. | After-battle egg progress increment and full q.N runtime pending. |

## Inventory Ownership Rules

Important source distinction:

| Flow | Source consume owner |
| --- | --- |
| P21 catch | `game.h.ai()` checks `q.b(ball,1,0)`, then explicitly calls `q.d(ball,1,0)`. |
| P16/state17/petsetting normal item-on-pet | `game.h.bo()` checks `q.b(item,1,0)`, then `game.b.w(item)` applies and calls `game.g.o().d(item,1,0)`. |
| Top-level item 13/14 | `game.h.ac()` checks `q.b`, then explicitly calls `q.d` because these do not call `game.b.w`. |
| P11 shop | `game.h.b(byte,byte)` confirms purchase and calls `q.c(item,qty,0)`. |
| op17 reward | Event opcode adds/removes item count directly through source gameplay op helper. |

Rebuild currently consumes once in runtime around `BattleUnit.applyBattleItem()`
instead of consuming inside `BattleUnit.applyBattleItem()` itself. That maps to
source ownership as long as runtime does not also consume a second time.

Do not move consume into `BattleUnit.applyBattleItem()` unless all callers are
audited and the existing runtime consume is removed at the same time.

## Save / Persistence Matrix

| State | Source anchor | Rebuild status | Remaining gap |
| --- | --- | --- | --- |
| Normal/ball item counts | `game.g.J/K` | `PORTED/PARTIAL` through `sourceBagItems` and save runtime. | Exact source vector split and third flag/delete semantics partial. |
| Avoid-monster item13 state | `q.x`, `q.w` | `PORTED/PARTIAL`, saved. | Timer/decrement and `q.c(1)` global side effect pending. |
| Egg item14/hatch state | `q.N`, `q.I`, `game.k.q`, `q.R` | `PORTED/PARTIAL`, saved for current fields. | After-battle increment and full q.N vector runtime pending. |
| Ride state | `q.N`, `P[]`, `t`, movement speed | `PORTED/PARTIAL`, active ride index/speed saved. | Sprite swap, dismount, map object effects pending. |
| Equipment inventory | `q.L`, pet `c[5]` | `PORTED/PARTIAL` for party equip/unequip/transfer runtime. | `q.L` save/load and bank/equipment storage parity pending. |
| Item effect on pets | `game.b` fields, pet payload | `PORTED/PARTIAL` for HP/PP/debuff/revive modeled fields. | Full source pet payload byte parity and all passive/equipment stat effects pending. |

## Battle Lab Coverage Matrix

Use Battle Lab as the item regression module. It must seed scenarios only; item
behavior belongs in shared runtime.

| Lane/suite | Current item coverage |
| --- | --- |
| `npc.items_shop_exp` | P16 items, P11 shop/payment, P8/P22/P23. |
| `npc.petstate` | P5/P16 petstate target/warning boundaries where relevant. |
| `catch.core` | P21/P17 basic catch. |
| `catch.inventory_storage` | Ball counts, catch forbidden, storage bag/bank/full. |
| `panel_wheel` | Panel list input/scroll row mapping; run after bag/petsetting list changes. |

Recommended new Battle Lab or smoke coverage after this audit:

1. `state17_item6_pp_restore`
2. `state17_item8_hp_pp_restore`
3. `state17_item10_clear_debuff`
4. `state17_item12_full_revive`
5. `petsetting_item6_pp_restore`
6. `petsetting_item8_hp_pp_restore`
7. `petsetting_item10_clear_debuff`
8. `petsetting_item13_14_forbidden_regression`
9. save/load after a normal item count mutation
10. save/load after equipment equip/unequip if `q.L` persistence is implemented.

## Current Completion Summary

| Area | Completion status |
| --- | --- |
| Source item metadata `0..14` | `PORTED/PARTIAL`; rows load from `VqsvBattleTables`, with current UI using source ids/icons/descriptions. |
| Battle P4/P16 normal item use | `PORTED/PARTIAL`; behavior `1..5` route-smoked, behavior `6` source-unreachable from current table. |
| Battle P21/P17 catch items | `PORTED/PARTIAL`; current Bunny/generic catch path smoke-covered. |
| Battle P11 shop | `PORTED/PARTIAL`; full item row reachability and PC-free policy smoke-covered. |
| Panel bag state17 | `PORTED/PARTIAL`; item4/item11 and generic success/warning covered, but full item row coverage needs more focused smoke. |
| Panel petsetting item use | `PORTED/PARTIAL`; confirm loop exists, but full item row coverage needs more focused smoke. |
| Panel special item13/14 | `PORTED/PARTIAL`; current source branches covered. |
| Egg/hatch q.N case0 | `PORTED/PARTIAL`; hatch action covered, after-battle progress pending. |
| Ride q.N case5 | `PORTED/PARTIAL`; ride mutation covered, visual/dismount/q.N remainder pending. |
| Equipment q.L | `PORTED/PARTIAL`; party equip/unequip/transfer covered, save/load and battle stat effects pending. |
| Save/load | `PORTED/PARTIAL`; enough for current route, not full source RMS/vector parity. |

## High-Risk Gaps

1. **Panel state17 row coverage is weaker than battle P16.**
   Items `6/7/8/9/10/12` are source-reachable through P11 and metadata exists,
   but panel state17/petsetting completion needs focused smoke per behavior.

2. **Equipment persistence is not complete.**
   `petsetting c=2` runtime can equip/unequip/transfer for party pets, but
   `q.L` save/load and bank ownership parity remain pending.

3. **Special q.N rows are only partially covered.**
   Egg case0 and ride case5 are ported/partial. Cases `6/10/7/8/9` remain
   pending until source reachability and user-facing need are proven.

4. **Behavior 6 exists in source code but has no current source item row.**
   Do not invent a behavior-6 item just to close a matrix row.

5. **Full UI runtime parity is not complete.**
   `choice.ui`, `msgwarm.ui`, `openbox.ui`, `petstate.ui`, `shopbuy.ui`,
   `msgyn.ui`, `bag.ui`, and `ride.ui` are source-backed partial renderers, not
   full original `game.h`/`ao` widget runtime.

6. **Exact item visual effects are partial.**
   `game.b.w()` heal text/effect `l(amount)`, revive reset visuals, debuff clear
   visuals, and status/equipment/passive interactions are not pixel-perfect.

## Recommended Next Slice

Do not code new item logic first. Start with verification coverage for the
already source-proven normal-item behaviors.

Recommended next doc/slice:

```text
228_panel_state17_petsetting_item_behavior_smoke_matrix.md
```

Scope:

1. Add focused PNG checkpoints for panel state17 item use:
   - item `6` PP restore;
   - item `8` HP+PP restore;
   - item `10` clear debuff success and no-debuff warning;
   - item `12` stronger revive;
   - item `8/9` both-full warning `7`.
2. Add matching petsetting c0 checkpoints only where the source loop differs
   from state17.
3. If the smoke fails, fix only the failing source-proven branch.
4. Run regressions:
   - build/check/formula/mojibake;
   - `battle_quick`;
   - `panel_wheel` if any list input changes;
   - Battle Lab `npc.items_shop_exp`;
   - route Sophie/Bunny/Elder.

After that slice, choose between:

- `q.L` equipment save/load parity; or
- q.N remaining special rows; or
- deeper skill completion matrix.

Do not move to skill completion until item behavior coverage is locked enough
that item side effects cannot mask skill/status bugs.
