# 403 - Panel Bag q.O Case10 Transmit Route + Render Closeout

Date: 2026-07-15

## Scope

Port the audited `q.O` special row `10` route only:

- `bag.ui` tab `3` / `Dac thu`
- `aq.c[5][10] = [511,54,512]`
- `Truyen Tong Thach`
- open `/data/ui/transmit.ui`
- render/navigate/back/confirm
- confirm writes the source transmit world target tuple

No live client was opened. This slice used headless smoke PNG only.

## Source-Backed Behavior Ported

`VqsvPanelRuntime` now has `Mode.TRANSMIT`.

Route:

`bag.ui b=3 q.O case10` -> `o.a((byte)24)` -> `game.k.h transmit.ui`.

Destination table ported from source `game.k.z/A`:

| Index | Destination | Tuple `[scene,room,x,y,G,t]` |
| ---: | --- | --- |
| 0 | Thuy Kimura | `[1,0,196,208,0,-1]` |
| 1 | Bich Thuy thanh | `[2,1,196,208,0,-1]` |
| 2 | Nguyen Moc Thanh | `[3,3,196,208,0,-1]` |
| 3 | Niem Tho Thanh | `[4,5,320,352,0,-1]` |
| 4 | Hac Thach thanh | `[5,3,320,196,0,-1]` |
| 5 | Thien khong | `[7,2,288,112,0,-1]` |
| 6 | Xa co | `[8,0,160,144,0,-1]` |

Confirm writes:

- `sourceTransmitScene`
- `sourceTransmitRoom`
- `sourceTransmitX`
- `sourceTransmitY`
- `sourceTransmitG`
- `sourceTransmitT = -1`
- `sourceTransmitConfirmed = true`

It does not consume or mutate q.O row `10`, matching the audited source slice.

## UI Runtime Ported

`transmit.ui` renderer now uses decoded widgets:

- frame/background widgets `1..4`
- title widget `11`
- destination rows `5..9`
- scrollbar widgets `12/13`
- softkeys `14/15`

Input:

- Up/down changes selected destination and keeps the 5-row viewport valid.
- Back closes `transmit.ui` and restores `bag.ui` tab `3`, selected q.O row.
- Confirm closes panel and commits the transmit tuple.

Mouse hover/click is also wired for the 5 visible destination rows.

## Smoke PNGs

Output folder:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_qo_case10_403`

New checkpoints:

- `panel_bag_qo_case10_transmit_open.png`
- `panel_bag_qo_case10_transmit_navigation.png`
- `panel_bag_qo_case10_transmit_back.png`
- `panel_bag_qo_case10_transmit_confirm_world_target.png`

Assertions include:

- source row `[511,54,512]`
- route trace contains `o.a(24) game.k.h transmit.ui`
- visible `transmit.ui` frame/list/scrollbar pixels
- back restores `bag.ui` tab `3`
- confirm writes exact tuple for selected row `2`: `[3,3,196,208,0,-1]`
- q.O row `10` is not consumed
- q.O case `6` badge route still passes in the same suite

## Verification

Passed:

- `.\build.ps1`
- `java "-Dvqsv.modules=C:\Users\Dell\Downloads\ResourcesVQSV\modules" -cp build\libs\vqsv-liet-hoa-rebuild.jar VqsvIntroDemo --smoke-suite panel_bag build_intro_demo\panel_bag_qo_case10_403`
  - `panel_bag` passed `20/20`
- `java "-Dvqsv.modules=C:\Users\Dell\Downloads\ResourcesVQSV\modules" -cp build\libs\vqsv-liet-hoa-rebuild.jar VqsvIntroDemo --check`

## Remaining / Next Roadmap Step

Panel Bag q.O case `10` route/render is now closed at descriptor level.

Remaining if we want deeper parity later:

- make confirm actually invoke the world load/resume path instead of only writing the target tuple
- pixel-perfect Java ME text timing for long destination marquee

Next recommended roadmap step:

`404 - Panel Bag q.O Special Tab Closeout / Remaining Routes Decision`

Goal: summarize q.O routes already closed (`5`, `6`, `7/8/9`, `10`, egg row `0`) and choose whether to deepen actual world teleport resume, or return to the next softkey/panel area.
