# 379 - Panel bodyShop.ui Row 1 Pet Level-Up Service Matrix

Scope: `bodyShop.ui` row 1, label `Thăng cấp sủng vật`. This is the premium/SMS product 3 service. PC rebuild policy treats SMS/payment as free, but the UI flow and product reward must stay source-shaped.

## Source Flow

| Source | UI/state | Behavior | Rebuild status |
| --- | --- | --- | --- |
| `game.k.aC()` | `/data/ui/bodyShop.ui` | Opens portable shop hub with `c=0`, `f=0`, then calls `bA()`. | PORTED |
| `game.k.bA()` row `c=1` | widget `11` | Description uses `a.a.c(602) + a.a.a(604, [2,1,2])`; also calls `o.c(0)` and `bB()` for premium rows. | PORTED/PARTIAL: source-shaped text; exact generic `a.a` text runtime still pending. |
| `game.k.bB()` row `c=1` | payment product | Maps `c=1` to `o.b((byte)3)`, product id 3. | PORTED |
| `game.k.aD()` product 3 | warning gate | If every pet in bag has level `>= 50`, opens `msgwarm.ui`: `Trong ba lô sủng vật đều đã max level` / `Nhấn nút 5 tiếp tục`. | PORTED |
| `game.k.aD()` product 3 | `smsInfo.ui` / SMS state | If at least one pet is not max level, enters payment confirm. PC rebuild uses free source-shaped `smsInfo.ui` confirm and does not send SMS. | PORTED/PARTIAL |
| `an.b(true)` product `case 3` | reward commit | Clears `game.k.F/E`, loops all bag pets, caps level to 50 with `+5`, refreshes pet, then queues evolution candidates. Sets `game.k.G=1` if queue exists, else `G=2`. | PORTED/PARTIAL |

## Product 3 Reward

Source logic:

```java
for each pet in bag:
    if level == 50:
        pet.J();
        continue;
    pet.x();
    if level + 5 >= 50:
        pet.h(50 - level);
    else:
        pet.h(5);
    pet.I();
    if evolution condition passes:
        game.k.E.addElement(pet);
        game.k.F.addElement("" + index);
game.k.G = game.k.E.size() <= 0 ? 2 : 1;
```

Rebuild mapping:

| Source concept | Rebuild field/helper |
| --- | --- |
| Bag pets `game.g.o().z[]` | `Scene.sourcePets` |
| Pet level | `SourcePetState.level` |
| Refresh `I()/J()` | `SourcePetState.refreshFromSourceDb()` |
| Evolution candidate `game.k.E/F` | `Scene.sourceEvolutionQueue` plus trace index |
| `game.k.L` detailed notice payload | `Scene.sourceEvolutionL = [level, species]` for first eligible pet |
| `game.k.I` queue state | `Scene.sourceEvolutionI` |

## Smoke Matrix

| Smoke PNG/checkpoint | Assertion |
| --- | --- |
| `panel_bodyshop_row1_open_description.png` | Hub selects row 1 and description is the level-up service, with `bodyShop.ui` visible. |
| `panel_bodyshop_row1_smsinfo_confirm.png` | Row 1 opens source-shaped free `smsInfo.ui` confirm before reward commit. |
| `panel_bodyshop_row1_all_max_warning.png` | All party pets level 50 opens `msgwarm.ui`, no mutation. |
| `panel_bodyshop_row1_success_level_plus5.png` | Level 7 pet becomes level 12 after free confirm. |
| `panel_bodyshop_row1_success_cap50.png` | Level 49 pet becomes level 50, not 54. |
| `panel_bodyshop_row1_evolution_queue.png` | Species 6 at level 11 becomes level 16 and queues evolution notice target 7. |

## Remaining Debt

- Full `smsInfo.ui`/`smsTip.ui` state machine is not claimed. PC rebuild intentionally bypasses real SMS/network.
- Exact generic Java ME `a.a.c()/a.a.a()` text runtime is still broader UI engine work.
- Evolution notice consumer already exists, but pixel-perfect `evolve.ui` and tutorial bridge remain separate roadmap items.
