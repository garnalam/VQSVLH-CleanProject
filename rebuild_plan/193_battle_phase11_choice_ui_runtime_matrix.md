# 193 Battle Phase 11 Choice UI Runtime Matrix

Status: AUDIT CREATED / NO CODE CHANGED.

Scope:

- Source audit for `game.h` choice-list flows.
- UI layout audit for `modules/ui/decoded/data__ui__choice.ui.json`.
- Rebuild mapping audit for the current battle `choice.ui` renderer.

Non-scope:

- No code changes.
- No client/JAR launch.
- No `petstate.ui`, `msgwarm.ui`, `openbox.ui`, or `choiceskill.ui` porting,
  except where source transitions out of `choice.ui`.

## Source Inputs

| Source | Purpose |
|---|---|
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | Source owner for UI facade and input handling. |
| `modules/ui/decoded/data__ui__choice.ui.json` | Decoded widget layout for `/data/ui/choice.ui`. |
| `modules/ui/original/choice.ui` | Original binary layout, used as decode origin. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | Current renderer mapping for battle `choice.ui`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Current producers for P21 catch menu and P4 item menu. |

## Boundary

`choice.ui` is not the battle skill list and not the pet switch screen.

| UI | Source owner | Current battle usage | Boundary |
|---|---|---|---|
| `choice.ui` | `game.h.ah/ai/aj/ak`, plus non-battle menu flows | P21 catch list, P4 item list, P11 shop-like list | This audit target. |
| `choiceskill.ui` | `game.h` P3 skill method before `ah()` | P3 skill list | Separate Phase 9/10 work; do not merge into `choice.ui`. |
| `petstate.ui` | `game.h.al/bo` and pet state helpers | P5 switch, P16 item target | Separate Phase 5 UI runtime work. |
| `msgwarm.ui` | `game.h.a(...)` warning facade | Warnings/success after choice confirm | Transition target only. |
| `openbox.ui` | Catch/openbox success flow | Catch success notification | Transition target only. |

## Source Call Matrix

| Flow | Source method | UI open / close | Widget setup | Row data source | Input | Side effect | Next state/UI | Status |
|---|---|---|---|---|---|---|---|---|
| P21 catch list open | `game.h.ah()` around `h.java:3920` | `p.a("/data/ui/choice.ui", 257, this)` | `8 = "Pokemon ball"`, `9 = source text "Ti le bat"`, `5 = source text "Su dung"`, hide `59/60` | `q.K`, each row is `int[] v2`; icon from `aq.c[4][itemId][1]`, name from `aq.c[4][itemId][0]`, chance from `game.d.b(itemId)` | Initial cursor from `this.b`, `al` list size from `q.K.size()` | None on open | Stays P21 | PORTED/PARTIAL in rebuild. |
| P21 catch scroll | `game.h.ai()` around `h.java:3951` | Same `choice.ui` stays open | Calls `p.a.b(0/1)` then `bn()` | Current selected `q.K[this.b]` | Key masks `4100` and `8448` | Updates selected row and widget `53` count text | Stays P21 | PARTIAL; rebuild has menu index/scroll but not full `al` runtime. |
| P21 catch confirm | `game.h.ai()` | Closes `choice.ui` only when consumed and entering P17 | `bn()` shows widget `53` count text, source meaning `So luong: X cai` | Selected `q.K[this.b]` | Key mask `196640`, `an.I()`, `!j()` | If `q.b(item,1,0)` false, opens `msgwarm.ui`; else sets `game.d.l`, calls `o.m()`, consumes `q.d(item,1,0)`, state `17` | P17 catch result or warning return | PORTED/PARTIAL; storage/full and animation remain separate catch debt. |
| P21 back | `game.h.ai()` | `p.a("/data/ui/choice.ui")` | None | None | `game.c.J()` plus key `262144` | None | State `20` command | PORTED/PARTIAL. |
| P4 item list open | `game.h.aj()` around `h.java:3992` | `p.a("/data/ui/choice.ui", 257, this)` | `8 = source text "Dao cu"`, `9 = source text "So luong"`, `5 = source text "Su dung"`, hide `59/60` | `q.J`, rows built by `be()` | Initial `s=0`, `f=0`, `b=0` | None | Stays P4 | PORTED/PARTIAL. |
| P4 item scroll | `game.h.ak()` and `be()` around `h.java:2470` | Same `choice.ui` stays open | `be()` rewrites row icon/name/count and `53` description | `q.J`; icon `aq.c[4][itemId][1]`, name `aq.c[4][itemId][0]`, count `v2[1]`, description `aq.c[4][selected][2]` | Key masks `4100` and `8448` | Updates `al` list state `e/f`, scroll bar widget `51` | Stays P4 | PARTIAL; rebuild has scroll/index and desc but not source `al` object. |
| P4 item confirm | `game.h.ak()` | Closes `choice.ui` when entering P16 or warning | None | Selected `q.J[this.h]` | Key mask `196640` | Blocks item behavior `7..10` in battle with `msgwarm.ui`; otherwise sets selected item and enters state `16` | P16 `petstate.ui` or warning | PORTED/PARTIAL; P16 target UI is not `choice.ui`. |
| P4 back | `game.h.ak()` | `p.a("/data/ui/choice.ui")` | None | None | Key mask `262144` | None | State `20` command | PORTED/PARTIAL. |
| Petsetting item tab | `game.h` around `h.java:2039` | Opens `choice.ui` layered with `petsetting.ui` and `petstate.ui` | `8 = source text "Dao cu"`, `9 = source text "So luong"`; for `k` toggles `5/6` hidden and `59/60` visible | `q.J` via `be()` | Petsetting input loop | World/menu item use | Not battle P4 | PENDING for generic runtime; not needed for battle P21/P4 slice. |
| Petsetting jewelry tab | `game.h` around `h.java:2092` | Opens `choice.ui` layered with `petsetting.ui` and `petstate.ui` | `8 = source text "Vat pham trang suc"`, `9 = source text "Trang thai"`; action source meaning `Mang theo` | `q.L` via `bd()` | Petsetting input loop | Equip/unequip accessory | Not battle P21/P4 | PENDING for generic runtime. |

## `choice.ui` Widget Matrix

The decoded JSON has mojibake default text for some labels. Source `game.h`
overrides most battle-facing labels at runtime. The original game therefore
does not rely on the decoded default strings for P21/P4 title/action.

| Widget id | Bounds | Source role | Data source / behavior | Current rebuild status |
|---:|---|---|---|---|
| 1 | `x=41,y=68,w=158` | Main outer frame, image alt `91/mode2` | Static frame | PARTIAL; renderer draws source cell but not full widget runtime. |
| 2 | `x=44,y=78,w=151` | Body fill band | Static color band | PARTIAL; hardcoded fallback height. |
| 3 | `x=44,y=238,w=151` | Bottom button band | Static color band | PARTIAL. |
| 4 | `x=44,y=70,w=151` | Header fill band | Static color band | PARTIAL. |
| 5 | `x=50,y=235,w=24` | Left softkey/action | Source sets action text such as `Su dung`, `Mang theo`, `Mua` | PARTIAL; battle renderer draws text but not widget visibility rules generically. |
| 6 | `x=164,y=235,w=24` | Right softkey/back | Default source meaning `Quay lai`; source toggles visible in petsetting contexts | PARTIAL. |
| 7 | `x=48,y=90,w=143` | List panel background | Static color band behind rows | PARTIAL. |
| 8 | `x=60,y=75,w=46` | Title column | Source sets `Pokemon ball`, `Dao cu`, etc. | PORTED/PARTIAL for battle menus. |
| 9 | `x=143,y=75,w=36` | Right column header | Source sets `Ti le bat`, `So luong`, etc. | PORTED/PARTIAL for battle menus. |
| 10/15/20/25/30 | row containers, `h=16` | Five visible row shells | `al` list runtime maps cursor/scroll to these rows | PARTIAL; rebuild maps visible rows manually. |
| 11/16/21/26/31 | row frame widgets | Row background; image `26/mode2`, alt `25/mode2` | Selected row chooses alternate frame | PARTIAL; selected frame drawn manually. |
| 13/18/23/28/33 | row name widgets | Row name text | P21/P4 uses `an.f(aq.c[4][itemId][0])` | PORTED/PARTIAL; text data present, marquee/runtime not full. |
| 14/19/24/29/34 | row value widgets | Right value text | P21 chance `%`; P4 item count | PORTED/PARTIAL. |
| 50 | `x=183,y=98,w=3` | Scroll track | Visible when list longer than 5 rows | PARTIAL; manual draw. |
| 51 | `x=183,y=98,w=4` | Scroll knob | Source `be()` positions via `98 + h * 72 / size`; P21 uses current `al` | PARTIAL; rebuild approximates with selected index. |
| 52 | `x=52,y=174,w=135` | Description/count box frame, alt `24/mode2` | Shows only when bottom description/count is meaningful | PORTED/PARTIAL. |
| 53 | `x=57,y=180,w=125` | Description/count text | P21 `bn()` count; P4 selected item description | PORTED/PARTIAL; source text source is known. |
| 54..58 | `x=54,y=95..155,w=14` | Row icons | Source creates `m` sprite renderer with sprite `258`, cell `aq.c[4][itemId][1]` | PORTED/PARTIAL; rebuild draws sprite 258 cell directly. |
| 59 | `x=1,y=296,w=43` | Alternate left softkey for non-battle/petsetting layout | Source shows when `o instanceof k`, hides `5/6` | PENDING for generic runtime; out of battle scope. |
| 60 | `x=197,y=296,w=43` | Alternate right softkey for non-battle/petsetting layout | Source shows when `o instanceof k`, hides `5/6` | PENDING for generic runtime; out of battle scope. |

## Current Rebuild Mapping

| Rebuild area | File | Evidence | Status |
|---|---|---|---|
| P21 catch data producer | `rebuild_game/src/main/java/VqsvBattleRuntime.java` `prepareCatchMenu()` | Builds names, chance values, item ids, sprite 258 icon cells, title `Pokemon ball`, subtitle source meaning `Ti le bat`, action source meaning `Su dung` | PORTED/PARTIAL. |
| P4 item data producer | `VqsvBattleRuntime.prepareItemMenu()` | Builds item names/count/description, title source meaning `Dao cu`, subtitle source meaning `So luong`, action source meaning `Su dung` | PORTED/PARTIAL. |
| Generic menu state carrier | `VqsvIntroDemo.Scene` battle menu fields | `battleMenuTitle`, `battleMenuSubtitle`, `battleMenuAction`, row names/values/ids/icons/descriptions | PARTIAL; source `al` list object not modeled. |
| Choice renderer | `VqsvBattleRenderer.drawChoiceOverlay()` | Loads `VqsvUiLayout.load("choice.ui")`; draws static widgets, five row frames, icon sprite 258, desc/count box | PARTIAL; layout-backed but not full widget runtime. |
| Scroll behavior | `VqsvBattleRuntime.syncMenuScroll()` and renderer knob math | Keeps visible window and selected index | APPROX/PARTIAL; not identical to source `al.a.e/f` rules in `be()`. |
| Widget visibility | Renderer conditionals | Hides/shows desc box by content/title checks | PARTIAL; not a generic `a(false/true)` widget visibility model. |
| Text behavior | `drawSourceWidgetText(...)` | Uses widget width and text mode `b` | PARTIAL; not full `game.h`/UI text object runtime. |

## Gaps To Close In Phase 11

| Gap | Source evidence | Risk | Recommended status |
|---|---|---|---|
| No generic `choice.ui` widget tree runtime | Source calls widget ids and visibility directly: `p.a.a(id).h().a`, `.a(false)`, `.b(...)` | Future P21/P4 changes can accidentally hardcode around UI | PENDING. |
| No source `al` list object parity | `ah()` and `be()` set `((al)widget0).a.f/e/a`, then source input calls `p.a.b(0/1)` | Scroll/cursor edge cases can differ for long lists | PENDING. |
| Scroll knob formula differs by flow | `be()` uses `98 + h * 72 / q.J.size()`; P21 depends on current list object and `bn()` | Visual mismatch on long item/ball lists | PARTIAL. |
| Widget `59/60` alternate softkeys not modeled generically | Petsetting flows toggle `5/6` vs `59/60` | Not a battle blocker, but required for full `choice.ui` runtime | PENDING. |
| Default UI text in decoded JSON is mojibake | Decoded JSON labels are mojibake, source overwrites many labels | Renderer must keep source text in `VqsvText`, not rely on decoded mojibake | KNOWN / HANDLE CAREFULLY. |
| Row icon lifecycle is simplified | Source creates/destroys `m` sprite object per row and cell; rebuild direct-draws sprite 258 | Could miss sprite mode/tick behavior | PARTIAL. |
| P21 warning return and P4 warning return use separate `msgwarm.ui` | Source closes warning and reopens `choice.ui`/state depending on `f` | Logic mostly covered, but UI runtime boundary should stay separate | PORTED/PARTIAL. |

## Existing Smoke Coverage

| Checkpoint | Coverage | Status |
|---|---|---|
| `battle_bunny_catch_p21` | P21 `choice.ui` open, title/subtitle/action/ball list | Existing focused smoke. |
| `battle_choice_ui_scroll_source_rows` | Multi-row list/scroll source rows | Existing focused smoke. |
| `battle_elder_item_p4` | P4 item list open | Existing focused smoke. |
| `battle_p16_back_returns_p4` | P16 back returns to P4 item list | Existing focused smoke. |
| `battle_catch_missing_count_warning_return_p21` | P21 warning returns to catch choice | Existing focused smoke. |
| `battle_quick` suite | Does not include all `choice.ui` focused cases | Quick gate only; not enough for Phase 11 UI parity. |

## Next Code Slice Recommendation

Phase 11-A should be small and UI-only:

1. Add a `VqsvChoiceUiView` or equivalent runtime object that mirrors the
   source-visible pieces of `choice.ui`: widget visibility, widget text,
   row models, selected index, scroll offset, and softkey labels.
2. Keep P21 and P4 logic unchanged.
3. Make `prepareCatchMenu()` and `prepareItemMenu()` populate this view instead
   of relying only on scattered `Scene.battleMenu*` fields.
4. Make `drawChoiceOverlay()` render from the view while still using decoded
   `choice.ui` widget bounds/images.
5. Do not include petsetting `59/60` support in the first code slice unless it
   falls out naturally from the visibility model.

Required focused smoke after Phase 11-A code:

```text
java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_bunny_catch_p21 build/smoke/phase11/battle_bunny_catch_p21.png
java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_choice_ui_scroll_source_rows build/smoke/phase11/battle_choice_ui_scroll_source_rows.png
java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_elder_item_p4 build/smoke/phase11/battle_elder_item_p4.png
java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_catch_missing_count_warning_return_p21 build/smoke/phase11/battle_catch_missing_count_warning_return_p21.png
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build/smoke/suites/battle_quick
```

## Current Phase Position

Phase 11 is Battle UI Widget Runtime Parity.

This audit starts with `choice.ui` because it is used by both P21 catch and P4
item flow and still sits in a source-shaped renderer. The next slice should not
change battle rules; it should only move the current P21/P4 data into a clearer
`choice.ui` runtime model and preserve all existing smoke behavior.
