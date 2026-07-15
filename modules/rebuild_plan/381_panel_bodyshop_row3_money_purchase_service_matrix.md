# 381 - Panel bodyShop.ui Row 3 Money Purchase Service Matrix

Scope: `bodyShop.ui` row 3, label `Mua sắm kim tiền`. PC rebuild policy treats SMS/payment as free, while preserving source product id and reward side effect.

## Source Flow

| Source | UI/state | Behavior | Rebuild status |
| --- | --- | --- | --- |
| `game.k.aC()` | `/data/ui/bodyShop.ui` | Opens portable shop hub with `c=0`, `f=0`, then calls `bA()`. | PORTED |
| `game.k.bA()` row `c=3` | widget `11` | Description uses `a.a.c(601) + a.a.a(604, [2,1,2])`; for premium rows calls `o.c(0)` and `bB()`. | PORTED/PARTIAL |
| `game.k.bB()` row `c=3` | payment product | Maps `c=3` to `o.b((byte)2)`, product id 2. | PORTED |
| `game.k.aD()` product 2 | `smsInfo.ui` / SMS state | Enters payment confirm when description is ready. PC rebuild uses free source-shaped `smsInfo.ui` confirm. | PORTED/PARTIAL |
| `an.b(true)` product `case 2` | reward commit | Calls `game.g.o().s(10000)`, adding 10000 money. | PORTED |

## Product 2 Reward

Source:

```java
case 2: {
    game.g.o().s(10000);
    break;
}
```

Rebuild mapping:

| Source concept | Rebuild field/helper |
| --- | --- |
| `game.g.o().s(10000)` | `Scene.sourceMoney += 10000` |
| SMS/payment | Free confirm, no network/SMS send |
| Success dialog | `msgwarm.ui` source-shaped confirmation |

## Smoke Matrix

| Smoke PNG/checkpoint | Assertion |
| --- | --- |
| `panel_bodyshop_row3_open_description.png` | Hub selects row 3 and row description is visible. |
| `panel_bodyshop_row3_smsinfo_confirm.png` | Row 3 opens source-shaped free `smsInfo.ui` confirm. |
| `panel_bodyshop_row3_success_money_plus10000.png` | Confirm adds exactly 10000 money and returns to `bodyShop.ui` with success warning. |
| `panel_bodyshop_row3_smsinfo_back.png` | Back from confirm returns to `bodyShop.ui` without changing money. |

## Remaining Debt

- Full generic `smsInfo.ui`/`smsTip.ui` state machine is not claimed.
- Exact `a.a.c()/a.a.a()` text runtime remains broader UI engine work; row description is source-shaped.
