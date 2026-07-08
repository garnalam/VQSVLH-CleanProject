import java.util.Arrays;

final class VqsvSourceEffects {
    private VqsvSourceEffects() {
    }

    static void op5ActorEffect(VqsvIntroDemo.Scene s, int mode, int actorId, int animation, int x, int y) {
        if (mode == 0) {
            s.spawnActorEffect(-1, animation);
        } else if (mode == 1 && (x != 0 || y != 0)) {
            s.tempSprites.add(new TempSprite(x, y, animation, 120));
        } else {
            s.spawnActorEffect(actorId, animation);
        }
        s.sourceStateTrace.add("PORTED/APPROX op5 effect mode=" + mode
                + " actor=" + actorId
                + " anim=" + animation
                + " xy=[" + x + "," + y + "]");
    }

    static void op39RefreshPets(VqsvIntroDemo.Scene s) {
        for (SourcePetState pet : s.sourcePets) {
            pet.refreshFromSourceDb();
            s.sourcePetRefreshOps++;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL op39 game.c case39 -> game.g.z[i].I()"
                + " refresh HP=max and skill PP=max count=" + s.sourcePets.size()
                + " refreshOps=" + s.sourcePetRefreshOps
                + " pending full game.b.c() visual/runtime side effects");
    }

    static void op25SetGameFlag(VqsvIntroDemo.Scene s, int arg0) {
        s.sourceGameCF = arg0 == 0;
        s.sourceStateTrace.add("PORTED op25 game.c.f=" + s.sourceGameCF + " arg0=" + arg0);
    }

    static Blocking op9SourceEffect(VqsvIntroDemo.Scene s, String context, int... args) {
        int effectId = args.length > 0 ? args[0] : -1;
        switch (effectId) {
            case 1:
            case 2: {
                int color = sourceEffectColor(args);
                s.effect.startFade(effectId, color);
                s.sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                        + " -> b.a().c(color,id) fade id=" + effectId
                        + " color=0x" + String.format("%06X", color & 0xFFFFFF));
                return s.effect::doneOverlay;
            }
            case 10:
                s.effect.startFlash(argOrZero(args, 1), argOrZero(args, 2));
                s.sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                        + " -> b.a().d flash/toggle path");
                return s.effect::doneOverlay;
            case 12:
            case 13:
                s.effect.startBars(effectId, Math.max(1, argOrZero(args, 1)),
                        Math.max(1, argOrZero(args, 2)), Math.max(1, argOrZero(args, 3)),
                        Math.max(0, argOrZero(args, 4)), Math.max(0, argOrZero(args, 5)));
                s.sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                        + " -> b.a().a bar transition path");
                return s.effect::doneBars;
            case 16:
                if (argOrZero(args, 1) == 0) {
                    s.effect.startParticles(Math.max(1, argOrZero(args, 2)) * 10);
                } else if (argOrZero(args, 1) == 1 || argOrZero(args, 1) == 2) {
                    s.effect.startFireParticles(Math.max(1, argOrZero(args, 2)) * 10);
                } else {
                    s.effect.stopParticles();
                }
                s.sourceStateTrace.add("PORTED/APPROX " + context + " op9 " + Arrays.toString(args)
                        + " -> source particle texture family");
                return null;
            case 14:
            case 15:
            case 17:
                s.sourceStateTrace.add("PENDING " + context + " op9 " + Arrays.toString(args)
                        + " source id handled by b.a() actor/texture path; not used as full renderer yet");
                return null;
            default:
                s.sourceStateTrace.add("UNKNOWN " + context + " op9 " + Arrays.toString(args)
                        + " unsupported source effect id");
                return null;
        }
    }

    static void op67SetBattleActor(VqsvIntroDemo.Scene s, int actorId) {
        s.worldEventActor = actorId;
        s.battleEventActor = actorId;
        s.sourceStateTrace.add("PORTED room0 group6 op67 game.k.v=" + actorId);
    }

    static void op56ActorVisibility(VqsvIntroDemo.Scene s, int mode, int[] ids, int[] states) {
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            if (id < 0 || id >= s.actors.length || s.actors[id] == null) {
                continue;
            }
            Actor actor = s.actors[id];
            if (mode == 0) {
                actor.visible = true;
                actor.direction = states[Math.min(i, states.length - 1)];
                actor.applyMode(0);
            } else if (mode == 1) {
                actor.visible = false;
                actor.applyMode(0);
            }
        }
        s.sourceStateTrace.add("PORTED/APPROX op56 mode=" + mode + " ids=" + Arrays.toString(ids)
                + " states=" + Arrays.toString(states));
    }

    private static int argOrZero(int[] args, int index) {
        return index >= 0 && index < args.length ? args[index] : 0;
    }

    private static int sourceEffectColor(int[] args) {
        int r = argOrZero(args, 2) & 0xFF;
        int g = argOrZero(args, 3) & 0xFF;
        int b = argOrZero(args, 4) & 0xFF;
        return (r << 16) | (g << 8) | b;
    }
}
