# 121 Battle EXP Post Consumer `game.d.X()` Matrix

Status: PORTED/PARTIAL + CHECKPOINTS PASS.

Purpose:

- Audit source `game.d.X()` after Slice A/B EXP producer work.
- Separate producer `game.d.h(b defeated)` from consumer `game.d.X()`.
- Identify exact rebuild gaps before coding post-EXP consumer parity.

Scope:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`

No live client/game. No PNG smoke in this audit step.

## Source Call Sites

| Source location | Trigger | Meaning |
| --- | --- | --- |
| `game.d.a(b, boolean)` around enemy-team defeat | `u[1] >= s.length` then `game.d.X(); this.a((byte)8);` | Player won; prepare EXP display state P8. |
| `game.d` state branch around case `1` | `game.d.X(); this.a((byte)8);` | Another win/finish branch enters P8. |

Important:

- `game.d.X()` runs before entering P8.
- It consumes pending EXP `game.b.B` already produced by `game.d.h(b defeated)`.
- P8 UI then reads committed pet EXP/state from `game.d.j`.

## Source Function

Source:

```java
private static void X() {
    int n2;
    for (n2 = 0; n2 < j.size(); ++n2) {
        if (((b)j.elementAt(n2)).S()) {
            ((b)j.elementAt(n2)).g(((b)game.d.j.elementAt((int)n2)).B);
            ((b)game.d.j.elementAt((int)n2)).B = 0;
            ((b)j.elementAt(n2)).d(false);
            continue;
        }
        j.removeElementAt(n2);
        --n2;
    }
    if (p.c((byte)0, (byte)0) == 2 && p.c((byte)0, (byte)1) == 1) {
        for (n2 = 0; n2 < game.d.p.A; ++n2) {
            if (!game.d.p.z[n2].S()) continue;
            game.d.p.z[n2].u(game.d.p.z[n2].d[1]
                + aq.c[0][game.d.p.z[n2].q()][5] * aq.c[2][0][6] / 100);
            game.d.p.z[n2].l(aq.c[0][game.d.p.z[n2].q()][5] * aq.c[2][0][6] / 100);
        }
    }
}
```

## Source Helper Meanings

| Source helper | Source behavior | Rebuild equivalent | Status |
| --- | --- | --- | --- |
| `game.b.S()` | `return this.d[1] > 0;` | `BattleUnit.alive()` | PORTED |
| `game.b.g(int)` | If level < 50, add to `S` EXP and clamp non-negative. | `BattleUnit.addSourceExp(int)` | PORTED |
| `game.b.B` | Pending EXP award field before commit. | `SourcePetState.sourcePendingExp` | PORTED/PARTIAL |
| `game.b.d(false)` | Set `Y=false`, clears active/in-battle marker. | `SourcePetState.sourceD(false)` | MISSING in `X()` path |
| `game.g.B[0][0]` / `[0][1]` | Global passive flags checked by `p.c(0,0/1)`. | `Scene.sourceGlobalState[0][0/1]` exists after Slice B | PARTIAL |
| `game.b.u(int)` | Set HP pre-value `Q`, clamped to max HP. | No explicit visible previous-HP field in rebuild. | MISSING/PENDING |
| `game.b.l(int)` | Heal current HP by amount, clamped to max HP. | `BattleUnit.heal(int)` then persist payload HP. | PARTIAL |

## Consumer Step Matrix

| Step | Source | Effect | Rebuild current | Gap |
| --- | --- | --- | --- | --- |
| 1 | Iterate `game.d.j` | Consumer walks post-battle display queue. | `sourceExpDisplay` is committed in `prepareSourceExpAwards()`. | PORTED/PARTIAL |
| 2 | If pet alive `S()` | Commit `B -> S` via `g(B)`. | `unit.addSourceExp(pending)` then `persistBattleUnit(unit)`. | PORTED |
| 3 | Clear `B` | `B = 0`. | `sourcePendingExp = 0`. | PORTED |
| 4 | Clear active marker | `d(false)` on each alive `j` pet. | Not done during commit. | MISSING |
| 5 | Remove dead `j` entries | Dead pets removed from `j`. | `pruneSourceExpVectors()` removes dead before prepare and display. | PORTED/PARTIAL |
| 6 | Global passive heal gate | `p.c(0,0)==2 && p.c(0,1)==1`. | `sourceGlobalState` table exists but no consumer logic. | MISSING |
| 7 | Passive heal amount | `aq.c[0][species][5] * aq.c[2][0][6] / 100`. | Tables available: species group 0, group 2 row 0. | MISSING |
| 8 | Passive heal applies to all alive party pets | For every alive `p.z[n]`, call `u(oldHp + heal)` and `l(heal)`. | No all-party heal in `prepareSourceExpAwards()`. | MISSING |

Implementation update:

| Step | Rebuild status |
| --- | --- |
| Commit `B -> S` | PORTED |
| Clear `B` | PORTED |
| Clear `game.b.d(false)` marker | PORTED via `SourcePetState.sourceD(false)` |
| Remove dead `game.d.j` entries | PORTED in consumer helper |
| Passive heal gate `B[0][0]==2 && B[0][1]==1` | PORTED/PARTIAL |
| Passive heal amount from `aq.c[0][species][5] * aq.c[2][0][6] / 100` | PORTED |
| `game.b.u(...)` pre-heal `Q` display value | PENDING |

## Important Source Detail

The passive heal amount uses raw species table HP base at `aq.c[0][species][5]`,
not current calculated max HP. That means rebuild should not use
`BattleUnit.maxHp()` for the heal amount unless source table fallback is missing.

`u(currentHp + heal)` stores the pre-heal display value `Q`, then `l(heal)`
actually heals/clamps `d[1]`. Rebuild currently does not expose `Q`, so first
code slice can update HP/payload and trace `Q` as PARTIAL unless later UI needs
animated heal display.

## Rebuild Current State

In `VqsvBattleRuntime.prepareSourceExpAwards()`:

- Producer and consumer are currently collapsed into one method.
- `game.d.h(...)` producer is represented by adding `sourcePendingExp`.
- `game.d.X()` consumer is represented by immediate `addSourceExp(pending)`.
- Trace already says `passive-heal=PENDING`.

Current strengths:

- `B -> S` commit is source-shaped.
- Dead entries are pruned before display.
- P8 iterates `game.d.j` through `sourceExpDisplay`.
- Slice A/B checkpoints pass.

Current gaps:

- `game.b.d(false)` cleanup on each alive `j` pet is missing.
- `game.g.B[0][0]==2 && B[0][1]==1` passive heal is missing.
- Passive heal `Q` pre-heal display field is missing.
- Save/global persistence for `sourceGlobalState` remains PARTIAL from Slice B.

## Recommended Code Slice

Slice C: `game.d.X()` consumer parity, no UI changes.

Tasks:

1. Split helper naming in `VqsvBattleRuntime` so source trace distinguishes:
   - `game.d.h producer`
   - `game.d.X consumer`
2. During consumer commit:
   - commit pending `B -> S`;
   - clear `sourcePendingExp`;
   - call `pet.sourceD(false)` for alive `j` pets;
   - optionally reset source turn marker only if a source line proves it.
3. Add passive heal helper gated by:
   - `s.sourceGlobalState[0][0] == 2`
   - `s.sourceGlobalState[0][1] == 1`
4. Heal all alive party pets using:
   - `aq.c[0][species][5] * aq.c[2][0][6] / 100`
5. Persist each healed pet payload HP.
6. Trace exact source facts:
   - `game.d.X commit B->S`
   - `game.b.d(false)`
   - `game.d.X passive heal B[0][0/1]`

Suggested checkpoints:

| Checkpoint | Purpose |
| --- | --- |
| `battle_exp_consumer_x_clears_active_marker` | Pet in `j` has `sourceActive=false` after commit. |
| `battle_exp_consumer_x_removes_dead_j` | Dead `j` entry is removed and not displayed. |
| `battle_exp_consumer_x_passive_heal` | `sourceGlobalState[0][0]=2`, `[0][1]=1` heals all alive source pets. |

Checkpoint result:

| Checkpoint | Status | PNG |
| --- | --- | --- |
| `battle_exp_consumer_x_clears_active_marker` | PASS | `rebuild_game/build_intro_demo/sliceC_battle_exp_consumer_x_clears_active_marker.png` |
| `battle_exp_consumer_x_removes_dead_j` | PASS | `rebuild_game/build_intro_demo/sliceC_battle_exp_consumer_x_removes_dead_j.png` |
| `battle_exp_consumer_x_passive_heal` | PASS | `rebuild_game/build_intro_demo/sliceC_battle_exp_consumer_x_passive_heal.png` |

Regression run:

- `build.ps1`: PASS.
- Java mojibake scan: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- Slice A EXP vector checkpoints: PASS.
- Slice B passive/share checkpoints: PASS.

Required regression after code:

- `build.ps1`
- Java mojibake scan
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Slice A checkpoints
- Slice B checkpoints

## Roadmap Position

This is still Battle Roadmap Phase 4/5 boundary work:

- Phase 4 EXP/level-up queue producer is now stronger.
- Slice C closes the post-producer consumer before returning to larger Phase 5
  UI/catch/pet/item flows.
