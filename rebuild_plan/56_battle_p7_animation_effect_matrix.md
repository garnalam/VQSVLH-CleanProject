# 56 Battle P7 Animation Effect Matrix

Status: PORTED/PARTIAL for the first direct-damage P7 slice.

## Source Facts

- `game.d.a(byte)` P7 entry (`case 7`) prepares attacker/target UI, sets flags `z/A`, then calls `n()`.
- `game.d.n()` loads the current skill row from `effect.mid`: `O = ao[h.D]`.
- `effect.mid` rows are 7-byte chunks:
  - chunk[0]: actor side for the effect step. `0` means target-side actor in the observed P7 path; non-zero means attacker-side actor.
  - chunk[1]: `1` creates an `ah` special effect from `speffect.mid`; `0` applies actor animation directly.
  - chunk[2]: animation id or speffect id.
  - chunk[3]: animation parameter/state.
  - chunk[4]/[5]/[6]: trigger frame / state-change hooks used by source during P7 update.
- P7 draw path in `game.d.b(Graphics)` draws actors, optional `H` special effect, then battle floating text via `c(Graphics)`.
- Damage is not applied at P7 entry. It happens later in P7 update after the actor/effect sequence reaches the damage point:
  - target HP is reduced by `((b)h.p).k(Z[0])`;
  - floating damage text is queued with `a("-" + Z[0], byte 0, ...)`;
  - `V()` advances/removes floating text using `blood.mid`.
- For current smoke skill 10:
  - `db.mid aq.c[1][10] = [1,127,539,100,0,45,0,-1,-1,0]`
  - `effect.mid[10] = [0,0,21,1,-1,-1,0]`
  - This is a direct-damage target actor animation row with no `speffect.mid` special effect.

## Ported In Rebuild

| Source state/helper | Source resource | Rebuild equivalent | Status |
| --- | --- | --- | --- |
| `game.d case 7` entry | `effect.mid` | `SourceBattleRuntime.prepareP7()` loads selected skill effect row | PORTED/PARTIAL |
| `game.d.n()` | `effect.mid` | `VqsvBattleAnimationTables.effectRow(skillId)` | PORTED |
| `game.d.c(Graphics)` damage text path | `blood.mid` | `battleP7DamageText` + source-shaped damage marker | PARTIAL |
| P7 actor animation step | `effect.mid chunk[2]/[3]` | `VqsvBattleRenderer` draws selected side using animation state from row | PORTED/PARTIAL |
| HP reduction timing | `game.d P7 update` + `game.b.b(target)` | damage applies after animation phase, not immediately on state entry | PORTED |
| P7 exit | `q()/p()/P1/P8/P9` | exits to P1/P8/P9 after damage text phase | PORTED/PARTIAL |

## Smoke

- `battle_elder_p7_anim_start.png`
  - Reaches P7 phase 1 before damage.
  - For skill 10, visible special effect is intentionally absent because `effect.mid[10][1] == 0`.
- `battle_elder_p7_damage_frame.png`
  - Damage has applied, enemy HP drops, floating damage marker is visible.
- `battle_elder_p7_after_resolve.png`
  - P7 exits to the next battle dispatch phase.
- Regression route smoke:
  - Sophie battle -> branch 78.
  - Bunny battle -> result -1 and return task.
  - Elder battle -> reward state.

## Still Partial / Pending

- Pixel-perfect actor motion is not complete. Source uses `f` actor animation objects and `cpos.mid/pos.mid`; rebuild currently applies the effect row animation state to the rendered battle sprite.
- `speffect.mid` via `ah` is not ported in this slice. Skill 10 does not require it, but skills with `effect.mid chunk[1] == 1` remain pending.
- `blood.mid` texture path is only source-shaped. Rebuild shows a damage marker and text; it does not yet render MIDP `blood_0..2` exactly with `game.d.a(Graphics, Image, ...)`.
- Trigger hooks `chunk[4]/[5]/[6]` are documented but not fully simulated for multi-step effect rows.
- Buff/debuff visual queues from `bufDebuf.mid` remain pending.
