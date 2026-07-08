final class VqsvBattleScripts {
    private VqsvBattleScripts() {
    }

    static Blocking room1BunnyBattleCaptureRuntime(VqsvIntroDemo.Scene s) {
        VqsvSourceStoryState.ensureInitialDienMieu(s, "room1 group0 Bunny battle entry");
        s.sourceStateTrace.add("PORTED/APPROX room1 group0 op37 battleSetup=[[34,5,1]]");
        s.sourceStateTrace.add("PORTED/APPROX room1 group0 op52 this.i=true game.c.j=false args=[0,1]");
        s.sourceStateTrace.add("PORTED/APPROX room1 group0 op66 an.U=0");
        s.sourceStateTrace.add("PORTED/PARTIAL room1 group0 op32 battleEntry mode=[0,0]"
                + " source game.i state12 b.a().c(-2013265920,6) before game.d state13");
        s.sourceStateTrace.add("PORTED/APPROX room1 group0 op47 branch=[12,0,0] result=-1 continue success path; full game.d command UI pending");
        return new BattleEntryTransitionThenRuntime(new SourceBattleRuntime(
                50,
                new int[]{34, 5, 1},
                new int[]{0, 1},
                new int[]{0, 0},
                new int[]{12, 0, 0},
                -1), 6, 0);
    }

    static Blocking room0Group6ElderBattleRuntime(VqsvIntroDemo.Scene s) {
        s.sourceStateTrace.add("PORTED/APPROX room0 group6 op37 battleSetup species=68 level=5 nature=1 from game.d.a(int[][])");
        s.sourceStateTrace.add("PORTED/APPROX room0 group6 op32 battleEntry mode=[0,2] captures world screen then state=12 in source");
        s.sourceStateTrace.add("PORTED/APPROX room0 group6 op47 branch=[10,10,0] result=0 continue reward path; full game.d command UI pending");
        return new SourceBattleRuntime(
                52,
                new int[]{68, 5, 1},
                new int[0],
                new int[]{0, 2},
                new int[]{10, 10, 0},
                0,
                true);
    }
}

final class BattleEntryTransitionThenRuntime implements Blocking {
    private final Blocking runtime;
    private final int sourceEffectId;
    private final int sourceMode;
    private boolean started;
    private boolean runtimeStarted;

    BattleEntryTransitionThenRuntime(Blocking runtime, int sourceEffectId, int sourceMode) {
        this.runtime = runtime;
        this.sourceEffectId = sourceEffectId;
        this.sourceMode = sourceMode;
    }

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            if (sourceEffectId == 6) {
                s.effect.startBattleEntryTransition(sourceMode);
            } else {
                s.effect.startFlash(14, 0);
            }
            s.sourceStateTrace.add("PORTED/PARTIAL battle op32 pre-entry transition"
                    + " sourceEffect=" + sourceEffectId
                    + " mode=" + sourceMode
                    + " exact root b.java pixel parity pending");
            return false;
        }
        if (!runtimeStarted) {
            if (!s.effect.doneOverlay(s)) {
                return false;
            }
            runtimeStarted = true;
            s.effect.clearOverlay();
        }
        return runtime.tick(s);
    }
}
