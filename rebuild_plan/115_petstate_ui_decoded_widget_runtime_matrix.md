# Petstate UI Binary Widget Runtime Matrix

Date: 2026-07-08

Scope: `/data/ui/petstate.ui` rendering for world petstate, battle P5, and
battle P16 item target.

## Source Facts

| Source | Fact | Status |
|---|---|---|
| `game.h.W()` | Resets selected row `b = 0`, then calls `e(c)`. | VERIFIED |
| `game.h.e(int)` | Loads `/data/ui/petstate.ui` with sprite bank `257` and fills row/detail widgets. | VERIFIED |
| `game.h.e(int)` battle branch | Uses `game.d.f[]` order for rows. | VERIFIED |
| `game.h.e(int)` battle branch | Hides widgets `63/64`; widget `75` is `Sử dụng` for P16/item and `Xuất chiến` for P5. | VERIFIED |
| `game.h.e(int)` row bars | Widget ids `16+i*6` and `17+i*6` receive `#P + percent`. | VERIFIED |
| `game.h.a(b[],int)` detail fill | Widget `48` sprite, `51` name, `52` element, `62` evolution text, `61` relation, `59/60` held item, `65..68` level/stat values. | VERIFIED |
| `ao.a(String,int)` | Binary parser reads container/style/visual/grid records recursively from `.ui`. | VERIFIED/PARTIAL in rebuild |
| `k.java` visual draw | Selected state uses `image_ref` + `e/f/g`; normal state uses `alt_image_ref` + `j/k/l`; `#P` draws progress fill with text color. | VERIFIED |

## Runtime Mapping

| Widget group | Source ids | Rebuild equivalent | Status |
|---|---|---|---|
| UI loading | `/data/ui/petstate.ui` | `VqsvUiLayout.load("petstate.ui")` now tries `modules/ui/original/petstate.ui` first and falls back to decoded JSON only if binary parsing fails. | PORTED/PARTIAL |
| Binary parse proof | all visual widgets | Smoke asserts `binarySource == true`, `widgetCount >= 70`, and required ids exist. | PORTED |
| Visual widget fields | x/y/w/h/text/colors/image refs | Parser reads visual fields from binary using `ao.java` order. | PORTED/PARTIAL |
| Main frame/bands | `1`, `3`, `4`, `5` | Renderer uses source widget positions, colors, and image refs where available; some `h=-1` bands still use source-shaped height fallbacks. | PARTIAL |
| Static cells | `1`, `7`, `8`, `9..12` | Renderer uses widget image/alt refs instead of hardcoded cells. | PORTED/PARTIAL |
| Row widgets | `6/14/15/16/17 + row*6` | Row normal/selected cell comes from widget refs; row number and HP/EXP bars use widget positions and source colors. | PORTED/PARTIAL |
| `#P` progress | `16+i*6`, `17+i*6` | Gauge fill color now comes from widget text color, matching `k.java` behavior more closely. | PORTED/PARTIAL |
| Arrows | `49`, `50` | Draws mode-3 widget refs; still source-shaped because full `m` sprite state runtime is not generic. | PARTIAL |
| Detail sprite | `48` | Clips and draws selected pet sprite using widget bounds. | PORTED/PARTIAL |
| Detail text | `51..62`, `65..68` | Dynamic values follow `game.h.a(b[],int)` mapping; long text now marquee-scrolls instead of hard clipping. | PORTED/PARTIAL |
| Quality/star widgets | `69..74` | Positions and empty refs come from widget map; filled-star state still source-shaped. | PARTIAL |
| Softkeys | `75`, `76` | Action/back rendered by widget id; P16 action is `Sử dụng`, P5 action is `Xuất chiến`. | PORTED/PARTIAL |

## Current Limits

| Limit | Why |
|---|---|
| Not full `ao/al/ac` runtime yet. | Rebuild parses enough binary data for visual widgets, but does not execute the full container navigation/style tree generically. |
| Exact selected-style timing is partial. | Renderer maps selected/normal state from source fields, but does not execute every `z` style transition. |
| Exact text scroll timing is partial. | Text now marquee-scrolls when wider than the widget, but does not yet reproduce `k.n[]` delay/step exactly. |
| Some visual heights are still inferred. | Source visual widgets often encode `h=-1`; renderer keeps local source-shaped heights for bands and progress bars. |
| World petstate submenus remain pending. | Send/release/equip/skill submenus are outside this P5/P16 visual slice. |

## Verification

Smoke PNGs generated after this slice:

- `rebuild_game/build_intro_demo/battle_p16_target_petstate_ui_binary_runtime.png`
- `rebuild_game/build_intro_demo/battle_p5_petstate_source_rows_binary_runtime.png`
- `rebuild_game/build_intro_demo/battle_p16_item_heal_hp_binary_runtime.png`
- `rebuild_game/build_intro_demo/battle_p16_item_revive_binary_runtime.png`
- `rebuild_game/build_intro_demo/route_sophie_after_battle_branch_petstate_binary_regression.png`
- `rebuild_game/build_intro_demo/route_bunny_after_battle_task_petstate_binary_regression.png`
- `rebuild_game/build_intro_demo/route_elder_after_battle_reward_state_petstate_binary_regression.png`

Checks:

- `build.ps1`: pass.
- `com.vqsv.rebuild.Main --check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- Java source mojibake scan: pass.

## Next Step

If petstate must become closer before returning to battle roadmap, the next
small slice should be **exact `k.java` visual text/progress timing**:

1. Audit `k.a(Graphics,...)` fields `n[]`, `p/q`, and selected flag.
2. Reproduce text scroll start delay and step for widgets `60/61/62`.
3. Add two-frame smoke for petstate text: start frame and active marquee frame.
4. Keep P5/P16 logic unchanged.
