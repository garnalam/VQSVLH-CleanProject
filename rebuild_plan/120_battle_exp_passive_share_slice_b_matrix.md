# 120 Battle EXP Passive Share Slice B Matrix

Status: PORTED/PARTIAL + CHECKPOINTS PASS.

Purpose:

- Continue after Slice A checkpoints passed.
- Port the passive/share EXP branches inside `game.d.h(b defeated)`.
- Keep scope to EXP producer only; no battle UI, no intro/world changes.

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`

## Source Facts

### Direct participants: `game.d.x`

In `game.d.h(b defeated)`, source iterates `x` first:

```java
b3 = (b)x.elementAt(n3);
n2 = n5 / n6 * aH[n6 - 1] * by / 1000;
if (b3.f((byte)5)) {
    n2 = n2 * (aq.c[3][5][5] + 100) / 100;
}
b3.B += n2;
if (!j.contains(b3)) j.addElement(b3);
```

Meaning:

- `x` is the direct participant vector.
- `B` is pending EXP.
- `j` is the post-battle EXP display queue.
- `f(5)` is checked on `game.b.c[5]`, mapped in rebuild as
  `BattleUnit.STAT_FORM`.
- Multiplier param comes from `aq.c[3][5][5]`, mapped in rebuild through
  `VqsvBattleTables.status(5).raw[5]`.

### Reserve/global share: `p.c(7,0)==2`

Source then iterates all party pets not alive/eligible participants:

```java
if (!this.c(n3).S() || x.contains(this.c(n3))) continue;
if (p.c((byte)7, (byte)0) == 2) {
    // level factor uses b3.s(), the last direct participant, not reserve pet
    n2 = n5 / n6 * aH[n6 - 1] * by / 3000;
    this.c(n3).B += n2;
    this.c(n3).c();
    if (!j.contains(this.c(n3))) j.addElement(this.c(n3));
    continue;
}
```

Meaning:

- `game.g.B[7][0] == 2` enables global reserve share.
- Divisor is `3000`, not `1000`.
- Source uses the last direct participant `b3.s()` for the level factor.
  This looks odd, but it is source behavior and must be preserved.

### Reserve form share: `f(6)`

If global share is not active:

```java
if (!this.c(n3).f((byte)6)) continue;
// level factor again uses b3.s()
n2 = n5 / n6 * aH[n6 - 1] * by / 1000;
this.c(n3).B += n2;
this.c(n3).c();
if (!j.contains(this.c(n3))) j.addElement(this.c(n3));
```

Meaning:

- Reserve pet form/status `c[5] == 6` receives reserve share.
- Divisor is `1000`.
- Level factor again follows last direct participant, not reserve pet.

## Rebuild Mapping

| Source | Rebuild mapping | Status |
| --- | --- | --- |
| `game.d.x` | `VqsvBattleRuntime.sourceExpParticipants` | PORTED in Slice A |
| `game.d.j` | `VqsvBattleRuntime.sourceExpDisplay` | PORTED in Slice A |
| `game.b.B` | `SourcePetState.sourcePendingExp` | PORTED in Slice A |
| `game.b.c[5]` | `BattleUnit.STAT_FORM` from `SourcePetState.sourcePayload[2]` | PORTED |
| `b.f(5)` | `BattleUnit.hasSourceFormStatus(5)` | PORTED |
| `b.f(6)` | `BattleUnit.hasSourceFormStatus(6)` | PORTED |
| `aq.c[3][5][5]` | `VqsvBattleTables.status(5).raw[5]` | PORTED |
| `game.g.B[7][0]` | `Scene.sourceGlobalState[7][0]` | PORTED for Slice B |

## Slice B Checkpoints

| Checkpoint | Purpose | Expected |
| --- | --- | --- |
| `battle_exp_vector_participant_form5_multiplier` | Direct participant has `f(5)` | EXP uses status 5 multiplier |
| `battle_exp_vector_reserve_form6_share` | Reserve pet has `f(6)` | Reserve joins `j`, gets divisor `1000` share |
| `battle_exp_vector_global_state7_share` | `game.g.B[7][0] == 2` | Non-participant reserve joins `j`, gets divisor `3000` share |

Checkpoint result:

| Checkpoint | Status | PNG |
| --- | --- | --- |
| `battle_exp_vector_participant_form5_multiplier` | PASS | `rebuild_game/build_intro_demo/sliceB_battle_exp_vector_participant_form5_multiplier.png` |
| `battle_exp_vector_reserve_form6_share` | PASS | `rebuild_game/build_intro_demo/sliceB_battle_exp_vector_reserve_form6_share.png` |
| `battle_exp_vector_global_state7_share` | PASS | `rebuild_game/build_intro_demo/sliceB_battle_exp_vector_global_state7_share.png` |

Regression run:

- `build.ps1`: PASS.
- Java mojibake scan: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- Slice A checkpoints rerun: PASS.

## Remaining Out Of Scope

| Item | Status | Reason |
| --- | --- | --- |
| Post-win passive heal in `game.d.X()` | PENDING | Separate consumer/commit behavior, not Slice B producer |
| `p.k(0)` quest counter after EXP | PENDING | Not part of EXP share vector |
| Save/global persistence for `game.g.B` | PARTIAL | Slice B adds runtime table only; save parity later |

## Next Roadmap Step

With Slice B producer complete, the next source-backed step is post-EXP
consumer parity:

1. Audit `game.d.X()` and related passive heal/save side effects.
2. Decide whether to port post-win passive heal first or return to P21/P17 catch
   edge cases, depending on current battle roadmap priority.
3. Keep checks PNG/headless only.
