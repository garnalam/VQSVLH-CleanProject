# 291 - Battle Debuff6 Nhut Chi Audit

Scope: source audit for target-side debuff id `6` / `Nhut Chi` before
dedicated closeout smoke.

## Plain Gameplay Explanation

`Nhut Chi` lowers the affected pet's outgoing damage.

It does not lower HP each turn and does not change attack/defense/speed stats
directly. Instead, when the debuffed pet later attacks, source subtracts a
percentage from the final damage value.

For current source producer skills `33` and `39`, the stored value is `10`, so
outgoing damage is reduced by `10%` while the debuff is active.

## Source Facts

```text
debuff6 = [317,328,3]
text317 = Nhut Chi
text328 = Damage ratio decreases for Y turns.

skill33 = [3,150,562,100,1,30,2,6,10,0]
skill39 = [3,156,568,150,3,15,2,6,10,0]

effect.mid[33] = [0,0,23,0,-1,-1,0]
effect.mid[39] = [0,0,23,0,-1,-1,0]

bufDebuf ar[1][6] = [1,12,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

## Source Chain

Producer:

- `game.b.b(target)` handles direct damage skills `33/39`.
- Both skills have `skill[6] = 2`, `skill[7] = 6`, `skill[8] = 10`.
- Apply case `6` stores:

```text
target.w[6][1] = aq.c[1][sourceSkill][8]
```

- Duration comes from `aq.c[7][6][2] = 3`.
- Source writes `w[6][3] = sourceSkill` and `w[6][4] = 1`.

Damage consumer:

```text
if (attacker.p(6)) {
    damage -= damage * attacker.w[6][1] / 100;
}
```

This means debuff6 belongs to the attacker when it attacks later.

Miss/source-side-effect detail:

- Source `game.d` calls `this.Z = this.h.b((b)this.h.p)` before the P7 hit/miss
  text branch.
- `game.b.b(target)` mutates debuff slots while computing `Z`.
- Therefore a dodge/miss hides HP/debuff text, but source-side debuff slot
  mutation can already have happened.
- Rebuild previously discarded pending debuff side effects on miss. That was
  wrong for source immediate debuff/clear-buff mutations and is fixed in the
  closeout.

P12/P13 active queue:

- `bufDebuf` has a visual row for debuff6: `[1,12,0,-1]`.
- But source helper only starts visual rows when the debuff id is listed in
  `game.d.ai[1]`.
- `game.d.ai[1]` excludes id `6`.
- Therefore P12/P13 should skip body visual and only run `game.b.q(6)` plus
  duration decrement/clear.
- `game.b.q(6)` is a no-op return.

## Required Closeout Checkpoints

```text
battle_status_debuff6_before_no_effect
battle_status_debuff6_skill33_producer_damage_down10
battle_status_debuff6_skill39_producer_damage_down10
battle_status_debuff6_miss_queue_no_text
battle_status_debuff6_p12_no_body_visual_noop
battle_status_debuff6_expiry_clears_icon
battle_status_debuff6_outgoing_damage_down
battle_status_debuff6_buff14_blocks_skill33
```

Expected proof:

| Checkpoint | Must Prove |
| --- | --- |
| before | no debuff6, no enemy status icon |
| skill33 | producer visual actor effect `23`, stores value `10`, duration `3`, icon `7/137` |
| skill39 | same producer behavior with higher skill power |
| miss queue | miss/dodge hides debuff text, but source immediate debuff slot is still committed and queued |
| P12 no visual | no `active queue visual start`, no actor/speffect, HP unchanged, duration `3->2` |
| expiry | duration `3->2->1->0`, HP unchanged, icon cleared |
| outgoing damage | attacker with debuff6 value `10` deals lower damage; sample smoke locks `80 -> 72` |
| buff14 block | damage can hit, debuff6 is not applied |

## Classification Before Smoke

```text
Debuff6 producer skills 33/39: PORTED/PARTIAL until dedicated smoke passes
Stored outgoing-damage-down value: PORTED/PARTIAL until dedicated smoke passes
Damage formula consumer: PORTED/PARTIAL until dedicated smoke passes
P12/P13 no-body-visual skip: NEEDS DEDICATED SMOKE
Miss source-immediate side effect: NEEDS PATCH/SMOKE
Expiry clear/icon: NEEDS DEDICATED SMOKE
Buff14 block: NEEDS DEDICATED SMOKE
Pixel-perfect original comparison: PENDING
```

