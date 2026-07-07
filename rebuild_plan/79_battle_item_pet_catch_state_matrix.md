# 79 Battle Item / Pet / Catch State Matrix

Status: SOURCE AUDIT ONLY. No rebuild code changed in this step.

Scope: Phase 5 battle behavior for `game.d` states P4/P16/P5/P21/P17 and
their `game.h` UI facade methods. This matrix follows the current roadmap after
P7/P12/P13/P15 active-effect work.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:857..908` | State enter/init for P4 item list, P16 item target, P5 pet switch, P17 catch result, P21 catch list. |
| `game/d.java:1582..1724` | Update switch for P4/P16/P5/P17/P21. |
| `game/d.java:1885..1908` | Draw switch for P4/P5/P17/P21; P17 draws battle scene + catch sprite/H effect. |
| `game/h.java:3920..3988` | `ah()/ai()` P21 catch list UI and confirm/back behavior. |
| `game/h.java:3996..4060` | `aj()/ak()` P4 item list UI and blocked item behavior checks. |
| `game/h.java:4060..4108` | `bo()` item target validation and apply warning/result flow. |
| `game/h.java:4109..4145` | `al()` P16 item target input/back behavior. |
| `game/h.java:5750..5878` | UI callback mapping for battle P4/P5/P16/P21 selections. |
| `game/d.java:1011..1035` | Source state 15 transition after a valid P5 replacement/swap. |
| `game/d.java:1876..1898` | Draw state 15 uses battle scene + overlay; draw state 5 is a pet-selection UI state. |
| `game/b.java:1121..1157` | `w(itemId)` applies item behavior and removes item from bag. |
| `game/b.java:1162..1194` | `x(itemId)` validates whether an item can be used on the selected pet. |
| `game/g.java:1526..1533` | `y()` catch storage result: `0` bag, `1` bank, `2` no space/release. |
| `modules/script/decoded/data__script__db.mid.json` group `4` | Item table rows. Behavior is column `[5]`; params are `[6..8]`. |

## Source State Matrix

| State | Enter/init | Update method | UI file(s) | Input | Side effects | Next state(s) | Rebuild status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P4 item list | `game.d.a(4)` calls `S.aj()`. | `game.d.b()` calls `S.ak()`. | `/data/ui/choice.ui`, `/data/ui/msgwarm.ui`. | Up/down list, confirm item, back. | Lists bag `q.J`; blocks item behaviors `7..10` in battle with warning. Selected item id stored in `S.s`. | P16 on usable item, P20 on back, P4 warning loop. | PARTIAL: rebuild lists non-ball items and blocks `7..10`, but source bag/UI callbacks and warning flags are simplified. |
| P16 item target/use | `game.d.a(16)` sets `S.c=0`, `S.l=false`, calls `S.W()`. | `game.d.b()` calls `S.al()`, which calls `bo()` on confirm. | `/data/ui/petstate.ui`, `/data/ui/msgwarm.ui`. | Up/down pet target, confirm use, back. | Calls `game.b.x(itemId)` to validate; then removes item and calls `game.b.w(itemId)`. If used on active battle unit, can mark `h.J=true`, update active display, advance turn. | P4 on back/fail, P1 if active unit consumed turn, P4 or battle menu after success depending `h.J`. | PORTED/PARTIAL: `game.b.x/w` behavior `1..6` is wired through selected target; UI is still source-shaped generic menu, not full `petstate.ui`. |
| P5 pet switch | `game.d.a(5)` sets `S.c=0`, calls `S.W()`. | `game.d.b()` calls `S.X()`. | `/data/ui/petstate.ui`, possibly `/data/ui/msgwarm.ui`. | Pet list navigation, confirm/back. | `game.d.a(slot)` rejects dead/current pets, otherwise reorders `f[slot] -> f[0]`, marks new active pet, clears old active relation, then `game.h.X()` enters source state 15. Draw case prints selected pet name at y=200. | P15 after valid switch, P5 warning loop for invalid/current/dead, P20 on back. | PORTED/PARTIAL: rebuild now rejects dead/current, reorders source pet list, persists HP/PP, enters a P15 transition before P1, uses `cpos.mid` row `an[0][1]` for elder P5 switch motion, and uses a battle-only `petstate` renderer with list, selected sprite, name, HP, level, attack/defense/speed, quality stars, and source P5 button label `Xuất chiến`. Still not full `petstate.ui` widget runtime or full `game.d.an` parity for all battle layouts. |
| P21 catch list | `game.d.a(21)` targets enemy `d[0]`, sets `h.p`, calls `S.ah()`. | `game.d.b()` calls `S.ai()`. | `/data/ui/choice.ui`, `/data/ui/msgwarm.ui`. | Up/down ball list, confirm, back. | Lists `q.K` ball bag entries. Confirm checks count, sets `game.d.l=itemId`, removes ball via `q.d(item,1,0)`, then enters P17. If no count and item 0, can enter P101 purchase/SMS. | P17 on confirm, P20 on back, P101 purchase path, P21 warning loop. | PORTED/PARTIAL: rebuild lists bag items with behavior `0`, consumes ball, and routes P17. Tutorial seed and purchase/SMS path are approximate/partial. |
| P17 catch result | `game.d.a(17)` targets enemy `d[0]`, creates sprite `269`, positions at `h.i/h.j`, sets phase `q=0`, computes chance via `b(itemId)`, sets `ak` caught flag. | Phase machine `q=0..4`: ball animation, `H` effect, success/fail message, storage, restore enemy on fail. | Battle draw, sprite `269`, special `H/ah`, message UI. | Confirm after result messages. | On success, calls `game.g.y()`: `0` add to bag, `1` add to bank, `2` no space/release. On fail, restores enemy and returns battle turn. | P8/world exit after caught message path; P1 on fail; warning/full paths through message flags. | PORTED/PARTIAL: rebuild has sprite 269 phases, ball consume, storage bag/bank/release, fail returns P1. Exact chance, `H` animation ordering/timing, hide/restore, and full storage parity remain partial. |

## Item Behavior Matrix

From `aq.c[4][item][5]` and `game.b.w/x`.

| Behavior | Source `game.b.w(item)` apply | Source `game.b.x(item)` validation | Battle UI status |
| ---: | --- | --- | --- |
| 0 | Ball/catch item; not applied by `w()`. | Returns `6` if used as normal item. | Used by P21 catch list, not P4/P16. |
| 1 | Heal HP by `maxHP * paramA / 100 + paramB`; displays heal amount via `l()`. | If HP already full, returns warning code `2`. | PORTED: `BattleUnit.applyBattleItem`, smoke `battle_p16_item_heal_hp`. |
| 2 | Restore skill PP by `paramA` through `B(s3)`. | If all skill PP full, returns warning code `3`. | PORTED: PP capped by skill max, smoke `battle_p16_item_pp_restore` and warning smoke. |
| 3 | Heal HP and restore PP. | If HP full and PP full, returns `7`; if only HP full returns `2`; if PP not full can use. | PORTED: smoke `battle_p16_item_hp_pp`; warning `7` implemented but not separately smoked yet. |
| 4 | Revive/death item: calls `c()`, sets HP to computed heal, restores PP. | If pet alive, returns `1`; dead pet can use. | PORTED/PARTIAL: reserve-pet revive smoke passes; exact original `c()` actor reset visuals not fully modeled. |
| 5 | Clears debuffs via `C()`. | If no debuffs active, returns warning code `4`. | PORTED: smoke `battle_p16_item_clear_debuff` and no-debuff warning. |
| 6 | Sets `d[6]=2` excitement/state flag. | If already `d[6] >= 2`, returns warning code `5`. | PORTED in `BattleUnit`; UNSMOKED because current `aq.c[4]` item rows do not expose behavior `6`. |
| 7..10 | Not applied in battle P4; `game.h.ak()` blocks with warning "Trong chiến đấu không thể sử dụng". | N/A in battle path. | Rebuild blocks these in P4. |

Validation warning code mapping from `game.h.bo()`:

| `game.b.x(item)` return | Source warning |
| ---: | --- |
| `0` | Pet is dead; cannot use. |
| `1` | Pet does not exist / cannot use. |
| `2` | HP full. |
| `3` | Skill value/PP full. |
| `4` | No negative effects. |
| `5` | Excited; cannot use. |
| `7` | HP and skill are both full. |
| `8` | Pet is dead; cannot use. |
| `-1` | Valid; proceed with item consumption and `w(item)`. |

## Catch Flow Matrix

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Build ball list | `game.h.ah()` reads `q.K`, fills `choice.ui` icon/name/chance/count. | PORTED/PARTIAL from `sourceBagItems` behavior `0`. |
| Confirm ball | `game.h.ai()` checks `q.b(item,1,0)`, sets `game.d.l`, removes ball, enters P17. | PORTED: removes one item and enters P17. |
| Purchase edge | If count missing for item 0, warning can route P101. | PENDING/PARTIAL; rebuild warning exists but SMS/purchase not full. |
| Chance | `game.d.a(17)` calls `b(itemId)`, then `ak = ae.a(100) < chance`, with special `U==0 && V==5` force fail. | PARTIAL: rebuild uses deterministic source-shaped chance and tutorial/Bunny shortcuts. |
| Animation | Sprite `269`, `q=0..4`, `H/ah` effect, enemy hide/restore. | PORTED/PARTIAL: sprite 269 phases and source-shaped scale/offset exist, not pixel-perfect full `H`. |
| Success storage | `game.g.y()` returns bag/bank/full. Calls `p.a(...)` or `p.b(...)` with enemy payload. | PORTED/PARTIAL: adds to sourcePets/sourcePetBank/release full; payload parity partial. |
| Fail | Clears `H`, restores enemy visible, marks catcher action used, increments turn and enters P1. | PORTED/PARTIAL. |

## Pet Switch Matrix

| Source concern | Evidence | Rebuild status |
| --- | --- | --- |
| Uses petstate UI, not choice.ui. | `game.d.a(5)` calls `S.W()`, `game.h.e()` loads `/data/ui/petstate.ui`, fills rows `16/17 + i*6`, fills selected detail widgets `48/51/52/61/62/65..68/69..74`, and changes widget `75` to `Xuất chiến` when `P == 5`. | PORTED/PARTIAL: rebuild uses battle-only `petstate` renderer, not generic choice overlay; rows, selected sprite/detail stats, quality stars, and `Xuất chiến` are present. Equipment/relation/evolution text remains partial because current rebuild battle pet payload does not yet carry those fields fully. |
| Cannot freely select current/dead/invalid pet. | `game.h.X()` calls `game.d.a(slot)` and warning UI for invalid choices. | PORTED: current/dead/invalid checks exist for the current rebuild party model. |
| Petstate click/input | Source `petstate.ui` row containers are spaced every 15 px from y `86`, not like `choice.ui`. | PORTED: rebuild has petstate-specific row hitbox and smoke `battle_p5_click_reserve_success`. Live checkpoint `battle_elder_command_ui` now seeds a reserve pet so P5 can be tested directly. |
| Forced replacement after death differs from voluntary switch. | Source P15/P5/P13 paths are distinct. | P15 enemy replacement exists; player forced replacement still simplified through P5. |
| Switch may consume turn / set active unit J. | P16 item use and P5 paths manipulate `h.J`, `i`, P1/P13. | PARTIAL. |

## Current Rebuild Mapping

| Rebuild method | Source equivalent | Current status |
| --- | --- | --- |
| `prepareCatchMenu()` | `game.h.ah()` | PORTED/PARTIAL. |
| `tickCatchList()` | `game.h.ai()` | PORTED/PARTIAL. |
| `tickCatchResult()` | `game.d` P17 update | PORTED/PARTIAL. |
| `applyCatchStorage()` | `game.g.y()`, `game.g.a/b(enemyPayload)` | PORTED/PARTIAL. |
| `prepareItemMenu()` | `game.h.aj()` | PARTIAL. |
| `tickItemList()` | `game.h.ak()` | PARTIAL. |
| `prepareItemTargetMenu()` | `game.d.a(16)` + `game.h.W()` | PORTED/PARTIAL: selected target ids come from source pet party; generic choice renderer, not full `petstate.ui`. |
| `tickItemTarget()` | `game.h.al()/bo()` + `game.b.x/w` | PORTED/PARTIAL: validates and applies behavior `1..6`, consumes item, persists active/reserve pet HP/PP payload. |
| `preparePetMenu()` / `tickPetSwitch()` / `tickPlayerSwitchTransition()` | `game.d.a(5)` + `game.h.X()` + source state 15 | PORTED/PARTIAL: validation and source list reorder are ported; transition state is source-shaped, not exact `game.d.an` frame table playback. |

## Recommended Next Code Slices

Do not implement all Phase 5 at once. Recommended order:

1. **P5 pet switch parity**
   - DONE for practical battle path: dead/current validation, voluntary/forced list behavior, source pet reorder, HP/PP persistence, and source state 15 transition checkpoint.
   - Remaining debt: exact `petstate.ui` widget runtime and full `game.d.an` transition playback for all battle layouts. The current elder P5 player-switch movement uses source `cpos.mid` through `80_battle_p15_cpos_transition_matrix.md`.

2. **P21/P17 catch edge cases**
   - Remove tutorial auto-seed or gate it exactly to source tutorial.
   - Finish purchase/SMS/no-ball edge path.
   - Improve chance formula and storage payload parity.

3. **P16 remaining UI parity**
   - Replace generic choice-style target renderer with fuller `petstate.ui`
     behavior only after source widget/runtime is audited.
   - Add a dedicated smoke for validation code `7` and behavior `6` if a real
     source item path exposes them.

## Honest Current Gaps

- Full `petstate.ui` widget runtime is not ported; rebuild now has a battle-only
  `petstate` renderer for P5, while P16 item target still uses source-shaped
  generic choice UI.
- P5 valid switch enters P15 and uses source `cpos.mid` for the current elder
  player-switch movement. Full `game.d.an` playback for P0, enemy replacement,
  side-marker `al[]`, and alternate layouts remains partial.
- `game.b.w/x` behavior `1..6` is wired for P16, but behavior `6` has no
  current source item smoke in `aq.c[4]`, and exact `c()` revive actor visual
  reset remains PARTIAL.
- Catch animation is source-shaped but not pixel-perfect.
- Catch storage payload is useful for current rebuild, but not guaranteed byte
  identical to original `game.g` save payload.
- P101/SMS purchase path is still outside core battle parity.
