# 380 - Panel bodyShop.ui Row 2 Badge Purchase Service Matrix

Scope: `bodyShop.ui` row 2, label `Mua sắm huy hiệu`. PC rebuild policy treats SMS/payment as free, but source product id and reward side effect must be preserved.

## Source Flow

| Source | UI/state | Behavior | Rebuild status |
| --- | --- | --- | --- |
| `game.k.aC()` | `/data/ui/bodyShop.ui` | Opens portable shop hub with `c=0`, `f=0`, then calls `bA()`. | PORTED |
| `game.k.bA()` row `c=2` | widget `11` | Description uses `a.a.c(603) + a.a.a(604, [2,1,2])`; for premium rows calls `o.c(0)` and `bB()`. | PORTED/PARTIAL |
| `game.k.bB()` row `c=2` | payment product | Maps `c=2` to `o.b((byte)4)`, product id 4. | PORTED |
| `game.k.aD()` product 4 | `smsInfo.ui` / SMS state | No all-max gate; enters payment confirm when description is ready. PC rebuild uses free source-shaped `smsInfo.ui` confirm. | PORTED/PARTIAL |
| `an.b(true)` product `case 4` | reward commit | Calls `game.g.o().u(10)`, adding 10 badges/medals. | PORTED |

## Product 4 Reward

Source:

```java
case 4: {
    game.g.o().u(10);
}
```

Rebuild mapping:

| Source concept | Rebuild field/helper |
| --- | --- |
| `game.g.o().u(10)` | `Scene.sourceBadges += 10` |
| SMS/payment | Free confirm, no network/SMS send |
| Success dialog | `msgwarm.ui` source-shaped confirmation |

## Smoke Matrix

| Smoke PNG/checkpoint | Assertion |
| --- | --- |
| `panel_bodyshop_row2_open_description.png` | Hub selects row 2 and row description is visible. |
| `panel_bodyshop_row2_smsinfo_confirm.png` | Row 2 opens source-shaped free `smsInfo.ui` confirm. |
| `panel_bodyshop_row2_success_badges_plus10.png` | Confirm adds exactly 10 badges and returns to `bodyShop.ui` with success warning. |
| `panel_bodyshop_row2_smsinfo_back.png` | Back from confirm returns to `bodyShop.ui` without changing badge count. |

## Remaining Debt

- Full generic `smsInfo.ui`/`smsTip.ui` state machine is not claimed.
- Exact `a.a.c()/a.a.a()` text runtime remains broader UI engine work; row description is source-shaped.
