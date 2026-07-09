# 190 Battle Phase 10-B Normal P7 AH Closeout Coverage

Status: CLOSEOUT / COVERAGE AUDIT.

Scope:

- Normal P7 `game.d.H` special effect chunks only.
- Source files/tables:
  - `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - `modules/source_code/decoded/decompiled_source_cfr/ah.java`
  - `modules/script/decoded/data__script__effect.mid.json`
  - `modules/script/decoded/data__script__speffect.mid.json`
  - `rebuild_game/src/main/java/VqsvBattleRuntime.java`
  - `rebuild_game/src/main/java/VqsvBattleRenderer.java`
  - `rebuild_game/src/main/java/VqsvSmokeHarness.java`

No client/JAR was opened for this audit.

## Source Facts

Normal P7 special chunks are the `effect.mid` 7-byte chunks where:

```text
chunk[1] == 1
```

Source route:

```text
game.d case 7
-> game.d.n()
-> if effectChunk[1] == 1:
     H = new ah()
     sourceSpeffect = speffect.mid[effectChunk[2]]
     runtime row passed to ah =
       [speffectType, actorX, actorY, actorSpriteId, actorAnimState, actorOrientation]
       + speffect[1..]
```

Important: `speffect.mid` rows alone do not contain actor snapshot fields.
`game.d.n()` prepends those fields before calling `ah.a(short[])`.

## Full Normal P7 AH Type Scan

Scan source:

```text
effect.mid rows -> every 7-byte chunk -> chunk[1] == 1 -> speffect row -> AH type
```

Result:

| AH type | Chunk count | Skills | Speffect rows | Renderer status |
|---:|---:|---|---|---|
| `1` | `16` | `4,5,24,25,35,44,45,51,57,58,61,62,64,65,67,68` | `11,13,15,17` | PORTED/PARTIAL |
| `7` | `6` | `25,34,35,41,43,47` | `4` | PORTED/PARTIAL |
| `8` | `4` | `12,23,26,37` | `6` | PORTED/PARTIAL |
| `9` | `27` | `4,5,11,15,17,21,27,32,37,38,42,44,45,48,50,51,52,53,54,56,58,62,64,65,68` | `0,1,5,7,8,9,10,16,18,19` | PORTED/PARTIAL |
| `12` | `4` | `2,8,55,57` | `12,14` | PORTED/PARTIAL |

Conclusion:

- Normal P7 uses only AH types `1`, `7`, `8`, `9`, and `12`.
- Normal P7 does not use AH types `11`, `13`, `14`, or `15`.
- AH types `11`, `13`, `14`, and `15` are state1 `L` species overlay work, not normal P7 `H` work.
- No normal P7 AH type is currently renderer-MISSING.

## Representative Visual Smoke Coverage

| AH type | Representative smoke | Source-backed route | Status |
|---:|---|---|---|
| `1` | `battle_elder_p7_speffect45_type1` | skill `45`, chunk1, `speffect 15` | PORTED/PARTIAL |
| `7` | `battle_phase10b_p7_type7_skill34_overlay` | skill `34`, chunk0, `speffect 4` | PORTED/PARTIAL |
| `8` | `battle_phase10b_p7_type8_skill12_overlay` | skill `12`, chunk1, `speffect 6` | PORTED/PARTIAL |
| `9` | `battle_elder_p7_speffect45_overlay` and `battle_elder_p7_skill15_chunk4_trigger` | skill `45` chunk0 / skill `15` chunk1 | PORTED/PARTIAL |
| `12` | `battle_phase10b_p7_type12_skill55_overlay` | skill `55`, chunk0, `speffect 12` | PORTED/PARTIAL |

The representative smokes prove renderer reachability and visible output for
each normal P7 AH type. They do not prove every `speffect` row variant or every
attacker/target side combination.

## Detailed Coverage Gaps

These are not renderer blockers. They are broader visual coverage gaps.

| Gap | Source-backed examples | Current status | Recommended handling |
|---|---|---|---|
| AH type `1` attacker-side | skills `62,64,65,68`, `speffect 15` | Renderer exists; representative smoke is target-side skill45 | Smoke-only later if attacker-side visual mismatch appears. |
| AH type `1` other rows | `speffect 11,13,17` | Renderer exists; row variants not all PNG-smoked | Add smoke-only row coverage only if visual route is selected. |
| AH type `7` multi-chunk users | skills `25,35,41,43,47` | Renderer exists; representative smoke is skill34 | Existing Phase 9 logic smokes cover some skills, but not dedicated visual AH assertions for each. |
| AH type `8` other skills | skills `23,26,37` | Renderer exists; representative smoke is skill12 | Smoke-only later; no new renderer needed. |
| AH type `9` attacker-side | skills `11,17,21,27,42,48,52,58,64,65` | Renderer exists; many logic smokes exist, but not all dedicated visual AH asserts | Good candidate if user reports self-side effect mismatch. |
| AH type `9` many color rows | `speffect 0,1,5,7,8,9,10,16,18,19` | Renderer exists; not every color row has a PNG-specific assert | Lower risk than missing renderer; keep as coverage debt. |
| AH type `12` row `14` | skills `2,8,57` | Renderer exists; smoke is row `12` via skill55 | Add smoke-only later if debuff1 family visual needs inspection. |
| Exact `l.a/l.b/drawRGB` behavior | all AH types | PARTIAL | Requires exact Java ME transform/pixel parity work or original capture compare. |
| Generic `ah.java` interpreter | all AH branches | PENDING | Current rebuild uses targeted renderers; acceptable while slices remain small. |

## Renderer Closeout Matrix

| AH type | Runtime gate | Duration source-shape | Renderer | Visual smoke | Closeout |
|---:|---|---|---|---|---|
| `1` | yes | `row[2]` | `drawP7SpecialEffect -> applyAhType1Texture` | yes | CLOSED / PARTIAL |
| `7` | yes | `row[2]` | `drawP7SpecialType7` | yes | CLOSED / PARTIAL |
| `8` | yes | `row[2]` | `drawP7SpecialType8` | yes | CLOSED / PARTIAL |
| `9` | yes | `row[6]`, interval `row[7]` | `applyAhType9Transform` | yes | CLOSED / PARTIAL |
| `12` | yes | `row[5]` | `drawP7SpecialType12` | yes | CLOSED / PARTIAL |

`CLOSED / PARTIAL` means:

- normal P7 can reach the AH type;
- renderer draws a visible source-shaped effect;
- smoke harness has at least one source-backed visual checkpoint;
- exact MIDP pixel parity and full generic `ah.java` interpretation remain out
  of scope.

## Smoke Evidence

Phase 10-B PNGs:

```text
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type12_skill55_start.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type12_skill55_overlay.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type12_skill55_after.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type8_skill12_start.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type8_skill12_overlay.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type8_skill12_after.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type7_skill34_start.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type7_skill34_overlay.png
rebuild_game/build/smoke/phase10b/battle_phase10b_p7_type7_skill34_after.png
```

Older normal P7 visual evidence:

```text
battle_elder_p7_speffect45_start
battle_elder_p7_speffect45_overlay
battle_elder_p7_speffect45_type1
battle_elder_p7_speffect45_after
battle_elder_p7_skill15_chunk4_trigger
```

Recent regression evidence:

```text
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
battle_phase10a_status_icons_mixed_order
```

## Honest Status

PORTED/PARTIAL:

- Normal P7 AH type reachability for every source-used AH type:
  `1/7/8/9/12`.
- Representative visual smoke for each source-used normal P7 AH type.
- Current manual battle routes still pass after type7/type8/type12 additions.

PENDING / NOT CLAIMED:

- Pixel-perfect MIDP parity.
- Exact Java ME `l.a(...)`, `l.b(...)`, and `drawRGB(...)` transform behavior.
- Dedicated visual PNG for every `speffect` row and every attacker/target side
  variant.
- Generic `ah.java` interpreter.

UNKNOWN:

- Whether any remaining visual mismatch is visible in original-client capture
  for a specific skill. This needs a selected skill and original reference
  image/video, not broad guessing.

## Recommendation

Do not keep looping inside Phase 10-B unless a specific skill visual looks wrong.
Renderer coverage for normal P7 AH types is closed enough to move on.

Two valid next paths:

1. Move to the next roadmap phase/slice.
2. If the user wants stricter visual coverage before moving on, do a smoke-only
   mini-slice:
   - attacker-side type9: skill `11` or `65`;
   - attacker-side type1: skill `65` chunk1;
   - type12 row14: skill `2` or `8`;
   - type7 multi-chunk: skill `25`.

Recommended next step:

```text
Phase 10-C / next battle visual slice: choose one concrete source-backed visual
gap only if there is a visible mismatch. Otherwise return to the main battle
roadmap instead of broad visual polishing.
```
