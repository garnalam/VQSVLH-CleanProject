# 285 - Battle Debuff3 Thuc Loai Audit

Scope: source audit for target-side debuff id `3` / `Thuc Loai` before any
new closeout code.

## Plain Gameplay Explanation

`Thuc Loai` is a delayed-damage status.

In normal play:

- the hit that applies it still deals normal skill damage immediately;
- the status icon remains on the target for 3 turns;
- the first active queue tick only reduces duration from `3 -> 2`;
- the second active queue tick only reduces duration from `2 -> 1`;
- the last tick, when duration is `1`, deals delayed HP damage and then clears;
- if that delayed damage kills the target, the normal battle KO/result path must run.

This is different from `Quan Quanh`/debuff2. `Quan Quanh` is a bind/command
lock flag with no HP tick. `Thuc Loai` is a stored-damage bomb that only fires
near expiry.

## Source Facts

Primary source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`
- `modules/script/decoded/data__script__chs.mid.json`

Raw source rows:

```text
debuff3 = [314,325,3]

skill13 = [1,130,542,50,1,30,2,3,150,0]
skill19 = [1,136,548,150,3,15,2,3,200,0]

effect.mid[13] = [0,0,21,0,-1,-1,0]
effect.mid[19] = [0,0,21,0,-1,-1,0]

bufDebuf ar[1][3] -> debuff visual row [0,21,0,-1]
```

Source text:

```text
314 = Thuc Loai
325 = Sau Y hiep da bi X da thuong, neu tu vong thi hieu qua Thuc Loai bien mat.
542 = low damage, target enters Thuc Loai, after 2 turns it receives relatively high damage.
548 = relatively high damage, target enters Thuc Loai, after 2 turns it receives high damage.
```

## Producer Chain

`game.b.b(target)` handles skills `13` and `19` in the normal direct-damage
family.

Source behavior:

- skill power uses `skill[3]`;
- target debuff id comes from `skill[7] = 3`;
- implicit chance is `-1`, so ordinary targets receive the debuff unless
  buff14/status3 gates block it;
- apply stores `w[3][1] = preSkillRawDamage`;
- apply stores source skill id in `w[3][3]`;
- duration comes from `aq.c[7][3][2] = 3`;
- queue bank `1`, id `3` is inserted for P12/P13 active processing.

Rebuild status:

- `BattlePendingDebuff.commit()` stores `preSkillRaw` for effect ids `0` and
  `3`, sets duration/source skill/active flag, and queues bank `1`.
- Phase 9-G smoke already covers skill13/19 success, buff14 block, status3
  block, and miss-while-queue behavior.

Classification: `PORTED/PARTIAL` until dedicated closeout smoke is tightened.

## Active Queue Visual

Source P12/P13 builds active queue in bank order:

1. buff bank `0`;
2. debuff bank `1`;
3. slot order `0..2`.

For debuff3:

```text
bufDebuf row = [0,21,0,-1]
```

That means the active body visual is a type `0` actor/source effect using
effect id `21`, state `0`, no follow-up speffect segment. It is not like
debuff2, which advances into `speffect 6` / AH type `8`.

Rebuild status:

- `activeQueueNeedsVisual()` includes debuff id `3`.
- `prepareActiveQueueSegment()` creates `P7ActorAnimation` for row type `0`.
- Existing checkpoints cover queue start, actor cursor mid-frame, and after
  apply.

Gap: no dedicated closeout PNG set named for `Thuc Loai` yet. Existing
checkpoints are older P12 generic names.

Classification: `PORTED/PARTIAL`; pixel-perfect original compare is not
claimed.

## Delayed Damage Formula

Source `game.b.q(3)`:

```text
if (w[3][0] > 1) break;
damage = w[3][1] * aq.c[1][w[3][3]][8] / 100;
if target dies, actor state becomes 3.
```

Then shared P12/P13 logic decrements duration and clears the effect when it
reaches zero.

Exact numbers:

- skill13 delayed percent: `150%`;
- skill19 delayed percent: `200%`;
- if stored raw is `20`, skill13 final delayed damage is `30`;
- if stored raw is `20`, skill19 final delayed damage is `40`;
- first tick at duration `3` should not damage;
- second tick at duration `2` should not damage;
- third tick at duration `1` should damage, clear icon, and may route to KO.

Rebuild status:

- `BattleUnit.tickSourceDebuff(3)` checks `duration <= 1`, computes
  `storedRaw * skill[8] / 100`, applies HP damage, then decrements duration.
- `finishActiveQueue()` routes dead units through `handleDeadBattleUnit(...)`.

Gap: existing smoke says `battle_p12_debuff3_after_apply`, but does not
separately prove:

- `3 -> 2` no damage;
- `2 -> 1` no damage;
- `1 -> 0` exact damage and icon clear;
- delayed-damage KO transition.

Classification: logic appears `PORTED`, closeout smoke is `MISSING`.

## HUD Icon

Source HUD icon rule:

```text
debuff icon cell = debuffId + 1
duration cell = 134 + duration
```

For debuff3:

- icon cell `4`;
- duration `3` cell `137`;
- duration `2` cell `136`;
- duration `1` cell `135`;
- duration `0` clears icon.

Rebuild status:

- `syncStatusSlots()` uses the same `debuffId + 1` and `134 + duration`
  mapping.

Classification: `PORTED`, but should be locked by dedicated closeout PNG.

## Current Coverage

Existing broad/older checkpoints:

```text
battle_phase9g_debuff3_success_skill_13
battle_phase9g_debuff3_success_skill_19
battle_phase9g_debuff3_buff14_block_skill_13
battle_phase9g_debuff3_status3_block_skill_13
battle_phase9g_debuff3_miss_queue_skill_13
battle_p12_debuff3_queue_start
battle_p12_debuff3_type0_actor_mid
battle_p12_debuff3_after_apply
```

These prove the old Phase 9-G family and generic P12 consumer path, but they
do not yet match the newer closeout standard used by debuff0/1/2.

## Required Next Slice

Create dedicated debuff3 closeout smoke/checkpoints:

```text
battle_status_debuff3_before_no_effect
battle_status_debuff3_skill13_producer_apply
battle_status_debuff3_skill19_producer_apply
battle_status_debuff3_body_visual_actor21
battle_status_debuff3_tick1_no_damage_duration2
battle_status_debuff3_tick2_no_damage_duration1
battle_status_debuff3_final_tick_damage_skill13
battle_status_debuff3_final_tick_damage_skill19
battle_status_debuff3_final_tick_ko_transition
battle_status_debuff3_buff14_blocks_skill13
```

Expected numeric smoke:

| Check | Before | After |
| --- | --- | --- |
| producer skill13 | no debuff3 | icon `4/137`, source skill `13`, stored raw from damage pre-skill |
| producer skill19 | no debuff3 | icon `4/137`, source skill `19`, stored raw from damage pre-skill |
| tick1 | HP `80`, duration `3` | HP `80`, duration `2`, icon `4/136` |
| tick2 | HP `80`, duration `2` | HP `80`, duration `1`, icon `4/135` |
| final skill13 | HP `80`, stored raw `20`, duration `1` | HP `50`, duration `0`, icon clear |
| final skill19 | HP `80`, stored raw `20`, duration `1` | HP `40`, duration `0`, icon clear |
| final KO | HP below delayed damage | KO/result path through P8/P15/P5/P9 as side-appropriate |

## Audit Classification

```text
Source producer skills 13/19: PROVED
Source delayed damage formula: PROVED
Source body visual row: PROVED
Rebuild producer path: PORTED/PARTIAL
Rebuild P12/P13 visual path: PORTED/PARTIAL
Rebuild delayed damage formula: PORTED
Dedicated closeout smoke: PENDING
Delayed-damage KO transition smoke: PENDING
Pixel-perfect visual parity: PENDING
```

## Next Step

Implement only the dedicated closeout smoke/assert layer first. If any
checkpoint fails, patch the smallest runtime gap proven by that failure.
After that, run build/check/formula/mojibake plus focused PNG smoke and
`battle_quick`.
