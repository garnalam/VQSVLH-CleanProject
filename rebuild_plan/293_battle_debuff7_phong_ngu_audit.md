# 293 - Battle Debuff7 Phong Ngu Source Audit

Scope: dedicated re-tightening for target-side debuff id `7` / `Phong Ngu`.

## Plain Gameplay Explanation

This status is named like "defense", but it is a defense-down debuff.

When it lands, the target's current defense is reduced. The next attacks against
that target should deal more damage while the debuff remains active. It does not
deal HP damage per turn and it does not create a body-attached visual during the
P12/P13 active-status queue.

## Source Rows

```text
debuff7 = [318,329,3]
skill51 = [5,168,580,80,0,45,2,7,20,0]
skill57 = [5,174,586,120,2,30,2,7,20,0]
effect51 = [0,0,25,0,-1,-1,0, 0,1,8,0,-1,-1,0, 0,1,11,0,-1,-1,0]
effect57 = [0,0,25,1,-1,-1,0, 0,1,14,0,-1,-1,0, 0,1,11,0,-1,-1,0]
bufDebuf ar[1][7] = [1,19,0,-1,1,6,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

## Source Chain

Producer skills `51` and `57` are direct-damage skills with target debuff id
`7`. Both use source parameter `skill[8] = 20`.

In `game.b.b(target)`, the source apply switch handles debuff id `7` as:

```text
w[7][1] = target.baseDefense * skill[8] / 100
target.currentDefense = target.baseDefense - w[7][1]
duration = aq.c[7][7][2]
```

So with target base defense `100`, the stored value is `20` and current defense
becomes `80`.

`game.b.q(7)` reasserts the same formula during the active-status queue:

```text
currentDefense = baseDefense - w[7][1]
```

The active visual gate in `game.d` only includes target debuffs
`[0,1,2,3,8,9,10]`. Debuff7 is not in that list, so the bufDebuf visual row
exists in data but is skipped by the runtime gate.

## Important Behavior

- Hit path: HP damage and debuff text are visible; debuff7 applies.
- Miss path: source-immediate debuff mutation still commits, but HP damage and
  debuff text remain hidden by the later P7 hit/text gate.
- Active tick: no body visual; defense is reasserted; duration decreases.
- Expiry: defense is restored to base value and the status icon clears.
- Buff14 blocks the incoming debuff, leaving defense unchanged.

## Classification

```text
Producer skills 51/57: PORTED
Stored formula baseDefense * skill[8] / 100: PORTED
Defense apply/reassert/restore: PORTED
P7 miss source-immediate mutation: PORTED
P12/P13 no-body-visual skip via game.d.ai[1]: PORTED
Buff14 block: PORTED
Pixel-perfect original comparison: PENDING
```

## Next Step

Run the closeout smoke set and document numeric before/during/after results in
`294_battle_debuff7_phong_ngu_closeout.md`.
