# 137 Battle P7 Original-vs-Rebuild Frame Compare

## Scope

Compare P7 base actor sprite frames for state `1` attack and state `2` hit/recoil on the active Elder and Bunny battle routes.

This slice uses the strongest original reference currently available in the repo:

- source sprite binaries from `modules/spr/original`;
- source decoded image assets from `modules/img/decoded`;
- source battle position tables from `pos.mid` / `cpos.mid`;
- source animation rows loaded through `SpriteAnim`.

No original MIDP/client runtime capture pair exists yet. Therefore this is a source-asset-vs-rebuild frame compare, not a full pixel-perfect runtime claim.

## Source Paths

- `modules/source_code/decoded/decompiled_source_cfr/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `rebuild_game/src/main/java/VqsvSpriteRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Compare Method

The smoke harness now renders the current rebuild frame normally, then renders an expected transparent sprite-only layer from the same source sprite data:

1. Resolve route and side.
2. Assert current rebuild visual id, state, and cursor.
3. Compute actor anchor from source `pos.mid` / `cpos.mid`, matching `VqsvBattleRenderer`.
4. Render expected sprite using source `SpriteAnim` at the same anchor and orientation.
5. Compare non-transparent expected sprite pixels against the rebuild frame.

This catches state/cursor/anchor/orientation mismatches for the base pet sprite while avoiding a false claim about the full MIDP battle renderer.

## Checkpoints

| Checkpoint | Route | Side | State | Status |
| --- | --- | --- | --- | --- |
| `battle_elder_p7_state1_attack_source_compare` | Elder battle | player attacker | `1` attack/action | SOURCE-ASSET COMPARE |
| `battle_elder_p7_state2_hit_source_compare` | Elder battle | enemy target | `2` hit/recoil | SOURCE-ASSET COMPARE |
| `battle_bunny_p7_state1_attack_source_compare` | Bunny battle | player attacker | `1` attack/action | SOURCE-ASSET COMPARE |
| `battle_bunny_p7_state2_hit_source_compare` | Bunny battle | enemy target | `2` hit/recoil | SOURCE-ASSET COMPARE |

## Status

| Area | Status | Note |
| --- | --- | --- |
| P7 base sprite state/cursor compare | PORTED/PARTIAL | Compared against source sprite/cpos assets in smoke. |
| P7 source asset pixel compare | PORTED/PARTIAL | Non-transparent expected sprite pixels must match rebuild output. |
| Full original-vs-rebuild runtime pixel compare | PENDING | Needs original client/MIDP frame captures for the same ticks. |
| Patch need after this audit | NONE FOUND | All four source-asset compare checkpoints passed; no frame/cell patch was justified in this slice. |

## Smoke Result

Passed:

- `battle_elder_p7_state1_attack_source_compare`
- `battle_elder_p7_state2_hit_source_compare`
- `battle_bunny_p7_state1_attack_source_compare`
- `battle_bunny_p7_state2_hit_source_compare`
- route regressions: Sophie, Bunny, Elder

No mismatch was found in P7 base sprite state/cursor/anchor/orientation for these route frames.

## Next

If all four checkpoints pass, the next battle visual debt is not state `1/2` cursor anymore; it is full P7 frame capture parity with original client frames, or the next documented visual area in roadmap if original captures are unavailable.
