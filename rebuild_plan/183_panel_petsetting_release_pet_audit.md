# 183 - Panel petsetting c=3 release pet audit

Date: 2026-07-10

## Target

Audit source path for:

- `petsetting c=3`
- Vietnamese UI action: `Phong sinh`
- source release/pet removal flow

This is an audit only. No code mutation is included here.

## Context

Already covered before this audit:

- `petsetting c=0`: item `choice.ui` render + item-use warning/success loop
- `petsetting c=2`: equipment `choice.ui` render + equip/unequip/transfer loop
- `petsetting c=4`: skill `skill.ui` render/navigate/back

The next unported petsetting branch is `c=3`, release pet.

## Primary Source Files

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/ui/decoded/data__ui__msgconfirm.ui.json`

## Source UI Entry

In `game.h.X()`, source state uses:

- `f`: panel substate
- `b`: selected pet index
- `c`: selected petsetting row
- `o`: owner/runtime; world panel owner is `game.k`

When already inside petsetting (`f == 1`) and confirm on row `c == 3`, source does:

```java
case 3: {
    if (aq.a((byte)0, (short)this.q.z[this.b].q(), (byte)22) == 2) {
        this.f = 3;
        this.E();
        this.p.a("/data/ui/petsetting.ui");
        this.a("Than thu khong the phong sinh", "Nhan nut 5 de tiep tuc");
        break;
    }
    this.f = 2;
    this.p.a("/data/ui/msgconfirm.ui", 257, this);
    this.p.a("/data/ui/petsetting.ui");
    this.b("Ban muon phong sinh sung vat nay?", "Xac nhan");
    break;
}
```

Meaning:

- If selected pet is a protected/mythic pet:
  - set `f = 3`
  - open `/data/ui/msgwarm.ui`
  - close `/data/ui/petsetting.ui`
  - show warning: `Than thu khong the phong sinh`
- Else:
  - set `f = 2`
  - open `/data/ui/msgconfirm.ui`
  - close `/data/ui/petsetting.ui`
  - show confirmation: `Ban muon phong sinh sung vat nay?`
  - confirm button text: `Xac nhan`

## Protected Pet Check

Source check:

```java
aq.a((byte)0, (short)this.q.z[this.b].q(), (byte)22) == 2
```

Source `aq.a(byte group, short row, byte col)` simply returns:

```java
c[group][row][col]
```

So this is:

- `aq.c[0][speciesId][22] == 2`

Status:

- `PORTED/PENDING`: rebuild can read this through `VqsvBattleTables.row(0, speciesId)[22]`, but release flow is not implemented yet.

## msgconfirm.ui Layout

Decoded source UI:

- `/data/ui/msgconfirm.ui`
- widget `1`: frame image `img_124`, at `x=50 y=137`
- widget `4`: message text area, also at `x=50 y=137`
- widget `2`: left softkey, default text `Xac dinh`, at bottom-left
- widget `3`: right softkey, default text `Quay lai`, at bottom-right

In source, `this.b(message, action)` sets the message/action text for this confirm UI.

Current rebuild status:

- `PENDING`: no dedicated source-backed petsetting `msgconfirm.ui` runtime for release pet yet.
- Existing panel `option.ui` confirmation is separate and should not be reused blindly.

## Confirm / Cancel Loop

After `c == 3` opens confirmation, later in `game.h.X()` source handles:

```java
case 3: {
    if (this.o.k(131072) && this.f == 2 || this.o.k(131104) && this.f == 3) {
        if (this.f == 2) {
            if (this.q.o(this.b)) {
                this.q.l(this.q.z[this.b].c[5]);
                this.q.z[this.b].c[5] = -1;
                this.q.m(this.b);
                if (this.b >= this.q.A) {
                    --this.b;
                }
                ((k)this.o).M.i();
                this.e(this.b);
                this.p.a("/data/ui/msgconfirm.ui");
                this.f = 0;
                break;
            }
            this.f = 3;
            this.p.a("/data/ui/msgwarm.ui", 257, this);
            this.a("Ba lo phai luu it nhat 1 sung vat", "Nhan nut 5 de tiep tuc");
            this.p.a("/data/ui/msgconfirm.ui");
            break;
        }
        this.f = 0;
        this.p.a("/data/ui/msgwarm.ui");
        break;
    }
    if (this.o.k(786432) && this.f <= 2) {
        this.f = 0;
        this.p.a("/data/ui/msgconfirm.ui");
        break;
    }
    break;
}
```

Interpretation for world owner `game.k`:

- `f == 2`: confirm dialog is open.
- Confirm key `131072`:
  - If another living pet exists, release succeeds.
  - If no other living pet exists, warning opens and `f = 3`.
- `f == 3`: warning is open.
  - Confirm key `131104` closes warning and returns `f = 0`.
- Cancel/back key `786432` while `f <= 2`:
  - closes confirm
  - returns `f = 0`

Important nuance:

- There is no success `msgwarm.ui` after release. On success it directly closes `msgconfirm.ui`, refreshes petstate, and returns to `f = 0`.
- The selected petsetting UI is closed when opening `msgconfirm.ui`; after successful release, source refreshes `petstate.ui`, not `petsetting.ui`.

## Source Mutation

Release success source mutation:

```java
this.q.l(this.q.z[this.b].c[5]);
this.q.z[this.b].c[5] = -1;
this.q.m(this.b);
if (this.b >= this.q.A) {
    --this.b;
}
((k)this.o).M.i();
this.e(this.b);
this.p.a("/data/ui/msgconfirm.ui");
this.f = 0;
```

Meaning:

1. If pet has equipment in `c[5]`, mark that equipment as not worn:
   - `game.g.l(itemId)` sets matching `q.L` row `[1] = 0`.
2. Clear selected pet equipment slot:
   - `q.z[b].c[5] = -1`
3. Remove selected pet from party:
   - `game.g.m(b)`
4. Clamp selected index if it is now past end.
5. Refresh panel model:
   - `((k)o).M.i()`
   - `e(b)`
6. Close confirm and return idle.

Source `game.g.m(index)`:

```java
this.z[n2] = null;
while (n2 < this.A - 1) {
    this.z[n2] = this.z[n2 + 1];
    this.z[n2 + 1] = null;
    ++n2;
}
--this.A;
```

So it removes from party and shifts remaining pets left.

Source `game.g.o(index)`:

```java
int aliveOtherCount = 0;
for (int i = 0; i < this.A; ++i) {
    if (i == index || !this.z[i].S()) continue;
    ++aliveOtherCount;
}
return aliveOtherCount > 0;
```

So release is allowed only if at least one other party pet is alive.

## Bank Behavior Distinction

There is another release-like path in `game.h.X()` for `o.Q == 16`:

- It checks `q.z()` bank capacity.
- It checks `q.o(b)` alive-other condition.
- It moves pet payload to bank with `q.b(q.z[b].P())`.
- Then removes party pet with `q.m(b)`.

This is **not** the normal `petsetting c=3` row under `o.Q == 6 || o.Q == 0`.

For this target slice:

- Do not move released pet to bank.
- Do not call bank-add semantics.
- Just remove from party and clear equipment.

## Rebuild Mapping

Existing rebuild structures:

- party pets: `Scene.sourcePets`
- bank pets: `Scene.sourcePetBank`
- equipment list: `Scene.sourceEquipmentItems`
- selected pet index: `Scene.battleMenuIndex`
- selected equipment slot: `SourcePetState.sourcePayload[2]`

Recommended rebuild mapping:

- protected pet check:
  - `VqsvBattleTables.instance().row(0, speciesId)[22] == 2`
- alive-other check:
  - iterate `sourcePets`, skip selected index, require payload HP > 0
- release success:
  - if selected pet equipment id >= 0, set matching `SourceEquipmentItem.equippedFlag = false`
  - set selected pet `sourcePayload[2] = -1`
  - remove selected pet from `sourcePets`
  - clamp `battleMenuIndex`
  - call/open `openWorldPetstate()` to refresh rows
  - close release confirm
- warning if no other living pet:
  - show `msgwarm.ui` text `Ba lo phai luu it nhat 1 sung vat`
  - after confirm close warning, return to `petstate.ui`

## UI Slice Proposal

Recommended implementation order:

1. Add source-backed release confirm state:
   - `sourceReleaseConfirmVisible`
   - `sourceReleaseWarningMode`
   - selected pet index snapshot if needed
2. Render `/data/ui/msgconfirm.ui`:
   - frame widget `1`
   - message widget `4`
   - softkey widget `2 = Xac nhan`
   - softkey widget `3 = Quay lai`
3. Open flow:
   - from petsetting row `c=3`
   - protected pet warning path first
   - normal confirm path second
4. Confirm flow:
   - cancel/back closes confirm, returns to `petstate.ui`
   - confirm with no other living pet opens warning
   - confirm with other living pet removes selected party pet, clears equipment, refreshes `petstate.ui`
   - warning confirm closes warning and returns to `petstate.ui`

## Smoke Plan

Focused PNG smoke:

- `panel_petstate_petsetting_release_confirm_open`
- `panel_petstate_petsetting_release_cancel_returns_petstate`
- `panel_petstate_petsetting_release_success_removes_pet`
- `panel_petstate_petsetting_release_success_clears_equipment`
- `panel_petstate_petsetting_release_last_alive_warning`
- `panel_petstate_petsetting_release_warning_returns_petstate`
- optional if a source species row is known/proven:
  - `panel_petstate_petsetting_release_protected_warning`

Regression PNG smoke:

- item choice warning/success
- equipment equip/unequip/transfer
- skill open/back
- bag open
- save success
- gamesystem option confirm-no
- route Sophie/Bunny/Elder

## Status

- `PORTED/PENDING`: release pet source path audited.
- `PENDING`: `msgconfirm.ui` render/runtime in rebuild.
- `PENDING`: release mutation in `sourcePets`.
- `PENDING`: protected pet species smoke, unless a concrete `aq.c[0][species][22] == 2` row is proven.
- `PENDING`: source save/load effect after release.
- `PENDING`: original-vs-rebuild pixel compare.

## Next

Recommended next slice:

Implement `petsetting c=3` open/render/cancel first:

- open `msgconfirm.ui`
- cancel/back returns to `petstate.ui`
- protected/no-mutating paths can be logged/blocked until smoke proves UI

Then implement release success + last-alive warning in the following slice.
