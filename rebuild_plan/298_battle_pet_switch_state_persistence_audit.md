# 298 - Battle Pet Switch State Persistence Audit

## Goal

When a player pet leaves the field and later returns, rebuild must keep the pet as a real source-shaped state object, not as a fresh battle mock.

This covers:

- current HP
- current skill PP
- active buff slots
- active debuff slots
- active effect queue rebuilt from those slots
- mutable stats reapplied from active buff/debuff state

## Source/Rebuild Rule

`SourcePetState` is the durable party record used by panel/petstate/save/load/battle entry. `BattleUnit` is the active battle runtime object.

Therefore the runtime rule is:

1. Before leaving the field, persist active `BattleUnit` into `SourcePetState`.
2. Reorder party so selected reserve pet becomes slot 0.
3. Rebuild active `SourceBattleUnit.playerFromSourcePets(...)`.
4. When a pet comes back, load HP/EXP/PP/buff/debuff slots from `SourcePetState`.
5. Rebuild `activeEffectQueue` and reapply stat effects after loading raw slots.

## Implementation Status

`VqsvSourceModels.SourcePetState`

- `PORTED/PARTIAL`: persists HP/EXP through source payload.
- `PORTED/PARTIAL`: persists skill PP through `skillCooldowns`.
- `PORTED/PARTIAL`: now persists `sourceBuffSlots`.
- `PORTED/PARTIAL`: persists `sourceDebuffSlots`.
- `PORTED/PARTIAL`: caught pet state copies buff/debuff slots if source battle unit exists.

`VqsvBattleUnit.fromSourcePet`

- `PORTED/PARTIAL`: loads HP/EXP/PP from source pet state.
- `PORTED/PARTIAL`: now loads buff/debuff slots.
- `PORTED/PARTIAL`: now calls `restoreSourceStatusState()`.

`restoreSourceStatusState()`

- rebuilds active buff queue from active buff slots.
- rebuilds active debuff queue from active debuff slots.
- restores mutable attack/defense/speed to base.
- reapplies active stat effects for known stat-affecting buff/debuff families.

`VqsvSaveRuntime`

- `PORTED/PARTIAL`: now writes/loads flattened `buffSlots` and `debuffSlots` for source pets and bank pets.
- `PENDING`: exact original persistent storage byte layout is not fully claimed.

## Smoke Checkpoint

Checkpoint:

`battle_p5_switch_preserves_hp_pp_buff_debuff_state`

Scenario:

1. Start Elder battle with two player pets.
2. Active pet A is forced to:
   - HP `55`
   - skill slot 0 PP `12`
   - buff7 active from source-style producer
   - debuff5 active from source-style producer
3. Open P5 and switch to pet B.
4. Assert pet A is now reserve slot 1 and still has:
   - payload HP `55`
   - PP `12`
   - active buff7 slot/value/duration
   - active debuff5 slot/value/duration
5. Return to command state, open P5 again, switch back to pet A.
6. Assert active runtime pet A restores:
   - HP `55`
   - PP `12`
   - buff7 active queue slot present
   - debuff5 active queue slot present
   - current speed matches restored stat effect result

Result:

`PASS`

PNG:

`rebuild_game/build_intro_demo/battle_p5_switch_preserves_hp_pp_buff_debuff_state.png`

## Remaining Honest Status

- `PORTED/PARTIAL`: runtime switch persistence is smoke-locked for HP/PP/buff/debuff/active queue/stat restore.
- `PORTED/PARTIAL`: save/load has slot serialization implemented, but exact original save binary parity remains unclaimed.
- `PENDING`: full original `game.d` storage vector parity for every exotic pet field.
- `PENDING`: broader multi-pet, multi-effect combinations beyond the focused P5 round-trip smoke.

## Next Recommended Step

Continue skill/debuff roadmap with debuff10 `Te Liet` only after running normal regression. If pet persistence bugs appear later, add a second checkpoint for save/load status-slot round trip before widening gameplay.
