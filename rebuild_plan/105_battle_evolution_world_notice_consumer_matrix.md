# 105 Battle Evolution World Notice Consumer Matrix

Status: SLICE 2 IMPLEMENTED AS `PORTED/PARTIAL`.

This slice consumes the evolution/mutation notice queue produced by the
source-shaped `game.b.J()` rebuild equivalent. Later Slice 3 work is tracked in
`107_evolution_msgwarm_tutorial_evolve_slice_matrix.md`.

## Source Facts

Source: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`.

Audited consumer shape:

```text
if (P == 0 && !M.h() && game.k.I == 0 && game.k.H != null && H.size() > 0) {
    if (ac >= H.size()) {
        H.removeAllElements();
        ac = 0;
        I = 1;
    } else if (S.ax()) {
        int[] notice = (int[]) H.elementAt(ac);
        action = "Tiến hóa";
        if (aq.c[0][aq.a((byte)0, (short)notice[0], (byte)19)][2] == 3) {
            action = "Dị hoá";
        }
        if (!K && L[0] != -1 && ac == H.size() - 1) {
            S.E();
            S.a("Nhấn #2" + f(notice[1]) + "#0 đạt tới có thể" + action + " điều kiện",
                "Nhấn nút 5 để tiếp tục");
        } else {
            S.b("#2" + f(notice[1]) + "#0 có thể" + action);
        }
        ++ac;
    }
}
```

## Rebuild Matrix

| Source | Rebuild | Status |
| --- | --- | --- |
| `game.k.H` | `Scene.sourceEvolutionQueue` | `PORTED/PARTIAL`; producer already fills it in Slice 1. |
| `game.k.L` | `Scene.sourceEvolutionL` | `PORTED/PARTIAL`; producer fills `[level, species]`. |
| `game.k.I == 0` | `Scene.sourceEvolutionI == 0` | `PORTED`; consumer only runs while pending. |
| `game.k.ac` | `Scene.sourceEvolutionNoticeIndex` | `PORTED`; increments after notice starts, clears after queue exhausted. |
| `P == 0 && !M.h()` | rebuild idle gate: no current blocking text/choice/save/world petstate/battle overlay and no remaining auto event | `PORTED/PARTIAL`; source state labels are mapped conservatively to avoid stealing control from manual script events. |
| `S.ax()` | `text == null` and no active blocking | `PORTED/PARTIAL`; exact `game.h.ax()` widget readiness is not a generic UI runtime yet. |
| mutation wording | `notice.targetKind == 3 ? "Dị hoá" : "Tiến hóa"` | `PORTED`. |
| pet name | species row name via `VqsvBattleTables` | `PORTED/PARTIAL`; equivalent to source text id lookup for audited species. |
| `S.b(...)` | `TextBox.openBox(...)` simple notice | `PORTED/PARTIAL`; source-shaped message, not full `game.h` widget runtime. Long source-ui notices now use one-line marquee instead of wrapping. |
| `S.E(); S.a(text,prompt)` | detailed notice displayed through `TextBox.msgWarm(...)` using `/data/ui/msgwarm.ui` decoded coordinates and one-line marquee | `PORTED/PARTIAL`; source-shaped renderer, not generic widget runtime. |
| queue exhausted | clear queue, reset index, set `I = 1` | `PORTED`. |

## Smoke

Focused checkpoint:

```text
world_evolution_notice_after_levelup
world_evolution_notice_queue_exhausted
```

Fixture:

- active pet species `6`, level `11`.
- EXP set just below level `12`.
- source-shaped battle victory triggers level-up.
- Slice 1 producer creates notice `species=6 -> target=7`.
- Slice 2 consumer shows the world notice and leaves `evolve.ui` untouched.

Expected trace:

```text
PORTED/PARTIAL battle P22 game.b.J evolution queue species=6 target=7 ...
PORTED/PARTIAL game.k evolution notice consume ac=0 species=6 target=7 ...
SMOKE verified game.k.H/L/I Slice2 world notice consumer ...
```

`world_evolution_notice_queue_exhausted` additionally confirms that after
notice confirmation the rebuild clears the queue, resets `ac` to `0`, and sets
`game.k.I` equivalent to `1`.

## Follow-up Status

- `msgwarm.ui`, tutorial bridge `K/L/key32/U=4`, `evolve.ui`, confirm
  validation, material consume, and pet payload mutation are now implemented as
  source-shaped Slice 3 work in `107_evolution_msgwarm_tutorial_evolve_slice_matrix.md`.
- Save/resume persistence for a pending evolution queue remains `PENDING`.
