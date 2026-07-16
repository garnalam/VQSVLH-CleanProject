# 405 - Panel Record / Do Giam Route Matrix

Date: 2026-07-15

Scope: audit-only source route matrix for the right-softkey `Do giam` branch after the `Lung bao / bag.ui` work. No runtime code is changed in this slice.

## Prime Rules

- Source first: this audit is based on decoded `game.k` methods and decoded UI files.
- No live client. PNG smoke references are existing rebuild checkpoints only.
- Do not touch battle, skill animation, transmit world load, or generic event VM in this slice.
- Classify every claim as `PORTED`, `PORTED/PARTIAL`, `APPROX`, `PENDING`, `UNKNOWN`, or `STUB`.
- Do not claim pixel-perfect without original-vs-rebuild pixel compare.

## Files Read

Source/runtime:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

UI data:

- `modules/ui/decoded/data__ui__record.ui.json`
- `modules/ui/decoded/data__ui__petmap.ui.json`
- `modules/ui/decoded/data__ui__badge.ui.json`
- `modules/ui/decoded/data__ui__gamemenu.ui.json`

Required current handoff:

- `rebuild_plan/404_new_dev_chat_handoff_panel_softkey_after_bag.md`

## Source Route Summary

`Do giam` is the right-softkey `gamemenu.ui` row `3` branch. In source, it opens `record.ui`, then branches to either pet encyclopedia or badge list:

- `game.k.Q()` opens `/data/ui/record.ui`, closes `/data/ui/gamemenu.ui`, fills record counters, restores selected record option from `this.c`, then sets `b=0`, `f=0`.
- `game.k.R()` handles `record.ui` input:
  - left/right changes selected record option with widget cursor movement.
  - confirm option `0` checks `game.j.p().l(5)` before opening petmap state `11`; if missing the petmap item, it shows a warning message.
  - confirm option `1` opens badge state `12`.
  - back closes `record.ui` and returns to `gamemenu.ui`.
- `game.k.S()` opens `/data/ui/petmap.ui`, closes `record.ui`, resets `b/c/f`, then calls `bb()`.
- `game.k.bb()` refreshes petmap tab/row data, owned markers, preview sprite, count text, and scrollbar.
- `game.k.T()` handles petmap up/down/left/right/back.
- `game.k.W()` opens `/data/ui/badge.ui`, closes `record.ui`, seeds achieved badge icons from `q.C`, then calls `be()`.
- `game.k.be()` refreshes badge name, description, achieved status.
- `game.k.X()` handles badge grid navigation and back. Back returns to bag if badge was entered from bag state `8`, otherwise returns to `record.ui`.

## Route Matrix

| Source state/UI | Input | Source method | Data source | Side effect | Next UI/state | Rebuild status |
| --- | --- | --- | --- | --- | --- | --- |
| `gamemenu.ui` row `3` | confirm | menu dispatcher -> `game.k.Q()` | selected `gamemenu` row | opens `record.ui`, closes `gamemenu.ui`, fills counters | `record.ui` / record state | `PORTED/PARTIAL` |
| `record.ui` | left/right | `game.k.R()` | record widget cursor `this.c` | moves option cursor | `record.ui` | `PORTED/PARTIAL` |
| `record.ui` option `0` | confirm | `game.k.R()` | `game.j.p().l(5)` petmap item availability | if allowed, `o.a((byte)11)` | `petmap.ui` | `PORTED/PARTIAL`; item gate is not fully source-parity audited in rebuild |
| `record.ui` option `0` | confirm when locked | `game.k.R()` | `game.j.p().l(5) == false` | opens warning text: missing pet encyclopedia item | warning over `record.ui` | `PENDING` in current closeout scope |
| `record.ui` option `1` | confirm | `game.k.R()` | selected option `this.c == 1` | `o.a((byte)12)` | `badge.ui` | `PORTED/PARTIAL` |
| `record.ui` | back | `game.k.R()` | `a.a.i ? 3 : 2` source menu selected row | closes `record.ui`, returns menu selected row | `gamemenu.ui` | `PORTED/PARTIAL` |
| `petmap.ui` open | state entry | `game.k.S()` | `q.Y`, `q.X`, `q.a`, `aq.c[0]` via `bb()` | closes `record.ui`, resets tab/cursor | `petmap.ui` | `PORTED/PARTIAL` |
| `petmap.ui` | up/down | `game.k.T()` -> `bb()` | visible cursor `a.e/a.f`, current tab | moves species row, refreshes preview/count/scrollbar | `petmap.ui` | `PORTED/PARTIAL` |
| `petmap.ui` | left/right | `game.k.T()` -> `ba()` -> `bb()` | tab arrays `q.X/q.Y` | changes category tab and resets row scroll | `petmap.ui` | `PORTED/PARTIAL` |
| `petmap.ui` row/detail | render refresh | `game.k.bb()` | species id `q.X[b] + h`; sprite `aq.c[0][species][17]`; owned state `q.a(tab, species)` | shows preview sprite only if owned; row marker `101/102`; count text `365+b + owned/total`; scrollbar widget `23` | `petmap.ui` | `PORTED/PARTIAL`; exact source list/table parity still needs a dedicated closeout |
| `petmap.ui` | confirm | `game.k.T()` | no visible confirm branch in loaded source snippet | no route proven in this audit | stays/unknown | `PENDING` |
| `petmap.ui` | back | `game.k.T()` | previous state `o.b` | if previous state is bag `8`, return bag; otherwise set `c=0`, return record | `record.ui` or `bag.ui` | `PORTED/PARTIAL`; bag-origin route is separate from `401` |
| `badge.ui` open from record | state entry | `game.k.W()` | badge state `q.C[i][0]` and badge table `aq.c[2]` | closes `record.ui`, seeds badge icons, refreshes details | `badge.ui` | `PORTED/PARTIAL` |
| `badge.ui` | up/down/left/right | `game.k.X()` -> `be()` | selected badge index `this.b` | moves badge cursor and refreshes detail text/status | `badge.ui` | `PORTED/PARTIAL` |
| `badge.ui` detail | render refresh | `game.k.be()` | badge name `aq.c[2][b][0]`; description `aq.c[2][b][2 + q.b(b,1)]`; status `q.b(b,0)` | text `Chua dat` or `Da dat duoc`; achieved clears widget `33` | `badge.ui` | `PORTED/PARTIAL`; exact `q.C[8][2]` data parity is not fully closed |
| `badge.ui` | back | `game.k.X()` | previous state `o.b` | if previous state is bag `8`, return bag; otherwise set `c=1`, return record | `record.ui` or `bag.ui` | `PORTED/PARTIAL`; bag-origin route already covered by `401` |

## Data Binding Matrix

| UI | Source data | Rebuild data/status | Audit note |
| --- | --- | --- | --- |
| `record.ui` total caught | `q.B + q.P.size()` | `sourcePets.size() + sourcePetBank.size()` | `PORTED/PARTIAL`; source meaning is similar but not exact storage parity. |
| `record.ui` distinct owned | `q.G` | `distinctOwnedSpecies(scene)` | `PORTED/PARTIAL`; rebuild derives from current pet/bank state, not exact source field lifecycle. |
| `record.ui` rare count | `q.I` | `rareOwnedSpecies(scene)` | `PORTED/PARTIAL`; source classification needs exact species rarity parity audit if this becomes visible mismatch. |
| `record.ui` divine/beast count | `q.H` | hardcoded `0` in renderer | `APPROX`; route may be okay early-game, but data parity is not complete. |
| `record.ui` badge count | count where `q.b(badge, 0) == 2` | `sourceBadges` / proxy count | `PORTED/PARTIAL`; exact source `q.C` state not fully represented. |
| `record.ui` playtime | `game.f.B().n + game.f.B().o - game.f.B().p`, formatted by `game.l.a(l2)[1]` | `"00:00"` | `APPROX`; needs save/playtime runtime integration if user cares. |
| `petmap.ui` tabs/counts | `q.X[b]`, `q.Y[b]`, text id `365+b` | source-shaped tab names/row generation | `PORTED/PARTIAL`; should be verified against exact source species category arrays before full parity claim. |
| `petmap.ui` owned marker | `q.a(tab, species) == 2` for row marker; `>0` for preview sprite | current owned species from rebuild source pet state | `PORTED/PARTIAL`; user-visible early-game behavior likely ok, exact encyclopedia state lifecycle still pending. |
| `petmap.ui` preview sprite | `aq.c[0][species][17]`, widget `21` animation | sprite id from rebuild pet row metadata | `PORTED/PARTIAL`; exact frame/anchor/pixel placement not proven. |
| `badge.ui` achieved icon | `q.C[i][0] != 0`, sprite cell `46+i` | `sourceBadgeAchieved(scene, i)` proxy | `PORTED/PARTIAL`; source `q.C[8][2]` parity should be next if badge route becomes important. |
| `badge.ui` badge detail | `aq.c[2][b]`, `q.b(b, 1)` detail variant | rebuild `badgeName`, `badgeDescription`, `badgeStatusText` | `PORTED/PARTIAL`; source table-backed enough for route smoke, not yet full lifecycle parity. |

## Current Rebuild Runtime Mapping

| Runtime part | Current behavior | Status |
| --- | --- | --- |
| `VqsvPanelRuntime.tickRecord()` | left/right option, back to gamemenu row 3, confirm option 0 to petmap, option 1 to badge | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.renderRecord()` | loads `record.ui`, draws source-shaped frame/buttons/counters | `PORTED/PARTIAL`; some counters are proxy/approx |
| `VqsvPanelRuntime.tickPetmap()` | up/down row, left/right tab, back to record, confirm traces pending detail | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.renderPetmap()` | loads `petmap.ui`, draws tabs, rows, owned marker, owned sprite preview, scrollbar | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.tickBadge()` | grid navigation, back to record or bag-origin return | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.renderBadge()` | loads `badge.ui`, draws icons/detail/status | `PORTED/PARTIAL` |

## Existing Smoke Coverage

These checkpoints already exist and are relevant to this route:

| Checkpoint | Coverage | Status |
| --- | --- | --- |
| `panel_petmap_record_open_from_gamemenu` | gamemenu row 3 opens record | `PORTED/PARTIAL` |
| `panel_petmap_open_from_record` | record option 0 opens petmap | `PORTED/PARTIAL` |
| `panel_petmap_navigation` | petmap row navigation | `PORTED/PARTIAL` |
| `panel_petmap_tab_navigation` | petmap category navigation | `PORTED/PARTIAL` |
| `panel_petmap_back_returns_record` | petmap back to record | `PORTED/PARTIAL` |
| `panel_petmap_record_back_returns_gamemenu` | record back to gamemenu | `PORTED/PARTIAL` |
| `panel_badge_open_from_record` | record option 1 opens badge | `PORTED/PARTIAL` |
| `panel_badge_navigation` | badge grid navigation | `PORTED/PARTIAL` |
| `panel_badge_back_returns_record` | badge back to record | `PORTED/PARTIAL` |
| `panel_badge_record_back_returns_gamemenu` | record back after badge path | `PORTED/PARTIAL` |
| `panel_petmap_mouse_wheel_scrollbar_no_confirm` | petmap wheel moves scrollbar/selection without confirm | `PORTED/PARTIAL` |
| `panel_petmap_mouse_wheel_hover_click_viewport` | petmap wheel then hover/click maps visible row correctly | `PORTED/PARTIAL` |

No new smoke was run for this audit-only slice.

## Gaps / Risks

- `record.ui` option `0` source gate `game.j.p().l(5)` is not fully closed in rebuild. Current route smoke opens petmap, but the missing-item warning path is `PENDING`.
- `record.ui` counters are not exact source field parity. Divine/beast count and playtime are especially `APPROX`.
- `petmap.ui` exact `q.X/q.Y/q.a()` lifecycle is not fully mirrored. Current rebuild is source-shaped and early-route useful, not a full encyclopedia VM.
- `petmap.ui` confirm/detail behavior is `PENDING`; source snippet loaded here does not prove a confirm route.
- `badge.ui` exact `q.C[8][2]` state lifecycle is `PORTED/PARTIAL`, with proxy state used in rebuild. Bag-origin badge back was already handled separately in `401`.
- No original-vs-rebuild pixel compare has been done for `record.ui`, `petmap.ui`, or `badge.ui`; visual status cannot be called pixel-perfect.

## Closeout Status

`Do giam / record.ui`: `PORTED/PARTIAL`.

Route open/navigation/back is already implemented enough for panel smoke:

- gamemenu row `3` -> `record.ui`
- record option `0` -> `petmap.ui`
- record option `1` -> `badge.ui`
- petmap navigation/tab/back
- badge navigation/back

Remaining work is mostly exact source-data parity and one missing warning/detail path:

- `record.ui` missing petmap item warning from `game.j.p().l(5) == false`
- exact `record.ui` counters/playtime
- exact `petmap.ui` category/ownership lifecycle
- exact `badge.ui` `q.C[8][2]` lifecycle
- petmap confirm/detail behavior, if source later proves one

## Recommended Next Slice

Recommended next step: create `406_panel_record_closeout_smoke_matrix.md` and run/lock the existing record/petmap/badge PNG checkpoints as the closeout baseline.

If the user wants a code slice instead of smoke closeout, the safest next implementation slice is:

`406_badge_qc_exact_state_parity`

Reason: badge data has a compact source state shape (`q.C[8][2]`) and visible UI impact, while petmap full encyclopedia ownership/category parity is broader.
