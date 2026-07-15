# 392 Battle P23 Learn Skill Full Slot Replace Audit

Status: SOURCE-PARITY LOCKED.

Purpose:

- Audit whether the original game supports replacing an old skill when the pet already has 5 skill slots.
- Lock rebuild behavior with a smoke checkpoint so this path is not accidentally changed into a non-source feature.

## Source Finding

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
- `modules/ui/decoded/data__ui__choiceskill.ui.json`

### Level-up learn gate

In S60 source `game.k.aq()` the level-up UI only prepares the learn-skill candidate array when both checks pass:

```java
if (i3.F() < 5 && i3.F() < i3.t() / 10 + 1) {
    this.x = i3.G();
    widget51 = "Co the hoc tap ky nang moi";
} else {
    widget51 = "";
}
```

Meaning:

- `i3.F()` is current skill count.
- If skill count is already 5, `this.x` is not set.
- `game.k.ar()` only enters battle state 23 when `this.x != null`.
- Therefore full-slot pets do not open `choiceskill.ui` after level-up.

### Learn mutation

Source `game.i.g(byte skillId)` only inserts into the first `-1` slot:

```java
for (int i2 = 0; i2 < this.A.length; ++i2) {
    if (this.A[i2] != -1) continue;
    this.A[i2] = by;
    this.P = (byte)(this.P + 1);
    this.z[i2] = aq.c[1][by][5];
    return;
}
```

There is no replacement branch here. If all 5 slots are occupied, the method returns without mutation.

## Rebuild Decision

Do not add replace-skill behavior to P23 as source parity.

Current rebuild behavior remains:

- `BattleUnit.sourceCanLearnAfterLevelUp()` requires `skillCount < skillIds.length`.
- `BattleUnit.learnSourceSkill(skillId)` returns false if `skillCount >= skillIds.length`.
- P23 opens only when source candidate list exists after the level-up gate.

If we later want a modern QoL "replace skill" feature, it must be a separate extension task, not mixed into the source-parity battle roadmap.

## Smoke Coverage

Added checkpoint:

- `battle_exp_levelup_full_skill_slots_no_replace`

The checkpoint:

- Seeds a pet with 5 skills.
- Forces a battle level-up.
- Asserts `levelUp.ui` appears.
- Asserts `learnSkills=[]`.
- Presses through the level-up UI.
- Asserts `choiceskill.ui` never opens.
- Asserts the 5-skill payload stays unchanged.

Run result:

- PASS: `rebuild_game/build/smoke/battle_exp_levelup_full_skill_slots_no_replace.png`
- Regression PASS: `battle_exp_levelup_choiceskill_ui`
- Regression PASS: `battle_exp_levelup_learn_skill_done`
- Regression PASS: `panel_petstate_petsetting_skill_data_binding_five_slots`
- `build.ps1`: PASS
- `com.vqsv.rebuild.Main --check`: PASS

## Roadmap Note

This closes the "replace skill when full slot" question for source parity.

Next sensible step for `petsetting.ui` row 4 is UI/runtime polish only if needed:

- exact `skill.ui` generic widget runtime,
- exact row-title/description marquee timing,
- exact Java ME clip/baseline parity.

For deeper gameplay, return to battle skill/status roadmap rather than inventing replace logic.
