# 63 Battle Actor Action States Matrix

Status: SOURCE AUDIT + SMALL PORT.

Scope: battle actor base states used by `game.b.d(byte)` and P7 source calls.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `game.b.d(byte)`
  - `game.b.a(Graphics)`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P7 entry around `case 7`
  - P7 update around target/attacker state changes
  - `U()` post-damage/death state helper
- `modules/source_code/decoded/decompiled_source_cfr/d.java`
  - sprite animation cursor/duration methods
- `modules/script/decoded/data__script__speffect.mid.json`

## State Matrix

| State | Source call | Meaning observed | Extra source effect | Rebuild status |
| --- | --- | --- | --- | --- |
| `0` | `actor.d((byte)0)` | Idle / normal battle pose. Uses sprite animation state 0. | None. | PORTED |
| `1` | `actor.d((byte)1)` | Action / attack pose. Source sets base sprite state 1 and may create `L` special for specific species. | Species-specific `z(20..28)` for ids `0,10,75,87,91,92,97,98`. | PORTED/PARTIAL: base state 1; species `L` extras pending. |
| `2` | `actor.d((byte)2)` | Hurt/recover pose after being hit. | None in `d(byte)`, base sprite state 2. | PORTED |
| `3` | `actor.d((byte)3)` | Down/death pose. Source may dispose base sprite and starts AH type 16 row `[16,0,0,4]` if battle flag allows. | AH type 16 down effect. | PENDING for full death effect. |
| `4` | `actor.d((byte)4)` | Hold/loop state using sprite state 4. | None. | PENDING, no known current smoke path. |

## P7 Source Calls

| Source location | Behavior | Rebuild mapping |
| --- | --- | --- |
| P7 entry, after `n()` | If first chunk side `O[J*7] == 0`, attacker `h.d(1)`; else attacker `h.d(0)`. | Port target for this slice. |
| P7 target-side animation complete | Target `d(2)` when sequence ends or next chunk switches to attacker-side. | Port target for this slice. |
| P7 `H.a()` start | Attacker `h.d(0)` and owner actor hidden via `b(false)`. | Already source-shaped in P7 runtime. |
| Post damage helper `U()` | Target state `3` if HP <= 0, otherwise `0`. | Nonlethal recover to `0`; death effect pending. |

## Honest Limits

This slice improves battle feel by making attacker action and target hurt states
visible. It does not yet port:

- Species-specific `L` effects spawned by state `1`.
- Death/down AH type 16 for state `3`.
- State `4` paths.
- Pixel-perfect timing of every actor state compared with MIDP.
