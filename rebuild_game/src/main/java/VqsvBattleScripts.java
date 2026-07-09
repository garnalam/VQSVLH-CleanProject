final class VqsvBattleScripts {
    private VqsvBattleScripts() {
    }

    static Blocking room1BunnyBattleCaptureRuntime(VqsvIntroDemo.Scene s) {
        VqsvSourceStoryState.ensureInitialDienMieu(s, "room1 group0 Bunny battle entry");
        s.sourceStateTrace.add("PORTED/APPROX room1 group0 op52 this.i=true game.c.j=false args=[0,1]");
        s.sourceStateTrace.add("PORTED/APPROX room1 group0 op66 an.U=0");
        VqsvBattleEventDescriptor descriptor = VqsvBattleEventDescriptor.SCENE1_ROOM1_GROUP0_BUNNY;
        return new BattleEntryTransitionThenRuntime(descriptor.runtime(s, -1), 6, descriptor.op32Mode(0));
    }

    static Blocking room0Group6ElderBattleRuntime(VqsvIntroDemo.Scene s) {
        return VqsvBattleEventDescriptor.SCENE1_ROOM0_GROUP6_ELDER.runtime(s, 0);
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
