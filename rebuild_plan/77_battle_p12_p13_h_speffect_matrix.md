# 77 Battle P12/P13 H Speffect Matrix

Status: SOURCE AUDIT plus CODE SLICE for active-queue `H` special effects.

Scope: `game.d` states `12`/`13`, `bufDebuf.mid` kind `1` segments, and
`speffect.mid` rows that are actually reachable through the source `ai` visual
gate.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:71` | `ai = {{3,5,13},{0,1,2,3,8,9,10}}`; only these buff/debuff ids play visual queue rows. Other ids are applied immediately with no `H/u` visual. |
| `game/d.java:469..491` | Helper `a(b)` returns `false` and starts visual playback only when current bank/id is present in `ai`; otherwise it calls `b.o/q()` and duration decrement immediately. |
| `game/d.java:494..517` | Helper `o()` starts each visual segment. Segment kind `1` creates `H = new ah()` from `speffect.mid`; segment kind `0` calls `b.a(short,byte)` to create actor action `u`. |
| `game/d.java:1198..1243` | P12/P13 update ticks `u` and `H`, supports trigger `[3]`, hides actor while `H` plays, then restores actor. |
| `modules/script/decoded/data__script__bufDebuf.mid.json` | Maps buff/debuff ids to active-queue visual rows. |
| `modules/script/decoded/data__script__speffect.mid.json` | Maps speffect ids to AH type and raw params. |
| `modules/source_code/decoded/decompiled_source_cfr/ah.java` | AH renderer behavior for types `1`, `8`, `9`, `12` used by reachable P12/P13 `H` segments. |

## Reachable Kind `1` Matrix

`bank 0` means buff, `bank 1` means debuff.

| Bank | Effect id | Source visual row | Segment | Speffect id | AH type | Rebuild status |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| 0 | 3 | `ap[3] = [0,33,0,0] [1,7,0,-1]` | 1 | 7 | 9 | PORTED/PARTIAL via type9 color overlay. |
| 0 | 5 | `ap[6] = [0,23,0,-1]` | none | none | none | PORTED/PARTIAL through type0 actor action only. |
| 0 | 13 | `ap[4] = [1,17,0,-1]` | 0 | 17 | 1 | PORTED/PARTIAL via type1 texture overlay. |
| 1 | 0 | `aq[0] = [1,18,0,-1]` | 0 | 18 | 9 | PORTED/PARTIAL via type9 color overlay. |
| 1 | 1 | `aq[1] = [1,14,0,-1]` | 0 | 14 | 12 | PORTED/PARTIAL in this slice. |
| 1 | 2 | `aq[2] = [0,21,0,0] [1,6,0,0]` | 1 | 6 | 8 | PORTED/PARTIAL in this slice, after type0 trigger. |
| 1 | 3 | `aq[3] = [0,21,0,-1]` | none | none | none | PORTED/PARTIAL through type0 actor action only. |
| 1 | 8 | `aq[5] = [1,0,0,-1] [0,25,0,-1]` | 0 | 0 | 9 | PORTED/PARTIAL via type9 color overlay; following type0 path covered by actor-action runtime shape. |
| 1 | 9 | `aq[6] = [1,12,0,-1]` | 0 | 12 | 12 | PORTED/PARTIAL via same type12 renderer as debuff 1. |
| 1 | 10 | `aq[7] = [1,19,0,-1] [1,6,0,-1]` | 0 | 19 | 9 | PORTED/PARTIAL via type9 color overlay. |
| 1 | 10 | `aq[7] = [1,19,0,-1] [1,6,0,-1]` | 1 | 6 | 8 | PORTED/PARTIAL via same type8 renderer as debuff 2. |

## Non-Reachable Kind `1` Rows

These rows exist in `bufDebuf.mid`, but source `ai` does not allow their ids to
enter visual playback. They are applied immediately by `game.d.a(b)`.

| Bank | Effect id | Speffect ids in row | AH type(s) | Reason not ported now |
| --- | ---: | --- | --- | --- |
| 0 | 0,1,4,7,10,12 | 16,15 | 9,1 | Buff id not in `ai[0]`; source skips visual. |
| 0 | 6,14 | 4,17 | 7,1 | Buff id not in `ai[0]`; type7 is not source-called by P12/P13 active queue. |
| 0 | 8,9,11 | 19/18,15 | 9,1 | Buff id not in `ai[0]`; source skips visual. |
| 1 | 4,5,6,7 | 1,11 | 9,1 | Debuff id not in `ai[1]`; source skips visual. |

Conclusion: active queue kind `1` only needs AH types `1`, `8`, `9`, and `12`
for source-reachable P12/P13 visual playback. Types `0`, `6`, `7`, `10`, `11`,
`13`, `14`, and `15` are not called by the source active queue gate in this
slice.

## Code Mapping

| AH type | Source behavior | Rebuild equivalent | Status |
| ---: | --- | --- | --- |
| 1 | Texture/scroll blend overlay in `ah`; used by speffect 15/17/11. | Existing `applyAhType1Texture(...)` path. | PORTED/PARTIAL. |
| 8 | Repeated transformed copy with per-step offsets; used by speffect 6. | New `drawP7SpecialType8(...)` uses raw row timing/step triples and alpha-offset overlay. | PORTED/PARTIAL; not pixel-compared to Java ME `l.a/l.b`. |
| 9 | Color/alpha flash overlay; used by speffect 0/1/7/16/18/19. | Existing `applyAhType9Transform(...)` path. | PORTED/PARTIAL. |
| 12 | Two alpha copies with frame offset tracks; used by speffect 12/14. | New `drawP7SpecialType12(...)` uses raw row frame offsets and alpha layers. | PORTED/PARTIAL; not pixel-compared to Java ME `drawRGB`. |

## Smoke Evidence

| Smoke PNG | What it proves |
| --- | --- |
| `rebuild_game/build_intro_demo/battle_p12_debuff1_type12_special.png` | Debuff id 1 enters P12 visual queue and renders `speffect 14`, AH type 12. |
| `rebuild_game/build_intro_demo/battle_p12_debuff2_type8_special.png` | Debuff id 2 plays type0 actor action and advances to `speffect 6`, AH type 8. |
| `rebuild_game/build_intro_demo/battle_p12_debuff3_type0_actor_mid_regression.png` | Existing type0 actor-action path still ticks after adding type8/type12. |

## Remaining Honest Gaps

- Full generic `ah` renderer is still not implemented. This slice ports only
  types that P12/P13 source actually calls through the visual gate.
- Type8/type12 are source-shaped by row timing and offsets, but not
  pixel-compared against the MIDP renderer.
- Actor hide/restore during `H` is approximated by overlay visibility; source
  calls `b2.b(false/true)` around `H`.
