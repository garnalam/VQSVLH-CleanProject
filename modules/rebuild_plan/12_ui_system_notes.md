# UI System Notes

Nguon doc trong pass nay:

- `source_code/decoded/decompiled_source_cfr/ab.java`
- `source_code/decoded/decompiled_source_cfr/ao.java`
- `source_code/decoded/decompiled_source_cfr/af.java`
- `source_code/decoded/decompiled_source_cfr/al.java`
- `source_code/decoded/decompiled_source_cfr/ac.java`
- `source_code/decoded/decompiled_source_cfr/k.java`
- `source_code/decoded/decompiled_source_cfr/m.java`
- `source_code/decoded/decompiled_source_cfr/y.java`
- `source_code/decoded/decompiled_source_cfr/z.java`
- `source_code/decoded/decompiled_source_cfr/w.java`
- `source_code/decoded/decompiled_source_cfr/game/h.java`
- `ui/original/*.ui`
- `ui/decoded/*.ui.json`
- `source_code/decoded/decompiled_source_cfr/a/*`
- `source_code/decoded/decompiled_source_cfr/q.java`

Trang thai: PARTIAL but solid enough for rebuild planning.

## 1. Ket Luan Nhanh

UI runtime khong phai HTML-like scene tree don gian. No la cay widget binary `.ui`:

```text
ab UI manager/cache
  -> ao UI instance/parser/focus controller
      -> al container/root container
      -> af visual/text/image/sprite control
      -> ac list/grid control
      -> k visual style payload
      -> m sprite/image animation handle
      -> z style/action table
      -> y shared palette/theme/render helper
```

`game.h` la workflow controller that su: mo/tat UI, gan text/icon, doc input, goi gameplay methods trong `game.g`, `game.b`, `game.d`, `game.k`.

`package a/*` KHONG phai UI layout runtime. No la Lua-like bytecode VM dung trong SMS/payment canvas `q.java`, load `/data/event/scene_13.mib`. Can port neu muon day du SMS/payment, nhung khong can cho menu/dialog/bag/shop/task co ban.

## 2. `ab`: UI Manager / Stack

`ab` la singleton:

```text
ab.a()
```

Fields:

| Field | Meaning | Status |
|---|---|---|
| `Hashtable d` | Map ui path -> `ao` instance. | VERIFIED |
| `Vector e` | Render stack/list of opened `ao`. | VERIFIED |
| `Vector f` | Open path stack, last element is top UI path. | VERIFIED |
| `ao a` | Current/top active UI. | VERIFIED |
| `y b` | Shared UI render/theme helper passed into each `ao`. | PARTIAL |

Important behavior:

- `a(String path, int imagePack, i owner)` opens UI.
- If UI already open and path is not `/data/ui/dialog.ui`, `ab` removes old instance, frees it, then reloads.
- `/data/ui/dialog.ui` is special-cached: it can be re-added without freeing, likely because dialog is reused often.
- `a(String path)` closes UI, updates current top to last opened UI.
- `b(path)` returns true if path is topmost.
- `c(path)` returns true if path is somewhere in stack.
- `d(path)` returns the `ao` instance by path.
- `a(Graphics)` renders all opened `ao` in stack order, then resets clip.

Port rule: preserve stack order and topmost behavior. A lot of `game.h` logic assumes `p.b("/data/ui/X.ui")` means "X is the currently active UI", not merely loaded.

## 3. `ao`: UI Parser, Renderer, Focus Controller

`ao(String path, int imagePack)` loads binary `.ui`:

```text
ae.b(byte[20000], path)       -> read file bytes
read header/magic/version
read root al fields
recursive parse children
build id-indexed widget array a[200]
build focus path e/f
```

Widget types observed in parser:

| Binary type | Class | JSON name | Meaning | Status |
|---|---|---|---|---|
| `0` | `al` | `container/root_container` | Container with child widgets and optional navigation/action table. | VERIFIED |
| `1` | `af` | `visual` | Text/image/sprite visual control. Most labels, icons, buttons. | VERIFIED |
| `2` | `ac` | list/grid | List/grid/selectable control; stores item cells and selection. | VERIFIED/PARTIAL |

`ao.a(Graphics)` renders the root container recursively. CFR damages a debug-looking branch around bounding boxes, but normal draw path is clear:

```text
for each child:
  if child has z/style animation:
    child.f().a(...)
  else:
    child.a(...)
```

`ao.b(int keyCode)` handles focus/navigation:

- For `ac` list/grid, keys `0/1/2/3/5/7` call list movement/confirm/back style methods.
- For `al` containers, action map comes from `.ui` navigation table; fallback mapping exists:

```text
0 -> up
1 -> down
2 -> left
3 -> right
5 -> confirm/action
7 -> back/up parent
14..25 -> soft/action events mapped as n - 6
```

`ao` reports interaction events to owner through `i h.a(int[]{leftIndex,rightIndex,eventCode})`. In practice `game.h` reads `this.o.k(keyMask)` directly for many workflows, but `ao` focus event routing still matters for selected index.

## 4. `af`: Visual Control

`af` implements `w` and wraps a `k` style/render payload.

Important fields:

| Field | Meaning | Status |
|---|---|---|
| `d` | Widget id. | VERIFIED |
| `e/f/g/h` | x/y/w/h. | VERIFIED |
| `i` | Type marker, defaults `1` for visual. | VERIFIED |
| `j` | `k` render payload: text, colors, image refs, sprite refs. | VERIFIED |
| `k/l` | Parent anchor id/alignment mode. | VERIFIED |
| `m` | Optional `z` style/action object. | PARTIAL |
| `a/b` | Extra bytes read at end of visual record; used by game code sometimes. | PARTIAL |
| `n` | Visible flag; `a(boolean)` toggles it. | VERIFIED |

Anchor modes in `af.a(parent)`:

```text
0 top-left
1 top-center, stretch width to parent width
2 top-right
3 center-left, stretch height
4 copy full parent rect
5 center-right, stretch height
6 bottom-left
7 bottom-center, stretch width
8 bottom-right
9 no anchor/default
```

Port rule: many UI files depend on this anchoring. Do not hardcode absolute coordinates only.

## 5. `.ui` Resources

Available decoded UI files: 40 layouts.

Large/high-priority layouts:

| UI | Observed structure | Main workflow |
|---|---|---|
| `bag.ui` | `container=24`, `visual=139` | Bag/inventory, item use, item detail, pet state shortcuts. |
| `petstate.ui` | `container=6`, `visual=70` | Pet detail/status/equipment/skill entry. |
| `battle.ui` | `visual=59` | Battle HUD and commands. |
| `evolve.ui` | `visual=50` | Pet evolution. |
| `shopbuy.ui` | `container=5`, `visual=45` | Buy UI. |
| `task.ui` | `container=5`, `visual=37` | Task/quest list. |
| `choice.ui` | `container=5`, `visual=35` | Pet/action choice menus. |
| `dialog.ui` | `visual=6` | World/dialog text. |
| `openbox.ui` | `visual=2` | Reward/lost message. |
| `taskTip.ui` | `visual=2` | Task tip popup. |

UI paths referenced by `game.h` most often:

```text
msgwarm.ui: 83
petstate.ui: 19
petsetting.ui: 14
choice.ui: 13
shop.ui: 13
gamemenu.ui: 10
msgyn.ui: 10
wharf2.ui: 10
openbox.ui: 9
gamesystem.ui: 8
levelUp.ui: 8
world.ui: 8
bag.ui: 7
msgconfirm.ui: 7
msgtip.ui: 7
shopbuy.ui: 7
```

Port rule: `msgwarm.ui` is not optional. It is the common warning/error popup for bag/shop/battle flows.

## 6. `game.h`: UI Workflow Controller

`game.h` owns:

```text
ab p     -> UI stack manager
game.g q -> player/gameplay model
an o     -> current module/input source
```

Observed workflow groups:

| UI workflow | Main files | What it touches | Status |
|---|---|---|---|
| World HUD | `world.ui`, `openbox.ui`, `taskTip.ui` | World text, popups, prompt animation. | VERIFIED |
| Game menu/system/help | `gamemenu.ui`, `gamesystem.ui`, `help.ui`, `help1.ui`, `msgtip.ui` | Save, help, system menu. | VERIFIED/PARTIAL |
| Pet status/settings | `petstate.ui`, `petsetting.ui`, `choice.ui`, `skill.ui`, `evolve.ui`, `levelUp.ui` | Active pets `game.g.z`, pet stats/skills/evolve. | PARTIAL |
| Bag/inventory | `bag.ui`, `msgwarm.ui`, `msgconfirm.ui`, `petstate.ui` | Vectors `K/J/L/M/N`, item use and pet interaction. | PARTIAL |
| Shop | `shop.ui`, `shopbuy.ui`, `shopsale.ui`, `msgyn.ui`, `msgRecover.ui`, `msgwarm.ui` | Buy/sell, money/currency, quantity selection. | PARTIAL |
| Task/record/badge | `task.ui`, `record.ui`, `badge.ui`, `taskOption.ui` | Quest/task state vectors and event VM choices. | PARTIAL |
| Battle UI | `battle.ui`, `choiceskill.ui`, `choice.ui`, `msgwarm.ui` | Battle commands, target selection, skills/items. | PARTIAL |
| SMS/payment | `smsTip.ui`, `smsInfo.ui`, `q.java`, `package a/*` | Payment prompt and bytecode script. | PARTIAL |
| Wharf/transmit | `wharf1.ui`, `wharf2.ui`, `transmit.ui`, `shopbuy.ui` | Travel/wharf/shop-like flows. | PARTIAL |

The event VM uses a small subset already audited:

- `dialog.ui`
- `option.ui`
- `answer.ui`
- `wharf1.ui`
- `taskOption.ui`
- `openbox.ui`
- `taskTip.ui`
- `msgtip.ui`

Full game rebuild needs all workflow groups above.

## 7. `package a/*`: Lua-Like Bytecode VM

This package is not menu renderer.

Observed roles:

| Class | Role | Status |
|---|---|---|
| `a.f` | Bytecode loader. Reads signature byte `27`, constants, nested functions. | VERIFIED |
| `a.g` | Interpreter/VM. Executes instruction array, stack, calls native functions. | VERIFIED/PARTIAL |
| `a.i` | VM thread/stack/call-frame owner. | VERIFIED |
| `a.c` | Call frame/register window. | VERIFIED/PARTIAL |
| `a.h` | Table/global environment. | VERIFIED/PARTIAL |
| `a.a` | Built-in/native functions and assertions. | PARTIAL |
| `a.b` | Native function interface. | VERIFIED |
| `a.d` | Closure/function wrapper. | VERIFIED/PARTIAL |
| `a.j` | Upvalue/reference cell. | PARTIAL |

`q.java` creates `a.g`, injects native functions:

```text
ca, gi, gs, gg, gj, yc, m, n, as, _fc, _fb, ts, aa
```

Then loads:

```text
/data/event/scene_13.mib
```

Conclusion: keep this in rebuild backlog for SMS/payment path. It should not block world/dialog/bag/shop UI renderer.

## 8. Port Priority

Priority A, needed for intro/world/event:

```text
ab, ao parser, al, af, k, m, z, y
dialog.ui, openbox.ui, taskTip.ui, option.ui, answer.ui, taskOption.ui, wharf1.ui, world.ui
```

Priority B, needed for playable menu/gameplay:

```text
game.h flows for gamemenu/system/petstate/bag/shop/task/battle
bag.ui, petstate.ui, battle.ui, shop.ui, shopbuy.ui, msgwarm.ui, msgyn.ui, msgconfirm.ui
```

Priority C, full feature:

```text
package a/*, q/r SMS canvas, smsTip.ui, smsInfo.ui, wharf2/transmit/record/badge/evolve/levelUp
```

## 9. Remaining Audit

- Decode exact binary `.ui` spec field-by-field against `ao.a(byte[],...)`.
- Audit `al/ac/k/m/z/y` in detail; current pass only identifies role and control flow.
- UI workflow matrix da tach rieng trong [16_ui_workflow_matrix.md](16_ui_workflow_matrix.md); con can branch-level validation cho cac hang `PARTIAL`.
- Build a widget-id matrix per `.ui` file: id -> type -> meaning -> who writes it.
- Verify `package a/*` instruction set only if SMS/payment must be rebuilt.
